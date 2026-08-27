import { apiGet, apiPost, apiPut } from './http'

export interface RolAdmin {
  idRol: number
  codigo: string
  nombre: string
  descripcion: string | null
  activo: boolean
  usuariosAsociados: number
  permisosAsociados: number
  creadoEn: string | null
  modificadoEn: string | null
}

export interface PermisoAdmin {
  idPermiso: number
  codigo: string
  nombre: string
  modulo: string
  activo: boolean
  asignado: boolean
}

export interface BuscarRolesParams {
  texto?: string
  activo?: boolean
  limite?: number
}

export function buscarRoles(params: BuscarRolesParams) {
  const query: Record<string, string> = {}
  if (params.texto) query.texto = params.texto
  if (params.activo !== undefined) query.activo = String(params.activo)
  if (params.limite) query.limite = String(params.limite)
  return apiGet<RolAdmin[]>('/api/admin/roles', query)
}

export function obtenerRol(idRol: number) {
  return apiGet<RolAdmin>(`/api/admin/roles/${idRol}`)
}

export function listarPermisosDelRol(idRol: number) {
  return apiGet<PermisoAdmin[]>(`/api/admin/roles/${idRol}/permisos`)
}

export interface GuardarRolRequest {
  idRol: number | null
  codigo: string
  nombre: string
  descripcion: string | null
  activo: boolean
}

export function crearRol(request: GuardarRolRequest) {
  return apiPost<RolAdmin>('/api/admin/roles', request)
}

export function editarRol(idRol: number, request: GuardarRolRequest) {
  return apiPut<RolAdmin>(`/api/admin/roles/${idRol}`, request)
}

export function cambiarActivoRol(idRol: number, activo: boolean) {
  return apiPut<RolAdmin>(`/api/admin/roles/${idRol}/activo`, {}, { activo: String(activo) })
}

export function guardarPermisosRol(idRol: number, idsPermiso: number[]) {
  return apiPut<void>(`/api/admin/roles/${idRol}/permisos`, { idsPermiso })
}
