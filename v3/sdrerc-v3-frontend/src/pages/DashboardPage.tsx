import { useCallback, useEffect, useState } from 'react'
import { Alert, Box, Button, Stack, TextField, Typography } from '@mui/material'
import { obtenerDashboard, type DashboardData } from '../api/dashboardApi'
import { ApiError } from '../api/http'
import { useAuth } from '../auth/AuthContext'
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { BarChartCard } from '../charts/BarChartCard'
import { DonutChartCard } from '../charts/DonutChartCard'
import { TrendLineChartCard } from '../charts/TrendLineChartCard'
import { createCategoricalColorAssigner, estadoNotificacionColors } from '../charts/chartPalette'

const ROL_ADMIN_SISTEMA = 'ADMIN_SISTEMA'

function primerDiaMesActualISO(): string {
  const hoy = new Date()
  return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-01`
}

function hoyISO(): string {
  return new Date().toISOString().slice(0, 10)
}

function formatearMes(mesIso: string): string {
  const [anio, mes] = mesIso.split('-').map(Number)
  return new Date(anio, mes - 1, 1).toLocaleDateString('es-PE', { month: 'short', year: 'numeric' })
}

/**
 * Equivalente web de JPanelDashboardV2 (V2): mismos 5 KPIs, mismos 5 gráficos, mismo rango de
 * fechas por defecto (día 1 del mes actual -> hoy). Exclusivo ADMIN_SISTEMA — segunda capa de
 * defensa aquí (además del filtro de ruta), igual criterio que
 * JPanelDashboardV2.tieneAcceso()/DashboardService.tieneAcceso() en V2: si alguien llega aquí sin
 * el rol, se muestra un mensaje de acceso restringido en vez de datos o una excepción.
 */
export function DashboardPage() {
  const { session } = useAuth()
  const [desde, setDesde] = useState(primerDiaMesActualISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [datos, setDatos] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const tieneAcceso = session?.roles.includes(ROL_ADMIN_SISTEMA) ?? false

  const cargar = useCallback(() => {
    setLoading(true)
    setError(null)
    obtenerDashboard(desde, hasta)
      .then(setDatos)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
      .finally(() => setLoading(false))
  }, [desde, hasta])

  useEffect(() => {
    if (tieneAcceso) cargar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tieneAcceso])

  if (!tieneAcceso) {
    return (
      <Box sx={{ p: 4 }}>
        <Alert severity="warning">El Dashboard es exclusivo para administradores del sistema.</Alert>
      </Box>
    )
  }

  const resultadoAnalisisColor = createCategoricalColorAssigner()

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4 }}>
      <Typography variant="h6" sx={{ color: sdrercColors.primary }}>
        Dashboard gerencial
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Vista consolidada de expedientes por etapa, resultados y carga de trabajo.
      </Typography>

      {/* Una sola fila de filtros por encima de todo lo que afecta (ver skill dataviz: nunca
          filtros dentro de cada tarjeta de gráfico individual). */}
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center', mb: 3 }}>
        <TextField
          label="Fecha desde"
          type="date"
          size="small"
          value={desde}
          onChange={(e) => setDesde(e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
        />
        <TextField
          label="Fecha hasta"
          type="date"
          size="small"
          value={hasta}
          onChange={(e) => setHasta(e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
        />
        <Button variant="contained" onClick={cargar} disabled={loading}>
          Refrescar
        </Button>
      </Stack>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {datos && (
        // Opacidad reducida mientras refresca en vez de un skeleton (evita el salto de layout).
        <Box sx={{ opacity: loading ? 0.6 : 1, transition: 'opacity 0.15s' }}>
          <Stack direction="row" spacing={2} sx={{ mb: 3, flexWrap: 'wrap', gap: 2 }}>
            <MetricCard label="Expedientes activos" value={datos.resumen.activos} caption="En trámite" color={sdrercColors.info} />
            <MetricCard label="Vencidos" value={datos.resumen.vencidos} caption="Plazo excedido" color={sdrercColors.error} />
            <MetricCard label="Por vencer" value={datos.resumen.porVencer} caption="0 a 5 días" color={sdrercColors.warning} />
            <MetricCard label="Ingresados" value={datos.resumen.ingresadosPeriodo} caption="En el periodo" color={sdrercColors.primary} />
            <MetricCard label="Cerrados" value={datos.resumen.cerradosPeriodo} caption="En el periodo" color={sdrercColors.success} />
          </Stack>

          <Box
            sx={{
              display: 'grid',
              gridTemplateColumns: { xs: '1fr', md: '1fr 1fr', xl: '1fr 1fr 1fr' },
              gap: 3,
            }}
          >
            <BarChartCard title="Expedientes por etapa" data={datos.porEtapa} />
            <DonutChartCard
              title="Resultado de análisis (periodo)"
              data={datos.resultadosAnalisis}
              colorFor={resultadoAnalisisColor}
            />
            <BarChartCard
              title="Carga por abogado (top 10)"
              data={datos.cargaAbogados.map((c) => ({
                etiqueta: c.abogado,
                total: c.analisisPorRecibir + c.analisisEnProceso + c.analisisObservado + c.analisisCartaIntermedia + c.enVerificacion + c.enEjecucion,
              }))}
              horizontal
            />
            <TrendLineChartCard
              title="Ingresados vs. cerrados por mes"
              data={datos.tendenciaMensual.map((t) => ({
                mes: formatearMes(t.mes),
                ingresados: t.ingresados,
                cerrados: t.cerrados,
              }))}
            />
            <DonutChartCard
              title="Estado final de notificación"
              data={datos.estadoNotificacion}
              colorFor={(etiqueta) => estadoNotificacionColors[etiqueta] ?? sdrercColors.dividerStrong}
            />
          </Box>
        </Box>
      )}
    </Box>
  )
}
