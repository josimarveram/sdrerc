import { apiGet, apiPost, apiPut } from './http'

export interface PlazoConfiguracion {
  idPlazoConfiguracion: number | null
  codigo: string
  nombre: string
  ambito: string | null
  diasPlazo: number
  unidadPlazo: string
  porcentajeVerdeDesde: number | null
  porcentajeAmarilloDesde: number | null
  porcentajeRojoDesde: number | null
  fechaVigenciaDesde: string | null
  fechaVigenciaHasta: string | null
  activo: boolean
  observacion: string | null
  idEtapaExpediente?: number | null
  etapaNombre?: string | null
  idTipoDocumentoAdjunto?: number | null
  tipoDocumentoNombre?: string | null
  creadoEn?: string | null
  modificadoEn?: string | null
}

export interface BuscarPlazosParams {
  texto?: string
  activo?: boolean
  limite?: number
}

export function buscarPlazos(params: BuscarPlazosParams) {
  const query: Record<string, string> = {}
  if (params.texto) query.texto = params.texto
  if (params.activo !== undefined) query.activo = String(params.activo)
  if (params.limite) query.limite = String(params.limite)
  return apiGet<PlazoConfiguracion[]>('/api/admin/plazos', query)
}

export function obtenerPlazoSolicitud() {
  return apiGet<PlazoConfiguracion>('/api/admin/plazos/solicitud')
}

export interface GuardarPlazoRequest {
  idPlazoConfiguracion: number | null
  codigo: string
  nombre: string
  ambito: string | null
  diasPlazo: number
  unidadPlazo: string
  porcentajeVerdeDesde: number | null
  porcentajeAmarilloDesde: number | null
  porcentajeRojoDesde: number | null
  fechaVigenciaDesde: string | null
  fechaVigenciaHasta: string | null
  activo: boolean
  observacion: string | null
}

export function crearPlazo(request: GuardarPlazoRequest) {
  return apiPost<PlazoConfiguracion>('/api/admin/plazos', request)
}

export function editarPlazo(idPlazoConfiguracion: number, request: GuardarPlazoRequest) {
  return apiPut<PlazoConfiguracion>(`/api/admin/plazos/${idPlazoConfiguracion}`, request)
}

export function cambiarActivoPlazo(idPlazoConfiguracion: number, activo: boolean) {
  return apiPut<PlazoConfiguracion>(`/api/admin/plazos/${idPlazoConfiguracion}/activo`, {}, { activo: String(activo) })
}

export const UNIDADES_PLAZO = ['HABILES', 'CALENDARIO'] as const
