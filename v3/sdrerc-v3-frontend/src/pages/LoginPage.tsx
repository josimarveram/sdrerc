import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Link,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
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
} from '../api/authApi'
import { ApiError } from '../api/http'
import { useAuth } from '../auth/AuthContext'
import { sdrercColors } from '../theme'

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

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
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
      (session) => finishLogin(session.accessToken, session.username, session.nombreCompleto, session.roles, null),
    )
  }

  function handleVerifyTotp() {
    if (!challenge) return
    runStep(
      () => verifyTotpCode(challenge.token, code),
      (session) => finishLogin(session.accessToken, session.username, session.nombreCompleto, session.roles, null),
    )
  }

  function handleConfirmEnrollment() {
    if (!challenge) return
    runStep(
      () => confirmTotpEnrollment(challenge.token, code),
      (session) =>
        finishLogin(
          session.accessToken,
          session.username,
          session.nombreCompleto,
          session.roles,
          session.backupCodes,
        ),
    )
  }

  function finishLogin(
    accessToken: string,
    respUsername: string,
    nombreCompleto: string,
    roles: string[],
    backupCodes: string[] | null,
  ) {
    setSession({ accessToken, username: respUsername, nombreCompleto, roles, backupCodes })
    if (backupCodes && backupCodes.length > 0) {
      setPendingBackupCodes(backupCodes)
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
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: sdrercColors.backgroundDefault,
      }}
    >
      <Paper elevation={1} sx={{ width: 400, p: 4, borderRadius: 2 }}>
        <Typography variant="h6" sx={{ color: sdrercColors.primary, mb: 0.5 }}>
          SDRERC
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Sistema de Diligencias Registrales - Acceso V3
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {phase === 'CREDENTIALS' && (
          <Stack spacing={2}>
            <TextField
              label="Usuario"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoFocus
              fullWidth
            />
            <TextField
              label="Contraseña"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleCredentialsSubmit()}
              fullWidth
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
      </Paper>
    </Box>
  )
}

function SubmitButton({ loading, onClick, label }: { loading: boolean; onClick: () => void; label: string }) {
  return (
    <Button variant="contained" onClick={onClick} disabled={loading} fullWidth>
      {loading ? <CircularProgress size={22} color="inherit" /> : label}
    </Button>
  )
}

function describeError(e: unknown): string {
  if (e instanceof ApiError) return e.message
  if (e instanceof Error) return e.message
  return 'Ocurrió un error inesperado.'
}
