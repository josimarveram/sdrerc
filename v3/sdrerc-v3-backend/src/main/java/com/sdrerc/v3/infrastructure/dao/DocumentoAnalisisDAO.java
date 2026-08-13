package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.application.CalendarioLaboralService;
import com.sdrerc.v3.domain.AsignacionCartaRespuestaDTO;
import com.sdrerc.v3.domain.PlazoConfiguracionDTO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Subconjunto de com.sdrerc.infrastructure.sdrercapp.dao.DocumentoAnalisisDAO (V2, 2838 líneas —
 * el DAO grande de documentos analizados que respalda Análisis/Verificación/Ejecución/
 * Notificación). Este port en V3 incluye únicamente {@link #listarCartasRespuestaPendientes}
 * (bandeja "Cartas de respuesta" de Asignación) y sus dos helpers privados de cálculo de
 * vencimiento — el resto del DAO de V2 (guardar/eliminar documentos, asignación a
 * validador/notificador, intentos de notificación, etc.) pertenece a Análisis/Notificación,
 * todavía no portados a V3.
 *
 * <p>Alcance de este incremento: solo lectura (listado de la bandeja). El botón "Registrar
 * Asignación" de V2 (deriva el expediente de vuelta a Análisis con estado ANALISIS/DERIVADO, y
 * exige que la carta intermedia ya tenga una carta de respuesta agregada como documento hijo) NO
 * se porta todavía: depende de gestión jerárquica de documentos analizados (padre/hijo), que es
 * territorio de Análisis (Fase 4), no construido en V3 hasta ahora.</p>
 */
@Repository
public class DocumentoAnalisisDAO {

    private final DataSource dataSource;
    private final CalendarioLaboralService calendarioLaboralService;
    private final PlazoConfiguracionDAO plazoConfiguracionDAO;

    public DocumentoAnalisisDAO(
            DataSource dataSource,
            CalendarioLaboralService calendarioLaboralService,
            PlazoConfiguracionDAO plazoConfiguracionDAO) {
        this.dataSource = dataSource;
        this.calendarioLaboralService = calendarioLaboralService;
        this.plazoConfiguracionDAO = plazoConfiguracionDAO;
    }

    // Estado Final del documento en Notificación (4 estados, ver v3/CLAUDE.md y AGENTS.md):
    // POR_NOTIFICAR (sin intentos), ATENDIDO (algún intento EXITOSA/ubicado), POR_PUBLICAR
    // (intento 1 y 2 ambos FALLIDA/no ubicado) y PENDIENTE (resto). Se deriva de
    // expediente_notificacion + estado_notificacion, sin columna nueva.
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

    /**
     * Bandeja "Cartas de respuesta" de Asignación: documentos analizados INTERMEDIO que requieren
     * respuesta y ya fueron notificados, cuyo estado final de notificación es ATENDIDO (el
     * ciudadano ya fue ubicado en algún intento directo, o el documento ya quedó Publicado), y que
     * el expediente todavía NO volvió a etapa ANALISIS (en cuanto se deriva de vuelta, deja de
     * listarse aquí — el seguimiento pasa a Análisis).
     */
    public List<AsignacionCartaRespuestaDTO> listarCartasRespuestaPendientes() throws SQLException {
        List<AsignacionCartaRespuestaDTO> items = new ArrayList<>();
        String sqlInterno = "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, "
                + "esol.numero_expediente_sgd, "
                + nombrePersona("p") + " AS titular, "
                + "tda.codigo AS tipo_documento_codigo, tda.nombre AS tipo_documento_nombre, "
                + "ed.nombre AS estado_documento_nombre, "
                + "et.codigo AS etapa_codigo, "
                + "da.fecha_documento, da.numero_documento, "
                + "da.descripcion, NVL(da.requiere_respuesta, 0) AS requiere_respuesta, "
                + "NVL(da.notificado, 0) AS notificado, da.fecha_acuse, da.confirmacion_respuesta, "
                + "da.fecha_respuesta, da.numero_hoja_envio_respuesta, "
                + "NVL(e.requiere_publicacion, 0) AS requiere_publicacion, "
                + "(SELECT fecha_publicacion FROM ("
                + " SELECT p2.fecha_publicacion FROM expediente_publicacion p2 "
                + " WHERE p2.id_expediente = da.id_expediente AND p2.activo = 1 "
                + " ORDER BY p2.creado_en DESC, p2.id_expediente_publicacion DESC"
                + ") WHERE ROWNUM = 1) AS fecha_publicacion_edicto, "
                + "(SELECT n2.fecha_envio FROM expediente_notificacion n2 "
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
                + "AND UPPER(NVL(tda.clasificacion, '')) = 'INTERMEDIO' ";
        String sql = "SELECT * FROM (" + sqlInterno + ") "
                + "WHERE estado_final_notificacion_codigo = 'ATENDIDO' "
                + "AND UPPER(etapa_codigo) <> 'ANALISIS' "
                + "ORDER BY fecha_documento DESC NULLS LAST, id_documento_analizado DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Map<String, Integer> diasPlazoCache = new HashMap<>();
            while (rs.next()) {
                String tipoDocumentoCodigo = rs.getString("tipo_documento_codigo");
                String etapaCodigo = rs.getString("etapa_codigo");
                LocalDate fechaAcuse = toLocalDate(rs.getDate("fecha_acuse"));
                LocalDate fechaPublicacionEdicto = toLocalDate(rs.getDate("fecha_publicacion_edicto"));
                LocalDate fechaPublicacionNotif = toLocalDate(rs.getDate("fecha_publicacion_notif"));

                VencimientoCarta vencimiento = resolverVencimientoCarta(
                        conn, tipoDocumentoCodigo, fechaAcuse, fechaPublicacionNotif, fechaPublicacionEdicto, diasPlazoCache);

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
     * Alerta de vencimiento en cascada de una carta intermedia: mientras el documento no tenga
     * Fecha Publ. Edicto registrada, la alerta activa es el vencimiento de RESPUESTA (desde Fecha
     * Acuse o, si no hay acuse directo, desde Fecha Publ. Notif.). En cuanto se registra Fecha
     * Publ. Edicto, la alerta cambia a PUBLICACION (desde esa fecha, plazo = código +
     * "_PUBLICACION"). Sin plazo configurado para el código que corresponda, no hay alerta (null).
     */
    private VencimientoCarta resolverVencimientoCarta(
            Connection conn,
            String tipoDocumentoCodigo,
            LocalDate fechaAcuse,
            LocalDate fechaPublicacionNotif,
            LocalDate fechaPublicacionEdicto,
            Map<String, Integer> diasPlazoCache) throws SQLException {
        if (fechaPublicacionEdicto != null) {
            Integer diasPlazoPublicacion = resolverDiasPlazoCarta(conn, tipoDocumentoCodigo + "_PUBLICACION", diasPlazoCache);
            if (diasPlazoPublicacion != null) {
                LocalDate fechaVencimiento = calendarioLaboralService.calcularFechaVencimientoHabil(
                        conn, fechaPublicacionEdicto, diasPlazoPublicacion);
                long dias = calendarioLaboralService.calcularDiasHabilesRestantes(conn, LocalDate.now(), fechaVencimiento);
                return new VencimientoCarta(dias, diasPlazoPublicacion, fechaVencimiento, "PUBLICACION");
            }
        }
        LocalDate fechaInicioRespuesta = fechaAcuse != null ? fechaAcuse : fechaPublicacionNotif;
        if (fechaInicioRespuesta != null) {
            Integer diasPlazoRespuesta = resolverDiasPlazoCarta(conn, tipoDocumentoCodigo, diasPlazoCache);
            if (diasPlazoRespuesta != null) {
                LocalDate fechaVencimiento = calendarioLaboralService.calcularFechaVencimientoHabil(
                        conn, fechaInicioRespuesta, diasPlazoRespuesta);
                long dias = calendarioLaboralService.calcularDiasHabilesRestantes(conn, LocalDate.now(), fechaVencimiento);
                return new VencimientoCarta(dias, diasPlazoRespuesta, fechaVencimiento, "RESPUESTA");
            }
        }
        return null;
    }

    /**
     * Días configurados en PLAZO_CONFIGURACION para el código de plazo de una carta intermedia,
     * cacheado por código dentro de la misma consulta.
     */
    private Integer resolverDiasPlazoCarta(Connection conn, String codigoPlazo, Map<String, Integer> cache) throws SQLException {
        if (codigoPlazo == null) {
            return null;
        }
        if (cache.containsKey(codigoPlazo)) {
            return cache.get(codigoPlazo);
        }
        PlazoConfiguracionDTO plazo = plazoConfiguracionDAO.obtenerPlazoPorCodigo(conn, codigoPlazo);
        Integer dias = plazo == null || plazo.getDiasPlazo() == null || plazo.getDiasPlazo() <= 0
                ? null
                : plazo.getDiasPlazo();
        cache.put(codigoPlazo, dias);
        return dias;
    }

    private static String nombrePersona(String alias) {
        return "TRIM(NVL(" + alias + ".razon_social, TRIM(NVL(" + alias + ".nombres, '') || ' ' || NVL(" + alias + ".apellidos, ''))))";
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
