import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  Stack,
  Typography,
  alpha,
} from '@mui/material'
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutlined'
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined'
import ReportProblemOutlinedIcon from '@mui/icons-material/ReportProblemOutlined'
import WarningAmberOutlinedIcon from '@mui/icons-material/WarningAmberOutlined'
import { sdrercColors } from '../theme'

/**
 * Reemplazo premium de `window.confirm`/`window.alert` (01/09/2026, pedido explícito del usuario:
 * "se ven como cuadros de diálogo rústicos del sistema operativo... quiero un componente
 * reutilizable y centralizado"). Un solo `Dialog` de MUI, montado una vez en `main.tsx`, reutilizado
 * por toda la app vía el hook `useAppDialog()` — mismo criterio que `useAuth()`.
 *
 * Diseño alineado al "sistema de diseño premium" ya construido en esta sesión: `PaperProps
 * elevation=3` (mismo tono de sombra que `theme.ts#MuiPaper.elevation3`, la escala reservada para
 * modal/drawer/popover), círculo de ícono con fondo pastel (`alpha(color, 0.12)`, mismo lenguaje
 * que las pastillas de color de `index.css` para íconos de acción de grilla), color según
 * severidad (`danger`=rojo/eliminar, `warning`=ámbar/acción reversible pero impactante,
 * `info`=azul/acción normal, `success`=verde, solo para `alert`).
 *
 * API deliberadamente parecida a `window.confirm`/`window.alert` (mensaje como 1er argumento,
 * opciones como 2do) para que reemplazar cada sitio sea mecánico:
 *   `const ok = window.confirm('¿Eliminar X?')` → `const ok = await confirm('¿Eliminar X?', {tone:'danger', confirmLabel:'Eliminar'})`
 *   `window.alert('mensaje')` → `await alert('mensaje')`
 * Ambos requieren que la función contenedora sea `async` — ya lo son en todos los sitios
 * reales encontrados (siempre siguen con `try { await ... }`).
 */

export type DialogTone = 'danger' | 'warning' | 'info' | 'success'

export interface ConfirmOptions {
  title?: string
  tone?: DialogTone
  confirmLabel?: string
  cancelLabel?: string
}

export interface AlertOptions {
  title?: string
  tone?: DialogTone
  okLabel?: string
}

interface AppDialogContextValue {
  confirm: (message: ReactNode, options?: ConfirmOptions) => Promise<boolean>
  alert: (message: ReactNode, options?: AlertOptions) => Promise<void>
}

const AppDialogContext = createContext<AppDialogContextValue | null>(null)

const TONE_CONFIG: Record<DialogTone, { color: string; Icon: typeof InfoOutlinedIcon; defaultTitle: string }> = {
  danger: { color: sdrercColors.error, Icon: ReportProblemOutlinedIcon, defaultTitle: 'Confirmar eliminación' },
  warning: { color: sdrercColors.warning, Icon: WarningAmberOutlinedIcon, defaultTitle: 'Confirmar acción' },
  info: { color: sdrercColors.info, Icon: InfoOutlinedIcon, defaultTitle: 'Confirmar acción' },
  success: { color: sdrercColors.success, Icon: CheckCircleOutlineIcon, defaultTitle: 'Aviso' },
}

type DialogState =
  | { mode: 'confirm'; message: ReactNode; options: ConfirmOptions; resolve: (value: boolean) => void }
  | { mode: 'alert'; message: ReactNode; options: AlertOptions; resolve: (value: void) => void }
  | null

export function AppDialogProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<DialogState>(null)

  const confirm = useCallback((message: ReactNode, options: ConfirmOptions = {}) => {
    return new Promise<boolean>((resolve) => {
      setState({ mode: 'confirm', message, options, resolve })
    })
  }, [])

  const alert = useCallback((message: ReactNode, options: AlertOptions = {}) => {
    return new Promise<void>((resolve) => {
      setState({ mode: 'alert', message, options, resolve })
    })
  }, [])

  const value = useMemo(() => ({ confirm, alert }), [confirm, alert])

  function cerrar(resultado: boolean) {
    if (!state) return
    if (state.mode === 'confirm') state.resolve(resultado)
    else state.resolve()
    setState(null)
  }

  const tone: DialogTone = (state?.options.tone as DialogTone | undefined) ?? 'info'
  const { color, Icon, defaultTitle } = TONE_CONFIG[tone]
  const title = state?.options.title ?? defaultTitle

  return (
    <AppDialogContext.Provider value={value}>
      {children}
      <Dialog
        open={state !== null}
        onClose={() => cerrar(false)}
        slotProps={{ paper: { elevation: 3, sx: { borderRadius: 3, minWidth: 420, maxWidth: 480 } } }}
      >
        <DialogContent sx={{ pt: 4, pb: 1, px: 4 }}>
          <Stack spacing={2}>
            <Box
              sx={{
                width: 48,
                height: 48,
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                bgcolor: alpha(color, 0.12),
              }}
            >
              <Icon sx={{ color, fontSize: 26 }} />
            </Box>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              {title}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ whiteSpace: 'pre-line' }}>
              {state?.message}
            </Typography>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 4, pb: 3, pt: 2 }}>
          {state?.mode === 'confirm' && (
            <Button onClick={() => cerrar(false)} color="inherit">
              {state.options.cancelLabel ?? 'Cancelar'}
            </Button>
          )}
          <Button
            onClick={() => cerrar(true)}
            variant="contained"
            sx={{ bgcolor: color, '&:hover': { bgcolor: alpha(color, 0.85) } }}
          >
            {state?.mode === 'confirm' ? (state.options.confirmLabel ?? 'Continuar') : (state?.options.okLabel ?? 'Entendido')}
          </Button>
        </DialogActions>
      </Dialog>
    </AppDialogContext.Provider>
  )
}

export function useAppDialog(): AppDialogContextValue {
  const ctx = useContext(AppDialogContext)
  if (!ctx) throw new Error('useAppDialog debe usarse dentro de <AppDialogProvider>')
  return ctx
}
