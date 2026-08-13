package com.sdrerc.v3.web.dto;

import java.util.List;

public record AsociarRelacionadosRequest(
        Long idExpedientePrincipal,
        List<Long> idsRelacionados,
        String descripcion) {
}
