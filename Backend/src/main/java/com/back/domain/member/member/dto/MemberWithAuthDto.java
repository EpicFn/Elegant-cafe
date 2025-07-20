package com.back.domain.member.member.dto;

import com.back.domain.member.member.entity.Member;
import org.springframework.lang.NonNull;

// admin 권한 여부를 포함한 DTO 클래스
public record MemberWithAuthDto(
    @NonNull Long id,
    @NonNull String email,
    @NonNull String name,
    @NonNull boolean isAdmin
) {

    public MemberWithAuthDto(Long id, String email, String name) {
        this(id, email, name, false);
    }

    public MemberWithAuthDto(Member member) {
        this(member.getId(), member.getEmail(), member.getName(), member.isAdmin());
    }
}
