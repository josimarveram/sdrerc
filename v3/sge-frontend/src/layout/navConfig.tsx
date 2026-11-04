import type { ReactNode } from 'react'
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined'
import BarChartOutlinedIcon from '@mui/icons-material/BarChartOutlined'
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined'
import MoveToInboxOutlinedIcon from '@mui/icons-material/MoveToInboxOutlined'
import AssignmentIndOutlinedIcon from '@mui/icons-material/AssignmentIndOutlined'
import FactCheckOutlinedIcon from '@mui/icons-material/FactCheckOutlined'
import VerifiedOutlinedIcon from '@mui/icons-material/VerifiedOutlined'
import GavelOutlinedIcon from '@mui/icons-material/GavelOutlined'
import NotificationsOutlinedIcon from '@mui/icons-material/NotificationsOutlined'
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined'
import PeopleOutlinedIcon from '@mui/icons-material/PeopleOutlined'
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined'
import AdminPanelSettingsOutlinedIcon from '@mui/icons-material/AdminPanelSettingsOutlined'
import EventOutlinedIcon from '@mui/icons-material/EventOutlined'
import ScheduleOutlinedIcon from '@mui/icons-material/ScheduleOutlined'
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined'

export interface NavItem {
  label: string
  path?: string
  icon: ReactNode
  built: boolean
  /**
   * Código PERMISO (MENU_*, ver tabla `PERMISO`/`ROL_PERMISO` — mismo catálogo que V2, `65_matriz_permisos_roles_operativos.sql`)
   * requerido para ver este ítem. `undefined` = siempre visible (ej. "Inicio"). Filtrado real en
   * `Sidebar.tsx`/`HomePage.tsx` contra `session.permisos` (fail-open si esa lista viene vacía,
   * mismo criterio que `SessionContext.tienePermiso` en V2).
   */
  requiredPermiso?: string
}

export interface NavGroup {
  title: string | null
  items: NavItem[]
}

/**
 * Estructura de navegación calcada de los "Modulos vigentes SDRERC V2" (ver `CLAUDE.md` raíz,
 * incluida la separación "Dashboard y Reportes" vs. "Administración" como bloques propios, no
 * mezclados con la operación diaria). Los módulos que todavía no existen en V3 se muestran igual
 * (deshabilitados, con chip "Próximamente") en vez de omitirse — el usuario pidió explícitamente un
 * home "a la altura" de la app, y la lista completa comunica el alcance real del sistema sin
 * inventar funcionalidad que no existe.
 */
export const NAV_GROUPS: NavGroup[] = [
  {
    title: null,
    items: [{ label: 'Inicio', path: '/', icon: <HomeOutlinedIcon fontSize="small" />, built: true }],
  },
  {
    title: 'Dashboard y Reportes',
    items: [
      {
        label: 'Dashboard',
        path: '/dashboard',
        icon: <BarChartOutlinedIcon fontSize="small" />,
        built: true,
        requiredPermiso: 'MENU_DASHBOARD',
      },
    ],
  },
  {
    title: 'Operación',
    items: [
      { label: 'Bandeja de Expedientes', path: '/bandeja', icon: <Inventory2OutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_BANDEJA' },
      { label: 'Registro / Recepción', path: '/registro', icon: <MoveToInboxOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_REGISTRO' },
      { label: 'Asignación', path: '/asignacion', icon: <AssignmentIndOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_ASIGNACION' },
      { label: 'Análisis', path: '/analisis', icon: <FactCheckOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_ANALISIS' },
      { label: 'Verificación', path: '/verificacion', icon: <VerifiedOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_VERIFICACION' },
      { label: 'Ejecución', path: '/ejecucion', icon: <GavelOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_EJECUCION' },
      // 22/08/2026, pedido explícito del usuario: `/notificacion` (no una bandeja fija) — un rol
      // restringido a una sola pestaña de Notificación (ej. VALIDACION) debe aterrizar directo en la
      // suya, no siempre en "Bandeja Asignación" (ver `NotificacionIndexPage.tsx`).
      { label: 'Notificación', path: '/notificacion', icon: <NotificationsOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_NOTIFICACION' },
      { label: 'Expediente digital', icon: <DescriptionOutlinedIcon fontSize="small" />, built: false, requiredPermiso: 'MENU_EXPEDIENTE_DIGITAL' },
    ],
  },
  {
    title: 'Administración',
    items: [
      { label: 'Usuarios', path: '/admin/usuarios', icon: <PeopleOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_ADMIN_USUARIOS' },
      { label: 'Equipo Jurídico', path: '/admin/equipo-juridico', icon: <GroupsOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_ADMIN_EQUIPO_JURIDICO' },
      { label: 'Roles', path: '/admin/roles', icon: <AdminPanelSettingsOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_ADMIN_ROLES' },
      { label: 'Feriados', path: '/admin/feriados', icon: <EventOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_ADMIN_FERIADOS' },
      { label: 'Plazos', path: '/admin/plazos', icon: <ScheduleOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_ADMIN_PLAZOS' },
      { label: 'Plantillas de documento', path: '/admin/plantillas', icon: <ArticleOutlinedIcon fontSize="small" />, built: true, requiredPermiso: 'MENU_ADMIN_PLANTILLAS' },
    ],
  },
]
