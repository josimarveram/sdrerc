import { apiPost } from './http'

/** Mismos 4 pasos que LoginFrameV2.enrutarSegundoFactor() (V2). */
export type NextLoginStep = 'CAMBIO_PASSWORD' | 'EMAIL' | 'TOTP' | 'TOTP_ENROLL'

export interface LoginChallengeResponse {
  challengeToken: string
  nextStep: NextLoginStep
  username: string
  nombreCompleto: string
  tieneCorreo: boolean
  correoEnmascarado: string | null
  totpHabilitado: boolean
}

export interface SessionResponse {
  accessToken: string
  username: string
  nombreCompleto: string
  roles: string[]
  backupCodes: string[] | null
}

export interface TotpEnrollStartResponse {
  secretBase32: string
  enrollmentUri: string
}

const BASE = '/api/auth'

export function login(username: string, password: string) {
  return apiPost<LoginChallengeResponse>(`${BASE}/login`, { username, password })
}

export function changePassword(challengeToken: string, newPassword: string) {
  return apiPost<LoginChallengeResponse>(`${BASE}/change-password`, { challengeToken, newPassword })
}

export function sendEmailCode(challengeToken: string) {
  return apiPost<{ sent: boolean }>(`${BASE}/2fa/email/send`, { challengeToken })
}

export function verifyEmailCode(challengeToken: string, code: string) {
  return apiPost<SessionResponse>(`${BASE}/2fa/email/verify`, { challengeToken, code })
}

export function verifyTotpCode(challengeToken: string, code: string) {
  return apiPost<SessionResponse>(`${BASE}/2fa/totp/verify`, { challengeToken, code })
}

export function startTotpEnrollment(challengeToken: string) {
  return apiPost<TotpEnrollStartResponse>(`${BASE}/2fa/totp/enroll/start`, { challengeToken })
}

export function confirmTotpEnrollment(challengeToken: string, code: string) {
  return apiPost<SessionResponse>(`${BASE}/2fa/totp/enroll/confirm`, { challengeToken, code })
}
