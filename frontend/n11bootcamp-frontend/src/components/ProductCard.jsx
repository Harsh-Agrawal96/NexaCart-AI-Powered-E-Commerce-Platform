import { Link } from "react-router-dom";
import { formatTry } from "../utils/money";

function ProductCard({ product }) {
  return (
    <Link
      to={`/products/${product.id}`}
      className="group block overflow-hidden rounded-2xl border border-slate-200 bg-white transition hover:shadow-lg"
    >
      <div className="flex h-44 items-center justify-center bg-slate-100">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.02]"
          />
        ) : (
          <span className="text-sm font-medium text-slate-500">Resim Yok</span>
        )}
      </div>

      <div className="flex flex-col gap-2 p-4">
        <div className="line-clamp-2 text-sm font-semibold text-slate-900">
          {product.name}
        </div>
        {product.categoryName ? (
          <div className="text-xs font-medium text-slate-500">
            {product.categoryName}
          </div>
        ) : null}
        <div className="mt-auto text-base font-extrabold text-violet-700">
          {formatTry(product.price)}
        </div>
      </div>
    </Link>
  );
}

export default ProductCard;
