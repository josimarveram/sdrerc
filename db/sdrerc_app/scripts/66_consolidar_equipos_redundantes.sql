/* ============================================================
   SCRIPT 66 - Consolidar equipos redundantes en EQUIPO
   Ejecutar conectado como SDRERC_APP.

   Contexto: la pantalla Administracion > Equipo Juridico
   (JPanelEquipoJuridicoV2) permite crear filas nuevas directamente
   en la tabla EQUIPO (la misma tabla sembrada por 09 con
   EQ_REGISTRO/EQ_ASIGNACION/EQ_ANALISIS/etc. y usada por
   EQUIPO_USUARIO para enrutar trabajo). Su tooltip de ayuda sugeria
   como ejemplo el prefijo "EQUIPO_" (p.ej. EQUIPO_ANALISIS), distinto
   al prefijo "EQ_" ya sembrado para el mismo fin. Resultado: equipos
   duplicados para la misma etapa funcional (EQUIPO_ANALISIS junto a
   EQ_ANALISIS, etc.), confirmado por el usuario. El tooltip ya se
   corrigio en el codigo Java (usa EQ_ como ejemplo y lista los
   equipos ya sembrados) para no repetir el problema.

   Este script consolida los duplicados existentes hacia el equipo
   canonico (el sembrado por 09/44), sin eliminar fisicamente nada
   (regla del proyecto: nunca eliminar fisicamente equipos):
   1) Por cada par (redundante -> canonico), migra las membresias
      activas de EQUIPO_USUARIO del redundante al canonico
      (conserva es_responsable si alguno de los dos ya lo tenia).
   2) Desactiva las membresias que quedaron en el equipo redundante.
   3) Desactiva el equipo redundante (activo=0), deja constancia en
      su descripcion.

   Pares consolidados:
   - EQUIPO_ANALISIS      -> EQ_ANALISIS
   - EQUIPO_VERIFICACION  -> EQ_VERIFICACION
   - EQUIPO_EJECUCION     -> EQ_EJECUCION
   - EQUIPO_NOTIFICACION  -> EQ_NOTIFICACION
   - EQ_FIRMA_EMISION     -> EQ_VERIFICACION (Firma/Emision ya esta
     integrada dentro de Verificacion, no es modulo aparte; nadie
     deberia seguir teniendo este equipo como destino de trabajo)

   Si alguno de los codigos redundantes no existe en la base (porque
   nunca se creo, o ya fue corregido antes), el script simplemente lo
   omite y sigue con el resto: es seguro re-ejecutarlo.

   No se ejecuto contra ninguna base de datos.
   ============================================================ */

DECLARE
  CURSOR c_pares IS
    SELECT 'EQUIPO_ANALISIS' AS codigo_redundante, 'EQ_ANALISIS' AS codigo_canonico FROM dual
    UNION ALL SELECT 'EQUIPO_VERIFICACION', 'EQ_VERIFICACION' FROM dual
    UNION ALL SELECT 'EQUIPO_EJECUCION', 'EQ_EJECUCION' FROM dual
    UNION ALL SELECT 'EQUIPO_NOTIFICACION', 'EQ_NOTIFICACION' FROM dual
    UNION ALL SELECT 'EQ_FIRMA_EMISION', 'EQ_VERIFICACION' FROM dual;

  v_id_redundante equipo.id_equipo%TYPE;
  v_id_canonico   equipo.id_equipo%TYPE;
  v_migrados      PLS_INTEGER;
BEGIN
  FOR r IN c_pares LOOP
    BEGIN
      SELECT id_equipo INTO v_id_redundante
        FROM equipo
       WHERE UPPER(codigo) = r.codigo_redundante;
    EXCEPTION
      WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE(
          'Sin cambios: ' || r.codigo_redundante || ' no existe, nada que consolidar.');
        CONTINUE;
    END;

    SELECT id_equipo INTO v_id_canonico
      FROM equipo
     WHERE UPPER(codigo) = r.codigo_canonico;

    -- 1) Migrar membresias activas del equipo redundante al canonico
    MERGE INTO equipo_usuario dst
    USING (
      SELECT eu.id_usuario AS id_usuario, v_id_canonico AS id_equipo, eu.es_responsable AS es_responsable
        FROM equipo_usuario eu
       WHERE eu.id_equipo = v_id_redundante
         AND eu.activo = 1
    ) src
    ON (dst.id_usuario = src.id_usuario AND dst.id_equipo = src.id_equipo)
    WHEN MATCHED THEN UPDATE
      SET dst.activo = 1,
          dst.es_responsable = GREATEST(NVL(dst.es_responsable, 0), NVL(src.es_responsable, 0)),
          dst.modificado_en = SYSTIMESTAMP
    WHEN NOT MATCHED THEN
      INSERT (id_usuario, id_equipo, es_responsable, activo)
      VALUES (src.id_usuario, src.id_equipo, src.es_responsable, 1);

    v_migrados := SQL%ROWCOUNT;

    -- 2) Desactivar membresias que quedaron en el equipo redundante
    UPDATE equipo_usuario
       SET activo = 0,
           es_responsable = 0,
           modificado_en = SYSTIMESTAMP
     WHERE id_equipo = v_id_redundante
       AND activo = 1;

    -- 3) Desactivar el equipo redundante (no se elimina fisicamente)
    UPDATE equipo
       SET activo = 0,
           descripcion = 'Redundante con ' || r.codigo_canonico || '; consolidado por script 66 el '
                         || TO_CHAR(SYSDATE, 'DD/MM/YYYY') || '.',
           modificado_en = SYSTIMESTAMP
     WHERE id_equipo = v_id_redundante;

    DBMS_OUTPUT.PUT_LINE(
      'Consolidado: ' || r.codigo_redundante || ' -> ' || r.codigo_canonico ||
      ' (' || v_migrados || ' membresias migradas/actualizadas).');
  END LOOP;

  COMMIT;
END;


/* ============================================================
   Verificacion posterior
   ============================================================ */

-- Equipos redundantes: deben quedar inactivos (o no existir)
SELECT codigo, nombre, activo, descripcion
  FROM equipo
 WHERE UPPER(codigo) IN ('EQUIPO_ANALISIS', 'EQUIPO_VERIFICACION', 'EQUIPO_EJECUCION',
                          'EQUIPO_NOTIFICACION', 'EQ_FIRMA_EMISION')
 ORDER BY codigo;

-- Catalogo completo de equipos activos tras la consolidacion
SELECT codigo, nombre, activo
  FROM equipo
 ORDER BY activo DESC, codigo;

-- Cuantos usuarios activos quedo cada equipo canonico tras la migracion
SELECT eq.codigo AS equipo, COUNT(eu.id_usuario) AS usuarios
  FROM equipo eq
  LEFT JOIN equipo_usuario eu ON eu.id_equipo = eq.id_equipo AND eu.activo = 1
 WHERE eq.activo = 1
 GROUP BY eq.codigo
 ORDER BY eq.codigo;
