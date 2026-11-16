import Keycloak from "keycloak-js";

export function createKeycloakClient() {
  const url = import.meta.env.VITE_KEYCLOAK_URL ?? "http://localhost:8090";
  const realm = import.meta.env.VITE_KEYCLOAK_REALM ?? "n11bootcamp";
  const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? "frontend";

  return new Keycloak({ url, realm, clientId });
}
