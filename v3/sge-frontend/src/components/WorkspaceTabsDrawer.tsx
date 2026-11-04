import { Avatar, Box, IconButton, Paper, Tab, Tabs, Tooltip, Typography } from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import OpenInFullOutlinedIcon from '@mui/icons-material/OpenInFullOutlined'
import CloseFullscreenOutlinedIcon from '@mui/icons-material/CloseFullscreenOutlined'
import { useEffect, useRef, useState } from 'react'
import type { MouseEvent as ReactMouseEvent, ReactNode, WheelEvent } from 'react'
import { sdrercColors } from '../theme'
import { SIDEBAR_DOM_ID, SIDEBAR_WIDTH_EXPANDED } from '../layout/Sidebar'

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

// Traduce el scroll vertical de la rueda del mouse a scroll horizontal del strip de tabs — MUI
// Tabs no lo hace por sí solo. Sin `preventDefault()` a propósito: React registra el listener nativo
// raíz de `wheel` como pasivo por performance, y llamar `preventDefault()` desde el handler
// sintético puede disparar un warning/no-op según navegador; el pequeño scroll vertical de fondo
// que puede colarse de paso es un costo aceptable frente a ese riesgo.
function desplazarConRueda(e: WheelEvent<HTMLDivElement>) {
  const scroller = e.currentTarget.querySelector<HTMLDivElement>('.MuiTabs-scroller')
  if (!scroller || scroller.scrollWidth <= scroller.clientWidth) return
  if (Math.abs(e.deltaY) > Math.abs(e.deltaX)) {
    scroller.scrollLeft += e.deltaY
  }
}

const ANCHO_NORMAL = 640
const ANCHO_MINIMO = 400

// Ancho REAL del sidebar en el DOM en este instante (13/08/2026, corrige un bug real: la versión
// anterior reservaba siempre `SIDEBAR_WIDTH_EXPANDED` como buffer fijo, sin importar si el sidebar
// estaba realmente colapsado o expandido — con el sidebar colapsado (68px) eso dejaba ~164px de
// espacio disponible sin cubrir al "expandir" este panel, reportado por el usuario como "deja
// espacio sin cubrir"). `getBoundingClientRect()` sobre `SIDEBAR_DOM_ID` (id estable del `Paper`
// del Drawer, ver `layout/Sidebar.tsx`) siempre refleja el ancho actual, sea cual sea el estado de
// colapso — sin depender de prop-drilling ni de sincronizar estado entre árboles de componentes
// distintos (este panel vive fuera de `AppLayout`). `SIDEBAR_WIDTH_EXPANDED` queda solo como
// fallback si el elemento no se encuentra por algún motivo (nunca debería pasar en uso normal).
function anchoSidebarActual(): number {
  const el = document.getElementById(SIDEBAR_DOM_ID)
  return el ? el.getBoundingClientRect().width : SIDEBAR_WIDTH_EXPANDED
}

// Ancho disponible a la derecha del sidebar, con el ancho real medido (no una constante fija).
// Fuente de verdad ÚNICA del ancho máximo del panel: tanto el botón "Expandir panel" (`anchoActual`,
// más abajo) como el límite del arrastre manual (`iniciarArrastre#onMove`) usan este mismo valor sin
// ratio de reducción — antes el arrastre tenía su propio tope (`RATIO_MAXIMO_ARRASTRE=0.9`, 90%),
// desincronizado del 100% que ya usaba el botón desde el fix anterior; se eliminó ese ratio para que
// ambos caminos no puedan volver a desalinearse (13/08/2026, pedido explícito del usuario).
function anchoDisponible(): number {
  return Math.max(ANCHO_MINIMO, window.innerWidth - anchoSidebarActual())
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
 * **Legibilidad del tab activo + overflow (13/08/2026)**: el tab activo del nivel 1 tiene fondo
 * blanco (contraste real contra la barra oscura `sidebarBg`), texto semibold, e indicador inferior
 * de 3.5px en `navSidebarActiveBar`; los inactivos quedan en blanco atenuado sin fondo propio. Las
 * flechas de overflow de MUI (`scrollButtons="auto"`) se recolorearon a blanco (se perdían contra
 * el fondo oscuro) y se agregó `desplazarConRueda` para scroll horizontal con la rueda del mouse.
 *
 * **Expandir/contraer + resize manual (13/08/2026, mismo día, pedido explícito del usuario)**:
 * - Botón "Expandir/Contraer panel" (`OpenInFullOutlined`/`CloseFullscreenOutlined`, estilo
 *   maximize/restore) al extremo derecho de la fila de tabs de nivel 1 — alterna `expandido` entre
 *   el ancho normal (o el último ancho manual, ver abajo) y el 100% de `anchoDisponible()` (el ancho
 *   real a la derecha del sidebar, sin ningún ratio de reducción).
 * - Handle de resize (barra de 6px, `cursor: col-resize`) en el borde izquierdo del panel: arrastrar
 *   ajusta el ancho en tiempo real entre `ANCHO_MINIMO` (400px) y el 100% de `anchoDisponible()` —
 *   MISMA función/fuente de verdad que usa el botón de expandir (13/08/2026, segundo bugfix: el
 *   arrastre tenía antes su propio tope `RATIO_MAXIMO_ARRASTRE=0.9` (90%), desincronizado del 100%
 *   que ya usaba el botón; se eliminó ese ratio para que ambos caminos no puedan volver a
 *   desalinearse, y para que el arrastre también llegue al 100% real con el sidebar colapsado o
 *   expandido, igual que el botón). Arrastrar siempre desactiva `expandido` (el ancho pasa a ser
 *   "manual") y actualiza `anchoManual` en vivo; al soltar, el ancho queda fijo en ese valor sin
 *   rebote posterior (el clamp final ya es el mismo que el último valor calculado en `onMove`).
 * - Convivencia entre ambos (criterio más simple que cumple lo pedido, sin caso especial): el ancho
 *   final es `expandido ? anchoDisponible() : (anchoManual ?? ANCHO_NORMAL)` — expandir siempre ocupa
 *   el 100% del espacio disponible (ignora `anchoManual` mientras dure, pero no lo borra); contraer
 *   vuelve solo, sin código adicional, al último `anchoManual` si existía, o al ancho normal si el
 *   usuario nunca arrastró el handle. Transición animada (`width 220ms ease`) SALVO durante el
 *   arrastre activo (se desactiva para que el resize se sienta 1:1 con el mouse, no con retraso de
 *   easing).
 * - **Ancho disponible medido en vivo, no hardcodeado (13/08/2026, corrige bug real reportado por el
 *   usuario: "no llega al 100%, deja espacio sin cubrir")**: `anchoDisponible()` restaba antes un
 *   buffer fijo `SIDEBAR_WIDTH_EXPANDED` (232px) sin importar si el sidebar estaba realmente
 *   colapsado (68px) o expandido — con el sidebar colapsado, el buffer sobraba ~164px que quedaban
 *   como franja vacía a la izquierda del panel, además del propio recorte del extinto
 *   `RATIO_EXPANDIDO` (88%). Fix: `Sidebar.tsx` expone `SIDEBAR_DOM_ID` (id estable en el `Paper`
 *   del `Drawer`, vía `slotProps={{ paper: { id: SIDEBAR_DOM_ID } }}`) y este componente mide el
 *   ancho REAL con `anchoSidebarActual()` (`getBoundingClientRect().width` sobre ese id, con
 *   fallback a `SIDEBAR_WIDTH_EXPANDED` solo si el elemento no existe todavía). Como
 *   `anchoDisponible()` se recalcula en cada render (no se cachea en estado), el resultado ya es
 *   correcto tanto con el sidebar colapsado como expandido sin lógica adicional.
 * - El contenido interno (`PanelDatosExpediente`, `RegistroExpedienteForm`/`SectionCard`) no
 *   necesitó ningún cambio para reflow: ya usa MUI `Grid` con `size={6}` (fluido/responsive por
 *   diseño, no columnas de ancho fijo), así que se reacomoda solo con el nuevo ancho del panel.
 *
 * Cierre de tab: `onCloseWorkspace` (ver `RegistroBandejaPage.tsx`) mueve el foco al tab ADYACENTE
 * por posición (el que ocupa el índice del cerrado, o el anterior si se cerró el último), no
 * simplemente "el último de la lista" — mismo criterio que un navegador.
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
  const [anchoManual, setAnchoManual] = useState<number | null>(null)
  const [expandido, setExpandido] = useState(false)
  const [arrastrando, setArrastrando] = useState(false)
  const listenersRef = useRef<{ move: (e: MouseEvent) => void; up: () => void } | null>(null)

  useEffect(
    () => () => {
      if (!listenersRef.current) return
      window.removeEventListener('mousemove', listenersRef.current.move)
      window.removeEventListener('mouseup', listenersRef.current.up)
      document.body.style.userSelect = ''
      document.body.style.cursor = ''
    },
    [],
  )

  function iniciarArrastre(e: ReactMouseEvent) {
    e.preventDefault()
    setExpandido(false)
    setArrastrando(true)
    document.body.style.userSelect = 'none'
    document.body.style.cursor = 'col-resize'

    const onMove = (ev: MouseEvent) => {
      const maximo = anchoDisponible()
      setAnchoManual(Math.min(maximo, Math.max(ANCHO_MINIMO, window.innerWidth - ev.clientX)))
    }
    const onUp = () => {
      setArrastrando(false)
      document.body.style.userSelect = ''
      document.body.style.cursor = ''
      window.removeEventListener('mousemove', onMove)
      window.removeEventListener('mouseup', onUp)
      listenersRef.current = null
    }
    listenersRef.current = { move: onMove, up: onUp }
    window.addEventListener('mousemove', onMove)
    window.addEventListener('mouseup', onUp)
  }

  if (workspaces.length === 0) return null
  const activo = workspaces.find((w) => w.id === activeWorkspaceId) ?? workspaces[workspaces.length - 1]
  const subTabActiva = activo.subTabs.find((s) => s.id === activo.activeSubTabId) ?? activo.subTabs[0]
  const anchoActual = expandido ? anchoDisponible() : (anchoManual ?? ANCHO_NORMAL)

  return (
    <Paper
      elevation={6}
      sx={{
        position: 'fixed',
        top: 0,
        right: 0,
        height: '100vh',
        width: anchoActual,
        maxWidth: '100vw',
        transition: arrastrando ? 'none' : 'width 220ms ease',
        zIndex: 1300,
        display: 'flex',
        flexDirection: 'column',
        borderRadius: 0,
      }}
    >
      {/* Handle de resize manual — borde izquierdo, cursor col-resize, resaltado en hover/arrastre */}
      <Box
        onMouseDown={iniciarArrastre}
        sx={{
          position: 'absolute',
          left: 0,
          top: 0,
          bottom: 0,
          width: 6,
          cursor: 'col-resize',
          zIndex: 1,
          bgcolor: arrastrando ? sdrercColors.navSidebarActiveBar : 'transparent',
          opacity: arrastrando ? 0.6 : 1,
          transition: 'background-color 0.15s ease',
          '&:hover': { bgcolor: sdrercColors.navSidebarActiveBar, opacity: 0.5 },
        }}
      />

      {/* Nivel 1: una pestaña por expediente/titular abierto + botón expandir/contraer */}
      <Box sx={{ display: 'flex', alignItems: 'stretch', bgcolor: sdrercColors.sidebarBg, borderBottom: 1, borderColor: 'divider', flexShrink: 0 }}>
        <Tabs
          value={activo.id}
          onChange={(_, value) => onSelectWorkspace(value)}
          variant="scrollable"
          scrollButtons="auto"
          allowScrollButtonsMobile
          onWheel={desplazarConRueda}
          TabIndicatorProps={{ sx: { bgcolor: sdrercColors.navSidebarActiveBar, height: 3.5 } }}
          sx={{
            flex: 1,
            minWidth: 0,
            minHeight: 44,
            '& .MuiTabs-scrollButtons': { color: '#FFFFFF' },
            '& .MuiTabs-scrollButtons.Mui-disabled': { opacity: 0.25 },
          }}
        >
          {workspaces.map((ws) => {
            const activa = ws.id === activo.id
            return (
              <Tab
                key={ws.id}
                value={ws.id}
                sx={{
                  minHeight: 44,
                  textTransform: 'none',
                  px: 1.5,
                  borderRadius: '6px 6px 0 0',
                  color: activa ? sdrercColors.textPrimary : 'rgba(255,255,255,0.6)',
                  bgcolor: activa ? '#FFFFFF' : 'transparent',
                  transition: 'background-color 0.15s ease, color 0.15s ease',
                  '&:hover': { bgcolor: activa ? '#FFFFFF' : 'rgba(255,255,255,0.08)' },
                }}
                label={
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
                    <Avatar sx={{ width: 20, height: 20, fontSize: 11, bgcolor: ws.color }}>{(ws.label || '?').trim().charAt(0).toUpperCase()}</Avatar>
                    <Typography
                      variant="body2"
                      component="span"
                      sx={{
                        color: 'inherit',
                        fontWeight: activa ? 600 : 400,
                        maxWidth: 160,
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                      }}
                    >
                      {ws.label}
                    </Typography>
                    <IconButton
                      size="small"
                      component="span"
                      onClick={(e) => {
                        e.stopPropagation()
                        onCloseWorkspace(ws.id)
                      }}
                      sx={{ color: 'inherit', opacity: activa ? 0.7 : 0.5, '&:hover': { opacity: 1 } }}
                    >
                      <CloseIcon fontSize="inherit" sx={{ fontSize: 14 }} />
                    </IconButton>
                  </Box>
                }
              />
            )
          })}
        </Tabs>

        <Tooltip title={expandido ? 'Contraer panel' : 'Expandir panel'}>
          <IconButton
            size="small"
            onClick={() => setExpandido((v) => !v)}
            sx={{ color: '#FFFFFF', alignSelf: 'center', mx: 1, flexShrink: 0 }}
          >
            {expandido ? <CloseFullscreenOutlinedIcon fontSize="small" /> : <OpenInFullOutlinedIcon fontSize="small" />}
          </IconButton>
        </Tooltip>
      </Box>

      {/* Nivel 2: subpestañas fijas (Datos/Asociar/Grupo Familiar) del expediente activo */}
      <Tabs
        value={subTabActiva.id}
        onChange={(_, value) => onSelectSubTab(activo.id, value)}
        variant="scrollable"
        scrollButtons="auto"
        onWheel={desplazarConRueda}
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
