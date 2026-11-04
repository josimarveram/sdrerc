package com.sdrerc.v3.domain;

/**
 * Port literal de com.sdrerc.domain.dto.sdrercapp.VerificacionRegistroDTO (V2) — payload de
 * "Registrar Verificación". {@code accionCodigo} llega ya resuelto por el frontend/Service según la
 * rama real (ver {@code VerificacionExpedienteDAO#registrarVerificacion}, 3 ramas documentadas ahí).
 */
public class VerificacionRegistroDTO {

    private final Long idExpediente;
    private final String accionCodigo;
    private final String resultadoNombre;
    private final String comentario;
    private final ObservacionVerificacionDTO observacion;
    private final Long idEquipoDestino;
    private final Long idUsuarioDestino;

    public VerificacionRegistroDTO(
            Long idExpediente,
            String accionCodigo,
            String resultadoNombre,
            String comentario,
            ObservacionVerificacionDTO observacion,
            Long idEquipoDestino,
            Long idUsuarioDestino) {
        this.idExpediente = idExpediente;
        this.accionCodigo = safe(accionCodigo);
        this.resultadoNombre = safe(resultadoNombre);
        this.comentario = safe(comentario);
        this.observacion = observacion;
        this.idEquipoDestino = idEquipoDestino;
        this.idUsuarioDestino = idUsuarioDestino;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getResultadoNombre() {
        return resultadoNombre;
    }

    public String getComentario() {
        return comentario;
    }

    public ObservacionVerificacionDTO getObservacion() {
        return observacion;
    }

    public Long getIdEquipoDestino() {
        return idEquipoDestino;
    }

    public Long getIdUsuarioDestino() {
        return idUsuarioDestino;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
