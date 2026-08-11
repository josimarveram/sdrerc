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
  sidebarBg: '#11314E',
  sidebarHover: '#163C5E',
  sidebarActive: '#0A2239',
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
        elevation1: {
          boxShadow: '0 1px 2px rgba(16,24,40,0.06)',
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
  },
})
