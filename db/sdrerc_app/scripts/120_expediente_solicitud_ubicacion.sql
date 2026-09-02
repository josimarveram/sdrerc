-- 120_expediente_solicitud_ubicacion.sql
-- Separa los datos de notificación/ubicación (correo, teléfono, dirección, ubigeo) del solicitante
-- desde GSTM_PERSONA hacia una tabla propia, asociada a GSTD_EXPEDIENTE_SOLICITUD en vez de a una
-- persona (28/08/2026, pedido explícito del usuario).
--
-- Motivo (bug real diagnosticado el mismo día): cuando el solicitante es Centro de Atención, Unidad
-- Orgánica o Municipalidad, NO existe una fila REMITENTE en EXPEDIENTE_PERSONA (por diseño, ver
-- v3/CLAUDE.md "Solicitante con 3 tipos" — esos 3 tipos nunca usan una persona real como remitente).
-- "Datos de Notificación y Ubicación" (Panel de datos) dependía por completo de esa persona, así que
-- para esos 3 tipos el dato NUNCA podía guardarse, sin importar cuántas veces se editara. Con esta
-- tabla nueva, el dato queda ligado a la SOLICITUD (que siempre existe), no a si el solicitante es o
-- no una persona real.
--
-- Nombrada según el estándar OTI (código de sistema GS) por ser un objeto NUEVO creado después del
-- renombrado del 26/08/2026 — ver docs/nomenclatura_bd/mapeo_nomenclatura_gs.md. Clasificada como
-- Detalle (D), hija de GSTD_EXPEDIENTE_SOLICITUD (1:1, misma relación que GSTD_EXPEDIENTE_ACTA/
-- GSTD_EXPEDIENTE_DOCUMENTO tienen con GSTC_EXPEDIENTE). Columnas alineadas al mismo criterio ya
-- aplicado a GSTM_PERSONA (verificado contra el esquema real): CORREO_ELECTRONICO/TELEFONO sin
-- prefijo (no encajan en ninguna regla de la Tabla VIII), DI_DIRECCION, DEPARTAMENTO/PROVINCIA/
-- DISTRITO sin prefijo (texto redundante del ubigeo, igual que en PERSONA).
--
-- Idempotente. NO ejecutar sin autorización explícita separada (regla permanente del proyecto).

SET SERVEROUTPUT ON

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_count FROM user_tables WHERE table_name = 'GSTD_EXPEDIENTE_SOLICITUD_UBICACION';
  IF v_count = 0 THEN
    EXECUTE IMMEDIATE '
      CREATE TABLE GSTD_EXPEDIENTE_SOLICITUD_UBICACION (
        ID_EXPEDIENTE_SOLICITUD_UBICACION NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        ID_EXPEDIENTE_SOLICITUD NUMBER NOT NULL,
        CORREO_ELECTRONICO VARCHAR2(250),
        TELEFONO VARCHAR2(30),
        DI_DIRECCION VARCHAR2(500),
        ID_UBIGEO_DISTRITO NUMBER,
        DEPARTAMENTO VARCHAR2(120),
        PROVINCIA VARCHAR2(120),
        DISTRITO VARCHAR2(120),
        IN_ACTIVO NUMBER(1) DEFAULT 1 NOT NULL,
        US_CREADO_POR NUMBER,
        FE_CREADO_EN TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
        US_MODIFICADO_POR NUMBER,
        FE_MODIFICADO_EN TIMESTAMP,
        CONSTRAINT UK_GSTD_EXPSOLUBIC_SOLICITUD UNIQUE (ID_EXPEDIENTE_SOLICITUD),
        CONSTRAINT FK_GSTD_EXPSOLUBIC_SOLICITUD FOREIGN KEY (ID_EXPEDIENTE_SOLICITUD)
          REFERENCES GSTD_EXPEDIENTE_SOLICITUD (ID_EXPEDIENTE_SOLICITUD),
        CONSTRAINT FK_GSTD_EXPSOLUBIC_UBIGEO FOREIGN KEY (ID_UBIGEO_DISTRITO)
          REFERENCES GSTM_UBIGEO_DISTRITO (ID_UBIGEO_DISTRITO)
      )';
    DBMS_OUTPUT.PUT_LINE('Tabla GSTD_EXPEDIENTE_SOLICITUD_UBICACION creada.');
  ELSE
    DBMS_OUTPUT.PUT_LINE('Tabla GSTD_EXPEDIENTE_SOLICITUD_UBICACION ya existe, sin cambios.');
  END IF;
END;
/

-- Backfill idempotente: copia (no inventa ni recalcula) el dato ya cargado en PERSONA para las
-- solicitudes que ya tienen un REMITENTE real con algo cargado — verificado el 28/08/2026 con un
-- SELECT de solo lectura que solo 3-4 solicitudes reales tienen este dato hoy. Sin esto, esas pocas
-- ya no se verían al cambiar la lectura hacia la tabla nueva.
INSERT INTO GSTD_EXPEDIENTE_SOLICITUD_UBICACION (
  ID_EXPEDIENTE_SOLICITUD, CORREO_ELECTRONICO, TELEFONO, DI_DIRECCION,
  ID_UBIGEO_DISTRITO, DEPARTAMENTO, PROVINCIA, DISTRITO
)
SELECT sol.ID_EXPEDIENTE_SOLICITUD, per.CORREO_ELECTRONICO, per.TELEFONO, per.DI_DIRECCION,
       per.ID_UBIGEO_DISTRITO, per.DEPARTAMENTO, per.PROVINCIA, per.DISTRITO
FROM GSTD_EXPEDIENTE_SOLICITUD sol
JOIN GSTD_EXPEDIENTE_PERSONA ep
  ON ep.ID_EXPEDIENTE = sol.ID_EXPEDIENTE AND ep.TI_RELACION_PERSONA = 'REMITENTE' AND ep.IN_ACTIVO = 1
JOIN GSTM_PERSONA per ON per.ID_PERSONA = ep.ID_PERSONA
WHERE sol.IN_ACTIVO = 1
  AND (per.CORREO_ELECTRONICO IS NOT NULL OR per.TELEFONO IS NOT NULL
       OR per.DI_DIRECCION IS NOT NULL OR per.ID_UBIGEO_DISTRITO IS NOT NULL)
  AND NOT EXISTS (
    SELECT 1 FROM GSTD_EXPEDIENTE_SOLICITUD_UBICACION u
    WHERE u.ID_EXPEDIENTE_SOLICITUD = sol.ID_EXPEDIENTE_SOLICITUD
  );

COMMIT;

-- Limpieza de GSTM_PERSONA (pedido explícito del usuario, 28/08/2026: "ya no va estar relacionado a
-- la persona sino al expediente") — una vez que el backfill de arriba ya copió el dato existente
-- hacia GSTD_EXPEDIENTE_SOLICITUD_UBICACION dentro de esta MISMA ejecución, se limpian esos mismos
-- campos en GSTM_PERSONA para que dejen de vivir ahí. Acotado a personas con rol REMITENTE
-- (GSTD_EXPEDIENTE_PERSONA.TI_RELACION_PERSONA='REMITENTE', activo): es el único rol para el que el
-- formulario "Datos de Notificación y Ubicación" escribe estos campos (TITULAR nunca los usa en la
-- práctica, ver v3/CLAUDE.md); acotar por REMITENTE evita tocar por error una fila de TITULAR aunque
-- comparta la misma tabla. Como `insertarPersona` nunca deduplica (cada alta crea una fila GSTM_PERSONA
-- nueva), una fila enlazada como REMITENTE no se comparte con ningún otro rol/expediente.
UPDATE GSTM_PERSONA per
SET per.CORREO_ELECTRONICO = NULL,
    per.TELEFONO = NULL,
    per.DI_DIRECCION = NULL,
    per.ID_UBIGEO_DISTRITO = NULL,
    per.DEPARTAMENTO = NULL,
    per.PROVINCIA = NULL,
    per.DISTRITO = NULL
WHERE per.ID_PERSONA IN (
  SELECT ep.ID_PERSONA
  FROM GSTD_EXPEDIENTE_PERSONA ep
  WHERE ep.TI_RELACION_PERSONA = 'REMITENTE' AND ep.IN_ACTIVO = 1
);

COMMIT;

-- Verificación de solo lectura (conteos agregados, sin datos de fila)
SELECT COUNT(*) AS filas_en_tabla_nueva FROM GSTD_EXPEDIENTE_SOLICITUD_UBICACION;
SELECT COUNT(*) AS remitentes_con_contacto_restante
FROM GSTM_PERSONA per
JOIN GSTD_EXPEDIENTE_PERSONA ep ON ep.ID_PERSONA = per.ID_PERSONA
WHERE ep.TI_RELACION_PERSONA = 'REMITENTE' AND ep.IN_ACTIVO = 1
  AND (per.CORREO_ELECTRONICO IS NOT NULL OR per.TELEFONO IS NOT NULL
       OR per.DI_DIRECCION IS NOT NULL OR per.ID_UBIGEO_DISTRITO IS NOT NULL);
