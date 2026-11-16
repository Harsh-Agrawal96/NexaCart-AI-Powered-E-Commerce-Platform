import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { adminDeleteProduct, adminGetProducts } from "../../services/adminProductService";
import { formatTry } from "../../utils/money";

export default function AdminProductsList() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  async function load() {
    setLoading(true);
    setError("");
    try {
      const res = await adminGetProducts({ page: 0, size: 50, sort: "createdAt,desc" });
      const page = res?.data;
      setItems(Array.isArray(page?.content) ? page.content : []);
    } catch (e) {
      const msg =
        e?.response?.data?.data?.detail ??
        e?.response?.data?.message ??
        "Ürünler yüklenemedi";
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
            Ürünler
          </h1>
          <p className="mt-1 text-sm text-slate-600">
            Listele, düzenle, sil.
          </p>
        </div>
        <Link
          to="/admin/products/new"
          className="inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-extrabold text-white hover:bg-slate-800"
        >
          Yeni Ürün
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
                  <th className="px-4 py-3">Ürün</th>
                  <th className="px-4 py-3">Kategori</th>
                  <th className="px-4 py-3">Fiyat</th>
                  <th className="px-4 py-3">Stok</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200">
                {items.map((p) => (
                  <tr key={p.id} className="hover:bg-slate-50/50">
                    <td className="px-4 py-3">
                      <div className="font-extrabold text-slate-900">{p.name}</div>
                      <div className="mt-1 font-mono text-xs text-slate-500">
                        {p.sku ?? p.id}
                      </div>
                    </td>
                    <td className="px-4 py-3 text-slate-700">
                      {p.categoryName ?? "—"}
                    </td>
                    <td className="px-4 py-3 font-extrabold text-violet-700">
                      {formatTry(p.price)}
                    </td>
                    <td className="px-4 py-3 text-slate-700">{p.quantity ?? "—"}</td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-2">
                        <Link
                          to={`/admin/products/${p.id}/edit`}
                          className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50"
                        >
                          Düzenle
                        </Link>
                        <button
                          type="button"
                          disabled={busyId === p.id}
                          onClick={async () => {
                            if (!confirm("Ürünü silmek istediğine emin misin?")) return;
                            setBusyId(p.id);
                            try {
                              await adminDeleteProduct(p.id);
                              await load();
                            } finally {
                              setBusyId(null);
                            }
                          }}
                          className="inline-flex items-center justify-center rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-extrabold text-rose-700 hover:bg-rose-100 disabled:opacity-50"
                        >
                          {busyId === p.id ? "..." : "Sil"}
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

