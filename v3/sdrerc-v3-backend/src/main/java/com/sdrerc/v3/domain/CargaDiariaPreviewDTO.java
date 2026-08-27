package com.sdrerc.v3.domain;

import com.sdrerc.v3.domain.rules.ProcedimientoRegistralRules;
import java.time.LocalDate;

/**
 * Port literal de com.sdrerc.domain.dto.sdrercapp.CargaDiariaPreviewDTO (V2): una fila de la
 * previsualización de Carga diaria, desde que se parsea el Excel/CSV hasta que queda registrada.
 * Mismos campos, mismas normalizaciones en los setters.
 */
public class CargaDiariaPreviewDTO {

    private int fila;
    private String numeroTramite;
    private String numeroDocumento;
    private String tipoProcedimiento;
    private String tipoSolicitud;
    private String tipoDocumento;
    private String tipoActa;
    private String numeroActa;
    private String acta;
    private String titular;
    private String remitente;
    // Campos opcionales estructurados (14/08/2026, Fase 5 "híbrida" de la iniciativa separar
    // Titular/Solicitante en 3 campos, ver v3/CLAUDE.md): la plantilla oficial distribuida hoy NO
    // los tiene — solo se llenan si el archivo cargado trae columnas adicionales reconocidas por
    // alias (CargaDiariaArchivoParserService). Cuando están presentes, `titular`/`remitente` se
    // recalculan a partir de ellos (misma combinación "PATERNO MATERNO, Nombres" que el resto del
    // sistema), así que todo el resto del pipeline (duplicados, grupo familiar, validación) sigue
    // funcionando exactamente igual sin tocar ningún otro campo.
    private String apellidoPaternoTitular;
    private String apellidoMaternoTitular;
    private String nombresTitular;
    private String apellidoPaternoSolicitante;
    private String apellidoMaternoSolicitante;
    private String nombresSolicitante;
    // Solicitante con 3 tipos (14/08/2026, Fase 3 de la iniciativa "Solicitante con 3 tipos", ver
    // v3/CLAUDE.md) — REDISEÑADO el mismo día (pedido explícito del usuario): no hay columna de
    // Excel para el tipo/centro/unidad. "SOLICITADO POR" (remitente) sigue siendo el único campo de
    // entrada; el sistema IDENTIFICA AUTOMÁTICAMENTE el tipo comparando ese texto contra
    // CENTROS_ATENCION.denominacion/UNIDADES_ORGANICAS.nombre (coincidencia por contención,
    // normalizada, ver CargaDiariaReglasService.clasificarTipoSolicitante) durante "Validar" — nunca
    // durante el parseo del archivo (el parser no tiene acceso a BD). Si no matchea con ninguna de
    // las 2 tablas, tipoSolicitante queda CIUDADANO por defecto y remitente se guarda tal cual se
    // escribió en el Excel, sin ninguna alteración.
    private String tipoSolicitante;
    // 18/08/2026, pedido explícito del usuario: combo editable de "Tipo solicitante" en la
    // previsualización, para corregir manualmente una clasificación automática incorrecta. Sin este
    // flag, el siguiente "Validar" volvería a ejecutar `clasificarTipoSolicitante` desde cero (que
    // corre incondicionalmente en cada validación, ver CargaDiariaReglasService) y descartaría la
    // elección manual del usuario. `false` por defecto: el parseo inicial y `clasificarTipoSolicitante`
    // nunca lo tocan a `true`, solo lo hace el cliente cuando edita esta columna a mano.
    private boolean tipoSolicitanteManual;
    private Long idCentroAtencion;
    private String centroAtencionDenominacion;
    private Long idUnidadOrganica;
    private String unidadOrganicaNombre;
    // 4to tipo de solicitante "Municipalidad" (14/08/2026, Fase 3, ver v3/CLAUDE.md), mismo criterio
    // que Centro de Atención/Unidad Orgánica: calculado por CargaDiariaReglasService.clasificarTipoSolicitante,
    // nunca leído del Excel.
    private Long idMunicipalidad;
    private String municipalidadDenominacion;
    private String canalRecepcion;
    private String numeroExpedienteSgd;
    private String tipoDocumentoIdentidadSolicitante;
    private String numeroDocumentoIdentidadSolicitante;
    private String tipoDocumentoIdentidadTitular;
    private String numeroDocumentoIdentidadTitular;
    private boolean grupoFamiliar;
    private String criterioGrupoFamiliar;
    private String observacionGrupoFamiliar;
    private LocalDate fechaRecepcion;
    private String fechaRecepcionTexto;
    private String observacionInicial;
    private String estadoValidacion;
    private String mensajeValidacion;
    private boolean posibleDuplicado;
    private String motivoDuplicado;
    private String motivoSinNumero;
    private String numeroExpedienteGenerado;
    private boolean listoParaRegistrar;
    private boolean registrado;
    private Long idExpedienteRegistrado;

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public String getNumeroTramite() {
        return numeroTramite;
    }

    public void setNumeroTramite(String numeroTramite) {
        this.numeroTramite = trimToNull(numeroTramite);
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = trimToNull(numeroDocumento);
    }

    public String getTipoProcedimiento() {
        return tipoProcedimiento;
    }

    public void setTipoProcedimiento(String tipoProcedimiento) {
        this.tipoProcedimiento = ProcedimientoRegistralRules.nombreCanonico(tipoProcedimiento);
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = normalizarTipoSolicitud(tipoSolicitud);
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = trimToNull(tipoDocumento);
    }

    public String getTipoActa() {
        return tipoActa;
    }

    public void setTipoActa(String tipoActa) {
        this.tipoActa = trimToNull(tipoActa);
    }

    public String getNumeroActa() {
        return numeroActa != null ? numeroActa : acta;
    }

    public void setNumeroActa(String numeroActa) {
        this.numeroActa = trimToNull(numeroActa);
        this.acta = this.numeroActa;
    }

    public String getActa() {
        return getNumeroActa();
    }

    public void setActa(String acta) {
        setNumeroActa(acta);
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = trimToNull(titular);
    }

    public String getApellidoPaternoTitular() {
        return apellidoPaternoTitular;
    }

    public void setApellidoPaternoTitular(String apellidoPaternoTitular) {
        this.apellidoPaternoTitular = trimToNull(apellidoPaternoTitular);
    }

    public String getApellidoMaternoTitular() {
        return apellidoMaternoTitular;
    }

    public void setApellidoMaternoTitular(String apellidoMaternoTitular) {
        this.apellidoMaternoTitular = trimToNull(apellidoMaternoTitular);
    }

    public String getNombresTitular() {
        return nombresTitular;
    }

    public void setNombresTitular(String nombresTitular) {
        this.nombresTitular = trimToNull(nombresTitular);
    }

    public String getApellidoPaternoSolicitante() {
        return apellidoPaternoSolicitante;
    }

    public void setApellidoPaternoSolicitante(String apellidoPaternoSolicitante) {
        this.apellidoPaternoSolicitante = trimToNull(apellidoPaternoSolicitante);
    }

    public String getApellidoMaternoSolicitante() {
        return apellidoMaternoSolicitante;
    }

    public void setApellidoMaternoSolicitante(String apellidoMaternoSolicitante) {
        this.apellidoMaternoSolicitante = trimToNull(apellidoMaternoSolicitante);
    }

    public String getNombresSolicitante() {
        return nombresSolicitante;
    }

    public void setNombresSolicitante(String nombresSolicitante) {
        this.nombresSolicitante = trimToNull(nombresSolicitante);
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = trimToNull(remitente);
    }

    public String getTipoSolicitante() {
        return tipoSolicitante;
    }

    public void setTipoSolicitante(String tipoSolicitante) {
        this.tipoSolicitante = trimToNull(tipoSolicitante);
    }

    /** CIUDADANO por defecto antes de que CargaDiariaReglasService calcule la clasificación real. */
    public String getTipoSolicitanteEfectivo() {
        return tipoSolicitante == null ? "CIUDADANO" : tipoSolicitante;
    }

    public boolean isTipoSolicitanteManual() {
        return tipoSolicitanteManual;
    }

    public void setTipoSolicitanteManual(boolean tipoSolicitanteManual) {
        this.tipoSolicitanteManual = tipoSolicitanteManual;
    }

    public Long getIdCentroAtencion() {
        return idCentroAtencion;
    }

    public void setIdCentroAtencion(Long idCentroAtencion) {
        this.idCentroAtencion = idCentroAtencion;
    }

    public String getCentroAtencionDenominacion() {
        return centroAtencionDenominacion;
    }

    public void setCentroAtencionDenominacion(String centroAtencionDenominacion) {
        this.centroAtencionDenominacion = trimToNull(centroAtencionDenominacion);
    }

    public Long getIdUnidadOrganica() {
        return idUnidadOrganica;
    }

    public void setIdUnidadOrganica(Long idUnidadOrganica) {
        this.idUnidadOrganica = idUnidadOrganica;
    }

    public String getUnidadOrganicaNombre() {
        return unidadOrganicaNombre;
    }

    public void setUnidadOrganicaNombre(String unidadOrganicaNombre) {
        this.unidadOrganicaNombre = trimToNull(unidadOrganicaNombre);
    }

    public Long getIdMunicipalidad() {
        return idMunicipalidad;
    }

    public void setIdMunicipalidad(Long idMunicipalidad) {
        this.idMunicipalidad = idMunicipalidad;
    }

    public String getMunicipalidadDenominacion() {
        return municipalidadDenominacion;
    }

    public void setMunicipalidadDenominacion(String municipalidadDenominacion) {
        this.municipalidadDenominacion = trimToNull(municipalidadDenominacion);
    }

    public String getCanalRecepcion() {
        return canalRecepcion;
    }

    public void setCanalRecepcion(String canalRecepcion) {
        this.canalRecepcion = trimToNull(canalRecepcion);
    }

    public String getNumeroExpedienteSgd() {
        return numeroExpedienteSgd;
    }

    public void setNumeroExpedienteSgd(String numeroExpedienteSgd) {
        this.numeroExpedienteSgd = trimToNull(numeroExpedienteSgd);
    }

    public String getTipoDocumentoIdentidadSolicitante() {
        return tipoDocumentoIdentidadSolicitante;
    }

    public void setTipoDocumentoIdentidadSolicitante(String tipoDocumentoIdentidadSolicitante) {
        this.tipoDocumentoIdentidadSolicitante = normalizarTipoDocumentoIdentidad(tipoDocumentoIdentidadSolicitante);
    }

    public String getNumeroDocumentoIdentidadSolicitante() {
        return numeroDocumentoIdentidadSolicitante;
    }

    public void setNumeroDocumentoIdentidadSolicitante(String numeroDocumentoIdentidadSolicitante) {
        this.numeroDocumentoIdentidadSolicitante = normalizarNumeroIdentidad(numeroDocumentoIdentidadSolicitante);
    }

    public String getTipoDocumentoIdentidadTitular() {
        return tipoDocumentoIdentidadTitular;
    }

    public void setTipoDocumentoIdentidadTitular(String tipoDocumentoIdentidadTitular) {
        this.tipoDocumentoIdentidadTitular = normalizarTipoDocumentoIdentidad(tipoDocumentoIdentidadTitular);
    }

    public String getNumeroDocumentoIdentidadTitular() {
        return numeroDocumentoIdentidadTitular;
    }

    public void setNumeroDocumentoIdentidadTitular(String numeroDocumentoIdentidadTitular) {
        this.numeroDocumentoIdentidadTitular = normalizarNumeroIdentidad(numeroDocumentoIdentidadTitular);
    }

    public boolean isGrupoFamiliar() {
        return grupoFamiliar;
    }

    public void setGrupoFamiliar(boolean grupoFamiliar) {
        this.grupoFamiliar = grupoFamiliar;
    }

    public String getCriterioGrupoFamiliar() {
        return criterioGrupoFamiliar;
    }

    public void setCriterioGrupoFamiliar(String criterioGrupoFamiliar) {
        this.criterioGrupoFamiliar = trimToNull(criterioGrupoFamiliar);
    }

    public String getObservacionGrupoFamiliar() {
        return observacionGrupoFamiliar;
    }

    public void setObservacionGrupoFamiliar(String observacionGrupoFamiliar) {
        this.observacionGrupoFamiliar = trimToNull(observacionGrupoFamiliar);
    }

    public boolean isPosibleGrupoFamiliar() {
        return !grupoFamiliar && (hasText(criterioGrupoFamiliar) || hasText(observacionGrupoFamiliar));
    }

    public String getGrupoFamiliarTexto() {
        return grupoFamiliar ? "Sí" : "No";
    }

    public void limpiarDeteccionGrupoFamiliar() {
        if (criterioGrupoFamiliar != null && criterioGrupoFamiliar.startsWith("COINCIDENCIA_APELLIDOS")) {
            criterioGrupoFamiliar = null;
        }
        if (observacionGrupoFamiliar != null) {
            StringBuilder limpio = new StringBuilder();
            String[] partes = observacionGrupoFamiliar.split("\\|");
            for (String parte : partes) {
                String texto = trimToNull(parte);
                if (texto == null || texto.startsWith("Posible grupo familiar")) {
                    continue;
                }
                if (limpio.length() > 0) {
                    limpio.append(" | ");
                }
                limpio.append(texto);
            }
            observacionGrupoFamiliar = limpio.length() == 0 ? null : limpio.toString();
        }
    }

    public void agregarObservacionGrupoFamiliar(String criterio, String observacion) {
        if (!hasText(criterioGrupoFamiliar)) {
            setCriterioGrupoFamiliar(criterio);
        }
        String texto = trimToNull(observacion);
        if (texto == null) {
            return;
        }
        if (observacionGrupoFamiliar == null || observacionGrupoFamiliar.trim().isEmpty()) {
            observacionGrupoFamiliar = texto;
        } else if (!observacionGrupoFamiliar.contains(texto)) {
            observacionGrupoFamiliar = observacionGrupoFamiliar + " | " + texto;
        }
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(LocalDate fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getFechaRecepcionTexto() {
        return fechaRecepcionTexto;
    }

    public void setFechaRecepcionTexto(String fechaRecepcionTexto) {
        this.fechaRecepcionTexto = trimToNull(fechaRecepcionTexto);
    }

    public String getObservacionInicial() {
        return observacionInicial;
    }

    public void setObservacionInicial(String observacionInicial) {
        this.observacionInicial = trimToNull(observacionInicial);
    }

    public String getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(String estadoValidacion) {
        this.estadoValidacion = trimToNull(estadoValidacion);
    }

    public String getMensajeValidacion() {
        return mensajeValidacion;
    }

    public void setMensajeValidacion(String mensajeValidacion) {
        this.mensajeValidacion = trimToNull(mensajeValidacion);
    }

    public boolean isPosibleDuplicado() {
        return posibleDuplicado;
    }

    public void setPosibleDuplicado(boolean posibleDuplicado) {
        this.posibleDuplicado = posibleDuplicado;
    }

    public String getMotivoDuplicado() {
        return motivoDuplicado;
    }

    public void setMotivoDuplicado(String motivoDuplicado) {
        this.motivoDuplicado = trimToNull(motivoDuplicado);
    }

    public String getNumeroExpedienteGenerado() {
        return numeroExpedienteGenerado;
    }

    public void setNumeroExpedienteGenerado(String numeroExpedienteGenerado) {
        this.numeroExpedienteGenerado = trimToNull(numeroExpedienteGenerado);
    }

    public String getMotivoSinNumero() {
        return motivoSinNumero;
    }

    public void setMotivoSinNumero(String motivoSinNumero) {
        this.motivoSinNumero = trimToNull(motivoSinNumero);
    }

    public boolean isListoParaRegistrar() {
        return listoParaRegistrar;
    }

    public void setListoParaRegistrar(boolean listoParaRegistrar) {
        this.listoParaRegistrar = listoParaRegistrar;
    }

    public boolean isRegistrado() {
        return registrado;
    }

    public void setRegistrado(boolean registrado) {
        this.registrado = registrado;
    }

    public Long getIdExpedienteRegistrado() {
        return idExpedienteRegistrado;
    }

    public void setIdExpedienteRegistrado(Long idExpedienteRegistrado) {
        this.idExpedienteRegistrado = idExpedienteRegistrado;
    }

    public void reiniciarValidacion() {
        estadoValidacion = "Pendiente";
        mensajeValidacion = null;
        posibleDuplicado = false;
        motivoDuplicado = null;
        motivoSinNumero = null;
        numeroExpedienteGenerado = null;
        listoParaRegistrar = false;
        registrado = false;
        idExpedienteRegistrado = null;
    }

    public void agregarMensaje(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return;
        }
        if (mensajeValidacion == null || mensajeValidacion.trim().isEmpty()) {
            mensajeValidacion = mensaje.trim();
        } else {
            mensajeValidacion = mensajeValidacion + " | " + mensaje.trim();
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizarTipoSolicitud(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        if ("PARTE".equalsIgnoreCase(trimmed)) {
            return "Parte";
        }
        if ("OFICIO".equalsIgnoreCase(trimmed)) {
            return "Oficio";
        }
        return trimmed;
    }

    private static String normalizarTipoDocumentoIdentidad(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        String upper = trimmed.toUpperCase();
        if ("CARNET DE EXTRANJERIA".equals(upper) || "CARNÉ DE EXTRANJERÍA".equals(upper)) {
            return "CE";
        }
        if ("PASAPORTE".equals(upper)) {
            return "PASAPORTE";
        }
        return upper;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String normalizarNumeroIdentidad(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        return trimmed.toUpperCase();
    }
}
