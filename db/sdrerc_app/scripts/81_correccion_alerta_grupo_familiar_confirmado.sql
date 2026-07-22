-- Corrige expedientes cuyo Grupo Familiar ya esta confirmado ("Si") pero cuya alerta
-- "Posible Grupo Familiar" quedo activa en EXPEDIENTE_ALERTA (no se marco como atendida).
--
-- Contexto: antes del fix de esta sesion en GrupoFamiliarDAO.asociarGrupoFamiliar, la
-- asociacion de un grupo familiar (Fase 2, PERSONA.id_grupo_familiar) no llamaba a
-- ExpedienteAlertaDAO.marcarAtendidas para la alerta "Posible Grupo Familiar", a diferencia
-- del flujo de Registro (ExpedienteRegistroDAO.registrarGrupoFamiliar) que si lo hacia desde
-- antes. Resultado: expedientes ya confirmados con Grupo Familiar = "Si" que seguian contando
-- en el KPI "Posible Grupo Familiar" de la Bandeja Asignacion en vez de mostrar "Sin Alerta".
--
-- El fix de codigo ya evita que esto vuelva a ocurrir hacia adelante (ver AGENTS.md, entrada
-- "Panel de Grupo Familiar (Registro y Asignacion)..."). Este script corrige unicamente los
-- datos ya persistidos en ese estado inconsistente.
--
-- Alcance: marca atendida=1/activo=0 en EXPEDIENTE_ALERTA solo para alertas "Posible Grupo
-- Familiar" activas cuyo expediente tiene, en su fila mas reciente y activa de
-- EXPEDIENTE_SOLICITUD, grupo_familiar = 1 (confirmado). No toca expedientes con Grupo
-- Familiar = "No" (esos deben seguir mostrando la alerta normalmente).
--
-- Idempotente: al re-ejecutar, la condicion WHERE (activo=1 AND atendida=0) ya no encuentra
-- las filas corregidas en una corrida anterior, por lo que no vuelve a afectarlas.

-- Diagnostico previo (informativo, no modifica datos):
SELECT COUNT(*) AS expedientes_a_corregir
FROM expediente_alerta ea
WHERE ea.activo = 1
  AND ea.atendida = 0
  AND UPPER(TRIM(ea.mensaje)) = 'POSIBLE GRUPO FAMILIAR'
  AND EXISTS (
    SELECT 1
    FROM expediente_solicitud es
    WHERE es.id_expediente = ea.id_expediente
      AND es.activo = 1
      AND es.grupo_familiar = 1
      AND es.id_expediente_solicitud = (
        SELECT MAX(es2.id_expediente_solicitud)
        FROM expediente_solicitud es2
        WHERE es2.id_expediente = ea.id_expediente
          AND es2.activo = 1
      )
  );

UPDATE expediente_alerta ea
SET ea.atendida = 1,
    ea.activo = 0,
    ea.modificado_en = SYSTIMESTAMP
WHERE ea.activo = 1
  AND ea.atendida = 0
  AND UPPER(TRIM(ea.mensaje)) = 'POSIBLE GRUPO FAMILIAR'
  AND EXISTS (
    SELECT 1
    FROM expediente_solicitud es
    WHERE es.id_expediente = ea.id_expediente
      AND es.activo = 1
      AND es.grupo_familiar = 1
      AND es.id_expediente_solicitud = (
        SELECT MAX(es2.id_expediente_solicitud)
        FROM expediente_solicitud es2
        WHERE es2.id_expediente = ea.id_expediente
          AND es2.activo = 1
      )
  );

COMMIT;

-- Verificacion posterior (informativo): deberia devolver 0.
SELECT COUNT(*) AS expedientes_pendientes_tras_correccion
FROM expediente_alerta ea
WHERE ea.activo = 1
  AND ea.atendida = 0
  AND UPPER(TRIM(ea.mensaje)) = 'POSIBLE GRUPO FAMILIAR'
  AND EXISTS (
    SELECT 1
    FROM expediente_solicitud es
    WHERE es.id_expediente = ea.id_expediente
      AND es.activo = 1
      AND es.grupo_familiar = 1
      AND es.id_expediente_solicitud = (
        SELECT MAX(es2.id_expediente_solicitud)
        FROM expediente_solicitud es2
        WHERE es2.id_expediente = ea.id_expediente
          AND es2.activo = 1
      )
  );
