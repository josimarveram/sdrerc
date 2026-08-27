import { apiGet } from './http'
import type { ExpedienteBandeja } from './registroBandejaApi'

export interface BuscarBandejaExpedientesParams {
  texto?: string
  etapa?: string
  estado?: string
  desde?: string
  hasta?: string
  limite?: number
}

/** Equivalente web de la Bandeja de Expedientes (V2: `JPanelBandejaExpedientesNueva`, perfil general — etapa libre, no fija a REGISTRO). */
export function buscarBandejaExpedientes(params: BuscarBandejaExpedientesParams) {
  const query: Record<string, string> = {}
  if (params.texto) query.texto = params.texto
  if (params.etapa) query.etapa = params.etapa
  if (params.estado) query.estado = params.estado
  if (params.desde) query.desde = params.desde
  if (params.hasta) query.hasta = params.hasta
  if (params.limite) query.limite = String(params.limite)
  return apiGet<ExpedienteBandeja[]>('/api/expedientes/bandeja', query)
}

export interface ExpedienteTimelineEvento {
  idExpedienteHistorial: number
  idExpediente: number
  fechaMovimiento: string | null
  movimientoCodigo: string
  movimientoNombre: string
  etapaOrigenCodigo: string
  etapaOrigenNombre: string
  etapaDestinoCodigo: string
  etapaDestinoNombre: string
  estadoOrigenCodigo: string
  estadoOrigenNombre: string
  estadoDestinoCodigo: string
  estadoDestinoNombre: string
  usuarioOrigen: string
  usuarioDestino: string
  equipoDestino: string
  comentario: string
  motivo: string
  tablaRelacionada: string
  idRegistroRelacionado: number | null
  // Calculado en el cliente, ver evaluarEventoDocumento en TrazabilidadContent.tsx
  eventoDeDocumento?: boolean
}

/** Línea de tiempo completa del expediente — subpestaña "Trazabilidad". */
export function listarTrazabilidad(idExpediente: number) {
  return apiGet<ExpedienteTimelineEvento[]>(`/api/expedientes/${idExpediente}/trazabilidad`)
}

export const ETAPAS_EXPEDIENTE = [
  { codigo: 'REGISTRO', nombre: 'Registro' },
  { codigo: 'ASIGNACION', nombre: 'Asignación' },
  { codigo: 'ANALISIS', nombre: 'Análisis' },
  { codigo: 'VERIFICACION', nombre: 'Verificación' },
  { codigo: 'EJECUCION', nombre: 'Ejecución' },
  { codigo: 'NOTIFICACION', nombre: 'Notificación' },
  { codigo: 'PUBLICACION', nombre: 'Publicación' },
  { codigo: 'CIERRE', nombre: 'Cierre' },
] as const
