import { useEffect, useMemo, useRef, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Checkbox,
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
import AddOutlinedIcon from '@mui/icons-material/AddOutlined'
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined'
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined'
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined'
import {
  descargarDocumentoWordEjecucion,
  eliminarDocumentoEjecucion,
  enviarSupervision,
  guardarDocumentoEjecucion,
  listarDocumentosEjecucion,
  listarResultadosEjecucion,
  listarUsuariosSupervision,
  registrarResultadoEjecucion,
  type EjecucionExpediente,
} from '../api/ejecucionApi'
import type { UsuarioAsignable } from '../api/asignacionApi'
import { listarEstadosDocumentoAnalisis, listarTiposDocumentoAdjuntoAnalisis, type DocumentoAnalizado } from '../api/analisisApi'
import type { CatalogoItem } from '../api/registroApi'
import { ApiError } from '../api/http'
import { SectionCard } from './RegistroExpedienteForm'
import { StatusBadge } from './StatusBadge'

interface EjecucionContentProps {
  expediente: EjecucionExpediente
  onActualizado?: () => void
}

/** Mismo helper que `documentoVacio` de `AnalisisContent.tsx` (fila nueva con id temporal negativo). */
function documentoVacio(idExpediente: number, nivel: 0 | 1, idDocumentoPadre: number | null, idTemporal: number): DocumentoAnalizado {
  return {
    idDocumentoAnalizado: idTemporal,
    idExpediente,
    idExpedienteAnalisis: null,
    tipoDocumentoCodigo: '',
    tipoDocumentoNombre: '',
    estadoDocumentoCodigo: nivel === 0 ? 'EN_PROYECTO' : '',
    estadoDocumentoNombre: nivel === 0 ? 'En proyecto' : '',
    fechaDocumento: null,
    numeroDocumento: '',
    descripcion: '',
    notificado: false,
    fechaAcuse: null,
    requiereRespuesta: false,
    confirmacionRespuesta: 'PENDIENTE',
    fechaRespuesta: null,
    numeroHojaEnvioRespuesta: '',
    requierePublicacion: false,
    fechaPublicacion: null,
    detalleObservacion: '',
    idDocumentoPadre,
    nivel,
    orden: 0,
    estadoRespuesta: '',
    activo: true,
    fechaRegistro: null,
    fechaModificacion: null,
    existeOposicion: null,
  }
}

/** Los 3 códigos de carta de notificación cuyo "Tipo documento" V2 permite editar en esta grilla (el resto queda de solo lectura, igual que Verificación). */
const TIPOS_CARTA_NOTIFICACION = new Set([
  'ANALISIS_DOC_17_CARTA_IMPROCEDENTE',
  'ANALISIS_DOC_18_CARTA_PROCEDENTE',
  'ANALISIS_DOC_19_CARTA_PROCEDENTE_EN_PARTE',
])

/**
 * Contenido de la subpestaña "Ejecutar" del workspace multipestañas de la Bandeja Ejecución.
 * "Datos" (la otra subpestaña) es la misma de Registro/Análisis/Verificación, con
 * `permiteEdicion={false}`.
 *
 * 5 secciones: Estado de la ejecución → Documentos ejecutados → Documentos de respuesta →
 * Resultado de ejecución → Enviar a Supervisión (mismo orden que Verificación: documentos antes
 * que resultado).
 *
 * **"Guardar Ejecución" de V2 separado en 2 acciones independientes** (pedido explícito del
 * usuario, resuelve de forma honesta la falta de atomicidad real de V2 — allá son 2 transacciones
 * SQL separadas con commits independientes bajo el mismo click):
 * - "Registrar resultado de ejecución" ejecuta SOLO `INICIO_EJECUCION`.
 * - "Enviar a Supervisión" (NO "Destino operativo" — V2 hardcodea el combo de equipo a un único
 *   equipo real `EQ_SUPERVISION`, sin combo de equipo en V3, solo combo de usuario destino) ejecuta
 *   SOLO `DERIVACION_A_NOTIFICACION`, con su propia validación de carta final en despacho.
 *
 * **"Observación / Error material / Devolver a Análisis" retirada del panel (21/08/2026, pedido
 * explícito del usuario)**: se había expuesto en un incremento anterior como código huérfano de V2
 * (existía Service/DAO con transiciones activas, pero ningún botón visible en el JPanel real) — el
 * usuario pidió quitarla de nuevo: esas 3 acciones correspondían al rol "supervisor de ejecución",
 * que ya no existe en el diseño vigente, así que no hace falta registrar esa información desde
 * ningún panel. El Service/DAO backend (`registrarObservacionEjecucion`/`registrarErrorMaterial`/
 * `devolverAnalisisDesdeEjecucion`, endpoints `/api/ejecucion/observacion`/`/error-material`/
 * `/devolver-analisis`) NO se tocó — sigue existiendo, simplemente sin ningún botón que lo invoque,
 * mismo estado que tenía antes de exponerse. El `StatusBadge` "Error material" en "Estado de la
 * ejecución" (informativo, basado en `expediente.corregible`) SÍ se conserva: sigue siendo útil ver
 * si un expediente quedó en ese estado por otra vía, aunque ya no haya una acción para registrarlo
 * desde acá.
 *
 * La grilla "Documentos ejecutados" reutiliza el mismo dato que Análisis/Verificación
 * (`EXPEDIENTE_DOCUMENTO_ANALIZADO`, vía `DocumentoAnalisisDAO`), expuesto bajo `/api/ejecucion`.
 * Diferencia real respecto a ambos: "Tipo documento" es editable SOLO cuando el tipo actual ya es
 * una de las 3 cartas de notificación (`TIPOS_CARTA_NOTIFICACION`) — ni siempre editable (Análisis)
 * ni siempre de solo lectura (Verificación).
 *
 * **"+Documento"/"+Relacionado" + 3 iconos (Guardar/Word/Eliminar) agregados (21/08/2026, pedido
 * explícito del usuario: "similar como en el panel de análisis")** — antes esta grilla era de solo
 * lectura salvo por el ícono "Guardar" (no se podían crear filas nuevas, y solo tenía ese 1 ícono).
 * Se replicó literalmente el patrón de `AnalisisContent.tsx` (mismo `documentoVacio`/`tempIdRef` con
 * id temporal negativo, mismos handlers `handleAgregarDocumento`/`handleAgregarRelacionado`/
 * `handleEliminarFila`/`handleDescargarWord`): fila padre gana Guardar+Word+Eliminar (antes solo
 * Guardar) y el checkbox "¿Requiere respuesta?" pasa de texto de solo lectura a editable (necesario
 * para poder configurar una fila recién creada); fila hija (Documentos de respuesta) gana un botón
 * "+Relacionado" y Guardar+Eliminar (sin Word, igual que en Análisis). **Diverge a propósito del
 * criterio documentado en el `CLAUDE.md` raíz** ("Ejecucion tiene Guardar, Word", sin Eliminar) — el
 * usuario pidió explícitamente los mismos 3 iconos que Análisis, no los 2 que V2 documentaba.
 * Backend: `EjecucionExpedienteService`/`EjecucionController` ganaron `eliminarDocumentoEjecucion`/
 * `generarDocumentoWordEjecucion` (mismos passthroughs delgados que ya existían en Análisis, sobre
 * la misma tabla) — la generación de Word reutiliza `AnalisisExpedienteDAO#obtenerAnalisisRegistrado`
 * (sin restricción de etapa) para la variable `#resAnalisis#`, igual que Análisis.
 */
export function EjecucionContent({ expediente, onActualizado }: EjecucionContentProps) {
  const [comentarioResultado, setComentarioResultado] = useState('')
  const [resultadoCodigo, setResultadoCodigo] = useState('')
  const [fechaEjecucion, setFechaEjecucion] = useState<string | null>(null)
  const [registrandoResultado, setRegistrandoResultado] = useState(false)

  const [comentarioSupervision, setComentarioSupervision] = useState('')
  const [usuarios, setUsuarios] = useState<UsuarioAsignable[]>([])
  const [idUsuarioDestino, setIdUsuarioDestino] = useState('')
  const [enviandoSupervision, setEnviandoSupervision] = useState(false)

  const [resultados, setResultados] = useState<CatalogoItem[]>([])
  const [tiposDocumento, setTiposDocumento] = useState<CatalogoItem[]>([])
  const [estadosDocumento, setEstadosDocumento] = useState<CatalogoItem[]>([])

  const [documentos, setDocumentos] = useState<DocumentoAnalizado[]>([])
  const [documentosLoading, setDocumentosLoading] = useState(false)
  const [padreFocoId, setPadreFocoId] = useState<number | null>(null)
  const [guardandoId, setGuardandoId] = useState<number | null>(null)
  const tempIdRef = useRef(-1)

  // Error genérico, solo para el ícono "Guardar" de la grilla "Documentos ejecutados". "Registrar
  // resultado de ejecución"/"Enviar a Supervisión" tienen su propio par mensaje/error, mostrado a la
  // derecha de su propio botón (21/08/2026, pedido explícito del usuario, mismo patrón ya aplicado
  // en Análisis/Verificación).
  const [error, setError] = useState<string | null>(null)
  const [mensajeResultado, setMensajeResultado] = useState<string | null>(null)
  const [errorResultado, setErrorResultado] = useState<string | null>(null)
  const [mensajeSupervision, setMensajeSupervision] = useState<string | null>(null)
  const [errorSupervision, setErrorSupervision] = useState<string | null>(null)

  const enEjecucion = expediente.etapaCodigo === 'EJECUCION'

  useEffect(() => {
    Promise.all([listarResultadosEjecucion(), listarTiposDocumentoAdjuntoAnalisis(), listarEstadosDocumentoAnalisis()])
      .then(([r, td, ed]) => {
        setResultados(r)
        setTiposDocumento(td)
        setEstadosDocumento(ed)
      })
      .catch(() => undefined)
    listarUsuariosSupervision()
      .then(setUsuarios)
      .catch(() => setUsuarios([]))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    setComentarioResultado('')
    setResultadoCodigo('')
    setFechaEjecucion(null)
    setComentarioSupervision('')
    setIdUsuarioDestino('')
    setError(null)
    setMensajeResultado(null)
    setErrorResultado(null)
    setMensajeSupervision(null)
    setErrorSupervision(null)
    recargarDocumentos()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [expediente.idExpediente])

  function recargarDocumentos() {
    setDocumentosLoading(true)
    listarDocumentosEjecucion(expediente.idExpediente)
      .then(setDocumentos)
      .catch(() => setDocumentos([]))
      .finally(() => setDocumentosLoading(false))
  }

  const padres = useMemo(() => documentos.filter((d) => d.nivel === 0 && d.activo), [documentos])
  const hijos = useMemo(
    () => documentos.filter((d) => d.nivel === 1 && d.activo && d.idDocumentoPadre === padreFocoId),
    [documentos, padreFocoId],
  )
  const padreFoco = padres.find((p) => p.idDocumentoAnalizado === padreFocoId) ?? null

  function actualizarDocumento(id: number, cambios: Partial<DocumentoAnalizado>) {
    setDocumentos((prev) => prev.map((d) => (d.idDocumentoAnalizado === id ? { ...d, ...cambios } : d)))
  }

  function handleAgregarDocumento() {
    const id = tempIdRef.current--
    setDocumentos((prev) => [...prev, documentoVacio(expediente.idExpediente, 0, null, id)])
    setPadreFocoId(id)
  }

  function handleAgregarRelacionado() {
    if (!padreFoco || padreFoco.idDocumentoAnalizado === null || padreFoco.idDocumentoAnalizado < 0) return
    const id = tempIdRef.current--
    setDocumentos((prev) => [...prev, documentoVacio(expediente.idExpediente, 1, padreFoco.idDocumentoAnalizado, id)])
  }

  async function handleGuardarFila(doc: DocumentoAnalizado) {
    setGuardandoId(doc.idDocumentoAnalizado)
    setError(null)
    try {
      const payload: DocumentoAnalizado = {
        ...doc,
        idExpediente: expediente.idExpediente,
        idDocumentoAnalizado: (doc.idDocumentoAnalizado ?? 0) < 0 ? null : doc.idDocumentoAnalizado,
      }
      const idReal = await guardarDocumentoEjecucion(payload)
      const idAnterior = doc.idDocumentoAnalizado
      const lista = await listarDocumentosEjecucion(expediente.idExpediente)
      setDocumentos((prev) => {
        const pendientes = prev.filter((d) => d.idDocumentoAnalizado !== idAnterior && (d.idDocumentoAnalizado ?? 0) < 0)
        return [...lista, ...pendientes]
      })
      if (padreFocoId === idAnterior) {
        setPadreFocoId(idReal)
      }
      onActualizado?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo guardar el documento.')
    } finally {
      setGuardandoId(null)
    }
  }

  async function handleEliminarFila(doc: DocumentoAnalizado) {
    if ((doc.idDocumentoAnalizado ?? 0) < 0) {
      setDocumentos((prev) => prev.filter((d) => d.idDocumentoAnalizado !== doc.idDocumentoAnalizado))
      return
    }
    const confirmado = window.confirm('Se dará de baja el documento (baja lógica, no física).\n¿Desea continuar?')
    if (!confirmado) return
    setGuardandoId(doc.idDocumentoAnalizado)
    setError(null)
    try {
      await eliminarDocumentoEjecucion(expediente.idExpediente, doc.idDocumentoAnalizado as number)
      if (padreFocoId === doc.idDocumentoAnalizado) {
        setPadreFocoId(null)
      }
      recargarDocumentos()
      onActualizado?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo eliminar el documento.')
    } finally {
      setGuardandoId(null)
    }
  }

  async function handleDescargarWord(doc: DocumentoAnalizado) {
    if ((doc.idDocumentoAnalizado ?? 0) < 0) return
    setError(null)
    try {
      const nombre = `${doc.tipoDocumentoNombre || 'documento'}_${expediente.numeroExpediente}.docx`.replace(/\s+/g, '_')
      await descargarDocumentoWordEjecucion(expediente.idExpediente, doc.idDocumentoAnalizado as number, nombre)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo generar el documento Word.')
    }
  }

  async function handleRegistrarResultado() {
    setErrorResultado(null)
    setMensajeResultado(null)
    if (!resultadoCodigo) {
      setErrorResultado('Seleccione el resultado de ejecución.')
      return
    }
    const confirmado = window.confirm('Se registrará el resultado de ejecución.\n¿Desea continuar?')
    if (!confirmado) return
    setRegistrandoResultado(true)
    try {
      const resultadoNombre = resultados.find((r) => r.codigo === resultadoCodigo)?.nombre || ''
      const resp = await registrarResultadoEjecucion(expediente.idExpediente, resultadoCodigo, resultadoNombre, fechaEjecucion, comentarioResultado)
      setMensajeResultado(resp.mensaje)
      onActualizado?.()
    } catch (e) {
      setErrorResultado(e instanceof ApiError ? e.message : 'No se pudo registrar el resultado de ejecución.')
    } finally {
      setRegistrandoResultado(false)
    }
  }

  async function handleEnviarSupervision() {
    setErrorSupervision(null)
    setMensajeSupervision(null)
    if (!idUsuarioDestino) {
      setErrorSupervision('Seleccione el usuario destino en Supervisión.')
      return
    }
    const confirmado = window.confirm(`Se enviará el expediente ${expediente.numeroExpediente} a Supervisión.\n¿Desea continuar?`)
    if (!confirmado) return
    setEnviandoSupervision(true)
    try {
      const resp = await enviarSupervision(expediente.idExpediente, comentarioSupervision, Number(idUsuarioDestino))
      setMensajeSupervision(resp.mensaje)
      onActualizado?.()
    } catch (e) {
      setErrorSupervision(e instanceof ApiError ? e.message : 'No se pudo enviar el expediente a Supervisión.')
    } finally {
      setEnviandoSupervision(false)
    }
  }

  return (
    <Stack spacing={2}>
      {error && (
        <Alert severity="error" sx={{ whiteSpace: 'pre-line' }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <SectionCard title="Estado de la ejecución">
        <Stack spacing={1.5}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Etapa / Estado:
            </Typography>
            <StatusBadge label={`${expediente.etapaCodigo} / ${expediente.estadoCodigo}`} tone="info" />
            {expediente.corregible && <StatusBadge label="Error material" tone="error" />}
            {expediente.ejecutado && <StatusBadge label="Ejecutado" tone="success" />}
          </Stack>
          <Typography variant="body2" color="text.secondary">
            Abogado: {expediente.responsable || '-'} — Resultado de Análisis: {expediente.resultadoAnalisis || '-'}
          </Typography>
        </Stack>
      </SectionCard>

      {enEjecucion && (
        <SectionCard title="Documentos ejecutados">
          <Stack spacing={2}>
            <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end' }}>
              <Button size="small" startIcon={<AddOutlinedIcon />} onClick={handleAgregarDocumento}>
                Documento
              </Button>
            </Stack>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell />
                    <TableCell>Tipo documento</TableCell>
                    <TableCell>N° Documento</TableCell>
                    <TableCell>Estado documento</TableCell>
                    <TableCell>Fecha Emisión</TableCell>
                    <TableCell>Comentario</TableCell>
                    <TableCell align="center">¿Requiere respuesta?</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {documentosLoading ? (
                    <TableRow>
                      <TableCell colSpan={7}>
                        <Typography variant="body2" color="text.secondary">
                          Cargando...
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : padres.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7}>
                        <Typography variant="body2" color="text.secondary">
                          Sin documentos analizados.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    padres.map((doc) => {
                      const esFilaNueva = (doc.idDocumentoAnalizado ?? 0) < 0
                      const esCartaFinal = TIPOS_CARTA_NOTIFICACION.has(doc.tipoDocumentoCodigo)
                      // Fila nueva (todavía sin guardar): siempre editable, para poder elegir la carta final
                      // al crearla. Fila ya guardada: editable SOLO si ya es una carta final (mismo criterio
                      // que antes) — un documento de otra área (Análisis/Verificación/etc.) nunca cambia de tipo acá.
                      const tipoEditable = esFilaNueva || esCartaFinal
                      // Solo puede eliminarse una fila nueva sin guardar (descarte, sin llamada al servidor) o
                      // una carta final ya guardada — documentos que vienen de Análisis/Verificación/otras áreas
                      // no se pueden eliminar desde Ejecución (21/08/2026, pedido explícito del usuario).
                      const puedeEliminar = esFilaNueva || esCartaFinal
                      return (
                        <TableRow
                          key={doc.idDocumentoAnalizado}
                          hover
                          selected={padreFocoId === doc.idDocumentoAnalizado}
                          onClick={() => setPadreFocoId(doc.idDocumentoAnalizado)}
                          sx={{ cursor: 'pointer' }}
                        >
                          <TableCell onClick={(e) => e.stopPropagation()}>
                            <Stack direction="row">
                              <Tooltip title="Guardar">
                                <span>
                                  <IconButton size="small" disabled={guardandoId === doc.idDocumentoAnalizado} onClick={() => handleGuardarFila(doc)}>
                                    <SaveOutlinedIcon fontSize="small" />
                                  </IconButton>
                                </span>
                              </Tooltip>
                              <Tooltip title={(doc.idDocumentoAnalizado ?? 0) < 0 ? 'Guarde el documento antes de descargar' : 'Descargar Word'}>
                                <span>
                                  <IconButton size="small" disabled={(doc.idDocumentoAnalizado ?? 0) < 0} onClick={() => handleDescargarWord(doc)}>
                                    <DescriptionOutlinedIcon fontSize="small" />
                                  </IconButton>
                                </span>
                              </Tooltip>
                              <Tooltip title={puedeEliminar ? 'Eliminar' : 'Solo pueden eliminarse cartas finales (Procedente/Improcedente/Procedente en parte) agregadas desde Ejecución'}>
                                <span>
                                  <IconButton size="small" disabled={!puedeEliminar} onClick={() => handleEliminarFila(doc)}>
                                    <DeleteOutlineOutlinedIcon fontSize="small" />
                                  </IconButton>
                                </span>
                              </Tooltip>
                            </Stack>
                          </TableCell>
                          <TableCell onClick={(e) => e.stopPropagation()} sx={{ minWidth: 160 }}>
                            {tipoEditable ? (
                              <TextField
                                select
                                size="small"
                                fullWidth
                                value={doc.tipoDocumentoCodigo}
                                onChange={(e) => {
                                  const t = tiposDocumento.find((x) => x.codigo === e.target.value)
                                  actualizarDocumento(doc.idDocumentoAnalizado as number, {
                                    tipoDocumentoCodigo: e.target.value,
                                    tipoDocumentoNombre: t?.nombre || '',
                                  })
                                }}
                              >
                                <MenuItem value="">Seleccione</MenuItem>
                                {tiposDocumento
                                  .filter((t) => TIPOS_CARTA_NOTIFICACION.has(t.codigo ?? ''))
                                  .map((t) => (
                                    <MenuItem key={t.codigo} value={t.codigo ?? ''}>
                                      {t.nombre}
                                    </MenuItem>
                                  ))}
                              </TextField>
                            ) : (
                              <Tooltip title="Solo las cartas de notificación (Improcedente/Procedente/Procedente en parte) permiten cambiar el tipo aquí.">
                                <span>{doc.tipoDocumentoNombre || '-'}</span>
                              </Tooltip>
                            )}
                          </TableCell>
                          <TableCell onClick={(e) => e.stopPropagation()}>
                            <TextField
                              size="small"
                              value={doc.numeroDocumento}
                              onChange={(e) => actualizarDocumento(doc.idDocumentoAnalizado as number, { numeroDocumento: e.target.value })}
                            />
                          </TableCell>
                          <TableCell onClick={(e) => e.stopPropagation()} sx={{ minWidth: 150 }}>
                            <TextField
                              select
                              size="small"
                              fullWidth
                              value={doc.estadoDocumentoCodigo}
                              onChange={(e) => {
                                const est = estadosDocumento.find((x) => x.codigo === e.target.value)
                                const emitido = e.target.value === 'EMITIDO'
                                actualizarDocumento(doc.idDocumentoAnalizado as number, {
                                  estadoDocumentoCodigo: e.target.value,
                                  estadoDocumentoNombre: est?.nombre || '',
                                  fechaDocumento: emitido ? new Date().toISOString().slice(0, 10) : doc.fechaDocumento,
                                })
                              }}
                            >
                              <MenuItem value="">Seleccione</MenuItem>
                              {estadosDocumento.map((e2) => (
                                <MenuItem key={e2.codigo} value={e2.codigo ?? ''}>
                                  {e2.nombre}
                                </MenuItem>
                              ))}
                            </TextField>
                          </TableCell>
                          <TableCell onClick={(e) => e.stopPropagation()}>
                            <TextField
                              size="small"
                              type="date"
                              value={doc.fechaDocumento ?? ''}
                              onChange={(e) => actualizarDocumento(doc.idDocumentoAnalizado as number, { fechaDocumento: e.target.value || null })}
                            />
                          </TableCell>
                          <TableCell onClick={(e) => e.stopPropagation()}>
                            <TextField
                              size="small"
                              value={doc.descripcion}
                              onChange={(e) => actualizarDocumento(doc.idDocumentoAnalizado as number, { descripcion: e.target.value })}
                            />
                          </TableCell>
                          <TableCell align="center" onClick={(e) => e.stopPropagation()}>
                            <Checkbox
                              checked={doc.requiereRespuesta}
                              onChange={(e) => actualizarDocumento(doc.idDocumentoAnalizado as number, { requiereRespuesta: e.target.checked })}
                            />
                          </TableCell>
                        </TableRow>
                      )
                    })
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Stack>
        </SectionCard>
      )}

      {enEjecucion && (
        <SectionCard title="Documentos de respuesta">
          <Box>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1 }}>
              <Typography variant="subtitle2">
                {padreFoco ? `Documento seleccionado: ${padreFoco.tipoDocumentoNombre || '-'}` : 'Ningún documento seleccionado'}
              </Typography>
            </Stack>
            {!padreFoco ? (
              <Typography variant="body2" color="text.secondary">
                Seleccione un documento principal en "Documentos ejecutados" para ver/agregar sus respuestas.
              </Typography>
            ) : (
              <Stack spacing={1.5}>
                <Stack direction="row">
                  <Button
                    size="small"
                    startIcon={<AddOutlinedIcon />}
                    disabled={(padreFoco.idDocumentoAnalizado ?? 0) < 0}
                    onClick={handleAgregarRelacionado}
                  >
                    Relacionado
                  </Button>
                </Stack>
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell />
                        <TableCell>Tipo documento</TableCell>
                        <TableCell>Comentario</TableCell>
                        <TableCell>Confirmación de respuesta</TableCell>
                        <TableCell>Fecha Respuesta</TableCell>
                        <TableCell>Fecha Publicación</TableCell>
                        <TableCell>Hoja de Envío</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {hijos.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={7}>
                            <Typography variant="body2" color="text.secondary">
                              Sin documentos de respuesta.
                            </Typography>
                          </TableCell>
                        </TableRow>
                      ) : (
                        hijos.map((doc) => (
                          <TableRow key={doc.idDocumentoAnalizado}>
                            <TableCell>
                              <Stack direction="row">
                                <Tooltip title="Guardar">
                                  <span>
                                    <IconButton size="small" disabled={guardandoId === doc.idDocumentoAnalizado} onClick={() => handleGuardarFila(doc)}>
                                      <SaveOutlinedIcon fontSize="small" />
                                    </IconButton>
                                  </span>
                                </Tooltip>
                                <Tooltip title="Eliminar">
                                  <span>
                                    <IconButton size="small" onClick={() => handleEliminarFila(doc)}>
                                      <DeleteOutlineOutlinedIcon fontSize="small" />
                                    </IconButton>
                                  </span>
                                </Tooltip>
                              </Stack>
                            </TableCell>
                            <TableCell sx={{ minWidth: 160 }}>
                              <TextField
                                select
                                size="small"
                                fullWidth
                                value={doc.tipoDocumentoCodigo}
                                onChange={(e) => {
                                  const t = tiposDocumento.find((x) => x.codigo === e.target.value)
                                  actualizarDocumento(doc.idDocumentoAnalizado as number, {
                                    tipoDocumentoCodigo: e.target.value,
                                    tipoDocumentoNombre: t?.nombre || '',
                                  })
                                }}
                              >
                                <MenuItem value="">Seleccione</MenuItem>
                                {tiposDocumento.map((t) => (
                                  <MenuItem key={t.codigo} value={t.codigo ?? ''}>
                                    {t.nombre}
                                  </MenuItem>
                                ))}
                              </TextField>
                            </TableCell>
                            <TableCell>
                              <TextField
                                size="small"
                                value={doc.descripcion}
                                onChange={(e) => actualizarDocumento(doc.idDocumentoAnalizado as number, { descripcion: e.target.value })}
                              />
                            </TableCell>
                            <TableCell sx={{ minWidth: 140 }}>
                              <TextField
                                select
                                size="small"
                                fullWidth
                                value={doc.confirmacionRespuesta || 'PENDIENTE'}
                                onChange={(e) => actualizarDocumento(doc.idDocumentoAnalizado as number, { confirmacionRespuesta: e.target.value })}
                              >
                                <MenuItem value="PENDIENTE">Pendiente</MenuItem>
                                <MenuItem value="SI">Sí</MenuItem>
                                <MenuItem value="NO">No</MenuItem>
                              </TextField>
                            </TableCell>
                            <TableCell>
                              <TextField
                                size="small"
                                type="date"
                                value={doc.fechaRespuesta ?? ''}
                                onChange={(e) => actualizarDocumento(doc.idDocumentoAnalizado as number, { fechaRespuesta: e.target.value || null })}
                              />
                            </TableCell>
                            <TableCell>{doc.fechaPublicacion || '-'}</TableCell>
                            <TableCell>
                              <TextField
                                size="small"
                                value={doc.numeroHojaEnvioRespuesta}
                                onChange={(e) =>
                                  actualizarDocumento(doc.idDocumentoAnalizado as number, { numeroHojaEnvioRespuesta: e.target.value })
                                }
                              />
                            </TableCell>
                          </TableRow>
                        ))
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Stack>
            )}
          </Box>
        </SectionCard>
      )}

      {enEjecucion && (
        <SectionCard title="Resultado de ejecución">
          <Stack spacing={2}>
            <TextField select label="Resultado" size="small" fullWidth value={resultadoCodigo} onChange={(e) => setResultadoCodigo(e.target.value)}>
              <MenuItem value="">Seleccione resultado</MenuItem>
              {resultados.map((r) => (
                <MenuItem key={r.codigo} value={r.codigo ?? ''}>
                  {r.nombre}
                </MenuItem>
              ))}
            </TextField>
            {resultadoCodigo === 'EJECUTADO' && (
              <TextField
                label="Fecha Ejecución"
                type="date"
                size="small"
                fullWidth
                value={fechaEjecucion ?? ''}
                onChange={(e) => setFechaEjecucion(e.target.value || null)}
                slotProps={{ inputLabel: { shrink: true } }}
              />
            )}
            <TextField
              label="Comentario (opcional)"
              size="small"
              fullWidth
              multiline
              minRows={2}
              value={comentarioResultado}
              onChange={(e) => setComentarioResultado(e.target.value)}
            />
            <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
              <Button variant="contained" onClick={handleRegistrarResultado} disabled={registrandoResultado} sx={{ alignSelf: 'flex-start' }}>
                Registrar resultado de ejecución
              </Button>
              {errorResultado && (
                <Alert severity="error" sx={{ py: 0, whiteSpace: 'pre-line' }} onClose={() => setErrorResultado(null)}>
                  {errorResultado}
                </Alert>
              )}
              {mensajeResultado && (
                <Alert severity="success" sx={{ py: 0 }} onClose={() => setMensajeResultado(null)}>
                  {mensajeResultado}
                </Alert>
              )}
            </Stack>
          </Stack>
        </SectionCard>
      )}

      {enEjecucion && (
        <SectionCard title="Enviar a Supervisión">
          <Stack spacing={2}>
            <Typography variant="body2" color="text.secondary">
              Deriva el expediente ejecutado a Notificación, pendiente de asignar. Requiere una carta de notificación final ya despachada.
            </Typography>
            <TextField
              label="Comentario (opcional)"
              size="small"
              fullWidth
              multiline
              minRows={2}
              value={comentarioSupervision}
              onChange={(e) => setComentarioSupervision(e.target.value)}
            />
            <TextField
              select
              label="Usuario destino"
              size="small"
              fullWidth
              value={idUsuarioDestino}
              onChange={(e) => setIdUsuarioDestino(e.target.value)}
            >
              <MenuItem value="">Seleccione usuario</MenuItem>
              {usuarios.map((u) => (
                <MenuItem key={u.idUsuario} value={String(u.idUsuario)}>
                  {u.nombreCompleto}
                </MenuItem>
              ))}
            </TextField>
            <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
              <Button variant="contained" onClick={handleEnviarSupervision} disabled={enviandoSupervision} sx={{ alignSelf: 'flex-start' }}>
                Enviar a Supervisión
              </Button>
              {errorSupervision && (
                <Alert severity="error" sx={{ py: 0, whiteSpace: 'pre-line' }} onClose={() => setErrorSupervision(null)}>
                  {errorSupervision}
                </Alert>
              )}
              {mensajeSupervision && (
                <Alert severity="success" sx={{ py: 0 }} onClose={() => setMensajeSupervision(null)}>
                  {mensajeSupervision}
                </Alert>
              )}
            </Stack>
          </Stack>
        </SectionCard>
      )}

      {!enEjecucion && <Alert severity="info">Este expediente no está en un estado operable desde Ejecución.</Alert>}
    </Stack>
  )
}
