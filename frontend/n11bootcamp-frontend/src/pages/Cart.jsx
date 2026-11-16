import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useKeycloakAuth } from "../auth/KeycloakProvider.jsx";
import { getKeycloakInstance } from "../auth/keycloakRuntime";
import {
  addCartItem,
  describeCartAddSuccess,
  getCart,
  removeCartItem,
  updateCartItemAmount,
} from "../services/cartService";
import { useToast } from "../context/ToastContext.jsx";
import { createOrder, getOrderById } from "../services/orderService";
import { formatTry } from "../utils/money";
import { getUserIdFromKeycloak } from "../utils/user";
import {
  clearPendingCheckout,
  isOrderAwaitingPayment,
  readPendingCheckout,
  writePendingCheckout,
} from "../utils/pendingCheckoutStorage";

export default function Cart() {
  const navigate = useNavigate();
  const { authenticated, login } = useKeycloakAuth();
  const { showToast } = useToast();

  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);
  const [ordering, setOrdering] = useState(false);
  const [orderId, setOrderId] = useState(null);
  const [lastOrder, setLastOrder] = useState(null);
  const [orderError, setOrderError] = useState("");

  const userId = useMemo(
    () =>
      authenticated ? getUserIdFromKeycloak(getKeycloakInstance()) : null,
    [authenticated],
  );

  async function refresh() {
    setLoading(true);
    setError("");
    try {
      const cartData = await getCart();
      setCart(cartData);
    } catch (e) {
      const msg =
        e?.response?.data?.data?.detail ??
        e?.response?.data?.message ??
        "Sepet yüklenemedi";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!authenticated) {
      setLoading(false);
      setCart(null);
      return;
    }
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [authenticated]);

  useEffect(() => {
    if (!authenticated || !userId) return;

    let cancelled = false;

    (async () => {
      const stored = readPendingCheckout(userId);
      if (!stored?.id) return;

      try {
        const o = await getOrderById(stored.id);
        if (cancelled) return;

        const st = String(o?.status ?? "");
        if (st === "PAID" || st === "CANCELLED") {
          clearPendingCheckout(userId);
          return;
        }
        if (isOrderAwaitingPayment(o?.status)) {
          setLastOrder(o);
          setOrderId(o.id);
          writePendingCheckout(userId, o);
        } else {
          clearPendingCheckout(userId);
        }
      } catch {
        clearPendingCheckout(userId);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [authenticated, userId]);

  if (!authenticated) {
    return (
      <div className="mx-auto max-w-lg rounded-3xl border border-slate-200 bg-white p-6">
        <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
          Sepet
        </h1>
        <p className="mt-2 text-sm text-slate-600">
          Sepet ve sipariş işlemleri için giriş yapmalısın.
        </p>
        <button
          type="button"
          onClick={() => login({ redirectUri: window.location.href })}
          className="mt-5 inline-flex w-full items-center justify-center rounded-2xl bg-slate-900 px-4 py-3 text-sm font-extrabold text-white hover:bg-slate-800"
        >
          Giriş Yap
        </button>
      </div>
    );
  }

  const items = cart?.items ?? [];
  const hasOpenPaymentOrder =
    Boolean(lastOrder?.id) && isOrderAwaitingPayment(lastOrder?.status);

  return (
    <section className="space-y-6">
      <div className="flex items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
            Sepet
          </h1>
          <p className="mt-1 text-sm text-slate-600">
            {items.length ? `${items.length} ürün` : "Sepetin boş"}
          </p>
        </div>
        <Link
          to="/products"
          className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
        >
          Alışverişe devam et
        </Link>
      </div>

      {loading ? (
        <div className="rounded-3xl border border-slate-200 bg-white p-6 text-sm text-slate-700">
          Yükleniyor...
        </div>
      ) : error ? (
        <div className="rounded-3xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">
          {error}
        </div>
      ) : (
        <div className="grid gap-6 lg:grid-cols-12">
          <div className="space-y-3 lg:col-span-7">
            {items.length ? (
              items.map((it) => {
                const productId = it.productId;
                const title = it.productName;
                const sku = it.productSku;
                const amount = it.amount;
                const unitPrice = it.unitPrice;
                const totalPrice = it.totalPrice;

                return (
                  <div
                    key={productId}
                    className="rounded-3xl border border-slate-200 bg-white p-5"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="min-w-0">
                        <div className="truncate text-sm font-extrabold text-slate-900">
                          {title}
                        </div>
                        <div className="mt-1 text-xs text-slate-500">
                          {sku ? `SKU: ${sku}` : null}
                        </div>
                        <div className="mt-3 flex flex-wrap items-center gap-3 text-sm">
                          <div className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-2 py-1">
                            <button
                              type="button"
                              disabled={busyId === productId}
                              onClick={async () => {
                                setBusyId(productId);
                                try {
                                  await updateCartItemAmount(productId, amount - 1);
                                  await refresh();
                                } finally {
                                  setBusyId(null);
                                }
                              }}
                              className="inline-flex h-7 w-7 items-center justify-center rounded-full border border-slate-200 bg-white text-xs font-extrabold text-slate-800 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                              aria-label="Azalt"
                            >
                              −
                            </button>
                            <div className="min-w-10 text-center text-xs font-extrabold text-slate-800">
                              {amount}
                            </div>
                            <button
                              type="button"
                              disabled={busyId === productId}
                              onClick={async () => {
                                setBusyId(productId);
                                try {
                                  const cart = await addCartItem({
                                    productId,
                                    amount: 1,
                                  });
                                  showToast(
                                    describeCartAddSuccess(cart, productId),
                                  );
                                  await refresh();
                                } finally {
                                  setBusyId(null);
                                }
                              }}
                              className="inline-flex h-7 w-7 items-center justify-center rounded-full border border-slate-200 bg-white text-xs font-extrabold text-slate-800 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                              aria-label="Arttır"
                            >
                              +
                            </button>
                          </div>
                          {unitPrice != null ? (
                            <div className="text-sm font-extrabold text-slate-900">
                              {formatTry(unitPrice)}
                              <span className="ml-1 text-xs font-semibold text-slate-500">
                                / adet
                              </span>
                            </div>
                          ) : null}
                          {totalPrice != null ? (
                            <div className="text-sm font-extrabold text-violet-700">
                              {formatTry(totalPrice)}
                            </div>
                          ) : null}
                        </div>
                      </div>

                      <button
                        type="button"
                        disabled={busyId === productId}
                        onClick={async () => {
                          setBusyId(productId);
                          try {
                            await removeCartItem(productId);
                            await refresh();
                          } finally {
                            setBusyId(null);
                          }
                        }}
                        className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        {busyId === productId ? "Siliniyor..." : "Kaldır"}
                      </button>
                    </div>
                  </div>
                );
              })
            ) : (
              <div className="rounded-3xl border border-slate-200 bg-white p-6 text-sm text-slate-700">
                Sepetin boş.
              </div>
            )}
          </div>

          <div className="space-y-4 lg:col-span-5">
            <div className="rounded-3xl border border-slate-200 bg-white p-6">
              <div className="text-sm font-extrabold text-slate-900">
                Sipariş Özeti
              </div>
              <div className="mt-4 flex items-center justify-between text-sm">
                <span className="text-slate-600">Ara toplam</span>
                <span className="font-extrabold text-slate-900">
                  {formatTry(cart?.grandTotal)}
                </span>
              </div>

              {hasOpenPaymentOrder ? (
                <div className="mt-5 rounded-2xl border border-violet-200 bg-gradient-to-br from-violet-50 to-white px-4 py-4 text-sm text-slate-800 shadow-sm">
                  <div className="text-xs font-extrabold uppercase tracking-wide text-violet-700">
                    Ödeme bekleyen sipariş
                  </div>
                  <div className="mt-2 break-all font-mono text-xs text-slate-700">
                    {String(lastOrder.id)}
                  </div>
                  <div className="mt-2 text-xs text-slate-600">
                    Durum:{" "}
                    <span className="font-bold text-slate-900">
                      {String(lastOrder.status ?? "—")}
                    </span>
                  </div>
                  <div className="mt-4 flex flex-col gap-2">
                    <button
                      type="button"
                      onClick={() =>
                        navigate(
                          `/payment?orderId=${lastOrder.id}&grandTotal=${encodeURIComponent(
                            String(lastOrder.grandTotal ?? ""),
                          )}`,
                        )
                      }
                      className="inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-3 py-2.5 text-xs font-extrabold text-white hover:bg-slate-800"
                    >
                      Ödemeye Git
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        if (userId) clearPendingCheckout(userId);
                        setLastOrder(null);
                        setOrderId(null);
                      }}
                      className="inline-flex w-full items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-xs font-extrabold text-slate-800 hover:bg-slate-50"
                    >
                      Bu siparişi burada gösterme
                    </button>
                    <p className="text-[11px] leading-snug text-slate-500">
                      Bu düğme yalnızca bu oturumdaki kaydı siler; sipariş
                      sunucuda duruyorsa sistemde görünmeye devam edebilir.
                    </p>
                  </div>
                </div>
              ) : null}

              {hasOpenPaymentOrder ? (
                <p className="mt-3 text-xs text-slate-600">
                  Yeni sipariş oluşturmak için önce ödemeyi tamamlayın veya yukarıdan
                  bu kaydı kaldırın.
                </p>
              ) : null}

              <button
                type="button"
                disabled={!items.length || ordering || hasOpenPaymentOrder}
                onClick={async () => {
                  setOrdering(true);
                  setOrderError("");
                  setOrderId(null);
                  try {
                    const o = await createOrder();
                    setOrderId(o?.id ?? null);
                    setLastOrder(o);
                    if (userId && o?.id) {
                      writePendingCheckout(userId, o);
                    }
                    await refresh();
                    if (o?.id && String(o?.status) === "AWAITING_PAYMENT") {
                      navigate(
                        `/payment?orderId=${o.id}&grandTotal=${encodeURIComponent(
                          String(o.grandTotal ?? ""),
                        )}`,
                      );
                    }
                  } catch (e) {
                    const msg =
                      e?.response?.data?.data?.detail ??
                      e?.response?.data?.message ??
                      "Sipariş oluşturulamadı";
                    setOrderError(msg);
                  } finally {
                    setOrdering(false);
                  }
                }}
                className="mt-6 inline-flex w-full items-center justify-center rounded-2xl bg-slate-900 px-4 py-3 text-sm font-extrabold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {ordering ? "Oluşturuluyor..." : "Sipariş Oluştur"}
              </button>

              {!hasOpenPaymentOrder && orderId ? (
                <div className="mt-4 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
                  Sipariş oluşturuldu. ID:{" "}
                  <span className="font-mono font-semibold">{orderId}</span>
                </div>
              ) : null}

              {orderError ? (
                <div className="mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                  {orderError}
                </div>
              ) : null}
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

