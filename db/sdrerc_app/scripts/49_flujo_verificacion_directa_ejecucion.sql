/* ============================================================
   SCRIPT 49 - Transicion directa Verificacion (Aprobado) -> Ejecucion
   Ejecutar conectado como SDRERC_APP.
   Idempotente: usa MERGE y conserva datos existentes.

   Contexto: el panel de Verificacion ahora resuelve documento a
   documento (Estado/N° Documento/Fecha Emision se editan en la
   grilla "Documentos verificados"). El boton "Registrar Verificacion"
   pasa a tener un unico resultado "Aprobado" u "Observado":
   - Aprobado + resultado de analisis resolutivo (PROCEDENTE o
     PROCEDENTE EN PARTE): el expediente debe pasar directamente de
     VERIFICACION/EN_VERIFICACION a EJECUCION/EN_EJECUCION, sin pasar
     por la etapa FIRMA_EMISION (ya integrada dentro de Verificacion).
   - Observado: usa las transiciones ya existentes
     REGISTRO_OBSERVACION_VERIFICACION y DEVOLUCION_A_ANALISIS, que no
     requieren cambios de esquema.

   Este script agrega unicamente la transicion faltante para el caso
   Aprobado -> Ejecucion, reutilizando el codigo_accion
   'APROBACION_VERIFICACION' ya existente en tipo_movimiento.
   ============================================================ */

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo, eo.id_etapa AS id_etapa_origen, so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino, sd.id_estado AS id_estado_destino,
         'APROBACION_VERIFICACION' AS codigo_accion,
         'Aprobar verificacion y enviar directo a ejecucion' AS nombre_accion,
         0 AS requiere_comentario, 0 AS requiere_documento
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'EN_VERIFICACION'
  JOIN etapa_expediente ed ON ed.codigo = 'EJECUCION'
  JOIN estado_expediente sd ON sd.codigo = 'EN_EJECUCION'
  WHERE f.codigo = 'SDRERC_TO_BE'
) s
ON (
  t.id_flujo = s.id_flujo
  AND t.id_etapa_origen = s.id_etapa_origen
  AND t.id_estado_origen = s.id_estado_origen
  AND t.codigo_accion = s.codigo_accion
  AND t.id_etapa_destino = s.id_etapa_destino
  AND t.id_estado_destino = s.id_estado_destino
)
WHEN MATCHED THEN UPDATE SET t.nombre_accion = s.nombre_accion, t.requiere_comentario = s.requiere_comentario, t.requiere_documento = s.requiere_documento, t.activo = 1
WHEN NOT MATCHED THEN INSERT (id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino, codigo_accion, nombre_accion, requiere_comentario, requiere_documento, activo)
VALUES (s.id_flujo, s.id_etapa_origen, s.id_estado_origen, s.id_etapa_destino, s.id_estado_destino, s.codigo_accion, s.nombre_accion, s.requiere_comentario, s.requiere_documento, 1);

COMMIT;

/* ============================================================
   Validacion posterior
   ============================================================ */

SELECT ft.codigo_accion,
       eo.codigo AS etapa_origen,
       so.codigo AS estado_origen,
       ed.codigo AS etapa_destino,
       sd.codigo AS estado_destino,
       ft.requiere_comentario,
       ft.requiere_documento,
       ft.activo
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
WHERE f.codigo = 'SDRERC_TO_BE'
  AND eo.codigo = 'VERIFICACION'
  AND so.codigo = 'EN_VERIFICACION'
ORDER BY ft.codigo_accion;
