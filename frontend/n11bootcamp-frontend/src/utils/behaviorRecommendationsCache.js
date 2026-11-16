/**
 * Davranış önerileri için sekme içi önbellek (sessionStorage).
 * Başarılı yanıtlar kısa TTL; hata ve boş yanıtlar daha uzun TTL ile tekrar isteği keser.
 */

const STORAGE_PREFIX = "n11.br.v1";

/** Aynı anahtar için eşzamanlı tek istek */
const pending = new Map();

export function buildBehaviorRecCacheKey({ userId, currentProductId, limit }) {
  const pid =
    currentProductId === undefined || currentProductId === null
      ? ""
      : String(currentProductId);
  return `${STORAGE_PREFIX}:${userId}:${pid}:${limit}`;
}

/**
 * @param {string} key
 * @param {{ successTtlMs: number, errorTtlMs: number, emptyTtlMs: number }} ttls
 * @returns {{ kind: 'success'|'error'|'empty', savedAt: number, data?: unknown, message?: string } | null}
 */
export function readBehaviorRecCache(key, ttls) {
  try {
    const raw = sessionStorage.getItem(key);
    if (!raw) return null;
    let obj;
    try {
      obj = JSON.parse(raw);
    } catch {
      return null;
    }
    const { savedAt, kind } = obj;
    if (typeof savedAt !== "number" || !kind) return null;
    const age = Date.now() - savedAt;
    let ttlMs = ttls.successTtlMs;
    if (kind === "error") ttlMs = ttls.errorTtlMs;
    else if (kind === "empty") ttlMs = ttls.emptyTtlMs;
    if (age > ttlMs) {
      sessionStorage.removeItem(key);
      return null;
    }
    return obj;
  } catch {
    return null;
  }
}

/**
 * @param {string} key
 * @param {{ kind: 'success'|'error'|'empty', data?: unknown, message?: string }} payload
 */
export function writeBehaviorRecCache(key, payload) {
  try {
    const entry = {
      ...payload,
      savedAt: Date.now(),
    };
    sessionStorage.setItem(key, JSON.stringify(entry));
  } catch {
    // kotası / gizli mod — sessizce yoksay
  }
}

/**
 * @template T
 * @param {string} key
 * @param {() => Promise<T>} fetcher
 * @returns {Promise<T>}
 */
export function dedupeBehaviorRecFetch(key, fetcher) {
  const existing = pending.get(key);
  if (existing) return existing;
  const p = fetcher().finally(() => {
    pending.delete(key);
  });
  pending.set(key, p);
  return p;
}

function hasRenderableContent(res) {
  const products = Array.isArray(res?.recommendedProducts)
    ? res.recommendedProducts
    : [];
  const summary = typeof res?.summary === "string" ? res.summary.trim() : "";
  return products.length > 0 || summary.length > 0;
}

/**
 * API gövdesinden önbellek türünü seçer.
 * @param {unknown} res
 * @returns {'success'|'empty'}
 */
export function classifyBehaviorRecResponse(res) {
  return hasRenderableContent(res) ? "success" : "empty";
}
