import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, CircularProgress, IconButton, MenuItem, Stack, TextField, Tooltip } from '@mui/material'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import RemoveCircleOutlineIcon from '@mui/icons-material/RemoveCircleOutlined'
import { DataGrid, type GridColDef, type GridComparatorFn } from '@mui/x-data-grid'
import { buscarBandejaEjecucion, type EjecucionExpediente } from '../api/ejecucionApi'
import { listarConfirmados, type ExpedienteRelacionado } from '../api/expedienteRelacionadoApi'
import { obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { dataGridBaseSx } from '../dataGridTheme'
import { DataGridStandardToolbar } from '../components/DataGridStandardToolbar'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, diasBadgeTone } from '../components/StatusBadge'
import { EjecucionContent } from '../components/EjecucionContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
import { TrazabilidadContent } from '../components/TrazabilidadContent'
import { GrupoFamiliarConfirmadoContent } from '../components/GrupoFamiliarConfirmadoContent'
import { primerDiaMesAnteriorISO, hoyISO } from '../utils/fechas'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { EJECUCION_TABS } from '../layout/moduleTabs'

type FiltroKpi = 'PENDIENTES' | 'EN_REVISION' | 'ERROR_MATERIAL' | 'EJECUTADOS' | 'LISTOS_NOTIFICACION' | 'PLAZO_CRITICO' | null

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

// Mismo patrón que RegistroBandejaPage.tsx/AsignacionBandejaPage.tsx/AnalisisBandejaPage.tsx/VerificacionBandejaPage.tsx
// (sin componente de tabla compartido en el proyecto, cada bandeja duplica este helper/paleta).
const GROUP_STRIPE_COLORS = [
  'rgb(30, 59, 97)', 'rgb(56, 88, 128)', 'rgb(77, 132, 164)', 'rgb(94, 154, 183)', 'rgb(147, 186, 210)',
  'rgb(10, 118, 145)', 'rgb(11, 142, 151)', 'rgb(65, 164, 181)', 'rgb(63, 95, 135)', 'rgb(84, 110, 154)',
]
function acentoGrupo(idPrincipal: number): string {
  return GROUP_STRIPE_COLORS[Math.abs(idPrincipal) % GROUP_STRIPE_COLORS.length]
}
const EXPANDED_PARENT_BACKGROUND = 'rgb(205, 236, 244)'
const ASSOCIATED_ROW_BACKGROUND = 'rgb(238, 250, 252)'

const COLUMNA_FIJA_INICIO_SX = {
  '& .MuiDataGrid-columnHeader[data-field="expandir"], & .MuiDataGrid-cell[data-field="expandir"]': {
    position: 'sticky',
    left: 0,
    zIndex: 2,
    bgcolor: sdrercColors.backgroundPaper,
  },
  '& .MuiDataGrid-columnHeader[data-field="expandir"]': { zIndex: 3 },
  '& .fila-asociada .MuiDataGrid-cell[data-field="expandir"]': { bgcolor: ASSOCIATED_ROW_BACKGROUND },
  '& .fila-principal-expandida .MuiDataGrid-cell[data-field="expandir"]': { bgcolor: EXPANDED_PARENT_BACKGROUND },
} as const

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#2370A8'
const COLOR_TRAZABILIDAD = '#8E44AD'
const COLOR_EJECUCION = 'rgb(198, 111, 21)'
// Mismo color que "Grupo Familiar" en Registro/Asignación (RegistroBandejaPage.tsx), para
// consistencia visual entre todos los módulos que muestran esta subpestaña.
const COLOR_GRUPO_FAMILIAR = '#7B4FA0'

interface FilaEjecucion extends EjecucionExpediente {
  esFilaAsociada?: boolean
  idPrincipal?: number
}

function alertaAsociada(asociado: ExpedienteRelacionado): string {
  const alerta = (asociado.alertaIngreso ?? '').toLowerCase()
  if (alerta.includes('potencial duplicado')) return 'Potencial duplicado'
  if (alerta.includes('posible grupo familiar')) return 'Posible Grupo Familiar'
  if (alerta.includes('con observaciones')) return 'Con observaciones'
  return 'Duplicado confirmado'
}

function agruparHijosBajoPadre(comparadorOriginal?: GridComparatorFn<unknown>): GridComparatorFn<unknown> {
  return (v1, v2, params1, params2) => {
    const fila1 = params1.api.getRow(params1.id) as FilaEjecucion | null
    const fila2 = params2.api.getRow(params2.id) as FilaEjecucion | null
    const valor1 = fila1?.esFilaAsociada && fila1.idPrincipal != null ? params1.api.getCellValue(fila1.idPrincipal, params1.field) : v1
    const valor2 = fila2?.esFilaAsociada && fila2.idPrincipal != null ? params2.api.getCellValue(fila2.idPrincipal, params2.field) : v2
    if (comparadorOriginal) return comparadorOriginal(valor1, valor2, params1, params2)
    if (valor1 == null && valor2 == null) return 0
    if (valor1 == null) return -1
    if (valor2 == null) return 1
    if (valor1 < valor2) return -1
    if (valor1 > valor2) return 1
    return 0
  }
}

/** Adapta una fila de esta bandeja al tipo que espera el "Panel de datos" compartido con Registro. */
function adaptarParaPanelDatos(row: EjecucionExpediente): ExpedienteBandeja {
  return {
    idExpediente: row.idExpediente,
    numeroExpediente: row.numeroExpediente,
    numeroTramiteDocumentario: '',
    numeroExpedienteSgd: row.numeroExpedienteSgd,
    etapaCodigo: row.etapaCodigo,
    estadoCodigo: row.estadoCodigo,
    abogadoInicial: '',
    responsableActual: row.responsable,
    equipoActual: '',
    fechaRecepcion: row.fechaRecepcion,
    fechaRegistro: null,
    fechaUltimoMovimiento: null,
    fechaVencimiento: row.fechaVencimiento,
    requierePublicacion: false,
    expedienteDigitalCompleto: false,
    canal: '',
    procedimiento: row.procedimiento,
    tipoActa: row.tipoActa,
    numeroActa: row.numeroActa,
    grupoFamiliar: 'No',
    alertas: '',
    titular: row.titular,
    relacionesConfirmadasComoPrincipal: row.totalRelacionados,
    relacionadoComoHijo: false,
    grupoFamiliarConfirmado: false,
    diasRestantes: row.diasRestantes,
  }
}

/**
 * Equivalente web de la pestaña "Bandeja Ejecución" (V2, único tab real de `JPanelEjecucionV2` —
 * sin sub-bandejas). 21/08/2026, primer incremento del módulo.
 *
 * 6 KPIs (mismo criterio 1:1 que V2, incluida la redundancia heredada entre "Ejecutados"/"Listos
 * para notificación" — ver Javadoc de `EjecucionExpedienteDTO`): Pendientes, En revisión, Error
 * material, Ejecutados, Listos para notificación, Plazo crítico.
 *
 * Columnas de la grilla, MISMO orden exacto que `EjecucionTableModel` (V2): Días, Expediente, N°
 * expediente SGD, Fecha solicitud, Tipo documento (resolución), Resultado (de Análisis), Abogado,
 * Titular, Estado — SIN columna "Asociados" (V2 no la muestra en esta grilla, a diferencia de
 * Verificación) y sin checkbox de selección múltiple (V2 usa selección simple acá).
 *
 * Panel multipestañas — 2 subpestañas: "Datos" (idéntica a Registro/Análisis/Verificación, con
 * `permiteEdicion={false}`) y "Ejecutar" (`EjecucionContent.tsx`, mismo nombre de lengüeta que V2:
 * `TAB_EJECUCION_OPERACION`).
 */
export function EjecucionBandejaPage() {
  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaMesAnteriorISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [estado, setEstado] = useState('')
  const [expedientes, setExpedientes] = useState<EjecucionExpediente[]>([])
  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [expandidoId, setExpandidoId] = useState<number | null>(null)
  const [asociadosPorPrincipal, setAsociadosPorPrincipal] = useState<Record<number, ExpedienteRelacionado[]>>({})
  const [cargandoExpansion, setCargandoExpansion] = useState<number | null>(null)

  type WorkspaceAbierto = { id: string; row: EjecucionExpediente; color: string; activeSubTabId: string }
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [datosPorExpediente, setDatosPorExpediente] = useState<
    Record<number, { detalle: ExpedienteEdicionManual | null; cargando: boolean; error: string | null }>
  >({})

  const buscar = useCallback(
    (params?: { texto?: string; desde?: string; hasta?: string; estado?: string }) => {
      setLoading(true)
      setError(null)
      buscarBandejaEjecucion({
        texto: params?.texto ?? texto,
        desde: params?.desde ?? desde,
        hasta: params?.hasta ?? hasta,
        estado: params?.estado ?? estado,
      })
        .then((data) => {
          setExpedientes(data)
          setExpandidoId(null)
          setAsociadosPorPrincipal({})
        })
        .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
        .finally(() => setLoading(false))
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [texto, desde, hasta, estado],
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
    const desdeDefault = primerDiaMesAnteriorISO()
    const hastaDefault = hoyISO()
    setTexto('')
    setDesde(desdeDefault)
    setHasta(hastaDefault)
    setEstado('')
    setFiltroKpi(null)
    buscar({ texto: '', desde: desdeDefault, hasta: hastaDefault, estado: '' })
  }

  function coincideFiltroKpi(item: EjecucionExpediente): boolean {
    switch (filtroKpi) {
      case 'PENDIENTES':
        return item.pendiente
      case 'EN_REVISION':
        return item.documentoResolucionConNumero
      case 'ERROR_MATERIAL':
        return item.corregible
      case 'EJECUTADOS':
        return item.ejecutado
      case 'LISTOS_NOTIFICACION':
        return item.listoParaNotificacion
      case 'PLAZO_CRITICO':
        return item.porVencerOVencido
      default:
        return true
    }
  }

  const kpis = useMemo(() => {
    let pendientes = 0
    let enRevision = 0
    let errorMaterial = 0
    let ejecutados = 0
    let listosNotificacion = 0
    let plazoCritico = 0
    for (const item of expedientes) {
      if (item.pendiente) pendientes++
      if (item.documentoResolucionConNumero) enRevision++
      if (item.corregible) errorMaterial++
      if (item.ejecutado) ejecutados++
      if (item.listoParaNotificacion) listosNotificacion++
      if (item.porVencerOVencido) plazoCritico++
    }
    return { pendientes, enRevision, errorMaterial, ejecutados, listosNotificacion, plazoCritico }
  }, [expedientes])

  const filas = useMemo(() => expedientes.filter(coincideFiltroKpi), [expedientes, filtroKpi])

  const filasConHijos = useMemo<FilaEjecucion[]>(() => {
    const resultado: FilaEjecucion[] = []
    for (const item of filas) {
      resultado.push(item)
      if (expandidoId === item.idExpediente) {
        const asociados = asociadosPorPrincipal[item.idExpediente] ?? []
        for (const asoc of asociados) {
          resultado.push({
            ...item,
            idExpediente: -asoc.idExpediente,
            numeroExpedienteSgd: asoc.numeroExpedienteSgd,
            fechaRecepcion: asoc.fechaRecepcion,
            fechaVencimiento: asoc.fechaVencimiento,
            diasRestantes: null,
            procedimiento: asoc.procedimiento || '-',
            tipoActa: asoc.tipoActa,
            numeroActa: asoc.numeroActa,
            titular: asoc.titular,
            responsable: asoc.abogadoAsignado || '',
            etapaCodigo: asoc.etapaCodigo || '-',
            estadoCodigo: asoc.estadoCodigo || alertaAsociada(asoc),
            esFilaAsociada: true,
            idPrincipal: item.idExpediente,
          })
        }
      }
    }
    return resultado
  }, [filas, expandidoId, asociadosPorPrincipal])

  function alternarExpansion(principal: EjecucionExpediente) {
    if (principal.totalRelacionados <= 0) return
    if (expandidoId === principal.idExpediente) {
      setExpandidoId(null)
      return
    }
    setExpandidoId(principal.idExpediente)
    if (!asociadosPorPrincipal[principal.idExpediente]) {
      setCargandoExpansion(principal.idExpediente)
      listarConfirmados(principal.idExpediente)
        .then((asociados) => setAsociadosPorPrincipal((prev) => ({ ...prev, [principal.idExpediente]: asociados })))
        .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudieron cargar los expedientes asociados.'))
        .finally(() => setCargandoExpansion(null))
    }
  }

  function alternarFiltro(valor: FiltroKpi) {
    setFiltroKpi((prev) => (prev === valor ? null : valor))
  }

  // Sincroniza en caliente el `row` de cada workspace ya abierto cada vez que la bandeja se
  // recarga — mismo fix ya aplicado a Análisis/Verificación (20/08/2026, ver v3/CLAUDE.md),
  // incorporado desde el primer incremento de este módulo (no como un afterthought): sin esto, tras
  // una acción exitosa dentro de EjecucionContent.tsx el workspace ya abierto seguiría mostrando el
  // expediente con su `etapaCodigo`/`estadoCodigo` viejo hasta cerrar y reabrir la pestaña.
  useEffect(() => {
    setWorkspacesAbiertos((prev) =>
      prev.map((w) => {
        const fresco = expedientes.find((e) => e.idExpediente === w.row.idExpediente)
        return fresco ? { ...w, row: fresco } : w
      }),
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [expedientes])

  function abrirWorkspace(row: EjecucionExpediente) {
    const id = `ws-${row.idExpediente}`
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { id, row, color, activeSubTabId: 'datos' }]
    })
    if (datosPorExpediente[row.idExpediente]) return
    recargarDatosExpediente(row.idExpediente)
  }

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
                  bandeja={adaptarParaPanelDatos(w.row)}
                  detalle={estadoDatos?.detalle ?? null}
                  cargando={estadoDatos?.cargando ?? false}
                  error={estadoDatos?.error ?? null}
                  permiteEdicion={false}
                  onGuardado={() => {
                    recargarDatosExpediente(w.row.idExpediente)
                    buscar()
                  }}
                />
              ),
            },
            {
              id: 'trazabilidad',
              label: 'Trazabilidad',
              color: COLOR_TRAZABILIDAD,
              content: <TrazabilidadContent idExpediente={w.row.idExpediente} />,
            },
            {
              id: 'ejecutar',
              label: 'Ejecutar',
              color: COLOR_EJECUCION,
              content: <EjecucionContent expediente={w.row} onActualizado={buscar} />,
            },
            // Subpestaña condicional (21/08/2026, pedido explícito del usuario): solo aparece si el
            // titular de este expediente ya pertenece a un grupo familiar CONFIRMADO
            // (`w.row.grupoFamiliar`, campo agregado a `EjecucionExpedienteDTO`/DAO en este mismo
            // incremento — antes Ejecución no exponía este flag). Puramente informativa, sin acciones.
            ...(w.row.grupoFamiliar
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

  const columnas: GridColDef<FilaEjecucion>[] = [
    {
      field: 'expandir',
      headerName: '',
      width: 44,
      sortable: false,
      filterable: false,
      renderCell: (params) => {
        const row = params.row
        if (row.esFilaAsociada) {
          return <Box sx={{ height: '100%', borderLeft: `5px solid ${acentoGrupo(row.idPrincipal ?? 0)}` }} />
        }
        const puedeExpandir = row.totalRelacionados > 0
        const expandido = expandidoId === row.idExpediente
        if (cargandoExpansion === row.idExpediente) {
          return <CircularProgress size={16} sx={{ ml: 1.5 }} />
        }
        if (!puedeExpandir) return null
        return (
          <IconButton size="small" onClick={() => alternarExpansion(row)} aria-label={expandido ? 'Colapsar asociados' : 'Expandir asociados'}>
            {expandido ? <RemoveCircleOutlineIcon fontSize="small" /> : <AddCircleOutlineIcon fontSize="small" />}
          </IconButton>
        )
      },
    },
    {
      field: 'diasRestantes',
      headerName: 'Días',
      width: 90,
      renderCell: (params) =>
        params.row.esFilaAsociada ? (
          <Tooltip title="La alerta de días aplica al expediente principal.">
            <span>-</span>
          </Tooltip>
        ) : (
          <StatusBadge
            label={params.value === null || params.value === undefined ? '-' : String(params.value)}
            tone={diasBadgeTone(params.value as number | null)}
          />
        ),
    },
    { field: 'numeroExpediente', headerName: 'Expediente', width: 190 },
    { field: 'numeroExpedienteSgd', headerName: 'N° expediente SGD', width: 160 },
    { field: 'fechaRecepcion', headerName: 'Fecha solicitud', width: 130, valueFormatter: (value) => formatearFecha(value as string | null) },
    { field: 'tipoDocumentoResolucion', headerName: 'Tipo documento', width: 160 },
    { field: 'resultadoAnalisis', headerName: 'Resultado', width: 170 },
    { field: 'responsable', headerName: 'Abogado', width: 190 },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    { field: 'estadoCodigo', headerName: 'Estado', width: 170 },
  ]

  const columnasConAgrupacion = useMemo<GridColDef<FilaEjecucion>[]>(
    () => columnas.map((col) => ({ ...col, sortComparator: agruparHijosBajoPadre(col.sortComparator) })),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [columnas],
  )

  const filtrosHeader = (
    <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', rowGap: 1.5 }}>
      <TextField
        label="Buscar (N° expediente, SGD, acta, titular...)"
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
      <TextField select label="Estado" size="small" sx={{ minWidth: 190 }} value={estado} onChange={(e) => setEstado(e.target.value)}>
        <MenuItem value="">Todos los estados</MenuItem>
        <MenuItem value="EN_EJECUCION">En ejecución</MenuItem>
        <MenuItem value="INDAGATORIO">Indagatorio</MenuItem>
        <MenuItem value="EJECUTADO">Ejecutado</MenuItem>
        <MenuItem value="DOCUMENTO_INCONSISTENTE_EJECUCION">Documento inconsistente</MenuItem>
        <MenuItem value="REQUIERE_CORRECCION_EJECUCION">Requiere corrección</MenuItem>
      </TextField>
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Bandeja Ejecución"
      subtitulo="Expedientes con resolución para ejecutar y derivar a Notificación."
      tabs={EJECUCION_TABS}
      filtros={filtrosHeader}
    >
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* 28/08/2026, pedido explícito del usuario en 2 pasadas: 1ra "que no sean 2 filas" (repeat(N,1fr)
          fijo -- desbordaba horizontalmente en vez de envolver al angostar la ventana, "se ve fatal");
          2da, tras verlo roto, "responsive real": `repeat(auto-fit, minmax(130px, 1fr))` -- mismo
          130px que el nuevo `minWidth` de MetricCard (ver su Javadoc), permite que las tarjetas quepan
          en 1 sola fila en pantallas normales Y envuelvan (no desborden) en pantallas angostas. */}
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: 2, mb: 2 }}>
        <Box onClick={() => alternarFiltro('PENDIENTES')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Pendientes" value={kpis.pendientes} caption="En ejecución / Indagatorio" color={sdrercColors.info} active={filtroKpi === 'PENDIENTES'} />
        </Box>
        <Box onClick={() => alternarFiltro('EN_REVISION')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="En revisión" value={kpis.enRevision} caption="Con resolución numerada" color={sdrercColors.info} active={filtroKpi === 'EN_REVISION'} />
        </Box>
        <Box onClick={() => alternarFiltro('ERROR_MATERIAL')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Error material" value={kpis.errorMaterial} caption="Inconsistente / Requiere corrección" color={sdrercColors.error} active={filtroKpi === 'ERROR_MATERIAL'} />
        </Box>
        <Box onClick={() => alternarFiltro('EJECUTADOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Ejecutados" value={kpis.ejecutados} caption="Resultado registrado" color={sdrercColors.success} active={filtroKpi === 'EJECUTADOS'} />
        </Box>
        <Box onClick={() => alternarFiltro('LISTOS_NOTIFICACION')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Listos para notificación" value={kpis.listosNotificacion} caption="Puede enviarse a Supervisión" color={sdrercColors.success} active={filtroKpi === 'LISTOS_NOTIFICACION'} />
        </Box>
        <Box onClick={() => alternarFiltro('PLAZO_CRITICO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Plazo crítico" value={kpis.plazoCritico} caption="Por vencer o vencidos" color={sdrercColors.warning} active={filtroKpi === 'PLAZO_CRITICO'} />
        </Box>
      </Box>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2, mb: 3, overflow: 'hidden', boxShadow: '0 1px 3px rgba(16,24,40,0.08), 0 1px 2px rgba(16,24,40,0.04)' }}>
        <DataGrid
          rows={filasConHijos}
          columns={columnasConAgrupacion}
          getRowId={(row) => row.idExpediente}
          loading={loading}
          density="compact"
          autoHeight
          disableVirtualization
          showToolbar
          slots={{ toolbar: DataGridStandardToolbar }}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          getRowClassName={(params) =>
            params.row.esFilaAsociada ? 'fila-asociada' : expandidoId === params.row.idExpediente ? 'fila-principal-expandida' : ''
          }
          sx={{
            ...dataGridBaseSx,
            '& .fila-asociada': { bgcolor: ASSOCIATED_ROW_BACKGROUND, color: sdrercColors.textSecondary, fontSize: '0.8125rem' },
            '& .fila-principal-expandida': { bgcolor: EXPANDED_PARENT_BACKGROUND },
            ...COLUMNA_FIJA_INICIO_SX,
          }}
          onRowDoubleClick={(params) => {
            const row = params.row as FilaEjecucion
            if (row.esFilaAsociada) return
            abrirWorkspace(row)
          }}
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
