import { apiDelete, apiGet, apiPost, apiPut } from './http'
import type { NotificacionAsignacionDocumento } from './notificacionAsignacionApi'
import type { NotificacionIntento } from './notificacionNotificacionApi'

export type { NotificacionAsignacionDocumento, NotificacionIntento }

/** Cola de la Bandeja Publicación (4ta pestaña) — documentos que ya agotaron sus 2 intentos directos. */
export function listarBandejaPublicacion() {
  return apiGet<NotificacionAsignacionDocumento[]>('/api/notificacion/publicacion/bandeja')
}

/** Intentos 1/2 (solo lectura) + Publicación (editable) de un documento. */
export function listarIntentosPublicacion(idDocumentoAnalizado: number) {
  return apiGet<NotificacionIntento[]>(`/api/notificacion/publicacion/${idDocumentoAnalizado}/intentos`)
}

export interface AgregarPublicacionRequest {
  idExpediente: number
  idDocumentoAnalizado: number
  fechaPublicacion: string | null
}

/** "+ Agregar publicación", fila borrador guardada. */
export function agregarPublicacion(request: AgregarPublicacionRequest) {
  return apiPost<void>('/api/notificacion/publicacion/publicacion', request)
}

export interface ActualizarPublicacionRequest {
  idExpedienteNotificacion: number
  estadoNotificacionCodigo: 'PENDIENTE' | 'EXITOSA'
  fechaPublicacion: string | null
}

/** Guardar la fila de Publicación ya persistida. */
export function actualizarPublicacion(request: ActualizarPublicacionRequest) {
  return apiPut<void>('/api/notificacion/publicacion/publicacion', request)
}

/** Baja lógica de la fila de Publicación. */
export function eliminarPublicacion(idExpedienteNotificacion: number) {
  return apiDelete<void>(`/api/notificacion/publicacion/publicacion/${idExpedienteNotificacion}`)
}
