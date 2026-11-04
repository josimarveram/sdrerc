package com.sdrerc.v3.web.dto;

public record TotpEnrollStartResponse(String secretBase32, String enrollmentUri) {
}
