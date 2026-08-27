import { useState } from 'react'
import {
  Avatar,
  Box,
  ButtonBase,
  Chip,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Menu,
  MenuItem,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material'
import ChevronLeftOutlinedIcon from '@mui/icons-material/ChevronLeftOutlined'
import ChevronRightOutlinedIcon from '@mui/icons-material/ChevronRightOutlined'
import LogoutOutlinedIcon from '@mui/icons-material/LogoutOutlined'
import { useLocation, useNavigate } from 'react-router-dom'
import { NAV_GROUPS } from './navConfig'
import { TOPBAR_HEIGHT } from './layoutConstants'
import { sdrercColors } from '../theme'
import { useAuth } from '../auth/AuthContext'
import logoReniec from '../assets/reniec-logo.jpeg'

export const SIDEBAR_WIDTH_EXPANDED = 232
export const SIDEBAR_WIDTH_COLLAPSED = 68
/**
 * Id estable del `Paper` del Drawer, para que otros componentes fuera del árbol de `AppLayout`
 * (ej. `WorkspaceTabsDrawer.tsx`) puedan medir el ancho REAL del sidebar en el DOM con
 * `getBoundingClientRect()` — más confiable que duplicar el estado `collapsed` vía prop-drilling o
 * localStorage (que puede quedar desincronizado un render). Ver Javadoc de `WorkspaceTabsDrawer`
 * para el caso de uso concreto (botón "Expandir panel" al 100% del ancho disponible).
 */
export const SIDEBAR_DOM_ID = 'sdrerc-sidebar'

interface SidebarProps {
  collapsed: boolean
  onToggle: () => void
}

function inicialesDe(nombre: string): string {
  const partes = nombre.trim().split(/\s+/).filter(Boolean)
  if (partes.length === 0) return '?'
  if (partes.length === 1) return partes[0].slice(0, 2).toUpperCase()
  return (partes[0][0] + partes[1][0]).toUpperCase()
}

/**
 * Sidebar de navegación principal, estilo SaaS moderno (Linear/Notion/Vercel). Historial de
 * rediseños: paleta/indicador de activo (13/08/2026), fix de estructura sidebar/TopBar el mismo día
 * (ver Javadoc anterior en git blame), y esta tercera pasada (13/08/2026, mismo día, pedido explícito
 * del usuario) que redistribuye el `Paper` en 3 franjas fijas dentro de un `display:flex;
 * flexDirection:column` (ya provisto por defecto por `.MuiDrawer-paper` de MUI):
 *
 * 1. Franja superior (`TOPBAR_HEIGHT`, `flexShrink:0`): logo de RENIEC + "SDRERC" + toggle de
 *    colapso — sin cambios de esta pasada, solo se redujo la altura porque `TOPBAR_HEIGHT` bajó de
 *    64 a 56px (ver `layoutConstants.ts`).
 * 2. Navegación (`flex:1`, `overflowY:'auto'`): scroll propio, independiente del contenido
 *    principal y de la tarjeta de usuario — al ser `flex:1` entre dos hermanos `flexShrink:0`, el
 *    alto disponible se recalcula solo, sin cálculos manuales de `calc()`; los últimos items nunca
 *    quedan tapados por la tarjeta de abajo porque esta ocupa su propio espacio fijo en el flujo.
 * 3. Tarjeta de sesión (`flexShrink:0`, nueva): antes vivía en el `TopBar` (pegada a la derecha,
 *    dejando un hueco vacío enorme en el centro del header — el motivo de este rediseño). Mismo
 *    patrón que Slack/Notion/Linear: avatar + nombre + usuario compactos, siempre visible sin
 *    scroll (`position` normal dentro del `Paper` ya fijo a la ventana, no hace falta `sticky`
 *    porque el `Paper` completo ya es `position:fixed`). Clic abre un `Menu` hacia ARRIBA
 *    (`anchorOrigin` `top`/`transformOrigin` `bottom`, porque el anchor está al fondo) con el badge
 *    de rol y "Salir". En modo colapsado se omite nombre/usuario (68px no alcanza), queda solo el
 *    avatar centrado — el menú al hacer clic sigue mostrando la identidad completa.
 *
 * Resto sin cambios respecto al rediseño anterior: sin borde sólido contra el contenido, indicador
 * de barra de 3px en el item activo, iconos outline en línea con el label, encabezados de sección
 * discretos, badge "Próximamente" como punto. `variant="permanent"` a propósito (no
 * `temporary`/`Modal`), mismo criterio que evitó el bug de captura de clics de `WorkspaceTabsDrawer.tsx`.
 */
export function Sidebar({ collapsed, onToggle }: SidebarProps) {
  const location = useLocation()
  const navigate = useNavigate()
  const { session, logout } = useAuth()
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null)
  const width = collapsed ? SIDEBAR_WIDTH_COLLAPSED : SIDEBAR_WIDTH_EXPANDED

  if (!session) return null

  function estaActivo(path: string): boolean {
    if (path === '/') return location.pathname === '/'
    return location.pathname === path || location.pathname.startsWith(`${path}/`)
  }

  const nombreCompleto = session.nombreCompleto || session.username

  return (
    <Drawer
      variant="permanent"
      slotProps={{ paper: { id: SIDEBAR_DOM_ID } }}
      sx={{
        width,
        flexShrink: 0,
        transition: 'width 0.2s ease',
        '& .MuiDrawer-paper': {
          width,
          boxSizing: 'border-box',
          bgcolor: sdrercColors.navSidebarBg,
          border: 'none',
          boxShadow: '2px 0 10px rgba(15, 23, 42, 0.04)',
          transition: 'width 0.2s ease',
          overflowX: 'hidden',
        },
      }}
    >
      {/* 1. Franja del logo: misma altura que TopBar (TOPBAR_HEIGHT), fija arriba del sidebar. */}
      <Box
        sx={{
          height: TOPBAR_HEIGHT,
          minHeight: TOPBAR_HEIGHT,
          flexShrink: 0,
          display: 'flex',
          alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'space-between',
          px: collapsed ? 0 : 1.5,
        }}
      >
        {collapsed ? (
          <IconButton
            size="small"
            onClick={onToggle}
            sx={{ color: sdrercColors.navSidebarIcon, '&:hover': { bgcolor: sdrercColors.navSidebarHoverBg } }}
          >
            <ChevronRightOutlinedIcon fontSize="small" />
          </IconButton>
        ) : (
          <>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, overflow: 'hidden', minWidth: 0 }}>
              <Box
                component="img"
                src={logoReniec}
                alt="RENIEC"
                sx={{
                  height: 28,
                  maxHeight: 32,
                  width: 'auto',
                  flexShrink: 0,
                  borderRadius: 1,
                  bgcolor: '#FFFFFF',
                  p: '3px',
                  boxShadow: '0 0 0 1px rgba(15,23,42,0.08)',
                }}
              />
              <Typography
                variant="subtitle1"
                noWrap
                sx={{ color: sdrercColors.navSidebarTextPrimary, fontWeight: 700, letterSpacing: 0.4 }}
              >
                SDRERC
              </Typography>
            </Box>
            <IconButton
              size="small"
              onClick={onToggle}
              sx={{ color: sdrercColors.navSidebarIcon, flexShrink: 0, '&:hover': { bgcolor: sdrercColors.navSidebarHoverBg } }}
            >
              <ChevronLeftOutlinedIcon fontSize="small" />
            </IconButton>
          </>
        )}
      </Box>

      {/* 2. Navegación: scroll propio, independiente del contenido principal y de la tarjeta de usuario. */}
      <Box sx={{ overflowY: 'auto', overflowX: 'hidden', flex: 1, pt: 0.5 }}>
        {NAV_GROUPS.map((group, groupIndex) => {
          const itemsVisibles = group.items.filter(
            (item) => !item.requiredPermiso || session.permisos.length === 0 || session.permisos.includes(item.requiredPermiso),
          )
          if (itemsVisibles.length === 0) return null
          return (
            <Box key={group.title ?? `group-${groupIndex}`} sx={{ mb: 1 }}>
              {group.title && !collapsed && (
                <Typography
                  variant="caption"
                  sx={{
                    display: 'block',
                    color: sdrercColors.navSidebarTextSecondary,
                    px: 2.5,
                    pt: 2,
                    pb: 0.75,
                    fontSize: '0.65rem',
                    fontWeight: 500,
                    textTransform: 'uppercase',
                    letterSpacing: '0.06em',
                  }}
                >
                  {group.title}
                </Typography>
              )}
              <List dense disablePadding>
                {itemsVisibles.map((item) => {
                  const activo = item.built && item.path ? estaActivo(item.path) : false
                  const boton = (
                    <Box key={item.label} sx={{ position: 'relative', mx: 1, my: 0.25 }}>
                      {activo && (
                        <Box
                          sx={{
                            position: 'absolute',
                            left: 0,
                            top: '18%',
                            bottom: '18%',
                            width: 3,
                            borderRadius: 1,
                            bgcolor: sdrercColors.navSidebarActiveBar,
                          }}
                        />
                      )}
                      <ListItemButton
                        disabled={!item.built}
                        selected={activo}
                        onClick={() => item.path && navigate(item.path)}
                        sx={{
                          borderRadius: 1.5,
                          pl: collapsed ? 1.25 : 2,
                          color: sdrercColors.navSidebarTextPrimary,
                          justifyContent: collapsed ? 'center' : 'flex-start',
                          '&:hover': { bgcolor: sdrercColors.navSidebarHoverBg },
                          '&.Mui-selected': { bgcolor: sdrercColors.navSidebarActiveBg, color: sdrercColors.navSidebarActiveBar },
                          '&.Mui-selected:hover': { bgcolor: sdrercColors.navSidebarActiveBg },
                          '&.Mui-disabled': { color: sdrercColors.navSidebarTextMuted, opacity: 1 },
                        }}
                      >
                        <ListItemIcon
                          sx={{
                            minWidth: collapsed ? 0 : 30,
                            color: activo ? sdrercColors.navSidebarActiveBar : sdrercColors.navSidebarIcon,
                            justifyContent: 'center',
                            '& .MuiSvgIcon-root': { fontSize: 19 },
                          }}
                        >
                          {item.icon}
                        </ListItemIcon>
                        {!collapsed && (
                          <ListItemText
                            primary={item.label}
                            slotProps={{ primary: { variant: 'body2', sx: { fontWeight: activo ? 600 : 500 } } }}
                          />
                        )}
                        {!collapsed && !item.built && (
                          <Box
                            aria-hidden
                            sx={{ width: 5, height: 5, borderRadius: '50%', bgcolor: sdrercColors.navSidebarTextMuted, flexShrink: 0 }}
                          />
                        )}
                      </ListItemButton>
                    </Box>
                  )
                  const tooltipTitle = collapsed
                    ? item.built
                      ? item.label
                      : `${item.label} (Próximamente)`
                    : item.built
                      ? ''
                      : 'Próximamente'
                  return (
                    <Tooltip key={item.label} title={tooltipTitle} placement="right" disableHoverListener={!tooltipTitle}>
                      <span>{boton}</span>
                    </Tooltip>
                  )
                })}
              </List>
            </Box>
          )
        })}
      </Box>

      {/* 3. Tarjeta de sesión, fija abajo — mismo patrón que Slack/Notion/Linear. */}
      <Box sx={{ flexShrink: 0, p: collapsed ? 1 : 1.25, boxShadow: 'inset 0 1px 0 rgba(15,23,42,0.05)' }}>
        <ButtonBase
          onClick={(e) => setMenuAnchor(e.currentTarget)}
          sx={{
            width: '100%',
            display: 'flex',
            alignItems: 'center',
            gap: 1,
            borderRadius: 1.5,
            p: 0.75,
            justifyContent: collapsed ? 'center' : 'flex-start',
            '&:hover': { bgcolor: sdrercColors.navSidebarHoverBg },
          }}
        >
          <Avatar sx={{ width: 30, height: 30, fontSize: '0.7rem', bgcolor: sdrercColors.primary, flexShrink: 0 }}>
            {inicialesDe(nombreCompleto)}
          </Avatar>
          {!collapsed && (
            <Box sx={{ minWidth: 0, overflow: 'hidden', textAlign: 'left' }}>
              <Typography variant="body2" noWrap sx={{ fontWeight: 600, color: sdrercColors.navSidebarTextPrimary, lineHeight: 1.25 }}>
                {nombreCompleto}
              </Typography>
              <Typography variant="caption" noWrap sx={{ color: sdrercColors.navSidebarTextSecondary, lineHeight: 1.25 }}>
                {session.username}
              </Typography>
            </Box>
          )}
        </ButtonBase>
      </Box>

      <Menu
        anchorEl={menuAnchor}
        open={Boolean(menuAnchor)}
        onClose={() => setMenuAnchor(null)}
        anchorOrigin={{ vertical: 'top', horizontal: 'left' }}
        transformOrigin={{ vertical: 'bottom', horizontal: 'left' }}
        slotProps={{ paper: { sx: { minWidth: 220, ml: 1 } } }}
      >
        <Box sx={{ px: 2, py: 1.25 }}>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            {nombreCompleto}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {session.username}
          </Typography>
          <Stack direction="row" spacing={0.5} sx={{ mt: 0.75, flexWrap: 'wrap' }}>
            {session.roles.map((r) => (
              <Chip key={r} label={r} size="small" variant="outlined" color="primary" sx={{ height: 20, fontSize: '0.65rem' }} />
            ))}
          </Stack>
        </Box>
        <Divider />
        <MenuItem
          onClick={() => {
            setMenuAnchor(null)
            logout()
            navigate('/login')
          }}
        >
          <ListItemIcon>
            <LogoutOutlinedIcon fontSize="small" />
          </ListItemIcon>
          Salir
        </MenuItem>
      </Menu>
    </Drawer>
  )
}
