/* ============================================================
   SCRIPT 60 - Catalogo de permisos por bandeja (pestana superior)
   dentro de modulos con mas de una bandeja: Registro/Recepcion,
   Asignacion y Notificacion.
   Ejecutar conectado como SDRERC_APP.

   Contexto: el script 59 sembro un permiso por modulo (boton del menu
   lateral, ej. MENU_ASIGNACION). Este script agrega un segundo nivel:
   un permiso por cada pestana superior (bandeja) dentro de esos 3
   modulos, ya que no todo el que tiene acceso al modulo debe ver todas
   sus bandejas (ej. Notificacion: Bandeja Asignacion es para
   supervisores, Bandeja Validacion es para validadores, Bandeja
   Notificacion es para quien ejecuta el envio).

   Deliberadamente NO se agregan permisos por panel/lengueta interna
   (Datos, Asignacion, Firma, Validar, etc.): esas son facetas de la
   misma tarea dentro de una bandeja ya autorizada, no funciones
   independientes. Tampoco se agrega permiso para la pestana dinamica
   "Edicion manual" de Asignacion (se abre solo como parte del flujo de
   Bandeja Asignacion, no es una bandeja de navegacion propia).

   Nota tecnica: cuando un usuario no tiene un permiso de bandeja, la
   pestana correspondiente se deshabilita (setEnabledAt) en vez de
   eliminarse del JTabbedPane. Esto es intencional: JPanelAsignacionV2.java
   y JPanelNotificacionV2.java tienen logica interna que asume indices
   fijos de pestana (comparaciones tipo getSelectedIndex() == 0/1/2);
   remover pestanas correria el riesgo de desalinear esa logica.

   Otorga todos los permisos nuevos al rol ADMIN_SISTEMA (mismo criterio
   que el script 59). La asignacion al resto de roles operativos queda
   pendiente de configurar desde Administracion > Roles > "Permisos del
   rol".

   Idempotente: usa MERGE, se puede re-ejecutar sin duplicar datos.
   ============================================================ */

MERGE INTO permiso dst
USING (
  SELECT 'BANDEJA_REGISTRO_LISTADO' AS codigo, 'Ver Bandeja Registro' AS nombre, 'Registro / Recepcion' AS modulo FROM dual
  UNION ALL SELECT 'BANDEJA_REGISTRO_CARGA_DIARIA', 'Ver Carga diaria', 'Registro / Recepcion' FROM dual
  UNION ALL SELECT 'BANDEJA_REGISTRO_MANUAL', 'Ver Registro manual', 'Registro / Recepcion' FROM dual
  UNION ALL SELECT 'BANDEJA_ASIGNACION_LISTADO', 'Ver Bandeja Asignacion', 'Asignacion' FROM dual
  UNION ALL SELECT 'BANDEJA_ASIGNACION_CARTAS_RESPUESTA', 'Ver Cartas de respuesta', 'Asignacion' FROM dual
  UNION ALL SELECT 'BANDEJA_ASIGNACION_CARGA_ABOGADOS', 'Ver Carga Abogados', 'Asignacion' FROM dual
  UNION ALL SELECT 'BANDEJA_NOTIFICACION_ASIGNACION', 'Ver Bandeja Asignacion (Notificacion)', 'Notificacion' FROM dual
  UNION ALL SELECT 'BANDEJA_NOTIFICACION_VALIDACION', 'Ver Bandeja Validacion', 'Notificacion' FROM dual
  UNION ALL SELECT 'BANDEJA_NOTIFICACION_NOTIFICACION', 'Ver Bandeja Notificacion', 'Notificacion' FROM dual
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
   Otorgar todos los permisos de bandeja sembrados al rol ADMIN_SISTEMA
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
      'BANDEJA_REGISTRO_LISTADO', 'BANDEJA_REGISTRO_CARGA_DIARIA', 'BANDEJA_REGISTRO_MANUAL',
      'BANDEJA_ASIGNACION_LISTADO', 'BANDEJA_ASIGNACION_CARTAS_RESPUESTA', 'BANDEJA_ASIGNACION_CARGA_ABOGADOS',
      'BANDEJA_NOTIFICACION_ASIGNACION', 'BANDEJA_NOTIFICACION_VALIDACION', 'BANDEJA_NOTIFICACION_NOTIFICACION'
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

SELECT codigo, nombre, modulo, activo
FROM permiso
WHERE codigo LIKE 'BANDEJA_%'
ORDER BY modulo, nombre;

SELECT r.codigo AS rol, COUNT(*) AS permisos_bandeja_activos
FROM rol_permiso rp
JOIN rol r ON r.id_rol = rp.id_rol
JOIN permiso p ON p.id_permiso = rp.id_permiso
WHERE rp.activo = 1 AND p.codigo LIKE 'BANDEJA_%'
GROUP BY r.codigo
ORDER BY r.codigo;
