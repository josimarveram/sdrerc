package com.sdrerc.domain.dto.sdrercapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnalisisRegistroDTO {

    private final Long idExpediente;
    private final Long idExpedienteAnalisis;
    private final String resultadoCodigo;
    private final String resultadoNombre;
    private final Boolean corresponde;
    private final Boolean incorporado;
    private final boolean requiereReconstitucion;
    private final boolean tieneLegitimidad;
    private final boolean cumpleMediosProbatorios;
    private final String fundamento;
    private final String motivoNoCorrespondeCodigo;
    private final String tipoDocumentoNoCorrespondeCodigo;
    private final String tipoDocumentoNoCorrespondeNombre;
    private final String numeroDocumentoProveido;
    private final ObservacionAnalisisDTO observacion;
    private final List<DocumentoAnalizadoDTO> documentosAnalizados;

    public AnalisisRegistroDTO(
            Long idExpediente,
            String resultadoCodigo,
            String resultadoNombre,
            Boolean corresponde,
            Boolean incorporado,
            boolean requiereReconstitucion,
            boolean tieneLegitimidad,
            boolean cumpleMediosProbatorios,
            String fundamento,
            String motivoNoCorrespondeCodigo,
            String numeroDocumentoProveido,
            ObservacionAnalisisDTO observacion,
            List<DocumentoAnalizadoDTO> documentosAnalizados) {
        this(
                idExpediente,
                resultadoCodigo,
                resultadoNombre,
                corresponde,
                incorporado,
                requiereReconstitucion,
                tieneLegitimidad,
                cumpleMediosProbatorios,
                fundamento,
                motivoNoCorrespondeCodigo,
                "PROVEIDO",
                "Proveido",
                numeroDocumentoProveido,
                observacion,
                documentosAnalizados);
    }

    public AnalisisRegistroDTO(
            Long idExpediente,
            String resultadoCodigo,
            String resultadoNombre,
            Boolean corresponde,
            Boolean incorporado,
            boolean requiereReconstitucion,
            boolean tieneLegitimidad,
            boolean cumpleMediosProbatorios,
            String fundamento,
            String motivoNoCorrespondeCodigo,
            String tipoDocumentoNoCorrespondeCodigo,
            String tipoDocumentoNoCorrespondeNombre,
            String numeroDocumentoProveido,
            ObservacionAnalisisDTO observacion,
            List<DocumentoAnalizadoDTO> documentosAnalizados) {
        this(
                idExpediente,
                null,
                resultadoCodigo,
                resultadoNombre,
                corresponde,
                incorporado,
                requiereReconstitucion,
                tieneLegitimidad,
                cumpleMediosProbatorios,
                fundamento,
                motivoNoCorrespondeCodigo,
                tipoDocumentoNoCorrespondeCodigo,
                tipoDocumentoNoCorrespondeNombre,
                numeroDocumentoProveido,
                observacion,
                documentosAnalizados);
    }

    public AnalisisRegistroDTO(
            Long idExpediente,
            Long idExpedienteAnalisis,
            String resultadoCodigo,
            String resultadoNombre,
            Boolean corresponde,
            Boolean incorporado,
            boolean requiereReconstitucion,
            boolean tieneLegitimidad,
            boolean cumpleMediosProbatorios,
            String fundamento,
            String motivoNoCorrespondeCodigo,
            String tipoDocumentoNoCorrespondeCodigo,
            String tipoDocumentoNoCorrespondeNombre,
            String numeroDocumentoProveido,
            ObservacionAnalisisDTO observacion,
            List<DocumentoAnalizadoDTO> documentosAnalizados) {
        this.idExpediente = idExpediente;
        this.idExpedienteAnalisis = idExpedienteAnalisis;
        this.resultadoCodigo = safe(resultadoCodigo);
        this.resultadoNombre = safe(resultadoNombre);
        this.corresponde = corresponde;
        this.incorporado = incorporado;
        this.requiereReconstitucion = requiereReconstitucion;
        this.tieneLegitimidad = tieneLegitimidad;
        this.cumpleMediosProbatorios = cumpleMediosProbatorios;
        this.fundamento = safe(fundamento);
        this.motivoNoCorrespondeCodigo = safe(motivoNoCorrespondeCodigo);
        this.tipoDocumentoNoCorrespondeCodigo = safe(tipoDocumentoNoCorrespondeCodigo);
        this.tipoDocumentoNoCorrespondeNombre = safe(tipoDocumentoNoCorrespondeNombre);
        this.numeroDocumentoProveido = safe(numeroDocumentoProveido);
        this.observacion = observacion;
        this.documentosAnalizados = documentosAnalizados == null
                ? Collections.<DocumentoAnalizadoDTO>emptyList()
                : Collections.unmodifiableList(new ArrayList<DocumentoAnalizadoDTO>(documentosAnalizados));
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public Long getIdExpedienteAnalisis() {
        return idExpedienteAnalisis;
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

    public String getTipoDocumentoNoCorrespondeCodigo() {
        return tipoDocumentoNoCorrespondeCodigo;
    }

    public String getTipoDocumentoNoCorrespondeNombre() {
        return tipoDocumentoNoCorrespondeNombre;
    }

    public String getNumeroDocumentoProveido() {
        return numeroDocumentoProveido;
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
