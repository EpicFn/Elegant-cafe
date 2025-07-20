package com.back.domain.product.controller;

import com.back.domain.product.entity.Product;
import com.back.domain.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AdmProductControllerTest {

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

    private ResultActions writeRequest(String productName, int price, String imageUrl,
                                       String category, String description, boolean orderable) throws Exception {
        String json = """
                {
                    "productName": "%s",
                    "price": %d,
                    "category": "%s",
                    "description": "%s",
                    "orderable": %b
                }
                """.formatted(productName, price, category, description, orderable);

        MockMultipartFile data = new MockMultipartFile(
                "data", "data.json", "application/json", json.getBytes(StandardCharsets.UTF_8)
        );

        // file은 선택사항 (null 가능)
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", "dummy-image".getBytes()
        );


        return mvc.
                perform(
                        multipart("/api/adm/products")
                                .file(data)
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                ).andDo(print());
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("상품 생성")
    void create1() throws Exception {

        String productName = "새로운 상품이름";
        int price = 10000;
        String imageUrl = "123";
        String category = "아이스";
        String description = "아이스 아메리카노";
        boolean orderable = true;

        ResultActions resultActions = writeRequest(productName, price, imageUrl, category, description, orderable);

        //상품의 제일 마지막 찾는 함수
        Product product = productService.getLatestItem().get();

        resultActions
                .andExpect(status().isCreated())
                .andExpect(handler().handlerType(AdmProductController.class))
                .andExpect(handler().methodName("createWithImage"))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("%d번 상품이 생성되었습니다.".formatted(product.getId())));

        checkProduct(resultActions, product);

    }

    private ResultActions modifyRequest(long productId,
                                        String productName,
                                        int price,
                                        String category,
                                        String description,
                                        boolean orderable,
                                        MockMultipartFile imageFile) throws Exception {
        // JSON 문자열 생성
        String jsonData = """
        {
            "productName": "%s",
            "price": %d,
            "category": "%s",
            "description": "%s",
            "orderable": %b
        }
        """.formatted(productName, price, category, description, orderable);

        // JSON을 data 파트로 넣음
        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "data.json", "application/json", jsonData.getBytes(StandardCharsets.UTF_8)
        );

        // file 파트는 이미지를 넣거나 null 가능
        return mvc.perform(multipart("/api/adm/products/%d".formatted(productId))
                        .file(dataPart)
                        .file(imageFile) // imageFile 이름은 반드시 "file"이어야 함
                        .with(request -> {
                            request.setMethod("PUT"); // PUT으로 강제
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andDo(print());
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("상품 수정")
    void modify1() throws Exception {

        long productId = 1;
        String productName = "수정된 이름";
        int price = 1000;
        String category = "수정된 카테고리";
        String description = "수정된 설명";
        boolean orderable = true;

        MockMultipartFile imageFile = new MockMultipartFile(
                "file", "new-image.jpg", "image/jpeg", "image-data-here".getBytes()
        );

        ResultActions resultActions = modifyRequest(productId, productName, price,
                category, description, orderable, imageFile);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(AdmProductController.class))
                .andExpect(handler().methodName("modifywithImage"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%d번 상품이 수정되었습니다.".formatted(productId)));

        Product product = productService.getItem(productId).get();
        checkProduct(resultActions, product);

    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("상품 수정2 (값 안넣을때)")
    void modify2() throws Exception {

        long productId = 1;
        String productName = "";
        int price = 100;
        String category = "";
        String description = "";
        boolean orderable = true;

        MockMultipartFile imageFile = new MockMultipartFile(
                "file", "new-image.jpg", "image/jpeg", "image-data-here".getBytes()
        );

        ResultActions resultActions = modifyRequest(productId, productName, price,
                category, description, orderable,imageFile);

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(handler().handlerType(AdmProductController.class))
                .andExpect(handler().methodName("modifywithImage"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("""
                        category-NotBlank-must not be blank
                        description-NotBlank-must not be blank
                        productName-NotBlank-must not be blank
                        """.trim().stripIndent())); //ServiceException에서 필드명을 알파벳 순으로 정렬함, 필드명-코드-메세지 형태

    }

    private ResultActions deleteRequest(long productId) throws Exception {
        return mvc
                .perform(
                        delete("/api/adm/products/%d".formatted(productId))
                )
                .andDo(print());
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("상품 삭제")
    void delete1() throws Exception {

        long productId = 1;

        ResultActions resultActions = deleteRequest(productId);

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(AdmProductController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%d번 상품이 삭제되었습니다.".formatted(productId)));

    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("상품 삭제 - (존재하지 않는)")
    void delete2() throws Exception {
        long nonExistentProductId = 99999;

        ResultActions resultActions = deleteRequest(nonExistentProductId);

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다."));
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("주문 가능 불가능")
    void order1() throws Exception {
        long productId = 1;

        ResultActions resultActions = mvc
                .perform(
                        put("/api/adm/products/%d/orderable".formatted(productId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "orderable" : false
                                        }
                                        """)

                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(AdmProductController.class))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(handler().methodName("isOrderable"))
                .andExpect(jsonPath("$.message").value("%d번 상품이 주문 불가능하게 변경되었습니다.".formatted(productId)));
    }

}
