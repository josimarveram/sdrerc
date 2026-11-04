import { sdrercColors } from './theme'

/**
 * Estilo base compartido para cualquier `<DataGrid>` del sistema (28/08/2026, sistema de diseño
 * premium — Incremento 1, ver plan "necesito-un-plan-de-cuddly-wombat.md"): antes cada `<DataGrid>`
 * de cada página de bandeja se sentaba directo sobre `backgroundPaper` (blanco) sin ningún estilo
 * de encabezado propio — se fundía con el resto de la página blanca, sin separación visual entre
 * "cabecera de tabla" y "cuerpo de tabla".
 *
 * No existe un componente `<AppDataGrid>` único (cada página arma su propio `<DataGrid sx={...}>`,
 * documentado como patrón ya establecido en el proyecto — "no existe un componente de tabla
 * reutilizable entre módulos"): en vez de forzar ese refactor mayor (tocaría las ~12 páginas de
 * bandeja de una sola vez, alto riesgo), este objeto se MEZCLA (spread) dentro del `sx` propio que
 * cada página ya define, sin reemplazar ni pisar nada de lo que ya tenía (columnas fijas por
 * `position:sticky`, colores de fila asociada, etc.).
 *
 * Aplicación página por página diferida a propósito al Incremento 2 del plan (verificar
 * visualmente cada bandeja antes de seguir con la siguiente) — este archivo solo se CREA en este
 * incremento, todavía no se importa desde ninguna página.
 */
export const dataGridBaseSx = {
  border: 'none',
  // 28/08/2026, bug real corregido (el usuario reportó "las cabeceras siguen viéndose blancas"):
  // esta versión de MUI X resuelve el fondo del encabezado con la variable CSS
  // `--DataGrid-t-header-background-base` (confirmado leyendo el código fuente real del paquete
  // instalado, `node_modules/@mui/x-data-grid/components/containers/GridRootStyles.js` — mismo
  // criterio de "verificar contra el paquete real" ya aplicado antes en este proyecto para
  // `pinnedColumns`/`disableColumnVirtualization`). Esa MISMA variable la consumen 2 reglas
  // distintas: `.MuiDataGrid-columnHeaders` (el contenedor de la fila de encabezados) Y
  // `.MuiDataGrid-columnHeader` (cada celda individual, que se dibuja ENCIMA del contenedor y cubre
  // toda su área) — pintar solo el contenedor (como hacía la versión anterior de este archivo)
  // queda completamente tapado por las celdas hijas, que sin este override siguen resolviendo la
  // variable a su valor por defecto (blanco). Sobreescribir la variable CSS en la raíz del
  // `<DataGrid>` es lo único que cascada correctamente a ambas reglas a la vez.
  '--DataGrid-t-header-background-base': sdrercColors.backgroundDefault,
  '& .MuiDataGrid-columnHeaders': {
    borderBottom: `1px solid ${sdrercColors.divider}`,
  },
  '& .MuiDataGrid-columnHeaderTitle': {
    fontWeight: 700,
    color: sdrercColors.textPrimary,
  },
  '& .MuiDataGrid-row:hover': {
    backgroundColor: sdrercColors.softBlue,
  },
  '& .MuiDataGrid-footerContainer': {
    borderTop: `1px solid ${sdrercColors.divider}`,
  },
} as const

/**
 * Nombre de clase compartido para los botones legacy del toolbar (28/08/2026, pedido explícito del
 * usuario: "estandariza que todas las grillas... tengan los iconos superiores sin texto... tal como
 * se encuentra en la Bandeja de Expedientes").
 *
 * `BandejaExpedientesPage.tsx` (y cualquier otra página que solo use `showToolbar` sin `slots.toolbar`
 * propio) ya se ve icono-solo de fábrica: el toolbar por defecto de esta versión de MUI X (v9.11) usa
 * primitivos internos (`ColumnsPanelTrigger`/`FilterPanelTrigger`/menú de exportación) que solo son
 * accesibles desde DENTRO del paquete — no forman parte del API público (`grep` contra
 * `node_modules/@mui/x-data-grid/index.d.ts` confirma que `ColumnsPanelTrigger`/`FilterPanelTrigger`/
 * `ExportPrint`/`ExportCsv`/`ToolbarButton` no se exportan), así que no se pueden reutilizar
 * directamente en un `slots.toolbar` custom sin un deep-import no soportado (frágil, se rompería con
 * cualquier actualización de parche).
 *
 * Los 5 toolbars custom del sistema (`ToolbarBandejaRegistro`, `ToolbarBandejaAsignacion`,
 * `ToolbarBandejaNotificacionAsignacion`, y los de las 2 bandejas de Notificación con árbol de
 * documentos) siguen usando los building blocks legacy `GridToolbarColumnsButton`/
 * `GridToolbarFilterButton`/`GridToolbarExport` (marcados `@deprecated` pero funcionales, ya
 * documentado en el Javadoc de `ToolbarBandejaRegistro`) — esos SÍ siguen mostrando el texto
 * "Columns"/"Filters"/"Export" porque cada uno fuerza su propio `children` de texto en el JSX interno
 * (confirmado leyendo el código fuente real: `GridToolbarColumnsButton.js`/`GridToolbarExportContainer.js`
 * — la última posición del spread de props siempre gana con `children: apiRef.current.getLocaleText(...)`,
 * así que no se puede pisar el texto pasando `slotProps.button.children`).
 *
 * **Por qué una clase CSS y no `sx`**: el tipo declarado de `slotProps.button` en esta versión de
 * MUI X (`Partial<GridSlotProps['baseButton']>`) NO expone `sx` en su interfaz pública (confirmado
 * con `tsc`: "Object literal may only specify known properties, and 'sx' does not exist..."), aunque
 * en runtime el slot real SÍ es un `Button` de MUI que lo soportaría — un hueco de tipado del propio
 * paquete, no un error del código. `className` sí está en el tipo, así que la clase real vive en
 * `index.css` (antes sin usar en el proyecto — recién importado desde `main.tsx` para este propósito)
 * en vez de `sx` inline. La regla colapsa el texto del botón (`font-size: 0`, nodo de texto plano sin
 * `<span>` propio que targetear por separado) a ancho cero, sin afectar el ícono porque `SvgIcon` fija
 * su propio tamaño explícito (`fontSize="medium"` → `1.5rem` vía clase propia, no heredado por
 * `font-size` del padre) — verificado leyendo `SvgIcon.js`/`Button.js` reales antes de aplicar el
 * truco, mismo criterio de "no asumir, confirmar contra el paquete instalado" ya usado en este
 * archivo para el bug del encabezado. `GridToolbarQuickFilter` (el ícono de búsqueda) NO necesita
 * este tratamiento: ya renderiza icono-solo de fábrica en ambos casos (el toolbar por defecto y los
 * 5 custom ya lo usan), se expande a un campo de texto recién al hacer clic.
 */
export const DATA_GRID_TOOLBAR_ICON_BUTTON_CLASS = 'sdrerc-toolbar-icon-btn'

/**
 * 2da pasada del mismo día (28/08/2026, pedido explícito del usuario: "cambiar el ícono de exportar
 * ya no quiero que despliegue a print y export to csv... un solo ícono de Excel... uniformizar estos
 * 4 íconos... mientras el DataGrid lo permita"): revierte parcialmente la conclusión del bloque de
 * arriba — "ya se ve bien de fábrica" dejó de ser suficiente, porque el toolbar por defecto
 * (`showToolbar` sin `slots.toolbar` propio) NO se puede recolorear ni reemplazar su botón de
 * exportar sin acceso a los primitivos internos (`ColumnsPanelTrigger`/etc., no públicos, ver arriba).
 * Único camino real para lograr color + un solo ícono de Excel en TODAS las bandejas por igual:
 * `components/DataGridStandardToolbar.tsx` (nuevo) — un toolbar propio, reutilizado por las 11
 * bandejas del sistema (antes 5 con toolbar custom + 6 con `showToolbar` puro) — ver Javadoc de ese
 * archivo para el detalle de qué exporta y cómo lo consume cada caso.
 *
 * Las 4 clases de color de abajo vienen de `index.css` (mismo mecanismo `!important` ya establecido
 * — MUI inyecta su propio CSS en tiempo de ejecución, después que Vite ya insertó el nuestro, así que
 * cualquier técnica sin `!important` pierde el empate de especificidad de forma no determinística).
 * `DATA_GRID_TOOLBAR_SEARCH_CLASS` no se usa desde ningún componente de este proyecto — el selector
 * real (`.MuiDataGrid-toolbarQuickFilterTrigger`) ya está en `index.css` como regla global, aplicada
 * automáticamente a las 11 bandejas sin tocar una sola línea de cada página.
 */
export const DATA_GRID_TOOLBAR_COLUMNS_CLASS = 'sdrerc-icon-columns'
export const DATA_GRID_TOOLBAR_FILTERS_CLASS = 'sdrerc-icon-filters'
export const DATA_GRID_TOOLBAR_EXPORT_CLASS = 'sdrerc-icon-export'

/**
 * Colores compartidos por las 3 acciones de fila más repetidas en las grillas de "documentos
 * analizados" (Análisis/Verificación/Ejecución/Notificación/Cartas de Respuesta/Grupo Familiar/
 * Plantillas) — mismo criterio que las clases de arriba, ver Javadoc completo en `index.css`. Un
 * solo color por acción en TODA la app, para que el usuario aprenda una sola asociación
 * color→significado (azul Word/azul Guardar/rojo Eliminar) en vez de una paleta distinta por módulo.
 */
export const DATA_GRID_ROW_ICON_WORD_CLASS = 'sdrerc-icon-word'
export const DATA_GRID_ROW_ICON_SAVE_CLASS = 'sdrerc-icon-save'
export const DATA_GRID_ROW_ICON_DELETE_CLASS = 'sdrerc-icon-delete'

/**
 * Ícono de "agrupar documentos por expediente" (29/08/2026, Bandeja de supervisión de Notificación,
 * pedido explícito del usuario: carpeta con "+" para expandir/colapsar los N documentos de un mismo
 * expediente bajo una sola fila padre, evitando repetir N° expediente/SGD/Titular en cada documento).
 * Color propio (ámbar/naranja, asociación visual real con un ícono de carpeta), distinto de los 3
 * colores de acción de fila ya reservados (azul Word/azul Guardar/rojo Eliminar) — mismo mecanismo
 * `!important` de `index.css`, ver Javadoc ahí.
 */
export const DATA_GRID_ROW_ICON_FOLDER_CLASS = 'sdrerc-icon-folder'

/**
 * Ícono de "ver intentos de notificación" (30/08/2026, Bandeja Notificación y Bandeja Publicación,
 * pedido explícito del usuario: reemplazar el "+"/"-" genérico de expandir/colapsar por uno que
 * represente la notificación al ciudadano, virtual y/o presencial). Se probaron primero
 * `ContactMailOutlined`/`ContactMail` (sobre+persona) y luego `MarkunreadMailboxOutlined`/
 * `MarkunreadMailbox` (buzón postal), ninguno convenció al usuario — reemplazados el mismo día por
 * `PersonPinCircleOutlined`/`PersonPinCircle` (MUI, pin de ubicación con silueta de persona,
 * elegido explícitamente entre 4 opciones vía `AskUserQuestion`): el objetivo real de cada intento
 * es "ubicar" al ciudadano, virtual o presencial — el propio sistema ya usa el término "Ubicado"/
 * "No ubicado" para el resultado (ver `NotificacionIntento.ubicado`), así que este ícono es el que
 * mejor refleja la funcionalidad real, no solo "notificación" en abstracto. Color
 * `sdrercColors.warning` (ámbar institucional, el mismo ya usado en KPIs como "Vencidos"/
 * "Pendientes" en toda la app) — corregido el mismo día (pedido explícito: "un color más acorde al
 * diseño del aplicativo") tras un primer intento con un ámbar brillante ad-hoc.
 */
export const DATA_GRID_ROW_ICON_NOTIFICACION_CLASS = 'sdrerc-icon-notificacion'
