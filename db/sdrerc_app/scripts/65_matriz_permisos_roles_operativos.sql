/* ============================================================
   SCRIPT 65 - Matriz de permisos por rol operativo + roles nuevos
   VALIDACION y SUPERVISOR_NOTIFICACION
   Ejecutar conectado como SDRERC_APP.

   Contexto: los scripts 59/60 sembraron el catalogo completo de permisos
   (MENU_* y BANDEJA_*) pero solo le otorgaron TODO a ADMIN_SISTEMA. El
   resto de roles (RECEPCION, ASIGNACION, ABOGADO, SUPERVISION,
   NOTIFICACION, REGISTRADOR_CIVIL) quedaron sin ningun permiso
   configurado, y como SessionContext.tienePermiso es fail-open cuando
   la suma de permisos de un usuario esta vacia, en la practica CUALQUIER
   usuario autenticado veia el menu completo, sin importar su rol. Con
   los 111 usuarios ya cargados (script 63) y sus roles asignados, este
   script cierra esa brecha: define que ve cada rol.

   IMPORTANTE - equipo NO es un mecanismo de control de acceso. Equipo
   solo determina a quien se le asigna trabajo (a que equipo/usuario se
   rutea un expediente en Asignacion/Notificacion). Lo que determina que
   modulos/bandejas ve un usuario es exclusivamente su(s) rol(es) via
   PERMISO/ROL_PERMISO. Ver diagnostico en
   00_diagnostico_roles_permisos_equipos.sql.

   Cambios:
   1) NOTIFICACION se divide en 3 roles (pedido explicito del usuario,
      tras revisar la primera version de este script que solo separaba
      VALIDACION):
      - SUPERVISOR_NOTIFICACION (nuevo): ve Bandeja Asignacion de
        Notificacion (asignar/reasignar documentos a validadores y
        notificadores). Se reasigna a SHIRLEY DIOSES (usuario sdioses):
        en el Excel de origen (personal_supervisores.xlsx, hoja
        "PERSONAL Y SUPERVISORES") su propia fila la lista a ELLA MISMA
        como supervisor (patron auto-referencial = cima de la cadena),
        no tiene cargo de calificadora asignado (columna de cargo
        vacia, a diferencia de sus companeros), y 17 personas del area
        "Notificacion de Documentos" la listan como su supervisora. Es
        el mismo tipo de evidencia usado en el script 63 para identificar
        a LISSET CALIXTRO/LIUBEN CELI/GLENYS BRUNO como supervisoras.
        Confirmado ademas por el usuario: hoy supervisa de hecho
        Ejecucion, Notificacion y Validacion. Por eso se le asignan
        AMBOS roles nuevos que si tienen mecanismo de permiso hoy
        (SUPERVISOR_NOTIFICACION + VALIDACION); Ejecucion no tiene
        permiso a nivel bandeja ni rol de supervision propio en el
        sistema, asi que su supervision ahi no se modela todavia (ver
        bloque "Ajuste puntual" mas abajo).
      - VALIDACION: ve unicamente Bandeja Validacion (sin cambios
        respecto a la version anterior de este script). Hoy NADIE mas
        tiene este rol asignado: el Excel de origen no distinguia
        "validador" dentro del area "Notificacion de Documentos"; falta
        que un administrador reasigne manualmente (Administracion >
        Usuarios) a las personas que efectivamente validaran, cambiando
        su rol de NOTIFICACION a VALIDACION y su equipo de
        EQ_NOTIFICACION a EQ_VALIDACION.
      - NOTIFICACION (existente, se recorta): ve unicamente Bandeja
        Notificacion; pierde Bandeja Asignacion (ahora exclusiva de
        SUPERVISOR_NOTIFICACION).
   2) REGISTRADOR_CIVIL queda sin permisos por decision explicita: Firma
      y Emision ya esta integrada dentro de Verificacion (no es modulo
      aparte) y nadie tiene ese rol asignado hoy. Se revisa cuando haga
      falta, sin bloquear este ajuste.
   3) Matriz de permisos por rol:

      | Rol                     | Modulos (MENU_*)                              | Bandejas (BANDEJA_*)                                          |
      |--------------------------|------------------------------------------------|-----------------------------------------------------------------|
      | RECEPCION                | MENU_BANDEJA, MENU_REGISTRO                     | BANDEJA_REGISTRO_LISTADO/CARGA_DIARIA/MANUAL (las 3)            |
      | ASIGNACION                | MENU_BANDEJA, MENU_ASIGNACION                   | BANDEJA_ASIGNACION_LISTADO/CARTAS_RESPUESTA/CARGA_ABOGADOS (3)  |
      | ABOGADO                   | MENU_BANDEJA, MENU_ANALISIS, MENU_EJECUCION,    | (Analisis/Ejecucion no tienen permiso a nivel bandeja)          |
      |                           | MENU_EXPEDIENTE_DIGITAL                         |                                                                   |
      | SUPERVISION               | MENU_BANDEJA, MENU_VERIFICACION                 | (Verificacion no tiene permiso a nivel bandeja)                 |
      | SUPERVISOR_NOTIFICACION   | MENU_BANDEJA, MENU_NOTIFICACION                 | BANDEJA_NOTIFICACION_ASIGNACION unicamente                      |
      | NOTIFICACION              | MENU_BANDEJA, MENU_NOTIFICACION                 | BANDEJA_NOTIFICACION_NOTIFICACION unicamente                    |
      | VALIDACION                | MENU_BANDEJA, MENU_NOTIFICACION                 | BANDEJA_NOTIFICACION_VALIDACION unicamente                      |
      | CONSULTA                  | MENU_BANDEJA unicamente                         | (ninguna)                                                        |
      | REGISTRADOR_CIVIL         | (ninguno, ver punto 2)                          | (ninguna)                                                        |
      | ADMIN_SISTEMA             | ya tiene todos (scripts 59/60), no se toca      | ya tiene todas (scripts 59/60), no se toca                      |

      MENU_ADMIN_* (Usuarios/Equipo Juridico/Roles/Feriados/Plazos)
      queda exclusivo de ADMIN_SISTEMA; ningun rol operativo lo recibe.

   Idempotente: MERGE, se puede re-ejecutar sin duplicar. No se ejecuto
   contra ninguna base de datos.
   ============================================================ */

/* ------------------------------------------------------------
   Roles nuevos: VALIDACION y SUPERVISOR_NOTIFICACION
   ------------------------------------------------------------ */

MERGE INTO rol dst
USING (
  SELECT 'VALIDACION' AS codigo, 'Validacion' AS nombre,
         'Validacion de documentos finales (Bandeja Validacion de Notificacion)' AS descripcion
    FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, descripcion, activo)
  VALUES (src.codigo, src.nombre, src.descripcion, 1);

MERGE INTO rol dst
USING (
  SELECT 'SUPERVISOR_NOTIFICACION' AS codigo, 'Supervisor de Notificacion' AS nombre,
         'Asigna documentos a validadores y notificadores (Bandeja Asignacion de Notificacion)' AS descripcion
    FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, descripcion, activo)
  VALUES (src.codigo, src.nombre, src.descripcion, 1);

COMMIT;

/* ------------------------------------------------------------
   Matriz de permisos por rol
   ------------------------------------------------------------ */

MERGE INTO rol_permiso dst
USING (
  SELECT r.id_rol AS id_rol, p.id_permiso AS id_permiso
    FROM (
      -- RECEPCION
      SELECT 'RECEPCION' AS rol_codigo, 'MENU_BANDEJA' AS permiso_codigo FROM dual
      UNION ALL SELECT 'RECEPCION', 'MENU_REGISTRO' FROM dual
      UNION ALL SELECT 'RECEPCION', 'BANDEJA_REGISTRO_LISTADO' FROM dual
      UNION ALL SELECT 'RECEPCION', 'BANDEJA_REGISTRO_CARGA_DIARIA' FROM dual
      UNION ALL SELECT 'RECEPCION', 'BANDEJA_REGISTRO_MANUAL' FROM dual
      -- ASIGNACION
      UNION ALL SELECT 'ASIGNACION', 'MENU_BANDEJA' FROM dual
      UNION ALL SELECT 'ASIGNACION', 'MENU_ASIGNACION' FROM dual
      UNION ALL SELECT 'ASIGNACION', 'BANDEJA_ASIGNACION_LISTADO' FROM dual
      UNION ALL SELECT 'ASIGNACION', 'BANDEJA_ASIGNACION_CARTAS_RESPUESTA' FROM dual
      UNION ALL SELECT 'ASIGNACION', 'BANDEJA_ASIGNACION_CARGA_ABOGADOS' FROM dual
      -- ABOGADO
      UNION ALL SELECT 'ABOGADO', 'MENU_BANDEJA' FROM dual
      UNION ALL SELECT 'ABOGADO', 'MENU_ANALISIS' FROM dual
      UNION ALL SELECT 'ABOGADO', 'MENU_EJECUCION' FROM dual
      UNION ALL SELECT 'ABOGADO', 'MENU_EXPEDIENTE_DIGITAL' FROM dual
      -- SUPERVISION
      UNION ALL SELECT 'SUPERVISION', 'MENU_BANDEJA' FROM dual
      UNION ALL SELECT 'SUPERVISION', 'MENU_VERIFICACION' FROM dual
      -- SUPERVISOR_NOTIFICACION (solo Bandeja Asignacion)
      UNION ALL SELECT 'SUPERVISOR_NOTIFICACION', 'MENU_BANDEJA' FROM dual
      UNION ALL SELECT 'SUPERVISOR_NOTIFICACION', 'MENU_NOTIFICACION' FROM dual
      UNION ALL SELECT 'SUPERVISOR_NOTIFICACION', 'BANDEJA_NOTIFICACION_ASIGNACION' FROM dual
      -- NOTIFICACION (solo Bandeja Notificacion)
      UNION ALL SELECT 'NOTIFICACION', 'MENU_BANDEJA' FROM dual
      UNION ALL SELECT 'NOTIFICACION', 'MENU_NOTIFICACION' FROM dual
      UNION ALL SELECT 'NOTIFICACION', 'BANDEJA_NOTIFICACION_NOTIFICACION' FROM dual
      -- VALIDACION (solo Bandeja Validacion)
      UNION ALL SELECT 'VALIDACION', 'MENU_BANDEJA' FROM dual
      UNION ALL SELECT 'VALIDACION', 'MENU_NOTIFICACION' FROM dual
      UNION ALL SELECT 'VALIDACION', 'BANDEJA_NOTIFICACION_VALIDACION' FROM dual
      -- CONSULTA (solo bandeja general de expedientes)
      UNION ALL SELECT 'CONSULTA', 'MENU_BANDEJA' FROM dual
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

/* ------------------------------------------------------------
   Ajuste puntual: SHIRLEY DIOSES (sdioses) hoy supervisa de hecho
   Ejecucion, Notificacion y Validacion (confirmado por el usuario).
   De esos tres, solo Notificacion/Validacion tienen mecanismo de
   permiso por bandeja en el sistema (Ejecucion no tiene permiso a
   nivel bandeja; darle supervision real ahi requeriria un rol o
   permiso nuevo que hoy no existe y no se crea sin autorizacion
   explicita, ver CLAUDE.md "No crear rutas, estados, tablas o
   acciones no definidas"). Este ajuste cubre lo que si es modelable
   hoy: se reasigna de NOTIFICACION a SUPERVISOR_NOTIFICACION +
   VALIDACION (ve Bandeja Asignacion y Bandeja Validacion de
   Notificacion), y se marca responsable en EQ_NOTIFICACION y
   EQ_VALIDACION.
   ------------------------------------------------------------ */

UPDATE usuario_rol ur
   SET ur.activo = 0,
       ur.modificado_en = SYSTIMESTAMP
 WHERE ur.id_usuario = (SELECT id_usuario FROM usuario WHERE UPPER(username) = 'SDIOSES')
   AND ur.id_rol = (SELECT id_rol FROM rol WHERE UPPER(codigo) = 'NOTIFICACION')
   AND ur.activo = 1;

MERGE INTO usuario_rol dst
USING (
  SELECT u.id_usuario AS id_usuario, r.id_rol AS id_rol
    FROM usuario u
    CROSS JOIN rol r
   WHERE UPPER(u.username) = 'SDIOSES'
     AND UPPER(r.codigo) IN ('SUPERVISOR_NOTIFICACION', 'VALIDACION')
) src
ON (dst.id_usuario = src.id_usuario AND dst.id_rol = src.id_rol)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_usuario, id_rol, activo)
  VALUES (src.id_usuario, src.id_rol, 1);

MERGE INTO equipo_usuario dst
USING (
  SELECT u.id_usuario AS id_usuario, eq.id_equipo AS id_equipo
    FROM usuario u
    CROSS JOIN equipo eq
   WHERE UPPER(u.username) = 'SDIOSES'
     AND UPPER(eq.codigo) IN ('EQ_NOTIFICACION', 'EQ_VALIDACION')
) src
ON (dst.id_usuario = src.id_usuario AND dst.id_equipo = src.id_equipo)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.es_responsable = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_usuario, id_equipo, es_responsable, activo)
  VALUES (src.id_usuario, src.id_equipo, 1, 1);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT r.codigo AS rol, p.codigo AS permiso, p.modulo
  FROM rol_permiso rp
  JOIN rol r ON r.id_rol = rp.id_rol
  JOIN permiso p ON p.id_permiso = rp.id_permiso
 WHERE rp.activo = 1
 ORDER BY r.codigo, p.codigo;

SELECT r.codigo AS rol, COUNT(*) AS permisos_activos
  FROM rol_permiso rp
  JOIN rol r ON r.id_rol = rp.id_rol
 WHERE rp.activo = 1
 GROUP BY r.codigo
 ORDER BY r.codigo;

-- Confirmar que ningun rol operativo quedo sin permisos (fail-open no deseado)
SELECT r.codigo AS rol_sin_permisos
  FROM rol r
 WHERE r.activo = 1
   AND UPPER(r.codigo) NOT IN ('REGISTRADOR_CIVIL')  -- excluido a proposito, ver punto 2
   AND NOT EXISTS (SELECT 1 FROM rol_permiso rp WHERE rp.id_rol = r.id_rol AND rp.activo = 1)
 ORDER BY r.codigo;

-- Cuantos de los 111 usuarios cargados quedarian efectivamente restringidos
-- (tienen al menos un rol con permisos) vs. cuantos siguen fail-open
SELECT
  SUM(CASE WHEN tiene_permiso_real = 1 THEN 1 ELSE 0 END) AS usuarios_restringidos,
  SUM(CASE WHEN tiene_permiso_real = 0 THEN 1 ELSE 0 END) AS usuarios_fail_open
  FROM (
    SELECT u.id_usuario,
           MAX(CASE WHEN rp.id_permiso IS NOT NULL THEN 1 ELSE 0 END) AS tiene_permiso_real
      FROM usuario u
      LEFT JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario AND ur.activo = 1
      LEFT JOIN rol_permiso rp ON rp.id_rol = ur.id_rol AND rp.activo = 1
     WHERE u.activo = 1
     GROUP BY u.id_usuario
  );

-- Confirmar el ajuste puntual de SHIRLEY DIOSES (sdioses)
SELECT u.username, u.nombre_completo,
       LISTAGG(DISTINCT r.codigo, ', ') WITHIN GROUP (ORDER BY r.codigo) AS roles_activos,
       LISTAGG(DISTINCT eq.codigo || CASE WHEN eu.es_responsable = 1 THEN ' (resp.)' ELSE '' END, ', ')
         WITHIN GROUP (ORDER BY eq.codigo) AS equipos_activos
  FROM usuario u
  LEFT JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario AND ur.activo = 1
  LEFT JOIN rol r ON r.id_rol = ur.id_rol
  LEFT JOIN equipo_usuario eu ON eu.id_usuario = u.id_usuario AND eu.activo = 1
  LEFT JOIN equipo eq ON eq.id_equipo = eu.id_equipo
 WHERE UPPER(u.username) = 'SDIOSES'
 GROUP BY u.username, u.nombre_completo;
