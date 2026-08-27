import { useEffect, useMemo, useState } from 'react'
import { Box, Button, Paper, Stack, Tab, Tabs, Typography } from '@mui/material'
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { listarTiposConPlantilla, type PlantillaDocumentoAdmin } from '../api/adminPlantillasApi'
import { ApiError } from '../api/http'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import { DlgVariablesDisponibles } from '../components/DlgVariablesDisponibles'
import { PlantillaVersionPanel } from '../components/PlantillaVersionPanel'
import { PlantillaBloquesPanel } from '../components/PlantillaBloquesPanel'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ADMIN_PLANTILLAS_TABS } from '../layout/moduleTabs'
import { sdrercColors } from '../theme'

/**
 * Equivalente web de Administración &gt; Plantillas de documento (V2: `JPanelPlantillasDocumentoV2`
 * + `DlgBloquesPlantillaV2`). A diferencia de Usuarios/Roles/Equipo Jurídico, los "tipos de
 * documento" son un catálogo fijo (`TIPO_DOCUMENTO_ADJUNTO`, prefijo `ANALISIS_%`) — no se crean
 * ni eliminan desde acá, solo se administra SU plantilla — por eso el detalle es un panel de
 * selección simple (clic en la fila, no doble clic + `WorkspaceTabsDrawer`) en vez del patrón de
 * workspace multipestañas de los otros 3 módulos de Administración: no hay "Nuevo tipo" ni sentido
 * en tener varios tipos abiertos en pestañas simultáneas.
 */
export function AdminPlantillasPage() {
  const [tipos, setTipos] = useState<PlantillaDocumentoAdmin[]>([])
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [seleccionado, setSeleccionado] = useState<PlantillaDocumentoAdmin | null>(null)
  const [tab, setTab] = useState<'plantilla' | 'bloques'>('plantilla')
  const [variablesAbierto, setVariablesAbierto] = useState(false)

  async function cargar() {
    setCargando(true)
    setError(null)
    try {
      const data = await listarTiposConPlantilla()
      setTipos(data)
      if (seleccionado) {
        const actualizado = data.find((t) => t.idTipoDocumentoAdjunto === seleccionado.idTipoDocumentoAdjunto)
        if (actualizado) setSeleccionado(actualizado)
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cargar el listado de plantillas.')
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const kpis = useMemo(() => {
    const conPlantilla = tipos.filter((t) => t.idPlantillaDocumento !== null).length
    return { total: tipos.length, conPlantilla, sinPlantilla: tipos.length - conPlantilla }
  }, [tipos])

  const columnas: GridColDef<PlantillaDocumentoAdmin>[] = [
    { field: 'tipoDocumentoNombre', headerName: 'Tipo de documento', flex: 1, minWidth: 240 },
    { field: 'tipoDocumentoCodigo', headerName: 'Código', width: 220 },
    {
      field: 'estado',
      headerName: 'Estado',
      width: 170,
      valueGetter: (_v, row) => (row.idPlantillaDocumento !== null ? 'Con plantilla propia' : 'Usa plantilla del sistema'),
      renderCell: (params) => (
        <StatusBadge
          label={params.row.idPlantillaDocumento !== null ? 'Con plantilla propia' : 'Usa plantilla del sistema'}
          tone={params.row.idPlantillaDocumento !== null ? 'success' : 'warning'}
        />
      ),
    },
    { field: 'version', headerName: 'Versión', width: 90 },
    { field: 'cargadoPor', headerName: 'Cargado por', width: 180 },
  ]

  return (
    <PageHeaderSticky
      titulo="Plantillas de documento"
      subtitulo="Plantillas Word por tipo de documento, usadas al generar documentos en Análisis y Ejecución."
      tabs={ADMIN_PLANTILLAS_TABS}
      acciones={
        <Button variant="outlined" startIcon={<DescriptionOutlinedIcon />} onClick={() => setVariablesAbierto(true)}>
          Variables disponibles
        </Button>
      }
    >
      <Stack spacing={2}>
        {error && (
          <Typography color="error" variant="body2">
            {error}
          </Typography>
        )}
        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px,1fr))', gap: 2 }}>
          <MetricCard label="Tipos de documento" value={kpis.total} caption="Con generación de Word en Análisis" color={sdrercColors.primary} />
          <MetricCard label="Con plantilla propia" value={kpis.conPlantilla} caption="Administradas acá" color="#1F9254" />
          <MetricCard label="Usan plantilla del sistema" value={kpis.sinPlantilla} caption="Archivo en docs/plantillas" color="#B8860B" />
        </Box>
        <DataGrid
          rows={tipos}
          columns={columnas}
          getRowId={(row) => row.idTipoDocumentoAdjunto}
          loading={cargando}
          onRowClick={(params) => {
            setSeleccionado(params.row)
            setTab('plantilla')
          }}
          autoHeight
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          rowSelectionModel={
            seleccionado ? { type: 'include', ids: new Set([seleccionado.idTipoDocumentoAdjunto]) } : { type: 'include', ids: new Set() }
          }
        />

        {seleccionado && (
          <Paper elevation={1} sx={{ borderRadius: 2, overflow: 'hidden' }}>
            <Box sx={{ px: 3, pt: 2, bgcolor: sdrercColors.backgroundPaperAlt }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                {seleccionado.tipoDocumentoNombre}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {seleccionado.tipoDocumentoCodigo}
              </Typography>
              <Tabs value={tab} onChange={(_e, v) => setTab(v)} sx={{ mt: 1 }}>
                <Tab value="plantilla" label="Plantilla actual" />
                <Tab value="bloques" label="Bloques de contenido" />
              </Tabs>
            </Box>
            <Box sx={{ p: 3 }}>
              {tab === 'plantilla' ? (
                <PlantillaVersionPanel tipo={seleccionado} onCambio={cargar} />
              ) : (
                <PlantillaBloquesPanel tipo={seleccionado} />
              )}
            </Box>
          </Paper>
        )}
      </Stack>

      {variablesAbierto && <DlgVariablesDisponibles onClose={() => setVariablesAbierto(false)} />}
    </PageHeaderSticky>
  )
}
