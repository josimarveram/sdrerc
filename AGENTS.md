# AGENTS.md - Reglas persistentes del proyecto SDRERC

Este archivo es la fuente principal de reglas operativas para sesiones futuras de Codex o Codex CLI en el proyecto SDRERC.

Ruta del proyecto:

```text
D:\2026\FuentesRENIEC\sdrerc_CODIGOS
```

## 1. Rol del agente

- Actuar como agente principal de desarrollo para SDRERC.
- Trabajar de forma autonoma, ordenada y verificable.
- Priorizar cambios pequenos, incrementales y compilables.
- Leer el codigo y la documentacion existente antes de asumir detalles importantes.
- Mantener foco en la tarea solicitada y evitar refactors no pedidos.

## 2. Contexto persistente

- Usar `AGENTS.md` como fuente principal de reglas del proyecto.
- Usar `docs/arquitectura_app/*.md`, prompts e informes como contexto tecnico cuando aplique.
- Usar como fuentes funcionales historicas las actas `docs/arquitectura_bd/Acta_Reunión_011-2026-DRC.md`, `Acta_Reunión_012-2026-DRC.md` y `Acta_Reunión_013-2026-DRC.md`.
- Interpretar las actas de forma cronologica: ante contradiccion, el acuerdo posterior reemplaza al anterior; el Acta 013 del 22/05/2026 consolida el flujo revisado. Los requisitos anteriores no contradichos siguen vigentes.
- Usar `/resume`, `/compact` y `/review` cuando ayuden a conservar continuidad y ahorrar contexto/tokens.
- No repetir contexto largo salvo que sea necesario para decidir, validar o explicar un bloqueo.
- Antes de pedir contexto al usuario, revisar archivos locales relevantes del proyecto.
- `AGENTS.md` es una fuente viva de reglas: si una tarea agrega, cambia o elimina una configuracion funcional, una regla persistente, una restriccion de UI o un criterio operativo, actualizar este archivo en la misma intervencion para que el siguiente prompt herede el estado correcto.
- Si una configuracion se quita, se reemplaza o se redefine, reflejar ese cambio en `AGENTS.md` de inmediato; no dejar reglas desfasadas entre sesiones.

## 3. Reglas SDRERC obligatorias

- Mantener SDRERC V2 separada de la app legacy.
- No tocar `src/main/java/com/sdrerc/infrastructure/database/OracleConnection.java` salvo autorizacion explicita.
- No tocar `FrmLogin.java` legacy salvo autorizacion explicita.
- No tocar `MenuPrincipal.java` legacy salvo autorizacion explicita.
- No tocar `com.sdrerc.Main` legacy salvo autorizacion explicita.
- No eliminar codigo legacy.
- No ejecutar SQL salvo autorizacion explicita.
- No modificar datos de BD salvo autorizacion explicita.
- No imprimir ni documentar passwords reales.
- No crear etapa visual `VALIDACION`.
- Mantener Java 8 y Swing.
- Mantener FlatLaf/AppUiConfig si se reutiliza sin afectar legacy.

## 4. Reglas de SDRERC V2

- Avanzar modulos V2 dentro de la nueva arquitectura.
- Mantener la App SDRERC V2 separada de la app legacy.
- V2 puede evolucionar visualmente en paquetes `appv2` y `expedienteconsola` cuando la tarea lo pida.
- Usar `SDRERC_APP` mediante la conexion paralela `SdrercAppConnection`.
- No modificar la conexion global legacy.
- Consultar SDRERC_APP mediante DAOs y Services; no poner SQL en formularios Swing.
- Usar vistas y DAOs de lectura cuando el incremento sea solo consulta.
- Por defecto, V2 es lectura/consulta.
- No implementar `INSERT`, `UPDATE`, `DELETE`, `MERGE`, `executeUpdate` ni movimientos reales de flujo salvo autorizacion explicita.
- No implementar escrituras o movimientos reales de flujo salvo que la tarea lo pida explicitamente.
- No usar IDs hardcodeados.
- Mantener UI moderna tipo Service Console / Case Management.
- Mantener nombres visuales amigables para etapas y estados; no mostrar codigos tecnicos al usuario final cuando exista nombre amigable.
- No crear etapa visual `VALIDACION`.
- En Registro Manual V2 no capturar datos de notificacion; esa gestion corresponde al modulo/etapa de Notificacion.
- El campo `Fecha recepcion` de Registro Manual debe usar un componente reutilizable premium con formato `dd/MM/yyyy`, apertura del calendario al hacer clic en la caja o el icono y alineacion visual institucional.
- En Registro Manual, `Hoja de envio` solo aplica como texto condicional cuando la validacion inicial no corresponde a la SDRERC.
- En Registro Manual, mantener combos de catalogo con nombres amigables; excluir `RUC` del combo de Titular y permitirlo en Remitente cuando el modelo lo requiera.
- Si el modelo aun no separa canal de ingreso y modalidad, usar opciones compuestas amigables en la UI y dejar documentada la separacion futura como mejora de arquitectura.
- En Registro / Recepcion, la carga diaria y el registro manual deben detectar duplicidad solo por la combinacion `numero de acta + titular`. Los registros duplicados se guardan para trazabilidad, quedan marcados como potencial duplicado y no deben generar un nuevo numero de expediente hasta que se confirme/asocie el caso.
- La bandeja de Registro / Recepcion debe incluir una lenguetа/panel contextual "Asociar duplicados" (analoga a la de Asignacion) que permita confirmar la asociacion de posibles duplicados por `numero de acta + titular` directamente desde Recepcion, ademas de seguir permitiendolo desde Asignacion. Ambas rutas usan el mismo servicio de asociacion (`ExpedienteRelacionadoService`/`ExpedienteRelacionadoDeteccionService`); no duplicar logica de negocio.
- En Registro / Recepcion, los duplicados sin numero no deben consumir correlativo. El siguiente numero `SDRERC-EXP-YYYY-000001` debe calcularse desde el ultimo numero de expediente existente y avanzar solo cuando realmente se asigna numero; no usar `id_expediente` como correlativo visible.
- En Registro / Recepcion, las solicitudes con procedimiento registral `RECONSIDERACION` o `APELACION` se registran sin numero de expediente y con observacion operativa; en Asignacion el asignador decide si las asocia a un expediente principal o genera un nuevo numero de expediente antes de asignarlas.
- En todas las bandejas operativas V2, la columna `Dias` debe mostrar dias habiles restantes respecto de `EXPEDIENTE.FECHA_VENCIMIENTO`, no dias transcurridos desde ingreso, registro o ultimo movimiento.
- El vencimiento de solicitudes SDRERC debe calcularse en dias habiles salvo configuracion explicita distinta; los dias habiles excluyen sabados, domingos y feriados activos configurados.
- Los feriados nacionales o dias no laborables excepcionales deben administrarse por configuracion/mantenimiento; no hardcodearlos en Java ni cargarlos desde internet.
- No recalcular vencimientos historicos masivamente sin autorizacion explicita; aplicar el calculo habil a nuevos registros, cargas o acciones controladas.
- En modulos operativos V2 con filtros de `Fecha solicitud` desde/hasta, salvo la Bandeja General de Expedientes, el rango por defecto debe iniciar el primer dia del mes de hace cinco meses y terminar en la fecha actual; centralizar el calculo en helper reutilizable y no hardcodear fechas.
- Los combos de estado de cada modulo operativo deben consultar estados activos de `ESTADO_EXPEDIENTE` por la etapa correspondiente, mostrar primero `Todos los estados`, usar `NOMBRE` como texto visible y conservar `CODIGO` internamente para filtros y acciones; resolver la etapa por codigo, no hardcodear `ID_ETAPA`.
- En la bandeja de Registro / Recepcion, los filtros visibles deben priorizar busqueda, rango de `Fecha solicitud`, estado de la etapa `REGISTRO` y cantidad a mostrar; no mostrar filtro de etapa.
- El numero de expediente SDRERC V2 debe generarse con estructura `SDRERC-EXP-YYYY-000001`, usando guiones normales, mayusculas, anio vigente y correlativo de seis digitos.
- La plantilla oficial de carga diaria debe usar `TIPO DOCUMENTO IDENTIDAD SOLICITANTE`, `N° DOCUMENTO IDENTIDAD SOLICITANTE`, `TIPO DOCUMENTO IDENTIDAD TITULAR` y `N° DOCUMENTO IDENTIDAD TITULAR`; `DNI SOLICITANTE` queda solo como alias de compatibilidad de importacion, no como columna oficial visible.
- En carga diaria, si `TITULAR` coincide con `SOLICITADO POR`, el importador debe completar el documento de identidad del titular con el documento del solicitante cuando falte; si el documento del solicitante es `SIN DNI`, debe persistirse como vacio.
- En carga diaria, el canal de recepcion se deriva por reglas de plantilla: tramite web con numeros -> MPV, `SIN TRAMITE` con documento de solicitante numerico -> MP presencial, y `SIN TRAMITE` sin documento -> OR o Interno segun el origen RENIEC informado en `SOLICITADO POR`.
- En carga diaria, `CANAL RECEPCIÓN` puede venir informado desde plantilla con lista desplegable; si queda vacio, se aplica la derivacion automatica existente. La referencia externa `N° EXPEDIENTE SGD` debe guardarse en `EXPEDIENTE_SOLICITUD.NUMERO_EXPEDIENTE_SGD` y extender las busquedas de bandejas sin confundirse con numero de expediente SDRERC, tramite web ni numero de documento.
- `OR Pasivo` es un canal de recepcion reservado para solicitudes provenientes del pasivo acumulado de Oficinas Registrales derivado a SDRERC; no cambiar derivaciones automaticas hacia este canal sin una regla funcional explicita para carga inicial masiva.
- `N° expediente SGD` y `Tipo de acta` son obligatorios tanto en Registro manual/Edicion manual como en Carga diaria; el registro no debe completarse sin ambos datos.
- La fecha de solicitud/origen actualmente usada por plazos y filtros no debe reemplazarse por la fecha de recepcion SDRERC. La fecha en que SDRERC recibe la solicitud debe registrarse como dato adicional para reportes en `EXPEDIENTE_SOLICITUD.FECHA_RECEPCION_SDRERC`.
- La plantilla de carga diaria debe mantener listas desplegables para tipo de documento de identidad, procedimiento registral, tipo de acta, tipo documento y tipo de solicitud; las reglas de identidad son SIN DNI con numero vacio, DNI 8 numeros, RUC 11 numeros, CE/Pasaporte hasta 12 alfanumericos.
- La plantilla de carga diaria debe generar las columnas de numeros/documentos/tramites/actas como texto y aplicar validacion de datos en Excel para el numero de identidad segun el tipo seleccionado, ademas de la validacion del importador.
- La carga diaria de Registro / Recepcion debe ofrecer descarga de una plantilla oficial `.xlsx` con encabezados compatibles con el parser V2. La importacion debe poder previsualizar y confirmar el mismo archivo de plantilla cuando el usuario complete la hoja de carga.
- La previsualizacion de carga diaria debe permitir editar en memoria las celdas importadas antes de validar o confirmar; las columnas calculadas de validacion, duplicidad y numero generado se recalculan por Service y no deben editarse manualmente.
- En Carga Diaria, la deteccion de reglas debe concentrarse en un servicio central puro (`CargaDiariaReglasService` o equivalente). `Validacion` resuelve el preview y marca numero/alertas/motivos; `Confirmacion` solo persiste el preview ya validado y no debe recalcular duplicidad, grupo familiar ni numero de expediente.
- La carga diaria mediante Excel representa el canal de interoperabilidad controlada con los archivos originados en SGD; no asumir integracion directa con SGD sin contrato y autorizacion.
- Para actas de matrimonio, el modelo y la UI V2 deben permitir dos titulares. La persistencia debe usar multiples relaciones `EXPEDIENTE_PERSONA` de tipo `TITULAR`; no agregar columnas especificas de segundo titular en la cabecera V2.
- Los datos de grupo familiar y de notificacion no son obligatorios en Registro / Recepcion. Los datos de notificacion se gestionan en su modulo.
- Grupo familiar es una alerta o marca funcional no bloqueante; no debe impedir registro, importacion ni edicion de solicitudes.
- Grupo familiar no debe confundirse con duplicidad: la duplicidad en Registro / Recepcion sigue siendo exclusivamente por la combinacion `numero de acta + titular`.
- En fase 1, grupo familiar se persiste como marca simple en `EXPEDIENTE_SOLICITUD`; no crear relaciones automaticas en `EXPEDIENTE_RELACION`, tabla formal de grupo familiar ni catalogo de parentesco sin autorizacion explicita.
- La deteccion automatica de posible grupo familiar por coincidencia conservadora de apellidos es solo alerta operativa y no confirma grupo familiar por si sola.
- Las alertas o incidencias operativas del expediente deben persistirse en `EXPEDIENTE_ALERTA`; pueden ser multiples por expediente, deben mostrar concatenacion en UI y no depender solo de `EXPEDIENTE_SOLICITUD.OBSERVACION`.
- `Asignacion` puede mostrar grupo familiar como sugerencia para asignacion coordinada, pero no debe forzar abogado ni equipo automaticamente por esa marca.
- El plazo de atencion debe resolverse mediante `PLAZO_CONFIGURACION` o administracion equivalente; un fallback fijo en Java es solo contingencia tecnica y no debe ser la fuente funcional oficial.
- La configuracion oficial inicial de plazos SDRERC debe contemplar `SOLICITUD_RECTIFICACION_ADMINISTRATIVA = 30 dias habiles`, `SOLICITUD_RECONSIDERACION = 15 dias habiles` y `SOLICITUD_APELACION = 30 dias habiles`; `SOLICITUD_SDRERC = 30 dias habiles` queda como plazo general de contingencia.
- Los plazos oficiales deben centralizarse en configuracion o helper unico; no repetir dias ni logica de calendario laboral en pantallas.
- Los plazos funcionales se mantienen desde Administracion / Plazos; no hardcodear plazos operativos en formularios Swing.
- La exportacion de reportes Excel por etapa es un requerimiento funcional. Debe implementarse mediante Service/DAO, con nombres amigables y sin SQL en formularios, una vez validada la matriz de columnas de cada reporte.
- La actualizacion masiva por Excel para Ejecucion y Notificacion es un requerimiento pendiente de definicion de matriz. No implementarla ni efectuar escrituras parciales hasta contar con estructura, reglas de validacion y autorizacion explicita.
- El mantenimiento de descripciones breves preconfiguradas por tipo de documento es un requerimiento pendiente de diseno de catalogo; no resolverlo con listas hardcodeadas.
- La opcion funcional `Asignacion de respuesta` permanece pendiente de definicion; no inventar etapa, estado, tabla ni accion hasta contar con acuerdo funcional.
- La seleccion multiple para asignacion masiva sigue vigente en el modulo Asignacion. El Acta 013 reemplaza la propuesta de nueva asignacion en Ejecucion porque debe mantenerse el abogado inicial. Una asignacion especifica o masiva en Notificacion permanece pendiente de definicion y no debe asumirse.

## 4.1 Estado actual de modulos SDRERC V2

Modulos V2 ya incorporados o en uso dentro de la app nueva:

- Inicio.
- Bandeja de Expedientes.
- Registro / Recepcion.
- Asignacion.
- Analisis.
- Verificacion.
- Firma / Emision integrada dentro de Verificacion, sin entrada independiente en el menu lateral.
- Ejecucion.
- Notificacion.
- Publicacion.
- Expediente digital.
- Administracion / Usuarios.
- Administracion / Equipo Juridico.
- Administracion / Roles.
- Administracion / Feriados.
- Administracion / Plazos.

Reglas por modulo:

- `MenuPrincipalV2` es el punto de integracion visual de modulos V2; no usar el menu legacy para nuevas entradas.
- `Firma / Emision` no debe exponerse como modulo lateral independiente en V2; sus acciones visuales se gestionan dentro de `Verificacion`, reutilizando los Services/DAOs transaccionales de Firma/Emision y las etapas/estados reales del flujo.
- La Bandeja de Expedientes no debe mostrar `V2` en el titulo ni en el nombre visual del modulo.
- `Ver detalle` en bandejas operativas debe abrir la consola unica `DlgConsolaExpedienteV2`, no crear consolas paralelas.
- Los modulos administrativos `Roles`, `Usuarios` y `Equipo Juridico` no deben mostrar columnas `Creado` ni `Modificado` en sus listados principales, salvo que el usuario lo pida explicitamente.
- Los modulos V2 deben evitar bloques de cabecera duplicados dentro del panel cuando `MenuPrincipalV2` ya muestra titulo y subtitulo. Si el panel interno necesita un bloque superior, debe aportar contexto operativo nuevo y no repetir titulo/subtitulo.
- En los bloques superiores de `Registro / Recepcion`, `Asignacion`, `Analisis`, `Verificacion`, `Ejecucion`, `Notificacion`, `Publicacion`, `Expediente digital` y `Bandeja de Expedientes`, usar textos descriptivos que definan el modulo y su proposito operativo; evitar textos genericos o repetidos.

## 4.2 Escritura controlada ya autorizada en V2

Por defecto V2 sigue siendo lectura/consulta. Las escrituras reales solo son validas dentro de los modulos ya autorizados y con DAO/Service transaccional:

- `Asignacion`: asignar expedientes `REGISTRO / REGISTRADO` hacia `ASIGNACION / ASIGNADO`, registrar historial y evitar doble asignacion.
- `Asignacion`: detectar posibles relacionados solo de forma visual; insertar en `EXPEDIENTE_RELACION` unicamente cuando el usuario confirma la asociacion. No fusionar expedientes.
- `Asignacion`: la asociacion confirmada debe validar exclusivamente la coincidencia normalizada `numero de acta + titular`, excluir de posibles relacionados los pares que ya tengan relacion activa y orientar la relacion hacia el expediente principal con numero; si ambos tienen o carecen de numero, usar como principal el registro mas antiguo.
- `Asignacion`: un duplicado confirmado debe registrarse como documento duplicado asociado al expediente principal mediante `EXPEDIENTE_RELACION`; no debe convertirse automaticamente en `EXPEDIENTE_DOCUMENTO_ANALIZADO` ni avanzar como expediente operativo independiente si pertenece al mismo caso registral.
- `Asignacion`: el expediente principal concentra la gestion operativa del caso; el registro duplicado confirmado puede conservar trazabilidad y numero compartido, pero debe quedar excluido de asignacion independiente y visible como asociado/documento duplicado en consola.
- `Asignacion`: al asociar por misma acta y titular, si el expediente relacionado no tiene numero de expediente y el principal si lo tiene, el relacionado debe heredar el mismo `numero_expediente` dentro de la misma transaccion, junto con la sincronizacion de fecha de vencimiento e historial.
- `Asignacion`: al confirmar un documento duplicado asociado, debe heredar dentro de la misma transaccion el equipo y abogado vigentes del expediente principal. Si el principal se asigna despues de asociar, la asignacion debe sincronizarse a sus documentos asociados activos sin cambiar la etapa ni el estado operativo de estos; el panel debe mostrar el equipo y abogado del registro principal o asociado que tenga el foco.
- `Asignacion`: la asociacion rapida debe permitir seleccionar dos o mas solicitudes desde la grilla; los motivos visuales como `Con observaciones` o `Potencial duplicado` no bloquean la asociacion, siempre que el DAO confirme misma acta y titular.
- `Asignacion`: el combo de estado debe mostrar `Todos los estados`, `Registrado` y `Asignado`, cargados desde `ESTADO_EXPEDIENTE` por codigo en ese orden; no insertar un segundo `REGISTRADO` para la etapa Asignacion porque `estado_expediente.codigo` es unico globalmente.
- `Asignacion`: el asignador puede corregir el procedimiento registral antes de asignar solo para expedientes `REGISTRO / REGISTRADO`; solo puede cambiarse a `Reconsideracion` o `Apelacion`, validando en UI y DAO sin cambiar el numero de expediente. El tipo de documento no debe editarse desde `Datos registrales`; las cartas de respuesta se gestionan como documentos analizados en la seccion `Cartas de respuesta`.
- `Asignacion`: en asignacion simple o multiple, la hoja de envio se captura por expediente y se persiste en `EXPEDIENTE_ASIGNACION.NUMERO_HOJA_ENVIO`; el numero debe ser unico antes de confirmar la asignacion. Un valor vacio o compuesto solo por un guion (`-`) se trata como hoja de envio no ingresada.
- `Asignacion`: la reasignacion de un expediente ya asignado se realiza dentro de la lengueta `Asignacion` (no en `Asociar`), reutilizando el mismo boton `Generar asignacion` que la asignacion inicial. Por defecto la casilla de seleccion (checkbox) del listado de expedientes queda bloqueada/deshabilitada para expedientes ya asignados, igual que antes de introducir la reasignacion. En el bloque `Asignacion de abogado` hay un checkbox `Habilitar reasignacion`; al activarlo, la casilla del listado tambien permite marcar expedientes ya asignados (junto con los nuevos, en la misma seleccion o de forma individual con clic en la fila). El checkbox se desactiva automaticamente al completar `Generar asignacion`, para que el listado vuelva a bloquear por defecto las casillas de expedientes asignados, incluyendo los recien asignados/reasignados en esa misma accion. La grilla de asignacion muestra dos columnas de hoja de envio: `Hoja de envio nueva` (editable, vacia por defecto para expedientes ya asignados, para forzar un valor distinto) y `Hoja de envio actual` (solo lectura, referencia del valor previo). Al generar, los expedientes nuevos se asignan en una sola transaccion y los ya asignados se reasignan individualmente: se desactiva (`activa=0`) la fila vigente en `EXPEDIENTE_ASIGNACION` sin eliminarla, se inserta una nueva fila con `es_reasignacion_excepcional=1` y se actualiza el responsable/equipo del expediente. La lengueta `Asignacion` incluye ademas el bloque de historial de asignaciones/reasignaciones (activas e historicas) del expediente con foco, con columnas `Tipo` (Asignacion inicial/Reasignacion), `Abogado`, `Equipo`, `Hoja de envio`, `Fecha`, `Asignado por` (usuario que ejecuto la accion) y `Estado`; `Asociar` ya no muestra ese historial.
- `Asignacion`: el panel de asignacion puede mostrar documentos relacionados pendientes en una tabla compacta con `N° documento` y accion de recepcion/asociacion; esa accion debe reutilizar el Service/DAO transaccional de relacion, registrar evidencia en historial y no poblar `EXPEDIENTE_DOCUMENTO_ANALIZADO`.
- `Asignacion`: la columna de la bandeja principal se llama `Abogado actual` (no `Abogado asignado`) porque esta bandeja no filtra por etapa y puede listar expedientes ya avanzados a Analisis/Verificacion/Ejecucion/Notificacion; debe mostrar quien tiene el expediente hoy, no solo el abogado que hizo Analisis. Se resuelve desde `EXPEDIENTE.id_usuario_responsable_actual`, con fallback a la asignacion activa en `EXPEDIENTE_ASIGNACION` cuando ese campo es nulo (mismo patron ya usado en `AsignacionExpedienteDAO`). Para que ese campo siga reflejando al responsable real conforme el expediente avanza, `DocumentoAnalisisDAO.asignarNotificacionMultiple`/`reasignarNotificacion` deben actualizarlo al validador/notificador destino cuando Notificacion asigna o reasigna un documento (ademas de la columna propia del documento en `EXPEDIENTE_DOCUMENTO_ANALIZADO`), y `registrarResultadoValidacion` debe limpiarlo (NULL) al marcar `Observado`, para que el fallback vuelva a resolver al abogado de Analisis/Ejecucion en vez de dejar fijo al validador que ya no lo esta trabajando.
- `Analisis`: recibir expedientes `ASIGNACION / ASIGNADO`, registrar evaluacion, observaciones y documentos analizados, enviar a verificacion y archivar no corresponde si el flujo `SDRERC_TO_BE` lo permite; no exponer acciones directas de derivacion a notificacion ni derivacion externa desde el panel de Analisis.
- `Analisis`: `N° Documento (Proveido)` debe permanecer habilitado para cualquier resultado y registrarse como `EXPEDIENTE_DOCUMENTO` cuando tenga valor. Para `NO_CORRESPONDE` es obligatorio; ademas se bloquean `Acta incorporada`, evaluaciones, documentos analizados, observacion y comentario de movimiento, y no se exigen ni registran documentos analizados.
- `Analisis`: el combo `Tipo` de documentos analizados debe cargar solo tipos activos con codigo `ANALISIS_%` desde `TIPO_DOCUMENTO_ADJUNTO`, respetando el orden del codigo; `PROVEIDO` es un tipo tecnico para documentos del expediente y no debe mostrarse en ese combo.
- `Analisis`: la grilla de documentos analizados debe separar la gestion documental del resultado final. Guardar documentos, cartas intermedias, oficios, cargos, respuestas, subsanaciones o anexos no debe exigir resultado/fundamento final ni mover el expediente a Verificacion. El resultado final y el envio a Verificacion siguen siendo acciones independientes. Guardar o eliminar un documento analizado (icono de diskette/eliminar en la grilla) refresca unicamente esa grilla y la lectura de Publicacion prevista; no debe resetear ni recargar los combos/checks/fundamento del bloque "Resultado del analisis" en pantalla.
- `Analisis`: los documentos analizados pueden organizarse en jerarquia de maximo dos niveles dentro de `EXPEDIENTE_DOCUMENTO_ANALIZADO`: documento principal con `ID_DOCUMENTO_PADRE` nulo y documento relacionado/respuesta con `ID_DOCUMENTO_PADRE` apuntando al principal. No registrar respuestas como nueva solicitud o expediente independiente, no permitir nietos y usar baja logica con `ACTIVO=0` cuando corresponda.
- `Analisis`: al recibir un expediente principal, recibir en la misma transaccion los documentos asociados que ya se encuentren en `ASIGNACION / ASIGNADO`. Si un documento se asocia despues de la recepcion del principal, debe quedar pendiente y solo el abogado responsable puede recibirlo individualmente desde la seccion `Documentos asociados`; esta accion no lo convierte automaticamente en documento analizado.
- `Analisis`: la derivacion externa no debe mostrarse como boton o accion preparada en el panel de Analisis; si se requiere en el futuro, debe definirse el flujo funcional completo de entidad destino, tipo de derivacion y datos documentales antes de habilitar escritura.
- `Analisis`: las plantillas Word de `docs/plantillas` usan variables `#nomVariable#` (camelCase); el listado completo por plantilla vive en `docs/arquitectura_app/variables_plantillas_word.md`. Al generar una RESOLUCION, `AnalisisPlantillaDocumentoService` autocompleta `#numDocInforme#`/`#fechaDocInforme#` con los datos del documento analizado tipo `INFORME` mas reciente (activo, mayor `fecha_documento`) del mismo expediente; si no existe informe, esas variables quedan vacias.
- `Analisis`: la clasificacion de negocio de los tipos de documento (`TIPO_DOCUMENTO_ADJUNTO.CLASIFICACION`) sigue esta taxonomia: cartas finales (carta_abandono, carta_improcedente, carta_procedente, carta_procedente_en_parte) y resoluciones -> `FINAL`; cartas intermedias (carta_edicto, carta_falta_sustento, carta_indagatorio, carta_precisar_pretension) y oficios (oficio_indagatorio_cancelacion, oficio_reconstitucion) -> `INTERMEDIO`; informes (informe_abandono, informe_cancelacion, informe_reconstitucion, informe_rectificacion) -> sin clasificar (`NULL`, uso interno, no participan de Cartas de Respuesta ni del auto-marcado de "Requiere respuesta"). Detalle completo en `docs/arquitectura_app/variables_plantillas_word.md`.
- `Verificacion`: consultar expedientes en `VERIFICACION`, revisar analisis y documentos, aprobar verificacion, observar, marcar documento inconsistente, devolver a Analisis y enviar a `FIRMA_EMISION / PARA_FIRMA` si el flujo `SDRERC_TO_BE` lo permite.
- `Verificacion`: desde la UI V2 tambien concentra los controles de firma, emision, numeracion y ruteo posterior para expedientes en `FIRMA_EMISION`; las resoluciones pasan a Ejecucion y los documentos no resolutivos pueden pasar a Notificacion cuando exista transicion real, sin crear rutas paralelas ni cambiar las transiciones reales.
- `Verificacion`: no crear tabla paralela de verificacion si el modelo no la define; usar `EXPEDIENTE`, `EXPEDIENTE_HISTORIAL`, `EXPEDIENTE_OBSERVACION`, `EXPEDIENTE_EVALUACION` y `EXPEDIENTE_DOCUMENTO_ANALIZADO` segun corresponda.
- `Verificacion`: las acciones autorizadas deben resolver transiciones reales por codigo, como `APROBACION_VERIFICACION`, `ENVIO_FIRMA`, `REGISTRO_OBSERVACION_VERIFICACION`, `REVERSION_ESTADO_DOCUMENTO` y `DEVOLUCION_A_ANALISIS`; si falta una transicion, catalogo, evaluacion o documento requerido, bloquear con diagnostico sin escritura parcial.
- `Firma / Emision`: consultar expedientes en `FIRMA_EMISION`, revisar analisis, verificacion, documentos y observaciones, registrar firma, registrar emision, registrar numero de resolucion/documento y enrutar segun tipo documental: resoluciones hacia `EJECUCION / EN_EJECUCION`, y oficios, cartas u otros documentos no resolutivos hacia `NOTIFICACION / EN_NOTIFICACION` si el flujo `SDRERC_TO_BE` lo permite.
- `Firma / Emision`: usar `EXPEDIENTE_RESOLUCION` para metadata del documento resolutivo cuando exista, incluyendo `NUMERO_RESOLUCION`, `FECHA_RESOLUCION` y `FECHA_FIRMA`; no implementar carga fisica de archivo salvo autorizacion y estructura clara.
- `Firma / Emision`: las acciones autorizadas deben resolver transiciones reales por codigo, como `FIRMA_DOCUMENTO` y `REGISTRO_NUMERO_RESOLUCION`; no inventar `EMISION_DOCUMENTO` ni otra accion si el flujo real no la define.
- `Firma / Emision`: validar estado actual antes de registrar firma, emision, numeracion o envio a Ejecucion; si falta tabla, columna, catalogo, transicion o constraint, bloquear con diagnostico sin escritura parcial.
- `Ejecucion`: consultar expedientes en `EJECUCION`, revisar resolucion/documento, documentos, analisis, verificacion, historial y expedientes asociados, registrar atencion/resultado de ejecucion y marcar ejecutado solo si el flujo `SDRERC_TO_BE` expone una transicion real.
- `Ejecucion`: derivar a `NOTIFICACION / EN_NOTIFICACION` y revertir/devolver a `ANALISIS / OBSERVADO` solo cuando `FLUJO_TRANSICION` y las acciones permitidas lo soporten; no inventar rutas, etapas, estados ni acciones.
- `Ejecucion`: no crear tabla paralela de ejecucion si el modelo no la define; usar `EXPEDIENTE`, `EXPEDIENTE_HISTORIAL`, `EXPEDIENTE_OBSERVACION`, `EXPEDIENTE_RESOLUCION` y documentos/metadata existentes segun corresponda.
- `Ejecucion`: las acciones autorizadas deben resolver transiciones reales por codigo, como `INICIO_EJECUCION`, `OBSERVACION_EJECUCION`, `REVERSION_ESTADO_DOCUMENTO_EJECUCION`, `DEVOLUCION_A_ANALISIS` y `DERIVACION_A_NOTIFICACION`; si falta transicion, catalogo, documento requerido o constraint, bloquear con diagnostico sin escritura parcial.
- `Ejecucion`: toda reversion a Analisis debe exigir motivo/comentario, preservar resolucion y documentos previos, registrar historial y evitar borrados o reemplazos fisicos.
- `Notificacion`: consultar expedientes en `NOTIFICACION`, revisar resolucion/documento, documentos, analisis, verificacion, ejecucion, historial y expedientes asociados, registrar modalidad de notificacion, cargo de acuse, resultado de notificacion, publicacion requerida y cierre cuando el flujo `SDRERC_TO_BE` lo permita.
- `Notificacion`: usar las tablas reales `EXPEDIENTE_NOTIFICACION`, `EXPEDIENTE_CARGO_ACUSE`, `EXPEDIENTE_PUBLICACION`, `EXPEDIENTE_HISTORIAL`, `EXPEDIENTE_RESOLUCION` y `EXPEDIENTE` segun corresponda; no crear tablas paralelas ni guardar datos no soportados por el modelo.
- `Notificacion`: las acciones autorizadas deben resolver transiciones reales por codigo, como `NOTIFICACION_VIRTUAL`, `NOTIFICACION_PRESENCIAL_1`, `NOTIFICACION_PRESENCIAL_2`, `RECEPCION_CARGO_ACUSE`, `CONFIRMACION_NOTIFICACION`, `REGISTRO_NOTIFICACION_FALLIDA`, `GENERACION_PUBLICACION` y `CIERRE`; si falta transicion, catalogo, documento requerido o constraint, bloquear con diagnostico sin escritura parcial.
- `Notificacion`: no implementar envio real de correos, SMS, WhatsApp ni integraciones externas de notificacion sin autorizacion explicita; el modulo registra metadata y trazabilidad funcional, no comunicaciones externas.
- `Notificacion`: para publicacion condicional, primero registrar notificacion fallida o estado `REQUIERE_PUBLICACION` si el flujo lo exige, y luego derivar a `PUBLICACION_CONDICIONAL / PENDIENTE_PUBLICACION` solo con transicion activa.
- `Notificacion`: puede incorporar una pestaña interna de `Cierre / Seguimiento` para control terminal y trazabilidad del estado final; no debe exponerse como modulo lateral independiente ni como pantalla separada.
- `Notificacion`: el cierre terminal desde `NOTIFICACION / NOTIFICADO` debe marcar el expediente como cerrado cuando el modelo lo soporte, registrar historial y nunca eliminar datos fisicamente.
- `Publicacion`: consultar expedientes en `PUBLICACION_CONDICIONAL`, revisar resolucion/documento, notificacion previa, cargo de acuse, documentos, historial, observaciones y expedientes asociados.
- `Publicacion`: registrar datos de publicacion, marcar publicacion registrada y cerrar expediente publicado solo cuando el flujo `SDRERC_TO_BE` exponga una transicion real activa.
- `Publicacion`: usar las tablas reales `EXPEDIENTE_PUBLICACION`, `EXPEDIENTE_NOTIFICACION`, `EXPEDIENTE_CARGO_ACUSE`, `EXPEDIENTE_HISTORIAL`, `EXPEDIENTE_RESOLUCION` y `EXPEDIENTE` segun corresponda; no crear tablas paralelas ni guardar datos no soportados por el modelo.
- `Publicacion`: las acciones autorizadas deben resolver transiciones reales por codigo, como `REGISTRO_PUBLICACION` y `CIERRE`; si falta transicion, catalogo, documento requerido, tabla, columna o constraint, bloquear con diagnostico sin escritura parcial.
- `Publicacion`: no implementar publicacion real en portales externos ni integraciones externas sin autorizacion explicita; el modulo registra metadata y trazabilidad funcional, no publicaciones externas.
- `Publicacion`: si el flujo de publicacion requiere cierre terminal, debe derivarse a la pestaña interna de `Cierre / Seguimiento` dentro de `Notificacion`, con historial y sin exponer un modulo lateral independiente.
- `Expediente digital`: consultar expedientes en `EXPEDIENTE_DIGITAL`, revisar documentos, resolucion/documento, notificacion/publicacion si existe, historial, observaciones y expedientes asociados.
- `Expediente digital`: registrar o actualizar metadata de carpeta/ruta/enlace digital y marcar completitud digital solo mediante DAO/Service transaccional y cuando el flujo `SDRERC_TO_BE` exponga una transicion real activa.
- `Expediente digital`: usar las tablas reales `EXPEDIENTE_DIGITAL`, `EXPEDIENTE`, `EXPEDIENTE_DOCUMENTO`, `EXPEDIENTE_DOCUMENTO_ANALIZADO`, `EXPEDIENTE_HISTORIAL`, `EXPEDIENTE_RESOLUCION`, `EXPEDIENTE_NOTIFICACION` y `EXPEDIENTE_PUBLICACION` segun corresponda; no crear tablas paralelas ni guardar datos no soportados por el modelo.
- `Expediente digital`: las acciones autorizadas deben resolver transiciones reales por codigo, como `CREACION_CARPETA_EXPEDIENTE_DIGITAL` y `CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL`; si `REGISTRO_LINK_EXPEDIENTE_DIGITAL` existe como catalogo pero no como transicion activa, usar la transicion real configurada y reportar el diagnostico.
- `Expediente digital`: al marcar completo, actualizar `EXPEDIENTE.EXPEDIENTE_DIGITAL_COMPLETO` cuando el modelo lo soporte, validar documentos o metadata requerida, registrar historial y no hacer escritura parcial si falta tabla, columna, catalogo, transicion o constraint.
- `Expediente digital`: no mover archivos fisicamente, no eliminar archivos, no implementar carga masiva documental ni integraciones externas con NAS, SharePoint, Drive, MinIO u otros repositorios sin autorizacion explicita.
- `Notificacion`: la pestaña interna de `Cierre / Seguimiento` puede consultar expedientes candidatos con cierre terminal, revisar antecedentes completos, documentos, resolucion, notificacion, publicacion, expediente digital, historial y expedientes asociados; cualquier accion de archivo o derivacion externa debe seguir la regla funcional aprobada dentro de `Notificacion` y no como modulo lateral independiente.
- `Roles`: crear, editar, activar e inactivar roles. Nunca eliminar fisicamente roles.
- `Usuarios`: crear, editar, activar e inactivar usuarios, y asociar roles/equipo si el modelo lo permite. Nunca mostrar ni guardar passwords en texto plano.
- `Equipo Juridico`: crear, editar, activar e inactivar equipos, y gestionar miembros/supervisor si el modelo lo permite. Nunca eliminar fisicamente equipos ni usuarios.

Toda escritura V2 autorizada debe:

- Resolver IDs por codigo o catalogo, no hardcodearlos.
- Validar estado actual antes de escribir para prevenir cambios concurrentes.
- Usar transaccion completa con commit/rollback.
- Registrar historial/movimiento cuando el modelo lo soporte.
- Bloquear la accion y reportar diagnostico exacto si falta tabla, columna, catalogo, transicion o constraint.

## 4.2.1 Flujo operativo Analisis, Verificacion, Ejecucion y Notificacion

Esta seccion consolida el criterio funcional vigente para documentos proyectados, verificados, firmados, ejecutados y notificados. El flujo operativo base es `Analisis -> Verificacion -> Ejecucion / Notificacion -> Notificacion`. Cuando exista tension con una regla general anterior, prevalece este ruteo documentado sin inventar etapas, estados ni transiciones.

- En `Analisis`, el abogado recibe el expediente y proyecta documentos. Los documentos pueden ser carta intermedia, informe, oficio, resolucion, carta de notificacion cuando corresponda u otros documentos definidos por catalogo.
- Los documentos proyectados en `Analisis` se registran como documentos analizados y deben conservar, segun soporte del modelo, tipo de documento, numero si corresponde, resultado de analisis, estado del documento, fecha de emision, hoja de envio y datos de respuesta si aplica.
- Si el documento es carta intermedia, el expediente puede quedar como `Pendiente de respuesta` y no debe cerrarse como atendido. La carta intermedia la genera el abogado, la firma el supervisor de `Analisis` y luego pasa a `Notificacion`.
- Las cartas intermedias no pasan por `Ejecucion` y no deben ser firmadas por la supervisora de `Notificacion` como carta final.
- `Verificacion` valida el documento generado desde `Analisis`. Si el documento presenta observacion, no debe firmarse y debe devolverse para subsanacion, normalmente a `Analisis` salvo que funcionalmente corresponda otro modulo; siempre debe conservar trazabilidad, comentario y motivo de observacion.
- Si el documento esta correcto, en `Verificacion` se firma, se registra el numero del documento, se registra la fecha de emision o firma cuando corresponda y se actualiza el estado documental como firmado o emitido segun corresponda. El numero puede corresponder a resolucion, oficio o carta de notificacion.
- Despues de `Verificacion`, solo las resoluciones pasan a `Ejecucion`. Los oficios, cartas intermedias u otros documentos firmados que no requieren ejecucion pasan directamente a `Notificacion`, siempre que exista transicion real activa.
- Las resoluciones firmadas en `Verificacion` pasan automaticamente a `Ejecucion` del mismo abogado que realizo el `Analisis`; `Ejecucion` no debe manejarse como reasignacion manual general, salvo regla funcional futura documentada.
- En `Ejecucion`, si la resolucion es `Procedente` o `Procedente en parte`, el abogado realiza la anotacion textual y luego elabora o genera la carta de notificacion.
- En `Ejecucion`, si la resolucion es `Improcedente`, no requiere anotacion textual; solo se elabora o genera la carta de notificacion.
- `Ejecucion` debe conservar trazabilidad del documento origen, resultado, abogado responsable y fecha de ejecucion. Despues de generar la carta de notificacion final, el documento pasa al flujo de `Notificacion` mediante transicion real activa y con historial.
- `Notificacion` debe operar con una bandeja interna. El supervisor de notificacion asigna documentos a validadores, abogados o personas responsables de validar o verificar documentos antes de notificar, cuando el modelo lo soporte.
- El validador de `Notificacion` revisa el documento antes de notificar y debe poder marcarlo como `Validado` u `Observado`; cuando observe, debe registrar comentario de observacion.
- Si el documento observado corresponde al documento o analisis, debe volver a `Analisis`; si corresponde a ejecucion o carta final, debe volver a `Ejecucion`. Toda devolucion debe conservar trazabilidad e historial.
- Si el documento esta validado, el supervisor firma cuando corresponda y luego se procede a notificar, sin reactivar `Firma / Emision` como modulo lateral independiente.
- Resumen de ruteo: `Analisis` proyecta documentos; `Verificacion` valida, observa o firma y registra numero; si es resolucion pasa automaticamente a `Ejecucion` del mismo abogado de `Analisis`; si es oficio, carta intermedia u otro documento firmado no ejecutable pasa directo a `Notificacion`; `Ejecucion` ejecuta resoluciones y genera carta final; `Notificacion` valida, observa o firma cuando corresponda y procede a notificar.
- La validacion de documentos para notificacion pertenece operativamente a `Notificacion` como bandeja o asignacion interna; no crear una etapa visual independiente llamada `VALIDACION`.
- `Firma / Emision` no debe volver como modulo lateral independiente. Sus controles se mantienen integrados dentro de `Verificacion`.
- No hardcodear IDs de catalogo, no ejecutar SQL sin autorizacion, no tocar legacy ni `OracleConnection.java`, y no cambiar estas reglas funcionales sin documentar la decision.

### Pendientes funcionales no implementables aun

- Queda pendiente confirmar quien marca como culminados los casos `ORE`, `Culminacion en linea` y otros casos especiales por definir.
- No implementar cierre automatico ni cambio definitivo a `Culminado` para esos casos hasta confirmar responsable, modulo y regla de negocio.
- No asumir que `Notificacion` culmina esos casos especiales sin acuerdo funcional explicito.

## 4.3 Lenguaje visual vigente

- Usar nombres amigables y sin sufijo tecnico `V2` en titulos visibles de modulos.
- No usar `padre` ni `hijo` en UI para relaciones de expedientes. Usar `Expedientes asociados`, `Posibles relacionados`, `Misma acta y titular` o `Relacion confirmada`.
- En listados administrativos, priorizar datos operativos visibles; auditoria tecnica solo debe mostrarse en detalle o cuando se solicite.
- Mantener badges sobrios para estado, etapa, alertas, asociados y escritura controlada.
- Evitar repetir literalmente subtitulos como cards internas del mismo modulo.

## 4.4 Home / Inicio y navegacion V2

- El Home / Inicio es la portada operativa de la aplicacion oficial SDRERC; no debe mostrar textos visibles que sugieran app legacy, migracion temporal, version alterna o sufijo tecnico `V2`.
- El hero del Home debe usar el logo RENIEC desde recurso local empaquetado, actualmente `src/main/resources/com/sdrerc/ui/imagenes/LogoRENIEC.png`.
- No cargar logos, imagenes ni iconos desde internet en runtime; los recursos visuales deben vivir en `src/main/resources`.
- El logo RENIEC del Home debe verse natural, sin recorte, sin distorsion, con proporcion original y con el fondo que corresponda al arte aprobado. Si el logo trae fondo blanco, conservarlo y evitar bordes o lineas superiores artificiales.
- El Home debe evitar scroll horizontal global. Las metricas, accesos rapidos y modulos principales deben reacomodarse segun el ancho disponible.
- El Home ya no debe mostrar el bloque visual `Flujo operativo`; al cargar debe priorizar la vista superior desde `Panel Ejecutivo` y contenido operativo resumido.
- Si en el futuro se requiere volver a mostrar un flujo operativo en Home, debe validarse funcionalmente antes de reintroducirlo y no debe mostrar codigos tecnicos como `FIRMA_EMISION`, `PUBLICACION_CONDICIONAL`, `EXPEDIENTE_DIGITAL` o `CIERRE_ARCHIVO`.
- El menu lateral V2 debe mantener iconos locales, tooltips en modo colapsado, agrupacion visual clara y navegacion funcional. No usar abreviaturas como reemplazo principal de iconos en modo colapsado.

## 4.5 Estabilizacion V2 de catalogos, detalle y tablas

- Los combos de catalogo V2 deben cargar desde `CatalogoLookupService` o servicios especificos; no hardcodear listas operativas si existe tabla de catalogo real.
- Si un catalogo requerido queda sin opciones activas, la UI debe mostrar diagnostico amigable y no fallar silenciosamente.
- Los datos maestros faltantes para combos deben resolverse con scripts idempotentes separados y autorizados; no modificar scripts base ni reestructurar BD durante estabilizaciones funcionales.
- La consola unica `DlgConsolaExpedienteV2` debe mostrar datos disponibles del modelo real: solicitud, titular, remitente, acta, documento, resolucion, notificacion, cargo, publicacion, expediente digital, historial, observaciones y asociados.
- No usar placeholders permanentes como `No disponible en vista actual` cuando el dato puede obtenerse por DAO de lectura desde tablas reales.
- Las bandejas y listados V2 deben usar `AppV2Table` y `AppV2TableColumnSizer` cuando aplique, con tooltips para valores largos y nombres amigables para etapas, estados y acciones.
- Mantener el mapper visual central `DisplayNameMapperV2`; no crear mapeos duplicados de etapa/estado/accion en cada panel.
- En tablas, conservar codigos tecnicos internamente si son necesarios para acciones, pero mostrar valores amigables al usuario final.
- En bandejas operativas de expedientes, evitar mostrar columnas tecnicas como `ID`; si son necesarias para acciones, conservarlas como columnas internas ocultas con prefijo `_`.
- Los KPI superiores de cada modulo deben calcularse con el mismo criterio de filtro de busqueda activo de la bandeja; al presionar `Buscar`, los KPI deben recalcularse segun las fechas y filtros seleccionados, y al cambiar el filtro debe mantenerse la coherencia entre cards y listado.
- En bandejas operativas, ubicar `Dias` al inicio como indicador numerico/badge y mostrar solo el numero, sin repetir la palabra `dias` en cada celda.
- En grillas principales de expedientes, no mostrar `Responsable`, `Abogado inicial`, `Abogado` ni `Ultimo mov.` salvo necesidad funcional explicita del modulo o pedido del usuario; esos datos deben quedar disponibles en la consola/detalle cuando correspondan.
- Las columnas principales de bandeja deben priorizar gestion operativa: `Dias`, expediente, tramite/documento, titular, procedimiento, etapa, estado y alertas/asociados cuando apliquen.
- Las columnas de etapa, estado, plazo, publicacion, digital, observacion y asociados deben mostrarse con badges sobrios cuando el componente lo permita.
- `Asignacion` es el primer patron base visual para modulos operativos SDRERC V2. Los siguientes modulos operativos deben tender a la estructura: cards superiores, buscador/filtros ancho, zona operativa con tabla izquierda y panel derecho de accion/contexto.
- Los buscadores de modulos operativos deben usar campo amplio, placeholder claro, botones alineados y comportamiento responsive desktop; evitar campos cortos o controles comprimidos.
- Los paneles derechos operativos deben usar fondo blanco, borde suave, padding, secciones internas, scroll vertical sin scroll horizontal y acciones al pie.
- Los formularios y paneles V2 deben permitir copiar y seleccionar texto visible desde labels y campos de texto mediante menu contextual, respetando popups propios y sin exponer campos de contraseña.
- En modulos operativos con panel derecho de accion contextual, el panel debe estar oculto inicialmente si no hay seleccion operativa; la grilla/listado debe ocupar todo el ancho disponible y el panel debe aparecer solo al seleccionar uno o mas expedientes, salvo que el modulo requiera contexto permanente.
- Si el usuario cierra manualmente un panel derecho contextual con seleccion activa, no limpiar la seleccion; el panel debe reabrirse cuando el usuario vuelva a interactuar con la fila o la seleccion operativa.
- En modulos operativos con panel derecho contextual, el panel puede implementarse como redimensionable mediante divisor horizontal cuando el formulario requiera mas espacio; no imponer porcentajes maximos fijos si el usuario necesita ampliar el panel hasta el ancho disponible, y evitar superposiciones o scroll horizontal innecesario.
- El panel derecho contextual redimensionable debe expandir internamente combos, textareas, secciones y acciones segun el ancho disponible, manteniendo comportamiento responsive desktop para distintas resoluciones.
- En modulos operativos con panel derecho contextual, puede usarse un chip premium de encabezado para alternar vista normal/ampliada; debe sentirse como adhesivo/indicador visual integrado al panel, no como boton textual tosco, y cuando exista expediente en foco, el chip, el acento del panel y la fila seleccionada deben compartir una identidad visual sobria y legible.
- En bandejas V2 con documentos asociados o duplicados confirmados, la carga inicial debe priorizar documentos/solicitudes principales y cargar asociados bajo demanda al desplegar la fila principal; no usar textos `padre` ni `hijo` en UI.
- Las filas asociadas en grillas expandibles deben mostrarse indentadas o tintadas con el color/acento del expediente principal, sin repetir el numero de expediente cuando la pertenencia visual al principal ya es clara.
- Si un documento asociado no es operativo, no debe permitirse asignacion independiente; su seleccion debe actualizar el panel contextual con datos del asociado y referencia al expediente principal.
- Los controles de expandir/contraer en grillas anidadas V2 deben renderizarse con componentes o dibujo estable, no con caracteres Unicode que puedan fallar visualmente en Windows/Swing.
- Las filas asociadas nunca deben usar IDs internos como reemplazo visual de datos funcionales; si procedimiento, solicitante, abogado, documento u otro dato no existe, mostrar `-` o un texto amigable y diagnosticar la brecha.
- En paneles y detalles V2, `Tramite Web` debe mostrar `EXPEDIENTE.NUMERO_TRAMITE_DOCUMENTARIO` y `N° Documento` debe mostrar `EXPEDIENTE_DOCUMENTO.NUMERO_DOCUMENTO`; no mezclar ambos valores ni usar uno como fallback del otro, tanto para expedientes principales como para documentos asociados.
- El estado `Recibido por abogado` o `Pendiente de recibir` para documentos asociados debe mostrarse como informacion controlada; una accion de recepcion solo debe habilitarse para el abogado responsable y no debe poblar automaticamente `EXPEDIENTE_DOCUMENTO_ANALIZADO`.
- Al buscar, limpiar o refrescar grillas anidadas V2, limpiar expansiones/cache de asociados para evitar datos visuales de resultados anteriores.
- Antes de duplicar layout manual en un modulo operativo, preferir componentes reutilizables `AppV2SearchToolbar`, `AppV2TableSectionPanel`, `AppV2SideActionPanel` y `AppV2SideSectionPanel` cuando apliquen.
- Las grillas avanzadas V2 deben encapsular librerias externas en componentes `appv2` reutilizables; no usar SwingX, GlazedLists u otra libreria directamente dentro de cada `JPanel`.
- `Registro / Recepcion` puede funcionar como piloto controlado de mejoras de grilla; si una libreria externa no resuelve de forma confiable en Maven, deploy LAN o instalador, priorizar `AppV2Table`, `AppV2TablePanel` y `AppV2TableColumnSizer` mejorados antes de incorporar dependencia.
- No incorporar GlazedLists u otra capa de filtrado/listas en modulos V2 si el beneficio no supera el riesgo de complejidad; la busqueda principal debe seguir en Service/DAO y cualquier filtro local debe diferenciarse visualmente.
- Las listas y bandejas V2 deben evolucionar a paginacion real desde Service/DAO. El selector `Mostrar` o un limite maximo de filas no sustituye la paginacion requerida por el Acta 012-2026-DRC.

## 4.6 Alineamiento BPMN TO BE V2

- El archivo `docs/arquitectura_bd/TO BE V2.bpmn` es referencia funcional para validar el flujo operativo SDRERC V2, junto con `SDRERC_APP`.
- El BPMN TO BE V2 no autoriza crear una etapa visual `VALIDACION`; las validaciones del BPMN se implementan como acciones, observaciones, evaluaciones, estados o reglas dentro de las macroetapas existentes.
- Los actores BPMN externos como OGD, SDPRC y Ciudadano/Entidad no deben convertirse automaticamente en usuarios, equipos o modulos internos V2 salvo autorizacion explicita.
- La app V2 mantiene estas macroetapas como estructura visual principal: Registro, Asignacion, Analisis, Verificacion, Firma / Emision, Ejecucion, Notificacion, Publicacion y Expediente digital. El seguimiento/cierre terminal se implementa dentro de `Notificacion` como pestaña interna, no como macroetapa o modulo lateral separado.
- Los ajustes de flujo derivados del BPMN deben aplicarse como scripts idempotentes correlativos en `db/sdrerc_app/scripts/`, sin `DROP`, `DELETE` ni `TRUNCATE`, y sin modificar expedientes transaccionales.
- En `SDRERC_APP`, `estado_expediente.codigo` es unico globalmente. No duplicar el mismo codigo de estado por etapa; si una accion reutiliza un estado en mas de una etapa, resolverlo por la transicion activa y documentar el criterio.
- `Analisis` debe soportar `REGISTRO_RESULTADO_ANALISIS` desde `RECIBIDO_POR_ABOGADO`, `OBSERVADO` y `SUBSANADO` hacia `ATENDIDO`, `OBSERVADO`, `NO_CORRESPONDE`, `EN_ABANDONO` y `OBSERVACION_ADMINISTRATIVA`, siempre mediante transicion activa.
- `Ejecucion` debe soportar `INICIO_EJECUCION` desde `EJECUCION / EN_EJECUCION` hacia `EJECUCION / EJECUTADO`, con historial y validacion de resolucion/documento cuando el modulo lo requiera.
- Las autorizaciones de flujo por rol/equipo deben resolverse por codigo en `flujo_transicion_rol` y `flujo_transicion_equipo`, sin IDs hardcodeados.
- Si el BPMN introduce tareas mas detalladas que el modelo actual no representa como modulo propio, mapearlas primero a la macroetapa existente y reportar cualquier brecha antes de crear tablas, etapas o pantallas nuevas.

## 4.7 Empaquetado y despliegue SDRERC V2

- El artefacto distribuible oficial de SDRERC V2 debe generarse desde Maven como `SDRERC-V2.jar`.
- El manifest del JAR distribuible debe apuntar a `com.sdrerc.appv2.MainV2`.
- `UserManagementApp-1.0.0.jar` no es el artefacto final de SDRERC V2 y no debe usarse para despliegues nuevos.
- `run-v2.ps1` queda como launcher de desarrollo; no debe ser requisito para PCs cliente.
- El despliegue LAN debe vivir en `deploy/SDRERC-V2/` con rutas relativas, sin depender de `.m2/repository`, IntelliJ IDEA ni `target/classes`.
- La configuracion de conexion SDRERC_APP para despliegue debe externalizarse en `config/sdrerc-app.properties` o variables de entorno; no documentar passwords reales.
- El instalador cliente de red local debe vivir en `tools/installer-client/`, generar `dist/sdrerc-client/`, usar `SDRERC-V2.jar` autocontenido y crear `config/sdrerc-client.properties` con IP/puerto/servicio editables; no hardcodear IP ni credenciales en Java o repositorio.

## 4.8 Estado consolidado vigente al 07/07/2026

Esta seccion resume reglas recientes que deben guiar nuevos prompts o asistentes externos que partan solo de `AGENTS.md`. No reemplaza las reglas anteriores; las precisa donde hubo ajustes posteriores.

### Registro / Recepcion

- La bandeja `Registro / Recepcion` trabaja con tres pestañas superiores: `Bandeja Registro`, `Carga diaria` y `Registro manual`.
- El panel derecho de `Bandeja Registro` se abre con doble clic sobre la fila, no con un clic simple. Debe usar el titulo generico `Panel de datos` (no `Panel de Registro`, ver seccion "Titulo generico Panel de datos" mas abajo) y mostrar debajo el nombre del titular en azul, con el mismo estilo visual replicable en otros paneles de datos.
- El panel derecho de datos de Registro es informativo: no debe mostrar botones de accion. Debe agrupar datos en `Datos del plazo`, `Datos del expediente`, `Datos del acta`, `Datos de solicitud`, `Datos del titular`, `Datos del solicitante` y `Datos de Notificacion y Ubicacion`.
- En el panel de datos de Registro, `Datos del plazo` debe mostrar `Dias` como pill con color de vencimiento y `Fecha Vencimiento`; `Datos del expediente` debe mostrar `N° expediente` y `N° expediente SGD`.
- El formulario `Registro manual` y la `Edicion manual` deben mantener el bloque `Datos del expediente` encima de `Datos del acta`; `N° expediente` es solo lectura, y `N° expediente SGD` vive en ese bloque, no en `Datos de solicitud`.
- En `Datos de solicitud`, el orden vigente es: `Fecha recepcion`, `Canal de ingreso`, `Nro. tramite web`, `Procedimiento registral`, `Tipo documento`, `N° documento`, `Tipo de solicitud`, `Prioridad` y `Marca operativa`.
- El bloque visual antes llamado `Remitente` debe mostrarse como `Solicitante`.
- `Nro. tramite web` no es obligatorio. Solo se habilita si `Canal de ingreso` es `Mesa de partes virtual`; para otros canales debe bloquearse y mostrar `SIN TRAMITE`.
- El combo `Tipo documento` de `Datos de solicitud` debe normalizar tildes y equivalencias de texto para seleccionar correctamente valores como `Resolucion`, `Hoja de envio`, `Hoja de elevacion` o `Informe tecnico` aunque el Excel los traiga sin tilde.
- En `Bandeja Registro`, el combo de estado del filtro debe mostrar unicamente `Todos los estados` y `Registrado`.
- En `Bandeja Registro`, los KPI vigentes son `Potencial duplicado` y `Posible Grupo Familiar`; no mostrar `Total Registrados` salvo pedido futuro.
- En `Bandeja Registro`, los KPI se calculan con los filtros activos de busqueda. Clic en KPI filtra el listado por ese KPI; clic en `Buscar` limpia el filtro KPI y vuelve al total filtrado por fechas/estado/busqueda.
- En `Bandeja Registro`, la columna `Alertas` debe mostrar solo `Sin Alerta`, `Potencial duplicado` o `Posible Grupo Familiar`. Observaciones extensas y datos incompletos pertenecen a la previsualizacion de carga diaria, no a la columna de bandeja.
- La carga diaria debe mostrar observaciones concatenadas solo en la previsualizacion/exportacion, por ejemplo `Potencial duplicado`, `Posible Grupo Familiar` y `Dato incompleto: [campo]`.
- En carga diaria, `Potencial duplicado` solo aplica cuando coinciden `numero de acta + titular completo`. El primer registro del grupo que genera numero queda `Sin observacion`; los siguientes quedan como `Potencial duplicado` y no generan numero.
- En carga diaria, nombres completos iguales con diferente numero de acta no son duplicados ni grupo familiar por si solos.
- `Posible Grupo Familiar` aplica cuando coinciden conservadoramente las dos primeras palabras del titular, pero el titular completo y/o numero de acta no configuran duplicado. No debe coexistir con `Potencial duplicado` en la misma solicitud.
- La previsualizacion de carga diaria debe permitir editar celdas sin desactivar `Confirmar carga`; al editar, se recalculan solo observaciones dependientes del dato incompleto corregido sin borrar alertas validas como duplicidad o grupo familiar.
- `Validar carga` no debe quedar inutilizado despues de validar; la importacion debe poder confirmarse aun con observaciones no bloqueantes.
- `Registrar G.F` no es una pestaña superior de Registro; es una lengueta/panel derecho contextual que aparece cuando hay expedientes seleccionados por casilla para registrar grupo familiar.
- Registrar grupo familiar marca la solicitud en `EXPEDIENTE_SOLICITUD` y debe resolver/desactivar la alerta `Posible Grupo Familiar` en `EXPEDIENTE_ALERTA`, de modo que deje de aparecer en bandejas y KPI.

### Asociacion de expedientes y alertas

- Los expedientes asociados por duplicidad o relacion confirmada se muestran con icono de expandir `+` en la fila principal y el asociado debajo; el asociado no debe aparecer como otro expediente principal independiente en Registro, Asignacion ni Analisis.
- La fila asociada debe replicar el patron visual de Asignacion: icono documental, banda/acento vertical izquierdo, fondo celeste suave, texto atenuado, sin checkbox comun y jerarquia visual clara respecto al expediente principal.
- La relacion se orienta a un principal canonico: primero el expediente con numero SDRERC; si ambos tienen o ninguno tiene numero, el mas antiguo.
- Al asociar un duplicado por misma acta y titular, el expediente asociado hereda numero de expediente, fecha de vencimiento, abogado/equipo cuando corresponda y queda excluido de gestion independiente como principal.
- Al resolver una duplicidad mediante asociacion, la alerta `Potencial duplicado` debe quedar atendida/desactivada en BD para que no contabilice en KPI ni se siga mostrando como alerta pendiente.
- En bandejas jerarquicas, el expediente asociado debe mostrar su propia alerta funcional cuando corresponda, no copiar automaticamente la alerta visual del principal.
- En el panel `Asociar` de Asignacion, la grilla de solicitudes asociadas debe mostrar `N° expediente SGD`, `Estado`, `Fecha Asociacion` cuando exista y accion `X` solo si la relacion ya esta activa. La `X` elimina la asociacion logicamente y debe tener tooltip.

### Asignacion

- La bandeja `Asignacion` trabaja con tres pestañas superiores: `Bandeja Asignacion`, `Cartas de respuesta` y `Carga Abogados`.
- Los KPI principales de Asignacion deben incluir `Pendientes`, `Potencial duplicado`, `Posible Grupo Familiar`, `Por vencer` y `Vencidos`, calculados segun filtros activos.
- El filtro de busqueda de Asignacion debe replicar el diseño, tamanos y posicionamiento del filtro de Registro, manteniendo opciones de estado propias de Asignacion.
- La bandeja de Asignacion no debe mostrar columna `Solicitante`; debe mostrar `Alertas` con la misma semantica de Registro: `Sin Alerta`, `Potencial duplicado` o `Posible Grupo Familiar`.
- El panel derecho de Asignacion debe usar lenguetas separadas: `Datos`, `Asignacion` y `Asociar`. Las lenguetas pueden seleccionar primero y expandir/restaurar al segundo clic, manteniendo colores diferenciados de inactivo, activo y expandido.
- El panel `Datos` de Asignacion debe reutilizar el mismo contenido, estructura y estilo del panel `Datos` de Registro, con titulo `Panel de Asignacion` y nombre del titular debajo.
- El panel `Asignacion` debe conservar siempre el bloque `Asignacion de abogado` para uno o varios expedientes seleccionados.
- La grilla del bloque `Asignacion de abogado` debe listar todos los expedientes seleccionados por casilla, permitir hoja de envio por expediente y mostrarse sin barra vertical interna cuando sea razonable; debe crecer para mostrar las filas seleccionadas.
- La accion principal del panel de Asignacion debe ser `Generar asignacion`; no usar dos botones redundantes como `Asignar expediente` y `Asignar seleccionados`, ni popup adicional si la captura ya esta dentro del panel.
- En asignacion simple o multiple, validar que todos los expedientes seleccionados tengan hoja de envio cuando la regla funcional la exija y que el numero sea unico antes de confirmar.
- La casilla de cabecera de las grillas de Registro y Asignacion debe seleccionar todas las filas filtradas seleccionables, respetando que filas asociadas no operativas no se traten como principales seleccionables.
- La columna de casillas debe tener ancho compacto y uniforme entre Registro y Asignacion; no confundir ancho de la columna con tamano visual del checkbox.

### Analisis

- El modulo `Analisis` vuelve a manejar un unico analisis operativo por expediente. No reintroducir multiples bloques de analisis salvo requerimiento funcional explicito y script aprobado.
- La grilla de documentos analizados puede ser jerarquica en maximo dos niveles: documento principal y documento relacionado/respuesta. Esta jerarquia documental no significa multiples analisis.
- La grilla jerarquica de documentos de analisis debe conservar separacion entre documentos y resultado final: guardar documentos no exige resultado final ni mueve a Verificacion.
- Para la version de un unico analisis, las columnas funcionales vigentes de documentos analizados son: `Tipo`, `N° Documento`, `Estado`, `Fecha Emision`, `Descripcion` (etiqueta UI `Comentario`), `¿Requiere respuesta?`, `Confirmacion de respuesta`, `Fecha Respuesta`, `Fecha Publicacion` y `Hoja de Envio`. `Detalle Obs.`, `Fecha Acuse` y `Notificado` siguen existiendo en el modelo/DTO pero no se muestran como columna en la grilla vigente de Analisis. Si existe columna de control o expandir, no debe contarse como dato funcional ni mostrarse como ID tecnico.
- La grilla debe permitir agregar documento padre, agregar documento hijo solo sobre un padre, guardar cambios y baja logica. No permitir nietos ni eliminacion fisica.
- En Analisis, `Resultado final` sigue siendo accion separada. No debe existir una pestana independiente de Resultado si el diseno vigente lo integra al panel de analisis.
- En Analisis, eliminar o evitar bloques no pertinentes dentro del panel operativo como `Publicacion prevista`, `Expediente digital`, observaciones de publicacion o acciones externas si no corresponden al analisis actual.
- La edicion manual desde Analisis debe reutilizar el formulario de edicion manual de Registro/Recepcion, sin restringirse solo a expedientes en estado `Registrado`.

### Verificacion, Ejecucion, Notificacion y Publicacion

- Verificacion debe tener pestaña superior `Bandeja Verificacion` y panel derecho con lenguetas `Datos` y `Verificar`, siguiendo el patron visual de Analisis.
- La grilla `Documentos revisados en analisis` de Verificacion debe replicar el diseno de documentos analizados de Analisis, con columnas fijas cuando aplique, filtros por columna, flechas de ordenamiento y solo icono de guardar; no debe mostrar iconos de Word ni eliminar.
- En Verificacion, el supervisor puede modificar `Estado`, `Detalle Obs.`, `Fecha Emision` y `N° Documento` de los documentos revisados; el combo de estado debe usar las mismas opciones que la grilla de Analisis.
- Al final del panel `Verificar` solo deben quedar los botones principales `Registrar Verificacion` y `Cancelar`, salvo nueva regla funcional documentada.
- Ejecucion debe tener pestaña superior `Bandeja Ejecucion` y mantener panel derecho no limitado por contenedor interno de pestaña.
- Notificacion debe manejar pestañas superiores `Bandeja Asignacion`, `Bandeja Validacion`, `Bandeja Notificacion` y `Cierre`; `Cierre` es pestaña interna, no modulo lateral.
- Publicacion existe como modulo V2, pero no debe implementar publicacion real en portales externos; solo metadata, trazabilidad y transiciones reales.

### Bandejas, filtros, tablas y paneles

- Todas las bandejas operativas deben abrir panel derecho con doble clic sobre fila, no con clic simple, salvo seleccion por casillas cuando el panel corresponda a accion masiva.
- Los filtros de busqueda deben usar formato compacto de tres filas: busqueda y botones principales; fechas desde/hasta; estado, grupo familiar y limite numerico. Evitar texto visible `Mostrar` si el input numerico ya comunica el limite.
- Las fechas visibles en grillas y filtros deben usar `dd/MM/yyyy`; no mostrar hora ni formato `yyyy-MM-dd`.
- Las columnas `Fecha Solicitud` y `Fecha Vencimiento` deben estar visibles en Registro y Asignacion; `Fecha Vencimiento` va despues de `Fecha Solicitud`.
- En Registro, agregar `N° expediente SGD` al lado derecho de `Nro. Expediente`.
- Las grillas operativas deben tener filtros por columna debajo de cabeceras y ordenamiento por cabecera con flechas visibles; cabecera, filtros y cuerpo deben desplazarse sincronizados en el mismo `JScrollPane`.
- No usar scroll horizontal global del panel cuando la grilla tiene muchas columnas; el scroll horizontal visible debe pertenecer a la tabla.
- Los renderers de columnas no deben depender de indices fragiles si se agregan o reordenan columnas; preferir resolver por nombre de columna o constantes centralizadas para evitar que estilos de `Estado` y `Alertas` se apliquen a columnas equivocadas.
- La columna `Dias` debe usar pill con color derivado de configuracion de plazos, no colores hardcodeados inconsistentes. Los colores deben respetar porcentajes configurados en `PLAZO_CONFIGURACION`.
- El panel izquierdo de modulos debe ser compacto, ajustado al contenido y no consumir ancho innecesario. En modo colapsado debe conservar iconos y tooltips.
- Los botones principales de proceso, como `Buscar`, `Confirmar carga`, `Registrar expediente`, `Generar asignacion`, `Registrar Analisis` o `Registrar Verificacion`, deben usar el estilo azul institucional de accion principal, conservando forma y tamano original del boton del modulo.

### Administracion y catalogos

- Los modulos de Administracion deben tender al mismo patron visual de paneles usado en modulos operativos: grilla principal, panel derecho al seleccionar fila, botonera uniforme y acciones primarias azules.
- En Administracion, `Usuarios`, `Roles`, `Equipo Juridico`, `Feriados` y `Plazos` deben mantener filtros por columna, flechas de ordenamiento y panel contextual sin distorsionar columnas.
- En el modulo `Usuarios`, el tipo de documento debe ser combo basado en el catalogo de tipo documento de identidad usado para titulares/solicitantes.
- `Equipo Juridico`, `Feriados` y `Plazos` deben replicar textos y estilo de botones de `Usuarios` y `Roles` segun su funcionalidad.

### Ubigeo, base de datos y scripts recientes

- Las tablas `UBIGEO_DEPARTAMENTO`, `UBIGEO_PROVINCIA` y `UBIGEO_DISTRITO` deben replicar la estructura y datos de referencia aprobados desde la BD origen `SYSTEM` hacia `SDRERC_APP` en `XEPDB1` mediante script intermedio unico cuando se autorice.
- Si ya existe data sembrada por un script anterior de ubigeo, los scripts posteriores deben ser compatibles e idempotentes; no duplicar datos ni asumir tablas vacias sin validacion previa.
- No intentar resetear identities o sequences con comandos no soportados por la version Oracle sin validar si la columna sigue siendo identity o default por sequence. Si el modelo quedo inconsistente, documentar y generar script correctivo controlado, no ejecutar automaticamente.

### Permisos, roles, equipos y supervision (14/07/2026)

- Los scripts 63 a 71 (`db/sdrerc_app/scripts`) y los diagnosticos de solo lectura `00_diagnostico_roles_permisos_equipos.sql`/`00_diagnostico_permisos_reales_vs_esperado.sql` quedan preparados pero NO ejecutados contra ninguna base de datos; requieren autorizacion explicita antes de correrlos. Dependencias de orden: 64/67 requieren que 63 ya haya creado los usuarios; 69 asume el estado que deja 63 (miembros de `EQ_NOTIFICACION`) y 65 (roles nuevos).
- Script 63 carga 111 usuarios reales del personal SDRERC (fuente `docs/arquitectura_app/Personal_SDRERC_Usuarios_Herramienta_Interna.xlsx`) con rol/equipo segun su area, agrega `USUARIO.TELEFONO` y el rol nuevo `CONSULTA`. La primera contrasena sigue asignandose por `Restablecer clave`; no hay autoservicio de alta.
- Script 65 define la matriz real de permisos por rol operativo: antes solo `ADMIN_SISTEMA` tenia permisos reales y el resto de roles quedaba fail-open (veian el menu completo sin importar su rol). Agrega los roles `VALIDACION` y `SUPERVISOR_NOTIFICACION` (Notificacion se divide en 3 roles: quien asigna documentos a validadores/notificadores, quien notifica y quien valida). `REGISTRADOR_CIVIL` queda sin permisos a proposito porque Firma/Emision ya esta integrada en Verificacion y nadie tiene ese rol asignado hoy.
- Scripts 70/71 desactivan (nunca eliminan fisicamente) 4 roles y varios permisos creados a mano desde los botones "Nuevo rol"/"Nuevo equipo" de Administracion, fuera de los scripts del proyecto y sin referencia en el codigo Java: roles `ADMINISTRADOR`/`ANALISTA`/`PREASIGNADOR`/`SUPERVISOR` (solapan con roles oficiales, 0 usuarios asignados) y permisos con prefijo `PERM_*` (fuera del catalogo oficial de 22 permisos `MENU_*`/`BANDEJA_*`). Script 66 aplica el mismo criterio a equipos duplicados (`EQUIPO_ANALISIS` vs `EQ_ANALISIS`, etc., incluyendo `EQ_FIRMA_EMISION` -> `EQ_VERIFICACION`), migrando membresias activas al equipo canonico antes de desactivar el redundante.
- El tooltip de `Codigo` en `JPanelRolesV2` (Administracion > Roles) y en `JPanelEquipoJuridicoV2` (Administracion > Equipo Juridico) ahora advierten no repetir un rol/equipo ya existente y listan los codigos oficiales, para no repetir la causa raiz de los duplicados que motivaron los scripts 66/70/71.
- `Equipo Juridico` incorpora una vista de solo lectura "Personal por supervisor" (combo de supervisores + grilla de abogados supervisados), respaldada por el DAO nuevo `UsuarioSupervisionDAO` y `EquipoJuridicoService.listarSupervisoresConAbogados()`/`listarAbogadosPorSupervisor()`; lee `USUARIO_SUPERVISION`, no crea ni edita relaciones de supervision desde la UI.
- `USUARIO_SUPERVISION` se sigue cargando por script, no desde UI: 64 (area Analisis, 74 relaciones) y 67 (Recepcion/Asignacion y Notificacion, 25 relaciones adicionales) cubren en total 8 supervisores. Quedan brechas documentadas dentro de los propios scripts: 2 personas de Recepcion/Asignacion sin rol/equipo asignado por dato de origen insuficiente, y 1 fila de Notificacion sin supervisor resoluble (celda con el texto literal "SUPERVISOR" en el Excel de origen); requieren asignacion manual de un administrador.
- Script 68 sincroniza `EQ_EJECUCION` con los miembros vigentes de `EQ_ANALISIS` (mismo abogado en Analisis y Ejecucion, regla ya documentada en la seccion 4.2); es re-ejecutable para mantener ambos equipos alineados si cambia la membresia de `EQ_ANALISIS`. Script 69 distribuye el personal de `EQ_NOTIFICACION` cargado por 63 entre Notificacion/Validacion/Publicacion porque el Excel de origen no distinguia esos 3 roles dentro de "Notificacion de Documentos"; el reparto fue autorizado por el usuario sin un criterio de negocio especifico (aleatorio, documentado con semilla fija en el propio script). El modulo Publicacion sigue sin rol/permiso propio en el catalogo (brecha ya reportada); las 2 personas asignadas a `EQ_PUBLICACION` conservan el rol `NOTIFICACION` como unico acceso disponible hasta que se defina el modulo.

### Ejecucion: carta de notificacion obligatoria antes de guardar (15/07/2026)

- `JPanelEjecucionV2.registrarEjecucion()` ("Guardar Ejecucion") valida `DocumentoAnalisisService.tieneDocumentoFinalEnDespacho(idExpediente)` **antes** de llamar a `EjecucionExpedienteService.registrarEjecucion(...)`; si no hay carta de notificacion final en despacho, bloquea con `IllegalArgumentException` sin escribir nada en BD. Si la hay, encadena `registrarEjecucion` (`INICIO_EJECUCION`) y `derivarNotificacion` (`DERIVACION_A_NOTIFICACION`) en la misma accion.
- Antes de este cambio, `INICIO_EJECUCION` se registraba primero y la falta de carta solo se reportaba como texto adicional en el mensaje de exito: el expediente quedaba marcado `EJECUTADO` sin haberse derivado a Notificacion, y al reabrir el panel el boton "Guardar Ejecucion" aparecia deshabilitado (la transicion ya no aplicaba desde ese estado) sin que el usuario tuviera una salida clara. No repetir ese orden (accion primero, validacion despues) en flujos similares de dos pasos.

### Visibilidad de bandejas por asignacion y autor de historial para ADMIN_SISTEMA (15/07/2026)

- Ademas del control de acceso por rol a modulo/bandeja (seccion "Permisos, roles, equipos y supervision"), las bandejas de `Analisis`, `Verificacion`, `Ejecucion` y las pestanas `Validacion`/`Notificacion` de `Notificacion` ahora filtran tambien por fila: un usuario sin rol `ADMIN_SISTEMA` solo ve expedientes/documentos cuyo responsable actual (`EXPEDIENTE.id_usuario_responsable_actual`/`id_equipo_responsable_actual`, o en Notificacion `EXPEDIENTE_DOCUMENTO_ANALIZADO.id_usuario_notificacion`/`id_equipo_notificacion`) coincide con su propio usuario o alguno de sus equipos (`EQUIPO_USUARIO`). `ADMIN_SISTEMA` no tiene ese filtro y ve todo, se le haya asignado/derivado o no.
- Logica centralizada en `VisibilidadBandejaSql.construirCondicion(...)` (nuevo, paquete `infrastructure.sdrercapp.dao`): arma la clausula SQL y sus binds; si no admin y no hay usuario ni equipo resuelto, deniega por defecto (`1=0`) en vez de mostrar todo. Invocada desde `AnalisisExpedienteDAO`, `VerificacionExpedienteDAO`, `EjecucionExpedienteDAO`, `NotificacionExpedienteDAO` y `DocumentoAnalisisDAO` (`listarDocumentosValidacion`/`listarDocumentosNotificacion`), con los `*ExpedienteService`/`DocumentoAnalisisService` correspondientes resolviendo `esAdmin` (`SessionContext.hasRole("ADMIN_SISTEMA")`) y `idsEquipoActual` (`UsuarioAsignacionService.listarIdsEquipoDeUsuario(idUsuario)`, nuevo) antes de llamar al DAO. Las firmas publicas de los `Service` no cambiaron: los `JPanel*V2` que ya llamaban `buscarExpedientes(...)` no requirieron cambios.
- Deliberadamente EXCLUIDAS de este filtro por fila (siguen mostrando toda la cola, sin cambios): `Registro/Recepcion` (y la Bandeja de Expedientes general, que comparte `ExpedienteBandejaDAO`/`JPanelBandejaExpedientesNueva` con la pestana "Bandeja Registro"), la Bandeja Asignacion del modulo `Asignacion`, y la Bandeja Asignacion de `Notificacion` (`DocumentoAnalisisDAO.listarDocumentosAsignacionNotificacion`). Son pantallas de coordinacion/despacho: quien registra, asigna o reasigna necesita ver toda la cola entrante para poder derivarla, no solo lo que ya quedo a su propio nombre; ademas, en Registro y antes de que Asignacion actue, `id_usuario_responsable_actual`/`id_equipo_responsable_actual` son `NULL` para la mayoria de expedientes, por lo que un filtro por fila las dejaria vacias para todos. Si se pide extender el filtro a estas tres pantallas, definir primero que significa "lo mio" para un rol coordinador antes de implementarlo, para no romper la reasignacion ya construida en Asignacion.
- Autor de `EXPEDIENTE_HISTORIAL` cuando actua `ADMIN_SISTEMA`: si el usuario que ejecuta una accion de Analisis, Verificacion, Ejecucion, Asignacion (expediente), o asignacion/reasignacion de Notificacion tiene el rol `ADMIN_SISTEMA`, el historial NO guarda al administrador como autor (`id_usuario_origen`/`creado_por`); se sustituye por el usuario asignado/reasignado/derivado de esa misma accion (el destino ya resuelto en la misma llamada). Si la accion no cambia de responsable (p. ej. generar numero de expediente, editar datos de asignacion), no hay a quien sustituir y se conserva el autor real. Confirmado explicitamente por el usuario pese a que implica que el historial deja de reflejar literalmente quien ejecuto el clic; no revertir este comportamiento sin que lo pida de nuevo.
- Implementado como metodo privado `resolverAutorHistorial(conn, idUsuarioActor, idUsuarioDestino)` duplicado (misma logica, sin centralizar) en `AsignacionExpedienteDAO`, `AnalisisExpedienteDAO`, `VerificacionExpedienteDAO`, `EjecucionExpedienteDAO`, `NotificacionExpedienteDAO` (en su `insertarHistorial` compartido) y `DocumentoAnalisisDAO` (solo en `insertarHistorialNotificacion`), apoyado en `CatalogoLookupDAO.tieneRolAdminSistema(conn, idUsuario)` (nuevo) para resolver el rol dentro de la misma transaccion sin depender de `SessionContext`.

### Estado NOTIFICACION/POR_ASIGNAR y doble salto automatico desde Ejecucion/Verificacion (15/07/2026)

- Pedido explicito del usuario: al guardar Ejecucion (carta `FINAL` en despacho) o al aprobar Verificacion con destino (carta `INTERMEDIO` ya Emitida), el expediente debe encadenar 2 cambios de estado automaticos e inmediatos en la misma transaccion, sin accion manual adicional: (1) el estado propio de "trabajo terminado" en el modulo de origen (`EJECUCION/EJECUTADO` o `VERIFICACION/VERIFICADO`, ya existian), y (2) de inmediato `NOTIFICACION/POR_ASIGNAR` (estado nuevo).
- `POR_ASIGNAR` (etapa `NOTIFICACION`) sembrado por `db/sdrerc_app/scripts/72_estado_por_asignar_notificacion.sql` (ya ejecutado, autorizado explicitamente por el usuario). El script tambien retargeteo la fila de `flujo_transicion` `DERIVACION_A_NOTIFICACION` que usa `EjecucionExpedienteDAO.derivarNotificacion` (antes `EJECUCION/EJECUTADO -> NOTIFICACION/EN_NOTIFICACION`, ahora `-> NOTIFICACION/POR_ASIGNAR`) y agrego una fila nueva para Verificacion (`VERIFICACION/VERIFICADO -> NOTIFICACION/POR_ASIGNAR`, mismo `codigo_accion` `DERIVACION_A_NOTIFICACION`). No toco las otras 2 filas existentes de `DERIVACION_A_NOTIFICACION` (origen `ANALISIS/EN_ABANDONO` y `ANALISIS/OBSERVACION_ADMINISTRATIVA`), que siguen destinando a `EN_NOTIFICACION`.
- `VerificacionExpedienteDAO.aprobarVerificacionConDestino` ahora encadena, dentro de la misma transaccion y antes del commit, una segunda transicion `requerirTransicion(DERIVACION_A_NOTIFICACION, VERIFICACION/VERIFICADO, NOTIFICACION/POR_ASIGNAR)` con su propio registro en `EXPEDIENTE_HISTORIAL`, replicando el patron atomico de dos pasos que ya usa `JPanelEjecucionV2.registrarEjecucion()` (ver entrada anterior sobre la carta de notificacion obligatoria) pero encapsulado en el DAO en vez de en la UI, para no repetir el riesgo de una accion parcialmente completada entre dos llamadas de Service separadas.
- `POR_ASIGNAR` es distinto de `EN_NOTIFICACION`: representa "recien derivado, sin asignar todavia", mientras que `EN_NOTIFICACION` sigue representando el ciclo de notificacion propiamente dicho (una vez que el documento ya fue asignado/validado/emitido). Aun NO existe una transicion definida de `POR_ASIGNAR` hacia `EN_NOTIFICACION`; queda pendiente de definir junto con el resto del ciclo de asignacion/firma en Notificacion, no se asumio ni se inserto una transicion para ese paso.
- `DocumentoAnalisisDAO.CONDICION_ASIGNACION_NOTIFICACION` (la condicion detras de `listarDocumentosAsignacionNotificacion()`, la Bandeja Asignacion de Notificacion) ahora exige ademas `expediente.estado_actual = POR_ASIGNAR` (join nuevo a `estado_expediente` por `e.id_estado_actual`, alias `eest`), no solo que el documento cumpla clasificacion/estado (`INTERMEDIO`+`Emitido` o `FINAL`+`En despacho`/`Validado`). Esto fue pedido explicitamente por el usuario porque antes la bandeja se guiaba solo por el documento, sin exigir que el expediente estuviera realmente "recien llegado" a Notificacion.
- El script 72 incluyo ademas una correccion puntual de datos (paso 4, no solo catalogo): expedientes que ya estaban varados en `NOTIFICACION/EN_NOTIFICACION` (llegaron ahi con el comportamiento anterior a este cambio) y que tenian un documento sin asignar cumpliendo la condicion de Asignacion, se retargearon a `POR_ASIGNAR` para no quedar huerfanos/invisibles con la condicion nueva. Se corrigieron 4 expedientes reales en el momento de ejecutar el script (incluido el caso reportado por el usuario, `SDRERC-EXP-2026-000178`); no se reejecuta automaticamente hacia adelante, es una correccion de una sola vez para el estado en que quedo la BD antes de este fix.

### Combo "usuario destino" vacio para equipos de Notificacion/Validacion (16/07/2026)

- Bug reportado: en Verificacion (destino de un documento INTERMEDIO) y en Notificacion > Bandeja Asignacion, al elegir equipo destino `EQ_NOTIFICACION` o `EQ_VALIDACION`, el combo de "usuario destino" quedaba vacio.
- Causa: ambos combos reutilizaban `UsuarioAsignacionDAO.listarAbogadosAsignables(idEquipo)`, que ademas de filtrar por `id_equipo` exige `UPPER(r.codigo) IN ('ABOGADO', 'ANALISTA')`. Los miembros reales de `EQ_NOTIFICACION`/`EQ_VALIDACION` tienen rol `NOTIFICACION`/`VALIDACION`/`SUPERVISOR_NOTIFICACION`, no `ABOGADO`/`ANALISTA`, asi que la consulta siempre devolvia 0 filas para esos dos equipos especificos (confirmado leyendo directo de BD: 0 con el metodo viejo, 8 y 9 miembros respectivamente con el metodo nuevo).
- Fix: nuevo metodo `UsuarioAsignacionDAO.listarUsuariosAsignablesPorEquipo(idEquipo)` (+ wrappers en `UsuarioAsignacionService` y en `VerificacionExpedienteService`) que filtra unicamente por pertenencia activa al equipo (`EQUIPO_USUARIO`), sin restriccion de rol; la pertenencia al equipo ya es el criterio correcto de "quien puede recibir este destino". `JPanelVerificacionV2.cargarUsuariosDestino()` y `JPanelNotificacionV2.cargarUsuariosAsignacionNotif()` ahora llaman a este metodo nuevo en vez del viejo. `listarAbogadosAsignables` (el metodo original, restringido a ABOGADO/ANALISTA) se mantiene intacto y sigue usandose tal cual en `JPanelAsignacionV2` (asignar expediente a un abogado de Analisis), donde el filtro de rol si es correcto.
- El nuevo metodo resuelve `rol_codigo` (LISTAGG, puede traer mas de un rol por usuario) y `supervisor_nombre` con subconsultas escalares en vez de `LEFT JOIN`, para no duplicar filas cuando un usuario tiene mas de un rol activo o mas de una relacion de supervision activa (le paso exactamente eso a `sdioses`, que tiene roles `SUPERVISOR_NOTIFICACION` + `VALIDACION` simultaneos). `UsuarioAsignableDTO.getDisplayName()` no muestra el rol ni el supervisor en el combo, asi que esta info solo viaja en el DTO por si se necesita en otro lado.

### Expandir/restaurar panel lateral en lenguetas de Notificacion (16/07/2026)

- Bug reportado: en la Bandeja Asignacion de Notificacion, hacer clic en una lengueta ya activa (`Datos`/`Asignacion`/`Firma`) no expandia ni restauraba el panel lateral, a diferencia del Panel de Asignacion del modulo Asignacion (donde clic sobre la lengueta activa alterna `splitOperativo.setSideExpanded(!isSideExpanded())`).
- Causa: `JPanelNotificacionV2.seleccionarTabAsigNotif(...)` y `seleccionarTabValidacion(...)` nunca implementaron esa logica al construirse (solo mostraban la card y actualizaban el estado visual de las lenguetas con `expandido` fijo en `false`); no es una regresion de un cambio reciente, faltaba desde la implementacion original de estas dos bandejas.
- Fix: ambos metodos ahora replican el patron de `JPanelAsignacionV2.seleccionarTabAsignacion(...)`: si el clic es sobre la MISMA lengueta ya activa (`mismaTab`) y el panel lateral esta visible, alternan `splitAsigNotif.setSideExpanded(...)` / `splitValidacionNotif.setSideExpanded(...)`; el estado `expandido` pasado a `tab.setState(activa, expandido)` ahora se calcula desde `split*.isSideExpanded()` en vez de estar hardcodeado a `false`.
- La pestaña "Bandeja Notificacion" (tercera pestaña superior, `splitOperativo`) no se toco: ya tenia su propio mecanismo de expandir/restaurar via `toggleSideExpanded()` en el chip de encabezado, funcionando correctamente.

### Estado del expediente en Notificacion: POR_ASIGNAR / POR_VALIDAR / "Por notificar", y columna "Estado" vs "Estado doc." (16/07/2026)

- Pedido explicito del usuario: ademas del estado del *documento* (columna ahora renombrada `Estado doc.`), cada una de las 3 bandejas de Notificacion debe mostrar el estado del *expediente/solicitud* (columna nueva `Estado`) y filtrar por el: Bandeja Asignacion solo `Por asignar`, Bandeja Validacion solo `Por validar`, Bandeja Notificacion solo `Por notificar`.
- Estados en etapa `NOTIFICACION`: `POR_ASIGNAR` (script 72, sesion anterior) y `POR_VALIDAR` (nuevo, `db/sdrerc_app/scripts/73_estado_por_validar_y_renombrar_por_notificar.sql`, ya ejecutado) son codigos genuinamente nuevos sin transiciones previas que dependieran de ellos. `POR_NOTIFICAR` NO es un codigo nuevo: se reutiliza `EN_NOTIFICACION` (mismo codigo, solo se le cambio el `nombre` visible a "Por notificar" via el mismo script 73). Decision deliberada: `NotificacionExpedienteDAO.estadoOrigenNotificacion(...)` exige literalmente el codigo `EN_NOTIFICACION` como origen de `NOTIFICACION_VIRTUAL`/`NOTIFICACION_PRESENCIAL_1` antes de siquiera consultar `flujo_transicion`; introducir un codigo nuevo habria obligado a reescribir esa cadena completa (intentos -> cargo pendiente -> cargo recibido -> `CONFIRMACION_NOTIFICACION` -> `NOTIFICADO`, este ultimo ya existente, sin cambios) en vez de solo renombrar la etiqueta.
- Flujo completo de transiciones de expediente en Notificacion, todo dentro de DAOs ya existentes (ninguna via `flujo_transicion`, siguiendo el patron ya usado por `asignarNotificacionMultiple` para `id_usuario_responsable_actual`, que tampoco pasa por `flujo_transicion`):
  1. Ejecucion (`derivarNotificacion`) o Verificacion (`aprobarVerificacionConDestino`, segundo salto encadenado) dejan el expediente en `POR_ASIGNAR`.
  2. `DocumentoAnalisisDAO.asignarNotificacionMultiple`/`reasignarNotificacion` (accion "Asignar"/"Reasignar" de la Bandeja Asignacion) resuelven el `codigo` del equipo destino (nueva funcion privada `resolverEstadoExpedienteDestinoNotificacion`) y fijan `expediente.id_estado_actual` en la misma actualizacion que ya tocaba `id_equipo_responsable_actual`/`id_usuario_responsable_actual`: `EQ_VALIDACION` -> `POR_VALIDAR`, `EQ_NOTIFICACION` -> `EN_NOTIFICACION`. Si el equipo no es ninguno de los dos, no se toca el estado (defensivo).
  3. `DocumentoAnalisisDAO.registrarResultadoValidacion` (Bandeja Validacion, accion Aprobado/Observado): en la rama Aprobado (documento pasa a `VALIDADO`), el expediente REVIERTE a `POR_ASIGNAR` para que el coordinador lo derive por segunda vez, ahora a `EQ_NOTIFICACION` (la condicion de Asignacion ya aceptaba `FINAL`+`VALIDADO` desde antes). En la rama Observado, el estado del expediente no se toca (solo se limpia el responsable, comportamiento ya existente); es deliberado, no un descuido: el documento queda `OBSERVADO`, que no matchea ninguna de las 3 condiciones de bandeja, asi que de todas formas deja de aparecer en Asignacion/Validacion/Notificacion sin necesitar tocar el estado del expediente.
- Las 3 condiciones de `DocumentoAnalisisDAO` ahora exigen ademas el estado del expediente (join nuevo a `estado_expediente eest` sobre `e.id_estado_actual`, ya usado desde la sesion anterior en Asignacion): `CONDICION_ASIGNACION_NOTIFICACION` exige `POR_ASIGNAR`, `CONDICION_VALIDACION_NOTIFICACION` exige `POR_VALIDAR`, `CONDICION_BANDEJA_NOTIFICACION` exige `EN_NOTIFICACION`.
- `NotificacionAsignacionDocumentoDTO` ahora expone `estadoExpedienteCodigo`/`estadoExpediente` (via el mismo join `eest`), ademas de `estadoDocumentoCodigo`/`estadoDocumento` que ya existian. Las 3 grillas de `JPanelNotificacionV2` (`asignacionNotifModel`, `validacionModel`, `notifBandejaModel`) renombraron su columna `Estado` a `Estado doc.` y agregaron una columna `Estado` nueva con el estado del expediente; en filas de expediente asociado (icono `+` de la Bandeja Asignacion), la columna `Estado` nueva reutiliza `estadoAsociadoAsigNotif(...)` (ya mostraba el estado del expediente asociado) y `Estado doc.` queda en `-` (no hay documento propio en esa fila).
- Correccion de datos puntual (script 73, paso 3): documentos ya asignados a un validador/notificador antes de esta regla, cuyo expediente habia quedado en `POR_ASIGNAR` sin actualizar, se retargearon a `POR_VALIDAR`/`EN_NOTIFICACION` segun el equipo real del documento (1 expediente afectado en el momento de ejecutar el script). Verificado extremo a extremo con `DocumentoAnalisisDAO.listarDocumentosAsignacionNotificacion/listarDocumentosValidacion/listarDocumentosNotificacion` antes de dar el cambio por cerrado.

### Combos "Equipo destino" del bloque "Destino operativo" restringidos por modulo (17/07/2026)

- Pedido explicito del usuario: cada combo "Equipo destino" (bloque "Destino operativo", presente en Asignacion/Verificacion/Notificacion) debe listar solo un subconjunto fijo de equipos, no todos los activos:
  - `JPanelAsignacionV2` (bloque "Asignacion de abogado"): solo `EQ_ANALISIS`. Nuevo metodo `esEquipoDestinoAsignacionValido(codigo)`.
  - `JPanelVerificacionV2` (bloque "Destino operativo"): `EQ_ANALISIS`, `EQ_EJECUCION`, `EQ_SUPERVISION`. Nuevo metodo `esEquipoDestinoVerificacionValido(codigo)`.
  - `JPanelNotificacionV2` (Bandeja Asignacion, bloque "Destino operativo"): `EQ_NOTIFICACION`, `EQ_VALIDACION` (ya filtrado antes) mas `EQ_ANALISIS`, `EQ_EJECUCION` (agregados ahora).
- El filtro se aplica en el metodo que puebla cada combo especifico (client-side, tras `listarEquiposActivos()`), no en el DAO/Service generico `listarEquiposActivos()`, que sigue devolviendo todos los equipos activos para otras pantallas (p. ej. Administracion > Equipo Juridico).
- Riesgo detectado en la entrada anterior (el segundo salto automatico se disparaba siempre hacia Notificacion sin importar el equipo elegido) ya fue resuelto: ver "Verificacion.aprobarVerificacionConDestino ramificado por equipo destino" mas abajo.

### Verificacion.aprobarVerificacionConDestino ramificado por equipo destino (17/07/2026)

- Pedido explicito del usuario, respuesta directa al riesgo detectado antes: el destino real del expediente al "Registrar verificacion" con destino debe depender del equipo elegido en el combo "Equipo destino" (bloque "Destino operativo"), no ser siempre `NOTIFICACION/POR_ASIGNAR`:
  - `Eq. Analisis` -> `ANALISIS/OBSERVADO` (el expediente vuelve a Analisis, mismo destino que "Devolver a Analisis").
  - `Eq. Ejecucion` -> `EJECUCION/EN_EJECUCION` (mismo destino que `aprobarVerificacionDirecta` usa para resoluciones).
  - `Eq. Supervision` -> `NOTIFICACION/POR_ASIGNAR` con el doble salto encadenado ya existente (comportamiento historico sin cambios).
- `VerificacionExpedienteDAO.aprobarVerificacionConDestino` ahora resuelve el `codigo` del equipo destino (`obtenerCodigoEquipo`, `SELECT codigo FROM equipo WHERE id_equipo = ?`) y delega en 3 metodos privados nuevos: `aprobarConDestinoAAnalisis`, `aprobarConDestinoAEjecucion`, `aprobarConDestinoANotificacion` (este ultimo conserva integro el codigo previo, incluido el doble salto). Cualquier equipo distinto de `EQ_ANALISIS`/`EQ_EJECUCION` cae en la rama de Notificacion (hoy solo puede ser `EQ_SUPERVISION` segun el whitelist del combo).
- Rama `Eq. Ejecucion`: NO reasigna `id_usuario_responsable_actual`/`id_equipo_responsable_actual` al usuario/equipo elegido en el combo (usa `actualizarExpediente`, no `actualizarExpedienteConDestino`). Motivo: la regla vigente de Ejecucion exige que el expediente lo atienda el mismo abogado que hizo Analisis, no un usuario arbitrario elegido en un combo; el combo solo sirve para habilitar/confirmar la accion, no para reasignar.
- Rama `Eq. Analisis`: SI actualiza responsable (`actualizarExpedienteConDestino`) al usuario/equipo elegido, igual que la rama de Notificacion, porque esa devolucion si implica reasignar quien atiende el expediente en Analisis. No inserta una fila estructurada en `EXPEDIENTE_OBSERVACION` (a diferencia del flujo dedicado "Devolver a Analisis"/`registrarObservacion`+`devolverAnalisis`): este punto de entrada solo recibe un `comentario` de texto plano, no un `ObservacionVerificacionDTO` estructurado (tipo/motivo/detalle), asi que el comentario queda solamente en `EXPEDIENTE_HISTORIAL`.
- Transicion de catalogo que faltaba: `DEVOLUCION_A_ANALISIS: VERIFICACION/EN_VERIFICACION -> ANALISIS/OBSERVADO` (las 2 filas previas de esa accion partian de `REQUIERE_CORRECCION`/`DOCUMENTO_INCONSISTENTE`, no de `EN_VERIFICACION`). Sembrada por `74_transicion_verificacion_destino_analisis.sql` (ya ejecutado, id_flujo_transicion=122). La transicion `Eq. Ejecucion` (`APROBACION_VERIFICACION: VERIFICACION/EN_VERIFICACION -> EJECUCION/EN_EJECUCION`) ya existia (misma fila que usa `aprobarVerificacionDirecta`), no requirio script.

### Orden e icono de acciones en grillas de "documentos analizados" (17/07/2026)

- Pedido explicito del usuario: en todas las grillas de documentos analizados que tienen columnas de accion (iconos), ese bloque de iconos debe ir al **inicio** de las columnas (antes de Tipo/Numero/Estado/etc.), en el orden `Guardar, Word, Eliminar` quedando solo los iconos que cada grilla realmente tiene.
- Archivos tocados (todos son puramente reordenamiento de indices de columna; ningun icono nuevo, ningun cambio de comportamiento):
  - `DocumentoAnalisisTreeGridPanelV2` (Analisis): tabla padre `Guardar(0), Word(1), Eliminar(2)` + datos despues; tabla hijo `Guardar(0), Eliminar(1)` + datos despues.
  - `DocumentoEjecucionTreeGridPanelV2` (Ejecucion): tabla padre `Guardar(0), Word(1)` + datos despues (esta grilla NUNCA tuvo icono Eliminar, ni en padre ni en hijo; no se agrego uno nuevo porque el pedido era reordenar, no añadir funcionalidad).
  - `DocumentoVerificacionTreeGridPanelV2` (Verificacion): tabla padre `Guardar(0)` + datos despues (ya solo tenia ese icono, sin Word/Eliminar, segun regla vigente de Verificacion).
  - `CartaRespuestaTreeGridPanelV2` (Cartas de respuesta, dentro de Asignacion): tabla hijo (documentos de respuesta) `Guardar(0)` + datos despues; la tabla padre no tiene iconos de accion.
  - `com.sdrerc.ui.views.notificacion.JPanelNotificacionV2` reutiliza directamente la clase `DocumentoEjecucionTreeGridPanelV2` para la grilla de documentos de su lengueta `Validar` (`documentosValidacionTreePanel`, sin `setHandlers`/`setCatalogos` propios); al reordenar esa clase compartida, esta grilla de Notificacion quedo reordenada automaticamente sin tocar `JPanelNotificacionV2`. No se encontro ninguna otra grilla con icono de accion en el modulo de Notificacion (se busco `SaveDocumentIcon`/`DeleteDocumentIcon`/`WordDocumentIcon` en todo el paquete `notificacion`).
- Mecanismo del cambio: cada clase ya usaba constantes simbolicas (`PADRE_COL_GUARDAR`, `HIJO_COL_ELIMINAR`, etc.) en switch/renderers, nunca indices literales sueltos, asi que renumerar las constantes y el array `columns` (nombres de cabecera) fue suficiente; no hizo falta tocar la logica de negocio. El metodo privado `ajustarAnchos(JTable, int[])` de cada clase se cambio a `ajustarAnchos(JTable, int startColumn, int[])` para poder seguir aplicando los anchos de las columnas de datos (que ahora empiezan en un indice distinto de 0) sin afectar el ancho ya fijado de las columnas de icono (`configurarColumnasAccion`).

### Administrador de plantillas de documento - Fase 2 (17/07/2026)

- Contexto: el usuario penso una idea de condicionales en `docs/plantillas/informe_rectificacion.docx` ("SI CANAL DE RECEPCION = MPV: ..." / "SI EL RESULTADO DEL ANALISIS = IMPROCEDENTE: ..."). Se diagnostico que esas lineas son texto plano instructivo para que un abogado las edite a mano en Word; el sistema YA tenia un motor de condicionales real (`AnalisisPlantillaDocumentoService.aplicarCondicionales`, marcadores `[[SI_...:...]] ... [[FIN_SI]]`) pero hardcodeado a solo 2 criterios (`ACTA`/`PROCEDIMIENTO`) y sin usar en ninguna plantilla real. Se propusieron 2 fases: Fase 1 (generalizar el marcador + aplicarlo a esas 2 plantillas, pequeño) y Fase 2 (modulo de administracion de plantillas). El usuario pidio arrancar por la Fase 2; la generalizacion del marcador se hizo igual porque es prerrequisito minimo y de bajo riesgo para que el modulo tenga sentido (ver variables/condicionales abajo). Aplicar los marcadores reales a los .docx de rectificacion sigue pendiente (Fase 1 propiamente dicha, no se toco el contenido de ningun .docx existente en esta sesion).
- Decision de arquitectura clave: las plantillas se guardan como **BLOB en Oracle** (`PLANTILLA_DOCUMENTO.contenido`), no como archivo en el file share. Motivo: el despliegue vigente hace que cada cliente **copie la app localmente** (`C:\SDRERC_CLIENTE\app`); un archivo dejado solo en el disco de quien lo sube no se veria en las demas maquinas hasta el proximo release. Oracle es el unico recurso realmente compartido y accesible en vivo por todos los clientes de la LAN.
- Tabla nueva `PLANTILLA_DOCUMENTO` (script `75_plantilla_documento.sql`, **ya ejecutado**): versionada por `id_tipo_documento_adjunto` (unique `(id_tipo_documento_adjunto, version)`), `contenido` BLOB, `activo` (solo una version activa por tipo, regla aplicada en Java no en BD), auditoria estandar (`creado_por`/`creado_en`/`modificado_por`/`modificado_en`). El mismo script siembra el permiso `MENU_ADMIN_PLANTILLAS` (modulo Administracion) y lo otorga a `ADMIN_SISTEMA`, mismo patron que `59_catalogo_permisos_menu.sql`.
- **Adopcion gradual, no migracion forzada**: al terminar la sesion la tabla queda vacia (0 filas) para los 22 tipos de documento de Analisis (`ANALISIS_DOC_*`/`ANALISIS_20_*`). Mientras un tipo de documento no tenga fila `activo=1`, `AnalisisPlantillaDocumentoService` sigue resolviendo la plantilla exactamente como antes (archivo en `docs/plantillas` por coincidencia aproximada de nombre, `resolverPlantilla`). Ningun archivo `.docx` existente se toco ni se cargo a la tabla en esta sesion (se hizo una carga+lectura+borrado de prueba durante la verificacion, con un archivo temporal marcado `..._TEST.docx`, borrado antes de terminar).
- `AnalisisPlantillaDocumentoService.generarDocumento(...)` ahora resuelve la plantilla en este orden: (1) `PlantillaDocumentoDAO.obtenerActivaPorCodigoTipo(codigo)` (BLOB activo para ese tipo, si existe) gana siempre sobre el archivo; (2) si no hay fila activa o la consulta falla por cualquier motivo (p.ej. tabla no aplicada todavia en un ambiente rezagado), se degrada sin excepcion al mecanismo de archivo ya existente. `nombreSugerido(...)` sigue la misma prioridad para decidir la extension sugerida.
- Marcador condicional generalizado (ya no hardcodeado a ACTA/PROCEDIMIENTO): `PATRON_SI` ahora acepta cualquier nombre de variable, `[[SI_<variable>:valor1|valor2]] ... [[FIN_SI]]`. `aplicarCondicionales`/`coincideCriterio` comparan contra el mismo `Map<String,String>` que ya resuelve los `#variable#` (sin mapa/tabla nuevo), con comparacion de nombre de variable insensible a mayusculas/guiones bajos (`canalRecepcion` y `CANAL_RECEPCION` son el mismo criterio) y de valores esperados insensible a mayusculas/tildes (reusa `normalizarClave`). Ejemplo real pendiente de aplicar en el .docx cuando se autorice la Fase 1: `[[SI_canalRecepcion:MPV]]...[[FIN_SI]]`, `[[SI_resAnalisis:PROCEDENTE|PROCEDENTE_EN_PARTE]]...[[FIN_SI]]`.
- Nuevas clases: `PlantillaDocumentoDTO` (metadata, sin el BLOB), `PlantillaDocumentoDAO` (BLOB via `setBytes`/`getBytes`, no streaming, porque los .docx de plantilla son pequeños; `insertarVersion` desactiva la version previa y calcula `MAX(version)+1` dentro de la misma transaccion; `activarVersion` permite reactivar una version anterior del historial, mismo patron transaccional), `PlantillaDocumentoService` (valida extension `.docx` y tamano maximo 15 MB, resuelve usuario actual igual que el resto de Services).
- Modulo UI nuevo `Administracion > Plantillas de documento` (`JPanelPlantillasDocumentoV2`, mismo patron visual que `JPanelFeriadosV2`: metricas + split listado/detalle): grilla de los 22 tipos de documento de Analisis (`listarTiposConPlantillaAnalisis`, `LEFT JOIN` a la version activa) con badge verde/gris segun tenga o no plantilla propia; panel derecho con "Cargar nueva version" (`JFileChooser` filtrado a `.docx`), "Descargar plantilla actual", historial de versiones con "Activar version seleccionada" (rollback), y "Ver variables disponibles" (dialogo de referencia estatico con la lista de `#variable#` y la sintaxis de `[[SI_...]]`, mantenido a mano igual que `docs/arquitectura_app/variables_plantillas_word.md`). Cableado en `MenuPrincipalV2` bajo la seccion "Administracion", permiso `MENU_ADMIN_PLANTILLAS`, icono reutilizado `AppV2IconProvider.FIRMA_EMISION` (no hay icono SVG dedicado a "plantillas" en `icons/appv2`).
- No se construyo (deliberadamente, para no sobre-construir): editor WYSIWYG de Word dentro de Swing (Word sigue siendo el editor real; el modulo solo versiona/publica el archivo), ni un motor de variables arbitrarias/libres (las variables siguen siendo la whitelist fija que ya devuelve `valores()`; agregar una variable nueva sigue requiriendo un cambio de codigo en `AnalisisPlantillaDocumentoService.valores(...)`). **Actualizacion (ver entrada siguiente):** el punto de "ni una tabla de reglas de condicion separada del marcador" quedo superado por el sistema de bloques de contenido pedido despues por el usuario, que SI agrega una tabla de condiciones con UI de formulario en vez de texto crudo en el Word.

### Bloques de contenido para plantillas (evolucion de Fase 2, 17/07/2026)

- Contexto: el usuario, tras entender que el administrador de plantillas de la entrada anterior es solo un gestor de versiones de archivos (sin editor dentro de la app), pidio investigar alternativas "premium" (`.dot`, librerias Java, campos `IF` nativos de Word). Se investigo con fuentes web: ni Apache POI ni docx4j evaluan campos `IF` de Word automaticamente (docx4j confirma en su propio issue tracker que no soportan MERGEFIELD/IF anidados), asi que esa ruta exigia escribir un parser de campos igual de complejo que el sistema de marcadores ya existente, con la limitante de que los campos `IF` de Word solo tienen 2 ramas (no listas OR). El usuario propuso en su lugar una idea propia mejor: administrar el contenido dinamico como una lista ordenada de "bloques" (titulo + contenido + condicion armada con combos) desde un formulario, sin tocar el Word a mano. Se diseño e implemento esa idea.
- Tabla nueva `PLANTILLA_BLOQUE` (script `76_plantilla_bloque.sql`, **ya ejecutado**): un bloque por fila, versionado por `id_tipo_documento_adjunto` (independiente de `PLANTILLA_DOCUMENTO`/la plantilla base, para que cambiar el membrete no afecte los bloques ya configurados). Condicion opcional por bloque: `variable_condicion` (nombre libre, mismo criterio de matching que `[[SI_...]]`), `operador_condicion` (`COINCIDE`/`NO_COINCIDE`, `CHECK` en BD), `valores_condicion` (lista separada por `|`, igual formato que los marcadores). Si `variable_condicion` es NULL, el bloque siempre se incluye. Baja logica (`activo`), nunca fisica.
- Mecanismo de insercion en `AnalisisPlantillaDocumentoService`: la plantilla base debe tener un parrafo con el texto exacto `[[CONTENIDO]]`; ahi se insertan, en orden, los bloques cuya condicion se cumple (titulo en negrita como parrafo aparte + contenido debajo, con sus `#variable#` intactos hasta que `reemplazarParrafos` los resuelve mas adelante junto con el resto del documento). Reusa integramente `coincideCriterio`/`buscarValorPorVariable` ya construidos para `[[SI_...]]` (mismo comportamiento de matching, cero logica duplicada); `coincideCondicionBloque` solo envuelve eso con el operador `COINCIDE`/`NO_COINCIDE`. Si la plantilla no tiene el marcador, o el tipo de documento no tiene bloques configurados, no pasa nada (compatibilidad total con plantillas que no usan esta funcionalidad).
- **Bug de Apache POI 5.2.5 encontrado y evitado** (importante para quien toque este codigo despues): `XWPFDocument.insertNewParagraph(XmlCursor)` -el metodo "oficial" documentado por POI para insertar un parrafo en medio de un documento- lanza `ClassCastException: XmlAnyTypeImpl cannot be cast to CTP` de forma consistente en esta version, tanto con documentos creados en memoria (`new XWPFDocument()`) como con documentos reales exportados desde Word (probado con `informe_rectificacion.docx`), y tanto dentro del jar shaded como con classpath plano (se descarto que fuera un problema de shading). Se evito por completo con una tecnica alternativa: los parrafos nuevos se crean con `document.createParagraph()` (metodo probado, sin problemas, que los agrega al final del cuerpo) y luego se reubican uno por uno justo antes del marcador con `XmlCursor.moveXml(cursorDestino)` (operacion generica de XMLBeans sobre el XML crudo, sin el casting problematico de POI). Mover siempre "justo antes del marcador", en orden de creacion, preserva el orden final correcto.
- **Segundo efecto colateral del mismo bug, tambien resuelto**: despues de usar `moveXml`, la lista de parrafos que POI cachea en memoria (`document.getParagraphs()`) queda desincronizada del XML real (movido por fuera de la API tipada de POI), y cualquier llamada posterior sobre el MISMO objeto `XWPFDocument` (incluida la propia eliminacion del parrafo marcador, o `reemplazarParrafos` recorriendo `getParagraphs()`) puede lanzar `XmlValueDisconnectedException` o simplemente no encontrar el nodo. Solucion: `generarDocxDesdeStream` ahora hace un **round-trip obligatorio** despues de `aplicarCondicionales`+`insertarBloques` (serializa a bytes en memoria y vuelve a abrir un `XWPFDocument` nuevo desde esos bytes) antes de seguir con `reemplazarParrafos`/tablas/headers/footers; la eliminacion del propio parrafo `[[CONTENIDO]]` (`eliminarMarcadorContenido`) se hace sobre ese documento ya reabierto, nunca sobre el que sufrio el `moveXml`. Verificado extremo a extremo con una plantilla real (`informe_rectificacion.docx` con el marcador insertado en un parrafo intermedio) y 3 bloques de prueba (uno sin condicion, uno que coincide, uno que no coincide) antes de dar el cambio por cerrado; los 3 casos se comportaron como se esperaba y los datos de prueba se borraron al terminar.
- Nuevas clases: `PlantillaBloqueDTO`, `PlantillaBloqueDAO` (`contenido` es CLOB, no VARCHAR2, para no limitar el tamano del texto de un bloque; `guardarOrden(List<Long> idsOrdenados, idUsuario)` reescribe `orden` de todos los bloques de una vez via batch, en una transaccion), `PlantillaBloqueService` (valida que si se activa condicion, variable+operador+valores vengan completos los tres juntos).
- UI nueva `DlgBloquesPlantillaV2` (dialogo modal, mismo patron que `DlgEditarEquipoJuridico`): lista de bloques del tipo de documento seleccionado (con resumen de titulo/condicion) + formulario de edicion (titulo, contenido, checkbox "Aplicar condicion" que habilita combo de variable + combo de operador ["Coincide con"/"No coincide con"] + campo de valores esperados separados por coma, convertidos a `|` internamente). Botones Nuevo/Guardar/Eliminar/Subir/Bajar (subir-bajar reescribe el orden completo via `guardarOrden`). Se abre desde el boton nuevo "Administrar bloques de contenido" en el panel de detalle de `JPanelPlantillasDocumentoV2` (junto a "Cargar nueva version"/"Descargar plantilla actual"/"Ver variables disponibles"), solo habilitado con un tipo de documento seleccionado. El dialogo de "Ver variables disponibles" del panel principal ahora tambien menciona este flujo y el requisito del marcador `[[CONTENIDO]]`.

### Bandeja Ejecucion excluia expedientes Improcedentes (17/07/2026)

- Reporte del usuario: expediente `SDRERC-EXP-2026-000108`, con resultado de analisis `Improcedente` y ya correctamente en etapa `EJECUCION/EN_EJECUCION`, no aparecia en la Bandeja Ejecucion. Regla vigente de Ejecucion: "Solo resoluciones pasan a Ejecucion" (por etapa/tipo de documento), sin condicionar por el resultado del analisis; Procedente, Improcedente y Procedente en parte deben tratarse igual una vez que el expediente ya esta en Ejecucion (cada uno con su propio flujo de anotacion textual/carta, pero todos visibles en la bandeja).
- Causa raiz: `EjecucionExpedienteDAO.buscarExpedientes` traia el filtro real correcto (`WHERE ... et.codigo = 'EJECUCION'`, mas visibilidad por asignacion), pero el `WHERE` externo del `SELECT * FROM (...)` agregaba ademas `AND UPPER(NVL(resultado_analisis, '')) IN ('PROCEDENTE', 'PROCEDENTE EN PARTE')`, que excluia silenciosamente cualquier expediente con resultado `Improcedente` (o sin resultado registrado) aunque ya estuviera legitimamente en Ejecucion. No hay evidencia de que esta restriccion respondiera a una regla de negocio real; no aparece documentada en ninguna parte de CLAUDE.md/AGENTS.md antes de este fix.
- Fix: se elimino ese filtro adicional (`") WHERE ROWNUM <= ?"` en vez de `") WHERE UPPER(NVL(resultado_analisis, '')) IN (...) AND ROWNUM <= ?"`); la bandeja ahora depende unicamente de la etapa del expediente y la visibilidad por asignacion, igual que las demas bandejas del sistema.
- Verificado contra datos reales antes y despues del fix: con el filtro bugueado la bandeja mostraba 2 expedientes (solo Procedente); despues del fix muestra 9 (7 Improcedente + 2 Procedente), incluyendo `SDRERC-EXP-2026-000108` con su resultado `Improcedente` correcto.

### Marcadores nombrados [[CONTENIDO:seccion]] para bloques de plantilla (17/07/2026)

- Contexto: al orientar al usuario sobre como reutilizar `docs/plantillas/informe_rectificacion.docx` con el administrador de plantillas, se detecto que ese documento real tiene **dos** puntos distintos de contenido condicional (uno en "I. Antecedentes" por canal de recepcion, otro en "III/IV Conclusiones-Recomendaciones" por resultado del analisis), y el marcador `[[CONTENIDO]]` sin nombre construido en la entrada anterior solo soporta un punto de insercion por documento. Se extendio para soportar multiples marcadores nombrados en el mismo documento.
- Columna nueva `PLANTILLA_BLOQUE.SECCION` (script `77_plantilla_bloque_seccion.sql`, **ya ejecutado**, `ALTER TABLE ADD COLUMN`, nullable): si esta vacia, el bloque sigue yendo al marcador sin nombre `[[CONTENIDO]]` (compatibilidad total con lo que ya existia); si tiene un valor (ej. `antecedentes`), el bloque solo se inserta en el marcador nombrado `[[CONTENIDO:antecedentes]]`. Una misma plantilla puede tener varios marcadores nombrados distintos, cada uno recibiendo solo los bloques de su propia seccion.
- `AnalisisPlantillaDocumentoService`: `PATRON_MARCADOR_CONTENIDO` (regex `\[\[CONTENIDO(?::([A-Za-z0-9_]+))?\]\]`) reemplaza el literal fijo anterior; `insertarBloques` ahora agrupa los bloques por `seccion` normalizada (`normalizarNombreVariable`, mismo criterio insensible a mayusculas/guiones que ya se usaba para nombres de variable), busca TODOS los parrafos marcador del documento (`buscarParrafosMarcador`, ya no uno solo) y, para cada uno, inserta solo el grupo de bloques de su propia seccion (misma tecnica `createParagraph`+`XmlCursor.moveXml` de la entrada anterior, sin cambios). `eliminarMarcadorContenido` tambien pasa a eliminar TODOS los marcadores encontrados (iterando en reversa, mismo patron ya usado por `aplicarCondicionales`), no solo uno.
- **Gap real encontrado y corregido de paso**: la variable `resAnalisis` (resultado del analisis: Procedente/Improcedente/...) estaba **documentada** en comentarios y en el dialogo "Ver variables disponibles" desde la sesion anterior, pero **nunca se agrego de verdad** al `Map<String,String> valores` que arma `AnalisisPlantillaDocumentoService.valores(...)` — cualquier condicion o `#resAnalisis#` que alguien hubiera intentado usar habria quedado siempre vacia. Se agrego `values.put("resAnalisis", valor(expediente.getUltimoResultadoAnalisis()))` (el campo ya existia en `AnalisisExpedienteDTO`, solo no estaba conectado al mapa).
- UI: `DlgBloquesPlantillaV2` agrega el campo "Sección (opcional)" al formulario (en blanco = marcador sin nombre); el resumen de cada bloque en la lista izquierda ahora antepone `[seccion]` cuando aplica. El texto de "Ver variables disponibles" en `JPanelPlantillasDocumentoV2` explica la sintaxis de marcadores nombrados con el ejemplo concreto antecedentes/recomendaciones.
- Verificado extremo a extremo con `informe_rectificacion.docx` real modificado con 2 marcadores (`[[CONTENIDO:antecedentes]]` y `[[CONTENIDO:recomendaciones]]`) y 4 bloques de prueba (2 por seccion, cada par con una condicion que coincide y otra que no): el resultado solo incluyo los 2 bloques correctos (uno por seccion), sin mezclarse entre secciones, y ambos marcadores quedaron removidos. Datos de prueba eliminados al terminar.

### Panel de Analisis se cerraba solo al recibir un expediente (20/07/2026)

- Reporte del usuario: en `JPanelAnalisisV2`, al hacer clic en la lengueta de documentos con un expediente "recibible" (etapa ASIGNACION/estado ASIGNADO), aparece la confirmacion "¿Desea recibir el expediente asignado?"; al aceptar, el panel derecho de Analisis se cerraba solo, cuando deberia seguir abierto mostrando el mismo expediente ya recibido.
- Causa raiz: tras `recibirExpediente(...)`, el flujo llama `buscar(idExpediente)` para refrescar la grilla y reseleccionar el mismo expediente. `buscar(...)` primero llama `cargarTabla(...)`, que hace `table.clearSelection()` antes de repoblar la tabla; ese instante intermedio "sin seleccion" dispara `actualizarSeleccion() -> actualizarVisibilidadPanelAnalisis()`, que oculta el panel (`obtenerFilaSeleccionada() == null`). Recien despues `buscar(...)` llama `seleccionarFilaPorId(...)` para reseleccionar el mismo expediente, pero `actualizarVisibilidadPanelAnalisis()` tiene un guard `if (!splitOperativo.isSideVisible()) return;` que, una vez el panel quedo oculto por el paso anterior, impide que se reabra automaticamente aunque la seleccion se restaure correctamente. Ese guard es intencional para OTROS casos (no abrir el panel solo por cambiar de fila con un clic simple, regla "doble clic abre panel"), pero no distingue "el usuario cerro el panel a proposito" (ya cubierto por el flag existente `panelAnalisisCerradoPorUsuario`) de "el panel se oculto como efecto secundario transitorio de un refresco de grilla".
- Fix, sin tocar el guard general (para no romper la regla de doble clic en otras bandejas): `buscar(Long idExpedienteASeleccionar)` ahora captura `panelAnalisisAbiertoAntes` (si `splitOperativo.isSideVisible()` antes de lanzar la busqueda); si tras refrescar la reseleccion del mismo expediente tiene exito (`seleccionarFilaPorId` retorna true) y el panel estaba abierto antes, se reabre explicitamente (`splitOperativo.setSideVisible(true)`, `panelAnalisisCerradoPorUsuario = false`). Si el expediente ya no aparece en la bandeja tras el refresco (p.ej. quedo archivado o cambio de etapa fuera del filtro actual), la reseleccion falla y el panel se mantiene cerrado, comportamiento correcto.
- Mismo bug encontrado en un flujo hermano: `confirmarYEjecutar(...)` (usado por el boton "Recibir expediente" del panel ademas de la lengueta, y por otras acciones como "Registrar resultado final"/"Archivar - No corresponde") llamaba `buscar()` sin id al terminar, perdiendo la reseleccion por completo (ni siquiera intentaba reabrir). Se corrigio capturando `obtenerSeleccionado()` antes de ejecutar la operacion y pasando ese id a `buscar(idExpedienteSeleccionado)`, beneficiando a todos los llamadores de `confirmarYEjecutar` con `limpiarYBuscarAlFinal=true` sin duplicar logica.
- Alcance: se corrigio solo el modulo de Analisis, que fue el reportado. Verificacion/Ejecucion/Notificacion no se tocaron; si presentan el mismo patron (refrescar grilla + reseleccionar tras una accion), revisar con el mismo diagnostico si se reporta.

### Titulo generico "Panel de datos" en todos los modulos (20/07/2026)

- Pedido explicito del usuario: en TODOS los paneles derechos, la lengueta/panel puramente informativo (plazo/expediente/acta/solicitud/titular/solicitante/notificacion-ubicacion, sin botones de accion) debe titularse siempre `Panel de datos`, generico e identico en todos los modulos; NO debe decir el nombre del modulo (`Panel de Analisis`, `Panel de Asignacion`, `Panel de Verificacion`, `Panel de Ejecucion`, `Panel de Registro`, etc.). Esto contradice lo documentado antes (CLAUDE.md/AGENTS.md indicaban titulo `Panel de Registro`/`Panel de Asignacion` para ese panel); ambos archivos se actualizaron para reflejar la regla nueva.
- Importante: esta regla aplica **solo** al panel/lengueta puramente informativo (el que en cada modulo se llama lengueta `Datos`). Las demas lenguetas de accion (`Verificar`, `Asignacion` operativa, `Asociar`, `Ejecutar`, `Analizar`/formulario de resultado, `Firma`, `Validar`, `Cierre`, `Cartas de respuesta`, el panel unico de la Bandeja Notificacion final) **conservan** su titulo especifico de modulo/accion; no se tocaron.
- Archivos y metodos modificados (todos son cambios de solo el string del titulo, sin tocar estructura ni logica):
  - `JPanelBandejaExpedientesNueva` (compartido por `Bandeja de Expedientes` general y por `Registro / Recepcion`, ver `perfilRegistroRecepcion`): `crearPanelRecepcion()` (titulo inicial) y `actualizarTituloPanelRecepcion(...)` (titulo dinamico con titular debajo, el que en la practica ve el usuario tras seleccionar una fila) — ambos "Panel de recepción"/"Panel de Registro" -> `Panel de datos`.
  - `JPanelVerificacionV2.crearPanelDatosVerificacion()`: "Panel de Verificación" -> `Panel de datos`. `crearPanelVerificacionOperativa()` (lengueta `Verificar`) sin cambios.
  - `JPanelAsignacionV2.crearPanelDatosExpediente()` y `actualizarTituloPanelAsignacionPorItem(...)` (variable `titulo`, tambien reutilizada por `panelCartasRespuesta` que ya heredaba el mismo texto): "Panel de Asignación" -> `Panel de datos`. `crearPanelAsignacionOperativa()` (lengueta `Asignacion`) y `crearPanelAsociar()` (lengueta `Asociar`) sin cambios.
  - `JPanelAnalisisV2.crearPanelDatosAnalisis()` y `actualizarTituloPanelAnalisis(...)` (titulo HTML dinamico con titular debajo): "Panel de análisis"/"Panel de Análisis" -> `Panel de datos`. `crearPanelAnalisis()` (formulario+documentos) y `crearPanelResultadoAnalisis()` (metodo sin usar, dead code) sin cambios.
  - `JPanelEjecucionV2.crearPanelDatosEjecucion()`: "Panel de Ejecución" -> `Panel de datos`. `crearPanelEjecucionOperativa()` (lengueta `Ejecutar`) sin cambios.
  - `JPanelNotificacionV2`: lengueta `Datos` de la Bandeja Asignacion (`datosAsigNotif.crearPanel(...)`, antes "Panel de Asignación") y lengueta `Datos` de la Bandeja Validacion (`datosValidacionNotif.crearPanel(...)`, antes "Panel de Validación") -> `Panel de datos`. Sin cambios: lengueta `Asignacion` operativa, lengueta `Firma`, lengueta `Validar` operativa (incluye el `JLabel` interno `lblPanelValidacionTitulo` que muestra "Panel de Validación - <numero>" dentro de esa misma lengueta, no es el titulo del panel), el panel unico de la Bandeja Notificacion final ("Panel de notificación", no tiene lengueta `Datos` separada) y `Cierre`.
- No se toco `JPanelFirmaEmisionV2` ("Panel de firma y emisión"): es codigo legacy sin ninguna referencia/uso en el resto del proyecto (no esta cableado a ningun menu), consistente con la regla de no reactivar Firma/Emision como modulo lateral independiente.
- Modulos con un unico panel sin lenguetas Datos/accion (`JPanelExpedienteDigitalV2`, `JPanelCierreArchivoV2`, `JPanelPublicacionV2`) no aplicaban esta regla y no se tocaron.

### Nuevo documento en Ejecucion nacia en estado Emitido en vez de En proyecto (20/07/2026)

- Reporte del usuario: al agregar un documento nuevo (boton `+ Documento`) en el bloque de documentos del panel de Ejecucion, el estado inicial del documento debe ser `En proyecto`, igual que en Analisis.
- Causa raiz: `DocumentoEjecucionTreeGridPanelV2.primerEstado()` (el metodo que resuelve el estado por defecto de una fila nueva) buscaba el estado cuyo codigo coincidiera con la constante `ESTADO_VISIBLE_PADRE = "EMITIDO"`, en vez de buscar `EN_PROYECTO` como hace el metodo equivalente en `DocumentoAnalisisTreeGridPanelV2` (el patron de referencia que Ejecucion dice replicar). Esto hacia que cada documento nuevo apareciera directamente como `Emitido`.
- Fix: `primerEstado()` ahora busca `EN_PROYECTO` (mismo criterio que Analisis), con el mismo fallback al primer estado del catalogo si no se encuentra. Se elimino la constante `ESTADO_VISIBLE_PADRE`, que tras el fix quedo sin ningun otro uso en el archivo (no filtraba la grilla como su nombre sugeria; solo se usaba en este metodo).

### Fecha Emision de un documento nuevo solo debe mostrarse si el estado es Emitido (20/07/2026)

- Pedido explicito del usuario, en Analisis y Ejecucion: un documento nuevo en la grilla de "documentos analizados" no debe traer la Fecha Emision precargada; la fecha solo debe aparecer cuando el `Estado` del documento (columna/combo de esa misma fila) es `Emitido`. Si se elige cualquier otro estado, la Fecha Emision debe quedar en blanco.
- Dos partes del bug/comportamiento faltante:
  1. **Valor inicial de un documento nuevo**: `DocumentoAnalisisTreeGridPanelV2.DocumentoRow.base(...)` seteaba `row.fechaDocumento = LocalDate.now()` incondicionalmente para cualquier fila nueva (padre o hijo), sin importar el estado con el que nacia (que ya es `EN_PROYECTO` por el fix anterior). Se corrigio a `row.fechaDocumento = esEstadoEmitido(estado) ? LocalDate.now() : null` (nuevo helper estatico `DocumentoRow.esEstadoEmitido(CatalogoItemDTO)`, compara `codigo` contra `"EMITIDO"`). `DocumentoEjecucionTreeGridPanelV2.DocumentoRow.base(...)` ya dejaba `fechaDocumento` en `null` por defecto (no seteaba nada), asi que esa mitad no tenia el bug ahi; se le agrego el mismo helper `esEstadoEmitido(...)` para reutilizar en el punto 2.
  2. **Reactivo al cambiar el combo de Estado en una fila ya creada**: en ambos `PadreTableModel.setValueAt(...)` (caso `PADRE_COL_ESTADO_DOCUMENTO`), al cambiar el estado de la fila se evalua `DocumentoRow.esEstadoEmitido(...)`: si el nuevo estado es `Emitido` y la fecha estaba vacia, se autocompleta con `LocalDate.now()` (el usuario puede corregirla despues con el selector de fecha, columna Fecha Emision sigue editable); si el nuevo estado es cualquier otro, la fecha se limpia a `null` de inmediato. No se toco `HijoTableModel` (Analisis) porque la tabla hijo no tiene columna de Estado, solo Fecha Respuesta (logica distinta, fuera de este pedido).
- Alcance: solo Analisis y Ejecucion, que fueron los modulos mencionados. Verificacion no se toco (su grilla de documentos revisados no permite crear filas nuevas, solo editar `Estado`/`Detalle Obs.`/`Fecha Emision`/`N° Documento` de documentos ya emitidos desde Analisis).

### Mensaje de validacion inapropiado al registrar analisis sin resultado (20/07/2026)

- Reporte del usuario: al intentar "Registrar resultado final" en Analisis sin haber seleccionado el `Resultado` del analisis, aparecia un mensaje de validacion "inapropiado" (`ERROR ILEGAL...`). No se encontro ningun texto literal "ILEGAL"/"Illegal" en el codigo del proyecto ni un manejador global de excepciones no capturadas; el mensaje real de validacion (`AnalisisValidacionService.validarRegistroAnalisis`: "Seleccione el resultado del análisis.") ya era correcto en texto, pero el FLUJO con el que se mostraba era confuso: `registrarAnalisis()` primero mostraba el dialogo de confirmacion "Se registrará el resultado final... ¿Desea continuar?" (`confirmarYEjecutar`) y solo DESPUES de que el usuario confirmaba, la validacion corria dentro del `SwingWorker` y fallaba con una `IllegalArgumentException` que llegaba a `mostrarError` envuelta en `ExecutionException` (titulo "Error de análisis"). La sospecha mas probable es que el usuario interpreto ese flujo/titulo tecnico (excepcion `IllegalArgumentException` de por medio) como el mensaje "inapropiado".
- Fix, en `JPanelAnalisisV2.registrarAnalisis()`: la validacion ahora corre **antes** de mostrar el dialogo de confirmacion, llamando a un nuevo metodo publico `AnalisisExpedienteService.validarRegistroAnalisis(AnalisisRegistroDTO)` (delega en el mismo `AnalisisValidacionService.validarRegistroAnalisis(...)` que ya usaba `registrarAnalisis(...)`, sin duplicar reglas). Si hay errores, se muestran de inmediato con `JOptionPane.showMessageDialog(..., JOptionPane.WARNING_MESSAGE)` (icono de advertencia, no de error/excepcion) con el titulo "Registrar resultado final" y el texto claro, y la accion se corta ahi: ya no se le pregunta al usuario "¿Desea continuar?" para una accion que de entrada no puede completarse, ni el mensaje pasa por el envoltorio de excepcion (`ExecutionException`/`IllegalArgumentException`) de `confirmarYEjecutar`/`mostrarError`.
- Alcance: solo el boton "Registrar resultado final" de Analisis, que fue el caso reportado. Otras acciones que usan `confirmarYEjecutar` (`recibir`, `archivarNoCorresponde`, etc.) conservan su flujo actual (validan dentro de la operacion, despues de confirmar) porque no fueron reportadas.

### Documentos/solicitudes asociadas en Analisis: panel de datos vacio y sin boton para recibir (20/07/2026)

- Reporte del usuario: al hacer doble clic en una fila de solicitud asociada/duplicada dentro de la Bandeja Analisis, el "Panel de datos" no mostraba informacion; ademas no existia ningun boton/opcion para "recibir" ese documento asociado o duplicado, y esto debia funcionar tanto antes como despues de asociar y de asignar.
- Causa 1 (panel vacio): en `JPanelAnalisisV2.actualizarSeleccion()`, la rama `if (asociado) { ... }` llamaba `cargarDatosExpedienteAnalisis(null)`, y ese metodo, al recibir `null`, simplemente ejecuta `limpiarDatosExpedienteAnalisis()` (pone "-" en todas las etiquetas del bloque "Panel de datos"). Solo las etiquetas fuera de ese bloque (expediente, titular, acta, procedimiento, responsable, etapa/estado, alertas) se llenaban desde el `ExpedienteRelacionadoDTO` de la fila asociada.
- Fix 1: nuevo metodo `cargarDatosExpedienteAsociado(ExpedienteRelacionadoDTO relacionado)` que limpia el bloque y luego llena con los datos que si trae el DTO de relacionado (numero de tramite web, N° expediente SGD, fecha de recepcion, numero de expediente, estado -reutilizando el helper `estadoAsociado(...)`-, tipo/numero de acta, numero de documento, solicitante, dias/vencimiento vía `actualizarBadgeDias`, y equipo asignado). Campos que el DTO de relacionado no trae (tipo/numero de documento del titular, canal de ingreso, prioridad, datos de contacto/ubigeo del solicitante, etc.) quedan en "-" como antes, en vez de mostrar datos de otro expediente por error. Se invoca en el mismo punto donde antes se llamaba `cargarDatosExpedienteAnalisis(null)`.
- Causa 2 (sin boton "recibir"): existian ya, sin usar, una clase `RecibirAsociadoRenderer`/`RecibirAsociadoEditor` (icono `AppV2ReceiveActionButton`) y los metodos `puedeRecibirDocumentoAsociado(int)`/`recibirDocumentoAsociado(int)` completamente implementados, pero la tabla `documentosAsociadosTable` (mini-grilla de documentos asociados dentro del "Panel de datos") solo tenia 2 columnas ("N° expediente SGD", "Estado") con `isCellEditable(...)` retornando `false` siempre: nunca se agrego una tercera columna de accion, asi que el boton jamas se renderizaba ni era clickeable. No era un problema de la condicion de habilitado (`puedeRecibirDocumentoAsociado`, que exige `etapa=ASIGNACION` y `estado=ASIGNADO`, igual que exige `AnalisisExpedienteDAO.recibirDocumentoAsociado`/`requerirTransicionRecepcion` a nivel de BD), sino que el control nunca aparecia en absoluto.
- Fix 2: se agrego la tercera columna `"Acción"` al `documentosAsociadosModel` (editable solo esa columna), con `setCellRenderer(new RecibirAsociadoRenderer())`/`setCellEditor(new RecibirAsociadoEditor())` en `configurarDocumentosAsociadosTabla()`, y el valor placeholder `"Recibir"` al construir cada fila en `actualizarDocumentosAsociadosPanel(...)`. El boton ahora aparece siempre en la mini-grilla; su estado visual (habilitado/deshabilitado/"recibido") ya dependia correctamente de `puedeRecibirDocumentoAsociado(...)`, que refleja el ciclo de vida real: antes de que el duplicado llegue a `ASIGNACION/ASIGNADO` o despues de ya haber sido recibido, el boton se muestra deshabilitado con tooltip explicativo en vez de simplemente no existir; en el momento en que el documento asociado si esta en `ASIGNACION/ASIGNADO`, el boton queda habilitado y funcional.
- No se toco `AnalisisExpedienteDAO.recibirDocumentoAsociado` ni la transicion `FLUJO_TRANSICION` que usa (`ASIGNACION/ASIGNADO -> ANALISIS/RECIBIDO_POR_ABOGADO`, la misma que usa `recibirExpediente` para el principal): esa regla de negocio ya era correcta, el problema era exclusivamente de UI (control ausente y datos no pintados).

### Grupo Familiar Fase 2: ID de grupo real a nivel de persona (20/07/2026)

- Evoluciona la Fase 1 (`27_grupo_familiar_fase1.sql`, solo un flag booleano en `EXPEDIENTE_SOLICITUD` sin vincular integrantes entre si) hacia un ID de grupo familiar real. Script `80_grupo_familiar_fase2.sql` (ya ejecutado): crea tabla ancla `GRUPO_FAMILIAR` (solo ID + columnas de auditoria) y agrega `PERSONA.id_grupo_familiar` (nullable, FK a `GRUPO_FAMILIAR`, indice `ix_persona_grupo_familiar`); siembra el tipo de movimiento `ASOCIACION_GRUPO_FAMILIAR` para historial.
- El vinculo se hace a nivel `PERSONA`, no por expediente: si la misma persona (misma fila de `PERSONA`) vuelve a ser titular de un expediente futuro, hereda el grupo familiar automaticamente sin necesidad de volver a asociarlo. Decision confirmada explicitamente por el usuario tras pregunta de aclaracion ("A la persona" vs "al expediente").
- El ID de grupo es solo interno (numero tecnico sin codigo visible tipo `SDRERC-EXP-...`); no se expone como codigo amigable en UI, solo se usa para saber que otras personas comparten el mismo grupo.
- No reutiliza `EXPEDIENTE_RELACION` (esa tabla es para duplicados, que heredan numero de expediente/equipo/abogado; grupo familiar no debe heredar nada de eso, solo marca "Si" en Grupo Familiar).
- Backend: `GrupoFamiliarDAO`/`GrupoFamiliarService` (nuevos). `listarPosiblesIntegrantes(idExpediente)` detecta candidatos por coincidencia de apellidos del titular reutilizando `GrupoFamiliarHeuristicaService.claveApellidosTitular(...)` (misma heuristica que ya usaba Carga diaria). `asociarGrupoFamiliar(...)` crea o une un grupo compartido con el mismo patron de resolucion de ambiguedad ya usado para duplicados: si los candidatos marcados pertenecen a mas de un grupo existente distinto, bloquea la accion con mensaje claro en vez de adivinar.
- UI: panel "Grupo Familiar" con la misma estructura que "Asociar duplicados" (candidatos con checkbox + boton de asociacion + tabla de solo lectura de integrantes actuales del grupo). En Registro evoluciona el panel simple que ya existia (`JPanelRegistrarGrupoFamiliarV2`, lengueta contextual por seleccion de casillas); en Asignacion se agrega como lengueta nueva `Grupo Familiar` en `JPanelAsignacionV2` (no existia ninguna).

### Panel de Grupo Familiar (Registro y Asignacion): panel se cerraba al asociar, alerta/estado no se refrescaban, grillas con diseno rigido (21/07/2026)

- Reporte del usuario tras revisar el panel de Grupo Familiar en Asignacion (aplica igual a Registro por el mismo patron de codigo):
  1. Al hacer clic en "Asociar al grupo familiar" el panel lateral se cerraba solo; debia quedarse abierto.
  2. La alerta `Posible Grupo Familiar` no se desactivaba automaticamente tras confirmar la asociacion.
  3. Al reabrir el panel del mismo expediente, seguia mostrando el conteo viejo de "posible(s) integrante(s)" en vez de reflejar que ya no quedan candidatos pendientes.
  4. La grilla "Coincidencias por apellidos del titular" seguia listando personas que ya pertenecian a un grupo familiar (propio o ajeno), cuando ya no deberian aparecer como "posible integrante".
  5. El boton de asociar no bajaba a deshabilitado en ese escenario (consecuencia directa del punto 4: sin filas, no hay nada que marcar).
  6. Faltaba mostrar, en el bloque "Asociacion" encima de "Accion", el expediente/titular actualmente enfocado.
  7. Las 2 grillas del panel (candidatos e integrantes actuales) truncaban el contenido con puntos suspensivos, no tenian filtro por columna, usaban un tamano fijo en pixeles que a veces ocultaba filas completas (0 filas visibles con el panel angosto) y carecian de scroll horizontal como alternativa al vertical.
- Causa 1 y 2 (cierre de panel + alerta): `GrupoFamiliarDAO.listarPosiblesIntegrantes` no excluia candidatos que ya tuvieran `id_grupo_familiar` asignado (de cualquier grupo), y `asociarGrupoFamiliar` no llamaba a `ExpedienteAlertaDAO.marcarAtendidas(...)` para la alerta `Posible Grupo Familiar` tras confirmar, a diferencia del patron ya usado por `ExpedienteRegistroDAO.registrarGrupoFamiliar`. Ademas, en la UI, el refresco posterior a la asociacion llamaba a un `buscar()`/`refrescar()` sin argumento, que en ambos modulos limpia la seleccion de tabla (`table.clearSelection()`) y/o oculta explicitamente el panel lateral (`ocultarPanelRecepcion()` en Registro) sin volver a abrirlo, el mismo patron de bug ya corregido antes en Analisis para "Recibir expediente".
- Fix 1 (backend): `GrupoFamiliarDAO.listarPosiblesIntegrantes` ahora excluye (con `continue`) cualquier candidato cuyo `id_grupo_familiar` ya no sea nulo, sin importar a que grupo pertenezca. `asociarGrupoFamiliar` ahora llama `expedienteAlertaDAO.marcarAtendidas(conn, idExpediente, Collections.singletonList("Posible Grupo Familiar"), idUsuario)` por cada expediente asociado, igual que Registro Fase 1.
- Fix 2 (UI, ambos modulos): se agregaron sobrecargas `buscar(Long idExpedienteAReseleccionar)`/`refrescar(Long idExpedienteAReseleccionar)` y `cargarTabla(items, Long idExpedienteAReseleccionar)` que capturan si el panel estaba abierto y en que lengueta/tarjeta antes de refrescar, reseleccionan la misma fila por ID despues del refresco, y restauran explicitamente la visibilidad/expansion del panel y la lengueta activa si estaba abierta antes. Los metodos de asociacion (`asociarGrupoFamiliarSeleccion` en Asignacion; `asociarMarcadosMasivo`/`asociarGrupoFamiliarDeteccion` en Registro) ahora llaman a estas sobrecargas pasando el expediente principal en foco, en vez de un refresco ciego; la reseleccion ya dispara por si sola la recarga de "posibles integrantes" (no hace falta una segunda llamada manual).
- Fix 3 (label "Expediente"): nuevo `JLabel` (`lblExpedienteFocoGrupoFamiliar`, "-" cuando no hay seleccion) agregado como primera fila del bloque "Asociacion", antes de "Accion", con el formato `N° expediente / Nombres del Titular`, en ambos modulos.
- Fix 4 (rediseno de grillas, ambos modulos): las 2 grillas de cada panel (`AppV2TablePanel` ya existente) ahora usan `AppV2ColumnFilterSupport.install(...)` (filtro por columna, igual patron que el resto de bandejas del sistema), `AppV2TableColumnSizer.sizeToContent(...)` para eliminar los puntos suspensivos (el ancho de columna se ajusta al contenido real en vez de quedar fijo y truncar), `JScrollPane.VERTICAL_SCROLLBAR_NEVER` + `JTable.AUTO_RESIZE_OFF` (scroll horizontal disponible cuando el ancho del panel es menor al contenido, nunca scroll vertical), y un helper `ajustarAlturaGrillaSinScrollVertical(table, scrollPane)` que recalcula la altura preferida del `JScrollPane` como `filas * alturaFila + alturaEncabezado` cada vez que cambian los datos, para que las "n" filas se vean siempre completas sin importar el ancho del panel. Se eliminaron los `setPreferredSize(new Dimension(...))` fijos que antes limitaban el area visible de las grillas.
- El helper de altura esta duplicado como metodo privado en cada clase (`JPanelAsignacionV2` y `JPanelBandejaExpedientesNueva`, clase externa de `JPanelRegistrarGrupoFamiliarV2`) porque no hay una utilidad compartida para ese calculo; no se centralizo para no tocar mas archivos de los necesarios para esta tarea.

### Retirar integrante del grupo familiar desde "Grupo familiar actual" (21/07/2026)

- Pedido del usuario: agregar un icono "x" en la primera columna de la grilla "Grupo familiar actual" (Registro y Asignacion) para retirar a una persona del grupo. Tambien reporto que, tras las mejoras anteriores del mismo dia, el expediente actualmente seleccionado (con Grupo Familiar = "Si") a veces ya no aparecia como integrante de su propio grupo en esa grilla; antes si aparecia.
- Sobre el punto del expediente propio que no aparece: se reviso a fondo `GrupoFamiliarDAO.listarIntegrantesGrupoFamiliar` y `asociarGrupoFamiliar`; la asociacion SI actualiza `PERSONA.id_grupo_familiar` para el expediente principal igual que para los candidatos (`idsExpedientes.add(idExpedientePrincipal)` antes del loop que actualiza a todos), y la consulta de integrantes (`WHERE p.id_grupo_familiar = ?`) usa LEFT JOIN hacia expediente/etapa/estado, por lo que estructuralmente no deberia poder excluir la fila de la persona ancla. No se pudo reproducir en vivo (la primera captura "despues" adjunta por el usuario correspondia a la bandeja general, no al panel abierto) ni verificar contra BD (credenciales del `config/sdrerc-app.properties` local no coinciden con la instancia Oracle disponible en este entorno). Queda pendiente de confirmar con un caso concreto (numero de expediente) si el sintoma persiste; no se aplico ningun cambio especulativo sobre esta consulta para no enmascarar una causa real no confirmada.

### Causa real encontrada: "Grupo familiar actual" recortaba la ultima fila (la del expediente propio) por un problema de layout, no de datos (21/07/2026)

- El usuario adjunto una segunda captura ("despues") mostrando SI el panel abierto: expediente en foco `SDRERC-EXP-2026-000052 / MAGGIOLO SOLANO PEDRO FRANCÍSCO`, "Estado: 3 persona(s) en el grupo familiar" pero la grilla solo pintaba 2 filas (ADA MIRIAM y JUAN JOSÉ, orden alfabetico), sin la fila de PEDRO FRANCÍSCO (el expediente propio, ultimo alfabeticamente) y sin scroll vertical para revelarla.
- Diagnostico: el texto de estado usa `integrantes.size()` (3, correcto, viene de la misma lista que llena la tabla) mientras que la tabla se llena con un `for` simple sin filtros ni dedup (`grupoFamiliarActualModel.addRow(...)`/`modeloGrupoActual.addRow(...)` por cada integrante) — es decir, el modelo de la tabla SI llega a tener las 3 filas. La discrepancia era puramente visual: `ajustarAlturaGrillaSinScrollVertical(table, scrollPane)` calculaba la altura correcta (`filas * rowHeight + alturaEncabezado`) pero solo llamaba `scrollPane.revalidate()`; al estar la grilla anidada dentro de secciones/paneles con su propio scroll vertical (el panel lateral completo ya scrollea, ver regla "Panel derecho con lenguetas... scroll vertical interno"), ese `revalidate()` no siempre alcanzaba a reflejarse en el layout real a tiempo, dejando la grilla con la altura de una carga anterior (2 filas) mientras el nuevo dato (3 filas) ya estaba en el modelo, y como `VERTICAL_SCROLLBAR_NEVER` no deja ver el resto, la ultima fila quedaba recortada sin ningun indicio visual de que faltaba.
- Fix: `ajustarAlturaGrillaSinScrollVertical` (duplicado en `JPanelAsignacionV2` y `JPanelBandejaExpedientesNueva`) ahora, despues de fijar el nuevo `preferredSize`, invalida explicitamente toda la cadena de componentes padres desde el `scrollPane` hasta la raiz, y fuerza `window.validate()` + `window.repaint()` sobre la ventana contenedora (via `SwingUtilities.getWindowAncestor(scrollPane)`) en vez de confiar solo en `revalidate()` del scrollPane. Esto garantiza un re-layout completo y sincronico del arbol de componentes cada vez que cambia el numero de filas, sin importar cuantos contenedores/scroll anidados haya entre la grilla y la ventana.
- Esta correccion aplica a las 2 grillas de ambos paneles de Grupo Familiar (comparten el mismo helper), no solo a "Grupo familiar actual"; se eligio esta solucion "fuerza bruta" (revalidar toda la ventana) en vez de intentar identificar el contenedor exacto que no propagaba el revalidate, porque es de bajo riesgo (solo se ejecuta al recargar datos, no en un loop caliente) y robusta ante cualquier anidamiento de contenedores futuro.

### Recorte de fila persistia tras el fix anterior: se agrego setPreferredScrollableViewportSize (21/07/2026)

- El usuario reporto el MISMO sintoma con un caso distinto y mas chico (grupo de 2 personas, "ACERO MUCHA"): la etiqueta decia "2 persona(s) en el grupo familiar" pero la grilla solo mostraba 1 fila (faltaba la del expediente propio en foco, `SDRERC-EXP-2026-000001 / ACERO MUCHA MARÍA VICTORIA`), con el icono de eliminar visible (confirma que la app ya corria con el fix anterior de `revalidate`/`window.validate()` y aun asi el sintoma persistia).
- Se descarto definitivamente una causa de datos o de logica Java: `GrupoFamiliarDAO.listarIntegrantesGrupoFamiliar` no tiene ninguna exclusion por `id_expediente` (verificado); `GrupoFamiliarIntegranteDTO` y los helpers `valorUi`/`valorUiGrupoFamiliar`/`DisplayNameMapperV2.etapa`/`estado` son 100% null-safe (no pueden lanzar excepcion a mitad del for-loop que llena el modelo); el texto de estado y las filas de la grilla vienen exactamente de la misma lista `integrantes`, asi que si el texto dice "2" el modelo de la tabla necesariamente recibio 2 `addRow(...)`. La conclusion es que el problema sigue siendo 100% de layout/pintado de Swing (la ultima fila queda fuera del area visible del `JScrollPane`), no de datos.
- Endurecimiento adicional en `ajustarAlturaGrillaSinScrollVertical` (ambos archivos): ademas de invalidar la cadena de padres y forzar `window.validate()`/`repaint()`, ahora tambien llama `table.setPreferredScrollableViewportSize(new Dimension(ancho, filas * alturaFila))` — la API estandar de Swing (`Scrollable.getPreferredScrollableViewportSize()`) que un `JScrollPane` consulta directamente para decidir el tamano de su viewport, en vez de depender unicamente de `scrollPane.setPreferredSize(...)`. Tambien se cambio `table.getRowCount()` por `table.getModel().getRowCount()` al calcular `filas`, para que el calculo de altura nunca dependa del `TableRowSorter` (que podria, en teoria, reportar un conteo de vista distinto al del modelo si quedara algun filtro residual).
- Pendiente de confirmar: si el usuario esta probando con el jar reconstruido despues de este commit (y no de un build intermedio entre el commit anterior y este), ya que el commit anterior (`873e39c`) tambien debia corregir este sintoma pero aparentemente no fue suficiente por si solo.

### Causa raiz definitiva del recorte de fila: AppV2SideSectionPanel.addContent usa GridBagLayout con fill=HORIZONTAL (21/07/2026)

- El usuario confirmo que el fix anterior (`cb38eaa`) seguia sin mostrar todas las filas de una: aparecia solo 1 fila y habia que desplazar con la rueda del mouse para ver la 2da (caso `ACERO MUCHA`, grupo de 2 personas). Esto probo que el `JScrollPane` interno SI tenia contenido de sobra (scrollable), es decir, el area visible seguia siendo mas chica que el contenido real pese a fijar `preferredSize`/`setPreferredScrollableViewportSize` en el `JScrollPane` y forzar `window.validate()`.
- Causa raiz real: `AppV2SideSectionPanel.addContent(Component component)` inserta el componente (nuestro `AppV2TablePanel`, que envuelve el `JScrollPane`) en un `GridBagLayout` con `gbc.fill = GridBagConstraints.HORIZONTAL` y `weighty` por defecto (0). Con `fill=HORIZONTAL`, `GridBagLayout` puede terminar asignando a la celda una altura basada en el estado de cache de layout previo (`GridBagLayout` cachea `comptable`/`layoutInfo` por contenedor) en vez de la `preferredSize` mas reciente del `JScrollPane`, si el componente en si (el `AppV2TablePanel`, no el `JScrollPane` directamente) no refleja ese cambio de tamano hacia su propio `getPreferredSize()` de forma que el `GridBagLayout` del padre lo detecte a tiempo.
- Fix definitivo: `ajustarAlturaGrillaSinScrollVertical` cambio de firma (`JTable, JScrollPane` -> `JTable, AppV2TablePanel`) y ahora fija tamano EXACTO (no solo preferido) en AMBOS niveles — el `JScrollPane` interno Y el `AppV2TablePanel` que se pasa realmente a `addContent(...)` — usando `setMinimumSize`/`setPreferredSize`/`setMaximumSize` con la MISMA altura en los 3 (`altura = filas * rowHeight + alturaEncabezado + 4`), de modo que ningun layout manager (GridBagLayout, BoxLayout, CardLayout) tenga margen de ambiguedad entre "tamano preferido" y "tamano minimo/maximo" para decidir cuanto espacio darle. El ancho SI se mantiene flexible (minimo 80px, maximo `Integer.MAX_VALUE`) para no romper el requisito de "scroll horizontal cuando el panel es angosto" (si se hubiera fijado tambien el ancho minimo al ancho natural del contenido, el panel lateral nunca podria angostarse por debajo de eso).
- Aplicado a las 4 grillas de Grupo Familiar (candidatos + grupo actual, en Registro y Asignacion), mismo helper compartido por archivo. Compilado y empaquetado sin errores.
- Nota para uso futuro: cualquier grilla nueva que use el patron "sin scroll vertical, altura dinamica segun filas" dentro de un panel lateral (`AppV2SideActionPanel`/`AppV2SideSectionPanel`) debe fijar tamano exacto tanto en el `JScrollPane` como en el `AppV2TablePanel` que se pasa a `addContent(...)`, no solo en el `JScrollPane`; fijar solo el `JScrollPane` no fue suficiente en la practica por como `AppV2SideSectionPanel.addContent` usa `GridBagLayout` con `fill=HORIZONTAL`.

### Causa raiz final: AppV2TablePanel (CardLayout) no es compatible con el patron "altura dinamica sin scroll vertical" (21/07/2026)

- El sintoma seguia identico incluso despues de fijar tamano exacto en el `AppV2TablePanel` (commit `a096b47`): el usuario reporto que solo se veia 1 fila y habia que usar la rueda del mouse para ver la 2da, en la misma grilla "Grupo familiar actual".
- El usuario sugirio revisar un panel que SI funciona correctamente con este patron ("documentos de analisis"/documentos relacionados). Se encontro el panel de referencia real: `JPanelAsignacionV2.crearPanelDocumentosRelacionados()` + `ajustarTamanoDocumentosRelacionados()` (la grilla "Solicitudes asociadas" de la lengueta "Asociar", que SI ajusta su altura a "n" filas sin scroll vertical desde hace tiempo). Comparando ambas implementaciones:
  - La referencia que funciona usa un `JScrollPane` **crudo** (`documentosRelacionadosScroll`), sin ningun wrapper adicional; solo fija tamano en la tabla (`setPreferredScrollableViewportSize`) y en el propio `JScrollPane` (preferido + minimo, **sin maximo**), y llama `revalidate()`/`repaint()` sobre el wrapper INMEDIATO (`documentosRelacionadosWrapper`, un `JPanel` plano con `BorderLayout`), no sobre toda la ventana.
  - Nuestras grillas de Grupo Familiar, en cambio, envolvian el `JScrollPane` dentro de `AppV2TablePanel`, que usa internamente un `CardLayout` (para alternar entre la tabla y un estado "vacio" con mensaje). Ese `CardLayout` es la pieza que NO estaba presente en el panel de referencia y es la diferencia estructural real entre "funciona" y "no funciona".
  - Diagnostico de por que el `CardLayout` rompe el patron: aunque `CardLayout` implementa `LayoutManager2` (soporta invalidacion), la combinacion de "cambiar de carta" (`cardLayout.show(...)`, disparado por `setEmpty(false)` cada vez que el grupo pasa de 0 a N integrantes) junto con el cambio de tamano del `JScrollPane` en el mismo ciclo de refresco parecia dejar a la carta visible con un tamano heredado/no actualizado a tiempo, incluso forzando `window.validate()`.
- Fix definitivo: se elimino `AppV2TablePanel` de las 2 grillas de Grupo Familiar (candidatos e integrantes actuales) en Registro y Asignacion, reemplazandolo por un `JScrollPane` crudo (mismo patron exacto que `documentosRelacionadosScroll`: borde `AppV2Theme.BORDER`, scroll horizontal `AS_NEEDED`, scroll vertical `NEVER`). Se elimino tambien el mensaje de "estado vacio" propio de `AppV2TablePanel` (ya redundante: las etiquetas `lblEstadoDeteccion...`/`lblEstadoGrupo...Actual`, que siempre estuvieron arriba de cada grilla, ya comunican "no se detectaron..."/"aun no pertenece a un grupo familiar"; una tabla vacia con encabezados es una alternativa aceptable y comun).
- `ajustarAlturaGrillaSinScrollVertical` volvio a la firma `(JTable, JScrollPane)` (sin `AppV2TablePanel`): fija `setPreferredScrollableViewportSize` en la tabla, preferido+minimo (sin maximo) en el `JScrollPane`, invalida la cadena de padres, revalida el padre inmediato (como el panel de referencia) Y ademas fuerza `window.validate()` (se mantuvo como red de seguridad adicional, no hace dano).
- `AppV2ColumnFilterSupport.install(...)` ahora recibe el `JScrollPane` crudo tanto como `scrollPane` como `filterHost` (antes se pasaba el `AppV2TablePanel`); `filterHost` solo se usa para `revalidate()`/`repaint()` tras interactuar con los filtros, cualquier `JComponent` sirve.
- Leccion para uso futuro: **no usar `AppV2TablePanel` (por su `CardLayout` interno) para grillas que necesiten el patron "altura dinamica segun N filas, sin scroll vertical".** Para ese patron, replicar `crearPanelDocumentosRelacionados()`/`ajustarTamanoDocumentosRelacionados()` (`JScrollPane` crudo + `BorderLayout` simple), reservando `AppV2TablePanel` para grillas de tamano fijo/con scroll vertical visible donde el estado "vacio" con mensaje si aporta valor.

### Pivote final: "Grupo familiar actual" adopta el patron fijo de "Historial de asignacion / reasignacion" (21/07/2026)

- Tras el fix anterior (commit `aa56dc2`, JScrollPane crudo sin CardLayout) el usuario confirmo que el sintoma seguia identico: seguia viendose solo 1 fila de "Grupo familiar actual", requiriendo la rueda del mouse. El requisito original ("sin scroll vertical, mostrar siempre las N filas ajustando el bloque") resulto no ser alcanzable de forma confiable dentro de esta jerarquia de contenedores (`AppV2SideActionPanel`/`AppV2SideSectionPanel`) pese a 4 intentos distintos (revalidate simple, window.validate(), setPreferredScrollableViewportSize, JScrollPane crudo).
- El usuario pidio explicitamente replicar el diseño de un bloque que SI funciona de forma confiable en el mismo panel: "Historial de asignación / reasignación" (`crearHistorialAsignacion()`/`cargarHistorialAsignacion(...)`, ya existente desde antes de esta tarea). Ese bloque usa el patron opuesto al que se venia intentando: `AppV2TablePanel` (con su `CardLayout` para el estado vacio) dentro de un `JPanel` envoltorio con `setPreferredSize(new Dimension(320, 180))` **fijo**, sin tocar la politica de scroll vertical (queda en el default `VERTICAL_SCROLLBAR_AS_NEEDED`, es decir, SI puede aparecer una barra de scroll vertical normal si el contenido excede el alto fijo).
- Cambio aplicado (solo al bloque "Grupo familiar actual", en Registro y Asignacion; el bloque "Posibles integrantes"/candidatos NO se toco porque no fue reportado con este sintoma y mantiene el patron `JScrollPane` crudo + altura dinamica): se revirtio a `AppV2TablePanel` (con su mensaje de estado vacio) envuelto en un `JPanel(BorderLayout)` con `setPreferredSize(new Dimension(420, 180))`, replicando exactamente la estructura de `crearHistorialAsignacion()`. Se elimino la llamada a `ajustarAlturaGrillaSinScrollVertical(...)` para esta grilla especifica (ya no aplica; el tamano es fijo) y se restauro `panelTablaGrupoFamiliarActual.setEmpty(...)`/`panelTablaGrupoActual.setEmpty(...)` en los 3 puntos relevantes (carga con datos, reset por seleccion nula, `limpiarGrupoActual()`).
- Consecuencia aceptada explicitamente por el usuario (pidio replicar ESTE patron a sabiendas de que incluye scroll): si un grupo familiar tiene mas integrantes de los que caben en 180px de alto, la grilla mostrara una barra de scroll vertical normal en vez de crecer indefinidamente; esto es coherente con como ya se comporta "Historial de asignación / reasignación" en el mismo panel, no una regresion nueva.
- Confirmado por el usuario que las filas ya se ven completas. Quedo un detalle: los anchos de columna de "Grupo familiar actual" seguian usando los valores fijos de `AppV2TableColumnSizer.applyWidths(..., 34, 150, 130, 130, 150)` puestos en la construccion del panel (antes de tener datos), truncando con puntos suspensivos nombres/numeros de expediente largos. Fix: se agrego `AppV2TableColumnSizer.sizeToContent(grupoFamiliarActualTable)`/`sizeToContent(tablaGrupoActual)` justo despues de poblar el modelo en cada recarga (mismo helper ya usado por "Posibles integrantes" y por el resto de grillas del sistema), para que el ancho de columna se recalcule contra el contenido real cada vez que cambian los datos, no solo una vez al construir el panel. Con `AUTO_RESIZE_OFF` + scroll horizontal `AS_NEEDED` (heredado de `AppV2TablePanel`), si el contenido es mas ancho que los 420px fijos del bloque, aparece scroll horizontal en vez de truncar.
- Nuevo `GrupoFamiliarDAO.eliminarDeGrupoFamiliar(idExpediente, idUsuario)`: resuelve el titular del expediente, bloquea (`FOR UPDATE`) su fila de `PERSONA`, limpia `id_grupo_familiar` a `NULL`, revierte `EXPEDIENTE_SOLICITUD.grupo_familiar` a `0` (`criterio_grupo_familiar` a `NULL`, nuevo metodo `desmarcarFlagExpedienteSolicitud`, simetrico a `marcarFlagExpedienteSolicitud`) e inserta `EXPEDIENTE_HISTORIAL` reutilizando el tipo de movimiento `ASOCIACION_GRUPO_FAMILIAR` ya existente (con comentario distinto, "Persona retirada del grupo familiar.", via una sobrecarga nueva de `insertarHistorial` con comentario parametrizable) en vez de crear un tipo de movimiento nuevo, para no requerir autorizacion de un script SQL adicional en esta tarea. No se marca ninguna alerta al retirar (no aplica: la alerta `Posible Grupo Familiar` no se reactiva automaticamente).
- `GrupoFamiliarService.eliminarDeGrupoFamiliar(idExpediente)` expone el metodo a la capa UI.
- UI (ambos modulos): la grilla "Grupo familiar actual" (`grupoFamiliarActualModel`/`modeloGrupoActual`) gano una primera columna de accion (icono `AppV2RemoveActionButton`, mismo componente ya usado para "Eliminar" en la grilla de documentos relacionados de Asignacion) con su propio renderer/editor (`EliminarIntegranteGrupoFamiliarRenderer`/`Editor` en `JPanelAsignacionV2`; `EliminarIntegranteGrupoFamiliarRendererRegistro`/`EditorRegistro` en `JPanelBandejaExpedientesNueva`, anidados dentro de `JPanelRegistrarGrupoFamiliarV2`). Cada panel ahora guarda en un campo (`integrantesGrupoFamiliarActuales`/`integrantesGrupoActuales`) la ultima lista cargada, para resolver el `idExpediente` de la fila clickeada. Al confirmar, se llama al service y se refresca con el mismo patron de reseleccion ya usado para "Asociar" (`buscar(idPrincipal)`/`refrescar(idPrincipal)`), para que el panel no se cierre y la grilla se actualice con el grupo ya sin esa persona.

### "Posibles integrantes" no detectaba candidatos ya agrupados con OTRO grupo distinto al del ancla (21/07/2026)

- Caso reportado: se registra manualmente una solicitud nueva (SDRERC-EXP-2026-000183, ACERO MUCHA JOSE M...) con el mismo apellido que 2 solicitudes ya asociadas entre si en un grupo familiar confirmado (000005 y 000001). Al seleccionar la solicitud nueva (sin grupo familiar propio todavia) y abrir el panel "Grupo Familiar", "Posibles integrantes" mostraba "No se detectaron posibles integrantes por apellidos" en vez de listar a esos 2 integrantes ya agrupados como candidatos para unirse a ESE grupo existente.
- Causa: el fix anterior de esta misma sesion en `GrupoFamiliarDAO.listarPosiblesIntegrantes` (ver entrada "Panel de Grupo Familiar... se cerraba al asociar...") excluia CUALQUIER candidato con `id_grupo_familiar` no nulo, sin importar si ese grupo coincidia o no con el del expediente ancla. Eso era correcto para el caso que motivo el fix (ancla ya asociada a un grupo, no repetir a sus propios compañeros de grupo como "posible integrante"), pero incorrecto para este caso: el ancla (000183) NO tiene grupo todavia, asi que los candidatos que YA pertenecen a un grupo (distinto, ya que el ancla no tiene ninguno) son precisamente los que el usuario necesita ver para poder unirse a ese grupo existente.
- Fix: se agrego `idGrupoFamiliarAncla` (resuelto una sola vez al inicio del metodo, misma logica que ya usa `listarIntegrantesGrupoFamiliar`) y la exclusion ahora compara grupos: un candidato solo se excluye si `idGrupoFamiliarCandidato != null && idGrupoFamiliarCandidato.equals(idGrupoFamiliarAncla)` (ya esta confirmado junto al ancla en el mismo grupo). Si el candidato pertenece a un grupo DISTINTO o el ancla todavia no tiene grupo, si se lista como "posible integrante"; `asociarGrupoFamiliar(...)` ya soporta este caso sin cambios (si entre las personas involucradas hay exactamente un grupo existente, el ancla se une a ese grupo en vez de crear uno nuevo).

### "Posibles integrantes" replica el patron fijo de "Historial de asignación / reasignación" (21/07/2026)

- Mismo pedido explicito del usuario que origino la entrada anterior sobre "Grupo familiar actual", ahora extendido al bloque "Coincidencias por apellidos del titular" ("Posibles integrantes") de ambos modulos, por consistencia visual/de comportamiento entre las 2 grillas del mismo panel.
- Cambio aplicado (Registro y Asignacion): se reintrodujo `AppV2TablePanel` (con su mensaje de estado vacio "Sin coincidencias detectadas") para `integrantesGrupoFamiliarTable`/`tablaIntegrantes`, envuelto en el `JPanel(BorderLayout)` existente (`contentDeteccion`, que ya tenia el encabezado/ayuda en `NORTH`) con `setPreferredSize(new Dimension(420, 260))` fijo (mas alto que el de "Grupo familiar actual" porque este bloque tambien incluye el texto de ayuda de 2 lineas en el mismo contenedor). Se elimino la llamada a `ajustarAlturaGrillaSinScrollVertical(...)` para esta grilla (ya no aplica) y se restauro `panelTablaIntegrantesGrupoFamiliar.setEmpty(...)`/`panelTablaIntegrantes.setEmpty(...)` en los mismos 3 puntos que "Grupo familiar actual" (carga con datos, reset por seleccion nula), agregando ademas `AppV2TableColumnSizer.sizeToContent(...)` tras poblar el modelo para que el ancho de columnas siga ajustandose al contenido real (mismo fix ya aplicado a "Grupo familiar actual").
- Con esto, el helper `ajustarAlturaGrillaSinScrollVertical` quedo sin ningun uso en ninguna de las 2 grillas de Grupo Familiar (en ninguno de los 2 archivos) y se elimino por completo de `JPanelAsignacionV2.java` y `JPanelBandejaExpedientesNueva.java` (junto con el import `java.awt.Container`, que solo se usaba ahi); patron descartado para este panel a favor del patron fijo de "Historial de asignación / reasignación".

### Buscador manual con autocompletado para agregar posibles integrantes fuera de la deteccion por apellidos (21/07/2026)

- Pedido del usuario: agregar un boton "+" y un buscador dentro del bloque "Posibles integrantes" (Registro y Asignacion) para agregar manualmente una solicitud como posible integrante del grupo familiar aunque no tenga alerta ni coincida por apellidos (ej. cambio de apellido por matrimonio, error de tipeo en el apellido, etc.). Busca por titular, N° de expediente o N° de expediente SGD, sin importar etapa/estado, solo si `Grupo Familiar Confirmado = No` y excluyendo a quienes ya tienen la alerta `Posible Grupo Familiar` activa (esos ya se detectan por la via automatica). La UX pedida es "tipo buscador con efecto jQuery": autocompletado que muestra opciones mientras se escribe y al seleccionar una la agrega automaticamente a la grilla de "Coincidencias por apellidos del titular".
- Nuevo componente reutilizable `com.sdrerc.ui.appv2.components.AppV2SearchAutocompleteSupport`: adjunta a cualquier `JTextField` un comportamiento de autocompletado (debounce de 300ms via `javax.swing.Timer`, busqueda asincrona via `SwingWorker`, resultados en un popup flotante `JWindow` con un `JList` posicionado debajo del campo). API generica: `attach(campo, minCaracteres, Buscador<T>, Function<T,String> textoOpcion, Consumer<T> alSeleccionar)`. Soporta seleccion con mouse o teclado (flecha abajo entra a la lista, Enter selecciona, Escape cierra el popup). No depende de ninguna libreria externa (no hay equivalente a jQuery en Swing; este es el patron nativo mas parecido: busqueda incremental con resultados en vivo y seleccion unica).
- Nuevo `GrupoFamiliarDAO.buscarPosiblesIntegrantesManual(idExpedientePrincipal, texto)`: mismo patron de JOIN que `listarPosiblesIntegrantes` (expediente + expediente_persona + persona + etapa/estado), pero sin el filtro de heuristica de apellidos. Filtra por `UPPER(numero_expediente) LIKE`, `UPPER(titular) LIKE` o `UPPER(numero_expediente_sgd) LIKE` (comparando contra la MISMA fila mas reciente de `expediente_solicitud`, mismo patron `SELECT MAX(id_expediente_solicitud)... WHERE activo=1` ya usado en el resto del DAO). Excluye candidatos con `NVL(expediente_solicitud.grupo_familiar, 0) = 1` (ya confirmados) y candidatos con alerta `Posible Grupo Familiar` activa (`NOT EXISTS` contra `expediente_alerta`). Limite de 20 resultados (`ROWNUM <= 20`) para no saturar el popup de autocompletado. Expuesto via `GrupoFamiliarService.buscarPosiblesIntegrantesManual(...)`.
- UI (ambos modulos, mismo patron duplicado que el resto del panel): boton "+" (`btnAgregarPosibleIntegranteManual`) al lado del titulo "Coincidencias por apellidos del titular" que muestra/oculta una fila de busqueda (`AppV2SearchField` dentro de `panelBusquedaPosibleIntegranteManual`, oculta por defecto); el campo tiene el autocompletado adjunto via `AppV2SearchAutocompleteSupport.attach(...)`, buscando contra el expediente actualmente en foco (`expedienteFocoGrupoFamiliar`). Al seleccionar una opcion, `agregarPosibleIntegranteManual(candidato)` la agrega a la lista en memoria (`candidatosGrupoFamiliarActuales`/`candidatosActuales`) y a la grilla (checkbox marcado por defecto, igual que los detectados automaticamente), evitando duplicados por `idExpediente` (avisa con un mensaje si ya esta en la lista) y recalculando ancho de columnas (`sizeToContent`) y el contador de "posible(s) integrante(s) detectado(s)". La fila de busqueda y su contenido se limpian/ocultan automaticamente cada vez que cambia el expediente seleccionado en la bandeja.
- El resultado agregado manualmente se asocia exactamente igual que uno detectado por apellidos: al marcar y confirmar "Asociar al grupo familiar" pasa por el mismo `GrupoFamiliarDAO.asociarGrupoFamiliar(...)` sin cambios (no hay diferencia de origen "manual" vs "automatico" a nivel de datos, solo en como llego a la grilla).
- El boton "+" original (texto plano) se reemplazo por un icono premium de lupa (`AppV2SearchActionButton`, nuevo, mismo patron de pintado a mano que `AppV2ReceiveActionButton`/`AppV2RemoveActionButton`): cuadrado redondeado con fondo solido `AppV2Theme.PRIMARY` (azul institucional, mas oscuro en hover) y una lupa blanca dibujada con `Graphics2D`, en vez de un `JButton` de fondo blanco por defecto.
- Columna nueva "Tipo Acta" (ultima) en la grilla "Grupo familiar actual" (Registro y Asignacion): `GrupoFamiliarIntegranteDTO` gano el campo `tipoActa`; `GrupoFamiliarDAO.listarIntegrantesGrupoFamiliar` agrega `LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1` + `LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta` (mismo patron de join ya usado para la columna "Tipo Acta" del resto de bandejas, ej. `AsignacionExpedienteDAO`), seleccionando `ta.nombre AS tipo_acta`. No afecta a "Posibles integrantes" (esa grilla no tiene esta columna, no se pidio).

### Boton para eliminar la alerta "Posible Grupo Familiar" desde el bloque Asociacion (21/07/2026)

- Pedido del usuario: en el bloque "Asociación" del panel Grupo Familiar (Registro y Asignacion), agregar un boton azul con icono de tacho al costado derecho de "N° expediente / Nombres titular" para eliminar la alerta `Posible Grupo Familiar` de ese expediente, visible SOLO cuando el expediente no tiene grupo familiar confirmado (`Grupo Familiar Confirmado = No`) Y tiene la alerta activa (si no cumple ambas condiciones, el boton no se muestra). Ademas, debajo de "Expediente" agregar una fila "Alerta" mostrando el mensaje de alerta con el mismo diseño (color de fondo/letra) que la columna "Alertas" del listado de expedientes.
- Se reutilizo el diseño EXACTO de esa columna: en `JPanelAsignacionV2.AsignacionRenderer` (renderer de la grilla principal), la columna de alertas usa `new BadgeV2(text, AppV2Theme.SOFT_ORANGE, AppV2Theme.WARNING)` cuando el texto no empieza con "Sin" (fondo naranja suave, texto en color de advertencia). Para "Alerta" en el panel Grupo Familiar no se uso `BadgeV2` como componente aparte (para no tener que reemplazar el componente en el `GridBagLayout` de `AppV2SideSectionPanel.addRow`, que ya tiene una referencia fija); en su lugar se restylea el MISMO `JLabel` (`lblAlertaGrupoFamiliar`) en cada carga: cuando hay alerta activa, `setOpaque(true)` + `setBackground(AppV2Theme.SOFT_ORANGE)` + `setForeground(AppV2Theme.WARNING)` + fuente en negrita + el mismo padding de `BadgeV2` (`5,9,5,9`); cuando no hay alerta, vuelve a texto plano "-" sin fondo.
- Nuevo `GrupoFamiliarEstadoAlertaDTO` (`grupoFamiliarConfirmado` + `mensajeAlertaPosibleGrupoFamiliar`, con `tieneAlertaPosibleGrupoFamiliar()` de conveniencia) y `GrupoFamiliarDAO.obtenerEstadoAlerta(idExpediente)`: una sola consulta (`SELECT ... FROM dual` con 2 subconsultas escalares) que resuelve `NVL(expediente_solicitud.grupo_familiar, 0)` (mas reciente, `activo=1`) y el `mensaje` de la alerta `Posible Grupo Familiar` si sigue activa y no atendida. `GrupoFamiliarDAO.eliminarAlertaPosibleGrupoFamiliar(idExpediente, idUsuario)` reutiliza `ExpedienteAlertaDAO.marcarAtendidas(...)` (mismo mecanismo ya usado al asociar un grupo familiar, que tambien marca esta alerta como atendida).
- Nuevo icono `AppV2TrashActionButton` (mismo patron de pintado a mano que `AppV2SearchActionButton`/`AppV2ReceiveActionButton`): cuadrado redondeado con fondo solido `AppV2Theme.PRIMARY` (azul, no rojo, para diferenciarlo visualmente de `AppV2RemoveActionButton` que SI es rojo y se usa para eliminaciones destructivas de datos) y un tacho de basura dibujado con `Graphics2D` (tapa + cuerpo + una ranura vertical).
- UI (ambos modulos): el boton se agrega envolviendo `lblExpedienteFocoGrupoFamiliar` en un `JPanel(BorderLayout)` (label en CENTER, boton en EAST) que se pasa como el componente de la fila "Expediente"; al estar oculto (`setVisible(false)`), `BorderLayout` no le reserva espacio (no hace falta quitarlo del layout). Cada vez que se selecciona un expediente, `cargarEstadoAlertaGrupoFamiliar(idExpediente)` (nuevo, async) consulta el estado y `actualizarAlertaGrupoFamiliar(estado)` decide la visibilidad del boton y el estilo de la etiqueta "Alerta". Al confirmar la eliminacion, se llama al service y se refresca con el mismo patron de reseleccion ya usado para "Asociar"/"Eliminar integrante" (`buscar(idExpediente)`/`refrescar(idExpediente)`), de modo que el boton y la etiqueta reflejen el nuevo estado (alerta ya eliminada) sin cerrar el panel.

### Paridad visual de lenguetas laterales de Registro con Asignacion (21/07/2026)

- Pedido del usuario: las lenguetas de Registro/Recepcion (`Datos`/`Registrar G.F`/`Asociar`) deben verse y comportarse EXACTAMENTE igual que las de Asignacion (`Datos`/`Asignacion`/`Asociar`/`Grupo Fam.`) en diseño, colores y orden, no solo en funcionalidad.
- Auditoria contra Asignacion revelo 2 diferencias visuales reales (la funcionalidad de "Asociar duplicados" y "Grupo Familiar" ya era equivalente desde tareas anteriores de esta sesion):
  1. La lengueta de Grupo Familiar en Registro decia "Registrar G.F" y usaba una paleta NARANJA casi identica a la de "Asociar" (`new Color(248,240,225)/(201,129,42)/(156,96,22)`), mientras que en Asignacion la lengueta "Grupo Fam." usa una paleta VERDE (`new Color(224,245,232)/(35,138,94)/verde.darker()`) claramente diferenciada de "Asociar" (naranja).
  2. El orden vertical de las lenguetas en Registro era Datos, Registrar G.F, Asociar; en Asignacion (y segun lo pedido) debe ser Datos, Asociar, Grupo Fam.
  3. El panel "Grupo Familiar" de Registro (`JPanelRegistrarGrupoFamiliarV2`) usaba `setAccentColor(AppV2Theme.TEAL)` (un teal azulado, `(31,137,141)`) en vez del mismo verde `new Color(35,138,94)` que usa el panel equivalente de Asignacion — el borde/acento del panel no coincidia con el verde de su propia lengueta.
- Fix: `crearTabRegistrarGF()` ahora usa el texto "Grupo Fam." y la MISMA paleta verde que Asignacion (`accent.darker()` para el tercer color, igual formula que `crearTabAsignacion` en `JPanelAsignacionV2`); se intercambiaron las coordenadas Y de `tabPanelAsociarDuplicados` y `tabPanelRegistrarGF` en `crearPanelRecepcionConTab().doLayout()` (y el orden de `wrapper.add(...)`) para que el orden visual quede Datos, Asociar, Grupo Fam.; `JPanelRegistrarGrupoFamiliarV2` cambio su accent color a `new Color(35, 138, 94)` para coincidir con su propia lengueta y con el panel de Asignacion. Los identificadores internos (`tabPanelRegistrarGF`, `PANEL_RECEPCION_CARD_GF`, etc.) NO se renombraron, solo el texto visible y los colores, para minimizar el riesgo de romper referencias existentes.
- Se verifico que el panel "Asociar duplicados" de Registro (secciones "Selección y alertas"/"Asociación rápida"/"Decisión de número", accent naranja `(198,121,31)`) ya coincidia exactamente con el panel "Panel de Asociación" de Asignacion (mismas secciones, mismo accent); no se le hicieron cambios. El titulo interno del panel se dejo como "Asociar duplicados" (no "Panel de Asociación") porque esa es la terminologia ya establecida y documentada extensamente en este archivo para ese panel especifico; el pedido del usuario se referia a la lengueta, no al titulo interno.
- **Correccion sobre la entrada anterior**: el usuario adjunto capturas comparando ambos paneles lado a lado y demostro que la afirmacion de arriba ("ya coincidia exactamente") era incorrecta — solo se habia verificado que los TITULOS de sección coincidian ("Selección y alertas"/"Asociación rápida"/"Decisión de número"), no el CONTENIDO real dentro de cada seccion. Ver entrada siguiente con la comparacion real y el fix aplicado.

### Reescritura del panel "Asociar" de Registro para igualar al de Asignacion (21/07/2026)

- El usuario adjunto 2 capturas (`screen-panel-asociar-asignacion.png` / `screen-panel-asociar-registrorecepcion.png`) mostrando diferencias reales de contenido, no solo de color:
  1. Titulo del panel: "Panel de Asociación" (Asignacion) vs "Asociar duplicados" (Registro).
  2. Sección "Selección y alertas": en Asignacion tiene 4 filas (`Seleccionados`, `Recepción`, `Grupo familiar`, `Alertas`) que resumen el estado del expediente/seleccion ANTES de la lista de candidatos; en Registro solo tenia 1 fila (`Estado`) con un mensaje generico de deteccion.
  3. La grilla de candidatos ("Solicitudes asociadas" en Asignacion / "Coincidencias por N° de acta y titular" en Registro): en Asignacion el bloque completo (titulo + tabla) se OCULTA por completo cuando no hay candidatos (`mostrarSolicitudesAsociadas(!relacionados.isEmpty())`), sin caja vacia visible; en Registro usaba `AppV2TablePanel` con un estado vacio SIEMPRE visible ("Sin duplicados detectados" en una caja con borde fijo de 320x220).
  4. Texto del boton de accion: en Asignacion es dinamico segun el estado (`"Asociar al principal (N)"` / `"Sin relacionados pendientes"` / etc., ver `btnAsociarRelacionados`); en Registro era un texto estatico fijo "Asociar todo" que no reflejaba el conteo ni el estado.
- El usuario autorizo explicitamente eliminar y reconstruir el panel desde cero si era mas simple. Se opto por una reescritura dirigida (no un borrado completo) de `JPanelAsociarDuplicadosRecepcionV2`, preservando toda la logica de negocio ya correcta y probada (`resolverPrincipal`, `asociarSeleccionados`, `asociarRapido`, `generarNumeroExpediente`, `ejecutarAsociacion`, el flujo dual "Asociar seleccionados" (checkboxes) + "Asociación rápida" (un clic) que fue una decision explicita de una tarea anterior y no debia perderse), cambiando solo la CAPA VISUAL para igualar a Asignacion:
  1. `super("Asociar duplicados")` -> `super("Panel de Asociación")`.
  2. "Selección y alertas" ahora tiene las mismas 4 filas que Asignacion, adaptadas al modelo de Registro (que es de foco unico, no de seleccion multiple por checkboxes en la grilla principal como Asignacion): `Seleccionados` muestra "1 expediente(s) seleccionados" cuando hay un expediente en foco (Registro no tiene seleccion multiple para este flujo) o "0" si no hay ninguno; `Recepción` muestra `expedientePrincipal.getResponsableActual()` o "Sin abogado asignado" si esta vacio (con el mismo color condicional que `aplicarEstadoRecepcion` de Asignacion: verde si "Recibido por abogado", ambar si "Sin abogado asignado"); `Grupo familiar` muestra `expedientePrincipal.getGrupoFamiliar()` (Sí/No); `Alertas` reutiliza el campo `lblEstadoAsociar` ya existente (el texto de deteccion de duplicados), solo se le cambio la etiqueta de fila de "Estado" a "Alertas".
  3. La grilla de candidatos se reconstruyo replicando exactamente la tecnica de `crearPanelDocumentosRelacionados()`/`ajustarTamanoDocumentosRelacionados()` de Asignacion: se elimino el `AppV2TablePanel` (con su `CardLayout` y estado vacio) y se reemplazo por un `JScrollPane` crudo (`duplicadosScroll`) dentro de un `JPanel(BorderLayout)` con el scroll en `WEST` (`duplicadosWrapper`), dimensionado dinamicamente por `ajustarTamanoDuplicados()` (mismo algoritmo que `ajustarTamanoDocumentosRelacionados`: ancho = suma de anchos de columna, alto = filas reales + encabezado + barra horizontal). El bloque completo (titulo "Solicitudes asociadas" + ayuda + tabla) se envuelve en `contentCoincidenciasDuplicados` y se oculta/muestra como una unidad con `mostrarCoincidenciasDuplicados(boolean)`, llamado con `!duplicadosActuales.isEmpty()` cada vez que se recargan los duplicados — igual que `mostrarSolicitudesAsociadas` en Asignacion. Ya no hay caja vacia visible cuando no hay candidatos.
  4. `actualizarEstadoBoton()` ahora tambien actualiza el texto de `btnAsociarRapido` dinamicamente: `"Asociar todo (N)"` cuando hay candidatos, `"Sin relacionados pendientes"` cuando no hay (mismo mensaje literal que usa Asignacion para el caso vacio).
- Deliberadamente NO se importo la arquitectura de seleccion multiple de Asignacion (checkboxes en la grilla principal que alimentan un resumen "Seleccionados: N" real y un modo "múltiple" completo en `actualizarPanelSeleccion`): Registro no tiene ese mecanismo para este flujo y agregarlo hubiera sido una funcionalidad nueva mucho mas grande que lo pedido (paridad visual/de contenido), con alto riesgo de romper el resto del archivo (que ya es muy extenso). El campo "Seleccionados" en Registro por eso siempre vale 1 o 0, reflejando el foco unico real de este panel, no una cuenta de multiples marcados.

### Despliegue cliente-servidor

- El modo vigente de actualizacion cliente-servidor es LAN por `FILE_SHARE`/UNC dentro de la misma red. El cliente no debe ejecutar el JAR desde la carpeta compartida; debe copiar/actualizar localmente y ejecutar desde `C:\SDRERC_CLIENTE`.
- La carpeta servidor vigente es `D:\SDRERC_RELEASES\latest`, con `version.json`, `SDRERC-V2.zip` y `checksums.txt`.
- El launcher cliente vive bajo `C:\SDRERC_CLIENTE\launcher` y debe usar `updater-config.json` apuntando a `\\SERVIDOR\SDRERC_RELEASES\latest` o recurso equivalente.
- El modo HTTP/VPN queda como capacidad experimental documentada, no como configuracion estandar vigente, salvo autorizacion futura. No exponer Oracle ni releases a internet publico.
- Para publicar una version LAN se usa `.\scripts\server\publish-sdrerc-release.ps1 -Version "x.y.z"` desde el proyecto, generando y copiando el paquete al release latest.

## 5. Reglas de SQL y BD

- No ejecutar SQL sin autorizacion explicita.
- No modificar scripts SQL sin autorizacion explicita.
- No modificar datos de BD sin autorizacion explicita.
- Scripts de prueba o patch solo deben crearse/modificarse si la tarea lo pide.
- No reejecutar scripts base sobre una BD existente salvo autorizacion explicita.
- No usar `DROP`, `DELETE`, `TRUNCATE`, `INSERT`, `UPDATE` o `MERGE` en scripts o codigo salvo autorizacion explicita y alcance claro.
- Si la tarea autoriza crear un script SQL, hacerlo idempotente cuando sea posible y no ejecutarlo salvo instruccion separada.

## 6. Seguridad y credenciales

- No imprimir credenciales en respuestas finales.
- No documentar passwords reales.
- Si aparece un password real en documentacion editable dentro del alcance, reemplazarlo por `[REDACTADO]`.
- Si aparece un password real en codigo legacy que no fue autorizado tocar, no modificarlo; reportarlo brevemente.
- No pedir, exponer ni inferir credenciales si no son necesarias para la tarea.

## 7. Reglas de git

- Al terminar cada tarea, ejecutar `git status`.
- No usar `git add .`.
- Agregar solo archivos creados/modificados por la tarea.
- Si hay cambios ajenos o preexistentes, reportarlos brevemente y excluirlos del commit.
- Si el build o verificacion falla, no hacer commit ni push, salvo que el usuario lo autorice explicitamente.
- Si el build o verificacion pasa, hacer commit obligatorio con un mensaje claro y breve.
- Despues del commit, hacer push obligatorio al branch actual.
- Cada incremento funcional o documental que se complete debe cerrar con `git add` selectivo, commit con nombre coherente y push al branch actual; no considerar una tarea cerrada si queda solo en el working tree.
- Si el push falla por credenciales, red o permisos:
  - reportar la causa exacta;
  - indicar el commit creado;
  - indicar el branch actual;
  - indicar el comando manual para ejecutar el push.
- No pedir el mensaje de commit al inicio.
- Generar un mensaje de commit coherente segun la tarea.
- No incluir archivos no relacionados.
- No revertir cambios ajenos.
- No mostrar `git diff` completo salvo que se pida explicitamente.
- Si hay conflictos o riesgo de mezclar cambios, detenerse y reportar.
- AGENTS.md autoriza por defecto `git add` selectivo, commit y push controlado al finalizar tareas normales.
- No hacer commit/push si el build falla, hay conflictos, hay cambios ajenos riesgosos, hay comandos destructivos pendientes, hay SQL o BD involucrada sin autorizacion, hay credenciales o datos sensibles en riesgo, o el usuario pide explicitamente no hacer commit/push.
- Si la tarea indica explicitamente no hacer commit, obedecer.
- Solo pedir confirmacion si hay conflictos, cambios ajenos riesgosos, comandos destructivos, restricciones del entorno o riesgo de incluir archivos no relacionados.

## 8. Autonomia / full access

- Asumir aprobacion para comandos normales de lectura, edicion, compilacion, pruebas y git cuando el entorno lo permita.
- Pedir confirmacion solo ante operaciones destructivas, SQL, datos sensibles, credenciales, conflictos de git o cambios fuera de alcance.
- Para evitar preguntas de aprobacion en Codex CLI, se recomienda usar modo autonomo con:

```powershell
codex --approval never --sandbox workspace-write
```

- Para maxima autonomia en entornos controlados y de confianza, se puede usar:

```powershell
codex --approval never --sandbox danger-full-access
```

- `danger-full-access` reduce barreras de seguridad. Usarlo solo cuando el repo, la tarea y el entorno sean confiables.
- Aunque el entorno tenga full access, respetar las restricciones del proyecto, especialmente legacy, SQL, credenciales y cambios de BD.
- Aun con full access, respetar por defecto: no SQL, no legacy, no datos de BD y no passwords salvo autorizacion explicita.

## 9. Ahorro de tokens

- No pegar salidas largas de consola.
- No devolver `git diff` completo salvo pedido explicito.
- No pegar bloques largos de codigo en la respuesta final.
- Resumir cambios por archivo.
- Resumir errores solo con lo necesario para corregirlos.
- Entregar respuestas finales cortas y accionables.
- Evitar repetir codigo completo si solo se modificaron fragmentos.
- Evitar mostrar contenido completo de archivos creados/modificados salvo que se pida.
- Preferir referencias a archivos y resumen de impacto antes que bloques largos de texto.

## 10. Alcance y forma de trabajo

- Mantener cambios pequenos, incrementales y compilables.
- No hacer refactors amplios no solicitados.
- Antes de modificar, leer archivos relevantes.
- Si la tarea es ambigua, proponer o aplicar el alcance minimo seguro.
- Corregir solo errores atribuibles a la tarea actual.
- No mezclar mejoras oportunistas con la tarea solicitada.
- Cuando una mejora dependa de una configuracion persistente o de reglas de negocio documentadas, revisar y mantener alineado `AGENTS.md` antes de cerrar la tarea.

## 11. Compilacion y verificacion

- Ejecutar `mvn clean compile` cuando se modifique Java.
- Ejecutar `mvn clean package` si se modifica App V2, launcher o estructura de ejecucion.
- Si Maven/JDK no esta disponible, reportar el bloqueo y hacer revision estatica.
- Corregir solo errores atribuibles a la tarea actual.
- Para cambios solo Markdown o documentacion, no compilar salvo que la tarea lo pida.

## 12. Flujo estandar de trabajo

1. Leer el contexto minimo necesario.
2. Hacer un plan breve solo si la tarea es amplia.
3. Implementar el cambio solicitado.
4. Compilar/verificar segun aplique.
5. Revisar restricciones de la tarea.
6. Revisar `git status`.
7. Hacer `git add` selectivo solo de archivos de la tarea.
8. Crear commit obligatorio si la verificacion paso y no hay bloqueos.
9. Hacer push obligatorio al branch actual.
10. Si el push falla, reportar causa exacta, commit, branch y comando manual.
11. Entregar resumen final.

## 13. Respuesta final esperada

- Responder corto y accionable.
- Indicar archivos creados/modificados.
- Resumir brevemente la implementacion.
- Indicar resultado de build o verificacion.
- Indicar restricciones cumplidas.
- Indicar cambios ajenos detectados si los hubo.
- Indicar commit creado.
- Confirmar push realizado.
- Si el push fallo, indicar causa exacta y comando manual para ejecutar el push.
- Indicar bloqueos concretos si existieron.

## 14. Restricciones por defecto

- No ejecutar SQL sin autorizacion explicita.
- No modificar scripts SQL sin autorizacion explicita.
- No modificar datos de BD sin autorizacion explicita.
- No introducir escrituras en SDRERC V2 salvo que la tarea lo pida.
- No documentar passwords reales.
- No tocar legacy sin autorizacion explicita.

## 15. Vision del sistema SDRERC V2

SDRERC V2 busca ser:

- Una consola moderna de gestion de expedientes registrales.
- Una aplicacion de lectura primero y escritura controlada despues.
- Una interfaz basada en etapas, estados, responsables y acciones permitidas.
- Una herramienta para reducir confusion del usuario final.
- Una app con trazabilidad, historial, documentos, observaciones, notificaciones, publicacion, expediente digital y cierre.

## 16. Modelo mental de usuario

La interfaz debe ayudar al usuario a responder rapidamente:

- Donde esta el expediente.
- Quien lo tiene.
- Que etapa tiene.
- Que estado tiene.
- Que accion sigue.
- Si esta observado.
- Si esta vencido.
- Si tiene documentos pendientes.
- Si requiere notificacion.
- Si requiere publicacion.
- Si tiene expediente digital.
- Cual fue su historial.

## 17. Principios UX/UI obligatorios

- No mostrar codigos tecnicos al usuario si existe nombre amigable.
- No mostrar `ASIGNACION` si puede mostrarse `Asignacion`.
- No mostrar `EN_VERIFICACION` si puede mostrarse `En verificacion`.
- Evitar controles diminutos.
- Usar combos para catalogos cuando sea posible.
- Usar badges para etapa, estado y plazo.
- Usar panel lateral con cards.
- Usar barra visual de etapas.
- Evitar interfaces planas tipo tabla/formulario cuando el flujo requiera contexto.
- Mantener estilo institucional sobrio.
- No copiar logos, colores ni branding de referencias externas.

## 18. Flujo funcional esperado

El flujo visual y funcional base de SDRERC V2 debe respetar estas macroetapas:

- Registro.
- Asignacion.
- Analisis.
- Verificacion.
- Firma / Emision.
- Ejecucion.
- Notificacion.
- Publicacion condicional.
- Expediente digital.

Rutas especiales que deben considerarse en el diseno y en futuros incrementos autorizados:

- Devolucion desde Verificacion a Analisis.
- Correccion y reenvio.
- Reversion desde Ejecucion a Analisis.
- Notificacion virtual.
- Notificacion presencial.
- Cargos de acuse.
- Publicacion.
- Expediente digital.

## 19. Arquitectura visual de la consola

La Consola Expediente V2 debe tender a una arquitectura tipo Service Console / Case Management con:

- Header ejecutivo del expediente.
- Datos clave visibles.
- Barra visual de etapas.
- Pestanas de detalle.
- Seccion de documentos.
- Timeline / historial.
- Panel lateral de resumen.
- Acciones permitidas informativas hasta que exista autorizacion explicita de escritura.
- Diseno orientado a contexto, trazabilidad y decision rapida.

## 20. Criterios de aceptacion visual

- No debe haber campos superpuestos.
- No debe haber controles diminutos.
- Etapas y estados deben verse con nombres amigables.
- La barra de etapas debe ser legible.
- El panel lateral debe tener cards separadas y claras.
- La bandeja debe tener filtros entendibles.
- Los combos deben usarse para etapa y estado cuando corresponda.
- La consola debe mostrar contexto suficiente sin saturar.
- La UI debe ser sobria, institucional y moderna.

## 21. Criterios de aceptacion tecnica

- Build exitoso con `mvn clean compile` cuando se modifique Java.
- Ejecutar `mvn clean package` cuando se modifique App V2, launcher o estructura de ejecucion.
- App legacy intacta.
- Sin SQL ejecutado salvo autorizacion explicita.
- Sin scripts SQL modificados salvo autorizacion explicita.
- Sin escrituras no autorizadas.
- Sin cambios en `OracleConnection.java`.
- Sin cambios en `FrmLogin.java` legacy.
- Sin cambios en `MenuPrincipal.java` legacy.
- Sin cambios en `com.sdrerc.Main` legacy.
- Sin passwords reales documentados.
- Sin IDs hardcodeados nuevos.

## 22. Nombres amigables de etapas/estados

Mapeo visual obligatorio de macroetapas:

- `REGISTRO` -> `Registro`
- `ASIGNACION` -> `Asignacion`
- `ANALISIS` -> `Analisis`
- `VERIFICACION` -> `Verificacion`
- `FIRMA_EMISION` -> `Firma / Emision`
- `EJECUCION` -> `Ejecucion`
- `NOTIFICACION` -> `Notificacion`
- `PUBLICACION_CONDICIONAL` -> `Publicacion`
- `EXPEDIENTE_DIGITAL` -> `Expediente digital`

Reglas:

- No crear etapa visual `VALIDACION`.
- Las validaciones son acciones, observaciones, evaluaciones o reglas dentro de una etapa.
- Mantener codigos tecnicos internamente.
- Mostrar nombres amigables al usuario final.

## 23. Reglas para prompts incrementales

- Cada prompt debe indicar alcance exacto.
- Cada incremento debe ser pequeno, compilable y verificable.
- No mezclar UI con escritura salvo autorizacion explicita.
- No mezclar BD con Java salvo autorizacion explicita.
- Si el cambio es visual, no tocar SQL.
- Si el cambio es de BD, no tocar Java salvo autorizacion explicita.
- Si hay cambios ajenos en `git status`, reportarlos y no incluirlos.
- Si una mejora visual genera superposicion, corregir layout antes de agregar nuevas funcionalidades.
- Siempre indicar archivos creados/modificados, build y restricciones cumplidas.
