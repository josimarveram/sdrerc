/* ============================================================
   SCRIPT 09 - Datos maestros iniciales
   Ejecutar conectado como SDRERC_APP despues de constraints.
   ============================================================ */

INSERT INTO area (codigo, nombre) VALUES ('SDRERC', 'Subdireccion de Recursos Registrales');

INSERT INTO rol (codigo, nombre, descripcion) VALUES ('ADMIN_SISTEMA', 'Administrador del sistema', 'Gestion tecnica y configuracion');
INSERT INTO rol (codigo, nombre, descripcion) VALUES ('RECEPCION', 'Recepcion', 'Registro documental dentro de REGISTRO');
INSERT INTO rol (codigo, nombre, descripcion) VALUES ('ASIGNACION', 'Asignacion', 'Asignacion y asociacion de duplicados');
INSERT INTO rol (codigo, nombre, descripcion) VALUES ('ABOGADO', 'Abogado', 'Analisis y ejecucion del expediente');
INSERT INTO rol (codigo, nombre, descripcion) VALUES ('SUPERVISION', 'Supervision', 'Responsabilidad funcional dentro de VERIFICACION');
INSERT INTO rol (codigo, nombre, descripcion) VALUES ('NOTIFICACION', 'Notificacion', 'Gestion de notificaciones y cargos');
INSERT INTO rol (codigo, nombre, descripcion) VALUES ('REGISTRADOR_CIVIL', 'Registrador Civil', 'Firma y emision');

INSERT INTO equipo (codigo, nombre) VALUES ('EQ_REGISTRO', 'Registro');
INSERT INTO equipo (codigo, nombre) VALUES ('EQ_ASIGNACION', 'Asignacion');
INSERT INTO equipo (codigo, nombre) VALUES ('EQ_ANALISIS', 'Analisis');
INSERT INTO equipo (codigo, nombre) VALUES ('EQ_VERIFICACION', 'Verificacion');
INSERT INTO equipo (codigo, nombre) VALUES ('EQ_FIRMA_EMISION', 'Firma y emision');
INSERT INTO equipo (codigo, nombre) VALUES ('EQ_EJECUCION', 'Ejecucion');
INSERT INTO equipo (codigo, nombre) VALUES ('EQ_NOTIFICACION', 'Notificacion');
INSERT INTO equipo (codigo, nombre) VALUES ('EQ_PUBLICACION', 'Publicacion condicional');
INSERT INTO equipo (codigo, nombre) VALUES ('EQ_EXPEDIENTE_DIGITAL', 'Expediente digital');

INSERT INTO entidad_externa (codigo, nombre, tipo_entidad) VALUES ('SDPRC', 'Subdireccion de Procesamiento de Registros Civiles', 'SDPRC');
INSERT INTO entidad_externa (codigo, nombre, tipo_entidad) VALUES ('OGD', 'Oficina de Gestion Documental', 'OGD');
INSERT INTO entidad_externa (codigo, nombre, tipo_entidad) VALUES ('CIUDADANO_ENTIDAD', 'Ciudadano o Entidad', 'CIUDADANO');
INSERT INTO entidad_externa (codigo, nombre, tipo_entidad) VALUES ('MUNICIPALIDAD', 'Municipalidad', 'MUNICIPALIDAD');

INSERT INTO canal_recepcion (codigo, nombre) VALUES ('MESA_PARTES', 'Mesa de partes');
INSERT INTO canal_recepcion (codigo, nombre) VALUES ('TRAMITE_DOCUMENTARIO', 'Tramite documentario');
INSERT INTO canal_recepcion (codigo, nombre) VALUES ('OGD', 'OGD');
INSERT INTO canal_recepcion (codigo, nombre) VALUES ('CORREO_INSTITUCIONAL', 'Correo institucional');
INSERT INTO canal_recepcion (codigo, nombre) VALUES ('PRESENCIAL', 'Presencial');
INSERT INTO canal_recepcion (codigo, nombre) VALUES ('VIRTUAL', 'Virtual');

INSERT INTO etapa_expediente (codigo, nombre, orden) VALUES ('REGISTRO', 'Registro', 1);
INSERT INTO etapa_expediente (codigo, nombre, orden) VALUES ('ASIGNACION', 'Asignacion', 2);
INSERT INTO etapa_expediente (codigo, nombre, orden) VALUES ('ANALISIS', 'Analisis', 3);
INSERT INTO etapa_expediente (codigo, nombre, orden) VALUES ('VERIFICACION', 'Verificacion', 4);
INSERT INTO etapa_expediente (codigo, nombre, orden) VALUES ('FIRMA_EMISION', 'Firma y emision', 5);
INSERT INTO etapa_expediente (codigo, nombre, orden) VALUES ('EJECUCION', 'Ejecucion', 6);
INSERT INTO etapa_expediente (codigo, nombre, orden) VALUES ('NOTIFICACION', 'Notificacion', 7);
INSERT INTO etapa_expediente (codigo, nombre, orden) VALUES ('PUBLICACION_CONDICIONAL', 'Publicacion condicional', 8);
INSERT INTO etapa_expediente (codigo, nombre, orden) VALUES ('EXPEDIENTE_DIGITAL', 'Expediente digital', 9);
INSERT INTO etapa_expediente (codigo, nombre, orden) VALUES ('CIERRE_ARCHIVO', 'Cierre y archivo', 10);

INSERT INTO estado_expediente (id_etapa, codigo, nombre, id_legacy)
SELECT id_etapa, 'REGISTRADO', 'Registrado', 56 FROM etapa_expediente WHERE codigo = 'REGISTRO';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'IMPORTADO', 'Importado' FROM etapa_expediente WHERE codigo = 'REGISTRO';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'POTENCIAL_DUPLICADO', 'Potencial duplicado' FROM etapa_expediente WHERE codigo = 'REGISTRO';
INSERT INTO estado_expediente (id_etapa, codigo, nombre, id_legacy) SELECT id_etapa, 'ASIGNADO', 'Asignado', 57 FROM etapa_expediente WHERE codigo = 'ASIGNACION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre, id_legacy) SELECT id_etapa, 'RECIBIDO_POR_ABOGADO', 'Recibido por abogado', 58 FROM etapa_expediente WHERE codigo = 'ANALISIS';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'EN_ANALISIS', 'En analisis' FROM etapa_expediente WHERE codigo = 'ANALISIS';
INSERT INTO estado_expediente (id_etapa, codigo, nombre, id_legacy) SELECT id_etapa, 'ATENDIDO', 'Atendido', 59 FROM etapa_expediente WHERE codigo = 'ANALISIS';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'NO_CORRESPONDE', 'No corresponde' FROM etapa_expediente WHERE codigo = 'ANALISIS';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'EN_ABANDONO', 'En abandono' FROM etapa_expediente WHERE codigo = 'ANALISIS';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'OBSERVACION_ADMINISTRATIVA', 'Observacion administrativa' FROM etapa_expediente WHERE codigo = 'ANALISIS';
INSERT INTO estado_expediente (id_etapa, codigo, nombre, id_legacy) SELECT id_etapa, 'VERIFICADO', 'Verificado', 87 FROM etapa_expediente WHERE codigo = 'VERIFICACION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'REQUIERE_CORRECCION', 'Requiere correccion' FROM etapa_expediente WHERE codigo = 'VERIFICACION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'FIRMADO', 'Firmado' FROM etapa_expediente WHERE codigo = 'FIRMA_EMISION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'EMITIDO', 'Emitido' FROM etapa_expediente WHERE codigo = 'FIRMA_EMISION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'RESOLUCION_NUMERADA', 'Resolucion numerada' FROM etapa_expediente WHERE codigo = 'FIRMA_EMISION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre, id_legacy) SELECT id_etapa, 'EN_EJECUCION', 'En ejecucion', 88 FROM etapa_expediente WHERE codigo = 'EJECUCION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'INDAGATORIO', 'Indagatorio' FROM etapa_expediente WHERE codigo = 'EJECUCION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre, id_legacy) SELECT id_etapa, 'EJECUTADO', 'Ejecutado', 89 FROM etapa_expediente WHERE codigo = 'EJECUCION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre, id_legacy) SELECT id_etapa, 'EN_NOTIFICACION', 'En notificacion', 90 FROM etapa_expediente WHERE codigo = 'NOTIFICACION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'CARGO_PENDIENTE', 'Cargo pendiente' FROM etapa_expediente WHERE codigo = 'NOTIFICACION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'CARGO_RECIBIDO', 'Cargo recibido' FROM etapa_expediente WHERE codigo = 'NOTIFICACION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre, id_legacy) SELECT id_etapa, 'NOTIFICADO', 'Notificado', 91 FROM etapa_expediente WHERE codigo = 'NOTIFICACION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'REQUIERE_PUBLICACION', 'Requiere publicacion' FROM etapa_expediente WHERE codigo = 'NOTIFICACION';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'PENDIENTE_PUBLICACION', 'Pendiente de publicacion' FROM etapa_expediente WHERE codigo = 'PUBLICACION_CONDICIONAL';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'PUBLICACION_REGISTRADA', 'Publicacion registrada' FROM etapa_expediente WHERE codigo = 'PUBLICACION_CONDICIONAL';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'CARPETA_CREADA', 'Carpeta creada' FROM etapa_expediente WHERE codigo = 'EXPEDIENTE_DIGITAL';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'LINK_REGISTRADO', 'Link registrado' FROM etapa_expediente WHERE codigo = 'EXPEDIENTE_DIGITAL';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'EXPEDIENTE_DIGITAL_COMPLETO', 'Expediente digital completo' FROM etapa_expediente WHERE codigo = 'EXPEDIENTE_DIGITAL';
INSERT INTO estado_expediente (id_etapa, codigo, nombre, finaliza_expediente) SELECT id_etapa, 'CERRADO', 'Cerrado', 1 FROM etapa_expediente WHERE codigo = 'CIERRE_ARCHIVO';
INSERT INTO estado_expediente (id_etapa, codigo, nombre, finaliza_expediente) SELECT id_etapa, 'ARCHIVADO', 'Archivado', 1 FROM etapa_expediente WHERE codigo = 'CIERRE_ARCHIVO';
INSERT INTO estado_expediente (id_etapa, codigo, nombre) SELECT id_etapa, 'DERIVACION_EXTERNA_PENDIENTE', 'Derivacion externa pendiente' FROM etapa_expediente WHERE codigo = 'CIERRE_ARCHIVO';

INSERT INTO tipo_resultado_evaluacion (codigo, nombre, id_legacy) VALUES ('PROCEDENTE', 'Procedente', 73);
INSERT INTO tipo_resultado_evaluacion (codigo, nombre, id_legacy) VALUES ('IMPROCEDENTE', 'Improcedente', 74);
INSERT INTO tipo_resultado_evaluacion (codigo, nombre, id_legacy) VALUES ('PROCEDENTE_EN_PARTE', 'Procedente en parte', 107);
INSERT INTO tipo_resultado_evaluacion (codigo, nombre) VALUES ('EN_ABANDONO', 'En abandono');
INSERT INTO tipo_resultado_evaluacion (codigo, nombre) VALUES ('OBSERVACION_ADMINISTRATIVA', 'Observacion administrativa');

INSERT INTO tipo_resultado_ejecucion (codigo, nombre) VALUES ('INDAGATORIO', 'Indagatorio');
INSERT INTO tipo_resultado_ejecucion (codigo, nombre, id_legacy) VALUES ('EJECUTADO', 'Ejecutado', 82);
INSERT INTO tipo_resultado_ejecucion (codigo, nombre, id_legacy) VALUES ('NO_CORRESPONDE_EJECUTAR', 'No corresponde ejecutar', 83);
INSERT INTO tipo_resultado_ejecucion (codigo, nombre, id_legacy) VALUES ('PENDIENTE_POR_EJECUTAR', 'Pendiente por ejecutar', 84);
INSERT INTO tipo_resultado_ejecucion (codigo, nombre, id_legacy) VALUES ('CULMINACION_EN_LINEA', 'Culminacion en linea', 85);
INSERT INTO tipo_resultado_ejecucion (codigo, nombre, id_legacy) VALUES ('RESOLUCION_OBSERVADA', 'Resolucion observada', 86);

INSERT INTO tipo_notificacion (codigo, nombre) VALUES ('VIRTUAL', 'Virtual');
INSERT INTO tipo_notificacion (codigo, nombre) VALUES ('PRESENCIAL_1', 'Presencial 1');
INSERT INTO tipo_notificacion (codigo, nombre) VALUES ('PRESENCIAL_2', 'Presencial 2');

INSERT INTO estado_notificacion (codigo, nombre) VALUES ('PENDIENTE', 'Pendiente');
INSERT INTO estado_notificacion (codigo, nombre) VALUES ('ENVIADA', 'Enviada');
INSERT INTO estado_notificacion (codigo, nombre) VALUES ('EXITOSA', 'Exitosa');
INSERT INTO estado_notificacion (codigo, nombre) VALUES ('FALLIDA', 'Fallida');

INSERT INTO estado_cargo_acuse (codigo, nombre) VALUES ('CARGO_PENDIENTE', 'Cargo pendiente');
INSERT INTO estado_cargo_acuse (codigo, nombre) VALUES ('CARGO_RECIBIDO', 'Cargo recibido');

INSERT INTO estado_documento (codigo, nombre) VALUES ('EN_PROYECTO', 'En proyecto');
INSERT INTO estado_documento (codigo, nombre) VALUES ('EN_DESPACHO', 'En despacho');
INSERT INTO estado_documento (codigo, nombre) VALUES ('EMITIDO', 'Emitido');
INSERT INTO estado_documento (codigo, nombre) VALUES ('FIRMADO', 'Firmado');
INSERT INTO estado_documento (codigo, nombre) VALUES ('OBSERVADO', 'Observado');

INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('IMPORTACION_CARGA_DIARIA', 'Importacion de carga diaria');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('RECEPCION_DOCUMENTO', 'Recepcion de documento');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('GENERACION_CODIGO_EXPEDIENTE', 'Generacion de codigo de expediente');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('ASOCIACION_DUPLICADO', 'Asociacion de duplicado');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('ASIGNACION_ABOGADO', 'Asignacion de abogado');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('RECEPCION_ASIGNACION', 'Recepcion de asignacion');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('REGISTRO_RESULTADO_ANALISIS', 'Registro de resultado de analisis');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('REVISION_SUPERVISOR', 'Revision de supervisor');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('REVERSION_ESTADO_DOCUMENTO', 'Reversion de estado de documento');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('FIRMA_DOCUMENTO', 'Firma de documento');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('REGISTRO_NUMERO_RESOLUCION', 'Registro de numero de resolucion');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('INICIO_EJECUCION', 'Inicio de ejecucion');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('NOTIFICACION_VIRTUAL', 'Notificacion virtual');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('NOTIFICACION_PRESENCIAL_1', 'Primera notificacion presencial');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('NOTIFICACION_PRESENCIAL_2', 'Segunda notificacion presencial');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('RECEPCION_CARGO_ACUSE', 'Recepcion de cargo de acuse');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('DERIVACION_A_NOTIFICACION', 'Derivacion a notificacion');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('DERIVACION_EXTERNA', 'Derivacion externa');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('GENERACION_PUBLICACION', 'Generacion de publicacion');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('CREACION_CARPETA_EXPEDIENTE_DIGITAL', 'Creacion de carpeta de expediente digital');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('REGISTRO_LINK_EXPEDIENTE_DIGITAL', 'Registro de link de expediente digital');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL', 'Carga de documentos de expediente digital');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('CIERRE', 'Cierre de expediente');
INSERT INTO tipo_movimiento (codigo, nombre) VALUES ('ARCHIVO', 'Archivo de expediente');

INSERT INTO flujo (codigo, nombre, version_flujo)
VALUES ('SDRERC_TO_BE', 'Flujo TO BE SDRERC', '1.0');

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'ASIGNACION_ABOGADO', 'Asignar expediente a abogado', 0, 0
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'REGISTRO'
JOIN estado_expediente so ON so.codigo = 'REGISTRADO'
JOIN etapa_expediente ed ON ed.codigo = 'ASIGNACION'
JOIN estado_expediente sd ON sd.codigo = 'ASIGNADO'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'RECEPCION_ASIGNACION', 'Recibir asignacion para analisis', 0, 0
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'ASIGNACION'
JOIN estado_expediente so ON so.codigo = 'ASIGNADO'
JOIN etapa_expediente ed ON ed.codigo = 'ANALISIS'
JOIN estado_expediente sd ON sd.codigo = 'RECIBIDO_POR_ABOGADO'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'REGISTRO_RESULTADO_ANALISIS', 'Enviar analisis a verificacion', 0, 1
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
JOIN estado_expediente so ON so.codigo = 'ATENDIDO'
JOIN etapa_expediente ed ON ed.codigo = 'VERIFICACION'
JOIN estado_expediente sd ON sd.codigo = 'VERIFICADO'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'REVISION_SUPERVISOR', 'Enviar a firma y emision', 0, 1
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'VERIFICACION'
JOIN estado_expediente so ON so.codigo = 'VERIFICADO'
JOIN etapa_expediente ed ON ed.codigo = 'FIRMA_EMISION'
JOIN estado_expediente sd ON sd.codigo = 'FIRMADO'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'REGISTRO_NUMERO_RESOLUCION', 'Iniciar ejecucion con resolucion numerada', 0, 1
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'FIRMA_EMISION'
JOIN estado_expediente so ON so.codigo = 'RESOLUCION_NUMERADA'
JOIN etapa_expediente ed ON ed.codigo = 'EJECUCION'
JOIN estado_expediente sd ON sd.codigo = 'EN_EJECUCION'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'FIRMA_DOCUMENTO', 'Emitir documento firmado', 0, 1
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'FIRMA_EMISION'
JOIN estado_expediente so ON so.codigo = 'FIRMADO'
JOIN etapa_expediente ed ON ed.codigo = 'FIRMA_EMISION'
JOIN estado_expediente sd ON sd.codigo = 'EMITIDO'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'REGISTRO_NUMERO_RESOLUCION', 'Registrar numero de resolucion', 0, 1
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'FIRMA_EMISION'
JOIN estado_expediente so ON so.codigo = 'EMITIDO'
JOIN etapa_expediente ed ON ed.codigo = 'FIRMA_EMISION'
JOIN estado_expediente sd ON sd.codigo = 'RESOLUCION_NUMERADA'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DERIVACION_A_NOTIFICACION', 'Derivar ejecucion a notificacion', 0, 0
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'EJECUCION'
JOIN estado_expediente so ON so.codigo = 'EJECUTADO'
JOIN etapa_expediente ed ON ed.codigo = 'NOTIFICACION'
JOIN estado_expediente sd ON sd.codigo = 'EN_NOTIFICACION'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'CIERRE', 'Cerrar expediente notificado', 0, 0
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
JOIN estado_expediente so ON so.codigo = 'NOTIFICADO'
JOIN etapa_expediente ed ON ed.codigo = 'CIERRE_ARCHIVO'
JOIN estado_expediente sd ON sd.codigo = 'CERRADO'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'ARCHIVO', 'Archivar expediente no corresponde', 1, 0
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
JOIN estado_expediente so ON so.codigo = 'NO_CORRESPONDE'
JOIN etapa_expediente ed ON ed.codigo = 'CIERRE_ARCHIVO'
JOIN estado_expediente sd ON sd.codigo = 'ARCHIVADO'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DERIVACION_EXTERNA', 'Derivar expediente no corresponde a entidad externa', 1, 1
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
JOIN estado_expediente so ON so.codigo = 'NO_CORRESPONDE'
JOIN etapa_expediente ed ON ed.codigo = 'CIERRE_ARCHIVO'
JOIN estado_expediente sd ON sd.codigo = 'DERIVACION_EXTERNA_PENDIENTE'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DERIVACION_A_NOTIFICACION', 'Derivar abandono a notificacion', 0, 0
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
JOIN estado_expediente so ON so.codigo = 'EN_ABANDONO'
JOIN etapa_expediente ed ON ed.codigo = 'NOTIFICACION'
JOIN estado_expediente sd ON sd.codigo = 'EN_NOTIFICACION'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'DERIVACION_A_NOTIFICACION', 'Derivar observacion administrativa a notificacion', 0, 0
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'ANALISIS'
JOIN estado_expediente so ON so.codigo = 'OBSERVACION_ADMINISTRATIVA'
JOIN etapa_expediente ed ON ed.codigo = 'NOTIFICACION'
JOIN estado_expediente sd ON sd.codigo = 'EN_NOTIFICACION'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'GENERACION_PUBLICACION', 'Generar publicacion por notificacion fallida', 1, 1
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'NOTIFICACION'
JOIN estado_expediente so ON so.codigo = 'REQUIERE_PUBLICACION'
JOIN etapa_expediente ed ON ed.codigo = 'PUBLICACION_CONDICIONAL'
JOIN estado_expediente sd ON sd.codigo = 'PENDIENTE_PUBLICACION'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'CIERRE', 'Cerrar expediente publicado', 0, 0
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'PUBLICACION_CONDICIONAL'
JOIN estado_expediente so ON so.codigo = 'PUBLICACION_REGISTRADA'
JOIN etapa_expediente ed ON ed.codigo = 'CIERRE_ARCHIVO'
JOIN estado_expediente sd ON sd.codigo = 'CERRADO'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'CREACION_CARPETA_EXPEDIENTE_DIGITAL', 'Crear expediente digital como indicador', 0, 0
FROM flujo f
JOIN etapa_expediente ed ON ed.codigo = 'EXPEDIENTE_DIGITAL'
JOIN estado_expediente sd ON sd.codigo = 'CARPETA_CREADA'
LEFT JOIN etapa_expediente eo ON 1 = 0
LEFT JOIN estado_expediente so ON 1 = 0
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'CREACION_CARPETA_EXPEDIENTE_DIGITAL', 'Crear carpeta de expediente digital', 0, 0
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'EXPEDIENTE_DIGITAL'
JOIN estado_expediente so ON so.codigo = 'CARPETA_CREADA'
JOIN etapa_expediente ed ON ed.codigo = 'EXPEDIENTE_DIGITAL'
JOIN estado_expediente sd ON sd.codigo = 'LINK_REGISTRADO'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO flujo_transicion (
  id_flujo, id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino,
  codigo_accion, nombre_accion, requiere_comentario, requiere_documento
)
SELECT f.id_flujo, eo.id_etapa, so.id_estado, ed.id_etapa, sd.id_estado,
       'CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL', 'Marcar expediente digital completo', 0, 1
FROM flujo f
JOIN etapa_expediente eo ON eo.codigo = 'EXPEDIENTE_DIGITAL'
JOIN estado_expediente so ON so.codigo = 'LINK_REGISTRADO'
JOIN etapa_expediente ed ON ed.codigo = 'EXPEDIENTE_DIGITAL'
JOIN estado_expediente sd ON sd.codigo = 'EXPEDIENTE_DIGITAL_COMPLETO'
WHERE f.codigo = 'SDRERC_TO_BE';

INSERT INTO legacy_estado_map (id_legacy, descripcion_legacy, codigo_estado_nuevo, codigo_etapa_nueva) VALUES (56, 'Registrado', 'REGISTRADO', 'REGISTRO');
INSERT INTO legacy_estado_map (id_legacy, descripcion_legacy, codigo_estado_nuevo, codigo_etapa_nueva) VALUES (57, 'Asignado', 'ASIGNADO', 'ASIGNACION');
INSERT INTO legacy_estado_map (id_legacy, descripcion_legacy, codigo_estado_nuevo, codigo_etapa_nueva) VALUES (58, 'Recibido', 'RECIBIDO_POR_ABOGADO', 'ANALISIS');
INSERT INTO legacy_estado_map (id_legacy, descripcion_legacy, codigo_estado_nuevo, codigo_etapa_nueva) VALUES (59, 'Atendido', 'ATENDIDO', 'ANALISIS');
INSERT INTO legacy_estado_map (id_legacy, descripcion_legacy, codigo_estado_nuevo, codigo_etapa_nueva) VALUES (87, 'Verificado', 'VERIFICADO', 'VERIFICACION');
INSERT INTO legacy_estado_map (id_legacy, descripcion_legacy, codigo_estado_nuevo, codigo_etapa_nueva) VALUES (88, 'Asignado a ejecucion', 'EN_EJECUCION', 'EJECUCION');
INSERT INTO legacy_estado_map (id_legacy, descripcion_legacy, codigo_estado_nuevo, codigo_etapa_nueva) VALUES (89, 'Ejecutado', 'EJECUTADO', 'EJECUCION');
INSERT INTO legacy_estado_map (id_legacy, descripcion_legacy, codigo_estado_nuevo, codigo_etapa_nueva) VALUES (90, 'Asignado a notificacion', 'EN_NOTIFICACION', 'NOTIFICACION');
INSERT INTO legacy_estado_map (id_legacy, descripcion_legacy, codigo_estado_nuevo, codigo_etapa_nueva) VALUES (91, 'Notificado', 'NOTIFICADO', 'NOTIFICACION');

COMMIT;
