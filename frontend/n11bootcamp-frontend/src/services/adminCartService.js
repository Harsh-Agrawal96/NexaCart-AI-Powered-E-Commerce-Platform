import { httpClient } from "./httpClient";

export async function adminListCarts(params = {}) {
  const res = await httpClient.get("/api/v1/admin/carts", { params });
  return res.data;
}

export async function adminGetCartById(id) {
  const res = await httpClient.get(`/api/v1/admin/carts/${id}`);
  return res.data;
}
