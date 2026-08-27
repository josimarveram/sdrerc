import { useEffect, useState } from 'react'
import { Alert, Box, CircularProgress, Grid, IconButton, Stack, Tooltip, Typography } from '@mui/material'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import CheckOutlinedIcon from '@mui/icons-material/CheckOutlined'
import CloseOutlinedIcon from '@mui/icons-material/CloseOutlined'
import type { ExpedienteBandeja } from '../api/registroBandejaApi'
import {
  guardarEdicionManual,
  listarCanalesRecepcion,
  listarCentrosAtencion,
  listarDepartamentos,
  listarDistritos,
  listarMunicipalidades,
  listarProcedimientosRegistrales,
  listarProvincias,
  listarTiposActa,
  listarTiposDocumento,
  listarUnidadesOrganicas,
  obtenerExpedienteParaEdicion,
  validarEdicionManual,
  type CatalogoItem,
  type CentroAtencion,
  type ExpedienteEdicionManual,
  type Municipalidad,
  type UbigeoItem,
  type UnidadOrganica,
} from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { StatusBadge, diasBadgeTone } from './StatusBadge'
import { filtrarMensajesFormulario, RegistroExpedienteForm, SectionCard } from './RegistroExpedienteForm'

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

function Dato({ label, value }: { label: string; value: string | null | undefined }) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary" component="div">
        {label}
      </Typography>
      <Typography variant="body2">{value && value.trim() !== '' ? value : '-'}</Typography>
    </Box>
  )
}

interface PanelDatosExpedienteProps {
  bandeja: ExpedienteBandeja
  detalle: ExpedienteEdicionManual | null
  cargando: boolean
  error: string | null
  /** Refresca el detalle de solo lectura del padre (`RegistroBandejaPage`) tras un guardado exitoso. */
  onGuardado?: () => void
  /**
   * Bloqueo EXPLÍCITO por módulo (20/08/2026, pedido explícito del usuario, regla permanente para
   * todos los módulos siguientes a partir de Verificación — ver v3/CLAUDE.md): el ícono de lápiz
   * solo debe estar disponible en Registro/Recepción, Asignación y Análisis (`true`, el valor por
   * defecto, para no tocar esos 3 módulos ya construidos). Verificación y cualquier módulo
   * posterior (Ejecución, Notificación, etc.) deben pasar `false`. Deliberadamente NO se infiere
   * solo de `puedeEditar` (la condición de estado ya existente, `etapaCodigo==='REGISTRO' && ...`):
   * un expediente en Verificación nunca cumpliría esa condición de todas formas, pero el pedido es
   * un bloqueo explícito por módulo, no implícito por estado — más robusto ante futuros cambios de
   * datos que coincidan por accidente con la condición de estado.
   */
  permiteEdicion?: boolean
  /**
   * 21/08/2026, pedido explícito del usuario: habilita el lápiz de edición en el módulo Análisis,
   * donde el expediente nunca cumple `etapaCodigo==='REGISTRO'` (siempre está en ASIGNACION/ASIGNADO
   * o ANALISIS) — sin este flag, el ícono quedaba siempre visible pero permanentemente deshabilitado
   * ahí. Replica `edicionDesdeAnalisis` (V2): omite la condición de etapa/estado/responsable tanto
   * en el frontend (`puedeEditar`) como en el backend (`ExpedienteEdicionManualDAO.validarEditable`,
   * que directamente no aplica esa validación cuando `desdeAnalisis=true`).
   */
  desdeAnalisis?: boolean
}

/**
 * Equivalente web del "Panel de datos" de V2 (doble clic en Bandeja Registro): título genérico
 * "Panel de datos" + titular en azul debajo (ver CLAUDE.md raíz, "Panel derecho Registro"), mismos
 * 7 bloques: Datos del plazo, Datos del expediente, Datos del acta, Datos de solicitud, Datos del
 * titular, Datos del solicitante, Datos de Notificación y Ubicación.
 *
 * **Edición inline (13/08/2026, pedido explícito del usuario)**: ícono de lápiz junto al titular,
 * en la cabecera. Al activarse, reemplaza los 7 bloques de solo lectura por `RegistroExpedienteForm`
 * — el MISMO componente/lógica de negocio que ya usan Registro manual y `RegistroEdicionManualPage`
 * (evita duplicar reglas de negocio, tal como pidió el usuario: "usa el mismo endpoint/lógica que ya
 * usa el formulario de Registro manual"). Esto también resuelve solo el requisito de "qué campos son
 * editables": `RegistroExpedienteForm` ya expone exactamente el subconjunto de campos que V2/V3
 * consideran editables (Canal, Tipo de acta, Tipo de solicitud, Tipo de documento como selects;
 * Fecha de recepción como date picker; el resto texto libre) — "Días"/"Fecha Vencimiento" (Datos del
 * plazo, ambos calculados) y "N° expediente"/"Etapa"/"Estado" (Datos del expediente, gestionados por
 * el flujo de asignación/transiciones, no por edición manual) NUNCA aparecen en ese formulario, así
 * que quedan automáticamente fuera de edición sin lógica adicional — ni siquiera se muestra el
 * bloque "Datos del plazo" en modo edición, ya que sus 2 únicos campos son ambos no editables.
 *
 * Flujo: clic en el lápiz → `obtenerExpedienteParaEdicion` (mismo endpoint que la página de edición
 * dedicada, con su misma validación de "etapa REGISTRO/estado REGISTRADO/sin asignación" del lado
 * del backend) + catálogos (canales/procedimientos/tipos de documento/tipos de acta/departamentos,
 * más provincias/distritos en cascada) → `RegistroExpedienteForm` con estado local editable → ícono
 * de guardar (check) llama `guardarEdicionManual` (mismo PUT que la página dedicada) y, si tiene
 * éxito, dispara `onGuardado` para que el padre vuelva a pedir el detalle de solo lectura → ícono de
 * cancelar (x) descarta el estado local sin persistir nada, sin tocar el `detalle` original. El
 * lápiz se deshabilita (con tooltip explicando el motivo) cuando el expediente no cumple esa misma
 * condición de editabilidad — mismo criterio que ya usa la columna "Editar" de la grilla
 * (`etapaCodigo==='REGISTRO' && estadoCodigo==='REGISTRADO' && !responsableActual`), para no dejar
 * clickear un lápiz que el backend rechazaría de todas formas.
 *
 * Compromiso deliberado: el agrupamiento de secciones difiere entre el modo lectura (7 bloques,
 * organizados por tema) y el modo edición (6 secciones de `RegistroExpedienteForm`, organizadas
 * igual que V2/Registro manual) — los campos "saltan" de agrupamiento al activar edición. Se aceptó
 * este trade-off porque reimplementar el formulario de edición con el agrupamiento del modo lectura
 * hubiera duplicado la lógica de negocio (catálogos, validación, selects) que el pedido explícito
 * del usuario pidió evitar duplicar.
 */
export function PanelDatosExpediente({
  bandeja,
  detalle,
  cargando,
  error,
  onGuardado,
  permiteEdicion = true,
  desdeAnalisis = false,
}: PanelDatosExpedienteProps) {
  const puedeEditar =
    permiteEdicion &&
    (desdeAnalisis || (bandeja.etapaCodigo === 'REGISTRO' && bandeja.estadoCodigo === 'REGISTRADO' && !bandeja.responsableActual))

  const [modoEdicion, setModoEdicion] = useState(false)
  const [registroEdicion, setRegistroEdicion] = useState<ExpedienteEdicionManual | null>(null)
  const [cargandoEdicion, setCargandoEdicion] = useState(false)
  const [guardando, setGuardando] = useState(false)
  const [errorEdicion, setErrorEdicion] = useState<string | null>(null)
  // Fuerza a mostrar en rojo los campos obligatorios vacíos al intentar Guardar (18/08/2026, pedido
  // explícito del usuario) — ver Javadoc de `RegistroExpedienteFormProps.mostrarErrores`.
  const [mostrarErrores, setMostrarErrores] = useState(false)
  const [canales, setCanales] = useState<CatalogoItem[]>([])
  const [procedimientos, setProcedimientos] = useState<CatalogoItem[]>([])
  const [tiposDocumento, setTiposDocumento] = useState<CatalogoItem[]>([])
  const [tiposActa, setTiposActa] = useState<CatalogoItem[]>([])
  const [centrosAtencion, setCentrosAtencion] = useState<CentroAtencion[]>([])
  const [unidadesOrganicas, setUnidadesOrganicas] = useState<UnidadOrganica[]>([])
  const [municipalidades, setMunicipalidades] = useState<Municipalidad[]>([])
  const [departamentos, setDepartamentos] = useState<UbigeoItem[]>([])
  const [provincias, setProvincias] = useState<UbigeoItem[]>([])
  const [distritos, setDistritos] = useState<UbigeoItem[]>([])

  useEffect(() => {
    if (!registroEdicion?.remitente.idDepartamento) {
      setProvincias([])
      return
    }
    listarProvincias(registroEdicion.remitente.idDepartamento)
      .then(setProvincias)
      .catch(() => setProvincias([]))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [registroEdicion?.remitente.idDepartamento])

  useEffect(() => {
    if (!registroEdicion?.remitente.idProvincia) {
      setDistritos([])
      return
    }
    listarDistritos(registroEdicion.remitente.idProvincia)
      .then(setDistritos)
      .catch(() => setDistritos([]))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [registroEdicion?.remitente.idProvincia])

  function iniciarEdicion() {
    setErrorEdicion(null)
    setMostrarErrores(false)
    setCargandoEdicion(true)
    setModoEdicion(true)
    Promise.all([
      obtenerExpedienteParaEdicion(bandeja.idExpediente, desdeAnalisis),
      listarCanalesRecepcion(),
      listarProcedimientosRegistrales(),
      listarTiposDocumento(),
      listarTiposActa(),
      listarDepartamentos(),
      listarCentrosAtencion(),
      listarUnidadesOrganicas(),
      listarMunicipalidades(),
    ])
      .then(([exp, c, p, d, a, dep, ca, uo, muni]) => {
        setRegistroEdicion(exp)
        setCanales(c)
        setProcedimientos(p)
        setTiposDocumento(d)
        setTiposActa(a)
        setDepartamentos(dep)
        setCentrosAtencion(ca)
        setUnidadesOrganicas(uo)
        setMunicipalidades(muni)
      })
      .catch((e) => setErrorEdicion(e instanceof ApiError ? e.message : 'No se pudo cargar el expediente para editar.'))
      .finally(() => setCargandoEdicion(false))
  }

  function cancelarEdicion() {
    setModoEdicion(false)
    setRegistroEdicion(null)
    setErrorEdicion(null)
    setMostrarErrores(false)
  }

  async function guardarEdicion() {
    if (!registroEdicion) return
    setGuardando(true)
    setErrorEdicion(null)
    setMostrarErrores(true)
    try {
      // Valida primero (13/08/2026, pedido explícito del usuario): antes se llamaba directo a
      // guardarEdicionManual, que ante un N° expediente SGD duplicado devolvía solo un mensaje de
      // error genérico (400, sin el DTO actualizado) — el campo nunca se resaltaba en rojo porque
      // `numeroExpedienteSgdDuplicado` en el estado local seguía en `false`. Este panel no tiene un
      // botón "Validar" separado (a propósito, ver Javadoc del componente: solo Guardar/Cancelar),
      // así que el propio "Guardar" ahora valida internamente primero — mismo resultado final, sin
      // agregar un botón nuevo. Si hay mensajes de validación, se actualiza el formulario con el
      // DTO devuelto (que sí trae `numeroExpedienteSgdDuplicado`/`motivoNumeroExpedienteSgdDuplicado`/
      // `posibleDuplicado`/`motivoDuplicado` marcados) y se corta antes de guardar.
      const resp = await validarEdicionManual(registroEdicion)
      if (resp.mensajes.length > 0) {
        setRegistroEdicion(resp.registro)
        // Solo mensajes de nivel de FORMULARIO en el Alert superior (13/08/2026, pedido explícito
        // del usuario) — los de campo individual ya se ven inline en rojo junto a cada campo, vía
        // `ValidatedTextField` dentro de `RegistroExpedienteForm`. Si TODOS los mensajes eran de
        // campo (ninguno de formulario), igual se bloquea el guardado (mismo `if` de arriba), pero
        // con un aviso genérico apuntando a los campos marcados en vez de repetir el texto inline.
        const mensajesFormulario = filtrarMensajesFormulario(resp.mensajes)
        setErrorEdicion(mensajesFormulario.length > 0 ? mensajesFormulario.join(' | ') : 'Corrija los campos marcados en rojo antes de guardar.')
        return
      }
      await guardarEdicionManual(resp.registro, desdeAnalisis)
      setModoEdicion(false)
      setRegistroEdicion(null)
      onGuardado?.()
    } catch (e) {
      setErrorEdicion(e instanceof ApiError ? e.message : 'No se pudo guardar los cambios.')
    } finally {
      setGuardando(false)
    }
  }

  function updateSolicitud<K extends keyof ExpedienteEdicionManual['solicitud']>(key: K, value: ExpedienteEdicionManual['solicitud'][K]) {
    setRegistroEdicion((prev) => (prev ? { ...prev, solicitud: { ...prev.solicitud, [key]: value } } : prev))
  }
  function updateActa<K extends keyof ExpedienteEdicionManual['acta']>(key: K, value: ExpedienteEdicionManual['acta'][K]) {
    setRegistroEdicion((prev) => (prev ? { ...prev, acta: { ...prev.acta, [key]: value } } : prev))
  }
  function updateTitular<K extends keyof ExpedienteEdicionManual['titular']>(key: K, value: ExpedienteEdicionManual['titular'][K]) {
    setRegistroEdicion((prev) => (prev ? { ...prev, titular: { ...prev.titular, [key]: value } } : prev))
  }
  function updateRemitente<K extends keyof ExpedienteEdicionManual['remitente']>(key: K, value: ExpedienteEdicionManual['remitente'][K]) {
    setRegistroEdicion((prev) => (prev ? { ...prev, remitente: { ...prev.remitente, [key]: value } } : prev))
  }

  return (
    <Stack spacing={3}>
      <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 2 }}>
        <Box>
          <Typography variant="h6" sx={{ color: sdrercColors.primary }}>
            Panel de datos
          </Typography>
          <Typography variant="subtitle1" sx={{ color: sdrercColors.info, fontWeight: 600 }}>
            {bandeja.titular || '-'}
          </Typography>
        </Box>
        {modoEdicion ? (
          <Stack direction="row" spacing={0.5}>
            <Tooltip title="Guardar cambios">
              <span>
                <IconButton size="small" color="primary" onClick={guardarEdicion} disabled={guardando || cargandoEdicion || !registroEdicion}>
                  <CheckOutlinedIcon fontSize="small" />
                </IconButton>
              </span>
            </Tooltip>
            <Tooltip title="Cancelar">
              <span>
                <IconButton size="small" onClick={cancelarEdicion} disabled={guardando}>
                  <CloseOutlinedIcon fontSize="small" />
                </IconButton>
              </span>
            </Tooltip>
          </Stack>
        ) : permiteEdicion ? (
          <Tooltip title={puedeEditar ? 'Editar' : 'Solo se puede editar mientras el expediente esté en Registro, sin asignar'}>
            <span>
              <IconButton size="small" onClick={iniciarEdicion} disabled={!puedeEditar}>
                <EditOutlinedIcon fontSize="small" />
              </IconButton>
            </span>
          </Tooltip>
        ) : null}
      </Box>

      {error && <Alert severity="error">{error}</Alert>}
      {errorEdicion && <Alert severity="error">{errorEdicion}</Alert>}

      {modoEdicion ? (
        cargandoEdicion || !registroEdicion ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress size={28} />
          </Box>
        ) : (
          <RegistroExpedienteForm
            solicitud={registroEdicion.solicitud}
            acta={registroEdicion.acta}
            titular={registroEdicion.titular}
            remitente={registroEdicion.remitente}
            numeroExpedienteVistaPrevia={registroEdicion.numeroExpedienteVistaPrevia}
            posibleDuplicado={registroEdicion.posibleDuplicado}
            motivoDuplicado={registroEdicion.motivoDuplicado}
            numeroExpedienteSgdDuplicado={registroEdicion.numeroExpedienteSgdDuplicado}
            motivoNumeroExpedienteSgdDuplicado={registroEdicion.motivoNumeroExpedienteSgdDuplicado}
            updateSolicitud={updateSolicitud}
            updateActa={updateActa}
            updateTitular={updateTitular}
            updateRemitente={updateRemitente}
            canales={canales}
            procedimientos={procedimientos}
            tiposDocumento={tiposDocumento}
            tiposActa={tiposActa}
            centrosAtencion={centrosAtencion}
            unidadesOrganicas={unidadesOrganicas}
            municipalidades={municipalidades}
            departamentos={departamentos}
            provincias={provincias}
            distritos={distritos}
            numeroExpedienteLabel={registroEdicion.numeroExpediente || registroEdicion.numeroExpedienteVistaPrevia || 'Sin número'}
            mostrarErrores={mostrarErrores}
          />
        )
      ) : (
        <>
          <SectionCard title="Datos del plazo">
            <Grid container spacing={2}>
              <Grid size={6}>
                <Typography variant="caption" color="text.secondary" component="div">
                  Días
                </Typography>
                <StatusBadge
                  label={bandeja.diasRestantes === null || bandeja.diasRestantes === undefined ? '-' : String(bandeja.diasRestantes)}
                  tone={diasBadgeTone(bandeja.diasRestantes)}
                />
              </Grid>
              <Grid size={6}>
                <Dato label="Fecha Vencimiento" value={formatearFecha(bandeja.fechaVencimiento)} />
              </Grid>
            </Grid>
          </SectionCard>

          <SectionCard title="Datos del expediente">
            <Grid container spacing={2}>
              <Grid size={6}>
                <Dato label="N° expediente" value={bandeja.numeroExpediente} />
              </Grid>
              <Grid size={6}>
                <Dato label="N° expediente SGD" value={bandeja.numeroExpedienteSgd} />
              </Grid>
              <Grid size={6}>
                <Dato label="N° trámite" value={bandeja.numeroTramiteDocumentario} />
              </Grid>
              <Grid size={6}>
                <Dato label="Canal" value={bandeja.canal} />
              </Grid>
              <Grid size={6}>
                <Dato label="Etapa" value={bandeja.etapaCodigo} />
              </Grid>
              <Grid size={6}>
                <Dato label="Estado" value={bandeja.estadoCodigo} />
              </Grid>
            </Grid>
          </SectionCard>

          <SectionCard title="Datos del acta">
            <Grid container spacing={2}>
              <Grid size={6}>
                <Dato label="Tipo de acta" value={bandeja.tipoActa} />
              </Grid>
              <Grid size={6}>
                <Dato label="N° acta" value={bandeja.numeroActa} />
              </Grid>
            </Grid>
          </SectionCard>

          <SectionCard title="Datos de solicitud">
            <Grid container spacing={2}>
              <Grid size={6}>
                <Dato label="Procedimiento registral" value={bandeja.procedimiento} />
              </Grid>
              <Grid size={6}>
                <Dato label="Fecha de recepción" value={formatearFecha(bandeja.fechaRecepcion)} />
              </Grid>
              <Grid size={6}>
                <Dato label="Tipo de solicitud" value={detalle?.solicitud.tipoSolicitudNombre} />
              </Grid>
              <Grid size={6}>
                <Dato label="Tipo de documento" value={detalle?.solicitud.tipoDocumentoNombre} />
              </Grid>
              <Grid size={6}>
                <Dato label="N° documento" value={detalle?.solicitud.numeroDocumento} />
              </Grid>
              <Grid size={6}>
                <Dato label="Validación inicial" value={detalle?.solicitud.validacionInicial} />
              </Grid>
              <Grid size={6}>
                <Dato label="Hoja de envío" value={detalle?.solicitud.hojaEnvio} />
              </Grid>
            </Grid>
          </SectionCard>

          <SectionCard title="Datos del titular">
            <Grid container spacing={2}>
              <Grid size={12}>
                <Dato label="Nombres" value={detalle?.titular.nombreCompleto ?? bandeja.titular} />
              </Grid>
              <Grid size={6}>
                <Dato label="Tipo documento" value={detalle?.titular.tipoDocumento} />
              </Grid>
              <Grid size={6}>
                <Dato label="N° documento" value={detalle?.titular.numeroDocumento} />
              </Grid>
            </Grid>
          </SectionCard>

          <SectionCard title="Datos del solicitante">
            <Grid container spacing={2}>
              {/* Solicitante con 4 tipos posibles (14/08/2026, ver v3/CLAUDE.md): Centro de Atención,
                  Unidad Orgánica y Municipalidad no usan `remitente` en absoluto, muestran su propio
                  nombre. */}
              {detalle?.solicitud.tipoSolicitante === 'CENTRO_ATENCION' ? (
                <Grid size={12}>
                  <Dato label="Centro de atención" value={detalle?.solicitud.centroAtencionDenominacion} />
                </Grid>
              ) : detalle?.solicitud.tipoSolicitante === 'UNIDAD_ORGANICA' ? (
                <Grid size={12}>
                  <Dato label="Unidad orgánica" value={detalle?.solicitud.unidadOrganicaNombre} />
                </Grid>
              ) : detalle?.solicitud.tipoSolicitante === 'MUNICIPALIDAD' ? (
                <Grid size={12}>
                  <Dato label="Municipalidad" value={detalle?.solicitud.municipalidadDenominacion} />
                </Grid>
              ) : (
                <>
                  <Grid size={12}>
                    <Dato label="Nombres / Razón Social" value={detalle?.remitente.nombreCompleto} />
                  </Grid>
                  <Grid size={6}>
                    <Dato label="Tipo documento" value={detalle?.remitente.tipoDocumento} />
                  </Grid>
                  <Grid size={6}>
                    <Dato label="N° documento" value={detalle?.remitente.numeroDocumento} />
                  </Grid>
                </>
              )}
            </Grid>
          </SectionCard>

          <SectionCard title="Datos de Notificación y Ubicación">
            <Grid container spacing={2}>
              <Grid size={6}>
                <Dato label="Correo" value={detalle?.remitente.correo} />
              </Grid>
              <Grid size={6}>
                <Dato label="Teléfono" value={detalle?.remitente.telefono} />
              </Grid>
              <Grid size={4}>
                <Dato label="Departamento" value={detalle?.remitente.departamento} />
              </Grid>
              <Grid size={4}>
                <Dato label="Provincia" value={detalle?.remitente.provincia} />
              </Grid>
              <Grid size={4}>
                <Dato label="Distrito" value={detalle?.remitente.distrito} />
              </Grid>
              <Grid size={12}>
                <Dato label="Dirección" value={detalle?.remitente.direccion} />
              </Grid>
            </Grid>
          </SectionCard>

          {cargando && (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
              <CircularProgress size={22} />
            </Box>
          )}
        </>
      )}
    </Stack>
  )
}
