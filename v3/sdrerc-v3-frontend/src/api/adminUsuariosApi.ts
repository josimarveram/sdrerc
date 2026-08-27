import { apiGet, apiPost, apiPut } from './http'
import type { EquipoAsignacion } from './asignacionApi'

export type { EquipoAsignacion }

export interface UsuarioAdmin {
  idUsuario: number
  username: string
  nombreCompleto: string
  tipoDocumento: string | null
  numeroDocumento: string | null
  correo: string | null
  telefono: string | null
  estado: string
  activo: boolean
  rolesResumen: string | null
  idsRoles: number[]
  equiposResumen: string | null
  idsEquipos: number[]
  creadoEn: string | null
  modificadoEn: string | null
}

export interface RolResumen {
  idRol: number
  codigo: string
  nombre: string
}

export interface BuscarUsuariosParams {
  texto?: string
  activo?: boolean
  idRol?: number
  idEquipo?: number
  limite?: number
}

export function buscarUsuarios(params: BuscarUsuariosParams) {
  const query: Record<string, string> = {}
  if (params.texto) query.texto = params.texto
  if (params.activo !== undefined) query.activo = String(params.activo)
  if (params.idRol) query.idRol = String(params.idRol)
  if (params.idEquipo) query.idEquipo = String(params.idEquipo)
  if (params.limite) query.limite = String(params.limite)
  return apiGet<UsuarioAdmin[]>('/api/admin/usuarios', query)
}

export function obtenerUsuario(idUsuario: number) {
  return apiGet<UsuarioAdmin>(`/api/admin/usuarios/${idUsuario}`)
}

export function listarRolesActivos() {
  return apiGet<RolResumen[]>('/api/admin/usuarios/roles')
}

export function listarEquiposActivos() {
  return apiGet<EquipoAsignacion[]>('/api/admin/usuarios/equipos')
}

export interface GuardarUsuarioRequest {
  idUsuario: number | null
  username: string
  nombreCompleto: string
  tipoDocumento: string | null
  numeroDocumento: string | null
  correo: string | null
  telefono: string | null
  activo: boolean
  idsRoles: number[]
  idsEquipos: number[]
}

export function crearUsuario(request: GuardarUsuarioRequest) {
  return apiPost<UsuarioAdmin>('/api/admin/usuarios', request)
}

export function editarUsuario(idUsuario: number, request: GuardarUsuarioRequest) {
  return apiPut<UsuarioAdmin>(`/api/admin/usuarios/${idUsuario}`, request)
}

export function cambiarActivoUsuario(idUsuario: number, activo: boolean) {
  return apiPut<UsuarioAdmin>(`/api/admin/usuarios/${idUsuario}/activo`, {}, { activo: String(activo) })
}

export interface RestablecerClaveRequest {
  passwordTemporal: string
  reiniciarVerificacionDosPasos: boolean
}

export function restablecerClave(idUsuario: number, request: RestablecerClaveRequest) {
  return apiPost<void>(`/api/admin/usuarios/${idUsuario}/restablecer-clave`, request)
}
