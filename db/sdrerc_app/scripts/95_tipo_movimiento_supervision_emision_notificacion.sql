/* ============================================================
   SCRIPT 95 - Codigo de tipo_movimiento para "Registrar Supervision"
   (resultado Aprobado) del mini-panel "Emision" de la Bandeja
   Asignacion de Notificacion.
   Ejecutar conectado como SDRERC_APP.

   Contexto: antes de este script, el boton "Registrar Supervision"
   con resultado Aprobado no dejaba ningun rastro en base de datos
   (solo el resultado Observado registraba algo, al derivar el
   documento a Eq. Analisis/Eq. Ejecucion). El resto de modulos que
   tienen un combo "Resultado" si registran su resultado: Analisis en
   EXPEDIENTE_EVALUACION; Verificacion y Ejecucion en la transicion de
   etapa/estado + EXPEDIENTE_HISTORIAL.motivo. Este script agrega el
   codigo de tipo_movimiento que permite a
   DocumentoAnalisisDAO.registrarSupervisionEmisionAprobada dejar ese
   mismo rastro en la tabla generica expediente_historial (ya
   existente, sin crear tablas nuevas) con
   tabla_relacionada = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' y
   id_registro_relacionado = id_documento_analizado (mismo patron ya
   usado por ASIGNACION_NOTIFICACION/REASIGNACION_NOTIFICACION,
   sembrados por el script 58).

   Idempotente: usa MERGE, no falla si el codigo ya existe.
   ============================================================ */

MERGE INTO tipo_movimiento dst
USING (
  SELECT 'SUPERVISION_EMISION_NOTIFICACION' AS codigo,
         'Supervision aprobada de emision de documento en Notificacion' AS nombre FROM dual
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

SELECT codigo, nombre, activo
FROM tipo_movimiento
WHERE codigo = 'SUPERVISION_EMISION_NOTIFICACION';
