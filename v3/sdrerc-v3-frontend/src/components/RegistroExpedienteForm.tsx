import { useEffect } from 'react'
import { alpha, Alert, Autocomplete, Box, FormControl, FormControlLabel, Grid, MenuItem, Paper, Radio, RadioGroup, Stack, TextField, Typography } from '@mui/material'
import type {
  CatalogoItem,
  CentroAtencion,
  DatosActa,
  DatosPersonaRegistro,
  DatosSolicitud,
  Municipalidad,
  UbigeoItem,
  UnidadOrganica,
} from '../api/registroApi'
import { sdrercColors } from '../theme'
import { ValidatedTextField, type CampoValidator } from './ValidatedTextField'

// Mismas listas fijas que V2 (JPanelRegistroManualRecepcionV2): no son catálogo de BD, están
// hardcodeadas también allá (crearTiposSolicitud/cmbPrioridad/crearTiposDocumentoTitular/
// crearTiposDocumentoRemitente).
const TIPOS_SOLICITUD = [
  { codigo: 'PARTE', nombre: 'Parte' },
  { codigo: 'OFICIO', nombre: 'Oficio' },
]
// 31/08/2026, pedido explícito del usuario: opciones cambiadas de Normal/Alta/Urgente (réplica de
// V2) a Normal/Urgente/Muy urgente — divergencia deliberada de V2 en este campo puntual. Exportado
// para que `PanelDatosExpediente.tsx` (solo lectura) traduzca el código guardado en
// `EXPEDIENTE.PRIORIDAD` al mismo nombre visible, sin duplicar la lista.
export const PRIORIDADES = [
  { codigo: 'NORMAL', nombre: 'Normal' },
  { codigo: 'URGENTE', nombre: 'Urgente' },
  { codigo: 'MUY_URGENTE', nombre: 'Muy urgente' },
]

export function nombrePrioridad(codigo: string | null | undefined): string | null {
  if (!codigo) return null
  return PRIORIDADES.find((p) => p.codigo === codigo)?.nombre ?? codigo
}
const TIPOS_DOC_TITULAR = ['DNI', 'CE', 'PASAPORTE']
const TIPOS_DOC_REMITENTE = ['DNI', 'CE', 'RUC', 'PASAPORTE']
const CORRESPONDE = 'Sí corresponde a la SDRERC'
const NO_CORRESPONDE = 'No corresponde a la SDRERC'

// Réplica client-side de las mismas reglas de RegistroManualValidacionService (V3 backend, port
// literal de V2) — mismos patrones/mensajes EXACTOS, para que (a) la validación inline (verde/rojo
// al salir del campo) coincida con lo que el servidor validaría, y (b) `filtrarMensajesFormulario`
// pueda reconocer por texto exacto qué mensajes del backend ya se muestran inline y no duplicarlos
// en el bloque inferior (13/08/2026, pedido explícito del usuario).
const DOCUMENT_PATTERN = /^[A-Za-z0-9-]{4,30}$/
const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/
const PHONE_PATTERN = /^[0-9+()\-\s]{6,30}$/

function requerido(mensaje: string): CampoValidator {
  return (v) => (v.trim() ? null : mensaje)
}

/** Sin tildes, mayúsculas, recortado — para comparar texto de búsqueda insensible a acentos. */
const DIACRITICOS_PATTERN = new RegExp('[' + String.fromCharCode(0x0300) + '-' + String.fromCharCode(0x036f) + ']', 'g')

function normalizarBusqueda(valor: string): string {
  return valor.normalize('NFD').replace(DIACRITICOS_PATTERN, '').toUpperCase().trim()
}

// Desambiguación de municipalidades homónimas (18/08/2026, pedido explícito del usuario): la línea
// secundaria del selector siempre muestra Departamento / Provincia / Distrito; cuando el tipo es
// "Centro Poblado" o "Comunidad Nativa", se agrega "— C.P. {centro_poblado}" al final, porque ese
// campo es lo que distingue homónimos dentro del mismo distrito (ver v3/CLAUDE.md).
function esMunicipalidadDeCentroPoblado(tipo: string | null): boolean {
  return tipo === 'MUNICIPALIDAD DE CENTRO POBLADO' || tipo === 'COMUNIDAD NATIVA'
}

function lineaUbicacionMunicipalidad(m: Municipalidad): string {
  const base = [m.departamento, m.provincia, m.distrito].filter(Boolean).join(' / ')
  if (esMunicipalidadDeCentroPoblado(m.tipo) && m.centroPoblado) {
    return base ? `${base} — C.P. ${m.centroPoblado}` : `C.P. ${m.centroPoblado}`
  }
  return base
}

/** Par etiqueta/valor de solo lectura, usado en la tarjeta de confirmación de Centro de Atención. */
function CampoSoloLectura({ label, value }: { label: string; value: string | null }) {
  return (
    <div>
      <Typography variant="caption" color="text.secondary" component="div">
        {label}
      </Typography>
      <Typography variant="body2">{value && value.trim() !== '' ? value : '-'}</Typography>
    </div>
  )
}

function documentoValido(etiqueta: string): CampoValidator {
  const mensaje = `Documento de ${etiqueta} inválido.`
  return (v) => (!v.trim() || DOCUMENT_PATTERN.test(v.trim()) ? null : mensaje)
}

function correoValido(mensaje: string): CampoValidator {
  return (v) => (!v.trim() || EMAIL_PATTERN.test(v.trim()) ? null : mensaje)
}

function telefonoValido(mensaje: string): CampoValidator {
  return (v) => (!v.trim() || PHONE_PATTERN.test(v.trim()) ? null : mensaje)
}

function fechaRecepcionValida(v: string): string | null {
  if (!v.trim()) return 'Fecha de recepción obligatoria o inválida.'
  const fecha = new Date(`${v}T00:00:00`)
  if (Number.isNaN(fecha.getTime())) return 'Fecha de recepción obligatoria o inválida.'
  const hoy = new Date()
  hoy.setHours(23, 59, 59, 999)
  return fecha.getTime() > hoy.getTime() ? 'La fecha de recepción no puede ser futura.' : null
}

// Mismos mensajes literales que emite el backend para estos campos (ver arriba) — si el usuario ya
// los ve inline junto al campo (ValidatedTextField), no tiene sentido repetirlos en el bloque
// "Errores / advertencias" de más abajo.
const MENSAJES_CAMPO_INLINE = new Set([
  'Hoja de envío obligatoria cuando no corresponde a la SDRERC.',
  'Canal de ingreso obligatorio.',
  'Tipo de solicitud obligatorio.',
  'Fecha de recepción obligatoria o inválida.',
  'La fecha de recepción no puede ser futura.',
  'Tipo de procedimiento obligatorio.',
  'Tipo de documento obligatorio.',
  'N° expediente SGD obligatorio.',
  'Número de acta obligatorio.',
  'Tipo de acta obligatorio.',
  'Titular obligatorio.',
  'Solicitante obligatorio.',
  'Documento de titular inválido.',
  'Documento de solicitante inválido.',
  'Correo de solicitante inválido.',
  'Teléfono de solicitante inválido.',
  // Solicitante con 3 (ahora 4) tipos (14/08/2026, ver v3/CLAUDE.md): ya se muestran en rojo directo
  // sobre el Autocomplete/select correspondiente (ver sección "5. Solicitante" más abajo).
  'Seleccione un centro de atención.',
  'Seleccione una unidad orgánica.',
  'Seleccione una municipalidad.',
])

/**
 * Filtra del bloque inferior "Errores / advertencias" los mensajes que ya se muestran inline junto
 * a su campo — deja solo mensajes de nivel de FORMULARIO: duplicados (acta+titular, N° expediente
 * SGD contra la base — estos SÍ dependen de una consulta al servidor, no de una regla local),
 * posible grupo familiar, "requiere decisión de asignación", errores de conexión, etc. (13/08/2026,
 * pedido explícito del usuario: "MANTÉN el bloque inferior... pero redúcelo a mensajes de nivel de
 * FORMULARIO... no deben duplicarse abajo"). Comparación por texto EXACTO — si el backend cambia el
 * texto de alguno de estos mensajes, hay que actualizar `MENSAJES_CAMPO_INLINE` también.
 */
export function filtrarMensajesFormulario(mensajes: string[]): string[] {
  return mensajes.filter((m) => !MENSAJES_CAMPO_INLINE.has(m.trim()))
}

/**
 * `true` si algún mensaje es de campo obligatorio/formato (`MENSAJES_CAMPO_INLINE`) — usado para
 * BLOQUEAR el registro/guardado (18/08/2026, pedido explícito del usuario: "al hacer clic en el
 * botón Registrar no debería permitir grabar" si faltan campos obligatorios). Deliberadamente NO
 * bloquea por mensajes de nivel de formulario (posible duplicado, posible grupo familiar, "requiere
 * decisión de asignación") — esos son advertencias que V2 permite registrar igual, quedando el
 * expediente marcado para revisión/asociación posterior; solo lo obligatorio/formato debe impedir
 * el envío.
 */
export function tieneErroresDeCampo(mensajes: string[]): boolean {
  return mensajes.some((m) => MENSAJES_CAMPO_INLINE.has(m.trim()))
}

/**
 * Texto + resaltado para el cuadro "Errores / advertencias del formulario" de la sección "Resumen y
 * confirmación" (18/08/2026, pedido explícito del usuario). Antes, cuando TODOS los mensajes eran de
 * campo (ya visibles en rojo junto a cada campo, sin ninguno de nivel de formulario), el cuadro
 * mostraba "Sin advertencias de formulario (valide primero)." — confuso: el usuario podía tener
 * varios campos obligatorios vacíos, resaltados en rojo pero fuera de la vista (formulario largo,
 * scrolleado hacia abajo), y este cuadro parecía decir que todo estaba bien. Ahora, en ese caso
 * específico, muestra un aviso genérico que no repite nombres de columna (para no duplicar lo que ya
 * se ve inline) pero sí orienta a la acción: desplazarse hacia arriba y revisar los campos en rojo.
 */
export function resumenMensajesFormulario(mensajes: string[]): { texto: string; resaltado: boolean } {
  const mensajesFormulario = filtrarMensajesFormulario(mensajes)
  if (mensajesFormulario.length > 0) {
    return { texto: mensajesFormulario.join('\n'), resaltado: true }
  }
  if (mensajes.length > 0) {
    return {
      texto: 'Faltan campos obligatorios por completar o corregir. Desplácese hacia arriba y revise los campos marcados en rojo.',
      resaltado: true,
    }
  }
  return { texto: 'Sin advertencias de formulario (valide primero).', resaltado: false }
}

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
  centrosAtencion: CentroAtencion[]
  unidadesOrganicas: UnidadOrganica[]
  municipalidades: Municipalidad[]
  departamentos: UbigeoItem[]
  provincias: UbigeoItem[]
  distritos: UbigeoItem[]
  /** "N° expediente" del bloque "Datos del expediente": editable false siempre (V2: número nunca se toca aquí). */
  numeroExpedienteLabel: string
  /**
   * `true` tras intentar "Validar"/"Registrar"/"Guardar" (18/08/2026, pedido explícito del usuario)
   * — fuerza a mostrar en rojo cualquier campo obligatorio vacío, aunque el usuario nunca lo haya
   * enfocado/abandonado (sin esto, `ValidatedTextField` solo se pinta tras su propio `onBlur`). Cada
   * página controla cuándo pasa a `true` (`Validar`/`Registrar`/`Guardar`) y cuándo se reinicia
   * (`Limpiar`, "Registrar otro", o al empezar una edición nueva).
   */
  mostrarErrores?: boolean
}

/**
 * Secciones 1–6 de JPanelRegistroManualRecepcionV2 (V2), compartidas entre Registro manual (alta)
 * y Edición manual — mismos campos, misma agrupación (V2 reutiliza literalmente el mismo panel
 * Swing en ambos modos). La sección 7 ("Resumen y confirmación") y los botones de acción quedan en
 * cada página, porque su texto/comportamiento sí difieren entre alta y edición.
 *
 * **Validación inline por campo (13/08/2026, pedido explícito del usuario)**: los campos con una
 * regla de validación real del backend (`RegistroManualValidacionService`, ver
 * `MENSAJES_CAMPO_INLINE` más arriba para el mapeo exacto campo↔mensaje) usan `ValidatedTextField`
 * en vez de `TextField` — borde verde + ícono de check tras salir del campo (blur) si el valor es
 * válido y no está vacío, borde rojo + ícono de alerta + mensaje corto debajo si no. Nunca se
 * evalúa mientras el usuario escribe (recién en `onBlur`), y los campos SIN regla real (Nro. trámite
 * web, Prioridad, Canal de ingreso, Tipo documento de titular/solicitante, Departamento/Provincia/
 * Distrito/Dirección) se dejaron como `TextField` plano — nunca se pintan verde/rojo, a propósito
 * ("no pintes de verde campos opcionales vacíos ni campos sin validación específica"). El campo
 * "N° expediente SGD" combina ambos mecanismos: `validate` local (obligatorio, instantáneo) +
 * `externalError` (duplicado, viene del servidor recién tras "Validar"/intentar guardar).
 * - **Bug de paso corregido**: "Tipo de acta" no tenía `required` en la versión anterior de este
 *   formulario, aunque el backend SÍ lo exige (`validarActa`, "Tipo de acta obligatorio.") — se
 *   agregó `required`/`requiredMessage` ahí también, no solo el resto de la validación inline.
 * - El bloque inferior "Errores / advertencias" de cada página (`RegistroManualPage.tsx`/
 *   `RegistroEdicionManualPage.tsx`) usa `filtrarMensajesFormulario` para NO repetir estos mismos
 *   mensajes de campo — solo quedan ahí los de nivel de formulario (duplicados, posible grupo
 *   familiar, "requiere decisión de asignación", errores de conexión).
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
  centrosAtencion,
  unidadesOrganicas,
  municipalidades,
  departamentos,
  provincias,
  distritos,
  numeroExpedienteLabel,
  mostrarErrores,
}: RegistroExpedienteFormProps) {
  const tipoSolicitante = solicitud.tipoSolicitante ?? 'CIUDADANO'
  const centroAtencionSeleccionado = centrosAtencion.find((c) => c.idCentroAtencion === solicitud.idCentroAtencion) ?? null
  const municipalidadSeleccionada = municipalidades.find((m) => m.idMunicipalidad === solicitud.idMunicipalidad) ?? null

  // El RadioGroup de "Validación inicial" mostraba "Sí corresponde a la SDRERC" seleccionado por
  // defecto (`value={solicitud.validacionInicial ?? CORRESPONDE}`) pero el estado real seguía en
  // `null`/vacío hasta que el usuario tocaba el radio a mano — al registrar sin tocarlo, el backend
  // rechazaba con "Seleccione el resultado de validación inicial." pese a verse marcado en pantalla
  // (18/08/2026, bug reportado por el usuario). `registroVacio()` ya inicializa el valor real para
  // el caso de alta; este efecto cubre además Edición manual/panel de datos inline cuando cargan un
  // expediente cuyo `validacionInicial` llegó vacío desde el servidor.
  useEffect(() => {
    if (!solicitud.validacionInicial) {
      updateSolicitud('validacionInicial', CORRESPONDE)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [solicitud.validacionInicial])

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
          <ValidatedTextField
            label="Hoja de envío"
            fullWidth
            sx={{ mt: 2 }}
            value={solicitud.hojaEnvio ?? ''}
            onChange={(e) => updateSolicitud('hojaEnvio', e.target.value)}
            validate={requerido('Hoja de envío obligatoria cuando no corresponde a la SDRERC.')}
            forceTouched={mostrarErrores}
          />
        )}
      </SectionCard>

      {/* 2. Datos de solicitud */}
      <SectionCard title="Datos de solicitud">
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <ValidatedTextField
              label="Fecha recepción"
              type="date"
              required
              fullWidth
              slotProps={{ inputLabel: { shrink: true } }}
              value={solicitud.fechaRecepcion ?? ''}
              onChange={(e) => updateSolicitud('fechaRecepcion', e.target.value)}
              validate={fechaRecepcionValida}
              forceTouched={mostrarErrores}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <CatalogoSelect
              label="Canal de ingreso"
              required
              requiredMessage="Canal de ingreso obligatorio."
              forceTouched={mostrarErrores}
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
              requiredMessage="Tipo de procedimiento obligatorio."
              forceTouched={mostrarErrores}
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
              requiredMessage="Tipo de documento obligatorio."
              forceTouched={mostrarErrores}
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
            <ValidatedTextField
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
              validate={requerido('Tipo de solicitud obligatorio.')}
              forceTouched={mostrarErrores}
            >
              {TIPOS_SOLICITUD.map((t) => (
                <MenuItem key={t.codigo} value={t.codigo}>
                  {t.nombre}
                </MenuItem>
              ))}
            </ValidatedTextField>
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
              <ValidatedTextField
                label="N° expediente SGD"
                required
                fullWidth
                value={solicitud.numeroExpedienteSgd ?? ''}
                onChange={(e) => updateSolicitud('numeroExpedienteSgd', e.target.value)}
                validate={requerido('N° expediente SGD obligatorio.')}
                externalError={numeroExpedienteSgdDuplicado ? motivoNumeroExpedienteSgdDuplicado : null}
                forceTouched={mostrarErrores}
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
                required
                requiredMessage="Tipo de acta obligatorio."
                forceTouched={mostrarErrores}
                items={tiposActa}
                value={acta.tipoActaCodigo}
                onChange={(codigo, nombre) => {
                  updateActa('tipoActaCodigo', codigo)
                  updateActa('tipoActaNombre', nombre)
                }}
              />
              <ValidatedTextField
                label="Nro. acta"
                required
                fullWidth
                value={acta.numeroActa ?? ''}
                onChange={(e) => updateActa('numeroActa', e.target.value)}
                validate={requerido('Número de acta obligatorio.')}
                forceTouched={mostrarErrores}
              />
            </Stack>
          </SectionCard>
        </Grid>
      </Grid>

      {/* 4. Titular — un solo campo "Nombres" de texto libre (revertido 14/08/2026: el cliente
          confirmó NO dividir Titular/Solicitante en apellido paterno/materno/nombres para Registro
          manual/Edición manual, mantener el campo único como estaba antes de la Fase 3). Las
          columnas apellido_paterno/apellido_materno de PERSONA quedan en la base de datos sin
          tocar por si el cliente cambia de opinión más adelante — ver v3/CLAUDE.md. */}
      <SectionCard title="Titular">
        <Grid container spacing={2}>
          <Grid size={12}>
            <ValidatedTextField
              label="Apellidos Nombres"
              required
              fullWidth
              value={titular.nombreCompleto ?? ''}
              onChange={(e) => updateTitular('nombreCompleto', e.target.value)}
              validate={requerido('Titular obligatorio.')}
              forceTouched={mostrarErrores}
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
            <ValidatedTextField
              label="Número documento"
              fullWidth
              value={titular.numeroDocumento ?? ''}
              onChange={(e) => updateTitular('numeroDocumento', e.target.value)}
              validate={documentoValido('titular')}
              forceTouched={mostrarErrores}
            />
          </Grid>
        </Grid>
      </SectionCard>

      {/* 5. Solicitante — 3 tipos posibles (14/08/2026, iniciativa "Solicitante con 3 tipos", ver
          v3/CLAUDE.md): Ciudadano (por defecto, campos ya existentes), Centro de Atención (buscador
          + confirmación de solo lectura) o Unidad Orgánica (select simple, 7 valores fijos). El
          selector va encima de los campos, que cambian según la opción elegida; al cambiar de tipo
          se limpian los valores del tipo anterior para no guardar datos mixtos. */}
      <SectionCard title="Solicitante">
        <Stack spacing={2}>
          <FormControl>
            <RadioGroup
              row
              value={tipoSolicitante}
              onChange={(e) => {
                const value = e.target.value
                updateSolicitud('tipoSolicitante', value)
                if (value !== 'CIUDADANO') {
                  updateRemitente('nombreCompleto', null)
                  updateRemitente('tipoDocumento', null)
                  updateRemitente('numeroDocumento', null)
                }
                if (value !== 'CENTRO_ATENCION') {
                  updateSolicitud('idCentroAtencion', null)
                  updateSolicitud('centroAtencionDenominacion', null)
                }
                if (value !== 'UNIDAD_ORGANICA') {
                  updateSolicitud('idUnidadOrganica', null)
                  updateSolicitud('unidadOrganicaNombre', null)
                }
                if (value !== 'MUNICIPALIDAD') {
                  updateSolicitud('idMunicipalidad', null)
                  updateSolicitud('municipalidadDenominacion', null)
                }
              }}
            >
              <FormControlLabel value="CIUDADANO" control={<Radio />} label="Ciudadano" />
              <FormControlLabel value="CENTRO_ATENCION" control={<Radio />} label="Centro de Atención" />
              <FormControlLabel value="UNIDAD_ORGANICA" control={<Radio />} label="Unidad Orgánica" />
              <FormControlLabel value="MUNICIPALIDAD" control={<Radio />} label="Municipalidad" />
            </RadioGroup>
          </FormControl>

          {tipoSolicitante === 'CIUDADANO' && (
            <Grid container spacing={2}>
              <Grid size={12}>
                <ValidatedTextField
                  label="Apellidos Nombres / Razón Social"
                  required
                  fullWidth
                  value={remitente.nombreCompleto ?? ''}
                  onChange={(e) => updateRemitente('nombreCompleto', e.target.value)}
                  validate={requerido('Solicitante obligatorio.')}
                  forceTouched={mostrarErrores}
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
                <ValidatedTextField
                  label="Número documento"
                  fullWidth
                  value={remitente.numeroDocumento ?? ''}
                  onChange={(e) => updateRemitente('numeroDocumento', e.target.value)}
                  validate={documentoValido('solicitante')}
                  forceTouched={mostrarErrores}
                />
              </Grid>
            </Grid>
          )}

          {tipoSolicitante === 'CENTRO_ATENCION' && (
            <Stack spacing={2}>
              <Autocomplete
                options={centrosAtencion}
                value={centroAtencionSeleccionado}
                getOptionLabel={(o) => o.denominacion ?? ''}
                // Sin esto, MUI usa por defecto getOptionLabel(option) como key de React — si 2
                // centros comparten denominación, colisionan y la opción se ve "repetida" en el
                // desplegable (mismo bug ya visto y corregido en Municipalidad, ver más abajo).
                getOptionKey={(o) => String(o.idCentroAtencion)}
                isOptionEqualToValue={(o, v) => o.idCentroAtencion === v.idCentroAtencion}
                filterOptions={(options, state) => {
                  const texto = normalizarBusqueda(state.inputValue)
                  if (!texto) return options
                  return options.filter((o) =>
                    [o.denominacion, o.codigoSio, o.departamento, o.provincia, o.distrito].some((v) =>
                      normalizarBusqueda(v ?? '').includes(texto),
                    ),
                  )
                }}
                onChange={(_, value) => {
                  updateSolicitud('idCentroAtencion', value?.idCentroAtencion ?? null)
                  updateSolicitud('centroAtencionDenominacion', value?.denominacion ?? null)
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Centro de atención"
                    placeholder="Buscar por denominación, código SIO o ubicación..."
                    required
                    error={!centroAtencionSeleccionado}
                    helperText={!centroAtencionSeleccionado ? 'Seleccione un centro de atención.' : undefined}
                  />
                )}
              />
              {centroAtencionSeleccionado && (
                <Paper variant="outlined" sx={{ p: 2, bgcolor: sdrercColors.backgroundPaper }}>
                  <Grid container spacing={1.5}>
                    <Grid size={12}>
                      <CampoSoloLectura label="Denominación" value={centroAtencionSeleccionado.denominacion} />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <CampoSoloLectura label="Categorización" value={centroAtencionSeleccionado.categorizacion} />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6 }}>
                      <CampoSoloLectura label="Código SIO" value={centroAtencionSeleccionado.codigoSio ?? '-'} />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <CampoSoloLectura label="Departamento" value={centroAtencionSeleccionado.departamento} />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <CampoSoloLectura label="Provincia" value={centroAtencionSeleccionado.provincia} />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <CampoSoloLectura label="Distrito" value={centroAtencionSeleccionado.distrito} />
                    </Grid>
                  </Grid>
                </Paper>
              )}
            </Stack>
          )}

          {tipoSolicitante === 'UNIDAD_ORGANICA' && (
            <TextField
              select
              label="Unidad orgánica"
              required
              fullWidth
              value={solicitud.idUnidadOrganica ?? ''}
              error={!solicitud.idUnidadOrganica}
              helperText={!solicitud.idUnidadOrganica ? 'Seleccione una unidad orgánica.' : undefined}
              onChange={(e) => {
                const id = e.target.value ? Number(e.target.value) : null
                const unidad = unidadesOrganicas.find((u) => u.idUnidadOrganica === id) ?? null
                updateSolicitud('idUnidadOrganica', id)
                updateSolicitud('unidadOrganicaNombre', unidad?.nombre ?? null)
              }}
            >
              <MenuItem value="">No definido</MenuItem>
              {unidadesOrganicas.map((u) => (
                <MenuItem key={u.idUnidadOrganica} value={u.idUnidadOrganica}>
                  {u.nombre}
                </MenuItem>
              ))}
            </TextField>
          )}

          {tipoSolicitante === 'MUNICIPALIDAD' && (
            <Stack spacing={2}>
              <Autocomplete
                options={municipalidades}
                value={municipalidadSeleccionada}
                getOptionLabel={(o) => o.denominacion ?? ''}
                // Sin esto, MUI usa por defecto getOptionLabel(option) (la denominación) como key de
                // React — con homónimos reales (2-3 municipalidades con la misma denominación, la
                // razón de ser de este selector) esa key colisiona y React termina renderizando la
                // opción repetida varias veces en el desplegable en vez de una por cada ubicación
                // real (18/08/2026, bug reportado por el usuario). El id es la única key que sí es
                // única por fila.
                getOptionKey={(o) => String(o.idMunicipalidad)}
                isOptionEqualToValue={(o, v) => o.idMunicipalidad === v.idMunicipalidad}
                filterOptions={(options, state) => {
                  const texto = normalizarBusqueda(state.inputValue)
                  const filtrados = texto
                    ? options.filter((o) =>
                        [o.denominacion, o.departamento, o.provincia, o.distrito, o.centroPoblado]
                          .filter(Boolean)
                          .some((campo) => normalizarBusqueda(campo as string).includes(texto)),
                      )
                    : options
                  // Limita el desplegable a los primeros 50 resultados (18/08/2026, pedido explícito
                  // del usuario: la carga se sentía lenta al abrir el combo) — sin esto, con el campo
                  // vacío se renderizaban los 1652 registros del catálogo de una sola vez. El usuario
                  // sigue pudiendo escribir para acotar la búsqueda hasta que aparezca lo que busca.
                  return filtrados.slice(0, 50)
                }}
                renderOption={(props, option) => {
                  const { key, ...rest } = props
                  const ubicacion = lineaUbicacionMunicipalidad(option)
                  return (
                    <Box component="li" key={key} {...rest}>
                      <Stack spacing={0}>
                        <Typography variant="body2">{option.denominacion}</Typography>
                        {ubicacion && (
                          <Typography variant="caption" color="text.secondary">
                            {ubicacion}
                          </Typography>
                        )}
                      </Stack>
                    </Box>
                  )
                }}
                onChange={(_, value) => {
                  updateSolicitud('idMunicipalidad', value?.idMunicipalidad ?? null)
                  updateSolicitud('municipalidadDenominacion', value?.denominacion ?? null)
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Municipalidad"
                    placeholder="Buscar por denominación, departamento, provincia, distrito..."
                    required
                    error={!municipalidadSeleccionada}
                    helperText={!municipalidadSeleccionada ? 'Seleccione una municipalidad.' : undefined}
                  />
                )}
              />
              {municipalidadSeleccionada && (
                <Paper variant="outlined" sx={{ p: 2, bgcolor: sdrercColors.backgroundPaper }}>
                  <Grid container spacing={1.5}>
                    <Grid size={12}>
                      <CampoSoloLectura label="Denominación" value={municipalidadSeleccionada.denominacion} />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <CampoSoloLectura label="Departamento" value={municipalidadSeleccionada.departamento} />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <CampoSoloLectura label="Provincia" value={municipalidadSeleccionada.provincia} />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 4 }}>
                      <CampoSoloLectura label="Distrito" value={municipalidadSeleccionada.distrito} />
                    </Grid>
                    {esMunicipalidadDeCentroPoblado(municipalidadSeleccionada.tipo) && (
                      <Grid size={12}>
                        <CampoSoloLectura label="Centro poblado" value={municipalidadSeleccionada.centroPoblado} />
                      </Grid>
                    )}
                    <Grid size={12}>
                      <CampoSoloLectura label="Código" value={municipalidadSeleccionada.codigo} />
                    </Grid>
                  </Grid>
                </Paper>
              )}
            </Stack>
          )}
        </Stack>
      </SectionCard>

      {/* 6. Datos de notificación y ubicación (en V2 llena los campos de contacto/ubigeo del
          Solicitante, no del titular — remitente.setCorreo/setTelefono/setDireccion/ubigeo) */}
      <SectionCard title="Datos de notificación y ubicación">
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <ValidatedTextField
              label="Correo"
              fullWidth
              value={remitente.correo ?? ''}
              onChange={(e) => updateRemitente('correo', e.target.value)}
              validate={correoValido('Correo de solicitante inválido.')}
              forceTouched={mostrarErrores}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <ValidatedTextField
              label="Teléfono"
              fullWidth
              value={remitente.telefono ?? ''}
              onChange={(e) => updateRemitente('telefono', e.target.value)}
              validate={telefonoValido('Teléfono de solicitante inválido.')}
              forceTouched={mostrarErrores}
            />
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

/**
 * Barra de acento superior (28/08/2026, sistema de diseño premium — Incremento 1, ver plan
 * "necesito-un-plan-de-cuddly-wombat.md"): mismo espíritu que la de `MetricCard` — refuerza la
 * identidad visual de cada sección en vez de una tarjeta blanca plana. `accentColor` es opcional
 * (default `sdrercColors.primary`, el azul institucional) para no romper ninguno de los 16 archivos
 * que ya usan `SectionCard` sin ese prop; queda listo para que el Incremento 2 (acento por módulo)
 * lo sobrescriba página por página sin tocar este componente de nuevo.
 *
 * Borde completo + hover (28/08/2026, mismo día, mismo pedido que `MetricCard` — "que el mismo
 * lenguaje de acento se extienda a los paneles"): mismo tratamiento de borde (`alpha(accentColor,
 * 0.25)` en reposo, `0.5` en hover) que ya se aplicó a `MetricCard` — SIN el estado "activo"
 * (fondo tintado + brillo) porque `SectionCard` no es clicable/filtrable, ese lenguaje es exclusivo
 * de una tarjeta que representa una selección posible.
 */
export function SectionCard({
  title,
  children,
  accentColor = sdrercColors.primary,
}: {
  title: string
  children: React.ReactNode
  accentColor?: string
}) {
  return (
    <Paper
      elevation={1}
      sx={{
        position: 'relative',
        overflow: 'hidden',
        p: 3,
        borderRadius: 2,
        border: `1px solid ${alpha(accentColor, 0.25)}`,
        transition: 'border-color 150ms ease, box-shadow 150ms ease',
        '&:hover': { borderColor: alpha(accentColor, 0.5), boxShadow: '0 4px 12px rgba(16,24,40,0.08)' },
      }}
    >
      <Box sx={{ position: 'absolute', top: 0, left: 0, right: 0, height: 3, bgcolor: accentColor }} />
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
  requiredMessage,
  forceTouched,
}: {
  label: string
  items: CatalogoItem[]
  value: string | null
  onChange: (codigo: string | null, nombre: string | null) => void
  required?: boolean
  /** Mensaje EXACTO que emite el backend para este campo — ver `MENSAJES_CAMPO_INLINE` más arriba. */
  requiredMessage?: string
  forceTouched?: boolean
}) {
  return (
    <ValidatedTextField
      select
      label={label}
      required={required}
      forceTouched={forceTouched}
      fullWidth
      value={value ?? ''}
      onChange={(e) => {
        const item = items.find((i) => i.codigo === e.target.value)
        onChange(item?.codigo ?? null, item?.nombre ?? null)
      }}
      validate={required && requiredMessage ? requerido(requiredMessage) : undefined}
    >
      {items.map((item) => (
        <MenuItem key={item.codigo} value={item.codigo ?? ''}>
          {item.nombre}
        </MenuItem>
      ))}
    </ValidatedTextField>
  )
}
