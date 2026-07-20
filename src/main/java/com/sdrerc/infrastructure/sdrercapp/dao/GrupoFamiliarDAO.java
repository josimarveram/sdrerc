package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.GrupoFamiliarHeuristicaService;
import com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarCandidatoDTO;
import com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarIntegranteDTO;
import com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarResultadoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fase 2 de Grupo Familiar: a diferencia de la Fase 1 (marca booleana simple en
 * EXPEDIENTE_SOLICITUD, ver 27_grupo_familiar_fase1.sql), este DAO vincula PERSONAS entre si
 * mediante un ID de grupo familiar (GRUPO_FAMILIAR.id_grupo_familiar, referenciado desde
 * PERSONA.id_grupo_familiar). El vinculo es a nivel persona, no por expediente: si la misma
 * persona vuelve a ser titular de otro expediente, hereda el grupo automaticamente.
 *
 * No reutiliza EXPEDIENTE_RELACION (esa tabla hereda numero de expediente/equipo/abogado para
 * duplicados; grupo familiar no debe heredar nada de eso, son conceptos distintos).
 */
public class GrupoFamiliarDAO {

    private static final String CODIGO_MOVIMIENTO_ASOCIACION_GF = "ASOCIACION_GRUPO_FAMILIAR";
    private static final String TITULAR_SQL =
            "COALESCE(NULLIF(TRIM(p.razon_social), ''), "
                    + "NULLIF(TRIM(TRIM(NVL(p.nombres, '')) || ' ' || TRIM(NVL(p.apellidos, ''))), ''), "
                    + "p.numero_documento)";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final GrupoFamiliarHeuristicaService heuristicaService;

    public GrupoFamiliarDAO() {
        this(new CatalogoLookupDAO(), new GrupoFamiliarHeuristicaService());
    }

    public GrupoFamiliarDAO(CatalogoLookupDAO catalogoLookupDAO, GrupoFamiliarHeuristicaService heuristicaService) {
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.heuristicaService = heuristicaService;
    }

    public List<GrupoFamiliarCandidatoDTO> listarPosiblesIntegrantes(Long idExpedientePrincipal) throws SQLException {
        List<GrupoFamiliarCandidatoDTO> candidatos = new ArrayList<GrupoFamiliarCandidatoDTO>();
        if (idExpedientePrincipal == null) {
            return candidatos;
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            String titularAncla = obtenerTitularTexto(conn, idExpedientePrincipal);
            String claveAncla = heuristicaService.claveApellidosTitular(titularAncla);
            if (!hasText(claveAncla)) {
                return candidatos;
            }
            Long idPersonaAncla = obtenerIdPersonaTitular(conn, idExpedientePrincipal);

            String sql = "SELECT e.id_expediente, e.numero_expediente, et.codigo etapa_codigo, es2.codigo estado_codigo, "
                    + "p.id_persona, p.id_grupo_familiar, " + TITULAR_SQL + " AS titular, "
                    + "ur.nombre_completo AS abogado_asignado "
                    + "FROM expediente e "
                    + "JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 "
                    + "AND ep.tipo_relacion_persona = 'TITULAR' "
                    + "JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                    + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                    + "JOIN estado_expediente es2 ON es2.id_estado = e.id_estado_actual "
                    + "LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual "
                    + "WHERE e.activo = 1 AND e.id_expediente <> ? "
                    + "AND ROWNUM <= 3000";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpedientePrincipal);
                try (ResultSet rs = ps.executeQuery()) {
                    Set<Long> vistos = new HashSet<Long>();
                    while (rs.next()) {
                        Long idPersona = getLongOrNull(rs, "id_persona");
                        if (idPersona == null || idPersona.equals(idPersonaAncla) || !vistos.add(idPersona)) {
                            continue;
                        }
                        String titular = rs.getString("titular");
                        if (!claveAncla.equals(heuristicaService.claveApellidosTitular(titular))) {
                            continue;
                        }
                        candidatos.add(new GrupoFamiliarCandidatoDTO(
                                getLongOrNull(rs, "id_expediente"),
                                rs.getString("numero_expediente"),
                                idPersona,
                                titular,
                                rs.getString("etapa_codigo"),
                                rs.getString("estado_codigo"),
                                rs.getString("abogado_asignado"),
                                getLongOrNull(rs, "id_grupo_familiar")));
                    }
                }
            }
        }
        return candidatos;
    }

    public List<GrupoFamiliarIntegranteDTO> listarIntegrantesGrupoFamiliar(Long idExpediente) throws SQLException {
        List<GrupoFamiliarIntegranteDTO> integrantes = new ArrayList<GrupoFamiliarIntegranteDTO>();
        if (idExpediente == null) {
            return integrantes;
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            Long idPersona = obtenerIdPersonaTitular(conn, idExpediente);
            if (idPersona == null) {
                return integrantes;
            }
            Long idGrupoFamiliar = obtenerGrupoFamiliarDePersona(conn, idPersona);
            if (idGrupoFamiliar == null) {
                return integrantes;
            }
            String sql = "SELECT p.id_persona, " + TITULAR_SQL + " AS nombre, "
                    + "e.id_expediente, e.numero_expediente, et.codigo etapa_codigo, es2.codigo estado_codigo, "
                    + "ur.nombre_completo abogado_asignado "
                    + "FROM persona p "
                    + "LEFT JOIN expediente_persona ep ON ep.id_persona = p.id_persona AND ep.activo = 1 "
                    + "AND ep.tipo_relacion_persona = 'TITULAR' "
                    + "LEFT JOIN expediente e ON e.id_expediente = ep.id_expediente AND e.activo = 1 "
                    + "LEFT JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                    + "LEFT JOIN estado_expediente es2 ON es2.id_estado = e.id_estado_actual "
                    + "LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual "
                    + "WHERE p.id_grupo_familiar = ? AND p.activo = 1 "
                    + "ORDER BY nombre";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idGrupoFamiliar);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        integrantes.add(new GrupoFamiliarIntegranteDTO(
                                getLongOrNull(rs, "id_persona"),
                                rs.getString("nombre"),
                                getLongOrNull(rs, "id_expediente"),
                                rs.getString("numero_expediente"),
                                rs.getString("etapa_codigo"),
                                rs.getString("estado_codigo"),
                                rs.getString("abogado_asignado")));
                    }
                }
            }
        }
        return integrantes;
    }

    public GrupoFamiliarResultadoDTO asociarGrupoFamiliar(
            Long idExpedientePrincipal,
            List<Long> idsExpedientesCandidatos,
            Long idUsuario) throws SQLException {
        if (idExpedientePrincipal == null) {
            throw new IllegalArgumentException("Seleccione el expediente principal para asociar.");
        }
        if (idsExpedientesCandidatos == null || idsExpedientesCandidatos.isEmpty()) {
            throw new IllegalArgumentException("Seleccione al menos un integrante para asociar al grupo familiar.");
        }
        Set<Long> idsExpedientes = new LinkedHashSet<Long>();
        idsExpedientes.add(idExpedientePrincipal);
        idsExpedientes.addAll(idsExpedientesCandidatos);

        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Map<Long, Long> idPersonaPorExpediente = new LinkedHashMap<Long, Long>();
                for (Long idExpediente : idsExpedientes) {
                    Long idPersona = obtenerIdPersonaTitular(conn, idExpediente);
                    if (idPersona == null) {
                        throw new SQLException("El expediente " + idExpediente + " no tiene titular registrado.");
                    }
                    idPersonaPorExpediente.put(idExpediente, idPersona);
                }

                Map<Long, Long> grupoPorPersona = new LinkedHashMap<Long, Long>();
                for (Long idPersona : new LinkedHashSet<Long>(idPersonaPorExpediente.values())) {
                    grupoPorPersona.put(idPersona, bloquearYObtenerGrupoPersona(conn, idPersona));
                }

                Set<Long> gruposExistentes = new LinkedHashSet<Long>();
                for (Long idGrupo : grupoPorPersona.values()) {
                    if (idGrupo != null) {
                        gruposExistentes.add(idGrupo);
                    }
                }
                if (gruposExistentes.size() > 1) {
                    throw new SQLException("Las personas seleccionadas ya pertenecen a grupos familiares distintos. "
                            + "Revise la selección: solo puede asociarse un grupo familiar por vez.");
                }

                Long idGrupoFamiliar = gruposExistentes.isEmpty()
                        ? crearGrupoFamiliar(conn, idUsuario)
                        : gruposExistentes.iterator().next();

                Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_ASOCIACION_GF);
                int asociados = 0;
                int yaAsociados = 0;
                for (Map.Entry<Long, Long> entry : idPersonaPorExpediente.entrySet()) {
                    Long idExpediente = entry.getKey();
                    Long idPersona = entry.getValue();
                    Long grupoActual = grupoPorPersona.get(idPersona);
                    if (grupoActual != null && grupoActual.equals(idGrupoFamiliar)) {
                        yaAsociados++;
                        continue;
                    }
                    actualizarGrupoPersona(conn, idPersona, idGrupoFamiliar, idUsuario);
                    marcarFlagExpedienteSolicitud(conn, idExpediente, idUsuario);
                    if (idMovimiento != null) {
                        insertarHistorial(conn, idExpediente, idMovimiento, idPersona, idGrupoFamiliar, idUsuario);
                    }
                    asociados++;
                }

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                String mensaje = asociados + " persona(s) asociada(s) al grupo familiar.";
                if (yaAsociados > 0) {
                    mensaje += " " + yaAsociados + " ya pertenecían a ese grupo.";
                }
                return new GrupoFamiliarResultadoDTO(idsExpedientes.size(), asociados, yaAsociados, 0, mensaje);
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

    private String obtenerTitularTexto(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT " + TITULAR_SQL + " AS titular "
                + "FROM expediente_persona ep JOIN persona p ON p.id_persona = ep.id_persona "
                + "WHERE ep.id_expediente = ? AND ep.activo = 1 AND ep.tipo_relacion_persona = 'TITULAR' "
                + "AND p.activo = 1 AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("titular") : null;
            }
        }
    }

    private Long obtenerIdPersonaTitular(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT p.id_persona FROM expediente_persona ep JOIN persona p ON p.id_persona = ep.id_persona "
                + "WHERE ep.id_expediente = ? AND ep.activo = 1 AND ep.tipo_relacion_persona = 'TITULAR' "
                + "AND p.activo = 1 AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? getLongOrNull(rs, "id_persona") : null;
            }
        }
    }

    private Long obtenerGrupoFamiliarDePersona(Connection conn, Long idPersona) throws SQLException {
        String sql = "SELECT id_grupo_familiar FROM persona WHERE id_persona = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPersona);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? getLongOrNull(rs, "id_grupo_familiar") : null;
            }
        }
    }

    private Long bloquearYObtenerGrupoPersona(Connection conn, Long idPersona) throws SQLException {
        String sql = "SELECT id_grupo_familiar FROM persona WHERE id_persona = ? AND activo = 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPersona);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("La persona seleccionada ya no está disponible.");
                }
                return getLongOrNull(rs, "id_grupo_familiar");
            }
        }
    }

    private Long crearGrupoFamiliar(Connection conn, Long idUsuario) throws SQLException {
        String sql = "INSERT INTO grupo_familiar (activo, creado_por, creado_en) VALUES (1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_GRUPO_FAMILIAR"})) {
            setLongOrNull(ps, 1, idUsuario);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "grupo_familiar");
        }
    }

    private void actualizarGrupoPersona(Connection conn, Long idPersona, Long idGrupoFamiliar, Long idUsuario) throws SQLException {
        String sql = "UPDATE persona SET id_grupo_familiar = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_persona = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idGrupoFamiliar);
            setLongOrNull(ps, 2, idUsuario);
            ps.setLong(3, idPersona);
            ps.executeUpdate();
        }
    }

    private void marcarFlagExpedienteSolicitud(Connection conn, Long idExpediente, Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_solicitud SET grupo_familiar = 1, "
                + "criterio_grupo_familiar = 'CONFIRMADO_ASIGNACION', "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_solicitud = ("
                + "  SELECT MAX(id_expediente_solicitud) FROM expediente_solicitud "
                + "  WHERE id_expediente = ? AND activo = 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setLongOrNull(ps, 1, idUsuario);
            ps.setLong(2, idExpediente);
            ps.executeUpdate();
        }
    }

    private void insertarHistorial(
            Connection conn,
            Long idExpediente,
            Long idMovimiento,
            Long idPersona,
            Long idGrupoFamiliar,
            Long idUsuario) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_usuario_origen, "
                + "tabla_relacionada, id_registro_relacionado, comentario, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, 'PERSONA', ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idUsuario);
            setLongOrNull(ps, 4, idPersona);
            ps.setString(5, "Persona asociada al grupo familiar.");
            ps.setString(6, CODIGO_MOVIMIENTO_ASOCIACION_GF);
            setLongOrNull(ps, 7, idUsuario);
            ps.executeUpdate();
        }
    }

    private Long obtenerGeneratedKey(PreparedStatement ps, String entidad) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
        throw new SQLException("No se pudo obtener el identificador generado de " + entidad + ".");
    }

    private void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
