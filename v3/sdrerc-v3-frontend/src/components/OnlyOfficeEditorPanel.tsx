import { Alert, Box, CircularProgress } from '@mui/material'
import { useOnlyOfficeEditor } from '../hooks/useOnlyOfficeEditor'

export interface OnlyOfficeEditorPanelProps {
  idPlantillaDocumento: number
  /** Alto fijo en px del editor embebido — OnlyOffice necesita una altura explícita, no fluida. */
  alto?: number
}

/**
 * Edición real del `.docx` embebida DENTRO de la subpestaña "Editor en línea" (Administración &gt;
 * Plantillas de documento), a diferencia de `OnlyOfficeEditorDialog` (retirado) que abría un
 * `Dialog fullScreen` aparte — pedido explícito del usuario de que el editor "se pueda visualizar
 * dentro del mismo panel". Mismo mecanismo de montaje (`useOnlyOfficeEditor`), solo cambia el
 * contenedor visual (`Box` de alto fijo en vez de un `Dialog`).
 */
export function OnlyOfficeEditorPanel({ idPlantillaDocumento, alto = 720 }: OnlyOfficeEditorPanelProps) {
  const containerId = `sdrerc-onlyoffice-editor-${idPlantillaDocumento}`
  const { cargando, error } = useOnlyOfficeEditor(containerId, idPlantillaDocumento, true)

  return (
    <Box sx={{ position: 'relative', width: '100%', height: alto, borderRadius: 2, overflow: 'hidden', border: '1px solid', borderColor: 'divider' }}>
      {cargando && (
        <Box sx={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1, bgcolor: 'background.paper' }}>
          <CircularProgress />
        </Box>
      )}
      {error && (
        <Box sx={{ p: 2 }}>
          <Alert severity="error">{error}</Alert>
        </Box>
      )}
      <Box id={containerId} sx={{ width: '100%', height: '100%' }} />
    </Box>
  )
}
