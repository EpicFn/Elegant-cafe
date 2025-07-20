package com.back.domain.member.address.service;

import com.back.domain.member.address.entity.Address;
import com.back.domain.member.address.repository.AddressRepository;
import com.back.domain.member.member.entity.Member;
import com.back.global.exception.ServiceException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;

    @Transactional
    public Address submitAddress(Member member, String content) {
        addressRepository.findByMemberAndContent(member, content)
                .ifPresent(existingAddress -> {
                    throw new ServiceException(409, "이미 동일한 주소가 존재합니다.");
                });

        Address address = new Address(content, false, member);

        return addressRepository.save(address);
    }

    @Transactional(readOnly = true)
    public List<Address> getAddressList(Member member) {
        return addressRepository.findAllByMember(member);
    }

    @Transactional
    public void deleteAddress(Member member, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ServiceException(404, "주소를 찾을 수 없습니다."));

        if (!address.getMember().equals(member))
            throw new ServiceException(403, "다른 유저의 주소는 삭제할 수 없습니다.");

        addressRepository.delete(address);
    }

    public Address updateAddress(Member member, Long addressId, @NotBlank String content) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ServiceException(404, "주소를 찾을 수 없습니다."));

        if (!address.getMember().equals(member))
            throw new ServiceException(403, "다른 유저의 주소는 수정할 수 없습니다.");

        if (content == null || content.isBlank())
            throw new ServiceException(400, "주소 내용은 비어있을 수 없습니다.");

        address.updateContent(content);

        return addressRepository.save(address);
    }

    public Address setDefaultAddress(Member member, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ServiceException(404, "주소를 찾을 수 없습니다."));

        if (!address.getMember().equals(member))
            throw new ServiceException(403, "다른 유저의 주소는 수정할 수 없습니다.");

        // 모든 주소의 기본 설정을 해제
        addressRepository.findAllByMember(member).forEach(addr -> addr.setDefault(false));

        // 선택한 주소를 기본 주소로 설정
        address.setDefault(true);

        return addressRepository.save(address);
    }
}
