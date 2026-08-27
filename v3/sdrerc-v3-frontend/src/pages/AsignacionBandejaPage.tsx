import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  MenuItem,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import RemoveCircleOutlineIcon from '@mui/icons-material/RemoveCircleOutlined'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import {
  DataGrid,
  GridToolbarContainer,
  GridToolbarColumnsButton,
  GridToolbarFilterButton,
  GridToolbarExport,
  GridToolbarQuickFilter,
  useGridApiContext,
  type GridColDef,
  type GridComparatorFn,
  type GridRowSelectionModel,
} from '@mui/x-data-grid'
import { buscarBandejaAsignacion, type AsignacionExpediente } from '../api/asignacionApi'
import { listarConfirmados, type ExpedienteRelacionado } from '../api/expedienteRelacionadoApi'
import { asociarGrupoFamiliar } from '../api/grupoFamiliarApi'
import {
  eliminarExpedienteRegistro,
  obtenerImpactoEliminacionRegistro,
  type ExpedienteEdicionManual,
  type ImpactoEliminacionRegistro,
} from '../api/registroApi'
import { obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, alertaBadgeTone, diasBadgeTone } from '../components/StatusBadge'
import { AsociarDuplicadosContent } from '../components/AsociarDuplicadosContent'
import { GrupoFamiliarContent } from '../components/GrupoFamiliarContent'
import { AsignacionSeleccionContent } from '../components/AsignacionSeleccionContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
import { primerDiaMesActualISO, hoyISO } from '../utils/fechas'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ASIGNACION_TABS } from '../layout/moduleTabs'

type FiltroKpi = 'PENDIENTES' | 'POTENCIAL_DUPLICADO' | 'POSIBLE_GRUPO_FAMILIAR' | 'GRUPO_FAMILIAR_CONFIRMADO' | 'POR_VENCER' | 'VENCIDOS' | null

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

// Mismos 10 tonos que GROUP_STRIPE_COLORS en RegistroBandejaPage.tsx (réplica exacta, ver Javadoc
// ahí — cada página de bandeja define sus propios helpers, no hay componente de tabla compartido en
// este proyecto, ver v3/CLAUDE.md).
const GROUP_STRIPE_COLORS = [
  'rgb(30, 59, 97)',
  'rgb(56, 88, 128)',
  'rgb(77, 132, 164)',
  'rgb(94, 154, 183)',
  'rgb(147, 186, 210)',
  'rgb(10, 118, 145)',
  'rgb(11, 142, 151)',
  'rgb(65, 164, 181)',
  'rgb(63, 95, 135)',
  'rgb(84, 110, 154)',
]
function acentoGrupo(idPrincipal: number): string {
  return GROUP_STRIPE_COLORS[Math.abs(idPrincipal) % GROUP_STRIPE_COLORS.length]
}
const EXPANDED_PARENT_BACKGROUND = 'rgb(205, 236, 244)'
const ASSOCIATED_ROW_BACKGROUND = 'rgb(238, 250, 252)'

// Columnas fijas al inicio (checkbox de selección + "+"/eliminar) al desplazar horizontalmente
// (19/08/2026, 3er intento, pedido explícito del usuario — mismo bloque que RegistroBandejaPage.tsx,
// ver Javadoc allá y v3/CLAUDE.md para el diagnóstico completo: ni el "column pinning" nativo de MUI
// X ni `disableColumnVirtualization` funcionan en la versión instalada; este truco `position:
// sticky` sí funciona, siempre que `disableVirtualization` en el <DataGrid> mantenga todas las
// celdas montadas en el DOM).
const ANCHO_COLUMNA_CHECKBOX = 50
const COLUMNAS_FIJAS_INICIO_SX = {
  '& .MuiDataGrid-columnHeader[data-field="__check__"], & .MuiDataGrid-cell[data-field="__check__"]': {
    position: 'sticky',
    left: 0,
    zIndex: 2,
  },
  '& .MuiDataGrid-columnHeader[data-field="expandir"], & .MuiDataGrid-cell[data-field="expandir"]': {
    position: 'sticky',
    left: ANCHO_COLUMNA_CHECKBOX,
    zIndex: 2,
  },
  '& .MuiDataGrid-cell[data-field="__check__"], & .MuiDataGrid-cell[data-field="expandir"]': {
    bgcolor: sdrercColors.backgroundPaper,
  },
  '& .MuiDataGrid-columnHeader[data-field="__check__"], & .MuiDataGrid-columnHeader[data-field="expandir"]': {
    bgcolor: sdrercColors.backgroundPaper,
    zIndex: 3,
  },
  '& .fila-asociada .MuiDataGrid-cell[data-field="__check__"], & .fila-asociada .MuiDataGrid-cell[data-field="expandir"]': {
    bgcolor: ASSOCIATED_ROW_BACKGROUND,
  },
  '& .fila-principal-expandida .MuiDataGrid-cell[data-field="__check__"], & .fila-principal-expandida .MuiDataGrid-cell[data-field="expandir"]': {
    bgcolor: EXPANDED_PARENT_BACKGROUND,
  },
} as const

// Misma paleta/criterio que RegistroBandejaPage.tsx (identidad por titular del workspace, no
// confundir con GROUP_STRIPE_COLORS que es la franja de filas asociadas en la grilla).
const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#2370A8'
// Color propio de la subpestaña "Asignación" (19/08/2026, réplica del panel derecho real de V2,
// ver v3/CLAUDE.md) — mismo verde-azulado (`new Color(10, 118, 145)`) que usa
// `crearPanelAsignacionOperativa` en JPanelAsignacionV2.java para acentuar esa lengueta específica.
const COLOR_ASIGNACION = 'rgb(10, 118, 145)'
const COLOR_ASOCIAR = '#B86910'
const COLOR_GRUPO_FAMILIAR = '#7B4FA0'

interface FilaAsignacion extends AsignacionExpediente {
  esFilaAsociada?: boolean
  idPrincipal?: number
}

// Mismo mapeo que alertaAsociadaRegistro (V2) / alertaAsociada en RegistroBandejaPage.tsx: la
// alerta de una fila hija se deriva de ExpedienteRelacionado.alertaIngreso.
function alertaAsociada(asociado: ExpedienteRelacionado): string {
  const alerta = (asociado.alertaIngreso ?? '').toLowerCase()
  if (alerta.includes('potencial duplicado')) return 'Potencial duplicado'
  if (alerta.includes('posible grupo familiar')) return 'Posible Grupo Familiar'
  if (alerta.includes('con observaciones')) return 'Con observaciones'
  return 'Duplicado confirmado'
}

// Réplica exacta de agruparHijosBajoPadre en RegistroBandejaPage.tsx (19/08/2026, mismo fix de
// "las filas hijas deben ubicarse siempre inmediatamente debajo de su fila padre" al ordenar por
// cabecera, ver Javadoc allá para el detalle completo del mecanismo).
function agruparHijosBajoPadre(comparadorOriginal?: GridComparatorFn<unknown>): GridComparatorFn<unknown> {
  return (v1, v2, params1, params2) => {
    const fila1 = params1.api.getRow(params1.id) as FilaAsignacion | null
    const fila2 = params2.api.getRow(params2.id) as FilaAsignacion | null
    const valor1 = fila1?.esFilaAsociada && fila1.idPrincipal != null ? params1.api.getCellValue(fila1.idPrincipal, params1.field) : v1
    const valor2 = fila2?.esFilaAsociada && fila2.idPrincipal != null ? params2.api.getCellValue(fila2.idPrincipal, params2.field) : v2
    if (comparadorOriginal) {
      return comparadorOriginal(valor1, valor2, params1, params2)
    }
    if (valor1 == null && valor2 == null) return 0
    if (valor1 == null) return -1
    if (valor2 == null) return 1
    if (valor1 < valor2) return -1
    if (valor1 > valor2) return 1
    return 0
  }
}

// Adapta una fila de esta bandeja (AsignacionExpediente) al tipo que espera el "Panel de datos"
// compartido con Registro (`PanelDatosExpediente.tsx`, tipado sobre `ExpedienteBandeja`) — mismo
// componente/lógica de negocio, sin duplicarlo. Campos que Asignación no expone (abogadoInicial,
// equipoActual, fechaRegistro, etc.) quedan con un valor neutro: `PanelDatosExpediente` no los lee.
function adaptarParaPanelDatos(row: AsignacionExpediente): ExpedienteBandeja {
  return {
    idExpediente: row.idExpediente,
    numeroExpediente: row.numeroExpediente,
    numeroTramiteDocumentario: row.numeroTramiteDocumentario,
    numeroExpedienteSgd: row.numeroExpedienteSgd,
    etapaCodigo: row.etapaCodigo,
    estadoCodigo: row.estadoCodigo,
    abogadoInicial: '',
    responsableActual: row.asignacionActiva ? row.abogadoAsignado : '',
    equipoActual: '',
    fechaRecepcion: row.fechaRecepcion,
    fechaRegistro: null,
    fechaUltimoMovimiento: null,
    fechaVencimiento: row.fechaVencimiento,
    requierePublicacion: false,
    expedienteDigitalCompleto: false,
    canal: row.canalIngreso,
    procedimiento: row.procedimiento,
    tipoActa: row.tipoActa,
    numeroActa: row.numeroActa,
    grupoFamiliar: row.grupoFamiliar ? 'Sí' : 'No',
    alertas: row.alertaIngreso,
    titular: row.titular,
    relacionesConfirmadasComoPrincipal: row.asociadosConfirmados,
    relacionadoComoHijo: false,
    grupoFamiliarConfirmado: row.grupoFamiliar,
    diasRestantes: row.diasRestantes,
  }
}

const SIN_SELECCION: GridRowSelectionModel = { type: 'include', ids: new Set() }

declare module '@mui/x-data-grid' {
  interface ToolbarPropsOverrides {
    seleccionados: number[]
    onDeseleccionar: () => void
    onAsociarGrupoFamiliar: () => void
    procesandoGrupoFamiliar: boolean
    onAsignar: () => void
  }
}

interface ToolbarBandejaAsignacionProps {
  seleccionados: number[]
  onDeseleccionar: () => void
  onAsociarGrupoFamiliar: () => void
  procesandoGrupoFamiliar: boolean
  onAsignar: () => void
}

/**
 * Toolbar de la grilla (19/08/2026, réplica del mismo patrón ya construido en
 * `ToolbarBandejaRegistro` de RegistroBandejaPage.tsx, pedido explícito del usuario de paridad
 * entre ambos módulos): contador "N seleccionados" + "Asignar"/"Asociar al grupo familiar"/
 * "Exportar seleccionados"/"Deseleccionar todo" a la izquierda (oculto sin selección), controles
 * estándar (columnas/filtro/exportar/buscar) a la derecha.
 *
 * **"Asignar" (19/08/2026, pedido explícito del usuario)**: habilitado con 1+ seleccionados
 * (distinto del umbral de 2+ que sigue usando "Asociar al grupo familiar", que si necesita al menos
 * un par) — abre el panel multipestañas, en modo "por titular" o "Selección múltiple" según la
 * selección (`handleAsignarSeleccion`, ver Javadoc de `AsignacionBandejaPage` más abajo),
 * reemplazando el bloque fijo que antes vivía debajo de la grilla para el mismo flujo de
 * asignación/reasignación en lote.
 */
function ToolbarBandejaAsignacion({
  seleccionados,
  onDeseleccionar,
  onAsociarGrupoFamiliar,
  procesandoGrupoFamiliar,
  onAsignar,
}: ToolbarBandejaAsignacionProps) {
  const apiRef = useGridApiContext()
  const hay = seleccionados.length > 0

  return (
    <GridToolbarContainer>
      {hay && (
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          <Chip
            label={`${seleccionados.length} seleccionado${seleccionados.length > 1 ? 's' : ''}`}
            size="small"
            color="primary"
            variant="outlined"
          />
          <Button size="small" variant="contained" onClick={onAsignar}>
            Asignar
          </Button>
          <Tooltip title={seleccionados.length < 2 ? 'Marque 2 o más expedientes para asociarlos como un mismo grupo familiar' : ''}>
            <span>
              <Button size="small" disabled={seleccionados.length < 2 || procesandoGrupoFamiliar} onClick={onAsociarGrupoFamiliar}>
                Asociar al grupo familiar
              </Button>
            </span>
          </Tooltip>
          <Button size="small" onClick={() => apiRef.current.exportDataAsCsv({ getRowsToExport: () => seleccionados })}>
            Exportar seleccionados
          </Button>
          <Button size="small" onClick={onDeseleccionar}>
            Deseleccionar todo
          </Button>
        </Stack>
      )}
      <Box sx={{ flex: 1 }} />
      <GridToolbarColumnsButton />
      <GridToolbarFilterButton />
      <GridToolbarExport />
      <GridToolbarQuickFilter />
    </GridToolbarContainer>
  )
}

/**
 * Equivalente web de la pestaña "Bandeja Asignación" (V2, lengueta principal de
 * JPanelAsignacionV2) + el panel derecho de Asignación. Alcance: listado + filtros + 7 KPIs, panel
 * multipestañas, edición inline vía "Panel de datos" (mismo `PanelDatosExpediente`/
 * `RegistroExpedienteForm` que Registro), expansión de expedientes asociados con ícono "+" + ícono
 * de eliminar en la misma columna, agrupación de filas hijas bajo su padre al ordenar, KPIs en grid
 * equitativo, toolbar con contador de seleccionados, `pageSizeOptions` unificado y sin tope de
 * filas por defecto (`buscarBandejaAsignacion` ya no fuerza `limite=200`, ver `asignacionApi.ts`).
 *
 * **Panel multipestañas — 2 orígenes distintos, con contenido distinto (rediseñado 20/08/2026,
 * pedido explícito del usuario; historial completo de cómo se llegó acá abajo)**:
 * - **Doble clic sobre una fila** (`kind: 'individual'`, `abrirWorkspace`): abre/reutiliza un
 *   workspace por `idExpediente` con subpestañas **Datos, Asociar, Grupo Familiar** — ya NO incluye
 *   "Asignación" (se retiró de acá 20/08/2026, pedido explícito del usuario: esa acción vive
 *   exclusivamente en el origen siguiente).
 * - **Acción "Asignar" del toolbar** (1+ filas marcadas por checkbox, `handleAsignarSeleccion`):
 *   decide por TITULAR, no por cantidad — si la selección pertenece toda a la misma persona (1 o
 *   más de sus expedientes), abre/reutiliza un workspace por titular (`kind: 'individualAsignacion'`,
 *   `ws-asig-{titular}`) con un único subtab "Asignación"; si mezcla 2+ titulares distintos, abre el
 *   workspace "Selección múltiple" (`kind: 'multiple'`, id fijo `ws-multiple`), mismo subtab único.
 *   Ambos orígenes comparten el mismo componente de contenido, `AsignacionSeleccionContent.tsx`
 *   (mini-grilla "Expedientes seleccionados" + "Destino operativo", igual en los 2 casos), con la
 *   ÚNICA diferencia real de que el modo "por titular" SÍ muestra "Historial de asignación /
 *   reasignación" al final (`mostrarHistorial=true`) y "Selección múltiple" no (`false`) — cada
 *   expediente del lote mixto tiene su propio historial, se consulta entrando a su propio workspace
 *   por titular.
 * - **Restricción de checkbox mientras un workspace "por titular" está abierto** (pedido explícito
 *   del usuario): `isRowSelectable` bloquea marcar filas de OTRO titular distinto al de
 *   `workspaceTitularActivo` (el primer workspace `'individualAsignacion'` encontrado en
 *   `workspacesAbiertos`) — evita mezclar por error la selección de una asignación ya enfocada en 1
 *   persona con expedientes de otra. Se levanta sola al cerrar ese tab (deja de existir en la
 *   lista). Mientras ningún workspace de ese tipo está abierto, la selección es libre (así se puede
 *   construir a propósito una selección de titulares mezclados para "Selección múltiple").
 *
 * Historial de cómo se llegó a este diseño (pasadas previas, mismo día 19/08/2026, contexto
 * conservado por si se necesita entender una decisión anterior):
 * - 1ª pasada: se agregó la subpestaña "Asignación" al workspace de doble clic (2ª posición,
 *   replicando `crearPanelAsignacionConTab` de V2) con historial embebido, reemplazando el `Dialog`
 *   "Ver historial" que tenía antes esta página.
 * - 2ª pasada: se agregó el flujo de selección múltiple/lote (checkbox + "Asignar" del toolbar),
 *   reemplazando el bloque fijo que vivía debajo de la grilla.
 * - 3ª pasada: `isRowSelectable` dejó de depender de un checkbox "Habilitar reasignación" manual —
 *   cualquier fila no asociada quedó seleccionable siempre (Registrado o Asignado); la mini-grilla
 *   del lote pasó a detectar automáticamente "Tipo de operación" (Asignación inicial/Reasignación)
 *   por fila en vez de ese checkbox.
 * - 5ª pasada: se corrigió que "Asignar" con exactamente 1 fila abriera igual "Selección múltiple"
 *   (debía abrir un tab individual); se unificó el diseño visual de "Destino operativo" entre el
 *   modo de 1 expediente y el de lote.
 * - 20/08/2026 (esta pasada): el criterio para decidir "individual" vs "múltiple" pasó de ser
 *   CANTIDAD (1 vs 2+) a ser TITULAR (mismo titular vs titulares mezclados); la subpestaña
 *   "Asignación" se retiró del workspace de doble clic (quedó exclusiva del flujo
 *   checkbox+"Asignar"); se agregó la restricción de checkbox por titular; y
 *   `AsignacionExpedienteContent.tsx`/`AsignacionMultipleContent.tsx` se fusionaron en un solo
 *   componente, `AsignacionSeleccionContent.tsx` (ver su Javadoc para el detalle completo).
 *
 * La columna "Historial" de la grilla no existe (se retiró en la 1ª pasada); las columnas
 * "Asociar"/"Grupo Familiar" tampoco (mismo criterio que Registro al introducir el panel
 * multipestañas): las 3 acciones viven en las subpestañas de alguno de los 2 orígenes de arriba.
 */
export function AsignacionBandejaPage() {
  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaMesActualISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [estado, setEstado] = useState('')
  const [expedientes, setExpedientes] = useState<AsignacionExpediente[]>([])
  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [rowSelectionModel, setRowSelectionModel] = useState<GridRowSelectionModel>(SIN_SELECCION)

  // Expansión de expedientes asociados (icono "+", 19/08/2026): mismo patrón que
  // RegistroBandejaPage.tsx — solo un principal expandido a la vez, `listarConfirmados` cacheado
  // por principal en memoria.
  const [expandidoId, setExpandidoId] = useState<number | null>(null)
  const [asociadosPorPrincipal, setAsociadosPorPrincipal] = useState<Record<number, ExpedienteRelacionado[]>>({})
  const [cargandoExpansion, setCargandoExpansion] = useState<number | null>(null)

  // Eliminar (baja lógica, mismo backend/flujo que Registro — 19/08/2026).
  const [expedienteAEliminar, setExpedienteAEliminar] = useState<FilaAsignacion | null>(null)
  const [eliminando, setEliminando] = useState(false)
  const [errorEliminar, setErrorEliminar] = useState<string | null>(null)
  const [impactoEliminar, setImpactoEliminar] = useState<ImpactoEliminacionRegistro | null>(null)
  const [cargandoImpacto, setCargandoImpacto] = useState(false)

  // "Asociar al grupo familiar" masivo desde la selección por checkbox (19/08/2026, mismo patrón
  // que ToolbarBandejaRegistro).
  const [procesandoGrupoFamiliarMasivo, setProcesandoGrupoFamiliarMasivo] = useState(false)
  const [mensajeGrupoFamiliarMasivo, setMensajeGrupoFamiliarMasivo] = useState<string | null>(null)

  // Panel multipestañas (WorkspaceTabsDrawer, 19/08/2026): mismo patrón que RegistroBandejaPage.tsx.
  //
  // 20/08/2026, pedido explícito del usuario — 3 tipos de workspace, cada uno con un origen y un
  // contenido de subpestañas distinto (antes había solo 2: 'individual' con 4 subpestañas incluida
  // "Asignación", y 'multiple'):
  // - `'individual'`: se abre EXCLUSIVAMENTE con doble clic sobre una fila. Subpestañas: Datos,
  //   Asociar, Grupo Familiar — YA NO incluye "Asignación" (se retiró de acá; ver los 2 tipos
  //   siguientes). Sigue siendo 1 workspace por `idExpediente` (`ws-{idExpediente}`).
  // - `'individualAsignacion'`: se abre con la acción "Asignar" del toolbar cuando la selección de
  //   checkboxes pertenece TODA al mismo titular (1 o más expedientes de esa misma persona). Subtab
  //   único "Asignación", contenido `AsignacionSeleccionContent` con `mostrarHistorial=true`. Clave
  //   por titular normalizado (`ws-asig-{titular}`), no por `idExpediente` — puede terminar
  //   representando más de 1 expediente si el usuario marca varios del mismo titular (ver
  //   `isRowSelectable` más abajo, que restringe la grilla a ese mismo titular mientras este
  //   workspace sigue abierto).
  // - `'multiple'`: se abre con "Asignar" cuando la selección mezcla 2+ titulares distintos. Subtab
  //   único "Asignación", contenido `AsignacionSeleccionContent` con `mostrarHistorial=false`. A lo
  //   sumo 1 instancia (id fijo `ws-multiple`).
  //
  // Ninguno de los 2 últimos guarda un snapshot de `expedientes` (fix de bug real, 19/08/2026, 2ª
  // pasada: "la mini-grilla del panel no se actualiza en tiempo real al marcar más checkboxes") — el
  // contenido siempre se arma con `expedientesSeleccionados` EN VIVO desde `workspacesUI`.
  type WorkspaceAbierto =
    | { kind: 'individual'; id: string; row: AsignacionExpediente; color: string; activeSubTabId: string }
    | { kind: 'individualAsignacion'; id: string; titular: string; color: string; activeSubTabId: string }
    | { kind: 'multiple'; id: string; color: string; activeSubTabId: string }
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [datosPorExpediente, setDatosPorExpediente] = useState<
    Record<number, { detalle: ExpedienteEdicionManual | null; cargando: boolean; error: string | null }>
  >({})

  const buscar = useCallback(
    (params?: { texto?: string; desde?: string; hasta?: string; estado?: string }) => {
      setLoading(true)
      setError(null)
      buscarBandejaAsignacion({
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
    const desdeDefault = primerDiaMesActualISO()
    const hastaDefault = hoyISO()
    setTexto('')
    setDesde(desdeDefault)
    setHasta(hastaDefault)
    setEstado('')
    setFiltroKpi(null)
    buscar({ texto: '', desde: desdeDefault, hasta: hastaDefault, estado: '' })
  }

  function coincideFiltroKpi(item: AsignacionExpediente): boolean {
    const dias = item.diasRestantes
    switch (filtroKpi) {
      case 'PENDIENTES':
        return item.asignable
      case 'POTENCIAL_DUPLICADO':
        return item.potencialDuplicado
      case 'POSIBLE_GRUPO_FAMILIAR':
        return item.posibleGrupoFamiliar
      case 'GRUPO_FAMILIAR_CONFIRMADO':
        return item.grupoFamiliar
      case 'POR_VENCER':
        return dias !== null && dias >= 0 && dias <= 5
      case 'VENCIDOS':
        return dias !== null && dias < 0
      default:
        return true
    }
  }

  const kpis = useMemo(() => {
    let pendientes = 0
    let potencialDuplicado = 0
    let posibleGrupoFamiliar = 0
    let grupoFamiliarConfirmado = 0
    let porVencer = 0
    let vencidos = 0
    for (const item of expedientes) {
      if (item.asignable) pendientes++
      if (item.potencialDuplicado) potencialDuplicado++
      if (item.posibleGrupoFamiliar) posibleGrupoFamiliar++
      if (item.grupoFamiliar) grupoFamiliarConfirmado++
      const dias = item.diasRestantes
      if (dias !== null && dias < 0) vencidos++
      else if (dias !== null && dias <= 5) porVencer++
    }
    return { total: expedientes.length, pendientes, potencialDuplicado, posibleGrupoFamiliar, grupoFamiliarConfirmado, porVencer, vencidos }
  }, [expedientes])

  const filas = useMemo(() => expedientes.filter(coincideFiltroKpi), [expedientes, filtroKpi])

  // Intercala filas hijas sintéticas justo debajo de su principal cuando está expandido — mismo
  // patrón que filasConHijos en RegistroBandejaPage.tsx.
  const filasConHijos = useMemo<FilaAsignacion[]>(() => {
    const resultado: FilaAsignacion[] = []
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
            etapaCodigo: asoc.etapaCodigo || '-',
            estadoCodigo: asoc.estadoCodigo || 'Documento asociado',
            abogadoAsignado: '',
            asignacionActiva: false,
            asignable: false,
            alertaIngreso: alertaAsociada(asoc),
            esFilaAsociada: true,
            idPrincipal: item.idExpediente,
          })
        }
      }
    }
    return resultado
  }, [filas, expandidoId, asociadosPorPrincipal])

  function alternarExpansion(principal: AsignacionExpediente) {
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

  const idsSeleccionados = useMemo(() => {
    return filas
      .filter((f) =>
        rowSelectionModel.type === 'include'
          ? rowSelectionModel.ids.has(f.idExpediente)
          : !rowSelectionModel.ids.has(f.idExpediente),
      )
      .map((f) => f.idExpediente)
  }, [filas, rowSelectionModel])

  const expedientesSeleccionados = useMemo(
    () => expedientes.filter((e) => idsSeleccionados.includes(e.idExpediente)),
    [expedientes, idsSeleccionados],
  )

  // "Asociar al grupo familiar" masivo (19/08/2026, mismo patrón que
  // asociarSeleccionadosGrupoFamiliar en RegistroBandejaPage.tsx): el primer id marcado actúa como
  // principal del grupo, el resto se asocia contra él.
  async function asociarSeleccionadosGrupoFamiliar() {
    if (idsSeleccionados.length < 2) return
    const confirmado = window.confirm(
      `Se asociarán ${idsSeleccionados.length} expedientes marcados como un mismo grupo familiar.\n` +
        'Solo se marca "Sí" en Grupo Familiar; no se asocian expedientes entre sí.\n\n¿Desea continuar?',
    )
    if (!confirmado) return
    const [idPrincipal, ...resto] = idsSeleccionados
    setProcesandoGrupoFamiliarMasivo(true)
    setError(null)
    setMensajeGrupoFamiliarMasivo(null)
    try {
      const resultado = await asociarGrupoFamiliar(idPrincipal, resto)
      setMensajeGrupoFamiliarMasivo(resultado.mensaje)
      setRowSelectionModel(SIN_SELECCION)
      buscar()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo asociar el grupo familiar.')
    } finally {
      setProcesandoGrupoFamiliarMasivo(false)
    }
  }

  async function confirmarEliminar() {
    if (!expedienteAEliminar) return
    setEliminando(true)
    setErrorEliminar(null)
    try {
      await eliminarExpedienteRegistro(expedienteAEliminar.idExpediente)
      setExpedienteAEliminar(null)
      buscar()
    } catch (e) {
      setErrorEliminar(e instanceof ApiError ? e.message : 'No se pudo eliminar el expediente.')
    } finally {
      setEliminando(false)
    }
  }

  useEffect(() => {
    if (!expedienteAEliminar) {
      setImpactoEliminar(null)
      return
    }
    let cancelado = false
    setCargandoImpacto(true)
    obtenerImpactoEliminacionRegistro(expedienteAEliminar.idExpediente)
      .then((impacto) => {
        if (!cancelado) setImpactoEliminar(impacto)
      })
      .catch(() => {
        if (!cancelado) setImpactoEliminar(null)
      })
      .finally(() => {
        if (!cancelado) setCargandoImpacto(false)
      })
    return () => {
      cancelado = true
    }
  }, [expedienteAEliminar])

  // 20/08/2026, pedido explícito del usuario (regla de "tamaño mínimo de grupo", mismo criterio que
  // RegistroBandejaPage.tsx): las 2 partes de grupo familiar indican el alcance real según el
  // tamaño ACTUAL del vínculo (calculado dinámicamente por el backend, no cacheado) — con 2 o menos,
  // se disuelve por completo (también afecta a la otra solicitud); con 3 o más, solo se retira esta.
  const mensajeImpactoEliminar = useMemo(() => {
    if (!impactoEliminar || impactoEliminar.sinImpacto) return null
    const partes: string[] = []
    if (impactoEliminar.grupoFamiliarConfirmado) {
      const tamano = impactoEliminar.tamanoGrupoFamiliarConfirmado
      partes.push(
        tamano > 0 && tamano <= 2
          ? 'de un Grupo Familiar Confirmado (solo 2 integrantes: el grupo se disolverá por completo)'
          : tamano > 2
            ? `de un Grupo Familiar Confirmado (${tamano} integrantes: los demás conservarán su vínculo)`
            : 'de un Grupo Familiar Confirmado',
      )
    }
    if (impactoEliminar.potencialDuplicado) partes.push('de 1 alerta de Potencial duplicado')
    if (impactoEliminar.posibleGrupoFamiliar) {
      const tamano = impactoEliminar.tamanoPosibleGrupoFamiliar
      partes.push(
        tamano > 0 && tamano <= 2
          ? 'de 1 alerta de Posible Grupo Familiar (solo 2 solicitudes relacionadas: se eliminará también de la otra)'
          : tamano > 2
            ? `de 1 alerta de Posible Grupo Familiar (quedan ${tamano - 1} solicitudes relacionadas que conservarán la alerta)`
            : 'de 1 alerta de Posible Grupo Familiar',
      )
    }
    if (partes.length === 0) return null
    const listado = partes.length === 1 ? partes[0] : partes.slice(0, -1).join(', ') + ' y ' + partes[partes.length - 1]
    return `Este expediente forma parte ${listado}. Al eliminarlo, también se actualizarán estos registros.`
  }, [impactoEliminar])

  // Panel multipestañas (19/08/2026): mismo patrón que abrirWorkspace/cerrarWorkspace/
  // seleccionarSubTab/recargarDatosExpediente en RegistroBandejaPage.tsx.
  //
  // 20/08/2026, pedido explícito del usuario: exclusivo del doble clic — ya no acepta forzar una
  // subpestaña "Asignación" (esa subpestaña se retiró por completo de este tipo de workspace, ver
  // Javadoc de `WorkspaceAbierto`). Vuelve a su forma simple original.
  function abrirWorkspace(row: AsignacionExpediente) {
    const id = `ws-${row.idExpediente}`
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { kind: 'individual', id, row, color, activeSubTabId: 'datos' }]
    })
    if (datosPorExpediente[row.idExpediente]) return
    recargarDatosExpediente(row.idExpediente)
  }

  // Mismo criterio de comparación de titulares usado en 2 lugares (agrupar la selección por titular
  // al decidir qué workspace abrir, y restringir `isRowSelectable` mientras uno está abierto) — sin
  // normalizar tildes/diacríticos a propósito (mismo dato ya normalizado que expone el backend en
  // `AsignacionExpediente.titular`, no hace falta duplicar esa lógica acá).
  function normalizarTitular(titular: string): string {
    return (titular || '').trim().toUpperCase()
  }

  // "Asignar" del toolbar en modo "1 titular" (20/08/2026, pedido explícito del usuario, reemplaza
  // el uso anterior de `abrirWorkspace` con subpestaña forzada): workspace propio, clave por titular
  // normalizado (no por `idExpediente` — puede terminar representando más de 1 expediente de la
  // misma persona). Subtab único "Asignación" con `mostrarHistorial=true`. El contenido siempre se
  // arma con la selección EN VIVO desde `workspacesUI` (nunca un snapshot).
  function abrirWorkspaceAsignacionTitular(titular: string) {
    const id = `ws-asig-${normalizarTitular(titular)}`
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { kind: 'individualAsignacion' as const, id, titular, color, activeSubTabId: 'asignacion' }]
    })
  }

  // "Asignar" del toolbar en modo "Selección múltiple" (titulares mezclados) — a lo sumo una
  // instancia (id fijo 'ws-multiple'). Subtab único "Asignación" con `mostrarHistorial=false`.
  function abrirWorkspaceMultiple() {
    const id = 'ws-multiple'
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { kind: 'multiple' as const, id, color, activeSubTabId: 'asignacionMultiple' }]
    })
  }

  // 20/08/2026, pedido explícito del usuario: "Asignar" ya no decide individual/múltiple por
  // CANTIDAD de filas marcadas — decide por si la selección pertenece TODA al mismo titular (1 o más
  // expedientes de esa misma persona) o mezcla titulares distintos. Con 1 solo titular representado
  // abre/reutiliza el workspace por titular (mismo tab con su punto de color, subpestaña
  // "Asignación" única); con 2+ titulares distintos abre "Selección múltiple" como antes.
  function handleAsignarSeleccion() {
    if (expedientesSeleccionados.length === 0) return
    const titulares = new Set(expedientesSeleccionados.map((e) => normalizarTitular(e.titular)))
    if (titulares.size === 1) {
      abrirWorkspaceAsignacionTitular(expedientesSeleccionados[0].titular)
      return
    }
    abrirWorkspaceMultiple()
  }

  // 20/08/2026, pedido explícito del usuario: mientras haya un workspace 'individualAsignacion'
  // abierto (para un titular específico), la grilla no debe permitir marcar checkboxes de OTROS
  // titulares — evita que el usuario amplíe por error el alcance de una asignación ya enfocada en 1
  // persona. Se evalúa por "está abierto" (presente en `workspacesAbiertos`), no por "está en foco
  // en este instante": el usuario puede cambiar de tab para revisar otra cosa sin que la restricción
  // desaparezca mientras la asignación siga pendiente. Al cerrar ese tab, la restricción se levanta
  // sola (ya no hay ningún workspace de ese tipo en la lista).
  const workspaceTitularActivo = workspacesAbiertos.find(
    (w): w is Extract<WorkspaceAbierto, { kind: 'individualAsignacion' }> => w.kind === 'individualAsignacion',
  )

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
        // Workspace "Selección múltiple" (titulares mezclados): un único subtab "Asignación", sin
        // Historial (cada expediente tiene el suyo propio, se consulta entrando a su propio
        // workspace por titular). `expedientesSeleccionados` es la selección EN VIVO (no un
        // snapshot, ver Javadoc de `WorkspaceAbierto` más arriba).
        if (w.kind === 'multiple') {
          const cantidad = expedientesSeleccionados.length
          return {
            id: w.id,
            label: `Selección múltiple (${cantidad} expediente${cantidad === 1 ? '' : 's'})`,
            color: w.color,
            activeSubTabId: w.activeSubTabId,
            subTabs: [
              {
                id: 'asignacionMultiple',
                label: 'Asignación',
                color: COLOR_ASIGNACION,
                content: (
                  <AsignacionSeleccionContent
                    expedientes={expedientesSeleccionados}
                    mostrarHistorial={false}
                    onAsignado={() => {
                      buscar()
                      setRowSelectionModel(SIN_SELECCION)
                    }}
                  />
                ),
              },
            ],
          }
        }
        // Workspace "Asignación por titular" (20/08/2026, pedido explícito del usuario): igual que
        // 'multiple' pero con un único titular (1+ expedientes de esa misma persona, restringido por
        // `isRowSelectable` mientras este tab siga abierto) — sí muestra Historial, y el tab usa el
        // mismo lenguaje visual de "workspace por titular" (label = nombre del titular, punto de
        // color) que el workspace de doble clic. Sin Datos/Asociar/Grupo Familiar: ese flujo vive
        // exclusivamente en el workspace de doble clic (kind 'individual', más abajo).
        if (w.kind === 'individualAsignacion') {
          return {
            id: w.id,
            label: w.titular || 'Sin titular',
            color: w.color,
            activeSubTabId: w.activeSubTabId,
            subTabs: [
              {
                id: 'asignacion',
                label: 'Asignación',
                color: COLOR_ASIGNACION,
                content: (
                  <AsignacionSeleccionContent
                    expedientes={expedientesSeleccionados}
                    mostrarHistorial
                    onAsignado={() => {
                      buscar()
                      setRowSelectionModel(SIN_SELECCION)
                    }}
                  />
                ),
              },
            ],
          }
        }
        // Workspace "Datos" (doble clic sobre una fila, 20/08/2026: ya NO incluye la subpestaña
        // "Asignación" — esa acción vive exclusivamente en los 2 workspaces de arriba, abiertos desde
        // el toolbar "Asignar"). Subtabs: Datos, Asociar, Grupo Familiar.
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
                  onGuardado={() => {
                    recargarDatosExpediente(w.row.idExpediente)
                    buscar()
                  }}
                />
              ),
            },
            {
              id: 'asociar',
              label: 'Asociar',
              color: COLOR_ASOCIAR,
              content: <AsociarDuplicadosContent idExpedientePrincipal={w.row.idExpediente} onAsociado={buscar} />,
            },
            {
              id: 'grupoFamiliar',
              label: 'Grupo Familiar',
              color: COLOR_GRUPO_FAMILIAR,
              content: (
                <GrupoFamiliarContent
                  idExpedientePrincipal={w.row.idExpediente}
                  numeroExpediente={w.row.numeroExpediente}
                  titular={w.row.titular}
                  onAsociado={buscar}
                />
              ),
            },
          ],
        }
      }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [workspacesAbiertos, datosPorExpediente, expedientesSeleccionados],
  )

  // Mismo orden exacto que AsignacionTableModel en V2 (JPanelAsignacionV2): Días, Nro. Expediente,
  // N° expediente SGD, Fecha Solicitud, Fecha Vencimiento, Proc. Registral, Tipo Acta, Nro. Acta,
  // Titular, Abogado actual, Etapa, Estado, Alertas — más la columna "expandir/eliminar" al inicio
  // (19/08/2026, mismo patrón que RegistroBandejaPage.tsx). Las columnas "Asociar"/"Grupo
  // Familiar"/"Historial" se retiraron: "Asociar"/"Grupo Familiar" viven en las subpestañas del
  // workspace de doble clic; "Asignación"+historial embebido vive en los workspaces que abre la
  // acción "Asignar" del toolbar (`AsignacionSeleccionContent.tsx`, ver Javadoc de la página).
  const columnas: GridColDef<FilaAsignacion>[] = [
    {
      // Mismo patrón que RegistroBandejaPage.tsx: ícono de expandir asociados + ícono de eliminar
      // en la misma columna, posición horizontal fija. Filas asociadas no muestran ningún ícono
      // propio, solo la franja de color de su grupo.
      field: 'expandir',
      headerName: '',
      width: 84,
      sortable: false,
      filterable: false,
      renderCell: (params) => {
        const row = params.row
        if (row.esFilaAsociada) {
          return <Box sx={{ height: '100%', borderLeft: `5px solid ${acentoGrupo(row.idPrincipal ?? 0)}` }} />
        }
        const puedeExpandir = row.asociadosConfirmados > 0
        const puedeEliminar = row.etapaCodigo === 'REGISTRO' && row.estadoCodigo === 'REGISTRADO' && !row.asignacionActiva
        const expandido = expandidoId === row.idExpediente
        return (
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            {cargandoExpansion === row.idExpediente ? (
              <CircularProgress size={16} sx={{ ml: 1, mr: 1 }} />
            ) : puedeExpandir ? (
              <IconButton size="small" onClick={() => alternarExpansion(row)} aria-label={expandido ? 'Colapsar asociados' : 'Expandir asociados'}>
                {expandido ? <RemoveCircleOutlineIcon fontSize="small" /> : <AddCircleOutlineIcon fontSize="small" />}
              </IconButton>
            ) : (
              <Box sx={{ width: 34 }} />
            )}
            <Tooltip title={puedeEliminar ? 'Eliminar' : 'Solo se puede eliminar mientras el expediente esté en Registro, sin asignar'}>
              <span>
                <IconButton size="small" onClick={() => setExpedienteAEliminar(row)} disabled={!puedeEliminar} aria-label="Eliminar">
                  <DeleteOutlinedIcon fontSize="small" />
                </IconButton>
              </span>
            </Tooltip>
          </Box>
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
    { field: 'numeroExpediente', headerName: 'Nro. Expediente', width: 190 },
    { field: 'numeroExpedienteSgd', headerName: 'N° expediente SGD', width: 160 },
    {
      field: 'fechaRecepcion',
      headerName: 'Fecha Solicitud',
      width: 130,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    {
      field: 'fechaVencimiento',
      headerName: 'Fecha Vencimiento',
      width: 140,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    { field: 'procedimiento', headerName: 'Proc. Registral', width: 170 },
    { field: 'tipoActa', headerName: 'Tipo Acta', width: 150 },
    { field: 'numeroActa', headerName: 'Nro. Acta', width: 110 },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    { field: 'abogadoAsignado', headerName: 'Abogado actual', width: 180 },
    { field: 'etapaCodigo', headerName: 'Etapa', width: 130 },
    { field: 'estadoCodigo', headerName: 'Estado', width: 130 },
    {
      field: 'alertaIngreso',
      headerName: 'Alertas',
      width: 170,
      renderCell: (params) => <StatusBadge label={params.value as string} tone={alertaBadgeTone(params.value as string)} />,
    },
  ]

  // Envuelve el sortComparator de cada columna con agruparHijosBajoPadre (19/08/2026, mismo patrón
  // que RegistroBandejaPage.tsx) — aplicado una sola vez, cualquier columna nueva lo hereda.
  const columnasConAgrupacion = useMemo<GridColDef<FilaAsignacion>[]>(
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
      <TextField select label="Estado" size="small" sx={{ minWidth: 180 }} value={estado} onChange={(e) => setEstado(e.target.value)}>
        <MenuItem value="">Todos los estados</MenuItem>
        <MenuItem value="REGISTRADO">Registrado</MenuItem>
        <MenuItem value="ASIGNADO">Asignado</MenuItem>
      </TextField>
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Bandeja Asignación"
      subtitulo="Expedientes registrados pendientes de asignar a un equipo y abogado responsable."
      tabs={ASIGNACION_TABS}
      filtros={filtrosHeader}
    >
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      {mensajeGrupoFamiliarMasivo && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setMensajeGrupoFamiliarMasivo(null)}>
          {mensajeGrupoFamiliarMasivo}
        </Alert>
      )}

      {/* KPIs en grid de columnas iguales (19/08/2026, mismo patrón que RegistroBandejaPage.tsx —
          antes era un Stack flexWrap que los agrupaba a la izquierda dejando espacio vacío). */}
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 2, mb: 2 }}>
        <Box onClick={() => setFiltroKpi(null)} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Total de expedientes" value={kpis.total} caption="Según filtros aplicados" color={sdrercColors.primary} />
        </Box>
        <Box onClick={() => alternarFiltro('PENDIENTES')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Pendientes" value={kpis.pendientes} caption="Para asignación" color={sdrercColors.info} />
        </Box>
        <Box onClick={() => alternarFiltro('POTENCIAL_DUPLICADO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Potencial duplicado" value={kpis.potencialDuplicado} caption="Acta + titular" color={sdrercColors.warning} />
        </Box>
        <Box onClick={() => alternarFiltro('POSIBLE_GRUPO_FAMILIAR')} sx={{ cursor: 'pointer' }}>
          <MetricCard
            label="Posible Grupo Familiar"
            value={kpis.posibleGrupoFamiliar}
            caption="Apellidos coincidentes"
            color={sdrercColors.primary}
          />
        </Box>
        <Box onClick={() => alternarFiltro('GRUPO_FAMILIAR_CONFIRMADO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Grupo Familiar Confirmado" value={kpis.grupoFamiliarConfirmado} caption="Registrado" color={sdrercColors.success} />
        </Box>
        <Box onClick={() => alternarFiltro('POR_VENCER')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Por vencer" value={kpis.porVencer} caption="0 a 5 días hábiles" color={sdrercColors.warning} />
        </Box>
        <Box onClick={() => alternarFiltro('VENCIDOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Vencidos" value={kpis.vencidos} caption="Plazo excedido" color={sdrercColors.error} />
        </Box>
      </Box>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2, mb: 3 }}>
        <DataGrid
          rows={filasConHijos}
          columns={columnasConAgrupacion}
          getRowId={(row) => row.idExpediente}
          loading={loading}
          density="compact"
          autoHeight
          // 19/08/2026, pedido explícito del usuario (3er intento — mismo diagnóstico que
          // RegistroBandejaPage.tsx, ver Javadoc allá y v3/CLAUDE.md): `disableVirtualization` es
          // el único prop real/vigente para que TODAS las celdas queden montadas en el DOM (el
          // "column pinning" nativo nunca se activa en esta edición, y `disableColumnVirtualization`
          // es un prop obsoleto sin efecto) — necesario para que `position: sticky`
          // (COLUMNAS_FIJAS_INICIO_SX) funcione de forma confiable en todo el rango de scroll.
          disableVirtualization
          checkboxSelection
          // 19/08/2026, 2ª pasada, pedido explícito del usuario (fix de bug real): antes exigía
          // `row.asignable || (modoReasignacion && row.asignacionActiva)` — con "Habilitar
          // reasignación" retirado del sistema (vivía dentro del panel, que a su vez solo podía
          // abrirse con una selección ya hecha: un candado sin llave), los expedientes en estado
          // "Asignado" nunca podían marcarse. Ahora cualquier fila raíz (no asociada/hija) es
          // seleccionable sin importar su estado — el tipo de operación (asignación inicial vs.
          // reasignación) se detecta y se muestra automáticamente por fila dentro del panel de
          // asignación (`AsignacionSeleccionContent.tsx`), no se decide de antemano acá.
          //
          // 20/08/2026, pedido explícito del usuario: además, mientras haya un workspace de
          // asignación "por titular" abierto (`workspaceTitularActivo`), las filas de OTRO titular
          // quedan sin poder marcarse — evita mezclar por error la selección de una asignación ya
          // enfocada en 1 persona con expedientes de otra.
          isRowSelectable={(params) => {
            const row = params.row as FilaAsignacion
            if (row.esFilaAsociada) return false
            if (workspaceTitularActivo && normalizarTitular(row.titular) !== normalizarTitular(workspaceTitularActivo.titular)) {
              return false
            }
            return true
          }}
          rowSelectionModel={rowSelectionModel}
          onRowSelectionModelChange={setRowSelectionModel}
          showToolbar
          slots={{ toolbar: ToolbarBandejaAsignacion }}
          slotProps={{
            toolbar: {
              seleccionados: idsSeleccionados,
              onDeseleccionar: () => setRowSelectionModel(SIN_SELECCION),
              onAsociarGrupoFamiliar: asociarSeleccionadosGrupoFamiliar,
              procesandoGrupoFamiliar: procesandoGrupoFamiliarMasivo,
              onAsignar: handleAsignarSeleccion,
            },
          }}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          getRowClassName={(params) =>
            params.row.esFilaAsociada ? 'fila-asociada' : expandidoId === params.row.idExpediente ? 'fila-principal-expandida' : ''
          }
          sx={{
            '& .fila-asociada': { bgcolor: ASSOCIATED_ROW_BACKGROUND, color: sdrercColors.textSecondary, fontSize: '0.8125rem' },
            '& .fila-principal-expandida': { bgcolor: EXPANDED_PARENT_BACKGROUND },
            ...COLUMNAS_FIJAS_INICIO_SX,
          }}
          onRowDoubleClick={(params) => {
            const row = params.row as FilaAsignacion
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

      {/* Confirmación de eliminar (19/08/2026, mismo flujo/backend que RegistroBandejaPage.tsx). */}
      <Dialog open={expedienteAEliminar !== null} onClose={() => (!eliminando ? setExpedienteAEliminar(null) : undefined)}>
        <DialogTitle>Eliminar expediente</DialogTitle>
        <DialogContent>
          <Typography variant="body2">
            ¿Desea eliminar (baja lógica) el expediente{' '}
            <strong>{expedienteAEliminar?.numeroExpediente || expedienteAEliminar?.titular}</strong>? Esta acción no se puede deshacer desde
            la bandeja.
          </Typography>
          {cargandoImpacto && (
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mt: 2 }}>
              <CircularProgress size={16} />
              <Typography variant="body2" color="text.secondary">
                Verificando relaciones activas del expediente…
              </Typography>
            </Stack>
          )}
          {!cargandoImpacto && mensajeImpactoEliminar && (
            <Alert severity="warning" sx={{ mt: 2 }}>
              {mensajeImpactoEliminar}
            </Alert>
          )}
          {errorEliminar && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {errorEliminar}
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setExpedienteAEliminar(null)} disabled={eliminando}>
            Cancelar
          </Button>
          <Button color="error" variant="contained" onClick={confirmarEliminar} disabled={eliminando}>
            Eliminar
          </Button>
        </DialogActions>
      </Dialog>
    </PageHeaderSticky>
  )
}
