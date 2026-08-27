import { useEffect, useMemo, useRef, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  FormControlLabel,
  IconButton,
  MenuItem,
  Snackbar,
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
  descargarDocumentoWord,
  eliminarDocumentoAnalizado,
  enviarVerificacion,
  guardarDocumentoAnalizado,
  listarDocumentosAnalizados,
  listarEstadosDocumentoAnalisis,
  listarMotivosNoCorresponde,
  listarResultadosAnalisis,
  listarTiposDocumentoAdjuntoAnalisis,
  listarTiposObservacion,
  obtenerAnalisisRegistrado,
  recibirDocumentoAsociado,
  recibirExpediente,
  registrarAnalisis,
  validarRegistroAnalisis,
  type AnalisisExpediente,
  type AnalisisRegistro,
  type DocumentoAnalizado,
} from '../api/analisisApi'
import { listarConfirmados, type ExpedienteRelacionado } from '../api/expedienteRelacionadoApi'
import type { CatalogoItem } from '../api/registroApi'
import { ApiError } from '../api/http'
import { SectionCard } from './RegistroExpedienteForm'
import { StatusBadge } from './StatusBadge'

interface AnalisisContentProps {
  expediente: AnalisisExpediente
  onActualizado?: () => void
}

const TIPO_DOC_NO_CORRESPONDE_OPCIONES = [
  { codigo: 'PROVEIDO', nombre: 'Proveído' },
  { codigo: 'HOJA_ENVIO', nombre: 'Hoja de Envío' },
]

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

/**
 * Contenido de la subpestaña "Análisis" del workspace multipestañas de la Bandeja Análisis. "Datos"
 * (la otra subpestaña) no se tocó — sigue siendo idéntica a la de Registro/Recepción.
 *
 * 5 bloques, cada uno visible solo cuando aplica al estado real del expediente (nunca todos a la
 * vez, para no abrumar): Recibir expediente (ASIGNACION/ASIGNADO), Recibir solicitudes asociadas
 * (si tiene duplicados confirmados), Documentos analizados (20/08/2026: ahora ANTES que "Resultado
 * del análisis", pedido explícito del usuario — el abogado revisa/carga documentos antes de fijar
 * el resultado final), Resultado del análisis, Enviar a Verificación (solo ATENDIDO/SUBSANADO, más
 * estricto que el hint de UI {@link AnalisisExpediente.enviableVerificacion}).
 *
 * **Transición en caliente tras "Recibir expediente" (20/08/2026, pedido explícito del usuario)**:
 * antes, tras recibir con éxito, el panel se quedaba mostrando la sección "Recibir expediente" (el
 * usuario tenía que cerrar y reabrir la pestaña para ver "Documentos analizados"/"Resultado del
 * análisis"). Causa raíz real: cada workspace de `AnalisisBandejaPage.tsx` guarda su propia copia
 * congelada del expediente (`WorkspaceAbierto.row`), asignada una sola vez al abrir — `onActualizado`
 * solo recargaba la lista de la bandeja, nunca ese `row` ya abierto. Se corrigió en
 * `AnalisisBandejaPage.tsx` con un `useEffect` que sincroniza `row` con la lista recién recargada
 * cada vez que cambia (ver Javadoc de esa página). Con el dato ya fresco, este componente además
 * "congela" a propósito la vista anterior 5 segundos (`transicionPendiente`) tras un "Recibir
 * expediente" exitoso, mostrando un `Snackbar` de confirmación con auto-cierre — el cambio de
 * sección (de "Recibir expediente" a "Documentos analizados") recién ocurre cuando el snackbar se
 * cierra (automático o manual), para que el usuario alcance a leer la confirmación antes de que el
 * layout cambie debajo de él, sin necesidad de cerrar/reabrir la pestaña. Este congelamiento es
 * exclusivo de "Recibir expediente" (pedido explícito, no extendido a "Registrar resultado
 * final"/"Enviar a Verificación": esas 2 acciones no cambian qué secciones se muestran —
 * `enAnalisis` sigue siendo `true` antes y después de ambas — así que no hay ningún salto de layout
 * que suavizar ahí).
 */
export function AnalisisContent({ expediente, onActualizado }: AnalisisContentProps) {
  const [comentario, setComentario] = useState('')
  const [procesando, setProcesando] = useState(false)
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  // Mensajes de "Registrar análisis" y "Enviar a Verificación" (21/08/2026, pedido explícito del
  // usuario): antes compartían el `mensaje`/`error` de arriba, mostrado en un Alert al tope del
  // panel; ahora cada uno tiene su propio estado, renderizado a la derecha de su propio botón.
  const [mensajeResultado, setMensajeResultado] = useState<string | null>(null)
  const [errorResultado, setErrorResultado] = useState<string | null>(null)
  const [mensajeVerificacion, setMensajeVerificacion] = useState<string | null>(null)
  const [errorVerificacion, setErrorVerificacion] = useState<string | null>(null)

  // Solicitudes asociadas
  const [asociados, setAsociados] = useState<ExpedienteRelacionado[]>([])
  const [recibiendoAsociadoId, setRecibiendoAsociadoId] = useState<number | null>(null)

  // Catálogos
  const [resultados, setResultados] = useState<CatalogoItem[]>([])
  const [motivos, setMotivos] = useState<CatalogoItem[]>([])
  const [tiposObservacion, setTiposObservacion] = useState<CatalogoItem[]>([])
  const [tiposDocumento, setTiposDocumento] = useState<CatalogoItem[]>([])
  const [estadosDocumento, setEstadosDocumento] = useState<CatalogoItem[]>([])

  // Resultado del análisis
  const [resultadoCodigo, setResultadoCodigo] = useState('')
  const [incorporado, setIncorporado] = useState<'' | 'SI' | 'NO'>('')
  const [reconstitucion, setReconstitucion] = useState(false)
  const [legitimidad, setLegitimidad] = useState(false)
  const [mediosProbatorios, setMediosProbatorios] = useState(false)
  const [fundamento, setFundamento] = useState('')
  const [motivoNoCorresponde, setMotivoNoCorresponde] = useState('')
  const [tipoDocNoCorresponde, setTipoDocNoCorresponde] = useState('')
  const [numeroDocProveido, setNumeroDocProveido] = useState('')
  const [obsTipo, setObsTipo] = useState('')
  const [obsDescripcion, setObsDescripcion] = useState('')
  const [erroresValidacion, setErroresValidacion] = useState<string[]>([])
  const [registrando, setRegistrando] = useState(false)

  // Documentos analizados (maestro-detalle)
  const [documentos, setDocumentos] = useState<DocumentoAnalizado[]>([])
  const [documentosLoading, setDocumentosLoading] = useState(false)
  const [padreFocoId, setPadreFocoId] = useState<number | null>(null)
  const [guardandoId, setGuardandoId] = useState<number | null>(null)
  const tempIdRef = useRef(-1)

  const [enviandoVerificacion, setEnviandoVerificacion] = useState(false)

  // Transición en caliente tras "Recibir expediente" (ver Javadoc del componente)
  const [transicionPendiente, setTransicionPendiente] = useState(false)
  const [snackbarRecepcion, setSnackbarRecepcion] = useState<string | null>(null)

  const enAnalisisReal = expediente.etapaCodigo === 'ANALISIS'
  const mostrarRecibir = transicionPendiente ? true : expediente.recibible
  const enAnalisis = transicionPendiente ? false : enAnalisisReal
  const esNoCorresponde = resultadoCodigo === 'NO_CORRESPONDE'
  const requiereObservacion = resultadoCodigo === 'OBSERVADO' || resultadoCodigo === 'OBSERVACION_ADMINISTRATIVA'
  const enviableAhora = expediente.estadoCodigo === 'ATENDIDO' || expediente.estadoCodigo === 'SUBSANADO'

  function cerrarSnackbarRecepcion() {
    setSnackbarRecepcion(null)
    setTransicionPendiente(false)
  }

  useEffect(() => {
    Promise.all([
      listarResultadosAnalisis(),
      listarMotivosNoCorresponde(),
      listarTiposObservacion(),
      listarTiposDocumentoAdjuntoAnalisis(),
      listarEstadosDocumentoAnalisis(),
    ])
      .then(([r, m, o, t, e]) => {
        setResultados(r)
        setMotivos(m)
        setTiposObservacion(o)
        setTiposDocumento(t)
        setEstadosDocumento(e)
      })
      .catch(() => undefined)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    recargarResultado()
    recargarDocumentos()
    recargarAsociados()
    setTransicionPendiente(false)
    setSnackbarRecepcion(null)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [expediente.idExpediente])

  function recargarResultado() {
    obtenerAnalisisRegistrado(expediente.idExpediente)
      .then((d) => {
        setResultadoCodigo(d.resultadoCodigo || '')
        setIncorporado(d.incorporado === true ? 'SI' : d.incorporado === false ? 'NO' : '')
        setReconstitucion(d.requiereReconstitucion)
        setLegitimidad(d.tieneLegitimidad)
        setMediosProbatorios(d.cumpleMediosProbatorios)
        setFundamento(d.fundamento || '')
        setMotivoNoCorresponde(d.motivoNoCorrespondeCodigo || '')
        setTipoDocNoCorresponde(d.tipoDocumentoNoCorrespondeCodigo || '')
        setNumeroDocProveido(d.numeroDocumentoProveido || '')
        setObsTipo(d.observacion?.tipoObservacionCodigo || '')
        setObsDescripcion(d.observacion?.descripcion || '')
      })
      .catch(() => undefined)
  }

  function recargarDocumentos() {
    setDocumentosLoading(true)
    listarDocumentosAnalizados(expediente.idExpediente)
      .then(setDocumentos)
      .catch(() => setDocumentos([]))
      .finally(() => setDocumentosLoading(false))
  }

  function recargarAsociados() {
    if (expediente.asociadosConfirmados <= 0) {
      setAsociados([])
      return
    }
    listarConfirmados(expediente.idExpediente)
      .then(setAsociados)
      .catch(() => setAsociados([]))
  }

  async function handleRecibir() {
    const confirmado = window.confirm(
      `Se recibirá el expediente ${expediente.numeroExpediente} para análisis.\n¿Desea continuar?`,
    )
    if (!confirmado) return
    setProcesando(true)
    setError(null)
    setMensaje(null)
    try {
      const resultado = await recibirExpediente(expediente.idExpediente, comentario)
      setComentario('')
      onActualizado?.()
      setTransicionPendiente(true)
      setSnackbarRecepcion(resultado.mensaje)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo recibir el expediente.')
    } finally {
      setProcesando(false)
    }
  }

  async function handleRecibirAsociado(asociado: ExpedienteRelacionado) {
    const confirmado = window.confirm(
      `Se recibirá la solicitud asociada ${asociado.numeroExpediente || asociado.numeroExpedienteSgd} para análisis.\n¿Desea continuar?`,
    )
    if (!confirmado) return
    setRecibiendoAsociadoId(asociado.idExpediente)
    setError(null)
    setMensaje(null)
    try {
      const resultado = await recibirDocumentoAsociado(expediente.idExpediente, asociado.idExpediente, '')
      setMensaje(resultado.mensaje)
      recargarAsociados()
      onActualizado?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo recibir la solicitud asociada.')
    } finally {
      setRecibiendoAsociadoId(null)
    }
  }

  // --- Documentos analizados ---

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
      const idReal = await guardarDocumentoAnalizado(payload)
      const idAnterior = doc.idDocumentoAnalizado
      const lista = await listarDocumentosAnalizados(expediente.idExpediente)
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
      return;
    }
    const confirmado = window.confirm('Se dará de baja el documento (baja lógica, no física).\n¿Desea continuar?')
    if (!confirmado) return
    setGuardandoId(doc.idDocumentoAnalizado)
    setError(null)
    try {
      await eliminarDocumentoAnalizado(expediente.idExpediente, doc.idDocumentoAnalizado as number)
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
      await descargarDocumentoWord(expediente.idExpediente, doc.idDocumentoAnalizado as number, nombre)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo generar el documento Word.')
    }
  }

  // --- Resultado del análisis ---

  function armarRegistro(): AnalisisRegistro {
    return {
      idExpediente: expediente.idExpediente,
      idExpedienteAnalisis: null,
      resultadoCodigo,
      resultadoNombre: resultados.find((r) => r.codigo === resultadoCodigo)?.nombre || '',
      corresponde: resultadoCodigo ? !esNoCorresponde : null,
      incorporado: esNoCorresponde ? null : incorporado === 'SI' ? true : incorporado === 'NO' ? false : null,
      requiereReconstitucion: !esNoCorresponde && reconstitucion,
      tieneLegitimidad: !esNoCorresponde && legitimidad,
      cumpleMediosProbatorios: !esNoCorresponde && mediosProbatorios,
      fundamento,
      motivoNoCorrespondeCodigo: esNoCorresponde ? motivoNoCorresponde : '',
      tipoDocumentoNoCorrespondeCodigo: esNoCorresponde ? tipoDocNoCorresponde : '',
      tipoDocumentoNoCorrespondeNombre: esNoCorresponde
        ? TIPO_DOC_NO_CORRESPONDE_OPCIONES.find((o) => o.codigo === tipoDocNoCorresponde)?.nombre || ''
        : '',
      numeroDocumentoProveido: esNoCorresponde ? numeroDocProveido : '',
      observacion: requiereObservacion
        ? { tipoObservacionCodigo: obsTipo, tipoObservacionNombre: tiposObservacion.find((t) => t.codigo === obsTipo)?.nombre || '', descripcion: obsDescripcion }
        : null,
      documentosAnalizados: documentos,
    }
  }

  async function handleRegistrarResultado() {
    setErrorResultado(null)
    setMensajeResultado(null)
    const registro = armarRegistro()
    try {
      const errores = await validarRegistroAnalisis(registro)
      if (errores.length > 0) {
        setErroresValidacion(errores)
        return
      }
      setErroresValidacion([])
    } catch (e) {
      setErrorResultado(e instanceof ApiError ? e.message : 'No se pudo validar el resultado del análisis.')
      return
    }
    const confirmado = window.confirm('Se registrará el resultado del análisis.\n¿Desea continuar?')
    if (!confirmado) return
    setRegistrando(true)
    try {
      const resultado = await registrarAnalisis(registro)
      setMensajeResultado(resultado.mensaje)
      recargarResultado()
      onActualizado?.()
    } catch (e) {
      setErrorResultado(e instanceof ApiError ? e.message : 'No se pudo registrar el resultado del análisis.')
    } finally {
      setRegistrando(false)
    }
  }

  async function handleEnviarVerificacion() {
    const confirmado = window.confirm(
      `Se enviará el expediente ${expediente.numeroExpediente} a Verificación.\n¿Desea continuar?`,
    )
    if (!confirmado) return
    setEnviandoVerificacion(true)
    setErrorVerificacion(null)
    setMensajeVerificacion(null)
    try {
      const resultado = await enviarVerificacion(expediente.idExpediente, '')
      setMensajeVerificacion(resultado.mensaje)
      onActualizado?.()
    } catch (e) {
      setErrorVerificacion(e instanceof ApiError ? e.message : 'No se pudo enviar el expediente a verificación.')
    } finally {
      setEnviandoVerificacion(false)
    }
  }

  return (
    <Stack spacing={2}>
      {error && (
        <Alert severity="error" sx={{ whiteSpace: 'pre-line' }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}
      {mensaje && (
        <Alert severity="success" onClose={() => setMensaje(null)}>
          {mensaje}
        </Alert>
      )}

      <SectionCard title="Estado del análisis">
        <Stack spacing={1.5}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Etapa / Estado:
            </Typography>
            <StatusBadge label={`${expediente.etapaCodigo} / ${expediente.estadoCodigo}`} tone="info" />
            {expediente.derivado && <StatusBadge label="Derivado" tone="warning" />}
            {expediente.observado && <StatusBadge label="Observado" tone="warning" />}
          </Stack>
          <Typography variant="body2" color="text.secondary">
            Abogado designado: {expediente.responsable || '-'}
          </Typography>
        </Stack>
      </SectionCard>

      {mostrarRecibir && (
        <SectionCard title="Recibir expediente">
          <Stack spacing={2}>
            <Typography variant="body2" color="text.secondary">
              El expediente está en Asignación / Asignado, pendiente de recepción para análisis.
            </Typography>
            <TextField
              label="Comentario (opcional)"
              size="small"
              fullWidth
              multiline
              minRows={2}
              value={comentario}
              onChange={(e) => setComentario(e.target.value)}
              disabled={transicionPendiente}
            />
            <Button
              variant="contained"
              onClick={handleRecibir}
              disabled={procesando || transicionPendiente}
              sx={{ alignSelf: 'flex-start' }}
            >
              Recibir expediente
            </Button>
          </Stack>
        </SectionCard>
      )}

      <Snackbar
        open={snackbarRecepcion !== null}
        autoHideDuration={5000}
        onClose={cerrarSnackbarRecepcion}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity="success" onClose={cerrarSnackbarRecepcion} sx={{ width: '100%' }}>
          {snackbarRecepcion}
        </Alert>
      </Snackbar>

      {asociados.length > 0 && (
        <SectionCard title={`Solicitudes asociadas (${asociados.length})`}>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>N° Expediente</TableCell>
                  <TableCell>N° SGD</TableCell>
                  <TableCell>Titular</TableCell>
                  <TableCell>Etapa / Estado</TableCell>
                  <TableCell align="right">Acción</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {asociados.map((a) => {
                  const puedeRecibir = a.etapaCodigo === 'ASIGNACION' && a.estadoCodigo === 'ASIGNADO'
                  return (
                    <TableRow key={a.idExpediente}>
                      <TableCell>{a.numeroExpediente || '-'}</TableCell>
                      <TableCell>{a.numeroExpedienteSgd || '-'}</TableCell>
                      <TableCell>{a.titular || '-'}</TableCell>
                      <TableCell>{a.etapaEstado || '-'}</TableCell>
                      <TableCell align="right">
                        <Tooltip title={puedeRecibir ? 'Recibir para análisis' : 'Solo puede recibirse mientras está Asignación / Asignado'}>
                          <span>
                            <Button
                              size="small"
                              variant="outlined"
                              disabled={!puedeRecibir || recibiendoAsociadoId === a.idExpediente}
                              onClick={() => handleRecibirAsociado(a)}
                            >
                              Recibir
                            </Button>
                          </span>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
      )}

      {enAnalisis && (
        <SectionCard title="Documentos analizados">
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
                    <TableCell>Tipo</TableCell>
                    <TableCell>N° Documento</TableCell>
                    <TableCell>Estado</TableCell>
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
                    padres.map((doc) => (
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
                            <Tooltip title="Eliminar">
                              <span>
                                <IconButton size="small" onClick={() => handleEliminarFila(doc)}>
                                  <DeleteOutlineOutlinedIcon fontSize="small" />
                                </IconButton>
                              </span>
                            </Tooltip>
                          </Stack>
                        </TableCell>
                        <TableCell onClick={(e) => e.stopPropagation()} sx={{ minWidth: 160 }}>
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
                                // 24/08/2026, pedido explícito del usuario: al elegir un tipo de
                                // documento cuya clasificación sea INTERMEDIO, "Requiere respuesta"
                                // se marca por defecto (sigue siendo editable a mano después).
                                requiereRespuesta: t?.clasificacion === 'INTERMEDIO' ? true : doc.requiereRespuesta,
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
                                fechaDocumento: emitido ? new Date().toISOString().slice(0, 10) : null,
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
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>

            <Box>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1 }}>
                <Typography variant="subtitle2">
                  Documento(s) de respuesta {padreFoco ? `— ${padreFoco.tipoDocumentoNombre || 'documento seleccionado'}` : ''}
                </Typography>
                {padreFoco && <Chip size="small" label={`${hijos.length}`} />}
              </Stack>
              {!padreFoco ? (
                <Typography variant="body2" color="text.secondary">
                  Seleccione un documento principal arriba para ver/agregar sus respuestas.
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
                          <TableCell>Tipo</TableCell>
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
          </Stack>
        </SectionCard>
      )}

      {enAnalisis && (
        <SectionCard title="Resultado del análisis">
          <Stack spacing={2}>
            {erroresValidacion.length > 0 && (
              <Alert severity="warning">
                <Stack spacing={0.25}>
                  {erroresValidacion.map((m, i) => (
                    <Typography key={i} variant="body2">
                      • {m}
                    </Typography>
                  ))}
                </Stack>
              </Alert>
            )}
            <TextField select label="Resultado" size="small" fullWidth value={resultadoCodigo} onChange={(e) => setResultadoCodigo(e.target.value)}>
              <MenuItem value="">Seleccione resultado</MenuItem>
              {resultados.map((r) => (
                <MenuItem key={r.codigo} value={r.codigo ?? ''}>
                  {r.nombre}
                </MenuItem>
              ))}
            </TextField>

            {!esNoCorresponde && (
              <>
                <TextField
                  select
                  label="¿Acta incorporada?"
                  size="small"
                  fullWidth
                  value={incorporado}
                  onChange={(e) => setIncorporado(e.target.value as '' | 'SI' | 'NO')}
                >
                  <MenuItem value="">Seleccione</MenuItem>
                  <MenuItem value="SI">Sí</MenuItem>
                  <MenuItem value="NO">No</MenuItem>
                </TextField>
                <Stack direction="row" spacing={2} sx={{ flexWrap: 'wrap' }}>
                  <FormControlLabel
                    control={<Checkbox checked={reconstitucion} onChange={(e) => setReconstitucion(e.target.checked)} />}
                    label="Requiere reconstitución"
                  />
                  <FormControlLabel
                    control={<Checkbox checked={legitimidad} onChange={(e) => setLegitimidad(e.target.checked)} />}
                    label="Tiene legitimidad"
                  />
                  <FormControlLabel
                    control={<Checkbox checked={mediosProbatorios} onChange={(e) => setMediosProbatorios(e.target.checked)} />}
                    label="Cumple medios probatorios"
                  />
                </Stack>
              </>
            )}

            {esNoCorresponde && (
              <>
                <TextField
                  select
                  label="Motivo no corresponde"
                  size="small"
                  fullWidth
                  value={motivoNoCorresponde}
                  onChange={(e) => setMotivoNoCorresponde(e.target.value)}
                >
                  <MenuItem value="">Seleccione motivo</MenuItem>
                  {motivos.map((m) => (
                    <MenuItem key={m.codigo} value={m.codigo ?? ''}>
                      {m.nombre}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  select
                  label="Tipo Documento"
                  size="small"
                  fullWidth
                  value={tipoDocNoCorresponde}
                  onChange={(e) => setTipoDocNoCorresponde(e.target.value)}
                >
                  <MenuItem value="">Seleccione tipo</MenuItem>
                  {TIPO_DOC_NO_CORRESPONDE_OPCIONES.map((o) => (
                    <MenuItem key={o.codigo} value={o.codigo}>
                      {o.nombre}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  label="N° Documento proveído"
                  size="small"
                  fullWidth
                  value={numeroDocProveido}
                  onChange={(e) => setNumeroDocProveido(e.target.value)}
                />
              </>
            )}

            <TextField
              label="Fundamento"
              size="small"
              fullWidth
              multiline
              minRows={2}
              value={fundamento}
              onChange={(e) => setFundamento(e.target.value)}
            />

            {requiereObservacion && (
              <Stack spacing={2}>
                <TextField select label="Tipo de observación" size="small" fullWidth value={obsTipo} onChange={(e) => setObsTipo(e.target.value)}>
                  <MenuItem value="">Seleccione tipo</MenuItem>
                  {tiposObservacion.map((t) => (
                    <MenuItem key={t.codigo} value={t.codigo ?? ''}>
                      {t.nombre}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  label="Observación"
                  size="small"
                  fullWidth
                  multiline
                  minRows={2}
                  value={obsDescripcion}
                  onChange={(e) => setObsDescripcion(e.target.value)}
                />
              </Stack>
            )}

            <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
              <Button variant="contained" onClick={handleRegistrarResultado} disabled={registrando} sx={{ alignSelf: 'flex-start' }}>
                Registrar análisis
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

      {enAnalisis && (
        <SectionCard title="Enviar a Verificación">
          <Stack spacing={1.5}>
            <Typography variant="body2" color="text.secondary">
              Habilitado solo si el expediente está Atendido o Subsanado.
            </Typography>
            <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
              <Button
                variant="contained"
                onClick={handleEnviarVerificacion}
                disabled={!enviableAhora || enviandoVerificacion}
                sx={{ alignSelf: 'flex-start' }}
              >
                Enviar a Verificación
              </Button>
              {errorVerificacion && (
                <Alert severity="error" sx={{ py: 0, whiteSpace: 'pre-line' }} onClose={() => setErrorVerificacion(null)}>
                  {errorVerificacion}
                </Alert>
              )}
              {mensajeVerificacion && (
                <Alert severity="success" sx={{ py: 0 }} onClose={() => setMensajeVerificacion(null)}>
                  {mensajeVerificacion}
                </Alert>
              )}
            </Stack>
          </Stack>
        </SectionCard>
      )}

      {!expediente.recibible && !enAnalisis && (
        <Alert severity="info">Este expediente no está en un estado operable desde Análisis.</Alert>
      )}
    </Stack>
  )
}
