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
- Flujo: credenciales -> cambio de contrasena obligatorio si `debe_cambiar_password=1` -> enrolamiento TOTP (primera vez, sin `totp_habilitado`) o verificacion TOTP (logins posteriores) -> sesion.
- TOTP implementado con RFC 6238 sobre JDK puro (sin libreria externa de TOTP); enrolamiento con codigo QR generado con ZXing y clave manual como respaldo.
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
- La asociacion de duplicados puede resolverse desde Registro (lengueta "Asociar duplicados" en el panel de Registro) o desde Asignacion (lengueta "Asociar"); ambas reutilizan el mismo servicio de asociacion.
- Reconsideracion y Apelacion se registran sin numero; Asignacion decide asociar o generar numero.
- Numero SDRERC visible: `SDRERC-EXP-YYYY-000001`.
- No usar `id_expediente` como correlativo visible.
- Fecha visible en UI: `dd/MM/yyyy`.
- `Nro. tramite web` no es obligatorio.
- Si canal es `Mesa de partes virtual`, habilitar `Nro. tramite web`.
- Si canal no es `Mesa de partes virtual`, bloquear y mostrar `SIN TRAMITE`.
- `N° expediente SGD` vive en el bloque `Datos del expediente`.
- `N° expediente SGD` y `Tipo de acta` son obligatorios en Registro manual, Edicion manual y Carga diaria.
- `N° expediente SGD` debe ser único (sin duplicados) en Registro manual, Edicion manual y Carga diaria (20/07/2026). Validado contra `EXPEDIENTE_SOLICITUD.numero_expediente_sgd` de expedientes activos y no asociados (`ExpedienteRegistroDAO.detectarDuplicadoPorNumeroExpedienteSgd`/`detectarDuplicadosPorNumeroSgdContraBase`); en Edicion manual se excluye el propio id_expediente para no marcarse a si mismo como duplicado. Es un bloqueo real (no una advertencia): en Registro manual y Edicion manual deshabilita el boton de guardar (`RegistroManualExpedienteService.registrar`/`ExpedienteEdicionManualService.validar` tambien lo rechazan del lado servidor); en Carga diaria la fila queda con `estadoValidacion="Error"` y `listoParaRegistrar=false`, por lo que Confirmar la salta sin bloquear el resto del lote. Carga diaria tambien detecta el mismo SGD repetido dentro del propio Excel (mismo patron que la deteccion de acta+titular). No se agrego una restriccion UNIQUE a nivel de BD (solo existe el indice no unico `ix_exp_sol_sgd`); si se requiere ese refuerzo adicional, es un paso aparte que requiere autorizacion y revisar datos historicos.
- `Tipo documento` de solicitud debe normalizar equivalencias con y sin tilde.

KPIs vigentes en Bandeja Registro:

- `Potencial duplicado`.
- `Posible Grupo Familiar`.

Reglas KPI:

- KPIs se calculan segun filtros activos.
- Clic en KPI filtra por esa alerta.
- Clic en `Buscar` limpia filtro KPI y vuelve al total por filtros de busqueda.

Alertas:

- En bandeja, columna `Alertas` solo muestra:
  - `Sin Alerta`.
  - `Potencial duplicado`.
  - `Posible Grupo Familiar`.
- Observaciones extensas y datos incompletos pertenecen a previsualizacion de carga diaria/exportacion.
- Alertas/incidencias persistentes se guardan en `EXPEDIENTE_ALERTA`.
- Grupo familiar fase 1 se marca en `EXPEDIENTE_SOLICITUD`.
- Fase 2 de Grupo Familiar (20/07/2026): ademas del flag de Fase 1, ahora existe un ID unico de grupo familiar (tabla `GRUPO_FAMILIAR` + `PERSONA.id_grupo_familiar`, vinculo a nivel persona, no por expediente) para saber que otras personas pertenecen al mismo grupo y que expedientes tienen. Panel "Grupo Familiar" (Registro, lengueta existente "Registrar G.F" evolucionada; Asignacion, lengueta nueva) con tabla de candidatos por apellidos (`GrupoFamiliarHeuristicaService`) + boton de asociacion + tabla de solo lectura "Grupo familiar actual". No se empatan expedientes entre si (no usa `EXPEDIENTE_RELACION`); solo se vincula a la persona titular y se marca "Si" en Grupo Familiar.

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
- Titulo: `Panel de Registro`.
- Debajo del titulo mostrar titular en azul.
- Informativo, sin botones.
- Bloques: `Datos del plazo`, `Datos del expediente`, `Datos del acta`, `Datos de solicitud`, `Datos del titular`, `Datos del solicitante`, `Datos de Notificacion y Ubicacion`.
- `Datos del plazo` muestra `Dias` como pill y `Fecha Vencimiento`.
- `Registrar G.F` es lengueta/panel contextual por seleccion de casillas, no pestana superior.

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

Asociacion:

- Confirmar relacion solo por coincidencia normalizada `numero de acta + titular`, salvo reglas futuras explicitas.
- Principal canonico: primero expediente con numero SDRERC; si ambos tienen o ninguno tiene numero, el mas antiguo.
- Duplicado asociado hereda numero de expediente, fecha de vencimiento, equipo/abogado cuando corresponda.
- UX de asociacion (20/07/2026): en Asignacion ("Solicitudes asociadas") y en Registro ("Asociar duplicados"), si el expediente que el usuario tiene enfocado/seleccionado no tiene numero pero entre las coincidencias marcadas hay exactamente una CON numero, la interfaz detecta e invierte automaticamente cual es el principal real (muestra su numero en negrita en "Expediente principal" y lo usa como destino real de la asociacion), sin que el usuario tenga que adivinar cual fila seleccionar primero. Si hay mas de una marcada con numero, no se decide solo: se bloquea con mensaje pidiendo dejar marcado unicamente el principal. La columna de numero en las tablas de candidatos muestra "Sin número (potencial duplicado)" en vez de "-".
- Propagacion de estado (20/07/2026): desde Asignacion en adelante (Asignacion, Analisis, Verificacion, Ejecucion, Notificacion, Cierre), cada cambio de etapa/estado del expediente principal se propaga automaticamente a todos sus expedientes asociados activos (mismo numero SDRERC), en la misma transaccion. Implementado en `ExpedienteEstadoPropagacionDAO.propagarEstadoAAsociados(...)`, invocado desde el `actualizarExpediente` de cada DAO de transicion. Reemplaza el paso manual de "Recibir documento asociado" en Analisis (el boton sigue existiendo como fallback, pero normalmente el asociado ya llega recibido junto con el principal). No se propaga responsable/equipo, solo etapa/estado.
- Antes de crear una relacion nueva, `ExpedienteRelacionadoDAO.asociarRelacionados` resuelve el `id_expediente_principal` recibido contra el grafo de `EXPEDIENTE_RELACION` ya existente (`resolverPrincipalCanonico`): si ese expediente ya es el "relacionado" (asociado) de otro principal activo, usa ese principal real en vez del expediente recibido. Evita que un expediente ya asociado se use como principal de una nueva asociacion y parta el grupo en 2 (bug reportado con SDRERC-EXP-2026-000179, corregido 20/07/2026); relevante porque el duplicado hereda el numero SDRERC del principal, asi que el criterio "primero el que tiene numero" ya no alcanza para desempatar una vez asociado.
- Al resolver duplicidad, desactivar/atender alerta `Potencial duplicado` en BD.
- En bandejas jerarquicas, asociado muestra su propia alerta funcional cuando corresponda.
- UI no debe usar `padre` ni `hijo`; usar `expediente principal`, `expediente asociado` o `relacion confirmada`.

Panel derecho Asignacion:

- Lenguetas: `Datos`, `Asignacion`, `Asociar`.
- La primera seleccion activa lengueta; segundo clic puede expandir/restaurar segun patron vigente.
- `Datos` reutiliza contenido/estilo de Registro con titulo `Panel de Asignacion` y titular debajo.
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

- Plantillas Word viven bajo `docs/plantillas`.
- La descarga/relleno de plantilla debe reemplazar variables como titular, DNI, solicitante, acta, etc., con datos de la solicitud.
- Las variables usan el formato `#nomVariable#` (camelCase) dentro del Word; el listado completo por plantilla vive en `docs/arquitectura_app/variables_plantillas_word.md`.
- Al generar una RESOLUCION, la plantilla puede autocompletar `#numDocInforme#`/`#fechaDocInforme#` con los datos del documento analizado tipo `INFORME` mas reciente (activo, mayor `fecha_documento`) del mismo expediente; si no existe informe, esas variables quedan vacias. La logica vive en `AnalisisPlantillaDocumentoService`, no en el JPanel.
- Clasificacion de negocio (`TIPO_DOCUMENTO_ADJUNTO.CLASIFICACION`): cartas finales y resoluciones -> `FINAL`; cartas intermedias y oficios -> `INTERMEDIO`; informes -> sin clasificar (`NULL`). Detalle completo en `docs/arquitectura_app/variables_plantillas_word.md`.

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

- Solo resoluciones pasan a Ejecucion.
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
- Grilla de documentos usa las mismas columnas que Analisis, filtrada a documentos `Emitido`/`Resolucion`; permite `+Documento`/`+Relacionado` y descarga Word.
- Bloque `Resultado de ejecucion` incluye `Fecha Ejecucion` con calendario condicional segun el resultado elegido.
- Catalogo de resultado de ejecucion (`OREC`) sembrado por `56_agregar_resultado_ejecucion_orec.sql`.

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
- Panel derecho oculto por defecto (doble clic para abrir), con lenguetas `Datos`, `Asignacion` y `Firma`, mismo patron visual que el Panel de Asignacion de Asignacion (incluye `Habilitar reasignacion`, `Hoja de envio nueva` vs `Hoja de envio actual`, historial de asignaciones/reasignaciones).
- Enrutamiento por clasificacion de documento: `INTERMEDIO` se asigna al equipo `EQ_NOTIFICACION` (abogados de notificacion); `FINAL` se asigna al equipo `EQ_VALIDACION` (validadores). No se permite mezclar clasificaciones en una misma asignacion, ni asignar al equipo que no corresponde.
- Ciclo de un documento `FINAL`: En despacho -> Bandeja Asignacion (asignar validador) -> Bandeja Validacion (Aprobado/Observado) -> Aprobado reaparece en Bandeja Asignacion con lengueta `Firma` habilitada (pasa de Validado a Emitido, con numero de documento y fecha) -> Bandeja Notificacion.
- Ciclo de un documento `INTERMEDIO`: ya llega Emitido desde Verificacion; la lengueta `Asignacion` no aplica (deshabilitada) y `Firma` solo permite corregir numero/fecha si hace falta; ya es visible en Bandeja Notificacion sin accion adicional.
- KPIs propios en la bandeja, con el mismo patron de filtros compactos de tres filas que Registro/Asignacion.
- Historial de asignaciones/reasignaciones de Notificacion se guarda en la tabla generica `EXPEDIENTE_HISTORIAL` (no una tabla nueva), con `tipo_movimiento` `ASIGNACION_NOTIFICACION`/`REASIGNACION_NOTIFICACION` (`58_tipo_movimiento_notificacion.sql`).

Bandeja Validacion (implementado):

- Sin checkbox ni `+`; solo doble clic para abrir el panel derecho.
- Panel derecho oculto por defecto, con lenguetas `Datos` y `Validar`.
- `Validar` incluye la grilla de documentos del expediente (editable en `Estado`) y el bloque `Resultado de validacion`: `Aprobado`/`Observado` (catalogo `tipo_resultado_validacion`, `57_catalogo_resultado_validacion_notificacion.sql`) con comentario obligatorio si es Observado.
- Marcar `Observado` cambia el documento a estado `OBSERVADO` y limpia la asignacion (equipo/usuario/hoja de envio de notificacion), sin tocar `expediente.id_etapa_actual`/`id_estado_actual`; el abogado responsable (Ejecucion para `FINAL`, Analisis/Verificacion para `INTERMEDIO`) lo ve en su propia grilla de documentos, que ya no filtra por estado.

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

## Permisos (control de acceso)

Estado: implementado y en uso.

- Permisos por rol, no por equipo; equipo es una dimension de negocio/alcance de datos (a quien se asigna trabajo), no de control de acceso a pantallas.
- Dos niveles de permiso: modulo (boton del menu lateral) y bandeja (pestana superior dentro de un modulo). No hay permisos a nivel de panel/lengueta interna (`Datos`/`Asignacion`/`Firma`, etc.): son facetas de la misma tarea dentro de una bandeja ya autorizada, no funciones independientes.
- Tablas `permiso` y `rol_permiso` ya existian (`07_tablas_fase_2.sql`) pero estaban vacias/sin uso en tiempo de ejecucion; sembradas por `59_catalogo_permisos_menu.sql` (modulo) y `60_catalogo_permisos_bandejas.sql` (bandeja, solo en Registro/Recepcion, Asignacion y Notificacion, que tienen mas de una pestana superior). Ambos scripts otorgan todos los permisos a `ADMIN_SISTEMA`; la asignacion al resto de roles se configura desde Administracion > Roles > `Permisos del rol`.
- `SessionContext.setPermisos(...)`/`tienePermiso(codigo)` es fail-open: si el catalogo de permisos resuelto para la sesion esta vacio, `tienePermiso` retorna `true` (no bloquea nada hasta que un admin configure permisos reales por rol).
- `MenuPrincipalV2.resolverPermisosSesion()` puebla `SessionContext` via `PermisoRolService` antes de construir el menu; oculta botones de modulo sin permiso.
- Bandejas sin permiso se deshabilitan con `tabs.setEnabledAt(indice, false)` (no se eliminan del `JTabbedPane`): `JPanelRegistroRecepcionV2`, `JPanelAsignacionV2` y `JPanelNotificacionV2` tienen logica interna que asume indices fijos de pestana (comparaciones `getSelectedIndex()==N`); remover pestanas correria el riesgo de desalinear esa logica.

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
