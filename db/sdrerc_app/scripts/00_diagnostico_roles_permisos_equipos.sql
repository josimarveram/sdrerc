/* ============================================================
   DIAGNOSTICO - Roles, permisos y equipos (solo lectura)
   Ejecutar conectado como SDRERC_APP. No modifica datos.

   Uso: correr cada bloque para entender el estado actual antes de
   decidir la matriz de rol_permiso. No forma parte de la numeracion
   incremental de scripts (es diagnostico, como 00_diagnostico_previo.sql).
   ============================================================ */

-- 1) Catalogo de roles y cuantos permisos tiene cada uno hoy
SELECT r.codigo AS rol, r.nombre, r.activo,
       COUNT(rp.id_permiso) AS permisos_asignados
  FROM rol r
  LEFT JOIN rol_permiso rp ON rp.id_rol = r.id_rol AND rp.activo = 1
 GROUP BY r.codigo, r.nombre, r.activo
 ORDER BY r.codigo;

-- 2) Catalogo completo de permisos (modulo + bandeja) disponibles
SELECT codigo, nombre, modulo, activo
  FROM permiso
 ORDER BY modulo, codigo;

-- 3) Detalle: que permiso tiene cada rol hoy (vacio = fail-open = ve todo)
SELECT r.codigo AS rol, p.codigo AS permiso, p.modulo
  FROM rol_permiso rp
  JOIN rol r ON r.id_rol = rp.id_rol
  JOIN permiso p ON p.id_permiso = rp.id_permiso
 WHERE rp.activo = 1
 ORDER BY r.codigo, p.codigo;

-- 4) Equipos y cuantos usuarios activos tiene cada uno
SELECT eq.codigo AS equipo, eq.nombre, COUNT(eu.id_usuario) AS usuarios
  FROM equipo eq
  LEFT JOIN equipo_usuario eu ON eu.id_equipo = eq.id_equipo AND eu.activo = 1
 GROUP BY eq.codigo, eq.nombre
 ORDER BY eq.codigo;

-- 5) Usuarios con su(s) rol(es), equipo(s) y si son responsables
SELECT u.username, u.nombre_completo,
       LISTAGG(DISTINCT r.codigo, ', ') WITHIN GROUP (ORDER BY r.codigo) AS roles,
       LISTAGG(DISTINCT eq.codigo || CASE WHEN eu.es_responsable = 1 THEN ' (resp.)' ELSE '' END, ', ')
         WITHIN GROUP (ORDER BY eq.codigo) AS equipos
  FROM usuario u
  LEFT JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario AND ur.activo = 1
  LEFT JOIN rol r ON r.id_rol = ur.id_rol
  LEFT JOIN equipo_usuario eu ON eu.id_usuario = u.id_usuario AND eu.activo = 1
  LEFT JOIN equipo eq ON eq.id_equipo = eu.id_equipo
 WHERE u.activo = 1
 GROUP BY u.username, u.nombre_completo
 ORDER BY u.username;

-- 6) Usuarios sin ningun rol (quedan fail-open = ven todo hasta que se les asigne uno)
SELECT u.username, u.nombre_completo
  FROM usuario u
 WHERE u.activo = 1
   AND NOT EXISTS (SELECT 1 FROM usuario_rol ur WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1)
 ORDER BY u.username;

-- 7) Relacion supervisor/abogado (USUARIO_SUPERVISION) por supervisor
SELECT sup.username AS supervisor, sup.nombre_completo AS supervisor_nombre,
       COUNT(*) AS abogados_supervisados
  FROM usuario_supervision us
  JOIN usuario sup ON sup.id_usuario = us.id_supervisor
 WHERE us.activo = 1
 GROUP BY sup.username, sup.nombre_completo
 ORDER BY abogados_supervisados DESC;
