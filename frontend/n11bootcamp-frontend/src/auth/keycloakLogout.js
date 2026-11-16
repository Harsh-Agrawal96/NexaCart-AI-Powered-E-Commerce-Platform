import { getKeycloakInstance } from "./keycloakRuntime";

/**
 * Keycloak JS default POST logout sometimes submits empty id_token_hint fields which can cause 400s.
 * Using createLogoutUrl() + GET navigation matches Keycloak's expected query params.
 */
export function logoutFromKeycloak({ redirectUri } = {}) {
  const kc = getKeycloakInstance();
  if (!kc) return;

  const method =
    import.meta.env.VITE_KEYCLOAK_LOGOUT_METHOD === "POST" ? "POST" : "GET";

  if (method === "GET") {
    const url = kc.createLogoutUrl({ redirectUri });
    window.location.replace(url);
    return;
  }

  // Explicit POST path (rare). Prefer GET unless you know your Keycloak requires POST.
  void kc.logout({ redirectUri, logoutMethod: "POST" });
}
