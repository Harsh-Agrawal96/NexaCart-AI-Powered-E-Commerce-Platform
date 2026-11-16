import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { adminGetCartById } from "../../services/adminCartService";
import { normalizeCartPayload } from "../../services/cartService";
import { formatTry } from "../../utils/money";

export default function AdminCartDetail() {
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [cart, setCart] = useState(null);

  useEffect(() => {
    let mounted = true;
    async function run() {
      if (!id) return;
      setLoading(true);
      setError("");
      try {
        const res = await adminGetCartById(id);
        const c = normalizeCartPayload(res);
        if (mounted) setCart(c);
      } catch (e) {
        const msg =
          e?.response?.data?.data?.detail ??
          e?.response?.data?.message ??
          "Sepet yüklenemedi";
        if (mounted) setError(msg);
      } finally {
        if (mounted) setLoading(false);
      }
    }
    run();
    return () => {
      mounted = false;
    };
  }, [id]);

  const items = cart?.items ?? [];

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">Sepet detayı</h1>
          <p className="mt-1 font-mono text-sm text-slate-600">{id}</p>
        </div>
        <Link
          to="/admin/carts"
          className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
        >
          Listeye dön
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
        <>
          <div className="rounded-3xl border border-slate-200 bg-white p-5">
            <div className="grid gap-3 sm:grid-cols-3">
              <div>
                <div className="text-xs font-semibold text-slate-500">cartId</div>
                <div className="mt-1 font-mono text-sm text-slate-900">{cart?.cartId ?? "—"}</div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-500">userId</div>
                <div className="mt-1 font-mono text-sm text-slate-900">{cart?.userId ?? "—"}</div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-500">Ara toplam</div>
                <div className="mt-1 text-lg font-extrabold text-slate-900">
                  {formatTry(cart?.grandTotal)}
                </div>
              </div>
            </div>
          </div>

          <div className="overflow-hidden rounded-3xl border border-slate-200 bg-white">
            <div className="overflow-x-auto">
              <table className="min-w-full text-left text-sm">
                <thead className="bg-slate-50 text-xs font-extrabold uppercase tracking-wide text-slate-600">
                  <tr>
                    <th className="px-4 py-3">Ürün</th>
                    <th className="px-4 py-3">SKU</th>
                    <th className="px-4 py-3">Adet</th>
                    <th className="px-4 py-3">Birim</th>
                    <th className="px-4 py-3">Satır</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200">
                  {items.map((it, idx) => (
                    <tr
                      key={it.productId ? `${it.productId}-${idx}` : `row-${idx}`}
                      className="hover:bg-slate-50/50"
                    >
                      <td className="px-4 py-3 font-extrabold text-slate-900">
                        {it.productName ?? "—"}
                      </td>
                      <td className="px-4 py-3 font-mono text-xs text-slate-600">
                        {it.productSku ?? "—"}
                      </td>
                      <td className="px-4 py-3">{it.amount ?? "—"}</td>
                      <td className="px-4 py-3 font-extrabold">{formatTry(it.unitPrice)}</td>
                      <td className="px-4 py-3 font-extrabold text-violet-700">
                        {formatTry(it.totalPrice)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {!items.length ? (
              <div className="border-t border-slate-200 px-4 py-8 text-center text-sm text-slate-600">
                Satır yok.
              </div>
            ) : null}
          </div>
        </>
      )}
    </div>
  );
}
