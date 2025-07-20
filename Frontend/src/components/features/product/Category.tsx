"use client";

import Button from "@/src/components/common/Button";
import { Product } from "./types";

interface CategoryProps {
    products: Product[];
    selectedCategory: string;
    onSelectCategory: (category: string) => void;
}

const Category = ({ products, selectedCategory, onSelectCategory }: CategoryProps) => {
    const categories = ["전체", ...Array.from(new Set(products.map(p => p.category)))];

    return (
        <div className="flex items-center space-x-4 py-4">
            {categories.map((category) => (
                <Button
                    key={category}
                    text={category}
                    bgColor={selectedCategory === category ? "bg-black" : "bg-white"}
                    fontColor={selectedCategory === category ? "text-white" : "text-black"}
                    hoverColor={selectedCategory === category ? "hover:bg-black" : "hover:bg-gray-200"}
                    onClick={() => onSelectCategory(category)}
                />
            ))}
        </div>
    );
};

export default Category;