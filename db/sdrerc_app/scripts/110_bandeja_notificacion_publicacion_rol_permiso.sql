/* ============================================================
   SCRIPT 110 - Permiso de bandeja "Bandeja de publicacion"
   (4ta pestana de Notificacion), otorgado al rol NOTIFICACION.

   Contexto original (21/08/2026): el diagnostico de esa fecha
   (usuario sdioses, rol SUPERVISOR_NOTIFICACION viendo las 4
   pestanas de Notificacion en V3) confirmo que el catalogo de
   permisos por bandeja ya estaba correctamente configurado para 3
   de las 4 pestanas:
     - SUPERVISOR_NOTIFICACION -> BANDEJA_NOTIFICACION_ASIGNACION
     - NOTIFICACION            -> BANDEJA_NOTIFICACION_NOTIFICACION
     - VALIDACION              -> BANDEJA_NOTIFICACION_VALIDACION
   (sembrado por 60_catalogo_permisos_bandejas.sql / asignado por
   65_matriz_permisos_roles_operativos.sql, ambos ya ejecutados).

   La 4ta pestana ("Bandeja de publicacion" en V3; "Publicacion" en
   V2) nunca tuvo codigo de permiso propio - confirmado tambien en
   el CLAUDE.md raiz, seccion Notificacion, "Pendiente... esta
   pestana no tiene codigo de permiso propio sembrado".

   REVISION (24/08/2026, pedido explicito del usuario): la primera
   version de este script (nunca ejecutada) creaba un rol nuevo
   "PUBLICACION" dedicado, siguiendo el mismo patron 1-rol-por-
   bandeja que NOTIFICACION/VALIDACION. Verificado contra datos
   reales (SELECT de solo lectura autorizado) que los usuarios
   reales que necesitan esta bandeja (ealarcon, zcalle) ya tienen
   el rol NOTIFICACION (con el que ya ven Bandeja Notificacion) y ya
   fueron agregados al equipo EQ_PUBLICACION (visibilidad de datos,
   correcto) - no tienen ni necesitan un rol nuevo. El rol
   "PUBLICACION" separado habria dejado a estos usuarios sin acceso
   igual, salvo que ademas se les reasignara de rol. Se corrigio
   este script para otorgar el permiso directamente al rol
   NOTIFICACION ya existente (mismo patron ya usado en los otros 3
   permisos de bandeja: cada rol solo tiene el suyo, ver
   verificacion mas abajo), en vez de crear un rol dedicado nuevo.
   Ya NO crea el rol PUBLICACION.

   Este script:
   1. Siembra el permiso BANDEJA_NOTIFICACION_PUBLICACION (modulo
      Notificacion), mismo patron MERGE que el script 60.
   2. Otorga ese permiso a ADMIN_SISTEMA (mismo criterio que los 3
      permisos de bandeja ya existentes, ver verificacion mas abajo)
      y al rol NOTIFICACION (rol real de ealarcon/zcalle).

   Idempotente: usa MERGE en ambos bloques, se puede re-ejecutar sin
   duplicar datos. NO ejecutado contra la base - creado para revision
   del usuario, requiere autorizacion explicita antes de correrlo.

   Verificacion previa contra Oracle real (24/08/2026, solo lectura):
     - BANDEJA_NOTIFICACION_PUBLICACION no existe todavia en PERMISO.
     - Los 3 permisos de bandeja ya existentes siguen el patron
       "ADMIN_SISTEMA + el rol operativo correspondiente, nada mas":
         BANDEJA_NOTIFICACION_ASIGNACION   -> ADMIN_SISTEMA, SUPERVISOR_NOTIFICACION
         BANDEJA_NOTIFICACION_NOTIFICACION -> ADMIN_SISTEMA, NOTIFICACION
         BANDEJA_NOTIFICACION_VALIDACION   -> ADMIN_SISTEMA, VALIDACION
     - ealarcon y zcalle: usuario_rol = NOTIFICACION (activo);
       equipo_usuario = EQ_NOTIFICACION + EQ_PUBLICACION (ambos activos).
   ============================================================ */

MERGE INTO permiso dst
USING (
  SELECT 'BANDEJA_NOTIFICACION_PUBLICACION' AS codigo,
         'Ver Bandeja de publicacion' AS nombre,
         'Notificacion' AS modulo
    FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.modulo = src.modulo,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, modulo, activo)
  VALUES (src.codigo, src.nombre, src.modulo, 1);

COMMIT;

/* ------------------------------------------------------------
   Otorgar el permiso nuevo a ADMIN_SISTEMA y al rol NOTIFICACION
   (rol real de ealarcon/zcalle, no un rol PUBLICACION dedicado)
   ------------------------------------------------------------ */

MERGE INTO rol_permiso dst
USING (
  SELECT r.id_rol AS id_rol, p.id_permiso AS id_permiso
    FROM (
      SELECT 'ADMIN_SISTEMA' AS rol_codigo, 'BANDEJA_NOTIFICACION_PUBLICACION' AS permiso_codigo FROM dual
      UNION ALL SELECT 'NOTIFICACION', 'BANDEJA_NOTIFICACION_PUBLICACION' FROM dual
    ) pares
    JOIN rol r ON UPPER(r.codigo) = pares.rol_codigo
    JOIN permiso p ON UPPER(p.codigo) = pares.permiso_codigo
   WHERE r.activo = 1 AND p.activo = 1
) src
ON (dst.id_rol = src.id_rol AND dst.id_permiso = src.id_permiso)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_rol, id_permiso, activo)
  VALUES (src.id_rol, src.id_permiso, 1);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT codigo, nombre, modulo, activo
FROM permiso
WHERE codigo = 'BANDEJA_NOTIFICACION_PUBLICACION';

SELECT r.codigo AS rol, p.codigo AS permiso, rp.activo
FROM rol_permiso rp
JOIN rol r ON r.id_rol = rp.id_rol
JOIN permiso p ON p.id_permiso = rp.id_permiso
WHERE p.codigo = 'BANDEJA_NOTIFICACION_PUBLICACION'
ORDER BY r.codigo;
