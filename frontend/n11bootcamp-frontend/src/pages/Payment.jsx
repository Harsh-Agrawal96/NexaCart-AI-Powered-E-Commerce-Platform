import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { getKeycloakInstance } from "../auth/keycloakRuntime";
import { useToast } from "../context/ToastContext.jsx";
import { pay } from "../services/paymentService";
import { formatTry } from "../utils/money";
import { clearPendingCheckout } from "../utils/pendingCheckoutStorage";
import { getUserIdFromKeycloak } from "../utils/user";

const providers = [
  { value: "N11_BOOTCAMP_PAY", label: "N11 Bootcamp Pay" },
  { value: "IYZICO_PAY", label: "Iyzico" },
  { value: "ALWAYS_FAIL_PAY", label: "Always Fail (Test)" },
];

export default function Payment() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [searchParams] = useSearchParams();
  const orderId = searchParams.get("orderId") ?? "";
  const grandTotalParam = searchParams.get("grandTotal") ?? "";

  const [providerType, setProviderType] = useState("N11_BOOTCAMP_PAY");
  const isIyzico = providerType === "IYZICO_PAY";

  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState({
    cardHolderName: "",
    cardNumber: "",
    expireMonth: "",
    expireYear: "",
    cvc: "",
    buyerName: "",
    buyerSurname: "",
    buyerEmail: "",
    buyerPhone: "",
    buyerCity: "",
    buyerAddress: "",
  });

  const canSubmit = useMemo(() => {
    if (!orderId) return false;
    // Backend N11/FAIL için de bu alanları görmezden geliyor; UI kırılmasın diye her provider’da submit serbest.
    return true;
  }, [orderId]);

  useEffect(() => {
    // Demo/placeholder değerleri sadece N11/FAIL seçiminde otomatik dolduralım.
    if (providerType === "IYZICO_PAY") return;
    setForm((s) => ({
      ...s,
      cardHolderName: s.cardHolderName || "Test Kullanıcı",
      cardNumber: s.cardNumber || "5528790000000008",
      expireMonth: s.expireMonth || "12",
      expireYear: s.expireYear || "2030",
      cvc: s.cvc || "123",
      buyerName: s.buyerName || "Test",
      buyerSurname: s.buyerSurname || "User",
      buyerEmail: s.buyerEmail || "test.user@n11bootcamp.local",
      buyerPhone: s.buyerPhone || "5551112233",
      buyerCity: s.buyerCity || "İstanbul",
      buyerAddress: s.buyerAddress || "Örnek Mah. Test Sok. No:1",
    }));
  }, [providerType]);

  async function onSubmit(e) {
    e.preventDefault();

    if (!orderId) {
      showToast({
        title: "Sipariş bulunamadı",
        subtitle: "Ödeme için geçerli bir sipariş bağlantısı gerekli.",
        variant: "error",
      });
      return;
    }

    setLoading(true);
    try {
      const result = await pay({
        orderId,
        providerType,
        iyzico: form,
      });
      const inner = result?.data ?? result ?? null;

      if (inner?.success) {
        const uid = getUserIdFromKeycloak(getKeycloakInstance());
        if (uid) clearPendingCheckout(uid);
        const tid = inner?.transactionId;
        const subtitleParts = [
          tid ? `İşlem no: ${tid}` : null,
          orderId ? `Sipariş: ${orderId}` : null,
        ].filter(Boolean);
        showToast({
          title: "Ödeme başarılı",
          subtitle:
            subtitleParts.length > 0 ? subtitleParts.join(" · ") : undefined,
          variant: "success",
        });
        navigate("/", { replace: true });
      } else {
        const reason =
          inner?.failReason ?? inner?.message ?? "Ödeme tamamlanamadı.";
        showToast({
          title: "Ödeme başarısız",
          subtitle: String(reason),
          variant: "error",
        });
        navigate("/cart", { replace: true });
      }
    } catch (err) {
      const msg =
        err?.response?.data?.data?.failReason ??
        err?.response?.data?.data?.detail ??
        err?.response?.data?.message ??
        err?.message ??
        "Ödeme başarısız";
      showToast({
        title: "Ödeme başarısız",
        subtitle: String(msg),
        variant: "error",
      });
      navigate("/cart", { replace: true });
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
            Ödeme
          </h1>
          <div className="mt-1 text-sm text-slate-600">
            Sipariş ID: <span className="font-mono">{orderId || "—"}</span>
          </div>
        </div>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
        >
          Geri
        </button>
      </div>

      <div className="grid gap-6 lg:grid-cols-12">
        <div className="lg:col-span-7">
          <form
            onSubmit={onSubmit}
            className="rounded-3xl border border-slate-200 bg-white p-6"
          >
            <div className="text-sm font-extrabold text-slate-900">
              Ödeme Yöntemi
            </div>
            <div className="mt-3">
              <select
                value={providerType}
                onChange={(e) => setProviderType(e.target.value)}
                className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              >
                {providers.map((p) => (
                  <option key={p.value} value={p.value}>
                    {p.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="mt-6 space-y-6">
              <div className="flex items-center justify-between gap-3">
                <div className="text-sm font-extrabold text-slate-900">
                  Kart Bilgileri
                </div>
                {!isIyzico ? (
                  <div className="text-xs font-semibold text-slate-500">
                    (Test / placeholder)
                  </div>
                ) : null}
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <input
                  value={form.cardHolderName}
                  onChange={(e) =>
                    setForm((s) => ({ ...s, cardHolderName: e.target.value }))
                  }
                  className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                  placeholder="Kart üzerindeki isim"
                />
                <input
                  value={form.cardNumber}
                  onChange={(e) =>
                    setForm((s) => ({ ...s, cardNumber: e.target.value }))
                  }
                  className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                  placeholder="Kart numarası"
                  inputMode="numeric"
                />
                <input
                  value={form.expireMonth}
                  onChange={(e) =>
                    setForm((s) => ({ ...s, expireMonth: e.target.value }))
                  }
                  className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                  placeholder="Ay (MM)"
                  inputMode="numeric"
                />
                <input
                  value={form.expireYear}
                  onChange={(e) =>
                    setForm((s) => ({ ...s, expireYear: e.target.value }))
                  }
                  className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                  placeholder="Yıl (YYYY)"
                  inputMode="numeric"
                />
                <input
                  value={form.cvc}
                  onChange={(e) => setForm((s) => ({ ...s, cvc: e.target.value }))}
                  className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                  placeholder="CVC"
                  inputMode="numeric"
                />
              </div>

              <div>
                <div className="text-sm font-extrabold text-slate-900">
                  Alıcı Bilgileri
                </div>
                <div className="mt-3 grid gap-3 sm:grid-cols-2">
                  <input
                    value={form.buyerName}
                    onChange={(e) =>
                      setForm((s) => ({ ...s, buyerName: e.target.value }))
                    }
                    className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                    placeholder="Ad"
                  />
                  <input
                    value={form.buyerSurname}
                    onChange={(e) =>
                      setForm((s) => ({ ...s, buyerSurname: e.target.value }))
                    }
                    className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                    placeholder="Soyad"
                  />
                  <input
                    value={form.buyerEmail}
                    onChange={(e) =>
                      setForm((s) => ({ ...s, buyerEmail: e.target.value }))
                    }
                    className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                    placeholder="E-posta"
                    inputMode="email"
                  />
                  <input
                    value={form.buyerPhone}
                    onChange={(e) =>
                      setForm((s) => ({ ...s, buyerPhone: e.target.value }))
                    }
                    className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                    placeholder="Telefon"
                    inputMode="tel"
                  />
                  <input
                    value={form.buyerCity}
                    onChange={(e) =>
                      setForm((s) => ({ ...s, buyerCity: e.target.value }))
                    }
                    className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                    placeholder="Şehir"
                  />
                  <input
                    value={form.buyerAddress}
                    onChange={(e) =>
                      setForm((s) => ({ ...s, buyerAddress: e.target.value }))
                    }
                    className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200 sm:col-span-2"
                    placeholder="Adres"
                  />
                </div>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading || !canSubmit}
              className="mt-6 inline-flex w-full items-center justify-center rounded-2xl bg-slate-900 px-4 py-3 text-sm font-extrabold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {loading ? "Gönderiliyor..." : "Ödemeyi Tamamla"}
            </button>
          </form>
        </div>

        <div className="space-y-4 lg:col-span-5">
          <div className="rounded-3xl border border-slate-200 bg-white p-6">
            <div className="text-sm font-extrabold text-slate-900">Tutar</div>
            <div className="mt-2 text-3xl font-extrabold text-violet-700">
              {grandTotalParam ? formatTry(Number(grandTotalParam)) : "—"}
            </div>
            <p className="mt-2 text-sm text-slate-600">
              Tutar backend tarafından siparişten hesaplanır.
            </p>
          </div>

          <div className="rounded-3xl border border-slate-200 bg-white p-6">
            <div className="text-sm font-extrabold text-slate-900">Not</div>
            <p className="mt-2 text-sm text-slate-600">
              Test sağlayıcılarında form placeholder amaçlıdır; backend bu alanları
              görmezden gelir. Iyzico seçince gerçek bilgilerle deneyebilirsin.
            </p>
          </div>

          <div className="rounded-3xl border border-slate-200 bg-white p-6">
            <div className="text-sm font-extrabold text-slate-900">
              Devam
            </div>
            <div className="mt-3 flex flex-col gap-2">
              <Link
                to="/cart"
                className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
              >
                Sepete dön
              </Link>
              <Link
                to="/products"
                className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
              >
                Ürünlere git
              </Link>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

