import { Routes, Route } from "react-router-dom";
import Layout from "./layouts/Layout";
import Home from "./pages/Home";
import Products from "./pages/Products";
import ProductDetail from "./pages/ProductDetail";
import Cart from "./pages/Cart";
import AuthRoutePlaceholder from "./pages/AuthRoutePlaceholder";
import Payment from "./pages/Payment";
import AdminLayout from "./layouts/AdminLayout";
import AdminDashboard from "./pages/admin/AdminDashboard";
import AdminProductsList from "./pages/admin/AdminProductsList";
import AdminProductForm from "./pages/admin/AdminProductForm";
import AdminCategoriesList from "./pages/admin/AdminCategoriesList";
import AdminCategoryForm from "./pages/admin/AdminCategoryForm";
import AdminServices from "./pages/admin/AdminServices";
import AdminUsersList from "./pages/admin/AdminUsersList";
import AdminUserForm from "./pages/admin/AdminUserForm";
import AdminLogs from "./pages/admin/AdminLogs";
import AdminCartsList from "./pages/admin/AdminCartsList";
import AdminCartDetail from "./pages/admin/AdminCartDetail";
import AdminOrdersList from "./pages/admin/AdminOrdersList";
import AdminOrderDetail from "./pages/admin/AdminOrderDetail";
import NotFound from "./pages/NotFound";
import AdminReviewsList from "./pages/admin/AdminReviewsList";
import AdminReviewDetail from "./pages/admin/AdminReviewDetail";

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/products" element={<Products />} />
        <Route path="/products/:id" element={<ProductDetail />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/login" element={<AuthRoutePlaceholder />} />
        <Route path="/register" element={<AuthRoutePlaceholder />} />
        <Route path="/payment" element={<Payment />} />
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="products" element={<AdminProductsList />} />
          <Route path="products/new" element={<AdminProductForm mode="create" />} />
          <Route path="products/:id/edit" element={<AdminProductForm mode="edit" />} />

          <Route path="categories" element={<AdminCategoriesList />} />
          <Route path="categories/new" element={<AdminCategoryForm mode="create" />} />
          <Route path="categories/:id/edit" element={<AdminCategoryForm mode="edit" />} />

          <Route path="users" element={<AdminUsersList />} />
          <Route path="users/new" element={<AdminUserForm mode="create" />} />
          <Route path="users/:id/edit" element={<AdminUserForm mode="edit" />} />

          <Route path="logs" element={<AdminLogs />} />

          <Route path="reviews" element={<AdminReviewsList />} />
          <Route path="reviews/:id" element={<AdminReviewDetail />} />

          <Route path="carts" element={<AdminCartsList />} />
          <Route path="carts/:id" element={<AdminCartDetail />} />

          <Route path="orders" element={<AdminOrdersList />} />
          <Route path="orders/:id" element={<AdminOrderDetail />} />

          <Route path="services" element={<AdminServices />} />

          <Route path="*" element={<NotFound />} />
        </Route>

        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}

export default App;
