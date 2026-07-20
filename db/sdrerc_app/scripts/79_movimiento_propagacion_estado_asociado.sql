-- Siembra el tipo de movimiento usado por ExpedienteEstadoPropagacionDAO para registrar,
-- en EXPEDIENTE_HISTORIAL, los cambios de etapa/estado que se propagan automaticamente
-- desde un expediente principal hacia sus expedientes asociados (duplicados confirmados).
--
-- Idempotente: no inserta si el codigo ya existe.

INSERT INTO tipo_movimiento (codigo, nombre)
SELECT 'PROPAGACION_ESTADO_ASOCIADO', 'Propagacion automatica de estado a expediente asociado'
FROM dual
WHERE NOT EXISTS (
  SELECT 1 FROM tipo_movimiento WHERE codigo = 'PROPAGACION_ESTADO_ASOCIADO'
);

COMMIT;
