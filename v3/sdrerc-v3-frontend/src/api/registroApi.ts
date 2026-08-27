import { apiDelete, apiGet, apiPost, apiPut } from './http'
import { hoyISO } from '../utils/fechas'

export interface CatalogoItem {
  codigo: string | null
  nombre: string | null
  /** Solo poblado por el catálogo de tipos de documento de Análisis (`INTERMEDIO`/`FINAL`/null). */
  clasificacion?: string | null
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
  // Tipo de solicitante (14/08/2026, iniciativa "Solicitante con 3 tipos", ver v3/CLAUDE.md):
  // 'CIUDADANO' | 'CENTRO_ATENCION' | 'UNIDAD_ORGANICA'. CIUDADANO usa los campos ya existentes de
  // `remitente` (DatosPersonaRegistro); los otros 2 tipos no usan `remitente` en absoluto.
  // centroAtencionDenominacion/unidadOrganicaNombre son de solo lectura (para mostrar sin volver a
  // resolver el catálogo), no se envían al guardar.
  tipoSolicitante: string | null
  idCentroAtencion: number | null
  centroAtencionDenominacion: string | null
  idUnidadOrganica: number | null
  unidadOrganicaNombre: string | null
  // 4to tipo de solicitante "Municipalidad" (14/08/2026, ver v3/CLAUDE.md), mismo criterio que los 2
  // anteriores: idMunicipalidad solo aplica cuando tipoSolicitante='MUNICIPALIDAD',
  // municipalidadDenominacion es de solo lectura.
  idMunicipalidad: number | null
  municipalidadDenominacion: string | null
}

/** Catálogo de Solicitante tipo "Centro de Atención" (240 filas, script 102). */
export interface CentroAtencion {
  idCentroAtencion: number
  denominacion: string | null
  categorizacion: string | null
  departamento: string | null
  provincia: string | null
  distrito: string | null
  tipoLocal: string | null
  oficinaRegional: string | null
  codigoSio: string | null
}

/** Catálogo de Solicitante tipo "Unidad Orgánica" (7 valores fijos, script 101). */
export interface UnidadOrganica {
  idUnidadOrganica: number
  nombre: string
}

/** Catálogo de Solicitante tipo "Municipalidad" (1652 filas, script 104). */
export interface Municipalidad {
  idMunicipalidad: number
  denominacion: string | null
  tipo: string | null
  codigo: string | null
  departamento: string | null
  provincia: string | null
  distrito: string | null
  // Solo tiene valor real para tipo MUNICIPALIDAD DE CENTRO POBLADO/COMUNIDAD NATIVA (18/08/2026,
  // agregado para desambiguar homónimos reales del catálogo, ver RegistroExpedienteForm.tsx).
  centroPoblado: string | null
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
  // Campos estructurados (14/08/2026, Fase 3 de la iniciativa "separar Titular/Solicitante en 3
  // campos", ver v3/CLAUDE.md): solo aplican a persona natural (Titular siempre; Remitente cuando
  // su tipo de documento no es RUC). `nombreCompleto` se sigue calculando y enviando en base a
  // estos 3 campos ("PATERNO MATERNO, Nombres") para no romper validación/lectura ya existente —
  // ver `combinarNombreCompleto` en RegistroExpedienteForm.tsx. Persona jurídica (RUC) los deja en
  // null y sigue usando únicamente `nombreCompleto` como razón social.
  apellidoPaterno: string | null
  apellidoMaterno: string | null
  nombres: string | null
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
    apellidoPaterno: null,
    apellidoMaterno: null,
    nombres: null,
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
      // Fecha de solicitud con la fecha actual por defecto (13/08/2026, pedido explícito del
      // usuario) — sigue siendo editable, el usuario puede cambiarla si la solicitud es de otro
      // día. Mismo `hoyISO()` (fecha LOCAL, no UTC) que ya usan los filtros de búsqueda de las
      // bandejas, para no reintroducir el bug de "se adelanta un día" que ese helper corrigió.
      fechaRecepcion: hoyISO(),
      tipoSolicitudCodigo: null,
      tipoSolicitudNombre: null,
      tipoProcedimientoCodigo: null,
      tipoProcedimientoNombre: null,
      tipoDocumentoCodigo: null,
      tipoDocumentoNombre: null,
      canalCodigo: null,
      canalNombre: null,
      prioridad: 'NORMAL',
      // "Sí corresponde a la SDRERC" por defecto (18/08/2026, fix de bug reportado por el usuario):
      // el RadioGroup de RegistroExpedienteForm.tsx ya mostraba este valor seleccionado por defecto
      // (`value={solicitud.validacionInicial ?? CORRESPONDE}`), pero el estado real seguía en `null`
      // hasta que el usuario tocaba el radio a mano — al registrar sin tocarlo, el backend rechazaba
      // con "Seleccione el resultado de validación inicial." pese a verse marcado en pantalla.
      validacionInicial: 'Sí corresponde a la SDRERC',
      hojaEnvio: null,
      observacionInicial: null,
      grupoFamiliar: false,
      criterioGrupoFamiliar: null,
      observacionGrupoFamiliar: null,
      tipoSolicitante: 'CIUDADANO',
      idCentroAtencion: null,
      centroAtencionDenominacion: null,
      idUnidadOrganica: null,
      unidadOrganicaNombre: null,
      idMunicipalidad: null,
      municipalidadDenominacion: null,
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

export function listarCentrosAtencion() {
  return apiGet<CentroAtencion[]>('/api/catalogos/centros-atencion')
}

export function listarUnidadesOrganicas() {
  return apiGet<UnidadOrganica[]>('/api/catalogos/unidades-organicas')
}

export function listarMunicipalidades() {
  return apiGet<Municipalidad[]>('/api/catalogos/municipalidades')
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

/**
 * `desdeAnalisis` (21/08/2026, pedido explícito del usuario): omite server-side la exigencia de
 * etapa REGISTRO/estado REGISTRADO/sin asignación — habilita el lápiz de edición del panel "Datos"
 * dentro del módulo Análisis (un expediente ya recibido ahí nunca cumple esa condición de todas
 * formas). Ver `ExpedienteEdicionManualDAO`/`Service` (backend).
 */
export function obtenerExpedienteParaEdicion(idExpediente: number, desdeAnalisis = false) {
  return apiGet<ExpedienteEdicionManual>(`/api/registro/edicion/${idExpediente}`, desdeAnalisis ? { desdeAnalisis: 'true' } : undefined)
}

export function validarEdicionManual(registro: ExpedienteEdicionManual) {
  return apiPost<ExpedienteEdicionManualValidacionResponse>('/api/registro/edicion/validar', registro)
}

export function guardarEdicionManual(registro: ExpedienteEdicionManual, desdeAnalisis = false) {
  return apiPut<RegistroManualResultado>('/api/registro/edicion', registro, desdeAnalisis ? { desdeAnalisis: 'true' } : undefined)
}

/**
 * Baja lógica de un registro desde Registro/Recepción (13/08/2026, pedido explícito del usuario:
 * "misma acción/lógica que tenía el botón 'Eliminar' en la v2 del sistema"). Mismo backend que
 * `guardarEdicionManual` (`ExpedienteEdicionManualDAO`), exige que el expediente siga en
 * etapa REGISTRO/estado REGISTRADO/sin asignación — ver Javadoc del DAO en el backend para el
 * alcance exacto (no incluye la limpieza en cascada de alertas de solicitudes relacionadas que sí
 * hace V2, diferida explícitamente).
 */
export function eliminarExpedienteRegistro(idExpediente: number) {
  return apiDelete<RegistroManualResultado>(`/api/registro/edicion/${idExpediente}`)
}

/**
 * Info de impacto para el modal de confirmación de eliminación (13/08/2026, pedido explícito del
 * usuario): si el expediente tiene hoy alertas activas "Potencial duplicado"/"Posible Grupo
 * Familiar", y si su titular ya pertenece a un grupo familiar confirmado — para avisar antes de
 * confirmar que, al eliminarlo, el backend también actualizará esos registros relacionados en
 * cascada (ver `ExpedienteEdicionManualDAO.eliminar`/`obtenerImpactoEliminacion`, backend).
 */
export interface ImpactoEliminacionRegistro {
  potencialDuplicado: boolean
  posibleGrupoFamiliar: boolean
  grupoFamiliarConfirmado: boolean
  // 20/08/2026, pedido explícito del usuario (regla de "tamaño mínimo de grupo"): tamaño ACTUAL
  // (calculado dinámicamente por el backend, no cacheado) del vínculo correspondiente — 0 cuando el
  // booleano hermano es falso. Con 2 o menos, eliminar este expediente disuelve el vínculo por
  // completo (también afecta a la otra solicitud); con 3 o más, solo retira a este expediente y las
  // demás conservan su alerta/pertenencia sin cambios.
  tamanoPosibleGrupoFamiliar: number
  tamanoGrupoFamiliarConfirmado: number
  sinImpacto: boolean
}

export function obtenerImpactoEliminacionRegistro(idExpediente: number) {
  return apiGet<ImpactoEliminacionRegistro>(`/api/registro/edicion/${idExpediente}/impacto-eliminacion`)
}
