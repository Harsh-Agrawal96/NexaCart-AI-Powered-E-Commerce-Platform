import { Outlet, useLocation, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import Login from "../pages/Login";
import Register from "../pages/Register";

export default function Layout() {
  const location = useLocation();
  const navigate = useNavigate();
  const authModal =
    location.pathname === "/login"
      ? "login"
      : location.pathname === "/register"
        ? "register"
        : null;

  return (
    <div className="flex min-h-dvh flex-col bg-slate-50">
      <Navbar />
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">
        <Outlet />
      </main>
      <Footer />

      {authModal ? (
        <div className="fixed inset-0 z-[60]">
          <button
            type="button"
            aria-label="Kapat"
            onClick={() => navigate(-1)}
            className="absolute inset-0 bg-black/40"
          />
          <div className="absolute inset-0 flex items-center justify-center p-4">
            <div className="w-full max-w-md">
              {authModal === "login" ? <Login /> : <Register />}
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}

