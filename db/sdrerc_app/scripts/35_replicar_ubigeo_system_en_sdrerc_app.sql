/* ============================================================
   SCRIPT 35 - Replicar estructura funcional de ubigeo en SDRERC_APP

   Conexion de ejecucion:
   - Ejecutar SOLO en XEPDB1 / SDRERC_APP.

   Objetivo:
   - Mantener compatibilidad con la estructura V2 actual
     (id_ubigeo_*, codigo, nombre, activo).
   - Agregar en UBIGEO_* los campos equivalentes al maestro legacy
     DEPARTAMENTO / PROVINCIA / DISTRITO.
   - Preparar SDRERC_APP para recibir la data exportada desde XE/SYSTEM.

   Importante:
   - Este script NO lee tablas SYSTEM ni usa DB LINK.
   - Para la carga de registros usar ademas:
     db/sdrerc_app/scripts/37_generar_script_intermedio_ubigeo_system.sql
   - Amplia los campos codigo legacy creados por el script 34 para
     soportar codigos oficiales sin truncamiento.
   - No usa DROP, DELETE ni TRUNCATE.
   ============================================================ */

DECLARE
  v_length NUMBER;
BEGIN
  SELECT data_length INTO v_length
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DEPARTAMENTO'
     AND column_name = 'CODIGO';
  IF v_length < 10 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento MODIFY codigo VARCHAR2(10)';
  END IF;

  SELECT data_length INTO v_length
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'CODIGO';
  IF v_length < 10 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia MODIFY codigo VARCHAR2(10)';
  END IF;

  SELECT data_length INTO v_length
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'CODIGO';
  IF v_length < 10 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito MODIFY codigo VARCHAR2(10)';
  END IF;
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DEPARTAMENTO'
     AND column_name = 'ID_DEPARTAMENTO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento ADD id_departamento NUMBER';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DEPARTAMENTO'
     AND column_name = 'CODIGO_DEPARTAMENTO_INEI';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento ADD codigo_departamento_inei VARCHAR2(10)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DEPARTAMENTO'
     AND column_name = 'CODIGO_DEPARTAMENTO_RENIEC';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento ADD codigo_departamento_reniec VARCHAR2(10)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DEPARTAMENTO'
     AND column_name = 'DESCRIPCION';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento ADD descripcion VARCHAR2(100)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DEPARTAMENTO'
     AND column_name = 'ABREVIATURA';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento ADD abreviatura VARCHAR2(7)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DEPARTAMENTO'
     AND column_name = 'ACTIVE';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento ADD active NUMBER(1)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DEPARTAMENTO'
     AND column_name = 'FECHA_REGISTRO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento ADD fecha_registro DATE';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DEPARTAMENTO'
     AND column_name = 'USUARIO_REGISTRO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento ADD usuario_registro VARCHAR2(20)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DEPARTAMENTO'
     AND column_name = 'FECHA_MODIFICACION';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento ADD fecha_modificacion DATE';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DEPARTAMENTO'
     AND column_name = 'USUARIO_MODIFICACION';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento ADD usuario_modificacion VARCHAR2(20)';
  END IF;
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'ID_PROVINCIA';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD id_provincia NUMBER';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'ID_DEPARTAMENTO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD id_departamento NUMBER';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'CODIGO_PROVINCIA_INEI';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD codigo_provincia_inei VARCHAR2(10)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'CODIGO_PROVINCIA_RENIEC';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD codigo_provincia_reniec VARCHAR2(10)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'DESCRIPCION';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD descripcion VARCHAR2(100)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'ABREVIATURA';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD abreviatura VARCHAR2(7)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'ACTIVE';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD active NUMBER(1)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'FECHA_REGISTRO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD fecha_registro DATE';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'USUARIO_REGISTRO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD usuario_registro VARCHAR2(20)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'FECHA_MODIFICACION';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD fecha_modificacion DATE';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_PROVINCIA'
     AND column_name = 'USUARIO_MODIFICACION';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD usuario_modificacion VARCHAR2(20)';
  END IF;
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'ID_DISTRITO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD id_distrito NUMBER';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'ID_DEPARTAMENTO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD id_departamento NUMBER';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'ID_PROVINCIA';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD id_provincia NUMBER';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'CODIGO_DISTRITO_INEI';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD codigo_distrito_inei VARCHAR2(10)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'CODIGO_DISTRITO_RENIEC';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD codigo_distrito_reniec VARCHAR2(10)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'DESCRIPCION';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD descripcion VARCHAR2(100)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'ABREVIATURA';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD abreviatura VARCHAR2(7)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'ACTIVE';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD active NUMBER(1)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'FECHA_REGISTRO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD fecha_registro DATE';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'USUARIO_REGISTRO';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD usuario_registro VARCHAR2(20)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'FECHA_MODIFICACION';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD fecha_modificacion DATE';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'UBIGEO_DISTRITO'
     AND column_name = 'USUARIO_MODIFICACION';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD usuario_modificacion VARCHAR2(20)';
  END IF;
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count
    FROM user_constraints
   WHERE constraint_name = 'UK_UBIGEO_DEP_ID_SYSTEM';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_departamento ADD CONSTRAINT uk_ubigeo_dep_id_system UNIQUE (id_departamento)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_constraints
   WHERE constraint_name = 'UK_UBIGEO_PROV_ID_SYSTEM';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_provincia ADD CONSTRAINT uk_ubigeo_prov_id_system UNIQUE (id_provincia)';
  END IF;

  SELECT COUNT(*) INTO v_count
    FROM user_constraints
   WHERE constraint_name = 'UK_UBIGEO_DIST_ID_SYSTEM';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ubigeo_distrito ADD CONSTRAINT uk_ubigeo_dist_id_system UNIQUE (id_distrito)';
  END IF;
END;


SELECT table_name, column_name, data_type, data_length
  FROM user_tab_columns
 WHERE table_name IN ('UBIGEO_DEPARTAMENTO', 'UBIGEO_PROVINCIA', 'UBIGEO_DISTRITO')
 ORDER BY table_name, column_id;
