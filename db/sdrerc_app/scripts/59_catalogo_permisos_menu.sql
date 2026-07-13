/* ============================================================
   SCRIPT 59 - Catalogo de permisos por modulo de menu (SDRERC V2)
   Ejecutar conectado como SDRERC_APP.

   Contexto: las tablas `permiso` y `rol_permiso` ya existian (creadas en
   07_tablas_fase_2.sql) y la pantalla Administracion > Roles > "Permisos
   del rol" ya permite asignarlos, pero el catalogo `permiso` estaba vacio
   y ningun punto de la aplicacion consultaba estos permisos para decidir
   algo en tiempo de ejecucion. Este script:

   1) Siembra un permiso por cada boton visible del menu lateral V2
      (MenuPrincipalV2.java), con codigo = el mismo codigo que el codigo
      Java usa para verificar SessionContext.tienePermiso(...).
   2) Otorga TODOS los permisos al rol ADMIN_SISTEMA, para garantizar que
      siempre exista al menos un rol con acceso completo (evita que el
      catalogo deje a todos los usuarios bloqueados si nadie configura
      permisos todavia).

   No crea tablas nuevas. No modifica usuario_rol ni rol_permiso de otros
   roles: la asignacion de permisos a RECEPCION, ASIGNACION, ABOGADO,
   SUPERVISION, NOTIFICACION y REGISTRADOR_CIVIL queda pendiente de
   configurar manualmente desde Administracion > Roles > "Permisos del
   rol" (pantalla ya existente), segun el alcance real que corresponda a
   cada rol.

   Idempotente: usa MERGE, se puede re-ejecutar sin duplicar datos.
   ============================================================ */

MERGE INTO permiso dst
USING (
  SELECT 'MENU_BANDEJA' AS codigo, 'Ver Bandeja de Expedientes' AS nombre, 'Operacion registral' AS modulo FROM dual
  UNION ALL SELECT 'MENU_REGISTRO', 'Ver Registro / Recepcion', 'Operacion registral' FROM dual
  UNION ALL SELECT 'MENU_ASIGNACION', 'Ver Asignacion', 'Operacion registral' FROM dual
  UNION ALL SELECT 'MENU_ANALISIS', 'Ver Analisis', 'Operacion registral' FROM dual
  UNION ALL SELECT 'MENU_VERIFICACION', 'Ver Verificacion', 'Operacion registral' FROM dual
  UNION ALL SELECT 'MENU_EJECUCION', 'Ver Ejecucion', 'Operacion registral' FROM dual
  UNION ALL SELECT 'MENU_NOTIFICACION', 'Ver Notificacion', 'Seguimiento y comunicacion' FROM dual
  UNION ALL SELECT 'MENU_EXPEDIENTE_DIGITAL', 'Ver Expediente digital', 'Seguimiento y comunicacion' FROM dual
  UNION ALL SELECT 'MENU_ADMIN_USUARIOS', 'Administrar Usuarios', 'Administracion' FROM dual
  UNION ALL SELECT 'MENU_ADMIN_EQUIPO_JURIDICO', 'Administrar Equipo Juridico', 'Administracion' FROM dual
  UNION ALL SELECT 'MENU_ADMIN_ROLES', 'Administrar Roles', 'Administracion' FROM dual
  UNION ALL SELECT 'MENU_ADMIN_FERIADOS', 'Administrar Feriados', 'Administracion' FROM dual
  UNION ALL SELECT 'MENU_ADMIN_PLAZOS', 'Administrar Plazos', 'Administracion' FROM dual
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
   Otorgar todos los permisos sembrados al rol ADMIN_SISTEMA
   ------------------------------------------------------------ */

MERGE INTO rol_permiso dst
USING (
  SELECT r.id_rol AS id_rol, p.id_permiso AS id_permiso
  FROM rol r
  CROSS JOIN permiso p
  WHERE UPPER(r.codigo) = 'ADMIN_SISTEMA'
    AND r.activo = 1
    AND p.activo = 1
    AND p.codigo IN (
      'MENU_BANDEJA', 'MENU_REGISTRO', 'MENU_ASIGNACION', 'MENU_ANALISIS',
      'MENU_VERIFICACION', 'MENU_EJECUCION', 'MENU_NOTIFICACION', 'MENU_EXPEDIENTE_DIGITAL',
      'MENU_ADMIN_USUARIOS', 'MENU_ADMIN_EQUIPO_JURIDICO', 'MENU_ADMIN_ROLES',
      'MENU_ADMIN_FERIADOS', 'MENU_ADMIN_PLAZOS'
    )
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

SELECT codigo, nombre, modulo, activo FROM permiso ORDER BY modulo, nombre;

SELECT r.codigo AS rol, COUNT(*) AS permisos_activos
FROM rol_permiso rp
JOIN rol r ON r.id_rol = rp.id_rol
WHERE rp.activo = 1
GROUP BY r.codigo
ORDER BY r.codigo;
