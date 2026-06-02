/* ============================================================
   SCRIPT 18 - Datos maestros para combos de Registro Manual V2
   Ejecutar conectado como SDRERC_APP.

   Alcance:
   - Poblar de forma idempotente los catalogos soportados por la
     estructura actual de SDRERC_APP.
   - No crea tablas, no elimina datos y no reestructura el modelo.
   - Los combos sin tabla/campo claro quedan diagnosticados al final.

   Recomendacion futura:
   - Separar Canal / Origen documental y Modalidad de ingreso.
   - Crear catalogos formales para grupo familiar, grado de parentesco
     y tipo de solicitud antes de persistirlos fuera de observaciones.
   ============================================================ */

/* ------------------------------------------------------------
   1. Tipo procedimiento -> procedimiento_registral
   ------------------------------------------------------------ */
MERGE INTO procedimiento_registral dst
USING (
  SELECT 'RECTIFICACION_ADMINISTRATIVA' AS codigo, 'Rectificación administrativa' AS nombre FROM dual
  UNION ALL SELECT 'TITULO_NACIONALIDAD', 'Título de Nacionalidad' FROM dual
  UNION ALL SELECT 'CANCELACION', 'Cancelación' FROM dual
  UNION ALL SELECT 'RECONSIDERACION', 'Reconsideración' FROM dual
  UNION ALL SELECT 'RECONSTITUCION', 'Reconstitución' FROM dual
  UNION ALL SELECT 'APELACION', 'Apelación' FROM dual
  UNION ALL SELECT 'ACTUALIZACION_DATOS', 'Actualización de datos' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

/* ------------------------------------------------------------
   2. Tipo documento -> tipo_documento
   ------------------------------------------------------------ */
MERGE INTO tipo_documento dst
USING (
  SELECT 'SOLICITUD' AS codigo, 'Solicitud' AS nombre FROM dual
  UNION ALL SELECT 'CARTA', 'Carta' FROM dual
  UNION ALL SELECT 'INFORME', 'Informe' FROM dual
  UNION ALL SELECT 'OFICIO', 'Oficio' FROM dual
  UNION ALL SELECT 'RESOLUCION', 'Resolución' FROM dual
  UNION ALL SELECT 'HOJA_ENVIO', 'Hoja de envío' FROM dual
  UNION ALL SELECT 'MEMORANDO', 'Memorando' FROM dual
  UNION ALL SELECT 'HOJA_ELEVACION', 'Hoja de elevación' FROM dual
  UNION ALL SELECT 'PROVEIDO', 'Proveido' FROM dual
  UNION ALL SELECT 'PEDIDO', 'Pedido' FROM dual
  UNION ALL SELECT 'FORMATO', 'Formato' FROM dual
  UNION ALL SELECT 'EXPEDIENTE', 'Expediente' FROM dual
  UNION ALL SELECT 'AYUDA_MEMORIA', 'Ayuda memoria' FROM dual
  UNION ALL SELECT 'INFORME_TECNICO', 'Informe técnico' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

/* ------------------------------------------------------------
   3. Canal de ingreso -> canal_recepcion

   Diagnostico:
   La base actual ya trae canales simples:
   MESA_PARTES, TRAMITE_DOCUMENTARIO, OGD, CORREO_INSTITUCIONAL,
   PRESENCIAL y VIRTUAL.

   Para cumplir la lista oficial operativa del Registro Manual sin
   reestructurar BD, se agregan codigos compuestos oficiales.
   ------------------------------------------------------------ */
MERGE INTO canal_recepcion dst
USING (
  SELECT 'INTERNO' AS codigo, 'Interno' AS nombre FROM dual
  UNION ALL SELECT 'MESA_PARTES_PRESENCIAL', 'Mesa de partes presencial' FROM dual
  UNION ALL SELECT 'MESA_PARTES_VIRTUAL', 'Mesa de partes virtual' FROM dual
  UNION ALL SELECT 'OR_PRESENCIAL', 'OR Presencial' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

/* ------------------------------------------------------------
   4. Tipo de acta -> tipo_acta
   ------------------------------------------------------------ */
MERGE INTO tipo_acta dst
USING (
  SELECT 'NACIMIENTO' AS codigo, 'Nacimiento' AS nombre FROM dual
  UNION ALL SELECT 'MATRIMONIO', 'Matrimonio' FROM dual
  UNION ALL SELECT 'DEFUNCION', 'Defunción' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

/* ------------------------------------------------------------
   5. Unidad organica -> area

   La estructura actual no tiene tabla UNIDAD_ORGANICA. Se usa AREA
   porque ya representa unidades organizativas internas y tiene codigo,
   nombre, descripcion y activo.
   ------------------------------------------------------------ */
MERGE INTO area dst
USING (
  SELECT 'DRC' AS codigo, 'DIRECCIÓN DE REGISTROS CIVILES' AS nombre,
         'Unidad orgánica usada por Registro Manual V2' AS descripcion FROM dual
  UNION ALL SELECT 'SD_TECNICA_NORMATIVA', 'SUB DIRECCIÓN TÉCNICA NORMATIVA',
         'Unidad orgánica usada por Registro Manual V2' FROM dual
  UNION ALL SELECT 'SD_PROCESAMIENTO_REGISTROS_CIVILES', 'SUB DIRECCIÓN DE PROCESAMIENTO DE REGISTROS CIVILES',
         'Unidad orgánica usada por Registro Manual V2' FROM dual
  UNION ALL SELECT 'SD_VINCULOS_ARCHIVO_REGISTRAL', 'SUB DIRECCIÓN DE VÍNCULOS Y ARCHIVO REGISTRAL',
         'Unidad orgánica usada por Registro Manual V2' FROM dual
  UNION ALL SELECT 'SD_INVESTIGACION_DEPURACION_REGISTRAL', 'SUB DIRECCIÓN DE INVESTIGACIÓN Y DEPURACIÓN REGISTRAL',
         'Unidad orgánica usada por Registro Manual V2' FROM dual
  UNION ALL SELECT 'SD_PROCESAMIENTO_IDENTIFICACION', 'SUB DIRECCIÓN DE PROCESAMIENTO DE IDENTIFICACIÓN',
         'Unidad orgánica usada por Registro Manual V2' FROM dual
  UNION ALL SELECT 'OREC', 'OFICINA DE REGISTRO Y ESTADO CIVIL',
         'Unidad orgánica usada por Registro Manual V2' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.descripcion = src.descripcion,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, descripcion, activo)
  VALUES (src.codigo, src.nombre, src.descripcion, 1);

COMMIT;

/* ============================================================
   VALIDACIONES
   ============================================================ */

/* Catalogos soportados por la estructura actual */
SELECT 'PROCEDIMIENTO_REGISTRAL' AS catalogo, codigo, nombre, activo
FROM procedimiento_registral
WHERE codigo IN (
  'RECTIFICACION_ADMINISTRATIVA',
  'TITULO_NACIONALIDAD',
  'CANCELACION',
  'RECONSIDERACION',
  'RECONSTITUCION',
  'APELACION',
  'ACTUALIZACION_DATOS'
)
UNION ALL
SELECT 'TIPO_DOCUMENTO', codigo, nombre, activo
FROM tipo_documento
WHERE codigo IN (
  'SOLICITUD',
  'CARTA',
  'INFORME',
  'OFICIO',
  'RESOLUCION',
  'HOJA_ENVIO',
  'MEMORANDO',
  'HOJA_ELEVACION',
  'PROVEIDO',
  'PEDIDO',
  'FORMATO',
  'EXPEDIENTE',
  'AYUDA_MEMORIA',
  'INFORME_TECNICO'
)
UNION ALL
SELECT 'CANAL_RECEPCION', codigo, nombre, activo
FROM canal_recepcion
WHERE codigo IN (
  'INTERNO',
  'MESA_PARTES_PRESENCIAL',
  'MESA_PARTES_VIRTUAL',
  'OR_PRESENCIAL'
)
UNION ALL
SELECT 'TIPO_ACTA', codigo, nombre, activo
FROM tipo_acta
WHERE codigo IN (
  'NACIMIENTO',
  'MATRIMONIO',
  'DEFUNCION'
)
UNION ALL
SELECT 'AREA_UNIDAD_ORGANICA', codigo, nombre, activo
FROM area
WHERE codigo IN (
  'DRC',
  'SD_TECNICA_NORMATIVA',
  'SD_PROCESAMIENTO_REGISTROS_CIVILES',
  'SD_VINCULOS_ARCHIVO_REGISTRAL',
  'SD_INVESTIGACION_DEPURACION_REGISTRAL',
  'SD_PROCESAMIENTO_IDENTIFICACION',
  'OREC'
)
ORDER BY 1, 3;

/* Verificar que todas las opciones oficiales soportadas existan */
WITH esperado AS (
  SELECT 'PROCEDIMIENTO_REGISTRAL' AS catalogo, 'RECTIFICACION_ADMINISTRATIVA' AS codigo, 'Rectificación administrativa' AS nombre FROM dual
  UNION ALL SELECT 'PROCEDIMIENTO_REGISTRAL', 'TITULO_NACIONALIDAD', 'Título de Nacionalidad' FROM dual
  UNION ALL SELECT 'PROCEDIMIENTO_REGISTRAL', 'CANCELACION', 'Cancelación' FROM dual
  UNION ALL SELECT 'PROCEDIMIENTO_REGISTRAL', 'RECONSIDERACION', 'Reconsideración' FROM dual
  UNION ALL SELECT 'PROCEDIMIENTO_REGISTRAL', 'RECONSTITUCION', 'Reconstitución' FROM dual
  UNION ALL SELECT 'PROCEDIMIENTO_REGISTRAL', 'APELACION', 'Apelación' FROM dual
  UNION ALL SELECT 'PROCEDIMIENTO_REGISTRAL', 'ACTUALIZACION_DATOS', 'Actualización de datos' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'SOLICITUD', 'Solicitud' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'CARTA', 'Carta' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'INFORME', 'Informe' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'OFICIO', 'Oficio' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'RESOLUCION', 'Resolución' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'HOJA_ENVIO', 'Hoja de envío' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'MEMORANDO', 'Memorando' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'HOJA_ELEVACION', 'Hoja de elevación' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'PROVEIDO', 'Proveido' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'PEDIDO', 'Pedido' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'FORMATO', 'Formato' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'EXPEDIENTE', 'Expediente' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'AYUDA_MEMORIA', 'Ayuda memoria' FROM dual
  UNION ALL SELECT 'TIPO_DOCUMENTO', 'INFORME_TECNICO', 'Informe técnico' FROM dual
  UNION ALL SELECT 'CANAL_RECEPCION', 'INTERNO', 'Interno' FROM dual
  UNION ALL SELECT 'CANAL_RECEPCION', 'MESA_PARTES_PRESENCIAL', 'Mesa de partes presencial' FROM dual
  UNION ALL SELECT 'CANAL_RECEPCION', 'MESA_PARTES_VIRTUAL', 'Mesa de partes virtual' FROM dual
  UNION ALL SELECT 'CANAL_RECEPCION', 'OR_PRESENCIAL', 'OR Presencial' FROM dual
  UNION ALL SELECT 'TIPO_ACTA', 'NACIMIENTO', 'Nacimiento' FROM dual
  UNION ALL SELECT 'TIPO_ACTA', 'MATRIMONIO', 'Matrimonio' FROM dual
  UNION ALL SELECT 'TIPO_ACTA', 'DEFUNCION', 'Defunción' FROM dual
  UNION ALL SELECT 'AREA_UNIDAD_ORGANICA', 'DRC', 'DIRECCIÓN DE REGISTROS CIVILES' FROM dual
  UNION ALL SELECT 'AREA_UNIDAD_ORGANICA', 'SD_TECNICA_NORMATIVA', 'SUB DIRECCIÓN TÉCNICA NORMATIVA' FROM dual
  UNION ALL SELECT 'AREA_UNIDAD_ORGANICA', 'SD_PROCESAMIENTO_REGISTROS_CIVILES', 'SUB DIRECCIÓN DE PROCESAMIENTO DE REGISTROS CIVILES' FROM dual
  UNION ALL SELECT 'AREA_UNIDAD_ORGANICA', 'SD_VINCULOS_ARCHIVO_REGISTRAL', 'SUB DIRECCIÓN DE VÍNCULOS Y ARCHIVO REGISTRAL' FROM dual
  UNION ALL SELECT 'AREA_UNIDAD_ORGANICA', 'SD_INVESTIGACION_DEPURACION_REGISTRAL', 'SUB DIRECCIÓN DE INVESTIGACIÓN Y DEPURACIÓN REGISTRAL' FROM dual
  UNION ALL SELECT 'AREA_UNIDAD_ORGANICA', 'SD_PROCESAMIENTO_IDENTIFICACION', 'SUB DIRECCIÓN DE PROCESAMIENTO DE IDENTIFICACIÓN' FROM dual
  UNION ALL SELECT 'AREA_UNIDAD_ORGANICA', 'OREC', 'OFICINA DE REGISTRO Y ESTADO CIVIL' FROM dual
),
actual AS (
  SELECT 'PROCEDIMIENTO_REGISTRAL' AS catalogo, codigo, nombre, activo FROM procedimiento_registral
  UNION ALL SELECT 'TIPO_DOCUMENTO', codigo, nombre, activo FROM tipo_documento
  UNION ALL SELECT 'CANAL_RECEPCION', codigo, nombre, activo FROM canal_recepcion
  UNION ALL SELECT 'TIPO_ACTA', codigo, nombre, activo FROM tipo_acta
  UNION ALL SELECT 'AREA_UNIDAD_ORGANICA', codigo, nombre, activo FROM area
)
SELECT e.catalogo,
       e.codigo,
       e.nombre,
       CASE
         WHEN a.codigo IS NULL THEN 'FALTA'
         WHEN a.activo = 1 THEN 'ACTIVO'
         ELSE 'INACTIVO'
       END AS estado_validacion
FROM esperado e
LEFT JOIN actual a
  ON a.catalogo = e.catalogo
 AND UPPER(a.codigo) = e.codigo
ORDER BY 1, 3;

/* Detectar duplicados por catalogo/codigo */
SELECT catalogo, codigo, cantidad
FROM (
  SELECT 'PROCEDIMIENTO_REGISTRAL' AS catalogo, codigo, COUNT(*) AS cantidad
  FROM procedimiento_registral
  GROUP BY codigo
  HAVING COUNT(*) > 1
  UNION ALL
  SELECT 'TIPO_DOCUMENTO', codigo, COUNT(*)
  FROM tipo_documento
  GROUP BY codigo
  HAVING COUNT(*) > 1
  UNION ALL
  SELECT 'CANAL_RECEPCION', codigo, COUNT(*)
  FROM canal_recepcion
  GROUP BY codigo
  HAVING COUNT(*) > 1
  UNION ALL
  SELECT 'TIPO_ACTA', codigo, COUNT(*)
  FROM tipo_acta
  GROUP BY codigo
  HAVING COUNT(*) > 1
  UNION ALL
  SELECT 'AREA_UNIDAD_ORGANICA', codigo, COUNT(*)
  FROM area
  GROUP BY codigo
  HAVING COUNT(*) > 1
)
ORDER BY 1, 2;

/* Verificar que los combos actuales del Registro Manual tengan datos */
SELECT 'Tipo procedimiento' AS combo, COUNT(*) AS opciones_activas
FROM procedimiento_registral
WHERE activo = 1
UNION ALL
SELECT 'Tipo documento', COUNT(*)
FROM tipo_documento
WHERE activo = 1
UNION ALL
SELECT 'Canal de ingreso', COUNT(*)
FROM canal_recepcion
WHERE activo = 1
UNION ALL
SELECT 'Tipo de acta', COUNT(*)
FROM tipo_acta
WHERE activo = 1;

/* Catalogos oficiales que requieren estructura futura */
WITH pendiente AS (
  SELECT 'GRUPO_FAMILIAR' AS catalogo, 'CON_GRUPO_FAMILIAR' AS codigo, 'Con grupo familiar' AS nombre FROM dual
  UNION ALL SELECT 'GRUPO_FAMILIAR', 'SIN_GRUPO_FAMILIAR', 'Sin grupo familiar' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'PADRE', 'PADRE' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'MADRE', 'MADRE' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'HIJO', 'HIJO(A)' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'ABUELO', 'ABUELO(A)' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'HERMANO', 'HERMANO(A)' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'NIETO', 'NIETO(A)' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'BISABUELO', 'BISABUELO(A)' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'TIO', 'TIO(A)' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'SOBRINO', 'SOBRINO(A)' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'BIZNIETO', 'BIZNIETO(A)' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'TIO_ABUELO', 'TIO(A) ABUELO(A)' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'PRIMO', 'PRIMO(A)' FROM dual
  UNION ALL SELECT 'GRADO_PARENTESCO', 'SOBRINO_NIETO', 'SOBRINO(A) NIETO(A)' FROM dual
  UNION ALL SELECT 'TIPO_SOLICITUD', 'PARTE', 'Parte' FROM dual
  UNION ALL SELECT 'TIPO_SOLICITUD', 'OFICIO', 'Oficio' FROM dual
)
SELECT catalogo,
       codigo,
       nombre,
       'PENDIENTE_ESTRUCTURA' AS estado_validacion,
       'No existe tabla/campo especifico en scripts base SDRERC_APP para persistir este catalogo sin reestructuracion.' AS observacion
FROM pendiente
ORDER BY 1, 3;
