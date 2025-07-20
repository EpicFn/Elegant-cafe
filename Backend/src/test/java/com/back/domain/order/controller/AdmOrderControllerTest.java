package com.back.domain.order.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.order.dto.OrderItemParam;
import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderStatus;
import com.back.domain.order.service.OrderService;
import com.back.global.exception.ServiceException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AdmOrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("주문 목록 조회 - 관리자")
    @WithUserDetails("admin@gmail.com")
    void t1() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(get("/api/adm/orders"))
                .andDo(print());

        List<Order> orders = orderService.getAllOrders();

        resultActions
                .andExpect(handler().handlerType(AdmOrderController.class))
                .andExpect(handler().methodName("getOrders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("주문 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(orders.size()));

        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            resultActions
                    .andExpect(jsonPath("$.data[%d].id".formatted(i)).value(order.getId()))
                    .andExpect(jsonPath("$.data[%d].customerEmail".formatted(i)).value(order.getCustomer().getEmail()))
                    .andExpect(jsonPath("$.data[0].createdDate").value(Matchers.startsWith(order.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))))
                    .andExpect(jsonPath("$.data[%d].customerName".formatted(i)).value(order.getCustomer().getName()))
                    .andExpect(jsonPath("$.data[%d].state".formatted(i)).value(order.getStatus().name()))
                    .andExpect(jsonPath("$.data[%d].customerAddress".formatted(i)).value(order.getCustomerAddress()))
                    .andExpect(jsonPath("$.data[%d].orderItems", i).isArray())
                    .andExpect(jsonPath("$.data[%d].orderItems.length()".formatted(i)).value(order.getOrderItems().size()));
        }
    }

    @Test
    @DisplayName("주문 목록 조회 - 권한 없음")
    @WithUserDetails("user2@gmail.com")
    void t2() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/adm/orders")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("주문 상세 조회 - 관리자")
    @WithUserDetails("admin@gmail.com")
    void t3() throws Exception {

        List<Order> orders = orderService.getAllOrders();
        Order targetOrder = orders.get(0);


        ResultActions resultActions = mockMvc
                .perform(get("/api/adm/orders/" + targetOrder.getId() + "/detail"))
                .andDo(print());


        resultActions
                .andExpect(handler().handlerType(AdmOrderController.class))
                .andExpect(handler().methodName("getOrderDetail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("주문 상세 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.id").value(targetOrder.getId()))
                .andExpect(jsonPath("$.data.customerEmail").value(targetOrder.getCustomer().getEmail()))
                .andExpect(jsonPath("$.data.customerName").value(targetOrder.getCustomer().getName()))
                .andExpect(jsonPath("$.data.customerAddress").value(targetOrder.getCustomerAddress()))
                .andExpect(jsonPath("$.data.state").value(targetOrder.getStatus().name()))
                .andExpect(jsonPath("$.data.orderItems").isArray());
    }

    @Test
    @DisplayName("주문 상세 조회 - 권한 없음")
    @WithUserDetails("user2@gmail.com")
    void t4() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(
                        get("/api/adm/orders/1/detail")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("주문 상태 변경 - 주문완료")
    @WithUserDetails("admin@gmail.com")
    void t5() throws Exception {
        // given : 주문 상태를 변경할 주문을 준비
        Member user = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));
        Order targetOrder = orderService.createOrder(
                user,
                "서울시 강남구 역삼동",
                List.of(new OrderItemParam(1L, 2)
                ) // 상품 ID 1번, 수량 2개
        );
        targetOrder.changeStatus(OrderStatus.CANCELED); // 초기 상태를 CANACELED으로 설정

        // when : 주문 상태를 변경하는 요청 전송
        ResultActions resultActions = mockMvc
                .perform(
                        put("/api/adm/orders/" + targetOrder.getId() + "/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "status": "ORDERED"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // then : 응답 검증
        resultActions
                .andExpect(handler().handlerType(AdmOrderController.class))
                .andExpect(handler().methodName("updateOrderStatus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s번 주문의 상태가 %s로 변경되었습니다.".formatted(targetOrder.getId(), "주문완료")))
                .andExpect(jsonPath("$.data.id").value(targetOrder.getId()))
                .andExpect(jsonPath("$.data.status").value("주문완료"));

        // 추가 검증: 주문 상태가 실제로 변경되었는지 확인
        assertThat(targetOrder.getStatus()).isEqualTo(OrderStatus.ORDERED);
    }

    @Test
    @DisplayName("주문 상태 변경 - 배송중")
    @WithUserDetails("admin@gmail.com")
    void t6() throws Exception {
        // given : 주문 상태를 변경할 주문을 준비
        Member user = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));
        Order targetOrder = orderService.createOrder(
                user,
                "서울시 강남구 역삼동",
                List.of(new OrderItemParam(1L, 2)
                ) // 상품 ID 1번, 수량 2개
        );

        // when : 주문 상태를 변경하는 요청 전송
        ResultActions resultActions = mockMvc
                .perform(
                        put("/api/adm/orders/" + targetOrder.getId() + "/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "status": "SHIPPING"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // then : 응답 검증
        resultActions
                .andExpect(handler().handlerType(AdmOrderController.class))
                .andExpect(handler().methodName("updateOrderStatus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s번 주문의 상태가 %s로 변경되었습니다.".formatted(targetOrder.getId(), "배송중")))
                .andExpect(jsonPath("$.data.id").value(targetOrder.getId()))
                .andExpect(jsonPath("$.data.status").value("배송중"));

        // 추가 검증: 주문 상태가 실제로 변경되었는지 확인
        assertThat(targetOrder.getStatus()).isEqualTo(OrderStatus.SHIPPING);
    }

    @Test
    @DisplayName("주문 상태 변경 - 배송완료")
    @WithUserDetails("admin@gmail.com")
    void t7() throws Exception {
        // given : 주문 상태를 변경할 주문을 준비
        Member user = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));
        Order targetOrder = orderService.createOrder(
                user,
                "서울시 강남구 역삼동",
                List.of(new OrderItemParam(1L, 2)
                ) // 상품 ID 1번, 수량 2개
        );

        // when : 주문 상태를 변경하는 요청 전송
        ResultActions resultActions = mockMvc
                .perform(
                        put("/api/adm/orders/" + targetOrder.getId() + "/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "status": "COMPLETED"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // then : 응답 검증
        resultActions
                .andExpect(handler().handlerType(AdmOrderController.class))
                .andExpect(handler().methodName("updateOrderStatus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s번 주문의 상태가 %s로 변경되었습니다.".formatted(targetOrder.getId(), "배송완료")))
                .andExpect(jsonPath("$.data.id").value(targetOrder.getId()))
                .andExpect(jsonPath("$.data.status").value("배송완료"));

        // 추가 검증: 주문 상태가 실제로 변경되었는지 확인
        assertThat(targetOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("주문 상태 변경 - 주문취소")
    @WithUserDetails("admin@gmail.com")
    void t8() throws Exception {
        // given : 주문 상태를 변경할 주문을 준비
        Member user = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));
        Order targetOrder = orderService.createOrder(
                user,
                "서울시 강남구 역삼동",
                List.of(new OrderItemParam(1L, 2)
                ) // 상품 ID 1번, 수량 2개
        );

        // when : 주문 상태를 변경하는 요청 전송
        ResultActions resultActions = mockMvc
                .perform(
                        put("/api/adm/orders/" + targetOrder.getId() + "/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "status": "CANCELED"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // then : 응답 검증
        resultActions
                .andExpect(handler().handlerType(AdmOrderController.class))
                .andExpect(handler().methodName("updateOrderStatus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s번 주문의 상태가 %s로 변경되었습니다.".formatted(targetOrder.getId(), "주문취소")))
                .andExpect(jsonPath("$.data.id").value(targetOrder.getId()))
                .andExpect(jsonPath("$.data.status").value("주문취소"));

        // 추가 검증: 주문 상태가 실제로 변경되었는지 확인
        assertThat(targetOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("주문 상태 변경 - 잘못된 상태")
    @WithUserDetails("admin@gmail.com")
    void t9() throws Exception {
        // given : 주문 상태를 변경할 주문을 준비
        Member user = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));
        Order targetOrder = orderService.createOrder(
                user,
                "서울시 강남구 역삼동",
                List.of(new OrderItemParam(1L, 2)
                ) // 상품 ID 1번, 수량 2개
        );

        // when : 잘못된 주문 상태를 변경하는 요청 전송
        ResultActions resultActions = mockMvc
                .perform(
                        put("/api/adm/orders/" + targetOrder.getId() + "/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "status": "INVALID_STATUS"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // then : 응답 검증
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("유효하지 않은 주문 상태입니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
