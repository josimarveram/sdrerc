import { useCallback, useEffect, useMemo, useState } from 'react'
import { Box, Button, IconButton, MenuItem, Stack, TextField, Tooltip, Typography } from '@mui/material'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import ToggleOnOutlinedIcon from '@mui/icons-material/ToggleOnOutlined'
import ToggleOffOutlinedIcon from '@mui/icons-material/ToggleOffOutlined'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { buscarPlazos, cambiarActivoPlazo, obtenerPlazoSolicitud, type PlazoConfiguracion } from '../api/adminPlazosApi'
import { ApiError } from '../api/http'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import { DlgPlazo } from '../components/DlgPlazo'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ADMIN_PLAZOS_TABS } from '../layout/moduleTabs'

const COLOR_TOTAL = '#154775'

/**
 * Equivalente web de Administración &gt; Plazos (V2: `JPanelPlazosV2`). Mismo patrón de
 * bandeja+diálogo que Feriados. Ver `DlgPlazo.tsx` para la mejora de umbrales de color sobre V2.
 */
export function AdminPlazosPage() {
  const [plazos, setPlazos] = useState<PlazoConfiguracion[]>([])
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [texto, setTexto] = useState('')
  const [estadoFiltro, setEstadoFiltro] = useState<'' | 'true' | 'false'>('')

  const [dlgPlazo, setDlgPlazo] = useState<{ abierto: boolean; plazo: PlazoConfiguracion | null }>({ abierto: false, plazo: null })
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [plazoSolicitud, setPlazoSolicitud] = useState<PlazoConfiguracion | null>(null)

  const buscar = useCallback(async () => {
    setCargando(true)
    setError(null)
    try {
      const data = await buscarPlazos({ texto: texto || undefined, activo: estadoFiltro === '' ? undefined : estadoFiltro === 'true' })
      setPlazos(data)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cargar el listado de plazos.')
    } finally {
      setCargando(false)
    }
  }, [texto, estadoFiltro])

  useEffect(() => {
    buscar()
  }, [buscar])

  useEffect(() => {
    obtenerPlazoSolicitud()
      .then(setPlazoSolicitud)
      .catch(() => setPlazoSolicitud(null))
  }, [])

  function limpiar() {
    setTexto('')
    setEstadoFiltro('')
  }

  const kpis = useMemo(() => {
    const activos = plazos.filter((p) => p.activo).length
    return { total: plazos.length, activos, inactivos: plazos.length - activos }
  }, [plazos])

  async function alternarActivo(plazo: PlazoConfiguracion) {
    const confirmado = window.confirm(plazo.activo ? `¿Inactivar el plazo ${plazo.nombre}?` : `¿Activar el plazo ${plazo.nombre}?`)
    if (!confirmado || plazo.idPlazoConfiguracion === null) return
    try {
      await cambiarActivoPlazo(plazo.idPlazoConfiguracion, !plazo.activo)
      await buscar()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cambiar el estado del plazo.')
    }
  }

  const columnas: GridColDef<PlazoConfiguracion>[] = [
    { field: 'codigo', headerName: 'Código', width: 220 },
    { field: 'nombre', headerName: 'Nombre', flex: 1, minWidth: 220 },
    { field: 'ambito', headerName: 'Ámbito', width: 160 },
    { field: 'diasPlazo', headerName: 'Días', width: 80 },
    {
      field: 'unidadPlazo',
      headerName: 'Unidad',
      width: 130,
      valueFormatter: (value) => (value === 'HABILES' ? 'Hábiles' : 'Calendario'),
    },
    {
      field: 'umbrales',
      headerName: 'Umbrales de color',
      width: 150,
      sortable: false,
      filterable: false,
      renderCell: (params) =>
        params.row.porcentajeVerdeDesde != null ? (
          <StatusBadge label="Configurados" tone="info" />
        ) : (
          <Typography variant="caption" color="text.secondary">
            Por defecto
          </Typography>
        ),
    },
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
          <Tooltip title="Editar">
            <IconButton size="small" onClick={() => setDlgPlazo({ abierto: true, plazo: params.row })}>
              <EditOutlinedIcon fontSize="small" />
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

  return (
    <PageHeaderSticky
      titulo="Plazos"
      subtitulo="Configuración de plazos legales y umbrales de vencimiento por tipo de procedimiento."
      tabs={ADMIN_PLAZOS_TABS}
      acciones={
        <Button variant="contained" startIcon={<AddCircleOutlineIcon />} onClick={() => setDlgPlazo({ abierto: true, plazo: null })}>
          Nuevo plazo
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
        {mensaje && (
          <Typography color="success.main" variant="body2">
            {mensaje}
          </Typography>
        )}
        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px,1fr))', gap: 2 }}>
          <MetricCard label="Total de plazos" value={kpis.total} caption="Según filtros activos" color={COLOR_TOTAL} />
          <MetricCard label="Activos" value={kpis.activos} caption="Vigentes en el sistema" color="#1F9254" />
          <MetricCard
            label="Solicitud SDRERC"
            value={plazoSolicitud ? plazoSolicitud.diasPlazo : 0}
            caption={plazoSolicitud ? `${plazoSolicitud.nombre} (${plazoSolicitud.unidadPlazo === 'HABILES' ? 'hábiles' : 'calendario'})` : 'No configurado'}
            color="#B8860B"
          />
        </Box>
        <DataGrid
          rows={plazos}
          columns={columnas}
          getRowId={(row) => row.idPlazoConfiguracion ?? row.codigo}
          loading={cargando}
          onRowDoubleClick={(params) => setDlgPlazo({ abierto: true, plazo: params.row })}
          autoHeight
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
        />
      </Stack>

      {dlgPlazo.abierto && (
        <DlgPlazo
          plazo={dlgPlazo.plazo}
          onClose={() => setDlgPlazo({ abierto: false, plazo: null })}
          onGuardado={() => {
            setDlgPlazo({ abierto: false, plazo: null })
            setMensaje('Plazo guardado correctamente.')
            buscar()
            obtenerPlazoSolicitud()
              .then(setPlazoSolicitud)
              .catch(() => {})
          }}
        />
      )}
    </PageHeaderSticky>
  )
}
