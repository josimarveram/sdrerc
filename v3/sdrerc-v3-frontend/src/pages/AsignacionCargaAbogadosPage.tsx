import { useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, LinearProgress, MenuItem, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Tooltip, Typography } from '@mui/material'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import {
  cargaTotal,
  enAnalisis,
  listarCargaAbogados,
  listarDocumentosPorAbogado,
  listarIdsUsuarioConSolicitudEnRango,
  type CargaLaboralAbogado,
  type CargaLaboralDocumento,
} from '../api/asignacionApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, diasBadgeTone } from '../components/StatusBadge'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { primerDiaMesActualISO, hoyISO } from '../utils/fechas'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ASIGNACION_TABS } from '../layout/moduleTabs'

type FiltroKpi = null | 'CON_CARGA' | 'SIN_CARGA' | 'POR_VENCER' | 'VENCIDOS'

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DETALLE = 'rgb(10, 118, 145)'

/**
 * Equivalente web de la sub-pestaña "Carga Abogados" de Asignación (V2, `cargaLaboralModel` en
 * JPanelAsignacionV2). Solo lectura, sin acciones de escritura — indicadores informativos para
 * apoyar la asignación, no bloquean ninguna decisión (mismo texto de ayuda que V2). Columnas
 * exactas: Abogado + 4 subcolumnas de la carga de Análisis (Por recibir/En análisis/Observado/
 * Carta intermedia). Supervisor, Por vencer y Vencidos NO son columnas — Supervisor es filtro,
 * Por vencer/Vencidos son KPIs clicables (mismo criterio que V2).
 *
 * 24/08/2026, pedido explícito del usuario ("mejora... como un experto en gestión de expedientes"),
 * sobre el incremento de solo-lectura ya construido (13/08/2026) — 4 mejoras deliberadas, sin
 * cambios de backend (la data ya tenía paridad completa con V2, verificado antes de tocar nada):
 * 1. **"Detalle de carga" migrado de `Dialog` a `WorkspaceTabsDrawer`** — mismo patrón de panel
 *    multipestañas ya usado en el resto de bandejas de V3 (Registro/Asignación/Análisis/
 *    Verificación/Ejecución/Notificación/Cartas de respuesta); antes era el único panel de detalle
 *    de todo el módulo que usaba un `Dialog` modal suelto, inconsistente con "los últimos diseños e
 *    interfaces de la V3" que pidió el usuario.
 * 2. **Columna "Carga relativa"** (mini barra de progreso, sin equivalente en V2): compara la carga
 *    de Análisis de cada abogado (`enAnalisis(item)`) contra el máximo del conjunto ya filtrado —
 *    permite ver de un vistazo quién está más cargado sin tener que leer/sumar las 4 subcolumnas
 *    numéricas fila por fila.
 * 3. **KPI "Carga promedio"** (7mo KPI, sin equivalente en V2): `solicitudes / abogados` — punto de
 *    referencia rápido para que el supervisor juzgue si un abogado puntual está por encima/debajo
 *    del promedio del equipo, sin tener que calcularlo mentalmente.
 * 4. **Resaltado de fila con vencidos** (`getRowClassName`, mismo mecanismo ya usado para filas
 *    asociadas en otras bandejas): un abogado con `vencidos > 0` se ve en rojo suave de entrada, sin
 *    tener que hacer clic en el KPI "Vencidos" primero para notarlo. Orden inicial de la grilla
 *    también cambia a "En análisis" descendente (antes sin orden explícito) — el abogado con más
 *    carga activa aparece primero, coherente con el propósito de esta bandeja (apoyar decisiones de
 *    asignación).
 *
 * Bug real corregido (24/08/2026): el filtro de fecha de esta bandeja apuntaba a
 * `EXPEDIENTE.fecha_vencimiento` — un dato por naturaleza FUTURO (los expedientes vigentes vencen
 * semanas/meses adelante) — con el default "01 del mes actual → hoy" (orientado al pasado) esto
 * dejaba la grilla siempre vacía. Se probó primero quitando el default; **25/08/2026, pedido
 * explícito del usuario**: el filtro en sí debía apuntar a Fecha Solicitud
 * (`EXPEDIENTE_SOLICITUD.fecha_recepcion`, mismo campo que ya usa "Fecha Solicitud" en
 * Registro/Asignación), no a Fecha Vencimiento — al ser un campo orientado al pasado, el default
 * "01 del mes actual → hoy" vuelve a tener sentido y se restauró (`listarIdsUsuarioConSolicitudEnRango`,
 * antes `...ConVencimientoEnRango`).
 */
export function AsignacionCargaAbogadosPage() {
  const [cargas, setCargas] = useState<CargaLaboralAbogado[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaMesActualISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [abogado, setAbogado] = useState('')
  const [supervisor, setSupervisor] = useState('')
  const [limite, setLimite] = useState(100)
  const [idsRangoFecha, setIdsRangoFecha] = useState<Set<number> | null>(null)
  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)

  type WorkspaceAbierto = { id: string; abogado: CargaLaboralAbogado; color: string; activeSubTabId: string }
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [documentosPorAbogado, setDocumentosPorAbogado] = useState<
    Record<number, { lista: CargaLaboralDocumento[]; cargando: boolean; error: string | null }>
  >({})

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
    aplicarFiltroFecha(desde, hasta)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Extraído de handleBuscar para poder aplicarlo tanto al montar como al presionar
  // "Buscar"/"Limpiar". Filtra por Fecha Solicitud (no Fecha Vencimiento, ver Javadoc arriba),
  // con el mismo default "01 del mes actual → hoy" que el resto de bandejas.
  async function aplicarFiltroFecha(d: string, h: string) {
    if (d || h) {
      setLoading(true)
      try {
        const ids = await listarIdsUsuarioConSolicitudEnRango(d, h)
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

  function handleBuscar() {
    setFiltroKpi(null)
    aplicarFiltroFecha(desde, hasta)
  }

  function handleLimpiar() {
    const desdeDefault = primerDiaMesActualISO()
    const hastaDefault = hoyISO()
    setTexto('')
    setDesde(desdeDefault)
    setHasta(hastaDefault)
    setAbogado('')
    setSupervisor('')
    setLimite(100)
    setFiltroKpi(null)
    aplicarFiltroFecha(desdeDefault, hastaDefault)
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
    if (filtroKpi === 'CON_CARGA') resultado = resultado.filter((c) => enAnalisis(c) > 0)
    if (filtroKpi === 'SIN_CARGA') resultado = resultado.filter((c) => enAnalisis(c) === 0)
    if (filtroKpi === 'POR_VENCER') resultado = resultado.filter((c) => c.porVencer > 0)
    if (filtroKpi === 'VENCIDOS') resultado = resultado.filter((c) => c.vencidos > 0)
    return resultado.slice(0, limite)
  }, [cargas, texto, abogado, supervisor, idsRangoFecha, filtroKpi, limite])

  const maxEnAnalisisFiltrado = useMemo(() => Math.max(1, ...filas.map((f) => enAnalisis(f))), [filas])

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
    const promedio = cargas.length > 0 ? Math.round((solicitudes / cargas.length) * 10) / 10 : 0
    return { abogados: cargas.length, conCarga, sinCarga, solicitudes, porVencer, vencidos, promedio }
  }, [cargas])

  function alternarFiltro(valor: FiltroKpi) {
    setFiltroKpi((prev) => (prev === valor ? null : valor))
  }

  function recargarDetalle(idUsuario: number) {
    setDocumentosPorAbogado((prev) => ({ ...prev, [idUsuario]: { lista: prev[idUsuario]?.lista ?? [], cargando: true, error: null } }))
    listarDocumentosPorAbogado(idUsuario)
      .then((lista) => setDocumentosPorAbogado((prev) => ({ ...prev, [idUsuario]: { lista, cargando: false, error: null } })))
      .catch((e) =>
        setDocumentosPorAbogado((prev) => ({
          ...prev,
          [idUsuario]: { lista: [], cargando: false, error: e instanceof ApiError ? e.message : 'No se pudo cargar el detalle de carga.' },
        })),
      )
  }

  function abrirWorkspace(item: CargaLaboralAbogado) {
    const id = `ws-abogado-${item.idUsuario}`
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { id, abogado: item, color, activeSubTabId: 'detalle' }]
    })
    if (!documentosPorAbogado[item.idUsuario]) {
      recargarDetalle(item.idUsuario)
    }
  }

  function cerrarWorkspace(id: string) {
    setWorkspacesAbiertos((prev) => {
      const idx = prev.findIndex((w) => w.id === id)
      const restantes = prev.filter((w) => w.id !== id)
      setActiveWorkspaceId((actual) => {
        if (actual !== id) return actual
        if (restantes.length === 0) return null
        return restantes[Math.min(idx, restantes.length - 1)].id
      })
      return restantes
    })
  }

  function seleccionarSubTab(workspaceId: string, subTabId: string) {
    setWorkspacesAbiertos((prev) => prev.map((w) => (w.id === workspaceId ? { ...w, activeSubTabId: subTabId } : w)))
  }

  const workspacesUI: Workspace[] = useMemo(
    () =>
      workspacesAbiertos.map((w) => {
        const estado = documentosPorAbogado[w.abogado.idUsuario]
        return {
          id: w.id,
          label: w.abogado.abogado,
          color: w.color,
          activeSubTabId: w.activeSubTabId,
          subTabs: [
            {
              id: 'detalle',
              label: 'Detalle de carga',
              color: COLOR_DETALLE,
              content: (
                <Stack spacing={2}>
                  <Stack direction="row" spacing={3} sx={{ flexWrap: 'wrap' }}>
                    <Typography variant="body2" color="text.secondary">
                      Supervisor: <strong>{w.abogado.supervisor || '-'}</strong>
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Carga total: <strong>{cargaTotal(w.abogado)}</strong>
                    </Typography>
                  </Stack>
                  {estado?.cargando ? (
                    <Typography variant="body2" color="text.secondary">
                      Cargando...
                    </Typography>
                  ) : estado?.error ? (
                    <Alert severity="error">{estado.error}</Alert>
                  ) : !estado || estado.lista.length === 0 ? (
                    <Typography variant="body2" color="text.secondary">
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
                          {estado.lista.map((doc) => (
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
                  <Typography variant="caption" color="text.secondary">
                    Solo carga de Análisis (Por recibir/En análisis/Observado/Carta intermedia); no incluye Verificación ni Ejecución.
                  </Typography>
                </Stack>
              ),
            },
          ],
        }
      }),
    [workspacesAbiertos, documentosPorAbogado],
  )

  const columnas: GridColDef<CargaLaboralAbogado>[] = [
    { field: 'abogado', headerName: 'Abogado', flex: 1, minWidth: 220 },
    { field: 'analisisPorRecibir', headerName: 'Por recibir', width: 110 },
    { field: 'analisisEnProceso', headerName: 'En análisis', width: 110 },
    { field: 'analisisObservado', headerName: 'Observado', width: 110 },
    { field: 'analisisCartaIntermedia', headerName: 'Carta intermedia', width: 150 },
    {
      field: 'cargaRelativa',
      headerName: 'Carga relativa',
      width: 160,
      sortable: false,
      filterable: false,
      valueGetter: (_value, row) => enAnalisis(row),
      renderCell: (params) => {
        const valor = enAnalisis(params.row)
        const porcentaje = Math.round((valor / maxEnAnalisisFiltrado) * 100)
        const color = valor === 0 ? sdrercColors.textSecondary : porcentaje >= 75 ? sdrercColors.error : porcentaje >= 40 ? sdrercColors.warning : sdrercColors.success
        return (
          <Tooltip title={`${valor} expediente(s) en Análisis`}>
            <Box sx={{ width: '100%', display: 'flex', alignItems: 'center', gap: 1 }}>
              <LinearProgress
                variant="determinate"
                value={porcentaje}
                sx={{ flex: 1, height: 6, borderRadius: 3, bgcolor: sdrercColors.softGray, '& .MuiLinearProgress-bar': { bgcolor: color } }}
              />
            </Box>
          </Tooltip>
        )
      },
    },
  ]

  const filtrosHeader = (
    <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', rowGap: 1.5 }}>
      <TextField
        label="Buscar abogado"
        size="small"
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && handleBuscar()}
        sx={{ minWidth: 240 }}
      />
      <Button variant="contained" size="small" onClick={handleBuscar} disabled={loading}>
        Buscar
      </Button>
      <Button variant="outlined" size="small" onClick={handleLimpiar} disabled={loading}>
        Limpiar
      </Button>
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
      <TextField select label="Abogado" size="small" sx={{ minWidth: 180 }} value={abogado} onChange={(e) => setAbogado(e.target.value)}>
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
        sx={{ minWidth: 180 }}
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
        sx={{ width: 100 }}
        value={limite}
        onChange={(e) => setLimite(Number(e.target.value) || 100)}
      />
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Carga Abogados"
      subtitulo="Indicadores informativos para apoyar la asignación; no bloquean la decisión."
      tabs={ASIGNACION_TABS}
      filtros={filtrosHeader}
    >
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* KPIs (filtro en memoria sobre la lista ya cargada, igual que V2) — ya no comparten Paper
          con el buscador: el buscador quedó sticky en la Fila 3 del header, los KPIs se desplazan
          con el resto del cuerpo (13/08/2026, pedido explícito del usuario). */}
      <Stack direction="row" spacing={2} sx={{ mb: 2, flexWrap: 'wrap', gap: 2 }}>
        <MetricCard label="Abogados" value={kpis.abogados} caption="Activos" color={sdrercColors.info} />
        <Box onClick={() => alternarFiltro('CON_CARGA')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Con carga" value={kpis.conCarga} caption="Con solicitudes en Análisis" color={sdrercColors.success} />
        </Box>
        <Box onClick={() => alternarFiltro('SIN_CARGA')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Sin carga" value={kpis.sinCarga} caption="Sin solicitudes en Análisis" color={sdrercColors.warning} />
        </Box>
        <MetricCard label="Solicitudes" value={kpis.solicitudes} caption="Carga total asignada" color={sdrercColors.primary} />
        <MetricCard label="Carga promedio" value={kpis.promedio} caption="Solicitudes por abogado" color={sdrercColors.info} />
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
          onRowDoubleClick={(params) => abrirWorkspace(params.row as CargaLaboralAbogado)}
          pageSizeOptions={[25, 50, 100]}
          initialState={{
            pagination: { paginationModel: { pageSize: 25 } },
            sorting: { sortModel: [{ field: 'analisisEnProceso', sort: 'desc' }] },
          }}
          getRowClassName={(params) => (params.row.vencidos > 0 ? 'fila-vencida' : '')}
          sx={{ '& .fila-vencida': { bgcolor: sdrercColors.softRed } }}
        />
      </Box>

      <WorkspaceTabsDrawer
        workspaces={workspacesUI}
        activeWorkspaceId={activeWorkspaceId}
        onSelectWorkspace={setActiveWorkspaceId}
        onCloseWorkspace={cerrarWorkspace}
        onSelectSubTab={seleccionarSubTab}
      />
    </PageHeaderSticky>
  )
}
