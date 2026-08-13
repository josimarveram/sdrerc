# Plan de migración SDRERC V2 (Swing) → SDRERC V3 (Web)

## Contexto

SDRERC V2 es una aplicación de escritorio (Java 8, Swing, FlatLaf, Maven, Oracle `SDRERC_APP`) que gestiona el flujo completo de expedientes registrales: Registro → Asignación → Análisis → Verificación → Ejecución/Notificación (con su 4ª sub-pestaña `Publicación`) → Publicación condicional → Expediente digital, más Administración y Dashboard. Se solicita un plan para migrarla a una versión web nueva, SDRERC V3, priorizando velocidad de entrega mediante la reutilización de la lógica de negocio ya construida, sin tocar V2 ni su base de datos compartida de forma insegura durante la transición. **Alcance de esta migración**: por pedido explícito del usuario, el módulo standalone `Publicación condicional` y `Expediente digital` quedan fuera de esta migración (se retoman más adelante); el resto del flujo sí forma parte del roadmap.

Antes de diseñar este plan se leyó `AGENTS.md` y `CLAUDE.md` completos (fuente de verdad funcional, con cientos de reglas ya validadas en producción/pruebas que V3 debe cumplir como requisito, no como sugerencia) y se investigó el código real de V2: la capa `application/sdrercapp` (59 Services), `infrastructure/sdrercapp/dao` (47 DAOs), `domain/dto/sdrercapp` (99 DTOs, ~34.000 líneas), el mecanismo de login/2FA (`AutenticacionService`, `TotpService`, `TotpSecretCipher`, `EmailOtpMailer`), y el empaquetado/despliegue (`pom.xml`, `MainV2`, scripts de release LAN, generación de Word con Apache POI). Los hallazgos concretos de esa investigación sustentan cada recomendación de este plan.

**Hallazgo central que define la estrategia**: la capa Service/DAO de V2 está **completamente libre de Swing/AWT** (verificado con grep, cero resultados) y sus DTOs son POJOs planos. El principal obstáculo para portarla a web no es reescribir lógica de negocio, sino reemplazar un mecanismo puntual: `SessionContext` (`src/main/java/com/sdrerc/shared/session/SessionContext.java`) es un singleton con campos `static` que asume "un proceso JVM = un usuario" — válido en escritorio, incompatible con un servidor web multiusuario. Ya existe dentro del propio código un patrón correcto a generalizar: `VisibilidadBandejaSql` (`src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/VisibilidadBandejaSql.java`) recibe el usuario como parámetro explícito en vez de consultar el singleton.

No se encontró evidencia de jobs/procesos en background en V2 (sin `Timer`, `ScheduledExecutorService`, Quartz, cron). Todo el trabajo ocurre síncronamente dentro de la acción de un usuario, con transacciones manuales por método DAO. Esto simplifica la convivencia V2/V3: el único vector de conflicto real es escritura concurrente humano-vs-humano, no humano-vs-proceso automático.

---

## 1. Estructura de repo propuesta

Monorepo: módulo nuevo `v3/` en el mismo repo Git, sin agregarlo como `<module>` del `pom.xml` raíz de V2 (para que `mvn package` de V2 no se vea afectado ni en build ni en dependencias transitivas). Se descarta un repo separado porque complicaría copiar/adaptar clases DAO/Service con el código V2 a la vista; si más adelante V3 necesita ciclo de release independiente, se extrae con `git subtree split` sin perder historia.

```
sdrerc_CODIGOS/
├── src/main/java/...              ← V2, intocable
├── pom.xml                        ← pom V2, intocable
├── v3/
│   ├── sdrerc-v3-backend/         ← Maven, Spring Boot, Java 17
│   │   └── src/main/java/com/sdrerc/v3/
│   │       ├── application/       ← Services portados/adaptados
│   │       ├── infrastructure/    ← DAOs portados/adaptados, config datasource
│   │       ├── domain/            ← DTOs portados casi literal
│   │       ├── web/               ← @RestController, DTOs request/response
│   │       ├── security/          ← reemplazo de SessionContext (request-scoped)
│   │       └── config/
│   └── sdrerc-v3-frontend/        ← proyecto npm independiente
│       ├── src/
│       └── package.json
├── CLAUDE.md
└── AGENTS.md
```

---

## 2. Stack tecnológico recomendado

### Backend: Spring Boot 3.x sobre Java 17, mismo driver Oracle (`ojdbc8`, validar si conviene `ojdbc11`)

Es la opción más **rápida**, no la más "moderna" en abstracto, porque:

- El activo más caro de reconstruir (59 Services + 47 DAOs, ~34.000 líneas) ya tiene exactamente el shape que Spring espera: beans Java planos inyectados por constructor, sin Swing que desenredar.
- Los DAOs ya usan JDBC puro (`PreparedStatement`/`ResultSet`) — portar un DAO es cambiar `SdrercAppConnection.getConnection()` por una `DataSource` inyectada, no reescribir SQL.
- HikariCP, BCrypt (`jbcrypt`), Apache POI (`poi-ooxml`) y Jakarta Mail ya son dependencias del `pom.xml` de V2 — se reutilizan las mismas versiones en el `pom.xml` de `v3-backend`, sin evaluar alternativas.
- El equipo ya domina Java/Maven/Oracle: la única curva de aprendizaje nueva es Spring (anotaciones, DI, ciclo de request) y el frontend, inevitables en cualquier stack.

Se descartan explícitamente Node/NestJS o Python/FastAPI (obligarían a reescribir 47 DAOs y 59 Services desde cero, contradiciendo el requisito de velocidad) y Quarkus/Micronaut (mismo ecosistema Java sin ventaja real sobre Spring Boot para este caso: auth stateless + JDBC legado).

### Frontend: React + TypeScript + Vite, con una librería de componentes madura (MUI o Ant Design)

El dominio tiene bandejas con muchas columnas, filtros por columna, sub-pestañas (Notificación tiene 4) y formularios largos — terreno donde tablas/tabs/date-pickers ya resueltos ahorran semanas. Se descarta SSR/Next.js: es una app interna LAN sin necesidad de SEO; una SPA de estáticos servida junto al backend (o nginx simple) es más simple de operar en el modelo LAN/Tailscale actual.

**Diseño/layout: mucha más libertad que en V2, y se define desde el inicio (importante, pedido explícito del usuario).** Swing con FlatLaf está limitado por gestores de layout rígidos — `AGENTS.md` documenta varios episodios de esfuerzo extra solo para lograr una altura dinámica sin scroll (`GridBagLayout` con `fill=HORIZONTAL`, incompatibilidad de `CardLayout` con ese patrón, etc.). En web, CSS flexbox/grid más una librería de componentes (MUI o Ant Design) hacen mucho más simple ajustar espaciados, densidad de tablas, temas de color y breakpoints responsive. Como cada panel se reescribe desde cero en React (no hay port de código Swing), la migración es la oportunidad natural para mejorar visualmente — pero para no terminar con cada módulo viéndose distinto según quién lo construyó, **se define una guía de estilo/design system único (tema, tipografía, espaciados, densidad de tablas, paleta) en la Fase 0**, antes de construir el primer módulo de negocio (Dashboard, Fase 1), y todos los módulos posteriores lo reutilizan en vez de decidir estilos módulo por módulo. Ver Fase 0 en la sección 7.

### Autenticación/2FA en contexto web

Se reutilizan **literalmente** los POJOs ya identificados sin contaminación de Swing: `AutenticacionService`, `TotpService`, `TotpSecretCipher`, `EmailOtpMailer`, `UsuarioDAO`, `UsuarioEmailOtpDAO`, `UsuarioTotpBackupCodeDAO`, `PasswordEncoder`(BCrypt). Solo cambia el mecanismo de sesión:

1. `POST /api/auth/login` (usuario+password) → `AutenticacionService.iniciarLogin(...)` → si requiere 2FA, responde con un `loginChallengeId` de corta vida (sin autenticar aún).
2. `POST /api/auth/2fa` (challengeId + código TOTP/OTP-correo/backup code) → misma lógica de verificación existente → si es válido, `completarLogin(idUsuario)` (idéntico a V2: resetea intentos, registra último login, carga roles) y el backend emite un **JWT de corta duración (15-30 min) + refresh token**, en vez de setear el singleton `SessionContext`.
3. Se prefiere JWT sobre sesión server-side porque encaja mejor con el modelo LAN/Tailscale sin estado compartido entre instancias, y porque el bloqueo (5 intentos/15 min) y `debe_cambiar_password` ya se validan en cada login dentro de `AutenticacionService`, no dependen de revocar sesiones a mitad de vida del token. Revocación inmediata (ej. desactivar usuario en caliente) queda como mejora futura con tabla de tokens revocados, no bloqueante para la primera fase.
4. `SessionContext` se reemplaza por un contexto **request-scoped** (`AuthenticatedUser` extraído del JWT en un filtro, pasado explícitamente a los Services) — es la generalización del patrón ya bueno de `VisibilidadBandejaSql` aplicada a los ~15 Services que hoy llaman `resolverUsuarioActualSdrercApp()` contra el singleton estático.

---

## 3. Convivencia V2/V3 sobre la misma Oracle

Sin jobs en background en V2 que respetar, el único riesgo real es escritura concurrente humano-vs-humano sobre el mismo expediente desde ambos sistemas.

- **Transacciones**: se preserva la semántica actual (commit/rollback manual dentro de cada método DAO) al portar; orquestar transacciones a nivel Service es una mejora opcional post-paridad, no parte de esta migración.
- **Bloqueo optimista** (no pesimista): validar en cada `UPDATE` que ninguna edición paralela desde V2 se pise (comparar timestamp de última modificación si la columna ya existe en la tabla, o agregarla puntualmente donde falte y se justifique). Se prefiere sobre `SELECT FOR UPDATE` porque el patrón actual de V2 es de conexiones cortas sin locks retenidos entre pantallas; un lock pesimista de larga duración no encaja y aumentaría el riesgo de bloqueos cruzados entre ambos sistemas.
- **Caché en V3**: solo de lectura con TTL bajo (catálogos, feriados, permisos) — nunca fuente de verdad, porque V2 puede escribir por debajo en cualquier momento.
- **Pool de conexiones**: V2 y V3 son procesos separados con pools HikariCP independientes; el riesgo no es compartir conexiones sino que Oracle reciba, en conjunto, más conexiones concurrentes de las que su `PROCESSES`/`SESSIONS` soporte (ver riesgos).
- **Auditoría/historial**: V3 debe replicar exactamente la regla de sustitución de autor cuando actúa `ADMIN_SISTEMA` (ya documentada en `CLAUDE.md`), para que el historial combinado V2+V3 sea coherente y legible desde Expediente Digital sin importar el origen del evento.

---

## 4. Orden de migración módulo por módulo

| Fase | Módulo | Razón del orden |
|---|---|---|
| 1 | **Dashboard** | Solo lectura, aislado, sin riesgo de conflicto con V2. Piloto ideal para validar el esqueleto completo (auth+JWT, datasource, despliegue, frontend base) sin arriesgar datos transaccionales. |
| 2 | **Registro / Bandeja de expedientes** | Menor complejidad de transiciones de estado; alto valor por ser el punto de entrada del sistema. |
| 3 | **Asignación** | Introduce visibilidad por asignación (`VisibilidadBandejaSql`) y permisos por rol en un módulo con escritura real: valida el reemplazo de `SessionContext` fuera de un caso trivial. |
| 4 | **Análisis** | Introduce generación de documentos Word (POI) bajo request HTTP concurrente. |
| 5 | **Verificación** | Forma similar a Análisis, reutiliza patrones ya validados. |
| 6 | **Ejecución** | Consolida los patrones de transición de estado de las fases 3-5. |
| 7 | **Notificación** (4 sub-pestañas: Asignación/Validación/Notificación/Publicación) | La más compleja del sistema (más estados/transiciones/sub-bandejas según `CLAUDE.md`); se aborda al final, con el equipo ya dominando stack, auth, visibilidad y generación de documentos. La 4ª sub-pestaña, `Publicación`, no es el módulo standalone `Publicación condicional` (ver nota debajo): se activa cuando un expediente acumuló 2 intentos de notificación con resultado `NO UBICADO` en la 3ª sub-pestaña (`Notificación`), y ahí se agrega el registro de publicación correspondiente. |
| 8 | **Administración** (Usuarios, Roles, Equipo Jurídico, Feriados, Plazos, Plantillas) | Prerrequisito operativo (crear usuarios, cargar feriados) desde la Fase 2 en adelante — se paraleliza parcialmente, no es literalmente "última" en trabajo aunque cierre el roadmap. |

**Fuera de alcance de esta migración (aclaración del usuario, 11/08/2026)**: el módulo standalone **`Publicación condicional`** (macro-etapa `PUBLICACION_CONDICIONAL`, distinto de la sub-pestaña `Publicación` dentro de Notificación) y **`Expediente digital`** se **omiten** de este plan por ahora; se piensan más adelante, no forman parte de las fases de esta migración. Tampoco existe una pestaña ni módulo `Cierre` como tal en V3 (se descarta esa noción, no es solo un cambio de nombre): si en el futuro se modela un "cierre" del ciclo de vida del expediente, sería un **cambio de estado automático** (candidato: disparado por la emisión de la carta final) — pendiente de definir, fuera de alcance de este plan.

---

## 5. Qué se porta casi literal vs. qué se rediseña

**Casi literal** (solo se adapta conexión/transacción/sumidero de salida):
- Los 47 DAOs, cambiando `SdrercAppConnection.getConnection()` por `DataSource` de Spring — empezando por `VisibilidadBandejaSql` como plantilla de referencia.
- Services de cálculo puro: `CalendarioLaboralService`, `CorrelativoExpedienteService`, `PermisoRolService` (salvo la resolución de usuario actual).
- Autenticación/2FA completa: `AutenticacionService`, `TotpService`, `TotpSecretCipher`, `EmailOtpMailer`, `UsuarioDAO`, `UsuarioEmailOtpDAO`, `UsuarioTotpBackupCodeDAO`.
- `AnalisisPlantillaDocumentoService.generarDocumento(...)`: cambia el destino de `Path` a `OutputStream`/`byte[]` para servir por HTTP; `PlantillaDocumentoDAO` no cambia nada porque ya trabaja con `byte[]` puro sobre BLOB.
- Los 99 DTOs, como POJOs; opcionalmente se deriva un contrato OpenAPI/TS para el frontend.
- La lógica de negocio de todos los Services de dominio (Registro, Asignación, Análisis, Verificación, Ejecución, Notificación): no cambia, solo el mecanismo de invocación.

**Debe rediseñarse** (inherente a escritorio, sin equivalente directo):
- `SessionContext` → contexto request-scoped derivado del JWT, generalizando `VisibilidadBandejaSql`. Es el cambio transversal de mayor riesgo: toca los ~15 Services que llaman `resolverUsuarioActualSdrercApp()`.
- `SwingWorker` → llamadas HTTP asíncronas desde React (fetch + estado de carga); si algún caso puntual resulta demasiado largo para request-response síncrono, se evalúa polling caso por caso, no de antemano.
- Paneles/`JFrame`/`JDialog` → componentes React, reescritura de UI guiada por las reglas funcionales de `CLAUDE.md` (no hay port de código aquí).
- `MainV2`/`LoginFrameV2`/`MenuPrincipalV2`: sin código reutilizable literal, pero el concepto "login exitoso → cargar menú según roles" se traslada como rutas protegidas por rol en React.
- Acoplamiento DAO→Service en constructor (ej. `AsignacionExpedienteDAO` instanciando `CalendarioLaboralService` directamente): se resuelve con inyección de dependencias de Spring, lo cual de hecho corrige el acoplamiento como efecto colateral, sin refactor dedicado.
- `config/sdrerc-app.properties` plano → configuración por perfil de Spring Boot (`application-{profile}.yml`) con secretos fuera del repo, preservando el principio de cascada (property → env var → archivo) que ya usa `TotpSecretCipher`.

---

## 6. Riesgos concretos y mitigación

1. **Fuga de identidad por `SessionContext`**: si algún Service portado conserva una llamada directa a `SessionContext.getUsername()/hasRole()/tienePermiso()`, un servidor con requests concurrentes de usuarios distintos puede operar con la identidad equivocada. Mitigación: el módulo `v3-backend` no debe tener en su classpath ninguna clase `SessionContext` de V2; forzarlo con un test estático (ej. ArchUnit) que rompa el build si aparece esa dependencia.
2. **Pool HikariCP dimensionado para escritorio** (`DEFAULT_POOL_MAX_SIZE=5` en V2, comentario explícito en el código de que no está pensado para concurrencia compartida): el pool de V3 debe dimensionarse para concurrencia web real, sumado a las conexiones que V2 sigue abriendo. Mitigación: dimensionar de forma conservadora contra el `PROCESSES`/`SESSIONS` real de la instancia Oracle y monitorear consumo total V2+V3 antes de escalar usuarios a V3.
3. **Acoplamiento DAO→Service en constructor**: si se porta mecánicamente sin DI, se corre el riesgo de instanciar Services (y abrir conexiones) en cada request. Mitigación: normalizar a inyección de dependencias desde el primer port.
4. **Permisos con fail-open** (`tienePermiso()` permite todo si no hay catálogo cargado): aceptable en un JAR de escritorio, más riesgoso en una superficie web. Recomendación: cambiar a **fail-closed** en el backend de V3 (denegar si no hay catálogo cargado), documentado como decisión deliberada distinta de V2.
5. **Visibilidad por asignación**: `VisibilidadBandejaSql` debe alimentarse siempre del usuario real de cada request, nunca de un valor cacheado — mismo riesgo que el punto 1 pero a nivel de filtro de datos. Mitigación: pasar `idUsuarioActual` explícito desde el controller en cada llamada.
6. **`XWPFDocument` (Apache POI) no es thread-safe**: cada request de generación de Word debe crear su propia instancia, sin reutilizar ni cachear plantillas abiertas como singleton. Mitigación: revisar `AnalisisPlantillaDocumentoService` durante el port para confirmar ausencia de campos estáticos/de instancia que retengan el documento entre invocaciones.
7. **Bloqueo de intentos fallidos bajo concurrencia real**: `UsuarioDAO.registrarIntentoFallido` nunca fue probado bajo el perfil de carga adversarial de un servidor web expuesto (aunque sea LAN/Tailscale). Mitigación: reverificar atomicidad del `UPDATE` bajo concurrencia real antes de ir a producción con V3.
8. **Omisión de la sustitución de autor cuando actúa `ADMIN_SISTEMA`**: un desarrollador que porte un Service a REST puede omitirla al no ver `SessionContext` directamente. Mitigación: incluirla explícitamente en el checklist de port de cada Service (ver criterio de validación, sección 8).
9. **Superficie de red ampliada**: un backend HTTP amplía la superficie respecto a un JAR con solo conexión saliente a Oracle. Mitigación: reforzar HTTPS/rate-limiting en `/api/auth/*` aun dentro de Tailscale.

### Decisión sobre modelo de despliegue

Se recomienda **mantener LAN/Tailscale sin exponer Oracle a internet**, también para V3: el backend Spring Boot corre en un servidor dentro de la misma malla, Oracle sigue sin puerto público, y los navegadores acceden al backend vía Tailscale igual que hoy los JARs V2 acceden al share SMB/UNC de release. La diferencia estructural es que ahora hay un proceso servidor centralizado en vez de N procesos JVM de escritorio, lo cual de hecho reduce las conexiones directas a Oracle si el pool se dimensiona correctamente (riesgo 2). No se justifica exponer Oracle a internet ni cambiar el modelo de red para una migración cuyo objetivo es paridad funcional.

### Nota: procedimientos almacenados (evaluar más adelante)

No se incorporan procedimientos almacenados a este plan. Hoy toda consulta/acción se resuelve con SQL plano desde los DAOs (`PreparedStatement`/`ResultSet`) y no hay evidencia de que el cuello de botella real haya sido el round-trip app↔BD: el problema de performance ya identificado y corregido en V2 fue falta de índices en columnas de FK (`AGENTS.md`, 24/07/2026), no ausencia de procedimientos almacenados. Meter procedimientos ahora partiría la lógica de negocio entre Java (lo que este plan busca portar casi literal) y PL/SQL nueva, con más riesgo de romper reglas sensibles ya afinadas (visibilidad por asignación, permisos, sustitución de autor `ADMIN_SISTEMA`) que beneficio medido. **Queda como nota explícita para evaluar más adelante, si el perfilado real de V3 bajo carga lo justifica** — y, de justificarse, primero como reescritura puntual de la consulta a un solo SQL con joins, y solo si eso no alcanza, como procedimiento almacenado aislado a ese caso específico, no como política general desde el inicio.

---

## 7. Roadmap por fases (tamaño relativo, sin fechas)

- **Fase 0 — Cimientos** (pequeño): `v3-backend` con Spring Boot arrancando contra Oracle, port completo de autenticación/2FA (login+TOTP+OTP correo+backup codes) con JWT, esqueleto `v3-frontend` con login funcionando contra el backend real, **más la guía de estilo/design system base** (tema, tipografía, espaciados, densidad de tablas, paleta, componentes de layout reutilizables — MUI o Ant Design como base). Sin módulos de negocio todavía. **Hito**: un usuario real se loguea en V3 con 2FA usando las mismas credenciales que en V2 y recibe un JWT válido, **y** existe un catálogo visual base (tema + 2-3 componentes de referencia: tabla con filtros, formulario, tabs) que el resto de fases reutiliza en vez de definir estilos módulo por módulo.
- **Fase 1 — Dashboard** (pequeño). **Hito**: un ADMIN_SISTEMA ve en V3 los mismos números que en V2 para el mismo período (verificado comparando al menos 3 métricas).
- **Fase 2 — Registro/Bandeja** (mediano, primer módulo de escritura). **Hito**: un expediente creado en V3 aparece correctamente en la bandeja de V2 con los mismos datos (prueba real de convivencia sobre la misma BD).
- **Fase 3 — Asignación** (mediano). **Hito**: dos usuarios con roles distintos ven bandejas distintas en V3 según `VisibilidadBandejaSql`, igual que en V2.
- **Fases 4-6 — Análisis, Verificación, Ejecución** (medianas, patrón ya establecido se repite). **Hito por fase**: un Word generado desde V3 es funcionalmente equivalente byte a byte al de V2 con la misma plantilla y datos.
- **Fase 7 — Notificación** (grande, la mayor del roadmap). **Hito**: las 4 sub-bandejas (Asignación/Validación/Notificación/Publicación) replican en V3 el mismo conteo/estado que V2 para un conjunto de expedientes de prueba controlado, incluyendo el paso de un expediente con 2 intentos `NO UBICADO` desde la 3ª sub-pestaña hacia el registro de publicación en la 4ª.
- **Fase 8 — Administración** (mediano, paralelizable desde la Fase 2). **Hito**: un usuario creado en V3 opera en V2 sin diferencias, y viceversa.
- **Fase 9 — Cutover progresivo**: por módulo, según el criterio de la sección 8; no es un "big bang" único.

**Fuera de alcance de este roadmap**: `Publicación condicional` (módulo standalone) y `Expediente digital` no tienen fase asignada en esta migración; quedan pendientes de retomar más adelante (ver nota de la sección 4).

---

## 8. Criterio de "módulo de V3 validado" para uso paralelo con usuarios reales

Un módulo se considera validado cuando cumple **todos** estos puntos:

1. **Paridad funcional contra `CLAUDE.md`**: cada regla documentada para ese módulo tiene un caso de prueba ejecutado en V3 con resultado idéntico al esperado en V2.
2. **Convivencia bidireccional**: una acción hecha en V2 es visible correctamente en V3 y viceversa, sin duplicar ni perder datos, para al menos un ciclo completo (alta, edición, cambio de estado) por tipo de operación del módulo.
3. **Concurrencia cruzada**: dos usuarios (uno en V2, uno en V3) sobre el mismo expediente en ventanas solapadas no producen pérdida de escritura silenciosa (valida el bloqueo optimista); un conflicto detectado muestra error claro, nunca sobrescribe en silencio.
4. **Auditoría coherente**: acciones desde V3 quedan en el historial con el mismo formato y reglas de autoría (incluida la sustitución para `ADMIN_SISTEMA`) que V2.
5. **Identidad verificada**: prueba con dos sesiones concurrentes de usuarios distintos en V3 confirmando ausencia de fuga de identidad/datos entre requests.
6. **Carga concurrente aceptada**: el módulo soporta sin degradación el número de usuarios concurrentes reales observado en V2 para ese módulo específico.
7. **Rollback disponible**: mecanismo documentado y probado para volver a operar exclusivamente en V2 sin pérdida de datos si aparece un problema.
8. **Aprobación funcional explícita** de al menos un usuario operativo real del módulo, tras un período de uso paralelo supervisado.

Solo con los 8 puntos cumplidos se habilita ese módulo para uso general en paralelo; el resto permanece exclusivamente en V2 hasta cumplir el mismo criterio de forma independiente.

---

## Archivos clave de V2 referenciados en este plan

- `src/main/java/com/sdrerc/shared/session/SessionContext.java` — punto de reemplazo transversal (singleton estático → contexto request-scoped), el cambio de mayor riesgo del plan.
- `src/main/java/com/sdrerc/infrastructure/sdrercapp/dao/VisibilidadBandejaSql.java` — patrón de referencia a generalizar (usuario como parámetro explícito).
- `src/main/java/com/sdrerc/application/sdrercapp/AutenticacionService.java` — núcleo del flujo login/2FA a portar casi literal detrás de endpoints REST.
- `src/main/java/com/sdrerc/infrastructure/database/SdrercAppConnection.java` — punto de adaptación de conexión/pool (HikariCP desktop-sized → `DataSource` dimensionada para web).
- `pom.xml` (raíz) — versiones de dependencias ya validadas (ojdbc8, jbcrypt, poi-ooxml, jakarta.mail, HikariCP) a reutilizar en `v3/sdrerc-v3-backend/pom.xml`.
- `CLAUDE.md` — fuente de verdad de reglas funcionales por módulo; referencia obligatoria para los criterios de paridad de cada fase.

---

## Anexo A: Propuesta inicial de guía de estilo (Fase 0)

Primera versión propuesta por el equipo de migración (sin capturas de referencia del usuario). Parte de la paleta institucional que **ya existe y está aprobada** en `src/main/java/com/sdrerc/ui/appv2/theme/AppV2Theme.java` (V2) — no se inventan colores nuevos, se traducen a tokens web y se completan los conceptos que Swing no tiene (radios, elevación, densidad, breakpoints). Esto cumple de entrada la regla de `AGENTS.md` de no copiar branding externo, y da continuidad visual con lo que el cliente ya conoce.

### Principio rector: paridad de datos/campos, libertad de diseño (pedido explícito del usuario, 11/08/2026)

**Todo formulario o pantalla de V3 debe capturar exactamente los mismos campos, en la misma agrupación funcional, que su equivalente real en V2** (mismo modelo de datos, mismas secciones, mismos campos obligatorios/opcionales) — verificado contra el código Swing real (`JPanelXxxV2.java`), no inventado ni simplificado por conveniencia. Ejemplo concreto detectado y corregido: el primer intento del formulario de Registro manual agregó campos que V2 no pide ahí (Año de acta, Ubicación registral) y movió/fusionó otros de forma distinta a como V2 los agrupa (N° expediente SGD, datos de contacto del solicitante, combos de ubigeo en cascada Departamento→Provincia→Distrito) — quedó corregido, pero el criterio queda documentado aquí para no repetir el error en los módulos siguientes.

Lo que **sí** es libertad total de V3 (y donde el usuario confirmó que el rediseño ya en curso le gusta): la implementación visual — colores, tipografía, densidad, layout responsive para cualquier dispositivo, agrupación en cards/columnas, MUI en vez de Swing. El Anexo A/B de este documento cubre esa libertad. Lo que **no** es libertad: qué datos existen y qué información pide cada pantalla — eso lo define el modelo de datos y los formularios reales de V2, no una reinterpretación del equipo de migración.

Esta regla vive en el plan de migración (no en `CLAUDE.md`/`AGENTS.md`, que son contexto de V2) porque es una regla de *cómo migrar*, no una regla funcional de V2 en sí.

### Librería de componentes recomendada: MUI (Material UI) + MUI X Data Grid

Entre MUI y Ant Design (dejado abierto en la sección 2), se recomienda **MUI** para esta propuesta concreta porque:
- Su API de theming (`createTheme`) mapea directo a los tokens de abajo (paleta, tipografía, `shape.borderRadius`, overrides por componente) sin pelear contra un look-and-feel muy opinado por defecto, a diferencia de Ant Design (más fácil de "des-brandear" hacia el tono institucional sobrio que pide `AGENTS.md`).
- `MUI X Data Grid` resuelve de fábrica exactamente el patrón que domina esta app: tablas densas con filtro por columna, orden por cabecera, selección múltiple y filas expandibles (jerarquía expediente principal/asociado) — el mismo patrón que hoy exige código Swing a medida (`AppV2ColumnFilterSupport`, `TableRowSorter`).

### Paleta (tokens de color, heredados de `AppV2Theme`)

| Token | Hex | Uso |
|---|---|---|
| `primary` | `#154775` | Color institucional principal (botones primarios, enlaces activos) |
| `primary.dark` | `#0C2F52` | Hover/pressed de elementos primarios oscuros |
| `primary.hover` | `#1F5B91` | Hover de botones primarios |
| `sidebar.bg` | `#11314E` | Fondo del menú lateral |
| `sidebar.hover` | `#163C5E` | Hover de ítem de menú |
| `sidebar.active` | `#0A2239` | Ítem de menú activo |
| `background.default` | `#F4F6F9` | Fondo general de la app |
| `background.paper` | `#FFFFFF` | Superficie de cards/paneles |
| `background.paperAlt` | `#FAFBFD` | Superficie alterna (filas striped, paneles secundarios) |
| `divider` | `#DAE1E9` | Bordes/separadores estándar |
| `divider.strong` | `#C4CDD8` | Bordes con más énfasis |
| `text.primary` | `#1C242E` | Texto principal |
| `text.secondary` | `#525F6E` | Texto secundario/etiquetas |
| `success` | `#268150` | Estados positivos (Emitido, Validado, Ubicado) |
| `warning` | `#B86910` | Estados de alerta (Por vencer, Observado) |
| `error` | `#B23434` | Estados críticos (Vencido, Fallida) |
| `info` | `#2370A8` | Estados neutros informativos |
| `soft.blue` / `.green` / `.orange` / `.red` / `.gray` | `#E5F1FC` / `#E5F4EC` / `#FFF3E0` / `#FDE8E8` / `#EFF2F6` | Fondos de badges de estado (texto en el color "fuerte" correspondiente) |

### Tipografía

- Familia: `"Segoe UI", system-ui, -apple-system, Roboto, Helvetica, Arial, sans-serif` (mantiene "Segoe UI" como preferido en Windows/LAN, con fallback seguro en cualquier navegador).
- Escala heredada de V2, en `rem` con base 16px: `small` 0.75rem (12px), `base` 0.8125rem (13px), `medium` 0.9375rem (15px), `title` 1.25rem (20px), `hero` 1.875rem (30px).

### Espaciado

Escala de 4px heredada de V2 (`8/12/20/28`) extendida a una escala completa de 8 puntos para cubrir casos que Swing no necesitaba: `4, 8, 12, 16, 20, 24, 28, 32, 40, 48`.

### Conceptos nuevos que Swing no tenía (a definir en Fase 0)

- **Radios de borde**: sutiles, `4px` en inputs/botones, `8px` en cards/paneles — moderno sin perder el tono institucional (nada de esquinas muy redondeadas tipo consumer app).
- **Elevación**: sombra muy suave (`0 1px 2px rgba(16,24,40,0.06)`) solo en cards flotantes/menús desplegables/modales; las tablas y paneles fijos van sin sombra, con borde `divider` como en V2.
- **Densidad de tabla**: densidad "compacta" (altura de fila ~36px) por defecto en todas las bandejas, igual de denso que las grillas Swing actuales; opción de densidad "cómoda" (~44px) como preferencia de usuario, no como default.
- **Breakpoints responsive**: la prioridad es escritorio/laptop en LAN (como V2), no mobile-first; un solo breakpoint de "colapsar sidebar a íconos" alrededor de 1280px es suficiente para la primera versión.
- **Badges de estado**: reutilizan el patrón ya validado de `StatusBadgeV2` (fondo `soft.*` + texto en el color fuerte correspondiente) — se porta el criterio, no el código Swing.

### Cómo se valida esta propuesta (Fase 0)

Se materializa como un tema MUI (`createTheme`) + 3 componentes de referencia construidos sobre él: una tabla con filtros por columna y fila expandible (equivalente a Bandeja Registro/Asignación), un formulario largo con secciones tipo card (equivalente al panel lateral "Datos"), y un set de tabs (equivalente a las sub-pestañas de Notificación). Estos 3 componentes de referencia se revisan con el usuario antes de construir el Dashboard (Fase 1); si el usuario aporta capturas de referencia en cualquier momento, se ajustan estos 3 componentes primero, en vez de rehacer cada módulo por separado.

---

## Anexo B: Patrones de bandeja, panel lateral y consola — mapeo V2 → web

Principio general: **no se rediseñan estos patrones desde cero**. Están validados operativamente con usuarios reales tras muchas iteraciones documentadas en `AGENTS.md` (correcciones puntuales sobre cuándo se abre/cierra el panel, qué dispara cada filtro, etc.); rediseñarlos arriesgaría reabrir esa negociación funcional y confundir a usuarios que ya conocen V2. Lo que cambia es solo la implementación técnica (Swing → React/MUI), no el comportamiento. Cada patrón se construye **una sola vez** como componente compartido y lo reutilizan todos los módulos — así la consistencia queda garantizada por construcción, no por disciplina de cada desarrollador.

### 1. Patrón "Bandeja" (Registro, Asignación, Análisis, Verificación, Ejecución, cada sub-pestaña de Notificación)

| V2 (Swing, ya validado en `CLAUDE.md`) | Equivalente web propuesto |
|---|---|
| Buscador compacto de 3 filas: texto libre + Buscar/Limpiar; fecha desde/hasta; combos + límite numérico | Componente único `BandejaSearchPanel` (MUI `TextField` + `Button`, 2x `DatePicker`, `Select` + `TextField` numérico), reutilizado en todas las bandejas, parametrizado solo en qué combos recibe |
| KPIs clicables (`MetricCardV2`): clic filtra por esa alerta/condición; clic en Buscar limpia el filtro KPI | Componente único `MetricCard` (MUI `Card`, número grande + label) en fila horizontal; el filtro activo por KPI se guarda en el estado de la página (o query param de la URL, ventaja nueva sobre V2: filtro compartible por link) |
| Filtros por columna debajo de cabecera, orden por cabecera con flechas, cabecera+filtros+cuerpo sincronizados en un solo scroll, sin scroll horizontal global | `MUI X Data Grid` con filtro por columna nativo, `sortable` headers, virtualización nativa (mejora sobre V2: no hay que optimizarla a mano) |
| Filas asociadas/duplicadas: icono `+` para expandir, franja de color a la izquierda (rota por grupo), fondo celeste suave, texto atenuado, sin checkbox propio | `getRowClassName` del Data Grid aplica la franja/fondo/atenuado condicionalmente; expansión vía `detailPanel` (Data Grid Pro) o fila hija sintética si se usa la edición Community — a confirmar en Fase 0 según licenciamiento |
| Doble clic en fila abre panel lateral (nunca clic simple) | `onRowDoubleClick` del Data Grid abre el `SidePanel` (ver patrón 2) |

### 2. Patrón "Panel lateral" (Datos, Asignación, Verificar, Validar, etc.)

| V2 (Swing, ya validado en `CLAUDE.md`) | Equivalente web propuesto |
|---|---|
| Oculto por defecto, se abre solo con doble clic | Componente único `SidePanel`: MUI `Drawer` `anchor="right"` `variant="persistent"` (no modal, no bloquea la grilla) |
| Lengüetas (`Datos` siempre + pestañas específicas del módulo); título genérico "Panel de datos" + titular en azul debajo | MUI `Tabs` dentro del `Drawer`; header fijo con el mismo texto genérico "Panel de datos" + titular |
| Botón X cierra sin limpiar la selección de la grilla | `IconButton` de cierre que solo colapsa el `Drawer`; la selección vive en el estado del padre, independiente del panel |
| Scroll vertical interno, nunca scroll horizontal | `overflow-y: auto; overflow-x: hidden` en el contenido del `Drawer`, ancho fijo (~420-480px) |
| Mini-paneles tipo stepper para flujos multi-paso (ej. "① Emisión ② Asignación" en Notificación), con footer fijo compartido para los botones de acción | `Card`s numeradas renderizadas condicionalmente según el estado del documento enfocado, más un `Box` con `position: sticky; bottom: 0` para el footer de acciones — mismo patrón, sin usar el componente `Stepper` lineal de MUI porque el flujo real no es estrictamente lineal (puede bloquearse/desbloquearse según reglas de negocio) |

### 3. Patrón "Consola de expediente" (`DlgConsolaExpedienteV2`)

| V2 (Swing) | Equivalente web propuesto | Nota |
|---|---|---|
| Diálogo modal (`JDialog`) con header ejecutivo, barra de etapas, pestañas, documentos, timeline, panel de resumen | Página propia con ruta `/expedientes/:id` (no modal) | **Mejora deliberada, no solo traducción**: en web conviene que la consola sea una URL navegable (bookmarkeable, compartible por link, funciona con atrás/adelante del navegador) — algo que un `JDialog` de escritorio no podía ofrecer. Se documenta aquí para que no se pierda como decisión al construirla (Fase 2 en adelante, cuando se necesite `Ver detalle` desde alguna bandeja) |
| Barra visual de etapas | MUI `Stepper` horizontal, `activeStep` = etapa actual, no clicable (informativo) | |
| Timeline/historial (`EXPEDIENTE_HISTORIAL`) | `Timeline` de `@mui/lab` | |
| Panel lateral de resumen | Reutiliza el mismo componente `SidePanel` del patrón 2, en modo resumen | Evita crear un cuarto patrón de panel distinto |

Estos 3 mapeos se validan junto con los 3 componentes de referencia de Fase 0 (sección "Cómo se valida esta propuesta" arriba), antes de construir el Dashboard.

## Nota

Este documento es solo el plan. No se ha creado ningún directorio, archivo de código ni script SQL como parte de su elaboración; V2 permanece intacto.
