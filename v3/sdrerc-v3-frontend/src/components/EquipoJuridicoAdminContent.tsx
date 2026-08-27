import { useState } from 'react'
import { Alert, Box, FormControlLabel, Grid, MenuItem, Stack, Switch, TextField, Tooltip, Typography } from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import StarIcon from '@mui/icons-material/Star'
import StarBorderIcon from '@mui/icons-material/StarBorder'
import { SectionCard } from './RegistroExpedienteForm'
import { StatusBadge } from './StatusBadge'
import { ValidatedTextField, type CampoValidator } from './ValidatedTextField'
import { sdrercColors } from '../theme'
import type { AreaAdmin, EquipoMiembroAdmin, UsuarioAsignableEquipo } from '../api/adminEquipoJuridicoApi'

function requerido(mensaje: string): CampoValidator {
  return (v) => (v.trim() ? null : mensaje)
}

function codigoValido(mensaje: string): CampoValidator {
  return (v) => (!v.trim() || /^[A-Z0-9_]+$/.test(v.trim()) ? null : mensaje)
}

export interface EquipoFormState {
  idArea: number | ''
  codigo: string
  nombre: string
  descripcion: string
  activo: boolean
  idResponsable: number | ''
}

export function equipoFormVacio(): EquipoFormState {
  return { idArea: '', codigo: '', nombre: '', descripcion: '', activo: true, idResponsable: '' }
}

const BOTON_SX = {
  px: 2.5,
  py: 1,
  borderRadius: 1.5,
  border: 'none',
  bgcolor: sdrercColors.primary,
  color: '#fff',
  fontWeight: 600,
  cursor: 'pointer',
} as const

interface EquipoJuridicoAdminContentProps {
  seccion: 'datos' | 'miembros'
  esNuevo: boolean
  form: EquipoFormState
  areas: AreaAdmin[]
  supervisoresAsignables: UsuarioAsignableEquipo[]
  miembros: EquipoMiembroAdmin[]
  usuariosAsignables: UsuarioAsignableEquipo[]
  usuarioParaAgregar: number | ''
  guardando: boolean
  mensaje: string | null
  error: string | null
  errorMiembros: string | null
  onCampo: <K extends keyof EquipoFormState>(campo: K, valor: EquipoFormState[K]) => void
  onGuardar: () => void
  onCancelar: () => void
  onUsuarioParaAgregarChange: (idUsuario: number | '') => void
  onAgregarMiembro: () => void
  onQuitarMiembro: (idUsuario: number, nombre: string) => void
  onMarcarResponsable: (idUsuario: number) => void
}

/**
 * Subpestañas "Datos" y "Miembros" del workspace de Administración &gt; Equipo Jurídico. Mismo
 * patrón de multi-select por Chip que Usuarios/Roles no aplica acá — "Miembros" es una grilla
 * (mismo criterio visual que V2, `MiembrosTableModel`) con acción inline por fila (quitar/marcar
 * responsable), no un multi-select de una sola vez.
 */
export function EquipoJuridicoAdminContent({
  seccion,
  esNuevo,
  form,
  areas,
  supervisoresAsignables,
  miembros,
  usuariosAsignables,
  usuarioParaAgregar,
  guardando,
  mensaje,
  error,
  errorMiembros,
  onCampo,
  onGuardar,
  onCancelar,
  onUsuarioParaAgregarChange,
  onAgregarMiembro,
  onQuitarMiembro,
  onMarcarResponsable,
}: EquipoJuridicoAdminContentProps) {
  const [mostrarErrores, setMostrarErrores] = useState(false)

  if (seccion === 'datos') {
    return (
      <Stack spacing={2}>
        <SectionCard title="Datos del equipo">
          <Grid container spacing={2}>
            <Grid size={6}>
              <ValidatedTextField
                label="Código"
                fullWidth
                value={form.codigo}
                onChange={(e) => onCampo('codigo', e.target.value.toUpperCase())}
                validate={(v) => requerido('Ingrese el código del equipo.')(v) ?? codigoValido('Use mayúsculas, números y guion bajo, sin espacios.')(v)}
                forceTouched={mostrarErrores}
              />
            </Grid>
            <Grid size={6}>
              <TextField
                select
                label="Área"
                fullWidth
                value={form.idArea === '' ? '' : String(form.idArea)}
                onChange={(e) => onCampo('idArea', e.target.value === '' ? '' : Number(e.target.value))}
              >
                <MenuItem value="">(Sin área)</MenuItem>
                {areas.map((a) => (
                  <MenuItem key={a.idArea} value={String(a.idArea)}>
                    {a.nombre}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid size={12}>
              <ValidatedTextField
                label="Nombre"
                fullWidth
                value={form.nombre}
                onChange={(e) => onCampo('nombre', e.target.value)}
                validate={requerido('Ingrese el nombre del equipo.')}
                forceTouched={mostrarErrores}
              />
            </Grid>
            <Grid size={12}>
              <TextField
                label="Descripción"
                fullWidth
                multiline
                minRows={2}
                value={form.descripcion}
                onChange={(e) => onCampo('descripcion', e.target.value)}
              />
            </Grid>
            <Grid size={6}>
              <TextField
                select
                label="Responsable del equipo"
                fullWidth
                value={form.idResponsable === '' ? '' : String(form.idResponsable)}
                onChange={(e) => onCampo('idResponsable', e.target.value === '' ? '' : Number(e.target.value))}
                helperText="Solo usuarios con rol de supervisión."
              >
                <MenuItem value="">(Sin responsable)</MenuItem>
                {supervisoresAsignables.map((u) => (
                  <MenuItem key={u.idUsuario} value={String(u.idUsuario)}>
                    {u.nombreCompleto || u.username}
                  </MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid size={6}>
              <FormControlLabel
                sx={{ mt: 1 }}
                control={<Switch checked={form.activo} onChange={(e) => onCampo('activo', e.target.checked)} />}
                label={form.activo ? 'Activo' : 'Inactivo'}
              />
            </Grid>
          </Grid>
        </SectionCard>
        <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap">
          <Box
            component="button"
            onClick={() => {
              setMostrarErrores(true)
              onGuardar()
            }}
            disabled={guardando}
            sx={{ ...BOTON_SX, opacity: guardando ? 0.7 : 1 }}
          >
            {esNuevo ? 'Crear equipo' : 'Guardar equipo'}
          </Box>
          <Box
            component="button"
            onClick={onCancelar}
            sx={{ px: 2.5, py: 1, borderRadius: 1.5, border: `1px solid ${sdrercColors.divider}`, bgcolor: 'transparent', cursor: 'pointer' }}
          >
            Cancelar
          </Box>
          {mensaje && <Alert severity="success" sx={{ py: 0 }}>{mensaje}</Alert>}
          {error && <Alert severity="error" sx={{ py: 0 }}>{error}</Alert>}
        </Stack>
      </Stack>
    )
  }

  if (esNuevo) {
    return <Alert severity="info">Guarde los datos del equipo antes de administrar sus miembros.</Alert>
  }

  return (
    <Stack spacing={2}>
      <SectionCard title="Agregar miembro">
        <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap">
          <TextField
            select
            label="Usuario"
            size="small"
            sx={{ minWidth: 260 }}
            value={usuarioParaAgregar === '' ? '' : String(usuarioParaAgregar)}
            onChange={(e) => onUsuarioParaAgregarChange(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <MenuItem value="">Seleccione un usuario</MenuItem>
            {usuariosAsignables.map((u) => (
              <MenuItem key={u.idUsuario} value={String(u.idUsuario)}>
                {u.nombreCompleto || u.username} {u.rolesResumen ? `(${u.rolesResumen})` : ''}
              </MenuItem>
            ))}
          </TextField>
          <Box component="button" onClick={onAgregarMiembro} disabled={usuarioParaAgregar === ''} sx={{ ...BOTON_SX, opacity: usuarioParaAgregar === '' ? 0.5 : 1 }}>
            Agregar miembro
          </Box>
        </Stack>
      </SectionCard>
      <SectionCard title="Personal del equipo">
        {errorMiembros && <Alert severity="error" sx={{ mb: 2 }}>{errorMiembros}</Alert>}
        {miembros.length === 0 && (
          <Typography variant="body2" color="text.secondary">
            Sin miembros registrados.
          </Typography>
        )}
        <Stack spacing={1}>
          {miembros.map((m) => (
            <Stack
              key={m.idEquipoUsuario}
              direction="row"
              spacing={2}
              alignItems="center"
              sx={{
                p: 1.5,
                borderRadius: 1.5,
                bgcolor: m.relacionActiva ? sdrercColors.backgroundPaper : sdrercColors.softGray,
                opacity: m.relacionActiva ? 1 : 0.6,
                border: `1px solid ${sdrercColors.divider}`,
              }}
            >
              <Tooltip title={m.responsable ? 'Responsable del equipo' : 'Marcar como responsable'}>
                <Box
                  component="button"
                  onClick={() => m.relacionActiva && m.supervisor && onMarcarResponsable(m.idUsuario)}
                  disabled={!m.relacionActiva || !m.supervisor}
                  sx={{ border: 'none', bgcolor: 'transparent', cursor: m.relacionActiva && m.supervisor ? 'pointer' : 'default', display: 'flex' }}
                >
                  {m.responsable ? <StarIcon fontSize="small" sx={{ color: '#B8860B' }} /> : <StarBorderIcon fontSize="small" sx={{ color: sdrercColors.textSecondary }} />}
                </Box>
              </Tooltip>
              <Box sx={{ flex: 1 }}>
                <Typography sx={{ fontWeight: 600 }}>{m.nombreCompleto || m.username}</Typography>
                <Typography variant="caption" color="text.secondary">
                  {m.username} {m.rolesResumen ? `· ${m.rolesResumen}` : ''}
                </Typography>
              </Box>
              <StatusBadge label={m.relacionActiva ? 'Activo' : 'Inactivo'} tone={m.relacionActiva ? 'success' : 'neutral'} />
              {m.relacionActiva && (
                <Tooltip title="Quitar del equipo">
                  <Box component="button" onClick={() => onQuitarMiembro(m.idUsuario, m.nombreCompleto || m.username)} sx={{ border: 'none', bgcolor: 'transparent', cursor: 'pointer', display: 'flex' }}>
                    <CloseIcon fontSize="small" sx={{ color: sdrercColors.error }} />
                  </Box>
                </Tooltip>
              )}
            </Stack>
          ))}
        </Stack>
      </SectionCard>
    </Stack>
  )
}
