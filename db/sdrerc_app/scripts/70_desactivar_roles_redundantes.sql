/* ============================================================
   SCRIPT 70 - Desactivar roles redundantes creados manualmente
   Ejecutar conectado como SDRERC_APP.

   Contexto: ver 00_diagnostico_permisos_reales_vs_esperado.sql, punto 1.
   Administracion > Roles muestra 14 roles, pero los scripts de este
   proyecto (09, 63, 65) solo reconocen 10. Los otros 4 no existen en
   ningun script ni se referencian en el codigo Java (verificado por
   grep en src/main/java): fueron creados a mano con el boton "Nuevo
   rol", mismo origen que la duplicidad de equipos que corrigio el
   script 66 (EQUIPO_ANALISIS vs EQ_ANALISIS).

   Los 4 roles y con que rol ya existente se solapan conceptualmente:
     - ADMINISTRADOR ("Acceso administrativo del aplicativo")
       solapa con ADMIN_SISTEMA ("Gestion tecnica y configuracion").
     - ANALISTA ("Analista juridico/documental")
       solapa con ABOGADO ("Analisis y ejecucion del expediente").
     - SUPERVISOR ("Supervisor de equipo juridico")
       solapa con SUPERVISION ("Responsabilidad funcional dentro de
       VERIFICACION").
     - PREASIGNADOR ya estaba Inactivo; no corresponde a ninguna etapa
       documentada del flujo SDRERC V2.

   Los 4 tienen 0 usuarios asignados hoy (confirmado en Administracion >
   Roles), asi que no hace falta migrar a nadie de un rol a otro (a
   diferencia del script 66, que si tuvo que mover miembros de equipo).
   Solo se desactivan: r.activo=0 es suficiente para que dejen de sumar
   permisos a cualquier usuario futuro (PermisoDAO exige r.activo=1 en
   el JOIN), sin necesidad de tocar sus filas en ROL_PERMISO. No se
   eliminan fisicamente (regla del proyecto: nunca eliminar fisicamente
   roles).

   Si alguno de estos 4 SI tiene usuarios asignados en tu base (revisa
   primero el punto 1 del diagnostico), no ejecutes este script sin
   antes reasignar a esas personas al rol correcto; el UPDATE de abajo
   ya protege contra eso (WHERE ... usuarios_asignados = 0) y no hara
   nada si alguno tiene gente asignada, pero conviene revisarlo a mano
   igual.

   Idempotente: UPDATE condicional, se puede re-ejecutar sin efecto
   adicional. No se ejecuto contra ninguna base de datos.
   ============================================================ */

UPDATE rol r
   SET r.activo = 0,
       r.descripcion = 'Redundante, consolidado por script 70 el ' || TO_CHAR(SYSDATE, 'DD/MM/YYYY')
                        || '. ' || NVL(r.descripcion, ''),
       r.modificado_en = SYSTIMESTAMP
 WHERE UPPER(r.codigo) IN ('ADMINISTRADOR', 'ANALISTA', 'PREASIGNADOR', 'SUPERVISOR')
   AND NOT EXISTS (
         SELECT 1 FROM usuario_rol ur WHERE ur.id_rol = r.id_rol AND ur.activo = 1);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT codigo, nombre, activo, descripcion
  FROM rol
 WHERE UPPER(codigo) IN ('ADMINISTRADOR', 'ANALISTA', 'PREASIGNADOR', 'SUPERVISOR')
 ORDER BY codigo;

-- Debe quedar vacio: si aparece algo aqui, ese rol SI tiene usuarios y
-- no se desactivo (revisar y reasignar manualmente antes de reintentar)
SELECT r.codigo, COUNT(*) AS usuarios_asignados
  FROM rol r
  JOIN usuario_rol ur ON ur.id_rol = r.id_rol AND ur.activo = 1
 WHERE UPPER(r.codigo) IN ('ADMINISTRADOR', 'ANALISTA', 'PREASIGNADOR', 'SUPERVISOR')
 GROUP BY r.codigo;
