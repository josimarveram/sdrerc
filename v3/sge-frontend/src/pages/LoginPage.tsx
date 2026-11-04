import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  IconButton,
  InputAdornment,
  Link,
  Paper,
  Stack,
  TextField,
  Typography,
  alpha,
} from '@mui/material'
import PersonOutlineOutlinedIcon from '@mui/icons-material/PersonOutlineOutlined'
import LockOutlinedIcon from '@mui/icons-material/LockOutlined'
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined'
import VisibilityOffOutlinedIcon from '@mui/icons-material/VisibilityOffOutlined'
import { QRCodeSVG } from 'qrcode.react'
import {
  changePassword,
  confirmTotpEnrollment,
  login,
  sendEmailCode,
  startTotpEnrollment,
  verifyEmailCode,
  verifyTotpCode,
  type NextLoginStep,
  type SessionResponse,
} from '../api/authApi'
import { ApiError } from '../api/http'
import { useAuth } from '../auth/AuthContext'
import { sdrercColors } from '../theme'
import logoReniec from '../assets/reniec-logo.jpeg'

type Phase = 'CREDENTIALS' | NextLoginStep | 'BACKUP_CODES'

interface ChallengeState {
  token: string
  tieneCorreo: boolean
  correoEnmascarado: string | null
  totpHabilitado: boolean
}

/**
 * Replica el flujo completo de login/2FA de LoginFrameV2 (V2): credenciales -> cambio de
 * password obligatorio si aplica -> segundo factor (correo primero si hay correo cargado, con
 * opcion de cambiar a TOTP sin volver a pedir credenciales) -> sesion. Ver
 * docs/arquitectura_app/PLAN_MIGRACION_SDRERC_V3.md, Fase 0.
 */
export function LoginPage() {
  const navigate = useNavigate()
  const { setSession } = useAuth()

  const [phase, setPhase] = useState<Phase>('CREDENTIALS')
  const [challenge, setChallenge] = useState<ChallengeState | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [pendingBackupCodes, setPendingBackupCodes] = useState<string[] | null>(null)
  // 01/09/2026: http.ts#manejarSesionVencida redirige aquí con este parámetro cuando el JWT
  // vence/es inválido — evita que el usuario llegue al login sin ninguna explicación de por qué
  // fue expulsado de la página en la que estaba.
  const [sesionVencida] = useState(() => new URLSearchParams(window.location.search).get('sesionVencida') === '1')
  // 01/09/2026: distinto del anterior — lo dispara `InactivityGuard.tsx` cuando cierra la sesión
  // de forma PROACTIVA tras 10 minutos sin actividad (antes de que el JWT llegue a vencer solo).
  const [cierreInactividad] = useState(() => new URLSearchParams(window.location.search).get('cierreInactividad') === '1')

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [mostrarPassword, setMostrarPassword] = useState(false)
  const [newPassword, setNewPassword] = useState('')
  const [code, setCode] = useState('')
  const [enrollment, setEnrollment] = useState<{ secretBase32: string; enrollmentUri: string } | null>(null)

  const emailSentOnceRef = useRef(false)
  const enrollmentStartedOnceRef = useRef(false)

  useEffect(() => {
    if (phase === 'EMAIL' && challenge && !emailSentOnceRef.current) {
      emailSentOnceRef.current = true
      sendEmailCode(challenge.token).catch((e) => setError(describeError(e)))
    }
    if (phase !== 'EMAIL') {
      emailSentOnceRef.current = false
    }
  }, [phase, challenge])

  useEffect(() => {
    if (phase === 'TOTP_ENROLL' && challenge && !enrollmentStartedOnceRef.current) {
      enrollmentStartedOnceRef.current = true
      startTotpEnrollment(challenge.token)
        .then(setEnrollment)
        .catch((e) => setError(describeError(e)))
    }
    if (phase !== 'TOTP_ENROLL') {
      enrollmentStartedOnceRef.current = false
    }
  }, [phase, challenge])

  async function runStep<T>(action: () => Promise<T>, onSuccess: (result: T) => void) {
    setLoading(true)
    setError(null)
    try {
      const result = await action()
      onSuccess(result)
    } catch (e) {
      setError(describeError(e))
    } finally {
      setLoading(false)
    }
  }

  function handleCredentialsSubmit() {
    runStep(
      () => login(username, password),
      (resp) => {
        setChallenge({
          token: resp.challengeToken,
          tieneCorreo: resp.tieneCorreo,
          correoEnmascarado: resp.correoEnmascarado,
          totpHabilitado: resp.totpHabilitado,
        })
        setPhase(resp.nextStep)
      },
    )
  }

  function handleChangePasswordSubmit() {
    if (!challenge) return
    runStep(
      () => changePassword(challenge.token, newPassword),
      (resp) => {
        setChallenge({
          token: resp.challengeToken,
          tieneCorreo: resp.tieneCorreo,
          correoEnmascarado: resp.correoEnmascarado,
          totpHabilitado: resp.totpHabilitado,
        })
        setPhase(resp.nextStep)
      },
    )
  }

  function handleVerifyEmail() {
    if (!challenge) return
    runStep(
      () => verifyEmailCode(challenge.token, code),
      (session) => finishLogin(session),
    )
  }

  function handleVerifyTotp() {
    if (!challenge) return
    runStep(
      () => verifyTotpCode(challenge.token, code),
      (session) => finishLogin(session),
    )
  }

  function handleConfirmEnrollment() {
    if (!challenge) return
    runStep(
      () => confirmTotpEnrollment(challenge.token, code),
      (session) => finishLogin(session),
    )
  }

  // Recibe el SessionResponse completo (incluye `permisos`, ver AuthContext.tsx) en vez de
  // desarmarlo en parametros sueltos - una version anterior reconstruia el objeto a mano en este
  // punto y se olvido de `permisos`, dejando session.permisos en `undefined` para cualquier login
  // fresco (bug real: "Cannot read properties of undefined (reading 'length')" en Sidebar/HomePage).
  function finishLogin(session: SessionResponse) {
    setSession(session)
    if (session.backupCodes && session.backupCodes.length > 0) {
      setPendingBackupCodes(session.backupCodes)
      setPhase('BACKUP_CODES')
    } else {
      navigate('/')
    }
  }

  function switchToAuthenticatorApp() {
    if (!challenge) return
    setError(null)
    setCode('')
    setPhase(challenge.totpHabilitado ? 'TOTP' : 'TOTP_ENROLL')
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 2,
        p: 2,
        background: `linear-gradient(135deg, ${sdrercColors.primaryDark} 0%, ${sdrercColors.primary} 55%, ${sdrercColors.info} 100%)`,
      }}
    >
      <Paper
        elevation={3}
        sx={{
          width: 420,
          maxWidth: '100%',
          borderRadius: 4,
          overflow: 'hidden',
        }}
      >
        <Box
          sx={{
            px: 4,
            pt: 4,
            pb: 3,
            textAlign: 'center',
            background: `linear-gradient(135deg, ${sdrercColors.primaryDark} 0%, ${sdrercColors.primary} 100%)`,
          }}
        >
          <Box
            component="img"
            src={logoReniec}
            alt="RENIEC"
            sx={{
              height: 56,
              width: 'auto',
              borderRadius: 2,
              bgcolor: '#FFFFFF',
              p: '6px',
              boxShadow: '0 4px 14px rgba(0,0,0,0.25)',
              mb: 1.5,
            }}
          />
          <Typography variant="h5" sx={{ color: '#FFFFFF', fontWeight: 700, letterSpacing: 1 }}>
            SGE
          </Typography>
          <Typography variant="body2" sx={{ color: alpha('#FFFFFF', 0.85), mt: 0.5 }}>
            Sistema de Gestión de Expedientes - Acceso
          </Typography>
        </Box>

        <Box sx={{ px: 4, py: 4 }}>
          {!error && sesionVencida && phase === 'CREDENTIALS' && (
            <Alert severity="info" sx={{ mb: 2, borderRadius: 2 }}>
              Su sesión expiró por inactividad. Inicie sesión nuevamente.
            </Alert>
          )}

          {!error && cierreInactividad && phase === 'CREDENTIALS' && (
            <Alert severity="info" sx={{ mb: 2, borderRadius: 2 }}>
              Su sesión se cerró automáticamente por inactividad. Inicie sesión nuevamente.
            </Alert>
          )}

          {error && (
            <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>
              {error}
            </Alert>
          )}

          {phase === 'CREDENTIALS' && (
            <Stack spacing={2.5}>
              <TextField
                label="Usuario"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoFocus
                fullWidth
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <PersonOutlineOutlinedIcon fontSize="small" sx={{ color: sdrercColors.textSecondary }} />
                      </InputAdornment>
                    ),
                  },
                }}
              />
              <TextField
                label="Contraseña"
                type={mostrarPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleCredentialsSubmit()}
                fullWidth
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <LockOutlinedIcon fontSize="small" sx={{ color: sdrercColors.textSecondary }} />
                      </InputAdornment>
                    ),
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          size="small"
                          edge="end"
                          tabIndex={-1}
                          onClick={() => setMostrarPassword((v) => !v)}
                          aria-label={mostrarPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                        >
                          {mostrarPassword ? (
                            <VisibilityOffOutlinedIcon fontSize="small" />
                          ) : (
                            <VisibilityOutlinedIcon fontSize="small" />
                          )}
                        </IconButton>
                      </InputAdornment>
                    ),
                  },
                }}
              />
              <SubmitButton loading={loading} onClick={handleCredentialsSubmit} label="Ingresar" />
            </Stack>
          )}

        {phase === 'CAMBIO_PASSWORD' && (
          <Stack spacing={2}>
            <Typography variant="body2">Debe cambiar su contraseña antes de continuar.</Typography>
            <TextField
              label="Nueva contraseña"
              type="password"
              helperText="Mínimo 8 caracteres, combinando letras y números."
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              autoFocus
              fullWidth
            />
            <SubmitButton loading={loading} onClick={handleChangePasswordSubmit} label="Cambiar contraseña" />
          </Stack>
        )}

        {phase === 'EMAIL' && challenge && (
          <Stack spacing={2}>
            <Typography variant="body2">
              Enviamos un código de verificación a <strong>{challenge.correoEnmascarado}</strong>. Vence en 10
              minutos.
            </Typography>
            <TextField
              label="Código de 6 dígitos"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleVerifyEmail()}
              autoFocus
              fullWidth
            />
            <SubmitButton loading={loading} onClick={handleVerifyEmail} label="Verificar" />
            <Stack direction="row" sx={{ justifyContent: 'space-between' }}>
              <Link component="button" variant="body2" onClick={() => sendEmailCode(challenge.token)}>
                Reenviar código
              </Link>
              <Link component="button" variant="body2" onClick={switchToAuthenticatorApp}>
                Prefiero usar una app autenticadora
              </Link>
            </Stack>
          </Stack>
        )}

        {phase === 'TOTP' && (
          <Stack spacing={2}>
            <Typography variant="body2">Ingrese el código de su aplicación autenticadora.</Typography>
            <TextField
              label="Código de 6 dígitos (o de respaldo)"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleVerifyTotp()}
              autoFocus
              fullWidth
            />
            <SubmitButton loading={loading} onClick={handleVerifyTotp} label="Verificar" />
          </Stack>
        )}

        {phase === 'TOTP_ENROLL' && (
          <Stack spacing={2}>
            <Typography variant="body2">
              Escanee este código QR con su aplicación autenticadora (Google Authenticator, Microsoft
              Authenticator, etc.).
            </Typography>
            {enrollment ? (
              <>
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 1 }}>
                  <QRCodeSVG value={enrollment.enrollmentUri} size={180} />
                </Box>
                <Typography variant="body2" color="text.secondary">
                  Clave manual (si no puede escanear): <code>{enrollment.secretBase32}</code>
                </Typography>
                <TextField
                  label="Código de 6 dígitos"
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleConfirmEnrollment()}
                  fullWidth
                />
                <SubmitButton loading={loading} onClick={handleConfirmEnrollment} label="Confirmar" />
              </>
            ) : (
              <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
                <CircularProgress size={24} />
              </Box>
            )}
          </Stack>
        )}

        {phase === 'BACKUP_CODES' && pendingBackupCodes && (
          <Stack spacing={2}>
            <Alert severity="warning">
              Guarde estos códigos de respaldo en un lugar seguro. Se muestran una única vez y sirven para
              ingresar si pierde su dispositivo autenticador.
            </Alert>
            <Stack direction="row" sx={{ flexWrap: 'wrap', gap: 1 }}>
              {pendingBackupCodes.map((c) => (
                <Chip key={c} label={c} sx={{ fontFamily: 'monospace' }} />
              ))}
            </Stack>
            <Button variant="contained" onClick={() => navigate('/')}>
              Continuar
            </Button>
          </Stack>
        )}
        </Box>
      </Paper>

      <Typography variant="caption" sx={{ color: alpha('#FFFFFF', 0.75) }}>
        Acceso restringido a personal autorizado.
      </Typography>
    </Box>
  )
}

function SubmitButton({ loading, onClick, label }: { loading: boolean; onClick: () => void; label: string }) {
  return (
    <Button
      variant="contained"
      onClick={onClick}
      disabled={loading}
      fullWidth
      sx={{
        py: 1.2,
        borderRadius: 2,
        fontWeight: 600,
        boxShadow: '0 6px 16px rgba(21,71,117,0.28)',
        '&:hover': { boxShadow: '0 8px 20px rgba(21,71,117,0.36)' },
      }}
    >
      {loading ? <CircularProgress size={22} color="inherit" /> : label}
    </Button>
  )
}

function describeError(e: unknown): string {
  if (e instanceof ApiError) return e.message
  if (e instanceof Error) return e.message
  return 'Ocurrió un error inesperado.'
}
