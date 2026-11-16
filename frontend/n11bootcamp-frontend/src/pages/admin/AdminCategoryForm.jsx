import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  adminCreateCategory,
  adminGetCategoryById,
  adminUpdateCategory,
} from "../../services/adminCategoryService";

function Field({ label, children }) {
  return (
    <div className="space-y-1">
      <div className="text-xs font-semibold text-slate-600">{label}</div>
      {children}
    </div>
  );
}

export default function AdminCategoryForm({ mode }) {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = mode === "edit";

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const [form, setForm] = useState({
    name: "",
    slug: "",
    description: "",
    imageUrl: "",
    displayOrder: "",
    active: true, // only used in update
  });

  const payload = useMemo(() => {
    const base = {
      name: form.name.trim(),
      slug: form.slug.trim(),
      description: form.description.trim() || null,
      imageUrl: form.imageUrl.trim() || null,
      displayOrder: form.displayOrder === "" ? null : Number(form.displayOrder),
    };
    if (isEdit) return { ...base, active: !!form.active };
    return base; // CreateCategoryRequest has no `active`
  }, [form]);

  useEffect(() => {
    let mounted = true;
    async function run() {
      setLoading(true);
      setError("");
      try {
        if (isEdit && id) {
          const res = await adminGetCategoryById(id);
          const c = res?.data ?? null;
          if (c && mounted) {
            setForm({
              name: c.name ?? "",
              slug: c.slug ?? "",
              description: c.description ?? "",
              imageUrl: c.imageUrl ?? "",
              displayOrder: c.displayOrder ?? "",
              active: typeof c.active === "boolean" ? c.active : true,
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
        await adminUpdateCategory(id, payload);
      } else {
        await adminCreateCategory(payload);
      }
      navigate("/admin/categories");
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
            {isEdit ? "Kategori Güncelle" : "Yeni Kategori"}
          </h1>
          <p className="mt-1 text-sm text-slate-600">Admin CRUD formu.</p>
        </div>
        <Link
          to="/admin/categories"
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
            <Field label="Kategori adı">
              <input
                value={form.name}
                onChange={(e) => setForm((s) => ({ ...s, name: e.target.value }))}
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                required
              />
            </Field>

            <Field label="Slug">
              <input
                value={form.slug}
                onChange={(e) => setForm((s) => ({ ...s, slug: e.target.value }))}
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              />
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

            <Field label="Sıra">
              <input
                value={form.displayOrder}
                onChange={(e) =>
                  setForm((s) => ({
                    ...s,
                    displayOrder: e.target.value.replace(/[^\d]/g, ""),
                  }))
                }
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                inputMode="numeric"
              />
            </Field>

            <div className="lg:col-span-2">
              <Field label="Açıklama">
                <textarea
                  value={form.description}
                  onChange={(e) =>
                    setForm((s) => ({ ...s, description: e.target.value }))
                  }
                  className="min-h-24 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
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

