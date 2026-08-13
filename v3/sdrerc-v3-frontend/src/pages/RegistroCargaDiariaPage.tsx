import { useMemo, useRef, useState } from 'react'
import { Alert, Box, Button, Stack, Tooltip, Typography } from '@mui/material'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { useNavigate } from 'react-router-dom'
import {
  confirmarCargaDiaria,
  descargarPlantillaCargaDiaria,
  exportarCargaDiaria,
  previsualizarCargaDiaria,
  validarCargaDiaria,
  type CargaDiariaPreview,
} from '../api/cargaDiariaApi'
import { ApiError, descargarBlob } from '../api/http'
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'

function formatearFecha(iso: string | null): string {
  if (!iso) return ''
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

// Mismo criterio de parseo que CargaDiariaArchivoParserService/JPanelCargaDiariaRecepcionV2.parseFechaTabla
// (V2): admite yyyy-MM-dd o dd/MM/yyyy; cualquier otro formato se descarta (fecha queda vacía).
function parseFechaEditada(value: string): string | null {
  const trimmed = value.trim()
  if (!trimmed) return null
  if (/^\d{4}-\d{2}-\d{2}$/.test(trimmed)) return trimmed
  const m = trimmed.match(/^(\d{2})\/(\d{2})\/(\d{4})$/)
  if (m) return `${m[3]}-${m[2]}-${m[1]}`
  return null
}

// Mismo criterio que ProcedimientoRegistralRules.requiereDecisionAsignacionParaNumero (V2/V3
// backend): Reconsideración/Apelación se registran sin número, a decidir en Asignación. Réplica
// solo para formatear la columna "Número expediente" en el cliente; la fuente de verdad real es
// numeroExpedienteGenerado que ya calcula el backend.
function requiereDecisionAsignacionParaNumero(procedimiento: string | null): boolean {
  if (!procedimiento) return false
  const normalizado = procedimiento
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toUpperCase()
  return normalizado.includes('RECONSIDERACION') || normalizado.includes('APELACION')
}

// Réplica de CargaDiariaExportService.numeroExpedientePreview (V2/V3 backend) para que la columna
// "Número expediente" de la grilla muestre el mismo texto de respaldo que la exportación a Excel.
function numeroExpedientePreview(item: CargaDiariaPreview): string {
  if (item.numeroExpedienteGenerado) return item.numeroExpedienteGenerado
  if (item.posibleDuplicado) return 'Sin número por duplicado'
  if (requiereDecisionAsignacionParaNumero(item.tipoProcedimiento)) return 'Sin número por procedimiento'
  return 'Pendiente'
}

// Réplica de CargaDiariaExportService.observacionValidacionTabla (V2/V3 backend): consolida el
// mensaje crudo de validación en frases cortas "Dato incompleto: X", igual que la grilla y la
// exportación de V2.
function observacionValidacionTabla(item: CargaDiariaPreview): string {
  const observaciones: string[] = []
  if (item.posibleDuplicado) observaciones.push('Potencial duplicado')
  if (item.grupoFamiliar || item.posibleGrupoFamiliar) observaciones.push('Posible Grupo Familiar')
  const mensaje = item.mensajeValidacion
  if (mensaje) {
    for (const parte of mensaje.split(/\s*\|\s*/)) {
      const etiqueta = convertirADatoIncompleto(parte)
      if (etiqueta && !observaciones.includes(etiqueta)) observaciones.push(etiqueta)
    }
  }
  return observaciones.length === 0 ? 'Sin observación' : observaciones.join(' | ')
}

function convertirADatoIncompleto(mensaje: string): string | null {
  const texto = mensaje.trim()
  if (!texto) return null
  const lower = texto.toLowerCase()
  if (!lower.includes('obligatorio') && !lower.includes('inválida') && !lower.includes('invalida') && !lower.includes('determinar')) {
    return null
  }
  if (lower.includes('número de trámite')) return 'Dato incompleto: Número de trámite'
  if (lower.includes('número de documento')) return 'Dato incompleto: N° Documento'
  if (lower.includes('tipo de procedimiento')) return 'Dato incompleto: Procedimiento registral'
  if (lower.includes('tipo de solicitud')) return 'Dato incompleto: Tipo de solicitud'
  if (lower.includes('tipo de documento de identidad del solicitante')) return 'Dato incompleto: Tipo documento identidad solicitante'
  if (lower.includes('número de documento de identidad del solicitante')) return 'Dato incompleto: N° documento identidad solicitante'
  if (lower.includes('tipo de documento de identidad del titular')) return 'Dato incompleto: Tipo documento identidad titular'
  if (lower.includes('número de documento de identidad del titular')) return 'Dato incompleto: N° documento identidad titular'
  if (lower.includes('tipo de documento')) return 'Dato incompleto: Tipo documento'
  if (lower.includes('tipo de acta')) return 'Dato incompleto: Tipo de acta'
  if (lower.includes('número de acta')) return 'Dato incompleto: N° Acta'
  if (lower.includes('titular')) return 'Dato incompleto: Titular'
  if (lower.includes('fecha de solicitud')) return 'Dato incompleto: Fecha de solicitud'
  if (lower.includes('canal de recepción') || lower.includes('canal de recepcion')) return 'Dato incompleto: Canal recepción'
  return null
}

function toneEstadoValidacion(estado: string | null): 'success' | 'warning' | 'error' | 'info' | 'neutral' {
  switch (estado) {
    case 'Válido':
      return 'success'
    case 'Registrado':
      return 'info'
    case 'Pendiente':
      return 'neutral'
    case 'Duplicado':
    case 'Con observaciones':
      return 'warning'
    case 'Error':
      return 'error'
    default:
      return 'neutral'
  }
}

const CAMPOS_EDITABLES = new Set<keyof CargaDiariaPreview>([
  'tipoSolicitud',
  'remitente',
  'tipoDocumentoIdentidadSolicitante',
  'numeroDocumentoIdentidadSolicitante',
  'numeroTramite',
  'canalRecepcion',
  'numeroExpedienteSgd',
  'tipoDocumento',
  'numeroDocumento',
  'tipoProcedimiento',
  'tipoActa',
  'numeroActa',
  'titular',
  'tipoDocumentoIdentidadTitular',
  'numeroDocumentoIdentidadTitular',
])

/**
 * Equivalente web de la pestaña "Carga diaria" de Recepción (V2: JPanelCargaDiariaRecepcionV2).
 * Subir Excel/CSV → parsear → validar → previsualización EDITABLE (columnas de datos importados,
 * igual que V2: `esColumnaEditable`) → botón "Validar" para recalcular duplicados/grupo
 * familiar/número tras editar → Confirmar y registrar. También exporta la previsualización a Excel
 * y permite descargar la plantilla oficial — mismo alcance funcional que V2.
 *
 * Diferencia deliberada de comportamiento respecto a V2 (no de datos): en V2, editar una celda solo
 * recalcula localmente "datos incompletos" (`actualizarValidacionLocal`) sin tocar
 * `listoParaRegistrar`, por lo que técnicamente se puede confirmar sin volver a validar contra la
 * base. Aquí, cualquier edición marca la fila `Pendiente` y limpia `listoParaRegistrar`, obligando a
 * presionar "Validar" (que sí revisa duplicados/grupo familiar/número contra SDRERC_APP) antes de
 * poder confirmarla — más seguro, mismo resultado final una vez validada.
 */
export function RegistroCargaDiariaPage() {
  const navigate = useNavigate()
  const fileInputRef = useRef<HTMLInputElement | null>(null)
  const [nombreArchivo, setNombreArchivo] = useState<string | null>(null)
  const [registros, setRegistros] = useState<CargaDiariaPreview[]>([])
  const [loading, setLoading] = useState(false)
  const [validando, setValidando] = useState(false)
  const [confirmando, setConfirmando] = useState(false)
  const [exportando, setExportando] = useState(false)
  const [descargandoPlantilla, setDescargandoPlantilla] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [mensaje, setMensaje] = useState<string | null>(null)

  function handleSeleccionarArchivo() {
    fileInputRef.current?.click()
  }

  function handleArchivoElegido(event: React.ChangeEvent<HTMLInputElement>) {
    const archivo = event.target.files?.[0]
    event.target.value = ''
    if (!archivo) return
    setNombreArchivo(archivo.name)
    setRegistros([])
    setMensaje(null)
    setError(null)
    setLoading(true)
    previsualizarCargaDiaria(archivo)
      .then((data) => {
        setRegistros(data)
        const listos = data.filter((r) => r.listoParaRegistrar).length
        setMensaje(`${data.length} registro(s) leído(s) y validado(s). ${listos} listo(s) para registrar.`)
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudo previsualizar el archivo.'))
      .finally(() => setLoading(false))
  }

  function handleValidar() {
    if (registros.length === 0) {
      setError('Previsualice un archivo antes de validar.')
      return
    }
    setValidando(true)
    setError(null)
    validarCargaDiaria(registros)
      .then((data) => {
        setRegistros(data)
        const listos = data.filter((r) => r.listoParaRegistrar).length
        setMensaje(`${listos} registro(s) listo(s) para registrar.`)
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudo validar la carga diaria.'))
      .finally(() => setValidando(false))
  }

  function handleConfirmar() {
    const listos = registros.filter((r) => r.listoParaRegistrar && !r.registrado).length
    if (listos === 0) {
      setError('No hay registros listos para registrar.')
      return
    }
    const confirmado = window.confirm(
      `Se registrarán ${listos} expediente(s) en SDRERC_APP.\n` +
        'Los registros con observaciones o duplicidad también se registrarán y quedarán marcados.\n\n' +
        '¿Desea confirmar la carga?',
    )
    if (!confirmado) return
    setConfirmando(true)
    setError(null)
    confirmarCargaDiaria(registros)
      .then((resultado) => {
        setRegistros(resultado.registros)
        setMensaje(resultado.mensaje)
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudo confirmar la carga diaria.'))
      .finally(() => setConfirmando(false))
  }

  function handleExportar() {
    if (registros.length === 0) {
      setError('No hay registros para exportar.')
      return
    }
    setExportando(true)
    setError(null)
    exportarCargaDiaria(registros)
      .then((blob) => {
        const nombre = `previsualizacion_carga_diaria_${new Date()
          .toISOString()
          .replace(/[-:T]/g, '')
          .slice(0, 15)}.xlsx`
        descargarBlob(blob, nombre)
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudo exportar la previsualización.'))
      .finally(() => setExportando(false))
  }

  function handleDescargarPlantilla() {
    setDescargandoPlantilla(true)
    setError(null)
    descargarPlantillaCargaDiaria()
      .then((blob) => descargarBlob(blob, 'plantilla_carga_diaria_sdrerc.xlsx'))
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudo descargar la plantilla.'))
      .finally(() => setDescargandoPlantilla(false))
  }

  function handleLimpiar() {
    setNombreArchivo(null)
    setRegistros([])
    setMensaje(null)
    setError(null)
  }

  function processRowUpdate(newRow: CargaDiariaPreview): CargaDiariaPreview {
    const actualizado: CargaDiariaPreview = {
      ...newRow,
      estadoValidacion: 'Pendiente',
      mensajeValidacion: null,
      listoParaRegistrar: false,
    }
    setRegistros((prev) => prev.map((r) => (r.fila === actualizado.fila ? actualizado : r)))
    setMensaje('Celda actualizada. Presione Validar para recalcular observaciones, duplicidad y número de expediente.')
    return actualizado
  }

  const resumen = useMemo(() => {
    const validos = registros.filter((r) => r.estadoValidacion === 'Válido' || r.estadoValidacion === 'Registrado').length
    const conAlertas = registros.filter(
      (r) => r.estadoValidacion === 'Error' || r.estadoValidacion === 'Duplicado' || r.estadoValidacion === 'Con observaciones',
    ).length
    return { validos, conAlertas }
  }, [registros])

  const listosParaConfirmar = useMemo(
    () => registros.some((r) => r.listoParaRegistrar && !r.registrado),
    [registros],
  )

  const ocupado = loading || validando || confirmando || exportando || descargandoPlantilla

  function columnaTexto(field: keyof CargaDiariaPreview, headerName: string, width: number): GridColDef<CargaDiariaPreview> {
    return {
      field,
      headerName,
      width,
      editable: CAMPOS_EDITABLES.has(field),
      isCellEditable: (params) => !params.row.registrado,
    }
  }

  const columnas: GridColDef<CargaDiariaPreview>[] = [
    columnaTexto('tipoSolicitud', 'Tipo de solicitud', 130),
    {
      field: 'fechaRecepcion',
      headerName: 'Fecha de solicitud',
      width: 140,
      editable: true,
      isCellEditable: (params) => !params.row.registrado,
      valueGetter: (_value, row) => formatearFecha(row.fechaRecepcion) || row.fechaRecepcionTexto || '',
      valueSetter: (value, row) => {
        const texto = String(value ?? '').trim()
        return { ...row, fechaRecepcion: parseFechaEditada(texto), fechaRecepcionTexto: texto }
      },
    },
    columnaTexto('remitente', 'Solicitado por', 220),
    columnaTexto('tipoDocumentoIdentidadSolicitante', 'Tipo doc. solicitante', 150),
    columnaTexto('numeroDocumentoIdentidadSolicitante', 'N° doc. solicitante', 140),
    columnaTexto('numeroTramite', 'N° trámite web', 140),
    columnaTexto('canalRecepcion', 'Canal recepción', 160),
    columnaTexto('numeroExpedienteSgd', 'N° expediente SGD', 160),
    columnaTexto('tipoDocumento', 'Tipo documento', 140),
    columnaTexto('numeroDocumento', 'N° documento', 130),
    columnaTexto('tipoProcedimiento', 'Procedimiento registral', 190),
    columnaTexto('tipoActa', 'Tipo de acta', 130),
    columnaTexto('numeroActa', 'N° acta', 110),
    columnaTexto('titular', 'Titular', 220),
    columnaTexto('tipoDocumentoIdentidadTitular', 'Tipo doc. titular', 130),
    columnaTexto('numeroDocumentoIdentidadTitular', 'N° doc. titular', 130),
    {
      field: 'estadoValidacion',
      headerName: 'Resultado del sistema',
      width: 170,
      renderCell: (params) => (
        <StatusBadge label={(params.value as string) ?? '-'} tone={toneEstadoValidacion(params.value as string | null)} />
      ),
    },
    {
      field: 'posibleDuplicado',
      headerName: 'Duplicidad',
      width: 100,
      renderCell: (params) => {
        const item = params.row as CargaDiariaPreview
        const texto = item.posibleDuplicado ? 'Sí' : 'No'
        return item.motivoDuplicado ? (
          <Tooltip title={item.motivoDuplicado}>
            <span>{texto}</span>
          </Tooltip>
        ) : (
          <span>{texto}</span>
        )
      },
    },
    {
      field: 'numeroExpedienteGenerado',
      headerName: 'Número expediente',
      width: 190,
      valueGetter: (_value, row) => numeroExpedientePreview(row),
    },
    {
      field: 'mensajeValidacion',
      headerName: 'Observación',
      width: 320,
      valueGetter: (_value, row) => observacionValidacionTabla(row),
    },
  ]

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4 }}>
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h6" sx={{ color: sdrercColors.primary }}>
            Carga diaria
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Suba la plantilla oficial de carga diaria (.xlsx o .csv) para registrar expedientes en lote.
          </Typography>
        </Box>
        <Button variant="text" onClick={() => navigate('/registro')}>
          Volver a Bandeja Registro
        </Button>
      </Stack>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      {mensaje && !error && (
        <Alert severity="info" sx={{ mb: 2 }}>
          {mensaje}
        </Alert>
      )}

      <Stack direction="row" spacing={2} sx={{ mb: 3, alignItems: 'center', flexWrap: 'wrap' }}>
        <input ref={fileInputRef} type="file" accept=".xlsx,.csv" hidden onChange={handleArchivoElegido} />
        <Button variant="outlined" onClick={handleDescargarPlantilla} disabled={ocupado}>
          Descargar plantilla
        </Button>
        <Button variant="contained" onClick={handleSeleccionarArchivo} disabled={ocupado}>
          Seleccionar archivo
        </Button>
        <Button variant="outlined" onClick={handleValidar} disabled={ocupado || registros.length === 0}>
          Validar
        </Button>
        <Button
          variant="contained"
          color="success"
          onClick={handleConfirmar}
          disabled={ocupado || !listosParaConfirmar}
        >
          Confirmar carga
        </Button>
        <Button variant="outlined" onClick={handleExportar} disabled={ocupado || registros.length === 0}>
          Exportar
        </Button>
        <Button variant="outlined" onClick={handleLimpiar} disabled={ocupado}>
          Limpiar
        </Button>
        <Typography variant="body2" color="text.secondary">
          {nombreArchivo ?? 'Sin archivo seleccionado'}
        </Typography>
      </Stack>

      <Stack direction="row" spacing={2} sx={{ mb: 3 }}>
        <MetricCard label="Válidos" value={resumen.validos} caption="Sin errores críticos" color={sdrercColors.success} />
        <MetricCard label="Con alertas" value={resumen.conAlertas} caption="No bloqueantes" color={sdrercColors.warning} />
      </Stack>

      <Box sx={{ height: 560, bgcolor: 'white', borderRadius: 2 }}>
        <DataGrid<CargaDiariaPreview>
          rows={registros}
          columns={columnas}
          getRowId={(row) => row.fila}
          loading={loading || confirmando}
          density="compact"
          disableRowSelectionOnClick
          processRowUpdate={processRowUpdate}
          onProcessRowUpdateError={(e) => setError(e instanceof ApiError ? e.message : 'No se pudo editar la celda.')}
        />
      </Box>
    </Box>
  )
}
