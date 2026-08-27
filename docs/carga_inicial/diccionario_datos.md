# Diccionario de datos — Carga Inicial (`data-unificada-2026.xlsx`)

Este documento lista **todas las columnas reales de encabezado** (fila 1) de las 2 hojas del Excel de migración histórica, y cómo las considera/guarda hoy el batch de Carga Inicial (`CargaInicialArchivoParserService` + `CargaInicialDAO`, en `v3/sdrerc-v3-backend`). Generado leyendo únicamente la fila 1 de cada hoja (regla de confidencialidad: nunca se leen/imprimen datos de fila).

Marca las columnas como:
- **Mapeada**: se lee y se persiste en una tabla/columna real de Oracle.
- **Usada, no persistida**: se lee y participa en una decisión/cálculo, pero no queda guardada tal cual (ej. reglas de mapeo, notas de texto libre).
- **Ignorada**: el parser ni siquiera la lee hoy.

Revisa la columna **"Cómo se guarda / se usa hoy"** y dime si algo está mal considerado — con eso ajusto el código.

---

## Hoja "DATA DIARIA UNIFICADA" (963 filas)

| # | Encabezado real (Excel) | Estado | Campo interno (`CargaInicialFilaDTO`) | Cómo se guarda / se usa hoy |
|---|---|---|---|---|
| 0 | FECHA DE SOLICITUD | Mapeada | `fechaSolicitud` | `EXPEDIENTE.fecha_registro`, `EXPEDIENTE.fecha_ultimo_movimiento` y `EXPEDIENTE_SOLICITUD.fecha_recepcion`. También base para calcular `fecha_vencimiento` (plazo legal). |
| 1 | TIPO DE TRAMITE | Usada, no persistida | `tipoTramite` | Se agrega como texto libre a la nota de `EXPEDIENTE_HISTORIAL.comentario` del registro ("Tipo trámite (Excel): ..."). No tiene columna estructurada propia. |
| 2 | CANAL DE RECEPCION | Mapeada | `canalRecepcion` | Se traduce a un código real de `CANAL_RECEPCION` (MPV→`MESA_PARTES_VIRTUAL`, MP Presencial→`MESA_PARTES_PRESENCIAL`, OR→`OR_PRESENCIAL`, etc.) y se guarda en `EXPEDIENTE_SOLICITUD.id_canal_recepcion`. Si el texto no calza con ningún código conocido, la fila queda `INCOMPLETO` ("Canal no reconocido"). |
| 3 | DERIVADO POR | Usada, no persistida | `derivadoPor` | Igual que "Tipo de trámite": texto libre agregado a la nota de historial ("Derivado por: ..."). |
| 4 | DNI SOLICITANTE | Mapeada | `dniSolicitante` | `PERSONA.numero_documento` (tipo_documento="DNI") de la persona única titular=solicitante (ver fila 11). Si dice "SIN DNI"/"S/N", se guarda `NULL`. |
| 5 | N° TRAMITE WEB | Mapeada | `numeroTramiteWeb` | `EXPEDIENTE_SOLICITUD.numero_tramite_documentario`. |
| 6 | TIPO DOCUMENTO | Mapeada | `tipoDocumento` | `EXPEDIENTE_DOCUMENTO.nombre_documento` (texto libre, sin resolver contra catálogo). |
| 7 | N° DOCUMENTO | Mapeada | `numeroDocumento` | `EXPEDIENTE_DOCUMENTO.numero_documento`. |
| 8 | TIPO DE SOLICITUD | Mapeada | `tipoSolicitud` | `EXPEDIENTE_SOLICITUD.asunto` (texto libre, **no** se resuelve contra el catálogo de Procedimiento Registral). |
| 9 | TIPO DE ACTA | Mapeada | `tipoActa` | Se resuelve contra `TIPO_ACTA` (por código o nombre aproximado) → `EXPEDIENTE_ACTA.id_tipo_acta`. Si no calza, queda `NULL` (no bloquea). |
| 10 | N° ACTA | Mapeada | `numeroActa` | `EXPEDIENTE_ACTA.numero_acta`. Si viene vacío, la fila queda `INCOMPLETO` ("Sin número de acta") pero el expediente igual se crea. |
| 11 | TITULAR | Mapeada (clave) | `titular` | Nombre de la única `PERSONA` que se crea/reutiliza para esta fila; se enlaza a `EXPEDIENTE_PERSONA` con **2 roles a la vez**: `TITULAR` y `REMITENTE` (el Excel no trae un solicitante distinto del titular, ver decisión de diseño abajo). |
| 12 | RESPONSABLE PREEVALUACION | Usada, no persistida | `responsablePreevaluacion` | Texto libre agregado a la nota de `EXPEDIENTE_HISTORIAL.comentario` del registro ("Responsable preevaluación (Excel): ..."). |
| 13 | ESTADO | Ignorada | — | No se lee. |
| 14 | ESTADO DE RECEPCION | Ignorada | — | No se lee. |
| 15 | FECHA ASIGNADO | Mapeada | `fechaAsignado` | `EXPEDIENTE_ASIGNACION.fecha_asignacion` y fecha del historial de asignación. |
| 16 | MES_ASIGNACION_EXPEDIENTE | Ignorada | — | No se lee (derivable de la fecha, columna auxiliar del propio Excel). |
| 17 | N° DE DOCUMENTO HOJA DE ENVIO | Mapeada | `numeroHojaEnvioAsignacion` | `EXPEDIENTE_ASIGNACION.numero_hoja_envio`. Si el número ya existe en OTRO expediente real (duplicado histórico genuino, verificado), se guarda `NULL` en su lugar y la fila queda `INCOMPLETO` con el motivo explicado. |
| 18 | N° EXPEDIENTE DIGITAL | Mapeada (pase aparte) | `numeroExpedienteDigital` | Marca de "esta fila es duplicado de otro expediente ya numerado" (formatos `SDRERC_EXP_2026_000NNN` / `DUPLICADO_EXP NNN` / `DUPLICIDAD_EXP NNNN`). En un pase posterior (`corregirNumeroExpedienteDigital`), el correlativo extraído reemplaza el `numero_expediente` recién generado por el ya existente del expediente principal — **no** se crea `EXPEDIENTE_RELACION` (vínculo formal de "asociado"), solo se iguala el número. |
| 19 | TIPO CONTRATO | Ignorada (descartada) | — | Decisión ya confirmada: es un dato de RR.HH. del abogado, no del expediente — nunca se lee. |
| 20 | ASIGNADO A ABOGADO: | Mapeada | `asignadoAAbogado` | Se resuelve por nombre (tolera tildes/coma/espacios + una tabla de 9 alias de typos ya verificados, incluye usuarios **inactivos** a propósito) contra `USUARIO.nombre_completo` → `id_usuario_responsable_actual`/`EXPEDIENTE_ASIGNACION.id_usuario`. Si resuelve, además exige que tenga **exactamente 1** equipo activo (o el combo especial EQ_ANALISIS+EQ_EJECUCION) para poder asignar `id_equipo`; si no, el expediente se crea pero queda en `REGISTRO` (no llega a `ASIGNACION`). |
| 21 | N° RESOLUCION | Mapeada | `numeroResolucion` | Usado como número del documento "Resolución" sintetizado en Análisis (`EXPEDIENTE_DOCUMENTO_ANALIZADO.numero_documento`) — con prioridad a la columna 22 (`resolucionLimpia`) si viene poblada. |
| 22 | RESOLUCION_LIMPIA | Mapeada | `resolucionLimpia` | Versión "limpia" del número de Resolución — se prefiere sobre la columna 21 cuando ambas tienen valor. |
| 23 | DATA | Ignorada | — | No se lee (columna auxiliar/técnica del propio archivo de cruce). |
| 24 | EJECUCIONES.RESOLUCIONES | Ignorada | — | No se lee (redundante con la columna 21/22, del lado de la hoja EJECUCIONES original). |
| 25 | EJECUCIONES.PRIORIDAD | Ignorada | — | No se lee. |
| 26 | EJECUCIONES.FECHA DE EMISIÓN | Mapeada | `fechaEmisionAnalisis` | Fecha base del resultado de Análisis y del paso técnico por Verificación (`EXPEDIENTE_EVALUACION.fecha_evaluacion`, fecha de las transiciones `REGISTRO_RESULTADO_ANALISIS`/`ENVIO_VERIFICACION`/`APROBACION_VERIFICACION`, y fecha del documento Resolución si no hay otra mejor). |
| 27 | EJECUCIONES.MES EMISION | Ignorada | — | No se lee (derivable de la fecha anterior). |
| 28 | EJECUCIONES.PROCEDIMIENTO | Mapeada | `procedimientoEjecuciones` | Determina el **tipo real de Resolución** sintetizada (RECTIFICACION/RECONSTITUCION/CANCELACION → código específico de `TIPO_DOCUMENTO_ADJUNTO`; cualquier otro valor → código genérico `RESOLUCION`). |
| 29 | EJECUCIONES.TIPO DE ACTA | Ignorada | — | No se lee (redundante con la columna 9 del lado Registro). |
| 30 | EJECUCIONES.TIPO DE CALIFICACION | Mapeada | `resultadoAnalisisTexto` | El **resultado de Análisis** — se traduce a uno de los códigos de `TIPO_RESULTADO_EVALUACION` (PROCEDENTE/IMPROCEDENTE/PROCEDENTE_EN_PARTE/INFUNDADO/DESISTIMIENTO/EN_ABANDONO, por coincidencia de texto). Determina también qué tipo de Carta de notificación se sintetiza. Si viene vacío pero sí hay Resolución+Procedimiento+Estado (columnas 21/22, 28, 41), se aplica el camino alterno "sin calificación" (avanza igual, sin carta). |
| 31 | EJECUCIONES.ERROR MATERIAL | Ignorada (diferida) | — | No se lee — mencionada en el plan original como campo ambiguo a decidir más adelante; nunca se retomó. |
| 32 | EJECUCIONES.DESCRIPCION DE ERROR MATERIAL | Ignorada (diferida) | — | No se lee. |
| 33 | EJECUCIONES.N° RESOLUCION PRIMIGENIA | Ignorada (diferida) | — | No se lee. |
| 34 | EJECUCIONES.ERROR MATERIAL 2° SALIDA | Ignorada (diferida) | — | No se lee. |
| 35 | EJECUCIONES.DESCRIPCION | Ignorada (diferida) | — | No se lee. |
| 36 | EJECUCIONES.ABOGADO QUE EMITIÓ RESOLUCIÓN | Ignorada | — | No se lee (se usa siempre la columna 20 "ASIGNADO A ABOGADO" como responsable único de Registro→Ejecución). |
| 37 | EJECUCIONES.SUPERVISOR | Ignorada (diferida) | — | No se lee — mencionada en el plan original como campo ambiguo; el batch no asigna supervisor. |
| 38 | EJECUCIONES.FECHA DE ASIGNACION | Ignorada | — | No se lee (redundante con la columna 15 del lado Registro). |
| 39 | EJECUCIONES.ASIGNADO A: | Ignorada | — | No se lee (redundante con la columna 20; el notificador real se toma de la columna 45, "ASIGNADO A:_1"). |
| 40 | EJECUCIONES.FECHA EJECUCION | Mapeada | `fechaEjecucion` | Fecha de las transiciones `INICIO_EJECUCION`/`OBSERVACION_EJECUCION` y de la derivación a Notificación (con respaldo a la fecha de Resolución u hoy si viene vacía). |
| 41 | EJECUCIONES.ESTADO | Mapeada | `estadoEjecucionTexto` | Se clasifica en 4 grupos (`EJECUTAR`/`OBSERVAR`/`PENDIENTE`/`DETENER`) que deciden si el expediente llega a `EJECUCION/EJECUTADO`, `EJECUCION/REQUIERE_CORRECCION`, o se queda en `EJECUCION/EN_EJECUCION` sin forzar avance. |
| 42 | EJECUCIONES.N° ACTA RECONSTITUIDA | Ignorada (diferida) | — | No se lee. |
| 43 | EJECUCIONES.OBSERVACIONES | Ignorada | — | No se lee (la observación real que sí se persiste al marcar "Resolución observada" es un texto fijo genérico, no el contenido de esta columna). |
| 44 | EJECUCIONES.FECHA ASIGNACION NOTIF | Ignorada | — | No se lee (se usa la fecha de Ejecución como base de los intentos de notificación, no una fecha propia de asignación al notificador). |
| 45 | EJECUCIONES.ASIGNADO A:_1 | Mapeada | `asignadoNotificacion` | Se resuelve por nombre contra `USUARIO` (mismo mecanismo que la columna 20) → notificador responsable en `NOTIFICACION/EN_NOTIFICACION` (`id_usuario_responsable_actual`, equipo `EQ_NOTIFICACION`). |
| 46 | EJECUCIONES.MEDIO DE NOTIFICACION | **Parseada, no usada** | `medioNotificacion` | Se lee al DTO pero **ningún método del DAO la consulta hoy** — la modalidad del intento (Virtual/Presencial) se infiere sola (intento 1 = Virtual, intento 2 = Presencial si hay datos de "2° carta"), sin mirar esta columna. |
| 47 | EJECUCIONES.FECHA EMISION 1° CARTA | Mapeada | `fechaEmision1Carta` | Fecha de envío del intento 1 de notificación (`EXPEDIENTE_NOTIFICACION.fecha_envio`), con respaldo a la fecha de Resolución si viene vacía. |
| 48 | EJECUCIONES.1° NUMERO CARTA | Mapeada | `numero1Carta` | Número/código del intento 1 (`EXPEDIENTE_NOTIFICACION.codigo_notificacion`), con respaldo al número de Resolución si viene vacío. |
| 49 | EJECUCIONES.FECHA ELABORACION 2° CARTA | Mapeada | `fechaElaboracion2Carta` | Si tiene valor (junto con la col. 50), dispara un 2° intento de notificación (Presencial) y marca el intento 1 como `FALLIDA`. |
| 50 | EJECUCIONES.2° NUMERO CARTA | Mapeada | `numero2Carta` | Código del intento 2, si existe. |
| 51 | EJECUCIONES.PLAZO DE ELABORACION DE DOCUMENTACION 2 DIAS HABILES | Ignorada (diferida) | — | No se lee. |
| 52 | EJECUCIONES.NUMERO OFICIO | Ignorada (diferida) | — | No se lee. |
| 53 | EJECUCIONES.FECHA ELABORACION 2° OFICIO | Ignorada (diferida) | — | No se lee. |
| 54 | EJECUCIONES.2° NUMERO OFICIO | Ignorada (diferida) | — | No se lee. |
| 55 | EJECUCIONES.PLAZO DE ELABORACION DE DOCUMENTACION 2 DIAS HABILES2 | Ignorada (diferida) | — | No se lee. |
| 56 | EJECUCIONES.MEMORANDO | Ignorada (diferida) | — | No se lee. |
| 57 | EJECUCIONES.OBSERVACIONES. | Ignorada (diferida) | — | No se lee. |
| 58 | ESTADO CRUCE | Ignorada (verificación externa) | — | No se lee por código — se usó manualmente (fuera del batch) para confirmar que el 100% de las filas cruzó `ENCONTRADO` contra la hoja EJECUCIONES original. |

---

## Hoja "DATA PASIVO OR UNIFICADA" (752 filas)

Mismo motor de parseo/guardado que la hoja Diaria; solo cambian los índices de columna y 3 diferencias reales marcadas abajo.

| # | Encabezado real (Excel) | Estado | Campo interno (`CargaInicialFilaDTO`) | Cómo se guarda / se usa hoy |
|---|---|---|---|---|
| 0 | FECHA DE SOLICITUD- OR | **Ignorada** | — | **No se lee** — se usa la columna 1 ("FECHA DE RECEPCION-SDRERC") como fecha de solicitud/registro, no esta. |
| 1 | FECHA DE RECEPCION-SDRERC | Mapeada | `fechaSolicitud` | Mismo destino que la columna 0 de Diaria (`EXPEDIENTE.fecha_registro`, `EXPEDIENTE_SOLICITUD.fecha_recepcion`). |
| 2 | TIPO DE TRAMITE | Usada, no persistida | `tipoTramite` | Igual que Diaria (nota de historial). |
| 3 | DERIVADO POR | Usada, no persistida | `derivadoPor` | Igual que Diaria (nota de historial). |
| 4 | N° DE EXPEDIENTE | Mapeada (clave) | `numeroExpedienteLegado` | Se **preserva tal cual** como `EXPEDIENTE.numero_expediente` real (decisión ya confirmada) — a diferencia de Diaria, que siempre genera un correlativo SDRERC nuevo. Si el número ya existe activo en la base, la fila se rechaza con `ERROR` ("N° de expediente legado ya registrado"). Si viene vacío, se genera un correlativo nuevo igual que Diaria. |
| 5 | N° DE FOLIOS | Ignorada | — | No se lee. |
| 6 | ESTADO | Ignorada | — | No se lee. |
| 7 | TIPO DOCUMENTO | Mapeada | `tipoDocumento` | Igual que Diaria. |
| 8 | N° DOCUMENTO | Mapeada | `numeroDocumento` | Igual que Diaria. |
| 9 | TIPO DE ACTA | Mapeada | `tipoActa` | Igual que Diaria. |
| 10 | N° ACTA | Mapeada | `numeroActa` | Igual que Diaria. |
| 11 | TITULAR | Mapeada (clave) | `titular` | Igual que Diaria (misma persona = titular + remitente). |
| 12 | ESTADO DE RECEPCION | Ignorada | — | No se lee. |
| 13 | FECHA ASIGNADO | Mapeada | `fechaAsignado` | Igual que Diaria. |
| 14 | MES_ASIGNACION_EXPEDIENTE | Ignorada | — | No se lee. |
| 15 | N° DE DOCUMENTO - HOJA DE ENVIO | Mapeada | `numeroHojaEnvioAsignacion` | Igual que Diaria. |
| 16 | N° EXPEDIENTE DIGITAL | Mapeada (pase aparte) | `numeroExpedienteDigital` | Igual que Diaria (solo 2 filas reales de esta hoja lo traen). |
| 17 | TIPO CONTRATO | Ignorada (descartada) | — | Igual que Diaria. |
| 18 | ASIGNADO A ABOGADO: | Mapeada | `asignadoAAbogado` | Igual que Diaria. |
| 19 | N° RESOLUCION | Mapeada | `numeroResolucion` | Igual que Diaria. |
| 20 | DATA | Ignorada | — | No se lee. |
| 21 | RESOLUCION_LIMPIA | Mapeada | `resolucionLimpia` | Igual que Diaria (preferida sobre la columna 19). |
| 22 | EJECUCIONES.PRIORIDAD | Ignorada | — | No se lee. |
| 23 | EJECUCIONES.FECHA DE EMISIÓN | Mapeada | `fechaEmisionAnalisis` | Igual que Diaria. |
| 24 | EJECUCIONES.MES EMISION | Ignorada | — | No se lee. |
| 25 | EJECUCIONES.PROCEDIMIENTO | Mapeada | `procedimientoEjecuciones` | Igual que Diaria. |
| 26 | EJECUCIONES.RESOLUCIONES | Ignorada | — | No se lee. |
| 27 | EJECUCIONES.RESOLUCION_LIMPIA | **Ignorada** | — | **No se lee** — nombre de columna duplicado de la #21 dentro de esta misma hoja; se usa solo la #21. |
| 28 | EJECUCIONES.TIPO DE ACTA | Ignorada | — | No se lee. |
| 29 | EJECUCIONES.TIPO DE CALIFICACION | Mapeada | `resultadoAnalisisTexto` | Igual que Diaria. |
| 30 | EJECUCIONES.ERROR MATERIAL | Ignorada (diferida) | — | No se lee. |
| 31 | EJECUCIONES.DESCRIPCION DE ERROR MATERIAL | Ignorada (diferida) | — | No se lee. |
| 32 | EJECUCIONES.N° RESOLUCION PRIMIGENIA | Ignorada (diferida) | — | No se lee. |
| 33 | EJECUCIONES.ERROR MATERIAL 2° SALIDA | Ignorada (diferida) | — | No se lee. |
| 34 | EJECUCIONES.DESCRIPCION | Ignorada (diferida) | — | No se lee. |
| 35 | EJECUCIONES.ABOGADO QUE EMITIÓ RESOLUCIÓN | Ignorada | — | No se lee. |
| 36 | EJECUCIONES.SUPERVISOR | Ignorada (diferida) | — | No se lee. |
| 37 | EJECUCIONES.FECHA DE ASIGNACION | Ignorada | — | No se lee. |
| 38 | EJECUCIONES.ASIGNADO A: | Ignorada | — | No se lee. |
| 39 | EJECUCIONES.FECHA EJECUCION | Mapeada | `fechaEjecucion` | Igual que Diaria. |
| 40 | EJECUCIONES.ESTADO | Mapeada | `estadoEjecucionTexto` | Igual que Diaria. |
| 41 | EJECUCIONES.N° ACTA RECONSTITUIDA | Ignorada (diferida) | — | No se lee. |
| 42 | EJECUCIONES.OBSERVACIONES | Ignorada | — | No se lee. |
| 43 | EJECUCIONES.FECHA ASIGNACION NOTIF | Ignorada | — | No se lee. |
| 44 | EJECUCIONES.ASIGNADO A:_1 | Mapeada | `asignadoNotificacion` | Igual que Diaria. |
| 45 | EJECUCIONES.MEDIO DE NOTIFICACION | **Parseada, no usada** | `medioNotificacion` | Igual que Diaria (nunca consultada por el DAO). |
| 46 | EJECUCIONES.FECHA EMISION 1° CARTA | Mapeada | `fechaEmision1Carta` | Igual que Diaria. |
| 47 | EJECUCIONES.1° NUMERO CARTA | Mapeada | `numero1Carta` | Igual que Diaria. |
| 48 | EJECUCIONES.FECHA ELABORACION 2° CARTA | Mapeada | `fechaElaboracion2Carta` | Igual que Diaria. |
| 49 | EJECUCIONES.2° NUMERO CARTA | Mapeada | `numero2Carta` | Igual que Diaria. |
| 50 | EJECUCIONES.PLAZO DE ELABORACION DE DOCUMENTACION 2 DIAS HABILES | Ignorada (diferida) | — | No se lee. |
| 51 | EJECUCIONES.NUMERO OFICIO | Ignorada (diferida) | — | No se lee. |
| 52 | EJECUCIONES.FECHA ELABORACION 2° OFICIO | Ignorada (diferida) | — | No se lee. |
| 53 | EJECUCIONES.2° NUMERO OFICIO | Ignorada (diferida) | — | No se lee. |
| 54 | EJECUCIONES.PLAZO DE ELABORACION DE DOCUMENTACION 2 DIAS HABILES2 | Ignorada (diferida) | — | No se lee. |
| 55 | EJECUCIONES.MEMORANDO | Ignorada (diferida) | — | No se lee. |
| 56 | EJECUCIONES.OBSERVACIONES. | Ignorada (diferida) | — | No se lee. |
| 57 | ESTADO CRUCE | Ignorada (verificación externa) | — | Igual que Diaria. |

---

## Diferencias reales entre ambas hojas (ya confirmadas, no pendientes de revisión)

- **Numeración de expediente**: Diaria siempre genera un correlativo `SDRERC-EXP-AAAA-NNNNNN` nuevo; Pasivo OR **preserva** el "N° DE EXPEDIENTE" legado tal cual, sin generar uno nuevo salvo que venga vacío.
- **DNI del solicitante**: solo Diaria trae la columna "DNI SOLICITANTE"; Pasivo OR no tiene ese dato — su `PERSONA` se crea sin `numero_documento`.
- **Fecha base**: Diaria usa "FECHA DE SOLICITUD" (columna 0); Pasivo OR usa "FECHA DE RECEPCION-SDRERC" (columna 1, no la columna 0 "FECHA DE SOLICITUD- OR").

## Notas de diseño ya confirmadas (para que no se vuelvan a preguntar)

- **Titular = Solicitante**: ninguna de las 2 hojas trae un nombre de solicitante distinto del titular — se crea/reutiliza una única `PERSONA` y se enlaza dos veces en `EXPEDIENTE_PERSONA` (`TITULAR` y `REMITENTE`).
- **"Sin N° de acta" ya no bloquea** la creación del expediente (columna nullable en Oracle) — la fila queda `INCOMPLETO`, no `ERROR`.
- **Deduplicación de `PERSONA`**: se busca primero por `tipo_documento+numero_documento` antes de insertar una nueva (solo aplica cuando hay DNI real, Diaria).
- **Abogado sin equipo resoluble**: si "ASIGNADO A ABOGADO" no resuelve por nombre, o resuelve pero sin exactamente 1 equipo activo (salvo el combo especial Análisis+Ejecución), el expediente se crea igual pero se queda en `REGISTRO` (nunca llega a `ASIGNACION`).
