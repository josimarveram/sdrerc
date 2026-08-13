-- =====================================================================
-- Script: 99_vista_expediente_asignacion_detalle.sql
-- Proposito: Formalizar en una vista el SELECT rico que necesita la
--            Bandeja Asignacion (columnas + alertas + hoja de envio +
--            abogado/equipo responsable), hoy duplicado como SQL inline
--            en 2 lugares:
--              - com.sdrerc.infrastructure.sdrercapp.dao.AsignacionExpedienteDAO
--                (V2, escritorio, metodo buscarExpedientes) - NO SE TOCA,
--                sigue con su propio SQL inline, sin cambios.
--              - com.sdrerc.v3.infrastructure.dao.AsignacionExpedienteDAO
--                (V3, backend web, metodo buscarExpedientes) - se
--                actualiza en este mismo cambio para consultar esta
--                vista en lugar de repetir el join completo en Java.
--
-- Por que una vista nueva y no reutilizar vw_expediente_bandeja /
-- vw_bandeja_asignacion (script 10_vistas_bandejas_consola.sql):
--   vw_expediente_bandeja es deliberadamente generica (columnas basicas
--   compartidas por TODAS las bandejas: Registro, Analisis, Verificacion,
--   Ejecucion, Notificacion, etc.). La Bandeja Asignacion necesita columnas
--   propias que no tiene sentido agregar ahi porque inflarian una vista
--   compartida por modulos que no las usan: numero_expediente_sgd, canal,
--   procedimiento, tipo_acta/numero_acta, titular y solicitante con datos
--   de contacto, potencial_duplicado, grupo_familiar y su alerta, hoja de
--   envio de la asignacion activa, y el abogado_asignado con fallback a
--   expediente_asignacion (distinto del generico responsable_actual). El
--   propio V2 ya tomo esta misma decision: su AsignacionExpedienteDAO
--   nunca reutilizo vw_expediente_bandeja, arma su propio SQL. Esta vista
--   es exactamente ese mismo SQL, solo que persistido con nombre en vez
--   de repetido como texto en cada DAO.
--
-- Diferencias respecto al SQL inline original (sin cambio de comportamiento):
--   - Los 2 codigos de tipo_relacion usados para "duplicado asociado"
--     (DOCUMENTO_DUPLICADO_ASOCIADO, MISMA_ACTA_TITULAR) se pasaban como
--     bind params en el DAO pero SIEMPRE eran esas 2 constantes; aqui
--     quedan literales porque una vista no acepta parametros.
--   - El filtro "NOT EXISTS duplicado asociado" (para que un expediente
--     ya asociado como duplicado no aparezca como fila principal) queda
--     fijo en la vista en vez de repetirse en cada DAO que la consuma:
--     es una regla de negocio constante, no un filtro dinamico de busqueda.
--   - Los filtros SI dinamicos por request (texto libre, estado, rango de
--     fechas, limite/ROWNUM, orden) NO estan en la vista: se siguen
--     aplicando en el DAO con "SELECT * FROM vw_expediente_asignacion_detalle
--     WHERE ...", mismo patron que ya usa
--     com.sdrerc.v3.infrastructure.dao.ExpedienteBandejaDAO sobre
--     vw_expediente_bandeja.
--
-- IMPORTANTE:
-- - Este script NO ha sido ejecutado contra SDRERC_APP. Requiere
--   autorizacion y ejecucion manual separada, conectado como SDRERC_APP.
-- - No agrega columnas ni tablas nuevas, no modifica datos.
-- - Idempotente: CREATE OR REPLACE VIEW.
-- - V2 (escritorio) no se toca ni depende de esta vista.
-- =====================================================================

CREATE OR REPLACE VIEW vw_expediente_asignacion_detalle AS
SELECT DISTINCT
    e.id_expediente,
    e.numero_expediente,
    esol.numero_expediente_sgd,
    e.numero_tramite_documentario,
    cr.nombre AS canal_ingreso,
    (SELECT MAX(axa.numero_hoja_envio) KEEP (DENSE_RANK LAST ORDER BY axa.fecha_asignacion, axa.id_expediente_asignacion)
       FROM expediente_asignacion axa
      WHERE axa.id_expediente = e.id_expediente AND axa.activa = 1 AND axa.activo = 1) AS numero_hoja_envio_asignacion,
    (SELECT MIN(ed.numero_documento) KEEP (DENSE_RANK FIRST ORDER BY ed.id_expediente_documento)
       FROM expediente_documento ed
      WHERE ed.id_expediente = e.id_expediente AND ed.activo = 1
        AND TRIM(ed.numero_documento) IS NOT NULL) AS numero_documento,
    (SELECT MIN(ed.nombre_documento) KEEP (DENSE_RANK FIRST ORDER BY ed.id_expediente_documento)
       FROM expediente_documento ed
      WHERE ed.id_expediente = e.id_expediente AND ed.activo = 1
        AND TRIM(ed.nombre_documento) IS NOT NULL) AS tipo_documento,
    esol.asunto AS procedimiento,
    ta.nombre AS tipo_acta,
    ea.numero_acta,
    TRIM(NVL(p.razon_social, TRIM(NVL(p.nombres, '') || ' ' || NVL(p.apellidos, '')))) AS titular,
    TRIM(NVL(ps.razon_social, TRIM(NVL(ps.nombres, '') || ' ' || NVL(ps.apellidos, '')))) AS solicitante,
    ps.tipo_documento AS solicitante_tipo_documento,
    ps.numero_documento AS solicitante_numero_documento,
    ps.correo_electronico AS solicitante_correo,
    ps.telefono AS solicitante_telefono,
    ps.direccion AS solicitante_direccion,
    ps.departamento AS solicitante_departamento,
    ps.provincia AS solicitante_provincia,
    ps.distrito AS solicitante_distrito,
    p.tipo_documento AS tipo_documento_titular,
    p.numero_documento AS numero_documento_titular,
    eqr.nombre AS equipo_asignado,
    e.id_equipo_responsable_actual AS id_equipo_responsable,
    NVL(ur.nombre_completo, (SELECT MAX(ua.nombre_completo) FROM expediente_asignacion axa
        JOIN usuario ua ON ua.id_usuario = axa.id_usuario_asignado
       WHERE axa.id_expediente = e.id_expediente AND axa.activa = 1 AND axa.activo = 1)) AS abogado_asignado,
    e.id_usuario_responsable_actual AS id_abogado_responsable,
    esol.fecha_recepcion,
    e.fecha_vencimiento,
    esol.potencial_duplicado,
    esol.observacion AS observacion_solicitud,
    NVL(esol.grupo_familiar, 0) AS grupo_familiar,
    esol.criterio_grupo_familiar,
    esol.observacion_grupo_familiar,
    (CASE WHEN EXISTS (
        SELECT 1 FROM expediente_alerta eal
         WHERE eal.id_expediente = e.id_expediente
           AND eal.activo = 1 AND eal.atendida = 0
           AND UPPER(TRIM(eal.mensaje)) = 'POSIBLE GRUPO FAMILIAR'
     ) THEN 1 ELSE 0 END) AS alerta_grupo_familiar_activa,
    e.fecha_registro,
    et.codigo AS etapa_codigo,
    est.codigo AS estado_codigo,
    UPPER(NVL(TRIM(NVL(p.razon_social, TRIM(NVL(p.nombres, '') || ' ' || NVL(p.apellidos, '')))), 'ZZZ')) AS orden_titular,
    (SELECT COUNT(*) FROM expediente_asignacion ax
      WHERE ax.id_expediente = e.id_expediente AND ax.activa = 1 AND ax.activo = 1) AS asignacion_activa,
    (SELECT COUNT(*) FROM expediente_relacion rc
      WHERE rc.activo = 1
        AND rc.id_expediente_principal = e.id_expediente
        AND UPPER(rc.tipo_relacion) IN ('DOCUMENTO_DUPLICADO_ASOCIADO', 'MISMA_ACTA_TITULAR')) AS asociados_confirmados
FROM expediente e
JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual
JOIN estado_expediente est ON est.id_estado = e.id_estado_actual
LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1
LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1
LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta
LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR'
LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1
LEFT JOIN persona ps ON ps.id_persona = esol.id_persona_solicitante AND ps.activo = 1
LEFT JOIN canal_recepcion cr ON cr.id_canal_recepcion = esol.id_canal_recepcion
LEFT JOIN equipo eqr ON eqr.id_equipo = e.id_equipo_responsable_actual
LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual
WHERE e.activo = 1
  AND NOT EXISTS (
    SELECT 1 FROM expediente_relacion r
     WHERE r.activo = 1
       AND r.id_expediente_relacionado = e.id_expediente
       AND UPPER(r.tipo_relacion) IN ('DOCUMENTO_DUPLICADO_ASOCIADO', 'MISMA_ACTA_TITULAR')
  );
