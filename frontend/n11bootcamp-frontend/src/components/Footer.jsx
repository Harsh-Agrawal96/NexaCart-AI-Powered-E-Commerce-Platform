export default function Footer() {
  return (
    <footer className="border-t border-slate-200 bg-white">
      <div className="mx-auto flex max-w-6xl flex-col gap-2 px-4 py-8 sm:flex-row sm:items-center sm:justify-between">
        <div className="text-sm text-slate-600">
          © {new Date().getFullYear()} n11 Bootcamp
        </div>
        <div className="text-xs text-slate-500">
          React + Vite • Tailwind • Microservices Backend
        </div>
      </div>
    </footer>
  );
}

