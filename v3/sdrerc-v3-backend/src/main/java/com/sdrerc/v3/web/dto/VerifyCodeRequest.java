package com.sdrerc.v3.web.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyCodeRequest(@NotBlank String challengeToken, @NotBlank String code) {
}
