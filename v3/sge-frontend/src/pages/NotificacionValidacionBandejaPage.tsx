import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Badge,
  Box,
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
import CreateNewFolderOutlinedIcon from '@mui/icons-material/CreateNewFolderOutlined'
import FolderOpenOutlinedIcon from '@mui/icons-material/FolderOpenOutlined'
import { DataGrid, type GridColDef, type GridComparatorFn } from '@mui/x-data-grid'
import { listarBandejaValidacion, type NotificacionAsignacionDocumento } from '../api/notificacionValidacionApi'
import { obtenerDatosExpediente, type ExpedienteBandeja } from '../api/registroBandejaApi'
import { listarDocumentosAnalizados, type DocumentoAnalizado } from '../api/analisisApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { ApiError } from '../api/http'
import { sdrercColors } from '../theme'
import { dataGridBaseSx, DATA_GRID_ROW_ICON_FOLDER_CLASS } from '../dataGridTheme'
import { DataGridStandardToolbar } from '../components/DataGridStandardToolbar'
import { primerDiaMesAnteriorISO, hoyISO } from '../utils/fechas'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge, diasBadgeTone } from '../components/StatusBadge'
import { NotificacionValidacionContent } from '../components/NotificacionValidacionContent'
import { WorkspaceTabsDrawer, type Workspace } from '../components/WorkspaceTabsDrawer'
import { PanelDatosExpediente } from '../components/PanelDatosExpediente'
import { TrazabilidadContent } from '../components/TrazabilidadContent'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { NOTIFICACION_TABS } from '../layout/moduleTabs'

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

const WORKSPACE_COLORS = ['#154775', '#0B8E97', '#B8860B', '#8E44AD', '#C0392B', '#16A085', '#D35400', '#2C3E50']
const COLOR_DATOS = '#2370A8'
const COLOR_TRAZABILIDAD = '#8E44AD'
const COLOR_VALIDAR = 'rgb(15, 118, 110)'

/**
 * Fila real de la grilla (30/08/2026, réplica literal del mismo diseño de "Bandeja de supervisión"
 * — `NotificacionAsignacionBandejaPage.tsx`, pedido explícito del usuario: "replicar tal cual el
 * diseño de la grilla de la bandeja de supervisión a la bandeja de validación"). Mismo criterio: todo
 * expediente se agrupa en una fila `padre` (ícono de carpeta SIEMPRE visible, sin importar cuántos
 * documentos tenga en el subconjunto de esta bandeja — ver Parte 7 de esa página), y al expandir
 * trae el universo COMPLETO de documentos del expediente como una única fila `hijo` con su propia
 * "nested grid" (tabla real, no un Stack de campos apilados).
 */
interface FilaValidacion extends NotificacionAsignacionDocumento {
  filaId: number
  filaTipo: 'padre' | 'hijo'
  cantidadDocumentos: number
  documentosDelExpediente?: DocumentoAnalizado[]
}

/** Mismo criterio que `documentoReferencia` de Bandeja de supervisión — en esta bandeja todos los
 * documentos ya son FINAL (`CONDICION_VALIDACION_NOTIFICACION`), así que en la práctica solo desempata
 * por `fechaDocumento` más reciente; se mantiene la misma función para consistencia de diseño. */
function documentoReferencia(docs: NotificacionAsignacionDocumento[]): NotificacionAsignacionDocumento {
  const ordenados = [...docs].sort((a, b) => {
    if (a.clasificacion !== b.clasificacion) {
      if (a.clasificacion === 'FINAL') return -1
      if (b.clasificacion === 'FINAL') return 1
    }
    return (b.fechaDocumento || '').localeCompare(a.fechaDocumento || '')
  })
  return ordenados[0]
}

/** Mismo `agruparHijosBajoExpediente` de Bandeja de supervisión — evita que una fila 'hijo' se
 * desagrupe de su 'padre' al ordenar por cualquier cabecera. */
function agruparHijosBajoExpediente(comparadorOriginal?: GridComparatorFn<unknown>): GridComparatorFn<unknown> {
  return (v1, v2, params1, params2) => {
    const fila1 = params1.api.getRow(params1.id) as FilaValidacion | null
    const fila2 = params2.api.getRow(params2.id) as FilaValidacion | null
    const valor1 = fila1?.filaTipo === 'hijo' ? params1.api.getCellValue(-fila1.idExpediente, params1.field) : v1
    const valor2 = fila2?.filaTipo === 'hijo' ? params2.api.getCellValue(-fila2.idExpediente, params2.field) : v2
    if (comparadorOriginal) return comparadorOriginal(valor1, valor2, params1, params2)
    if (valor1 == null && valor2 == null) return 0
    if (valor1 == null) return -1
    if (valor2 == null) return 1
    if (valor1 < valor2) return -1
    if (valor1 > valor2) return 1
    return 0
  }
}

/** 28/08/2026, pedido explícito del usuario: KPIs clicables, mismo patrón ya usado en el resto de
 * bandejas del sistema (`alternarFiltro`/`coincideFiltroKpi`). "Pendientes" no representa un
 * subconjunto (es el total, todo lo que llega a esta bandeja ya está "pendiente de validar" por
 * definición) — actúa como "Total de expedientes" en otras bandejas: limpia cualquier filtro de KPI
 * activo en vez de acotar la lista. */
type FiltroKpi = null | 'POR_VENCER' | 'VENCIDOS'

/** Adapta una fila (documento) de esta bandeja al tipo que espera el "Panel de datos" compartido. */
function adaptarParaPanelDatos(row: NotificacionAsignacionDocumento): ExpedienteBandeja {
  return {
    idExpediente: row.idExpediente,
    numeroExpediente: row.numeroExpediente,
    numeroTramiteDocumentario: '',
    numeroExpedienteSgd: row.numeroExpedienteSgd,
    etapaCodigo: 'NOTIFICACION',
    estadoCodigo: row.estadoExpedienteCodigo,
    abogadoInicial: '',
    responsableActual: row.usuarioNotificacionActual,
    equipoActual: '',
    fechaRecepcion: null,
    fechaRegistro: null,
    fechaUltimoMovimiento: null,
    fechaVencimiento: row.fechaVencimiento,
    requierePublicacion: false,
    expedienteDigitalCompleto: false,
    canal: '',
    procedimiento: '',
    tipoActa: '',
    numeroActa: '',
    grupoFamiliar: 'No',
    alertas: '',
    titular: row.titular,
    relacionesConfirmadasComoPrincipal: row.totalRelacionados,
    relacionadoComoHijo: false,
    grupoFamiliarConfirmado: false,
    diasRestantes: row.diasRestantes,
  }
}

/**
 * Equivalente web de la 2da pestaña de Notificación, "Bandeja Validación" (21/08/2026, pedido
 * explícito del usuario, investigación previa contra `JPanelNotificacionV2.crearBandejaValidacion`
 * real). Diseño de grilla actualizado el 30/08/2026 (pedido explícito del usuario: "replicar tal
 * cual el diseño de la grilla de la bandeja de supervisión a la bandeja de validación") — ya NO
 * replica literalmente `validacionModel` de V2 (columnas propias + `SINGLE_SELECTION` sin expandir);
 * ahora comparte el mismo diseño que `NotificacionAsignacionBandejaPage.tsx` (Bandeja de
 * supervisión): agrupamiento por expediente con ícono de carpeta SIEMPRE visible (`FilaValidacion`,
 * `filasAgrupadas`), nested grid con "tarjeta de acento lateral" al expandir (universo completo de
 * documentos del expediente vía `listarDocumentosAnalizados`, no solo los de esta bandeja), y las
 * mismas 9 columnas de fila padre (Días/Expediente/N° SGD/Fecha solicitud/Tipo documento/Resultado/
 * Abogado/Titular/Estado). Sin checkbox de selección múltiple ni botón "Asignar" (esta bandeja no
 * tiene una acción bulk equivalente — su acción real es "Validar" por documento vía doble clic, sin
 * cambios en ese punto).
 *
 * Bug real corregido de paso (30/08/2026): `listarDocumentosValidacion` (backend) usaba el mismo
 * mapper compartido que `listarDocumentosAsignacionNotificacion`, extendido en un incremento previo
 * para leer `fecha_recepcion`/`responsable`/`resultado_analisis` — sin esas 3 columnas en su propio
 * SQL, esta bandeja fallaba con `SQLException` en cualquier consulta. Se agregaron los mismos JOIN/
 * subconsulta ya usados en Bandeja de supervisión (ver Javadoc de `DocumentoAnalisisDAO#listarDocumentosValidacion`).
 *
 * 3 KPIs (mismo criterio que el header real de V2 — Pendientes/Por vencer/Vencidos, sobre
 * `diasRestantes`, ya que el KPI "Pendientes" es simplemente el total: todo lo que aparece acá está,
 * por definición, pendiente de validar). Buscador de texto + Fecha Emisión desde/hasta (sin combo de
 * Estado — V2 real solo tiene un ítem fijo "Pendientes de validar" en ese combo, sin valor real como
 * filtro, así que no se replicó). Filtrado 100% en memoria (mismo criterio que Bandeja Asignación),
 * ANTES de agrupar por expediente.
 *
 * Panel multipestañas — 2 subpestañas: "Datos" (`permiteEdicion=false`) y "Validar"
 * (`NotificacionValidacionContent.tsx`, ver su Javadoc para el alcance real y la ausencia deliberada
 * de "Destino operativo").
 */
export function NotificacionValidacionBandejaPage() {
  const [documentos, setDocumentos] = useState<NotificacionAsignacionDocumento[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [texto, setTexto] = useState('')
  const [desde, setDesde] = useState(primerDiaMesAnteriorISO())
  const [hasta, setHasta] = useState(hoyISO())
  const [filtroKpi, setFiltroKpi] = useState<FiltroKpi>(null)

  type WorkspaceAbierto = { id: string; row: NotificacionAsignacionDocumento; color: string; activeSubTabId: string }
  const [workspacesAbiertos, setWorkspacesAbiertos] = useState<WorkspaceAbierto[]>([])
  const [activeWorkspaceId, setActiveWorkspaceId] = useState<string | null>(null)
  const [datosPorExpediente, setDatosPorExpediente] = useState<
    Record<number, { detalle: ExpedienteEdicionManual | null; cargando: boolean; error: string | null }>
  >({})

  const buscar = useCallback(() => {
    setLoading(true)
    setError(null)
    listarBandejaValidacion()
      .then(setDocumentos)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    buscar()
  }, [buscar])

  // Mismo fix de sincronización ya aplicado en el resto de bandejas de este módulo (ver v3/CLAUDE.md).
  useEffect(() => {
    setWorkspacesAbiertos((prev) =>
      prev.map((w) => {
        const fresco = documentos.find((d) => d.idDocumentoAnalizado === w.row.idDocumentoAnalizado)
        return fresco ? { ...w, row: fresco } : w
      }),
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [documentos])

  function handleLimpiar() {
    setTexto('')
    setDesde(primerDiaMesAnteriorISO())
    setHasta(hoyISO())
    setFiltroKpi(null)
  }

  function handleBuscar() {
    setFiltroKpi(null)
    buscar()
  }

  function alternarFiltro(valor: FiltroKpi) {
    setFiltroKpi((prev) => (prev === valor ? null : valor))
  }

  const kpis = useMemo(() => {
    let porVencer = 0
    let vencidos = 0
    for (const item of documentos) {
      if (item.diasRestantes !== null) {
        if (item.diasRestantes < 0) vencidos++
        else if (item.diasRestantes <= 5) porVencer++
      }
    }
    return { pendientes: documentos.length, porVencer, vencidos }
  }, [documentos])

  const filas = useMemo(() => {
    const textoNorm = texto.trim().toUpperCase()
    return documentos.filter((item) => {
      if (textoNorm) {
        const coincide = [item.numeroExpediente, item.numeroExpedienteSgd, item.titular, item.tipoDocumento, item.numeroDocumento]
          .some((v) => (v || '').toUpperCase().includes(textoNorm))
        if (!coincide) return false
      }
      if (item.fechaDocumento) {
        const fecha = item.fechaDocumento.slice(0, 10)
        if (desde && fecha < desde) return false
        if (hasta && fecha > hasta) return false
      }
      if (filtroKpi === 'POR_VENCER' && !(item.diasRestantes !== null && item.diasRestantes >= 0 && item.diasRestantes <= 5)) return false
      if (filtroKpi === 'VENCIDOS' && !(item.diasRestantes !== null && item.diasRestantes < 0)) return false
      return true
    })
  }, [documentos, texto, desde, hasta, filtroKpi])

  // Agrupamiento por expediente (30/08/2026, réplica literal de Bandeja de supervisión) — `filas` ya
  // viene filtrada por KPI/búsqueda; se agrupa DESPUÉS de filtrar, sobre lo que realmente se muestra.
  const gruposPorExpediente = useMemo(() => {
    const mapa = new Map<number, NotificacionAsignacionDocumento[]>()
    for (const fila of filas) {
      const lista = mapa.get(fila.idExpediente)
      if (lista) lista.push(fila)
      else mapa.set(fila.idExpediente, [fila])
    }
    return mapa
  }, [filas])

  const [expedientesExpandidos, setExpedientesExpandidos] = useState<Set<number>>(new Set())

  function alternarExpansionExpediente(idExpediente: number) {
    setExpedientesExpandidos((prev) => {
      const next = new Set(prev)
      if (next.has(idExpediente)) next.delete(idExpediente)
      else next.add(idExpediente)
      return next
    })
  }

  // Al expandir un padre, trae el universo COMPLETO de documentos del expediente (mismo endpoint que
  // usa la subpestaña "Análisis", sin restricción de etapa) — cacheado por expediente.
  const [documentosCompletos, setDocumentosCompletos] = useState<Record<number, DocumentoAnalizado[]>>({})
  const [cargandoCompleto, setCargandoCompleto] = useState<Set<number>>(new Set())

  useEffect(() => {
    for (const idExpediente of expedientesExpandidos) {
      if (documentosCompletos[idExpediente] || cargandoCompleto.has(idExpediente)) continue
      setCargandoCompleto((prev) => new Set(prev).add(idExpediente))
      listarDocumentosAnalizados(idExpediente)
        // 30/08/2026, pedido explícito del usuario: "en los documentos del nestedgrid solo deben
        // aparecer las cartas intermedias o finales" — excluye Informes/Resoluciones y cualquier otro
        // documento sin clasificación (`clasificacion=''`, ver `CLAUDE.md` raíz sección Análisis).
        .then((docs) => docs.filter((d) => d.clasificacion === 'INTERMEDIO' || d.clasificacion === 'FINAL'))
        .then((docs) => setDocumentosCompletos((prev) => ({ ...prev, [idExpediente]: docs })))
        .catch(() => setDocumentosCompletos((prev) => ({ ...prev, [idExpediente]: [] })))
        .finally(() =>
          setCargandoCompleto((prev) => {
            const next = new Set(prev)
            next.delete(idExpediente)
            return next
          }),
        )
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [expedientesExpandidos])

  // Filas realmente renderizadas por el `<DataGrid>`: TODO expediente se colapsa en una fila
  // sintética `filaId` negativo (`-idExpediente`), sin importar cuántos documentos tenga en el
  // subconjunto de esta bandeja — el ícono de carpeta aparece siempre (Parte 7 de Bandeja de
  // supervisión, replicado acá tal cual).
  const filasAgrupadas = useMemo<FilaValidacion[]>(() => {
    const resultado: FilaValidacion[] = []
    for (const [idExpediente, docs] of gruposPorExpediente) {
      const referencia = documentoReferencia(docs)
      resultado.push({ ...referencia, filaId: -idExpediente, filaTipo: 'padre', cantidadDocumentos: docs.length })
      if (expedientesExpandidos.has(idExpediente)) {
        resultado.push({
          ...referencia,
          filaId: -idExpediente - 1_000_000,
          filaTipo: 'hijo',
          cantidadDocumentos: docs.length,
          documentosDelExpediente: documentosCompletos[idExpediente],
        })
      }
    }
    return resultado
  }, [gruposPorExpediente, expedientesExpandidos, documentosCompletos])

  function abrirWorkspace(row: NotificacionAsignacionDocumento) {
    const id = `ws-${row.idExpediente}`
    setActiveWorkspaceId(id)
    setWorkspacesAbiertos((prev) => {
      if (prev.some((w) => w.id === id)) {
        return prev.map((w) => (w.id === id ? { ...w, row } : w))
      }
      const color = WORKSPACE_COLORS[prev.length % WORKSPACE_COLORS.length]
      return [...prev, { id, row, color, activeSubTabId: 'datos' }]
    })
    if (datosPorExpediente[row.idExpediente]) return
    recargarDatosExpediente(row.idExpediente)
  }

  function recargarDatosExpediente(idExpediente: number) {
    setDatosPorExpediente((prev) => ({ ...prev, [idExpediente]: { detalle: prev[idExpediente]?.detalle ?? null, cargando: true, error: null } }))
    obtenerDatosExpediente(idExpediente)
      .then((detalle) => setDatosPorExpediente((prev) => ({ ...prev, [idExpediente]: { detalle, cargando: false, error: null } })))
      .catch((e) =>
        setDatosPorExpediente((prev) => ({
          ...prev,
          [idExpediente]: {
            detalle: prev[idExpediente]?.detalle ?? null,
            cargando: false,
            error: e instanceof ApiError ? e.message : 'No se pudo cargar el panel de datos.',
          },
        })),
      )
  }

  function cerrarWorkspace(id: string) {
    setWorkspacesAbiertos((prev) => {
      const idx = prev.findIndex((w) => w.id === id)
      const restantes = prev.filter((w) => w.id !== id)
      setActiveWorkspaceId((actual) => {
        if (actual !== id) return actual
        if (restantes.length === 0) return null
        return restantes[Math.min(idx, restantes.length - 1)].id
      })
      return restantes
    })
  }

  function seleccionarSubTab(workspaceId: string, subTabId: string) {
    setWorkspacesAbiertos((prev) => prev.map((w) => (w.id === workspaceId ? { ...w, activeSubTabId: subTabId } : w)))
  }

  const workspacesUI: Workspace[] = useMemo(
    () =>
      workspacesAbiertos.map((w) => {
        const estadoDatos = datosPorExpediente[w.row.idExpediente]
        return {
          id: w.id,
          label: w.row.titular || w.row.numeroExpediente || `Expediente ${w.row.idExpediente}`,
          color: w.color,
          activeSubTabId: w.activeSubTabId,
          subTabs: [
            {
              id: 'datos',
              label: 'Datos',
              color: COLOR_DATOS,
              content: (
                <PanelDatosExpediente
                  bandeja={adaptarParaPanelDatos(w.row)}
                  detalle={estadoDatos?.detalle ?? null}
                  cargando={estadoDatos?.cargando ?? false}
                  error={estadoDatos?.error ?? null}
                  permiteEdicion={false}
                  onGuardado={() => recargarDatosExpediente(w.row.idExpediente)}
                />
              ),
            },
            {
              id: 'trazabilidad',
              label: 'Trazabilidad',
              color: COLOR_TRAZABILIDAD,
              content: <TrazabilidadContent idExpediente={w.row.idExpediente} />,
            },
            {
              id: 'validar',
              label: 'Validar',
              color: COLOR_VALIDAR,
              content: <NotificacionValidacionContent idExpediente={w.row.idExpediente} documentoFoco={w.row} onActualizado={buscar} />,
            },
          ],
        }
      }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [workspacesAbiertos, datosPorExpediente],
  )

  // 30/08/2026, réplica literal del diseño de columnas de Bandeja de supervisión (pedido explícito
  // del usuario) — mismas 9 columnas (Días/Expediente/N° SGD/Fecha solicitud/Tipo documento/
  // Resultado/Abogado/Titular/Estado), con `colSpan` en `diasRestantes` para fusionar todas las
  // columnas restantes en la fila 'hijo' y renderizar ahí la nested grid ("tarjeta con acento
  // lateral", ver Parte 6 de esa página). "Clas. Documentos"/"N° Documento"/"Estado doc."/"Validador
  // asignado" (columnas propias que tenía esta bandeja antes) ya no se muestran como columna de la
  // fila padre — siguen visibles por documento dentro de la nested grid al expandir.
  const columnas: GridColDef<FilaValidacion>[] = [
    {
      field: 'expandir',
      headerName: '',
      width: 56,
      sortable: false,
      filterable: false,
      disableColumnMenu: true,
      renderCell: (params) => {
        const fila = params.row
        if (fila.filaTipo !== 'padre') return null
        const expandido = expedientesExpandidos.has(fila.idExpediente)
        return (
          <Tooltip title={expandido ? 'Colapsar documentos' : 'Ver documentos del expediente'}>
            <Badge badgeContent={fila.cantidadDocumentos} color="info" overlap="circular">
              <IconButton
                size="small"
                className={DATA_GRID_ROW_ICON_FOLDER_CLASS}
                onClick={(e) => {
                  e.stopPropagation()
                  alternarExpansionExpediente(fila.idExpediente)
                }}
              >
                {expandido ? <FolderOpenOutlinedIcon fontSize="small" /> : <CreateNewFolderOutlinedIcon fontSize="small" />}
              </IconButton>
            </Badge>
          </Tooltip>
        )
      },
    },
    {
      field: 'diasRestantes',
      headerName: 'Días',
      width: 90,
      colSpan: (_value, row) => (row.filaTipo === 'hijo' ? 100 : undefined),
      renderCell: (params) => {
        const fila = params.row
        if (fila.filaTipo === 'hijo') {
          const documentos = fila.documentosDelExpediente
          return (
            <Box
              sx={{
                width: 'fit-content',
                maxWidth: '100%',
                my: 0.75,
                py: 1,
                pl: 1.5,
                pr: 2,
                bgcolor: sdrercColors.softGray,
                borderLeft: `4px solid ${sdrercColors.info}`,
                borderRadius: '0 8px 8px 0',
              }}
            >
              <Typography
                variant="caption"
                sx={{
                  display: 'block',
                  mb: 0.75,
                  fontWeight: 700,
                  textTransform: 'uppercase',
                  letterSpacing: '0.06em',
                  color: sdrercColors.textSecondary,
                }}
              >
                Documentos del expediente{documentos ? ` (${documentos.length})` : ''}
              </Typography>
              {!documentos ? (
                <Typography variant="caption" color="text.secondary">
                  Cargando documentos del expediente…
                </Typography>
              ) : documentos.length === 0 ? (
                <Typography variant="caption" color="text.secondary">
                  Este expediente no tiene documentos registrados.
                </Typography>
              ) : (
                <TableContainer
                  sx={{
                    width: 'auto',
                    maxWidth: '100%',
                    bgcolor: sdrercColors.backgroundPaper,
                    borderRadius: 1.5,
                    border: `1px solid ${sdrercColors.divider}`,
                    boxShadow: '0 1px 2px rgba(16,24,40,0.05)',
                  }}
                >
                  <Table size="small" sx={{ width: 'auto', tableLayout: 'auto' }}>
                    <TableHead>
                      <TableRow>
                        <TableCell padding="checkbox" sx={{ borderBottom: `1px solid ${sdrercColors.divider}` }} />
                        {['Tipo', 'N° Documento', 'Estado', 'Fecha Emisión', '¿Requiere respuesta?'].map((titulo) => (
                          <TableCell
                            key={titulo}
                            sx={{
                              fontSize: '0.7rem',
                              fontWeight: 600,
                              textTransform: 'uppercase',
                              letterSpacing: '0.04em',
                              color: sdrercColors.textSecondary,
                              whiteSpace: 'nowrap',
                              borderBottom: `1px solid ${sdrercColors.divider}`,
                            }}
                          >
                            {titulo}
                          </TableCell>
                        ))}
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {documentos.map((doc) => {
                        const id = doc.idDocumentoAnalizado
                        return (
                          <TableRow key={id ?? `${doc.tipoDocumentoNombre}-${doc.numeroDocumento}`} hover>
                            <TableCell padding="checkbox">
                              <Checkbox size="small" checked={false} disabled />
                            </TableCell>
                            <TableCell sx={{ whiteSpace: 'nowrap' }}>{doc.tipoDocumentoNombre || '-'}</TableCell>
                            <TableCell sx={{ whiteSpace: 'nowrap' }}>{doc.numeroDocumento || '-'}</TableCell>
                            <TableCell sx={{ whiteSpace: 'nowrap' }}>{doc.estadoDocumentoNombre || '-'}</TableCell>
                            <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatearFecha(doc.fechaDocumento)}</TableCell>
                            <TableCell sx={{ whiteSpace: 'nowrap' }}>{doc.requiereRespuesta ? 'Sí' : 'No'}</TableCell>
                          </TableRow>
                        )
                      })}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Box>
          )
        }
        return (
          <StatusBadge
            label={params.value === null || params.value === undefined ? '-' : String(params.value)}
            tone={diasBadgeTone(params.value as number | null)}
          />
        )
      },
    },
    { field: 'numeroExpediente', headerName: 'Expediente', width: 190 },
    { field: 'numeroExpedienteSgd', headerName: 'N° expediente SGD', width: 160 },
    { field: 'fechaSolicitud', headerName: 'Fecha solicitud', width: 130, valueFormatter: (value) => formatearFecha(value as string | null) },
    { field: 'tipoDocumento', headerName: 'Tipo documento', width: 190 },
    { field: 'resultadoAnalisis', headerName: 'Resultado', width: 170, valueFormatter: (value) => (value as string) || '-' },
    { field: 'abogado', headerName: 'Abogado', width: 190, valueFormatter: (value) => (value as string) || '-' },
    { field: 'titular', headerName: 'Titular', flex: 1, minWidth: 200 },
    { field: 'estadoExpediente', headerName: 'Estado', width: 170 },
    // 02/09/2026, pedido explícito del usuario: falta la columna "Estado doc." (estado del
    // documento en sí, distinto de "Estado" del expediente) — mismo campo ya expuesto por el DTO.
    { field: 'estadoDocumento', headerName: 'Estado doc.', width: 150, valueFormatter: (value) => (value as string) || '-' },
  ]

  // Mismo mecanismo de `agruparHijosBajoExpediente` de Bandeja de supervisión — aplicado sobre TODAS
  // las columnas para que ordenar por cualquier cabecera no desagrupe una fila 'hijo' de su 'padre'.
  const columnasConAgrupacion = useMemo(
    () => columnas.map((columna) => ({ ...columna, sortComparator: agruparHijosBajoExpediente(columna.sortComparator) })),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [expedientesExpandidos],
  )

  const filtrosHeader = (
    <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap', rowGap: 1.5 }}>
      <TextField
        label="Buscar (N° expediente, SGD, titular, documento...)"
        size="small"
        value={texto}
        onChange={(e) => setTexto(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && buscar()}
        sx={{ minWidth: 280 }}
      />
      <Button variant="contained" size="small" onClick={handleBuscar} disabled={loading}>
        Buscar
      </Button>
      <Button variant="outlined" size="small" onClick={handleLimpiar} disabled={loading}>
        Limpiar
      </Button>
      <TextField
        label="Fecha Emisión desde"
        type="date"
        size="small"
        value={desde}
        onChange={(e) => setDesde(e.target.value)}
        slotProps={{ inputLabel: { shrink: true } }}
      />
      <TextField
        label="Fecha Emisión hasta"
        type="date"
        size="small"
        value={hasta}
        onChange={(e) => setHasta(e.target.value)}
        slotProps={{ inputLabel: { shrink: true } }}
      />
    </Stack>
  )

  return (
    <PageHeaderSticky
      titulo="Bandeja Validación"
      subtitulo="Cartas finales pendientes de validar."
      tabs={NOTIFICACION_TABS}
      filtros={filtrosHeader}
    >
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* 28/08/2026, pedido explícito del usuario en 2 pasadas: 1ra "que no sean 2 filas" (repeat(N,1fr)
          fijo -- desbordaba horizontalmente en vez de envolver al angostar la ventana, "se ve fatal");
          2da, tras verlo roto, "responsive real": `repeat(auto-fit, minmax(130px, 1fr))` -- mismo
          130px que el nuevo `minWidth` de MetricCard (ver su Javadoc), permite que las tarjetas quepan
          en 1 sola fila en pantallas normales Y envuelvan (no desborden) en pantallas angostas. */}
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: 2, mb: 2 }}>
        <Box onClick={() => setFiltroKpi(null)} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Pendientes" value={kpis.pendientes} caption="Para validar" color={sdrercColors.info} active={filtroKpi === null} />
        </Box>
        <Box onClick={() => alternarFiltro('POR_VENCER')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Por vencer" value={kpis.porVencer} caption="0 a 5 días hábiles" color={sdrercColors.warning} active={filtroKpi === 'POR_VENCER'} />
        </Box>
        <Box onClick={() => alternarFiltro('VENCIDOS')} sx={{ cursor: 'pointer' }}>
          <MetricCard label="Vencidos" value={kpis.vencidos} caption="Plazo excedido" color={sdrercColors.error} active={filtroKpi === 'VENCIDOS'} />
        </Box>
      </Box>

      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2, mb: 3, overflow: 'hidden', boxShadow: '0 1px 3px rgba(16,24,40,0.08), 0 1px 2px rgba(16,24,40,0.04)' }}>
        <DataGrid
          rows={filasAgrupadas}
          columns={columnasConAgrupacion}
          getRowId={(row) => row.filaId}
          loading={loading}
          density="compact"
          autoHeight
          getRowHeight={(params) => ((params.model as FilaValidacion).filaTipo === 'hijo' ? 'auto' : undefined)}
          showToolbar
          slots={{ toolbar: DataGridStandardToolbar }}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          onRowDoubleClick={(params) => {
            const fila = params.row as FilaValidacion
            if (fila.filaTipo === 'hijo') return
            abrirWorkspace(fila)
          }}
          sx={dataGridBaseSx}
        />
      </Box>

      <WorkspaceTabsDrawer
        workspaces={workspacesUI}
        activeWorkspaceId={activeWorkspaceId}
        onSelectWorkspace={setActiveWorkspaceId}
        onCloseWorkspace={cerrarWorkspace}
        onSelectSubTab={seleccionarSubTab}
      />
    </PageHeaderSticky>
  )
}
