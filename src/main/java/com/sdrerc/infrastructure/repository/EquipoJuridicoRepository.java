package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.EquipoJuridicoConsultaItem;
import com.sdrerc.domain.model.EquipoJuridicoResumen;
import com.sdrerc.domain.model.PaginatedResult;
import com.sdrerc.domain.model.SupervisorComboItem;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                    ? "AND UPPER(u.STATUS) IN ('ACTIVE', 'ACTIVO')"
                    : "AND UPPER(u.STATUS) IN ('INACTIVE', 'INACTIVO')");

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private EquipoJuridicoConsultaItem mapConsultaItem(ResultSet rs) throws SQLException {
        EquipoJuridicoConsultaItem item = new EquipoJuridicoConsultaItem();
        item.setAbogadoId(rs.getLong("ABOGADO_ID"));
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
}
