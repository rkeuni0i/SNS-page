package com.mysns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "아이디를 입력해 주세요.")
        @Size(max = 128, message = "아이디가 너무 깁니다(최대 128자).")
        @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "아이디는 이메일 주소 형식이어야 합니다.")
        String id,

        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다.") // BCrypt는 72바이트까지만 반영한다
        String password,

        @NotBlank(message = "비밀번호 확인을 입력해 주세요.")
        String passwordConfirm,

        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(max = 32, message = "이름이 너무 깁니다(최대 32자).")
        String name) {
}
