package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.UsuarioAutenticacionDTO;
import com.sdrerc.domain.model.User;
import com.sdrerc.infrastructure.security.PasswordEncoder;
import com.sdrerc.infrastructure.security.TotpSecretCipher;
import com.sdrerc.infrastructure.security.TotpService;
import com.sdrerc.infrastructure.sdrercapp.dao.UsuarioDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.UsuarioTotpBackupCodeDAO;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Orquesta el flujo completo de login premium con 2FA: credenciales -&gt; cambio de contraseña
 * obligatorio (si aplica) -&gt; enrolamiento o verificación TOTP -&gt; sesión. No contiene SQL propio,
 * delega en {@link UsuarioDAO} / {@link UsuarioTotpBackupCodeDAO}.
 *
 * <p>Mensajes de error deliberadamente genéricos: nunca revelan si un username existe o no
 * (mitigación estándar contra enumeración de cuentas). El mismo contador de bloqueo aplica tanto
 * a fallos de contraseña como de código TOTP/respaldo, porque un código de 6 dígitos es
 * fuerza-bruteable en pocos miles de intentos si no se limita.</p>
 */
public class AutenticacionService {

    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";
    private static final String MENSAJE_CODIGO_INVALIDO = "Código incorrecto o vencido.";
    private static final String TOTP_ISSUER = "SDRERC";
    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final int MINUTOS_BLOQUEO = 15;
    private static final int MIN_LONGITUD_PASSWORD = 8;
    private static final int CANTIDAD_BACKUP_CODES = 8;
    private static final String ALFABETO_BACKUP_CODE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final UsuarioDAO usuarioDAO;
    private final UsuarioTotpBackupCodeDAO backupCodeDAO;

    public AutenticacionService() {
        this(new UsuarioDAO(), new UsuarioTotpBackupCodeDAO());
    }

    public AutenticacionService(UsuarioDAO usuarioDAO, UsuarioTotpBackupCodeDAO backupCodeDAO) {
        this.usuarioDAO = usuarioDAO;
        this.backupCodeDAO = backupCodeDAO;
    }

    public ResultadoLogin iniciarLogin(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException(MENSAJE_CREDENCIALES_INVALIDAS);
        }
        UsuarioAutenticacionDTO usuario = usuarioDAO.buscarPorUsername(username.trim());
        if (usuario == null || !usuario.isActivo()) {
            throw new IllegalArgumentException(MENSAJE_CREDENCIALES_INVALIDAS);
        }
        validarNoBloqueado(usuario);
        if (usuario.getPasswordHash() == null || !PasswordEncoder.matches(password, usuario.getPasswordHash())) {
            usuarioDAO.registrarIntentoFallido(usuario.getIdUsuario(), MAX_INTENTOS_FALLIDOS, MINUTOS_BLOQUEO);
            throw new IllegalArgumentException(MENSAJE_CREDENCIALES_INVALIDAS);
        }
        return new ResultadoLogin(
                usuario.getIdUsuario(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.isDebeCambiarPassword(),
                usuario.isTotpHabilitado());
    }

    public void cambiarPasswordObligatorio(Long idUsuario, String nuevaPassword) throws SQLException {
        if (idUsuario == null) {
            throw new IllegalStateException("Sesión de login inválida.");
        }
        validarComplejidadPassword(nuevaPassword);
        UsuarioAutenticacionDTO usuario = usuarioDAO.obtenerAutenticacionPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalStateException("Sesión de login inválida.");
        }
        if (!usuario.isTotpHabilitado()) {
            TotpSecretCipher.requerirConfiguracion();
        }
        String hash = PasswordEncoder.hash(nuevaPassword.trim());
        usuarioDAO.actualizarPasswordHash(idUsuario, hash, false, idUsuario);
    }

    public ResultadoEnrolamientoTotp iniciarEnrolamientoTotp(Long idUsuario, String username) throws SQLException {
        if (idUsuario == null) {
            throw new IllegalStateException("Sesión de login inválida.");
        }
        TotpSecretCipher.requerirConfiguracion();
        String secretoBase32 = TotpService.generarSecreto();
        String cifrado = TotpSecretCipher.cifrar(secretoBase32);
        usuarioDAO.actualizarTotp(idUsuario, cifrado, false);
        String uri = TotpService.construirUriEnrolamiento(secretoBase32, username, TOTP_ISSUER);
        return new ResultadoEnrolamientoTotp(secretoBase32, uri);
    }

    public List<String> confirmarEnrolamientoTotp(Long idUsuario, String codigoIngresado) throws SQLException {
        if (idUsuario == null) {
            throw new IllegalStateException("Sesión de login inválida.");
        }
        UsuarioAutenticacionDTO usuario = usuarioDAO.obtenerAutenticacionPorId(idUsuario);
        if (usuario == null || usuario.getTotpSecretCifrado() == null) {
            throw new IllegalStateException("No hay un enrolamiento de verificación en dos pasos pendiente.");
        }
        String secretoBase32 = TotpSecretCipher.descifrar(usuario.getTotpSecretCifrado());
        if (!TotpService.validarCodigo(secretoBase32, codigoIngresado)) {
            usuarioDAO.registrarIntentoFallido(idUsuario, MAX_INTENTOS_FALLIDOS, MINUTOS_BLOQUEO);
            throw new IllegalArgumentException(MENSAJE_CODIGO_INVALIDO);
        }
        usuarioDAO.actualizarTotp(idUsuario, usuario.getTotpSecretCifrado(), true);

        List<String> backupCodes = generarBackupCodes();
        backupCodeDAO.generarYGuardarLote(idUsuario, backupCodes);
        return backupCodes;
    }

    public void validarCodigoTotp(Long idUsuario, String codigoIngresado) throws SQLException {
        if (idUsuario == null) {
            throw new IllegalStateException("Sesión de login inválida.");
        }
        UsuarioAutenticacionDTO usuario = usuarioDAO.obtenerAutenticacionPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalStateException("Sesión de login inválida.");
        }
        validarNoBloqueado(usuario);
        if (usuario.isTotpHabilitado() && usuario.getTotpSecretCifrado() != null) {
            String secretoBase32 = TotpSecretCipher.descifrar(usuario.getTotpSecretCifrado());
            if (TotpService.validarCodigo(secretoBase32, codigoIngresado)) {
                return;
            }
        }
        if (backupCodeDAO.consumirSiValido(idUsuario, codigoIngresado)) {
            return;
        }
        usuarioDAO.registrarIntentoFallido(idUsuario, MAX_INTENTOS_FALLIDOS, MINUTOS_BLOQUEO);
        throw new IllegalArgumentException(MENSAJE_CODIGO_INVALIDO);
    }

    public User completarLogin(Long idUsuario) throws SQLException {
        if (idUsuario == null) {
            throw new IllegalStateException("Sesión de login inválida.");
        }
        usuarioDAO.resetearIntentosFallidos(idUsuario);
        usuarioDAO.registrarUltimoLogin(idUsuario);

        UsuarioAutenticacionDTO usuario = usuarioDAO.obtenerAutenticacionPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalStateException("Sesión de login inválida.");
        }
        Set<String> codigosRol = usuarioDAO.listarCodigosRolPorUsuario(idUsuario);

        User user = new User(usuario.getIdUsuario(), usuario.getUsername(), usuario.getNombreCompleto(), "ACTIVO");
        user.setRoles(new ArrayList<>(codigosRol));
        return user;
    }

    private void validarNoBloqueado(UsuarioAutenticacionDTO usuario) {
        LocalDateTime bloqueadoHasta = usuario.getBloqueadoHasta();
        if (bloqueadoHasta != null && bloqueadoHasta.isAfter(LocalDateTime.now())) {
            long minutosRestantes = Math.max(1, ChronoUnit.MINUTES.between(LocalDateTime.now(), bloqueadoHasta) + 1);
            throw new IllegalArgumentException(
                    "Cuenta bloqueada temporalmente por intentos fallidos. Intente nuevamente en "
                            + minutosRestantes + " minuto(s).");
        }
    }

    private void validarComplejidadPassword(String password) {
        if (password == null || password.trim().length() < MIN_LONGITUD_PASSWORD) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos " + MIN_LONGITUD_PASSWORD + " caracteres.");
        }
        String valor = password.trim();
        boolean tieneLetra = valor.chars().anyMatch(Character::isLetter);
        boolean tieneDigito = valor.chars().anyMatch(Character::isDigit);
        if (!tieneLetra || !tieneDigito) {
            throw new IllegalArgumentException("La contraseña debe combinar letras y números.");
        }
    }

    private List<String> generarBackupCodes() {
        SecureRandom random = new SecureRandom();
        List<String> codigos = new ArrayList<>(CANTIDAD_BACKUP_CODES);
        for (int i = 0; i < CANTIDAD_BACKUP_CODES; i++) {
            StringBuilder sb = new StringBuilder(9);
            for (int j = 0; j < 8; j++) {
                if (j == 4) {
                    sb.append('-');
                }
                sb.append(ALFABETO_BACKUP_CODE.charAt(random.nextInt(ALFABETO_BACKUP_CODE.length())));
            }
            codigos.add(sb.toString());
        }
        return codigos;
    }

    public static final class ResultadoLogin {
        private final Long idUsuario;
        private final String username;
        private final String nombreCompleto;
        private final boolean debeCambiarPassword;
        private final boolean totpHabilitado;

        public ResultadoLogin(Long idUsuario, String username, String nombreCompleto,
                boolean debeCambiarPassword, boolean totpHabilitado) {
            this.idUsuario = idUsuario;
            this.username = username;
            this.nombreCompleto = nombreCompleto;
            this.debeCambiarPassword = debeCambiarPassword;
            this.totpHabilitado = totpHabilitado;
        }

        public Long getIdUsuario() {
            return idUsuario;
        }

        public String getUsername() {
            return username;
        }

        public String getNombreCompleto() {
            return nombreCompleto;
        }

        public boolean isDebeCambiarPassword() {
            return debeCambiarPassword;
        }

        public boolean isTotpHabilitado() {
            return totpHabilitado;
        }
    }

    public static final class ResultadoEnrolamientoTotp {
        private final String secretoBase32;
        private final String uriEnrolamiento;

        public ResultadoEnrolamientoTotp(String secretoBase32, String uriEnrolamiento) {
            this.secretoBase32 = secretoBase32;
            this.uriEnrolamiento = uriEnrolamiento;
        }

        public String getSecretoBase32() {
            return secretoBase32;
        }

        public String getUriEnrolamiento() {
            return uriEnrolamiento;
        }
    }
}
