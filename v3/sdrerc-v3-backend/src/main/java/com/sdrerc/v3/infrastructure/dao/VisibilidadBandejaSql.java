package com.sdrerc.v3.infrastructure.dao;

import java.util.List;

/**
 * Port literal de com.sdrerc.infrastructure.sdrercapp.dao.VisibilidadBandejaSql (V2) — primer
 * consumidor real en V3 (20/08/2026, módulo Análisis): arma la condición SQL de visibilidad por
 * asignación usada por las bandejas operativas que SÍ filtran por fila (Análisis, Verificación,
 * Ejecución, Notificación — ver `CLAUDE.md` raíz, sección "Visibilidad por asignación dentro de
 * una bandeja"; Registro/Asignación quedan explícitamente excluidas de este filtro, son pantallas
 * de coordinación/despacho que deben ver toda la cola).
 *
 * <p>ADMIN_SISTEMA ve todo (sin condición); el resto de usuarios solo ve expedientes/documentos
 * cuyo responsable actual coincide con su propio acceso. Prioridad: si el expediente/documento ya
 * tiene un responsable individual (columnaUsuario no nulo), solo ese usuario (o su supervisor, ver
 * incluirAbogadosSupervisados) lo ve; el match por equipo es únicamente un fallback para el caso en
 * que todavía no se asignó a una persona específica (columnaUsuario nulo, responsabilidad solo de
 * equipo), para no dejar sin visibilidad el trabajo aún no reclamado por nadie. Si no se pudo
 * resolver ni usuario ni equipo del actor, se deniega por defecto (falla cerrado) en vez de mostrar
 * todo.
 *
 * <p>Único cambio real respecto a V2: {@code esAdmin}/{@code idUsuarioActual}/
 * {@code idsEquipoActual} se resuelven en V3 desde {@code CurrentUser.get()} (JWT), no desde
 * {@code UsuarioAsignacionService.resolverUsuarioActualSdrercApp()} contra el username de sesión —
 * el JWT ya trae el {@code idUsuario} directo, sin necesidad de esa consulta intermedia.
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
     * Igual que la sobrecarga de 6 parámetros, pero con la opción de incluir también (vía OR) los
     * expedientes cuyo responsable actual (columnaUsuario) es un abogado supervisado por el actor
     * según USUARIO_SUPERVISION. Pensado para bandejas donde quien filtra no es el mismo abogado
     * responsable del expediente, sino su supervisor (ej. Verificación): el responsable actual no
     * cambia al entrar a Verificación (sigue siendo el abogado de Análisis), así que sin esta
     * condición adicional el supervisor no vería nada por usuario ni por equipo.
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
            condiciones.append("(").append(columnaUsuario).append(" IS NULL AND ").append(columnaEquipo).append(" IN (");
            for (int i = 0; i < idsEquipoActual.size(); i++) {
                if (i > 0) {
                    condiciones.append(", ");
                }
                condiciones.append("?");
                params.add(idsEquipoActual.get(i));
            }
            condiciones.append("))");
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
