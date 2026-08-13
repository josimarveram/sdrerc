import { Dialog, DialogContent, DialogTitle, Typography } from '@mui/material'
import { GrupoFamiliarContent } from './GrupoFamiliarContent'

interface GrupoFamiliarPanelProps {
  open: boolean
  onClose: () => void
  idExpedientePrincipal: number
  numeroExpediente: string
  titular: string
  onAsociado?: () => void
}

/**
 * Equivalente web del panel "Grupo Familiar" — compartido por Registro y Asignación (ver CLAUDE.md
 * raíz). Envoltorio `Dialog` delgado sobre `GrupoFamiliarContent.tsx` (la lógica real vive ahí, para
 * poder reutilizarla también embebida como subpestaña en Bandeja Registro — ver
 * `WorkspaceTabsDrawer.tsx`). Asignación sigue usando este componente sin cambios.
 */
export function GrupoFamiliarPanel({
  open,
  onClose,
  idExpedientePrincipal,
  numeroExpediente,
  titular,
  onAsociado,
}: GrupoFamiliarPanelProps) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        Grupo Familiar
        <Typography variant="body2" color="text.secondary">
          {numeroExpediente} — {titular}
        </Typography>
      </DialogTitle>
      <DialogContent>
        {open && <GrupoFamiliarContent idExpedientePrincipal={idExpedientePrincipal} onAsociado={onAsociado} />}
      </DialogContent>
    </Dialog>
  )
}
