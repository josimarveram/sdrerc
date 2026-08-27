import { useCallback, useEffect, useMemo, useState } from 'react'
import { Box, Button, IconButton, MenuItem, Stack, TextField, Tooltip, Typography } from '@mui/material'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import ToggleOnOutlinedIcon from '@mui/icons-material/ToggleOnOutlined'
import ToggleOffOutlinedIcon from '@mui/icons-material/ToggleOffOutlined'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import {
  agregarMiembro,
  buscarEquipos,
  cambiarActivoEquipo,
  crearEquipo,
  editarEquipo,
  listarAreasActivas,
  listarMiembros,
  listarSupervisoresAsignables,
  listarUsuariosAsignables,
  marcarResponsable,
  quitarMiembro,
  type AreaAdmin,
  type EquipoJuridicoAdmin,
  type EquipoMiembroAdmin,
  type UsuarioAsignableEquipo,
} from '../api/adminEquipoJuridicoApi'
import { ApiError } from '../api/http'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import { EquipoJuridicoAdminContent, equipoFormVacio, type EquipoFormState } from '../components/EquipoJuridicoAdminContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ADMIN_EQUIPO_JURIDICO_TABS } from '../layout/moduleTabs'

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#154775'
const COLOR_MIEMBROS = '#0B8E97'

interface WorkspaceAbierto {
  id: string
  idEquipo: number | null
  color: string
  activeSubTabId: string
}

/**
 * Equivalente web de Administración &gt; Equipo Jurídico, pestaña "Equipos" (V2:
 * `JPanelEquipoJuridicoV2`, grilla principal + panel "Miembros"). "Personal por supervisor" es una
 * página separada (`AdminPersonalPorSupervisorPage.tsx`), mismo criterio que V2 (2ª pestaña de
 * nivel superior del módulo, no una subpestaña del workspace).
 */
export function AdminEquipoJuridicoPage() {
  const [equipos, setEquipos] = useState<EquipoJuridicoAdmin[]>([])
  const [areas, setAreas] = useState<AreaAdmin[]>([])
  const [supervisoresAsignables, setSupervisoresAsignables] = useState<UsuarioAsignableEquipo[]>([])
  const [usuariosAsignables, setUsuariosAsignables] = useState<UsuarioAsignableEquipo[]>([])
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [texto, setTexto] = useState('')
  const [estadoFiltro, setEstadoFiltro] = useState<'' | 'true' | 'false'>('')
  const [idAreaFiltro, setIdAreaFiltro] = useState('')

  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [workspaceActivo, setWorkspaceActivo] = useState<string | null>(null)
  const [formularios, setFormularios] = useState<Record<string, EquipoFormState>>({})
  const [miembrosPorWorkspace, setMiembrosPorWorkspace] = useState<Record<string, EquipoMiembroAdmin[]>>({})
  const [usuarioParaAgregarPorWorkspace, setUsuarioParaAgregarPorWorkspace] = useState<Record<string, number | ''>>({})
  const [guardandoId, setGuardandoId] = useState<string | null>(null)
  const [mensajePorWorkspace, setMensajePorWorkspace] = useState<Record<string, string | null>>({})
  const [errorPorWorkspace, setErrorPorWorkspace] = useState<Record<string, string | null>>({})
  const [errorMiembrosPorWorkspace, setErrorMiembrosPorWorkspace] = useState<Record<string, string | null>>({})

  const buscar = useCallback(async () => {
    setCargando(true)
    setError(null)
    try {
      const data = await buscarEquipos({
        texto: texto || undefined,
        activo: estadoFiltro === '' ? undefined : estadoFiltro === 'true',
        idArea: idAreaFiltro ? Number(idAreaFiltro) : undefined,
      })
      setEquipos(data)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cargar el listado de equipos.')
    } finally {
      setCargando(false)
    }
  }, [texto, estadoFiltro, idAreaFiltro])

  useEffect(() => {
    buscar()
  }, [buscar])

  useEffect(() => {
    listarAreasActivas().then(setAreas).catch(() => setAreas([]))
    listarSupervisoresAsignables().then(setSupervisoresAsignables).catch(() => setSupervisoresAsignables([]))
    listarUsuariosAsignables().then(setUsuariosAsignables).catch(() => setUsuariosAsignables([]))
  }, [])

  function limpiar() {
    setTexto('')
    setEstadoFiltro('')
    setIdAreaFiltro('')
  }

  const kpis = useMemo(() => {
    const activos = equipos.filter((e) => e.activo).length
    return { total: equipos.length, activos, inactivos: equipos.length - activos }
  }, [equipos])

  function equipoFormDesdeDTO(e: EquipoJuridicoAdmin): EquipoFormState {
    return {
      idArea: e.idArea ?? '',
      codigo: e.codigo,
      nombre: e.nombre,
      descripcion: e.descripcion ?? '',
      activo: e.activo,
      idResponsable: e.idResponsable ?? '',
    }
  }

  async function cargarMiembros(workspaceId: string, idEquipo: number) {
    try {
      const data = await listarMiembros(idEquipo)
      setMiembrosPorWorkspace((prev) => ({ ...prev, [workspaceId]: data }))
    } catch {
      setMiembrosPorWorkspace((prev) => ({ ...prev, [workspaceId]: [] }))
    }
  }

  function abrirWorkspace(equipo: EquipoJuridicoAdmin | null) {
    const id = equipo ? `ws-${equipo.idEquipo}` : 'ws-nuevo'
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { id, idEquipo: equipo?.idEquipo ?? null, color, activeSubTabId: 'datos' }]
    })
    setFormularios((prev) => ({ ...prev, [id]: equipo ? equipoFormDesdeDTO(equipo) : equipoFormVacio() }))
    setWorkspaceActivo(id)
    if (equipo) {
      cargarMiembros(id, equipo.idEquipo)
    }
  }

  function cerrarWorkspace(id: string) {
    setWorkspacesAbiertos((prev) => {
      const idx = prev.findIndex((w) => w.id === id)
      const siguiente = prev.filter((w) => w.id !== id)
      if (workspaceActivo === id) {
        const nuevoActivo = siguiente[Math.min(idx, siguiente.length - 1)]
        setWorkspaceActivo(nuevoActivo ? nuevoActivo.id : null)
      }
      return siguiente
    })
  }

  function actualizarCampo<K extends keyof EquipoFormState>(workspaceId: string, campo: K, valor: EquipoFormState[K]) {
    setFormularios((prev) => ({ ...prev, [workspaceId]: { ...prev[workspaceId], [campo]: valor } }))
  }

  async function guardar(workspaceId: string) {
    const form = formularios[workspaceId]
    const ws = workspacesAbiertos.find((w) => w.id === workspaceId)
    if (!form || !ws) return
    setMensajePorWorkspace((prev) => ({ ...prev, [workspaceId]: null }))
    setErrorPorWorkspace((prev) => ({ ...prev, [workspaceId]: null }))
    setGuardandoId(workspaceId)
    try {
      const request = {
        idEquipo: ws.idEquipo,
        idArea: form.idArea === '' ? null : form.idArea,
        codigo: form.codigo,
        nombre: form.nombre,
        descripcion: form.descripcion || null,
        activo: form.activo,
        idResponsable: form.idResponsable === '' ? null : form.idResponsable,
      }
      const guardado = ws.idEquipo ? await editarEquipo(ws.idEquipo, request) : await crearEquipo(request)
      setMensajePorWorkspace((prev) => ({ ...prev, [workspaceId]: 'Equipo guardado correctamente.' }))
      setFormularios((prev) => ({ ...prev, [workspaceId]: equipoFormDesdeDTO(guardado) }))
      if (!ws.idEquipo) {
        setWorkspacesAbiertos((prev) => prev.map((w) => (w.id === workspaceId ? { ...w, idEquipo: guardado.idEquipo } : w)))
        cargarMiembros(workspaceId, guardado.idEquipo)
      }
      await buscar()
    } catch (err) {
      setErrorPorWorkspace((prev) => ({ ...prev, [workspaceId]: err instanceof ApiError ? err.message : 'No se pudo guardar el equipo.' }))
    } finally {
      setGuardandoId(null)
    }
  }

  async function handleAgregarMiembro(workspaceId: string, idEquipo: number) {
    const idUsuario = usuarioParaAgregarPorWorkspace[workspaceId]
    if (idUsuario === '' || idUsuario === undefined) return
    setErrorMiembrosPorWorkspace((prev) => ({ ...prev, [workspaceId]: null }))
    try {
      await agregarMiembro(idEquipo, idUsuario)
      setUsuarioParaAgregarPorWorkspace((prev) => ({ ...prev, [workspaceId]: '' }))
      await cargarMiembros(workspaceId, idEquipo)
      await buscar()
    } catch (err) {
      setErrorMiembrosPorWorkspace((prev) => ({ ...prev, [workspaceId]: err instanceof ApiError ? err.message : 'No se pudo agregar el miembro.' }))
    }
  }

  async function handleQuitarMiembro(workspaceId: string, idEquipo: number, idUsuario: number, nombre: string) {
    const confirmado = window.confirm(`¿Quitar a ${nombre} del equipo?`)
    if (!confirmado) return
    setErrorMiembrosPorWorkspace((prev) => ({ ...prev, [workspaceId]: null }))
    try {
      await quitarMiembro(idEquipo, idUsuario)
      await cargarMiembros(workspaceId, idEquipo)
      await buscar()
    } catch (err) {
      setErrorMiembrosPorWorkspace((prev) => ({ ...prev, [workspaceId]: err instanceof ApiError ? err.message : 'No se pudo quitar el miembro.' }))
    }
  }

  async function handleMarcarResponsable(workspaceId: string, idEquipo: number, idUsuario: number) {
    setErrorMiembrosPorWorkspace((prev) => ({ ...prev, [workspaceId]: null }))
    try {
      await marcarResponsable(idEquipo, idUsuario)
      await cargarMiembros(workspaceId, idEquipo)
      await buscar()
    } catch (err) {
      setErrorMiembrosPorWorkspace((prev) => ({ ...prev, [workspaceId]: err instanceof ApiError ? err.message : 'No se pudo marcar el responsable.' }))
    }
  }

  async function alternarActivo(equipo: EquipoJuridicoAdmin) {
    const confirmado = window.confirm(equipo.activo ? `¿Inactivar el equipo ${equipo.nombre}?` : `¿Activar el equipo ${equipo.nombre}?`)
    if (!confirmado) return
    try {
      await cambiarActivoEquipo(equipo.idEquipo, !equipo.activo)
      await buscar()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cambiar el estado del equipo.')
    }
  }

  const columnas: GridColDef<EquipoJuridicoAdmin>[] = [
    { field: 'codigo', headerName: 'Código', width: 160 },
    { field: 'nombre', headerName: 'Nombre', flex: 1, minWidth: 200 },
    { field: 'areaNombre', headerName: 'Área', width: 160 },
    { field: 'responsableNombre', headerName: 'Responsable', width: 200 },
    { field: 'miembrosActivos', headerName: 'Miembros', width: 100 },
    { field: 'abogadosActivos', headerName: 'Abogados', width: 100 },
    {
      field: 'activo',
      headerName: 'Estado',
      width: 110,
      renderCell: (params) => <StatusBadge label={params.row.activo ? 'Activo' : 'Inactivo'} tone={params.row.activo ? 'success' : 'neutral'} />,
    },
    {
      field: 'acciones',
      headerName: '',
      width: 60,
      sortable: false,
      filterable: false,
      renderCell: (params) => (
        <Tooltip title={params.row.activo ? 'Inactivar' : 'Activar'}>
          <IconButton size="small" onClick={() => alternarActivo(params.row)}>
            {params.row.activo ? <ToggleOnOutlinedIcon fontSize="small" color="success" /> : <ToggleOffOutlinedIcon fontSize="small" />}
          </IconButton>
        </Tooltip>
      ),
    },
  ]

  const workspaces: Workspace[] = workspacesAbiertos.map((w) => {
    const form = formularios[w.id] ?? equipoFormVacio()
    const esNuevo = w.idEquipo === null
    const commonProps = {
      esNuevo,
      form,
      areas,
      supervisoresAsignables,
      miembros: miembrosPorWorkspace[w.id] ?? [],
      usuariosAsignables,
      usuarioParaAgregar: usuarioParaAgregarPorWorkspace[w.id] ?? ('' as number | ''),
      guardando: guardandoId === w.id,
      mensaje: mensajePorWorkspace[w.id] ?? null,
      error: errorPorWorkspace[w.id] ?? null,
      errorMiembros: errorMiembrosPorWorkspace[w.id] ?? null,
      onCampo: <K extends keyof EquipoFormState>(campo: K, valor: EquipoFormState[K]) => actualizarCampo(w.id, campo, valor),
      onGuardar: () => guardar(w.id),
      onCancelar: () => cerrarWorkspace(w.id),
      onUsuarioParaAgregarChange: (idUsuario: number | '') => setUsuarioParaAgregarPorWorkspace((prev) => ({ ...prev, [w.id]: idUsuario })),
      onAgregarMiembro: () => w.idEquipo && handleAgregarMiembro(w.id, w.idEquipo),
      onQuitarMiembro: (idUsuario: number, nombre: string) => w.idEquipo && handleQuitarMiembro(w.id, w.idEquipo, idUsuario, nombre),
      onMarcarResponsable: (idUsuario: number) => w.idEquipo && handleMarcarResponsable(w.id, w.idEquipo, idUsuario),
    }
    return {
      id: w.id,
      label: esNuevo ? 'Nuevo equipo' : form.nombre || form.codigo,
      color: w.color,
      activeSubTabId: w.activeSubTabId,
      subTabs: [
        { id: 'datos', label: 'Datos', color: COLOR_DATOS, content: <EquipoJuridicoAdminContent seccion="datos" {...commonProps} /> },
        { id: 'miembros', label: 'Miembros', color: COLOR_MIEMBROS, content: <EquipoJuridicoAdminContent seccion="miembros" {...commonProps} /> },
      ],
    }
  })

  return (
    <PageHeaderSticky
      titulo="Equipo Jurídico"
      subtitulo="Administración de equipos, miembros y responsables."
      tabs={ADMIN_EQUIPO_JURIDICO_TABS}
      acciones={
        <Button variant="contained" startIcon={<AddCircleOutlineIcon />} onClick={() => abrirWorkspace(null)}>
          Nuevo equipo
        </Button>
      }
      filtros={
        <Stack direction="row" spacing={2} flexWrap="wrap" alignItems="center">
          <TextField label="Buscar" size="small" value={texto} onChange={(e) => setTexto(e.target.value)} sx={{ minWidth: 220 }} />
          <Button variant="contained" onClick={buscar}>
            Buscar
          </Button>
          <Button variant="outlined" onClick={limpiar}>
            Limpiar
          </Button>
          <TextField
            select
            label="Estado"
            size="small"
            value={estadoFiltro}
            onChange={(e) => setEstadoFiltro(e.target.value as '' | 'true' | 'false')}
            sx={{ minWidth: 150 }}
          >
            <MenuItem value="">Todos los estados</MenuItem>
            <MenuItem value="true">Activos</MenuItem>
            <MenuItem value="false">Inactivos</MenuItem>
          </TextField>
          <TextField select label="Área" size="small" value={idAreaFiltro} onChange={(e) => setIdAreaFiltro(e.target.value)} sx={{ minWidth: 180 }}>
            <MenuItem value="">Todas las áreas</MenuItem>
            {areas.map((a) => (
              <MenuItem key={a.idArea} value={String(a.idArea)}>
                {a.nombre}
              </MenuItem>
            ))}
          </TextField>
        </Stack>
      }
    >
      <Stack spacing={2}>
        {error && (
          <Typography color="error" variant="body2">
            {error}
          </Typography>
        )}
        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px,1fr))', gap: 2 }}>
          <MetricCard label="Total de equipos" value={kpis.total} caption="Según filtros activos" color={COLOR_DATOS} />
          <MetricCard label="Activos" value={kpis.activos} caption="Operativos" color="#1F9254" />
          <MetricCard label="Inactivos" value={kpis.inactivos} caption="Sin uso" color="#8A94A6" />
        </Box>
        <DataGrid
          rows={equipos}
          columns={columnas}
          getRowId={(row) => row.idEquipo}
          loading={cargando}
          onRowDoubleClick={(params) => abrirWorkspace(params.row)}
          autoHeight
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
        />
      </Stack>

      <WorkspaceTabsDrawer
        workspaces={workspaces}
        activeWorkspaceId={workspaceActivo}
        onSelectWorkspace={setWorkspaceActivo}
        onCloseWorkspace={cerrarWorkspace}
        onSelectSubTab={(workspaceId, subTabId) =>
          setWorkspacesAbiertos((prev) => prev.map((w) => (w.id === workspaceId ? { ...w, activeSubTabId: subTabId } : w)))
        }
      />
    </PageHeaderSticky>
  )
}
