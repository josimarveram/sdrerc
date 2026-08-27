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

export interface GrupoFamiliarEstadoAlerta {
  grupoFamiliarConfirmado: boolean
  mensajeAlertaPosibleGrupoFamiliar: string | null
  tieneAlerta: boolean
  // 20/08/2026, pedido explícito del usuario (regla de tamaño mínimo): cantidad de
  // solicitudes/integrantes que tiene HOY el vínculo (grupo confirmado o alerta), calculada
  // dinámicamente por el backend en cada consulta — 0 si no hay ni grupo confirmado ni alerta
  // activa. Con 2 o menos, descartar/retirar disuelve el vínculo por completo; con 3 o más, solo
  // afecta a la solicitud sobre la que se actúa.
  tamanoGrupo: number
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

export function obtenerEstadoAlertaGrupoFamiliar(idExpediente: number) {
  return apiGet<GrupoFamiliarEstadoAlerta>(`/api/grupo-familiar/${idExpediente}/estado-alerta`)
}

/** Descarta la alerta "Posible Grupo Familiar" sin asociar ("esto no es en realidad un posible integrante", botón papelera en V2). */
export function eliminarAlertaPosibleGrupoFamiliar(idExpediente: number) {
  return apiDelete<void>(`/api/grupo-familiar/${idExpediente}/alerta`)
}
