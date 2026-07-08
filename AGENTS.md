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
- En Registro / Recepcion, la carga diaria y el registro manual deben detectar duplicidad solo por la combinacion `numero de acta + titular`. Los registros duplicados se guardan para trazabilidad, quedan marcados como potencial duplicado y no deben generar un nuevo numero de expediente hasta que Asignacion confirme/asocie el caso.
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
- `Asignacion`: en asignacion simple o multiple, la hoja de envio se captura por expediente y se persiste en `EXPEDIENTE_ASIGNACION.NUMERO_HOJA_ENVIO`; el numero debe ser unico antes de confirmar la asignacion.
- `Asignacion`: el panel de asignacion puede mostrar documentos relacionados pendientes en una tabla compacta con `N° documento` y accion de recepcion/asociacion; esa accion debe reutilizar el Service/DAO transaccional de relacion, registrar evidencia en historial y no poblar `EXPEDIENTE_DOCUMENTO_ANALIZADO`.
- `Analisis`: recibir expedientes `ASIGNACION / ASIGNADO`, registrar evaluacion, observaciones y documentos analizados, enviar a verificacion y archivar no corresponde si el flujo `SDRERC_TO_BE` lo permite; no exponer acciones directas de derivacion a notificacion ni derivacion externa desde el panel de Analisis.
- `Analisis`: `N° Documento (Proveido)` debe permanecer habilitado para cualquier resultado y registrarse como `EXPEDIENTE_DOCUMENTO` cuando tenga valor. Para `NO_CORRESPONDE` es obligatorio; ademas se bloquean `Acta incorporada`, evaluaciones, documentos analizados, observacion y comentario de movimiento, y no se exigen ni registran documentos analizados.
- `Analisis`: el combo `Tipo` de documentos analizados debe cargar solo tipos activos con codigo `ANALISIS_%` desde `TIPO_DOCUMENTO_ADJUNTO`, respetando el orden del codigo; `PROVEIDO` es un tipo tecnico para documentos del expediente y no debe mostrarse en ese combo.
- `Analisis`: la grilla de documentos analizados debe separar la gestion documental del resultado final. Guardar documentos, cartas intermedias, oficios, cargos, respuestas, subsanaciones o anexos no debe exigir resultado/fundamento final ni mover el expediente a Verificacion. El resultado final y el envio a Verificacion siguen siendo acciones independientes.
- `Analisis`: los documentos analizados pueden organizarse en jerarquia de maximo dos niveles dentro de `EXPEDIENTE_DOCUMENTO_ANALIZADO`: documento principal con `ID_DOCUMENTO_PADRE` nulo y documento relacionado/respuesta con `ID_DOCUMENTO_PADRE` apuntando al principal. No registrar respuestas como nueva solicitud o expediente independiente, no permitir nietos y usar baja logica con `ACTIVO=0` cuando corresponda.
- `Analisis`: al recibir un expediente principal, recibir en la misma transaccion los documentos asociados que ya se encuentren en `ASIGNACION / ASIGNADO`. Si un documento se asocia despues de la recepcion del principal, debe quedar pendiente y solo el abogado responsable puede recibirlo individualmente desde la seccion `Documentos asociados`; esta accion no lo convierte automaticamente en documento analizado.
- `Analisis`: la derivacion externa no debe mostrarse como boton o accion preparada en el panel de Analisis; si se requiere en el futuro, debe definirse el flujo funcional completo de entidad destino, tipo de derivacion y datos documentales antes de habilitar escritura.
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
- El panel derecho de `Bandeja Registro` se abre con doble clic sobre la fila, no con un clic simple. Debe usar el titulo `Panel de Registro` y mostrar debajo el nombre del titular en azul, con el mismo estilo visual replicable en otros paneles de datos.
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
- Para la version de un unico analisis, las columnas funcionales de documentos analizados son: `Tipo`, `Estado`, `Detalle Obs.`, `Fecha Emision`, `N° Documento`, `Descripcion`, `¿Requiere respuesta?`, `Fecha Acuse`, `Confirmacion de respuesta`, `Fecha Respuesta`, `Hoja de Envio` y `Notificado`. Si existe columna de control o expandir, no debe contarse como dato funcional ni mostrarse como ID tecnico.
- La grilla jerarquica propuesta para documentos de analisis puede usar columnas: expandir/contraer, `Tipo documento`, `Numero documento`, `Fecha documento`, `Descripcion`, `¿Requiere respuesta?`, `Estado respuesta`, `Estado documento`, `Observacion`, `Usuario registro` y `Fecha registro`, siempre ocultando IDs tecnicos.
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
