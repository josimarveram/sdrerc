package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.EquipoJuridicoDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EquipoJuridicoValidacionService {

    public void prepararEquipo(EquipoJuridicoDTO equipo) {
        if (equipo == null) {
            return;
        }
        equipo.setCodigo(normalizarCodigo(equipo.getCodigo()));
        equipo.setNombre(trimToNull(equipo.getNombre()));
        equipo.setDescripcion(trimToNull(equipo.getDescripcion()));
    }

    public List<String> validarGuardar(EquipoJuridicoDTO equipo) {
        List<String> errores = new ArrayList<>();
        if (equipo == null) {
            errores.add("Complete los datos del equipo.");
            return errores;
        }
        if (!hasText(equipo.getCodigo())) {
            errores.add("Ingrese el código del equipo.");
        } else if (!equipo.getCodigo().matches("[A-Z0-9_]+")) {
            errores.add("El código del equipo debe usar mayúsculas, números y guion bajo, sin espacios.");
        } else if (equipo.getCodigo().length() > 40) {
            errores.add("El código del equipo no debe superar 40 caracteres.");
        }
        if (!hasText(equipo.getNombre())) {
            errores.add("Ingrese el nombre del equipo.");
        } else if (equipo.getNombre().length() > 150) {
            errores.add("El nombre del equipo no debe superar 150 caracteres.");
        }
        if (hasText(equipo.getDescripcion()) && equipo.getDescripcion().length() > 500) {
            errores.add("La descripción del equipo no debe superar 500 caracteres.");
        }
        return errores;
    }

    private static String normalizarCodigo(String value) {
        if (value == null) {
            return null;
        }
        return value.trim()
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
