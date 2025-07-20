package com.back.domain.member.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberUpdateDto(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 2, max = 30)
        String name,

        @NotBlank
        @Size(min = 8, max = 20)
        String password
) {
}
