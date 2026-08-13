import { Alert, FormControl, FormControlLabel, Grid, MenuItem, Paper, Radio, RadioGroup, Stack, TextField, Typography } from '@mui/material'
import type {
  CatalogoItem,
  DatosActa,
  DatosPersonaRegistro,
  DatosSolicitud,
  UbigeoItem,
} from '../api/registroApi'
import { sdrercColors } from '../theme'

// Mismas listas fijas que V2 (JPanelRegistroManualRecepcionV2): no son catálogo de BD, están
// hardcodeadas también allá (crearTiposSolicitud/cmbPrioridad/crearTiposDocumentoTitular/
// crearTiposDocumentoRemitente).
const TIPOS_SOLICITUD = [
  { codigo: 'PARTE', nombre: 'Parte' },
  { codigo: 'OFICIO', nombre: 'Oficio' },
]
const PRIORIDADES = [
  { codigo: 'NORMAL', nombre: 'Normal' },
  { codigo: 'ALTA', nombre: 'Alta' },
  { codigo: 'URGENTE', nombre: 'Urgente' },
]
const TIPOS_DOC_TITULAR = ['DNI', 'CE', 'PASAPORTE']
const TIPOS_DOC_REMITENTE = ['DNI', 'CE', 'RUC', 'PASAPORTE']
const CORRESPONDE = 'Sí corresponde a la SDRERC'
const NO_CORRESPONDE = 'No corresponde a la SDRERC'

interface RegistroExpedienteFormProps {
  solicitud: DatosSolicitud
  acta: DatosActa
  titular: DatosPersonaRegistro
  remitente: DatosPersonaRegistro
  numeroExpedienteVistaPrevia: string | null
  posibleDuplicado: boolean
  motivoDuplicado: string | null
  numeroExpedienteSgdDuplicado: boolean
  motivoNumeroExpedienteSgdDuplicado: string | null
  updateSolicitud: <K extends keyof DatosSolicitud>(key: K, value: DatosSolicitud[K]) => void
  updateActa: <K extends keyof DatosActa>(key: K, value: DatosActa[K]) => void
  updateTitular: <K extends keyof DatosPersonaRegistro>(key: K, value: DatosPersonaRegistro[K]) => void
  updateRemitente: <K extends keyof DatosPersonaRegistro>(key: K, value: DatosPersonaRegistro[K]) => void
  canales: CatalogoItem[]
  procedimientos: CatalogoItem[]
  tiposDocumento: CatalogoItem[]
  tiposActa: CatalogoItem[]
  departamentos: UbigeoItem[]
  provincias: UbigeoItem[]
  distritos: UbigeoItem[]
  /** "N° expediente" del bloque "Datos del expediente": editable false siempre (V2: número nunca se toca aquí). */
  numeroExpedienteLabel: string
}

/**
 * Secciones 1–6 de JPanelRegistroManualRecepcionV2 (V2), compartidas entre Registro manual (alta)
 * y Edición manual — mismos campos, misma agrupación (V2 reutiliza literalmente el mismo panel
 * Swing en ambos modos). La sección 7 ("Resumen y confirmación") y los botones de acción quedan en
 * cada página, porque su texto/comportamiento sí difieren entre alta y edición.
 */
export function RegistroExpedienteForm({
  solicitud,
  acta,
  titular,
  remitente,
  posibleDuplicado,
  motivoDuplicado,
  numeroExpedienteSgdDuplicado,
  motivoNumeroExpedienteSgdDuplicado,
  updateSolicitud,
  updateActa,
  updateTitular,
  updateRemitente,
  canales,
  procedimientos,
  tiposDocumento,
  tiposActa,
  departamentos,
  provincias,
  distritos,
  numeroExpedienteLabel,
}: RegistroExpedienteFormProps) {
  return (
    <>
      {/* 1. Validación inicial */}
      <SectionCard title="Validación inicial">
        <FormControl>
          <RadioGroup row value={solicitud.validacionInicial ?? CORRESPONDE} onChange={(e) => updateSolicitud('validacionInicial', e.target.value)}>
            <FormControlLabel value={CORRESPONDE} control={<Radio />} label={CORRESPONDE} />
            <FormControlLabel value={NO_CORRESPONDE} control={<Radio />} label={NO_CORRESPONDE} />
          </RadioGroup>
        </FormControl>
        {solicitud.validacionInicial === NO_CORRESPONDE && (
          <TextField
            label="Hoja de envío"
            fullWidth
            sx={{ mt: 2 }}
            value={solicitud.hojaEnvio ?? ''}
            onChange={(e) => updateSolicitud('hojaEnvio', e.target.value)}
          />
        )}
      </SectionCard>

      {/* 2. Datos de solicitud */}
      <SectionCard title="Datos de solicitud">
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              label="Fecha recepción"
              type="date"
              required
              fullWidth
              slotProps={{ inputLabel: { shrink: true } }}
              value={solicitud.fechaRecepcion ?? ''}
              onChange={(e) => updateSolicitud('fechaRecepcion', e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <CatalogoSelect
              label="Canal de ingreso"
              items={canales}
              value={solicitud.canalCodigo}
              onChange={(codigo, nombre) => {
                updateSolicitud('canalCodigo', codigo)
                updateSolicitud('canalNombre', nombre)
              }}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              label="Nro. trámite web"
              fullWidth
              value={solicitud.numeroTramite ?? ''}
              onChange={(e) => updateSolicitud('numeroTramite', e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <CatalogoSelect
              label="Procedimiento registral"
              required
              items={procedimientos}
              value={solicitud.tipoProcedimientoCodigo}
              onChange={(codigo, nombre) => {
                updateSolicitud('tipoProcedimientoCodigo', codigo)
                updateSolicitud('tipoProcedimientoNombre', nombre)
              }}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <CatalogoSelect
              label="Tipo documento"
              required
              items={tiposDocumento}
              value={solicitud.tipoDocumentoCodigo}
              onChange={(codigo, nombre) => {
                updateSolicitud('tipoDocumentoCodigo', codigo)
                updateSolicitud('tipoDocumentoNombre', nombre)
              }}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              label="N° documento"
              fullWidth
              value={solicitud.numeroDocumento ?? ''}
              onChange={(e) => updateSolicitud('numeroDocumento', e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              select
              label="Tipo de solicitud"
              required
              fullWidth
              value={solicitud.tipoSolicitudCodigo ?? ''}
              onChange={(e) => {
                const item = TIPOS_SOLICITUD.find((t) => t.codigo === e.target.value)
                updateSolicitud('tipoSolicitudCodigo', item?.codigo ?? null)
                updateSolicitud('tipoSolicitudNombre', item?.nombre ?? null)
              }}
            >
              {TIPOS_SOLICITUD.map((t) => (
                <MenuItem key={t.codigo} value={t.codigo}>
                  {t.nombre}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              select
              label="Prioridad"
              fullWidth
              value={solicitud.prioridad ?? 'NORMAL'}
              onChange={(e) => updateSolicitud('prioridad', e.target.value)}
            >
              {PRIORIDADES.map((p) => (
                <MenuItem key={p.codigo} value={p.codigo}>
                  {p.nombre}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
        </Grid>
      </SectionCard>

      {/* 3. Datos del expediente + Datos del acta (misma fila que V2: crearDatosExpedienteYActa) */}
      <Grid container spacing={3}>
        <Grid size={{ xs: 12, sm: 6 }}>
          <SectionCard title="Datos del expediente">
            <Stack spacing={2}>
              <TextField label="N° expediente" fullWidth disabled value={numeroExpedienteLabel} />
              <TextField
                label="N° expediente SGD"
                required
                fullWidth
                value={solicitud.numeroExpedienteSgd ?? ''}
                onChange={(e) => updateSolicitud('numeroExpedienteSgd', e.target.value)}
                error={numeroExpedienteSgdDuplicado}
                helperText={motivoNumeroExpedienteSgdDuplicado ?? undefined}
              />
            </Stack>
          </SectionCard>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <SectionCard title="Datos del acta">
            {posibleDuplicado && (
              <Alert severity="warning" sx={{ mb: 2 }}>
                {motivoDuplicado}
              </Alert>
            )}
            <Stack spacing={2}>
              <CatalogoSelect
                label="Tipo de acta"
                items={tiposActa}
                value={acta.tipoActaCodigo}
                onChange={(codigo, nombre) => {
                  updateActa('tipoActaCodigo', codigo)
                  updateActa('tipoActaNombre', nombre)
                }}
              />
              <TextField
                label="Nro. acta"
                required
                fullWidth
                value={acta.numeroActa ?? ''}
                onChange={(e) => updateActa('numeroActa', e.target.value)}
              />
            </Stack>
          </SectionCard>
        </Grid>
      </Grid>

      {/* 4. Titular */}
      <SectionCard title="Titular">
        <Grid container spacing={2}>
          <Grid size={12}>
            <TextField
              label="Nombres"
              required
              fullWidth
              value={titular.nombreCompleto ?? ''}
              onChange={(e) => updateTitular('nombreCompleto', e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              select
              label="Tipo documento"
              fullWidth
              value={titular.tipoDocumento ?? ''}
              onChange={(e) => updateTitular('tipoDocumento', e.target.value || null)}
            >
              <MenuItem value="">No definido</MenuItem>
              {TIPOS_DOC_TITULAR.map((t) => (
                <MenuItem key={t} value={t}>
                  {t}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              label="Número documento"
              fullWidth
              value={titular.numeroDocumento ?? ''}
              onChange={(e) => updateTitular('numeroDocumento', e.target.value)}
            />
          </Grid>
        </Grid>
      </SectionCard>

      {/* 5. Solicitante */}
      <SectionCard title="Solicitante">
        <Grid container spacing={2}>
          <Grid size={12}>
            <TextField
              label="Nombres / Razón Social"
              required
              fullWidth
              value={remitente.nombreCompleto ?? ''}
              onChange={(e) => updateRemitente('nombreCompleto', e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              select
              label="Tipo documento"
              fullWidth
              value={remitente.tipoDocumento ?? ''}
              onChange={(e) => updateRemitente('tipoDocumento', e.target.value || null)}
            >
              <MenuItem value="">No definido</MenuItem>
              {TIPOS_DOC_REMITENTE.map((t) => (
                <MenuItem key={t} value={t}>
                  {t}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              label="Número documento"
              fullWidth
              value={remitente.numeroDocumento ?? ''}
              onChange={(e) => updateRemitente('numeroDocumento', e.target.value)}
            />
          </Grid>
        </Grid>
      </SectionCard>

      {/* 6. Datos de notificación y ubicación (en V2 llena los campos de contacto/ubigeo del
          Solicitante, no del titular — remitente.setCorreo/setTelefono/setDireccion/ubigeo) */}
      <SectionCard title="Datos de notificación y ubicación">
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField label="Correo" fullWidth value={remitente.correo ?? ''} onChange={(e) => updateRemitente('correo', e.target.value)} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField label="Teléfono" fullWidth value={remitente.telefono ?? ''} onChange={(e) => updateRemitente('telefono', e.target.value)} />
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <TextField
              select
              label="Departamento"
              fullWidth
              value={remitente.idDepartamento ?? ''}
              onChange={(e) => {
                const item = departamentos.find((d) => d.id === Number(e.target.value))
                updateRemitente('idDepartamento', item?.id ?? null)
                updateRemitente('departamento', item?.nombre ?? null)
                updateRemitente('idProvincia', null)
                updateRemitente('provincia', null)
                updateRemitente('idDistrito', null)
                updateRemitente('distrito', null)
              }}
            >
              <MenuItem value="">Seleccione</MenuItem>
              {departamentos.map((d) => (
                <MenuItem key={d.id} value={d.id}>
                  {d.nombre}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <TextField
              select
              label="Provincia"
              fullWidth
              disabled={!remitente.idDepartamento}
              value={remitente.idProvincia ?? ''}
              onChange={(e) => {
                const item = provincias.find((p) => p.id === Number(e.target.value))
                updateRemitente('idProvincia', item?.id ?? null)
                updateRemitente('provincia', item?.nombre ?? null)
                updateRemitente('idDistrito', null)
                updateRemitente('distrito', null)
              }}
            >
              <MenuItem value="">Seleccione</MenuItem>
              {provincias.map((p) => (
                <MenuItem key={p.id} value={p.id}>
                  {p.nombre}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={{ xs: 12, sm: 4 }}>
            <TextField
              select
              label="Distrito"
              fullWidth
              disabled={!remitente.idProvincia}
              value={remitente.idDistrito ?? ''}
              onChange={(e) => {
                const item = distritos.find((d) => d.id === Number(e.target.value))
                updateRemitente('idDistrito', item?.id ?? null)
                updateRemitente('distrito', item?.nombre ?? null)
              }}
            >
              <MenuItem value="">Seleccione</MenuItem>
              {distritos.map((d) => (
                <MenuItem key={d.id} value={d.id}>
                  {d.nombre}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid size={12}>
            <TextField label="Dirección" fullWidth value={remitente.direccion ?? ''} onChange={(e) => updateRemitente('direccion', e.target.value)} />
          </Grid>
        </Grid>
      </SectionCard>
    </>
  )
}

export function SectionCard({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <Paper elevation={1} sx={{ p: 3, borderRadius: 2 }}>
      <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2, color: sdrercColors.textPrimary }}>
        {title}
      </Typography>
      {children}
    </Paper>
  )
}

function CatalogoSelect({
  label,
  items,
  value,
  onChange,
  required,
}: {
  label: string
  items: CatalogoItem[]
  value: string | null
  onChange: (codigo: string | null, nombre: string | null) => void
  required?: boolean
}) {
  return (
    <TextField
      select
      label={label}
      required={required}
      fullWidth
      value={value ?? ''}
      onChange={(e) => {
        const item = items.find((i) => i.codigo === e.target.value)
        onChange(item?.codigo ?? null, item?.nombre ?? null)
      }}
    >
      {items.map((item) => (
        <MenuItem key={item.codigo} value={item.codigo ?? ''}>
          {item.nombre}
        </MenuItem>
      ))}
    </TextField>
  )
}
