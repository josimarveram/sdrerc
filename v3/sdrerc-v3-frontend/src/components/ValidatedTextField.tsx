import { useState } from 'react'
import { InputAdornment, TextField, Tooltip } from '@mui/material'
import type { TextFieldProps } from '@mui/material'
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined'
import ErrorOutlinedIcon from '@mui/icons-material/ErrorOutlined'
import { sdrercColors } from '../theme'

export type CampoValidator = (valorTexto: string) => string | null

interface ValidatedTextFieldProps extends Omit<TextFieldProps, 'error' | 'helperText' | 'onBlur' | 'color'> {
  /**
   * Devuelve un mensaje de error si el valor no es válido, o `null` si lo es. Si no se pasa, el
   * campo queda SIEMPRE neutral (nunca se pinta verde/rojo) — reservado para campos sin regla de
   * validación real (13/08/2026, pedido explícito del usuario: "no pintes de verde campos
   * opcionales vacíos ni campos sin validación específica").
   */
  validate?: CampoValidator
  /**
   * Error ya conocido desde afuera (ej. N° expediente SGD duplicado, detectado por el servidor vía
   * el botón "Validar", no por esta validación local instantánea) — tiene prioridad sobre
   * `validate` y se muestra sin esperar a que el usuario salga del campo, porque ya se sabe que es
   * inválido en el momento en que llega.
   */
  externalError?: string | null
  /**
   * Fuerza a mostrar el estado de validación (rojo si es inválido/vacío) sin esperar a que el
   * usuario salga del campo (18/08/2026, pedido explícito del usuario: al hacer clic en "Validar" o
   * "Registrar"/"Guardar", los campos obligatorios vacíos que el usuario nunca tocó deben verse en
   * rojo de inmediato, no solo los que sí llegó a enfocar y abandonar). Se combina con el `blur`
   * local: una vez `true`, el campo se comporta como si ya hubiera sido tocado, para siempre.
   */
  forceTouched?: boolean
}

/**
 * `TextField` con validación visual por campo (13/08/2026, pedido explícito del usuario, en
 * `RegistroExpedienteForm.tsx` — compartido por Registro manual, Edición manual y el panel de datos
 * inline, "usando el mismo componente de input validado para no duplicar lógica visual"):
 * - Borde verde (`sdrercColors.success`, ya usado en el resto del sistema para éxito — no se
 *   introduce un verde nuevo separado de la paleta) + ícono de check cuando el campo tiene una
 *   regla de validación real, el usuario ya salió de él (blur) y el valor es válido Y no está
 *   vacío (un campo opcional vacío nunca se pinta verde, solo neutral).
 * - Borde rojo (`sdrercColors.error`) + ícono de alerta + mensaje corto debajo (mismo `helperText`
 *   nativo de MUI) cuando el valor no pasa `validate` (tras blur) o cuando llega `externalError`.
 * - Sin color mientras el usuario todavía está escribiendo (recién se evalúa en `onBlur`, nunca en
 *   `onChange`) y sin color si el campo no tiene `validate` ni `externalError` en absoluto.
 * - Transición de color de borde de 180ms (ni instantánea/abrupta ni perceptible como lenta).
 */
export function ValidatedTextField({ validate, externalError, value, onBlur, sx, slotProps, forceTouched, ...props }: ValidatedTextFieldProps) {
  const [touchedState, setTouched] = useState(false)
  const touched = touchedState || Boolean(forceTouched)

  const texto = typeof value === 'string' ? value : value === null || value === undefined ? '' : String(value)
  const vacio = texto.trim() === ''
  const errorLocal = touched && validate ? validate(texto) : null
  const mensajeError = externalError ?? errorLocal
  const tieneError = Boolean(mensajeError)
  const esValido = touched && !tieneError && !vacio && (validate !== undefined || externalError !== undefined)

  const colorBorde = tieneError ? sdrercColors.error : esValido ? sdrercColors.success : undefined
  // El ícono de check/alerta se omite en campos `select`: MUI ya reserva ese mismo espacio (extremo
  // derecho del input) para su propia flecha de despliegue — agregar un segundo ícono ahí arriesga
  // superponerse visualmente con la flecha nativa, algo que no se puede verificar sin navegador.
  // El borde de color + `helperText` (abajo) sí se aplican igual en selects.
  const mostrarIconoAdornment = !props.select

  return (
    <TextField
      {...props}
      value={value}
      onBlur={(e) => {
        setTouched(true)
        onBlur?.(e)
      }}
      error={tieneError}
      helperText={tieneError ? mensajeError : props.helperText}
      slotProps={{
        ...slotProps,
        input: mostrarIconoAdornment
          ? {
              ...(slotProps?.input as object),
              endAdornment: tieneError ? (
                <InputAdornment position="end">
                  <Tooltip title={mensajeError ?? ''}>
                    <ErrorOutlinedIcon fontSize="small" sx={{ color: sdrercColors.error }} />
                  </Tooltip>
                </InputAdornment>
              ) : esValido ? (
                <InputAdornment position="end">
                  <CheckCircleOutlinedIcon fontSize="small" sx={{ color: sdrercColors.success }} />
                </InputAdornment>
              ) : (
                (slotProps?.input as { endAdornment?: React.ReactNode } | undefined)?.endAdornment
              ),
            }
          : slotProps?.input,
      }}
      sx={[
        {
          '& .MuiOutlinedInput-root': {
            transition: 'border-color 180ms ease',
            ...(colorBorde && {
              '& .MuiOutlinedInput-notchedOutline': { borderColor: colorBorde },
              '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: colorBorde },
              '&.Mui-focused .MuiOutlinedInput-notchedOutline': { borderColor: colorBorde },
            }),
          },
        },
        ...(Array.isArray(sx) ? sx : sx ? [sx] : []),
      ]}
    />
  )
}
