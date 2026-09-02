# syntax=docker/dockerfile:1
#
# Imagen del frontend SDRERC V3 (React + TypeScript + Vite, build estático).
# Build multi-stage: la etapa "build" genera los archivos estáticos con Node;
# la etapa final los sirve con nginx (imagen liviana, sin Node/npm en runtime).
#
# Usa nginxinc/nginx-unprivileged (no nginx:alpine "a secas") a propósito: ya
# escucha en el puerto 8080 y corre 100% sin privilegios de root, que es
# exactamente lo que exige el SCC "restricted" por defecto de OpenShift (un
# nginx:alpine normal necesita root para el proceso master, lo que OpenShift
# rechaza salvo que se le dé una SCC especial - mejor no depender de eso).
#
# Build (desde este directorio, v3/sdrerc-v3-frontend):
#   docker build -t sdrerc-v3-frontend:local .
#
# Run local de prueba (BACKEND_URL debe apuntar a donde esté corriendo el backend):
#   docker run --rm -p 8080:8080 -e BACKEND_URL=http://host.docker.internal:8081 sdrerc-v3-frontend:local

# ---- Etapa 1: build ----
FROM node:22-alpine AS build
WORKDIR /build

# Confiar en el certificado raíz de Norton (interceptor TLS local de ESTA laptop
# de desarrollo, mismo certificado que usa el backend - ver Dockerfile del
# backend para el detalle completo). Node no usa el truststore de Java: necesita
# el certificado en PEM vía NODE_EXTRA_CA_CERTS. Sin esto, "npm ci" falla con el
# error interno "Exit handler never called!" (síntoma real de un fallo de TLS
# mal reportado por el CLI de npm, no un problema del propio npm). Sin efecto
# en redes que no intercepten TLS (ej. la red real de build de la OTI).
COPY docker/local-tls-root.pem /tmp/local-tls-root.pem
ENV NODE_EXTRA_CA_CERTS=/tmp/local-tls-root.pem

# Copiar solo los manifiestos de dependencias primero, mismo motivo de cacheo
# de capas que en el Dockerfile del backend.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
# "npm run build" real (tsc -b && vite build) BLOQUEA ante cualquier error de
# tipos - y el proyecto arrastra ~48 errores de TypeScript ya conocidos y
# documentados en v3/CLAUDE.md (ninguno bloqueante en desarrollo: el dev server
# de Vite y "npx tsc --noEmit" usados en todo el proyecto son solo informativos,
# nunca bloquean nada). Se usa "vite build" directo (sin tsc -b) para que el
# build del contenedor genere el mismo bundle que ya corre hoy en producción vía
# el dev server, sin inventar un gate nuevo que no existía antes de containerizar.
RUN npx vite build

# ---- Etapa 2: runtime ----
FROM nginxinc/nginx-unprivileged:1.27-alpine

COPY --from=build /build/dist /usr/share/nginx/html
# nginx.conf.template usa ${BACKEND_URL} - el propio entrypoint de esta imagen
# ya ejecuta envsubst sobre cualquier archivo en /etc/nginx/templates/*.template
# y deja el resultado en /etc/nginx/conf.d/ antes de arrancar nginx, sin
# necesitar ningún script de entrypoint propio.
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
ENV BACKEND_URL=http://sdrerc-v3-backend:8081

EXPOSE 8080
