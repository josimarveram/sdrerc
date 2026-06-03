package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.UsuarioDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class UsuarioValidacionService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    public List<String> validarGuardar(UsuarioDTO usuario, List<Long> idsRoles) {
        List<String> errores = new ArrayList<>();
        if (usuario == null) {
            errores.add("Ingrese los datos del usuario.");
            return errores;
        }
        if (!hasText(usuario.getUsername())) {
            errores.add("Ingrese el usuario.");
        } else if (!usuario.getUsername().matches("[A-Z0-9._-]+")) {
            errores.add("El usuario solo debe usar letras, números, punto, guion o guion bajo.");
        } else if (usuario.getUsername().length() > 60) {
            errores.add("El usuario no debe superar 60 caracteres.");
        }

        if (!hasText(usuario.getNombres())) {
            errores.add("Ingrese los nombres.");
        }
        if (!hasText(usuario.getNombreCompleto())) {
            errores.add("Ingrese el nombre completo del usuario.");
        } else if (usuario.getNombreCompleto().length() > 250) {
            errores.add("El nombre completo no debe superar 250 caracteres.");
        }

        if (hasText(usuario.getCorreo())) {
            if (usuario.getCorreo().length() > 250) {
                errores.add("El correo no debe superar 250 caracteres.");
            } else if (!EMAIL_PATTERN.matcher(usuario.getCorreo()).matches()) {
                errores.add("Ingrese un correo válido.");
            }
        }

        if (hasText(usuario.getTipoDocumento()) && usuario.getTipoDocumento().length() > 20) {
            errores.add("El tipo de documento no debe superar 20 caracteres.");
        }
        if (hasText(usuario.getNumeroDocumento()) && usuario.getNumeroDocumento().length() > 20) {
            errores.add("El número de documento no debe superar 20 caracteres.");
        }
        if (idsRoles == null || idsRoles.isEmpty()) {
            errores.add("Seleccione al menos un rol.");
        }
        return errores;
    }

    public void prepararUsuario(UsuarioDTO usuario) {
        if (usuario == null) {
            return;
        }
        usuario.setUsername(normalizarLogin(usuario.getUsername()));
        usuario.setNombres(trim(usuario.getNombres()));
        usuario.setApellidos(trim(usuario.getApellidos()));
        usuario.setTipoDocumento(normalizarCodigoCorto(usuario.getTipoDocumento()));
        usuario.setNumeroDocumento(trim(usuario.getNumeroDocumento()));
        usuario.setCorreo(normalizarCorreo(usuario.getCorreo()));
        usuario.setNombreCompleto(construirNombreCompleto(usuario.getNombres(), usuario.getApellidos()));
        usuario.setEstado(usuario.isActivo() ? "ACTIVO" : "INACTIVO");
    }

    private String construirNombreCompleto(String nombres, String apellidos) {
        String n = trim(nombres);
        String a = trim(apellidos);
        if (!hasText(a)) {
            return n;
        }
        return (n + " " + a).trim();
    }

    private String normalizarLogin(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarCodigoCorto(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarCorreo(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
