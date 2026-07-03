/* ============================================================
   SCRIPT 36 - Generar carga SQL de ubigeo desde XE / SYSTEM

   Conexion de ejecucion:
   - Ejecutar SOLO en XE con el esquema que ve
     DEPARTAMENTO / PROVINCIA / DISTRITO.

   Objetivo:
   - Generar sentencias MERGE listas para ejecutar en XEPDB1 / SDRERC_APP.
   - Replicar registros del maestro institucional en:
       UBIGEO_DEPARTAMENTO
       UBIGEO_PROVINCIA
       UBIGEO_DISTRITO

   Uso sugerido en DBeaver:
   1. Ejecutar cada SELECT.
   2. Exportar el resultado de la columna SCRIPT_SQL a un archivo .sql.
   3. Unir el contenido en este orden:
      a) cabecera
      b) departamentos
      c) provincias
      d) distritos
      e) pie
   4. Ejecutar el archivo generado en XEPDB1 despues del script 35.

   Notas:
   - No modifica SYSTEM.
   - No usa DROP, DELETE ni TRUNCATE.
   ============================================================ */

SELECT '-- CARGA UBIGEO GENERADA DESDE XE/SYSTEM - ejecutar en XEPDB1/SDRERC_APP' AS script_sql FROM dual
UNION ALL
SELECT 'ALTER SESSION SET NLS_DATE_FORMAT = ''YYYY-MM-DD HH24:MI:SS'';' FROM dual
UNION ALL
SELECT '' FROM dual;

SELECT
  'MERGE INTO ubigeo_departamento t USING (SELECT '
  || id_departamento || ' id_departamento, '''
  || REPLACE(codigo_departamento_inei, '''', '''''') || ''' codigo_departamento_inei, '
  || CASE
       WHEN codigo_departamento_reniec IS NULL THEN 'NULL'
       ELSE '''' || REPLACE(codigo_departamento_reniec, '''', '''''') || ''''
     END || ' codigo_departamento_reniec, '''
  || REPLACE(descripcion, '''', '''''') || ''' descripcion, '
  || CASE
       WHEN abreviatura IS NULL THEN 'NULL'
       ELSE '''' || REPLACE(abreviatura, '''', '''''') || ''''
     END || ' abreviatura, '
  || NVL(TO_CHAR(active), '0') || ' active, '
  || 'TO_DATE(''' || TO_CHAR(fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'') fecha_registro, '''
  || REPLACE(usuario_registro, '''', '''''') || ''' usuario_registro, '
  || CASE
       WHEN fecha_modificacion IS NULL THEN 'NULL'
       ELSE 'TO_DATE(''' || TO_CHAR(fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')'
     END || ' fecha_modificacion, '
  || CASE
       WHEN usuario_modificacion IS NULL THEN 'NULL'
       ELSE '''' || REPLACE(usuario_modificacion, '''', '''''') || ''''
     END || ' usuario_modificacion FROM dual) s '
  || 'ON (t.id_departamento = s.id_departamento) '
  || 'WHEN MATCHED THEN UPDATE SET '
  || 't.codigo_departamento_inei = s.codigo_departamento_inei, '
  || 't.codigo_departamento_reniec = s.codigo_departamento_reniec, '
  || 't.descripcion = s.descripcion, '
  || 't.abreviatura = s.abreviatura, '
  || 't.active = s.active, '
  || 't.fecha_registro = s.fecha_registro, '
  || 't.usuario_registro = s.usuario_registro, '
  || 't.fecha_modificacion = s.fecha_modificacion, '
  || 't.usuario_modificacion = s.usuario_modificacion, '
  || 't.codigo = s.codigo_departamento_inei, '
  || 't.nombre = s.descripcion, '
  || 't.activo = s.active, '
  || 't.creado_en = NVL(t.creado_en, CAST(s.fecha_registro AS TIMESTAMP)), '
  || 't.modificado_en = CAST(s.fecha_modificacion AS TIMESTAMP) '
  || 'WHEN NOT MATCHED THEN INSERT '
  || '(id_departamento, codigo_departamento_inei, codigo_departamento_reniec, descripcion, abreviatura, active, fecha_registro, usuario_registro, fecha_modificacion, usuario_modificacion, codigo, nombre, activo, creado_en, modificado_en) VALUES '
  || '(s.id_departamento, s.codigo_departamento_inei, s.codigo_departamento_reniec, s.descripcion, s.abreviatura, s.active, s.fecha_registro, s.usuario_registro, s.fecha_modificacion, s.usuario_modificacion, s.codigo_departamento_inei, s.descripcion, s.active, CAST(s.fecha_registro AS TIMESTAMP), CAST(s.fecha_modificacion AS TIMESTAMP));'
  AS script_sql
FROM departamento
ORDER BY id_departamento;

SELECT
  'MERGE INTO ubigeo_provincia t USING (SELECT '
  || id_provincia || ' id_provincia, '
  || id_departamento || ' id_departamento, '''
  || REPLACE(codigo_provincia_inei, '''', '''''') || ''' codigo_provincia_inei, '
  || CASE
       WHEN codigo_provincia_reniec IS NULL THEN 'NULL'
       ELSE '''' || REPLACE(codigo_provincia_reniec, '''', '''''') || ''''
     END || ' codigo_provincia_reniec, '''
  || REPLACE(descripcion, '''', '''''') || ''' descripcion, '
  || CASE
       WHEN abreviatura IS NULL THEN 'NULL'
       ELSE '''' || REPLACE(abreviatura, '''', '''''') || ''''
     END || ' abreviatura, '
  || NVL(TO_CHAR(active), '0') || ' active, '
  || 'TO_DATE(''' || TO_CHAR(fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'') fecha_registro, '''
  || REPLACE(usuario_registro, '''', '''''') || ''' usuario_registro, '
  || CASE
       WHEN fecha_modificacion IS NULL THEN 'NULL'
       ELSE 'TO_DATE(''' || TO_CHAR(fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')'
     END || ' fecha_modificacion, '
  || CASE
       WHEN usuario_modificacion IS NULL THEN 'NULL'
       ELSE '''' || REPLACE(usuario_modificacion, '''', '''''') || ''''
     END || ' usuario_modificacion FROM dual) s '
  || 'ON (t.id_provincia = s.id_provincia) '
  || 'WHEN MATCHED THEN UPDATE SET '
  || 't.id_departamento = s.id_departamento, '
  || 't.codigo_provincia_inei = s.codigo_provincia_inei, '
  || 't.codigo_provincia_reniec = s.codigo_provincia_reniec, '
  || 't.descripcion = s.descripcion, '
  || 't.abreviatura = s.abreviatura, '
  || 't.active = s.active, '
  || 't.fecha_registro = s.fecha_registro, '
  || 't.usuario_registro = s.usuario_registro, '
  || 't.fecha_modificacion = s.fecha_modificacion, '
  || 't.usuario_modificacion = s.usuario_modificacion, '
  || 't.codigo = s.codigo_provincia_inei, '
  || 't.nombre = s.descripcion, '
  || 't.activo = s.active, '
  || 't.id_ubigeo_departamento = (SELECT d.id_ubigeo_departamento FROM ubigeo_departamento d WHERE d.id_departamento = s.id_departamento), '
  || 't.creado_en = NVL(t.creado_en, CAST(s.fecha_registro AS TIMESTAMP)), '
  || 't.modificado_en = CAST(s.fecha_modificacion AS TIMESTAMP) '
  || 'WHEN NOT MATCHED THEN INSERT '
  || '(id_provincia, id_departamento, codigo_provincia_inei, codigo_provincia_reniec, descripcion, abreviatura, active, fecha_registro, usuario_registro, fecha_modificacion, usuario_modificacion, codigo, nombre, activo, creado_en, modificado_en, id_ubigeo_departamento) VALUES '
  || '(s.id_provincia, s.id_departamento, s.codigo_provincia_inei, s.codigo_provincia_reniec, s.descripcion, s.abreviatura, s.active, s.fecha_registro, s.usuario_registro, s.fecha_modificacion, s.usuario_modificacion, s.codigo_provincia_inei, s.descripcion, s.active, CAST(s.fecha_registro AS TIMESTAMP), CAST(s.fecha_modificacion AS TIMESTAMP), (SELECT d.id_ubigeo_departamento FROM ubigeo_departamento d WHERE d.id_departamento = s.id_departamento));'
  AS script_sql
FROM provincia
ORDER BY id_provincia;

SELECT
  'MERGE INTO ubigeo_distrito t USING (SELECT '
  || id_distrito || ' id_distrito, '
  || id_departamento || ' id_departamento, '
  || id_provincia || ' id_provincia, '''
  || REPLACE(codigo_distrito_inei, '''', '''''') || ''' codigo_distrito_inei, '
  || CASE
       WHEN codigo_distrito_reniec IS NULL THEN 'NULL'
       ELSE '''' || REPLACE(codigo_distrito_reniec, '''', '''''') || ''''
     END || ' codigo_distrito_reniec, '''
  || REPLACE(descripcion, '''', '''''') || ''' descripcion, '
  || CASE
       WHEN abreviatura IS NULL THEN 'NULL'
       ELSE '''' || REPLACE(abreviatura, '''', '''''') || ''''
     END || ' abreviatura, '
  || NVL(TO_CHAR(active), '0') || ' active, '
  || 'TO_DATE(''' || TO_CHAR(fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'') fecha_registro, '''
  || REPLACE(usuario_registro, '''', '''''') || ''' usuario_registro, '
  || CASE
       WHEN fecha_modificacion IS NULL THEN 'NULL'
       ELSE 'TO_DATE(''' || TO_CHAR(fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')'
     END || ' fecha_modificacion, '
  || CASE
       WHEN usuario_modificacion IS NULL THEN 'NULL'
       ELSE '''' || REPLACE(usuario_modificacion, '''', '''''') || ''''
     END || ' usuario_modificacion FROM dual) s '
  || 'ON (t.id_distrito = s.id_distrito) '
  || 'WHEN MATCHED THEN UPDATE SET '
  || 't.id_departamento = s.id_departamento, '
  || 't.id_provincia = s.id_provincia, '
  || 't.codigo_distrito_inei = s.codigo_distrito_inei, '
  || 't.codigo_distrito_reniec = s.codigo_distrito_reniec, '
  || 't.descripcion = s.descripcion, '
  || 't.abreviatura = s.abreviatura, '
  || 't.active = s.active, '
  || 't.fecha_registro = s.fecha_registro, '
  || 't.usuario_registro = s.usuario_registro, '
  || 't.fecha_modificacion = s.fecha_modificacion, '
  || 't.usuario_modificacion = s.usuario_modificacion, '
  || 't.codigo = s.codigo_distrito_inei, '
  || 't.nombre = s.descripcion, '
  || 't.activo = s.active, '
  || 't.id_ubigeo_provincia = (SELECT p.id_ubigeo_provincia FROM ubigeo_provincia p WHERE p.id_provincia = s.id_provincia), '
  || 't.creado_en = NVL(t.creado_en, CAST(s.fecha_registro AS TIMESTAMP)), '
  || 't.modificado_en = CAST(s.fecha_modificacion AS TIMESTAMP) '
  || 'WHEN NOT MATCHED THEN INSERT '
  || '(id_distrito, id_departamento, id_provincia, codigo_distrito_inei, codigo_distrito_reniec, descripcion, abreviatura, active, fecha_registro, usuario_registro, fecha_modificacion, usuario_modificacion, codigo, nombre, activo, creado_en, modificado_en, id_ubigeo_provincia) VALUES '
  || '(s.id_distrito, s.id_departamento, s.id_provincia, s.codigo_distrito_inei, s.codigo_distrito_reniec, s.descripcion, s.abreviatura, s.active, s.fecha_registro, s.usuario_registro, s.fecha_modificacion, s.usuario_modificacion, s.codigo_distrito_inei, s.descripcion, s.active, CAST(s.fecha_registro AS TIMESTAMP), CAST(s.fecha_modificacion AS TIMESTAMP), (SELECT p.id_ubigeo_provincia FROM ubigeo_provincia p WHERE p.id_provincia = s.id_provincia));'
  AS script_sql
FROM distrito
ORDER BY id_distrito;

SELECT '' AS script_sql FROM dual
UNION ALL
SELECT 'COMMIT;' FROM dual;
