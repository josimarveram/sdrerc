import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Chip, MenuItem, Stack, TextField } from '@mui/material'
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
import { Button } from '@mui/material'
import {
  listarBandejaAsignacionNotificacion,
  type NotificacionAsignacionDocumento,
} from '../api/notificacionAsignacionApi'
import { obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { primerDiaMesActualISO, hoyISO } from '../utils/fechas'
import { MetricCard } from '../components/MetricCard'
import { NotificacionAsignacionContent } from '../components/NotificacionAsignacionContent'
import { NotificacionAsignacionMultipleContent } from '../components/NotificacionAsignacionMultipleContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
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
const COLOR_ASIGNACION = 'rgb(15, 118, 110)'

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
      <GridToolbarColumnsButton />
      <GridToolbarFilterButton />
      <GridToolbarExport />
      <GridToolbarQuickFilter />
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
 * Equivalente web de la 1ra pestaña de Notificación, "Bandeja Asignación" (JPanelNotificacionV2,
 * `crearBandejaAsignacionNotificacion`) — 21/08/2026, primer incremento del módulo. Ver Javadoc de
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
 * **Selección múltiple + acción "Asignar" (21/08/2026, Parte 1, pedido explícito del usuario —
 * réplica mecánica del mismo patrón ya construido/corregido en `AsignacionBandejaPage.tsx`)**:
 * checkbox de selección en la grilla (fila = documento) + toolbar con contador "N seleccionados" +
 * botón "Asignar" (`handleAsignarSeleccion`). Decide el modo del panel por CANTIDAD DE EXPEDIENTES
 * DISTINTOS representados en la selección (no por cantidad de filas/documentos, ya que un mismo
 * expediente puede tener 2+ documentos seleccionados a la vez — ej. una carta final y su hoja de
 * envío): 1 expediente → abre/reutiliza el workspace "individual" de ese expediente (mismo que abre
 * el doble clic, `NotificacionAsignacionContent.tsx`, forzando la subpestaña "Asignación"); 2+
 * expedientes distintos → abre el workspace fijo "Selección múltiple (N expedientes)"
 * (`NotificacionAsignacionMultipleContent.tsx`, sin Historial, con mini-grilla de detección
 * automática Asignación inicial/Reasignación). Mismo criterio de "contenido siempre en vivo" que
 * `AsignacionBandejaPage.tsx` (nunca un snapshot de la selección).
 *
 * A diferencia de Asignación (que separa el workspace de doble clic — Datos/Asociar/Grupo Familiar —
 * del workspace de "Asignar" — Asignación/Historial —), acá ambos orígenes (doble clic y "Asignar"
 * con 1 solo expediente) abren el MISMO workspace `kind: 'individual'` con sus 2 subpestañas ya
 * existentes (Datos/Asignación) — no hay Asociar/Grupo Familiar en esta bandeja, así que no hace
 * falta separar los 2 orígenes en 2 tipos de workspace distintos como sí lo exige Asignación.
 *
 * Panel multipestañas — 2 subpestañas para el workspace "individual": "Datos" (idéntica a los demás
 * módulos, `permiteEdicion=false`) y "Asignación" (`NotificacionAsignacionContent.tsx`, ver su
 * Javadoc para el alcance real); 1 subpestaña única "Asignación" para el workspace "múltiple".
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
  const [desde, setDesde] = useState(primerDiaMesActualISO())
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
    setDesde(primerDiaMesActualISO())
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

  const idsSeleccionados = useMemo(() => {
    return filas
      .filter((f) =>
        rowSelectionModel.type === 'include'
          ? rowSelectionModel.ids.has(f.idDocumentoAnalizado)
          : !rowSelectionModel.ids.has(f.idDocumentoAnalizado),
      )
      .map((f) => f.idDocumentoAnalizado)
  }, [filas, rowSelectionModel])

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

  // 21/08/2026, Parte 1, pedido explícito del usuario (mismo criterio ya corregido en
  // AsignacionBandejaPage.tsx): decide por cantidad de EXPEDIENTES distintos representados en la
  // selección de documentos, no por cantidad de filas/documentos.
  function handleAsignarSeleccion() {
    if (documentosSeleccionados.length === 0) return
    const idsExpediente = new Set(documentosSeleccionados.map((d) => d.idExpediente))
    if (idsExpediente.size === 1) {
      abrirWorkspace(documentosSeleccionados[0], 'asignacion', true)
      return
    }
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
          return {
            id: w.id,
            label: `Selección múltiple (${cantidadExpedientes} expediente${cantidadExpedientes === 1 ? '' : 's'})`,
            color: w.color,
            activeSubTabId: w.activeSubTabId,
            subTabs: [
              {
                id: 'asignacionMultiple',
                label: 'Asignación',
                color: COLOR_ASIGNACION,
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
              id: 'asignacion',
              label: 'Asignación',
              color: COLOR_ASIGNACION,
              content: (
                <NotificacionAsignacionContent idExpediente={w.row.idExpediente} documentoFoco={w.row} onActualizado={buscar} />
              ),
            },
          ],
        }
      }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [workspacesAbiertos, datosPorExpediente, documentosSeleccionados],
  )

  const columnas: GridColDef<NotificacionAsignacionDocumento>[] = [
    { field: 'numeroExpediente', headerName: 'N° expediente', width: 190 },
    { field: 'numeroExpedienteSgd', headerName: 'N° expediente SGD', width: 160 },
    { field: 'clasificacion', headerName: 'Clas. Documentos', width: 130 },
    { field: 'tipoDocumento', headerName: 'Tipo documento', width: 190 },
    { field: 'numeroDocumento', headerName: 'N° Documento', width: 140 },
    { field: 'fechaDocumento', headerName: 'Fecha Emisión', width: 130, valueFormatter: (value) => formatearFecha(value as string | null) },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    {
      field: 'estadoExpediente',
      headerName: 'Estado',
      width: 160,
      // 22/08/2026, pedido explícito del usuario: `estado_expediente` en BD es siempre "Por asignar"
      // tanto para un documento FINAL recién llegado como para uno ya validado (ambos casos son el
      // mismo código `POR_ASIGNAR`, ver `CLAUDE.md` raíz — no hay estado de BD propio para "a la
      // espera de emitir"). Se relabela solo en la UI, reutilizando `listoParaFirma` (ya calculado en
      // el DTO como `FINAL && estadoDocumentoCodigo==='VALIDADO'`, la misma condición que abre la
      // Parte "Emitir" del panel individual): un documento ya validado y aún no emitido se muestra
      // como "Por emitir", no "Por asignar" (ese rótulo se reserva para cuando realmente falta asignar
      // a validador/notificador, incluyendo el caso ya EMITIDO que vuelve a ser una asignación real
      // hacia el equipo de Notificación).
      valueGetter: (_value, row) => (row.listoParaFirma ? 'Por emitir' : row.estadoExpediente),
    },
    { field: 'estadoDocumento', headerName: 'Estado doc.', width: 150 },
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
      titulo="Bandeja Asignación"
      subtitulo="Documentos pendientes de asignar a validadores/notificadores."
      tabs={NOTIFICACION_TABS}
      filtros={filtrosHeader}
    >
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 2, mb: 2 }}>
        <Box onClick={() => alternarFiltro('PENDIENTES')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Pendientes de asignación" value={kpis.pendientes} caption="Sin usuario de notificación" color={sdrercColors.warning} />
        </Box>
        <Box onClick={() => alternarFiltro('ASIGNADOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Asignados" value={kpis.asignados} caption="Con validador/notificador" color={sdrercColors.success} />
        </Box>
        <Box onClick={() => alternarFiltro('LISTOS_FIRMA')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Listos para firma" value={kpis.listosFirma} caption="Final ya validado" color={sdrercColors.info} />
        </Box>
        <Box onClick={() => alternarFiltro('INTERMEDIOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Intermedios" value={kpis.intermedios} caption="Cartas/oficios intermedios" color={sdrercColors.info} />
        </Box>
      </Box>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2, mb: 3 }}>
        <DataGrid
          rows={filas}
          columns={columnas}
          getRowId={(row) => row.idDocumentoAnalizado}
          loading={loading}
          density="compact"
          autoHeight
          showToolbar
          checkboxSelection
          rowSelectionModel={rowSelectionModel}
          onRowSelectionModelChange={setRowSelectionModel}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          onRowDoubleClick={(params) => abrirWorkspace(params.row as NotificacionAsignacionDocumento)}
          slots={{ toolbar: ToolbarBandejaNotificacionAsignacion }}
          slotProps={{
            toolbar: {
              seleccionados: idsSeleccionados,
              onDeseleccionar: () => setRowSelectionModel(SIN_SELECCION),
              onAsignar: handleAsignarSeleccion,
            },
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
