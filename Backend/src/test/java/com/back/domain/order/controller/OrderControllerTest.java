package com.back.domain.order.controller;

import com.back.domain.order.entity.Order;
import com.back.domain.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrderService orderService;

    @Test
    @WithUserDetails("user1@gmail.com")
    @DisplayName("1. 주문 생성")
    void t1() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerAddress": "서울역",
                                    "orderItems": [
                                      { "productId": 1, "count": 2 },
                                      { "productId": 2, "count": 1 }
                                    ]
                                }
                                """))
                .andDo(print());

        Order order = orderService.findLatest().orElseThrow();

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("createOrder"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("%d번 주문이 생성되었습니다.".formatted(order.getId())))
                .andExpect(jsonPath("$.data.id").value(order.getId()))
                .andExpect(jsonPath("$.data.customerEmail").value(order.getCustomer().getEmail()))
                .andExpect(jsonPath("$.data.customerAddress").value(order.getCustomerAddress()))
                .andExpect(jsonPath("$.data.state").value(order.getStatus().name()));
    }

    @Test
    @WithUserDetails("user1@gmail.com")
    @DisplayName("2. 주문 생성 실패 - 없는 상품")
    void t2() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerAddress": "서울역",
                                    "orderItems": [
                                      { "productId": 999, "count": 2 },
                                      { "productId": 2000, "count": 1 }
                                    ]
                                }
                                """))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("createOrder"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다."));
    }

    @Test
    @WithUserDetails("user1@gmail.com")
    @DisplayName("3. 배송지 변경")
    void t3() throws Exception {
        long targetOrder = 1L;
        String newAddress = "미국";

        ResultActions resultActions = mockMvc
                .perform(put("/api/orders/" + targetOrder + "/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "newAddress": "%s"
                                }
                                """.formatted(newAddress)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("updateOrderAddress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s번 주문 주소가 변경되었습니다.".formatted(targetOrder)))
                .andExpect(jsonPath("$.data").value("%s".formatted(newAddress)));
    }

    @Test
    @WithUserDetails("user2@gmail.com")
    @DisplayName("4. 배송지 변경 - 권한 없음")
    void t4() throws Exception {
        long targetOrder = 1L;

        ResultActions resultActions = mockMvc
                .perform(put("/api/orders/" + targetOrder + "/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "newAddress": "미국"
                                }
                                """))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("updateOrderAddress"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("%d번 주문 주소 변경 권한이 없습니다.".formatted(targetOrder)));
    }

    @Test
    @WithUserDetails("user1@gmail.com")
    @DisplayName("5. 배송지 변경 - 빈 주소")
    void t5() throws Exception {
        long targetOrder = 1L;
        String newAddress = "";

        ResultActions resultActions = mockMvc
                .perform(put("/api/orders/" + targetOrder + "/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "newAddress": "%s"
                                }
                                """.formatted(newAddress)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("updateOrderAddress"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @WithUserDetails("user1@gmail.com")
    @DisplayName("6. 배송지 변경 - 없는 주문 ID")
    void t6() throws Exception {
        long notExistsOrderId = 9999L;
        String newAddress = "목성";

        ResultActions resultActions = mockMvc
                .perform(put("/api/orders/" + notExistsOrderId + "/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "newAddress": "%s"
                                }
                                """.formatted(newAddress)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("updateOrderAddress"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("해당 주문이 존재하지 않습니다."));
    }

    @Test
    @WithUserDetails("user1@gmail.com")
    @DisplayName("7. 배송지 변경 - 이미 취소된 주문")
    void t7() throws Exception {
        long targetOrder = 3L;
        String newAddress = "토성";

        ResultActions resultActions = mockMvc
                .perform(put("/api/orders/" + targetOrder + "/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "newAddress": "%s"
                                }
                                """.formatted(newAddress)))
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("updateOrderAddress"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("이미 취소된 주문입니다."));
    }

    @Test
    @WithUserDetails("user1@gmail.com")
    @DisplayName("8. 주문 취소")
    void t8() throws Exception {
        long targetOrder = 1L;

        ResultActions resultActions = mockMvc
                .perform(delete("/api/orders/" + targetOrder)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("cancelOrder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s번 주문이 취소되었습니다.".formatted(targetOrder)))
                .andExpect(jsonPath("$.data.state").value("CANCELED"));
    }

    @Test
    @WithUserDetails("user2@gmail.com")
    @DisplayName("9. 주문 취소 - 권한 없음")
    void t9() throws Exception {
        long targetOrder = 1L;

        ResultActions resultActions = mockMvc
                .perform(delete("/api/orders/" + targetOrder)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("cancelOrder"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("%d번 주문 취소 권한이 없습니다.".formatted(targetOrder)));
    }

    @Test
    @WithUserDetails("user1@gmail.com")
    @DisplayName("10. 주문 취소 - 없는 주문 ID")
    void t10() throws Exception {
        long targetOrder = 999L;

        ResultActions resultActions = mockMvc
                .perform(delete("/api/orders/" + targetOrder)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("cancelOrder"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("해당 주문이 존재하지 않습니다."));
    }

    @Test
    @WithUserDetails("user1@gmail.com")
    @DisplayName("11. 주문 취소 - 이미 취소된 주문")
    void t11() throws Exception {
        long targetOrder = 3L;

        ResultActions resultActions = mockMvc
                .perform(delete("/api/orders/" + targetOrder)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("cancelOrder"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("이미 취소된 주문입니다."));
    }
}