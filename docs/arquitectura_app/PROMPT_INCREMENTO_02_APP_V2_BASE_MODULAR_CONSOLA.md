Continuamos con la nueva App SDRERC V2 dentro del mismo proyecto Maven.

Antes de modificar archivos, lee obligatoriamente estos documentos:

1.  docs/arquitectura_app/PROMPT_REESTRUCTURACION_APP_SDRERC.md

2.  docs/arquitectura_app/INFORME_REESTRUCTURACION_APP_SDRERC.md

Trátalos como marco rector aprobado de la reestructuración. Además,
considera como referencia visual la intención de diseño moderno que se
viene trabajando: menú lateral limpio, header superior, cards, tablas
modernas, badges de estado, flujo por etapas, experiencia amigable para
usuarios finales y una interfaz más ordenada que la app legacy.

Decisión arquitectónica actual:\
No vamos a modificar pantalla por pantalla la app legacy actual. Vamos a
construir una nueva App SDRERC V2 dentro del mismo proyecto Maven,
modular, ordenada y progresiva, usando la app antigua solo como
referencia funcional, técnica y visual.

Contexto actual validado:

- La App SDRERC V2 ya existe como aplicación paralela dentro del mismo
  proyecto.

- La app legacy debe seguir intacta.

- Ya existen:

  - src/main/java/com/sdrerc/appv2/MainV2.java

  - src/main/java/com/sdrerc/ui/appv2/MenuPrincipalV2.java

  - src/main/java/com/sdrerc/ui/appv2/HomeV2.java

  - run-v2.ps1

- La Bandeja Expedientes V2 ya se abre desde MenuPrincipalV2.

- SdrercAppConnection ya apunta correctamente a:\
  jdbc:oracle:thin:@localhost:1521/XEPDB1

- La conexión entra como usuario SDRERC_APP.

- VW_EXPEDIENTE_BANDEJA devuelve data.

- Al presionar Buscar en Bandeja Expedientes V2, la tabla muestra
  correctamente expedientes.

- El sistema ya compila con Maven.

- La app legacy sigue funcionando y no debe ser reemplazada todavía.

Objetivo general de la V2:\
Construir una nueva aplicación de gestión de expedientes por flujo, no
una copia exacta de formularios legacy.

Los módulos funcionales antiguos NO deben desaparecer. Deben conservarse
en la App V2 como módulos/etapas organizadas:

- Registro / Recepción

- Asignación

- Análisis

- Verificación

- Firma / Emisión

- Ejecución

- Notificación

- Publicación

- Cierre / Archivo

- Administración

Pero no se deben clonar todavía pantalla por pantalla. La V2 debe
organizarlos como:

- bandejas filtradas por etapa;

- consola central del expediente;

- timeline/historial;

- acciones permitidas según flujo;

- módulos visuales limpios;

- componentes reutilizables.

Concepto funcional esperado:\
Bandeja General V2\
-\> seleccionar expediente\
-\> Ver Detalle / Consola Expediente V2\
-\> Datos generales\
-\> Etapa actual\
-\> Estado actual\
-\> Responsable actual\
-\> Historial / timeline\
-\> Acciones permitidas visibles\
-\> Sin ejecutar acciones todavía

Cada módulo por etapa debe ser en el futuro una vista filtrada:

- Módulo Registro / Recepción = expedientes en etapa REGISTRO o
  recepción equivalente.

- Módulo Asignación = expedientes en etapa ASIGNACION.

- Módulo Análisis = expedientes en etapa ANALISIS.

- Módulo Verificación = expedientes en etapa VERIFICACION.

- Módulo Firma / Emisión = expedientes en etapa FIRMA_EMISION.

- Módulo Ejecución = expedientes en etapa EJECUCION.

- Módulo Notificación = expedientes en etapa NOTIFICACION.

- Módulo Publicación = expedientes en etapa PUBLICACION_CONDICIONAL.

- Módulo Cierre / Archivo = expedientes en etapa CIERRE_ARCHIVO.

Reglas obligatorias:

- No modificar OracleConnection.java.

- No modificar la conexión legacy.

- No modificar FrmLogin.java legacy.

- No modificar MenuPrincipal.java legacy.

- No eliminar código legacy.

- No ejecutar SQL.

- No modificar datos en BD.

- No implementar acciones de escritura todavía.

- No implementar movimientos de flujo todavía.

- No usar INSERT, UPDATE, DELETE, DROP, TRUNCATE ni MERGE en la App V2.

- No usar IDs hardcodeados.

- No imprimir ni documentar passwords reales.

- Si encuentras passwords reales en documentación, reemplázalos por
  \[REDACTADO\].

- Mantener Java 8 y Swing.

- Usar SdrercAppConnection para la App V2.

- Toda consulta debe pasar por DAO/Service, no SQL directo en Swing.

- Mantener la App V2 separada de la legacy.

- No agregar librerías nuevas salvo que sea estrictamente necesario y
  previamente justificado.

- No cambiar pom.xml salvo que sea estrictamente necesario y previamente
  justificado.

Organización modular esperada:\
Usar o preparar esta estructura:

- com.sdrerc.appv2\
  Punto de entrada de la App V2.

- com.sdrerc.ui.appv2\
  Shell principal, Home, layout base.

- com.sdrerc.ui.appv2.theme\
  Constantes visuales de la App V2: colores, fuentes, bordes,
  espaciados.

- com.sdrerc.ui.appv2.components\
  Componentes reutilizables de UI V2: cards, badges, botones, paneles,
  headers, secciones, utilidades visuales.

- com.sdrerc.ui.appv2.modules\
  Organización visual futura por módulos/etapas.

- com.sdrerc.ui.views.expedienteconsola\
  Bandeja, consola, timeline y detalle de expediente.

- com.sdrerc.application.sdrercapp\
  Servicios de aplicación de la nueva base SDRERC_APP.

- com.sdrerc.infrastructure.sdrercapp.dao\
  DAOs de la nueva base SDRERC_APP.

- com.sdrerc.domain.dto.sdrercapp\
  DTOs para la nueva base SDRERC_APP.

Objetivo de este incremento:\
Crear una base visual y modular para SDRERC V2, conservar
conceptualmente los módulos legacy como etapas, mejorar el Home V2 con
diseño por flujo, y crear la primera Consola Expediente V2 de solo
lectura.

Tareas de este incremento:

1.  Revisar documentos base\
    Leer:

- docs/arquitectura_app/PROMPT_REESTRUCTURACION_APP_SDRERC.md

- docs/arquitectura_app/INFORME_REESTRUCTURACION_APP_SDRERC.md

Antes de modificar, confirmar internamente que la solución propuesta no
contradice la reestructuración aprobada.

2.  Crear base visual reutilizable para la App V2

Crear, si corresponde:

src/main/java/com/sdrerc/ui/appv2/theme/AppV2Theme.java

Debe centralizar:

- colores principales;

- colores de fondo;

- colores de borde;

- colores de texto;

- colores de éxito, alerta, error e información;

- fuente base;

- tamaños de fuente;

- espaciados básicos;

- métodos utilitarios simples para bordes o fuentes, si aporta orden.

Crear, si corresponde:

src/main/java/com/sdrerc/ui/appv2/components/CardPanelV2.java\
src/main/java/com/sdrerc/ui/appv2/components/BadgeV2.java\
src/main/java/com/sdrerc/ui/appv2/components/SectionPanelV2.java

Deben ser simples, compatibles con Java 8 y Swing, y no deben depender
de librerías nuevas.

3.  Mejorar HomeV2 sin consultar BD

Modificar:\
src/main/java/com/sdrerc/ui/appv2/HomeV2.java

HomeV2 debe mostrar visualmente:

- título "SDRERC V2";

- subtítulo "Sistema de Rectificación de Actas - Nueva arquitectura
  SDRERC_APP";

- tarjetas de acceso rápido;

- bloque "Flujo operativo" con etapas:\
  REGISTRO -\> ASIGNACION -\> ANALISIS -\> VERIFICACION -\>
  FIRMA_EMISION -\> EJECUCION -\> NOTIFICACION -\>
  PUBLICACION_CONDICIONAL -\> CIERRE_ARCHIVO

- módulos principales visibles:\
  Registro / Recepción\
  Asignación\
  Análisis\
  Verificación\
  Firma / Emisión\
  Ejecución\
  Notificación\
  Publicación\
  Cierre / Archivo\
  Administración

- diseño limpio, con cards y badges;

- sin consultar BD todavía.

Corregir cualquier texto con encoding incorrecto como "RectificaciÃ³n".
Usar UTF-8 correctamente o Unicode escape si es necesario.

4.  Mejorar MenuPrincipalV2 de forma modular

Modificar solo:\
src/main/java/com/sdrerc/ui/appv2/MenuPrincipalV2.java

Objetivo:

- Mantener Inicio.

- Mantener Bandeja Expedientes V2.

- Agregar organización visual de módulos/etapas sin implementar aún
  todas las pantallas.

- Mostrar secciones de menú ordenadas, por ejemplo:\
  Inicio\
  Expedientes

  - Bandeja General V2

  - Registro / Recepción

  - Asignación

  - Análisis

  - Verificación

  - Firma / Emisión

  - Ejecución

  - Notificación

  - Publicación / Cierre\
    Administración

  - Usuarios

  - Equipo jurídico

  - Roles\
    Salir

Para los módulos todavía no implementados, mostrar un mensaje amigable:\
"Módulo pendiente de implementación en SDRERC V2".

No modificar MenuPrincipal.java legacy.

5.  Mejorar Bandeja Expedientes V2 sin cambiar la lógica de consulta

Modificar:\
src/main/java/com/sdrerc/ui/views/expedienteconsola/JPanelBandejaExpedientesNueva.java

Objetivos:

- Mantener consulta a VW_EXPEDIENTE_BANDEJA.

- Mantener botón Buscar.

- Mantener botón Limpiar.

- Habilitar botón Ver detalle cuando se seleccione una fila.

- Obtener idExpediente de la fila seleccionada.

- Abrir la nueva Consola Expediente V2.

- Mejorar visualmente la tabla, estados y columnas si es seguro.

- No implementar acciones de flujo.

- No escribir en BD.

- Mantener logs seguros, sin imprimir URL, usuario ni password.

6.  Crear Consola Expediente V2 de solo lectura

Primero revisar columnas reales en:

db/sdrerc_app/scripts/10_vistas_bandejas_consola.sql

Identificar columnas reales de:

- VW_EXPEDIENTE_CONSOLA

- VW_EXPEDIENTE_TIMELINE

- VW_EXPEDIENTE_ACCIONES_PERMITIDAS

No inventar nombres de columnas. Adaptarse a lo que existe en los
scripts.

Crear DTOs en:\
src/main/java/com/sdrerc/domain/dto/sdrercapp/

Sugeridos:

- ExpedienteConsolaDTO.java

- ExpedienteTimelineDTO.java

- AccionPermitidaDTO.java

Usar tipos seguros:

- Long/Integer para IDs y números.

- String para códigos, nombres, descripciones.

- LocalDate / LocalDateTime para fechas.

- Boolean para flags.\
  Aplicar null safety.

Crear DAOs de solo lectura en:\
src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/

Sugeridos:

- ExpedienteConsolaDAO.java

- ExpedienteTimelineDAO.java

- AccionesPermitidasDAO.java

Reglas DAO:

- Usar SdrercAppConnection.

- Usar PreparedStatement.

- Filtrar por id_expediente o campo equivalente según la vista.

- No concatenar IDs.

- No hacer escrituras.

- No usar SQL en Swing.

Crear servicio en:\
src/main/java/com/sdrerc/application/sdrercapp/ExpedienteDetalleService.java

Debe exponer métodos simples:

- obtenerConsolaPorExpediente(Long idExpediente)

- listarTimeline(Long idExpediente)

- listarAccionesPermitidas(Long idExpediente)

Si los nombres exactos deben variar según columnas reales, ajustar
manteniendo la intención.

Crear dialog/pantalla:\
src/main/java/com/sdrerc/ui/views/expedienteconsola/DlgConsolaExpedienteV2.java

Debe ser JDialog de solo lectura, con diseño moderno básico:

- encabezado con número de expediente;

- etapa actual;

- estado actual;

- responsable actual;

- fechas principales;

- bloque de datos generales;

- tabla o panel de timeline/historial;

- panel de acciones permitidas visibles como badges o botones
  deshabilitados;

- botón Cerrar.

Importante:

- No ejecutar acciones.

- No actualizar estados.

- No registrar historial.

- No modificar datos.

7.  Integrar Consola desde Bandeja V2

En JPanelBandejaExpedientesNueva:

- al seleccionar una fila, habilitar Ver detalle;

- al hacer clic en Ver detalle, abrir DlgConsolaExpedienteV2 con el
  idExpediente seleccionado;

- si no hay selección, mostrar mensaje amigable;

- mantener Buscar y Limpiar funcionando.

8.  No implementar ejecución de acciones todavía

Las acciones permitidas deben mostrarse solo como referencia
visual/informativa.

No crear todavía:

- FlujoMovimientoService con escritura.

- Registro de historial.

- Actualización de estado.

- Botones activos que cambien datos.

9.  Compilar

Ejecutar:\
mvn clean compile

10. Si compila correctamente, empaquetar

Ejecutar:\
mvn clean package

11. No ejecutar SQL

No ejecutar scripts SQL ni consultas SQL contra la base. Solo leer
archivos .sql del proyecto para conocer columnas de vistas.

Entregable obligatorio:

- Confirmación de que leíste PROMPT_REESTRUCTURACION_APP_SDRERC.md.

- Confirmación de que leíste INFORME_REESTRUCTURACION_APP_SDRERC.md.

- Archivos creados.

- Archivos modificados.

- Explicación de la estructura modular propuesta.

- Explicación de cómo se conservan los módulos legacy como
  etapas/módulos V2.

- Columnas detectadas en:

  - VW_EXPEDIENTE_CONSOLA

  - VW_EXPEDIENTE_TIMELINE

  - VW_EXPEDIENTE_ACCIONES_PERMITIDAS

- Explicación de cómo probar:

  1.  .\\run-v2.ps1

  2.  abrir Bandeja Expedientes V2

  3.  presionar Buscar

  4.  seleccionar expediente

  5.  presionar Ver detalle

<!-- -->

- Confirmación de que OracleConnection.java no fue modificado.

- Confirmación de que FrmLogin.java legacy no fue modificado.

- Confirmación de que MenuPrincipal.java legacy no fue modificado.

- Confirmación de que no se ejecutó SQL.

- Confirmación de que no se implementaron INSERT, UPDATE, DELETE, DROP,
  TRUNCATE ni MERGE.

- Resultado de mvn clean compile.

- Resultado de mvn clean package, si aplica.
