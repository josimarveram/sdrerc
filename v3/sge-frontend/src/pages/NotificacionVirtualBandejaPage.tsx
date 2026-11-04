import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  Alert,
  Badge,
  Box,
  Button,
  Chip,
  CircularProgress,
  IconButton,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import {
  DataGrid,
  GridToolbarContainer,
  useGridApiContext,
  type GridColDef,
  type GridRowSelectionModel,
} from '@mui/x-data-grid'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import PersonPinCircleOutlinedIcon from '@mui/icons-material/PersonPinCircleOutlined'
import PersonPinCircleIcon from '@mui/icons-material/PersonPinCircle'
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined'
import CloseOutlinedIcon from '@mui/icons-material/CloseOutlined'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import {
  agregarIntentoNotificacion,
  actualizarIntentoNotificacion,
  confirmarRecepcionIntentoNotificacion,
  eliminarIntentoNotificacion,
  listarBandejaNotificacion,
  listarIntentosNotificacion,
  type NotificacionAsignacionDocumento,
  type NotificacionIntento,
} from '../api/notificacionNotificacionApi'
import { obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { ApiError } from '../api/http'
import { useAppDialog } from '../dialogs/AppDialogProvider'
import { sdrercColors } from '../theme'
import {
  dataGridBaseSx,
  DATA_GRID_ROW_ICON_DELETE_CLASS,
  DATA_GRID_ROW_ICON_SAVE_CLASS,
  DATA_GRID_ROW_ICON_NOTIFICACION_CLASS,
} from '../dataGridTheme'
import { DataGridStandardToolbarControls } from '../components/DataGridStandardToolbar'
import { primerDiaMesAnteriorISO, hoyISO } from '../utils/fechas'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
import { TrazabilidadContent } from '../components/TrazabilidadContent'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { NOTIFICACION_TABS } from '../layout/moduleTabs'

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

/** Colapsa el código real de catálogo (VIRTUAL/PRESENCIAL_1/PRESENCIAL_2) al valor del combo (VIRTUAL/PRESENCIAL). Port de `codigoModalidadColumna` (V2). */
function modalidadColumna(tipoNotificacionCodigo: string): 'VIRTUAL' | 'PRESENCIAL' {
  return tipoNotificacionCodigo?.toUpperCase() === 'VIRTUAL' ? 'VIRTUAL' : 'PRESENCIAL'
}

/** Expande la selección del combo al código real de catálogo. Port de `codigoModalidadParaGuardar` (V2). */
function modalidadParaGuardar(seleccion: string, numeroIntento: number): string {
  if (seleccion === 'VIRTUAL') return 'VIRTUAL'
  return numeroIntento >= 3 ? 'PRESENCIAL_2' : 'PRESENCIAL_1'
}

/** Port de `codigoEstadoParaColumnaEstado` (V2): deriva el valor del combo "Estado" desde el estado real persistido. */
function estadoColumna(estadoNotificacionCodigo: string): 'PENDIENTE' | 'ENVIADA' | 'EXITOSA' {
  const c = estadoNotificacionCodigo?.toUpperCase()
  if (c === 'EXITOSA') return 'EXITOSA'
  if (c === 'PENDIENTE') return 'PENDIENTE'
  return 'ENVIADA'
}

/** Port de `codigoEstadoNotifParaColumna` (V2): deriva el valor del combo "Estado Notificación". */
function estadoNotifColumna(estadoNotificacionCodigo: string): '' | 'FALLIDA' | 'EXITOSA' {
  const c = estadoNotificacionCodigo?.toUpperCase()
  if (c === 'EXITOSA') return 'EXITOSA'
  if (c === 'FALLIDA') return 'FALLIDA'
  return ''
}

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#2370A8'
const COLOR_TRAZABILIDAD = '#8E44AD'

type FilaPadre = { id: string; tipo: 'padre'; doc: NotificacionAsignacionDocumento }
// 30/08/2026, pedido explícito del usuario: "replica el diseño de nested grid de la Bandeja de
// supervisión". Antes cada intento/borrador era su PROPIA fila del DataGrid (fusionada por colSpan,
// mostrando un formulario horizontal plano) — se reemplaza por una única fila 'intentos' por
// documento expandido, que renderiza una tabla real (Table/TableHead/TableBody) con TODOS los
// intentos + borradores del documento adentro, dentro de la misma "tarjeta con acento lateral" ya
// usada en `NotificacionAsignacionBandejaPage.tsx`. `FilaHijo`/`FilaBorrador` se conservan como
// formas de objeto (no como tipos de fila del grid) — se siguen construyendo al vuelo dentro del
// render para pasarlos, sin cambios, a `guardarIntento`/`eliminarIntento`/`guardarBorrador`/
// `cancelarBorrador`.
type FilaHijo = { id: string; tipo: 'hijo'; idDocumentoAnalizado: number; intento: NotificacionIntento }
type FilaBorrador = {
  id: string
  tipo: 'borrador'
  idDocumentoAnalizado: number
  idExpediente: number
  tempId: number
  numeroIntento: number
}
type FilaIntentos = { id: string; tipo: 'intentos'; idDocumentoAnalizado: number; idExpediente: number }
type FilaNotif = FilaPadre | FilaIntentos

interface EdicionIntento {
  modalidad: 'VIRTUAL' | 'PRESENCIAL'
  estado: 'PENDIENTE' | 'ENVIADA' | 'EXITOSA'
  estadoNotif: '' | 'FALLIDA' | 'EXITOSA'
  codigo: string
  fechaEnvio: string
  fechaRecepcion: string
}

/** 23/08/2026, pedido explícito del usuario: el borrador ahora expone los mismos campos que un intento ya persistido (incluidos Fecha Acuse/Estado Notificación) desde el primer momento, no solo tras el primer Guardar. */
interface EdicionBorrador {
  modalidad: 'VIRTUAL' | 'PRESENCIAL'
  estado: 'PENDIENTE' | 'ENVIADA' | 'EXITOSA'
  estadoNotif: '' | 'FALLIDA' | 'EXITOSA'
  codigo: string
  fechaEnvio: string
  fechaRecepcion: string
}

function edicionInicialIntento(intento: NotificacionIntento): EdicionIntento {
  return {
    modalidad: modalidadColumna(intento.tipoNotificacionCodigo),
    estado: estadoColumna(intento.estadoNotificacionCodigo),
    estadoNotif: estadoNotifColumna(intento.estadoNotificacionCodigo),
    codigo: intento.codigoNotificacion || '',
    fechaEnvio: intento.fechaEnvio ? intento.fechaEnvio.slice(0, 10) : '',
    fechaRecepcion: intento.fechaRecepcion ? intento.fechaRecepcion.slice(0, 10) : '',
  }
}

function edicionInicialBorrador(modalidad: 'VIRTUAL' | 'PRESENCIAL'): EdicionBorrador {
  return { modalidad, estado: 'PENDIENTE', estadoNotif: '', codigo: '', fechaEnvio: '', fechaRecepcion: '' }
}

/** Port de la derivación de `estadoFinalCodigo` en `guardarFilaIntento` (V2): combina "Estado"/"Estado Notificación". */
function derivarEstadoFinal(
  estado: 'PENDIENTE' | 'ENVIADA' | 'EXITOSA',
  estadoNotif: '' | 'FALLIDA' | 'EXITOSA',
): 'EXITOSA' | 'FALLIDA' | 'ENVIADA' | 'PENDIENTE' {
  if (estado === 'EXITOSA' || estadoNotif === 'EXITOSA') return 'EXITOSA'
  if (estadoNotif === 'FALLIDA') return 'FALLIDA'
  return estado === 'ENVIADA' ? 'ENVIADA' : 'PENDIENTE'
}

/**
 * 23/08/2026, pedido explícito del usuario: "Agregar intento" no debía permitir insertar 2
 * borradores a la vez para el mismo documento, ni habilitar el intento 2 si el intento 1 todavía no
 * fue resuelto (sus campos "Estado" y "Estado Notificación" siguen en su valor por defecto recién
 * insertado). Reglas, en orden:
 * 1. Si ya hay un borrador sin guardar para el documento, no se agrega otro.
 * 2. Si el intento más reciente ya quedó Ubicado, no tiene sentido un intento nuevo (regla ya
 *    existente, V2).
 * 3. Si el intento más reciente sigue en su estado por defecto (`Estado=Pendiente` y
 *    `Estado Notificación` en blanco — es decir, nunca se actualizó tras registrarlo), se bloquea:
 *    el usuario debe registrar el resultado del intento actual antes de habilitar el siguiente.
 * 4. Máximo 2 intentos directos (regla ya existente, V2).
 */
function puedeAgregarProximoIntento(
  intentosActuales: NotificacionIntento[],
  borradoresActuales: FilaBorrador[],
): { ok: true } | { ok: false; motivo: 'borrador-pendiente' | 'ya-ubicado' | 'sin-resolver' | 'maximo' } {
  if (borradoresActuales.length > 0) return { ok: false, motivo: 'borrador-pendiente' }
  if (intentosActuales.some((i) => i.ubicado)) return { ok: false, motivo: 'ya-ubicado' }
  const ultimo = intentosActuales[intentosActuales.length - 1]
  if (ultimo) {
    const estado = estadoColumna(ultimo.estadoNotificacionCodigo)
    const estadoNotif = estadoNotifColumna(ultimo.estadoNotificacionCodigo)
    if (estado === 'PENDIENTE' && estadoNotif === '') return { ok: false, motivo: 'sin-resolver' }
  }
  if (intentosActuales.length + 1 > 2) return { ok: false, motivo: 'maximo' }
  return { ok: true }
}

declare module '@mui/x-data-grid' {
  interface ToolbarPropsOverrides {
    seleccionados: number[]
    onAgregarIntento: () => void
  }
}

function ToolbarBandejaNotificacion({ seleccionados, onAgregarIntento }: { seleccionados: number[]; onAgregarIntento: () => void }) {
  const apiRef = useGridApiContext()
  return (
    <GridToolbarContainer>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
        {seleccionados.length > 0 && (
          <Chip label={`${seleccionados.length} seleccionado${seleccionados.length > 1 ? 's' : ''}`} size="small" color="primary" variant="outlined" />
        )}
        <Button size="small" variant="contained" startIcon={<AddCircleOutlineIcon />} onClick={onAgregarIntento}>
          Agregar intento
        </Button>
        {seleccionados.length > 0 && (
          <Button size="small" onClick={() => apiRef.current.exportDataAsCsv({ getRowsToExport: () => seleccionados })}>
            Exportar seleccionados
          </Button>
        )}
      </Stack>
      <Box sx={{ flex: 1 }} />
      <DataGridStandardToolbarControls />
    </GridToolbarContainer>
  )
}

function adaptarParaPanelDatos(doc: NotificacionAsignacionDocumento): ExpedienteBandeja {
  return {
    idExpediente: doc.idExpediente,
    numeroExpediente: doc.numeroExpediente,
    numeroTramiteDocumentario: '',
    numeroExpedienteSgd: doc.numeroExpedienteSgd,
    etapaCodigo: 'NOTIFICACION',
    estadoCodigo: doc.estadoExpedienteCodigo,
    abogadoInicial: '',
    responsableActual: doc.usuarioNotificacionActual,
    equipoActual: '',
    fechaRecepcion: null,
    fechaRegistro: null,
    fechaUltimoMovimiento: null,
    fechaVencimiento: doc.fechaVencimiento,
    requierePublicacion: false,
    expedienteDigitalCompleto: false,
    canal: '',
    procedimiento: '',
    tipoActa: '',
    numeroActa: '',
    grupoFamiliar: 'No',
    alertas: '',
    titular: doc.titular,
    relacionesConfirmadasComoPrincipal: doc.totalRelacionados,
    relacionadoComoHijo: false,
    grupoFamiliarConfirmado: false,
    diasRestantes: doc.diasRestantes,
  }
}

type FiltroKpi = 'POR_NOTIFICAR' | 'PENDIENTE' | 'ATENDIDO' | 'POR_PUBLICAR' | 'POR_VENCER' | 'VENCIDOS' | null

/**
 * Equivalente web de la 3ra pestaña de Notificación, "Bandeja Notificación" (22/08/2026, pedido
 * explícito del usuario vía prompt estructurado, investigación previa obligatoria contra
 * `JPanelNotificacionV2.crearBandejaNotifV2`/`reconstruirFilasNotifBandeja`/`crearBorradoresIntento`/
 * `guardarFilaIntento` real).
 *
 * **Hallazgo clave de la investigación (ver Javadoc de `NotificacionNotificacionService`, backend):
 * en V2 real, el buscador/KPIs/panel de doble clic de esta pestaña dependen de una lista legacy a
 * nivel de expediente completamente desconectada de la grilla árbol real que sí se ve en pantalla —
 * un bug de V2, no una funcionalidad a replicar.** V3 construye los 3 directamente sobre
 * `NotificacionAsignacionDocumentoDTO` (el mismo DTO real de la grilla), igual que Asignación/Validación.
 *
 * Grilla árbol única (documento padre + intentos hijo, filas sintéticas intercaladas en el mismo
 * `DataGrid` — mismo patrón ya usado por `RegistroBandejaPage.tsx` para expedientes asociados,
 * extendido acá con celdas editables): checkbox de selección múltiple SOLO en filas padre
 * (`isRowSelectable`), ícono de expandir/colapsar que solo aparece si el documento ya tiene intentos
 * (guardados o borradores en memoria), y "+ Agregar intento" en el toolbar que inserta una fila hija
 * "borrador" editable por cada documento marcado (o el último enfocado con doble clic si no hay
 * ninguno marcado) — máximo 2 intentos directos por documento, bloqueado si el intento 1 ya quedó
 * Ubicado (mismas reglas que V2, `crearBorradoresIntento`).
 *
 * Columnas: en vez de reutilizar por posición las mismas columnas del padre con distinto significado
 * en la fila hija (truco de `DefaultTableModel` de Swing, no aplicable de forma clara a un
 * `DataGrid`), se define un conjunto ÚNICO de columnas — las que no aplican a un tipo de fila
 * muestran "-"/vacío — con las mismas 7 columnas de intento que V2 (Nro. Intento, Modalidad, Fecha
 * Envío, Estado, Código/Usuario Notif., Fecha Acuse, Estado Notificación) más las columnas del
 * documento padre. Bloque de iconos de acción (Guardar/Cancelar/Eliminar) al inicio de las columnas,
 * mismo criterio que Análisis.
 *
 * Al hacer doble clic en una fila padre, se abre el panel con las subpestañas "Datos" y
 * "Trazabilidad" (esta última agregada el 01/09/2026, pedido explícito del usuario: "replicar el
 * panel de Trazabilidad... en todas las bandejas desde Recepción hasta Publicación") — sin
 * subpestaña de acción/asignación (pedido explícito original: "sin subpestaña de
 * acción/asignación en esta bandeja"), se reutiliza `WorkspaceTabsDrawer`, mismo componente
 * compartido que las demás bandejas de este módulo.
 */
export function NotificacionNotificacionBandejaPage() {
  const { confirm: confirmDialog, alert: alertDialog } = useAppDialog()
  const [documentos, setDocumentos] = useState<NotificacionAsignacionDocumento[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [expandidos, setExpandidos] = useState<Set<number>>(new Set())
  const [intentosPorDocumento, setIntentosPorDocumento] = useState<Record<number, NotificacionIntento[]>>({})
  const [cargandoIntentos, setCargandoIntentos] = useState<Set<number>>(new Set())
  const [borradoresPorDocumento, setBorradoresPorDocumento] = useState<Record<number, FilaBorrador[]>>({})
  const [edicionesIntento, setEdicionesIntento] = useState<Record<number, EdicionIntento>>({})
  const [edicionesBorrador, setEdicionesBorrador] = useState<Record<number, EdicionBorrador>>({})
  const [procesandoFila, setProcesandoFila] = useState<string | null>(null)
  const tempIdRef = useRef(-1)

  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)
  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaMesAnteriorISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [estadoFinalFiltro, setEstadoFinalFiltro] = useState('')

  const [rowSelectionModel, setRowSelectionModel] = useState<GridRowSelectionModel>({ type: 'include', ids: new Set() })
  const [idPadreEnfocado, setIdPadreEnfocado] = useState<number | null>(null)

  type WorkspaceAbierto = { id: string; doc: NotificacionAsignacionDocumento; color: string; activeSubTabId: string }
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [datosPorExpediente, setDatosPorExpediente] = useState<
    Record<number, { detalle: ExpedienteEdicionManual | null; cargando: boolean; error: string | null }>
  >({})

  const buscar = useCallback(() => {
    setLoading(true)
    setError(null)
    listarBandejaNotificacion()
      .then(setDocumentos)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    buscar()
  }, [buscar])

  const kpis = useMemo(() => {
    let porNotificar = 0
    let pendiente = 0
    let atendido = 0
    let porPublicar = 0
    let porVencer = 0
    let vencidos = 0
    for (const doc of documentos) {
      switch (doc.estadoFinalNotificacionCodigo) {
        case 'PENDIENTE':
          pendiente++
          break
        case 'ATENDIDO':
          atendido++
          break
        case 'POR_PUBLICAR':
          porPublicar++
          break
        default:
          porNotificar++
      }
      if (doc.diasRestantes !== null) {
        if (doc.diasRestantes < 0) vencidos++
        else if (doc.diasRestantes <= 5) porVencer++
      }
    }
    return { porNotificar, pendiente, atendido, porPublicar, porVencer, vencidos }
  }, [documentos])

  function coincideFiltroKpi(doc: NotificacionAsignacionDocumento): boolean {
    switch (filtroKpi) {
      case 'POR_NOTIFICAR':
        return doc.estadoFinalNotificacionCodigo === 'POR_NOTIFICAR' || !doc.estadoFinalNotificacionCodigo
      case 'PENDIENTE':
        return doc.estadoFinalNotificacionCodigo === 'PENDIENTE'
      case 'ATENDIDO':
        return doc.estadoFinalNotificacionCodigo === 'ATENDIDO'
      case 'POR_PUBLICAR':
        return doc.estadoFinalNotificacionCodigo === 'POR_PUBLICAR'
      case 'POR_VENCER':
        return doc.diasRestantes !== null && doc.diasRestantes >= 0 && doc.diasRestantes <= 5
      case 'VENCIDOS':
        return doc.diasRestantes !== null && doc.diasRestantes < 0
      default:
        return true
    }
  }

  function coincideBusqueda(doc: NotificacionAsignacionDocumento): boolean {
    const textoNorm = texto.trim().toUpperCase()
    if (textoNorm) {
      const coincide = [doc.numeroExpediente, doc.numeroExpedienteSgd, doc.titular, doc.tipoDocumento, doc.numeroDocumento]
        .some((v) => (v || '').toUpperCase().includes(textoNorm))
      if (!coincide) return false
    }
    if (estadoFinalFiltro && doc.estadoFinalNotificacionCodigo !== estadoFinalFiltro) return false
    if (doc.fechaDocumento) {
      const fecha = doc.fechaDocumento.slice(0, 10)
      if (desde && fecha < desde) return false
      if (hasta && fecha > hasta) return false
    }
    return true
  }

  const documentosFiltrados = useMemo(
    () => documentos.filter((d) => coincideFiltroKpi(d) && coincideBusqueda(d)),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [documentos, filtroKpi, texto, estadoFinalFiltro, desde, hasta],
  )

  function alternarFiltroKpi(valor: FiltroKpi) {
    setFiltroKpi((prev) => (prev === valor ? null : valor))
  }

  function handleLimpiar() {
    setTexto('')
    setDesde(primerDiaMesAnteriorISO())
    setHasta(hoyISO())
    setEstadoFinalFiltro('')
  }

  /** Fuerza una relectura contra el backend, sin usar la caché — ver `asegurarIntentosCargados`. */
  async function cargarIntentosFrescos(idDocumento: number): Promise<NotificacionIntento[]> {
    setCargandoIntentos((prev) => new Set(prev).add(idDocumento))
    try {
      const intentos = await listarIntentosNotificacion(idDocumento)
      setIntentosPorDocumento((prev) => ({ ...prev, [idDocumento]: intentos }))
      setEdicionesIntento((prev) => {
        const next = { ...prev }
        for (const intento of intentos) {
          next[intento.idExpedienteNotificacion] = edicionInicialIntento(intento)
        }
        return next
      })
      return intentos
    } finally {
      setCargandoIntentos((prev) => {
        const next = new Set(prev)
        next.delete(idDocumento)
        return next
      })
    }
  }

  async function asegurarIntentosCargados(idDocumento: number): Promise<NotificacionIntento[]> {
    if (intentosPorDocumento[idDocumento]) return intentosPorDocumento[idDocumento]
    return cargarIntentosFrescos(idDocumento)
  }

  async function alternarExpansion(doc: NotificacionAsignacionDocumento) {
    if (expandidos.has(doc.idDocumentoAnalizado)) {
      setExpandidos((prev) => {
        const next = new Set(prev)
        next.delete(doc.idDocumentoAnalizado)
        return next
      })
      return
    }
    await asegurarIntentosCargados(doc.idDocumentoAnalizado)
    setExpandidos((prev) => new Set(prev).add(doc.idDocumentoAnalizado))
  }

  const idsSeleccionados = useMemo(() => {
    return documentosFiltrados
      .filter((d) =>
        rowSelectionModel.type === 'include'
          ? rowSelectionModel.ids.has(d.idDocumentoAnalizado)
          : !rowSelectionModel.ids.has(d.idDocumentoAnalizado),
      )
      .map((d) => d.idDocumentoAnalizado)
  }, [documentosFiltrados, rowSelectionModel])

  /**
   * "+ Agregar intento" — port de `agregarIntentosInline`/`crearBorradoresIntento` (V2), endurecido
   * con las reglas nuevas de `puedeAgregarProximoIntento` (23/08/2026, pedido explícito del usuario):
   * nunca más de 1 borrador sin guardar por documento, y el intento anterior debe quedar resuelto
   * (Estado + Estado Notificación ya actualizados) antes de habilitar el siguiente.
   */
  async function handleAgregarIntento() {
    const objetivos = new Set<number>(idsSeleccionados)
    if (objetivos.size === 0 && idPadreEnfocado !== null) {
      objetivos.add(idPadreEnfocado)
    }
    if (objetivos.size === 0) {
      await alertDialog('Seleccione uno o varios documentos de la bandeja de notificación.', { tone: 'warning' })
      return
    }
    // 23/08/2026, bug real corregido (pedido explícito del usuario: "el mensaje aparece 2 veces"):
    // la versión anterior leía/decidía dentro de los propios callbacks de `setIntentosPorDocumento`/
    // `setBorradoresPorDocumento` y además disparaba `window.alert`/`setEdicionesBorrador` DESDE
    // ADENTRO de esos callbacks — React 18 StrictMode invoca dos veces (a propósito, para detectar
    // impurezas) cualquier función pasada a `setState` en su forma de actualizador, así que ese
    // efecto secundario (el alert) se ejecutaba dos veces por cada click, aunque el estado final
    // aplicado fuera correcto ("y luego me agrega"). Fix: primero se calcula todo de forma pura
    // (sin tocar estado ni mostrar diálogos) leyendo directamente los resultados de
    // `cargarIntentosFrescos` (no del closure de `intentosPorDocumento`, para no depender de que
    // ya se haya re-renderizado) y el `borradoresPorDocumento` actual del closure (no cambia entre el
    // `await` de arriba y este punto); recién después se aplican los `setState` (con actualizadores
    // puros, sin alerts adentro) y se muestra el `alert` una sola vez, fuera de cualquier actualizador.
    // 30/08/2026, bug real corregido (pedido explícito del usuario: "se queda pegado el mensaje...
    // ya fue ubicado"): usar `asegurarIntentosCargados` (que confía en la caché si ya está poblada)
    // podía evaluar la regla contra un `intentosPorDocumento` desactualizado justo después de guardar
    // o eliminar un intento del mismo documento — se fuerza siempre una relectura fresca del backend
    // (`cargarIntentosFrescos`) antes de decidir si corresponde agregar el siguiente intento.
    const cargas = await Promise.all([...objetivos].map(async (id) => [id, await cargarIntentosFrescos(id)] as const))
    const intentosPorId = new Map(cargas)

    const borradoresNuevosPorDoc = new Map<number, FilaBorrador>()
    const edicionesNuevas = new Map<number, EdicionBorrador>()
    const idsParaExpandir: number[] = []
    const motivos = new Set<string>()

    for (const id of objetivos) {
      const doc = documentos.find((d) => d.idDocumentoAnalizado === id)
      if (!doc) continue
      const intentos = intentosPorId.get(id) ?? []
      const borradoresExistentes = borradoresPorDocumento[id] ?? []
      const resultado = puedeAgregarProximoIntento(intentos, borradoresExistentes)
      if (!resultado.ok) {
        motivos.add(resultado.motivo)
        continue
      }
      const siguiente = intentos.length + 1
      const tempId = tempIdRef.current--
      borradoresNuevosPorDoc.set(id, {
        id: `borrador-${tempId}`,
        tipo: 'borrador',
        idDocumentoAnalizado: id,
        idExpediente: doc.idExpediente,
        tempId,
        numeroIntento: siguiente,
      })
      edicionesNuevas.set(tempId, edicionInicialBorrador(siguiente === 1 ? 'VIRTUAL' : 'PRESENCIAL'))
      idsParaExpandir.push(id)
    }

    if (borradoresNuevosPorDoc.size === 0) {
      const mensajes: Record<string, string> = {
        'borrador-pendiente': 'Ya hay un intento sin guardar. Guarde o cancele ese intento antes de agregar otro.',
        'ya-ubicado': 'El documento ya fue ubicado (Atendido); no corresponde un nuevo intento.',
        'sin-resolver': 'Registre el resultado del intento actual (Estado y Estado Notificación) antes de agregar el siguiente.',
        maximo: 'Ya se alcanzó el máximo de 2 intentos de notificación para este documento.',
      }
      await alertDialog([...motivos].map((m) => mensajes[m]).join('\n') || 'No se pudo agregar el intento.', { tone: 'warning' })
      return
    }

    setBorradoresPorDocumento((prev) => {
      const next = { ...prev }
      for (const [id, borrador] of borradoresNuevosPorDoc) {
        next[id] = [...(next[id] ?? []), borrador]
      }
      return next
    })
    setEdicionesBorrador((prev) => {
      const next = { ...prev }
      for (const [tempId, edicion] of edicionesNuevas) {
        next[tempId] = edicion
      }
      return next
    })
    setExpandidos((prev) => {
      const next = new Set(prev)
      for (const id of idsParaExpandir) next.add(id)
      return next
    })
  }

  function cancelarBorrador(fila: FilaBorrador) {
    setBorradoresPorDocumento((prev) => {
      const lista = (prev[fila.idDocumentoAnalizado] ?? []).filter((b) => b.tempId !== fila.tempId)
      return { ...prev, [fila.idDocumentoAnalizado]: lista }
    })
    setEdicionesBorrador((prev) => {
      const next = { ...prev }
      delete next[fila.tempId]
      return next
    })
  }

  /**
   * Guardar un borrador — 23/08/2026, pedido explícito del usuario: como el borrador ahora también
   * expone Estado/Estado Notificación/Fecha Acuse desde el primer momento (ver `EdicionBorrador`),
   * el guardado ya no es un simple INSERT: si el usuario dejó esos campos en su valor por defecto
   * (Pendiente/en blanco), se comporta igual que antes (`agregarIntentoNotificacion`, nace
   * `PENDIENTE`); si ya los resolvió, se encadena un segundo paso (`confirmarRecepcion`/
   * `actualizarIntento`, mismo criterio que `guardarIntento`) sobre el intento recién creado, para
   * no descartar en silencio datos que el usuario ya cargó antes de guardar por primera vez.
   */
  async function guardarBorrador(fila: FilaBorrador) {
    const edicion = edicionesBorrador[fila.tempId]
    if (!edicion) return
    setProcesandoFila(fila.id)
    setError(null)
    try {
      await agregarIntentoNotificacion({
        idExpediente: fila.idExpediente,
        idDocumentoAnalizado: fila.idDocumentoAnalizado,
        tipoNotificacionCodigo: modalidadParaGuardar(edicion.modalidad, fila.numeroIntento),
        codigoNotificacion: edicion.codigo,
        fechaEnvio: edicion.fechaEnvio || null,
      })
      let intentos = await listarIntentosNotificacion(fila.idDocumentoAnalizado)
      const estadoFinal = derivarEstadoFinal(edicion.estado, edicion.estadoNotif)
      if (estadoFinal !== 'PENDIENTE') {
        const creado = intentos.find((i) => i.numeroIntento === fila.numeroIntento)
        if (creado) {
          if (estadoFinal === 'EXITOSA') {
            await confirmarRecepcionIntentoNotificacion({
              idExpediente: fila.idExpediente,
              idExpedienteNotificacion: creado.idExpedienteNotificacion,
              idDocumentoAnalizado: fila.idDocumentoAnalizado,
              codigoORecibidoPor: edicion.codigo,
              fechaEnvio: edicion.fechaEnvio || null,
              fechaRecepcion: edicion.fechaRecepcion || null,
            })
          } else {
            await actualizarIntentoNotificacion({
              idExpedienteNotificacion: creado.idExpedienteNotificacion,
              tipoNotificacionCodigo: modalidadParaGuardar(edicion.modalidad, fila.numeroIntento),
              estadoNotificacionCodigo: estadoFinal,
              codigoNotificacion: edicion.codigo,
              fechaEnvio: edicion.fechaEnvio || null,
            })
          }
          intentos = await listarIntentosNotificacion(fila.idDocumentoAnalizado)
        }
      }
      cancelarBorrador(fila)
      setIntentosPorDocumento((prev) => ({ ...prev, [fila.idDocumentoAnalizado]: intentos }))
      setEdicionesIntento((prev) => {
        const next = { ...prev }
        for (const intento of intentos) {
          next[intento.idExpedienteNotificacion] = edicionInicialIntento(intento)
        }
        return next
      })
      buscar()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo registrar el intento de notificación.')
    } finally {
      setProcesandoFila(null)
    }
  }

  async function guardarIntento(fila: FilaHijo) {
    const edicion = edicionesIntento[fila.intento.idExpedienteNotificacion]
    if (!edicion) return
    const estadoFinal = derivarEstadoFinal(edicion.estado, edicion.estadoNotif)
    setProcesandoFila(fila.id)
    setError(null)
    try {
      if (estadoFinal === 'EXITOSA') {
        await confirmarRecepcionIntentoNotificacion({
          idExpediente: fila.intento.idExpediente,
          idExpedienteNotificacion: fila.intento.idExpedienteNotificacion,
          idDocumentoAnalizado: fila.intento.idDocumentoAnalizado,
          codigoORecibidoPor: edicion.codigo,
          fechaEnvio: edicion.fechaEnvio || null,
          fechaRecepcion: edicion.fechaRecepcion || null,
        })
      } else {
        await actualizarIntentoNotificacion({
          idExpedienteNotificacion: fila.intento.idExpedienteNotificacion,
          tipoNotificacionCodigo: modalidadParaGuardar(edicion.modalidad, fila.intento.numeroIntento),
          estadoNotificacionCodigo: estadoFinal,
          codigoNotificacion: edicion.codigo,
          fechaEnvio: edicion.fechaEnvio || null,
        })
      }
      const intentos = await listarIntentosNotificacion(fila.idDocumentoAnalizado)
      setIntentosPorDocumento((prev) => ({ ...prev, [fila.idDocumentoAnalizado]: intentos }))
      setEdicionesIntento((prev) => {
        const next = { ...prev }
        for (const intento of intentos) {
          next[intento.idExpedienteNotificacion] = edicionInicialIntento(intento)
        }
        return next
      })
      buscar()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo actualizar el intento de notificación.')
    } finally {
      setProcesandoFila(null)
    }
  }

  async function eliminarIntento(fila: FilaHijo) {
    if (!(await confirmDialog(`¿Desea eliminar el intento ${fila.intento.numeroIntento} de notificación?`, { tone: 'danger', confirmLabel: 'Eliminar' }))) return
    setProcesandoFila(fila.id)
    setError(null)
    try {
      await eliminarIntentoNotificacion(fila.intento.idExpedienteNotificacion)
      const intentos = await listarIntentosNotificacion(fila.idDocumentoAnalizado)
      setIntentosPorDocumento((prev) => ({ ...prev, [fila.idDocumentoAnalizado]: intentos }))
      buscar()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo eliminar el intento de notificación.')
    } finally {
      setProcesandoFila(null)
    }
  }

  // 30/08/2026: una sola fila 'intentos' por documento expandido (en vez de una fila por cada
  // intento/borrador) — el contenido real (intentos + borradores) se lee directo de
  // `intentosPorDocumento`/`borradoresPorDocumento` dentro del propio `renderCell`, así que esta
  // fila sintética no necesita cargar esos datos en su identidad.
  const filasConHijos = useMemo<FilaNotif[]>(() => {
    const resultado: FilaNotif[] = []
    for (const doc of documentosFiltrados) {
      resultado.push({ id: `doc-${doc.idDocumentoAnalizado}`, tipo: 'padre', doc })
      if (expandidos.has(doc.idDocumentoAnalizado)) {
        resultado.push({
          id: `intentos-${doc.idDocumentoAnalizado}`,
          tipo: 'intentos',
          idDocumentoAnalizado: doc.idDocumentoAnalizado,
          idExpediente: doc.idExpediente,
        })
      }
    }
    return resultado
  }, [documentosFiltrados, expandidos])

  function abrirWorkspace(doc: NotificacionAsignacionDocumento) {
    const id = `ws-${doc.idExpediente}`
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) {
        return prev.map((w) => (w.id === id ? { ...w, doc } : w))
      }
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { id, doc, color, activeSubTabId: 'datos' }]
    })
    if (datosPorExpediente[doc.idExpediente]) return
    recargarDatosExpediente(doc.idExpediente)
  }

  function seleccionarSubTab(workspaceId: string, subTabId: string) {
    setWorkspacesAbiertos((prev) => prev.map((w) => (w.id === workspaceId ? { ...w, activeSubTabId: subTabId } : w)))
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

  const workspacesUI: Workspace[] = useMemo(
    () =>
      workspacesAbiertos.map((w) => {
        const estadoDatos = datosPorExpediente[w.doc.idExpediente]
        return {
          id: w.id,
          label: w.doc.titular || w.doc.numeroExpediente || `Expediente ${w.doc.idExpediente}`,
          color: w.color,
          activeSubTabId: w.activeSubTabId,
          subTabs: [
            {
              id: 'datos',
              label: 'Datos',
              color: COLOR_DATOS,
              content: (
                <PanelDatosExpediente
                  bandeja={adaptarParaPanelDatos(w.doc)}
                  detalle={estadoDatos?.detalle ?? null}
                  cargando={estadoDatos?.cargando ?? false}
                  error={estadoDatos?.error ?? null}
                  permiteEdicion={false}
                  onGuardado={() => recargarDatosExpediente(w.doc.idExpediente)}
                />
              ),
            },
            {
              id: 'trazabilidad',
              label: 'Trazabilidad',
              color: COLOR_TRAZABILIDAD,
              content: <TrazabilidadContent idExpediente={w.doc.idExpediente} />,
            },
          ],
        }
      }),
    [workspacesAbiertos, datosPorExpediente],
  )

  const columnas: GridColDef<FilaNotif>[] = [
    {
      // 30/08/2026: con la fila 'intentos' única (nested grid, ver columna 'numeroExpediente'), los
      // íconos de Guardar/Cancelar/Eliminar de cada intento/borrador viven DENTRO de la propia tabla
      // anidada, no en esta columna — acá solo queda el ícono de expandir/colapsar de la fila padre.
      field: 'expandir',
      headerName: '',
      width: 56,
      sortable: false,
      filterable: false,
      renderCell: (params) => {
        const row = params.row
        if (row.tipo !== 'padre') return null
        if (cargandoIntentos.has(row.doc.idDocumentoAnalizado)) {
          return <CircularProgress size={16} />
        }
        const expandido = expandidos.has(row.doc.idDocumentoAnalizado)
        // 30/08/2026, bug real corregido (pedido explícito del usuario: "al eliminar el 1er intento
        // desaparece el ícono para contraer"): antes el ícono solo se mostraba si `totalIntentos > 0`
        // — al eliminar el único intento de un documento ya expandido, `totalIntentos` bajaba a 0 tras
        // el refresco de la bandeja y el ícono desaparecía por completo, dejando la fila expandida
        // (con la nested grid vacía) sin ninguna forma de volver a colapsarla. Ahora también se
        // muestra mientras el documento ya está expandido, para poder colapsarlo siempre.
        const puedeExpandir = row.doc.totalIntentos > 0 || expandido
        if (!puedeExpandir) return null
        // 30/08/2026, pedido explícito del usuario (3er ajuste, elegido explícitamente entre 4
        // opciones vía AskUserQuestion): `PersonPinCircle` (pin de ubicación con silueta de persona)
        // — encaja mejor semánticamente que los 2 intentos anteriores (`ContactMail`/
        // `MarkunreadMailbox`): el objetivo real de cada intento es "ubicar" al ciudadano, virtual o
        // presencial — el propio sistema ya usa el término "Ubicado"/"No ubicado" para el resultado
        // (ver `estadoNotif`/`NotificacionIntento.ubicado`). Mismo criterio ya usado para el ícono de
        // carpeta de la Bandeja de supervisión: outline mientras está colapsado, versión "llena" al
        // expandir, con un `Badge` mostrando cuántos intentos tiene el documento.
        return (
          <Tooltip title={expandido ? 'Colapsar intentos de notificación' : 'Ver intentos de notificación'}>
            <IconButton
              size="small"
              className={DATA_GRID_ROW_ICON_NOTIFICACION_CLASS}
              onClick={() => alternarExpansion(row.doc)}
              aria-label={expandido ? 'Colapsar intentos' : 'Ver intentos de notificación'}
            >
              <Badge
                badgeContent={row.doc.totalIntentos}
                overlap="circular"
                max={9}
                sx={{
                  '& .MuiBadge-badge': {
                    backgroundColor: '#6A4C93',
                    color: '#fff',
                    fontSize: '0.55rem',
                    minWidth: 14,
                    height: 14,
                    padding: '0 2px',
                  },
                }}
              >
                {expandido ? <PersonPinCircleIcon fontSize="small" /> : <PersonPinCircleOutlinedIcon fontSize="small" />}
              </Badge>
            </IconButton>
          </Tooltip>
        )
      },
    },
    {
      field: 'numeroExpediente',
      headerName: 'N° expediente',
      width: 190,
      sortable: false,
      // 28/08/2026, bug real corregido (pedido explícito del usuario: "el icono de Filters no está
      // funcionando"): esta columna (y las 7 siguientes) leen `row.doc.x` en vez de un campo plano —
      // sin `valueGetter`, el filtro no tiene ningún valor sobre el que operar, por eso `filterable`
      // se había puesto en `false` desde el port inicial. `valueGetter` extrae el valor real solo
      // para la fila padre (la fila 'intentos' no tiene `numeroExpediente` propio, vuelve '').
      valueGetter: (_value, row) => (row.tipo === 'padre' ? row.doc.numeroExpediente : ''),
      // 30/08/2026, pedido explícito del usuario: "replica el diseño de nested grid de la Bandeja de
      // supervisión" — mismo mecanismo `colSpan` (Community, no requiere DataGrid Pro) que ya fusiona
      // esta celda con el resto de columnas de datos cuando la fila es 'intentos', pero ahora renderiza
      // una tabla real (Table/TableHead/TableBody) con TODOS los intentos + borradores del documento,
      // dentro de la misma "tarjeta con acento lateral" (fondo `softGray`, borde izquierdo `info`) ya
      // usada en `NotificacionAsignacionBandejaPage.tsx` — en vez del formulario horizontal plano
      // anterior, uno por cada fila de intento/borrador.
      colSpan: (_value, row) => (row.tipo !== 'padre' ? 100 : undefined),
      renderCell: (params) => {
        const row = params.row
        if (row.tipo === 'padre') return row.doc.numeroExpediente
        const intentos = intentosPorDocumento[row.idDocumentoAnalizado]
        const borradores = borradoresPorDocumento[row.idDocumentoAnalizado] ?? []
        const totalFilas = (intentos?.length ?? 0) + borradores.length
        return (
          <Box
            sx={{
              width: 'fit-content',
              maxWidth: '100%',
              my: 0.75,
              py: 1,
              pl: 1.5,
              pr: 2,
              bgcolor: sdrercColors.softGray,
              borderLeft: `4px solid ${sdrercColors.info}`,
              borderRadius: '0 8px 8px 0',
            }}
          >
            <Typography
              variant="caption"
              sx={{
                display: 'block',
                mb: 0.75,
                fontWeight: 700,
                textTransform: 'uppercase',
                letterSpacing: '0.06em',
                color: sdrercColors.textSecondary,
              }}
            >
              Intentos de notificación del documento{intentos ? ` (${totalFilas})` : ''}
            </Typography>
            {!intentos ? (
              <Typography variant="caption" color="text.secondary">
                Cargando intentos de notificación…
              </Typography>
            ) : totalFilas === 0 ? (
              <Typography variant="caption" color="text.secondary">
                Este documento no tiene intentos de notificación registrados.
              </Typography>
            ) : (
              <TableContainer
                sx={{
                  width: 'auto',
                  maxWidth: '100%',
                  bgcolor: sdrercColors.backgroundPaper,
                  borderRadius: 1.5,
                  border: `1px solid ${sdrercColors.divider}`,
                  boxShadow: '0 1px 2px rgba(16,24,40,0.05)',
                }}
              >
                <Table size="small" sx={{ width: 'auto', tableLayout: 'auto' }}>
                  <TableHead>
                    <TableRow>
                      <TableCell sx={{ borderBottom: `1px solid ${sdrercColors.divider}` }} />
                      {['Nro.', 'Modalidad', 'Fecha Envío', 'Estado', 'Código/Usuario Notif.', 'Fecha Acuse', 'Estado Notificación'].map(
                        (titulo) => (
                          <TableCell
                            key={titulo}
                            sx={{
                              fontSize: '0.7rem',
                              fontWeight: 600,
                              textTransform: 'uppercase',
                              letterSpacing: '0.04em',
                              color: sdrercColors.textSecondary,
                              whiteSpace: 'nowrap',
                              borderBottom: `1px solid ${sdrercColors.divider}`,
                            }}
                          >
                            {titulo}
                          </TableCell>
                        ),
                      )}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {intentos.map((intento) => {
                      const filaHijo: FilaHijo = {
                        id: `hijo-${intento.idExpedienteNotificacion}`,
                        tipo: 'hijo',
                        idDocumentoAnalizado: row.idDocumentoAnalizado,
                        intento,
                      }
                      const edicion = edicionesIntento[intento.idExpedienteNotificacion]
                      if (!edicion) return null
                      const setCampo = (cambios: Partial<EdicionIntento>) =>
                        setEdicionesIntento((prev) => ({ ...prev, [intento.idExpedienteNotificacion]: { ...edicion, ...cambios } }))
                      const procesando = procesandoFila === filaHijo.id
                      return (
                        <TableRow key={intento.idExpedienteNotificacion} hover>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <Stack direction="row">
                              <Tooltip title="Guardar">
                                <span>
                                  <IconButton size="small" className={DATA_GRID_ROW_ICON_SAVE_CLASS} disabled={procesando} onClick={() => guardarIntento(filaHijo)}>
                                    <SaveOutlinedIcon fontSize="small" />
                                  </IconButton>
                                </span>
                              </Tooltip>
                              <Tooltip title="Eliminar">
                                <span>
                                  <IconButton size="small" className={DATA_GRID_ROW_ICON_DELETE_CLASS} disabled={procesando} onClick={() => eliminarIntento(filaHijo)}>
                                    <DeleteOutlinedIcon fontSize="small" />
                                  </IconButton>
                                </span>
                              </Tooltip>
                            </Stack>
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <Chip label={intento.numeroIntento} size="small" color="primary" variant="outlined" />
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField
                              select
                              size="small"
                              sx={{ minWidth: 120 }}
                              value={edicion.modalidad}
                              onChange={(e) => setCampo({ modalidad: e.target.value as 'VIRTUAL' | 'PRESENCIAL' })}
                            >
                              <MenuItem value="VIRTUAL">Virtual</MenuItem>
                              <MenuItem value="PRESENCIAL">Presencial</MenuItem>
                            </TextField>
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField
                              type="date"
                              size="small"
                              sx={{ minWidth: 145 }}
                              slotProps={{ inputLabel: { shrink: true } }}
                              value={edicion.fechaEnvio}
                              onChange={(e) => setCampo({ fechaEnvio: e.target.value })}
                            />
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField
                              select
                              size="small"
                              sx={{ minWidth: 120 }}
                              value={edicion.estado}
                              onChange={(e) => setCampo({ estado: e.target.value as 'PENDIENTE' | 'ENVIADA' | 'EXITOSA' })}
                            >
                              <MenuItem value="PENDIENTE">Pendiente</MenuItem>
                              <MenuItem value="ENVIADA">Enviado</MenuItem>
                              <MenuItem value="EXITOSA">Atendido</MenuItem>
                            </TextField>
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField size="small" sx={{ minWidth: 160 }} value={edicion.codigo} onChange={(e) => setCampo({ codigo: e.target.value })} />
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField
                              type="date"
                              size="small"
                              sx={{ minWidth: 145 }}
                              slotProps={{ inputLabel: { shrink: true } }}
                              value={edicion.fechaRecepcion}
                              onChange={(e) => setCampo({ fechaRecepcion: e.target.value })}
                            />
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField
                              select
                              size="small"
                              sx={{ minWidth: 150 }}
                              value={edicion.estadoNotif}
                              onChange={(e) => setCampo({ estadoNotif: e.target.value as '' | 'FALLIDA' | 'EXITOSA' })}
                            >
                              <MenuItem value="">(sin definir)</MenuItem>
                              <MenuItem value="FALLIDA">No ubicado</MenuItem>
                              <MenuItem value="EXITOSA">Ubicado</MenuItem>
                            </TextField>
                          </TableCell>
                        </TableRow>
                      )
                    })}
                    {borradores.map((borrador) => {
                      const edicion = edicionesBorrador[borrador.tempId]
                      if (!edicion) return null
                      const setCampo = (cambios: Partial<EdicionBorrador>) =>
                        setEdicionesBorrador((prev) => ({ ...prev, [borrador.tempId]: { ...edicion, ...cambios } }))
                      const procesando = procesandoFila === borrador.id
                      return (
                        <TableRow key={borrador.tempId} hover>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <Stack direction="row">
                              <Tooltip title="Guardar">
                                <span>
                                  <IconButton size="small" className={DATA_GRID_ROW_ICON_SAVE_CLASS} disabled={procesando} onClick={() => guardarBorrador(borrador)}>
                                    <SaveOutlinedIcon fontSize="small" />
                                  </IconButton>
                                </span>
                              </Tooltip>
                              <Tooltip title="Cancelar">
                                <span>
                                  <IconButton size="small" disabled={procesando} onClick={() => cancelarBorrador(borrador)}>
                                    <CloseOutlinedIcon fontSize="small" />
                                  </IconButton>
                                </span>
                              </Tooltip>
                            </Stack>
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <Chip label={`${borrador.numeroIntento} (nuevo)`} size="small" color="warning" variant="outlined" />
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField
                              select
                              size="small"
                              sx={{ minWidth: 120 }}
                              value={edicion.modalidad}
                              onChange={(e) => setCampo({ modalidad: e.target.value as 'VIRTUAL' | 'PRESENCIAL' })}
                            >
                              <MenuItem value="VIRTUAL">Virtual</MenuItem>
                              <MenuItem value="PRESENCIAL">Presencial</MenuItem>
                            </TextField>
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField
                              type="date"
                              size="small"
                              sx={{ minWidth: 145 }}
                              slotProps={{ inputLabel: { shrink: true } }}
                              value={edicion.fechaEnvio}
                              onChange={(e) => setCampo({ fechaEnvio: e.target.value })}
                            />
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField
                              select
                              size="small"
                              sx={{ minWidth: 120 }}
                              value={edicion.estado}
                              onChange={(e) => setCampo({ estado: e.target.value as 'PENDIENTE' | 'ENVIADA' | 'EXITOSA' })}
                            >
                              <MenuItem value="PENDIENTE">Pendiente</MenuItem>
                              <MenuItem value="ENVIADA">Enviado</MenuItem>
                              <MenuItem value="EXITOSA">Atendido</MenuItem>
                            </TextField>
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField size="small" sx={{ minWidth: 160 }} value={edicion.codigo} onChange={(e) => setCampo({ codigo: e.target.value })} />
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField
                              type="date"
                              size="small"
                              sx={{ minWidth: 145 }}
                              slotProps={{ inputLabel: { shrink: true } }}
                              value={edicion.fechaRecepcion}
                              onChange={(e) => setCampo({ fechaRecepcion: e.target.value })}
                            />
                          </TableCell>
                          <TableCell sx={{ whiteSpace: 'nowrap' }}>
                            <TextField
                              select
                              size="small"
                              sx={{ minWidth: 150 }}
                              value={edicion.estadoNotif}
                              onChange={(e) => setCampo({ estadoNotif: e.target.value as '' | 'FALLIDA' | 'EXITOSA' })}
                            >
                              <MenuItem value="">(sin definir)</MenuItem>
                              <MenuItem value="FALLIDA">No ubicado</MenuItem>
                              <MenuItem value="EXITOSA">Ubicado</MenuItem>
                            </TextField>
                          </TableCell>
                        </TableRow>
                      )
                    })}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Box>
        )
      },
    },
    {
      field: 'clasificacion',
      headerName: 'Clas. Documentos',
      width: 130,
      sortable: false,
      valueGetter: (_value, row) => (row.tipo === 'padre' ? row.doc.clasificacion : ''),
      renderCell: (params) => (params.row.tipo === 'padre' ? params.row.doc.clasificacion : ''),
    },
    {
      field: 'tipoDocumento',
      headerName: 'Tipo documento',
      width: 190,
      sortable: false,
      valueGetter: (_value, row) => (row.tipo === 'padre' ? row.doc.tipoDocumento : ''),
      renderCell: (params) => (params.row.tipo === 'padre' ? params.row.doc.tipoDocumento : ''),
    },
    {
      field: 'numeroDocumento',
      headerName: 'N° Documento',
      width: 140,
      sortable: false,
      valueGetter: (_value, row) => (row.tipo === 'padre' ? row.doc.numeroDocumento : ''),
      renderCell: (params) => (params.row.tipo === 'padre' ? params.row.doc.numeroDocumento : ''),
    },
    {
      field: 'fechaEmision',
      headerName: 'Fecha Emisión',
      width: 130,
      sortable: false,
      // Devuelve el texto ya formateado (dd/MM/yyyy) para que el operador de filtro por defecto
      // ("contiene") funcione contra lo mismo que el usuario ve en pantalla — mismo criterio simple
      // ya usado en `numeroExpediente`/`titular` de esta grilla, sin introducir `type:'date'` (exigiría
      // devolver un `Date` real y perdería el "-" que se muestra hoy para filas sin fecha).
      valueGetter: (_value, row) => (row.tipo === 'padre' ? formatearFecha(row.doc.fechaDocumento) : ''),
      renderCell: (params) => (params.row.tipo === 'padre' ? formatearFecha(params.row.doc.fechaDocumento) : ''),
    },
    {
      field: 'titular',
      headerName: 'Titular',
      flex: 1,
      minWidth: 180,
      sortable: false,
      valueGetter: (_value, row) => (row.tipo === 'padre' ? row.doc.titular : ''),
      renderCell: (params) => (params.row.tipo === 'padre' ? params.row.doc.titular : ''),
    },
    {
      field: 'estadoFinal',
      headerName: 'Estado Final',
      width: 150,
      sortable: false,
      valueGetter: (_value, row) => (row.tipo === 'padre' ? row.doc.estadoFinalNotificacion || 'Por notificar' : ''),
      renderCell: (params) => {
        if (params.row.tipo !== 'padre') return null
        const doc = params.row.doc
        const tone =
          doc.estadoFinalNotificacionCodigo === 'ATENDIDO'
            ? 'success'
            : doc.estadoFinalNotificacionCodigo === 'POR_PUBLICAR'
              ? 'error'
              : doc.estadoFinalNotificacionCodigo === 'PENDIENTE'
                ? 'warning'
                : 'info'
        return <StatusBadge label={doc.estadoFinalNotificacion || 'Por notificar'} tone={tone} />
      },
    },
    {
      field: 'estadoDoc',
      headerName: 'Estado doc.',
      width: 130,
      sortable: false,
      valueGetter: (_value, row) => (row.tipo === 'padre' ? row.doc.estadoDocumento : ''),
      renderCell: (params) => (params.row.tipo === 'padre' ? params.row.doc.estadoDocumento : ''),
    },
  ]

  const filtrosHeader = (
    <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', rowGap: 1.5 }}>
      <TextField
        label="Buscar (N° expediente, SGD, titular, documento...)"
        size="small"
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && buscar()}
        sx={{ minWidth: 280 }}
      />
      <Button variant="contained" size="small" onClick={buscar} disabled={loading}>
        Buscar
      </Button>
      <Button variant="outlined" size="small" onClick={handleLimpiar} disabled={loading}>
        Limpiar
      </Button>
      <TextField
        label="Fecha Emisión desde"
        type="date"
        size="small"
        value={desde}
        onChange={(e) => setDesde(e.target.value)}
        slotProps={{ inputLabel: { shrink: true } }}
      />
      <TextField
        label="Fecha Emisión hasta"
        type="date"
        size="small"
        value={hasta}
        onChange={(e) => setHasta(e.target.value)}
        slotProps={{ inputLabel: { shrink: true } }}
      />
      <TextField
        select
        label="Estado Final"
        size="small"
        sx={{ minWidth: 180 }}
        value={estadoFinalFiltro}
        onChange={(e) => setEstadoFinalFiltro(e.target.value)}
      >
        <MenuItem value="">Todos los estados</MenuItem>
        <MenuItem value="POR_NOTIFICAR">Por notificar</MenuItem>
        <MenuItem value="PENDIENTE">Pendiente</MenuItem>
        <MenuItem value="ATENDIDO">Atendido</MenuItem>
        <MenuItem value="POR_PUBLICAR">Por publicar</MenuItem>
      </TextField>
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Bandeja Notificación"
      subtitulo="Intentos de notificación al ciudadano."
      tabs={NOTIFICACION_TABS}
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
        <Box onClick={() => alternarFiltroKpi('POR_NOTIFICAR')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Por notificar" value={kpis.porNotificar} caption="Sin intentos aún" color={sdrercColors.info} active={filtroKpi === 'POR_NOTIFICAR'} />
        </Box>
        <Box onClick={() => alternarFiltroKpi('PENDIENTE')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Pendiente" value={kpis.pendiente} caption="Intento en curso" color={sdrercColors.warning} active={filtroKpi === 'PENDIENTE'} />
        </Box>
        <Box onClick={() => alternarFiltroKpi('ATENDIDO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Atendido" value={kpis.atendido} caption="Ciudadano ubicado" color={sdrercColors.success} active={filtroKpi === 'ATENDIDO'} />
        </Box>
        <Box onClick={() => alternarFiltroKpi('POR_PUBLICAR')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Por publicar" value={kpis.porPublicar} caption="Ambos intentos fallidos" color={sdrercColors.error} active={filtroKpi === 'POR_PUBLICAR'} />
        </Box>
        <Box onClick={() => alternarFiltroKpi('POR_VENCER')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Por vencer" value={kpis.porVencer} caption="0 a 5 días hábiles" color={sdrercColors.warning} active={filtroKpi === 'POR_VENCER'} />
        </Box>
        <Box onClick={() => alternarFiltroKpi('VENCIDOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Vencidos" value={kpis.vencidos} caption="Plazo excedido" color={sdrercColors.error} active={filtroKpi === 'VENCIDOS'} />
        </Box>
      </Box>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2, mb: 3, overflow: 'hidden', boxShadow: '0 1px 3px rgba(16,24,40,0.08), 0 1px 2px rgba(16,24,40,0.04)' }}>
        <DataGrid
          rows={filasConHijos}
          columns={columnas}
          getRowId={(row) => row.id}
          loading={loading}
          density="compact"
          autoHeight
          showToolbar
          checkboxSelection
          isRowSelectable={(params) => (params.row as FilaNotif).tipo === 'padre'}
          rowSelectionModel={rowSelectionModel}
          onRowSelectionModelChange={setRowSelectionModel}
          hideFooterSelectedRowCount
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          onRowDoubleClick={(params) => {
            const row = params.row as FilaNotif
            if (row.tipo !== 'padre') return
            setIdPadreEnfocado(row.doc.idDocumentoAnalizado)
            abrirWorkspace(row.doc)
          }}
          onRowClick={(params) => {
            const row = params.row as FilaNotif
            if (row.tipo === 'padre') setIdPadreEnfocado(row.doc.idDocumentoAnalizado)
          }}
          slots={{ toolbar: ToolbarBandejaNotificacion }}
          slotProps={{ toolbar: { seleccionados: idsSeleccionados, onAgregarIntento: handleAgregarIntento } }}
          getRowHeight={() => 'auto'}
          getRowClassName={(params) => ((params.row as FilaNotif).tipo !== 'padre' ? 'fila-intento' : '')}
          sx={{
            ...dataGridBaseSx,
            '& .fila-intento': { bgcolor: sdrercColors.softBlue },
            '& .fila-intento .MuiDataGrid-cell': { py: 1 },
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
