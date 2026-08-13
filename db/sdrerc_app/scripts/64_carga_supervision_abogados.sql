/* ============================================================
   SCRIPT 64 - Carga de supervision abogado/supervisor
   Ejecutar conectado como SDRERC_APP.

   Fuente: docs/arquitectura_app/personal_supervisores.xlsx, hojas
   "Abogados" (asignacion base) y "Abogados 2026" (reasignacion vigente
   para 2026). Cuando un abogado aparece en ambas hojas (aunque con
   variante de escritura del nombre) se usa el supervisor de
   "Abogados 2026" por ser el acuerdo mas reciente, igual criterio de
   precedencia que las actas de reunion. La deduplicacion se hizo
   despues de resolver cada nombre a su USERNAME (no comparando texto
   crudo), porque varias personas aparecen con distinta puntuacion o
   una palabra de mas/menos entre ambas hojas (ver overrides abajo).

   Requiere haber ejecutado antes el script 63 (carga de los 111
   usuarios): este script solo agrega la relacion USUARIO_SUPERVISION
   entre usuarios que YA deben existir; no crea usuarios nuevos.

   74 relaciones abogado-supervisor resueltas (una por abogado, ya
   deduplicadas). Nombres de supervisor en el Excel en orden
   "Nombre Apellido" (ej. "ARIADNA MACHUCA"), resueltos a mano contra
   el roster del script 63: SHIRLEY DIOSES=sdioses,
   JULIO SANTIAGO=jsantiago, JUANA PINTO=jpinto, GLENYS BRUNO=gbruno,
   ARIADNA MACHUCA=amachuca, LIUBEN CELI=lceli, FLOR ZAPANA=fzapana,
   LISSET CALIXTRO=lcalixtro.

   5 abogados de "Abogados 2026" (la hoja vigente) no existian en el
   Excel original Personal_SDRERC_Usuarios_Herramienta_Interna.xlsx,
   por lo que el script 63 no los tenia dados de alta. Se agregaron ahi
   como alta nueva (sin DNI/telefono, ver punto 5 de la cabecera del
   script 63) y aqui ya se incluye su relacion de supervision:
     - LOPEZ CHIQUIHUANCA MONICA PATRICIA (mlopez) -> supervisor JUANA PINTO (jpinto)
     - MONTENEGRO CARBAJAL SANDRA MARIBEL (smontenegro) -> supervisor ARIADNA MACHUCA (amachuca)
     - PUSE SANTILLAN JENIFFER MILAGROS (jpuse) -> supervisor GLENYS BRUNO (gbruno)
     - CORTEZ PITA ALEXANDRA KAREN (acortez) -> supervisor JULIO SANTIAGO (jsantiago)
     - FERNANDEZ OBANDO GINO AXCEL (gfernandez) -> supervisor JULIO SANTIAGO (jsantiago)

   Requiere que el script 63 ya haya creado estos 5 usuarios (misma
   dependencia que para el resto del roster).

   Nota: este script NO toca ni reemplaza el rol/equipo ya asignado por
   el script 63 (ABOGADO + EQ_ANALISIS, etc.); solo agrega el vinculo
   adicional supervisor-abogado en USUARIO_SUPERVISION, que es una
   relacion de reporte funcional distinta de la asignacion de
   equipo/rol.

   Idempotente: MERGE por (id_supervisor, id_abogado), se puede
   re-ejecutar sin duplicar. No se ejecuto contra ninguna base de datos.
   ============================================================ */

MERGE INTO usuario_supervision dst
USING (
  SELECT sup.id_usuario AS id_supervisor, ab.id_usuario AS id_abogado
    FROM (
    SELECT 'aalvarez' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'aarmis' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'acastillo' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'achipoco' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'azegarra' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'ccasimiro' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'cchampi' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'ccotrina' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'cgozar' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'cilave' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'clatorre' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'cmilian' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'cramos' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'cvargas' AS abogado_username, 'jpinto' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'dleon' AS abogado_username, 'jpinto' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'dpuch' AS abogado_username, 'lceli' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'earestegui' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'emartinez' AS abogado_username, 'lceli' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'fbardales' AS abogado_username, 'jpinto' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'fpomalia' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'fvasquez' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'grodas' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'hrojas' AS abogado_username, 'jpinto' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'irios' AS abogado_username, 'jpinto' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'jalvarado' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'janastacio' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'jbellmunt' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'jcespedes' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'jcuri' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'jdiaz' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'jguardapuclla' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'jmorales' AS abogado_username, 'jpinto' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'jquerevalu' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'kalhuay' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'kmorales' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'ldelcastillo' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'licochea' AS abogado_username, 'lceli' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'lromani' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mbautista' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mfiestas' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mjimenez' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mmaque' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mpachas' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mparedes' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mreategui' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mreategui2' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'msalvador' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mvasquez' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'ndonayre' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'nquispe' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'ntumialan' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'ntupacyupanqui' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'nvereau' AS abogado_username, 'jpinto' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'pcabana' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'phancco' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'rasto' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'rmendoza' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'rramirez' AS abogado_username, 'jpinto' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'rromero' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'rtapia' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'scordova' AS abogado_username, 'jpinto' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'sfernandez' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'slino' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'smorocho' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'smunante' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'ssalazar' AS abogado_username, 'fzapana' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'tsalazar' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'wdelacruz' AS abogado_username, 'lceli' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'yjimenez' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mlopez' AS abogado_username, 'jpinto' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'smontenegro' AS abogado_username, 'amachuca' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'jpuse' AS abogado_username, 'gbruno' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'acortez' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'gfernandez' AS abogado_username, 'jsantiago' AS supervisor_username FROM dual
    ) pares
    JOIN usuario ab ON UPPER(ab.username) = UPPER(pares.abogado_username)
    JOIN usuario sup ON UPPER(sup.username) = UPPER(pares.supervisor_username)
) src
ON (dst.id_supervisor = src.id_supervisor AND dst.id_abogado = src.id_abogado)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_supervisor, id_abogado, activo, creado_en)
  VALUES (src.id_supervisor, src.id_abogado, 1, SYSTIMESTAMP);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT sup.username AS supervisor, sup.nombre_completo AS supervisor_nombre,
       COUNT(*) AS abogados_supervisados
  FROM usuario_supervision us
  JOIN usuario sup ON sup.id_usuario = us.id_supervisor
 WHERE us.activo = 1
 GROUP BY sup.username, sup.nombre_completo
 ORDER BY abogados_supervisados DESC;

SELECT ab.username AS abogado, ab.nombre_completo AS abogado_nombre,
       sup.username AS supervisor, sup.nombre_completo AS supervisor_nombre
  FROM usuario_supervision us
  JOIN usuario ab ON ab.id_usuario = us.id_abogado
  JOIN usuario sup ON sup.id_usuario = us.id_supervisor
 WHERE us.activo = 1
 ORDER BY sup.username, ab.username;

-- Un abogado no deberia tener mas de un supervisor activo a la vez
SELECT ab.username, COUNT(*) AS supervisores_activos
  FROM usuario_supervision us
  JOIN usuario ab ON ab.id_usuario = us.id_abogado
 WHERE us.activo = 1
 GROUP BY ab.username
HAVING COUNT(*) > 1;

SELECT COUNT(*) AS total_relaciones_supervision FROM usuario_supervision WHERE activo = 1;
