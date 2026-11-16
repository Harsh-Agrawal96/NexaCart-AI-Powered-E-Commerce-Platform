import { httpClient } from "./httpClient";

export async function adminGetProducts(params = {}) {
  const res = await httpClient.get("/api/v1/admin/products", { params });
  return res.data;
}

export async function adminGetProductById(id) {
  const res = await httpClient.get(`/api/v1/admin/products/${id}`);
  return res.data;
}

export async function adminCreateProduct(payload) {
  const res = await httpClient.post("/api/v1/admin/products", payload);
  return res.data;
}

export async function adminUpdateProduct(id, payload) {
  const res = await httpClient.put(`/api/v1/admin/products/${id}`, payload);
  return res.data;
}

export async function adminDeleteProduct(id) {
  const res = await httpClient.delete(`/api/v1/admin/products/${id}`);
  return res.data;
}

