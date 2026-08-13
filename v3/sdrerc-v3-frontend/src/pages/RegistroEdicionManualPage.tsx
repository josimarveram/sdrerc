import { useEffect, useState } from 'react'
import { Alert, Box, Button, CircularProgress, Stack, TextField, Typography } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'
import {
  guardarEdicionManual,
  listarCanalesRecepcion,
  listarDepartamentos,
  listarDistritos,
  listarProcedimientosRegistrales,
  listarProvincias,
  listarTiposActa,
  listarTiposDocumento,
  obtenerExpedienteParaEdicion,
  validarEdicionManual,
  type CatalogoItem,
  type ExpedienteEdicionManual,
  type RegistroManualResultado,
  type UbigeoItem,
} from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { RegistroExpedienteForm, SectionCard } from '../components/RegistroExpedienteForm'

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
  const [departamentos, setDepartamentos] = useState<UbigeoItem[]>([])
  const [provincias, setProvincias] = useState<UbigeoItem[]>([])
  const [distritos, setDistritos] = useState<UbigeoItem[]>([])
  const [mensajes, setMensajes] = useState<string[]>([])
  const [resultado, setResultado] = useState<RegistroManualResultado | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [cargando, setCargando] = useState(true)
  const [loading, setLoading] = useState(false)

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
    ])
      .then(([exp, c, p, d, a, dep]) => {
        setRegistro(exp)
        setCanales(c)
        setProcedimientos(p)
        setTiposDocumento(d)
        setTiposActa(a)
        setDepartamentos(dep)
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
    try {
      const resp = await guardarEdicionManual(registro)
      setResultado(resp)
      setMensajes([])
    } catch (e) {
      setError(describeError(e))
    } finally {
      setLoading(false)
    }
  }

  if (cargando) {
    return (
      <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4, display: 'flex', justifyContent: 'center', mt: 8 }}>
        <CircularProgress />
      </Box>
    )
  }

  if (resultado) {
    return (
      <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4, display: 'flex', justifyContent: 'center' }}>
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
      <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4 }}>
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
    <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4 }}>
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
          departamentos={departamentos}
          provincias={provincias}
          distritos={distritos}
          numeroExpedienteLabel={registro.numeroExpediente || registro.numeroExpedienteVistaPrevia || 'Sin número'}
        />

        <SectionCard title="Resumen y confirmación">
          <Stack spacing={2}>
            <Typography variant="body2">
              <strong>Número expediente:</strong> {registro.numeroExpediente || registro.numeroExpedienteVistaPrevia}
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
