import { apiDelete, apiGet, apiGetBlob, apiPost, descargarBlob } from './http'
import type { UsuarioAsignable } from './asignacionApi'
import type { CatalogoItem } from './registroApi'
import type { DocumentoAnalizado } from './analisisApi'

/**
 * Fila de la Bandeja Ejecución (20/08/2026) — port acotado de `EjecucionExpedienteDTO` (backend), a
 * su vez port acotado de V2 (~50 campos allá). Sin campos de `expediente_resolucion` (tabla
 * verificada sin filas reales — ver v3/CLAUDE.md), reemplazados por `documentoResolucionConNumero`
 * (calculado contra `EXPEDIENTE_DOCUMENTO_ANALIZADO`, misma tabla que Análisis/Verificación).
 */
export interface EjecucionExpediente {
  idExpediente: number
  numeroExpediente: string
  numeroExpedienteSgd: string
  procedimiento: string
  tipoDocumentoResolucion: string
  tipoActa: string
  numeroActa: string
  titular: string
  responsable: string
  idResponsable: number | null
  idEquipoResponsable: number | null
  fechaRecepcion: string | null
  fechaVencimiento: string | null
  diasRestantes: number | null
  etapaCodigo: string
  estadoCodigo: string
  resultadoAnalisis: string
  responsableAnalisis: string
  totalDocumentos: number
  totalRelacionados: number
  documentoResolucionConNumero: boolean
  /** Flag `EXPEDIENTE_SOLICITUD.grupo_familiar` (grupo CONFIRMADO, no la alerta) — agregado 21/08/2026 para gatear la subpestaña "Grupo Familiar" de solo lectura. */
  grupoFamiliar: boolean
  enEjecucion: boolean
  ejecutado: boolean
  corregible: boolean
  pendiente: boolean
  listoParaNotificacion: boolean
  porVencerOVencido: boolean
}

export interface BuscarBandejaEjecucionParams {
  texto?: string
  estado?: string
  desde?: string
  hasta?: string
  limite?: number
}

export function buscarBandejaEjecucion(params: BuscarBandejaEjecucionParams) {
  const query: Record<string, string> = {}
  if (params.texto) query.texto = params.texto
  if (params.estado) query.estado = params.estado
  if (params.desde) query.desde = params.desde
  if (params.hasta) query.hasta = params.hasta
  if (params.limite) query.limite = String(params.limite)
  return apiGet<EjecucionExpediente[]>('/api/ejecucion/bandeja', query)
}

export interface EjecucionResultado {
  idExpediente: number
  numeroExpediente: string | null
  accionCodigo: string
  etapaDestinoCodigo: string
  estadoDestinoCodigo: string
  mensaje: string
}

/** "Registrar resultado de ejecución" — ejecuta solo INICIO_EJECUCION. */
export function registrarResultadoEjecucion(idExpediente: number, resultadoCodigo: string, resultadoNombre: string, fechaEjecucion: string | null, comentario: string) {
  return apiPost<EjecucionResultado>('/api/ejecucion/resultado', { idExpediente, resultadoCodigo, resultadoNombre, fechaEjecucion, comentario })
}

/** "Enviar a Supervisión" — ejecuta solo DERIVACION_A_NOTIFICACION; sin idEquipoDestino (V2 lo hardcodea a EQ_SUPERVISION). */
export function enviarSupervision(idExpediente: number, comentario: string, idUsuarioDestino: number) {
  return apiPost<EjecucionResultado>('/api/ejecucion/enviar-supervision', { idExpediente, comentario, idUsuarioDestino })
}

export function registrarObservacionEjecucion(idExpediente: number, tipoObservacionCodigo: string, motivoCorreccionCodigo: string, descripcion: string) {
  return apiPost<EjecucionResultado>('/api/ejecucion/observacion', { idExpediente, tipoObservacionCodigo, motivoCorreccionCodigo, descripcion })
}

export function registrarErrorMaterial(idExpediente: number, tipoObservacionCodigo: string, motivoCorreccionCodigo: string, descripcion: string) {
  return apiPost<EjecucionResultado>('/api/ejecucion/error-material', { idExpediente, tipoObservacionCodigo, motivoCorreccionCodigo, descripcion })
}

export function devolverAnalisisDesdeEjecucion(
  idExpediente: number,
  tipoObservacionCodigo: string,
  motivoCorreccionCodigo: string,
  motivoReversion: string,
  comentario: string,
) {
  return apiPost<EjecucionResultado>('/api/ejecucion/devolver-analisis', {
    idExpediente,
    tipoObservacionCodigo,
    motivoCorreccionCodigo,
    motivoReversion,
    comentario,
  })
}

export function listarUsuariosSupervision() {
  return apiGet<UsuarioAsignable[]>('/api/ejecucion/usuarios-supervision')
}

export function listarResultadosEjecucion() {
  return apiGet<CatalogoItem[]>('/api/ejecucion/catalogos/resultados')
}

export function listarTiposObservacionEjecucion() {
  return apiGet<CatalogoItem[]>('/api/ejecucion/catalogos/tipos-observacion')
}

export function listarMotivosCorreccion() {
  return apiGet<CatalogoItem[]>('/api/ejecucion/catalogos/motivos-correccion')
}

/** Grilla "Documentos ejecutados" — mismo dato que Análisis/Verificación (EXPEDIENTE_DOCUMENTO_ANALIZADO). */
export function listarDocumentosEjecucion(idExpediente: number) {
  return apiGet<DocumentoAnalizado[]>(`/api/ejecucion/${idExpediente}/documentos`)
}

export function guardarDocumentoEjecucion(documento: DocumentoAnalizado) {
  return apiPost<number>('/api/ejecucion/documentos', documento)
}

/** Ícono "Eliminar" por fila (21/08/2026, pedido explícito del usuario) — baja lógica, mismo patrón que Análisis. */
export function eliminarDocumentoEjecucion(idExpediente: number, idDocumentoAnalizado: number) {
  return apiDelete<void>(`/api/ejecucion/documentos/${idExpediente}/${idDocumentoAnalizado}`)
}

/** Ícono "Word" por fila (21/08/2026, pedido explícito del usuario) — mismo mecanismo que Análisis. */
export async function descargarDocumentoWordEjecucion(idExpediente: number, idDocumentoAnalizado: number, nombreSugerido: string) {
  const blob = await apiGetBlob(`/api/ejecucion/${idExpediente}/documentos/${idDocumentoAnalizado}/word`)
  descargarBlob(blob, nombreSugerido)
}
