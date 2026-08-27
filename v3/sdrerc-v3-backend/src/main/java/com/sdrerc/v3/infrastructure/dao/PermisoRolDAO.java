package com.sdrerc.v3.infrastructure.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port del control de acceso por rol de V2 (tablas {@code PERMISO}/{@code ROL_PERMISO}, ver
 * `CLAUDE.md` raíz sección "Permisos"). Reemplaza en V3 al singleton {@code SessionContext} de V2
 * (que resolvía los permisos una sola vez al construir {@code MenuPrincipalV2}) — acá se calculan
 * en el momento de completar el login ({@code AuthController#completarSesion}) y viajan en el JWT/
 * respuesta de sesión, no se vuelven a consultar por request.
 *
 * <p><b>Fail-open, mismo criterio que V2</b>: si la unión de permisos de todos los roles activos
 * del usuario resulta vacía (ningún rol del usuario tiene ninguna fila en {@code ROL_PERMISO}), se
 * devuelve una lista vacía — el frontend interpreta una lista vacía como "sin restricciones
 * conocidas, mostrar todo" (mismo comportamiento que {@code SessionContext.tienePermiso} en V2:
 * "si el catálogo de permisos resuelto para la sesión está vacío, retorna true"). No es un bug, es
 * el mismo diseño deliberado de V2 para no bloquear a un usuario mientras un administrador todavía
 * no le configura permisos reales.</p>
 */
@Repository
public class PermisoRolDAO {

    private final DataSource dataSource;

    public PermisoRolDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<String> listarCodigosPermisoDeRoles(List<String> codigosRol) throws SQLException {
        if (codigosRol == null || codigosRol.isEmpty()) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT p.codigo FROM permiso p "
                        + "JOIN rol_permiso rp ON rp.id_permiso = p.id_permiso "
                        + "JOIN rol r ON r.id_rol = rp.id_rol "
                        + "WHERE rp.activo = 1 AND p.activo = 1 AND r.activo = 1 AND UPPER(r.codigo) IN (");
        for (int i = 0; i < codigosRol.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < codigosRol.size(); i++) {
                ps.setString(i + 1, codigosRol.get(i).trim().toUpperCase(java.util.Locale.ROOT));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<String> codigos = new ArrayList<>();
                while (rs.next()) {
                    codigos.add(rs.getString("codigo"));
                }
                return codigos;
            }
        }
    }
}
