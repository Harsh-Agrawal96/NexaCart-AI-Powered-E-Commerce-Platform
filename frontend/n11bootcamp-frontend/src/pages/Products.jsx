import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import ProductCard from "../components/ProductCard";
import BehaviorRecommendationsSection from "../components/BehaviorRecommendationsSection.jsx";
import { getProducts } from "../services/productService";
import { useDebouncedValue } from "../hooks/useDebouncedValue";
import { parseMoney } from "../utils/money";

export default function Products() {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [localSearch, setLocalSearch] = useState("");
  const [localMinPrice, setLocalMinPrice] = useState("");
  const [localMaxPrice, setLocalMaxPrice] = useState("");
  const debouncedSearch = useDebouncedValue(localSearch, 450);

  const filters = useMemo(() => {
    const categoryId = searchParams.get("categoryId") ?? "";
    const search = searchParams.get("search") ?? "";
    const minPrice = searchParams.get("minPrice") ?? "";
    const maxPrice = searchParams.get("maxPrice") ?? "";
    const sort = searchParams.get("sort") ?? "createdAt,desc";
    const pageNumber = Number(searchParams.get("page") ?? "0");

    return {
      categoryId,
      search,
      minPrice,
      maxPrice,
      sort,
      pageNumber: Number.isFinite(pageNumber) ? pageNumber : 0,
    };
  }, [searchParams]);

  useEffect(() => {
    setLocalSearch(filters.search);
    setLocalMinPrice(filters.minPrice);
    setLocalMaxPrice(filters.maxPrice);
  }, [filters.maxPrice, filters.minPrice, filters.search]);

  useEffect(() => {
    let mounted = true;

    async function run() {
      setLoading(true);
      setError(null);
      try {
        const params = {
          ...(filters.categoryId ? { categoryId: filters.categoryId } : {}),
          ...(filters.search ? { search: filters.search } : {}),
          ...(filters.minPrice !== "" ? { minPrice: filters.minPrice } : {}),
          ...(filters.maxPrice !== "" ? { maxPrice: filters.maxPrice } : {}),
          page: filters.pageNumber,
          size: Number(import.meta.env.VITE_PRODUCT_PAGE_SIZE ?? 15),
          sort: filters.sort,
        };

        const res = await getProducts(params);
        const p = res?.data; // ApiResponse<T>: data alanı Page

        if (!p) {
          if (mounted) {
            setItems([]);
            setPage(null);
          }
          return;
        }

        const list = Array.isArray(p.content) ? p.content : [];
        if (mounted) {
          setItems(list);
          setPage(p);
        }
      } catch (e) {
        if (mounted) setError(e);
      } finally {
        if (mounted) setLoading(false);
      }
    }

    run();
    return () => {
      mounted = false;
    };
  }, [filters]);

  const message = useMemo(() => {
    if (loading) return "Yükleniyor...";
    if (error) return "Bir hata oluştu.";
    if (!items.length) return "Sonuç bulunamadı.";
    return null;
  }, [error, items.length, loading]);

  function updateParam(key, value) {
    const next = new URLSearchParams(searchParams);
    if (!value) next.delete(key);
    else next.set(key, value);
    next.set("page", "0");
    setSearchParams(next);
  }

  const hasAnyFilter =
    !!filters.categoryId ||
    !!filters.search ||
    !!filters.minPrice ||
    !!filters.maxPrice ||
    filters.sort !== "createdAt,desc";

  useEffect(() => {
    const normalized = debouncedSearch.trim();
    if (normalized === filters.search) return;
    updateParam("search", normalized);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedSearch]);

  function applyPrice() {
    const next = new URLSearchParams(searchParams);

    const minRaw = String(localMinPrice).trim();
    const maxRaw = String(localMaxPrice).trim();

    const min = parseMoney(minRaw);
    const max = parseMoney(maxRaw);

    if (!minRaw) next.delete("minPrice");
    else if (min === null) return;
    else next.set("minPrice", String(min));

    if (!maxRaw) next.delete("maxPrice");
    else if (max === null) return;
    else next.set("maxPrice", String(max));

    if (min !== null && max !== null && min > max) return;

    next.set("page", "0");
    setSearchParams(next);
  }

  function clearAll() {
    setSearchParams(new URLSearchParams());
  }

  function goToPage(nextPage) {
    const next = new URLSearchParams(searchParams);
    next.set("page", String(nextPage));
    setSearchParams(next);
  }

  const pageNumbers = useMemo(() => {
    if (!page) return [];
    const total = page.totalPages ?? 0;
    const current = page.number ?? 0;
    const start = Math.max(0, current - 2);
    const end = Math.min(total - 1, current + 2);
    const nums = [];
    for (let i = start; i <= end; i += 1) nums.push(i);
    return nums;
  }, [page]);

  return (
    <section className="space-y-6">
      <BehaviorRecommendationsSection
        layout="slider"
        limit={20}
        maxItems={20}
      />

      <div className="flex items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
            Ürünler
          </h1>
        </div>
        <div className="text-sm text-slate-600">
          {loading ? "..." : `${page?.totalElements ?? items.length} ürün`}
        </div>
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white p-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="text-xs font-semibold text-slate-500">Filtreler</div>
          {hasAnyFilter ? (
            <button
              type="button"
              onClick={clearAll}
              className="text-xs font-semibold text-slate-600 hover:text-slate-900"
            >
              Temizle
            </button>
          ) : null}
        </div>

        <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-500">Sıralama</div>
            <select
              value={filters.sort}
              onChange={(e) => updateParam("sort", e.target.value)}
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
            >
              <option value="createdAt,desc">Önerilen</option>
              <option value="price,asc">En ucuz</option>
              <option value="price,desc">En pahalı</option>
            </select>
          </div>

          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-500">Arama</div>
            <input
              value={localSearch}
              onChange={(e) => setLocalSearch(e.target.value)}
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              placeholder="Samsung, iPhone..."
            />
          </div>

          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-500">Min fiyat</div>
            <input
              inputMode="decimal"
              value={localMinPrice}
              onChange={(e) =>
                setLocalMinPrice(e.target.value.replace(/[^\d.,]/g, ""))
              }
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              placeholder="0"
            />
          </div>

          <div className="space-y-1">
            <div className="text-xs font-semibold text-slate-500">Max fiyat</div>
            <input
              inputMode="decimal"
              value={localMaxPrice}
              onChange={(e) =>
                setLocalMaxPrice(e.target.value.replace(/[^\d.,]/g, ""))
              }
              className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              placeholder="99999"
            />
          </div>

          <div className="flex items-end">
            <button
              type="button"
              onClick={applyPrice}
              className="inline-flex w-full items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800"
            >
              Fiyatı uygula
            </button>
          </div>
        </div>
      </div>

      {message ? (
        <div className="rounded-2xl border border-slate-200 bg-white p-6 text-sm text-slate-700">
          {message}
        </div>
      ) : (
        <div className="space-y-5">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {items.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>

          {page ? (
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="text-sm text-slate-600">
                Sayfa {page.number + 1} / {page.totalPages}
              </div>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => goToPage(0)}
                  disabled={page.first}
                  className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  İlk
                </button>
                <button
                  type="button"
                  onClick={() => goToPage(Math.max(0, page.number - 1))}
                  disabled={page.first}
                  className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Önceki
                </button>
                <div className="hidden items-center gap-1 sm:flex">
                  {pageNumbers.map((n) => (
                    <button
                      key={n}
                      type="button"
                      onClick={() => goToPage(n)}
                      className={[
                        "inline-flex h-10 w-10 items-center justify-center rounded-xl border text-sm font-semibold",
                        n === page.number
                          ? "border-slate-300 bg-slate-100 text-slate-900"
                          : "border-slate-200 bg-white text-slate-800 hover:bg-slate-50",
                      ].join(" ")}
                    >
                      {n + 1}
                    </button>
                  ))}
                </div>
                <button
                  type="button"
                  onClick={() => goToPage(Math.min(page.totalPages - 1, page.number + 1))}
                  disabled={page.last}
                  className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Sonraki
                </button>
                <button
                  type="button"
                  onClick={() => goToPage(page.totalPages - 1)}
                  disabled={page.last}
                  className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                >
                  Son
                </button>
              </div>
            </div>
          ) : null}
        </div>
      )}
    </section>
  );
}

