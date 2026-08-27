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
  type GridColumnVisibilityModel,
  type GridComparatorFn,
  type GridRowSelectionModel,
} from '@mui/x-data-grid'
import { buscarBandejaRegistro, obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import { listarConfirmados, type ExpedienteRelacionado } from '../api/expedienteRelacionadoApi'
import { asociarGrupoFamiliar } from '../api/grupoFamiliarApi'
import {
  eliminarExpedienteRegistro,
  obtenerImpactoEliminacionRegistro,
  type ExpedienteEdicionManual,
  type ImpactoEliminacionRegistro,
} from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, alertaBadgeTone, diasBadgeTone } from '../components/StatusBadge'
import { AsociarDuplicadosContent } from '../components/AsociarDuplicadosContent'
import { GrupoFamiliarContent } from '../components/GrupoFamiliarContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { REGISTRO_TABS } from '../layout/moduleTabs'
import { primerDiaMesActualISO, hoyISO } from '../utils/fechas'

// Mismos 10 tonos que GROUP_STRIPE_COLORS (JPanelBandejaExpedientesNueva, V2): rotación
// determinística por id del expediente principal (id % 10), no aleatoria.
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
// Mismos fondos que EXPANDED_PARENT_BACKGROUND/EXPANDED_ASSOCIATED_BACKGROUND (V2).
const EXPANDED_PARENT_BACKGROUND = 'rgb(205, 236, 244)'
const ASSOCIATED_ROW_BACKGROUND = 'rgb(238, 250, 252)'

// Columnas fijas al inicio (checkbox de selección + "+"/eliminar) al desplazar horizontalmente
// (19/08/2026, 3er intento, pedido explícito del usuario — ver el diagnóstico completo en el
// `sx`/`disableVirtualization` del <DataGrid> más abajo y en v3/CLAUDE.md: ni el "column pinning"
// nativo de MUI X ni `disableColumnVirtualization` funcionan en la versión instalada; este truco
// `position: sticky` SÍ funciona, siempre que TODAS las celdas estén montadas en el DOM —
// `disableVirtualization` en el <DataGrid> se encarga de eso).
// 50px = ancho por defecto de la columna de checkbox de MUI X (`GRID_CHECKBOX_SELECTION_COL_DEF`),
// estable entre versiones; no hay forma de leerlo en tiempo de ejecución sin acoplarse a internals
// de MUI, así que se usa el valor documentado directo.
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
  // Para que la celda fija de una fila coloreada (asociada/expandida) no muestre el fondo blanco
  // por defecto de arriba en vez de mezclarse con el resto de su propia fila.
  '& .fila-asociada .MuiDataGrid-cell[data-field="__check__"], & .fila-asociada .MuiDataGrid-cell[data-field="expandir"]': {
    bgcolor: ASSOCIATED_ROW_BACKGROUND,
  },
  '& .fila-principal-expandida .MuiDataGrid-cell[data-field="__check__"], & .fila-principal-expandida .MuiDataGrid-cell[data-field="expandir"]': {
    bgcolor: EXPANDED_PARENT_BACKGROUND,
  },
} as const

// Paleta de identidad por titular (pestaña superior del panel multipestañas): distinta de
// GROUP_STRIPE_COLORS (esa es para la franja de filas asociadas en la grilla) para que ambos usos
// no se confundan visualmente. Se asigna por orden de apertura, no por hash del id.
const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
// Color fijo por tipo de subpestaña — consistente entre todos los expedientes abiertos, para que
// "Datos" siempre se identifique con el mismo color sin importar de quién sea la pestaña.
const COLOR_DATOS = '#2370A8'
const COLOR_ASOCIAR = '#B86910'
const COLOR_GRUPO_FAMILIAR = '#7B4FA0'

interface FilaBandeja extends ExpedienteBandeja {
  esFilaAsociada?: boolean
  idPrincipal?: number
}

/**
 * Envuelve el `sortComparator` de una columna (18/08/2026, pedido explícito del usuario: "sin
 * importar el criterio de ordenamiento... las filas hijas deben ubicarse siempre inmediatamente
 * debajo de su fila padre") para que las filas hijas sintéticas (`esFilaAsociada`, ver
 * `filasConHijos`) nunca se ordenen por su propio valor, sino por el valor de esa misma columna en
 * su fila PADRE (`idPrincipal`). Con eso, en el criterio de orden activo, cada hija "vale lo mismo"
 * que su padre — el sort de MUI (estable) las agrupa en la misma posición, y como `filasConHijos` ya
 * inserta cada hija justo después de su padre en el array de entrada, ese orden relativo se conserva
 * dentro del grupo "empatado" sin importar qué columna se esté ordenando. Se aplica una sola vez
 * sobre todas las columnas (`columnasConAgrupacion`, más abajo) en vez de reescribir cada
 * `sortComparator` individualmente.
 */
function agruparHijosBajoPadre(comparadorOriginal?: GridComparatorFn<unknown>): GridComparatorFn<unknown> {
  return (v1, v2, params1, params2) => {
    const fila1 = params1.api.getRow(params1.id) as FilaBandeja | null
    const fila2 = params2.api.getRow(params2.id) as FilaBandeja | null
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

// Mismo mapeo que alertaAsociadaRegistro (V2): la alerta de una fila hija se deriva de
// ExpedienteRelacionado.alertaIngreso, no de la columna "alertas" del DTO de bandeja.
function alertaAsociada(asociado: ExpedienteRelacionado): string {
  const alerta = (asociado.alertaIngreso ?? '').toLowerCase()
  if (alerta.includes('potencial duplicado')) return 'Potencial duplicado'
  if (alerta.includes('posible grupo familiar')) return 'Posible Grupo Familiar'
  if (alerta.includes('con observaciones')) return 'Con observaciones'
  return 'Duplicado confirmado'
}

type FiltroAlerta = 'POTENCIAL_DUPLICADO' | 'POSIBLE_GRUPO_FAMILIAR' | 'GRUPO_FAMILIAR_CONFIRMADO' | null

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

// Ocultas en la carga inicial (a pedido del usuario, 11/08/2026): siguen disponibles vía el
// botón "Columns" del toolbar (Manage columns). Al ser estado de componente (no persistido en
// localStorage/sessionStorage), la visibilidad vuelve sola a este default apenas se sale de la
// página y se vuelve a entrar — no hace falta resetearlo a mano al cambiar de módulo.
const COLUMNAS_OCULTAS_POR_DEFECTO: GridColumnVisibilityModel = {
  canal: false,
  tipoActa: false,
  numeroActa: false,
}

// Habilita pasar props propias al slot `toolbar` de forma tipada, sin `as any` — patrón
// documentado de MUI X para slots con props custom.
declare module '@mui/x-data-grid' {
  interface ToolbarPropsOverrides {
    seleccionados: number[]
    onDeseleccionar: () => void
    onAsociarGrupoFamiliar: () => void
    procesandoGrupoFamiliar: boolean
  }
}

interface ToolbarBandejaRegistroProps {
  seleccionados: number[]
  onDeseleccionar: () => void
  onAsociarGrupoFamiliar: () => void
  procesandoGrupoFamiliar: boolean
}

/**
 * Toolbar de la grilla: contador "N seleccionados" + acciones de selección múltiple a la
 * izquierda (oculto por completo sin selección), controles estándar (columnas/filtro/exportar/
 * buscar) a la derecha — misma franja (13/08/2026, pedido explícito del usuario, segunda pasada:
 * reemplaza los botones "Carga diaria"/"+ Registro manual" que se habían puesto acá en el
 * incremento anterior — ya son redundantes con esas mismas 2 pestañas de `REGISTRO_TABS` en la
 * Fila 1 del header, "no se pierde funcionalidad, solo se elimina la duplicidad").
 *
 * Acciones elegidas deliberadamente ("las que tengan sentido... según el resto del sistema", no
 * las 3 sugeridas literalmente por el usuario): "Exportar seleccionados" (real, usa
 * `apiRef.exportDataAsCsv` acotado a `seleccionados`) y "Deseleccionar todo" (real, limpia
 * `rowSelectionModel`). Se descartó "Asignar" del ejemplo del pedido — Registro no tiene un flujo
 * de asignación a abogado propio (eso es exclusivamente de Asignación, `AsignacionBandejaPage`);
 * agregar un botón "Asignar" acá habría sido una acción sin implementación real detrás.
 *
 * **"Asociar al grupo familiar (N)" agregado el 13/08/2026** (pedido explícito del usuario: "revisa
 * bien el panel de grupo familiar de la v2 y replícalo... respetando el diseño de la v3"): replica
 * la sección "Selección múltiple marcada" del panel real de V2 (`JPanelRegistrarGrupoFamiliarV2`,
 * botones "Asociar seleccionados"/"Limpiar selección" atados al checkbox de la bandeja principal, NO
 * al panel lateral) — pero reutilizando la selección por checkbox que V3 YA tiene en esta grilla
 * (antes solo servía para exportar/deseleccionar) en vez de una columna de checkbox paralela como
 * en V2 (ahí "Registrar G.F" y esta selección múltiple son 2 mecanismos de marcado distintos porque
 * V2 no tenía un checkbox de selección general en esa grilla). Habilitado con 2+ seleccionados,
 * deshabilitado con menos (mismo umbral que V2).
 *
 * Definido a nivel de módulo (no dentro de `RegistroBandejaPage`) a propósito: `slots.toolbar` de
 * `DataGrid` espera una referencia de componente ESTABLE — si se definiera inline en el cuerpo del
 * componente, sería una función nueva en cada render, y MUI la trataría como un tipo de componente
 * distinto en cada re-render, desmontando y remontando el toolbar completo (perdiendo el foco del
 * cuadro de búsqueda rápida en cada tecla escrita). `seleccionados`/`onDeseleccionar` sí varían por
 * render y se pasan vía `slotProps.toolbar` (props normales, sin ese problema).
 *
 * Se usan los building blocks "legacy" de MUI X (`GridToolbarContainer`/`GridToolbarColumnsButton`/
 * `GridToolbarFilterButton`/`GridToolbarExport`/`GridToolbarQuickFilter`, marcados `@deprecated` en
 * v9 a favor de primitivos nuevos con patrón `render` prop) en vez de reimplementar la composición
 * con los primitivos nuevos: siguen completamente funcionales (deprecated ≠ roto), y reimplementar
 * a mano el menú de exportación/badge de filtros/tooltips localizados sin poder probarlo en un
 * navegador real habría sido un riesgo real de regresión silenciosa.
 */
function ToolbarBandejaRegistro({
  seleccionados,
  onDeseleccionar,
  onAsociarGrupoFamiliar,
  procesandoGrupoFamiliar,
}: ToolbarBandejaRegistroProps) {
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

const SIN_SELECCION: GridRowSelectionModel = { type: 'include', ids: new Set() }

/**
 * Equivalente web de la pestaña "Bandeja Registro" (V2). Alcance hasta ahora: listado + filtros +
 * 3 KPIs (calculados en memoria sobre la lista ya cargada, igual que
 * JPanelBandejaExpedientesNueva.actualizarMetricasAlertasRegistro/filtrarPorAlertaRegistro en V2
 * — no una consulta aparte) y expansión de expedientes asociados/duplicados (icono "+" por fila,
 * franja de color por grupo, fondo celeste, texto atenuado — mismo patrón visual que
 * `iconoExpansionRegistro`/`insertarAsociadosRegistro` de V2, adaptado a `DataGrid` intercalando
 * filas hijas sintéticas en vez de un `JTable` mutable). Fuente de los asociados:
 * `listarConfirmados` (expedientes ya confirmados como relacionados). Solo un expediente principal
 * expandido a la vez, igual que V2.
 *
 * **Columna "Editar" eliminada (13/08/2026, pedido explícito del usuario)**: navegaba a
 * `/registro/editar/:idExpediente` (`RegistroEdicionManualPage.tsx`) — reemplazada por el ícono de
 * edición inline del Panel de datos (`PanelDatosExpediente.tsx`, subpestaña "Datos" del workspace),
 * que cubre el mismo caso de uso sin salir de la bandeja. La ruta dedicada `/registro/editar/:id` NO
 * se eliminó (nadie pidió borrar la página en sí, solo el botón de esta grilla) — sigue accesible
 * por URL directa si se necesita.
 *
 * Doble clic en una fila (no asociada) abre un "workspace" — panel multipestañas de 2 niveles
 * estilo consola (ver `WorkspaceTabsDrawer.tsx`): una pestaña superior por titular (color propio
 * por persona) con 3 subpestañas fijas debajo — Datos (solo lectura), Asociar y Grupo Familiar
 * (`AsociarDuplicadosContent`/`GrupoFamiliarContent`, extraídos de los `Dialog` que usa Asignación
 * para poder embeberse aquí sin modal). Pueden quedar varios expedientes abiertos a la vez, cada
 * uno con su propia subpestaña activa. Diseño aprobado explícitamente por el usuario tras una
 * consulta de UX (12/08/2026), con foto de referencia de Salesforce Console
 * (`docs/arquitectura_app/screen-multipestañas.jpeg`).
 */
export function RegistroBandejaPage() {
  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaMesActualISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [estado, setEstado] = useState('')
  const [expedientes, setExpedientes] = useState<ExpedienteBandeja[]>([])
  const [filtroAlerta, setFiltroAlerta] = useState<FiltroAlerta>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [columnVisibilityModel, setColumnVisibilityModel] = useState<GridColumnVisibilityModel>(
    COLUMNAS_OCULTAS_POR_DEFECTO,
  )
  // Selección múltiple (checkbox): mismo patrón que AsignacionBandejaPage.tsx. Filas asociadas
  // (esFilaAsociada) no son seleccionables — no son expedientes independientes, ver isRowSelectable
  // en el DataGrid más abajo.
  const [rowSelectionModel, setRowSelectionModel] = useState<GridRowSelectionModel>(SIN_SELECCION)
  // Expansión de filas asociadas (icono "+"): solo un expediente principal expandido a la vez,
  // igual que contraerTodosExceptoRegistro en V2.
  const [expandidoId, setExpandidoId] = useState<number | null>(null)
  const [asociadosPorPrincipal, setAsociadosPorPrincipal] = useState<Record<number, ExpedienteRelacionado[]>>({})
  const [cargandoExpansion, setCargandoExpansion] = useState<number | null>(null)
  // Eliminar (baja lógica, mismo icono que V2): modal de confirmación antes de ejecutar.
  const [expedienteAEliminar, setExpedienteAEliminar] = useState<FilaBandeja | null>(null)
  const [eliminando, setEliminando] = useState(false)
  const [errorEliminar, setErrorEliminar] = useState<string | null>(null)
  // Impacto de la eliminación (13/08/2026, pedido explícito del usuario): se consulta al abrir el
  // modal para avisar si el expediente tiene alertas activas/pertenece a un grupo familiar
  // confirmado que también se actualizarán en cascada. No bloquea el modal si falla — el usuario
  // igual puede confirmar la eliminación sin el aviso (la cascada real la hace el backend siempre).
  const [impactoEliminar, setImpactoEliminar] = useState<ImpactoEliminacionRegistro | null>(null)
  const [cargandoImpacto, setCargandoImpacto] = useState(false)
  // "Asociar al grupo familiar" masivo (13/08/2026, replica "Selección múltiple marcada" de V2 —
  // ver Javadoc de ToolbarBandejaRegistro): usa la MISMA selección por checkbox de la grilla.
  const [procesandoGrupoFamiliarMasivo, setProcesandoGrupoFamiliarMasivo] = useState(false)
  const [mensajeGrupoFamiliarMasivo, setMensajeGrupoFamiliarMasivo] = useState<string | null>(null)

  // Panel multipestañas (WorkspaceTabsDrawer): una pestaña por expediente/titular abierto (doble
  // clic), cada una con 3 subpestañas fijas — Datos/Asociar/Grupo Familiar. Varios expedientes
  // pueden estar abiertos a la vez; cada uno mantiene su propia subpestaña activa y su propio
  // estado de carga de "Datos".
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<{ id: string; row: ExpedienteBandeja; color: string; activeSubTabId: string }[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [datosPorExpediente, setDatosPorExpediente] = useState<
    Record<number, { detalle: ExpedienteEdicionManual | null; cargando: boolean; error: string | null }>
  >({})

  const buscar = useCallback(
    (params?: { texto?: string; desde?: string; hasta?: string; estado?: string }) => {
      setLoading(true)
      setError(null)
      buscarBandejaRegistro({
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

  function abrirWorkspace(row: ExpedienteBandeja) {
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

  // Extraído de abrirWorkspace (13/08/2026, pedido explícito del usuario: edición inline en la
  // subpestaña "Datos") para poder reutilizarlo como refresco forzado tras guardar una edición —
  // a diferencia de abrirWorkspace, este SIEMPRE vuelve a pedir el detalle (sin el guard de "ya
  // está en caché"), porque el propósito es traer los datos recién guardados.
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
        // Foco al tab adyacente por posición (el que queda en el índice del cerrado, o el
        // anterior si se cerró el último) — mismo criterio que un navegador, no "siempre el
        // último de la lista" (pedido explícito del usuario, 13/08/2026).
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
                  onGuardado={() => recargarDatosExpediente(w.row.idExpediente)}
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
    [workspacesAbiertos, datosPorExpediente],
  )

  function alternarExpansion(principal: ExpedienteBandeja) {
    if (principal.relacionesConfirmadasComoPrincipal <= 0) return
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

  useEffect(() => {
    buscar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function handleBuscar() {
    setFiltroAlerta(null)
    buscar()
  }

  function handleLimpiar() {
    const desdeDefault = primerDiaMesActualISO()
    const hastaDefault = hoyISO()
    setTexto('')
    setDesde(desdeDefault)
    setHasta(hastaDefault)
    setEstado('')
    setFiltroAlerta(null)
    buscar({ texto: '', desde: desdeDefault, hasta: hastaDefault, estado: '' })
  }

  function coincideAlertaRegistro(item: ExpedienteBandeja): boolean {
    if (filtroAlerta === 'POTENCIAL_DUPLICADO') return item.alertas === 'Potencial duplicado'
    if (filtroAlerta === 'POSIBLE_GRUPO_FAMILIAR') return item.alertas === 'Posible Grupo Familiar'
    if (filtroAlerta === 'GRUPO_FAMILIAR_CONFIRMADO') return item.grupoFamiliarConfirmado
    return true
  }

  // 19/08/2026, pedido explícito del usuario: un expediente que ya quedó confirmado como duplicado
  // (asociado) de otro principal (`relacionadoComoHijo`, ya calculado por el backend —
  // `es_relacionado_hijo` en `ExpedienteBandejaDAO`) ya no debe listarse como fila RAÍZ independiente
  // — solo debe verse como fila hija al expandir su principal (icono "+"), igual que ya funciona para
  // Grupo Familiar. `expedientes` (estado crudo, tal como llega del backend) se conserva SIN filtrar
  // para que los KPIs de alertas (`potencialDuplicado`/`posibleGrupoFamiliar`/`grupoFamiliarConfirmado`
  // más abajo) sigan contando correctamente sin importar si la fila es visible a nivel raíz o no —
  // "Total de expedientes" es la ÚNICA excepción (decisión explícita del usuario): debe reflejar
  // exactamente lo que se ve a nivel raíz, así que se calcula sobre `expedientesRaiz`, no sobre
  // `expedientes`.
  const expedientesRaiz = useMemo(() => expedientes.filter((e) => !e.relacionadoComoHijo), [expedientes])

  const kpis = useMemo(
    () => ({
      total: expedientesRaiz.length,
      potencialDuplicado: expedientes.filter((e) => e.alertas === 'Potencial duplicado').length,
      posibleGrupoFamiliar: expedientes.filter((e) => e.alertas === 'Posible Grupo Familiar').length,
      grupoFamiliarConfirmado: expedientes.filter((e) => e.grupoFamiliarConfirmado).length,
    }),
    [expedientes, expedientesRaiz],
  )

  const filas = useMemo(() => expedientesRaiz.filter(coincideAlertaRegistro), [expedientesRaiz, filtroAlerta])

  // Intercala filas hijas sintéticas justo debajo de su principal cuando está expandido — mismo
  // patrón que insertarAsociadosRegistro (V2), adaptado a DataGrid (que no soporta filas
  // heterogéneas insertadas a mano tan naturalmente como un JTable). La fila hija hereda N°
  // Expediente y Canal del principal (V2 hace lo mismo: esos 2 datos "pertenecen" al principal),
  // el resto viene del asociado.
  const filasConHijos = useMemo<FilaBandeja[]>(() => {
    const resultado: FilaBandeja[] = []
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
            // 19/08/2026, pedido explícito del usuario: antes se guardaba `asoc.etapaEstado` (texto
            // combinado del backend, ej. "Registro/Registrado") directo en `estadoCodigo`, mezclando
            // Etapa y Estado en la misma celda — ahora que existe la columna "Etapa" propia, cada
            // campo va a su celda correspondiente, igual que en la fila padre.
            etapaCodigo: asoc.etapaCodigo || '-',
            estadoCodigo: asoc.estadoCodigo || 'Documento asociado',
            alertas: alertaAsociada(asoc),
            esFilaAsociada: true,
            idPrincipal: item.idExpediente,
          })
        }
      }
    }
    return resultado
  }, [filas, expandidoId, asociadosPorPrincipal])

  // Mismo criterio que idsSeleccionados en AsignacionBandejaPage.tsx: se cruza contra `expedientes`
  // (la lista real, sin filas hijas sintéticas de negativo) para quedarnos solo con ids reales.
  const idsSeleccionados = useMemo(() => {
    return expedientes
      .filter((f) =>
        rowSelectionModel.type === 'include' ? rowSelectionModel.ids.has(f.idExpediente) : !rowSelectionModel.ids.has(f.idExpediente),
      )
      .map((f) => f.idExpediente)
  }, [expedientes, rowSelectionModel])

  // "Asociar al grupo familiar" masivo (13/08/2026, pedido explícito del usuario): replica
  // asociarMarcadosMasivo (V2) — el primer id marcado actúa como principal del grupo, el resto se
  // asocia contra él. Mismo mensaje de confirmación (solo marca "Sí" en Grupo Familiar, no asocia
  // expedientes entre sí) y mismo backend (`asociarGrupoFamiliar`) que usa GrupoFamiliarContent.tsx
  // para el flujo de "posibles integrantes" — es el mismo servicio, solo cambia el origen de los ids.
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

  function alternarFiltro(valor: FiltroAlerta) {
    setFiltroAlerta((prev) => (prev === valor ? null : valor))
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

  // Consulta el impacto apenas se abre el modal (setExpedienteAEliminar), no antes — evita pedirlo
  // para cada fila de la grilla. Se descarta si el usuario cierra/cambia de fila antes de que
  // responda (guarda `cancelado`), para no pisar el impacto de una fila distinta con una respuesta
  // tardía de otra.
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

  // 20/08/2026, pedido explícito del usuario (regla de "tamaño mínimo de grupo"): las 2 partes de
  // grupo familiar ahora indican el alcance real de la eliminación según el tamaño ACTUAL del
  // vínculo (calculado dinámicamente por el backend, no cacheado, ver `ImpactoEliminacionRegistro`)
  // — con 2 o menos, el vínculo se disuelve por completo (también afecta a la otra solicitud); con 3
  // o más, solo se retira esta y las demás conservan su alerta/pertenencia sin cambios. "Potencial
  // duplicado" no tiene noción de tamaño (no aplica esta regla), queda igual que antes.
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

  // Mismo orden exacto que crearTableModel(perfilRegistroRecepcion=true) en V2
  // (JPanelBandejaExpedientesNueva): Dias, Nro. Expediente, N° expediente SGD, Canal,
  // Fecha Solicitud, Fecha Vencimiento, Proc. Registral, Tipo Acta, Nro Acta, Titular, Estado, Alertas.
  const columnas: GridColDef<FilaBandeja>[] = [
    {
      // Ancho ampliado de 44 a 84px (13/08/2026, pedido explícito del usuario) para que convivan
      // el ícono de expandir asociados (ya existía) y el nuevo ícono de eliminar, en la MISMA
      // columna, uno junto al otro — posición horizontal fija en todas las filas, no una columna
      // nueva. Filas asociadas (esFilaAsociada) NO muestran el ícono de eliminar (mismo criterio
      // que la selección por checkbox: no son expedientes independientes).
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
        const puedeExpandir = row.relacionesConfirmadasComoPrincipal > 0
        const puedeEliminar = row.etapaCodigo === 'REGISTRO' && row.estadoCodigo === 'REGISTRADO' && !row.responsableActual
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
    {
      field: 'numeroExpediente',
      headerName: 'Nro. Expediente',
      width: 190,
      renderCell: (params) => (
        <Box sx={{ pl: params.row.esFilaAsociada ? 1 : 0 }}>{params.value}</Box>
      ),
    },
    { field: 'numeroExpedienteSgd', headerName: 'N° expediente SGD', width: 160 },
    { field: 'canal', headerName: 'Canal', width: 150 },
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
    { field: 'numeroActa', headerName: 'Nro Acta', width: 120 },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    // 19/08/2026, pedido explícito del usuario: columna nueva, mismo campo que ya usa "Etapa" en el
    // Panel de datos (`PanelDatosExpediente.tsx`, `bandeja.etapaCodigo`) — antes solo se veía ahí, no
    // en la grilla. Ubicada justo antes de "Estado" para replicar el mismo orden del panel lateral.
    { field: 'etapaCodigo', headerName: 'Etapa', width: 130 },
    { field: 'estadoCodigo', headerName: 'Estado', width: 130 },
    {
      field: 'alertas',
      headerName: 'Alertas',
      width: 170,
      renderCell: (params) => <StatusBadge label={params.value as string} tone={alertaBadgeTone(params.value as string)} />,
    },
  ]

  // Envuelve el `sortComparator` de cada columna con `agruparHijosBajoPadre` (ver Javadoc arriba) —
  // aplicado una sola vez acá, no columna por columna, así que cualquier columna nueva que se agregue
  // en el futuro hereda la agrupación automáticamente sin tener que acordarse de repetirla.
  const columnasConAgrupacion = useMemo<GridColDef<FilaBandeja>[]>(
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
      </TextField>
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Bandeja Registro"
      subtitulo="Expedientes en etapa de Registro / Recepción."
      tabs={REGISTRO_TABS}
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

      {/* KPIs (filtro en memoria sobre la lista ya cargada, igual que V2) — ya no comparten Paper
          con el buscador: el buscador quedó sticky en la Fila 3 del header, los KPIs se desplazan
          con el resto del cuerpo (13/08/2026, pedido explícito del usuario). Distribución en grid de
          columnas iguales (13/08/2026, mismo día, pedido explícito del usuario: antes era un
          `Stack flexWrap` que las agrupaba a la izquierda dejando espacio vacío a la derecha) —
          `repeat(auto-fit, minmax(200px,1fr))` reparte el ancho completo disponible en partes
          iguales entre las 4 tarjetas, y en pantallas angostas las reacomoda en más filas en vez de
          desbordar (cada celda nunca baja de 200px). */}
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 2, mb: 2 }}>
        {/* "Total de expedientes" (reemplaza el indicador suelto agregado en el incremento
            anterior): primer KPI de la fila, mismo componente/estilo que el resto — no clicable (no
            filtra nada, es el universo completo bajo los filtros activos). Refleja
            `expedientesRaiz.length` (19/08/2026, pedido explícito del usuario: decisión consultada
            explícitamente — este KPI cuenta SOLO filas raíz/principales, excluyendo los duplicados
            ya confirmados como hijos de otro expediente, que ahora dejaron de listarse como fila
            independiente; ver `expedientesRaiz` más arriba). Coincide con el total que muestra el
            paginador del DataGrid mientras no haya un KPI de alerta clicado como filtro adicional. */}
        <MetricCard label="Total de expedientes" value={kpis.total} caption="Según filtros aplicados" color={sdrercColors.primary} />
        <Box onClick={() => alternarFiltro('POTENCIAL_DUPLICADO')} sx={{ cursor: 'pointer' }}>
          <MetricCard
            label="Potencial duplicado"
            value={kpis.potencialDuplicado}
            caption="Acta + titular"
            color={sdrercColors.warning}
          />
        </Box>
        <Box onClick={() => alternarFiltro('POSIBLE_GRUPO_FAMILIAR')} sx={{ cursor: 'pointer' }}>
          <MetricCard
            label="Posible Grupo Familiar"
            value={kpis.posibleGrupoFamiliar}
            caption="Apellidos coincidentes"
            color={sdrercColors.info}
          />
        </Box>
        <Box onClick={() => alternarFiltro('GRUPO_FAMILIAR_CONFIRMADO')} sx={{ cursor: 'pointer' }}>
          <MetricCard
            label="Grupo Familiar Confirmado"
            value={kpis.grupoFamiliarConfirmado}
            caption="Registrado"
            color={sdrercColors.primary}
          />
        </Box>
      </Box>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2 }}>
        <DataGrid
          rows={filasConHijos}
          columns={columnasConAgrupacion}
          getRowId={(row) => row.idExpediente}
          loading={loading}
          density="compact"
          autoHeight
          // 19/08/2026, pedido explícito del usuario (3er intento — ver Javadoc completo del
          // diagnóstico en v3/CLAUDE.md): confirmado leyendo el código fuente real de
          // `node_modules/@mui/x-data-grid`/`@mui/x-virtualizer` que (a) el "column pinning" real
          // (`initialState.pinnedColumns`) NO tiene ningún hook que lo lea en la edición Community
          // instalada — el estado nunca se puebla, por eso "ahora no tiene nada fijo"; (b)
          // `disableColumnVirtualization` (el prop que se probó en el intento anterior) es un
          // `PropTypes` obsoleto que ya NO está conectado al motor de virtualización real (que se
          // movió a un paquete aparte, `@mui/x-virtualizer`) — nunca tuvo efecto, por eso las
          // columnas seguían "podándose" del DOM a mitad de scroll pese a haberlo puesto. El único
          // prop real y vigente para esto es `disableVirtualization` (desactiva virtualización de
          // filas Y columnas por completo) — con el volumen de esta grilla (paginada, máx. 100
          // filas × ~13 columnas visibles) el costo de no virtualizar es despreciable. Combinado con
          // `position: sticky` (bloque `COLUMNAS_FIJAS_INICIO_SX`, ver constante arriba) sobre las
          // celdas de "__check__"/"expandir" — con TODAS las celdas siempre montadas en el DOM, el
          // sticky ya no puede fallar por elementos que desaparecen mid-scroll.
          disableVirtualization
          disableRowSelectionOnClick
          checkboxSelection
          isRowSelectable={(params) => !(params.row as FilaBandeja).esFilaAsociada}
          rowSelectionModel={rowSelectionModel}
          onRowSelectionModelChange={setRowSelectionModel}
          showToolbar
          slots={{ toolbar: ToolbarBandejaRegistro }}
          slotProps={{
            toolbar: {
              seleccionados: idsSeleccionados,
              onDeseleccionar: () => setRowSelectionModel(SIN_SELECCION),
              onAsociarGrupoFamiliar: asociarSeleccionadosGrupoFamiliar,
              procesandoGrupoFamiliar: procesandoGrupoFamiliarMasivo,
            },
          }}
          columnVisibilityModel={columnVisibilityModel}
          onColumnVisibilityModelChange={setColumnVisibilityModel}
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
            const row = params.row as FilaBandeja
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

      {/* Confirmación de eliminar (13/08/2026, pedido explícito del usuario: "misma acción/lógica
          que tenía el botón 'Eliminar' en la v2 del sistema" — modal de confirmación antes de
          ejecutar, luego refresca la bandeja desde el backend). */}
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
