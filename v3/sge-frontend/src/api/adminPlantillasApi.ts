import { apiDelete, apiGet, apiGetBlob, apiPost, apiPostForm, apiPut } from './http'

export interface PlantillaDocumentoAdmin {
  idPlantillaDocumento: number | null
  idTipoDocumentoAdjunto: number
  tipoDocumentoCodigo: string
  tipoDocumentoNombre: string
  version: number
  nombreArchivo: string | null
  tamanoBytes: number
  comentario: string | null
  activo: boolean
  cargadoPor: string | null
  creadoEn: string | null
}

export interface PlantillaBloqueAdmin {
  idPlantillaBloque: number
  idTipoDocumentoAdjunto: number
  orden: number
  seccion: string | null
  titulo: string | null
  contenido: string
  variableCondicion: string | null
  operadorCondicion: 'COINCIDE' | 'NO_COINCIDE' | null
  valoresCondicion: string | null
  activo: boolean
}

export interface PlantillaVariable {
  nombre: string
  descripcion: string
  categoria: string
}

export function listarTiposConPlantilla() {
  return apiGet<PlantillaDocumentoAdmin[]>('/api/admin/plantillas')
}

/** Config firmada lista para pasarle a `new DocsAPI.DocEditor(id, config)` — ver VistaPreviaPlantillaDialog para la vista previa de solo lectura, este es el modo edición real. */
export function obtenerConfigOnlyOffice(idPlantillaDocumento: number) {
  return apiGet<Record<string, unknown>>(`/api/admin/plantillas/onlyoffice/${idPlantillaDocumento}/config`)
}

export function listarHistorial(idTipoDocumentoAdjunto: number) {
  return apiGet<PlantillaDocumentoAdmin[]>(`/api/admin/plantillas/${idTipoDocumentoAdjunto}/historial`)
}

export async function descargarPlantilla(idPlantillaDocumento: number, nombreSugerido: string) {
  const blob = await apiGetBlob(`/api/admin/plantillas/version/${idPlantillaDocumento}/descargar`)
  return { blob, nombreSugerido }
}

export function cargarNuevaVersion(idTipoDocumentoAdjunto: number, archivo: File, comentario: string) {
  const form = new FormData()
  form.append('archivo', archivo)
  if (comentario) form.append('comentario', comentario)
  return apiPostForm<{ id: number }>(`/api/admin/plantillas/${idTipoDocumentoAdjunto}/version`, form)
}

export function activarVersion(idPlantillaDocumento: number) {
  return apiPut<void>(`/api/admin/plantillas/version/${idPlantillaDocumento}/activar`, {})
}

export async function generarDocumentoPrueba(idTipoDocumentoAdjunto: number) {
  return apiGetBlob(`/api/admin/plantillas/${idTipoDocumentoAdjunto}/generar-prueba`)
}

export function listarVariablesDisponibles() {
  return apiGet<PlantillaVariable[]>('/api/admin/plantillas/variables')
}

export function listarBloques(idTipoDocumentoAdjunto: number) {
  return apiGet<PlantillaBloqueAdmin[]>(`/api/admin/plantillas/${idTipoDocumentoAdjunto}/bloques`)
}

export interface GuardarBloqueRequest {
  idPlantillaBloque: number | null
  idTipoDocumentoAdjunto: number
  seccion: string | null
  titulo: string | null
  contenido: string
  variableCondicion: string | null
  operadorCondicion: string | null
  valoresCondicion: string | null
}

export function crearBloque(request: GuardarBloqueRequest) {
  return apiPost<{ id: number }>('/api/admin/plantillas/bloques', request)
}

export function editarBloque(idPlantillaBloque: number, request: GuardarBloqueRequest) {
  return apiPut<{ id: number }>(`/api/admin/plantillas/bloques/${idPlantillaBloque}`, request)
}

export function eliminarBloque(idPlantillaBloque: number) {
  return apiDelete<void>(`/api/admin/plantillas/bloques/${idPlantillaBloque}`)
}

export function guardarOrdenBloques(idTipoDocumentoAdjunto: number, idsOrdenados: number[]) {
  return apiPut<void>(`/api/admin/plantillas/${idTipoDocumentoAdjunto}/bloques/orden`, { idsOrdenados })
}
