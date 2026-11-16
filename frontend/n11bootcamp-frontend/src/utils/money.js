export function parseMoney(input) {
  const s = String(input ?? "").trim().replace(",", ".");
  if (!s) return null;

  const n = Number(s);
  if (!Number.isFinite(n)) return null;

  return n;
}

export function formatTry(amount) {
  const n = typeof amount === "number" ? amount : Number(amount);
  if (!Number.isFinite(n)) return "—";

  try {
    return new Intl.NumberFormat("tr-TR", {
      style: "currency",
      currency: "TRY",
      maximumFractionDigits: 2,
    }).format(n);
  } catch {
    return `${n} ₺`;
  }
}
