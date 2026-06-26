package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalisisDetalleDTO {

    private final boolean registrado;
    private final String resultadoCodigo;
    private final String resultadoNombre;
    private final Boolean corresponde;
    private final Boolean incorporado;
    private final boolean requiereReconstitucion;
    private final boolean tieneLegitimidad;
    private final boolean cumpleMediosProbatorios;
    private final String fundamento;
    private final String motivoNoCorrespondeCodigo;
    private final String motivoNoCorrespondeNombre;
    private final String tipoDocumentoNoCorrespondeCodigo;
    private final String tipoDocumentoNoCorrespondeNombre;
    private final String numeroDocumentoProveido;
    private final LocalDate fechaEvaluacion;
    private final ObservacionAnalisisDTO observacion;
    private final List<DocumentoAnalizadoDTO> documentosAnalizados;

    public AnalisisDetalleDTO(
            boolean registrado,
            String resultadoCodigo,
            String resultadoNombre,
            Boolean corresponde,
            Boolean incorporado,
            boolean requiereReconstitucion,
            boolean tieneLegitimidad,
            boolean cumpleMediosProbatorios,
            String fundamento,
            String motivoNoCorrespondeCodigo,
            String motivoNoCorrespondeNombre,
            String numeroDocumentoProveido,
            LocalDate fechaEvaluacion,
            ObservacionAnalisisDTO observacion,
            List<DocumentoAnalizadoDTO> documentosAnalizados) {
        this(
                registrado,
                resultadoCodigo,
                resultadoNombre,
                corresponde,
                incorporado,
                requiereReconstitucion,
                tieneLegitimidad,
                cumpleMediosProbatorios,
                fundamento,
                motivoNoCorrespondeCodigo,
                motivoNoCorrespondeNombre,
                "PROVEIDO",
                "Proveido",
                numeroDocumentoProveido,
                fechaEvaluacion,
                observacion,
                documentosAnalizados);
    }

    public AnalisisDetalleDTO(
            boolean registrado,
            String resultadoCodigo,
            String resultadoNombre,
            Boolean corresponde,
            Boolean incorporado,
            boolean requiereReconstitucion,
            boolean tieneLegitimidad,
            boolean cumpleMediosProbatorios,
            String fundamento,
            String motivoNoCorrespondeCodigo,
            String motivoNoCorrespondeNombre,
            String tipoDocumentoNoCorrespondeCodigo,
            String tipoDocumentoNoCorrespondeNombre,
            String numeroDocumentoProveido,
            LocalDate fechaEvaluacion,
            ObservacionAnalisisDTO observacion,
            List<DocumentoAnalizadoDTO> documentosAnalizados) {
        this.registrado = registrado;
        this.resultadoCodigo = safe(resultadoCodigo);
        this.resultadoNombre = safe(resultadoNombre);
        this.corresponde = corresponde;
        this.incorporado = incorporado;
        this.requiereReconstitucion = requiereReconstitucion;
        this.tieneLegitimidad = tieneLegitimidad;
        this.cumpleMediosProbatorios = cumpleMediosProbatorios;
        this.fundamento = safe(fundamento);
        this.motivoNoCorrespondeCodigo = safe(motivoNoCorrespondeCodigo);
        this.motivoNoCorrespondeNombre = safe(motivoNoCorrespondeNombre);
        this.tipoDocumentoNoCorrespondeCodigo = safe(tipoDocumentoNoCorrespondeCodigo);
        this.tipoDocumentoNoCorrespondeNombre = safe(tipoDocumentoNoCorrespondeNombre);
        this.numeroDocumentoProveido = safe(numeroDocumentoProveido);
        this.fechaEvaluacion = fechaEvaluacion;
        this.observacion = observacion;
        this.documentosAnalizados = documentosAnalizados == null
                ? Collections.<DocumentoAnalizadoDTO>emptyList()
                : Collections.unmodifiableList(new ArrayList<DocumentoAnalizadoDTO>(documentosAnalizados));
    }

    public boolean isRegistrado() {
        return registrado;
    }

    public String getResultadoCodigo() {
        return resultadoCodigo;
    }

    public String getResultadoNombre() {
        return resultadoNombre;
    }

    public Boolean getCorresponde() {
        return corresponde;
    }

    public Boolean getIncorporado() {
        return incorporado;
    }

    public boolean isRequiereReconstitucion() {
        return requiereReconstitucion;
    }

    public boolean isTieneLegitimidad() {
        return tieneLegitimidad;
    }

    public boolean isCumpleMediosProbatorios() {
        return cumpleMediosProbatorios;
    }

    public String getFundamento() {
        return fundamento;
    }

    public String getMotivoNoCorrespondeCodigo() {
        return motivoNoCorrespondeCodigo;
    }

    public String getMotivoNoCorrespondeNombre() {
        return motivoNoCorrespondeNombre;
    }

    public String getTipoDocumentoNoCorrespondeCodigo() {
        return tipoDocumentoNoCorrespondeCodigo;
    }

    public String getTipoDocumentoNoCorrespondeNombre() {
        return tipoDocumentoNoCorrespondeNombre;
    }

    public String getNumeroDocumentoProveido() {
        return numeroDocumentoProveido;
    }

    public LocalDate getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public ObservacionAnalisisDTO getObservacion() {
        return observacion;
    }

    public List<DocumentoAnalizadoDTO> getDocumentosAnalizados() {
        return documentosAnalizados;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
