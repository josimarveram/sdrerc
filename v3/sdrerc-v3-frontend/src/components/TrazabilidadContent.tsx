import { useEffect, useMemo, useState } from 'react'
import { Alert, Box, Paper, Stack, ToggleButton, ToggleButtonGroup, Typography } from '@mui/material'
import { listarTrazabilidad, type ExpedienteTimelineEvento } from '../api/expedienteConsolaApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { StatusBadge } from './StatusBadge'

interface TrazabilidadContentProps {
  idExpediente: number
}

type Filtro = 'TODOS' | 'EXPEDIENTE' | 'DOCUMENTOS'

function formatearFechaHora(iso: string | null): string {
  if (!iso) return '-'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '-'
  return d.toLocaleString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function flecha(origenCodigo: string, origenNombre: string, destinoCodigo: string, destinoNombre: string): string {
  const origen = origenNombre || origenCodigo
  const destino = destinoNombre || destinoCodigo
  if (!origen && !destino) return ''
  if (!origen) return destino
  if (!destino) return origen
  return `${origen} → ${destino}`
}

/**
 * Subpestaña "Trazabilidad" del "Expediente 360°" (Bandeja de Expedientes, 24/08/2026). Línea de
 * tiempo VERTICAL (mejora explícita sobre V2: `DlgConsolaExpedienteV2#cargarTimeline` solo pinta
 * una tabla plana de 5 columnas) sobre `EXPEDIENTE_HISTORIAL` completo — desde el registro hasta
 * hoy, cualquier etapa, sin filtrar por visibilidad de asignación (mismo criterio que V2).
 *
 * Filtro Todos/Solo expediente/Solo documentos (mejora explícita, sin equivalente en V2, ver
 * Javadoc de `ExpedienteTimelineDTO` en el backend): un mismo evento puede pertenecer al expediente
 * en sí o a un documento puntual (`tablaRelacionada==='EXPEDIENTE_DOCUMENTO_ANALIZADO'`) — responde
 * al pedido explícito del usuario de ver también "el historial de los documentos que lo contiene".
 */
export function TrazabilidadContent({ idExpediente }: TrazabilidadContentProps) {
  const [eventos, setEventos] = useState<ExpedienteTimelineEvento[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [filtro, setFiltro] = useState<Filtro>('TODOS')

  useEffect(() => {
    setLoading(true)
    setError(null)
    listarTrazabilidad(idExpediente)
      .then(setEventos)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudo cargar la trazabilidad del expediente.'))
      .finally(() => setLoading(false))
  }, [idExpediente])

  const eventosFiltrados = useMemo(() => {
    if (filtro === 'TODOS') return eventos
    const esDocumento = filtro === 'DOCUMENTOS'
    return eventos.filter((e) => (e.tablaRelacionada === 'EXPEDIENTE_DOCUMENTO_ANALIZADO') === esDocumento)
  }, [eventos, filtro])

  const totalDocumentos = useMemo(() => eventos.filter((e) => e.tablaRelacionada === 'EXPEDIENTE_DOCUMENTO_ANALIZADO').length, [eventos])
  const totalExpediente = eventos.length - totalDocumentos

  return (
    <Stack spacing={2}>
      {error && <Alert severity="error">{error}</Alert>}
      <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
        <ToggleButtonGroup size="small" exclusive value={filtro} onChange={(_e, v) => v && setFiltro(v)}>
          <ToggleButton value="TODOS">Todos ({eventos.length})</ToggleButton>
          <ToggleButton value="EXPEDIENTE">Del expediente ({totalExpediente})</ToggleButton>
          <ToggleButton value="DOCUMENTOS">De documentos ({totalDocumentos})</ToggleButton>
        </ToggleButtonGroup>
      </Stack>

      {loading ? (
        <Typography variant="body2" color="text.secondary">
          Cargando trazabilidad...
        </Typography>
      ) : eventosFiltrados.length === 0 ? (
        <Typography variant="body2" color="text.secondary">
          Sin movimientos registrados para este filtro.
        </Typography>
      ) : (
        <Stack sx={{ position: 'relative', pl: 3 }}>
          <Box
            sx={{
              position: 'absolute',
              left: 9,
              top: 8,
              bottom: 8,
              width: 2,
              bgcolor: sdrercColors.softGray,
            }}
          />
          {eventosFiltrados.map((evento) => {
            const esDocumento = evento.tablaRelacionada === 'EXPEDIENTE_DOCUMENTO_ANALIZADO'
            const color = esDocumento ? sdrercColors.info : sdrercColors.primary
            return (
              <Box key={evento.idExpedienteHistorial} sx={{ position: 'relative', pb: 2.5 }}>
                <Box
                  sx={{
                    position: 'absolute',
                    left: -24,
                    top: 4,
                    width: 12,
                    height: 12,
                    borderRadius: '50%',
                    bgcolor: color,
                    border: '2px solid white',
                    boxShadow: `0 0 0 2px ${color}`,
                  }}
                />
                <Paper variant="outlined" sx={{ p: 1.5, borderRadius: 2 }}>
                  <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap', mb: 0.5 }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                      {evento.movimientoNombre || evento.movimientoCodigo}
                    </Typography>
                    <StatusBadge label={esDocumento ? 'Documento' : 'Expediente'} tone={esDocumento ? 'info' : 'neutral'} />
                    <Typography variant="caption" color="text.secondary">
                      {formatearFechaHora(evento.fechaMovimiento)}
                    </Typography>
                  </Stack>
                  {(evento.etapaOrigenCodigo || evento.etapaDestinoCodigo) && (
                    <Typography variant="body2" color="text.secondary">
                      Etapa: {flecha(evento.etapaOrigenCodigo, evento.etapaOrigenNombre, evento.etapaDestinoCodigo, evento.etapaDestinoNombre)}
                    </Typography>
                  )}
                  {(evento.estadoOrigenCodigo || evento.estadoDestinoCodigo) && (
                    <Typography variant="body2" color="text.secondary">
                      Estado: {flecha(evento.estadoOrigenCodigo, evento.estadoOrigenNombre, evento.estadoDestinoCodigo, evento.estadoDestinoNombre)}
                    </Typography>
                  )}
                  <Typography variant="body2" color="text.secondary">
                    {evento.usuarioDestino ? `Realizado por: ${evento.usuarioDestino}` : evento.usuarioOrigen ? `Realizado por: ${evento.usuarioOrigen}` : 'Sin usuario registrado'}
                    {evento.equipoDestino ? ` · Equipo: ${evento.equipoDestino}` : ''}
                  </Typography>
                  {evento.comentario && (
                    <Typography variant="body2" sx={{ mt: 0.5, fontStyle: 'italic' }}>
                      {evento.comentario}
                    </Typography>
                  )}
                  {evento.motivo && evento.motivo !== evento.comentario && (
                    <Typography variant="body2" sx={{ mt: 0.5, fontWeight: 600 }}>
                      Resultado registrado: {evento.motivo}
                    </Typography>
                  )}
                </Paper>
              </Box>
            )
          })}
        </Stack>
      )}
    </Stack>
  )
}
