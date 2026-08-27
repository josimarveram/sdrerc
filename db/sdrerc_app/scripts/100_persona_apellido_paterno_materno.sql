/* ============================================================
   SCRIPT 100 - PERSONA: apellido_paterno / apellido_materno (Fase 1
   de la separacion de "Titular"/"Solicitante" en 3 campos)
   Ejecutar conectado como SDRERC_APP.

   Contexto: hoy el titular/solicitante de un expediente se captura
   como un unico texto libre (Registro manual, Edicion manual, Carga
   diaria) que termina guardandose completo en persona.razon_social
   (ExpedienteRegistroDAO.insertarPersona/insertarPersonaManual, V3).
   Las columnas persona.nombres/persona.apellidos ya existen pero no
   se usan para datos capturados por estos 3 flujos.

   Esto genera errores reales de negocio: si un usuario escribe
   "JUAN CARLOS GARCIA LOPEZ" en vez de "GARCIA LOPEZ JUAN CARLOS", la
   deteccion de "Posible Grupo Familiar" (GrupoFamiliarHeuristicaService.
   claveApellidosTitular, que asume que los primeros 2 tokens
   significativos del texto son los apellidos) genera una clave de
   coincidencia distinta para la misma persona, y el sistema no
   detecta que pertenece al mismo grupo familiar.

   Este script agrega apellido_paterno/apellido_materno a PERSONA,
   mismo patron ya usado y probado en TECNICO (tabla legado,
   EquipoJuridicoService.java: APELLIDO_PATERNO/APELLIDO_MATERNO/
   NOMBRES como columnas separadas) para el modulo Equipo Juridico.

   Deliberadamente aditivo, sin tocar nada que V2 ya lea/escriba:
   - NO se modifica ni se elimina persona.nombres.
   - NO se modifica ni se elimina persona.apellidos.
   - NO se modifica ni se elimina persona.razon_social.
   V2 sigue leyendo/escribiendo esas 3 columnas exactamente igual que
   hoy, sin ningun cambio de su lado. Cuando V3 empiece a escribir los
   2 campos nuevos (Fase 2, backend, todavia no incluida en este
   script), lo hara en DOBLE escritura: llena apellido_paterno/
   apellido_materno/nombres estructurados Y sigue llenando
   razon_social concatenado ("apellido_paterno apellido_materno,
   nombres") para que V2 no note ninguna diferencia.

   NO se hace backfill de registros ya existentes: adivinar
   apellido_paterno/apellido_materno de un razon_social ya guardado
   como texto libre, con la misma heuristica riesgosa del punto
   anterior, perpetuaria el mismo problema que este cambio busca
   resolver. Los registros historicos quedan con estas 2 columnas en
   NULL hasta que se decida una limpieza manual/gradual aparte
   (fuera de alcance de este script).

   No se toca ninguna tabla ni columna legacy, ni OracleConnection.java,
   ni el flujo de Registro/Recepcion legacy.

   Idempotente en su totalidad (verifica user_tab_columns antes de
   cada ALTER). No se ejecuta automaticamente: requiere autorizacion
   explicita antes de correr contra la base.
   ============================================================ */

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'PERSONA'
     AND column_name = 'APELLIDO_PATERNO';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE persona ADD apellido_paterno VARCHAR2(120)';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN persona.apellido_paterno IS
    'Apellido paterno, campo estructurado (Fase 1 de separacion Titular/Solicitante). NULL en registros historicos sin backfill. Persona juridica (razon_social) no lo usa.'
  ]';
END;


DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1)
    INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'PERSONA'
     AND column_name = 'APELLIDO_MATERNO';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE persona ADD apellido_materno VARCHAR2(120)';
  END IF;

  EXECUTE IMMEDIATE q'[
    COMMENT ON COLUMN persona.apellido_materno IS
    'Apellido materno, campo estructurado (Fase 1 de separacion Titular/Solicitante). Opcional (no toda persona lo informa). NULL en registros historicos sin backfill.'
  ]';
END;


SELECT column_name, data_type, data_length, nullable, data_default
  FROM user_tab_columns
 WHERE table_name = 'PERSONA'
   AND column_name IN (
       'NOMBRES',
       'APELLIDOS',
       'RAZON_SOCIAL',
       'APELLIDO_PATERNO',
       'APELLIDO_MATERNO'
   )
 ORDER BY column_id;
