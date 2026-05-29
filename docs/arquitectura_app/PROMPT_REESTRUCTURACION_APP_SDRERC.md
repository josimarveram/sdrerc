# PROMPT_REESTRUCTURACION_APP_SDRERC.md

## Rol esperado

Actúa como arquitecto senior de software, experto en Java Swing,
FlatLaf, IntelliJ IDEA, Oracle XE, diseño UX/UI de aplicaciones
administrativas modernas, sistemas de expedientes, arquitectura por
capas, refactorización progresiva de sistemas legacy y buenas prácticas
con Codex CLI.

Estoy trabajando en el proyecto Java Swing **SDRERC** ubicado en:

    D:\2026\FuentesRENIEC\sdrerc_CODIGOS

La aplicación actual funciona sobre una base legacy en Oracle/SYSTEM,
pero ya se diseñó e implementó una nueva base Oracle XE llamada:

    SDRERC_APP

La nueva base incluye el flujo `SDRERC_TO_BE`, vistas de
bandeja/consola, historial, auditoría, documentos, notificaciones,
cargos, publicación, expediente digital y reglas de transición.

## Objetivo general

Iniciar la reestructuración progresiva de la aplicación Java Swing
SDRERC para:

1.  Conectarla progresivamente a la nueva base `SDRERC_APP`.

2.  Mantener funcionando el sistema legacy mientras se migra por fases.

3.  Crear una nueva arquitectura interna más limpia:

    - conexión;
    - DAOs;
    - DTOs;
    - servicios;
    - vistas Swing;
    - componentes visuales reutilizables.

4.  Rediseñar visualmente la aplicación para que sea más moderna,
    amigable, entendible y orientada a una **consola de expedientes por
    etapas**.

## Contexto actual de base de datos

La base `SDRERC_APP` ya existe.

Ya se ejecutaron los scripts base:

    01 al 11

También se ejecutaron patches incrementales:

    14_patch_flujo_verificacion_correccion.sql
    15_patch_reversion_ejecucion_a_analisis.sql

El flujo `SDRERC_TO_BE` contempla:

    REGISTRO
    ASIGNACION
    ANALISIS
    VERIFICACION
    FIRMA_EMISION
    EJECUCION
    NOTIFICACION
    PUBLICACION_CONDICIONAL
    EXPEDIENTE_DIGITAL
    CIERRE_ARCHIVO

También contempla:

    - devolución desde verificación a análisis;
    - corrección y reenvío;
    - reversión desde ejecución a análisis;
    - notificación virtual;
    - notificaciones presenciales;
    - cargos de acuse;
    - publicación condicional;
    - expediente digital;
    - cierre y archivo.

## Regla crítica

No crear ni mostrar una etapa visual llamada:

    VALIDACION

Las validaciones deben manejarse como:

    acciones,
    evaluaciones,
    observaciones,
    resultados,
    reglas de transición

dentro de cada etapa correspondiente.

## Objetivo visual

El sistema debe evolucionar de una interfaz plana basada en
formularios/tablas hacia una interfaz tipo **consola moderna de
expediente/caso**.

La nueva experiencia debe permitir al usuario entender rápidamente:

    - dónde está el expediente;
    - quién lo tiene;
    - qué etapa sigue;
    - qué acción está permitida;
    - si está vencido;
    - si tiene observaciones;
    - si tiene documentos pendientes;
    - si tiene resolución;
    - si está en ejecución;
    - si está en notificación;
    - si requiere publicación;
    - si tiene expediente digital completo;
    - cuál fue su historial.

## Diseño visual esperado

### 1. Nueva bandeja de expedientes

Proponer una pantalla piloto, por ejemplo:

    BandejaExpedientesNuevaView
    JPanelBandejaExpedientesNueva

Debe leer desde:

    VW_EXPEDIENTE_BANDEJA

Debe mostrar:

    - número de expediente;
    - número de trámite;
    - titular principal;
    - procedimiento;
    - etapa actual;
    - estado actual;
    - abogado inicial;
    - responsable actual;
    - fecha de registro;
    - fecha de último movimiento;
    - días restantes;
    - alerta;
    - tiene observaciones;
    - tiene documentos pendientes;
    - requiere publicación;
    - expediente digital completo.

Debe tener:

    - filtros superiores agrupados en una tarjeta;
    - búsqueda por expediente/trámite/titular;
    - filtro por etapa;
    - filtro por estado;
    - filtro por responsable;
    - filtro por fecha;
    - botones Buscar, Limpiar, Exportar, Ver detalle;
    - tabla con filas alternadas;
    - badges visuales de estado;
    - semáforo de días restantes;
    - diseño sobrio, institucional y moderno.

### 2. Consola visual del expediente

Proponer una pantalla piloto, por ejemplo:

    ExpedienteConsolaView
    JPanelExpedienteConsola

Debe leer desde:

    VW_EXPEDIENTE_CONSOLA
    VW_EXPEDIENTE_TIMELINE
    VW_EXPEDIENTE_DOCUMENTOS
    VW_EXPEDIENTE_DOCUMENTOS_ANALIZADOS
    VW_EXPEDIENTE_EVALUACIONES
    VW_EXPEDIENTE_RESOLUCIONES
    VW_EXPEDIENTE_NOTIFICACIONES
    VW_EXPEDIENTE_CARGOS_ACUSE
    VW_EXPEDIENTE_PUBLICACIONES
    VW_EXPEDIENTE_DIGITAL
    VW_EXPEDIENTE_ACCIONES_PERMITIDAS

Debe tener:

### A. Encabezado del expediente

Mostrar:

    - número de expediente;
    - número de trámite;
    - titular principal;
    - procedimiento;
    - acta;
    - etapa actual;
    - estado actual;
    - abogado inicial;
    - responsable actual;
    - equipo actual;
    - fecha registro;
    - fecha vencimiento;
    - días restantes;
    - número de resolución;
    - indicador de notificación;
    - indicador de cargo de acuse;
    - indicador de publicación;
    - indicador de expediente digital.

### B. Barra visual de etapas

Mostrar:

    Registro
    Asignación
    Análisis
    Verificación
    Firma/Emisión
    Ejecución
    Notificación
    Publicación
    Expediente digital
    Cierre/Archivo

Reglas visuales:

    - completadas: verde;
    - actual: azul;
    - pendientes: gris;
    - observadas/corrección: naranja;
    - vencidas/críticas: rojo;
    - cerradas/archivadas: gris oscuro;
    - publicación: condicional;
    - expediente digital: etapa o tarjeta de completitud.

### C. Panel central con pestañas

    Detalles
    Solicitudes
    Actas
    Personas
    Documentos
    Documentos analizados
    Evaluaciones
    Resoluciones
    Notificaciones
    Cargos de acuse
    Publicación
    Observaciones
    Expediente digital
    Historial
    Auditoría

### D. Panel lateral derecho

Tarjetas de resumen:

    - Abogado inicial;
    - Responsable actual;
    - Última acción;
    - Próxima acción permitida;
    - Documentos pendientes;
    - Observaciones pendientes;
    - Número de resolución;
    - Intentos de notificación;
    - Cargo de acuse;
    - Publicación;
    - Expediente digital;
    - Alertas de plazo.

### E. Timeline / historial

Mostrar:

    - fecha;
    - acción;
    - etapa origen;
    - estado origen;
    - etapa destino;
    - estado destino;
    - usuario origen;
    - usuario destino;
    - comentario;
    - motivo;
    - documento/resolución/notificación relacionada.

## Acciones permitidas

En la consola deben mostrarse botones según:

    VW_EXPEDIENTE_ACCIONES_PERMITIDAS

Ejemplos:

    Asignar abogado
    Recibir asignación
    Enviar a verificación
    Registrar observación de verificación
    Devolver a análisis
    Corregir documento
    Reenviar a verificación
    Aprobar verificación
    Enviar a firma
    Firmar documento
    Registrar número de resolución
    Iniciar ejecución
    Revertir documento en ejecución
    Devolver a análisis desde ejecución
    Derivar a notificación
    Registrar notificación virtual
    Registrar notificación presencial
    Registrar cargo de acuse
    Generar publicación
    Cerrar expediente
    Archivar expediente

No implementar todavía la ejecución real de acciones. Primero solo
analizar, proponer y preparar arquitectura.

## Arquitectura técnica esperada

Analizar el proyecto actual y proponer nuevas capas sin romper el
legacy.

### DTOs sugeridos

    ExpedienteBandejaDTO
    ExpedienteConsolaDTO
    ExpedienteTimelineDTO
    ExpedienteDocumentoDTO
    ExpedienteAccionPermitidaDTO
    ExpedienteResolucionDTO
    ExpedienteNotificacionDTO
    ExpedienteDigitalDTO

### DAOs sugeridos

    ExpedienteBandejaDAO
    ExpedienteConsolaDAO
    ExpedienteTimelineDAO
    ExpedienteDocumentoDAO
    FlujoAccionDAO
    ExpedienteResolucionDAO
    ExpedienteNotificacionDAO
    ExpedienteDigitalDAO

### Servicios sugeridos

    ExpedienteConsultaService
    FlujoExpedienteService
    ExpedienteHistorialService
    ExpedienteDocumentoService
    ExpedienteNotificacionService
    ExpedienteDigitalService

### Componentes Swing reutilizables sugeridos

    EstadoBadgeRenderer
    EtapaBadgeRenderer
    DiasRestantesRenderer
    StageProgressPanel
    ExpedienteHeaderPanel
    ExpedienteSideInfoPanel
    ExpedienteTimelinePanel
    ActionButtonPanel
    CardPanel
    SearchFilterPanel

## Reglas técnicas obligatorias

    - No eliminar código legacy.
    - No cambiar conexión global todavía.
    - No migrar todas las pantallas de golpe.
    - No usar IDs hardcodeados.
    - Usar códigos de catálogo.
    - Usar vistas de SDRERC_APP para lectura.
    - Mantener conexión legacy disponible.
    - Crear conexión paralela a SDRERC_APP.
    - Evitar SQL complejo dentro de formularios.
    - Centralizar consultas en DAOs.
    - Centralizar reglas de transición en servicios.
    - Todo movimiento futuro debe dejar historial.
    - No ejecutar SQL destructivo.
    - No modificar BD desde Java todavía.
    - Primero lectura, luego acciones.
    - Primero análisis, luego implementación.

## Trabajo inicial solicitado a Codex

No modifiques código todavía.

Primero analiza el proyecto actual e identifica:

    1. Clase o clases actuales de conexión Oracle.
    2. Configuración actual de usuario, password, host, puerto y servicio.
    3. Formularios principales relacionados con:
       - recepción;
       - asignación;
       - expedientes por trabajar;
       - expedientes por verificar;
       - ejecución;
       - notificación;
       - administración.
    4. DAOs existentes.
    5. Modelos existentes.
    6. SQL embebido en formularios.
    7. Clases que usan tablas legacy en SYSTEM.
    8. Estados legacy hardcodeados.
    9. Pantallas que deberían migrarse primero.
    10. Riesgos de cambiar conexión de golpe.
    11. Uso actual de FlatLaf o estilos visuales.
    12. Estructura actual de paquetes Java.
    13. Si el proyecto usa Maven, Gradle u otra estructura.
    14. Posibles clases/componentes reutilizables existentes.

## Entregable inicial obligatorio

Antes de modificar código, genera un informe técnico en:

    docs/arquitectura_app/INFORME_REESTRUCTURACION_APP_SDRERC.md

El informe debe incluir:

    A. Resumen del proyecto actual.
    B. Clases actuales de conexión Oracle.
    C. Formularios actuales que deben ser migrados.
    D. DAOs existentes.
    E. Modelos existentes.
    F. SQL embebido encontrado.
    G. Estados legacy hardcodeados.
    H. Pantallas prioritarias para rediseño.
    I. Propuesta de nueva arquitectura por capas.
    J. Propuesta de clases nuevas.
    K. Propuesta de componentes visuales Swing.
    L. Propuesta de rediseño visual de bandeja.
    M. Propuesta de rediseño visual de consola del expediente.
    N. Plan de implementación por fases.
    O. Riesgos.
    P. Archivos que modificarías o crearías.
    Q. Recomendación del primer módulo piloto.

## Plan de implementación esperado

Proponer el plan por fases:

    FASE 1: Crear conexión paralela a SDRERC_APP sin eliminar conexión legacy.
    FASE 2: Crear DAOs de lectura sobre vistas.
    FASE 3: Crear DTOs.
    FASE 4: Crear servicios de consulta.
    FASE 5: Crear bandeja nueva piloto.
    FASE 6: Crear consola visual del expediente.
    FASE 7: Mostrar acciones permitidas.
    FASE 8: Implementar movimiento de expediente con historial.
    FASE 9: Migración piloto de datos.
    FASE 10: Reemplazo progresivo de pantallas legacy.

## Restricciones finales

    - No hagas cambios de código en esta primera respuesta.
    - No crees clases todavía.
    - No cambies archivos Java todavía.
    - No ejecutes SQL.
    - No borres archivos.
    - No cambies la conexión global.
    - Solo analiza y genera el informe técnico solicitado.
