import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  adminDeleteReview,
  adminGetReviewById,
  adminUpdateReview,
} from "../../services/adminReviewService";

function fmtDate(v) {
  if (!v) return "—";
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);
  return d.toLocaleString("tr-TR");
}

function getReviewerName(r) {
  const first = String(r?.userFirstName ?? "").trim();
  const last = String(r?.userLastName ?? "").trim();
  const full = `${first} ${last}`.trim();
  return full || "Anonim";
}

export default function AdminReviewDetail() {
  const { id } = useParams();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [review, setReview] = useState(null);

  const [form, setForm] = useState({ rating: "", comment: "" });

  const payload = useMemo(() => {
    return {
      rating: form.rating === "" ? null : Number(form.rating),
      comment: form.comment,
    };
  }, [form.comment, form.rating]);

  useEffect(() => {
    let mounted = true;
    async function run() {
      if (!id) return;
      setLoading(true);
      setError("");
      try {
        const r = await adminGetReviewById(id);
        if (!mounted) return;
        setReview(r);
        setForm({
          rating: r?.rating ?? "",
          comment: r?.comment ?? "",
        });
      } catch (e) {
        const msg =
          e?.response?.data?.data?.detail ??
          e?.response?.data?.message ??
          "Yorum yüklenemedi";
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

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
            Yorum detayı
          </h1>
          <p className="mt-1 font-mono text-sm text-slate-600">{id}</p>
        </div>
        <Link
          to="/admin/reviews"
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
        <div className="space-y-4">
          <div className="rounded-3xl border border-slate-200 bg-white p-5">
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
              <div>
                <div className="text-xs font-semibold text-slate-500">productId</div>
                <div className="mt-1 break-all font-mono text-xs text-slate-700">
                  {review?.productId ?? "—"}
                </div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-500">Kullanıcı</div>
                <div className="mt-1 text-sm font-extrabold text-slate-900">
                  {getReviewerName(review)}
                </div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-500">createdAt</div>
                <div className="mt-1 text-sm text-slate-800">
                  {fmtDate(review?.createdAt)}
                </div>
              </div>
              <div>
                <div className="text-xs font-semibold text-slate-500">updatedAt</div>
                <div className="mt-1 text-sm text-slate-800">
                  {fmtDate(review?.updatedAt)}
                </div>
              </div>
            </div>

            <details className="mt-4 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
              <summary className="cursor-pointer select-none text-xs font-extrabold text-slate-700">
                Teknik detaylar
              </summary>
              <div className="mt-3 grid gap-2 sm:grid-cols-2">
                <div>
                  <div className="text-[11px] font-semibold text-slate-500">userId</div>
                  <div className="mt-1 break-all font-mono text-xs text-slate-700">
                    {review?.userId ?? "—"}
                  </div>
                </div>
                <div>
                  <div className="text-[11px] font-semibold text-slate-500">reviewId</div>
                  <div className="mt-1 break-all font-mono text-xs text-slate-700">
                    {review?.id ?? "—"}
                  </div>
                </div>
              </div>
            </details>
          </div>

          <form
            onSubmit={async (e) => {
              e.preventDefault();
              if (!id) return;
              setSaving(true);
              setError("");
              try {
                const next = await adminUpdateReview(id, payload);
                setReview(next);
              } catch (e2) {
                const msg =
                  e2?.response?.data?.data?.detail ??
                  e2?.response?.data?.message ??
                  "Güncellenemedi";
                setError(msg);
              } finally {
                setSaving(false);
              }
            }}
            className="rounded-3xl border border-slate-200 bg-white p-6"
          >
            <div className="grid gap-4 lg:grid-cols-2">
              <div className="space-y-1">
                <div className="text-xs font-semibold text-slate-600">Rating</div>
                <select
                  value={form.rating}
                  onChange={(e) => setForm((s) => ({ ...s, rating: e.target.value }))}
                  className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                >
                  <option value="">—</option>
                  {[1, 2, 3, 4, 5].map((n) => (
                    <option key={n} value={n}>
                      {n}
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-1 lg:col-span-2">
                <div className="text-xs font-semibold text-slate-600">Yorum</div>
                <textarea
                  value={form.comment}
                  onChange={(e) => setForm((s) => ({ ...s, comment: e.target.value }))}
                  rows={5}
                  maxLength={2000}
                  className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                />
              </div>
            </div>

            {error ? (
              <div className="mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                {error}
              </div>
            ) : null}

            <div className="mt-6 flex flex-wrap items-center justify-between gap-2">
              <button
                type="submit"
                disabled={saving}
                className="inline-flex items-center justify-center rounded-2xl bg-slate-900 px-5 py-3 text-sm font-extrabold text-white hover:bg-slate-800 disabled:opacity-50"
              >
                {saving ? "Kaydediliyor..." : "Kaydet"}
              </button>

              <button
                type="button"
                onClick={async () => {
                  if (!id) return;
                  if (!confirm("Yorum silinsin mi?")) return;
                  setSaving(true);
                  setError("");
                  try {
                    await adminDeleteReview(id);
                    window.location.href = "/admin/reviews";
                  } catch (e3) {
                    const msg =
                      e3?.response?.data?.data?.detail ??
                      e3?.response?.data?.message ??
                      "Silinemedi";
                    setError(msg);
                  } finally {
                    setSaving(false);
                  }
                }}
                className="inline-flex items-center justify-center rounded-2xl border border-rose-200 bg-rose-50 px-5 py-3 text-sm font-extrabold text-rose-700 hover:bg-rose-100 disabled:opacity-50"
              >
                Sil
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}

