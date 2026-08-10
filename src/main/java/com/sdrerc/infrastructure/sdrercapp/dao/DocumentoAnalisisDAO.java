package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.AsignacionCartaRespuestaDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO;
import com.sdrerc.domain.dto.sdrercapp.PlazoConfiguracionDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocumentoAnalisisDAO {

    private static final String CODIGO_MOVIMIENTO_ASIGNACION_NOTIFICACION = "ASIGNACION_NOTIFICACION";
    private static final String CODIGO_MOVIMIENTO_REASIGNACION_NOTIFICACION = "REASIGNACION_NOTIFICACION";
    private static final String CODIGO_MOVIMIENTO_DEVOLUCION_EJECUCION = "DEVOLUCION_A_EJECUCION";
    private static final String CODIGO_MOVIMIENTO_SUPERVISION_EMISION_NOTIFICACION = "SUPERVISION_EMISION_NOTIFICACION";
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final CalendarioLaboralService calendarioLaboralService = new CalendarioLaboralService();
    private final PlazoConfiguracionDAO plazoConfiguracionDAO = new PlazoConfiguracionDAO();

    public DocumentoAnalisisDAO() {
        this(new CatalogoLookupDAO());
    }

    public DocumentoAnalisisDAO(CatalogoLookupDAO catalogoLookupDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public List<CatalogoItemDTO> listarTiposDocumentoAnalizado() throws SQLException {
        return catalogoLookupDAO.listarTiposDocumentoAdjuntoAnalisis();
    }

    public Set<String> listarCodigosTipoDocumentoIntermedio() throws SQLException {
        Set<String> codigos = new HashSet<String>();
        try (Connection conn = SdrercAppConnection.getConnection()) {
            if (!soportaClasificacionTipoDocumento(conn)) {
                return codigos;
            }
            String sql = "SELECT codigo FROM tipo_documento_adjunto "
                    + "WHERE activo = 1 AND UPPER(clasificacion) = 'INTERMEDIO'";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    codigos.add(rs.getString("codigo"));
                }
            }
        }
        return codigos;
    }

    public List<CatalogoItemDTO> listarEstadosDocumento() throws SQLException {
        return filtrarEstadosDocumentoAnalizado(catalogoLookupDAO.listarEstadosDocumento());
    }

    /**
     * Catalogo para las columnas "Estado documento" de Notificacion (mini-panel "Emision" de la
     * Bandeja Asignacion y grilla de la Bandeja Validacion): Emitido/En despacho/Observado/
     * Validado, a diferencia de {@link #listarEstadosDocumento()} (usado por Analisis/Ejecucion),
     * que incluye "En proyecto" en vez de "Validado" porque ese estado no aplica en esas etapas.
     */
    public List<CatalogoItemDTO> listarEstadosDocumentoNotificacion() throws SQLException {
        List<CatalogoItemDTO> estados = catalogoLookupDAO.listarEstadosDocumento();
        List<CatalogoItemDTO> filtrados = new ArrayList<CatalogoItemDTO>();
        for (CatalogoItemDTO estado : estados) {
            if (estado != null && ("EMITIDO".equalsIgnoreCase(estado.getCodigo())
                    || "EN_DESPACHO".equalsIgnoreCase(estado.getCodigo())
                    || "OBSERVADO".equalsIgnoreCase(estado.getCodigo())
                    || "VALIDADO".equalsIgnoreCase(estado.getCodigo()))) {
                filtrados.add(estado);
            }
        }
        return filtrados;
    }

    public List<CatalogoItemDTO> listarResultadosValidacion() throws SQLException {
        return catalogoLookupDAO.listarResultadosValidacion();
    }

    public List<DocumentoAnalizadoDTO> listarPorExpediente(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return new ArrayList<DocumentoAnalizadoDTO>();
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return listarPorExpediente(conn, idExpediente);
        }
    }

    public List<AsignacionCartaRespuestaDTO> listarCartasRespuestaPendientes() throws SQLException {
        return listarCartasRespuestaPendientes(null);
    }

    public List<AsignacionCartaRespuestaDTO> listarCartasRespuestaPendientes(Long idExpedienteFiltro) throws SQLException {
        List<AsignacionCartaRespuestaDTO> items = new ArrayList<AsignacionCartaRespuestaDTO>();
        try (Connection conn = SdrercAppConnection.getConnection()) {
            if (!soportaRespuestaDocumentoAnalizado(conn)) {
                return items;
            }
            boolean soportaPublicacion = soportaPublicacionPreparada(conn);
            boolean soportaNumeroDocumento = soportaNumeroDocumentoAnalizado(conn);
            boolean soportaClasificacion = soportaClasificacionTipoDocumento(conn);
            String sqlInterno = "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, "
                    + "esol.numero_expediente_sgd, "
                    + nombrePersona("p") + " AS titular, "
                    + "tda.codigo AS tipo_documento_codigo, tda.nombre AS tipo_documento_nombre, "
                    + "ed.nombre AS estado_documento_nombre, "
                    + "et.codigo AS etapa_codigo, "
                    + "da.fecha_documento, "
                    + (soportaNumeroDocumento
                            ? "da.numero_documento, "
                            : "CAST(NULL AS VARCHAR2(120)) AS numero_documento, ")
                    + "da.descripcion, NVL(da.requiere_respuesta, 0) AS requiere_respuesta, "
                    + "NVL(da.notificado, 0) AS notificado, da.fecha_acuse, da.confirmacion_respuesta, "
                    + "da.fecha_respuesta, da.numero_hoja_envio_respuesta "
                    + (soportaPublicacion
                            ? ", NVL(e.requiere_publicacion, 0) AS requiere_publicacion, "
                            + "(SELECT fecha_publicacion FROM ("
                            + " SELECT p2.fecha_publicacion FROM expediente_publicacion p2 "
                            + " WHERE p2.id_expediente = da.id_expediente AND p2.activo = 1 "
                            + " ORDER BY p2.creado_en DESC, p2.id_expediente_publicacion DESC"
                            + ") WHERE ROWNUM = 1) AS fecha_publicacion_edicto "
                            : ", 0 AS requiere_publicacion, CAST(NULL AS DATE) AS fecha_publicacion_edicto ")
                    // Fecha Publ. Notif.: fecha del 3er "intento" (tipo_notificacion=PUBLICACION) de
                    // EXPEDIENTE_NOTIFICACION, solo si ya quedo EXITOSA (Publicado) en la Bandeja
                    // Publicacion de Notificacion; distinta de fecha_publicacion_edicto (arriba, de
                    // EXPEDIENTE_PUBLICACION, el modulo Publicacion standalone).
                    + ", (SELECT n2.fecha_envio FROM expediente_notificacion n2 "
                    + " JOIN tipo_notificacion tn2 ON tn2.id_tipo_notificacion = n2.id_tipo_notificacion "
                    + " JOIN estado_notificacion en2 ON en2.id_estado_notificacion = n2.id_estado_notificacion "
                    + " WHERE n2.id_documento_analizado = da.id_documento_analizado AND n2.activo = 1 "
                    + " AND UPPER(tn2.codigo) = 'PUBLICACION' AND UPPER(en2.codigo) = 'EXITOSA' AND ROWNUM = 1"
                    + ") AS fecha_publicacion_notif, "
                    + ESTADO_FINAL_NOTIFICACION_SQL + " "
                    + "FROM expediente_documento_analizado da "
                    + "JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1 "
                    + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                    + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                    + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                    + " AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                    + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                    + "LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                    + "WHERE da.activo = 1 "
                    + "AND NVL(da.requiere_respuesta, 0) = 1 "
                    + "AND NVL(da.notificado, 0) = 1 "
                    + (soportaClasificacion ? "AND UPPER(NVL(tda.clasificacion, '')) = 'INTERMEDIO' " : "")
                    + (idExpedienteFiltro != null ? "AND da.id_expediente = ? " : "");
            // Solo se muestra en la Bandeja Cartas de Respuesta cuando el estado final de la
            // notificacion ya es Atendido (ubicado en algun intento, o publicado en la Bandeja
            // Publicacion de Notificacion); pedido explicito del usuario (07/08/2026). Ademas, en
            // cuanto el expediente ya volvio a etapa ANALISIS (ya fue derivado via "Registrar
            // Asignacion") deja de mostrarse aqui: el seguimiento pasa a ser responsabilidad de
            // Analisis (ver KPI "Derivado" de esa bandeja), no queda "atendido" en Cartas de
            // Respuesta indefinidamente; pedido explicito del usuario (08/08/2026).
            String sql = "SELECT * FROM (" + sqlInterno + ") "
                    + "WHERE estado_final_notificacion_codigo = 'ATENDIDO' "
                    + "AND UPPER(etapa_codigo) <> 'ANALISIS' "
                    + "ORDER BY fecha_documento DESC NULLS LAST, id_documento_analizado DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (idExpedienteFiltro != null) {
                    ps.setLong(1, idExpedienteFiltro);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    Map<String, Integer> diasPlazoCache = new HashMap<String, Integer>();
                    while (rs.next()) {
                        String tipoDocumentoCodigo = rs.getString("tipo_documento_codigo");
                        String etapaCodigo = rs.getString("etapa_codigo");
                        LocalDate fechaAcuse = toLocalDate(rs.getDate("fecha_acuse"));
                        LocalDate fechaPublicacionEdicto = toLocalDate(rs.getDate("fecha_publicacion_edicto"));
                        LocalDate fechaPublicacionNotif = toLocalDate(rs.getDate("fecha_publicacion_notif"));

                        VencimientoCarta vencimiento = resolverVencimientoCarta(
                                conn, tipoDocumentoCodigo, fechaAcuse, fechaPublicacionNotif,
                                fechaPublicacionEdicto, diasPlazoCache);

                        items.add(new AsignacionCartaRespuestaDTO(
                                getLongOrNull(rs, "id_documento_analizado"),
                                getLongOrNull(rs, "id_expediente"),
                                rs.getString("numero_expediente"),
                                rs.getString("numero_expediente_sgd"),
                                rs.getString("titular"),
                                rs.getString("tipo_documento_nombre"),
                                rs.getString("estado_documento_nombre"),
                                toLocalDate(rs.getDate("fecha_documento")),
                                rs.getString("numero_documento"),
                                rs.getString("descripcion"),
                                rs.getInt("requiere_respuesta") == 1,
                                rs.getInt("notificado") == 1,
                                fechaAcuse,
                                rs.getString("confirmacion_respuesta"),
                                toLocalDate(rs.getDate("fecha_respuesta")),
                                rs.getString("numero_hoja_envio_respuesta"),
                                rs.getInt("requiere_publicacion") == 1,
                                fechaPublicacionEdicto,
                                fechaPublicacionNotif,
                                tipoDocumentoCodigo,
                                etapaCodigo,
                                vencimiento == null ? null : vencimiento.dias,
                                vencimiento == null ? null : vencimiento.diasPlazo,
                                vencimiento == null ? null : vencimiento.fecha,
                                vencimiento == null ? null : vencimiento.tipoAlerta));
                    }
                }
            }
        }
        return items;
    }

    private static final class VencimientoCarta {
        private final Long dias;
        private final Integer diasPlazo;
        private final LocalDate fecha;
        private final String tipoAlerta;

        private VencimientoCarta(Long dias, Integer diasPlazo, LocalDate fecha, String tipoAlerta) {
            this.dias = dias;
            this.diasPlazo = diasPlazo;
            this.fecha = fecha;
            this.tipoAlerta = tipoAlerta;
        }
    }

    /**
     * Alerta de vencimiento en cascada de una carta intermedia (ver AGENTS.md, "Alertas de
     * vencimiento de respuesta/publicación en Cartas de Respuesta"): mientras el documento no
     * tenga Fecha Publ. Edicto registrada, la alerta activa es el vencimiento de RESPUESTA, que
     * corre desde Fecha Acuse o, si no hay acuse directo (el ciudadano se ubicó vía la Bandeja
     * Publicación de Notificación en vez de un intento directo), desde Fecha Publ. Notif. — plazo
     * = código de tipo_documento_adjunto (30 días para Edicto/Precisar Pretensión/Falta Sustento,
     * 10 días para Indagatorio, ver `92_plazos_vencimiento_cartas_respuesta.sql`). En cuanto se
     * registra Fecha Publ. Edicto (hoy solo posible en Carta Edicto), la alerta cambia
     * automáticamente a PUBLICACION (desde Fecha Publ. Edicto, plazo = código +
     * "_PUBLICACION", 15 días para Edicto), sin esperar a que se derive el expediente a Análisis —
     * pedido explícito del usuario (07/08/2026). Si no hay plazo configurado para el tipo/etapa que
     * corresponda, no hay alerta (null) en vez de inventar un valor.
     */
    private VencimientoCarta resolverVencimientoCarta(
            Connection conn,
            String tipoDocumentoCodigo,
            LocalDate fechaAcuse,
            LocalDate fechaPublicacionNotif,
            LocalDate fechaPublicacionEdicto,
            Map<String, Integer> diasPlazoCache) throws SQLException {
        if (fechaPublicacionEdicto != null) {
            Integer diasPlazoPublicacion = resolverDiasPlazoCarta(
                    conn, tipoDocumentoCodigo + "_PUBLICACION", diasPlazoCache);
            if (diasPlazoPublicacion != null) {
                LocalDate fechaVencimiento = calendarioLaboralService.calcularFechaVencimientoHabil(
                        conn, fechaPublicacionEdicto, diasPlazoPublicacion.intValue());
                long dias = calendarioLaboralService.calcularDiasHabilesRestantes(conn, LocalDate.now(), fechaVencimiento);
                return new VencimientoCarta(Long.valueOf(dias), diasPlazoPublicacion, fechaVencimiento, "PUBLICACION");
            }
        }
        LocalDate fechaInicioRespuesta = fechaAcuse != null ? fechaAcuse : fechaPublicacionNotif;
        if (fechaInicioRespuesta != null) {
            Integer diasPlazoRespuesta = resolverDiasPlazoCarta(conn, tipoDocumentoCodigo, diasPlazoCache);
            if (diasPlazoRespuesta != null) {
                LocalDate fechaVencimiento = calendarioLaboralService.calcularFechaVencimientoHabil(
                        conn, fechaInicioRespuesta, diasPlazoRespuesta.intValue());
                long dias = calendarioLaboralService.calcularDiasHabilesRestantes(conn, LocalDate.now(), fechaVencimiento);
                return new VencimientoCarta(Long.valueOf(dias), diasPlazoRespuesta, fechaVencimiento, "RESPUESTA");
            }
        }
        return null;
    }

    /**
     * Días configurados en PLAZO_CONFIGURACION para el código de plazo de una carta intermedia
     * (el propio código de tipo_documento_adjunto para el plazo de respuesta, o ese código con
     * sufijo "_PUBLICACION" para el plazo de publicación del edicto). Cacheado por código dentro
     * de la misma consulta para no repetir el SELECT por cada fila. Null si no hay fila de plazo
     * configurada (tipo sin plazo, o "_PUBLICACION" para tipos que no publican edicto).
     */
    private Integer resolverDiasPlazoCarta(
            Connection conn, String codigoPlazo, Map<String, Integer> cache) throws SQLException {
        if (codigoPlazo == null) {
            return null;
        }
        if (cache.containsKey(codigoPlazo)) {
            return cache.get(codigoPlazo);
        }
        PlazoConfiguracionDTO plazo = plazoConfiguracionDAO.obtenerPlazoPorCodigo(conn, codigoPlazo);
        Integer dias = plazo == null || plazo.getDiasPlazo() == null || plazo.getDiasPlazo().intValue() <= 0
                ? null
                : plazo.getDiasPlazo();
        cache.put(codigoPlazo, dias);
        return dias;
    }

    private static final String CONDICION_ASIGNACION_NOTIFICACION =
            "((UPPER(NVL(eest.codigo, '')) = 'POR_ASIGNAR' "
            + "AND ((UPPER(NVL(tda.clasificacion, '')) = 'INTERMEDIO' AND UPPER(NVL(ed.codigo, '')) = 'EMITIDO') "
            // FINAL + EMITIDO: documento ya firmado tras volver Validado del validador (panel
            // "Emision" del segundo momento en JPanelNotificacionV2), a la espera de la segunda
            // Asignacion (esta vez a Eq. Notificacion). Sin esta rama el documento desaparecia de
            // las 3 bandejas de Notificacion en cuanto se guardaba la firma, sin forma de continuar.
            + "OR (UPPER(NVL(tda.clasificacion, '')) = 'FINAL' AND UPPER(NVL(ed.codigo, '')) IN ('EN_DESPACHO', 'VALIDADO', 'EMITIDO')))) "
            // Documento FINAL que el validador marco Observado: registrarResultadoValidacion no
            // mueve el estado del expediente (se queda en POR_VALIDAR), asi que sin esta rama el
            // documento queda invisible en las 3 bandejas de Notificacion. Reaparece aqui para que
            // el supervisor pueda derivarlo con el "Destino operativo" (Eq. Analisis/Eq. Ejecucion).
            + "OR (UPPER(NVL(eest.codigo, '')) = 'POR_VALIDAR' AND UPPER(NVL(ed.codigo, '')) = 'OBSERVADO' "
            + "AND UPPER(NVL(tda.clasificacion, '')) = 'FINAL'))";

    private static final String CONDICION_VALIDACION_NOTIFICACION =
            "(UPPER(NVL(eest.codigo, '')) = 'POR_VALIDAR' "
            + "AND UPPER(NVL(tda.clasificacion, '')) = 'FINAL' AND UPPER(NVL(ed.codigo, '')) = 'EN_DESPACHO')";

    private static final String CONDICION_BANDEJA_NOTIFICACION =
            "(UPPER(NVL(eest.codigo, '')) = 'EN_NOTIFICACION' "
            + "AND UPPER(NVL(tda.clasificacion, '')) IN ('INTERMEDIO', 'FINAL') AND UPPER(NVL(ed.codigo, '')) = 'EMITIDO')";

    // Estado Final del documento en la Bandeja Notificacion (4 estados, ver AGENTS.md):
    // POR_NOTIFICAR (sin intentos), ATENDIDO (algun intento EXITOSA/ubicado), POR_PUBLICAR
    // (intento 1 y 2 ambos FALLIDA/no ubicado) y PENDIENTE (resto de casos con intentos).
    // No requiere columna nueva: se deriva de expediente_notificacion + estado_notificacion.
    private static final String ESTADO_FINAL_NOTIFICACION_SQL =
            "(SELECT CASE "
            + "WHEN COUNT(*) = 0 THEN 'POR_NOTIFICAR' "
            + "WHEN MAX(CASE WHEN en3.codigo = 'EXITOSA' THEN 1 ELSE 0 END) = 1 THEN 'ATENDIDO' "
            + "WHEN MAX(CASE WHEN n3.numero_intento = 1 AND en3.codigo = 'FALLIDA' THEN 1 ELSE 0 END) = 1 "
            + "AND MAX(CASE WHEN n3.numero_intento = 2 AND en3.codigo = 'FALLIDA' THEN 1 ELSE 0 END) = 1 "
            + "THEN 'POR_PUBLICAR' "
            + "ELSE 'PENDIENTE' END "
            + "FROM expediente_notificacion n3 "
            + "JOIN estado_notificacion en3 ON en3.id_estado_notificacion = n3.id_estado_notificacion "
            + "WHERE n3.id_documento_analizado = da.id_documento_analizado AND n3.activo = 1) "
            + "AS estado_final_notificacion_codigo";

    // Bandera 0/1 independiente de estado_final_notificacion_codigo: "ambos intentos directos (1 y
    // 2) ya fallaron", sin importar si despues se registro o no un 3er intento de PUBLICACION ni el
    // resultado de este. A diferencia de estado_final_notificacion_codigo (que una vez que la
    // publicacion se marca EXITOSA pasa a ATENDIDO), esta bandera permanece en 1 para siempre una
    // vez que el documento entra al circuito de publicacion: se usa para que la Bandeja Notificacion
    // (3ra pestana) excluya el documento de forma definitiva y la Bandeja Publicacion (4ta pestana)
    // lo siga mostrando incluso despues de publicado (con su propio Estado Publicacion=Publicado),
    // en vez de que "rebote" de una bandeja a la otra segun si ya se publico o no; pedido explicito
    // del usuario (08/08/2026).
    private static final String AGOTO_INTENTOS_DIRECTOS_SQL =
            "(SELECT CASE WHEN "
            + "MAX(CASE WHEN n4.numero_intento = 1 AND en4.codigo = 'FALLIDA' THEN 1 ELSE 0 END) = 1 "
            + "AND MAX(CASE WHEN n4.numero_intento = 2 AND en4.codigo = 'FALLIDA' THEN 1 ELSE 0 END) = 1 "
            + "THEN 1 ELSE 0 END "
            + "FROM expediente_notificacion n4 "
            + "JOIN estado_notificacion en4 ON en4.id_estado_notificacion = n4.id_estado_notificacion "
            + "WHERE n4.id_documento_analizado = da.id_documento_analizado AND n4.activo = 1) "
            + "AS agoto_intentos_directos";

    // Fecha del intento de Publicación (tipo_notificacion=PUBLICACION) ya EXITOSA, expuesta a nivel
    // de documento (fila padre) para que la Bandeja Publicación pueda mostrarla sin necesitar
    // expandir el arbol; mismo patron/subconsulta ya usado por "Fecha Publ. Notif." de la Bandeja
    // Cartas de Respuesta (listarCartasRespuestaPendientes), aqui reutilizado como constante
    // compartida (09/08/2026, pedido explicito del usuario: "Fecha Publicación no muestra nada").
    private static final String FECHA_PUBLICACION_NOTIF_SQL =
            "(SELECT n5.fecha_envio FROM expediente_notificacion n5 "
            + "JOIN tipo_notificacion tn5 ON tn5.id_tipo_notificacion = n5.id_tipo_notificacion "
            + "JOIN estado_notificacion en5 ON en5.id_estado_notificacion = n5.id_estado_notificacion "
            + "WHERE n5.id_documento_analizado = da.id_documento_analizado AND n5.activo = 1 "
            + "AND UPPER(tn5.codigo) = 'PUBLICACION' AND UPPER(en5.codigo) = 'EXITOSA' AND ROWNUM = 1) "
            + "AS fecha_publicacion_notif";

    private static String nombreEstadoFinalNotificacion(String codigo) {
        if (codigo == null) {
            return "Por notificar";
        }
        switch (codigo.trim().toUpperCase()) {
            case "ATENDIDO":
                return "Atendido";
            case "POR_PUBLICAR":
                return "Por publicar";
            case "PENDIENTE":
                return "Pendiente";
            case "POR_NOTIFICAR":
            default:
                return "Por notificar";
        }
    }

    public List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> listarDocumentosAsignacionNotificacion() throws SQLException {
        // Bandeja de coordinacion (SUPERVISOR_NOTIFICACION asigna a validadores/notificadores):
        // no se filtra por visibilidad individual porque su funcion es ver y repartir toda la cola.
        return listarDocumentosNotificacionPareado(CONDICION_ASIGNACION_NOTIFICACION, false, true, null, null);
    }

    public List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> listarDocumentosValidacion(
            boolean esAdmin, Long idUsuarioActual, List<Long> idsEquipoActual) throws SQLException {
        return listarDocumentosNotificacionPareado(CONDICION_VALIDACION_NOTIFICACION, true, esAdmin, idUsuarioActual, idsEquipoActual);
    }

    /**
     * Bandeja Notificacion (3ra pestana): excluye de forma definitiva los documentos que ya
     * agotaron sus 2 intentos directos (ver AGOTO_INTENTOS_DIRECTOS_SQL) — esos pasan a mostrarse
     * exclusivamente en la Bandeja Publicacion (4ta pestana, ver listarDocumentosBandejaPublicacion),
     * incluso despues de que la publicacion ya se registro/marco Publicado (no "rebotan" de vuelta
     * a esta bandeja solo porque estado_final_notificacion_codigo haya pasado a ATENDIDO); pedido
     * explicito del usuario (08/08/2026).
     */
    public List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> listarDocumentosNotificacion(
            boolean esAdmin, Long idUsuarioActual, List<Long> idsEquipoActual) throws SQLException {
        return listarDocumentosNotificacionPareado(
                CONDICION_BANDEJA_NOTIFICACION, false, esAdmin, idUsuarioActual, idsEquipoActual, Boolean.FALSE);
    }

    /**
     * Bandeja Publicacion (4ta pestana de Notificacion): mismo universo de documentos que la
     * Bandeja Notificacion (CONDICION_BANDEJA_NOTIFICACION), acotado a los que ya agotaron el
     * intento 1 y 2 (AGOTO_INTENTOS_DIRECTOS_SQL = 1), sea que la publicacion ya se haya registrado
     * o no y sin importar su resultado (el documento permanece aqui aunque ya este Publicado, con
     * su propio Estado Publicacion mostrando ese resultado). Misma visibilidad por asignacion que
     * la Bandeja Notificacion.
     */
    public List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> listarDocumentosBandejaPublicacion(
            boolean esAdmin, Long idUsuarioActual, List<Long> idsEquipoActual) throws SQLException {
        return listarDocumentosNotificacionPareado(
                CONDICION_BANDEJA_NOTIFICACION, false, esAdmin, idUsuarioActual, idsEquipoActual, Boolean.TRUE);
    }

    public List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> listarDocumentosPublicacion() throws SQLException {
        return listarDocumentosNotificacionPorEstados(
                java.util.Arrays.asList("FINALIZADO"), java.util.Arrays.asList("INTERMEDIO", "FINAL"));
    }

    /**
     * Todos los documentos analizados principales (nivel 0, activos) de un expediente, sin filtrar
     * por clasificacion ni estado -- a diferencia de listarDocumentosNotificacionPorEstados (que
     * filtra la cola global de una bandeja). Usado por el panel "Firma" de la Bandeja Asignacion de
     * Notificacion para listar todos los documentos del expediente enfocado (FINAL e INTERMEDIO),
     * dejando que la UI decida cuales son editables segun clasificacion/estado por fila.
     */
    public List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> listarDocumentosPorExpediente(
            Long idExpediente) throws SQLException {
        List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items =
                new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
        if (idExpediente == null) {
            return items;
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            if (!soportaClasificacionTipoDocumento(conn)) {
                return items;
            }
            String sql = "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, "
                    + "esol.numero_expediente_sgd, tda.clasificacion, tda.nombre AS tipo_documento_nombre, "
                    + "da.numero_documento, da.fecha_documento, "
                    + nombrePersona("p") + " AS titular, "
                    + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                    + "e.fecha_vencimiento "
                    + "FROM expediente_documento_analizado da "
                    + "JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1 "
                    + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                    + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                    + " AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                    + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                    + "LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                    + "WHERE da.activo = 1 AND da.nivel = 0 AND da.id_expediente = ? "
                    + "ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpediente);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(new com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO(
                                getLongOrNull(rs, "id_documento_analizado"),
                                getLongOrNull(rs, "id_expediente"),
                                rs.getString("numero_expediente"),
                                rs.getString("numero_expediente_sgd"),
                                rs.getString("clasificacion"),
                                rs.getString("tipo_documento_nombre"),
                                rs.getString("numero_documento"),
                                toLocalDate(rs.getDate("fecha_documento")),
                                rs.getString("titular"),
                                rs.getString("estado_documento_codigo"),
                                rs.getString("estado_documento_nombre"),
                                0,
                                false,
                                "",
                                "",
                                "",
                                "",
                                toLocalDate(rs.getDate("fecha_vencimiento")),
                                null,
                                0,
                                "",
                                "",
                                null));
                    }
                }
            }
        }
        return items;
    }

    public boolean tieneDocumentoFinalEnDespacho(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return false;
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            if (!soportaClasificacionTipoDocumento(conn)) {
                return false;
            }
            String sql = "SELECT COUNT(*) FROM expediente_documento_analizado da "
                    + "JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                    + "WHERE da.id_expediente = ? AND da.activo = 1 AND da.nivel = 0 "
                    + "AND UPPER(NVL(tda.clasificacion, '')) = 'FINAL' AND UPPER(ed.codigo) = 'EN_DESPACHO'";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpediente);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        }
    }

    private List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> listarDocumentosNotificacionPorEstados(
            List<String> estadosCodigo, List<String> clasificaciones) throws SQLException {
        List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items =
                new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
        try (Connection conn = SdrercAppConnection.getConnection()) {
            if (!soportaClasificacionTipoDocumento(conn)) {
                return items;
            }
            boolean soportaIntentos = soportaIntentosNotificacionDocumento(conn);
            StringBuilder estadoPlaceholders = new StringBuilder();
            for (int i = 0; i < estadosCodigo.size(); i++) {
                estadoPlaceholders.append(i == 0 ? "?" : ", ?");
            }
            StringBuilder clasifPlaceholders = new StringBuilder();
            for (int i = 0; i < clasificaciones.size(); i++) {
                clasifPlaceholders.append(i == 0 ? "?" : ", ?");
            }
            String sql = "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, esol.numero_expediente_sgd, "
                    + "tda.clasificacion, tda.nombre AS tipo_documento_nombre, "
                    + "da.numero_documento, da.fecha_documento, "
                    + nombrePersona("p") + " AS titular, "
                    + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                    + "(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 "
                    + "AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, "
                    + "CASE WHEN da.id_usuario_notificacion IS NOT NULL THEN 1 ELSE 0 END AS asignado, "
                    + "da.numero_hoja_envio_notificacion, un.nombre_completo AS usuario_notificacion_actual, "
                    + "eest.codigo AS estado_expediente_codigo, eest.nombre AS estado_expediente_nombre, "
                    + "e.fecha_vencimiento, "
                    + (soportaIntentos
                            ? "(SELECT COUNT(*) FROM expediente_notificacion en2 "
                            + " WHERE en2.id_documento_analizado = da.id_documento_analizado AND en2.activo = 1) AS total_intentos, "
                            + ESTADO_FINAL_NOTIFICACION_SQL + " "
                            : "0 AS total_intentos, 'POR_NOTIFICAR' AS estado_final_notificacion_codigo ")
                    + "FROM expediente_documento_analizado da "
                    + "JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1 "
                    + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                    + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                    + " AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                    + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                    + "LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                    + "LEFT JOIN usuario un ON un.id_usuario = da.id_usuario_notificacion "
                    + "LEFT JOIN estado_expediente eest ON eest.id_estado = e.id_estado_actual "
                    + "WHERE da.activo = 1 "
                    + "AND UPPER(NVL(tda.clasificacion, '')) IN (" + clasifPlaceholders + ") "
                    + "AND UPPER(NVL(ed.codigo, '')) IN (" + estadoPlaceholders + ") "
                    + "ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int index = 1;
                for (String clasificacion : clasificaciones) {
                    ps.setString(index++, clasificacion);
                }
                for (String estadoCodigo : estadosCodigo) {
                    ps.setString(index++, estadoCodigo);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(new com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO(
                                getLongOrNull(rs, "id_documento_analizado"),
                                getLongOrNull(rs, "id_expediente"),
                                rs.getString("numero_expediente"),
                                rs.getString("numero_expediente_sgd"),
                                rs.getString("clasificacion"),
                                rs.getString("tipo_documento_nombre"),
                                rs.getString("numero_documento"),
                                toLocalDate(rs.getDate("fecha_documento")),
                                rs.getString("titular"),
                                rs.getString("estado_documento_codigo"),
                                rs.getString("estado_documento_nombre"),
                                rs.getInt("relaciones_confirmadas"),
                                rs.getInt("asignado") == 1,
                                rs.getString("numero_hoja_envio_notificacion"),
                                rs.getString("usuario_notificacion_actual"),
                                rs.getString("estado_expediente_codigo"),
                                rs.getString("estado_expediente_nombre"),
                                toLocalDate(rs.getDate("fecha_vencimiento")),
                                calendarioLaboralService.calcularDiasHabilesRestantes(
                                        conn, getLongOrNull(rs, "id_expediente"), rs.getDate("fecha_vencimiento")),
                                rs.getInt("total_intentos"),
                                rs.getString("estado_final_notificacion_codigo"),
                                nombreEstadoFinalNotificacion(rs.getString("estado_final_notificacion_codigo")),
                                null));
                    }
                }
            }
        }
        return items;
    }

    private List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> listarDocumentosNotificacionPareado(
            String condicionPareada,
            boolean soloAsignados,
            boolean esAdmin,
            Long idUsuarioActual,
            List<Long> idsEquipoActual) throws SQLException {
        return listarDocumentosNotificacionPareado(condicionPareada, soloAsignados, esAdmin, idUsuarioActual, idsEquipoActual, null);
    }

    /**
     * @param filtroAgotoIntentosDirectos si no es null, envuelve la consulta pareada en un
     *      SELECT * FROM (...) externo filtrado por AGOTO_INTENTOS_DIRECTOS_SQL: TRUE muestra solo
     *      documentos que ya agotaron sus 2 intentos directos (Bandeja Publicacion), FALSE excluye
     *      esos mismos documentos (Bandeja Notificacion). No se puede filtrar directamente en el
     *      WHERE interno porque esa columna es una subconsulta correlacionada del SELECT, no una
     *      columna real de las tablas del FROM.
     */
    private List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> listarDocumentosNotificacionPareado(
            String condicionPareada,
            boolean soloAsignados,
            boolean esAdmin,
            Long idUsuarioActual,
            List<Long> idsEquipoActual,
            Boolean filtroAgotoIntentosDirectos) throws SQLException {
        List<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO> items =
                new ArrayList<com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO>();
        try (Connection conn = SdrercAppConnection.getConnection()) {
            if (!soportaClasificacionTipoDocumento(conn)) {
                return items;
            }
            boolean soportaIntentos = soportaIntentosNotificacionDocumento(conn);
            List<Object> paramsVisibilidad = new ArrayList<Object>();
            String condicionVisibilidad = VisibilidadBandejaSql.construirCondicion(
                    paramsVisibilidad, esAdmin, idUsuarioActual, idsEquipoActual,
                    "da.id_usuario_notificacion", "da.id_equipo_notificacion");
            String sqlInterno = "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, esol.numero_expediente_sgd, "
                    + "tda.clasificacion, tda.nombre AS tipo_documento_nombre, "
                    + "da.numero_documento, da.fecha_documento, "
                    + nombrePersona("p") + " AS titular, "
                    + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                    + "(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 "
                    + "AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, "
                    + "CASE WHEN da.id_usuario_notificacion IS NOT NULL THEN 1 ELSE 0 END AS asignado, "
                    + "da.numero_hoja_envio_notificacion, un.nombre_completo AS usuario_notificacion_actual, "
                    + "eest.codigo AS estado_expediente_codigo, eest.nombre AS estado_expediente_nombre, "
                    + "e.fecha_vencimiento, "
                    + (soportaIntentos
                            ? "(SELECT COUNT(*) FROM expediente_notificacion en2 "
                            + " WHERE en2.id_documento_analizado = da.id_documento_analizado AND en2.activo = 1) AS total_intentos, "
                            + ESTADO_FINAL_NOTIFICACION_SQL + ", " + AGOTO_INTENTOS_DIRECTOS_SQL + ", "
                            + FECHA_PUBLICACION_NOTIF_SQL + " "
                            : "0 AS total_intentos, 'POR_NOTIFICAR' AS estado_final_notificacion_codigo, "
                            + "0 AS agoto_intentos_directos, CAST(NULL AS DATE) AS fecha_publicacion_notif ")
                    + "FROM expediente_documento_analizado da "
                    + "JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1 "
                    + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                    + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                    + " AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                    + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                    + "LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                    + "LEFT JOIN usuario un ON un.id_usuario = da.id_usuario_notificacion "
                    + "LEFT JOIN estado_expediente eest ON eest.id_estado = e.id_estado_actual "
                    + "WHERE da.activo = 1 "
                    + "AND " + condicionPareada + " "
                    + (soloAsignados ? "AND da.id_usuario_notificacion IS NOT NULL " : "")
                    + condicionVisibilidad;
            String sql = filtroAgotoIntentosDirectos == null
                    ? sqlInterno + "ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC"
                    : "SELECT * FROM (" + sqlInterno + ") pub "
                    + "WHERE pub.agoto_intentos_directos = ? "
                    + "ORDER BY pub.fecha_documento DESC NULLS LAST, pub.id_documento_analizado DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int index = 1;
                for (int i = 0; i < paramsVisibilidad.size(); i++) {
                    ps.setObject(index++, paramsVisibilidad.get(i));
                }
                if (filtroAgotoIntentosDirectos != null) {
                    ps.setInt(index++, filtroAgotoIntentosDirectos.booleanValue() ? 1 : 0);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(new com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO(
                                getLongOrNull(rs, "id_documento_analizado"),
                                getLongOrNull(rs, "id_expediente"),
                                rs.getString("numero_expediente"),
                                rs.getString("numero_expediente_sgd"),
                                rs.getString("clasificacion"),
                                rs.getString("tipo_documento_nombre"),
                                rs.getString("numero_documento"),
                                toLocalDate(rs.getDate("fecha_documento")),
                                rs.getString("titular"),
                                rs.getString("estado_documento_codigo"),
                                rs.getString("estado_documento_nombre"),
                                rs.getInt("relaciones_confirmadas"),
                                rs.getInt("asignado") == 1,
                                rs.getString("numero_hoja_envio_notificacion"),
                                rs.getString("usuario_notificacion_actual"),
                                rs.getString("estado_expediente_codigo"),
                                rs.getString("estado_expediente_nombre"),
                                toLocalDate(rs.getDate("fecha_vencimiento")),
                                calendarioLaboralService.calcularDiasHabilesRestantes(
                                        conn, getLongOrNull(rs, "id_expediente"), rs.getDate("fecha_vencimiento")),
                                rs.getInt("total_intentos"),
                                rs.getString("estado_final_notificacion_codigo"),
                                nombreEstadoFinalNotificacion(rs.getString("estado_final_notificacion_codigo")),
                                toLocalDate(rs.getDate("fecha_publicacion_notif"))));
                    }
                }
            }
        }
        return items;
    }

    public void asignarNotificacion(
            List<Long> idsDocumentoAnalizado,
            Long idEquipoDestino,
            Long idUsuarioDestino,
            String numeroHojaEnvio,
            Long idUsuario) throws SQLException {
        asignarNotificacion(idsDocumentoAnalizado, idEquipoDestino, idUsuarioDestino, numeroHojaEnvio, idUsuario, false);
    }

    public void asignarNotificacion(
            List<Long> idsDocumentoAnalizado,
            Long idEquipoDestino,
            Long idUsuarioDestino,
            String numeroHojaEnvio,
            Long idUsuario,
            boolean reasignacion) throws SQLException {
        Map<Long, String> hojasEnvio = new HashMap<Long, String>();
        if (idsDocumentoAnalizado != null) {
            for (Long idDocumento : idsDocumentoAnalizado) {
                hojasEnvio.put(idDocumento, numeroHojaEnvio);
            }
        }
        asignarNotificacionMultiple(idsDocumentoAnalizado, idEquipoDestino, idUsuarioDestino, hojasEnvio, idUsuario, reasignacion);
    }

    public void asignarNotificacionMultiple(
            List<Long> idsDocumentoAnalizado,
            Long idEquipoDestino,
            Long idUsuarioDestino,
            Map<Long, String> hojasEnvioPorDocumento,
            Long idUsuario,
            boolean reasignacion) throws SQLException {
        if (idsDocumentoAnalizado == null || idsDocumentoAnalizado.isEmpty() || idEquipoDestino == null || idUsuarioDestino == null) {
            throw new IllegalArgumentException("Seleccione documentos, equipo destino y usuario destino para generar la asignación.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn,
                        reasignacion ? CODIGO_MOVIMIENTO_REASIGNACION_NOTIFICACION : CODIGO_MOVIMIENTO_ASIGNACION_NOTIFICACION);
                // El estado del expediente (columna "Estado" de las bandejas, distinta del estado
                // del documento) refleja a que bandeja pasa segun el equipo destino: EQ_VALIDACION
                // -> Por validar, EQ_NOTIFICACION -> Por notificar (codigo EN_NOTIFICACION, ya
                // conectado al flujo de intentos/cargo/confirmacion existente). Si el equipo no es
                // ninguno de los dos, no se toca el estado del expediente.
                Long idEstadoExpedienteDestino = resolverEstadoExpedienteDestinoNotificacion(conn, idEquipoDestino);
                String sql = "UPDATE expediente_documento_analizado SET "
                        + "id_equipo_notificacion = ?, id_usuario_notificacion = ?, numero_hoja_envio_notificacion = ?, "
                        + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_documento_analizado = ? AND activo = 1";
                String sqlExpediente = "SELECT d.id_expediente, e.id_etapa_actual FROM expediente_documento_analizado d "
                        + "JOIN expediente e ON e.id_expediente = d.id_expediente "
                        + "WHERE d.id_documento_analizado = ?";
                String sqlResponsable = "UPDATE expediente SET "
                        + "id_equipo_responsable_actual = ?, id_usuario_responsable_actual = ?, "
                        + (idEstadoExpedienteDestino != null ? "id_estado_actual = ?, " : "")
                        + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_expediente = ? AND activo = 1";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     PreparedStatement psExpediente = conn.prepareStatement(sqlExpediente);
                     PreparedStatement psResponsable = conn.prepareStatement(sqlResponsable)) {
                    for (Long idDocumento : idsDocumentoAnalizado) {
                        String numeroHojaEnvio = hojasEnvioPorDocumento == null ? null : hojasEnvioPorDocumento.get(idDocumento);
                        int index = 1;
                        ps.setLong(index++, idEquipoDestino);
                        ps.setLong(index++, idUsuarioDestino);
                        setStringOrNull(ps, index++, numeroHojaEnvio);
                        if (idUsuario == null) {
                            ps.setNull(index++, Types.NUMERIC);
                        } else {
                            ps.setLong(index++, idUsuario);
                        }
                        ps.setLong(index, idDocumento);
                        ps.executeUpdate();

                        Long idExpediente = null;
                        Long idEtapaActualExpediente = null;
                        psExpediente.setLong(1, idDocumento);
                        try (ResultSet rs = psExpediente.executeQuery()) {
                            if (rs.next()) {
                                idExpediente = getLongOrNull(rs, "id_expediente");
                                idEtapaActualExpediente = getLongOrNull(rs, "id_etapa_actual");
                            }
                        }
                        if (idExpediente != null) {
                            // Mantiene EXPEDIENTE.id_usuario_responsable_actual alineado con quien
                            // trabaja el expediente ahora (validador/notificador), para que las
                            // bandejas que muestran "Abogado actual" no sigan mostrando al abogado
                            // de Analisis/Ejecucion una vez que Notificacion asigna el documento.
                            int idxResp = 1;
                            psResponsable.setLong(idxResp++, idEquipoDestino);
                            psResponsable.setLong(idxResp++, idUsuarioDestino);
                            if (idEstadoExpedienteDestino != null) {
                                psResponsable.setLong(idxResp++, idEstadoExpedienteDestino);
                            }
                            setLongOrNull(psResponsable, idxResp++, idUsuario);
                            psResponsable.setLong(idxResp, idExpediente);
                            psResponsable.executeUpdate();
                            if (idEstadoExpedienteDestino != null && idEtapaActualExpediente != null) {
                                ExpedienteEstadoPropagacionDAO.propagarEstadoAAsociados(
                                        conn, idExpediente, idEtapaActualExpediente, idEstadoExpedienteDestino, idUsuario);
                            }
                            if (idMovimiento != null) {
                                insertarHistorialNotificacion(
                                        conn, idExpediente, idDocumento, idMovimiento,
                                        idUsuarioDestino, idEquipoDestino, numeroHojaEnvio, idUsuario);
                            }
                        }
                    }
                }
                conn.commit();
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    public void reasignarNotificacion(
            Long idDocumentoAnalizado,
            Long idEquipoDestino,
            Long idUsuarioDestino,
            String numeroHojaEnvio,
            Long idUsuario) throws SQLException {
        if (idDocumentoAnalizado == null || idEquipoDestino == null || idUsuarioDestino == null) {
            throw new IllegalArgumentException("Seleccione documento, equipo destino y usuario destino para reasignar.");
        }
        asignarNotificacion(
                java.util.Collections.singletonList(idDocumentoAnalizado),
                idEquipoDestino,
                idUsuarioDestino,
                numeroHojaEnvio,
                idUsuario,
                true);
    }

    private void insertarHistorialNotificacion(
            Connection conn,
            Long idExpediente,
            Long idDocumentoAnalizado,
            Long idMovimiento,
            Long idUsuarioDestino,
            Long idEquipoDestino,
            String numeroHojaEnvio,
            Long idUsuarioCreador) throws SQLException {
        Long idAutorHistorial = resolverAutorHistorial(conn, idUsuarioCreador, idUsuarioDestino);
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, "
                + "id_usuario_destino, id_equipo_destino, "
                + "tabla_relacionada, id_registro_relacionado, comentario, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, 'EXPEDIENTE_DOCUMENTO_ANALIZADO', ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idUsuarioDestino);
            setLongOrNull(ps, 4, idEquipoDestino);
            ps.setLong(5, idDocumentoAnalizado);
            setStringOrNull(ps, 6, numeroHojaEnvio);
            setLongOrNull(ps, 7, idAutorHistorial);
            ps.executeUpdate();
        }
    }

    /**
     * Si quien ejecuta la accion es ADMIN_SISTEMA, el historial no debe quedar a su nombre:
     * se sustituye por el usuario asignado/responsable/destino de esa misma accion. Si no hay
     * un destino resuelto, se conserva el autor real.
     */
    private Long resolverAutorHistorial(Connection conn, Long idUsuarioActor, Long idUsuarioDestino) throws SQLException {
        if (idUsuarioDestino == null || !catalogoLookupDAO.tieneRolAdminSistema(conn, idUsuarioActor)) {
            return idUsuarioActor;
        }
        return idUsuarioDestino;
    }

    /**
     * Resuelve a que estado de EXPEDIENTE (etapa NOTIFICACION) debe pasar el expediente segun el
     * equipo al que se asigna un documento: EQ_VALIDACION -> Por validar, EQ_NOTIFICACION -> Por
     * notificar (codigo EN_NOTIFICACION, reutilizado; ver script 73). Si el equipo no es ninguno
     * de los dos, retorna null y el llamador no debe tocar el estado del expediente.
     */
    private Long resolverEstadoExpedienteDestinoNotificacion(Connection conn, Long idEquipoDestino) throws SQLException {
        if (idEquipoDestino == null) {
            return null;
        }
        String sql = "SELECT codigo FROM equipo WHERE id_equipo = ?";
        String codigoEquipo = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipoDestino);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    codigoEquipo = rs.getString("codigo");
                }
            }
        }
        if (codigoEquipo == null) {
            return null;
        }
        String estadoDestino;
        if ("EQ_VALIDACION".equalsIgnoreCase(codigoEquipo)) {
            estadoDestino = "POR_VALIDAR";
        } else if ("EQ_NOTIFICACION".equalsIgnoreCase(codigoEquipo)) {
            estadoDestino = "EN_NOTIFICACION";
        } else {
            return null;
        }
        return catalogoLookupDAO.obtenerEstadoId(conn, estadoDestino);
    }

    public List<com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO> listarHistorialAsignacionesNotificacion(
            Long idDocumentoAnalizado) throws SQLException {
        List<com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO> items =
                new ArrayList<com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO>();
        if (idDocumentoAnalizado == null) {
            return items;
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            String sql = "SELECT eh.id_expediente_historial, un.nombre_completo AS usuario_destino, "
                    + "eq.nombre AS equipo_destino, eh.comentario AS numero_hoja_envio, eh.fecha_movimiento, "
                    + "uc.nombre_completo AS asignado_por, tm.codigo AS codigo_movimiento "
                    + "FROM expediente_historial eh "
                    + "LEFT JOIN usuario un ON un.id_usuario = eh.id_usuario_destino "
                    + "LEFT JOIN equipo eq ON eq.id_equipo = eh.id_equipo_destino "
                    + "LEFT JOIN usuario uc ON uc.id_usuario = eh.creado_por "
                    + "LEFT JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = eh.id_tipo_movimiento "
                    + "WHERE eh.activo = 1 AND eh.tabla_relacionada = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                    + "AND eh.id_registro_relacionado = ? "
                    + "AND tm.codigo IN (?, ?) "
                    + "ORDER BY eh.fecha_movimiento DESC, eh.id_expediente_historial DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idDocumentoAnalizado);
                ps.setString(2, CODIGO_MOVIMIENTO_ASIGNACION_NOTIFICACION);
                ps.setString(3, CODIGO_MOVIMIENTO_REASIGNACION_NOTIFICACION);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean primero = true;
                    while (rs.next()) {
                        items.add(new com.sdrerc.domain.dto.sdrercapp.AsignacionHistorialDTO(
                                getLongOrNull(rs, "id_expediente_historial"),
                                rs.getString("usuario_destino"),
                                rs.getString("equipo_destino"),
                                rs.getString("numero_hoja_envio"),
                                toLocalDateTime(rs.getTimestamp("fecha_movimiento")),
                                primero,
                                CODIGO_MOVIMIENTO_REASIGNACION_NOTIFICACION.equalsIgnoreCase(rs.getString("codigo_movimiento")),
                                null,
                                rs.getString("asignado_por")));
                        primero = false;
                    }
                }
            }
        }
        return items;
    }

    public void registrarFirmaDocumentoNotificacion(
            Long idDocumentoAnalizado,
            String numeroDocumento,
            LocalDate fechaEmision,
            String estadoDocumentoCodigo,
            Long idUsuario) throws SQLException {
        if (idDocumentoAnalizado == null) {
            throw new IllegalArgumentException("Seleccione un documento para registrar la firma.");
        }
        String codigoResuelto = estadoDocumentoCodigo == null || estadoDocumentoCodigo.trim().isEmpty()
                ? "EMITIDO" : estadoDocumentoCodigo.trim();
        try (Connection conn = SdrercAppConnection.getConnection()) {
            Long idEstado = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, codigoResuelto);
            if (idEstado == null) {
                throw new SQLException("No se encontró el estado de documento " + codigoResuelto + ".");
            }
            String sql = "UPDATE expediente_documento_analizado SET "
                    + "id_estado_documento = ?, numero_documento = ?, fecha_documento = ?, "
                    + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                    + "WHERE id_documento_analizado = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idEstado);
                setStringOrNull(ps, 2, numeroDocumento);
                setDateOrNull(ps, 3, fechaEmision);
                if (idUsuario == null) {
                    ps.setNull(4, Types.NUMERIC);
                } else {
                    ps.setLong(4, idUsuario);
                }
                ps.setLong(5, idDocumentoAnalizado);
                int updated = ps.executeUpdate();
                if (updated != 1) {
                    throw new SQLException("No se pudo registrar la firma del documento.");
                }
            }
        }
    }

    /**
     * Registra en EXPEDIENTE_HISTORIAL que el supervisor aprobó la emisión de un documento desde
     * el mini-panel "Emisión" de la Bandeja Asignación de Notificación (botón "Registrar
     * Supervisión", resultado Aprobado). No mueve etapa/estado del expediente (esa transición real
     * ocurre después, al generar la segunda asignación hacia Eq. Notificación en el mini-panel
     * "② Asignación"); es solo el registro de auditoría de que la supervisión se realizó — antes de
     * este método, un resultado Aprobado no dejaba ningún rastro en base de datos, a diferencia de
     * Análisis (EXPEDIENTE_EVALUACION), Verificación y Ejecución (transición de etapa/estado +
     * EXPEDIENTE_HISTORIAL.motivo), que sí registran su resultado.
     */
    public void registrarSupervisionEmisionAprobada(
            Long idExpediente, Long idDocumentoAnalizado, Long idUsuario) throws SQLException {
        if (idExpediente == null || idDocumentoAnalizado == null) {
            throw new IllegalArgumentException("Seleccione un documento para registrar la supervisión.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_SUPERVISION_EMISION_NOTIFICACION);
            if (idMovimiento == null) {
                throw new SQLException("No se encontró el movimiento " + CODIGO_MOVIMIENTO_SUPERVISION_EMISION_NOTIFICACION
                        + ". Ejecute el script de catálogo correspondiente.");
            }
            String sql = "INSERT INTO expediente_historial ("
                    + "id_expediente, id_tipo_movimiento, fecha_movimiento, "
                    + "tabla_relacionada, id_registro_relacionado, comentario, activo, creado_por, creado_en"
                    + ") VALUES (?, ?, SYSTIMESTAMP, 'EXPEDIENTE_DOCUMENTO_ANALIZADO', ?, ?, 1, ?, SYSTIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpediente);
                ps.setLong(2, idMovimiento);
                ps.setLong(3, idDocumentoAnalizado);
                ps.setString(4, "Emisión aprobada por supervisión.");
                setLongOrNull(ps, 5, idUsuario);
                ps.executeUpdate();
            }
        }
    }

    /**
     * true si ya existe un registro de "Registrar Supervisión" (Aprobado) para este documento
     * ({@link #registrarSupervisionEmisionAprobada}). Usado por el panel para bloquear/desbloquear
     * el mini-panel "② Asignación" y los botones "Generar asignación"/"Registrar Supervisión" según
     * corresponda (pedido explícito del usuario, 07/08/2026): ya no basta con que el documento esté
     * Emitido, debe existir el registro de supervisión en base de datos.
     */
    public boolean existeSupervisionEmisionAprobada(Long idDocumentoAnalizado) throws SQLException {
        if (idDocumentoAnalizado == null) {
            return false;
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            String sql = "SELECT 1 FROM expediente_historial eh "
                    + "JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = eh.id_tipo_movimiento "
                    + "WHERE eh.activo = 1 AND eh.tabla_relacionada = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                    + "AND eh.id_registro_relacionado = ? "
                    + "AND UPPER(tm.codigo) = 'SUPERVISION_EMISION_NOTIFICACION' AND ROWNUM = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idDocumentoAnalizado);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        }
    }

    /**
     * Deriva el expediente de un documento (etapa NOTIFICACION, en cualquiera de sus estados
     * reales: Por asignar/Por validar/Por notificar/Notificado) de vuelta a
     * EJECUCION/EN_EJECUCION. Caso de negocio: el validador observa el documento y el
     * supervisor de notificacion, desde el panel de Asignacion, decide que el expediente debe
     * volver a Ejecucion en vez de a Analisis o de reasignarse dentro de Notificacion. No
     * reasigna responsable (misma regla que
     * VerificacionExpedienteDAO.aprobarConDestinoAEjecucion): Ejecucion exige que la atienda el
     * mismo abogado ya ligado via EXPEDIENTE_ASIGNACION, no el usuario elegido en el combo de
     * destino operativo (que aqui solo se registra en el historial). El destino a Analisis (Eq.
     * Analisis) NO tiene un metodo propio: reutiliza
     * AsignacionExpedienteService.reasignarDesdeCartaRespuesta, que ya resuelve la misma
     * transicion DEVOLUCION_A_ANALISIS de forma dinamica y ademas actualiza EXPEDIENTE_ASIGNACION
     * (necesario porque Analisis SI reasigna responsable a un abogado elegido).
     */
    public void derivarDocumentoNotificacionAEjecucion(
            Long idDocumentoAnalizado,
            Long idEquipoDestino,
            Long idUsuarioDestino,
            String comentario,
            Long idUsuario) throws SQLException {
        if (idDocumentoAnalizado == null) {
            throw new IllegalArgumentException("Seleccione un documento para derivar a Ejecución.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idExpediente = obtenerIdExpedientePorDocumento(conn, idDocumentoAnalizado);
                if (idExpediente == null) {
                    throw new SQLException("El documento seleccionado no existe o no está activo.");
                }
                String sqlExpediente = "SELECT e.id_etapa_actual, e.id_estado_actual, "
                        + "et.codigo AS etapa_codigo, es.codigo AS estado_codigo "
                        + "FROM expediente e "
                        + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                        + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                        + "WHERE e.id_expediente = ? AND e.activo = 1 FOR UPDATE";
                Long idEtapaOrigen;
                Long idEstadoOrigen;
                String etapaCodigo;
                String estadoCodigo;
                try (PreparedStatement ps = conn.prepareStatement(sqlExpediente)) {
                    ps.setLong(1, idExpediente);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("El expediente del documento seleccionado no existe o no está activo.");
                        }
                        idEtapaOrigen = getLongOrNull(rs, "id_etapa_actual");
                        idEstadoOrigen = getLongOrNull(rs, "id_estado_actual");
                        etapaCodigo = rs.getString("etapa_codigo");
                        estadoCodigo = rs.getString("estado_codigo");
                    }
                }
                long[] destino = resolverTransicionPorCodigo(
                        conn, CODIGO_MOVIMIENTO_DEVOLUCION_EJECUCION, etapaCodigo, estadoCodigo, "EJECUCION", "EN_EJECUCION");
                Long idEtapaDestino = destino[0];
                Long idEstadoDestino = destino[1];

                String sqlUpdate = "UPDATE expediente SET id_etapa_actual = ?, id_estado_actual = ?, "
                        + "fecha_ultimo_movimiento = SYSTIMESTAMP, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_expediente = ? AND activo = 1";
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setLong(1, idEtapaDestino);
                    ps.setLong(2, idEstadoDestino);
                    setLongOrNull(ps, 3, idUsuario);
                    ps.setLong(4, idExpediente);
                    int updated = ps.executeUpdate();
                    if (updated != 1) {
                        throw new SQLException("No se pudo actualizar el expediente seleccionado.");
                    }
                }
                ExpedienteEstadoPropagacionDAO.propagarEstadoAAsociados(conn, idExpediente, idEtapaDestino, idEstadoDestino, idUsuario);

                Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_DEVOLUCION_EJECUCION);
                if (idMovimiento != null) {
                    Long idAutorHistorial = resolverAutorHistorial(conn, idUsuario, idUsuarioDestino);
                    String sqlHistorial = "INSERT INTO expediente_historial ("
                            + "id_expediente, id_tipo_movimiento, fecha_movimiento, "
                            + "id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino, "
                            + "id_usuario_origen, id_usuario_destino, id_equipo_destino, "
                            + "comentario, activo, creado_por, creado_en"
                            + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlHistorial)) {
                        ps.setLong(1, idExpediente);
                        ps.setLong(2, idMovimiento);
                        setLongOrNull(ps, 3, idEtapaOrigen);
                        setLongOrNull(ps, 4, idEstadoOrigen);
                        ps.setLong(5, idEtapaDestino);
                        ps.setLong(6, idEstadoDestino);
                        setLongOrNull(ps, 7, idAutorHistorial);
                        setLongOrNull(ps, 8, idUsuarioDestino);
                        setLongOrNull(ps, 9, idEquipoDestino);
                        setStringOrNull(ps, 10, comentario);
                        setLongOrNull(ps, 11, idAutorHistorial);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private Long obtenerIdExpedientePorDocumento(Connection conn, Long idDocumentoAnalizado) throws SQLException {
        String sql = "SELECT id_expediente FROM expediente_documento_analizado WHERE id_documento_analizado = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idDocumentoAnalizado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? getLongOrNull(rs, "id_expediente") : null;
            }
        }
    }

    /**
     * Resuelve dinamicamente la transicion activa (etapa/estado destino) para una accion y un
     * origen real, sin asumir un origen fijo (el expediente puede llegar a este punto desde
     * distintos estados de la etapa NOTIFICACION). Mismo patron que
     * AsignacionExpedienteDAO.resolverTransicionPorCodigo / VerificacionExpedienteDAO.requerirTransicion.
     */
    private long[] resolverTransicionPorCodigo(
            Connection conn,
            String accionCodigo,
            String etapaOrigenCodigo,
            String estadoOrigenCodigo,
            String etapaDestinoCodigo,
            String estadoDestinoCodigo) throws SQLException {
        String sql = "SELECT ft.id_etapa_destino, ft.id_estado_destino "
                + "FROM flujo f "
                + "JOIN flujo_transicion ft ON ft.id_flujo = f.id_flujo "
                + "JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen "
                + "JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen "
                + "JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino "
                + "JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino "
                + "WHERE f.codigo = ? AND f.activo = 1 AND ft.activo = 1 "
                + "AND ft.codigo_accion = ? "
                + "AND eo.codigo = ? AND so.codigo = ? "
                + "AND ed.codigo = ? AND sd.codigo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, CODIGO_FLUJO);
            ps.setString(2, accionCodigo);
            ps.setString(3, etapaOrigenCodigo);
            ps.setString(4, estadoOrigenCodigo);
            ps.setString(5, etapaDestinoCodigo);
            ps.setString(6, estadoDestinoCodigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No existe transición activa "
                            + etapaOrigenCodigo + "/" + estadoOrigenCodigo + " -> "
                            + etapaDestinoCodigo + "/" + estadoDestinoCodigo
                            + " para " + accionCodigo + " en " + CODIGO_FLUJO + ".");
                }
                return new long[]{rs.getLong("id_etapa_destino"), rs.getLong("id_estado_destino")};
            }
        }
    }

    public void registrarResultadoValidacion(
            Long idDocumentoAnalizado,
            String resultadoCodigo,
            String comentario,
            Long idUsuario) throws SQLException {
        if (idDocumentoAnalizado == null) {
            throw new IllegalArgumentException("Seleccione un documento para registrar la validación.");
        }
        boolean observado = "OBSERVADO".equalsIgnoreCase(resultadoCodigo);
        String estadoDestino = observado ? "OBSERVADO" : "VALIDADO";
        try (Connection conn = SdrercAppConnection.getConnection()) {
            Long idEstadoDestino = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, estadoDestino);
            if (idEstadoDestino == null) {
                throw new SQLException("No se encontró el estado de documento " + estadoDestino
                        + ". Ejecute el script 44_asignacion_notificacion_validacion.sql.");
            }
            boolean soportaDetalleObservacion = soportaDetalleObservacionDocumentoAnalizado(conn);
            String sql = "UPDATE expediente_documento_analizado SET "
                    + "id_estado_documento = ?, "
                    + (observado ? "id_equipo_notificacion = NULL, id_usuario_notificacion = NULL, numero_hoja_envio_notificacion = NULL, " : "")
                    + (observado && soportaDetalleObservacion ? "detalle_observacion = ?, " : "")
                    + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                    + "WHERE id_documento_analizado = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int index = 1;
                ps.setLong(index++, idEstadoDestino);
                if (observado && soportaDetalleObservacion) {
                    setStringOrNull(ps, index++, comentario);
                }
                if (idUsuario == null) {
                    ps.setNull(index++, Types.NUMERIC);
                } else {
                    ps.setLong(index++, idUsuario);
                }
                ps.setLong(index, idDocumentoAnalizado);
                int updated = ps.executeUpdate();
                if (updated != 1) {
                    throw new SQLException("No se pudo registrar el resultado de la validación.");
                }
            }
            String sqlExpediente = "SELECT d.id_expediente, e.id_etapa_actual FROM expediente_documento_analizado d "
                    + "JOIN expediente e ON e.id_expediente = d.id_expediente "
                    + "WHERE d.id_documento_analizado = ?";
            Long idExpediente = null;
            Long idEtapaActualExpediente = null;
            try (PreparedStatement psExpediente = conn.prepareStatement(sqlExpediente)) {
                psExpediente.setLong(1, idDocumentoAnalizado);
                try (ResultSet rs = psExpediente.executeQuery()) {
                    if (rs.next()) {
                        idExpediente = getLongOrNull(rs, "id_expediente");
                        idEtapaActualExpediente = getLongOrNull(rs, "id_etapa_actual");
                    }
                }
            }
            if (idExpediente != null) {
                if (observado) {
                    // Libera el responsable actual del expediente (quedaba en el validador/notificador)
                    // para que las bandejas vuelvan a resolver al abogado de Analisis/Ejecucion mediante
                    // el fallback ya existente sobre EXPEDIENTE_ASIGNACION.
                    String sqlResponsable = "UPDATE expediente SET "
                            + "id_equipo_responsable_actual = NULL, id_usuario_responsable_actual = NULL, "
                            + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                            + "WHERE id_expediente = ? AND activo = 1";
                    try (PreparedStatement psResponsable = conn.prepareStatement(sqlResponsable)) {
                        setLongOrNull(psResponsable, 1, idUsuario);
                        psResponsable.setLong(2, idExpediente);
                        psResponsable.executeUpdate();
                    }
                } else {
                    // Aprobado (documento pasa a Validado): el expediente vuelve a "Por asignar"
                    // para que reaparezca en la Bandeja Asignacion y el coordinador lo derive esta
                    // vez al equipo de Notificacion (ver CONDICION_ASIGNACION_NOTIFICACION, que ya
                    // acepta FINAL+Validado).
                    Long idEstadoPorAsignar = catalogoLookupDAO.obtenerEstadoId(conn, "POR_ASIGNAR");
                    if (idEstadoPorAsignar != null) {
                        String sqlEstado = "UPDATE expediente SET "
                                + "id_estado_actual = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                                + "WHERE id_expediente = ? AND activo = 1";
                        try (PreparedStatement psEstado = conn.prepareStatement(sqlEstado)) {
                            psEstado.setLong(1, idEstadoPorAsignar);
                            setLongOrNull(psEstado, 2, idUsuario);
                            psEstado.setLong(3, idExpediente);
                            psEstado.executeUpdate();
                        }
                        if (idEtapaActualExpediente != null) {
                            ExpedienteEstadoPropagacionDAO.propagarEstadoAAsociados(
                                    conn, idExpediente, idEtapaActualExpediente, idEstadoPorAsignar, idUsuario);
                        }
                    }
                }
            }
        }
    }

    public List<NotificacionIntentoDTO> listarIntentosNotificacion(Long idDocumentoAnalizado) throws SQLException {
        List<NotificacionIntentoDTO> items = new ArrayList<NotificacionIntentoDTO>();
        if (idDocumentoAnalizado == null) {
            return items;
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            if (!soportaIntentosNotificacionDocumento(conn)) {
                return items;
            }
            boolean soportaPublicacion = soportaPublicacionPreparada(conn);
            String sql = "SELECT n.id_expediente_notificacion, n.id_expediente, n.id_documento_analizado, "
                    + "n.numero_intento, tn.codigo AS tipo_notificacion_codigo, tn.nombre AS tipo_notificacion, "
                    + "n.fecha_envio, en.codigo AS estado_notificacion_codigo, en.nombre AS estado_notificacion, "
                    + "n.codigo_notificacion, "
                    + "(SELECT MAX(c.fecha_recepcion) FROM expediente_cargo_acuse c "
                    + " WHERE c.id_expediente_notificacion = n.id_expediente_notificacion AND c.activo = 1) AS fecha_recepcion, "
                    + (soportaPublicacion
                            ? "(SELECT fecha_publicacion FROM ("
                            + " SELECT p.fecha_publicacion FROM expediente_publicacion p "
                            + " WHERE p.id_expediente = n.id_expediente AND p.activo = 1 "
                            + " ORDER BY p.creado_en DESC, p.id_expediente_publicacion DESC"
                            + ") WHERE ROWNUM = 1) AS fecha_publicacion "
                            : "CAST(NULL AS DATE) AS fecha_publicacion ")
                    + "FROM expediente_notificacion n "
                    + "JOIN tipo_notificacion tn ON tn.id_tipo_notificacion = n.id_tipo_notificacion "
                    + "JOIN estado_notificacion en ON en.id_estado_notificacion = n.id_estado_notificacion "
                    + "WHERE n.id_documento_analizado = ? AND n.activo = 1 "
                    + "ORDER BY n.numero_intento, n.id_expediente_notificacion";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idDocumentoAnalizado);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        LocalDateTime fechaRecepcion = toLocalDateTime(rs.getTimestamp("fecha_recepcion"));
                        String estadoCodigo = rs.getString("estado_notificacion_codigo");
                        boolean ubicado = "EXITOSA".equalsIgnoreCase(estadoCodigo) || fechaRecepcion != null;
                        items.add(new NotificacionIntentoDTO(
                                getLongOrNull(rs, "id_expediente_notificacion"),
                                getLongOrNull(rs, "id_expediente"),
                                getLongOrNull(rs, "id_documento_analizado"),
                                rs.getInt("numero_intento"),
                                rs.getString("tipo_notificacion_codigo"),
                                rs.getString("tipo_notificacion"),
                                toLocalDateTime(rs.getTimestamp("fecha_envio")),
                                estadoCodigo,
                                rs.getString("estado_notificacion"),
                                rs.getString("codigo_notificacion"),
                                fechaRecepcion,
                                ubicado,
                                toLocalDate(rs.getDate("fecha_publicacion"))));
                    }
                }
            }
        }
        return items;
    }

    public void registrarIntentoNotificacion(
            Long idExpediente,
            Long idDocumentoAnalizado,
            String tipoNotificacionCodigo,
            String codigoNotificacion,
            LocalDate fechaEnvio,
            Long idUsuario) throws SQLException {
        registrarIntentoNotificacion(idExpediente, idDocumentoAnalizado, tipoNotificacionCodigo, codigoNotificacion, fechaEnvio, null, idUsuario);
    }

    /**
     * @param estadoNotificacionCodigo si es null/vacío, se inserta como `PENDIENTE` (comportamiento
     *      historico, usado por los intentos 1/2 de Notificación: un intento siempre nace pendiente
     *      hasta que se confirme la recepción). La Bandeja Publicación sí pasa un valor explícito
     *      aquí: a diferencia de un intento de notificación al ciudadano, el usuario puede marcar
     *      "Publicado" desde el primer guardado del borrador, y antes de este parámetro ese valor
     *      se ignoraba silenciosamente (quedaba `PENDIENTE` pese a lo elegido en el combo), obligando
     *      a un segundo clic en Guardar (ahora sí por la ruta de actualización, que sí respeta el
     *      estado) para que el cambio surtiera efecto — bug reportado por el usuario 05/08/2026.
     */
    public void registrarIntentoNotificacion(
            Long idExpediente,
            Long idDocumentoAnalizado,
            String tipoNotificacionCodigo,
            String codigoNotificacion,
            LocalDate fechaEnvio,
            String estadoNotificacionCodigo,
            Long idUsuario) throws SQLException {
        if (idExpediente == null || idDocumentoAnalizado == null) {
            throw new IllegalArgumentException("Seleccione expediente y documento para registrar el intento.");
        }
        String tipoCodigo = hasText(tipoNotificacionCodigo) ? tipoNotificacionCodigo.trim().toUpperCase() : "VIRTUAL";
        String estadoCodigo = hasText(estadoNotificacionCodigo) ? estadoNotificacionCodigo.trim().toUpperCase() : "PENDIENTE";
        try (Connection conn = SdrercAppConnection.getConnection()) {
            if (!soportaIntentosNotificacionDocumento(conn)) {
                throw new SQLException("La base de datos no soporta intentos de notificación por documento. Ejecute el script 45_intentos_notificacion_documento.sql.");
            }
            Long idTipoNotificacion = catalogoLookupDAO.obtenerTipoNotificacionId(conn, tipoCodigo);
            if (idTipoNotificacion == null) {
                throw new SQLException("No se encontró el tipo de notificación " + tipoCodigo + ". Verifique el catálogo o ejecute el script correspondiente.");
            }
            Long idEstadoInicial = catalogoLookupDAO.obtenerEstadoNotificacionId(conn, estadoCodigo);
            if (idEstadoInicial == null) {
                throw new SQLException("No se encontró el estado de notificación " + estadoCodigo + ".");
            }
            int numeroIntento = obtenerSiguienteIntentoNotificacion(conn, idDocumentoAnalizado);
            if (numeroIntento > 3) {
                throw new SQLException("Solo se permiten hasta 3 intentos de notificación por documento.");
            }
            String sql = "INSERT INTO expediente_notificacion ("
                    + "id_expediente, id_documento_analizado, id_tipo_notificacion, id_estado_notificacion, "
                    + "numero_intento, fecha_envio, resultado, requiere_publicacion, codigo_notificacion, "
                    + "observacion, activo, creado_por, creado_en"
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpediente);
                ps.setLong(2, idDocumentoAnalizado);
                ps.setLong(3, idTipoNotificacion);
                ps.setLong(4, idEstadoInicial);
                ps.setInt(5, numeroIntento);
                if (fechaEnvio == null) {
                    ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                } else {
                    ps.setTimestamp(6, Timestamp.valueOf(fechaEnvio.atStartOfDay()));
                }
                ps.setString(7, estadoCodigo);
                ps.setInt(8, "PUBLICACION".equalsIgnoreCase(tipoCodigo) ? 1 : 0);
                setStringOrNull(ps, 9, limitar(codigoNotificacion, 60));
                setStringOrNull(ps, 10, "Intento registrado desde bandeja de documentos.");
                if (idUsuario == null) {
                    ps.setNull(11, Types.NUMERIC);
                } else {
                    ps.setLong(11, idUsuario);
                }
                ps.executeUpdate();
            }
        }
    }

    public void actualizarIntentoNotificacion(
            Long idExpedienteNotificacion,
            String tipoNotificacionCodigo,
            String estadoNotificacionCodigo,
            String codigoNotificacion,
            LocalDate fechaEnvio,
            String observacion,
            Long idUsuario) throws SQLException {
        if (idExpedienteNotificacion == null) {
            throw new IllegalArgumentException("Seleccione el intento de notificación a actualizar.");
        }
        String tipoCodigo = hasText(tipoNotificacionCodigo) ? tipoNotificacionCodigo.trim().toUpperCase() : "VIRTUAL";
        String estadoCodigo = hasText(estadoNotificacionCodigo) ? estadoNotificacionCodigo.trim().toUpperCase() : "PENDIENTE";
        try (Connection conn = SdrercAppConnection.getConnection()) {
            if (!soportaIntentosNotificacionDocumento(conn)) {
                throw new SQLException("La base de datos no soporta intentos de notificación por documento. Ejecute el script 45_intentos_notificacion_documento.sql.");
            }
            Long idTipoNotificacion = catalogoLookupDAO.obtenerTipoNotificacionId(conn, tipoCodigo);
            if (idTipoNotificacion == null) {
                throw new SQLException("No se encontró el tipo de notificación " + tipoCodigo + ".");
            }
            Long idEstadoNotificacion = catalogoLookupDAO.obtenerEstadoNotificacionId(conn, estadoCodigo);
            if (idEstadoNotificacion == null) {
                throw new SQLException("No se encontró el estado de notificación " + estadoCodigo + ".");
            }
            String sql = "UPDATE expediente_notificacion SET "
                    + "id_tipo_notificacion = ?, id_estado_notificacion = ?, resultado = ?, "
                    + "codigo_notificacion = ?, fecha_envio = ?, observacion = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                    + "WHERE id_expediente_notificacion = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idTipoNotificacion);
                ps.setLong(2, idEstadoNotificacion);
                ps.setString(3, estadoCodigo);
                setStringOrNull(ps, 4, limitar(codigoNotificacion, 60));
                if (fechaEnvio == null) {
                    ps.setNull(5, Types.TIMESTAMP);
                } else {
                    ps.setTimestamp(5, Timestamp.valueOf(fechaEnvio.atStartOfDay()));
                }
                setStringOrNull(ps, 6, limitar(observacion, 500));
                if (idUsuario == null) {
                    ps.setNull(7, Types.NUMERIC);
                } else {
                    ps.setLong(7, idUsuario);
                }
                ps.setLong(8, idExpedienteNotificacion);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No se encontró el intento de notificación indicado.");
                }
            }
        }
    }

    /**
     * Confirma la recepcion (Fecha Recepcion = Fecha Acuse) de un intento de notificacion:
     * marca el intento como EXITOSA/ubicado, registra el cargo en expediente_cargo_acuse con
     * el codigo digitado (Codigo Notificacion si la modalidad es virtual, Usuario Notificacion
     * si es presencial; ambos se persisten en el mismo campo de texto, expediente_cargo_acuse.recibido_por)
     * y refleja la misma fecha como "fecha acuse" del documento analizado (expediente_documento_analizado,
     * columnas ya existentes fecha_acuse/notificado, usadas por el panel de Analisis/Cartas de respuesta).
     */
    public void confirmarRecepcionIntentoNotificacion(
            Long idExpediente,
            Long idExpedienteNotificacion,
            Long idDocumentoAnalizado,
            String codigoORecibidoPor,
            LocalDate fechaEnvio,
            LocalDate fechaRecepcion,
            Long idUsuario) throws SQLException {
        if (idExpediente == null || idExpedienteNotificacion == null) {
            throw new IllegalArgumentException("Seleccione el intento de notificación a confirmar.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            if (!soportaIntentosNotificacionDocumento(conn)) {
                throw new SQLException("La base de datos no soporta intentos de notificación por documento.");
            }
            Long idEstadoNotificacion = catalogoLookupDAO.obtenerEstadoNotificacionId(conn, "EXITOSA");
            if (idEstadoNotificacion == null) {
                throw new SQLException("No se encontró el estado de notificación EXITOSA.");
            }
            String sqlUpdate = "UPDATE expediente_notificacion SET "
                    + "id_estado_notificacion = ?, resultado = 'EXITOSA', codigo_notificacion = ?, "
                    + (fechaEnvio != null ? "fecha_envio = ?, " : "")
                    + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                    + "WHERE id_expediente_notificacion = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                int index = 1;
                ps.setLong(index++, idEstadoNotificacion);
                setStringOrNull(ps, index++, limitar(codigoORecibidoPor, 60));
                if (fechaEnvio != null) {
                    ps.setTimestamp(index++, Timestamp.valueOf(fechaEnvio.atStartOfDay()));
                }
                if (idUsuario == null) {
                    ps.setNull(index++, Types.NUMERIC);
                } else {
                    ps.setLong(index++, idUsuario);
                }
                ps.setLong(index, idExpedienteNotificacion);
                int filas = ps.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No se encontró el intento de notificación indicado.");
                }
            }
            Long idEstadoCargo = catalogoLookupDAO.obtenerEstadoCargoAcuseId(conn, "CARGO_RECIBIDO");
            if (idEstadoCargo == null) {
                throw new SQLException("No se encontró el estado de cargo CARGO_RECIBIDO.");
            }
            LocalDate fechaAcuse = fechaRecepcion == null ? LocalDate.now() : fechaRecepcion;
            String sqlInsert = "INSERT INTO expediente_cargo_acuse ("
                    + "id_expediente, id_expediente_notificacion, id_estado_cargo_acuse, fecha_recepcion, "
                    + "recibido_por, activo, creado_por, creado_en"
                    + ") VALUES (?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setLong(1, idExpediente);
                ps.setLong(2, idExpedienteNotificacion);
                ps.setLong(3, idEstadoCargo);
                ps.setTimestamp(4, Timestamp.valueOf(fechaAcuse.atStartOfDay()));
                setStringOrNull(ps, 5, limitar(codigoORecibidoPor, 250));
                if (idUsuario == null) {
                    ps.setNull(6, Types.NUMERIC);
                } else {
                    ps.setLong(6, idUsuario);
                }
                ps.executeUpdate();
            }
            if (idDocumentoAnalizado != null) {
                String sqlAcuse = "UPDATE expediente_documento_analizado SET "
                        + "fecha_acuse = ?, notificado = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_documento_analizado = ? AND activo = 1";
                try (PreparedStatement ps = conn.prepareStatement(sqlAcuse)) {
                    ps.setDate(1, Date.valueOf(fechaAcuse));
                    if (idUsuario == null) {
                        ps.setNull(2, Types.NUMERIC);
                    } else {
                        ps.setLong(2, idUsuario);
                    }
                    ps.setLong(3, idDocumentoAnalizado);
                    ps.executeUpdate();
                }
            }
        }
    }

    /** Baja logica de un intento de notificacion ya registrado (mismo patron que darBajaDocumentoAnalizado). */
    public void darBajaIntentoNotificacion(Long idExpedienteNotificacion, Long idUsuario) throws SQLException {
        if (idExpedienteNotificacion == null) {
            return;
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            String sql = "UPDATE expediente_notificacion SET activo = 0, "
                    + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                    + "WHERE id_expediente_notificacion = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (idUsuario == null) {
                    ps.setNull(1, Types.NUMERIC);
                } else {
                    ps.setLong(1, idUsuario);
                }
                ps.setLong(2, idExpedienteNotificacion);
                ps.executeUpdate();
            }
        }
    }

    public void guardarCartaRespuesta(
            Long idExpediente,
            DocumentoAnalizadoDTO carta,
            Long idUsuario) throws SQLException {
        if (idExpediente == null || carta == null || !hasText(carta.getTipoDocumentoCodigo())) {
            throw new IllegalArgumentException("Seleccione el tipo de documento de la carta de respuesta.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, carta.getTipoDocumentoCodigo());
                if (idTipoDocumento == null) {
                    throw new SQLException("No se encontró el tipo de documento: " + carta.getTipoDocumentoCodigo() + ".");
                }
                boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
                boolean soportaOposicion = soportaExisteOposicion(conn);
                Long idDocumento = carta.getIdDocumentoAnalizado();
                if (idDocumento == null || idDocumento.longValue() < 0L) {
                    insertarCartaRespuesta(conn, idExpediente, carta, idTipoDocumento, soportaAnalisisMultiple, soportaOposicion, idUsuario);
                } else {
                    actualizarCartaRespuesta(conn, idExpediente, carta, soportaOposicion, idUsuario);
                }
                actualizarFechaPublicacionCarta(conn, idExpediente, carta.getFechaPublicacion(), idUsuario);
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Persiste la "Fech.Publ.Edicto" editada desde la grilla "Documentos de cartas de respuesta"
     * (CartaRespuestaTreeGridPanelV2, pestañas "Cartas de Rpta" y "Documentos" de Asignación).
     * Reutiliza EXPEDIENTE_PUBLICACION (misma tabla que lee el panel de solo lectura "Publicación
     * prevista" de Análisis) via los helpers ya existentes {@link #insertarPublicacionPreparada}/
     * {@link #actualizarPublicacionPreparada(Connection, Long, LocalDate, Long)} — ya estaban
     * escritos (con el comentario "...desde Asignación - Cartas de respuesta" en el INSERT) pero
     * nunca se llamaban desde ningún lado: ni desde aquí (bug reportado, la fecha se mostraba en
     * la grilla via un JOIN de lectura pero el icono Guardar nunca la escribía), ni desde
     * {@link #actualizarPublicacionPreparada(Connection, Long, DocumentoAnalizadoDTO, Long)} (esa
     * variante tampoco tenía ningún llamador activo: {@code actualizarRespuestaDocumentoAnalizado}
     * y {@code DocumentoAnalisisService.guardarRespuestaDocumentoAnalizado} existen pero ningún
     * panel los invoca actualmente). Esta variante nueva NO toca EXPEDIENTE.requiere_publicacion
     * a propósito: ese flag lo gestiona el panel de Análisis (checkbox "Requiere publicación"),
     * que esta grilla no expone, y esta pantalla no debe pisarlo con un valor asumido.
     */
    private void actualizarFechaPublicacionCarta(
            Connection conn, Long idExpediente, LocalDate fechaPublicacion, Long idUsuario) throws SQLException {
        if (!soportaPublicacionPreparada(conn)) {
            return;
        }
        Long idPublicacion = obtenerPublicacionActiva(conn, idExpediente);
        if (idPublicacion == null) {
            if (fechaPublicacion != null) {
                insertarPublicacionPreparada(conn, idExpediente, fechaPublicacion, idUsuario);
            }
            return;
        }
        actualizarPublicacionPreparada(conn, idPublicacion, fechaPublicacion, idUsuario);
    }

    private void insertarCartaRespuesta(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO carta,
            Long idTipoDocumento,
            boolean soportaAnalisisMultiple,
            boolean soportaOposicion,
            Long idUsuario) throws SQLException {
        String sql = "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, "
                + (soportaAnalisisMultiple ? "id_expediente_analisis, " : "")
                + "id_documento_padre, nivel, orden, id_tipo_documento_adjunto, "
                + "fecha_documento, confirmacion_respuesta, fecha_respuesta, numero_hoja_envio_respuesta, "
                + (soportaOposicion ? "existe_oposicion, " : "")
                + "activo, creado_por, creado_en"
                + ") VALUES (?, " + (soportaAnalisisMultiple ? "?, " : "")
                + "?, ?, 0, ?, ?, ?, ?, ?, " + (soportaOposicion ? "?, " : "")
                + "1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            ps.setLong(index++, idExpediente);
            if (soportaAnalisisMultiple) {
                setLongOrNull(ps, index++, carta.getIdExpedienteAnalisis());
            }
            setLongOrNull(ps, index++, carta.getIdDocumentoPadre());
            ps.setInt(index++, 1);
            ps.setLong(index++, idTipoDocumento);
            setDateOrNull(ps, index++, carta.getFechaDocumento());
            setStringOrNull(ps, index++, normalizarConfirmacionRespuesta(carta.getConfirmacionRespuesta()));
            setDateOrNull(ps, index++, carta.getFechaRespuesta());
            setStringOrNull(ps, index++, limitar(emptyToNull(carta.getNumeroHojaEnvioRespuesta()), 120));
            if (soportaOposicion) {
                setBooleanOrNull(ps, index++, carta.getExisteOposicion());
            }
            if (idUsuario == null) {
                ps.setNull(index, Types.NUMERIC);
            } else {
                ps.setLong(index, idUsuario);
            }
            ps.executeUpdate();
        }
    }

    private void actualizarCartaRespuesta(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO carta,
            boolean soportaOposicion,
            Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_documento_analizado SET "
                + "confirmacion_respuesta = ?, fecha_respuesta = ?, numero_hoja_envio_respuesta = ?, "
                + (soportaOposicion ? "existe_oposicion = ?, " : "")
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            setStringOrNull(ps, index++, normalizarConfirmacionRespuesta(carta.getConfirmacionRespuesta()));
            setDateOrNull(ps, index++, carta.getFechaRespuesta());
            setStringOrNull(ps, index++, limitar(emptyToNull(carta.getNumeroHojaEnvioRespuesta()), 120));
            if (soportaOposicion) {
                setBooleanOrNull(ps, index++, carta.getExisteOposicion());
            }
            if (idUsuario == null) {
                ps.setNull(index++, Types.NUMERIC);
            } else {
                ps.setLong(index++, idUsuario);
            }
            ps.setLong(index++, carta.getIdDocumentoAnalizado());
            ps.setLong(index, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la carta de respuesta.");
            }
        }
    }

    public List<DocumentoAnalizadoDTO> listarPorExpediente(Connection conn, Long idExpediente) throws SQLException {
        return listarPorExpediente(conn, idExpediente, null);
    }

    public List<DocumentoAnalizadoDTO> listarPorExpediente(
            Connection conn,
            Long idExpediente,
            Long idExpedienteAnalisis) throws SQLException {
        List<DocumentoAnalizadoDTO> documentos = new ArrayList<>();
        if (conn == null || idExpediente == null) {
            return documentos;
        }
        boolean soportaRespuesta = soportaRespuestaDocumentoAnalizado(conn);
        boolean soportaPublicacion = soportaPublicacionPreparada(conn);
        boolean soportaNumeroDocumento = soportaNumeroDocumentoAnalizado(conn);
        boolean soportaDetalleObservacion = soportaDetalleObservacionDocumentoAnalizado(conn);
        boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
        boolean soportaJerarquia = soportaJerarquiaDocumentoAnalizado(conn);
        boolean soportaOposicion = soportaExisteOposicion(conn);
        String sql = "SELECT da.id_documento_analizado, da.id_expediente, "
                + (soportaAnalisisMultiple
                        ? "da.id_expediente_analisis, "
                        : "CAST(NULL AS NUMBER) AS id_expediente_analisis, ")
                + (soportaJerarquia
                        ? "da.id_documento_padre, NVL(da.nivel, 0) AS nivel, NVL(da.orden, 0) AS orden, "
                        + "da.estado_respuesta, da.activo, "
                        : "CAST(NULL AS NUMBER) AS id_documento_padre, 0 AS nivel, 0 AS orden, "
                        + "CAST(NULL AS VARCHAR2(40)) AS estado_respuesta, da.activo, ")
                + "da.creado_en, da.modificado_en, "
                + "td.codigo AS tipo_documento_codigo, td.nombre AS tipo_documento_nombre, "
                + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                + "da.fecha_documento, "
                + (soportaNumeroDocumento
                        ? "da.numero_documento, "
                        : "CAST(NULL AS VARCHAR2(120)) AS numero_documento, ")
                + (soportaDetalleObservacion
                        ? "da.detalle_observacion, "
                        : "CAST(NULL AS VARCHAR2(1000)) AS detalle_observacion, ")
                + "da.descripcion, "
                + (soportaRespuesta
                        ? "NVL(da.notificado, 0) AS notificado, da.fecha_acuse, "
                        + "NVL(da.requiere_respuesta, 0) AS requiere_respuesta, "
                        + "da.confirmacion_respuesta, da.fecha_respuesta, da.numero_hoja_envio_respuesta "
                        : "0 AS notificado, CAST(NULL AS DATE) AS fecha_acuse, "
                        + "0 AS requiere_respuesta, CAST(NULL AS VARCHAR2(20)) AS confirmacion_respuesta, "
                        + "CAST(NULL AS DATE) AS fecha_respuesta, "
                        + "CAST(NULL AS VARCHAR2(120)) AS numero_hoja_envio_respuesta ")
                + (soportaPublicacion
                        ? ", NVL(e.requiere_publicacion, 0) AS requiere_publicacion, "
                        + "(SELECT fecha_publicacion FROM ("
                        + " SELECT p.fecha_publicacion FROM expediente_publicacion p "
                        + " WHERE p.id_expediente = da.id_expediente AND p.activo = 1 "
                        + " ORDER BY p.creado_en DESC, p.id_expediente_publicacion DESC"
                        + ") WHERE ROWNUM = 1) AS fecha_publicacion "
                        : ", 0 AS requiere_publicacion, CAST(NULL AS DATE) AS fecha_publicacion ")
                + (soportaOposicion
                        ? ", da.existe_oposicion "
                        : ", CAST(NULL AS NUMBER(1)) AS existe_oposicion ")
                + "FROM expediente_documento_analizado da "
                + "JOIN expediente e ON e.id_expediente = da.id_expediente "
                + "LEFT JOIN tipo_documento_adjunto td ON td.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                + "WHERE da.id_expediente = ? AND da.activo = 1 "
                + (soportaAnalisisMultiple && idExpedienteAnalisis != null
                        ? "AND da.id_expediente_analisis = ? "
                        : "")
                + (soportaJerarquia
                        ? "ORDER BY NVL(da.id_documento_padre, da.id_documento_analizado), NVL(da.nivel, 0), "
                        + "NVL(da.orden, da.id_documento_analizado), da.id_documento_analizado"
                        : "ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            if (soportaAnalisisMultiple && idExpedienteAnalisis != null) {
                ps.setLong(2, idExpedienteAnalisis);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documentos.add(new DocumentoAnalizadoDTO(
                            getLongOrNull(rs, "id_documento_analizado"),
                            getLongOrNull(rs, "id_expediente"),
                            getLongOrNull(rs, "id_expediente_analisis"),
                            rs.getString("tipo_documento_codigo"),
                            rs.getString("tipo_documento_nombre"),
                            rs.getString("estado_documento_codigo"),
                            rs.getString("estado_documento_nombre"),
                            toLocalDate(rs.getDate("fecha_documento")),
                            rs.getString("numero_documento"),
                            rs.getString("descripcion"),
                            rs.getInt("notificado") == 1,
                            toLocalDate(rs.getDate("fecha_acuse")),
                            rs.getInt("requiere_respuesta") == 1,
                            rs.getString("confirmacion_respuesta"),
                            toLocalDate(rs.getDate("fecha_respuesta")),
                            rs.getString("numero_hoja_envio_respuesta"),
                            rs.getInt("requiere_publicacion") == 1,
                            toLocalDate(rs.getDate("fecha_publicacion")),
                            rs.getString("detalle_observacion"),
                            getLongOrNull(rs, "id_documento_padre"),
                            rs.getInt("nivel"),
                            rs.getInt("orden"),
                            rs.getString("estado_respuesta"),
                            rs.getInt("activo") == 1,
                            "",
                            toLocalDateTime(rs.getTimestamp("creado_en")),
                            "",
                            toLocalDateTime(rs.getTimestamp("modificado_en")),
                            getBooleanOrNull(rs, "existe_oposicion")));
                }
            }
        }
        return documentos;
    }

    public int contarPorExpediente(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expediente_documento_analizado WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void guardarDocumentosJerarquicos(
            Connection conn,
            Long idExpediente,
            List<DocumentoAnalizadoDTO> documentos,
            Long idUsuario) throws SQLException {
        if (conn == null || idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para guardar documentos de análisis.");
        }
        if (!soportaJerarquiaDocumentoAnalizado(conn)) {
            throw new SQLException("Faltan columnas jerárquicas en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 39_patch_documento_analizado_jerarquia.sql.");
        }
        Map<Long, Long> idsTemporales = new HashMap<Long, Long>();
        if (documentos == null) {
            return;
        }
        for (DocumentoAnalizadoDTO documento : documentos) {
            if (documento == null) {
                continue;
            }
            if (!documento.isActivo()) {
                darBajaDocumentoAnalizado(conn, idExpediente, documento.getIdDocumentoAnalizado(), idUsuario);
                continue;
            }
            validarJerarquia(documento, idsTemporales);
            Long idPadre = resolverIdPadreJerarquico(documento, idsTemporales);
            Long idDocumento = documento.getIdDocumentoAnalizado();
            if (idDocumento == null || idDocumento.longValue() < 0L) {
                Long nuevoId = insertarDocumentoAnalizadoJerarquico(conn, idExpediente, documento, idPadre, idUsuario);
                if (idDocumento != null) {
                    idsTemporales.put(idDocumento, nuevoId);
                }
            } else {
                actualizarDocumentoAnalizadoJerarquico(conn, idExpediente, documento, idPadre, idUsuario);
            }
        }
    }

    public void guardarDocumentoJerarquico(
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        if (idExpediente == null || documento == null) {
            throw new IllegalArgumentException("Seleccione un expediente para guardar el documento.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                List<DocumentoAnalizadoDTO> documentos = new ArrayList<DocumentoAnalizadoDTO>();
                documentos.add(documento);
                guardarDocumentosJerarquicos(conn, idExpediente, documentos, idUsuarioModificador);
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }
    }

    public void darBajaDocumentoAnalizado(
            Long idExpediente,
            Long idDocumentoAnalizado,
            Long idUsuarioModificador) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            darBajaDocumentoAnalizado(conn, idExpediente, idDocumentoAnalizado, idUsuarioModificador);
        }
    }

    public void darBajaDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            Long idDocumentoAnalizado,
            Long idUsuarioModificador) throws SQLException {
        if (conn == null || idExpediente == null || idDocumentoAnalizado == null || idDocumentoAnalizado.longValue() < 0L) {
            return;
        }
        String sql = "UPDATE expediente_documento_analizado SET activo = 0, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idUsuarioModificador == null) {
                ps.setNull(1, Types.NUMERIC);
            } else {
                ps.setLong(1, idUsuarioModificador);
            }
            ps.setLong(2, idDocumentoAnalizado);
            ps.setLong(3, idExpediente);
            ps.executeUpdate();
        }
    }

    public void actualizarRespuestaDocumentoAnalizado(
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        if (idExpediente == null || documento == null || documento.getIdDocumentoAnalizado() == null) {
            throw new IllegalArgumentException("Seleccione un documento analizado para actualizar la respuesta.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                actualizarRespuestaDocumentoAnalizado(conn, idExpediente, documento, idUsuarioModificador);
                actualizarPublicacionPreparada(conn, idExpediente, documento, idUsuarioModificador);
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }
    }

    private void actualizarPublicacionPreparada(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        if (!soportaPublicacionPreparada(conn)) {
            if (documento.isRequierePublicacion() || documento.getFechaPublicacion() != null) {
                throw new SQLException("La base de datos no tiene soporte completo de publicación preparada en EXPEDIENTE/EXPEDIENTE_PUBLICACION.");
            }
            return;
        }
        String sqlExpediente = "UPDATE expediente SET requiere_publicacion = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sqlExpediente)) {
            ps.setInt(1, documento.isRequierePublicacion() ? 1 : 0);
            if (idUsuarioModificador == null) {
                ps.setNull(2, Types.NUMERIC);
            } else {
                ps.setLong(2, idUsuarioModificador);
            }
            ps.setLong(3, idExpediente);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar el indicador de publicación del expediente.");
            }
        }
        if (!documento.isRequierePublicacion() && documento.getFechaPublicacion() == null) {
            return;
        }
        Long idPublicacion = obtenerPublicacionActiva(conn, idExpediente);
        if (idPublicacion == null) {
            insertarPublicacionPreparada(conn, idExpediente, documento.getFechaPublicacion(), idUsuarioModificador);
        } else {
            actualizarPublicacionPreparada(conn, idPublicacion, documento.getFechaPublicacion(), idUsuarioModificador);
        }
    }

    public void actualizarRespuestaDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        if (!soportaRespuestaDocumentoAnalizado(conn)) {
            throw new SQLException("Faltan columnas de respuesta en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 29_patch_documento_analizado_respuesta.sql.");
        }
        String confirmacion = normalizarConfirmacionRespuesta(documento.getConfirmacionRespuesta());
        LocalDate fechaRespuesta = documento.getFechaRespuesta();
        String hojaRespuesta = limitar(emptyToNull(documento.getNumeroHojaEnvioRespuesta()), 120);
        if (!documento.isRequiereRespuesta()) {
            confirmacion = null;
            fechaRespuesta = null;
            hojaRespuesta = null;
        } else if (!documento.isNotificado() || documento.getFechaAcuse() == null) {
            confirmacion = "PENDIENTE";
            fechaRespuesta = null;
            hojaRespuesta = null;
        }
        String sql = "UPDATE expediente_documento_analizado SET "
                + "notificado = ?, fecha_acuse = ?, confirmacion_respuesta = ?, fecha_respuesta = ?, "
                + "numero_hoja_envio_respuesta = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, documento.isNotificado() ? 1 : 0);
            setDateOrNull(ps, 2, documento.getFechaAcuse());
            if (confirmacion == null) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, confirmacion);
            }
            setDateOrNull(ps, 4, fechaRespuesta);
            if (hojaRespuesta == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, hojaRespuesta);
            }
            if (idUsuarioModificador == null) {
                ps.setNull(6, Types.NUMERIC);
            } else {
                ps.setLong(6, idUsuarioModificador);
            }
            ps.setLong(7, documento.getIdDocumentoAnalizado());
            ps.setLong(8, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la respuesta del documento analizado.");
            }
        }
    }

    public void insertarDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioCreador) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        boolean soportaRespuesta = soportaRespuestaDocumentoAnalizado(conn);
        boolean soportaNumeroDocumento = soportaNumeroDocumentoAnalizado(conn);
        boolean soportaDetalleObservacion = soportaDetalleObservacionDocumentoAnalizado(conn);
        boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
        if (!soportaNumeroDocumento && hasText(documento.getNumeroDocumento())) {
            throw new SQLException("Falta la columna NUMERO_DOCUMENTO en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 32_patch_documento_analizado_numero_documento.sql.");
        }
        if (!soportaDetalleObservacion && hasText(documento.getDetalleObservacion())) {
            throw new SQLException("Falta la columna DETALLE_OBSERVACION en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 33_patch_documento_analizado_detalle_observacion.sql.");
        }
        if (!soportaRespuesta && tieneDatosRespuesta(documento)) {
            throw new SQLException("Faltan columnas de respuesta en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 29_patch_documento_analizado_respuesta.sql.");
        }
        String sql = soportaRespuesta
                ? "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, "
                + (soportaAnalisisMultiple ? "id_expediente_analisis, " : "")
                + "id_tipo_documento_adjunto, id_estado_documento, fecha_documento, "
                + (soportaNumeroDocumento ? "numero_documento, " : "")
                + (soportaDetalleObservacion ? "detalle_observacion, " : "")
                + "descripcion, notificado, fecha_acuse, requiere_respuesta, confirmacion_respuesta, "
                + "fecha_respuesta, numero_hoja_envio_respuesta, activo, creado_por, creado_en"
                + ") VALUES (?, " + (soportaAnalisisMultiple ? "?, " : "") + "?, ?, ?, " + (soportaNumeroDocumento ? "?, " : "")
                + (soportaDetalleObservacion ? "?, " : "") + "?, ?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)"
                : "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, "
                + (soportaAnalisisMultiple ? "id_expediente_analisis, " : "")
                + "id_tipo_documento_adjunto, id_estado_documento, fecha_documento, "
                + (soportaNumeroDocumento ? "numero_documento, " : "")
                + (soportaDetalleObservacion ? "detalle_observacion, " : "")
                + "descripcion, activo, creado_por, creado_en"
                + ") VALUES (?, " + (soportaAnalisisMultiple ? "?, " : "") + "?, ?, ?, " + (soportaNumeroDocumento ? "?, " : "")
                + (soportaDetalleObservacion ? "?, " : "") + "?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            ps.setLong(index++, idExpediente);
            if (soportaAnalisisMultiple) {
                setLongOrNull(ps, index++, documento.getIdExpedienteAnalisis());
            }
            ps.setLong(index++, idTipoDocumento);
            ps.setLong(index++, idEstadoDocumento);
            setDateOrNull(ps, index++, documento.getFechaDocumento());
            if (soportaNumeroDocumento) {
                ps.setString(index++, limitar(documento.getNumeroDocumento(), 120));
            }
            if (soportaDetalleObservacion) {
                setStringOrNull(ps, index++, detalleObservacionPersistencia(documento));
            }
            ps.setString(index++, limitar(documento.getDescripcion(), 1000));
            int usuarioIndex;
            if (soportaRespuesta) {
                RespuestaPersistencia respuesta = respuestaPersistencia(documento, true);
                ps.setInt(index++, documento.isNotificado() ? 1 : 0);
                setDateOrNull(ps, index++, documento.getFechaAcuse());
                ps.setInt(index++, documento.isRequiereRespuesta() ? 1 : 0);
                setStringOrNull(ps, index++, respuesta.confirmacion);
                setDateOrNull(ps, index++, respuesta.fechaRespuesta);
                setStringOrNull(ps, index++, respuesta.hojaRespuesta);
                usuarioIndex = index;
            } else {
                usuarioIndex = index;
            }
            if (idUsuarioCreador == null) {
                ps.setNull(usuarioIndex, java.sql.Types.NUMERIC);
            } else {
                ps.setLong(usuarioIndex, idUsuarioCreador);
            }
            ps.executeUpdate();
        }
    }

    public void actualizarDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        boolean soportaRespuesta = soportaRespuestaDocumentoAnalizado(conn);
        boolean soportaNumeroDocumento = soportaNumeroDocumentoAnalizado(conn);
        boolean soportaDetalleObservacion = soportaDetalleObservacionDocumentoAnalizado(conn);
        boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
        if (!soportaNumeroDocumento && hasText(documento.getNumeroDocumento())) {
            throw new SQLException("Falta la columna NUMERO_DOCUMENTO en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 32_patch_documento_analizado_numero_documento.sql.");
        }
        if (!soportaDetalleObservacion && hasText(documento.getDetalleObservacion())) {
            throw new SQLException("Falta la columna DETALLE_OBSERVACION en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 33_patch_documento_analizado_detalle_observacion.sql.");
        }
        if (!soportaRespuesta && tieneDatosRespuesta(documento)) {
            throw new SQLException("Faltan columnas de respuesta en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 29_patch_documento_analizado_respuesta.sql.");
        }
        String sql = soportaRespuesta
                ? "UPDATE expediente_documento_analizado SET "
                + (soportaAnalisisMultiple ? "id_expediente_analisis = ?, " : "")
                + "id_tipo_documento_adjunto = ?, id_estado_documento = ?, fecha_documento = ?, "
                + (soportaNumeroDocumento ? "numero_documento = ?, " : "")
                + (soportaDetalleObservacion ? "detalle_observacion = ?, " : "")
                + "descripcion = ?, "
                + "notificado = ?, fecha_acuse = ?, requiere_respuesta = ?, confirmacion_respuesta = ?, "
                + "fecha_respuesta = ?, numero_hoja_envio_respuesta = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1"
                : "UPDATE expediente_documento_analizado SET "
                + (soportaAnalisisMultiple ? "id_expediente_analisis = ?, " : "")
                + "id_tipo_documento_adjunto = ?, id_estado_documento = ?, fecha_documento = ?, "
                + (soportaNumeroDocumento ? "numero_documento = ?, " : "")
                + (soportaDetalleObservacion ? "detalle_observacion = ?, " : "")
                + "descripcion = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            if (soportaAnalisisMultiple) {
                setLongOrNull(ps, index++, documento.getIdExpedienteAnalisis());
            }
            ps.setLong(index++, idTipoDocumento);
            ps.setLong(index++, idEstadoDocumento);
            setDateOrNull(ps, index++, documento.getFechaDocumento());
            if (soportaNumeroDocumento) {
                ps.setString(index++, limitar(documento.getNumeroDocumento(), 120));
            }
            if (soportaDetalleObservacion) {
                setStringOrNull(ps, index++, detalleObservacionPersistencia(documento));
            }
            ps.setString(index++, limitar(documento.getDescripcion(), 1000));
            int userIndex;
            int idIndex;
            if (soportaRespuesta) {
                RespuestaPersistencia respuesta = respuestaPersistencia(documento, true);
                ps.setInt(index++, documento.isNotificado() ? 1 : 0);
                setDateOrNull(ps, index++, documento.getFechaAcuse());
                ps.setInt(index++, documento.isRequiereRespuesta() ? 1 : 0);
                setStringOrNull(ps, index++, respuesta.confirmacion);
                setDateOrNull(ps, index++, respuesta.fechaRespuesta);
                setStringOrNull(ps, index++, respuesta.hojaRespuesta);
                userIndex = index++;
                idIndex = index;
            } else {
                userIndex = index++;
                idIndex = index;
            }
            if (idUsuarioModificador == null) {
                ps.setNull(userIndex, Types.NUMERIC);
            } else {
                ps.setLong(userIndex, idUsuarioModificador);
            }
            ps.setLong(idIndex, documento.getIdDocumentoAnalizado());
            ps.setLong(idIndex + 1, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el documento analizado.");
            }
        }
    }

    private Long insertarDocumentoAnalizadoJerarquico(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idDocumentoPadre,
            Long idUsuarioCreador) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
        boolean soportaDetalleObservacion = soportaDetalleObservacionDocumentoAnalizado(conn);
        String sql = "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, "
                + (soportaAnalisisMultiple ? "id_expediente_analisis, " : "")
                + "id_documento_padre, nivel, orden, id_tipo_documento_adjunto, id_estado_documento, "
                + "fecha_documento, numero_documento, "
                + (soportaDetalleObservacion ? "detalle_observacion, " : "")
                + "descripcion, "
                + "notificado, fecha_acuse, requiere_respuesta, confirmacion_respuesta, "
                + "fecha_respuesta, numero_hoja_envio_respuesta, estado_respuesta, "
                + "activo, creado_por, creado_en"
                + ") VALUES (?, " + (soportaAnalisisMultiple ? "?, " : "")
                + "?, ?, ?, ?, ?, ?, ?, " + (soportaDetalleObservacion ? "?, " : "")
                + "?, ?, ?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_DOCUMENTO_ANALIZADO"})) {
            int index = 1;
            ps.setLong(index++, idExpediente);
            if (soportaAnalisisMultiple) {
                setLongOrNull(ps, index++, documento.getIdExpedienteAnalisis());
            }
            setLongOrNull(ps, index++, idDocumentoPadre);
            ps.setInt(index++, documento.getNivel());
            ps.setInt(index++, documento.getOrden());
            ps.setLong(index++, idTipoDocumento);
            ps.setLong(index++, idEstadoDocumento);
            setDateOrNull(ps, index++, documento.getFechaDocumento());
            ps.setString(index++, limitar(documento.getNumeroDocumento(), 120));
            if (soportaDetalleObservacion) {
                setStringOrNull(ps, index++, detalleObservacionPersistencia(documento));
            }
            ps.setString(index++, limitar(documento.getDescripcion(), 1000));
            RespuestaPersistencia respuesta = respuestaPersistencia(documento, true);
            ps.setInt(index++, documento.isNotificado() ? 1 : 0);
            setDateOrNull(ps, index++, documento.getFechaAcuse());
            ps.setInt(index++, documento.isRequiereRespuesta() ? 1 : 0);
            setStringOrNull(ps, index++, respuesta.confirmacion);
            setDateOrNull(ps, index++, respuesta.fechaRespuesta);
            setStringOrNull(ps, index++, respuesta.hojaRespuesta);
            setStringOrNull(ps, index++, estadoRespuestaPersistencia(documento));
            if (idUsuarioCreador == null) {
                ps.setNull(index, Types.NUMERIC);
            } else {
                ps.setLong(index, idUsuarioCreador);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el identificador generado del documento analizado.");
    }

    private void actualizarDocumentoAnalizadoJerarquico(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idDocumentoPadre,
            Long idUsuarioModificador) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        boolean soportaAnalisisMultiple = soportaAnalisisMultiple(conn);
        boolean soportaDetalleObservacion = soportaDetalleObservacionDocumentoAnalizado(conn);
        String sql = "UPDATE expediente_documento_analizado SET "
                + (soportaAnalisisMultiple ? "id_expediente_analisis = ?, " : "")
                + "id_documento_padre = ?, nivel = ?, orden = ?, "
                + "id_tipo_documento_adjunto = ?, id_estado_documento = ?, fecha_documento = ?, "
                + "numero_documento = ?, "
                + (soportaDetalleObservacion ? "detalle_observacion = ?, " : "")
                + "descripcion = ?, "
                + "notificado = ?, fecha_acuse = ?, requiere_respuesta = ?, confirmacion_respuesta = ?, "
                + "fecha_respuesta = ?, numero_hoja_envio_respuesta = ?, estado_respuesta = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            if (soportaAnalisisMultiple) {
                setLongOrNull(ps, index++, documento.getIdExpedienteAnalisis());
            }
            setLongOrNull(ps, index++, idDocumentoPadre);
            ps.setInt(index++, documento.getNivel());
            ps.setInt(index++, documento.getOrden());
            ps.setLong(index++, idTipoDocumento);
            ps.setLong(index++, idEstadoDocumento);
            setDateOrNull(ps, index++, documento.getFechaDocumento());
            ps.setString(index++, limitar(documento.getNumeroDocumento(), 120));
            if (soportaDetalleObservacion) {
                setStringOrNull(ps, index++, detalleObservacionPersistencia(documento));
            }
            ps.setString(index++, limitar(documento.getDescripcion(), 1000));
            RespuestaPersistencia respuesta = respuestaPersistencia(documento, true);
            ps.setInt(index++, documento.isNotificado() ? 1 : 0);
            setDateOrNull(ps, index++, documento.getFechaAcuse());
            ps.setInt(index++, documento.isRequiereRespuesta() ? 1 : 0);
            setStringOrNull(ps, index++, respuesta.confirmacion);
            setDateOrNull(ps, index++, respuesta.fechaRespuesta);
            setStringOrNull(ps, index++, respuesta.hojaRespuesta);
            setStringOrNull(ps, index++, estadoRespuestaPersistencia(documento));
            if (idUsuarioModificador == null) {
                ps.setNull(index++, Types.NUMERIC);
            } else {
                ps.setLong(index++, idUsuarioModificador);
            }
            ps.setLong(index++, documento.getIdDocumentoAnalizado());
            ps.setLong(index, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el documento analizado.");
            }
        }
    }

    public void actualizarEstadoDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        String sql = "UPDATE expediente_documento_analizado SET "
                + "id_estado_documento = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEstadoDocumento);
            if (idUsuarioModificador == null) {
                ps.setNull(2, java.sql.Types.NUMERIC);
            } else {
                ps.setLong(2, idUsuarioModificador);
            }
            ps.setLong(3, documento.getIdDocumentoAnalizado());
            ps.setLong(4, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el estado del documento analizado.");
            }
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Boolean getBooleanOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value == 1;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static void setDateOrNull(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }

    private static void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    private static void setBooleanOrNull(PreparedStatement ps, int index, Boolean value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setInt(index, value.booleanValue() ? 1 : 0);
        }
    }

    private static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static boolean soportaRespuestaDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT column_name) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name IN ("
                + "'NOTIFICADO', 'FECHA_ACUSE', 'REQUIERE_RESPUESTA', "
                + "'CONFIRMACION_RESPUESTA', 'FECHA_RESPUESTA', 'NUMERO_HOJA_ENVIO_RESPUESTA'"
                + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 6;
        }
    }

    private static boolean soportaNumeroDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name = 'NUMERO_DOCUMENTO'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaDetalleObservacionDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name = 'DETALLE_OBSERVACION'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaExisteOposicion(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name = 'EXISTE_OPOSICION'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaClasificacionTipoDocumento(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'TIPO_DOCUMENTO_ADJUNTO' "
                + "AND column_name = 'CLASIFICACION'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaJerarquiaDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT column_name) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name IN ('ID_DOCUMENTO_PADRE', 'NIVEL', 'ORDEN', 'ESTADO_RESPUESTA')";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 4;
        }
    }

    private static boolean soportaAnalisisMultiple(Connection conn) throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(1) FROM user_tables WHERE table_name = 'EXPEDIENTE_ANALISIS') + "
                + "(SELECT COUNT(1) FROM user_tab_columns WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' AND column_name = 'ID_EXPEDIENTE_ANALISIS') "
                + "AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt("total") == 2;
        }
    }

    private static boolean soportaPublicacionPreparada(Connection conn) throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(1) FROM user_tab_columns WHERE table_name = 'EXPEDIENTE' AND column_name = 'REQUIERE_PUBLICACION') + "
                + "(SELECT COUNT(1) FROM user_tables WHERE table_name = 'EXPEDIENTE_PUBLICACION') + "
                + "(SELECT COUNT(1) FROM user_tab_columns WHERE table_name = 'EXPEDIENTE_PUBLICACION' AND column_name = 'FECHA_PUBLICACION') "
                + "AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt("total") == 3;
        }
    }

    private static boolean soportaIntentosNotificacionDocumento(Connection conn) throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(1) FROM user_tables WHERE table_name = 'EXPEDIENTE_NOTIFICACION') + "
                + "(SELECT COUNT(1) FROM user_tables WHERE table_name = 'EXPEDIENTE_CARGO_ACUSE') + "
                + "(SELECT COUNT(1) FROM user_tab_columns WHERE table_name = 'EXPEDIENTE_NOTIFICACION' AND column_name = 'ID_DOCUMENTO_ANALIZADO') + "
                + "(SELECT COUNT(1) FROM user_tab_columns WHERE table_name = 'EXPEDIENTE_NOTIFICACION' AND column_name = 'CODIGO_NOTIFICACION') "
                + "AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt("total") == 4;
        }
    }

    private static int obtenerSiguienteIntentoNotificacion(Connection conn, Long idDocumentoAnalizado) throws SQLException {
        String sql = "SELECT NVL(MAX(numero_intento), 0) + 1 AS siguiente "
                + "FROM expediente_notificacion "
                + "WHERE id_documento_analizado = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idDocumentoAnalizado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("siguiente") : 1;
            }
        }
    }

    private static Long obtenerPublicacionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_expediente_publicacion FROM ("
                + "SELECT id_expediente_publicacion FROM expediente_publicacion "
                + "WHERE id_expediente = ? AND activo = 1 "
                + "ORDER BY creado_en DESC, id_expediente_publicacion DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return getLongOrNull(rs, "id_expediente_publicacion");
            }
        }
    }

    private static void insertarPublicacionPreparada(
            Connection conn,
            Long idExpediente,
            LocalDate fechaPublicacion,
            Long idUsuario) throws SQLException {
        String sql = "INSERT INTO expediente_publicacion ("
                + "id_expediente, tipo_publicacion, estado_publicacion, fecha_generacion, fecha_publicacion, "
                + "observacion, activo, creado_por, creado_en"
                + ") VALUES (?, 'CARTA_RESPUESTA', 'PENDIENTE_PUBLICACION', SYSDATE, ?, "
                + "'Publicación preparada desde Asignación - Cartas de respuesta.', 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            setDateOrNull(ps, 2, fechaPublicacion);
            if (idUsuario == null) {
                ps.setNull(3, Types.NUMERIC);
            } else {
                ps.setLong(3, idUsuario);
            }
            ps.executeUpdate();
        }
    }

    private static void actualizarPublicacionPreparada(
            Connection conn,
            Long idPublicacion,
            LocalDate fechaPublicacion,
            Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_publicacion SET "
                + "tipo_publicacion = NVL(tipo_publicacion, 'CARTA_RESPUESTA'), "
                + "estado_publicacion = CASE WHEN estado_publicacion IS NULL THEN 'PENDIENTE_PUBLICACION' ELSE estado_publicacion END, "
                + "fecha_publicacion = ?, "
                + "observacion = NVL(observacion, 'Publicación preparada desde Asignación - Cartas de respuesta.'), "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_publicacion = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setDateOrNull(ps, 1, fechaPublicacion);
            if (idUsuario == null) {
                ps.setNull(2, Types.NUMERIC);
            } else {
                ps.setLong(2, idUsuario);
            }
            ps.setLong(3, idPublicacion);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar la publicación preparada.");
            }
        }
    }

    private static String normalizarConfirmacionRespuesta(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        normalized = normalized.replace('Í', 'I');
        if ("SI".equals(normalized) || "NO".equals(normalized) || "PENDIENTE".equals(normalized)) {
            return normalized;
        }
        return "PENDIENTE";
    }

    private static RespuestaPersistencia respuestaPersistencia(
            DocumentoAnalizadoDTO documento,
            boolean incluirRequiereRespuesta) {
        boolean requiereRespuesta = incluirRequiereRespuesta && documento.isRequiereRespuesta();
        String confirmacion = requiereRespuesta
                ? normalizarConfirmacionRespuesta(documento.getConfirmacionRespuesta())
                : null;
        LocalDate fechaRespuesta = requiereRespuesta ? documento.getFechaRespuesta() : null;
        String hojaRespuesta = requiereRespuesta ? limitar(emptyToNull(documento.getNumeroHojaEnvioRespuesta()), 120) : null;
        return new RespuestaPersistencia(confirmacion, fechaRespuesta, hojaRespuesta);
    }

    private static boolean tieneDatosRespuesta(DocumentoAnalizadoDTO documento) {
        return documento.isNotificado()
                || documento.getFechaAcuse() != null
                || documento.isRequiereRespuesta()
                || !emptyString(documento.getConfirmacionRespuesta()).isEmpty()
                || documento.getFechaRespuesta() != null
                || !emptyString(documento.getNumeroHojaEnvioRespuesta()).isEmpty();
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static String emptyString(String value) {
        return value == null ? "" : value.trim();
    }

    private static String detalleObservacionPersistencia(DocumentoAnalizadoDTO documento) {
        return esEstadoObservado(documento.getEstadoDocumentoCodigo())
                ? limitar(emptyToNull(documento.getDetalleObservacion()), 1000)
                : null;
    }

    private static List<CatalogoItemDTO> filtrarEstadosDocumentoAnalizado(List<CatalogoItemDTO> estados) {
        List<CatalogoItemDTO> filtrados = new ArrayList<CatalogoItemDTO>();
        if (estados == null) {
            return filtrados;
        }
        for (CatalogoItemDTO estado : estados) {
            if (estado != null && esEstadoDocumentoAnalizadoPermitido(estado.getCodigo())) {
                filtrados.add(estado);
            }
        }
        return filtrados;
    }

    private static boolean esEstadoDocumentoAnalizadoPermitido(String codigo) {
        String value = codigo == null ? "" : codigo.trim().toUpperCase();
        return "EN_PROYECTO".equals(value)
                || "EN_DESPACHO".equals(value)
                || "EMITIDO".equals(value)
                || "OBSERVADO".equals(value);
    }

    private static boolean esEstadoObservado(String codigo) {
        return "OBSERVADO".equalsIgnoreCase(codigo == null ? "" : codigo.trim());
    }

    private static void validarJerarquia(
            DocumentoAnalizadoDTO documento,
            Map<Long, Long> idsTemporales) throws SQLException {
        if (documento.getNivel() < 0 || documento.getNivel() > 1) {
            throw new SQLException("Solo se permiten documentos principales e hijos directos.");
        }
        if (documento.getNivel() == 0 && documento.getIdDocumentoPadre() != null) {
            throw new SQLException("Un documento principal no debe tener documento padre.");
        }
        if (documento.getNivel() == 1) {
            Long idPadre = documento.getIdDocumentoPadre();
            if (idPadre == null) {
                throw new SQLException("Toda respuesta debe estar asociada a un documento analizado que requiere respuesta.");
            }
            if (idPadre.longValue() < 0L && (idsTemporales == null || !idsTemporales.containsKey(idPadre))) {
                throw new SQLException("El documento padre debe guardarse antes de registrar el documento hijo.");
            }
        }
    }

    private static Long resolverIdPadreJerarquico(
            DocumentoAnalizadoDTO documento,
            Map<Long, Long> idsTemporales) {
        Long idPadre = documento.getIdDocumentoPadre();
        if (idPadre == null) {
            return null;
        }
        if (idPadre.longValue() < 0L && idsTemporales != null) {
            return idsTemporales.get(idPadre);
        }
        return idPadre;
    }

    private static String estadoRespuestaPersistencia(DocumentoAnalizadoDTO documento) {
        String estado = documento.getEstadoRespuesta();
        if (estado != null && !estado.trim().isEmpty()) {
            return limitar(estado.trim().toUpperCase(java.util.Locale.ROOT), 40);
        }
        return documento.isRequiereRespuesta() ? "PENDIENTE" : null;
    }

    private static String nombrePersona(String alias) {
        return "TRIM(NVL(" + alias + ".razon_social, TRIM(NVL(" + alias + ".nombres, '') || ' ' || NVL(" + alias + ".apellidos, ''))))";
    }

    private static void rollbackSilencioso(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // rollback de contingencia
        }
    }

    private static String limitar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static final class RespuestaPersistencia {
        private final String confirmacion;
        private final LocalDate fechaRespuesta;
        private final String hojaRespuesta;

        private RespuestaPersistencia(String confirmacion, LocalDate fechaRespuesta, String hojaRespuesta) {
            this.confirmacion = confirmacion;
            this.fechaRespuesta = fechaRespuesta;
            this.hojaRespuesta = hojaRespuesta;
        }
    }
}
