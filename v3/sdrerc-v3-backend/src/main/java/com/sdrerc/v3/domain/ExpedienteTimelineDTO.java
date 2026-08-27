package com.sdrerc.v3.domain;

import java.time.LocalDateTime;

/**
 * Un evento de la línea de tiempo completa del expediente — subpestaña "Trazabilidad" de la
 * Bandeja de Expedientes. Inspirado en {@code ExpedienteTimelineDTO} (V2, respaldado por la vista
 * ya existente {@code vw_expediente_timeline}), pero NO es un port literal: se consulta
 * {@code EXPEDIENTE_HISTORIAL} directamente (mismos joins que esa vista) en vez de reutilizarla,
 * para poder agregar 4 campos que V2 nunca expone en esta pantalla — mejora explícita pedida por
 * el usuario ("acepto sugerencias... como un experto"):
 * <ul>
 *   <li>{@code movimientoNombre}/{@code etapaXNombre}/{@code estadoXNombre}: V2 solo trae el código
 *   y lo traduce con un mapeador estático en el cliente Swing ({@code DisplayNameMapperV2}); V3
 *   resuelve el nombre amigable directo en el SELECT (columna {@code NOMBRE} ya existente en
 *   {@code tipo_movimiento}/{@code etapa_expediente}/{@code estado_expediente}), sin depender de un
 *   diccionario duplicado en el frontend.</li>
 *   <li>{@code equipoDestino}: V2 no lo muestra en absoluto en esta pantalla, pese a que
 *   {@code EXPEDIENTE_HISTORIAL.ID_EQUIPO_DESTINO} ya existe y se llena en la mayoría de
 *   movimientos (asignaciones/reasignaciones/derivaciones) — contexto real que faltaba.</li>
 *   <li>{@code tablaRelacionada}/{@code idRegistroRelacionado}: permiten distinguir si el evento es
 *   del EXPEDIENTE en sí o de un DOCUMENTO puntual ({@code EXPEDIENTE_DOCUMENTO_ANALIZADO}) —
 *   responde al pedido explícito del usuario "igual me interesa el historial de los documentos que
 *   lo contiene" sin necesitar una segunda tabla/consulta: el mismo historial genérico ya sirve para
 *   ambos casos, filtrable en el frontend por {@code tablaRelacionada}.</li>
 * </ul>
 */
public class ExpedienteTimelineDTO {

    private final Long idExpedienteHistorial;
    private final Long idExpediente;
    private final LocalDateTime fechaMovimiento;
    private final String movimientoCodigo;
    private final String movimientoNombre;
    private final String etapaOrigenCodigo;
    private final String etapaOrigenNombre;
    private final String etapaDestinoCodigo;
    private final String etapaDestinoNombre;
    private final String estadoOrigenCodigo;
    private final String estadoOrigenNombre;
    private final String estadoDestinoCodigo;
    private final String estadoDestinoNombre;
    private final String usuarioOrigen;
    private final String usuarioDestino;
    private final String equipoDestino;
    private final String comentario;
    private final String motivo;
    private final String tablaRelacionada;
    private final Long idRegistroRelacionado;

    public ExpedienteTimelineDTO(
            Long idExpedienteHistorial,
            Long idExpediente,
            LocalDateTime fechaMovimiento,
            String movimientoCodigo,
            String movimientoNombre,
            String etapaOrigenCodigo,
            String etapaOrigenNombre,
            String etapaDestinoCodigo,
            String etapaDestinoNombre,
            String estadoOrigenCodigo,
            String estadoOrigenNombre,
            String estadoDestinoCodigo,
            String estadoDestinoNombre,
            String usuarioOrigen,
            String usuarioDestino,
            String equipoDestino,
            String comentario,
            String motivo,
            String tablaRelacionada,
            Long idRegistroRelacionado) {
        this.idExpedienteHistorial = idExpedienteHistorial;
        this.idExpediente = idExpediente;
        this.fechaMovimiento = fechaMovimiento;
        this.movimientoCodigo = safe(movimientoCodigo);
        this.movimientoNombre = safe(movimientoNombre);
        this.etapaOrigenCodigo = safe(etapaOrigenCodigo);
        this.etapaOrigenNombre = safe(etapaOrigenNombre);
        this.etapaDestinoCodigo = safe(etapaDestinoCodigo);
        this.etapaDestinoNombre = safe(etapaDestinoNombre);
        this.estadoOrigenCodigo = safe(estadoOrigenCodigo);
        this.estadoOrigenNombre = safe(estadoOrigenNombre);
        this.estadoDestinoCodigo = safe(estadoDestinoCodigo);
        this.estadoDestinoNombre = safe(estadoDestinoNombre);
        this.usuarioOrigen = safe(usuarioOrigen);
        this.usuarioDestino = safe(usuarioDestino);
        this.equipoDestino = safe(equipoDestino);
        this.comentario = safe(comentario);
        this.motivo = safe(motivo);
        this.tablaRelacionada = safe(tablaRelacionada);
        this.idRegistroRelacionado = idRegistroRelacionado;
    }

    public Long getIdExpedienteHistorial() {
        return idExpedienteHistorial;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public String getMovimientoCodigo() {
        return movimientoCodigo;
    }

    public String getMovimientoNombre() {
        return movimientoNombre;
    }

    public String getEtapaOrigenCodigo() {
        return etapaOrigenCodigo;
    }

    public String getEtapaOrigenNombre() {
        return etapaOrigenNombre;
    }

    public String getEtapaDestinoCodigo() {
        return etapaDestinoCodigo;
    }

    public String getEtapaDestinoNombre() {
        return etapaDestinoNombre;
    }

    public String getEstadoOrigenCodigo() {
        return estadoOrigenCodigo;
    }

    public String getEstadoOrigenNombre() {
        return estadoOrigenNombre;
    }

    public String getEstadoDestinoCodigo() {
        return estadoDestinoCodigo;
    }

    public String getEstadoDestinoNombre() {
        return estadoDestinoNombre;
    }

    public String getUsuarioOrigen() {
        return usuarioOrigen;
    }

    public String getUsuarioDestino() {
        return usuarioDestino;
    }

    public String getEquipoDestino() {
        return equipoDestino;
    }

    public String getComentario() {
        return comentario;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getTablaRelacionada() {
        return tablaRelacionada;
    }

    public Long getIdRegistroRelacionado() {
        return idRegistroRelacionado;
    }

    /** Distingue si el evento pertenece al expediente en sí o a un documento puntual dentro de él. */
    public boolean isEventoDeDocumento() {
        return "EXPEDIENTE_DOCUMENTO_ANALIZADO".equalsIgnoreCase(tablaRelacionada);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
