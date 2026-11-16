import { httpClient } from "./httpClient";
import { formatTry } from "../utils/money";

/**
 * ApiResponseCartResponse veya düz CartResponse.
 * CartResponse: cartId, userId, items[], grandTotal
 */
export function normalizeCartPayload(body) {
  if (body == null || typeof body !== "object") return null;

  let cart = body;
  if ("success" in body && body.data !== undefined) {
    cart = body.data;
  }
  if (!cart || typeof cart !== "object") return null;

  const rawList =
    cart.items ??
    cart.cartItems ??
    cart.lines ??
    [];

  const items = Array.isArray(rawList)
    ? rawList.map((it) => ({
        ...it,
        productId: it.productId ?? it.product_id,
        productName: it.productName ?? it.product_name ?? it.product?.name,
        productSku: it.productSku ?? it.product_sku ?? it.product?.sku,
        amount: Number(it.amount ?? it.quantity ?? 0) || 0,
        unitPrice: it.unitPrice ?? it.unit_price ?? it.product?.price ?? null,
        totalPrice: it.totalPrice ?? it.total_price ?? null,
      }))
    : [];

  return {
    ...cart,
    items,
    grandTotal: cart.grandTotal ?? cart.total ?? null,
  };
}

export async function getCart() {
  const res = await httpClient.get("/api/v1/cart");
  return normalizeCartPayload(res.data);
}

export async function addCartItem({ productId, amount }) {
  const res = await httpClient.post("/api/v1/cart/items", { productId, amount });
  return normalizeCartPayload(res.data);
}

/**
 * Sepete ekleme yanıtından toast metni (ürün adı, adet, sepet toplamı).
 * @param {object|null|undefined} cart normalizeCartPayload çıktısı
 * @param {string} productId
 */
export function describeCartAddSuccess(cart, productId) {
  const pid = String(productId);
  if (!cart?.items?.length) {
    return { title: "Ürün sepete eklendi." };
  }
  const line = cart.items.find((it) => String(it.productId) === pid);
  const name = line?.productName?.trim() || "Ürün";
  const qty = Number(line?.amount ?? 0) || null;

  const title =
    qty != null && qty > 1 ? `${name} · Sepette ${qty} adet` : `${name} sepete eklendi`;

  let subtitle;
  if (cart.grandTotal != null && cart.grandTotal !== "") {
    subtitle = `Sepet toplamı: ${formatTry(cart.grandTotal)}`;
  }

  return subtitle ? { title, subtitle } : { title };
}

export async function removeCartItem(productId) {
  const res = await httpClient.delete(`/api/v1/cart/items/${productId}`);
  return res.data;
}

export async function updateCartItemAmount(productId, amount) {
  const res = await httpClient.patch(`/api/v1/cart/items/${productId}`, { amount });
  return normalizeCartPayload(res.data);
}
