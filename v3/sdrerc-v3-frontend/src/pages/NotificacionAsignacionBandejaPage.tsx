import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Badge,
  Box,
  Checkbox,
  Chip,
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
import CreateNewFolderOutlinedIcon from '@mui/icons-material/CreateNewFolderOutlined'
import FolderOpenOutlinedIcon from '@mui/icons-material/FolderOpenOutlined'
import {
  DataGrid,
  GridToolbarContainer,
  useGridApiContext,
  type GridColDef,
  type GridComparatorFn,
  type GridRowSelectionModel,
} from '@mui/x-data-grid'
import { Button } from '@mui/material'
import {
  listarBandejaAsignacionNotificacion,
  type NotificacionAsignacionDocumento,
} from '../api/notificacionAsignacionApi'
import { obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import { listarDocumentosAnalizados, type DocumentoAnalizado } from '../api/analisisApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { dataGridBaseSx, DATA_GRID_ROW_ICON_FOLDER_CLASS } from '../dataGridTheme'
import { DataGridStandardToolbarControls } from '../components/DataGridStandardToolbar'
import { StatusBadge, diasBadgeTone } from '../components/StatusBadge'
import { primerDiaMesAnteriorISO, hoyISO } from '../utils/fechas'
import { MetricCard } from '../components/MetricCard'
import { NotificacionSupervisionContent } from '../components/NotificacionSupervisionContent'
import { NotificacionAsignacionMultipleContent } from '../components/NotificacionAsignacionMultipleContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
import { TrazabilidadContent } from '../components/TrazabilidadContent'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { NOTIFICACION_TABS } from '../layout/moduleTabs'

type FiltroEstadoDocumento = '' | 'EN_DESPACHO' | 'VALIDADO' | 'EMITIDO' | 'OBSERVADO'

type FiltroKpi = 'PENDIENTES' | 'ASIGNADOS' | 'LISTOS_FIRMA' | 'INTERMEDIOS' | null

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#2370A8'
const COLOR_TRAZABILIDAD = '#8E44AD'
const COLOR_SUPERVISION = 'rgb(15, 118, 110)'
const COLOR_ASIGNAR = 'rgb(15, 118, 110)'

const SIN_SELECCION: GridRowSelectionModel = { type: 'include', ids: new Set() }

declare module '@mui/x-data-grid' {
  interface ToolbarPropsOverrides {
    seleccionados: number[]
    onDeseleccionar: () => void
    onAsignar: () => void
  }
}

interface ToolbarBandejaNotificacionAsignacionProps {
  seleccionados: number[]
  onDeseleccionar: () => void
  onAsignar: () => void
}

/**
 * Toolbar de la grilla (21/08/2026, Parte 1 — réplica mecánica del mismo patrón ya construido en
 * `ToolbarBandejaAsignacion` de `AsignacionBandejaPage.tsx`): contador "N seleccionados" + "Asignar"
 * (habilitado con 1+) + "Exportar seleccionados"/"Deseleccionar todo" a la izquierda (oculto sin
 * selección), controles estándar (columnas/filtro/exportar/buscar) a la derecha. Sin "Asociar al
 * grupo familiar" (esa acción no existe en esta bandeja).
 */
function ToolbarBandejaNotificacionAsignacion({ seleccionados, onDeseleccionar, onAsignar }: ToolbarBandejaNotificacionAsignacionProps) {
  const apiRef = useGridApiContext()
  const hay = seleccionados.length > 0

  return (
    <GridToolbarContainer>
      {hay && (
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
          <Chip label={`${seleccionados.length} seleccionado${seleccionados.length > 1 ? 's' : ''}`} size="small" color="primary" variant="outlined" />
          <Button size="small" variant="contained" onClick={onAsignar}>
            Asignar
          </Button>
          <Button size="small" onClick={() => apiRef.current.exportDataAsCsv({ getRowsToExport: () => seleccionados })}>
            Exportar seleccionados
          </Button>
          <Button size="small" onClick={onDeseleccionar}>
            Deseleccionar todo
          </Button>
        </Stack>
      )}
      <Box sx={{ flex: 1 }} />
      <DataGridStandardToolbarControls />
    </GridToolbarContainer>
  )
}

/** Adapta una fila (documento) de esta bandeja al tipo que espera el "Panel de datos" compartido. */
function adaptarParaPanelDatos(row: NotificacionAsignacionDocumento): ExpedienteBandeja {
  return {
    idExpediente: row.idExpediente,
    numeroExpediente: row.numeroExpediente,
    numeroTramiteDocumentario: '',
    numeroExpedienteSgd: row.numeroExpedienteSgd,
    etapaCodigo: 'NOTIFICACION',
    estadoCodigo: row.estadoExpedienteCodigo,
    abogadoInicial: '',
    responsableActual: row.usuarioNotificacionActual,
    equipoActual: '',
    fechaRecepcion: null,
    fechaRegistro: null,
    fechaUltimoMovimiento: null,
    fechaVencimiento: row.fechaVencimiento,
    requierePublicacion: false,
    expedienteDigitalCompleto: false,
    canal: '',
    procedimiento: '',
    tipoActa: '',
    numeroActa: '',
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
 * Fila real de la grilla (29/08/2026, agrupamiento por expediente — pedido explícito del usuario:
 * "carpetita con un + bien bonito... se despliegan los documentos que serán notificados... evita
 * datos redundantes entre padre (principal solicitud) e hijos (documentos)"). Nunca se blanquean
 * campos del documento subyacente (`NotificacionAsignacionDocumento` sigue completo) — el blanqueo es
 * puramente de PRESENTACIÓN vía `valueGetter` por columna, así una fila 'hijo' sigue teniendo datos
 * reales completos si se necesita abrir su propio workspace (`onRowDoubleClick`), sin depender de un
 * segundo campo "origen" paralelo.
 * - `padre`: fila sintética (no es un documento real, `filaId` negativo = `-idExpediente`) que
 *   representa el expediente — SIEMPRE, sin importar si tiene 1 o N documentos en el subconjunto de
 *   esta bandeja (30/08/2026, Parte 7, pedido explícito del usuario: "SI QUIERO QUE CAMBIES EL
 *   CRITERIO" — el ícono de expandir debe aparecer siempre, no solo con 2+ documentos, para poder ver
 *   el universo completo del expediente aunque solo haya 1 documento que califique para esta bandeja).
 *   Arma su identidad (numeroExpediente/numeroExpedienteSgd/titular) copiando el documento de
 *   referencia del grupo (`documentoReferencia`).
 * - `hijo`: fila con el universo COMPLETO de documentos del expediente (no solo los de esta bandeja),
 *   visible solo mientras el padre está expandido — blanquea en pantalla N° expediente/SGD/Titular
 *   (ya visibles en la fila padre, justo arriba).
 */
interface FilaSupervision extends NotificacionAsignacionDocumento {
  filaId: number
  // 30/08/2026, Parte 7 (pedido explícito del usuario: "SI QUIERO QUE CAMBIES EL CRITERIO" — el
  // ícono de expandir/nested grid debe aparecer SIEMPRE, incluso si el expediente solo tiene 1
  // documento que califica para esta bandeja): se elimina el tipo 'individual' — todo expediente se
  // renderiza como 'padre' (con ícono de expandir), sin importar cuántos documentos tenga en el
  // subconjunto propio de esta bandeja. La nested grid, al expandir, siempre trae el UNIVERSO
  // COMPLETO de documentos del expediente (vía `listarDocumentosAnalizados`, ver `documentosCompletos`
  // más abajo) — así se ve cualquier Carta Final "En despacho"/"Observado" aunque sea el único
  // documento de la fila.
  filaTipo: 'padre' | 'hijo'
  cantidadDocumentos: number
  /** Solo 'hijo' (30/08/2026, Parte 3 — "nested grid" real, no un Stack de campos apilados): UNA sola
   * fila 'hijo' por expediente expandido, con TODOS sus documentos (no solo los de esta bandeja) —
   * se renderiza como una mini-tabla propia (encabezado + una fila por documento) dentro de la celda
   * fusionada por `colSpan`, en vez de una fila por documento con campos "Etiqueta: valor" en línea. */
  documentosDelExpediente?: DocumentoAnalizado[]
}

/**
 * Elige qué documento del grupo representa al expediente en la fila 'padre' (29/08/2026, Parte 2, pedido
 * explícito del usuario: "Tipo documento y Resultado... variable dependiendo si lo envían desde
 * verificación... ejecución... validación"). Prioriza FINAL sobre INTERMEDIO (una carta final ya en camino
 * a Validación/Ejecución es más relevante que una intermedia) y, entre iguales, el más reciente por
 * `fechaDocumento`. "Resultado"/"Abogado"/"Fecha solicitud" son valores a nivel de EXPEDIENTE (mismos en
 * cualquier documento del grupo, ver `DocumentoAnalisisDAO#listarDocumentosAsignacionNotificacion`), así que
 * no dependen de esta elección — solo "Tipo documento" (y, de forma derivada, "Estado") sí.
 */
function documentoReferencia(docs: NotificacionAsignacionDocumento[]): NotificacionAsignacionDocumento {
  const ordenados = [...docs].sort((a, b) => {
    if (a.clasificacion !== b.clasificacion) {
      if (a.clasificacion === 'FINAL') return -1
      if (b.clasificacion === 'FINAL') return 1
    }
    return (b.fechaDocumento || '').localeCompare(a.fechaDocumento || '')
  })
  return ordenados[0]
}

/**
 * Envuelve el `sortComparator` de cada columna (mismo patrón que `agruparHijosBajoPadre` en
 * `RegistroBandejaPage.tsx`) para que una fila 'hijo', al ordenar por cabecera, "valga" el valor de
 * esa misma columna en su fila padre — así el orden estable de MUI mantiene cada grupo junto (el
 * padre siempre insertado justo antes de sus hijos, ver `filasAgrupadas`), sin importar qué columna
 * se esté ordenando.
 */
function agruparHijosBajoExpediente(comparadorOriginal?: GridComparatorFn<unknown>): GridComparatorFn<unknown> {
  return (v1, v2, params1, params2) => {
    const fila1 = params1.api.getRow(params1.id) as FilaSupervision | null
    const fila2 = params2.api.getRow(params2.id) as FilaSupervision | null
    const valor1 = fila1?.filaTipo === 'hijo' ? params1.api.getCellValue(-fila1.idExpediente, params1.field) : v1
    const valor2 = fila2?.filaTipo === 'hijo' ? params2.api.getCellValue(-fila2.idExpediente, params2.field) : v2
    if (comparadorOriginal) return comparadorOriginal(valor1, valor2, params1, params2)
    if (valor1 == null && valor2 == null) return 0
    if (valor1 == null) return -1
    if (valor2 == null) return 1
    if (valor1 < valor2) return -1
    if (valor1 > valor2) return 1
    return 0
  }
}

/**
 * Equivalente web de la 1ra pestaña de Notificación, renombrada de "Bandeja Asignación" a "Bandeja
 * de supervisión" (28/08/2026, pedido explícito del usuario, Parte 1 — el rol real de este panel es
 * la SUPERVISIÓN de notificaciones, no solo asignar). Ver Javadoc de
 * `DocumentoAnalisisDAO`/`NotificacionAsignacionService` (backend) para el alcance completo
 * investigado contra el código real de V2.
 *
 * Fila = DOCUMENTO (no expediente, a diferencia de Registro/Asignación/Análisis/Verificación/
 * Ejecución) — confirmado leyendo `AsignacionNotifTableModel` real de V2. Columnas, mismo orden:
 * N° expediente, N° expediente SGD, Clas. Documentos, Tipo documento, N° Documento, Fecha
 * Emisión, Titular, Estado (expediente), Estado doc. Sin columna "Asociados" propia — el ícono "+"
 * de expandir expedientes asociados SÍ existe en esta bandeja en V2 (única de las 4 con ese
 * mecanismo), diferido a un incremento siguiente para no expandir más el alcance ya grande de
 * este primero.
 *
 * 4 KPIs (mismo criterio que `actualizarMetricasAsigNotif`): Pendientes de asignación, Asignados,
 * Listos para firma, Intermedios.
 *
 * **Rediseño 28/08/2026, Parte 3, pedido explícito del usuario: 2 paneles totalmente separados**
 * (mismo criterio que ya usa `AsignacionBandejaPage.tsx` — doble clic vs botón "Asignar" abren
 * contenidos distintos, no la misma subpestaña forzada):
 * - **Doble clic** sobre cualquier fila → workspace `kind: 'individual'`, subpestañas "Datos" +
 *   **"Supervisión"** (`NotificacionSupervisionContent.tsx`) — el supervisor revisa el expediente y,
 *   si decide no enviarlo a Validación, puede emitir la carta final él mismo desde ahí. Sin "Destino
 *   operativo" (se retiró de este panel, ver más abajo).
 * - **Botón azul "Asignar"** (toolbar, habilitado con 1+ filas marcadas) → SIEMPRE abre el workspace
 *   fijo `kind: 'multiple'` (`NotificacionAsignacionMultipleContent.tsx`, ya genérico para cualquier
 *   cantidad de documentos/expedientes desde su construcción original — no hacía falta un componente
 *   nuevo) — único responsable de "Destino operativo" (derivar a `EQ_VALIDACION`/`EQ_NOTIFICACION`)
 *   en todo este módulo desde ahora. Se retiró la distinción "1 expediente → workspace individual
 *   forzado / 2+ → workspace múltiple" que existía antes de este rediseño (`handleAsignarSeleccion`
 *   ya no abre el workspace "individual" en ningún caso).
 *
 * **Emisión directa de cartas finales sin pasar por Validación** (Parte 2, confirmado explícitamente
 * por el usuario): ver Javadoc de `NotificacionSupervisionContent.tsx` — no requirió ningún cambio de
 * backend, `registrarFirmaDocumentoNotificacion` nunca exigió que el documento estuviera `VALIDADO`.
 */
export function NotificacionAsignacionBandejaPage() {
  const [documentos, setDocumentos] = useState<NotificacionAsignacionDocumento[]>([])
  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Panel de búsqueda (21/08/2026, mismo patrón de 3 filas ya usado en el resto de bandejas — se
  // había omitido por error en el primer incremento de este módulo). `listarBandejaAsignacionNotificacion`
  // no acepta parámetros, así que el filtrado es 100% en memoria sobre `documentos` ya cargado
  // completo (mismo criterio que `AsignacionCartasRespuestaPage.tsx`). "Fecha Emisión desde/hasta"
  // filtra sobre `fechaDocumento` (la misma columna "Fecha Emisión" ya visible en la grilla) — se
  // descartó `fechaVencimiento` (bug real, ver v3/CLAUDE.md: al ser una fecha típicamente FUTURA
  // para un expediente activo, el rango por defecto pasado dejaba la grilla vacía). "Estado" filtra
  // sobre `estadoDocumentoCodigo` (En despacho/Validado/Emitido/Observado), más granular y accionable
  // acá que el estado del expediente.
  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaMesAnteriorISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [estadoDocumentoFiltro, setEstadoDocumentoFiltro] = useState<FiltroEstadoDocumento>('')

  const [rowSelectionModel, setRowSelectionModel] = useState<GridRowSelectionModel>(SIN_SELECCION)

  type WorkspaceAbierto =
    | { kind: 'individual'; id: string; row: NotificacionAsignacionDocumento; color: string; activeSubTabId: string }
    | { kind: 'multiple'; id: string; color: string; activeSubTabId: string }
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [datosPorExpediente, setDatosPorExpediente] = useState<
    Record<number, { detalle: ExpedienteEdicionManual | null; cargando: boolean; error: string | null }>
  >({})

  const buscar = useCallback(() => {
    setLoading(true)
    setError(null)
    listarBandejaAsignacionNotificacion()
      .then(setDocumentos)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    buscar()
  }, [buscar])

  function coincideFiltroKpi(item: NotificacionAsignacionDocumento): boolean {
    switch (filtroKpi) {
      case 'PENDIENTES':
        return item.pendienteAsignacion
      case 'ASIGNADOS':
        return item.asignado
      case 'LISTOS_FIRMA':
        return item.listoParaFirma
      case 'INTERMEDIOS':
        return item.clasificacion === 'INTERMEDIO'
      default:
        return true
    }
  }

  const kpis = useMemo(() => {
    let pendientes = 0
    let asignados = 0
    let listosFirma = 0
    let intermedios = 0
    for (const item of documentos) {
      if (item.pendienteAsignacion) pendientes++
      if (item.asignado) asignados++
      if (item.listoParaFirma) listosFirma++
      if (item.clasificacion === 'INTERMEDIO') intermedios++
    }
    return { pendientes, asignados, listosFirma, intermedios }
  }, [documentos])

  function coincideBusqueda(item: NotificacionAsignacionDocumento): boolean {
    const textoNorm = texto.trim().toUpperCase()
    if (textoNorm) {
      const coincide = [item.numeroExpediente, item.numeroExpedienteSgd, item.titular, item.tipoDocumento, item.numeroDocumento]
        .some((v) => (v || '').toUpperCase().includes(textoNorm))
      if (!coincide) return false
    }
    if (estadoDocumentoFiltro && item.estadoDocumentoCodigo !== estadoDocumentoFiltro) return false
    // 21/08/2026, 2do bug real (distinto del anterior): el filtro de fecha apuntaba a
    // `fechaVencimiento`, un dato que para un expediente ACTIVO casi siempre es una fecha FUTURA (el
    // expediente aún no vence) — con el rango por defecto "01 del mes actual -> hoy" (pasado),
    // cualquier documento con vencimiento poblado pero futuro quedaba excluido igual, dejando la
    // grilla vacía aunque el dato no fuera nulo. Se cambió el filtro a `fechaDocumento` (Fecha
    // Emisión, la misma columna ya visible en la grilla) — coherente con un rango de fechas pasadas;
    // mismo criterio de "no excluir nulos" que ya documenta el CLAUDE.md raíz para esta columna en
    // V2 (un documento aún no emitido no debe desaparecer solo porque hay un filtro de fecha activo).
    if (item.fechaDocumento) {
      const fecha = item.fechaDocumento.slice(0, 10)
      if (desde && fecha < desde) return false
      if (hasta && fecha > hasta) return false
    }
    return true
  }

  const filas = useMemo(
    () => documentos.filter((d) => coincideFiltroKpi(d) && coincideBusqueda(d)),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [documentos, filtroKpi, texto, estadoDocumentoFiltro, desde, hasta],
  )

  function alternarFiltro(valor: FiltroKpi) {
    setFiltroKpi((prev) => (prev === valor ? null : valor))
  }

  function handleLimpiar() {
    setTexto('')
    setDesde(primerDiaMesAnteriorISO())
    setHasta(hoyISO())
    setEstadoDocumentoFiltro('')
  }

  // Mismo fix de sincronización ya aplicado a Análisis/Verificación/Ejecución (ver v3/CLAUDE.md).
  useEffect(() => {
    setWorkspacesAbiertos((prev) =>
      prev.map((w) => {
        if (w.kind !== 'individual') return w
        const fresco = documentos.find((d) => d.idDocumentoAnalizado === w.row.idDocumentoAnalizado)
        return fresco ? { ...w, row: fresco } : w
      }),
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [documentos])

  // Agrupamiento por expediente (29/08/2026, pedido explícito del usuario: "carpetita con un + bien
  // bonito... se despliegan los documentos que serán notificados... evita datos redundantes entre
  // padre (principal solicitud) e hijos (documentos)"). `filas` ya viene filtrada por KPI/búsqueda;
  // se agrupa DESPUÉS de filtrar, sobre lo que realmente se va a mostrar.
  const gruposPorExpediente = useMemo(() => {
    const mapa = new Map<number, NotificacionAsignacionDocumento[]>()
    for (const fila of filas) {
      const lista = mapa.get(fila.idExpediente)
      if (lista) lista.push(fila)
      else mapa.set(fila.idExpediente, [fila])
    }
    return mapa
  }, [filas])

  const [expedientesExpandidos, setExpedientesExpandidos] = useState<Set<number>>(new Set())

  function alternarExpansionExpediente(idExpediente: number) {
    setExpedientesExpandidos((prev) => {
      const next = new Set(prev)
      if (next.has(idExpediente)) next.delete(idExpediente)
      else next.add(idExpediente)
      return next
    })
  }

  // 29/08/2026, Parte 2 (pedido explícito del usuario: "en el caso de los hijos podriamos traer todos los
  // documentos que contiene el expediente"): a diferencia del primer agrupamiento (que solo mostraba los
  // documentos ya cargados en esta bandeja, un subconjunto filtrado por CONDICION_ASIGNACION_NOTIFICACION),
  // al expandir se pide el universo COMPLETO de documentos del expediente (mismo endpoint que usa la
  // subpestaña "Análisis", sin restricción de etapa) — se cachea por expediente para no repetir el fetch.
  const [documentosCompletos, setDocumentosCompletos] = useState<Record<number, DocumentoAnalizado[]>>({})
  const [cargandoCompleto, setCargandoCompleto] = useState<Set<number>>(new Set())

  useEffect(() => {
    for (const idExpediente of expedientesExpandidos) {
      if (documentosCompletos[idExpediente] || cargandoCompleto.has(idExpediente)) continue
      setCargandoCompleto((prev) => new Set(prev).add(idExpediente))
      listarDocumentosAnalizados(idExpediente)
        // 30/08/2026, pedido explícito del usuario: "en los documentos del nestedgrid solo deben
        // aparecer las cartas intermedias o finales" — excluye Informes/Resoluciones y cualquier otro
        // documento sin clasificación (`clasificacion=''`, ver `CLAUDE.md` raíz sección Análisis).
        .then((docs) => docs.filter((d) => d.clasificacion === 'INTERMEDIO' || d.clasificacion === 'FINAL'))
        .then((docs) => setDocumentosCompletos((prev) => ({ ...prev, [idExpediente]: docs })))
        .catch(() => setDocumentosCompletos((prev) => ({ ...prev, [idExpediente]: [] })))
        .finally(() =>
          setCargandoCompleto((prev) => {
            const next = new Set(prev)
            next.delete(idExpediente)
            return next
          }),
        )
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [expedientesExpandidos])

  // Filas realmente renderizadas por el `<DataGrid>` (30/08/2026, Parte 7): TODO expediente se
  // colapsa en una fila sintética `filaId` negativo (`-idExpediente`, nunca choca con un
  // `idDocumentoAnalizado` real), sin importar cuántos documentos tenga en el subconjunto de esta
  // bandeja (antes, con 1 solo documento, se saltaba directo a `filaTipo: 'individual'` sin ícono de
  // expandir — el usuario pidió explícitamente que el ícono aparezca siempre). Solo si está
  // expandido se inserta, debajo, una fila `hijo` con el universo COMPLETO de documentos del
  // expediente.
  const filasAgrupadas = useMemo<FilaSupervision[]>(() => {
    const resultado: FilaSupervision[] = []
    for (const [idExpediente, docs] of gruposPorExpediente) {
      const referencia = documentoReferencia(docs)
      resultado.push({ ...referencia, filaId: -idExpediente, filaTipo: 'padre', cantidadDocumentos: docs.length })
      if (expedientesExpandidos.has(idExpediente)) {
        // 30/08/2026, Parte 3: UNA sola fila 'hijo' por expediente expandido (no una por documento) —
        // lleva la lista completa para renderizarse como una mini-tabla propia ("nested grid").
        resultado.push({
          ...referencia,
          filaId: -idExpediente - 1_000_000,
          filaTipo: 'hijo',
          cantidadDocumentos: docs.length,
          documentosDelExpediente: documentosCompletos[idExpediente],
        })
      }
    }
    return resultado
  }, [gruposPorExpediente, expedientesExpandidos, documentosCompletos])

  // Mapa de "fila padre" -> los `idDocumentoAnalizado` reales que representa (siempre TODO su
  // grupo de esta bandeja, aunque sea 1 solo — Parte 7: ya no hay filas 'individual' con su propio
  // id de documento como filaId). Las filas `hijo` nunca son seleccionables por sí solas (ver
  // `isRowSelectable` del `<DataGrid>`), así que no aparecen acá.
  const documentosPorFilaId = useMemo(() => {
    const mapa = new Map<number, number[]>()
    for (const [idExpediente, docs] of gruposPorExpediente) {
      mapa.set(
        -idExpediente,
        docs.map((d) => d.idDocumentoAnalizado),
      )
    }
    return mapa
  }, [gruposPorExpediente])

  const idsSeleccionados = useMemo(() => {
    const resultado: number[] = []
    for (const [filaId, docIds] of documentosPorFilaId) {
      const marcado = rowSelectionModel.type === 'include' ? rowSelectionModel.ids.has(filaId) : !rowSelectionModel.ids.has(filaId)
      if (marcado) resultado.push(...docIds)
    }
    return resultado
  }, [documentosPorFilaId, rowSelectionModel])

  const documentosSeleccionados = useMemo(
    () => documentos.filter((d) => idsSeleccionados.includes(d.idDocumentoAnalizado)),
    [documentos, idsSeleccionados],
  )

  /** Doble clic sobre una fila, o "Asignar" con documentos de un solo expediente (`subTabId`/`forzar`). */
  function abrirWorkspace(row: NotificacionAsignacionDocumento, subTabId = 'datos', forzar = false) {
    const id = `ws-${row.idExpediente}`
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) {
        return prev.map((w) => (w.id === id ? { ...w, row, activeSubTabId: forzar ? subTabId : w.activeSubTabId } : w))
      }
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { kind: 'individual' as const, id, row, color, activeSubTabId: subTabId }]
    })
    if (datosPorExpediente[row.idExpediente]) return
    recargarDatosExpediente(row.idExpediente)
  }

  /** "Asignar" del toolbar con documentos de 2+ expedientes distintos — a lo sumo 1 instancia. */
  function abrirWorkspaceMultiple() {
    const id = 'ws-multiple'
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { kind: 'multiple' as const, id, color, activeSubTabId: 'asignacionMultiple' }]
    })
  }

  // 28/08/2026, pedido explícito del usuario (Parte 3): "Asignar" ahora SIEMPRE abre el panel
  // dedicado de asignación (workspace fijo `ws-multiple`), sin importar si la selección representa 1
  // o varios expedientes — `NotificacionAsignacionMultipleContent` ya soportaba cualquier cantidad.
  // El doble clic sigue abriendo el workspace "individual" (Datos/Supervisión), pero ya no comparte
  // ninguna subpestaña con "Asignar".
  function handleAsignarSeleccion() {
    if (documentosSeleccionados.length === 0) return
    abrirWorkspaceMultiple()
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
        if (w.kind === 'multiple') {
          const cantidadExpedientes = new Set(documentosSeleccionados.map((d) => d.idExpediente)).size
          const label =
            cantidadExpedientes === 1 && documentosSeleccionados.length > 0
              ? `Asignar — ${documentosSeleccionados[0].titular || documentosSeleccionados[0].numeroExpediente}`
              : `Asignar (${documentosSeleccionados.length} documento${documentosSeleccionados.length === 1 ? '' : 's'})`
          return {
            id: w.id,
            label,
            color: w.color,
            activeSubTabId: w.activeSubTabId,
            subTabs: [
              {
                id: 'asignacionMultiple',
                label: 'Asignar',
                color: COLOR_ASIGNAR,
                content: (
                  <NotificacionAsignacionMultipleContent
                    documentos={documentosSeleccionados}
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
                  onGuardado={() => recargarDatosExpediente(w.row.idExpediente)}
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
              id: 'supervision',
              label: 'Supervisión',
              color: COLOR_SUPERVISION,
              content: (
                <NotificacionSupervisionContent idExpediente={w.row.idExpediente} documentoFoco={w.row} onActualizado={buscar} />
              ),
            },
          ],
        }
      }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [workspacesAbiertos, datosPorExpediente, documentosSeleccionados],
  )

  // 29/08/2026, Parte 2 (pedido explícito del usuario): filas 'individual'/'padre' pasan al mismo
  // set de columnas que usa Bandeja Ejecución (Días/Expediente/N° SGD/Fecha solicitud/Tipo
  // documento/Resultado/Abogado/Titular/Estado) — "Tipo documento" ya no es la resolución+resultado
  // de análisis, sino el documento/resultado desde donde se está derivando (carta intermedia más
  // reciente si viene de Verificación, carta final más reciente si viene de Ejecución/Validación,
  // resuelto por `documentoReferencia` sobre el grupo); "Resultado"/"Abogado"/"Fecha solicitud" son
  // valores de EXPEDIENTE (iguales en cualquier documento del grupo, ya vienen resueltos del backend
  // — ver `DocumentoAnalisisDAO#listarDocumentosAsignacionNotificacion`), evitando así las columnas
  // vacías que mostraba el diseño anterior. Filas 'hijo' (documento real, expedido al expandir el
  // padre) usan columnas estilo panel de Análisis (Tipo/N° Documento/Estado/Fecha Emisión/Comentario/
  // ¿Requiere respuesta?, sin "Fecha Acuse") renderizadas dentro de una única celda fusionada
  // (`colSpan`, mismo mecanismo ya usado en `NotificacionNotificacionBandejaPage.tsx` para la fila de
  // intento) porque su conjunto de columnas es completamente distinto al de la fila padre.
  const columnas: GridColDef<FilaSupervision>[] = [
    {
      field: 'expandir',
      headerName: '',
      width: 56,
      sortable: false,
      filterable: false,
      disableColumnMenu: true,
      renderCell: (params) => {
        const fila = params.row
        if (fila.filaTipo !== 'padre') return null
        const expandido = expedientesExpandidos.has(fila.idExpediente)
        // 30/08/2026, Parte 7: el badge sigue contando los documentos de ESTA bandeja (puede ser 1),
        // pero al expandir siempre se trae el universo completo del expediente (ver `documentosCompletos`)
        // — el tooltip ya no promete "N documentos", para no confundir si el expediente trae más.
        return (
          <Tooltip title={expandido ? 'Colapsar documentos' : 'Ver documentos del expediente'}>
            <Badge
              badgeContent={fila.cantidadDocumentos}
              color="info"
              overlap="circular"
              sx={{ '& .MuiBadge-badge': { fontSize: '0.55rem', minWidth: 14, height: 14, padding: '0 2px' } }}
            >
              <IconButton
                size="small"
                className={DATA_GRID_ROW_ICON_FOLDER_CLASS}
                onClick={(e) => {
                  e.stopPropagation()
                  alternarExpansionExpediente(fila.idExpediente)
                }}
              >
                {expandido ? <FolderOpenOutlinedIcon fontSize="small" /> : <CreateNewFolderOutlinedIcon fontSize="small" />}
              </IconButton>
            </Badge>
          </Tooltip>
        )
      },
    },
    {
      field: 'diasRestantes',
      headerName: 'Días',
      width: 90,
      // Para 'hijo' la celda se fusiona con TODAS las columnas restantes (colSpan grande, mismo
      // patrón que la fila de intento de Bandeja Notificación) y renderiza el documento real con
      // columnas estilo Análisis — el resto de columnas de esta fila ya no se renderizan por separado.
      colSpan: (_value, row) => (row.filaTipo === 'hijo' ? 100 : undefined),
      renderCell: (params) => {
        const fila = params.row
        if (fila.filaTipo === 'hijo') {
          const documentos = fila.documentosDelExpediente
          // 30/08/2026, Parte 6 (pedido explícito del usuario, propuesta A aprobada: "tarjeta con
          // acento lateral") — la nested grid completa (carga/vacío/tabla) vive dentro de la misma
          // "tarjeta" (fondo `softGray`, borde izquierdo de acento en `info`, esquinas redondeadas
          // solo a la derecha) para que se lea como contenido ANIDADO dentro de la fila del
          // expediente, nunca al mismo nivel visual que la grilla principal.
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
                Documentos del expediente{documentos ? ` (${documentos.length})` : ''}
              </Typography>
              {!documentos ? (
                <Typography variant="caption" color="text.secondary">
                  Cargando documentos del expediente…
                </Typography>
              ) : documentos.length === 0 ? (
                <Typography variant="caption" color="text.secondary">
                  Este expediente no tiene documentos registrados.
                </Typography>
              ) : (
                // Tabla en tarjeta blanca propia (contraste contra el fondo gris de la tarjeta
                // exterior) — encabezado a propósito más chico/liviano/gris que el de la grilla
                // principal (`dataGridBaseSx`: negrita 700, `textPrimary`, sin mayúsculas), para que
                // nunca compita visualmente con la cabecera real de la bandeja. `tableLayout:'auto'`
                // (en vez de `width:'100%'`, que estiraba columnas angostas dejando huecos enormes)
                // deja que cada columna mida según su propio contenido, igual que una tabla HTML
                // nativa sin forzar. Casillas por documento puramente decorativas/bloqueadas —
                // la selección real para "Asignar" sigue siendo solo por fila de nivel superior
                // (individual/padre completo, ver `documentosPorFilaId`).
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
                        <TableCell padding="checkbox" sx={{ borderBottom: `1px solid ${sdrercColors.divider}` }} />
                        {['Tipo', 'N° Documento', 'Estado', 'Fecha Emisión', '¿Requiere respuesta?'].map((titulo) => (
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
                        ))}
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {documentos.map((doc) => {
                        const id = doc.idDocumentoAnalizado
                        return (
                          <TableRow key={id ?? `${doc.tipoDocumentoNombre}-${doc.numeroDocumento}`} hover>
                            <TableCell padding="checkbox">
                              <Checkbox size="small" checked={false} disabled />
                            </TableCell>
                            <TableCell sx={{ whiteSpace: 'nowrap' }}>{doc.tipoDocumentoNombre || '-'}</TableCell>
                            <TableCell sx={{ whiteSpace: 'nowrap' }}>{doc.numeroDocumento || '-'}</TableCell>
                            <TableCell sx={{ whiteSpace: 'nowrap' }}>{doc.estadoDocumentoNombre || '-'}</TableCell>
                            <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatearFecha(doc.fechaDocumento)}</TableCell>
                            <TableCell sx={{ whiteSpace: 'nowrap' }}>{doc.requiereRespuesta ? 'Sí' : 'No'}</TableCell>
                          </TableRow>
                        )
                      })}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Box>
          )
        }
        return (
          <StatusBadge
            label={params.value === null || params.value === undefined ? '-' : String(params.value)}
            tone={diasBadgeTone(params.value as number | null)}
          />
        )
      },
    },
    { field: 'numeroExpediente', headerName: 'Expediente', width: 190 },
    { field: 'numeroExpedienteSgd', headerName: 'N° expediente SGD', width: 160 },
    { field: 'fechaSolicitud', headerName: 'Fecha solicitud', width: 130, valueFormatter: (value) => formatearFecha(value as string | null) },
    { field: 'tipoDocumento', headerName: 'Tipo documento', width: 190 },
    { field: 'resultadoAnalisis', headerName: 'Resultado', width: 170, valueFormatter: (value) => (value as string) || '-' },
    { field: 'abogado', headerName: 'Abogado', width: 190, valueFormatter: (value) => (value as string) || '-' },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    {
      field: 'estadoExpediente',
      headerName: 'Estado',
      width: 170,
      // Mismo relabelado ya vigente (28/08/2026, Parte 2): `estado_expediente` en BD es siempre "Por
      // asignar" para todo lo que aparece en esta bandeja, sin distinción de BD entre "recién
      // llegado" y "ya supervisado" — se deriva 100% en la UI. Con `documentoReferencia` como base
      // del padre, esta misma lógica por-documento ya representa correctamente al grupo, sin
      // necesitar el caso especial "N documento(s)" que tenía el diseño anterior.
      valueGetter: (_value, row) => {
        if (row.clasificacion === 'INTERMEDIO') return 'Pendiente de asignar'
        return row.listoParaFirma || row.estadoDocumentoCodigo === 'EMITIDO' ? 'Supervisado' : 'Pendiente de supervisión'
      },
    },
    // 02/09/2026, pedido explícito del usuario: falta la columna "Estado doc." (estado real del
    // documento — En despacho/Validado/Observado/Emitido —, distinto del "Estado" derivado de arriba).
    { field: 'estadoDocumento', headerName: 'Estado doc.', width: 150, valueFormatter: (value) => (value as string) || '-' },
  ]

  // Mismo mecanismo de `agruparHijosBajoPadre` ya usado en `RegistroBandejaPage.tsx`: aplicado una
  // sola vez sobre TODAS las columnas para que ordenar por cualquier cabecera no desagrupe una fila
  // 'hijo' de su 'padre' (MUI reordena por valor crudo de celda si no se protege el comparador).
  const columnasConAgrupacion = useMemo(
    () => columnas.map((columna) => ({ ...columna, sortComparator: agruparHijosBajoExpediente(columna.sortComparator) })),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [expedientesExpandidos],
  )

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
        label="Estado doc."
        size="small"
        sx={{ minWidth: 180 }}
        value={estadoDocumentoFiltro}
        onChange={(e) => setEstadoDocumentoFiltro(e.target.value as FiltroEstadoDocumento)}
      >
        <MenuItem value="">Todos los estados</MenuItem>
        <MenuItem value="EN_DESPACHO">En despacho</MenuItem>
        <MenuItem value="VALIDADO">Validado</MenuItem>
        <MenuItem value="EMITIDO">Emitido</MenuItem>
        <MenuItem value="OBSERVADO">Observado</MenuItem>
      </TextField>
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Bandeja de supervisión"
      subtitulo="Documentos pendientes de supervisión, emisión y asignación a validadores/notificadores."
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
        <Box onClick={() => alternarFiltro('PENDIENTES')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Pendientes de asignación" value={kpis.pendientes} caption="Sin usuario de notificación" color={sdrercColors.warning} active={filtroKpi === 'PENDIENTES'} />
        </Box>
        <Box onClick={() => alternarFiltro('ASIGNADOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Asignados" value={kpis.asignados} caption="Con validador/notificador" color={sdrercColors.success} active={filtroKpi === 'ASIGNADOS'} />
        </Box>
        <Box onClick={() => alternarFiltro('LISTOS_FIRMA')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Listos para firma" value={kpis.listosFirma} caption="Final ya validado" color={sdrercColors.info} active={filtroKpi === 'LISTOS_FIRMA'} />
        </Box>
        <Box onClick={() => alternarFiltro('INTERMEDIOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Intermedios" value={kpis.intermedios} caption="Cartas/oficios intermedios" color={sdrercColors.info} active={filtroKpi === 'INTERMEDIOS'} />
        </Box>
      </Box>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2, mb: 3, overflow: 'hidden', boxShadow: '0 1px 3px rgba(16,24,40,0.08), 0 1px 2px rgba(16,24,40,0.04)' }}>
        <DataGrid
          rows={filasAgrupadas}
          columns={columnasConAgrupacion}
          getRowId={(row) => row.filaId}
          loading={loading}
          density="compact"
          autoHeight
          // 30/08/2026, Parte 3: la fila 'hijo' ahora renderiza una tabla anidada con 1 fila por
          // documento (altura variable) — 'auto' deja que cada fila mida lo que su contenido real
          // necesita; el resto de filas ('individual'/'padre') caen al alto por defecto de `density`.
          getRowHeight={(params) => ((params.model as FilaSupervision).filaTipo === 'hijo' ? 'auto' : undefined)}
          showToolbar
          checkboxSelection
          isRowSelectable={(params) => (params.row as FilaSupervision).filaTipo !== 'hijo'}
          // 30/08/2026, Parte 5 (pedido explícito del usuario: "esa casilla unica por expediente
          // debajo del padre es la que no quiero, debes quitarla"): `isRowSelectable=false` deja la
          // fila 'hijo' NO seleccionable, pero MUI igual dibuja una casilla nativa deshabilitada en su
          // columna de checkbox (comportamiento de fábrica, no oculta la celda) — de ahí la única
          // casilla suelta que aparecía debajo de cada padre expandido. Se oculta por CSS vía
          // `getRowClassName`, sin tocar la columna de checkbox en sí (que sigue funcionando normal
          // para 'individual'/'padre').
          getRowClassName={(params) => ((params.row as FilaSupervision).filaTipo === 'hijo' ? 'fila-hijo-notif-sin-checkbox' : '')}
          rowSelectionModel={rowSelectionModel}
          onRowSelectionModelChange={setRowSelectionModel}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          // 30/08/2026, Parte 7: como TODA fila no-hijo ahora es 'padre' (ver `filasAgrupadas`), el
          // doble clic ya no puede seguir usándose para alternar la expansión (dejaría de poder
          // abrirse el workspace desde cualquier fila) — se separa por completo: doble clic SIEMPRE
          // abre el workspace (Datos/Supervisión) sobre el documento de referencia del grupo; expandir
          // la nested grid queda exclusivamente a cargo del ícono de carpeta (con `stopPropagation`).
          onRowDoubleClick={(params) => {
            const fila = params.row as FilaSupervision
            if (fila.filaTipo === 'hijo') return
            abrirWorkspace(fila)
          }}
          slots={{ toolbar: ToolbarBandejaNotificacionAsignacion }}
          slotProps={{
            toolbar: {
              seleccionados: idsSeleccionados,
              onDeseleccionar: () => setRowSelectionModel(SIN_SELECCION),
              onAsignar: handleAsignarSeleccion,
            },
          }}
          sx={{
            ...dataGridBaseSx,
            '& .fila-hijo-notif-sin-checkbox .MuiDataGrid-cellCheckbox': { visibility: 'hidden' },
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
