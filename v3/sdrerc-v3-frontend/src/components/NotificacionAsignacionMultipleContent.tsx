import { useEffect, useState } from 'react'
import { Alert, Button, MenuItem, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField } from '@mui/material'
import {
  generarAsignacionNotificacion,
  listarEquiposDestinoNotificacion,
  listarUsuariosDestinoNotificacion,
  type NotificacionAsignacionDocumento,
} from '../api/notificacionAsignacionApi'
import type { EquipoAsignacion, UsuarioAsignable } from '../api/asignacionApi'
import { ApiError } from '../api/http'
import { SectionCard } from './RegistroExpedienteForm'
import { StatusBadge } from './StatusBadge'

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
 * Se abre (`NotificacionAsignacionBandejaPage.tsx#handleAsignarSeleccion`) cuando la selección por
 * checkbox del toolbar ("Asignar") abarca documentos de 2+ expedientes distintos — con 1 solo
 * expediente representado en la selección, se abre en cambio el workspace "individual" ya existente
 * (mismo por-expediente que el doble clic, `NotificacionAsignacionContent.tsx`).
 *
 * **Sin Historial** (mismo criterio que `AsignacionSeleccionContent` con `mostrarHistorial=false`):
 * cada documento tiene su propio historial, se consulta entrando a su propio expediente.
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
  const [equipos, setEquipos] = useState<EquipoAsignacion[]>([])
  const [usuarios, setUsuarios] = useState<UsuarioAsignable[]>([])
  const [idEquipoDestino, setIdEquipoDestino] = useState('')
  const [idUsuarioDestino, setIdUsuarioDestino] = useState('')
  const [hojasEnvio, setHojasEnvio] = useState<Record<number, string>>({})
  const [procesando, setProcesando] = useState(false)
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    listarEquiposDestinoNotificacion().then(setEquipos).catch(() => setEquipos([]))
  }, [])

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
    if (!window.confirm(`¿Generar asignación de ${documentos.length} documento(s)?`)) return
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
