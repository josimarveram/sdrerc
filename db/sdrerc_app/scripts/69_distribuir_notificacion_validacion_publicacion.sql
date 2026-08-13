/* ============================================================
   SCRIPT 69 - Distribuir personal de Notificacion entre
   Notificacion / Validacion / Publicacion
   Ejecutar conectado como SDRERC_APP.

   Contexto: los 17 miembros de EQ_NOTIFICACION cargados por el script 63
   (sin contar a SHIRLEY DIOSES, que el script 65 ya reasigno como
   supervisora con roles SUPERVISOR_NOTIFICACION + VALIDACION) quedaron
   todos en un solo grupo, porque el Excel de origen no distinguia
   "notificador" de "validador" de "publicador" dentro del area
   "Notificacion de Documentos" (mismo hueco que ya documentaba el
   script 65). El usuario pidio distribuirlos asi, sin un criterio de
   negocio especifico (autorizo reparto aleatorio):
     - 8 a Validacion.
     - 7 se quedan en Notificacion (sin cambios).
     - 2 a Publicacion.

   Reparto aleatorio (semilla fija para que quede documentado):
     Validacion (8):    ealarcon, hdelaguila, ltuesta, rgaspar, rtang,
                         slino, tgil, zcalle
     Notificacion (7):  etupayachi, gale, gvasquez, kmorales, lmatos,
                         mvasquez, ntupacyupanqui  (sin cambios, ya
                         tienen rol NOTIFICACION + equipo EQ_NOTIFICACION)
     Publicacion (2):   ccotrina, yrivera

   Que hace:
   1) Para los 8 de Validacion: desactiva su rol NOTIFICACION, activa rol
      VALIDACION; desactiva su membresia EQ_NOTIFICACION, activa
      EQ_VALIDACION. Esto es exactamente lo que el script 65 dejo
      pendiente ("falta que un administrador reasigne manualmente...
      cambiando su rol de NOTIFICACION a VALIDACION y su equipo de
      EQ_NOTIFICACION a EQ_VALIDACION").
   2) Para los 2 de Publicacion: desactiva su membresia EQ_NOTIFICACION,
      activa EQ_PUBLICACION. NO se les cambia el rol: hoy no existe un
      rol/permiso PUBLICACION en el catalogo (el modulo Publicacion no
      tiene boton de menu ni permiso definido en MenuPrincipalV2, brecha
      ya reportada aparte); quitarles NOTIFICACION los dejaria sin
      ningun acceso. Quedan con equipo EQ_PUBLICACION (para
      enrutamiento/organizacion) y rol NOTIFICACION (unico acceso
      disponible hasta que se defina el modulo).
   3) Los 7 de Notificacion no se tocan.

   Idempotente: MERGE/UPDATE por combinacion (usuario, rol) o
   (usuario, equipo), se puede re-ejecutar sin duplicar. No se ejecuto
   contra ninguna base de datos.
   ============================================================ */

/* ------------------------------------------------------------
   1) Grupo Validacion (8 personas): rol NOTIFICACION -> VALIDACION,
      equipo EQ_NOTIFICACION -> EQ_VALIDACION
   ------------------------------------------------------------ */

UPDATE usuario_rol ur
   SET ur.activo = 0,
       ur.modificado_en = SYSTIMESTAMP
 WHERE ur.id_rol = (SELECT id_rol FROM rol WHERE UPPER(codigo) = 'NOTIFICACION')
   AND ur.activo = 1
   AND ur.id_usuario IN (
         SELECT id_usuario FROM usuario
          WHERE UPPER(username) IN (
            'EALARCON', 'HDELAGUILA', 'LTUESTA', 'RGASPAR',
            'RTANG', 'SLINO', 'TGIL', 'ZCALLE'));

MERGE INTO usuario_rol dst
USING (
  SELECT u.id_usuario AS id_usuario, r.id_rol AS id_rol
    FROM usuario u
    CROSS JOIN rol r
   WHERE UPPER(u.username) IN (
           'EALARCON', 'HDELAGUILA', 'LTUESTA', 'RGASPAR',
           'RTANG', 'SLINO', 'TGIL', 'ZCALLE')
     AND UPPER(r.codigo) = 'VALIDACION'
) src
ON (dst.id_usuario = src.id_usuario AND dst.id_rol = src.id_rol)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_usuario, id_rol, activo)
  VALUES (src.id_usuario, src.id_rol, 1);

UPDATE equipo_usuario eu
   SET eu.activo = 0,
       eu.es_responsable = 0,
       eu.modificado_en = SYSTIMESTAMP
 WHERE eu.id_equipo = (SELECT id_equipo FROM equipo WHERE UPPER(codigo) = 'EQ_NOTIFICACION')
   AND eu.activo = 1
   AND eu.id_usuario IN (
         SELECT id_usuario FROM usuario
          WHERE UPPER(username) IN (
            'EALARCON', 'HDELAGUILA', 'LTUESTA', 'RGASPAR',
            'RTANG', 'SLINO', 'TGIL', 'ZCALLE'));

MERGE INTO equipo_usuario dst
USING (
  SELECT u.id_usuario AS id_usuario, eq.id_equipo AS id_equipo
    FROM usuario u
    CROSS JOIN equipo eq
   WHERE UPPER(u.username) IN (
           'EALARCON', 'HDELAGUILA', 'LTUESTA', 'RGASPAR',
           'RTANG', 'SLINO', 'TGIL', 'ZCALLE')
     AND UPPER(eq.codigo) = 'EQ_VALIDACION'
) src
ON (dst.id_usuario = src.id_usuario AND dst.id_equipo = src.id_equipo)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_usuario, id_equipo, es_responsable, activo)
  VALUES (src.id_usuario, src.id_equipo, 0, 1);

/* ------------------------------------------------------------
   2) Grupo Publicacion (2 personas): equipo EQ_NOTIFICACION ->
      EQ_PUBLICACION (rol NOTIFICACION se mantiene, ver punto 2 de la
      cabecera)
   ------------------------------------------------------------ */

UPDATE equipo_usuario eu
   SET eu.activo = 0,
       eu.es_responsable = 0,
       eu.modificado_en = SYSTIMESTAMP
 WHERE eu.id_equipo = (SELECT id_equipo FROM equipo WHERE UPPER(codigo) = 'EQ_NOTIFICACION')
   AND eu.activo = 1
   AND eu.id_usuario IN (
         SELECT id_usuario FROM usuario
          WHERE UPPER(username) IN ('CCOTRINA', 'YRIVERA'));

MERGE INTO equipo_usuario dst
USING (
  SELECT u.id_usuario AS id_usuario, eq.id_equipo AS id_equipo
    FROM usuario u
    CROSS JOIN equipo eq
   WHERE UPPER(u.username) IN ('CCOTRINA', 'YRIVERA')
     AND UPPER(eq.codigo) = 'EQ_PUBLICACION'
) src
ON (dst.id_usuario = src.id_usuario AND dst.id_equipo = src.id_equipo)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_usuario, id_equipo, es_responsable, activo)
  VALUES (src.id_usuario, src.id_equipo, 0, 1);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT u.username, u.nombre_completo,
       LISTAGG(DISTINCT r.codigo, ', ') WITHIN GROUP (ORDER BY r.codigo) AS roles,
       LISTAGG(DISTINCT eq.codigo, ', ') WITHIN GROUP (ORDER BY eq.codigo) AS equipos
  FROM usuario u
  LEFT JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario AND ur.activo = 1
  LEFT JOIN rol r ON r.id_rol = ur.id_rol
  LEFT JOIN equipo_usuario eu ON eu.id_usuario = u.id_usuario AND eu.activo = 1
  LEFT JOIN equipo eq ON eq.id_equipo = eu.id_equipo
 WHERE UPPER(u.username) IN (
         'EALARCON', 'HDELAGUILA', 'LTUESTA', 'RGASPAR', 'RTANG', 'SLINO', 'TGIL', 'ZCALLE',
         'ETUPAYACHI', 'GALE', 'GVASQUEZ', 'KMORALES', 'LMATOS', 'MVASQUEZ', 'NTUPACYUPANQUI',
         'CCOTRINA', 'YRIVERA')
 GROUP BY u.username, u.nombre_completo
 ORDER BY equipos, u.username;

SELECT eq.codigo, COUNT(*) AS miembros
  FROM equipo_usuario eu
  JOIN equipo eq ON eq.id_equipo = eu.id_equipo
 WHERE eu.activo = 1 AND UPPER(eq.codigo) IN ('EQ_NOTIFICACION', 'EQ_VALIDACION', 'EQ_PUBLICACION')
 GROUP BY eq.codigo
 ORDER BY eq.codigo;
