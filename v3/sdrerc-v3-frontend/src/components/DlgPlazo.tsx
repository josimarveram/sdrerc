import { useEffect, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  MenuItem,
  Slider,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { crearPlazo, editarPlazo, UNIDADES_PLAZO, type PlazoConfiguracion } from '../api/adminPlazosApi'
import { ApiError } from '../api/http'
import { StatusBadge } from './StatusBadge'

interface DlgPlazoProps {
  plazo: PlazoConfiguracion | null
  onClose: () => void
  onGuardado: () => void
}

/**
 * Alta/edición de Administración &gt; Plazos (V2: `JPanelPlazosV2`). Campos base idénticos a V2
 * (código/nombre/ámbito/días/unidad/vigencia/observación/activo) más una mejora explícita de V3
 * (pedido del usuario, "acepto sugerencias... como un experto"): sección "Umbrales de color"
 * opcional que expone `porcentajeVerdeDesde/AmarilloDesde/RojoDesde` — ya existentes en el DTO
 * backend desde V2 pero NUNCA editables desde ningún formulario real de V2 (confirmado con
 * `grep`, ver `v3/CLAUDE.md`) — con una vista previa en vivo del pill "Días" bajo esos umbrales.
 */
export function DlgPlazo({ plazo, onClose, onGuardado }: DlgPlazoProps) {
  const [codigo, setCodigo] = useState(plazo?.codigo ?? '')
  const [nombre, setNombre] = useState(plazo?.nombre ?? '')
  const [ambito, setAmbito] = useState(plazo?.ambito ?? '')
  const [diasPlazo, setDiasPlazo] = useState(plazo?.diasPlazo ?? 30)
  const [unidadPlazo, setUnidadPlazo] = useState(plazo?.unidadPlazo ?? 'HABILES')
  const [fechaVigenciaDesde, setFechaVigenciaDesde] = useState(plazo?.fechaVigenciaDesde ?? '')
  const [fechaVigenciaHasta, setFechaVigenciaHasta] = useState(plazo?.fechaVigenciaHasta ?? '')
  const [observacion, setObservacion] = useState(plazo?.observacion ?? '')
  const [activo, setActivo] = useState(plazo?.activo ?? true)

  const [umbralesActivos, setUmbralesActivos] = useState(
    plazo?.porcentajeVerdeDesde != null || plazo?.porcentajeAmarilloDesde != null || plazo?.porcentajeRojoDesde != null,
  )
  const [verde, setVerde] = useState(plazo?.porcentajeVerdeDesde ?? 50)
  const [amarillo, setAmarillo] = useState(plazo?.porcentajeAmarilloDesde ?? 20)
  const [rojo, setRojo] = useState(plazo?.porcentajeRojoDesde ?? 0)

  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setCodigo(plazo?.codigo ?? '')
    setNombre(plazo?.nombre ?? '')
    setAmbito(plazo?.ambito ?? '')
    setDiasPlazo(plazo?.diasPlazo ?? 30)
    setUnidadPlazo(plazo?.unidadPlazo ?? 'HABILES')
    setFechaVigenciaDesde(plazo?.fechaVigenciaDesde ?? '')
    setFechaVigenciaHasta(plazo?.fechaVigenciaHasta ?? '')
    setObservacion(plazo?.observacion ?? '')
    setActivo(plazo?.activo ?? true)
    setUmbralesActivos(plazo?.porcentajeVerdeDesde != null || plazo?.porcentajeAmarilloDesde != null || plazo?.porcentajeRojoDesde != null)
    setVerde(plazo?.porcentajeVerdeDesde ?? 50)
    setAmarillo(plazo?.porcentajeAmarilloDesde ?? 20)
    setRojo(plazo?.porcentajeRojoDesde ?? 0)
    setError(null)
  }, [plazo])

  async function guardar() {
    setError(null)
    if (!codigo.trim()) {
      setError('El código es obligatorio.')
      return
    }
    if (!nombre.trim()) {
      setError('El nombre es obligatorio.')
      return
    }
    if (!diasPlazo || diasPlazo <= 0) {
      setError('Los días de plazo deben ser mayores a 0.')
      return
    }
    if (umbralesActivos && !(verde > amarillo && amarillo > rojo)) {
      setError('Los umbrales deben cumplir: verde > amarillo > rojo.')
      return
    }
    setGuardando(true)
    try {
      const request = {
        idPlazoConfiguracion: plazo?.idPlazoConfiguracion ?? null,
        codigo: codigo.trim().toUpperCase(),
        nombre: nombre.trim(),
        ambito: ambito.trim() || null,
        diasPlazo,
        unidadPlazo,
        porcentajeVerdeDesde: umbralesActivos ? verde : null,
        porcentajeAmarilloDesde: umbralesActivos ? amarillo : null,
        porcentajeRojoDesde: umbralesActivos ? rojo : null,
        fechaVigenciaDesde: fechaVigenciaDesde || null,
        fechaVigenciaHasta: fechaVigenciaHasta || null,
        activo,
        observacion: observacion.trim() || null,
      }
      if (plazo?.idPlazoConfiguracion) {
        await editarPlazo(plazo.idPlazoConfiguracion, request)
      } else {
        await crearPlazo(request)
      }
      onGuardado()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo guardar el plazo.')
    } finally {
      setGuardando(false)
    }
  }

  return (
    <Dialog open onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{plazo ? 'Editar plazo' : 'Nuevo plazo'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Stack direction="row" spacing={2}>
            <TextField label="Código" fullWidth value={codigo} onChange={(e) => setCodigo(e.target.value)} />
            <TextField label="Ámbito" fullWidth value={ambito} onChange={(e) => setAmbito(e.target.value)} />
          </Stack>
          <TextField label="Nombre" fullWidth value={nombre} onChange={(e) => setNombre(e.target.value)} />
          <Stack direction="row" spacing={2}>
            <TextField
              label="Días de plazo"
              type="number"
              fullWidth
              value={diasPlazo}
              onChange={(e) => setDiasPlazo(Number(e.target.value))}
            />
            <TextField select label="Unidad" fullWidth value={unidadPlazo} onChange={(e) => setUnidadPlazo(e.target.value)}>
              {UNIDADES_PLAZO.map((u) => (
                <MenuItem key={u} value={u}>
                  {u === 'HABILES' ? 'Días hábiles' : 'Días calendario'}
                </MenuItem>
              ))}
            </TextField>
          </Stack>
          <Stack direction="row" spacing={2}>
            <TextField
              label="Vigencia desde"
              type="date"
              fullWidth
              value={fechaVigenciaDesde}
              onChange={(e) => setFechaVigenciaDesde(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <TextField
              label="Vigencia hasta"
              type="date"
              fullWidth
              value={fechaVigenciaHasta}
              onChange={(e) => setFechaVigenciaHasta(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </Stack>
          <TextField
            label="Observación"
            fullWidth
            multiline
            minRows={2}
            value={observacion}
            onChange={(e) => setObservacion(e.target.value)}
          />
          <FormControlLabel control={<Checkbox checked={activo} onChange={(e) => setActivo(e.target.checked)} />} label="Activo" />

          <Box sx={{ borderTop: '1px solid #E2E6EC', pt: 2 }}>
            <FormControlLabel
              control={<Checkbox checked={umbralesActivos} onChange={(e) => setUmbralesActivos(e.target.checked)} />}
              label={
                <Typography variant="subtitle2">
                  Umbrales de color (mejora V3 — no configurable en V2)
                </Typography>
              }
            />
            {umbralesActivos && (
              <Stack spacing={2} sx={{ mt: 1, px: 1 }}>
                <Typography variant="body2" color="text.secondary">
                  Porcentaje del plazo transcurrido a partir del cual el pill "Días" cambia de color. Debe cumplirse verde &gt; amarillo &gt; rojo.
                </Typography>
                <Box>
                  <Typography variant="caption">Verde desde: {verde}%</Typography>
                  <Slider value={verde} onChange={(_e, v) => setVerde(v as number)} min={0} max={100} size="small" sx={{ color: '#1F9254' }} />
                </Box>
                <Box>
                  <Typography variant="caption">Amarillo desde: {amarillo}%</Typography>
                  <Slider value={amarillo} onChange={(_e, v) => setAmarillo(v as number)} min={0} max={100} size="small" sx={{ color: '#B8860B' }} />
                </Box>
                <Box>
                  <Typography variant="caption">Rojo desde: {rojo}%</Typography>
                  <Slider value={rojo} onChange={(_e, v) => setRojo(v as number)} min={0} max={100} size="small" sx={{ color: '#C0392B' }} />
                </Box>
                <Stack direction="row" spacing={1} alignItems="center">
                  <Typography variant="body2" color="text.secondary">
                    Vista previa:
                  </Typography>
                  <StatusBadge label={`${Math.round((verde / 100) * diasPlazo)} días`} tone="success" />
                  <StatusBadge label={`${Math.round((amarillo / 100) * diasPlazo)} días`} tone="warning" />
                  <StatusBadge label={`${Math.round((rojo / 100) * diasPlazo)} días`} tone="error" />
                </Stack>
              </Stack>
            )}
          </Box>

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
