import { Link } from "react-router-dom";
import BehaviorRecommendationsSection from "../components/BehaviorRecommendationsSection.jsx";
import { useKeycloakAuth } from "../auth/KeycloakProvider.jsx";

export default function Home() {
  const { ready, authenticated } = useKeycloakAuth();

  return (
    <section className="space-y-6">
      <div className="rounded-2xl border border-slate-200 bg-white p-6 sm:p-10">
        <h1 className="text-3xl font-extrabold tracking-tight text-slate-900 sm:text-4xl">
          Kampanyalı Ürünler
        </h1>
        <p className="mt-3 max-w-2xl text-slate-600">
          Öne çıkan ürünleri keşfet, kategoriler arasında gezin.
        </p>
        <div className="mt-6 flex flex-wrap gap-3">
          <Link
            to="/products"
            className="inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800"
          >
            Alışverişe Başla
          </Link>
          {ready && !authenticated ? (
            <>
              <Link
                to="/register"
                className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
              >
                Kayıt Ol
              </Link>
              <Link
                to="/login"
                className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
              >
                Giriş Yap
              </Link>
            </>
          ) : null}
        </div>
      </div>

      <BehaviorRecommendationsSection />
    </section>
  );
}
