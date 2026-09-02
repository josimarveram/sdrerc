---
title: "Informe: Etapas y Estados del flujo SDRERC V3"
subtitle: "Catálogo completo vs. uso real — de Recepción a Publicación"
date: "31/08/2026"
---

# Informe: Etapas y Estados del flujo SDRERC V3

**Fecha:** 31/08/2026
**Objetivo:** Dar una visión completa y verificada del catálogo de etapas/estados definido en base de datos, contrastado contra el uso real hoy en producción, para apoyar decisiones sobre qué mantener, retirar o formalizar.

**Metodología:** Consultas de solo lectura (`SELECT`) contra el esquema Oracle `SDRERC_APP`, más revisión (`grep`) del código fuente real de V3 para confirmar qué transiciones se invocan efectivamente desde la aplicación. No se ejecutó ninguna sentencia de escritura.

---

## 1. Catálogo completo: 10 etapas, 39 estados

| Etapa (orden macro) | Estados definidos en catálogo |
|---|---|
| **REGISTRO** | IMPORTADO, POTENCIAL_DUPLICADO, REGISTRADO |
| **ASIGNACION** | ASIGNADO |
| **ANALISIS** | RECIBIDO_POR_ABOGADO, EN_ANALISIS, OBSERVADO, SUBSANADO, DERIVADO, ATENDIDO, NO_CORRESPONDE, OBSERVACION_ADMINISTRATIVA, EN_ABANDONO |
| **VERIFICACION** | EN_VERIFICACION, VERIFICADO, REQUIERE_CORRECCION, DOCUMENTO_INCONSISTENTE |
| **FIRMA_EMISION** | PARA_FIRMA, FIRMADO, EMITIDO, RESOLUCION_NUMERADA |
| **EJECUCION** | EN_EJECUCION, EJECUTADO, INDAGATORIO |
| **NOTIFICACION** | POR_ASIGNAR, POR_VALIDAR, EN_NOTIFICACION, CARGO_PENDIENTE, CARGO_RECIBIDO, NOTIFICADO, REQUIERE_PUBLICACION |
| **PUBLICACION_CONDICIONAL** | PENDIENTE_PUBLICACION, PUBLICACION_REGISTRADA |
| **EXPEDIENTE_DIGITAL** | CARPETA_CREADA, LINK_REGISTRADO, EXPEDIENTE_DIGITAL_COMPLETO |
| **CIERRE_ARCHIVO** | CERRADO, ARCHIVADO, DERIVACION_EXTERNA_PENDIENTE |

---

## 2. Uso real hoy (expedientes activos por etapa/estado)

| Etapa | Estado | Expedientes activos |
|---|---|---|
| REGISTRO | REGISTRADO | 192 |
| ASIGNACION | ASIGNADO | 9 |
| ANALISIS | RECIBIDO_POR_ABOGADO | 5 |
| ANALISIS | DERIVADO | 1 |
| ANALISIS | NO_CORRESPONDE | 3 |
| ANALISIS | EN_ABANDONO | 6 |
| VERIFICACION | EN_VERIFICACION | 3 |
| EJECUCION | EN_EJECUCION | 51 |
| EJECUCION | EJECUTADO | 36 |
| EJECUCION | REQUIERE_CORRECCION ⚠️ | 5 (ver hallazgo 4) |
| NOTIFICACION | POR_ASIGNAR | 4 |
| NOTIFICACION | POR_VALIDAR | 1 |
| NOTIFICACION | EN_NOTIFICACION | 1612 |

Cualquier estado del catálogo completo (sección 1) que no aparece en esta tabla tiene **0 expedientes activos hoy** — incluidos, entre otros: `ANALISIS/EN_ANALISIS`, `OBSERVADO`, `SUBSANADO`, `ATENDIDO`, `OBSERVACION_ADMINISTRATIVA`; `VERIFICACION/VERIFICADO`, `REQUIERE_CORRECCION` (el propio de Verificación), `DOCUMENTO_INCONSISTENTE`; y las 4 etapas completas descritas en la sección 3.

---

## 3. Etapas 100% sin uso real hoy (0 expedientes en cualquiera de sus estados)

- **FIRMA_EMISION** — ya documentado en el propio código (`VerificacionExpedienteDAO`/`VerificacionExpedienteDTO`): "0 filas reales, verificado". Consistente con la regla de negocio vigente ("Firma/Emisión integrada dentro de Verificación, sin módulo lateral"). El catálogo `flujo_transicion` sí tiene una transición **activa** hacia ahí (`ENVIO_FIRMA: VERIFICACION/VERIFICADO → FIRMA_EMISION/PARA_FIRMA`), pero **ningún código de V3 la invoca**: un camino "vivo" en el catálogo pero muerto en la práctica.
- **PUBLICACION_CONDICIONAL** — cero referencias en todo el backend V3. Ver hallazgo 4: quedó reemplazada de facto por otro mecanismo.
- **EXPEDIENTE_DIGITAL** (la etapa, no el módulo "Expediente digital" que sí existe como consulta de solo lectura) — cero referencias; ningún código invoca `CREACION_CARPETA_EXPEDIENTE_DIGITAL`/`CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL`.
- **CIERRE_ARCHIVO** — la única referencia en todo V3 es `DashboardDAO`, que lee `EXPEDIENTE_HISTORIAL` con `tipo_movimiento='CIERRE'` para el KPI "Cerrados en el periodo" (una lectura histórica, no una transición real). Ningún DAO escribe la transición `CIERRE`/`ARCHIVO`/`DERIVACION_EXTERNA` — el expediente nunca llega aquí en la práctica.

---

## 4. Hallazgo real: "Publicación" tiene 2 mecanismos paralelos, nunca reconciliados

- **El mecanismo viejo**: etapa `PUBLICACION_CONDICIONAL` (`PENDIENTE_PUBLICACION` → `PUBLICACION_REGISTRADA`), con transiciones activas en el catálogo (`GENERACION_PUBLICACION`, `REGISTRO_PUBLICACION`, `CIERRE`) pero sin ningún código que las use — 100% muerto.
- **El mecanismo real, el que sí se usa**: la "Bandeja de publicación" (4.ª pestaña de Notificación) **nunca mueve el expediente a esta etapa** — el expediente permanece en `NOTIFICACION/EN_NOTIFICACION` todo el tiempo, y la "publicación" se registra como un 3.er intento dentro de `EXPEDIENTE_NOTIFICACION` (`tipo_notificacion='PUBLICACION'`), un mecanismo completamente distinto a nivel de tabla, sin relación con la etapa/estado del expediente.

**Es una decisión de diseño real que conviene formalizar**: definir si `PUBLICACION_CONDICIONAL` se sigue sembrando en el catálogo sin usarla nunca, o si se retira/desactiva para no confundir a futuro.

---

## 5. Hallazgo real: EJECUCION reutiliza estados que "pertenecen" a VERIFICACION

Los 5 expedientes en `EJECUCION/REQUIERE_CORRECCION` (tabla de la sección 2) usan, por ID, la **misma fila** de `estado_expediente` cuyo dueño real (`id_etapa`) es `VERIFICACION` — no existe una fila propia `REQUIERE_CORRECCION` bajo `EJECUCION` en el catálogo. Confirmado con `SELECT` cruzado: `id_etapa_actual=EJECUCION` pero `id_estado_actual` apunta a la fila que pertenece a `VERIFICACION`.

Mismo patrón para `EJECUCION/DOCUMENTO_INCONSISTENTE` (la transición existe activa en `flujo_transicion`, `REVERSION_ESTADO_DOCUMENTO_EJECUCION`, aunque hoy tiene 0 expedientes reales usándola).

Es decir: `flujo_transicion` fue sembrado a propósito para reutilizar por ID las filas de `REQUIERE_CORRECCION`/`DOCUMENTO_INCONSISTENTE` de Verificación en vez de crear filas dedicadas para Ejecución. Funciona (Oracle no lo impide, no hay `CHECK` que lo prohíba), pero rompe la expectativa de "1 estado = 1 etapa dueña" si alguien lee `estado_expediente` esperando ese mapeo limpio.

**Vale la pena decidir** si se acepta como atajo intencional o se crean 2 filas propias para Ejecución.

---

## 6. Transiciones definidas pero inactivas (`activo=0`) en el catálogo

- `REGISTRO_RESULTADO_ANALISIS`: `ANALISIS/ATENDIDO → VERIFICACION/VERIFICADO` (activo=0)
- `REVISION_SUPERVISOR`: `VERIFICACION/VERIFICADO → FIRMA_EMISION/FIRMADO` (activo=0)

Ambas parecen residuos de un diseño anterior, ya reemplazadas por otras transiciones activas equivalentes (`ENVIO_VERIFICACION`, `ENVIO_FIRMA`). No bloquean nada hoy, pero son candidatas a limpieza de catálogo si se decide.

---

## 7. Resumen para decisión

De las **10 etapas** del catálogo, solo **6 tienen expedientes reales hoy**: Registro, Asignación, Análisis, Verificación, Ejecución y Notificación (incluida su "Bandeja de publicación" interna). El flujo real termina, de facto, en Notificación.

Las **4 etapas restantes** — Firma/Emisión, Publicación Condicional, Expediente Digital, Cierre/Archivo — existen en el catálogo, y algunas hasta tienen transiciones "activas" en `flujo_transicion`, pero **ningún código las invoca**. Son placeholders fieles a reglas de negocio ya documentadas ("no implementar cierre automático", "Firma/Emisión integrada en Verificación") más que un flujo vivo.

### Puntos que requieren una decisión explícita

1. **Publicación**: ¿formalizar `PUBLICACION_CONDICIONAL` como muerta (documentarla/retirarla) o migrar el mecanismo real (intento tipo `PUBLICACION` sobre `EXPEDIENTE_NOTIFICACION`) hacia esa etapa?
2. **Ejecución vs. Verificación**: ¿mantener el atajo de reutilizar por ID los estados `REQUIERE_CORRECCION`/`DOCUMENTO_INCONSISTENTE` de Verificación, o crear filas propias para Ejecución?
3. **Firma/Emisión, Expediente Digital, Cierre/Archivo**: ¿conservar como etapas "dormidas" del catálogo por si se activan más adelante, o limpiar del todo (transiciones inactivas incluidas)?
4. **Transiciones inactivas** (`REGISTRO_RESULTADO_ANALISIS`/`REVISION_SUPERVISOR`): ¿limpiar del catálogo o dejarlas como registro histórico?

---

## 8. Qué etapa/estado filtra cada bandeja (nivel aplicativo, no solo BD)

Cada módulo/bandeja del aplicativo V3 aplica su propio filtro de etapa/estado en el código (SQL del DAO). Esta tabla conecta ese filtro con el catálogo de las secciones 1-2 — es la vista "aplicativo" que complementa la vista "base de datos" de arriba.

| Módulo — Bandeja | Filtro real aplicado en código |
|---|---|
| Registro — Bandeja Registro | `etapa = REGISTRO` (estado típico `REGISTRADO`). Vista `vw_expediente_bandeja`. |
| Asignación — Bandeja Asignación | **Sin filtro de etapa** (bandeja de coordinación): lista expedientes en cualquier etapa/estado, incluidos los ya avanzados a Análisis/Verificación/Ejecución/Notificación. Vista `vw_expediente_asignacion_detalle`. |
| Asignación — Cartas de respuesta | A nivel de documento: `estado_final_notificacion_codigo = ATENDIDO`. Excluye expedientes que ya volvieron a `etapa = ANALISIS`. |
| Asignación — Carga Abogados | Agrega carga del abogado sobre `ANALISIS` + `VERIFICACION` + `EJECUCION` (4 subcolumnas propias solo de Análisis: Por recibir/En análisis/Observado/Derivado). |
| Análisis — Bandeja Análisis | `(etapa = ASIGNACION AND estado = ASIGNADO) OR etapa = ANALISIS`. Vista `vw_expediente_analisis_detalle`. |
| Verificación — Bandeja Verificación | `etapa = VERIFICACION` (SELECT inline, sin vista propia). |
| Ejecución — Bandeja Ejecución | `etapa = EJECUCION`, sin filtrar por resultado de análisis. |
| Notificación — Bandeja Asignación | A nivel de documento: `estado_expediente = POR_ASIGNAR`, `clasificacion IN (FINAL, INTERMEDIO)`, `estado_documento IN (EN_DESPACHO, VALIDADO, EMITIDO, OBSERVADO)`. |
| Notificación — Bandeja Validación | A nivel de documento: `estado_expediente = POR_VALIDAR AND clasificacion = FINAL AND estado_documento = EN_DESPACHO`. Filtra además por visibilidad de fila (asignación individual/equipo). |
| Notificación — Bandeja Notificación | A nivel de documento: `estado_expediente = EN_NOTIFICACION AND estado_documento = EMITIDO`, excluyendo documentos que ya agotaron sus 2 intentos directos (`agoto_intentos_directos = 0`). |
| Notificación — Bandeja de publicación | Mismo universo que la anterior, pero `agoto_intentos_directos = 1` (documentos que sí agotaron sus 2 intentos directos). |
| Bandeja de Expedientes (Expediente 360°) | Combo "Etapa" real y visible: el usuario elige cualquiera de las 10 etapas del catálogo; sin restricción fija. |

**Lectura clave de esta tabla**: ningún módulo hoy filtra por `FIRMA_EMISION`, `PUBLICACION_CONDICIONAL`, `EXPEDIENTE_DIGITAL` ni `CIERRE_ARCHIVO` — coincide exactamente con que esas 4 etapas tienen 0 uso real (sección 3). Las bandejas de Notificación (Asignación/Validación/Notificación/Publicación) no filtran por `etapa_expediente` de forma directa como las demás — filtran por una combinación de `estado_expediente` + estado del **documento** (`EXPEDIENTE_DOCUMENTO_ANALIZADO`), porque a diferencia de Registro/Asignación/Análisis/Verificación/Ejecución (donde la unidad de trabajo es el expediente), en Notificación la unidad de trabajo real es el documento dentro del expediente.
