import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Box, Button, Dialog, DialogActions, DialogContent, LinearProgress, Stack, Typography, alpha } from '@mui/material'
import AccessTimeOutlinedIcon from '@mui/icons-material/AccessTimeOutlined'
import { useAuth } from '../auth/AuthContext'
import { sdrercColors } from '../theme'

const MINUTOS_INACTIVIDAD = 10
const SEGUNDOS_CUENTA_REGRESIVA = 10
const MS_INACTIVIDAD = MINUTOS_INACTIVIDAD * 60 * 1000
const EVENTOS_ACTIVIDAD = ['mousedown', 'mousemove', 'keydown', 'wheel', 'touchstart'] as const

/**
 * Cierre de sesión automático por inactividad (01/09/2026, pedido explícito del usuario), en
 * línea con la reducción de `sdrerc.v3.jwt.session-ttl-minutes` de 30 a 10 minutos: tras 10
 * minutos sin actividad real del usuario (mouse/teclado/scroll/touch), se muestra un aviso
 * premium con cuenta regresiva de 10 segundos; si no se interactúa con el botón "Continuar
 * sesión" antes de que llegue a 0, se cierra la sesión y se redirige a `/login` con un aviso
 * propio (distinto del que ya muestra `http.ts#manejarSesionVencida` cuando el JWT vence "solo",
 * sin que este guard llegue a dispararse primero — ej. una request en curso justo al filo).
 *
 * Montado una sola vez dentro de `AppLayout.tsx` (el shell autenticado, que ya retorna `null`
 * sin sesión) — `LoginPage` no pasa por acá, no tiene sesión que cerrar. Mientras el aviso está
 * visible, la actividad del mouse/teclado deja de resetear el temporizador a propósito (patrón
 * intencional, no un descuido): solo el botón "Continuar sesión" cuenta como decisión explícita
 * de seguir conectado, para que el comportamiento sea determinista sin importar si el usuario
 * solo movió el mouse sin querer mientras leía el aviso.
 */
export function InactivityGuard() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const [advertenciaVisible, setAdvertenciaVisible] = useState(false)
  const [segundosRestantes, setSegundosRestantes] = useState(SEGUNDOS_CUENTA_REGRESIVA)

  const timerInactividadRef = useRef<number | null>(null)
  const intervalCuentaRegresivaRef = useRef<number | null>(null)
  const advertenciaVisibleRef = useRef(false)

  const limpiarTemporizadorInactividad = useCallback(() => {
    if (timerInactividadRef.current !== null) {
      window.clearTimeout(timerInactividadRef.current)
      timerInactividadRef.current = null
    }
  }, [])

  const limpiarCuentaRegresiva = useCallback(() => {
    if (intervalCuentaRegresivaRef.current !== null) {
      window.clearInterval(intervalCuentaRegresivaRef.current)
      intervalCuentaRegresivaRef.current = null
    }
  }, [])

  const cerrarSesionPorInactividad = useCallback(() => {
    limpiarCuentaRegresiva()
    limpiarTemporizadorInactividad()
    advertenciaVisibleRef.current = false
    setAdvertenciaVisible(false)
    logout()
    navigate('/login?cierreInactividad=1', { replace: true })
  }, [limpiarCuentaRegresiva, limpiarTemporizadorInactividad, logout, navigate])

  const mostrarAdvertencia = useCallback(() => {
    advertenciaVisibleRef.current = true
    setAdvertenciaVisible(true)
    setSegundosRestantes(SEGUNDOS_CUENTA_REGRESIVA)
    limpiarCuentaRegresiva()
    intervalCuentaRegresivaRef.current = window.setInterval(() => {
      setSegundosRestantes((segundos) => {
        if (segundos <= 1) {
          cerrarSesionPorInactividad()
          return 0
        }
        return segundos - 1
      })
    }, 1000)
  }, [cerrarSesionPorInactividad, limpiarCuentaRegresiva])

  const reiniciarTemporizadorInactividad = useCallback(() => {
    limpiarTemporizadorInactividad()
    timerInactividadRef.current = window.setTimeout(mostrarAdvertencia, MS_INACTIVIDAD)
  }, [limpiarTemporizadorInactividad, mostrarAdvertencia])

  const continuarSesion = useCallback(() => {
    limpiarCuentaRegresiva()
    advertenciaVisibleRef.current = false
    setAdvertenciaVisible(false)
    reiniciarTemporizadorInactividad()
  }, [limpiarCuentaRegresiva, reiniciarTemporizadorInactividad])

  useEffect(() => {
    reiniciarTemporizadorInactividad()

    let ultimoReset = Date.now()
    function alDetectarActividad() {
      if (advertenciaVisibleRef.current) return
      const ahora = Date.now()
      // Throttle simple (1s): mousemove/wheel disparan decenas de eventos por segundo, no hace
      // falta clearTimeout+setTimeout en cada uno.
      if (ahora - ultimoReset < 1000) return
      ultimoReset = ahora
      reiniciarTemporizadorInactividad()
    }

    EVENTOS_ACTIVIDAD.forEach((evento) => window.addEventListener(evento, alDetectarActividad))
    return () => {
      EVENTOS_ACTIVIDAD.forEach((evento) => window.removeEventListener(evento, alDetectarActividad))
      limpiarTemporizadorInactividad()
      limpiarCuentaRegresiva()
    }
  }, [reiniciarTemporizadorInactividad, limpiarTemporizadorInactividad, limpiarCuentaRegresiva])

  return (
    <Dialog
      open={advertenciaVisible}
      maxWidth="xs"
      fullWidth
      slotProps={{ paper: { elevation: 3, sx: { borderRadius: 3 } } }}
    >
      <DialogContent sx={{ pt: 4, pb: 1, px: 4 }}>
        <Stack spacing={2} sx={{ alignItems: 'center', textAlign: 'center' }}>
          <Box
            sx={{
              width: 56,
              height: 56,
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              bgcolor: alpha(sdrercColors.warning, 0.12),
            }}
          >
            <AccessTimeOutlinedIcon sx={{ color: sdrercColors.warning, fontSize: 30 }} />
          </Box>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>
            Su sesión está por cerrarse
          </Typography>
          <Typography variant="body2" color="text.secondary">
            No se detectó actividad reciente. Por seguridad, la sesión se cerrará automáticamente en:
          </Typography>
          <Typography variant="h3" sx={{ color: sdrercColors.warning, fontWeight: 700, lineHeight: 1 }}>
            {segundosRestantes}s
          </Typography>
          <LinearProgress
            variant="determinate"
            value={(segundosRestantes / SEGUNDOS_CUENTA_REGRESIVA) * 100}
            sx={{
              width: '100%',
              height: 6,
              borderRadius: 3,
              bgcolor: alpha(sdrercColors.warning, 0.15),
              '& .MuiLinearProgress-bar': { bgcolor: sdrercColors.warning },
            }}
          />
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 4, pb: 3, pt: 2, justifyContent: 'center' }}>
        <Button variant="contained" onClick={continuarSesion} sx={{ minWidth: 220 }}>
          Continuar sesión
        </Button>
      </DialogActions>
    </Dialog>
  )
}
