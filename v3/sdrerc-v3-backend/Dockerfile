# syntax=docker/dockerfile:1
#
# Imagen del backend SDRERC V3 (Spring Boot 3.5 / Java 17).
# Build multi-stage: la etapa "build" compila el jar con Maven; la etapa final
# solo contiene el JRE + el jar, sin herramientas de build (imagen más chica y
# con menor superficie de ataque).
#
# Build (desde este directorio, v3/sdrerc-v3-backend):
#   docker build -t sdrerc-v3-backend:local .
#
# Run local de prueba (requiere Oracle alcanzable desde el contenedor):
#   docker run --rm -p 8081:8081 \
#     -e SDRERC_V3_DB_URL="jdbc:oracle:thin:@host.docker.internal:1521/XEPDB1" \
#     -e SDRERC_V3_DB_USER=sdrerc_app \
#     -e SDRERC_V3_DB_PASSWORD=*** \
#     -e SDRERC_V3_JWT_SECRET=*** \
#     sdrerc-v3-backend:local
#
# Variables de entorno soportadas: ver v3/sdrerc-v3-backend/src/main/resources/application.yml
# (todas con prefijo SDRERC_V3_*/SDRERC_APP_*, sin ningún valor secreto por defecto).

# ---- Etapa 1: build ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

# Confiar en el certificado raíz de Norton (interceptor TLS local de ESTA laptop
# de desarrollo, ya usado en el proyecto para el mismo problema con SMTP - ver
# scripts/client/smtp-root-cert.cer) SOLO en esta etapa de build, nunca en la
# imagen final: sin esto, Maven no puede validar el certificado real de Maven
# Central (PKIX path building failed) porque Norton reemplaza el certificado en
# tránsito. Es un no-op inofensivo en cualquier red que NO intercepte TLS (ej.
# la red real de build de la OTI) - importar un certificado que nunca aparece
# en ningún handshake no tiene ningún efecto.
COPY docker/local-tls-root.cer /tmp/local-tls-root.cer
RUN keytool -importcert -keystore "$JAVA_HOME/lib/security/cacerts" -trustcacerts \
    -alias local-tls-interceptor -file /tmp/local-tls-root.cer -storepass changeit -noprompt

# Copiar solo el pom.xml primero permite que Docker cachee la descarga de
# dependencias en una capa separada: mientras el pom.xml no cambie, un rebuild
# tras editar solo código Java no vuelve a descargar el árbol de dependencias.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Etapa 2: runtime ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Usuario no-root: requisito real de las políticas por defecto de OpenShift
# (SCC "restricted" no permite contenedores corriendo como root).
RUN groupadd --system sdrerc && useradd --system --gid sdrerc --create-home sdrerc
COPY --from=build /build/target/sdrerc-v3-backend.jar app.jar
RUN chown sdrerc:sdrerc app.jar
USER sdrerc

EXPOSE 8081

# Sin HEALTHCHECK de Docker a propósito: en Kubernetes/OpenShift el liveness/readiness
# probe se configura a nivel de Pod (ver deploy/openshift/backend-deployment.yaml),
# apuntando a GET /actuator/health (ya expuesto por Spring Boot Actuator, sin
# necesitar curl/wget instalados en la imagen).
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
