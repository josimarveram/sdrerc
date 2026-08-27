**INFORME DE ANÁLISIS**

**Análisis de cumplimiento de estándares OTI — Aplicativo web SDRERC V3**

**Elaborado para:** Dirección de Registros Civiles (DRC) / Sub Dirección de Recursos Registrales Civiles (SDRERC)

**Fecha:** [dd de mes de 2026]

**Alcance:** revisión de 19 de los 20 documentos normativos de OTI (directivas y normas administrativas internas) frente al estado actual del aplicativo SDRERC V3 (backend Spring Boot + frontend React), en migración desde SDRERC V2 (Java 8 Swing).

---

## 1. Objetivo

Identificar, antes de solicitar el pase a producción institucional de SDRERC V3, qué exigencias normativas de OTI ya cumplimos, cuáles presentan brechas que hay que subsanar, y qué información todavía no podemos verificar por falta de documentos. El objetivo es que el equipo de DRC tenga un panorama claro para priorizar el trabajo pendiente y anticiparse a las observaciones que normalmente haría un revisor de OTI o el DBA institucional.

---

## 2. Resumen ejecutivo

| Categoría | Estado | Prioridad |
|---|---|---|
| Autenticación delegada al Identity Server institucional | ❌ No cumple | Crítica |
| Gestión de secretos (Secret Manager) | ❌ No cumple | Crítica |
| Algoritmos y librería criptográfica exactos | ⚠️ Parcial | Alta |
| Logs de auditoría estructurados (NDJSON) | ❌ No cumple | Alta |
| Gobernanza formal de cambios en BD | ❌ No cumple | Alta |
| Gobernanza formal de desarrollo (SITD, actas, análisis de vulnerabilidad) | ❌ No cumple | Alta |
| Segregación de ambientes (V2/V3 comparten Oracle) | ❌ No cumple | Alta |
| CI/CD, SAST/DAST, secret-scanning, SBOM | ❌ No cumple | Media |
| Convenciones de nomenclatura de BD | ⚠️ No verificable (falta documento) | Media |
| Ofuscación de datos confidenciales en logs | ⚠️ Parcial | Media |
| Registro en catálogo institucional de aplicaciones | ❌ No cumple | Media |
| Prevención de SQL Injection (PreparedStatement) | ✅ Cumple | — |
| Hash de contraseñas (BCrypt) | ✅ Cumple | — |
| Doble factor de autenticación obligatorio | ✅ Cumple (mecanismo distinto al exigido) | — |
| Bloqueo por intentos fallidos, mensajes de error genéricos | ✅ Cumple | — |
| Separación de capas UI/Service/DAO | ✅ Cumple | — |
| Baja lógica en vez de DELETE físico | ✅ Cumple | — |

---

## 3. Lo que SÍ cumplimos hoy

### 3.1 Seguridad de código fuente

- Todos los DAOs (V2 y V3) usan `PreparedStatement` de forma consistente — sin concatenación de SQL, por lo tanto sin inyección SQL.
- No se guardan contraseñas en texto plano en ningún punto del código ni de la base de datos.
- Mensajes de error de login deliberadamente genéricos (nunca revelan si un usuario existe).
- Bloqueo temporal por intentos fallidos (5 intentos / 15 minutos), compartido entre fallos de contraseña y de código TOTP/respaldo.

### 3.2 Autenticación (parcial, ver brecha en sección 4.1)

- Segundo factor de autenticación **obligatorio para todos los usuarios**, más estricto que el mínimo exigido, aunque implementado con un mecanismo propio (TOTP + OTP por correo) en vez del mecanismo institucional.
- Hash de contraseñas con BCrypt, algoritmo dentro de la lista aceptada por el Estándar de Controles Criptográficos.
- Secreto TOTP cifrado antes de persistirse (nunca en claro).

### 3.3 Arquitectura y buenas prácticas

- Separación estricta de capas: la UI nunca contiene SQL; UI llama a Service, Service llama a DAO — alineado con el Estándar de Arquitectura.
- Baja lógica (`ACTIVO=0`) en lugar de eliminación física de datos, en prácticamente todo el esquema.
- Reglas de negocio documentadas y versionadas (`CLAUDE.md`/`AGENTS.md`), lo que facilita futuras auditorías funcionales.

---

## 4. Brechas identificadas

### 4.1 Autenticación no delegada al Identity Server institucional — CRÍTICA

El Estándar de Autenticación y Autorización exige que toda aplicación del RENIEC delegue la autenticación de servidores públicos a la Plataforma de Gestión de Identidades (Identity Server) vía OIDC/OAuth 2.0, y que la autenticación de ciudadanos priorice Passkeys/FIDO2/WebAuthn.

SDRERC V2 y V3 autentican contra su propia tabla `USUARIO` con contraseña + TOTP/OTP por correo, sin delegar a ningún proveedor de identidad institucional. Esta es la brecha estructural más importante: no se resuelve con un parche de código, requiere una decisión de arquitectura consensuada con OTI (posible plazo de adecuación para sistemas en migración).

### 4.2 Gestión de secretos — CRÍTICA

Las contraseñas de base de datos y demás secretos (`SDRERC_V3_DB_PASSWORD`, `SDRERC_V3_JWT_SECRET`, credenciales SMTP) se almacenan como variables de entorno de usuario de Windows en texto plano. El estándar exige un Secret Manager dedicado; nunca variables de entorno ni archivos de configuración.

### 4.3 Algoritmos y librería criptográfica — ALTA

- El estándar exige AES-256 en modo **CBC** con padding PKCS#7; V3 usa AES-**GCM** (más robusto técnicamente, pero no es el modo especificado).
- El estándar exige **Bouncy Castle** como librería criptográfica en Java; V3 usa el proveedor JCE estándar del JDK.

### 4.4 Logs de auditoría estructurados — ALTA

El Estándar de Logs de Auditoría exige un archivo NDJSON diario por aplicación, con campos obligatorios (UUID de transacción, ID de aplicación registrado en el catálogo institucional, acción funcional/transaccional, request/response), separado de los logs técnicos, con retención de 10 años.

SDRERC solo tiene logs técnicos (Log4j2). La tabla `AUDITORIA_EVENTO` existe en el esquema pero **no la usa ningún código**, ni en V2 ni en V3.

### 4.5 Gobernanza formal de cambios en base de datos — ALTA

NAI-025 exige que todo DML/DDL contra producción pase por un circuito formal: Solicitud de Requerimiento firmada digitalmente → ejecución exclusiva por un DBA designado → Acta de Pase a Producción, con enmascaramiento de datos sensibles en copias producción→desarrollo.

Los scripts SQL del proyecto (`db/sdrerc_app/scripts/`) se ejecutan hoy de forma ad-hoc, sin ese circuito. Es, junto con la nomenclatura de BD, el punto que con mayor probabilidad observará el DBA institucional.

### 4.6 Gobernanza formal de desarrollo de software — ALTA

DI-042 y NAI-029 exigen tramitar todo requerimiento de desarrollo mediante el Sistema Integrado de Trámite Documentario (SITD), con formularios firmados digitalmente (Acta de Lineamientos y Requisitos, Acta de Pruebas de Aceptación, Acta de Conformidad, entre otros) y un análisis de vulnerabilidad obligatorio antes de cualquier pase a producción (Anexo N° 13 de DI 064).

SDRERC V3 se desarrolla hoy fuera de ese circuito institucional.

### 4.7 Segregación de ambientes — ALTA

El Estándar de Desarrollo Seguro exige que los ambientes de desarrollo, pruebas y producción nunca compartan bases de datos ni secretos. SDRERC V2 (en producción) y V3 (en desarrollo) comparten el **mismo esquema Oracle** `SDRERC_APP`, lo cual contradice directamente esta regla.

### 4.8 CI/CD y controles de seguridad automatizados — MEDIA

No hay evidencia de integración continua, análisis estático/dinámico de código (SAST/DAST), escaneo de secretos, protección de ramas, commits firmados ni generación de SBOM — todos exigidos por el Estándar de Desarrollo Seguro.

### 4.9 Convenciones de nomenclatura de base de datos — NO VERIFICABLE

El "documento de Estándares de Base de Datos" es citado varias veces por NAI-025 y NAI-027 como referencia obligatoria para nombrar tablas y columnas, pero **no está entre los documentos con que contamos**. No podemos confirmar hoy si `EXPEDIENTE`, `USUARIO`, `PERSONA`, etc. (snake_case en español) cumplen la convención institucional. Este documento ya fue solicitado formalmente a OTI (ver memorando adjunto).

### 4.10 Ofuscación de datos confidenciales en logs — MEDIA

El Estándar de Tratamiento de Datos Confidenciales exige DNI/nombres/teléfono/correo ofuscados en logs. No se ha verificado si algún log de SDRERC imprime esta información sin ofuscar.

### 4.11 Registro en el catálogo institucional de aplicaciones — MEDIA

Varios documentos asumen que cada aplicativo tiene un ID y nombre asignados por un "Arquitecto de Aplicaciones" en un catálogo central (usado, entre otras cosas, como campo obligatorio del log de auditoría NDJSON). SDRERC V3 no está registrado en ese catálogo.

---

## 5. Documentos pendientes de solicitar a OTI

Además de las brechas de implementación, hay 7 documentos normativos referenciados por los estándares ya revisados que no obran en nuestro poder, y que son necesarios para completar esta verificación (solicitud formal ya cursada mediante memorando aparte):

1. Documento de "Estándares de Base de Datos"
2. Directiva DI-446-OPP "Metodología para la Gestión por Proyectos Institucionales"
3. "Estándar para la Construcción Segura de Aplicaciones Web"
4. Directiva DI-038-OTI/003 "Controles Tecnológicos de Seguridad de la Información del SGSI - RENIEC"
5. Política y Objetivos de Seguridad de la Información del RENIEC (RJ N° 000153-2023/JNAC/RENIEC)
6. "Guía de diseño web"
7. "Lineamientos para uso de logotipo y elementos de la identidad gráfica del RENIEC"

---

## 6. Priorización recomendada

**Antes de cualquier solicitud de pase a producción institucional**, se recomienda atender en este orden:

1. Definir con OTI la ruta de adecuación de autenticación (delegación al Identity Server) — es la brecha que más tiempo de coordinación institucional requiere, conviene plantearla cuanto antes.
2. Migrar secretos a Secret Manager — brecha técnica de esfuerzo contenido, alto impacto de seguridad.
3. Implementar el log de auditoría NDJSON usando la tabla `AUDITORIA_EVENTO` ya existente — esfuerzo moderado, requiere primero el ID de aplicación del catálogo institucional.
4. Iniciar el trámite formal de gobernanza de BD (NAI-025) para los próximos scripts, en paralelo a solicitar el documento de nomenclatura.
5. Evaluar con OTI un plan de segregación de ambientes (Oracle propio para V3) antes de escalar usuarios en paralelo con V2.
6. Cambiar AES-GCM por AES-CBC/PKCS#7 con Bouncy Castle — cambio de bajo riesgo funcional, alineación directa al estándar.
7. Formalizar el ingreso de SDRERC V3 al circuito SITD/DI-042/NAI-029 para futuros requerimientos.

---

## 7. Documentos normativos revisados

`NAI-025`, `DI-042`, `NAI-029`, `DI 061`, `EstandarArquitectura`, `EstandarAutenticacionAutorizacion`, `EstandarCodificacionSegura`, `EstandarControlesCriptograficos`, `EstandarDesarrolloSeguro`, `EstandarLogsAuditoria`, `EstandarTratamientoDatosConfidenciales`, `DI 064`, `DI-041`, `DI-049`, `DI 071`, `NAI-024`, `GP-45`, `NAI-027` (segunda versión). Pendiente de revisión: `7650158-modificatorias-reglamento-de-organizacion-y-funciones-rof` (estructura organizacional, baja probabilidad de contener requisitos técnicos adicionales).
