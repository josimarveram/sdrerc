package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.RolDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RolValidacionService {

    public List<String> validarGuardar(RolDTO rol) {
        List<String> errores = new ArrayList<>();
        if (rol == null) {
            errores.add("Ingrese los datos del rol.");
            return errores;
        }
        if (!hasText(rol.getCodigo())) {
            errores.add("Ingrese el código del rol.");
        } else if (!rol.getCodigo().matches("[A-Z0-9_]+")) {
            errores.add("El código debe usar mayúsculas, números y guion bajo, sin espacios.");
        } else if (rol.getCodigo().length() > 40) {
            errores.add("El código del rol no debe superar 40 caracteres.");
        }

        if (!hasText(rol.getNombre())) {
            errores.add("Ingrese el nombre del rol.");
        } else if (rol.getNombre().length() > 150) {
            errores.add("El nombre del rol no debe superar 150 caracteres.");
        }

        if (rol.getDescripcion() != null && rol.getDescripcion().length() > 500) {
            errores.add("La descripción no debe superar 500 caracteres.");
        }
        return errores;
    }

    public String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return "";
        }
        return codigo.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", "_");
    }

    public String normalizarTexto(String value) {
        return value == null ? null : value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
