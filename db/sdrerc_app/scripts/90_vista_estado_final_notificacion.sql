-- =====================================================================
-- Script: 90_vista_estado_final_notificacion.sql
-- Proposito: Formalizar en SQL, para reporting/consultas futuras, la
--            derivacion del "Estado Final" del documento en la Bandeja
--            Notificacion (4 estados: POR NOTIFICAR / PENDIENTE / ATENDIDO
--            / POR PUBLICAR), tal como quedo especificado por el usuario
--            en la reestructuracion de la bandeja de intentos de
--            notificacion al ciudadano.
--
-- IMPORTANTE:
-- - Este script NO ha sido ejecutado contra SDRERC_APP. Se prepara segun
--   autorizacion explicita del usuario, pero su ejecucion requiere una
--   autorizacion separada.
-- - NO agrega columnas ni tablas nuevas. La logica del Estado Final ya
--   fue implementada directamente en Java (DocumentoAnalisisDAO,
--   metodo listarDocumentosNotificacionPareado/PorEstados, constante
--   ESTADO_FINAL_NOTIFICACION_SQL) porque toda la informacion necesaria
--   ya existe en EXPEDIENTE_NOTIFICACION + ESTADO_NOTIFICACION:
--     * POR_NOTIFICAR: el documento no tiene ninguna fila activa en
--       EXPEDIENTE_NOTIFICACION.
--     * ATENDIDO: existe algun intento con ESTADO_NOTIFICACION.codigo =
--       'EXITOSA' (equivale a "ubicado").
--     * POR_PUBLICAR: el intento 1 y el intento 2 tienen ambos codigo
--       'FALLIDA' (equivale a "Estado ENVIADO + Estado Notificacion NO
--       UBICADO" en ambos intentos, segun el Excel de diseno de bandejas).
--     * PENDIENTE: cualquier otro caso con intentos registrados.
-- - "Codigo Notificacion" (modalidad virtual) y "Usuario Notificacion"
--   (modalidad presencial) tampoco requirieron columna nueva: ambos se
--   persisten en EXPEDIENTE_NOTIFICACION.CODIGO_NOTIFICACION (texto
--   digitado por el usuario) y, al confirmar la recepcion/acuse, tambien
--   en EXPEDIENTE_CARGO_ACUSE.RECIBIDO_POR (con FECHA_RECEPCION =
--   "Fecha Acuse" que ya usa la Bandeja Cartas de respuesta).
--
-- Esta vista es un espejo de solo lectura de esa misma logica, pensada
-- para reportes/consultas administrativas que necesiten filtrar o
-- agrupar documentos por Estado Final sin pasar por la aplicacion Java.
-- No se usa desde la aplicacion (la aplicacion ya calcula el campo en la
-- consulta principal). Idempotente: CREATE OR REPLACE VIEW.
-- =====================================================================

CREATE OR REPLACE VIEW vw_documento_estado_final_notif AS
SELECT
    da.id_documento_analizado,
    da.id_expediente,
    e.numero_expediente,
    tda.clasificacion,
    tda.nombre AS tipo_documento,
    da.numero_documento,
    da.fecha_documento,
    (SELECT COUNT(*) FROM expediente_notificacion en1
      WHERE en1.id_documento_analizado = da.id_documento_analizado AND en1.activo = 1) AS total_intentos,
    CASE
        WHEN NOT EXISTS (
            SELECT 1 FROM expediente_notificacion en0
            WHERE en0.id_documento_analizado = da.id_documento_analizado AND en0.activo = 1)
            THEN 'POR_NOTIFICAR'
        WHEN EXISTS (
            SELECT 1 FROM expediente_notificacion en2
            JOIN estado_notificacion est2 ON est2.id_estado_notificacion = en2.id_estado_notificacion
            WHERE en2.id_documento_analizado = da.id_documento_analizado AND en2.activo = 1
              AND est2.codigo = 'EXITOSA')
            THEN 'ATENDIDO'
        WHEN EXISTS (
            SELECT 1 FROM expediente_notificacion en3
            JOIN estado_notificacion est3 ON est3.id_estado_notificacion = en3.id_estado_notificacion
            WHERE en3.id_documento_analizado = da.id_documento_analizado AND en3.activo = 1
              AND en3.numero_intento = 1 AND est3.codigo = 'FALLIDA')
         AND EXISTS (
            SELECT 1 FROM expediente_notificacion en4
            JOIN estado_notificacion est4 ON est4.id_estado_notificacion = en4.id_estado_notificacion
            WHERE en4.id_documento_analizado = da.id_documento_analizado AND en4.activo = 1
              AND en4.numero_intento = 2 AND est4.codigo = 'FALLIDA')
            THEN 'POR_PUBLICAR'
        ELSE 'PENDIENTE'
    END AS estado_final_notificacion_codigo
FROM expediente_documento_analizado da
JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1
LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto
WHERE da.activo = 1
  AND UPPER(NVL(tda.clasificacion, '')) IN ('INTERMEDIO', 'FINAL');
