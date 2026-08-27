import { useCallback, useEffect, useMemo, useState } from 'react'
import { Box, Button, IconButton, MenuItem, Stack, TextField, Tooltip, Typography } from '@mui/material'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import ToggleOnOutlinedIcon from '@mui/icons-material/ToggleOnOutlined'
import ToggleOffOutlinedIcon from '@mui/icons-material/ToggleOffOutlined'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import {
  buscarRoles,
  cambiarActivoRol,
  crearRol,
  editarRol,
  guardarPermisosRol,
  listarPermisosDelRol,
  type PermisoAdmin,
  type RolAdmin,
} from '../api/adminRolesApi'
import { ApiError } from '../api/http'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import { RolAdminContent, rolFormVacio, type RolFormState } from '../components/RolAdminContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ADMIN_ROLES_TABS } from '../layout/moduleTabs'

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#154775'
const COLOR_PERMISOS = '#0B8E97'

interface WorkspaceAbierto {
  id: string
  idRol: number | null
  color: string
  activeSubTabId: string
}

/**
 * Equivalente web de Administración &gt; Roles (V2: `JPanelRolesV2`). Mismo patrón de
 * bandeja+workspace que Administración &gt; Usuarios. Ver Javadoc de `RolAdminContent.tsx` para la
 * mejora de diseño sobre V2 (Accordion por módulo en vez de tabla plana).
 */
export function AdminRolesPage() {
  const [roles, setRoles] = useState<RolAdmin[]>([])
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [texto, setTexto] = useState('')
  const [estadoFiltro, setEstadoFiltro] = useState<'' | 'true' | 'false'>('')

  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [workspaceActivo, setWorkspaceActivo] = useState<string | null>(null)
  const [formularios, setFormularios] = useState<Record<string, RolFormState>>({})
  const [permisosPorWorkspace, setPermisosPorWorkspace] = useState<Record<string, PermisoAdmin[]>>({})
  const [seleccionPermisos, setSeleccionPermisos] = useState<Record<string, number[]>>({})
  const [guardandoId, setGuardandoId] = useState<string | null>(null)
  const [guardandoPermisosId, setGuardandoPermisosId] = useState<string | null>(null)
  const [mensajePorWorkspace, setMensajePorWorkspace] = useState<Record<string, string | null>>({})
  const [errorPorWorkspace, setErrorPorWorkspace] = useState<Record<string, string | null>>({})
  const [mensajePermisosPorWorkspace, setMensajePermisosPorWorkspace] = useState<Record<string, string | null>>({})
  const [errorPermisosPorWorkspace, setErrorPermisosPorWorkspace] = useState<Record<string, string | null>>({})

  const buscar = useCallback(async () => {
    setCargando(true)
    setError(null)
    try {
      const data = await buscarRoles({
        texto: texto || undefined,
        activo: estadoFiltro === '' ? undefined : estadoFiltro === 'true',
      })
      setRoles(data)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cargar el listado de roles.')
    } finally {
      setCargando(false)
    }
  }, [texto, estadoFiltro])

  useEffect(() => {
    buscar()
  }, [buscar])

  function limpiar() {
    setTexto('')
    setEstadoFiltro('')
  }

  const kpis = useMemo(() => {
    const activos = roles.filter((r) => r.activo).length
    return { total: roles.length, activos, inactivos: roles.length - activos }
  }, [roles])

  function rolFormDesdeDTO(r: RolAdmin): RolFormState {
    return { codigo: r.codigo, nombre: r.nombre, descripcion: r.descripcion ?? '', activo: r.activo }
  }

  async function cargarPermisos(workspaceId: string, idRol: number) {
    try {
      const data = await listarPermisosDelRol(idRol)
      setPermisosPorWorkspace((prev) => ({ ...prev, [workspaceId]: data }))
      setSeleccionPermisos((prev) => ({ ...prev, [workspaceId]: data.filter((p) => p.asignado).map((p) => p.idPermiso) }))
    } catch {
      setPermisosPorWorkspace((prev) => ({ ...prev, [workspaceId]: [] }))
    }
  }

  function abrirWorkspace(rol: RolAdmin | null) {
    const id = rol ? `ws-${rol.idRol}` : 'ws-nuevo'
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { id, idRol: rol?.idRol ?? null, color, activeSubTabId: 'datos' }]
    })
    setFormularios((prev) => ({ ...prev, [id]: rol ? rolFormDesdeDTO(rol) : rolFormVacio() }))
    setWorkspaceActivo(id)
    if (rol) {
      cargarPermisos(id, rol.idRol)
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

  function actualizarCampo<K extends keyof RolFormState>(workspaceId: string, campo: K, valor: RolFormState[K]) {
    setFormularios((prev) => ({ ...prev, [workspaceId]: { ...prev[workspaceId], [campo]: valor } }))
  }

  function togglePermiso(workspaceId: string, idPermiso: number) {
    setSeleccionPermisos((prev) => {
      const actual = prev[workspaceId] ?? []
      const nuevo = actual.includes(idPermiso) ? actual.filter((x) => x !== idPermiso) : [...actual, idPermiso]
      return { ...prev, [workspaceId]: nuevo }
    })
  }

  function toggleModulo(workspaceId: string, idsModulo: number[], marcarTodos: boolean) {
    setSeleccionPermisos((prev) => {
      const actual = new Set(prev[workspaceId] ?? [])
      for (const id of idsModulo) {
        if (marcarTodos) actual.add(id)
        else actual.delete(id)
      }
      return { ...prev, [workspaceId]: [...actual] }
    })
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
        idRol: ws.idRol,
        codigo: form.codigo,
        nombre: form.nombre,
        descripcion: form.descripcion || null,
        activo: form.activo,
      }
      const guardado = ws.idRol ? await editarRol(ws.idRol, request) : await crearRol(request)
      setMensajePorWorkspace((prev) => ({ ...prev, [workspaceId]: 'Rol guardado correctamente.' }))
      setFormularios((prev) => ({ ...prev, [workspaceId]: rolFormDesdeDTO(guardado) }))
      if (!ws.idRol) {
        setWorkspacesAbiertos((prev) => prev.map((w) => (w.id === workspaceId ? { ...w, idRol: guardado.idRol } : w)))
        cargarPermisos(workspaceId, guardado.idRol)
      }
      await buscar()
    } catch (err) {
      setErrorPorWorkspace((prev) => ({ ...prev, [workspaceId]: err instanceof ApiError ? err.message : 'No se pudo guardar el rol.' }))
    } finally {
      setGuardandoId(null)
    }
  }

  async function guardarPermisos(workspaceId: string) {
    const ws = workspacesAbiertos.find((w) => w.id === workspaceId)
    if (!ws || !ws.idRol) return
    setMensajePermisosPorWorkspace((prev) => ({ ...prev, [workspaceId]: null }))
    setErrorPermisosPorWorkspace((prev) => ({ ...prev, [workspaceId]: null }))
    setGuardandoPermisosId(workspaceId)
    try {
      await guardarPermisosRol(ws.idRol, seleccionPermisos[workspaceId] ?? [])
      setMensajePermisosPorWorkspace((prev) => ({ ...prev, [workspaceId]: 'Permisos guardados correctamente.' }))
      await buscar()
    } catch (err) {
      setErrorPermisosPorWorkspace((prev) => ({
        ...prev,
        [workspaceId]: err instanceof ApiError ? err.message : 'No se pudo guardar los permisos.',
      }))
    } finally {
      setGuardandoPermisosId(null)
    }
  }

  async function alternarActivo(rol: RolAdmin) {
    const confirmado = window.confirm(rol.activo ? `¿Inactivar el rol ${rol.nombre}?` : `¿Activar el rol ${rol.nombre}?`)
    if (!confirmado) return
    try {
      await cambiarActivoRol(rol.idRol, !rol.activo)
      await buscar()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cambiar el estado del rol.')
    }
  }

  const columnas: GridColDef<RolAdmin>[] = [
    { field: 'codigo', headerName: 'Código', width: 180 },
    { field: 'nombre', headerName: 'Nombre', flex: 1, minWidth: 200 },
    { field: 'descripcion', headerName: 'Descripción', flex: 1, minWidth: 220 },
    { field: 'usuariosAsociados', headerName: 'Usuarios', width: 100 },
    { field: 'permisosAsociados', headerName: 'Permisos', width: 100 },
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
    const form = formularios[w.id] ?? rolFormVacio()
    const esNuevo = w.idRol === null
    const commonProps = {
      esNuevo,
      form,
      permisos: permisosPorWorkspace[w.id] ?? [],
      permisosSeleccionados: seleccionPermisos[w.id] ?? [],
      guardando: guardandoId === w.id,
      guardandoPermisos: guardandoPermisosId === w.id,
      mensaje: mensajePorWorkspace[w.id] ?? null,
      error: errorPorWorkspace[w.id] ?? null,
      mensajePermisos: mensajePermisosPorWorkspace[w.id] ?? null,
      errorPermisos: errorPermisosPorWorkspace[w.id] ?? null,
      onCampo: <K extends keyof RolFormState>(campo: K, valor: RolFormState[K]) => actualizarCampo(w.id, campo, valor),
      onTogglePermiso: (idPermiso: number) => togglePermiso(w.id, idPermiso),
      onToggleModulo: (idsModulo: number[], marcarTodos: boolean) => toggleModulo(w.id, idsModulo, marcarTodos),
      onGuardar: () => guardar(w.id),
      onCancelar: () => cerrarWorkspace(w.id),
      onGuardarPermisos: () => guardarPermisos(w.id),
    }
    return {
      id: w.id,
      label: esNuevo ? 'Nuevo rol' : form.nombre || form.codigo,
      color: w.color,
      activeSubTabId: w.activeSubTabId,
      subTabs: [
        { id: 'datos', label: 'Datos', color: COLOR_DATOS, content: <RolAdminContent seccion="datos" {...commonProps} /> },
        { id: 'permisos', label: 'Permisos del rol', color: COLOR_PERMISOS, content: <RolAdminContent seccion="permisos" {...commonProps} /> },
      ],
    }
  })

  return (
    <PageHeaderSticky
      titulo="Roles"
      subtitulo="Administración de roles y permisos por módulo/bandeja."
      tabs={ADMIN_ROLES_TABS}
      acciones={
        <Button variant="contained" startIcon={<AddCircleOutlineIcon />} onClick={() => abrirWorkspace(null)}>
          Nuevo rol
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
          <MetricCard label="Total de roles" value={kpis.total} caption="Según filtros activos" color={COLOR_DATOS} />
          <MetricCard label="Activos" value={kpis.activos} caption="Disponibles para asignar" color="#1F9254" />
          <MetricCard label="Inactivos" value={kpis.inactivos} caption="Sin uso" color="#8A94A6" />
        </Box>
        <DataGrid
          rows={roles}
          columns={columnas}
          getRowId={(row) => row.idRol}
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
