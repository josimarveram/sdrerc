import { IconButton, Tooltip } from '@mui/material'
import { GridToolbarColumnsButton, GridToolbarContainer, GridToolbarFilterButton, GridToolbarQuickFilter, useGridApiContext } from '@mui/x-data-grid'
import {
  DATA_GRID_TOOLBAR_COLUMNS_CLASS,
  DATA_GRID_TOOLBAR_EXPORT_CLASS,
  DATA_GRID_TOOLBAR_ICON_BUTTON_CLASS,
  DATA_GRID_TOOLBAR_FILTERS_CLASS,
} from '../dataGridTheme'
import { ExcelFileIcon } from './icons/FileTypeIcons'

/**
 * Botón de exportación reducido a un solo ícono de Excel (28/08/2026, pedido explícito del usuario:
 * "ya no quiero que despliegue a print y export to csv... cambia todo eso por un solo ícono de
 * Excel"). Reemplaza a `GridToolbarExport` (que SIEMPRE abre un menú con "Imprimir"/"Descargar CSV",
 * sin forma de quitar esa 2da opción vía props — confirmado leyendo `GridToolbarExportContainer.js`:
 * el menú se arma incondicionalmente si hay al menos 1 opción habilitada) — este botón llama
 * directo `apiRef.current.exportDataAsCsv()` (mismo mecanismo que ya usan "Exportar seleccionados"
 * en varias bandejas, un `.csv` que Excel abre nativamente sin ningún paso intermedio) sin menú.
 */
function BotonExportarExcel() {
  const apiRef = useGridApiContext()
  return (
    <Tooltip title="Exportar a Excel">
      <IconButton
        size="small"
        className={`${DATA_GRID_TOOLBAR_ICON_BUTTON_CLASS} ${DATA_GRID_TOOLBAR_EXPORT_CLASS}`}
        onClick={() => apiRef.current.exportDataAsCsv()}
      >
        <ExcelFileIcon fontSize="small" />
      </IconButton>
    </Tooltip>
  )
}

/**
 * Cluster de los 4 controles estándar de cualquier `DataGrid` del sistema — Columnas/Filtros/
 * Exportar(Excel)/Buscar, cada uno con su propio color (28/08/2026, sistema de diseño premium, 2da
 * pasada, pedido explícito del usuario: "iconos más premium... colorido pero elegante... uniformizar
 * estos 4 iconos"). Pensado para insertarse dentro de CUALQUIER `slots.toolbar` custom (junto a
 * controles propios de cada bandeja, ej. el chip "N seleccionados" de `ToolbarBandejaRegistro`) — no
 * incluye `GridToolbarContainer` a propósito, así el llamador decide dónde ubicarlo dentro de su
 * propio layout (normalmente al final, empujado a la derecha con un `<Box sx={{flex:1}}/>` antes).
 *
 * `GridToolbarQuickFilter` no lleva ninguna clase/prop de color propia: su color (magenta/morado) se
 * aplica vía un selector CSS GLOBAL en `index.css` (`.MuiDataGrid-toolbarQuickFilterTrigger`, el
 * nombre de clase real que MUI X genera para este control) — se pinta igual en las 11 bandejas del
 * sistema sin necesidad de tocar este componente ni ningún otro archivo.
 */
export function DataGridStandardToolbarControls() {
  return (
    <>
      <GridToolbarColumnsButton
        slotProps={{ button: { className: `${DATA_GRID_TOOLBAR_ICON_BUTTON_CLASS} ${DATA_GRID_TOOLBAR_COLUMNS_CLASS}` } }}
      />
      <GridToolbarFilterButton
        slotProps={{ button: { className: `${DATA_GRID_TOOLBAR_ICON_BUTTON_CLASS} ${DATA_GRID_TOOLBAR_FILTERS_CLASS}` } }}
      />
      <BotonExportarExcel />
      <GridToolbarQuickFilter />
    </>
  )
}

/**
 * Toolbar completo, listo para usar como `slots={{ toolbar: DataGridStandardToolbar }}` en cualquier
 * `DataGrid` que hoy solo tenga `showToolbar` (sin controles propios de la bandeja) — reemplaza el
 * toolbar por defecto de MUI X (icono-solo pero SIN color, sin forma de personalizarlo, ver Javadoc
 * de `DATA_GRID_TOOLBAR_ICON_BUTTON_CLASS` en `dataGridTheme.ts`) por este, con los mismos 4 controles
 * ya coloreados. Las bandejas que YA tenían un toolbar propio con controles adicionales (selección
 * múltiple, "Asociar al grupo familiar", etc.) NO usan este componente completo — importan
 * únicamente `DataGridStandardToolbarControls` y la insertan dentro de su propio toolbar existente,
 * junto a sus botones propios.
 */
export function DataGridStandardToolbar() {
  return (
    <GridToolbarContainer>
      <DataGridStandardToolbarControls />
    </GridToolbarContainer>
  )
}
