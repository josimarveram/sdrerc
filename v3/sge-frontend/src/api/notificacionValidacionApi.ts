import { apiGet, apiPost } from './http'
import type { NotificacionAsignacionDocumento } from './notificacionAsignacionApi'
import type { EquipoAsignacion, UsuarioAsignable } from './asignacionApi'

export type { NotificacionAsignacionDocumento }

/** Cola de la Bandeja Validación de Notificación — SÍ filtra por visibilidad de fila (a diferencia de Bandeja Asignación). */
export function listarBandejaValidacion() {
  return apiGet<NotificacionAsignacionDocumento[]>('/api/notificacion/validacion/bandeja')
}

/** Grilla "Documentos a validar" del panel — todos los documentos del expediente enfocado. */
export function listarDocumentosPorExpedienteValidacion(idExpediente: number) {
  return apiGet<NotificacionAsignacionDocumento[]>(`/api/notificacion/validacion/${idExpediente}/documentos`)
}

/** "Registrar Validación" — resultadoCodigo: 'APROBADO' (mostrado como "Validado" en la UI) / 'OBSERVADO'. */
export function registrarResultadoValidacion(idDocumentoAnalizado: number, resultadoCodigo: 'APROBADO' | 'OBSERVADO', comentario: string) {
  return apiPost<void>('/api/notificacion/validacion/resultado', { idDocumentoAnalizado, resultadoCodigo, comentario })
}

/** Combo "Equipo destino" del bloque "Enviar a supervisión" (22/08/2026, pedido explícito del usuario) — fijo a EQ_SUPERVISION. */
export function listarEquipoSupervision() {
  return apiGet<EquipoAsignacion[]>('/api/notificacion/validacion/equipo-supervision')
}

/** Combo "Usuario destino" del bloque "Enviar a supervisión". */
export function listarUsuariosSupervision(idEquipo: number) {
  return apiGet<UsuarioAsignable[]>('/api/notificacion/validacion/usuarios-supervision', { idEquipo: String(idEquipo) })
}

/** "Enviar a supervisión" — deriva el responsable al usuario/equipo elegido, sin tocar el estado del expediente. */
export function enviarASupervision(idDocumentoAnalizado: number, idEquipoDestino: number, idUsuarioDestino: number) {
  return apiPost<void>('/api/notificacion/validacion/enviar-supervision', { idDocumentoAnalizado, idEquipoDestino, idUsuarioDestino })
}
