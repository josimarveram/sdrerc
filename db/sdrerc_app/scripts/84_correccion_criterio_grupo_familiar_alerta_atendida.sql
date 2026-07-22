/* ============================================================
   SCRIPT 84 - Limpiar criterio/observacion de Grupo Familiar en expedientes
   cuya alerta "Posible Grupo Familiar" ya fue atendida (descartada)
   Ejecutar conectado como SDRERC_APP.

   Contexto: el KPI "Posible Grupo Familiar" mostraba 45 en la Bandeja Registro
   y 44 en la Bandeja Asignacion para los mismos datos. Causa: Asignacion cuenta
   solo expedientes con alerta ACTIVA en EXPEDIENTE_ALERTA (mensaje = 'Posible
   Grupo Familiar', activo=1, atendida=0); Registro ademas revisa directamente
   EXPEDIENTE_SOLICITUD.criterio_grupo_familiar/observacion_grupo_familiar, sin
   importar si la alerta ya fue atendida.

   El boton "eliminar Alerta de Posible Grupo Familiar" (GrupoFamiliarDAO.
   eliminarAlertaPosibleGrupoFamiliar) marcaba la alerta como atendida pero
   nunca limpiaba esos dos campos heuristicos en EXPEDIENTE_SOLICITUD. El fix
   de codigo de esta misma fecha ya evita que esto vuelva a ocurrir hacia
   adelante (ver AGENTS.md). Este script corrige unicamente los expedientes ya
   afectados por el comportamiento anterior.

   Alcance: limpia criterio_grupo_familiar/observacion_grupo_familiar en la
   solicitud activa mas reciente de cada expediente cuyo grupo_familiar NO esta
   confirmado (=0) y cuya alerta "Posible Grupo Familiar" ya esta atendida (o
   nunca existio). No toca expedientes con grupo_familiar=1 (confirmados: ahi
   criterio_grupo_familiar='CONFIRMADO_ASIGNACION' es un rastro de auditoria
   valido y no debe borrarse) ni expedientes con alerta todavia activa (esos
   siguen siendo candidatos reales pendientes de revisar).

   Idempotente: al re-ejecutar, la condicion WHERE ya no encuentra las filas
   corregidas en una corrida anterior (los campos ya estan en NULL).
   ============================================================ */

-- Diagnostico previo (informativo, no modifica datos):
SELECT s.id_expediente, e.numero_expediente, s.criterio_grupo_familiar, s.observacion_grupo_familiar
FROM expediente_solicitud s
JOIN expediente e ON e.id_expediente = s.id_expediente
WHERE s.activo = 1
  AND NVL(s.grupo_familiar, 0) = 0
  AND (s.criterio_grupo_familiar IS NOT NULL OR s.observacion_grupo_familiar IS NOT NULL)
  AND s.id_expediente_solicitud = (
    SELECT MAX(s2.id_expediente_solicitud)
    FROM expediente_solicitud s2
    WHERE s2.id_expediente = s.id_expediente AND s2.activo = 1
  )
  AND NOT EXISTS (
    SELECT 1 FROM expediente_alerta ea
    WHERE ea.id_expediente = s.id_expediente
      AND ea.activo = 1 AND ea.atendida = 0
      AND UPPER(TRIM(ea.mensaje)) = 'POSIBLE GRUPO FAMILIAR'
  );

UPDATE expediente_solicitud s
SET s.criterio_grupo_familiar = NULL,
    s.observacion_grupo_familiar = NULL,
    s.modificado_en = SYSTIMESTAMP
WHERE s.activo = 1
  AND NVL(s.grupo_familiar, 0) = 0
  AND (s.criterio_grupo_familiar IS NOT NULL OR s.observacion_grupo_familiar IS NOT NULL)
  AND s.id_expediente_solicitud = (
    SELECT MAX(s2.id_expediente_solicitud)
    FROM expediente_solicitud s2
    WHERE s2.id_expediente = s.id_expediente AND s2.activo = 1
  )
  AND NOT EXISTS (
    SELECT 1 FROM expediente_alerta ea
    WHERE ea.id_expediente = s.id_expediente
      AND ea.activo = 1 AND ea.atendida = 0
      AND UPPER(TRIM(ea.mensaje)) = 'POSIBLE GRUPO FAMILIAR'
  );

COMMIT;

-- Verificacion posterior (informativo): deberia devolver 0 filas.
SELECT s.id_expediente, e.numero_expediente, s.criterio_grupo_familiar, s.observacion_grupo_familiar
FROM expediente_solicitud s
JOIN expediente e ON e.id_expediente = s.id_expediente
WHERE s.activo = 1
  AND NVL(s.grupo_familiar, 0) = 0
  AND (s.criterio_grupo_familiar IS NOT NULL OR s.observacion_grupo_familiar IS NOT NULL)
  AND s.id_expediente_solicitud = (
    SELECT MAX(s2.id_expediente_solicitud)
    FROM expediente_solicitud s2
    WHERE s2.id_expediente = s.id_expediente AND s2.activo = 1
  )
  AND NOT EXISTS (
    SELECT 1 FROM expediente_alerta ea
    WHERE ea.id_expediente = s.id_expediente
      AND ea.activo = 1 AND ea.atendida = 0
      AND UPPER(TRIM(ea.mensaje)) = 'POSIBLE GRUPO FAMILIAR'
  );
