import { Alert, Button, Stack, Tooltip, Typography } from '@mui/material'
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined'
import RefreshOutlinedIcon from '@mui/icons-material/RefreshOutlined'
import { OnlyOfficeEditorPanel } from './OnlyOfficeEditorPanel'
import type { PlantillaDocumentoAdmin } from '../api/adminPlantillasApi'

/**
 * Subpestaña "Editor en línea" — edita en el navegador, vía OnlyOffice Document Server, la versión
 * VIGENTE del tipo de documento enfocado. A diferencia del intento anterior (`OnlyOfficeEditorDialog`,
 * retirado), el editor vive embebido dentro del propio panel del workspace, no en un `Dialog`
 * `fullScreen` aparte — pedido explícito del usuario.
 */
export function PlantillaEditorEnLineaContent({
  tipo,
  onIrAPlantilla,
  onActualizar,
}: {
  tipo: PlantillaDocumentoAdmin
  onIrAPlantilla: () => void
  onActualizar: () => void
}) {
  if (tipo.idPlantillaDocumento === null) {
    return (
      <Stack spacing={2} alignItems="flex-start">
        <Alert severity="info" icon={<DescriptionOutlinedIcon fontSize="inherit" />}>
          Este tipo de documento todavía usa la plantilla del sistema (carpeta <code>docs/plantillas</code>) — no tiene
          una versión propia cargada. Suba una versión en la subpestaña "Plantilla" para poder editarla en línea acá.
        </Alert>
        <Button variant="outlined" onClick={onIrAPlantilla}>
          Ir a "Plantilla"
        </Button>
      </Stack>
    )
  }

  return (
    <Stack spacing={2}>
      <Stack direction="row" alignItems="center" justifyContent="space-between" flexWrap="wrap" gap={1}>
        <Typography variant="body2" color="text.secondary">
          Edición real del <code>.docx</code> vigente (versión {tipo.version}). Al guardar (botón propio del editor)
          se crea una nueva versión y queda activada de inmediato — visible en el historial de la subpestaña "Plantilla".
        </Typography>
        <Tooltip title="El editor guarda en segundo plano; use esto para reflejar la versión más reciente en el resto del panel.">
          <Button size="small" startIcon={<RefreshOutlinedIcon />} onClick={onActualizar}>
            Actualizar versión
          </Button>
        </Tooltip>
      </Stack>
      <OnlyOfficeEditorPanel idPlantillaDocumento={tipo.idPlantillaDocumento} />
    </Stack>
  )
}
