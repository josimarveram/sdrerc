-- Corrige un registro puntual de EXPEDIENTE_RELACION con id_expediente_principal incorrecto.
--
-- Contexto: bug en ExpedienteRelacionadoDAO.asociarRelacionados permitia usar como "principal"
-- un expediente que en realidad ya era un "relacionado" (asociado) de otro principal existente.
-- Reportado por el usuario en SDRERC-EXP-2026-000179: al asociar una 2da solicitud duplicada
-- (id_expediente=204) al grupo, el codigo tomo como principal el id_expediente=203 (que ya
-- estaba asociado como duplicado de id_expediente=202, el principal real y mas antiguo) en vez
-- de resolver hacia el principal canonico 202. Resultado: dos "grupos" separados en vez de un
-- unico principal (202) con 2 asociados (203 y 204).
--
-- El fix de codigo (ExpedienteRelacionadoDAO.resolverPrincipalCanonico, llamado desde
-- asociarRelacionados) evita que esto vuelva a ocurrir hacia adelante. Este script corrige
-- unicamente el dato ya persistido para este caso puntual.
--
-- Idempotente: el UPDATE solo aplica si la fila sigue en el estado incorrecto reportado;
-- si ya fue corregida (o el dato cambio de forma distinta a la esperada), no hace nada.

UPDATE expediente_relacion
SET id_expediente_principal = 202,
    modificado_en = SYSTIMESTAMP
WHERE id_expediente_relacion = 42
  AND id_expediente_principal = 203
  AND id_expediente_relacionado = 204
  AND activo = 1;

COMMIT;
