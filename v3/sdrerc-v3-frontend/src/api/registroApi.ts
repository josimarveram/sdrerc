import { apiGet, apiPost, apiPut } from './http'

export interface CatalogoItem {
  codigo: string | null
  nombre: string | null
}

export interface UbigeoItem {
  id: number
  codigo: string
  nombre: string
  idPadre: number | null
}

export interface DatosSolicitud {
  numeroTramite: string | null
  numeroDocumento: string | null
  numeroExpedienteSgd: string | null
  fechaRecepcion: string | null // yyyy-MM-dd
  tipoSolicitudCodigo: string | null
  tipoSolicitudNombre: string | null
  tipoProcedimientoCodigo: string | null
  tipoProcedimientoNombre: string | null
  tipoDocumentoCodigo: string | null
  tipoDocumentoNombre: string | null
  canalCodigo: string | null
  canalNombre: string | null
  prioridad: string | null
  validacionInicial: string | null
  hojaEnvio: string | null
  observacionInicial: string | null
  grupoFamiliar: boolean
  criterioGrupoFamiliar: string | null
  observacionGrupoFamiliar: string | null
}

export interface DatosActa {
  tipoActaCodigo: string | null
  tipoActaNombre: string | null
  numeroActa: string | null
  anioActa: number | null
  ubicacionRegistral: string | null
  origenRegistral: string | null
  observacion: string | null
}

export interface DatosPersonaRegistro {
  nombreCompleto: string | null
  tipoDocumento: string | null
  numeroDocumento: string | null
  telefono: string | null
  correo: string | null
  direccion: string | null
  idDepartamento: number | null
  idProvincia: number | null
  idDistrito: number | null
  departamento: string | null
  provincia: string | null
  distrito: string | null
  tipoRemitente: string | null
  observacion: string | null
}

export interface RegistroManualExpediente {
  solicitud: DatosSolicitud
  acta: DatosActa
  titular: DatosPersonaRegistro
  remitente: DatosPersonaRegistro
  observacionesGenerales: string | null
  numeroExpedienteVistaPrevia: string | null
  posibleDuplicado: boolean
  motivoDuplicado: string | null
  numeroExpedienteSgdDuplicado: boolean
  motivoNumeroExpedienteSgdDuplicado: string | null
}

export interface RegistroManualValidacionResponse {
  mensajes: string[]
  registro: RegistroManualExpediente
}

export interface RegistroManualResultado {
  idExpediente: number
  numeroExpediente: string
  mensaje: string
}

export function personaVacia(): DatosPersonaRegistro {
  return {
    nombreCompleto: null,
    tipoDocumento: null,
    numeroDocumento: null,
    telefono: null,
    correo: null,
    direccion: null,
    idDepartamento: null,
    idProvincia: null,
    idDistrito: null,
    departamento: null,
    provincia: null,
    distrito: null,
    tipoRemitente: null,
    observacion: null,
  }
}

export function registroVacio(): RegistroManualExpediente {
  return {
    solicitud: {
      numeroTramite: null,
      numeroDocumento: null,
      numeroExpedienteSgd: null,
      fechaRecepcion: null,
      tipoSolicitudCodigo: null,
      tipoSolicitudNombre: null,
      tipoProcedimientoCodigo: null,
      tipoProcedimientoNombre: null,
      tipoDocumentoCodigo: null,
      tipoDocumentoNombre: null,
      canalCodigo: null,
      canalNombre: null,
      prioridad: 'NORMAL',
      validacionInicial: null,
      hojaEnvio: null,
      observacionInicial: null,
      grupoFamiliar: false,
      criterioGrupoFamiliar: null,
      observacionGrupoFamiliar: null,
    },
    acta: {
      tipoActaCodigo: null,
      tipoActaNombre: null,
      numeroActa: null,
      anioActa: null,
      ubicacionRegistral: null,
      origenRegistral: null,
      observacion: null,
    },
    titular: personaVacia(),
    remitente: personaVacia(),
    observacionesGenerales: null,
    numeroExpedienteVistaPrevia: null,
    posibleDuplicado: false,
    motivoDuplicado: null,
    numeroExpedienteSgdDuplicado: false,
    motivoNumeroExpedienteSgdDuplicado: null,
  }
}

export function listarCanalesRecepcion() {
  return apiGet<CatalogoItem[]>('/api/catalogos/canales-recepcion')
}

export function listarProcedimientosRegistrales() {
  return apiGet<CatalogoItem[]>('/api/catalogos/procedimientos-registrales')
}

export function listarTiposDocumento() {
  return apiGet<CatalogoItem[]>('/api/catalogos/tipos-documento')
}

export function listarTiposActa() {
  return apiGet<CatalogoItem[]>('/api/catalogos/tipos-acta')
}

export function listarDepartamentos() {
  return apiGet<UbigeoItem[]>('/api/ubigeo/departamentos')
}

export function listarProvincias(idDepartamento: number) {
  return apiGet<UbigeoItem[]>('/api/ubigeo/provincias', { idDepartamento: String(idDepartamento) })
}

export function listarDistritos(idProvincia: number) {
  return apiGet<UbigeoItem[]>('/api/ubigeo/distritos', { idProvincia: String(idProvincia) })
}

export function validarRegistroManual(registro: RegistroManualExpediente) {
  return apiPost<RegistroManualValidacionResponse>('/api/registro/manual/validar', registro)
}

export function registrarManual(registro: RegistroManualExpediente) {
  return apiPost<RegistroManualResultado>('/api/registro/manual', registro)
}

// Edición manual: mismos campos que RegistroManualExpediente + identificación del expediente ya
// registrado (com.sdrerc.v3.domain.ExpedienteEdicionManualDTO, backend).
export interface ExpedienteEdicionManual extends RegistroManualExpediente {
  idExpediente: number
  numeroExpediente: string | null
  etapaCodigo: string | null
  estadoCodigo: string | null
}

export interface ExpedienteEdicionManualValidacionResponse {
  mensajes: string[]
  registro: ExpedienteEdicionManual
}

export function obtenerExpedienteParaEdicion(idExpediente: number) {
  return apiGet<ExpedienteEdicionManual>(`/api/registro/edicion/${idExpediente}`)
}

export function validarEdicionManual(registro: ExpedienteEdicionManual) {
  return apiPost<ExpedienteEdicionManualValidacionResponse>('/api/registro/edicion/validar', registro)
}

export function guardarEdicionManual(registro: ExpedienteEdicionManual) {
  return apiPut<RegistroManualResultado>('/api/registro/edicion', registro)
}
