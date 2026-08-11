package com.sdrerc.v3.web;

import com.sdrerc.v3.application.AutenticacionService;
import com.sdrerc.v3.domain.AuthenticatedUser;
import com.sdrerc.v3.security.jwt.JwtService;
import com.sdrerc.v3.web.dto.ChallengeTokenRequest;
import com.sdrerc.v3.web.dto.ChangePasswordRequest;
import com.sdrerc.v3.web.dto.LoginChallengeResponse;
import com.sdrerc.v3.web.dto.LoginRequest;
import com.sdrerc.v3.web.dto.NextLoginStep;
import com.sdrerc.v3.web.dto.SessionResponse;
import com.sdrerc.v3.web.dto.TotpEnrollStartResponse;
import com.sdrerc.v3.web.dto.VerifyCodeRequest;
import jakarta.validation.Valid;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Puerta de entrada web del flujo de login/2FA de V2 (AutenticacionService), sin cambios de
 * lógica: credenciales -&gt; cambio de password obligatorio si aplica -&gt; segundo factor
 * (correo primero si hay correo cargado, TOTP como alternativa; enrolamiento la primera vez
 * si no tiene TOTP habilitado) -&gt; sesión. Ver plan de migración, sección 2 ("Autenticación/2FA
 * en contexto web") y AGENTS.md/CLAUDE.md ("Doble factor obligatorio para todos los usuarios,
 * sin excepción de rol"): por eso /login nunca devuelve un token de sesión directamente, siempre
 * un challengeToken que exige completar el paso de la sección "nextStep".
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticacionService autenticacionService;
    private final JwtService jwtService;

    public AuthController(AutenticacionService autenticacionService, JwtService jwtService) {
        this.autenticacionService = autenticacionService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginChallengeResponse login(@Valid @RequestBody LoginRequest request) throws SQLException {
        AutenticacionService.ResultadoLogin resultado =
                autenticacionService.iniciarLogin(request.username(), request.password());
        return construirChallengeResponse(
                resultado.getIdUsuario(), resultado.getUsername(), resultado.getNombreCompleto(),
                resultado.isDebeCambiarPassword(), resultado.isTieneCorreo(), resultado.getCorreo(),
                resultado.isTotpHabilitado());
    }

    @PostMapping("/change-password")
    public LoginChallengeResponse changePassword(@Valid @RequestBody ChangePasswordRequest request)
            throws SQLException {
        JwtService.LoginChallenge challenge = jwtService.parseChallengeToken(request.challengeToken());
        autenticacionService.cambiarPasswordObligatorio(challenge.idUsuario(), request.newPassword());
        // debeCambiarPassword ya quedo en false tras el cambio; el resto de datos del challenge
        // (correo/TOTP) no cambian con el password, se reutilizan tal cual (mismo criterio que
        // LoginFrameV2, que no vuelve a consultar BD para esto en medio del flujo).
        return construirChallengeResponse(
                challenge.idUsuario(), challenge.username(), null,
                false, challenge.tieneCorreo(), null, challenge.totpHabilitado());
    }

    @PostMapping("/2fa/email/send")
    public Map<String, Boolean> sendEmailCode(@Valid @RequestBody ChallengeTokenRequest request) throws SQLException {
        JwtService.LoginChallenge challenge = jwtService.parseChallengeToken(request.challengeToken());
        autenticacionService.enviarCodigoCorreo(challenge.idUsuario());
        return Map.of("sent", true);
    }

    @PostMapping("/2fa/email/verify")
    public SessionResponse verifyEmailCode(@Valid @RequestBody VerifyCodeRequest request) throws SQLException {
        JwtService.LoginChallenge challenge = jwtService.parseChallengeToken(request.challengeToken());
        autenticacionService.validarCodigoCorreo(challenge.idUsuario(), request.code());
        return completarSesion(challenge.idUsuario(), null);
    }

    @PostMapping("/2fa/totp/verify")
    public SessionResponse verifyTotpCode(@Valid @RequestBody VerifyCodeRequest request) throws SQLException {
        JwtService.LoginChallenge challenge = jwtService.parseChallengeToken(request.challengeToken());
        autenticacionService.validarCodigoTotp(challenge.idUsuario(), request.code());
        return completarSesion(challenge.idUsuario(), null);
    }

    @PostMapping("/2fa/totp/enroll/start")
    public TotpEnrollStartResponse startTotpEnrollment(@Valid @RequestBody ChallengeTokenRequest request)
            throws SQLException {
        JwtService.LoginChallenge challenge = jwtService.parseChallengeToken(request.challengeToken());
        AutenticacionService.ResultadoEnrolamientoTotp resultado =
                autenticacionService.iniciarEnrolamientoTotp(challenge.idUsuario(), challenge.username());
        return new TotpEnrollStartResponse(resultado.getSecretoBase32(), resultado.getUriEnrolamiento());
    }

    @PostMapping("/2fa/totp/enroll/confirm")
    public SessionResponse confirmTotpEnrollment(@Valid @RequestBody VerifyCodeRequest request) throws SQLException {
        JwtService.LoginChallenge challenge = jwtService.parseChallengeToken(request.challengeToken());
        List<String> backupCodes = autenticacionService.confirmarEnrolamientoTotp(challenge.idUsuario(), request.code());
        return completarSesion(challenge.idUsuario(), backupCodes);
    }

    private SessionResponse completarSesion(Long idUsuario, List<String> backupCodesOEnrolamiento) throws SQLException {
        AuthenticatedUser user = autenticacionService.completarLogin(idUsuario);
        String accessToken = jwtService.issueSessionToken(user);
        return new SessionResponse(accessToken, user.username(), user.nombreCompleto(), user.roles(),
                backupCodesOEnrolamiento);
    }

    private LoginChallengeResponse construirChallengeResponse(Long idUsuario, String username, String nombreCompleto,
            boolean debeCambiarPassword, boolean tieneCorreo, String correoSinEnmascarar, boolean totpHabilitado) {
        String correoEnmascarado = tieneCorreo ? AutenticacionService.enmascararCorreo(correoSinEnmascarar) : null;
        NextLoginStep nextStep = resolverNextStep(debeCambiarPassword, tieneCorreo, totpHabilitado);
        String challengeToken = jwtService.issueChallengeToken(
                idUsuario, username, tieneCorreo, correoEnmascarado, totpHabilitado);
        return new LoginChallengeResponse(challengeToken, nextStep, username, nombreCompleto,
                tieneCorreo, correoEnmascarado, totpHabilitado);
    }

    /**
     * Replica exacta de {@code LoginFrameV2.enrutarSegundoFactor()} (V2): cambio de password
     * obligatorio primero si aplica; luego correo si el usuario tiene {@code USUARIO.correo}
     * cargado (primera opción); si no, TOTP (verificación si ya está habilitado, enrolamiento la
     * primera vez). El usuario puede igual cambiar de EMAIL a TOTP desde el frontend sin volver a
     * pedir credenciales, llamando directo a {@code /2fa/totp/*} con el mismo challengeToken
     * (equivalente al enlace "Prefiero usar una app autenticadora" de V2).
     */
    private NextLoginStep resolverNextStep(boolean debeCambiarPassword, boolean tieneCorreo, boolean totpHabilitado) {
        if (debeCambiarPassword) {
            return NextLoginStep.CAMBIO_PASSWORD;
        }
        if (tieneCorreo) {
            return NextLoginStep.EMAIL;
        }
        return totpHabilitado ? NextLoginStep.TOTP : NextLoginStep.TOTP_ENROLL;
    }
}
