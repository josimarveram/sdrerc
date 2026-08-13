import { useEffect, useState } from 'react'
import { Alert, Box, Button, Paper, Stack, TextField, Typography } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import {
  listarCanalesRecepcion,
  listarDepartamentos,
  listarDistritos,
  listarProcedimientosRegistrales,
  listarProvincias,
  listarTiposActa,
  listarTiposDocumento,
  registrarManual,
  registroVacio,
  validarRegistroManual,
  type CatalogoItem,
  type RegistroManualExpediente,
  type RegistroManualResultado,
  type UbigeoItem,
} from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { RegistroExpedienteForm, SectionCard } from '../components/RegistroExpedienteForm'

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
  const [departamentos, setDepartamentos] = useState<UbigeoItem[]>([])
  const [provincias, setProvincias] = useState<UbigeoItem[]>([])
  const [distritos, setDistritos] = useState<UbigeoItem[]>([])
  const [mensajes, setMensajes] = useState<string[]>([])
  const [resultado, setResultado] = useState<RegistroManualResultado | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    Promise.all([
      listarCanalesRecepcion(),
      listarProcedimientosRegistrales(),
      listarTiposDocumento(),
      listarTiposActa(),
      listarDepartamentos(),
    ])
      .then(([c, p, d, a, dep]) => {
        setCanales(c)
        setProcedimientos(p)
        setTiposDocumento(d)
        setTiposActa(a)
        setDepartamentos(dep)
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
    } catch (e) {
      setError(describeError(e))
    } finally {
      setLoading(false)
    }
  }

  async function handleRegistrar() {
    setLoading(true)
    setError(null)
    try {
      const resp = await registrarManual(registro)
      setResultado(resp)
      setMensajes([])
    } catch (e) {
      setError(describeError(e))
    } finally {
      setLoading(false)
    }
  }

  if (resultado) {
    return (
      <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4, display: 'flex', justifyContent: 'center' }}>
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
    <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4 }}>
      <Typography variant="h6" sx={{ color: sdrercColors.primary }}>
        Registro manual
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Alta de un expediente nuevo en SDRERC_APP.
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
          departamentos={departamentos}
          provincias={provincias}
          distritos={distritos}
          numeroExpedienteLabel={registro.numeroExpedienteVistaPrevia ?? 'Se genera al registrar'}
        />

        {/* 7. Resumen y confirmación */}
        <SectionCard title="Resumen y confirmación">
          <Stack spacing={2}>
            <Typography variant="body2">
              <strong>Número expediente:</strong> {registro.numeroExpedienteVistaPrevia ?? 'Se genera al registrar'}
            </Typography>
            <TextField
              label="Errores / advertencias"
              multiline
              minRows={2}
              fullWidth
              disabled
              value={mensajes.length > 0 ? mensajes.join('\n') : 'Sin advertencias (valide primero).'}
              sx={mensajes.length > 0 ? { '& .MuiInputBase-root': { bgcolor: sdrercColors.softRed } } : undefined}
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
            }}
          >
            Limpiar
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
