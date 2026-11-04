import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { sdrercColors } from '../theme'
import { trendSeriesColors } from './chartPalette'
import { ChartCardShell } from './ChartCardShell'
import { EmptyState } from './BarChartCard'

interface TrendPoint {
  mes: string // ya formateado para mostrar, ej. "ago. 2026"
  ingresados: number
  cerrados: number
}

/** Línea de 2 series con leyenda (siempre presente desde 2 series en adelante). */
export function TrendLineChartCard({ title, data }: { title: string; data: TrendPoint[] }) {
  return (
    <ChartCardShell title={title}>
      {data.length === 0 ? (
        <EmptyState label="Sin datos en el periodo" />
      ) : (
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data} margin={{ top: 4, right: 16, left: 0, bottom: 4 }}>
            <CartesianGrid stroke={sdrercColors.divider} vertical={false} />
            <XAxis dataKey="mes" tick={{ fill: sdrercColors.textSecondary, fontSize: 12 }} axisLine={{ stroke: sdrercColors.dividerStrong }} tickLine={false} />
            <YAxis allowDecimals={false} tick={{ fill: sdrercColors.textSecondary, fontSize: 12 }} axisLine={{ stroke: sdrercColors.dividerStrong }} tickLine={false} />
            <Tooltip contentStyle={{ borderRadius: 8, borderColor: sdrercColors.divider, fontSize: 13 }} />
            <Legend wrapperStyle={{ fontSize: 12, color: sdrercColors.textSecondary }} />
            <Line
              type="monotone"
              dataKey="ingresados"
              name="Ingresados"
              stroke={trendSeriesColors.ingresados}
              strokeWidth={2}
              dot={{ r: 4, fill: trendSeriesColors.ingresados, stroke: sdrercColors.backgroundPaper, strokeWidth: 2 }}
              activeDot={{ r: 5 }}
            />
            <Line
              type="monotone"
              dataKey="cerrados"
              name="Cerrados"
              stroke={trendSeriesColors.cerrados}
              strokeWidth={2}
              dot={{ r: 4, fill: trendSeriesColors.cerrados, stroke: sdrercColors.backgroundPaper, strokeWidth: 2 }}
              activeDot={{ r: 5 }}
            />
          </LineChart>
        </ResponsiveContainer>
      )}
    </ChartCardShell>
  )
}
