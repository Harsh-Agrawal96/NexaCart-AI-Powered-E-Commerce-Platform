import { httpClient } from "./httpClient";

export async function pay({ orderId, providerType, iyzico }) {
  const body = {
    orderId,
    providerType,
    ...(iyzico ? { ...iyzico } : {}),
  };

  const res = await httpClient.post("/api/v1/payment", body);
  return res.data ?? res;
}

