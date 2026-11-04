import { useState } from 'react'
import {
  Alert,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { restablecerClave } from '../api/adminUsuariosApi'
import { ApiError } from '../api/http'

interface DlgRestablecerClaveProps {
  idUsuario: number
  nombreCompleto: string
  onClose: () => void
  onExito: () => void
}

/** Equivalente web de `DlgRestablecerClaveV2` (V2): Administración &gt; Usuarios &gt; "Restablecer clave". */
export function DlgRestablecerClave({ idUsuario, nombreCompleto, onClose, onExito }: DlgRestablecerClaveProps) {
  const [password, setPassword] = useState('')
  const [reiniciarTotp, setReiniciarTotp] = useState(false)
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function confirmar() {
    setError(null)
    if (password.trim().length < 8) {
      setError('La contraseña temporal debe tener al menos 8 caracteres.')
      return
    }
    setGuardando(true)
    try {
      await restablecerClave(idUsuario, { passwordTemporal: password.trim(), reiniciarVerificacionDosPasos: reiniciarTotp })
      onExito()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo restablecer la contraseña.')
    } finally {
      setGuardando(false)
    }
  }

  return (
    <Dialog open onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Restablecer clave</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Usuario: <strong>{nombreCompleto}</strong>
          </Typography>
          <TextField
            label="Contraseña temporal"
            type="text"
            fullWidth
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            helperText="Mínimo 8 caracteres. El usuario deberá cambiarla en su próximo ingreso."
          />
          <FormControlLabel
            control={<Checkbox checked={reiniciarTotp} onChange={(e) => setReiniciarTotp(e.target.checked)} />}
            label="Reiniciar verificación en dos pasos (el usuario perdió su dispositivo autenticador)"
          />
          {error && <Alert severity="error">{error}</Alert>}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={guardando}>
          Cancelar
        </Button>
        <Button onClick={confirmar} variant="contained" disabled={guardando}>
          Restablecer
        </Button>
      </DialogActions>
    </Dialog>
  )
}
