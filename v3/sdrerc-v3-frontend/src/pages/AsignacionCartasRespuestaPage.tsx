import { useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, MenuItem, Stack, TextField, Typography } from '@mui/material'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { useNavigate } from 'react-router-dom'
import { listarCartasRespuestaPendientes, type AsignacionCartaRespuesta } from '../api/asignacionApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, diasBadgeTone } from '../components/StatusBadge'

type FiltroEstado = '' | 'Pendiente de Respuesta' | 'Edicto Publicado'

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

/**
 * Equivalente web de la sub-pestaña "Cartas de respuesta" de Asignación (V2,
 * `bandejaCartasRespuestaModel` en JPanelAsignacionV2). Alcance de este incremento: **solo
 * lectura** — listado + filtros + 5 KPIs (Cartas, Con acuse, Pendientes, Publicación, Vencimiento),
 * mismas 11 columnas exactas y mismo orden que V2 (Días, Fecha Vencimiento, N° expediente, N°
 * expediente SGD, Titular, Tipo documento, N° Documento, Fecha Acuse, Fecha Publ. Notif., Fecha
 * Publ. Edicto, Estado). El botón "Registrar Asignación" de V2 (deriva el expediente de vuelta a
 * Análisis) NO se portó todavía: depende de gestión jerárquica de documentos analizados
 * (padre/hijo), territorio de Análisis (Fase 4), no construido en V3.
 */
export function AsignacionCartasRespuestaPage() {
  const navigate = useNavigate()
  const [items, setItems] = useState<AsignacionCartaRespuesta[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState('')
  const [hasta, setHasta] = useState('')
  const [estado, setEstado] = useState<FiltroEstado>('')

  function cargar() {
    setLoading(true)
    setError(null)
    listarCartasRespuestaPendientes()
      .then(setItems)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    cargar()
  }, [])

  function handleLimpiar() {
    setTexto('')
    setDesde('')
    setHasta('')
    setEstado('')
  }

  const filas = useMemo(() => {
    const textoNorm = texto.trim().toUpperCase()
    return items.filter((item) => {
      if (textoNorm) {
        const coincide = [item.numeroExpediente, item.numeroExpedienteSgd, item.titular, item.tipoDocumentoNombre, item.numeroDocumento]
          .some((v) => v.toUpperCase().includes(textoNorm))
        if (!coincide) return false
      }
      if (estado && item.estadoCarta !== estado) return false
      if (desde && (!item.fechaVencimiento || item.fechaVencimiento.slice(0, 10) < desde)) return false
      if (hasta && (!item.fechaVencimiento || item.fechaVencimiento.slice(0, 10) > hasta)) return false
      return true
    })
  }, [items, texto, estado, desde, hasta])

  const kpis = useMemo(() => {
    let conAcuse = 0
    let pendientes = 0
    let publicacion = 0
    let vencimiento = 0
    for (const item of items) {
      if (item.fechaAcuse) conAcuse++
      const confirmacion = item.confirmacionRespuesta.trim().toUpperCase()
      if (confirmacion !== 'SI' && confirmacion !== 'NO') pendientes++
      if (item.requierePublicacion) publicacion++
      if (item.diasVencimiento !== null) vencimiento++
    }
    return { total: items.length, conAcuse, pendientes, publicacion, vencimiento }
  }, [items])

  // Mismo orden exacto que bandejaCartasRespuestaModel en V2 (JPanelAsignacionV2).
  const columnas: GridColDef<AsignacionCartaRespuesta>[] = [
    {
      field: 'diasVencimiento',
      headerName: 'Días',
      width: 90,
      renderCell: (params) => (
        <StatusBadge
          label={params.value === null || params.value === undefined ? '-' : String(params.value)}
          tone={diasBadgeTone(params.value as number | null)}
        />
      ),
    },
    {
      field: 'fechaVencimiento',
      headerName: 'Fecha Vencimiento',
      width: 140,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    { field: 'numeroExpediente', headerName: 'N° expediente', width: 190 },
    { field: 'numeroExpedienteSgd', headerName: 'N° expediente SGD', width: 160 },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    { field: 'tipoDocumentoNombre', headerName: 'Tipo documento', width: 170 },
    { field: 'numeroDocumento', headerName: 'N° Documento', width: 150 },
    {
      field: 'fechaAcuse',
      headerName: 'Fecha Acuse',
      width: 130,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    {
      field: 'fechaPublicacionNotificacion',
      headerName: 'Fecha Publ. Notif.',
      width: 150,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    {
      field: 'fechaPublicacionEdicto',
      headerName: 'Fecha Publ. Edicto',
      width: 150,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    { field: 'estadoCarta', headerName: 'Estado', width: 170 },
  ]

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4 }}>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h6" sx={{ color: sdrercColors.primary }}>
            Cartas de respuesta
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Documentos analizados con respuesta del ciudadano pendiente, dentro de Asignación.
          </Typography>
        </Box>
        <Button variant="outlined" onClick={() => navigate('/asignacion')}>
          Bandeja Asignación
        </Button>
      </Stack>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Stack spacing={2} sx={{ mb: 3 }}>
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
          <TextField
            label="Buscar (N° expediente, SGD, titular, documento...)"
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            sx={{ minWidth: 320 }}
          />
          <Button variant="contained" onClick={cargar} disabled={loading}>
            Buscar
          </Button>
          <Button variant="outlined" onClick={handleLimpiar} disabled={loading}>
            Limpiar
          </Button>
        </Stack>
        <Stack direction="row" spacing={2}>
          <TextField
            label="Fecha vencimiento desde"
            type="date"
            size="small"
            value={desde}
            onChange={(e) => setDesde(e.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField
            label="Fecha vencimiento hasta"
            type="date"
            size="small"
            value={hasta}
            onChange={(e) => setHasta(e.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
        </Stack>
        <Stack direction="row" spacing={2}>
          <TextField
            select
            label="Estado"
            size="small"
            sx={{ minWidth: 220 }}
            value={estado}
            onChange={(e) => setEstado(e.target.value as FiltroEstado)}
          >
            <MenuItem value="">Todos los estados</MenuItem>
            <MenuItem value="Pendiente de Respuesta">Pendiente de Respuesta</MenuItem>
            <MenuItem value="Edicto Publicado">Edicto Publicado</MenuItem>
          </TextField>
        </Stack>
      </Stack>

      <Stack direction="row" spacing={2} sx={{ mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <MetricCard label="Cartas" value={kpis.total} caption="Con respuesta requerida" color={sdrercColors.info} />
        <MetricCard label="Con acuse" value={kpis.conAcuse} caption="Listas para respuesta" color={sdrercColors.success} />
        <MetricCard label="Pendientes" value={kpis.pendientes} caption="Sin confirmación final" color={sdrercColors.warning} />
        <MetricCard label="Publicación" value={kpis.publicacion} caption="Preparadas para publicar" color={sdrercColors.primary} />
        <MetricCard label="Vencimiento" value={kpis.vencimiento} caption="Plazo de respuesta o publicación activo" color={sdrercColors.warning} />
      </Stack>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2 }}>
        <DataGrid
          rows={filas}
          columns={columnas}
          getRowId={(row) => row.idDocumentoAnalizado}
          loading={loading}
          density="compact"
          autoHeight
          disableRowSelectionOnClick
          showToolbar
          pageSizeOptions={[25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 25 } } }}
        />
      </Box>
    </Box>
  )
}
