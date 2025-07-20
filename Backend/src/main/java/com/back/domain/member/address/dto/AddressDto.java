package com.back.domain.member.address.dto;

import com.back.domain.member.address.entity.Address;
import org.springframework.lang.NonNull;

public record AddressDto(
        @NonNull String content,
        @NonNull Boolean isDefault
) {
    public AddressDto(Address address) {
        this(address.getContent(), address.getIsDefault());
    }

}
