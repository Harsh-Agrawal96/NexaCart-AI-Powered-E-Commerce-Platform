import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  adminDeleteUser,
  adminGetDeletedUser,
  adminListUsers,
  adminRestoreUser,
} from "../../services/adminUserService";

function Pill({ ok, children }) {
  return (
    <span
      className={[
        "inline-flex items-center rounded-full px-3 py-1 text-xs font-extrabold ring-1",
        ok
          ? "bg-emerald-50 text-emerald-800 ring-emerald-200"
          : "bg-slate-100 text-slate-700 ring-slate-200",
      ].join(" ")}
    >
      {children}
    </span>
  );
}

export default function AdminUsersList() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  const [page, setPage] = useState({
    content: [],
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  // Deleted user tools
  const [deletedId, setDeletedId] = useState("");
  const [deletedUser, setDeletedUser] = useState(null);
  const [deletedError, setDeletedError] = useState("");

  const users = page?.content ?? [];

  const metaText = useMemo(() => {
    return `${page.page + 1} / ${Math.max(1, page.totalPages)} • ${page.totalElements} kullanıcı`;
  }, [page.page, page.totalElements, page.totalPages]);

  async function load(nextPage = page.page) {
    setLoading(true);
    setError("");
    try {
      const res = await adminListUsers({ page: nextPage, size: page.size });
      const p = res?.data;
      setPage(
        p ?? {
          content: [],
          page: 0,
          size: page.size,
          totalElements: 0,
          totalPages: 0,
          first: true,
          last: true,
        },
      );
    } catch (e) {
      const msg =
        e?.response?.data?.data?.detail ??
        e?.response?.data?.message ??
        "Kullanıcılar yüklenemedi";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function fetchDeleted() {
    setDeletedUser(null);
    setDeletedError("");
    const id = deletedId.trim();
    if (!id) return;
    try {
      const res = await adminGetDeletedUser(id);
      setDeletedUser(res?.data ?? null);
    } catch (e) {
      const msg =
        e?.response?.data?.data?.detail ??
        e?.response?.data?.message ??
        "Silinmiş kullanıcı bulunamadı";
      setDeletedError(msg);
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
            Kullanıcılar
          </h1>
          <p className="mt-1 text-sm text-slate-600">{metaText}</p>
        </div>
        <Link
          to="/admin/users/new"
          className="inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-extrabold text-white hover:bg-slate-800"
        >
          Yeni Kullanıcı
        </Link>
      </div>

      <div className="rounded-3xl border border-slate-200 bg-white p-5">
        <div className="text-sm font-extrabold text-slate-900">
          Silinmiş kullanıcıyı bul / restore
        </div>
        <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-end">
          <div className="flex-1 space-y-1">
            <div className="text-xs font-semibold text-slate-600">User ID</div>
            <input
              value={deletedId}
              onChange={(e) => setDeletedId(e.target.value)}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              placeholder="uuid..."
            />
          </div>
          <button
            type="button"
            onClick={fetchDeleted}
            className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-extrabold text-slate-800 hover:bg-slate-50"
          >
            Getir
          </button>
          <button
            type="button"
            disabled={!deletedId.trim()}
            onClick={async () => {
              const id = deletedId.trim();
              if (!id) return;
              setBusyId(id);
              try {
                await adminRestoreUser(id);
                await load(page.page);
                await fetchDeleted();
              } finally {
                setBusyId(null);
              }
            }}
            className="inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-extrabold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {busyId === deletedId.trim() ? "..." : "Restore"}
          </button>
        </div>

        {deletedError ? (
          <div className="mt-3 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
            {deletedError}
          </div>
        ) : null}

        {deletedUser ? (
          <div className="mt-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-800">
            <div className="font-extrabold">
              {deletedUser.firstName} {deletedUser.lastName}
            </div>
            <div className="mt-1 font-mono text-xs text-slate-600">
              {deletedUser.id}
            </div>
          </div>
        ) : null}
      </div>

      {loading ? (
        <div className="rounded-3xl border border-slate-200 bg-white p-6 text-sm text-slate-700">
          Yükleniyor...
        </div>
      ) : error ? (
        <div className="rounded-3xl border border-rose-200 bg-rose-50 p-6 text-sm text-rose-700">
          {error}
        </div>
      ) : (
        <div className="overflow-hidden rounded-3xl border border-slate-200 bg-white">
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-slate-50 text-xs font-extrabold uppercase tracking-wide text-slate-600">
                <tr>
                  <th className="px-4 py-3">Kullanıcı</th>
                  <th className="px-4 py-3">E-posta</th>
                  <th className="px-4 py-3">Telefon</th>
                  <th className="px-4 py-3">Aktif</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {users.map((u) => (
                  <tr key={u.id} className="hover:bg-slate-50/50">
                    <td className="px-4 py-3">
                      <div className="font-extrabold text-slate-900">
                        {u.firstName} {u.lastName}
                      </div>
                      <div className="mt-1 font-mono text-xs text-slate-500">
                        {u.keycloakId ?? u.id}
                      </div>
                    </td>
                    <td className="px-4 py-3 text-slate-700">{u.email ?? "—"}</td>
                    <td className="px-4 py-3 text-slate-700">
                      {u.phoneNumber ?? "—"}
                    </td>
                    <td className="px-4 py-3">
                      <Pill ok={!!u.active}>{u.active ? "Evet" : "Hayır"}</Pill>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-2">
                        <Link
                          to={`/admin/users/${u.id}/edit`}
                          className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50"
                        >
                          Düzenle
                        </Link>
                        <button
                          type="button"
                          disabled={busyId === u.id}
                          onClick={async () => {
                            if (!confirm("Kullanıcı silinsin mi?")) return;
                            setBusyId(u.id);
                            try {
                              await adminDeleteUser(u.id);
                              await load(page.page);
                            } finally {
                              setBusyId(null);
                            }
                          }}
                          className="inline-flex items-center justify-center rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-extrabold text-rose-700 hover:bg-rose-100 disabled:opacity-50"
                        >
                          {busyId === u.id ? "..." : "Sil"}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-between gap-2 border-t border-slate-200 bg-white px-4 py-3">
            <button
              type="button"
              disabled={page.first}
              onClick={() => load(Math.max(0, page.page - 1))}
              className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50 disabled:opacity-50"
            >
              Önceki
            </button>
            <div className="text-xs font-semibold text-slate-600">{metaText}</div>
            <button
              type="button"
              disabled={page.last}
              onClick={() => load(page.page + 1)}
              className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50 disabled:opacity-50"
            >
              Sonraki
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

