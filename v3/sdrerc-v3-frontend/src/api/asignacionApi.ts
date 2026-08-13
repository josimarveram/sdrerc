import { apiGet, apiPost } from './http'

export interface AsignacionExpediente {
  idExpediente: number
  numeroExpediente: string
  numeroExpedienteSgd: string
  numeroHojaEnvioAsignacion: string
  fechaRecepcion: string | null
  fechaVencimiento: string | null
  diasRestantes: number | null
  procedimiento: string
  tipoActa: string
  numeroActa: string
  titular: string
  abogadoAsignado: string
  etapaCodigo: string
  estadoCodigo: string
  asignacionActiva: boolean
  potencialDuplicado: boolean
  posibleGrupoFamiliar: boolean
  grupoFamiliar: boolean
  asignable: boolean
  alertaIngreso: string
}

export interface BuscarBandejaAsignacionParams {
  texto?: string
  estado?: string
  desde?: string
  hasta?: string
  limite?: number
}

export function buscarBandejaAsignacion(params: BuscarBandejaAsignacionParams) {
  const query: Record<string, string> = {}
  if (params.texto) query.texto = params.texto
  if (params.estado) query.estado = params.estado
  if (params.desde) query.desde = params.desde
  if (params.hasta) query.hasta = params.hasta
  query.limite = String(params.limite ?? 200)
  return apiGet<AsignacionExpediente[]>('/api/asignacion/bandeja', query)
}

export interface EquipoAsignacion {
  idEquipo: number
  codigo: string
  nombre: string
  area: string
  displayName: string
}

export function listarEquipos() {
  return apiGet<EquipoAsignacion[]>('/api/asignacion/equipos')
}

export interface UsuarioAsignable {
  idUsuario: number
  username: string
  nombreCompleto: string
  idEquipo: number | null
  equipoNombre: string
  rolCodigo: string
  supervisorNombre: string
  displayName: string
}

export function listarAbogados(idEquipo: number) {
  return apiGet<UsuarioAsignable[]>('/api/asignacion/abogados', { idEquipo: String(idEquipo) })
}

export interface AsignarExpedientesRequest {
  idsExpediente: number[]
  idEquipo: number
  idAbogado: number
  comentario: string
  hojasEnvioPorExpediente: Record<number, string>
}

export interface AsignacionResultado {
  exitoso: boolean
  totalSolicitados: number
  totalAsignados: number
  mensaje: string
  detalles: string[]
}

export function asignarExpedientes(request: AsignarExpedientesRequest) {
  return apiPost<AsignacionResultado>('/api/asignacion/asignar', request)
}

export interface ReasignarExpedienteRequest {
  idExpediente: number
  idEquipo: number
  idAbogado: number
  numeroHojaEnvio: string
  comentario: string
}

export function reasignarExpediente(request: ReasignarExpedienteRequest) {
  return apiPost<AsignacionResultado>('/api/asignacion/reasignar', request)
}

export interface AsignacionHistorial {
  idExpedienteAsignacion: number
  abogado: string
  equipo: string
  numeroHojaEnvio: string
  fechaAsignacion: string | null
  activa: boolean
  reasignacionExcepcional: boolean
  motivo: string
  asignadoPor: string
}

export function listarHistorialAsignaciones(idExpediente: number) {
  return apiGet<AsignacionHistorial[]>(`/api/asignacion/${idExpediente}/historial`)
}

export interface AsignacionCartaRespuesta {
  idDocumentoAnalizado: number
  idExpediente: number
  numeroExpediente: string
  numeroExpedienteSgd: string
  titular: string
  tipoDocumentoNombre: string
  estadoDocumentoNombre: string
  fechaDocumento: string | null
  numeroDocumento: string
  descripcion: string
  requiereRespuesta: boolean
  notificado: boolean
  fechaAcuse: string | null
  confirmacionRespuesta: string
  fechaRespuesta: string | null
  numeroHojaEnvioRespuesta: string
  requierePublicacion: boolean
  fechaPublicacionEdicto: string | null
  fechaPublicacionNotificacion: string | null
  tipoDocumentoCodigo: string
  etapaCodigo: string
  diasVencimiento: number | null
  diasPlazoVencimiento: number | null
  fechaVencimiento: string | null
  tipoAlertaVencimiento: string
  estadoCarta: string
}

export function listarCartasRespuestaPendientes() {
  return apiGet<AsignacionCartaRespuesta[]>('/api/asignacion/cartas-respuesta')
}

export interface CargaLaboralAbogado {
  idUsuario: number
  abogado: string
  supervisor: string
  analisisPorRecibir: number
  analisisEnProceso: number
  analisisObservado: number
  analisisCartaIntermedia: number
  enVerificacion: number
  enEjecucion: number
  porVencer: number
  vencidos: number
}

export function enAnalisis(item: CargaLaboralAbogado): number {
  return item.analisisPorRecibir + item.analisisEnProceso + item.analisisObservado + item.analisisCartaIntermedia
}

export function cargaTotal(item: CargaLaboralAbogado): number {
  return enAnalisis(item) + item.enVerificacion + item.enEjecucion
}

export function listarCargaAbogados(idEquipo?: number) {
  return apiGet<CargaLaboralAbogado[]>('/api/asignacion/carga-abogados', idEquipo ? { idEquipo: String(idEquipo) } : undefined)
}

export interface CargaLaboralDocumento {
  idExpediente: number
  numeroExpediente: string
  etapa: string
  estado: string
  fechaVencimiento: string | null
  diasRestantes: number | null
}

export function listarDocumentosPorAbogado(idUsuario: number) {
  return apiGet<CargaLaboralDocumento[]>(`/api/asignacion/carga-abogados/${idUsuario}/documentos`)
}

export function listarIdsUsuarioConVencimientoEnRango(desde: string, hasta: string) {
  const params: Record<string, string> = {}
  if (desde) params.desde = desde
  if (hasta) params.hasta = hasta
  return apiGet<number[]>('/api/asignacion/carga-abogados/vencimiento', params)
}

/** "Decisión de número" del panel "Asociar" (V2): genera número independiente para una solicitud sin número. */
export function generarNumeroExpediente(idExpediente: number) {
  return apiPost<{ numeroExpediente: string }>(`/api/asignacion/${idExpediente}/generar-numero`, {})
}
