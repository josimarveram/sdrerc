import { useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, MenuItem, Stack, TextField, Typography } from '@mui/material'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { listarCartasRespuestaPendientes, type AsignacionCartaRespuesta } from '../api/asignacionApi'
import { obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, diasBadgeTone } from '../components/StatusBadge'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
import { CartaRespuestaContent } from '../components/CartaRespuestaContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { primerDiaMesActualISO, hoyISO } from '../utils/fechas'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ASIGNACION_TABS } from '../layout/moduleTabs'

type FiltroEstado = '' | 'Pendiente de Respuesta' | 'Edicto Publicado'
type FiltroKpi = null | 'CON_ACUSE' | 'PENDIENTES' | 'PUBLICACION' | 'VENCIMIENTO' | 'LISTAS_PARA_ASIGNAR'

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#2370A8'
const COLOR_REGISTRAR = 'rgb(10, 118, 145)'

/** Adapta una fila de esta bandeja (documento) al tipo que espera el "Panel de datos" compartido con Registro. */
function adaptarParaPanelDatos(item: AsignacionCartaRespuesta): ExpedienteBandeja {
  return {
    idExpediente: item.idExpediente,
    numeroExpediente: item.numeroExpediente,
    numeroTramiteDocumentario: '',
    numeroExpedienteSgd: item.numeroExpedienteSgd,
    etapaCodigo: item.etapaCodigo,
    estadoCodigo: '',
    abogadoInicial: '',
    responsableActual: '',
    equipoActual: '',
    fechaRecepcion: null,
    fechaRegistro: null,
    fechaUltimoMovimiento: null,
    fechaVencimiento: item.fechaVencimiento,
    requierePublicacion: item.requierePublicacion,
    expedienteDigitalCompleto: false,
    canal: '',
    procedimiento: '',
    tipoActa: '',
    numeroActa: '',
    grupoFamiliar: '',
    alertas: '',
    titular: item.titular,
    relacionesConfirmadasComoPrincipal: 0,
    relacionadoComoHijo: false,
    grupoFamiliarConfirmado: false,
    diasRestantes: item.diasVencimiento !== null ? Number(item.diasVencimiento) : null,
  }
}

/**
 * Equivalente web de la sub-pestaña "Cartas de respuesta" de Asignación (V2, `bandejaCartasRespuestaModel`
 * + panel `crearCartasRespuesta()`/`crearDestinoOperativoCarta()` en `JPanelAsignacionV2`). 24/08/2026,
 * pedido explícito del usuario: completa el alcance de este incremento — hasta ahora la bandeja era
 * solo lectura (ver `v3/CLAUDE.md`), el botón "Registrar Asignación" (derivar el expediente de vuelta
 * a Análisis con la carta de respuesta ya vinculada) estaba diferido porque dependía de la gestión
 * jerárquica de documentos analizados, no construida en V3 hasta la Fase 4 (Análisis). Con esa pieza
 * ya construida, se completa el flujo real.
 *
 * Mismas 11 columnas/orden y 5 KPIs que el incremento anterior (solo lectura), MÁS: columna
 * "Respuesta" y KPI "Listas para asignar" — mejora explícita de V3 (sin equivalente en V2, que solo
 * expone esta información al abrir el detalle de una fila): permite ver de un vistazo, sin entrar a
 * cada carta, cuáles ya tienen su respuesta registrada y están listas para derivar.
 *
 * **Interacción**: doble clic abre el panel multipestañas (mismo patrón ya establecido en el resto
 * de V3, a diferencia de V2 que usa clic simple sobre un panel lateral fijo) con 2 subpestañas:
 * "Datos" (`PanelDatosExpediente`, mismo componente compartido con Registro/Análisis) y "Registrar
 * Asignación" (`CartaRespuestaContent.tsx`, ver su Javadoc para el detalle del flujo real).
 */
export function AsignacionCartasRespuestaPage() {
  const [items, setItems] = useState<AsignacionCartaRespuesta[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaMesActualISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [estado, setEstado] = useState<FiltroEstado>('')
  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)

  type WorkspaceAbierto = { id: string; carta: AsignacionCartaRespuesta; color: string; activeSubTabId: string }
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [datosPorExpediente, setDatosPorExpediente] = useState<
    Record<number, { detalle: ExpedienteEdicionManual | null; cargando: boolean; error: string | null }>
  >({})

  function cargar() {
    setLoading(true)
    setError(null)
    listarCartasRespuestaPendientes()
      .then(setItems)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    cargar()
  }, [])

  // Sincroniza en caliente el `carta` de cada workspace ya abierto al recargar la bandeja (mismo fix
  // ya aplicado en Análisis/Verificación/Ejecución/Notificación, ver v3/CLAUDE.md): sin esto,
  // "Registrar Asignación" exitoso dejaría el panel abierto mostrando datos desactualizados.
  useEffect(() => {
    setWorkspacesAbiertos((prev) =>
      prev.map((w) => {
        const fresco = items.find((i) => i.idDocumentoAnalizado === w.carta.idDocumentoAnalizado)
        return fresco ? { ...w, carta: fresco } : w
      }),
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [items])

  function handleBuscar() {
    setFiltroKpi(null)
    cargar()
  }

  function handleLimpiar() {
    setTexto('')
    setDesde(primerDiaMesActualISO())
    setHasta(hoyISO())
    setEstado('')
    setFiltroKpi(null)
  }

  // 25/08/2026, pedido explícito del usuario ("necesito que lo habilites para todos los kpis"):
  // mismo patrón ya usado en Bandeja Asignación/Carga Abogados — cada KPI (salvo "Cartas", que
  // representa el total sin filtrar, igual que "Total de expedientes" en otras bandejas) alterna un
  // filtro clicable sobre `filas`, con la MISMA condición exacta que ya usa el cálculo de `kpis`.
  function alternarFiltro(valor: FiltroKpi) {
    setFiltroKpi((prev) => (prev === valor ? null : valor))
  }

  function coincideFiltroKpi(item: AsignacionCartaRespuesta): boolean {
    switch (filtroKpi) {
      case 'CON_ACUSE':
        return Boolean(item.fechaAcuse)
      case 'PENDIENTES': {
        const confirmacion = item.confirmacionRespuesta.trim().toUpperCase()
        return confirmacion !== 'SI' && confirmacion !== 'NO'
      }
      case 'PUBLICACION':
        return item.requierePublicacion
      case 'VENCIMIENTO':
        return item.diasVencimiento !== null
      case 'LISTAS_PARA_ASIGNAR':
        return item.tieneRespuestaRegistrada
      default:
        return true
    }
  }

  const filas = useMemo(() => {
    const textoNorm = texto.trim().toUpperCase()
    return items.filter((item) => {
      if (textoNorm) {
        const coincide = [item.numeroExpediente, item.numeroExpedienteSgd, item.titular, item.tipoDocumentoNombre, item.numeroDocumento]
          .some((v) => v.toUpperCase().includes(textoNorm))
        if (!coincide) return false
      }
      if (estado && item.estadoCarta !== estado) return false
      // 25/08/2026, pedido explícito del usuario: filtro por Fecha Solicitud del expediente
      // (orientada al pasado, coherente con el default "01 del mes actual → hoy"), no por Fecha
      // Vencimiento de la carta (columna propia de la grilla, que sigue mostrándose sin cambios).
      if (item.fechaSolicitud) {
        if (desde && item.fechaSolicitud.slice(0, 10) < desde) return false
        if (hasta && item.fechaSolicitud.slice(0, 10) > hasta) return false
      }
      if (!coincideFiltroKpi(item)) return false
      return true
    })
  }, [items, texto, estado, desde, hasta, filtroKpi])

  const kpis = useMemo(() => {
    let conAcuse = 0
    let pendientes = 0
    let publicacion = 0
    let vencimiento = 0
    let listasParaAsignar = 0
    for (const item of items) {
      if (item.fechaAcuse) conAcuse++
      const confirmacion = item.confirmacionRespuesta.trim().toUpperCase()
      if (confirmacion !== 'SI' && confirmacion !== 'NO') pendientes++
      if (item.requierePublicacion) publicacion++
      if (item.diasVencimiento !== null) vencimiento++
      if (item.tieneRespuestaRegistrada) listasParaAsignar++
    }
    return { total: items.length, conAcuse, pendientes, publicacion, vencimiento, listasParaAsignar }
  }, [items])

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

  function abrirWorkspace(carta: AsignacionCartaRespuesta) {
    const id = `ws-carta-${carta.idDocumentoAnalizado}`
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { id, carta, color, activeSubTabId: 'datos' }]
    })
    if (!datosPorExpediente[carta.idExpediente]) {
      recargarDatosExpediente(carta.idExpediente)
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
        const estadoDatos = datosPorExpediente[w.carta.idExpediente]
        return {
          id: w.id,
          label: w.carta.titular || w.carta.numeroExpediente || `Expediente ${w.carta.idExpediente}`,
          color: w.color,
          activeSubTabId: w.activeSubTabId,
          subTabs: [
            {
              id: 'datos',
              label: 'Datos',
              color: COLOR_DATOS,
              content: (
                <PanelDatosExpediente
                  bandeja={adaptarParaPanelDatos(w.carta)}
                  detalle={estadoDatos?.detalle ?? null}
                  cargando={estadoDatos?.cargando ?? false}
                  error={estadoDatos?.error ?? null}
                  onGuardado={() => recargarDatosExpediente(w.carta.idExpediente)}
                />
              ),
            },
            {
              id: 'registrar-asignacion',
              label: 'Registrar Asignación',
              color: COLOR_REGISTRAR,
              content: <CartaRespuestaContent carta={w.carta} onActualizado={cargar} />,
            },
          ],
        }
      }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [workspacesAbiertos, datosPorExpediente],
  )

  // Mismo orden exacto que bandejaCartasRespuestaModel en V2 (JPanelAsignacionV2) + columna
  // "Respuesta" nueva (mejora V3, ver Javadoc del componente).
  const columnas: GridColDef<AsignacionCartaRespuesta>[] = [
    {
      field: 'diasVencimiento',
      headerName: 'Días',
      width: 90,
      renderCell: (params) => (
        <StatusBadge
          label={params.value === null || params.value === undefined ? '-' : String(params.value)}
          tone={diasBadgeTone(params.value as number | null)}
        />
      ),
    },
    {
      field: 'fechaVencimiento',
      headerName: 'Fecha Vencimiento',
      width: 140,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    { field: 'numeroExpediente', headerName: 'N° expediente', width: 190 },
    { field: 'numeroExpedienteSgd', headerName: 'N° expediente SGD', width: 160 },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    { field: 'tipoDocumentoNombre', headerName: 'Tipo documento', width: 170 },
    { field: 'numeroDocumento', headerName: 'N° Documento', width: 150 },
    {
      field: 'fechaAcuse',
      headerName: 'Fecha Acuse',
      width: 130,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    {
      field: 'fechaPublicacionNotificacion',
      headerName: 'Fecha Publ. Notif.',
      width: 150,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    {
      field: 'fechaPublicacionEdicto',
      headerName: 'Fecha Publ. Edicto',
      width: 150,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    { field: 'estadoCarta', headerName: 'Estado', width: 170 },
    {
      field: 'tieneRespuestaRegistrada',
      headerName: 'Respuesta',
      width: 130,
      renderCell: (params) => (
        <StatusBadge label={params.value ? 'Lista' : 'Pendiente'} tone={params.value ? 'success' : 'neutral'} />
      ),
    },
  ]

  const filtrosHeader = (
    <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', rowGap: 1.5 }}>
      <TextField
        label="Buscar (N° expediente, SGD, titular, documento...)"
        size="small"
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
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
      <TextField
        select
        label="Estado"
        size="small"
        sx={{ minWidth: 180 }}
        value={estado}
        onChange={(e) => setEstado(e.target.value as FiltroEstado)}
      >
        <MenuItem value="">Todos los estados</MenuItem>
        <MenuItem value="Pendiente de Respuesta">Pendiente de Respuesta</MenuItem>
        <MenuItem value="Edicto Publicado">Edicto Publicado</MenuItem>
      </TextField>
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Cartas de respuesta"
      subtitulo="Documentos analizados con respuesta del ciudadano pendiente, dentro de Asignación."
      tabs={ASIGNACION_TABS}
      filtros={filtrosHeader}
    >
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px,1fr))', gap: 2, mb: 2 }}>
        <MetricCard label="Cartas" value={kpis.total} caption="Con respuesta requerida" color={sdrercColors.info} />
        <Box onClick={() => alternarFiltro('CON_ACUSE')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Con acuse" value={kpis.conAcuse} caption="Listas para respuesta" color={sdrercColors.success} />
        </Box>
        <Box onClick={() => alternarFiltro('PENDIENTES')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Pendientes" value={kpis.pendientes} caption="Sin confirmación final" color={sdrercColors.warning} />
        </Box>
        <Box onClick={() => alternarFiltro('PUBLICACION')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Publicación" value={kpis.publicacion} caption="Preparadas para publicar" color={sdrercColors.primary} />
        </Box>
        <Box onClick={() => alternarFiltro('VENCIMIENTO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Vencimiento" value={kpis.vencimiento} caption="Plazo de respuesta o publicación activo" color={sdrercColors.warning} />
        </Box>
        <Box onClick={() => alternarFiltro('LISTAS_PARA_ASIGNAR')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Listas para asignar" value={kpis.listasParaAsignar} caption="Con carta de respuesta registrada" color={sdrercColors.success} />
        </Box>
      </Box>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2 }}>
        <DataGrid
          rows={filas}
          columns={columnas}
          getRowId={(row) => row.idDocumentoAnalizado}
          loading={loading}
          density="compact"
          autoHeight
          disableRowSelectionOnClick
          showToolbar
          pageSizeOptions={[25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 25 } } }}
          onRowDoubleClick={(params) => abrirWorkspace(params.row)}
        />
      </Box>

      <WorkspaceTabsDrawer
        workspaces={workspacesUI}
        activeWorkspaceId={activeWorkspaceId}
        onSelectWorkspace={setActiveWorkspaceId}
        onCloseWorkspace={cerrarWorkspace}
        onSelectSubTab={seleccionarSubTab}
      />

      {workspacesAbiertos.length === 0 && (
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
          Doble clic en una fila para ver sus datos y registrar la asignación de vuelta a Análisis.
        </Typography>
      )}
    </PageHeaderSticky>
  )
}
