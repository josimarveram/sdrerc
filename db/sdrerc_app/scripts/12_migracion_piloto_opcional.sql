/* ============================================================
   SCRIPT 12 - Migracion piloto opcional
   Ejecutar solo despues de validar estructura, maestros y mapeos.
   Requiere sinonimos o acceso de lectura a tablas legacy.
   Ajustar nombres origen segun conexion DBeaver.
   ============================================================ */

/* Usuarios piloto desde legacy, si existe acceso a APP_USERS.
INSERT INTO usuario (username, nombre_completo, estado, id_legacy, activo)
SELECT username,
       full_name,
       status,
       user_id,
       CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END
FROM app_users;
*/

/* Expedientes piloto desde legacy.
INSERT INTO expediente (
  numero_expediente,
  numero_tramite_documentario,
  id_etapa_actual,
  id_estado_actual,
  fecha_registro,
  fecha_ultimo_movimiento,
  id_legacy,
  creado_por
)
SELECT e.num_expediente,
       e.numero_tramite_documento,
       et.id_etapa,
       es.id_estado,
       CAST(e.fecha_registra AS TIMESTAMP),
       CAST(NVL(e.fecha_modifica, e.fecha_registra) AS TIMESTAMP),
       e.id_expediente,
       e.id_usuario_crea
FROM expediente e
JOIN legacy_estado_map lem ON lem.id_legacy = e.estado
JOIN etapa_expediente et ON et.codigo = lem.codigo_etapa_nueva
JOIN estado_expediente es ON es.codigo = lem.codigo_estado_nuevo;
*/

/* Solicitudes piloto.
INSERT INTO expediente_solicitud (
  id_expediente,
  numero_tramite_documentario,
  fecha_recepcion,
  asunto,
  es_tramite_virtual,
  correo_electronico,
  creado_por
)
SELECT ne.id_expediente,
       le.numero_tramite_documento,
       le.fecha_solicitud,
       le.hoja_envio_expediente,
       CASE WHEN UPPER(NVL(le.canal_recepcion, '')) LIKE '%VIRTUAL%' THEN 1 ELSE 0 END,
       le.correo_electronico,
       le.id_usuario_crea
FROM expediente le
JOIN sdrerc_app.expediente ne ON ne.id_legacy = le.id_expediente;
*/

/* Personas piloto: remitente y titulares deben revisarse antes de cargar.
   TODO: normalizar nombres completos y documentos antes de insertar.
*/

/* Asignaciones piloto.
   TODO: mapear APP_USERS.ID_TECNICO hacia USUARIO.ID_LEGACY o tabla puente.
*/

/* Validacion posterior recomendada:
SELECT COUNT(*) FROM expediente;
SELECT codigo_etapa_nueva, codigo_estado_nuevo, COUNT(*)
FROM legacy_estado_map
GROUP BY codigo_etapa_nueva, codigo_estado_nuevo;
*/

