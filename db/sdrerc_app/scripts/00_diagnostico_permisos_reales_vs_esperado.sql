/* ============================================================
   DIAGNOSTICO - Permisos reales (BD) vs matriz esperada (script 65)
   Ejecutar conectado como SDRERC_APP. No modifica datos.

   Contexto: en Administracion > Roles aparecen 14 roles, pero los
   scripts de este proyecto (09, 63, 65) solo reconocen 10:
   ADMIN_SISTEMA, RECEPCION, ASIGNACION, ABOGADO, SUPERVISION,
   SUPERVISOR_NOTIFICACION, NOTIFICACION, VALIDACION, CONSULTA,
   REGISTRADOR_CIVIL. Los otros 4 (ADMINISTRADOR, ANALISTA,
   PREASIGNADOR, SUPERVISOR) no existen en ningun script ni se
   referencian en el codigo Java (verificado por grep): son roles
   creados manualmente desde el boton "Nuevo rol" de esa pantalla,
   mismo origen que la duplicidad de equipos (EQUIPO_ANALISIS vs
   EQ_ANALISIS) que se corrigio con el script 66.

   Este diagnostico compara, para los 10 roles reconocidos, que
   permiso tiene CADA UNO hoy en la base contra la matriz documentada
   en el script 65, y lista aparte los 4 roles no reconocidos con su
   cantidad de usuarios/permisos para decidir que hacer con ellos
   (ver script 70, que los desactiva).

   El punto 4 detecta ademas un problema distinto: filas en el
   catalogo PERMISO que no vienen de los scripts 59/60 (prefijo
   PERM_* en vez de MENU_*/BANDEJA_*, confirmado por grep que ningun
   codigo Java las inserta -- no hay pantalla "Nuevo permiso"). Se
   encontraron 3 asignadas al rol ABOGADO; el punto 4 muestra el total
   y quien mas las referencia. Ver script 71 para la limpieza.
   ============================================================ */

-- 1) Roles NO reconocidos por los scripts del proyecto (candidatos a
--    consolidar/desactivar, ver script 70)
SELECT r.codigo, r.nombre, r.descripcion, r.activo,
       (SELECT COUNT(*) FROM usuario_rol ur WHERE ur.id_rol = r.id_rol AND ur.activo = 1) AS usuarios_asignados,
       (SELECT COUNT(*) FROM rol_permiso rp WHERE rp.id_rol = r.id_rol AND rp.activo = 1) AS permisos_asignados
  FROM rol r
 WHERE UPPER(r.codigo) NOT IN (
         'ADMIN_SISTEMA', 'RECEPCION', 'ASIGNACION', 'ABOGADO', 'SUPERVISION',
         'SUPERVISOR_NOTIFICACION', 'NOTIFICACION', 'VALIDACION', 'CONSULTA',
         'REGISTRADOR_CIVIL')
 ORDER BY r.codigo;

-- 2) Matriz esperada (igual a la del script 65) vs lo que hay realmente
--    en rol_permiso. Columna OBSERVACION marca lo que sobra o falta.
WITH esperado (rol_codigo, permiso_codigo) AS (
  SELECT 'RECEPCION', 'MENU_BANDEJA' FROM dual
  UNION ALL SELECT 'RECEPCION', 'MENU_REGISTRO' FROM dual
  UNION ALL SELECT 'RECEPCION', 'BANDEJA_REGISTRO_LISTADO' FROM dual
  UNION ALL SELECT 'RECEPCION', 'BANDEJA_REGISTRO_CARGA_DIARIA' FROM dual
  UNION ALL SELECT 'RECEPCION', 'BANDEJA_REGISTRO_MANUAL' FROM dual
  UNION ALL SELECT 'ASIGNACION', 'MENU_BANDEJA' FROM dual
  UNION ALL SELECT 'ASIGNACION', 'MENU_ASIGNACION' FROM dual
  UNION ALL SELECT 'ASIGNACION', 'BANDEJA_ASIGNACION_LISTADO' FROM dual
  UNION ALL SELECT 'ASIGNACION', 'BANDEJA_ASIGNACION_CARTAS_RESPUESTA' FROM dual
  UNION ALL SELECT 'ASIGNACION', 'BANDEJA_ASIGNACION_CARGA_ABOGADOS' FROM dual
  UNION ALL SELECT 'ABOGADO', 'MENU_BANDEJA' FROM dual
  UNION ALL SELECT 'ABOGADO', 'MENU_ANALISIS' FROM dual
  UNION ALL SELECT 'ABOGADO', 'MENU_EJECUCION' FROM dual
  UNION ALL SELECT 'ABOGADO', 'MENU_EXPEDIENTE_DIGITAL' FROM dual
  UNION ALL SELECT 'SUPERVISION', 'MENU_BANDEJA' FROM dual
  UNION ALL SELECT 'SUPERVISION', 'MENU_VERIFICACION' FROM dual
  UNION ALL SELECT 'SUPERVISOR_NOTIFICACION', 'MENU_BANDEJA' FROM dual
  UNION ALL SELECT 'SUPERVISOR_NOTIFICACION', 'MENU_NOTIFICACION' FROM dual
  UNION ALL SELECT 'SUPERVISOR_NOTIFICACION', 'BANDEJA_NOTIFICACION_ASIGNACION' FROM dual
  UNION ALL SELECT 'NOTIFICACION', 'MENU_BANDEJA' FROM dual
  UNION ALL SELECT 'NOTIFICACION', 'MENU_NOTIFICACION' FROM dual
  UNION ALL SELECT 'NOTIFICACION', 'BANDEJA_NOTIFICACION_NOTIFICACION' FROM dual
  UNION ALL SELECT 'VALIDACION', 'MENU_BANDEJA' FROM dual
  UNION ALL SELECT 'VALIDACION', 'MENU_NOTIFICACION' FROM dual
  UNION ALL SELECT 'VALIDACION', 'BANDEJA_NOTIFICACION_VALIDACION' FROM dual
  UNION ALL SELECT 'CONSULTA', 'MENU_BANDEJA' FROM dual
)
SELECT rc.rol_codigo AS rol,
       rc.permiso_codigo AS permiso,
       CASE
         WHEN rc.origen = 'REAL' AND rc.esperado = 0 THEN 'SOBRA (no esta en la matriz del script 65)'
         WHEN rc.origen = 'ESPERADO' AND rc.real = 0 THEN 'FALTA (deberia estar segun script 65)'
         ELSE 'OK'
       END AS observacion
  FROM (
    -- lo que hay realmente en rol_permiso (para roles reconocidos)
    SELECT r.codigo AS rol_codigo, p.codigo AS permiso_codigo, 'REAL' AS origen,
           1 AS real,
           CASE WHEN EXISTS (SELECT 1 FROM esperado e WHERE e.rol_codigo = UPPER(r.codigo) AND e.permiso_codigo = UPPER(p.codigo)) THEN 1 ELSE 0 END AS esperado
      FROM rol_permiso rp
      JOIN rol r ON r.id_rol = rp.id_rol
      JOIN permiso p ON p.id_permiso = rp.id_permiso
     WHERE rp.activo = 1
       AND UPPER(r.codigo) IN (
             'ADMIN_SISTEMA', 'RECEPCION', 'ASIGNACION', 'ABOGADO', 'SUPERVISION',
             'SUPERVISOR_NOTIFICACION', 'NOTIFICACION', 'VALIDACION', 'CONSULTA',
             'REGISTRADOR_CIVIL')
    UNION ALL
    -- lo que deberia haber segun el script 65 (para detectar faltantes)
    SELECT e.rol_codigo, e.permiso_codigo, 'ESPERADO' AS origen,
           CASE WHEN EXISTS (
             SELECT 1 FROM rol_permiso rp JOIN rol r ON r.id_rol = rp.id_rol JOIN permiso p ON p.id_permiso = rp.id_permiso
              WHERE UPPER(r.codigo) = e.rol_codigo AND UPPER(p.codigo) = e.permiso_codigo AND rp.activo = 1
           ) THEN 1 ELSE 0 END AS real,
           1 AS esperado
      FROM esperado e
  ) rc
 WHERE NOT (rc.origen = 'REAL' AND rc.esperado = 1)   -- evita duplicar las filas que ya estan OK
 ORDER BY rol, permiso;

-- 3) ADMIN_SISTEMA no tiene matriz fija (ya tiene todos los permisos por
--    scripts 59/60): solo se informa cuantos tiene hoy, para contraste.
SELECT r.codigo, COUNT(*) AS permisos_activos,
       (SELECT COUNT(*) FROM permiso WHERE activo = 1) AS permisos_totales_catalogo
  FROM rol_permiso rp
  JOIN rol r ON r.id_rol = rp.id_rol
 WHERE rp.activo = 1 AND UPPER(r.codigo) = 'ADMIN_SISTEMA'
 GROUP BY r.codigo;

-- 4) Permisos del catalogo que NO son de scripts 59/60 (los 22
--    MENU_*/BANDEJA_* oficiales). Ninguna clase Java inserta en PERMISO
--    (no hay pantalla "Nuevo permiso"), asi que estos se cargaron por
--    fuera de este repositorio. Muestra que roles los usan hoy (activos
--    o no) para decidir el alcance del script 71.
SELECT p.codigo, p.nombre, p.modulo, p.activo,
       (SELECT LISTAGG(r.codigo || CASE WHEN rp.activo = 1 THEN '' ELSE ' (rp inactivo)' END, ', ')
               WITHIN GROUP (ORDER BY r.codigo)
          FROM rol_permiso rp JOIN rol r ON r.id_rol = rp.id_rol
         WHERE rp.id_permiso = p.id_permiso) AS roles_que_lo_referencian
  FROM permiso p
 WHERE UPPER(p.codigo) NOT IN (
         'MENU_BANDEJA', 'MENU_REGISTRO', 'MENU_ASIGNACION', 'MENU_ANALISIS', 'MENU_EJECUCION',
         'MENU_EXPEDIENTE_DIGITAL', 'MENU_VERIFICACION', 'MENU_NOTIFICACION', 'MENU_ADMIN_USUARIOS',
         'MENU_ADMIN_EQUIPO_JURIDICO', 'MENU_ADMIN_ROLES', 'MENU_ADMIN_FERIADOS', 'MENU_ADMIN_PLAZOS',
         'BANDEJA_REGISTRO_LISTADO', 'BANDEJA_REGISTRO_CARGA_DIARIA', 'BANDEJA_REGISTRO_MANUAL',
         'BANDEJA_ASIGNACION_LISTADO', 'BANDEJA_ASIGNACION_CARTAS_RESPUESTA', 'BANDEJA_ASIGNACION_CARGA_ABOGADOS',
         'BANDEJA_NOTIFICACION_ASIGNACION', 'BANDEJA_NOTIFICACION_NOTIFICACION', 'BANDEJA_NOTIFICACION_VALIDACION')
 ORDER BY p.codigo;
