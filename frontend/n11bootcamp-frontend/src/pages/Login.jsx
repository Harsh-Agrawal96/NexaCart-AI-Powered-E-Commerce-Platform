import { useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useKeycloakAuth } from "../auth/KeycloakProvider.jsx";

export default function Login() {
  const navigate = useNavigate();
  const { ready, authenticated, login } = useKeycloakAuth();

  useEffect(() => {
    if (!ready) return;
    if (authenticated) navigate("/", { replace: true });
  }, [authenticated, navigate, ready]);

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xl">
      <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
        Giriş Yap
      </h1>

      <p className="mt-2 text-sm text-slate-600">
        Keycloak ile güvenli giriş yapın.
      </p>

      <div className="mt-6 space-y-3">
        <button
          type="button"
          disabled={!ready || authenticated}
          onClick={() =>
            login({
              redirectUri: window.location.origin + "/",
            })
          }
          className="inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {!ready ? "Hazırlanıyor..." : "Keycloak ile devam et"}
        </button>

        <p className="text-center text-sm text-slate-600">
          Hesabın yok mu?{" "}
          <Link
            to="/register"
            className="font-semibold text-violet-700 hover:text-violet-900"
          >
            Kayıt ol
          </Link>
        </p>

        <button
          type="button"
          onClick={() => navigate(-1)}
          className="inline-flex w-full items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
        >
          Vazgeç
        </button>
      </div>
    </div>
  );
}

