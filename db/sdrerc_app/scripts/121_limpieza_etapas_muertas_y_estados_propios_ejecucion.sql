/* ============================================================
   SCRIPT 121 - Limpieza de etapas muertas + estados propios de Ejecucion
   Ejecutar conectado como SDRERC_APP.

   Contexto: informe de etapas/estados del 31/08/2026 (catalogo vs. uso
   real de Recepcion a Publicacion) + 3 decisiones explicitas del usuario
   sobre ese informe:

   1) PUBLICACION_CONDICIONAL formalizada como etapa muerta: la
      "Bandeja de publicacion" real de V3 (4ta pestana de Notificacion)
      nunca usa esta etapa - el expediente permanece en
      NOTIFICACION/EN_NOTIFICACION y la publicacion se registra como un
      3er intento sobre EXPEDIENTE_NOTIFICACION (tipo_notificacion=
      'PUBLICACION'). Se desactivan sus 3 transiciones
      (GENERACION_PUBLICACION / REGISTRO_PUBLICACION / CIERRE), sin
      borrar filas de catalogo.

   2) EJECUCION deja de reutilizar por ID los estados REQUIERE_CORRECCION/
      DOCUMENTO_INCONSISTENTE de VERIFICACION (hallazgo real: 5
      expedientes reales en EJECUCION apuntaban a la MISMA fila de
      estado_expediente cuyo dueno real (id_etapa) es VERIFICACION - un
      atajo de flujo_transicion, no un bug de runtime, pero rompe la
      expectativa "1 estado = 1 etapa dueña"). Se crean 2 filas propias
      (REQUIERE_CORRECCION_EJECUCION / DOCUMENTO_INCONSISTENTE_EJECUCION,
      etapa EJECUCION), se migran los 5 expedientes reales, y se
      reemplazan las 4 filas de flujo_transicion que mezclaban ambas
      etapas (ids 46/47/48/49) por 4 nuevas apuntando a los estados
      propios. Codigo Java actualizado en el mismo commit
      (EjecucionExpedienteDAO/EjecucionExpedienteDTO/CargaInicialDAO +
      combo de filtro de EjecucionBandejaPage.tsx).

   3) FIRMA_EMISION, EXPEDIENTE_DIGITAL y CIERRE_ARCHIVO: 0 expedientes
      reales en cualquiera de sus estados (verificado en el informe),
      confirmado que ningun codigo de V3 las invoca. Se desactivan TODAS
      sus transiciones (ENVIO_FIRMA, FIRMA_DOCUMENTO x2,
      REGISTRO_NUMERO_RESOLUCION x2, CREACION_CARPETA_EXPEDIENTE_DIGITAL,
      CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL, ARCHIVO, DERIVACION_EXTERNA,
      CIERRE hacia CIERRE_ARCHIVO desde NOTIFICACION), sin borrar filas
      de catalogo (mismo criterio que el resto del proyecto: nunca se
      elimina fisicamente un objeto de catalogo, solo se desactiva).

   NO incluye el punto 4 (REGISTRO_RESULTADO_ANALISIS/REVISION_SUPERVISOR,
   ya inactivas de antes) - el usuario solo pidio aclaracion sobre su
   significado, no una accion sobre ellas; se dejan tal cual.

   Idempotente: los UPDATE de activo=0 son naturalmente re-ejecutables
   (poner 0 donde ya es 0 no cambia nada); los INSERT usan guardas
   NOT EXISTS por codigo/combinacion exacta (mismo patron del script 109).
   NO SE EJECUTA AUTOMATICAMENTE. Requiere autorizacion explicita antes
   de correr contra Oracle.
   ============================================================ */

-- ------------------------------------------------------------
-- PARTE 1: desactivar transiciones de las 4 etapas sin uso real
-- (PUBLICACION_CONDICIONAL, FIRMA_EMISION, EXPEDIENTE_DIGITAL,
-- CIERRE_ARCHIVO). IDs confirmados por consulta real contra Oracle
-- el 31/08/2026 (ver v3/CLAUDE.md).
-- ------------------------------------------------------------
UPDATE flujo_transicion
   SET activo = 0, modificado_en = SYSTIMESTAMP
 WHERE id_flujo_transicion IN (
         10,  -- ARCHIVO: ANALISIS/NO_CORRESPONDE -> CIERRE_ARCHIVO/ARCHIVADO
         11,  -- DERIVACION_EXTERNA: ANALISIS/NO_CORRESPONDE -> CIERRE_ARCHIVO/DERIVACION_EXTERNA_PENDIENTE
          9,  -- CIERRE: NOTIFICACION/NOTIFICADO -> CIERRE_ARCHIVO/CERRADO
         14,  -- GENERACION_PUBLICACION: NOTIFICACION/REQUIERE_PUBLICACION -> PUBLICACION_CONDICIONAL/PENDIENTE_PUBLICACION
         59,  -- REGISTRO_PUBLICACION: PUBLICACION_CONDICIONAL/PENDIENTE_PUBLICACION -> PUBLICACION_CONDICIONAL/PUBLICACION_REGISTRADA
         15,  -- CIERRE: PUBLICACION_CONDICIONAL/PUBLICACION_REGISTRADA -> CIERRE_ARCHIVO/CERRADO
         17,  -- CREACION_CARPETA_EXPEDIENTE_DIGITAL
         18,  -- CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL
         28,  -- FIRMA_DOCUMENTO: FIRMA_EMISION/PARA_FIRMA -> FIRMA_EMISION/FIRMADO
          6,  -- FIRMA_DOCUMENTO: FIRMA_EMISION/FIRMADO -> FIRMA_EMISION/EMITIDO
          7,  -- REGISTRO_NUMERO_RESOLUCION: FIRMA_EMISION/EMITIDO -> FIRMA_EMISION/RESOLUCION_NUMERADA
          5,  -- REGISTRO_NUMERO_RESOLUCION: FIRMA_EMISION/RESOLUCION_NUMERADA -> EJECUCION/EN_EJECUCION
         27   -- ENVIO_FIRMA: VERIFICACION/VERIFICADO -> FIRMA_EMISION/PARA_FIRMA
       )
   AND activo = 1;

-- ------------------------------------------------------------
-- PARTE 2: estados propios de EJECUCION (deja de compartir por ID
-- las filas de VERIFICACION)
-- ------------------------------------------------------------
INSERT INTO estado_expediente (id_etapa, codigo, nombre, finaliza_expediente, requiere_comentario, activo, creado_en)
SELECT et.id_etapa, 'REQUIERE_CORRECCION_EJECUCION', 'Requiere correccion', 0, 0, 1, SYSTIMESTAMP
  FROM etapa_expediente et
 WHERE et.codigo = 'EJECUCION'
   AND NOT EXISTS (SELECT 1 FROM estado_expediente WHERE codigo = 'REQUIERE_CORRECCION_EJECUCION');

INSERT INTO estado_expediente (id_etapa, codigo, nombre, finaliza_expediente, requiere_comentario, activo, creado_en)
SELECT et.id_etapa, 'DOCUMENTO_INCONSISTENTE_EJECUCION', 'Documento inconsistente', 0, 0, 1, SYSTIMESTAMP
  FROM etapa_expediente et
 WHERE et.codigo = 'EJECUCION'
   AND NOT EXISTS (SELECT 1 FROM estado_expediente WHERE codigo = 'DOCUMENTO_INCONSISTENTE_EJECUCION');

-- Migrar expedientes reales que hoy apuntan al ID compartido con VERIFICACION
-- (5 expedientes conocidos en REQUIERE_CORRECCION; 0 en DOCUMENTO_INCONSISTENTE,
-- el 2do UPDATE es simetrico/defensivo, sin efecto si no hay filas).
UPDATE expediente e
   SET e.id_estado_actual = (SELECT id_estado FROM estado_expediente WHERE codigo = 'REQUIERE_CORRECCION_EJECUCION')
 WHERE e.id_etapa_actual = (SELECT id_etapa FROM etapa_expediente WHERE codigo = 'EJECUCION')
   AND e.id_estado_actual = (SELECT es.id_estado FROM estado_expediente es
                               JOIN etapa_expediente et ON et.id_etapa = es.id_etapa
                              WHERE es.codigo = 'REQUIERE_CORRECCION' AND et.codigo = 'VERIFICACION')
   AND e.activo = 1;

UPDATE expediente e
   SET e.id_estado_actual = (SELECT id_estado FROM estado_expediente WHERE codigo = 'DOCUMENTO_INCONSISTENTE_EJECUCION')
 WHERE e.id_etapa_actual = (SELECT id_etapa FROM etapa_expediente WHERE codigo = 'EJECUCION')
   AND e.id_estado_actual = (SELECT es.id_estado FROM estado_expediente es
                               JOIN etapa_expediente et ON et.id_etapa = es.id_etapa
                              WHERE es.codigo = 'DOCUMENTO_INCONSISTENTE' AND et.codigo = 'VERIFICACION')
   AND e.activo = 1;

-- Desactivar las 4 filas que mezclaban EJECUCION con el estado prestado de VERIFICACION
UPDATE flujo_transicion
   SET activo = 0, modificado_en = SYSTIMESTAMP
 WHERE id_flujo_transicion IN (46, 47, 48, 49)
   AND activo = 1;

-- Nuevas transiciones de EJECUCION, ya con estados propios
-- (a) OBSERVACION_EJECUCION: EJECUCION/EN_EJECUCION -> EJECUCION/REQUIERE_CORRECCION_EJECUCION
INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'OBSERVACION_EJECUCION', 'Registrar observacion de ejecucion', 1, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'EJECUCION'
  JOIN estado_expediente so ON so.codigo = 'EN_EJECUCION' AND so.id_etapa = eo.id_etapa
  JOIN etapa_expediente ed ON ed.codigo = 'EJECUCION'
  JOIN estado_expediente sd ON sd.codigo = 'REQUIERE_CORRECCION_EJECUCION'
 WHERE f.codigo = 'SDRERC_TO_BE'
   AND NOT EXISTS (
         SELECT 1 FROM flujo_transicion ft2
          WHERE ft2.codigo_accion = 'OBSERVACION_EJECUCION' AND ft2.activo = 1
            AND ft2.id_estado_destino = sd.id_estado);

-- (b) REVERSION_ESTADO_DOCUMENTO_EJECUCION: EJECUCION/EN_EJECUCION -> EJECUCION/DOCUMENTO_INCONSISTENTE_EJECUCION
INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'REVERSION_ESTADO_DOCUMENTO_EJECUCION', 'Registrar documento inconsistente en ejecucion', 1, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'EJECUCION'
  JOIN estado_expediente so ON so.codigo = 'EN_EJECUCION' AND so.id_etapa = eo.id_etapa
  JOIN etapa_expediente ed ON ed.codigo = 'EJECUCION'
  JOIN estado_expediente sd ON sd.codigo = 'DOCUMENTO_INCONSISTENTE_EJECUCION'
 WHERE f.codigo = 'SDRERC_TO_BE'
   AND NOT EXISTS (
         SELECT 1 FROM flujo_transicion ft2
          WHERE ft2.codigo_accion = 'REVERSION_ESTADO_DOCUMENTO_EJECUCION' AND ft2.activo = 1
            AND ft2.id_estado_destino = sd.id_estado);

-- (c) DEVOLUCION_A_ANALISIS: EJECUCION/REQUIERE_CORRECCION_EJECUCION -> ANALISIS/OBSERVADO
INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DEVOLUCION_A_ANALISIS', 'Devolver observacion de ejecucion a analisis', 1, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'EJECUCION'
  JOIN estado_expediente so ON so.codigo = 'REQUIERE_CORRECCION_EJECUCION' AND so.id_etapa = eo.id_etapa
  JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
  JOIN estado_expediente sd ON sd.codigo = 'OBSERVADO' AND sd.id_etapa = ed.id_etapa
 WHERE f.codigo = 'SDRERC_TO_BE'
   AND NOT EXISTS (
         SELECT 1 FROM flujo_transicion ft2
          WHERE ft2.codigo_accion = 'DEVOLUCION_A_ANALISIS' AND ft2.activo = 1
            AND ft2.id_estado_origen = so.id_estado);

-- (d) DEVOLUCION_A_ANALISIS: EJECUCION/DOCUMENTO_INCONSISTENTE_EJECUCION -> ANALISIS/OBSERVADO
INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DEVOLUCION_A_ANALISIS', 'Devolver documento inconsistente de ejecucion a analisis', 1, 1
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'EJECUCION'
  JOIN estado_expediente so ON so.codigo = 'DOCUMENTO_INCONSISTENTE_EJECUCION' AND so.id_etapa = eo.id_etapa
  JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
  JOIN estado_expediente sd ON sd.codigo = 'OBSERVADO' AND sd.id_etapa = ed.id_etapa
 WHERE f.codigo = 'SDRERC_TO_BE'
   AND NOT EXISTS (
         SELECT 1 FROM flujo_transicion ft2
          WHERE ft2.codigo_accion = 'DEVOLUCION_A_ANALISIS' AND ft2.activo = 1
            AND ft2.id_estado_origen = so.id_estado);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

-- Las 13 transiciones deben quedar activo=0
SELECT id_flujo_transicion, codigo_accion, activo FROM flujo_transicion
 WHERE id_flujo_transicion IN (10,11,9,14,59,15,17,18,28,6,7,5,27,46,47,48,49)
 ORDER BY id_flujo_transicion;

-- Los 2 estados propios de EJECUCION deben existir, activos, bajo la etapa correcta
SELECT es.id_estado, et.codigo AS etapa, es.codigo, es.activo
  FROM estado_expediente es JOIN etapa_expediente et ON et.id_etapa = es.id_etapa
 WHERE es.codigo IN ('REQUIERE_CORRECCION_EJECUCION','DOCUMENTO_INCONSISTENTE_EJECUCION');

-- Ningun expediente activo en EJECUCION debe seguir apuntando al estado de VERIFICACION
SELECT COUNT(*) AS deberia_ser_cero
  FROM expediente e
  JOIN estado_expediente es ON es.id_estado = e.id_estado_actual
  JOIN etapa_expediente eest ON eest.id_etapa = es.id_etapa
 WHERE e.activo = 1
   AND e.id_etapa_actual = (SELECT id_etapa FROM etapa_expediente WHERE codigo = 'EJECUCION')
   AND eest.codigo = 'VERIFICACION';

-- Las 4 transiciones nuevas de EJECUCION deben existir, activas
SELECT ft.id_flujo_transicion, ft.codigo_accion, eo.codigo || '/' || so.codigo AS origen,
       ed.codigo || '/' || sd.codigo AS destino, ft.activo
  FROM flujo_transicion ft
  JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
  JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
  JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
  JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
 WHERE so.codigo LIKE '%_EJECUCION' OR sd.codigo LIKE '%_EJECUCION'
 ORDER BY ft.codigo_accion;
