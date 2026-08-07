/* ============================================================
   SCRIPT 94 - Permiso de menu para el modulo Dashboard (SDRERC V2)
   Ejecutar conectado como SDRERC_APP.

   Contexto: el modulo Dashboard (reportes gerenciales, MenuPrincipalV2 /
   JPanelDashboardV2) es exclusivo para el jefe de unidad / administracion
   del sistema. A diferencia del resto de botones del menu (sembrados en
   59_catalogo_permisos_menu.sql), el permiso MENU_DASHBOARD no existia
   todavia porque el modulo se creo despues de ese script.

   Ubicacion en el menu: bloque propio "Dashboard y Reportes" (no dentro de
   "Inicio" ni de "Administracion"), para que quede como vista analitica de
   alto nivel, separada de la configuracion/catalogos que vive en
   Administracion. El campo `modulo` del permiso refleja ese mismo nombre de
   bloque, para que se agrupe igual en Administracion > Roles > "Permisos
   del rol".

   Este script:
   1) Siembra el permiso MENU_DASHBOARD en el catalogo `permiso`.
   2) Lo otorga UNICAMENTE al rol ADMIN_SISTEMA (a diferencia de
      59_catalogo_permisos_menu.sql, que otorga todo lo sembrado a
      ADMIN_SISTEMA sin excepcion, aqui es igual: el Dashboard es
      deliberadamente exclusivo de ese rol, no se otorga a ningun otro).

   Sin este script ejecutado, SessionContext.tienePermiso("MENU_DASHBOARD")
   devuelve false para todos los usuarios (incluido ADMIN_SISTEMA, ya que
   su catalogo de permisos resuelto no esta vacio) y el boton "Dashboard"
   del menu lateral queda deshabilitado para todos.

   Idempotente: usa MERGE, se puede re-ejecutar sin duplicar datos.
   ============================================================ */

MERGE INTO permiso dst
USING (
  SELECT 'MENU_DASHBOARD' AS codigo, 'Ver Dashboard' AS nombre, 'Dashboard y Reportes' AS modulo FROM dual
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
   Otorgar el permiso unicamente al rol ADMIN_SISTEMA
   ------------------------------------------------------------ */

MERGE INTO rol_permiso dst
USING (
  SELECT r.id_rol AS id_rol, p.id_permiso AS id_permiso
  FROM rol r
  CROSS JOIN permiso p
  WHERE UPPER(r.codigo) = 'ADMIN_SISTEMA'
    AND r.activo = 1
    AND p.activo = 1
    AND p.codigo = 'MENU_DASHBOARD'
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

SELECT codigo, nombre, modulo, activo FROM permiso WHERE codigo = 'MENU_DASHBOARD';

SELECT r.codigo AS rol, p.codigo AS permiso, rp.activo
FROM rol_permiso rp
JOIN rol r ON r.id_rol = rp.id_rol
JOIN permiso p ON p.id_permiso = rp.id_permiso
WHERE p.codigo = 'MENU_DASHBOARD';
