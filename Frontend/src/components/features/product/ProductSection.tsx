"use client";

import { useState, useEffect } from "react";
import Category from "./Category";
import ProductList from "./ProductList";
import { useCart } from "@/src/store/cart";
import { ProductService } from "@/src/services";
import { Product } from "@/src/types";

const ProductSection = () => {
  const { addToCart } = useCart();
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedCategory, setSelectedCategory] = useState("전체");

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const productData = await ProductService.getProducts();
        setProducts(productData);
      } catch (error) {
        console.error(error);
      }
    };

    fetchProducts();
  }, []);

  const filteredProducts =
    selectedCategory === "전체"
      ? products
      : products.filter((product) => product.category === selectedCategory);

  return (
    <>
      <Category
        products={products}
        selectedCategory={selectedCategory}
        onSelectCategory={setSelectedCategory}
      />
      <ProductList products={filteredProducts} addToCart={addToCart} />
    </>
  );
};

export default ProductSection;
