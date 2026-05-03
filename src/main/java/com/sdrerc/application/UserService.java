/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Role;
import com.sdrerc.domain.model.PaginatedResult;
import com.sdrerc.domain.model.User;
import com.sdrerc.domain.model.UsuarioListadoItem;
import com.sdrerc.infrastructure.repository.UserRepository;
import com.sdrerc.infrastructure.security.PasswordEncoder;
import com.sdrerc.infrastructure.security.PasswordPolicy;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author David
 */
public class UserService {
    private static final String MENSAJE_USUARIO_OPERATIVO_SIN_TECNICO =
            "Los usuarios con rol ABOGADO o SUPERVISION deben registrarse desde Equipo Jurídico "
            + "para crear automáticamente la persona operativa y su vínculo técnico.";

    private final UserRepository repo;

    
    public UserService() {
        this.repo = new UserRepository();
    }

    public void registrar(User role) throws SQLException {
        if (role.getUsername()== null || role.getUsername().isEmpty()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio");
        }
        repo.save(role);
    }

    public void actualizar(User role) throws SQLException {
        repo.update(role);
    }

    public List<User> listar() throws SQLException {
        return repo.findAll();
    }
    
    public List<User> buscar(String nombre, String estado) throws SQLException{
        return repo.buscar(nombre, estado);
    }

    public PaginatedResult<UsuarioListadoItem> buscarPaginado(
            String filtro, String estado, int page, int pageSize) throws SQLException {
        return repo.buscarPaginado(filtro, estado, page, pageSize);
    }

    public void cambiarEstado(Long id, String estado) throws SQLException {
        repo.cambiarEstado(id, estado);
    }

    public void vincularTecnico(Long userId, Integer idTecnico) throws SQLException {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Seleccione un usuario.");
        }
        if (idTecnico == null || idTecnico <= 0) {
            throw new IllegalArgumentException("Seleccione un técnico válido.");
        }

        Long idTecnicoActual = repo.obtenerIdTecnicoUsuario(userId);
        if (idTecnicoActual != null && idTecnicoActual.intValue() == idTecnico.intValue()) {
            throw new IllegalStateException("El usuario ya tiene vinculado este técnico.");
        }

        if (repo.existeTecnicoVinculadoAOtroUsuario(userId, idTecnico)) {
            throw new IllegalStateException("Este técnico ya está vinculado a otro usuario.");
        }

        try {
            repo.vincularTecnico(userId, idTecnico);
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1) {
                throw new IllegalStateException("Este técnico ya está vinculado a otro usuario.", ex);
            }
            throw ex;
        }
    }

    public void cambiarPassword(Long userId, String hash)
            throws SQLException {

        repo.actualizarPassword(userId, hash);
    }

    public void resetPasswordTemporal(Long userId, String username, String temporaryPassword)
            throws SQLException {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Seleccione un usuario válido.");
        }
        PasswordPolicy.validateTemporaryPassword(username, temporaryPassword);
        String hash = PasswordEncoder.hash(temporaryPassword);
        repo.resetPasswordTemporal(userId, hash, obtenerUsuarioReset());
    }

    public void cambiarPasswordObligatorio(Long userId,
            String username,
            String currentPassword,
            String newPassword,
            String confirmPassword) throws SQLException {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Seleccione un usuario válido.");
        }
        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new IllegalArgumentException("Ingrese la contraseña actual.");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("Ingrese la nueva contraseña.");
        }
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }

        String hashActual = repo.obtenerPasswordHash(userId);
        if (hashActual == null || hashActual.trim().isEmpty()
                || !passwordMatches(currentPassword, hashActual)) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (passwordMatches(newPassword, hashActual)) {
            throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la contraseña actual.");
        }

        PasswordPolicy.validateTemporaryPassword(username, newPassword);
        String nuevoHash = PasswordEncoder.hash(newPassword);
        repo.actualizarPasswordObligatorio(userId, nuevoHash);
    }

    private boolean passwordMatches(String password, String hash) {
        try {
            return PasswordEncoder.matches(password, hash);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String obtenerUsuarioReset() {
        try {
            String username = SessionContext.getUsername();
            String resetBy = username == null || username.trim().isEmpty() ? "APP" : username.trim();
            return resetBy.length() > 50 ? resetBy.substring(0, 50) : resetBy;
        } catch (Exception ex) {
            return "APP";
        }
    }
    
    public List<Role> obtenerRolesUsuario(Long userId) throws SQLException {
        return repo.listarRolesPorUsuario(userId);
    }
    
    public void asignarRoles(Long userId, List<Long> roles)
            throws SQLException {
        validarRolesOperativosConTecnico(userId, roles);
        repo.asignarRoles(userId, roles);
    }

    private void validarRolesOperativosConTecnico(Long userId, List<Long> roleIds)
            throws SQLException {
        if (userId == null || userId <= 0 || roleIds == null || roleIds.isEmpty()) {
            return;
        }

        if (!incluyeRolOperativoJuridico(roleIds)) {
            return;
        }

        Long idTecnico = repo.obtenerIdTecnicoUsuario(userId);
        if (idTecnico == null) {
            throw new IllegalStateException(MENSAJE_USUARIO_OPERATIVO_SIN_TECNICO);
        }
    }

    private boolean incluyeRolOperativoJuridico(List<Long> roleIds)
            throws SQLException {
        List<String> roleNames = repo.obtenerRoleNamesPorIds(roleIds);
        for (String roleName : roleNames) {
            String normalized = roleName == null ? "" : roleName.trim().toUpperCase();
            if ("ABOGADO".equals(normalized) || "SUPERVISION".equals(normalized)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean tieneRol(Long userId, String roleName)
            throws SQLException {
        return repo.tieneRol(userId, roleName);
    }
    
    public List<User> listarPorRol(String roleName)
            throws SQLException {
        return repo.listarPorRol(roleName);
    }
}
