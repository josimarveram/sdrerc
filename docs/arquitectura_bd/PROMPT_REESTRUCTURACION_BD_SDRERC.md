Actúa como arquitecto senior Oracle XE, DBA, analista funcional experto
en expedientes registrales, BPMN, Bizagi, Java Swing/FlatLaf,
trazabilidad, auditoría, UX empresarial y diseño de sistemas
administrativos legacy.

Estoy trabajando en el sistema SDRERC dentro de IntelliJ IDEA.

Necesito diseñar e implementar progresivamente una nueva base de datos
Oracle XE llamada SDRERC_APP, basada en:

1\. El flujo funcional TO BE modelado en Bizagi/BPMN.

2\. El diagnóstico de la base actual en Oracle/SYSTEM.

3\. La última Acta de Reunión N.° 013-2026-DRC, donde se validaron
ajustes funcionales importantes para ejecución, notificación, cargos de
acuse, expediente digital, publicación y asignación de abogados.

IMPORTANTE:

La idea NO es copiar la base actual en SYSTEM ni reutilizar su mal
diseño. La base actual solo sirve como diagnóstico y referencia
funcional. Actualmente la tabla EXPEDIENTE concentra demasiados datos y
la estructura tiene poca normalización, baja integridad referencial,
catálogos mezclados y pocos FK reales.

La nueva base SDRERC_APP debe diseñarse limpia, robusta, normalizada,
relacionada, auditable y preparada para trabajar correctamente hacia
adelante.

No quiero seguir usando SYSTEM como dueño de tablas. SYSTEM solo debe
usarse para crear el nuevo usuario/esquema SDRERC_APP y asignar permisos
mínimos.

Objetivo general:

Diseñar desde cero la nueva base Oracle SDRERC_APP y dejarla preparada
para un nuevo diseño visual del sistema, más amigable para el usuario
final, basado en etapas, estado actual visible, responsables,
trazabilidad, historial completo, documentos, alertas, cargos de acuse,
publicación, expediente digital y búsqueda eficiente de expedientes.

Archivo BPMN:

Usa el archivo TO BE.bpmn como referencia funcional principal para
identificar actores, etapas, actividades, decisiones, documentos,
derivaciones, responsables y transiciones.

Acta de reunión:

Usa también el Acta de Reunión N.° 013-2026-DRC como referencia
funcional obligatoria, especialmente los acuerdos y ajustes sobre:

\- recepción;

\- asignación;

\- asociación de duplicados;

\- expedientes por trabajar;

\- análisis;

\- ejecución;

\- notificación;

\- cargos de acuse;

\- publicación;

\- expediente digital;

\- continuidad del abogado responsable durante análisis y ejecución.

============================================================

1\. ACLARACIÓN FUNCIONAL SOBRE ACTORES DEL BPMN

============================================================

No todos los carriles o participantes del BPMN tendrán acceso al
aplicativo SDRERC.

NO deben modelarse como usuarios internos del sistema:

\- SDPRC

\- Ciudadano / Entidad

\- OGD

Estos actores pueden participar en el proceso documental o
administrativo, pero NO tendrán login, bandeja, rol, permisos ni usuario
dentro del aplicativo.

Por tanto:

1\. SDPRC no debe crearse como USUARIO ni como EQUIPO interno operativo.

Debe tratarse como una entidad externa o área externa de coordinación,
especialmente para casos como:

\- solicitud de culminación de línea;

\- derivación externa;

\- respuesta externa;

\- documento u oficio recibido;

\- documento u oficio enviado.

2\. Ciudadano / Entidad no debe crearse como usuario del sistema.

Debe modelarse como PERSONA, SOLICITANTE, REMITENTE o ENTIDAD_EXTERNA,
según corresponda.

Puede figurar en:

\- EXPEDIENTE_PERSONA;

\- EXPEDIENTE_SOLICITUD;

\- EXPEDIENTE_DERIVACION_EXTERNA;

\- EXPEDIENTE_NOTIFICACION.

3\. OGD no debe crearse como usuario interno del sistema si no tendrá
acceso al aplicativo.

Debe tratarse como origen documental o unidad externa de recepción
documental.

Puede figurar como:

\- origen_documental;

\- entidad_origen;

\- canal_recepcion;

\- área externa;

\- referencia de trámite documentario.

Usuarios internos reales del aplicativo SDRERC:

Modelar como usuarios, roles, equipos y bandejas solo a los actores que
sí usarán el sistema:

\- Sub Director de SDRERC

\- Asistente de Recepción

\- Asignador

\- Supervisor

\- Abogado

\- Registrador Civil

\- Administrador del sistema, si aplica

Estos sí deben participar en:

\- USUARIO

\- ROL

\- USUARIO_ROL

\- EQUIPO

\- EQUIPO_USUARIO

\- USUARIO_SUPERVISION

\- EXPEDIENTE_ASIGNACION

\- EXPEDIENTE_HISTORIAL

\- permisos

\- acciones permitidas

\- bandejas internas

Crear una tabla para entidades externas, por ejemplo:

ENTIDAD_EXTERNA

\- ID_ENTIDAD_EXTERNA

\- CODIGO

\- NOMBRE

\- TIPO_ENTIDAD

\- ACTIVO

\- CREADO_POR

\- CREADO_EN

\- MODIFICADO_POR

\- MODIFICADO_EN

Tipos posibles:

\- SDPRC

\- OGD

\- MUNICIPALIDAD

\- OFICINA_REGISTRAL

\- CIUDADANO

\- ENTIDAD_PUBLICA

\- ENTIDAD_PRIVADA

\- OTRO

Crear también una tabla:

CANAL_RECEPCION

\- ID_CANAL_RECEPCION

\- CODIGO

\- NOMBRE

\- ACTIVO

Ejemplos:

\- Mesa de partes

\- Trámite documentario

\- OGD

\- Correo institucional

\- Presencial

\- Virtual

En EXPEDIENTE_SOLICITUD incluir campos como:

\- ID_ENTIDAD_ORIGEN

\- ID_CANAL_RECEPCION

\- ID_PERSONA_SOLICITANTE

\- NUMERO_TRAMITE_DOCUMENTARIO

\- FECHA_RECEPCION

\- ASUNTO

\- OBSERVACION

\- ES_TRAMITE_VIRTUAL

\- CORREO_ELECTRONICO_OBLIGATORIO

Regla del acta:

Para todos los trámites virtuales, el correo electrónico debe ser
obligatorio.

En EXPEDIENTE_DERIVACION_EXTERNA incluir:

\- ID_EXPEDIENTE

\- ID_ENTIDAD_DESTINO

\- TIPO_DERIVACION

\- NUMERO_OFICIO

\- FECHA_ENVIO

\- FECHA_RESPUESTA

\- ESTADO_RESPUESTA

\- ID_DOCUMENTO_ENVIADO

\- ID_DOCUMENTO_RESPUESTA

\- COMENTARIO

Regla importante:

Las entidades externas pueden aparecer en historial como referencia,
pero no como usuarios responsables internos.

Por ejemplo, en EXPEDIENTE_HISTORIAL se puede registrar:

\- ID_USUARIO_ORIGEN: usuario interno que realiza la acción.

\- ID_USUARIO_DESTINO: usuario interno, si aplica.

\- ID_ENTIDAD_EXTERNA_ORIGEN: si el movimiento proviene de una entidad
externa.

\- ID_ENTIDAD_EXTERNA_DESTINO: SDPRC, municipalidad, OGD u otra entidad
externa, si aplica.

Pero la responsabilidad operativa dentro del sistema siempre debe recaer
en un usuario o equipo interno.

No crear bandejas internas para:

\- SDPRC

\- Ciudadano / Entidad

\- OGD

Sí crear bandejas para:

\- Recepción

\- Asignador

\- Abogado de análisis

\- Supervisor

\- Ejecución por trabajar

\- Notificación por trabajar

\- Cargos de acuse

\- Publicación

\- Registrador Civil

\- Administración

\- Derivaciones externas pendientes, pero gestionadas por usuarios
internos.

============================================================

2\. CONSIDERACIONES OBLIGATORIAS DEL ACTA 013-2026-DRC

============================================================

Incorporar obligatoriamente estas reglas funcionales validadas:

1\. Revisión del flujo culminada

Se revisaron funcionalidades de:

\- recepción;

\- asignación;

\- análisis;

\- asociación de expedientes duplicados;

\- cargos de acuse;

\- expediente digital;

\- publicación;

\- ejecución;

\- notificación.

2\. Abogado único para análisis y ejecución

La SDRERC indicó que el análisis y la ejecución de cada solicitud estará
a cargo de un único abogado durante todo el proceso.

Regla clave:

\- Se elimina la asignación por etapas independientes entre análisis y
ejecución.

\- La "Bandeja de Ejecución" ya no debe requerir una nueva asignación.

\- El expediente debe permanecer vinculado al abogado asignado
inicialmente.

\- La etapa de ejecución puede existir como etapa funcional y visual,
pero NO debe crear una nueva asignación independiente si el abogado ya
fue asignado en análisis.

\- El historial sí debe registrar el pase a ejecución.

\- La asignación activa puede mantenerse con el mismo abogado
responsable.

\- Si excepcionalmente se reasigna, debe quedar registrado en
EXPEDIENTE_HISTORIAL y EXPEDIENTE_ASIGNACION.

3\. Módulo de Recepción

La importación de carga diaria debe incluir la totalidad de registros,
incluso los identificados como duplicados.

Regla:

\- No excluir duplicados durante la importación.

\- Los registros duplicados deben cargarse y marcarse como potencial
duplicado.

\- La revisión y cruce con documento primigenio se gestionará en el
módulo de Asignación.

4\. Módulo de Asignación

El asignador será responsable de:

\- asociar registros duplicados;

\- asociar cartas;

\- asociar otros documentos;

\- validar relación con el documento principal;

\- asociarlos al expediente principal si corresponde.

Regla:

\- Estos casos pueden ser derivados directamente a la Bandeja de
Notificación sin requerir todo el flujo regular.

\- Debe existir soporte para EXPEDIENTE_RELACIONADO o relación entre
expediente principal y solicitudes/documentos asociados.

\- Debe registrarse historial de asociación de duplicado/documento.

\- Debe poder diferenciar expediente principal de solicitud/documento
asociado.

5\. Módulo de Expedientes por Trabajar

Debe incluir campo:

\- CORRESPONDE / NO CORRESPONDE

Regla:

\- Si se selecciona CORRESPONDE, se aplica el flujo completo de
análisis, ejecución y notificación.

\- Si se selecciona NO CORRESPONDE, se habilita el campo MOTIVO.

\- El abogado podrá archivar el expediente.

\- Además, mediante trámite documentario, podrá derivarlo al área
correspondiente.

\- Esta derivación debe registrarse como derivación externa o
administrativa, no como asignación interna del sistema si el área no usa
SDRERC.

6\. Campo Resultado

Mantener resultados actuales:

\- PROCEDENTE

\- IMPROCEDENTE

\- PROCEDENTE_EN_PARTE

Agregar nuevos resultados:

\- EN_ABANDONO

\- OBSERVACION_ADMINISTRATIVA

Regla:

\- PROCEDENTE, IMPROCEDENTE y PROCEDENTE_EN_PARTE continúan el flujo
regular.

\- EN_ABANDONO y OBSERVACION_ADMINISTRATIVA deben derivarse directamente
a Bandeja de Notificación.

\- Estos resultados deben estar en catálogo TIPO_RESULTADO_EVALUACION o
RESULTADO_ANALISIS.

7\. Observaciones

Los campos:

\- ¿Tiene observación?

\- Tipo de observación

Regla:

\- En Expedientes por Trabajar deben estar en modo lectura.

\- Solo deben habilitarse en el módulo Expediente por Verificar.

\- Deben quedar registrados en EXPEDIENTE_OBSERVACION.

\- Deben impactar en estado REQUIERE_CORRECCION u OBSERVADO cuando
corresponda.

8\. Campo Incorporado

Incluir campo obligatorio:

\- INCORPORADO_SI_NO

Regla:

\- Debe identificar si el acta está incorporada o no incorporada.

\- Si INCORPORADO = SÍ, se habilitan las casillas:

\- RECONSTITUCION

\- LEGITIMIDAD

\- CUMPLE_MEDIOS_PROBATORIOS

\- Si INCORPORADO = NO, esas casillas deben estar deshabilitadas.

\- Estos datos deben modelarse como evaluaciones del expediente,
preferentemente en EXPEDIENTE_EVALUACION o campos específicos
relacionados al análisis del acta.

9\. Documentos analizados

Se habilitará una sección para registrar:

\- Fecha

\- Tipo de documento: Carta / Oficio / Memo / Otro

\- Descripción

\- Estado: En proyecto / En despacho / Emitido

Regla:

\- Esta información debe modelarse en EXPEDIENTE_DOCUMENTO o
EXPEDIENTE_DOCUMENTO_ANALIZADO.

\- Debe estar disponible tanto para análisis como para ejecución por
trabajar.

\- Los estados de documentos deben estar en catálogo:

\- EN_PROYECTO

\- EN_DESPACHO

\- EMITIDO

10\. Bandeja de Ejecución

Reglas:

\- El supervisor podrá revertir el estado de un documento cuando
identifique observaciones.

\- La reversión debe mantener trazabilidad de la corrección.

\- No se debe permitir que el expediente continúe a ejecución o
notificación con documentos pendientes o inconsistentes.

\- Una vez firmado el documento, el supervisor debe registrar el número
de resolución.

\- El número de resolución debe estar disponible para etapas
posteriores.

11\. Ejecución por trabajar

Agregar estado:

\- INDAGATORIO

Regla:

\- INDAGATORIO debe estar como estado o resultado de ejecución.

\- Debe ser considerado en ESTADO_EXPEDIENTE o TIPO_RESULTADO_EJECUCION.

12\. Notificación por trabajar / Bandeja de Cargos de Acuse

El flujo estándar de notificación consta de:

\- 1 notificación virtual

\- 2 notificaciones presenciales

Regla:

\- Agotadas estas instancias sin éxito, se debe proceder con generación
de publicación.

\- Debe existir soporte para registrar intentos de notificación.

\- Debe existir soporte para cargos de acuse.

\- Debe existir soporte para publicación.

\- La SDRERC enviará matriz Excel con variables de notificación y
publicación; por ahora diseñar estructura flexible.

13\. Publicación

Se debe evaluar implementación o adecuación de módulo de publicaciones.

Regla:

\- Debe existir una entidad EXPEDIENTE_PUBLICACION o considerarse en
Fase 2 si no se implementa aún.

\- Debe permitir relacionar casos sin acuse con el expediente.

\- Debe estar vinculada a los intentos fallidos de notificación.

14\. Expediente digital

La SDRERC confirmará si la opción de Expediente Digital requiere o no
asignación de abogados.

Regla recomendada:

\- Dado que el código se genera desde etapas iniciales, se recomienda
que el abogado responsable cree la carpeta del expediente digital.

\- El abogado responsable debe registrar el enlace en el módulo
Expediente por Trabajar.

\- El abogado responsable también debe encargarse de cargar los
documentos en la ruta compartida correspondiente.

\- El diseño debe soportar ambos escenarios:

A. Expediente digital sin nueva asignación.

B. Expediente digital con asignación específica si SDRERC lo confirma
después.

\- Por defecto, no crear una asignación independiente obligatoria para
expediente digital.

\- Registrar código, ruta, enlace, estado de completitud, usuario que
crea, fecha de creación y usuario custodio.

============================================================

3\. OBJETIVO FUNCIONAL DEL NUEVO SISTEMA

============================================================

El nuevo diseño debe permitir que el usuario pueda:

1\. Buscar rápidamente un expediente por:

\- número de expediente;

\- número de trámite documentario;

\- número de acta;

\- titular;

\- remitente;

\- tipo de documento;

\- procedimiento registral;

\- estado;

\- etapa;

\- responsable;

\- fechas;

\- entidad origen;

\- canal de recepción;

\- número de resolución;

\- estado de notificación;

\- estado de cargo de acuse;

\- publicación.

2\. Identificar visualmente el estado actual del expediente:

\- etapa actual;

\- estado actual;

\- responsable actual;

\- abogado asignado inicialmente;

\- equipo actual;

\- fecha de último movimiento;

\- días restantes o vencidos;

\- prioridad/alerta;

\- si tiene observaciones;

\- si requiere corrección;

\- si tiene documentos pendientes;

\- si está vencido;

\- si está archivado o cerrado;

\- si tiene derivación externa pendiente;

\- si tiene expediente digital completo;

\- si requiere publicación;

\- si tiene cargos de acuse pendientes.

3\. Ver el flujo completo del expediente tipo consola o timeline:

\- quién lo registró;

\- quién lo asignó;

\- quién lo recibió;

\- quién lo trabajó;

\- quién lo verificó;

\- quién registró número de resolución;

\- quién lo envió a ejecución;

\- quién ejecutó;

\- quién lo envió a notificación;

\- quién notificó;

\- cuántos intentos de notificación tuvo;

\- si tuvo cargo de acuse;

\- si pasó a publicación;

\- quién lo cerró o archivó;

\- cuándo ocurrió cada acción;

\- desde qué etapa/estado pasó;

\- hacia qué etapa/estado pasó;

\- qué usuario lo derivó;

\- a qué usuario/equipo se derivó;

\- si fue derivado a una entidad externa;

\- comentario;

\- motivo;

\- observaciones;

\- documentos generados o adjuntos en cada etapa.

4\. Visualizar documentos asociados o solicitudes duplicadas bajo un
expediente principal, sin mezclar todo en una sola tabla gigante.

5\. Consultar logs de auditoría:

\- cambios de datos;

\- cambios de estado;

\- cambios de responsable;

\- cambios de asignación;

\- cambios de documentos;

\- reversión de documentos;

\- registro de número de resolución;

\- cambios en notificaciones;

\- cambios en cargos de acuse;

\- cambios en publicación;

\- usuario que realizó el cambio;

\- fecha;

\- origen de la acción;

\- valor anterior y valor nuevo cuando aplique.

============================================================

4\. CONTEXTO ACTUAL LEGACY

============================================================

Contexto actual:

\- Sistema Java Swing/FlatLaf.

\- Oracle XE.

\- Conexión actual con usuario SYSTEM.

\- Tablas actuales en SYSTEM/USERS.

\- Tabla EXPEDIENTE actual concentra demasiada información.

\- Pocas FK reales.

\- Catálogos mezclados en CATALOGO/CATALOGO_ITEM.

\- Estados parcialmente hardcodeados en Java.

\- La base actual no debe ser copiada.

\- Solo debe usarse para entender reglas, campos y flujo.

Estados actuales conocidos:

\- 56 Registrado / Registro Expediente

\- 57 Asignado / Expediente Asignado

\- 58 Recibido / Expediente Recibido

\- 59 Atendido / Expediente Atendido

\- 87 Verificado / Expediente Verificado

\- 88 Asignado a ejecución / Ejecución Asignada

\- 89 Ejecución trabajada / Ejecución Trabajada

\- 90 Notificación asignada / Notificación Asignada

\- 91 Notificación trabajada / Notificación Trabajada

Crear equivalencias:

\- LEGACY_ESTADO_MAP

\- LEGACY_CATALOGO_MAP

También se puede agregar ID_LEGACY en ESTADO_EXPEDIENTE y otros
catálogos críticos para compatibilidad gradual.

No migrar datos piloto sin validación. Si se migra, hacerlo como piloto,
sin borrar datos actuales.

============================================================

5\. ACTIVIDADES CLAVE DEL BPMN Y ACTA QUE DEBEN REFLEJARSE EN LA BD

============================================================

Considerar como mínimo estas actividades funcionales:

Recepción:

\- Recibir documentos.

\- Importar carga diaria completa, incluyendo duplicados.

\- Generar reporte de solicitudes y actualizar información.

\- Cargar archivo actualizado.

\- Revisar archivo y asignar código de expediente.

Asignación:

\- Validar bandejas de abogados.

\- Validar y/o asociar solicitudes duplicadas.

\- Asociar cartas u otros documentos al expediente principal.

\- Asignar expedientes según tipo de solicitud.

\- Derivar ciertos documentos asociados directamente a notificación si
corresponde.

Análisis:

\- Recibir asignación.

\- Validar si corresponde atención.

\- Registrar Corresponde / No corresponde.

\- Registrar motivo cuando no corresponde.

\- Archivar expediente si no corresponde.

\- Derivar externamente al área correspondiente mediante trámite
documentario.

\- Verificar si el acta está incorporada.

\- Verificar que el acta esté actualizada en tablas maestras.

\- Solicitar culminación de línea a SDPRC.

\- Verificar si el acta tiene deterioro que amerite reconstitución.

\- Validar legitimidad del solicitante.

\- Validar medios probatorios.

\- Registrar resultado: Procedente, Improcedente, Procedente en parte,
En abandono, Observación administrativa.

\- Registrar documentos analizados.

Verificación:

\- Validar consistencia de documentos.

\- Registrar si tiene observación.

\- Registrar tipo de observación.

\- Solicitar corrección si corresponde.

\- Revertir estado de documento si el supervisor identifica
observaciones.

\- Evitar continuidad del expediente si existen documentos pendientes o
inconsistentes.

Firma / Emisión:

\- Proyectar informe y resolución de procedencia.

\- Proyectar informe y resolución de improcedencia.

\- Proyectar informe y resolución corregido.

\- Firmar documentos.

\- Registrar número de resolución una vez firmado.

\- Emitir documentos.

Ejecución:

\- Ejecutar resolución.

\- Registrar anotación textual.

\- Incluir estado Indagatorio.

\- Registrar documentos analizados en ejecución.

\- Mantener el mismo abogado asignado inicialmente, sin nueva asignación
obligatoria para ejecución.

Notificación:

\- Asignar o derivar a notificación cuando corresponda.

\- Proyectar carta de notificación.

\- Ejecutar 1 notificación virtual.

\- Ejecutar hasta 2 notificaciones presenciales.

\- Registrar resultado de cada intento.

\- Registrar cargos de acuse.

\- Si no hay éxito, pasar a publicación.

Publicación:

\- Registrar casos sin acuse.

\- Relacionar publicación con expediente.

\- Registrar datos mínimos de publicación.

\- Preparar estructura flexible para matriz Excel futura de SDRERC.

Expediente digital:

\- Crear código de expediente digital.

\- Crear carpeta de expediente digital.

\- Registrar link de acceso.

\- Registrar documentos cargados en ruta compartida.

\- Registrar estado de completitud.

\- Registrar custodia.

\- Por defecto, el abogado responsable inicial crea carpeta y registra
enlace.

\- Dejar preparado que SDRERC confirme si requiere asignación
específica.

Archivo / Cierre:

\- Cerrar expediente.

\- Archivar expediente.

\- Registrar motivo.

\- Mantener trazabilidad.

============================================================

6\. DECISIONES O GATEWAYS DEL BPMN Y ACTA A MODELAR

============================================================

Las decisiones deben transformarse en resultados, reglas, transiciones,
evaluaciones o catálogos.

Decisiones a considerar:

\- ¿Completo?

\- ¿Es duplicado?

\- ¿Se asocia a expediente principal?

\- ¿Corresponde atención?

\- ¿No corresponde?

\- ¿Cuál es el motivo de no corresponde?

\- ¿Incorporada?

\- ¿Actualizada en tablas maestras?

\- ¿Para reconstitución?

\- ¿Tiene legitimidad?

\- ¿Cumple medios probatorios?

\- ¿Resultado de evaluación?

\- ¿Procedente?

\- ¿Improcedente?

\- ¿Procedente en parte?

\- ¿En abandono?

\- ¿Observación administrativa?

\- ¿Requiere corrección?

\- ¿Tiene observación?

\- ¿Tipo de observación?

\- ¿Documento en proyecto, despacho o emitido?

\- ¿Documento firmado?

\- ¿Número de resolución registrado?

\- ¿Tipo de documento?

\- ¿Para despacho?

\- ¿Tipo de resolución?

\- ¿Estado indagatorio?

\- ¿Tipo de notificación?

\- ¿Notificación virtual exitosa?

\- ¿Primera notificación presencial exitosa?

\- ¿Segunda notificación presencial exitosa?

\- ¿Requiere publicación?

\- ¿Cargo de acuse recibido?

\- ¿Expediente digital completo?

\- ¿Tipo de error?

Estas decisiones deben reflejarse en:

\- EXPEDIENTE_EVALUACION;

\- TIPO_EVALUACION;

\- TIPO_RESULTADO_EVALUACION;

\- FLUJO_TRANSICION;

\- EXPEDIENTE_HISTORIAL;

\- EXPEDIENTE_OBSERVACION;

\- EXPEDIENTE_RESOLUCION;

\- EXPEDIENTE_DOCUMENTO;

\- EXPEDIENTE_NOTIFICACION;

\- EXPEDIENTE_CARGO_ACUSE;

\- EXPEDIENTE_PUBLICACION;

\- EXPEDIENTE_DIGITAL.

IMPORTANTE:

No crear una etapa visual llamada VALIDACION, porque las validaciones
son actividades transversales realizadas por distintos roles en
diferentes momentos del flujo.

Las validaciones deben modelarse como:

\- movimientos;

\- evaluaciones;

\- observaciones;

\- resultados;

\- reglas de transición;

\- documentos requeridos;

\- condiciones dentro de cada etapa.

Ejemplos:

\- Revisar archivo, completar datos o asociar duplicados pertenece a
REGISTRO o ASIGNACION.

\- Validar bandejas y asignar responsable pertenece a ASIGNACION.

\- Validar si corresponde atención, acta incorporada, legitimidad y
medios probatorios pertenece a ANALISIS.

\- Validar consistencia documental pertenece a VERIFICACION.

\- Validar tipo de resolución o documentos para firma pertenece a
FIRMA_EMISION.

\- Validar ejecución de resolución pertenece a EJECUCION.

\- Validar tipo de notificación, intentos y cargo pertenece a
NOTIFICACION.

\- Validar expediente digital completo pertenece a EXPEDIENTE_DIGITAL.

============================================================

7\. ETAPAS FUNCIONALES PRINCIPALES

============================================================

La barra visual de etapas del expediente debe representar solo
macroetapas claras del avance del expediente.

NO considerar una etapa llamada VALIDACION.

Etapas funcionales principales para el modelo y la interfaz:

1\. REGISTRO

2\. ASIGNACION

3\. ANALISIS

4\. VERIFICACION

5\. FIRMA_EMISION

6\. EJECUCION

7\. NOTIFICACION

8\. PUBLICACION

9\. EXPEDIENTE_DIGITAL

10\. CIERRE_ARCHIVO

Para la barra visual de etapas del expediente, usar:

REGISTRO -\> ASIGNACION -\> ANALISIS -\> VERIFICACION -\> FIRMA_EMISION
-\> EJECUCION -\> NOTIFICACION -\> PUBLICACION -\> EXPEDIENTE_DIGITAL
-\> CIERRE_ARCHIVO

Texto amigable para el usuario final:

Registro -\> Asignación -\> Análisis -\> Verificación -\> Firma/Emisión
-\> Ejecución -\> Notificación -\> Publicación -\> Expediente digital
-\> Cierre/Archivo

Regla visual:

\- PUBLICACION no siempre será obligatoria.

\- PUBLICACION solo se activa cuando se agotan la notificación virtual y
las dos presenciales sin éxito.

\- EXPEDIENTE_DIGITAL puede avanzar en paralelo o desde etapas
tempranas, pero debe visualizarse como estado de completitud/custodia.

\- La etapa actual debe estar en EXPEDIENTE.ID_ETAPA_ACTUAL.

\- El expediente puede tener indicadores paralelos, por ejemplo:
tiene_publicacion, expediente_digital_completo, cargos_pendientes.

============================================================

8\. ESTADOS SUGERIDOS

============================================================

Estados sugeridos para el nuevo modelo:

Registro:

\- REGISTRADO

\- DOCUMENTO_RECIBIDO

\- ARCHIVO_CARGADO

\- ARCHIVO_REVISADO

\- CODIGO_EXPEDIENTE_GENERADO

\- POTENCIAL_DUPLICADO

Asignación:

\- DUPLICADO_ASOCIADO

\- DOCUMENTO_ASOCIADO_EXPEDIENTE_PRINCIPAL

\- ASIGNADO

\- DERIVADO_DIRECTO_NOTIFICACION

Análisis:

\- RECIBIDO_POR_ABOGADO

\- EN_ANALISIS

\- CORRESPONDE

\- NO_CORRESPONDE

\- OBSERVADO

\- SUBSANADO

\- ATENDIDO

\- PROCEDENTE

\- IMPROCEDENTE

\- PROCEDENTE_EN_PARTE

\- EN_ABANDONO

\- OBSERVACION_ADMINISTRATIVA

Verificación:

\- EN_VERIFICACION

\- REQUIERE_CORRECCION

\- VERIFICADO

\- DOCUMENTO_REVERTIDO

\- DOCUMENTO_INCONSISTENTE

Firma / Emisión:

\- PARA_FIRMA

\- FIRMADO

\- NUMERO_RESOLUCION_REGISTRADO

\- EMITIDO

Ejecución:

\- EN_EJECUCION

\- INDAGATORIO

\- EJECUTADO

Notificación:

\- PARA_NOTIFICACION

\- NOTIFICACION_VIRTUAL_ENVIADA

\- NOTIFICACION_VIRTUAL_EXITOSA

\- NOTIFICACION_VIRTUAL_FALLIDA

\- PRIMERA_NOTIFICACION_PRESENCIAL_ENVIADA

\- PRIMERA_NOTIFICACION_PRESENCIAL_FALLIDA

\- SEGUNDA_NOTIFICACION_PRESENCIAL_ENVIADA

\- SEGUNDA_NOTIFICACION_PRESENCIAL_FALLIDA

\- NOTIFICADO

\- CARGO_RECIBIDO

\- CARGO_PENDIENTE

Publicación:

\- PARA_PUBLICACION

\- PUBLICACION_GENERADA

\- PUBLICACION_REALIZADA

Expediente digital:

\- EXPEDIENTE_DIGITAL_PENDIENTE

\- EXPEDIENTE_DIGITAL_INCOMPLETO

\- EXPEDIENTE_DIGITAL_COMPLETO

\- CUSTODIADO

Cierre / Archivo:

\- CERRADO

\- ARCHIVADO

\- RECHAZADO

\- DERIVADO_MUNICIPALIDAD

\- DERIVADO_SDPRC

\- DERIVACION_EXTERNA_PENDIENTE

\- REABIERTO

Mantener compatibilidad con estados legacy mediante ID_LEGACY o tabla
LEGACY_ESTADO_MAP.

============================================================

9\. MOVIMIENTOS SUGERIDOS

============================================================

Movimientos sugeridos:

Registro:

\- RECEPCION_DOCUMENTO

\- IMPORTACION_CARGA_DIARIA

\- CARGA_ARCHIVO

\- REVISION_ARCHIVO

\- GENERACION_CODIGO_EXPEDIENTE

\- MARCAR_POTENCIAL_DUPLICADO

Asignación:

\- ASOCIACION_DUPLICADO

\- ASOCIACION_DOCUMENTO_EXPEDIENTE

\- ASIGNACION

\- DERIVACION_DIRECTA_NOTIFICACION

Análisis:

\- RECEPCION_ASIGNACION

\- REGISTRO_CORRESPONDE

\- REGISTRO_NO_CORRESPONDE

\- ARCHIVO_NO_CORRESPONDE

\- DERIVACION_AREA_CORRESPONDIENTE

\- VALIDACION_ACTA_INCORPORADA

\- VALIDACION_TABLAS_MAESTRAS

\- SOLICITUD_CULMINACION_LINEA

\- VALIDACION_RECONSTITUCION

\- VALIDACION_LEGITIMIDAD

\- VALIDACION_MEDIOS_PROBATORIOS

\- REGISTRO_RESULTADO_ANALISIS

\- PROYECCION_RESOLUCION_PROCEDENCIA

\- PROYECCION_RESOLUCION_IMPROCEDENCIA

\- PROYECCION_RESOLUCION_PROCEDENTE_PARTE

\- REGISTRO_ABANDONO

\- REGISTRO_OBSERVACION_ADMINISTRATIVA

\- REGISTRO_DOCUMENTO_ANALIZADO

Verificación:

\- VALIDACION_CONSISTENCIA_DOCUMENTAL

\- REGISTRO_OBSERVACION_VERIFICACION

\- SOLICITUD_CORRECCION

\- REVERSION_ESTADO_DOCUMENTO

\- APROBACION_VERIFICACION

Firma / Emisión:

\- ENVIO_FIRMA

\- FIRMA_DOCUMENTO

\- REGISTRO_NUMERO_RESOLUCION

\- EMISION_DOCUMENTOS

Ejecución:

\- INICIO_EJECUCION

\- REGISTRO_INDAGATORIO

\- EJECUCION_RESOLUCION

\- REGISTRO_ANOTACION_TEXTUAL

\- REGISTRO_DOCUMENTO_ANALIZADO_EJECUCION

Notificación:

\- DERIVACION_NOTIFICACION

\- PROYECCION_CARTA_NOTIFICACION

\- NOTIFICACION_VIRTUAL

\- REGISTRO_RESULTADO_NOTIFICACION_VIRTUAL

\- NOTIFICACION_PRESENCIAL_1

\- REGISTRO_RESULTADO_NOTIFICACION_PRESENCIAL_1

\- NOTIFICACION_PRESENCIAL_2

\- REGISTRO_RESULTADO_NOTIFICACION_PRESENCIAL_2

\- RECEPCION_CARGO_ACUSE

\- REGISTRO_CARGO_ACUSE

Publicación:

\- GENERACION_PUBLICACION

\- REGISTRO_PUBLICACION

Expediente digital:

\- CREACION_CODIGO_EXPEDIENTE_DIGITAL

\- CREACION_CARPETA_EXPEDIENTE_DIGITAL

\- REGISTRO_LINK_EXPEDIENTE_DIGITAL

\- CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL

\- ACTUALIZACION_SISTEMA_CONTROL

\- CUSTODIA_EXPEDIENTE_DIGITAL

Cierre / Archivo:

\- CIERRE

\- ARCHIVO

\- RECHAZO

\- REAPERTURA

Nota:

Aunque algunos movimientos contienen la palabra VALIDACION, no deben
interpretarse como una etapa visual llamada VALIDACION. Son acciones
internas dentro de una etapa concreta.

============================================================

10\. TABLAS PRINCIPALES REQUERIDAS

============================================================

Diseñar el modelo con estas entidades o tablas principales.

10.1 Seguridad y organización:

\- USUARIO

\- ROL

\- USUARIO_ROL

\- AREA

\- EQUIPO

\- EQUIPO_USUARIO

\- USUARIO_SUPERVISION

10.2 Entidades externas y canales:

\- ENTIDAD_EXTERNA

\- CANAL_RECEPCION

10.3 Catálogos críticos:

\- ETAPA_EXPEDIENTE

\- ESTADO_EXPEDIENTE

\- TIPO_MOVIMIENTO

\- TIPO_DOCUMENTO

\- TIPO_ACTA

\- PROCEDIMIENTO_REGISTRAL

\- TIPO_DOCUMENTO_ADJUNTO

\- TIPO_OBSERVACION

\- TIPO_RESOLUCION

\- TIPO_NOTIFICACION

\- TIPO_EVALUACION

\- TIPO_RESULTADO_EVALUACION

\- TIPO_RESULTADO_EJECUCION

\- TIPO_DERIVACION

\- TIPO_PUBLICACION

\- ESTADO_DOCUMENTO

\- ESTADO_NOTIFICACION

\- ESTADO_CARGO_ACUSE

\- MOTIVO_ARCHIVO

\- MOTIVO_RECHAZO

\- MOTIVO_CORRECCION

\- MOTIVO_NO_CORRESPONDE

10.4 Flujo y reglas:

\- FLUJO

\- FLUJO_TRANSICION

\- FLUJO_TRANSICION_ROL

\- FLUJO_TRANSICION_EQUIPO

La tabla FLUJO_TRANSICION debe permitir validar:

\- etapa origen;

\- estado origen;

\- movimiento;

\- etapa destino;

\- estado destino;

\- rol autorizado;

\- equipo autorizado;

\- si requiere comentario;

\- si requiere documento;

\- si requiere usuario destino;

\- si permite entidad externa destino;

\- si cierra asignación activa;

\- si crea nueva asignación;

\- si mantiene abogado responsable;

\- si genera documento;

\- si requiere número de resolución;

\- si espera respuesta externa;

\- si finaliza expediente;

\- si archiva expediente;

\- si deriva directo a notificación;

\- si habilita publicación;

\- si está activa.

10.5 Expediente y datos principales:

\- EXPEDIENTE

\- EXPEDIENTE_SOLICITUD

\- EXPEDIENTE_ACTA

\- PERSONA

\- EXPEDIENTE_PERSONA

\- EXPEDIENTE_RELACION

EXPEDIENTE debe ser una tabla limpia, no gigante. Debe guardar:

\- ID_EXPEDIENTE

\- NUMERO_EXPEDIENTE

\- NUMERO_TRAMITE_DOCUMENTARIO

\- ID_PROCEDIMIENTO_REGISTRAL

\- ID_ETAPA_ACTUAL

\- ID_ESTADO_ACTUAL

\- ID_USUARIO_RESPONSABLE_ACTUAL

\- ID_USUARIO_ABOGADO_INICIAL

\- ID_EQUIPO_RESPONSABLE_ACTUAL

\- FECHA_REGISTRO

\- FECHA_ULTIMO_MOVIMIENTO

\- FECHA_VENCIMIENTO

\- PRIORIDAD

\- INDICADOR_OBSERVADO

\- INDICADOR_VENCIDO

\- INDICADOR_ARCHIVADO

\- INDICADOR_CERRADO

\- INDICADOR_REQUIERE_PUBLICACION

\- INDICADOR_EXPEDIENTE_DIGITAL_COMPLETO

\- ACTIVO

\- columnas de auditoría

No debe mezclar titulares, documentos, remitentes, actas y notificación
en una sola tabla.

EXPEDIENTE_RELACION debe permitir:

\- relacionar duplicados;

\- asociar cartas;

\- asociar oficios;

\- asociar documentos a expediente principal;

\- indicar expediente principal y expediente/documento asociado;

\- registrar tipo de relación;

\- registrar usuario asignador que asoció;

\- registrar fecha y comentario.

10.6 Evaluaciones del expediente:

Crear una tabla general:

\- EXPEDIENTE_EVALUACION

Debe permitir registrar evaluaciones como:

\- corresponde / no corresponde;

\- motivo de no corresponde;

\- expediente completo o incompleto;

\- acta incorporada o no incorporada;

\- acta actualizada en tablas maestras;

\- requiere culminación de línea;

\- requiere reconstitución;

\- tiene legitimidad;

\- medios probatorios válidos;

\- resultado de evaluación;

\- requiere corrección;

\- tipo de error: forma, fondo o error material;

\- para despacho;

\- tipo de resolución;

\- tipo de notificación;

\- expediente digital completo.

Campos sugeridos:

\- ID_EVALUACION

\- ID_EXPEDIENTE

\- ID_ETAPA

\- ID_TIPO_EVALUACION

\- RESULTADO

\- COMENTARIO

\- MOTIVO

\- ID_USUARIO_EVALUA

\- FECHA_EVALUACION

\- ACTIVO

\- columnas de auditoría

10.7 Documentos, resoluciones y documentos analizados:

Crear:

\- EXPEDIENTE_DOCUMENTO

\- EXPEDIENTE_DOCUMENTO_ANALIZADO

\- EXPEDIENTE_RESOLUCION

EXPEDIENTE_DOCUMENTO debe soportar:

\- documentos recibidos;

\- documentos generados;

\- informes;

\- resoluciones;

\- cartas de notificación;

\- oficios;

\- cargos;

\- documentos digitalizados;

\- archivos cargados;

\- documentos firmados.

EXPEDIENTE_DOCUMENTO_ANALIZADO debe registrar:

\- fecha;

\- tipo de documento;

\- descripción;

\- estado del documento: EN_PROYECTO, EN_DESPACHO, EMITIDO;

\- etapa en que se registra;

\- usuario que registra;

\- si fue observado;

\- si fue revertido por supervisor;

\- motivo de reversión.

EXPEDIENTE_RESOLUCION debe soportar:

\- tipo de resolución;

\- resultado: procedente, improcedente, procedente en parte;

\- número de resolución;

\- fecha de firma;

\- usuario que firma;

\- usuario supervisor que registra número;

\- estado: proyectada, firmada, emitida, ejecutada;

\- documento asociado.

10.8 Notificaciones, cargos y publicación:

Crear:

\- EXPEDIENTE_NOTIFICACION

\- EXPEDIENTE_CARGO_ACUSE

\- EXPEDIENTE_PUBLICACION

EXPEDIENTE_NOTIFICACION debe soportar:

\- notificación virtual;

\- primera notificación presencial;

\- segunda notificación presencial;

\- fecha de envío;

\- fecha de recepción;

\- resultado;

\- intento número;

\- canal;

\- usuario responsable;

\- documento asociado;

\- observación.

Regla:

\- El flujo estándar consta de una notificación virtual y dos
presenciales.

\- Si las tres instancias no tienen éxito, se habilita publicación.

EXPEDIENTE_CARGO_ACUSE debe soportar:

\- cargo recibido;

\- fecha de cargo;

\- documento de cargo;

\- resultado;

\- observación;

\- relación con intento de notificación.

EXPEDIENTE_PUBLICACION debe soportar:

\- expediente relacionado;

\- motivo de publicación;

\- fecha de generación;

\- fecha de publicación;

\- medio de publicación si aplica;

\- estado de publicación;

\- documento asociado.

10.9 Derivaciones externas:

Crear:

\- EXPEDIENTE_DERIVACION_EXTERNA

Debe soportar:

\- derivación a municipalidad;

\- derivación a oficina registral;

\- derivación a SDPRC;

\- derivación a área correspondiente por No Corresponde;

\- solicitud de culminación de línea;

\- oficio enviado;

\- fecha envío;

\- fecha respuesta;

\- estado de respuesta;

\- documento asociado;

\- usuario interno responsable.

10.10 Expediente digital:

Crear:

\- EXPEDIENTE_DIGITAL

Debe soportar:

\- código de expediente digital;

\- ruta de carpeta;

\- link de acceso;

\- fecha de creación;

\- usuario que crea;

\- usuario abogado responsable;

\- estado de completitud;

\- fecha de custodia;

\- usuario custodio;

\- requiere_asignacion_especifica;

\- id_usuario_asignado_expediente_digital, opcional.

También debe relacionarse con EXPEDIENTE_DOCUMENTO para conocer qué
documentos forman parte del expediente digital.

10.11 Asignaciones:

Crear:

\- EXPEDIENTE_ASIGNACION

Debe registrar responsables por etapa:

\- ID_ASIGNACION

\- ID_EXPEDIENTE

\- ID_USUARIO_ASIGNADO

\- ID_EQUIPO_ASIGNADO

\- ID_ROL_FUNCIONAL

\- ID_ETAPA

\- ID_ESTADO

\- FECHA_ASIGNACION

\- FECHA_RECEPCION

\- FECHA_FINALIZACION

\- ACTIVA

\- ES_ABOGADO_PRINCIPAL

\- OBSERVACION

\- USUARIO_ASIGNA

\- columnas de auditoría

Regla del acta:

\- La asignación inicial del abogado debe mantenerse durante análisis y
ejecución.

\- La etapa EJECUCION no debe generar nueva asignación obligatoria.

\- La asignación puede seguir activa o referenciarse como abogado
principal.

\- Si se reasigna excepcionalmente, cerrar asignación anterior, crear
nueva y registrar historial.

Debe impedir duplicidad de asignaciones activas para el mismo expediente
y etapa, salvo regla funcional explícita.

10.12 Historial funcional:

Crear:

\- EXPEDIENTE_HISTORIAL

Debe registrar toda acción relevante del BPMN y acta:

\- recepción;

\- importación;

\- carga;

\- revisión;

\- asociación de duplicado;

\- asignación;

\- derivación;

\- evaluación;

\- observación;

\- corrección;

\- reversión de documento;

\- firma;

\- registro de número de resolución;

\- emisión;

\- ejecución;

\- indagatorio;

\- notificación;

\- cargo de acuse;

\- publicación;

\- expediente digital;

\- custodia;

\- archivo;

\- cierre.

Campos mínimos:

\- ID_HISTORIAL

\- ID_EXPEDIENTE

\- ID_TIPO_MOVIMIENTO

\- ID_ETAPA_ORIGEN

\- ID_ESTADO_ORIGEN

\- ID_ETAPA_DESTINO

\- ID_ESTADO_DESTINO

\- ID_USUARIO_ORIGEN

\- ID_USUARIO_DESTINO

\- ID_EQUIPO_ORIGEN

\- ID_EQUIPO_DESTINO

\- ID_ENTIDAD_EXTERNA_ORIGEN

\- ID_ENTIDAD_EXTERNA_DESTINO

\- ID_ASIGNACION_ORIGEN

\- ID_ASIGNACION_DESTINO

\- ID_DOCUMENTO

\- ID_RESOLUCION

\- ID_NOTIFICACION

\- ID_PUBLICACION

\- COMENTARIO

\- MOTIVO

\- FECHA_MOVIMIENTO

\- CREADO_POR

\- CREADO_EN

Regla obligatoria:

No debe existir cambio de estado, etapa, responsable, asignación,
derivación, firma, reversión, notificación, cargo, publicación, archivo
o cierre sin registro en EXPEDIENTE_HISTORIAL.

10.13 Observaciones:

Crear:

\- EXPEDIENTE_OBSERVACION

Debe soportar observaciones por etapa:

\- análisis;

\- verificación;

\- ejecución;

\- notificación;

\- expediente digital;

\- cierre.

Regla del acta:

\- En Expedientes por Trabajar, los campos de observación deben verse en
modo lectura.

\- Solo se habilitan en Expediente por Verificar.

10.14 Plazos y alertas:

Crear:

\- PLAZO_CONFIGURACION

\- EXPEDIENTE_PLAZO

\- EXPEDIENTE_ALERTA

Debe soportar:

\- plazo por procedimiento;

\- plazo por tipo de documento;

\- plazo por etapa;

\- días hábiles o calendario;

\- fecha vencimiento calculada;

\- alerta próxima a vencer;

\- alerta vencida;

\- semáforo visual.

10.15 Auditoría técnica:

Crear:

\- AUDITORIA_EVENTO

Debe registrar:

\- tabla afectada;

\- id registro;

\- operación;

\- usuario;

\- fecha;

\- valor anterior;

\- valor nuevo;

\- módulo;

\- origen de acción;

\- IP o equipo si aplica.

Diferenciar claramente:

\- EXPEDIENTE_HISTORIAL = trazabilidad funcional.

\- AUDITORIA_EVENTO = auditoría técnica de cambios.

============================================================

11\. MVP OBLIGATORIO Y FASE 2 PARA NO SOBREDIMENSIONAR

============================================================

No quiero una base innecesariamente gigante desde el primer intento.

Diseñar en dos niveles:

11.1 MVP obligatorio:

Estas tablas son obligatorias para la primera versión porque soportan el
núcleo del flujo, bandejas, consola, historial y rediseño visual:

\- EXPEDIENTE

\- EXPEDIENTE_SOLICITUD

\- EXPEDIENTE_ACTA

\- PERSONA

\- EXPEDIENTE_PERSONA

\- EXPEDIENTE_RELACION

\- EXPEDIENTE_DOCUMENTO

\- EXPEDIENTE_DOCUMENTO_ANALIZADO

\- EXPEDIENTE_ASIGNACION

\- EXPEDIENTE_HISTORIAL

\- EXPEDIENTE_EVALUACION

\- EXPEDIENTE_OBSERVACION

\- EXPEDIENTE_RESOLUCION

\- EXPEDIENTE_NOTIFICACION

\- EXPEDIENTE_CARGO_ACUSE

\- EXPEDIENTE_DIGITAL

\- USUARIO

\- ROL

\- USUARIO_ROL

\- EQUIPO

\- EQUIPO_USUARIO

\- ENTIDAD_EXTERNA

\- CANAL_RECEPCION

\- ETAPA_EXPEDIENTE

\- ESTADO_EXPEDIENTE

\- TIPO_MOVIMIENTO

\- FLUJO

\- FLUJO_TRANSICION

\- AUDITORIA_EVENTO

\- LEGACY_ESTADO_MAP

\- LEGACY_CATALOGO_MAP

11.2 Fase 2:

Estas tablas pueden quedar para una segunda fase, salvo que sean
estrictamente necesarias desde el inicio:

\- EXPEDIENTE_DERIVACION_EXTERNA

\- EXPEDIENTE_PUBLICACION

\- EXPEDIENTE_PLAZO

\- EXPEDIENTE_ALERTA

\- PLAZO_CONFIGURACION

\- FLUJO_TRANSICION_ROL

\- FLUJO_TRANSICION_EQUIPO

\- PERMISO

\- ROL_PERMISO

\- EXPEDIENTE_METRICA_ETAPA

\- AUDITORIA_DETALLE_CAMBIO si se requiere granularidad adicional

Nota:

Aunque EXPEDIENTE_PUBLICACION puede ser Fase 2, dejar diseñada la
relación desde notificación para poder activarla cuando SDRERC entregue
la matriz Excel.

Criterio:

Si una funcionalidad puede resolverse al inicio con
EXPEDIENTE_HISTORIAL, EXPEDIENTE_DOCUMENTO, EXPEDIENTE_EVALUACION o
EXPEDIENTE_OBSERVACION, no crear una tabla demasiado específica todavía.

Crear tablas específicas solo cuando:

\- tengan datos propios muy diferenciados;

\- sean necesarias para reportes;

\- tengan reglas de negocio particulares;

\- sean necesarias para la interfaz visual;

\- sean necesarias para integridad y trazabilidad.

============================================================

12\. RELACIONES PRINCIPALES

============================================================

Definir relaciones obligatorias:

\- EXPEDIENTE.ID_PROCEDIMIENTO_REGISTRAL -\>
PROCEDIMIENTO_REGISTRAL.ID_PROCEDIMIENTO_REGISTRAL

\- EXPEDIENTE.ID_ETAPA_ACTUAL -\> ETAPA_EXPEDIENTE.ID_ETAPA

\- EXPEDIENTE.ID_ESTADO_ACTUAL -\> ESTADO_EXPEDIENTE.ID_ESTADO

\- EXPEDIENTE.ID_USUARIO_RESPONSABLE_ACTUAL -\> USUARIO.ID_USUARIO

\- EXPEDIENTE.ID_USUARIO_ABOGADO_INICIAL -\> USUARIO.ID_USUARIO

\- EXPEDIENTE.ID_EQUIPO_RESPONSABLE_ACTUAL -\> EQUIPO.ID_EQUIPO

\- EXPEDIENTE_SOLICITUD.ID_EXPEDIENTE -\> EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_SOLICITUD.ID_ENTIDAD_ORIGEN -\>
ENTIDAD_EXTERNA.ID_ENTIDAD_EXTERNA

\- EXPEDIENTE_SOLICITUD.ID_CANAL_RECEPCION -\>
CANAL_RECEPCION.ID_CANAL_RECEPCION

\- EXPEDIENTE_SOLICITUD.ID_PERSONA_SOLICITANTE -\> PERSONA.ID_PERSONA

\- EXPEDIENTE_ACTA.ID_EXPEDIENTE -\> EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_ACTA.ID_TIPO_ACTA -\> TIPO_ACTA.ID_TIPO_ACTA

\- EXPEDIENTE_PERSONA.ID_EXPEDIENTE -\> EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_PERSONA.ID_PERSONA -\> PERSONA.ID_PERSONA

\- EXPEDIENTE_RELACION.ID_EXPEDIENTE_PRINCIPAL -\>
EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_RELACION.ID_EXPEDIENTE_RELACIONADO -\>
EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_DOCUMENTO.ID_EXPEDIENTE -\> EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_DOCUMENTO.ID_TIPO_DOCUMENTO_ADJUNTO -\>
TIPO_DOCUMENTO_ADJUNTO.ID_TIPO_DOCUMENTO_ADJUNTO

\- EXPEDIENTE_DOCUMENTO.ID_ETAPA -\> ETAPA_EXPEDIENTE.ID_ETAPA

\- EXPEDIENTE_DOCUMENTO_ANALIZADO.ID_EXPEDIENTE -\>
EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_DOCUMENTO_ANALIZADO.ID_DOCUMENTO -\>
EXPEDIENTE_DOCUMENTO.ID_DOCUMENTO

\- EXPEDIENTE_RESOLUCION.ID_EXPEDIENTE -\> EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_RESOLUCION.ID_DOCUMENTO -\>
EXPEDIENTE_DOCUMENTO.ID_DOCUMENTO

\- EXPEDIENTE_NOTIFICACION.ID_EXPEDIENTE -\> EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_NOTIFICACION.ID_DOCUMENTO -\>
EXPEDIENTE_DOCUMENTO.ID_DOCUMENTO

\- EXPEDIENTE_CARGO_ACUSE.ID_EXPEDIENTE -\> EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_CARGO_ACUSE.ID_NOTIFICACION -\>
EXPEDIENTE_NOTIFICACION.ID_NOTIFICACION

\- EXPEDIENTE_PUBLICACION.ID_EXPEDIENTE -\> EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_DIGITAL.ID_EXPEDIENTE -\> EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_ASIGNACION.ID_EXPEDIENTE -\> EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_ASIGNACION.ID_USUARIO_ASIGNADO -\> USUARIO.ID_USUARIO

\- EXPEDIENTE_ASIGNACION.ID_EQUIPO_ASIGNADO -\> EQUIPO.ID_EQUIPO

\- EXPEDIENTE_ASIGNACION.ID_ROL_FUNCIONAL -\> ROL.ID_ROL

\- EXPEDIENTE_ASIGNACION.ID_ETAPA -\> ETAPA_EXPEDIENTE.ID_ETAPA

\- EXPEDIENTE_ASIGNACION.ID_ESTADO -\> ESTADO_EXPEDIENTE.ID_ESTADO

\- EXPEDIENTE_HISTORIAL.ID_EXPEDIENTE -\> EXPEDIENTE.ID_EXPEDIENTE

\- EXPEDIENTE_HISTORIAL.ID_TIPO_MOVIMIENTO -\>
TIPO_MOVIMIENTO.ID_TIPO_MOVIMIENTO

\- EXPEDIENTE_HISTORIAL.ID_ETAPA_ORIGEN -\> ETAPA_EXPEDIENTE.ID_ETAPA

\- EXPEDIENTE_HISTORIAL.ID_ETAPA_DESTINO -\> ETAPA_EXPEDIENTE.ID_ETAPA

\- EXPEDIENTE_HISTORIAL.ID_ESTADO_ORIGEN -\> ESTADO_EXPEDIENTE.ID_ESTADO

\- EXPEDIENTE_HISTORIAL.ID_ESTADO_DESTINO -\>
ESTADO_EXPEDIENTE.ID_ESTADO

\- EXPEDIENTE_HISTORIAL.ID_USUARIO_ORIGEN -\> USUARIO.ID_USUARIO

\- EXPEDIENTE_HISTORIAL.ID_USUARIO_DESTINO -\> USUARIO.ID_USUARIO

\- EXPEDIENTE_HISTORIAL.ID_EQUIPO_ORIGEN -\> EQUIPO.ID_EQUIPO

\- EXPEDIENTE_HISTORIAL.ID_EQUIPO_DESTINO -\> EQUIPO.ID_EQUIPO

\- EXPEDIENTE_HISTORIAL.ID_ENTIDAD_EXTERNA_ORIGEN -\>
ENTIDAD_EXTERNA.ID_ENTIDAD_EXTERNA

\- EXPEDIENTE_HISTORIAL.ID_ENTIDAD_EXTERNA_DESTINO -\>
ENTIDAD_EXTERNA.ID_ENTIDAD_EXTERNA

\- FLUJO_TRANSICION.ID_ETAPA_ORIGEN -\> ETAPA_EXPEDIENTE.ID_ETAPA

\- FLUJO_TRANSICION.ID_ESTADO_ORIGEN -\> ESTADO_EXPEDIENTE.ID_ESTADO

\- FLUJO_TRANSICION.ID_TIPO_MOVIMIENTO -\>
TIPO_MOVIMIENTO.ID_TIPO_MOVIMIENTO

\- FLUJO_TRANSICION.ID_ETAPA_DESTINO -\> ETAPA_EXPEDIENTE.ID_ETAPA

\- FLUJO_TRANSICION.ID_ESTADO_DESTINO -\> ESTADO_EXPEDIENTE.ID_ESTADO

============================================================

13\. DISEÑO FUNCIONAL DE TRANSICIONES

============================================================

Toda acción de flujo debe validarse contra FLUJO_TRANSICION.

Regla base:

El sistema debe buscar en FLUJO_TRANSICION si la combinación actual es
válida:

\- etapa actual;

\- estado actual;

\- movimiento solicitado;

\- rol del usuario;

\- equipo del usuario.

Si existe transición activa:

\- permite avanzar;

\- actualiza etapa/estado en EXPEDIENTE;

\- cierra asignación activa anterior si corresponde;

\- crea nueva asignación si corresponde;

\- mantiene abogado responsable inicial cuando corresponda;

\- actualiza responsable actual;

\- inserta historial;

\- registra auditoría;

\- registra documento, evaluación, observación, resolución,
notificación, cargo, publicación o expediente digital si aplica.

Si no existe transición:

\- bloquear acción;

\- mostrar mensaje funcional:

"No está permitida esta acción para el estado actual del expediente o
para su rol".

Casos de responsable:

A. Movimiento sin cambio de responsable

Ejemplo:

\- recibir expediente;

\- analizar expediente;

\- evaluar expediente;

\- pasar a ejecución cuando el mismo abogado continúa.

Se mantiene el mismo responsable actual.

B. Movimiento con cambio de responsable interno

Ejemplo:

\- asignar inicialmente;

\- derivar a notificación;

\- reasignar excepcionalmente;

\- asignar cargo de acuse si se define.

Se cierra asignación activa anterior y se crea nueva asignación si
corresponde.

C. Movimiento con entidad externa

Ejemplo:

\- derivar a SDPRC;

\- derivar a municipalidad;

\- enviar oficio externo;

\- derivar a área correspondiente por No Corresponde.

No se crea usuario interno para la entidad externa.

Se registra la entidad externa destino y el usuario interno responsable
del seguimiento.

D. Movimiento de cierre

Ejemplo:

\- cerrar expediente;

\- archivar expediente.

Se finaliza asignación activa y el expediente queda cerrado/archivado.

============================================================

14\. VISTAS PARA JAVA SWING Y REDISEÑO VISUAL

============================================================

Crear vistas para alimentar la interfaz Java Swing/FlatLaf sin consultas
complejas en formularios:

\- VW_EXPEDIENTE_BANDEJA

\- VW_EXPEDIENTE_CONSOLA

\- VW_EXPEDIENTE_DETALLE

\- VW_EXPEDIENTE_TIMELINE

\- VW_EXPEDIENTE_HISTORIAL

\- VW_EXPEDIENTE_DOCUMENTOS

\- VW_EXPEDIENTE_DOCUMENTOS_ANALIZADOS

\- VW_EXPEDIENTE_PERSONAS

\- VW_EXPEDIENTE_EVALUACIONES

\- VW_EXPEDIENTE_RESOLUCIONES

\- VW_EXPEDIENTE_NOTIFICACIONES

\- VW_EXPEDIENTE_CARGOS_ACUSE

\- VW_EXPEDIENTE_PUBLICACIONES

\- VW_EXPEDIENTE_DERIVACIONES

\- VW_EXPEDIENTE_DIGITAL

\- VW_EXPEDIENTE_ALERTAS

\- VW_EXPEDIENTE_ACCIONES_PERMITIDAS

\- VW_EXPEDIENTE_RESPONSABLE_ACTUAL

La bandeja debe mostrar:

\- ID expediente;

\- número de expediente;

\- número de trámite;

\- número de acta;

\- titular principal;

\- remitente;

\- entidad origen;

\- canal de recepción;

\- procedimiento;

\- etapa actual;

\- estado actual;

\- abogado responsable inicial;

\- responsable actual;

\- equipo actual;

\- fecha registro;

\- fecha último movimiento;

\- fecha vencimiento;

\- días restantes;

\- alerta visual;

\- tiene observaciones;

\- tiene documentos pendientes;

\- tiene documentos inconsistentes;

\- tiene documentos analizados;

\- tiene derivación externa;

\- tiene expediente digital;

\- tiene cargo pendiente;

\- requiere publicación;

\- número de resolución;

\- prioridad.

La consola debe mostrar:

\- encabezado del expediente;

\- barra visual de etapas;

\- abogado responsable inicial;

\- responsable actual;

\- última derivación;

\- documentos generados;

\- documentos analizados;

\- evaluaciones;

\- resolución y número de resolución;

\- intentos de notificación;

\- cargos de acuse;

\- publicación;

\- historial/timeline;

\- alertas;

\- próximas acciones permitidas;

\- datos externos relevantes como solicitante, entidad origen, entidad
destino u oficio.

============================================================

15\. BANDEJAS FUNCIONALES SUGERIDAS

============================================================

Diseñar vistas o filtros para:

1\. Bandeja de recepción.

2\. Bandeja de asignador.

3\. Bandeja de abogado de análisis / expedientes por trabajar.

4\. Bandeja de supervisor / expedientes por verificar.

5\. Bandeja de correcciones.

6\. Bandeja de firma.

7\. Bandeja de ejecución por trabajar.

8\. Bandeja de notificación por trabajar.

9\. Bandeja de cargos de acuse.

10\. Bandeja de publicación.

11\. Bandeja de expedientes digitales.

12\. Bandeja de archivados/cerrados.

13\. Bandeja de derivados externos pendientes, gestionada por usuarios
internos.

No crear bandejas para:

\- SDPRC

\- Ciudadano / Entidad

\- OGD

No crear una bandeja llamada VALIDACION, salvo que internamente se
requiera una consulta operativa. Visual y funcionalmente las
validaciones deben pertenecer a las etapas correspondientes.

Regla del acta:

\- La Bandeja de Ejecución no debe requerir nueva asignación
independiente.

\- Debe mostrar expedientes del mismo abogado asignado inicialmente
cuando pasan a ejecución.

\- La Bandeja de Notificación debe recibir también casos derivados
directamente por abandono, observación administrativa,
duplicados/cartas/documentos asociados, o según regla funcional.

============================================================

16\. DISEÑO VISUAL DE LA CONSOLA DEL EXPEDIENTE

============================================================

La consola de expediente debe ser una vista tipo caso/CRM.

Debe tener estas secciones:

A. Encabezado del expediente

Mostrar:

\- número de expediente;

\- número de trámite documentario;

\- procedimiento;

\- titular principal;

\- acta;

\- estado actual;

\- etapa actual;

\- abogado responsable inicial;

\- responsable actual;

\- equipo actual;

\- fecha de registro;

\- fecha de vencimiento;

\- días restantes o vencidos;

\- entidad origen;

\- canal de recepción;

\- número de resolución;

\- indicador de cargo de acuse;

\- indicador de publicación;

\- indicador de expediente digital.

B. Barra visual de etapas

Mostrar etapas:

REGISTRO -\> ASIGNACION -\> ANALISIS -\> VERIFICACION -\> FIRMA_EMISION
-\> EJECUCION -\> NOTIFICACION -\> PUBLICACION -\> EXPEDIENTE_DIGITAL
-\> CIERRE_ARCHIVO

Regla visual:

\- etapas completadas en verde;

\- etapa actual en azul;

\- etapas pendientes en gris;

\- observadas en naranja;

\- vencidas o críticas en rojo;

\- archivadas/cerradas en gris oscuro.

\- PUBLICACION debe aparecer como etapa condicional o badge cuando
aplique.

\- EXPEDIENTE_DIGITAL puede mostrarse como etapa o tarjeta de
completitud/custodia.

No mostrar una etapa llamada VALIDACION.

C. Panel central con pestañas

Tabs:

\- Detalles

\- Solicitudes

\- Actas

\- Personas

\- Documentos

\- Documentos analizados

\- Evaluaciones

\- Resoluciones

\- Notificaciones

\- Cargos de acuse

\- Publicación

\- Observaciones

\- Expediente digital

\- Historial

\- Auditoría

D. Panel lateral derecho

Tarjetas:

\- Abogado responsable inicial

\- Responsable actual

\- Última derivación

\- Eventos clave

\- Alertas de plazo

\- Documentos recientes

\- Documentos pendientes/inconsistentes

\- Observaciones pendientes

\- Próxima acción permitida

\- Número de resolución

\- Intentos de notificación

\- Cargo de acuse

\- Publicación

\- Derivación externa pendiente

\- Estado del expediente digital

E. Timeline / historial

Mostrar como línea de tiempo:

\- fecha;

\- movimiento;

\- usuario origen;

\- usuario destino;

\- equipo origen;

\- equipo destino;

\- entidad externa origen/destino si aplica;

\- estado anterior;

\- estado nuevo;

\- documento relacionado;

\- resolución relacionada;

\- notificación relacionada;

\- publicación relacionada;

\- comentario;

\- motivo.

============================================================

17\. REGLAS DE HISTORIAL FUNCIONAL

============================================================

Cada cambio funcional relevante debe insertar en EXPEDIENTE_HISTORIAL.

Regla obligatoria:

No debe existir cambio de etapa, estado, responsable, asignación,
derivación, firma, reversión, notificación, cargo, publicación, archivo
o cierre sin historial.

Ejemplos:

A. Registro:

\- movimiento: RECEPCION_DOCUMENTO, IMPORTACION_CARGA_DIARIA,
CARGA_ARCHIVO, REVISION_ARCHIVO o GENERACION_CODIGO_EXPEDIENTE

\- origen: nulo

\- destino: REGISTRO / REGISTRADO

\- usuario origen: usuario que registra

\- comentario: expediente registrado.

B. Asociación de duplicado/documento:

\- movimiento: ASOCIACION_DUPLICADO o ASOCIACION_DOCUMENTO_EXPEDIENTE

\- usuario origen: asignador

\- debe registrar expediente principal y expediente/documento asociado.

C. Asignación:

\- movimiento: ASIGNACION

\- estado origen: REGISTRADO

\- estado destino: ASIGNADO

\- usuario origen: asignador o supervisor

\- usuario destino: abogado asignado

\- equipo destino: equipo jurídico correspondiente.

\- marcar abogado como responsable inicial si aplica.

D. Recepción:

\- movimiento: RECEPCION_ASIGNACION

\- estado origen: ASIGNADO

\- estado destino: RECIBIDO_POR_ABOGADO

\- usuario origen/destino: abogado asignado.

E. Atención / análisis:

\- movimiento: REGISTRO_CORRESPONDE, REGISTRO_NO_CORRESPONDE,
VALIDACION_ACTA_INCORPORADA, VALIDACION_MEDIOS_PROBATORIOS o
REGISTRO_RESULTADO_ANALISIS

\- estado origen: RECIBIDO_POR_ABOGADO o EN_ANALISIS

\- estado destino: EN_ANALISIS, OBSERVADO, ATENDIDO, NO_CORRESPONDE,
EN_ABANDONO u OBSERVACION_ADMINISTRATIVA

\- usuario origen/destino: abogado que atiende.

F. Verificación:

\- movimiento: VALIDACION_CONSISTENCIA_DOCUMENTAL

\- estado origen: ATENDIDO

\- estado destino: VERIFICADO o REQUIERE_CORRECCION

\- usuario origen: supervisor que verifica.

G. Reversión de documento:

\- movimiento: REVERSION_ESTADO_DOCUMENTO

\- usuario origen: supervisor

\- debe registrar documento afectado, estado anterior, estado nuevo y
motivo.

H. Firma:

\- movimiento: FIRMA_DOCUMENTO

\- estado origen: PARA_FIRMA

\- estado destino: FIRMADO

\- usuario origen: usuario que firma.

I. Registro de número de resolución:

\- movimiento: REGISTRO_NUMERO_RESOLUCION

\- usuario origen: supervisor

\- debe registrar número de resolución y resolución asociada.

J. Ejecución:

\- movimiento: INICIO_EJECUCION, REGISTRO_INDAGATORIO,
EJECUCION_RESOLUCION o REGISTRO_ANOTACION_TEXTUAL

\- debe mantener abogado responsable inicial salvo reasignación
excepcional.

K. Notificación:

\- movimiento: NOTIFICACION_VIRTUAL, NOTIFICACION_PRESENCIAL_1,
NOTIFICACION_PRESENCIAL_2

\- debe registrar intento y resultado.

L. Cargo:

\- movimiento: RECEPCION_CARGO_ACUSE

\- debe registrar cargo asociado a notificación.

M. Publicación:

\- movimiento: GENERACION_PUBLICACION o REGISTRO_PUBLICACION

\- solo aplica cuando se agotaron intentos de notificación sin éxito.

N. Expediente digital:

\- movimiento: CREACION_CARPETA_EXPEDIENTE_DIGITAL,
REGISTRO_LINK_EXPEDIENTE_DIGITAL, CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL o
CUSTODIA_EXPEDIENTE_DIGITAL

\- usuario: abogado responsable o custodio según definición.

O. Cierre:

\- movimiento: CIERRE

\- estado destino: CERRADO.

P. Archivo:

\- movimiento: ARCHIVO

\- estado destino: ARCHIVADO.

============================================================

18\. REGLAS DE AUDITORÍA TÉCNICA

============================================================

La auditoría técnica debe estar separada del historial funcional.

AUDITORIA_EVENTO debe registrar:

\- tabla afectada;

\- id del registro;

\- operación: INSERT, UPDATE, DELETE lógico;

\- usuario;

\- fecha;

\- valor anterior;

\- valor nuevo;

\- origen acción;

\- módulo;

\- IP o nombre del equipo si aplica.

Se debe auditar especialmente:

\- cambios en EXPEDIENTE;

\- cambios de estado;

\- cambios de responsable;

\- cambios de asignación;

\- cambios en documentos;

\- cambios en documentos analizados;

\- reversión de documentos;

\- cambios en análisis/evaluaciones;

\- cambios en observaciones;

\- registro o modificación del número de resolución;

\- cambios en notificación;

\- cambios en cargos de acuse;

\- cambios en publicación;

\- cambios en expediente digital;

\- cambios en tablas maestras del flujo.

Diferencia importante:

\- EXPEDIENTE_HISTORIAL registra la trazabilidad funcional del
expediente.

\- AUDITORIA_EVENTO registra cambios técnicos o sensibles en datos.

============================================================

19\. REGLAS PARA DISEÑO FÍSICO ORACLE

============================================================

Aplicar estas reglas:

\- Todas las tablas deben tener PK.

\- Todas las relaciones importantes deben tener FK.

\- Los catálogos críticos deben tener CODIGO único y NOMBRE.

\- Las tablas transaccionales deben tener CREADO_POR, CREADO_EN,
MODIFICADO_POR, MODIFICADO_EN, ACTIVO.

\- Usar GENERATED BY DEFAULT AS IDENTITY si la versión de Oracle XE lo
soporta.

\- Crear índices para búsquedas frecuentes.

\- Crear unique constraints donde corresponda.

\- Crear check constraints para flags como ACTIVO, FINALIZA_EXPEDIENTE,
REQUIERE_COMENTARIO.

\- No otorgar privilegio DBA al usuario SDRERC_APP.

\- No usar SYSTEM como dueño final de tablas.

\- No borrar tablas actuales.

\- No ejecutar scripts automáticamente.

\- No inventar columnas actuales si no están confirmadas.

\- Si falta información del DDL real, generar consultas para confirmar
nombres reales.

============================================================

20\. ÍNDICES RECOMENDADOS

============================================================

Crear índices sobre:

EXPEDIENTE:

\- NUMERO_EXPEDIENTE

\- NUMERO_TRAMITE_DOCUMENTARIO

\- ID_ESTADO_ACTUAL

\- ID_ETAPA_ACTUAL

\- ID_USUARIO_RESPONSABLE_ACTUAL

\- ID_USUARIO_ABOGADO_INICIAL

\- ID_EQUIPO_RESPONSABLE_ACTUAL

\- FECHA_REGISTRO

\- FECHA_VENCIMIENTO

\- FECHA_ULTIMO_MOVIMIENTO

\- INDICADOR_REQUIERE_PUBLICACION

\- INDICADOR_EXPEDIENTE_DIGITAL_COMPLETO

EXPEDIENTE_SOLICITUD:

\- ID_EXPEDIENTE

\- NUMERO_TRAMITE_DOCUMENTARIO

\- ID_ENTIDAD_ORIGEN

\- ID_CANAL_RECEPCION

\- ID_PERSONA_SOLICITANTE

EXPEDIENTE_PERSONA:

\- ID_EXPEDIENTE

\- ID_PERSONA

\- TIPO_RELACION_PERSONA

PERSONA:

\- NUMERO_DOCUMENTO

\- NOMBRES

\- APELLIDOS

\- CORREO_ELECTRONICO

EXPEDIENTE_ACTA:

\- NUMERO_ACTA

\- ID_TIPO_ACTA

EXPEDIENTE_RELACION:

\- ID_EXPEDIENTE_PRINCIPAL

\- ID_EXPEDIENTE_RELACIONADO

\- TIPO_RELACION

EXPEDIENTE_HISTORIAL:

\- ID_EXPEDIENTE

\- FECHA_MOVIMIENTO

\- ID_TIPO_MOVIMIENTO

\- ID_USUARIO_DESTINO

\- ID_ESTADO_DESTINO

\- ID_ETAPA_DESTINO

EXPEDIENTE_ASIGNACION:

\- ID_EXPEDIENTE

\- ID_USUARIO_ASIGNADO

\- ID_EQUIPO_ASIGNADO

\- ID_ETAPA

\- ACTIVA

\- ES_ABOGADO_PRINCIPAL

EXPEDIENTE_DOCUMENTO:

\- ID_EXPEDIENTE

\- ID_TIPO_DOCUMENTO_ADJUNTO

\- HASH_ARCHIVO

EXPEDIENTE_RESOLUCION:

\- ID_EXPEDIENTE

\- NUMERO_RESOLUCION

EXPEDIENTE_NOTIFICACION:

\- ID_EXPEDIENTE

\- TIPO_NOTIFICACION

\- NUMERO_INTENTO

\- ESTADO_NOTIFICACION

EXPEDIENTE_CARGO_ACUSE:

\- ID_EXPEDIENTE

\- ID_NOTIFICACION

EXPEDIENTE_PUBLICACION:

\- ID_EXPEDIENTE

\- ESTADO_PUBLICACION

EXPEDIENTE_DERIVACION_EXTERNA:

\- ID_EXPEDIENTE

\- ID_ENTIDAD_DESTINO

\- ESTADO_RESPUESTA

\- FECHA_ENVIO

\- FECHA_RESPUESTA

============================================================

21\. COMPATIBILIDAD GRADUAL CON SISTEMA LEGACY

============================================================

Como el sistema actual tiene IDs hardcodeados, crear una estrategia de
transición:

A. Mantener base actual en SYSTEM sin borrar nada.

B. Crear SDRERC_APP en paralelo.

C. Crear tablas de equivalencia:

\- LEGACY_ESTADO_MAP

\- LEGACY_CATALOGO_MAP

D. Usar columna ID_LEGACY en catálogos críticos.

E. Crear vistas de compatibilidad si ayuda a que Java lea datos con
nombres similares.

F. Migrar primero maestros.

G. Migrar luego un conjunto piloto de expedientes.

H. Validar conteos.

I. Adaptar Java por módulos, no todo de golpe.

J. Mantener rama Git separada para esta reestructuración.

No cambiar la conexión Java a SDRERC_APP hasta que:

\- el esquema esté creado;

\- los datos maestros estén cargados;

\- las vistas principales existan;

\- una migración piloto esté validada;

\- las pantallas principales puedan leer desde vistas nuevas.

============================================================

22\. SCRIPTS SOLICITADOS PARA DBEAVER

============================================================

Luego de validar el modelo, generar scripts separados y ordenados:

SCRIPT 00 - Diagnóstico previo

Consultas SELECT para validar:

\- tablas actuales;

\- columnas;

\- constraints;

\- índices;

\- conteos;

\- estados legacy;

\- catálogos;

\- asignaciones duplicadas;

\- expedientes sin estado;

\- expedientes sin responsable;

\- objetos SDRERC dentro de SYSTEM.

SCRIPT 01 - Crear esquema SDRERC_APP

Debe ejecutarse como SYSTEM o SYS.

Crear usuario SDRERC_APP.

Asignar permisos mínimos necesarios.

No otorgar DBA.

Usar USERS o proponer tablespace.

Considerar conexión a XE o XEPDB1.

SCRIPT 02 - Seguridad, usuarios, roles y equipos

Crear:

\- USUARIO

\- ROL

\- USUARIO_ROL

\- AREA

\- EQUIPO

\- EQUIPO_USUARIO

\- USUARIO_SUPERVISION

SCRIPT 03 - Entidades externas y canales

Crear:

\- ENTIDAD_EXTERNA

\- CANAL_RECEPCION

SCRIPT 04 - Catálogos críticos

Crear:

\- ETAPA_EXPEDIENTE

\- ESTADO_EXPEDIENTE

\- TIPO_MOVIMIENTO

\- TIPO_DOCUMENTO

\- TIPO_ACTA

\- PROCEDIMIENTO_REGISTRAL

\- TIPO_DOCUMENTO_ADJUNTO

\- TIPO_OBSERVACION

\- TIPO_RESOLUCION

\- TIPO_NOTIFICACION

\- TIPO_EVALUACION

\- TIPO_RESULTADO_EVALUACION

\- TIPO_RESULTADO_EJECUCION

\- TIPO_DERIVACION

\- TIPO_PUBLICACION

\- ESTADO_DOCUMENTO

\- ESTADO_NOTIFICACION

\- ESTADO_CARGO_ACUSE

\- MOTIVO_ARCHIVO

\- MOTIVO_RECHAZO

\- MOTIVO_CORRECCION

\- MOTIVO_NO_CORRESPONDE

SCRIPT 05 - Flujo, etapas, estados y transiciones

Crear:

\- FLUJO

\- FLUJO_TRANSICION

\- FLUJO_TRANSICION_ROL, si se considera MVP o Fase 2

\- FLUJO_TRANSICION_EQUIPO, si se considera MVP o Fase 2

SCRIPT 06 - Tablas transaccionales MVP

Crear:

\- EXPEDIENTE

\- EXPEDIENTE_SOLICITUD

\- EXPEDIENTE_ACTA

\- PERSONA

\- EXPEDIENTE_PERSONA

\- EXPEDIENTE_RELACION

\- EXPEDIENTE_DOCUMENTO

\- EXPEDIENTE_DOCUMENTO_ANALIZADO

\- EXPEDIENTE_ASIGNACION

\- EXPEDIENTE_HISTORIAL

\- EXPEDIENTE_EVALUACION

\- EXPEDIENTE_OBSERVACION

\- EXPEDIENTE_RESOLUCION

\- EXPEDIENTE_NOTIFICACION

\- EXPEDIENTE_CARGO_ACUSE

\- EXPEDIENTE_DIGITAL

\- AUDITORIA_EVENTO

SCRIPT 07 - Tablas Fase 2

Crear si corresponde:

\- EXPEDIENTE_DERIVACION_EXTERNA

\- EXPEDIENTE_PUBLICACION

\- EXPEDIENTE_PLAZO

\- EXPEDIENTE_ALERTA

\- PLAZO_CONFIGURACION

\- PERMISO

\- ROL_PERMISO

\- EXPEDIENTE_METRICA_ETAPA

SCRIPT 08 - Constraints e índices

Crear:

\- PK

\- FK

\- UK

\- CHECK

\- índices recomendados.

SCRIPT 09 - Datos maestros iniciales

Insertar:

\- roles base;

\- equipos base;

\- entidades externas base;

\- canales de recepción;

\- etapas;

\- estados;

\- tipos de movimiento;

\- procedimientos registrales;

\- tipos de documento;

\- tipos de acta;

\- tipos de resolución;

\- tipos de notificación;

\- tipos de evaluación;

\- tipos de derivación;

\- tipos de publicación;

\- estados de documento;

\- estados de notificación;

\- estados de cargo de acuse;

\- motivos;

\- transiciones iniciales.

Incluir equivalencia con estados legacy:

\- 56 Registro Expediente

\- 57 Expediente Asignado

\- 58 Expediente Recibido

\- 59 Expediente Atendido

\- 87 Expediente Verificado

\- 88 Ejecución Asignada

\- 89 Ejecución Trabajada

\- 90 Notificación Asignada

\- 91 Notificación Trabajada

SCRIPT 10 - Vistas para bandejas y consola

Crear:

\- VW_EXPEDIENTE_BANDEJA

\- VW_EXPEDIENTE_CONSOLA

\- VW_EXPEDIENTE_DETALLE

\- VW_EXPEDIENTE_TIMELINE

\- VW_EXPEDIENTE_HISTORIAL

\- VW_EXPEDIENTE_DOCUMENTOS

\- VW_EXPEDIENTE_DOCUMENTOS_ANALIZADOS

\- VW_EXPEDIENTE_PERSONAS

\- VW_EXPEDIENTE_EVALUACIONES

\- VW_EXPEDIENTE_RESOLUCIONES

\- VW_EXPEDIENTE_NOTIFICACIONES

\- VW_EXPEDIENTE_CARGOS_ACUSE

\- VW_EXPEDIENTE_PUBLICACIONES

\- VW_EXPEDIENTE_DERIVACIONES

\- VW_EXPEDIENTE_DIGITAL

\- VW_EXPEDIENTE_ALERTAS

\- VW_EXPEDIENTE_ACCIONES_PERMITIDAS

\- VW_EXPEDIENTE_RESPONSABLE_ACTUAL

SCRIPT 11 - Validaciones

Consultas para validar:

\- expedientes por estado;

\- expedientes por etapa;

\- expedientes por responsable;

\- expedientes por abogado inicial;

\- expedientes por equipo;

\- historial de un expediente;

\- asignaciones activas;

\- asignaciones duplicadas;

\- expedientes vencidos;

\- expedientes sin historial;

\- documentos por expediente;

\- documentos analizados por estado;

\- resoluciones sin número;

\- expedientes con notificaciones pendientes;

\- expedientes con cargos de acuse pendientes;

\- expedientes que requieren publicación;

\- expedientes con expediente digital incompleto;

\- conteos antes y después de migración.

SCRIPT 12 - Migración piloto opcional

Proponer scripts INSERT INTO \... SELECT \...

No ejecutar migración automática.

No borrar datos actuales.

Incluir TODO si faltan columnas reales.

============================================================

23\. ENTREGABLE INICIAL OBLIGATORIO

============================================================

Antes de generar scripts SQL completos, primero produce y valida:

A. Modelo lógico basado en el BPMN y en el Acta 013-2026-DRC.

B. Lista final de tablas recomendadas.

C. Separación entre MVP obligatorio y Fase 2.

D. Relaciones principales.

E. Transiciones principales derivadas del BPMN y del acta.

F. Reglas de historial.

G. Reglas de auditoría.

H. Diseño de bandejas y consola.

I. Manejo de usuarios internos versus actores externos.

J. Confirmación explícita de que no existirá una etapa visual llamada
VALIDACION.

K. Confirmación explícita de que análisis y ejecución mantendrán al
mismo abogado asignado inicialmente, salvo reasignación excepcional.

L. Modelo para duplicados y asociación a expediente principal.

M. Modelo para resultado En abandono y Observación administrativa con
derivación a notificación.

N. Modelo para notificación: 1 virtual + 2 presenciales + publicación.

O. Modelo para cargos de acuse.

P. Modelo para expediente digital, contemplando que puede no requerir
nueva asignación.

Q. Riesgos funcionales y técnicos.

R. Recomendación de qué tablas son obligatorias en la primera versión y
cuáles pueden quedar para segunda fase.

No generes todavía el DDL completo hasta que valide esta propuesta.

============================================================

24\. REGLAS ESTRICTAS

============================================================

\- No modifiques código Java todavía.

\- No ejecutes scripts automáticamente.

\- No borres tablas actuales.

\- No uses SYSTEM como dueño final de tablas.

\- No otorgues privilegio DBA al usuario SDRERC_APP.

\- No inventes columnas actuales si no están confirmadas.

\- Si algo no se puede confirmar desde el código, DDL, BPMN o acta,
márcalo como supuesto.

\- Si falta información del DDL real, genera consultas para que yo las
ejecute en DBeaver.

\- Toda tabla debe tener PK.

\- Toda relación importante debe tener FK.

\- Todo catálogo crítico debe tener CODIGO único y NOMBRE.

\- Toda tabla transaccional debe tener auditoría.

\- Toda transición de estado debe dejar historial.

\- Todo cambio de responsable debe dejar historial.

\- Toda asignación activa debe poder consultarse.

\- Todo expediente debe tener etapa actual, estado actual y fecha de
último movimiento.

\- Las bandejas solo deben corresponder a usuarios/equipos internos.

\- SDPRC, Ciudadano/Entidad y OGD no deben tener login, bandeja, rol ni
permisos.

\- Las entidades externas deben modelarse como ENTIDAD_EXTERNA, PERSONA,
CANAL_RECEPCION, remitente, solicitante u origen/destino documental.

\- No crear etapa visual llamada VALIDACION.

\- Las validaciones deben manejarse como evaluaciones, movimientos,
observaciones, resultados o reglas de transición dentro de cada etapa.

\- No crear nueva asignación obligatoria para ejecución si el abogado
asignado inicialmente continúa como responsable.

\- La importación de carga diaria debe incluir duplicados.

\- La asociación de duplicados/documentos debe gestionarse en
asignación.

\- En abandono y Observación administrativa deben poder derivarse
directamente a notificación.

\- El flujo estándar de notificación debe soportar 1 virtual + 2
presenciales + publicación.

\- El expediente digital debe soportar creación de carpeta, link y carga
documental por el abogado responsable, sin exigir asignación
independiente salvo decisión posterior.

\- Prioriza integridad, trazabilidad, mantenibilidad, compatibilidad
gradual y experiencia del usuario final.

\- El diseño debe estar preparado para un rediseño visual tipo consola
de expedientes, con encabezado del caso, barra de avance, responsable
actual, abogado inicial, eventos clave, documentos, documentos
analizados, evaluaciones, resolución, número de resolución, historial,
derivaciones, notificaciones, cargos de acuse, publicación, expediente
digital y alertas.

Empieza con el diagnóstico funcional-técnico y el modelo propuesto. No
generes todavía el DDL completo hasta que confirme el diseño lógico y
físico.
