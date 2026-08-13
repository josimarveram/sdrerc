/* ============================================================
   SCRIPT 72 - Estado POR_ASIGNAR en etapa Notificacion + transicion
   automatica Ejecucion/Verificacion -> Notificacion
   Ejecutar conectado como SDRERC_APP.

   Contexto: al guardar Ejecucion (carta FINAL en despacho) o al
   aprobar Verificacion con destino (carta INTERMEDIA emitida), el
   expediente debe quedar de inmediato en NOTIFICACION en un estado
   que distinga "recien llegado, sin asignar todavia" del resto de
   estados de Notificacion (EN_NOTIFICACION, CARGO_PENDIENTE, etc.,
   que representan el ciclo de notificacion propiamente dicho una vez
   que el documento ya fue asignado/validado/emitido).

   Que hace:
   1) Siembra el estado POR_ASIGNAR ("Por asignar") en la etapa
      NOTIFICACION.
   2) Retarget de la transicion DERIVACION_A_NOTIFICACION que usa
      Ejecucion (EJECUCION/EJECUTADO -> NOTIFICACION/*): su destino
      pasa de EN_NOTIFICACION a POR_ASIGNAR. Es la unica fila de
      FLUJO_TRANSICION con ese origen exacto (EJECUCION/EJECUTADO),
      por lo que no afecta las otras dos filas de DERIVACION_A_NOTIFICACION
      que ya existian (origen ANALISIS/EN_ABANDONO y
      ANALISIS/OBSERVACION_ADMINISTRATIVA), que no se tocan.
   3) Agrega una fila nueva de FLUJO_TRANSICION: DERIVACION_A_NOTIFICACION
      desde VERIFICACION/VERIFICADO hacia NOTIFICACION/POR_ASIGNAR, para
      que VerificacionExpedienteDAO.aprobarVerificacionConDestino pueda
      encadenar automaticamente ese segundo salto (documentos INTERMEDIO)
      igual que Ejecucion ya hace con las cartas FINALES.

   No crea estados ni transiciones para el paso siguiente (de
   POR_ASIGNAR hacia EN_NOTIFICACION); eso queda pendiente de definir
   como parte del ciclo de asignacion/firma en Notificacion, no se
   asume aqui.

   4) Correccion puntual de datos: expedientes que ya estaban en
      NOTIFICACION/EN_NOTIFICACION (llegaron ahi ANTES de este script,
      cuando el destino todavia era EN_NOTIFICACION) y que tienen un
      documento sin asignar que cumple la condicion de la Bandeja
      Asignacion (INTERMEDIO/Emitido o FINAL/En despacho-Validado): se
      retargean a POR_ASIGNAR para que sigan siendo visibles en esa
      bandeja con la condicion nueva. No toca expedientes cuyo
      documento ya fue asignado (id_usuario_notificacion no nulo) ni
      ningun otro estado de Notificacion.

   Idempotente: los INSERT verifican NOT EXISTS por codigo; el UPDATE
   es condicional por codigo_accion + etapa/estado origen exactos o por
   coincidencia exacta de estado+documento pendiente, se puede
   re-ejecutar sin efecto adicional.
   ============================================================ */

-- 1) Estado nuevo POR_ASIGNAR en etapa NOTIFICACION
INSERT INTO estado_expediente (id_etapa, codigo, nombre)
SELECT et.id_etapa, 'POR_ASIGNAR', 'Por asignar'
  FROM etapa_expediente et
 WHERE et.codigo = 'NOTIFICACION'
   AND NOT EXISTS (SELECT 1 FROM estado_expediente WHERE UPPER(codigo) = 'POR_ASIGNAR');

COMMIT;

-- 2) Retarget de la transicion de Ejecucion: EJECUCION/EJECUTADO -> NOTIFICACION/POR_ASIGNAR
UPDATE flujo_transicion ft
   SET ft.id_estado_destino = (SELECT id_estado FROM estado_expediente WHERE codigo = 'POR_ASIGNAR'),
       ft.modificado_en = SYSTIMESTAMP
 WHERE ft.codigo_accion = 'DERIVACION_A_NOTIFICACION'
   AND ft.id_etapa_origen = (SELECT id_etapa FROM etapa_expediente WHERE codigo = 'EJECUCION')
   AND ft.id_estado_origen = (SELECT id_estado FROM estado_expediente WHERE codigo = 'EJECUTADO')
   AND ft.id_etapa_destino = (SELECT id_etapa FROM etapa_expediente WHERE codigo = 'NOTIFICACION')
   AND ft.id_estado_destino <> (SELECT id_estado FROM estado_expediente WHERE codigo = 'POR_ASIGNAR');

COMMIT;

-- 3) Transicion nueva para Verificacion: VERIFICACION/VERIFICADO -> NOTIFICACION/POR_ASIGNAR
INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DERIVACION_A_NOTIFICACION', 'Derivar documento intermedio emitido a notificacion', 0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'VERIFICADO'
  JOIN etapa_expediente ed ON ed.codigo = 'NOTIFICACION'
  JOIN estado_expediente sd ON sd.codigo = 'POR_ASIGNAR'
 WHERE f.codigo = 'SDRERC_TO_BE'
   AND NOT EXISTS (
         SELECT 1 FROM flujo_transicion ft2
          WHERE ft2.codigo_accion = 'DERIVACION_A_NOTIFICACION'
            AND ft2.id_etapa_origen = eo.id_etapa
            AND ft2.id_estado_origen = so.id_estado
            AND ft2.id_etapa_destino = ed.id_etapa
            AND ft2.id_estado_destino = sd.id_estado);

COMMIT;

-- 4) Corregir expedientes ya varados en EN_NOTIFICACION con documento pendiente de asignar
UPDATE expediente e
   SET e.id_estado_actual = (SELECT id_estado FROM estado_expediente WHERE codigo = 'POR_ASIGNAR'),
       e.modificado_en = SYSTIMESTAMP
 WHERE e.activo = 1
   AND e.id_etapa_actual = (SELECT id_etapa FROM etapa_expediente WHERE codigo = 'NOTIFICACION')
   AND e.id_estado_actual = (SELECT id_estado FROM estado_expediente WHERE codigo = 'EN_NOTIFICACION')
   AND EXISTS (
         SELECT 1
           FROM expediente_documento_analizado da
           JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto
           JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento
          WHERE da.id_expediente = e.id_expediente
            AND da.activo = 1
            AND da.id_usuario_notificacion IS NULL
            AND ((UPPER(NVL(tda.clasificacion, '')) = 'INTERMEDIO' AND UPPER(NVL(ed.codigo, '')) = 'EMITIDO')
              OR (UPPER(NVL(tda.clasificacion, '')) = 'FINAL' AND UPPER(NVL(ed.codigo, '')) IN ('EN_DESPACHO', 'VALIDADO'))));

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT codigo, nombre FROM estado_expediente WHERE UPPER(codigo) = 'POR_ASIGNAR';

SELECT ft.id_flujo_transicion, ft.codigo_accion, eo.codigo AS etapa_origen, so.codigo AS estado_origen,
       ed.codigo AS etapa_destino, sd.codigo AS estado_destino, ft.activo
  FROM flujo_transicion ft
  JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
  JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
  JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
  JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
 WHERE ft.codigo_accion = 'DERIVACION_A_NOTIFICACION'
 ORDER BY eo.codigo, so.codigo;
