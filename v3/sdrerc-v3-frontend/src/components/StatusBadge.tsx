import { Chip } from '@mui/material'
import { sdrercColors } from '../theme'

/**
 * Badge de estado/alerta reutilizable — equivalente web de StatusBadgeV2 (V2). Fondo suave +
 * texto en el color "fuerte" correspondiente, mismo patrón que Anexo A/B del plan de migración.
 * NOTA de fidelidad: el cálculo de color de "Días" aquí es una aproximación simple (vencido /
 * por vencer <=5 días / verde), no replica todavía el umbral por porcentaje de
 * PLAZO_CONFIGURACION (porcentajeVerdeDesde/AmarilloDesde/RojoDesde) que usa
 * StatusBadgeV2.forDias en V2 — pendiente de portar cuando se necesite con más precisión.
 */

const TONES = {
  success: { bg: sdrercColors.softGreen, fg: sdrercColors.success },
  warning: { bg: sdrercColors.softOrange, fg: sdrercColors.warning },
  error: { bg: sdrercColors.softRed, fg: sdrercColors.error },
  info: { bg: sdrercColors.softBlue, fg: sdrercColors.info },
  neutral: { bg: sdrercColors.softGray, fg: sdrercColors.textSecondary },
} as const

export function StatusBadge({ label, tone }: { label: string; tone: keyof typeof TONES }) {
  const { bg, fg } = TONES[tone]
  return (
    <Chip
      label={label}
      size="small"
      sx={{ bgcolor: bg, color: fg, fontWeight: 600, borderRadius: 1 }}
    />
  )
}

export function diasBadgeTone(dias: number | null): keyof typeof TONES {
  if (dias === null) return 'neutral'
  if (dias < 0) return 'error'
  if (dias <= 5) return 'warning'
  return 'success'
}

export function alertaBadgeTone(alerta: string): keyof typeof TONES {
  switch (alerta) {
    case 'Potencial duplicado':
      return 'warning'
    case 'Posible Grupo Familiar':
      return 'info'
    case 'Duplicado confirmado':
      return 'neutral'
    default:
      return 'success'
  }
}
