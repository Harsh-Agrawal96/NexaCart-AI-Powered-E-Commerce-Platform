import { useCallback, useEffect, useRef, useState } from "react";
import { requestProductAdvice } from "../services/aiAdviceService";

function Bubble({ role, children }) {
  const isUser = role === "user";
  return (
    <div
      className={`flex w-full ${isUser ? "justify-end" : "justify-start"}`}
    >
      <div
        className={[
          "max-w-[92%] rounded-2xl px-4 py-3 text-sm shadow-sm",
          isUser
            ? "bg-violet-600 text-white"
            : "border border-slate-200 bg-white text-slate-800",
        ].join(" ")}
      >
        {children}
      </div>
    </div>
  );
}

function AdviceCard({ advice }) {
  if (!advice) return null;
  const pros = Array.isArray(advice.pros) ? advice.pros : [];
  const cons = Array.isArray(advice.cons) ? advice.cons : [];
  return (
    <div className="space-y-3 text-sm">
      {advice.verdict ? (
        <div>
          <div className="text-[11px] font-extrabold uppercase tracking-wide text-slate-500">
            Karar
          </div>
          <div className="mt-1 font-semibold text-slate-900">{advice.verdict}</div>
        </div>
      ) : null}
      {advice.summary ? (
        <div>
          <div className="text-[11px] font-extrabold uppercase tracking-wide text-slate-500">
            Özet
          </div>
          <p className="mt-1 whitespace-pre-wrap leading-relaxed text-slate-700">
            {advice.summary}
          </p>
        </div>
      ) : null}
      {pros.length ? (
        <div>
          <div className="text-[11px] font-extrabold uppercase tracking-wide text-emerald-700">
            Artılar
          </div>
          <ul className="mt-1 list-inside list-disc space-y-1 text-slate-700">
            {pros.map((p, i) => (
              <li key={i}>{p}</li>
            ))}
          </ul>
        </div>
      ) : null}
      {cons.length ? (
        <div>
          <div className="text-[11px] font-extrabold uppercase tracking-wide text-rose-700">
            Eksiler
          </div>
          <ul className="mt-1 list-inside list-disc space-y-1 text-slate-700">
            {cons.map((c, i) => (
              <li key={i}>{c}</li>
            ))}
          </ul>
        </div>
      ) : null}
      <div className="flex flex-wrap gap-x-4 gap-y-1 border-t border-slate-100 pt-2 text-xs text-slate-500">
        {typeof advice.confidence === "number" ? (
          <span>
            Güven: <strong className="text-slate-700">{advice.confidence.toFixed(2)}</strong>
          </span>
        ) : null}
        {typeof advice.usedReviewCount === "number" ? (
          <span>
            Kullanılan yorum:{" "}
            <strong className="text-slate-700">{advice.usedReviewCount}</strong>
          </span>
        ) : null}
      </div>
    </div>
  );
}

export default function ProductAiChatPanel({ productId, productName }) {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState(() => [
    {
      id: "welcome",
      role: "assistant",
      text:
        "Merhaba, ben ürün asistanıyım. Bu ürünü alıp almaman için yorumlar ve ürün özetiyle birlikte değerlendirebilirim. Sorunu yaz veya boş bırakıp gönder — genel tavsiye üretirim.",
    },
  ]);
  const bottomRef = useRef(null);

  const scrollToBottom = useCallback(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, open, scrollToBottom]);

  async function handleSend(e) {
    e.preventDefault();
    if (!productId || loading) return;
    const q = String(input ?? "").trim().slice(0, 500);
    setMessages((m) => [
      ...m,
      {
        id: `u-${Date.now()}`,
        role: "user",
        text: q || "(Genel tavsiye)",
      },
    ]);
    setInput("");
    setLoading(true);
    try {
      const advice = await requestProductAdvice({
        productId,
        question: q,
      });
      setMessages((m) => [
        ...m,
        {
          id: `a-${Date.now()}`,
          role: "assistant",
          advice,
        },
      ]);
    } catch (err) {
      const msg =
        err?.response?.data?.data?.detail ??
        err?.response?.data?.message ??
        err?.message ??
        "Asistan şu an yanıt veremedi.";
      setMessages((m) => [
        ...m,
        {
          id: `e-${Date.now()}`,
          role: "assistant",
          text: `Bir hata oluştu: ${msg}`,
        },
      ]);
    } finally {
      setLoading(false);
    }
  }

  if (!productId) return null;

  return (
    <>
      <button
        type="button"
        aria-label="Yapay zekâ ürün asistanını aç"
        onClick={() => setOpen((v) => !v)}
        className="fixed bottom-6 right-6 z-40 flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-violet-600 to-indigo-700 text-2xl shadow-lg ring-4 ring-white/80 transition hover:scale-105 hover:shadow-xl focus:outline-none focus-visible:ring-4 focus-visible:ring-violet-300"
      >
        🤖
      </button>

      {open ? (
        <div className="fixed bottom-24 right-6 z-40 flex w-[min(100vw-2rem,380px)] flex-col overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-2xl">
          <div className="flex items-center gap-3 border-b border-slate-100 bg-gradient-to-r from-violet-600 to-indigo-700 px-4 py-3 text-white">
            <span className="text-2xl" aria-hidden>
              🤖
            </span>
            <div className="min-w-0 flex-1">
              <div className="text-xs font-extrabold uppercase tracking-wide opacity-90">
                AI Asistan
              </div>
              <div className="truncate text-sm font-semibold">
                {productName || "Ürün"}
              </div>
            </div>
            <button
              type="button"
              onClick={() => setOpen(false)}
              className="rounded-xl bg-white/15 px-3 py-1 text-xs font-extrabold hover:bg-white/25"
            >
              Kapat
            </button>
          </div>

          <div className="max-h-[min(52vh,420px)] space-y-3 overflow-y-auto bg-slate-50 px-3 py-4">
            {messages.map((msg) => (
              <Bubble key={msg.id} role={msg.role}>
                {msg.advice ? (
                  <AdviceCard advice={msg.advice} />
                ) : (
                  <span className="whitespace-pre-wrap">{msg.text}</span>
                )}
              </Bubble>
            ))}
            {loading ? (
              <div className="text-center text-xs font-semibold text-slate-500">
                Düşünüyorum…
              </div>
            ) : null}
            <div ref={bottomRef} />
          </div>

          <form
            onSubmit={handleSend}
            className="border-t border-slate-100 bg-white p-3"
          >
            <div className="flex gap-2">
              <input
                type="text"
                maxLength={500}
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="Örn: Bu ürünü alır mıydın?"
                className="min-w-0 flex-1 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm outline-none focus:ring-4 focus:ring-violet-200"
              />
              <button
                type="submit"
                disabled={loading}
                className="shrink-0 rounded-2xl bg-slate-900 px-4 py-2 text-sm font-extrabold text-white hover:bg-slate-800 disabled:opacity-50"
              >
                Gönder
              </button>
            </div>
          </form>
        </div>
      ) : null}
    </>
  );
}
