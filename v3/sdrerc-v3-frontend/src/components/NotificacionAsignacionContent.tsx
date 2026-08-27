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
  existeSupervisionAprobadaNotificacion,
  generarAsignacionNotificacion,
  guardarFirmaDocumentoNotificacion,
  listarDocumentosPorExpedienteNotificacion,
  listarEquiposDestinoNotificacion,
  listarEstadosDocumentoNotificacion,
  listarHistorialAsignacionNotificacion,
  listarUsuariosDestinoNotificacion,
  registrarSupervisionNotificacion,
  type NotificacionAsignacionDocumento,
} from '../api/notificacionAsignacionApi'
import type { CatalogoItem } from '../api/registroApi'
import type { EquipoAsignacion, UsuarioAsignable, AsignacionHistorial } from '../api/asignacionApi'
import { ApiError } from '../api/http'
import { SectionCard } from './RegistroExpedienteForm'
import { StatusBadge } from './StatusBadge'

interface NotificacionAsignacionContentProps {
  idExpediente: number
  documentoFoco: NotificacionAsignacionDocumento
  onActualizado: () => void
}

function formatearFechaHora(iso: string | null): string {
  if (!iso) return '-'
  const [fecha, hora] = iso.split('T')
  const [anio, mes, dia] = fecha.split('-')
  const hhmm = hora ? hora.slice(0, 5) : '00:00'
  return `${dia}/${mes}/${anio} ${hhmm}`
}

/**
 * Subpestaña "Asignación" de la Bandeja Asignación de Notificación — implementa los Momentos 1 y 3
 * del flujo real de Notificación (el Momento 2, devolución automática Validador→Supervisor cuando
 * estado=VALIDADO + resultado=Aprobado, NO se implementa: solo se usa su condición de disparo como
 * filtro de "ya está lista", ver más abajo).
 *
 * **2 partes claramente separadas (21/08/2026, pedido explícito del usuario — corrige una versión
 * anterior que mostraba "Documentos para emitir" y "Documentos a asignar" siempre juntos, algo que
 * el usuario señaló como incorrecto)**, mismo criterio de "1ra parte / 2da parte" ya usado en
 * `AnalisisContent.tsx` (ahí: "Recibir expediente" vs "Documentos analizados"/"Resultado del
 * análisis"; acá: "Asignación" vs "Emitir") — identificadas con un rótulo en negrita al inicio del
 * `Alert` superior, sin mecanismo de espera/transición-en-caliente (a diferencia de Análisis, cuyo
 * cambio de parte lo dispara un botón DENTRO del mismo panel — acá el cambio de parte lo dispara un
 * actor externo, el Validador en la todavía no construida Bandeja Validación, así que el panel
 * simplemente refleja el estado vigente de `documentoFoco` cada vez que se abre/refresca, sin
 * necesidad de un Snackbar):
 *
 * - **Parte "Asignación"** (`!parteEmitir`): Carta Intermedia (SIEMPRE cae acá, nunca pasa por
 *   Validador) o Carta Final recién llegada/observada (`estadoDocumentoCodigo` distinto de
 *   `VALIDADO`/`EMITIDO` — incluye `EN_DESPACHO` y `OBSERVADO`, este último ya una vez devuelto por
 *   el validador, tratado con la misma mecánica de reasignación, sin UI separada). Muestra
 *   ÚNICAMENTE "Documentos a asignar" (mini-grilla de revisión + Hoja de envío nueva) + "Destino
 *   operativo", forzado a `EQ_NOTIFICACION` (Intermedia) o `EQ_VALIDACION` (Final) — SIN "Documentos
 *   para emitir" ni "Resultado de Supervisión": una Carta Final recién llegada no se puede emitir
 *   todavía (falta que el Validador la apruebe primero).
 * - **Parte "Emitir"** (`parteEmitir`): EXCLUSIVA de Carta Final con `estadoDocumentoCodigo` en
 *   `VALIDADO`/`EMITIDO` (`VALIDADO`+`Aprobado` es la condición de disparo documentada para el
 *   Momento 2, usada acá solo como filtro — la devolución automática en sí no se construye). Muestra
 *   "Documentos para emitir" (mismo estilo que "Documentos analizados" de Análisis — ícono Guardar
 *   al inicio, sin alta de filas — editable N° Documento/Estado documento/Fecha Emisión) +
 *   "Resultado de Supervisión" (auditoría, sin mover estado) + "Destino operativo" (forzado a
 *   `EQ_NOTIFICACION`, con la Hoja de envío como campo suelto dentro del propio bloque en vez de
 *   columna de grilla, ya que "Documentos a asignar" no se muestra en esta parte) — SIN "Documentos
 *   a asignar".
 *
 * "Destino operativo" es el único bloque común a ambas partes — solo cambia el equipo forzado
 * (`equipoObjetivoCodigo`) y el conjunto de documentos sobre los que opera (`documentosParaAsignar`).
 * El combo "Equipo destino" reduce su lista de opciones a la única válida para la parte activa (se
 * autoselecciona sola, sin mostrar una ambigüedad que no existe).
 *
 * **Campo "Comentario" retirado** (era huérfano, igual que en Asignación V2): verificado en el
 * backend (`NotificacionAsignacionService.generarAsignacion`) que el parámetro nunca se persiste en
 * la rama `EQ_VALIDACION`/`EQ_NOTIFICACION` (`DocumentoAnalisisDAO.asignarNotificacionMultiple` ni
 * siquiera lo recibe) — las 2 partes de esta entrega pasan siempre por esa rama. **"Habilitar
 * reasignación" tampoco es un checkbox manual**: se deriva solo (`documentosParaAsignar.some(d =>
 * d.asignado)`), mismo criterio que `AsignacionSeleccionContent.tsx`.
 *
 * `EQ_EJECUCION` (destino ya soportado por el backend, para derivar un documento Observado de vuelta
 * a Ejecución) se retiró de este panel a propósito: no forma parte de las 2 partes de esta entrega.
 *
 * **Sin cambios de base de datos para este ajuste**: los estados/equipos/transiciones ya existentes
 * (EN_DESPACHO/VALIDADO/EMITIDO/OBSERVADO, EQ_VALIDACION/EQ_NOTIFICACION) no cambian — este es un
 * ajuste puramente de qué bloques renderiza el frontend según la combinación clasificación+estado
 * ya disponible en `NotificacionAsignacionDocumentoDTO`.
 */
export function NotificacionAsignacionContent({ idExpediente, documentoFoco, onActualizado }: NotificacionAsignacionContentProps) {
  const [documentos, setDocumentos] = useState<NotificacionAsignacionDocumento[]>([])
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [hojasEnvio, setHojasEnvio] = useState<Record<number, string>>({})
  const [estadosDocumento, setEstadosDocumento] = useState<CatalogoItem[]>([])

  const [equipos, setEquipos] = useState<EquipoAsignacion[]>([])
  const [usuarios, setUsuarios] = useState<UsuarioAsignable[]>([])
  const [idEquipoDestino, setIdEquipoDestino] = useState('')
  const [idUsuarioDestino, setIdUsuarioDestino] = useState('')
  const [procesandoAsignacion, setProcesandoAsignacion] = useState(false)
  const [mensajeAsignacion, setMensajeAsignacion] = useState<string | null>(null)
  const [errorAsignacion, setErrorAsignacion] = useState<string | null>(null)

  const [resultadoSupervision, setResultadoSupervision] = useState<'APROBADO' | 'OBSERVADO'>('APROBADO')
  const [comentarioSupervision, setComentarioSupervision] = useState('')
  const [supervisionAprobada, setSupervisionAprobada] = useState(false)
  const [procesandoSupervision, setProcesandoSupervision] = useState(false)
  const [mensajeSupervision, setMensajeSupervision] = useState<string | null>(null)
  const [errorSupervision, setErrorSupervision] = useState<string | null>(null)

  const [historial, setHistorial] = useState<AsignacionHistorial[]>([])

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
    setHojasEnvio({})
    setResultadoSupervision('APROBADO')
    setComentarioSupervision('')
    setMensajeAsignacion(null)
    setErrorAsignacion(null)
    setMensajeSupervision(null)
    setErrorSupervision(null)
    existeSupervisionAprobadaNotificacion(documentoFoco.idDocumentoAnalizado)
      .then(setSupervisionAprobada)
      .catch(() => setSupervisionAprobada(false))
    listarHistorialAsignacionNotificacion(documentoFoco.idDocumentoAnalizado)
      .then(setHistorial)
      .catch(() => setHistorial([]))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idExpediente, documentoFoco.idDocumentoAnalizado])

  useEffect(() => {
    listarEstadosDocumentoNotificacion().then(setEstadosDocumento).catch(() => setEstadosDocumento([]))
    listarEquiposDestinoNotificacion().then(setEquipos).catch(() => setEquipos([]))
  }, [])

  // 2 partes del documento enfocado (ver Javadoc de la clase para la explicación completa).
  const esFinal = documentoFoco.clasificacion === 'FINAL'
  const esIntermedio = documentoFoco.clasificacion === 'INTERMEDIO'
  const parteEmitir = esFinal && (documentoFoco.estadoDocumentoCodigo === 'VALIDADO' || documentoFoco.estadoDocumentoCodigo === 'EMITIDO')

  // "Documentos para emitir" (parte Emitir): EXCLUSIVAMENTE cartas finales ya Validado/Emitido.
  const documentosEmitir = useMemo(
    () => documentos.filter((d) => d.clasificacion === 'FINAL' && (d.estadoDocumentoCodigo === 'VALIDADO' || d.estadoDocumentoCodigo === 'EMITIDO')),
    [documentos],
  )
  // "Documentos a asignar" (parte Asignación): cartas intermedias (siempre) + cartas finales aún no
  // validadas/emitidas (recién llegadas u observadas).
  // 21/08/2026, bug real corregido tras verificar contra datos reales en Oracle: una Carta Intermedia
  // TODAVÍA en EN_DESPACHO (aún no emitida desde Verificación) también matcheaba `clasificacion ===
  // 'INTERMEDIO'` sin exigir su propio estado — `listarDocumentosPorExpedienteNotificacion` trae
  // TODOS los documentos del expediente, así que un intermedio abandonado/incompleto de una etapa
  // anterior podía colarse acá invitando a asignar un documento que ni siquiera fue emitido. Mismo
  // criterio exacto que exige `CONDICION_ASIGNACION_NOTIFICACION` (backend): INTERMEDIO solo cuenta
  // si ya está EMITIDO.
  const documentosAsignar = useMemo(
    () =>
      documentos.filter(
        (d) =>
          (d.clasificacion === 'INTERMEDIO' && d.estadoDocumentoCodigo === 'EMITIDO') ||
          (d.clasificacion === 'FINAL' && d.estadoDocumentoCodigo !== 'VALIDADO' && d.estadoDocumentoCodigo !== 'EMITIDO'),
      ),
    [documentos],
  )
  const documentosParaAsignar = parteEmitir ? documentosEmitir : documentosAsignar
  // Detección automática de "Asignación inicial"/"Reasignación" (mismo criterio que
  // `AsignacionSeleccionContent`/`NotificacionAsignacionMultipleContent`) — reemplaza cualquier
  // checkbox manual de "Habilitar reasignación".
  const esReasignacion = documentosParaAsignar.some((d) => d.asignado)

  const equipoObjetivoCodigo = parteEmitir ? 'EQ_NOTIFICACION' : esIntermedio ? 'EQ_NOTIFICACION' : 'EQ_VALIDACION'
  const equiposFiltrados = useMemo(() => equipos.filter((eq) => eq.codigo === equipoObjetivoCodigo), [equipos, equipoObjetivoCodigo])
  const equipoObjetivo = equiposFiltrados[0] ?? null

  useEffect(() => {
    setIdEquipoDestino(equipoObjetivo ? String(equipoObjetivo.idEquipo) : '')
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [equipoObjetivo?.idEquipo])

  useEffect(() => {
    if (!idEquipoDestino) {
      setUsuarios([])
      setIdUsuarioDestino('')
      return
    }
    listarUsuariosDestinoNotificacion(Number(idEquipoDestino)).then(setUsuarios).catch(() => setUsuarios([]))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idEquipoDestino])

  const usuarioSeleccionado = usuarios.find((u) => String(u.idUsuario) === idUsuarioDestino) ?? null

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

  function limpiarAsignacion() {
    setHojasEnvio({})
    setIdUsuarioDestino('')
    setErrorAsignacion(null)
  }

  async function handleGenerarAsignacion() {
    setErrorAsignacion(null)
    setMensajeAsignacion(null)
    const ids = documentosParaAsignar.map((d) => d.idDocumentoAnalizado)
    if (ids.length === 0) return
    if (!idEquipoDestino || !idUsuarioDestino) {
      setErrorAsignacion('Seleccione un usuario destino.')
      return
    }
    if (!window.confirm(`¿Generar asignación de ${ids.length} documento(s)?`)) return
    setProcesandoAsignacion(true)
    try {
      await generarAsignacionNotificacion({
        idsDocumentoAnalizado: ids,
        idEquipoDestino: Number(idEquipoDestino),
        idUsuarioDestino: Number(idUsuarioDestino),
        hojasEnvioPorDocumento: hojasEnvio,
        comentario: '',
        reasignacion: esReasignacion,
      })
      setMensajeAsignacion(`${ids.length} documento(s) asignado(s) correctamente${usuarioSeleccionado ? ` a ${usuarioSeleccionado.displayName}` : ''}.`)
      limpiarAsignacion()
      cargar()
      onActualizado()
      // 22/08/2026, pedido explícito del usuario: "Generar asignación" ya guardaba el historial en
      // BD (`asignarNotificacionMultiple` inserta el movimiento), pero el bloque "Historial de
      // asignación / reasignación" de este mismo panel solo se cargaba una vez al enfocar el
      // documento (`useEffect` de arriba) — quedaba desactualizado hasta cerrar/reabrir el panel.
      listarHistorialAsignacionNotificacion(documentoFoco.idDocumentoAnalizado)
        .then(setHistorial)
        .catch(() => {})
    } catch (e) {
      setErrorAsignacion(e instanceof ApiError ? e.message : 'No se pudo generar la asignación.')
    } finally {
      setProcesandoAsignacion(false)
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
      setMensajeSupervision('Documento observado. Use "Destino operativo" para derivarlo nuevamente a Validación.')
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
      setMensajeSupervision('Supervisión registrada correctamente.')
      onActualizado()
    } catch (e) {
      setErrorSupervision(e instanceof ApiError ? e.message : 'No se pudo registrar la supervisión.')
    } finally {
      setProcesandoSupervision(false)
    }
  }

  return (
    <Stack spacing={3}>
      <Alert severity={parteEmitir ? 'success' : 'info'}>
        <strong>{parteEmitir ? 'Emitir' : 'Asignación'}</strong> — {documentoFoco.numeroExpediente} — {documentoFoco.titular} ·{' '}
        {documentoFoco.tipoDocumento} ({documentoFoco.clasificacion}) · Estado doc.: {documentoFoco.estadoDocumento || '-'}
      </Alert>
      {error && <Alert severity="error">{error}</Alert>}

      {parteEmitir && (
        <SectionCard title="Documentos para emitir">
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
                {documentosEmitir.map((doc) => (
                  <TableRow key={doc.idDocumentoAnalizado}>
                    <TableCell>
                      <Tooltip title="Guardar">
                        <IconButton size="small" onClick={() => handleGuardarFirma(doc)}>
                          <SaveOutlinedIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                    <TableCell>{doc.tipoDocumento}</TableCell>
                    <TableCell sx={{ minWidth: 150 }}>
                      <TextField
                        size="small"
                        fullWidth
                        value={doc.numeroDocumento}
                        onChange={(e) => actualizarDocumentoLocal(doc.idDocumentoAnalizado, { numeroDocumento: e.target.value })}
                      />
                    </TableCell>
                    <TableCell sx={{ minWidth: 160 }}>
                      <TextField
                        select
                        size="small"
                        fullWidth
                        value={doc.estadoDocumentoCodigo}
                        onChange={(e) => handleCambiarEstado(doc, e.target.value)}
                      >
                        {estadosDocumento.map((op) => (
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
                        value={doc.fechaDocumento ?? ''}
                        onChange={(e) => actualizarDocumentoLocal(doc.idDocumentoAnalizado, { fechaDocumento: e.target.value || null })}
                      />
                    </TableCell>
                  </TableRow>
                ))}
                {documentosEmitir.length === 0 && !cargando && (
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
      )}

      {parteEmitir && (
        <SectionCard title="Resultado de Supervisión">
          <Stack spacing={2}>
            <TextField
              select
              label="Resultado"
              size="small"
              fullWidth
              value={resultadoSupervision}
              onChange={(e) => setResultadoSupervision(e.target.value as 'APROBADO' | 'OBSERVADO')}
            >
              <MenuItem value="APROBADO">Aprobado</MenuItem>
              <MenuItem value="OBSERVADO">Observado</MenuItem>
            </TextField>
            {resultadoSupervision === 'OBSERVADO' && (
              <TextField
                label="Comentario (motivo de la observación)"
                size="small"
                fullWidth
                multiline
                minRows={2}
                value={comentarioSupervision}
                onChange={(e) => setComentarioSupervision(e.target.value)}
              />
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
              {mensajeSupervision && <Alert severity="success" sx={{ py: 0 }}>{mensajeSupervision}</Alert>}
              {errorSupervision && <Alert severity="error" sx={{ py: 0 }}>{errorSupervision}</Alert>}
            </Stack>
            {supervisionAprobada && (
              <Typography variant="caption" color="text.secondary">
                Ya se registró la supervisión Aprobada de este documento.
              </Typography>
            )}
          </Stack>
        </SectionCard>
      )}

      {!parteEmitir && (
        <SectionCard title={`Documentos a asignar (${documentosAsignar.length})`}>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Tipo documento</TableCell>
                  <TableCell>N° Documento</TableCell>
                  <TableCell>Hoja de envío actual</TableCell>
                  <TableCell>Responsable actual</TableCell>
                  <TableCell>Tipo de operación</TableCell>
                  <TableCell>Hoja de envío nueva</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {documentosAsignar.map((doc) => (
                  <TableRow key={doc.idDocumentoAnalizado}>
                    <TableCell>{doc.tipoDocumento}</TableCell>
                    <TableCell>{doc.numeroDocumento || '-'}</TableCell>
                    <TableCell>{doc.numeroHojaEnvioNotificacion || '-'}</TableCell>
                    <TableCell>{doc.usuarioNotificacionActual || '-'}</TableCell>
                    <TableCell>
                      {doc.asignado ? (
                        <StatusBadge label="Reasignación" tone="warning" />
                      ) : (
                        <StatusBadge label="Asignación inicial" tone="info" />
                      )}
                    </TableCell>
                    <TableCell sx={{ minWidth: 180 }}>
                      <TextField
                        size="small"
                        fullWidth
                        placeholder="N° hoja de envío"
                        value={hojasEnvio[doc.idDocumentoAnalizado] ?? ''}
                        onChange={(e) => setHojasEnvio((prev) => ({ ...prev, [doc.idDocumentoAnalizado]: e.target.value }))}
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
      )}

      <SectionCard title="Destino operativo">
        <Stack spacing={2}>
          <TextField
            label="Responsable actual"
            size="small"
            fullWidth
            value={documentoFoco.asignado ? `${documentoFoco.usuarioNotificacionActual || '-'} (reasignación)` : 'Sin asignación previa (nuevo)'}
            slotProps={{ input: { readOnly: true } }}
          />
          {/* La Hoja de envío ya se captura como columna dentro de "Documentos a asignar" en esa
              parte — acá solo aparece cuando esa grilla no se muestra (parte Emitir). */}
          {parteEmitir &&
            documentosParaAsignar.map((doc) => (
              <TextField
                key={doc.idDocumentoAnalizado}
                label={`Hoja de envío — ${doc.tipoDocumento}`}
                size="small"
                fullWidth
                value={hojasEnvio[doc.idDocumentoAnalizado] ?? ''}
                onChange={(e) => setHojasEnvio((prev) => ({ ...prev, [doc.idDocumentoAnalizado]: e.target.value }))}
              />
            ))}
          <TextField
            select
            label="Equipo destino"
            size="small"
            fullWidth
            value={idEquipoDestino}
            onChange={(e) => setIdEquipoDestino(e.target.value)}
          >
            {equiposFiltrados.length === 0 && <MenuItem value="">Cargando...</MenuItem>}
            {equiposFiltrados.map((eq) => (
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
          <TextField
            label="Supervisor"
            size="small"
            fullWidth
            value={usuarioSeleccionado?.supervisorNombre || '-'}
            slotProps={{ input: { readOnly: true } }}
          />
        </Stack>
        <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
          <Button variant="contained" onClick={handleGenerarAsignacion} disabled={procesandoAsignacion || !idEquipoDestino || !idUsuarioDestino}>
            Generar asignación
          </Button>
          <Button variant="outlined" onClick={limpiarAsignacion} disabled={procesandoAsignacion}>
            Limpiar
          </Button>
        </Stack>
        {mensajeAsignacion && (
          <Alert severity="success" sx={{ mt: 2 }} onClose={() => setMensajeAsignacion(null)}>
            {mensajeAsignacion}
          </Alert>
        )}
        {errorAsignacion && (
          <Alert severity="error" sx={{ mt: 2 }} onClose={() => setErrorAsignacion(null)}>
            {errorAsignacion}
          </Alert>
        )}
      </SectionCard>

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
    </Stack>
  )
}
