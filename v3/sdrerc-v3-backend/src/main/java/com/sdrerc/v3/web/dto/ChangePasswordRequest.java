package com.sdrerc.v3.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(@NotBlank String challengeToken, @NotBlank String newPassword) {
}
