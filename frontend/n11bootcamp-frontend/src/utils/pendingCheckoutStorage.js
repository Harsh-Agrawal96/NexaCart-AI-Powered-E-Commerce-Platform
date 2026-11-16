/**
 * Ödeme bekleyen sipariş özeti (aynı sekme oturumunda sepete dönünce korunur).
 * Anahtar: Keycloak sub — kullanıcılar arası karışmaz.
 */
const PREFIX = "n11.pendingCheckout.v1";

/**
 * @param {string} userId
 * @returns {{ id: string, grandTotal?: unknown, status?: string } | null}
 */
export function readPendingCheckout(userId) {
  if (!userId) return null;
  try {
    const raw = sessionStorage.getItem(`${PREFIX}:${userId}`);
    if (!raw) return null;
    const o = JSON.parse(raw);
    if (!o?.id) return null;
    return o;
  } catch {
    return null;
  }
}

/**
 * @param {string} userId
 * @param {{ id: string, grandTotal?: unknown, status?: string }} order
 */
export function writePendingCheckout(userId, order) {
  if (!userId || !order?.id) return;
  try {
    sessionStorage.setItem(
      `${PREFIX}:${userId}`,
      JSON.stringify({
        id: order.id,
        grandTotal: order.grandTotal,
        status: order.status,
      }),
    );
  } catch {
    // kotası / gizli mod
  }
}

/** @param {string} userId */
export function clearPendingCheckout(userId) {
  if (!userId) return;
  try {
    sessionStorage.removeItem(`${PREFIX}:${userId}`);
  } catch {
    // ignore
  }
}

/** Sipariş ödeme ekranına gidilebilir mi (sunucu durumu) */
export function isOrderAwaitingPayment(status) {
  const s = String(status ?? "");
  return s === "PENDING" || s === "AWAITING_PAYMENT";
}
