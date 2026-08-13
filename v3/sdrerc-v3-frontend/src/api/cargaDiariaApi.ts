import { apiGetBlob, apiPost, apiPostBlob, apiPostForm } from './http'

// Mismos campos que com.sdrerc.v3.domain.CargaDiariaPreviewDTO (backend), una fila de la
// previsualización de Carga diaria.
export interface CargaDiariaPreview {
  fila: number
  numeroTramite: string | null
  numeroDocumento: string | null
  tipoProcedimiento: string | null
  tipoSolicitud: string | null
  tipoDocumento: string | null
  tipoActa: string | null
  numeroActa: string | null
  titular: string | null
  remitente: string | null
  canalRecepcion: string | null
  numeroExpedienteSgd: string | null
  tipoDocumentoIdentidadSolicitante: string | null
  numeroDocumentoIdentidadSolicitante: string | null
  tipoDocumentoIdentidadTitular: string | null
  numeroDocumentoIdentidadTitular: string | null
  grupoFamiliar: boolean
  posibleGrupoFamiliar: boolean
  criterioGrupoFamiliar: string | null
  observacionGrupoFamiliar: string | null
  fechaRecepcion: string | null
  fechaRecepcionTexto: string | null
  observacionInicial: string | null
  estadoValidacion: string | null
  mensajeValidacion: string | null
  posibleDuplicado: boolean
  motivoDuplicado: string | null
  motivoSinNumero: string | null
  numeroExpedienteGenerado: string | null
  listoParaRegistrar: boolean
  registrado: boolean
  idExpedienteRegistrado: number | null
}

export interface CargaDiariaResultado {
  registrados: number
  omitidos: number
  mensaje: string
  registros: CargaDiariaPreview[]
}

export function previsualizarCargaDiaria(archivo: File) {
  const form = new FormData()
  form.append('archivo', archivo)
  return apiPostForm<CargaDiariaPreview[]>('/api/registro/carga-diaria/previsualizar', form)
}

export function confirmarCargaDiaria(registros: CargaDiariaPreview[]) {
  return apiPost<CargaDiariaResultado>('/api/registro/carga-diaria/confirmar', registros)
}

/** Re-ejecuta las reglas de validación (duplicados, grupo familiar, número) tras editar celdas. */
export function validarCargaDiaria(registros: CargaDiariaPreview[]) {
  return apiPost<CargaDiariaPreview[]>('/api/registro/carga-diaria/validar', registros)
}

export function exportarCargaDiaria(registros: CargaDiariaPreview[]) {
  return apiPostBlob('/api/registro/carga-diaria/exportar', registros)
}

export function descargarPlantillaCargaDiaria() {
  return apiGetBlob('/api/registro/carga-diaria/plantilla')
}
