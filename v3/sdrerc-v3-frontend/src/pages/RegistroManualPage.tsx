import { useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, Paper, Stack, TextField, Typography } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import {
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
  registrarManual,
  registroVacio,
  validarRegistroManual,
  type CatalogoItem,
  type CentroAtencion,
  type Municipalidad,
  type RegistroManualExpediente,
  type RegistroManualResultado,
  type UbigeoItem,
  type UnidadOrganica,
} from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { RegistroExpedienteForm, SectionCard, resumenMensajesFormulario, tieneErroresDeCampo } from '../components/RegistroExpedienteForm'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { REGISTRO_TABS } from '../layout/moduleTabs'

/**
 * Equivalente web de JPanelRegistroManualRecepcionV2 (V2) en modo alta, mismos campos y misma
 * agrupación de secciones (pedido explícito del usuario, 11/08/2026 — ver Anexo A del plan de
 * migración: "paridad de datos/campos, libertad de diseño"). Orden real de V2: Validación inicial
 * -> Datos de solicitud -> Datos del expediente + Datos del acta -> Titular -> Solicitante ->
 * Datos de notificación y ubicación (en realidad datos de contacto/ubigeo del SOLICITANTE, no del
 * titular) -> Resumen y confirmación. Las secciones 1-6 viven en el componente compartido
 * `RegistroExpedienteForm` (también usado por `RegistroEdicionManualPage`, igual que V2 reutiliza
 * literalmente el mismo panel Swing en ambos modos).
 */
export function RegistroManualPage() {
  const navigate = useNavigate()
  const [registro, setRegistro] = useState<RegistroManualExpediente>(registroVacio())
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
  const [loading, setLoading] = useState(false)
  // Fuerza a mostrar en rojo los campos obligatorios vacíos al intentar Validar/Registrar, aunque el
  // usuario nunca los haya tocado (18/08/2026, pedido explícito del usuario) — ver Javadoc de
  // `RegistroExpedienteFormProps.mostrarErrores`.
  const [mostrarErrores, setMostrarErrores] = useState(false)
  // Texto + resaltado del cuadro "Errores / advertencias del formulario" — ver Javadoc de
  // `resumenMensajesFormulario` (18/08/2026, pedido explícito del usuario).
  const resumenFormulario = useMemo(() => resumenMensajesFormulario(mensajes), [mensajes])

  useEffect(() => {
    Promise.all([
      listarCanalesRecepcion(),
      listarProcedimientosRegistrales(),
      listarTiposDocumento(),
      listarTiposActa(),
      listarDepartamentos(),
      listarCentrosAtencion(),
      listarUnidadesOrganicas(),
      listarMunicipalidades(),
    ])
      .then(([c, p, d, a, dep, ca, uo, muni]) => {
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
  }, [])

  useEffect(() => {
    if (!registro.remitente.idDepartamento) {
      setProvincias([])
      return
    }
    listarProvincias(registro.remitente.idDepartamento)
      .then(setProvincias)
      .catch((e) => setError(describeError(e)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [registro.remitente.idDepartamento])

  useEffect(() => {
    if (!registro.remitente.idProvincia) {
      setDistritos([])
      return
    }
    listarDistritos(registro.remitente.idProvincia)
      .then(setDistritos)
      .catch((e) => setError(describeError(e)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [registro.remitente.idProvincia])

  function updateSolicitud<K extends keyof RegistroManualExpediente['solicitud']>(
    key: K,
    value: RegistroManualExpediente['solicitud'][K],
  ) {
    setRegistro((prev) => ({ ...prev, solicitud: { ...prev.solicitud, [key]: value } }))
  }

  function updateActa<K extends keyof RegistroManualExpediente['acta']>(key: K, value: RegistroManualExpediente['acta'][K]) {
    setRegistro((prev) => ({ ...prev, acta: { ...prev.acta, [key]: value } }))
  }

  function updateTitular<K extends keyof RegistroManualExpediente['titular']>(
    key: K,
    value: RegistroManualExpediente['titular'][K],
  ) {
    setRegistro((prev) => ({ ...prev, titular: { ...prev.titular, [key]: value } }))
  }

  function updateRemitente<K extends keyof RegistroManualExpediente['remitente']>(
    key: K,
    value: RegistroManualExpediente['remitente'][K],
  ) {
    setRegistro((prev) => ({ ...prev, remitente: { ...prev.remitente, [key]: value } }))
  }

  async function handleValidar() {
    setLoading(true)
    setError(null)
    setResultado(null)
    try {
      const resp = await validarRegistroManual(registro)
      setMensajes(resp.mensajes)
      setRegistro(resp.registro)
      setMostrarErrores(true)
    } catch (e) {
      setError(describeError(e))
    } finally {
      setLoading(false)
    }
  }

  async function handleRegistrar() {
    setLoading(true)
    setError(null)
    setMostrarErrores(true)
    try {
      // Revalida con los datos actuales antes de registrar (18/08/2026, pedido explícito del
      // usuario: "al hacer clic en el botón Registrar no debería permitir grabar" si faltan campos
      // obligatorios) — `tieneErroresDeCampo` bloquea solo por mensajes de campo (obligatorio/
      // formato); advertencias de nivel de formulario (posible duplicado, posible grupo familiar,
      // "requiere decisión de asignación") siguen sin bloquear, igual que V2: el expediente se
      // registra y queda marcado para revisión/asociación posterior.
      const resp = await validarRegistroManual(registro)
      setMensajes(resp.mensajes)
      setRegistro(resp.registro)
      if (tieneErroresDeCampo(resp.mensajes)) {
        setError('Corrija los campos obligatorios marcados en rojo antes de registrar.')
        return
      }
      const resultado = await registrarManual(resp.registro)
      setResultado(resultado)
      setMensajes([])
    } catch (e) {
      setError(describeError(e))
    } finally {
      setLoading(false)
    }
  }

  if (resultado) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center' }}>
        <Paper elevation={1} sx={{ p: 4, borderRadius: 2, maxWidth: 480, alignSelf: 'flex-start', mt: 8 }}>
          <Alert severity="success" sx={{ mb: 2 }}>
            {resultado.mensaje}
          </Alert>
          <Typography variant="body1" sx={{ mb: 3 }}>
            <strong>Número de expediente:</strong> {resultado.numeroExpediente || '(sin número)'}
          </Typography>
          <Stack direction="row" spacing={2}>
            <Button
              variant="contained"
              onClick={() => {
                setRegistro(registroVacio())
                setResultado(null)
                setMostrarErrores(false)
              }}
            >
              Registrar otro
            </Button>
            <Button variant="outlined" onClick={() => navigate('/')}>
              Volver al inicio
            </Button>
          </Stack>
        </Paper>
      </Box>
    )
  }

  return (
    <PageHeaderSticky titulo="Registro manual" subtitulo="Alta de un expediente nuevo en SDRERC_APP." tabs={REGISTRO_TABS}>
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
          numeroExpedienteLabel={registro.numeroExpedienteVistaPrevia ?? 'Se genera al registrar'}
          mostrarErrores={mostrarErrores}
        />

        {/* 7. Resumen y confirmación */}
        <SectionCard title="Resumen y confirmación">
          <Stack spacing={2}>
            <Typography variant="body2">
              <strong>Número expediente:</strong> {registro.numeroExpedienteVistaPrevia ?? 'Se genera al registrar'}
            </Typography>
            {/* Mensajes de nivel de FORMULARIO (13/08/2026, pedido explícito del usuario): los de
                campo individual (obligatorio/formato) ya se muestran inline junto a cada campo vía
                `ValidatedTextField`, así que no se repiten acá — solo duplicados por acta+titular/
                SGD contra la base, posible grupo familiar, "requiere decisión de asignación",
                errores de conexión. Si TODOS los mensajes eran de campo, `resumenMensajesFormulario`
                muestra un aviso genérico en vez de dar a entender que no hay ningún problema
                (18/08/2026, pedido explícito del usuario). */}
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
          <Button variant="contained" onClick={handleRegistrar} disabled={loading}>
            Registrar
          </Button>
          <Button
            variant="text"
            onClick={() => {
              setRegistro(registroVacio())
              setMensajes([])
              setMostrarErrores(false)
            }}
          >
            Limpiar
          </Button>
        </Stack>
      </Stack>
    </PageHeaderSticky>
  )
}

function describeError(e: unknown): string {
  if (e instanceof ApiError) return e.message
  if (e instanceof Error) return e.message
  return 'Ocurrió un error inesperado.'
}
