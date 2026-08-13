import type { ReactNode } from 'react'
import { Box, Paper, Typography } from '@mui/material'
import { sdrercColors } from '../theme'

/** Contenedor común para los 5 gráficos del Dashboard — mismo patrón que crearContenedorGrafico() en V2. */
export function ChartCardShell({ title, children }: { title: string; children: ReactNode }) {
  return (
    <Paper elevation={1} sx={{ p: 3, borderRadius: 2, height: 340, display: 'flex', flexDirection: 'column' }}>
      <Typography variant="subtitle1" sx={{ color: sdrercColors.textPrimary, fontWeight: 600, mb: 2 }}>
        {title}
      </Typography>
      <Box sx={{ flex: 1, minHeight: 0 }}>
        {children}
      </Box>
    </Paper>
  )
}
