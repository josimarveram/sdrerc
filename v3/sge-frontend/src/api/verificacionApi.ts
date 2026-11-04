import { apiGet, apiPost } from './http'
import type { EquipoAsignacion, UsuarioAsignable } from './asignacionApi'
import type { DocumentoAnalizado } from './analisisApi'

/**
 * Fila de la Bandeja Verificación (20/08/2026) — port acotado de `VerificacionExpedienteDTO`
 * (backend), a su vez port acotado de V2 (63 campos allá). Sin campos de etapa `FIRMA_EMISION`
 * (verificado: 0 filas reales en esa etapa, ver v3/CLAUDE.md).
 */
export interface VerificacionExpediente {
  idExpediente: number
  numeroExpediente: string
  numeroExpedienteSgd: string
  procedimiento: string
  tipoDocumentoPendiente: string
  tipoActa: string
  numeroActa: string
  titular: string
  responsable: string
  idResponsable: number | null
  idEquipoResponsable: number | null
  fechaRecepcion: string | null
  fechaVencimiento: string | null
  diasRestantes: number | null
  fechaRegistro: string | null
  etapaCodigo: string
  estadoCodigo: string
  grupoFamiliar: boolean
  asociadosConfirmados: number
  documentosAnalizados: number
  ultimoResultadoAnalisis: string
  responsableAnalisis: string
  equipoAnalisis: string
  registrableVerificacion: boolean
  observado: boolean
  inconsistente: boolean
  aprobado: boolean
  resultadoResolutivo: boolean
  porVencerOVencido: boolean
}

export interface BuscarBandejaVerificacionParams {
  texto?: string
  estado?: string
  desde?: string
  hasta?: string
  limite?: number
}

export function buscarBandejaVerificacion(params: BuscarBandejaVerificacionParams) {
  const query: Record<string, string> = {}
  if (params.texto) query.texto = params.texto
  if (params.estado) query.estado = params.estado
  if (params.desde) query.desde = params.desde
  if (params.hasta) query.hasta = params.hasta
  if (params.limite) query.limite = String(params.limite)
  return apiGet<VerificacionExpediente[]>('/api/verificacion/bandeja', query)
}

export interface VerificacionResultado {
  idExpediente: number
  numeroExpediente: string | null
  accionCodigo: string
  etapaDestinoCodigo: string
  estadoDestinoCodigo: string
  mensaje: string
}

/** "Registrar Verificación" (Observado) — solo guarda, no mueve el expediente de etapa. */
export function registrarObservado(idExpediente: number, comentario: string, descripcionObservacion: string) {
  return apiPost<VerificacionResultado>('/api/verificacion/observar', { idExpediente, comentario, descripcionObservacion })
}

/** "Registrar Verificación" (Aprobado) — solo guarda, no mueve el expediente de etapa (antes "aprobarVerificacionDirecta", enviaba directo a Ejecución). */
export function registrarAprobado(idExpediente: number, comentario: string) {
  return apiPost<VerificacionResultado>('/api/verificacion/registrar-aprobado', { idExpediente, comentario })
}

/** "Enviar a ..." (Destino operativo) — mueve el expediente ya registrado (Observado o Aprobado) al equipo elegido. */
export function aprobarVerificacionConDestino(idExpediente: number, comentario: string, idEquipoDestino: number, idUsuarioDestino: number) {
  return apiPost<VerificacionResultado>('/api/verificacion/aprobar-destino', { idExpediente, comentario, idEquipoDestino, idUsuarioDestino })
}

export function listarEquiposDestinoVerificacion() {
  return apiGet<EquipoAsignacion[]>('/api/verificacion/equipos')
}

export function listarUsuariosDestinoVerificacion(idEquipo: number) {
  return apiGet<UsuarioAsignable[]>('/api/verificacion/usuarios', { idEquipo: String(idEquipo) })
}

/** Grilla "Documentos verificados" — mismo dato que la de Análisis (EXPEDIENTE_DOCUMENTO_ANALIZADO), expuesto bajo /api/verificacion por higiene de API. */
export function listarDocumentosVerificados(idExpediente: number) {
  return apiGet<DocumentoAnalizado[]>(`/api/verificacion/${idExpediente}/documentos`)
}

/** Ícono "Guardar" por fila — devuelve el id real del documento. */
export function guardarDocumentoVerificado(documento: DocumentoAnalizado) {
  return apiPost<number>('/api/verificacion/documentos', documento)
}
