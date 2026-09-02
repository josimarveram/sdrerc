import { createTheme } from '@mui/material/styles'

/**
 * Tema base de V3, derivado de la paleta institucional ya aprobada en
 * src/main/java/com/sdrerc/ui/appv2/theme/AppV2Theme.java (V2) — no son colores nuevos,
 * son los mismos traducidos a hex para web. Ver Anexo A de
 * docs/arquitectura_app/PLAN_MIGRACION_SDRERC_V3.md.
 */

export const sdrercColors = {
  primary: '#154775',
  primaryDark: '#0C2F52',
  primaryHover: '#1F5B91',
  // Oscuro, reservado para la barra superior del panel de workspaces tipo Salesforce Console
  // (`WorkspaceTabsDrawer.tsx`) — NO es el sidebar de navegación principal, ver `navSidebar*` abajo.
  sidebarBg: '#11314E',
  sidebarHover: '#163C5E',
  sidebarActive: '#0A2239',
  // Sidebar de navegación principal (`layout/Sidebar.tsx`): claro (no fondo invertido oscuro, esa
  // opción ya se descartó explícitamente el 13/08/2026), pero con más peso visual que el rediseño
  // original — 28/08/2026, sistema de diseño premium, pedido explícito del usuario ("sigo viendo
  // muy blanco el menú de módulos"). El valor anterior (`#FAFBFC`) era MÁS claro que el propio canvas
  // de contenido (`backgroundDefault`, `#F4F6F9`) — el sidebar se distinguía menos que el área de
  // trabajo, al revés de lo esperado. Opción elegida explícitamente por el usuario ("C: tinte azul
  // sutil"): gris con tinte azul (conecta con `primary` sin ser oscuro/saturado) + fondo activo más
  // saturado, en vez de un gris neutro genérico.
  navSidebarBg: '#EEF3FA',
  navSidebarHoverBg: '#E1EAF5',
  navSidebarActiveBg: '#D6E6F7',
  // 28/08/2026, mismo día: antes era un 3er azul independiente (`#2F6FB0`), distinto de `primary`
  // (#154775, institucional) y de `info` (#2370A8, ya la identidad de la subpestaña "Datos" en
  // TODAS las bandejas operativas) — 3 azules casi iguales para roles muy parecidos ("azul de
  // marca/identidad") se leía como paleta descoordinada, no como decisión deliberada. Unificado a
  // `info` (ver `sdrercColors.info` más abajo) — el mismo azul que ya usa el sistema para su
  // "identidad neutra", sin agregar un token nuevo. Diagnóstico + propuesta confirmados
  // explícitamente por el usuario antes de aplicar.
  navSidebarActiveBar: '#2370A8',
  navSidebarTextPrimary: '#33414F',
  navSidebarTextSecondary: '#95A1AC',
  navSidebarTextMuted: '#B9C1C9',
  navSidebarIcon: '#66747F',
  backgroundDefault: '#F4F6F9',
  backgroundPaper: '#FFFFFF',
  backgroundPaperAlt: '#FAFBFD',
  divider: '#DAE1E9',
  dividerStrong: '#C4CDD8',
  textPrimary: '#1C242E',
  textSecondary: '#525F6E',
  success: '#268150',
  warning: '#B86910',
  error: '#B23434',
  info: '#2370A8',
  softBlue: '#E5F1FC',
  softGreen: '#E5F4EC',
  softOrange: '#FFF3E0',
  softRed: '#FDE8E8',
  softGray: '#EFF2F6',
} as const

const fontFamily = '"Segoe UI", system-ui, -apple-system, Roboto, Helvetica, Arial, sans-serif'

export const sdrercTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: sdrercColors.primary,
      dark: sdrercColors.primaryDark,
      light: sdrercColors.primaryHover,
      contrastText: '#FFFFFF',
    },
    success: { main: sdrercColors.success },
    warning: { main: sdrercColors.warning },
    error: { main: sdrercColors.error },
    info: { main: sdrercColors.info },
    background: {
      default: sdrercColors.backgroundDefault,
      paper: sdrercColors.backgroundPaper,
    },
    text: {
      primary: sdrercColors.textPrimary,
      secondary: sdrercColors.textSecondary,
    },
    divider: sdrercColors.divider,
  },
  typography: {
    fontFamily,
    // Escala heredada de V2 (AppV2Theme.FONT_SIZE_*), en rem con base 16px.
    fontSize: 13,
    body2: { fontSize: '0.75rem' }, // small (12px)
    body1: { fontSize: '0.8125rem' }, // base (13px)
    subtitle1: { fontSize: '0.9375rem' }, // medium (15px)
    h6: { fontSize: '1.25rem', fontWeight: 600 }, // title (20px)
    h4: { fontSize: '1.875rem', fontWeight: 600 }, // hero (30px)
  },
  shape: {
    borderRadius: 8, // cards/paneles; inputs/botones se ajustan a 4px en su propio override
  },
  spacing: 4, // mismo paso base que V2 (SPACE_SMALL=8 -> theme.spacing(2), SPACE=12 -> spacing(3), etc.)
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: { backgroundColor: sdrercColors.backgroundDefault },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
        // Escala de elevación real (28/08/2026, Incremento 0 del sistema de diseño — ver plan
        // "necesito-un-plan-de-cuddly-wombat.md"): la sombra anterior (`0 1px 2px rgba(...,0.06)`)
        // era casi imperceptible sobre un canvas que además era blanco puro (ver fix en
        // `AppLayout.tsx`, mismo incremento) — tarjeta y fondo se fundían en una sola superficie sin
        // separación visual. 3 niveles con nombre semántico, centralizados acá para que
        // `MetricCard`/`SectionCard`/el wrapper de cualquier `DataGrid` los hereden gratis sin tocar
        // cada página: reposo (elevation1, el default de `Paper` sin prop explícito), hover/activo
        // (elevation2), modal/drawer/popover (elevation3 — MUI ya usa elevation8/16/24 internamente
        // para `Menu`/`Dialog`, pero se deja el mismo valor de elevation3 disponible para quien lo
        // pida explícito).
        elevation1: {
          boxShadow: '0 1px 3px rgba(16,24,40,0.08), 0 1px 2px rgba(16,24,40,0.04)',
        },
        elevation2: {
          boxShadow: '0 4px 12px rgba(16,24,40,0.08)',
        },
        elevation3: {
          boxShadow: '0 12px 32px rgba(16,24,40,0.14)',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 4,
          textTransform: 'none',
          fontWeight: 600,
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 4,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          border: `1px solid ${sdrercColors.divider}`,
          boxShadow: 'none',
        },
      },
    },
    // Estados interactivos consolidados (28/08/2026, Incremento 0): antes cada página que usaba
    // `ListItemButton` (menús contextuales, listas de selección en diálogos, etc. — el Sidebar
    // principal ya define su propio hover/selected inline y no se ve afectado, solo se beneficia
    // cualquier `ListItemButton` fuera de él) reinventaba su propio hover en `sx` suelto o no tenía
    // ninguno. Con esto, cualquier `ListItemButton` nuevo hereda un hover/selected consistente sin
    // trabajo adicional.
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 6,
          '&:hover': { backgroundColor: sdrercColors.navSidebarHoverBg },
          '&.Mui-selected': { backgroundColor: sdrercColors.navSidebarActiveBg },
          '&.Mui-selected:hover': { backgroundColor: sdrercColors.navSidebarActiveBg },
        },
      },
    },
  },
})
