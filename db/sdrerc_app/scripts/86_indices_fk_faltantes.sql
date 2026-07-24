/* ============================================================
   SCRIPT 86 - Indices en columnas de FK sin indice (optimizacion de bandejas)
   Ejecutar conectado como SDRERC_APP.

   Contexto: el usuario pidio optimizar las busquedas de las bandejas pensando
   en produccion con varios usuarios conectados a la vez. Diagnostico (ver
   AGENTS.md, entrada "Pool de conexiones JDBC (HikariCP)...") encontro 74
   columnas de llave foranea sin indice en el esquema. Oracle NO indexa
   automaticamente las columnas de FK (a diferencia de las PK); sin indice,
   cualquier join o subconsulta correlacionada contra esa columna (el patron
   que usan practicamente todas las bandejas: por cada expediente de la
   grilla principal, subconsultas a expediente_solicitud, expediente_persona,
   expediente_acta, expediente_relacion, expediente_documento_analizado, etc.
   filtrando por id_expediente) obliga a un full scan de la tabla hija en vez
   de un acceso indexado. A la escala de datos de prueba actual no se nota;
   con miles/decenas de miles de expedientes reales degrada de forma severa.

   Alcance: TODAS las columnas de FK del esquema sin indice en su columna
   lider (no solo las de las tablas mas consultadas por las bandejas). Es la
   practica estandar recomendada para cualquier columna de FK en Oracle
   (ademas de acelerar joins, evita bloqueos de tabla completa en la tabla
   hija durante ciertas operaciones sobre la tabla padre); el costo de
   mantenimiento de un indice extra en tablas de catalogo/bajo volumen es
   despreciable, asi que no vale la pena hacer una seleccion parcial.

   Mecanismo (mismo patron dinamico ya usado en scripts 62/85: recorre el
   diccionario de datos y genera la DDL, en vez de una lista de 74
   sentencias CREATE INDEX escritas a mano):
   1) Recorre user_cons_columns/user_constraints buscando columnas de FK
      (constraint_type='R') que no sean ya la columna lider de ningun indice
      existente.
   2) Para cada una, crea un indice B-tree no unico con nombre
      IX_FK_<TABLA>_<COLUMNA> (se confirmo que esta instancia soporta
      identificadores largos, hasta 62 caracteres probado sin problema).

   Idempotente: como el cursor se re-evalua contra el estado real del
   diccionario de datos en cada corrida, las columnas que ya tienen indice
   (incluidas las creadas por una corrida anterior de este mismo script) se
   excluyen automaticamente sin necesidad de un chequeo adicional por fila.

   Bajo riesgo: CREATE INDEX no es destructivo ni irreversible (a diferencia
   de los scripts 62/85 que hacen TRUNCATE) — si algun indice no aporta valor
   se puede DROP INDEX sin perder datos. Aun asi, requiere autorizacion
   explicita antes de ejecutarse contra la base real, igual que cualquier
   cambio de esquema de este proyecto.
   ============================================================ */

SET SERVEROUTPUT ON;

BEGIN
  FOR fk IN (
    SELECT a.table_name, a.column_name
      FROM user_cons_columns a
      JOIN user_constraints c
        ON c.constraint_name = a.constraint_name
       AND c.constraint_type = 'R'
     WHERE NOT EXISTS (
       SELECT 1 FROM user_ind_columns i
        WHERE i.table_name = a.table_name
          AND i.column_name = a.column_name
          AND i.column_position = 1
     )
     ORDER BY a.table_name, a.column_name
  ) LOOP
    EXECUTE IMMEDIATE 'CREATE INDEX ix_fk_' || LOWER(fk.table_name) || '_' || LOWER(fk.column_name)
        || ' ON ' || fk.table_name || '(' || fk.column_name || ')';
    DBMS_OUTPUT.PUT_LINE('Indice creado: ix_fk_' || LOWER(fk.table_name) || '_' || LOWER(fk.column_name)
        || ' sobre ' || fk.table_name || '.' || fk.column_name);
  END LOOP;
END;
/

/* ============================================================
   Verificacion posterior: deberia devolver 0 filas (ninguna FK sin indice).
   ============================================================ */

SELECT a.table_name, a.column_name
  FROM user_cons_columns a
  JOIN user_constraints c
    ON c.constraint_name = a.constraint_name
   AND c.constraint_type = 'R'
 WHERE NOT EXISTS (
   SELECT 1 FROM user_ind_columns i
    WHERE i.table_name = a.table_name
      AND i.column_name = a.column_name
      AND i.column_position = 1
 )
 ORDER BY a.table_name, a.column_name;

-- Listado de los indices nuevos creados por este script
SELECT index_name, table_name FROM user_indexes
 WHERE index_name LIKE 'IX_FK_%'
 ORDER BY table_name, index_name;
