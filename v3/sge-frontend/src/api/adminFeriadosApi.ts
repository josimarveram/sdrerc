import { apiGet, apiPost, apiPostForm, apiPut } from './http'

export interface FeriadoAdmin {
  idFeriado: number | null
  fecha: string
  nombre: string
  tipo: string
  activo: boolean
  observacion: string | null
  creadoEn: string | null
  modificadoEn: string | null
}

export interface BuscarFeriadosParams {
  anio?: number
  activo?: boolean
  limite?: number
}

export function buscarFeriados(params: BuscarFeriadosParams) {
  const query: Record<string, string> = {}
  if (params.anio) query.anio = String(params.anio)
  if (params.activo !== undefined) query.activo = String(params.activo)
  if (params.limite) query.limite = String(params.limite)
  return apiGet<FeriadoAdmin[]>('/api/admin/feriados', query)
}

export interface GuardarFeriadoRequest {
  idFeriado: number | null
  fecha: string
  nombre: string
  tipo: string
  activo: boolean
  observacion: string | null
}

export function crearFeriado(request: GuardarFeriadoRequest) {
  return apiPost<FeriadoAdmin>('/api/admin/feriados', request)
}

export function editarFeriado(idFeriado: number, request: GuardarFeriadoRequest) {
  return apiPut<FeriadoAdmin>(`/api/admin/feriados/${idFeriado}`, request)
}

export function cambiarActivoFeriado(idFeriado: number, activo: boolean) {
  return apiPut<FeriadoAdmin>(`/api/admin/feriados/${idFeriado}/activo`, {}, { activo: String(activo) })
}

export function importarFeriadosXml(archivo: File, anio: number) {
  const form = new FormData()
  form.append('archivo', archivo)
  return apiPostForm<{ importados: number }>(`/api/admin/feriados/importar-xml?anio=${anio}`, form)
}
