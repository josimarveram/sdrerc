import { apiDelete, apiGet, apiPost, apiPut } from './http'
import type { NotificacionAsignacionDocumento } from './notificacionAsignacionApi'

export type { NotificacionAsignacionDocumento }

/** Intento (fila hija) de la Bandeja Notificación — port de `NotificacionIntentoDTO` (V2/V3). */
export interface NotificacionIntento {
  idExpedienteNotificacion: number
  idExpediente: number
  idDocumentoAnalizado: number
  numeroIntento: number
  tipoNotificacionCodigo: string
  tipoNotificacion: string
  fechaEnvio: string | null
  estadoNotificacionCodigo: string
  estadoNotificacion: string
  codigoNotificacion: string
  fechaRecepcion: string | null
  ubicado: boolean
  fechaPublicacion: string | null
}

/** Cola de la Bandeja Notificación (3ra pestaña) — excluye documentos que ya agotaron sus 2 intentos directos. */
export function listarBandejaNotificacion() {
  return apiGet<NotificacionAsignacionDocumento[]>('/api/notificacion/notificacion/bandeja')
}

/** Intentos directos (1/2) de un documento — expansión "+" de una fila padre. */
export function listarIntentosNotificacion(idDocumentoAnalizado: number) {
  return apiGet<NotificacionIntento[]>(`/api/notificacion/notificacion/${idDocumentoAnalizado}/intentos`)
}

export interface AgregarIntentoRequest {
  idExpediente: number
  idDocumentoAnalizado: number
  tipoNotificacionCodigo: string
  codigoNotificacion: string
  fechaEnvio: string | null
}

/** "+ Agregar intento", fila borrador guardada. */
export function agregarIntentoNotificacion(request: AgregarIntentoRequest) {
  return apiPost<void>('/api/notificacion/notificacion/intentos', request)
}

export interface ActualizarIntentoRequest {
  idExpedienteNotificacion: number
  tipoNotificacionCodigo: string
  estadoNotificacionCodigo: string
  codigoNotificacion: string
  fechaEnvio: string | null
}

/** Guardar una fila de intento existente sin marcarla "Ubicado". */
export function actualizarIntentoNotificacion(request: ActualizarIntentoRequest) {
  return apiPut<void>('/api/notificacion/notificacion/intentos', request)
}

export interface ConfirmarRecepcionRequest {
  idExpediente: number
  idExpedienteNotificacion: number
  idDocumentoAnalizado: number
  codigoORecibidoPor: string
  fechaEnvio: string | null
  fechaRecepcion: string | null
}

/** Guardar una fila de intento marcándola "Ubicado" — confirma recepción (Fecha Acuse). */
export function confirmarRecepcionIntentoNotificacion(request: ConfirmarRecepcionRequest) {
  return apiPost<void>('/api/notificacion/notificacion/intentos/confirmar-recepcion', request)
}

/** Baja lógica de un intento ya registrado. */
export function eliminarIntentoNotificacion(idExpedienteNotificacion: number) {
  return apiDelete<void>(`/api/notificacion/notificacion/intentos/${idExpedienteNotificacion}`)
}
