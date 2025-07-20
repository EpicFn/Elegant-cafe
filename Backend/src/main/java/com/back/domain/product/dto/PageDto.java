package com.back.domain.product.dto;

import com.back.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageDto {

    List<ProductDto> items;
    int totalPages;
    int totalItems;
    int currentPageNo;
    int pageSize;

    public PageDto(Page<Product> productPage) {

        this.items = productPage.getContent()
                .stream()
                .map(ProductDto::new)
                .toList();
        this.totalPages = productPage.getTotalPages();
        this.totalItems = (int)productPage.getTotalElements();
        this.currentPageNo = productPage.getNumber()+1; // jpa는 페이지 번호 0부터 시작
        this.pageSize = productPage.getSize();
    }

}
