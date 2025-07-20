import ProductCard from "./ProductCard";
import { Product } from "./types";

interface ProductListProps {
    products: Product[];
    addToCart: (product: Product, quantity: number) => void;
}

const ProductList = ({ products, addToCart }: ProductListProps) => {
    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {products.map((product) => (
                <ProductCard key={product.id} product={product} addToCart={addToCart} />
            ))}
        </div>
    );
};

export default ProductList;