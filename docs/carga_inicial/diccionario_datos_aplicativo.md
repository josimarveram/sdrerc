# Diccionario de datos — dónde se ve cada campo del Excel en el aplicativo V3

Para cada columna del Excel de Carga Inicial: qué es y en qué pantalla/campo del aplicativo V3 aparece ese dato una vez migrado. Los campos marcados "No se muestra" no tienen una pantalla propia (quedan como nota interna o simplemente no se usan).

---

## Hoja "DATA DIARIA UNIFICADA"

| Columna Excel | Descripción breve | Dónde se ve en V3 |
|---|---|---|
| FECHA DE SOLICITUD | Fecha en que se recibió la solicitud | Bandeja Registro → columna "Fecha Solicitud"; Panel de datos → "Datos del plazo/expediente" |
| TIPO DE TRAMITE | Tipo de trámite original del Excel | No se muestra (queda como nota interna de trazabilidad) |
| CANAL DE RECEPCION | Canal por el que ingresó la solicitud | Bandeja Registro → Panel de datos → "Canal" |
| DERIVADO POR | Quién derivó la solicitud | No se muestra (nota interna de trazabilidad) |
| DNI SOLICITANTE | DNI del solicitante | Panel de datos → "Datos del solicitante" → N° de documento |
| N° TRAMITE WEB | N° de trámite documentario web | Panel de datos → "Datos de solicitud" → "N° trámite web" |
| TIPO DOCUMENTO | Tipo de documento con el que llegó la solicitud | Panel de datos → "Datos de solicitud" |
| N° DOCUMENTO | N° de ese documento de ingreso | Panel de datos → "Datos de solicitud" |
| TIPO DE SOLICITUD | Procedimiento/tipo de solicitud | Panel de datos → "Datos de solicitud" → "Procedimiento" |
| TIPO DE ACTA | Tipo de acta registral | Bandeja Registro → columna "Tipo Acta" (oculta por defecto); Panel de datos → "Datos del acta" |
| N° ACTA | N° del acta | Bandeja Registro → columna "Nro Acta"; Panel de datos → "Datos del acta" |
| TITULAR | Nombre del titular del acta | Bandeja Registro → columna "Titular"; Panel de datos (nombre debajo del título) |
| RESPONSABLE PREEVALUACION | Quién preevaluó la solicitud en el sistema legado | No se muestra (nota interna de trazabilidad) |
| ESTADO | Estado en el sistema legado | No se usa |
| ESTADO DE RECEPCION | Estado de recepción legado | No se usa |
| FECHA ASIGNADO | Fecha en que se asignó al abogado | Bandeja Asignación → columna "Fecha Asignación" |
| MES_ASIGNACION_EXPEDIENTE | Mes de asignación (auxiliar del Excel) | No se usa |
| N° DE DOCUMENTO HOJA DE ENVIO | N° de hoja de envío de la asignación | Bandeja Asignación → panel "Asignación" → "Hoja de envío" |
| N° EXPEDIENTE DIGITAL | Marca si esta fila es duplicado de otro expediente ya numerado | Define el N° de expediente que se ve en toda la app (columna "Nro. Expediente" de cualquier bandeja) |
| TIPO CONTRATO | Tipo de contrato del abogado (RR.HH.) | No se usa |
| ASIGNADO A ABOGADO: | Abogado responsable del expediente | Bandeja Asignación/Análisis → columna "Abogado"/"Abogado designado" |
| N° RESOLUCION | N° de la resolución emitida | Bandeja Análisis/Ejecución → grilla "Documentos analizados" → N° Documento (tipo Resolución) |
| RESOLUCION_LIMPIA | Versión depurada del N° de resolución | Igual que "N° RESOLUCION" (se prefiere esta si ambas tienen dato) |
| DATA | Columna auxiliar del archivo | No se usa |
| EJECUCIONES.RESOLUCIONES | Redundante con N° Resolución | No se usa |
| EJECUCIONES.PRIORIDAD | Prioridad del expediente en Ejecución | No se usa |
| EJECUCIONES.FECHA DE EMISIÓN | Fecha de emisión de la resolución | Bandeja Análisis → grilla "Documentos analizados" → "Fecha Emisión" |
| EJECUCIONES.MES EMISION | Mes de emisión (auxiliar) | No se usa |
| EJECUCIONES.PROCEDIMIENTO | Tipo de procedimiento resuelto (Rectificación/Reconstitución/Cancelación) | Define el tipo de documento "Resolución de..." que se ve en Bandeja Análisis/Ejecución |
| EJECUCIONES.TIPO DE ACTA | Tipo de acta (redundante) | No se usa |
| EJECUCIONES.TIPO DE CALIFICACION | Resultado del análisis (Procedente/Improcedente/etc.) | Bandeja Análisis → "Resultado del análisis" → campo "Resultado" |
| EJECUCIONES.ERROR MATERIAL | Si hubo error material | No se usa |
| EJECUCIONES.DESCRIPCION DE ERROR MATERIAL | Detalle del error material | No se usa |
| EJECUCIONES.N° RESOLUCION PRIMIGENIA | N° de resolución original (si hubo corrección) | No se usa |
| EJECUCIONES.ERROR MATERIAL 2° SALIDA | 2° error material | No se usa |
| EJECUCIONES.DESCRIPCION | Descripción general | No se usa |
| EJECUCIONES.ABOGADO QUE EMITIÓ RESOLUCIÓN | Abogado que firmó la resolución | No se usa (se usa "ASIGNADO A ABOGADO" como único responsable) |
| EJECUCIONES.SUPERVISOR | Supervisor del expediente | No se usa |
| EJECUCIONES.FECHA DE ASIGNACION | Fecha de asignación (redundante) | No se usa |
| EJECUCIONES.ASIGNADO A: | Abogado (redundante) | No se usa |
| EJECUCIONES.FECHA EJECUCION | Fecha en que se ejecutó la resolución | Bandeja Ejecución → "Resultado de ejecución" → "Fecha Ejecución" |
| EJECUCIONES.ESTADO | Estado de la ejecución (Ejecutado/Pendiente/Observado/etc.) | Bandeja Ejecución → columna "Estado" |
| EJECUCIONES.N° ACTA RECONSTITUIDA | N° de acta tras reconstitución | No se usa |
| EJECUCIONES.OBSERVACIONES | Observaciones de ejecución | No se usa |
| EJECUCIONES.FECHA ASIGNACION NOTIF | Fecha de asignación al notificador | No se usa |
| EJECUCIONES.ASIGNADO A:_1 | Notificador responsable | Bandeja Notificación → "Bandeja Asignación" → columna "Responsable"/notificador |
| EJECUCIONES.MEDIO DE NOTIFICACION | Medio de notificación (virtual/presencial) | No se usa (el medio se define automáticamente: intento 1 virtual, intento 2 presencial) |
| EJECUCIONES.FECHA EMISION 1° CARTA | Fecha de envío de la 1ra carta | Bandeja Notificación → grilla de intentos → "Intento 1" → Fecha Envío |
| EJECUCIONES.1° NUMERO CARTA | N°/código de la 1ra carta | Bandeja Notificación → grilla de intentos → "Intento 1" → Código |
| EJECUCIONES.FECHA ELABORACION 2° CARTA | Fecha de la 2da carta (si hubo) | Bandeja Notificación → grilla de intentos → "Intento 2" → Fecha Envío |
| EJECUCIONES.2° NUMERO CARTA | N°/código de la 2da carta | Bandeja Notificación → grilla de intentos → "Intento 2" → Código |
| EJECUCIONES.PLAZO DE ELABORACION DE DOCUMENTACION 2 DIAS HABILES | Plazo de elaboración | No se usa |
| EJECUCIONES.NUMERO OFICIO | N° de oficio | No se usa |
| EJECUCIONES.FECHA ELABORACION 2° OFICIO | Fecha del 2° oficio | No se usa |
| EJECUCIONES.2° NUMERO OFICIO | N° del 2° oficio | No se usa |
| EJECUCIONES.PLAZO DE ELABORACION DE DOCUMENTACION 2 DIAS HABILES2 | Plazo (repetido) | No se usa |
| EJECUCIONES.MEMORANDO | N° de memorando | No se usa |
| EJECUCIONES.OBSERVACIONES. | Observaciones finales | No se usa |
| ESTADO CRUCE | Si la fila cruzó correctamente contra Ejecuciones | No se usa (solo verificación externa antes de migrar) |

---

## Hoja "DATA PASIVO OR UNIFICADA"

Mismos campos que la hoja Diaria, con estas diferencias puntuales:

| Columna Excel | Descripción breve | Dónde se ve en V3 |
|---|---|---|
| FECHA DE SOLICITUD- OR | Fecha de solicitud original en la Oficina Registral | No se usa (se usa "FECHA DE RECEPCION-SDRERC" en su lugar) |
| FECHA DE RECEPCION-SDRERC | Fecha en que SDRERC recibió el caso | Bandeja Registro → columna "Fecha Solicitud" |
| N° DE EXPEDIENTE | N° de expediente ya asignado en el sistema legado | Se usa directamente como el N° de expediente visible en toda la app (a diferencia de Diaria, que genera uno nuevo) |
| N° DE FOLIOS | Cantidad de folios del expediente físico | No se usa |
| ESTADO | Estado en el sistema legado | No se usa |
| EJECUCIONES.RESOLUCION_LIMPIA | Duplicado de la columna "RESOLUCION_LIMPIA" | No se usa |

El resto de columnas (TIPO DE TRAMITE, DERIVADO POR, TIPO DOCUMENTO, N° DOCUMENTO, TIPO DE ACTA, N° ACTA, TITULAR, FECHA ASIGNADO, N° DE DOCUMENTO - HOJA DE ENVIO, N° EXPEDIENTE DIGITAL, TIPO CONTRATO, ASIGNADO A ABOGADO, N° RESOLUCION, y todo el bloque EJECUCIONES.\*) se comportan igual que en la hoja Diaria — ver tabla de arriba.

**Diferencia adicional**: esta hoja no tiene columna "DNI SOLICITANTE" — el titular se registra sin N° de documento en "Datos del solicitante".
