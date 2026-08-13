import { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { useNavigate } from 'react-router-dom'
import {
  cargaTotal,
  enAnalisis,
  listarCargaAbogados,
  listarDocumentosPorAbogado,
  listarIdsUsuarioConVencimientoEnRango,
  type CargaLaboralAbogado,
  type CargaLaboralDocumento,
} from '../api/asignacionApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, diasBadgeTone } from '../components/StatusBadge'

type FiltroKpi = null | 'POR_VENCER' | 'VENCIDOS'

/**
 * Equivalente web de la sub-pestaña "Carga Abogados" de Asignación (V2, `cargaLaboralModel` en
 * JPanelAsignacionV2). Solo lectura, sin acciones de escritura — indicadores informativos para
 * apoyar la asignación, no bloquean ninguna decisión (mismo texto de ayuda que V2). Columnas
 * exactas: Abogado + 4 subcolumnas de la carga de Análisis (Por recibir/En análisis/Observado/
 * Carta intermedia). Supervisor, Por vencer y Vencidos NO son columnas — Supervisor es filtro,
 * Por vencer/Vencidos son KPIs clicables (mismo criterio que V2). Panel "Detalle de carga" al
 * doble clic (aquí como diálogo, libertad de diseño): N° Expediente/Etapa/Estado/Días,
 * exclusivamente carga de Análisis (sin Verificación/Ejecución), igual que V2.
 */
export function AsignacionCargaAbogadosPage() {
  const navigate = useNavigate()
  const [cargas, setCargas] = useState<CargaLaboralAbogado[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState('')
  const [hasta, setHasta] = useState('')
  const [abogado, setAbogado] = useState('')
  const [supervisor, setSupervisor] = useState('')
  const [limite, setLimite] = useState(100)
  const [idsRangoFecha, setIdsRangoFecha] = useState<Set<number> | null>(null)
  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)

  const [detalleAbogado, setDetalleAbogado] = useState<CargaLaboralAbogado | null>(null)
  const [detalleDocumentos, setDetalleDocumentos] = useState<CargaLaboralDocumento[]>([])
  const [detalleLoading, setDetalleLoading] = useState(false)

  function cargar() {
    setLoading(true)
    setError(null)
    listarCargaAbogados()
      .then(setCargas)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    cargar()
  }, [])

  async function handleBuscar() {
    setFiltroKpi(null)
    if (desde || hasta) {
      setLoading(true)
      try {
        const ids = await listarIdsUsuarioConVencimientoEnRango(desde, hasta)
        setIdsRangoFecha(new Set(ids))
      } catch (e) {
        setError(e instanceof ApiError ? e.message : 'No se pudo aplicar el filtro de fecha.')
      } finally {
        setLoading(false)
      }
    } else {
      setIdsRangoFecha(null)
    }
  }

  function handleLimpiar() {
    setTexto('')
    setDesde('')
    setHasta('')
    setAbogado('')
    setSupervisor('')
    setLimite(100)
    setIdsRangoFecha(null)
    setFiltroKpi(null)
  }

  const abogados = useMemo(() => Array.from(new Set(cargas.map((c) => c.abogado))).sort(), [cargas])
  const supervisores = useMemo(
    () => Array.from(new Set(cargas.map((c) => c.supervisor).filter(Boolean))).sort(),
    [cargas],
  )

  const filas = useMemo(() => {
    const textoNorm = texto.trim().toUpperCase()
    let resultado = cargas.filter((item) => {
      if (textoNorm && !item.abogado.toUpperCase().includes(textoNorm)) return false
      if (abogado && item.abogado !== abogado) return false
      if (supervisor && item.supervisor !== supervisor) return false
      if (idsRangoFecha && !idsRangoFecha.has(item.idUsuario)) return false
      return true
    })
    if (filtroKpi === 'POR_VENCER') resultado = resultado.filter((c) => c.porVencer > 0)
    if (filtroKpi === 'VENCIDOS') resultado = resultado.filter((c) => c.vencidos > 0)
    return resultado.slice(0, limite)
  }, [cargas, texto, abogado, supervisor, idsRangoFecha, filtroKpi, limite])

  const kpis = useMemo(() => {
    let conCarga = 0
    let sinCarga = 0
    let solicitudes = 0
    let porVencer = 0
    let vencidos = 0
    for (const item of cargas) {
      solicitudes += cargaTotal(item)
      porVencer += item.porVencer
      vencidos += item.vencidos
      if (enAnalisis(item) > 0) conCarga++
      else sinCarga++
    }
    return { abogados: cargas.length, conCarga, sinCarga, solicitudes, porVencer, vencidos }
  }, [cargas])

  function alternarFiltro(valor: FiltroKpi) {
    setFiltroKpi((prev) => (prev === valor ? null : valor))
  }

  function abrirDetalle(item: CargaLaboralAbogado) {
    setDetalleAbogado(item)
    setDetalleLoading(true)
    listarDocumentosPorAbogado(item.idUsuario)
      .then(setDetalleDocumentos)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudo cargar el detalle de carga.'))
      .finally(() => setDetalleLoading(false))
  }

  const columnas: GridColDef<CargaLaboralAbogado>[] = [
    { field: 'abogado', headerName: 'Abogado', flex: 1, minWidth: 220 },
    { field: 'analisisPorRecibir', headerName: 'Por recibir', width: 120 },
    { field: 'analisisEnProceso', headerName: 'En análisis', width: 120 },
    { field: 'analisisObservado', headerName: 'Observado', width: 120 },
    { field: 'analisisCartaIntermedia', headerName: 'Carta intermedia', width: 150 },
  ]

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4 }}>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h6" sx={{ color: sdrercColors.primary }}>
            Carga Abogados
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Indicadores informativos para apoyar la asignación; no bloquean la decisión.
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
            label="Buscar abogado"
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleBuscar()}
            sx={{ minWidth: 260 }}
          />
          <Button variant="contained" onClick={handleBuscar} disabled={loading}>
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
          <TextField select label="Abogado" size="small" sx={{ minWidth: 220 }} value={abogado} onChange={(e) => setAbogado(e.target.value)}>
            <MenuItem value="">Todos</MenuItem>
            {abogados.map((a) => (
              <MenuItem key={a} value={a}>
                {a}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Supervisor"
            size="small"
            sx={{ minWidth: 220 }}
            value={supervisor}
            onChange={(e) => setSupervisor(e.target.value)}
          >
            <MenuItem value="">Todos</MenuItem>
            {supervisores.map((s) => (
              <MenuItem key={s} value={s}>
                {s}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Límite"
            type="number"
            size="small"
            sx={{ width: 120 }}
            value={limite}
            onChange={(e) => setLimite(Number(e.target.value) || 100)}
          />
        </Stack>
      </Stack>

      <Stack direction="row" spacing={2} sx={{ mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <MetricCard label="Abogados" value={kpis.abogados} caption="Activos" color={sdrercColors.info} />
        <MetricCard label="Con carga" value={kpis.conCarga} caption="Con solicitudes en Análisis" color={sdrercColors.success} />
        <MetricCard label="Sin carga" value={kpis.sinCarga} caption="Sin solicitudes en Análisis" color={sdrercColors.warning} />
        <MetricCard label="Solicitudes" value={kpis.solicitudes} caption="Carga total asignada" color={sdrercColors.primary} />
        <Box onClick={() => alternarFiltro('POR_VENCER')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Por vencer" value={kpis.porVencer} caption="0 a 5 días hábiles" color={sdrercColors.warning} />
        </Box>
        <Box onClick={() => alternarFiltro('VENCIDOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Vencidos" value={kpis.vencidos} caption="Plazo excedido" color={sdrercColors.error} />
        </Box>
      </Stack>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2 }}>
        <DataGrid
          rows={filas}
          columns={columnas}
          getRowId={(row) => row.idUsuario}
          loading={loading}
          density="compact"
          autoHeight
          disableRowSelectionOnClick
          showToolbar
          onRowDoubleClick={(params) => abrirDetalle(params.row as CargaLaboralAbogado)}
          pageSizeOptions={[25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 25 } } }}
        />
      </Box>

      <Dialog open={detalleAbogado !== null} onClose={() => setDetalleAbogado(null)} maxWidth="md" fullWidth>
        <DialogTitle>
          Detalle de carga
          {detalleAbogado && (
            <Typography variant="body2" color="text.secondary">
              {detalleAbogado.abogado}
            </Typography>
          )}
        </DialogTitle>
        <DialogContent>
          {detalleLoading ? (
            <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
              Cargando...
            </Typography>
          ) : detalleDocumentos.length === 0 ? (
            <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
              Sin expedientes de Análisis para este abogado.
            </Typography>
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>N° Expediente</TableCell>
                    <TableCell>Etapa</TableCell>
                    <TableCell>Estado</TableCell>
                    <TableCell>Días</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {detalleDocumentos.map((doc) => (
                    <TableRow key={doc.idExpediente}>
                      <TableCell>{doc.numeroExpediente || '-'}</TableCell>
                      <TableCell>{doc.etapa || '-'}</TableCell>
                      <TableCell>{doc.estado || '-'}</TableCell>
                      <TableCell>
                        <StatusBadge
                          label={doc.diasRestantes === null ? '-' : String(doc.diasRestantes)}
                          tone={diasBadgeTone(doc.diasRestantes)}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2 }}>
            Solo carga de Análisis (Por recibir/En análisis/Observado/Carta intermedia); no incluye Verificación ni Ejecución.
          </Typography>
        </DialogContent>
      </Dialog>
    </Box>
  )
}
