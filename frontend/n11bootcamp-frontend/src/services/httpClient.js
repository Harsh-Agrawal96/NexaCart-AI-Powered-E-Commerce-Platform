import axios from "axios";
import { getKeycloakInstance } from "../auth/keycloakRuntime";
import { getUserIdFromKeycloak } from "../utils/user";

/** Cart/order kullanıcı endpoint’leri OpenAPI’de X-User-Id istiyor; admin rotalarına gönderilmez. */
function shouldAttachXUserId(config) {
  const raw = String(config.url ?? "");
  const path = raw.startsWith("http") ? new URL(raw).pathname : raw;
  const method = String(config.method ?? "get").toLowerCase();
  if (path.includes("/api/v1/cart")) return true;
  if (method === "post" && /\/api\/v1\/orders\/?(\?.*)?$/.test(path)) return true;
  if (method === "get" && /^\/api\/v1\/orders\/[^/]+$/.test(path)) return true;
  if (path.startsWith("/api/v1/reviews")) return true; // user ops: create/update/delete
  if (path.startsWith("/api/v1/ai/")) return true;
  // Ürün detayında davranış log’u: GET /api/v1/products/{id} (liste /api/v1/products değil)
  if (
    method === "get" &&
    /^\/api\/v1\/products\/[^/]+$/.test(path)
  ) {
    return true;
  }
  return false;
}

const envBase = import.meta.env.VITE_API_BASE_URL;
const defaultBase =
  import.meta.env.DEV && (envBase === undefined || envBase === "")
    ? ""
    : (envBase ?? "http://localhost:8080");

export const httpClient = axios.create({
  baseURL: defaultBase,
  headers: {
    "Content-Type": "application/json",
  },
});

httpClient.interceptors.request.use(async (config) => {
  const kc = getKeycloakInstance();
  if (!kc?.authenticated) return config;

  try {
    // Don't force refresh when no refresh token exists (anonymous / partial sessions)
    if (!kc.refreshToken) {
      config.headers = config.headers ?? {};
      if (kc.token) config.headers.Authorization = `Bearer ${kc.token}`;
      const uid = getUserIdFromKeycloak(kc);
      if (uid && shouldAttachXUserId(config)) {
        config.headers["X-User-Id"] = uid;
      }
      return config;
    }

    await kc.updateToken(10);
  } catch {
    // Token refresh failed — request may still proceed without auth depending on endpoint
  }

  config.headers = config.headers ?? {};
  if (kc.token) config.headers.Authorization = `Bearer ${kc.token}`;

  const uid = getUserIdFromKeycloak(kc);
  if (uid && shouldAttachXUserId(config)) {
    config.headers["X-User-Id"] = uid;
  }

  return config;
});

httpClient.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error?.config;
    const status = error?.response?.status;
    if (!original || status !== 401 || original._retry) throw error;

    original._retry = true;

    try {
      const kc = getKeycloakInstance();
      if (!kc?.authenticated) throw error;

      if (!kc.refreshToken) throw error;

      await kc.updateToken(30);
      original.headers = original.headers ?? {};
      if (kc.token) original.headers.Authorization = `Bearer ${kc.token}`;
      const uid = getUserIdFromKeycloak(kc);
      if (uid && shouldAttachXUserId(original)) {
        original.headers["X-User-Id"] = uid;
      }
      return httpClient(original);
    } catch (e) {
      throw e;
    }
  },
);

