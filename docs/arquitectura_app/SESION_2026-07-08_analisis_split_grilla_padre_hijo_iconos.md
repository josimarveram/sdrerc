# Sesión 2026-07-08 (continuación) - Análisis: split de la grilla en padre/hijo, iconos por fila y catálogo "Carta de Respuesta"

Registro técnico de esta sesión de Claude Code sobre el módulo Análisis, posterior a la sesión documentada en `SESION_2026-07-08_analisis_columnas_documentos_analizados.md`. Es una bitácora de lo realizado; no reemplaza a `AGENTS.md` ni `CLAUDE.md`.

## 1. Punto de partida

Al iniciar, `git status` mostraba modificados de una sesión previa:

- `db/sdrerc_app/scripts/40_datos_maestros_resultados_evaluacion_analisis.sql`
- `src/main/java/com/sdrerc/application/sdrercapp/AnalisisExpedienteService.java`
- `src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/AnalisisExpedienteDAO.java`
- `src/main/java/com/sdrerc/ui/views/analisis/DocumentoAnalisisTreeGridPanelV2.java`
- `src/main/java/com/sdrerc/ui/views/analisis/JPanelAnalisisV2.java`
- Sin trackear: `db/sdrerc_app/scripts/41_datos_maestros_tipo_documento_carta_respuesta.sql`, `docs/arquitectura_app/SESION_2026-07-08_analisis_columnas_documentos_analizados.md`

## 2. Pedido del usuario

El usuario adjuntó una captura (mockup Excel) mostrando que la grilla de "Documentos analizados" en Análisis debía ser **dos grillas visualmente separadas**, no una sola tabla:

- Arriba, banda "DOCUMENTOS DE ANALISIS" (fila **PADRE**): columnas Tipo documento, Número Documento, Estado documento, Fecha Emisión, Comentario, ¿Requiere respuesta?. Se agrega con el botón "+ Documento".
- Abajo, banda "BANDEJA DE CARTAS DE RESPUESTA (ASIGNACION)" (fila **HIJO**): columnas Tipo documento, Confirmación de respuesta, Fecha Respuesta, Fecha Publicación, Hoja de Envío. Se agrega con el botón "+ Relacionado".

Pedido adicional:

- Agregar un tipo de documento de catálogo "Carta de respuesta" vía script BD (no hardcodeado).
- Fila padre: 3 iconos al final (Word para descargar plantilla, Guardar para grabar cambios de esa fila, Eliminar para esa fila).
- Fila hija: 2 iconos al final (Guardar, Eliminar).

## 3. Exploración previa (antes de tocar código)

Se investigó el estado real del código (no asumido) antes de diseñar el cambio:

- `DocumentoAnalisisTreeGridPanelV2.java` **ya tenía** soporte de jerarquía padre/hijo de 2 niveles a nivel de datos (BD: columnas `ID_DOCUMENTO_PADRE`, `NIVEL`, `ORDEN`, `ESTADO_RESPUESTA` en `EXPEDIENTE_DOCUMENTO_ANALIZADO`, sembradas por `39_patch_documento_analizado_jerarquia.sql`; DAO: `DocumentoAnalisisDAO.guardarDocumentosJerarquicos`/`listarPorExpediente`; DTO: `DocumentoAnalizadoDTO` con `idDocumentoPadre`/`nivel`/`orden`/`estadoRespuesta`), pero la presentaba como **una sola tabla** con una columna `+`/`-` que expandía/colapsaba los hijos indentados bajo el padre — no como dos grillas separadas.
- Se encontró un segundo hallazgo clave: `JPanelAnalisisV2.java` contenía un subsistema de grilla plana completo (`documentosTable`, `documentosFijosTable`, `documentoModel`) con iconos ya implementados (`WordDocumentIcon`, `SaveDocumentIcon`, `DeleteDocumentIcon` y sus renderers/editors), pero **nunca se pintaba en pantalla**: el campo `documentosScrollPane` nunca se inicializaba con `new JScrollPane(...)`, y el panel lateral "Plantillas Word" (`crearPanelPlantillasAnalisis()`) nunca se invocaba desde ningún otro punto del archivo. Era código muerto sobreviviente de un diseño anterior a la migración a `DocumentoAnalisisTreeGridPanelV2`.
- Se confirmó la estructura del catálogo `tipo_documento_adjunto` (columnas `codigo`, `nombre`, `activo`) y el patrón `MERGE` idempotente usado en scripts previos (`21_...sql`, `31_...sql`), y que `CatalogoLookupDAO.listarTiposDocumentoAdjuntoAnalisis()` filtra `codigo LIKE 'ANALISIS_%' ORDER BY codigo` — cualquier código nuevo con ese prefijo aparece automáticamente en el combo sin tocar Java.

## 4. Preguntas al usuario antes de planear (sin asumir)

1. **Alcance de la grilla hija**: ¿solo los relacionados del padre seleccionado en la grilla superior, o todos los relacionados del expediente sin depender de selección? → Respuesta: **solo del padre seleccionado**.
2. **Limpieza del código muerto** (~800 líneas: `documentosTable`, iconos Word/Guardar/Eliminar nunca renderizados): ¿reutilizar esos iconos portándolos a la grilla nueva y eliminar el bloque muerto, o dejarlo intacto? → Respuesta: **sí, eliminarlo**.

## 5. Plan (modo plan, aprobado por el usuario)

Plan completo guardado en `C:\Users\joel_\.claude\plans\valiant-zooming-sprout.md` (fuera del repo). Puntos clave:

1. Reescribir `DocumentoAnalisisTreeGridPanelV2.java` con dos `JTable`/`AbstractTableModel` (`PadreTableModel`, `HijoTableModel`) en vez de un único modelo con expand/collapse.
2. Portar (mover, no duplicar) `WordDocumentIcon`/`SaveDocumentIcon`/`DeleteDocumentIcon` desde `JPanelAnalisisV2` y crear renderers/editors genéricos por fila (`RowActionRenderer`/`RowActionEditor`).
3. Wiring nuevo en `JPanelAnalisisV2.crearDocumentosPanel()` con 3 handlers adicionales (`SaveRowHandler`, `DeleteRowHandler`, `DownloadPlantillaHandler`).
4. Nuevos métodos de servicio/DAO: `guardarDocumentoAnalisisJerarquico` (reutiliza el motor jerárquico existente con lista de 1) y `darBajaDocumentosAnalisis` (nuevo, porque la validación `AnalisisValidacionService.validarDocumentosAnalisis` exige al menos un documento activo en la lista, lo cual rompe una baja pura).
5. Script SQL nuevo `41_datos_maestros_tipo_documento_carta_respuesta.sql`, sin ejecutar.
6. Eliminación del subsistema muerto en `JPanelAnalisisV2.java`.

## 6. Implementación

### `AnalisisExpedienteDAO.java`

Nuevo método `darBajaDocumentosAnalisis(Long idExpediente, List<Long> idsDocumentoAnalizado, Long idUsuario)`, ubicado junto a `guardarDocumentosAnalisisJerarquicos`. Bloquea el expediente (`bloquearExpediente`), valida etapa/estado igual que el guardado jerárquico, y hace un bucle de `documentoAnalisisDAO.darBajaDocumentoAnalizado(conn, idExpediente, id, idUsuario)` (baja lógica, `activo=0`, ya existente) dentro de una única transacción con commit/rollback.

### `AnalisisExpedienteService.java`

Dos métodos nuevos:

```java
public AnalisisResultadoDTO guardarDocumentoAnalisisJerarquico(Long idExpediente, DocumentoAnalizadoDTO documento) throws SQLException {
    List<DocumentoAnalizadoDTO> documentos = new ArrayList<DocumentoAnalizadoDTO>();
    if (documento != null) documentos.add(documento);
    return guardarDocumentosAnalisisJerarquicos(idExpediente, documentos);
}

public AnalisisResultadoDTO darBajaDocumentosAnalisis(Long idExpediente, List<Long> idsDocumentoAnalizado) throws SQLException {
    validacionService.validarExpedienteSeleccionado(idExpediente);
    if (idsDocumentoAnalizado == null || idsDocumentoAnalizado.isEmpty()) {
        throw new IllegalArgumentException("Seleccione al menos un documento para dar de baja.");
    }
    return analisisExpedienteDAO.darBajaDocumentosAnalisis(idExpediente, idsDocumentoAnalizado, resolverUsuarioActualSdrercApp());
}
```

### `DocumentoAnalisisTreeGridPanelV2.java` (reescrito completo)

- Eliminada la columna `COL_EXPANDIR` y toda la lógica de expand/collapse (`toggleExpanded`, `expanded`, `cerrarGruposSinHijos`).
- Dos `JTable` independientes (`tablaPadre`, `tablaHijo`) sobre `PadreTableModel`/`HijoTableModel`, ambos filtrando de una única lista fuente `allRows`.
- `tablaPadre`: columnas Tipo, N° Documento, Estado documento, Fecha Emisión, Comentario, ¿Requiere respuesta?, + 3 columnas de icono (Word/Guardar/Eliminar).
- `tablaHijo`: columnas Tipo, Confirmación de respuesta, Fecha Respuesta, Fecha Publicación (no editable, se alimenta desde otro flujo), Hoja de Envío, + 2 columnas de icono (Guardar/Eliminar).
- Selección de una fila en `tablaPadre` dispara `rebuildHijo(idPadre)`, que filtra `allRows` por `nivel==1 && parentId==idPadre`.
- Banners de sección ("Documentos de análisis" / "Documentos relacionados / respuesta") con el estilo `AppV2Theme.SURFACE_ALT` ya usado en cabeceras de tabla del proyecto.
- Nuevas interfaces funcionales: `SaveRowHandler`, `DeleteRowHandler`, `DownloadPlantillaHandler` (además de `SaveHandler` existente), todas expuestas vía `setHandlers(...)`.
- `guardarFila(DocumentoRow)`: llama a `saveRowHandler.guardarFila(documento)` en `SwingWorker`; en éxito llama a `refrescar()` (recarga completa desde BD, igual que el guardado masivo existente — así se resuelven los IDs temporales negativos).
- `eliminarFila(DocumentoRow)`: si el id es temporal (< 0, nunca guardado) se quita solo del modelo local (con cascada a hijos temporales si es padre); si es real, confirma y llama a `deleteRowHandler.eliminarFila(idExpediente, ids)` incluyendo en cascada los ids de hijos activos reales cuando la fila es padre.
- `descargarPlantilla(DocumentoRow)`: delega en `downloadHandler.descargar(documento)`, sin acoplar este panel a `AnalisisPlantillaDocumentoService`/`AnalisisExpedienteDTO`.
- Botón "Dar de baja" (bulk) eliminado del toolbar — queda cubierto por el icono Eliminar por fila. Se mantienen "Guardar cambios", "Cancelar cambios", "Refrescar".

### `JPanelAnalisisV2.java`

- `crearDocumentosPanel()`: `setHandlers(...)` ampliado con los 3 handlers nuevos, cada uno reutilizando `requerirSeleccion(...)`/`puedeGuardarDocumentos(item)` igual que el `SaveHandler` original.
- `descargarPlantillaDocumento(int modelRow)` → adaptado a `descargarPlantillaDocumento(DocumentoAnalizadoDTO documento)`, recibiendo el documento directamente en vez de leerlo de la fila muerta.
- **Limpieza de código muerto** (delegada a un subagente con instrucciones explícitas de verificar cada símbolo con grep antes de borrar, y de no tocar nada relacionado con `documentosTreePanel`): eliminadas ~1440 líneas netas (archivo pasó de 4520 a 3066 líneas). Se borraron: las 19 constantes `COL_DOCUMENTO_*`, los campos `documentoModel`/`documentosTable`/`documentosFijosModel`/`documentosFijosTable`/`documentosScrollPane`/`documentosFijosScrollPane`/`documentosColumnFilterSupport` y filtros asociados, métodos auxiliares (`construirScrollFijoDocumentos`, `configurarDocumentoTabla(Base)`, `actualizarAlturaDocumentosAnalizados`, `documentoDesdeFila`, `quitarDocumentoFila`, `guardarDocumentoFila`, `descargarPlantillaDocumentoSeleccionado`, `actualizarPlantillaSeleccionada`, `crearPanelPlantillas(Analisis)`, filtros de columna fija, pintura de cabecera congelada, etc.) y clases internas (`FrozenDocumentHeaderPanel`, `PlantillaDocumentoRenderer/Editor`, `GuardarDocumentoRenderer/Editor`, `EliminarDocumentoRenderer/Editor`, `DescripcionDocumentoRenderer/CellEditor`, `FechaDocumentoCellEditor`, `WordDocumentIcon`/`SaveDocumentIcon`/`DeleteDocumentIcon` —ya movidos a `DocumentoAnalisisTreeGridPanelV2`—, entre otras).
  - Ampliación de alcance justificada y documentada por el subagente: el archivo **no compilaba** antes de terminar la limpieza porque `descargarPlantillaDocumentoSeleccionado()` llamaba a un overload de `descargarPlantillaDocumento(int)` ya inexistente; investigando esto se detectó un segundo bolsillo de código inalcanzable (`btnAgregarDocumento`/`btnQuitarDocumento` y handlers asociados, nunca agregados a ningún contenedor visible) que también se eliminó por depender enteramente del mismo modelo muerto.
  - Se preservaron intactos, por tener usos vivos fuera del subsistema muerto: `documentosTreePanel` y su wiring, `descargarPlantillaDocumento(DocumentoAnalizadoDTO)`, `crearExpedienteDigitalPanel()` (queda sin otro llamador, fuera de alcance de esta tarea, no se tocó), y varios campos de formulario (`cmbTipoDocumento`, `cmbEstadoDocumento`, etc.) usados por otros métodos vivos.
  - `obtenerDocumentosFormulario()` simplificado a delegar directo en `documentosTreePanel.getDocumentosActivos()` (el *fallback* que iteraba `documentoModel` era inalcanzable, ya que `documentosTreePanel` nunca es `null` en tiempo de ejecución).

### `db/sdrerc_app/scripts/41_datos_maestros_tipo_documento_carta_respuesta.sql` (nuevo, no ejecutado)

`MERGE` idempotente que agrega `ANALISIS_DOC_20_CARTA_RESPUESTA` → "Carta de Respuesta" a `tipo_documento_adjunto`, siguiendo el patrón exacto de `31_datos_maestros_tipos_documento_analisis_plantillas.sql`. No se ejecutó contra la base de datos.

## 7. Verificación

- El subagente que hizo la limpieza ejecutó `mvn -q -o clean compile` (offline) al finalizar: **BUILD SUCCESS**, sin errores. Hizo un barrido final por grep de todos los símbolos eliminados sin encontrar referencias colgantes.
- Se intentó recompilar de forma independiente con `mvn clean compile` desde la sesión principal para verificar el estado consolidado (incluyendo los cambios de Service/DAO hechos después de la limpieza), pero el comando falló repetidamente con `Tool permission request failed: Error: Stream closed` — un problema de la capa de permisos de la herramienta, no del código (comandos `git`/`echo` sí respondieron con normalidad en el mismo intervalo). **No se logró confirmar la compilación del estado final consolidado en esta sesión.**
- No se probó la UI en un navegador/entorno gráfico real (aplicación Swing de escritorio, fuera del alcance de las herramientas de esta sesión).

## 8. Estado final

- SQL: no se ejecutó ningún script contra la base de datos (script 41 solo creado/versionado).
- Legacy y `OracleConnection.java`: no tocados.
- Commit/push: **no realizados** en esta sesión (no fueron solicitados explícitamente).
- Build: verificado exitosamente por el subagente antes de los últimos cambios de Service/DAO; **pendiente reconfirmar** `mvn clean compile` sobre el estado final una vez se resuelva el problema de permisos de la herramienta.

## 9. Pendientes para la próxima sesión

- Reintentar `mvn clean compile` sobre el estado final (incluye los cambios en `AnalisisExpedienteService.java` y `AnalisisExpedienteDAO.java` hechos después de la verificación del subagente).
- Probar manualmente en la app: abrir un expediente en Análisis, confirmar que aparecen las dos grillas separadas, agregar/guardar/eliminar documentos padre e hijo con los iconos por fila, y descargar una plantilla Word desde el icono de fila padre.
- Ejecutar (con autorización explícita aparte) el script `41_datos_maestros_tipo_documento_carta_respuesta.sql` si se desea que "Carta de Respuesta" aparezca en el combo de tipo documento.
- Decidir si se hace commit/push de este conjunto de cambios.
