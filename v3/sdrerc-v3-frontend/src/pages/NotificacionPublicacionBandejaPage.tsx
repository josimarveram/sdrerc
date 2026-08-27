import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  IconButton,
  MenuItem,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import {
  DataGrid,
  GridToolbarColumnsButton,
  GridToolbarContainer,
  GridToolbarExport,
  GridToolbarFilterButton,
  GridToolbarQuickFilter,
  useGridApiContext,
  type GridColDef,
  type GridRowSelectionModel,
} from '@mui/x-data-grid'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import RemoveCircleOutlineIcon from '@mui/icons-material/RemoveCircleOutlined'
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined'
import CloseOutlinedIcon from '@mui/icons-material/CloseOutlined'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import {
  actualizarPublicacion,
  agregarPublicacion,
  eliminarPublicacion,
  listarBandejaPublicacion,
  listarIntentosPublicacion,
  type NotificacionAsignacionDocumento,
  type NotificacionIntento,
} from '../api/notificacionPublicacionApi'
import { obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { primerDiaMesActualISO, hoyISO } from '../utils/fechas'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { NOTIFICACION_TABS } from '../layout/moduleTabs'

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

/** Port de `textoModalidad`/`codigoModalidadColumna` (V2) para un intento directo (1/2), solo lectura. */
function textoModalidadDirecto(tipoNotificacionCodigo: string): string {
  return tipoNotificacionCodigo?.toUpperCase() === 'VIRTUAL' ? 'Virtual' : 'Presencial'
}

/** Port de `textoEstadoHija` (V2): EXITOSA->Atendido, PENDIENTE->Pendiente, resto(ENVIADA/FALLIDA)->Enviado. */
function textoEstadoDirecto(estadoNotificacionCodigo: string): string {
  const c = estadoNotificacionCodigo?.toUpperCase()
  if (c === 'EXITOSA') return 'Atendido'
  if (c === 'PENDIENTE') return 'Pendiente'
  return 'Enviado'
}

/** Port de `textoEstadoNotifHija` (V2): EXITOSA->Ubicado, FALLIDA->No ubicado, resto->"-". */
function textoEstadoNotifDirecto(estadoNotificacionCodigo: string): string {
  const c = estadoNotificacionCodigo?.toUpperCase()
  if (c === 'EXITOSA') return 'Ubicado'
  if (c === 'FALLIDA') return 'No ubicado'
  return '-'
}

/** Port de `textoEstadoFinalPublicacion` (V2): en esta bandeja, ATENDIDO solo puede venir de una Publicación exitosa. */
function textoEstadoFinalPublicacion(doc: NotificacionAsignacionDocumento): string {
  if (doc.estadoFinalNotificacionCodigo === 'ATENDIDO') return 'Publicado'
  return doc.estadoFinalNotificacion || 'Por publicar'
}

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#2370A8'

type FilaPadre = { id: string; tipo: 'padre'; doc: NotificacionAsignacionDocumento }
/** Intento directo (1 o 2) — SIEMPRE de solo lectura en esta bandeja (mismo criterio que V2: "sin ícono de acción, sin editores"). */
type FilaDirecto = { id: string; tipo: 'directo'; idDocumentoAnalizado: number; intento: NotificacionIntento }
/** La fila de Publicación ya persistida — única editable entre los registros ya guardados. */
type FilaPublicacion = { id: string; tipo: 'publicacion'; idDocumentoAnalizado: number; intento: NotificacionIntento }
type FilaBorrador = { id: string; tipo: 'borrador'; idDocumentoAnalizado: number; idExpediente: number; tempId: number }
type FilaNotif = FilaPadre | FilaDirecto | FilaPublicacion | FilaBorrador

interface EdicionPublicacion {
  estadoNotif: 'PENDIENTE' | 'EXITOSA'
  fechaPublicacion: string
}

function edicionInicialPublicacion(intento: NotificacionIntento): EdicionPublicacion {
  return {
    estadoNotif: intento.estadoNotificacionCodigo?.toUpperCase() === 'EXITOSA' ? 'EXITOSA' : 'PENDIENTE',
    fechaPublicacion: intento.fechaEnvio ? intento.fechaEnvio.slice(0, 10) : '',
  }
}

/**
 * "+ Agregar publicación" — port de la regla real de V2 ("no aplica si el documento ya tiene una
 * publicación registrada o si ya alcanzó el máximo de 3 intentos"), más la misma protección ya
 * aplicada en Bandeja Notificación (nunca 2 borradores sin guardar a la vez).
 */
function puedeAgregarPublicacion(
  intentos: NotificacionIntento[],
  borradoresActuales: FilaBorrador[],
): { ok: true } | { ok: false; motivo: 'borrador-pendiente' | 'ya-registrada' | 'maximo' } {
  if (borradoresActuales.length > 0) return { ok: false, motivo: 'borrador-pendiente' }
  if (intentos.some((i) => i.tipoNotificacionCodigo?.toUpperCase() === 'PUBLICACION')) return { ok: false, motivo: 'ya-registrada' }
  if (intentos.length + 1 > 3) return { ok: false, motivo: 'maximo' }
  return { ok: true }
}

declare module '@mui/x-data-grid' {
  interface ToolbarPropsOverrides {
    seleccionados: number[]
    onAgregarPublicacion: () => void
  }
}

function ToolbarBandejaPublicacion({ seleccionados, onAgregarPublicacion }: { seleccionados: number[]; onAgregarPublicacion: () => void }) {
  const apiRef = useGridApiContext()
  return (
    <GridToolbarContainer>
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
        {seleccionados.length > 0 && (
          <Chip label={`${seleccionados.length} seleccionado${seleccionados.length > 1 ? 's' : ''}`} size="small" color="primary" variant="outlined" />
        )}
        <Button size="small" variant="contained" startIcon={<AddCircleOutlineIcon />} onClick={onAgregarPublicacion}>
          Agregar publicación
        </Button>
        {seleccionados.length > 0 && (
          <Button size="small" onClick={() => apiRef.current.exportDataAsCsv({ getRowsToExport: () => seleccionados })}>
            Exportar seleccionados
          </Button>
        )}
      </Stack>
      <Box sx={{ flex: 1 }} />
      <GridToolbarColumnsButton />
      <GridToolbarFilterButton />
      <GridToolbarExport />
      <GridToolbarQuickFilter />
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

type FiltroKpi = 'SIN_PUBLICACION' | 'PUBLICACION_REGISTRADA' | 'VENCIDOS' | null

/**
 * Equivalente web de la 4ta pestaña de Notificación, "Bandeja Publicación" (24/08/2026, pedido
 * explícito del usuario: "sería casi lo mismo que la bandeja de notificación, solo que sería 1 sola
 * notificación de tipo publicación"). Mismo diseño/patrón que `NotificacionNotificacionBandejaPage.tsx`
 * (KPIs clicables, buscador de 3 filas, grilla árbol sobre un único `DataGrid` con filas sintéticas
 * fusionadas vía `colSpan`, doble clic abre ÚNICAMENTE el Panel de datos) — ver Javadoc de esa página
 * para el detalle de esas decisiones de diseño, no repetido acá.
 *
 * Diferencias reales de negocio contra Bandeja Notificación (investigación previa contra la sección
 * "Bandeja Publicacion" ya documentada en el `CLAUDE.md` raíz — implementada en V2 el 05/08/2026):
 * - Fuente de datos: {@code listarDocumentosBandejaPublicacion} — mismo universo que Bandeja
 *   Notificación, pero acotado a documentos que YA agotaron sus 2 intentos directos
 *   ({@code agoto_intentos_directos = 1}, justo lo opuesto).
 * - Los intentos 1 y 2, al expandir, se muestran de SOLO LECTURA (sin ícono de acción, sin editores;
 *   confirmado contra V2 real) — únicamente el 3er registro ("Publicación", mismo mecanismo de
 *   intento con {@code tipo_notificacion=PUBLICACION}) es editable: "Fecha Publicación" (reutiliza
 *   {@code fecha_envio}, mismo campo que cualquier intento) + "Estado Notif./Public." (combo
 *   Pendiente/Publicado). Antes de guardarse aparece como fila borrador (Guardar+Cancelar); ya
 *   guardada permite editar y tiene ícono Eliminar.
 * - "+ Agregar publicación" (botón renombrado, mismo mecanismo que "+ Agregar intento"): no aplica
 *   si el documento ya tiene una publicación registrada o alcanzó el máximo de 3 intentos.
 * - Columna propia adicional en la fila padre: "Fecha Publicación" ({@code doc.fechaPublicacionNotif},
 *   ya expuesta por el DTO compartido). "Estado Final" se relabela a "Publicado" cuando el código real
 *   es {@code ATENDIDO} (en este universo, solo puede provenir de una Publicación exitosa — nunca de
 *   un intento 1/2, imposible aquí).
 * - KPIs propios de V2 real (no los 6 de Bandeja Notificación): "Documentos" (total), "Sin
 *   publicación registrar" / "Publicación registrada" (según {@code fechaPublicacionNotif}) y
 *   "Vencidos".
 */
export function NotificacionPublicacionBandejaPage() {
  const [documentos, setDocumentos] = useState<NotificacionAsignacionDocumento[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [expandidos, setExpandidos] = useState<Set<number>>(new Set())
  const [intentosPorDocumento, setIntentosPorDocumento] = useState<Record<number, NotificacionIntento[]>>({})
  const [cargandoIntentos, setCargandoIntentos] = useState<Set<number>>(new Set())
  const [borradoresPorDocumento, setBorradoresPorDocumento] = useState<Record<number, FilaBorrador[]>>({})
  const [edicionesPublicacion, setEdicionesPublicacion] = useState<Record<number, EdicionPublicacion>>({})
  const [edicionesBorrador, setEdicionesBorrador] = useState<Record<number, EdicionPublicacion>>({})
  const [procesandoFila, setProcesandoFila] = useState<string | null>(null)
  const tempIdRef = useRef(-1)

  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)
  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaMesActualISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [estadoFinalFiltro, setEstadoFinalFiltro] = useState('')

  const [rowSelectionModel, setRowSelectionModel] = useState<GridRowSelectionModel>({ type: 'include', ids: new Set() })
  const [idPadreEnfocado, setIdPadreEnfocado] = useState<number | null>(null)

  type WorkspaceAbierto = { id: string; doc: NotificacionAsignacionDocumento; color: string }
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [datosPorExpediente, setDatosPorExpediente] = useState<
    Record<number, { detalle: ExpedienteEdicionManual | null; cargando: boolean; error: string | null }>
  >({})

  const buscar = useCallback(() => {
    setLoading(true)
    setError(null)
    listarBandejaPublicacion()
      .then(setDocumentos)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    buscar()
  }, [buscar])

  const kpis = useMemo(() => {
    let sinPublicacion = 0
    let publicacionRegistrada = 0
    let vencidos = 0
    for (const doc of documentos) {
      if (doc.fechaPublicacionNotif) publicacionRegistrada++
      else sinPublicacion++
      if (doc.diasRestantes !== null && doc.diasRestantes < 0) vencidos++
    }
    return { documentos: documentos.length, sinPublicacion, publicacionRegistrada, vencidos }
  }, [documentos])

  function coincideFiltroKpi(doc: NotificacionAsignacionDocumento): boolean {
    switch (filtroKpi) {
      case 'SIN_PUBLICACION':
        return !doc.fechaPublicacionNotif
      case 'PUBLICACION_REGISTRADA':
        return !!doc.fechaPublicacionNotif
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
    setDesde(primerDiaMesActualISO())
    setHasta(hoyISO())
    setEstadoFinalFiltro('')
  }

  async function asegurarIntentosCargados(idDocumento: number): Promise<NotificacionIntento[]> {
    if (intentosPorDocumento[idDocumento]) return intentosPorDocumento[idDocumento]
    setCargandoIntentos((prev) => new Set(prev).add(idDocumento))
    try {
      const intentos = await listarIntentosPublicacion(idDocumento)
      setIntentosPorDocumento((prev) => ({ ...prev, [idDocumento]: intentos }))
      setEdicionesPublicacion((prev) => {
        const next = { ...prev }
        for (const intento of intentos) {
          if (intento.tipoNotificacionCodigo?.toUpperCase() === 'PUBLICACION') {
            next[intento.idExpedienteNotificacion] = edicionInicialPublicacion(intento)
          }
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
   * "+ Agregar publicación" — mismo patrón puro (calcular todo antes de tocar estado o mostrar
   * diálogos) ya corregido en Bandeja Notificación tras el bug real de StrictMode duplicando el
   * `alert` (ver `v3/CLAUDE.md`, 23/08/2026) — replicado acá desde el día 1 para no reintroducirlo.
   */
  async function handleAgregarPublicacion() {
    const objetivos = new Set<number>(idsSeleccionados)
    if (objetivos.size === 0 && idPadreEnfocado !== null) {
      objetivos.add(idPadreEnfocado)
    }
    if (objetivos.size === 0) {
      window.alert('Seleccione uno o varios documentos de la bandeja de publicación.')
      return
    }
    const cargas = await Promise.all([...objetivos].map(async (id) => [id, await asegurarIntentosCargados(id)] as const))
    const intentosPorId = new Map(cargas)

    const borradoresNuevosPorDoc = new Map<number, FilaBorrador>()
    const edicionesNuevas = new Map<number, EdicionPublicacion>()
    const idsParaExpandir: number[] = []
    const motivos = new Set<string>()

    for (const id of objetivos) {
      const doc = documentos.find((d) => d.idDocumentoAnalizado === id)
      if (!doc) continue
      const intentos = intentosPorId.get(id) ?? []
      const borradoresExistentes = borradoresPorDocumento[id] ?? []
      const resultado = puedeAgregarPublicacion(intentos, borradoresExistentes)
      if (!resultado.ok) {
        motivos.add(resultado.motivo)
        continue
      }
      const tempId = tempIdRef.current--
      borradoresNuevosPorDoc.set(id, { id: `borrador-${tempId}`, tipo: 'borrador', idDocumentoAnalizado: id, idExpediente: doc.idExpediente, tempId })
      edicionesNuevas.set(tempId, { estadoNotif: 'PENDIENTE', fechaPublicacion: '' })
      idsParaExpandir.push(id)
    }

    if (borradoresNuevosPorDoc.size === 0) {
      const mensajes: Record<string, string> = {
        'borrador-pendiente': 'Ya hay una publicación sin guardar. Guarde o cancele esa fila antes de agregar otra.',
        'ya-registrada': 'Este documento ya tiene una publicación registrada.',
        maximo: 'Ya se alcanzó el máximo de 3 intentos (2 directos + publicación) para este documento.',
      }
      window.alert([...motivos].map((m) => mensajes[m]).join('\n') || 'No se pudo agregar la publicación.')
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
      for (const [tempId, edicion] of edicionesNuevas) next[tempId] = edicion
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

  async function recargarIntentos(idDocumento: number) {
    const intentos = await listarIntentosPublicacion(idDocumento)
    setIntentosPorDocumento((prev) => ({ ...prev, [idDocumento]: intentos }))
    setEdicionesPublicacion((prev) => {
      const next = { ...prev }
      for (const intento of intentos) {
        if (intento.tipoNotificacionCodigo?.toUpperCase() === 'PUBLICACION') {
          next[intento.idExpedienteNotificacion] = edicionInicialPublicacion(intento)
        }
      }
      return next
    })
  }

  async function guardarBorrador(fila: FilaBorrador) {
    const edicion = edicionesBorrador[fila.tempId]
    if (!edicion) return
    setProcesandoFila(fila.id)
    setError(null)
    try {
      await agregarPublicacion({ idExpediente: fila.idExpediente, idDocumentoAnalizado: fila.idDocumentoAnalizado, fechaPublicacion: edicion.fechaPublicacion || null })
      if (edicion.estadoNotif === 'EXITOSA') {
        const intentos = await listarIntentosPublicacion(fila.idDocumentoAnalizado)
        const creado = intentos.find((i) => i.tipoNotificacionCodigo?.toUpperCase() === 'PUBLICACION')
        if (creado) {
          await actualizarPublicacion({
            idExpedienteNotificacion: creado.idExpedienteNotificacion,
            estadoNotificacionCodigo: 'EXITOSA',
            fechaPublicacion: edicion.fechaPublicacion || null,
          })
        }
      }
      cancelarBorrador(fila)
      await recargarIntentos(fila.idDocumentoAnalizado)
      buscar()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo registrar la publicación.')
    } finally {
      setProcesandoFila(null)
    }
  }

  async function guardarPublicacion(fila: FilaPublicacion) {
    const edicion = edicionesPublicacion[fila.intento.idExpedienteNotificacion]
    if (!edicion) return
    setProcesandoFila(fila.id)
    setError(null)
    try {
      await actualizarPublicacion({
        idExpedienteNotificacion: fila.intento.idExpedienteNotificacion,
        estadoNotificacionCodigo: edicion.estadoNotif,
        fechaPublicacion: edicion.fechaPublicacion || null,
      })
      await recargarIntentos(fila.idDocumentoAnalizado)
      buscar()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo actualizar la publicación.')
    } finally {
      setProcesandoFila(null)
    }
  }

  async function handleEliminarPublicacion(fila: FilaPublicacion) {
    if (!window.confirm('¿Desea eliminar el registro de publicación?')) return
    setProcesandoFila(fila.id)
    setError(null)
    try {
      await eliminarPublicacion(fila.intento.idExpedienteNotificacion)
      await recargarIntentos(fila.idDocumentoAnalizado)
      buscar()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo eliminar la publicación.')
    } finally {
      setProcesandoFila(null)
    }
  }

  const filasConHijos = useMemo<FilaNotif[]>(() => {
    const resultado: FilaNotif[] = []
    for (const doc of documentosFiltrados) {
      resultado.push({ id: `doc-${doc.idDocumentoAnalizado}`, tipo: 'padre', doc })
      if (expandidos.has(doc.idDocumentoAnalizado)) {
        const intentos = intentosPorDocumento[doc.idDocumentoAnalizado] ?? []
        for (const intento of intentos) {
          if (intento.tipoNotificacionCodigo?.toUpperCase() === 'PUBLICACION') {
            resultado.push({ id: `pub-${intento.idExpedienteNotificacion}`, tipo: 'publicacion', idDocumentoAnalizado: doc.idDocumentoAnalizado, intento })
          } else {
            resultado.push({ id: `directo-${intento.idExpedienteNotificacion}`, tipo: 'directo', idDocumentoAnalizado: doc.idDocumentoAnalizado, intento })
          }
        }
        for (const borrador of borradoresPorDocumento[doc.idDocumentoAnalizado] ?? []) {
          resultado.push(borrador)
        }
      }
    }
    return resultado
  }, [documentosFiltrados, expandidos, intentosPorDocumento, borradoresPorDocumento])

  function abrirWorkspace(doc: NotificacionAsignacionDocumento) {
    const id = `ws-${doc.idExpediente}`
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) {
        return prev.map((w) => (w.id === id ? { ...w, doc } : w))
      }
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { id, doc, color }]
    })
    if (datosPorExpediente[doc.idExpediente]) return
    recargarDatosExpediente(doc.idExpediente)
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
          activeSubTabId: 'datos',
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
          ],
        }
      }),
    [workspacesAbiertos, datosPorExpediente],
  )

  const columnas: GridColDef<FilaNotif>[] = [
    {
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
        const puedeExpandir = row.doc.totalIntentos > 0
        if (!puedeExpandir) return null
        const expandido = expandidos.has(row.doc.idDocumentoAnalizado)
        return (
          <IconButton size="small" onClick={() => alternarExpansion(row.doc)} aria-label={expandido ? 'Colapsar intentos' : 'Expandir intentos'}>
            {expandido ? <RemoveCircleOutlineIcon fontSize="small" /> : <AddCircleOutlineIcon fontSize="small" />}
          </IconButton>
        )
      },
    },
    {
      field: 'accion',
      headerName: '',
      width: 90,
      sortable: false,
      filterable: false,
      renderCell: (params) => {
        const row = params.row
        const procesando = procesandoFila === row.id
        if (row.tipo === 'padre' || row.tipo === 'directo') return null
        if (row.tipo === 'borrador') {
          return (
            <Stack direction="row">
              <Tooltip title="Guardar">
                <span>
                  <IconButton size="small" disabled={procesando} onClick={() => guardarBorrador(row)}>
                    <SaveOutlinedIcon fontSize="small" />
                  </IconButton>
                </span>
              </Tooltip>
              <Tooltip title="Cancelar">
                <span>
                  <IconButton size="small" disabled={procesando} onClick={() => cancelarBorrador(row)}>
                    <CloseOutlinedIcon fontSize="small" />
                  </IconButton>
                </span>
              </Tooltip>
            </Stack>
          )
        }
        return (
          <Stack direction="row">
            <Tooltip title="Guardar">
              <span>
                <IconButton size="small" disabled={procesando} onClick={() => guardarPublicacion(row)}>
                  <SaveOutlinedIcon fontSize="small" />
                </IconButton>
              </span>
            </Tooltip>
            <Tooltip title="Eliminar">
              <span>
                <IconButton size="small" disabled={procesando} onClick={() => handleEliminarPublicacion(row)}>
                  <DeleteOutlinedIcon fontSize="small" />
                </IconButton>
              </span>
            </Tooltip>
          </Stack>
        )
      },
    },
    {
      field: 'numeroExpediente',
      headerName: 'N° expediente',
      width: 190,
      sortable: false,
      filterable: false,
      // Mismo criterio ya establecido en Bandeja Notificación (ver Javadoc de esa página): las
      // columnas de intento se muestran DEBAJO de la fila principal (colSpan sobre toda la sección de
      // datos), no como columnas adicionales a la derecha.
      colSpan: (_value, row) => (row.tipo !== 'padre' ? 100 : undefined),
      renderCell: (params) => {
        const row = params.row
        if (row.tipo === 'padre') return row.doc.numeroExpediente
        if (row.tipo === 'directo') {
          const i = row.intento
          return (
            <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', py: 1, width: '100%' }}>
              <Chip label={`Intento ${i.numeroIntento}`} size="small" variant="outlined" />
              <Typography variant="body2" sx={{ minWidth: 90 }}>
                {textoModalidadDirecto(i.tipoNotificacionCodigo)}
              </Typography>
              <Typography variant="body2" sx={{ minWidth: 110 }}>
                {formatearFecha(i.fechaEnvio)}
              </Typography>
              <Typography variant="body2" sx={{ minWidth: 90 }}>
                {textoEstadoDirecto(i.estadoNotificacionCodigo)}
              </Typography>
              <Typography variant="body2" sx={{ minWidth: 130 }}>
                {i.codigoNotificacion || '-'}
              </Typography>
              <Typography variant="body2" sx={{ minWidth: 110 }}>
                {formatearFecha(i.fechaRecepcion)}
              </Typography>
              <Typography variant="body2" sx={{ minWidth: 110 }}>
                {textoEstadoNotifDirecto(i.estadoNotificacionCodigo)}
              </Typography>
            </Stack>
          )
        }
        if (row.tipo === 'publicacion') {
          const edicion = edicionesPublicacion[row.intento.idExpedienteNotificacion]
          if (!edicion) return null
          const setCampo = (cambios: Partial<EdicionPublicacion>) =>
            setEdicionesPublicacion((prev) => ({ ...prev, [row.intento.idExpedienteNotificacion]: { ...edicion, ...cambios } }))
          return (
            <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', py: 1, width: '100%' }}>
              <Chip label="Publicación" size="small" color="primary" variant="outlined" />
              <Typography variant="body2" sx={{ minWidth: 90 }}>
                Publicación
              </Typography>
              <Typography variant="body2" sx={{ minWidth: 90 }}>
                Enviado
              </Typography>
              <Typography variant="body2" sx={{ minWidth: 130 }}>
                -
              </Typography>
              <TextField
                label="Fecha Publicación"
                type="date"
                size="small"
                sx={{ minWidth: 160 }}
                slotProps={{ inputLabel: { shrink: true } }}
                value={edicion.fechaPublicacion}
                onChange={(e) => setCampo({ fechaPublicacion: e.target.value })}
              />
              <TextField
                select
                label="Estado Notif./Public."
                size="small"
                sx={{ minWidth: 170 }}
                value={edicion.estadoNotif}
                onChange={(e) => setCampo({ estadoNotif: e.target.value as 'PENDIENTE' | 'EXITOSA' })}
              >
                <MenuItem value="PENDIENTE">Pendiente</MenuItem>
                <MenuItem value="EXITOSA">Publicado</MenuItem>
              </TextField>
            </Stack>
          )
        }
        const edicion = edicionesBorrador[row.tempId]
        if (!edicion) return null
        const setCampo = (cambios: Partial<EdicionPublicacion>) => setEdicionesBorrador((prev) => ({ ...prev, [row.tempId]: { ...edicion, ...cambios } }))
        return (
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', py: 1, width: '100%' }}>
            <Chip label="Publicación (nueva)" size="small" color="warning" variant="outlined" />
            <Typography variant="body2" sx={{ minWidth: 90 }}>
              Publicación
            </Typography>
            <Typography variant="body2" sx={{ minWidth: 90 }}>
              Enviado
            </Typography>
            <Typography variant="body2" sx={{ minWidth: 130 }}>
              -
            </Typography>
            <TextField
              label="Fecha Publicación"
              type="date"
              size="small"
              sx={{ minWidth: 160 }}
              slotProps={{ inputLabel: { shrink: true } }}
              value={edicion.fechaPublicacion}
              onChange={(e) => setCampo({ fechaPublicacion: e.target.value })}
            />
            <TextField
              select
              label="Estado Notif./Public."
              size="small"
              sx={{ minWidth: 170 }}
              value={edicion.estadoNotif}
              onChange={(e) => setCampo({ estadoNotif: e.target.value as 'PENDIENTE' | 'EXITOSA' })}
            >
              <MenuItem value="PENDIENTE">Pendiente</MenuItem>
              <MenuItem value="EXITOSA">Publicado</MenuItem>
            </TextField>
          </Stack>
        )
      },
    },
    {
      field: 'clasificacion',
      headerName: 'Clas. Documentos',
      width: 130,
      sortable: false,
      filterable: false,
      renderCell: (params) => (params.row.tipo === 'padre' ? params.row.doc.clasificacion : ''),
    },
    {
      field: 'tipoDocumento',
      headerName: 'Tipo documento',
      width: 190,
      sortable: false,
      filterable: false,
      renderCell: (params) => (params.row.tipo === 'padre' ? params.row.doc.tipoDocumento : ''),
    },
    {
      field: 'numeroDocumento',
      headerName: 'N° Documento',
      width: 140,
      sortable: false,
      filterable: false,
      renderCell: (params) => (params.row.tipo === 'padre' ? params.row.doc.numeroDocumento : ''),
    },
    {
      field: 'fechaEmision',
      headerName: 'Fecha Emisión',
      width: 130,
      sortable: false,
      filterable: false,
      renderCell: (params) => (params.row.tipo === 'padre' ? formatearFecha(params.row.doc.fechaDocumento) : ''),
    },
    {
      field: 'titular',
      headerName: 'Titular',
      flex: 1,
      minWidth: 180,
      sortable: false,
      filterable: false,
      renderCell: (params) => (params.row.tipo === 'padre' ? params.row.doc.titular : ''),
    },
    {
      field: 'estadoFinal',
      headerName: 'Estado Final',
      width: 150,
      sortable: false,
      filterable: false,
      renderCell: (params) => {
        if (params.row.tipo !== 'padre') return null
        const doc = params.row.doc
        const tone = doc.estadoFinalNotificacionCodigo === 'ATENDIDO' ? 'success' : 'error'
        return <StatusBadge label={textoEstadoFinalPublicacion(doc)} tone={tone} />
      },
    },
    {
      field: 'estadoDoc',
      headerName: 'Estado doc.',
      width: 130,
      sortable: false,
      filterable: false,
      renderCell: (params) => (params.row.tipo === 'padre' ? params.row.doc.estadoDocumento : ''),
    },
    {
      field: 'fechaPublicacion',
      headerName: 'Fecha Publicación',
      width: 160,
      sortable: false,
      filterable: false,
      renderCell: (params) => (params.row.tipo === 'padre' ? formatearFecha(params.row.doc.fechaPublicacionNotif) : ''),
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
        <MenuItem value="POR_PUBLICAR">Por publicar</MenuItem>
        <MenuItem value="ATENDIDO">Publicado</MenuItem>
      </TextField>
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Bandeja de publicación"
      subtitulo="Documentos que agotaron sus intentos directos de notificación."
      tabs={NOTIFICACION_TABS}
      filtros={filtrosHeader}
    >
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 2, mb: 2 }}>
        <Box onClick={() => alternarFiltroKpi(null)} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Documentos" value={kpis.documentos} caption="Total en la bandeja" color={sdrercColors.info} />
        </Box>
        <Box onClick={() => alternarFiltroKpi('SIN_PUBLICACION')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Sin publicación registrar" value={kpis.sinPublicacion} caption="Aún no registrada" color={sdrercColors.warning} />
        </Box>
        <Box onClick={() => alternarFiltroKpi('PUBLICACION_REGISTRADA')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Publicación registrada" value={kpis.publicacionRegistrada} caption="Ya publicado" color={sdrercColors.success} />
        </Box>
        <Box onClick={() => alternarFiltroKpi('VENCIDOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Vencidos" value={kpis.vencidos} caption="Plazo excedido" color={sdrercColors.error} />
        </Box>
      </Box>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2, mb: 3 }}>
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
          slots={{ toolbar: ToolbarBandejaPublicacion }}
          slotProps={{ toolbar: { seleccionados: idsSeleccionados, onAgregarPublicacion: handleAgregarPublicacion } }}
          getRowHeight={() => 'auto'}
          getRowClassName={(params) => ((params.row as FilaNotif).tipo !== 'padre' ? 'fila-intento' : '')}
          sx={{
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
        onSelectSubTab={() => {}}
      />
    </PageHeaderSticky>
  )
}
