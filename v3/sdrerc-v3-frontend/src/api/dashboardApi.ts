import { apiGet } from './http'

export interface DashboardResumen {
  activos: number
  vencidos: number
  porVencer: number
  ingresadosPeriodo: number
  cerradosPeriodo: number
}

export interface DashboardConteo {
  etiqueta: string
  total: number
}

export interface DashboardTendenciaMensual {
  mes: string // ISO yyyy-MM-dd (dia 1 del mes)
  ingresados: number
  cerrados: number
}

export interface CargaLaboralAbogado {
  idUsuario: number
  abogado: string
  supervisor: string
  analisisPorRecibir: number
  analisisEnProceso: number
  analisisObservado: number
  analisisCartaIntermedia: number
  enVerificacion: number
  enEjecucion: number
  porVencer: number
  vencidos: number
}

export interface DashboardData {
  resumen: DashboardResumen
  porEtapa: DashboardConteo[]
  resultadosAnalisis: DashboardConteo[]
  cargaAbogados: CargaLaboralAbogado[]
  tendenciaMensual: DashboardTendenciaMensual[]
  estadoNotificacion: DashboardConteo[]
}

/** desde/hasta en formato yyyy-MM-dd; si se omiten, el backend usa dia 1 del mes actual -> hoy. */
export function obtenerDashboard(desde?: string, hasta?: string) {
  const params: Record<string, string> = {}
  if (desde) params.desde = desde
  if (hasta) params.hasta = hasta
  return apiGet<DashboardData>('/api/dashboard', params)
}
