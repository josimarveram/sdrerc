/* ============================================================
   SCRIPT 52 - Correccion de clasificacion de tipos de documento de Analisis
   Ejecutar conectado como SDRERC_APP.

   Contexto: el script 43 intento clasificar tipos de documento como
   INTERMEDIO/FINAL usando codigos ('ANALISIS_01_CARTA_EDICTO', etc.)
   que NO coinciden con los codigos realmente sembrados por el script
   31 ('ANALISIS_DOC_01_CARTA_ABANDONO', etc. -- con infijo "DOC").
   Como resultado, ese UPDATE no afecto ninguna fila y hoy ningun tipo
   de documento tiene clasificacion asignada.

   Este script corrige la clasificacion usando los codigos reales:
   - Cartas y oficios de tramite (comunicaciones intermedias al
     ciudadano/entidades durante el proceso) -> INTERMEDIO.
   - Resoluciones y cartas de resultado final (Procedente/Improcedente/
     Procedente en parte) -> FINAL.
   - Informes internos (uso interno, no se notifican al ciudadano) ->
     sin clasificar (NULL), no participan de Cartas de Respuesta ni
     del auto-marcado de "Requiere respuesta".

   Idempotente: solo actualiza cuando la clasificacion actual difiere.
   No inserta ni elimina filas, no toca datos de expedientes.
   ============================================================ */

UPDATE tipo_documento_adjunto
   SET clasificacion = 'INTERMEDIO'
 WHERE UPPER(codigo) IN (
       'ANALISIS_DOC_01_CARTA_ABANDONO',
       'ANALISIS_DOC_02_CARTA_EDICTO',
       'ANALISIS_DOC_03_CARTA_FALTA_SUSTENTO',
       'ANALISIS_DOC_04_CARTA_INDAGATORIO',
       'ANALISIS_DOC_05_CARTA_PRECISAR_PRETENSION',
       'ANALISIS_DOC_10_OFICIO_INDAGATORIO_CANCELACION',
       'ANALISIS_DOC_11_OFICIO_RECONSTITUCION'
       )
   AND (clasificacion IS NULL OR clasificacion <> 'INTERMEDIO');

UPDATE tipo_documento_adjunto
   SET clasificacion = 'FINAL'
 WHERE UPPER(codigo) IN (
       'ANALISIS_DOC_12_RESOLUCION_ABANDONO',
       'ANALISIS_DOC_13_RESOLUCION_CANCELACION',
       'ANALISIS_DOC_14_RESOLUCION_ERROR_MATERIAL',
       'ANALISIS_DOC_15_RESOLUCION_RECONSTITUCION',
       'ANALISIS_DOC_16_RESOLUCION_RECTIFICACION',
       'ANALISIS_DOC_17_CARTA_IMPROCEDENTE',
       'ANALISIS_DOC_18_CARTA_PROCEDENTE',
       'ANALISIS_DOC_19_CARTA_PROCEDENTE_EN_PARTE'
       )
   AND (clasificacion IS NULL OR clasificacion <> 'FINAL');

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT codigo, nombre, clasificacion, activo
  FROM tipo_documento_adjunto
 WHERE codigo LIKE 'ANALISIS_%'
 ORDER BY codigo;
