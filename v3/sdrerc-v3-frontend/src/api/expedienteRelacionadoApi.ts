import { apiGet, apiPost } from './http'

export interface ExpedienteRelacionado {
  idExpediente: number
  numeroExpediente: string
  numeroExpedienteSgd: string
  numeroDocumento: string
  tipoActa: string
  numeroActa: string
  titular: string
  procedimiento: string
  solicitante: string
  equipoAsignado: string
  abogadoAsignado: string
  etapaEstado: string
  fechaRecepcion: string | null
  fechaVencimiento: string | null
  diasRestantes: number | null
  motivoCoincidencia: string
  alertaIngreso: string
  tipoRelacion: string
  descripcionRelacion: string
  fechaRelacion: string | null
  usuarioRelacion: string
}

export interface AsociarRelacionadosRequest {
  idExpedientePrincipal: number
  idsRelacionados: number[]
  descripcion: string
}

export interface ExpedienteRelacionResultado {
  totalSolicitados: number
  totalAsociados: number
  totalYaAsociados: number
  totalOmitidos: number
  mensaje: string
}

export function listarSolicitudesDelGrupo(idExpediente: number) {
  return apiGet<ExpedienteRelacionado[]>(`/api/expedientes-relacionados/${idExpediente}/solicitudes-grupo`)
}

export function listarConfirmados(idExpediente: number) {
  return apiGet<ExpedienteRelacionado[]>(`/api/expedientes-relacionados/${idExpediente}/confirmados`)
}

export function asociarRelacionados(request: AsociarRelacionadosRequest) {
  return apiPost<ExpedienteRelacionResultado>('/api/expedientes-relacionados/asociar', request)
}
