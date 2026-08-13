import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  Checkbox,
  IconButton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import {
  asociarGrupoFamiliar,
  buscarPosiblesIntegrantesManual,
  eliminarDeGrupoFamiliar,
  listarIntegrantesGrupoFamiliar,
  listarPosiblesIntegrantes,
  type GrupoFamiliarCandidato,
  type GrupoFamiliarIntegrante,
} from '../api/grupoFamiliarApi'
import { ApiError } from '../api/http'

interface GrupoFamiliarContentProps {
  idExpedientePrincipal: number
  onAsociado?: () => void
}

/**
 * Contenido de "Grupo Familiar" sin envoltorio de `Dialog` — extraído de `GrupoFamiliarPanel.tsx`
 * para poder embeberse como subpestaña dentro de `WorkspaceTabsDrawer` (Bandeja Registro). Misma
 * lógica exacta, solo cambia el ciclo de vida: se carga al montar/cambiar `idExpedientePrincipal`
 * en vez de estar condicionado a un `open` de Dialog. `GrupoFamiliarPanel.tsx` sigue existiendo
 * como envoltorio `Dialog` delgado sobre este mismo componente, usado sin cambios por Asignación.
 */
export function GrupoFamiliarContent({ idExpedientePrincipal, onAsociado }: GrupoFamiliarContentProps) {
  const [candidatos, setCandidatos] = useState<GrupoFamiliarCandidato[]>([])
  const [integrantes, setIntegrantes] = useState<GrupoFamiliarIntegrante[]>([])
  const [seleccionados, setSeleccionados] = useState<number[]>([])
  const [textoBusqueda, setTextoBusqueda] = useState('')
  const [loading, setLoading] = useState(false)
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [mensaje, setMensaje] = useState<string | null>(null)

  function cargar() {
    setLoading(true)
    setError(null)
    Promise.all([listarPosiblesIntegrantes(idExpedientePrincipal), listarIntegrantesGrupoFamiliar(idExpedientePrincipal)])
      .then(([cand, integ]) => {
        setCandidatos(cand)
        setIntegrantes(integ)
        setSeleccionados([])
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudo cargar la información de grupo familiar.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    if (idExpedientePrincipal) {
      cargar()
      setMensaje(null)
      setTextoBusqueda('')
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idExpedientePrincipal])

  function alternarSeleccion(idExpediente: number) {
    setSeleccionados((prev) => (prev.includes(idExpediente) ? prev.filter((x) => x !== idExpediente) : [...prev, idExpediente]))
  }

  function handleBuscarManual() {
    if (!textoBusqueda.trim()) return
    setLoading(true)
    setError(null)
    buscarPosiblesIntegrantesManual(idExpedientePrincipal, textoBusqueda.trim())
      .then(setCandidatos)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudo buscar candidatos.'))
      .finally(() => setLoading(false))
  }

  async function handleAsociar() {
    if (seleccionados.length === 0) return
    setProcesando(true)
    setError(null)
    setMensaje(null)
    try {
      const resultado = await asociarGrupoFamiliar(idExpedientePrincipal, seleccionados)
      setMensaje(resultado.mensaje)
      cargar()
      onAsociado?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo asociar al grupo familiar.')
    } finally {
      setProcesando(false)
    }
  }

  async function handleRetirar(idExpediente: number) {
    setProcesando(true)
    setError(null)
    setMensaje(null)
    try {
      const resultado = await eliminarDeGrupoFamiliar(idExpediente)
      setMensaje(resultado.mensaje)
      cargar()
      onAsociado?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo retirar del grupo familiar.')
    } finally {
      setProcesando(false)
    }
  }

  return (
    <Stack spacing={2}>
      {error && <Alert severity="error">{error}</Alert>}
      {mensaje && (
        <Alert severity="success" onClose={() => setMensaje(null)}>
          {mensaje}
        </Alert>
      )}

      <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
        <TextField
          size="small"
          label="Buscar manualmente (N° expediente, SGD, titular)"
          value={textoBusqueda}
          onChange={(e) => setTextoBusqueda(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleBuscarManual()}
          sx={{ minWidth: 280 }}
        />
        <Button variant="outlined" size="small" onClick={handleBuscarManual} disabled={!textoBusqueda.trim()}>
          Buscar
        </Button>
        <Button variant="text" size="small" onClick={cargar}>
          Ver candidatos por apellidos
        </Button>
      </Stack>

      <Typography variant="subtitle2">Posibles integrantes</Typography>
      {loading ? (
        <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
          Cargando...
        </Typography>
      ) : candidatos.length === 0 ? (
        <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
          No se encontraron candidatos. Prueba con la búsqueda manual.
        </Typography>
      ) : (
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox" />
                <TableCell>N° Expediente</TableCell>
                <TableCell>Titular</TableCell>
                <TableCell>Etapa</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell>Abogado</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {candidatos.map((c) => (
                <TableRow key={c.idExpediente}>
                  <TableCell padding="checkbox">
                    <Checkbox size="small" checked={seleccionados.includes(c.idExpediente)} onChange={() => alternarSeleccion(c.idExpediente)} />
                  </TableCell>
                  <TableCell>{c.numeroExpediente || '-'}</TableCell>
                  <TableCell>{c.titular || '-'}</TableCell>
                  <TableCell>{c.etapaCodigo || '-'}</TableCell>
                  <TableCell>{c.estadoCodigo || '-'}</TableCell>
                  <TableCell>{c.abogadoAsignado || '-'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Button variant="contained" disabled={procesando || seleccionados.length === 0} onClick={handleAsociar} sx={{ alignSelf: 'flex-start' }}>
        Asociar al grupo familiar
      </Button>

      <Typography variant="subtitle2">Grupo familiar actual</Typography>
      {integrantes.length === 0 ? (
        <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
          Este expediente todavía no pertenece a un grupo familiar confirmado.
        </Typography>
      ) : (
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox" />
                <TableCell>Nombre</TableCell>
                <TableCell>N° Expediente</TableCell>
                <TableCell>Etapa</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell>Abogado</TableCell>
                <TableCell>Tipo Acta</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {integrantes.map((i) => (
                <TableRow key={i.idPersona}>
                  <TableCell padding="checkbox">
                    <Tooltip title="Retirar del grupo familiar">
                      <span>
                        <IconButton size="small" disabled={procesando || !i.idExpediente} onClick={() => i.idExpediente && handleRetirar(i.idExpediente)}>
                          <CloseIcon fontSize="small" />
                        </IconButton>
                      </span>
                    </Tooltip>
                  </TableCell>
                  <TableCell>{i.nombreCompleto || '-'}</TableCell>
                  <TableCell>{i.numeroExpediente || '-'}</TableCell>
                  <TableCell>{i.etapaCodigo || '-'}</TableCell>
                  <TableCell>{i.estadoCodigo || '-'}</TableCell>
                  <TableCell>{i.abogadoAsignado || '-'}</TableCell>
                  <TableCell>{i.tipoActa || '-'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Stack>
  )
}
