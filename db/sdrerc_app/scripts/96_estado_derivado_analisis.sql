/* ============================================================
   SCRIPT 96 - Nuevo estado ANALISIS/DERIVADO y transiciones desde
   Notificacion, para distinguir un expediente derivado desde la
   Bandeja Cartas de Respuesta (Asignacion) de uno realmente Observado
   por el validador/supervisor de Notificacion.
   Ejecutar conectado como SDRERC_APP.

   Contexto: "Registrar asignacion" del panel Cartas de Respuesta
   (JPanelAsignacionV2.registrarAsignacionCarta/registrarAsignacionDocumento)
   y el "Registrar Supervision" (resultado Observado) del panel de
   Asignacion de Notificacion (JPanelNotificacionV2.derivarAsigNotifADestinoOperativo)
   usaban el MISMO destino ANALISIS/OBSERVADO al derivar de vuelta a
   Analisis, aunque son 2 situaciones de negocio distintas: la primera
   es una carta intermedia ya respondida por el ciudadano que el
   abogado debe continuar trabajando (no es una observacion/rechazo);
   la segunda si es una observacion real del supervisor de Notificacion
   que exige correccion. Pedido explicito del usuario (07/08/2026):
   diferenciarlas con un estado nuevo "Derivado" para la primera,
   dejando "Observado" exclusivo de la segunda.

   Mismo patron ya usado por el script 91 (Camino A, origenes
   NOTIFICACION/EN_NOTIFICACION y NOTIFICACION/NOTIFICADO): se agregan
   filas de flujo_transicion NUEVAS con el mismo codigo_accion
   DEVOLUCION_A_ANALISIS pero destino ANALISIS/DERIVADO en vez de
   ANALISIS/OBSERVADO. El script 93 (Camino B, origen
   NOTIFICACION/POR_VALIDAR -> ANALISIS/OBSERVADO) NO se toca: sigue
   siendo el unico origen posible para el caso Observado real.

   Idempotente: el estado se inserta solo si no existe (por codigo +
   etapa), y cada transicion verifica NOT EXISTS por combinacion
   exacta de codigo_accion + etapa/estado origen + etapa/estado destino.
   ============================================================ */

INSERT INTO estado_expediente (id_etapa, codigo, nombre)
SELECT id_etapa, 'DERIVADO', 'Derivado'
  FROM etapa_expediente
 WHERE codigo = 'ANALISIS'
   AND NOT EXISTS (
         SELECT 1 FROM estado_expediente se
          JOIN etapa_expediente ee ON ee.id_etapa = se.id_etapa
          WHERE ee.codigo = 'ANALISIS' AND UPPER(se.codigo) = 'DERIVADO');

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DEVOLUCION_A_ANALISIS', 'Derivar a Analisis desde Cartas de Respuesta (Asignacion)', 0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'EN_NOTIFICACION'
  JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
  JOIN estado_expediente sd ON sd.codigo = 'DERIVADO'
 WHERE f.codigo = 'SDRERC_TO_BE'
   AND NOT EXISTS (
         SELECT 1 FROM flujo_transicion ft2
          WHERE ft2.codigo_accion = 'DEVOLUCION_A_ANALISIS'
            AND ft2.id_etapa_origen = eo.id_etapa
            AND ft2.id_estado_origen = so.id_estado
            AND ft2.id_etapa_destino = ed.id_etapa
            AND ft2.id_estado_destino = sd.id_estado);

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DEVOLUCION_A_ANALISIS', 'Derivar a Analisis desde Cartas de Respuesta (Asignacion)', 0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
  JOIN estado_expediente so ON so.codigo = 'NOTIFICADO'
  JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
  JOIN estado_expediente sd ON sd.codigo = 'DERIVADO'
 WHERE f.codigo = 'SDRERC_TO_BE'
   AND NOT EXISTS (
         SELECT 1 FROM flujo_transicion ft2
          WHERE ft2.codigo_accion = 'DEVOLUCION_A_ANALISIS'
            AND ft2.id_etapa_origen = eo.id_etapa
            AND ft2.id_estado_origen = so.id_estado
            AND ft2.id_etapa_destino = ed.id_etapa
            AND ft2.id_estado_destino = sd.id_estado);

/* ------------------------------------------------------------
   Transiciones REGISTRO_RESULTADO_ANALISIS con origen DERIVADO,
   replicando exactamente el mismo set de 5 destinos ya sembrado
   para origen OBSERVADO en el script 20
   (20_datos_maestros_flujo_to_be_v2_bpmn.sql, lineas 72-81):
   ATENDIDO, OBSERVADO, NO_CORRESPONDE, EN_ABANDONO,
   OBSERVACION_ADMINISTRATIVA. Sin esto, "Registrar resultado
   final" fallaria (AnalisisExpedienteDAO.registrarAnalisis exige
   una fila de flujo_transicion real via requerirTransicion) para
   cualquier expediente que llegue a Analisis como Derivado.
   ------------------------------------------------------------ */

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo,
         eo.id_etapa AS id_etapa_origen,
         so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino,
         sd.id_estado AS id_estado_destino,
         r.codigo_accion,
         r.nombre_accion,
         r.requiere_comentario,
         r.requiere_documento
    FROM (
      SELECT 'ANALISIS' etapa_origen, 'DERIVADO' estado_origen, 'ANALISIS' etapa_destino, 'ATENDIDO' estado_destino,
             'REGISTRO_RESULTADO_ANALISIS' codigo_accion, 'Registrar correccion de analisis desde derivado' nombre_accion, 1 requiere_comentario, 0 requiere_documento FROM dual
      UNION ALL SELECT 'ANALISIS', 'DERIVADO', 'ANALISIS', 'OBSERVADO',
             'REGISTRO_RESULTADO_ANALISIS', 'Observar expediente derivado', 1, 0 FROM dual
      UNION ALL SELECT 'ANALISIS', 'DERIVADO', 'ANALISIS', 'NO_CORRESPONDE',
             'REGISTRO_RESULTADO_ANALISIS', 'Registrar no corresponde desde derivado', 1, 0 FROM dual
      UNION ALL SELECT 'ANALISIS', 'DERIVADO', 'ANALISIS', 'EN_ABANDONO',
             'REGISTRO_RESULTADO_ANALISIS', 'Registrar abandono desde derivado', 1, 0 FROM dual
      UNION ALL SELECT 'ANALISIS', 'DERIVADO', 'ANALISIS', 'OBSERVACION_ADMINISTRATIVA',
             'REGISTRO_RESULTADO_ANALISIS', 'Registrar observacion administrativa desde derivado', 1, 0 FROM dual
    ) r
    JOIN flujo f ON f.codigo = 'SDRERC_TO_BE'
    JOIN etapa_expediente eo ON eo.codigo = r.etapa_origen
    JOIN estado_expediente so ON so.codigo = r.estado_origen
    JOIN etapa_expediente ed ON ed.codigo = r.etapa_destino
    JOIN estado_expediente sd ON sd.codigo = r.estado_destino
) s
ON (
  t.id_flujo = s.id_flujo
  AND t.id_etapa_origen = s.id_etapa_origen
  AND t.id_estado_origen = s.id_estado_origen
  AND t.codigo_accion = s.codigo_accion
  AND t.id_etapa_destino = s.id_etapa_destino
  AND t.id_estado_destino = s.id_estado_destino
)
WHEN MATCHED THEN UPDATE
  SET t.nombre_accion = s.nombre_accion,
      t.requiere_comentario = s.requiere_comentario,
      t.requiere_documento = s.requiere_documento,
      t.activo = 1,
      t.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (
    id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
    codigo_accion, nombre_accion, requiere_comentario, requiere_documento, activo
  )
  VALUES (
    s.id_flujo, s.id_etapa_origen, s.id_estado_origen, s.id_etapa_destino, s.id_estado_destino,
    s.codigo_accion, s.nombre_accion, s.requiere_comentario, s.requiere_documento, 1
  );

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT se.codigo, se.nombre, ee.codigo AS etapa
  FROM estado_expediente se
  JOIN etapa_expediente ee ON ee.id_etapa = se.id_etapa
 WHERE ee.codigo = 'ANALISIS' AND UPPER(se.codigo) = 'DERIVADO';

SELECT ft.id_flujo_transicion, ft.codigo_accion, eo.codigo AS etapa_origen, so.codigo AS estado_origen,
       ed.codigo AS etapa_destino, sd.codigo AS estado_destino, ft.activo
  FROM flujo_transicion ft
  JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
  JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
  JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
  JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
 WHERE ft.codigo_accion = 'DEVOLUCION_A_ANALISIS'
 ORDER BY eo.codigo, so.codigo, sd.codigo;
