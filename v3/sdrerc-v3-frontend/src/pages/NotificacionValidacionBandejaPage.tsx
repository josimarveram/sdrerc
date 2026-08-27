import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, Stack, TextField } from '@mui/material'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { listarBandejaValidacion, type NotificacionAsignacionDocumento } from '../api/notificacionValidacionApi'
import { obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { primerDiaMesActualISO, hoyISO } from '../utils/fechas'
import { MetricCard } from '../components/MetricCard'
import { NotificacionValidacionContent } from '../components/NotificacionValidacionContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { NOTIFICACION_TABS } from '../layout/moduleTabs'

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#2370A8'
const COLOR_VALIDAR = 'rgb(15, 118, 110)'

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
 * Equivalente web de la 2da pestaña de Notificación, "Bandeja Validación" (21/08/2026, pedido
 * explícito del usuario, investigación previa contra `JPanelNotificacionV2.crearBandejaValidacion`
 * real). A diferencia de Bandeja Asignación: SIN checkbox de selección múltiple ni ícono "+" de
 * expandir (V2 real: `SINGLE_SELECTION`, sin esos mecanismos) — solo doble clic abre el panel.
 *
 * Mismas 9 columnas y mismo orden que `AsignacionNotifTableModel`/`validacionModel` real de V2:
 * N° expediente, N° expediente SGD, Clas. Documentos, Tipo documento, N° Documento, Fecha Emisión,
 * Titular, Estado, Estado doc. (siempre `clasificacion='FINAL'`, `estadoDocumentoCodigo='EN_DESPACHO'`,
 * `estadoExpedienteCodigo='POR_VALIDAR'` — confirmado con `CONDICION_VALIDACION_NOTIFICACION`).
 *
 * 3 KPIs (mismo criterio que el header real de V2 — Pendientes/Por vencer/Vencidos, sobre
 * `diasRestantes`, ya que el KPI "Pendientes" es simplemente el total: todo lo que aparece acá está,
 * por definición, pendiente de validar). Buscador de texto + Fecha Emisión desde/hasta (sin combo de
 * Estado — V2 real solo tiene un ítem fijo "Pendientes de validar" en ese combo, sin valor real como
 * filtro, así que no se replicó). Filtrado 100% en memoria (mismo criterio que Bandeja Asignación).
 *
 * Panel multipestañas — 2 subpestañas: "Datos" (`permiteEdicion=false`) y "Validar"
 * (`NotificacionValidacionContent.tsx`, ver su Javadoc para el alcance real y la ausencia deliberada
 * de "Destino operativo").
 */
export function NotificacionValidacionBandejaPage() {
  const [documentos, setDocumentos] = useState<NotificacionAsignacionDocumento[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaMesActualISO())
  const [hasta, setHasta] = useState(hoyISO())

  type WorkspaceAbierto = { id: string; row: NotificacionAsignacionDocumento; color: string; activeSubTabId: string }
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [datosPorExpediente, setDatosPorExpediente] = useState<
    Record<number, { detalle: ExpedienteEdicionManual | null; cargando: boolean; error: string | null }>
  >({})

  const buscar = useCallback(() => {
    setLoading(true)
    setError(null)
    listarBandejaValidacion()
      .then(setDocumentos)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    buscar()
  }, [buscar])

  // Mismo fix de sincronización ya aplicado en el resto de bandejas de este módulo (ver v3/CLAUDE.md).
  useEffect(() => {
    setWorkspacesAbiertos((prev) =>
      prev.map((w) => {
        const fresco = documentos.find((d) => d.idDocumentoAnalizado === w.row.idDocumentoAnalizado)
        return fresco ? { ...w, row: fresco } : w
      }),
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [documentos])

  function handleLimpiar() {
    setTexto('')
    setDesde(primerDiaMesActualISO())
    setHasta(hoyISO())
  }

  const kpis = useMemo(() => {
    let porVencer = 0
    let vencidos = 0
    for (const item of documentos) {
      if (item.diasRestantes !== null) {
        if (item.diasRestantes < 0) vencidos++
        else if (item.diasRestantes <= 5) porVencer++
      }
    }
    return { pendientes: documentos.length, porVencer, vencidos }
  }, [documentos])

  const filas = useMemo(() => {
    const textoNorm = texto.trim().toUpperCase()
    return documentos.filter((item) => {
      if (textoNorm) {
        const coincide = [item.numeroExpediente, item.numeroExpedienteSgd, item.titular, item.tipoDocumento, item.numeroDocumento]
          .some((v) => (v || '').toUpperCase().includes(textoNorm))
        if (!coincide) return false
      }
      if (item.fechaDocumento) {
        const fecha = item.fechaDocumento.slice(0, 10)
        if (desde && fecha < desde) return false
        if (hasta && fecha > hasta) return false
      }
      return true
    })
  }, [documentos, texto, desde, hasta])

  function abrirWorkspace(row: NotificacionAsignacionDocumento) {
    const id = `ws-${row.idExpediente}`
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) {
        return prev.map((w) => (w.id === id ? { ...w, row } : w))
      }
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
                  onGuardado={() => recargarDatosExpediente(w.row.idExpediente)}
                />
              ),
            },
            {
              id: 'validar',
              label: 'Validar',
              color: COLOR_VALIDAR,
              content: <NotificacionValidacionContent idExpediente={w.row.idExpediente} documentoFoco={w.row} onActualizado={buscar} />,
            },
          ],
        }
      }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [workspacesAbiertos, datosPorExpediente],
  )

  const columnas: GridColDef<NotificacionAsignacionDocumento>[] = [
    { field: 'numeroExpediente', headerName: 'N° expediente', width: 190 },
    { field: 'numeroExpedienteSgd', headerName: 'N° expediente SGD', width: 160 },
    { field: 'clasificacion', headerName: 'Clas. Documentos', width: 130 },
    { field: 'tipoDocumento', headerName: 'Tipo documento', width: 190 },
    { field: 'numeroDocumento', headerName: 'N° Documento', width: 140 },
    { field: 'fechaDocumento', headerName: 'Fecha Emisión', width: 130, valueFormatter: (value) => formatearFecha(value as string | null) },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    { field: 'estadoExpediente', headerName: 'Estado', width: 160 },
    { field: 'estadoDocumento', headerName: 'Estado doc.', width: 150 },
    // 22/08/2026, pedido explícito del usuario: mostrar la cuenta de la persona registrada en
    // "Destino operativo" desde la Bandeja Asignación (`da.id_usuario_notificacion`, ya resuelto por
    // el backend como `usuarioNotificacionActual` — mismo campo que ya usa Bandeja Asignación).
    { field: 'usuarioNotificacionActual', headerName: 'Validador asignado', width: 200 },
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
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Bandeja Validación"
      subtitulo="Cartas finales pendientes de validar."
      tabs={NOTIFICACION_TABS}
      filtros={filtrosHeader}
    >
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 2, mb: 2 }}>
        <MetricCard label="Pendientes" value={kpis.pendientes} caption="Para validar" color={sdrercColors.info} />
        <MetricCard label="Por vencer" value={kpis.porVencer} caption="0 a 5 días hábiles" color={sdrercColors.warning} />
        <MetricCard label="Vencidos" value={kpis.vencidos} caption="Plazo excedido" color={sdrercColors.error} />
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
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          onRowDoubleClick={(params) => abrirWorkspace(params.row as NotificacionAsignacionDocumento)}
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
