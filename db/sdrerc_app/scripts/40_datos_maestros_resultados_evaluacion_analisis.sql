/* ============================================================
   SCRIPT 40 - Datos maestros de resultados de evaluacion (Analisis)

   Agrega a tipo_resultado_evaluacion los codigos OBSERVADO y
   NO_CORRESPONDE que hasta ahora estaban hardcodeados en Java
   (AnalisisExpedienteService.listarResultadosAnalisis), mas EDICTO,
   FALTA_SUSTENTO e INDAGATORIO, para que el combo "Resultado" del
   panel de Analisis se cargue integramente desde base de datos.

   Ejecutar conectado como SDRERC_APP cuando se autorice.
   Script idempotente, no destructivo y sin datos transaccionales.
   No ejecutar automaticamente: requiere instruccion explicita aparte.
   ============================================================ */

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*)
    INTO v_count
    FROM tipo_resultado_evaluacion
   WHERE UPPER(codigo) = 'OBSERVADO';

  IF v_count = 0 THEN
    INSERT INTO tipo_resultado_evaluacion (codigo, nombre, activo, creado_en)
    VALUES ('OBSERVADO', 'Observado / requiere subsanacion', 1, SYSTIMESTAMP);
  ELSE
    UPDATE tipo_resultado_evaluacion
       SET nombre = 'Observado / requiere subsanacion',
           activo = 1,
           modificado_en = SYSTIMESTAMP
     WHERE UPPER(codigo) = 'OBSERVADO';
  END IF;
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*)
    INTO v_count
    FROM tipo_resultado_evaluacion
   WHERE UPPER(codigo) = 'NO_CORRESPONDE';

  IF v_count = 0 THEN
    INSERT INTO tipo_resultado_evaluacion (codigo, nombre, activo, creado_en)
    VALUES ('NO_CORRESPONDE', 'No corresponde a SDRERC', 1, SYSTIMESTAMP);
  ELSE
    UPDATE tipo_resultado_evaluacion
       SET nombre = 'No corresponde a SDRERC',
           activo = 1,
           modificado_en = SYSTIMESTAMP
     WHERE UPPER(codigo) = 'NO_CORRESPONDE';
  END IF;
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*)
    INTO v_count
    FROM tipo_resultado_evaluacion
   WHERE UPPER(codigo) = 'EDICTO';

  IF v_count = 0 THEN
    INSERT INTO tipo_resultado_evaluacion (codigo, nombre, activo, creado_en)
    VALUES ('EDICTO', 'Edicto', 1, SYSTIMESTAMP);
  ELSE
    UPDATE tipo_resultado_evaluacion
       SET nombre = 'Edicto',
           activo = 1,
           modificado_en = SYSTIMESTAMP
     WHERE UPPER(codigo) = 'EDICTO';
  END IF;
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*)
    INTO v_count
    FROM tipo_resultado_evaluacion
   WHERE UPPER(codigo) = 'FALTA_SUSTENTO';

  IF v_count = 0 THEN
    INSERT INTO tipo_resultado_evaluacion (codigo, nombre, activo, creado_en)
    VALUES ('FALTA_SUSTENTO', 'Falta sustento', 1, SYSTIMESTAMP);
  ELSE
    UPDATE tipo_resultado_evaluacion
       SET nombre = 'Falta sustento',
           activo = 1,
           modificado_en = SYSTIMESTAMP
     WHERE UPPER(codigo) = 'FALTA_SUSTENTO';
  END IF;
END;
/

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*)
    INTO v_count
    FROM tipo_resultado_evaluacion
   WHERE UPPER(codigo) = 'INDAGATORIO';

  IF v_count = 0 THEN
    INSERT INTO tipo_resultado_evaluacion (codigo, nombre, activo, creado_en)
    VALUES ('INDAGATORIO', 'Indagatorio', 1, SYSTIMESTAMP);
  ELSE
    UPDATE tipo_resultado_evaluacion
       SET nombre = 'Indagatorio',
           activo = 1,
           modificado_en = SYSTIMESTAMP
     WHERE UPPER(codigo) = 'INDAGATORIO';
  END IF;
END;
/

SELECT codigo, nombre, activo
  FROM tipo_resultado_evaluacion
 ORDER BY codigo;
