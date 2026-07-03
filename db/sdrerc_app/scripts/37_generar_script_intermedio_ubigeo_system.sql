/* ============================================================
   SCRIPT 37 - Generar script intermedio unico de ubigeo

   Conexion de ejecucion:
   - Ejecutar SOLO en XE / SYSTEM.

   Resultado:
   - Devuelve una sola fila / una sola columna CLOB llamada SCRIPT_SQL
     con todo el script intermedio listo para ejecutar en XEPDB1 / SDRERC_APP.

   Compatibilidad:
   - Compatible con data previa sembrada por el script 34 en SDRERC_APP.
   - Primero actualiza por CODIGO.
   - Luego completa por ID oficial mediante MERGE.

   Uso:
   1. Ejecutar este script en XE / SYSTEM.
   2. Copiar el contenido completo de la columna SCRIPT_SQL.
   3. Guardarlo como archivo .sql si se desea.
   4. Ejecutarlo en XEPDB1 / SDRERC_APP despues del script 35.

   Notas:
   - No modifica SYSTEM.
   - No requiere DB LINK.
   - No usa DROP, DELETE ni TRUNCATE.
   ============================================================ */

SELECT
    '-- CARGA UBIGEO GENERADA DESDE XE/SYSTEM - ejecutar en XEPDB1/SDRERC_APP' || CHR(10)
 || 'ALTER SESSION SET NLS_DATE_FORMAT = ''YYYY-MM-DD HH24:MI:SS'';' || CHR(10) || CHR(10)
 || '-- =====================================================' || CHR(10)
 || '-- DEPARTAMENTOS' || CHR(10)
 || '-- =====================================================' || CHR(10)
 || (
      SELECT XMLCAST(
               XMLAGG(
                 XMLELEMENT(
                   e,
                   'UPDATE ubigeo_departamento t SET '
                   || 't.id_departamento = ' || d.id_departamento || ', '
                   || 't.codigo_departamento_inei = ''' || REPLACE(d.codigo_departamento_inei, '''', '''''') || ''', '
                   || 't.codigo_departamento_reniec = ' || CASE WHEN d.codigo_departamento_reniec IS NULL THEN 'NULL' ELSE '''' || REPLACE(d.codigo_departamento_reniec, '''', '''''') || '''' END || ', '
                   || 't.descripcion = ''' || REPLACE(d.descripcion, '''', '''''') || ''', '
                   || 't.abreviatura = ' || CASE WHEN d.abreviatura IS NULL THEN 'NULL' ELSE '''' || REPLACE(d.abreviatura, '''', '''''') || '''' END || ', '
                   || 't.active = ' || NVL(TO_CHAR(d.active), '0') || ', '
                   || 't.fecha_registro = TO_DATE(''' || TO_CHAR(d.fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS''), '
                   || 't.usuario_registro = ''' || REPLACE(d.usuario_registro, '''', '''''') || ''', '
                   || 't.fecha_modificacion = ' || CASE WHEN d.fecha_modificacion IS NULL THEN 'NULL' ELSE 'TO_DATE(''' || TO_CHAR(d.fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')' END || ', '
                   || 't.usuario_modificacion = ' || CASE WHEN d.usuario_modificacion IS NULL THEN 'NULL' ELSE '''' || REPLACE(d.usuario_modificacion, '''', '''''') || '''' END || ', '
                   || 't.codigo = ''' || REPLACE(d.codigo_departamento_inei, '''', '''''') || ''', '
                   || 't.nombre = ''' || REPLACE(d.descripcion, '''', '''''') || ''', '
                   || 't.activo = ' || NVL(TO_CHAR(d.active), '0') || ', '
                   || 't.creado_en = NVL(t.creado_en, CAST(TO_DATE(''' || TO_CHAR(d.fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'') AS TIMESTAMP)), '
                   || 't.modificado_en = CAST(' || CASE WHEN d.fecha_modificacion IS NULL THEN 'NULL' ELSE 'TO_DATE(''' || TO_CHAR(d.fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')' END || ' AS TIMESTAMP) '
                   || 'WHERE t.codigo = ''' || REPLACE(d.codigo_departamento_inei, '''', '''''') || ''';' || CHR(10)
                   || 'MERGE INTO ubigeo_departamento t USING (SELECT '
                   || d.id_departamento || ' id_departamento, '''
                   || REPLACE(d.codigo_departamento_inei, '''', '''''') || ''' codigo_departamento_inei, '
                   || CASE WHEN d.codigo_departamento_reniec IS NULL THEN 'NULL' ELSE '''' || REPLACE(d.codigo_departamento_reniec, '''', '''''') || '''' END || ' codigo_departamento_reniec, '''
                   || REPLACE(d.descripcion, '''', '''''') || ''' descripcion, '
                   || CASE WHEN d.abreviatura IS NULL THEN 'NULL' ELSE '''' || REPLACE(d.abreviatura, '''', '''''') || '''' END || ' abreviatura, '
                   || NVL(TO_CHAR(d.active), '0') || ' active, '
                   || 'TO_DATE(''' || TO_CHAR(d.fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'') fecha_registro, '''
                   || REPLACE(d.usuario_registro, '''', '''''') || ''' usuario_registro, '
                   || CASE WHEN d.fecha_modificacion IS NULL THEN 'NULL' ELSE 'TO_DATE(''' || TO_CHAR(d.fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')' END || ' fecha_modificacion, '
                   || CASE WHEN d.usuario_modificacion IS NULL THEN 'NULL' ELSE '''' || REPLACE(d.usuario_modificacion, '''', '''''') || '''' END || ' usuario_modificacion FROM dual) s '
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
                   || CHR(10) || CHR(10)
                 )
                 ORDER BY d.id_departamento
               ) AS CLOB
             )
        FROM departamento d
    )
 || '-- =====================================================' || CHR(10)
 || '-- PROVINCIAS' || CHR(10)
 || '-- =====================================================' || CHR(10)
 || (
      SELECT XMLCAST(
               XMLAGG(
                 XMLELEMENT(
                   e,
                   'UPDATE ubigeo_provincia t SET '
                   || 't.id_provincia = ' || p.id_provincia || ', '
                   || 't.id_departamento = ' || p.id_departamento || ', '
                   || 't.codigo_provincia_inei = ''' || REPLACE(p.codigo_provincia_inei, '''', '''''') || ''', '
                   || 't.codigo_provincia_reniec = ' || CASE WHEN p.codigo_provincia_reniec IS NULL THEN 'NULL' ELSE '''' || REPLACE(p.codigo_provincia_reniec, '''', '''''') || '''' END || ', '
                   || 't.descripcion = ''' || REPLACE(p.descripcion, '''', '''''') || ''', '
                   || 't.abreviatura = ' || CASE WHEN p.abreviatura IS NULL THEN 'NULL' ELSE '''' || REPLACE(p.abreviatura, '''', '''''') || '''' END || ', '
                   || 't.active = ' || NVL(TO_CHAR(p.active), '0') || ', '
                   || 't.fecha_registro = TO_DATE(''' || TO_CHAR(p.fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS''), '
                   || 't.usuario_registro = ''' || REPLACE(p.usuario_registro, '''', '''''') || ''', '
                   || 't.fecha_modificacion = ' || CASE WHEN p.fecha_modificacion IS NULL THEN 'NULL' ELSE 'TO_DATE(''' || TO_CHAR(p.fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')' END || ', '
                   || 't.usuario_modificacion = ' || CASE WHEN p.usuario_modificacion IS NULL THEN 'NULL' ELSE '''' || REPLACE(p.usuario_modificacion, '''', '''''') || '''' END || ', '
                   || 't.codigo = ''' || REPLACE(p.codigo_provincia_inei, '''', '''''') || ''', '
                   || 't.nombre = ''' || REPLACE(p.descripcion, '''', '''''') || ''', '
                   || 't.activo = ' || NVL(TO_CHAR(p.active), '0') || ', '
                   || 't.creado_en = NVL(t.creado_en, CAST(TO_DATE(''' || TO_CHAR(p.fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'') AS TIMESTAMP)), '
                   || 't.modificado_en = CAST(' || CASE WHEN p.fecha_modificacion IS NULL THEN 'NULL' ELSE 'TO_DATE(''' || TO_CHAR(p.fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')' END || ' AS TIMESTAMP), '
                   || 't.id_ubigeo_departamento = (SELECT d.id_ubigeo_departamento FROM ubigeo_departamento d WHERE d.id_departamento = ' || p.id_departamento || ') '
                   || 'WHERE t.codigo = ''' || REPLACE(p.codigo_provincia_inei, '''', '''''') || ''';' || CHR(10)
                   || 'MERGE INTO ubigeo_provincia t USING (SELECT '
                   || p.id_provincia || ' id_provincia, '
                   || p.id_departamento || ' id_departamento, '''
                   || REPLACE(p.codigo_provincia_inei, '''', '''''') || ''' codigo_provincia_inei, '
                   || CASE WHEN p.codigo_provincia_reniec IS NULL THEN 'NULL' ELSE '''' || REPLACE(p.codigo_provincia_reniec, '''', '''''') || '''' END || ' codigo_provincia_reniec, '''
                   || REPLACE(p.descripcion, '''', '''''') || ''' descripcion, '
                   || CASE WHEN p.abreviatura IS NULL THEN 'NULL' ELSE '''' || REPLACE(p.abreviatura, '''', '''''') || '''' END || ' abreviatura, '
                   || NVL(TO_CHAR(p.active), '0') || ' active, '
                   || 'TO_DATE(''' || TO_CHAR(p.fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'') fecha_registro, '''
                   || REPLACE(p.usuario_registro, '''', '''''') || ''' usuario_registro, '
                   || CASE WHEN p.fecha_modificacion IS NULL THEN 'NULL' ELSE 'TO_DATE(''' || TO_CHAR(p.fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')' END || ' fecha_modificacion, '
                   || CASE WHEN p.usuario_modificacion IS NULL THEN 'NULL' ELSE '''' || REPLACE(p.usuario_modificacion, '''', '''''') || '''' END || ' usuario_modificacion FROM dual) s '
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
                   || CHR(10) || CHR(10)
                 )
                 ORDER BY p.id_provincia
               ) AS CLOB
             )
        FROM provincia p
    )
 || '-- =====================================================' || CHR(10)
 || '-- DISTRITOS' || CHR(10)
 || '-- =====================================================' || CHR(10)
 || (
      SELECT XMLCAST(
               XMLAGG(
                 XMLELEMENT(
                   e,
                   'UPDATE ubigeo_distrito t SET '
                   || 't.id_distrito = ' || x.id_distrito || ', '
                   || 't.id_departamento = ' || x.id_departamento || ', '
                   || 't.id_provincia = ' || x.id_provincia || ', '
                   || 't.codigo_distrito_inei = ''' || REPLACE(x.codigo_distrito_inei, '''', '''''') || ''', '
                   || 't.codigo_distrito_reniec = ' || CASE WHEN x.codigo_distrito_reniec IS NULL THEN 'NULL' ELSE '''' || REPLACE(x.codigo_distrito_reniec, '''', '''''') || '''' END || ', '
                   || 't.descripcion = ''' || REPLACE(x.descripcion, '''', '''''') || ''', '
                   || 't.abreviatura = ' || CASE WHEN x.abreviatura IS NULL THEN 'NULL' ELSE '''' || REPLACE(x.abreviatura, '''', '''''') || '''' END || ', '
                   || 't.active = ' || NVL(TO_CHAR(x.active), '0') || ', '
                   || 't.fecha_registro = TO_DATE(''' || TO_CHAR(x.fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS''), '
                   || 't.usuario_registro = ''' || REPLACE(x.usuario_registro, '''', '''''') || ''', '
                   || 't.fecha_modificacion = ' || CASE WHEN x.fecha_modificacion IS NULL THEN 'NULL' ELSE 'TO_DATE(''' || TO_CHAR(x.fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')' END || ', '
                   || 't.usuario_modificacion = ' || CASE WHEN x.usuario_modificacion IS NULL THEN 'NULL' ELSE '''' || REPLACE(x.usuario_modificacion, '''', '''''') || '''' END || ', '
                   || 't.codigo = ''' || REPLACE(x.codigo_distrito_inei, '''', '''''') || ''', '
                   || 't.nombre = ''' || REPLACE(x.descripcion, '''', '''''') || ''', '
                   || 't.activo = ' || NVL(TO_CHAR(x.active), '0') || ', '
                   || 't.creado_en = NVL(t.creado_en, CAST(TO_DATE(''' || TO_CHAR(x.fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'') AS TIMESTAMP)), '
                   || 't.modificado_en = CAST(' || CASE WHEN x.fecha_modificacion IS NULL THEN 'NULL' ELSE 'TO_DATE(''' || TO_CHAR(x.fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')' END || ' AS TIMESTAMP), '
                   || 't.id_ubigeo_provincia = (SELECT p.id_ubigeo_provincia FROM ubigeo_provincia p WHERE p.id_provincia = ' || x.id_provincia || ') '
                   || 'WHERE t.codigo = ''' || REPLACE(x.codigo_distrito_inei, '''', '''''') || ''';' || CHR(10)
                   || 'MERGE INTO ubigeo_distrito t USING (SELECT '
                   || x.id_distrito || ' id_distrito, '
                   || x.id_departamento || ' id_departamento, '
                   || x.id_provincia || ' id_provincia, '''
                   || REPLACE(x.codigo_distrito_inei, '''', '''''') || ''' codigo_distrito_inei, '
                   || CASE WHEN x.codigo_distrito_reniec IS NULL THEN 'NULL' ELSE '''' || REPLACE(x.codigo_distrito_reniec, '''', '''''') || '''' END || ' codigo_distrito_reniec, '''
                   || REPLACE(x.descripcion, '''', '''''') || ''' descripcion, '
                   || CASE WHEN x.abreviatura IS NULL THEN 'NULL' ELSE '''' || REPLACE(x.abreviatura, '''', '''''') || '''' END || ' abreviatura, '
                   || NVL(TO_CHAR(x.active), '0') || ' active, '
                   || 'TO_DATE(''' || TO_CHAR(x.fecha_registro, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'') fecha_registro, '''
                   || REPLACE(x.usuario_registro, '''', '''''') || ''' usuario_registro, '
                   || CASE WHEN x.fecha_modificacion IS NULL THEN 'NULL' ELSE 'TO_DATE(''' || TO_CHAR(x.fecha_modificacion, 'YYYY-MM-DD HH24:MI:SS') || ''',''YYYY-MM-DD HH24:MI:SS'')' END || ' fecha_modificacion, '
                   || CASE WHEN x.usuario_modificacion IS NULL THEN 'NULL' ELSE '''' || REPLACE(x.usuario_modificacion, '''', '''''') || '''' END || ' usuario_modificacion FROM dual) s '
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
                   || CHR(10) || CHR(10)
                 )
                 ORDER BY x.id_distrito
               ) AS CLOB
             )
        FROM distrito x
    )
 || 'COMMIT;'
 AS script_sql
FROM dual;
