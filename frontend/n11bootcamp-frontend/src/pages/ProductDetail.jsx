import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getProductById } from "../services/productService";
import { formatTry } from "../utils/money";
import { addCartItem, describeCartAddSuccess } from "../services/cartService";
import { useToast } from "../context/ToastContext.jsx";
import { useKeycloakAuth } from "../auth/KeycloakProvider.jsx";
import {
  createReview,
  deleteMyReview,
  listReviewsByProduct,
  updateMyReview,
} from "../services/reviewService";
import { getKeycloakInstance } from "../auth/keycloakRuntime";
import { getUserIdFromKeycloak } from "../utils/user";
import ProductAiChatPanel from "../components/ProductAiChatPanel.jsx";
import BehaviorRecommendationsSection from "../components/BehaviorRecommendationsSection.jsx";

function fmtDate(v) {
  if (!v) return "—";
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);
  return d.toLocaleString("tr-TR");
}

function Stars({ value }) {
  const n = Math.max(0, Math.min(5, Number(value ?? 0)));
  return (
    <div className="inline-flex items-center gap-1" aria-label={`Puan: ${n}/5`}>
      {[1, 2, 3, 4, 5].map((i) => (
        <span
          key={i}
          className={i <= n ? "text-amber-400" : "text-slate-200"}
        >
          ★
        </span>
      ))}
    </div>
  );
}

function getReviewerName(r) {
  const first = String(r?.userFirstName ?? "").trim();
  const last = String(r?.userLastName ?? "").trim();
  const full = `${first} ${last}`.trim();
  return full || "Anonim";
}

export default function ProductDetail() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [adding, setAdding] = useState(false);
  const [addError, setAddError] = useState("");
  const { authenticated, login } = useKeycloakAuth();
  const { showToast } = useToast();

  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [reviewsError, setReviewsError] = useState("");
  const [reviewsPage, setReviewsPage] = useState({
    content: [],
    page: 0,
    size: 5,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  });
  const [ratingMin, setRatingMin] = useState("");
  const [ratingMax, setRatingMax] = useState("");

  const [reviewForm, setReviewForm] = useState({ rating: "5", comment: "" });
  const [reviewBusy, setReviewBusy] = useState(false);
  const [reviewMsg, setReviewMsg] = useState("");

  const myUserId = useMemo(() => {
    const kc = getKeycloakInstance();
    return getUserIdFromKeycloak(kc);
  }, [authenticated]);

  const reviews = reviewsPage?.content ?? [];
  const myReview = useMemo(() => {
    if (!myUserId) return null;
    return reviews.find((r) => String(r.userId) === String(myUserId)) ?? null;
  }, [myUserId, reviews]);

  const avgRating = useMemo(() => {
    if (!reviews.length) return null;
    const sum = reviews.reduce((a, r) => a + Number(r.rating ?? 0), 0);
    return sum / reviews.length;
  }, [reviews]);

  useEffect(() => {
    let mounted = true;

    async function run() {
      setLoading(true);
      setError(null);
      try {
        const data = await getProductById(id);
        const p = data?.data ?? data;
        if (mounted) setProduct(p);
      } catch (e) {
        if (mounted) setError(e);
      } finally {
        if (mounted) setLoading(false);
      }
    }

    if (id) run();
    return () => {
      mounted = false;
    };
  }, [id]);

  async function loadReviews(nextPage = 0) {
    if (!id) return;
    setReviewsLoading(true);
    setReviewsError("");
    try {
      const p = await listReviewsByProduct({
        productId: id,
        ratingMin: ratingMin === "" ? undefined : Number(ratingMin),
        ratingMax: ratingMax === "" ? undefined : Number(ratingMax),
        page: nextPage,
        size: reviewsPage.size,
        sort: "createdAt,desc",
      });
      setReviewsPage(
        p ?? {
          content: [],
          page: 0,
          size: reviewsPage.size,
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
        "Yorumlar yüklenemedi";
      setReviewsError(msg);
    } finally {
      setReviewsLoading(false);
    }
  }

  useEffect(() => {
    loadReviews(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <Link
              to="/products"
              className="text-xs font-semibold text-slate-500 hover:text-slate-800"
            >
              Ürünler
            </Link>
            <span className="text-xs font-semibold text-slate-400">/</span>
            <span className="text-xs font-semibold text-slate-500">
              Detay
            </span>
          </div>

          <h1 className="mt-2 text-balance text-3xl font-extrabold tracking-tight text-slate-900 sm:text-4xl">
            {loading ? "Yükleniyor..." : product?.name ?? "Ürün"}
          </h1>

          {product?.categoryName ? (
            <div className="mt-2 inline-flex rounded-full border border-slate-200 bg-white px-3 py-1 text-xs font-semibold text-slate-700">
              {product.categoryName}
            </div>
          ) : null}
        </div>

        <Link
          to="/products"
          className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
        >
          Ürünlere dön
        </Link>
      </div>

      {error ? (
        <div className="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-700">
          Ürün yüklenemedi. Backend ayakta mı?
        </div>
      ) : loading ? (
        <div className="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-700">
          Yükleniyor...
        </div>
      ) : (
        <div className="space-y-6">
          <div className="grid items-start gap-6 lg:grid-cols-12">
            <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white lg:col-span-5">
              <div className="relative flex max-h-[min(55vh,320px)] items-center justify-center overflow-hidden bg-slate-100 sm:max-h-[min(50vh,380px)]">
                {product?.imageUrl ? (
                  <img
                    src={product.imageUrl}
                    alt={product?.name ?? "Ürün"}
                    className="h-full w-full max-h-[min(55vh,320px)] object-contain sm:max-h-[min(50vh,380px)]"
                  />
                ) : (
                  <div className="flex min-h-[200px] w-full items-center justify-center">
                    <span className="text-sm font-medium text-slate-500">
                      Resim Yok
                    </span>
                  </div>
                )}
              </div>
            </div>

            <div className="space-y-5 lg:col-span-7">
              <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="text-xs font-semibold text-slate-500">
                      Fiyat
                    </div>
                    <div className="mt-1 text-3xl font-extrabold text-violet-700">
                      {formatTry(product?.price)}
                    </div>
                  </div>
                  {typeof product?.active === "boolean" ? (
                    <span
                      className={[
                        "rounded-full px-3 py-1 text-xs font-extrabold",
                        product.active
                          ? "bg-emerald-50 text-emerald-800 ring-1 ring-emerald-200"
                          : "bg-slate-100 text-slate-700 ring-1 ring-slate-200",
                      ].join(" ")}
                    >
                      {product.active ? "Stokta" : "Pasif"}
                    </span>
                  ) : null}
                </div>

                <div className="mt-4 flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                  <div className="min-w-0">
                    <div className="text-xs font-semibold text-slate-500">Yorumlar</div>
                    <div className="mt-1 flex flex-wrap items-center gap-2">
                      <Stars value={Math.round((avgRating ?? 0) * 2) / 2} />
                      <span className="text-xs font-semibold text-slate-700">
                        {avgRating != null ? avgRating.toFixed(1) : "—"}
                      </span>
                      <span className="text-xs text-slate-500">
                        ({reviewsPage?.totalElements ?? 0})
                      </span>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      const el = document.getElementById("reviews");
                      el?.scrollIntoView({ behavior: "smooth", block: "start" });
                    }}
                    className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-100"
                  >
                    Yorumları gör
                  </button>
                </div>

                <div className="mt-5 grid gap-3 sm:grid-cols-2">
                  <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                    <div className="text-xs font-semibold text-slate-500">
                      Stok
                    </div>
                    <div className="mt-1 text-lg font-extrabold text-slate-900">
                      {product?.quantity ?? "—"}
                    </div>
                  </div>
                  <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                    <div className="text-xs font-semibold text-slate-500">SKU</div>
                    <div className="mt-1 truncate font-mono text-sm font-semibold text-slate-900">
                      {product?.sku ?? "—"}
                    </div>
                  </div>
                </div>

                <div className="mt-5 rounded-2xl border border-slate-200 bg-white p-4">
                  <div className="text-xs font-semibold text-slate-500">
                    Ürün ID
                  </div>
                  <div className="mt-1 break-all font-mono text-sm text-slate-800">
                    {id}
                  </div>
                </div>

                <button
                  type="button"
                  disabled={adding}
                  onClick={async () => {
                    setAddError("");
                    if (!authenticated) {
                      login({ redirectUri: window.location.href });
                      return;
                    }
                    try {
                      setAdding(true);
                      const cart = await addCartItem({
                        productId: id,
                        amount: 1,
                      });
                      showToast(describeCartAddSuccess(cart, id));
                    } catch (e) {
                      const msg =
                        e?.response?.data?.data?.detail ??
                        e?.response?.data?.message ??
                        "Sepete eklenemedi";
                      setAddError(msg);
                    } finally {
                      setAdding(false);
                    }
                  }}
                  className="mt-6 inline-flex w-full items-center justify-center rounded-2xl bg-slate-900 px-4 py-3 text-sm font-extrabold text-white hover:bg-slate-800"
                >
                  {adding ? "Ekleniyor..." : "Sepete Ekle"}
                </button>

                {addError ? (
                  <div className="mt-3 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                    {addError}
                  </div>
                ) : null}

                <p className="mt-3 text-center text-xs text-slate-500">
                  Kampanya ve kargo bilgileri yakında eklenecek.
                </p>
              </div>
            </div>
          </div>

          <div className="rounded-3xl border border-slate-200 bg-white p-6">
            <div className="text-sm font-extrabold text-slate-900">
              Ürün Açıklaması
            </div>
            {product?.description ? (
              <p className="mt-3 whitespace-pre-wrap text-sm leading-relaxed text-slate-700">
                {product.description}
              </p>
            ) : (
              <p className="mt-3 text-sm text-slate-600">
                Bu ürün için açıklama bulunmuyor.
              </p>
            )}
          </div>

          <div
            id="detail-reviews-ai"
            className="grid gap-6 lg:grid-cols-2 lg:items-start"
          >
            <div id="reviews" className="rounded-3xl border border-slate-200 bg-white p-6">
              <div className="flex flex-wrap items-end justify-between gap-3">
                <div>
                  <div className="text-sm font-extrabold text-slate-900">Yorumlar</div>
                  <div className="mt-1 text-xs text-slate-500">
                    Filtre: puan aralığı + sayfalama
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => loadReviews(0)}
                  className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50"
                >
                  Yenile
                </button>
              </div>

              <div className="mt-4 grid gap-3 sm:grid-cols-3">
                <div className="space-y-1">
                  <div className="text-xs font-semibold text-slate-600">ratingMin</div>
                  <select
                    value={ratingMin}
                    onChange={(e) => setRatingMin(e.target.value)}
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
                <div className="space-y-1">
                  <div className="text-xs font-semibold text-slate-600">ratingMax</div>
                  <select
                    value={ratingMax}
                    onChange={(e) => setRatingMax(e.target.value)}
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
                <div className="flex items-end gap-2">
                  <button
                    type="button"
                    onClick={() => loadReviews(0)}
                    className="inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-extrabold text-white hover:bg-slate-800"
                  >
                    Uygula
                  </button>
                </div>
              </div>

              <div className="mt-6 rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="text-xs font-semibold text-slate-600">Yorum yaz</div>
                {!authenticated ? (
                  <div className="mt-2 flex flex-wrap items-center justify-between gap-2">
                    <div className="text-sm text-slate-700">
                      Yorum yazmak için giriş yapmalısın.
                    </div>
                    <button
                      type="button"
                      onClick={() => login({ redirectUri: window.location.href })}
                      className="inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-extrabold text-white hover:bg-slate-800"
                    >
                      Giriş Yap
                    </button>
                  </div>
                ) : (
                  <form
                    onSubmit={async (e) => {
                      e.preventDefault();
                      if (!id) return;
                      setReviewBusy(true);
                      setReviewMsg("");
                      setReviewsError("");
                      try {
                        const ratingNum = Number(reviewForm.rating);
                        if (myReview?.id) {
                          await updateMyReview(myReview.id, {
                            rating: ratingNum,
                            comment: reviewForm.comment,
                          });
                          setReviewMsg("Yorum güncellendi.");
                        } else {
                          await createReview({
                            productId: id,
                            rating: ratingNum,
                            comment: reviewForm.comment,
                          });
                          setReviewMsg("Yorum eklendi.");
                        }
                        setReviewForm((s) => ({ ...s, comment: "" }));
                        await loadReviews(0);
                      } catch (e2) {
                        const msg =
                          e2?.response?.data?.data?.detail ??
                          e2?.response?.data?.message ??
                          "Yorum kaydedilemedi";
                        setReviewsError(msg);
                      } finally {
                        setReviewBusy(false);
                      }
                    }}
                    className="mt-3 grid gap-3"
                  >
                    <div className="grid gap-3 sm:grid-cols-3">
                      <div className="space-y-1">
                        <div className="text-xs font-semibold text-slate-600">Puan</div>
                        <select
                          value={reviewForm.rating}
                          onChange={(e) =>
                            setReviewForm((s) => ({ ...s, rating: e.target.value }))
                          }
                          className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                        >
                          {[1, 2, 3, 4, 5].map((n) => (
                            <option key={n} value={n}>
                              {n}
                            </option>
                          ))}
                        </select>
                      </div>
                      <div className="space-y-1 sm:col-span-2">
                        <div className="text-xs font-semibold text-slate-600">Yorum</div>
                        <textarea
                          value={reviewForm.comment}
                          onChange={(e) =>
                            setReviewForm((s) => ({ ...s, comment: e.target.value }))
                          }
                          rows={3}
                          maxLength={2000}
                          className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                          placeholder="İsteğe bağlı..."
                        />
                      </div>
                    </div>
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <button
                        type="submit"
                        disabled={reviewBusy}
                        className="inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-extrabold text-white hover:bg-slate-800 disabled:opacity-50"
                      >
                        {reviewBusy ? "Kaydediliyor..." : myReview?.id ? "Güncelle" : "Gönder"}
                      </button>
                      {myReview?.id ? (
                        <button
                          type="button"
                          disabled={reviewBusy}
                          onClick={async () => {
                            if (!confirm("Yorumun silinsin mi?")) return;
                            setReviewBusy(true);
                            setReviewMsg("");
                            setReviewsError("");
                            try {
                              await deleteMyReview(myReview.id);
                              setReviewMsg("Yorum silindi.");
                              await loadReviews(0);
                            } catch (e3) {
                              const msg =
                                e3?.response?.data?.data?.detail ??
                                e3?.response?.data?.message ??
                                "Yorum silinemedi";
                              setReviewsError(msg);
                            } finally {
                              setReviewBusy(false);
                            }
                          }}
                          className="inline-flex items-center justify-center rounded-xl border border-rose-200 bg-rose-50 px-4 py-2 text-sm font-extrabold text-rose-700 hover:bg-rose-100 disabled:opacity-50"
                        >
                          Sil
                        </button>
                      ) : null}
                    </div>
                    {reviewMsg ? (
                      <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
                        {reviewMsg}
                      </div>
                    ) : null}
                  </form>
                )}
              </div>

              {reviewsLoading ? (
                <div className="mt-4 text-sm text-slate-700">Yükleniyor...</div>
              ) : reviewsError ? (
                <div className="mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                  {reviewsError}
                </div>
              ) : (
                <div className="mt-4 space-y-3">
                  {reviews.map((r) => (
                    <div key={r.id} className="rounded-2xl border border-slate-200 bg-white p-4">
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <div className="flex items-center gap-2">
                          <Stars value={r.rating} />
                          <span className="text-xs font-semibold text-slate-500">
                            {fmtDate(r.createdAt)}
                          </span>
                        </div>
                        <span className="truncate text-xs font-extrabold text-slate-800">
                          {getReviewerName(r)}
                        </span>
                      </div>
                      <div className="mt-2 whitespace-pre-wrap text-sm text-slate-800">
                        {r.comment || <span className="text-slate-500">—</span>}
                      </div>
                    </div>
                  ))}
                  {!reviews.length ? (
                    <div className="rounded-2xl border border-slate-200 bg-white p-4 text-sm text-slate-700">
                      Henüz yorum yok.
                    </div>
                  ) : null}

                  <div className="flex items-center justify-between pt-1">
                    <button
                      type="button"
                      disabled={!!reviewsPage.first}
                      onClick={() => loadReviews(Math.max(0, reviewsPage.page - 1))}
                      className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50 disabled:opacity-50"
                    >
                      Önceki
                    </button>
                    <div className="text-xs font-semibold text-slate-600">
                      {reviewsPage.page + 1} / {Math.max(1, reviewsPage.totalPages)}
                    </div>
                    <button
                      type="button"
                      disabled={!!reviewsPage.last}
                      onClick={() => loadReviews(reviewsPage.page + 1)}
                      className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs font-extrabold text-slate-800 hover:bg-slate-50 disabled:opacity-50"
                    >
                      Sonraki
                    </button>
                  </div>
                </div>
              )}
            </div>

            <div className="min-w-0">
              <BehaviorRecommendationsSection
                currentProductId={id}
                narrowColumn
              />
            </div>
          </div>
        </div>
      )}

      {id ? (
        <ProductAiChatPanel productId={id} productName={product?.name} />
      ) : null}
    </section>
  );
}

