import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { adminGetOrderById } from "../../services/adminOrderService";
import { formatTry } from "../../utils/money";

function unwrapApi(body) {
  if (body && typeof body === "object" && "success" in body && body.data !== undefined) {
    return body.data ?? null;
  }
  return body ?? null;
}

function fmtDate(v) {
  if (!v) return "—";
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);
  return d.toLocaleString("tr-TR");
}

export default function AdminOrderDetail() {
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [order, setOrder] = useState(null);

  useEffect(() => {
    let mounted = true;
    async function run() {
      if (!id) return;
      setLoading(true);
      setError("");
      try {
        const res = await adminGetOrderById(id);
        const o = unwrapApi(res);
        if (mounted) setOrder(o);
      } catch (e) {
        const msg =
          e?.response?.data?.data?.detail ??
          e?.response?.data?.message ??
          "Sipariş yüklenemedi";
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

  const items = order?.items ?? [];

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">Sipariş detayı</h1>
          <p className="mt-1 font-mono text-sm text-slate-600">{id}</p>
        </div>
        <Link
          to="/admin/orders"
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
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <div>
                <div className="text-xs font-semibold text-slate-500">Durum</div>
                <div className="mt-1 text-base font-extrabold text-slate-900">
                  {order?.status ?? "—"}
                </div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-500">userId</div>
                <div className="mt-1 font-mono text-sm text-slate-900">{order?.userId ?? "—"}</div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-500">Toplam</div>
                <div className="mt-1 text-lg font-extrabold text-slate-900">
                  {formatTry(order?.grandTotal)}
                </div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-500">Oluşturulma</div>
                <div className="mt-1 text-sm text-slate-800">{fmtDate(order?.createdAt)}</div>
              </div>
            </div>
            {order?.cancelReason ? (
              <div className="mt-4 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
                <span className="font-semibold">İptal nedeni:</span> {order.cancelReason}
              </div>
            ) : null}
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
