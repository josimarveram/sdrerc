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
- Usar `/resume`, `/compact` y `/review` cuando ayuden a conservar continuidad y ahorrar contexto/tokens.
- No repetir contexto largo salvo que sea necesario para decidir, validar o explicar un bloqueo.
- Antes de pedir contexto al usuario, revisar archivos locales relevantes del proyecto.

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

## 4.1 Estado actual de modulos SDRERC V2

Modulos V2 ya incorporados o en uso dentro de la app nueva:

- Inicio.
- Bandeja de Expedientes.
- Registro / Recepcion.
- Asignacion.
- Analisis.
- Verificacion.
- Firma / Emision.
- Ejecucion.
- Notificacion.
- Publicacion.
- Expediente digital.
- Cierre / Archivo.
- Administracion / Usuarios.
- Administracion / Equipo Juridico.
- Administracion / Roles.

Reglas por modulo:

- `MenuPrincipalV2` es el punto de integracion visual de modulos V2; no usar el menu legacy para nuevas entradas.
- La Bandeja de Expedientes no debe mostrar `V2` en el titulo ni en el nombre visual del modulo.
- `Ver detalle` en bandejas operativas debe abrir la consola unica `DlgConsolaExpedienteV2`, no crear consolas paralelas.
- Los modulos administrativos `Roles`, `Usuarios` y `Equipo Juridico` no deben mostrar columnas `Creado` ni `Modificado` en sus listados principales, salvo que el usuario lo pida explicitamente.
- Los modulos V2 deben evitar bloques de cabecera duplicados dentro del panel cuando `MenuPrincipalV2` ya muestra titulo y subtitulo. Si el panel interno necesita un bloque superior, debe aportar contexto operativo nuevo y no repetir titulo/subtitulo.
- En los bloques superiores de `Registro / Recepcion`, `Asignacion`, `Analisis`, `Verificacion`, `Firma / Emision`, `Ejecucion`, `Notificacion`, `Publicacion`, `Expediente digital`, `Cierre / Archivo` y `Bandeja de Expedientes`, usar textos descriptivos que definan el modulo y su proposito operativo; evitar textos genericos o repetidos.

## 4.2 Escritura controlada ya autorizada en V2

Por defecto V2 sigue siendo lectura/consulta. Las escrituras reales solo son validas dentro de los modulos ya autorizados y con DAO/Service transaccional:

- `Asignacion`: asignar expedientes `REGISTRO / REGISTRADO` hacia `ASIGNACION / ASIGNADO`, registrar historial y evitar doble asignacion.
- `Asignacion`: detectar posibles relacionados solo de forma visual; insertar en `EXPEDIENTE_RELACION` unicamente cuando el usuario confirma la asociacion. No fusionar expedientes.
- `Analisis`: recibir expedientes `ASIGNACION / ASIGNADO`, registrar evaluacion, observaciones y documentos analizados, enviar a verificacion, derivar a notificacion en rutas especiales y archivar no corresponde si el flujo `SDRERC_TO_BE` lo permite.
- `Analisis`: la derivacion externa requiere entidad destino, tipo de derivacion y datos documentales; si esa estructura funcional no esta completa en el modulo, debe mostrarse como accion preparada/bloqueada con diagnostico, sin escritura parcial.
- `Verificacion`: consultar expedientes en `VERIFICACION`, revisar analisis y documentos, aprobar verificacion, observar, marcar documento inconsistente, devolver a Analisis y enviar a `FIRMA_EMISION / PARA_FIRMA` si el flujo `SDRERC_TO_BE` lo permite.
- `Verificacion`: no crear tabla paralela de verificacion si el modelo no la define; usar `EXPEDIENTE`, `EXPEDIENTE_HISTORIAL`, `EXPEDIENTE_OBSERVACION`, `EXPEDIENTE_EVALUACION` y `EXPEDIENTE_DOCUMENTO_ANALIZADO` segun corresponda.
- `Verificacion`: las acciones autorizadas deben resolver transiciones reales por codigo, como `APROBACION_VERIFICACION`, `ENVIO_FIRMA`, `REGISTRO_OBSERVACION_VERIFICACION`, `REVERSION_ESTADO_DOCUMENTO` y `DEVOLUCION_A_ANALISIS`; si falta una transicion, catalogo, evaluacion o documento requerido, bloquear con diagnostico sin escritura parcial.
- `Firma / Emision`: consultar expedientes en `FIRMA_EMISION`, revisar analisis, verificacion, documentos y observaciones, registrar firma, registrar emision, registrar numero de resolucion/documento y enviar a `EJECUCION / EN_EJECUCION` si el flujo `SDRERC_TO_BE` lo permite.
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
- `Notificacion`: el cierre desde `NOTIFICACION / NOTIFICADO` hacia `CIERRE_ARCHIVO / CERRADO` debe marcar el expediente como cerrado cuando el modelo lo soporte, registrar historial y nunca eliminar datos fisicamente.
- `Publicacion`: consultar expedientes en `PUBLICACION_CONDICIONAL`, revisar resolucion/documento, notificacion previa, cargo de acuse, documentos, historial, observaciones y expedientes asociados.
- `Publicacion`: registrar datos de publicacion, marcar publicacion registrada y cerrar expediente publicado solo cuando el flujo `SDRERC_TO_BE` exponga una transicion real activa.
- `Publicacion`: usar las tablas reales `EXPEDIENTE_PUBLICACION`, `EXPEDIENTE_NOTIFICACION`, `EXPEDIENTE_CARGO_ACUSE`, `EXPEDIENTE_HISTORIAL`, `EXPEDIENTE_RESOLUCION` y `EXPEDIENTE` segun corresponda; no crear tablas paralelas ni guardar datos no soportados por el modelo.
- `Publicacion`: las acciones autorizadas deben resolver transiciones reales por codigo, como `REGISTRO_PUBLICACION` y `CIERRE`; si falta transicion, catalogo, documento requerido, tabla, columna o constraint, bloquear con diagnostico sin escritura parcial.
- `Publicacion`: no implementar publicacion real en portales externos ni integraciones externas sin autorizacion explicita; el modulo registra metadata y trazabilidad funcional, no publicaciones externas.
- `Publicacion`: el cierre desde `PUBLICACION_CONDICIONAL / PUBLICACION_REGISTRADA` hacia `CIERRE_ARCHIVO / CERRADO` debe marcar el expediente como cerrado cuando el modelo lo soporte, registrar historial y nunca eliminar datos fisicamente.
- `Expediente digital`: consultar expedientes en `EXPEDIENTE_DIGITAL`, revisar documentos, resolucion/documento, notificacion/publicacion si existe, historial, observaciones y expedientes asociados.
- `Expediente digital`: registrar o actualizar metadata de carpeta/ruta/enlace digital y marcar completitud digital solo mediante DAO/Service transaccional y cuando el flujo `SDRERC_TO_BE` exponga una transicion real activa.
- `Expediente digital`: usar las tablas reales `EXPEDIENTE_DIGITAL`, `EXPEDIENTE`, `EXPEDIENTE_DOCUMENTO`, `EXPEDIENTE_DOCUMENTO_ANALIZADO`, `EXPEDIENTE_HISTORIAL`, `EXPEDIENTE_RESOLUCION`, `EXPEDIENTE_NOTIFICACION` y `EXPEDIENTE_PUBLICACION` segun corresponda; no crear tablas paralelas ni guardar datos no soportados por el modelo.
- `Expediente digital`: las acciones autorizadas deben resolver transiciones reales por codigo, como `CREACION_CARPETA_EXPEDIENTE_DIGITAL` y `CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL`; si `REGISTRO_LINK_EXPEDIENTE_DIGITAL` existe como catalogo pero no como transicion activa, usar la transicion real configurada y reportar el diagnostico.
- `Expediente digital`: al marcar completo, actualizar `EXPEDIENTE.EXPEDIENTE_DIGITAL_COMPLETO` cuando el modelo lo soporte, validar documentos o metadata requerida, registrar historial y no hacer escritura parcial si falta tabla, columna, catalogo, transicion o constraint.
- `Expediente digital`: no mover archivos fisicamente, no eliminar archivos, no implementar carga masiva documental ni integraciones externas con NAS, SharePoint, Drive, MinIO u otros repositorios sin autorizacion explicita.
- `Cierre / Archivo`: consultar expedientes en `CIERRE_ARCHIVO` y expedientes candidatos con acciones activas `CIERRE` o `ARCHIVO`, revisar antecedentes completos, documentos, resolucion, notificacion, publicacion, expediente digital, historial y expedientes asociados.
- `Cierre / Archivo`: registrar cierre hacia `CIERRE_ARCHIVO / CERRADO` y archivo hacia `CIERRE_ARCHIVO / ARCHIVADO` solo cuando el flujo `SDRERC_TO_BE` exponga transicion real activa.
- `Cierre / Archivo`: usar las tablas reales `EXPEDIENTE`, `EXPEDIENTE_HISTORIAL`, `EXPEDIENTE_OBSERVACION`, `EXPEDIENTE_RESOLUCION`, `EXPEDIENTE_NOTIFICACION`, `EXPEDIENTE_PUBLICACION`, `EXPEDIENTE_DIGITAL` y `EXPEDIENTE_DERIVACION_EXTERNA` segun corresponda; no crear tablas paralelas ni guardar datos no soportados por el modelo.
- `Cierre / Archivo`: las acciones autorizadas deben resolver transiciones reales por codigo, como `CIERRE` y `ARCHIVO`; si falta transicion, catalogo, documento requerido, tabla, columna o constraint, bloquear con diagnostico sin escritura parcial.
- `Cierre / Archivo`: marcar `EXPEDIENTE.CERRADO` o `EXPEDIENTE.ARCHIVADO` cuando el modelo lo soporte, registrar historial/movimiento y nunca eliminar fisicamente expedientes, documentos ni archivos.
- `Cierre / Archivo`: la derivacion externa pendiente puede mostrarse en consulta/preparacion; no registrar derivaciones externas desde este modulo salvo autorizacion explicita y estructura completa de entidad destino, tipo de derivacion y documento soporte.
- `Roles`: crear, editar, activar e inactivar roles. Nunca eliminar fisicamente roles.
- `Usuarios`: crear, editar, activar e inactivar usuarios, y asociar roles/equipo si el modelo lo permite. Nunca mostrar ni guardar passwords en texto plano.
- `Equipo Juridico`: crear, editar, activar e inactivar equipos, y gestionar miembros/supervisor si el modelo lo permite. Nunca eliminar fisicamente equipos ni usuarios.

Toda escritura V2 autorizada debe:

- Resolver IDs por codigo o catalogo, no hardcodearlos.
- Validar estado actual antes de escribir para prevenir cambios concurrentes.
- Usar transaccion completa con commit/rollback.
- Registrar historial/movimiento cuando el modelo lo soporte.
- Bloquear la accion y reportar diagnostico exacto si falta tabla, columna, catalogo, transicion o constraint.

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
- El Home debe evitar scroll horizontal global. Las metricas, accesos rapidos, flujo operativo y modulos principales deben reacomodarse segun el ancho disponible.
- El bloque `Flujo operativo` del Home debe verse como una secuencia premium de etapas, no como una lista plana. Debe mantener flechas o conectores visibles, tarjetas/chips de etapa, numeracion o jerarquia clara, y nombres amigables.
- El flujo operativo puede usar colores sobrios por etapa para mejorar lectura, siempre dentro de la paleta institucional. Evitar colores brillantes, saturados o ajenos al estilo SDRERC.
- El flujo operativo debe usar estas etapas amigables y en este orden: Registro, Asignacion, Analisis, Verificacion, Firma / Emision, Ejecucion, Notificacion, Publicacion, Expediente digital, Cierre / Archivo.
- El flujo operativo no debe mostrar codigos tecnicos como `FIRMA_EMISION`, `PUBLICACION_CONDICIONAL`, `EXPEDIENTE_DIGITAL` o `CIERRE_ARCHIVO`.
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
- En bandejas operativas, ubicar `Dias` al inicio como indicador numerico/badge y mostrar solo el numero, sin repetir la palabra `dias` en cada celda.
- En grillas principales de expedientes, no mostrar `Responsable`, `Abogado inicial`, `Abogado` ni `Ultimo mov.` salvo necesidad funcional explicita del modulo o pedido del usuario; esos datos deben quedar disponibles en la consola/detalle cuando correspondan.
- Las columnas principales de bandeja deben priorizar gestion operativa: `Dias`, expediente, tramite/documento, titular, procedimiento, etapa, estado y alertas/asociados cuando apliquen.
- Las columnas de etapa, estado, plazo, publicacion, digital, observacion y asociados deben mostrarse con badges sobrios cuando el componente lo permita.
- `Asignacion` es el primer patron base visual para modulos operativos SDRERC V2. Los siguientes modulos operativos deben tender a la estructura: cards superiores, buscador/filtros ancho, zona operativa con tabla izquierda y panel derecho de accion/contexto.
- Los buscadores de modulos operativos deben usar campo amplio, placeholder claro, botones alineados y comportamiento responsive desktop; evitar campos cortos o controles comprimidos.
- Los paneles derechos operativos deben usar fondo blanco, borde suave, padding, secciones internas, scroll vertical sin scroll horizontal y acciones al pie.
- En modulos operativos con panel derecho de accion contextual, el panel debe estar oculto inicialmente si no hay seleccion operativa; la grilla/listado debe ocupar todo el ancho disponible y el panel debe aparecer solo al seleccionar uno o mas expedientes, salvo que el modulo requiera contexto permanente.
- Si el usuario cierra manualmente un panel derecho contextual con seleccion activa, no limpiar la seleccion; el panel debe reabrirse cuando el usuario vuelva a interactuar con la fila o la seleccion operativa.
- En modulos operativos con panel derecho contextual, el panel puede implementarse como redimensionable mediante divisor horizontal cuando el formulario requiera mas espacio; no imponer porcentajes maximos fijos si el usuario necesita ampliar el panel hasta el ancho disponible, y evitar superposiciones o scroll horizontal innecesario.
- El panel derecho contextual redimensionable debe expandir internamente combos, textareas, secciones y acciones segun el ancho disponible, manteniendo comportamiento responsive desktop para distintas resoluciones.
- Antes de duplicar layout manual en un modulo operativo, preferir componentes reutilizables `AppV2SearchToolbar`, `AppV2TableSectionPanel`, `AppV2SideActionPanel` y `AppV2SideSectionPanel` cuando apliquen.

## 4.6 Alineamiento BPMN TO BE V2

- El archivo `docs/arquitectura_bd/TO BE V2.bpmn` es referencia funcional para validar el flujo operativo SDRERC V2, junto con `SDRERC_APP`.
- El BPMN TO BE V2 no autoriza crear una etapa visual `VALIDACION`; las validaciones del BPMN se implementan como acciones, observaciones, evaluaciones, estados o reglas dentro de las macroetapas existentes.
- Los actores BPMN externos como OGD, SDPRC y Ciudadano/Entidad no deben convertirse automaticamente en usuarios, equipos o modulos internos V2 salvo autorizacion explicita.
- La app V2 mantiene estas macroetapas como estructura visual principal: Registro, Asignacion, Analisis, Verificacion, Firma / Emision, Ejecucion, Notificacion, Publicacion, Expediente digital y Cierre / Archivo.
- Los ajustes de flujo derivados del BPMN deben aplicarse como scripts idempotentes correlativos en `db/sdrerc_app/scripts/`, sin `DROP`, `DELETE` ni `TRUNCATE`, y sin modificar expedientes transaccionales.
- En `SDRERC_APP`, `estado_expediente.codigo` es unico globalmente. No duplicar el mismo codigo de estado por etapa; si una accion reutiliza un estado en mas de una etapa, resolverlo por la transicion activa y documentar el criterio.
- `Analisis` debe soportar `REGISTRO_RESULTADO_ANALISIS` desde `RECIBIDO_POR_ABOGADO`, `OBSERVADO` y `SUBSANADO` hacia `ATENDIDO`, `OBSERVADO`, `NO_CORRESPONDE`, `EN_ABANDONO` y `OBSERVACION_ADMINISTRATIVA`, siempre mediante transicion activa.
- `Ejecucion` debe soportar `INICIO_EJECUCION` desde `EJECUCION / EN_EJECUCION` hacia `EJECUCION / EJECUTADO`, con historial y validacion de resolucion/documento cuando el modulo lo requiera.
- Las autorizaciones de flujo por rol/equipo deben resolverse por codigo en `flujo_transicion_rol` y `flujo_transicion_equipo`, sin IDs hardcodeados.
- Si el BPMN introduce tareas mas detalladas que el modelo actual no representa como modulo propio, mapearlas primero a la macroetapa existente y reportar cualquier brecha antes de crear tablas, etapas o pantallas nuevas.

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
- Cierre / Archivo.

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
- `CIERRE_ARCHIVO` -> `Cierre / Archivo`

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
