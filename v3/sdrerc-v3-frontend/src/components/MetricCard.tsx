import { Paper, Typography } from '@mui/material'
import { sdrercColors } from '../theme'

interface MetricCardProps {
  label: string
  value: number
  caption: string
  color: string
}

/**
 * Stat tile reutilizable (Anexo A/B del plan de migracion): equivalente web de MetricCardV2 (V2).
 * Sentence case sin dos puntos finales, valor en cifras proporcionales (nunca tabular-nums en un
 * numero grande y aislado, ver skill dataviz), color solo en el valor — la etiqueta y el
 * subtitulo siempre en tinta de texto, nunca en el color de la serie.
 */
export function MetricCard({ label, value, caption, color }: MetricCardProps) {
  return (
    <Paper elevation={1} sx={{ p: 2.5, borderRadius: 2, flex: 1, minWidth: 160 }}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="h4" sx={{ color, my: 0.5, fontVariantNumeric: 'proportional-nums' }}>
        {value.toLocaleString('es-PE')}
      </Typography>
      <Typography variant="body2" sx={{ color: sdrercColors.textSecondary }}>
        {caption}
      </Typography>
    </Paper>
  )
}
