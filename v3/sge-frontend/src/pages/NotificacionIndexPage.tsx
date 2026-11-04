import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { NOTIFICACION_TABS, primerTabPermitido } from '../layout/moduleTabs'

/**
 * Destino real del ítem de sidebar "Notificación" (22/08/2026, pedido explícito del usuario) — antes
 * apuntaba fijo a `/notificacion/asignacion` en `navConfig.tsx`, así que un rol restringido a una
 * sola bandeja (ej. VALIDACION) siempre aterrizaba en Bandeja Asignación aunque esa pestaña ni
 * siquiera le apareciera visible en el header (`PageHeaderSticky` ya filtra `tabs` por permiso desde
 * el 21/08/2026), mostrando el listado equivocado bajo el único tab que sí veía. Esta página no
 * renderiza nada por sí misma: solo redirige a la primera bandeja de `NOTIFICACION_TABS` que el
 * catálogo de permisos de la sesión habilita (`primerTabPermitido`, mismo criterio de filtro/fail-open
 * que ya usa `PageHeaderSticky`).
 */
export function NotificacionIndexPage() {
  const { session } = useAuth()
  const destino = primerTabPermitido(NOTIFICACION_TABS, session?.permisos ?? [])
  return <Navigate to={destino} replace />
}
