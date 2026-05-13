package com.sdrerc.ui.views.expedientes;

import com.sdrerc.domain.model.Expediente.Expediente;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CargaDiariaExcelRow {

    public static final String ESTADO_VALIDO = "VÁLIDO";
    public static final String ESTADO_ADVERTENCIA = "ADVERTENCIA";
    public static final String ESTADO_ERROR = "ERROR";
    public static final String ESTADO_DUPLICADO = "DUPLICADO";
    public static final String CARGA_PENDIENTE = "PENDIENTE";
    public static final String CARGA_REGISTRADO = "REGISTRADO";
    public static final String CARGA_OMITIDO = "OMITIDO";
    public static final String CARGA_ERROR = "ERROR";

    private final int numeroFilaExcel;
    private String estadoValidacion = ESTADO_VALIDO;
    private final List<String> observaciones = new ArrayList<>();
    private Date fechaSolicitud;
    private String fechaSolicitudTexto;
    private String canal;
    private String referencia;
    private String tipoSolicitud;
    private int idTipoSolicitud;
    private String procedimientoRegistral;
    private int idProcedimientoRegistral;
    private String tipoDocumento;
    private int idTipoDocumento;
    private String numeroDocumento;
    private String numeroDocumentoPersistente;
    private String tipoActa;
    private int idTipoActa;
    private String numeroActa;
    private String titular;
    private String dniTitularVisual;
    private String dniTitularPersistente;
    private String titular2;
    private String solicitadoPor;
    private String dniSolicitanteVisual;
    private String dniSolicitantePersistente;
    private Expediente expedienteDuplicado;
    private String estadoCarga = CARGA_PENDIENTE;
    private String observacionCarga = "";

    public CargaDiariaExcelRow(int numeroFilaExcel) {
        this.numeroFilaExcel = numeroFilaExcel;
    }

    public int getNumeroFilaExcel() {
        return numeroFilaExcel;
    }

    public String getEstadoValidacion() {
        return estadoValidacion;
    }

    public String getObservacionesResumen() {
        if (observaciones.isEmpty()) {
            return "Registro válido para importar.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < observaciones.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(observaciones.get(i));
        }
        return sb.toString();
    }

    public boolean esImportable() {
        return ESTADO_VALIDO.equals(estadoValidacion) || ESTADO_ADVERTENCIA.equals(estadoValidacion);
    }

    public boolean esMatrimonio() {
        return "MATRIMONIO".equalsIgnoreCase(tipoActa == null ? "" : tipoActa.trim());
    }

    public void addInfo(String mensaje) {
        if (mensaje != null && !mensaje.trim().isEmpty()) {
            observaciones.add(mensaje.trim());
        }
    }

    public void addAdvertencia(String mensaje) {
        addInfo(mensaje);
        if (ESTADO_VALIDO.equals(estadoValidacion)) {
            estadoValidacion = ESTADO_ADVERTENCIA;
        }
    }

    public void addError(String mensaje) {
        addInfo(mensaje);
        estadoValidacion = ESTADO_ERROR;
    }

    public void marcarDuplicado(String mensaje, Expediente duplicado) {
        addInfo(mensaje);
        estadoValidacion = ESTADO_DUPLICADO;
        expedienteDuplicado = duplicado;
    }

    public Date getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Date fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getFechaSolicitudTexto() {
        return fechaSolicitudTexto;
    }

    public void setFechaSolicitudTexto(String fechaSolicitudTexto) {
        this.fechaSolicitudTexto = fechaSolicitudTexto;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public int getIdTipoSolicitud() {
        return idTipoSolicitud;
    }

    public void setIdTipoSolicitud(int idTipoSolicitud) {
        this.idTipoSolicitud = idTipoSolicitud;
    }

    public String getProcedimientoRegistral() {
        return procedimientoRegistral;
    }

    public void setProcedimientoRegistral(String procedimientoRegistral) {
        this.procedimientoRegistral = procedimientoRegistral;
    }

    public int getIdProcedimientoRegistral() {
        return idProcedimientoRegistral;
    }

    public void setIdProcedimientoRegistral(int idProcedimientoRegistral) {
        this.idProcedimientoRegistral = idProcedimientoRegistral;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public int getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(int idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNumeroDocumentoPersistente() {
        return numeroDocumentoPersistente;
    }

    public void setNumeroDocumentoPersistente(String numeroDocumentoPersistente) {
        this.numeroDocumentoPersistente = numeroDocumentoPersistente;
    }

    public String getTipoActa() {
        return tipoActa;
    }

    public void setTipoActa(String tipoActa) {
        this.tipoActa = tipoActa;
    }

    public int getIdTipoActa() {
        return idTipoActa;
    }

    public void setIdTipoActa(int idTipoActa) {
        this.idTipoActa = idTipoActa;
    }

    public String getNumeroActa() {
        return numeroActa;
    }

    public void setNumeroActa(String numeroActa) {
        this.numeroActa = numeroActa;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getDniTitularVisual() {
        return dniTitularVisual;
    }

    public void setDniTitularVisual(String dniTitularVisual) {
        this.dniTitularVisual = dniTitularVisual;
    }

    public String getDniTitularPersistente() {
        return dniTitularPersistente;
    }

    public void setDniTitularPersistente(String dniTitularPersistente) {
        this.dniTitularPersistente = dniTitularPersistente;
    }

    public String getTitular2() {
        return titular2;
    }

    public void setTitular2(String titular2) {
        this.titular2 = titular2;
    }

    public String getSolicitadoPor() {
        return solicitadoPor;
    }

    public void setSolicitadoPor(String solicitadoPor) {
        this.solicitadoPor = solicitadoPor;
    }

    public String getDniSolicitanteVisual() {
        return dniSolicitanteVisual;
    }

    public void setDniSolicitanteVisual(String dniSolicitanteVisual) {
        this.dniSolicitanteVisual = dniSolicitanteVisual;
    }

    public String getDniSolicitantePersistente() {
        return dniSolicitantePersistente;
    }

    public void setDniSolicitantePersistente(String dniSolicitantePersistente) {
        this.dniSolicitantePersistente = dniSolicitantePersistente;
    }

    public Expediente getExpedienteDuplicado() {
        return expedienteDuplicado;
    }

    public String getEstadoCarga() {
        return estadoCarga;
    }

    public String getObservacionCarga() {
        return observacionCarga;
    }

    public void setResultadoCarga(String estadoCarga, String observacionCarga) {
        this.estadoCarga = estadoCarga;
        this.observacionCarga = observacionCarga == null ? "" : observacionCarga.trim();
    }
}
