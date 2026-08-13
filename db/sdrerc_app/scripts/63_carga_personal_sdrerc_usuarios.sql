/* ============================================================
   SCRIPT 63 - Carga de personal SDRERC como usuarios V2
   Ejecutar conectado como SDRERC_APP.

   Fuente: docs/arquitectura_app/Personal_SDRERC_Usuarios_Herramienta_Interna.xlsx
   (hoja "CARGOS Y FUNCIONES", 111 personas) + 7 altas agregadas despues,
   sin DNI/telefono (ver punto 5).

   Que hace:
   1) Agrega la columna USUARIO.TELEFONO (idempotente) para conservar el
      telefono de contacto de cada persona; es el unico dato adicional del
      Excel que se modelo, porque es el unico que un perfil de usuario
      necesita razonablemente y no tiene ya una columna equivalente.
      Sede, modalidad (presencial/T.T.), condicion CAP/CAS y cargo
      primigeno NO se modelaron: ninguna pantalla de SDRERC V2 los consume
      hoy; si en el futuro se necesitan, agregar en un script aparte.
   2) Siembra el rol CONSULTA (catalogo `rol`), que no existia: el Excel
      distingue explicitamente un nivel de acceso "CONSULTA" (solo lectura)
      dentro del area Recepcion/Asignacion, distinto de RECEPCION,
      ASIGNACION y SUPERVISION que ya existian.
   3) Inserta los 111 usuarios (MERGE por USERNAME, no duplica si se
      re-ejecuta). PASSWORD_HASH queda NULL: la primera contrasena la
      asigna el administrador desde Administracion > Usuarios >
      "Restablecer clave", igual que para cualquier otro usuario V2 (no
      hay autoservicio de alta). Cada persona no podra iniciar sesion
      hasta que se le restablezca la clave individualmente.
   4) Asigna 109 roles (USUARIO_ROL) y 101 membresias de equipo
      (EQUIPO_USUARIO) segun el area/nivel de acceso indicado en el Excel.
   5) Agrega 7 personas que aparecen en personal_supervisores.xlsx pero que
      NO estaban en el Excel de origen de este script, por lo que no
      tienen DNI ni telefono (quedan en NULL; se pueden completar despues
      editando el usuario desde Administracion > Usuarios):
      5.a) 5 abogados de las hojas "Abogados"/"Abogados 2026" (usadas por
      el script 64 para la relacion supervisor-abogado): LOPEZ
      CHIQUIHUANCA MONICA PATRICIA (mlopez), MONTENEGRO CARBAJAL SANDRA
      MARIBEL (smontenegro), PUSE SANTILLAN JENIFFER MILAGROS (jpuse),
      CORTEZ PITA ALEXANDRA KAREN (acortez), FERNANDEZ OBANDO GINO AXCEL
      (gfernandez). Los 5 quedan con rol ABOGADO y equipo EQ_ANALISIS,
      igual que el resto del area de Analisis; el script 64 ya las
      incluye en la relacion USUARIO_SUPERVISION.
      5.b) 2 personas de la hoja "PERSONAL Y SUPERVISORES ", area
      "Recepcion y Asignacion de Expedientes", supervisadas por LISSET
      CALIXTRO (usadas por el script 67 para extender USUARIO_SUPERVISION
      a esa area): CORDOVA CALLE SHIRLEY STEFANY (scordova2 -- colisiona
      con 'scordova' ya usado por CORDOVA LA TORRE SAMIRA, de ahi el
      sufijo), QUINTANA AVILA INES ANA (iquintana). A diferencia de 5.a,
      estas 2 quedan SIN rol ni equipo asignado: su unico dato de origen
      es "CALIFICADOR DE PROCESOS / ADMINISTRATIVO", que no permite
      distinguir si les corresponde rol RECEPCION, ASIGNACION o CONSULTA
      (esa distincion salia de una columna del Excel de origen de este
      script que ellas nunca tuvieron); solo se agregan como usuario +
      relacion de supervision, igual que las 11 personas de SIRCM/Staff
      SDRERC. Falta que un administrador les asigne rol/equipo
      manualmente si corresponde.

   USERNAME: primera letra del primer nombre + primer apellido, en
   minusculas y sin tildes (ej. "ELBA JUANA ALARCON ESPINOZA" -> username
   a partir de primer nombre "ELBA" + primer apellido "ALARCON" =
   "ealarcon"). Para nombres con apellido compuesto que empieza con
   particula (DEL, DE LA, LA) se conserva el apellido compuesto completo
   sin espacios (ej. "DEL CASTILLO" -> "delcastillo"). Colisiones (un solo
   caso: dos personas "REATEGUI" con nombre que empieza con M) se
   resuelven agregando un sufijo numerico a partir de la segunda
   coincidencia, en el orden en que aparecen en el Excel.

   Mapeo de area/nivel de acceso del Excel a rol/equipo SDRERC V2:
     - "ANALISIS Y FUNDAMENTACION LEGAL"          -> rol ABOGADO, equipo EQ_ANALISIS
                                                      (+ rol SUPERVISION si "ROL"=SUPERVISOR)
     - "NOTIFICACION DE DOCUMENTOS"                -> rol NOTIFICACION, equipo EQ_NOTIFICACION
     - "RECEPCION Y ASIGNACION DE EXPEDIENTES",
       nivel de acceso REGISTRO                    -> rol RECEPCION, equipo EQ_REGISTRO
       nivel de acceso ASIGNACION                  -> rol ASIGNACION, equipo EQ_ASIGNACION
       nivel de acceso SUPERVISION                 -> rol SUPERVISION, responsable de EQ_REGISTRO y EQ_ASIGNACION
       nivel de acceso CONSULTA                    -> rol CONSULTA (nuevo, sin equipo)
     - "ACTUALIZACION DEL SIRCM", "STAFF SDRERC"   -> sin rol ni equipo operativo V2 (no
                                                      corresponden a ninguna etapa del
                                                      flujo SDRERC V2; quedan como usuario
                                                      registrado sin acceso funcional hasta
                                                      que se defina uno)

   IMPORTANTE - permisos hoy son fail-open: mientras Administracion > Roles
   no tenga configurados permisos reales para RECEPCION, ASIGNACION,
   ABOGADO, SUPERVISION, NOTIFICACION o el nuevo CONSULTA, cualquier
   usuario autenticado con esos roles vera el menu completo igual (ver
   SessionContext.tienePermiso, fail-open documentado en CLAUDE.md). El
   rol asignado por este script queda listo para cuando se configure la
   matriz de permisos; no restringe el acceso por si solo todavia.

   11 personas quedan SIN rol/equipo asignado por no corresponder a
   ninguna etapa operativa de SDRERC V2 (SIRCM / Staff SDRERC):
     - ARANYA ARANGO ALEXANDRA (STAFF SDRERC / ANALISTA ESTADÍSTICO)
     - ARIAS ALCIDES ERICK GIANCARLO (STAFF SDRERC / ASESOR DE DIRECCIÓN)
     - BERNALDO BASTIDAS NATIVIDAD GLADYS (STAFF SDRERC / ASESOR DE DIRECCIÓN)
     - CAMPOS RAFFO JUAN CARLOS (STAFF SDRERC / ASISTENTE DE RECURSOS HUMANOS)
     - CÁRDENAS VIDAL CLAUDIA LISSETTE (STAFF SDRERC / SECRETARIA)
     - CASTRO OLANO CLAUDIA ELVIRA (ACTUALIZACIÓN DEL SIRCM / CALIFICADOR DE PROCESOS)
     - DIAZ MITMA ANTHONY (ACTUALIZACIÓN DEL SIRCM / ASIGNADOR)
     - PATIÑO VARGAS ERIKA SHEILA (ACTUALIZACIÓN DEL SIRCM / CALIFICADOR DE PROCESOS)
     - ROMERO TORRES MIGUEL ANGEL (ACTUALIZACIÓN DEL SIRCM / SUPERVISOR)
     - TELLO ROJAS DANIEL EDUARDO (STAFF SDRERC / ASISTENTE)
     - VILLAR NUÑEZ DIANE NICOLE (ACTUALIZACIÓN DEL SIRCM / CALIFICADOR DE PROCESOS)

   Idempotente: MERGE por USERNAME/rol/equipo, se puede re-ejecutar sin
   duplicar. No se ejecuto contra ninguna base de datos.
   ============================================================ */

DECLARE
  v_count NUMBER;
BEGIN
  SELECT COUNT(1) INTO v_count
    FROM user_tab_columns
   WHERE table_name = 'USUARIO' AND column_name = 'TELEFONO';

  IF v_count = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE usuario ADD (telefono VARCHAR2(30))';
  END IF;
END;


/* ------------------------------------------------------------
   Rol nuevo: CONSULTA (acceso de solo lectura). No se le asignan
   permisos en este script; configurarlos desde Administracion > Roles >
   "Permisos del rol" cuando se defina que puede ver un consultante.
   ------------------------------------------------------------ */

MERGE INTO rol dst
USING (
  SELECT 'CONSULTA' AS codigo, 'Consulta' AS nombre,
         'Acceso de solo consulta; permisos pendientes de configurar' AS descripcion
    FROM dual
) src
ON (UPPER(dst.codigo) = src.codigo)
WHEN MATCHED THEN UPDATE
  SET dst.nombre = src.nombre,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (codigo, nombre, descripcion, activo)
  VALUES (src.codigo, src.nombre, src.descripcion, 1);

COMMIT;


/* ------------------------------------------------------------
   Usuarios (111 filas, MERGE por USERNAME)
   ------------------------------------------------------------ */

MERGE INTO usuario dst
USING (
  SELECT 'ealarcon' AS username, 'ALARCON ESPINOZA ELBA JUANA' AS nombre_completo, 'DNI' AS tipo_documento, '20055952' AS numero_documento, '969606590' AS telefono FROM dual
  UNION ALL
  SELECT 'gale' AS username, 'ALE RIVAS GLADYS JENY' AS nombre_completo, 'DNI' AS tipo_documento, '02771578' AS numero_documento, '947649536' AS telefono FROM dual
  UNION ALL
  SELECT 'kalhuay' AS username, 'ALHUAY LOZANO KATHYA SIBERIA' AS nombre_completo, 'DNI' AS tipo_documento, '45390583' AS numero_documento, '949272068' AS telefono FROM dual
  UNION ALL
  SELECT 'jalvarado' AS username, 'ALVARADO CAZORLA JOSE PAUL' AS nombre_completo, 'DNI' AS tipo_documento, '41204670' AS numero_documento, '950774451' AS telefono FROM dual
  UNION ALL
  SELECT 'aalvarez' AS username, 'ALVAREZ GARCIA ANA CAROLINA' AS nombre_completo, 'DNI' AS tipo_documento, '40896888' AS numero_documento, '956425270' AS telefono FROM dual
  UNION ALL
  SELECT 'janastacio' AS username, 'ANASTACIO CORDOVA JESSICA ELIZABETH' AS nombre_completo, 'DNI' AS tipo_documento, '46810630' AS numero_documento, '978871946' AS telefono FROM dual
  UNION ALL
  SELECT 'aaranya' AS username, 'ARANYA ARANGO ALEXANDRA' AS nombre_completo, 'DNI' AS tipo_documento, '76260254' AS numero_documento, '951195741' AS telefono FROM dual
  UNION ALL
  SELECT 'earestegui' AS username, 'ARÉSTEGUI CAHUANA ELMER ARNALDO' AS nombre_completo, 'DNI' AS tipo_documento, '02167675' AS numero_documento, '980200300' AS telefono FROM dual
  UNION ALL
  SELECT 'earias' AS username, 'ARIAS ALCIDES ERICK GIANCARLO' AS nombre_completo, 'DNI' AS tipo_documento, '41550958' AS numero_documento, '980560759' AS telefono FROM dual
  UNION ALL
  SELECT 'sarias' AS username, 'ARIAS ESTEBAN SHEYLA LORENA' AS nombre_completo, 'DNI' AS tipo_documento, '42887607' AS numero_documento, '987129298' AS telefono FROM dual
  UNION ALL
  SELECT 'aarmis' AS username, 'ARMIS LIMA ABEL JIMMY' AS nombre_completo, 'DNI' AS tipo_documento, '10184902' AS numero_documento, '989265791' AS telefono FROM dual
  UNION ALL
  SELECT 'rasto' AS username, 'ASTO OCHOA RICARDO ALBERTO' AS nombre_completo, 'DNI' AS tipo_documento, '08316847' AS numero_documento, '999842452' AS telefono FROM dual
  UNION ALL
  SELECT 'fbardales' AS username, 'BARDALES MALDONADO FLOR MARLLORY' AS nombre_completo, 'DNI' AS tipo_documento, '22514266' AS numero_documento, '961634560' AS telefono FROM dual
  UNION ALL
  SELECT 'mbautista' AS username, 'BAUTISTA HUAMANCONDOR MARIELLA MARIA' AS nombre_completo, 'DNI' AS tipo_documento, '07541791' AS numero_documento, '976046593' AS telefono FROM dual
  UNION ALL
  SELECT 'mbazan' AS username, 'BAZAN NEYRA MIRIAN CLOTILDE' AS nombre_completo, 'DNI' AS tipo_documento, '07625811' AS numero_documento, '947034035' AS telefono FROM dual
  UNION ALL
  SELECT 'jbellmunt' AS username, 'BELLMUNT ALLAIN JOSE CRISTOBAL' AS nombre_completo, 'DNI' AS tipo_documento, '25747000' AS numero_documento, '997268331' AS telefono FROM dual
  UNION ALL
  SELECT 'nbernaldo' AS username, 'BERNALDO BASTIDAS NATIVIDAD GLADYS' AS nombre_completo, 'DNI' AS tipo_documento, '40097502' AS numero_documento, '987750444' AS telefono FROM dual
  UNION ALL
  SELECT 'gbruno' AS username, 'BRUNO ARIAS GLENYS GABRIELA' AS nombre_completo, 'DNI' AS tipo_documento, '40078053' AS numero_documento, '988809465' AS telefono FROM dual
  UNION ALL
  SELECT 'pcabana' AS username, 'CABANA AGUILAR PAUL STIVEN' AS nombre_completo, 'DNI' AS tipo_documento, '72665377' AS numero_documento, '987866679' AS telefono FROM dual
  UNION ALL
  SELECT 'lcalixtro' AS username, 'CALIXTRO FERRINI LISSET VERONICA' AS nombre_completo, 'DNI' AS tipo_documento, '10878228' AS numero_documento, '993793104' AS telefono FROM dual
  UNION ALL
  SELECT 'zcalle' AS username, 'CALLE HUAMAN ZAIDA' AS nombre_completo, 'DNI' AS tipo_documento, '28305494' AS numero_documento, '966613348' AS telefono FROM dual
  UNION ALL
  SELECT 'jcampos' AS username, 'CAMPOS RAFFO JUAN CARLOS' AS nombre_completo, 'DNI' AS tipo_documento, '00507971' AS numero_documento, '971092372' AS telefono FROM dual
  UNION ALL
  SELECT 'ncapani' AS username, 'CAPANI ZORRILLA NAOMY ISABEL' AS nombre_completo, 'DNI' AS tipo_documento, '71237695' AS numero_documento, '963822322' AS telefono FROM dual
  UNION ALL
  SELECT 'ccardenas' AS username, 'CÁRDENAS VIDAL CLAUDIA LISSETTE' AS nombre_completo, 'DNI' AS tipo_documento, '44939809' AS numero_documento, '979709508' AS telefono FROM dual
  UNION ALL
  SELECT 'ccasimiro' AS username, 'CASIMIRO JULCA CARMEN BERTHA' AS nombre_completo, 'DNI' AS tipo_documento, '09990392' AS numero_documento, '992960184' AS telefono FROM dual
  UNION ALL
  SELECT 'acastillo' AS username, 'CASTILLO CURAY ANGEL GIANCARLO' AS nombre_completo, 'DNI' AS tipo_documento, '45493001' AS numero_documento, '977254463' AS telefono FROM dual
  UNION ALL
  SELECT 'ccastro' AS username, 'CASTRO OLANO CLAUDIA ELVIRA' AS nombre_completo, 'DNI' AS tipo_documento, '40723041' AS numero_documento, '997764179' AS telefono FROM dual
  UNION ALL
  SELECT 'lceli' AS username, 'CELI SILVA DE RUIZ LIUBEN DEL PILAR' AS nombre_completo, 'DNI' AS tipo_documento, '09866010' AS numero_documento, '943707681' AS telefono FROM dual
  UNION ALL
  SELECT 'rcercado' AS username, 'CERCADO LUMBRERAS ROSA LUZ' AS nombre_completo, 'DNI' AS tipo_documento, '41567196' AS numero_documento, '997079183' AS telefono FROM dual
  UNION ALL
  SELECT 'jcespedes' AS username, 'CESPEDES BERNUY JUAN CARLOS' AS nombre_completo, 'DNI' AS tipo_documento, '40699776' AS numero_documento, '993028382' AS telefono FROM dual
  UNION ALL
  SELECT 'cchampi' AS username, 'CHAMPI MENDOZA CARMEN ROSA' AS nombre_completo, 'DNI' AS tipo_documento, '23982105' AS numero_documento, '974760700' AS telefono FROM dual
  UNION ALL
  SELECT 'achipoco' AS username, 'CHIPOCO SANCHEZ ANGIE MARCELA' AS nombre_completo, 'DNI' AS tipo_documento, '40498856' AS numero_documento, '965730480' AS telefono FROM dual
  UNION ALL
  SELECT 'jchuquispuma' AS username, 'CHUQUISPUMA ESPINOZA, JOEL VLADIMIR' AS nombre_completo, 'DNI' AS tipo_documento, '71019181' AS numero_documento, '996065305' AS telefono FROM dual
  UNION ALL
  SELECT 'scordova' AS username, 'CORDOVA LA TORRE SAMIRA' AS nombre_completo, 'DNI' AS tipo_documento, '28281757' AS numero_documento, '949190901' AS telefono FROM dual
  UNION ALL
  SELECT 'ccotrina' AS username, 'COTRINA MESIAS CINTHYA KAROLAYN' AS nombre_completo, 'DNI' AS tipo_documento, '47508186' AS numero_documento, '912608360' AS telefono FROM dual
  UNION ALL
  SELECT 'jcuri' AS username, 'CURI FIGUEREDO JOYCE CYNTHIA' AS nombre_completo, 'DNI' AS tipo_documento, '42598694' AS numero_documento, '942157684' AS telefono FROM dual
  UNION ALL
  SELECT 'wdelacruz' AS username, 'DE LA CRUZ PIZARRO WILSON' AS nombre_completo, 'DNI' AS tipo_documento, '28297621' AS numero_documento, '975006340' AS telefono FROM dual
  UNION ALL
  SELECT 'hdelaguila' AS username, 'DEL AGUILA PINEDO HEINZ MARLON' AS nombre_completo, 'DNI' AS tipo_documento, '41931404' AS numero_documento, '976783269' AS telefono FROM dual
  UNION ALL
  SELECT 'ldelcastillo' AS username, 'DEL CASTILLO VERA LIEZBETH' AS nombre_completo, 'DNI' AS tipo_documento, '42257735' AS numero_documento, '910446578' AS telefono FROM dual
  UNION ALL
  SELECT 'adiaz' AS username, 'DIAZ MITMA ANTHONY' AS nombre_completo, 'DNI' AS tipo_documento, '42948552' AS numero_documento, '998424581' AS telefono FROM dual
  UNION ALL
  SELECT 'jdiaz' AS username, 'DIAZ ROJAS JAVIER PASCUAL' AS nombre_completo, 'DNI' AS tipo_documento, '28289074' AS numero_documento, '990151863' AS telefono FROM dual
  UNION ALL
  SELECT 'sdioses' AS username, 'DIOSES LEQUERNAQUE SHIRLEY YUVICSA' AS nombre_completo, 'DNI' AS tipo_documento, '40188115' AS numero_documento, '985177308' AS telefono FROM dual
  UNION ALL
  SELECT 'ndonayre' AS username, 'DONAYRE RIVADENEYRA NANCY CARMEN' AS nombre_completo, 'DNI' AS tipo_documento, '28310875' AS numero_documento, '958078216' AS telefono FROM dual
  UNION ALL
  SELECT 'sfernandez' AS username, 'FERNANDEZ FERNANDEZ, SERGIO ORLANDO' AS nombre_completo, 'DNI' AS tipo_documento, '08138287' AS numero_documento, '945764697' AS telefono FROM dual
  UNION ALL
  SELECT 'mfiestas' AS username, 'FIESTAS CHERRE, MANUEL SIXTO' AS nombre_completo, 'DNI' AS tipo_documento, '25625287' AS numero_documento, '945032740' AS telefono FROM dual
  UNION ALL
  SELECT 'cfiestas' AS username, 'FIESTAS SALCEDO, CECILIA YULIANA' AS nombre_completo, 'DNI' AS tipo_documento, '43689253' AS numero_documento, '924102566' AS telefono FROM dual
  UNION ALL
  SELECT 'rgaspar' AS username, 'GASPAR SANCHEZ, ROLANDO IVAN' AS nombre_completo, 'DNI' AS tipo_documento, '42136641' AS numero_documento, '928683248' AS telefono FROM dual
  UNION ALL
  SELECT 'tgil' AS username, 'GIL BERNALES TANIA YSABEL' AS nombre_completo, 'DNI' AS tipo_documento, '41524030' AS numero_documento, '974979709' AS telefono FROM dual
  UNION ALL
  SELECT 'cgozar' AS username, 'GOZAR CASAS CLAUDIA ARACELI' AS nombre_completo, 'DNI' AS tipo_documento, '46650465' AS numero_documento, '956695879' AS telefono FROM dual
  UNION ALL
  SELECT 'jguardapuclla' AS username, 'GUARDAPUCLLA QUISPE JAIME' AS nombre_completo, 'DNI' AS tipo_documento, '23948525' AS numero_documento, '984123961' AS telefono FROM dual
  UNION ALL
  SELECT 'phancco' AS username, 'HANCCO NAVARRO PERCY LEON' AS nombre_completo, 'DNI' AS tipo_documento, '02434360' AS numero_documento, '930316167' AS telefono FROM dual
  UNION ALL
  SELECT 'licochea' AS username, 'ICOCHEA MARTEL LUIS JAIME' AS nombre_completo, 'DNI' AS tipo_documento, '07735888' AS numero_documento, '935900318' AS telefono FROM dual
  UNION ALL
  SELECT 'cilave' AS username, 'ILAVE VELASCO CAROLINE BERTA' AS nombre_completo, 'DNI' AS tipo_documento, '07880669' AS numero_documento, '973104786' AS telefono FROM dual
  UNION ALL
  SELECT 'pjara' AS username, 'JARA CARO PAUL WILLIAM' AS nombre_completo, 'DNI' AS tipo_documento, '32138367' AS numero_documento, '971441673' AS telefono FROM dual
  UNION ALL
  SELECT 'mjimenez' AS username, 'JIMENEZ CONCHA DE SILVA MAGALY' AS nombre_completo, 'DNI' AS tipo_documento, '08676601' AS numero_documento, '965307545' AS telefono FROM dual
  UNION ALL
  SELECT 'yjimenez' AS username, 'JIMENEZ MORVELI YENI' AS nombre_completo, 'DNI' AS tipo_documento, '24008017' AS numero_documento, '984777344' AS telefono FROM dual
  UNION ALL
  SELECT 'clatorre' AS username, 'LA TORRE CASTILLO CARLOS ALCIBIADES CELSO' AS nombre_completo, 'DNI' AS tipo_documento, '10542393' AS numero_documento, '975347074' AS telefono FROM dual
  UNION ALL
  SELECT 'dleon' AS username, 'LEON SANCHEZ DYAN YOMARA' AS nombre_completo, 'DNI' AS tipo_documento, '44512656' AS numero_documento, '939006003' AS telefono FROM dual
  UNION ALL
  SELECT 'slino' AS username, 'LINO QUECAÑO SUSSAN MILAGROS' AS nombre_completo, 'DNI' AS tipo_documento, '47253375' AS numero_documento, '944673328' AS telefono FROM dual
  UNION ALL
  SELECT 'amachuca' AS username, 'MACHUCA VARGAS ARIADNA CLAUDIA' AS nombre_completo, 'DNI' AS tipo_documento, '09930881' AS numero_documento, '994991970' AS telefono FROM dual
  UNION ALL
  SELECT 'mmaque' AS username, 'MAQUE HANCCO MARIA' AS nombre_completo, 'DNI' AS tipo_documento, '29657684' AS numero_documento, '969051886' AS telefono FROM dual
  UNION ALL
  SELECT 'emartinez' AS username, 'MARTINEZ EYZAGUIRRE EVELYN YAHAYRA' AS nombre_completo, 'DNI' AS tipo_documento, '42138922' AS numero_documento, '994927057' AS telefono FROM dual
  UNION ALL
  SELECT 'lmatos' AS username, 'MATOS TORRES LESLY ANDREA' AS nombre_completo, 'DNI' AS tipo_documento, '43538280' AS numero_documento, '989091851' AS telefono FROM dual
  UNION ALL
  SELECT 'mmedina' AS username, 'MEDINA BARRANCA MAYRA ALEJANDRA' AS nombre_completo, 'DNI' AS tipo_documento, '74834385' AS numero_documento, '912774804' AS telefono FROM dual
  UNION ALL
  SELECT 'rmendoza' AS username, 'MENDOZA MOLINA ROSARIO' AS nombre_completo, 'DNI' AS tipo_documento, '06921728' AS numero_documento, '985103764' AS telefono FROM dual
  UNION ALL
  SELECT 'cmilian' AS username, 'MILIAN TORRES CATHERINE MARISELA' AS nombre_completo, 'DNI' AS tipo_documento, '42409195' AS numero_documento, '943237847' AS telefono FROM dual
  UNION ALL
  SELECT 'kmorales' AS username, 'MORALES HIDALGO KARLA SUSANA' AS nombre_completo, 'DNI' AS tipo_documento, '41686355' AS numero_documento, '987926665' AS telefono FROM dual
  UNION ALL
  SELECT 'jmorales' AS username, 'MORALES VASQUEZ JUAN FRANCO' AS nombre_completo, 'DNI' AS tipo_documento, '41649017' AS numero_documento, '943399325' AS telefono FROM dual
  UNION ALL
  SELECT 'smorocho' AS username, 'MOROCHO MARCHAN SILVIA JANET' AS nombre_completo, 'DNI' AS tipo_documento, '02897507' AS numero_documento, '969052482' AS telefono FROM dual
  UNION ALL
  SELECT 'smunante' AS username, 'MUÑANTE LUNA SANDRA VERONICA' AS nombre_completo, 'DNI' AS tipo_documento, '10290327' AS numero_documento, '993317378' AS telefono FROM dual
  UNION ALL
  SELECT 'mpachas' AS username, 'PACHAS ALMEYDA MARIA VIVIANA' AS nombre_completo, 'DNI' AS tipo_documento, '41162415' AS numero_documento, '950403764' AS telefono FROM dual
  UNION ALL
  SELECT 'rpantoja' AS username, 'PANTOJA LAZARO ROGGER OMAR' AS nombre_completo, 'DNI' AS tipo_documento, '07528598' AS numero_documento, '987186516' AS telefono FROM dual
  UNION ALL
  SELECT 'mparedes' AS username, 'PAREDES GUTIERREZ MILIPSA OLGA' AS nombre_completo, 'DNI' AS tipo_documento, '29558559' AS numero_documento, '989172760' AS telefono FROM dual
  UNION ALL
  SELECT 'epatino' AS username, 'PATIÑO VARGAS ERIKA SHEILA' AS nombre_completo, 'DNI' AS tipo_documento, '40549759' AS numero_documento, '956598444' AS telefono FROM dual
  UNION ALL
  SELECT 'jpinto' AS username, 'PINTO WEHRLE, JUANA CARLOTA' AS nombre_completo, 'DNI' AS tipo_documento, '10338926' AS numero_documento, '967443966' AS telefono FROM dual
  UNION ALL
  SELECT 'fpomalia' AS username, 'POMALIA ALVARO FIORELLA JULIA' AS nombre_completo, 'DNI' AS tipo_documento, '70151596' AS numero_documento, '980532664' AS telefono FROM dual
  UNION ALL
  SELECT 'dpuch' AS username, 'PUCH PARDO FIGUEROA DAVID EDUARDO' AS nombre_completo, 'DNI' AS tipo_documento, '10867251' AS numero_documento, '912114751' AS telefono FROM dual
  UNION ALL
  SELECT 'jquerevalu' AS username, 'QUEREVALÚ PÉREZ JESSICA GLADYS' AS nombre_completo, 'DNI' AS tipo_documento, '10722535' AS numero_documento, '997216650' AS telefono FROM dual
  UNION ALL
  SELECT 'nquispe' AS username, 'QUISPE ALFARO NILDA' AS nombre_completo, 'DNI' AS tipo_documento, '28306240' AS numero_documento, '948430310' AS telefono FROM dual
  UNION ALL
  SELECT 'rramirez' AS username, 'RAMIREZ ZAPATA RUTH DEL SOCORRO' AS nombre_completo, 'DNI' AS tipo_documento, '08469317' AS numero_documento, '997371267' AS telefono FROM dual
  UNION ALL
  SELECT 'cramos' AS username, 'RAMOS CAMPOS CARMEN LUISA' AS nombre_completo, 'DNI' AS tipo_documento, '07504333' AS numero_documento, '962226302' AS telefono FROM dual
  UNION ALL
  SELECT 'mreategui' AS username, 'REATEGUI BRANDYCH MEGGIE' AS nombre_completo, 'DNI' AS tipo_documento, '47449940' AS numero_documento, '987130392' AS telefono FROM dual
  UNION ALL
  SELECT 'mreategui2' AS username, 'REATEGUI KOCLENG MAIKOL STIFF' AS nombre_completo, 'DNI' AS tipo_documento, '06816346' AS numero_documento, '937022943' AS telefono FROM dual
  UNION ALL
  SELECT 'irios' AS username, 'RIOS IBAÑEZ IVAN' AS nombre_completo, 'DNI' AS tipo_documento, '40844660' AS numero_documento, '949271056' AS telefono FROM dual
  UNION ALL
  SELECT 'frivas' AS username, 'RIVAS CIRIACO FIORELLA FATIMA' AS nombre_completo, 'DNI' AS tipo_documento, '46614373' AS numero_documento, '953298903' AS telefono FROM dual
  UNION ALL
  SELECT 'yrivera' AS username, 'RIVERA ARAUCO, YANET PAOLA' AS nombre_completo, 'DNI' AS tipo_documento, '40678662' AS numero_documento, '913105900' AS telefono FROM dual
  UNION ALL
  SELECT 'grodas' AS username, 'RODAS VALLE GABRIELA ANATOLIA' AS nombre_completo, 'DNI' AS tipo_documento, '10548429' AS numero_documento, '988055914' AS telefono FROM dual
  UNION ALL
  SELECT 'hrojas' AS username, 'ROJAS PORRAS HUGO ENRIQUE' AS nombre_completo, 'DNI' AS tipo_documento, '19925764' AS numero_documento, '945363645' AS telefono FROM dual
  UNION ALL
  SELECT 'lromani' AS username, 'ROMANI CAHUANA LUIS ENRIQUE' AS nombre_completo, 'DNI' AS tipo_documento, '76061825' AS numero_documento, '936364414' AS telefono FROM dual
  UNION ALL
  SELECT 'rromero' AS username, 'ROMERO OROZCO, REBECA ANGELICA' AS nombre_completo, 'DNI' AS tipo_documento, '76481508' AS numero_documento, '942251283' AS telefono FROM dual
  UNION ALL
  SELECT 'mromero' AS username, 'ROMERO TORRES MIGUEL ANGEL' AS nombre_completo, 'DNI' AS tipo_documento, '18836708' AS numero_documento, '951515788' AS telefono FROM dual
  UNION ALL
  SELECT 'tsalazar' AS username, 'SALAZAR QUIÑONEZ, TERESA DILAINES' AS nombre_completo, 'DNI' AS tipo_documento, '44867877' AS numero_documento, '994422152' AS telefono FROM dual
  UNION ALL
  SELECT 'ssalazar' AS username, 'SALAZAR ZUMAETA SANDRA JESSICA' AS nombre_completo, 'DNI' AS tipo_documento, '06795095' AS numero_documento, '999989664' AS telefono FROM dual
  UNION ALL
  SELECT 'msalvador' AS username, 'SALVADOR SEGURA, MARIA ALEXANDRA SUSANA' AS nombre_completo, 'DNI' AS tipo_documento, '48211156' AS numero_documento, '962678607' AS telefono FROM dual
  UNION ALL
  SELECT 'lsanchez' AS username, 'SANCHEZ MARTINEZ LUIS FELIPE' AS nombre_completo, 'DNI' AS tipo_documento, '40835456' AS numero_documento, '936511440' AS telefono FROM dual
  UNION ALL
  SELECT 'jsantiago' AS username, 'SANTIAGO RODRIGUEZ JULIO CESAR' AS nombre_completo, 'DNI' AS tipo_documento, '09766158' AS numero_documento, '943514064' AS telefono FROM dual
  UNION ALL
  SELECT 'rtang' AS username, 'TANG MARTINEZ ROBERTO MARCOS' AS nombre_completo, 'DNI' AS tipo_documento, '07877933' AS numero_documento, '943686061' AS telefono FROM dual
  UNION ALL
  SELECT 'rtapia' AS username, 'TAPIA GONZALES ROSA YANET' AS nombre_completo, 'DNI' AS tipo_documento, '10089503' AS numero_documento, '994705771' AS telefono FROM dual
  UNION ALL
  SELECT 'dtello' AS username, 'TELLO ROJAS DANIEL EDUARDO' AS nombre_completo, 'DNI' AS tipo_documento, '40441109' AS numero_documento, '990020896' AS telefono FROM dual
  UNION ALL
  SELECT 'ltuesta' AS username, 'TUESTA VIENA LLESY LEYDIT' AS nombre_completo, 'DNI' AS tipo_documento, '18173696' AS numero_documento, '976986611' AS telefono FROM dual
  UNION ALL
  SELECT 'ntumialan' AS username, 'TUMIALAN FABIAN NANCY' AS nombre_completo, 'DNI' AS tipo_documento, '10744196' AS numero_documento, '985935716' AS telefono FROM dual
  UNION ALL
  SELECT 'ntupacyupanqui' AS username, 'TUPACYUPANQUI GARAGATTI, NATALI INES ALEJANDRA' AS nombre_completo, 'DNI' AS tipo_documento, '46551872' AS numero_documento, '995228659' AS telefono FROM dual
  UNION ALL
  SELECT 'etupayachi' AS username, 'TUPAYACHI ROMERO ENZO' AS nombre_completo, 'DNI' AS tipo_documento, '23978862' AS numero_documento, '974206777' AS telefono FROM dual
  UNION ALL
  SELECT 'cvargas' AS username, 'VARGAS POMEZ CARLOS ENRIQUE' AS nombre_completo, 'DNI' AS tipo_documento, '21527934' AS numero_documento, '916725036' AS telefono FROM dual
  UNION ALL
  SELECT 'gvasquez' AS username, 'VASQUEZ DE LA CRUZ GIANNINA MAGDA' AS nombre_completo, 'DNI' AS tipo_documento, '40565346' AS numero_documento, '968158444' AS telefono FROM dual
  UNION ALL
  SELECT 'fvasquez' AS username, 'VASQUEZ MORAN FELICIO SAMUEL' AS nombre_completo, 'DNI' AS tipo_documento, '08518813' AS numero_documento, '955 830 786' AS telefono FROM dual
  UNION ALL
  SELECT 'mvasquez' AS username, 'VASQUEZ VELASQUEZ, MARCO ANTONIO' AS nombre_completo, 'DNI' AS tipo_documento, '45819213' AS numero_documento, '949070013' AS telefono FROM dual
  UNION ALL
  SELECT 'nvereau' AS username, 'VEREAU FLORES NANCY SIDNEY' AS nombre_completo, 'DNI' AS tipo_documento, '40130956' AS numero_documento, '999292427' AS telefono FROM dual
  UNION ALL
  SELECT 'dvillar' AS username, 'VILLAR NUÑEZ DIANE NICOLE' AS nombre_completo, 'DNI' AS tipo_documento, '76416420' AS numero_documento, '991432772' AS telefono FROM dual
  UNION ALL
  SELECT 'fzapana' AS username, 'ZAPANA CARO DE BARRETO, FLOR ESTELA' AS nombre_completo, 'DNI' AS tipo_documento, '10753893' AS numero_documento, '999479015' AS telefono FROM dual
  UNION ALL
  SELECT 'azegarra' AS username, 'ZEGARRA RAMOS, ABEL AUGUSTO' AS nombre_completo, 'DNI' AS tipo_documento, '44664575' AS numero_documento, '994522812' AS telefono FROM dual
  UNION ALL
  SELECT 'mlopez' AS username, 'LOPEZ CHIQUIHUANCA MONICA PATRICIA' AS nombre_completo, NULL AS tipo_documento, NULL AS numero_documento, NULL AS telefono FROM dual
  UNION ALL
  SELECT 'smontenegro' AS username, 'MONTENEGRO CARBAJAL SANDRA MARIBEL' AS nombre_completo, NULL AS tipo_documento, NULL AS numero_documento, NULL AS telefono FROM dual
  UNION ALL
  SELECT 'jpuse' AS username, 'PUSE SANTILLAN JENIFFER MILAGROS' AS nombre_completo, NULL AS tipo_documento, NULL AS numero_documento, NULL AS telefono FROM dual
  UNION ALL
  SELECT 'acortez' AS username, 'CORTEZ PITA ALEXANDRA KAREN' AS nombre_completo, NULL AS tipo_documento, NULL AS numero_documento, NULL AS telefono FROM dual
  UNION ALL
  SELECT 'gfernandez' AS username, 'FERNANDEZ OBANDO GINO AXCEL' AS nombre_completo, NULL AS tipo_documento, NULL AS numero_documento, NULL AS telefono FROM dual
  UNION ALL
  SELECT 'scordova2' AS username, 'CORDOVA CALLE SHIRLEY STEFANY' AS nombre_completo, NULL AS tipo_documento, NULL AS numero_documento, NULL AS telefono FROM dual
  UNION ALL
  SELECT 'iquintana' AS username, 'QUINTANA AVILA INES ANA' AS nombre_completo, NULL AS tipo_documento, NULL AS numero_documento, NULL AS telefono FROM dual
) src
ON (UPPER(dst.username) = UPPER(src.username))
WHEN NOT MATCHED THEN
  INSERT (username, nombre_completo, tipo_documento, numero_documento, telefono, estado, activo, creado_en)
  VALUES (src.username, src.nombre_completo, src.tipo_documento, src.numero_documento, src.telefono, 'ACTIVO', 1, SYSTIMESTAMP);

COMMIT;


/* ------------------------------------------------------------
   Roles por usuario (104 filas)
   ------------------------------------------------------------ */

MERGE INTO usuario_rol dst
USING (
  SELECT u.id_usuario AS id_usuario, r.id_rol AS id_rol
    FROM (
    SELECT 'ealarcon' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'gale' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'kalhuay' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jalvarado' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'aalvarez' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'janastacio' AS username, 'CONSULTA' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'earestegui' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'sarias' AS username, 'RECEPCION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'aarmis' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'rasto' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'fbardales' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mbautista' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mbazan' AS username, 'RECEPCION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jbellmunt' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'gbruno' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'gbruno' AS username, 'SUPERVISION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'pcabana' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'lcalixtro' AS username, 'SUPERVISION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'zcalle' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'ncapani' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'ccasimiro' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'acastillo' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'lceli' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'lceli' AS username, 'SUPERVISION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'rcercado' AS username, 'CONSULTA' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jcespedes' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'cchampi' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'achipoco' AS username, 'RECEPCION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jchuquispuma' AS username, 'RECEPCION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'scordova' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'ccotrina' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jcuri' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'wdelacruz' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'hdelaguila' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'ldelcastillo' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jdiaz' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'sdioses' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'ndonayre' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'sfernandez' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mfiestas' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'cfiestas' AS username, 'CONSULTA' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'rgaspar' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'tgil' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'cgozar' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jguardapuclla' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'phancco' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'licochea' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'cilave' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'pjara' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mjimenez' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'yjimenez' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'clatorre' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'dleon' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'slino' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'amachuca' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mmaque' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'emartinez' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'lmatos' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mmedina' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'rmendoza' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'cmilian' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'kmorales' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jmorales' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'smorocho' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'smunante' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mpachas' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'rpantoja' AS username, 'ASIGNACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mparedes' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jpinto' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'fpomalia' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'dpuch' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jquerevalu' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'nquispe' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'rramirez' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'cramos' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mreategui' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mreategui2' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'irios' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'frivas' AS username, 'CONSULTA' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'yrivera' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'grodas' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'hrojas' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'lromani' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'rromero' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'tsalazar' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'ssalazar' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'msalvador' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'lsanchez' AS username, 'CONSULTA' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jsantiago' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jsantiago' AS username, 'SUPERVISION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'rtang' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'rtapia' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'ltuesta' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'ntumialan' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'ntupacyupanqui' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'etupayachi' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'cvargas' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'gvasquez' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'fvasquez' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mvasquez' AS username, 'NOTIFICACION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'nvereau' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'fzapana' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'fzapana' AS username, 'SUPERVISION' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'azegarra' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'mlopez' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'smontenegro' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'jpuse' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'acortez' AS username, 'ABOGADO' AS rol_codigo FROM dual
    UNION ALL
    SELECT 'gfernandez' AS username, 'ABOGADO' AS rol_codigo FROM dual
    ) pares
    JOIN usuario u ON UPPER(u.username) = UPPER(pares.username)
    JOIN rol r ON UPPER(r.codigo) = pares.rol_codigo
) src
ON (dst.id_usuario = src.id_usuario AND dst.id_rol = src.id_rol)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_usuario, id_rol, activo, creado_en)
  VALUES (src.id_usuario, src.id_rol, 1, SYSTIMESTAMP);

COMMIT;


/* ------------------------------------------------------------
   Equipos por usuario (96 filas)
   ------------------------------------------------------------ */

MERGE INTO equipo_usuario dst
USING (
  SELECT u.id_usuario AS id_usuario, eq.id_equipo AS id_equipo, pares.es_responsable AS es_responsable
    FROM (
    SELECT 'ealarcon' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'gale' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'kalhuay' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jalvarado' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'aalvarez' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'earestegui' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'sarias' AS username, 'EQ_REGISTRO' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'aarmis' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'rasto' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'fbardales' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mbautista' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mbazan' AS username, 'EQ_REGISTRO' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jbellmunt' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'gbruno' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'pcabana' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'lcalixtro' AS username, 'EQ_REGISTRO' AS equipo_codigo, 1 AS es_responsable FROM dual
    UNION ALL
    SELECT 'lcalixtro' AS username, 'EQ_ASIGNACION' AS equipo_codigo, 1 AS es_responsable FROM dual
    UNION ALL
    SELECT 'zcalle' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'ncapani' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'ccasimiro' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'acastillo' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'lceli' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jcespedes' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'cchampi' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'achipoco' AS username, 'EQ_REGISTRO' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jchuquispuma' AS username, 'EQ_REGISTRO' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'scordova' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'ccotrina' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jcuri' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'wdelacruz' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'hdelaguila' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'ldelcastillo' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jdiaz' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'sdioses' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'ndonayre' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'sfernandez' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mfiestas' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'rgaspar' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'tgil' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'cgozar' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jguardapuclla' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'phancco' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'licochea' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'cilave' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'pjara' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mjimenez' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'yjimenez' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'clatorre' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'dleon' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'slino' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'amachuca' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mmaque' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'emartinez' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'lmatos' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mmedina' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'rmendoza' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'cmilian' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'kmorales' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jmorales' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'smorocho' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'smunante' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mpachas' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'rpantoja' AS username, 'EQ_ASIGNACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mparedes' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jpinto' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'fpomalia' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'dpuch' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jquerevalu' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'nquispe' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'rramirez' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'cramos' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mreategui' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mreategui2' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'irios' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'yrivera' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'grodas' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'hrojas' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'lromani' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'rromero' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'tsalazar' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'ssalazar' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'msalvador' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jsantiago' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'rtang' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'rtapia' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'ltuesta' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'ntumialan' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'ntupacyupanqui' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'etupayachi' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'cvargas' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'gvasquez' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'fvasquez' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mvasquez' AS username, 'EQ_NOTIFICACION' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'nvereau' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'fzapana' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'azegarra' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'mlopez' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'smontenegro' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'jpuse' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'acortez' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    UNION ALL
    SELECT 'gfernandez' AS username, 'EQ_ANALISIS' AS equipo_codigo, 0 AS es_responsable FROM dual
    ) pares
    JOIN usuario u ON UPPER(u.username) = UPPER(pares.username)
    JOIN equipo eq ON UPPER(eq.codigo) = pares.equipo_codigo
) src
ON (dst.id_usuario = src.id_usuario AND dst.id_equipo = src.id_equipo)
WHEN MATCHED THEN UPDATE
  SET dst.es_responsable = src.es_responsable,
      dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_equipo, id_usuario, es_responsable, activo, creado_en)
  VALUES (src.id_equipo, src.id_usuario, src.es_responsable, 1, SYSTIMESTAMP);

COMMIT;


/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT COUNT(*) AS total_usuarios_cargados FROM usuario
 WHERE username IN (
  'ealarcon',
  'gale',
  'kalhuay',
  'jalvarado',
  'aalvarez',
  'janastacio',
  'aaranya',
  'earestegui',
  'earias',
  'sarias',
  'aarmis',
  'rasto',
  'fbardales',
  'mbautista',
  'mbazan',
  'jbellmunt',
  'nbernaldo',
  'gbruno',
  'pcabana',
  'lcalixtro',
  'zcalle',
  'jcampos',
  'ncapani',
  'ccardenas',
  'ccasimiro',
  'acastillo',
  'ccastro',
  'lceli',
  'rcercado',
  'jcespedes',
  'cchampi',
  'achipoco',
  'jchuquispuma',
  'scordova',
  'ccotrina',
  'jcuri',
  'wdelacruz',
  'hdelaguila',
  'ldelcastillo',
  'adiaz',
  'jdiaz',
  'sdioses',
  'ndonayre',
  'sfernandez',
  'mfiestas',
  'cfiestas',
  'rgaspar',
  'tgil',
  'cgozar',
  'jguardapuclla',
  'phancco',
  'licochea',
  'cilave',
  'pjara',
  'mjimenez',
  'yjimenez',
  'clatorre',
  'dleon',
  'slino',
  'amachuca',
  'mmaque',
  'emartinez',
  'lmatos',
  'mmedina',
  'rmendoza',
  'cmilian',
  'kmorales',
  'jmorales',
  'smorocho',
  'smunante',
  'mpachas',
  'rpantoja',
  'mparedes',
  'epatino',
  'jpinto',
  'fpomalia',
  'dpuch',
  'jquerevalu',
  'nquispe',
  'rramirez',
  'cramos',
  'mreategui',
  'mreategui2',
  'irios',
  'frivas',
  'yrivera',
  'grodas',
  'hrojas',
  'lromani',
  'rromero',
  'mromero',
  'tsalazar',
  'ssalazar',
  'msalvador',
  'lsanchez',
  'jsantiago',
  'rtang',
  'rtapia',
  'dtello',
  'ltuesta',
  'ntumialan',
  'ntupacyupanqui',
  'etupayachi',
  'cvargas',
  'gvasquez',
  'fvasquez',
  'mvasquez',
  'nvereau',
  'dvillar',
  'fzapana',
  'mlopez',
  'smontenegro',
  'jpuse',
  'acortez',
  'gfernandez',
  'scordova2',
  'iquintana',
  'azegarra');

SELECT r.codigo AS rol, COUNT(*) AS usuarios
  FROM usuario_rol ur
  JOIN usuario u ON u.id_usuario = ur.id_usuario
  JOIN rol r ON r.id_rol = ur.id_rol
 WHERE ur.activo = 1
   AND u.username IN (
  'ealarcon',
  'gale',
  'kalhuay',
  'jalvarado',
  'aalvarez',
  'janastacio',
  'aaranya',
  'earestegui',
  'earias',
  'sarias',
  'aarmis',
  'rasto',
  'fbardales',
  'mbautista',
  'mbazan',
  'jbellmunt',
  'nbernaldo',
  'gbruno',
  'pcabana',
  'lcalixtro',
  'zcalle',
  'jcampos',
  'ncapani',
  'ccardenas',
  'ccasimiro',
  'acastillo',
  'ccastro',
  'lceli',
  'rcercado',
  'jcespedes',
  'cchampi',
  'achipoco',
  'jchuquispuma',
  'scordova',
  'ccotrina',
  'jcuri',
  'wdelacruz',
  'hdelaguila',
  'ldelcastillo',
  'adiaz',
  'jdiaz',
  'sdioses',
  'ndonayre',
  'sfernandez',
  'mfiestas',
  'cfiestas',
  'rgaspar',
  'tgil',
  'cgozar',
  'jguardapuclla',
  'phancco',
  'licochea',
  'cilave',
  'pjara',
  'mjimenez',
  'yjimenez',
  'clatorre',
  'dleon',
  'slino',
  'amachuca',
  'mmaque',
  'emartinez',
  'lmatos',
  'mmedina',
  'rmendoza',
  'cmilian',
  'kmorales',
  'jmorales',
  'smorocho',
  'smunante',
  'mpachas',
  'rpantoja',
  'mparedes',
  'epatino',
  'jpinto',
  'fpomalia',
  'dpuch',
  'jquerevalu',
  'nquispe',
  'rramirez',
  'cramos',
  'mreategui',
  'mreategui2',
  'irios',
  'frivas',
  'yrivera',
  'grodas',
  'hrojas',
  'lromani',
  'rromero',
  'mromero',
  'tsalazar',
  'ssalazar',
  'msalvador',
  'lsanchez',
  'jsantiago',
  'rtang',
  'rtapia',
  'dtello',
  'ltuesta',
  'ntumialan',
  'ntupacyupanqui',
  'etupayachi',
  'cvargas',
  'gvasquez',
  'fvasquez',
  'mvasquez',
  'nvereau',
  'dvillar',
  'fzapana',
  'mlopez',
  'smontenegro',
  'jpuse',
  'acortez',
  'gfernandez',
  'scordova2',
  'iquintana',
  'azegarra')
 GROUP BY r.codigo
 ORDER BY r.codigo;

SELECT eq.codigo AS equipo, COUNT(*) AS usuarios,
       SUM(CASE WHEN eu.es_responsable = 1 THEN 1 ELSE 0 END) AS responsables
  FROM equipo_usuario eu
  JOIN usuario u ON u.id_usuario = eu.id_usuario
  JOIN equipo eq ON eq.id_equipo = eu.id_equipo
 WHERE eu.activo = 1
   AND u.username IN (
  'ealarcon',
  'gale',
  'kalhuay',
  'jalvarado',
  'aalvarez',
  'janastacio',
  'aaranya',
  'earestegui',
  'earias',
  'sarias',
  'aarmis',
  'rasto',
  'fbardales',
  'mbautista',
  'mbazan',
  'jbellmunt',
  'nbernaldo',
  'gbruno',
  'pcabana',
  'lcalixtro',
  'zcalle',
  'jcampos',
  'ncapani',
  'ccardenas',
  'ccasimiro',
  'acastillo',
  'ccastro',
  'lceli',
  'rcercado',
  'jcespedes',
  'cchampi',
  'achipoco',
  'jchuquispuma',
  'scordova',
  'ccotrina',
  'jcuri',
  'wdelacruz',
  'hdelaguila',
  'ldelcastillo',
  'adiaz',
  'jdiaz',
  'sdioses',
  'ndonayre',
  'sfernandez',
  'mfiestas',
  'cfiestas',
  'rgaspar',
  'tgil',
  'cgozar',
  'jguardapuclla',
  'phancco',
  'licochea',
  'cilave',
  'pjara',
  'mjimenez',
  'yjimenez',
  'clatorre',
  'dleon',
  'slino',
  'amachuca',
  'mmaque',
  'emartinez',
  'lmatos',
  'mmedina',
  'rmendoza',
  'cmilian',
  'kmorales',
  'jmorales',
  'smorocho',
  'smunante',
  'mpachas',
  'rpantoja',
  'mparedes',
  'epatino',
  'jpinto',
  'fpomalia',
  'dpuch',
  'jquerevalu',
  'nquispe',
  'rramirez',
  'cramos',
  'mreategui',
  'mreategui2',
  'irios',
  'frivas',
  'yrivera',
  'grodas',
  'hrojas',
  'lromani',
  'rromero',
  'mromero',
  'tsalazar',
  'ssalazar',
  'msalvador',
  'lsanchez',
  'jsantiago',
  'rtang',
  'rtapia',
  'dtello',
  'ltuesta',
  'ntumialan',
  'ntupacyupanqui',
  'etupayachi',
  'cvargas',
  'gvasquez',
  'fvasquez',
  'mvasquez',
  'nvereau',
  'dvillar',
  'fzapana',
  'mlopez',
  'smontenegro',
  'jpuse',
  'acortez',
  'gfernandez',
  'scordova2',
  'iquintana',
  'azegarra')
 GROUP BY eq.codigo
 ORDER BY eq.codigo;

-- Usuarios sin ningun rol asignado (SIRCM / Staff SDRERC, ver cabecera del script)
SELECT u.username, u.nombre_completo
  FROM usuario u
 WHERE u.username IN (
  'aaranya',
  'earias',
  'nbernaldo',
  'jcampos',
  'ccardenas',
  'ccastro',
  'adiaz',
  'epatino',
  'mromero',
  'dtello',
  'dvillar')
   AND NOT EXISTS (SELECT 1 FROM usuario_rol ur WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1)
 ORDER BY u.username;
