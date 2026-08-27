package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.application.CalendarioLaboralService;
import com.sdrerc.v3.domain.DatosActaDTO;
import com.sdrerc.v3.domain.DatosPersonaRegistroDTO;
import com.sdrerc.v3.domain.DatosSolicitudDTO;
import com.sdrerc.v3.domain.ExpedienteEdicionManualDTO;
import com.sdrerc.v3.domain.ImpactoEliminacionRegistroDTO;
import com.sdrerc.v3.domain.RegistroManualResultadoDTO;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port de com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteEdicionManualDAO (V2), acotado al
 * camino de edición/baja desde Registro/Recepción ({@code obtenerParaEdicion}/{@code guardar}/
 * {@code eliminar}, siempre con {@code validarEstadoRegistro=true}/{@code desdeAnalisis=false}) MÁS
 * el camino de edición desde Análisis ({@code obtenerParaEdicionDesdeAnalisis}/
 * {@code guardarDesdeAnalisis}, agregado 21/08/2026, pedido explícito del usuario — ver Javadoc de
 * {@link #validarEditable(ExpedienteEstado, boolean, boolean)}). El punto de entrada equivalente
 * desde Asignación (con sus propias reglas distintas en V2) sigue sin portar: ese módulo no expone
 * hoy un botón "Editar" en V3. Mismo SQL que V2 en cada método portado.
 *
 * <p>13/08/2026, pedido explícito del usuario: {@link #eliminar} ahora SÍ replica la limpieza en
 * cascada de alertas de solicitudes relacionadas que hace V2 (antes deliberadamente diferida, ver
 * historial de este Javadoc) — delega en {@link ExpedienteRegistroDAO#limpiarAlertaPotencialDuplicadoTrasEliminacion}
 * y en los 3 métodos de cascada portados en {@link GrupoFamiliarDAO}, en el mismo orden que V2.</p>
 */
@Repository
public class ExpedienteEdicionManualDAO {

    private static final String CODIGO_ETAPA_REGISTRO = "REGISTRO";
    private static final String CODIGO_ESTADO_REGISTRADO = "REGISTRADO";
    private static final String CODIGO_MOVIMIENTO_EDICION = "RECEPCION_DOCUMENTO";
    private static final String CODIGO_MOVIMIENTO_ELIMINACION = "BAJA_REGISTRO";
    private static final String ALERTA_POTENCIAL_DUPLICADO = "Potencial duplicado";
    private static final String ALERTA_POSIBLE_GRUPO_FAMILIAR = "Posible Grupo Familiar";

    private final DataSource dataSource;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final CalendarioLaboralService calendarioLaboralService;
    private final ExpedienteAlertaDAO expedienteAlertaDAO;
    private final ExpedienteRegistroDAO expedienteRegistroDAO;
    private final GrupoFamiliarDAO grupoFamiliarDAO;

    public ExpedienteEdicionManualDAO(
            DataSource dataSource,
            CatalogoLookupDAO catalogoLookupDAO,
            CalendarioLaboralService calendarioLaboralService,
            ExpedienteAlertaDAO expedienteAlertaDAO,
            ExpedienteRegistroDAO expedienteRegistroDAO,
            GrupoFamiliarDAO grupoFamiliarDAO) {
        this.dataSource = dataSource;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.calendarioLaboralService = calendarioLaboralService;
        this.expedienteAlertaDAO = expedienteAlertaDAO;
        this.expedienteRegistroDAO = expedienteRegistroDAO;
        this.grupoFamiliarDAO = grupoFamiliarDAO;
    }

    /**
     * Info de impacto para el modal de confirmación de eliminación (13/08/2026, requisito nuevo de
     * V3 sin equivalente en V2): si el propio expediente tiene hoy alertas "Potencial duplicado"/
     * "Posible Grupo Familiar" activas, y si su titular pertenece a un grupo familiar ya confirmado.
     * Solo lectura, no abre transacción de escritura.
     *
     * <p>20/08/2026, pedido explícito del usuario: además, cuando corresponde, el tamaño ACTUAL
     * (dinámico, no cacheado) del vínculo — cuántas solicitudes tiene hoy la alerta "Posible Grupo
     * Familiar"/el grupo familiar confirmado — para que el modal pueda anticipar si eliminar este
     * expediente disolverá el vínculo por completo (2 o menos) o solo lo retirará a él (3 o más),
     * misma regla que aplica {@code GrupoFamiliarDAO} al momento real de la baja.</p>
     */
    public ImpactoEliminacionRegistroDTO obtenerImpactoEliminacion(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean potencialDuplicado = expedienteAlertaDAO.tieneAlertaActiva(conn, idExpediente, ALERTA_POTENCIAL_DUPLICADO);
            boolean posibleGrupoFamiliar = expedienteAlertaDAO.tieneAlertaActiva(conn, idExpediente, ALERTA_POSIBLE_GRUPO_FAMILIAR);
            boolean grupoFamiliarConfirmado = grupoFamiliarDAO.perteneceGrupoFamiliarConfirmado(idExpediente);
            int tamanoPosibleGrupoFamiliar = posibleGrupoFamiliar ? grupoFamiliarDAO.contarPosibleGrupoFamiliar(idExpediente) : 0;
            int tamanoGrupoFamiliarConfirmado =
                    grupoFamiliarConfirmado ? grupoFamiliarDAO.contarIntegrantesGrupoFamiliarConfirmado(idExpediente) : 0;
            return new ImpactoEliminacionRegistroDTO(
                    potencialDuplicado, posibleGrupoFamiliar, grupoFamiliarConfirmado,
                    tamanoPosibleGrupoFamiliar, tamanoGrupoFamiliarConfirmado);
        }
    }

    public ExpedienteEdicionManualDTO obtenerParaEdicion(Long idExpediente) throws SQLException {
        return obtenerParaEdicionInterno(idExpediente, false);
    }

    /**
     * Igual que {@link #obtenerParaEdicion}, pero sin exigir etapa REGISTRO/estado REGISTRADO/sin
     * asignación — port de {@code obtenerParaEdicionDesdeAnalisis} (V2), usado cuando el lápiz de
     * edición del panel "Datos" se abre desde el módulo Análisis (21/08/2026, pedido explícito del
     * usuario). Ver Javadoc de {@link #validarEditable(ExpedienteEstado, boolean, boolean)}.
     */
    public ExpedienteEdicionManualDTO obtenerParaEdicionDesdeAnalisis(Long idExpediente) throws SQLException {
        return obtenerParaEdicionInterno(idExpediente, true);
    }

    private ExpedienteEdicionManualDTO obtenerParaEdicionInterno(Long idExpediente, boolean desdeAnalisis) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para editar.");
        }
        try (Connection conn = dataSource.getConnection()) {
            ExpedienteEdicionManualDTO dto = consultarDto(conn, idExpediente);
            validarEditable(new ExpedienteEstado(
                    null, null, dto.getEtapaCodigo(), dto.getEstadoCodigo(), dto.getNumeroExpediente(), null),
                    tieneAsignacionActiva(conn, idExpediente), desdeAnalisis);
            return dto;
        }
    }

    /**
     * Igual que {@link #obtenerParaEdicion}, pero sin exigir que el expediente sea editable — usado
     * por el panel "Datos" (solo lectura, informativo, sin botones), que debe funcionar para
     * cualquier expediente sin importar etapa/estado/asignación (ver `CLAUDE.md` raíz, "Panel
     * derecho Registro").
     */
    public ExpedienteEdicionManualDTO obtenerDatos(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
        try (Connection conn = dataSource.getConnection()) {
            return consultarDto(conn, idExpediente);
        }
    }

    private ExpedienteEdicionManualDTO consultarDto(Connection conn, Long idExpediente) throws SQLException {
            boolean soportaGrupoFamiliar = soportaGrupoFamiliar(conn);
            boolean soportaUbigeoPersona = soportaUbigeoPersona(conn);
            boolean soportaTipoSolicitante = soportaTipoSolicitante(conn);
            boolean soportaMunicipalidad = soportaMunicipalidad(conn);
            String solicitanteTipoSelect = soportaTipoSolicitante
                    ? "sol.tipo_solicitante, sol.id_centro_atencion, ca.denominacion AS centro_atencion_denominacion, "
                            + "sol.id_unidad_organica, uo.nombre AS unidad_organica_nombre, "
                    : "CAST(NULL AS VARCHAR2(20)) AS tipo_solicitante, CAST(NULL AS NUMBER) AS id_centro_atencion, "
                            + "CAST(NULL AS VARCHAR2(200)) AS centro_atencion_denominacion, "
                            + "CAST(NULL AS NUMBER) AS id_unidad_organica, CAST(NULL AS VARCHAR2(150)) AS unidad_organica_nombre, ";
            String municipalidadSelect = soportaMunicipalidad
                    ? "sol.id_municipalidad, muni.denominacion AS municipalidad_denominacion, "
                    : "CAST(NULL AS NUMBER) AS id_municipalidad, CAST(NULL AS VARCHAR2(150)) AS municipalidad_denominacion, ";
            String grupoFamiliarSelect = soportaGrupoFamiliar
                    ? "NVL(sol.grupo_familiar, 0) AS grupo_familiar, sol.criterio_grupo_familiar, sol.observacion_grupo_familiar, "
                    : "0 AS grupo_familiar, CAST(NULL AS VARCHAR2(80)) AS criterio_grupo_familiar, "
                            + "CAST(NULL AS VARCHAR2(500)) AS observacion_grupo_familiar, ";
            // 19/08/2026, pedido explícito del usuario: PERSONA solo guarda `id_ubigeo_distrito` (un
            // distrito determina de forma unívoca su provincia/departamento en la jerarquía UBIGEO,
            // ver v3/CLAUDE.md), nunca `id_ubigeo_provincia`/`id_ubigeo_departamento` por separado —
            // por eso los combos "Provincia"/"Departamento" del panel de edición nunca mostraban el
            // valor preseleccionado: el DTO recibía `idDistrito` pero `idDepartamento`/`idProvincia`
            // siempre llegaban en `null`, y el frontend arma esos combos en cascada (Provincia
            // depende de `idDepartamento`, Distrito depende de `idProvincia`) — sin esos 2 ids, ni
            // siquiera el propio Distrito llegaba a mostrarse seleccionado, porque su lista de
            // opciones (`listarDistritos`) nunca se cargaba. Fix: se resuelven ambos ids "hacia
            // arriba" desde `id_ubigeo_distrito` vía los JOIN a `ubigeo_distrito`/`ubigeo_provincia`
            // (ver más abajo, condicionados al mismo `soportaUbigeoPersona`).
            String ubigeoSelect = soportaUbigeoPersona
                    ? "NVL(remitente.id_ubigeo_distrito, solicitante.id_ubigeo_distrito) AS remitente_id_distrito, "
                            + "ubp.id_ubigeo_provincia AS remitente_id_provincia, "
                            + "ubp.id_ubigeo_departamento AS remitente_id_departamento, "
                            + "NVL(remitente.departamento, solicitante.departamento) AS remitente_departamento, "
                            + "NVL(remitente.provincia, solicitante.provincia) AS remitente_provincia, "
                            + "NVL(remitente.distrito, solicitante.distrito) AS remitente_distrito, "
                    : "CAST(NULL AS NUMBER) AS remitente_id_distrito, "
                            + "CAST(NULL AS NUMBER) AS remitente_id_provincia, "
                            + "CAST(NULL AS NUMBER) AS remitente_id_departamento, "
                            + "CAST(NULL AS VARCHAR2(120)) AS remitente_departamento, "
                            + "CAST(NULL AS VARCHAR2(120)) AS remitente_provincia, "
                            + "CAST(NULL AS VARCHAR2(120)) AS remitente_distrito, ";
            String sql = ""
                    + "WITH sol AS ("
                    + "  SELECT * FROM (SELECT s.* FROM expediente_solicitud s "
                    + "  WHERE s.id_expediente = ? AND s.activo = 1 ORDER BY s.creado_en DESC, s.id_expediente_solicitud DESC) WHERE ROWNUM = 1"
                    + "), doc AS ("
                    + "  SELECT * FROM (SELECT d.* FROM expediente_documento d "
                    + "  WHERE d.id_expediente = ? AND d.activo = 1 ORDER BY d.creado_en DESC, d.id_expediente_documento DESC) WHERE ROWNUM = 1"
                    + "), act AS ("
                    + "  SELECT * FROM (SELECT a.* FROM expediente_acta a "
                    + "  WHERE a.id_expediente = ? AND a.activo = 1 ORDER BY a.creado_en DESC, a.id_expediente_acta DESC) WHERE ROWNUM = 1"
                    + "), titular AS ("
                    + "  SELECT * FROM (SELECT ep.id_expediente_persona, p.* "
                    + "  FROM expediente_persona ep JOIN persona p ON p.id_persona = ep.id_persona "
                    + "  WHERE ep.id_expediente = ? AND ep.tipo_relacion_persona = 'TITULAR' AND ep.activo = 1 AND p.activo = 1 "
                    + "  ORDER BY ep.creado_en DESC, ep.id_expediente_persona DESC) WHERE ROWNUM = 1"
                    + "), remitente AS ("
                    + "  SELECT * FROM (SELECT ep.id_expediente_persona, p.* "
                    + "  FROM expediente_persona ep JOIN persona p ON p.id_persona = ep.id_persona "
                    + "  WHERE ep.id_expediente = ? AND ep.tipo_relacion_persona = 'REMITENTE' AND ep.activo = 1 AND p.activo = 1 "
                    + "  ORDER BY ep.creado_en DESC, ep.id_expediente_persona DESC) WHERE ROWNUM = 1"
                    + ") "
                    + "SELECT e.id_expediente, e.numero_expediente, e.numero_tramite_documentario, e.prioridad, "
                    + "et.codigo AS etapa_codigo, est.codigo AS estado_codigo, "
                    + "sol.numero_tramite_documentario AS solicitud_tramite, sol.numero_expediente_sgd, sol.fecha_recepcion, "
                    + "sol.asunto, sol.observacion, " + grupoFamiliarSelect + solicitanteTipoSelect + municipalidadSelect
                    + "cr.codigo AS canal_codigo, cr.nombre AS canal_nombre, "
                    + "doc.nombre_documento, doc.numero_documento, "
                    + "act.numero_acta, act.anio_acta, ta.codigo AS tipo_acta_codigo, ta.nombre AS tipo_acta_nombre, "
                    + "titular.tipo_documento AS titular_tipo_documento, titular.numero_documento AS titular_numero_documento, "
                    + nombrePersona("titular") + " AS titular_nombre, "
                    + "NVL(remitente.tipo_documento, solicitante.tipo_documento) AS remitente_tipo_documento, "
                    + "NVL(remitente.numero_documento, solicitante.numero_documento) AS remitente_numero_documento, "
                    + "NVL(remitente.correo_electronico, solicitante.correo_electronico) AS remitente_correo, "
                    + "NVL(remitente.telefono, solicitante.telefono) AS remitente_telefono, "
                    + "NVL(remitente.direccion, solicitante.direccion) AS remitente_direccion, "
                    + ubigeoSelect
                    + "COALESCE(" + nombrePersona("remitente") + ", " + nombrePersona("solicitante") + ") AS remitente_nombre "
                    + "FROM expediente e "
                    + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                    + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                    + "LEFT JOIN sol ON sol.id_expediente = e.id_expediente "
                    + "LEFT JOIN canal_recepcion cr ON cr.id_canal_recepcion = sol.id_canal_recepcion "
                    + "LEFT JOIN persona solicitante ON solicitante.id_persona = sol.id_persona_solicitante "
                    + (soportaTipoSolicitante
                            ? "LEFT JOIN centros_atencion ca ON ca.id_centro_atencion = sol.id_centro_atencion "
                                    + "LEFT JOIN unidades_organicas uo ON uo.id_unidad_organica = sol.id_unidad_organica "
                            : "")
                    + (soportaMunicipalidad
                            ? "LEFT JOIN municipalidades muni ON muni.id_municipalidad = sol.id_municipalidad "
                            : "")
                    + "LEFT JOIN doc ON doc.id_expediente = e.id_expediente "
                    + "LEFT JOIN act ON act.id_expediente = e.id_expediente "
                    + "LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = act.id_tipo_acta "
                    + "LEFT JOIN titular ON titular.id_persona IS NOT NULL "
                    + "LEFT JOIN remitente ON remitente.id_persona IS NOT NULL "
                    + (soportaUbigeoPersona
                            ? "LEFT JOIN ubigeo_distrito ubd ON ubd.id_ubigeo_distrito = "
                                    + "NVL(remitente.id_ubigeo_distrito, solicitante.id_ubigeo_distrito) "
                                    + "LEFT JOIN ubigeo_provincia ubp ON ubp.id_ubigeo_provincia = ubd.id_ubigeo_provincia "
                            : "")
                    + "WHERE e.id_expediente = ? AND e.activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 1; i <= 5; i++) {
                    ps.setLong(i, idExpediente);
                }
                ps.setLong(6, idExpediente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("No se encontró el expediente seleccionado.");
                    }
                    return map(rs, conn);
                }
            }
    }

    public RegistroManualResultadoDTO guardar(ExpedienteEdicionManualDTO dto, Long idUsuarioActual) throws SQLException {
        return guardarInterno(dto, idUsuarioActual, false);
    }

    /**
     * Igual que {@link #guardar}, pero sin exigir etapa REGISTRO/estado REGISTRADO/sin asignación —
     * port de {@code guardarDesdeAnalisis} (V2). Habilita el lápiz de edición del panel "Datos"
     * dentro del módulo Análisis (21/08/2026, pedido explícito del usuario): un expediente ya
     * recibido/en análisis nunca cumple esa condición de todas formas, así que sin este bypass el
     * lápiz quedaba siempre deshabilitado ahí.
     */
    public RegistroManualResultadoDTO guardarDesdeAnalisis(ExpedienteEdicionManualDTO dto, Long idUsuarioActual) throws SQLException {
        return guardarInterno(dto, idUsuarioActual, true);
    }

    private RegistroManualResultadoDTO guardarInterno(ExpedienteEdicionManualDTO dto, Long idUsuarioActual, boolean desdeAnalisis)
            throws SQLException {
        if (dto == null || dto.getIdExpediente() == null) {
            throw new IllegalArgumentException("Seleccione un expediente para editar.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstado estado = bloquearYObtenerEstado(conn, dto.getIdExpediente());
                validarEditable(estado, tieneAsignacionActiva(conn, dto.getIdExpediente()), desdeAnalisis);

                boolean solicitanteEsCiudadano = "CIUDADANO".equals(dto.getSolicitud().getTipoSolicitanteEfectivo());
                Long idTitular = upsertPersonaRelacion(conn, dto.getIdExpediente(), "TITULAR", dto.getTitular());
                Long idRemitente;
                if (solicitanteEsCiudadano) {
                    idRemitente = upsertPersonaRelacion(conn, dto.getIdExpediente(), "REMITENTE", dto.getRemitente());
                } else {
                    // El expediente pudo tener antes un Ciudadano como solicitante (o viceversa): al
                    // cambiar de tipo, se desactiva la relación REMITENTE anterior en EXPEDIENTE_PERSONA
                    // para que no quede una persona "fantasma" inconsistente con tipo_solicitante.
                    desactivarPersonaRelacion(conn, dto.getIdExpediente(), "REMITENTE");
                    idRemitente = null;
                }
                Long idSolicitante = idRemitente == null ? (solicitanteEsCiudadano ? idTitular : null) : idRemitente;

                actualizarExpediente(conn, dto);
                upsertSolicitud(conn, dto, idSolicitante);
                upsertActa(conn, dto);
                upsertDocumento(conn, dto);
                insertarHistorial(conn, dto, estado, desdeAnalisis);
                registrarAlertasEdicion(conn, dto, idUsuarioActual);

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new RegistroManualResultadoDTO(
                        dto.getIdExpediente(),
                        textoNumeroExpediente(dto.getNumeroExpediente()),
                        "Datos de registro actualizados correctamente. No se modificó el número de expediente.");
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
     * Port de {@code ExpedienteEdicionManualDAO.eliminar} (V2): baja lógica (`activo=0`) de un
     * registro desde Registro/Recepción, con la misma validación de editabilidad que {@link #guardar}
     * (mismo lock {@code FOR UPDATE}, misma transacción) e historial ({@code tipo_movimiento
     * BAJA_REGISTRO}, motivo {@code BAJA_REGISTRO_RECEPCION}).
     *
     * <p>13/08/2026, pedido explícito del usuario: se agregó la limpieza en cascada que hace V2
     * después de la baja, en la MISMA transacción y mismo orden que allá — {@code
     * expedienteRegistroDAO.limpiarAlertaPotencialDuplicadoTrasEliminacion} (alerta "Potencial
     * duplicado" de la solicitud relacionada por acta, si solo queda 1), {@code
     * grupoFamiliarDAO.limpiarAlertaPosibleGrupoFamiliarTrasEliminacion} (ídem "Posible Grupo
     * Familiar" por apellidos), {@code grupoFamiliarDAO.limpiarAlertasGrupoFamiliarDeMiembrosRestantes}
     * (limpia la alerta remanente de los DEMÁS integrantes de un grupo familiar YA CONFIRMADO — debe
     * ir ANTES de retirar al propio integrante, porque necesita leer el grupo todavía asignado a su
     * persona) y {@code grupoFamiliarDAO.retirarDeGrupoFamiliarSiCorresponde} (retira al PROPIO
     * titular eliminado de su grupo familiar confirmado, al final).
     */
    public RegistroManualResultadoDTO eliminar(Long idExpediente, Long idUsuarioActual) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para eliminar.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstado estado = bloquearYObtenerEstado(conn, idExpediente);
                validarEditable(estado, tieneAsignacionActiva(conn, idExpediente));

                Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_ELIMINACION);
                if (idMovimiento == null) {
                    throw new SQLException("No se encontró el movimiento " + CODIGO_MOVIMIENTO_ELIMINACION
                            + ". Ejecute el script de catálogo correspondiente.");
                }
                String sqlHistorial = "INSERT INTO expediente_historial ("
                        + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_etapa_origen, id_estado_origen, "
                        + "id_etapa_destino, id_estado_destino, tabla_relacionada, id_registro_relacionado, "
                        + "comentario, motivo, activo, creado_por"
                        + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, 'EXPEDIENTE', ?, ?, 'BAJA_REGISTRO_RECEPCION', 1, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlHistorial)) {
                    ps.setLong(1, idExpediente);
                    ps.setLong(2, idMovimiento);
                    ps.setLong(3, estado.idEtapa);
                    ps.setLong(4, estado.idEstado);
                    ps.setLong(5, estado.idEtapa);
                    ps.setLong(6, estado.idEstado);
                    ps.setLong(7, idExpediente);
                    ps.setString(8, limitar("Registro eliminado (baja lógica) desde Recepción. Expediente: "
                            + textoNumeroExpediente(estado.numeroExpediente) + ".", 2000));
                    setLongOrNull(ps, 9, idUsuarioActual);
                    ps.executeUpdate();
                }

                String sqlBaja = "UPDATE expediente SET activo = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_expediente = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlBaja)) {
                    setLongOrNull(ps, 1, idUsuarioActual);
                    ps.setLong(2, idExpediente);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("No se pudo eliminar el expediente seleccionado.");
                    }
                }

                expedienteRegistroDAO.limpiarAlertaPotencialDuplicadoTrasEliminacion(conn, idExpediente, idUsuarioActual);
                grupoFamiliarDAO.limpiarAlertaPosibleGrupoFamiliarTrasEliminacion(conn, idExpediente, idUsuarioActual);
                // Grupo familiar YA CONFIRMADO: limpiar alertas remanentes de los demás integrantes
                // primero (necesita leer el grupo todavía asignado a esta persona) y recién después
                // retirar al propio integrante eliminado del grupo.
                grupoFamiliarDAO.limpiarAlertasGrupoFamiliarDeMiembrosRestantes(conn, idExpediente, idUsuarioActual);
                grupoFamiliarDAO.retirarDeGrupoFamiliarSiCorresponde(conn, idExpediente, idUsuarioActual);

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new RegistroManualResultadoDTO(
                        idExpediente,
                        textoNumeroExpediente(estado.numeroExpediente),
                        "El registro fue eliminado correctamente.");
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

    private ExpedienteEdicionManualDTO map(ResultSet rs, Connection conn) throws SQLException {
        ExpedienteEdicionManualDTO dto = new ExpedienteEdicionManualDTO();
        dto.setIdExpediente(getLongOrNull(rs, "id_expediente"));
        dto.setNumeroExpediente(rs.getString("numero_expediente"));
        dto.setEtapaCodigo(rs.getString("etapa_codigo"));
        dto.setEstadoCodigo(rs.getString("estado_codigo"));
        dto.setNumeroExpedienteVistaPrevia(textoNumeroExpediente(dto.getNumeroExpediente()));

        String observacion = rs.getString("observacion");
        DatosSolicitudDTO solicitud = new DatosSolicitudDTO();
        solicitud.setNumeroTramite(firstText(rs.getString("solicitud_tramite"), rs.getString("numero_tramite_documentario")));
        solicitud.setNumeroDocumento(rs.getString("numero_documento"));
        solicitud.setNumeroExpedienteSgd(rs.getString("numero_expediente_sgd"));
        Date fechaRecepcion = rs.getDate("fecha_recepcion");
        solicitud.setFechaRecepcion(fechaRecepcion == null ? null : fechaRecepcion.toLocalDate());
        solicitud.setTipoSolicitudNombre(extraerObservacion(observacion, "Tipo de solicitud"));
        solicitud.setTipoSolicitudCodigo(codigoTipoSolicitud(solicitud.getTipoSolicitudNombre()));
        solicitud.setTipoProcedimientoNombre(rs.getString("asunto"));
        solicitud.setTipoProcedimientoCodigo(codigoCatalogoPorNombre(conn, "procedimiento_registral", rs.getString("asunto")));
        solicitud.setTipoDocumentoNombre(firstText(rs.getString("nombre_documento"), extraerObservacion(observacion, "Tipo de documento")));
        solicitud.setTipoDocumentoCodigo(codigoCatalogoPorNombre(conn, "tipo_documento", solicitud.getTipoDocumentoNombre()));
        solicitud.setCanalCodigo(rs.getString("canal_codigo"));
        solicitud.setCanalNombre(rs.getString("canal_nombre"));
        solicitud.setPrioridad(rs.getString("prioridad"));
        solicitud.setValidacionInicial(firstText(extraerObservacion(observacion, "Validación inicial"), "Sí corresponde a la SDRERC"));
        solicitud.setHojaEnvio(extraerObservacion(observacion, "Hoja de envío"));
        solicitud.setGrupoFamiliar(rs.getInt("grupo_familiar") == 1);
        solicitud.setCriterioGrupoFamiliar(firstText(rs.getString("criterio_grupo_familiar"), extraerObservacion(observacion, "Criterio grupo familiar")));
        solicitud.setObservacionGrupoFamiliar(firstText(rs.getString("observacion_grupo_familiar"), extraerObservacion(observacion, "Observación grupo familiar")));
        solicitud.setTipoSolicitante(rs.getString("tipo_solicitante"));
        solicitud.setIdCentroAtencion(getLongOrNull(rs, "id_centro_atencion"));
        solicitud.setCentroAtencionDenominacion(rs.getString("centro_atencion_denominacion"));
        solicitud.setIdUnidadOrganica(getLongOrNull(rs, "id_unidad_organica"));
        solicitud.setUnidadOrganicaNombre(rs.getString("unidad_organica_nombre"));
        solicitud.setIdMunicipalidad(getLongOrNull(rs, "id_municipalidad"));
        solicitud.setMunicipalidadDenominacion(rs.getString("municipalidad_denominacion"));
        dto.setSolicitud(solicitud);

        DatosActaDTO acta = new DatosActaDTO();
        acta.setTipoActaCodigo(rs.getString("tipo_acta_codigo"));
        acta.setTipoActaNombre(rs.getString("tipo_acta_nombre"));
        acta.setNumeroActa(rs.getString("numero_acta"));
        int anioActa = rs.getInt("anio_acta");
        acta.setAnioActa(rs.wasNull() ? null : anioActa);
        dto.setActa(acta);

        DatosPersonaRegistroDTO titular = new DatosPersonaRegistroDTO();
        titular.setNombreCompleto(rs.getString("titular_nombre"));
        titular.setTipoDocumento(rs.getString("titular_tipo_documento"));
        titular.setNumeroDocumento(rs.getString("titular_numero_documento"));
        dto.setTitular(titular);

        DatosPersonaRegistroDTO remitente = new DatosPersonaRegistroDTO();
        remitente.setNombreCompleto(rs.getString("remitente_nombre"));
        remitente.setTipoDocumento(rs.getString("remitente_tipo_documento"));
        remitente.setNumeroDocumento(rs.getString("remitente_numero_documento"));
        remitente.setCorreo(rs.getString("remitente_correo"));
        remitente.setTelefono(rs.getString("remitente_telefono"));
        remitente.setDireccion(rs.getString("remitente_direccion"));
        remitente.setIdDistrito(getLongOrNull(rs, "remitente_id_distrito"));
        remitente.setIdProvincia(getLongOrNull(rs, "remitente_id_provincia"));
        remitente.setIdDepartamento(getLongOrNull(rs, "remitente_id_departamento"));
        remitente.setDepartamento(rs.getString("remitente_departamento"));
        remitente.setProvincia(rs.getString("remitente_provincia"));
        remitente.setDistrito(rs.getString("remitente_distrito"));
        dto.setRemitente(remitente);
        return dto;
    }

    private void actualizarExpediente(Connection conn, ExpedienteEdicionManualDTO dto) throws SQLException {
        DatosSolicitudDTO solicitud = dto.getSolicitud();
        String sql = "UPDATE expediente SET "
                + "numero_tramite_documentario = ?, prioridad = ?, fecha_vencimiento = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, solicitud.getNumeroTramite());
            ps.setString(2, hasText(solicitud.getPrioridad()) ? solicitud.getPrioridad() : "NORMAL");
            ps.setDate(3, calcularFechaVencimiento(conn, solicitud.getFechaRecepcion(), solicitud.getTipoProcedimientoNombre()));
            ps.setLong(4, dto.getIdExpediente());
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar el expediente seleccionado.");
            }
        }
    }

    private void upsertSolicitud(Connection conn, ExpedienteEdicionManualDTO dto, Long idSolicitante) throws SQLException {
        Long idSolicitud = obtenerUltimoId(conn, "expediente_solicitud", "id_expediente_solicitud", dto.getIdExpediente());
        DatosSolicitudDTO solicitud = dto.getSolicitud();
        boolean soportaGrupoFamiliar = soportaGrupoFamiliar(conn);
        boolean soportaTipoSolicitante = soportaTipoSolicitante(conn);
        boolean soportaMunicipalidad = soportaMunicipalidad(conn);
        Long idCanal = null;
        if (hasText(solicitud.getCanalCodigo())) {
            idCanal = catalogoLookupDAO.obtenerCanalRecepcionId(conn, solicitud.getCanalCodigo());
        }
        if (idSolicitud == null) {
            String columnasGrupo = soportaGrupoFamiliar
                    ? ", grupo_familiar, criterio_grupo_familiar, observacion_grupo_familiar"
                    : "";
            String valoresGrupo = soportaGrupoFamiliar ? ", ?, ?, ?" : "";
            String columnasSolicitante = soportaTipoSolicitante
                    ? ", tipo_solicitante, id_centro_atencion, id_unidad_organica"
                    : "";
            String valoresSolicitante = soportaTipoSolicitante ? ", ?, ?, ?" : "";
            String columnaMunicipalidad = soportaMunicipalidad ? ", id_municipalidad" : "";
            String valorMunicipalidad = soportaMunicipalidad ? ", ?" : "";
            String insert = "INSERT INTO expediente_solicitud ("
                    + "id_expediente, id_canal_recepcion, id_persona_solicitante, numero_tramite_documentario, "
                    + "numero_expediente_sgd, fecha_recepcion, asunto, observacion, es_tramite_virtual, "
                    + "correo_electronico, potencial_duplicado" + columnasGrupo + columnasSolicitante + columnaMunicipalidad + ", activo"
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, NULL, ?" + valoresGrupo + valoresSolicitante + valorMunicipalidad + ", 1)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                int idx = 1;
                ps.setLong(idx++, dto.getIdExpediente());
                setLongOrNull(ps, idx++, idCanal);
                setLongOrNull(ps, idx++, idSolicitante);
                ps.setString(idx++, solicitud.getNumeroTramite());
                ps.setString(idx++, solicitud.getNumeroExpedienteSgd());
                ps.setDate(idx++, toSqlDate(solicitud.getFechaRecepcion()));
                ps.setString(idx++, solicitud.getTipoProcedimientoNombre());
                ps.setString(idx++, limitar(observacionSolicitud(dto), 1000));
                ps.setInt(idx++, dto.isPosibleDuplicado() ? 1 : 0);
                if (soportaGrupoFamiliar) {
                    ps.setInt(idx++, solicitud.isGrupoFamiliar() ? 1 : 0);
                    ps.setString(idx++, limitar(resolverCriterioGrupoFamiliar(solicitud), 80));
                    ps.setString(idx++, limitar(solicitud.getObservacionGrupoFamiliar(), 500));
                }
                if (soportaTipoSolicitante) {
                    ps.setString(idx++, solicitud.getTipoSolicitanteEfectivo());
                    setLongOrNull(ps, idx++, solicitud.getIdCentroAtencion());
                    setLongOrNull(ps, idx++, solicitud.getIdUnidadOrganica());
                }
                if (soportaMunicipalidad) {
                    setLongOrNull(ps, idx++, solicitud.getIdMunicipalidad());
                }
                ps.executeUpdate();
            }
            return;
        }
        String columnasGrupoUpdate = soportaGrupoFamiliar
                ? "grupo_familiar = ?, criterio_grupo_familiar = ?, observacion_grupo_familiar = ?, "
                : "";
        String columnasSolicitanteUpdate = soportaTipoSolicitante
                ? "tipo_solicitante = ?, id_centro_atencion = ?, id_unidad_organica = ?, "
                : "";
        String columnaMunicipalidadUpdate = soportaMunicipalidad ? "id_municipalidad = ?, " : "";
        String update = "UPDATE expediente_solicitud SET "
                + "id_canal_recepcion = ?, id_persona_solicitante = ?, numero_tramite_documentario = ?, "
                + "numero_expediente_sgd = ?, fecha_recepcion = ?, asunto = ?, observacion = ?, potencial_duplicado = ?, "
                + columnasGrupoUpdate
                + columnasSolicitanteUpdate
                + columnaMunicipalidadUpdate
                + "modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_solicitud = ?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            int idx = 1;
            setLongOrNull(ps, idx++, idCanal);
            setLongOrNull(ps, idx++, idSolicitante);
            ps.setString(idx++, solicitud.getNumeroTramite());
            ps.setString(idx++, solicitud.getNumeroExpedienteSgd());
            ps.setDate(idx++, toSqlDate(solicitud.getFechaRecepcion()));
            ps.setString(idx++, solicitud.getTipoProcedimientoNombre());
            ps.setString(idx++, limitar(observacionSolicitud(dto), 1000));
            ps.setInt(idx++, dto.isPosibleDuplicado() ? 1 : 0);
            if (soportaGrupoFamiliar) {
                ps.setInt(idx++, solicitud.isGrupoFamiliar() ? 1 : 0);
                ps.setString(idx++, limitar(resolverCriterioGrupoFamiliar(solicitud), 80));
                ps.setString(idx++, limitar(solicitud.getObservacionGrupoFamiliar(), 500));
            }
            if (soportaTipoSolicitante) {
                ps.setString(idx++, solicitud.getTipoSolicitanteEfectivo());
                setLongOrNull(ps, idx++, solicitud.getIdCentroAtencion());
                setLongOrNull(ps, idx++, solicitud.getIdUnidadOrganica());
            }
            if (soportaMunicipalidad) {
                setLongOrNull(ps, idx++, solicitud.getIdMunicipalidad());
            }
            ps.setLong(idx, idSolicitud);
            ps.executeUpdate();
        }
    }

    private void upsertActa(Connection conn, ExpedienteEdicionManualDTO dto) throws SQLException {
        Long idActa = obtenerUltimoId(conn, "expediente_acta", "id_expediente_acta", dto.getIdExpediente());
        DatosActaDTO acta = dto.getActa();
        Long idTipoActa = null;
        if (hasText(acta.getTipoActaCodigo())) {
            idTipoActa = catalogoLookupDAO.obtenerTipoActaId(conn, acta.getTipoActaCodigo());
        }
        if (idActa == null) {
            String insert = "INSERT INTO expediente_acta (id_expediente, id_tipo_acta, numero_acta, anio_acta, activo) "
                    + "VALUES (?, ?, ?, ?, 1)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setLong(1, dto.getIdExpediente());
                setLongOrNull(ps, 2, idTipoActa);
                ps.setString(3, acta.getNumeroActa());
                setIntegerOrNull(ps, 4, acta.getAnioActa());
                ps.executeUpdate();
            }
            return;
        }
        String update = "UPDATE expediente_acta SET id_tipo_acta = ?, numero_acta = ?, anio_acta = ?, "
                + "modificado_en = SYSTIMESTAMP WHERE id_expediente_acta = ?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            setLongOrNull(ps, 1, idTipoActa);
            ps.setString(2, acta.getNumeroActa());
            setIntegerOrNull(ps, 3, acta.getAnioActa());
            ps.setLong(4, idActa);
            ps.executeUpdate();
        }
    }

    private void upsertDocumento(Connection conn, ExpedienteEdicionManualDTO dto) throws SQLException {
        Long idDocumento = obtenerUltimoId(conn, "expediente_documento", "id_expediente_documento", dto.getIdExpediente());
        DatosSolicitudDTO solicitud = dto.getSolicitud();
        if (idDocumento == null) {
            String insert = "INSERT INTO expediente_documento ("
                    + "id_expediente, nombre_documento, numero_documento, fecha_documento, activo"
                    + ") VALUES (?, ?, ?, ?, 1)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setLong(1, dto.getIdExpediente());
                ps.setString(2, solicitud.getTipoDocumentoNombre());
                ps.setString(3, solicitud.getNumeroDocumento());
                ps.setDate(4, toSqlDate(solicitud.getFechaRecepcion()));
                ps.executeUpdate();
            }
            return;
        }
        String update = "UPDATE expediente_documento SET nombre_documento = ?, numero_documento = ?, "
                + "fecha_documento = ?, modificado_en = SYSTIMESTAMP WHERE id_expediente_documento = ?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, solicitud.getTipoDocumentoNombre());
            ps.setString(2, solicitud.getNumeroDocumento());
            ps.setDate(3, toSqlDate(solicitud.getFechaRecepcion()));
            ps.setLong(4, idDocumento);
            ps.executeUpdate();
        }
    }

    private Long upsertPersonaRelacion(
            Connection conn,
            Long idExpediente,
            String tipoRelacion,
            DatosPersonaRegistroDTO persona) throws SQLException {
        if (persona == null || !hasText(persona.getNombreCompleto())) {
            return null;
        }
        Long idPersona = obtenerPersonaRelacion(conn, idExpediente, tipoRelacion);
        if (idPersona == null) {
            idPersona = insertarPersona(conn, persona);
            insertarExpedientePersona(conn, idExpediente, idPersona, tipoRelacion);
        } else {
            actualizarPersona(conn, idPersona, persona);
        }
        return idPersona;
    }

    /**
     * Desactiva (baja lógica) la relación activa de {@code tipoRelacion} de este expediente en
     * EXPEDIENTE_PERSONA, si existe (14/08/2026, iniciativa "Solicitante con 3 tipos"). No borra la
     * fila de PERSONA en sí (puede seguir en uso por otro rol/expediente), solo el vínculo.
     */
    private void desactivarPersonaRelacion(Connection conn, Long idExpediente, String tipoRelacion) throws SQLException {
        String sql = "UPDATE expediente_persona SET activo = 0, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND tipo_relacion_persona = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, tipoRelacion);
            ps.executeUpdate();
        }
    }

    private Long obtenerPersonaRelacion(Connection conn, Long idExpediente, String tipoRelacion) throws SQLException {
        String sql = "SELECT id_persona FROM (SELECT ep.id_persona FROM expediente_persona ep "
                + "WHERE ep.id_expediente = ? AND ep.tipo_relacion_persona = ? AND ep.activo = 1 "
                + "ORDER BY ep.creado_en DESC, ep.id_expediente_persona DESC) WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, tipoRelacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return getLongOrNull(rs, "id_persona");
            }
        }
    }

    private Long insertarPersona(Connection conn, DatosPersonaRegistroDTO persona) throws SQLException {
        boolean soportaUbigeo = soportaUbigeoPersona(conn);
        boolean soportaApellidos = soportaApellidosPersona(conn);
        String columnasUbigeo = soportaUbigeo
                ? ", id_ubigeo_distrito, departamento, provincia, distrito"
                : "";
        String valoresUbigeo = soportaUbigeo ? ", ?, ?, ?, ?" : "";
        String columnasApellidos = soportaApellidos ? ", apellido_paterno, apellido_materno, nombres" : "";
        String valoresApellidos = soportaApellidos ? ", ?, ?, ?" : "";
        String sql = "INSERT INTO persona (tipo_documento, numero_documento, razon_social, "
                + "correo_electronico, telefono, direccion" + columnasUbigeo + columnasApellidos + ", activo) "
                + "VALUES (?, ?, ?, ?, ?, ?" + valoresUbigeo + valoresApellidos + ", 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_PERSONA"})) {
            int idx = 1;
            ps.setString(idx++, persona.getTipoDocumento());
            ps.setString(idx++, persona.getNumeroDocumento());
            ps.setString(idx++, persona.getNombreCompleto());
            ps.setString(idx++, persona.getCorreo());
            ps.setString(idx++, persona.getTelefono());
            ps.setString(idx++, persona.getDireccion());
            if (soportaUbigeo) {
                setLongOrNull(ps, idx++, persona.getIdDistrito());
                ps.setString(idx++, persona.getDepartamento());
                ps.setString(idx++, persona.getProvincia());
                ps.setString(idx++, persona.getDistrito());
            }
            if (soportaApellidos) {
                ps.setString(idx++, persona.getApellidoPaterno());
                ps.setString(idx++, persona.getApellidoMaterno());
                ps.setString(idx++, persona.getNombres());
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo registrar la persona del expediente.");
    }

    private void actualizarPersona(Connection conn, Long idPersona, DatosPersonaRegistroDTO persona) throws SQLException {
        boolean soportaUbigeo = soportaUbigeoPersona(conn);
        boolean soportaApellidos = soportaApellidosPersona(conn);
        String columnasUbigeo = soportaUbigeo
                ? "id_ubigeo_distrito = ?, departamento = ?, provincia = ?, distrito = ?, "
                : "";
        String columnasApellidos = soportaApellidos
                ? "apellido_paterno = ?, apellido_materno = ?, nombres = ?, "
                : "";
        String sql = "UPDATE persona SET tipo_documento = ?, numero_documento = ?, razon_social = ?, "
                + "correo_electronico = ?, telefono = ?, direccion = ?, "
                + columnasUbigeo
                + columnasApellidos
                + "modificado_en = SYSTIMESTAMP WHERE id_persona = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setString(idx++, persona.getTipoDocumento());
            ps.setString(idx++, persona.getNumeroDocumento());
            ps.setString(idx++, persona.getNombreCompleto());
            ps.setString(idx++, persona.getCorreo());
            ps.setString(idx++, persona.getTelefono());
            ps.setString(idx++, persona.getDireccion());
            if (soportaUbigeo) {
                setLongOrNull(ps, idx++, persona.getIdDistrito());
                ps.setString(idx++, persona.getDepartamento());
                ps.setString(idx++, persona.getProvincia());
                ps.setString(idx++, persona.getDistrito());
            }
            if (soportaApellidos) {
                ps.setString(idx++, persona.getApellidoPaterno());
                ps.setString(idx++, persona.getApellidoMaterno());
                ps.setString(idx++, persona.getNombres());
            }
            ps.setLong(idx, idPersona);
            ps.executeUpdate();
        }
    }

    private void insertarExpedientePersona(Connection conn, Long idExpediente, Long idPersona, String tipoRelacion) throws SQLException {
        String sql = "INSERT INTO expediente_persona (id_expediente, id_persona, tipo_relacion_persona, activo) "
                + "VALUES (?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idPersona);
            ps.setString(3, tipoRelacion);
            ps.executeUpdate();
        }
    }

    private void insertarHistorial(Connection conn, ExpedienteEdicionManualDTO dto, ExpedienteEstado estado, boolean desdeAnalisis)
            throws SQLException {
        Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_EDICION);
        if (idMovimiento == null) {
            throw new SQLException("No se encontró el movimiento RECEPCION_DOCUMENTO para registrar la edición manual.");
        }
        String comentarioBase = desdeAnalisis
                ? "Edición de datos desde Análisis. "
                : "Edición manual de datos de recepción. ";
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_etapa_origen, id_estado_origen, "
                + "id_etapa_destino, id_estado_destino, tabla_relacionada, id_registro_relacionado, "
                + "comentario, motivo, activo"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, 'EXPEDIENTE', ?, ?, 'EDICION_MANUAL_RECEPCION', 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, dto.getIdExpediente());
            ps.setLong(2, idMovimiento);
            ps.setLong(3, estado.idEtapa);
            ps.setLong(4, estado.idEstado);
            ps.setLong(5, estado.idEtapa);
            ps.setLong(6, estado.idEstado);
            ps.setLong(7, dto.getIdExpediente());
            ps.setString(8, limitar(comentarioBase + "Expediente: "
                    + textoNumeroExpediente(dto.getNumeroExpediente())
                    + ". No se modificó el número de expediente.", 2000));
            ps.executeUpdate();
        }
    }

    private ExpedienteEstado bloquearYObtenerEstado(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT e.id_etapa_actual, e.id_estado_actual, et.codigo AS etapa_codigo, est.codigo AS estado_codigo, "
                + "e.numero_expediente, e.id_usuario_responsable_actual "
                + "FROM expediente e "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "WHERE e.id_expediente = ? AND e.activo = 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No se encontró el expediente seleccionado.");
                }
                return new ExpedienteEstado(
                        rs.getLong("id_etapa_actual"),
                        rs.getLong("id_estado_actual"),
                        rs.getString("etapa_codigo"),
                        rs.getString("estado_codigo"),
                        rs.getString("numero_expediente"),
                        getLongOrNull(rs, "id_usuario_responsable_actual"));
            }
        }
    }

    private void validarEditable(ExpedienteEstado estado, boolean tieneAsignacionActiva) throws SQLException {
        validarEditable(estado, tieneAsignacionActiva, false);
    }

    /**
     * Port de {@code validarEditable(estado, tieneAsignacionActiva, desdeAnalisis)} (V2): cuando
     * {@code desdeAnalisis} es {@code true}, se omite por completo la exigencia de etapa
     * REGISTRO/estado REGISTRADO/sin asignación — un expediente ya recibido en Análisis nunca
     * cumple esa condición, así que V2 directamente no la aplica cuando la edición se abre desde ese
     * módulo (mismo criterio replicado literal en V3, 21/08/2026, pedido explícito del usuario).
     */
    private void validarEditable(ExpedienteEstado estado, boolean tieneAsignacionActiva, boolean desdeAnalisis) throws SQLException {
        if (desdeAnalisis) {
            return;
        }
        if (!CODIGO_ETAPA_REGISTRO.equalsIgnoreCase(safe(estado.etapaCodigo))
                || !CODIGO_ESTADO_REGISTRADO.equalsIgnoreCase(safe(estado.estadoCodigo))) {
            throw new SQLException("Solo se permite editar expedientes en estado Registrado de Registro / Recepción.");
        }
        if (estado.idResponsableActual != null || tieneAsignacionActiva) {
            throw new SQLException("El expediente ya tiene asignación a abogado. No se permite edición manual desde Recepción.");
        }
    }

    private boolean tieneAsignacionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT COUNT(1) FROM expediente_asignacion WHERE id_expediente = ? AND activa = 1 AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Long obtenerUltimoId(Connection conn, String tabla, String columnaId, Long idExpediente) throws SQLException {
        String sql = "SELECT " + columnaId + " FROM (SELECT " + columnaId + " FROM " + tabla
                + " WHERE id_expediente = ? AND activo = 1 ORDER BY creado_en DESC, " + columnaId + " DESC) WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return getLongOrNull(rs, columnaId);
            }
        }
    }

    private Date calcularFechaVencimiento(Connection conn, LocalDate fechaSolicitud, String procedimientoRegistral) throws SQLException {
        if (fechaSolicitud == null) {
            return null;
        }
        return Date.valueOf(calendarioLaboralService.calcularFechaVencimientoSolicitud(conn, fechaSolicitud, procedimientoRegistral));
    }

    private String observacionSolicitud(ExpedienteEdicionManualDTO dto) {
        StringBuilder sb = new StringBuilder();
        append(sb, "Validación inicial", dto.getSolicitud().getValidacionInicial());
        append(sb, "Hoja de envío", dto.getSolicitud().getHojaEnvio());
        append(sb, "Tipo de solicitud", dto.getSolicitud().getTipoSolicitudNombre());
        append(sb, "Tipo de documento", dto.getSolicitud().getTipoDocumentoNombre());
        append(sb, "N° EXPEDIENTE SGD", dto.getSolicitud().getNumeroExpedienteSgd());
        append(sb, "Grupo familiar", dto.getSolicitud().getGrupoFamiliarTexto());
        append(sb, "Criterio grupo familiar", dto.getSolicitud().getCriterioGrupoFamiliar());
        append(sb, "Observación grupo familiar", dto.getSolicitud().getObservacionGrupoFamiliar());
        append(sb, "Tipo de acta", dto.getActa().getTipoActaNombre());
        append(sb, "Observaciones de registro", dto.getObservacionesGenerales());
        append(sb, "Edición manual", "Datos de recepción actualizados sin modificar número de expediente");
        return sb.toString();
    }

    private String resolverCriterioGrupoFamiliar(DatosSolicitudDTO solicitud) {
        if (solicitud == null) {
            return null;
        }
        if (hasText(solicitud.getCriterioGrupoFamiliar())) {
            return solicitud.getCriterioGrupoFamiliar();
        }
        return solicitud.isGrupoFamiliar() ? "MANUAL" : null;
    }

    private void registrarAlertasEdicion(Connection conn, ExpedienteEdicionManualDTO dto, Long idUsuarioActual) throws SQLException {
        List<String> alertas = new ArrayList<>();
        if (hasText(dto.getMotivoDuplicado())) {
            alertas.add("Potencial duplicado");
        } else if (dto.getSolicitud() != null
                && (dto.getSolicitud().isGrupoFamiliar()
                || hasText(dto.getSolicitud().getCriterioGrupoFamiliar())
                || hasText(dto.getSolicitud().getObservacionGrupoFamiliar()))) {
            alertas.add("Posible Grupo Familiar");
        }
        expedienteAlertaDAO.registrarAlertas(conn, dto.getIdExpediente(), "EDICION_MANUAL", "ALERTA", alertas, idUsuarioActual);
    }

    private void append(StringBuilder sb, String etiqueta, String valor) {
        if (!hasText(valor)) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" | ");
        }
        sb.append(etiqueta).append(": ").append(valor.trim());
    }

    private String extraerObservacion(String observacion, String etiqueta) {
        if (!hasText(observacion) || !hasText(etiqueta)) {
            return null;
        }
        String prefix = etiqueta.trim().toUpperCase(Locale.ROOT) + ":";
        String[] partes = observacion.split("\\s*\\|\\s*");
        for (String parte : partes) {
            if (parte != null && parte.trim().toUpperCase(Locale.ROOT).startsWith(prefix)) {
                return parte.trim().substring(prefix.length()).trim();
            }
        }
        return null;
    }

    private String codigoTipoSolicitud(String nombre) {
        if (!hasText(nombre)) {
            return null;
        }
        if ("PARTE".equalsIgnoreCase(nombre) || "Parte".equalsIgnoreCase(nombre)) {
            return "PARTE";
        }
        if ("OFICIO".equalsIgnoreCase(nombre) || "Oficio".equalsIgnoreCase(nombre)) {
            return "OFICIO";
        }
        return nombre.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private String codigoCatalogoPorNombre(Connection conn, String tabla, String nombre) throws SQLException {
        if (!hasText(nombre)) {
            return null;
        }
        String sql = "SELECT codigo FROM " + tabla + " "
                + "WHERE activo = 1 AND (UPPER(nombre) = ? OR UPPER(codigo) = ?) AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String normalized = nombre.trim().toUpperCase(Locale.ROOT);
            ps.setString(1, normalized);
            ps.setString(2, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("codigo") : null;
            }
        }
    }

    private static String nombrePersona(String alias) {
        return "NULLIF(TRIM(NVL(" + alias + ".razon_social, TRIM(NVL(" + alias + ".nombres, '') || ' ' || NVL(" + alias + ".apellidos, '')))), '')";
    }

    private static String textoNumeroExpediente(String numeroExpediente) {
        return hasText(numeroExpediente) ? numeroExpediente.trim() : "Sin número por duplicado";
    }

    private static String firstText(String first, String fallback) {
        return hasText(first) ? first : fallback;
    }

    private static Date toSqlDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private boolean soportaGrupoFamiliar(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_SOLICITUD' AND column_name = 'GRUPO_FAMILIAR'";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private boolean soportaUbigeoPersona(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'PERSONA' AND column_name IN ('ID_UBIGEO_DISTRITO', 'DEPARTAMENTO', 'PROVINCIA', 'DISTRITO')";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 4;
        }
    }

    /**
     * Mismo patrón que {@link #soportaUbigeoPersona(Connection)} (degradación elegante mientras el
     * script {@code 100_persona_apellido_paterno_materno.sql} no se ejecute contra Oracle): ver
     * Javadoc gemelo en {@code ExpedienteRegistroDAO}.
     */
    private boolean soportaApellidosPersona(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'PERSONA' AND column_name IN ('APELLIDO_PATERNO', 'APELLIDO_MATERNO')";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 2;
        }
    }

    /** Mismo patrón que {@link #soportaApellidosPersona(Connection)} — ver Javadoc gemelo en ExpedienteRegistroDAO. */
    private boolean soportaTipoSolicitante(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_SOLICITUD' "
                + "AND column_name IN ('TIPO_SOLICITANTE', 'ID_CENTRO_ATENCION', 'ID_UNIDAD_ORGANICA')";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 3;
        }
    }

    /**
     * Mismo patrón que {@link #soportaTipoSolicitante(Connection)}, independiente de ese chequeo: la
     * columna {@code ID_MUNICIPALIDAD} viene de un script separado ({@code
     * 105_municipalidad_tipo_solicitante.sql}), que puede ejecutarse en un momento distinto al 101.
     */
    private boolean soportaMunicipalidad(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_SOLICITUD' AND column_name = 'ID_MUNICIPALIDAD'";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 1;
        }
    }

    private static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static void setIntegerOrNull(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String limitar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        int low = 0;
        int high = Math.min(value.length(), maxLength);
        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            if (value.substring(0, mid).getBytes(StandardCharsets.UTF_8).length <= maxLength) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return value.substring(0, low);
    }

    private void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
        }
    }

    private static class ExpedienteEstado {

        private final Long idEtapa;
        private final Long idEstado;
        private final String etapaCodigo;
        private final String estadoCodigo;
        private final String numeroExpediente;
        private final Long idResponsableActual;

        private ExpedienteEstado(
                Long idEtapa,
                Long idEstado,
                String etapaCodigo,
                String estadoCodigo,
                String numeroExpediente,
                Long idResponsableActual) {
            this.idEtapa = idEtapa;
            this.idEstado = idEstado;
            this.etapaCodigo = etapaCodigo;
            this.estadoCodigo = estadoCodigo;
            this.numeroExpediente = numeroExpediente;
            this.idResponsableActual = idResponsableActual;
        }
    }
}
