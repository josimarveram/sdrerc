/* ============================================================
   SCRIPT 71 - Desactivar permisos fuera del catalogo oficial
   Ejecutar conectado como SDRERC_APP.

   Contexto: ver 00_diagnostico_permisos_reales_vs_esperado.sql, punto
   4. El catalogo PERMISO tiene filas con prefijo PERM_* (ej.
   PERM_ANALISIS_GESTIONAR, PERM_BANDEJA_VER, PERM_EXPEDIENTE_DETALLE)
   que no vienen de los scripts 59/60 (los 22 permisos oficiales, todos
   MENU_*/BANDEJA_*) ni de ningun otro script de este repositorio, y
   ningun codigo Java las inserta (no existe pantalla "Nuevo permiso"
   en la app). Se confirmaron 3 asignadas al rol ABOGADO; puede haber
   mas asignadas a los 4 roles redundantes que el script 70 ya
   desactivo (ADMINISTRADOR, ANALISTA, PREASIGNADOR, SUPERVISOR), que
   ya no importan funcionalmente pero conviene limpiar igual para que
   no aparezcan como opciones al configurar un rol nuevo.

   Que hace:
   1) Desactiva (rol_permiso.activo=0) cualquier vinculo entre un rol y
      un permiso fuera del catalogo oficial de 22 codigos, sin importar
      que rol sea (cubre ABOGADO y cualquier otro caso no visto en el
      diagnostico).
   2) Desactiva (permiso.activo=0) esas filas del catalogo PERMISO en
      si, para que dejen de listarse como opcion en Administracion >
      Roles > "Permisos del rol". PermisoDAO exige p.activo=1 en el
      JOIN (igual que con rol.activo), asi que esto es suficiente sin
      tocar mas tablas.

   No se elimina fisicamente nada (regla del proyecto). Si alguno de
   estos permisos resulta que SI se usa para algo fuera de lo que este
   diagnostico alcanzo a ver, revisar antes de ejecutar.

   Idempotente: UPDATE condicional por codigo NOT IN lista oficial, se
   puede re-ejecutar sin efecto adicional. No se ejecuto contra
   ninguna base de datos.
   ============================================================ */

UPDATE rol_permiso rp
   SET rp.activo = 0,
       rp.modificado_en = SYSTIMESTAMP
 WHERE rp.activo = 1
   AND rp.id_permiso IN (
         SELECT id_permiso FROM permiso
          WHERE UPPER(codigo) NOT IN (
            'MENU_BANDEJA', 'MENU_REGISTRO', 'MENU_ASIGNACION', 'MENU_ANALISIS', 'MENU_EJECUCION',
            'MENU_EXPEDIENTE_DIGITAL', 'MENU_VERIFICACION', 'MENU_NOTIFICACION', 'MENU_ADMIN_USUARIOS',
            'MENU_ADMIN_EQUIPO_JURIDICO', 'MENU_ADMIN_ROLES', 'MENU_ADMIN_FERIADOS', 'MENU_ADMIN_PLAZOS',
            'BANDEJA_REGISTRO_LISTADO', 'BANDEJA_REGISTRO_CARGA_DIARIA', 'BANDEJA_REGISTRO_MANUAL',
            'BANDEJA_ASIGNACION_LISTADO', 'BANDEJA_ASIGNACION_CARTAS_RESPUESTA', 'BANDEJA_ASIGNACION_CARGA_ABOGADOS',
            'BANDEJA_NOTIFICACION_ASIGNACION', 'BANDEJA_NOTIFICACION_NOTIFICACION', 'BANDEJA_NOTIFICACION_VALIDACION'));

UPDATE permiso p
   SET p.activo = 0,
       p.modificado_en = SYSTIMESTAMP
 WHERE UPPER(p.codigo) NOT IN (
         'MENU_BANDEJA', 'MENU_REGISTRO', 'MENU_ASIGNACION', 'MENU_ANALISIS', 'MENU_EJECUCION',
         'MENU_EXPEDIENTE_DIGITAL', 'MENU_VERIFICACION', 'MENU_NOTIFICACION', 'MENU_ADMIN_USUARIOS',
         'MENU_ADMIN_EQUIPO_JURIDICO', 'MENU_ADMIN_ROLES', 'MENU_ADMIN_FERIADOS', 'MENU_ADMIN_PLAZOS',
         'BANDEJA_REGISTRO_LISTADO', 'BANDEJA_REGISTRO_CARGA_DIARIA', 'BANDEJA_REGISTRO_MANUAL',
         'BANDEJA_ASIGNACION_LISTADO', 'BANDEJA_ASIGNACION_CARTAS_RESPUESTA', 'BANDEJA_ASIGNACION_CARGA_ABOGADOS',
         'BANDEJA_NOTIFICACION_ASIGNACION', 'BANDEJA_NOTIFICACION_NOTIFICACION', 'BANDEJA_NOTIFICACION_VALIDACION');

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

-- Debe quedar exactamente en 4: MENU_ANALISIS, MENU_BANDEJA,
-- MENU_EJECUCION, MENU_EXPEDIENTE_DIGITAL
SELECT p.codigo
  FROM rol_permiso rp
  JOIN rol r ON r.id_rol = rp.id_rol
  JOIN permiso p ON p.id_permiso = rp.id_permiso
 WHERE UPPER(r.codigo) = 'ABOGADO' AND rp.activo = 1
 ORDER BY p.codigo;

-- Catalogo de permisos activos restante (deberia ser 22)
SELECT COUNT(*) AS permisos_activos_catalogo FROM permiso WHERE activo = 1;

-- Confirmar que ya no quedan permisos PERM_* activos
SELECT codigo, activo FROM permiso WHERE UPPER(codigo) LIKE 'PERM\_%' ESCAPE '\' ORDER BY codigo;
