# CLAUDE.md - Contexto operativo para Claude Code en SDRERC

Este archivo resume el contexto vigente del proyecto SDRERC V2 para que Claude Code pueda trabajar con el mismo marco funcional, tecnico y operativo usado en Codex CLI.

La fuente principal sigue siendo `AGENTS.md`. Si hay contradiccion entre este archivo y `AGENTS.md`, prevalece `AGENTS.md`. Cuando una tarea cambie reglas persistentes, actualizar ambos archivos si corresponde.

## Proyecto

- Nombre: SDRERC V2.
- Ruta local: `D:\2026\FuentesRENIEC\sdrerc_CODIGOS`.
- Stack: Java 8, Swing, FlatLaf, Maven, Oracle, PowerShell 5.1 para launcher/despliegue.
- Artefacto oficial: `target/SDRERC-V2.jar`.
- Main class del JAR: `com.sdrerc.appv2.MainV2`.
- BD nueva: esquema `SDRERC_APP` en Oracle `XEPDB1`.
- Conexion V2: `SdrercAppConnection`.
- Conexion legacy intocable: `OracleConnection.java`.

## Regla de inicio para Claude Code

Antes de modificar cualquier cosa:

1. Leer `AGENTS.md`.
2. Leer este `CLAUDE.md`.
3. Revisar los archivos reales afectados por la tarea.
4. Ejecutar `git status --short --untracked-files=all`.
5. Si hay reglas funcionales nuevas o cambiadas, actualizar `AGENTS.md` y este archivo cuando aplique.

## Restricciones permanentes

- No tocar legacy salvo autorizacion explicita.
- No tocar `src/main/java/com/sdrerc/infrastructure/database/OracleConnection.java`.
- No tocar `FrmLogin.java` legacy.
- No tocar `MenuPrincipal.java` legacy.
- No tocar `com.sdrerc.Main` legacy.
- No ejecutar SQL sin autorizacion explicita.
- No modificar datos de BD sin autorizacion explicita.
- No documentar passwords reales ni credenciales.
- No crear etapa visual `VALIDACION`.
- No reactivar `Firma / Emision` como modulo lateral independiente.
- No crear rutas, estados, tablas o acciones no definidas.
- No hardcodear IDs de catalogo.
- No poner SQL en JPanel.
- UI llama Service; Service llama DAO.
- Mantener Java 8 + Swing + FlatLaf.
- No usar `git add .`.
- No commitear secretos reales (`db.password`, `security.totp.key`) en `config/sdrerc-app.properties`; revisar el diff de ese archivo antes de cada commit aunque ya este trackeado en el repo.

## Arquitectura tecnica

Paquetes principales:

- `src/main/java/com/sdrerc/appv2`: arranque V2.
- `src/main/java/com/sdrerc/ui/appv2`: shell, menu, componentes y tema V2.
- `src/main/java/com/sdrerc/ui/views`: pantallas V2 y algunas vistas legacy que no deben tocarse.
- `src/main/java/com/sdrerc/application/sdrercapp`: Services de negocio V2.
- `src/main/java/com/sdrerc/domain/dto/sdrercapp`: DTOs V2.
- `src/main/java/com/sdrerc/infrastructure/sdrercapp/dao`: DAOs V2.
- `db/sdrerc_app/scripts`: scripts incrementales de BD. Crear scripts solo cuando la tarea lo autorice y no ejecutarlos sin instruccion separada.

Dependencias relevantes:

- Oracle JDBC `ojdbc8`.
- FlatLaf.
- Apache POI para Excel.
- JCalendar.
- Log4j2.
- BCrypt.
- ZXing (`core` + `javase`): generacion de codigo QR para enrolamiento TOTP del login V2.
- HikariCP: pool de conexiones JDBC hacia SDRERC_APP. `SdrercAppConnection.getConnection()` ya no usa `DriverManager` directo (una conexion fisica nueva por cada consulta no escala con varios usuarios conectados a la vez); ningun DAO cambio, la firma publica es identica.
- Jakarta Mail: envio del codigo OTP del 2FA por correo (login V2).

## Documentacion funcional clave

Leer segun corresponda:

- `AGENTS.md`: reglas persistentes y fuente principal.
- `docs/arquitectura_app/detalle_funciones.md`: detalle funcional de Recepcion, Asignacion, Analisis, Verificacion, Ejecucion y Notificacion.
- `docs/arquitectura_app/DIAGNOSTICO_MAESTRO_BRECHAS_HASTA_NOTIFICACION.md`: brechas iniciales y orden incremental.
- `docs/arquitectura_app/PLAN_PROMPTS_INCREMENTALES_HASTA_NOTIFICACION.md`: prompts historicos del plan.
- `docs/arquitectura_bd/Acta_Reunión_011-2026-DRC.md`.
- `docs/arquitectura_bd/Acta_Reunión_012-2026-DRC.md`.
- `docs/arquitectura_bd/Acta_Reunión_013-2026-DRC.md`.

Interpretacion de actas:

- Leerlas cronologicamente.
- Ante contradiccion, el acuerdo posterior reemplaza al anterior.
- El Acta 013 del 22/05/2026 consolida el flujo operativo revisado.

## Modulos vigentes SDRERC V2

Modulos en uso o incorporados:

- Inicio.
- Bandeja de Expedientes.
- Registro / Recepcion.
- Asignacion.
- Analisis.
- Verificacion.
- Firma / Emision integrada dentro de Verificacion, sin menu lateral independiente.
- Ejecucion.
- Notificacion.
- Publicacion.
- Expediente digital.
- Administracion / Usuarios.
- Administracion / Equipo Juridico.
- Administracion / Roles.
- Administracion / Feriados.
- Administracion / Plazos.
- Administracion / Plantillas de documento.

Reglas de menu:

- `MenuPrincipalV2` integra los modulos V2.
- No usar el menu legacy para nuevas entradas.
- `Cierre` se maneja como pestana interna de Notificacion, no como modulo lateral independiente.
- No mostrar sufijo tecnico `V2` al usuario final.

## Login y autenticacion (V2)

Estado: implementado y en uso. `MainV2` ya no abre `MenuPrincipalV2` directamente; primero abre `LoginFrameV2` (`com.sdrerc.ui.appv2.login`), y solo tras autenticar con exito construye el menu.

Reglas vigentes:

- Autentica contra la tabla `USUARIO` de `SDRERC_APP`, no contra la legacy `APP_USERS`. No confundir con `FrmLogin`/`LoginService`/`UserService` legacy, que siguen aislados y sin tocar.
- Doble factor obligatorio para todos los usuarios, sin excepcion de rol.
- Flujo: credenciales -> cambio de contrasena obligatorio si `debe_cambiar_password=1` -> segundo factor -> sesion. El segundo factor es **correo electronico (primera opcion)** si el usuario tiene `USUARIO.correo` cargado (codigo de 6 digitos enviado por SMTP, valido 10 minutos, sin ceremonia de enrolamiento previa), con **authenticator TOTP como alternativa** (enlace "Prefiero usar una app autenticadora" desde el paso de correo); si el usuario no tiene correo cargado, cae automaticamente a TOTP (enrolamiento la primera vez, verificacion despues), igual que antes de agregar el correo. Un usuario puede tener ambos metodos disponibles y cambiar entre ellos en el momento del login, sin re-enrolar.
- TOTP implementado con RFC 6238 sobre JDK puro (sin libreria externa de TOTP); enrolamiento con codigo QR generado con ZXing y clave manual como respaldo.
- 2FA por correo: tabla `USUARIO_EMAIL_OTP` (codigo hasheado con BCrypt, un solo uso, expira a los 10 minutos; script `82_login_2fa_correo.sql`, **ya ejecutado**). Envio via Jakarta Mail (SMTP STARTTLS); config en `mail.smtp.host`/`port`/`user`/`password`/`from` de `config/sdrerc-app.properties` (o variables de entorno `SDRERC_APP_SMTP_*`), hoy con un SMTP externo (ej. Gmail con clave de aplicacion) como solucion interina mientras se define un SMTP institucional dentro de la LAN del cliente; sin credenciales reales en el repo. Falta completar `mail.smtp.user`/`mail.smtp.password` para poder enviar correos reales. Clases: `EmailOtpMailer` (`infrastructure.security`), `UsuarioEmailOtpDAO`, `PasoEmailVerificarPanel` (UI).
- Al confirmar el enrolamiento se generan 8 codigos de respaldo de un solo uso (formato `XXXX-XXXX`, hasheados con BCrypt en `USUARIO_TOTP_BACKUP_CODE`); se muestran una unica vez y no se vuelven a exponer.
- Bloqueo temporal (5 intentos fallidos, 15 minutos) compartido entre fallos de contrasena y de codigo TOTP/respaldo, para evitar fuerza bruta sobre un codigo de 6 digitos.
- La primera contrasena la asigna el administrador desde Administracion > Usuarios > `Restablecer clave` (ya habilitado); no existe flujo de autoservicio para reclamar cuenta sin contrasena.
- `Restablecer clave` permite ademas marcar "reiniciar verificacion en dos pasos" cuando el usuario perdio su dispositivo autenticador (limpia `totp_secret`/`totp_habilitado`, fuerza nuevo enrolamiento).
- El secreto TOTP se cifra (AES-GCM) antes de persistirse en `USUARIO.TOTP_SECRET`; nunca se guarda en claro. La clave de cifrado se resuelve por `security.totp.key` en `config/sdrerc-app.properties` o variable de entorno `SDRERC_APP_TOTP_KEY`, nunca hardcodeada en el fuente.
- Clases clave: `LoginFrameV2` + `PasoCambioPasswordPanel`/`PasoTotpEnrolarPanel`/`PasoTotpVerificarPanel` (UI), `AutenticacionService` (orquestacion), `TotpService`/`TotpSecretCipher` (`infrastructure.security`), `UsuarioDAO` (metodos de autenticacion/bloqueo/roles).
- Mensajes de error de login deliberadamente genericos; nunca revelar si un username existe o no.
- Esquema: columnas nuevas en `USUARIO` (`debe_cambiar_password`, `totp_secret`, `totp_habilitado`, `totp_confirmado_en`, `intentos_fallidos`, `bloqueado_hasta`, `password_actualizado_en`, `ultimo_login_en`) y tabla `USUARIO_TOTP_BACKUP_CODE`, agregadas por `61_login_2fa_usuario.sql` (ya ejecutado).
- Bootstrap del primer superadministrador via `62_reset_datos_prueba_y_superadmin.sql` (ya ejecutado); utilidad `com.sdrerc.tools.PasswordHashCli` genera el hash BCrypt localmente para no escribir contrasenas en texto plano en scripts versionados.

## Flujo funcional principal

Flujo macro:

`Registro -> Asignacion -> Analisis -> Verificacion -> Ejecucion / Notificacion -> Publicacion condicional -> Cierre / Expediente digital`

Flujo consolidado de documentos:

1. `Analisis`: abogado recibe expediente y proyecta documentos.
2. `Verificacion`: supervisor valida, observa o firma/emite documento.
3. Despues de Verificacion:
   - Resoluciones pasan a `Ejecucion`.
   - Oficios, cartas intermedias u otros documentos firmados que no requieren ejecucion pasan a `Notificacion`, solo con transicion real.
4. `Ejecucion`:
   - Mismo abogado de Analisis atiende resoluciones.
   - Procedente / procedente en parte: anotacion textual + carta de notificacion.
   - Improcedente: carta de notificacion.
5. `Notificacion`: valida documento/carta, registra intentos, acuse/cargo y resultado.
6. `Publicacion`: solo metadata/trazabilidad y transiciones reales; no publicar externamente.

Cartas intermedias:

- Las genera el abogado en Analisis.
- Las firma el supervisor de Analisis.
- Pasan a Notificacion.
- No pasan por Ejecucion.
- No deben ser firmadas por la supervisora de Notificacion como carta final.
- El expediente puede quedar pendiente de respuesta si el flujo lo soporta.

Pendientes funcionales:

- ORE.
- Culminacion en linea.
- Otros casos especiales.
- No implementar cierre automatico para esos casos hasta confirmar responsable, modulo y regla.

## Registro / Recepcion

Pestanas superiores:

- `Bandeja Registro`.
- `Carga diaria`.
- `Registro manual`.

Reglas principales:

- Carga diaria y registro manual detectan duplicidad solo por `numero de acta + titular completo`.
- Duplicados se registran para trazabilidad, pero no generan numero de expediente hasta resolverse (asociacion confirmada).
- La asociacion de duplicados puede resolverse desde Registro (lengueta "Asociar duplicados" en el panel de Registro) o desde Asignacion (lengueta "Asociar"); ambas reutilizan el mismo servicio de asociacion (`ExpedienteRelacionadoService`/`ExpedienteRelacionadoDeteccionService`).
- Reconsideracion y Apelacion se registran sin numero; se puede asociar a un expediente principal o generar numero manualmente. La generacion manual de numero (bloque "Decision de numero", visible para cualquier solicitud en etapa REGISTRO/estado REGISTRADO sin numero, sin restriccion de tipo de procedimiento) esta disponible tanto desde Registro (panel "Asociar duplicados") como desde Asignacion (panel "Asociar"); ambas llaman `AsignacionExpedienteService.generarNumeroExpediente`, que exige que el expediente siga en etapa REGISTRO/estado REGISTRADO. Antes solo aplicaba a Reconsideracion/Apelacion (validado tambien en `AsignacionExpedienteDAO.generarNumeroExpediente`); se amplio a cualquier procedimiento por pedido explicito del usuario (04/08/2026), ya que cualquier solicitud puede quedar sin numero mientras esta pendiente de asociar como potencial duplicado, no solo Reconsideracion/Apelacion.
- El panel "Asociar duplicados" de Registro replica el diseno del panel "Asociar" de Asignacion: seccion "Seleccion y alertas" (estado + tabla de coincidencias por acta/titular), "Asociacion rapida" (un clic asocia todas las coincidencias detectadas, sin marcar candidato por candidato) y "Decision de numero". El flujo manual con checkboxes ("Asociar seleccionados") se mantiene ademas de "Asociacion rapida", no se elimino.
- La grilla principal de Registro (al igual que Asignacion/Ejecucion/Notificacion) pinta una franja de color a la izquierda de cada fila asociada que rota por grupo (paleta de 10 colores segun `id_expediente` principal), no un color unico fijo.
- Numero SDRERC visible: `SDRERC-EXP-YYYY-000001`.
- No usar `id_expediente` como correlativo visible.
- Fecha visible en UI: `dd/MM/yyyy`.
- `Nro. tramite web` no es obligatorio.
- Si canal es `Mesa de partes virtual`, habilitar `Nro. tramite web`.
- Si canal no es `Mesa de partes virtual`, bloquear y mostrar `SIN TRAMITE`.
- `N° expediente SGD` vive en el bloque `Datos del expediente`.
- `N° expediente SGD` y `Tipo de acta` son obligatorios en Registro manual, Edicion manual y Carga diaria.
- `Tipo documento` de solicitud debe normalizar equivalencias con y sin tilde.

KPIs vigentes en Bandeja Registro:

- `Potencial duplicado`.
- `Posible Grupo Familiar`.

Reglas KPI:

- KPIs se calculan segun filtros activos.
- Clic en KPI filtra por esa alerta.
- Clic en `Buscar` limpia filtro KPI y vuelve al total por filtros de busqueda.

Alertas:

- En bandeja, columna `Alertas` de una fila principal (no asociada) muestra:
  - `Sin Alerta`.
  - `Potencial duplicado`.
  - `Posible Grupo Familiar`.
- Una fila asociada (hijo, expandida con `+` bajo su principal) muestra ademas `Duplicado confirmado` cuando ya fue resuelta como duplicado (asociada via `EXPEDIENTE_RELACION`) y no tiene otra alerta propia activa (potencial duplicado/posible grupo familiar/con observaciones) pendiente; esa fila nunca vuelve a `Sin Alerta` porque, por definicion, toda fila que aparece en esa lista ya fue confirmada como duplicado de su principal. `Generar numero de expediente` (decision de que NO era duplicado) si limpia el flag `EXPEDIENTE_SOLICITUD.POTENCIAL_DUPLICADO` de esa fila y la deja en `Sin Alerta` como fila principal independiente, sin pasar por `Duplicado confirmado` (esa etiqueta es exclusiva de filas asociadas).
- Observaciones extensas y datos incompletos pertenecen a previsualizacion de carga diaria/exportacion.
- Alertas/incidencias persistentes se guardan en `EXPEDIENTE_ALERTA`.
- Grupo familiar Fase 1 (flag simple) se marca en `EXPEDIENTE_SOLICITUD`. Fase 2 (vigente) agrega un ID de grupo familiar real: tabla `GRUPO_FAMILIAR` + `PERSONA.id_grupo_familiar` (vinculo a nivel persona, no por expediente, para heredar el grupo en expedientes futuros del mismo titular). `GrupoFamiliarDAO`/`GrupoFamiliarService` detectan candidatos por apellidos del titular y asocian sin heredar numero/equipo/abogado (no reutiliza `EXPEDIENTE_RELACION`). Al asociar, se marca atendida la alerta `Posible Grupo Familiar` del expediente y los candidatos ya agrupados (propio o ajeno grupo) dejan de listarse como "posible integrante". La grilla de solo lectura "Grupo familiar actual" tiene ademas un icono "x" en la primera columna (`AppV2RemoveActionButton`) para retirar a una persona del grupo (`GrupoFamiliarDAO.eliminarDeGrupoFamiliar`, limpia `PERSONA.id_grupo_familiar` y revierte el flag de `EXPEDIENTE_SOLICITUD`).

Carga diaria:

- Usa plantilla Excel oficial.
- Validacion se concentra en `CargaDiariaReglasService` o equivalente.
- Validacion calcula preview, numero, alertas y observaciones.
- Confirmacion solo persiste el preview validado; no debe recalcular reglas.
- Se permite editar celdas en previsualizacion sin desactivar confirmar.
- `Validar carga` no debe quedar inutilizado luego de validar.
- Exportar previsualizacion debe incluir observaciones concatenadas.

Panel derecho Registro:

- Se abre con doble clic, no con clic simple.
- Titulo: `Panel de datos` (generico, igual en todos los modulos; no debe decir el nombre del modulo).
- Debajo del titulo mostrar titular en azul.
- Informativo, sin botones.
- Bloques: `Datos del plazo`, `Datos del expediente`, `Datos del acta`, `Datos de solicitud`, `Datos del titular`, `Datos del solicitante`, `Datos de Notificacion y Ubicacion`.
- `Datos del plazo` muestra `Dias` como pill y `Fecha Vencimiento`.
- `Registrar G.F` es lengueta/panel contextual por seleccion de casillas, no pestana superior. Su panel "Grupo Familiar" (`JPanelRegistrarGrupoFamiliarV2`) replica el patron de "Asociar duplicados": grilla de candidatos por apellidos con checkbox + boton "Asociar al grupo familiar" + grilla de solo lectura "Grupo familiar actual", ambas con filtro por columna (`AppV2ColumnFilterSupport`), sin scroll vertical (altura ajustada a las filas reales) y con scroll horizontal cuando el panel es angosto. Al asociar, el panel permanece abierto y reselecciona el mismo expediente (no se cierra solo).

## Asignacion

Pestanas superiores:

- `Bandeja Asignacion`.
- `Cartas de respuesta`.
- `Carga Abogados`.

KPIs principales:

- `Pendientes`.
- `Potencial duplicado`.
- `Posible Grupo Familiar`.
- `Por vencer`.
- `Vencidos`.

Reglas de bandeja:

- Filtros deben replicar diseno/tamanos/posiciones de Registro, manteniendo estados propios de Asignacion.
- No mostrar columna `Solicitante` en listado principal.
- Mostrar columna `Alertas` con la misma semantica que Registro.
- Filas asociadas se despliegan con icono `+`, icono documental, franja/acento izquierdo, fondo celeste suave y texto atenuado.
- Asociados no deben aparecer como principales independientes.
- La columna se llama `Abogado actual` (no `Abogado asignado`) y muestra a quien tiene el expediente hoy segun su etapa/estado, no solo al abogado de Analisis. Se resuelve desde `EXPEDIENTE.id_usuario_responsable_actual` (con fallback a la asignacion activa en `EXPEDIENTE_ASIGNACION` si es nulo). `DocumentoAnalisisDAO.asignarNotificacionMultiple`/`reasignarNotificacion` actualizan ese campo al validador/notificador cuando Notificacion asigna o reasigna un documento; `registrarResultadoValidacion` lo limpia (vuelve a NULL) al marcar `Observado`, para que el fallback vuelva a mostrar al abogado de Analisis/Ejecucion. Esta bandeja no filtra por etapa, por lo que puede listar expedientes ya avanzados a Analisis/Verificacion/Ejecucion/Notificacion.

Carga Abogados:

- El conteo es siempre por abogado, nunca por equipo: un abogado que pertenece a 2+ equipos activos debe aparecer en una sola fila (no una fila duplicada por cada equipo). `UsuarioAsignacionDAO.listarCargaLaboralAbogados` resuelve el supervisor como subconsulta escalar (MAX), no como JOIN en la tabla conductora.
- Grilla final: `Abogado` + 4 subcolumnas de la carga de Análisis: `Por recibir`, `En análisis`, `Observado`, `Carta intermedia` (mutuamente excluyentes, suman el total de Análisis del abogado). `Verificación` y `Ejecución` YA NO son columnas de la grilla (sus conteos se conservan internamente para `getCargaTotal()`/KPIs y se ven fila por fila en el panel lateral de detalle, ver más abajo).
- Definición exacta de las 4 subcolumnas (importante: `etapa=ASIGNACION/estado=ASIGNADO` cuenta como carga de Análisis aunque el expediente técnicamente no haya llegado aún a `etapa=ANALISIS`; antes de esto esas solicitudes no se contaban en ningún lado):
  - **Por recibir**: `etapa=ASIGNACION AND estado=ASIGNADO`. Asignado desde Asignación, el abogado de Análisis todavía no hace clic en "Recibir expediente" (esa acción es la que recién mueve el expediente a `etapa=ANALISIS/estado=RECIBIDO_POR_ABOGADO`).
  - **Observado**: `etapa=ANALISIS AND estado=OBSERVADO`. Regresa desde Verificación o desde el supervisor de Notificación con resultado Observado. Tiene prioridad sobre "Carta intermedia" (un expediente Observado nunca cuenta como Carta intermedia).
  - **Carta intermedia**: `etapa=ANALISIS`, estado distinto de OBSERVADO, y tiene un documento analizado activo de clasificación `INTERMEDIO` con `requiere_respuesta=1`, `notificado=1` y `confirmacion_respuesta` ya registrada — es decir, ya fue derivado de vuelta desde la bandeja "Cartas de respuesta" de Asignación hacia el equipo de Análisis para que el abogado actúe sobre la respuesta.
  - **En análisis**: `etapa=ANALISIS`, estado distinto de OBSERVADO, sin esa carta intermedia ya respondida (el resto: recién recibido, subsanado, atendido, etc.).
- `Supervisor`, `Por vencer` y `Vencidos` no son columnas de la grilla: `Supervisor` es filtro (combo) del panel de búsqueda propio de esta pestaña; `Por vencer`/`Vencidos` son KPIs clicables (`MetricCardV2`).
- Columnas eliminadas previamente (no vuelven): `Equipo` (un abogado puede estar en varios equipos; no aporta al conteo por abogado) y `Asignadas`/`Documentos (Cartas/Pedidos)` (conteos genericos o de documentos, reemplazados por el desglose de expedientes de Análisis).
- Sin encabezado agrupado visual "ANÁLISIS" arriba de las 4 subcolumnas: la grilla usa `AppV2ColumnFilterSupport` (filtros por columna, patrón estándar de toda la app), cuyo encabezado tiene una altura fija de 30px compartida por todas las grillas de la app; un encabezado de 2 líneas no cabe ahí sin agrandar ese componente compartido. Las 4 subcolumnas llevan encabezado de una sola línea.
- Panel de búsqueda propio (arriba de la grilla, mismo diseño de 3 filas que Registro/Asignación): texto libre (nombre de abogado), `Fecha desde`/`Fecha hasta` (sobre `EXPEDIENTE.fecha_vencimiento` de los expedientes de carga del abogado, no una fecha propia del agregado), combo `Abogado`, combo `Supervisor` (ambos poblados con los valores ya cargados, sin consulta aparte), límite numérico. El filtro de fechas resuelve primero el `Set` de ids de abogado con al menos un expediente vencido en el rango (`UsuarioAsignacionService.listarIdsUsuarioConVencimientoEnRango`) y luego intersecta en memoria; el resto de los filtros son en memoria directo sobre la carga ya cargada.
- KPIs de esta bandeja: `Abogados`, `Con carga`, `Sin carga`, `Solicitudes` (los 4 originales) más `Por vencer` y `Vencidos` (clicables, filtran la grilla por `getPorVencer() > 0` / `getVencidos() > 0` del abogado). `Con carga`/`Sin carga` filtran exclusivamente por si el abogado tiene algo en las 4 subcolumnas de Análisis (`getEnAnalisis() > 0` / `== 0`), NO por la carga total del abogado; un abogado con carga solo en Verificación/Ejecución (y nada en Análisis) cuenta como "Sin carga" para este KPI, aunque siga apareciendo en la grilla con `0` en sus 4 columnas (nunca se oculta a nadie de la grilla). `Solicitudes` es la excepción: sigue sumando la carga total (Análisis+Verificación+Ejecución), como medida general de carga del abogado.
- Panel lateral de detalle: doble clic en una fila de abogado abre un panel lateral propio de esta pestaña (independiente del panel lateral compartido de `Bandeja Asignación`/`Cartas de respuesta`), título "Detalle de carga", con el listado de expedientes de ese abogado (columnas `N° Expediente`, `Etapa`, `Estado`, `Días`); `Estado` usa el mismo pill que las demás bandejas (`StatusBadgeV2.forEstado`) y `Días` el mismo pill verde/amarillo/rojo/vencido que la columna `Días` de otras bandejas (`StatusBadgeV2.forDias`), para que vencidos y por vencer se distingan visualmente sin columnas booleanas aparte. Este panel (y el filtro de fechas del buscador) muestran EXCLUSIVAMENTE la carga de Análisis (los mismos 4 buckets de la grilla: Por recibir/En análisis/Observado/Carta intermedia); NO incluye expedientes en Verificación ni Ejecución, para que lo que se ve al hacer doble clic siempre sea coherente con lo que ya muestran las 4 columnas visibles.

Asociacion:

- Confirmar relacion solo por coincidencia normalizada `numero de acta + titular`, salvo reglas futuras explicitas.
- Principal canonico: primero expediente con numero SDRERC; si ambos tienen o ninguno tiene numero, el mas antiguo.
- Duplicado asociado hereda numero de expediente, fecha de vencimiento, equipo/abogado cuando corresponda.
- Al resolver duplicidad, desactivar/atender alerta `Potencial duplicado` en BD.
- En bandejas jerarquicas, asociado muestra su propia alerta funcional cuando corresponda.
- UI no debe usar `padre` ni `hijo`; usar `expediente principal`, `expediente asociado` o `relacion confirmada`.

Panel derecho Asignacion:

- Lenguetas: `Datos`, `Asignacion`, `Asociar`, `Grupo Familiar`.
- `Grupo Familiar` sigue el mismo patron que el panel homonimo de Registro (candidatos por apellidos con checkbox + "Asociar al grupo familiar" + grupo familiar actual de solo lectura), con el bloque "Asociacion" mostrando "N° expediente / Nombres del Titular" del expediente en foco. Permanece abierto y reselecciona el expediente tras asociar.
- La primera seleccion activa lengueta; segundo clic puede expandir/restaurar segun patron vigente.
- `Datos` reutiliza contenido/estilo de Registro con titulo `Panel de datos` (generico, no `Panel de Asignacion`) y titular debajo.
- `Asignacion` conserva siempre bloque `Asignacion de abogado` para uno o varios expedientes.
- Accion principal: `Generar asignacion`.
- No usar popup si la captura ya esta en el panel.
- La grilla de asignacion debe listar todos los expedientes seleccionados y permitir hoja de envio por expediente.
- Hoja de envio se persiste en `EXPEDIENTE_ASIGNACION.NUMERO_HOJA_ENVIO` y debe ser unica. Un valor vacio o solo un guion (`-`) se considera hoja de envio no ingresada.
- La reasignacion a otro abogado/equipo se realiza dentro de la misma lengueta `Asignacion`, no en `Asociar`: por defecto la casilla de seleccion del listado queda bloqueada para expedientes ya asignados (igual que antes de existir la reasignacion). Un checkbox `Habilitar reasignacion` en el bloque `Asignacion de abogado` habilita esa casilla tambien para expedientes ya asignados, permitiendo marcarlos junto con expedientes nuevos y reasignarlos con el mismo boton `Generar asignacion`; el checkbox se desactiva automaticamente al terminar la accion, para que el listado vuelva a bloquear por defecto los expedientes asignados (incluyendo los recien asignados/reasignados). La grilla separa `Hoja de envio nueva` (editable, vacia por defecto en reasignacion) de `Hoja de envio actual` (solo lectura, referencia). `Asignacion` incluye el bloque de historial de asignaciones/reasignaciones del expediente con foco (Tipo, Abogado, Equipo, Hoja de envio, Fecha, Asignado por, Estado); `Asociar` ya no lo muestra.

Cartas de respuesta:

- Vista propia dentro de Asignacion.
- Lista documentos analizados que requieren respuesta.
- Debe incluir numero de expediente en primera columna.
- No crear etapa `Asignacion de respuesta`.

## Analisis

Reglas vigentes:

- El modulo vuelve a manejar un unico analisis operativo por expediente.
- No reintroducir multiples bloques de analisis salvo requerimiento explicito y script aprobado.
- El abogado recibe expediente asignado.
- Recibir documentos asociados aplica segun reglas de responsable y estado.
- Documentos analizados y resultado final son responsabilidades separadas.
- Guardar documentos no exige resultado/fundamento final.
- Guardar documentos no mueve el expediente a Verificacion.
- Enviar a Verificacion sigue siendo accion explicita.
- Guardar o eliminar un documento analizado (icono de diskette/eliminar en la grilla) refresca solo esa grilla y la lectura de Publicacion prevista; no debe resetear los combos/checks/fundamento del bloque "Resultado del analisis".
- Recibir un expediente (desde la lengueta o el boton "Recibir expediente") refresca la grilla y reselecciona el mismo expediente; si el panel derecho estaba abierto antes de recibir, debe seguir abierto despues (no cerrarse solo). `JPanelAnalisisV2.buscar(Long)` reabre el panel explicitamente cuando la reseleccion tiene exito y estaba abierto antes de refrescar; `confirmarYEjecutar(...)` (usado por varias acciones, no solo recibir) pasa el expediente previamente seleccionado para que se reintente esa reseleccion.
- En la grilla de documentos analizados (Analisis y Ejecucion), la `Fecha Emision` de un documento nuevo empieza en blanco, no con la fecha de hoy. Solo se autocompleta con la fecha actual cuando el combo `Estado` de esa fila pasa a `Emitido`; si se elige cualquier otro estado, la fecha se limpia. Sigue siendo editable a mano en cualquier momento.
- "Registrar resultado final" valida (resultado seleccionado, documentos, etc.) antes de mostrar el dialogo de confirmacion "¿Desea continuar?"; si falta algo, se avisa de inmediato con un mensaje claro (`AnalisisExpedienteService.validarRegistroAnalisis(...)`) sin pedir confirmar una accion que de entrada no puede completarse.

Documentos analizados:

- Pueden organizarse en jerarquia de maximo dos niveles:
  - documento principal;
  - documento relacionado/respuesta.
- Usar `EXPEDIENTE_DOCUMENTO_ANALIZADO`.
- `ID_DOCUMENTO_PADRE` nulo indica documento principal.
- No permitir nietos.
- No eliminar fisicamente; usar baja logica con `ACTIVO=0`.
- Respuestas no son solicitudes ni expedientes nuevos.

Columnas funcionales de la version de analisis unico:

- `Tipo`.
- `N° Documento`.
- `Estado`.
- `Fecha Emision`.
- `Descripcion` (etiqueta UI: `Comentario`).
- `¿Requiere respuesta?`.
- `Confirmacion de respuesta`.
- `Fecha Respuesta`.
- `Fecha Publicacion`.
- `Hoja de Envio`.

Nota: `Detalle Obs.`, `Fecha Acuse` y `Notificado` se mantienen como datos del modelo/DTO pero ya no se muestran como columna en la grilla vigente de Analisis.

Estado de documentos:

- Opciones alineadas con Verificacion:
  - `En proyecto`.
  - `En despacho`.
  - `Emitido`.
  - `Observado`.

Plantillas:

- Plantillas Word viven bajo `docs/plantillas` (archivo) o en `PLANTILLA_DOCUMENTO` (BLOB en Oracle, administradas desde Administracion > Plantillas de documento). Prioridad de resolucion en `AnalisisPlantillaDocumentoService.generarDocumento`: si el tipo de documento tiene una version `activo=1` en `PLANTILLA_DOCUMENTO`, esa BLOB gana siempre; si no existe fila activa (o la consulta falla por cualquier motivo), se usa el archivo en `docs/plantillas` por coincidencia aproximada de nombre (`resolverPlantilla`), igual que antes de este modulo.
- La descarga/relleno de plantilla debe reemplazar variables como titular, DNI, solicitante, acta, etc., con datos de la solicitud.
- Las variables usan el formato `#nomVariable#` (camelCase) dentro del Word; el listado completo por plantilla vive en `docs/arquitectura_app/variables_plantillas_word.md`.
- Al generar una RESOLUCION, la plantilla puede autocompletar `#numDocInforme#`/`#fechaDocInforme#` con los datos del documento analizado tipo `INFORME` mas reciente (activo, mayor `fecha_documento`) del mismo expediente; si no existe informe, esas variables quedan vacias. La logica vive en `AnalisisPlantillaDocumentoService`, no en el JPanel.
- Clasificacion de negocio (`TIPO_DOCUMENTO_ADJUNTO.CLASIFICACION`): cartas finales y resoluciones -> `FINAL`; cartas intermedias y oficios -> `INTERMEDIO`; informes -> sin clasificar (`NULL`). Detalle completo en `docs/arquitectura_app/variables_plantillas_word.md`.
- Condicionales dentro del propio Word: marcadores `[[SI_<variable>:valor1|valor2]] ... [[FIN_SI]]` (parrafos aparte), evaluados por `AnalisisPlantillaDocumentoService.aplicarCondicionales` contra el mismo mapa que resuelve los `#variable#` (nombre de variable insensible a mayusculas/guiones bajos, valores insensibles a mayusculas/tildes). Pendiente (no hecho todavia): aplicar marcadores reales a `informe_rectificacion.docx`/`resolucion_rectificacion.docx`, que hoy solo tienen texto instructivo en lugar de marcadores funcionales. Alternativa recomendada para no editar el Word a mano: bloques de contenido (ver mas abajo).

Administracion de plantillas de documento (modulo nuevo, implementado):

- Tabla `PLANTILLA_DOCUMENTO` (script `75_plantilla_documento.sql`, ya ejecutado): versiona el contenido `.docx` como BLOB por `id_tipo_documento_adjunto`; solo una version `activo=1` por tipo a la vez (regla en Java, no en BD); nunca se borra fisicamente, solo se desactiva al activar otra version.
- Adopcion gradual: mientras un tipo de documento no tenga fila activa, sigue usando el archivo en `docs/plantillas` sin cambios. Ningun `.docx` existente fue migrado/cargado a la tabla.
- Clases: `PlantillaDocumentoDTO`, `PlantillaDocumentoDAO`, `PlantillaDocumentoService` (valida `.docx` y tamano maximo 15 MB), panel `JPanelPlantillasDocumentoV2` (Administracion > Plantillas de documento, permiso `MENU_ADMIN_PLANTILLAS`).
- El modulo permite cargar nueva version (sube archivo, versiona automaticamente), descargar la version vigente, ver historial de versiones y activar una version anterior (rollback), y consultar la lista de variables/sintaxis de condicionales disponibles.
- Se guarda como BLOB en Oracle (no como archivo en el file share) porque el despliegue vigente copia la app localmente a cada cliente (`C:\SDRERC_CLIENTE\app`); un archivo subido solo quedaria en la maquina de quien lo sube hasta el proximo release. Oracle es el unico recurso compartido en vivo por todos los clientes de la LAN.

Bloques de contenido (evolucion del administrador de plantillas, implementado):

- Tabla `PLANTILLA_BLOQUE` (script `76_plantilla_bloque.sql`, ya ejecutado): lista ordenada de bloques (titulo + contenido + condicion opcional) por `id_tipo_documento_adjunto`, independiente de la version de la plantilla base. Condicion opcional armada con `variable_condicion`/`operador_condicion` (`COINCIDE`/`NO_COINCIDE`)/`valores_condicion` (lista separada por `|`), reusa el mismo motor de comparacion que los marcadores `[[SI_...]]`.
- Requiere que la plantilla base tenga un parrafo con el texto exacto `[[CONTENIDO]]` (marcador sin nombre) o `[[CONTENIDO:seccion]]` (marcador nombrado, columna `PLANTILLA_BLOQUE.SECCION`, script `77_plantilla_bloque_seccion.sql` ya ejecutado): ahi se insertan, en orden, los bloques de esa seccion cuya condicion se cumple. Una plantilla puede tener varios marcadores nombrados distintos (ej. `antecedentes`/`recomendaciones`) para documentos con mas de un punto de contenido dinamico. Sin el marcador correspondiente, esos bloques no se insertan (no rompe nada).
- UI: dialogo `DlgBloquesPlantillaV2` (boton "Administrar bloques de contenido" en `JPanelPlantillasDocumentoV2`) con lista de bloques + formulario (seccion opcional, titulo, contenido, condicion via combos) + reordenar (subir/bajar).
- Variable `resAnalisis` (resultado del analisis) esta disponible en el mapa de sustitucion junto con las demas `#variable#` (`AnalisisPlantillaDocumentoService.valores(...)`, viene de `expediente.getUltimoResultadoAnalisis()`).
- Nota tecnica importante para mantenimiento futuro: `XWPFDocument.insertNewParagraph` (API oficial de Apache POI 5.2.5 para insertar parrafos en medio de un documento) tiene un `ClassCastException` reproducible con documentos reales de Word en esta version; se evita insertando los parrafos nuevos con `createParagraph()` (al final) y reubicandolos con `XmlCursor.moveXml`, seguido de un round-trip de serializar+reabrir el documento antes de continuar (necesario porque `moveXml` desincroniza la lista de parrafos que POI cachea en memoria). Ver detalle completo en AGENTS.md.

Documentos/solicitudes asociadas (duplicados) en la Bandeja Analisis:

- Doble clic en una fila asociada/duplicada carga el "Panel de datos" con los campos que trae `ExpedienteRelacionadoDTO` (N° expediente, SGD, tramite web, acta, N° documento, solicitante, estado, dias/vencimiento, equipo); campos que ese DTO no trae (contacto/ubigeo del solicitante, canal de ingreso, prioridad, etc.) quedan en "-", no se inventan ni se copian de otro expediente.
- La mini-grilla de documentos asociados dentro del "Panel de datos" tiene una columna de accion "Recibir" (icono `AppV2ReceiveActionButton`, via `RecibirAsociadoRenderer`/`RecibirAsociadoEditor`), habilitada solo cuando el documento asociado esta en `ASIGNACION/ASIGNADO` (misma transicion de BD que exige `AnalisisExpedienteDAO.recibirDocumentoAsociado`); antes de llegar ahi o despues de ya recibido, el boton se muestra deshabilitado con tooltip explicativo, nunca ausente.

No debe haber:

- Pestana independiente de Resultado si el diseno vigente integra resultado al panel de analisis.
- Bloques de Publicacion prevista o Expediente digital dentro del panel de Analisis si no corresponden.
- Derivacion directa a Notificacion desde Analisis salvo transicion/regla real.

## Verificacion

Pestana superior:

- `Bandeja Verificacion`.

Panel derecho:

- Lenguetas `Datos` y `Verificar`.
- Debe seguir patron visual de Analisis.
- Al final del panel `Verificar` solo deben quedar `Registrar Verificacion` y `Cancelar`, salvo regla posterior.

Reglas:

- Verificacion revisa documentos generados desde Analisis.
- Si hay observacion: no firma, registra motivo y devuelve normalmente a Analisis con trazabilidad.
- Si esta correcto: firma/emite, registra numero de documento y fecha, y actualiza estado documental.
- `Firma / Emision` esta integrada visualmente en Verificacion.
- No crear modulo lateral `Firma / Emision`.
- Resoluciones pasan a Ejecucion.
- Oficios/cartas/no resolutivos pasan a Notificacion solo con transicion real.
- `VerificacionExpedienteDAO.aprobarVerificacionConDestino` ramifica el destino final segun el equipo elegido en el combo "Equipo destino" del bloque "Destino operativo":
  - `Eq. Analisis` -> `ANALISIS/OBSERVADO` (devuelve el expediente a Analisis; SI reasigna responsable al usuario/equipo elegido; no inserta `EXPEDIENTE_OBSERVACION` estructurada porque este punto de entrada solo recibe un comentario de texto plano, no un `ObservacionVerificacionDTO`).
  - `Eq. Ejecucion` -> `EJECUCION/EN_EJECUCION` (mismo destino que `aprobarVerificacionDirecta` usa para resoluciones; NO reasigna responsable, porque la regla de Ejecucion exige que la atienda el mismo abogado de Analisis, no el usuario elegido en el combo).
  - Cualquier otro equipo (hoy solo `Eq. Supervision`, unico restante del whitelist) -> comportamiento historico: se encadenan 2 saltos de estado automaticos e inmediatos en la misma transaccion, primero `VERIFICACION/EN_VERIFICACION` -> `VERIFICACION/VERIFICADO` (`APROBACION_VERIFICACION`), y de inmediato `VERIFICACION/VERIFICADO` -> `NOTIFICACION/POR_ASIGNAR` (`DERIVACION_A_NOTIFICACION`). El expediente nunca queda "parado" en Verificado a la espera de una accion manual adicional; ver seccion Notificacion para el significado de `POR_ASIGNAR`.
  - Transicion de catalogo que faltaba (`DEVOLUCION_A_ANALISIS: VERIFICACION/EN_VERIFICACION -> ANALISIS/OBSERVADO`) sembrada por `74_transicion_verificacion_destino_analisis.sql` (ya ejecutado); la de `Eq. Ejecucion` ya existia en el catalogo (misma fila de `aprobarVerificacionDirecta`).
- El combo "Usuario destino" (equipo `EQ_NOTIFICACION`/`EQ_VALIDACION`) usa `UsuarioAsignacionService.listarUsuariosAsignablesPorEquipo(idEquipo)` (filtra solo por pertenencia activa al equipo, sin restriccion de rol), no `listarAbogadosAsignables` (restringido a rol `ABOGADO`/`ANALISTA`, que dejaba el combo vacio para esos dos equipos).
- El combo "Equipo destino" del bloque "Destino operativo" solo ofrece `EQ_ANALISIS`, `EQ_EJECUCION` y `EQ_SUPERVISION` (filtro por codigo en `cargarEquiposDestino()`, no en el DAO generico `listarEquiposActivos()`).

Grilla de documentos revisados:

- Replica diseno de documentos analizados de Analisis.
- Filtros por columna, flechas de ordenamiento y columnas fijas cuando aplique.
- Solo icono de guardar.
- No mostrar iconos Word ni eliminar.
- Supervisor puede editar `Estado`, `Detalle Obs.`, `Fecha Emision` y `N° Documento`.

## Ejecucion

Pestana superior:

- `Bandeja Ejecucion`.

Reglas:

- Solo resoluciones pasan a Ejecucion, sin importar el resultado del analisis (Procedente, Improcedente y Procedente en parte deben verse igual en la Bandeja Ejecucion; el filtro real es la etapa del expediente `et.codigo = 'EJECUCION'`, no el resultado). `EjecucionExpedienteDAO.buscarExpedientes` tenia un filtro adicional bugueado `WHERE UPPER(resultado_analisis) IN ('PROCEDENTE','PROCEDENTE EN PARTE')` que excluia silenciosamente los expedientes Improcedentes de la bandeja aunque ya estuvieran correctamente en Ejecucion; se quito (corregido 17/07/2026).
- El responsable debe ser el mismo abogado que realizo Analisis.
- Ejecucion no es reasignacion manual general.
- Procedente / procedente en parte: anotacion textual + carta de notificacion.
- Improcedente: carta de notificacion.
- Error material devuelve a Analisis solo con transicion real y motivo.
- Toda devolucion conserva resolucion/documentos previos e historial.
- Derivar a Notificacion solo con carta/documento listo, sin error pendiente y transicion real.

Grilla y panel derecho (implementado):

- Grilla principal replica filtros/diseno de Verificacion, con icono `+` para expandir expedientes asociados (mismo patron que Asignacion/Verificacion: expande expedientes relacionados por acta+titular, no documentos ni intentos).
- Panel derecho con lenguetas `Datos` y `Ejecutar`, oculto por defecto (se abre con doble clic).
- Grilla de documentos usa las mismas columnas que Analisis, filtrada a documentos `Emitido`/`Resolucion`; permite `+Documento`/`+Relacionado` y descarga Word. Un documento nuevo (`+Documento`) nace en estado `En proyecto` (mismo criterio que Analisis), no `Emitido`.
- Bloque `Resultado de ejecucion` incluye `Fecha Ejecucion` con calendario condicional segun el resultado elegido.
- Catalogo de resultado de ejecucion (`OREC`) sembrado por `56_agregar_resultado_ejecucion_orec.sql`.
- `Guardar Ejecucion` valida que exista una carta de notificacion final en despacho (`DocumentoAnalisisService.tieneDocumentoFinalEnDespacho`) **antes** de tocar la base de datos; si falta, bloquea la accion completa con mensaje claro y no registra `INICIO_EJECUCION`. Si esta lista, registra `INICIO_EJECUCION` y `DERIVACION_A_NOTIFICACION` en la misma accion, de modo que un guardado exitoso siempre deja el expediente visible en la Bandeja Asignacion de Notificacion. Antes de esta regla, la ejecucion se guardaba igual sin carta y el boton quedaba bloqueado en un estado intermedio sin derivar.
- El destino de `DERIVACION_A_NOTIFICACION` (Ejecucion) es `NOTIFICACION/POR_ASIGNAR`, no `EN_NOTIFICACION` (retargeteado por `72_estado_por_asignar_notificacion.sql`, ya ejecutado). Ver seccion Notificacion.

## Notificacion

Pestanas superiores vigentes:

- `Bandeja Asignacion`.
- `Bandeja Validacion`.
- `Bandeja Notificacion`.
- `Cierre`.

Reglas:

- Notificacion tiene bandeja operativa.
- Supervisor puede asignar documentos a validadores/abogados cuando el modelo lo soporte.
- Validador marca `Validado` u `Observado`, con comentario si observa.
- Observado vuelve a Analisis o Ejecucion segun origen del problema, conservando historial.
- Validado pasa a firma/siguiente paso de notificacion segun flujo.
- Registrar intentos:
  - intento 1 virtual;
  - intento 2 presencial/fisico;
  - intento 3 presencial/fisico.
- Registrar acuse/cargo si aplica.
- No enviar correos, SMS, WhatsApp ni integraciones externas.
- Notificacion registra metadata y trazabilidad.

Bandeja Asignacion (implementado):

- Grilla con columna `N° expediente SGD`, checkbox de seleccion individual/multiple e icono `+` que expande expedientes asociados (no documentos).
- Panel derecho oculto por defecto (doble clic para abrir), con lenguetas `Datos` y `Asignacion` (esta ultima internamente "Panel de Asignacion y Firma"; la antigua lengueta `Firma` separada se fusiono aqui, ver mas abajo), mismo patron visual que el Panel de Asignacion de Asignacion (incluye `Habilitar reasignacion`, `Hoja de envio nueva` vs `Hoja de envio actual`, historial de asignaciones/reasignaciones). Clic en la lengueta ya activa expande/restaura el panel (`splitAsigNotif.setSideExpanded(...)`), igual que en el Panel de Asignacion de Asignacion; la Bandeja Validacion (lenguetas `Datos`/`Validar`, `splitValidacionNotif`) sigue el mismo patron.
- Enrutamiento por clasificacion de documento: `INTERMEDIO` se asigna al equipo `EQ_NOTIFICACION` (abogados de notificacion); `FINAL` se asigna al equipo `EQ_VALIDACION` (validadores). No se permite mezclar clasificaciones en una misma asignacion, ni asignar al equipo que no corresponde.
- Ciclo de un documento `FINAL`: En despacho -> Bandeja Asignacion (asignar validador) -> Bandeja Validacion (Aprobado/Observado) -> Aprobado reaparece en Bandeja Asignacion con la seccion "Documentos a firmar" habilitada (pasa de Validado a Emitido, con numero de documento y fecha) -> Bandeja Notificacion. Si el validador marca Observado, el expediente se queda en `NOTIFICACION/POR_VALIDAR` (el resultado de validacion no mueve etapa/estado, solo limpia el responsable) y el documento reaparece en la misma Bandeja Asignacion (condicion ampliada, ver mas abajo) para que el supervisor lo derive con "Destino operativo".
- Ciclo de un documento `INTERMEDIO`: ya llega Emitido desde Verificacion; la seccion "Asignacion" no aplica (los combos quedan sin uso real) y "Documentos a firmar" solo permite corregir numero/fecha si hace falta; ya es visible en Bandeja Notificacion sin accion adicional.
- Panel fusionado "Asignacion" (antes 2 lenguetas separadas `Asignacion`/`Firma`): una sola lengueta con 4 secciones apiladas: `Documentos seleccionados` (grilla de hoja de envio, antes "Asignacion a validador"), `Destino operativo` (combos Equipo/Usuario destino), `Documentos a firmar` (grilla `DocumentoFirmaNotificacionTreeGridPanelV2`, ver abajo) e `Historial de asignacion/reasignacion`. Sin footer separado para Firma: el guardado de cada documento a firmar sigue siendo por fila (icono Guardar), y el footer `Generar asignacion`/`Cancelar` sigue existiendo solo para la asignacion a validador/notificador.
- `Destino operativo` (combo `Equipo destino`) ya no es solo `EQ_NOTIFICACION`/`EQ_VALIDACION`: `EQ_ANALISIS` y `EQ_EJECUCION` ahora derivan de verdad el expediente (antes estaban en el combo pero no hacian nada real). Exige exactamente 1 documento seleccionado (a diferencia de la asignacion a validador/notificador, que acepta lote), y solo pide abogado en el combo `Usuario destino` cuando el destino es Analisis (Ejecucion lo ignora: la regla es que la atienda el mismo abogado ya ligado via `EXPEDIENTE_ASIGNACION`, no el usuario elegido). Derivar a Analisis reutiliza `AsignacionExpedienteDAO.reasignarDesdeCartaRespuesta` (mismo metodo que el panel Cartas de Respuesta de Asignacion); derivar a Ejecucion usa el metodo nuevo `DocumentoAnalisisDAO.derivarDocumentoNotificacionAEjecucion`. Ambos dependen de transiciones de `FLUJO_TRANSICION` sembradas por el script `93_transicion_notificacion_destino_analisis_ejecucion.sql` (no ejecutado, requiere autorizacion) desde el unico origen real posible `NOTIFICACION/POR_VALIDAR`.
- Panel `Documentos a firmar`: replica el diseno del panel `Verificar` de Verificacion (grilla de documentos con icono Guardar por fila, `DocumentoFirmaNotificacionTreeGridPanelV2`), en vez de un formulario de un solo documento. La grilla lista TODOS los documentos del expediente del documento enfocado (no solo el documento cuya fila esta seleccionada en la bandeja, que en Asignacion es por-documento no por-expediente), columnas `Tipo documento`/`Numero Documento`/`Estado documento`/`Fecha Emision`. Solo Numero/Fecha son editables, y solo en las filas elegibles para firma: `INTERMEDIO` siempre, `FINAL` solo si su estado es `VALIDADO`; las filas no elegibles se ven en gris con el icono Guardar deshabilitado y tooltip explicando el motivo.
- KPIs propios en la bandeja, con el mismo patron de filtros compactos de tres filas que Registro/Asignacion.
- Historial de asignaciones/reasignaciones de Notificacion se guarda en la tabla generica `EXPEDIENTE_HISTORIAL` (no una tabla nueva), con `tipo_movimiento` `ASIGNACION_NOTIFICACION`/`REASIGNACION_NOTIFICACION` (`58_tipo_movimiento_notificacion.sql`).
- El filtro `Fecha Emision desde/hasta` de esta bandeja NO debe excluir documentos sin `fecha_documento` (es el caso normal de un documento recien llegado `EN_DESPACHO`, aun no emitido/fechado): si `fecha_documento` es nulo, el documento se muestra sin importar el rango de fechas seleccionado; el rango solo aplica cuando el documento ya tiene fecha. Antes de esta correccion, cualquier documento sin fecha desaparecia de la bandeja en cuanto se activaba un filtro de fecha, aunque estuviera correctamente pendiente de asignar.
- El combo "Usuario destino" del panel derecho (equipo `EQ_NOTIFICACION`/`EQ_VALIDACION`) usa `UsuarioAsignacionService.listarUsuariosAsignablesPorEquipo(idEquipo)` (solo pertenencia activa al equipo), no `listarAbogadosAsignables` (restringido a rol `ABOGADO`/`ANALISTA`, que dejaba el combo vacio para estos dos equipos porque sus miembros tienen rol `NOTIFICACION`/`VALIDACION`/`SUPERVISOR_NOTIFICACION`).

Estado del expediente en Notificacion (columna "Estado", distinta de "Estado doc."):

- Tres estados nuevos/reutilizados en etapa `NOTIFICACION` gobiernan que bandeja muestra cada expediente: `POR_ASIGNAR` ("Por asignar", `72_estado_por_asignar_notificacion.sql`), `POR_VALIDAR` ("Por validar", `73_estado_por_validar_y_renombrar_por_notificar.sql`) y `EN_NOTIFICACION` (renombrado a "Por notificar" por el mismo script 73; el codigo interno sigue siendo `EN_NOTIFICACION` a proposito, ver mas abajo). Ambos scripts ya ejecutados.
- Ejecucion (`DERIVACION_A_NOTIFICACION` desde `EJECUCION/EJECUTADO`) y Verificacion (`DERIVACION_A_NOTIFICACION` desde `VERIFICACION/VERIFICADO`, encadenado automaticamente tras `APROBACION_VERIFICACION`) dejan el expediente en `NOTIFICACION/POR_ASIGNAR` de inmediato.
- `DocumentoAnalisisDAO.asignarNotificacionMultiple`/`reasignarNotificacion` resuelven el codigo del equipo destino y actualizan `expediente.id_estado_actual` en la misma llamada: `EQ_VALIDACION` -> `POR_VALIDAR`, `EQ_NOTIFICACION` -> `EN_NOTIFICACION` ("Por notificar"). `registrarResultadoValidacion`, cuando el resultado es Aprobado (no Observado), revierte el expediente a `POR_ASIGNAR` para que reaparezca en Bandeja Asignacion y el coordinador lo derive esta vez a Notificacion (el documento FINAL ya quedo en `VALIDADO`, que `CONDICION_ASIGNACION_NOTIFICACION` ya acepta). Cuando es Observado, el estado del expediente no se toca (solo se limpia el responsable), igual que antes.
- Cada una de las 3 bandejas exige ademas su propio estado de expediente en la condicion SQL (join a `estado_expediente` por `e.id_estado_actual`, alias `eest`): Asignacion exige `POR_ASIGNAR`, Validacion exige `POR_VALIDAR`, Notificacion exige `EN_NOTIFICACION`. No exigir esto ademas de la clasificacion/estado del documento era la brecha reportada por el usuario.
- Decision de diseno importante: la bandeja final de Notificacion NO usa un codigo nuevo tipo `POR_NOTIFICAR`. Se reutiliza `EN_NOTIFICACION`, que ya era el origen exigido por el flujo completo de intentos/cargo/confirmacion (`NOTIFICACION_VIRTUAL`, `NOTIFICACION_PRESENCIAL_1/2`, `RECEPCION_CARGO_ACUSE`, `CONFIRMACION_NOTIFICACION` -> `NOTIFICADO`, este ultimo ya existente y sin cambios). Crear un codigo nuevo habria obligado a reescribir esa cadena de transiciones ya probada (`NotificacionExpedienteDAO.estadoOrigenNotificacion` la exige literalmente antes de intentar la transicion). Solo se cambio el `nombre` visible de "En notificacion" a "Por notificar"; el codigo y todo el flujo de intentos siguen intactos.
- El DTO `NotificacionAsignacionDocumentoDTO` ahora expone `estadoExpedienteCodigo`/`estadoExpediente` ademas de `estadoDocumentoCodigo`/`estadoDocumento`; las 3 grillas de `JPanelNotificacionV2` (Asignacion, Validacion, Notificacion) muestran ambos: la columna que antes se llamaba `Estado` ahora es `Estado doc.` (estado del documento), y se agrego una columna `Estado` nueva con el estado del expediente.
- El script 73 tambien corrigio puntualmente documentos ya asignados a un validador/notificador cuyo expediente habia quedado en `POR_ASIGNAR` (asignado antes de esta regla): se retargearon a `POR_VALIDAR`/`EN_NOTIFICACION` segun el equipo real del documento, para que no quedaran huerfanos con las condiciones nuevas.

Bandeja Validacion (implementado):

- Sin checkbox ni `+`; solo doble clic para abrir el panel derecho.
- Panel derecho oculto por defecto, con lenguetas `Datos` y `Validar`.
- `Validar` incluye la grilla de documentos del expediente (editable en `Estado`) y el bloque `Resultado de validacion`: `Aprobado`/`Observado` (catalogo `tipo_resultado_validacion`, `57_catalogo_resultado_validacion_notificacion.sql`) con comentario obligatorio si es Observado.
- Marcar `Observado` cambia el documento a estado `OBSERVADO` y limpia la asignacion (equipo/usuario/hoja de envio de notificacion), sin tocar `expediente.id_etapa_actual`/`id_estado_actual`; el abogado responsable (Ejecucion para `FINAL`, Analisis/Verificacion para `INTERMEDIO`) lo ve en su propia grilla de documentos, que ya no filtra por estado.

Bandeja Notificacion, intentos al ciudadano (implementado):

- Grilla arbol unica (documento padre + intentos hijo), sin dividir en 2 grillas. Fila padre con checkbox de seleccion multiple (para agregar intentos a varios documentos a la vez) e icono de expandir/colapsar que solo aparece si el documento ya tiene intentos.
- `Estado Final` del documento (columna de la fila padre, calculado en servidor via `DocumentoAnalisisDAO`, no en el cliente) tiene 4 valores: `POR NOTIFICAR` (sin intentos), `PENDIENTE` (intento ENVIADO con Estado Notificacion en blanco/NO UBICADO), `ATENDIDO` (intento con Estado Notificacion UBICADO) y `POR PUBLICAR` (intento 1 y 2 ambos ENVIADO + NO UBICADO). Se deriva de `expediente_notificacion`/`estado_notificacion` (codigos PENDIENTE/ENVIADA/FALLIDA/EXITOSA); no requiere columna nueva.
- `+ Agregar intento` no abre dialogo: inserta una fila hija "borrador" editable (Modalidad por combo, Codigo/Usuario Notificacion por texto) para cada documento marcado (o el ultimo con clic simple si no hay ninguno marcado). Se persiste con el icono Guardar de esa misma fila; hay un icono Cancelar para descartarla sin guardar.
- Fila hija (intento) editable inline: Modalidad, Estado (`Pendiente`/`Enviado`, bloqueado si ya es `Atendido`) y Estado Notificacion (en blanco/`No ubicado`/`Ubicado`); elegir `Ubicado` y guardar confirma la recepcion (Fecha Recepcion = Fecha Acuse, la misma que ve Cartas de Respuesta) y marca el intento `Atendido`.
- "Codigo Notificacion" (modalidad virtual) y "Usuario Notificacion" (modalidad presencial) son el mismo campo de texto libre reutilizado segun la modalidad de esa fila; se guardan en `expediente_notificacion.codigo_notificacion` y, al confirmar recepcion, tambien en `expediente_cargo_acuse.recibido_por`. No existe columna separada "usuario_notificacion".

Cierre:

- Es pestana interna de Notificacion.
- No exponer como modulo lateral independiente.
- Cierre terminal debe registrar historial y nunca borrar datos.

## Publicacion

- Existe como modulo V2.
- No implementar publicacion real en portales externos.
- Solo registrar metadata, historial y transiciones reales.
- Usar `EXPEDIENTE_PUBLICACION`, `EXPEDIENTE_NOTIFICACION`, `EXPEDIENTE_CARGO_ACUSE`, `EXPEDIENTE_HISTORIAL`, `EXPEDIENTE_RESOLUCION` y `EXPEDIENTE` segun soporte real.
- Si falta transicion, catalogo, tabla o columna, bloquear con diagnostico sin escritura parcial.

## Expediente digital

- Consultar metadata de expediente digital cuando exista.
- No mover archivos fisicamente.
- No eliminar archivos.
- No implementar integraciones externas con NAS, SharePoint, Drive, MinIO u otros sin autorizacion.
- No asumir que es prioridad para almacenamiento documental; la prioridad actual es trazabilidad del tramite.

## Administracion

Modulos:

- Usuarios.
- Roles.
- Equipo Juridico.
- Feriados.
- Plazos.

Reglas:

- Usar patron visual de modulos operativos cuando aplique.
- Grilla principal con filtros por columna y flechas.
- Grilla ajustada al contenido real (sin columna `ID` visible) en Usuarios, Roles y Equipo Juridico.
- Panel derecho al seleccionar fila, oculto por defecto: se abre con doble clic, no al cargar la bandeja.
- Panel derecho incluye boton `X` para cerrarlo (Usuarios, Roles, Equipo Juridico).
- Botones principales azules institucionales conservando forma/tamano del modulo.
- Nunca eliminar fisicamente roles, usuarios o equipos.
- No mostrar ni guardar passwords en texto plano.
- `Usuarios`: tipo documento debe ser combo basado en catalogo de identidad.
- `Usuarios`: `Restablecer clave` (implementado) asigna contrasena temporal al usuario seleccionado (`DlgRestablecerClaveV2`), fuerza cambio en el proximo login, y permite marcar "reiniciar verificacion en dos pasos" cuando corresponda.
- `Equipo Juridico`: incluye la vista "Personal por supervisor" (combo de supervisores + grilla de abogados supervisados) respaldada por `UsuarioSupervisionDAO` y `EquipoJuridicoService.listarSupervisoresConAbogados()`/`listarAbogadosPorSupervisor()`, que lee `USUARIO_SUPERVISION`. La grilla tiene una primera columna `Activo` (casilla editable inline, mismo patron que "Personal del equipo") que quita/vuelve a asignar a un abogado del supervisor elegido en el combo (`EquipoJuridicoService.quitarAbogadoDeSupervisor`/`agregarAbogadoASupervisor`, baja/alta logica sobre `USUARIO_SUPERVISION.ACTIVO`, sin dialogo de confirmacion por ser trivialmente reversible). La grilla lista abogados activos E inactivos para ese supervisor (antes solo listaba activos); las filas inactivas se muestran atenuadas en gris.
- `Equipo Juridico`, grilla "Personal del equipo" (miembros de un equipo, distinta de "Personal por supervisor"): primera columna `Activo` es una casilla editable inline (checkbox nativo de JTable) que activa/desactiva de forma rapida la pertenencia del usuario al equipo (`EQUIPO_USUARIO.ACTIVO`), reutilizando `EquipoJuridicoService.agregarMiembro`/`quitarMiembro` (baja/alta logica, sin dialogo de confirmacion en ningun sentido porque es trivialmente reversible con la misma casilla). La grilla lista miembros activos E inactivos (antes solo listaba activos, por lo que un miembro retirado desaparecia sin forma de reactivarlo salvo volver a buscarlo en el combo de alta); las filas inactivas se muestran atenuadas en gris. Los botones `Agregar miembro`/`Quitar miembro`/`Marcar responsable` siguen intactos como alternativa.
- `Roles`/`Equipo Juridico`: el tooltip del campo `Codigo` ahora advierte no repetir un rol/equipo ya existente y lista los codigos oficiales ya sembrados (roles: los 10 reconocidos por scripts; equipos: prefijo `EQ_`), para evitar que se repita la duplicidad corregida por los scripts 66/70/71.

## Permisos (control de acceso)

Estado: implementado y en uso.

- Permisos por rol, no por equipo; equipo es una dimension de negocio/alcance de datos (a quien se asigna trabajo), no de control de acceso a pantallas.
- Dos niveles de permiso: modulo (boton del menu lateral) y bandeja (pestana superior dentro de un modulo). No hay permisos a nivel de panel/lengueta interna (`Datos`/`Asignacion`/`Firma`, etc.): son facetas de la misma tarea dentro de una bandeja ya autorizada, no funciones independientes.
- Tablas `permiso` y `rol_permiso` ya existian (`07_tablas_fase_2.sql`) pero estaban vacias/sin uso en tiempo de ejecucion; sembradas por `59_catalogo_permisos_menu.sql` (modulo) y `60_catalogo_permisos_bandejas.sql` (bandeja, solo en Registro/Recepcion, Asignacion y Notificacion, que tienen mas de una pestana superior). Ambos scripts otorgan todos los permisos a `ADMIN_SISTEMA`; la asignacion al resto de roles se configura desde Administracion > Roles > `Permisos del rol`.
- `SessionContext.setPermisos(...)`/`tienePermiso(codigo)` es fail-open: si el catalogo de permisos resuelto para la sesion esta vacio, `tienePermiso` retorna `true` (no bloquea nada hasta que un admin configure permisos reales por rol).
- `MenuPrincipalV2.resolverPermisosSesion()` puebla `SessionContext` via `PermisoRolService` antes de construir el menu; oculta botones de modulo sin permiso.
- Bandejas sin permiso se deshabilitan con `tabs.setEnabledAt(indice, false)` (no se eliminan del `JTabbedPane`): `JPanelRegistroRecepcionV2`, `JPanelAsignacionV2` y `JPanelNotificacionV2` tienen logica interna que asume indices fijos de pestana (comparaciones `getSelectedIndex()==N`); remover pestanas correria el riesgo de desalinear esa logica.
- Matriz real de permisos por rol operativo (RECEPCION, ASIGNACION, ABOGADO, SUPERVISION, SUPERVISOR_NOTIFICACION, NOTIFICACION, VALIDACION, CONSULTA) preparada en `65_matriz_permisos_roles_operativos.sql`; agrega los roles nuevos `VALIDACION` y `SUPERVISOR_NOTIFICACION` (Notificacion se divide en 3 roles: quien asigna, quien notifica, quien valida). `REGISTRADOR_CIVIL` queda sin permisos a proposito (Firma/Emision ya esta integrada en Verificacion, nadie tiene ese rol hoy). Script preparado, no ejecutado contra BD.
- Ademas de los 10 roles reconocidos por los scripts del proyecto, Administracion > Roles permite crear roles libremente por boton "Nuevo rol": asi aparecieron 4 roles redundantes sin uso (`ADMINISTRADOR`, `ANALISTA`, `PREASIGNADOR`, `SUPERVISOR`, 0 usuarios asignados) que solapan con roles oficiales, y permisos sueltos fuera de catalogo (prefijo `PERM_*`, distinto de `MENU_*`/`BANDEJA_*`). Diagnostico de solo lectura en `00_diagnostico_permisos_reales_vs_esperado.sql`; limpieza (desactivacion logica, nunca eliminacion fisica) en `70_desactivar_roles_redundantes.sql`/`71_desactivar_permisos_no_oficiales.sql`. Ninguno ejecutado contra BD.

Visibilidad por asignacion dentro de una bandeja (dato, no permiso):

- Ademas del control de acceso por rol (arriba), las bandejas de Analisis, Verificacion, Ejecucion y las pestanas Validacion/Notificacion de Notificacion filtran ademas por fila: un usuario normal solo ve expedientes/documentos cuyo responsable actual (`EXPEDIENTE.id_usuario_responsable_actual`/`id_equipo_responsable_actual`, o `EXPEDIENTE_DOCUMENTO_ANALIZADO.id_usuario_notificacion`/`id_equipo_notificacion` en Notificacion) coincide con su propio usuario o con alguno de sus equipos (`EQUIPO_USUARIO`). `ADMIN_SISTEMA` no tiene ese filtro y ve todo, este o no asignado/derivado a el. Implementado en `VisibilidadBandejaSql.construirCondicion(...)` (helper compartido en `infrastructure.sdrercapp.dao`), invocado desde `AnalisisExpedienteDAO`, `VerificacionExpedienteDAO`, `EjecucionExpedienteDAO`, `NotificacionExpedienteDAO` y `DocumentoAnalisisDAO.listarDocumentosValidacion/listarDocumentosNotificacion`. Si no se pudo resolver ni usuario ni equipo del actor, se deniega por defecto (`1=0`), no se muestra todo.
- Excluidas a proposito de este filtro por fila (siguen mostrando toda la cola, como antes): `Registro/Recepcion` (incluida la Bandeja de Expedientes general, que comparte el mismo `ExpedienteBandejaDAO`/`JPanelBandejaExpedientesNueva`), la Bandeja Asignacion del modulo Asignacion, y la Bandeja Asignacion de Notificacion. Estas tres son pantallas de coordinacion/despacho (quien registra, quien asigna, quien reasigna) cuyo trabajo exige ver toda la cola entrante, no solo lo ya asignado a si mismos; ademas, en Registro/Asignacion el responsable todavia es `NULL` para la mayoria de expedientes (recien se completa cuando Asignacion actua), por lo que un filtro por fila las dejaria vacias. Revisar si se pide extender el filtro a estas bandejas mas adelante.
- `UsuarioAsignacionService.resolverUsuarioActualSdrercApp()`/`esAdminSistemaActual()`/`listarIdsEquipoDeUsuario(idUsuario)` son los helpers nuevos para resolver usuario/rol/equipos de la sesion actual contra SDRERC_APP; los `*ExpedienteService` existentes conservan su propio `resolverUsuarioActualSdrercApp()` privado (no se refactorizo esa duplicacion) y ahora ademas calculan `esAdmin`/`idsEquipoActual` para pasarlos al DAO.

Autor del historial cuando actua ADMIN_SISTEMA:

- Cuando quien ejecuta una accion de Analisis, Verificacion, Ejecucion, Asignacion (expediente), o asignacion/reasignacion de Notificacion tiene el rol `ADMIN_SISTEMA`, `EXPEDIENTE_HISTORIAL` NO guarda al administrador como autor (`id_usuario_origen`/`creado_por`): se sustituye por el usuario asignado/reasignado/derivado de esa misma accion (el destino ya resuelto en cada llamada). Si la accion no cambia de responsable (por ejemplo, generar numero de expediente o editar datos de asignacion), no hay a quien sustituir y se conserva el autor real.
- Implementado como `resolverAutorHistorial(conn, idUsuarioActor, idUsuarioDestino)` (metodo privado duplicado con la misma logica en cada DAO, no centralizado) en `AsignacionExpedienteDAO`, `AnalisisExpedienteDAO`, `VerificacionExpedienteDAO`, `EjecucionExpedienteDAO`, `NotificacionExpedienteDAO` y `DocumentoAnalisisDAO` (este ultimo solo en `insertarHistorialNotificacion`, la asignacion/reasignacion a validador/notificador). Usa `CatalogoLookupDAO.tieneRolAdminSistema(conn, idUsuario)` (nuevo) para decidir dentro de la misma transaccion.
- Esta sustitucion es deliberada y fue confirmada explicitamente por el usuario pese a que implica que el historial ya no refleja literalmente quien hizo clic; no revertir sin pedirlo de nuevo.

## Base de datos y scripts

Reglas:

- No ejecutar SQL sin autorizacion explicita.
- No modificar datos sin autorizacion.
- No usar `DROP`, `DELETE`, `TRUNCATE`, `INSERT`, `UPDATE` o `MERGE` salvo autorizacion y alcance claro.
- Scripts nuevos deben ser idempotentes cuando sea posible.
- Scripts se crean en `db/sdrerc_app/scripts` con numeracion correlativa.
- No reejecutar scripts base sobre BD existente sin autorizacion.
- No recalcular historicos masivamente.

Tablas relevantes mencionadas por reglas vigentes:

- `EXPEDIENTE`.
- `EXPEDIENTE_SOLICITUD`.
- `EXPEDIENTE_PERSONA`.
- `EXPEDIENTE_ACTA`.
- `EXPEDIENTE_ALERTA`.
- `EXPEDIENTE_RELACION`.
- `EXPEDIENTE_ASIGNACION`.
- `EXPEDIENTE_DOCUMENTO`.
- `EXPEDIENTE_DOCUMENTO_ANALIZADO`.
- `EXPEDIENTE_EVALUACION`.
- `EXPEDIENTE_HISTORIAL`.
- `EXPEDIENTE_OBSERVACION`.
- `EXPEDIENTE_RESOLUCION`.
- `EXPEDIENTE_NOTIFICACION`.
- `EXPEDIENTE_CARGO_ACUSE`.
- `EXPEDIENTE_PUBLICACION`.
- `EXPEDIENTE_DIGITAL`.
- `PLAZO_CONFIGURACION`.
- `ESTADO_EXPEDIENTE`.
- `FLUJO_TRANSICION`.
- `TIPO_DOCUMENTO_ADJUNTO`.
- `UBIGEO_DEPARTAMENTO`, `UBIGEO_PROVINCIA`, `UBIGEO_DISTRITO`.
- `USUARIO`, `USUARIO_ROL`, `EQUIPO_USUARIO`, `USUARIO_SUPERVISION`, `USUARIO_TOTP_BACKUP_CODE` (login/autenticacion V2).
- `PERMISO`, `ROL_PERMISO` (control de acceso, ver seccion Permisos).

Scripts recientes relevantes:

- `38_analisis_multiple.sql` existe, pero la regla vigente vuelve a analisis unico.
- `44_asignacion_notificacion_validacion.sql`: estados `ASIGNADO`/`VALIDADO`, equipo `EQ_VALIDACION`, columnas de asignacion de notificacion en `EXPEDIENTE_DOCUMENTO_ANALIZADO`.
- `56_agregar_resultado_ejecucion_orec.sql`: catalogo de resultado de ejecucion.
- `57_catalogo_resultado_validacion_notificacion.sql`: catalogo `APROBADO`/`OBSERVADO` para Bandeja Validacion de Notificacion.
- `58_tipo_movimiento_notificacion.sql`: tipos `ASIGNACION_NOTIFICACION`/`REASIGNACION_NOTIFICACION` para historial.
- `59_catalogo_permisos_menu.sql`: permisos por modulo (boton de menu lateral).
- `60_catalogo_permisos_bandejas.sql`: permisos por bandeja (pestana superior) en Registro/Recepcion, Asignacion y Notificacion.
- `61_login_2fa_usuario.sql`: columnas de autenticacion/TOTP en `USUARIO` + tabla `USUARIO_TOTP_BACKUP_CODE`. Ya ejecutado.
- `62_reset_datos_prueba_y_superadmin.sql`: reset completo de datos de prueba (trunca tablas transaccionales, conserva catalogos, reinicia `IDENTITY` a 1) + creacion del superadmin. Ya ejecutado; no reejecutar sin autorizacion explicita (es destructivo).
- `63_carga_personal_sdrerc_usuarios.sql`: carga 111 usuarios reales del personal SDRERC con rol/equipo segun su area (fuente `docs/arquitectura_app/Personal_SDRERC_Usuarios_Herramienta_Interna.xlsx`), agrega `USUARIO.TELEFONO` y el rol nuevo `CONSULTA`. No ejecutado.
- `64_carga_supervision_abogados.sql`: relacion supervisor-abogado en `USUARIO_SUPERVISION` para el area de Analisis (74 relaciones, fuente `docs/arquitectura_app/personal_supervisores.xlsx`). Requiere el script 63 ya ejecutado. No ejecutado.
- `65_matriz_permisos_roles_operativos.sql`: matriz de permisos por rol operativo y roles nuevos `VALIDACION`/`SUPERVISOR_NOTIFICACION` (ver seccion Permisos). No ejecutado.
- `66_consolidar_equipos_redundantes.sql`: consolida equipos duplicados creados a mano (`EQUIPO_ANALISIS`/`EQUIPO_VERIFICACION`/`EQUIPO_EJECUCION`/`EQUIPO_NOTIFICACION`/`EQ_FIRMA_EMISION`) hacia el equipo canonico `EQ_*`, migrando membresias activas antes de desactivar el redundante; nunca elimina fisicamente. No ejecutado.
- `67_carga_supervision_recepcion_notificacion.sql`: extiende `USUARIO_SUPERVISION` a Recepcion/Asignacion y Notificacion (25 relaciones adicionales, mismo Excel del script 64, hoja distinta). Requiere el script 63 ya ejecutado. No ejecutado.
- `68_espejar_equipo_ejecucion_analisis.sql`: sincroniza miembros de `EQ_EJECUCION` con `EQ_ANALISIS` (mismo abogado en Analisis y Ejecucion, regla vigente de Ejecucion); re-ejecutable para mantener ambos equipos alineados. No ejecutado.
- `69_distribuir_notificacion_validacion_publicacion.sql`: distribuye el personal de `EQ_NOTIFICACION` cargado por 63 entre Notificacion/Validacion/Publicacion (el Excel de origen no distinguia esos 3 roles); reparto autorizado por el usuario sin criterio de negocio especifico, documentado con semilla fija en el propio script. No ejecutado.
- `70_desactivar_roles_redundantes.sql` / `71_desactivar_permisos_no_oficiales.sql`: desactivan roles y permisos creados fuera de los scripts del proyecto (ver seccion Permisos). No ejecutados.
- `72_estado_por_asignar_notificacion.sql`: siembra el estado `POR_ASIGNAR` en etapa `NOTIFICACION`, retarget de `DERIVACION_A_NOTIFICACION` (Ejecucion) hacia ese estado, nueva fila de transicion para Verificacion, y correccion puntual de expedientes ya varados en `EN_NOTIFICACION` con documento pendiente de asignar (ver seccion Notificacion). Ya ejecutado.
- `73_estado_por_validar_y_renombrar_por_notificar.sql`: siembra el estado `POR_VALIDAR` en etapa `NOTIFICACION`, renombra el `nombre` de `EN_NOTIFICACION` a "Por notificar" (mismo codigo), y corrige puntualmente expedientes ya asignados cuyo estado habia quedado desalineado (ver seccion Notificacion). Ya ejecutado.
- `74_transicion_verificacion_destino_analisis.sql`: agrega la fila de `FLUJO_TRANSICION` que faltaba `DEVOLUCION_A_ANALISIS: VERIFICACION/EN_VERIFICACION -> ANALISIS/OBSERVADO`, para que `aprobarVerificacionConDestino` pueda enviar el expediente a Analisis cuando el destino elegido es `Eq. Analisis` (ver seccion Verificacion). Ya ejecutado.
- `75_plantilla_documento.sql`: crea la tabla `PLANTILLA_DOCUMENTO` (plantillas Word versionadas como BLOB por tipo de documento) y siembra el permiso `MENU_ADMIN_PLANTILLAS` otorgado a `ADMIN_SISTEMA` (ver seccion Plantillas). Ya ejecutado.
- `76_plantilla_bloque.sql`: crea la tabla `PLANTILLA_BLOQUE` (bloques de contenido con condicion opcional, insertados en el marcador `[[CONTENIDO]]` de la plantilla base) (ver seccion Plantillas). Ya ejecutado.
- `77_plantilla_bloque_seccion.sql`: agrega la columna `PLANTILLA_BLOQUE.SECCION` para soportar marcadores nombrados `[[CONTENIDO:seccion]]` (varios puntos de contenido dinamico por plantilla) (ver seccion Plantillas). Ya ejecutado.
- Diagnosticos de solo lectura (no forman parte de la numeracion incremental): `00_diagnostico_roles_permisos_equipos.sql`, `00_diagnostico_permisos_reales_vs_esperado.sql`.

Ubigeo:

- `UBIGEO_*` debe replicar estructura y datos aprobados desde BD origen `SYSTEM` hacia `SDRERC_APP` en `XEPDB1` con script intermedio unico cuando se autorice.
- Si ya existe data sembrada, scripts posteriores deben ser compatibles e idempotentes.

## UI/UX vigente

Reglas generales:

- UI institucional, sobria, moderna.
- No mostrar codigos tecnicos si existe nombre amigable.
- No mostrar `V2` al usuario final.
- No usar `padre/hijo` en UI.
- Doble clic abre panel derecho en bandejas operativas.
- Seleccion por casillas puede abrir panel contextual masivo.
- Panel derecho con lenguetas, X, scroll vertical interno y sin scroll horizontal.
- Si se cierra panel con X, no limpiar seleccion.
- Las lenguetas pueden seleccionar y expandir/restaurar segun patron vigente.
- Botones principales usan azul institucional, conservando forma/tamano original.
- Fechas visibles: `dd/MM/yyyy`, sin hora.
- Tablas con filtros por columna debajo de cabeceras.
- Ordenamiento por cabecera con flechas visibles.
- Cabecera, filtros y cuerpo deben desplazarse sincronizados dentro del mismo `JScrollPane`.
- No usar scroll horizontal global para tablas.
- Columna `Dias` como pill con color segun configuracion de plazos.
- Renderers de columnas no deben depender de indices fragiles; preferir nombres/constantes.
- En las grillas de "documentos analizados" (Analisis, Cartas de respuesta de Asignacion, Verificacion, Ejecucion, y la grilla de Notificacion que reutiliza la clase de Ejecucion), el bloque de iconos de accion va al inicio de las columnas, en el orden `Guardar, Word, Eliminar` (cada grilla muestra solo los iconos que le aplican; Verificacion y la grilla de Notificacion solo tienen `Guardar`, Ejecucion tiene `Guardar, Word`, Cartas de respuesta solo tiene `Guardar` en su tabla de documentos hijo).

Bandejas:

- `Fecha Solicitud` y `Fecha Vencimiento` visibles en Registro y Asignacion.
- `Fecha Vencimiento` va despues de `Fecha Solicitud`.
- En Registro, `N° expediente SGD` va al lado derecho de `Nro. Expediente`.
- Filas asociadas deben ser visualmente jerarquicas: icono, franja/acento, fondo celeste suave, texto atenuado y sin checkbox comun.

Filtros:

- Formato compacto de tres filas:
  - busqueda y botones principales;
  - fechas desde/hasta;
  - estado, grupo familiar y limite numerico.
- Evitar texto visible `Mostrar`; el input numerico comunica el limite.
- Registro: combo estado solo `Todos los estados` y `Registrado`.
- Asignacion: combo estado conserva opciones propias.

## Plazos

- `Dias` representa dias habiles restantes respecto de `EXPEDIENTE.FECHA_VENCIMIENTO`.
- No representa dias transcurridos.
- Dias habiles excluyen sabados, domingos y feriados activos configurados.
- No hardcodear feriados.
- No hardcodear plazos operativos en formularios Swing.
- Plazos oficiales iniciales:
  - Rectificacion administrativa: 30 dias habiles.
  - Reconsideracion: 15 dias habiles.
  - Apelacion: 30 dias habiles.
  - SDRERC general: 30 dias habiles como contingencia.
- Colores de pill deben derivarse de `PLAZO_CONFIGURACION`, no de reglas dispersas.

## Despliegue cliente-servidor LAN

Modo vigente:

- LAN por `FILE_SHARE`/UNC.
- No ejecutar JAR desde carpeta compartida.
- Cliente copia/actualiza localmente en `C:\SDRERC_CLIENTE`.

Servidor:

- Carpeta: `D:\SDRERC_RELEASES\latest`.
- Archivos esperados:
  - `version.json`.
  - `SDRERC-V2.zip`.
  - `checksums.txt`.

Cliente:

- Launcher: `C:\SDRERC_CLIENTE\launcher`.
- Config: `updater-config.json`.
- App local: `C:\SDRERC_CLIENTE\app`.
- Logs: `C:\SDRERC_CLIENTE\logs`.

Scripts:

- Cliente: `scripts/client/sdrerc-launcher.ps1`.
- Cliente BAT: `scripts/client/run-sdrerc-client.bat`.
- Servidor: `scripts/server/publish-sdrerc-release.ps1`.

Publicar release LAN:

```powershell
.\scripts\server\publish-sdrerc-release.ps1 -Version "x.y.z"
```

HTTP/VPN:

- Queda como capacidad experimental documentada.
- No es configuracion estandar vigente.
- No exponer Oracle ni releases a internet publico.

## Git y validacion

Reglas:

- Ejecutar `git status` antes y despues.
- No usar `git add .`.
- Agregar solo archivos de la tarea.
- No incluir logs, zips, outputs, credenciales ni archivos ajenos.
- Si se modifica Java: `mvn clean compile`.
- Si se modifica App V2, launcher o empaquetado: `mvn clean package`.
- Para cambios solo Markdown, no compilar salvo pedido explicito.
- Si build/verificacion pasa, AGENTS.md indica commit y push obligatorios en tareas normales.
- Si la tarea dice explicitamente no hacer commit/push, obedecer.
- Si hay SQL/BD involucrada sin autorizacion, no ejecutar ni hacer cambios de datos.

Formato final esperado:

- Resumen corto.
- Archivos modificados.
- Validacion realizada.
- Confirmar si se ejecuto o no SQL.
- Confirmar legacy y `OracleConnection.java` intactos.
- Commit y push si correspondio.

## Como debe actuar Claude Code

- Usar `AGENTS.md` y este archivo como contexto base.
- Revisar codigo real antes de asumir implementacion.
- Mantener cambios pequenos, incrementales y compilables.
- No hacer refactors oportunistas.
- No corregir temas fuera de alcance salvo que bloqueen la tarea.
- Si detecta contradiccion funcional, priorizar la regla mas reciente documentada y reportar.
- Si una regla persistente cambia, actualizar `AGENTS.md` y `CLAUDE.md`.
- Si falta soporte de BD, crear script idempotente solo si el usuario lo autoriza o la tarea lo pide, y no ejecutarlo automaticamente.
- Si una accion depende de transicion real, validar `FLUJO_TRANSICION`; no inventar rutas.
- Si una UI requiere catalogo, cargar desde Service/DAO, no hardcodear.

## Prompt inicial recomendado para Claude Code

```text
Lee completamente AGENTS.md y CLAUDE.md antes de actuar. Usa esos archivos como contexto principal del proyecto SDRERC V2. Respeta las reglas funcionales, tecnicas, de BD, UI y Git alli documentadas. Antes de modificar codigo, revisa los archivos reales relacionados con la tarea. No ejecutes SQL ni toques legacy salvo autorizacion explicita.
```
