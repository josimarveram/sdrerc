import { Dialog, DialogContent, DialogTitle, Typography } from '@mui/material'
import { AsociarDuplicadosContent } from './AsociarDuplicadosContent'

interface AsociarDuplicadosPanelProps {
  open: boolean
  onClose: () => void
  idExpedientePrincipal: number
  numeroExpediente: string
  titular: string
  onAsociado?: () => void
}

/**
 * Equivalente web del panel "Asociar" — compartido por Registro y Asignación (ver CLAUDE.md raíz).
 * Envoltorio `Dialog` delgado sobre `AsociarDuplicadosContent.tsx` (la lógica real vive ahí, para
 * poder reutilizarla también embebida como subpestaña en Bandeja Registro — ver
 * `WorkspaceTabsDrawer.tsx`). Asignación sigue usando este componente sin cambios.
 */
export function AsociarDuplicadosPanel({
  open,
  onClose,
  idExpedientePrincipal,
  numeroExpediente,
  titular,
  onAsociado,
}: AsociarDuplicadosPanelProps) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        Asociar
        <Typography variant="body2" color="text.secondary">
          {numeroExpediente} — {titular}
        </Typography>
      </DialogTitle>
      <DialogContent>
        {open && <AsociarDuplicadosContent idExpedientePrincipal={idExpedientePrincipal} onAsociado={onAsociado} />}
      </DialogContent>
    </Dialog>
  )
}
