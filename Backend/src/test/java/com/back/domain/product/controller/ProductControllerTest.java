package com.back.domain.product.controller;

import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ProductControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProductService productService;


    private void checkProduct(ResultActions resultActions, Product product) throws Exception {
        resultActions
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(product.getId()))
                .andExpect(jsonPath("$.data.productName").value(product.getProductName()))
                .andExpect(jsonPath("$.data.price").value(product.getPrice()))
                .andExpect(jsonPath("$.data.imageUrl").value(product.getImageUrl()))
                .andExpect(jsonPath("$.data.category").value(product.getCategory()))
                .andExpect(jsonPath("$.data.description").value(product.getDescription()))
                .andExpect(jsonPath("$.data.orderable").value(product.isOrderable()))
                .andExpect(jsonPath("$.data.createdDate").value(matchesPattern(product.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.data.modifiedDate").value(matchesPattern(product.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
    }


    private void checkProducts(List<Product> products, ResultActions resultActions) throws Exception {

        for (int i = 0; i < products.size(); i++) {

            Product product = products.get(i);

            resultActions
                    .andExpect(jsonPath("$.data.items[%d]".formatted(i)).exists())
                    .andExpect(jsonPath("$.data.items[%d].id".formatted(i)).value(product.getId()))
                    .andExpect(jsonPath("$.data.items[%d].productName".formatted(i)).value(product.getProductName()))
                    .andExpect(jsonPath("$.data.items[%d].price".formatted(i)).value(product.getPrice()))
                    .andExpect(jsonPath("$.data.items[%d].imageUrl".formatted(i)).value(product.getImageUrl()))
                    .andExpect(jsonPath("$.data.items[%d].category".formatted(i)).value(product.getCategory()))
                    .andExpect(jsonPath("$.data.items[%d].description".formatted(i)).value(product.getDescription()))
                    .andExpect(jsonPath("$.data.items[%d].orderable".formatted(i)).value(product.isOrderable()))
                    .andExpect(jsonPath("$.data.items[%d].createdDate".formatted(i)).value(matchesPattern(product.getCreatedDate().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.data.items[%d].modifiedDate".formatted(i)).value(matchesPattern(product.getModifiedDate().toString().replaceAll("0+$", "") + ".*")));
        }


    }

    @Test
    @DisplayName("상품 전체 조회 (페이징 없음)")
    void items1() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        get("/api/products")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ProductController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.items.length()").value(3)) // 한페이지당 보여줄 상품 개수
                .andExpect(jsonPath("$.data.currentPageNo").isNumber()) // 현재 페이지
                .andExpect(jsonPath("$.data.totalPages").isNumber()); // 전체 페이지 개수


        Page<Product> productPage = productService.getItems(1, 5);
        List<Product> products = productPage.getContent();
        checkProducts(products, resultActions);

    }

    @Test
    @DisplayName("상품 전체 조회 (페이징이 되어여 함.")
    void items2() throws Exception {

        int page = 1;
        int pageSize = 5;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/products?page=%d&pageSize=%d".formatted(page, pageSize))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ProductController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.items.length()").value(3)) // 한페이지당 보여줄 글 개수
                .andExpect(jsonPath("$.data.currentPageNo").isNumber()) // 현재 페이지
                .andExpect(jsonPath("$.data.totalPages").isNumber()); // 전체 페이지 개수


        Page<Product> productPage = productService.getItems(page, pageSize);
        List<Product> products = productPage.getContent();
        checkProducts(products, resultActions);


    }

    @Test
    @DisplayName("상품 전체 조회 - 잘못된 페이지 파라미터(@Min/@Max 위반)")
    void items3() throws Exception {
        // 잘못된 page, pageSize 조합들
        String[] invalidRequests = {
                "/api/products?page=0&pageSize=5",     // page < 1
                "/api/products?page=-1&pageSize=5",    // page 음수
                "/api/products?page=1&pageSize=0",     // pageSize < 1
                "/api/products?page=1&pageSize=101"    // pageSize > 100
        };

        for (String url : invalidRequests) {
            ResultActions resultActions = mvc
                    .perform(get(url))
                    .andDo(print());

            resultActions
                    .andExpect(status().isBadRequest()) // 유효성 검사 실패 → 400 Bad Request
                    .andExpect(jsonPath("$.code").value(400))//    // ConstraintViolationException: 제약 조건(@NotNull, @Size 등)을 어겼을 때 발생하는 예외
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    //테스트 데잍터 생성후 , auto increment떄문에 롤백되어도 id가 1로 시작안함, 그대로 유지
    @Test
    @DisplayName("상품 단건 조회 1 - 원하는 {id}")
    void item1() throws Exception {

        long productId = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/products/%d".formatted(productId))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(ProductController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%d번 상품을 조회하였습니다.".formatted(productId)));

        Product product = productService.getItem(productId).get();

        checkProduct(resultActions, product);

    }
}
