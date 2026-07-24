/* ============================================================
   SCRIPT 88 - Miembros de EQ_VERIFICACION y EQ_SUPERVISION
   Ejecutar conectado como SDRERC_APP.

   Contexto (reportado por el usuario con captura de "Equipo Juridico"):
   ambos equipos aparecen con 0 miembros porque el script 63 nunca los
   poblo (solo asigno EQ_ANALISIS/EQ_EJECUCION/EQ_NOTIFICACION/etc. segun
   el area de cada persona en el Excel de personal).

   PARTE 1 - EQ_VERIFICACION:
   Pedido del usuario: "eq. verificacion deberia cargar los supervisores
   de analisis". No se hardcodea una lista de usernames: se toma
   dinamicamente "quien supervisa hoy a un abogado de EQ_ANALISIS" desde
   USUARIO_SUPERVISION (igual filosofia que el script 68, que tampoco usa
   lista fija). Esto significa que esta PARTE 1 depende de que
   USUARIO_SUPERVISION ya tenga datos: requiere haber ejecutado antes el
   script 87 (o el 64) de carga de supervision; si no, no inserta nada
   (no falla, simplemente no hay filas que agregar todavia).
   Con los datos ya revisados en el script 87, los supervisores
   resultantes de EQ_ANALISIS son: ARIADNA MACHUCA, JULIO SANTIAGO, JUANA
   PINTO, GLENYS BRUNO, LIUBEN CELI, FLOR ZAPANA (6 personas; LISSET
   CALIXTRO y SHIRLEY DIOSES no entran porque supervisan Recepcion/
   Asignacion y Notificacion, no Analisis).

   PARTE 2 - EQ_SUPERVISION:
   Pedido explicito del usuario: "en eq. supervision solo debe aparecer
   shirley dioses como unica usuario, ese es el equipo quien se va a
   encargar de las asignaciones en la Bandeja de Asignacion del modulo
   de Notificacion". A diferencia de la Parte 1, aqui SI se hardcodea
   (unico miembro pedido explicitamente, no hay regla derivable de otra
   tabla). Coincide con el rol SUPERVISOR_NOTIFICACION que ya tiene
   sdioses en el roster del script 63.

   Idempotente: MERGE por (id_equipo, id_usuario) en ambas partes, se
   puede re-ejecutar sin duplicar. No se ejecuto contra ninguna base de
   datos.
   ============================================================ */

-- PARTE 1: EQ_VERIFICACION = supervisores activos de abogados de EQ_ANALISIS
MERGE INTO equipo_usuario dst
USING (
  SELECT DISTINCT eq_dest.id_equipo AS id_equipo, sup.id_usuario AS id_usuario
    FROM usuario_supervision us
    JOIN equipo_usuario eu_analisis
      ON eu_analisis.id_usuario = us.id_abogado AND eu_analisis.activo = 1
    JOIN equipo eq_analisis
      ON eq_analisis.id_equipo = eu_analisis.id_equipo AND UPPER(eq_analisis.codigo) = 'EQ_ANALISIS'
    JOIN usuario sup ON sup.id_usuario = us.id_supervisor AND sup.activo = 1
    JOIN equipo eq_dest ON UPPER(eq_dest.codigo) = 'EQ_VERIFICACION'
   WHERE us.activo = 1
) src
ON (dst.id_usuario = src.id_usuario AND dst.id_equipo = src.id_equipo)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_equipo, id_usuario, es_responsable, activo, creado_en)
  VALUES (src.id_equipo, src.id_usuario, 0, 1, SYSTIMESTAMP);

-- PARTE 2: EQ_SUPERVISION = solo Shirley Dioses
MERGE INTO equipo_usuario dst
USING (
  SELECT eq_dest.id_equipo AS id_equipo, u.id_usuario AS id_usuario
    FROM usuario u
    JOIN equipo eq_dest ON UPPER(eq_dest.codigo) = 'EQ_SUPERVISION'
   WHERE UPPER(u.username) = 'SDIOSES' AND u.activo = 1
) src
ON (dst.id_usuario = src.id_usuario AND dst.id_equipo = src.id_equipo)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_equipo, id_usuario, es_responsable, activo, creado_en)
  VALUES (src.id_equipo, src.id_usuario, 0, 1, SYSTIMESTAMP);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT eq.codigo, COUNT(*) AS miembros
  FROM equipo_usuario eu
  JOIN equipo eq ON eq.id_equipo = eu.id_equipo
 WHERE eu.activo = 1 AND UPPER(eq.codigo) IN ('EQ_ANALISIS', 'EQ_VERIFICACION', 'EQ_SUPERVISION')
 GROUP BY eq.codigo
 ORDER BY eq.codigo;

SELECT eq.codigo AS equipo, u.username, u.nombre_completo
  FROM equipo_usuario eu
  JOIN equipo eq ON eq.id_equipo = eu.id_equipo
  JOIN usuario u ON u.id_usuario = eu.id_usuario
 WHERE eu.activo = 1 AND UPPER(eq.codigo) IN ('EQ_VERIFICACION', 'EQ_SUPERVISION')
 ORDER BY eq.codigo, u.username;
