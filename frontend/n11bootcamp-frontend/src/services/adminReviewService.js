import { httpClient } from "./httpClient";

function unwrapApi(body) {
  if (body && typeof body === "object" && "success" in body && body.data !== undefined) {
    return body.data ?? null;
  }
  return body ?? null;
}

export async function adminSearchReviews(params = {}) {
  const res = await httpClient.get("/api/v1/admin/reviews", { params });
  return unwrapApi(res.data); // PageResponseReviewResponse
}

export async function adminGetReviewById(id) {
  const res = await httpClient.get(`/api/v1/admin/reviews/${id}`);
  return unwrapApi(res.data); // ReviewResponse
}

export async function adminUpdateReview(id, payload) {
  const res = await httpClient.patch(`/api/v1/admin/reviews/${id}`, payload);
  return unwrapApi(res.data); // ReviewResponse
}

export async function adminDeleteReview(id) {
  const res = await httpClient.delete(`/api/v1/admin/reviews/${id}`);
  return unwrapApi(res.data);
}

