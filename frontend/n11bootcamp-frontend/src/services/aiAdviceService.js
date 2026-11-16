import { httpClient } from "./httpClient";

/**
 * Ürün + yorum özetiyle yapay zekâdan tavsiye alır (gateway: /api/v1/ai → ai-service).
 * @param {{ productId: string, question?: string }} payload
 */
export async function requestProductAdvice(payload) {
  const { data } = await httpClient.post(
    "/api/v1/ai/product-advice",
    {
      productId: payload.productId,
      question: payload.question ?? "",
    },
  );
  return data?.data ?? data;
}

/**
 * Kullanıcı davranışlarına göre ürün önerisi (giriş zorunlu; body.userId = Keycloak sub).
 * @param {{ userId: string, currentProductId?: string|null, limit?: number }} payload
 */
export async function requestBehaviorRecommendations(payload) {
  const body = {
    userId: payload.userId,
    limit: payload.limit ?? 10,
  };
  if (payload.currentProductId) {
    body.currentProductId = payload.currentProductId;
  }
  const { data } = await httpClient.post(
    "/api/v1/ai/behavior-recommendations",
    body,
  );
  return data?.data ?? data;
}
