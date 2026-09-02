import { useCallback, useEffect, useMemo, useState } from 'react'
import { Box, Button, IconButton, MenuItem, Stack, TextField, Tooltip, Typography } from '@mui/material'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import ToggleOnOutlinedIcon from '@mui/icons-material/ToggleOnOutlined'
import ToggleOffOutlinedIcon from '@mui/icons-material/ToggleOffOutlined'
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { dataGridBaseSx } from '../dataGridTheme'
import { sdrercColors } from '../theme'
import { buscarFeriados, cambiarActivoFeriado, type FeriadoAdmin } from '../api/adminFeriadosApi'
import { ApiError } from '../api/http'
import { useAppDialog } from '../dialogs/AppDialogProvider'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import { DlgFeriado } from '../components/DlgFeriado'
import { DlgImportarFeriadosXml } from '../components/DlgImportarFeriadosXml'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ADMIN_FERIADOS_TABS } from '../layout/moduleTabs'

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

const COLOR_TOTAL = '#154775'

/**
 * Equivalente web de Administración &gt; Feriados (V2: `JPanelFeriadosV2`). Mismo patrón de
 * bandeja+diálogo que Plantillas de documento (entidad plana sin sub-relaciones: no usa
 * `WorkspaceTabsDrawer`, un `Dialog` simple alcanza para alta/edición).
 *
 * Mejora sobre V2 (pedido explícito del usuario, "acepto sugerencias... como un experto"): 4to KPI
 * "Próximo feriado" (fecha del feriado activo más cercano en el futuro, o "-" si no hay ninguno)
 * en vez de limitarse a los 3 KPIs estáticos de V2 (Feriados/Activos/Inactivos) — información que
 * V2 no ofrece de un vistazo y que sí es relevante para quien administra el calendario laboral.
 */
export function AdminFeriadosPage() {
  const { confirm: confirmDialog } = useAppDialog()
  const [feriados, setFeriados] = useState<FeriadoAdmin[]>([])
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [anio, setAnio] = useState(new Date().getFullYear())
  const [estadoFiltro, setEstadoFiltro] = useState<'' | 'true' | 'false'>('')
  const [soloProximo, setSoloProximo] = useState(false)

  const [dlgFeriado, setDlgFeriado] = useState<{ abierto: boolean; feriado: FeriadoAdmin | null }>({ abierto: false, feriado: null })
  const [dlgImportar, setDlgImportar] = useState(false)
  const [mensaje, setMensaje] = useState<string | null>(null)

  const buscar = useCallback(async () => {
    setCargando(true)
    setError(null)
    try {
      const data = await buscarFeriados({ anio, activo: estadoFiltro === '' ? undefined : estadoFiltro === 'true' })
      setFeriados(data)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cargar el listado de feriados.')
    } finally {
      setCargando(false)
    }
  }, [anio, estadoFiltro])

  useEffect(() => {
    buscar()
  }, [buscar])

  function limpiar() {
    setAnio(new Date().getFullYear())
    setEstadoFiltro('')
    setSoloProximo(false)
  }

  const kpis = useMemo(() => {
    const activos = feriados.filter((f) => f.activo)
    const hoy = new Date()
    const hoyISO = hoy.toISOString().slice(0, 10)
    const proximo = activos.filter((f) => f.fecha >= hoyISO).sort((a, b) => a.fecha.localeCompare(b.fecha))[0]
    const diasRestantes = proximo
      ? Math.round((new Date(`${proximo.fecha}T00:00:00`).getTime() - new Date(`${hoyISO}T00:00:00`).getTime()) / 86400000)
      : 0
    return {
      total: feriados.length,
      activos: activos.length,
      inactivos: feriados.length - activos.length,
      diasRestantes,
      proximo,
      proximoCaption: proximo ? `${formatearFecha(proximo.fecha)} — ${proximo.nombre}` : 'Sin próximos feriados activos',
    }
  }, [feriados])

  const filasMostradas = useMemo(
    () => (soloProximo && kpis.proximo ? feriados.filter((f) => f.idFeriado === kpis.proximo!.idFeriado) : feriados),
    [feriados, soloProximo, kpis.proximo],
  )

  async function alternarActivo(feriado: FeriadoAdmin) {
    const confirmado = await confirmDialog(
      feriado.activo ? `¿Inactivar el feriado ${feriado.nombre}?` : `¿Activar el feriado ${feriado.nombre}?`,
      { tone: 'warning', confirmLabel: feriado.activo ? 'Inactivar' : 'Activar' },
    )
    if (!confirmado || feriado.idFeriado === null) return
    try {
      await cambiarActivoFeriado(feriado.idFeriado, !feriado.activo)
      await buscar()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cambiar el estado del feriado.')
    }
  }

  const columnas: GridColDef<FeriadoAdmin>[] = [
    { field: 'fecha', headerName: 'Fecha', width: 120, valueFormatter: (value) => formatearFecha(value as string) },
    { field: 'nombre', headerName: 'Nombre', flex: 1, minWidth: 220 },
    { field: 'tipo', headerName: 'Tipo', width: 130 },
    { field: 'observacion', headerName: 'Observación', flex: 1, minWidth: 220 },
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
            <IconButton size="small" onClick={() => setDlgFeriado({ abierto: true, feriado: params.row })}>
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
      titulo="Feriados"
      subtitulo="Calendario de feriados usado para el cálculo de días hábiles."
      tabs={ADMIN_FERIADOS_TABS}
      acciones={
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" startIcon={<UploadFileOutlinedIcon />} onClick={() => setDlgImportar(true)}>
            Importar XML
          </Button>
          <Button variant="contained" startIcon={<AddCircleOutlineIcon />} onClick={() => setDlgFeriado({ abierto: true, feriado: null })}>
            Nuevo feriado
          </Button>
        </Stack>
      }
      filtros={
        <Stack direction="row" spacing={2} flexWrap="wrap" alignItems="center">
          <TextField
            label="Año"
            type="number"
            size="small"
            value={anio}
            onChange={(e) => setAnio(Number(e.target.value))}
            sx={{ minWidth: 120 }}
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
        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: 2 }}>
          <Box onClick={() => { setEstadoFiltro(''); setSoloProximo(false) }} sx={{ cursor: 'pointer' }}>
            <MetricCard label="Total de feriados" value={kpis.total} caption={`Año ${anio}`} color={COLOR_TOTAL} active={estadoFiltro === '' && !soloProximo} />
          </Box>
          <Box onClick={() => { setEstadoFiltro((prev) => (prev === 'true' ? '' : 'true')); setSoloProximo(false) }} sx={{ cursor: 'pointer' }}>
            <MetricCard label="Activos" value={kpis.activos} caption="Aplican al calendario laboral" color="#1F9254" active={estadoFiltro === 'true' && !soloProximo} />
          </Box>
          <Box onClick={() => { setEstadoFiltro((prev) => (prev === 'false' ? '' : 'false')); setSoloProximo(false) }} sx={{ cursor: 'pointer' }}>
            <MetricCard label="Inactivos" value={kpis.inactivos} caption="Sin efecto en los plazos" color="#8A94A6" active={estadoFiltro === 'false' && !soloProximo} />
          </Box>
          <Box onClick={() => setSoloProximo((prev) => !prev)} sx={{ cursor: 'pointer' }}>
            <MetricCard label="Días al próximo feriado" value={kpis.diasRestantes} caption={kpis.proximoCaption} color="#B8860B" active={soloProximo} />
          </Box>
        </Box>
        <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2, overflow: 'hidden', boxShadow: '0 1px 3px rgba(16,24,40,0.08), 0 1px 2px rgba(16,24,40,0.04)' }}>
          <DataGrid
            rows={filasMostradas}
            columns={columnas}
            getRowId={(row) => row.idFeriado ?? row.fecha}
            loading={cargando}
            onRowDoubleClick={(params) => setDlgFeriado({ abierto: true, feriado: params.row })}
            density="compact"
            autoHeight
            showToolbar
            pageSizeOptions={[10, 25, 50, 100]}
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            sx={dataGridBaseSx}
          />
        </Box>
      </Stack>

      {dlgFeriado.abierto && (
        <DlgFeriado
          feriado={dlgFeriado.feriado}
          onClose={() => setDlgFeriado({ abierto: false, feriado: null })}
          onGuardado={() => {
            setDlgFeriado({ abierto: false, feriado: null })
            setMensaje('Feriado guardado correctamente.')
            buscar()
          }}
        />
      )}
      {dlgImportar && (
        <DlgImportarFeriadosXml
          onClose={() => setDlgImportar(false)}
          onImportado={(importados) => {
            setDlgImportar(false)
            setMensaje(`${importados} feriado(s) importado(s) correctamente.`)
            buscar()
          }}
        />
      )}
    </PageHeaderSticky>
  )
}
