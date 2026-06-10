# Prompt - Rediseno de pantallas SDRERC V2 segun BPMN TO BE V2

Usar este prompt para ejecutar una fase de rediseno visual/controlado de pantallas SDRERC V2, tomando como referencia funcional el flujo BPMN actualizado.

```text
Necesito ejecutar una fase de rediseno integral de pantallas SDRERC V2 segun el flujo actualizado:

docs/arquitectura_bd/TO BE V2.bpmn

Objetivo:
Redisenar y ajustar las pantallas SDRERC V2 para que reflejen mejor el flujo BPMN TO BE V2, sus etapas, decisiones, rutas normales y rutas alternativas, sin romper la arquitectura actual ni cambiar reglas de negocio no solicitadas.

El objetivo no es solo embellecer pantallas. El objetivo es que cada modulo operativo muestre claramente:

* en que parte del flujo BPMN esta el expediente;
* que decision funcional corresponde;
* que informacion debe revisar el usuario;
* que accion sigue;
* que rutas alternativas existen;
* que acciones estan bloqueadas por falta de transicion, documento, comentario o modelo.

Contexto:
SDRERC V2 ya tiene alineamiento inicial con el BPMN TO BE V2, SDRERC_APP y AGENTS.md actualizado. Ya existe script de alineamiento:

db/sdrerc_app/scripts/20_datos_maestros_flujo_to_be_v2_bpmn.sql

La app V2 ya tiene los modulos:

* Inicio.
* Bandeja de Expedientes.
* Registro / Recepcion.
* Asignacion.
* Analisis.
* Verificacion.
* Firma / Emision.
* Ejecucion.
* Notificacion.
* Publicacion.
* Expediente digital.
* Cierre / Archivo.
* Usuarios.
* Equipo Juridico.
* Roles.

Antes de modificar codigo, leer obligatoriamente:

* AGENTS.md actualizado.
* docs/arquitectura_bd/TO BE V2.bpmn
* docs/arquitectura_bd/PROMPT_REESTRUCTURACION_BD_SDRERC.md
* docs/arquitectura_bd/er_sdrerc_app.png
* db/sdrerc_app/scripts/20_datos_maestros_flujo_to_be_v2_bpmn.sql
* db/sdrerc_app/scripts/*.sql relevantes.
* docs/arquitectura_app/*.md relevantes.
* src/main/java/com/sdrerc/ui/appv2/MenuPrincipalV2.java
* src/main/java/com/sdrerc/ui/appv2/theme/AppV2Theme.java
* src/main/java/com/sdrerc/ui/appv2/components/*
* src/main/java/com/sdrerc/ui/appv2/util/DisplayNameMapperV2.java
* src/main/java/com/sdrerc/ui/views/expedienteconsola/
* src/main/java/com/sdrerc/ui/views/registrorecepcion/
* src/main/java/com/sdrerc/ui/views/asignacion/
* src/main/java/com/sdrerc/ui/views/analisis/
* src/main/java/com/sdrerc/ui/views/verificacion/
* src/main/java/com/sdrerc/ui/views/firmaemision/
* src/main/java/com/sdrerc/ui/views/ejecucion/
* src/main/java/com/sdrerc/ui/views/notificacion/
* src/main/java/com/sdrerc/ui/views/publicacion/
* src/main/java/com/sdrerc/ui/views/expedientedigital/
* src/main/java/com/sdrerc/ui/views/cierrearchivo/
* DAOs, Services y DTOs existentes de SDRERC_APP relacionados con flujo, acciones permitidas, historial, documentos, evaluacion, resolucion, notificacion, publicacion, expediente digital y cierre.

Reglas obligatorias:

* Respetar AGENTS.md actualizado.
* Mantener SDRERC V2 separada de legacy.
* No tocar legacy.
* No tocar OracleConnection.java.
* No ejecutar SQL salvo SELECTs de diagnostico expresamente necesarios.
* No modificar datos de BD en esta fase salvo autorizacion explicita posterior.
* No modificar scripts SQL en esta fase salvo que se detecte bloqueo y primero se reporte.
* No usar DROP, DELETE ni TRUNCATE.
* No crear etapa visual VALIDACION.
* No poner SQL en JPanel.
* UI llama a Service.
* Service llama a DAO.
* No cambiar reglas de negocio ni transiciones.
* No inventar acciones no soportadas por FLUJO_TRANSICION.
* No usar IDs hardcodeados.
* Mantener Java 8 + Swing + FlatLaf.
* Mantener estilo institucional SDRERC V2.
* Mantener nombres amigables en UI.
* No mostrar codigos tecnicos al usuario final si existe nombre amigable.
* No hacer refactor masivo innecesario.
* Aplicar build, commit y push segun AGENTS.md.

Autorizacion para esta fase:

* Puedes redisenar layout, textos visibles, paneles de contexto, secciones, badges, tablas, filtros, empty states y barras de acciones.
* Puedes crear componentes reutilizables UI V2 si reducen duplicacion real.
* Puedes ajustar DTOs/Services/DAOs solo si es necesario para exponer datos ya existentes en la BD o en vistas actuales.
* Puedes ejecutar SELECTs de diagnostico sobre SDRERC_APP para confirmar acciones permitidas, etapa/estado y datos mostrables.
* Puedes actualizar AGENTS.md si hay una regla visual o decision persistente nueva.

No autorizado:

* No crear tablas.
* No alterar tablas.
* No ejecutar scripts de datos.
* No modificar expedientes reales.
* No cambiar credenciales.
* No tocar legacy.
* No reemplazar el flujo BPMN por otro criterio.
* No crear una consola paralela.

Alcance 1: Diagnostico visual BPMN vs pantallas actuales

Antes de modificar, generar diagnostico breve:

* Que tareas BPMN ya estan representadas visualmente.
* Que tareas BPMN estan ocultas o poco claras en UI.
* Que decisiones BPMN no tienen contexto visual suficiente.
* Que rutas alternativas no se entienden desde el modulo.
* Que pantallas tienen acciones sin explicacion funcional.
* Que pantallas muestran codigos tecnicos.
* Que pantallas tienen panel derecho pobre o no alineado al flujo.
* Que pantallas pueden mejorarse sin tocar reglas de negocio.

No detenerse en un informe largo: diagnosticar y ejecutar cambios seguros.

Alcance 2: Patron visual comun por etapa BPMN

Cada modulo operativo debe tender a esta estructura:

1. Cabecera operativa del modulo:
   * titulo amigable;
   * subtitulo funcional;
   * badge de macroetapa;
   * indicador de ruta BPMN o etapa actual.

2. Filtros y bandeja:
   * buscador claro;
   * filtros por estado/accion/responsable cuando aplique;
   * tabla legible;
   * nombres amigables;
   * tooltips para valores largos;
   * empty state claro.

3. Panel de flujo/contexto:
   * expediente seleccionado;
   * posicion en el flujo;
   * decision BPMN relevante;
   * datos a revisar;
   * alertas;
   * acciones disponibles;
   * acciones no disponibles con razon amigable, si el sistema ya puede determinarlo.

4. Acciones:
   * accion principal visible;
   * acciones secundarias ordenadas;
   * rutas alternativas separadas visualmente;
   * mensajes de confirmacion claros;
   * bloqueo amigable si falta transicion o requisito.

5. Historial y trazabilidad:
   * indicar ultimo movimiento;
   * abrir consola unica con Ver detalle;
   * no duplicar consola.

Alcance 3: Home / Inicio

Actualizar el Home para que sea la portada oficial del flujo BPMN TO BE V2.

Debe mostrar:

* logo RENIEC local vigente;
* resumen ejecutivo de expedientes;
* flujo operativo premium con las macroetapas:
  Registro -> Asignacion -> Analisis -> Verificacion -> Firma / Emision -> Ejecucion -> Notificacion -> Publicacion -> Expediente digital -> Cierre / Archivo
* conectores/flechas visibles;
* colores sobrios por etapa si ayudan;
* accesos rapidos a Bandeja, Registro, Asignacion y Analisis;
* sin textos de legacy, migracion o V2 como version alterna.

No debe:

* generar scroll horizontal global;
* mostrar codigos tecnicos;
* verse como landing page generica.

Alcance 4: Bandeja de Expedientes

Debe convertirse en vista ejecutiva transversal:

* mostrar etapa/estado amigable;
* mostrar accion siguiente si existe desde `VW_EXPEDIENTE_ACCIONES_PERMITIDAS`;
* mostrar indicador de observacion, duplicado/asociado, vencimiento y responsable;
* permitir abrir `DlgConsolaExpedienteV2`;
* no mostrar `V2` en titulo.

Mejoras esperadas:

* panel de filtros uniforme;
* tabla con columnas no truncadas;
* badges de etapa/estado;
* columna de dias solo numerica si se muestra plazo;
* empty state claro.

Alcance 5: Registro / Recepcion

Representar mejor tareas BPMN:

* cargar archivo actualizado;
* validar archivo;
* asignar codigo de expediente;
* validar bandeja de expedientes;
* detectar/asociar solicitudes duplicadas;
* registrar manualmente cuando aplique.

Pantalla esperada:

* seccion de carga diaria con pasos claros;
* seccion de registro manual ordenada;
* bandeja de registro con filtros consistentes;
* indicador de duplicado por numero de acta + titular;
* campos de tramite visibles: tipo solicitud, numero documento, numero tramite, fecha solicitud/recepcion/vencimiento;
* no excluir errores ni duplicados de la carga; deben registrarse e identificarse facilmente.

Alcance 6: Asignacion

Representar mejor tareas BPMN:

* validar bandeja de expedientes;
* validar/asociar solicitudes duplicadas;
* asignar expedientes segun tipo de solicitud;
* identificar equipo/abogado/supervisor;
* permitir ver relacionados confirmados o posibles.

Pantalla esperada:

* buscador amplio como Registro / Recepcion;
* panel de asignacion con el mismo nivel visual que panel de Analisis;
* secciones: expediente seleccionado, duplicados/relacionados, responsable, acciones;
* seleccion multiple clara;
* acciones secundarias para seleccionar visibles/limpiar seleccion;
* advertencias sobre duplicados sin impedir registro.

Alcance 7: Analisis

Representar mejor tareas BPMN:

* recibir y evaluar expediente;
* decidir si corresponde atenderlo;
* validar consistencia de documentos;
* validar acta, legitimidad y medios probatorios;
* identificar si requiere edicto;
* registrar resultado de evaluacion;
* procedente, improcedente, observado, abandono, no corresponde;
* derivar a verificacion, notificacion o archivo cuando corresponda.

Pantalla esperada:

* panel derecho con decisiones del analista;
* tarjetas o secciones para documentos, acta, legitimidad, medios probatorios, edicto y resultado;
* acciones disponibles segun flujo;
* mensajes de bloqueo si falta documento analizado o evaluacion;
* no mostrar codigos tecnicos.

Alcance 8: Verificacion

Representar mejor tareas BPMN:

* validar consistencia de documentos;
* detectar si requiere correccion;
* devolver documentos a Analisis;
* validar que resolucion/informe no tenga errores;
* aprobar verificacion;
* enviar a Firma / Emision.

Pantalla esperada:

* panel de revision con resultado de Analisis;
* seccion de inconsistencias;
* acciones: aprobar, observar, documento inconsistente, devolver a Analisis, enviar a firma;
* advertencias claras cuando una accion no es valida.

Alcance 9: Firma / Emision

Representar mejor tareas BPMN:

* proyectar informe/resolucion;
* validar errores;
* devolver resolucion cuando exista error;
* firmar documentos;
* distinguir informe/resolucion/carta/oficio cuando el modelo lo permita;
* registrar emision;
* registrar numero de resolucion/documento;
* enviar a Ejecucion.

Pantalla esperada:

* panel de documento resolutivo;
* datos de firma, emision y numeracion;
* estado del documento;
* acciones ordenadas por secuencia: firmar, emitir, numerar, enviar a Ejecucion;
* no implementar carga fisica de archivos si no esta autorizada.

Alcance 10: Ejecucion

Representar mejor tareas BPMN:

* revisar resolucion/documento numerado;
* registrar ejecucion;
* marcar ejecutado;
* registrar observacion o inconsistencia;
* devolver a Analisis cuando el flujo lo permita;
* derivar a Notificacion.

Pantalla esperada:

* panel de ejecucion con resolucion/documento destacado;
* accion principal `Registrar ejecucion`;
* accion `Derivar a Notificacion` solo si corresponde;
* seccion de reversion/observacion separada visualmente.

Alcance 11: Notificacion

Representar mejor tareas BPMN:

* validar documentos a notificar;
* tipo de notificacion: virtual o presencial;
* enviar/registrar notificacion;
* recibir cargo de acuse;
* validar si tenemos acuse;
* registrar notificacion fallida;
* decidir si requiere publicacion;
* cerrar si corresponde.

Pantalla esperada:

* panel de modalidad de notificacion;
* datos de destinatario/cargo si el modelo lo permite;
* acciones: registrar virtual, registrar presencial, registrar cargo, confirmar notificacion, requiere publicacion, cerrar;
* no implementar envio real de correos/SMS/WhatsApp.

Alcance 12: Publicacion

Representar mejor tareas BPMN:

* validar si los documentos solicitan publicacion;
* registrar fecha de publicacion;
* registrar medio/referencia si el modelo lo permite;
* marcar publicacion registrada;
* cerrar expediente.

Pantalla esperada:

* panel de publicacion con motivo y notificacion previa;
* datos de publicacion visibles;
* acciones claras: registrar publicacion, cerrar expediente;
* no integrar portales externos.

Alcance 13: Expediente digital

Representar mejor tareas BPMN:

* crear carpeta y link de acceso;
* recopilar y clasificar documentos;
* cargar metadata documental;
* validar completitud;
* custodiar expediente digital;
* revisar avances.

Pantalla esperada:

* panel documental con carpeta/ruta/enlace;
* estado de completitud;
* documentos asociados;
* acciones: registrar carpeta, registrar enlace, marcar completo;
* no mover ni eliminar archivos fisicos.

Alcance 14: Cierre / Archivo

Representar mejor tareas BPMN:

* finalizar registro de informacion;
* registrar cierre;
* registrar archivo;
* visualizar derivacion externa pendiente;
* consultar historial completo.

Pantalla esperada:

* vista final de expediente;
* motivo de cierre/archivo;
* antecedentes completos;
* acciones solo si el flujo lo permite;
* no eliminar expedientes ni documentos.

Alcance 15: Consola unica del expediente

Ajustar `DlgConsolaExpedienteV2` para reflejar el BPMN:

* barra visual de etapas;
* etapa/estado actual;
* accion siguiente;
* documentos;
* evaluacion;
* resolucion;
* ejecucion;
* notificacion;
* publicacion;
* expediente digital;
* cierre/archivo;
* historial;
* asociados/relacionados.

No crear consola nueva.
No duplicar logica.
Si falta dato en el DTO/DAO, exponerlo por DAO/Service de lectura.

Alcance 16: Componentes reutilizables

Revisar y mejorar componentes V2 si conviene:

* AppV2ModuleLayoutPanel.
* AppV2FilterPanel.
* AppV2ActionPanel.
* AppV2TablePanel.
* AppV2EmptyStatePanel.
* AppV2MetricCard.
* AppV2SectionHeader.
* AppV2StageFlowPanel o componente equivalente para mostrar secuencia BPMN.
* AppV2DecisionCard o componente equivalente para decisiones BPMN.

Crear componentes nuevos solo si reducen duplicacion real y no generan refactor masivo.

Alcance 17: Acciones permitidas

Las pantallas deben consultar acciones permitidas desde:

* `VW_EXPEDIENTE_ACCIONES_PERMITIDAS`, si aplica;
* `FLUJO_TRANSICION`;
* Services/DAOs existentes.

Reglas:

* No mostrar acciones invalidas como disponibles.
* Si se muestra una accion bloqueada, explicar de forma amigable el requisito faltante.
* No inventar transiciones.
* No hardcodear IDs.
* Mantener codigos internamente, mostrar nombres amigables.

Alcance 18: Nombres amigables

Actualizar o reutilizar `DisplayNameMapperV2` para:

* etapas;
* estados;
* acciones;
* rutas especiales;
* resultado de evaluacion;
* tipo de notificacion;
* estados de expediente digital;
* estados de cierre/archivo.

No duplicar mapeos en cada JPanel.

Alcance 19: No romper

No romper:

* Registro / Recepcion.
* Carga diaria.
* Registro manual.
* Asignacion.
* Analisis.
* Verificacion.
* Firma / Emision.
* Ejecucion.
* Notificacion.
* Publicacion.
* Expediente digital.
* Cierre / Archivo.
* Bandeja de Expedientes.
* Consola Expediente.
* Expedientes asociados.
* Roles.
* Usuarios.
* Equipo Juridico.
* MenuPrincipalV2.
* conexion SDRERC_APP.
* flujo SDRERC_TO_BE.

No tocar:

* FrmLogin.java legacy.
* MenuPrincipal.java legacy.
* com.sdrerc.Main legacy.
* OracleConnection.java.

Alcance 20: Estrategia de implementacion

Implementar por prioridad:

Prioridad 1:
* Home.
* Bandeja de Expedientes.
* Consola unica.
* Registro / Recepcion.
* Asignacion.
* Analisis.

Prioridad 2:
* Verificacion.
* Firma / Emision.
* Ejecucion.
* Notificacion.

Prioridad 3:
* Publicacion.
* Expediente digital.
* Cierre / Archivo.

Si el alcance es demasiado grande:

* aplicar Prioridad 1 completa;
* dejar diagnostico de pendientes para Prioridad 2 y 3;
* no romper pantallas existentes.

Validacion:

* Ejecutar `mvn clean compile`.
* Si compila, ejecutar `mvn clean package`.
* Revisar `git status`.
* No usar `git add .`.
* Agregar solo archivos de esta tarea.
* Commit sugerido:
  `ui: redesign SDRERC V2 screens for TO BE BPMN flow`
* Push al branch actual.

Entregable final:

* Diagnostico breve BPMN vs pantallas.
* Pantallas redisenadas.
* Componentes reutilizables creados/modificados.
* Cambios por modulo.
* Cambios en consola unica.
* Cambios en nombres amigables.
* Confirmacion de acciones segun flujo.
* Confirmacion de que no se creo etapa VALIDACION.
* Confirmacion de que no se cambio logica de negocio.
* Confirmacion de que no se ejecuto SQL de escritura.
* Confirmacion de que no se tocaron scripts SQL, salvo que se haya reportado y autorizado.
* Confirmacion de que no se toco legacy.
* Confirmacion de que no se toco OracleConnection.java.
* Resultado de `mvn clean compile`.
* Resultado de `mvn clean package`.
* Commit creado.
* Push realizado o error exacto.
* Comando de prueba:
  `.\run-v2.ps1`
```
