# Diagnostico grupo familiar en Registro / Recepcion SDRERC V2

Fecha: 2026-06-20  
Alcance: diagnostico funcional, tecnico y de base de datos. No incluye implementacion, scripts ni cambios de datos.

## 1. Resumen ejecutivo

La funcionalidad de **Grupo familiar** debe implementarse como una ayuda operativa para detectar solicitudes posiblemente relacionadas por vinculo familiar, no como una restriccion de registro ni como duplicidad.

El modelo actual no tiene una estructura transaccional activa para guardar grupo familiar. Existen tablas utiles para expediente, solicitud, personas, relaciones, observaciones y alertas, pero no existe campo o tabla especifica para grupo familiar. El script `18_datos_maestros_combos_registro_manual.sql` deja expresamente pendiente crear catalogos formales para grupo familiar y grado de parentesco antes de persistirlos fuera de observaciones.

La recomendacion experta para una primera fase es una solucion incremental:

- Persistir una marca simple en `EXPEDIENTE_SOLICITUD`.
- Usar alertas/observaciones para deteccion automatica no bloqueante.
- Mostrar badge y alertas en Registro / Recepcion, Asignacion y Consola.
- No usar todavia parentesco detallado ni una estructura completa de grupos familiares.
- No reutilizar la logica de duplicidad, porque duplicidad y grupo familiar tienen reglas distintas.

## 2. Diferencia funcional clave

### Duplicidad

- Regla vigente: misma acta + mismo titular.
- Puede representar el mismo caso registral o documento duplicado.
- Ya tiene reglas propias en Registro / Recepcion y Asignacion.
- Puede afectar la generacion de numero de expediente y la asociacion operativa.
- Se gestiona con `EXPEDIENTE_RELACION` para duplicados confirmados.

### Grupo familiar

- Personas distintas que podrian pertenecer a un mismo grupo familiar.
- No necesariamente comparten acta.
- No necesariamente son duplicados.
- No debe bloquear registro, importacion ni generacion de expediente.
- Sirve como alerta para ordenar revision y sugerir asignacion coordinada.

### Parentesco

- Relacion explicita entre personas o expedientes.
- Puede ser padre, madre, hijo(a), hermano(a), abuelo(a), etc.
- No siempre se puede inferir automaticamente.
- Debe tratarse como una fase posterior, porque requiere catalogo y relacion confirmada.

## 3. Diagnostico del modelo actual

### Estructuras disponibles

Tablas relevantes encontradas en scripts:

- `EXPEDIENTE`: cabecera operativa, etapa, estado, numero de expediente y vencimiento.
- `EXPEDIENTE_SOLICITUD`: datos de ingreso, canal, tramite, solicitante, observacion, duplicidad y referencia SGD.
- `PERSONA`: datos de personas con `NOMBRES` y `APELLIDOS`.
- `EXPEDIENTE_PERSONA`: relacion expediente-persona por tipo, por ejemplo titular o remitente.
- `EXPEDIENTE_ACTA`: datos de acta.
- `EXPEDIENTE_RELACION`: relaciones entre expedientes.
- `EXPEDIENTE_ALERTA`: alertas funcionales con tipo, mensaje, nivel y estado de atencion.
- `EXPEDIENTE_OBSERVACION`: observaciones con trazabilidad.

### Brechas actuales

- No existe `GRUPO_FAMILIAR` en `EXPEDIENTE`.
- No existe `GRUPO_FAMILIAR` en `EXPEDIENTE_SOLICITUD`.
- No existe tabla `GRUPO_FAMILIAR`.
- No existe tabla de parentesco aplicada a expedientes o personas.
- El titular se captura en la plantilla y UI como texto completo; no hay columnas separadas de apellido paterno, apellido materno y nombres.
- La deteccion por apellidos no es plenamente confiable con el modelo actual.

## 4. Donde guardar grupo familiar

| Alternativa | Evaluacion | Riesgo | Recomendacion |
| --- | --- | --- | --- |
| Campo en `EXPEDIENTE` | Simple, pero mezcla marca de ingreso con cabecera de flujo | Puede contaminar cabecera operativa | No recomendado para fase 1 |
| Campo en `EXPEDIENTE_SOLICITUD` | Representa bien un dato de recepcion/importacion | No modela integrantes | Recomendado fase 1 |
| `EXPEDIENTE_RELACION` con tipo `GRUPO_FAMILIAR` | Permite relacionar expedientes confirmados | Puede confundirse con duplicidad y activar logicas de asociados | Recomendado fase 2, solo con servicio separado |
| `EXPEDIENTE_ALERTA` | Buena para alertas automaticas no bloqueantes | No es fuente unica de verdad para marca manual | Complemento recomendado |
| `EXPEDIENTE_OBSERVACION` | Permite texto libre | Dificil filtrar, auditar y mantener | No usar como fuente principal |
| Nueva tabla de grupo familiar | Modelo mas correcto y escalable | Mayor esfuerzo UI/BD y reglas de parentesco | Fase 3 |

## 5. Recomendacion de modelo para fase 1

Agregar de forma futura, mediante script idempotente autorizado, campos en `EXPEDIENTE_SOLICITUD`:

```sql
GRUPO_FAMILIAR NUMBER(1) DEFAULT 0 NOT NULL
CRITERIO_GRUPO_FAMILIAR VARCHAR2(80)
OBSERVACION_GRUPO_FAMILIAR VARCHAR2(500)
```

Valores sugeridos para `CRITERIO_GRUPO_FAMILIAR`:

- `MANUAL`
- `EXCEL`
- `COINCIDENCIA_APELLIDOS_EXCEL`
- `COINCIDENCIA_APELLIDOS_BD`
- `CONFIRMADO_ASIGNACION`

Como complemento, usar `EXPEDIENTE_ALERTA` para alertas generadas:

- `TIPO_ALERTA = POSIBLE_GRUPO_FAMILIAR`
- `NIVEL = INFO` o `ADVERTENCIA`
- Mensaje visible: `Posible grupo familiar por coincidencia de apellidos.`

Esto permite separar:

- Marca manual o importada: `EXPEDIENTE_SOLICITUD`.
- Alertas detectadas: `EXPEDIENTE_ALERTA`.
- Relaciones confirmadas futuras: `EXPEDIENTE_RELACION` con tipo independiente.

## 6. Diagnostico de carga diaria Excel

Archivos impactados en una futura implementacion:

- `CargaDiariaPlantillaService`
- `CargaDiariaArchivoParserService`
- `CargaDiariaPreviewDTO`
- `CargaDiariaValidacionService`
- `JPanelCargaDiariaRecepcionV2`
- `ExpedienteRegistroDAO`

La plantilla actual ya maneja:

- encabezados oficiales;
- listas desplegables;
- columnas opcionales;
- previsualizacion editable;
- columnas del sistema calculadas;
- observacion con mensajes generados por validacion.

Para incorporar grupo familiar:

- Agregar columna `GRUPO FAMILIAR`.
- Lista desplegable: `Si`, `No`.
- Valor por defecto funcional: `No`.
- Debe aparecer antes de columnas calculadas del sistema en la previsualizacion.
- Debe ser editable en memoria porque viene del Excel.
- No debe sobrescribir ni eliminar mensajes de validacion en `Observacion`.
- Debe persistirse al confirmar importacion.

La columna no debe bloquear importacion si esta vacia. Si esta vacia, debe interpretarse como `No`.

## 7. Deteccion automatica dentro del Excel

Regla solicitada:

> Si dos o mas filas del Excel tienen el mismo apellido paterno y apellido materno del titular, mostrar alerta de posible grupo familiar.

Problema tecnico actual:

- La plantilla tiene `TITULAR` como texto completo.
- No existen columnas separadas de apellido paterno y apellido materno.
- El modelo `PERSONA` tiene `NOMBRES` y `APELLIDOS`, pero la captura actual no garantiza separacion confiable.

Opciones:

### Opcion 1: Heuristica conservadora sobre `TITULAR`

Inferir apellidos desde el texto completo.

Ventaja:

- No cambia plantilla de nombres.

Riesgo:

- Alta probabilidad de falsos positivos y falsos negativos.
- Nombres compuestos, apellidos compuestos, particulas como `DE`, `DEL`, `LA`, `LOS` y errores de tipeo reducen confiabilidad.

Uso recomendado:

- Solo como alerta de baja criticidad.
- Mensaje: `Posible grupo familiar por coincidencia de apellidos inferidos.`
- Nunca bloquear.

### Opcion 2: Agregar columnas de apellidos del titular

Columnas posibles:

- `APELLIDO PATERNO TITULAR`
- `APELLIDO MATERNO TITULAR`
- `NOMBRES TITULAR`

Ventaja:

- Deteccion mas confiable.
- Permite indexar y consultar mejor en BD.

Riesgo:

- Mayor impacto en plantilla, parser, registro manual, edicion y migracion de datos.

Uso recomendado:

- Fase posterior si la deteccion automatica se vuelve relevante.

## 8. Deteccion contra base de datos

La deteccion contra BD podria revisar si existen titulares previos con apellidos coincidentes.

Limitaciones actuales:

- No hay apellidos paterno/materno normalizados.
- Buscar por texto completo de titular puede ser costoso e impreciso.
- No conviene consultar todo el historico sin limites.

Recomendacion:

- Fase 1: deteccion simple limitada y no bloqueante.
- Limitar por expedientes activos y un rango temporal razonable, por ejemplo ultimos 12 meses o anio actual, si el usuario lo valida.
- Mostrar solo conteo y ejemplos principales, no saturar la UI.
- Si se requiere precision, agregar campos normalizados de apellidos en fase posterior.

No se recomienda crear relaciones automaticas en BD solo por coincidencia de apellidos. La coincidencia debe ser alerta, no confirmacion.

## 9. Registro manual

Cambios futuros sugeridos:

- Agregar checkbox `Grupo familiar`.
- Valor por defecto: desmarcado.
- Mostrar alerta no bloqueante si se detecta posible grupo familiar.
- Permitir marcar o desmarcar manualmente.
- Persistir el valor en `EXPEDIENTE_SOLICITUD`.
- Guardar criterio `MANUAL` si el usuario lo marca.

No debe:

- impedir guardar;
- consumir reglas de duplicidad;
- modificar la generacion de numero de expediente;
- asignar abogado automaticamente.

## 10. Edicion manual

La edicion manual actual solo debe permitirse segun las reglas vigentes del modulo, principalmente cuando el expediente sigue editable y no ha sido asignado.

Cambios futuros sugeridos:

- Mostrar checkbox `Grupo familiar`.
- Cargar valor desde BD.
- Permitir modificar si el expediente sigue editable.
- Registrar historial si cambia de valor.
- Mantener trazabilidad sin alterar numero de expediente.

Historial sugerido:

- `Actualizacion de marca Grupo familiar`.
- Incluir valor anterior y nuevo si el modelo de historial lo permite.

## 11. Asignacion

Objetivo para el asignador:

- Identificar solicitudes con posible grupo familiar.
- Sugerir asignacion coordinada al mismo abogado.
- No forzar automaticamente la asignacion.

Cambios futuros sugeridos:

- Badge en grilla: `Grupo familiar`.
- Filtro rapido: `Con grupo familiar`.
- Mostrar en panel derecho:
  - `Grupo familiar: Si/No/Possible`.
  - criterio de deteccion;
  - coincidencias encontradas;
  - expedientes sugeridos por apellidos;
  - abogado asignado si existe.

No debe mezclarse con:

- `Documentos asociados`;
- `Duplicado confirmado`;
- `Misma acta y titular`.

Si se permite confirmar relacion familiar desde Asignacion, debe ser una accion separada y usar un tipo de relacion distinto a duplicidad.

## 12. Consola Expediente

La consola ya muestra documentos asociados y datos centrales del expediente. Grupo familiar debe mostrarse separado para evitar confusion con duplicidad.

Seccion sugerida:

**Grupo familiar**

Contenido:

- Estado: `No`, `Si`, `Posible`.
- Criterio: `Manual`, `Excel`, `Coincidencia de apellidos`, `Confirmado en Asignacion`.
- Observacion.
- Integrantes detectados o sugeridos:
  - titular;
  - expediente;
  - fecha solicitud;
  - estado;
  - abogado asignado si existe.

No usar textos `padre` ni `hijo`.

## 13. Parentesco

La lista de parentesco mencionada es funcionalmente valida:

- Padre
- Madre
- Hijo(a)
- Abuelo(a)
- Hermano(a)
- Nieto(a)
- Bisabuelo(a)
- Tio(a)
- Sobrino(a)
- Biznieto(a)
- Primo(a)
- Otro

Sin embargo, no se recomienda incorporarla en la primera fase.

Motivos:

- Requiere catalogo formal.
- Requiere definir si el parentesco es entre personas, expedientes o integrantes de grupo.
- Requiere UI de confirmacion.
- Puede generar falsa precision si se infiere automaticamente.

Recomendacion:

- Fase 1: `Grupo familiar Si/No` y `Posible grupo familiar`.
- Fase 2: confirmar relacion entre expedientes.
- Fase 3: parentesco detallado.

## 14. Alternativas tecnicas

### Alternativa A: campo simple en `EXPEDIENTE_SOLICITUD`

Campos:

- `GRUPO_FAMILIAR`
- `CRITERIO_GRUPO_FAMILIAR`
- `OBSERVACION_GRUPO_FAMILIAR`

Ventajas:

- Simple.
- Bajo riesgo.
- Encaja con Registro / Recepcion.
- Facil de mostrar en bandejas, paneles y consola.
- No mezcla reglas con duplicidad.

Desventajas:

- No modela integrantes.
- No modela parentesco.
- No confirma relaciones entre expedientes.

Veredicto:

- Recomendada para fase 1.

### Alternativa B: `EXPEDIENTE_RELACION` con tipo `GRUPO_FAMILIAR`

Ventajas:

- Permite relacionar expedientes.
- Reutiliza patron de relaciones.
- Facilita consola e identificacion de integrantes.

Desventajas:

- Riesgo de mezclar con duplicidad.
- La logica actual de relacionados tiene efectos especificos para duplicados.
- Requiere servicio separado para evitar heredar numero, asignacion o exclusiones indebidas.

Veredicto:

- Recomendada para fase 2, solo para relaciones confirmadas manualmente.

### Alternativa C: nuevas tablas de grupo familiar

Tablas posibles:

- `GRUPO_FAMILIAR`
- `GRUPO_FAMILIAR_EXPEDIENTE`
- `GRUPO_FAMILIAR_PERSONA`
- `PARENTESCO`

Ventajas:

- Modelo mas correcto.
- Soporta integrantes, parentesco y administracion futura.
- Mejor para reportes.

Desventajas:

- Mayor complejidad.
- Requiere mas pantallas y reglas.
- No es necesario para alerta inicial.

Veredicto:

- Recomendada como fase 3 si el proceso funcional madura.

## 15. Plan incremental recomendado

### Fase 1: alerta y marca simple

- Agregar columna `GRUPO FAMILIAR` a plantilla Excel.
- Agregar checkbox en Registro Manual.
- Agregar checkbox en Edicion Manual.
- Persistir marca simple en `EXPEDIENTE_SOLICITUD`.
- Detectar posible grupo familiar dentro del Excel con regla conservadora.
- Mostrar observacion de validacion sin bloquear.
- Mostrar badge en Registro / Recepcion y Asignacion.
- Mostrar seccion simple en Consola.

### Fase 2: confirmacion operativa

- Permitir al asignador confirmar relacion de grupo familiar.
- Registrar relacion con tipo `GRUPO_FAMILIAR` en `EXPEDIENTE_RELACION`.
- Mostrar integrantes confirmados en Asignacion y Consola.
- Sugerir mismo abogado sin forzarlo automaticamente.

### Fase 3: parentesco y administracion avanzada

- Crear catalogo de parentesco.
- Crear estructura formal de grupo familiar.
- Permitir administrar integrantes y vinculos.
- Agregar reportes.

## 16. Riesgos

- Falsos positivos por apellidos comunes.
- Falsos negativos por nombres mal ingresados o apellidos compuestos.
- Confusion con duplicidad si se reutiliza visualmente la misma seccion.
- Performance si se busca contra todo el historico sin indices ni limites.
- Mayor complejidad si se intenta capturar parentesco desde la primera fase.
- Dificultad de normalizacion si no se separan apellidos.

## 17. Criterios de aceptacion para futura implementacion

- Grupo familiar no bloquea carga diaria ni registro manual.
- Duplicidad sigue siendo solo misma acta + titular.
- La previsualizacion muestra `Grupo familiar` antes de columnas calculadas.
- Las observaciones de validacion no se eliminan al mostrar grupo familiar.
- Registro Manual permite marcar/desmarcar.
- Edicion Manual carga y guarda la marca si el expediente sigue editable.
- Asignacion muestra badge y alerta sin forzar abogado.
- Consola muestra seccion separada de Grupo familiar.
- No se usan textos `padre` ni `hijo` en UI.
- No se hardcodean IDs.
- SQL queda en DAO/Service, no en JPanel.
- No se crean relaciones automaticas por coincidencia de apellidos.

## 18. Archivos probablemente impactados en implementacion futura

Registro / Recepcion:

- `src/main/java/com/sdrerc/ui/views/registrorecepcion/JPanelCargaDiariaRecepcionV2.java`
- `src/main/java/com/sdrerc/ui/views/registrorecepcion/JPanelRegistroManualRecepcionV2.java`
- `src/main/java/com/sdrerc/application/sdrercapp/CargaDiariaPlantillaService.java`
- `src/main/java/com/sdrerc/application/sdrercapp/CargaDiariaArchivoParserService.java`
- `src/main/java/com/sdrerc/application/sdrercapp/CargaDiariaValidacionService.java`
- `src/main/java/com/sdrerc/domain/dto/sdrercapp/CargaDiariaPreviewDTO.java`
- `src/main/java/com/sdrerc/domain/dto/sdrercapp/RegistroManualExpedienteDTO.java`
- `src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/ExpedienteRegistroDAO.java`
- `src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/ExpedienteEdicionManualDAO.java`

Asignacion:

- `src/main/java/com/sdrerc/ui/views/asignacion/JPanelAsignacionV2.java`
- `src/main/java/com/sdrerc/domain/dto/sdrercapp/AsignacionExpedienteDTO.java`
- `src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/AsignacionExpedienteDAO.java`

Consola:

- `src/main/java/com/sdrerc/ui/views/expedienteconsola/DlgConsolaExpedienteV2.java`
- `src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/ExpedienteConsolaDAO.java`

Base de datos futura:

- Script idempotente nuevo para campos de `EXPEDIENTE_SOLICITUD`.
- Script futuro separado si se habilita `EXPEDIENTE_RELACION` tipo `GRUPO_FAMILIAR`.
- Script futuro de parentesco solo si se aprueba fase 3.

## 19. Restricciones cumplidas en este diagnostico

- No se modifico codigo Java.
- No se modifico base de datos.
- No se crearon scripts SQL.
- No se ejecuto SQL.
- No se tocaron clases legacy.
- No se toco `OracleConnection.java`.
- No se altero la logica vigente de duplicidad.
- No se implemento grupo familiar como restriccion.
