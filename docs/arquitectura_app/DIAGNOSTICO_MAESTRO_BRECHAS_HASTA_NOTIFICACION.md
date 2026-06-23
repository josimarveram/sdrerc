# Diagnostico maestro de brechas hasta Notificacion

Fecha: 2026-06-23

Alcance: Registro / Recepcion, Asignacion, Analisis, Verificacion integrada con firma/emision/documento emitido, Ejecucion y Notificacion en SDRERC V2.

Este documento ejecuta unicamente el `PROMPT 0 - Diagnostico maestro hasta Notificacion` de `PLAN_PROMPTS_INCREMENTALES_HASTA_NOTIFICACION.md`. No implementa codigo, no crea scripts SQL, no ejecuta SQL y no modifica datos.

## Fuentes revisadas

- `AGENTS.md`.
- `docs/arquitectura_app/detalle_funciones.md`.
- `docs/arquitectura_app/PLAN_PROMPTS_INCREMENTALES_HASTA_NOTIFICACION.md`.
- Documentacion relevante en `docs/arquitectura_app/` y `docs/arquitectura_bd/`.
- Scripts existentes en `db/sdrerc_app/scripts/`.
- Vistas V2 de Registro / Recepcion, Asignacion, Analisis, Verificacion, Ejecucion, Notificacion y Consola.
- Services, DTOs y DAOs V2 de `src/main/java/com/sdrerc/application/sdrercapp/`, `domain/dto/sdrercapp/` e `infrastructure/sdrercapp/dao/`.
- `MenuPrincipalV2`, componentes `appv2` y `AppV2Theme`.

## Resumen ejecutivo

El flujo V2 ya tiene una base amplia: Registro / Recepcion, Asignacion, Analisis, Verificacion, Ejecucion y Notificacion existen con DAOs/Services transaccionales, paneles contextuales, busquedas, estados por etapa, asociados, grupo familiar, hoja de envio, documentos analizados y acciones principales. Tambien existe integracion visual de Firma / Emision dentro de Verificacion, sin entrada lateral independiente.

Las brechas principales no son de pantalla inicial, sino de cierre funcional fino del flujo: plazos por tipo de procedimiento/documento, separacion entre guardar documentos intermedios y registrar resultado final de Analisis, ruteo correcto desde Verificacion segun tipo de documento/resolucion, preparacion de cartas de notificacion en Ejecucion, asignacion/atencion de Notificacion por documento y control de publicacion sin adelantar el modulo Publicacion.

La recomendacion es ejecutar los prompts incrementales en el orden ya definido, empezando por pulir Registro / Recepcion y Asignacion, luego separar documentos intermedios en Analisis, despues consolidar Verificacion/Firma/Documento emitido, cerrar Ejecucion y finalmente Notificacion.

## Estado transversal

| Tema | Estado actual | Brecha / riesgo | Recomendacion |
|---|---|---|---|
| Separacion V2 vs legacy | Los modulos V2 estan en paquetes nuevos y `MenuPrincipalV2`. | Persisten clases legacy en el arbol, pero no deben tocarse. | Mantener cambios solo en V2. |
| Firma / Emision | No aparece como entrada lateral independiente; Verificacion concentra acciones de firma/emision. | `HomeV2` aun muestra `Firma / Emision` como tarjeta/modulo operativo. Puede ser aceptable como etapa del flujo, pero no debe sugerir modulo lateral independiente. | En prompt de Verificacion o cierre visual, ajustar texto del Home si se decide que genera confusion. |
| Etapa visual VALIDACION | No se detecto una macro etapa `VALIDACION`. | En carga diaria existe columna `ESTADO VALIDACION`, que es validacion de archivo, no etapa del flujo. | Mantener como columna tecnica de previsualizacion o renombrar a `Estado de revision` si el texto se confunde con etapa. |
| Emitido | Verificacion/Firma usa estados de firma/emision y `DisplayNameMapperV2` presenta nombres amigables. | La accion tecnica de emision reutiliza transiciones reales de firma/numeracion; debe evitarse inventar `EMISION_DOCUMENTO` si no existe. | Mantener integrado en Verificacion y validar mensajes para que el usuario vea `Documento emitido`/`Emitido`, no un modulo separado. |
| Requiere publicacion / Fecha publicacion | El modelo tiene `EXPEDIENTE.REQUIERE_PUBLICACION`, `EXPEDIENTE_NOTIFICACION.REQUIERE_PUBLICACION` y `EXPEDIENTE_PUBLICACION.FECHA_PUBLICACION`. | La captura temprana de `Requiere publicacion` y `Fecha publicacion` antes de Notificacion no esta claramente cerrada en Asignacion/Analisis/Verificacion. | Definir en prompts posteriores donde se completa el dato sin implementar Publicacion completa. |
| Busquedas y orden | Se extendieron busquedas por expediente, tramite, SGD, acta, titular/documento en varios DAOs. | Debe validarse uniformidad en todos los modulos hasta Notificacion y el orden por dias/titular. | Incluir en cada prompt de modulo una verificacion de busqueda y orden comun. |
| Plazos | Hay `CalendarioLaboralService`, feriados y configuracion de plazos. | `detalle_funciones.md` menciona plazos por procedimiento: Rect. Adm. 30, Reconsideracion 15, Apelacion 30; AGENTS fija configuracion inicial general de 30 dias habiles. | Decidir si el modelo de `PLAZO_CONFIGURACION` debe soportar plazo por procedimiento antes de cambiar calculos. |
| Concurrencia | DAOs transaccionales bloquean/validan estados en acciones principales. | Riesgo de doble escritura en asignacion, documentos de analisis y notificacion si se habilitan nuevas acciones sin validar estado actual. | Cada prompt posterior debe exigir validacion de estado y transicion activa antes de escribir. |

## Matriz por modulo

### 1. Registro / Recepcion

| Aspecto | Funcionalidad esperada | Implementado actual | Faltante / brecha | Archivos impactados | Tablas/catalogos | Riesgo | Prompt recomendado |
|---|---|---|---|---|---|---|---|
| Registro individual y carga diaria | Registrar solicitudes SITD individual o por Excel con campos obligatorios. | Existe registro manual, carga diaria, plantilla, previsualizacion editable e importacion. | Validar que el registro manual y carga diaria mantengan los mismos campos obligatorios y nombres definitivos: `N° expediente SGD`, canal recepcion, fecha recepcion SDRERC. | `JPanelRegistroRecepcionV2`, `JPanelCargaDiariaRecepcionV2`, `JPanelRegistroManualRecepcionV2`, `CargaDiaria*Service`, `ExpedienteRegistroDAO`. | `EXPEDIENTE`, `EXPEDIENTE_SOLICITUD`, catalogos de procedimiento/tipo documento/tipo acta/canal. | Medio: inconsistencias de plantilla vs registro manual. | PROMPT 1 |
| Duplicidad | Validar duplicidad por numero de acta + titular, no bloquear registro. | DAO y UI manejan potencial duplicado, observacion y ausencia de numero para duplicados. | Validar en pruebas que no se consuma correlativo y que siempre llegue a Asignacion sin numero cuando corresponde. | `ExpedienteRegistroDAO`, `JPanelCargaDiariaRecepcionV2`, `JPanelRegistroManualRecepcionV2`. | `EXPEDIENTE_SOLICITUD.POTENCIAL_DUPLICADO`, `EXPEDIENTE.NUMERO_EXPEDIENTE`. | Alto si genera numeros indebidos. | PROMPT 1 |
| Reconsideracion / Apelacion | No generar numero de expediente en Registro; Asignacion decide asociar o generar numero. | Regla documentada en AGENTS y DAO tiene rutas para solicitudes sin numero. | Validar UI/mensajes en carga diaria y registro manual; asegurar que el usuario entiende que no es error. | `ExpedienteRegistroDAO`, `JPanelCargaDiariaRecepcionV2`, `JPanelRegistroManualRecepcionV2`. | Catalogo procedimiento registral. | Medio. | PROMPT 1 |
| Edicion previa a asignacion | Asignador puede editar mientras el expediente esta en estado Registrado y sin abogado. | Existe edicion manual con validacion de estado/asignacion. | Completar pruebas de campos editables vs no editables y garantizar que no cambia el numero generado. | `ExpedienteEdicionManualDAO`, `JPanelRegistroManualRecepcionV2`, `JPanelRegistroRecepcionV2`. | `EXPEDIENTE_SOLICITUD`, `EXPEDIENTE_PERSONA`, `EXPEDIENTE_ACTA`. | Medio. | PROMPT 1 |
| Grupo familiar | Alerta no bloqueante por apellidos y marca funcional. | Existe marca simple en solicitud, plantilla/preview y exposicion en Asignacion/Consola. | Afinar heuristica y evitar falsos positivos; no crear relaciones automaticas. | `CargaDiariaValidacionService`, `ExpedienteRegistroDAO`, `AsignacionExpedienteDAO`, `ExpedienteConsolaDAO`. | `EXPEDIENTE_SOLICITUD.GRUPO_FAMILIAR`, criterio y observacion. | Medio por confusion con duplicidad. | PROMPT 1 |
| Observaciones de previsualizacion | Las observaciones generadas por validacion deben conservarse. | La previsualizacion recalcula observaciones, pero la columna observacion aparece editable. | Revisar si permitir edicion manual puede borrar mensajes de validacion; separar `Observacion usuario` de `Observacion validacion` o bloquear la columna calculada. | `JPanelCargaDiariaRecepcionV2`, `CargaDiariaPreviewDTO`, `CargaDiariaValidacionService`. | No necesariamente requiere BD. | Medio. | PROMPT 1 |
| Plazos por procedimiento | Dias pendientes por tipo de procedimiento. | El calculo habil general ya existe. | Decidir si Rect. Adm./Apelacion/Reconsideracion requieren configuracion diferenciada; hoy no debe hardcodearse. | `CalendarioLaboralService`, `PlazoConfiguracionDAO`, Registro Services. | `PLAZO_CONFIGURACION`. | Alto si se muestra plazo incorrecto. | PROMPT 1 o prompt tecnico de plazos |

### 2. Asignacion

| Aspecto | Funcionalidad esperada | Implementado actual | Faltante / brecha | Archivos impactados | Tablas/catalogos | Riesgo | Prompt recomendado |
|---|---|---|---|---|---|---|---|
| Bandeja y panel contextual | Visualizar nuevos registros, sin numero, asociados, grupo familiar y datos de seleccion. | Existe grilla expandible/anidada, panel simple/multiple, documentos asociados y panel contextual. | Mantener consistencia visual al agregar nuevas secciones; evitar que asociados no operativos entren en asignacion. | `JPanelAsignacionV2`, componentes `appv2`. | Lectura desde `EXPEDIENTE`, `EXPEDIENTE_SOLICITUD`, `EXPEDIENTE_RELACION`. | Medio. | PROMPT 2 |
| Asociar duplicados / relacionados | Asociar por misma acta y titular; duplicado hereda numero/equipo/abogado cuando corresponde. | `ExpedienteRelacionadoDAO` registra relacion e historial; sincroniza numero/equipo/abogado segun reglas. | Validar casos con Reconsideracion/Apelacion y solicitudes sin numero para no asociar por motivo equivocado. | `ExpedienteRelacionadoDAO`, `JPanelAsignacionV2`. | `EXPEDIENTE_RELACION`, `EXPEDIENTE_HISTORIAL`, `EXPEDIENTE`. | Alto por trazabilidad. | PROMPT 2 |
| Generar numero a criterio del asignador | Para Reconsideracion/Apelacion o duplicados sin numero, el asignador puede generar numero si corresponde. | DAO tiene validacion de generacion manual solo para procedimientos permitidos. | Mejorar UX de decision y evidenciar historial/motivo; validar correlativo con concurrencia. | `AsignacionExpedienteDAO`, `JPanelAsignacionV2`. | `EXPEDIENTE.NUMERO_EXPEDIENTE`, historial. | Alto. | PROMPT 2 |
| Asignacion individual/masiva y hoja de envio | Capturar hoja de envio unica por expediente. | Existe captura individual/multiple y validacion de unicidad en DAO. | Confirmar que el script de columna/indice este aplicado en BD destino; mejorar feedback cuando falta columna. | `JPanelAsignacionV2`, `AsignacionExpedienteDAO`. | `EXPEDIENTE_ASIGNACION.NUMERO_HOJA_ENVIO`. | Alto si la BD no tiene script aplicado. | PROMPT 2 |
| Cartas de respuesta | Asignador registra notificacion, acuse, confirmacion, fecha respuesta y hoja de envio para cartas generadas en Analisis. | Existe seccion `Cartas de respuesta` usando `EXPEDIENTE_DOCUMENTO_ANALIZADO` y `DocumentoAnalisisDAO`. | Falta definir donde se captura `Requiere publicacion` y `Fecha publicacion`; validar plazos por tipo de carta y abandono por falta de respuesta. | `JPanelAsignacionV2`, `DocumentoAnalisisDAO`, `DocumentoAnalisisService`. | `EXPEDIENTE_DOCUMENTO_ANALIZADO`, script 29. | Alto por dependencia con Analisis/Notificacion. | PROMPT 2 y PROMPT 3 |
| Grupo familiar y carga laboral | Identificar grupo familiar y asignar preferentemente al mismo abogado; ver carga laboral. | Grupo familiar se muestra como alerta/sugerencia. | No se identifico una ventana consolidada de carga laboral por abogado. La asignacion coordinada no debe ser automatica, pero requiere soporte visual. | `JPanelAsignacionV2`, `EquipoJuridicoDAO`/Services si aplica. | `EXPEDIENTE_ASIGNACION`, equipo/usuario. | Medio. | PROMPT 2 |

### 3. Analisis

| Aspecto | Funcionalidad esperada | Implementado actual | Faltante / brecha | Archivos impactados | Tablas/catalogos | Riesgo | Prompt recomendado |
|---|---|---|---|---|---|---|---|
| Recepcion por abogado | Abogado recibe expediente y documentos asociados. | `AnalisisExpedienteDAO` recibe principal y asociados ya asignados; UI permite recibir asociados pendientes. | Validar permisos reales del abogado responsable y comportamiento si se asocia despues de la recepcion. | `JPanelAnalisisV2`, `AnalisisExpedienteDAO`, `ExpedienteRelacionadoDAO`. | `EXPEDIENTE_ASIGNACION`, `EXPEDIENTE_RELACION`, historial. | Medio. | PROMPT 3 |
| Documentos de analisis | Registrar documentos tipo `ANALISIS_%`, estado, fechas, descripcion, notificacion, acuse, respuesta. | Grilla de documentos existe con columnas extendidas y edicion de estado. | Falta separar `Guardar documento de analisis` de `Registrar resultado final`; hoy `registrarAnalisis` agrupa documentos, evaluacion y resultado. | `JPanelAnalisisV2`, `AnalisisExpedienteService`, `AnalisisExpedienteDAO`, `DocumentoAnalisisDAO`. | `EXPEDIENTE_DOCUMENTO_ANALIZADO`, `EXPEDIENTE_EVALUACION`. | Alto: impide carta intermedia sin cerrar analisis. | PROMPT 3 |
| Carta intermedia | Guardar carta intermedia sin resultado final ni cierre de analisis. | Existe soporte documental y campos de respuesta, pero no una accion independiente clara. | Definir accion/estado real si requiere espera; no crear etapa `Asignacion de respuesta`. Si no hay transicion, guardar solo documento y mantener expediente en Analisis. | `JPanelAnalisisV2`, `AnalisisExpedienteDAO`, `DocumentoAnalisisDAO`. | `TIPO_DOCUMENTO_ADJUNTO`, `EXPEDIENTE_DOCUMENTO_ANALIZADO`, flujo si existe. | Alto. | PROMPT 3 |
| Resultado final | Procedente, Improcedente, Procedente en parte, No corresponde, Observado/Abandono segun flujo. | Se registra evaluacion, observacion y transiciones a estados derivados. `NO_CORRESPONDE` tiene tratamiento especial. | Validar que no se exija resultado cuando solo se guarda documento; validar abandono por falta de respuesta si existe transicion real. | `AnalisisExpedienteDAO`, `JPanelAnalisisV2`. | `TIPO_RESULTADO_EVALUACION`, `FLUJO_TRANSICION`. | Alto. | PROMPT 3 |
| Derivaciones directas | No exponer derivacion a Notificacion ni derivacion externa desde Analisis. | UI no muestra esos botones. | `AnalisisExpedienteService/DAO` aun contiene `derivarNotificacionEspecial`; ruta dormida debe revisarse para no reexponerse sin acuerdo. | `AnalisisExpedienteService`, `AnalisisExpedienteDAO`. | `FLUJO_TRANSICION` si existe. | Medio. | PROMPT 3 |
| Datos complementarios | Abogado completa DNI, ubicacion de notificacion, carpeta expediente digital y enlace. | No se confirma una seccion madura para completar todos esos datos desde Analisis. | Definir alcance: si datos corresponden a Registro/Expediente digital/Notificacion, no duplicarlos; si se requieren en Analisis, agregar por prompt posterior. | `JPanelAnalisisV2`, consola, Expediente Digital. | `EXPEDIENTE_SOLICITUD`, `EXPEDIENTE_DIGITAL`, notificacion. | Medio. | PROMPT 3 |

### 4. Verificacion integrada con firma/emision/documento emitido

| Aspecto | Funcionalidad esperada | Implementado actual | Faltante / brecha | Archivos impactados | Tablas/catalogos | Riesgo | Prompt recomendado |
|---|---|---|---|---|---|---|---|
| Revision y observacion | Supervisor revisa documentos, observa y devuelve a Analisis si hay inconsistencia. | Panel Verificacion tiene aprobar, observar, documento inconsistente, devolver a Analisis. | Validar que la devolucion conserve documentos y motivo; no crear rutas paralelas. | `JPanelVerificacionV2`, `VerificacionExpedienteDAO`. | `EXPEDIENTE_DOCUMENTO_ANALIZADO`, `EXPEDIENTE_HISTORIAL`. | Medio. | PROMPT 4 |
| Firma/Emision integrada | Verificacion concentra firma, emision, numeracion y envio a Ejecucion. | `JPanelVerificacionV2` usa `FirmaEmisionExpedienteService/DAO`; no hay menu lateral Firma/Emision. | Ajustar etiquetas para que no parezca modulo independiente; revisar `HomeV2` si aplica. | `JPanelVerificacionV2`, `FirmaEmisionExpedienteDAO`, `MenuPrincipalV2`, `HomeV2`. | `EXPEDIENTE_RESOLUCION`, `FLUJO_TRANSICION`. | Medio. | PROMPT 4 |
| Ruteo por tipo de documento | Solo resoluciones van a Ejecucion; otros documentos van a Notificacion. | Hay envio a firma y envio a Ejecucion para resoluciones/numeracion. | No se ve cerrado el ruteo alterno de documentos no resolutivos hacia Notificacion. Debe depender de tipo/resultado/transicion real. | `JPanelVerificacionV2`, `VerificacionExpedienteDAO`, `FirmaEmisionExpedienteDAO`, `NotificacionExpedienteDAO`. | `TIPO_DOCUMENTO_ADJUNTO`, `EXPEDIENTE_RESOLUCION`, `FLUJO_TRANSICION`. | Alto. | PROMPT 4 |
| Carta Edicto firmada por Subdirector | Verificacion debe controlar estados incluso si firma el Subdirector. | No se identifico control visual/rol especifico para Subdirector. | Definir si es validacion de rol, etiqueta de firmante o solo tipo documental. | `JPanelVerificacionV2`, servicios de usuarios/roles, documentos. | `USUARIO`, `ROL`, `TIPO_DOCUMENTO_ADJUNTO`. | Medio. | PROMPT 4 |
| Requiere publicacion / Fecha publicacion | Deben quedar disponibles para Notificacion/Publicacion. | Existen campos en modelo, pero no se confirma captura completa en Verificacion. | Definir si `Fecha publicacion` se registra en Notificacion/Publicacion o se predeclara en Verificacion. | `JPanelVerificacionV2`, DAOs de Notificacion/Publicacion. | `EXPEDIENTE_PUBLICACION.FECHA_PUBLICACION`, `EXPEDIENTE.REQUIERE_PUBLICACION`. | Medio. | PROMPT 4 |

### 5. Ejecucion

| Aspecto | Funcionalidad esperada | Implementado actual | Faltante / brecha | Archivos impactados | Tablas/catalogos | Riesgo | Prompt recomendado |
|---|---|---|---|---|---|---|---|
| Recepcion de resoluciones firmadas | Supervisor extrae del SITD y valida error material. | Ejecucion consulta resolucion/documentos y permite registrar ejecucion, observar, inconsistencia, devolver a Analisis. | No hay integracion SITD; debe quedar como metadata/manual. Falta precisar validacion de error material con hoja de envio y nuevo numero de resolucion al reingresar. | `JPanelEjecucionV2`, `EjecucionExpedienteDAO`. | `EXPEDIENTE_RESOLUCION`, `EXPEDIENTE_HISTORIAL`. | Alto. | PROMPT 5 |
| Derivar al abogado que analizo | Resoluciones sin error deben ir al mismo abogado que analizo. | No se confirma una reasignacion/derivacion explicita al abogado original desde Ejecucion. | Definir si Ejecucion usa abogado responsable previo o una asignacion nueva; AGENTS evita nueva asignacion fuera de definicion. | `EjecucionExpedienteDAO`, `AsignacionExpedienteDAO`, DTOs. | `EXPEDIENTE_ASIGNACION`, historial. | Alto. | PROMPT 5 |
| Carta de notificacion | Abogado proyecta carta; supervisor valida antes de Notificacion. | Panel Ejecucion tiene documentos y derivacion a Notificacion, pero no se ve flujo especifico de carta de notificacion y validacion de supervisor. | Agregar gestion documental/estado si el modelo lo soporta; si no, diagnosticar script en prompt posterior. | `JPanelEjecucionV2`, `DocumentoEjecucionService`, `EjecucionExpedienteDAO`. | `EXPEDIENTE_DOCUMENTO`, `TIPO_DOCUMENTO_ADJUNTO`, `FLUJO_TRANSICION`. | Alto. | PROMPT 5 |
| Derivar a Notificacion | Solo cuando carta/resolucion esta lista y transicion real existe. | Existe accion `derivarNotificacion` en DAO/UI. | Validar prerequisitos documentales y que no derive incompleto. | `JPanelEjecucionV2`, `EjecucionExpedienteDAO`. | `FLUJO_TRANSICION`, `TIPO_MOVIMIENTO`. | Alto. | PROMPT 5 |

### 6. Notificacion

| Aspecto | Funcionalidad esperada | Implementado actual | Faltante / brecha | Archivos impactados | Tablas/catalogos | Riesgo | Prompt recomendado |
|---|---|---|---|---|---|---|---|
| Validacion de carta | Supervisor verifica carta de notificacion y devuelve a Ejecucion si hay error. | Panel Notificacion consume documentos de Ejecucion y registra notificacion/cargo/publicacion. | No se ve accion clara de validar carta ni devolver a Ejecucion para subsanacion. | `JPanelNotificacionV2`, `NotificacionExpedienteDAO`, `EjecucionExpedienteDAO`. | `FLUJO_TRANSICION`, documentos/historial. | Alto. | PROMPT 6 |
| Asignacion a abogados de Notificacion | Supervisor deriva documentos a abogados de Notificacion. | No se identifico asignacion de notificacion por abogado/documento. | Definir si usa `EXPEDIENTE_ASIGNACION`, nueva metadata, o solo responsable del expediente; no crear tabla sin acuerdo. | `JPanelNotificacionV2`, `NotificacionExpedienteDAO`, usuarios/equipo. | `USUARIO`, `EQUIPO_JURIDICO`, posible asignacion. | Alto. | PROMPT 6 |
| Registro por documento | Abogado registra tipo, fecha, acuse por documento. | `NotificacionRegistroDTO` y DAO trabajan principalmente por expediente; panel muestra documentos de Ejecucion. | Falta granularidad por documento si el requerimiento exige notificar cada documento. | `NotificacionRegistroDTO`, `JPanelNotificacionV2`, `NotificacionExpedienteDAO`. | `EXPEDIENTE_NOTIFICACION`, `EXPEDIENTE_CARGO_ACUSE`, documentos. | Alto. | PROMPT 6 |
| Intentos de notificacion | Hasta 3 intentos: 1 virtual y 2 presencial/fisico. | DAO calcula siguiente intento con maximo 3. | Validar secuencia obligatoria virtual/presencial; hoy el maximo no garantiza la regla de orden. | `NotificacionExpedienteDAO`, `NotificacionValidacionService`, UI. | `EXPEDIENTE_NOTIFICACION`, catalogos de tipo notificacion. | Medio. | PROMPT 6 |
| Acuse y cierre | Registrar cargo, marcar notificado y avanzar si flujo permite. | Existen `registrarCargo`, `marcarNotificado`, `cerrarExpediente`. | Validar prerequisitos: acuse requerido, resultado, publicacion requerida y expediente digital. | `NotificacionExpedienteDAO`, `NotificacionValidacionService`. | `EXPEDIENTE_CARGO_ACUSE`, `FLUJO_TRANSICION`. | Medio. | PROMPT 6 |
| Preparacion de publicacion | Si falla notificacion, preparar publicacion sin implementar Publicacion completa. | Notificacion ya tiene accion `registrarPublicacion` que puede insertar `EXPEDIENTE_PUBLICACION` y mover a Publicacion. | Ajustar alcance del prompt: Notificacion puede preparar/generar con transicion real, pero no debe asumir gestion completa de Publicacion. | `JPanelNotificacionV2`, `NotificacionExpedienteDAO`, `PublicacionExpedienteDAO`. | `EXPEDIENTE_PUBLICACION`, `FLUJO_TRANSICION`. | Medio/alto por cruce de modulo. | PROMPT 6 |

## Brechas de base de datos y scripts potenciales

No se crearon scripts. Solo se identifican necesidades potenciales para prompts posteriores:

1. Configuracion de plazo por procedimiento si se decide aplicar plazos distintos para Reconsideracion/Apelacion/Rectificacion administrativa.
2. Metadata de carta intermedia si los tipos `ANALISIS_%` existentes no cubren `CARTA EDICTO`, `CARTA FALTA SUSTENTO`, `CARTA INDAGATORIO`, `CARTA PRETENSION` con plazos de respuesta.
3. Campo/relacion para responsable de Notificacion por documento si se confirma asignacion especifica a abogados notificadores.
4. Prerrequisitos documentales para derivacion desde Ejecucion a Notificacion, si no se pueden resolver con `EXPEDIENTE_DOCUMENTO`/`EXPEDIENTE_DOCUMENTO_ANALIZADO`.
5. Captura uniforme de `Requiere publicacion` y `Fecha publicacion`; el modelo ya tiene piezas, pero debe decidirse punto funcional de captura.

## Flujos mal conectados o a validar

1. `Analisis`: la escritura de documentos analizados esta acoplada a `registrarAnalisis`, que tambien registra resultado. Esto bloquea el caso de carta intermedia sin resultado final.
2. `Analisis`: existen metodos de derivacion especial a Notificacion en Service/DAO aunque la UI ya no los expone. Deben quedar deshabilitados o eliminados funcionalmente en un prompt posterior si no tienen transicion vigente.
3. `Verificacion`: el ruteo diferenciado entre resoluciones hacia Ejecucion y otros documentos hacia Notificacion no esta cerrado en la UI/DAO revisada.
4. `Ejecucion`: la validacion de carta de notificacion por supervisor y retorno a Ejecucion desde Notificacion no estan claramente conectados.
5. `Notificacion`: la notificacion parece expediente-centrica; el requerimiento funcional habla de registro por documento.
6. `Notificacion`: se implementa preparacion/generacion de publicacion, pero debe limitarse a transicion real y no invadir la gestion completa de Publicacion.
7. `HomeV2`: puede sugerir `Firma / Emision` como modulo operativo independiente aunque el menu lateral ya lo integra en Verificacion.

## Orden recomendado de implementacion

1. PROMPT 1 - Completar Registro / Recepcion.
   - Cerrar plantilla, previsualizacion, observaciones de validacion, edicion manual, regla de sin numero y plazos por procedimiento si se aprueba.

2. PROMPT 2 - Completar Asignacion.
   - Pulir generacion manual de numero, asociacion, asignacion masiva, hoja de envio, grupo familiar, carga laboral y cartas de respuesta.

3. PROMPT 3 - Completar Analisis.
   - Separar `Guardar documento de analisis` de `Registrar resultado final`.
   - Habilitar carta intermedia sin cerrar analisis.
   - Mantener documentos asociados disponibles y sin convertirlos automaticamente en analizados.

4. PROMPT 4 - Completar Verificacion + Firma/Emision integrada.
   - Consolidar revision, firma, emision, numeracion, `Emitido` y ruteo por tipo de documento.
   - Mantener Firma / Emision sin menu independiente.

5. PROMPT 5 - Completar Ejecucion.
   - Validar resoluciones firmadas, error material, retorno a Analisis, carta de notificacion y derivacion segura a Notificacion.

6. PROMPT 6 - Completar Notificacion hasta preparacion de Publicacion.
   - Validar carta, asignar/atender notificacion si se define, registrar intentos por documento, acuse y preparacion de publicacion sin implementar Publicacion completa.

7. PROMPT 7 - QA integral hasta Notificacion.
   - Flujo end-to-end, regresiones, permisos, estados, transiciones y build.

8. PROMPT 8 - Documentacion y reglas finales.
   - Actualizar reglas persistentes solo con acuerdos consolidados.

## Restricciones cumplidas

- No se modifico codigo Java.
- No se crearon scripts SQL.
- No se ejecuto SQL.
- No se modificaron datos.
- No se toco codigo legacy.
- No se toco `OracleConnection.java`.
- No se cambiaron reglas de negocio.
- No se inventaron transiciones.
- No se creo etapa visual `VALIDACION`.
- No se ejecuto ningun prompt posterior al PROMPT 0.
