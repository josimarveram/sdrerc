import { useCallback, useEffect, useMemo, useState } from 'react'
import { Box, Button, IconButton, MenuItem, Stack, TextField, Tooltip, Typography } from '@mui/material'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import KeyOutlinedIcon from '@mui/icons-material/KeyOutlined'
import ToggleOnOutlinedIcon from '@mui/icons-material/ToggleOnOutlined'
import ToggleOffOutlinedIcon from '@mui/icons-material/ToggleOffOutlined'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import {
  buscarUsuarios,
  cambiarActivoUsuario,
  crearUsuario,
  editarUsuario,
  listarEquiposActivos,
  listarRolesActivos,
  type EquipoAsignacion,
  type RolResumen,
  type UsuarioAdmin,
} from '../api/adminUsuariosApi'
import { ApiError } from '../api/http'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import { DlgRestablecerClave } from '../components/DlgRestablecerClave'
import { UsuarioAdminContent, usuarioFormVacio, type UsuarioFormState } from '../components/UsuarioAdminContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ADMIN_USUARIOS_TABS } from '../layout/moduleTabs'

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#154775'
const COLOR_ROLES = '#0B8E97'

interface WorkspaceAbierto {
  id: string
  idUsuario: number | null
  color: string
  activeSubTabId: string
}

/**
 * Equivalente web de Administración &gt; Usuarios (V2: `JPanelUsuariosV2` + `DlgRestablecerClaveV2`).
 * Mismo patrón de bandeja+workspace ya usado en el resto de V3 (buscador de 3 filas, `DataGrid`,
 * `WorkspaceTabsDrawer` con subpestañas), no un CRUD aislado nuevo. Ver Javadoc de
 * `UsuarioAdminDTO`/`UsuarioAdminContent.tsx` para las 2 diferencias deliberadas de diseño respecto
 * a V2 (nombre completo único, multi-equipo).
 */
export function AdminUsuariosPage() {
  const [usuarios, setUsuarios] = useState<UsuarioAdmin[]>([])
  const [roles, setRoles] = useState<RolResumen[]>([])
  const [equipos, setEquipos] = useState<EquipoAsignacion[]>([])
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [texto, setTexto] = useState('')
  const [estadoFiltro, setEstadoFiltro] = useState<'' | 'true' | 'false'>('')
  const [idRolFiltro, setIdRolFiltro] = useState('')
  const [idEquipoFiltro, setIdEquipoFiltro] = useState('')

  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [workspaceActivo, setWorkspaceActivo] = useState<string | null>(null)
  const [formularios, setFormularios] = useState<Record<string, UsuarioFormState>>({})
  const [guardandoId, setGuardandoId] = useState<string | null>(null)
  const [mensajePorWorkspace, setMensajePorWorkspace] = useState<Record<string, string | null>>({})
  const [errorPorWorkspace, setErrorPorWorkspace] = useState<Record<string, string | null>>({})

  const [restablecerClaveUsuario, setRestablecerClaveUsuario] = useState<UsuarioAdmin | null>(null)

  const buscar = useCallback(async () => {
    setCargando(true)
    setError(null)
    try {
      const data = await buscarUsuarios({
        texto: texto || undefined,
        activo: estadoFiltro === '' ? undefined : estadoFiltro === 'true',
        idRol: idRolFiltro ? Number(idRolFiltro) : undefined,
        idEquipo: idEquipoFiltro ? Number(idEquipoFiltro) : undefined,
      })
      setUsuarios(data)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cargar el listado de usuarios.')
    } finally {
      setCargando(false)
    }
  }, [texto, estadoFiltro, idRolFiltro, idEquipoFiltro])

  useEffect(() => {
    buscar()
  }, [buscar])

  useEffect(() => {
    listarRolesActivos().then(setRoles).catch(() => setRoles([]))
    listarEquiposActivos().then(setEquipos).catch(() => setEquipos([]))
  }, [])

  function limpiar() {
    setTexto('')
    setEstadoFiltro('')
    setIdRolFiltro('')
    setIdEquipoFiltro('')
  }

  const kpis = useMemo(() => {
    const activos = usuarios.filter((u) => u.activo).length
    return { total: usuarios.length, activos, inactivos: usuarios.length - activos }
  }, [usuarios])

  function usuarioFormDesdeDTO(u: UsuarioAdmin): UsuarioFormState {
    return {
      username: u.username,
      nombreCompleto: u.nombreCompleto,
      tipoDocumento: u.tipoDocumento ?? '',
      numeroDocumento: u.numeroDocumento ?? '',
      correo: u.correo ?? '',
      telefono: u.telefono ?? '',
      activo: u.activo,
      idsRoles: u.idsRoles,
      idsEquipos: u.idsEquipos,
    }
  }

  function abrirWorkspace(usuario: UsuarioAdmin | null) {
    const id = usuario ? `ws-${usuario.idUsuario}` : 'ws-nuevo'
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { id, idUsuario: usuario?.idUsuario ?? null, color, activeSubTabId: 'datos' }]
    })
    setFormularios((prev) => ({
      ...prev,
      [id]: usuario ? usuarioFormDesdeDTO(usuario) : usuarioFormVacio(),
    }))
    setWorkspaceActivo(id)
  }

  function cerrarWorkspace(id: string) {
    setWorkspacesAbiertos((prev) => {
      const idx = prev.findIndex((w) => w.id === id);
      const siguiente = prev.filter((w) => w.id !== id)
      if (workspaceActivo === id) {
        const nuevoActivo = siguiente[Math.min(idx, siguiente.length - 1)]
        setWorkspaceActivo(nuevoActivo ? nuevoActivo.id : null)
      }
      return siguiente
    })
  }

  function actualizarCampo<K extends keyof UsuarioFormState>(workspaceId: string, campo: K, valor: UsuarioFormState[K]) {
    setFormularios((prev) => ({ ...prev, [workspaceId]: { ...prev[workspaceId], [campo]: valor } }))
  }

  function toggleEnLista(lista: number[], id: number): number[] {
    return lista.includes(id) ? lista.filter((x) => x !== id) : [...lista, id]
  }

  function toggleRol(workspaceId: string, idRol: number) {
    setFormularios((prev) => ({
      ...prev,
      [workspaceId]: { ...prev[workspaceId], idsRoles: toggleEnLista(prev[workspaceId].idsRoles, idRol) },
    }))
  }

  function toggleEquipo(workspaceId: string, idEquipo: number) {
    setFormularios((prev) => ({
      ...prev,
      [workspaceId]: { ...prev[workspaceId], idsEquipos: toggleEnLista(prev[workspaceId].idsEquipos, idEquipo) },
    }))
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
        idUsuario: ws.idUsuario,
        username: form.username,
        nombreCompleto: form.nombreCompleto,
        tipoDocumento: form.tipoDocumento || null,
        numeroDocumento: form.numeroDocumento || null,
        correo: form.correo || null,
        telefono: form.telefono || null,
        activo: form.activo,
        idsRoles: form.idsRoles,
        idsEquipos: form.idsEquipos,
      }
      const guardado = ws.idUsuario ? await editarUsuario(ws.idUsuario, request) : await crearUsuario(request)
      setMensajePorWorkspace((prev) => ({ ...prev, [workspaceId]: 'Usuario guardado correctamente.' }))
      setFormularios((prev) => ({ ...prev, [workspaceId]: usuarioFormDesdeDTO(guardado) }))
      if (!ws.idUsuario) {
        // Alta recién creada: el workspace "ws-nuevo" pasa a representar al usuario ya persistido,
        // para que un 2° "Guardar" en la misma pestaña edite en vez de crear un duplicado.
        setWorkspacesAbiertos((prev) =>
          prev.map((w) => (w.id === workspaceId ? { ...w, idUsuario: guardado.idUsuario } : w)),
        )
      }
      await buscar()
    } catch (err) {
      setErrorPorWorkspace((prev) => ({
        ...prev,
        [workspaceId]: err instanceof ApiError ? err.message : 'No se pudo guardar el usuario.',
      }))
    } finally {
      setGuardandoId(null)
    }
  }

  async function alternarActivo(usuario: UsuarioAdmin) {
    const confirmado = window.confirm(
      usuario.activo ? `¿Inactivar al usuario ${usuario.username}?` : `¿Activar al usuario ${usuario.username}?`,
    )
    if (!confirmado) return
    try {
      await cambiarActivoUsuario(usuario.idUsuario, !usuario.activo)
      await buscar()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cambiar el estado del usuario.')
    }
  }

  const columnas: GridColDef<UsuarioAdmin>[] = [
    { field: 'username', headerName: 'Usuario', width: 130 },
    { field: 'nombreCompleto', headerName: 'Nombre completo', flex: 1, minWidth: 220 },
    {
      field: 'documento',
      headerName: 'Documento',
      width: 150,
      valueGetter: (_v, row) => [row.tipoDocumento, row.numeroDocumento].filter(Boolean).join(' '),
    },
    { field: 'correo', headerName: 'Correo', width: 200 },
    { field: 'telefono', headerName: 'Teléfono', width: 130 },
    { field: 'rolesResumen', headerName: 'Roles', flex: 1, minWidth: 180 },
    { field: 'equiposResumen', headerName: 'Equipos', flex: 1, minWidth: 180 },
    {
      field: 'activo',
      headerName: 'Estado',
      width: 110,
      renderCell: (params) => <StatusBadge label={params.row.activo ? 'Activo' : 'Inactivo'} tone={params.row.activo ? 'success' : 'neutral'} />,
    },
    {
      field: 'acciones',
      headerName: '',
      width: 90,
      sortable: false,
      filterable: false,
      renderCell: (params) => (
        <Stack direction="row" spacing={0.5}>
          <Tooltip title="Restablecer clave">
            <IconButton size="small" onClick={() => setRestablecerClaveUsuario(params.row)}>
              <KeyOutlinedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title={params.row.activo ? 'Inactivar' : 'Activar'}>
            <IconButton size="small" onClick={() => alternarActivo(params.row)}>
              {params.row.activo ? <ToggleOnOutlinedIcon fontSize="small" color="success" /> : <ToggleOffOutlinedIcon fontSize="small" />}
            </IconButton>
          </Tooltip>
        </Stack>
      ),
    },
  ]

  const workspaces: Workspace[] = workspacesAbiertos.map((w) => {
    const form = formularios[w.id] ?? usuarioFormVacio()
    const esNuevo = w.idUsuario === null
    const commonProps = {
      form,
      esNuevo,
      roles,
      equipos,
      guardando: guardandoId === w.id,
      mensaje: mensajePorWorkspace[w.id] ?? null,
      error: errorPorWorkspace[w.id] ?? null,
      onCampo: <K extends keyof UsuarioFormState>(campo: K, valor: UsuarioFormState[K]) => actualizarCampo(w.id, campo, valor),
      onToggleRol: (idRol: number) => toggleRol(w.id, idRol),
      onToggleEquipo: (idEquipo: number) => toggleEquipo(w.id, idEquipo),
      onGuardar: () => guardar(w.id),
      onCancelar: () => cerrarWorkspace(w.id),
    }
    return {
      id: w.id,
      label: esNuevo ? 'Nuevo usuario' : form.nombreCompleto || form.username,
      color: w.color,
      activeSubTabId: w.activeSubTabId,
      subTabs: [
        { id: 'datos', label: 'Datos', color: COLOR_DATOS, content: <UsuarioAdminContent seccion="datos" {...commonProps} /> },
        { id: 'roles', label: 'Roles y equipos', color: COLOR_ROLES, content: <UsuarioAdminContent seccion="roles" {...commonProps} /> },
      ],
    }
  })

  return (
    <PageHeaderSticky
      titulo="Usuarios"
      subtitulo="Administración de usuarios, roles y equipos."
      tabs={ADMIN_USUARIOS_TABS}
      acciones={
        <Button variant="contained" startIcon={<AddCircleOutlineIcon />} onClick={() => abrirWorkspace(null)}>
          Nuevo usuario
        </Button>
      }
      filtros={
        <Stack direction="row" spacing={2} flexWrap="wrap" alignItems="center">
          <TextField
            label="Buscar"
            size="small"
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            sx={{ minWidth: 220 }}
          />
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
          <TextField
            select
            label="Rol"
            size="small"
            value={idRolFiltro}
            onChange={(e) => setIdRolFiltro(e.target.value)}
            sx={{ minWidth: 180 }}
          >
            <MenuItem value="">Todos los roles</MenuItem>
            {roles.map((r) => (
              <MenuItem key={r.idRol} value={String(r.idRol)}>
                {r.nombre}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Equipo"
            size="small"
            value={idEquipoFiltro}
            onChange={(e) => setIdEquipoFiltro(e.target.value)}
            sx={{ minWidth: 180 }}
          >
            <MenuItem value="">Todos los equipos</MenuItem>
            {equipos.map((eq) => (
              <MenuItem key={eq.idEquipo} value={String(eq.idEquipo)}>
                {eq.displayName}
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
          <MetricCard label="Total de usuarios" value={kpis.total} caption="Según filtros activos" color={COLOR_DATOS} />
          <MetricCard label="Activos" value={kpis.activos} caption="Pueden iniciar sesión" color="#1F9254" />
          <MetricCard label="Inactivos" value={kpis.inactivos} caption="Acceso bloqueado" color="#8A94A6" />
        </Box>
        <DataGrid
          rows={usuarios}
          columns={columnas}
          getRowId={(row) => row.idUsuario}
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

      {restablecerClaveUsuario && (
        <DlgRestablecerClave
          idUsuario={restablecerClaveUsuario.idUsuario}
          nombreCompleto={restablecerClaveUsuario.nombreCompleto}
          onClose={() => setRestablecerClaveUsuario(null)}
          onExito={() => setRestablecerClaveUsuario(null)}
        />
      )}
    </PageHeaderSticky>
  )
}
