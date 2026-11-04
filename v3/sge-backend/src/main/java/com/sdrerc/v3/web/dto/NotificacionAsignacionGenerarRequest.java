package com.sdrerc.v3.web.dto;

import java.util.List;
import java.util.Map;

/** "Destino operativo" (Bandeja Asignación de Notificación) — ver Javadoc de NotificacionAsignacionService. */
public record NotificacionAsignacionGenerarRequest(
        List<Long> idsDocumentoAnalizado,
        Long idEquipoDestino,
        Long idUsuarioDestino,
        Map<Long, String> hojasEnvioPorDocumento,
        String comentario,
        boolean reasignacion) {
}
