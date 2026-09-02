package com.sdrerc.v3.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT propio para la integración con OnlyOffice Document Server (ver `v3/CLAUDE.md`, sección
 * Plantillas — edición en línea). Secreto independiente de {@link JwtService} (sesión de la app):
 * este mismo valor se configura también como variable de entorno {@code JWT_SECRET} del
 * contenedor OnlyOffice, para que ambos lados firmen/verifiquen con la misma clave.
 *
 * <p>3 usos distintos, todos con el mismo secreto pero payloads/audiencia diferentes:</p>
 * <ul>
 *   <li>{@link #firmarConfigEditor}: firma el objeto de configuración completo que el frontend le
 *   pasa a {@code DocsAPI.DocEditor} — lo verifica el propio Document Server al abrir el editor,
 *   protocolo estándar de OnlyOffice (no inventado acá).</li>
 *   <li>{@link #emitirTokenDescarga}/{@link #validarTokenDescarga}: token propio (no de OnlyOffice)
 *   embebido en la URL que el contenedor usa para descargar el `.docx` — reemplaza al JWT de sesión
 *   normal, porque quien llama esa URL es el contenedor Document Server, no el navegador con una
 *   sesión de usuario.</li>
 *   <li>{@link #validarCallback}: verifica el JWT que el propio Document Server envía en el
 *   callback de guardado (header {@code Authorization} o campo {@code token} del body, según
 *   configuración — ver `OnlyOfficeAdminController`).</li>
 * </ul>
 */
@Component
public class OnlyOfficeJwtService {

    private final SecretKey signingKey;
    private final Duration descargaTtl;
    private final boolean configurado;

    public OnlyOfficeJwtService(
            @Value("${sdrerc.v3.onlyoffice.jwt-secret:}") String secret,
            @Value("${sdrerc.v3.onlyoffice.document-download-token-ttl-minutes:10}") long descargaTtlMinutos) {
        this.configurado = secret != null && !secret.trim().isEmpty();
        // A diferencia de JwtService (JWT propio de la app, firmado y verificado solo por
        // nosotros mismos, donde pre-hashear el secreto con SHA-256 es una defensa razonable),
        // acá el OTRO LADO es el Document Server de OnlyOffice (Node.js, librería `jsonwebtoken`),
        // que usa el secreto tal cual — en bruto — como clave HMAC, sin ningún pre-procesamiento.
        // Pre-hashearlo acá (como se hizo en un primer intento) produce una clave de firma
        // totalmente distinta a la que espera OnlyOffice, aunque el texto del secreto sea
        // idéntico en ambos lados — causa real del error "El 'token' de seguridad del documento
        // tiene un formato incorrecto" (27/08/2026, diagnosticado comparando por hash que el
        // secreto en sí coincidía byte a byte entre el contenedor y esta app antes de encontrar
        // el problema real). El secreto (40 caracteres alfanuméricos) ya supera los 32 bytes
        // mínimos que exige HMAC-SHA256, así que no hace falta derivar/rellenar nada.
        this.signingKey = configurado ? Keys.hmacShaKeyFor(secret.trim().getBytes(StandardCharsets.UTF_8)) : null;
        this.descargaTtl = Duration.ofMinutes(descargaTtlMinutos);
    }

    public boolean estaConfigurado() {
        return configurado;
    }

    private void exigirConfigurado() {
        if (!configurado) {
            throw new IllegalStateException(
                    "Edición en línea de plantillas no configurada: falta sdrerc.v3.onlyoffice.jwt-secret "
                            + "(o la variable de entorno SDRERC_V3_ONLYOFFICE_JWT_SECRET).");
        }
    }

    /** Firma el objeto de config completo, tal como exige el protocolo de OnlyOffice (claim {@code payload}). */
    public String firmarConfigEditor(Map<String, Object> config) {
        exigirConfigurado();
        return Jwts.builder().claims(config).signWith(signingKey).compact();
    }

    public String emitirTokenDescarga(Long idPlantillaDocumento) {
        exigirConfigurado();
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(idPlantillaDocumento))
                .claim("purpose", "ONLYOFFICE_DESCARGA")
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(descargaTtl)))
                .signWith(signingKey)
                .compact();
    }

    public Long validarTokenDescarga(String token) {
        exigirConfigurado();
        Claims claims = parseClaims(token);
        if (!"ONLYOFFICE_DESCARGA".equals(claims.get("purpose", String.class))) {
            throw new InvalidOnlyOfficeTokenException("Token de descarga inválido.");
        }
        return Long.valueOf(claims.getSubject());
    }

    /** Verifica el JWT que OnlyOffice adjunta al callback de guardado — no valida contenido, solo firma/vigencia. */
    public void validarCallback(String token) {
        exigirConfigurado();
        parseClaims(token);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidOnlyOfficeTokenException("Token de OnlyOffice inválido o vencido.", ex);
        }
    }

    public static class InvalidOnlyOfficeTokenException extends RuntimeException {
        public InvalidOnlyOfficeTokenException(String message) {
            super(message);
        }

        public InvalidOnlyOfficeTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
