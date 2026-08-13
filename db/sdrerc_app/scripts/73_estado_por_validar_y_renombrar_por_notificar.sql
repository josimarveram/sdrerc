/* ============================================================
   SCRIPT 73 - Estado POR_VALIDAR en Notificacion, renombrar
   EN_NOTIFICACION a "Por notificar", y correccion puntual de datos
   Ejecutar conectado como SDRERC_APP.

   Contexto: pedido explicito del usuario para la Bandeja Asignacion
   de Notificacion:
   - Bandeja Asignacion: solo expedientes en `POR_ASIGNAR` (ya
     implementado en el script 72); documentos INTERMEDIO+Emitido se
     derivan al equipo EQ_NOTIFICACION, documentos FINAL+En despacho
     se derivan al equipo EQ_VALIDACION.
   - Bandeja Validacion: solo expedientes en `POR_VALIDAR` (estado
     nuevo, sembrado por este script); documentos FINAL+En despacho;
     al aprobar, el documento pasa a Validado y el expediente vuelve a
     `POR_ASIGNAR` (para que el coordinador lo derive de nuevo, esta
     vez a Notificacion).
   - Bandeja Notificacion: solo expedientes en el estado que antes se
     llamaba `EN_NOTIFICACION`; documentos INTERMEDIO o FINAL ya
     Emitido.

   Decision de diseno: el estado que aparece en Bandeja Notificacion
   NO es un codigo nuevo. Se reutiliza el `EN_NOTIFICACION` que ya
   existia y que ya esta conectado a todo el flujo de intentos, cargo
   y confirmacion de notificacion (NOTIFICACION_VIRTUAL,
   NOTIFICACION_PRESENCIAL_1/2, RECEPCION_CARGO_ACUSE,
   CONFIRMACION_NOTIFICACION -> NOTIFICADO), que exige literalmente
   ese codigo como origen (ver NotificacionExpedienteDAO,
   estadoOrigenNotificacion). Crear un codigo nuevo tipo
   `POR_NOTIFICAR` habria obligado a reescribir esa cadena completa de
   transiciones ya probada. En su lugar, solo se renombra la etiqueta
   visible de `EN_NOTIFICACION` de "En notificacion" a "Por notificar"
   (mismo codigo, cero riesgo sobre flujo_transicion o el codigo Java
   existente). El estado `NOTIFICADO` (etapa NOTIFICACION) tambien ya
   existia y no se toca: es el que se alcanza al confirmar notificacion.

   Que hace:
   1) Siembra el estado POR_VALIDAR ("Por validar") en etapa
      NOTIFICACION.
   2) Renombra el nombre de EN_NOTIFICACION de "En notificacion" a
      "Por notificar" (mismo codigo, no se toca flujo_transicion).
   3) Corrige puntualmente documentos ya asignados a un validador o
      notificador (EXPEDIENTE_DOCUMENTO_ANALIZADO.id_usuario_notificacion
      no nulo) cuyo expediente quedo en POR_ASIGNAR porque la asignacion
      se hizo antes de esta regla: si el equipo de notificacion del
      documento es EQ_VALIDACION, el expediente pasa a POR_VALIDAR; si
      es EQ_NOTIFICACION, pasa a EN_NOTIFICACION ("Por notificar").

   Idempotente: los INSERT verifican NOT EXISTS por codigo; el UPDATE
   de nombre y la correccion de datos son condicionales, se pueden
   re-ejecutar sin efecto adicional.
   ============================================================ */

-- 1) Estado nuevo POR_VALIDAR en etapa NOTIFICACION
INSERT INTO estado_expediente (id_etapa, codigo, nombre)
SELECT et.id_etapa, 'POR_VALIDAR', 'Por validar'
  FROM etapa_expediente et
 WHERE et.codigo = 'NOTIFICACION'
   AND NOT EXISTS (SELECT 1 FROM estado_expediente WHERE UPPER(codigo) = 'POR_VALIDAR');

COMMIT;

-- 2) Renombrar EN_NOTIFICACION -> "Por notificar" (mismo codigo)
UPDATE estado_expediente
   SET nombre = 'Por notificar'
 WHERE UPPER(codigo) = 'EN_NOTIFICACION'
   AND nombre <> 'Por notificar';

COMMIT;

-- 3) Corregir documentos ya asignados cuyo expediente quedo en POR_ASIGNAR
UPDATE expediente e
   SET e.id_estado_actual = (
         SELECT est_nuevo.id_estado FROM estado_expediente est_nuevo
          WHERE est_nuevo.codigo = (
                SELECT CASE WHEN UPPER(eq.codigo) = 'EQ_VALIDACION' THEN 'POR_VALIDAR' ELSE 'EN_NOTIFICACION' END
                  FROM expediente_documento_analizado da2
                  JOIN equipo eq ON eq.id_equipo = da2.id_equipo_notificacion
                 WHERE da2.id_expediente = e.id_expediente
                   AND da2.activo = 1
                   AND da2.id_usuario_notificacion IS NOT NULL
                   AND ROWNUM = 1)),
       e.modificado_en = SYSTIMESTAMP
 WHERE e.activo = 1
   AND e.id_etapa_actual = (SELECT id_etapa FROM etapa_expediente WHERE codigo = 'NOTIFICACION')
   AND e.id_estado_actual = (SELECT id_estado FROM estado_expediente WHERE codigo = 'POR_ASIGNAR')
   AND EXISTS (
         SELECT 1 FROM expediente_documento_analizado da
          JOIN equipo eq ON eq.id_equipo = da.id_equipo_notificacion
         WHERE da.id_expediente = e.id_expediente
           AND da.activo = 1
           AND da.id_usuario_notificacion IS NOT NULL
           AND UPPER(eq.codigo) IN ('EQ_VALIDACION', 'EQ_NOTIFICACION'));

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT codigo, nombre FROM estado_expediente WHERE UPPER(codigo) IN ('POR_ASIGNAR', 'POR_VALIDAR', 'EN_NOTIFICACION', 'NOTIFICADO') ORDER BY codigo;

SELECT e.numero_expediente, est.codigo AS estado_expediente
  FROM expediente e
  JOIN estado_expediente est ON est.id_estado = e.id_estado_actual
 WHERE UPPER(est.codigo) IN ('POR_VALIDAR', 'EN_NOTIFICACION')
 ORDER BY est.codigo, e.numero_expediente;
