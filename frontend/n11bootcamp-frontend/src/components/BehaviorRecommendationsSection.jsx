import { useEffect, useMemo, useRef, useState } from "react";
import { useKeycloakAuth } from "../auth/KeycloakProvider.jsx";
import { getKeycloakInstance } from "../auth/keycloakRuntime";
import { getUserIdFromKeycloak } from "../utils/user";
import { requestBehaviorRecommendations } from "../services/aiAdviceService";
import {
  buildBehaviorRecCacheKey,
  classifyBehaviorRecResponse,
  dedupeBehaviorRecFetch,
  readBehaviorRecCache,
  writeBehaviorRecCache,
} from "../utils/behaviorRecommendationsCache";
import ProductCard from "./ProductCard";

/** ai-service {@code BehaviorRecommendationClientCacheConfig.CLIENT_RESPONSE_TTL_SECONDS} ile aynı olmalı */
const CLIENT_RESPONSE_TTL_SECONDS = Number(
  import.meta.env.VITE_BEHAVIOR_REC_CLIENT_TTL_SECONDS ?? 20,
);

/** HTTP / sunucu hatası: tekrar isteği uzun süre kes (sayfa geziniminde spam önleme) */
const ERROR_CACHE_SECONDS = Number(
  import.meta.env.VITE_BEHAVIOR_REC_ERROR_CACHE_SECONDS ?? 300,
);

/** 200 ama boş öneri: orta süre önbellek */
const EMPTY_CACHE_SECONDS = Number(
  import.meta.env.VITE_BEHAVIOR_REC_EMPTY_CACHE_SECONDS ?? 120,
);

function formatAgoTr(lastMs, nowMs) {
  const sec = Math.max(0, Math.floor((nowMs - lastMs) / 1000));
  if (sec < 60) return `${sec} sn önce`;
  if (sec < 3600) return `${Math.floor(sec / 60)} dk önce`;
  return `${Math.floor(sec / 3600)} sa önce`;
}

/**
 * Ana sayfa / ürün detay / ürün listesi: davranış + AI ile ürün önerisi.
 * @param {{ currentProductId?: string, limit?: number, maxCards?: number, maxItems?: number, narrowColumn?: boolean, layout?: 'grid' | 'slider' }} props
 */
export default function BehaviorRecommendationsSection({
  currentProductId,
  limit = 15,
  maxCards = 2,
  maxItems = 20,
  narrowColumn = false,
  layout = "grid",
}) {
  const { authenticated } = useKeycloakAuth();
  const userId = useMemo(
    () => (authenticated ? getUserIdFromKeycloak(getKeycloakInstance()) : null),
    [authenticated],
  );

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [data, setData] = useState(null);
  const [lastUpdatedAt, setLastUpdatedAt] = useState(null);
  const [nowTick, setNowTick] = useState(() => Date.now());
  const scrollerRef = useRef(null);

  useEffect(() => {
    if (lastUpdatedAt == null) return;
    const id = setInterval(() => setNowTick(Date.now()), 1000);
    return () => clearInterval(id);
  }, [lastUpdatedAt]);

  useEffect(() => {
    if (!authenticated || !userId) return;

    let mounted = true;

    const cacheKey = buildBehaviorRecCacheKey({
      userId,
      currentProductId: currentProductId || undefined,
      limit,
    });
    const successTtlMs = CLIENT_RESPONSE_TTL_SECONDS * 1000;
    const errorTtlMs = ERROR_CACHE_SECONDS * 1000;
    const emptyTtlMs = EMPTY_CACHE_SECONDS * 1000;

    const cached = readBehaviorRecCache(cacheKey, {
      successTtlMs,
      errorTtlMs,
      emptyTtlMs,
    });

    if (cached) {
      if (cached.kind === "success" && cached.data != null) {
        setData(cached.data);
        setError("");
        setLastUpdatedAt(cached.savedAt);
        setLoading(false);
        return () => {
          mounted = false;
        };
      }
      if (cached.kind === "empty") {
        setData(cached.data ?? null);
        setError("");
        setLastUpdatedAt(cached.savedAt);
        setLoading(false);
        return () => {
          mounted = false;
        };
      }
      if (cached.kind === "error" && cached.message) {
        setError(cached.message);
        setData(null);
        setLastUpdatedAt(null);
        setLoading(false);
        return () => {
          mounted = false;
        };
      }
    }

    async function run() {
      setLoading(true);
      setError("");
      try {
        const res = await dedupeBehaviorRecFetch(cacheKey, () =>
          requestBehaviorRecommendations({
            userId,
            currentProductId: currentProductId || undefined,
            limit,
          }),
        );
        if (!mounted) return;

        const kind = classifyBehaviorRecResponse(res);
        if (kind === "success") {
          writeBehaviorRecCache(cacheKey, { kind: "success", data: res });
          setData(res);
          setLastUpdatedAt(Date.now());
        } else {
          writeBehaviorRecCache(cacheKey, { kind: "empty", data: res });
          setData(res);
          setLastUpdatedAt(Date.now());
        }
      } catch (e) {
        const msg =
          e?.response?.data?.data?.detail ??
          e?.response?.data?.message ??
          e?.message ??
          "Öneriler yüklenemedi.";
        if (mounted) {
          writeBehaviorRecCache(cacheKey, { kind: "error", message: String(msg) });
          setError(msg);
          setData(null);
          setLastUpdatedAt(null);
        }
      } finally {
        if (mounted) setLoading(false);
      }
    }

    run();
    return () => {
      mounted = false;
    };
  }, [authenticated, userId, currentProductId, limit]);

  if (!authenticated || !userId) return null;

  const displayCap = layout === "slider" ? maxItems : maxCards;
  const products = Array.isArray(data?.recommendedProducts)
    ? data.recommendedProducts.slice(0, displayCap)
    : [];
  const summary = typeof data?.summary === "string" ? data.summary : "";
  const conf =
    typeof data?.confidence === "number" && !Number.isNaN(data.confidence)
      ? data.confidence
      : null;

  return (
    <section
      className="rounded-2xl border border-violet-200 bg-gradient-to-br from-white to-violet-50/80 p-5 shadow-sm sm:p-6"
      aria-labelledby="personal-rec-heading"
    >
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2
            id="personal-rec-heading"
            className="text-lg font-extrabold tracking-tight text-slate-900"
          >
            Sana özel ürün tavsiyeleri
          </h2>
          <p className="mt-1 text-xs text-slate-600">
            İlgi ve gezinti geçmişine göre seçilmiş ürünler.
          </p>
        </div>
        {conf != null ? (
          <div className="rounded-full bg-white/80 px-3 py-1 text-[11px] font-semibold text-slate-600 ring-1 ring-violet-200">
            Güven: <span className="text-slate-900">{conf.toFixed(2)}</span>
          </div>
        ) : null}
      </div>

      {lastUpdatedAt != null ? (
        <p className="mt-2 border-b border-violet-100/80 pb-3 text-xs text-slate-500">
          <span className="font-medium text-slate-600">
            Son güncelleme: {formatAgoTr(lastUpdatedAt, nowTick)}
          </span>
          <span className="text-slate-400">
            {" "}
            · Önbellek: öneri ~{CLIENT_RESPONSE_TTL_SECONDS} sn
            {ERROR_CACHE_SECONDS >= 60
              ? ` · hata ~${Math.round(ERROR_CACHE_SECONDS / 60)} dk`
              : ` · hata ~${ERROR_CACHE_SECONDS} sn`}
          </span>
        </p>
      ) : null}

      {loading ? (
        <div className="mt-4 text-sm font-medium text-slate-600">Yükleniyor…</div>
      ) : null}

      {error ? (
        <div className="mt-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
          {error}
        </div>
      ) : null}

      {!loading && !error && summary ? (
        <p className="mt-4 text-sm leading-relaxed text-slate-700">{summary}</p>
      ) : null}

      {!loading && !error && products.length && layout === "slider" ? (
        <div className="relative mt-4">
          <button
            type="button"
            onClick={() =>
              scrollerRef.current?.scrollBy({
                left: -Math.min(480, scrollerRef.current.clientWidth * 0.85),
                behavior: "smooth",
              })
            }
            className="pointer-events-auto absolute left-0 top-1/2 z-10 hidden -translate-y-1/2 rounded-full border border-slate-200 bg-white/95 p-2 shadow-sm hover:bg-white md:inline-flex"
            aria-label="Önceki öneriler"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              className="h-4 w-4 text-slate-700"
            >
              <path
                fillRule="evenodd"
                d="M15.53 4.22a.75.75 0 0 1 0 1.06L9.81 11l5.72 5.72a.75.75 0 1 1-1.06 1.06l-6.25-6.25a.75.75 0 0 1 0-1.06l6.25-6.25a.75.75 0 0 1 1.06 0Z"
                clipRule="evenodd"
              />
            </svg>
          </button>
          <button
            type="button"
            onClick={() =>
              scrollerRef.current?.scrollBy({
                left: Math.min(480, scrollerRef.current.clientWidth * 0.85),
                behavior: "smooth",
              })
            }
            className="pointer-events-auto absolute right-0 top-1/2 z-10 hidden -translate-y-1/2 rounded-full border border-slate-200 bg-white/95 p-2 shadow-sm hover:bg-white md:inline-flex"
            aria-label="Sonraki öneriler"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              className="h-4 w-4 text-slate-700"
            >
              <path
                fillRule="evenodd"
                d="M8.47 19.78a.75.75 0 0 1 0-1.06L14.19 13 8.47 7.28a.75.75 0 1 1 1.06-1.06l6.25 6.25a.75.75 0 0 1 0 1.06l-6.25 6.25a.75.75 0 0 1-1.06 0Z"
                clipRule="evenodd"
              />
            </svg>
          </button>

          <div
            ref={scrollerRef}
            className="flex snap-x snap-mandatory gap-4 overflow-x-auto scroll-smooth px-1 pb-2 [-ms-overflow-style:none] [scrollbar-width:none] md:px-10 [&::-webkit-scrollbar]:hidden"
          >
            {products.map((p) => (
              <div
                key={p.id}
                className="w-[min(46vw,220px)] shrink-0 snap-start sm:w-[220px]"
              >
                <ProductCard product={p} />
              </div>
            ))}
          </div>
        </div>
      ) : null}

      {!loading && !error && products.length && layout !== "slider" ? (
        <div
          className={
            narrowColumn
              ? "mt-5 grid grid-cols-1 gap-4"
              : "mt-5 grid gap-4 sm:grid-cols-2"
          }
        >
          {products.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      ) : null}

      {!loading && !error && !products.length && !summary ? (
        <p className="mt-4 text-sm text-slate-600">
          Şimdilik öneri üretilemedi; daha fazla gezinti sonrası tekrar dene.
        </p>
      ) : null}
    </section>
  );
}
