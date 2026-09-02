import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, CircularProgress, IconButton, MenuItem, Stack, TextField, Tooltip } from '@mui/material'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import RemoveCircleOutlineIcon from '@mui/icons-material/RemoveCircleOutlined'
import { DataGrid, type GridColDef, type GridComparatorFn } from '@mui/x-data-grid'
import { buscarBandejaAnalisis, type AnalisisExpediente } from '../api/analisisApi'
import { listarConfirmados, type ExpedienteRelacionado } from '../api/expedienteRelacionadoApi'
import { obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { dataGridBaseSx } from '../dataGridTheme'
import { DataGridStandardToolbar } from '../components/DataGridStandardToolbar'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, diasBadgeTone } from '../components/StatusBadge'
import { AnalisisContent } from '../components/AnalisisContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
import { TrazabilidadContent } from '../components/TrazabilidadContent'
import { GrupoFamiliarConfirmadoContent } from '../components/GrupoFamiliarConfirmadoContent'
import { primerDiaMesAnteriorISO, hoyISO } from '../utils/fechas'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ANALISIS_TABS } from '../layout/moduleTabs'

type FiltroKpi = 'POR_RECIBIR' | 'EN_ANALISIS' | 'DERIVADO' | 'OBSERVADOS' | 'POR_VENCER_VENCIDO' | 'GRUPO_FAMILIAR_CONFIRMADO' | null

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

// Misma paleta que RegistroBandejaPage.tsx/AsignacionBandejaPage.tsx (franja de color por grupo de
// filas asociadas — cada bandeja duplica este helper/paleta, no hay componente de tabla compartido
// en el proyecto, ver v3/CLAUDE.md).
const GROUP_STRIPE_COLORS = [
  'rgb(30, 59, 97)', 'rgb(56, 88, 128)', 'rgb(77, 132, 164)', 'rgb(94, 154, 183)', 'rgb(147, 186, 210)',
  'rgb(10, 118, 145)', 'rgb(11, 142, 151)', 'rgb(65, 164, 181)', 'rgb(63, 95, 135)', 'rgb(84, 110, 154)',
]
function acentoGrupo(idPrincipal: number): string {
  return GROUP_STRIPE_COLORS[Math.abs(idPrincipal) % GROUP_STRIPE_COLORS.length]
}
const EXPANDED_PARENT_BACKGROUND = 'rgb(205, 236, 244)'
const ASSOCIATED_ROW_BACKGROUND = 'rgb(238, 250, 252)'

// Columna "expandir" fija al inicio (mismo mecanismo `disableVirtualization` + `position: sticky`
// que RegistroBandejaPage.tsx/AsignacionBandejaPage.tsx, ver Javadoc allá y v3/CLAUDE.md para el
// diagnóstico completo de por qué el "column pinning" nativo de MUI X no funciona en esta versión).
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
const COLOR_ANALISIS = 'rgb(10, 118, 145)'
// Mismo color que "Grupo Familiar" en Registro/Asignación (RegistroBandejaPage.tsx), para
// consistencia visual entre todos los módulos que muestran esta subpestaña.
const COLOR_GRUPO_FAMILIAR = '#7B4FA0'

interface FilaAnalisis extends AnalisisExpediente {
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
    const fila1 = params1.api.getRow(params1.id) as FilaAnalisis | null
    const fila2 = params2.api.getRow(params2.id) as FilaAnalisis | null
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
function adaptarParaPanelDatos(row: AnalisisExpediente): ExpedienteBandeja {
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
    fechaRegistro: row.fechaRegistro,
    fechaUltimoMovimiento: null,
    fechaVencimiento: row.fechaVencimiento,
    requierePublicacion: false,
    expedienteDigitalCompleto: false,
    canal: '',
    procedimiento: row.procedimiento,
    tipoActa: row.tipoActa,
    numeroActa: row.numeroActa,
    grupoFamiliar: row.grupoFamiliar ? 'Sí' : 'No',
    alertas: '',
    titular: row.titular,
    relacionesConfirmadasComoPrincipal: row.asociadosConfirmados,
    relacionadoComoHijo: false,
    grupoFamiliarConfirmado: row.grupoFamiliar,
    diasRestantes: row.diasRestantes,
  }
}

/**
 * Equivalente web de la pestaña "Bandeja Análisis" (V2, único tab real de `JPanelAnalisisV2` —
 * este módulo no tiene sub-bandejas como Registro/Asignación). 20/08/2026, primer incremento del
 * módulo (pedido explícito del usuario, tras exploración a fondo del código real de V2 — ver
 * `v3/CLAUDE.md` para el detalle completo de qué se verificó y qué queda pendiente).
 *
 * Alcance de este incremento: listado + filtros + 6 KPIs (mismos que `JPanelAnalisisV2`:
 * cardPorRecibir/cardEnAnalisis/cardCartaIntermedia("Derivado")/cardObservados/cardVencimiento/
 * cardGrupoFamiliarConfirmado, mismos criterios exactos verificados contra el Swing real), grilla
 * con las 12 columnas del `AnalisisTableModel` real (Días/Expediente/N° SGD/Fecha solicitud/
 * Procedimiento/Tipo acta/Nro. acta/Titular/Abogado designado/Estado, sin checkbox ni columna de
 * eliminar — V2 no las tiene acá; columna "Asociados" retirada 21/08/2026 a pedido explícito del
 * usuario — el conteo sigue disponible internamente para habilitar el ícono "+" de expandir, solo
 * se quitó de la vista), expansión de expedientes asociados con ícono "+" (mismo patrón que
 * Registro/Asignación), y panel multipestañas al doble clic.
 *
 * **Panel multipestañas — EXACTAMENTE 2 subpestañas por titular, pedido explícito del usuario**:
 * "Datos" (idéntica en diseño a la de Registro/Recepción — mismo componente `PanelDatosExpediente`,
 * mismo endpoint `GET /api/registro/bandeja/{id}/datos`) y "Análisis" (`AnalisisContent.tsx`,
 * contenido propio de este módulo). Deliberadamente NO se replicaron acá los otros 2 tipos de
 * subpestaña que sí tienen Registro/Asignación ("Asociar"/"Grupo Familiar") — V2 tampoco los tiene
 * en el panel de Análisis real (su panel derecho es CardLayout de 2 lengüetas: "Panel de datos"/
 * "Panel de análisis"), así que 2 subpestañas es paridad real con V2, no una reducción arbitraria.
 *
 * **Lápiz de edición habilitado (21/08/2026, pedido explícito del usuario)**: `PanelDatosExpediente`
 * recibe `desdeAnalisis` — port de `edicionDesdeAnalisis` (V2), que omite por completo la exigencia
 * de etapa REGISTRO/estado REGISTRADO/sin asignación (un expediente en Análisis nunca la cumple, así
 * que sin este flag el lápiz quedaba siempre visible pero permanentemente deshabilitado). Ver
 * Javadoc de `ExpedienteEdicionManualDAO.validarEditable(ExpedienteEstado, boolean, boolean)`.
 *
 * **Nota de diseño real de V2, resuelta explícitamente a favor del pedido del usuario**: en V2, la
 * sección "Solicitudes asociadas" (con botón "Recibir" por fila) vive DENTRO de la lengüeta
 * "Panel de datos" (no en "Panel de análisis"). Como el pedido explícito de V3 es que "Datos" quede
 * "exactamente igual" a la de Registro/Recepción (que no tiene esa sección), esa funcionalidad se
 * deferrió a un incremento futuro de la subpestaña "Análisis" en vez de romper la paridad de
 * "Datos" — ver Javadoc de `AnalisisContent.tsx`.
 */
export function AnalisisBandejaPage() {
  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaMesAnteriorISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [estado, setEstado] = useState('')
  const [expedientes, setExpedientes] = useState<AnalisisExpediente[]>([])
  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [expandidoId, setExpandidoId] = useState<number | null>(null)
  const [asociadosPorPrincipal, setAsociadosPorPrincipal] = useState<Record<number, ExpedienteRelacionado[]>>({})
  const [cargandoExpansion, setCargandoExpansion] = useState<number | null>(null)

  type WorkspaceAbierto = { id: string; row: AnalisisExpediente; color: string; activeSubTabId: string }
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [datosPorExpediente, setDatosPorExpediente] = useState<
    Record<number, { detalle: ExpedienteEdicionManual | null; cargando: boolean; error: string | null }>
  >({})

  const buscar = useCallback(
    (params?: { texto?: string; desde?: string; hasta?: string; estado?: string }) => {
      setLoading(true)
      setError(null)
      buscarBandejaAnalisis({
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

  function coincideFiltroKpi(item: AnalisisExpediente): boolean {
    switch (filtroKpi) {
      case 'POR_RECIBIR':
        return item.recibible
      case 'EN_ANALISIS':
        return item.registrable || item.enviableVerificacion
      case 'DERIVADO':
        return item.derivado
      case 'OBSERVADOS':
        return item.observado
      case 'POR_VENCER_VENCIDO':
        return item.porVencerOVencido
      case 'GRUPO_FAMILIAR_CONFIRMADO':
        return item.grupoFamiliar
      default:
        return true
    }
  }

  const kpis = useMemo(() => {
    let porRecibir = 0
    let enAnalisis = 0
    let derivado = 0
    let observados = 0
    let porVencerVencido = 0
    let grupoFamiliarConfirmado = 0
    for (const item of expedientes) {
      if (item.recibible) porRecibir++
      if (item.registrable || item.enviableVerificacion) enAnalisis++
      if (item.derivado) derivado++
      if (item.observado) observados++
      if (item.porVencerOVencido) porVencerVencido++
      if (item.grupoFamiliar) grupoFamiliarConfirmado++
    }
    return { porRecibir, enAnalisis, derivado, observados, porVencerVencido, grupoFamiliarConfirmado }
  }, [expedientes])

  const filas = useMemo(() => expedientes.filter(coincideFiltroKpi), [expedientes, filtroKpi])

  const filasConHijos = useMemo<FilaAnalisis[]>(() => {
    const resultado: FilaAnalisis[] = []
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

  function alternarExpansion(principal: AnalisisExpediente) {
    if (principal.asociadosConfirmados <= 0) return
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
  // recarga (20/08/2026, pedido explícito del usuario — fix real, no cosmético): antes, `row` se
  // asignaba una sola vez en `abrirWorkspace` y quedaba congelado para siempre, así que una acción
  // exitosa dentro de `AnalisisContent.tsx` (ej. "Recibir expediente") recargaba la grilla
  // (`buscar()`, vía `onActualizado`) pero el workspace ya abierto seguía mostrando el expediente
  // con su `etapaCodigo`/`estadoCodigo` viejo — de ahí que hubiera que cerrar y reabrir la pestaña
  // para ver el cambio. Con este efecto, cualquier acción que dispare `onActualizado` (no solo
  // "Recibir expediente") refresca el/los workspace(s) abiertos automáticamente, sin tocar
  // `activeSubTabId`/`color` de cada uno.
  useEffect(() => {
    setWorkspacesAbiertos((prev) =>
      prev.map((w) => {
        const fresco = expedientes.find((e) => e.idExpediente === w.row.idExpediente)
        return fresco ? { ...w, row: fresco } : w
      }),
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [expedientes])

  function abrirWorkspace(row: AnalisisExpediente) {
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
                  desdeAnalisis
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
              id: 'analisis',
              label: 'Análisis',
              color: COLOR_ANALISIS,
              content: <AnalisisContent expediente={w.row} onActualizado={buscar} />,
            },
            // Subpestaña condicional (21/08/2026, pedido explícito del usuario): solo aparece si el
            // titular de este expediente ya pertenece a un grupo familiar CONFIRMADO
            // (`w.row.grupoFamiliar`, mismo flag que ya alimenta el KPI "Grupo Familiar Confirmado").
            // Puramente informativa, sin acciones — a diferencia de la subpestaña homónima de
            // Registro/Asignación (candidatos + asociar + retirar).
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

  // Mismo orden exacto que AnalisisTableModel en V2 (JPanelAnalisisV2): Días, Expediente,
  // N° expediente SGD, Fecha solicitud, Procedimiento, Tipo acta, Nro. acta, Titular,
  // Abogado designado, Estado — más la columna "expandir" al inicio (sin checkbox ni eliminar:
  // V2 no las tiene en esta bandeja). Columna "Asociados" retirada 21/08/2026, pedido explícito
  // del usuario (el conteo real sigue disponible para el ícono "+" de expandir, solo se quitó de
  // la vista de columnas).
  const columnas: GridColDef<FilaAnalisis>[] = [
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
        const puedeExpandir = row.asociadosConfirmados > 0
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
    { field: 'procedimiento', headerName: 'Procedimiento', width: 170 },
    { field: 'tipoActa', headerName: 'Tipo acta', width: 150 },
    { field: 'numeroActa', headerName: 'Nro. acta', width: 110 },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    { field: 'responsable', headerName: 'Abogado designado', width: 190 },
    { field: 'estadoCodigo', headerName: 'Estado', width: 160 },
  ]

  const columnasConAgrupacion = useMemo<GridColDef<FilaAnalisis>[]>(
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
      <TextField select label="Estado" size="small" sx={{ minWidth: 180 }} value={estado} onChange={(e) => setEstado(e.target.value)}>
        <MenuItem value="">Todos los estados</MenuItem>
        <MenuItem value="ASIGNADO">Asignado</MenuItem>
        <MenuItem value="RECIBIDO_POR_ABOGADO">Recibido por abogado</MenuItem>
        <MenuItem value="OBSERVADO">Observado</MenuItem>
        <MenuItem value="DERIVADO">Derivado</MenuItem>
        <MenuItem value="SUBSANADO">Subsanado</MenuItem>
        <MenuItem value="ATENDIDO">Atendido</MenuItem>
      </TextField>
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Bandeja Análisis"
      subtitulo="Expedientes recibidos y en proceso de análisis registral."
      tabs={ANALISIS_TABS}
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
        <Box onClick={() => alternarFiltro('POR_RECIBIR')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Por recibir" value={kpis.porRecibir} caption="Asignación / Asignado" color={sdrercColors.info} active={filtroKpi === 'POR_RECIBIR'} />
        </Box>
        <Box onClick={() => alternarFiltro('EN_ANALISIS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="En análisis" value={kpis.enAnalisis} caption="Recibidos y observados" color={sdrercColors.primary} active={filtroKpi === 'EN_ANALISIS'} />
        </Box>
        <Box onClick={() => alternarFiltro('DERIVADO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Derivado" value={kpis.derivado} caption="Desde Cartas de respuesta" color={sdrercColors.primary} active={filtroKpi === 'DERIVADO'} />
        </Box>
        <Box onClick={() => alternarFiltro('OBSERVADOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Observados" value={kpis.observados} caption="Requieren subsanación" color={sdrercColors.warning} active={filtroKpi === 'OBSERVADOS'} />
        </Box>
        <Box onClick={() => alternarFiltro('POR_VENCER_VENCIDO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Por vencer / vencidos" value={kpis.porVencerVencido} caption="Días hábiles críticos" color={sdrercColors.error} active={filtroKpi === 'POR_VENCER_VENCIDO'} />
        </Box>
        <Box onClick={() => alternarFiltro('GRUPO_FAMILIAR_CONFIRMADO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Grupo Familiar Confirmado" value={kpis.grupoFamiliarConfirmado} caption="Registrado" color={sdrercColors.success} active={filtroKpi === 'GRUPO_FAMILIAR_CONFIRMADO'} />
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
            const row = params.row as FilaAnalisis
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
