import { apiDelete, apiGet, apiGetBlob, apiPost, descargarBlob } from './http'
import type { CatalogoItem } from './registroApi'

/**
 * Fila de la Bandeja Análisis (20/08/2026, primer incremento del módulo — ver v3/CLAUDE.md).
 * Mismos nombres/criterios exactos que `AnalisisExpedienteDTO` (backend), que a su vez es un port
 * acotado del DTO real de V2 (41 campos allá, acotado a los que la bandeja + lógica de negocio de
 * este incremento necesitan).
 */
export interface AnalisisExpediente {
  idExpediente: number
  numeroExpediente: string
  numeroExpedienteSgd: string
  procedimiento: string
  tipoActa: string
  numeroActa: string
  titular: string
  responsable: string
  idResponsable: number | null
  idEquipoResponsable: number | null
  fechaRecepcion: string | null
  fechaVencimiento: string | null
  fechaRegistro: string | null
  diasRestantes: number | null
  etapaCodigo: string
  estadoCodigo: string
  grupoFamiliar: boolean
  asociadosConfirmados: number
  documentosAnalizados: number
  recibible: boolean
  registrable: boolean
  enviableVerificacion: boolean
  derivado: boolean
  observado: boolean
  porVencerOVencido: boolean
}

export interface BuscarBandejaAnalisisParams {
  texto?: string
  estado?: string
  desde?: string
  hasta?: string
  limite?: number
}

export function buscarBandejaAnalisis(params: BuscarBandejaAnalisisParams) {
  const query: Record<string, string> = {}
  if (params.texto) query.texto = params.texto
  if (params.estado) query.estado = params.estado
  if (params.desde) query.desde = params.desde
  if (params.hasta) query.hasta = params.hasta
  if (params.limite) query.limite = String(params.limite)
  return apiGet<AnalisisExpediente[]>('/api/analisis/bandeja', query)
}

export interface AnalisisResultado {
  idExpediente: number
  numeroExpediente: string | null
  accionCodigo: string
  etapaDestinoCodigo: string
  estadoDestinoCodigo: string
  mensaje: string
}

export function recibirExpediente(idExpediente: number, comentario: string) {
  return apiPost<AnalisisResultado>('/api/analisis/recibir', { idExpediente, comentario })
}

/** "Recibir" en la mini-grilla de "Solicitudes asociadas" de la subpestaña Análisis. */
export function recibirDocumentoAsociado(idExpedientePrincipal: number, idExpedienteAsociado: number, comentario: string) {
  return apiPost<AnalisisResultado>('/api/analisis/recibir-asociado', {
    idExpedientePrincipal,
    idExpedienteAsociado,
    comentario,
  })
}

/**
 * Documento de la grilla maestro-detalle "Documentos analizados" (jerarquía de máx. 2 niveles,
 * `idDocumentoPadre=null` = documento principal). Mismos 26 campos que `DocumentoAnalizadoDTO`
 * (backend), port acotado de V2. Filas nuevas sin guardar usan un `idDocumentoAnalizado` negativo
 * temporal (mismo patrón que V2: `id<0` en el frontend hasta el primer "Guardar").
 */
export interface DocumentoAnalizado {
  idDocumentoAnalizado: number | null
  idExpediente: number | null
  idExpedienteAnalisis: number | null
  tipoDocumentoCodigo: string
  tipoDocumentoNombre: string
  estadoDocumentoCodigo: string
  estadoDocumentoNombre: string
  fechaDocumento: string | null
  numeroDocumento: string
  descripcion: string
  notificado: boolean
  fechaAcuse: string | null
  requiereRespuesta: boolean
  confirmacionRespuesta: string
  fechaRespuesta: string | null
  numeroHojaEnvioRespuesta: string
  requierePublicacion: boolean
  fechaPublicacion: string | null
  detalleObservacion: string
  idDocumentoPadre: number | null
  nivel: number
  orden: number
  estadoRespuesta: string
  activo: boolean
  fechaRegistro: string | null
  fechaModificacion: string | null
  existeOposicion: boolean | null
  /** `TIPO_DOCUMENTO_ADJUNTO.clasificacion` ('FINAL'/'INTERMEDIO'/'' para informes sin clasificar) —
   * 30/08/2026, agregado para que las nested grid de Bandeja de supervisión/Validación (Notificación)
   * puedan filtrar solo cartas intermedias/finales. */
  clasificacion: string
}

export function listarDocumentosAnalizados(idExpediente: number) {
  return apiGet<DocumentoAnalizado[]>(`/api/analisis/${idExpediente}/documentos`)
}

/** Ícono "Guardar" por fila — `documento.idExpediente` ya viaja en el cuerpo. Devuelve el id real (resuelve un id temporal negativo). */
export function guardarDocumentoAnalizado(documento: DocumentoAnalizado) {
  return apiPost<number>('/api/analisis/documentos', documento)
}

/** Ícono "Eliminar" por fila — baja lógica. */
export function eliminarDocumentoAnalizado(idExpediente: number, idDocumentoAnalizado: number) {
  return apiDelete<void>(`/api/analisis/documentos/${idExpediente}/${idDocumentoAnalizado}`)
}

/** Descarga Word por fila (alcance simplificado, ver v3/CLAUDE.md). */
export async function descargarDocumentoWord(idExpediente: number, idDocumentoAnalizado: number, nombreSugerido: string) {
  const blob = await apiGetBlob(`/api/analisis/${idExpediente}/documentos/${idDocumentoAnalizado}/word`)
  descargarBlob(blob, nombreSugerido)
}

export interface ObservacionAnalisis {
  tipoObservacionCodigo: string
  tipoObservacionNombre: string
  descripcion: string
}

/** Payload de "Registrar resultado final" — bloque "Resultado del análisis". */
export interface AnalisisRegistro {
  idExpediente: number
  idExpedienteAnalisis: number | null
  resultadoCodigo: string
  resultadoNombre: string
  corresponde: boolean | null
  incorporado: boolean | null
  requiereReconstitucion: boolean
  tieneLegitimidad: boolean
  cumpleMediosProbatorios: boolean
  fundamento: string
  motivoNoCorrespondeCodigo: string
  tipoDocumentoNoCorrespondeCodigo: string
  tipoDocumentoNoCorrespondeNombre: string
  numeroDocumentoProveido: string
  observacion: ObservacionAnalisis | null
  documentosAnalizados: DocumentoAnalizado[]
}

/** Resultado YA REGISTRADO (repobla el formulario al reabrir el expediente). */
export interface AnalisisDetalle {
  registrado: boolean
  resultadoCodigo: string
  resultadoNombre: string
  corresponde: boolean | null
  incorporado: boolean | null
  requiereReconstitucion: boolean
  tieneLegitimidad: boolean
  cumpleMediosProbatorios: boolean
  fundamento: string
  motivoNoCorrespondeCodigo: string
  motivoNoCorrespondeNombre: string
  tipoDocumentoNoCorrespondeCodigo: string
  tipoDocumentoNoCorrespondeNombre: string
  numeroDocumentoProveido: string
  fechaEvaluacion: string | null
  observacion: ObservacionAnalisis | null
}

export function obtenerAnalisisRegistrado(idExpediente: number) {
  return apiGet<AnalisisDetalle>(`/api/analisis/${idExpediente}/resultado`)
}

/** Validación previa ("Registrar resultado final" antes del diálogo de confirmación) — devuelve mensajes, no lanza. */
export function validarRegistroAnalisis(registro: AnalisisRegistro) {
  return apiPost<string[]>('/api/analisis/resultado/validar', registro)
}

export function registrarAnalisis(registro: AnalisisRegistro) {
  return apiPost<AnalisisResultado>('/api/analisis/resultado', registro)
}

export function enviarVerificacion(idExpediente: number, comentario: string) {
  return apiPost<AnalisisResultado>('/api/analisis/verificacion', { idExpediente, comentario })
}

// Catálogos de la subpestaña "Análisis"
export function listarTiposDocumentoAdjuntoAnalisis() {
  return apiGet<CatalogoItem[]>('/api/catalogos/tipos-documento-adjunto-analisis')
}

/** Combo "Tipo documento" de la subpestaña "Ejecutar" (Bandeja Ejecución), 28/08/2026, pedido explícito del usuario. */
export function listarTiposDocumentoAdjuntoEjecucion() {
  return apiGet<CatalogoItem[]>('/api/catalogos/tipos-documento-adjunto-ejecucion')
}

/** Combo "Tipo documento" del panel "Registrar Asignación" (Bandeja Cartas de Respuesta), 28/08/2026, pedido explícito del usuario. */
export function listarTiposDocumentoAdjuntoCartaRespuesta() {
  return apiGet<CatalogoItem[]>('/api/catalogos/tipos-documento-adjunto-carta-respuesta')
}

export function listarEstadosDocumentoAnalisis() {
  return apiGet<CatalogoItem[]>('/api/catalogos/estados-documento-analisis')
}

export function listarResultadosAnalisis() {
  return apiGet<CatalogoItem[]>('/api/catalogos/resultados-analisis')
}

export function listarMotivosNoCorresponde() {
  return apiGet<CatalogoItem[]>('/api/catalogos/motivos-no-corresponde')
}

export function listarTiposObservacion() {
  return apiGet<CatalogoItem[]>('/api/catalogos/tipos-observacion')
}
