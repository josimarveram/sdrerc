/* ============================================================
   SCRIPT 114 - Asigna Eq. Analisis + Eq. Ejecucion a los abogados
   de "ASIGNADO A ABOGADO" (Excel de carga inicial) que hoy no
   tienen NINGUN equipo activo (Incremento 2/3 de la carga inicial,
   ver v3/CLAUDE.md 26/08/2026).

   Contexto: 42 abogados distintos, identificados correctamente por
   nombre contra USUARIO (incluye usuarios inactivos a proposito,
   igual que el batch), quedaron con 0 equipos activos y por eso sus
   expedientes de la carga inicial nunca llegaron a ASIGNACION.
   Decision explicita del usuario (26/08/2026): asignarles el mismo
   combo especial {EQ_ANALISIS, EQ_EJECUCION} que el batch ya sabe
   resolver, SIN distinguir si el USUARIO sigue activo o no - la meta
   es exclusivamente destrabar la reconstruccion historica.

   ADVERTENCIA (confirmada explicitamente con el usuario antes de
   generar este script): EQUIPO_USUARIO es una tabla COMPARTIDA con
   la aplicacion en vivo (V2 y V3 operativo) - esto tambien vuelve a
   estos 42 abogados asignables para trabajo NUEVO real en la Bandeja
   de Asignacion, no solo para la carga inicial.

   Idempotente (MERGE por id_usuario+id_equipo: reactiva la fila si ya
   existia inactiva, inserta si no existia ninguna). No se ejecuta
   automaticamente: requiere autorizacion explicita antes de correr
   contra la base.
   ============================================================ */

MERGE INTO equipo_usuario dst
USING (
  SELECT u.id_usuario, eq.id_equipo
  FROM usuario u
  CROSS JOIN equipo eq
  WHERE eq.codigo IN ('EQ_ANALISIS', 'EQ_EJECUCION')
    AND u.id_usuario IN (
      16,38,88,8,5,44,35,53,60,71,59,89,77,28,96,34,93,105,94,66,
      17,10,15,47,73,27,109,29,50,43,70,49,33,95,86,110,41,24,39,92,42,11
    )
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
   Verificacion posterior: cada uno de los 42 debe mostrar
   exactamente 2 filas activas (EQ_ANALISIS + EQ_EJECUCION).
   ============================================================ */

SELECT eu.id_usuario, u.nombre_completo, eq.codigo, eu.activo
FROM equipo_usuario eu
JOIN usuario u ON u.id_usuario = eu.id_usuario
JOIN equipo eq ON eq.id_equipo = eu.id_equipo
WHERE eu.id_usuario IN (
  16,38,88,8,5,44,35,53,60,71,59,89,77,28,96,34,93,105,94,66,
  17,10,15,47,73,27,109,29,50,43,70,49,33,95,86,110,41,24,39,92,42,11
)
ORDER BY eu.id_usuario, eq.codigo;
