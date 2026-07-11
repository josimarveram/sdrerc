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
- `Asociar` incluye la reasignacion de expediente a otro abogado/equipo: exige hoja de envio nueva (no sobrescribe la anterior), desactiva la asignacion vigente conservando su historial y muestra una grilla de historial de asignaciones/reasignaciones del expediente.

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
- Panel derecho al seleccionar fila.
- Botones principales azules institucionales conservando forma/tamano del modulo.
- Nunca eliminar fisicamente roles, usuarios o equipos.
- No mostrar ni guardar passwords en texto plano.
- `Usuarios`: tipo documento debe ser combo basado en catalogo de identidad.

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

Scripts recientes relevantes:

- `27_grupo_familiar_fase1.sql`.
- `29_patch_documento_analizado_respuesta.sql`.
- `30_datos_maestros_plazos_por_procedimiento.sql`.
- `31_datos_maestros_tipos_documento_analisis_plantillas.sql`.
- `32_patch_documento_analizado_numero_documento.sql`.
- `33_patch_documento_analizado_detalle_observacion.sql`.
- `34_ubigeo_notificacion_registro_manual.sql`.
- `37_carga_ubigeo_generada_para_xepdb1.sql`.
- `38_analisis_multiple.sql` existe, pero la regla vigente vuelve a analisis unico.
- `39_patch_documento_analizado_jerarquia.sql`.

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
