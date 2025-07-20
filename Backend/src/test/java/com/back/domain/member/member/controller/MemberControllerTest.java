package com.back.domain.member.member.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.order.dto.OrderItemParam;
import com.back.domain.order.entity.Order;
import com.back.domain.order.service.OrderService;
import com.back.global.exception.ServiceException;
import com.back.standard.util.Ut;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {
    @Autowired
    private MemberService memberService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원 가입")
    void join() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/members/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "testuser@gmail.com",
                                            "password": "testpassword",
                                            "name": "테스트 유저"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        Member member = memberService.findByEmail("testuser@gmail.com").orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("join"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getName())))
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.name").value(member.getName()));
    }

    @Test
    @DisplayName("로그인")
    void login() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/members/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "system@gmail.com",
                                            "password": "12345678"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        Member member = memberService.findByEmail("system@gmail.com").orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s님 환영합니다.".formatted(member.getName())))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.member.id").value(member.getId()))
                .andExpect(jsonPath("$.data.member.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.member.name").value(member.getName()))
                .andExpect(jsonPath("$.data.member.isAdmin").value(member.isAdmin()))
                .andExpect(jsonPath("$.data.apiKey").value(member.getApiKey()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        resultActions.andExpect(
                result -> {
                    Cookie apiKeyCookie = result.getResponse().getCookie("apiKey");
                    assertThat(apiKeyCookie.getValue()).isEqualTo(member.getApiKey());
                    assertThat(apiKeyCookie.getPath()).isEqualTo("/");
                    assertThat(apiKeyCookie.getAttribute("HttpOnly")).isEqualTo("true");

                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");
                    assertThat(accessTokenCookie.getValue()).isNotBlank();
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.getAttribute("HttpOnly")).isEqualTo("true");
                }
        );
    }

    @Test
    @DisplayName("로그인 - 잘못된 비밀번호")
    void login_wrongPassword() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/members/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "system@gmail.com",
                                            "password": "wrong_password"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("로그인 - 잘못된 이메일")
    void login_wrongEmail() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/members/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "wrong_system@gmail.com",
                                            "password": "wrong_password"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("존재하지 않는 이메일입니다."));
    }

    @Test
    @DisplayName("로그 아웃")
    void logout() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/members/logout")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("로그아웃 됐습니다."))
                .andExpect(result -> {
                    Cookie apiKeyCookie = result.getResponse().getCookie("apiKey");
                    assertThat(apiKeyCookie.getValue()).isEmpty();
                    assertThat(apiKeyCookie.getMaxAge()).isEqualTo(0);
                    assertThat(apiKeyCookie.getPath()).isEqualTo("/");
                    assertThat(apiKeyCookie.isHttpOnly()).isTrue();

                    Cookie accessTokenCookie = result.getResponse().getCookie("accessToken");
                    assertThat(accessTokenCookie.getValue()).isEmpty();
                    assertThat(accessTokenCookie.getMaxAge()).isEqualTo(0);
                    assertThat(accessTokenCookie.getPath()).isEqualTo("/");
                    assertThat(accessTokenCookie.isHttpOnly()).isTrue();
                });
    }

    @Test
    @DisplayName("회원 탈퇴")
    @WithUserDetails("user1@gmail.com")
    void withdraw() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/members/withdraw")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("withdraw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원 탈퇴가 완료되었습니다."));

        // 회원 정보가 삭제되었는지 확인
        assertThat(memberService.findByEmail("user1@gmail.com"))
                .isEmpty();
    }

    @Test
    @DisplayName("회원 탈퇴 - 어드민 계정으로 시도")
    @WithUserDetails("system@gmail.com")
    void withdraw_admin() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/members/withdraw")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("withdraw"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("관리자는 탈퇴할 수 없습니다."));

    }

    @Test
    @DisplayName("회원 탈퇴 - 로그인하지 않은 경우")
    void withdraw_notLoggedIn() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/members/withdraw")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."));
    }

    @Test
    @DisplayName("회원 정보 조회")
    @WithUserDetails("user1@gmail.com")
    void getMemberInfo() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/members/info")
                )
                .andDo(print());

        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("getMemberInfo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원 정보가 조회됐습니다."))
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.name").value(member.getName()))
                .andExpect(jsonPath("$.data.isAdmin").value(member.isAdmin()));
    }

    @Test
    @DisplayName("회원 정보 수정")
    @WithUserDetails("user1@gmail.com")
    void updateMemberInfo() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put("/api/members/info")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "modifiedUser1@gmail.com",
                                            "name": "수정된 이름",
                                            "password": "newPassword"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        Member member = memberService.findByEmail("modifiedUser1@gmail.com")
                .orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("updateMemberInfo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원 정보가 수정됐습니다."))
                .andExpect(jsonPath("$.data.id").value(member.getId()))
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.name").value(member.getName()))
                .andExpect(jsonPath("$.data.isAdmin").value(member.isAdmin()));

        assertThat(member.getName()).isEqualTo("수정된 이름");
        assertThat(passwordEncoder.matches("newPassword", member.getPassword())).isTrue();
    }

    @Test
    @DisplayName("회원 정보 수정 - 잘못된 입력")
    @WithUserDetails("user1@gmail.com")
    void updateMemberInfo_wrongInput() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put("/api/members/info")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "",
                                            "name": "수정된 이름",
                                            "password": "newPassword"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());


        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("updateMemberInfo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("email-NotBlank-must not be blank"));
    }

    @Test
    @DisplayName("회원 주문 내역 전체 조회")
    @WithUserDetails("user2@gmail.com")
    void getMemberOrders() throws Exception {
        Member member = memberService.findByEmail("user2@gmail.com")
                .orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));

        Order order1 = orderService.createOrder(
                member,
                "서울시 강남구 테헤란로 123",
                List.of(
                        new OrderItemParam(1L, 2), // 아메리카노(Ice)
                        new OrderItemParam(2L, 1) // 카페라떼(Hot)
                )
        );

        Order order2 = orderService.createOrder(
                member,
                "서울시 강남구 역삼로 456",
                List.of(
                        new OrderItemParam(3L, 1) // 카푸치노(Ice)
                )
        );

        ResultActions resultActions = mvc
                .perform(
                        get("/api/members/orders")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("getMemberOrders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원 주문 내역이 조회됐습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].orderId").value(order2.getId()))
                .andExpect(jsonPath("$.data[0].orderDate").value(order2.getCreatedDate().toString()))
                .andExpect(jsonPath("$.data[0].status").value(order2.getStatus().name()))
                .andExpect(jsonPath("$.data[0].customerAddress").value(order2.getCustomerAddress()))
                .andExpect(jsonPath("$.data[0].orderItems").isArray())
                .andExpect(jsonPath("$.data[0].orderItems.length()").value(order2.getOrderItems().size()))
                .andExpect(jsonPath("$.data[1].orderId").value(order1.getId()))
                .andExpect(jsonPath("$.data[1].orderDate").value(order1.getCreatedDate().toString()))
                .andExpect(jsonPath("$.data[1].status").value(order1.getStatus().name()))
                .andExpect(jsonPath("$.data[1].customerAddress").value(order1.getCustomerAddress()))
                .andExpect(jsonPath("$.data[1].orderItems").isArray())
                .andExpect(jsonPath("$.data[1].orderItems.length()").value(order1.getOrderItems().size()));

        // 주문 아이템 검증
        String json = resultActions.andReturn().getResponse().getContentAsString();

        JsonNode root = Ut.json.objectMapper.readTree(json);
        JsonNode dataArray = root.get("data");

        assertThat(dataArray).hasSize(2); // 주문 2건

        // 첫 번째 주문의 주문 아이템들
        JsonNode orderItems1 = dataArray.get(1).get("orderItems");
        assertThat(orderItems1).hasSize(order1.getOrderItems().size());
        assertThat(orderItems1.get(0).get("productName").asText()).isEqualTo("아메리카노(Ice)");
        assertThat(orderItems1.get(0).get("productId").asLong()).isEqualTo(1L);
        assertThat(orderItems1.get(0).get("count").asInt()).isEqualTo(2);
        assertThat(orderItems1.get(0).get("price").asInt()).isEqualTo(3500);

        assertThat(orderItems1.get(1).get("productName").asText()).isEqualTo("카페라떼(Hot)");
        assertThat(orderItems1.get(1).get("productId").asLong()).isEqualTo(2L);
        assertThat(orderItems1.get(1).get("count").asInt()).isEqualTo(1);
        assertThat(orderItems1.get(1).get("price").asInt()).isEqualTo(4000);

        // 두 번째 주문의 주문 아이템들
        JsonNode orderItems2 = dataArray.get(0).get("orderItems");
        assertThat(orderItems2).hasSize(order2.getOrderItems().size());
        assertThat(orderItems2.get(0).get("productName").asText()).isEqualTo("카푸치노(Ice)");
        assertThat(orderItems2.get(0).get("productId").asLong()).isEqualTo(3L);
        assertThat(orderItems2.get(0).get("count").asInt()).isEqualTo(1);
        assertThat(orderItems2.get(0).get("price").asInt()).isEqualTo(4500);
    }

    @Test
    @DisplayName("회원 특정 주문 내역 상세 조회")
    @WithUserDetails("user2@gmail.com")
    void getMemberOrderDetail() throws Exception {
        //Given
        Member member = memberService.findByEmail("user2@gmail.com")
                .orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));

        Order order1 = orderService.createOrder(
                member,
                "서울시 강남구 테헤란로 123",
                List.of(
                        new OrderItemParam(1L, 2), // 아메리카노(Ice)
                        new OrderItemParam(2L, 1) // 카페라떼(Hot)
                )
        );

        Order order2 = orderService.createOrder(
                member,
                "서울시 강남구 역삼로 456",
                List.of(
                        new OrderItemParam(3L, 1) // 카푸치노(Ice)
                )
        );

        //When
        ResultActions resultActions = mvc
                .perform(
                        get("/api/members/orders/{orderId}", order1.getId())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("getMemberOrderDetail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원 주문 상세 내역이 조회됐습니다."))
                .andExpect(jsonPath("$.data.orderId").value(order1.getId()))
                .andExpect(jsonPath("$.data.orderDate").value(order1.getCreatedDate().toString()))
                .andExpect(jsonPath("$.data.status").value(order1.getStatus().name()))
                .andExpect(jsonPath("$.data.customerAddress").value(order1.getCustomerAddress()))
                .andExpect(jsonPath("$.data.orderItems").isArray())
                .andExpect(jsonPath("$.data.orderItems.length()").value(order1.getOrderItems().size()));

        // 주문 아이템 검증
        String json = resultActions.andReturn().getResponse().getContentAsString();
        JsonNode root = Ut.json.objectMapper.readTree(json);
        JsonNode orderItems = root.get("data").get("orderItems");

        assertThat(orderItems).hasSize(order1.getOrderItems().size());
        assertThat(orderItems.get(0).get("productName").asText()).isEqualTo("아메리카노(Ice)");
        assertThat(orderItems.get(0).get("productId").asLong()).isEqualTo(1L);
        assertThat(orderItems.get(0).get("count").asInt()).isEqualTo(2);
        assertThat(orderItems.get(0).get("price").asInt()).isEqualTo(3500);
        assertThat(orderItems.get(1).get("productName").asText()).isEqualTo("카페라떼(Hot)");
        assertThat(orderItems.get(1).get("productId").asLong()).isEqualTo(2L);
        assertThat(orderItems.get(1).get("count").asInt()).isEqualTo(1);
        assertThat(orderItems.get(1).get("price").asInt()).isEqualTo(4000);
    }

    @Test
    @DisplayName("회원 특정 주문 내역 상세 조회 - 존재하지 않는 주문")
    @WithUserDetails("user2@gmail.com")
    void getMemberOrderDetail_notFound() throws Exception {
        //Given
        Long nonExistentOrderId = 999L;

        //When
        ResultActions resultActions = mvc
                .perform(
                        get("/api/members/orders/{orderId}", nonExistentOrderId)
                )
                .andDo(print());

        //Then
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("getMemberOrderDetail"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("해당 주문이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("회원 특정 주문 내역 상세 조회 - 다른 회원의 주문")
    @WithUserDetails("user2@gmail.com")
    void getMemberOrderDetail_otherMemberOrder() throws Exception {
        //Given
        Member otherMember = memberService.findByEmail("user3@gmail.com")
                .orElseThrow(() -> new ServiceException(404, "회원이 존재하지 않습니다."));
        Order otherMembersOrder = orderService.createOrder(
                otherMember,
                "서울시 강남구 테헤란로 123",
                List.of(
                        new OrderItemParam(1L, 2), // 아메리카노(Ice)
                        new OrderItemParam(2L, 1) // 카페라떼(Hot)
                )
        );

        //When
        ResultActions resultActions = mvc
                .perform(
                        get("/api/members/orders/{orderId}", otherMembersOrder.getId())
                )
                .andDo(print());

        //Then
        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("getMemberOrderDetail"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("해당 주문에 대한 권한이 없습니다."));
    }

    @Test
    @DisplayName("회원 패스워드 검증")
    @WithUserDetails("user1@gmail.com")
    void verifyPassword() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/members/verify-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "password": "12345678"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("verifyPassword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("비밀번호가 검증됐습니다."));
    }

    @Test
    @DisplayName("회원 패스워드 검증 - 잘못된 비밀번호")
    @WithUserDetails("user1@gmail.com")
    void verifyPassword_wrong() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/members/verify-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "password": "wrong_password"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(MemberController.class))
                .andExpect(handler().methodName("verifyPassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }
}