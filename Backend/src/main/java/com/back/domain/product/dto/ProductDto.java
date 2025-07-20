package com.back.domain.product.dto;

import com.back.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProductDto {
    private long id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String productName;
    private int price;
    private String imageUrl;
    private String category;
    private String description;
    private boolean orderable;

    public ProductDto(Product product) {
        this.id =product.getId();
        this.createdDate = product.getCreatedDate();
        this.modifiedDate = product.getModifiedDate();
        this.productName=product.getProductName();
        this.price=product.getPrice();
        this.imageUrl=product.getImageUrl();
        this.category=product.getCategory();
        this.description=product.getDescription();
        this.orderable=product.isOrderable();
    }
}
