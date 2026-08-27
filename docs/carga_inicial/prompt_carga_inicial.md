Implementa el módulo \"Carga Inicial\" (migración masiva histórica) en
SDRERC V3, para cargar de una sola vez el histórico de expedientes ya
resueltos (2026 y años anteriores vía OR) directamente en su estado
final real, con todo su ciclo de vida reconstruido (Registro →
Asignación → Análisis/Evaluación → Ejecución → Notificación), NO como si
fueran solicitudes nuevas que hay que revisar manualmente.

\## Contexto y diferencia con Carga Diaria

\"Carga Diaria\" (ya construida) es para solicitudes NUEVAS: detecta
posibles duplicados/grupo familiar y deja que un humano decida asociar o
no. \"Carga Inicial\" es un backfill histórico de casos que YA SE
RESOLVIERON en la vida real --- la meta es que el sistema quede con el
mismo historial que ya existe en Excel, sin someter cada fila a revisión
manual de duplicados/grupo familiar (son datos ya curados por el equipo
del cliente). Este es un módulo de un solo uso (o pocos usos puntuales),
no una pantalla operativa del día a día --- trátalo como una herramienta
de administración/migración, no como una bandeja más.

\## Fuentes de datos (rutas reales, ya confirmadas)

1\. \`docs/carga_inicial/DATA CONSOLIDADA - RECEPCIÓN Y ASIGNACIÓN
2026.xlsx\`

\- Hoja \"DATA DIARIA 2026\":
recepción+asignación+ejecución+notificación del año 2026. Columnas
confirmadas (fila de encabezado real, no necesitas volver a abrir el
archivo para esto): FECHA DE SOLICITUD, TIPO DE TRAMITE, CANAL DE
RECEPCION, DERIVADO POR, DNI SOLICITANTE, N° TRAMITE WEB, TIPO
DOCUMENTO, N° DOCUMENTO, TIPO DE SOLICITUD, TIPO DE ACTA, N° ACTA,
TITULAR, RESPONSABLE PREEVALUACION, ESTADO, ESTADO DE RECEPCION, FECHA
ASIGNADO, MES_ASIGNACION_EXPEDIENTE, N° DE DOCUMENTO - HOJA DE ENVIO, N°
EXPEDIENTE DIGITAL, TIPO CONTRATO, ASIGNADO A ABOGADO, N° RESOLUCION,
DATA (ver regla especial abajo), PRIORIDAD, FECHA DE EMISIÓN, MES
EMISION, PROCEDIMIENTO, RESOLUCIONES, TIPO DE ACTA (repetida), TIPO DE
CALIFICACION, ERROR MATERIAL, DESCRIPCION DE ERROR MATERIAL, N°
RESOLUCION PRIMIGENIA, ERROR MATERIAL 2° SALIDA, DESCRIPCION, ABOGADO
QUE EMITIÓ RESOLUCIÓN, SUPERVISOR, FECHA DE ASIGNACION, ASIGNADO A,
FECHA EJECUCION, ESTADO (ejecución), N° ACTA RECONSTITUIDA,
OBSERVACIONES, FECHA ASIGNACION NOTIF, ASIGNADO A (notif), MEDIO DE
NOTIFICACION, FECHA EMISION 1° CARTA, 1° NUMERO CARTA, FECHA ELABORACION
2° CARTA, 2° NUMERO CARTA, PLAZO ELABORACION DOC (x2), NUMERO OFICIO,
FECHA ELABORACION 2° OFICIO, 2° NUMERO OFICIO, MEMORANDO, OBSERVACIONES.

\- Hoja \"DATA PASIVO OR\": mismo tipo de datos pero de años anteriores,
backlog de Oficinas Registrales. Subconjunto más reducido de columnas:
FECHA DE SOLICITUD-OR, FECHA DE RECEPCION-SDRERC, TIPO DE TRAMITE, CANAL
DE RECEPCION, DERIVADO POR, TIPO DOCUMENTO, N° DOCUMENTO, TIPO DE ACTA,
N° ACTA, TITULAR, ESTADO DE RECEPCION, FECHA ASIGNADO, N° DE DOCUMENTO -
HOJA DE ENVIO, N° EXPEDIENTE DIGITAL, TIPO CONTRATO, ASIGNADO A ABOGADO,
N° RESOLUCION, DATA (ver regla especial abajo). NO tiene DNI, N° trámite
web, tipo de solicitud, ni las columnas de ejecución/notificación (esas
llegan del cruce).

\- Hoja \"Hoja1\": documentación de encabezados de las 2 hojas de arriba
--- NO es una fuente de datos a importar, solo referencia de estructura.
NO abrir \"DATA DIARIA 2026\" ni \"DATA PASIVO OR\" salvo para
procesarlas mediante el importador (ver regla de confidencialidad
abajo).

2\. \`docs/carga_inicial/DATA - EJECUCION Y NOTIFICACION 2026.xlsx\`

\- Hoja \"EJECUCIONES\": columnas PRIORIDAD, FECHA DE EMISIÓN, MES
EMISION, PROCEDIMIENTO, RESOLUCIONES (clave de cruce), TIPO DE ACTA,
TIPO DE CALIFICACION, ERROR MATERIAL, DESCRIPCION DE ERROR MATERIAL, N°
RESOLUCION PRIMIGENIA, ERROR MATERIAL 2° SALIDA, DESCRIPCION, ABOGADO
QUE EMITIÓ RESOLUCIÓN, SUPERVISOR, FECHA DE ASIGNACION, ASIGNADO A,
FECHA EJECUCION, ESTADO, N° ACTA RECONSTITUIDA, OBSERVACIONES, FECHA
ASIGNACION NOTIF, ASIGNADO A (notif), MEDIO DE NOTIFICACION, FECHA
EMISION 1° CARTA, 1° NUMERO CARTA, FECHA ELABORACION 2° CARTA, 2° NUMERO
CARTA, PLAZO ELABORACION DOC (x2), NUMERO OFICIO, FECHA ELABORACION 2°
OFICIO, 2° NUMERO OFICIO, MEMORANDO, OBSERVACIONES.

3\. \`docs/carga_inicial/ENCABEZADO DE DATA PARA APLICATIVO.xlsx\` ---
hoja \"DATA\": es el ESQUEMA OBJETIVO ya definido por el cliente, con
las columnas en el orden/agrupamiento en que deberían quedar mapeadas al
aplicativo, en 4 bloques: \"RECEPCIÓN Y ASIGNACIÓN\" (FECHA DE
SOLICITUD, N° EXPEDIENTE SGD, TIPO DE TRAMITE, CANAL DE RECEPCION,
DERIVADO POR, DNI SOLICITANTE, N° TRAMITE WEB, TIPO DOCUMENTO, N°
DOCUMENTO, TIPO DE PROCEDIMIENTO, TIPO DE ACTA, N° ACTA, TITULAR,
RESPONSABLE PREEVALUACION, ESTADO, ESTADO DE RECEPCION, FECHA ASIGNADO,
MES_ASIGNACION_EXPEDIENTE, N° DE DOCUMENTO - HOJA DE ENVIO, N°
EXPEDIENTE DIGITAL, TIPO CONTRATO(ABOGADO), ASIGNADO A ABOGADO), \"DATOS
PRINCIPALES PARA LA EJECUCIÓN\" (PRIORIDAD, FECHA DE EMISIÓN, N°
RESOLUCION, TIPO DE ACTA, TIPO DE CALIFICACION, ERROR MATERIAL),
\"EJECUCIÓN DE ANOTACIÓN TEXTUAL\" (FECHA EJECUCION, ESTADO, N° ACTA
RECONSTITUIDA), \"NOTIFICACIÓN DE CARTAS FINALES\" (TIPO DOCUMENTO
NOTIFICACIÓN, FECHA DE EMISIÓN DE DOCUMENTOS, N° CARTA, N° OFICIO, N°
MEMORANDO, FECHA NOTIFICACIÓN, MEDIO DE NOTIFICACIÓN, ACUSE, FECHA DE
ACUSE, ESTADO). Úsalo como guía de a qué campo del dominio V3 debe
mapear cada columna origen.

\## Los 2 cruces requeridos

\- \*\*Cruce 1\*\*: filas de \"DATA DIARIA 2026\" (clave \`N°
RESOLUCION\`) contra \"EJECUCIONES\" (clave \`RESOLUCIONES\`).

\- \*\*Cruce 2\*\*: filas de \"DATA PASIVO OR\" (clave \`N°
RESOLUCION\`) contra \"EJECUCIONES\" (clave \`RESOLUCIONES\`).

\- Normaliza la clave antes de comparar (trim, colapsar espacios
internos, mayúsculas) --- ya vi que \"RESOLUCIONES\" puede traer
espacios finales en los valores reales.

\- Una fila de \"DATA DIARIA 2026\"/\"DATA PASIVO OR\" sin match en
\"EJECUCIONES\" significa que el expediente nunca llegó a
Ejecución/Notificación --- debe importarse igual, solo que su
etapa/estado final queda en Asignación/Análisis según lo que sí tenga.
Una fila de \"EJECUCIONES\" sin match en ninguna de las 2 hojas de
origen es un caso huérfano --- repórtalo, no lo inventes.

\## Regla especial de canal de recepción (columna \"DATA\")

La columna \"DATA\" de cada hoja de origen NO es un dato real de
negocio, es una etiqueta constante puesta por quien armó el Excel:

\- En \"DATA DIARIA 2026\", el valor por defecto de \"DATA\" es
literalmente el texto \"DATA 2026 DIARIA\" --- IGNÓRALO. El canal de
recepción real de esa fila viene de la columna \"CANAL DE RECEPCION\" de
la misma hoja.

\- En \"DATA PASIVO OR\", el valor por defecto de \"DATA\" es
literalmente \"DATA PASIVO OR\" --- para TODA fila de esta hoja, el
canal de recepción real es siempre \`OR_PASIVO\` (\"OR Pasivo\"), sin
mirar ninguna otra columna. Ya verifiqué que este código YA EXISTE en el
catálogo \`CANAL_RECEPCION\` (no hace falta sembrarlo).

\## Regla de confidencialidad (obligatoria, no negociable)

Estos 2 archivos contienen datos reales de personas (nombres, DNI,
resoluciones) --- información sensible.

\- El código que construyas (parser/servicio de cruce/importador) debe
leer y procesar los archivos por completo en el backend (Apache POI, ya
es dependencia del proyecto --- mismo patrón que
\`CargaDiariaArchivoParserService\`), pero \*\*en ningún momento del
desarrollo debe el propio agente de IA leer, imprimir, loguear, ni pegar
en el chat/documentación/comentarios de código ningún valor real de
fila\*\* de \"DATA DIARIA 2026\", \"DATA PASIVO OR\" ni \"EJECUCIONES\".
Solo se puede trabajar con: nombres de columna (ya documentados en este
prompt), conteos agregados (filas totales, cruces exitosos, sin cruce,
duplicados, errores de validación) y, si hace falta depurar, IDs/números
de fila del Excel --- nunca el contenido real de una celda de dato
personal.

\- Cualquier prueba/verificación debe hacerse leyendo conteos (\`SELECT
COUNT(\*)\`, no \`SELECT \*\`) o ejecutando el propio importador contra
un archivo de prueba sintético con datos ficticios, nunca abriendo los
Excel reales fuera del propio pipeline de importación.

\- Los logs de la aplicación NO deben imprimir valores de columnas como
TITULAR, DNI SOLICITANTE, N° DOCUMENTO, N° RESOLUCION reales --- solo
IDs internos generados y conteos.

\## Diseño recomendado (como experto en cruces/cargas masivas --- para
confirmar conmigo antes de programar)

1\. \*\*Módulo nuevo bajo Administración\*\*
(\`MENU_ADMIN_CARGA_INICIAL\`, backend+frontend nuevos, no reutilizar
las pantallas de Carga Diaria): un asistente de 3 pasos ---

\- Paso 1 \"Analizar\": sube los 2 archivos, el backend hace ambos
cruces y devuelve un RESUMEN agregado (conteos por hoja, cruces
exitosos/fallidos, duplicados detectados por acta+titular/N° resolución,
filas con datos incompletos) --- nunca la grilla de filas con datos
reales.

\- Paso 2 \"Confirmar\": el usuario ve el resumen + una lista de solo
los PROBLEMAS a resolver (huérfanos, duplicados, datos incompletos)
identificados por número de fila de Excel, sin exponer el resto de
columnas sensibles innecesariamente en pantalla si se puede evitar --- a
definir cuánto detalle es aceptable mostrar en la UI (probablemente sí
hace falta mostrar el titular para que el usuario pueda decidir sobre un
duplicado; usa tu criterio y pregúntame si no es obvio).

\- Paso 3 \"Importar\": botón único, importación asíncrona (job en
background con endpoint de progreso --- dado el volumen de los archivos,
no bloquear un solo request HTTP), commit por lotes.

2\. \*\*Resolución automática de duplicados/grupo familiar\*\* (a
diferencia de Carga Diaria): dado que es data histórica ya curada,
decide con la MISMA lógica de \"expediente principal\" ya usada en
\`ExpedienteRelacionadoDAO\` (gana el que ya tiene número; si no, el más
antiguo) pero aplicándola automáticamente sin pausar por revisión manual
--- documenta esta decisión de diseño explícitamente y qué se hace si
hay ambigüedad real (¿se aborta esa fila y se reporta, o se aplica una
regla de desempate?). Pregúntame si tienes dudas sobre el criterio antes
de implementar.

3\. \*\*Reconstrucción de etapa/estado final por fila\*\*, según qué
columnas de la fila cruzada están pobladas (mismo patrón \"columna vacía
= paso no alcanzado\" ya usado en toda la app): si no hay datos de
ejecución, el expediente queda en Asignación/Análisis; si hay ejecución
pero no notificación, queda en Ejecución; si hay notificación completa,
queda en Notificación/Cerrado según corresponda. Investiga primero los
códigos reales de
\`ETAPA_EXPEDIENTE\`/\`ESTADO_EXPEDIENTE\`/\`FLUJO_TRANSICION\` ya
usados por los módulos operativos
(Asignación/Análisis/Ejecución/Notificación, todos ya construidos en V3
esta sesión) y reutilízalos --- no inventes estados nuevos sin
confirmarlo conmigo.

4\. \*\*Trazabilidad completa\*\*: por cada expediente importado,
inserta las filas de \`EXPEDIENTE_HISTORIAL\` que reconstruyan su
historia (Registro → Asignación → recepción por abogado → resultado de
análisis/evaluación → ejecución → notificación), con fechas reales
tomadas del Excel cuando existan (no \`SYSDATE\`) --- así la \"Bandeja
de Expedientes / Trazabilidad\" que ya construimos esta sesión
(\`/bandeja\`, subpestaña \"Trazabilidad\") muestra una línea de tiempo
coherente para estos expedientes migrados, indistinguible de uno que
pasó por el flujo operativo real.

5\. \*\*Tabla de auditoría propia de la migración\*\* (nueva, requiere
script SQL --- no ejecutar sin autorización): algo como
\`CARGA_INICIAL_LOG\` (archivo origen, hoja, fila de Excel,
\`id_expediente\` resultante, estado de la fila:
IMPORTADO/DUPLICADO/ERROR/HUERFANO, mensaje) para poder
auditar/reprocesar sin tener que volver a abrir los Excel originales.

6\. Documentos generados (resolución, cartas, oficios, memorandos) deben
insertarse en \`EXPEDIENTE_DOCUMENTO_ANALIZADO\` con los números/fechas
reales del Excel, replicando la jerarquía padre/hijo ya usada en
Análisis/Ejecución/Notificación donde aplique.

\## Antes de escribir código

1\. Lee \`CLAUDE.md\` (raíz) y \`v3/CLAUDE.md\` completos --- ya
documentan el modelo de datos, catálogos, transiciones y el patrón de
Carga Diaria que debes usar como referencia de pipeline (parsear →
validar/cruzar → previsualizar resumen → confirmar → importar), sin
reutilizar sus pantallas.

2\. Investiga (lectura de código, no de datos) los DAO/Service reales ya
construidos para Asignación/Análisis/Ejecución/Notificación en
\`v3/sdrerc-v3-backend\` para reutilizar sus catálogos, transiciones de
\`FLUJO_TRANSICION\` y criterios de estado --- no dupliques lógica ya
existente.

3\. Antes de programar el importador en sí, \*\*preséntame un diseño
concreto\*\* (tablas exactas a escribir, algoritmo de
cruce/normalización, política de duplicados/ambigüedad, arquitectura del
job asíncrono, forma del script SQL de auditoría) para que lo confirme
--- es una escritura masiva contra la misma BD compartida con V2, no
partas a codear sin luz verde en el diseño.

4\. No ejecutes ningún script SQL (crear tabla de auditoría, sembrar
catálogos si hiciera falta) sin autorización explícita separada.

5\. No proceses ni imprimas datos reales de las hojas restringidas en
ningún momento --- solo conteos agregados.
