import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  adminCreateUser,
  adminGetUserById,
  adminUpdateUser,
} from "../../services/adminUserService";

function Field({ label, children }) {
  return (
    <div className="space-y-1">
      <div className="text-xs font-semibold text-slate-600">{label}</div>
      {children}
    </div>
  );
}

export default function AdminUserForm({ mode }) {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = mode === "edit";

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const [form, setForm] = useState({
    keycloakId: "",
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
  });

  const payload = useMemo(() => {
    if (isEdit) {
      // UpdateUserRequest
      return {
        firstName: form.firstName.trim() || null,
        lastName: form.lastName.trim() || null,
        email: form.email.trim() || null,
        phoneNumber: form.phoneNumber.trim() || null,
      };
    }

    // CreateUserRequest required: keycloakId, firstName, lastName, email
    return {
      keycloakId: form.keycloakId.trim(),
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      email: form.email.trim(),
      phoneNumber: form.phoneNumber.trim() || null,
    };
  }, [form, isEdit]);

  useEffect(() => {
    let mounted = true;
    async function run() {
      setLoading(true);
      setError("");
      try {
        if (isEdit && id) {
          const res = await adminGetUserById(id);
          const u = res?.data ?? null;
          if (u && mounted) {
            setForm({
              keycloakId: u.keycloakId ?? "",
              firstName: u.firstName ?? "",
              lastName: u.lastName ?? "",
              email: u.email ?? "",
              phoneNumber: u.phoneNumber ?? "",
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
        await adminUpdateUser(id, payload);
      } else {
        await adminCreateUser(payload);
      }
      navigate("/admin/users");
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
            {isEdit ? "Kullanıcı Güncelle" : "Yeni Kullanıcı"}
          </h1>
          <p className="mt-1 text-sm text-slate-600">User Service CRUD formu.</p>
        </div>
        <Link
          to="/admin/users"
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
            {!isEdit ? (
              <Field label="Keycloak ID (sub)">
                <input
                  value={form.keycloakId}
                  onChange={(e) =>
                    setForm((s) => ({ ...s, keycloakId: e.target.value }))
                  }
                  className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                  placeholder="keycloak sub..."
                  required
                />
              </Field>
            ) : (
              <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="text-xs font-semibold text-slate-500">
                  Keycloak ID
                </div>
                <div className="mt-1 break-all font-mono text-xs text-slate-700">
                  {form.keycloakId || "—"}
                </div>
              </div>
            )}

            <Field label="E-posta">
              <input
                value={form.email}
                onChange={(e) => setForm((s) => ({ ...s, email: e.target.value }))}
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                inputMode="email"
                required={!isEdit}
              />
            </Field>

            <Field label="Ad">
              <input
                value={form.firstName}
                onChange={(e) =>
                  setForm((s) => ({ ...s, firstName: e.target.value }))
                }
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                required={!isEdit}
              />
            </Field>

            <Field label="Soyad">
              <input
                value={form.lastName}
                onChange={(e) =>
                  setForm((s) => ({ ...s, lastName: e.target.value }))
                }
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                required={!isEdit}
              />
            </Field>

            <Field label="Telefon">
              <input
                value={form.phoneNumber}
                onChange={(e) =>
                  setForm((s) => ({ ...s, phoneNumber: e.target.value }))
                }
                className="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
                placeholder="+90555..."
              />
            </Field>
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

