package com.sdrerc.v3.web.dto;

import java.util.List;
import java.util.Map;

public record AsignarExpedientesRequest(
        List<Long> idsExpediente,
        Long idEquipo,
        Long idAbogado,
        String comentario,
        Map<Long, String> hojasEnvioPorExpediente) {
}
