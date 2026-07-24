/* ============================================================
   SCRIPT 89 - Correccion de expediente_evaluacion.creado_por cuando
   quedo a nombre de un usuario ADMIN_SISTEMA
   Ejecutar conectado como SDRERC_APP.

   Contexto: reportado por el usuario con captura de la Bandeja
   Verificacion (columna "Abogado designado" mostrando "Josimar Vera
   Miranda", su propio usuario ADMIN_SISTEMA, en vez del abogado de
   Analisis designado). La regla "si ADMIN_SISTEMA ejecuta la accion,
   el registro debe quedar a nombre del abogado responsable, no del
   administrador" ya estaba implementada para EXPEDIENTE_HISTORIAL
   (resolverAutorHistorial, ver entrada "Autor del historial cuando
   actua ADMIN_SISTEMA" en AGENTS.md), pero nunca se aplico a
   EXPEDIENTE_EVALUACION.CREADO_POR, que es la columna que realmente
   alimenta "Abogado designado" en Verificacion
   (VerificacionExpedienteDAO, subconsulta "responsable_analisis"
   sobre expediente_evaluacion.creado_por). Fix de codigo ya aplicado
   en AnalisisExpedienteDAO.registrarAnalisis (usa resolverAutorHistorial
   antes de insertar/actualizar expediente_evaluacion); este script
   corrige unicamente los datos ya persistidos con el comportamiento
   anterior.

   Alcance: solo actualiza expediente_evaluacion.creado_por cuando:
     1) el usuario actual en creado_por tiene rol ADMIN_SISTEMA activo, y
     2) el expediente tiene un id_usuario_responsable_actual resoluble
        (el abogado a quien se le debe atribuir el registro).
   Si el expediente no tiene responsable actual, no se toca esa fila
   (no hay a quien sustituir, igual criterio que resolverAutorHistorial
   en Java).

   Idempotente: el UPDATE solo afecta filas que aun cumplen la condicion
   de "creado_por es ADMIN_SISTEMA"; una vez corregidas, reejecutar el
   script no vuelve a tocarlas. No se ejecuto contra ninguna base de
   datos.
   ============================================================ */

-- Diagnostico previo: filas afectadas
SELECT ev.id_expediente_evaluacion, e.numero_expediente,
       creado.username AS creado_por_actual, creado.nombre_completo AS creado_por_nombre,
       resp.username AS responsable_actual, resp.nombre_completo AS responsable_nombre
  FROM expediente_evaluacion ev
  JOIN expediente e ON e.id_expediente = ev.id_expediente
  JOIN usuario creado ON creado.id_usuario = ev.creado_por
  LEFT JOIN usuario resp ON resp.id_usuario = e.id_usuario_responsable_actual
 WHERE ev.activo = 1
   AND EXISTS (
         SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1
          WHERE ur.id_usuario = ev.creado_por AND ur.activo = 1 AND UPPER(r.codigo) = 'ADMIN_SISTEMA'
       );

UPDATE expediente_evaluacion ev
   SET ev.creado_por = (SELECT e.id_usuario_responsable_actual FROM expediente e WHERE e.id_expediente = ev.id_expediente)
 WHERE ev.activo = 1
   AND EXISTS (
         SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1
          WHERE ur.id_usuario = ev.creado_por AND ur.activo = 1 AND UPPER(r.codigo) = 'ADMIN_SISTEMA'
       )
   AND EXISTS (
         SELECT 1 FROM expediente e
          WHERE e.id_expediente = ev.id_expediente AND e.id_usuario_responsable_actual IS NOT NULL
       );

COMMIT;

/* ============================================================
   Verificacion posterior (debe devolver 0 filas)
   ============================================================ */

SELECT ev.id_expediente_evaluacion, e.numero_expediente,
       creado.username AS creado_por_actual, creado.nombre_completo AS creado_por_nombre
  FROM expediente_evaluacion ev
  JOIN expediente e ON e.id_expediente = ev.id_expediente
  JOIN usuario creado ON creado.id_usuario = ev.creado_por
 WHERE ev.activo = 1
   AND EXISTS (
         SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1
          WHERE ur.id_usuario = ev.creado_por AND ur.activo = 1 AND UPPER(r.codigo) = 'ADMIN_SISTEMA'
       );
