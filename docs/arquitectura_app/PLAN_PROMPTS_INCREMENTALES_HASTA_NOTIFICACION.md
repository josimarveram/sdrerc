# Plan de prompts incrementales hasta Notificacion

Este documento contiene prompts listos para copiar y pegar en futuras sesiones de Codex. La secuencia toma como base `detalle_funciones.md`, `AGENTS.md` y el criterio funcional vigente de SDRERC V2.

## Premisas transversales

- Antes de implementar cualquier incremento, revisar el estado actual del codigo, la base documentada y los riesgos.
- Mantener SDRERC V2 separada de legacy.
- No tocar `OracleConnection.java`, `FrmLogin.java` legacy, `MenuPrincipal.java` legacy ni `com.sdrerc.Main` legacy.
- No crear etapa visual `VALIDACION`.
- No exponer `Firma / Emision` como modulo lateral independiente; la experiencia de firma, emision y documento emitido se concentra visualmente dentro de `Verificacion`.
- Usar `Emitido` como estado/documento resultado cuando corresponda.
- Las banderas `Requiere publicacion` y `Fecha de publicacion` se capturan o completan desde la operacion autorizada antes de Notificacion, se consultan en etapas posteriores y preparan el futuro modulo Publicacion sin implementarlo en esta secuencia.
- No inventar `Asignacion de respuesta` como etapa, estado, tabla o accion hasta nueva definicion funcional.
- Toda escritura V2 debe pasar por UI -> Service -> DAO, con transaccion, validacion de estado actual e historial cuando el modelo lo soporte.
- No usar `git add .`; agregar solo archivos de cada tarea.

---

## PROMPT 0 - Diagnostico maestro hasta Notificacion

```text
Antes de implementar, analiza cómo está actualmente, qué falta por mejorar, qué falta por completar, qué está mal conectado y qué riesgos existen.

Necesito un diagnostico funcional, tecnico y de base de datos del flujo SDRERC V2 desde Registro / Recepcion hasta Notificacion, usando como fuente funcional `docs/arquitectura_app/detalle_funciones.md` y respetando `AGENTS.md`.

Objetivo:
Generar una matriz clara de brechas y plan de ejecucion incremental para los modulos:

- Registro / Recepcion.
- Asignacion.
- Analisis.
- Verificacion integrada con firma/emision/documento emitido.
- Ejecucion.
- Notificacion.

No implementar cambios en esta fase.

Archivos y carpetas a revisar obligatoriamente:

- AGENTS.md.
- docs/arquitectura_app/detalle_funciones.md.
- docs/arquitectura_app/*.md relevantes.
- docs/arquitectura_bd/*.md relevantes.
- db/sdrerc_app/scripts/*.sql.
- src/main/java/com/sdrerc/ui/views/registrorecepcion/.
- src/main/java/com/sdrerc/ui/views/asignacion/.
- src/main/java/com/sdrerc/ui/views/analisis/.
- src/main/java/com/sdrerc/ui/views/verificacion/.
- src/main/java/com/sdrerc/ui/views/ejecucion/.
- src/main/java/com/sdrerc/ui/views/notificacion/.
- src/main/java/com/sdrerc/ui/views/expedienteconsola/.
- src/main/java/com/sdrerc/application/sdrercapp/.
- src/main/java/com/sdrerc/domain/dto/sdrercapp/.
- src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/.
- src/main/java/com/sdrerc/ui/appv2/MenuPrincipalV2.java.
- src/main/java/com/sdrerc/ui/appv2/components/.
- src/main/java/com/sdrerc/ui/appv2/theme/AppV2Theme.java.

Alcance del diagnostico:

- Comparar detalle funcional esperado contra implementacion actual.
- Identificar que ya esta completo.
- Identificar que falta por modulo.
- Identificar flujos mal conectados.
- Identificar estados/transiciones faltantes o inconsistentes.
- Identificar tablas/columnas/catalogos ya existentes y brechas.
- Identificar donde se usa `Firma / Emision` como modulo independiente y como debe integrarse visualmente en Verificacion.
- Confirmar que no se cree etapa visual VALIDACION.
- Confirmar como se maneja `Emitido`.
- Confirmar donde deben aparecer `Requiere publicacion` y `Fecha de publicacion`.
- Identificar riesgos de datos historicos, concurrencia y doble escritura.

No hacer:

- No modificar codigo Java.
- No crear scripts SQL.
- No ejecutar SQL.
- No tocar legacy.
- No tocar OracleConnection.java.
- No cambiar estructura de base de datos.
- No hacer commit si no se crea documento.

Reglas de base de datos:

- Solo revisar scripts y modelo documentado.
- No ejecutar INSERT, UPDATE, DELETE, MERGE, DROP ni TRUNCATE.
- Si se detectan scripts necesarios, proponerlos para prompts posteriores.

Reglas UI:

- Mantener nombres visuales amigables.
- No usar codigos tecnicos como texto principal.
- No usar `padre` ni `hijo` en UI; usar expedientes asociados, documentos asociados o relacion confirmada.
- Firma/emision debe quedar visualmente dentro de Verificacion.

Entregable:

- Documento o respuesta con matriz por modulo:
  - funcionalidad esperada;
  - implementado;
  - faltante;
  - archivos impactados;
  - tablas/catalogos impactados;
  - riesgo;
  - prompt recomendado para implementar.
- Orden de implementacion recomendado hasta Notificacion.
- Lista de scripts potenciales, sin crearlos.
- Confirmacion de que no se implemento codigo ni SQL.

Git:

- Ejecutar `git status`.
- Si se genera un documento de diagnostico, agregar solo ese documento.
- Commit sugerido: `docs: diagnose flow gaps up to notification`.
- Push al branch actual si corresponde.
```

---

## PROMPT 1 - Completar Registro / Recepcion

```text
Antes de implementar, analiza cómo está actualmente, qué falta por mejorar, qué falta por completar, qué está mal conectado y qué riesgos existen.

Necesito completar y pulir el modulo Registro / Recepcion de SDRERC V2 segun `detalle_funciones.md`, sin cambiar la logica legacy y respetando AGENTS.md.

Objetivo:
Cerrar el flujo de recepcion para registro individual, carga diaria, previsualizacion, validacion y edicion permitida de solicitudes registradas, manteniendo duplicidad y grupo familiar como alertas operativas no bloqueantes.

Archivos y carpetas a revisar:

- AGENTS.md.
- docs/arquitectura_app/detalle_funciones.md.
- docs/arquitectura_app/DIAGNOSTICO_GRUPO_FAMILIAR_REGISTRO_RECEPCION.md si existe.
- db/sdrerc_app/scripts/*.sql.
- src/main/java/com/sdrerc/ui/views/registrorecepcion/.
- src/main/java/com/sdrerc/application/sdrercapp/CargaDiariaPlantillaService.java.
- src/main/java/com/sdrerc/application/sdrercapp/CargaDiariaArchivoParserService.java.
- src/main/java/com/sdrerc/application/sdrercapp/CargaDiariaValidacionService.java.
- src/main/java/com/sdrerc/domain/dto/sdrercapp/CargaDiariaPreviewDTO.java.
- src/main/java/com/sdrerc/domain/dto/sdrercapp/RegistroManualExpedienteDTO.java.
- src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/ExpedienteRegistroDAO.java.
- src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/ExpedienteEdicionManualDAO.java.
- src/main/java/com/sdrerc/ui/views/expedienteconsola/.

Alcance funcional:

- Validar que el registro individual capture los datos obligatorios:
  - tipo de solicitud;
  - fecha solicitud;
  - solicitado por;
  - documento de identidad del solicitante;
  - numero de tramite web;
  - tipo documento;
  - tipo acta;
  - numero de acta;
  - nombre del titular;
  - numero de expediente SGD si aplica;
  - canal de recepcion;
  - fecha recepcion SDRERC como dato reportable, sin alterar fecha solicitud.
- Validar carga diaria desde plantilla oficial.
- Mantener previsualizacion editable solo para campos importados desde Excel; los campos calculados por sistema no deben editarse.
- Mantener observaciones de validacion visibles en previsualizacion.
- Evitar que la edicion de una celda altere los KPI de previsualizacion salvo despues de revalidar.
- Mantener listas desplegables estables en Excel; las opciones no deben duplicarse ni degradarse al digitar o pegar datos.
- Mantener duplicidad solo por `numero de acta + titular`.
- Si es potencial duplicado, guardar trazabilidad sin generar nuevo numero de expediente hasta que Asignacion confirme/asocie.
- Si el procedimiento registral es Reconsideracion o Apelacion, registrar sin numero de expediente y con observacion operativa.
- Calcular vencimiento por plazo configurado y dias habiles segun corresponda, sin hardcodear plazos en UI.
- Mostrar grupo familiar como alerta/marca no bloqueante, sin crear relaciones automaticas.
- Permitir edicion manual solo si el expediente esta en estado Registrado y no fue asignado.
- La edicion manual no debe cambiar el numero de expediente generado.

Reglas especiales:

- La fecha solicitud/origen sigue siendo la base funcional para plazos.
- La fecha recepcion SDRERC es dato adicional para reportes.
- No cambiar la derivacion automatica hacia `OR Pasivo` salvo regla funcional explicita futura.
- No generar expediente para Reconsideracion o Apelacion en Registro / Recepcion; el asignador decide despues.
- No crear relaciones en EXPEDIENTE_RELACION desde Registro / Recepcion por grupo familiar.

No hacer:

- No tocar legacy.
- No tocar OracleConnection.java.
- No ejecutar SQL sin autorizacion explicita.
- No recalcular historicos.
- No modificar datos productivos.
- No poner SQL en JPanel.
- No crear una integracion directa con SGD o SITD.
- No cambiar el flujo de duplicidad vigente.

Reglas de base de datos:

- Revisar si las columnas necesarias ya existen.
- Si falta una columna imprescindible, crear script SQL idempotente separado y justificarlo.
- No usar DROP, DELETE ni TRUNCATE.
- No actualizar historicos masivamente.

Reglas UI:

- Mantener diseno institucional V2.
- No saturar la bandeja con columnas tecnicas.
- Tooltips en textos largos.
- Indicadores claros para duplicidad, grupo familiar, Reconsideracion y Apelacion sin bloquear registro.
- La bandeja debe cargar automaticamente segun filtros por defecto.

Reglas Service/DAO:

- La UI solo debe llamar a Services.
- Services llaman a DAOs.
- Toda escritura autorizada debe ser transaccional y registrar historial cuando el modelo lo soporte.
- Resolver IDs por codigo/catalogo, no hardcodear IDs.

Criterios de aceptacion:

- Registro manual guarda solicitudes normales.
- Registro manual guarda Reconsideracion/Apelacion sin numero.
- Carga diaria valida, permite previsualizar y confirmar.
- La previsualizacion permite editar datos importados sin alterar KPI hasta revalidar.
- Las observaciones de validacion se preservan.
- Las listas Excel no se corrompen al pegar datos.
- Duplicidad acta + titular no bloquea, pero marca y evita correlativo cuando corresponde.
- Grupo familiar no bloquea ni se mezcla con duplicidad.
- Edicion manual solo se habilita para Registrado no asignado.
- No se toca legacy ni OracleConnection.java.

Compilacion:

- Ejecutar `mvn clean compile`.
- Si compila, ejecutar `mvn clean package`.
- Validar `.\run-v2.ps1` o indicar razon si no se ejecuta para no dejar la app abierta.

Git:

- Ejecutar `git status`.
- No usar `git add .`.
- Agregar solo archivos de esta tarea.
- Commit sugerido: `feat: complete registro recepcion intake flow`.
- Push al branch actual si el build pasa.
```

---

## PROMPT 2 - Completar Asignacion

```text
Antes de implementar, analiza cómo está actualmente, qué falta por mejorar, qué falta por completar, qué está mal conectado y qué riesgos existen.

Necesito completar y pulir el modulo Asignacion de SDRERC V2 como bandeja operativa del asignador, segun `detalle_funciones.md` y AGENTS.md.

Objetivo:
Cerrar el flujo de asignacion para expedientes registrados, solicitudes sin numero por duplicidad/Reconsideracion/Apelacion, asignacion individual/masiva, hoja de envio, documentos asociados, grupo familiar, carga de trabajo y cartas de respuesta.

Archivos y carpetas a revisar:

- AGENTS.md.
- docs/arquitectura_app/detalle_funciones.md.
- docs/arquitectura_app/*.md relevantes.
- db/sdrerc_app/scripts/*.sql.
- src/main/java/com/sdrerc/ui/views/asignacion/.
- src/main/java/com/sdrerc/application/sdrercapp/*Asignacion*.
- src/main/java/com/sdrerc/domain/dto/sdrercapp/AsignacionExpedienteDTO.java.
- src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/AsignacionExpedienteDAO.java.
- Services/DAOs de expediente relacionado/documento asociado.
- DAOs de catalogos de tipo documento, procedimiento, estado, equipo y abogado.
- src/main/java/com/sdrerc/ui/views/expedienteconsola/.

Alcance funcional:

- Bandeja de nuevos registros y pendientes de asignacion.
- Mantener busqueda por:
  - numero expediente;
  - tramite web;
  - expediente SGD;
  - acta;
  - titular;
  - documento de identidad.
- Ordenar por dias restantes y titular segun regla vigente.
- Mostrar solicitudes sin numero por duplicidad, Reconsideracion o Apelacion.
- Permitir al asignador:
  - asociar duplicado a expediente principal cuando corresponda;
  - generar numero de expediente para Reconsideracion o Apelacion si decide tratarlo como expediente nuevo;
  - mantener Reconsideracion o Apelacion sin numero si se asocia al principal;
  - corregir procedimiento registral solo a Reconsideracion o Apelacion cuando el estado lo permita.
- No permitir edicion de tipo documento desde Datos registrales.
- Asignacion individual y masiva con captura obligatoria de hoja de envio por expediente cuando corresponda.
- Validar que numero de hoja de envio sea unico antes de grabar asignacion.
- Si se asocia duplicado a principal, heredar equipo y abogado del principal cuando corresponda.
- Mostrar y gestionar `Cartas de respuesta` en el panel de Asignacion usando documentos de analisis ya registrados.
- En Cartas de respuesta permitir registrar:
  - Notificado Si/No;
  - Fecha acuse;
  - Requiere respuesta como dato bloqueado desde Analisis;
  - Confirmacion de respuesta Si/No/Pendiente;
  - Fecha respuesta;
  - Hoja de envio de respuesta.
- Capturar o completar `Requiere publicacion` y `Fecha de publicacion` cuando el documento/caso lo requiera, para uso posterior en Notificacion/Publicacion.
- Mostrar grupo familiar como sugerencia de asignacion coordinada, sin forzar abogado.
- Permitir consulta de carga de trabajo del abogado en ventana o panel claro.

Reglas para documentos asociados:

- No usar textos `padre` ni `hijo` en UI.
- Usar `expediente principal`, `documento asociado`, `duplicado confirmado` o `relacion confirmada`.
- Documentos asociados no operativos no se asignan de forma independiente.
- La grilla expandible debe mantener carga diferida y acordeon si ya existe.

No hacer:

- No tocar legacy.
- No tocar OracleConnection.java.
- No ejecutar SQL sin autorizacion explicita.
- No crear etapa `Asignacion de respuesta`.
- No crear etapa visual VALIDACION.
- No poblar EXPEDIENTE_DOCUMENTO_ANALIZADO desde Asignacion.
- No asignar automaticamente por grupo familiar.
- No cambiar reglas de duplicidad.
- No poner SQL en JPanel.

Reglas de base de datos:

- Verificar si campos de hoja de envio y respuesta existen antes de usarlos.
- Crear scripts idempotentes solo si falta soporte minimo imprescindible.
- Resolver estados, etapas, transiciones y catalogos por codigo.
- Toda escritura debe validar estado actual y ser transaccional.
- No usar DROP, DELETE ni TRUNCATE.

Reglas UI:

- Mantener el patron visual vigente de Asignacion.
- Panel derecho contextual oculto inicialmente.
- Panel individual si hay una seleccion.
- Panel de asignacion multiple si hay varias casillas seleccionadas.
- En asignacion multiple, tabla con expediente, expediente SGD y hoja de envio editable por fila.
- No mostrar textos de ayuda redundantes que ya se eliminaron.
- Usar badges sobrios para grupo familiar, duplicidad, sin numero, Reconsideracion/Apelacion y asociados.

Reglas Service/DAO:

- UI llama Service.
- Service llama DAO.
- Escritura transaccional para asignacion individual, multiple, asociacion, generacion de numero y actualizacion de cartas.
- Registrar historial cuando el modelo lo soporte.

Criterios de aceptacion:

- Una seleccion abre panel individual.
- Varias selecciones abren panel multiple.
- Hoja de envio se captura y valida unicidad.
- Reconsideracion/Apelacion sin numero pueden asociarse o generar numero desde Asignacion.
- Duplicados por acta + titular se asocian sin convertirse en documentos analizados.
- Equipo/abogado se sincronizan segun reglas vigentes.
- Grupo familiar se visualiza como sugerencia no obligatoria.
- Cartas de respuesta se actualizan sin crear etapa nueva.
- Requiere publicacion y Fecha publicacion quedan disponibles para Notificacion/Publicacion segun modelo.
- No se toca legacy ni OracleConnection.java.

Compilacion:

- Ejecutar `mvn clean compile`.
- Si compila, ejecutar `mvn clean package`.
- Validar `.\run-v2.ps1` o indicar razon si no se ejecuta.

Git:

- Ejecutar `git status`.
- No usar `git add .`.
- Agregar solo archivos de esta tarea.
- Commit sugerido: `feat: complete asignacion operational flow`.
- Push al branch actual si el build pasa.
```

---

## PROMPT 3 - Completar Analisis

```text
Antes de implementar, analiza cómo está actualmente, qué falta por mejorar, qué falta por completar, qué está mal conectado y qué riesgos existen.

Necesito completar el modulo Analisis de SDRERC V2 separando claramente documentos de analisis, cartas intermedias y resultado final, segun `detalle_funciones.md` y AGENTS.md.

Objetivo:
Permitir que el abogado reciba expedientes, registre documentos intermedios o documentos de analisis sin cerrar indebidamente el analisis, y solo registre resultado final cuando corresponda.

Archivos y carpetas a revisar:

- AGENTS.md.
- docs/arquitectura_app/detalle_funciones.md.
- docs/arquitectura_app/*.md relevantes.
- docs/arquitectura_bd/TO BE V2.bpmn.
- db/sdrerc_app/scripts/*.sql.
- src/main/java/com/sdrerc/ui/views/analisis/.
- src/main/java/com/sdrerc/application/sdrercapp/*Analisis*.
- src/main/java/com/sdrerc/domain/dto/sdrercapp/*Analisis*.
- src/main/java/com/sdrerc/domain/dto/sdrercapp/DocumentoAnalizadoDTO.java.
- src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/*Analisis*.
- src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/DocumentoAnalisisDAO.java.
- src/main/java/com/sdrerc/ui/views/asignacion/.
- src/main/java/com/sdrerc/ui/views/verificacion/.
- src/main/java/com/sdrerc/ui/views/expedienteconsola/.

Alcance funcional:

- Recibir expedientes asignados.
- Al recibir expediente principal, recibir documentos asociados ya asignados al mismo abogado segun regla vigente.
- Si un documento asociado se vincula despues de la recepcion principal, mostrarlo pendiente de recibir y permitir recepcion individual solo al abogado responsable.
- Separar acciones:
  - Guardar documento de analisis.
  - Registrar resultado final de analisis.
  - Enviar a Verificacion.
- Permitir registrar `Carta intermedia` u otros documentos de tipo `ANALISIS_%` sin exigir resultado final.
- Mantener expediente en Analisis cuando solo se registra carta/documento intermedio.
- No mover a Verificacion hasta que el abogado confirme resultado final o envio correspondiente.
- Gestionar `No corresponde a SDRERC`:
  - habilitar N° Documento (Proveido) siempre;
  - para No corresponde, bloquear evaluaciones y documentos analizados;
  - exigir/procesar Proveido si la regla vigente lo requiere;
  - archivar o cerrar solo si existe transicion real y autorizada.
- Analizar:
  - sustento;
  - precisa pretension;
  - acta incorporada;
  - coincidencia con tablas maestras;
  - reconstitucion;
  - legitimidad;
  - medios probatorios;
  - procedente;
  - procedente en parte;
  - improcedente.
- Registrar cartas intermedias:
  - carta edicto;
  - carta falta sustento;
  - carta indagatoria;
  - carta precisar pretension;
  - otras definidas por catalogo `ANALISIS_%`.
- Registrar `Requiere respuesta` cuando aplique para que Asignacion complete carta de respuesta.
- Mostrar `Requiere publicacion` y `Fecha de publicacion` como datos consultables si ya fueron capturados antes; no inventar flujo de publicacion aqui.
- Mostrar todos los documentos de analisis en grilla sin scroll vertical interno, con opcion de editar estado u otros campos autorizados.
- Enviar todos los documentos relevantes a Verificacion.

Reglas de carta intermedia:

- Guardar carta intermedia no debe obligar a resultado final.
- Guardar carta intermedia no debe cerrar analisis.
- Si la carta requiere respuesta, el expediente puede quedar en estado de espera solo si existe estado/transicion real.
- Si no existe estado real, mantenerlo en Analisis y mostrar alerta operativa.
- Si vence plazo de respuesta sin respuesta, proponer abandono solo si el flujo real lo soporta.

No hacer:

- No tocar legacy.
- No tocar OracleConnection.java.
- No ejecutar SQL sin autorizacion explicita.
- No crear etapa `Asignacion de respuesta`.
- No exponer boton `Derivar a notificacion`.
- No exponer boton `Derivacion externa`.
- No crear etapa visual VALIDACION.
- No insertar documentos de respuesta desde Analisis si corresponden al asignador.
- No poner SQL en JPanel.

Reglas de base de datos:

- Cargar tipos de documentos desde `TIPO_DOCUMENTO_ADJUNTO` con codigo `ANALISIS_%`.
- No mostrar `PROVEIDO` en combo de documentos analizados.
- Si falta metadata para carta intermedia, diagnosticar y crear script idempotente solo con autorizacion.
- No usar DROP, DELETE ni TRUNCATE.
- No hardcodear IDs.

Reglas UI:

- Mantener patron visual de Asignacion/Verificacion.
- Panel derecho desde altura de KPI hasta pie de pantalla.
- Chip premium de expandir/restaurar.
- Grilla de documentos con columnas:
  - Tipo;
  - Estado;
  - Fecha;
  - Descripcion;
  - Notificado;
  - Fecha Acuse;
  - Requiere respuesta;
  - Confirmacion de respuesta;
  - Fecha Respuesta;
  - Hoja de Envio;
  - Accion editar/guardar segun corresponda.
- Estados y badges amigables.
- No mostrar codigos tecnicos.

Reglas Service/DAO:

- UI llama Service.
- Service llama DAO.
- Guardar documento y registrar resultado final deben ser operaciones separadas.
- Toda escritura debe validar estado actual y transaccion.
- Registrar historial/movimiento si el modelo lo soporta.

Criterios de aceptacion:

- El abogado puede guardar Carta intermedia sin seleccionar resultado final.
- Guardar documento no mueve expediente indebidamente.
- Registrar resultado final sigue funcionando.
- Enviar a Verificacion solo ocurre con accion explicita y transicion real.
- Documentos asociados pendientes se reciben individualmente solo por abogado responsable.
- No corresponde mantiene reglas vigentes.
- No aparecen acciones de notificacion ni derivacion externa.
- No se toca legacy ni OracleConnection.java.

Compilacion:

- Ejecutar `mvn clean compile`.
- Si compila, ejecutar `mvn clean package`.
- Validar `.\run-v2.ps1` o indicar razon si no se ejecuta.

Git:

- Ejecutar `git status`.
- No usar `git add .`.
- Agregar solo archivos de esta tarea.
- Commit sugerido: `feat: separate analysis documents from final result`.
- Push al branch actual si el build pasa.
```

---

## PROMPT 4 - Completar Verificacion integrada con Firma / Emision

```text
Antes de implementar, analiza cómo está actualmente, qué falta por mejorar, qué falta por completar, qué está mal conectado y qué riesgos existen.

Necesito completar el modulo Verificacion de SDRERC V2 integrando visualmente las responsabilidades de firma, emision, numeracion y documento emitido, sin reactivar `Firma / Emision` como modulo independiente.

Objetivo:
Que Verificacion cubra la revision del analisis, documentos, observaciones, aprobacion, devolucion a Analisis y gestion visual de documento emitido/firma/emision segun transiciones reales.

Archivos y carpetas a revisar:

- AGENTS.md.
- docs/arquitectura_app/detalle_funciones.md.
- docs/arquitectura_bd/TO BE V2.bpmn.
- db/sdrerc_app/scripts/*.sql.
- src/main/java/com/sdrerc/ui/views/verificacion/.
- src/main/java/com/sdrerc/ui/views/firmaemision/ solo para diagnostico/reutilizacion, sin exponer modulo independiente.
- src/main/java/com/sdrerc/application/sdrercapp/*Verificacion*.
- src/main/java/com/sdrerc/application/sdrercapp/*Firma*.
- src/main/java/com/sdrerc/domain/dto/sdrercapp/*Verificacion*.
- src/main/java/com/sdrerc/domain/dto/sdrercapp/*Firma*.
- src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/*Verificacion*.
- src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/*Firma*.
- src/main/java/com/sdrerc/ui/appv2/MenuPrincipalV2.java.
- src/main/java/com/sdrerc/ui/views/expedienteconsola/.

Alcance funcional:

- Mostrar expedientes en Verificacion y en estados reales de firma/emision cuando correspondan.
- Revisar consistencia de documentos elevados por Analisis.
- Ver documentos de analisis completos, cartas intermedias y resultados.
- Permitir cambiar estado de documentos revisados segun regla vigente.
- Registrar observacion y devolver a Analisis cuando exista transicion real.
- Aprobar Verificacion.
- Concentrar en Verificacion controles de:
  - firma;
  - emision;
  - numero de resolucion/documento;
  - estado visual `Emitido`;
  - envio a Ejecucion cuando aplique.
- Carta edicto y otros documentos que requieran firma de Subdirector deben gestionarse visualmente dentro de Verificacion si el modelo/transicion lo soporta.
- Diferenciar destino:
  - resoluciones procedentes/procedentes en parte/improcedentes hacia Ejecucion;
  - otros documentos hacia Notificacion solo si existe transicion real.
- Mostrar `Requiere publicacion` y `Fecha de publicacion` si estan disponibles, sin implementar Publicacion.

Reglas especiales:

- No crear modulo lateral independiente Firma / Emision.
- No crear rutas paralelas a las transiciones reales.
- Usar `Emitido` como texto visible cuando corresponda.
- Si una transicion, catalogo o tabla falta, bloquear con diagnostico y no hacer escritura parcial.

No hacer:

- No tocar legacy.
- No tocar OracleConnection.java.
- No ejecutar SQL sin autorizacion explicita.
- No crear etapa visual VALIDACION.
- No inventar accion `EMISION_DOCUMENTO` si no existe como transicion real.
- No crear tabla paralela de Verificacion.
- No ocultar errores funcionales con cambios visuales.
- No poner SQL en JPanel.

Reglas de base de datos:

- Usar tablas reales:
  - EXPEDIENTE;
  - EXPEDIENTE_HISTORIAL;
  - EXPEDIENTE_OBSERVACION;
  - EXPEDIENTE_EVALUACION;
  - EXPEDIENTE_DOCUMENTO_ANALIZADO;
  - EXPEDIENTE_RESOLUCION si corresponde.
- Resolver transiciones por codigo.
- No hardcodear IDs.
- No usar DROP, DELETE ni TRUNCATE.

Reglas UI:

- Mantener patron visual operativo.
- Panel contextual premium con chip de expandir/restaurar.
- No mostrar codigos tecnicos.
- Mostrar badges para Verificacion, Observado, Aprobado, Emitido, Requiere publicacion.
- Si firma/emision es un bloque del panel, debe estar claramente dentro de Verificacion.
- No usar `V2` en titulos visibles.

Reglas Service/DAO:

- UI llama Service.
- Service llama DAO.
- Escrituras transaccionales con validacion de estado actual.
- Registrar historial/movimiento.
- No hacer commits parciales si falla firma, emision o cambio de estado.

Criterios de aceptacion:

- Verificacion revisa documentos de Analisis.
- Se puede observar y devolver a Analisis si hay transicion real.
- Se puede aprobar Verificacion.
- Firma/emision/documento emitido se gestionan dentro de Verificacion.
- El menu lateral no expone Firma / Emision independiente.
- Estado visual `Emitido` aparece donde corresponde.
- Las resoluciones pueden avanzar a Ejecucion solo con transicion valida.
- Otros documentos pueden avanzar a Notificacion solo con transicion valida.
- No se toca legacy ni OracleConnection.java.

Compilacion:

- Ejecutar `mvn clean compile`.
- Si compila, ejecutar `mvn clean package`.
- Validar `.\run-v2.ps1` o indicar razon si no se ejecuta.

Git:

- Ejecutar `git status`.
- No usar `git add .`.
- Agregar solo archivos de esta tarea.
- Commit sugerido: `feat: complete verification with emitted document controls`.
- Push al branch actual si el build pasa.
```

---

## PROMPT 5 - Completar Ejecucion

```text
Antes de implementar, analiza cómo está actualmente, qué falta por mejorar, qué falta por completar, qué está mal conectado y qué riesgos existen.

Necesito completar el modulo Ejecucion de SDRERC V2 segun `detalle_funciones.md`, conectado con Verificacion integrada y antes de Notificacion.

Objetivo:
Permitir que Ejecucion gestione resoluciones/documentos emitidos, detecte error material, derive al abogado responsable, registre resultado de ejecucion y prepare documentos de notificacion sin inventar rutas.

Archivos y carpetas a revisar:

- AGENTS.md.
- docs/arquitectura_app/detalle_funciones.md.
- docs/arquitectura_bd/TO BE V2.bpmn.
- db/sdrerc_app/scripts/*.sql.
- src/main/java/com/sdrerc/ui/views/ejecucion/.
- src/main/java/com/sdrerc/application/sdrercapp/*Ejecucion*.
- src/main/java/com/sdrerc/domain/dto/sdrercapp/*Ejecucion*.
- src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/*Ejecucion*.
- src/main/java/com/sdrerc/ui/views/verificacion/.
- src/main/java/com/sdrerc/ui/views/analisis/.
- src/main/java/com/sdrerc/ui/views/notificacion/.
- src/main/java/com/sdrerc/ui/views/expedienteconsola/.

Alcance funcional:

- Consultar expedientes en Ejecucion.
- Revisar resolucion/documento emitido desde Verificacion.
- Registrar recepcion o revision de resolucion/documento si existe accion real.
- Validar error material.
- Si hay error material:
  - registrar observacion/motivo;
  - devolver a Analisis solo con transicion real;
  - registrar hoja de envio si el modelo lo requiere;
  - preservar resolucion y documentos previos;
  - no borrar archivos ni metadata.
- Si no hay error:
  - derivar al mismo abogado que realizo Analisis cuando la regla aplique;
  - permitir acciones de ejecucion segun procedimiento.
- Para improcedentes, preparar/proyectar carta de notificacion si el flujo lo soporta.
- Para procedente o procedente en parte, registrar anotacion/resultado de ejecucion y preparar carta de notificacion.
- Las cartas de notificacion deben quedar para validacion del supervisor antes de Notificacion si el modelo lo soporta.
- Derivar a Notificacion solo con transicion real activa.
- Mostrar documentos asociados y grupo familiar como contexto, no como nueva logica.

No hacer:

- No tocar legacy.
- No tocar OracleConnection.java.
- No ejecutar SQL sin autorizacion explicita.
- No crear etapa visual VALIDACION.
- No crear tabla paralela de Ejecucion.
- No inventar estados ni acciones no definidas.
- No mover fisicamente archivos.
- No enviar correos, SMS ni integraciones externas.
- No poner SQL en JPanel.

Reglas de base de datos:

- Usar tablas reales:
  - EXPEDIENTE;
  - EXPEDIENTE_HISTORIAL;
  - EXPEDIENTE_OBSERVACION;
  - EXPEDIENTE_RESOLUCION;
  - EXPEDIENTE_DOCUMENTO_ANALIZADO;
  - documentos/metadata existentes.
- Resolver transiciones por codigo.
- No hardcodear IDs.
- No usar DROP, DELETE ni TRUNCATE.

Reglas UI:

- Replicar patron visual de Verificacion.
- Buscador con fecha solicitud, estado y mostrar.
- Panel contextual premium.
- Mostrar documento emitido, resultado de verificacion, resolucion y alertas.
- Badges para error material, listo para notificacion, observado, ejecutado.
- No mostrar columnas tecnicas.

Reglas Service/DAO:

- UI llama Service.
- Service llama DAO.
- Transacciones completas para devolucion, inicio/resultado de ejecucion y derivacion.
- Registrar historial/movimiento.
- Validar estado actual antes de escribir.

Criterios de aceptacion:

- Ejecucion carga bandeja.
- Ver detalle abre DlgConsolaExpedienteV2.
- Error material devuelve a Analisis solo con transicion valida.
- Sin error permite avanzar flujo de ejecucion.
- Se conserva abogado responsable cuando aplique.
- Se preparan documentos de notificacion sin saltar supervisor ni inventar etapa.
- Derivacion a Notificacion solo ocurre con transicion real.
- No se toca legacy ni OracleConnection.java.

Compilacion:

- Ejecutar `mvn clean compile`.
- Si compila, ejecutar `mvn clean package`.
- Validar `.\run-v2.ps1` o indicar razon si no se ejecuta.

Git:

- Ejecutar `git status`.
- No usar `git add .`.
- Agregar solo archivos de esta tarea.
- Commit sugerido: `feat: complete ejecucion operational flow`.
- Push al branch actual si el build pasa.
```

---

## PROMPT 6 - Completar Notificacion hasta antes de Publicacion

```text
Antes de implementar, analiza cómo está actualmente, qué falta por mejorar, qué falta por completar, qué está mal conectado y qué riesgos existen.

Necesito completar el modulo Notificacion de SDRERC V2 hasta el punto previo a Publicacion, segun `detalle_funciones.md` y AGENTS.md.

Objetivo:
Permitir que Notificacion gestione validacion de cartas, asignacion/atencion de notificacion, intentos de notificacion, acuse, resultado y preparacion de publicacion sin implementar Publicacion todavia.

Archivos y carpetas a revisar:

- AGENTS.md.
- docs/arquitectura_app/detalle_funciones.md.
- docs/arquitectura_bd/TO BE V2.bpmn.
- db/sdrerc_app/scripts/*.sql.
- src/main/java/com/sdrerc/ui/views/notificacion/.
- src/main/java/com/sdrerc/application/sdrercapp/*Notificacion*.
- src/main/java/com/sdrerc/domain/dto/sdrercapp/*Notificacion*.
- src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/*Notificacion*.
- src/main/java/com/sdrerc/ui/views/ejecucion/.
- src/main/java/com/sdrerc/ui/views/verificacion/.
- src/main/java/com/sdrerc/ui/views/publicacion/ solo para diagnostico, sin implementar Publicacion completa.
- src/main/java/com/sdrerc/ui/views/expedienteconsola/.

Alcance funcional:

- Consultar expedientes/documentos pendientes de notificacion.
- Supervisor revisa carta de notificacion.
- Si la carta tiene error o inconsistencia, devolver a Ejecucion con motivo solo si existe transicion real.
- Si esta correcta, validar/firmar o marcar como lista para notificar segun flujo real.
- Validar documentos a notificar.
- Si existe asignacion a abogados de notificacion, implementarla solo con modelo y transicion reales; si no, dejar diagnostico.
- Registrar notificacion por documento:
  - tipo de notificacion;
  - fecha de notificacion;
  - si tiene acuse;
  - fecha/cargo de acuse si aplica;
  - resultado.
- Manejar hasta tres intentos:
  - primer intento virtual;
  - segundo intento presencial/fisico;
  - tercer intento presencial/fisico.
- Registrar notificacion fallida cuando correspondan intentos agotados.
- Consumir `Requiere publicacion` y `Fecha de publicacion` registrados previamente.
- Si requiere publicacion, preparar estado/datos para Publicacion solo con transicion real, sin implementar la pantalla Publicacion en este incremento.
- Si no requiere publicacion y el flujo lo permite, continuar hacia cierre/archivo o expediente digital segun transicion real.

Reglas especiales:

- No implementar publicacion real.
- No publicar en portales externos.
- No enviar correos, SMS, WhatsApp ni integraciones externas.
- Notificacion registra metadata y trazabilidad funcional.
- Requiere publicacion no debe inventar etapa ni estado si no existe transicion activa.

No hacer:

- No tocar legacy.
- No tocar OracleConnection.java.
- No ejecutar SQL sin autorizacion explicita.
- No crear etapa visual VALIDACION.
- No crear tabla paralela de Notificacion.
- No implementar Publicacion en este prompt.
- No crear acciones no definidas para asignacion de respuesta.
- No poner SQL en JPanel.

Reglas de base de datos:

- Usar tablas reales:
  - EXPEDIENTE_NOTIFICACION;
  - EXPEDIENTE_CARGO_ACUSE;
  - EXPEDIENTE_PUBLICACION solo si se prepara metadata permitida;
  - EXPEDIENTE_HISTORIAL;
  - EXPEDIENTE_RESOLUCION;
  - EXPEDIENTE;
  - documentos existentes.
- Resolver transiciones por codigo.
- No hardcodear IDs.
- No usar DROP, DELETE ni TRUNCATE.

Reglas UI:

- Replicar patron visual de Verificacion/Ejecucion.
- Panel contextual premium.
- Mostrar intentos de notificacion como bloque claro por documento.
- Badges para pendiente, notificado, fallido, requiere publicacion, acuse recibido.
- Mostrar Fecha publicacion solo como dato operativo cuando corresponda.
- No mostrar codigos tecnicos.

Reglas Service/DAO:

- UI llama Service.
- Service llama DAO.
- Cada intento de notificacion debe registrarse transaccionalmente.
- Validar estado actual y numero de intento.
- Registrar historial/movimiento.

Criterios de aceptacion:

- Notificacion carga bandeja.
- Supervisor puede validar carta o devolver a Ejecucion con motivo si transicion existe.
- Se registran intentos por documento.
- Se registra acuse si aplica.
- Se manejan hasta tres intentos sin duplicar.
- Requiere publicacion queda preparado, pero Publicacion no se implementa.
- No se toca legacy ni OracleConnection.java.

Compilacion:

- Ejecutar `mvn clean compile`.
- Si compila, ejecutar `mvn clean package`.
- Validar `.\run-v2.ps1` o indicar razon si no se ejecuta.

Git:

- Ejecutar `git status`.
- No usar `git add .`.
- Agregar solo archivos de esta tarea.
- Commit sugerido: `feat: complete notification flow before publication`.
- Push al branch actual si el build pasa.
```

---

## PROMPT 7 - QA visual e integracion hasta Notificacion

```text
Antes de implementar, analiza cómo está actualmente, qué falta por mejorar, qué falta por completar, qué está mal conectado y qué riesgos existen.

Necesito una pasada integral de QA visual, funcional y de integracion desde Registro / Recepcion hasta Notificacion en SDRERC V2.

Objetivo:
Validar que los modulos operativos hasta Notificacion tengan experiencia visual consistente, busquedas estandarizadas, paneles contextuales, grillas, consola de expediente y transiciones conectadas sin romper reglas de negocio.

Archivos y carpetas a revisar:

- AGENTS.md.
- docs/arquitectura_app/detalle_funciones.md.
- docs/arquitectura_app/*.md relevantes.
- src/main/java/com/sdrerc/ui/views/registrorecepcion/.
- src/main/java/com/sdrerc/ui/views/asignacion/.
- src/main/java/com/sdrerc/ui/views/analisis/.
- src/main/java/com/sdrerc/ui/views/verificacion/.
- src/main/java/com/sdrerc/ui/views/ejecucion/.
- src/main/java/com/sdrerc/ui/views/notificacion/.
- src/main/java/com/sdrerc/ui/views/expedienteconsola/.
- src/main/java/com/sdrerc/ui/appv2/components/.
- src/main/java/com/sdrerc/ui/appv2/theme/AppV2Theme.java.
- src/main/java/com/sdrerc/ui/appv2/MenuPrincipalV2.java.
- Services/DAOs/DTOs de los modulos mencionados.

Alcance de QA:

- Cards superiores uniformes.
- Filtros de busqueda por defecto con fechas desde/hasta.
- Busqueda por expediente, tramite web, expediente SGD, acta, titular y documento de identidad donde corresponda.
- Orden por dias restantes y titular segun regla vigente.
- Columna Dias con dias habiles restantes y badge.
- Tooltips en textos largos.
- Sin columnas tecnicas visibles.
- Sin encabezados truncados.
- Panel derecho contextual:
  - oculto inicialmente;
  - visible al seleccionar;
  - alto desde KPI hasta pie;
  - chip de expandir/restaurar;
  - X sin limpiar seleccion;
  - scroll vertical interno cuando corresponde, sin scroll horizontal.
- Grillas expandibles de documentos asociados donde aplique:
  - carga diferida;
  - icono consistente;
  - no usar textos padre/hijo;
  - no mostrar IDs internos.
- Consola de expediente:
  - muestra plazo legible;
  - permite maximizar;
  - secciones claras;
  - no muestra pills tecnicos innecesarios.
- Firma/emision no aparece como modulo lateral independiente.
- Verificacion muestra estado visual Emitido donde corresponda.
- Requiere publicacion y Fecha publicacion se ven/usan en los puntos definidos.

No hacer:

- No tocar legacy.
- No tocar OracleConnection.java.
- No ejecutar SQL.
- No crear scripts SQL.
- No cambiar reglas de negocio.
- No inventar transiciones.
- No crear etapa visual VALIDACION.
- No implementar Publicacion.
- No hacer refactor masivo.

Reglas de base de datos:

- No modificar datos.
- No ejecutar SQL.
- Si se detecta brecha de BD, documentarla y crear prompt separado.

Reglas UI:

- Mantener paleta institucional.
- Seleccion de grillas con color unico oficial.
- Colores de plazo solo en badges, sin mezclar con seleccion.
- Badges sobrios para estados, grupo familiar, duplicidad, asociados, Emitido y Requiere publicacion.
- No usar codigos tecnicos como texto visible principal.

Reglas Service/DAO:

- No mover SQL a UI.
- No introducir escrituras nuevas si el QA es visual.
- Si se corrige bug funcional, mantener Service/DAO.

Criterios de aceptacion:

- Todos los modulos hasta Notificacion se ven consistentes.
- No hay controles cortados ni solapados.
- No hay scroll horizontal global innecesario.
- Las busquedas se comportan de forma uniforme.
- Los paneles contextuales respetan alto y comportamiento.
- Ver detalle abre DlgConsolaExpedienteV2.
- Firma/emision queda integrada visualmente en Verificacion.
- Publicacion no queda implementada en este QA.
- No se toca legacy ni OracleConnection.java.

Compilacion:

- Ejecutar `mvn clean compile`.
- Si compila, ejecutar `mvn clean package`.
- Validar `.\run-v2.ps1` o indicar razon si no se ejecuta.

Git:

- Ejecutar `git status`.
- No usar `git add .`.
- Agregar solo archivos de esta tarea.
- Commit sugerido: `ui: polish operational flow up to notification`.
- Push al branch actual si el build pasa.
```

---

## PROMPT 8 - Diagnostico futuro de Publicacion

```text
Antes de implementar, analiza cómo está actualmente, qué falta por mejorar, qué falta por completar, qué está mal conectado y qué riesgos existen.

Necesito un diagnostico futuro del modulo Publicacion de SDRERC V2. Este prompt no debe implementar Publicacion todavia; solo debe analizar que se requiere despues de completar Notificacion.

Objetivo:
Definir como deberia implementarse Publicacion en un incremento posterior, usando la metadata preparada por Notificacion y respetando el flujo SDRERC.

Archivos y carpetas a revisar:

- AGENTS.md.
- docs/arquitectura_app/detalle_funciones.md.
- docs/arquitectura_bd/TO BE V2.bpmn.
- db/sdrerc_app/scripts/*.sql.
- src/main/java/com/sdrerc/ui/views/publicacion/.
- src/main/java/com/sdrerc/ui/views/notificacion/.
- src/main/java/com/sdrerc/application/sdrercapp/*Publicacion*.
- src/main/java/com/sdrerc/domain/dto/sdrercapp/*Publicacion*.
- src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/*Publicacion*.
- src/main/java/com/sdrerc/ui/views/expedienteconsola/.

Alcance del diagnostico:

- Revisar que datos llegan desde Notificacion.
- Validar como se usa `Requiere publicacion`.
- Validar como se usa `Fecha de publicacion`.
- Revisar tabla EXPEDIENTE_PUBLICACION y campos disponibles.
- Identificar si existe transicion real hacia Publicacion.
- Identificar si existe transicion real desde Publicacion hacia Cierre / Archivo.
- Identificar que documentos deben publicarse.
- Identificar roles/usuarios que deberian registrar publicacion.
- Definir bandeja, filtros, columnas y panel contextual sugeridos.
- Definir reglas de trazabilidad e historial.
- Identificar brechas de BD o catalogos sin crear scripts.

No hacer:

- No modificar codigo Java.
- No crear scripts SQL.
- No ejecutar SQL.
- No implementar Publicacion.
- No tocar legacy.
- No tocar OracleConnection.java.
- No cambiar transiciones.
- No crear integraciones externas con portales.

Reglas de base de datos:

- Solo diagnostico por lectura de scripts/modelo.
- No ejecutar DML ni DDL.
- Si falta tabla/columna/catalogo, documentar brecha.

Reglas UI sugeridas:

- Publicacion debe seguir patron visual de Notificacion.
- No mostrar codigos tecnicos.
- Debe mostrar documentos, fecha publicacion, medio, observacion, estado y cierre si aplica.
- Debe usar DlgConsolaExpedienteV2 para detalle.

Entregable:

- Documento de diagnostico con:
  - modelo actual;
  - flujo esperado;
  - datos que vienen de Notificacion;
  - brechas de BD/catalogo/transicion;
  - propuesta de UI;
  - propuesta Service/DAO;
  - criterios de aceptacion para implementacion futura;
  - riesgos.
- Confirmacion de que no se implemento codigo ni SQL.

Git:

- Ejecutar `git status`.
- Si se crea documento, agregar solo ese documento.
- Commit sugerido: `docs: diagnose publication module`.
- Push al branch actual si corresponde.
```

