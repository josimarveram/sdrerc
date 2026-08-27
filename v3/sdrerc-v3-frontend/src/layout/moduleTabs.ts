import type { PageHeaderTab } from './PageHeaderSticky'

/**
 * Pestañas de bandeja por módulo, consumidas por `PageHeaderSticky` (Fila 2). Nombres tomados
 * literalmente de las "Pestañas superiores" documentadas en el `CLAUDE.md` raíz (V2) — no son
 * genéricos/inventados. Cada módulo nuevo que se porte a V3 agrega acá su propio arreglo.
 */

/**
 * "Bandeja de Expedientes" (V2: `JPanelBandejaExpedientesNueva`, perfil general — etapa libre, no
 * confundir con `REGISTRO_TABS`, cuya "Bandeja Registro" es la misma clase pero con etapa fija a
 * REGISTRO). Una sola pestaña de nivel superior, mismo criterio que Análisis/Verificación/Ejecución.
 */
export const BANDEJA_TABS: PageHeaderTab[] = [{ label: 'Bandeja de Expedientes', path: '/bandeja' }]

/** V2, sección "Registro / Recepcion": "Bandeja Registro", "Carga diaria", "Registro manual". */
export const REGISTRO_TABS: PageHeaderTab[] = [
  { label: 'Bandeja Registro', path: '/registro' },
  { label: 'Carga diaria', path: '/registro/carga-diaria' },
  { label: 'Registro manual', path: '/registro/manual' },
]

/** V2, sección "Asignacion": "Bandeja Asignacion", "Cartas de respuesta", "Carga Abogados". */
export const ASIGNACION_TABS: PageHeaderTab[] = [
  { label: 'Bandeja Asignación', path: '/asignacion' },
  { label: 'Cartas de respuesta', path: '/asignacion/cartas-respuesta' },
  { label: 'Carga Abogados', path: '/asignacion/carga-abogados' },
]

/**
 * V2, "Análisis" (JPanelAnalisisV2, `tabsAnalisis`): una sola pestaña de nivel superior real,
 * "Bandeja Análisis" — a diferencia de Registro/Asignación, Análisis no tiene sub-bandejas propias
 * (Carga diaria/Cartas de respuesta/etc.), solo el panel derecho con lengüetas Datos/Análisis, que
 * en V3 vive en el 2° nivel del panel multipestañas (`WorkspaceTabsDrawer`), no acá.
 */
export const ANALISIS_TABS: PageHeaderTab[] = [{ label: 'Bandeja Análisis', path: '/analisis' }]

/** V2, "Verificación" (JPanelVerificacionV2): una sola pestaña de nivel superior real, "Bandeja Verificación" — mismo criterio que Análisis, sin sub-bandejas propias. */
export const VERIFICACION_TABS: PageHeaderTab[] = [{ label: 'Bandeja Verificación', path: '/verificacion' }]

/** V2, "Ejecución" (JPanelEjecucionV2): una sola pestaña de nivel superior real, "Bandeja Ejecución" — mismo criterio que Análisis/Verificación, sin sub-bandejas propias. */
export const EJECUCION_TABS: PageHeaderTab[] = [{ label: 'Bandeja Ejecución', path: '/ejecucion' }]

/**
 * V2, "Notificación" (JPanelNotificacionV2): 4 pestañas superiores reales. 21/08/2026, primera
 * fase del módulo (pedido explícito del usuario) — solo "Bandeja Asignación" tiene página
 * construida; las otras 3 quedan con nombre/ruta ya confirmados (no placeholders, a diferencia de
 * Publicación/Expediente digital abajo) para las fases siguientes del mismo módulo. "Cierre" (V2:
 * lengüeta interna del panel lateral de "Bandeja Notificación", no pestaña de nivel superior) está
 * EXCLUIDA del alcance de V3 por decisión de producto — no agregar.
 *
 * `requiredPermiso` (21/08/2026, mismo día, pedido explícito del usuario): códigos `BANDEJA_*`
 * reales de V2, ya sembrados en `PERMISO`/`ROL_PERMISO` por el script `60_catalogo_permisos_bandejas.sql`
 * (ejecutado) y asignados por rol en `65_matriz_permisos_roles_operativos.sql` (también ejecutado,
 * verificado contra Oracle real 21/08/2026 — el catálogo de BD ya estaba correcto, el gap era 100%
 * de frontend: `PageHeaderSticky` no filtraba `tabs` por permiso). `BANDEJA_NOTIFICACION_PUBLICACION`
 * NO existe todavía en el catálogo (V2 nunca lo sembró — ver `CLAUDE.md` raíz, sección Notificación,
 * "Pendiente... esta pestaña no tiene código de permiso propio sembrado") — hasta que se cree y
 * ejecute un script nuevo, esa pestaña queda oculta para cualquier usuario con permisos restringidos
 * (fail-open: solo un usuario con `permisos=[]` la vería, nunca uno con una lista real que no la
 * incluya).
 */
export const NOTIFICACION_TABS: PageHeaderTab[] = [
  { label: 'Bandeja Asignación', path: '/notificacion/asignacion', requiredPermiso: 'BANDEJA_NOTIFICACION_ASIGNACION' },
  { label: 'Bandeja Validación', path: '/notificacion/validacion', requiredPermiso: 'BANDEJA_NOTIFICACION_VALIDACION' },
  { label: 'Bandeja Notificación', path: '/notificacion/notificacion', requiredPermiso: 'BANDEJA_NOTIFICACION_NOTIFICACION' },
  { label: 'Bandeja de publicación', path: '/notificacion/publicacion', requiredPermiso: 'BANDEJA_NOTIFICACION_PUBLICACION' },
]

/**
 * Resuelve a qué bandeja debe aterrizar un usuario al entrar al módulo (22/08/2026, pedido explícito
 * del usuario — un rol restringido a una sola bandeja, ej. VALIDACION, entraba siempre a "Bandeja
 * Asignación" — ruta fija en `navConfig.tsx` — aunque esa pestaña ni siquiera le apareciera visible
 * en el header, mostrando el listado equivocado bajo el único tab que sí veía). Mismo criterio de
 * filtro/fail-open que ya usa `PageHeaderSticky` (lista de permisos vacía = sin restricciones
 * conocidas, se ve todo): el primer tab de `tabs` que pasa ese filtro gana. Si el catálogo de
 * permisos de la sesión no habilita NINGUNA pestaña real (caso límite no esperado hoy — cada rol
 * operativo de Notificación tiene al menos una bandeja propia en `65_matriz_permisos_roles_operativos.sql`),
 * cae al primer tab de la lista de todos modos en vez de construir una ruta rota.
 */
export function primerTabPermitido(tabs: PageHeaderTab[], permisos: string[]): string {
  const visibles = tabs.filter((t) => !t.requiredPermiso || permisos.length === 0 || permisos.includes(t.requiredPermiso))
  return (visibles[0] ?? tabs[0]).path
}

/**
 * Placeholders de una sola pestaña para módulos "Próximamente" (sin página construida todavía,
 * `path` no está ruteado en `App.tsx` — agregar la ruta real cuando se construya la página).
 * Confirmado explícitamente por el usuario 13/08/2026 ("Un solo tab 'Bandeja' cada uno") ante la
 * falta de una estructura de pestañas documentada en V2 para estos 2 módulos: en V2, "Publicacion"
 * era la 4ª pestaña interna de Notificación (no un módulo standalone con pestañas propias) y
 * "Expediente digital" nunca tuvo una bandeja con pestañas documentada (solo "consultar metadata...
 * cuando exista"). Ajustar el nombre/estructura real cuando se aborde cada módulo.
 */
export const PUBLICACION_TABS: PageHeaderTab[] = [{ label: 'Bandeja Publicación', path: '/publicacion' }]

export const EXPEDIENTE_DIGITAL_TABS: PageHeaderTab[] = [{ label: 'Expediente Digital', path: '/expediente-digital' }]

/**
 * V2, "Administracion" (JPanelUsuariosV2/JPanelRolesV2/JPanelEquipoJuridicoV2): 3 módulos
 * separados, no sub-pestañas de uno solo (ver plan de Administración,
 * `necesito-un-plan-de-cuddly-wombat.md`) — cada uno con una sola pestaña de nivel superior real,
 * mismo criterio que Análisis/Verificación/Ejecución. Equipo Jurídico gana una 2ª pestaña
 * ("Personal por supervisor") cuando se construya ese incremento.
 */
export const ADMIN_USUARIOS_TABS: PageHeaderTab[] = [{ label: 'Usuarios', path: '/admin/usuarios' }]
export const ADMIN_ROLES_TABS: PageHeaderTab[] = [{ label: 'Roles', path: '/admin/roles' }]
export const ADMIN_EQUIPO_JURIDICO_TABS: PageHeaderTab[] = [
  { label: 'Equipos', path: '/admin/equipo-juridico' },
  { label: 'Personal por supervisor', path: '/admin/equipo-juridico/supervisor' },
]
export const ADMIN_PLANTILLAS_TABS: PageHeaderTab[] = [{ label: 'Plantillas de documento', path: '/admin/plantillas' }]
export const ADMIN_FERIADOS_TABS: PageHeaderTab[] = [{ label: 'Feriados', path: '/admin/feriados' }]
export const ADMIN_PLAZOS_TABS: PageHeaderTab[] = [{ label: 'Plazos', path: '/admin/plazos' }]
