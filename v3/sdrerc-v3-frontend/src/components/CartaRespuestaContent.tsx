import { useEffect, useMemo, useRef, useState } from 'react'
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
import AddOutlinedIcon from '@mui/icons-material/AddOutlined'
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined'
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined'
import {
  eliminarDocumentoAnalizado,
  guardarDocumentoAnalizado,
  listarDocumentosAnalizados,
  listarEstadosDocumentoAnalisis,
  listarTiposDocumentoAdjuntoAnalisis,
  type DocumentoAnalizado,
} from '../api/analisisApi'
import {
  listarAbogadosCartasRespuesta,
  obtenerAbogadoActualCartaRespuesta,
  registrarAsignacionCartaRespuesta,
  type AsignacionCartaRespuesta,
  type UsuarioAsignable,
} from '../api/asignacionApi'
import type { CatalogoItem } from '../api/registroApi'
import { ApiError } from '../api/http'
import { hoyISO } from '../utils/fechas'
import { SectionCard } from './RegistroExpedienteForm'
import { StatusBadge } from './StatusBadge'

interface CartaRespuestaContentProps {
  carta: AsignacionCartaRespuesta
  onActualizado?: () => void
}

const TIPO_CARTA_RESPUESTA_CODIGO = 'ANALISIS_DOC_20_CARTA_RESPUESTA'
const TIPO_CARTA_RESPUESTA_NOMBRE = 'Carta de Respuesta'

function documentoVacio(idExpediente: number, nivel: 0 | 1, idDocumentoPadre: number | null, idTemporal: number): DocumentoAnalizado {
  return {
    idDocumentoAnalizado: idTemporal,
    idExpediente,
    idExpedienteAnalisis: null,
    tipoDocumentoCodigo: nivel === 1 ? TIPO_CARTA_RESPUESTA_CODIGO : '',
    tipoDocumentoNombre: nivel === 1 ? TIPO_CARTA_RESPUESTA_NOMBRE : '',
    // 25/08/2026, pedido explícito del usuario: "la carta de respuesta no debería tener estado
    // documento" — un documento hijo (nivel=1) nunca expone un combo "Estado" en ninguna grilla del
    // sistema; el backend (DocumentoAnalisisDAO) ya no lo exige para nivel=1 (persiste NULL).
    estadoDocumentoCodigo: nivel === 0 ? 'EN_PROYECTO' : '',
    estadoDocumentoNombre: nivel === 0 ? 'En proyecto' : '',
    fechaDocumento: null,
    numeroDocumento: '',
    descripcion: '',
    notificado: false,
    fechaAcuse: null,
    requiereRespuesta: nivel === 0,
    // 25/08/2026, pedido explícito del usuario: "Pendiente" ya no es una opción del combo — una
    // carta de respuesta nueva nace sin confirmación elegida (el backend sigue normalizando
    // internamente cualquier valor vacío a "PENDIENTE" solo para su propio cálculo de estado final).
    confirmacionRespuesta: '',
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
 * Contenido de la subpestaña "Registrar Asignación" del workspace de "Cartas de respuesta"
 * (Asignación). Réplica funcional de `CartaRespuestaTreeGridPanelV2` (V2, `permitirPedidoEnHijo=false`)
 * + el bloque "Destino operativo" de `crearDestinoOperativoCarta()` — mismo mecanismo de maestro-
 * detalle ya construido para "Documentos analizados" de Análisis (`AnalisisContent.tsx`), reutilizando
 * literalmente los mismos endpoints de documento (`/api/analisis/documentos*`, genéricos por
 * expediente, sin restricción de etapa) en vez de duplicar CRUD.
 *
 * **Diferencia real con V2 (no bug)**: el equipo destino está SIEMPRE fijo a `EQ_ANALISIS` — V2
 * también lo fija así en este panel (combo con 1 solo ítem, sin opción de elegir otro equipo), así
 * que acá se resuelve 100% server-side, sin combo "Equipo destino" en la UI (mismo criterio ya usado
 * en Ejecución "Enviar a Supervisión").
 *
 * **"Registrar Asignación" habilitado según la carta ORIGINAL de la bandeja** (`carta.idDocumentoAnalizado`,
 * el documento en el que se hizo doble clic), no según qué fila esté enfocada en la grilla "Documentos
 * que requieren respuesta" — mismo criterio exacto que V2 (`cartaRespuestaSeleccionadaTieneRespuesta`):
 * el usuario puede revisar/agregar respuestas a otros documentos del expediente en esta misma pantalla,
 * pero el botón solo confirma que la carta que abrió el panel ya tiene su respuesta.
 */
export function CartaRespuestaContent({ carta, onActualizado }: CartaRespuestaContentProps) {
  const [documentos, setDocumentos] = useState<DocumentoAnalizado[]>([])
  const [documentosLoading, setDocumentosLoading] = useState(false)
  const [padreFocoId, setPadreFocoId] = useState<number | null>(carta.idDocumentoAnalizado)
  const [guardandoId, setGuardandoId] = useState<number | null>(null)
  const [errorDocumentos, setErrorDocumentos] = useState<string | null>(null)
  const tempIdRef = useRef(-1)

  const [tiposDocumento, setTiposDocumento] = useState<CatalogoItem[]>([])
  const [estadosDocumento, setEstadosDocumento] = useState<CatalogoItem[]>([])

  const [abogados, setAbogados] = useState<UsuarioAsignable[]>([])
  const [idAbogado, setIdAbogado] = useState<number | ''>('')
  const [abogadoActualId, setAbogadoActualId] = useState<number | null>(null)
  const [comentario, setComentario] = useState('')
  const [procesando, setProcesando] = useState(false)
  const [mensajeAsignacion, setMensajeAsignacion] = useState<string | null>(null)
  const [errorAsignacion, setErrorAsignacion] = useState<string | null>(null)

  useEffect(() => {
    Promise.all([listarTiposDocumentoAdjuntoAnalisis(), listarEstadosDocumentoAnalisis(), listarAbogadosCartasRespuesta()])
      .then(([t, e, a]) => {
        setTiposDocumento(t)
        setEstadosDocumento(e)
        setAbogados(a)
      })
      .catch(() => undefined)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    setPadreFocoId(carta.idDocumentoAnalizado)
    setMensajeAsignacion(null)
    setErrorAsignacion(null)
    setComentario('')
    recargarDocumentos()
    obtenerAbogadoActualCartaRespuesta(carta.idExpediente)
      .then((r) => setAbogadoActualId(r.idAbogado))
      .catch(() => setAbogadoActualId(null))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [carta.idExpediente, carta.idDocumentoAnalizado])

  useEffect(() => {
    if (abogadoActualId !== null && abogados.some((a) => a.idUsuario === abogadoActualId)) {
      setIdAbogado(abogadoActualId)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [abogadoActualId, abogados])

  function recargarDocumentos() {
    setDocumentosLoading(true)
    listarDocumentosAnalizados(carta.idExpediente)
      .then(setDocumentos)
      .catch(() => setDocumentos([]))
      .finally(() => setDocumentosLoading(false))
  }

  const padres = useMemo(() => documentos.filter((d) => d.nivel === 0 && d.activo && d.requiereRespuesta), [documentos])
  const hijos = useMemo(
    () => documentos.filter((d) => d.nivel === 1 && d.activo && d.idDocumentoPadre === padreFocoId),
    [documentos, padreFocoId],
  )
  const padreFoco = padres.find((p) => p.idDocumentoAnalizado === padreFocoId) ?? null
  const tieneRespuestaCartaOriginal = useMemo(
    () => documentos.some((d) => d.nivel === 1 && d.activo && d.idDocumentoPadre === carta.idDocumentoAnalizado),
    [documentos, carta.idDocumentoAnalizado],
  )

  function actualizarDocumento(id: number, cambios: Partial<DocumentoAnalizado>) {
    setDocumentos((prev) => prev.map((d) => (d.idDocumentoAnalizado === id ? { ...d, ...cambios } : d)))
  }

  function handleAgregarDocumento() {
    const id = tempIdRef.current--
    setDocumentos((prev) => [...prev, documentoVacio(carta.idExpediente, 0, null, id)])
    setPadreFocoId(id)
  }

  function handleAgregarRelacionado() {
    if (!padreFoco || padreFoco.idDocumentoAnalizado === null || padreFoco.idDocumentoAnalizado < 0) return
    const id = tempIdRef.current--
    setDocumentos((prev) => [...prev, documentoVacio(carta.idExpediente, 1, padreFoco.idDocumentoAnalizado, id)])
  }

  async function handleGuardarFila(doc: DocumentoAnalizado) {
    setGuardandoId(doc.idDocumentoAnalizado)
    setErrorDocumentos(null)
    try {
      const payload: DocumentoAnalizado = {
        ...doc,
        idExpediente: carta.idExpediente,
        idDocumentoAnalizado: (doc.idDocumentoAnalizado ?? 0) < 0 ? null : doc.idDocumentoAnalizado,
      }
      const idReal = await guardarDocumentoAnalizado(payload)
      const idAnterior = doc.idDocumentoAnalizado
      const lista = await listarDocumentosAnalizados(carta.idExpediente)
      setDocumentos((prev) => {
        const pendientes = prev.filter((d) => d.idDocumentoAnalizado !== idAnterior && (d.idDocumentoAnalizado ?? 0) < 0)
        return [...lista, ...pendientes]
      })
      if (padreFocoId === idAnterior) {
        setPadreFocoId(idReal)
      }
      onActualizado?.()
    } catch (e) {
      setErrorDocumentos(e instanceof ApiError ? e.message : 'No se pudo guardar el documento.')
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
    setErrorDocumentos(null)
    try {
      await eliminarDocumentoAnalizado(carta.idExpediente, doc.idDocumentoAnalizado as number)
      if (padreFocoId === doc.idDocumentoAnalizado) {
        setPadreFocoId(null)
      }
      recargarDocumentos()
      onActualizado?.()
    } catch (e) {
      setErrorDocumentos(e instanceof ApiError ? e.message : 'No se pudo eliminar el documento.')
    } finally {
      setGuardandoId(null)
    }
  }

  async function handleRegistrarAsignacion() {
    if (!tieneRespuestaCartaOriginal) {
      setErrorAsignacion('Agregue la carta de respuesta en la grilla inferior antes de registrar la asignación.')
      return
    }
    if (idAbogado === '') {
      setErrorAsignacion('Seleccione el abogado responsable.')
      return
    }
    const confirmado = window.confirm(
      `Se derivará el expediente ${carta.numeroExpediente} de vuelta a Análisis (Derivado).\n¿Desea continuar?`,
    )
    if (!confirmado) return
    setProcesando(true)
    setErrorAsignacion(null)
    setMensajeAsignacion(null)
    try {
      const resultado = await registrarAsignacionCartaRespuesta({ idExpediente: carta.idExpediente, idAbogado, comentario })
      setMensajeAsignacion(resultado.mensaje)
      onActualizado?.()
    } catch (e) {
      setErrorAsignacion(e instanceof ApiError ? e.message : 'No se pudo registrar la asignación.')
    } finally {
      setProcesando(false)
    }
  }

  const abogadoSeleccionado = abogados.find((a) => a.idUsuario === idAbogado) ?? null

  return (
    <Stack spacing={2}>
      <SectionCard title="Documentos que requieren respuesta">
        <Stack spacing={2}>
          {errorDocumentos && (
            <Alert severity="error" onClose={() => setErrorDocumentos(null)}>
              {errorDocumentos}
            </Alert>
          )}
          <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end' }}>
            {/* 25/08/2026, pedido explícito del usuario: bloqueado — este panel es exclusivamente
                para registrar la respuesta de una carta ya existente, no para dar de alta documentos
                principales nuevos (eso es responsabilidad de Análisis). */}
            <Tooltip title="No disponible en este panel: los documentos principales se agregan desde Análisis.">
              <span>
                <Button size="small" startIcon={<AddOutlinedIcon />} onClick={handleAgregarDocumento} disabled>
                  Documento
                </Button>
              </span>
            </Tooltip>
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
                  <TableCell>Fecha Acuse</TableCell>
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
                        Sin documentos que requieran respuesta.
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
                      sx={{ cursor: 'pointer', bgcolor: doc.idDocumentoAnalizado === carta.idDocumentoAnalizado ? 'action.hover' : undefined }}
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
                          <Tooltip title="Eliminar">
                            <span>
                              <IconButton size="small" onClick={() => handleEliminarFila(doc)}>
                                <DeleteOutlineOutlinedIcon fontSize="small" />
                              </IconButton>
                            </span>
                          </Tooltip>
                        </Stack>
                      </TableCell>
                      <TableCell onClick={(e) => e.stopPropagation()} sx={{ minWidth: 180 }}>
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
                            actualizarDocumento(doc.idDocumentoAnalizado as number, {
                              estadoDocumentoCodigo: e.target.value,
                              estadoDocumentoNombre: est?.nombre || '',
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
                      <TableCell>{doc.fechaAcuse || '-'}</TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>

          <Box>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1 }}>
              <Typography variant="subtitle2">
                Cartas de respuesta {padreFoco ? `— ${padreFoco.tipoDocumentoNombre || 'documento seleccionado'}` : ''}
              </Typography>
              {padreFoco?.idDocumentoAnalizado === carta.idDocumentoAnalizado && <StatusBadge label="Carta seleccionada en la bandeja" tone="info" />}
            </Stack>
            {!padreFoco ? (
              <Typography variant="body2" color="text.secondary">
                Seleccione un documento arriba para ver/agregar sus cartas de respuesta.
              </Typography>
            ) : (
              <Stack spacing={1.5}>
                <Stack direction="row" sx={{ justifyContent: 'flex-end' }}>
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
                        <TableCell>Confirmación de respuesta</TableCell>
                        <TableCell>Fecha Respuesta</TableCell>
                        <TableCell>Fecha Publ. Edicto</TableCell>
                        <TableCell>Hoja de Envío</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {hijos.length === 0 ? (
                        <TableRow>
                          <TableCell colSpan={6}>
                            <Typography variant="body2" color="text.secondary">
                              Sin cartas de respuesta registradas.
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
                                {/* 25/08/2026, pedido explícito del usuario: esta grilla es
                                    exclusivamente de cartas de respuesta — no se debe poder elegir
                                    ningún otro tipo de documento acá. */}
                                {tiposDocumento
                                  .filter((t) => t.codigo === TIPO_CARTA_RESPUESTA_CODIGO)
                                  .map((t) => (
                                    <MenuItem key={t.codigo} value={t.codigo ?? ''}>
                                      {t.nombre}
                                    </MenuItem>
                                  ))}
                              </TextField>
                            </TableCell>
                            <TableCell sx={{ minWidth: 140 }}>
                              <TextField
                                select
                                size="small"
                                fullWidth
                                value={doc.confirmacionRespuesta === 'PENDIENTE' ? '' : doc.confirmacionRespuesta}
                                onChange={(e) => {
                                  const valor = e.target.value
                                  // 25/08/2026, pedido explícito del usuario: "Sí" autocompleta la
                                  // Fecha Respuesta con hoy; "No" la deja sin ninguna fecha por defecto.
                                  actualizarDocumento(doc.idDocumentoAnalizado as number, {
                                    confirmacionRespuesta: valor,
                                    fechaRespuesta: valor === 'SI' ? hoyISO() : valor === 'NO' ? null : doc.fechaRespuesta,
                                  })
                                }}
                              >
                                <MenuItem value="">Seleccione</MenuItem>
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

      <SectionCard title="Destino operativo">
        <Stack spacing={2}>
          <Typography variant="body2" color="text.secondary">
            Deriva el expediente de vuelta a Análisis (Equipo Análisis) para que el abogado continúe trabajándolo, con la carta de
            respuesta ya vinculada.
          </Typography>
          <TextField
            label="Abogado actual"
            size="small"
            fullWidth
            value={abogados.find((a) => a.idUsuario === abogadoActualId)?.nombreCompleto || 'Sin asignación previa'}
            slotProps={{ input: { readOnly: true } }}
          />
          <TextField
            select
            label="Abogado responsable"
            size="small"
            fullWidth
            value={idAbogado}
            onChange={(e) => setIdAbogado(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <MenuItem value="">Seleccione</MenuItem>
            {abogados.map((a) => (
              <MenuItem key={a.idUsuario} value={a.idUsuario}>
                {a.nombreCompleto}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Supervisor"
            size="small"
            fullWidth
            value={abogadoSeleccionado?.supervisorNombre || '-'}
            slotProps={{ input: { readOnly: true } }}
          />
          <TextField
            label="Comentario (opcional)"
            size="small"
            fullWidth
            multiline
            minRows={2}
            value={comentario}
            onChange={(e) => setComentario(e.target.value)}
          />
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
            <Button variant="contained" onClick={handleRegistrarAsignacion} disabled={procesando} sx={{ alignSelf: 'flex-start' }}>
              Registrar Asignación
            </Button>
            {errorAsignacion && (
              <Alert severity="error" sx={{ py: 0 }} onClose={() => setErrorAsignacion(null)}>
                {errorAsignacion}
              </Alert>
            )}
            {mensajeAsignacion && (
              <Alert severity="success" sx={{ py: 0 }} onClose={() => setMensajeAsignacion(null)}>
                {mensajeAsignacion}
              </Alert>
            )}
          </Stack>
          {!tieneRespuestaCartaOriginal && (
            <Alert severity="info">
              Agregue la carta de respuesta de "{carta.tipoDocumentoNombre}" en la grilla superior antes de registrar la asignación.
            </Alert>
          )}
        </Stack>
      </SectionCard>
    </Stack>
  )
}
