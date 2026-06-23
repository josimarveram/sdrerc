package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PlazoConfiguracionDTO {

    public static final String UNIDAD_HABILES = "HABILES";
    public static final String UNIDAD_CALENDARIO = "CALENDARIO";
    public static final String CODIGO_SOLICITUD_SDRERC = "SOLICITUD_SDRERC";
    public static final String CODIGO_SOLICITUD_RECTIFICACION_ADMINISTRATIVA = "SOLICITUD_RECTIFICACION_ADMINISTRATIVA";
    public static final String CODIGO_SOLICITUD_RECONSIDERACION = "SOLICITUD_RECONSIDERACION";
    public static final String CODIGO_SOLICITUD_APELACION = "SOLICITUD_APELACION";

    private Long idPlazoConfiguracion;
    private String codigo;
    private String nombre;
    private String ambito;
    private Long idEtapa;
    private String etapaCodigo;
    private String etapaNombre;
    private Long idTipoDocumento;
    private String tipoDocumentoCodigo;
    private String tipoDocumentoNombre;
    private Integer diasPlazo;
    private String unidadPlazo;
    private LocalDate fechaVigenciaDesde;
    private LocalDate fechaVigenciaHasta;
    private boolean activo;
    private String observacion;
    private LocalDateTime creadoEn;
    private LocalDateTime modificadoEn;

    public PlazoConfiguracionDTO() {
        this.codigo = CODIGO_SOLICITUD_SDRERC;
        this.nombre = "Plazo de atención de solicitudes SDRERC";
        this.ambito = CODIGO_SOLICITUD_SDRERC;
        this.diasPlazo = Integer.valueOf(30);
        this.unidadPlazo = UNIDAD_HABILES;
        this.activo = true;
    }

    public PlazoConfiguracionDTO(
            Long idPlazoConfiguracion,
            String codigo,
            String nombre,
            String ambito,
            Long idEtapa,
            String etapaCodigo,
            String etapaNombre,
            Long idTipoDocumento,
            String tipoDocumentoCodigo,
            String tipoDocumentoNombre,
            Integer diasPlazo,
            String unidadPlazo,
            LocalDate fechaVigenciaDesde,
            LocalDate fechaVigenciaHasta,
            boolean activo,
            String observacion,
            LocalDateTime creadoEn,
            LocalDateTime modificadoEn) {
        this.idPlazoConfiguracion = idPlazoConfiguracion;
        this.codigo = normalizeCode(codigo);
        this.nombre = safe(nombre);
        this.ambito = normalizeCode(ambito);
        this.idEtapa = idEtapa;
        this.etapaCodigo = normalizeCode(etapaCodigo);
        this.etapaNombre = safe(etapaNombre);
        this.idTipoDocumento = idTipoDocumento;
        this.tipoDocumentoCodigo = normalizeCode(tipoDocumentoCodigo);
        this.tipoDocumentoNombre = safe(tipoDocumentoNombre);
        this.diasPlazo = diasPlazo;
        this.unidadPlazo = normalizeUnit(unidadPlazo);
        this.fechaVigenciaDesde = fechaVigenciaDesde;
        this.fechaVigenciaHasta = fechaVigenciaHasta;
        this.activo = activo;
        this.observacion = safe(observacion);
        this.creadoEn = creadoEn;
        this.modificadoEn = modificadoEn;
    }

    public Long getIdPlazoConfiguracion() {
        return idPlazoConfiguracion;
    }

    public void setIdPlazoConfiguracion(Long idPlazoConfiguracion) {
        this.idPlazoConfiguracion = idPlazoConfiguracion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = normalizeCode(codigo);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = safe(nombre);
    }

    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        this.ambito = normalizeCode(ambito);
    }

    public Long getIdEtapa() {
        return idEtapa;
    }

    public void setIdEtapa(Long idEtapa) {
        this.idEtapa = idEtapa;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public void setEtapaCodigo(String etapaCodigo) {
        this.etapaCodigo = normalizeCode(etapaCodigo);
    }

    public String getEtapaNombre() {
        return etapaNombre;
    }

    public void setEtapaNombre(String etapaNombre) {
        this.etapaNombre = safe(etapaNombre);
    }

    public Long getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(Long idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getTipoDocumentoCodigo() {
        return tipoDocumentoCodigo;
    }

    public void setTipoDocumentoCodigo(String tipoDocumentoCodigo) {
        this.tipoDocumentoCodigo = normalizeCode(tipoDocumentoCodigo);
    }

    public String getTipoDocumentoNombre() {
        return tipoDocumentoNombre;
    }

    public void setTipoDocumentoNombre(String tipoDocumentoNombre) {
        this.tipoDocumentoNombre = safe(tipoDocumentoNombre);
    }

    public Integer getDiasPlazo() {
        return diasPlazo;
    }

    public void setDiasPlazo(Integer diasPlazo) {
        this.diasPlazo = diasPlazo;
    }

    public String getUnidadPlazo() {
        return unidadPlazo;
    }

    public void setUnidadPlazo(String unidadPlazo) {
        this.unidadPlazo = normalizeUnit(unidadPlazo);
    }

    public LocalDate getFechaVigenciaDesde() {
        return fechaVigenciaDesde;
    }

    public void setFechaVigenciaDesde(LocalDate fechaVigenciaDesde) {
        this.fechaVigenciaDesde = fechaVigenciaDesde;
    }

    public LocalDate getFechaVigenciaHasta() {
        return fechaVigenciaHasta;
    }

    public void setFechaVigenciaHasta(LocalDate fechaVigenciaHasta) {
        this.fechaVigenciaHasta = fechaVigenciaHasta;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = safe(observacion);
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getModificadoEn() {
        return modificadoEn;
    }

    public boolean isHabiles() {
        return UNIDAD_HABILES.equalsIgnoreCase(unidadPlazo);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeCode(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private static String normalizeUnit(String value) {
        String normalized = normalizeCode(value);
        return UNIDAD_CALENDARIO.equals(normalized) ? UNIDAD_CALENDARIO : UNIDAD_HABILES;
    }
}
