import { useEffect, useRef, useState } from 'react'
import { Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, IconButton, Stack, Typography } from '@mui/material'
import CloseOutlinedIcon from '@mui/icons-material/CloseOutlined'
import { renderAsync } from 'docx-preview'

/** Colores de resaltado, uno por tipo de marcador (ver `resaltarMarcadores`). */
const COLOR_VARIABLE = '#1565C0'
const COLOR_CONTENIDO = '#2E7D32'
const COLOR_CONDICIONAL = '#B8860B'

/**
 * Marcadores reales soportados por `AnalisisPlantillaDocumentoService` (ver v3/CLAUDE.md, sección
 * Plantillas): `#variable#`, `[[CONTENIDO]]`/`[[CONTENIDO:seccion]]`, `[[SI_variable:v1|v2]]` y
 * `[[FIN_SI]]`. Un solo regex con 4 grupos de captura para poder distinguir el tipo de cada match.
 */
const PATRON_MARCADORES =
  /(#[a-zA-Z0-9_]+#)|(\[\[CONTENIDO(?::[a-zA-Z0-9_]+)?\]\])|(\[\[SI_[a-zA-Z0-9_]+:[^\]]*\]\])|(\[\[FIN_SI\]\])/g

function estiloMarcador(color: string): string {
  return `background:${color}22;color:${color};border:1px solid ${color}66;border-radius:4px;padding:0 3px;font-weight:600;white-space:pre-wrap;`
}

/**
 * Recorre los nodos de texto ya renderizados por docx-preview y envuelve cada marcador encontrado
 * en un `<span>` resaltado. Limitación conocida y aceptada: si el autor del Word aplicó formato
 * distinto a mitad de un marcador (ej. negrita solo en la primera mitad de `#variable#`), el token
 * queda partido en 2 nodos de texto distintos por Word/docx-preview y no se resalta completo — caso
 * raro en la práctica, mismo límite que tiene cualquier motor de plantillas basado en texto plano.
 */
function resaltarMarcadores(root: HTMLElement) {
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT)
  const nodos: Text[] = []
  let actual: Node | null
  while ((actual = walker.nextNode())) nodos.push(actual as Text)

  for (const nodo of nodos) {
    const texto = nodo.textContent ?? ''
    PATRON_MARCADORES.lastIndex = 0
    if (!PATRON_MARCADORES.test(texto)) continue
    PATRON_MARCADORES.lastIndex = 0

    const frag = document.createDocumentFragment()
    let ultimoIndice = 0
    let match: RegExpExecArray | null
    while ((match = PATRON_MARCADORES.exec(texto))) {
      if (match.index > ultimoIndice) {
        frag.appendChild(document.createTextNode(texto.slice(ultimoIndice, match.index)))
      }
      const [, variable, contenido, condicionSi, finSi] = match
      const color = variable ? COLOR_VARIABLE : contenido ? COLOR_CONTENIDO : condicionSi || finSi ? COLOR_CONDICIONAL : '#000'
      const span = document.createElement('span')
      span.textContent = match[0]
      span.setAttribute('style', estiloMarcador(color))
      frag.appendChild(span)
      ultimoIndice = match.index + match[0].length
    }
    if (ultimoIndice < texto.length) {
      frag.appendChild(document.createTextNode(texto.slice(ultimoIndice)))
    }
    nodo.parentNode?.replaceChild(frag, nodo)
  }
}

export interface VistaPreviaPlantillaDialogProps {
  open: boolean
  onClose: () => void
  titulo: string
  /** Se llama recién al abrir el diálogo (no antes) — evita descargar el archivo si el usuario nunca hace clic en "Vista previa". */
  obtenerArchivo: () => Promise<Blob | File>
  /**
   * Opcional: convierte el diálogo en un paso de confirmación (ej. "Cargar nueva versión", donde el
   * archivo recién seleccionado se sube recién al confirmar acá, no al elegirlo) — sin esto, el
   * diálogo es de solo lectura con un único botón "Cerrar".
   */
  accionConfirmar?: { etiqueta: string; onConfirmar: () => void; procesando?: boolean }
}

/**
 * Vista previa del `.docx` real (plantilla o archivo recién seleccionado, sin subir) directo en el
 * navegador, vía `docx-preview` (Apache-2.0, 100% cliente, sin servidor nuevo). Resalta los
 * marcadores de variables/bloques/condicionales para que el administrador vea de un vistazo dónde
 * quedan ubicados dentro del documento real, sin depender de abrirlo en Word de escritorio.
 */
export function VistaPreviaPlantillaDialog({ open, onClose, titulo, obtenerArchivo, accionConfirmar }: VistaPreviaPlantillaDialogProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    let cancelado = false

    async function render() {
      setCargando(true)
      setError(null)
      try {
        const archivo = await obtenerArchivo()
        if (cancelado || !containerRef.current) return
        containerRef.current.innerHTML = ''
        await renderAsync(archivo, containerRef.current, undefined, {
          inWrapper: true,
          ignoreHeight: true,
          breakPages: true,
        })
        if (!cancelado) resaltarMarcadores(containerRef.current)
      } catch {
        if (!cancelado) setError('No se pudo generar la vista previa. Verifique que el archivo sea un .docx válido y no esté dañado.')
      } finally {
        if (!cancelado) setCargando(false)
      }
    }

    render()
    return () => {
      cancelado = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open])

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth slotProps={{ paper: { sx: { height: '90vh' } } }}>
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography variant="h6" component="span" noWrap sx={{ pr: 2 }}>
          Vista previa: {titulo}
        </Typography>
        <IconButton onClick={onClose} size="small">
          <CloseOutlinedIcon fontSize="small" />
        </IconButton>
      </DialogTitle>
      <DialogContent dividers sx={{ bgcolor: '#EDEFF2', overflow: 'auto' }}>
        <Stack direction="row" spacing={1} flexWrap="wrap" sx={{ mb: 2 }}>
          <Chip size="small" label="Variable (#variable#)" sx={{ bgcolor: `${COLOR_VARIABLE}22`, color: COLOR_VARIABLE, border: `1px solid ${COLOR_VARIABLE}66`, fontWeight: 600 }} />
          <Chip size="small" label="Bloque de contenido ([[CONTENIDO]])" sx={{ bgcolor: `${COLOR_CONTENIDO}22`, color: COLOR_CONTENIDO, border: `1px solid ${COLOR_CONTENIDO}66`, fontWeight: 600 }} />
          <Chip size="small" label="Condicional ([[SI_...]])" sx={{ bgcolor: `${COLOR_CONDICIONAL}22`, color: COLOR_CONDICIONAL, border: `1px solid ${COLOR_CONDICIONAL}66`, fontWeight: 600 }} />
        </Stack>
        {cargando && (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
            <CircularProgress />
          </Box>
        )}
        {error && <Alert severity="error">{error}</Alert>}
        <Box ref={containerRef} sx={{ '& .docx-wrapper': { bgcolor: 'transparent' } }} />
      </DialogContent>
      {accionConfirmar && (
        <DialogActions>
          <Button onClick={onClose} disabled={accionConfirmar.procesando}>
            Cancelar
          </Button>
          <Button variant="contained" onClick={accionConfirmar.onConfirmar} disabled={cargando || !!error || accionConfirmar.procesando}>
            {accionConfirmar.etiqueta}
          </Button>
        </DialogActions>
      )}
    </Dialog>
  )
}
