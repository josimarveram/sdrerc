package com.sdrerc.v3.security.jwt;

import com.sdrerc.v3.domain.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Emite y valida JWT propios de V3. No existe equivalente en V2 (una app de escritorio no
 * necesita sesion HTTP); reemplaza al singleton estatico {@code SessionContext} de V2 por una
 * identidad resuelta por request (ver plan de migracion, seccion 2 y riesgo 1).
 *
 * <p>Dos tipos de token, distinguidos por el claim {@code purpose}:</p>
 * <ul>
 *   <li>{@code LOGIN_CHALLENGE}: emitido tras validar usuario+password, antes de completar el
 *   2FA. Vida corta (5 min por defecto). Solo sirve contra los endpoints de
 *   {@code /api/auth/2fa/*} y {@code /api/auth/change-password} - no es un token de sesion.</li>
 *   <li>{@code SESSION}: emitido tras completar el 2FA exitosamente ({@code AutenticacionService
 *   .completarLogin}). Vida corta (30 min por defecto, ver plan seccion 2 punto 3), sin refresh
 *   token todavia (pendiente, no bloqueante para la Fase 0).</li>
 * </ul>
 */
@Component
public class JwtService {

    private static final String CLAIM_PURPOSE = "purpose";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_NOMBRE = "nombreCompleto";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TIENE_CORREO = "tieneCorreo";
    private static final String CLAIM_CORREO_ENMASCARADO = "correoEnmascarado";
    private static final String CLAIM_TOTP_HABILITADO = "totpHabilitado";
    private static final String PURPOSE_CHALLENGE = "LOGIN_CHALLENGE";
    private static final String PURPOSE_SESSION = "SESSION";

    private final SecretKey signingKey;
    private final Duration challengeTtl;
    private final Duration sessionTtl;

    public JwtService(
            @Value("${sdrerc.v3.jwt.secret:}") String secret,
            @Value("${sdrerc.v3.jwt.challenge-ttl-minutes:5}") long challengeTtlMinutes,
            @Value("${sdrerc.v3.jwt.session-ttl-minutes:30}") long sessionTtlMinutes) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Debe configurar sdrerc.v3.jwt.secret (o la variable de entorno "
                            + "SDRERC_V3_JWT_SECRET) para firmar los JWT de sesion. Nunca hardcodeada en el fuente.");
        }
        this.signingKey = Keys.hmacShaKeyFor(derivarClave256(secret.trim()));
        this.challengeTtl = Duration.ofMinutes(challengeTtlMinutes);
        this.sessionTtl = Duration.ofMinutes(sessionTtlMinutes);
    }

    /**
     * {@code tieneCorreo}/{@code correoEnmascarado}/{@code totpHabilitado} se congelan en el
     * propio token en el momento del login (igual que V2, que los mantiene en memoria en
     * {@code LoginFrameV2} durante todo el flujo sin volver a leerlos de BD hasta
     * {@code completarLogin}). Evita una consulta adicional en cada paso del 2FA y mantiene el
     * backend sin estado de sesion propio.
     */
    public String issueChallengeToken(Long idUsuario, String username, boolean tieneCorreo,
            String correoEnmascarado, boolean totpHabilitado) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(idUsuario))
                .claim(CLAIM_PURPOSE, PURPOSE_CHALLENGE)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_TIENE_CORREO, tieneCorreo)
                .claim(CLAIM_CORREO_ENMASCARADO, correoEnmascarado)
                .claim(CLAIM_TOTP_HABILITADO, totpHabilitado)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(challengeTtl)))
                .signWith(signingKey)
                .compact();
    }

    public String issueSessionToken(AuthenticatedUser user) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.idUsuario()))
                .claim(CLAIM_PURPOSE, PURPOSE_SESSION)
                .claim(CLAIM_USERNAME, user.username())
                .claim(CLAIM_NOMBRE, user.nombreCompleto())
                .claim(CLAIM_ROLES, user.roles())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(sessionTtl)))
                .signWith(signingKey)
                .compact();
    }

    public LoginChallenge parseChallengeToken(String token) {
        Claims claims = parseClaims(token);
        requirePurpose(claims, PURPOSE_CHALLENGE, "El token no corresponde a un desafío de login vigente.");
        return new LoginChallenge(
                Long.valueOf(claims.getSubject()),
                claims.get(CLAIM_USERNAME, String.class),
                Boolean.TRUE.equals(claims.get(CLAIM_TIENE_CORREO, Boolean.class)),
                claims.get(CLAIM_CORREO_ENMASCARADO, String.class),
                Boolean.TRUE.equals(claims.get(CLAIM_TOTP_HABILITADO, Boolean.class)));
    }

    @SuppressWarnings("unchecked")
    public AuthenticatedUser parseSessionToken(String token) {
        Claims claims = parseClaims(token);
        requirePurpose(claims, PURPOSE_SESSION, "El token no corresponde a una sesión vigente.");
        List<String> roles = claims.get(CLAIM_ROLES, List.class);
        return new AuthenticatedUser(
                Long.valueOf(claims.getSubject()),
                claims.get(CLAIM_USERNAME, String.class),
                claims.get(CLAIM_NOMBRE, String.class),
                roles == null ? List.of() : roles);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Token inválido o vencido.", ex);
        }
    }

    private void requirePurpose(Claims claims, String purposeEsperado, String mensajeError) {
        String purpose = claims.get(CLAIM_PURPOSE, String.class);
        if (!purposeEsperado.equals(purpose)) {
            throw new InvalidTokenException(mensajeError);
        }
    }

    /**
     * Deriva una clave AES/HMAC de 256 bits desde cualquier passphrase configurada, mismo patron
     * defensivo que {@code TotpSecretCipher} (para no exigir un formato exacto de clave al
     * administrador).
     */
    private static byte[] derivarClave256(String passphrase) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(passphrase.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se pudo derivar la clave de firma JWT.", ex);
        }
    }

    public record LoginChallenge(Long idUsuario, String username, boolean tieneCorreo,
            String correoEnmascarado, boolean totpHabilitado) {
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }

        public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
