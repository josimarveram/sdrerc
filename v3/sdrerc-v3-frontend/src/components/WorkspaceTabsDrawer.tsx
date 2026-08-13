import { Avatar, Box, IconButton, Paper, Tab, Tabs, Typography } from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import type { ReactNode } from 'react'
import { sdrercColors } from '../theme'

export interface WorkspaceSubTab {
  id: string
  label: string
  color: string
  content: ReactNode
}

export interface Workspace {
  id: string
  label: string
  color: string
  subTabs: WorkspaceSubTab[]
  activeSubTabId: string
}

interface WorkspaceTabsDrawerProps {
  workspaces: Workspace[]
  activeWorkspaceId: string | null
  onSelectWorkspace: (id: string) => void
  onCloseWorkspace: (id: string) => void
  onSelectSubTab: (workspaceId: string, subTabId: string) => void
}

/**
 * Panel lateral de 2 niveles, estilo consola tipo Salesforce (ver captura de referencia del
 * usuario, `docs/arquitectura_app/screen-multipestañas.jpeg`): una pestaña superior por
 * expediente/titular abierto (color propio por persona, para diferenciar una de otra a simple
 * vista) y, debajo de la pestaña activa, una fila de subpestañas fijas — Datos/Asociar/Grupo
 * Familiar — con color propio por tipo (consistente entre todas las personas: "Datos" siempre el
 * mismo color, "Asociar" siempre otro, etc.).
 *
 * NO usa `Drawer`/`Modal` de MUI a propósito: un `Modal` (incluso con `hideBackdrop`) sigue
 * capturando clics sobre todo el viewport aunque el backdrop no se vea, lo que bloqueaba el doble
 * clic sobre otras filas de la grilla mientras el panel ya estaba abierto (bug reportado por el
 * usuario en la primera versión de este componente). Un `Paper` con posición fija resuelve esto de
 * forma simple y predecible: nada fuera de su propia área intercepta clics.
 *
 * Alcance aprobado explícitamente por el usuario ("arrancar con la propuesta de alcance para
 * empezar"): sin persistencia entre sesiones, sin subpestañas anidadas más de un nivel.
 */
export function WorkspaceTabsDrawer({
  workspaces,
  activeWorkspaceId,
  onSelectWorkspace,
  onCloseWorkspace,
  onSelectSubTab,
}: WorkspaceTabsDrawerProps) {
  if (workspaces.length === 0) return null
  const activo = workspaces.find((w) => w.id === activeWorkspaceId) ?? workspaces[workspaces.length - 1]
  const subTabActiva = activo.subTabs.find((s) => s.id === activo.activeSubTabId) ?? activo.subTabs[0]

  return (
    <Paper
      elevation={6}
      sx={{
        position: 'fixed',
        top: 0,
        right: 0,
        height: '100vh',
        width: 640,
        maxWidth: '100vw',
        zIndex: 1300,
        display: 'flex',
        flexDirection: 'column',
        borderRadius: 0,
      }}
    >
      {/* Nivel 1: una pestaña por expediente/titular abierto */}
      <Tabs
        value={activo.id}
        onChange={(_, value) => onSelectWorkspace(value)}
        variant="scrollable"
        scrollButtons="auto"
        sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: sdrercColors.sidebarBg, minHeight: 44, flexShrink: 0 }}
      >
        {workspaces.map((ws) => (
          <Tab
            key={ws.id}
            value={ws.id}
            sx={{
              minHeight: 44,
              textTransform: 'none',
              color: '#FFFFFF',
              opacity: 0.75,
              '&.Mui-selected': { color: '#FFFFFF', opacity: 1 },
            }}
            label={
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                <Avatar sx={{ width: 20, height: 20, fontSize: 11, bgcolor: ws.color }}>{(ws.label || '?').trim().charAt(0).toUpperCase()}</Avatar>
                <Typography variant="body2" component="span" sx={{ color: 'inherit', maxWidth: 160, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {ws.label}
                </Typography>
                <IconButton
                  size="small"
                  component="span"
                  onClick={(e) => {
                    e.stopPropagation()
                    onCloseWorkspace(ws.id)
                  }}
                  sx={{ color: 'inherit' }}
                >
                  <CloseIcon fontSize="inherit" sx={{ fontSize: 14 }} />
                </IconButton>
              </Box>
            }
          />
        ))}
      </Tabs>

      {/* Nivel 2: subpestañas fijas (Datos/Asociar/Grupo Familiar) del expediente activo */}
      <Tabs
        value={subTabActiva.id}
        onChange={(_, value) => onSelectSubTab(activo.id, value)}
        variant="scrollable"
        scrollButtons="auto"
        TabIndicatorProps={{ sx: { bgcolor: subTabActiva.color, height: 3 } }}
        sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: sdrercColors.backgroundPaperAlt, minHeight: 40, flexShrink: 0 }}
      >
        {activo.subTabs.map((sub) => (
          <Tab
            key={sub.id}
            value={sub.id}
            sx={{ minHeight: 40, textTransform: 'none' }}
            label={
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: sub.color }} />
                <Typography variant="body2" component="span">
                  {sub.label}
                </Typography>
              </Box>
            }
          />
        ))}
      </Tabs>

      <Box sx={{ flex: 1, overflowY: 'auto', p: 3 }}>{subTabActiva.content}</Box>
    </Paper>
  )
}
