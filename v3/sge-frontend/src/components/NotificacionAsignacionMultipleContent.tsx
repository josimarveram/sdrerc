import { useEffect, useState } from 'react'
import { Alert, Button, MenuItem, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography } from '@mui/material'
import {
  generarAsignacionNotificacion,
  listarEquiposDestinoNotificacion,
  listarHistorialAsignacionNotificacion,
  listarUsuariosDestinoNotificacion,
  type NotificacionAsignacionDocumento,
} from '../api/notificacionAsignacionApi'
import type { AsignacionHistorial, EquipoAsignacion, UsuarioAsignable } from '../api/asignacionApi'
import { ApiError } from '../api/http'
import { useAppDialog } from '../dialogs/AppDialogProvider'
import { SectionCard } from './RegistroExpedienteForm'
import { StatusBadge } from './StatusBadge'

function formatearFechaHora(iso: string | null): string {
  if (!iso) return '-'
  const [fecha, hora] = iso.split('T')
  const [anio, mes, dia] = fecha.split('-')
  const hhmm = hora ? hora.slice(0, 5) : '00:00'
  return `${dia}/${mes}/${anio} ${hhmm}`
}

interface NotificacionAsignacionMultipleContentProps {
  documentos: NotificacionAsignacionDocumento[]
  onAsignado?: () => void
}

/**
 * Contenido de la subpestaña "Asignación" del workspace "Selección múltiple" de la Bandeja
 * Asignación de Notificación (21/08/2026, Parte 1 — réplica mecánica del mismo patrón ya construido
 * en `AsignacionSeleccionContent.tsx` para el módulo Asignación, adaptado a granularidad de
 * DOCUMENTO en vez de expediente: `AsignacionSeleccionContent` agrupa por expediente porque cada
 * fila de su grilla YA ES un expediente; acá cada fila de la bandeja es un documento
 * (`NotificacionAsignacionDocumentoDTO`), así que la mini-grilla lista documentos, no expedientes).
 *
 * 28/08/2026, pedido explícito del usuario (Parte 3): ahora es el ÚNICO contenido que abre el botón
 * azul "Asignar" (`NotificacionAsignacionBandejaPage.tsx#handleAsignarSeleccion`), sin importar si la
 * selección representa 1 o 2+ expedientes — ya era genérico para cualquier cantidad de documentos
 * desde su construcción original, no hizo falta un componente nuevo. El doble clic abre en cambio
 * `NotificacionSupervisionContent.tsx` (Datos/Supervisión), completamente separado de este panel.
 *
 * **Historial de asignación / reasignación** (28/08/2026, agregado): visible SOLO cuando la
 * selección es de un único documento (`documentos.length === 1`, mismo criterio que
 * `mostrarHistorial` en `AsignacionSeleccionContent.tsx`) — con 2+ documentos cada uno tiene su
 * propio historial, se consulta abriendo su expediente individual (doble clic).
 *
 * **Rediseño (21/08/2026, mismo día, pedido explícito del usuario)**: se retiró el checkbox manual
 * "Habilitar reasignación" — el flag `reasignacion` que viaja al backend ahora se deriva solo
 * (`documentos.some(d => d.asignado)`, mismo criterio que `NotificacionAsignacionContent.tsx`), sigue
 * siendo un único flag para todo el lote (el endpoint `generarAsignacionNotificacion`/
 * `NotificacionAsignacionService.generarAsignacion` acepta un solo booleano para todo el lote, sin el
 * split por-fila que sí tiene Asignación — cambiar esa semántica de backend excede el alcance de este
 * componente). También se retiró el campo "Comentario" (huérfano — confirmado en el backend que
 * nunca se persiste en la rama `EQ_VALIDACION`/`EQ_NOTIFICACION`, ver Javadoc de
 * `NotificacionAsignacionContent.tsx` para el detalle). La columna "Tipo de operación" de la
 * mini-grilla sigue siendo el mirror visual: detecta y muestra Asignación inicial/Reasignación por
 * fila (`doc.asignado`).
 */
export function NotificacionAsignacionMultipleContent({ documentos, onAsignado }: NotificacionAsignacionMultipleContentProps) {
  const { confirm: confirmDialog } = useAppDialog()
  const [equipos, setEquipos] = useState<EquipoAsignacion[]>([])
  const [usuarios, setUsuarios] = useState<UsuarioAsignable[]>([])
  const [idEquipoDestino, setIdEquipoDestino] = useState('')
  const [idUsuarioDestino, setIdUsuarioDestino] = useState('')
  const [hojasEnvio, setHojasEnvio] = useState<Record<number, string>>({})
  const [procesando, setProcesando] = useState(false)
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [historial, setHistorial] = useState<AsignacionHistorial[]>([])

  const mostrarHistorial = documentos.length === 1
  const idDocumentoUnico = mostrarHistorial ? documentos[0].idDocumentoAnalizado : null

  useEffect(() => {
    listarEquiposDestinoNotificacion().then(setEquipos).catch(() => setEquipos([]))
  }, [])

  useEffect(() => {
    if (idDocumentoUnico == null) {
      setHistorial([])
      return
    }
    listarHistorialAsignacionNotificacion(idDocumentoUnico).then(setHistorial).catch(() => setHistorial([]))
  }, [idDocumentoUnico])

  useEffect(() => {
    if (!idEquipoDestino) {
      setUsuarios([])
      setIdUsuarioDestino('')
      return
    }
    listarUsuariosDestinoNotificacion(Number(idEquipoDestino)).then(setUsuarios).catch(() => setUsuarios([]))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idEquipoDestino])

  const esReasignacion = documentos.some((d) => d.asignado)

  function limpiar() {
    setIdEquipoDestino('')
    setIdUsuarioDestino('')
    setHojasEnvio({})
    setError(null)
  }

  async function handleGenerarAsignacion() {
    setError(null)
    setMensaje(null)
    if (documentos.length === 0) return
    if (!idEquipoDestino || !idUsuarioDestino) {
      setError('Seleccione equipo y usuario destino.')
      return
    }
    if (!(await confirmDialog(`¿Generar asignación de ${documentos.length} documento(s)?`, { tone: 'info', confirmLabel: 'Generar' }))) return
    setProcesando(true)
    try {
      await generarAsignacionNotificacion({
        idsDocumentoAnalizado: documentos.map((d) => d.idDocumentoAnalizado),
        idEquipoDestino: Number(idEquipoDestino),
        idUsuarioDestino: Number(idUsuarioDestino),
        hojasEnvioPorDocumento: hojasEnvio,
        comentario: '',
        reasignacion: esReasignacion,
      })
      setMensaje(`${documentos.length} documento(s) asignado(s) correctamente.`)
      limpiar()
      onAsignado?.()
      if (idDocumentoUnico != null) {
        listarHistorialAsignacionNotificacion(idDocumentoUnico).then(setHistorial).catch(() => {})
      }
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo generar la asignación.')
    } finally {
      setProcesando(false)
    }
  }

  return (
    <Stack spacing={2}>
      {error && (
        <Alert severity="error" sx={{ whiteSpace: 'pre-line' }}>
          {error}
        </Alert>
      )}
      {mensaje && (
        <Alert severity="success" sx={{ whiteSpace: 'pre-line' }} onClose={() => setMensaje(null)}>
          {mensaje}
        </Alert>
      )}

      <SectionCard title={`Documentos seleccionados (${documentos.length})`}>
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>N° Expediente</TableCell>
                <TableCell>N° expediente SGD</TableCell>
                <TableCell>Tipo documento</TableCell>
                <TableCell>Clasificación</TableCell>
                <TableCell>Hoja de envío actual</TableCell>
                <TableCell>Responsable actual</TableCell>
                <TableCell>Tipo de operación</TableCell>
                <TableCell>Hoja de envío nueva</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {documentos.map((doc) => (
                <TableRow key={doc.idDocumentoAnalizado}>
                  <TableCell>{doc.numeroExpediente}</TableCell>
                  <TableCell>{doc.numeroExpedienteSgd || '-'}</TableCell>
                  <TableCell>{doc.tipoDocumento}</TableCell>
                  <TableCell>{doc.clasificacion}</TableCell>
                  <TableCell>{doc.numeroHojaEnvioNotificacion || '-'}</TableCell>
                  <TableCell>{doc.usuarioNotificacionActual || '-'}</TableCell>
                  <TableCell>
                    {doc.asignado ? (
                      <StatusBadge label="Reasignación" tone="warning" />
                    ) : (
                      <StatusBadge label="Asignación inicial" tone="info" />
                    )}
                  </TableCell>
                  <TableCell>
                    <TextField
                      size="small"
                      placeholder="N° hoja de envío"
                      value={hojasEnvio[doc.idDocumentoAnalizado] ?? ''}
                      onChange={(e) => setHojasEnvio((prev) => ({ ...prev, [doc.idDocumentoAnalizado]: e.target.value }))}
                      sx={{ minWidth: 180 }}
                    />
                  </TableCell>
                </TableRow>
              ))}
              {documentos.length === 0 && (
                <TableRow>
                  <TableCell colSpan={8} align="center">
                    Sin documentos seleccionados.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </SectionCard>

      <SectionCard title="Destino operativo">
        <Stack spacing={2}>
          <TextField
            select
            label="Equipo destino"
            size="small"
            fullWidth
            value={idEquipoDestino}
            onChange={(e) => setIdEquipoDestino(e.target.value)}
          >
            <MenuItem value="">Seleccione equipo</MenuItem>
            {equipos.map((eq) => (
              <MenuItem key={eq.idEquipo} value={String(eq.idEquipo)}>
                {eq.displayName}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Usuario destino"
            size="small"
            fullWidth
            value={idUsuarioDestino}
            disabled={!idEquipoDestino}
            onChange={(e) => setIdUsuarioDestino(e.target.value)}
          >
            <MenuItem value="">Seleccione usuario</MenuItem>
            {usuarios.map((u) => (
              <MenuItem key={u.idUsuario} value={String(u.idUsuario)}>
                {u.displayName}
              </MenuItem>
            ))}
          </TextField>
        </Stack>
      </SectionCard>

      {mostrarHistorial && (
        <SectionCard title="Historial de asignación / reasignación">
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Tipo</TableCell>
                  <TableCell>Usuario</TableCell>
                  <TableCell>Equipo</TableCell>
                  <TableCell>Hoja de envío</TableCell>
                  <TableCell>Fecha</TableCell>
                  <TableCell>Asignado por</TableCell>
                  <TableCell>Estado</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {historial.map((h) => (
                  <TableRow key={h.idExpedienteAsignacion}>
                    <TableCell>{h.reasignacionExcepcional ? 'Reasignación' : 'Asignación inicial'}</TableCell>
                    <TableCell>{h.abogado || '-'}</TableCell>
                    <TableCell>{h.equipo || '-'}</TableCell>
                    <TableCell>{h.numeroHojaEnvio || '-'}</TableCell>
                    <TableCell>{formatearFechaHora(h.fechaAsignacion)}</TableCell>
                    <TableCell>{h.asignadoPor || '-'}</TableCell>
                    <TableCell>{h.activa ? 'Activa' : 'Histórica'}</TableCell>
                  </TableRow>
                ))}
                {historial.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={7}>
                      <Typography variant="body2" color="text.secondary">
                        Sin historial de asignación registrado.
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
      )}

      <Stack direction="row" spacing={1}>
        <Button
          variant="contained"
          onClick={handleGenerarAsignacion}
          disabled={procesando || !idEquipoDestino || !idUsuarioDestino || documentos.length === 0}
        >
          Generar asignación
        </Button>
        <Button variant="outlined" onClick={limpiar} disabled={procesando}>
          Cancelar
        </Button>
      </Stack>
    </Stack>
  )
}
