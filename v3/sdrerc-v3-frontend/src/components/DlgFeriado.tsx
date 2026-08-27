import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  MenuItem,
  Stack,
  TextField,
} from '@mui/material'
import { crearFeriado, editarFeriado, type FeriadoAdmin } from '../api/adminFeriadosApi'
import { ApiError } from '../api/http'

interface DlgFeriadoProps {
  feriado: FeriadoAdmin | null
  onClose: () => void
  onGuardado: () => void
}

const TIPOS_FERIADO = ['NACIONAL', 'REGIONAL', 'LOCAL']

function hoyISO() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** Alta/edición de Administración &gt; Feriados (V2: `JPanelFeriadosV2`, formulario del panel derecho). */
export function DlgFeriado({ feriado, onClose, onGuardado }: DlgFeriadoProps) {
  const [fecha, setFecha] = useState(feriado?.fecha ?? hoyISO())
  const [nombre, setNombre] = useState(feriado?.nombre ?? '')
  const [tipo, setTipo] = useState(feriado?.tipo ?? 'NACIONAL')
  const [activo, setActivo] = useState(feriado?.activo ?? true)
  const [observacion, setObservacion] = useState(feriado?.observacion ?? '')
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setFecha(feriado?.fecha ?? hoyISO())
    setNombre(feriado?.nombre ?? '')
    setTipo(feriado?.tipo ?? 'NACIONAL')
    setActivo(feriado?.activo ?? true)
    setObservacion(feriado?.observacion ?? '')
    setError(null)
  }, [feriado])

  async function guardar() {
    setError(null)
    if (!fecha) {
      setError('La fecha es obligatoria.')
      return
    }
    if (!nombre.trim()) {
      setError('El nombre es obligatorio.')
      return
    }
    setGuardando(true)
    try {
      const request = { idFeriado: feriado?.idFeriado ?? null, fecha, nombre: nombre.trim(), tipo, activo, observacion: observacion.trim() || null }
      if (feriado?.idFeriado) {
        await editarFeriado(feriado.idFeriado, request)
      } else {
        await crearFeriado(request)
      }
      onGuardado()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo guardar el feriado.')
    } finally {
      setGuardando(false)
    }
  }

  return (
    <Dialog open onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>{feriado ? 'Editar feriado' : 'Nuevo feriado'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            label="Fecha"
            type="date"
            fullWidth
            value={fecha}
            onChange={(e) => setFecha(e.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField label="Nombre" fullWidth value={nombre} onChange={(e) => setNombre(e.target.value)} />
          <TextField select label="Tipo" fullWidth value={tipo} onChange={(e) => setTipo(e.target.value)}>
            {TIPOS_FERIADO.map((t) => (
              <MenuItem key={t} value={t}>
                {t}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Observación"
            fullWidth
            multiline
            minRows={3}
            value={observacion}
            onChange={(e) => setObservacion(e.target.value)}
          />
          <FormControlLabel control={<Checkbox checked={activo} onChange={(e) => setActivo(e.target.checked)} />} label="Activo" />
          {error && <Alert severity="error">{error}</Alert>}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={guardando}>
          Cancelar
        </Button>
        <Button onClick={guardar} variant="contained" disabled={guardando}>
          Guardar
        </Button>
      </DialogActions>
    </Dialog>
  )
}
