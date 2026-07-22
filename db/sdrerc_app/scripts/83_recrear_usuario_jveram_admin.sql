/* ============================================================
   SCRIPT 83 - Recrear usuario ADMIN_SISTEMA "jveram" (recuperacion de acceso)
   Ejecutar conectado como SDRERC_APP.

   Contexto: el 22/07/2026 se detecto que la tabla usuario quedo con exactamente
   118 filas de IDs contiguos (1-118), 0 filas con password_hash/totp_habilitado/
   correo, y la tabla expediente en 0 filas. Esto es la firma de un reset
   completo de datos de prueba (patron de 62_reset_datos_prueba_y_superadmin.sql
   + carga de usuarios), que dejo sin acceso a "jveram" (Josimar Vera Miranda,
   superadmin real usado para operar el sistema) porque esa cuenta no forma
   parte del roster cargado por el script de carga masiva de usuarios.

   Este script recrea unicamente esa cuenta como ADMIN_SISTEMA con una
   contraseña temporal (hash BCrypt ya generado localmente con
   com.sdrerc.tools.PasswordHashCli, nunca en texto plano en este archivo) y
   debe_cambiar_password=1, para forzar que se defina una contraseña real en el
   primer login. No toca ningun otro usuario ni tabla.

   Idempotente: si "jveram" ya existe, no hace nada (evita duplicados si se
   re-ejecuta por error).
   ============================================================ */

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1) INTO v_count FROM usuario WHERE UPPER(username) = 'JVERAM';

  IF v_count = 0 THEN
    INSERT INTO usuario (
      username, password_hash, nombre_completo, correo, estado, activo,
      debe_cambiar_password, totp_habilitado, intentos_fallidos, creado_en
    ) VALUES (
      'jveram',
      '$2a$10$HbZv43SYGcD6cU9/8PvS7.D/FjN/InbbA/iFSr3xZOJi0ZZee/AOu',
      'Josimar Vera Miranda',
      'josimarveram@gmail.com',
      'ACTIVO',
      1,
      1,
      0,
      0,
      SYSTIMESTAMP
    );
  END IF;
END;
/

DECLARE
  v_count NUMBER;
  v_id_usuario NUMBER;
  v_id_rol NUMBER;
BEGIN
  SELECT id_usuario INTO v_id_usuario FROM usuario WHERE UPPER(username) = 'JVERAM';
  SELECT id_rol INTO v_id_rol FROM rol WHERE codigo = 'ADMIN_SISTEMA';

  SELECT COUNT(1) INTO v_count
    FROM usuario_rol
   WHERE id_usuario = v_id_usuario AND id_rol = v_id_rol;

  IF v_count = 0 THEN
    INSERT INTO usuario_rol (id_usuario, id_rol, activo, creado_en)
    VALUES (v_id_usuario, v_id_rol, 1, SYSTIMESTAMP);
  END IF;
END;
/

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT u.id_usuario, u.username, u.nombre_completo, u.correo, u.activo, u.estado,
       u.debe_cambiar_password, r.codigo AS rol
FROM usuario u
JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario AND ur.activo = 1
JOIN rol r ON r.id_rol = ur.id_rol
WHERE UPPER(u.username) = 'JVERAM';
