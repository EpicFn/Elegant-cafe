package com.back.domain.member.address.controller;

import com.back.domain.member.address.entity.Address;
import com.back.domain.member.address.service.AddressService;
import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.entity.Member;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "AddressController", description = "주소 관리 엔드포인트")
public class AddressController {
    private final AddressService addressService;
    private final Rq rq;

    record AddressSubmitReqBody(
            @NotBlank
            String content
    ) {
    }

    record AddressResBody(
            Long id,
            String content,
            MemberDto member
    ) {
    }

    @PostMapping
    @Operation(summary = "주소 등록")
    public RsData<AddressResBody>  submitAddress(
            @Valid @RequestBody AddressSubmitReqBody reqBody
    ) {
        Member member = rq.getActor();
        Address address = addressService.submitAddress(member, reqBody.content());

        return new RsData<>(
                201,
                "주소가 등록됐습니다.",
                new AddressResBody(
                        address.getId(),
                        address.getContent(),
                        new MemberDto(address.getMember())
                )
        );
    }

    record AddressListResBody(
            Long id,
            String content,
            boolean isDefault
    ) {
    }

    @GetMapping
    @Operation(summary = "주소 목록 조회")
    public RsData<AddressListResBody[]> getAddressList() {
        Member member = rq.getActor();

        List<Address> addresses = addressService.getAddressList(member);

        AddressListResBody[] resBodies = addresses.stream()
                .map(address -> new AddressListResBody(
                        address.getId(),
                        address.getContent(),
                        address.getIsDefault()
                ))
                .toArray(AddressListResBody[]::new);

        return new RsData<>(
                200,
                "주소 목록을 조회했습니다.",
                resBodies
        );
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "주소 삭제")
    public RsData<Void> deleteAddress(
            @PathVariable Long addressId
    ) {
        Member member = rq.getActor();
        addressService.deleteAddress(member, addressId);

        return new RsData<>(
                204,
                "주소가 삭제됐습니다.",
                null
        );
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "주소 수정")
    public RsData<AddressResBody> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressSubmitReqBody reqBody
    ) {
        Member member = rq.getActor();
        Address address = addressService.updateAddress(member, addressId, reqBody.content());

        return new RsData<>(
                200,
                "주소가 수정됐습니다.",
                new AddressResBody(
                        addressId,
                        address.getContent(),
                        new MemberDto(member)
                )
        );
    }

    @PutMapping("/{addressId}/default")
    @Operation(summary = "기본 주소 설정")
    public RsData<Void> setDefaultAddress(
            @PathVariable Long addressId
    ) {
        Member member = rq.getActor();

        // 모든 주소의 기본 설정을 해제
        Address address = addressService.setDefaultAddress(member, addressId);

        return new RsData<>(
                200,
                "%s번 주소가 기본 주소로 설정됐습니다.".formatted(
                        address.getId()
                ),
                null
        );
    }

}
