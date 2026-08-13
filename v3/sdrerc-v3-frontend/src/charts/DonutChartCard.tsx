import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import { sdrercColors } from '../theme'
import { ChartCardShell } from './ChartCardShell'
import { EmptyState } from './BarChartCard'

interface DonutChartCardProps {
  title: string
  data: { etiqueta: string; total: number }[]
  colorFor: (etiqueta: string) => string
}

/**
 * Dona de identidad (parte del todo, "a simple vista", ≤6 segmentos esperados) — leyenda siempre
 * presente porque hay 2+ categorías, con separación de 2px entre segmentos (paddingAngle) en vez
 * de un borde dibujado.
 */
export function DonutChartCard({ title, data, colorFor }: DonutChartCardProps) {
  return (
    <ChartCardShell title={title}>
      {data.length === 0 ? (
        <EmptyState label="Sin datos en el periodo" />
      ) : (
        <ResponsiveContainer width="100%" height="100%">
          <PieChart margin={{ top: 4, right: 4, left: 4, bottom: 4 }}>
            <Pie
              data={data}
              dataKey="total"
              nameKey="etiqueta"
              innerRadius="55%"
              outerRadius="80%"
              paddingAngle={2}
              stroke={sdrercColors.backgroundPaper}
              strokeWidth={2}
            >
              {data.map((item) => (
                <Cell key={item.etiqueta} fill={colorFor(item.etiqueta)} />
              ))}
            </Pie>
            <Tooltip contentStyle={{ borderRadius: 8, borderColor: sdrercColors.divider, fontSize: 13 }} />
            <Legend
              layout="vertical"
              verticalAlign="middle"
              align="right"
              wrapperStyle={{ fontSize: 12, color: sdrercColors.textSecondary }}
            />
          </PieChart>
        </ResponsiveContainer>
      )}
    </ChartCardShell>
  )
}
