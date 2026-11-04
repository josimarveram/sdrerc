import { apiDelete, apiGet, apiGetBlob, apiPost, apiPostForm, apiPut } from './http'

export interface EquipoJuridicoAdmin {
  idEquipo: number
  idArea: number | null
  codigo: string
  nombre: string
  descripcion: string | null
  activo: boolean
  areaNombre: string | null
  idResponsable: number | null
  responsableNombre: string | null
  miembrosActivos: number
  abogadosActivos: number
  creadoEn: string | null
  modificadoEn: string | null
}

export interface AreaAdmin {
  idArea: number
  codigo: string
  nombre: string
  descripcion: string | null
  activo: boolean
}

export interface UsuarioAsignableEquipo {
  idUsuario: number
  username: string
  nombreCompleto: string
  rolesResumen: string | null
  activo: boolean
  abogado: boolean
  supervisor: boolean
}

export interface EquipoMiembroAdmin {
  idEquipoUsuario: number
  idEquipo: number
  idUsuario: number
  username: string
  nombreCompleto: string
  rolesResumen: string | null
  areaNombre: string | null
  usuarioActivo: boolean
  relacionActiva: boolean
  responsable: boolean
  abogado: boolean
  supervisor: boolean
  creadoEn: string | null
  modificadoEn: string | null
}

export interface AbogadoSupervisado {
  idUsuario: number
  username: string
  nombreCompleto: string
  rolesResumen: string | null
  equiposResumen: string | null
  usuarioActivo: boolean
  relacionActiva: boolean
  asignadoEn: string | null
}

export interface BuscarEquiposParams {
  texto?: string
  activo?: boolean
  idArea?: number
  idSupervisor?: number
  limite?: number
}

export function buscarEquipos(params: BuscarEquiposParams) {
  const query: Record<string, string> = {}
  if (params.texto) query.texto = params.texto
  if (params.activo !== undefined) query.activo = String(params.activo)
  if (params.idArea) query.idArea = String(params.idArea)
  if (params.idSupervisor) query.idSupervisor = String(params.idSupervisor)
  if (params.limite) query.limite = String(params.limite)
  return apiGet<EquipoJuridicoAdmin[]>('/api/admin/equipos', query)
}

export function obtenerEquipo(idEquipo: number) {
  return apiGet<EquipoJuridicoAdmin>(`/api/admin/equipos/${idEquipo}`)
}

export function listarAreasActivas() {
  return apiGet<AreaAdmin[]>('/api/admin/equipos/areas')
}

export function listarUsuariosAsignables() {
  return apiGet<UsuarioAsignableEquipo[]>('/api/admin/equipos/usuarios-asignables')
}

export function listarSupervisoresAsignables() {
  return apiGet<UsuarioAsignableEquipo[]>('/api/admin/equipos/supervisores-asignables')
}

export function listarMiembros(idEquipo: number) {
  return apiGet<EquipoMiembroAdmin[]>(`/api/admin/equipos/${idEquipo}/miembros`)
}

export interface GuardarEquipoJuridicoRequest {
  idEquipo: number | null
  idArea: number | null
  codigo: string
  nombre: string
  descripcion: string | null
  activo: boolean
  idResponsable: number | null
}

export function crearEquipo(request: GuardarEquipoJuridicoRequest) {
  return apiPost<EquipoJuridicoAdmin>('/api/admin/equipos', request)
}

export function editarEquipo(idEquipo: number, request: GuardarEquipoJuridicoRequest) {
  return apiPut<EquipoJuridicoAdmin>(`/api/admin/equipos/${idEquipo}`, request)
}

export function cambiarActivoEquipo(idEquipo: number, activo: boolean) {
  return apiPut<EquipoJuridicoAdmin>(`/api/admin/equipos/${idEquipo}/activo`, {}, { activo: String(activo) })
}

export function agregarMiembro(idEquipo: number, idUsuario: number) {
  return apiPost<void>(`/api/admin/equipos/${idEquipo}/miembros?idUsuario=${idUsuario}`, {})
}

export function quitarMiembro(idEquipo: number, idUsuario: number) {
  return apiDelete<void>(`/api/admin/equipos/${idEquipo}/miembros/${idUsuario}`)
}

export function marcarResponsable(idEquipo: number, idUsuario: number) {
  return apiPut<void>(`/api/admin/equipos/${idEquipo}/responsable`, {}, { idUsuario: String(idUsuario) })
}

export function listarSupervisoresConAbogados() {
  return apiGet<UsuarioAsignableEquipo[]>('/api/admin/equipos/supervisores')
}

export function listarAbogadosPorSupervisor(idSupervisor: number) {
  return apiGet<AbogadoSupervisado[]>(`/api/admin/equipos/supervisores/${idSupervisor}/abogados`)
}

export function agregarAbogadoASupervisor(idSupervisor: number, idAbogado: number) {
  return apiPost<void>(`/api/admin/equipos/supervisores/${idSupervisor}/abogados?idAbogado=${idAbogado}`, {})
}

export function quitarAbogadoDeSupervisor(idSupervisor: number, idAbogado: number) {
  return apiDelete<void>(`/api/admin/equipos/supervisores/${idSupervisor}/abogados/${idAbogado}`)
}

export async function descargarPlantillaSupervision() {
  return apiGetBlob('/api/admin/equipos/supervisores/plantilla-excel')
}

export interface EquipoSupervisionImportItem {
  fila: number
  usuarioAbogado: string
  usuarioSupervisor: string
  idAbogado: number | null
  nombreAbogado: string | null
  idSupervisor: number | null
  nombreSupervisor: string | null
  estadoValidacion: 'VALIDO' | 'ADVERTENCIA' | 'ERROR'
  mensaje: string | null
}

export function previsualizarExcelSupervision(archivo: File) {
  const form = new FormData()
  form.append('archivo', archivo)
  return apiPostForm<EquipoSupervisionImportItem[]>('/api/admin/equipos/supervisores/excel/previsualizar', form)
}

export interface EquipoSupervisionImportResultado {
  fila: number
  usuarioAbogado: string
  usuarioSupervisor: string
  resultado: 'IMPORTADO' | 'OMITIDO' | 'ERROR'
  mensaje: string | null
}

export function confirmarImportacionSupervision(filas: EquipoSupervisionImportItem[], incluirAdvertencias: boolean) {
  return apiPost<EquipoSupervisionImportResultado[]>('/api/admin/equipos/supervisores/excel/confirmar', {
    filas,
    incluirAdvertencias,
  })
}
