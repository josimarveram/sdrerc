/* ============================================================
   SCRIPT 113 - Codigos de tipo_documento_adjunto faltantes para
   las cartas de notificacion de resultados Desistimiento/Infundado
   (Incremento 3 de la carga inicial - migracion historica, ver
   v3/CLAUDE.md). Ejecutar conectado como SDRERC_APP.

   Contexto: EJECUCIONES.TIPO DE CALIFICACION trae, entre otros,
   valores "DESESTIMIENTO" (typo de "desistimiento") e "INFUNDADO"
   (9 filas reales en el Excel de carga inicial). El catalogo
   tipo_documento_adjunto ya tiene cartas de notificacion FINAL para
   Procedente/Improcedente/Procedente en parte (ANALISIS_DOC_17/18/19)
   y para Abandono (ANALISIS_DOC_01_CARTA_ABANDONO), pero no para
   Desistimiento ni Infundado - solo existe una "Resolucion
   Desistimiento" (ANALISIS_20_RESOLUCION_DESISTIMIENTO), sin su
   carta homologa. Decision explicita del usuario (26/08/2026):
   agregar los 2 codigos nuevos en vez de reutilizar uno existente.

   Idempotente (MERGE por codigo, mismo patron ya usado en los
   scripts 111/112). No se ejecuta automaticamente: requiere
   autorizacion explicita antes de correr contra la base.
   ============================================================ */

MERGE INTO tipo_documento_adjunto dst
USING (
  SELECT 'ANALISIS_DOC_22_CARTA_DESISTIMIENTO' AS codigo, 'Carta Desistimiento' AS nombre, 'FINAL' AS clasificacion FROM dual
  UNION ALL
  SELECT 'ANALISIS_DOC_23_CARTA_INFUNDADO', 'Carta Infundado', 'FINAL' FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.clasificacion = src.clasificacion,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, clasificacion, activo, creado_en)
  VALUES (src.codigo, src.nombre, src.clasificacion, 1, SYSTIMESTAMP);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT codigo, nombre, clasificacion, activo FROM tipo_documento_adjunto
WHERE codigo IN ('ANALISIS_DOC_22_CARTA_DESISTIMIENTO','ANALISIS_DOC_23_CARTA_INFUNDADO')
ORDER BY codigo;
