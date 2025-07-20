package com.back.global.initData;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.order.dto.OrderItemParam;
import com.back.domain.order.service.OrderService;
import com.back.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 테스트 환경의 초기 데이터 설정
 */
@Configuration
@Profile("test")
@RequiredArgsConstructor
public class TestInitData {
    @Autowired
    @Lazy
    private TestInitData self;
    private final MemberService memberService;
    private final ProductService productService;
    private final OrderService orderService;

    @Bean
    ApplicationRunner testInitDataApplicationRunner() {
        return args -> {
            self.work1();
            self.work2();
            self.work3();
        };
    }

    // 유저 데이터 삽입
    @Transactional
    public void work1() {
        if (memberService.count() > 0) return;
        Member memberSystem = memberService.joinAdmin("system@gmail.com", "12345678", "시스템");
        memberSystem.modifyApiKey(memberSystem.getEmail());

        Member memberAdmin = memberService.joinAdmin("admin@gmail.com", "12345678", "관리자");
        memberAdmin.modifyApiKey(memberAdmin.getEmail());

        Member user1 = memberService.join("user1@gmail.com", "12345678", "유저1");
        user1.modifyApiKey(user1.getEmail());

        Member user2 = memberService.join("user2@gmail.com", "12345678", "유저2");
        user2.modifyApiKey(user2.getEmail());

        Member user3 = memberService.join("user3@gmail.com", "12345678", "유저3");
        user3.modifyApiKey(user3.getEmail());
    }

    // 상품 데이터 삽입
    @Transactional
    public void work2() {
        productService.create("아메리카노(Ice)", 3500, null, "아이스 커피", "샷 + 물", true);
        productService.create("카페라떼(Hot)", 4000, null, "핫 커피", "샷 + 우유", true);
        productService.create("카푸치노(Ice)", 4500, null, "아이스 커피", "샷 + 우유 + 거품", true);
    }

    // 주문 데이터 삽입
    @Transactional
    public void work3() {
        Member user1 = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new RuntimeException("user1 not found"));

        orderService.createOrder(
                user1,
                "서울시 강남구 테헤란로 123",
                List.of(
                        new OrderItemParam(1L, 2),
                        new OrderItemParam(2L, 1)
                )
        );
        orderService.createOrder(
                user1,
                "서울시 송파구",
                List.of(
                        new OrderItemParam(3L, 1),
                        new OrderItemParam(1L, 4)
                )
        );
        var canceledOrder = orderService.createOrder(
                user1,
                "서울시 은평구",
                List.of(
                        new OrderItemParam(1L, 1)
                )
        );
        orderService.cancelOrder(canceledOrder.getId(), user1);
    }
}