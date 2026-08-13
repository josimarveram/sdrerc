/* ============================================================
   SCRIPT 67 - Carga de supervision Recepcion/Asignacion y Notificacion
   Ejecutar conectado como SDRERC_APP.

   Contexto: la pantalla nueva "Personal por supervisor" (Administracion >
   Equipo Juridico) lee USUARIO_SUPERVISION. El script 64 solo cargo esa
   tabla para el area de Analisis (74 relaciones, 6 supervisores:
   amachuca/fzapana/gbruno/jpinto/jsantiago/lceli), porque tomo como
   fuente las hojas "Abogados"/"Abogados 2026" de personal_supervisores.xlsx,
   que solo cubren esa area. Por eso el combo de supervisores no mostraba
   a LISSET CALIXTRO (Recepcion y Asignacion) ni a SHIRLEY DIOSES
   (Notificacion, y tambien Actualizacion del SIRCM/Generacion Expediente
   Digital), aunque el usuario los ve como supervisores validos en su
   Excel.

   Fuente de este script: la MISMA personal_supervisores.xlsx, pero la
   hoja "PERSONAL Y SUPERVISORES " (columna H=AREA, I=SUPERVISOR), que si
   tiene el supervisor persona por persona para TODAS las areas, no solo
   Analisis. Se cargan las relaciones de las areas "Notificacion de
   Documentos", "Recepcion y Asignacion de Expedientes", "Actualizacion
   del SIRCM" y "Generacion Expediente Digital".

   25 relaciones resueltas contra el roster de 118 usuarios (script 63,
   incluye las 2 altas nuevas del punto 5.b de su cabecera):
   - lcalixtro (Recepcion y Asignacion): janastacio, mbazan, rcercado,
     jchuquispuma, rpantoja, frivas, lsanchez, scordova2, iquintana (9).
   - sdioses (Notificacion + SIRCM + Expediente Digital): ealarcon, gale,
     zcalle, ccastro, hdelaguila, tgil, lmatos, epatino, yrivera, mromero,
     rtang, ltuesta, ntupacyupanqui, etupayachi, gvasquez, adiaz (16).
     OJO: adiaz (DIAZ MITMA ANTHONY) esta agrupado bajo el area "Recepcion
     y Asignacion" en el Excel, pero su columna SUPERVISOR individual dice
     "SHIRLEY DIOSES", no "LISSET CALIXTRO"; se respeto el dato de la
     columna, no el area.

   Solo 1 fila de esa hoja queda SIN resolver, no incluida aqui:
     - GASPAR SANCHEZ ROLANDO IVAN (Notificacion) -> su propia celda
       SUPERVISOR trae el texto literal "SUPERVISOR" en vez de un
       nombre; no se puede resolver a una persona sin adivinar.

   Con este script el combo de "Personal por supervisor" pasa de 6 a 8
   supervisores (coincide con el filtro de 8 nombres del Excel del
   usuario: Machuca, Zapana, Bruno, Pinto, Santiago, Calixtro, Celi,
   Dioses).

   Requiere que el script 63 ya haya creado estos usuarios. No pisa ni
   duplica las relaciones ya cargadas por el script 64 (claves distintas:
   ningun abogado de este script pertenece al area de Analisis).

   Idempotente: MERGE por (id_supervisor, id_abogado), se puede
   re-ejecutar sin duplicar. No se ejecuto contra ninguna base de datos.
   ============================================================ */

MERGE INTO usuario_supervision dst
USING (
  SELECT sup.id_usuario AS id_supervisor, ab.id_usuario AS id_abogado
    FROM (
    -- LISSET CALIXTRO (Recepcion y Asignacion de Expedientes)
    SELECT 'janastacio' AS abogado_username, 'lcalixtro' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mbazan' AS abogado_username, 'lcalixtro' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'rcercado' AS abogado_username, 'lcalixtro' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'jchuquispuma' AS abogado_username, 'lcalixtro' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'rpantoja' AS abogado_username, 'lcalixtro' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'frivas' AS abogado_username, 'lcalixtro' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'lsanchez' AS abogado_username, 'lcalixtro' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'scordova2' AS abogado_username, 'lcalixtro' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'iquintana' AS abogado_username, 'lcalixtro' AS supervisor_username FROM dual
    -- SHIRLEY DIOSES (Notificacion, Actualizacion SIRCM, Expediente Digital;
    -- incluye a DIAZ MITMA ANTHONY (adiaz), que aunque esta agrupado bajo el
    -- area "Recepcion y Asignacion" en el Excel, su columna SUPERVISOR
    -- individual dice "SHIRLEY DIOSES", no "LISSET CALIXTRO")
    UNION ALL
    SELECT 'adiaz' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'ealarcon' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'gale' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'zcalle' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'ccastro' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'hdelaguila' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'tgil' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'lmatos' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'epatino' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'yrivera' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'mromero' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'rtang' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'ltuesta' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'ntupacyupanqui' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'etupayachi' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
    UNION ALL
    SELECT 'gvasquez' AS abogado_username, 'sdioses' AS supervisor_username FROM dual
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
       COUNT(*) AS personas_supervisadas
  FROM usuario_supervision us
  JOIN usuario sup ON sup.id_usuario = us.id_supervisor
 WHERE us.activo = 1
 GROUP BY sup.username, sup.nombre_completo
 ORDER BY personas_supervisadas DESC;

SELECT ab.username AS supervisado, ab.nombre_completo AS supervisado_nombre,
       sup.username AS supervisor, sup.nombre_completo AS supervisor_nombre
  FROM usuario_supervision us
  JOIN usuario ab ON ab.id_usuario = us.id_abogado
  JOIN usuario sup ON sup.id_usuario = us.id_supervisor
 WHERE us.activo = 1
 ORDER BY sup.username, ab.username;

SELECT COUNT(*) AS total_relaciones_supervision FROM usuario_supervision WHERE activo = 1;
SELECT COUNT(DISTINCT id_supervisor) AS total_supervisores FROM usuario_supervision WHERE activo = 1;
