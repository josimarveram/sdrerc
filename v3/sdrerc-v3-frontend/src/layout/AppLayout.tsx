import { useState } from 'react'
import { Box } from '@mui/material'
import { Outlet } from 'react-router-dom'
import { Sidebar, SIDEBAR_WIDTH_COLLAPSED, SIDEBAR_WIDTH_EXPANDED } from './Sidebar'
import { useAuth } from '../auth/AuthContext'

const SIDEBAR_COLLAPSED_STORAGE_KEY = 'sdrerc.v3.sidebarCollapsed'

function leerColapsadoGuardado(): boolean {
  try {
    return localStorage.getItem(SIDEBAR_COLLAPSED_STORAGE_KEY) === '1'
  } catch {
    return false
  }
}

/**
 * Shell compartido de todas las pantallas autenticadas: `Sidebar` fijo + área de contenido con
 * `<Outlet/>` (rutas anidadas en `App.tsx`). Reemplaza el envoltorio
 * `<Box sx={{minHeight:'100vh', p:4}}>` que cada página repetía individualmente — ahora ese padding
 * de página vive acá una sola vez (`p:4`, cancelado por `PageHeaderSticky` vía márgenes negativos
 * en las páginas que lo usan — ver Javadoc de `PageHeaderSticky.tsx`). `LoginPage` no pasa por este
 * layout (ruta hermana, fuera del `<Route>` padre que usa `AppLayout`).
 *
 * **Ya NO renderiza `TopBar`** (eliminado 13/08/2026, mismo día, segunda pasada sobre el header):
 * `TopBar` había quedado reducido a una franja fija vacía (sin contenido propio, solo estructural)
 * que el usuario reportó como espacio muerto encima de `PageHeaderSticky`. `PageHeaderSticky` ahora
 * absorbe esa franja como su propia Fila 1 (`position: sticky; top: 0`, mismo `TOPBAR_HEIGHT` que la
 * franja del logo del `Sidebar`) — ver su Javadoc para el detalle completo. Sin `TopBar` tampoco
 * hace falta el spacer `<Toolbar/>` que compensaba su altura fija: las páginas que SÍ usan
 * `PageHeaderSticky` ya reservan ese espacio ellas mismas (su Fila 1 mide `TOPBAR_HEIGHT`); las que
 * NO lo usan (Dashboard, Home, formularios de edición) ahora arrancan su contenido directo en
 * `y:0` del área de `<main>` — antes tenían igual una franja vacía encima sin ningún propósito.
 *
 * El área de contenido es blanca (no `backgroundDefault`), a diferencia del sidebar (`navSidebarBg`,
 * gris muy claro): la separación entre ambos es solo un cambio de fondo + sombra difusa del
 * `Drawer`, sin borde sólido. El estado colapsado/expandido del sidebar se persiste en
 * `localStorage` para sobrevivir recargas.
 */
export function AppLayout() {
  const { session } = useAuth()
  const [collapsed, setCollapsed] = useState<boolean>(leerColapsadoGuardado)
  const sidebarWidth = collapsed ? SIDEBAR_WIDTH_COLLAPSED : SIDEBAR_WIDTH_EXPANDED

  if (!session) return null

  function alternarColapsado() {
    setCollapsed((prev) => {
      const next = !prev
      try {
        localStorage.setItem(SIDEBAR_COLLAPSED_STORAGE_KEY, next ? '1' : '0')
      } catch {
        // localStorage no disponible (ej. modo privado): el estado sigue funcionando en memoria, solo no persiste.
      }
      return next
    })
  }

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: '#FFFFFF' }}>
      <Sidebar collapsed={collapsed} onToggle={alternarColapsado} />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minWidth: 0,
          width: `calc(100% - ${sidebarWidth}px)`,
          bgcolor: '#FFFFFF',
        }}
      >
        <Box sx={{ p: 4 }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  )
}
