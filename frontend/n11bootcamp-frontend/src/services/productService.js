import { httpClient } from "./httpClient";

export async function getProducts(params = {}) {
  const res = await httpClient.get("/api/v1/products", { params });
  return res.data;
}

export async function getProductById(id) {
  const res = await httpClient.get(`/api/v1/products/${id}`);
  return res.data;
}

