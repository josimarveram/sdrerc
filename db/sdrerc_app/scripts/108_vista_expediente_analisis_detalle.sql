-- =====================================================================
-- Script: 108_vista_expediente_analisis_detalle.sql
-- Proposito: Formalizar en una vista el SELECT rico que necesita la
--            Bandeja Analisis de V3 (v3/sdrerc-v3-backend,
--            com.sdrerc.v3.infrastructure.dao.AnalisisExpedienteDAO),
--            mismo patron ya usado por:
--              - 10_vistas_bandejas_consola.sql -> vw_expediente_bandeja (Registro)
--              - 99_vista_expediente_asignacion_detalle.sql -> vw_expediente_asignacion_detalle (Asignacion)
--            Fuente de verdad del SELECT: com.sdrerc.infrastructure.sdrercapp.dao.AnalisisExpedienteDAO
--            (V2, escritorio, metodo buscarExpedientes) - NO SE TOCA, sigue con su propio SQL
--            inline, sin cambios. Esta vista es una version persistida y simplificada de ese
--            mismo SELECT, acotada a las columnas que V3 necesita para la bandeja + panel de
--            datos (13 columnas del TableModel real: Dias/Expediente/N SGD/Fecha solicitud/
--            Procedimiento/Tipo acta/Nro acta/Titular/Abogado designado/Estado/Asociados, mas
--            id/etapa para logica de negocio) - no se portaron las columnas de solicitante
--            (correo/telefono/direccion/ubigeo) porque V3 reutiliza el mismo "Panel de datos"
--            generico de Registro (GET /api/registro/bandeja/{id}/datos) para esa informacion,
--            no un SELECT propio.
--
-- Alcance de la bandeja (idéntico al de V2, verificado contra el codigo real): expedientes en
-- etapa ASIGNACION/estado ASIGNADO (pendientes de "Recibir") UNION expedientes ya en etapa
-- ANALISIS (cualquier estado) - la etapa ANALISIS incluye RECIBIDO_POR_ABOGADO, OBSERVADO,
-- DERIVADO, SUBSANADO, ATENDIDO, NO_CORRESPONDE, EN_ABANDONO, OBSERVACION_ADMINISTRATIVA. El
-- filtro de visibilidad por asignacion (VisibilidadBandejaSql, ya existe en V3) se sigue
-- aplicando en el DAO sobre esta vista, no aqui (mismo patron que vw_expediente_asignacion_detalle:
-- solo lo CONSTANTE va en la vista, lo dinamico por request queda en el DAO).
--
-- La misma regla de "expediente ya asociado como duplicado no aparece como fila principal" que
-- usan vw_expediente_bandeja/vw_expediente_asignacion_detalle se replica aqui (NOT EXISTS contra
-- expediente_relacion con id_expediente_relacionado = e.id_expediente).
--
-- IMPORTANTE:
-- - Este script NO ha sido ejecutado contra SDRERC_APP. Requiere autorizacion y ejecucion manual
--   separada, conectado como SDRERC_APP.
-- - No agrega columnas ni tablas nuevas, no modifica datos.
-- - Idempotente: CREATE OR REPLACE VIEW.
-- - V2 (escritorio) no se toca ni depende de esta vista.
-- =====================================================================

CREATE OR REPLACE VIEW vw_expediente_analisis_detalle AS
SELECT DISTINCT
    e.id_expediente,
    e.numero_expediente,
    esol.numero_expediente_sgd,
    esol.asunto AS procedimiento,
    ta.nombre AS tipo_acta,
    ea.numero_acta,
    TRIM(NVL(p.razon_social, TRIM(NVL(p.nombres, '') || ' ' || NVL(p.apellidos, '')))) AS titular,
    ur.nombre_completo AS responsable,
    e.id_usuario_responsable_actual AS id_responsable,
    e.id_equipo_responsable_actual AS id_equipo_responsable,
    esol.fecha_recepcion,
    e.fecha_vencimiento,
    e.fecha_registro,
    et.codigo AS etapa_codigo,
    est.codigo AS estado_codigo,
    NVL(esol.grupo_familiar, 0) AS grupo_familiar,
    UPPER(NVL(TRIM(NVL(p.razon_social, TRIM(NVL(p.nombres, '') || ' ' || NVL(p.apellidos, '')))), 'ZZZ')) AS orden_titular,
    (SELECT COUNT(*) FROM expediente_relacion rc
      WHERE rc.activo = 1
        AND rc.id_expediente_principal = e.id_expediente
        AND UPPER(rc.tipo_relacion) IN ('DOCUMENTO_DUPLICADO_ASOCIADO', 'MISMA_ACTA_TITULAR')) AS asociados_confirmados,
    (SELECT COUNT(*) FROM expediente_documento_analizado da
      WHERE da.id_expediente = e.id_expediente AND da.activo = 1) AS documentos_analizados
FROM expediente e
JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual
JOIN estado_expediente est ON est.id_estado = e.id_estado_actual
LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1
LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1
LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta
LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR'
LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1
LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual
WHERE e.activo = 1
  AND (
    (et.codigo = 'ASIGNACION' AND est.codigo = 'ASIGNADO')
    OR et.codigo = 'ANALISIS'
  )
  AND NOT EXISTS (
    SELECT 1 FROM expediente_relacion r
     WHERE r.activo = 1
       AND r.id_expediente_relacionado = e.id_expediente
       AND UPPER(r.tipo_relacion) IN ('DOCUMENTO_DUPLICADO_ASOCIADO', 'MISMA_ACTA_TITULAR')
  );
