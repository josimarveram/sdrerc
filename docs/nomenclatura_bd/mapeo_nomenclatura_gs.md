# Mapeo de nomenclatura — Estándar OTI (código de sistema **GS** — Sistema de Gestión de Expedientes)

**ESTADO: EJECUTADO CONTRA LA BASE REAL (26/08/2026)** — autorizado explícitamente por el usuario. Las 72 tablas, 871 columnas (con las excepciones ya documentadas) y los constraints con nombre propio (PK/UK/FK/CHECK reales) del esquema `SDRERC_APP` **ya tienen su nombre `GS...` real**. Se creó además una **vista de compatibilidad por cada tabla, con el nombre viejo**, para que V2 y V3 sigan funcionando sin ningún cambio de código mientras se actualiza el código en una etapa posterior sin presión de tiempo. Detalle completo de la ejecución, 2 bugs reales encontrados/corregidos en el proceso, y verificación final en `v3/CLAUDE.md`, sección "Estandarización de nomenclatura BD — ejecución real (26/08/2026)".

Inventario real del esquema `SDRERC_APP` (consultado en vivo contra Oracle, solo lectura, 26/08/2026) y su equivalencia bajo el estándar `MT-BD-E-EOBDO-01.0/GTI/SGGBD`. **Nada de esto se ha ejecutado contra la base** — es un documento de mapeo, no un script.

**Totales reales**: 72 tablas · 26 vistas · 871 columnas · 215 índices · 638 constraints (98 FK / 435 CHECK / 70 PK / 35 UK) · 2 funciones · 0 procedimientos/paquetes/triggers/clases Java.

**Regla de nombre aplicada**: `GST<TipoContenido>_<NombreActual>` para tablas, `GSV<TipoContenido>_<NombreActual sin "VW_">` para vistas — **se conserva el nombre actual completo, sin abreviar** (decisión confirmada).

---

## 1. Tablas — Maestra (M) — 41 tablas, catálogos/parametría

| Nombre actual | Nombre nuevo |
|---|---|
| AREA | GSTM_AREA |
| CANAL_RECEPCION | GSTM_CANAL_RECEPCION |
| CENTROS_ATENCION | GSTM_CENTROS_ATENCION |
| ENTIDAD_EXTERNA | GSTM_ENTIDAD_EXTERNA |
| EQUIPO | GSTM_EQUIPO |
| ESTADO_CARGO_ACUSE | GSTM_ESTADO_CARGO_ACUSE |
| ESTADO_DOCUMENTO | GSTM_ESTADO_DOCUMENTO |
| ESTADO_EXPEDIENTE | GSTM_ESTADO_EXPEDIENTE |
| ESTADO_NOTIFICACION | GSTM_ESTADO_NOTIFICACION |
| ETAPA_EXPEDIENTE | GSTM_ETAPA_EXPEDIENTE |
| FERIADO_NACIONAL | GSTM_FERIADO_NACIONAL |
| FLUJO | GSTM_FLUJO |
| FLUJO_TRANSICION | GSTM_FLUJO_TRANSICION |
| MOTIVO_ARCHIVO | GSTM_MOTIVO_ARCHIVO |
| MOTIVO_CORRECCION | GSTM_MOTIVO_CORRECCION |
| MOTIVO_NO_CORRESPONDE | GSTM_MOTIVO_NO_CORRESPONDE |
| MUNICIPALIDADES | GSTM_MUNICIPALIDADES |
| PERMISO | GSTM_PERMISO |
| PERSONA | GSTM_PERSONA |
| PLAZO_CONFIGURACION | GSTM_PLAZO_CONFIGURACION |
| PROCEDIMIENTO_REGISTRAL | GSTM_PROCEDIMIENTO_REGISTRAL |
| ROL | GSTM_ROL |
| TIPO_ACTA | GSTM_TIPO_ACTA |
| TIPO_DOCUMENTO | GSTM_TIPO_DOCUMENTO |
| TIPO_DOCUMENTO_ADJUNTO | GSTM_TIPO_DOCUMENTO_ADJUNTO |
| TIPO_MOVIMIENTO | GSTM_TIPO_MOVIMIENTO |
| TIPO_NOTIFICACION | GSTM_TIPO_NOTIFICACION |
| TIPO_OBSERVACION | GSTM_TIPO_OBSERVACION |
| TIPO_RESOLUCION | GSTM_TIPO_RESOLUCION |
| TIPO_RESULTADO_EJECUCION | GSTM_TIPO_RESULTADO_EJECUCION |
| TIPO_RESULTADO_EVALUACION | GSTM_TIPO_RESULTADO_EVALUACION |
| UBIGEO_DEPARTAMENTO | GSTM_UBIGEO_DEPARTAMENTO |
| UBIGEO_DISTRITO | GSTM_UBIGEO_DISTRITO |
| UBIGEO_PROVINCIA | GSTM_UBIGEO_PROVINCIA |
| UNIDADES_ORGANICAS | GSTM_UNIDADES_ORGANICAS |
| USUARIO | GSTM_USUARIO |

*(TIPO_RESULTADO_VALIDACION no aparece como tabla propia — está documentado en `CLAUDE.md` como catálogo huérfano/nunca sembrado en BD; no requiere mapeo.)*

## 2. Tablas — Cabecera (C) — 1 tabla

| Nombre actual | Nombre nuevo | Motivo |
|---|---|---|
| EXPEDIENTE | GSTC_EXPEDIENTE | Registro central del que cuelgan todas las tablas Detalle del expediente. |

## 3. Tablas — Detalle (D) — 22 tablas, hijas de EXPEDIENTE/USUARIO/otros

| Nombre actual | Nombre nuevo |
|---|---|
| EXPEDIENTE_ACTA | GSTD_EXPEDIENTE_ACTA |
| EXPEDIENTE_ALERTA | GSTD_EXPEDIENTE_ALERTA |
| EXPEDIENTE_ANALISIS | GSTD_EXPEDIENTE_ANALISIS *(0 filas — tabla en desuso, ver nota abajo)* |
| EXPEDIENTE_ASIGNACION | GSTD_EXPEDIENTE_ASIGNACION |
| EXPEDIENTE_CARGO_ACUSE | GSTD_EXPEDIENTE_CARGO_ACUSE |
| EXPEDIENTE_DERIVACION_EXTERNA | GSTD_EXPEDIENTE_DERIVACION_EXTERNA *(0 filas)* |
| EXPEDIENTE_DIGITAL | GSTD_EXPEDIENTE_DIGITAL *(0 filas — módulo no construido)* |
| EXPEDIENTE_DOCUMENTO | GSTD_EXPEDIENTE_DOCUMENTO |
| EXPEDIENTE_DOCUMENTO_ANALIZADO | GSTD_EXPEDIENTE_DOCUMENTO_ANALIZADO |
| EXPEDIENTE_EVALUACION | GSTD_EXPEDIENTE_EVALUACION |
| EXPEDIENTE_NOTIFICACION | GSTD_EXPEDIENTE_NOTIFICACION |
| EXPEDIENTE_OBSERVACION | GSTD_EXPEDIENTE_OBSERVACION |
| EXPEDIENTE_PERSONA | GSTD_EXPEDIENTE_PERSONA |
| EXPEDIENTE_PUBLICACION | GSTD_EXPEDIENTE_PUBLICACION |
| EXPEDIENTE_RELACION | GSTD_EXPEDIENTE_RELACION |
| EXPEDIENTE_RESOLUCION | GSTD_EXPEDIENTE_RESOLUCION *(0 filas — documentada en CLAUDE.md como tabla muerta, V3 no la usa)* |
| EXPEDIENTE_SOLICITUD | GSTD_EXPEDIENTE_SOLICITUD |
| FLUJO_TRANSICION_EQUIPO | GSTD_FLUJO_TRANSICION_EQUIPO |
| FLUJO_TRANSICION_ROL | GSTD_FLUJO_TRANSICION_ROL |
| PLANTILLA_BLOQUE | GSTD_PLANTILLA_BLOQUE |
| ROL_PERMISO | GSTD_ROL_PERMISO |
| EQUIPO_USUARIO | GSTD_EQUIPO_USUARIO |
| USUARIO_EMAIL_OTP | GSTD_USUARIO_EMAIL_OTP |
| USUARIO_ROL | GSTD_USUARIO_ROL |
| USUARIO_SUPERVISION | GSTD_USUARIO_SUPERVISION |
| USUARIO_TOTP_BACKUP_CODE | GSTD_USUARIO_TOTP_BACKUP_CODE |

## 4. Tablas — Log (L) — 3 tablas, append-only/auditoría

| Nombre actual | Nombre nuevo |
|---|---|
| AUDITORIA_EVENTO | GSTL_AUDITORIA_EVENTO *(0 filas)* |
| CARGA_INICIAL_LOG | GSTL_CARGA_INICIAL_LOG |
| EXPEDIENTE_HISTORIAL | GSTL_EXPEDIENTE_HISTORIAL |

## 5. Tablas — Auxiliar (X) — 6 tablas, configuración/apoyo (no catálogo puro, no detalle transaccional)

| Nombre actual | Nombre nuevo |
|---|---|
| EXPEDIENTE_METRICA_ETAPA | GSTX_EXPEDIENTE_METRICA_ETAPA *(0 filas)* |
| EXPEDIENTE_PLAZO | GSTX_EXPEDIENTE_PLAZO *(0 filas)* |
| GRUPO_FAMILIAR | GSTX_GRUPO_FAMILIAR |
| LEGACY_CATALOGO_MAP | GSTX_LEGACY_CATALOGO_MAP *(0 filas)* |
| LEGACY_ESTADO_MAP | GSTX_LEGACY_ESTADO_MAP |
| PLANTILLA_DOCUMENTO | GSTX_PLANTILLA_DOCUMENTO *(0 filas — BLOB versionado, sin plantilla cargada todavía)* |

**Nota — 9 tablas reales están en 0 filas** (`EXPEDIENTE_ANALISIS`, `EXPEDIENTE_DERIVACION_EXTERNA`, `EXPEDIENTE_DIGITAL`, `EXPEDIENTE_RESOLUCION`, `AUDITORIA_EVENTO`, `EXPEDIENTE_METRICA_ETAPA`, `EXPEDIENTE_PLAZO`, `LEGACY_CATALOGO_MAP`, `PLANTILLA_DOCUMENTO`, `MOTIVO_ARCHIVO`, `PLANTILLA_BLOQUE`), algunas confirmadas en desuso por `CLAUDE.md` (`EXPEDIENTE_ANALISIS`, `EXPEDIENTE_RESOLUCION`). **Decisión confirmada: se renombran igual, sin dar de baja** — un eventual `DROP` queda como decisión aparte, más delicada, que no bloquea el cierre de este estándar.

---

## 6. Vistas (26) — todas clasificadas como Movimiento (V), salvo 1 Histórica (H)

Se retira el prefijo `VW_` (ya lo reemplaza el identificador de objeto `GSV<Tipo>_`).

| Nombre actual | Nombre nuevo |
|---|---|
| VW_BANDEJA_ABOGADO_ANALISIS | GSVV_BANDEJA_ABOGADO_ANALISIS |
| VW_BANDEJA_ASIGNACION | GSVV_BANDEJA_ASIGNACION |
| VW_BANDEJA_CARGOS_ACUSE | GSVV_BANDEJA_CARGOS_ACUSE |
| VW_BANDEJA_CIERRE_ARCHIVO | GSVV_BANDEJA_CIERRE_ARCHIVO |
| VW_BANDEJA_EJECUCION | GSVV_BANDEJA_EJECUCION |
| VW_BANDEJA_EXPEDIENTE_DIGITAL | GSVV_BANDEJA_EXPEDIENTE_DIGITAL |
| VW_BANDEJA_FIRMA_EMISION | GSVV_BANDEJA_FIRMA_EMISION |
| VW_BANDEJA_NOTIFICACION | GSVV_BANDEJA_NOTIFICACION |
| VW_BANDEJA_PUBLICACION | GSVV_BANDEJA_PUBLICACION |
| VW_BANDEJA_REGISTRO | GSVV_BANDEJA_REGISTRO |
| VW_BANDEJA_VERIFICACION | GSVV_BANDEJA_VERIFICACION |
| VW_DOCUMENTO_ESTADO_FINAL_NOTIF | GSVV_DOCUMENTO_ESTADO_FINAL_NOTIF |
| VW_EXPEDIENTE_ACCIONES_PERMITIDAS | GSVV_EXPEDIENTE_ACCIONES_PERMITIDAS |
| VW_EXPEDIENTE_ANALISIS_DETALLE | GSVV_EXPEDIENTE_ANALISIS_DETALLE |
| VW_EXPEDIENTE_ASIGNACION_DETALLE | GSVV_EXPEDIENTE_ASIGNACION_DETALLE |
| VW_EXPEDIENTE_BANDEJA | GSVV_EXPEDIENTE_BANDEJA |
| VW_EXPEDIENTE_CARGOS_ACUSE | GSVV_EXPEDIENTE_CARGOS_ACUSE |
| VW_EXPEDIENTE_CONSOLA | GSVV_EXPEDIENTE_CONSOLA |
| VW_EXPEDIENTE_DIGITAL | GSVV_EXPEDIENTE_DIGITAL |
| VW_EXPEDIENTE_DOCUMENTOS | GSVV_EXPEDIENTE_DOCUMENTOS |
| VW_EXPEDIENTE_DOCUMENTOS_ANALIZADOS | GSVV_EXPEDIENTE_DOCUMENTOS_ANALIZADOS |
| VW_EXPEDIENTE_EVALUACIONES | GSVV_EXPEDIENTE_EVALUACIONES |
| VW_EXPEDIENTE_NOTIFICACIONES | GSVV_EXPEDIENTE_NOTIFICACIONES |
| VW_EXPEDIENTE_PERSONAS | GSVV_EXPEDIENTE_PERSONAS |
| VW_EXPEDIENTE_PUBLICACION | GSVV_EXPEDIENTE_PUBLICACION |
| VW_EXPEDIENTE_RESOLUCIONES | GSVV_EXPEDIENTE_RESOLUCIONES |
| VW_EXPEDIENTE_TIMELINE | **GSVH_EXPEDIENTE_TIMELINE** *(Histórica, no Movimiento — es la línea de tiempo/auditoría del expediente)* |

**Nota**: `GSVV_...` (V de "vista" + V de "movimiento") se ve repetitivo pero es la aplicación literal de la regla del estándar (`<IdObjeto>` = `V<TipoContenido>`, y el tipo de contenido de casi todas estas vistas es "Movimiento" = letra V). No es un error de este documento, así lo define el propio PDF.

---

## 7. Funciones (2) — únicos objetos PL/SQL del esquema

| Nombre actual | Nombre nuevo |
|---|---|
| FN_SDRERC_ES_DIA_HABIL | GSFU_ES_DIA_HABIL |
| FN_SDRERC_SUMAR_DIAS_HABILES | GSFU_SUMAR_DIAS_HABILES |

Sin procedimientos, paquetes, triggers ni clases Java en el esquema — nada más que mapear en esas categorías.

---

## 8. Columnas (871 en total) — regla de prefijo, no listado exhaustivo

Aplicar el prefijo según el contenido semántico de la columna (Tabla VIII del estándar), conservando el nombre actual después del prefijo. Reglas mecánicas según el patrón ya usado en el esquema actual:

| Patrón actual de columna | Prefijo OTI | Ejemplo |
|---|---|---|
| `id_*` (llave foránea/PK numérica) | `ID_` | `id_expediente` → `ID_EXPEDIENTE` |
| `numero_*`/`nu_*` | `NU_` | `numero_expediente` → `NU_EXPEDIENTE` |
| `fecha_*` | `FE_` | `fecha_registro` → `FE_REGISTRO` |
| `codigo`/`*_codigo` | `CO_` | `codigo` → `CO_...` (nombre de tabla implícito) |
| `nombre`/`nombre_completo` | `NO_` | `nombre_completo` → `NO_COMPLETO` |
| `apellido_paterno`/`apellido_materno` | `AP_` | `apellido_paterno` → `AP_PATERNO` |
| `descripcion`/`observacion`/`comentario`/`fundamento`/`motivo` (texto libre) | `DE_` | `observacion` → `DE_OBSERVACION` |
| `activo`/`es_*`/`requiere_*`/booleanos (0/1) | `IN_` (indicador) | `activo` → `IN_ACTIVO` |
| `tipo_*` (clasificación textual, no FK) | `TI_` | `tipo_relacion_persona` → `TI_RELACION_PERSONA` |
| `estado_*` (cuando es texto libre, no FK a catálogo) | `ES_` | — (la mayoría de "estado" en este esquema ya son FK `id_estado_*`, quedan como `ID_`) |
| `username`/`creado_por`/`modificado_por` | `US_` | `creado_por` → `US_CREADO_POR` |
| `direccion`/`ubicacion` | `DI_` | `direccion` → `DI_...` |
| `password`/`password_hash` | `PW_` | `password_hash` → `PW_HASH` |

**No se listan las 871 columnas individualmente en este documento** — el patrón de arriba es 100% mecánico y determinista. Si quieres el detalle exhaustivo columna por columna (por ejemplo para las tablas más grandes: `EXPEDIENTE` tiene ~20 columnas, `EXPEDIENTE_SOLICITUD` ~25), lo genero como anexo aparte.

---

## 9. Índices (215) y Constraints (638) — hallazgo importante: ya están razonablemente bien nombrados

- **PK/FK/UK (70+98+35=203) ya usan prefijos `PK_`/`FK_`/`UK_` con el nombre de tabla** (ej. `PK_AREA`, `FK_USUARIO_ROL_USUARIO`, `UK_AREA_CODIGO`) — **no partimos de cero**. El único cambio real es reemplazar el nombre de tabla viejo por el nuevo ya con prefijo `GST<X>_`: `PK_AREA` → `PK_GSTM_AREA`, `UK_AREA_CODIGO` → `UK_GSTM_AREA_CODIGO`.
- **FK usa sufijo descriptivo en vez de secuencia numérica** (`FK_USUARIO_ROL_USUARIO` en vez de `FK_USUARIO_ROL_01`) — técnicamente el estándar pide secuencia de 2 dígitos. **Decisión confirmada: se conserva el sufijo descriptivo** (desviación documentada del literal del estándar, más legible, sin pérdida de funcionalidad).
- **CHECK: de los 435 reales, 418 son generados automáticamente por Oracle** (`SYS_C%`, son los `NOT NULL` implícitos de cada columna) — **no se tocan**, Oracle los administra solos y no tiene sentido ponerles nombre de negocio. Solo los **17 CHECK con nombre propio** (`CK_SOL_CORREO_VIRTUAL`, `CK_EVAL_INCORPORADO`, etc., reglas de negocio reales) siguen el mismo criterio de PK/FK/UK: agregar el nombre de tabla ya renombrado si corresponde.
- **Índices**: los que respaldan PK ya heredan su nombre (`PK_AREA` es constraint E índice a la vez) — se renombran solos al renombrar el constraint. Los pocos índices adicionales (LOB, `SYS_IL...`) son gestionados internamente por Oracle, no se tocan.

---

## 10. Secuencias (72 reales, `ISEQ$$_NNNNN`) — excepción documentada

**Ninguna secuencia tiene nombre propio** — todas son `ISEQ$$_78730` etc., generadas automáticamente por Oracle porque las columnas ID usan `GENERATED ALWAYS AS IDENTITY` (patrón usado en todo el esquema en vez de `CREATE SEQUENCE` + trigger manual). Renombrarlas de verdad exigiría recrear cada columna (quitar el IDENTITY, crear una secuencia `GSSE_...` explícita, y un trigger `BEFORE INSERT` que la use) — cambio de diseño real, no un simple `RENAME`, con riesgo de afectar el comportamiento de autoincremento. **Decisión confirmada: se acepta como excepción documentada del estándar** — las 72 secuencias `IDENTITY` quedan fuera de alcance de este renombrado; no se convierten a secuencia+trigger explícito.

---

## Decisiones confirmadas (26/08/2026) — cierra el criterio de nomenclatura

1. **Abreviación**: se mantiene el nombre completo de tabla/columna (sin abreviar).
2. **Tablas en 0 filas / en desuso**: se renombran igual, sin dar de baja (`DROP` queda como decisión aparte, no bloqueante).
3. **Sufijo de FK**: se conserva el sufijo descriptivo actual (`FK_TABLA_COLUMNA`), no se cambia a secuencia numérica de 2 dígitos.
4. **Secuencias IDENTITY**: quedan como excepción documentada, sin renombrar ni convertir a secuencia+trigger explícito.

**Con esto, el criterio de nomenclatura queda cerrado.** Lo que sigue (fuera del alcance de este documento) es la ejecución: Fase 3 (análisis de impacto en código V2/V3), Fase 4→5 (estrategia y ejecución del corte por lotes) — ninguna de las cuales se inicia sin tu autorización explícita adicional, siguiendo la misma regla ya vigente del proyecto de no tocar legacy ni ejecutar SQL sin permiso puntual.

---

## Nivel de cumplimiento esperado (confirmado 26/08/2026)

Una vez ejecutado el renombrado completo (Fases 3-5), el cumplimiento del estándar OTI será de **~98-99%, con excepciones documentadas** — no 100% literal. Decisión confirmada explícitamente: **es aceptable un cumplimiento con excepciones documentadas**, no se exige literalidad absoluta. Detalle de lo que sí y lo que no cumple al pie de la letra:

| Objeto | ¿Cumple literal? |
|---|---|
| Tablas, vistas, funciones, columnas, PK, UK, CHECK con nombre propio | ✅ Sí, 100% |
| FK (98 reales) | ⚠️ No literal — conserva sufijo descriptivo en vez de secuencia numérica de 2 dígitos (excepción aceptada) |
| Secuencias (72 `IDENTITY`) | ❌ No — quedan sin nombre propio (excepción aceptada, requeriría cambio de diseño invasivo) |
| Código de sistema "GS" | ⚠️ Condicionado a confirmación externa de OTI (fuera de nuestro control) |

Si en algún momento OTI exige literalidad absoluta (auditoría formal), este documento ya deja identificado exactamente qué 2 puntos habría que revertir y su costo real (renombrar 98 FK a sufijo numérico; convertir 72 columnas IDENTITY a secuencia+trigger explícito).
