import { useEffect, useState } from 'react'
import {
  Alert,
  Box,
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
  existeSupervisionAprobadaNotificacion,
  guardarFirmaDocumentoNotificacion,
  listarDocumentosPorExpedienteNotificacion,
  listarEquiposDestinoObservado,
  listarEstadosDocumentoNotificacion,
  listarMotivosCorreccionSupervision,
  listarUsuariosDestinoNotificacion,
  registrarSupervisionNotificacion,
  registrarSupervisionObservada,
  type NotificacionAsignacionDocumento,
} from '../api/notificacionAsignacionApi'
import type { CatalogoItem } from '../api/registroApi'
import type { EquipoAsignacion, UsuarioAsignable } from '../api/asignacionApi'
import { ApiError } from '../api/http'
import { DATA_GRID_ROW_ICON_SAVE_CLASS } from '../dataGridTheme'
import { SectionCard } from './RegistroExpedienteForm'
import { StatusBadge } from './StatusBadge'

interface NotificacionSupervisionContentProps {
  idExpediente: number
  documentoFoco: NotificacionAsignacionDocumento
  onActualizado: () => void
}

/**
 * Subpestaña "Supervisión" del workspace de doble clic en la Bandeja de Supervisión (antes "Bandeja
 * de Asignación") — 28/08/2026, pedido explícito del usuario, reemplaza por completo a la antigua
 * subpestaña "Asignación" (`NotificacionAsignacionContent.tsx`, eliminada).
 *
 * **Separación de responsabilidades (pedido explícito, Parte 3)**: esta bandeja ahora tiene 2
 * paneles totalmente separados, igual que Asignación tiene "doble clic" (Datos/Asociar/Grupo
 * Familiar) vs "Asignar" (`AsignacionSeleccionContent`):
 * - **"Asignar"** (botón azul sobre la grilla, con 1+ filas marcadas): reutiliza literalmente
 *   `NotificacionAsignacionMultipleContent.tsx` (ya soportaba cualquier cantidad de documentos, no
 *   solo 2+) — deriva a `EQ_VALIDACION`/`EQ_NOTIFICACION`. Único responsable de "Destino operativo"
 *   en este módulo desde ahora.
 * - **"Supervisión"** (este componente, doble clic): réplica del panel "Verificar" de Verificación
 *   (Estado / Documentos / Resultado) — el supervisor revisa y, si decide NO enviar a Validación,
 *   puede emitir él mismo la carta final directamente desde acá.
 *
 * **02/09/2026, pedido explícito del usuario, corrige la afirmación de arriba ("SIN Destino
 * operativo")**: cuando el resultado elegido es Observado, SÍ aparece un bloque "Destino operativo"
 * propio de este panel (Equipo Análisis/Ejecución + Usuario destino + "Motivo de corrección") —
 * restaura el flujo real que existía antes del 28/08/2026, cuando Asignación y Supervisión eran un
 * solo panel. Es un "Destino operativo" DISTINTO del de "Asignar" (que solo ofrece Validación/
 * Notificación, para asignar el trabajo hacia adelante): este deriva HACIA ATRÁS, a Análisis o
 * Ejecución, para que corrijan algo puntual — mismo criterio que ya usa Ejecución/Verificación al
 * "devolver con observación". Implementado reutilizando {@code AsignacionExpedienteDAO#reasignarDesdeCartaRespuesta}
 * (mismo método de "Cartas de respuesta", con destino "OBSERVADO" en vez de "DERIVADO") para
 * `EQ_ANALISIS`, y {@code DocumentoAnalisisDAO#derivarDocumentoNotificacionAEjecucion} (ya existente)
 * para `EQ_EJECUCION`. Requiere la transición `DEVOLUCION_A_ANALISIS: NOTIFICACION/POR_ASIGNAR →
 * ANALISIS/OBSERVADO` — script {@code 122_transicion_notificacion_supervision_observado_analisis.sql},
 * creado, no ejecutado (ver `v3/CLAUDE.md`).
 *
 * **"Emisión directa" (Parte 2, confirmado explícitamente por el usuario)**: antes, "Documentos para
 * emitir" solo incluía cartas finales ya `VALIDADO`/`EMITIDO` — un documento recién llegado
 * (`EN_DESPACHO`) o devuelto observado por el validador (`OBSERVADO`) no se podía tocar desde acá,
 * obligando siempre a pasar por Validación. Ahora la grilla "Documentos" incluye TODAS las cartas
 * finales del expediente, en cualquier estado, siempre editables (N° Documento/Estado documento/
 * Fecha Emisión) — el supervisor puede fijar Estado→"Emitido" él mismo, sin depender de que un
 * validador la apruebe primero. **Sin cambios de backend**: `guardarFirmaDocumentoNotificacion`
 * (`registrarFirmaDocumentoNotificacion` en el DAO) nunca exigió que el documento estuviera ya
 * `VALIDADO` — la restricción era puramente del frontend anterior, que solo mostraba el editor para
 * ese subconjunto.
 *
 * Los documentos `INTERMEDIO` del expediente se listan también, de solo lectura (íconos/campos
 * bloqueados, mismo criterio ya usado en Ejecución/Verificación esta sesión — nunca se ocultan ni se
 * convierten en texto plano) — llegan siempre ya `EMITIDO` desde Verificación, el supervisor no los
 * revisa, solo los ve para tener el panorama completo del expediente antes de decidir "Asignar".
 */
export function NotificacionSupervisionContent({ idExpediente, documentoFoco, onActualizado }: NotificacionSupervisionContentProps) {
  const [documentos, setDocumentos] = useState<NotificacionAsignacionDocumento[]>([])
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [estadosDocumento, setEstadosDocumento] = useState<CatalogoItem[]>([])

  const [resultadoSupervision, setResultadoSupervision] = useState<'APROBADO' | 'OBSERVADO'>('APROBADO')
  const [comentarioSupervision, setComentarioSupervision] = useState('')
  const [supervisionAprobada, setSupervisionAprobada] = useState(false)
  const [procesandoSupervision, setProcesandoSupervision] = useState(false)
  const [mensajeSupervision, setMensajeSupervision] = useState<string | null>(null)
  const [errorSupervision, setErrorSupervision] = useState<string | null>(null)

  // 02/09/2026, pedido explícito del usuario: al elegir Observado, restaurar el "Motivo de
  // corrección" (catálogo real, incluye "Error material") + "Destino operativo" (Equipo Análisis/
  // Ejecución + Usuario destino) que existían en el diseño previo al 28/08/2026, cuando Asignación y
  // Supervisión eran un solo panel.
  const [motivosCorreccion, setMotivosCorreccion] = useState<CatalogoItem[]>([])
  const [motivoCorreccionCodigo, setMotivoCorreccionCodigo] = useState('')
  const [equiposObservado, setEquiposObservado] = useState<EquipoAsignacion[]>([])
  const [idEquipoDestinoObservado, setIdEquipoDestinoObservado] = useState('')
  const [usuariosObservado, setUsuariosObservado] = useState<UsuarioAsignable[]>([])
  const [idUsuarioDestinoObservado, setIdUsuarioDestinoObservado] = useState('')

  function cargar() {
    setCargando(true)
    setError(null)
    listarDocumentosPorExpedienteNotificacion(idExpediente)
      .then(setDocumentos)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudieron cargar los documentos del expediente.'))
      .finally(() => setCargando(false))
  }

  useEffect(() => {
    cargar()
    setResultadoSupervision('APROBADO')
    setComentarioSupervision('')
    setMensajeSupervision(null)
    setErrorSupervision(null)
    setMotivoCorreccionCodigo('')
    setIdEquipoDestinoObservado('')
    setIdUsuarioDestinoObservado('')
    existeSupervisionAprobadaNotificacion(documentoFoco.idDocumentoAnalizado)
      .then(setSupervisionAprobada)
      .catch(() => setSupervisionAprobada(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idExpediente, documentoFoco.idDocumentoAnalizado])

  useEffect(() => {
    listarEstadosDocumentoNotificacion().then(setEstadosDocumento).catch(() => setEstadosDocumento([]))
    listarMotivosCorreccionSupervision().then(setMotivosCorreccion).catch(() => setMotivosCorreccion([]))
    listarEquiposDestinoObservado().then(setEquiposObservado).catch(() => setEquiposObservado([]))
  }, [])

  useEffect(() => {
    if (!idEquipoDestinoObservado) {
      setUsuariosObservado([])
      setIdUsuarioDestinoObservado('')
      return
    }
    listarUsuariosDestinoNotificacion(Number(idEquipoDestinoObservado)).then(setUsuariosObservado).catch(() => setUsuariosObservado([]))
  }, [idEquipoDestinoObservado])

  function actualizarDocumentoLocal(id: number, cambios: Partial<NotificacionAsignacionDocumento>) {
    setDocumentos((prev) => prev.map((d) => (d.idDocumentoAnalizado === id ? { ...d, ...cambios } : d)))
  }

  function handleCambiarEstado(doc: NotificacionAsignacionDocumento, nuevoEstado: string) {
    const fechaDocumento = nuevoEstado === 'EMITIDO' ? new Date().toISOString().slice(0, 10) : doc.fechaDocumento
    actualizarDocumentoLocal(doc.idDocumentoAnalizado, { estadoDocumentoCodigo: nuevoEstado, fechaDocumento })
  }

  async function handleGuardarFirma(doc: NotificacionAsignacionDocumento) {
    try {
      await guardarFirmaDocumentoNotificacion(doc.idDocumentoAnalizado, doc.numeroDocumento, doc.fechaDocumento, doc.estadoDocumentoCodigo)
      cargar()
      onActualizado()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo guardar el documento.')
    }
  }

  async function handleRegistrarSupervision() {
    setErrorSupervision(null)
    setMensajeSupervision(null)
    if (resultadoSupervision === 'OBSERVADO') {
      if (!comentarioSupervision.trim()) {
        setErrorSupervision('Ingrese un comentario con el motivo de la observación.')
        return
      }
      if (!idEquipoDestinoObservado || !idUsuarioDestinoObservado) {
        setErrorSupervision('Seleccione el equipo y el usuario destino en "Destino operativo".')
        return
      }
      setProcesandoSupervision(true)
      try {
        const motivoNombre = motivosCorreccion.find((m) => m.codigo === motivoCorreccionCodigo)?.nombre || ''
        await registrarSupervisionObservada({
          idExpediente,
          idDocumentoAnalizado: documentoFoco.idDocumentoAnalizado,
          idEquipoDestino: Number(idEquipoDestinoObservado),
          idUsuarioDestino: Number(idUsuarioDestinoObservado),
          motivoCorreccionNombre: motivoNombre,
          comentario: comentarioSupervision,
        })
        const equipoNombre = equiposObservado.find((eq) => String(eq.idEquipo) === idEquipoDestinoObservado)?.displayName || 'el equipo elegido'
        setMensajeSupervision(`Documento observado y derivado a ${equipoNombre}.`)
        onActualizado()
      } catch (e) {
        setErrorSupervision(e instanceof ApiError ? e.message : 'No se pudo registrar la observación.')
      } finally {
        setProcesandoSupervision(false)
      }
      return
    }
    if (documentoFoco.estadoDocumentoCodigo !== 'EMITIDO') {
      setErrorSupervision('El documento enfocado debe estar Emitido antes de registrar la supervisión.')
      return
    }
    setProcesandoSupervision(true)
    try {
      await registrarSupervisionNotificacion(idExpediente, documentoFoco.idDocumentoAnalizado)
      setSupervisionAprobada(true)
      setMensajeSupervision('Supervisión registrada correctamente. Use el botón "Asignar" para derivarlo a Notificación.')
      onActualizado()
    } catch (e) {
      setErrorSupervision(e instanceof ApiError ? e.message : 'No se pudo registrar la supervisión.')
    } finally {
      setProcesandoSupervision(false)
    }
  }

  const padres = documentos.filter((d) => d.idDocumentoAnalizado != null)

  return (
    <Stack spacing={3}>
      <SectionCard title="Estado de la supervisión">
        <Stack spacing={1}>
          <Box>
            <StatusBadge label={`${documentoFoco.estadoExpediente || '-'}`} tone="info" />
          </Box>
          <Typography variant="body2" color="text.secondary">
            {documentoFoco.numeroExpediente} — {documentoFoco.titular} · {documentoFoco.tipoDocumento} (
            {documentoFoco.clasificacion}) · Estado doc.: {documentoFoco.estadoDocumento || '-'}
          </Typography>
        </Stack>
      </SectionCard>

      {error && <Alert severity="error">{error}</Alert>}

      <SectionCard title="Documentos">
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell />
                <TableCell>Tipo documento</TableCell>
                <TableCell>Clasificación</TableCell>
                <TableCell>N° Documento</TableCell>
                <TableCell>Estado documento</TableCell>
                <TableCell>Fecha Emisión</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {padres.map((doc) => {
                // 28/08/2026, pedido explícito del usuario: los documentos INTERMEDIO ya llegan
                // EMITIDO desde Verificación — el supervisor solo los consulta acá, no los edita (esa
                // acción es exclusiva de las cartas FINALES, que sí puede emitir él mismo). Mismo
                // criterio "bloqueado, no oculto, no convertido a texto" ya usado en Ejecución/
                // Verificación esta sesión.
                const puedeEditar = doc.clasificacion === 'FINAL'
                return (
                  <TableRow key={doc.idDocumentoAnalizado}>
                    <TableCell>
                      <Tooltip title={puedeEditar ? 'Guardar' : 'Documento intermedio: solo lectura'}>
                        <span>
                          <IconButton
                            size="small"
                            className={DATA_GRID_ROW_ICON_SAVE_CLASS}
                            disabled={!puedeEditar}
                            onClick={() => handleGuardarFirma(doc)}
                          >
                            <SaveOutlinedIcon fontSize="small" />
                          </IconButton>
                        </span>
                      </Tooltip>
                    </TableCell>
                    <TableCell>{doc.tipoDocumento}</TableCell>
                    <TableCell>{doc.clasificacion}</TableCell>
                    <TableCell sx={{ minWidth: 150 }}>
                      <TextField
                        size="small"
                        fullWidth
                        disabled={!puedeEditar}
                        value={doc.numeroDocumento}
                        onChange={(e) => actualizarDocumentoLocal(doc.idDocumentoAnalizado, { numeroDocumento: e.target.value })}
                      />
                    </TableCell>
                    <TableCell sx={{ minWidth: 160 }}>
                      <TextField
                        select
                        size="small"
                        fullWidth
                        disabled={!puedeEditar}
                        value={doc.estadoDocumentoCodigo}
                        onChange={(e) => handleCambiarEstado(doc, e.target.value)}
                      >
                        <MenuItem value={doc.estadoDocumentoCodigo}>{doc.estadoDocumento || doc.estadoDocumentoCodigo}</MenuItem>
                        {estadosDocumento
                          .filter((op) => op.codigo !== doc.estadoDocumentoCodigo)
                          .map((op) => (
                            <MenuItem key={op.codigo} value={op.codigo ?? ''}>
                              {op.nombre}
                            </MenuItem>
                          ))}
                      </TextField>
                    </TableCell>
                    <TableCell sx={{ minWidth: 150 }}>
                      <TextField
                        size="small"
                        type="date"
                        fullWidth
                        disabled={!puedeEditar}
                        value={doc.fechaDocumento ?? ''}
                        onChange={(e) => actualizarDocumentoLocal(doc.idDocumentoAnalizado, { fechaDocumento: e.target.value || null })}
                      />
                    </TableCell>
                  </TableRow>
                )
              })}
              {padres.length === 0 && !cargando && (
                <TableRow>
                  <TableCell colSpan={6}>
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

      <SectionCard title="Resultado de la supervisión">
        <Stack spacing={2}>
          {supervisionAprobada && (
            // 01/09/2026, pedido explícito del usuario: estandarizar el mismo patrón ya aplicado en
            // Análisis/Verificación/Ejecución — `supervisionAprobada` YA se releía del backend en
            // cada remount (`existeSupervisionAprobadaNotificacion`), pero solo se mostraba como un
            // `Typography caption` gris casi invisible, sin la alerta verde persistida que sí tienen
            // los demás paneles.
            <Alert severity="success">Ya se registró la supervisión Aprobada de este documento. Use el botón "Asignar" para derivarlo a Notificación.</Alert>
          )}
          <TextField
            select
            label="Resultado"
            size="small"
            fullWidth
            disabled={supervisionAprobada}
            value={resultadoSupervision}
            onChange={(e) => setResultadoSupervision(e.target.value as 'APROBADO' | 'OBSERVADO')}
          >
            <MenuItem value="APROBADO">Aprobado</MenuItem>
            <MenuItem value="OBSERVADO">Observado</MenuItem>
          </TextField>
          {resultadoSupervision === 'OBSERVADO' && (
            <>
              <TextField
                label="Comentario (motivo de la observación)"
                size="small"
                fullWidth
                multiline
                minRows={2}
                disabled={supervisionAprobada}
                value={comentarioSupervision}
                onChange={(e) => setComentarioSupervision(e.target.value)}
              />
              <TextField
                select
                label="Motivo de corrección"
                size="small"
                fullWidth
                disabled={supervisionAprobada}
                value={motivoCorreccionCodigo}
                onChange={(e) => setMotivoCorreccionCodigo(e.target.value)}
              >
                <MenuItem value="">Seleccione motivo</MenuItem>
                {motivosCorreccion.map((m) => (
                  <MenuItem key={m.codigo} value={m.codigo ?? ''}>
                    {m.nombre}
                  </MenuItem>
                ))}
              </TextField>
            </>
          )}
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
            <Button
              variant="outlined"
              size="small"
              disabled={procesandoSupervision || supervisionAprobada}
              onClick={handleRegistrarSupervision}
            >
              Registrar Supervisión
            </Button>
            {mensajeSupervision && (
              <Alert severity="success" sx={{ py: 0 }}>
                {mensajeSupervision}
              </Alert>
            )}
            {errorSupervision && (
              <Alert severity="error" sx={{ py: 0 }}>
                {errorSupervision}
              </Alert>
            )}
          </Stack>
        </Stack>
      </SectionCard>

      {resultadoSupervision === 'OBSERVADO' && !supervisionAprobada && (
        // 02/09/2026, pedido explícito del usuario: restaura el bloque "Destino operativo" que
        // existía antes del 28/08/2026 (cuando Asignación y Supervisión eran un solo panel) —
        // visible siempre que el resultado elegido sea Observado, para derivar el documento a
        // Equipo Análisis o Equipo Ejecución en el mismo registro, sin pasar por el botón "Asignar"
        // del toolbar (que solo deriva a Validación/Notificación).
        <SectionCard title="Destino operativo">
          <Stack spacing={2}>
            <TextField
              select
              label="Equipo destino"
              size="small"
              fullWidth
              value={idEquipoDestinoObservado}
              onChange={(e) => setIdEquipoDestinoObservado(e.target.value)}
            >
              <MenuItem value="">Seleccione equipo</MenuItem>
              {equiposObservado.map((eq) => (
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
              disabled={!idEquipoDestinoObservado}
              value={idUsuarioDestinoObservado}
              onChange={(e) => setIdUsuarioDestinoObservado(e.target.value)}
            >
              <MenuItem value="">Seleccione</MenuItem>
              {usuariosObservado.map((u) => (
                <MenuItem key={u.idUsuario} value={String(u.idUsuario)}>
                  {u.displayName}
                </MenuItem>
              ))}
            </TextField>
          </Stack>
        </SectionCard>
      )}
    </Stack>
  )
}
