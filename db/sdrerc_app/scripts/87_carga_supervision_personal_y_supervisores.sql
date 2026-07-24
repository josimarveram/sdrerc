/* ============================================================
   SCRIPT 87 - Carga de supervision abogado/supervisor
   (fuente: hoja "PERSONAL Y SUPERVISORES", todas las areas)
   Ejecutar conectado como SDRERC_APP.

   Fuente: docs/arquitectura_app/personal_supervisores.xlsx, hoja
   "PERSONAL Y SUPERVISORES" (117 filas de encabezado/leyenda incluidas,
   105 personas con datos reales, filas 7 a 111 de la hoja). A diferencia
   de las hojas "Abogados"/"Abogados 2026" que ya uso el script 64 (solo
   area Analisis, 74 relaciones), esta hoja trae un supervisor por
   persona de TODAS las areas (Analisis y fundamentacion legal,
   Notificacion de documentos, Recepcion y asignacion de expedientes,
   Actualizacion del SIRCM, Generacion expediente digital, Staff SDRERC).
   Es la fuente mas completa y reciente disponible; complementa (no
   reemplaza fisicamente) lo que ya dejaron los scripts 64/67, que
   siguen sin ejecutarse.

   Mecanismo pedido explicitamente por el usuario: si el abogado o el
   supervisor del Excel no existen en la tabla USUARIO, la fila se omite
   automaticamente por la propia consulta (JOIN, no filtro manual
   previo):
     - El nombre del abogado ("APELLIDOS Y NOMBRES", columna B de la
       hoja) se compara contra USUARIO.NOMBRE_COMPLETO normalizando
       mayusculas/espacios y tildes; si no hay match, la fila
       simplemente no aparece en el resultado del JOIN.
     - El supervisor viene en la hoja como "NOMBRE APELLIDO" corto (ej.
       "SHIRLEY DIOSES"), no como nombre completo. Se resuelve contra
       los 8 supervisores de area ya identificados a mano en el script
       64 (mismo mapeo, reutilizado tal cual: SHIRLEY DIOSES=sdioses,
       JULIO SANTIAGO=jsantiago, JUANA PINTO=jpinto, GLENYS BRUNO=gbruno,
       ARIADNA MACHUCA=amachuca, LIUBEN CELI=lceli, FLOR ZAPANA=fzapana,
       LISSET CALIXTRO=lcalixtro). Cualquier otro valor de la columna
       SUPERVISOR (en la practica: "SUBDIRECTOR", que aparece para los
       propios supervisores y para el personal de Staff SDRERC, y el
       texto literal "SUPERVISOR" en 1 fila de Notificacion, el mismo
       caso ya documentado en el script 64) no resuelve a ningun
       username y la fila se omite igual, sin necesidad de listarla
       aparte.

   Requiere haber ejecutado antes el script 63 (carga de los 111
   usuarios): este script solo agrega la relacion USUARIO_SUPERVISION
   entre usuarios que YA deben existir; no crea usuarios nuevos.

   Nota: este script NO toca ni reemplaza el rol/equipo ya asignado por
   el script 63; solo agrega el vinculo adicional supervisor-abogado en
   USUARIO_SUPERVISION, relacion de reporte funcional distinta de la
   asignacion de equipo/rol (modulo Equipo Juridico > "Personal por
   supervisor").

   Idempotente: MERGE por (id_supervisor, id_abogado), se puede
   re-ejecutar sin duplicar. No se ejecuto contra ninguna base de datos.
   ============================================================ */

MERGE INTO usuario_supervision dst
USING (
  SELECT sup.id_usuario AS id_supervisor, ab.id_usuario AS id_abogado
    FROM (
      SELECT
        pares.abogado_nombre,
        CASE UPPER(pares.supervisor_corto)
          WHEN 'SHIRLEY DIOSES'  THEN 'sdioses'
          WHEN 'JULIO SANTIAGO'  THEN 'jsantiago'
          WHEN 'JUANA PINTO'     THEN 'jpinto'
          WHEN 'GLENYS BRUNO'    THEN 'gbruno'
          WHEN 'ARIADNA MACHUCA' THEN 'amachuca'
          WHEN 'LIUBEN CELI'     THEN 'lceli'
          WHEN 'FLOR ZAPANA'     THEN 'fzapana'
          WHEN 'LISSET CALIXTRO' THEN 'lcalixtro'
          ELSE NULL
        END AS supervisor_username
      FROM (
    SELECT Q'[ALARCON ESPINOZA ELBA JUANA]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ALE RIVAS GLADYS JENY]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ALHUAY LOZANO KATHYA SIBERIA]' AS abogado_nombre, 'ARIADNA MACHUCA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ALVARADO CAZORLA JOSE PAUL]' AS abogado_nombre, 'JULIO SANTIAGO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ALVAREZ GARCIA ANA CAROLINA]' AS abogado_nombre, 'JULIO SANTIAGO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ANASTACIO CORDOVA JESSICA ELIZABETH]' AS abogado_nombre, 'LISSET CALIXTRO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ARANYA ARANGO ALEXANDRA]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ARÉSTEGUI CAHUANA ELMER ARNALDO]' AS abogado_nombre, 'JUANA PINTO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ARIAS ALCIDES ERICK GIANCARLO]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ARMIS LIMA ABEL JIMMY]' AS abogado_nombre, 'JULIO SANTIAGO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ASTO OCHOA RICARDO ALBERTO]' AS abogado_nombre, 'ARIADNA MACHUCA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[BARDALES MALDONADO FLOR MARLLORY]' AS abogado_nombre, 'LIUBEN CELI' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[BAZAN NEYRA MIRIAN CLOTILDE]' AS abogado_nombre, 'LISSET CALIXTRO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[BAUTISTA HUAMANCONDOR MARIELLA MARIA]' AS abogado_nombre, 'GLENYS BRUNO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[BELLMUNT ALLAIN JOSE CRISTOBAL]' AS abogado_nombre, 'GLENYS BRUNO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[BERNALDO BASTIDAS NATIVIDAD GLADYS]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[BRUNO ARIAS GLENYS GABRIELA]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CALIXTRO FERRINI LISSET VERONICA]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CALLE HUAMAN ZAIDA]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CABANA AGUILAR PAUL STIVEN]' AS abogado_nombre, 'LIUBEN CELI' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CAMPOS RAFFO JUAN CARLOS]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CÁRDENAS VIDAL CLAUDIA LISSETTE]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CASIMIRO JULCA CARMEN BERTHA]' AS abogado_nombre, 'ARIADNA MACHUCA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CASTILLO CURAY ANGEL GIANCARLO]' AS abogado_nombre, 'FLOR ZAPANA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CASTRO OLANO CLAUDIA ELVIRA]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CELI SILVA DE RUIZ LIUBEN DEL PILAR]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CERCADO LUMBRERAS ROSA LUZ]' AS abogado_nombre, 'LISSET CALIXTRO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CESPEDES BERNUY JUAN CARLOS]' AS abogado_nombre, 'ARIADNA MACHUCA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CHAMPI MENDOZA CARMEN ROSA]' AS abogado_nombre, 'JUANA PINTO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CHUQUISPUMA ESPINOZA JOEL VLADIMIR]' AS abogado_nombre, 'LISSET CALIXTRO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CORDOVA CALLE SHIRLEY STEFANY]' AS abogado_nombre, 'LISSET CALIXTRO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CORDOVA LA TORRE SAMIRA]' AS abogado_nombre, 'JUANA PINTO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[COTRINA MESIAS CINTHYA KAROLAYN]' AS abogado_nombre, 'JULIO SANTIAGO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[CURI FIGUEREDO JOYCE CYNTHIA]' AS abogado_nombre, 'JULIO SANTIAGO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[DE LA CRUZ PIZARRO WILSON]' AS abogado_nombre, 'LIUBEN CELI' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[DEL AGUILA PINEDO HEINZ MARLON]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[DEL CASTILLO VERA LIEZBETH]' AS abogado_nombre, 'GLENYS BRUNO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[DIAZ MITMA ANTHONY]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[DIAZ ROJAS JAVIER PASCUAL]' AS abogado_nombre, 'GLENYS BRUNO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[DIOSES LEQUERNAQUE SHIRLEY YUVICSA]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[DONAYRE RIVADENEYRA NANCY CARMEN]' AS abogado_nombre, 'JULIO SANTIAGO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[FERNANDEZ FERNANDEZ SERGIO ORLANDO]' AS abogado_nombre, 'ARIADNA MACHUCA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[FIESTAS CHERRE MANUEL SIXTO]' AS abogado_nombre, 'JULIO SANTIAGO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[GASPAR SANCHEZ ROLANDO IVAN]' AS abogado_nombre, 'SUPERVISOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[GIL BERNALES TANIA YSABEL]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[GOZAR CASAS CLAUDIA ARACELI]' AS abogado_nombre, 'ARIADNA MACHUCA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[GUARDAPUCLLA QUISPE JAIME]' AS abogado_nombre, 'ARIADNA MACHUCA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[HANCCO NAVARRO PERCY LEON]' AS abogado_nombre, 'JULIO SANTIAGO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ICOCHEA MARTEL LUIS JAIME]' AS abogado_nombre, 'LIUBEN CELI' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ILAVE VELASCO CAROLINE BERTA]' AS abogado_nombre, 'ARIADNA MACHUCA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[JARA CARO PAUL WILLIAM]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[JIMENEZ CONCHA DE SILVA MAGALY]' AS abogado_nombre, 'GLENYS BRUNO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[JIMENEZ MORVELI YENI]' AS abogado_nombre, 'FLOR ZAPANA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[LA TORRE CASTILLO CARLOS ALCIBIADES CELSO]' AS abogado_nombre, 'JUANA PINTO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[LEON SANCHEZ DYAN YOMARA]' AS abogado_nombre, 'JUANA PINTO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[LINO QUECAÑO SUSSAN MILAGROS]' AS abogado_nombre, 'FLOR ZAPANA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[MACHUCA VARGAS ARIADNA CLAUDIA]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[MAQUE HANCCO MARIA]' AS abogado_nombre, 'JULIO SANTIAGO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[MARTINEZ EYZAGUIRRE EVELYN YAHAYRA]' AS abogado_nombre, 'LIUBEN CELI' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[MATOS TORRES LESLY ANDREA]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[MENDOZA MOLINA ROSARIO]' AS abogado_nombre, 'GLENYS BRUNO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[MILIAN TORRES CATHERINE MARISELA]' AS abogado_nombre, 'JUANA PINTO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[MORALES HIDALGO KARLA SUSANA]' AS abogado_nombre, 'FLOR ZAPANA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[MORALES VASQUEZ JUAN FRANCO]' AS abogado_nombre, 'FLOR ZAPANA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[MOROCHO MARCHAN SILVIA JANET]' AS abogado_nombre, 'LIUBEN CELI' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[MUÑANTE LUNA SANDRA VERONICA]' AS abogado_nombre, 'JULIO SANTIAGO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[PACHAS ALMEYDA MARIA VIVIANA]' AS abogado_nombre, 'GLENYS BRUNO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[PANTOJA LAZARO ROGGER OMAR]' AS abogado_nombre, 'LISSET CALIXTRO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[PAREDES GUTIERREZ MILIPSA OLGA]' AS abogado_nombre, 'GLENYS BRUNO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[PATIÑO VARGAS ERIKA SHEILA]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[PINTO WEHRLE JUANA CARLOTA]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[POMALIA ALVARO FIORELLA JULIA]' AS abogado_nombre, 'ARIADNA MACHUCA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[PUCH PARDO FIGUEROA DAVID EDUARDO]' AS abogado_nombre, 'LIUBEN CELI' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[QUEREVALÚ PÉREZ JESSICA GLADYS]' AS abogado_nombre, 'GLENYS BRUNO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[QUINTANA AVILA INES ANA]' AS abogado_nombre, 'LISSET CALIXTRO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[QUISPE ALFARO NILDA]' AS abogado_nombre, 'GLENYS BRUNO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[RAMIREZ ZAPATA RUTH DEL SOCORRO]' AS abogado_nombre, 'JUANA PINTO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[RAMOS CAMPOS CARMEN LUISA]' AS abogado_nombre, 'ARIADNA MACHUCA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[REATEGUI BRANDYCH MEGGIE]' AS abogado_nombre, 'JULIO SANTIAGO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[REATEGUI KOCLENG MAIKOL STIFF]' AS abogado_nombre, 'GLENYS BRUNO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[RIOS IBAÑEZ IVAN]' AS abogado_nombre, 'JUANA PINTO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[RIVAS CIRIACO FIORELLA FATIMA]' AS abogado_nombre, 'LISSET CALIXTRO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[RIVERA ARAUCO YANET PAOLA]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[RODAS VALLE GABRIELA ANATOLIA]' AS abogado_nombre, 'FLOR ZAPANA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ROJAS PORRAS HUGO ENRIQUE]' AS abogado_nombre, 'LIUBEN CELI' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ROMANI CAHUANA LUIS ENRIQUE]' AS abogado_nombre, 'LIUBEN CELI' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ROMERO OROZCO REBECA ANGELICA]' AS abogado_nombre, 'FLOR ZAPANA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ROMERO TORRES MIGUEL ANGEL]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[SALAZAR ZUMAETA SANDRA JESSICA]' AS abogado_nombre, 'FLOR ZAPANA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[SANCHEZ MARTINEZ LUIS FELIPE]' AS abogado_nombre, 'LISSET CALIXTRO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[SANTIAGO RODRIGUEZ JULIO CESAR]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[TANG MARTINEZ ROBERTO MARCOS]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[TAPIA GONZALES ROSA YANET]' AS abogado_nombre, 'JUANA PINTO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[TELLO ROJAS DANIEL EDUARDO]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[TUESTA VIENA LLESY LEYDIT]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[TUMIALAN FABIAN NANCY]' AS abogado_nombre, 'FLOR ZAPANA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[TUPACYUPANQUI GARAGATTI NATALI INES ALEJANDRA]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[TUPAYACHI ROMERO ENZO]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[VARGAS POMEZ CARLOS ENRIQUE]' AS abogado_nombre, 'JUANA PINTO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[VASQUEZ DE LA CRUZ GIANNINA MAGDA]' AS abogado_nombre, 'SHIRLEY DIOSES' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[VASQUEZ GUTIERREZ ANDRES]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[VASQUEZ MORAN FELICIO SAMUEL]' AS abogado_nombre, 'FLOR ZAPANA' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[VEREAU FLORES NANCY SIDNEY]' AS abogado_nombre, 'JUANA PINTO' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ZAPANA CARO DE BARRETO FLOR ESTELA]' AS abogado_nombre, 'SUBDIRECTOR' AS supervisor_corto FROM dual
    UNION ALL
    SELECT Q'[ZEGARRA RAMOS ABEL AUGUSTO]' AS abogado_nombre, 'LIUBEN CELI' AS supervisor_corto FROM dual
      ) pares
    ) resuelto
    JOIN usuario ab
      ON TRANSLATE(UPPER(TRIM(REGEXP_REPLACE(ab.nombre_completo, '\s+', ' '))), 'ÁÉÍÓÚÑÜ', 'AEIOUNU')
       = TRANSLATE(UPPER(TRIM(REGEXP_REPLACE(resuelto.abogado_nombre, '\s+', ' '))), 'ÁÉÍÓÚÑÜ', 'AEIOUNU')
    JOIN usuario sup
      ON UPPER(sup.username) = UPPER(resuelto.supervisor_username)
) src
ON (dst.id_supervisor = src.id_supervisor AND dst.id_abogado = src.id_abogado)
WHEN MATCHED THEN UPDATE
  SET dst.activo = 1,
      dst.modificado_en = SYSTIMESTAMP
WHEN NOT MATCHED THEN
  INSERT (id_supervisor, id_abogado, activo, creado_en)
  VALUES (src.id_supervisor, src.id_abogado, 1, SYSTIMESTAMP);

COMMIT;

/* ============================================================
   Verificacion posterior
   ============================================================ */

SELECT sup.username AS supervisor, sup.nombre_completo AS supervisor_nombre,
       COUNT(*) AS abogados_supervisados
  FROM usuario_supervision us
  JOIN usuario sup ON sup.id_usuario = us.id_supervisor
 WHERE us.activo = 1
 GROUP BY sup.username, sup.nombre_completo
 ORDER BY abogados_supervisados DESC;

SELECT ab.username AS abogado, ab.nombre_completo AS abogado_nombre,
       sup.username AS supervisor, sup.nombre_completo AS supervisor_nombre
  FROM usuario_supervision us
  JOIN usuario ab ON ab.id_usuario = us.id_abogado
  JOIN usuario sup ON sup.id_usuario = us.id_supervisor
 WHERE us.activo = 1
 ORDER BY sup.username, ab.username;

-- Un abogado no deberia tener mas de un supervisor activo a la vez
SELECT ab.username, COUNT(*) AS supervisores_activos
  FROM usuario_supervision us
  JOIN usuario ab ON ab.id_usuario = us.id_abogado
 WHERE us.activo = 1
 GROUP BY ab.username
HAVING COUNT(*) > 1;

SELECT COUNT(*) AS total_relaciones_supervision FROM usuario_supervision WHERE activo = 1;
