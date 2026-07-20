package com.sdrerc.infrastructure.sdrercapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Propaga el cambio de etapa/estado de un expediente principal a sus expedientes asociados
 * (duplicados confirmados en EXPEDIENTE_RELACION, mismo numero SDRERC). Desde Asignacion en
 * adelante, un cambio de etapa/estado del expediente principal debe reflejarse de inmediato
 * en todos sus asociados activos, sin pasos manuales adicionales.
 *
 * Se invoca desde el metodo privado "actualizarExpediente" (o equivalente) de cada DAO de
 * transicion, en la misma transaccion/Connection, justo despues de mover al expediente que
 * origino el cambio. Si ese expediente no es principal de ninguna relacion activa, no hace
 * nada (consulta indexada por id_expediente_principal, costo despreciable).
 */
final class ExpedienteEstadoPropagacionDAO {

    private static final String TIPO_RELACION_DOCUMENTO_DUPLICADO_ASOCIADO = "DOCUMENTO_DUPLICADO_ASOCIADO";
    private static final String CODIGO_MOVIMIENTO_PROPAGACION = "PROPAGACION_ESTADO_ASOCIADO";

    private ExpedienteEstadoPropagacionDAO() {
    }

    static int propagarEstadoAAsociados(
            Connection conn,
            Long idExpedientePrincipal,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idUsuarioModificador) throws SQLException {
        if (idExpedientePrincipal == null || idEtapaDestino == null || idEstadoDestino == null) {
            return 0;
        }
        List<AsociadoEstado> asociados = listarAsociadosActivosConEstado(conn, idExpedientePrincipal);
        if (asociados.isEmpty()) {
            return 0;
        }
        Long idMovimiento = new CatalogoLookupDAO().obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_PROPAGACION);
        int actualizados = 0;
        for (AsociadoEstado asociado : asociados) {
            boolean mismoDestino = idEtapaDestino.equals(asociado.idEtapa) && idEstadoDestino.equals(asociado.idEstado);
            if (mismoDestino) {
                continue;
            }
            actualizarEtapaEstado(conn, asociado.idExpediente, idEtapaDestino, idEstadoDestino, idUsuarioModificador);
            if (idMovimiento != null) {
                insertarHistorialPropagado(
                        conn,
                        asociado.idExpediente,
                        idMovimiento,
                        asociado.idEtapa,
                        asociado.idEstado,
                        idEtapaDestino,
                        idEstadoDestino,
                        idUsuarioModificador,
                        idExpedientePrincipal);
            }
            actualizados++;
        }
        return actualizados;
    }

    private static List<AsociadoEstado> listarAsociadosActivosConEstado(Connection conn, Long idExpedientePrincipal) throws SQLException {
        List<AsociadoEstado> asociados = new ArrayList<AsociadoEstado>();
        String sql = "SELECT e.id_expediente, e.id_etapa_actual, e.id_estado_actual "
                + "FROM expediente_relacion r "
                + "JOIN expediente e ON e.id_expediente = r.id_expediente_relacionado "
                + "WHERE r.activo = 1 AND UPPER(r.tipo_relacion) = ? "
                + "AND r.id_expediente_principal = ? AND e.activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, TIPO_RELACION_DOCUMENTO_DUPLICADO_ASOCIADO);
            ps.setLong(2, idExpedientePrincipal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    asociados.add(new AsociadoEstado(
                            getLongOrNull(rs, "id_expediente"),
                            getLongOrNull(rs, "id_etapa_actual"),
                            getLongOrNull(rs, "id_estado_actual")));
                }
            }
        }
        return asociados;
    }

    private static void actualizarEtapaEstado(
            Connection conn,
            Long idExpediente,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idUsuarioModificador) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_etapa_actual = ?, "
                + "id_estado_actual = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, "
                + "modificado_por = ?, "
                + "modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            setLongOrNull(ps, 3, idUsuarioModificador);
            ps.setLong(4, idExpediente);
            ps.executeUpdate();
        }
    }

    private static void insertarHistorialPropagado(
            Connection conn,
            Long idExpediente,
            Long idMovimiento,
            Long idEtapaOrigen,
            Long idEstadoOrigen,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idUsuarioModificador,
            Long idExpedientePrincipal) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, "
                + "id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino, "
                + "id_usuario_origen, tabla_relacionada, id_registro_relacionado, comentario, motivo, "
                + "activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, ?, 'EXPEDIENTE', ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idEtapaOrigen);
            setLongOrNull(ps, 4, idEstadoOrigen);
            ps.setLong(5, idEtapaDestino);
            ps.setLong(6, idEstadoDestino);
            setLongOrNull(ps, 7, idUsuarioModificador);
            setLongOrNull(ps, 8, idExpedientePrincipal);
            ps.setString(9, "Cambio de etapa/estado propagado automáticamente desde el expediente principal.");
            ps.setString(10, "PROPAGACION_ASOCIADO");
            setLongOrNull(ps, 11, idUsuarioModificador);
            ps.executeUpdate();
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

    private static final class AsociadoEstado {
        private final Long idExpediente;
        private final Long idEtapa;
        private final Long idEstado;

        private AsociadoEstado(Long idExpediente, Long idEtapa, Long idEstado) {
            this.idExpediente = idExpediente;
            this.idEtapa = idEtapa;
            this.idEstado = idEstado;
        }
    }
}
