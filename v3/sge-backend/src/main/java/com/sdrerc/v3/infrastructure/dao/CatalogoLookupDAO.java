package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.CatalogoItemDTO;
import com.sdrerc.v3.domain.CentroAtencionDTO;
import com.sdrerc.v3.domain.MunicipalidadDTO;
import com.sdrerc.v3.domain.UnidadOrganicaDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Subconjunto de com.sdrerc.infrastructure.sdrercapp.dao.CatalogoLookupDAO (V2): solo los
 * lookups que necesita el registro manual (etapa/estado/tipo de movimiento/canal de
 * recepción/tipo de acta por código, más los 4 catálogos para combos: canal de recepción,
 * procedimiento registral, tipo de documento, tipo de acta). El resto de catálogos de V2 (tipo de
 * notificación, estado de notificación, etc.) se agregan cuando el modulo correspondiente se
 * porte. Mismo SQL.
 */
@Repository
public class CatalogoLookupDAO {

    private final DataSource dataSource;

    public CatalogoLookupDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<CatalogoItemDTO> listarCanalesRecepcion() throws SQLException {
        return listarCatalogo("canal_recepcion");
    }

    public List<CatalogoItemDTO> listarProcedimientosRegistrales() throws SQLException {
        return listarCatalogo("procedimiento_registral");
    }

    public List<CatalogoItemDTO> listarTiposDocumento() throws SQLException {
        return listarCatalogo("tipo_documento");
    }

    public List<CatalogoItemDTO> listarTiposActa() throws SQLException {
        return listarCatalogo("tipo_acta");
    }

    /**
     * Catálogos de la subpestaña "Análisis" (20/08/2026) — mismo patrón que los demás catálogos de
     * este DAO. "Estado documento" se filtra a los 4 códigos que realmente aplican a Análisis/
     * Ejecución (EN_PROYECTO/EN_DESPACHO/EMITIDO/OBSERVADO — port literal de
     * {@code DocumentoAnalisisDAO#esEstadoDocumentoAnalizadoPermitido} en V2; Notificación usa un
     * subconjunto distinto que incluye VALIDADO en vez de EN_PROYECTO, fuera de alcance acá).
     *
     * <p>Bug real corregido (20/08/2026, mismo día, reportado por el usuario: "la lista del combo
     * Tipo... no me está cargando la data actualizada"): este método delegaba en el genérico
     * {@code listarCatalogo("tipo_documento_adjunto")} (sin filtro, trae TODOS los tipos de
     * documento del sistema — Verificación/Ejecución/Notificación incluidos).</p>
     *
     * <p>28/08/2026, pedido explícito del usuario: el catálogo real de {@code tipo_documento_adjunto}
     * ya distingue por módulo con códigos propios ({@code ANALISIS_DOC_*}/{@code EJECUCION_DOC_*}/
     * {@code ASIGNACION_CARTA_RESPUESTA_*}) — este método se acota a códigos que CONTIENEN
     * {@code ANALISIS_DOC} (no solo que empiecen con {@code ANALISIS_}, para no volver a mezclar con
     * ningún otro prefijo), activos, ordenados por código. Ver
     * {@link #listarTiposDocumentoAdjuntoEjecucion()}/{@link #listarTiposDocumentoAdjuntoCartaRespuesta()}
     * para los combos equivalentes de Ejecución/Cartas de Respuesta.</p>
     */
    public List<CatalogoItemDTO> listarTiposDocumentoAdjuntoAnalisis() throws SQLException {
        return listarTiposDocumentoAdjuntoPorSubcadena("ANALISIS_DOC");
    }

    /** Combo "Tipo documento" de la subpestaña "Ejecutar" (Bandeja Ejecución), 28/08/2026, pedido explícito del usuario. */
    public List<CatalogoItemDTO> listarTiposDocumentoAdjuntoEjecucion() throws SQLException {
        return listarTiposDocumentoAdjuntoPorSubcadena("EJECUCION_DOC");
    }

    /** Combo "Tipo documento" del panel "Registrar Asignación" (Bandeja Cartas de Respuesta), 28/08/2026, pedido explícito del usuario. */
    public List<CatalogoItemDTO> listarTiposDocumentoAdjuntoCartaRespuesta() throws SQLException {
        return listarTiposDocumentoAdjuntoPorSubcadena("ASIGNACION_CARTA_RESPUESTA");
    }

    private List<CatalogoItemDTO> listarTiposDocumentoAdjuntoPorSubcadena(String subcadena) throws SQLException {
        String sql = "SELECT codigo, nombre, clasificacion FROM tipo_documento_adjunto "
                + "WHERE activo = 1 AND codigo LIKE ? ORDER BY codigo";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + subcadena + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<CatalogoItemDTO> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(new CatalogoItemDTO(rs.getString("codigo"), rs.getString("nombre"), rs.getString("clasificacion")));
                }
                return items;
            }
        }
    }

    public List<CatalogoItemDTO> listarEstadosDocumentoAnalisis() throws SQLException {
        List<CatalogoItemDTO> todos = listarCatalogo("estado_documento");
        List<CatalogoItemDTO> filtrados = new ArrayList<>();
        for (CatalogoItemDTO estado : todos) {
            String codigo = estado.codigo() == null ? "" : estado.codigo().trim().toUpperCase();
            if (codigo.equals("EN_PROYECTO") || codigo.equals("EN_DESPACHO") || codigo.equals("EMITIDO") || codigo.equals("OBSERVADO")) {
                filtrados.add(estado);
            }
        }
        return filtrados;
    }

    public List<CatalogoItemDTO> listarResultadosAnalisis() throws SQLException {
        return listarCatalogo("tipo_resultado_evaluacion");
    }

    /**
     * Estado documento para las grillas de Notificación (21/08/2026, Bandeja Asignación, Parte 2) —
     * a diferencia de {@link #listarEstadosDocumentoAnalisis()} (Análisis/Ejecución, EN_PROYECTO en
     * vez de VALIDADO), acá se filtra a los 4 códigos que documenta el CLAUDE.md raíz para el combo
     * real de V2 de esta bandeja: "Emitido/En despacho/Observado/Validado". {@code ASIGNADO} (estado
     * de EXPEDIENTE, no de documento) no aplica acá.
     */
    public List<CatalogoItemDTO> listarEstadosDocumentoNotificacion() throws SQLException {
        List<CatalogoItemDTO> todos = listarCatalogo("estado_documento");
        List<CatalogoItemDTO> filtrados = new ArrayList<>();
        for (CatalogoItemDTO estado : todos) {
            String codigo = estado.codigo() == null ? "" : estado.codigo().trim().toUpperCase();
            if (codigo.equals("EN_DESPACHO") || codigo.equals("VALIDADO") || codigo.equals("EMITIDO") || codigo.equals("OBSERVADO")) {
                filtrados.add(estado);
            }
        }
        return filtrados;
    }

    public List<CatalogoItemDTO> listarMotivosNoCorresponde() throws SQLException {
        return listarCatalogo("motivo_no_corresponde");
    }

    public List<CatalogoItemDTO> listarTiposObservacion() throws SQLException {
        return listarCatalogo("tipo_observacion");
    }

    /** Combo "Resultado" de Ejecución (20/08/2026) — tabla dedicada, sin filtro adicional (port literal de V2). */
    public List<CatalogoItemDTO> listarResultadosEjecucion() throws SQLException {
        return listarCatalogo("tipo_resultado_ejecucion");
    }

    /** Combo "Motivo de corrección" — usado por Ejecución en Observación/Error material/Devolver a Análisis (port literal de V2). */
    public List<CatalogoItemDTO> listarMotivosCorreccion() throws SQLException {
        return listarCatalogo("motivo_correccion");
    }

    /** Códigos de tipo_documento_adjunto cuya clasificación es INTERMEDIO (auto-fuerza "¿Requiere respuesta?" en la grilla). */
    public java.util.Set<String> listarCodigosTipoDocumentoIntermedio() throws SQLException {
        java.util.Set<String> codigos = new java.util.HashSet<>();
        String sql = "SELECT codigo FROM tipo_documento_adjunto WHERE activo = 1 AND UPPER(clasificacion) = 'INTERMEDIO'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                codigos.add(rs.getString("codigo"));
            }
        }
        return codigos;
    }

    public Long obtenerTipoDocumentoAdjuntoId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_documento_adjunto", "id_tipo_documento_adjunto", codigo);
    }

    public Long obtenerEstadoDocumentoId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "estado_documento", "id_estado_documento", codigo);
    }

    /** Bandeja Notificación (3ra pestaña) — combo "Modalidad" del intento (VIRTUAL/PRESENCIAL_1/PRESENCIAL_2). */
    public Long obtenerTipoNotificacionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_notificacion", "id_tipo_notificacion", codigo);
    }

    /** Bandeja Notificación — combo "Estado"/"Estado Notificación" del intento (PENDIENTE/ENVIADA/EXITOSA/FALLIDA). */
    public Long obtenerEstadoNotificacionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "estado_notificacion", "id_estado_notificacion", codigo);
    }

    /** Bandeja Notificación — confirmación de recepción (expediente_cargo_acuse.id_estado_cargo_acuse). */
    public Long obtenerEstadoCargoAcuseId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "estado_cargo_acuse", "id_estado_cargo_acuse", codigo);
    }

    public Long obtenerTipoResultadoEvaluacionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_resultado_evaluacion", "id_tipo_resultado_evaluacion", codigo);
    }

    /** Combo "Resultado" de Ejecución (tabla propia, ver {@link #listarResultadosEjecucion}) — usado por Carga Inicial (Incremento 2). */
    public Long obtenerTipoResultadoEjecucionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_resultado_ejecucion", "id_tipo_resultado_ejecucion", codigo);
    }

    public Long obtenerMotivoNoCorrespondeId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "motivo_no_corresponde", "id_motivo_no_corresponde", codigo);
    }

    public Long obtenerMotivoCorreccionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "motivo_correccion", "id_motivo_correccion", codigo);
    }

    public Long obtenerTipoObservacionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_observacion", "id_tipo_observacion", codigo);
    }

    /**
     * Catálogo de centros de atención (14/08/2026, iniciativa "Solicitante con 3 tipos", ver
     * v3/CLAUDE.md y scripts 101/102). Se traen todos los activos de una sola vez (240 filas
     * cargadas por el script 102, filtradas a categoría I/III) — el frontend filtra en memoria por
     * denominación/código SIO/departamento/provincia/distrito, sin necesidad de un endpoint de
     * búsqueda paginado para un catálogo de este tamaño.
     */
    public List<CentroAtencionDTO> listarCentrosAtencion() throws SQLException {
        String sql = "SELECT id_centro_atencion, denominacion, categorizacion, departamento, provincia, "
                + "distrito, tipo_local, oficina_regional, codigo_sio, nombre_anterior_res_019 "
                + "FROM centros_atencion WHERE activo = 1 ORDER BY denominacion";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<CentroAtencionDTO> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new CentroAtencionDTO(
                        rs.getLong("id_centro_atencion"),
                        rs.getString("denominacion"),
                        rs.getString("categorizacion"),
                        rs.getString("departamento"),
                        rs.getString("provincia"),
                        rs.getString("distrito"),
                        rs.getString("tipo_local"),
                        rs.getString("oficina_regional"),
                        rs.getString("codigo_sio"),
                        rs.getString("nombre_anterior_res_019")));
            }
            return items;
        }
    }

    /**
     * Catálogo de municipalidades (14/08/2026, 4to tipo de solicitante "Municipalidad", ver
     * v3/CLAUDE.md y scripts 103/104). Se traen todas las activas de una sola vez (1652 filas
     * cargadas por el script 104) — el frontend filtra en memoria por denominación/ubicación, sin
     * necesidad de un endpoint de búsqueda paginado. Incluye {@code centro_poblado} (18/08/2026,
     * pedido explícito del usuario) para poder desambiguar homónimos reales del catálogo.
     */
    public List<MunicipalidadDTO> listarMunicipalidades() throws SQLException {
        String sql = "SELECT id_municipalidad, denominacion, tipo, codigo, departamento, provincia, distrito, "
                + "centro_poblado FROM municipalidades WHERE activo = 1 ORDER BY denominacion";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<MunicipalidadDTO> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new MunicipalidadDTO(
                        rs.getLong("id_municipalidad"),
                        rs.getString("denominacion"),
                        rs.getString("tipo"),
                        rs.getString("codigo"),
                        rs.getString("departamento"),
                        rs.getString("provincia"),
                        rs.getString("distrito"),
                        rs.getString("centro_poblado")));
            }
            return items;
        }
    }

    /** Catálogo fijo de unidades orgánicas (8 valores, script 101; glosa agregada por script 107). */
    public List<UnidadOrganicaDTO> listarUnidadesOrganicas() throws SQLException {
        String sql = "SELECT id_unidad_organica, nombre, glosa FROM unidades_organicas WHERE activo = 1 ORDER BY nombre";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<UnidadOrganicaDTO> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new UnidadOrganicaDTO(
                        rs.getLong("id_unidad_organica"), rs.getString("nombre"), rs.getString("glosa")));
            }
            return items;
        }
    }

    private List<CatalogoItemDTO> listarCatalogo(String tabla) throws SQLException {
        String sql = "SELECT codigo, nombre FROM " + tabla + " WHERE activo = 1 ORDER BY nombre";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<CatalogoItemDTO> items = new ArrayList<>();
            while (rs.next()) {
                items.add(new CatalogoItemDTO(rs.getString("codigo"), rs.getString("nombre")));
            }
            return items;
        }
    }

    public Long obtenerEtapaId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "etapa_expediente", "id_etapa", codigo);
    }

    public Long obtenerEstadoId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "estado_expediente", "id_estado", codigo);
    }

    public Long obtenerTipoMovimientoId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_movimiento", "id_tipo_movimiento", codigo);
    }

    /**
     * Ids de equipo activos a los que pertenece idUsuario — usado por {@link VisibilidadBandejaSql}
     * (20/08/2026, primer consumidor real: Bandeja Análisis) como fallback de visibilidad cuando un
     * expediente todavía no tiene responsable individual asignado. Port del mismo criterio que
     * {@code UsuarioAsignacionService.listarIdsEquipoDeUsuario} en V2.
     */
    /** Overload de conexión propia — para que un Service (no un DAO ya dentro de una transacción) lo llame directo. */
    public List<Long> listarIdsEquipoDeUsuario(Long idUsuario) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return listarIdsEquipoDeUsuario(conn, idUsuario);
        }
    }

    public List<Long> listarIdsEquipoDeUsuario(Connection conn, Long idUsuario) throws SQLException {
        List<Long> ids = new ArrayList<>();
        if (idUsuario == null) {
            return ids;
        }
        String sql = "SELECT id_equipo FROM equipo_usuario WHERE id_usuario = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long value = rs.getLong("id_equipo");
                    if (!rs.wasNull()) {
                        ids.add(value);
                    }
                }
            }
        }
        return ids;
    }

    public Long obtenerCanalRecepcionId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "canal_recepcion", "id_canal_recepcion", codigo);
    }

    public Long obtenerTipoActaId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_acta", "id_tipo_acta", codigo);
    }

    /**
     * Carga diaria trae "Tipo de acta" como texto libre de la plantilla Excel (puede venir como
     * código o como nombre visible); a diferencia de {@link #obtenerTipoActaId}, que solo acepta
     * código exacto.
     */
    public Long obtenerTipoActaIdPorCodigoONombre(Connection conn, String value) throws SQLException {
        return obtenerIdPorCodigoONombre(conn, "tipo_acta", "id_tipo_acta", value);
    }

    private Long obtenerIdPorCodigo(Connection conn, String tabla, String columnaId, String codigo) throws SQLException {
        String sql = "SELECT " + columnaId + " FROM " + tabla + " WHERE UPPER(codigo) = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo == null ? "" : codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
    }

    private Long obtenerIdPorCodigoONombre(Connection conn, String tabla, String columnaId, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT " + columnaId + " FROM " + tabla
                + " WHERE activo = 1 AND (UPPER(codigo) = ? OR UPPER(nombre) = ?) AND ROWNUM = 1";
        String normalized = value.trim().toUpperCase();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalized);
            ps.setString(2, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long id = rs.getLong(1);
                return rs.wasNull() ? null : id;
            }
        }
    }
}
