import { NavLink, useNavigate, useSearchParams } from "react-router-dom";
import { useEffect, useState } from "react";
import CategoryBar from "./CategoryBar";
import { useKeycloakAuth } from "../auth/KeycloakProvider.jsx";
import { logoutFromKeycloak } from "../auth/keycloakLogout";

function Navbar() {
  const navigate = useNavigate();
  const { authenticated, login } = useKeycloakAuth();
  const [searchParams] = useSearchParams();
  const [search, setSearch] = useState("");
  const [categoryId, setCategoryId] = useState("");

  useEffect(() => {
    const q = searchParams.get("search") ?? "";
    const c = searchParams.get("categoryId") ?? "";
    setSearch(q);
    setCategoryId(c);
  }, [searchParams]);

  function onSubmit(e) {
    e.preventDefault();
    const value = search.trim();
    const qs = new URLSearchParams();
    if (value) qs.set("search", value);
    if (categoryId) qs.set("categoryId", categoryId);
    navigate(`/products${qs.toString() ? `?${qs.toString()}` : ""}`);
  }

  return (
    <header className="sticky top-0 z-50 border-b border-slate-200 bg-white/80 backdrop-blur">
      <nav className="mx-auto flex max-w-6xl items-center justify-between gap-3 px-4 py-3">
        <div className="flex items-center gap-6">
          <NavLink
            to="/"
            className="text-lg font-extrabold tracking-tight text-violet-700"
          >
            n11
          </NavLink>
        </div>

        <div className="hidden flex-1 px-2 sm:block">
          <div className="relative">
            <form onSubmit={onSubmit}>
              <input
                type="search"
                placeholder="Ürün, kategori, marka ara"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm outline-none ring-violet-200 placeholder:text-slate-400 focus:ring-4"
              />
            </form>
            <div className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-slate-400">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                className="h-5 w-5"
              >
                <path
                  fillRule="evenodd"
                  d="M10.5 3.75a6.75 6.75 0 1 0 4.02 12.174l3.53 3.53a.75.75 0 1 0 1.06-1.06l-3.53-3.53A6.75 6.75 0 0 0 10.5 3.75Zm-5.25 6.75a5.25 5.25 0 1 1 10.5 0 5.25 5.25 0 0 1-10.5 0Z"
                  clipRule="evenodd"
                />
              </svg>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <NavLink
            to="/cart"
            className={({ isActive }) =>
              [
                "inline-flex items-center justify-center rounded-xl border px-3 py-2 text-sm font-medium transition",
                isActive
                  ? "border-slate-300 bg-slate-100 text-slate-900"
                  : "border-slate-200 bg-white text-slate-700 hover:bg-slate-50",
              ].join(" ")
            }
            aria-label="Sepet"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              className="h-5 w-5"
            >
              <path d="M2.25 3a.75.75 0 0 0 0 1.5h1.386c.17 0 .318.114.36.279l2.272 8.977a2.25 2.25 0 0 0 2.182 1.694h8.7a2.25 2.25 0 0 0 2.182-1.694l1.32-5.216a.75.75 0 0 0-.727-.934H6.31l-.776-3.066A1.875 1.875 0 0 0 3.716 3H2.25Z" />
              <path
                fillRule="evenodd"
                d="M9 20.25a.75.75 0 0 1-.75.75h-.5a.75.75 0 0 1 0-1.5h.5a.75.75 0 0 1 .75.75Zm8.25.75a.75.75 0 1 0 0-1.5h-.5a.75.75 0 0 0 0 1.5h.5Z"
                clipRule="evenodd"
              />
            </svg>
            <span className="ml-2 hidden sm:inline">Sepet</span>
          </NavLink>

          {authenticated ? (
            <button
              type="button"
              onClick={() =>
                logoutFromKeycloak({ redirectUri: window.location.origin + "/" })
              }
              className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
            >
              Çıkış
            </button>
          ) : (
            <div className="flex flex-wrap items-center justify-end gap-2">
              <button
                type="button"
                onClick={() => navigate("/register")}
                className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
              >
                Kayıt Ol
              </button>
              <button
                type="button"
                onClick={() =>
                  login({
                    redirectUri: window.location.origin + "/",
                  })
                }
                className="inline-flex items-center justify-center rounded-xl bg-slate-900 px-3 py-2 text-sm font-semibold text-white hover:bg-slate-800"
              >
                Giriş Yap
              </button>
            </div>
          )}
        </div>
      </nav>
      <CategoryBar />
    </header>
  );
}

export default Navbar;
