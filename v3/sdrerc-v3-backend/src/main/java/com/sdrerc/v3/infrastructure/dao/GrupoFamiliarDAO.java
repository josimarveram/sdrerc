package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.application.GrupoFamiliarHeuristicaService;
import com.sdrerc.v3.domain.GrupoFamiliarCandidatoDTO;
import com.sdrerc.v3.domain.GrupoFamiliarEstadoAlertaDTO;
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
 * <p>13/08/2026, pedido explícito del usuario: se portaron además {@link #limpiarAlertaPosibleGrupoFamiliarTrasEliminacion},
 * {@link #retirarDeGrupoFamiliarSiCorresponde} y {@link #limpiarAlertasGrupoFamiliarDeMiembrosRestantes}
 * (limpieza en cascada al eliminar/dar de baja un registro desde Registro/Recepción — ver
 * {@code ExpedienteEdicionManualDAO#eliminar}).</p>
 *
 * <p>13/08/2026, mismo día, pedido explícito del usuario ("revisa bien el panel de grupo familiar de
 * la v2 y replícalo... respetando el diseño de la v3"): se portaron también {@link #obtenerEstadoAlerta}
 * y el overload público de {@link #eliminarAlertaPosibleGrupoFamiliar(Long, Long)} — botón "descartar
 * alerta sin asociar" ("esto no es en realidad un posible integrante") del panel "Grupo Familiar" real
 * de V2, que faltaba en el port inicial.</p>
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

    /**
     * Retira a la persona titular de idExpediente de su grupo familiar CONFIRMADO — botón "x" del
     * panel "Grupo Familiar" ("Grupo familiar actual").
     *
     * <p>20/08/2026, pedido explícito del usuario (regla de tamaño mínimo, calculada dinámicamente en
     * esta misma transacción — nunca con un valor cacheado): un grupo no tiene sentido con 1 solo
     * integrante. Si el grupo tiene 2 integrantes activos ANTES de retirar a este (contando al
     * propio), se disuelve por completo: el otro integrante también pierde el vínculo, aunque su
     * propio expediente no se toque para nada más. Si tiene 3 o más, solo se retira a este; el resto
     * conserva su pertenencia al grupo sin cambios. Ver {@link #retirarPersonaDeGrupoConReglaDeTamano}
     * (mismo helper que usa {@link #retirarDeGrupoFamiliarSiCorresponde} en la cascada de baja lógica
     * de un expediente — misma regla, un solo lugar donde está implementada).</p>
     */
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
                if (bloquearYObtenerGrupoPersona(conn, idPersona) == null) {
                    throw new IllegalStateException("Este expediente ya no pertenece a ningún grupo familiar.");
                }
                int retirados = retirarPersonaDeGrupoConReglaDeTamano(
                        conn, idPersona, idExpediente, idUsuario,
                        "Persona retirada del grupo familiar.",
                        "Persona retirada del grupo familiar (el grupo quedaba con menos de 2 integrantes, se disolvió por completo).");
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                String mensaje = retirados > 1
                        ? "El grupo familiar quedaba con menos de 2 integrantes: se disolvió por completo (" + retirados + " persona(s) retirada(s))."
                        : "Se retiró la persona del grupo familiar.";
                return new GrupoFamiliarResultadoDTO(1, 0, 0, retirados, mensaje);
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

    /**
     * Estado de la alerta "Posible Grupo Familiar" del expediente enfocado — si ya pertenece a un
     * grupo familiar CONFIRMADO (flag {@code EXPEDIENTE_SOLICITUD.grupo_familiar}) y el mensaje de
     * alerta activa (no atendida), si existe. Usado por el panel "Grupo Familiar" para mostrar el
     * estado y habilitar el botón de descartar alerta sin asociar. Port literal de V2.
     */
    public GrupoFamiliarEstadoAlertaDTO obtenerEstadoAlerta(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return new GrupoFamiliarEstadoAlertaDTO(false, null, 0);
        }
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT "
                    + "NVL((SELECT MAX(esol.grupo_familiar) FROM expediente_solicitud esol "
                    + "  WHERE esol.id_expediente = ? AND esol.activo = 1), 0) AS grupo_familiar, "
                    + "(SELECT MAX(eal.mensaje) FROM expediente_alerta eal "
                    + "  WHERE eal.id_expediente = ? AND eal.activo = 1 AND eal.atendida = 0 "
                    + "  AND UPPER(TRIM(eal.mensaje)) = '" + ALERTA_POSIBLE_GRUPO_FAMILIAR.toUpperCase(Locale.ROOT) + "') AS mensaje_alerta "
                    + "FROM dual";
            boolean confirmado;
            String mensajeAlerta;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpediente);
                ps.setLong(2, idExpediente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return new GrupoFamiliarEstadoAlertaDTO(false, null, 0);
                    }
                    confirmado = rs.getInt("grupo_familiar") == 1;
                    mensajeAlerta = rs.getString("mensaje_alerta");
                }
            }
            // 20/08/2026, pedido explícito del usuario: tamaño del vínculo calculado dinámicamente
            // (no cacheado) en cada consulta — mismo criterio de conteo que usan los métodos de
            // eliminación reales (`listarPersonasActivasDelGrupo`/`listarExpedientesActivosPorApellidos`),
            // para que el frontend arme el texto de confirmación con el alcance real de la regla de
            // tamaño mínimo antes de que el usuario elimine/descarte nada.
            int tamanoGrupo = 0;
            if (confirmado) {
                Long idPersona = obtenerIdPersonaTitular(conn, idExpediente);
                Long idGrupoFamiliar = idPersona == null ? null : obtenerGrupoFamiliarDePersona(conn, idPersona);
                if (idGrupoFamiliar != null) {
                    tamanoGrupo = listarPersonasActivasDelGrupo(conn, idGrupoFamiliar).size();
                }
            } else if (hasText(mensajeAlerta)) {
                tamanoGrupo = 1 + listarExpedientesActivosPorApellidos(conn, idExpediente).size();
            }
            return new GrupoFamiliarEstadoAlertaDTO(confirmado, mensajeAlerta, tamanoGrupo);
        }
    }

    /**
     * Descarta la alerta "Posible Grupo Familiar" de un expediente SIN asociarlo a ningún grupo —
     * "esto no es en realidad un posible integrante" (botón papelera junto a la alerta en V2, visible
     * solo cuando hay alerta activa y el grupo todavía no está confirmado). Con conexión y
     * transacción propia, fuera de cualquier transacción de baja lógica.
     *
     * <p>20/08/2026, pedido explícito del usuario (regla de tamaño mínimo): ya NO se limita a
     * limpiar la alerta del propio expediente — delega en {@link #limpiarAlertaPosibleGrupoFamiliarTrasEliminacion},
     * que aplica la misma regla que el resto de escenarios de eliminación de este vínculo: si, además
     * de este expediente, solo queda 1 solicitud relacionada por apellidos (grupo de tamaño 2), se
     * descarta la alerta de AMBAS (ya no tiene sentido que quede una sola marcada); si quedan 2 o más
     * (grupo de tamaño 3+), se descarta únicamente la de este expediente y las demás conservan la
     * suya sin cambios. El tamaño se calcula dinámicamente en esta misma llamada, nunca con un valor
     * cacheado de una consulta previa (ver {@code GrupoFamiliarEstadoAlertaDTO#getTamanoGrupo}, que
     * el frontend consulta antes de confirmar, con el mismo criterio).</p>
     */
    public void eliminarAlertaPosibleGrupoFamiliar(Long idExpediente, Long idUsuario) throws SQLException {
        if (idExpediente == null) {
            return;
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                limpiarAlertaPosibleGrupoFamiliarTrasEliminacion(conn, idExpediente, idUsuario);
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
     * Ids de expedientes activos (excluyendo el propio) cuyo titular comparte la misma clave de
     * apellidos ({@link GrupoFamiliarHeuristicaService#claveApellidosTitular}) que idExpediente,
     * excluyendo coincidencias de titular EXACTO (esas son "Potencial duplicado", no "Posible Grupo
     * Familiar" — mismo criterio de exclusión que {@link #listarPosiblesIntegrantes}). Usado al
     * eliminar (baja lógica) un registro para decidir si la alerta de los demás relacionados también
     * debe limpiarse (solo quedaba 1 solicitud activa relacionada) o si debe mantenerse (eran 2 o
     * más). Port literal de V2.
     */
    private List<Long> listarExpedientesActivosPorApellidos(Connection conn, Long idExpediente) throws SQLException {
        List<Long> ids = new ArrayList<>();
        if (conn == null || idExpediente == null) {
            return ids;
        }
        String titular = obtenerTitularTexto(conn, idExpediente);
        String clave = heuristicaService.claveApellidosTitular(titular);
        if (!hasText(clave)) {
            return ids;
        }
        String sql = "SELECT e.id_expediente, " + TITULAR_SQL + " AS titular "
                + "FROM expediente e "
                + "JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 "
                + "  AND ep.tipo_relacion_persona = 'TITULAR' "
                + "JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                + "WHERE e.activo = 1 AND e.id_expediente <> ? "
                + "AND ROWNUM <= 3000";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String titularCandidato = rs.getString("titular");
                    if (clave.equals(heuristicaService.claveApellidosTitular(titularCandidato))
                            && !heuristicaService.coincideExactamente(titular, titularCandidato)) {
                        ids.add(getLongOrNull(rs, "id_expediente"));
                    }
                }
            }
        }
        return ids;
    }

    /** Overload privado con {@code Connection} recibida, para reutilizar dentro de la misma transacción de eliminación. */
    private void eliminarAlertaPosibleGrupoFamiliar(Connection conn, Long idExpediente, Long idUsuario) throws SQLException {
        expedienteAlertaDAO.marcarAtendidas(
                conn, idExpediente, Collections.singletonList(ALERTA_POSIBLE_GRUPO_FAMILIAR), idUsuario);
        limpiarCriterioGrupoFamiliar(conn, idExpediente, idUsuario);
    }

    /**
     * Al descartar la alerta "Posible Grupo Familiar" también se limpian los campos heurísticos de
     * detección (criterio_grupo_familiar/observacion_grupo_familiar) en la solicitud activa más
     * reciente. Sin esto, la Bandeja Registro seguía contando el expediente como "Posible Grupo
     * Familiar" en su KPI (revisa esos campos además de la alerta) aunque la alerta ya estuviera
     * atendida. Port literal de V2.
     */
    private void limpiarCriterioGrupoFamiliar(Connection conn, Long idExpediente, Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_solicitud SET criterio_grupo_familiar = NULL, "
                + "observacion_grupo_familiar = NULL, "
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

    /**
     * Limpia la alerta "Posible Grupo Familiar" (fila {@code EXPEDIENTE_ALERTA} + campos heurísticos
     * de {@code EXPEDIENTE_SOLICITUD}) del propio idExpediente y, si tras eliminarlo solo queda 1
     * solicitud activa relacionada por apellidos, también la de esa única sobreviviente (ya no hay
     * con quién formar un "posible" grupo). Si quedan 2 o más relacionadas, esas se dejan intactas.
     * No debe confundirse con {@link #limpiarAlertasGrupoFamiliarDeMiembrosRestantes}, que aplica al
     * grupo YA CONFIRMADO ({@code PERSONA.id_grupo_familiar}), sin condición de cantidad. Debe
     * llamarse dentro de una transacción ya abierta por el llamador (nunca abre/cierra la suya
     * propia). Port literal de V2 (07/08/2026 allá, 13/08/2026 acá).
     *
     * <p>20/08/2026: dejó de ser exclusivo de la cascada de baja lógica — el nombre se conserva
     * (evita renombrar sin necesidad) pero también lo invoca directamente
     * {@link #eliminarAlertaPosibleGrupoFamiliar(Long, Long)} (botón "descartar alerta" del panel,
     * sin que el expediente se elimine), con su propia transacción envolvente. Misma regla de tamaño
     * mínimo en ambos casos.</p>
     */
    public void limpiarAlertaPosibleGrupoFamiliarTrasEliminacion(
            Connection conn, Long idExpediente, Long idUsuario) throws SQLException {
        if (conn == null || idExpediente == null) {
            return;
        }
        List<Long> relacionados = listarExpedientesActivosPorApellidos(conn, idExpediente);
        eliminarAlertaPosibleGrupoFamiliar(conn, idExpediente, idUsuario);
        if (relacionados.size() == 1) {
            eliminarAlertaPosibleGrupoFamiliar(conn, relacionados.get(0), idUsuario);
        }
    }

    /**
     * Si el titular de idExpediente pertenece a un grupo familiar YA CONFIRMADO
     * ({@code PERSONA.id_grupo_familiar}), lo retira del grupo al eliminar (baja lógica) su
     * expediente: a diferencia de {@link #limpiarAlertasGrupoFamiliarDeMiembrosRestantes} (que solo
     * limpia alertas remanentes de los DEMÁS integrantes), esto afecta al PROPIO integrante
     * eliminado — si su expediente ya no existe, no debe seguir contando como miembro visible del
     * grupo familiar. No lanza excepción si no pertenecía a ningún grupo (caso normal de la mayoría
     * de bajas). Debe llamarse dentro de la misma transacción de la baja lógica, y después de
     * {@link #limpiarAlertasGrupoFamiliarDeMiembrosRestantes} (que necesita leer el grupo todavía
     * asignado a esta persona para encontrar a los demás integrantes).
     *
     * <p>20/08/2026, pedido explícito del usuario (regla de tamaño mínimo, misma que
     * {@link #eliminarDeGrupoFamiliar}, calculada dinámicamente dentro de esta misma transacción): si
     * el grupo tenía 2 integrantes activos antes de esta baja, el otro integrante también pierde el
     * vínculo (el grupo se disuelve por completo, aunque su propio expediente no se toca para nada
     * más); si tenía 3 o más, solo se retira al de idExpediente y el resto conserva su vínculo sin
     * cambios. Ver {@link #retirarPersonaDeGrupoConReglaDeTamano}.</p>
     */
    public void retirarDeGrupoFamiliarSiCorresponde(Connection conn, Long idExpediente, Long idUsuario) throws SQLException {
        if (conn == null || idExpediente == null) {
            return;
        }
        Long idPersona = obtenerIdPersonaTitular(conn, idExpediente);
        if (idPersona == null) {
            return;
        }
        if (bloquearYObtenerGrupoPersona(conn, idPersona) == null) {
            return;
        }
        retirarPersonaDeGrupoConReglaDeTamano(
                conn, idPersona, idExpediente, idUsuario,
                "Persona retirada del grupo familiar por eliminación (baja lógica) del expediente.",
                "Persona retirada del grupo familiar por eliminación (baja lógica) del expediente "
                        + "(el grupo quedaba con menos de 2 integrantes, se disolvió por completo).");
    }

    /**
     * Núcleo compartido de la regla de tamaño mínimo del grupo familiar CONFIRMADO, usado tanto por
     * {@link #eliminarDeGrupoFamiliar} (botón "x" directo del panel) como por
     * {@link #retirarDeGrupoFamiliarSiCorresponde} (cascada al eliminar/dar de baja un expediente) —
     * un solo lugar con la regla, para que ambos escenarios se comporten idénticamente (pedido
     * explícito del usuario: "aplica esta misma lógica en los 3 escenarios de eliminación").
     *
     * <p>Debe llamarse DESPUÉS de que el llamador ya bloqueó (FOR UPDATE) la fila de {@code idPersona}
     * en {@code PERSONA} (vía {@link #bloquearYObtenerGrupoPersona}), para que el conteo de
     * integrantes se calcule con datos frescos dentro de la misma transacción — nunca con un valor
     * cacheado de una consulta anterior. Cuenta los integrantes activos del grupo (incluyendo a
     * idPersona): con 2 o menos, retira a TODOS (el grupo se disuelve, no tiene sentido un grupo de 1
     * sola persona); con 3 o más, retira únicamente a idPersona. Cada persona retirada pierde su
     * {@code PERSONA.id_grupo_familiar} y su flag {@code EXPEDIENTE_SOLICITUD.grupo_familiar}, con su
     * propia fila de historial — ninguna de las demás ve tocado nada más de su expediente.</p>
     *
     * @return cantidad de personas efectivamente retiradas (1 si solo se retiró a idPersona, 2+ si el
     *     grupo se disolvió por completo).
     */
    private int retirarPersonaDeGrupoConReglaDeTamano(
            Connection conn,
            Long idPersona,
            Long idExpedienteDeIdPersona,
            Long idUsuario,
            String motivoRetiroSolo,
            String motivoDisolucion) throws SQLException {
        Long idGrupoFamiliar = obtenerGrupoFamiliarDePersona(conn, idPersona);
        if (idGrupoFamiliar == null) {
            return 0;
        }
        List<Long> personasGrupo = listarPersonasActivasDelGrupo(conn, idGrupoFamiliar);
        List<Long> aRetirar;
        String motivo;
        if (personasGrupo.size() <= 2) {
            aRetirar = new ArrayList<>(personasGrupo);
            if (!aRetirar.contains(idPersona)) {
                aRetirar.add(idPersona);
            }
            motivo = motivoDisolucion;
        } else {
            aRetirar = Collections.singletonList(idPersona);
            motivo = motivoRetiroSolo;
        }

        Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_ASOCIACION_GF);
        for (Long idPersonaRetirar : aRetirar) {
            actualizarGrupoPersona(conn, idPersonaRetirar, null, idUsuario);
            Long idExpedienteRetirar = idPersonaRetirar.equals(idPersona)
                    ? idExpedienteDeIdPersona
                    : obtenerExpedienteActivoDePersona(conn, idPersonaRetirar);
            if (idExpedienteRetirar != null) {
                desmarcarFlagExpedienteSolicitud(conn, idExpedienteRetirar, idUsuario);
                if (idMovimiento != null) {
                    insertarHistorial(conn, idExpedienteRetirar, idMovimiento, idPersonaRetirar, idUsuario, motivo);
                }
            }
        }
        return aRetirar.size();
    }

    /**
     * Ids de persona (distintos) que integran hoy el grupo familiar dado, con expediente activo como
     * titular — mismo criterio de "integrante visible" que {@link #listarIntegrantesGrupoFamiliar}, y
     * mismo criterio de "restantes" que {@link #limpiarAlertasGrupoFamiliarDeMiembrosRestantes}, pero
     * expuesto como helper reutilizable para calcular el TAMAÑO del grupo antes de decidir si se
     * disuelve (regla de tamaño mínimo, 20/08/2026). Calculado siempre en el momento de la llamada,
     * nunca cacheado.
     */
    private List<Long> listarPersonasActivasDelGrupo(Connection conn, Long idGrupoFamiliar) throws SQLException {
        List<Long> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT p.id_persona "
                + "FROM persona p "
                + "JOIN expediente_persona ep ON ep.id_persona = p.id_persona AND ep.activo = 1 "
                + "  AND ep.tipo_relacion_persona = 'TITULAR' "
                + "JOIN expediente e ON e.id_expediente = ep.id_expediente AND e.activo = 1 "
                + "WHERE p.id_grupo_familiar = ? AND p.activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idGrupoFamiliar);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(getLongOrNull(rs, "id_persona"));
                }
            }
        }
        return ids;
    }

    /** Expediente activo donde idPersona figura como titular (el primero que encuentre) — reverso de {@link #obtenerIdPersonaTitular}. */
    private Long obtenerExpedienteActivoDePersona(Connection conn, Long idPersona) throws SQLException {
        String sql = "SELECT ep.id_expediente FROM expediente_persona ep "
                + "JOIN expediente e ON e.id_expediente = ep.id_expediente AND e.activo = 1 "
                + "WHERE ep.id_persona = ? AND ep.activo = 1 AND ep.tipo_relacion_persona = 'TITULAR' AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPersona);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? getLongOrNull(rs, "id_expediente") : null;
            }
        }
    }

    /**
     * Cuando se elimina (baja lógica) el expediente de un integrante de un grupo familiar, los demás
     * integrantes que sigan con expediente activo en ese mismo grupo ({@code PERSONA.id_grupo_familiar})
     * pueden quedar mostrando "Posible Grupo Familiar" en el KPI/columna Alertas de Registro y
     * Asignación aunque ya estaban confirmados, porque esa columna solo lee si queda una alerta
     * {@code EXPEDIENTE_ALERTA} activa (no si el grupo tiene 2+ miembros activos). Limpia esa alerta
     * remanente en los integrantes que NO se eliminaron, sin tocar {@code PERSONA.id_grupo_familiar}
     * (el vínculo de grupo se mantiene por si aparece otro familiar futuro). Debe llamarse en la
     * MISMA transacción que la baja lógica del expediente eliminado. Port literal de V2.
     */
    public void limpiarAlertasGrupoFamiliarDeMiembrosRestantes(
            Connection conn, Long idExpedienteEliminado, Long idUsuario) throws SQLException {
        if (conn == null || idExpedienteEliminado == null) {
            return;
        }
        Long idPersona = obtenerIdPersonaTitular(conn, idExpedienteEliminado);
        if (idPersona == null) {
            return;
        }
        Long idGrupoFamiliar = obtenerGrupoFamiliarDePersona(conn, idPersona);
        if (idGrupoFamiliar == null) {
            return;
        }
        String sql = "SELECT DISTINCT e.id_expediente "
                + "FROM persona p "
                + "JOIN expediente_persona ep ON ep.id_persona = p.id_persona AND ep.activo = 1 "
                + "  AND ep.tipo_relacion_persona = 'TITULAR' "
                + "JOIN expediente e ON e.id_expediente = ep.id_expediente AND e.activo = 1 "
                + "WHERE p.id_grupo_familiar = ? AND p.activo = 1 AND e.id_expediente <> ?";
        List<Long> idsRestantes = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idGrupoFamiliar);
            ps.setLong(2, idExpedienteEliminado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idsRestantes.add(getLongOrNull(rs, "id_expediente"));
                }
            }
        }
        for (Long idExpedienteRestante : idsRestantes) {
            eliminarAlertaPosibleGrupoFamiliar(conn, idExpedienteRestante, idUsuario);
        }
    }

    /**
     * Consulta de solo lectura (sin abrir transacción propia, no modifica nada) usada por el modal de
     * confirmación de eliminación desde Registro/Recepción para advertir al usuario del impacto antes
     * de confirmar: si el titular ya pertenece a un grupo familiar confirmado, y cuántos integrantes
     * (además del propio) tiene ese grupo hoy con expediente activo. No forma parte del port de V2
     * (ahí no existía un aviso previo de impacto); es un requisito nuevo explícito de V3.
     */
    public boolean perteneceGrupoFamiliarConfirmado(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return false;
        }
        try (Connection conn = dataSource.getConnection()) {
            Long idPersona = obtenerIdPersonaTitular(conn, idExpediente);
            if (idPersona == null) {
                return false;
            }
            return obtenerGrupoFamiliarDePersona(conn, idPersona) != null;
        }
    }

    /**
     * Cantidad de integrantes activos que tiene HOY el grupo familiar confirmado de idExpediente (0
     * si no pertenece a ninguno) — mismo conteo que decide la regla de tamaño mínimo al eliminar
     * ({@link #retirarPersonaDeGrupoConReglaDeTamano}), expuesto de solo lectura para el modal de
     * confirmación de eliminación de la bandeja (20/08/2026, pedido explícito del usuario). Calculado
     * siempre al momento de la llamada, nunca cacheado.
     */
    public int contarIntegrantesGrupoFamiliarConfirmado(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return 0;
        }
        try (Connection conn = dataSource.getConnection()) {
            Long idPersona = obtenerIdPersonaTitular(conn, idExpediente);
            if (idPersona == null) {
                return 0;
            }
            Long idGrupoFamiliar = obtenerGrupoFamiliarDePersona(conn, idPersona);
            if (idGrupoFamiliar == null) {
                return 0;
            }
            return listarPersonasActivasDelGrupo(conn, idGrupoFamiliar).size();
        }
    }

    /**
     * Cantidad de solicitudes que integran hoy la alerta "Posible Grupo Familiar" de idExpediente (1
     * + relacionadas por apellidos, 0 si idExpediente no tiene la alerta activa) — mismo conteo que
     * decide la regla de tamaño mínimo al descartar la alerta
     * ({@link #limpiarAlertaPosibleGrupoFamiliarTrasEliminacion}), expuesto de solo lectura para el
     * modal de confirmación de eliminación de la bandeja (20/08/2026, pedido explícito del usuario).
     * Calculado siempre al momento de la llamada, nunca cacheado.
     */
    public int contarPosibleGrupoFamiliar(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return 0;
        }
        try (Connection conn = dataSource.getConnection()) {
            if (!expedienteAlertaDAO.tieneAlertaActiva(conn, idExpediente, ALERTA_POSIBLE_GRUPO_FAMILIAR)) {
                return 0;
            }
            return 1 + listarExpedientesActivosPorApellidos(conn, idExpediente).size();
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

    /**
     * 20/08/2026, bug real corregido (pedido explícito del usuario, caso reportado con la titular
     * ATEHORTUA HUANCACHOQUE IVANIA ISABELLA): al retirar/disolver un vínculo de grupo familiar
     * confirmado esta actualización dejaba {@code observacion_grupo_familiar} SIN limpiar (solo
     * limpiaba {@code criterio_grupo_familiar}) — a diferencia de {@link #limpiarCriterioGrupoFamiliar}
     * (usada por el camino de la ALERTA), que sí limpia ambas columnas. Esa columna sobrante es
     * justo la que {@code ExpedienteBandejaDAO.grupoFamiliar(...)} (Bandeja Registro) usa como
     * segunda señal para derivar "Posible Grupo Familiar" cuando el flag de confirmado ya es 0 —
     * por eso Registro seguía mostrando la alerta tras disolver el grupo, mientras Asignación (que
     * deriva su alerta de una fila activa en {@code EXPEDIENTE_ALERTA}, no de este texto) ya
     * mostraba "Sin Alerta" correctamente. Con las 2 columnas limpiadas, ambas bandejas quedan
     * consistentes.
     */
    private void desmarcarFlagExpedienteSolicitud(Connection conn, Long idExpediente, Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_solicitud SET grupo_familiar = 0, "
                + "criterio_grupo_familiar = NULL, "
                + "observacion_grupo_familiar = NULL, "
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
