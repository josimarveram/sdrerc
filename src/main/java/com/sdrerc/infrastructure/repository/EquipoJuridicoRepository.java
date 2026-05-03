package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.EquipoJuridicoConsultaItem;
import com.sdrerc.domain.model.EquipoJuridicoDetalle;
import com.sdrerc.domain.model.EquipoJuridicoResumen;
import com.sdrerc.domain.model.EquipoJuridicoSupervisorUpdateRequest;
import com.sdrerc.domain.model.EquipoJuridicoUpdateRequest;
import com.sdrerc.domain.model.PaginatedResult;
import com.sdrerc.domain.model.SupervisorComboItem;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EquipoJuridicoRepository {

    private static final String ABOGADO_NOMBRE_EXPR =
            "CASE " +
            "WHEN abo.ID_TECNICO IS NOT NULL THEN " +
            "  TRIM(NVL(tab.APELLIDO_PATERNO, '') || ' ' || NVL(tab.APELLIDO_MATERNO, '') || ', ' || NVL(tab.NOMBRES, '')) " +
            "ELSE abo.FULL_NAME END";

    private static final String SUPERVISOR_NOMBRE_EXPR =
            "CASE " +
            "WHEN sup.ID_TECNICO IS NOT NULL THEN " +
            "  TRIM(NVL(tsup.APELLIDO_PATERNO, '') || ' ' || NVL(tsup.APELLIDO_MATERNO, '') || ', ' || NVL(tsup.NOMBRES, '')) " +
            "ELSE sup.FULL_NAME END";

    public List<SupervisorComboItem> listarSupervisoresActivos() throws SQLException {
        String sql =
            "SELECT DISTINCT " +
            "u.USER_ID, " +
            "u.USERNAME, " +
            "CASE " +
            "  WHEN u.ID_TECNICO IS NOT NULL THEN " +
            "    TRIM(NVL(t.APELLIDO_PATERNO, '') || ' ' || NVL(t.APELLIDO_MATERNO, '') || ', ' || NVL(t.NOMBRES, '')) " +
            "  ELSE u.FULL_NAME " +
            "END AS NOMBRE_VISIBLE " +
            "FROM APP_USERS u " +
            "JOIN APP_USER_ROLES ur ON ur.USER_ID = u.USER_ID " +
            "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
            "LEFT JOIN TECNICO t ON t.ID_TECNICO = u.ID_TECNICO " +
            "WHERE UPPER(r.ROLE_NAME) = 'SUPERVISION' " +
            "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
            "AND UPPER(u.STATUS) IN ('ACTIVE', 'ACTIVO') " +
            "ORDER BY NOMBRE_VISIBLE";

        List<SupervisorComboItem> supervisores = new ArrayList<>();
        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                supervisores.add(new SupervisorComboItem(
                        rs.getLong("USER_ID"),
                        rs.getString("NOMBRE_VISIBLE"),
                        rs.getString("USERNAME"),
                        SupervisorComboItem.TIPO_SUPERVISOR
                ));
            }
        }
        return supervisores;
    }

    public PaginatedResult<EquipoJuridicoConsultaItem> buscarAbogados(
            Long supervisorId,
            boolean soloSinSupervisor,
            String filtro,
            String estado,
            int page,
            int pageSize) throws SQLException {

        int safePage = Math.max(1, page);
        int safePageSize = pageSize <= 0 ? 25 : pageSize;
        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();
        construirFiltros(where, params, supervisorId, soloSinSupervisor, filtro, estado);

        int totalRecords = contarAbogados(where.toString(), params);
        int totalPages = totalRecords == 0
                ? 1
                : (int) Math.ceil((double) totalRecords / safePageSize);
        if (safePage > totalPages) {
            safePage = totalPages;
        }

        String sql =
            "SELECT DISTINCT " +
            "abo.USER_ID AS ABOGADO_ID, " +
            "abo.ID_TECNICO AS ID_TECNICO, " +
            "abo.USERNAME AS ABOGADO_USERNAME, " +
            ABOGADO_NOMBRE_EXPR + " AS ABOGADO_NOMBRE, " +
            "sup.USER_ID AS SUPERVISOR_ID, " +
            "sup.USERNAME AS SUPERVISOR_USERNAME, " +
            SUPERVISOR_NOMBRE_EXPR + " AS SUPERVISOR_NOMBRE, " +
            "abo.STATUS AS ESTADO, " +
            "ci_tipo.DESCRIPCION AS TIPO_PERSONAL " +
            baseFromWhere() +
            where +
            "ORDER BY ABOGADO_NOMBRE " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<EquipoJuridicoConsultaItem> data = new ArrayList<>();
        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            int index = 1;
            for (Object param : params) {
                ps.setObject(index++, param);
            }
            ps.setInt(index++, (safePage - 1) * safePageSize);
            ps.setInt(index, safePageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.add(mapConsultaItem(rs));
                }
            }
        }

        return new PaginatedResult<>(data, safePage, safePageSize, totalRecords, totalPages);
    }

    public EquipoJuridicoDetalle obtenerDetalleAbogado(Long abogadoId) throws SQLException {
        if (abogadoId == null || abogadoId <= 0) {
            throw new IllegalArgumentException("Seleccione un abogado valido.");
        }

        String sql =
            "SELECT DISTINCT " +
            "u.USER_ID AS ABOGADO_ID, " +
            "u.USERNAME, " +
            "u.STATUS, " +
            "u.ID_TECNICO, " +
            "t.NUMERO_DOCUMENTO, " +
            "t.APELLIDO_PATERNO, " +
            "t.APELLIDO_MATERNO, " +
            "t.NOMBRES, " +
            "t.ID_TIPO_PERSONAL, " +
            "ci.DESCRIPCION AS TIPO_PERSONAL, " +
            "t.ACTIVE AS TECNICO_ACTIVE, " +
            "s.SUPERVISOR_ID " +
            "FROM APP_USERS u " +
            "JOIN APP_USER_ROLES ur ON ur.USER_ID = u.USER_ID " +
            "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
            "LEFT JOIN TECNICO t ON t.ID_TECNICO = u.ID_TECNICO " +
            "LEFT JOIN CATALOGO_ITEM ci ON ci.ID_CATALOGO_ITEM = t.ID_TIPO_PERSONAL " +
            "LEFT JOIN APP_USER_SUPERVISION s ON s.ABOGADO_ID = u.USER_ID " +
            "WHERE u.USER_ID = ? " +
            "AND UPPER(r.ROLE_NAME) = 'ABOGADO' " +
            "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO')";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, abogadoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDetalle(rs);
                }
            }
        }
        throw new IllegalArgumentException("No se encontro el abogado seleccionado.");
    }

    public void actualizarEquipoJuridico(EquipoJuridicoUpdateRequest request) throws SQLException {
        validarUpdateRequest(request);

        try (Connection cn = OracleConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (!tieneRol(cn, request.getAbogadoId(), "ABOGADO")) {
                    throw new IllegalArgumentException("El usuario seleccionado no tiene rol ABOGADO.");
                }
                if (!existeTecnico(cn, request.getIdTecnico())) {
                    throw new IllegalArgumentException("El tecnico asociado no existe.");
                }
                if (request.getSupervisorId() != null) {
                    if (request.getSupervisorId().equals(request.getAbogadoId())) {
                        throw new IllegalArgumentException("El abogado no puede ser su propio supervisor.");
                    }
                    if (!tieneRol(cn, request.getSupervisorId(), "SUPERVISION")) {
                        throw new IllegalArgumentException("El supervisor seleccionado no tiene rol SUPERVISION.");
                    }
                }

                actualizarTecnico(cn, request);
                actualizarUsuario(cn, request);
                actualizarSupervisor(cn, request);

                cn.commit();
            } catch (Exception ex) {
                cn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new IllegalStateException(ex.getMessage(), ex);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public EquipoJuridicoDetalle obtenerDetalleSupervisor(Long supervisorUserId) throws SQLException {
        if (supervisorUserId == null || supervisorUserId <= 0) {
            throw new IllegalArgumentException("Seleccione un supervisor valido.");
        }

        String sql =
            "SELECT DISTINCT " +
            "u.USER_ID AS ABOGADO_ID, " +
            "u.USERNAME, " +
            "u.STATUS, " +
            "u.ID_TECNICO, " +
            "t.NUMERO_DOCUMENTO, " +
            "t.APELLIDO_PATERNO, " +
            "t.APELLIDO_MATERNO, " +
            "t.NOMBRES, " +
            "t.ID_TIPO_PERSONAL, " +
            "ci.DESCRIPCION AS TIPO_PERSONAL, " +
            "t.ACTIVE AS TECNICO_ACTIVE, " +
            "CAST(NULL AS NUMBER) AS SUPERVISOR_ID " +
            "FROM APP_USERS u " +
            "JOIN APP_USER_ROLES ur ON ur.USER_ID = u.USER_ID " +
            "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
            "LEFT JOIN TECNICO t ON t.ID_TECNICO = u.ID_TECNICO " +
            "LEFT JOIN CATALOGO_ITEM ci ON ci.ID_CATALOGO_ITEM = t.ID_TIPO_PERSONAL " +
            "WHERE u.USER_ID = ? " +
            "AND UPPER(r.ROLE_NAME) = 'SUPERVISION' " +
            "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO')";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, supervisorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDetalle(rs);
                }
            }
        }
        throw new IllegalArgumentException("No se encontro el supervisor seleccionado.");
    }

    public void actualizarSupervisorEquipoJuridico(EquipoJuridicoSupervisorUpdateRequest request) throws SQLException {
        validarSupervisorUpdateRequest(request);

        try (Connection cn = OracleConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (!tieneRol(cn, request.getSupervisorId(), "SUPERVISION")) {
                    throw new IllegalArgumentException("El usuario seleccionado no tiene rol SUPERVISION.");
                }
                if (!existeTecnico(cn, request.getIdTecnico())) {
                    throw new IllegalArgumentException("El tecnico asociado no existe.");
                }
                if (!esEstadoActivo(request.getEstado()) && contarAbogadosAsignados(cn, request.getSupervisorId()) > 0) {
                    throw new IllegalStateException("No puede inactivar este supervisor porque tiene abogados asignados. Primero retire o cambie su equipo supervisado.");
                }

                EquipoJuridicoUpdateRequest update = new EquipoJuridicoUpdateRequest();
                update.setAbogadoId(request.getSupervisorId());
                update.setIdTecnico(request.getIdTecnico());
                update.setApellidoPaterno(request.getApellidoPaterno());
                update.setApellidoMaterno(request.getApellidoMaterno());
                update.setNombres(request.getNombres());
                update.setNumeroDocumento(request.getNumeroDocumento());
                update.setIdTipoPersonal(request.getIdTipoPersonal());
                update.setEstado(request.getEstado());
                update.setUsuarioModificacion(request.getUsuarioModificacion());

                actualizarTecnico(cn, update);
                actualizarUsuario(cn, update);

                cn.commit();
            } catch (Exception ex) {
                cn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new IllegalStateException(ex.getMessage(), ex);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public int contarAbogadosAsignados(Long supervisorUserId) throws SQLException {
        try (Connection cn = OracleConnection.getConnection()) {
            return contarAbogadosAsignados(cn, supervisorUserId);
        }
    }

    public EquipoJuridicoResumen obtenerResumen() throws SQLException {
        EquipoJuridicoResumen resumen = new EquipoJuridicoResumen();
        resumen.setTotalAbogados(contarPorRol("ABOGADO"));
        resumen.setTotalSupervisores(contarPorRol("SUPERVISION"));
        resumen.setAbogadosSinSupervisor(contarAbogadosSinSupervisor());
        resumen.setActivos(contarUsuariosEquipoPorEstado(true));
        resumen.setInactivos(contarUsuariosEquipoPorEstado(false));
        return resumen;
    }

    private String baseFromWhere() {
        return "FROM APP_USERS abo " +
            "JOIN APP_USER_ROLES urab ON urab.USER_ID = abo.USER_ID " +
            "JOIN APP_ROLES rab ON rab.ROLE_ID = urab.ROLE_ID " +
            "LEFT JOIN APP_USER_SUPERVISION s ON s.ABOGADO_ID = abo.USER_ID " +
            "LEFT JOIN APP_USERS sup ON sup.USER_ID = s.SUPERVISOR_ID " +
            "LEFT JOIN TECNICO tab ON tab.ID_TECNICO = abo.ID_TECNICO " +
            "LEFT JOIN CATALOGO_ITEM ci_tipo ON ci_tipo.ID_CATALOGO_ITEM = tab.ID_TIPO_PERSONAL " +
            "LEFT JOIN TECNICO tsup ON tsup.ID_TECNICO = sup.ID_TECNICO " +
            "WHERE UPPER(rab.ROLE_NAME) = 'ABOGADO' " +
            "AND UPPER(rab.STATUS) IN ('ACTIVE', 'ACTIVO') ";
    }

    private void construirFiltros(
            StringBuilder where,
            List<Object> params,
            Long supervisorId,
            boolean soloSinSupervisor,
            String filtro,
            String estado) {

        if (soloSinSupervisor) {
            where.append("AND s.SUPERVISOR_ID IS NULL ");
        } else if (supervisorId != null && supervisorId > 0) {
            where.append("AND s.SUPERVISOR_ID = ? ");
            params.add(supervisorId);
        }

        if (estado != null && !"TODOS".equalsIgnoreCase(estado.trim())) {
            if ("ACTIVO".equalsIgnoreCase(estado) || "ACTIVE".equalsIgnoreCase(estado)) {
                where.append("AND UPPER(abo.STATUS) IN ('ACTIVE', 'ACTIVO') ");
            } else if ("INACTIVO".equalsIgnoreCase(estado) || "INACTIVE".equalsIgnoreCase(estado)) {
                where.append("AND UPPER(abo.STATUS) IN ('INACTIVE', 'INACTIVO') ");
            }
        }

        String filtroNormalizado = filtro == null ? "" : filtro.trim().toUpperCase();
        if (!filtroNormalizado.isEmpty()) {
            String like = "%" + filtroNormalizado + "%";
            where.append("AND (");
            where.append("UPPER(abo.USERNAME) LIKE ? ");
            where.append("OR UPPER(").append(ABOGADO_NOMBRE_EXPR).append(") LIKE ? ");
            where.append("OR UPPER(NVL(sup.USERNAME, '')) LIKE ? ");
            where.append("OR UPPER(NVL(").append(SUPERVISOR_NOMBRE_EXPR).append(", '')) LIKE ? ");
            where.append(") ");
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
    }

    private int contarAbogados(String where, List<Object> params) throws SQLException {
        String sql =
            "SELECT COUNT(DISTINCT abo.USER_ID) " +
            baseFromWhere() +
            where;

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int index = 1;
            for (Object param : params) {
                ps.setObject(index++, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int contarPorRol(String roleName) throws SQLException {
        String sql =
            "SELECT COUNT(DISTINCT u.USER_ID) " +
            "FROM APP_USERS u " +
            "JOIN APP_USER_ROLES ur ON ur.USER_ID = u.USER_ID " +
            "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
            "WHERE UPPER(r.ROLE_NAME) = ? " +
            "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO')";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int contarAbogadosSinSupervisor() throws SQLException {
        String sql =
            "SELECT COUNT(DISTINCT abo.USER_ID) " +
            baseFromWhere() +
            "AND s.SUPERVISOR_ID IS NULL";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int contarUsuariosEquipoPorEstado(boolean activos) throws SQLException {
        String sql =
            "SELECT COUNT(DISTINCT u.USER_ID) " +
            "FROM APP_USERS u " +
            "JOIN APP_USER_ROLES ur ON ur.USER_ID = u.USER_ID " +
            "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
            "WHERE UPPER(r.ROLE_NAME) IN ('ABOGADO', 'SUPERVISION') " +
            "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
            (activos
                    ? "AND UPPER(u.STATUS) IN ('ACTIVE', 'ACTIVO') "
                    : "AND UPPER(u.STATUS) IN ('INACTIVE', 'INACTIVO') ");

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private EquipoJuridicoConsultaItem mapConsultaItem(ResultSet rs) throws SQLException {
        EquipoJuridicoConsultaItem item = new EquipoJuridicoConsultaItem();
        item.setAbogadoId(rs.getLong("ABOGADO_ID"));
        int idTecnico = rs.getInt("ID_TECNICO");
        item.setIdTecnico(rs.wasNull() ? null : idTecnico);
        item.setAbogadoUsername(rs.getString("ABOGADO_USERNAME"));
        item.setAbogadoNombre(rs.getString("ABOGADO_NOMBRE"));
        long supervisorId = rs.getLong("SUPERVISOR_ID");
        item.setSupervisorId(rs.wasNull() ? null : supervisorId);
        item.setSupervisorUsername(rs.getString("SUPERVISOR_USERNAME"));
        item.setSupervisorNombre(rs.getString("SUPERVISOR_NOMBRE"));
        item.setEstado(rs.getString("ESTADO"));
        item.setTipoPersonal(rs.getString("TIPO_PERSONAL"));
        return item;
    }

    private EquipoJuridicoDetalle mapDetalle(ResultSet rs) throws SQLException {
        EquipoJuridicoDetalle detalle = new EquipoJuridicoDetalle();
        detalle.setAbogadoId(rs.getLong("ABOGADO_ID"));
        detalle.setUsername(rs.getString("USERNAME"));
        detalle.setEstado(rs.getString("STATUS"));
        int idTecnico = rs.getInt("ID_TECNICO");
        detalle.setIdTecnico(rs.wasNull() ? null : idTecnico);
        long numeroDocumento = rs.getLong("NUMERO_DOCUMENTO");
        detalle.setNumeroDocumento(rs.wasNull() ? null : numeroDocumento);
        detalle.setApellidoPaterno(rs.getString("APELLIDO_PATERNO"));
        detalle.setApellidoMaterno(rs.getString("APELLIDO_MATERNO"));
        detalle.setNombres(rs.getString("NOMBRES"));
        int idTipoPersonal = rs.getInt("ID_TIPO_PERSONAL");
        detalle.setIdTipoPersonal(rs.wasNull() ? null : idTipoPersonal);
        detalle.setTipoPersonal(rs.getString("TIPO_PERSONAL"));
        int tecnicoActive = rs.getInt("TECNICO_ACTIVE");
        detalle.setTecnicoActive(rs.wasNull() ? null : tecnicoActive);
        long supervisorId = rs.getLong("SUPERVISOR_ID");
        detalle.setSupervisorId(rs.wasNull() ? null : supervisorId);
        return detalle;
    }

    private void validarUpdateRequest(EquipoJuridicoUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Ingrese los datos del abogado.");
        }
        if (request.getAbogadoId() == null || request.getAbogadoId() <= 0) {
            throw new IllegalArgumentException("Seleccione un abogado valido.");
        }
        if (request.getIdTecnico() == null || request.getIdTecnico() <= 0) {
            throw new IllegalArgumentException("El abogado no tiene tecnico asociado.");
        }
        if (isBlank(request.getApellidoPaterno())) {
            throw new IllegalArgumentException("Ingrese apellido paterno.");
        }
        if (isBlank(request.getNombres())) {
            throw new IllegalArgumentException("Ingrese nombres.");
        }
        if (isBlank(request.getEstado())) {
            throw new IllegalArgumentException("Seleccione estado.");
        }
    }

    private void validarSupervisorUpdateRequest(EquipoJuridicoSupervisorUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Ingrese los datos del supervisor.");
        }
        if (request.getSupervisorId() == null || request.getSupervisorId() <= 0) {
            throw new IllegalArgumentException("Seleccione un supervisor valido.");
        }
        if (request.getIdTecnico() == null || request.getIdTecnico() <= 0) {
            throw new IllegalArgumentException("El supervisor no tiene tecnico asociado.");
        }
        if (isBlank(request.getApellidoPaterno())) {
            throw new IllegalArgumentException("Ingrese apellido paterno.");
        }
        if (isBlank(request.getNombres())) {
            throw new IllegalArgumentException("Ingrese nombres.");
        }
        if (isBlank(request.getEstado())) {
            throw new IllegalArgumentException("Seleccione estado.");
        }
    }

    private void actualizarTecnico(Connection cn, EquipoJuridicoUpdateRequest request) throws SQLException {
        String sql =
            "UPDATE TECNICO " +
            "SET APELLIDO_PATERNO = ?, " +
            "APELLIDO_MATERNO = ?, " +
            "NOMBRES = ?, " +
            "NUMERO_DOCUMENTO = ?, " +
            "ID_TIPO_PERSONAL = ?, " +
            "ACTIVE = ?, " +
            "FECHA_MODIFICACION = SYSDATE, " +
            "USUARIO_MODIFICACION = ? " +
            "WHERE ID_TECNICO = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, request.getApellidoPaterno().trim());
            ps.setString(2, trimToNull(request.getApellidoMaterno()));
            ps.setString(3, request.getNombres().trim());
            if (request.getNumeroDocumento() == null) {
                ps.setNull(4, Types.NUMERIC);
            } else {
                ps.setLong(4, request.getNumeroDocumento());
            }
            if (request.getIdTipoPersonal() == null || request.getIdTipoPersonal() <= 0) {
                ps.setNull(5, Types.NUMERIC);
            } else {
                ps.setInt(5, request.getIdTipoPersonal());
            }
            ps.setInt(6, esEstadoActivo(request.getEstado()) ? 1 : 0);
            ps.setString(7, trimToNull(request.getUsuarioModificacion()));
            ps.setInt(8, request.getIdTecnico());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("No se encontro el tecnico asociado.");
            }
        }
    }

    private void actualizarUsuario(Connection cn, EquipoJuridicoUpdateRequest request) throws SQLException {
        String sql = "UPDATE APP_USERS SET STATUS = ? WHERE USER_ID = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, esEstadoActivo(request.getEstado()) ? "ACTIVE" : "INACTIVE");
            ps.setLong(2, request.getAbogadoId());
            ps.executeUpdate();
        }
    }

    private void actualizarSupervisor(Connection cn, EquipoJuridicoUpdateRequest request) throws SQLException {
        String deleteSql = "DELETE FROM APP_USER_SUPERVISION WHERE ABOGADO_ID = ?";
        try (PreparedStatement ps = cn.prepareStatement(deleteSql)) {
            ps.setLong(1, request.getAbogadoId());
            ps.executeUpdate();
        }

        if (request.getSupervisorId() == null) {
            return;
        }

        String insertSql =
            "INSERT INTO APP_USER_SUPERVISION (SUPERVISOR_ID, ABOGADO_ID) " +
            "VALUES (?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(insertSql)) {
            ps.setLong(1, request.getSupervisorId());
            ps.setLong(2, request.getAbogadoId());
            ps.executeUpdate();
        }
    }

    private boolean existeTecnico(Connection cn, Integer idTecnico) throws SQLException {
        String sql = "SELECT COUNT(1) FROM TECNICO WHERE ID_TECNICO = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idTecnico);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean tieneRol(Connection cn, Long userId, String roleName) throws SQLException {
        String sql =
            "SELECT COUNT(1) " +
            "FROM APP_USER_ROLES ur " +
            "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
            "WHERE ur.USER_ID = ? " +
            "AND UPPER(r.ROLE_NAME) = ? " +
            "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO')";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private int contarAbogadosAsignados(Connection cn, Long supervisorUserId) throws SQLException {
        String sql = "SELECT COUNT(1) FROM APP_USER_SUPERVISION WHERE SUPERVISOR_ID = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, supervisorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private boolean esEstadoActivo(String estado) {
        return "ACTIVE".equalsIgnoreCase(estado) || "ACTIVO".equalsIgnoreCase(estado);
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
