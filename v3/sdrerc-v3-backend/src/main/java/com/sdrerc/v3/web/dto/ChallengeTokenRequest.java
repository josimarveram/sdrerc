package com.sdrerc.v3.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChallengeTokenRequest(@NotBlank String challengeToken) {
}
