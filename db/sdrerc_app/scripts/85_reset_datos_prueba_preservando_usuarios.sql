/* ============================================================
   SCRIPT 85 - Reset de datos de prueba PRESERVANDO usuarios
   Ejecutar conectado como SDRERC_APP.

   ADVERTENCIA: este script es DESTRUCTIVO e IRREVERSIBLE para todo lo que
   NO sea catalogo/maestro ni tabla de usuarios. Vacia expedientes, personas,
   solicitudes, asignaciones, documentos, historial, notificaciones,
   auditoria, etc. y reinicia sus columnas IDENTITY a 1. NO se ejecuta
   automaticamente por el asistente: revisalo y autorizalo explicitamente
   antes de correrlo contra la base real.

   Diferencia con 62_reset_datos_prueba_y_superadmin.sql: ese script tambien
   vaciaba `usuario` y las tablas de usuario (por eso el Paso 5 de ese script
   tenia que reinsertar al superadmin a mano); este script agrega esas mismas
   tablas de usuario a la lista de PRESERVADAS, junto con los catalogos, para
   poder limpiar datos operativos de prueba sin perder cuentas, contrasenas,
   2FA (TOTP/correo) ni asignaciones de rol/equipo.

   Que SI se conserva:
   1) Catalogos/maestros (igual que el script 62): area, rol, equipo,
      entidad_externa, canal_recepcion, etapa_expediente, estado_expediente,
      tipo_movimiento, tipo_documento, tipo_acta, procedimiento_registral,
      tipo_documento_adjunto, estado_documento, tipo_observacion,
      tipo_resultado_evaluacion, tipo_resultado_ejecucion, tipo_notificacion,
      estado_notificacion, estado_cargo_acuse, tipo_resolucion,
      motivo_no_corresponde, motivo_archivo, motivo_correccion, flujo,
      flujo_transicion, flujo_transicion_rol, flujo_transicion_equipo,
      permiso, rol_permiso, plazo_configuracion, feriado_nacional,
      ubigeo_departamento, ubigeo_provincia, ubigeo_distrito,
      tipo_resultado_validacion, legacy_estado_map, legacy_catalogo_map.
   2) Tablas de usuario (nuevo respecto al script 62): usuario, usuario_rol,
      equipo_usuario, usuario_supervision, usuario_totp_backup_code,
      usuario_email_otp.

   Que SI se vacia: todo lo demas (persona, expediente y todas sus tablas
   hijas, auditoria_evento, etc.) — se calcula por exclusion de las dos
   listas anteriores, asi el script sigue siendo valido si en el futuro se
   agregan tablas transaccionales nuevas.

   Mecanismo (identico al script 62, solo cambia la lista de exclusion):
   1) Deshabilita TODAS las foreign keys del esquema (constraint_type='R').
   2) Trunca cada tabla que no este en la lista de catalogos+usuarios.
   3) Reinicia a 1 cada columna IDENTITY de esas mismas tablas.
   4) Vuelve a habilitar todas las foreign keys.

   Idempotente en el sentido de que puede volver a correrse (dejaria el
   esquema igual de vacio en lo operativo, con los mismos usuarios intactos),
   pero NO es reversible: destruye cualquier dato existente en las tablas no
   listadas como catalogo o usuario.
   ============================================================ */

SET SERVEROUTPUT ON;

/* ------------------------------------------------------------
   Paso 1: deshabilitar todas las foreign keys del esquema
   ------------------------------------------------------------ */
BEGIN
  FOR c IN (
    SELECT table_name, constraint_name
      FROM user_constraints
     WHERE constraint_type = 'R'
       AND status = 'ENABLED'
  ) LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE ' || c.table_name || ' DISABLE CONSTRAINT ' || c.constraint_name;
  END LOOP;
END;
/

/* ------------------------------------------------------------
   Paso 2: truncar todas las tablas EXCEPTO catalogos/maestros y usuarios
   ------------------------------------------------------------ */
BEGIN
  FOR t IN (
    SELECT table_name
      FROM user_tables
     WHERE table_name NOT IN (
       'AREA', 'ROL', 'EQUIPO', 'ENTIDAD_EXTERNA', 'CANAL_RECEPCION',
       'ETAPA_EXPEDIENTE', 'ESTADO_EXPEDIENTE', 'TIPO_MOVIMIENTO', 'TIPO_DOCUMENTO',
       'TIPO_ACTA', 'PROCEDIMIENTO_REGISTRAL', 'TIPO_DOCUMENTO_ADJUNTO',
       'ESTADO_DOCUMENTO', 'TIPO_OBSERVACION', 'TIPO_RESULTADO_EVALUACION',
       'TIPO_RESULTADO_EJECUCION', 'TIPO_NOTIFICACION', 'ESTADO_NOTIFICACION',
       'ESTADO_CARGO_ACUSE', 'TIPO_RESOLUCION', 'MOTIVO_NO_CORRESPONDE',
       'MOTIVO_ARCHIVO', 'MOTIVO_CORRECCION', 'FLUJO', 'FLUJO_TRANSICION',
       'FLUJO_TRANSICION_ROL', 'FLUJO_TRANSICION_EQUIPO', 'PERMISO', 'ROL_PERMISO',
       'PLAZO_CONFIGURACION', 'FERIADO_NACIONAL', 'UBIGEO_DEPARTAMENTO',
       'UBIGEO_PROVINCIA', 'UBIGEO_DISTRITO', 'TIPO_RESULTADO_VALIDACION',
       'LEGACY_ESTADO_MAP', 'LEGACY_CATALOGO_MAP',
       'USUARIO', 'USUARIO_ROL', 'EQUIPO_USUARIO', 'USUARIO_SUPERVISION',
       'USUARIO_TOTP_BACKUP_CODE', 'USUARIO_EMAIL_OTP'
     )
  ) LOOP
    EXECUTE IMMEDIATE 'TRUNCATE TABLE ' || t.table_name;
    DBMS_OUTPUT.PUT_LINE('Truncada: ' || t.table_name);
  END LOOP;
END;
/

/* ------------------------------------------------------------
   Paso 3: reiniciar a 1 cada columna IDENTITY de las tablas truncadas
   ------------------------------------------------------------ */
BEGIN
  FOR ic IN (
    SELECT table_name, column_name
      FROM user_tab_identity_cols
     WHERE table_name NOT IN (
       'AREA', 'ROL', 'EQUIPO', 'ENTIDAD_EXTERNA', 'CANAL_RECEPCION',
       'ETAPA_EXPEDIENTE', 'ESTADO_EXPEDIENTE', 'TIPO_MOVIMIENTO', 'TIPO_DOCUMENTO',
       'TIPO_ACTA', 'PROCEDIMIENTO_REGISTRAL', 'TIPO_DOCUMENTO_ADJUNTO',
       'ESTADO_DOCUMENTO', 'TIPO_OBSERVACION', 'TIPO_RESULTADO_EVALUACION',
       'TIPO_RESULTADO_EJECUCION', 'TIPO_NOTIFICACION', 'ESTADO_NOTIFICACION',
       'ESTADO_CARGO_ACUSE', 'TIPO_RESOLUCION', 'MOTIVO_NO_CORRESPONDE',
       'MOTIVO_ARCHIVO', 'MOTIVO_CORRECCION', 'FLUJO', 'FLUJO_TRANSICION',
       'FLUJO_TRANSICION_ROL', 'FLUJO_TRANSICION_EQUIPO', 'PERMISO', 'ROL_PERMISO',
       'PLAZO_CONFIGURACION', 'FERIADO_NACIONAL', 'UBIGEO_DEPARTAMENTO',
       'UBIGEO_PROVINCIA', 'UBIGEO_DISTRITO', 'TIPO_RESULTADO_VALIDACION',
       'LEGACY_ESTADO_MAP', 'LEGACY_CATALOGO_MAP',
       'USUARIO', 'USUARIO_ROL', 'EQUIPO_USUARIO', 'USUARIO_SUPERVISION',
       'USUARIO_TOTP_BACKUP_CODE', 'USUARIO_EMAIL_OTP'
     )
  ) LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE ' || ic.table_name || ' MODIFY ' || ic.column_name
        || ' GENERATED BY DEFAULT AS IDENTITY (START WITH 1)';
    DBMS_OUTPUT.PUT_LINE('IDENTITY reiniciada a 1: ' || ic.table_name || '.' || ic.column_name);
  END LOOP;
END;
/

/* ------------------------------------------------------------
   Paso 4: volver a habilitar todas las foreign keys
   ------------------------------------------------------------ */
BEGIN
  FOR c IN (
    SELECT table_name, constraint_name
      FROM user_constraints
     WHERE constraint_type = 'R'
       AND status = 'DISABLED'
  ) LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE ' || c.table_name || ' ENABLE CONSTRAINT ' || c.constraint_name;
  END LOOP;
END;
/

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT COUNT(*) AS total_usuarios FROM usuario;
SELECT COUNT(*) AS total_expedientes FROM expediente;

SELECT u.username, r.codigo AS rol
  FROM usuario_rol ur
  JOIN usuario u ON u.id_usuario = ur.id_usuario
  JOIN rol r ON r.id_rol = ur.id_rol
 WHERE ur.activo = 1
 ORDER BY u.username;
