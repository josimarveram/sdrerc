import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Dialog,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  MenuItem,
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
import { DataGrid, type GridColDef, type GridRowSelectionModel } from '@mui/x-data-grid'
import { useNavigate } from 'react-router-dom'
import {
  asignarExpedientes,
  buscarBandejaAsignacion,
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
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, alertaBadgeTone, diasBadgeTone } from '../components/StatusBadge'
import { AsociarDuplicadosPanel } from '../components/AsociarDuplicadosPanel'
import { GrupoFamiliarPanel } from '../components/GrupoFamiliarPanel'

type FiltroKpi = 'PENDIENTES' | 'POTENCIAL_DUPLICADO' | 'POSIBLE_GRUPO_FAMILIAR' | 'GRUPO_FAMILIAR_CONFIRMADO' | 'POR_VENCER' | 'VENCIDOS' | null

function hoyISO(): string {
  return new Date().toISOString().slice(0, 10)
}

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

// Mismo formato que DATE_HORA_FORMAT en JPanelAsignacionV2 (V2): "dd/MM/yyyy HH:mm".
function formatearFechaHora(iso: string | null): string {
  if (!iso) return '-'
  const [fecha, hora] = iso.split('T')
  const [anio, mes, dia] = fecha.split('-')
  const hhmm = hora ? hora.slice(0, 5) : '00:00'
  return `${dia}/${mes}/${anio} ${hhmm}`
}

const SIN_SELECCION: GridRowSelectionModel = { type: 'include', ids: new Set() }

/**
 * Equivalente web de la pestaña "Bandeja Asignación" (V2, lengueta principal de
 * JPanelAsignacionV2) + el bloque "Asignación de abogado" del panel derecho. Alcance hasta ahora
 * (Fase 3): listado + filtros + 6 KPIs, asignación inicial (equipo + abogado + hoja de envío
 * opcional por expediente + comentario) y reasignación de expedientes ya asignados detrás de
 * "Habilitar reasignación" (hoja de envío obligatoria ahí, con confirmación previa, mismo patrón
 * que V2: un lote de expedientes nuevos se asigna en batch, los ya asignados se reasignan uno por
 * uno con manejo de error individual), y el historial de asignaciones/reasignaciones por
 * expediente (botón "Ver" por fila, mismas 7 columnas que la sección "Historial de asignación /
 * reasignación" del panel derecho en V2 — aquí en un diálogo en vez de un panel lateral fijo,
 * porque V3 todavía no tiene el "Panel de datos" de Asignación), "Asociar" (botón "Asociar" por
 * fila, componente compartido con Bandeja Registro — ver `AsociarDuplicadosPanel.tsx`, mismo
 * servicio de asociación que usa V2 en ambos módulos) y "Grupo Familiar" (botón "Grupo Familiar"
 * por fila, componente compartido con Bandeja Registro — ver `GrupoFamiliarPanel.tsx`, mismo
 * criterio de reutilización). NO incluye todavía las sub-pestañas "Cartas de respuesta"/"Carga
 * Abogados" — quedan para incrementos posteriores.
 */
export function AsignacionBandejaPage() {
  const navigate = useNavigate()
  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState('')
  const [hasta, setHasta] = useState('')
  const [estado, setEstado] = useState('')
  const [limite, setLimite] = useState(200)
  const [expedientes, setExpedientes] = useState<AsignacionExpediente[]>([])
  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [equipos, setEquipos] = useState<EquipoAsignacion[]>([])
  const [abogados, setAbogados] = useState<UsuarioAsignable[]>([])
  const [idEquipo, setIdEquipo] = useState<number | ''>('')
  const [idAbogado, setIdAbogado] = useState<number | ''>('')
  const [comentario, setComentario] = useState('')
  const [hojasEnvio, setHojasEnvio] = useState<Record<number, string>>({})
  const [rowSelectionModel, setRowSelectionModel] = useState<GridRowSelectionModel>(SIN_SELECCION)
  const [asignando, setAsignando] = useState(false)
  const [mensajeAsignacion, setMensajeAsignacion] = useState<string | null>(null)
  const [modoReasignacion, setModoReasignacion] = useState(false)

  const [historialExpediente, setHistorialExpediente] = useState<AsignacionExpediente | null>(null)
  const [historialItems, setHistorialItems] = useState<AsignacionHistorial[]>([])
  const [historialLoading, setHistorialLoading] = useState(false)

  const [asociarExpediente, setAsociarExpediente] = useState<AsignacionExpediente | null>(null)
  const [grupoFamiliarExpediente, setGrupoFamiliarExpediente] = useState<AsignacionExpediente | null>(null)

  const buscar = useCallback(
    (params?: { texto?: string; desde?: string; hasta?: string; estado?: string }) => {
      setLoading(true)
      setError(null)
      buscarBandejaAsignacion({
        texto: params?.texto ?? texto,
        desde: params?.desde ?? desde,
        hasta: params?.hasta ?? hasta,
        estado: params?.estado ?? estado,
        limite,
      })
        .then(setExpedientes)
        .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
        .finally(() => setLoading(false))
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [texto, desde, hasta, estado, limite],
  )

  useEffect(() => {
    buscar()
    listarEquipos().then(setEquipos).catch(() => undefined)
    // eslint-disable-next-line react-hooks/exhaustive-deps
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

  function handleBuscar() {
    setFiltroKpi(null)
    buscar()
  }

  function handleLimpiar() {
    setTexto('')
    setDesde('')
    setHasta('')
    setEstado('')
    setFiltroKpi(null)
    buscar({ texto: '', desde: '', hasta: '', estado: '' })
  }

  function coincideFiltroKpi(item: AsignacionExpediente): boolean {
    const dias = item.diasRestantes
    switch (filtroKpi) {
      case 'PENDIENTES':
        return item.asignable
      case 'POTENCIAL_DUPLICADO':
        return item.potencialDuplicado
      case 'POSIBLE_GRUPO_FAMILIAR':
        return item.posibleGrupoFamiliar
      case 'GRUPO_FAMILIAR_CONFIRMADO':
        return item.grupoFamiliar
      case 'POR_VENCER':
        return dias !== null && dias >= 0 && dias <= 5
      case 'VENCIDOS':
        return dias !== null && dias < 0
      default:
        return true
    }
  }

  const kpis = useMemo(() => {
    let pendientes = 0
    let potencialDuplicado = 0
    let posibleGrupoFamiliar = 0
    let grupoFamiliarConfirmado = 0
    let porVencer = 0
    let vencidos = 0
    for (const item of expedientes) {
      if (item.asignable) pendientes++
      if (item.potencialDuplicado) potencialDuplicado++
      if (item.posibleGrupoFamiliar) posibleGrupoFamiliar++
      if (item.grupoFamiliar) grupoFamiliarConfirmado++
      const dias = item.diasRestantes
      if (dias !== null && dias < 0) vencidos++
      else if (dias !== null && dias <= 5) porVencer++
    }
    return { pendientes, potencialDuplicado, posibleGrupoFamiliar, grupoFamiliarConfirmado, porVencer, vencidos }
  }, [expedientes])

  const filas = useMemo(() => expedientes.filter(coincideFiltroKpi), [expedientes, filtroKpi])

  function alternarFiltro(valor: FiltroKpi) {
    setFiltroKpi((prev) => (prev === valor ? null : valor))
  }

  const idsSeleccionados = useMemo(() => {
    return filas
      .filter((f) =>
        rowSelectionModel.type === 'include'
          ? rowSelectionModel.ids.has(f.idExpediente)
          : !rowSelectionModel.ids.has(f.idExpediente),
      )
      .map((f) => f.idExpediente)
  }, [filas, rowSelectionModel])

  const expedientesSeleccionados = useMemo(
    () => expedientes.filter((e) => idsSeleccionados.includes(e.idExpediente)),
    [expedientes, idsSeleccionados],
  )

  // Mismo criterio que ejecutarAsignacion en V2: los ya asignados se reasignan, el resto se asigna
  // por primera vez — ambos grupos pueden ir mezclados en la misma selección/acción.
  const idsNuevos = useMemo(
    () => expedientesSeleccionados.filter((e) => !e.asignacionActiva).map((e) => e.idExpediente),
    [expedientesSeleccionados],
  )
  const idsReasignar = useMemo(
    () => expedientesSeleccionados.filter((e) => e.asignacionActiva).map((e) => e.idExpediente),
    [expedientesSeleccionados],
  )

  const abogadoSeleccionado = abogados.find((a) => a.idUsuario === idAbogado) ?? null

  function abrirHistorial(expediente: AsignacionExpediente) {
    setHistorialExpediente(expediente)
    setHistorialLoading(true)
    listarHistorialAsignaciones(expediente.idExpediente)
      .then(setHistorialItems)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudo cargar el historial de asignación.'))
      .finally(() => setHistorialLoading(false))
  }

  function cerrarHistorial() {
    setHistorialExpediente(null)
    setHistorialItems([])
  }

  function alternarModoReasignacion(checked: boolean) {
    setModoReasignacion(checked)
    if (!checked) {
      // Al desactivar, las filas ya asignadas que quedaron marcadas dejan de ser seleccionables
      // (mismo comportamiento que V2): se limpia la selección para no dejar ids "fantasma".
      setRowSelectionModel(SIN_SELECCION)
    }
  }

  async function handleGenerarAsignacion() {
    if (idEquipo === '' || idAbogado === '') return
    if (idsNuevos.length === 0 && idsReasignar.length === 0) return

    if (idsReasignar.length > 0) {
      const faltantes = idsReasignar.filter((id) => !hojasEnvio[id]?.trim())
      if (faltantes.length > 0) {
        setError('Ingrese la hoja de envío para cada expediente a reasignar (es obligatoria en reasignación, a diferencia de una asignación nueva).')
        return
      }
      const confirmado = window.confirm(
        `Va a reasignar ${idsReasignar.length} expediente(s) que ya tienen un abogado asignado. Se conservará el historial de la asignación anterior.\n¿Desea continuar?`,
      )
      if (!confirmado) return
    }

    setAsignando(true)
    setMensajeAsignacion(null)
    setError(null)
    try {
      const partes: string[] = []
      if (idsNuevos.length > 0) {
        const resultado = await asignarExpedientes({
          idsExpediente: idsNuevos,
          idEquipo,
          idAbogado,
          comentario,
          hojasEnvioPorExpediente: hojasEnvio,
        })
        partes.push(resultado.mensaje)
      }
      if (idsReasignar.length > 0) {
        let exitosas = 0
        const errores: string[] = []
        for (const id of idsReasignar) {
          try {
            await reasignarExpediente({
              idExpediente: id,
              idEquipo,
              idAbogado,
              numeroHojaEnvio: hojasEnvio[id] ?? '',
              comentario,
            })
            exitosas++
          } catch (e) {
            const numero = expedientes.find((exp) => exp.idExpediente === id)?.numeroExpediente ?? id
            errores.push(`${numero}: ${e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'}`)
          }
        }
        partes.push(`${exitosas} expediente(s) reasignado(s) correctamente.`)
        if (errores.length > 0) {
          partes.push(`${errores.length} reasignación(es) no se pudieron completar:\n- ${errores.join('\n- ')}`)
        }
      }
      setMensajeAsignacion(partes.join('\n'))
      setRowSelectionModel(SIN_SELECCION)
      setComentario('')
      setHojasEnvio({})
      setIdEquipo('')
      setModoReasignacion(false)
      buscar()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.')
    } finally {
      setAsignando(false)
    }
  }

  // Mismo orden exacto que AsignacionTableModel en V2 (JPanelAsignacionV2): Días, Nro. Expediente,
  // N° expediente SGD, Fecha Solicitud, Fecha Vencimiento, Proc. Registral, Tipo Acta, Nro. Acta,
  // Titular, Abogado actual, Estado, Alertas (los iconos de expansión/checkbox de V2 se resuelven
  // aquí con checkboxSelection nativo del DataGrid, no como columnas de datos).
  const columnas: GridColDef<AsignacionExpediente>[] = [
    {
      field: 'diasRestantes',
      headerName: 'Días',
      width: 90,
      renderCell: (params) => (
        <StatusBadge
          label={params.value === null || params.value === undefined ? '-' : String(params.value)}
          tone={diasBadgeTone(params.value as number | null)}
        />
      ),
    },
    { field: 'numeroExpediente', headerName: 'Nro. Expediente', width: 190 },
    { field: 'numeroExpedienteSgd', headerName: 'N° expediente SGD', width: 160 },
    {
      field: 'fechaRecepcion',
      headerName: 'Fecha Solicitud',
      width: 130,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    {
      field: 'fechaVencimiento',
      headerName: 'Fecha Vencimiento',
      width: 140,
      valueFormatter: (value) => formatearFecha(value as string | null),
    },
    { field: 'procedimiento', headerName: 'Proc. Registral', width: 170 },
    { field: 'tipoActa', headerName: 'Tipo Acta', width: 150 },
    { field: 'numeroActa', headerName: 'Nro. Acta', width: 110 },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    { field: 'abogadoAsignado', headerName: 'Abogado actual', width: 180 },
    { field: 'estadoCodigo', headerName: 'Estado', width: 130 },
    {
      field: 'alertaIngreso',
      headerName: 'Alertas',
      width: 170,
      renderCell: (params) => <StatusBadge label={params.value as string} tone={alertaBadgeTone(params.value as string)} />,
    },
    {
      field: 'historial',
      headerName: 'Historial',
      width: 100,
      sortable: false,
      filterable: false,
      renderCell: (params) => (
        <Button size="small" onClick={() => abrirHistorial(params.row as AsignacionExpediente)}>
          Ver
        </Button>
      ),
    },
    {
      field: 'asociar',
      headerName: 'Asociar',
      width: 100,
      sortable: false,
      filterable: false,
      renderCell: (params) => (
        <Button size="small" onClick={() => setAsociarExpediente(params.row as AsignacionExpediente)}>
          Asociar
        </Button>
      ),
    },
    {
      field: 'grupoFamiliarAccion',
      headerName: 'Grupo Familiar',
      width: 130,
      sortable: false,
      filterable: false,
      renderCell: (params) => (
        <Button size="small" onClick={() => setGrupoFamiliarExpediente(params.row as AsignacionExpediente)}>
          Grupo Familiar
        </Button>
      ),
    },
  ]

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4 }}>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h6" sx={{ color: sdrercColors.primary }}>
            Bandeja Asignación
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Expedientes registrados pendientes de asignar a un equipo y abogado responsable.
          </Typography>
        </Box>
        <Stack direction="row" spacing={2}>
          <Button variant="outlined" onClick={() => navigate('/asignacion/cartas-respuesta')}>
            Cartas de respuesta
          </Button>
          <Button variant="outlined" onClick={() => navigate('/asignacion/carga-abogados')}>
            Carga Abogados
          </Button>
          <Button variant="outlined" onClick={() => navigate('/registro')}>
            Bandeja Registro
          </Button>
        </Stack>
      </Stack>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      {mensajeAsignacion && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setMensajeAsignacion(null)}>
          {mensajeAsignacion}
        </Alert>
      )}

      <Stack spacing={2} sx={{ mb: 3 }}>
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
          <TextField
            label="Buscar (N° expediente, SGD, acta, titular...)"
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleBuscar()}
            sx={{ minWidth: 320 }}
          />
          <Button variant="contained" onClick={handleBuscar} disabled={loading}>
            Buscar
          </Button>
          <Button variant="outlined" onClick={handleLimpiar} disabled={loading}>
            Limpiar
          </Button>
        </Stack>
        <Stack direction="row" spacing={2}>
          <TextField
            label="Fecha desde"
            type="date"
            size="small"
            value={desde}
            onChange={(e) => setDesde(e.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <TextField
            label="Fecha hasta"
            type="date"
            size="small"
            value={hasta}
            onChange={(e) => setHasta(e.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
          />
        </Stack>
        <Stack direction="row" spacing={2}>
          <TextField select label="Estado" size="small" sx={{ minWidth: 200 }} value={estado} onChange={(e) => setEstado(e.target.value)}>
            <MenuItem value="">Todos los estados</MenuItem>
            <MenuItem value="REGISTRADO">Registrado</MenuItem>
            <MenuItem value="ASIGNADO">Asignado</MenuItem>
          </TextField>
          <TextField
            label="Límite"
            type="number"
            size="small"
            sx={{ width: 120 }}
            value={limite}
            onChange={(e) => setLimite(Number(e.target.value) || 200)}
          />
        </Stack>
      </Stack>

      <Stack direction="row" spacing={2} sx={{ mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <Box onClick={() => alternarFiltro('PENDIENTES')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Pendientes" value={kpis.pendientes} caption="Para asignación" color={sdrercColors.info} />
        </Box>
        <Box onClick={() => alternarFiltro('POTENCIAL_DUPLICADO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Potencial duplicado" value={kpis.potencialDuplicado} caption="Acta + titular" color={sdrercColors.warning} />
        </Box>
        <Box onClick={() => alternarFiltro('POSIBLE_GRUPO_FAMILIAR')} sx={{ cursor: 'pointer' }}>
          <MetricCard
            label="Posible Grupo Familiar"
            value={kpis.posibleGrupoFamiliar}
            caption="Apellidos coincidentes"
            color={sdrercColors.primary}
          />
        </Box>
        <Box onClick={() => alternarFiltro('GRUPO_FAMILIAR_CONFIRMADO')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Grupo Familiar Confirmado" value={kpis.grupoFamiliarConfirmado} caption="Registrado" color={sdrercColors.success} />
        </Box>
        <Box onClick={() => alternarFiltro('POR_VENCER')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Por vencer" value={kpis.porVencer} caption="0 a 5 días hábiles" color={sdrercColors.warning} />
        </Box>
        <Box onClick={() => alternarFiltro('VENCIDOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Vencidos" value={kpis.vencidos} caption="Plazo excedido" color={sdrercColors.error} />
        </Box>
      </Stack>

      <Tooltip title="Permite marcar en el listado expedientes ya asignados, para reasignarlos con una nueva hoja de envío.">
        <FormControlLabel
          sx={{ mb: 1 }}
          control={
            <Checkbox
              checked={modoReasignacion}
              onChange={(e) => alternarModoReasignacion(e.target.checked)}
            />
          }
          label="Habilitar reasignación"
        />
      </Tooltip>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2, mb: 3 }}>
        <DataGrid
          rows={filas}
          columns={columnas}
          getRowId={(row) => row.idExpediente}
          loading={loading}
          density="compact"
          autoHeight
          checkboxSelection
          isRowSelectable={(params) => {
            const row = params.row as AsignacionExpediente
            return row.asignable || (modoReasignacion && row.asignacionActiva)
          }}
          rowSelectionModel={rowSelectionModel}
          onRowSelectionModelChange={setRowSelectionModel}
          showToolbar
          pageSizeOptions={[25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 25 } } }}
        />
      </Box>

      {idsSeleccionados.length > 0 && (
        <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2, p: 3 }}>
          <Typography variant="subtitle1" sx={{ color: sdrercColors.primary, mb: 2 }}>
            Asignación de abogado ({idsSeleccionados.length} expediente{idsSeleccionados.length > 1 ? 's' : ''} seleccionado
            {idsSeleccionados.length > 1 ? 's' : ''})
          </Typography>
          <Stack direction="row" spacing={2} sx={{ mb: 2, flexWrap: 'wrap' }}>
            <TextField
              select
              label="Equipo destino"
              size="small"
              sx={{ minWidth: 220 }}
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
              sx={{ minWidth: 220 }}
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
              sx={{ minWidth: 200 }}
              value={abogadoSeleccionado?.supervisorNombre || '-'}
              slotProps={{ input: { readOnly: true } }}
            />
          </Stack>

          <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
            Hoja de envío (única por expediente; obligatoria para expedientes a reasignar, opcional para asignaciones nuevas)
          </Typography>
          <Stack spacing={1} sx={{ mb: 2 }}>
            {expedientesSeleccionados.map((exp) => (
              <Stack key={exp.idExpediente} direction="row" spacing={2} sx={{ alignItems: 'center' }}>
                <Typography variant="body2" sx={{ minWidth: 220 }}>
                  {exp.numeroExpediente}
                  {exp.asignacionActiva ? ' (reasignación)' : ''}
                </Typography>
                <TextField
                  size="small"
                  required={exp.asignacionActiva}
                  placeholder="N° hoja de envío"
                  value={hojasEnvio[exp.idExpediente] ?? ''}
                  onChange={(e) => setHojasEnvio((prev) => ({ ...prev, [exp.idExpediente]: e.target.value }))}
                  sx={{ minWidth: 220 }}
                />
              </Stack>
            ))}
          </Stack>

          <TextField
            label="Comentario"
            multiline
            minRows={2}
            fullWidth
            value={comentario}
            onChange={(e) => setComentario(e.target.value)}
            sx={{ mb: 2 }}
          />

          <Button
            variant="contained"
            onClick={handleGenerarAsignacion}
            disabled={asignando || idEquipo === '' || idAbogado === ''}
          >
            Generar asignación
          </Button>
        </Box>
      )}

      <Dialog open={historialExpediente !== null} onClose={cerrarHistorial} maxWidth="md" fullWidth>
        <DialogTitle>
          Historial de asignación / reasignación
          {historialExpediente && (
            <Typography variant="body2" color="text.secondary">
              {historialExpediente.numeroExpediente} — {historialExpediente.titular}
            </Typography>
          )}
        </DialogTitle>
        <DialogContent>
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
                    <TableRow key={item.idExpedienteAsignacion}>
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
        </DialogContent>
      </Dialog>

      {asociarExpediente && (
        <AsociarDuplicadosPanel
          open={asociarExpediente !== null}
          onClose={() => setAsociarExpediente(null)}
          idExpedientePrincipal={asociarExpediente.idExpediente}
          numeroExpediente={asociarExpediente.numeroExpediente}
          titular={asociarExpediente.titular}
          onAsociado={buscar}
        />
      )}

      {grupoFamiliarExpediente && (
        <GrupoFamiliarPanel
          open={grupoFamiliarExpediente !== null}
          onClose={() => setGrupoFamiliarExpediente(null)}
          idExpedientePrincipal={grupoFamiliarExpediente.idExpediente}
          numeroExpediente={grupoFamiliarExpediente.numeroExpediente}
          titular={grupoFamiliarExpediente.titular}
          onAsociado={buscar}
        />
      )}
    </Box>
  )
}
