/* ============================================================
   SCRIPT 106 - Reset de EXPEDIENTES y datos derivados,
   PRESERVANDO usuarios y TODOS los catalogos/maestros.
   Ejecutar conectado como SDRERC_APP.

   ADVERTENCIA: este script es DESTRUCTIVO e IRREVERSIBLE para expedientes,
   personas (titulares/solicitantes/remitentes), grupos familiares e
   historial/auditoria asociados. NO se ejecuta automaticamente por el
   asistente: revisalo y autorizalo explicitamente antes de correrlo contra
   la base real.

   ------------------------------------------------------------
   POR QUE ESTE SCRIPT EXISTE (diagnostico del bug real en el script 85)
   ------------------------------------------------------------
   El script 85 (85_reset_datos_prueba_preservando_usuarios.sql) truncaba
   "todo lo que NO este en esta lista de catalogos/usuarios" (un patron por
   EXCLUSION/lista negra). El problema: cuando se crearon tablas maestras
   nuevas DESPUES de escribir el script 85 (CENTROS_ATENCION,
   UNIDADES_ORGANICAS y MUNICIPALIDADES, scripts 101/103 de esta misma
   sesion), nadie actualizo la lista de exclusion del script 85 para
   incluirlas -> el script las trunco por error, pese a ser datos estaticos
   usados por combos de toda la aplicacion, no datos de expedientes.

   Este script 106 corrige la causa de raiz, no solo el sintoma: usa el
   patron OPUESTO, una lista BLANCA (por INCLUSION) de las tablas que SI se
   deben vaciar (expediente + sus tablas hijas directas + persona + grupo
   familiar + historial/auditoria). Cualquier tabla maestra/catalogo nueva
   que se agregue en el futuro (via un script nuevo, ej. 107, 108...) queda
   PROTEGIDA por defecto: solo se trunca si alguien la agrega explicitamente
   a la lista de abajo. Es la postura segura para este caso de uso
   ("resetear solo el universo de expedientes de prueba"), a diferencia del
   script 85 (que si necesita ser una lista negra porque su objetivo es
   vaciar "todo lo operativo", un universo mas amplio y menos definido).

   ------------------------------------------------------------
   Verificacion del universo real de tablas (14/08/2026, SELECT de solo
   lectura contra el esquema real antes de escribir este script, no se
   confio en el comentario del script 85 que ya estaba desactualizado):
   ------------------------------------------------------------
   - Se listaron las 71 tablas reales de USER_TABLES.
   - Se confirmo via USER_CONSTRAINTS que NINGUNA tabla catalogo/maestro
     tiene FK hacia EXPEDIENTE, PERSONA o GRUPO_FAMILIAR — solo las tablas
     EXPEDIENTE_* y PERSONA (hacia GRUPO_FAMILIAR) lo hacen. La lista de
     truncado de abajo es un conjunto cerrado: no hay ninguna FK externa
     colgando fuera de esta lista.

   Que SI se vacia (24 tablas — expedientes, documentos, historial,
   auditoria y personas derivadas de ellos, pedido explicito del usuario:
   "lo que es tambien historial o auditoria o persona eso tambien se debe
   eliminar"):
     EXPEDIENTE, EXPEDIENTE_ACTA, EXPEDIENTE_ALERTA, EXPEDIENTE_ANALISIS,
     EXPEDIENTE_ASIGNACION, EXPEDIENTE_CARGO_ACUSE,
     EXPEDIENTE_DERIVACION_EXTERNA, EXPEDIENTE_DIGITAL,
     EXPEDIENTE_DOCUMENTO, EXPEDIENTE_DOCUMENTO_ANALIZADO,
     EXPEDIENTE_EVALUACION, EXPEDIENTE_HISTORIAL, EXPEDIENTE_METRICA_ETAPA,
     EXPEDIENTE_NOTIFICACION, EXPEDIENTE_OBSERVACION, EXPEDIENTE_PERSONA,
     EXPEDIENTE_PLAZO, EXPEDIENTE_PUBLICACION, EXPEDIENTE_RELACION,
     EXPEDIENTE_RESOLUCION, EXPEDIENTE_SOLICITUD, PERSONA, GRUPO_FAMILIAR,
     AUDITORIA_EVENTO.

   Que SI se preserva (47 tablas — TODO lo demas, catalogos/maestros y
   usuarios, sin excepcion):
     AREA, CANAL_RECEPCION, CENTROS_ATENCION, ENTIDAD_EXTERNA, EQUIPO,
     ESTADO_CARGO_ACUSE, ESTADO_DOCUMENTO, ESTADO_EXPEDIENTE,
     ESTADO_NOTIFICACION, ETAPA_EXPEDIENTE, FERIADO_NACIONAL, FLUJO,
     FLUJO_TRANSICION, FLUJO_TRANSICION_EQUIPO, FLUJO_TRANSICION_ROL,
     LEGACY_CATALOGO_MAP, LEGACY_ESTADO_MAP, MOTIVO_ARCHIVO,
     MOTIVO_CORRECCION, MOTIVO_NO_CORRESPONDE, MUNICIPALIDADES, PERMISO,
     PLANTILLA_BLOQUE, PLANTILLA_DOCUMENTO, PLAZO_CONFIGURACION,
     PROCEDIMIENTO_REGISTRAL, ROL, ROL_PERMISO, TIPO_ACTA, TIPO_DOCUMENTO,
     TIPO_DOCUMENTO_ADJUNTO, TIPO_MOVIMIENTO, TIPO_NOTIFICACION,
     TIPO_OBSERVACION, TIPO_RESOLUCION, TIPO_RESULTADO_EJECUCION,
     TIPO_RESULTADO_EVALUACION, UBIGEO_DEPARTAMENTO, UBIGEO_DISTRITO,
     UBIGEO_PROVINCIA, UNIDADES_ORGANICAS, USUARIO, USUARIO_EMAIL_OTP,
     USUARIO_ROL, USUARIO_SUPERVISION, USUARIO_TOTP_BACKUP_CODE,
     EQUIPO_USUARIO.

   Mecanismo (mismo patron probado que el script 85/62, solo cambia la
   lista y el sentido — INCLUSION en vez de EXCLUSION):
   1) Deshabilita TODAS las foreign keys del esquema (evita errores de
      orden entre las 24 tablas — ej. EXPEDIENTE_RELACION tiene 2 FK hacia
      EXPEDIENTE, EXPEDIENTE_PERSONA/EXPEDIENTE_SOLICITUD hacia PERSONA).
   2) Trunca unicamente las 24 tablas listadas arriba.
   3) Reinicia a 1 la columna IDENTITY de esas mismas 24 tablas (si la
      tienen).
   4) Vuelve a habilitar todas las foreign keys.

   Idempotente en el sentido de que puede volver a correrse (dejaria el
   universo de expedientes igual de vacio, con catalogos/usuarios
   intactos), pero NO es reversible: destruye cualquier expediente,
   persona, grupo familiar, historial y evento de auditoria existente.
   ============================================================ */

SET SERVEROUTPUT ON;

/* ------------------------------------------------------------
   Paso 1: deshabilitar todas las foreign keys del esquema
   ------------------------------------------------------------ */
BEGIN
  FOR c IN (
    SELECT table_name, constraint_name
      FROM user_constraints
     WHERE constraint_type = 'R'
       AND status = 'ENABLED'
  ) LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE ' || c.table_name || ' DISABLE CONSTRAINT ' || c.constraint_name;
  END LOOP;
END;


/* ------------------------------------------------------------
   Paso 2: truncar UNICAMENTE las tablas de la lista blanca
   ------------------------------------------------------------ */
BEGIN
  FOR t IN (
    SELECT table_name
      FROM user_tables
     WHERE table_name IN (
       'EXPEDIENTE', 'EXPEDIENTE_ACTA', 'EXPEDIENTE_ALERTA', 'EXPEDIENTE_ANALISIS',
       'EXPEDIENTE_ASIGNACION', 'EXPEDIENTE_CARGO_ACUSE', 'EXPEDIENTE_DERIVACION_EXTERNA',
       'EXPEDIENTE_DIGITAL', 'EXPEDIENTE_DOCUMENTO', 'EXPEDIENTE_DOCUMENTO_ANALIZADO',
       'EXPEDIENTE_EVALUACION', 'EXPEDIENTE_HISTORIAL', 'EXPEDIENTE_METRICA_ETAPA',
       'EXPEDIENTE_NOTIFICACION', 'EXPEDIENTE_OBSERVACION', 'EXPEDIENTE_PERSONA',
       'EXPEDIENTE_PLAZO', 'EXPEDIENTE_PUBLICACION', 'EXPEDIENTE_RELACION',
       'EXPEDIENTE_RESOLUCION', 'EXPEDIENTE_SOLICITUD', 'PERSONA', 'GRUPO_FAMILIAR',
       'AUDITORIA_EVENTO'
     )
  ) LOOP
    EXECUTE IMMEDIATE 'TRUNCATE TABLE ' || t.table_name;
    DBMS_OUTPUT.PUT_LINE('Truncada: ' || t.table_name);
  END LOOP;
END;


/* ------------------------------------------------------------
   Paso 3: reiniciar a 1 la columna IDENTITY de esas mismas tablas
   ------------------------------------------------------------ */
BEGIN
  FOR ic IN (
    SELECT table_name, column_name
      FROM user_tab_identity_cols
     WHERE table_name IN (
       'EXPEDIENTE', 'EXPEDIENTE_ACTA', 'EXPEDIENTE_ALERTA', 'EXPEDIENTE_ANALISIS',
       'EXPEDIENTE_ASIGNACION', 'EXPEDIENTE_CARGO_ACUSE', 'EXPEDIENTE_DERIVACION_EXTERNA',
       'EXPEDIENTE_DIGITAL', 'EXPEDIENTE_DOCUMENTO', 'EXPEDIENTE_DOCUMENTO_ANALIZADO',
       'EXPEDIENTE_EVALUACION', 'EXPEDIENTE_HISTORIAL', 'EXPEDIENTE_METRICA_ETAPA',
       'EXPEDIENTE_NOTIFICACION', 'EXPEDIENTE_OBSERVACION', 'EXPEDIENTE_PERSONA',
       'EXPEDIENTE_PLAZO', 'EXPEDIENTE_PUBLICACION', 'EXPEDIENTE_RELACION',
       'EXPEDIENTE_RESOLUCION', 'EXPEDIENTE_SOLICITUD', 'PERSONA', 'GRUPO_FAMILIAR',
       'AUDITORIA_EVENTO'
     )
  ) LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE ' || ic.table_name || ' MODIFY ' || ic.column_name
        || ' GENERATED BY DEFAULT AS IDENTITY (START WITH 1)';
    DBMS_OUTPUT.PUT_LINE('IDENTITY reiniciada a 1: ' || ic.table_name || '.' || ic.column_name);
  END LOOP;
END;


/* ------------------------------------------------------------
   Paso 4: volver a habilitar todas las foreign keys
   ------------------------------------------------------------ */
BEGIN
  FOR c IN (
    SELECT table_name, constraint_name
      FROM user_constraints
     WHERE constraint_type = 'R'
       AND status = 'DISABLED'
  ) LOOP
    EXECUTE IMMEDIATE 'ALTER TABLE ' || c.table_name || ' ENABLE CONSTRAINT ' || c.constraint_name;
  END LOOP;
END;


/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT COUNT(*) AS total_expedientes FROM expediente;
SELECT COUNT(*) AS total_personas FROM persona;
SELECT COUNT(*) AS total_grupos_familiares FROM grupo_familiar;
SELECT COUNT(*) AS total_auditoria FROM auditoria_evento;

-- Confirmar que los maestros/catalogos NO se tocaron:
SELECT COUNT(*) AS total_usuarios FROM usuario;
SELECT COUNT(*) AS total_centros_atencion FROM centros_atencion;
SELECT COUNT(*) AS total_unidades_organicas FROM unidades_organicas;
SELECT COUNT(*) AS total_municipalidades FROM municipalidades;
SELECT COUNT(*) AS total_plantillas_documento FROM plantilla_documento;

SELECT u.username, r.codigo AS rol
  FROM usuario_rol ur
  JOIN usuario u ON u.id_usuario = ur.id_usuario
  JOIN rol r ON r.id_rol = ur.id_rol
 WHERE ur.activo = 1
 ORDER BY u.username;
