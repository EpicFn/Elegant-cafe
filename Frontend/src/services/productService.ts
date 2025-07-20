import client from "@/src/lib/backend/client";
import type {components} from "@/src/lib/backend/api/schema.d.ts";
import {Product} from "@/src/types/product";

type ProductDto = components["schemas"]["ProductDto"];
type ProductCreateReqBody = components["schemas"]["ProductCreateReqBody"];
type ProductUpdateReqBody = components["schemas"]["ProductUpdateReqBody"];

export class ProductService {
    static async getProducts(page: number = 1, pageSize: number = 100): Promise<Product[]> {
        const {data: response, error} = await client.GET("/api/products", {
            params: {
                query: {
                    page,
                    pageSize,
                }
            }
        });

        if (error) {
            throw new Error("상품 목록을 불러오는데 실패했습니다.");
        }

        if (!response?.data?.items) {
            throw new Error("상품 목록 데이터가 없습니다.");
        }

        return response.data.items.map((item: ProductDto) => ({
            id: item.id,
            createdDate: item.createdDate,
            modifiedDate: item.modifiedDate,
            productName: item.productName,
            price: item.price,
            imageUrl: item.imageUrl,
            category: item.category,
            description: item.description,
            orderable: item.orderable,
        }));
    }

    static async createProduct(data: {
        product: Omit<ProductCreateReqBody, 'file'>;
        file: File | null;
    }): Promise<Product> {
        const formData = new FormData();

        // JSON 데이터를 Blob으로 감싸서 FormData에 추가
        const jsonBlob = new Blob(
            [JSON.stringify(data.product)],
            {type: "application/json"}
        );
        formData.append("data", jsonBlob);

        // 파일이 있으면 추가
        if (data.file instanceof File) {
            formData.append("file", data.file);
        }

        const {data: response, error} = await client.POST("/api/adm/products", {
            body: formData as any,
        });

        if (error) {
            throw new Error("상품 생성에 실패했습니다.");
        }

        if (!response?.data) {
            throw new Error("상품 생성 응답 데이터가 없습니다.");
        }

        const createdProduct: Product = {
            id: response.data.id,
            createdDate: response.data.createdDate,
            modifiedDate: response.data.modifiedDate,
            productName: response.data.productName,
            price: response.data.price,
            imageUrl: response.data.imageUrl,
            category: response.data.category,
            description: response.data.description,
            orderable: response.data.orderable,
        };

        return createdProduct;
    }


    static async updateProduct(id: number, data: {
        product: Omit<ProductUpdateReqBody, 'file'>;
        file: File | null
    }): Promise<Product> {
        const formData = new FormData();
        formData.append("data", new Blob([JSON.stringify(data.product)], {type: "application/json"}));
        if (data.file) {
            formData.append("file", data.file);
        }

        const {data: response, error} = await client.PUT("/api/adm/products/{id}", {
            params: {
                path: {id},
            },
            body: formData as any, // FormData 타입이 openapi-typescript와 잘 맞지 않아 any로 캐스팅
            headers: {
                // "Content-Type": "multipart/form-data", // FormData 사용 시 자동으로 설정됨
            },
        });

        if (error) {
            throw new Error("상품 수정에 실패했습니다.");
        }

        if (!response?.data) {
            throw new Error("상품 수정 응답 데이터가 없습니다.");
        }

        const updatedProduct: Product = {
            id: response.data.id,
            createdDate: response.data.createdDate,
            modifiedDate: response.data.modifiedDate,
            productName: response.data.productName,
            price: response.data.price,
            imageUrl: response.data.imageUrl,
            category: response.data.category,
            description: response.data.description,
            orderable: response.data.orderable,
        };

        return updatedProduct;
    }

    static async deleteProduct(id: number): Promise<void> {
        const {error} = await client.DELETE("/api/adm/products/{id}", {
            params: {
                path: {id},
            },
        });

        if (error) {
            throw new Error("상품 삭제에 실패했습니다.");
        }
    }
}
