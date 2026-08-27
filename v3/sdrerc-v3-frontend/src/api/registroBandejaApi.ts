import { apiGet } from './http'
import type { ExpedienteEdicionManual } from './registroApi'

export interface ExpedienteBandeja {
  idExpediente: number
  numeroExpediente: string
  numeroTramiteDocumentario: string
  numeroExpedienteSgd: string
  etapaCodigo: string
  estadoCodigo: string
  abogadoInicial: string
  responsableActual: string
  equipoActual: string
  fechaRecepcion: string | null // yyyy-MM-dd
  fechaRegistro: string | null
  fechaUltimoMovimiento: string | null
  fechaVencimiento: string | null
  requierePublicacion: boolean
  expedienteDigitalCompleto: boolean
  canal: string
  procedimiento: string
  tipoActa: string
  numeroActa: string
  grupoFamiliar: string
  alertas: string
  titular: string
  relacionesConfirmadasComoPrincipal: number
  relacionadoComoHijo: boolean
  grupoFamiliarConfirmado: boolean
  diasRestantes: number | null
}

export interface BuscarBandejaRegistroParams {
  texto?: string
  estado?: string
  desde?: string
  hasta?: string
  limite?: number
}

// 18/08/2026, pedido explícito del usuario: sin límite de resultados por defecto — antes esta
// función siempre mandaba `limite=200` aunque el campo ya no estuviera en el buscador (la UI lo
// quitó, pero el wrapper seguía forzando el default). Ahora solo se envía `limite` si el llamador
// lo pasa explícitamente; si no, el backend devuelve todas las filas que califiquen por los demás
// filtros (ver Javadoc de `RegistroBandejaController`/`ExpedienteBandejaDAO.buscar`, backend).
export function buscarBandejaRegistro(params: BuscarBandejaRegistroParams) {
  const query: Record<string, string> = {}
  if (params.texto) query.texto = params.texto
  if (params.estado) query.estado = params.estado
  if (params.desde) query.desde = params.desde
  if (params.hasta) query.hasta = params.hasta
  if (params.limite) query.limite = String(params.limite)
  return apiGet<ExpedienteBandeja[]>('/api/registro/bandeja', query)
}

/** Panel "Datos" (solo lectura, no exige que el expediente sea editable). */
export function obtenerDatosExpediente(idExpediente: number) {
  return apiGet<ExpedienteEdicionManual>(`/api/registro/bandeja/${idExpediente}/datos`)
}
