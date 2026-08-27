import { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Button,
  IconButton,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined'
import {
  enviarASupervision,
  listarDocumentosPorExpedienteValidacion,
  listarEquipoSupervision,
  listarUsuariosSupervision,
  registrarResultadoValidacion,
  type NotificacionAsignacionDocumento,
} from '../api/notificacionValidacionApi'
import type { EquipoAsignacion, UsuarioAsignable } from '../api/asignacionApi'
import { ApiError } from '../api/http'
import { SectionCard } from './RegistroExpedienteForm'

interface NotificacionValidacionContentProps {
  idExpediente: number
  documentoFoco: NotificacionAsignacionDocumento
  onActualizado: () => void
}

/**
 * Subpestaña "Validar" de la Bandeja Validación de Notificación (21/08/2026, pedido explícito del
 * usuario — investigación previa contra el código real de V2, `JPanelNotificacionV2.crearBandejaValidacion`/
 * `crearPanelValidarOperativo`/`DocumentoAnalisisDAO.registrarResultadoValidacion`).
 *
 * Solo recibe Carta Final (`clasificacion==='FINAL'`) — las cartas intermedias nunca llegan a esta
 * bandeja, se asignan directo del Supervisor a Notificación (Bandeja Asignación de Notificación).
 *
 * **3 bloques (22/08/2026, mismo orden que "Registrar análisis" en `AnalisisContent.tsx` — solo como
 * referencia de diseño, pedido explícito del usuario): grilla de documentos → resultado → envío al
 * siguiente responsable**:
 * 1. **"Documentos a validar"** — MISMAS columnas que "Documentos para emitir" de
 *    `NotificacionAsignacionContent.tsx` (ícono Guardar al inicio, Tipo documento, N° Documento,
 *    Estado documento, Fecha Emisión), pero únicamente "Estado documento" es editable (pedido
 *    explícito del usuario) — combo con una sola opción real ("Validado", el único cambio de estado
 *    válido desde acá); N° Documento/Fecha Emisión se muestran de solo lectura para mantener la
 *    misma forma visual sin habilitar una edición que no aplica en esta bandeja. Guardar llama
 *    `registrarResultadoValidacion` con `APROBADO` directo, atajo rápido sin pasar por el bloque de
 *    abajo.
 * 2. **"Registro de la validación"**: combo Resultado (`Validado`/`Observado`, códigos reales
 *    `APROBADO`/`OBSERVADO`) + Comentario obligatorio si Observado + botón "Registrar Validación".
 * 3. **"Enviar a supervisión"** (NUEVO, 22/08/2026 — pedido explícito del usuario, no existe en V2
 *    real): bloque "Destino operativo" con Equipo destino (fijo a `EQ_SUPERVISION`, autoseleccionado
 *    igual que el resto de combos de equipo fijo en este módulo) + Usuario destino, y botón "Enviar a
 *    supervisión". Acción independiente de "Registrar Validación" — no reemplaza el cambio de estado
 *    automático que ya hace `registrarResultadoValidacion` (Aprobado revierte a `POR_ASIGNAR`,
 *    Observado limpia el responsable); solo permite elegir explícitamente a qué persona/equipo de
 *    supervisión se deriva el documento, en vez de dejarlo sin responsable puntual.
 */
export function NotificacionValidacionContent({ idExpediente, documentoFoco, onActualizado }: NotificacionValidacionContentProps) {
  const [documentos, setDocumentos] = useState<NotificacionAsignacionDocumento[]>([])
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [procesandoFila, setProcesandoFila] = useState<number | null>(null)

  const [resultado, setResultado] = useState<'APROBADO' | 'OBSERVADO'>('APROBADO')
  const [comentario, setComentario] = useState('')
  const [procesando, setProcesando] = useState(false)
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [errorRegistro, setErrorRegistro] = useState<string | null>(null)

  const [equipos, setEquipos] = useState<EquipoAsignacion[]>([])
  const [usuarios, setUsuarios] = useState<UsuarioAsignable[]>([])
  const [idEquipoDestino, setIdEquipoDestino] = useState('')
  const [idUsuarioDestino, setIdUsuarioDestino] = useState('')
  const [procesandoSupervision, setProcesandoSupervision] = useState(false)
  const [mensajeSupervision, setMensajeSupervision] = useState<string | null>(null)
  const [errorSupervision, setErrorSupervision] = useState<string | null>(null)

  function cargar() {
    setCargando(true)
    setError(null)
    listarDocumentosPorExpedienteValidacion(idExpediente)
      .then((data) => setDocumentos(data.filter((d) => d.clasificacion === 'FINAL')))
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudieron cargar los documentos del expediente.'))
      .finally(() => setCargando(false))
  }

  useEffect(() => {
    cargar()
    setResultado('APROBADO')
    setComentario('')
    setMensaje(null)
    setErrorRegistro(null)
    setIdUsuarioDestino('')
    setMensajeSupervision(null)
    setErrorSupervision(null)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idExpediente, documentoFoco.idDocumentoAnalizado])

  useEffect(() => {
    listarEquipoSupervision().then(setEquipos).catch(() => setEquipos([]))
  }, [])

  const equipoSupervision = equipos[0] ?? null

  useEffect(() => {
    setIdEquipoDestino(equipoSupervision ? String(equipoSupervision.idEquipo) : '')
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [equipoSupervision?.idEquipo])

  useEffect(() => {
    if (!idEquipoDestino) {
      setUsuarios([])
      setIdUsuarioDestino('')
      return
    }
    listarUsuariosSupervision(Number(idEquipoDestino)).then(setUsuarios).catch(() => setUsuarios([]))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idEquipoDestino])

  const usuarioSeleccionado = useMemo(() => usuarios.find((u) => String(u.idUsuario) === idUsuarioDestino) ?? null, [usuarios, idUsuarioDestino])

  async function handleGuardarFila(doc: NotificacionAsignacionDocumento) {
    setProcesandoFila(doc.idDocumentoAnalizado)
    setError(null)
    try {
      await registrarResultadoValidacion(doc.idDocumentoAnalizado, 'APROBADO', '')
      cargar()
      onActualizado()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo validar el documento.')
    } finally {
      setProcesandoFila(null)
    }
  }

  async function handleRegistrarValidacion() {
    setErrorRegistro(null)
    setMensaje(null)
    if (resultado === 'OBSERVADO' && !comentario.trim()) {
      setErrorRegistro('Ingrese un comentario con el motivo de la observación.')
      return
    }
    if (!window.confirm(`¿Registrar la validación como "${resultado === 'APROBADO' ? 'Validado' : 'Observado'}"?`)) return
    setProcesando(true)
    try {
      await registrarResultadoValidacion(documentoFoco.idDocumentoAnalizado, resultado, comentario)
      setMensaje(
        resultado === 'APROBADO'
          ? 'Validación registrada. El expediente vuelve a la Bandeja Asignación del Supervisor para emitir el documento.'
          : 'Observación registrada. El expediente vuelve a la Bandeja Asignación del Supervisor para reasignarlo.',
      )
      setComentario('')
      cargar()
      onActualizado()
    } catch (e) {
      setErrorRegistro(e instanceof ApiError ? e.message : 'No se pudo registrar la validación.')
    } finally {
      setProcesando(false)
    }
  }

  async function handleEnviarASupervision() {
    setErrorSupervision(null)
    setMensajeSupervision(null)
    if (!idEquipoDestino || !idUsuarioDestino) {
      setErrorSupervision('Seleccione un usuario destino.')
      return
    }
    if (!window.confirm('¿Enviar este documento a supervisión?')) return
    setProcesandoSupervision(true)
    try {
      await enviarASupervision(documentoFoco.idDocumentoAnalizado, Number(idEquipoDestino), Number(idUsuarioDestino))
      setMensajeSupervision(`Documento enviado a supervisión${usuarioSeleccionado ? ` (${usuarioSeleccionado.displayName})` : ''}.`)
      setIdUsuarioDestino('')
      onActualizado()
    } catch (e) {
      setErrorSupervision(e instanceof ApiError ? e.message : 'No se pudo enviar el documento a supervisión.')
    } finally {
      setProcesandoSupervision(false)
    }
  }

  return (
    <Stack spacing={3}>
      <Alert severity="info">
        {documentoFoco.numeroExpediente} — {documentoFoco.titular} · {documentoFoco.tipoDocumento} · Estado doc.:{' '}
        {documentoFoco.estadoDocumento || '-'}
      </Alert>
      {error && <Alert severity="error">{error}</Alert>}

      <SectionCard title="Documentos a validar">
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell />
                <TableCell>Tipo documento</TableCell>
                <TableCell>N° Documento</TableCell>
                <TableCell>Estado documento</TableCell>
                <TableCell>Fecha Emisión</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {documentos.map((doc) => (
                <TableRow key={doc.idDocumentoAnalizado}>
                  <TableCell>
                    <Tooltip title="Guardar">
                      <span>
                        <IconButton size="small" disabled={procesandoFila === doc.idDocumentoAnalizado} onClick={() => handleGuardarFila(doc)}>
                          <SaveOutlinedIcon fontSize="small" />
                        </IconButton>
                      </span>
                    </Tooltip>
                  </TableCell>
                  <TableCell>{doc.tipoDocumento}</TableCell>
                  <TableCell>{doc.numeroDocumento || '-'}</TableCell>
                  <TableCell sx={{ minWidth: 160 }}>
                    <TextField select size="small" fullWidth value="VALIDADO" disabled={procesandoFila === doc.idDocumentoAnalizado}>
                      <MenuItem value="VALIDADO">Validado</MenuItem>
                    </TextField>
                  </TableCell>
                  <TableCell>{doc.fechaDocumento ?? '-'}</TableCell>
                </TableRow>
              ))}
              {documentos.length === 0 && !cargando && (
                <TableRow>
                  <TableCell colSpan={5}>
                    <Typography variant="body2" color="text.secondary">
                      Sin documentos.
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </SectionCard>

      <SectionCard title="Registro de la validación">
        <Stack spacing={2}>
          <TextField
            select
            label="Resultado"
            size="small"
            fullWidth
            value={resultado}
            onChange={(e) => setResultado(e.target.value as 'APROBADO' | 'OBSERVADO')}
          >
            <MenuItem value="APROBADO">Validado</MenuItem>
            <MenuItem value="OBSERVADO">Observado</MenuItem>
          </TextField>
          {resultado === 'OBSERVADO' && (
            <TextField
              label="Comentario (motivo de la observación)"
              size="small"
              fullWidth
              multiline
              minRows={2}
              value={comentario}
              onChange={(e) => setComentario(e.target.value)}
            />
          )}
          <Alert severity={resultado === 'APROBADO' ? 'success' : 'warning'}>
            {resultado === 'APROBADO'
              ? 'Al registrar, el expediente vuelve automáticamente a la Bandeja Asignación del Supervisor, listo para emitir.'
              : 'Al registrar, el expediente vuelve automáticamente a la Bandeja Asignación del Supervisor para reasignarlo.'}
          </Alert>
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
            <Button variant="contained" disabled={procesando} onClick={handleRegistrarValidacion}>
              Registrar Validación
            </Button>
            {mensaje && <Alert severity="success" sx={{ py: 0 }}>{mensaje}</Alert>}
            {errorRegistro && <Alert severity="error" sx={{ py: 0 }}>{errorRegistro}</Alert>}
          </Stack>
        </Stack>
      </SectionCard>

      <SectionCard title="Enviar a supervisión">
        <Stack spacing={2}>
          <TextField select label="Equipo destino" size="small" fullWidth value={idEquipoDestino} onChange={(e) => setIdEquipoDestino(e.target.value)}>
            {equipos.length === 0 && <MenuItem value="">Cargando...</MenuItem>}
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
            onChange={(e) => setIdUsuarioDestino(e.target.value)}
            disabled={!idEquipoDestino}
          >
            <MenuItem value="">Seleccione</MenuItem>
            {usuarios.map((u) => (
              <MenuItem key={u.idUsuario} value={String(u.idUsuario)}>
                {u.displayName}
              </MenuItem>
            ))}
          </TextField>
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
            <Button
              variant="contained"
              disabled={procesandoSupervision || !idEquipoDestino || !idUsuarioDestino}
              onClick={handleEnviarASupervision}
            >
              Enviar a supervisión
            </Button>
            {mensajeSupervision && <Alert severity="success" sx={{ py: 0 }}>{mensajeSupervision}</Alert>}
            {errorSupervision && <Alert severity="error" sx={{ py: 0 }}>{errorSupervision}</Alert>}
          </Stack>
        </Stack>
      </SectionCard>
    </Stack>
  )
}
