import { useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, CircularProgress, Stack, TextField, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'
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
  type RegistroManualResultado,
  type UbigeoItem,
  type UnidadOrganica,
} from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { RegistroExpedienteForm, SectionCard, resumenMensajesFormulario } from '../components/RegistroExpedienteForm'

/**
 * Equivalente web de JPanelRegistroManualRecepcionV2 (V2) en modo edición, accesible desde el
 * botón "Editar" de la Bandeja Registro. Mismas secciones 1-6 que Registro manual (componente
 * compartido `RegistroExpedienteForm`); a diferencia del alta: "N° expediente" muestra el número
 * real (no "Se genera al registrar"), el botón principal dice "Guardar cambios" y no hay "Registrar
 * otro" al terminar, solo volver a la bandeja. Alcance: solo edición desde Registro/Recepción
 * (etapa REGISTRO/estado REGISTRADO, sin asignación activa) — ver Javadoc del backend
 * (`ExpedienteEdicionManualDAO`) para los puntos de entrada de V2 que no se portaron (desde
 * Análisis/Asignación, y eliminar).
 */
export function RegistroEdicionManualPage() {
  const navigate = useNavigate()
  const { idExpediente } = useParams<{ idExpediente: string }>()
  const [registro, setRegistro] = useState<ExpedienteEdicionManual | null>(null)
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
  const [mensajes, setMensajes] = useState<string[]>([])
  const [resultado, setResultado] = useState<RegistroManualResultado | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [cargando, setCargando] = useState(true)
  const [loading, setLoading] = useState(false)
  // Fuerza a mostrar en rojo los campos obligatorios vacíos al intentar Validar/Guardar (18/08/2026,
  // pedido explícito del usuario) — ver Javadoc de `RegistroExpedienteFormProps.mostrarErrores`.
  const [mostrarErrores, setMostrarErrores] = useState(false)
  // Texto + resaltado del cuadro "Errores / advertencias del formulario" — ver Javadoc de
  // `resumenMensajesFormulario` (18/08/2026, pedido explícito del usuario).
  const resumenFormulario = useMemo(() => resumenMensajesFormulario(mensajes), [mensajes])

  useEffect(() => {
    if (!idExpediente) return
    setCargando(true)
    Promise.all([
      obtenerExpedienteParaEdicion(Number(idExpediente)),
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
        setRegistro(exp)
        setCanales(c)
        setProcedimientos(p)
        setTiposDocumento(d)
        setTiposActa(a)
        setDepartamentos(dep)
        setCentrosAtencion(ca)
        setUnidadesOrganicas(uo)
        setMunicipalidades(muni)
      })
      .catch((e) => setError(describeError(e)))
      .finally(() => setCargando(false))
  }, [idExpediente])

  useEffect(() => {
    if (!registro?.remitente.idDepartamento) {
      setProvincias([])
      return
    }
    listarProvincias(registro.remitente.idDepartamento)
      .then(setProvincias)
      .catch((e) => setError(describeError(e)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [registro?.remitente.idDepartamento])

  useEffect(() => {
    if (!registro?.remitente.idProvincia) {
      setDistritos([])
      return
    }
    listarDistritos(registro.remitente.idProvincia)
      .then(setDistritos)
      .catch((e) => setError(describeError(e)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [registro?.remitente.idProvincia])

  function updateSolicitud<K extends keyof ExpedienteEdicionManual['solicitud']>(
    key: K,
    value: ExpedienteEdicionManual['solicitud'][K],
  ) {
    setRegistro((prev) => (prev ? { ...prev, solicitud: { ...prev.solicitud, [key]: value } } : prev))
  }

  function updateActa<K extends keyof ExpedienteEdicionManual['acta']>(key: K, value: ExpedienteEdicionManual['acta'][K]) {
    setRegistro((prev) => (prev ? { ...prev, acta: { ...prev.acta, [key]: value } } : prev))
  }

  function updateTitular<K extends keyof ExpedienteEdicionManual['titular']>(
    key: K,
    value: ExpedienteEdicionManual['titular'][K],
  ) {
    setRegistro((prev) => (prev ? { ...prev, titular: { ...prev.titular, [key]: value } } : prev))
  }

  function updateRemitente<K extends keyof ExpedienteEdicionManual['remitente']>(
    key: K,
    value: ExpedienteEdicionManual['remitente'][K],
  ) {
    setRegistro((prev) => (prev ? { ...prev, remitente: { ...prev.remitente, [key]: value } } : prev))
  }

  async function handleValidar() {
    if (!registro) return
    setLoading(true)
    setError(null)
    try {
      const resp = await validarEdicionManual(registro)
      setMensajes(resp.mensajes)
      setRegistro(resp.registro)
      setMostrarErrores(true)
    } catch (e) {
      setError(describeError(e))
    } finally {
      setLoading(false)
    }
  }

  async function handleGuardar() {
    if (!registro) return
    setLoading(true)
    setError(null)
    setMostrarErrores(true)
    try {
      // Revalida con los datos actuales antes de guardar (18/08/2026, pedido explícito del usuario:
      // no debería permitir grabar con campos obligatorios vacíos) — mismo patrón que ya usaba
      // `PanelDatosExpediente.guardarEdicion` (edición inline): si queda cualquier mensaje (de campo
      // o de formulario), se bloquea el guardado — a diferencia de Registro manual (alta), en
      // Edición manual TODO mensaje bloquea, ya que `ExpedienteEdicionManualService.guardar` en el
      // backend ya rechaza igual con una excepción; esto solo adelanta el mismo resultado al cliente
      // con mensajes claros y los campos resaltados en rojo.
      const resp = await validarEdicionManual(registro)
      setMensajes(resp.mensajes)
      setRegistro(resp.registro)
      if (resp.mensajes.length > 0) {
        setError('Corrija los campos marcados en rojo antes de guardar.')
        return
      }
      const resultado = await guardarEdicionManual(resp.registro)
      setResultado(resultado)
      setMensajes([])
    } catch (e) {
      setError(describeError(e))
    } finally {
      setLoading(false)
    }
  }

  if (cargando) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
        <CircularProgress />
      </Box>
    )
  }

  if (resultado) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center' }}>
        <SectionCard title="">
          <Alert severity="success" sx={{ mb: 2 }}>
            {resultado.mensaje}
          </Alert>
          <Button variant="contained" onClick={() => navigate('/registro')}>
            Volver a Bandeja Registro
          </Button>
        </SectionCard>
      </Box>
    )
  }

  if (!registro) {
    return (
      <Box>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <Button variant="outlined" onClick={() => navigate('/registro')}>
          Volver a Bandeja Registro
        </Button>
      </Box>
    )
  }

  return (
    <Box>
      <Typography variant="h6" sx={{ color: sdrercColors.primary }}>
        Edición manual
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Expediente {registro.numeroExpediente || registro.numeroExpedienteVistaPrevia}. No se modifica el número de expediente.
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Stack spacing={3} sx={{ maxWidth: 900 }}>
        <RegistroExpedienteForm
          solicitud={registro.solicitud}
          acta={registro.acta}
          titular={registro.titular}
          remitente={registro.remitente}
          numeroExpedienteVistaPrevia={registro.numeroExpedienteVistaPrevia}
          posibleDuplicado={registro.posibleDuplicado}
          motivoDuplicado={registro.motivoDuplicado}
          numeroExpedienteSgdDuplicado={registro.numeroExpedienteSgdDuplicado}
          motivoNumeroExpedienteSgdDuplicado={registro.motivoNumeroExpedienteSgdDuplicado}
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
          numeroExpedienteLabel={registro.numeroExpediente || registro.numeroExpedienteVistaPrevia || 'Sin número'}
          mostrarErrores={mostrarErrores}
        />

        <SectionCard title="Resumen y confirmación">
          <Stack spacing={2}>
            <Typography variant="body2">
              <strong>Número expediente:</strong> {registro.numeroExpediente || registro.numeroExpedienteVistaPrevia}
            </Typography>
            {/* Mensajes de nivel de FORMULARIO — ver Javadoc de `resumenMensajesFormulario`
                (18/08/2026, pedido explícito del usuario): los de campo individual ya se muestran
                inline junto a cada campo; si TODOS los mensajes eran de campo, se muestra un aviso
                genérico en vez de dar a entender que no hay ningún problema. */}
            <TextField
              label="Errores / advertencias del formulario"
              multiline
              minRows={2}
              fullWidth
              disabled
              value={resumenFormulario.texto}
              sx={resumenFormulario.resaltado ? { '& .MuiInputBase-root': { bgcolor: sdrercColors.softRed } } : undefined}
            />
          </Stack>
        </SectionCard>

        <Stack direction="row" spacing={2}>
          <Button variant="outlined" onClick={handleValidar} disabled={loading}>
            Validar
          </Button>
          <Button variant="contained" onClick={handleGuardar} disabled={loading}>
            Guardar cambios
          </Button>
          <Button variant="text" onClick={() => navigate('/registro')} disabled={loading}>
            Cancelar
          </Button>
        </Stack>
      </Stack>
    </Box>
  )
}

function describeError(e: unknown): string {
  if (e instanceof ApiError) return e.message
  if (e instanceof Error) return e.message
  return 'Ocurrió un error inesperado.'
}
