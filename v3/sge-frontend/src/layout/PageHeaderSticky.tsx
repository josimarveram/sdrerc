import type { ReactNode } from 'react'
import { Box, IconButton, Tab, Tabs, Tooltip, Typography } from '@mui/material'
import NotificationsOutlinedIcon from '@mui/icons-material/NotificationsOutlined'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { sdrercColors } from '../theme'
import { TOPBAR_HEIGHT } from './layoutConstants'

export interface PageHeaderTab {
  label: string
  path: string
  /** Contador opcional junto al label (ej. cantidad de registros de esa bandeja); sin uso real todavía en ningún módulo. */
  badge?: number | string
  /**
   * Código PERMISO de bandeja (BANDEJA_*, ver tabla `PERMISO`/`ROL_PERMISO` — mismo catálogo que
   * el `requiredPermiso` de `NavItem` en `navConfig.tsx`, pero a nivel de pestaña dentro de un
   * módulo en vez de a nivel de módulo completo). `undefined` = pestaña siempre visible (mismo
   * criterio ya usado por los módulos que no necesitan este 2° nivel de permiso). 21/08/2026,
   * pedido explícito del usuario tras detectar que un rol restringido a una sola bandeja de
   * Notificación (ej. SUPERVISOR_NOTIFICACION) veía las 4 pestañas igual — el catálogo de permisos
   * de BD ya tenía todo bien configurado (`BANDEJA_NOTIFICACION_*` en `ROL_PERMISO`), el gap era
   * 100% de frontend: nada filtraba `tabs` por permiso, a diferencia de `Sidebar.tsx`/`NavItem`
   * (que sí lo hacía a nivel de módulo desde el incremento de permisos del 21/08/2026).
   */
  requiredPermiso?: string
}

export interface PageHeaderStickyProps {
  /** Título de la página — ej. "Bandeja Registro". Solo se muestra en Fila 1 cuando NO hay `tabs` (con tabs, el tab activo ya identifica la página); se conserva siempre como prop (accesibilidad/futuro `document.title`), no se volvió opcional. */
  titulo: string
  /** Subtítulo descriptivo (Fila 2, chico y gris) — ej. "Expedientes en etapa de Registro / Recepción". */
  subtitulo?: string
  /** Botones de acción primaria del módulo (Fila 1, derecha, antes de la campana). */
  acciones?: ReactNode
  /** Pestañas de navegación entre las bandejas/vistas del módulo (Fila 1, ocupa el lugar del título cuando existen). Se omite si no se pasa. */
  tabs?: PageHeaderTab[]
  /** Buscador/filtros propios de la bandeja (Fila 3: texto, fechas, estado, límite, Buscar/Limpiar). Se omite si no se pasa. */
  filtros?: ReactNode
  /** Cuerpo de la página (alertas, KPIs, grilla) — NO es sticky, se desplaza normalmente debajo del bloque fijo. */
  children: ReactNode
}

/**
 * Header de página reutilizable para TODA bandeja del sistema — un solo componente, no una
 * reimplementación por módulo (pedido explícito del usuario 13/08/2026).
 *
 * **Fila 1 = tabs, no título (13/08/2026, tercera pasada, pedido explícito del usuario)**: el
 * título grande ("Bandeja Registro") quedaba redundante con el tab activo del mismo nombre dentro
 * de la fila de pestañas — "el tab activo... cumple esa función de identificar la página". Fix:
 * cuando el módulo tiene `tabs` (los 6 módulos ya portados los tienen), la Fila 1 muestra las
 * pestañas en el lugar donde antes iba el título, con acciones+campana igual que antes a la
 * derecha; `titulo` queda como fallback SOLO para un eventual módulo futuro sin `tabs` (no hay
 * ninguno así todavía, los 6 consumidores actuales siempre pasan `tabs`) — se mantiene el prop
 * requerido (no opcional) para no romper la firma ni forzar un chequeo de `undefined` en cada
 * página que ya lo pasa. El subtítulo (Fila 2) ya no comparte fila con nada: quedó solo.
 *
 * **Fusión con la barra superior de la app (13/08/2026, segunda pasada)**: la primera versión de
 * este componente vivía sticky DEBAJO de un `TopBar` fijo separado — el usuario reportó esa franja
 * como espacio muerto. Fix: `TopBar.tsx` se eliminó por completo; este componente absorbe esa
 * franja como su propia Fila 1, sticky a `top: 0`, alineada en altura con la franja del logo del
 * `Sidebar` (misma `TOPBAR_HEIGHT`).
 *
 * 3 filas, todas dentro del mismo bloque sticky (`position: sticky; top: 0`), + cuerpo con scroll normal:
 * 1. Alto fijo `TOPBAR_HEIGHT`: tabs del módulo (o título, si no hay tabs) a la izquierda / acciones
 *    primarias + campana de notificaciones a la derecha.
 * 2. Subtítulo (chico, gris), sin nada más en la fila.
 * 3. Buscador/filtros propios de la bandeja (opcional — `filtros`).
 *
 * Implementación técnica: `position: sticky` (NO un contenedor flex anidado con `height`/`overflow`
 * manual) — decisión deliberada. `AppLayout` no fija una altura para su área de contenido (el
 * documento/ventana es el scroll real), así que envolver esto en un `overflow-y:auto` propio habría
 * exigido convertir TODO el árbol de `<main>` a un modelo de altura fija en cascada, con riesgo real
 * de recortar contenido en las páginas que NO usan este componente (formularios, Dashboard, Home).
 *
 * `mx: -4; mt: -4` cancela exactamente el `p: 4` que `AppLayout` aplica a su `<Outlet/>`, para que
 * el bloque ocupe todo el ancho de `<main>` desde `y:0`; adentro se reintroduce `px: 4` para alinear
 * el contenido con el resto de la página. `zIndex: theme.zIndex.drawer + 1` para seguir pintando por
 * encima del cuerpo (KPIs/grilla) al scrollear.
 *
 * `acciones` es una prop normal (no Context/efectos) — ver historial de esta sesión: el mecanismo
 * anterior (`TopBarActionsContext`, ya retirado) causó un bucle de actualización infinito real.
 */
export function PageHeaderSticky({ titulo, subtitulo, acciones, tabs, filtros, children }: PageHeaderStickyProps) {
  const location = useLocation()
  const navigate = useNavigate()
  const { session } = useAuth()
  // Mismo criterio fail-open que `Sidebar.tsx` (`NavItem.requiredPermiso`): lista de permisos vacía
  // = sin restricciones conocidas, se muestran todas las pestañas. `session` puede ser `null` en el
  // instante entre desloguearse y la redirección a /login (ProtectedRoute); `permisos` cae a `[]`
  // (fail-open) en vez de reventar, mismo criterio que el resto del archivo.
  const permisos = session?.permisos ?? []
  const tabsVisibles = (tabs ?? []).filter(
    (t) => !t.requiredPermiso || permisos.length === 0 || permisos.includes(t.requiredPermiso),
  )
  const hayTabs = tabsVisibles.length > 0
  const tabActivo = tabsVisibles.find((t) => t.path === location.pathname)?.path ?? false

  return (
    <Box>
      <Box
        sx={{
          position: 'sticky',
          top: 0,
          zIndex: (t) => t.zIndex.drawer + 1,
          mx: -4,
          mt: -4,
          mb: 3,
          px: 4,
          bgcolor: sdrercColors.backgroundPaper,
          boxShadow: '0 1px 6px rgba(15, 23, 42, 0.05)',
        }}
      >
        {/* Fila 1: misma altura que la franja del logo del Sidebar — tabs (o título) / acciones + notificaciones */}
        <Box sx={{ height: TOPBAR_HEIGHT, minHeight: TOPBAR_HEIGHT, display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2 }}>
          {hayTabs ? (
            <Tabs
              value={tabActivo}
              onChange={(_, value: string) => navigate(value)}
              variant="scrollable"
              scrollButtons="auto"
              sx={{
                minWidth: 0,
                minHeight: TOPBAR_HEIGHT,
                '& .MuiTabs-indicator': { backgroundColor: sdrercColors.navSidebarActiveBar, height: 2.5 },
                '& .MuiTab-root': {
                  textTransform: 'none',
                  minHeight: TOPBAR_HEIGHT,
                  fontWeight: 500,
                  fontSize: '0.8125rem',
                  color: sdrercColors.textSecondary,
                },
                '& .Mui-selected': { color: `${sdrercColors.navSidebarActiveBar} !important`, fontWeight: 600 },
              }}
            >
              {tabsVisibles.map((t) => (
                <Tab
                  key={t.path}
                  value={t.path}
                  label={
                    t.badge === undefined ? (
                      t.label
                    ) : (
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                        {t.label}
                        <Box
                          sx={{
                            minWidth: 18,
                            height: 18,
                            px: 0.5,
                            borderRadius: '9px',
                            bgcolor: sdrercColors.softBlue,
                            color: sdrercColors.primary,
                            fontSize: '0.65rem',
                            fontWeight: 600,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                          }}
                        >
                          {t.badge}
                        </Box>
                      </Box>
                    )
                  }
                />
              ))}
            </Tabs>
          ) : (
            <Typography variant="h6" sx={{ color: sdrercColors.primary, minWidth: 0 }} noWrap>
              {titulo}
            </Typography>
          )}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexShrink: 0 }}>
            {acciones}
            <Tooltip title="Notificaciones">
              <IconButton size="small" sx={{ color: sdrercColors.textSecondary }}>
                <NotificationsOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Box>
        </Box>

        {/* Fila 2: subtítulo, sola */}
        {subtitulo && (
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{ pt: 1, pb: 1.25, borderTop: `1px solid ${sdrercColors.divider}` }}
            noWrap
          >
            {subtitulo}
          </Typography>
        )}

        {/* Fila 3: buscador/filtros propios de la bandeja */}
        {filtros && (
          <Box sx={{ py: 1.25, borderTop: subtitulo ? 'none' : `1px solid ${sdrercColors.divider}` }}>{filtros}</Box>
        )}
      </Box>

      {children}
    </Box>
  )
}
