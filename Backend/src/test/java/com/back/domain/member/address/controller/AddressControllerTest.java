package com.back.domain.member.address.controller;

import com.back.domain.member.address.service.AddressService;
import com.back.domain.member.address.entity.Address;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.global.rq.Rq;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AddressControllerTest {
    @Autowired
    private AddressService addressService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private Rq rq;


    @Test
    @DisplayName("주소 등록")
    @WithUserDetails("user1@gmail.com")
    void submitAddress() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/addresses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "서울특별시"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));

        Address address = member.getLastAddress()
                .orElseThrow(() -> new IllegalStateException("주소가 등록되지 않았습니다."));

        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("submitAddress"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("주소가 등록됐습니다."))
                .andExpect(jsonPath("$.data.id").value(address.getId()))
                .andExpect(jsonPath("$.data.content").value(address.getContent()))
                .andExpect(jsonPath("$.data.member.id").value(member.getId()));

        assertThat(address.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("주소 등록 - 이미 등록된 주소")
    @WithUserDetails("user1@gmail.com")
    void submitAddress_addressConflict() throws Exception {
        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));

        Address address = addressService.submitAddress(member, "서울특별시");

        ResultActions resultActions = mvc
                .perform(
                        post("/api/addresses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "서울특별시"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("submitAddress"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("이미 동일한 주소가 존재합니다."));

    }

    @Test
    @DisplayName("주소 등록 - 주소가 비어있음")
    @WithUserDetails("user1@gmail.com")
    void submitAddress_addressEmpty() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/addresses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": ""
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("submitAddress"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("content-NotBlank-must not be blank"));
    }

    @Test
    @DisplayName("전체 주소 조회")
    @WithUserDetails("user1@gmail.com")
    void getAddressList() throws Exception {
        // Given: 유저가 존재하고, 여러 주소가 등록되어 있음
        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));

        addressService.submitAddress(member, "서울특별시");
        addressService.submitAddress(member, "부산광역시");
        addressService.submitAddress(member, "대구광역시");

        // When: 주소 목록을 조회하는 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        get("/api/addresses")
                )
                .andDo(print());

        // Then: 주소 목록이 올바르게 반환되고, 상태 코드가 200이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("getAddressList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("주소 목록을 조회했습니다."))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].content").value("서울특별시"))
                .andExpect(jsonPath("$.data[1].content").value("부산광역시"))
                .andExpect(jsonPath("$.data[2].content").value("대구광역시"));
    }

    @Test
    @DisplayName("전체 주소 조회 - 주소 목록이 비어있음")
    @WithUserDetails("user1@gmail.com")
    void getAddressList_emptyList() throws Exception {
        // Given: 유저가 존재하고, 여러 주소가 등록되어 있음
        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));

        // When: 주소 목록을 조회하는 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        get("/api/addresses")
                )
                .andDo(print());

        // Then: 빈 주소 목록이 올바르게 반환되고, 상태 코드가 200이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("getAddressList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("주소 목록을 조회했습니다."))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("전체 주소 조회 - 인증된 유저가 없음")
    void getAddressList_withoutAuth() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/addresses")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("로그인 후 이용해주세요."));
    }

    @Test
    @DisplayName("주소 삭제")
    @WithUserDetails("user1@gmail.com")
    void deleteAddress() throws Exception {
        // Given: 유저가 존재하고, 주소가 등록되어 있음
        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));

        Address address = addressService.submitAddress(member, "서울특별시");

        // When: 주소 삭제 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/addresses/" + address.getId())
                )
                .andDo(print());

        // Then: 주소가 성공적으로 삭제되고, 상태 코드가 200이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("deleteAddress"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.code").value(204))
                .andExpect(jsonPath("$.message").value("주소가 삭제됐습니다."));
    }

    @Test
    @DisplayName("주소 삭제 - 다른 유저의 주소")
    @WithUserDetails("user1@gmail.com")
    void deleteAddress_otherUser() throws Exception {
        // Given: 유저가 존재하고, 다른 유저의 주소가 등록되어 있음
        Member otherMember = memberService.findByEmail("user2@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));
        Address address = addressService.submitAddress(otherMember, "서울특별시");

        // When: 다른 유저의 주소 삭제 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/addresses/" + address.getId())
                )
                .andDo(print());

        // Then: 상태 코드가 403이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("deleteAddress"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("다른 유저의 주소는 삭제할 수 없습니다."));
    }

    @Test
    @DisplayName("주소 삭제 - 존재하지 않는 주소")
    @WithUserDetails("user1@gmail.com")
    void deleteAddress_notFound() throws Exception {
        // Given: 유저가 존재하고, 주소가 등록되어 있지 않음
        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));

        // When: 존재하지 않는 주소 삭제 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/addresses/9999") // 존재하지 않는 주소 ID
                )
                .andDo(print());

        // Then: 상태 코드가 404이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("deleteAddress"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("주소를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("주소 수정")
    @WithUserDetails("user1@gmail.com")
    void updateAddress() throws Exception {
        // Given: 유저가 존재하고, 주소가 등록되어 있음
        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));
        Address address = addressService.submitAddress(member, "서울특별시");

        // When: 주소 수정 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        put("/api/addresses/" + address.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "수정된 서울특별시"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // Then: 주소가 성공적으로 수정되고, 상태 코드가 200이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("updateAddress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("주소가 수정됐습니다."))
                .andExpect(jsonPath("$.data.id").value(address.getId()))
                .andExpect(jsonPath("$.data.content").value("수정된 서울특별시"))
                .andExpect(jsonPath("$.data.member.id").value(member.getId()));
        assertThat(address.getContent()).isEqualTo("수정된 서울특별시");
    }

    @Test
    @DisplayName("주소 수정 - 다른 유저의 주소")
    @WithUserDetails("user1@gmail.com")
    void updateAddress_otherUser() throws Exception {
        // Given: 유저가 존재하고, 다른 유저의 주소가 등록되어 있음
        Member otherMember = memberService.findByEmail("user2@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));
        Address address = addressService.submitAddress(otherMember, "서울특별시");

        // When: 다른 유저의 주소 수정 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        put("/api/addresses/" + address.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "수정된 서울특별시"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // Then: 상태 코드가 403이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("updateAddress"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("다른 유저의 주소는 수정할 수 없습니다."));
    }

    @Test
    @DisplayName("주소 수정 - 존재하지 않는 주소")
    @WithUserDetails("user1@gmail.com")
    void updateAddress_notFound() throws Exception {
        // Given: 유저가 존재하고, 주소가 등록되어 있지 않음
        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));

        // When: 존재하지 않는 주소 수정 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        put("/api/addresses/9999") // 존재하지 않는 주소 ID
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "수정된 서울특별시"
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // Then: 상태 코드가 404이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("updateAddress"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("주소를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("주소 수정 - 주소가 비어있음")
    @WithUserDetails("user1@gmail.com")
    void updateAddress_addressEmpty() throws Exception {
        // Given: 유저가 존재하고, 주소가 등록되어 있음
        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));
        Address address = addressService.submitAddress(member, "서울특별시");

        // When: 주소 수정 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        put("/api/addresses/" + address.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": ""
                                        }
                                        """.stripIndent())
                )
                .andDo(print());

        // Then: 상태 코드가 400이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("updateAddress"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("content-NotBlank-must not be blank"));
    }

    @Test
    @DisplayName("기본 주소 설정")
    @WithUserDetails("user1@gmail.com")
    void setDefaultAddress() throws Exception {
        // Given: 유저가 존재하고, 여러 주소가 등록되어 있음
        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));
        Address address1 = addressService.submitAddress(member, "서울특별시");
        Address address2 = addressService.submitAddress(member, "부산광역시");
        address2.setDefault(true); // 주소2를 기본 주소로 설정

        // When: 기본 주소 설정 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        put("/api/addresses/" + address1.getId() + "/default")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // Then: 기본 주소가 성공적으로 설정되고, 상태 코드가 200이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("setDefaultAddress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("%s번 주소가 기본 주소로 설정됐습니다.".formatted(
                        address1.getId()
                )));

        // 주소1이 기본 주소로 설정되었는지 확인
        assertThat(address1.getIsDefault()).isTrue();
        // 주소2는 기본 주소가 아니어야 함
        assertThat(address2.getIsDefault()).isFalse();
    }

    @Test
    @DisplayName("기본 주소 설정 - 다른 유저의 주소")
    @WithUserDetails("user1@gmail.com")
    void setDefaultAddress_otherUser() throws Exception {
        // Given: 유저가 존재하고, 다른 유저의 주소가 등록되어 있음
        Member otherMember = memberService.findByEmail("user2@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));
        Address address = addressService.submitAddress(otherMember, "서울특별시");

        // When: 다른 유저의 주소 기본 설정 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        put("/api/addresses/" + address.getId() + "/default")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // Then: 상태 코드가 403이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("setDefaultAddress"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("다른 유저의 주소는 수정할 수 없습니다."));
    }

    @Test
    @DisplayName("기본 주소 설정 - 존재하지 않는 주소")
    @WithUserDetails("user1@gmail.com")
    void setDefaultAddress_notFound() throws Exception {
        // Given: 유저가 존재하고, 주소가 등록되어 있지 않음
        Member member = memberService.findByEmail("user1@gmail.com")
                .orElseThrow(() -> new IllegalStateException("유저가 존재하지 않습니다."));

        // When: 존재하지 않는 주소 기본 설정 API를 호출
        ResultActions resultActions = mvc
                .perform(
                        put("/api/addresses/9999/default") // 존재하지 않는 주소 ID
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        // Then: 상태 코드가 404이어야 함
        resultActions
                .andExpect(handler().handlerType(AddressController.class))
                .andExpect(handler().methodName("setDefaultAddress"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("주소를 찾을 수 없습니다."));
    }

}