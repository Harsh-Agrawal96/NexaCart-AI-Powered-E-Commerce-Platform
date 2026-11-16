import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { adminListCarts } from "../../services/adminCartService";

function fmtDate(v) {
  if (!v) return "—";
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);
  return d.toLocaleString("tr-TR");
}

export default function AdminCartsList() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [filters, setFilters] = useState({
    userId: "",
    status: "",
    createdFrom: "",
    createdTo: "",
  });

  const [page, setPage] = useState({
    content: [],
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });

  const rows = page?.content ?? [];

  const metaText = useMemo(() => {
    return `${page.page + 1} / ${Math.max(1, page.totalPages)} • ${page.totalElements} sepet`;
  }, [page.page, page.totalElements, page.totalPages]);

  async function load(nextPage = page.page) {
    setLoading(true);
    setError("");
    try {
      const params = {
        page: nextPage,
        size: page.size,
      };
      const uid = filters.userId.trim();
      if (uid) params.userId = uid;
      if (filters.status) params.status = filters.status;
      if (filters.createdFrom) params.createdFrom = new Date(filters.createdFrom).toISOString();
      if (filters.createdTo) params.createdTo = new Date(filters.createdTo).toISOString();

      const res = await adminListCarts(params);
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
        "Sepetler yüklenemedi";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">Sepetler</h1>
          <p className="mt-1 text-sm text-slate-600">{metaText}</p>
        </div>
      </div>

      <div className="rounded-3xl border border-slate-200 bg-white p-5">
        <div className="grid gap-3 md:grid-cols-2 lg:grid-cols-5">
          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-600">userId</div>
            <input
              value={filters.userId}
              onChange={(e) => setFilters((s) => ({ ...s, userId: e.target.value }))}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              placeholder="uuid"
            />
          </div>
          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-600">Durum</div>
            <select
              value={filters.status}
              onChange={(e) => setFilters((s) => ({ ...s, status: e.target.value }))}
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
            >
              <option value="">Hepsi</option>
              <option value="ACTIVE">ACTIVE</option>
              <option value="ORDERED">ORDERED</option>
            </select>
          </div>
          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-600">Oluşturulma (başlangıç)</div>
            <input
              type="datetime-local"
              value={filters.createdFrom}
              onChange={(e) => setFilters((s) => ({ ...s, createdFrom: e.target.value }))}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
            />
          </div>
          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-600">Oluşturulma (bitiş)</div>
            <input
              type="datetime-local"
              value={filters.createdTo}
              onChange={(e) => setFilters((s) => ({ ...s, createdTo: e.target.value }))}
              className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
            />
          </div>
          <div className="flex items-end">
            <button
              type="button"
              onClick={() => load(0)}
              className="inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-extrabold text-white hover:bg-slate-800"
            >
              Uygula
            </button>
          </div>
        </div>
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
                  <th className="px-4 py-3">Sepet</th>
                  <th className="px-4 py-3">Kullanıcı</th>
                  <th className="px-4 py-3">Durum</th>
                  <th className="px-4 py-3">Satır</th>
                  <th className="px-4 py-3">Güncelleme</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {rows.map((r) => (
                  <tr key={r.cartId} className="hover:bg-slate-50/50">
                    <td className="px-4 py-3 font-mono text-xs text-slate-700">{r.cartId}</td>
                    <td className="px-4 py-3 font-mono text-xs text-slate-700">{r.userId}</td>
                    <td className="px-4 py-3">
                      <span className="rounded-full bg-slate-100 px-2 py-1 text-xs font-extrabold text-slate-800 ring-1 ring-slate-200">
                        {r.status ?? "—"}
                      </span>
                    </td>
                    <td className="px-4 py-3 font-extrabold text-slate-900">{r.lineItemCount ?? 0}</td>
                    <td className="px-4 py-3 text-slate-600">{fmtDate(r.updatedAt)}</td>
                    <td className="px-4 py-3 text-right">
                      <Link
                        to={`/admin/carts/${r.cartId}`}
                        className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50"
                      >
                        Detay
                      </Link>
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
