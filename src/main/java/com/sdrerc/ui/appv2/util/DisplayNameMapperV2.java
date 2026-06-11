package com.sdrerc.ui.appv2.util;

public final class DisplayNameMapperV2 {

    private DisplayNameMapperV2() {
    }

    public static String etapa(String codigo) {
        String normalized = normalize(codigo);
        switch (normalized) {
            case "REGISTRO":
                return "Registro";
            case "ASIGNACION":
                return "Asignación";
            case "ANALISIS":
                return "Análisis";
            case "VERIFICACION":
                return "Verificación";
            case "FIRMA_EMISION":
                return "Firma / Emisión";
            case "EJECUCION":
                return "Ejecución";
            case "NOTIFICACION":
                return "Notificación";
            case "PUBLICACION_CONDICIONAL":
                return "Publicación";
            case "EXPEDIENTE_DIGITAL":
                return "Expediente digital";
            case "CIERRE_ARCHIVO":
                return "Cierre / Archivo";
            default:
                return humanize(codigo);
        }
    }

    public static String estado(String codigo) {
        String normalized = normalize(codigo);
        switch (normalized) {
            case "REGISTRADO":
                return "Registrado";
            case "ASIGNADO":
                return "Asignado";
            case "RECIBIDO_POR_ABOGADO":
                return "Recibido por abogado";
            case "ATENDIDO":
                return "Atendido";
            case "OBSERVADO":
                return "Observado";
            case "SUBSANADO":
                return "Subsanado";
            case "EN_ABANDONO":
                return "En abandono";
            case "OBSERVACION_ADMINISTRATIVA":
                return "Observación administrativa";
            case "NO_CORRESPONDE":
                return "No corresponde";
            case "EN_VERIFICACION":
                return "En verificación";
            case "REQUIERE_CORRECCION":
                return "Requiere corrección";
            case "DOCUMENTO_INCONSISTENTE":
                return "Documento inconsistente";
            case "VERIFICADO":
                return "Verificado";
            case "PARA_FIRMA":
                return "Para firma";
            case "FIRMADO":
                return "Firmado";
            case "EMITIDO":
                return "Emitido";
            case "RESOLUCION_NUMERADA":
                return "Resolución numerada";
            case "EN_EJECUCION":
                return "En ejecución";
            case "INDAGATORIO":
                return "Indagatorio";
            case "EJECUTADO":
                return "Ejecutado";
            case "EN_NOTIFICACION":
                return "En notificación";
            case "CARGO_PENDIENTE":
                return "Cargo pendiente";
            case "CARGO_RECIBIDO":
                return "Cargo recibido";
            case "NOTIFICADO":
                return "Notificado";
            case "REQUIERE_PUBLICACION":
                return "Requiere publicación";
            case "PENDIENTE_PUBLICACION":
                return "Pendiente de publicación";
            case "PUBLICACION_REGISTRADA":
                return "Publicación registrada";
            case "CARPETA_CREADA":
                return "Carpeta creada";
            case "LINK_REGISTRADO":
                return "Enlace registrado";
            case "EXPEDIENTE_DIGITAL_COMPLETO":
                return "Expediente digital completo";
            case "CERRADO":
                return "Cerrado";
            case "ARCHIVADO":
                return "Archivado";
            case "DERIVACION_EXTERNA_PENDIENTE":
                return "Derivación externa pendiente";
            default:
                return humanize(codigo);
        }
    }

    public static String accion(String codigo) {
        String normalized = normalize(codigo);
        switch (normalized) {
            case "IMPORTACION_CARGA_DIARIA":
                return "Importación de carga diaria";
            case "RECEPCION_DOCUMENTO":
                return "Recepción de documento";
            case "ASIGNACION_ABOGADO":
                return "Asignación de abogado";
            case "ASOCIACION_DUPLICADO":
                return "Asociación de duplicado";
            case "ASOCIACION_DOCUMENTO_EXPEDIENTE":
                return "Asociación de documento al expediente";
            case "DOCUMENTO_DUPLICADO_ASOCIADO":
                return "Documento duplicado asociado";
            case "MISMA_ACTA_TITULAR":
                return "Misma acta y titular";
            case "RECEPCION_ASIGNACION":
                return "Recepción de asignación";
            case "REGISTRO_RESULTADO_ANALISIS":
                return "Registro de análisis";
            case "ENVIO_VERIFICACION":
            case "REENVIO_VERIFICACION":
                return "Envío a verificación";
            case "APROBACION_VERIFICACION":
                return "Aprobación de verificación";
            case "ENVIO_FIRMA":
                return "Envío a firma";
            case "FIRMA_DOCUMENTO":
                return "Firma de documento";
            case "REGISTRO_NUMERO_RESOLUCION":
                return "Registro de número de resolución";
            case "INICIO_EJECUCION":
                return "Inicio de ejecución";
            case "DERIVACION_A_NOTIFICACION":
                return "Derivación a notificación";
            case "NOTIFICACION_VIRTUAL":
                return "Notificación virtual";
            case "NOTIFICACION_PRESENCIAL_1":
                return "Primera notificación presencial";
            case "NOTIFICACION_PRESENCIAL_2":
                return "Segunda notificación presencial";
            case "RECEPCION_CARGO_ACUSE":
                return "Recepción de cargo de acuse";
            case "CONFIRMACION_NOTIFICACION":
                return "Confirmación de notificación";
            case "REGISTRO_NOTIFICACION_FALLIDA":
                return "Registro de notificación fallida";
            case "GENERACION_PUBLICACION":
                return "Generación de publicación";
            case "REGISTRO_PUBLICACION":
                return "Registro de publicación";
            case "CREACION_CARPETA_EXPEDIENTE_DIGITAL":
                return "Creación de carpeta digital";
            case "CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL":
                return "Carga de documentos digitales";
            case "CIERRE":
                return "Cierre";
            case "ARCHIVO":
                return "Archivo";
            case "DEVOLUCION_A_ANALISIS":
                return "Devolución a análisis";
            case "REVERSION_ESTADO_DOCUMENTO":
            case "REVERSION_ESTADO_DOCUMENTO_EJECUCION":
                return "Revisión por documento inconsistente";
            default:
                return humanize(codigo);
        }
    }

    public static String valor(String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return "";
        }
        if (esEtapa(normalized)) {
            return etapa(value);
        }
        if (esEstadoConocido(normalized) || esCodigoTecnico(normalized)) {
            return estado(value);
        }
        return value;
    }

    public static boolean estadoObservado(String estado) {
        String normalized = normalize(estado);
        return normalized.contains("OBSERVADO")
                || normalized.contains("REQUIERE_CORRECCION")
                || normalized.contains("DOCUMENTO_INCONSISTENTE");
    }

    private static String humanize(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return "-";
        }
        String[] parts = codigo.trim().toLowerCase().split("_+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }
        return result.length() == 0 ? codigo : result.toString();
    }

    private static String normalize(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    private static boolean esEtapa(String normalized) {
        return "REGISTRO".equals(normalized)
                || "ASIGNACION".equals(normalized)
                || "ANALISIS".equals(normalized)
                || "VERIFICACION".equals(normalized)
                || "FIRMA_EMISION".equals(normalized)
                || "EJECUCION".equals(normalized)
                || "NOTIFICACION".equals(normalized)
                || "PUBLICACION_CONDICIONAL".equals(normalized)
                || "EXPEDIENTE_DIGITAL".equals(normalized)
                || "CIERRE_ARCHIVO".equals(normalized);
    }

    private static boolean esCodigoTecnico(String normalized) {
        return normalized.indexOf('_') >= 0 && normalized.equals(normalized.toUpperCase());
    }

    private static boolean esEstadoConocido(String normalized) {
        return "REGISTRADO".equals(normalized)
                || "ASIGNADO".equals(normalized)
                || "ATENDIDO".equals(normalized)
                || "OBSERVADO".equals(normalized)
                || "SUBSANADO".equals(normalized)
                || "VERIFICADO".equals(normalized)
                || "FIRMADO".equals(normalized)
                || "EMITIDO".equals(normalized)
                || "INDAGATORIO".equals(normalized)
                || "EJECUTADO".equals(normalized)
                || "NOTIFICADO".equals(normalized)
                || "CERRADO".equals(normalized)
                || "ARCHIVADO".equals(normalized);
    }
}
