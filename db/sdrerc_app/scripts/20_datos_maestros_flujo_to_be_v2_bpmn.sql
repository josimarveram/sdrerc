/* ============================================================
   SCRIPT 20 - Alineamiento de flujo TO BE V2 BPMN
   Ejecutar conectado como SDRERC_APP.

   Alcance:
   - Completar transiciones maestras requeridas por el BPMN TO BE V2
     y por los modulos operativos SDRERC V2.
   - Corregir huecos detectados en resultados especiales de Analisis.
   - Agregar la transicion de Ejecucion para marcar expediente ejecutado.
   - Asociar roles/equipos por codigo, sin IDs hardcodeados.

   Reglas:
   - Idempotente mediante MERGE, UPDATE e INSERT con NOT EXISTS.
   - No usa DROP, DELETE ni TRUNCATE.
   - No modifica expedientes ni datos transaccionales.
   - No crea etapa visual VALIDACION.
   ============================================================ */

MERGE INTO tipo_movimiento t
USING (
  SELECT 'REGISTRO_RESULTADO_ANALISIS' AS codigo, 'Registro de resultado de analisis' AS nombre FROM dual
  UNION ALL SELECT 'INICIO_EJECUCION', 'Inicio o registro de ejecucion' FROM dual
) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE
  SET t.nombre = s.nombre,
      t.activo = 1,
      t.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, activo)
  VALUES (s.codigo, s.nombre, 1);

MERGE INTO flujo t
USING (
  SELECT 'SDRERC_TO_BE' AS codigo,
         'Flujo TO BE SDRERC' AS nombre,
         '2.0-BPMN' AS version_flujo
  FROM dual
) s
ON (t.codigo = s.codigo)
WHEN MATCHED THEN UPDATE
  SET t.nombre = s.nombre,
      t.version_flujo = s.version_flujo,
      t.activo = 1,
      t.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, version_flujo, activo)
  VALUES (s.codigo, s.nombre, s.version_flujo, 1);

MERGE INTO flujo_transicion t
USING (
  SELECT f.id_flujo,
         eo.id_etapa AS id_etapa_origen,
         so.id_estado AS id_estado_origen,
         ed.id_etapa AS id_etapa_destino,
         sd.id_estado AS id_estado_destino,
         r.codigo_accion,
         r.nombre_accion,
         r.requiere_comentario,
         r.requiere_documento
  FROM (
    SELECT 'ANALISIS' etapa_origen, 'RECIBIDO_POR_ABOGADO' estado_origen, 'ANALISIS' etapa_destino, 'ATENDIDO' estado_destino,
           'REGISTRO_RESULTADO_ANALISIS' codigo_accion, 'Registrar resultado de analisis' nombre_accion, 1 requiere_comentario, 0 requiere_documento FROM dual
    UNION ALL SELECT 'ANALISIS', 'RECIBIDO_POR_ABOGADO', 'ANALISIS', 'OBSERVADO',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar observacion de analisis', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'RECIBIDO_POR_ABOGADO', 'ANALISIS', 'NO_CORRESPONDE',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar no corresponde', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'RECIBIDO_POR_ABOGADO', 'ANALISIS', 'EN_ABANDONO',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar abandono', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'RECIBIDO_POR_ABOGADO', 'ANALISIS', 'OBSERVACION_ADMINISTRATIVA',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar observacion administrativa', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'OBSERVADO', 'ANALISIS', 'ATENDIDO',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar correccion de analisis', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'OBSERVADO', 'ANALISIS', 'OBSERVADO',
           'REGISTRO_RESULTADO_ANALISIS', 'Actualizar observacion de analisis', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'OBSERVADO', 'ANALISIS', 'NO_CORRESPONDE',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar no corresponde desde observacion', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'OBSERVADO', 'ANALISIS', 'EN_ABANDONO',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar abandono desde observacion', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'OBSERVADO', 'ANALISIS', 'OBSERVACION_ADMINISTRATIVA',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar observacion administrativa desde observacion', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'SUBSANADO', 'ANALISIS', 'ATENDIDO',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar subsanacion de analisis', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'SUBSANADO', 'ANALISIS', 'OBSERVADO',
           'REGISTRO_RESULTADO_ANALISIS', 'Observar subsanacion de analisis', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'SUBSANADO', 'ANALISIS', 'NO_CORRESPONDE',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar no corresponde desde subsanacion', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'SUBSANADO', 'ANALISIS', 'EN_ABANDONO',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar abandono desde subsanacion', 1, 0 FROM dual
    UNION ALL SELECT 'ANALISIS', 'SUBSANADO', 'ANALISIS', 'OBSERVACION_ADMINISTRATIVA',
           'REGISTRO_RESULTADO_ANALISIS', 'Registrar observacion administrativa desde subsanacion', 1, 0 FROM dual
    UNION ALL SELECT 'EJECUCION', 'EN_EJECUCION', 'EJECUCION', 'EJECUTADO',
           'INICIO_EJECUCION', 'Registrar ejecucion y marcar ejecutado', 0, 1 FROM dual
  ) r
  JOIN flujo f ON f.codigo = 'SDRERC_TO_BE'
  JOIN etapa_expediente eo ON eo.codigo = r.etapa_origen
  JOIN estado_expediente so ON so.codigo = r.estado_origen
  JOIN etapa_expediente ed ON ed.codigo = r.etapa_destino
  JOIN estado_expediente sd ON sd.codigo = r.estado_destino
) s
ON (
  t.id_flujo = s.id_flujo
  AND t.id_etapa_origen = s.id_etapa_origen
  AND t.id_estado_origen = s.id_estado_origen
  AND t.codigo_accion = s.codigo_accion
  AND t.id_etapa_destino = s.id_etapa_destino
  AND t.id_estado_destino = s.id_estado_destino
)
WHEN MATCHED THEN UPDATE
  SET t.nombre_accion = s.nombre_accion,
      t.requiere_comentario = s.requiere_comentario,
      t.requiere_documento = s.requiere_documento,
      t.activo = 1,
      t.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (
    id_flujo,
    id_etapa_origen,
    id_estado_origen,
    id_etapa_destino,
    id_estado_destino,
    codigo_accion,
    nombre_accion,
    requiere_comentario,
    requiere_documento,
    activo
  )
  VALUES (
    s.id_flujo,
    s.id_etapa_origen,
    s.id_estado_origen,
    s.id_etapa_destino,
    s.id_estado_destino,
    s.codigo_accion,
    s.nombre_accion,
    s.requiere_comentario,
    s.requiere_documento,
    1
  );

UPDATE flujo_transicion_rol ftr
SET ftr.activo = 1,
    ftr.modificado_en = SYSTIMESTAMP
WHERE EXISTS (
  SELECT 1
  FROM flujo_transicion ft
  JOIN flujo f ON f.id_flujo = ft.id_flujo
  JOIN rol r ON r.id_rol = ftr.id_rol
  WHERE ftr.id_flujo_transicion = ft.id_flujo_transicion
    AND f.codigo = 'SDRERC_TO_BE'
    AND (
      (ft.codigo_accion = 'REGISTRO_RESULTADO_ANALISIS'
       AND r.codigo IN ('ABOGADO', 'ANALISTA', 'SUPERVISION', 'SUPERVISOR', 'ADMIN_SISTEMA', 'ADMINISTRADOR'))
      OR
      (ft.codigo_accion = 'INICIO_EJECUCION'
       AND r.codigo IN ('REGISTRADOR_CIVIL', 'SUPERVISION', 'SUPERVISOR', 'ADMIN_SISTEMA', 'ADMINISTRADOR'))
    )
);

INSERT INTO flujo_transicion_rol (id_flujo_transicion, id_rol, activo)
SELECT ft.id_flujo_transicion, r.id_rol, 1
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN rol r ON (
  (ft.codigo_accion = 'REGISTRO_RESULTADO_ANALISIS'
   AND r.codigo IN ('ABOGADO', 'ANALISTA', 'SUPERVISION', 'SUPERVISOR', 'ADMIN_SISTEMA', 'ADMINISTRADOR'))
  OR
  (ft.codigo_accion = 'INICIO_EJECUCION'
   AND r.codigo IN ('REGISTRADOR_CIVIL', 'SUPERVISION', 'SUPERVISOR', 'ADMIN_SISTEMA', 'ADMINISTRADOR'))
)
WHERE f.codigo = 'SDRERC_TO_BE'
  AND ft.activo = 1
  AND ft.codigo_accion IN ('REGISTRO_RESULTADO_ANALISIS', 'INICIO_EJECUCION')
  AND NOT EXISTS (
    SELECT 1
    FROM flujo_transicion_rol x
    WHERE x.id_flujo_transicion = ft.id_flujo_transicion
      AND x.id_rol = r.id_rol
  );

UPDATE flujo_transicion_equipo fte
SET fte.activo = 1,
    fte.modificado_en = SYSTIMESTAMP
WHERE EXISTS (
  SELECT 1
  FROM flujo_transicion ft
  JOIN flujo f ON f.id_flujo = ft.id_flujo
  JOIN equipo e ON e.id_equipo = fte.id_equipo
  WHERE fte.id_flujo_transicion = ft.id_flujo_transicion
    AND f.codigo = 'SDRERC_TO_BE'
    AND (
      (ft.codigo_accion = 'REGISTRO_RESULTADO_ANALISIS'
       AND e.codigo IN ('EQ_ANALISIS', 'EQUIPO_ANALISIS'))
      OR
      (ft.codigo_accion = 'INICIO_EJECUCION'
       AND e.codigo IN ('EQ_EJECUCION', 'EQUIPO_EJECUCION'))
    )
);

INSERT INTO flujo_transicion_equipo (id_flujo_transicion, id_equipo, activo)
SELECT ft.id_flujo_transicion, e.id_equipo, 1
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
JOIN equipo e ON (
  (ft.codigo_accion = 'REGISTRO_RESULTADO_ANALISIS'
   AND e.codigo IN ('EQ_ANALISIS', 'EQUIPO_ANALISIS'))
  OR
  (ft.codigo_accion = 'INICIO_EJECUCION'
   AND e.codigo IN ('EQ_EJECUCION', 'EQUIPO_EJECUCION'))
)
WHERE f.codigo = 'SDRERC_TO_BE'
  AND ft.activo = 1
  AND ft.codigo_accion IN ('REGISTRO_RESULTADO_ANALISIS', 'INICIO_EJECUCION')
  AND NOT EXISTS (
    SELECT 1
    FROM flujo_transicion_equipo x
    WHERE x.id_flujo_transicion = ft.id_flujo_transicion
      AND x.id_equipo = e.id_equipo
  );

COMMIT;

/* ============================================================
   Validaciones posteriores
   ============================================================ */

SELECT 'ETAPAS_VALIDACION_NO_VISUAL' AS validacion, COUNT(*) AS hallazgos
FROM etapa_expediente
WHERE UPPER(codigo) LIKE '%VALIDACION%'
   OR UPPER(nombre) LIKE '%VALIDACION%';

SELECT 'FALTANTES_ANALISIS_REGISTRO_RESULTADO' AS validacion, COUNT(*) AS faltantes
FROM (
  WITH origenes AS (
    SELECT 'RECIBIDO_POR_ABOGADO' estado FROM dual UNION ALL
    SELECT 'OBSERVADO' FROM dual UNION ALL
    SELECT 'SUBSANADO' FROM dual
  ), destinos AS (
    SELECT 'ATENDIDO' estado FROM dual UNION ALL
    SELECT 'OBSERVADO' FROM dual UNION ALL
    SELECT 'NO_CORRESPONDE' FROM dual UNION ALL
    SELECT 'EN_ABANDONO' FROM dual UNION ALL
    SELECT 'OBSERVACION_ADMINISTRATIVA' FROM dual
  ), esperadas AS (
    SELECT o.estado origen, d.estado destino FROM origenes o CROSS JOIN destinos d
  )
  SELECT e.origen, e.destino
  FROM esperadas e
  WHERE NOT EXISTS (
    SELECT 1
    FROM flujo_transicion ft
    JOIN flujo f ON f.id_flujo = ft.id_flujo
    JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
    JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
    JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
    JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
    WHERE f.codigo = 'SDRERC_TO_BE'
      AND eo.codigo = 'ANALISIS'
      AND ed.codigo = 'ANALISIS'
      AND so.codigo = e.origen
      AND sd.codigo = e.destino
      AND ft.codigo_accion = 'REGISTRO_RESULTADO_ANALISIS'
      AND ft.activo = 1
  )
);

SELECT 'FALTANTES_RUTAS_OPERATIVAS_DAO' AS validacion, COUNT(*) AS faltantes
FROM (
  WITH expected AS (
    SELECT 'EJECUCION/EN_EJECUCION' origen, 'EJECUCION/EJECUTADO' destino, 'INICIO_EJECUCION' accion FROM dual
  )
  SELECT e.origen, e.destino, e.accion
  FROM expected e
  WHERE NOT EXISTS (
    SELECT 1
    FROM flujo_transicion ft
    JOIN flujo f ON f.id_flujo = ft.id_flujo
    JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
    JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
    JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
    JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
    WHERE f.codigo = 'SDRERC_TO_BE'
      AND eo.codigo || '/' || so.codigo = e.origen
      AND ed.codigo || '/' || sd.codigo = e.destino
      AND ft.codigo_accion = e.accion
      AND ft.activo = 1
  )
);

SELECT ft.codigo_accion,
       COUNT(DISTINCT ftr.id_rol) AS roles_activos,
       COUNT(DISTINCT fte.id_equipo) AS equipos_activos
FROM flujo_transicion ft
JOIN flujo f ON f.id_flujo = ft.id_flujo
LEFT JOIN flujo_transicion_rol ftr
  ON ftr.id_flujo_transicion = ft.id_flujo_transicion
 AND ftr.activo = 1
LEFT JOIN flujo_transicion_equipo fte
  ON fte.id_flujo_transicion = ft.id_flujo_transicion
 AND fte.activo = 1
WHERE f.codigo = 'SDRERC_TO_BE'
  AND ft.codigo_accion IN ('REGISTRO_RESULTADO_ANALISIS', 'INICIO_EJECUCION')
  AND ft.activo = 1
GROUP BY ft.codigo_accion
ORDER BY ft.codigo_accion;
