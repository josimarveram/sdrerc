# Variables en plantillas Word (`docs/plantillas`)

Listado de las plantillas `.doc`/`.docx` de `docs/plantillas` con las variables `#nomVariable#` encontradas en el cuerpo del documento, encabezados y pies de página.

Nota: `~$forme_reconstitucion.docx` es un archivo de bloqueo temporal de Word (indica que `informe_reconstitucion.docx` está/estuvo abierto en algún equipo), no es una plantilla y no se incluye en el listado.

## Cartas

### carta_abandono.docx
- `#correo#`
- `#direccion#`
- `#dniSolicitante#`
- `#nomTitular#`
- `#nroActa#`
- `#tipoActa#`
- `#tipoProcedimiento#`

### carta_edicto.docx
- `#correo#`
- `#direccion#`
- `#dniTitular#`
- `#fechaSolicitud#`
- `#nomTitular#`
- `#nroActa#`
- `#tipoActa#`
- `#tipoProcedimiento#`

### carta_falta_sustento.doc
- `#correo#`
- `#direccion#`
- `#nomTitular#`
- `#nroActa#`
- `#tipoActa#`
- `#tipoProcedimiento#`

### carta_improcedente.docx
- `#correo#`
- `#direccion#`
- `#nomTitular#`
- `#nroActa#`

### carta_indagatorio.docx
- `#correo#`
- `#direccion#`
- `#nomTitular#`
- `#nroActa#`
- `#tipoActa#`

### carta_precisar_pretension.doc
- `#correo#`
- `#direccion#`
- `#nomTitular#`
- `#nroActa#`
- `#tipoActa#`
- `#tipoProcedimiento#`

### carta_procedente.docx
- `#correo#`
- `#direccion#`
- `#nomTitular#`
- `#nroActa#`

### carta_procedente_en_parte.docx
- `#correo#`
- `#direccion#`
- `#nomTitular#`
- `#nroActa#`

## Informes

### informe_abandono.docx
- `#dniTitular#`
- `#nomTitular#`
- `#nroActa#`
- `#tipoActa#`
- `#tipoProcedimiento#`

### informe_cancelacion.docx
- `#dniSolicitante#`
- `#fechaSolicitud#`
- `#nomSolicitante#`
- `#nomTitular#`
- `#nroActa#`
- `#nroTramiteWeb#`
- `#numDoc#`
- `#tipoActa#`
- `#tipoProcedimiento#`

### informe_reconstitucion.docx
- `#dniSolicitante#`
- `#dniTitular#`
- `#fechaSolicitud#`
- `#nomSolicitante#`
- `#nomTitular#`
- `#nroActa#`
- `#nroTramiteWeb#`
- `#tipoActa#`

### informe_rectificacion.docx
- `#dniTitular#`
- `#fechaSolicitud#`
- `#nomSolicitante#`
- `#nomTitular#`
- `#nroActa#`
- `#nroTramiteWeb#`
- `#numDoc#`
- `#tipoActa#`
- `#tipoDoc#`

## Oficios

### oficio_indagatorio_cancelacion.docx
- `#correo#`
- `#direccion#`
- `#nomTitular#`
- `#nroActa#`
- `#tipoActa#`

### oficio_reconstitucion.docx
- `#correo#`
- `#direccion#`
- `#dniSolicitante#`
- `#nomSolicitante#`
- `#nomTitular#`
- `#nroActa#`
- `#tipoActa#`

## Resoluciones

### resolucion_abandono.docx
- `#canalRecepcion#`
- `#dniSolicitante#`
- `#fechaActual#`
- `#fechaSolicitud#`
- `#nomSolicitante#`
- `#nomTitular#`
- `#nroActa#`
- `#numDoc#`
- `#numDocInforme#`
- `#tipoActa#`
- `#tipoDoc#`
- `#tipoProcedimiento#`

### resolucion_cancelacion.docx
- `#dniTitular#`
- `#fechaDocInforme#`
- `#fechaSolicitud#`
- `#nomSolicitante#`
- `#nomTitular#`
- `#nroActa#`
- `#nroTramiteWeb#`
- `#numDoc#`
- `#numDocInforme#`
- `#tipoActa#`

### resolucion_error_material.docx
- `#correo#`
- `#direccion#`
- `#nomTitular#`
- `#nroActa#`

### resolucion_reconstitucion.docx
- `#fechaDocInforme#`
- `#fechaSolicitud#`
- `#nomSolicitante#`
- `#nomTitular#`
- `#nroActa#`
- `#nroTramiteWeb#`
- `#numDoc#`
- `#numDocInforme#`
- `#tipoActa#`

### resolucion_rectificacion.docx
- `#fechaDocInforme#`
- `#fechaSolicitud#`
- `#nomSolicitante#`
- `#nomTitular#`
- `#nroActa#`
- `#numDoc#`
- `#numDocInforme#`
- `#tipoActa#`

## Observaciones

- Todas las variantes de nombres detectadas en revisiones anteriores (`#tipoDeProcedimiento#` vs `#tipoProcedimiento#`, `#fechaDeLaSolicitud#` vs `#fechaSolicitud#`, `#nunDoc#`/`#nunDocInforme#` vs `#numDoc#`/`#numDocInforme#`, `#fechaactual#` vs `#fechaActual#`) ya quedaron **unificadas** en todas las plantillas. Actualmente los nombres son consistentes en todo `docs/plantillas`.
- `resolucion_cancelacion.docx`, `resolucion_reconstitucion.docx` y `resolucion_rectificacion.docx` ya usan `#fechaDocInforme#` junto a `#numDocInforme#`; ambas están mapeadas en código a los datos del mismo documento tipo `INFORME` más reciente (ver tabla de mapeo abajo).
- ⚠️ `resolucion_abandono.docx` es la única resolución con `#numDocInforme#` que **no** usa `#fechaDocInforme#` para la fecha del informe: su frase sigue siendo `...el Informe N° #numDocInforme#, de fecha #fechaActual#...`, usando `#fechaActual#` (fecha de generación del documento) en vez de la fecha real de emisión del informe. Si se quiere el mismo comportamiento que las demás resoluciones, habría que reemplazar ese `#fechaActual#` por `#fechaDocInforme#` directamente en el Word.
- `resolucion_error_material.docx` no referencia informe (no tiene `#numDocInforme#`/`#fechaDocInforme#`), consistente con que la corrección de error material no depende de un informe previo.
- No se encontraron marcadores condicionales `[[SI_ACTA:...]]` / `[[SI_PROCEDIMIENTO:...]]` / `[[FIN_SI]]` en ninguna plantilla actual. Esa funcionalidad existe en el código (`AnalisisPlantillaDocumentoService`) pero aún no se aplicó a estos archivos Word.

## Clasificación de documentos de Análisis (negocio)

Taxonomía confirmada, aplicada sobre `tipo_documento_adjunto.clasificacion` (`INTERMEDIO`/`FINAL`/`NULL`, único CHECK permitido hoy en BD):

| Categoría | Plantillas | `clasificacion` en BD |
|---|---|---|
| Cartas finales | carta_abandono, carta_improcedente, carta_procedente, carta_procedente_en_parte | `FINAL` |
| Cartas intermedias | carta_edicto, carta_falta_sustento, carta_indagatorio, carta_precisar_pretension | `INTERMEDIO` |
| Informes | informe_abandono, informe_cancelacion, informe_reconstitucion, informe_rectificacion | `NULL` (uso interno, no se notifican) |
| Oficios | oficio_indagatorio_cancelacion, oficio_reconstitucion | `INTERMEDIO` |
| Resoluciones | resolucion_abandono, resolucion_cancelacion, resolucion_error_material, resolucion_reconstitucion, resolucion_rectificacion | `FINAL` |

`carta_abandono` estaba mal clasificada como `INTERMEDIO` (heredado del script 52); se corrigió a `FINAL` con `db/sdrerc_app/scripts/55_fix_clasificacion_carta_abandono_final.sql` (pendiente de ejecución). El resto de la clasificación ya coincidía con esta taxonomía.

## Mapeo de variables en código (`AnalisisPlantillaDocumentoService.valores()`)

| Variable | Origen del dato |
|---|---|
| `#nomTitular#` | `expediente.getTitular()` |
| `#dniTitular#` | `expediente.getNumeroDocumentoTitular()` |
| `#nomSolicitante#` | `expediente.getSolicitante()` |
| `#dniSolicitante#` | `expediente.getNumeroDocumentoSolicitante()` |
| `#tipoActa#` | `expediente.getTipoActa()` |
| `#nroActa#` | `expediente.getNumeroActa()` |
| `#direccion#` | `expediente.getDireccionSolicitante()` |
| `#correo#` | `expediente.getCorreoSolicitante()` |
| `#tipoProcedimiento#` | `expediente.getProcedimiento()` |
| `#fechaSolicitud#` | `expediente.getFechaRecepcion()` |
| `#nroTramiteWeb#` | `expediente.getNumeroTramiteDocumentario()` |
| `#canalRecepcion#` | `expediente.getCanalIngreso()` |
| `#fechaActual#` | Fecha del sistema al generar el documento (`LocalDate.now()`) |
| `#numDoc#` | `expediente.getNumeroDocumento()` (documento de origen de la solicitud, no el documento que se genera) |
| `#tipoDoc#` | `expediente.getTipoDocumento()` (documento de origen de la solicitud) |
| `#numDocInforme#` | N° de documento del documento analizado tipo `INFORME` más reciente (activo) del mismo expediente; vacío si no existe informe |
| `#fechaDocInforme#` | Fecha de emisión del documento analizado tipo `INFORME` más reciente (activo) del mismo expediente; vacío si no existe informe |

Todas las variables `#nomVariable#` detectadas en las 19 plantillas están mapeadas en código a la fecha de esta revisión.

### Historial de correcciones aplicadas en las plantillas

- `informe_abandono.doc` y `resolucion_abandono.doc` pasaron a `.docx` (el contenido interno ya era formato `.docx`).
- Se corrigió un marcador roto en `resolucion_abandono.docx`: el texto pasó de un `#` suelto seguido de texto literal a `...el Informe N° #numDoc#, de fecha #fechaActual#, sobre ABANDONO...`.
- `#nunDoc#` → `#numDoc#` y `#nunDocInforme#` → `#numDocInforme#` (typo corregido).
- `#fechaactual#` → `#fechaActual#` (camelCase).
- `#fechaDeLaSolicitud#` → `#fechaSolicitud#` (unificado en todas las plantillas, incluida `resolucion_rectificacion.docx`).
- `#tipoDeProcedimiento#` → `#tipoProcedimiento#` (unificado en todas las plantillas, incluida `resolucion_abandono.docx`).
- `resolucion_abandono.docx`: se separó la referencia al informe de la referencia al documento de origen de la solicitud. El texto `...el Informe N° #numDoc#, de fecha #fechaActual#...` pasó a `...el Informe N° #numDocInforme#, de fecha #fechaActual#...`; `#numDoc#`/`#tipoDoc#` quedaron exclusivamente para la frase `mediante #tipoDoc# N° #numDoc# de fecha #fechaActual#, se remite la solicitud...` (documento de origen de la solicitud).
