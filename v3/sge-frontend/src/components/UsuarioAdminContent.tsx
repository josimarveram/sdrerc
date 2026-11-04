import { useState } from 'react'
import {
  Alert,
  Box,
  Checkbox,
  Chip,
  FormControlLabel,
  Grid,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material'
import { SectionCard } from './RegistroExpedienteForm'
import { ValidatedTextField, type CampoValidator } from './ValidatedTextField'
import { sdrercColors } from '../theme'
import type { EquipoAsignacion, RolResumen } from '../api/adminUsuariosApi'

function requerido(mensaje: string): CampoValidator {
  return (v) => (v.trim() ? null : mensaje)
}

export interface UsuarioFormState {
  username: string
  nombreCompleto: string
  tipoDocumento: string
  numeroDocumento: string
  correo: string
  telefono: string
  activo: boolean
  idsRoles: number[]
  idsEquipos: number[]
}

export function usuarioFormVacio(): UsuarioFormState {
  return {
    username: '',
    nombreCompleto: '',
    tipoDocumento: '',
    numeroDocumento: '',
    correo: '',
    telefono: '',
    activo: true,
    idsRoles: [],
    idsEquipos: [],
  }
}

const TIPOS_DOCUMENTO = ['', 'DNI', 'CE', 'PASAPORTE', 'SIN DNI']

interface UsuarioAdminContentProps {
  seccion: 'datos' | 'roles'
  form: UsuarioFormState
  esNuevo: boolean
  roles: RolResumen[]
  equipos: EquipoAsignacion[]
  guardando: boolean
  mensaje: string | null
  error: string | null
  onCampo: <K extends keyof UsuarioFormState>(campo: K, valor: UsuarioFormState[K]) => void
  onToggleRol: (idRol: number) => void
  onToggleEquipo: (idEquipo: number) => void
  onGuardar: () => void
  onCancelar: () => void
}

/**
 * Subpestañas "Datos" y "Roles y equipos" del workspace de Administración &gt; Usuarios. Un solo
 * componente parametrizado por `seccion` (no 2 componentes separados) porque ambas vistas editan
 * el MISMO formulario y se guardan con la MISMA acción — mismo criterio que otros módulos donde
 * "Guardar" vive repetido en cada subpestaña para que el usuario no tenga que volver a "Datos"
 * para confirmar.
 *
 * Multi-equipo (checkboxes, no combo único): corrige a propósito la inconsistencia real de V2
 * (el formulario de Usuarios solo permitía 1 equipo activo, mientras Equipo Jurídico sí permitía
 * varios) — decisión ya confirmada explícitamente por el usuario, ver v3/CLAUDE.md.
 */
export function UsuarioAdminContent({
  seccion,
  form,
  esNuevo,
  roles,
  equipos,
  guardando,
  mensaje,
  error,
  onCampo,
  onToggleRol,
  onToggleEquipo,
  onGuardar,
  onCancelar,
}: UsuarioAdminContentProps) {
  const [mostrarErrores, setMostrarErrores] = useState(false)

  function guardar() {
    setMostrarErrores(true)
    onGuardar()
  }

  const footer = (
    <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap">
      <Box
        component="button"
        onClick={guardar}
        disabled={guardando}
        sx={{
          px: 2.5,
          py: 1,
          borderRadius: 1.5,
          border: 'none',
          bgcolor: sdrercColors.primary,
          color: '#fff',
          fontWeight: 600,
          cursor: guardando ? 'default' : 'pointer',
          opacity: guardando ? 0.7 : 1,
        }}
      >
        {esNuevo ? 'Crear usuario' : 'Guardar usuario'}
      </Box>
      <Box
        component="button"
        onClick={onCancelar}
        sx={{
          px: 2.5,
          py: 1,
          borderRadius: 1.5,
          border: `1px solid ${sdrercColors.divider}`,
          bgcolor: 'transparent',
          cursor: 'pointer',
        }}
      >
        Cancelar
      </Box>
      {mensaje && (
        <Alert severity="success" sx={{ py: 0 }}>
          {mensaje}
        </Alert>
      )}
      {error && (
        <Alert severity="error" sx={{ py: 0 }}>
          {error}
        </Alert>
      )}
    </Stack>
  )

  if (seccion === 'datos') {
    return (
      <Stack spacing={2}>
        <SectionCard title="Datos del usuario">
          <Grid container spacing={2}>
            <Grid size={6}>
              <ValidatedTextField
                label="Usuario (login)"
                fullWidth
                value={form.username}
                onChange={(e) => onCampo('username', e.target.value.toUpperCase())}
                validate={requerido('Ingrese el usuario.')}
                forceTouched={mostrarErrores}
              />
            </Grid>
            <Grid size={6}>
              <ValidatedTextField
                label="Nombre completo"
                fullWidth
                value={form.nombreCompleto}
                onChange={(e) => onCampo('nombreCompleto', e.target.value)}
                validate={requerido('Ingrese el nombre completo.')}
                forceTouched={mostrarErrores}
              />
            </Grid>
            <Grid size={4}>
              <TextField
                label="Tipo de documento"
                select
                fullWidth
                value={form.tipoDocumento}
                onChange={(e) => onCampo('tipoDocumento', e.target.value)}
                slotProps={{ select: { native: true } }}
              >
                {TIPOS_DOCUMENTO.map((t) => (
                  <option key={t} value={t}>
                    {t || '(sin definir)'}
                  </option>
                ))}
              </TextField>
            </Grid>
            <Grid size={4}>
              <TextField
                label="N° de documento"
                fullWidth
                value={form.numeroDocumento}
                onChange={(e) => onCampo('numeroDocumento', e.target.value)}
              />
            </Grid>
            <Grid size={4}>
              <FormControlLabel
                sx={{ mt: 1 }}
                control={<Switch checked={form.activo} onChange={(e) => onCampo('activo', e.target.checked)} />}
                label={form.activo ? 'Activo' : 'Inactivo'}
              />
            </Grid>
            <Grid size={6}>
              <ValidatedTextField
                label="Correo"
                fullWidth
                value={form.correo}
                onChange={(e) => onCampo('correo', e.target.value)}
              />
            </Grid>
            <Grid size={6}>
              <TextField
                label="Teléfono"
                fullWidth
                value={form.telefono}
                onChange={(e) => onCampo('telefono', e.target.value)}
              />
            </Grid>
          </Grid>
        </SectionCard>
        {footer}
      </Stack>
    )
  }

  return (
    <Stack spacing={2}>
      <SectionCard title="Roles">
        {roles.length === 0 && (
          <Typography variant="body2" color="text.secondary">
            No hay roles activos.
          </Typography>
        )}
        <Stack direction="row" flexWrap="wrap" gap={1}>
          {roles.map((r) => {
            const marcado = form.idsRoles.includes(r.idRol)
            return (
              <Chip
                key={r.idRol}
                label={r.nombre}
                clickable
                onClick={() => onToggleRol(r.idRol)}
                icon={<Checkbox checked={marcado} size="small" sx={{ p: 0, ml: 1 }} />}
                sx={{
                  bgcolor: marcado ? sdrercColors.softBlue : sdrercColors.softGray,
                  fontWeight: marcado ? 600 : 400,
                }}
              />
            )
          })}
        </Stack>
        {mostrarErrores && form.idsRoles.length === 0 && (
          <Typography variant="caption" color="error" sx={{ display: 'block', mt: 1 }}>
            Seleccione al menos un rol.
          </Typography>
        )}
      </SectionCard>
      <SectionCard title="Equipos">
        {equipos.length === 0 && (
          <Typography variant="body2" color="text.secondary">
            No hay equipos activos.
          </Typography>
        )}
        <Stack direction="row" flexWrap="wrap" gap={1}>
          {equipos.map((eq) => {
            const marcado = form.idsEquipos.includes(eq.idEquipo)
            return (
              <Chip
                key={eq.idEquipo}
                label={eq.displayName}
                clickable
                onClick={() => onToggleEquipo(eq.idEquipo)}
                icon={<Checkbox checked={marcado} size="small" sx={{ p: 0, ml: 1 }} />}
                sx={{
                  bgcolor: marcado ? sdrercColors.softBlue : sdrercColors.softGray,
                  fontWeight: marcado ? 600 : 400,
                }}
              />
            )
          })}
        </Stack>
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
          Un usuario puede pertenecer a varios equipos a la vez.
        </Typography>
      </SectionCard>
      {footer}
    </Stack>
  )
}
