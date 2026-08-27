import { useEffect, useState } from 'react'
import { Alert, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material'
import { listarConfirmados, type ExpedienteRelacionado } from '../api/expedienteRelacionadoApi'
import { ApiError } from '../api/http'
import { StatusBadge, alertaBadgeTone } from './StatusBadge'

interface AsociadosExpedienteContentProps {
  idExpediente: number
}

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

/**
 * Subpestaña "Duplicados / Asociados" del "Expediente 360°" (Bandeja de Expedientes, 24/08/2026).
 * Reutiliza literalmente `listarConfirmados` (mismo endpoint ya construido y usado por
 * Registro/Asignación/Análisis) — de solo lectura acá, sin botón "Asociar" (esa acción sigue siendo
 * responsabilidad de Registro/Asignación).
 */
export function AsociadosExpedienteContent({ idExpediente }: AsociadosExpedienteContentProps) {
  const [asociados, setAsociados] = useState<ExpedienteRelacionado[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    listarConfirmados(idExpediente)
      .then(setAsociados)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudieron cargar los expedientes asociados.'))
      .finally(() => setLoading(false))
  }, [idExpediente])

  if (error) return <Alert severity="error">{error}</Alert>
  if (loading) return <Typography variant="body2" color="text.secondary">Cargando...</Typography>
  if (asociados.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        Este expediente no tiene duplicados/asociados confirmados.
      </Typography>
    )
  }

  return (
    <TableContainer>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>N° Expediente</TableCell>
            <TableCell>N° SGD</TableCell>
            <TableCell>Titular</TableCell>
            <TableCell>Etapa / Estado</TableCell>
            <TableCell>Alerta</TableCell>
            <TableCell>Fecha de relación</TableCell>
            <TableCell>Registrado por</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {asociados.map((a) => (
            <TableRow key={a.idExpediente}>
              <TableCell>{a.numeroExpediente || '-'}</TableCell>
              <TableCell>{a.numeroExpedienteSgd || '-'}</TableCell>
              <TableCell>{a.titular || '-'}</TableCell>
              <TableCell>{a.etapaEstado || '-'}</TableCell>
              <TableCell>
                {a.alertaIngreso ? <StatusBadge label={a.alertaIngreso} tone={alertaBadgeTone(a.alertaIngreso)} /> : '-'}
              </TableCell>
              <TableCell>{formatearFecha(a.fechaRelacion)}</TableCell>
              <TableCell>{a.usuarioRelacion || '-'}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
