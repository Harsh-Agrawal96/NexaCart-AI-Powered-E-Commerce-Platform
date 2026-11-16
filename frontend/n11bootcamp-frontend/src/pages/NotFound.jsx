import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="mx-auto flex max-w-lg flex-col items-center px-4 py-16 text-center">
      <p className="text-6xl font-black tracking-tight text-slate-200">404</p>
      <h1 className="mt-4 text-2xl font-extrabold tracking-tight text-slate-900">
        Ups! Sayfa bulunamadı
      </h1>
      <p className="mt-2 text-sm text-slate-600">
        Aradığın adres yanlış olabilir veya sayfa kaldırılmış olabilir.
      </p>
      <Link
        to="/"
        className="mt-8 inline-flex items-center justify-center rounded-2xl bg-slate-900 px-6 py-3 text-sm font-extrabold text-white transition hover:bg-slate-800"
      >
        Ana sayfaya dön
      </Link>
    </div>
  );
}
