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
 * 개발 환경의 초기 데이터 설정
 */
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevInitData {
    @Autowired
    @Lazy
    private DevInitData self;
    private final MemberService memberService;
    private final ProductService productService;
    private final OrderService orderService;

    @Bean
    ApplicationRunner devInitDataApplicationRunner() {
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
        productService.create("아메리카노(Ice)", 3500, null, "아이스 커피", "시원하게 즐기는 기본 아이스 커피", true);
        productService.create("아메리카노(Hot)", 3500, null, "핫 커피", "진한 에스프레소에 뜨거운 물을 더한 클래식 커피", true);
        productService.create("카페라떼(Ice)", 4000, null, "아이스 커피", "에스프레소와 시원한 우유가 어우러진 부드러운 음료", true);
        productService.create("카페라떼(Hot)", 4000, null, "핫 커피", "따뜻한 우유와 에스프레소가 조화를 이루는 부드러운 라떼", true);
        productService.create("바닐라라떼(Ice)", 4500, null, "아이스 커피", "달콤한 바닐라 향이 더해진 시원한 라떼", true);
        productService.create("바닐라라떼(Hot)", 4500, null, "핫 커피", "따뜻하고 부드러운 바닐라 풍미의 라떼", true);
        productService.create("카푸치노(Ice)", 4500, null, "아이스 커피", "거품과 우유, 에스프레소가 어우러진 시원한 커피", true);
        productService.create("카푸치노(Hot)", 4500, null, "핫 커피", "풍부한 우유 거품 위에 시나몬 향이 감도는 커피", true);
        productService.create("돌체라떼(Ice)", 4800, null, "아이스 커피", "달콤한 연유와 시원한 우유가 어우러진 진한 라떼", true);
        productService.create("돌체라떼(Hot)", 4800, null, "핫 커피", "따뜻하고 진한 연유 라떼", true);
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
