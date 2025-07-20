package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.dto.MemberUpdateDto;
import com.back.domain.member.member.dto.MemberWithAuthDto;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.order.dto.UserOrderDetailResponseBody;
import com.back.domain.order.dto.UserOrderResponseBody;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "MemberController", description = "회원 관리 엔드포인트")
public class MemberController {
    private final MemberService memberService;
    private final Rq rq;

    record MemberJoinReqBody(
            @NotBlank
            @Email
            String email,

            @NotBlank
            @Size(min = 8, max = 20)
            String password,

            @NotBlank
            @Size(min = 2, max = 30)
            String name
    ) { }


    @PostMapping("/join")
    @Transactional
    @Operation(summary = "회원 가입")
    public RsData<MemberDto> join(
            @Valid @RequestBody MemberJoinReqBody reqBody
    ) {
        Member member = memberService.join(
                reqBody.email(),
                reqBody.password(),
                reqBody.name()
        );

        return new RsData<>(
                201,
                "%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getName()),
                new MemberDto(member)
        );
    }

    record MemberLoginReqBody(
            @NotBlank
            @Email
            String email,

            @NotBlank
            String password
    ) { }

    record MemberLoginResBody(
            MemberWithAuthDto member,
            String apiKey,
            String accessToken
    ) { }


    @PostMapping("/login")
    @Transactional(readOnly = true)
    @Operation(summary = "회원 로그인")
    public RsData<MemberLoginResBody> login(
            @Valid @RequestBody MemberLoginReqBody reqBody
    ) {
        Member member = memberService.findByEmail(reqBody.email())
                .orElseThrow(() -> new ServiceException(401, "존재하지 않는 이메일입니다."));

        memberService.checkPassword(
                member,
                reqBody.password()
        );

        String accessToken = memberService.genAccessToken(member);

        rq.setCookie("apiKey", member.getApiKey());
        rq.setCookie("accessToken", accessToken);

        return new RsData<>(
                200,
                "%s님 환영합니다.".formatted(member.getName()),
                new MemberLoginResBody(
                        new MemberWithAuthDto(member),
                        member.getApiKey(),
                        accessToken
                )
        );
    }

    @DeleteMapping("/logout")
    @Operation(summary = "회원 로그아웃")
    public RsData<Void> logout(){
        rq.deleteCookie("apiKey");
        rq.deleteCookie("accessToken");

        return new RsData<>(
                200,
                "로그아웃 됐습니다.",
                null
        );
    }

    @DeleteMapping("/withdraw")
    @Operation(summary = "회원 탈퇴")
    @Transactional
    public RsData<Void> withdraw() {
        Member actor = rq.getActor();
        Member member = memberService.findById(actor.getId())
                .orElseThrow(() -> new ServiceException(404, "존재하지 않는 회원입니다."));

        memberService.withdraw(member);

        rq.deleteCookie("apiKey");
        rq.deleteCookie("accessToken");

        return new RsData<>(
                200,
                "회원 탈퇴가 완료되었습니다.",
                null
        );
    }

    @GetMapping("/info")
    @Operation(summary = "회원 정보 조회")
    @Transactional(readOnly = true)
    public RsData<MemberWithAuthDto> getMemberInfo() {
        Member actor = rq.getActor();
        Member member = memberService.findById(actor.getId())
                .orElseThrow(() -> new ServiceException(404, "존재하지 않는 회원입니다."));

        return new RsData<>(
                200,
                "회원 정보가 조회됐습니다.",
                new MemberWithAuthDto(member)
        );
    }




    @PutMapping("/info")
    @Operation(summary = "회원 정보 수정")
    public RsData<MemberWithAuthDto> updateMemberInfo(
            @Valid @RequestBody MemberUpdateDto reqBody
    ) {
        Member actor = rq.getActor();
        Member member = memberService.findById(actor.getId())
                .orElseThrow(() -> new ServiceException(404, "존재하지 않는 회원입니다."));

        memberService.modify(reqBody, member);

        return new RsData<>(
                200,
                "회원 정보가 수정됐습니다.",
                new MemberWithAuthDto(member)
        );
    }

    @GetMapping("/orders")
    @Operation(summary = "회원의 주문 내역 전체 조회")
    public RsData<UserOrderResponseBody[]> getMemberOrders() {
        Member actor = rq.getActor();
        Member member = memberService.findById(actor.getId())
                .orElseThrow(() -> new ServiceException(404, "존재하지 않는 회원입니다."));

        UserOrderResponseBody[] resBody = memberService.getMemberOrders(member);

        return new RsData<>(
                200,
                "회원 주문 내역이 조회됐습니다.",
                resBody
        );
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "회원의 특정 주문 내역 상세 조회")
    public RsData<UserOrderDetailResponseBody> getMemberOrderDetail(
            @PathVariable Long orderId
    ) {
        Member actor = rq.getActor();
        Member member = memberService.findById(actor.getId())
                .orElseThrow(() -> new ServiceException(404, "존재하지 않는 회원입니다."));

        UserOrderDetailResponseBody resBody = memberService.getMemberOrderDetail(member, orderId);

        return new RsData<>(
                200,
                "회원 주문 상세 내역이 조회됐습니다.",
                resBody
        );
    }

    record MemberPasswordVerifyReqBody(
            @NotBlank
            @Size(min = 2, max = 20)
            String password
    ) { }

    @PostMapping("/verify-password")
    @Operation(summary = "회원 비밀번호 검증")
    public RsData<Void> verifyPassword(
            @Valid @RequestBody MemberPasswordVerifyReqBody reqBody
    ) {
        Member actor = rq.getActor();
        Member member = memberService.findById(actor.getId())
                .orElseThrow(() -> new ServiceException(404, "존재하지 않는 회원입니다."));

        memberService.checkPassword(member, reqBody.password());

        return new RsData<>(
                200,
                "비밀번호가 검증됐습니다.",
                null
        );

    }
}
