package com.sdrerc.v3.web.dto;

import java.util.List;

public record AsociarGrupoFamiliarRequest(
        Long idExpedientePrincipal,
        List<Long> idsExpedientesCandidatos) {
}
