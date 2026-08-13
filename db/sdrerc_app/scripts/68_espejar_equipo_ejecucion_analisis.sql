/* ============================================================
   SCRIPT 68 - Espejar EQ_EJECUCION con los miembros de EQ_ANALISIS
   Ejecutar conectado como SDRERC_APP.

   Contexto: Ejecucion.md/CLAUDE.md - "El responsable debe ser el mismo
   abogado que realizo Analisis". Hoy EQ_EJECUCION tiene 0 miembros
   porque script 63 solo asigno EQ_ANALISIS a los abogados. Este script
   agrega a cada usuario activo de EQ_ANALISIS tambien a EQ_EJECUCION
   (mismo id_usuario, mismo es_responsable), sin tocar EQ_ANALISIS.

   No usa una lista fija de usernames: toma "quien esta hoy en
   EQ_ANALISIS" directamente de equipo_usuario, asi que si mas adelante
   se agregan o quitan abogados de EQ_ANALISIS, basta re-ejecutar este
   script para que EQ_EJECUCION quede sincronizado otra vez.

   Idempotente: MERGE por (id_equipo, id_usuario), se puede re-ejecutar
   sin duplicar. No se ejecuto contra ninguna base de datos.
   ============================================================ */

MERGE INTO equipo_usuario dst
USING (
  SELECT eq_dest.id_equipo AS id_equipo, eu.id_usuario AS id_usuario, eu.es_responsable AS es_responsable
    FROM equipo_usuario eu
    JOIN equipo eq_origen ON eq_origen.id_equipo = eu.id_equipo AND UPPER(eq_origen.codigo) = 'EQ_ANALISIS'
    JOIN equipo eq_dest ON UPPER(eq_dest.codigo) = 'EQ_EJECUCION'
   WHERE eu.activo = 1
) src
ON (dst.id_usuario = src.id_usuario AND dst.id_equipo = src.id_equipo)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.es_responsable = src.es_responsable,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_equipo, id_usuario, es_responsable, activo, creado_en)
  VALUES (src.id_equipo, src.id_usuario, src.es_responsable, 1, SYSTIMESTAMP);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT eq.codigo, COUNT(*) AS miembros
  FROM equipo_usuario eu
  JOIN equipo eq ON eq.id_equipo = eu.id_equipo
 WHERE eu.activo = 1 AND UPPER(eq.codigo) IN ('EQ_ANALISIS', 'EQ_EJECUCION')
 GROUP BY eq.codigo
 ORDER BY eq.codigo;

-- Confirmar que EQ_ANALISIS y EQ_EJECUCION quedan con exactamente los mismos usuarios
SELECT u.username, u.nombre_completo
  FROM usuario u
 WHERE EXISTS (
         SELECT 1 FROM equipo_usuario eu JOIN equipo eq ON eq.id_equipo = eu.id_equipo
          WHERE eu.id_usuario = u.id_usuario AND eu.activo = 1 AND UPPER(eq.codigo) = 'EQ_ANALISIS')
   AND NOT EXISTS (
         SELECT 1 FROM equipo_usuario eu JOIN equipo eq ON eq.id_equipo = eu.id_equipo
          WHERE eu.id_usuario = u.id_usuario AND eu.activo = 1 AND UPPER(eq.codigo) = 'EQ_EJECUCION')
 ORDER BY u.username;
