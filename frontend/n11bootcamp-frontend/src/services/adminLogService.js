import { httpClient } from "./httpClient";

export async function adminGetLogs({ page = 0, size = 20 } = {}) {
  const res = await httpClient.get("/api/v1/logs", { params: { page, size } });
  return res.data;
}

export async function adminGetLogsByUserId(userId, { page = 0, size = 20 } = {}) {
  const res = await httpClient.get(`/api/v1/logs/user/${encodeURIComponent(userId)}`, {
    params: { page, size },
  });
  return res.data;
}

export async function adminGetLogsByServiceName(
  serviceName,
  { page = 0, size = 20 } = {},
) {
  const res = await httpClient.get(
    `/api/v1/logs/service/${encodeURIComponent(serviceName)}`,
    { params: { page, size } },
  );
  return res.data;
}

export async function adminGetLogsByOrderId(orderId, { page = 0, size = 20 } = {}) {
  const res = await httpClient.get(`/api/v1/logs/order/${encodeURIComponent(orderId)}`, {
    params: { page, size },
  });
  return res.data;
}

