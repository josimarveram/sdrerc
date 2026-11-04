import { Box, Button, CircularProgress, Dialog, DialogContent, Grow, LinearProgress, Stack, Typography } from '@mui/material'
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined'
import RadioButtonUncheckedOutlinedIcon from '@mui/icons-material/RadioButtonUncheckedOutlined'
import CancelOutlinedIcon from '@mui/icons-material/CancelOutlined'
import type { CargaDiariaResultado } from '../api/cargaDiariaApi'
import { sdrercColors } from '../theme'

export type EstadoPasoConfirmacion = 'pendiente' | 'en_proceso' | 'completado' | 'error'

interface CargaDiariaConfirmarModalProps {
  open: boolean
  pasos: string[]
  /** Índice del paso "en curso" (0-based). Igual a `pasos.length` cuando TODOS ya quedaron completados. */
  pasoActual: number
  pasoConError: number | null
  errorMensaje: string | null
  resultado: CargaDiariaResultado | null
  onCerrar: () => void
}

function estadoDePaso(indice: number, pasoActual: number, pasoConError: number | null): EstadoPasoConfirmacion {
  if (pasoConError !== null) {
    if (indice === pasoConError) return 'error'
    return indice < pasoConError ? 'completado' : 'pendiente'
  }
  if (indice < pasoActual) return 'completado'
  if (indice === pasoActual) return 'en_proceso'
  return 'pendiente'
}

/** Ícono + color por estado de paso — mismo lenguaje visual que `StatusBadge`/`MetricCard` (paleta de `sdrercColors`, sin colores nuevos). */
function IconoPaso({ estado }: { estado: EstadoPasoConfirmacion }) {
  if (estado === 'completado') {
    return (
      <Grow in appear>
        <CheckCircleOutlinedIcon sx={{ color: sdrercColors.success }} fontSize="small" />
      </Grow>
    )
  }
  if (estado === 'en_proceso') {
    return <CircularProgress size={16} thickness={5} sx={{ color: sdrercColors.primary }} />
  }
  if (estado === 'error') {
    return <CancelOutlinedIcon sx={{ color: sdrercColors.error }} fontSize="small" />
  }
  return <RadioButtonUncheckedOutlinedIcon sx={{ color: sdrercColors.textSecondary }} fontSize="small" />
}

/**
 * Modal de progreso de "Confirmar carga" (Carga diaria, 18/08/2026, pedido explícito del usuario:
 * "efecto de progreso visual atractivo... propio de sistemas modernos de importación masiva, tipo
 * Stripe/Notion/Linear, en vez de un simple estado de 'cargando' genérico").
 *
 * `POST /api/registro/carga-diaria/confirmar` es UNA sola llamada síncrona (verificado en
 * `ExpedienteRegistroDAO.registrarCarga`: un único `Connection`/transacción que recorre todas las
 * filas candidatas insertando persona/expediente/solicitud/acta/documento/historial/alertas antes
 * de un solo `commit()` al final) — el backend no reporta avance incremental real, así que no se
 * agregó WebSocket/SSE/polling (habría sido sobre-diseño para una operación que hoy es una sola
 * llamada HTTP). En su lugar, este componente es puramente presentacional: recibe `pasoActual` ya
 * calculado por la página (`RegistroCargaDiariaPage.tsx`), que simula el avance con temporizadores
 * mientras espera la respuesta real y completa las etapas restantes de un tirón apenas la respuesta
 * llega — ver el Javadoc de `handleConfirmar` en esa página para el detalle del temporizado.
 *
 * Los 5 pasos (`PASOS_CONFIRMACION_CARGA_DIARIA`, definidos en la página) están nombrados según el
 * trabajo REAL que hace `registrarCarga` por fila, no una narrativa inventada: insertar personas,
 * crear el expediente/número, guardar acta+documento+historial, y registrar alertas de duplicado/
 * grupo familiar (`registrarAlertasCargaDiaria`).
 */
export function CargaDiariaConfirmarModal({
  open,
  pasos,
  pasoActual,
  pasoConError,
  errorMensaje,
  resultado,
  onCerrar,
}: CargaDiariaConfirmarModalProps) {
  const enProgreso = !resultado && !errorMensaje
  const porcentaje = Math.min(100, Math.round((pasoActual / pasos.length) * 100))

  return (
    <Dialog
      open={open}
      maxWidth="xs"
      fullWidth
      disableEscapeKeyDown={enProgreso}
      onClose={enProgreso ? undefined : onCerrar}
    >
      <DialogContent sx={{ p: 4 }}>
        {resultado ? (
          <Stack spacing={2} alignItems="center" sx={{ py: 2, textAlign: 'center' }}>
            <Grow in appear timeout={400}>
              <CheckCircleOutlinedIcon sx={{ color: sdrercColors.success, fontSize: 64 }} />
            </Grow>
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              ¡Carga completada!
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {resultado.registrados} expediente{resultado.registrados === 1 ? '' : 's'} registrado
              {resultado.registrados === 1 ? '' : 's'}
              {resultado.omitidos > 0 ? ` · ${resultado.omitidos} omitido${resultado.omitidos === 1 ? '' : 's'}` : ''}.
            </Typography>
          </Stack>
        ) : errorMensaje ? (
          <Stack spacing={2} alignItems="center" sx={{ py: 2, textAlign: 'center' }}>
            <CancelOutlinedIcon sx={{ color: sdrercColors.error, fontSize: 56 }} />
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              No se pudo completar la carga
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {errorMensaje}
            </Typography>
            <Button variant="contained" onClick={onCerrar} sx={{ mt: 1 }}>
              Cerrar
            </Button>
          </Stack>
        ) : (
          <Stack spacing={3}>
            <Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 0.5 }}>
                Confirmando carga diaria
              </Typography>
              <Typography variant="body2" color="text.secondary">
                No cierre esta ventana mientras se registran los expedientes.
              </Typography>
            </Box>

            <LinearProgress
              variant="determinate"
              value={porcentaje}
              sx={{ height: 6, borderRadius: 3, bgcolor: sdrercColors.backgroundDefault }}
            />

            <Stack spacing={1.5}>
              {pasos.map((label, indice) => {
                const estado = estadoDePaso(indice, pasoActual, pasoConError)
                return (
                  <Stack key={label} direction="row" spacing={1.5} alignItems="center">
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: 20, height: 20 }}>
                      <IconoPaso estado={estado} />
                    </Box>
                    <Typography
                      variant="body2"
                      sx={{
                        color: estado === 'pendiente' ? 'text.disabled' : 'text.primary',
                        fontWeight: estado === 'en_proceso' ? 600 : 400,
                        transition: 'color 180ms ease, font-weight 180ms ease',
                      }}
                    >
                      {label}
                    </Typography>
                  </Stack>
                )
              })}
            </Stack>
          </Stack>
        )}
      </DialogContent>
    </Dialog>
  )
}
