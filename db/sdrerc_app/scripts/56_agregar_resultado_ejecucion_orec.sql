/* ============================================================
   SCRIPT 56 - Agregar resultado de ejecucion "Por ejecutar por OREC"
   Ejecutar conectado como SDRERC_APP.

   Contexto: el modulo de Ejecucion necesita un quinto resultado en el
   combo "Resultado de ejecucion" para expedientes que quedan pendientes
   de atencion por la Oficina Registral (OREC). El catalogo
   TIPO_RESULTADO_EJECUCION ya tiene EJECUTADO, NO_CORRESPONDE_EJECUTAR,
   PENDIENTE_POR_EJECUTAR y CULMINACION_EN_LINEA (sembrados en
   09_datos_maestros_iniciales.sql); falta POR_EJECUTAR_OREC.

   Idempotente: solo inserta si el codigo no existe todavia.
   No modifica ni elimina filas existentes.
   ============================================================ */

INSERT INTO tipo_resultado_ejecucion (codigo, nombre)
SELECT 'POR_EJECUTAR_OREC', 'Por ejecutar por OREC' FROM dual
 WHERE NOT EXISTS (
   SELECT 1 FROM tipo_resultado_ejecucion WHERE UPPER(codigo) = 'POR_EJECUTAR_OREC'
 );

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT codigo, nombre, activo
  FROM tipo_resultado_ejecucion
 ORDER BY id_tipo_resultado_ejecucion;
