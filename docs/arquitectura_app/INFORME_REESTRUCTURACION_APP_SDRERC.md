# Informe técnico de reestructuración progresiva de la app SDRERC

## A. Resumen del proyecto actual

El proyecto `sdrerc_CODIGOS` es una aplicación Java Swing para gestión de expedientes SDRERC. Usa Maven, Oracle JDBC, FlatLaf y componentes Swing propios. La estructura actual ya tiene una separación parcial por paquetes:

- `com.sdrerc.application`: servicios de aplicación existentes.
- `com.sdrerc.domain.model`: modelos de dominio y respuestas.
- `com.sdrerc.infrastructure.database`: conexión Oracle global.
- `com.sdrerc.infrastructure.repository`: repositorios con SQL JDBC.
- `com.sdrerc.infrastructure.security`: utilidades de seguridad.
- `com.sdrerc.shared`: constantes y sesión.
- `com.sdrerc.ui`: formularios, vistas Swing, menú, componentes y renderers.
- `com.sdrerc.util`: utilidades de UI y validación.

La aplicación actual sigue conectando contra el esquema legacy usando usuario `system`. La nueva base `SDRERC_APP` ya fue preparada con vistas de lectura y flujo `SDRERC_TO_BE`, pero todavía no está integrada en Java.

El enfoque recomendado es migración paralela: mantener la conexión legacy y agregar una segunda conexión específica para `SDRERC_APP`. La primera migración debe ser de lectura, con una bandeja nueva y una consola visual de expediente basadas en vistas.

## B. Clases actuales de conexión Oracle

Clase identificada:

- `src/main/java/com/sdrerc/infrastructure/database/OracleConnection.java`

Configuración actual:

```text
URL: jdbc:oracle:thin:@localhost:1521/xe
USER: system
PASSWORD: satrapa12345
DRIVER: oracle.jdbc.OracleDriver
```

La clase expone:

```java
public static Connection getConnection() throws SQLException
```

Observaciones:

- La conexión está hardcodeada.
- No hay configuración externa por properties/env.
- Todas las capas usan la misma conexión global.
- La conexión apunta a `system`, por lo que cambiarla directamente a `SDRERC_APP` rompería consultas legacy.
- Se debe crear una conexión paralela, por ejemplo `SdrercAppConnection`, sin tocar `OracleConnection` inicialmente.

## C. Formularios actuales que deben ser migrados

Pantallas principales identificadas desde `MenuPrincipal` y paquetes `ui.views`:

- Recepción:
  - `JPanelListadoRegistroExpediente`
  - `JPanelRegistrarExpediente`
- Asignación:
  - `JPanelFiltroBusqueda`
  - `JPanelRegistroAsignacion`
  - `JPanelRegistroAsignacionOlds`
  - `FrmAsignar`
- Expedientes asignados:
  - `JPanelListadoExpedientesAsignados`
  - `JPanelRegistrarExpedientePorRecibido`
  - `JPanelRegistrarExpedientePorRecibidoOlds`
- Expedientes por trabajar / análisis:
  - `JPanelListadoExpedientesPorTrabajar`
  - `JPanelRegistrarExpedientePorTrabajar`
- Expedientes por verificar:
  - `JPanelListadoExpedientesPorVerificar`
  - `JPanelRegistrarExpedientePorVerificar`
- Ejecución:
  - `JPanelListadoExpedientesEjecucionAsignar`
  - `JPanelRegistrarExpedientesEjecucionAsignar`
  - `JPanelListadoExpedientesEjecucionPorTrabajar`
  - `JPanelRegistrarExpedientesEjecucionPorTrabajar`
- Notificación:
  - `JPanelListadoExpedientesNotificacionAsignar`
  - `JPanelRegistrarExpedientesNotificacionAsignar`
  - `JPanelListadoExpedientesNotificacionPorTrabajar`
  - `JPanelRegistrarExpedientesNotificacionPorTrabajar`
- Administración:
  - `JPanelListadoUsuario`
  - `JPanelListadoRole`
  - `JPanelEquipoJuridico`
  - diálogos de edición/asignación/reset.

Prioridad de migración visual:

1. Crear bandeja unificada nueva sobre `VW_EXPEDIENTE_BANDEJA`.
2. Crear consola visual sobre `VW_EXPEDIENTE_CONSOLA` y vistas relacionadas.
3. Reemplazar progresivamente bandejas específicas por filtros de la bandeja unificada.

## D. DAOs existentes

El proyecto usa el término `Repository`, no `DAO`. Repositorios existentes:

- `CatalogoDetalleRepository`
- `CatalogoRepository`
- `DepartamentoRepository`
- `DistritoRepository`
- `EquipoJuridicoRepository`
- `ExpedienteAnalisisAbogadoRepository`
- `ExpedienteAsignacionRepository`
- `ExpedienteEjecucionAsignacionRepository`
- `ExpedienteEjecucionPorTrabajarRepository`
- `ExpedienteNotificacionAsignacionRepository`
- `ExpedienteObservacionEjecucionRepository`
- `ExpedienteObservacionVerificacionRepository`
- `ExpedientePorNotificarRepository`
- `ExpedienteRepository`
- `PlazoAtencionRepository`
- `ProvinciaRepository`
- `RoleRepository`
- `SupervisionRepository`
- `TecnicoRepository`
- `UserRepository`

Todos los repositorios revisados usan `OracleConnection.getConnection()`. Por tanto, hoy están acoplados al esquema legacy.

## E. Modelos existentes

Modelos principales identificados:

- Expediente:
  - `Expediente`
  - `ExpedienteFilter`
  - `ExpedienteResponse`
  - `ExpedienteAsignacion`
- Análisis:
  - `ExpedienteAnalisisAbogado`
  - `ExpedienteAnalisisAbogadoResponse`
  - `ExpedienteAnalisisAbogadoDetDoc`
  - `ExpedienteAnalisisAbogadoDetResponse`
- Observaciones:
  - `ExpedienteObservacionEjecucion`
  - `ExpedienteObservacionVerificacion`
- Seguridad/equipos:
  - `User`
  - `Role`
  - `Supervision`
  - `Tecnico`
  - `EquipoJuridico*`
- Catálogos:
  - `Catalogo`
  - `CatalogoItem`
  - `ComboItem`
  - `Departamento`
  - `Provincia`
  - `Distrito`
  - `Enumerado`
- Infraestructura de listas:
  - `PaginatedResult`
  - `UsuarioListadoItem`
  - `PlazoAtencionConfig`
  - `PlazoAtencionResultado`

Faltan DTOs orientados a las vistas nuevas de `SDRERC_APP`, por ejemplo `ExpedienteBandejaDTO`, `ExpedienteConsolaDTO`, `ExpedienteTimelineDTO` y `ExpedienteAccionPermitidaDTO`.

## F. SQL embebido encontrado

No se encontró SQL directo dentro de formularios Swing al buscar en `com.sdrerc.ui`. Las pantallas consumen servicios.

Sí hay SQL JDBC embebido en repositorios y en algunos servicios. Archivos con mayor presencia de SQL:

- `UserRepository.java`
- `ExpedienteAsignacionRepository.java`
- `ExpedienteRepository.java`
- `EquipoJuridicoService.java`
- `EquipoJuridicoRepository.java`
- `ExpedienteAnalisisAbogadoRepository.java`
- `SupervisionRepository.java`
- `RoleRepository.java`
- repositorios de ejecución, notificación, observación y catálogos.

Tablas legacy observadas:

- `EXPEDIENTE`
- `EXPEDIENTE_ASIGNACION`
- `EXPEDIENTE_ANALISIS_ABOGADO`
- `EXPEDIENTE_ANALISIS_ABOGADO_DET_DOC`
- `CATALOGO`
- `CATALOGO_ITEM`
- `TECNICO`
- `APP_USERS`
- `APP_ROLES`
- `APP_USER_ROLES`
- `APP_USER_SUPERVISION`
- `DEPARTAMENTO`
- `PROVINCIA`
- `DISTRITO`

Riesgos del SQL actual:

- Consultas y reglas mezcladas en repositorios grandes.
- Algunos servicios, como `EquipoJuridicoService`, ejecutan SQL directamente.
- Existen operaciones `DELETE` en repositorios legacy para reemplazar detalles/roles/supervisión.
- El flujo usa IDs numéricos legacy, no códigos de catálogo.
- No hay abstracción de fuente de datos para elegir legacy vs. `SDRERC_APP`.

## G. Estados legacy hardcodeados

Se identificaron constantes legacy en:

- `src/main/java/com/sdrerc/shared/constants/FlujoExpedienteConstants.java`
- `src/main/java/com/sdrerc/domain/model/Enumerado.java`

IDs relevantes:

```text
56 REGISTRO_EXPEDIENTE
57 EXPEDIENTE_ASIGNADO
58 EXPEDIENTE_RECIBIDO
59 EXPEDIENTE_ATENDIDO
87 EXPEDIENTE_VERIFICADO
88 EJECUCION_ASIGNADA
89 EJECUCION_TRABAJADA
90 NOTIFICACION_ASIGNADA
91 NOTIFICACION_TRABAJADA
73 PROCEDENTE
74 IMPROCEDENTE
60,61,62,63,64,71,72 documentos analizados
```

Estos IDs deben mantenerse para legacy, pero no deben propagarse a la nueva arquitectura `SDRERC_APP`. Para la nueva capa se deben usar códigos:

```text
REGISTRADO
ASIGNADO
RECIBIDO_POR_ABOGADO
ATENDIDO
EN_VERIFICACION
VERIFICADO
PARA_FIRMA
FIRMADO
EN_EJECUCION
EN_NOTIFICACION
PENDIENTE_PUBLICACION
CERRADO
```

Regla crítica: no crear ni mostrar etapa visual `VALIDACION`.

## H. Pantallas prioritarias para rediseño

Prioridad 1:

- `JPanelListadoRegistroExpediente`
- `JPanelFiltroBusqueda`
- `JPanelListadoExpedientesAsignados`
- `JPanelListadoExpedientesPorTrabajar`
- `JPanelListadoExpedientesPorVerificar`

Motivo: cubren recepción, asignación, análisis y verificación, que son el núcleo del flujo y donde debe aparecer la nueva lectura por bandejas.

Prioridad 2:

- `JPanelListadoExpedientesEjecucionAsignar`
- `JPanelListadoExpedientesEjecucionPorTrabajar`
- `JPanelListadoExpedientesNotificacionAsignar`
- `JPanelListadoExpedientesNotificacionPorTrabajar`

Motivo: deben alinearse con reversión desde ejecución, notificación, cargos y publicación.

Prioridad 3:

- Pantallas de administración de usuarios, roles y equipo jurídico.

Motivo: son importantes pero no bloquean la validación del flujo de expedientes.

## I. Propuesta de nueva arquitectura por capas

Agregar una rama paralela para `SDRERC_APP`, sin reemplazar legacy:

```text
com.sdrerc.infrastructure.database
  OracleConnection                 legacy, se mantiene
  SdrercAppConnection              nueva conexión a SDRERC_APP

com.sdrerc.infrastructure.sdrercapp.dao
  DAOs de lectura sobre vistas SDRERC_APP

com.sdrerc.application.sdrercapp
  Servicios de consulta y flujo

com.sdrerc.domain.dto.sdrercapp
  DTOs de bandeja/consola/timeline/documentos/acciones

com.sdrerc.ui.views.expedienteconsola
  Bandeja y consola visual nuevas

com.sdrerc.ui.common.sdrerc
  Componentes visuales reutilizables del rediseño
```

Principios:

- Lectura primero, acciones después.
- `SDRERC_APP` se consulta mediante vistas.
- Los formularios no deben contener SQL.
- Las acciones permitidas vienen de `VW_EXPEDIENTE_ACCIONES_PERMITIDAS`.
- Las reglas de transición se centralizan en servicios.
- Los movimientos futuros deben registrar `EXPEDIENTE_HISTORIAL`.

## J. Propuesta de clases nuevas

Conexión:

- `SdrercAppConnection`
- `DatabaseConnectionProvider` opcional, si se quiere abstraer legacy/app.

DTOs:

- `ExpedienteBandejaDTO`
- `ExpedienteConsolaDTO`
- `ExpedienteTimelineDTO`
- `ExpedienteDocumentoDTO`
- `ExpedienteDocumentoAnalizadoDTO`
- `ExpedienteAccionPermitidaDTO`
- `ExpedienteResolucionDTO`
- `ExpedienteNotificacionDTO`
- `ExpedienteCargoAcuseDTO`
- `ExpedientePublicacionDTO`
- `ExpedienteDigitalDTO`
- `ExpedienteFiltroDTO`

DAOs:

- `ExpedienteBandejaDAO`
- `ExpedienteConsolaDAO`
- `ExpedienteTimelineDAO`
- `ExpedienteDocumentoDAO`
- `ExpedienteEvaluacionDAO`
- `ExpedienteResolucionDAO`
- `ExpedienteNotificacionDAO`
- `ExpedienteCargoAcuseDAO`
- `ExpedientePublicacionDAO`
- `ExpedienteDigitalDAO`
- `FlujoAccionDAO`

Servicios:

- `ExpedienteConsultaService`
- `FlujoExpedienteService`
- `ExpedienteHistorialService`
- `ExpedienteDocumentoService`
- `ExpedienteNotificacionService`
- `ExpedienteDigitalService`

Vistas Swing piloto:

- `JPanelBandejaExpedientesNueva`
- `BandejaExpedientesNuevaView` si se prefiere JFrame/dialog wrapper.
- `JPanelExpedienteConsola`
- `ExpedienteConsolaView`

## K. Propuesta de componentes visuales Swing

Reutilizar y extender componentes actuales:

- `Table`
- `TableHeader`
- `TablePaginationHelper`
- `SearchText`
- `PanelBorder`
- `PlazoAtencionCellRenderer`
- `ModernScrollBarUI`
- `MouseWheelScrollHelper`
- `ButtonRenderer`
- `ButtonEditor`

Crear componentes nuevos:

- `EstadoBadgeRenderer`
- `EtapaBadgeRenderer`
- `DiasRestantesRenderer`
- `StageProgressPanel`
- `ExpedienteHeaderPanel`
- `ExpedienteSideInfoPanel`
- `ExpedienteTimelinePanel`
- `ActionButtonPanel`
- `CardPanel`
- `SearchFilterPanel`
- `BadgeLabel`
- `EmptyStatePanel`
- `LoadingPanel`

La estética debe apoyarse en `FlatLightLaf` ya configurado en `AppUiConfig`, con paleta institucional sobria y sin saturar la interfaz.

## L. Propuesta de rediseño visual de bandeja

Pantalla piloto:

```text
JPanelBandejaExpedientesNueva
```

Fuente de datos:

```text
VW_EXPEDIENTE_BANDEJA
```

Estructura:

- Franja superior con título, total de expedientes y última actualización.
- Tarjeta de filtros:
  - búsqueda por expediente/trámite/titular;
  - etapa;
  - estado;
  - responsable;
  - rango de fecha;
  - botones Buscar, Limpiar, Exportar, Ver detalle.
- Tabla principal:
  - número de expediente;
  - número de trámite;
  - etapa;
  - estado;
  - abogado inicial;
  - responsable actual;
  - equipo;
  - fecha registro;
  - último movimiento;
  - días restantes;
  - indicadores de publicación y expediente digital.
- Renderers:
  - badge por estado;
  - badge por etapa;
  - semáforo de plazo;
  - iconos discretos para publicación/digital.

Comportamiento:

- Doble clic o botón `Ver detalle` abre consola del expediente.
- Filtros deben operar sobre DAO/servicio, no sobre SQL en UI.
- Se puede iniciar mostrando datos de todas las etapas y luego filtrar por etapa.

## M. Propuesta de rediseño visual de consola del expediente

Pantalla piloto:

```text
JPanelExpedienteConsola
```

Fuentes de datos:

```text
VW_EXPEDIENTE_CONSOLA
VW_EXPEDIENTE_TIMELINE
VW_EXPEDIENTE_DOCUMENTOS
VW_EXPEDIENTE_DOCUMENTOS_ANALIZADOS
VW_EXPEDIENTE_EVALUACIONES
VW_EXPEDIENTE_RESOLUCIONES
VW_EXPEDIENTE_NOTIFICACIONES
VW_EXPEDIENTE_CARGOS_ACUSE
VW_EXPEDIENTE_PUBLICACION
VW_EXPEDIENTE_DIGITAL
VW_EXPEDIENTE_ACCIONES_PERMITIDAS
```

Diseño:

- Encabezado:
  - expediente;
  - trámite;
  - etapa/estado;
  - responsable;
  - fechas;
  - alertas.
- Barra visual de etapas:
  - Registro;
  - Asignación;
  - Análisis;
  - Verificación;
  - Firma/Emisión;
  - Ejecución;
  - Notificación;
  - Publicación;
  - Expediente digital;
  - Cierre/Archivo.
- Centro con pestañas:
  - Detalles;
  - Solicitudes;
  - Actas;
  - Personas;
  - Documentos;
  - Evaluaciones;
  - Resoluciones;
  - Notificaciones;
  - Cargos;
  - Publicación;
  - Observaciones;
  - Expediente digital;
  - Historial;
  - Auditoría.
- Panel derecho:
  - próxima acción permitida;
  - observaciones pendientes;
  - documentos pendientes;
  - resolución;
  - intentos de notificación;
  - publicación;
  - expediente digital;
  - alertas de plazo.
- Timeline:
  - fecha;
  - movimiento;
  - origen;
  - destino;
  - usuario;
  - comentario;
  - motivo.

Las acciones deben mostrarse como botones desde `VW_EXPEDIENTE_ACCIONES_PERMITIDAS`, pero en la primera fase no deben ejecutar movimientos.

## N. Plan de implementación por fases

FASE 1: Crear conexión paralela a `SDRERC_APP`.

- No tocar `OracleConnection`.
- Crear `SdrercAppConnection`.
- Externalizar credenciales gradualmente.

FASE 2: Crear DAOs de lectura sobre vistas.

- Implementar DAOs nuevos solo para SELECT.
- Usar `VW_EXPEDIENTE_BANDEJA` y `VW_EXPEDIENTE_CONSOLA`.

FASE 3: Crear DTOs.

- DTOs planos, sin lógica de UI.
- Mapear tipos Oracle con null safety.

FASE 4: Crear servicios de consulta.

- Servicios que componen datos de bandeja y consola.
- No modificar BD desde estos servicios.

FASE 5: Crear bandeja nueva piloto.

- Vista nueva en menú sin reemplazar pantallas legacy.
- Filtros superiores y tabla moderna.

FASE 6: Crear consola visual del expediente.

- Abrir desde la bandeja.
- Mostrar encabezado, etapas, pestañas y panel derecho.

FASE 7: Mostrar acciones permitidas.

- Botones no ejecutables o con mensaje "pendiente de implementación".
- Fuente: `VW_EXPEDIENTE_ACCIONES_PERMITIDAS`.

FASE 8: Implementar movimiento de expediente con historial.

- Servicio transaccional.
- Validar transición.
- Actualizar expediente.
- Insertar historial.
- No usar IDs hardcodeados.

FASE 9: Migración piloto de datos.

- Validar con expediente de prueba.
- Comparar legacy vs. `SDRERC_APP`.

FASE 10: Reemplazo progresivo de pantallas legacy.

- Migrar por módulo.
- Mantener rollback visual.

## O. Riesgos

- Cambiar la conexión global rompería todo el legacy.
- Los repositorios actuales usan tablas y columnas legacy.
- Existen IDs de catálogo hardcodeados en constantes y modelos.
- Algunas reglas de negocio están dentro de repositorios o servicios con SQL.
- Hay operaciones destructivas legacy (`DELETE`) en repositorios para reemplazo de detalles/roles.
- El flujo nuevo usa códigos y vistas; el legacy usa IDs numéricos.
- La UI actual combina pantallas NetBeans generadas, AbsoluteLayout y componentes modernos.
- El rediseño puede volverse demasiado grande si se intenta reemplazar todas las bandejas a la vez.
- `SDRERC_APP` debe permanecer como fuente de lectura al inicio para evitar inconsistencias.

## P. Archivos que modificaría o crearía

Crear:

- `src/main/java/com/sdrerc/infrastructure/database/SdrercAppConnection.java`
- `src/main/java/com/sdrerc/domain/dto/sdrercapp/ExpedienteBandejaDTO.java`
- `src/main/java/com/sdrerc/domain/dto/sdrercapp/ExpedienteConsolaDTO.java`
- `src/main/java/com/sdrerc/domain/dto/sdrercapp/ExpedienteTimelineDTO.java`
- `src/main/java/com/sdrerc/domain/dto/sdrercapp/ExpedienteDocumentoDTO.java`
- `src/main/java/com/sdrerc/domain/dto/sdrercapp/ExpedienteAccionPermitidaDTO.java`
- `src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/ExpedienteBandejaDAO.java`
- `src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/ExpedienteConsolaDAO.java`
- `src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/ExpedienteTimelineDAO.java`
- `src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/FlujoAccionDAO.java`
- `src/main/java/com/sdrerc/application/sdrercapp/ExpedienteConsultaService.java`
- `src/main/java/com/sdrerc/application/sdrercapp/FlujoExpedienteService.java`
- `src/main/java/com/sdrerc/ui/views/expedienteconsola/JPanelBandejaExpedientesNueva.java`
- `src/main/java/com/sdrerc/ui/views/expedienteconsola/JPanelExpedienteConsola.java`
- `src/main/java/com/sdrerc/ui/common/sdrerc/EstadoBadgeRenderer.java`
- `src/main/java/com/sdrerc/ui/common/sdrerc/EtapaBadgeRenderer.java`
- `src/main/java/com/sdrerc/ui/common/sdrerc/StageProgressPanel.java`
- `src/main/java/com/sdrerc/ui/common/sdrerc/ExpedienteHeaderPanel.java`
- `src/main/java/com/sdrerc/ui/common/sdrerc/ActionButtonPanel.java`

Modificar más adelante:

- `MenuPrincipal.java` para agregar acceso al módulo piloto.
- `AppUiConfig.java` si se requiere estandarizar más estilos.
- No modificar inicialmente `OracleConnection.java`.

## Q. Recomendación del primer módulo piloto

El primer módulo piloto debe ser:

```text
BandejaExpedientesNueva + ExpedienteConsola
```

Razón:

- Permite validar `SDRERC_APP` sin modificar datos desde Java.
- Usa vistas ya creadas.
- No rompe pantallas legacy.
- Permite probar el flujo `SDRERC_TO_BE` con acciones permitidas.
- Es el mayor salto de UX con menor riesgo técnico.

Primer incremento recomendado:

1. Crear `SdrercAppConnection`.
2. Crear `ExpedienteBandejaDTO`.
3. Crear `ExpedienteBandejaDAO` leyendo `VW_EXPEDIENTE_BANDEJA`.
4. Crear `ExpedienteConsultaService`.
5. Crear `JPanelBandejaExpedientesNueva`.
6. Agregar acceso temporal en `MenuPrincipal`.

No implementar ejecución de acciones hasta que lectura, bandeja, consola y acciones permitidas estén validadas con expedientes reales y de prueba.
