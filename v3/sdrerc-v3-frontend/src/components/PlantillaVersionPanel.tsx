import { useEffect, useRef, useState } from 'react'
import {
  Alert,
  Button,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
import DownloadOutlinedIcon from '@mui/icons-material/DownloadOutlined'
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutlined'
import PlayCircleOutlineIcon from '@mui/icons-material/PlayCircleOutlined'
import {
  activarVersion,
  cargarNuevaVersion,
  descargarPlantilla,
  generarDocumentoPrueba,
  listarHistorial,
  type PlantillaDocumentoAdmin,
} from '../api/adminPlantillasApi'
import { ApiError, descargarBlob } from '../api/http'
import { SectionCard } from './RegistroExpedienteForm'
import { StatusBadge } from './StatusBadge'

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const d = new Date(iso)
  return d.toLocaleString('es-PE', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function formatearTamano(bytes: number): string {
  if (!bytes) return '-'
  const kb = bytes / 1024
  return kb < 1024 ? `${kb.toFixed(0)} KB` : `${(kb / 1024).toFixed(1)} MB`
}

/**
 * Subpestaña "Plantilla actual" del tipo de documento seleccionado: historial de versiones
 * (mismo criterio que V2, `JPanelPlantillasDocumentoV2` — 1 sola versión activa a la vez, nunca se
 * borra físicamente) + "Generar documento de prueba" (mejora explícita de V3, ver Javadoc de
 * `AnalisisPlantillaDocumentoService#generarDocumentoPrueba`).
 */
export function PlantillaVersionPanel({ tipo, onCambio }: { tipo: PlantillaDocumentoAdmin; onCambio: () => void }) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [historial, setHistorial] = useState<PlantillaDocumentoAdmin[]>([])
  const [comentario, setComentario] = useState('')
  const [cargando, setCargando] = useState(false)
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function cargar() {
    try {
      setHistorial(await listarHistorial(tipo.idTipoDocumentoAdjunto))
    } catch {
      setHistorial([])
    }
  }

  useEffect(() => {
    cargar()
    setMensaje(null)
    setError(null)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tipo.idTipoDocumentoAdjunto])

  async function subirArchivo(e: React.ChangeEvent<HTMLInputElement>) {
    const archivo = e.target.files?.[0]
    if (!archivo) return
    setCargando(true)
    setError(null)
    setMensaje(null)
    try {
      await cargarNuevaVersion(tipo.idTipoDocumentoAdjunto, archivo, comentario)
      setMensaje('Nueva versión cargada y activada correctamente.')
      setComentario('')
      await cargar()
      onCambio()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cargar la plantilla.')
    } finally {
      setCargando(false)
      if (inputRef.current) inputRef.current.value = ''
    }
  }

  async function descargar(version: PlantillaDocumentoAdmin) {
    if (!version.idPlantillaDocumento) return
    try {
      const { blob } = await descargarPlantilla(version.idPlantillaDocumento, version.nombreArchivo ?? 'plantilla.docx')
      descargarBlob(blob, version.nombreArchivo ?? `plantilla_v${version.version}.docx`)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo descargar la plantilla.')
    }
  }

  async function activar(version: PlantillaDocumentoAdmin) {
    if (!version.idPlantillaDocumento) return
    const confirmado = window.confirm(`¿Activar la versión ${version.version}? Reemplazará a la versión actualmente activa.`)
    if (!confirmado) return
    setError(null)
    try {
      await activarVersion(version.idPlantillaDocumento)
      setMensaje(`Versión ${version.version} activada correctamente.`)
      await cargar()
      onCambio()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo activar la versión seleccionada.')
    }
  }

  async function generarPrueba() {
    setError(null)
    try {
      const blob = await generarDocumentoPrueba(tipo.idTipoDocumentoAdjunto)
      descargarBlob(blob, `prueba_${tipo.tipoDocumentoCodigo}.docx`)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo generar el documento de prueba.')
    }
  }

  return (
    <Stack spacing={2}>
      <SectionCard title="Cargar nueva versión">
        <Stack spacing={2}>
          <Typography variant="body2" color="text.secondary">
            Solo archivos <code>.docx</code>, máximo 15 MB. Al cargar, la nueva versión queda activa de inmediato
            (la anterior sigue disponible en el historial, nunca se borra).
          </Typography>
          <TextField
            label="Comentario (opcional)"
            size="small"
            value={comentario}
            onChange={(e) => setComentario(e.target.value)}
            placeholder="Ej. Corrige el párrafo de fundamentos legales"
          />
          <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap">
            <Button variant="contained" component="label" startIcon={<UploadFileOutlinedIcon />} disabled={cargando}>
              Seleccionar archivo .docx
              <input ref={inputRef} type="file" accept=".docx" hidden onChange={subirArchivo} />
            </Button>
            <Button variant="outlined" startIcon={<PlayCircleOutlineIcon />} onClick={generarPrueba}>
              Generar documento de prueba
            </Button>
          </Stack>
          {mensaje && <Alert severity="success">{mensaje}</Alert>}
          {error && <Alert severity="error">{error}</Alert>}
        </Stack>
      </SectionCard>
      <SectionCard title="Historial de versiones">
        {historial.length === 0 ? (
          <Typography color="text.secondary">
            Sin versiones cargadas — este tipo de documento usa el archivo de plantilla del sistema (carpeta
            docs/plantillas) hasta que se cargue una versión propia acá.
          </Typography>
        ) : (
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Versión</TableCell>
                <TableCell>Archivo</TableCell>
                <TableCell>Tamaño</TableCell>
                <TableCell>Comentario</TableCell>
                <TableCell>Cargado por</TableCell>
                <TableCell>Fecha</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell align="right">Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {historial.map((v) => (
                <TableRow key={v.idPlantillaDocumento} sx={{ opacity: v.activo ? 1 : 0.7 }}>
                  <TableCell>{v.version}</TableCell>
                  <TableCell>{v.nombreArchivo}</TableCell>
                  <TableCell>{formatearTamano(v.tamanoBytes)}</TableCell>
                  <TableCell>{v.comentario ?? '-'}</TableCell>
                  <TableCell>{v.cargadoPor ?? '-'}</TableCell>
                  <TableCell>{formatearFecha(v.creadoEn)}</TableCell>
                  <TableCell>
                    <StatusBadge label={v.activo ? 'Vigente' : 'Histórica'} tone={v.activo ? 'success' : 'neutral'} />
                  </TableCell>
                  <TableCell align="right">
                    <Stack direction="row" spacing={0.5} justifyContent="flex-end">
                      <Tooltip title="Descargar">
                        <Button size="small" onClick={() => descargar(v)}>
                          <DownloadOutlinedIcon fontSize="small" />
                        </Button>
                      </Tooltip>
                      {!v.activo && (
                        <Tooltip title="Activar esta versión">
                          <Button size="small" onClick={() => activar(v)}>
                            <CheckCircleOutlineIcon fontSize="small" />
                          </Button>
                        </Tooltip>
                      )}
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </SectionCard>
    </Stack>
  )
}
