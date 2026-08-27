import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, MenuItem, Stack, TextField } from '@mui/material'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { buscarBandejaExpedientes, ETAPAS_EXPEDIENTE } from '../api/expedienteConsolaApi'
import { obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, diasBadgeTone, alertaBadgeTone } from '../components/StatusBadge'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
import { DocumentosExpedienteContent } from '../components/DocumentosExpedienteContent'
import { TrazabilidadContent } from '../components/TrazabilidadContent'
import { AsociadosExpedienteContent } from '../components/AsociadosExpedienteContent'
import { GrupoFamiliarConfirmadoContent } from '../components/GrupoFamiliarConfirmadoContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { primerDiaMesActualISO, hoyISO } from '../utils/fechas'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { BANDEJA_TABS } from '../layout/moduleTabs'

type FiltroKpi = null | 'POR_VENCER' | 'VENCIDOS' | 'POTENCIAL_DUPLICADO' | 'POSIBLE_GRUPO_FAMILIAR' | 'GRUPO_FAMILIAR_CONFIRMADO'

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#2370A8'
const COLOR_DOCUMENTOS = 'rgb(10, 118, 145)'
const COLOR_TRAZABILIDAD = '#8E44AD'
const COLOR_ASOCIADOS = '#B8860B'
const COLOR_GRUPO_FAMILIAR = '#7B4FA0'

/**
 * Equivalente web del módulo "Bandeja de Expedientes" (V2: `JPanelBandejaExpedientesNueva`, perfil
 * general). 24/08/2026, pedido explícito del usuario: reconstrucción completa ("así como está ahora
 * no me gusta para nada"), no un port literal — ver investigación completa y decisiones de diseño en
 * `v3/CLAUDE.md`. Objetivo: que cualquier usuario (permiso `MENU_BANDEJA`, sin restricción de rol)
 * pueda abrir cualquier expediente sin importar la etapa y ver de un vistazo datos, documentos,
 * trazabilidad completa, duplicados/asociados y grupo familiar — 100% de solo lectura.
 *
 * Listado: mismas columnas base que Registro/Asignación (Días/Expediente/N° SGD/Titular/Estado/
 * Fecha solicitud/Fecha vencimiento/Alertas) MÁS la columna "Etapa" (V2 la tiene en esta bandeja
 * general aunque no en Registro) y un combo "Etapa" real y visible en el buscador — a propósito
 * distinto de Registro, que la mantiene oculta/fija.
 *
 * Panel multipestañas, 5 subpestañas ("Expediente 360°"): "Datos" (`PanelDatosExpediente`, igual
 * que el resto de módulos), "Documentos" (lista real con detalle expandible, mejora sobre V2 — ver
 * Javadoc de `DocumentosExpedienteContent.tsx`), "Trazabilidad" (línea de tiempo completa, mejora
 * sobre V2 — ver Javadoc de `TrazabilidadContent.tsx`), "Duplicados / Asociados" y "Grupo Familiar"
 * (condicional, solo si `grupoFamiliarConfirmado`). La lengüeta "Acciones disponibles" de
 * `DlgConsolaExpedienteV2` (V2) NO se portó a propósito: son botones deshabilitados con tooltip
 * "Pendiente de implementación", código muerto real en V2, confirmado leyendo el archivo.
 */
export function BandejaExpedientesPage() {
  const [texto, setTexto] = useState('')
  const [etapa, setEtapa] = useState('')
  const [estado, setEstado] = useState('')
  const [desde, setDesde] = useState(primerDiaMesActualISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [expedientes, setExpedientes] = useState<ExpedienteBandeja[]>([])
  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  type WorkspaceAbierto = { id: string; row: ExpedienteBandeja; color: string; activeSubTabId: string }
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [datosPorExpediente, setDatosPorExpediente] = useState<
    Record<number, { detalle: ExpedienteEdicionManual | null; cargando: boolean; error: string | null }>
  >({})

  const buscar = useCallback(
    (params?: { texto?: string; etapa?: string; estado?: string; desde?: string; hasta?: string }) => {
      setLoading(true)
      setError(null)
      buscarBandejaExpedientes({
        texto: params?.texto ?? texto,
        etapa: params?.etapa ?? etapa,
        estado: params?.estado ?? estado,
        desde: params?.desde ?? desde,
        hasta: params?.hasta ?? hasta,
      })
        .then(setExpedientes)
        .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
        .finally(() => setLoading(false))
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [texto, etapa, estado, desde, hasta],
  )

  useEffect(() => {
    buscar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function handleBuscar() {
    setFiltroKpi(null)
    buscar()
  }

  function handleLimpiar() {
    const desdeDefault = primerDiaMesActualISO()
    const hastaDefault = hoyISO()
    setTexto('')
    setEtapa('')
    setEstado('')
    setDesde(desdeDefault)
    setHasta(hastaDefault)
    setFiltroKpi(null)
    buscar({ texto: '', etapa: '', estado: '', desde: desdeDefault, hasta: hastaDefault })
  }

  function coincideFiltroKpi(item: ExpedienteBandeja): boolean {
    switch (filtroKpi) {
      case 'POR_VENCER':
        return item.diasRestantes !== null && item.diasRestantes >= 0 && item.diasRestantes <= 5
      case 'VENCIDOS':
        return item.diasRestantes !== null && item.diasRestantes < 0
      case 'POTENCIAL_DUPLICADO':
        return item.alertas === 'Potencial duplicado'
      case 'POSIBLE_GRUPO_FAMILIAR':
        return item.alertas === 'Posible Grupo Familiar'
      case 'GRUPO_FAMILIAR_CONFIRMADO':
        return item.grupoFamiliarConfirmado
      default:
        return true
    }
  }

  const kpis = useMemo(() => {
    let porVencer = 0
    let vencidos = 0
    let potencialDuplicado = 0
    let posibleGrupoFamiliar = 0
    let grupoFamiliarConfirmado = 0
    for (const item of expedientes) {
      if (item.diasRestantes !== null && item.diasRestantes >= 0 && item.diasRestantes <= 5) porVencer++
      if (item.diasRestantes !== null && item.diasRestantes < 0) vencidos++
      if (item.alertas === 'Potencial duplicado') potencialDuplicado++
      if (item.alertas === 'Posible Grupo Familiar') posibleGrupoFamiliar++
      if (item.grupoFamiliarConfirmado) grupoFamiliarConfirmado++
    }
    return { total: expedientes.length, porVencer, vencidos, potencialDuplicado, posibleGrupoFamiliar, grupoFamiliarConfirmado }
  }, [expedientes])

  const filas = useMemo(() => expedientes.filter(coincideFiltroKpi), [expedientes, filtroKpi])

  function alternarFiltro(valor: FiltroKpi) {
    setFiltroKpi((prev) => (prev === valor ? null : valor))
  }

  // Sincroniza en caliente el `row` de cada workspace ya abierto al recargar la bandeja (mismo fix
  // ya aplicado en Análisis/Verificación/Ejecución/Notificación/Cartas de respuesta).
  useEffect(() => {
    setWorkspacesAbiertos((prev) =>
      prev.map((w) => {
        const fresco = expedientes.find((e) => e.idExpediente === w.row.idExpediente)
        return fresco ? { ...w, row: fresco } : w
      }),
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [expedientes])

  function recargarDatosExpediente(idExpediente: number) {
    setDatosPorExpediente((prev) => ({ ...prev, [idExpediente]: { detalle: prev[idExpediente]?.detalle ?? null, cargando: true, error: null } }))
    obtenerDatosExpediente(idExpediente)
      .then((detalle) => setDatosPorExpediente((prev) => ({ ...prev, [idExpediente]: { detalle, cargando: false, error: null } })))
      .catch((e) =>
        setDatosPorExpediente((prev) => ({
          ...prev,
          [idExpediente]: {
            detalle: prev[idExpediente]?.detalle ?? null,
            cargando: false,
            error: e instanceof ApiError ? e.message : 'No se pudo cargar el panel de datos.',
          },
        })),
      )
  }

  function abrirWorkspace(row: ExpedienteBandeja) {
    const id = `ws-${row.idExpediente}`
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { id, row, color, activeSubTabId: 'datos' }]
    })
    if (!datosPorExpediente[row.idExpediente]) {
      recargarDatosExpediente(row.idExpediente)
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
        const estadoDatos = datosPorExpediente[w.row.idExpediente]
        return {
          id: w.id,
          label: w.row.titular || w.row.numeroExpediente || `Expediente ${w.row.idExpediente}`,
          color: w.color,
          activeSubTabId: w.activeSubTabId,
          subTabs: [
            {
              id: 'datos',
              label: 'Datos',
              color: COLOR_DATOS,
              content: (
                <PanelDatosExpediente
                  bandeja={w.row}
                  detalle={estadoDatos?.detalle ?? null}
                  cargando={estadoDatos?.cargando ?? false}
                  error={estadoDatos?.error ?? null}
                  permiteEdicion={false}
                  onGuardado={() => recargarDatosExpediente(w.row.idExpediente)}
                />
              ),
            },
            {
              id: 'documentos',
              label: 'Documentos',
              color: COLOR_DOCUMENTOS,
              content: <DocumentosExpedienteContent idExpediente={w.row.idExpediente} numeroExpediente={w.row.numeroExpediente} />,
            },
            {
              id: 'trazabilidad',
              label: 'Trazabilidad',
              color: COLOR_TRAZABILIDAD,
              content: <TrazabilidadContent idExpediente={w.row.idExpediente} />,
            },
            {
              id: 'asociados',
              label: 'Duplicados / Asociados',
              color: COLOR_ASOCIADOS,
              content: <AsociadosExpedienteContent idExpediente={w.row.idExpediente} />,
            },
            ...(w.row.grupoFamiliarConfirmado
              ? [
                  {
                    id: 'grupo-familiar',
                    label: 'Grupo Familiar',
                    color: COLOR_GRUPO_FAMILIAR,
                    content: <GrupoFamiliarConfirmadoContent idExpediente={w.row.idExpediente} />,
                  },
                ]
              : []),
          ],
        }
      }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [workspacesAbiertos, datosPorExpediente],
  )

  const columnas: GridColDef<ExpedienteBandeja>[] = [
    {
      field: 'diasRestantes',
      headerName: 'Días',
      width: 90,
      renderCell: (params) => (
        <StatusBadge
          label={params.value === null || params.value === undefined ? '-' : String(params.value)}
          tone={diasBadgeTone(params.value as number | null)}
        />
      ),
    },
    { field: 'numeroExpediente', headerName: 'Expediente', width: 190 },
    { field: 'numeroExpedienteSgd', headerName: 'N° expediente SGD', width: 160 },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    { field: 'etapaCodigo', headerName: 'Etapa', width: 140 },
    { field: 'estadoCodigo', headerName: 'Estado', width: 150 },
    { field: 'responsableActual', headerName: 'Responsable actual', width: 190 },
    { field: 'fechaRecepcion', headerName: 'Fecha solicitud', width: 130, valueFormatter: (value) => formatearFecha(value as string | null) },
    { field: 'fechaVencimiento', headerName: 'Fecha vencimiento', width: 140, valueFormatter: (value) => formatearFecha(value as string | null) },
    {
      field: 'alertas',
      headerName: 'Alertas',
      width: 170,
      renderCell: (params) => (params.value ? <StatusBadge label={params.value as string} tone={alertaBadgeTone(params.value as string)} /> : '-'),
    },
  ]

  const filtrosHeader = (
    <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', rowGap: 1.5 }}>
      <TextField
        label="Buscar (N° expediente, SGD, titular, acta...)"
        size="small"
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && handleBuscar()}
        sx={{ minWidth: 280 }}
      />
      <Button variant="contained" size="small" onClick={handleBuscar} disabled={loading}>
        Buscar
      </Button>
      <Button variant="outlined" size="small" onClick={handleLimpiar} disabled={loading}>
        Limpiar
      </Button>
      <TextField label="Fecha desde" type="date" size="small" value={desde} onChange={(e) => setDesde(e.target.value)} slotProps={{ inputLabel: { shrink: true } }} />
      <TextField label="Fecha hasta" type="date" size="small" value={hasta} onChange={(e) => setHasta(e.target.value)} slotProps={{ inputLabel: { shrink: true } }} />
      <TextField select label="Etapa" size="small" sx={{ minWidth: 170 }} value={etapa} onChange={(e) => setEtapa(e.target.value)}>
        <MenuItem value="">Todas las etapas</MenuItem>
        {ETAPAS_EXPEDIENTE.map((e) => (
          <MenuItem key={e.codigo} value={e.codigo}>
            {e.nombre}
          </MenuItem>
        ))}
      </TextField>
      <TextField select label="Estado" size="small" sx={{ minWidth: 170 }} value={estado} onChange={(e) => setEstado(e.target.value)}>
        <MenuItem value="">Todos los estados</MenuItem>
        <MenuItem value="REGISTRADO">Registrado</MenuItem>
        <MenuItem value="ASIGNADO">Asignado</MenuItem>
        <MenuItem value="RECIBIDO_POR_ABOGADO">Recibido por abogado</MenuItem>
        <MenuItem value="OBSERVADO">Observado</MenuItem>
        <MenuItem value="DERIVADO">Derivado</MenuItem>
        <MenuItem value="ATENDIDO">Atendido</MenuItem>
      </TextField>
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Bandeja de Expedientes"
      subtitulo="Consulta, seguimiento y trazabilidad completa de cualquier expediente, en cualquier etapa."
      tabs={BANDEJA_TABS}
      filtros={filtrosHeader}
    >
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 2, mb: 2 }}>
        <MetricCard label="Total" value={kpis.total} caption="Según filtros activos" color={sdrercColors.info} />
        <Box onClick={() => alternarFiltro('POR_VENCER')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Por vencer" value={kpis.porVencer} caption="0 a 5 días hábiles" color={sdrercColors.warning} />
        </Box>
        <Box onClick={() => alternarFiltro('VENCIDOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Vencidos" value={kpis.vencidos} caption="Plazo excedido" color={sdrercColors.error} />
        </Box>
        <Box onClick={() => alternarFiltro('POTENCIAL_DUPLICADO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Potencial duplicado" value={kpis.potencialDuplicado} caption="Pendiente de resolver" color={sdrercColors.warning} />
        </Box>
        <Box onClick={() => alternarFiltro('POSIBLE_GRUPO_FAMILIAR')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Posible Grupo Familiar" value={kpis.posibleGrupoFamiliar} caption="Coincidencia por apellidos" color={sdrercColors.info} />
        </Box>
        <Box onClick={() => alternarFiltro('GRUPO_FAMILIAR_CONFIRMADO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Grupo Familiar Confirmado" value={kpis.grupoFamiliarConfirmado} caption="Registrado" color={sdrercColors.success} />
        </Box>
      </Box>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2 }}>
        <DataGrid
          rows={filas}
          columns={columnas}
          getRowId={(row) => row.idExpediente}
          loading={loading}
          density="compact"
          autoHeight
          disableRowSelectionOnClick
          showToolbar
          onRowDoubleClick={(params) => abrirWorkspace(params.row as ExpedienteBandeja)}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
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
