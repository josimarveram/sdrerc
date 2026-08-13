import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, CircularProgress, IconButton, MenuItem, Paper, Stack, TextField, Tooltip, Typography } from '@mui/material'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import RemoveCircleOutlineIcon from '@mui/icons-material/RemoveCircleOutlined'
import { DataGrid, type GridColDef, type GridColumnVisibilityModel } from '@mui/x-data-grid'
import { useNavigate } from 'react-router-dom'
import { buscarBandejaRegistro, obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import { listarConfirmados, type ExpedienteRelacionado } from '../api/expedienteRelacionadoApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, alertaBadgeTone, diasBadgeTone } from '../components/StatusBadge'
import { AsociarDuplicadosContent } from '../components/AsociarDuplicadosContent'
import { GrupoFamiliarContent } from '../components/GrupoFamiliarContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'

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

function primerDiaHaceMesesISO(meses: number): string {
  const hoy = new Date()
  const base = new Date(hoy.getFullYear(), hoy.getMonth() - meses, 1)
  return `${base.getFullYear()}-${String(base.getMonth() + 1).padStart(2, '0')}-01`
}

function hoyISO(): string {
  return new Date().toISOString().slice(0, 10)
}

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

/**
 * Equivalente web de la pestaña "Bandeja Registro" (V2). Alcance hasta ahora: listado + filtros +
 * 3 KPIs (calculados en memoria sobre la lista ya cargada, igual que
 * JPanelBandejaExpedientesNueva.actualizarMetricasAlertasRegistro/filtrarPorAlertaRegistro en V2
 * — no una consulta aparte), "Editar" (Edición manual, habilitado solo si el expediente sigue en
 * REGISTRO/REGISTRADO sin asignación) y expansión de expedientes asociados/duplicados (icono "+"
 * por fila, franja de color por grupo, fondo celeste, texto atenuado — mismo patrón visual que
 * `iconoExpansionRegistro`/`insertarAsociadosRegistro` de V2, adaptado a `DataGrid` intercalando
 * filas hijas sintéticas en vez de un `JTable` mutable). Fuente de los asociados:
 * `listarConfirmados` (expedientes ya confirmados como relacionados). Solo un expediente principal
 * expandido a la vez, igual que V2.
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
  const navigate = useNavigate()
  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaHaceMesesISO(5))
  const [hasta, setHasta] = useState(hoyISO())
  const [estado, setEstado] = useState('')
  const [limite, setLimite] = useState(200)
  const [expedientes, setExpedientes] = useState<ExpedienteBandeja[]>([])
  const [filtroAlerta, setFiltroAlerta] = useState<FiltroAlerta>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [columnVisibilityModel, setColumnVisibilityModel] = useState<GridColumnVisibilityModel>(
    COLUMNAS_OCULTAS_POR_DEFECTO,
  )
  // Expansión de filas asociadas (icono "+"): solo un expediente principal expandido a la vez,
  // igual que contraerTodosExceptoRegistro en V2.
  const [expandidoId, setExpandidoId] = useState<number | null>(null)
  const [asociadosPorPrincipal, setAsociadosPorPrincipal] = useState<Record<number, ExpedienteRelacionado[]>>({})
  const [cargandoExpansion, setCargandoExpansion] = useState<number | null>(null)

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
        limite,
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
    [texto, desde, hasta, estado, limite],
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
    setDatosPorExpediente((prev) => ({ ...prev, [row.idExpediente]: { detalle: null, cargando: true, error: null } }))
    obtenerDatosExpediente(row.idExpediente)
      .then((detalle) => setDatosPorExpediente((prev) => ({ ...prev, [row.idExpediente]: { detalle, cargando: false, error: null } })))
      .catch((e) =>
        setDatosPorExpediente((prev) => ({
          ...prev,
          [row.idExpediente]: {
            detalle: null,
            cargando: false,
            error: e instanceof ApiError ? e.message : 'No se pudo cargar el panel de datos.',
          },
        })),
      )
  }

  function cerrarWorkspace(id: string) {
    setWorkspacesAbiertos((prev) => {
      const restantes = prev.filter((w) => w.id !== id)
      setActiveWorkspaceId((actual) => (actual === id ? (restantes.length > 0 ? restantes[restantes.length - 1].id : null) : actual))
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
              content: <GrupoFamiliarContent idExpedientePrincipal={w.row.idExpediente} onAsociado={buscar} />,
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
    const desdeDefault = primerDiaHaceMesesISO(5)
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

  const kpis = useMemo(
    () => ({
      potencialDuplicado: expedientes.filter((e) => e.alertas === 'Potencial duplicado').length,
      posibleGrupoFamiliar: expedientes.filter((e) => e.alertas === 'Posible Grupo Familiar').length,
      grupoFamiliarConfirmado: expedientes.filter((e) => e.grupoFamiliarConfirmado).length,
    }),
    [expedientes],
  )

  const filas = useMemo(() => expedientes.filter(coincideAlertaRegistro), [expedientes, filtroAlerta])

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
            estadoCodigo: asoc.etapaEstado || 'Documento asociado',
            alertas: alertaAsociada(asoc),
            esFilaAsociada: true,
            idPrincipal: item.idExpediente,
          })
        }
      }
    }
    return resultado
  }, [filas, expandidoId, asociadosPorPrincipal])

  function alternarFiltro(valor: FiltroAlerta) {
    setFiltroAlerta((prev) => (prev === valor ? null : valor))
  }

  // Mismo orden exacto que crearTableModel(perfilRegistroRecepcion=true) en V2
  // (JPanelBandejaExpedientesNueva): Dias, Nro. Expediente, N° expediente SGD, Canal,
  // Fecha Solicitud, Fecha Vencimiento, Proc. Registral, Tipo Acta, Nro Acta, Titular, Estado, Alertas.
  const columnas: GridColDef<FilaBandeja>[] = [
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
        if (row.relacionesConfirmadasComoPrincipal <= 0) return null
        if (cargandoExpansion === row.idExpediente) return <CircularProgress size={16} sx={{ ml: 1 }} />
        const expandido = expandidoId === row.idExpediente
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
    { field: 'estadoCodigo', headerName: 'Estado', width: 130 },
    {
      field: 'alertas',
      headerName: 'Alertas',
      width: 170,
      renderCell: (params) => <StatusBadge label={params.value as string} tone={alertaBadgeTone(params.value as string)} />,
    },
    {
      field: 'editar',
      headerName: 'Editar',
      width: 90,
      sortable: false,
      filterable: false,
      renderCell: (params) => {
        const item = params.row
        if (item.esFilaAsociada) return null
        const editable = item.etapaCodigo === 'REGISTRO' && item.estadoCodigo === 'REGISTRADO' && !item.responsableActual
        return (
          <Button size="small" disabled={!editable} onClick={() => navigate(`/registro/editar/${item.idExpediente}`)}>
            Editar
          </Button>
        )
      },
    },
  ]

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4 }}>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h6" sx={{ color: sdrercColors.primary }}>
            Bandeja Registro
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Expedientes en etapa de Registro / Recepción.
          </Typography>
        </Box>
        <Stack direction="row" spacing={2}>
          <Button variant="outlined" onClick={() => navigate('/registro/carga-diaria')}>
            Carga diaria
          </Button>
          <Button variant="contained" onClick={() => navigate('/registro/manual')}>
            + Registro manual
          </Button>
        </Stack>
      </Stack>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* Buscador + KPIs compactados en un solo bloque (antes 3 filas de buscador + fila de KPIs
          por separado, ocupaban demasiada altura y empujaban la grilla muy abajo). */}
      <Paper elevation={1} sx={{ p: 2, mb: 2, borderRadius: 2 }}>
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
          <TextField
            label="Límite"
            type="number"
            size="small"
            sx={{ width: 100 }}
            value={limite}
            onChange={(e) => setLimite(Number(e.target.value) || 200)}
          />
        </Stack>

        {/* KPIs (filtro en memoria sobre la lista ya cargada, igual que V2) */}
        <Stack direction="row" spacing={2} sx={{ mt: 2, flexWrap: 'wrap', gap: 2 }}>
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
        </Stack>
      </Paper>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2 }}>
        <DataGrid
          rows={filasConHijos}
          columns={columnas}
          getRowId={(row) => row.idExpediente}
          loading={loading}
          density="compact"
          autoHeight
          disableRowSelectionOnClick
          showToolbar
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
    </Box>
  )
}
