package com.back.domain.product.controller;

import com.back.domain.product.dto.PageDto;
import com.back.domain.product.dto.ProductDto;
import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Tag(name = "ProductController", description = "로그인 없이 상품목록, 상품상세 볼수있는 api")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService productService;

    @Operation(
            summary = "상품 목록 조회",
            description = "페이징 처리"
    )
    @GetMapping("/products")
    @Transactional(readOnly = true)
    public RsData<PageDto> getItems(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int pageSize
    ) {

        Page<Product> productPage = productService.getItems(page, pageSize);


        if (productPage.isEmpty()) { //목록이없더라도, 200 빈데이터 반환
            PageDto emptyPageDto = new PageDto(productPage);
            return RsData.successOf(emptyPageDto);
        }

        PageDto pageDto = new PageDto(productPage);
        return RsData.successOf(pageDto);
    }

    @Operation(
            summary = "상품 단건 조회",
            description = "상품 ID기반 상품의 상세 정보 조회"
    )
    @GetMapping("/products/{id}")
    @Transactional(readOnly = true)
    public RsData<ProductDto> getItem(@PathVariable long id) {

        Product product = productService.getItem(id).orElseThrow(
                () -> new ServiceException(404, "없는 상품입니다.")
        );

        return new RsData<>(
                200,
                "%d번 상품을 조회하였습니다.".formatted(id),
                new ProductDto(product)
        );
    }


}
