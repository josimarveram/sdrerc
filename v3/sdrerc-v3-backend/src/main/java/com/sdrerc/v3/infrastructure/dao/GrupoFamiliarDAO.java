package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.application.GrupoFamiliarHeuristicaService;
import com.sdrerc.v3.domain.GrupoFamiliarCandidatoDTO;
import com.sdrerc.v3.domain.GrupoFamiliarIntegranteDTO;
import com.sdrerc.v3.domain.GrupoFamiliarResultadoDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port de com.sdrerc.infrastructure.sdrercapp.dao.GrupoFamiliarDAO (V2, Fase 2 de Grupo Familiar:
 * vínculo por PERSONA vía {@code PERSONA.id_grupo_familiar}, no por expediente — si la misma
 * persona vuelve a ser titular de otro expediente, hereda el grupo automáticamente). Acotado al
 * panel "Grupo Familiar" (compartido por Registro y Asignación, mismo patrón que "Asociar" — ver
 * Javadoc de {@code ExpedienteRelacionadoDAO}): {@link #listarPosiblesIntegrantes} (candidatos por
 * heurística de apellidos), {@link #buscarPosiblesIntegrantesManual} (búsqueda manual de respaldo),
 * {@link #listarIntegrantesGrupoFamiliar} (grupo ya confirmado), {@link #asociarGrupoFamiliar}
 * (confirmar) y {@link #eliminarDeGrupoFamiliar} (retirar un integrante, botón "x" en V2).
 *
 * <p>Quedan fuera de este port (atados a la baja lógica de un registro, funcionalidad todavía no
 * construida en V3): {@code limpiarAlertaPosibleGrupoFamiliarTrasEliminacion},
 * {@code retirarDeGrupoFamiliarSiCorresponde}, {@code limpiarAlertasGrupoFamiliarDeMiembrosRestantes},
 * {@code obtenerEstadoAlerta} y {@code eliminarAlertaPosibleGrupoFamiliar} (descartar la alerta sin
 * asociar) — ninguno de los 5 forma parte del flujo documentado del panel "Grupo Familiar" en sí.</p>
 */
@Repository
public class GrupoFamiliarDAO {

    private static final String CODIGO_MOVIMIENTO_ASOCIACION_GF = "ASOCIACION_GRUPO_FAMILIAR";
    private static final String TITULAR_SQL =
            "COALESCE(NULLIF(TRIM(p.razon_social), ''), "
                    + "NULLIF(TRIM(TRIM(NVL(p.nombres, '')) || ' ' || TRIM(NVL(p.apellidos, ''))), ''), "
                    + "p.numero_documento)";
    private static final String ALERTA_POSIBLE_GRUPO_FAMILIAR = "Posible Grupo Familiar";

    private final DataSource dataSource;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final GrupoFamiliarHeuristicaService heuristicaService;
    private final ExpedienteAlertaDAO expedienteAlertaDAO;

    public GrupoFamiliarDAO(
            DataSource dataSource,
            CatalogoLookupDAO catalogoLookupDAO,
            GrupoFamiliarHeuristicaService heuristicaService,
            ExpedienteAlertaDAO expedienteAlertaDAO) {
        this.dataSource = dataSource;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.heuristicaService = heuristicaService;
        this.expedienteAlertaDAO = expedienteAlertaDAO;
    }

    public List<GrupoFamiliarCandidatoDTO> listarPosiblesIntegrantes(Long idExpedientePrincipal) throws SQLException {
        List<GrupoFamiliarCandidatoDTO> candidatos = new ArrayList<>();
        if (idExpedientePrincipal == null) {
            return candidatos;
        }
        try (Connection conn = dataSource.getConnection()) {
            String titularAncla = obtenerTitularTexto(conn, idExpedientePrincipal);
            String claveAncla = heuristicaService.claveApellidosTitular(titularAncla);
            if (!hasText(claveAncla)) {
                return candidatos;
            }
            Long idPersonaAncla = obtenerIdPersonaTitular(conn, idExpedientePrincipal);
            Long idGrupoFamiliarAncla = idPersonaAncla == null ? null : obtenerGrupoFamiliarDePersona(conn, idPersonaAncla);

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
                    + "AND NOT EXISTS (SELECT 1 FROM expediente_relacion er WHERE er.activo = 1 "
                    + "  AND ((er.id_expediente_principal = ? AND er.id_expediente_relacionado = e.id_expediente) "
                    + "    OR (er.id_expediente_relacionado = ? AND er.id_expediente_principal = e.id_expediente))) "
                    + "AND ROWNUM <= 3000";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpedientePrincipal);
                ps.setLong(2, idExpedientePrincipal);
                ps.setLong(3, idExpedientePrincipal);
                try (ResultSet rs = ps.executeQuery()) {
                    Set<Long> vistos = new HashSet<>();
                    while (rs.next()) {
                        Long idPersona = getLongOrNull(rs, "id_persona");
                        if (idPersona == null || idPersona.equals(idPersonaAncla) || !vistos.add(idPersona)) {
                            continue;
                        }
                        Long idGrupoFamiliarCandidato = getLongOrNull(rs, "id_grupo_familiar");
                        if (idGrupoFamiliarCandidato != null && idGrupoFamiliarCandidato.equals(idGrupoFamiliarAncla)) {
                            continue;
                        }
                        String titular = rs.getString("titular");
                        if (!claveAncla.equals(heuristicaService.claveApellidosTitular(titular))) {
                            continue;
                        }
                        if (heuristicaService.coincideExactamente(titularAncla, titular)) {
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
                                idGrupoFamiliarCandidato));
                    }
                }
            }
        }
        return candidatos;
    }

    public List<GrupoFamiliarCandidatoDTO> buscarPosiblesIntegrantesManual(Long idExpedientePrincipal, String texto) throws SQLException {
        List<GrupoFamiliarCandidatoDTO> candidatos = new ArrayList<>();
        if (idExpedientePrincipal == null || !hasText(texto)) {
            return candidatos;
        }
        try (Connection conn = dataSource.getConnection()) {
            String patron = "%" + texto.trim().toUpperCase(Locale.ROOT) + "%";
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
                    + "AND NVL((SELECT MAX(esol.grupo_familiar) FROM expediente_solicitud esol "
                    + "  WHERE esol.id_expediente = e.id_expediente AND esol.activo = 1), 0) = 0 "
                    + "AND NOT EXISTS (SELECT 1 FROM expediente_alerta eal "
                    + "  WHERE eal.id_expediente = e.id_expediente AND eal.activo = 1 AND eal.atendida = 0 "
                    + "  AND UPPER(TRIM(eal.mensaje)) = '" + ALERTA_POSIBLE_GRUPO_FAMILIAR.toUpperCase(Locale.ROOT) + "') "
                    + "AND (UPPER(e.numero_expediente) LIKE ? "
                    + "  OR UPPER(" + TITULAR_SQL + ") LIKE ? "
                    + "  OR UPPER((SELECT MAX(esol2.numero_expediente_sgd) FROM expediente_solicitud esol2 "
                    + "            WHERE esol2.id_expediente = e.id_expediente AND esol2.activo = 1)) LIKE ?) "
                    + "AND ROWNUM <= 20 "
                    + "ORDER BY " + TITULAR_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpedientePrincipal);
                ps.setString(2, patron);
                ps.setString(3, patron);
                ps.setString(4, patron);
                try (ResultSet rs = ps.executeQuery()) {
                    Set<Long> vistos = new HashSet<>();
                    while (rs.next()) {
                        Long idPersona = getLongOrNull(rs, "id_persona");
                        if (idPersona == null || !vistos.add(idPersona)) {
                            continue;
                        }
                        candidatos.add(new GrupoFamiliarCandidatoDTO(
                                getLongOrNull(rs, "id_expediente"),
                                rs.getString("numero_expediente"),
                                idPersona,
                                rs.getString("titular"),
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
        List<GrupoFamiliarIntegranteDTO> integrantes = new ArrayList<>();
        if (idExpediente == null) {
            return integrantes;
        }
        try (Connection conn = dataSource.getConnection()) {
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
                    + "ur.nombre_completo abogado_asignado, ta.nombre tipo_acta "
                    + "FROM persona p "
                    + "LEFT JOIN expediente_persona ep ON ep.id_persona = p.id_persona AND ep.activo = 1 "
                    + "AND ep.tipo_relacion_persona = 'TITULAR' "
                    + "LEFT JOIN expediente e ON e.id_expediente = ep.id_expediente AND e.activo = 1 "
                    + "LEFT JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                    + "LEFT JOIN estado_expediente es2 ON es2.id_estado = e.id_estado_actual "
                    + "LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual "
                    + "LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1 "
                    + "LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta "
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
                                rs.getString("abogado_asignado"),
                                rs.getString("tipo_acta")));
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
        Set<Long> idsExpedientes = new LinkedHashSet<>();
        idsExpedientes.add(idExpedientePrincipal);
        idsExpedientes.addAll(idsExpedientesCandidatos);

        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Map<Long, Long> idPersonaPorExpediente = new LinkedHashMap<>();
                for (Long idExpediente : idsExpedientes) {
                    Long idPersona = obtenerIdPersonaTitular(conn, idExpediente);
                    if (idPersona == null) {
                        throw new IllegalStateException("El expediente " + idExpediente + " no tiene titular registrado.");
                    }
                    idPersonaPorExpediente.put(idExpediente, idPersona);
                }

                Map<Long, Long> grupoPorPersona = new LinkedHashMap<>();
                for (Long idPersona : new LinkedHashSet<>(idPersonaPorExpediente.values())) {
                    grupoPorPersona.put(idPersona, bloquearYObtenerGrupoPersona(conn, idPersona));
                }

                Set<Long> gruposExistentes = new LinkedHashSet<>();
                for (Long idGrupo : grupoPorPersona.values()) {
                    if (idGrupo != null) {
                        gruposExistentes.add(idGrupo);
                    }
                }
                if (gruposExistentes.size() > 1) {
                    throw new IllegalStateException("Las personas seleccionadas ya pertenecen a grupos familiares distintos. "
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
                    expedienteAlertaDAO.marcarAtendidas(conn, idExpediente, Collections.singletonList(ALERTA_POSIBLE_GRUPO_FAMILIAR), idUsuario);
                    if (idMovimiento != null) {
                        insertarHistorial(conn, idExpediente, idMovimiento, idPersona, idUsuario);
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
            } catch (IllegalArgumentException | IllegalStateException ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                throw ex;
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

    public GrupoFamiliarResultadoDTO eliminarDeGrupoFamiliar(Long idExpediente, Long idUsuario) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione el expediente a retirar del grupo familiar.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idPersona = obtenerIdPersonaTitular(conn, idExpediente);
                if (idPersona == null) {
                    throw new IllegalStateException("El expediente no tiene titular registrado.");
                }
                Long idGrupoFamiliar = bloquearYObtenerGrupoPersona(conn, idPersona);
                if (idGrupoFamiliar == null) {
                    throw new IllegalStateException("Este expediente ya no pertenece a ningún grupo familiar.");
                }
                actualizarGrupoPersona(conn, idPersona, null, idUsuario);
                desmarcarFlagExpedienteSolicitud(conn, idExpediente, idUsuario);
                Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_ASOCIACION_GF);
                if (idMovimiento != null) {
                    insertarHistorial(conn, idExpediente, idMovimiento, idPersona, idUsuario, "Persona retirada del grupo familiar.");
                }
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new GrupoFamiliarResultadoDTO(1, 0, 0, 1, "Se retiró la persona del grupo familiar.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                throw ex;
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
                    throw new IllegalStateException("La persona seleccionada ya no está disponible.");
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
            setLongOrNull(ps, 1, idGrupoFamiliar);
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

    private void desmarcarFlagExpedienteSolicitud(Connection conn, Long idExpediente, Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_solicitud SET grupo_familiar = 0, "
                + "criterio_grupo_familiar = NULL, "
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

    private void insertarHistorial(Connection conn, Long idExpediente, Long idMovimiento, Long idPersona, Long idUsuario) throws SQLException {
        insertarHistorial(conn, idExpediente, idMovimiento, idPersona, idUsuario, "Persona asociada al grupo familiar.");
    }

    private void insertarHistorial(
            Connection conn,
            Long idExpediente,
            Long idMovimiento,
            Long idPersona,
            Long idUsuario,
            String comentario) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_usuario_origen, "
                + "tabla_relacionada, id_registro_relacionado, comentario, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, 'PERSONA', ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idUsuario);
            setLongOrNull(ps, 4, idPersona);
            ps.setString(5, comentario);
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
