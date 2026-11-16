import { httpClient } from "./httpClient";

/**
 * Uygulama kaydı: user-service Keycloak kullanıcısı + DB kaydı oluşturur (Keycloak register UI değil).
 * @param {{ firstName: string, lastName: string, email: string, password: string, phoneNumber?: string }} payload
 */
export async function registerAccount(payload) {
  const body = {
    firstName: payload.firstName.trim(),
    lastName: payload.lastName.trim(),
    email: payload.email.trim(),
    password: payload.password,
  };
  const phone = payload.phoneNumber?.trim();
  if (phone) body.phoneNumber = phone;

  const { data } = await httpClient.post("/api/v1/users/register", body);
  return data?.data ?? data;
}
