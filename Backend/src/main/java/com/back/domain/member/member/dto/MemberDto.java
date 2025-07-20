package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;
import org.springframework.lang.NonNull;

public record MemberDto(
        @NonNull Long id,
        @NonNull String email,
        @NonNull String name
) {
    public MemberDto(Member member) {
        this(
                member.getId(),
                member.getEmail(),
                member.getName()
        );
    }
}
