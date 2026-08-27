import { useState } from 'react'
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  Box,
  Checkbox,
  FormControlLabel,
  Grid,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import { SectionCard } from './RegistroExpedienteForm'
import { ValidatedTextField, type CampoValidator } from './ValidatedTextField'
import { sdrercColors } from '../theme'
import type { PermisoAdmin } from '../api/adminRolesApi'

function requerido(mensaje: string): CampoValidator {
  return (v) => (v.trim() ? null : mensaje)
}

function codigoValido(mensaje: string): CampoValidator {
  return (v) => (!v.trim() || /^[A-Z0-9_]+$/.test(v.trim()) ? null : mensaje)
}

export interface RolFormState {
  codigo: string
  nombre: string
  descripcion: string
  activo: boolean
}

export function rolFormVacio(): RolFormState {
  return { codigo: '', nombre: '', descripcion: '', activo: true }
}

interface RolAdminContentProps {
  seccion: 'datos' | 'permisos'
  esNuevo: boolean
  form: RolFormState
  permisos: PermisoAdmin[]
  permisosSeleccionados: number[]
  guardando: boolean
  guardandoPermisos: boolean
  mensaje: string | null
  error: string | null
  mensajePermisos: string | null
  errorPermisos: string | null
  onCampo: <K extends keyof RolFormState>(campo: K, valor: RolFormState[K]) => void
  onTogglePermiso: (idPermiso: number) => void
  onToggleModulo: (idsModulo: number[], marcarTodos: boolean) => void
  onGuardar: () => void
  onCancelar: () => void
  onGuardarPermisos: () => void
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

/**
 * Subpestañas "Datos" y "Permisos del rol" del workspace de Administración &gt; Roles. Mejora
 * explícita sobre V2 (`JPanelRolesV2`, "Permisos del rol" como tabla plana `codigo/modulo/nombre`
 * sin agrupar, ordenada por módulo): acá los permisos se agrupan en un `Accordion` por módulo, con
 * un checkbox "Marcar todos" en el header de cada acordeón — mismo dato del backend
 * (`PermisoAdminDAO.listarPermisosPorRol`, ya ordenado por módulo), solo cambia la presentación.
 *
 * Mismo requisito que V2: el rol debe guardarse primero (tener `idRol`) antes de poder asignarle
 * permisos — "Permisos del rol" muestra un aviso y deshabilita "Guardar permisos" mientras
 * `esNuevo` es verdadero, igual que `isCellEditable` en V2 (`PermisosTableModel`).
 */
export function RolAdminContent({
  seccion,
  esNuevo,
  form,
  permisos,
  permisosSeleccionados,
  guardando,
  guardandoPermisos,
  mensaje,
  error,
  mensajePermisos,
  errorPermisos,
  onCampo,
  onTogglePermiso,
  onToggleModulo,
  onGuardar,
  onCancelar,
  onGuardarPermisos,
}: RolAdminContentProps) {
  const [mostrarErrores, setMostrarErrores] = useState(false)

  if (seccion === 'datos') {
    return (
      <Stack spacing={2}>
        <SectionCard title="Datos del rol">
          <Grid container spacing={2}>
            <Grid size={6}>
              <ValidatedTextField
                label="Código"
                fullWidth
                value={form.codigo}
                onChange={(e) => onCampo('codigo', e.target.value.toUpperCase())}
                validate={(v) => requerido('Ingrese el código del rol.')(v) ?? codigoValido('Use mayúsculas, números y guion bajo, sin espacios.')(v)}
                forceTouched={mostrarErrores}
              />
              <Typography variant="caption" color="text.secondary">
                Ej. ABOGADO, SUPERVISION. Se normaliza a mayúsculas.
              </Typography>
            </Grid>
            <Grid size={6}>
              <FormControlLabel
                sx={{ mt: 1 }}
                control={<Switch checked={form.activo} onChange={(e) => onCampo('activo', e.target.checked)} />}
                label={form.activo ? 'Activo' : 'Inactivo'}
              />
            </Grid>
            <Grid size={12}>
              <ValidatedTextField
                label="Nombre"
                fullWidth
                value={form.nombre}
                onChange={(e) => onCampo('nombre', e.target.value)}
                validate={requerido('Ingrese el nombre del rol.')}
                forceTouched={mostrarErrores}
              />
            </Grid>
            <Grid size={12}>
              <TextField
                label="Descripción"
                fullWidth
                multiline
                minRows={3}
                value={form.descripcion}
                onChange={(e) => onCampo('descripcion', e.target.value)}
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
            {esNuevo ? 'Crear rol' : 'Guardar rol'}
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
    return (
      <Alert severity="info">Guarde los datos del rol antes de asignarle permisos.</Alert>
    )
  }

  const porModulo = new Map<string, PermisoAdmin[]>()
  for (const p of permisos) {
    const lista = porModulo.get(p.modulo) ?? []
    lista.push(p)
    porModulo.set(p.modulo, lista)
  }

  return (
    <Stack spacing={2}>
      {[...porModulo.entries()].map(([modulo, items]) => {
        const idsModulo = items.map((p) => p.idPermiso)
        const todosMarcados = idsModulo.every((id) => permisosSeleccionados.includes(id))
        const algunoMarcado = idsModulo.some((id) => permisosSeleccionados.includes(id))
        return (
          <Accordion key={modulo} defaultExpanded>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <FormControlLabel
                onClick={(e) => e.stopPropagation()}
                control={
                  <Checkbox
                    checked={todosMarcados}
                    indeterminate={!todosMarcados && algunoMarcado}
                    onChange={() => onToggleModulo(idsModulo, !todosMarcados)}
                  />
                }
                label={<Typography sx={{ fontWeight: 600 }}>{modulo || '(Sin módulo)'}</Typography>}
              />
            </AccordionSummary>
            <AccordionDetails>
              <Stack direction="row" flexWrap="wrap" gap={1}>
                {items.map((p) => (
                  <FormControlLabel
                    key={p.idPermiso}
                    control={<Checkbox checked={permisosSeleccionados.includes(p.idPermiso)} onChange={() => onTogglePermiso(p.idPermiso)} />}
                    label={p.nombre}
                    sx={{ minWidth: 260 }}
                  />
                ))}
              </Stack>
            </AccordionDetails>
          </Accordion>
        )
      })}
      <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap">
        <Box component="button" onClick={onGuardarPermisos} disabled={guardandoPermisos} sx={{ ...BOTON_SX, opacity: guardandoPermisos ? 0.7 : 1 }}>
          Guardar permisos
        </Box>
        {mensajePermisos && <Alert severity="success" sx={{ py: 0 }}>{mensajePermisos}</Alert>}
        {errorPermisos && <Alert severity="error" sx={{ py: 0 }}>{errorPermisos}</Alert>}
      </Stack>
    </Stack>
  )
}
