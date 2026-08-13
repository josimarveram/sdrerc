import { useEffect, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from '@mui/material'
import { asociarRelacionados, listarSolicitudesDelGrupo, type ExpedienteRelacionado } from '../api/expedienteRelacionadoApi'
import { generarNumeroExpediente } from '../api/asignacionApi'
import { ApiError } from '../api/http'

interface AsociarDuplicadosContentProps {
  idExpedientePrincipal: number
  onAsociado?: () => void
}

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

/**
 * Contenido de "Asociar" sin envoltorio de `Dialog` — extraído de `AsociarDuplicadosPanel.tsx` para
 * poder embeberse como subpestaña dentro de `WorkspaceTabsDrawer` (Bandeja Registro).
 * `AsociarDuplicadosPanel.tsx` sigue existiendo como envoltorio `Dialog` delgado sobre este mismo
 * componente, usado sin cambios por Asignación.
 *
 * Rediseñado el 12/08/2026 para reflejar el panel "Asociar" real de V2
 * (`JPanelAsignacionV2.crearPanelAsociar`: secciones "Selección y alertas"/"Asociación
 * rápida"/"Decisión de número"), tras detectar que la primera versión se basó en un diseño
 * simplificado que no correspondía al actual:
 * - Tabla "Solicitudes asociadas" con las mismas columnas que V2 (`documentosRelacionadosModel`):
 *   N° Expediente, N° SGD, Fecha Solicitud, Tipo/N° Acta, Procedimiento, Etapa/Estado, Abogado —
 *   sin Titular (todas las filas comparten el mismo, por definición del match) ni Alertas (V2
 *   tampoco la muestra en esta tabla).
 * - Un solo botón "Asociar" (antes había 2: "Asociación rápida" + "Asociar seleccionados" — V2 en
 *   realidad tiene un único botón que actúa sobre lo marcado, con texto/conteo dinámico).
 * - Se quitó la tabla "Asociados confirmados": V2 no la muestra dentro de este panel — los ya
 *   confirmados se ven expandiendo la fila principal en la grilla (ver `RegistroBandejaPage.tsx`,
 *   icono "+"), que ya está construido; mostrarla acá también sería redundante.
 * - Nueva sección "Decisión de número" (`btnGenerarNumeroExpediente` en V2): genera un número de
 *   expediente independiente para el expediente principal de este workspace cuando todavía no
 *   tiene uno (`AsignacionExpedienteDAO.generarNumeroExpediente`, recién portado a V3).
 *
 * Simplificación deliberada respecto a V2 (no bug): V2 también permite generar número en lote para
 * varios candidatos marcados a la vez y tiene una UI de resolución de "principal ambiguo" cuando
 * hay más de un candidato con número. Ninguna de las dos se replicó: el backend
 * (`ExpedienteRelacionadoDAO.resolverOrientacionRelacion`) ya resuelve automáticamente, por cada
 * par, cuál expediente es el principal real (el que tiene número; si ninguno/ambos tienen, el más
 * antiguo) — así que la ambigüedad que V2 resuelve a mano en la UI acá no hace falta mostrarla.
 */
export function AsociarDuplicadosContent({ idExpedientePrincipal, onAsociado }: AsociarDuplicadosContentProps) {
  const [candidatos, setCandidatos] = useState<ExpedienteRelacionado[]>([])
  const [seleccionados, setSeleccionados] = useState<number[]>([])
  const [loading, setLoading] = useState(false)
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [mensaje, setMensaje] = useState<string | null>(null)

  function cargar() {
    setLoading(true)
    setError(null)
    listarSolicitudesDelGrupo(idExpedientePrincipal)
      .then((grupo) => {
        setCandidatos(grupo)
        setSeleccionados([])
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudo cargar la información de asociación.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    if (idExpedientePrincipal) {
      cargar()
      setMensaje(null)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idExpedientePrincipal])

  function alternarSeleccion(id: number) {
    setSeleccionados((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]))
  }

  async function ejecutarAsociacion() {
    if (seleccionados.length === 0) return
    setProcesando(true)
    setError(null)
    setMensaje(null)
    try {
      const resultado = await asociarRelacionados({
        idExpedientePrincipal,
        idsRelacionados: seleccionados,
        descripcion: 'Documento duplicado asociado al expediente principal por misma acta y titular.',
      })
      setMensaje(resultado.mensaje)
      cargar()
      onAsociado?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo completar la asociación.')
    } finally {
      setProcesando(false)
    }
  }

  async function handleGenerarNumero() {
    setProcesando(true)
    setError(null)
    setMensaje(null)
    try {
      const resultado = await generarNumeroExpediente(idExpedientePrincipal)
      setMensaje(`Número de expediente generado: ${resultado.numeroExpediente}`)
      cargar()
      onAsociado?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo generar el número de expediente.')
    } finally {
      setProcesando(false)
    }
  }

  const principal = candidatos.find((c) => c.idExpediente === idExpedientePrincipal)
  const requiereDecisionNumero = principal !== undefined && !principal.numeroExpediente

  return (
    <Stack spacing={2}>
      {error && <Alert severity="error">{error}</Alert>}
      {mensaje && (
        <Alert severity="success" onClose={() => setMensaje(null)}>
          {mensaje}
        </Alert>
      )}

      {/* "Selección y alertas" (V2) */}
      <Box>
        <Typography variant="subtitle2">Solicitudes asociadas</Typography>
        <Typography variant="caption" color="text.secondary">
          Se muestran todas las solicitudes con la misma acta y titular, incluida la principal. Marque las que deban asociarse al
          expediente principal.
        </Typography>
      </Box>
      {loading ? (
        <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
          Cargando...
        </Typography>
      ) : candidatos.length === 0 ? (
        <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
          No hay solicitudes relacionadas para este expediente.
        </Typography>
      ) : (
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox" />
                <TableCell>N° Expediente</TableCell>
                <TableCell>N° SGD</TableCell>
                <TableCell>Fecha Solicitud</TableCell>
                <TableCell>Tipo/N° Acta</TableCell>
                <TableCell>Procedimiento</TableCell>
                <TableCell>Etapa/Estado</TableCell>
                <TableCell>Abogado</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {candidatos.map((c) => {
                const esPrincipal = c.idExpediente === idExpedientePrincipal
                return (
                  <TableRow key={c.idExpediente} selected={esPrincipal}>
                    <TableCell padding="checkbox">
                      {esPrincipal ? (
                        <Tooltip title="Expediente principal actual de este workspace.">
                          <Typography variant="caption" color="text.secondary">
                            Principal
                          </Typography>
                        </Tooltip>
                      ) : (
                        <Checkbox size="small" checked={seleccionados.includes(c.idExpediente)} onChange={() => alternarSeleccion(c.idExpediente)} />
                      )}
                    </TableCell>
                    <TableCell>{c.numeroExpediente || '-'}</TableCell>
                    <TableCell>{c.numeroExpedienteSgd || '-'}</TableCell>
                    <TableCell>{formatearFecha(c.fechaRecepcion)}</TableCell>
                    <TableCell>{[c.tipoActa, c.numeroActa].filter(Boolean).join(' ') || '-'}</TableCell>
                    <TableCell>{c.procedimiento || '-'}</TableCell>
                    <TableCell>{c.etapaEstado || '-'}</TableCell>
                    <TableCell>{c.abogadoAsignado || '-'}</TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* "Asociación rápida" (V2): un solo botón, texto/conteo dinámico según lo marcado */}
      <Button variant="contained" disabled={procesando || seleccionados.length === 0} onClick={ejecutarAsociacion} sx={{ alignSelf: 'flex-start' }}>
        {seleccionados.length > 0 ? `Asociar al principal (${seleccionados.length})` : 'Sin relacionados marcados'}
      </Button>

      <Typography variant="caption" color="text.secondary">
        Los expedientes ya asociados se ven expandiendo la fila principal en la Bandeja Registro (icono "+").
      </Typography>

      {/* "Decisión de número" (V2): solo aplica si el expediente principal de este workspace
          todavía no tiene número. */}
      {requiereDecisionNumero && (
        <Box sx={{ pt: 1, borderTop: 1, borderColor: 'divider' }}>
          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            Decisión de número
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>
            Use esta opción solo si esta solicitud NO corresponde asociarla a otro expediente como duplicado.
          </Typography>
          <Button variant="outlined" disabled={procesando} onClick={handleGenerarNumero}>
            Generar número de expediente
          </Button>
        </Box>
      )}
    </Stack>
  )
}
