import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  adminCreateProduct,
  adminGetProductById,
  adminUpdateProduct,
} from "../../services/adminProductService";
import { adminGetCategories } from "../../services/adminCategoryService";

function Field({ label, children }) {
  return (
    <div className="space-y-1">
      <div className="text-xs font-semibold text-slate-600">{label}</div>
      {children}
    </div>
  );
}

export default function AdminProductForm({ mode }) {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = mode === "edit";

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({
    name: "",
    sku: "",
    price: "",
    quantity: "",
    imageUrl: "",
    categoryId: "",
    description: "",
    active: true, // only used in update
  });

  const payload = useMemo(() => {
    const base = {
      name: form.name.trim(),
      description: form.description.trim() || null,
      price: form.price === "" ? null : Number(form.price),
      quantity: form.quantity === "" ? null : Number(form.quantity),
      sku: form.sku.trim(),
      imageUrl: form.imageUrl.trim() || null,
    };

    if (isEdit) {
      // UpdateProductRequest fields are optional, active & categoryId allowed
      return {
        ...base,
        categoryId: form.categoryId || null,
        active: !!form.active,
      };
    }

    // CreateProductRequest required: name, sku, price, quantity, categoryId
    return {
      ...base,
      categoryId: form.categoryId || null,
    };
  }, [form, isEdit]);

  useEffect(() => {
    let mounted = true;
    async function run() {
      setLoading(true);
      setError("");
      try {
        const catRes = await adminGetCategories();
        const list = Array.isArray(catRes?.data) ? catRes.data : [];
        if (mounted) setCategories(list);

        if (isEdit && id) {
          const res = await adminGetProductById(id);
          const p = res?.data ?? null;
          if (p && mounted) {
            setForm({
              name: p.name ?? "",
              sku: p.sku ?? "",
              price: p.price ?? "",
              quantity: p.quantity ?? "",
              imageUrl: p.imageUrl ?? "",
              description: p.description ?? "",
              active: typeof p.active === "boolean" ? p.active : true,
              categoryId: p.category?.id ?? "",
            });
          }
        }
      } catch (e) {
        const msg =
          e?.response?.data?.data?.detail ??
          e?.response?.data?.message ??
          "Form yüklenemedi";
        if (mounted) setError(msg);
      } finally {
        if (mounted) setLoading(false);
      }
    }
    run();
    return () => {
      mounted = false;
    };
  }, [id, isEdit]);

  async function onSubmit(e) {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      if (isEdit && id) {
        await adminUpdateProduct(id, payload);
      } else {
        await adminCreateProduct(payload);
      }
      navigate("/admin/products");
    } catch (e) {
      const msg =
        e?.response?.data?.data?.detail ??
        e?.response?.data?.message ??
        "Kaydedilemedi";
      setError(msg);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900">
            {isEdit ? "Ürün Güncelle" : "Yeni Ürün"}
          </h1>
          <p className="mt-1 text-sm text-slate-600">
            Admin endpoint’lerine bağlı CRUD formu.
          </p>
        </div>
        <Link
          to="/admin/products"
          className="inline-flex items-center justify-center rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-800 hover:bg-slate-50"
        >
          Listeye dön
        </Link>
      </div>

      {loading ? (
        <div className="rounded-3xl border border-slate-200 bg-white p-6 text-sm text-slate-700">
          Yükleniyor...
        </div>
      ) : (
        <form
          onSubmit={onSubmit}
          className="rounded-3xl border border-slate-200 bg-white p-6"
        >
          <div className="grid gap-4 lg:grid-cols-2">
            <Field label="Ürün adı">
              <input
                value={form.name}
                onChange={(e) => setForm((s) => ({ ...s, name: e.target.value }))}
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                required
              />
            </Field>

            <Field label="SKU">
              <input
                value={form.sku}
                onChange={(e) => setForm((s) => ({ ...s, sku: e.target.value }))}
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                required={!isEdit}
              />
            </Field>

            <Field label="Fiyat">
              <input
                value={form.price}
                onChange={(e) =>
                  setForm((s) => ({ ...s, price: e.target.value.replace(/[^\d.]/g, "") }))
                }
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                inputMode="decimal"
                required={!isEdit}
              />
            </Field>

            <Field label="Stok">
              <input
                value={form.quantity}
                onChange={(e) =>
                  setForm((s) => ({
                    ...s,
                    quantity: e.target.value.replace(/[^\d]/g, ""),
                  }))
                }
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                inputMode="numeric"
                required={!isEdit}
              />
            </Field>

            <Field label="Kategori">
              <select
                value={form.categoryId}
                onChange={(e) =>
                  setForm((s) => ({ ...s, categoryId: e.target.value }))
                }
                className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                required={!isEdit}
              >
                <option value="">Seçiniz</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </Field>

            <Field label="Görsel URL">
              <input
                value={form.imageUrl}
                onChange={(e) =>
                  setForm((s) => ({ ...s, imageUrl: e.target.value }))
                }
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              />
            </Field>

            <div className="lg:col-span-2">
              <Field label="Açıklama">
                <textarea
                  value={form.description}
                  onChange={(e) =>
                    setForm((s) => ({ ...s, description: e.target.value }))
                  }
                  className="min-h-28 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                />
              </Field>
            </div>

            {isEdit ? (
              <Field label="Aktif">
                <label className="inline-flex items-center gap-2 text-sm font-semibold text-slate-700">
                  <input
                    type="checkbox"
                    checked={form.active}
                    onChange={(e) =>
                      setForm((s) => ({ ...s, active: e.target.checked }))
                    }
                  />
                  Yayında
                </label>
              </Field>
            ) : null}
          </div>

          {error ? (
            <div className="mt-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
              {error}
            </div>
          ) : null}

          <div className="mt-6 flex flex-wrap items-center justify-between gap-3">
            <button
              type="submit"
              disabled={saving}
              className="inline-flex items-center justify-center rounded-2xl bg-slate-900 px-5 py-3 text-sm font-extrabold text-white hover:bg-slate-800 disabled:opacity-50"
            >
              {saving ? "Kaydediliyor..." : "Kaydet"}
            </button>

            <details className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700">
              <summary className="cursor-pointer select-none text-xs font-extrabold text-slate-700">
                Gönderilen payload (debug)
              </summary>
              <pre className="mt-3 overflow-x-auto text-xs">
{JSON.stringify(payload, null, 2)}
              </pre>
            </details>
          </div>
        </form>
      )}
    </div>
  );
}

