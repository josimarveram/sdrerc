/* ============================================================
   SCRIPT 55 - Correccion de clasificacion de Carta Abandono a FINAL
   Ejecutar conectado como SDRERC_APP.

   Contexto: el script 52 clasifico 'ANALISIS_DOC_01_CARTA_ABANDONO'
   como INTERMEDIO (comunicacion de tramite). Segun la taxonomia de
   negocio confirmada, la clasificacion de documentos de Analisis es:
   - Cartas finales: carta_abandono, carta_improcedente, carta_procedente,
     carta_procedente_en_parte.
   - Cartas intermedias: carta_edicto, carta_falta_sustento,
     carta_indagatorio, carta_precisar_pretension.
   - Informes: informe_abandono, informe_cancelacion,
     informe_reconstitucion, informe_rectificacion (sin clasificar,
     uso interno).
   - Oficios: oficio_indagatorio_cancelacion, oficio_reconstitucion.
   - Resoluciones: resolucion_abandono, resolucion_cancelacion,
     resolucion_error_material, resolucion_reconstitucion,
     resolucion_rectificacion.

   Carta Abandono debe agruparse con las demas cartas finales
   (Improcedente/Procedente/Procedente en parte), no con las cartas
   intermedias. Este script corrige unicamente esa fila; el resto de
   la clasificacion sembrada por el script 52 ya coincide con la
   taxonomia confirmada.

   Idempotente: solo actualiza cuando la clasificacion actual difiere.
   No inserta ni elimina filas, no toca datos de expedientes.
   ============================================================ */

UPDATE tipo_documento_adjunto
   SET clasificacion = 'FINAL'
 WHERE UPPER(codigo) = 'ANALISIS_DOC_01_CARTA_ABANDONO'
   AND (clasificacion IS NULL OR clasificacion <> 'FINAL');

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT codigo, nombre, clasificacion, activo
  FROM tipo_documento_adjunto
 WHERE codigo LIKE 'ANALISIS_%'
 ORDER BY codigo;
