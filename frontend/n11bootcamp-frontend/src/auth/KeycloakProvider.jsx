import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { createKeycloakClient } from "./keycloakSingleton";
import { setKeycloakInstance } from "./keycloakRuntime";

const KeycloakCtx = createContext(null);

export function KeycloakProvider({ children }) {
  const [kc] = useState(() => createKeycloakClient());
  const [ready, setReady] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function init() {
      const onLoad = import.meta.env.VITE_KEYCLOAK_ONLOAD ?? "check-sso";
      // 3rd-party cookie / iframe session checks can block updateToken() and hang API calls.
      // For a public e-commerce catalog, this is usually better disabled unless you explicitly need it.
      const checkLoginIframe =
        import.meta.env.VITE_KEYCLOAK_CHECK_LOGIN_IFRAME === "true";
      await kc.init({
        onLoad,
        pkceMethod: "S256",
        checkLoginIframe,
      });

      if (cancelled) return;

      setKeycloakInstance(kc);
      setAuthenticated(!!kc.authenticated);
      setReady(true);

      // Token refresh lifecycle (iframe-based refresh for public clients)
      kc.onTokenExpired = () => {
        if (!kc.refreshToken) return;
        kc.updateToken(30).catch(() => {});
      };

      // Keep React UI in sync with Keycloak session lifecycle
      kc.onAuthSuccess = () => setAuthenticated(!!kc.authenticated);
      kc.onAuthError = () => setAuthenticated(false);
      kc.onAuthLogout = () => setAuthenticated(false);

      kc.onAuthRefreshSuccess = () => setAuthenticated(!!kc.authenticated);
      kc.onAuthRefreshError = () => setAuthenticated(false);

    }

    init();
    return () => {
      cancelled = true;
    };
  }, [kc]);

  const value = useMemo(() => {
    return {
      keycloak: kc,
      ready,
      authenticated,
      login: (opts) => kc.login(opts),
      logout: (opts) => kc.logout(opts),
      token: () => kc.token,
      updateToken: (minValidity = 30) => kc.updateToken(minValidity),
    };
  }, [authenticated, kc, ready]);

  return <KeycloakCtx.Provider value={value}>{children}</KeycloakCtx.Provider>;
}

export function useKeycloakAuth() {
  const v = useContext(KeycloakCtx);
  if (!v) throw new Error("useKeycloakAuth must be used within KeycloakProvider");
  return v;
}
