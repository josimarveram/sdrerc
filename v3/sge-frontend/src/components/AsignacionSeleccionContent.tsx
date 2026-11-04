import { useEffect, useState } from 'react'
import {
  Alert,
  Button,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import {
  asignarExpedientes,
  listarAbogados,
  listarEquipos,
  listarHistorialAsignaciones,
  reasignarExpediente,
  type AsignacionExpediente,
  type AsignacionHistorial,
  type EquipoAsignacion,
  type UsuarioAsignable,
} from '../api/asignacionApi'
import { ApiError } from '../api/http'
import { useAppDialog } from '../dialogs/AppDialogProvider'
import { SectionCard } from './RegistroExpedienteForm'
import { StatusBadge } from './StatusBadge'

interface AsignacionSeleccionContentProps {
  expedientes: AsignacionExpediente[]
  mostrarHistorial: boolean
  onAsignado?: () => void
}

interface HistorialConExpediente extends AsignacionHistorial {
  idExpediente: number
  numeroExpediente: string
}

function formatearFechaHora(iso: string | null): string {
  if (!iso) return '-'
  const [fecha, hora] = iso.split('T')
  const [anio, mes, dia] = fecha.split('-')
  const hhmm = hora ? hora.slice(0, 5) : '00:00'
  return `${dia}/${mes}/${anio} ${hhmm}`
}

/**
 * Contenido de la subpestaña "Asignación" de la Bandeja Asignación (20/08/2026, pedido explícito
 * del usuario — fusiona `AsignacionExpedienteContent.tsx` (modo individual, 1 expediente) y
 * `AsignacionMultipleContent.tsx` (modo lote, N expedientes de titulares mezclados) en un solo
 * componente): ambos modos comparten exactamente la misma mini-grilla "Expedientes seleccionados
 * (N)" (Nro. Expediente/N° expediente SGD/Hoja de envío actual/Abogado actual/Tipo de
 * operación/Hoja de envío nueva) y el mismo bloque "Destino operativo" (Abogado de solo
 * lectura/Equipo destino/Abogado responsable/Supervisor) — pedido explícito del usuario: "debe ser
 * exactamente igual [entre ambos modos] a diferencia que en el panel de asignación individual sí
 * aparece el historial de asignación / reasignación".
 *
 * **`mostrarHistorial`** es la ÚNICA diferencia visual real entre los 2 casos de uso que abren este
 * componente desde `AsignacionBandejaPage.tsx`:
 * - `true`: workspace "individual por titular" (`kind: 'individualAsignacion'`) — se abre cuando la
 *   selección de checkboxes de la grilla, al hacer clic en "Asignar", pertenece TODA al mismo
 *   titular (1 o más expedientes de esa misma persona; la grilla ya restringe que otros titulares se
 *   puedan marcar mientras este workspace está abierto, ver `isRowSelectable` en
 *   `AsignacionBandejaPage.tsx`). Muestra el bloque "Historial de asignación / reasignación" al
 *   final, con las mismas 7 columnas de siempre; si `expedientes.length > 1` (2+ expedientes del
 *   mismo titular seleccionados a la vez), se agrega una 8va columna "N° Expediente" al ppio de la
 *   tabla (para no generar ambigüedad sobre a cuál expediente pertenece cada fila del historial
 *   combinado) — con exactamente 1 expediente (el caso normal) la tabla queda visualmente idéntica a
 *   la versión anterior de `AsignacionExpedienteContent.tsx`.
 * - `false`: workspace "Selección múltiple" (`kind: 'multiple'`, titulares mezclados) — sin
 *   Historial (cada expediente del lote tiene su propio historial individual, no uno compartido; se
 *   sigue viendo expediente por expediente entrando a su propio workspace por titular).
 *
 * El campo "Abogado" de solo lectura (primera fila de "Destino operativo") adapta su texto según el
 * caso: con exactamente 1 expediente, muestra el estado real de asignación de ESE expediente (igual
 * que el `AsignacionExpedienteContent.tsx` original); con 2+ del mismo titular, un texto genérico
 * "Varios expedientes de {titular}"; con 2+ de titulares mezclados, "Selección múltiple" (texto
 * original de `AsignacionMultipleContent.tsx`).
 *
 * **Sin campo "Comentario"** (pedido explícito del usuario, pasadas anteriores del mismo día): no
 * existía en V2 (código huérfano allá, ver `v3/CLAUDE.md`) — `asignarExpedientes`/
 * `reasignarExpediente` siguen aceptando el parámetro `comentario` en su firma (compartida con el
 * resto del sistema), acá se envía como cadena vacía.
 */
export function AsignacionSeleccionContent({ expedientes, mostrarHistorial, onAsignado }: AsignacionSeleccionContentProps) {
  const { confirm: confirmDialog } = useAppDialog()
  const [equipos, setEquipos] = useState<EquipoAsignacion[]>([])
  const [abogados, setAbogados] = useState<UsuarioAsignable[]>([])
  const [idEquipo, setIdEquipo] = useState<number | ''>('')
  const [idAbogado, setIdAbogado] = useState<number | ''>('')
  const [hojasEnvio, setHojasEnvio] = useState<Record<number, string>>({})
  const [asignando, setAsignando] = useState(false)
  const [mensaje, setMensaje] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const [historialItems, setHistorialItems] = useState<HistorialConExpediente[]>([])
  const [historialLoading, setHistorialLoading] = useState(false)

  const abogadoSeleccionado = abogados.find((a) => a.idUsuario === idAbogado) ?? null

  useEffect(() => {
    listarEquipos().then(setEquipos).catch(() => setEquipos([]))
  }, [])

  useEffect(() => {
    if (idEquipo === '') {
      setAbogados([])
      setIdAbogado('')
      return
    }
    listarAbogados(idEquipo).then(setAbogados).catch(() => setAbogados([]))
    setIdAbogado('')
  }, [idEquipo])

  useEffect(() => {
    if (!mostrarHistorial || expedientes.length === 0) {
      setHistorialItems([])
      return
    }
    setHistorialLoading(true)
    Promise.all(
      expedientes.map((exp) =>
        listarHistorialAsignaciones(exp.idExpediente).then((items) =>
          items.map((item) => ({ ...item, idExpediente: exp.idExpediente, numeroExpediente: exp.numeroExpediente })),
        ),
      ),
    )
      .then((listas) => {
        const combinado = listas.flat().sort((a, b) => (b.fechaAsignacion || '').localeCompare(a.fechaAsignacion || ''))
        setHistorialItems(combinado)
      })
      .catch(() => setHistorialItems([]))
      .finally(() => setHistorialLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mostrarHistorial, expedientes])

  function limpiar() {
    setIdEquipo('')
    setIdAbogado('')
    setHojasEnvio({})
    setError(null)
  }

  async function handleGenerarAsignacion() {
    if (idEquipo === '' || idAbogado === '') return
    const idsNuevos = expedientes.filter((e) => !e.asignacionActiva).map((e) => e.idExpediente)
    const idsReasignar = expedientes.filter((e) => e.asignacionActiva).map((e) => e.idExpediente)
    if (idsNuevos.length === 0 && idsReasignar.length === 0) return

    if (idsReasignar.length > 0) {
      const faltantes = idsReasignar.filter((id) => !hojasEnvio[id]?.trim())
      if (faltantes.length > 0) {
        setError('Ingrese la hoja de envío para cada expediente a reasignar (es obligatoria en reasignación, a diferencia de una asignación nueva).')
        return
      }
      const confirmado = await confirmDialog(
        `Va a reasignar ${idsReasignar.length} expediente(s) que ya tienen un abogado asignado. Se conservará el historial de la asignación anterior.`,
        { tone: 'warning', confirmLabel: 'Reasignar' },
      )
      if (!confirmado) return
    }

    setAsignando(true)
    setMensaje(null)
    setError(null)
    const nombreAbogado = abogadoSeleccionado?.displayName || 'el abogado seleccionado'
    try {
      const resumenPartes: string[] = []
      if (idsNuevos.length > 0) {
        await asignarExpedientes({
          idsExpediente: idsNuevos,
          idEquipo,
          idAbogado,
          comentario: '',
          hojasEnvioPorExpediente: hojasEnvio,
        })
        resumenPartes.push(`${idsNuevos.length} expediente(s) asignado(s)`)
      }
      const erroresReasignacion: string[] = []
      if (idsReasignar.length > 0) {
        let exitosas = 0
        for (const id of idsReasignar) {
          try {
            await reasignarExpediente({
              idExpediente: id,
              idEquipo,
              idAbogado,
              numeroHojaEnvio: hojasEnvio[id] ?? '',
              comentario: '',
            })
            exitosas++
          } catch (e) {
            const numero = expedientes.find((exp) => exp.idExpediente === id)?.numeroExpediente ?? id
            erroresReasignacion.push(`${numero}: ${e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'}`)
          }
        }
        resumenPartes.push(`${exitosas} expediente(s) reasignado(s)`)
      }
      let textoMensaje = `${resumenPartes.join(', ')} correctamente a ${nombreAbogado}.`
      if (erroresReasignacion.length > 0) {
        textoMensaje += `\n${erroresReasignacion.length} reasignación(es) no se pudieron completar:\n- ${erroresReasignacion.join('\n- ')}`
      }
      setMensaje(textoMensaje)
      limpiar()
      onAsignado?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.')
    } finally {
      setAsignando(false)
    }
  }

  const tituloAbogado = (() => {
    if (expedientes.length === 0) return '-'
    if (expedientes.length === 1) {
      const exp = expedientes[0]
      return exp.asignacionActiva ? `${exp.abogadoAsignado || '-'} (reasignación)` : 'Sin asignación previa (nuevo)'
    }
    if (mostrarHistorial) {
      return `Varios expedientes de ${expedientes[0].titular || 'el mismo titular'}`
    }
    return 'Selección múltiple'
  })()

  const mostrarColumnaExpediente = expedientes.length > 1

  return (
    <Stack spacing={2}>
      {error && (
        <Alert severity="error" sx={{ whiteSpace: 'pre-line' }}>
          {error}
        </Alert>
      )}
      {mensaje && (
        <Alert severity="success" sx={{ whiteSpace: 'pre-line' }} onClose={() => setMensaje(null)}>
          {mensaje}
        </Alert>
      )}

      <SectionCard title={`Expedientes seleccionados (${expedientes.length})`}>
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Nro. Expediente</TableCell>
                <TableCell>N° expediente SGD</TableCell>
                <TableCell>Hoja de envío actual</TableCell>
                <TableCell>Abogado actual</TableCell>
                <TableCell>Tipo de operación</TableCell>
                <TableCell>Hoja de envío nueva</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {expedientes.map((exp) => (
                <TableRow key={exp.idExpediente}>
                  <TableCell>{exp.numeroExpediente}</TableCell>
                  <TableCell>{exp.numeroExpedienteSgd || '-'}</TableCell>
                  <TableCell>{exp.numeroHojaEnvioAsignacion || '-'}</TableCell>
                  <TableCell>{exp.abogadoAsignado || '-'}</TableCell>
                  <TableCell>
                    {exp.asignacionActiva ? (
                      <StatusBadge label="Reasignación" tone="warning" />
                    ) : (
                      <StatusBadge label="Asignación inicial" tone="info" />
                    )}
                  </TableCell>
                  <TableCell>
                    <TextField
                      size="small"
                      required={exp.asignacionActiva}
                      placeholder="N° hoja de envío"
                      value={hojasEnvio[exp.idExpediente] ?? ''}
                      onChange={(e) => setHojasEnvio((prev) => ({ ...prev, [exp.idExpediente]: e.target.value }))}
                      sx={{ minWidth: 180 }}
                    />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </SectionCard>

      <SectionCard title="Destino operativo">
        <Stack spacing={2}>
          <TextField label="Abogado" size="small" fullWidth value={tituloAbogado} slotProps={{ input: { readOnly: true } }} />
          <TextField
            select
            label="Equipo destino"
            size="small"
            fullWidth
            value={idEquipo}
            onChange={(e) => setIdEquipo(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <MenuItem value="">Seleccione equipo</MenuItem>
            {equipos.map((eq) => (
              <MenuItem key={eq.idEquipo} value={eq.idEquipo}>
                {eq.displayName}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label="Abogado responsable"
            size="small"
            fullWidth
            value={idAbogado}
            disabled={idEquipo === ''}
            onChange={(e) => setIdAbogado(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <MenuItem value="">Seleccione abogado</MenuItem>
            {abogados.map((ab) => (
              <MenuItem key={ab.idUsuario} value={ab.idUsuario}>
                {ab.displayName}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            label="Supervisor"
            size="small"
            fullWidth
            value={abogadoSeleccionado?.supervisorNombre || '-'}
            slotProps={{ input: { readOnly: true } }}
          />
        </Stack>
      </SectionCard>

      <Stack direction="row" spacing={1}>
        <Button
          variant="contained"
          onClick={handleGenerarAsignacion}
          disabled={asignando || idEquipo === '' || idAbogado === '' || expedientes.length === 0}
        >
          Generar asignación
        </Button>
        <Button variant="outlined" onClick={limpiar} disabled={asignando}>
          {mostrarHistorial ? 'Limpiar' : 'Cancelar'}
        </Button>
      </Stack>

      {mostrarHistorial && (
        <SectionCard title="Historial de asignación / reasignación">
          {historialLoading ? (
            <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
              Cargando...
            </Typography>
          ) : historialItems.length === 0 ? (
            <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
              Sin historial de asignación registrado.
            </Typography>
          ) : (
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    {mostrarColumnaExpediente && <TableCell>N° Expediente</TableCell>}
                    <TableCell>Tipo</TableCell>
                    <TableCell>Abogado</TableCell>
                    <TableCell>Equipo</TableCell>
                    <TableCell>Hoja de envío</TableCell>
                    <TableCell>Fecha</TableCell>
                    <TableCell>Asignado por</TableCell>
                    <TableCell>Estado</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {historialItems.map((item) => (
                    <TableRow key={`${item.idExpediente}-${item.idExpedienteAsignacion}`}>
                      {mostrarColumnaExpediente && <TableCell>{item.numeroExpediente}</TableCell>}
                      <TableCell>{item.reasignacionExcepcional ? 'Reasignación' : 'Asignación inicial'}</TableCell>
                      <TableCell>{item.abogado || '-'}</TableCell>
                      <TableCell>{item.equipo || '-'}</TableCell>
                      <TableCell>{item.numeroHojaEnvio || '-'}</TableCell>
                      <TableCell>{formatearFechaHora(item.fechaAsignacion)}</TableCell>
                      <TableCell>{item.asignadoPor || '-'}</TableCell>
                      <TableCell>{item.activa ? 'Activa' : 'Histórica'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </SectionCard>
      )}
    </Stack>
  )
}
