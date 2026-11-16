import { httpClient } from "./httpClient";

export async function adminGetCategories() {
  const res = await httpClient.get("/api/v1/admin/categories");
  return res.data;
}

export async function adminGetCategoryById(id) {
  const res = await httpClient.get(`/api/v1/admin/categories/${id}`);
  return res.data;
}

export async function adminCreateCategory(payload) {
  const res = await httpClient.post("/api/v1/admin/categories", payload);
  return res.data;
}

export async function adminUpdateCategory(id, payload) {
  const res = await httpClient.put(`/api/v1/admin/categories/${id}`, payload);
  return res.data;
}

export async function adminDeleteCategory(id) {
  const res = await httpClient.delete(`/api/v1/admin/categories/${id}`);
  return res.data;
}

