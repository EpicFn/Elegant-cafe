export interface Product {
    id: number;
    createdDate: string;
    modifiedDate: string;
    productName: string;
    price: number;
    imageUrl: string | null;
    category: string;
    description: string;
    orderable: boolean;
}