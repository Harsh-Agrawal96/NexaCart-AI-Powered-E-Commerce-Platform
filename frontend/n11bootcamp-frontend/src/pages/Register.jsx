import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useKeycloakAuth } from "../auth/KeycloakProvider.jsx";
import { registerAccount } from "../services/userService";

export default function Register() {
  const navigate = useNavigate();
  const { ready, authenticated, login } = useKeycloakAuth();

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!ready) return;
    if (authenticated) navigate("/", { replace: true });
  }, [authenticated, navigate, ready]);

  async function onSubmit(e) {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      await registerAccount({
        firstName,
        lastName,
        email,
        password,
        phoneNumber: phoneNumber || undefined,
      });
      login({
        redirectUri: window.location.origin + "/",
      });
    } catch (err) {
      const msg =
        err?.response?.data?.data?.detail ??
        err?.response?.data?.message ??
        err?.message ??
        "Kayıt tamamlanamadı.";
      setError(msg);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-xl">
      <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
        Kayıt Ol
      </h1>

      <p className="mt-2 text-sm text-slate-600">
        Bilgilerini gir; kayıt tamamlanınca güvenli giriş ekranına
        yönlendirilirsin.
      </p>

      <form onSubmit={onSubmit} className="mt-6 space-y-4">
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-600" htmlFor="reg-fn">
              Ad
            </label>
            <input
              id="reg-fn"
              required
              minLength={2}
              maxLength={100}
              autoComplete="given-name"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
            />
          </div>
          <div className="space-y-1">
            <label className="text-xs font-semibold text-slate-600" htmlFor="reg-ln">
              Soyad
            </label>
            <input
              id="reg-ln"
              required
              minLength={2}
              maxLength={100}
              autoComplete="family-name"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
            />
          </div>
        </div>

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-600" htmlFor="reg-email">
            E-posta
          </label>
          <input
            id="reg-email"
            type="email"
            required
            autoComplete="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
          />
        </div>

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-600" htmlFor="reg-pw">
            Şifre (en az 8 karakter)
          </label>
          <input
            id="reg-pw"
            type="password"
            required
            minLength={8}
            autoComplete="new-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
          />
        </div>

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-600" htmlFor="reg-phone">
            Telefon <span className="font-normal text-slate-400">(isteğe bağlı)</span>
          </label>
          <input
            id="reg-phone"
            type="tel"
            inputMode="tel"
            autoComplete="tel"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            placeholder="+905551234567"
            className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
          />
        </div>

        {error ? (
          <div className="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
            {error}
          </div>
        ) : null}

        <button
          type="submit"
          disabled={busy || !ready || authenticated}
          className="inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {busy ? "Kaydediliyor..." : "Kayıt ol"}
        </button>
      </form>

      <p className="mt-4 text-center text-sm text-slate-600">
        Zaten hesabın var mı?{" "}
        <Link
          to="/login"
          className="font-semibold text-violet-700 hover:text-violet-900"
        >
          Giriş yap
        </Link>
      </p>

      <button
        type="button"
        onClick={() => navigate(-1)}
        className="mt-3 inline-flex w-full items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
      >
        Vazgeç
      </button>
    </div>
  );
}
