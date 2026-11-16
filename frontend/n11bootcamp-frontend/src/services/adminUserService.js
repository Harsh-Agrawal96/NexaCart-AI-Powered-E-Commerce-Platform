import { httpClient } from "./httpClient";

// Note: User service OpenAPI exposes list/create/update/delete under /api/v1/users (not /admin).
export async function adminListUsers(params = {}) {
  const res = await httpClient.get("/api/v1/users", { params });
  return res.data;
}

export async function adminGetUserById(id) {
  const res = await httpClient.get(`/api/v1/users/${id}`);
  return res.data;
}

export async function adminCreateUser(payload) {
  const res = await httpClient.post("/api/v1/users", payload);
  return res.data;
}

export async function adminUpdateUser(id, payload) {
  const res = await httpClient.put(`/api/v1/users/${id}`, payload);
  return res.data;
}

export async function adminDeleteUser(id) {
  const res = await httpClient.delete(`/api/v1/users/${id}`);
  return res.data;
}

// Admin endpoints
export async function adminGetDeletedUser(id) {
  const res = await httpClient.get(`/api/v1/admin/users/${id}/deleted`);
  return res.data;
}

export async function adminRestoreUser(id) {
  const res = await httpClient.patch(`/api/v1/admin/users/${id}/restore`);
  return res.data;
}

