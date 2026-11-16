import { httpClient } from "./httpClient";

export async function adminSearchOrders(params = {}) {
  const res = await httpClient.get("/api/v1/admin/orders", { params });
  return res.data;
}

export async function adminGetOrderById(id) {
  const res = await httpClient.get(`/api/v1/admin/orders/${id}`);
  return res.data;
}
