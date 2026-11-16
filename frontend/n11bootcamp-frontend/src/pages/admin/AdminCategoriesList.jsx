import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  adminDeleteCategory,
  adminGetCategories,
} from "../../services/adminCategoryService";

export default function AdminCategoriesList() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  async function load() {
    setLoading(true);
    setError("");
    try {
      const res = await adminGetCategories();
      setItems(Array.isArray(res?.data) ? res.data : []);
    } catch (e) {
      const msg =
        e?.response?.data?.data?.detail ??
        e?.response?.data?.message ??
        "Kategoriler yüklenemedi";
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
            Kategoriler
          </h1>
          <p className="mt-1 text-sm text-slate-600">Listele, düzenle, sil.</p>
        </div>
        <Link
          to="/admin/categories/new"
          className="inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-extrabold text-white hover:bg-slate-800"
        >
          Yeni Kategori
        </Link>
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
                  <th className="px-4 py-3">Kategori</th>
                  <th className="px-4 py-3">Slug</th>
                  <th className="px-4 py-3">Aktif</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {items.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/50">
                    <td className="px-4 py-3">
                      <div className="font-extrabold text-slate-900">{c.name}</div>
                      <div className="mt-1 font-mono text-xs text-slate-500">
                        {c.id}
                      </div>
                    </td>
                    <td className="px-4 py-3 text-slate-700">{c.slug ?? "—"}</td>
                    <td className="px-4 py-3 text-slate-700">
                      {typeof c.active === "boolean" ? (c.active ? "Evet" : "Hayır") : "—"}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-2">
                        <Link
                          to={`/admin/categories/${c.id}/edit`}
                          className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50"
                        >
                          Düzenle
                        </Link>
                        <button
                          type="button"
                          disabled={busyId === c.id}
                          onClick={async () => {
                            if (!confirm("Kategoriyi silmek istediğine emin misin?")) return;
                            setBusyId(c.id);
                            try {
                              await adminDeleteCategory(c.id);
                              await load();
                            } finally {
                              setBusyId(null);
                            }
                          }}
                          className="inline-flex items-center justify-center rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-extrabold text-rose-700 hover:bg-rose-100 disabled:opacity-50"
                        >
                          {busyId === c.id ? "..." : "Sil"}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

