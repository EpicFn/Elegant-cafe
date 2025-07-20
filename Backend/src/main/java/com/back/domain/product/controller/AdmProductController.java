package com.back.domain.product.controller;

import com.back.domain.product.dto.ProductDto;
import com.back.domain.product.dto.ProductWithOrderable;
import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/adm")
@RequiredArgsConstructor
@Tag(name = "AdmProductController", description = "관리자 상품 생성,수정, 삭제")
@SecurityRequirement(name = "bearerAuth")
public class AdmProductController {

    private final ProductService productService;

    public record GCSReqBody(@NotBlank String productName,
                             @Positive int price,
                             @NotBlank String category,
                             @NotBlank String description,
                             boolean orderable) {
    }

    @Operation(
            summary = "상품 생성",
            description = "json형식 데이터 + file형식으로 상품을 생성합니다"
    )
    @PostMapping("/products")
    @Transactional
    public RsData<ProductDto> createWithImage(
            @RequestPart("data") @Valid GCSReqBody reqBody,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        Product product = productService.uploadObject(reqBody, file);

        return new RsData<>(
                201,
                "%d번 상품이 생성되었습니다.".formatted(product.getId()),
                new ProductDto(product)
        );
    }

    record ModifyReqBody(@NotBlank String productName,
                         @Positive int price,
                         @NotBlank String category,
                         @NotBlank String description,
                         boolean orderable) {

    }

    @Operation(
            summary = "상품 수정",
            description = """
                    수정할때도 json형식+file 형태의 값을 입력해야함
                    시나리오1: 이미지 수정 없음
                    시나리오2: 새 이미지 업로드
                    """

    )
    @PutMapping("/products/{id}")
    @Transactional
    public RsData<ProductDto> modifywithImage(
            @PathVariable long id,
            @RequestPart("data") @Valid ModifyReqBody reqBody,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        Product product = productService.getItem(id).orElseThrow(
                () -> new ServiceException(404, "존재하지 않는 상품입니다")
        );

        //여기서 이미지바꾸는 로직 넣고
        productService.modifyImage(product, file);

        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ" + product.getImageUrl());

        //나머지 정보 수정
        productService.modify(product, reqBody.productName(), reqBody.price(), product.getImageUrl(),
                reqBody.category(), reqBody.description(), reqBody.orderable());

        return new RsData<>(
                200,
                "%d번 상품이 수정되었습니다.".formatted(id),
                new ProductDto(product)
        );
    }

    @Operation(
            summary = "상품 삭제",
            description = "일단 상품 삭제"
    )
    @DeleteMapping("/products/{id}")
    @Transactional
    public RsData<Void> delete(@PathVariable long id) {

        Product product = productService.getItem(id).orElseThrow(
                () -> new ServiceException(404, "존재하지 않는 상품입니다.")
        );

        productService.delete(product);

        return new RsData<>(
                200,
                "%d번 상품이 삭제되었습니다.".formatted(id),
                null
        );


    }

    record OrderReqBody(boolean orderable) {
    }

    @Operation(
            summary = "주문 가능 불가능 설정",
            description = "상품의 주문 가능 여부를 true 또는 false로 변경, 데이터는 orderable만 보냅니다"
    )
    @Transactional
    @PutMapping("/products/{id}/orderable")
    public RsData<ProductWithOrderable> isOrderable(@PathVariable long id,
                                                    @RequestBody OrderReqBody reqBody) {

        Product product = productService.getItem(id).orElseThrow(
                () -> new ServiceException(404, "존재하지 않는 상품입니다.")
        );

        productService.updateOrderable(product, reqBody.orderable());

        String message = String.format(
                "%d번 상품이 주문 %s하게 변경되었습니다.",
                id,
                reqBody.orderable() ? "가능" : "불가능"
        );

        return new RsData<>(
                200,
                message,
                new ProductWithOrderable(product.isOrderable())
        );
    }

}
