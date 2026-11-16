import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { getActiveCategories } from "../services/categoryService";

export default function CategoryBar() {
  const [searchParams] = useSearchParams();
  const activeId = searchParams.get("categoryId") ?? "";
  const search = searchParams.get("search") ?? "";
  const [items, setItems] = useState([]);
  const scrollerRef = useRef(null);

  useEffect(() => {
    let mounted = true;
    async function run() {
      try {
        const res = await getActiveCategories();
        const list = Array.isArray(res?.data) ? res.data : [];
        const sorted = [...list].sort((a, b) => {
          const ao = Number(a?.displayOrder ?? 0);
          const bo = Number(b?.displayOrder ?? 0);
          if (ao !== bo) return ao - bo;
          return String(a?.name ?? "").localeCompare(String(b?.name ?? ""), "tr");
        });
        if (mounted) setItems(sorted);
      } catch {
        if (mounted) setItems([]);
      }
    }
    run();
    return () => {
      mounted = false;
    };
  }, []);

  const baseQuery = useMemo(() => {
    const qs = new URLSearchParams();
    if (search) qs.set("search", search);
    return qs;
  }, [search]);

  function hrefFor(categoryId) {
    const qs = new URLSearchParams(baseQuery);
    if (categoryId) qs.set("categoryId", categoryId);
    const q = qs.toString();
    return `/products${q ? `?${q}` : ""}`;
  }

  function scrollByDir(dir) {
    const el = scrollerRef.current;
    if (!el) return;
    const amount = Math.max(240, Math.round(el.clientWidth * 0.6));
    el.scrollBy({ left: dir * amount, behavior: "smooth" });
  }

  return (
    <div className="border-t border-slate-200 bg-white">
      <div className="mx-auto max-w-6xl px-4">
        <div className="relative py-3">
          <button
            type="button"
            onClick={() => scrollByDir(-1)}
            className="pointer-events-auto absolute left-2 top-1/2 z-10 hidden -translate-y-1/2 rounded-full border border-slate-200 bg-white/95 p-2 shadow-sm backdrop-blur hover:bg-white sm:inline-flex"
            aria-label="Sola kaydır"
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
            onClick={() => scrollByDir(1)}
            className="pointer-events-auto absolute right-2 top-1/2 z-10 hidden -translate-y-1/2 rounded-full border border-slate-200 bg-white/95 p-2 shadow-sm backdrop-blur hover:bg-white sm:inline-flex"
            aria-label="Sağa kaydır"
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
            className="flex gap-3 overflow-x-auto scroll-smooth px-12 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden sm:px-14"
          >
          <Link
            to={hrefFor("")}
            className={[
              "shrink-0 rounded-full border px-3 py-1.5 text-xs font-semibold",
              activeId
                ? "border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
                : "border-slate-300 bg-slate-100 text-slate-900",
            ].join(" ")}
          >
            Tümü
          </Link>

          {items.map((c) => (
            <Link
              key={c.id}
              to={hrefFor(c.id)}
              className={[
                "shrink-0 rounded-full border px-1.5 py-1 pr-3 text-xs font-semibold transition",
                activeId === c.id
                  ? "border-slate-300 bg-slate-100 text-slate-900"
                  : "border-slate-200 bg-white text-slate-700 hover:bg-slate-50",
              ].join(" ")}
            >
              <span className="inline-flex items-center gap-2">
                <span className="h-6 w-6 overflow-hidden rounded-full bg-slate-100">
                  {c.imageUrl ? (
                    <img
                      src={c.imageUrl}
                      alt={c.name}
                      className="h-full w-full object-cover"
                      loading="lazy"
                    />
                  ) : null}
                </span>
                <span>{c.name}</span>
              </span>
            </Link>
          ))}
          </div>
        </div>
      </div>
    </div>
  );
}

