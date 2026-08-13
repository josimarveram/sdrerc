import { apiDelete, apiGet, apiPost } from './http'

export interface GrupoFamiliarCandidato {
  idExpediente: number
  numeroExpediente: string
  idPersona: number
  titular: string
  etapaCodigo: string
  estadoCodigo: string
  abogadoAsignado: string
  idGrupoFamiliarActual: number | null
  yaEnGrupo: boolean
}

export interface GrupoFamiliarIntegrante {
  idPersona: number
  nombreCompleto: string
  idExpediente: number
  numeroExpediente: string
  etapaCodigo: string
  estadoCodigo: string
  abogadoAsignado: string
  tipoActa: string
}

export interface GrupoFamiliarResultado {
  totalSolicitados: number
  totalAsociados: number
  totalYaAsociados: number
  totalOmitidos: number
  mensaje: string
}

export function listarPosiblesIntegrantes(idExpediente: number) {
  return apiGet<GrupoFamiliarCandidato[]>(`/api/grupo-familiar/${idExpediente}/candidatos`)
}

export function buscarPosiblesIntegrantesManual(idExpediente: number, texto: string) {
  return apiGet<GrupoFamiliarCandidato[]>(`/api/grupo-familiar/${idExpediente}/candidatos/buscar`, { texto })
}

export function listarIntegrantesGrupoFamiliar(idExpediente: number) {
  return apiGet<GrupoFamiliarIntegrante[]>(`/api/grupo-familiar/${idExpediente}/integrantes`)
}

export function asociarGrupoFamiliar(idExpedientePrincipal: number, idsExpedientesCandidatos: number[]) {
  return apiPost<GrupoFamiliarResultado>('/api/grupo-familiar/asociar', { idExpedientePrincipal, idsExpedientesCandidatos })
}

export function eliminarDeGrupoFamiliar(idExpediente: number) {
  return apiDelete<GrupoFamiliarResultado>(`/api/grupo-familiar/${idExpediente}`)
}
