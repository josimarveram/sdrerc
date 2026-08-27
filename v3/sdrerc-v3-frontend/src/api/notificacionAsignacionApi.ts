import { apiGet, apiPost } from './http'
import type { EquipoAsignacion, UsuarioAsignable, AsignacionHistorial } from './asignacionApi'
import type { CatalogoItem } from './registroApi'

/**
 * Bandeja Asignación de Notificación (21/08/2026, primera fase del módulo) — fila a nivel de
 * DOCUMENTO (no de expediente), mismo criterio que V2 (`NotificacionAsignacionDocumentoDTO`,
 * reutilizado tal cual por las 3 bandejas siguientes de este módulo cuando se construyan).
 */
export interface NotificacionAsignacionDocumento {
  idDocumentoAnalizado: number
  idExpediente: number
  numeroExpediente: string
  numeroExpedienteSgd: string
  clasificacion: string
  tipoDocumento: string
  numeroDocumento: string
  fechaDocumento: string | null
  titular: string
  estadoDocumentoCodigo: string
  estadoDocumento: string
  totalRelacionados: number
  asignado: boolean
  numeroHojaEnvioNotificacion: string
  usuarioNotificacionActual: string
  estadoExpedienteCodigo: string
  estadoExpediente: string
  fechaVencimiento: string | null
  diasRestantes: number | null
  totalIntentos: number
  estadoFinalNotificacionCodigo: string
  estadoFinalNotificacion: string
  fechaPublicacionNotif: string | null
  /** KPI "Pendientes de asignación". */
  pendienteAsignacion: boolean
  /** KPI "Listos para firma": documento FINAL ya validado, a la espera de firma. */
  listoParaFirma: boolean
}

export function listarBandejaAsignacionNotificacion() {
  return apiGet<NotificacionAsignacionDocumento[]>('/api/notificacion/asignacion/bandeja')
}

/** Mini-panel "① Emisión" — todos los documentos del expediente enfocado ("Documentos a firmar"). */
export function listarDocumentosPorExpedienteNotificacion(idExpediente: number) {
  return apiGet<NotificacionAsignacionDocumento[]>(`/api/notificacion/asignacion/${idExpediente}/documentos`)
}

/** Ícono "Guardar" de "Documentos a firmar". */
export function guardarFirmaDocumentoNotificacion(
  idDocumentoAnalizado: number,
  numeroDocumento: string,
  fechaEmision: string | null,
  estadoDocumentoCodigo: string,
) {
  return apiPost<void>('/api/notificacion/asignacion/firma', {
    idDocumentoAnalizado,
    numeroDocumento,
    fechaEmision,
    estadoDocumentoCodigo,
  })
}

export interface GenerarAsignacionNotificacionRequest {
  idsDocumentoAnalizado: number[]
  idEquipoDestino: number
  idUsuarioDestino: number
  hojasEnvioPorDocumento: Record<number, string>
  comentario: string
  reasignacion: boolean
}

/** "Destino operativo" — mini-panel "② Asignación". */
export function generarAsignacionNotificacion(request: GenerarAsignacionNotificacionRequest) {
  return apiPost<void>('/api/notificacion/asignacion/generar', request)
}

/** "Registrar Supervisión" (resultado Aprobado). */
export function registrarSupervisionNotificacion(idExpediente: number, idDocumentoAnalizado: number) {
  return apiPost<void>('/api/notificacion/asignacion/supervision', { idExpediente, idDocumentoAnalizado })
}

export function existeSupervisionAprobadaNotificacion(idDocumentoAnalizado: number) {
  return apiGet<boolean>(`/api/notificacion/asignacion/${idDocumentoAnalizado}/supervision`)
}

export function listarHistorialAsignacionNotificacion(idDocumentoAnalizado: number) {
  return apiGet<AsignacionHistorial[]>(`/api/notificacion/asignacion/${idDocumentoAnalizado}/historial`)
}

/** Combo "Equipo destino" — EQ_VALIDACION/EQ_NOTIFICACION/EQ_EJECUCION (EQ_ANALISIS diferido). */
export function listarEquiposDestinoNotificacion() {
  return apiGet<EquipoAsignacion[]>('/api/notificacion/asignacion/equipos')
}

export function listarUsuariosDestinoNotificacion(idEquipo: number) {
  return apiGet<UsuarioAsignable[]>('/api/notificacion/asignacion/usuarios', { idEquipo: String(idEquipo) })
}

/** Combo "Estado documento" de esta bandeja (incluye VALIDADO, a diferencia del de Análisis/Ejecución). */
export function listarEstadosDocumentoNotificacion() {
  return apiGet<CatalogoItem[]>('/api/catalogos/estados-documento-notificacion')
}
