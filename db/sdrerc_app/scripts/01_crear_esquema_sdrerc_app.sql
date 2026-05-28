/* ============================================================
   SCRIPT 01 - Crear esquema SDRERC_APP
   Ejecutar manualmente con una cuenta administradora.
   No otorgar DBA. No crear tablas con dueno SYSTEM.
   Ajustar password y tablespace si corresponde.
   ============================================================ */

CREATE USER SDRERC_APP IDENTIFIED BY "Cambiar_Clave_Segura_2026"
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP
  QUOTA UNLIMITED ON USERS;

GRANT CREATE SESSION TO SDRERC_APP;
GRANT CREATE TABLE TO SDRERC_APP;
GRANT CREATE VIEW TO SDRERC_APP;
GRANT CREATE SEQUENCE TO SDRERC_APP;
GRANT CREATE TRIGGER TO SDRERC_APP;
GRANT CREATE PROCEDURE TO SDRERC_APP;

-- Validacion esperada despues de ejecutar con cuenta administradora:
SELECT username, default_tablespace, temporary_tablespace, account_status
FROM dba_users
WHERE username = 'SDRERC_APP';

SELECT grantee, privilege
FROM dba_sys_privs
WHERE grantee = 'SDRERC_APP'
ORDER BY privilege;

