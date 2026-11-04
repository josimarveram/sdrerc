package com.sdrerc.v3.infrastructure.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.springframework.stereotype.Repository;

/**
 * Datos de notificación/ubicación (correo, teléfono, dirección, ubigeo) de UNA solicitud —
 * separados de {@code PERSONA} (28/08/2026, pedido explícito del usuario, diagnosticado a partir de
 * un reporte real: "cuando se trata de centro de atención, unidad orgánica y municipalidad no se
 * está guardando los datos de notificación y ubicación"). Causa raíz: esos 3 tipos de solicitante
 * NUNCA tienen una fila {@code PERSONA} como remitente (por diseño, ver v3/CLAUDE.md "Solicitante
 * con 3 tipos" — {@code EXPEDIENTE_PERSONA} con {@code tipo_relacion_persona='REMITENTE'} solo se
 * crea para {@code tipoSolicitante=CIUDADANO}), así que un campo que dependa de esa persona jamás
 * puede guardarse para ellos. Esta tabla nueva ({@code GSTD_EXPEDIENTE_SOLICITUD_UBICACION}, script
 * {@code 120_expediente_solicitud_ubicacion.sql}, <b>NO ejecutado todavía</b>) cuelga en cambio de
 * {@code EXPEDIENTE_SOLICITUD} — existe siempre, sin importar el tipo de solicitante.
 *
 * <p>Nombrada según el estándar OTI (código de sistema GS, ver
 * {@code docs/nomenclatura_bd/mapeo_nomenclatura_gs.md}) por ser un objeto NUEVO creado después del
 * renombrado del 26/08/2026 — a diferencia del resto del esquema, no necesita vista de
 * compatibilidad porque no existe ningún nombre "viejo" que preservar.</p>
 *
 * <p><b>Degradación elegante mientras el script no se ejecute</b> (mismo patrón ya establecido en
 * el proyecto — {@code soportaTipoSolicitante}/{@code soportaMunicipalidad}/etc.): {@link
 * #soportado(Connection)} le permite a {@code ExpedienteRegistroDAO}/{@code
 * ExpedienteEdicionManualDAO} seguir escribiendo estos campos en {@code PERSONA} exactamente como
 * antes (cero regresión) hasta que la tabla exista; una vez que existe, esos mismos llamadores dejan
 * de escribirlos en {@code PERSONA} y usan exclusivamente esta clase — ver Javadoc de {@code
 * ExpedienteEdicionManualDAO#actualizarPersona}/{@code ExpedienteRegistroDAO#insertarPersonaManual}.</p>
 */
@Repository
public class ExpedienteSolicitudUbicacionDAO {

    /** Resultado de solo lectura para el "Panel de datos". */
    public record Datos(
            String correo,
            String telefono,
            String direccion,
            Long idUbigeoDistrito,
            String departamento,
            String provincia,
            String distrito) {
    }

    public boolean soportado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tables WHERE table_name = 'GSTD_EXPEDIENTE_SOLICITUD_UBICACION'";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /** {@code null} si la solicitud todavía no tiene fila propia (nunca se guardó nada para ella). */
    public Datos obtener(Connection conn, Long idExpedienteSolicitud) throws SQLException {
        if (idExpedienteSolicitud == null) {
            return null;
        }
        String sql = "SELECT correo_electronico, telefono, di_direccion, id_ubigeo_distrito, "
                + "departamento, provincia, distrito "
                + "FROM gstd_expediente_solicitud_ubicacion WHERE id_expediente_solicitud = ? AND in_activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpedienteSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long idUbigeo = rs.getLong("id_ubigeo_distrito");
                return new Datos(
                        rs.getString("correo_electronico"),
                        rs.getString("telefono"),
                        rs.getString("di_direccion"),
                        rs.wasNull() ? null : idUbigeo,
                        rs.getString("departamento"),
                        rs.getString("provincia"),
                        rs.getString("distrito"));
            }
        }
    }

    /**
     * Upsert (crea la fila 1:1 de la solicitud la primera vez, la actualiza después) — nunca borra.
     * Se llama siempre que se registra/edita una solicitud, sin importar el tipo de solicitante
     * (a diferencia de {@code upsertPersonaRelacion}, que solo aplica a CIUDADANO).
     */
    public void guardar(
            Connection conn,
            Long idExpedienteSolicitud,
            String correo,
            String telefono,
            String direccion,
            Long idUbigeoDistrito,
            String departamento,
            String provincia,
            String distrito) throws SQLException {
        Long idExistente = obtenerIdFila(conn, idExpedienteSolicitud);
        if (idExistente == null) {
            String sql = "INSERT INTO gstd_expediente_solicitud_ubicacion ("
                    + "id_expediente_solicitud, correo_electronico, telefono, di_direccion, "
                    + "id_ubigeo_distrito, departamento, provincia, distrito, in_activo"
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpedienteSolicitud);
                ps.setString(2, correo);
                ps.setString(3, telefono);
                ps.setString(4, direccion);
                setLongOrNull(ps, 5, idUbigeoDistrito);
                ps.setString(6, departamento);
                ps.setString(7, provincia);
                ps.setString(8, distrito);
                ps.executeUpdate();
            }
            return;
        }
        String sql = "UPDATE gstd_expediente_solicitud_ubicacion SET correo_electronico = ?, telefono = ?, "
                + "di_direccion = ?, id_ubigeo_distrito = ?, departamento = ?, provincia = ?, distrito = ?, "
                + "fe_modificado_en = SYSTIMESTAMP WHERE id_expediente_solicitud_ubicacion = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, telefono);
            ps.setString(3, direccion);
            setLongOrNull(ps, 4, idUbigeoDistrito);
            ps.setString(5, departamento);
            ps.setString(6, provincia);
            ps.setString(7, distrito);
            ps.setLong(8, idExistente);
            ps.executeUpdate();
        }
    }

    private Long obtenerIdFila(Connection conn, Long idExpedienteSolicitud) throws SQLException {
        String sql = "SELECT id_expediente_solicitud_ubicacion FROM gstd_expediente_solicitud_ubicacion "
                + "WHERE id_expediente_solicitud = ? AND in_activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpedienteSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }
}
