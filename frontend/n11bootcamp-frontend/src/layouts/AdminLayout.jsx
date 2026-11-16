import { NavLink, Outlet } from "react-router-dom";
import { useKeycloakAuth } from "../auth/KeycloakProvider.jsx";

function SidebarLink({ to, children }) {
  return (
    <NavLink
      to={to}
      end
      className={({ isActive }) =>
        [
          "flex items-center justify-between rounded-xl px-3 py-2 text-sm font-semibold transition",
          isActive
            ? "bg-slate-900 text-white"
            : "text-slate-700 hover:bg-slate-100",
        ].join(" ")
      }
    >
      <span>{children}</span>
    </NavLink>
  );
}

export default function AdminLayout() {
  const { authenticated, login, keycloak } = useKeycloakAuth();
  const role = import.meta.env.VITE_ADMIN_REALM_ROLE ?? "ADMIN";
  const hasRole =
    typeof keycloak?.hasRealmRole === "function" ? keycloak.hasRealmRole(role) : true;

  if (!authenticated) {
    return (
      <div className="mx-auto max-w-lg rounded-3xl border border-slate-200 bg-white p-6">
        <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
          Admin Panel
        </h1>
        <p className="mt-2 text-sm text-slate-600">
          Admin paneli için giriş yapmalısın.
        </p>
        <button
          type="button"
          onClick={() => login({ redirectUri: window.location.href })}
          className="mt-5 inline-flex w-full items-center justify-center rounded-2xl bg-slate-900 px-4 py-3 text-sm font-extrabold text-white hover:bg-slate-800"
        >
          Giriş Yap
        </button>
      </div>
    );
  }

  return (
    <div className="grid gap-6 lg:grid-cols-12">
      <aside className="lg:col-span-3">
        <div className="rounded-3xl border border-slate-200 bg-white p-4">
          <div className="px-2">
            <div className="text-xs font-semibold text-slate-500">Admin</div>
            <div className="mt-1 text-base font-extrabold text-slate-900">
              Yönetim Paneli
            </div>
          </div>

          {!hasRole ? (
            <div className="mt-4 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
              Bu kullanıcıda <span className="font-mono">{role}</span> rolü görünmüyor.
              Endpoint’ler 403 dönebilir.
            </div>
          ) : null}

          <div className="mt-4 space-y-1">
            <SidebarLink to="/admin">Dashboard</SidebarLink>
            <SidebarLink to="/admin/products">Ürünler</SidebarLink>
            <SidebarLink to="/admin/categories">Kategoriler</SidebarLink>
            <SidebarLink to="/admin/users">Kullanıcılar</SidebarLink>
            <SidebarLink to="/admin/reviews">Yorumlar</SidebarLink>
            <SidebarLink to="/admin/carts">Sepetler</SidebarLink>
            <SidebarLink to="/admin/orders">Siparişler</SidebarLink>
            <SidebarLink to="/admin/logs">Loglar</SidebarLink>
            <SidebarLink to="/admin/services">Servis Durumu</SidebarLink>
          </div>
        </div>
      </aside>

      <section className="lg:col-span-9">
        <Outlet />
      </section>
    </div>
  );
}

