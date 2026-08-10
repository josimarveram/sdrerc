/* ============================================================
   SCRIPT 97 - Correccion puntual: limpiar fecha_acuse incorrecta
   Expediente: SDRERC-EXP-2026-000046

   Contexto: antes del fix de 08/08/2026 (ver AGENTS.md, seccion
   "Fix: Bandeja Notificación no excluía documentos POR_PUBLICAR +
   fecha_acuse duplicada al publicar"), el intento de Publicacion
   (tipo_notificacion='PUBLICACION') de este documento se guardo por
   error a traves de la ruta de intentos directos de la 3ra pestana
   (confirmarRecepcionIntentoNotificacion), que sobreescribio
   EXPEDIENTE_DOCUMENTO_ANALIZADO.FECHA_ACUSE aunque el documento nunca
   fue notificado directamente (sus intentos 1 y 2 quedaron ambos
   FALLIDA/No ubicado). Este script limpia unicamente ese dato mal
   escrito, sin tocar NOTIFICADO (que corresponde dejar en 1: el
   documento si fue notificado, via Publicacion) ni ninguna otra
   columna.

   Alcance deliberadamente acotado (defensivo): solo actualiza
   documentos del expediente indicado, solo si FECHA_ACUSE esta
   poblada, y solo si NO existe un intento directo (numero_intento 1 o
   2) realmente EXITOSA para ese documento -- es decir, solo corrige
   el caso exacto reportado (fecha_acuse puesta sin haber sido
   notificado directamente), sin arriesgar un fecha_acuse legitimo de
   otro documento de este mismo expediente.

   Ejecutar conectado como SDRERC_APP. NO ejecutar automaticamente:
   requiere autorizacion explicita separada.
   Idempotente: si ya no hay ninguna fila que cumpla la condicion (ya
   corregido, o nunca aplico), no actualiza nada.
   ============================================================ */

DECLARE
  v_numero_expediente VARCHAR2(60) := 'SDRERC-EXP-2026-000046';
  v_filas_afectadas    NUMBER := 0;
BEGIN
  UPDATE expediente_documento_analizado da
     SET da.fecha_acuse = NULL,
         da.modificado_en = SYSTIMESTAMP
   WHERE da.activo = 1
     AND da.fecha_acuse IS NOT NULL
     AND da.id_expediente IN (
           SELECT e.id_expediente
             FROM expediente e
            WHERE e.numero_expediente = v_numero_expediente
              AND e.activo = 1)
     AND NOT EXISTS (
           SELECT 1
             FROM expediente_notificacion n
             JOIN estado_notificacion en ON en.id_estado_notificacion = n.id_estado_notificacion
            WHERE n.id_documento_analizado = da.id_documento_analizado
              AND n.activo = 1
              AND n.numero_intento IN (1, 2)
              AND en.codigo = 'EXITOSA');

  v_filas_afectadas := SQL%ROWCOUNT;

  IF v_filas_afectadas = 0 THEN
    DBMS_OUTPUT.PUT_LINE(
      'Sin cambios: no se encontro fecha_acuse incorrecta para corregir en ' || v_numero_expediente || '.');
  ELSE
    COMMIT;
    DBMS_OUTPUT.PUT_LINE(
      'Corregido: ' || v_filas_afectadas || ' documento(s) de ' || v_numero_expediente ||
      ' con fecha_acuse limpiada.');
  END IF;
END;
/


/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT da.id_documento_analizado, tda.nombre AS tipo_documento, da.fecha_acuse,
       da.notificado, da.requiere_respuesta
  FROM expediente_documento_analizado da
  JOIN expediente e ON e.id_expediente = da.id_expediente
  LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto
 WHERE e.numero_expediente = 'SDRERC-EXP-2026-000046'
   AND da.activo = 1;

SELECT n.id_expediente_notificacion, n.numero_intento, tn.codigo AS tipo_notificacion,
       en.codigo AS estado_notificacion, n.fecha_envio
  FROM expediente_notificacion n
  JOIN expediente_documento_analizado da ON da.id_documento_analizado = n.id_documento_analizado
  JOIN expediente e ON e.id_expediente = da.id_expediente
  JOIN tipo_notificacion tn ON tn.id_tipo_notificacion = n.id_tipo_notificacion
  JOIN estado_notificacion en ON en.id_estado_notificacion = n.id_estado_notificacion
 WHERE e.numero_expediente = 'SDRERC-EXP-2026-000046'
   AND n.activo = 1
 ORDER BY n.numero_intento;
