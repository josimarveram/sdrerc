Necesito generar dentro del proyecto SDRERC una lista ordenada de
prompts incrementales para terminar los módulos operativos hasta antes
de Publicación, tomando como fuente funcional el archivo:

docs/arquitectura_app/detalle_funciones.md

Muy importante:\
Según AGENTS.md actualizado, el módulo Firma / Emisión ya no debe
manejarse como módulo independiente del menú lateral. Firma / Emisión
queda incorporado dentro del módulo Verificación. Además, la firma no
debe considerarse como etapa visual ni como estado visible
independiente; para el resultado documental se usará el estado "Emitido"
cuando corresponda.

Por lo tanto:

- No generar un prompt separado para Firma / Emisión como módulo
  independiente.

- No pedir creación de menú lateral para Firma / Emisión.

- No pedir etapa visual Firma / Emisión como flujo visible
  independiente.

- Las acciones de firma/emisión/numeración/documento emitido deben
  quedar dentro del prompt de Verificación.

- Usar "Emitido" como estado/documento resultado cuando corresponda.

- Mantener internamente los códigos reales del flujo si existen, pero
  visualmente no exponer Firma / Emisión como módulo separado.

Ruta del proyecto:\
D:\\2026\\FuentesRENIEC\\sdrerc_CODIGOS

Fuente funcional principal:\
docs/arquitectura_app/detalle_funciones.md

Documento de salida requerido:\
docs/arquitectura_app/PLAN_PROMPTS_INCREMENTALES_HASTA_NOTIFICACION.md

Objetivo:\
Crear un documento Markdown con prompts listos para copiar/pegar en
Codex CLI, uno por incremento, para avanzar de forma eficiente y
ordenada en los módulos:

- Registro / Recepción

- Asignación

- Análisis

- Verificación, incluyendo firma/emisión/documento emitido dentro del
  mismo módulo

- Ejecución

- Notificación

No implementar todavía:

- Publicación como módulo completo.

- Expediente digital como incremento principal, salvo lo mínimo que
  Análisis necesite mostrar o registrar según detalle_funciones.md.

- Cierre / Archivo como incremento principal, salvo referencias de
  cierre si el flujo lo exige.

Antes de generar el documento, analiza obligatoriamente:

- AGENTS.md actualizado.

- docs/arquitectura_app/detalle_funciones.md.

- docs/arquitectura_app/\*.md relevantes.

- docs/arquitectura_bd/TO BE V2.bpmn.

- db/sdrerc_app/scripts/\*.sql.

- src/main/java/com/sdrerc/ui/views/registrorecepcion/

- src/main/java/com/sdrerc/ui/views/asignacion/

- src/main/java/com/sdrerc/ui/views/analisis/

- src/main/java/com/sdrerc/ui/views/verificacion/

- src/main/java/com/sdrerc/ui/views/ejecucion/

- src/main/java/com/sdrerc/ui/views/notificacion/

- src/main/java/com/sdrerc/ui/views/expedienteconsola/

- src/main/java/com/sdrerc/ui/appv2/

- Services, DAOs y DTOs relacionados.

Reglas obligatorias:

- Esta tarea es documental y de planificación.

- No implementar código Java todavía.

- No crear scripts SQL todavía.

- No ejecutar SQL.

- No modificar datos.

- No tocar legacy.

- No tocar OracleConnection.java.

- No tocar FrmLogin.java legacy.

- No tocar MenuPrincipal.java legacy.

- No tocar com.sdrerc.Main legacy.

- No cambiar reglas de negocio.

- No inventar transiciones.

- No crear etapa visual VALIDACION.

- No volver a crear Firma / Emisión como módulo independiente.

- No tratar Firma como estado visible independiente.

- Usar estado visual "Emitido" cuando corresponda.

- Respetar AGENTS.md actualizado.

- Si se crea el documento Markdown, hacer commit y push.

- No usar git add .

Cada prompt generado debe iniciar con esta idea:\
"Antes de implementar, analiza cómo está actualmente, qué falta por
mejorar, qué falta por completar, qué está mal conectado y qué riesgos
existen."

Cada prompt debe incluir:

- objetivo funcional;

- archivos/carpetas a revisar;

- alcance exacto;

- qué NO debe hacer;

- reglas de BD;

- reglas de UI;

- reglas de Service/DAO;

- criterios de aceptación;

- build;

- git add selectivo;

- commit;

- push.

El plan debe tener este orden:

PROMPT 0 --- Diagnóstico maestro de brechas hasta Notificación

- Comparar detalle_funciones.md contra la aplicación actual.

- Identificar brechas funcionales, UI, BD, flujo y servicios.

- Generar matriz de prioridades.

- Aclarar explícitamente que Firma / Emisión se evaluará dentro de
  Verificación.

PROMPT 1 --- Registro / Recepción\
Debe considerar:

- Registro individual.

- Carga diaria.

- Previsualización editable.

- Campos obligatorios.

- Validación de duplicidad por número de acta + titular.

- Grupo familiar como alerta no bloqueante.

- Plazos por tipo de procedimiento.

- Reconsideración y Apelación sin número inicial si corresponde.

- Edición manual solo cuando esté en estado Registrado.

- Mejoras de layout amigable para usuario final.

PROMPT 2 --- Asignación\
Debe considerar:

- Registros nuevos.

- Registros sin número.

- Generación/asociación a expediente principal.

- Documentos asociados.

- Grupo familiar como sugerencia para asignar al mismo abogado.

- Carga laboral de abogados.

- Asignación individual y masiva.

- Hoja de envío SITD.

- Panel de documentos/cartas.

- Requiere publicación y Fecha de publicación registrados desde el panel
  de documentos/cartas.

- Plazos por tipo de carta/documento:

  - Carta edicto: 30 días.

  - Para publicación: 15 días.

  - Carta indagatoria: 10 días.

  - Carta precisar pretensión: 30 días.

  - Carta falta sustento: 30 días.

- No poblar documentos analizados desde Asignación.

- No forzar asignación automática por grupo familiar.

PROMPT 3 --- Análisis\
Debe considerar:

- Recibir expediente.

- Determinar si corresponde atender.

- No corresponde / archivo si el flujo lo permite.

- Evaluación.

- Sustento.

- Precisa pretensión.

- Acta incorporada.

- Tablas maestras.

- Reconstitución.

- Legitimidad.

- Medios probatorios.

- Procedente / Procedente en parte / Improcedente.

- Cartas intermedias.

- Carta edicto.

- Abandono por falta de respuesta si el flujo lo permite.

- Continuación de evaluación.

- Documentos analizados.

- Envío a Verificación.

- Completar datos faltantes.

- Registrar o mostrar carpeta/enlace de expediente digital si el modelo
  lo permite.

- Mostrar Requiere publicación y Fecha de publicación en modo lectura,
  porque se registran desde Asignación.

- No modificar Requiere publicación ni Fecha de publicación desde
  Análisis.

PROMPT 4 --- Verificación integrada con emisión\
Debe considerar:

- Verificación de consistencia documental.

- Observación y devolución a Análisis.

- Documento inconsistente.

- Aprobación de verificación.

- Controles de documento emitido dentro de Verificación.

- Registro/numeración de resolución o documento si el modelo lo soporta.

- Estado visual "Emitido".

- No usar Firma / Emisión como módulo independiente.

- No mostrar Firma como estado visual independiente.

- Cartas edicto firmadas/validadas por Subdirector si la regla funcional
  lo exige, pero dentro del módulo Verificación.

- Distinguir destino posterior:

  - resoluciones procedentes/procedentes en parte/improcedentes hacia
    Ejecución;

  - otros documentos hacia Notificación si el flujo lo permite.

- Mantener acciones con transiciones reales.

- Bloquear con diagnóstico si falta tabla, columna, catálogo o
  transición.

PROMPT 5 --- Ejecución\
Debe considerar:

- Resoluciones emitidas/derivadas.

- Validación de error material.

- Devolución a Análisis con hoja de envío si corresponde.

- Preservar resolución/documentos anteriores.

- Nuevo número de resolución solo si el modelo/flujo lo soporta.

- Derivar al mismo abogado que analizó.

- Improcedentes: carta de notificación.

- Procedente / Procedente en parte: anotación textual y carta de
  notificación.

- Carta de notificación validada por supervisor.

- Derivación a Notificación si el flujo lo permite.

PROMPT 6 --- Notificación hasta antes de Publicación\
Debe considerar:

- Supervisor verifica carta de notificación.

- Devolución a Ejecución si hay inconsistencia.

- Validación/firma interna de carta si corresponde, sin crear módulo de
  firma.

- Bandeja de documentos para notificar.

- Derivación a abogados de notificación si el modelo lo permite.

- Registro por documento:

  - Tipo de notificación.

  - Fecha de notificación.

  - Tiene acuse.

  - Resultado.

- Hasta 3 intentos:

  - 1 virtual.

  - 2 presenciales/físicos.

- Requiere publicación y Fecha de publicación deben consumirse desde la
  metadata registrada previamente en Asignación, pero también deben
  poder visualizarse en Notificación.

- Si Notificación falla y corresponde publicación, preparar derivación
  futura a Publicación solo si la transición real existe.

- No implementar módulo Publicación todavía.

- No implementar publicación externa.

PROMPT 7 --- QA visual e integración hasta Notificación\
Debe considerar:

- Layout consistente.

- Cards superiores.

- Buscadores anchos.

- Tablas limpias.

- Badges.

- Tooltips.

- Panel derecho contextual.

- Chip maximizar/restaurar.

- Grillas expandibles donde correspondan.

- Consola Expediente.

- Alertas de duplicidad.

- Grupo familiar.

- Requiere publicación.

- Fecha de publicación.

- Estado Emitido.

- Sin Firma / Emisión como módulo independiente.

- Sin códigos técnicos visibles.

PROMPT 8 --- Diagnóstico futuro de Publicación\
Solo diagnóstico, sin implementación.\
Debe considerar:

- Qué datos ya llegan desde Asignación/Notificación.

- Qué falta para implementar Publicación.

- EXPEDIENTE_PUBLICACION.

- Fecha de publicación.

- Registro de publicación.

- Cierre posterior.

- Riesgos.

Reglas de diseño para todos los prompts:

- Layout amigable para usuario final.

- Diseño premium similar al patrón de Asignación.

- Panel derecho contextual oculto inicialmente.

- Tabla ocupando todo el ancho cuando no hay selección.

- Acciones al pie del panel.

- Cards superiores con métricas útiles.

- Badges sobrios.

- No saturar con columnas técnicas.

- No mostrar ID.

- No mostrar códigos técnicos.

- No mostrar V2 al usuario final.

- No usar padre/hijo.

- No scroll horizontal global innecesario.

Reglas técnicas para todos los prompts:

- UI llama a Service.

- Service llama a DAO.

- SQL solo en DAO.

- Resolver IDs por código/catálogo.

- No hardcodear IDs.

- No tocar legacy.

- No tocar OracleConnection.java.

- Java 8 + Swing + FlatLaf.

- mvn clean compile.

- mvn clean package si se modifica App V2.

- git status.

- git add selectivo.

- commit si build pasa.

- push al branch actual.

El documento debe ser fácil de copiar/pegar en Codex CLI y debe contener
prompts completos, no solo títulos.

Git:

- Ejecutar git status.

- Agregar solo:\
  docs/arquitectura_app/PLAN_PROMPTS_INCREMENTALES_HASTA_NOTIFICACION.md

- No agregar capturas.

- No agregar config.

- No agregar deploy/installer/output.

- Commit si no hay conflictos.

Mensaje sugerido:\
docs: add incremental prompt plan up to notification

Push:

- Hacer push al branch actual.

Entregable final:

- ruta del documento creado;

- resumen del plan;

- confirmación de que Firma / Emisión ya no se trata como módulo
  independiente;

- confirmación de que Verificación concentra emisión/documento emitido;

- confirmación de que se usa estado visual Emitido;

- confirmación de que Requiere publicación y Fecha de publicación se
  consideran desde Asignación y se consumen después;

- git status;

- commit creado;

- push realizado o error exacto.
