/* ============================================================
   SCRIPT 92 - Plazos de vencimiento de respuesta/publicacion para
   cartas intermedias (panel Cartas de Respuesta de Asignacion)
   Ejecutar conectado como SDRERC_APP.

   Contexto: docs/arquitectura_app/bandeja_analisis_respuestas_notificaciones.xlsx,
   pestana "DOC.", columnas TIEMPO DE ESPERA y PUBLICACION. Pedido del usuario:
   agregar alertas de vencimiento en el panel Cartas de Respuesta (Asignacion),
   con los dias contados desde Fecha Acuse (respuesta) y desde la fecha de
   publicacion del edicto (publicacion), y que ambas alertas se desactiven al
   registrar la asignacion de la carta al abogado de Eq. Analisis (accion ya
   implementada en AsignacionExpedienteDAO.reasignarDesdeCartaRespuesta).

   Plazos segun el Excel (dias habiles):
     - CARTA EDICTO               : 30 dias respuesta, 15 dias publicacion
     - CARTA PRECISAR PRETENSION  : 30 dias respuesta, sin plazo de publicacion
     - CARTA INDAGATORIO          : 10 dias respuesta, sin plazo de publicacion
     - CARTA FALTA SUSTENTO       : 30 dias respuesta, sin plazo de publicacion

   PLAZO_CONFIGURACION.id_tipo_documento referencia la tabla TIPO_DOCUMENTO
   (procedimiento/tramite), no TIPO_DOCUMENTO_ADJUNTO (donde viven los codigos
   de estas 4 cartas). Se sigue el mismo patron ya usado para los plazos por
   procedimiento (SOLICITUD_RECTIFICACION_ADMINISTRATIVA, etc., ver script 30):
   codigo libre (columna CODIGO), sin FK a id_tipo_documento, resuelto en Java
   via PlazoConfiguracionDAO.obtenerPlazoPorCodigo(codigo).

   Convencion de codigo usada por DocumentoAnalisisDAO.listarCartasRespuestaPendientes:
     - Plazo de respuesta   : mismo codigo que TIPO_DOCUMENTO_ADJUNTO.codigo
                               (ej. 'ANALISIS_DOC_02_CARTA_EDICTO')
     - Plazo de publicacion : ese mismo codigo + sufijo '_PUBLICACION'
                               (ej. 'ANALISIS_DOC_02_CARTA_EDICTO_PUBLICACION')

   Idempotente: cada INSERT verifica NOT EXISTS por CODIGO exacto.
   ============================================================ */

INSERT INTO plazo_configuracion (
  codigo, nombre, ambito, dias_plazo, unidad_plazo,
  porcentaje_verde_desde, porcentaje_amarillo_desde, porcentaje_rojo_desde, activo
)
SELECT 'ANALISIS_DOC_02_CARTA_EDICTO', 'Vencimiento respuesta - Carta Edicto', 'CARTA_RESPUESTA',
       30, 'HABILES', 51, 21, 0, 1
  FROM dual
 WHERE NOT EXISTS (SELECT 1 FROM plazo_configuracion WHERE UPPER(codigo) = 'ANALISIS_DOC_02_CARTA_EDICTO');

INSERT INTO plazo_configuracion (
  codigo, nombre, ambito, dias_plazo, unidad_plazo,
  porcentaje_verde_desde, porcentaje_amarillo_desde, porcentaje_rojo_desde, activo
)
SELECT 'ANALISIS_DOC_02_CARTA_EDICTO_PUBLICACION', 'Vencimiento publicación - Carta Edicto', 'CARTA_PUBLICACION',
       15, 'HABILES', 51, 21, 0, 1
  FROM dual
 WHERE NOT EXISTS (SELECT 1 FROM plazo_configuracion WHERE UPPER(codigo) = 'ANALISIS_DOC_02_CARTA_EDICTO_PUBLICACION');

INSERT INTO plazo_configuracion (
  codigo, nombre, ambito, dias_plazo, unidad_plazo,
  porcentaje_verde_desde, porcentaje_amarillo_desde, porcentaje_rojo_desde, activo
)
SELECT 'ANALISIS_DOC_05_CARTA_PRECISAR_PRETENSION', 'Vencimiento respuesta - Carta Precisar Pretensión', 'CARTA_RESPUESTA',
       30, 'HABILES', 51, 21, 0, 1
  FROM dual
 WHERE NOT EXISTS (SELECT 1 FROM plazo_configuracion WHERE UPPER(codigo) = 'ANALISIS_DOC_05_CARTA_PRECISAR_PRETENSION');

INSERT INTO plazo_configuracion (
  codigo, nombre, ambito, dias_plazo, unidad_plazo,
  porcentaje_verde_desde, porcentaje_amarillo_desde, porcentaje_rojo_desde, activo
)
SELECT 'ANALISIS_DOC_04_CARTA_INDAGATORIO', 'Vencimiento respuesta - Carta Indagatorio', 'CARTA_RESPUESTA',
       10, 'HABILES', 51, 21, 0, 1
  FROM dual
 WHERE NOT EXISTS (SELECT 1 FROM plazo_configuracion WHERE UPPER(codigo) = 'ANALISIS_DOC_04_CARTA_INDAGATORIO');

INSERT INTO plazo_configuracion (
  codigo, nombre, ambito, dias_plazo, unidad_plazo,
  porcentaje_verde_desde, porcentaje_amarillo_desde, porcentaje_rojo_desde, activo
)
SELECT 'ANALISIS_DOC_03_CARTA_FALTA_SUSTENTO', 'Vencimiento respuesta - Carta Falta Sustento', 'CARTA_RESPUESTA',
       30, 'HABILES', 51, 21, 0, 1
  FROM dual
 WHERE NOT EXISTS (SELECT 1 FROM plazo_configuracion WHERE UPPER(codigo) = 'ANALISIS_DOC_03_CARTA_FALTA_SUSTENTO');

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT codigo, nombre, ambito, dias_plazo, unidad_plazo, activo
  FROM plazo_configuracion
 WHERE codigo LIKE 'ANALISIS_DOC_0%CARTA%'
 ORDER BY codigo;
