/* ============================================================
   SCRIPT 112 - Codigos de catalogo faltantes detectados al mapear
   el bloque EJECUCIONES.* del Excel de carga inicial (migracion
   historica, ver v3/CLAUDE.md, Incremento 2).
   Ejecutar conectado como SDRERC_APP.

   Contexto: al mapear "EJECUCIONES.TIPO DE CALIFICACION" contra
   TIPO_RESULTADO_EVALUACION se encontraron 9 filas reales con
   "DESESTIMIENTO"/"INFUNDADO" (typo de "desistimiento"), sin codigo
   exacto en el catalogo (10 codigos existentes: EDICTO, EN_ABANDONO,
   FALTA_SUSTENTO, IMPROCEDENTE, INDAGATORIO, NO_CORRESPONDE,
   OBSERVACION_ADMINISTRATIVA, PRECISAR_PRETENSION, PROCEDENTE,
   PROCEDENTE_EN_PARTE). Decision explicita del usuario (26/08/2026):
   agregar codigos nuevos en vez de forzar una aproximacion a un
   codigo ya existente.

   Al mapear "EJECUCIONES.ESTADO" contra TIPO_RESULTADO_EJECUCION se
   encontraron 6 filas reales con "ABANDONO"/"ANULADO", tampoco con
   codigo exacto en ese catalogo (6 codigos existentes:
   CULMINACION_EN_LINEA, EJECUTADO, NO_CORRESPONDE_EJECUTAR,
   PENDIENTE_POR_EJECUTAR, POR_EJECUTAR_POR_OREC,
   RESOLUCION_OBSERVADA). Decision explicita del usuario: agregar
   "EN_ABANDONO" y "ANULADO" como codigos nuevos de este catalogo
   (nombres literales pedidos por el usuario; EN_ABANDONO aqui es un
   codigo propio de TIPO_RESULTADO_EJECUCION, sin relacion con el
   codigo homonimo ya existente en TIPO_RESULTADO_EVALUACION - son
   catalogos distintos, coincide el nombre por ser el mismo concepto
   de negocio en 2 etapas distintas).

   Idempotente (MERGE por codigo, mismo patron ya usado en el
   proyecto - ver script 111). No se ejecuta automaticamente: requiere
   autorizacion explicita antes de correr contra la base.
   ============================================================ */

-- TIPO_RESULTADO_EVALUACION: agrega DESISTIMIENTO e INFUNDADO
MERGE INTO tipo_resultado_evaluacion dst
USING (
  SELECT 'DESISTIMIENTO' AS codigo, 'Desistimiento' AS nombre FROM dual
  UNION ALL
  SELECT 'INFUNDADO', 'Infundado' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

-- TIPO_RESULTADO_EJECUCION: agrega EN_ABANDONO y ANULADO
MERGE INTO tipo_resultado_ejecucion dst
USING (
  SELECT 'EN_ABANDONO' AS codigo, 'En abandono' AS nombre FROM dual
  UNION ALL
  SELECT 'ANULADO', 'Anulado' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (src.codigo, src.nombre, 1);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT codigo, nombre, activo FROM tipo_resultado_evaluacion
WHERE codigo IN ('DESISTIMIENTO','INFUNDADO') ORDER BY codigo;

SELECT codigo, nombre, activo FROM tipo_resultado_ejecucion
WHERE codigo IN ('EN_ABANDONO','ANULADO') ORDER BY codigo;
