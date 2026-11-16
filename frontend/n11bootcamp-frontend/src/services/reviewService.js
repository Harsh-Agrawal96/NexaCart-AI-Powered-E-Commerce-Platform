import { httpClient } from "./httpClient";

function unwrapApi(body) {
  if (body && typeof body === "object" && "success" in body && body.data !== undefined) {
    return body.data ?? null;
  }
  return body ?? null;
}

export async function listReviewsByProduct(
  { productId, ratingMin, ratingMax, page = 0, size = 10, sort = "createdAt,desc" },
  extraParams = {},
) {
  const params = {
    productId,
    page,
    size,
    sort,
    ...extraParams,
  };
  if (ratingMin != null && ratingMin !== "") params.ratingMin = ratingMin;
  if (ratingMax != null && ratingMax !== "") params.ratingMax = ratingMax;

  const res = await httpClient.get("/api/v1/reviews", { params });
  return unwrapApi(res.data); // PageResponseReviewResponse
}

export async function createReview({ productId, rating, comment }) {
  const res = await httpClient.post("/api/v1/reviews", { productId, rating, comment });
  return unwrapApi(res.data); // ReviewResponse
}

export async function updateMyReview(id, { rating, comment }) {
  const res = await httpClient.patch(`/api/v1/reviews/${id}`, { rating, comment });
  return unwrapApi(res.data); // ReviewResponse
}

export async function deleteMyReview(id) {
  const res = await httpClient.delete(`/api/v1/reviews/${id}`);
  return unwrapApi(res.data);
}

