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
  // Solicitante con 4 tipos (14/08/2026, ver v3/CLAUDE.md) — sin columna propia en el Excel: el
  // backend identifica el tipo automáticamente a partir de `remitente` ("SOLICITADO POR") contra
  // los catálogos CENTROS_ATENCION/UNIDADES_ORGANICAS/MUNICIPALIDADES durante "Validar" (coincidencia
  // por contención normalizada). `tipoSolicitante` SÍ es editable manualmente (combo, 18/08/2026,
  // pedido explícito del usuario) para corregir una clasificación automática incorrecta — al editarlo
  // se debe marcar `tipoSolicitanteManual=true` para que el backend no lo recalcule en el siguiente
  // "Validar" (ver CargaDiariaReglasService.clasificarTipoSolicitante). Los demás campos de este
  // bloque (idCentroAtencion/centroAtencionDenominacion/idUnidadOrganica/unidadOrganicaNombre/
  // idMunicipalidad/municipalidadDenominacion) siguen siendo de SOLO LECTURA — no hay forma de elegir
  // manualmente la entidad específica desde esta grilla, solo la categoría.
  tipoSolicitante: string | null
  tipoSolicitanteManual: boolean
  idCentroAtencion: number | null
  centroAtencionDenominacion: string | null
  idUnidadOrganica: number | null
  unidadOrganicaNombre: string | null
  idMunicipalidad: number | null
  municipalidadDenominacion: string | null
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
