package com.sdrerc.infrastructure.sdrercapp.dao;

import java.util.List;

/**
 * Arma la condicion SQL de visibilidad por asignacion usada por las bandejas operativas:
 * ADMIN_SISTEMA ve todo (sin condicion); el resto de usuarios solo ve expedientes/documentos
 * cuyo responsable actual (usuario o equipo) coincide con su propio acceso. Si no se pudo
 * resolver ni usuario ni equipo del actor, se deniega por defecto (falla cerrado) en vez de
 * mostrar todo.
 */
final class VisibilidadBandejaSql {

    private VisibilidadBandejaSql() {
    }

    static String construirCondicion(
            List<Object> params,
            boolean esAdmin,
            Long idUsuarioActual,
            List<Long> idsEquipoActual,
            String columnaUsuario,
            String columnaEquipo) {
        return construirCondicion(params, esAdmin, idUsuarioActual, idsEquipoActual, columnaUsuario, columnaEquipo, false);
    }

    /**
     * Igual que la sobrecarga de 6 parametros, pero con la opcion de incluir tambien (via OR)
     * los expedientes cuyo responsable actual (columnaUsuario) es un abogado supervisado por
     * el actor segun USUARIO_SUPERVISION. Pensado para bandejas donde quien filtra no es el
     * mismo abogado responsable del expediente, sino su supervisor (ej. Verificacion): el
     * responsable actual no cambia al entrar a Verificacion (sigue siendo el abogado de
     * Analisis), asi que sin esta condicion adicional el supervisor no veria nada por usuario
     * ni por equipo.
     */
    static String construirCondicion(
            List<Object> params,
            boolean esAdmin,
            Long idUsuarioActual,
            List<Long> idsEquipoActual,
            String columnaUsuario,
            String columnaEquipo,
            boolean incluirAbogadosSupervisados) {
        if (esAdmin) {
            return "";
        }
        StringBuilder condiciones = new StringBuilder();
        if (idUsuarioActual != null) {
            condiciones.append(columnaUsuario).append(" = ?");
            params.add(idUsuarioActual);
        }
        if (idsEquipoActual != null && !idsEquipoActual.isEmpty()) {
            if (condiciones.length() > 0) {
                condiciones.append(" OR ");
            }
            condiciones.append(columnaEquipo).append(" IN (");
            for (int i = 0; i < idsEquipoActual.size(); i++) {
                if (i > 0) {
                    condiciones.append(", ");
                }
                condiciones.append("?");
                params.add(idsEquipoActual.get(i));
            }
            condiciones.append(")");
        }
        if (incluirAbogadosSupervisados && idUsuarioActual != null) {
            if (condiciones.length() > 0) {
                condiciones.append(" OR ");
            }
            condiciones.append("EXISTS (SELECT 1 FROM usuario_supervision usv ")
                    .append("WHERE usv.id_supervisor = ? AND usv.id_abogado = ")
                    .append(columnaUsuario).append(" AND usv.activo = 1)");
            params.add(idUsuarioActual);
        }
        if (condiciones.length() == 0) {
            return "AND 1 = 0 ";
        }
        return "AND (" + condiciones + ") ";
    }
}
