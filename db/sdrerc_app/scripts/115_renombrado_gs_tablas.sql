/* ============================================================
   SCRIPT 115 - Renombrado de las 72 tablas de SDRERC_APP al
   estandar de nomenclatura OTI (codigo de sistema GS, ver
   docs/nomenclatura_bd/mapeo_nomenclatura_gs.md).

   *** NO EJECUTAR CONTRA LA BASE COMPARTIDA TODAVIA ***
   Este script por si solo ROMPE POR COMPLETO tanto V2 (produccion
   real en uso hoy) como V3, porque ningun DAO Java fue actualizado
   todavia para usar los nombres nuevos. Requiere, en este orden:
     1. Fase 3 del plan: análisis de impacto (grep exhaustivo de
        cada nombre viejo en V2 legacy + V3) - NO iniciada.
     2. Actualizar TODOS los DAOs de V2 (Java 8) y V3 (Java 17)
        para usar los nombres nuevos, compilar y probar.
     3. Backup completo verificado + script de rollback probado.
     4. Ventana de mantenimiento coordinada.
     5. Autorizacion explicita separada para ejecutar.

   ALTER TABLE...RENAME TO en Oracle es una operacion de diccionario
   de datos (no mueve filas), rapida, pero deja INVALIDOS de inmediato
   a: vistas que referencian la tabla, cualquier PL/SQL que la use,
   y (mas importante) cada PreparedStatement Java que la mencione.

   Idempotente parcialmente: cada RENAME se protege con un chequeo de
   existencia del nombre viejo antes de intentarlo, para poder
   re-ejecutar el script sin error si ya se corrio antes o si el
   corte se hizo por lotes.
   ============================================================ */

SET SERVEROUTPUT ON

DECLARE
  PROCEDURE renombrar_tabla(p_actual VARCHAR2, p_nuevo VARCHAR2) IS
    v_existe NUMBER;
  BEGIN
    SELECT COUNT(*) INTO v_existe FROM user_tables WHERE table_name = UPPER(p_actual);
    IF v_existe = 0 THEN
      DBMS_OUTPUT.PUT_LINE('Omitido (no existe con nombre viejo, ya renombrada o no aplica): ' || p_actual);
      RETURN;
    END IF;
    EXECUTE IMMEDIATE 'RENAME ' || p_actual || ' TO ' || p_nuevo;
    DBMS_OUTPUT.PUT_LINE('OK: ' || p_actual || ' -> ' || p_nuevo);
  END;
BEGIN
  -- 1. Maestra (M) - 35 tablas
  renombrar_tabla('AREA', 'GSTM_AREA');
  renombrar_tabla('CANAL_RECEPCION', 'GSTM_CANAL_RECEPCION');
  renombrar_tabla('CENTROS_ATENCION', 'GSTM_CENTROS_ATENCION');
  renombrar_tabla('ENTIDAD_EXTERNA', 'GSTM_ENTIDAD_EXTERNA');
  renombrar_tabla('EQUIPO', 'GSTM_EQUIPO');
  renombrar_tabla('ESTADO_CARGO_ACUSE', 'GSTM_ESTADO_CARGO_ACUSE');
  renombrar_tabla('ESTADO_DOCUMENTO', 'GSTM_ESTADO_DOCUMENTO');
  renombrar_tabla('ESTADO_EXPEDIENTE', 'GSTM_ESTADO_EXPEDIENTE');
  renombrar_tabla('ESTADO_NOTIFICACION', 'GSTM_ESTADO_NOTIFICACION');
  renombrar_tabla('ETAPA_EXPEDIENTE', 'GSTM_ETAPA_EXPEDIENTE');
  renombrar_tabla('FERIADO_NACIONAL', 'GSTM_FERIADO_NACIONAL');
  renombrar_tabla('FLUJO', 'GSTM_FLUJO');
  renombrar_tabla('FLUJO_TRANSICION', 'GSTM_FLUJO_TRANSICION');
  renombrar_tabla('MOTIVO_ARCHIVO', 'GSTM_MOTIVO_ARCHIVO');
  renombrar_tabla('MOTIVO_CORRECCION', 'GSTM_MOTIVO_CORRECCION');
  renombrar_tabla('MOTIVO_NO_CORRESPONDE', 'GSTM_MOTIVO_NO_CORRESPONDE');
  renombrar_tabla('MUNICIPALIDADES', 'GSTM_MUNICIPALIDADES');
  renombrar_tabla('PERMISO', 'GSTM_PERMISO');
  renombrar_tabla('PERSONA', 'GSTM_PERSONA');
  renombrar_tabla('PLAZO_CONFIGURACION', 'GSTM_PLAZO_CONFIGURACION');
  renombrar_tabla('PROCEDIMIENTO_REGISTRAL', 'GSTM_PROCEDIMIENTO_REGISTRAL');
  renombrar_tabla('ROL', 'GSTM_ROL');
  renombrar_tabla('TIPO_ACTA', 'GSTM_TIPO_ACTA');
  renombrar_tabla('TIPO_DOCUMENTO', 'GSTM_TIPO_DOCUMENTO');
  renombrar_tabla('TIPO_DOCUMENTO_ADJUNTO', 'GSTM_TIPO_DOCUMENTO_ADJUNTO');
  renombrar_tabla('TIPO_MOVIMIENTO', 'GSTM_TIPO_MOVIMIENTO');
  renombrar_tabla('TIPO_NOTIFICACION', 'GSTM_TIPO_NOTIFICACION');
  renombrar_tabla('TIPO_OBSERVACION', 'GSTM_TIPO_OBSERVACION');
  renombrar_tabla('TIPO_RESOLUCION', 'GSTM_TIPO_RESOLUCION');
  renombrar_tabla('TIPO_RESULTADO_EJECUCION', 'GSTM_TIPO_RESULTADO_EJECUCION');
  renombrar_tabla('TIPO_RESULTADO_EVALUACION', 'GSTM_TIPO_RESULTADO_EVALUACION');
  renombrar_tabla('UBIGEO_DEPARTAMENTO', 'GSTM_UBIGEO_DEPARTAMENTO');
  renombrar_tabla('UBIGEO_DISTRITO', 'GSTM_UBIGEO_DISTRITO');
  renombrar_tabla('UBIGEO_PROVINCIA', 'GSTM_UBIGEO_PROVINCIA');
  renombrar_tabla('UNIDADES_ORGANICAS', 'GSTM_UNIDADES_ORGANICAS');
  renombrar_tabla('USUARIO', 'GSTM_USUARIO');

  -- 2. Cabecera (C) - 1 tabla
  renombrar_tabla('EXPEDIENTE', 'GSTC_EXPEDIENTE');

  -- 3. Detalle (D) - 26 tablas
  renombrar_tabla('EXPEDIENTE_ACTA', 'GSTD_EXPEDIENTE_ACTA');
  renombrar_tabla('EXPEDIENTE_ALERTA', 'GSTD_EXPEDIENTE_ALERTA');
  renombrar_tabla('EXPEDIENTE_ANALISIS', 'GSTD_EXPEDIENTE_ANALISIS');
  renombrar_tabla('EXPEDIENTE_ASIGNACION', 'GSTD_EXPEDIENTE_ASIGNACION');
  renombrar_tabla('EXPEDIENTE_CARGO_ACUSE', 'GSTD_EXPEDIENTE_CARGO_ACUSE');
  renombrar_tabla('EXPEDIENTE_DERIVACION_EXTERNA', 'GSTD_EXPEDIENTE_DERIVACION_EXTERNA');
  renombrar_tabla('EXPEDIENTE_DIGITAL', 'GSTD_EXPEDIENTE_DIGITAL');
  renombrar_tabla('EXPEDIENTE_DOCUMENTO', 'GSTD_EXPEDIENTE_DOCUMENTO');
  renombrar_tabla('EXPEDIENTE_DOCUMENTO_ANALIZADO', 'GSTD_EXPEDIENTE_DOCUMENTO_ANALIZADO');
  renombrar_tabla('EXPEDIENTE_EVALUACION', 'GSTD_EXPEDIENTE_EVALUACION');
  renombrar_tabla('EXPEDIENTE_NOTIFICACION', 'GSTD_EXPEDIENTE_NOTIFICACION');
  renombrar_tabla('EXPEDIENTE_OBSERVACION', 'GSTD_EXPEDIENTE_OBSERVACION');
  renombrar_tabla('EXPEDIENTE_PERSONA', 'GSTD_EXPEDIENTE_PERSONA');
  renombrar_tabla('EXPEDIENTE_PUBLICACION', 'GSTD_EXPEDIENTE_PUBLICACION');
  renombrar_tabla('EXPEDIENTE_RELACION', 'GSTD_EXPEDIENTE_RELACION');
  renombrar_tabla('EXPEDIENTE_RESOLUCION', 'GSTD_EXPEDIENTE_RESOLUCION');
  renombrar_tabla('EXPEDIENTE_SOLICITUD', 'GSTD_EXPEDIENTE_SOLICITUD');
  renombrar_tabla('FLUJO_TRANSICION_EQUIPO', 'GSTD_FLUJO_TRANSICION_EQUIPO');
  renombrar_tabla('FLUJO_TRANSICION_ROL', 'GSTD_FLUJO_TRANSICION_ROL');
  renombrar_tabla('PLANTILLA_BLOQUE', 'GSTD_PLANTILLA_BLOQUE');
  renombrar_tabla('ROL_PERMISO', 'GSTD_ROL_PERMISO');
  renombrar_tabla('EQUIPO_USUARIO', 'GSTD_EQUIPO_USUARIO');
  renombrar_tabla('USUARIO_EMAIL_OTP', 'GSTD_USUARIO_EMAIL_OTP');
  renombrar_tabla('USUARIO_ROL', 'GSTD_USUARIO_ROL');
  renombrar_tabla('USUARIO_SUPERVISION', 'GSTD_USUARIO_SUPERVISION');
  renombrar_tabla('USUARIO_TOTP_BACKUP_CODE', 'GSTD_USUARIO_TOTP_BACKUP_CODE');

  -- 4. Log (L) - 3 tablas
  renombrar_tabla('AUDITORIA_EVENTO', 'GSTL_AUDITORIA_EVENTO');
  renombrar_tabla('CARGA_INICIAL_LOG', 'GSTL_CARGA_INICIAL_LOG');
  renombrar_tabla('EXPEDIENTE_HISTORIAL', 'GSTL_EXPEDIENTE_HISTORIAL');

  -- 5. Auxiliar (X) - 6 tablas
  renombrar_tabla('EXPEDIENTE_METRICA_ETAPA', 'GSTX_EXPEDIENTE_METRICA_ETAPA');
  renombrar_tabla('EXPEDIENTE_PLAZO', 'GSTX_EXPEDIENTE_PLAZO');
  renombrar_tabla('GRUPO_FAMILIAR', 'GSTX_GRUPO_FAMILIAR');
  renombrar_tabla('LEGACY_CATALOGO_MAP', 'GSTX_LEGACY_CATALOGO_MAP');
  renombrar_tabla('LEGACY_ESTADO_MAP', 'GSTX_LEGACY_ESTADO_MAP');
  renombrar_tabla('PLANTILLA_DOCUMENTO', 'GSTX_PLANTILLA_DOCUMENTO');

  DBMS_OUTPUT.PUT_LINE('--- Renombrado de tablas finalizado (72 tablas procesadas) ---');
END;
/

/* Verificacion posterior (solo lectura): confirmar que ya no queda
   ninguna tabla con el nombre viejo y que las 72 nuevas existen. */
SELECT COUNT(*) AS tablas_gs_creadas FROM user_tables WHERE table_name LIKE 'GST%';
