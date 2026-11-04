import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { Box, Typography } from '@mui/material'
import { sdrercColors } from '../theme'
import { singleSeriesBarColor } from './chartPalette'
import { ChartCardShell } from './ChartCardShell'

interface BarChartCardProps {
  title: string
  data: { etiqueta: string; total: number }[]
  horizontal?: boolean
  emptyLabel?: string
}

/**
 * Barras de una sola serie (un color uniforme — la longitud de la barra ya es el dato, ver
 * chartPalette.ts). Sin leyenda (un solo color no la necesita, el título ya dice qué se mide).
 * Usado por "Expedientes por etapa" (vertical) y "Carga por abogado" (horizontal).
 */
export function BarChartCard({ title, data, horizontal = false, emptyLabel = 'Sin datos en el periodo' }: BarChartCardProps) {
  return (
    <ChartCardShell title={title}>
      {data.length === 0 ? (
        <EmptyState label={emptyLabel} />
      ) : (
        <ResponsiveContainer width="100%" height="100%">
          <BarChart
            data={data}
            layout={horizontal ? 'vertical' : 'horizontal'}
            margin={{ top: 4, right: 16, left: 0, bottom: 4 }}
          >
            <CartesianGrid stroke={sdrercColors.divider} strokeDasharray="0" vertical={!horizontal} horizontal={horizontal} />
            {horizontal ? (
              <>
                <XAxis type="number" allowDecimals={false} tick={{ fill: sdrercColors.textSecondary, fontSize: 12 }} axisLine={{ stroke: sdrercColors.dividerStrong }} tickLine={false} />
                <YAxis
                  type="category"
                  dataKey="etiqueta"
                  width={110}
                  tick={{ fill: sdrercColors.textSecondary, fontSize: 12 }}
                  axisLine={{ stroke: sdrercColors.dividerStrong }}
                  tickLine={false}
                />
              </>
            ) : (
              <>
                <XAxis dataKey="etiqueta" tick={{ fill: sdrercColors.textSecondary, fontSize: 12 }} axisLine={{ stroke: sdrercColors.dividerStrong }} tickLine={false} />
                <YAxis allowDecimals={false} tick={{ fill: sdrercColors.textSecondary, fontSize: 12 }} axisLine={{ stroke: sdrercColors.dividerStrong }} tickLine={false} />
              </>
            )}
            <Tooltip
              cursor={{ fill: sdrercColors.softGray }}
              contentStyle={{ borderRadius: 8, borderColor: sdrercColors.divider, fontSize: 13 }}
            />
            <Bar
              dataKey="total"
              fill={singleSeriesBarColor}
              radius={horizontal ? [0, 4, 4, 0] : [4, 4, 0, 0]}
              barSize={20}
              maxBarSize={24}
            />
          </BarChart>
        </ResponsiveContainer>
      )}
    </ChartCardShell>
  )
}

export function EmptyState({ label }: { label: string }) {
  return (
    <Box sx={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
    </Box>
  )
}
