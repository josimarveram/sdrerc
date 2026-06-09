Necesito ejecutar una fase integral de estabilización funcional y visual
en SDRERC V2 para corregir inconsistencias detectadas en todos los
módulos.

Objetivo:\
Revisar y corregir de forma transversal:

- combos que no cargan datos;

- catálogos faltantes;

- datos incompletos en detalle/consola;

- titular/remitente/acta/procedimiento que no aparecen donde deberían;

- columnas demasiado cortas;

- textos truncados sin tooltip;

- tablas con mala distribución;

- paneles que no muestran información ya disponible;

- inconsistencias entre bandejas, detalle y módulos operativos;

- actualización automática de AGENTS.md con reglas/decisiones nuevas al
  finalizar.

Antes de modificar código o datos, lee obligatoriamente:

- AGENTS.md actualizado.

- docs/arquitectura_app/\*.md relevantes.

- docs/arquitectura_bd/PROMPT_REESTRUCTURACION_BD_SDRERC.md.

- docs/arquitectura_bd/er_sdrerc_app.png.

- db/sdrerc_app/scripts/\*.sql.

- src/main/java/com/sdrerc/ui/appv2/MenuPrincipalV2.java

- src/main/java/com/sdrerc/ui/appv2/theme/AppV2Theme.java

- src/main/java/com/sdrerc/ui/appv2/components/\*

- src/main/java/com/sdrerc/ui/views/expedienteconsola/

- src/main/java/com/sdrerc/ui/views/registrorecepcion/

- src/main/java/com/sdrerc/ui/views/asignacion/

- src/main/java/com/sdrerc/ui/views/analisis/

- src/main/java/com/sdrerc/ui/views/verificacion/

- src/main/java/com/sdrerc/ui/views/firmaemision/

- src/main/java/com/sdrerc/ui/views/ejecucion/

- src/main/java/com/sdrerc/ui/views/notificacion/

- src/main/java/com/sdrerc/ui/views/publicacion/

- src/main/java/com/sdrerc/ui/views/expedientedigital/

- src/main/java/com/sdrerc/ui/views/cierrearchivo/

- src/main/java/com/sdrerc/ui/views/administracion/

- DAOs, Services y DTOs existentes de SDRERC_APP.

Contexto:\
El sistema SDRERC V2 ya tiene incorporados los módulos principales, pero
se han detectado inconsistencias:

- varios combos aparecen vacíos;

- algunos módulos no cargan catálogos;

- el detalle del expediente no muestra datos del titular u otros campos
  que ya deberían venir desde BD;

- algunas bandejas no muestran correctamente procedimiento, acta,
  titular, responsable o estado;

- las columnas se ven muy cortas y no permiten leer la información;

- algunos textos aparecen truncados sin tooltip;

- faltan ajustes de consistencia entre bandeja, consola y módulos por
  etapa.

Reglas generales:

- Respeta AGENTS.md actualizado.

- Este incremento autoriza diagnóstico integral de lectura sobre BD
  SDRERC_APP.

- Este incremento autoriza insertar o completar datos maestros/catálogos
  faltantes cuando sea estrictamente necesario para que combos
  funcionen, siempre con scripts idempotentes y controlados.

- Este incremento autoriza ejecutar SQL de diagnóstico y SQL de
  poblamiento de datos maestros/catálogos, no autoriza cambios
  destructivos.

- No usar DROP, DELETE ni TRUNCATE.

- No modificar datos transaccionales de expedientes salvo que exista un
  bug evidente de datos de prueba y se justifique antes.

- No tocar legacy.

- No tocar OracleConnection.java.

- No poner SQL en JPanel.

- UI llama a Service.

- Service llama a DAO.

- Toda consulta o escritura de aplicación debe estar encapsulada en
  DAO/Service.

- Si se crean scripts SQL, deben ser idempotentes.

- Si se ejecutan scripts SQL, no imprimir passwords.

- Mantener Java 8 + Swing + FlatLaf.

- Aplicar build, commit y push según AGENTS.md.

- Al final de la tarea, actualizar automáticamente AGENTS.md con las
  reglas/decisiones nuevas detectadas, sin que el usuario tenga que
  pedirlo.

Autorización explícita de BD para esta tarea:\
Queda autorizado:

- ejecutar SELECTs de diagnóstico sobre SDRERC_APP;

- validar tablas de catálogo;

- validar datos maestros;

- validar vistas;

- validar joins de
  expediente/titular/acta/procedimiento/estado/etapa/responsable;

- crear scripts SQL idempotentes para poblar catálogos faltantes;

- ejecutar scripts idempotentes de poblamiento de catálogos cuando sean
  necesarios para corregir combos vacíos;

- agregar validaciones SELECT al final del script.

No queda autorizado:

- borrar datos;

- truncar tablas;

- alterar estructura sin diagnóstico y justificación;

- crear tablas nuevas salvo bloqueo grave y autorización posterior;

- modificar expedientes reales sin autorización adicional;

- imprimir credenciales;

- tocar conexión legacy.

Alcance 1: Auditoría de combos y catálogos

Revisar todos los módulos donde hay combos:

- Registro / Recepción.

- Registro manual.

- Asignación.

- Análisis.

- Verificación.

- Firma / Emisión.

- Ejecución.

- Notificación.

- Publicación.

- Expediente digital.

- Cierre / Archivo.

- Usuarios.

- Equipo Jurídico.

- Roles.

- Bandeja de Expedientes.

Identificar:

- combo vacío;

- combo con código técnico en vez de nombre amigable;

- combo que no llama al service correcto;

- combo que usa tabla equivocada;

- combo que debería filtrar activos;

- combo que debería excluir valores no aplicables;

- combo que no se refresca;

- combo que falla silenciosamente.

Revisar especialmente:

- tipo procedimiento;

- tipo documento;

- canal de ingreso;

- tipo acta;

- estado;

- etapa;

- equipo destino;

- abogado responsable;

- supervisor;

- resultado de análisis;

- resultado de verificación;

- modalidad de notificación;

- estado de cargo;

- resultado de publicación;

- estado de expediente digital;

- motivo de cierre/archivo;

- roles;

- usuarios;

- equipos;

- áreas.

Resultado esperado:

- Diagnóstico de combos vacíos.

- Causa exacta: falta de datos, error de DAO, error de service, filtro
  incorrecto, columna incorrecta, tabla sin registros, código
  hardcodeado, etc.

- Corrección aplicada.

- Si falta dato maestro, crear script idempotente y ejecutarlo.

- Si falta estructura, reportar bloqueo y no inventar.

Alcance 2: Auditoría de detalle/consola del expediente

Revisar DlgConsolaExpedienteV2 y componentes relacionados.

Validar que el detalle del expediente cargue correctamente:

- número expediente;

- número trámite;

- procedimiento;

- tipo documento;

- canal de ingreso;

- fecha recepción;

- etapa actual;

- estado actual;

- responsable actual;

- equipo;

- titular;

- tipo documento titular;

- número documento titular;

- remitente;

- tipo documento remitente;

- número documento remitente;

- tipo acta;

- número acta;

- resolución/documento si existe;

- notificación si existe;

- publicación si existe;

- expediente digital si existe;

- historial;

- observaciones;

- documentos;

- expedientes asociados.

Problema importante:\
Se detectó que datos como titular u otros campos no están cargando en
detalle aunque ya deberían estar disponibles.

Acciones:

- Revisar queries/DAOs de detalle.

- Corregir joins hacia persona, expediente_persona, expediente_acta,
  expediente_solicitud, procedimiento, estado, etapa y documentos.

- No duplicar SQL en UI.

- Si faltan DTOs, agregarlos.

- Si faltan campos en DTO, agregarlos.

- Si existen datos en BD pero no se muestran, corregir DAO/Service/UI.

- Si no existen datos en BD, diagnosticar si viene de registro
  manual/carga diaria/asignación y reportar.

Alcance 3: Auditoría de bandejas y columnas

Revisar todas las bandejas/listados:

- Bandeja de Expedientes.

- Bandeja Registro.

- Asignación.

- Análisis.

- Verificación.

- Firma / Emisión.

- Ejecución.

- Notificación.

- Publicación.

- Expediente digital.

- Cierre / Archivo.

- Usuarios.

- Equipo Jurídico.

- Roles.

Corregir:

- columnas demasiado cortas;

- encabezados truncados;

- celdas truncadas sin tooltip;

- mala distribución de anchos;

- columnas importantes ocultas;

- columna titular demasiado corta;

- columna procedimiento demasiado corta;

- columna expediente demasiado corta;

- columna estado demasiado corta;

- columna etapa demasiado corta;

- columnas técnicas innecesarias visibles;

- textos técnicos donde debería haber nombres amigables.

Reglas visuales:

- Usar anchos mínimos razonables.

- Usar preferredWidth por columna importante.

- Usar tooltips en celdas truncadas.

- Mantener tablas legibles.

- No saturar la tabla con demasiadas columnas.

- Si hay muchas columnas, priorizar:

  - expediente;

  - trámite;

  - procedimiento;

  - titular;

  - acta;

  - etapa;

  - estado;

  - responsable;

  - plazo/días;

  - alertas.

- Mover detalles secundarios a la consola, no necesariamente a la
  bandeja.

Resultado esperado:

- Tablas más legibles.

- Texto importante visible.

- Tooltips para valores largos.

- Columnas con ancho coherente.

- Sin romper consultas.

Alcance 4: Auditoría de nombres amigables

Revisar que en UI no se muestren códigos técnicos cuando exista nombre
amigable.

Corregir:

- REGISTRO -\> Registro

- ASIGNACION -\> Asignación

- ANALISIS -\> Análisis

- VERIFICACION -\> Verificación

- FIRMA_EMISION -\> Firma / Emisión

- EJECUCION -\> Ejecución

- NOTIFICACION -\> Notificación

- PUBLICACION_CONDICIONAL -\> Publicación

- EXPEDIENTE_DIGITAL -\> Expediente digital

- CIERRE_ARCHIVO -\> Cierre / Archivo

Estados:

- EN_VERIFICACION -\> En verificación

- PARA_FIRMA -\> Para firma

- EN_EJECUCION -\> En ejecución

- EN_NOTIFICACION -\> En notificación

- PENDIENTE_PUBLICACION -\> Pendiente de publicación

- EXPEDIENTE_DIGITAL_COMPLETO -\> Expediente digital completo

Si existe helper o mapper de nombres amigables, reutilizarlo.\
Si no existe, crear uno reusable:

- AppV2DisplayNameMapper\
  o nombre equivalente.

Alcance 5: Auditoría de datos maestros mínimos

Validar datos maestros mínimos requeridos por los módulos.

Revisar tablas/catálogos existentes para:

- procedimiento registral;

- tipo documento;

- canal recepción;

- tipo acta;

- unidad orgánica;

- etapa;

- estado;

- movimiento;

- resultado análisis;

- resultado verificación;

- modalidad notificación;

- resultado notificación;

- estado cargo acuse;

- estado publicación;

- estado expediente digital;

- motivo cierre/archivo;

- roles;

- equipos;

- usuarios de prueba si aplica.

Si faltan catálogos necesarios:

- crear script nuevo idempotente con número correlativo siguiente en:\
  db/sdrerc_app/scripts/

- nombre sugerido:\
  XX_datos_maestros_estabilizacion_v2.sql

- usar MERGE o lógica idempotente.

- incluir SELECTs de validación.

- ejecutar el script si es necesario para corregir combos vacíos.

- no usar DROP, DELETE ni TRUNCATE.

- no imprimir credenciales.

Alcance 6: Auditoría de Registro Manual y carga diaria

Validar que lo registrado por:

- Registro manual;

- Carga diaria;

genere datos suficientes para que luego aparezcan en:

- Bandeja de Expedientes;

- Bandeja Registro;

- Detalle/Consola;

- Asignación;

- Análisis.

Revisar:

- titular;

- remitente;

- acta;

- solicitud;

- procedimiento;

- documento inicial;

- estado inicial;

- etapa inicial;

- historial inicial.

Si el problema es que Registro manual o carga diaria no están poblando
una relación necesaria:

- corregir DAO/Service transaccional si está dentro del alcance;

- no modificar datos históricos masivamente sin autorización;

- reportar si requiere script de corrección para datos ya creados.

Alcance 7: Auditoría de errores silenciosos

Buscar patrones donde la UI falla sin mostrar causa:

- catch vacío;

- printStackTrace sin mensaje amigable;

- combos que fallan y quedan vacíos;

- services que retornan lista vacía ante error;

- mensajes genéricos.

Mejorar:

- registrar/loguear causa de forma segura;

- mostrar mensaje amigable cuando un catálogo no carga;

- no mostrar stacktrace al usuario;

- no ocultar errores críticos.

Alcance 8: Componentes reutilizables para tablas

Si no existe, crear utilitario reusable:

- AppV2TableColumnSizer

- AppV2TableTooltipRenderer

- AppV2FriendlyCellRenderer

- AppV2BadgeCellRenderer

Objetivo:

- aplicar anchos mínimos;

- tooltips en celdas;

- nombres amigables;

- badges de etapa/estado si ya existe estilo;

- reducir duplicidad entre módulos.

No refactorizar todos los módulos si se vuelve demasiado grande.\
Priorizar los módulos con problemas visibles:

1.  Bandeja de Expedientes.

2.  Registro / Recepción.

3.  Asignación.

4.  Análisis.

5.  Consola Expediente.

Luego aplicar el mismo patrón a los demás módulos si es seguro.

Alcance 9: Actualización automática de AGENTS.md

Al finalizar esta tarea, actualizar AGENTS.md automáticamente si se
detectan reglas nuevas que deben persistir.

Agregar una sección o ampliar secciones existentes con reglas como:

- Cada incremento de Codex debe actualizar AGENTS.md cuando incorpore
  una regla, decisión arquitectónica, patrón reusable o restricción
  nueva.

- Los combos V2 deben tener diagnóstico de catálogo y mensaje amigable
  si no cargan.

- Las tablas V2 deben usar tooltips y anchos mínimos para columnas
  principales.

- La consola/detalle debe cargar titular, acta, solicitud, etapa,
  estado, responsable e historial cuando existan en BD.

- Los catálogos faltantes deben resolverse con scripts idempotentes y
  validaciones.

- No ocultar errores de carga de catálogos.

- No mostrar códigos técnicos cuando haya nombres amigables.

- La estabilización transversal debe priorizar componentes reutilizables
  antes que fixes aislados.

Importante:

- No duplicar reglas ya existentes.

- Agregar solo reglas nuevas o aclaraciones útiles.

- Incluir AGENTS.md en el commit si se actualiza.

- Desde esta tarea en adelante, cualquier prompt que pida una
  implementación debe terminar actualizando AGENTS.md si se incorporan
  nuevas decisiones o reglas persistentes.

Alcance 10: Orden recomendado de ejecución

Trabajar en este orden:

1.  Diagnóstico de combos vacíos y catálogos.

2.  Diagnóstico de detalle/consola sin titular/datos.

3.  Diagnóstico de tablas/columnas truncadas.

4.  Corrección de DAOs/Services de lectura.

5.  Corrección de UI para mostrar datos ya disponibles.

6.  Script idempotente de datos maestros si falta catálogo.

7.  Ejecución controlada del script si es necesario.

8.  Aplicar anchos/tooltips en tablas prioritarias.

9.  Actualizar AGENTS.md con reglas nuevas.

10. Compilar y empaquetar.

11. Commit y push.

Criterios de aceptación:

Combos:

- Los combos principales cargan datos reales.

- Si un combo no puede cargar, muestra diagnóstico claro y no falla
  silenciosamente.

- No hay IDs hardcodeados nuevos.

- Catálogos faltantes se poblaron con script idempotente si
  correspondía.

Detalle/consola:

- El detalle del expediente muestra titular si existe.

- Muestra acta, procedimiento, etapa, estado y responsable si existen.

- Muestra historial/documentos/observaciones si existen.

- No muestra códigos técnicos innecesarios.

Bandejas/tablas:

- Columnas principales son legibles.

- Textos largos tienen tooltip.

- Titular/procedimiento/estado/etapa no quedan inútilmente cortos.

- No se rompe selección ni acciones.

Arquitectura:

- SQL solo en DAO o scripts autorizados.

- No SQL en JPanel.

- No cambios legacy.

- No cambios OracleConnection.java.

- AGENTS.md actualizado con reglas nuevas.

- Build exitoso.

Compilación:\
Ejecutar:\
mvn clean compile

Si compila:\
mvn clean package

Git:\
Respetar AGENTS.md:

- git status

- no usar git add .

- agregar solo archivos de esta tarea

- incluir AGENTS.md si fue actualizado

- commit si build pasa

- push al branch actual

Mensaje sugerido:\
fix: stabilize SDRERC V2 catalogs details and tables

Entregable final:

- Diagnóstico de combos/catálogos.

- Diagnóstico de detalle/consola.

- Diagnóstico de columnas/tablas.

- Scripts SQL creados/modificados/ejecutados si aplica.

- Archivos Java creados/modificados.

- AGENTS.md actualizado.

- Resumen de correcciones aplicadas.

- Confirmación de que no se tocó legacy.

- Confirmación de que no se tocó OracleConnection.java.

- Confirmación de SQL ejecutado si aplicó, sin credenciales.

- Resultado de mvn clean compile.

- Resultado de mvn clean package.

- Commit creado.

- Push realizado o error exacto.

- Comando de prueba:\
  .\\run-v2.ps1
