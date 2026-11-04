import { useEffect, useMemo, useState } from 'react'
import { Box, Stack, Typography } from '@mui/material'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { listarTiposConPlantilla, type PlantillaDocumentoAdmin } from '../api/adminPlantillasApi'
import { ApiError } from '../api/http'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import { PlantillaVersionPanel } from '../components/PlantillaVersionPanel'
import { PlantillaBloquesPanel } from '../components/PlantillaBloquesPanel'
import { VariablesDisponiblesContent } from '../components/VariablesDisponiblesContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ADMIN_PLANTILLAS_TABS } from '../layout/moduleTabs'
import { sdrercColors } from '../theme'
import { dataGridBaseSx } from '../dataGridTheme'

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
// 28/08/2026, sistema de diseño premium: unificado a `#2370A8` (mismo rol que "Datos" en las demás
// bandejas — la subpestaña base/primera de este workspace, antes en el azul institucional).
const COLOR_PLANTILLA = '#2370A8'
const COLOR_BLOQUES = '#0B8E97'
const COLOR_VARIABLES = '#8E44AD'

interface WorkspaceAbierto {
  id: string
  idTipoDocumentoAdjunto: number
  color: string
  activeSubTabId: string
}

/**
 * Equivalente web de Administración &gt; Plantillas de documento (V2: `JPanelPlantillasDocumentoV2`
 * + `DlgBloquesPlantillaV2`). Rediseñado el 27/08/2026 (pedido explícito del usuario) al mismo
 * patrón de consola tipo Salesforce ya usado en el resto de V3 (`WorkspaceTabsDrawer`, doble clic
 * abre el panel) en vez del anterior "panel debajo de la grilla" activado con un solo clic — los
 * "tipos de documento" siguen siendo un catálogo fijo (`TIPO_DOCUMENTO_ADJUNTO`, prefijo
 * `ANALISIS_%`, no se crean/eliminan desde acá), pero eso no exige un patrón de interacción
 * distinto al resto de la app; unificar mejora la consistencia sin costo real.
 *
 * 3 subpestañas por tipo de documento abierto: "Plantilla" (historial de versiones + carga),
 * "Bloques de contenido" (con Autocomplete de variables + chips + probador de condición, ver
 * `PlantillaBloquesPanel`) y "Variables" (catálogo filtrable, antes un `Dialog` global sin relación
 * con el tipo enfocado).
 *
 * "Editor en línea" (OnlyOffice embebido, `PlantillaEditorEnLineaContent`) se construyó y funcionó
 * (ver v3/CLAUDE.md, sección Plantillas) pero se OCULTÓ del menú el 02/09/2026 — pedido explícito del
 * usuario, de cara a la conversación con la UIS/OTI sobre despliegue en contenedores: exigía un
 * contenedor adicional (OnlyOffice Document Server) para un beneficio marginal frente al resto del
 * módulo (vista previa + versionado + bloques ya cubren la administración real de plantillas). El
 * componente y todo su soporte de backend (`PlantillaOnlyOfficeService`/`OnlyOfficeJwtService`/
 * `PlantillaOnlyOfficeController`) NO se eliminaron — solo se quitó la entrada de `subTabs` de abajo
 * — por si en el entorno oficial de la OTI sí hay margen para un contenedor más y se decide
 * reactivarlo más adelante.
 */
export function AdminPlantillasPage() {
  const [tipos, setTipos] = useState<PlantillaDocumentoAdmin[]>([])
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [workspaceActivo, setWorkspaceActivo] = useState<string | null>(null)
  const [filtroKpi, setFiltroKpi] = useState<'CON_PLANTILLA' | 'SIN_PLANTILLA' | null>(null)

  async function cargar() {
    setCargando(true)
    setError(null)
    try {
      const data = await listarTiposConPlantilla()
      setTipos(data)
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

  const tiposFiltrados = useMemo(() => {
    if (filtroKpi === 'CON_PLANTILLA') return tipos.filter((t) => t.idPlantillaDocumento !== null)
    if (filtroKpi === 'SIN_PLANTILLA') return tipos.filter((t) => t.idPlantillaDocumento === null)
    return tipos
  }, [tipos, filtroKpi])

  function alternarFiltro(valor: 'CON_PLANTILLA' | 'SIN_PLANTILLA') {
    setFiltroKpi((prev) => (prev === valor ? null : valor))
  }

  function abrirWorkspace(tipo: PlantillaDocumentoAdmin) {
    const id = `ws-${tipo.idTipoDocumentoAdjunto}`
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) return prev
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { id, idTipoDocumentoAdjunto: tipo.idTipoDocumentoAdjunto, color, activeSubTabId: 'plantilla' }]
    })
    setWorkspaceActivo(id)
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

  function irASubTab(workspaceId: string, subTabId: string) {
    setWorkspacesAbiertos((prev) => prev.map((w) => (w.id === workspaceId ? { ...w, activeSubTabId: subTabId } : w)))
  }

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

  const workspaces: Workspace[] = workspacesAbiertos.map((w) => {
    const tipo = tipos.find((t) => t.idTipoDocumentoAdjunto === w.idTipoDocumentoAdjunto)
    if (!tipo) {
      return {
        id: w.id,
        label: 'Cargando…',
        color: w.color,
        activeSubTabId: w.activeSubTabId,
        subTabs: [{ id: 'plantilla', label: 'Plantilla', color: COLOR_PLANTILLA, content: null }],
      }
    }
    return {
      id: w.id,
      label: tipo.tipoDocumentoNombre,
      color: w.color,
      activeSubTabId: w.activeSubTabId,
      subTabs: [
        {
          id: 'plantilla',
          label: 'Plantilla',
          color: COLOR_PLANTILLA,
          content: <PlantillaVersionPanel tipo={tipo} onCambio={cargar} />,
        },
        {
          id: 'bloques',
          label: 'Bloques de contenido',
          color: COLOR_BLOQUES,
          content: <PlantillaBloquesPanel tipo={tipo} />,
        },
        {
          id: 'variables',
          label: 'Variables',
          color: COLOR_VARIABLES,
          content: <VariablesDisponiblesContent />,
        },
      ],
    }
  })

  return (
    <PageHeaderSticky
      titulo="Plantillas de documento"
      subtitulo="Plantillas Word por tipo de documento, usadas al generar documentos en Análisis y Ejecución."
      tabs={ADMIN_PLANTILLAS_TABS}
    >
      <Stack spacing={2}>
        {error && (
          <Typography color="error" variant="body2">
            {error}
          </Typography>
        )}
        <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: 2 }}>
          <Box onClick={() => setFiltroKpi(null)} sx={{ cursor: 'pointer' }}>
            <MetricCard label="Tipos de documento" value={kpis.total} caption="Con generación de Word en Análisis" color={sdrercColors.primary} active={filtroKpi === null} />
          </Box>
          <Box onClick={() => alternarFiltro('CON_PLANTILLA')} sx={{ cursor: 'pointer' }}>
            <MetricCard label="Con plantilla propia" value={kpis.conPlantilla} caption="Administradas acá" color="#1F9254" active={filtroKpi === 'CON_PLANTILLA'} />
          </Box>
          <Box onClick={() => alternarFiltro('SIN_PLANTILLA')} sx={{ cursor: 'pointer' }}>
            <MetricCard label="Usan plantilla del sistema" value={kpis.sinPlantilla} caption="Archivo en docs/plantillas" color="#B8860B" active={filtroKpi === 'SIN_PLANTILLA'} />
          </Box>
        </Box>
        <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2, overflow: 'hidden', boxShadow: '0 1px 3px rgba(16,24,40,0.08), 0 1px 2px rgba(16,24,40,0.04)' }}>
          <DataGrid
            rows={tiposFiltrados}
            columns={columnas}
            getRowId={(row) => row.idTipoDocumentoAdjunto}
            loading={cargando}
            onRowDoubleClick={(params) => abrirWorkspace(params.row)}
            density="compact"
            autoHeight
            showToolbar
            pageSizeOptions={[10, 25, 50, 100]}
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            sx={dataGridBaseSx}
          />
        </Box>
      </Stack>

      <WorkspaceTabsDrawer
        workspaces={workspaces}
        activeWorkspaceId={workspaceActivo}
        onSelectWorkspace={setWorkspaceActivo}
        onCloseWorkspace={cerrarWorkspace}
        onSelectSubTab={irASubTab}
      />
    </PageHeaderSticky>
  )
}
