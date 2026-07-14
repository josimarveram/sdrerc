/* ============================================================
   SCRIPT 62 - Reset completo de datos de prueba + usuario superadmin
   Ejecutar conectado como SDRERC_APP.

   ADVERTENCIA: este script es DESTRUCTIVO e IRREVERSIBLE. Vacía TODAS las
   tablas transaccionales/operativas del esquema (usuarios, expedientes,
   solicitudes, personas, asignaciones, documentos, historial, notificaciones,
   auditoría, etc.) y reinicia sus columnas IDENTITY a 1. NO se ejecuta
   automáticamente por el asistente: revísalo, ajusta lo que corresponda
   (username/correo del superadmin) y ejecútalo tú mismo (o autorízalo
   explícitamente) contra la base real.

   Qué SÍ se conserva (catálogos/maestros, no son "datos de prueba"):
   area, rol, equipo, entidad_externa, canal_recepcion, etapa_expediente,
   estado_expediente, tipo_movimiento, tipo_documento, tipo_acta,
   procedimiento_registral, tipo_documento_adjunto, estado_documento,
   tipo_observacion, tipo_resultado_evaluacion, tipo_resultado_ejecucion,
   tipo_notificacion, estado_notificacion, estado_cargo_acuse, tipo_resolucion,
   motivo_no_corresponde, motivo_archivo, motivo_correccion, flujo,
   flujo_transicion, flujo_transicion_rol, flujo_transicion_equipo, permiso,
   rol_permiso, plazo_configuracion, feriado_nacional, ubigeo_departamento,
   ubigeo_provincia, ubigeo_distrito, tipo_resultado_validacion,
   legacy_estado_map, legacy_catalogo_map.

   Qué SÍ se vacía: todo lo demás (usuario, usuario_rol, equipo_usuario,
   usuario_supervision, usuario_totp_backup_code, persona, expediente y
   todas sus tablas hijas, auditoria_evento, etc.) — se calcula por
   exclusión de la lista anterior, así el script sigue siendo válido si en
   el futuro se agregan tablas transaccionales nuevas.

   Mecanismo (evita tener que calcular a mano el orden de 30+ FKs):
   1) Deshabilita TODAS las foreign keys del esquema (constraint_type='R').
   2) Trunca cada tabla que no esté en la lista de catálogos.
   3) Reinicia a 1 cada columna IDENTITY de esas mismas tablas.
   4) Vuelve a habilitar todas las foreign keys.
   5) Inserta el usuario superadministrador (rol ADMIN_SISTEMA).

   Idempotente en el sentido de que puede volver a correrse (dejaría el
   esquema igual de vacío + un solo superadmin), pero NO es reversible:
   destruye cualquier dato existente en las tablas no listadas como catálogo.
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


/* ------------------------------------------------------------
   Paso 2: truncar todas las tablas EXCEPTO los catálogos/maestros

   Nota: se usa una lista literal NOT IN en vez de un tipo colección
   (SYS.ODCIVARCHAR2LIST + MEMBER OF) porque esa combinación no compiló
   en el entorno real (ORA-00932 "se esperaba UDT"). NOT IN con literales
   es más simple y compatible con cualquier versión/cliente de Oracle.
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
       'LEGACY_ESTADO_MAP', 'LEGACY_CATALOGO_MAP'
     )
  ) LOOP
    EXECUTE IMMEDIATE 'TRUNCATE TABLE ' || t.table_name;
    DBMS_OUTPUT.PUT_LINE('Truncada: ' || t.table_name);
  END LOOP;
END;


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
       'LEGACY_ESTADO_MAP', 'LEGACY_CATALOGO_MAP'
     )
  ) LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE ' || ic.table_name || ' MODIFY ' || ic.column_name
        || ' GENERATED BY DEFAULT AS IDENTITY (START WITH 1)';
    DBMS_OUTPUT.PUT_LINE('IDENTITY reiniciada a 1: ' || ic.table_name || '.' || ic.column_name);
  END LOOP;
END;


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


/* ------------------------------------------------------------
   Paso 5: crear el usuario superadministrador

   IMPORTANTE: reemplaza :HASH_BCRYPT_TEMPORAL por el hash real antes de
   ejecutar. Genera ese hash localmente (no lo escribas nunca en texto
   plano en este archivo) con:

     java -cp target/SDRERC-V2.jar com.sdrerc.tools.PasswordHashCli "tu-contraseña-temporal"

   Revisa/ajusta el username y el correo si prefieres otros antes de
   ejecutar. Con debe_cambiar_password=1, el sistema te pedirá cambiar la
   contraseña y enrolar la verificación en dos pasos (TOTP) en tu primer
   ingreso — este INSERT reemplaza al bootstrap manual documentado al final
   de 61_login_2fa_usuario.sql (ya no hace falta repetir ese paso).
   ------------------------------------------------------------ */

INSERT INTO usuario (
    username, password_hash, nombre_completo, tipo_documento, numero_documento,
    correo, estado, activo, debe_cambiar_password, totp_habilitado,
    intentos_fallidos, creado_en
) VALUES (
    'jveram', '$2a$10$iVR0ENNqZNI10S7g4Df1c.Gn3Rdw4OnKfAzCEyuRetB1SCnt6u7Nu', 'Josimar Vera Miranda', 'DNI', '43847796',
    NULL, 'ACTIVO', 1, 1, 0,
    0, SYSTIMESTAMP
);

INSERT INTO usuario_rol (id_usuario, id_rol, activo, creado_en)
SELECT u.id_usuario, r.id_rol, 1, SYSTIMESTAMP
  FROM usuario u, rol r
 WHERE UPPER(u.username) = 'JVERAM'
   AND UPPER(r.codigo) = 'ADMIN_SISTEMA';

COMMIT;

/* ============================================================
   Verificación posterior
   ============================================================ */

SELECT id_usuario, username, nombre_completo, numero_documento, activo,
       debe_cambiar_password, totp_habilitado
  FROM usuario;

SELECT u.username, r.codigo AS rol
  FROM usuario_rol ur
  JOIN usuario u ON u.id_usuario = ur.id_usuario
  JOIN rol r ON r.id_rol = ur.id_rol
 WHERE ur.activo = 1;

SELECT table_name, num_rows FROM user_tables
 WHERE table_name IN ('EXPEDIENTE', 'EXPEDIENTE_SOLICITUD', 'PERSONA', 'USUARIO')
 ORDER BY table_name;
