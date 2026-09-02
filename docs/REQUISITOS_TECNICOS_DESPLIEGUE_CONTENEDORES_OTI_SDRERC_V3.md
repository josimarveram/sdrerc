**INFORME TÉCNICO**

**Requisitos técnicos para el despliegue en contenedores del aplicativo web SDRERC V3**

**Elaborado para:** Unidad de Ingeniería de Software (UIS) — Oficina de Tecnologías de la Información (OTI)

**Elaborado por:** Sub Dirección de Recursos Registrales Civiles (SDRERC) / Dirección de Registros Civiles (DRC)

**Fecha:** [dd de mes de 2026]

**Objetivo del documento:** listar los requisitos técnicos concretos para desplegar SDRERC V3 en el entorno de desarrollo/contenedores de la OTI, de modo que la UIS pueda verificar cuáles ya están disponibles en dicho entorno y cuáles requieren instalación, configuración o autorización adicional antes del primer despliegue.

---

## 1. Resumen de la arquitectura

SDRERC V3 es una aplicación web compuesta por **2 componentes independientes, cada uno empaquetado como su propia imagen de contenedor**:

| Componente | Tecnología | Rol |
|---|---|---|
| Backend | Java 17, Spring Boot 3.5 (API REST) | Lógica de negocio, acceso a Oracle, autenticación |
| Frontend | React 19 + TypeScript, servido por nginx | Interfaz de usuario, sirve archivos estáticos y reenvía `/api/*` al backend |

Ambos componentes son **stateless** (sin sesión en memoria, autenticación vía JWT) y **sin dependencias del sistema operativo del nodo** — toda la lógica y sus dependencias (JRE, librerías) viven dentro de la propia imagen de contenedor. La base de datos (Oracle) es un servicio externo al que el backend se conecta vía JDBC; no forma parte de los contenedores de la aplicación.

**Alcance de esta primera etapa**: arquitectura de 2 servicios (backend + frontend) containerizados de forma independiente, no una descomposición en microservicios — decisión explicada en el punto 5.

---

## 2. Checklist de requisitos técnicos

La columna "¿Disponible?" queda para que la UIS/OTI la complete según el estado real del entorno de desarrollo.

### 2.1 Plataforma de contenedores

| # | Requisito | Detalle | ¿Disponible? |
|---|---|---|---|
| 1 | Clúster Kubernetes/OpenShift | Con permiso para crear `Deployment`, `Service`, `Route` (o `Ingress`), `Secret` y `ConfigMap` en un namespace propio del proyecto | |
| 2 | Registro de imágenes de contenedor | Acceso de *push/pull* (ej. Quay u otro registry interno) para publicar y descargar las 2 imágenes (backend, frontend) | |
| 3 | Acceso a imágenes base públicas | `eclipse-temurin:17-jre-jammy`, `maven:3.9.9-eclipse-temurin-17`, `node:22-alpine`, `nginxinc/nginx-unprivileged:1.27-alpine` (Docker Hub). Si la red institucional no permite salida directa a Docker Hub, deben *mirrorearse* al registry interno antes del primer build | |
| 4 | Pipeline de build (deseable, no bloqueante) | Tekton/OpenShift Pipelines + GitOps (ArgoCD), o alternativamente `oc apply`/`oc new-app` manual como primer paso | |
| 5 | Recursos por Pod | Backend: 250m–1 CPU / 512Mi–1Gi RAM. Frontend: 50m–250m CPU / 64Mi–128Mi RAM (valores de referencia, ajustables) | |
| 6 | Almacenamiento persistente | **No se requiere ninguno** — la aplicación es *stateless*; los logs se emiten a `stdout`, estándar de contenedores, sin archivos locales que persistir | No aplica |

### 2.2 Base de datos

| # | Requisito | Detalle | ¿Disponible? |
|---|---|---|---|
| 7 | Oracle Database | Versión 19c o superior, ya provisionada y alcanzable en red desde el clúster (host, puerto, nombre de servicio/PDB) | |
| 8 | Usuario/esquema dedicado | Usuario Oracle con permisos sobre el esquema `SDRERC_APP` (o el que se defina) | |
| 9 | Conectividad de red | Los Pods del backend deben poder alcanzar Oracle por el puerto 1521 (o el que corresponda) — regla de firewall/`NetworkPolicy` si el clúster está segmentado de la red donde vive Oracle | |

### 2.3 Secretos y configuración

| # | Requisito | Detalle | ¿Disponible? |
|---|---|---|---|
| 10 | Gestor de secretos | `Secret` nativo de Kubernetes es suficiente (o el gestor institucional, si ya existe uno) — variables necesarias: credenciales de base de datos, clave de firma JWT, credenciales SMTP (opcional, ver punto 11) | |
| 11 | Servidor SMTP institucional (opcional) | Solo necesario si se desea que el segundo factor de autenticación (2FA) por correo electrónico use un relay SMTP institucional real. Sin este dato, el 2FA sigue funcionando completo vía aplicación autenticadora (TOTP), sin ningún bloqueo | |

### 2.4 Explícitamente NO requerido

- Java, Node.js ni Maven instalados en los nodos del clúster — todo vive dentro de las imágenes de contenedor.
- Cliente Oracle (Oracle Client) — el backend se conecta vía JDBC puro (driver embebido en la propia imagen).
- GPU o cualquier hardware especializado.
- Almacenamiento persistente (volúmenes) de ningún tipo.

---

## 3. Variables de entorno esperadas por el backend

Referencia técnica para quien configure el `Secret`/`ConfigMap` del backend en el clúster:

| Variable | Obligatoria | Descripción |
|---|---|---|
| `SDRERC_V3_DB_URL` | Sí | Cadena de conexión JDBC a Oracle (ej. `jdbc:oracle:thin:@host:1521/servicio`) |
| `SDRERC_V3_DB_USER` | Sí | Usuario de base de datos |
| `SDRERC_V3_DB_PASSWORD` | Sí | Contraseña de base de datos |
| `SDRERC_V3_JWT_SECRET` | Sí | Clave de firma de los tokens de sesión (JWT) |
| `SDRERC_APP_TOTP_KEY` | No | Clave de cifrado del secreto TOTP (2FA por app autenticadora) |
| `SDRERC_APP_SMTP_HOST` / `_PORT` / `_USER` / `_PASSWORD` / `_FROM` | No | Datos del relay SMTP, solo si se habilita 2FA por correo |

El backend expone `GET /actuator/health` (respuesta `{"status":"UP"}`) — reutilizable directamente como *liveness* y *readiness probe* de Kubernetes, sin necesitar código adicional.

---

## 4. Verificación ya realizada por el equipo de SDRERC

Antes de remitir este documento, el equipo de SDRERC construyó y verificó ambas imágenes de contenedor en un entorno local (build completo, arranque de ambos servicios, conexión real a una instancia Oracle de desarrollo, y prueba de comunicación entre frontend y backend a través del proxy interno de nginx). El resultado fue exitoso — se dispone de los `Dockerfile` de ambos componentes, un `docker-compose.yml` de referencia para pruebas locales, y manifiestos de ejemplo de Kubernetes/OpenShift (`Deployment`, `Service`, `Route`, `Secret`) listos para adaptarse a las convenciones reales del entorno de desarrollo institucional (nombre de namespace, registry interno, límites de recursos definitivos).

---

## 5. Nota sobre arquitectura de microservicios

Los estándares de arquitectura de la OTI recomiendan una descomposición funcional en servicios independientes. SDRERC V3 hoy comparte una única base de datos Oracle con transacciones de negocio que cruzan varios módulos del flujo de expedientes (registro, asignación, análisis, verificación, ejecución, notificación) — partir esa lógica en microservicios independientes exigiría rediseñar esas transacciones y agregar comunicación entre servicios, con riesgo real de afectar reglas de negocio ya validadas en producción interna.

Como primer paso, alineado con el espíritu del estándar (arquitectura por servicios, sin acoplamiento a un único proceso o máquina) sin ese riesgo, se optó por un **monolito modular containerizado en 2 imágenes independientes** (backend y frontend), cada una con su propio ciclo de vida de despliegue. Quedamos atentos a cualquier observación de la UIS al respecto para evaluar los siguientes pasos.

---

Quedamos atentos a la validación de este listado y a cualquier requisito adicional que la UIS/OTI considere necesario antes de proceder con el primer despliegue de prueba.

Atentamente,


_______________________________

[Nombre y cargo del remitente]

Sub Dirección de Recursos Registrales Civiles

Dirección de Registros Civiles
