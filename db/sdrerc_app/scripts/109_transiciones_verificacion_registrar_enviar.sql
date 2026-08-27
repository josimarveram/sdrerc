/* ============================================================
   SCRIPT 109 - Transicion nueva para separar "Registrar verificacion"
   (guardar, sin mover el expediente) de "Enviar a ..." (mover recien
   segun el equipo elegido en Destino operativo) en SDRERC V3.
   Ejecutar conectado como SDRERC_APP.

   Contexto (pedido explicito del usuario, 21/08/2026, 2 pasadas el mismo
   dia): en V3, el boton "Registrar verificacion" del panel de Verificacion
   movia el expediente de inmediato (a Analisis si Observado, o a Ejecucion
   si Aprobado + resultado resolutivo), sin pasar por la seccion "Destino
   operativo". Se rediseño para que "Registrar verificacion" SOLO guarde
   el resultado (igual que "Registrar resultado" en Analisis) y quede
   dentro de la misma etapa VERIFICACION; recien el boton dinamico de
   "Destino operativo" ("Enviar a Analisis"/"Enviar a Ejecucion"/"Enviar
   a Supervision") mueve el expediente al equipo elegido.

   El catalogo real de V2 YA tenia las transiciones necesarias para el
   paso de "Registrar" (reutilizadas sin cambios, ver v3/CLAUDE.md):
     - REGISTRO_OBSERVACION_VERIFICACION: VERIFICACION/EN_VERIFICACION
       -> VERIFICACION/REQUIERE_CORRECCION (id 20, ya activa)
     - APROBACION_VERIFICACION: VERIFICACION/EN_VERIFICACION
       -> VERIFICACION/VERIFICADO (id 26, ya activa)

   Y para el paso de "Enviar a..." cuando el resultado registrado fue
   Observado (REQUIERE_CORRECCION), unico destino valido Eq. Analisis:
     - DEVOLUCION_A_ANALISIS: VERIFICACION/REQUIERE_CORRECCION
       -> ANALISIS/OBSERVADO (id 22, ya activa)
   Y cuando fue Aprobado (VERIFICADO) + Eq. Supervision (documento
   INTERMEDIO), via 2 saltos automaticos:
     - DERIVACION_A_NOTIFICACION: VERIFICACION/VERIFICADO
       -> NOTIFICACION/POR_ASIGNAR (id 121, ya activa)

   Solo faltaba 1 fila, para "Enviar a Ejecucion" cuando el resultado
   registrado fue Aprobado (VERIFICADO) + documento FINAL (resolucion):
     - APROBACION_VERIFICACION: VERIFICACION/VERIFICADO -> EJECUCION/EN_EJECUCION
       (antes solo existia el mismo codigo_accion desde EN_VERIFICACION,
       id 101, sin combo de destino - ahora se necesita el mismo destino
       pero partiendo de VERIFICADO, ya que el paso de "Registrar" deja
       el expediente ahi primero).

   NOTA (2da pasada, mismo dia): la 1ra version de este script tambien
   agregaba DEVOLUCION_A_ANALISIS VERIFICADO -> ANALISIS/OBSERVADO, para
   permitir "Enviar a Analisis" despues de un Aprobado. Se retiro: el
   criterio de negocio correcto (pedido explicito del usuario) es que un
   expediente Aprobado NUNCA vuelve a Analisis (ese destino es exclusivo
   de la rama Observado) - el destino de un Aprobado depende de si el
   documento es FINAL (Ejecucion) o INTERMEDIO (Supervision), nunca Analisis.

   No hizo falta agregar tipo_movimiento nuevo: REGISTRO_OBSERVACION_VERIFICACION
   (id 26), APROBACION_VERIFICACION (id 30) y DEVOLUCION_A_ANALISIS (id 27)
   ya existen y estan activos.

   Idempotente: el INSERT verifica NOT EXISTS por combinacion exacta de
   codigo_accion + etapa/estado origen + etapa/estado destino (mismo
   patron que el script 74).
   ============================================================ */

-- APROBACION_VERIFICACION: VERIFICACION/VERIFICADO -> EJECUCION/EN_EJECUCION
INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'APROBACION_VERIFICACION', 'Enviar a Ejecucion desde destino operativo de Verificacion (ya aprobado)', 0, 0
  FROM flujo f
  JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
  JOIN estado_expediente so ON so.codigo = 'VERIFICADO'
  JOIN etapa_expediente ed ON ed.codigo = 'EJECUCION'
  JOIN estado_expediente sd ON sd.codigo = 'EN_EJECUCION'
 WHERE f.codigo = 'SDRERC_TO_BE'
   AND NOT EXISTS (
         SELECT 1 FROM flujo_transicion ft2
          WHERE ft2.codigo_accion = 'APROBACION_VERIFICACION'
            AND ft2.id_etapa_origen = eo.id_etapa
            AND ft2.id_estado_origen = so.id_estado
            AND ft2.id_etapa_destino = ed.id_etapa
            AND ft2.id_estado_destino = sd.id_estado);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT ft.id_flujo_transicion, ft.codigo_accion, eo.codigo AS etapa_origen, so.codigo AS estado_origen,
       ed.codigo AS etapa_destino, sd.codigo AS estado_destino, ft.activo
  FROM flujo_transicion ft
  JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen
  JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen
  JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino
  JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino
 WHERE eo.codigo = 'VERIFICACION' AND so.codigo = 'VERIFICADO'
 ORDER BY ft.codigo_accion;
