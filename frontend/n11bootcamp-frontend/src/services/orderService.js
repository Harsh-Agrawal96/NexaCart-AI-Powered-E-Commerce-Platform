import { httpClient } from "./httpClient";

function unwrapApi(body) {
  if (body && typeof body === "object" && "success" in body && body.data !== undefined) {
    return body.data ?? null;
  }
  return body ?? null;
}

export async function createOrder() {
  const res = await httpClient.post("/api/v1/orders");
  return unwrapApi(res.data);
}

export async function getOrderById(id) {
  const res = await httpClient.get(`/api/v1/orders/${id}`);
  return unwrapApi(res.data);
}
