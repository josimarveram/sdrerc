import { useEffect, useMemo, useState } from 'react'
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  Box,
  Button,
  Stack,
  Typography,
} from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { WordFileIcon } from './icons/FileTypeIcons'
import { descargarDocumentoWord, listarDocumentosAnalizados, type DocumentoAnalizado } from '../api/analisisApi'
import { ApiError } from '../api/http'
import { StatusBadge } from './StatusBadge'
import { DATA_GRID_ROW_ICON_WORD_CLASS } from '../dataGridTheme'

interface DocumentosExpedienteContentProps {
  idExpediente: number
  numeroExpediente: string
}

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

function Dato({ label, value }: { label: string; value: string | number | boolean | null | undefined }) {
  const texto = value === null || value === undefined || value === '' ? '-' : typeof value === 'boolean' ? (value ? 'Sí' : 'No') : String(value)
  return (
    <Box sx={{ minWidth: 160 }}>
      <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
        {label}
      </Typography>
      <Typography variant="body2">{texto}</Typography>
    </Box>
  )
}

/**
 * Subpestaña "Documentos" del "Expediente 360°" (Bandeja de Expedientes, 24/08/2026) — mejora
 * funcional real sobre V2: `DlgConsolaExpedienteV2#cargarDocumentos` (V2) solo muestra 2 tarjetas
 * resumen ("Documentos: N total" / "Documentos duplicados"), sin listar ni un solo documento real
 * ni su detalle — pese a llamarse "Documentos". Acá se reutiliza `listarDocumentosAnalizados`
 * (mismo endpoint ya construido para Análisis/Verificación/Ejecución/Notificación,
 * `EXPEDIENTE_DOCUMENTO_ANALIZADO`, sin filtrar por etapa) para listar TODOS los documentos reales
 * del expediente, agrupados padre/hijo, con el detalle completo de cada uno expandible — de solo
 * lectura, sin +Documento/Guardar/Eliminar (esas acciones viven en cada módulo operativo).
 */
export function DocumentosExpedienteContent({ idExpediente, numeroExpediente }: DocumentosExpedienteContentProps) {
  const [documentos, setDocumentos] = useState<DocumentoAnalizado[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    listarDocumentosAnalizados(idExpediente)
      .then(setDocumentos)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudieron cargar los documentos del expediente.'))
      .finally(() => setLoading(false))
  }, [idExpediente])

  const padres = useMemo(() => documentos.filter((d) => d.nivel === 0 && d.activo), [documentos])
  const hijosPorPadre = useMemo(() => {
    const mapa = new Map<number, DocumentoAnalizado[]>()
    for (const d of documentos) {
      if (d.nivel === 1 && d.activo && d.idDocumentoPadre !== null) {
        const lista = mapa.get(d.idDocumentoPadre) ?? []
        lista.push(d)
        mapa.set(d.idDocumentoPadre, lista)
      }
    }
    return mapa
  }, [documentos])

  async function handleDescargar(doc: DocumentoAnalizado) {
    try {
      const nombre = `${doc.tipoDocumentoNombre || 'documento'}_${numeroExpediente}.docx`.replace(/\s+/g, '_')
      await descargarDocumentoWord(idExpediente, doc.idDocumentoAnalizado as number, nombre)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo generar el documento Word.')
    }
  }

  return (
    <Stack spacing={2}>
      {error && <Alert severity="error">{error}</Alert>}
      {loading ? (
        <Typography variant="body2" color="text.secondary">
          Cargando documentos...
        </Typography>
      ) : padres.length === 0 ? (
        <Typography variant="body2" color="text.secondary">
          Este expediente todavía no tiene documentos registrados.
        </Typography>
      ) : (
        padres.map((doc) => {
          const hijos = hijosPorPadre.get(doc.idDocumentoAnalizado as number) ?? []
          return (
            <Accordion key={doc.idDocumentoAnalizado} disableGutters>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', flexWrap: 'wrap', width: '100%' }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                    {doc.tipoDocumentoNombre || 'Documento'}
                  </Typography>
                  <StatusBadge label={doc.estadoDocumentoNombre || doc.estadoDocumentoCodigo || 'Sin estado'} tone="info" />
                  {doc.numeroDocumento && <Typography variant="body2" color="text.secondary">N° {doc.numeroDocumento}</Typography>}
                  <Typography variant="body2" color="text.secondary">{formatearFecha(doc.fechaDocumento)}</Typography>
                  {hijos.length > 0 && <StatusBadge label={`${hijos.length} respuesta(s)`} tone="neutral" />}
                </Stack>
              </AccordionSummary>
              <AccordionDetails>
                <Stack spacing={2}>
                  <Stack direction="row" spacing={3} sx={{ flexWrap: 'wrap' }}>
                    <Dato label="Tipo de documento" value={doc.tipoDocumentoNombre} />
                    <Dato label="N° Documento" value={doc.numeroDocumento} />
                    <Dato label="Estado" value={doc.estadoDocumentoNombre} />
                    <Dato label="Fecha de emisión" value={formatearFecha(doc.fechaDocumento)} />
                    <Dato label="¿Requiere respuesta?" value={doc.requiereRespuesta} />
                    <Dato label="Notificado" value={doc.notificado} />
                    <Dato label="Fecha acuse" value={formatearFecha(doc.fechaAcuse)} />
                    <Dato label="¿Requiere publicación?" value={doc.requierePublicacion} />
                    <Dato label="Fecha publicación" value={formatearFecha(doc.fechaPublicacion)} />
                  </Stack>
                  {doc.descripcion && <Dato label="Comentario" value={doc.descripcion} />}
                  {doc.detalleObservacion && <Dato label="Detalle de observación" value={doc.detalleObservacion} />}
                  <Box>
                    <Button
                      size="small"
                      variant="outlined"
                      className={DATA_GRID_ROW_ICON_WORD_CLASS}
                      startIcon={<WordFileIcon />}
                      onClick={() => handleDescargar(doc)}
                      sx={{ borderColor: 'currentColor' }}
                    >
                      Descargar Word
                    </Button>
                  </Box>

                  {hijos.length > 0 && (
                    <Box sx={{ pl: 2, borderLeft: '3px solid', borderColor: 'divider' }}>
                      <Typography variant="subtitle2" sx={{ mb: 1 }}>
                        Documentos de respuesta ({hijos.length})
                      </Typography>
                      <Stack spacing={1.5}>
                        {hijos.map((hijo) => (
                          <Box key={hijo.idDocumentoAnalizado} sx={{ p: 1.5, bgcolor: 'action.hover', borderRadius: 1 }}>
                            <Stack direction="row" spacing={3} sx={{ flexWrap: 'wrap' }}>
                              <Dato label="Tipo" value={hijo.tipoDocumentoNombre} />
                              <Dato label="Confirmación de respuesta" value={hijo.confirmacionRespuesta} />
                              <Dato label="Fecha respuesta" value={formatearFecha(hijo.fechaRespuesta)} />
                              <Dato label="Hoja de envío" value={hijo.numeroHojaEnvioRespuesta} />
                              <Dato label="¿Existe oposición?" value={hijo.existeOposicion} />
                            </Stack>
                            {hijo.descripcion && <Box sx={{ mt: 1 }}><Dato label="Comentario" value={hijo.descripcion} /></Box>}
                          </Box>
                        ))}
                      </Stack>
                    </Box>
                  )}
                </Stack>
              </AccordionDetails>
            </Accordion>
          )
        })
      )}
    </Stack>
  )
}
