import { useEffect, useMemo, useRef, useState } from 'react'
import {
  Alert,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
import DownloadOutlinedIcon from '@mui/icons-material/DownloadOutlined'
import {
  agregarAbogadoASupervisor,
  confirmarImportacionSupervision,
  descargarPlantillaSupervision,
  listarAbogadosPorSupervisor,
  listarSupervisoresConAbogados,
  previsualizarExcelSupervision,
  quitarAbogadoDeSupervisor,
  type AbogadoSupervisado,
  type EquipoSupervisionImportItem,
  type EquipoSupervisionImportResultado,
  type UsuarioAsignableEquipo,
} from '../api/adminEquipoJuridicoApi'
import { ApiError, descargarBlob } from '../api/http'
import { useAppDialog } from '../dialogs/AppDialogProvider'
import { StatusBadge } from '../components/StatusBadge'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { ADMIN_EQUIPO_JURIDICO_TABS } from '../layout/moduleTabs'

/**
 * "Personal por supervisor" (Administración &gt; Equipo Jurídico, 2ª pestaña): combo de
 * supervisor + grilla de abogados supervisados con checkbox "Activo" inline (mismo patrón que
 * V2, `AbogadosSupervisorTableModel`), más el botón "Plantilla Excel" (descargar/previsualizar-
 * importar) descrito en el plan aprobado — versión simplificada respecto a la plantilla legacy de
 * V1, ver Javadoc de `EquipoSupervisionExcelService` (backend).
 */
export function AdminPersonalPorSupervisorPage() {
  const [supervisores, setSupervisores] = useState<UsuarioAsignableEquipo[]>([])
  const [idSupervisor, setIdSupervisor] = useState<number | ''>('')
  const [abogados, setAbogados] = useState<AbogadoSupervisado[]>([])
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [excelAbierto, setExcelAbierto] = useState(false)

  useEffect(() => {
    listarSupervisoresConAbogados().then(setSupervisores).catch(() => setSupervisores([]))
  }, [])

  async function buscar() {
    if (idSupervisor === '') return
    setCargando(true)
    setError(null)
    try {
      setAbogados(await listarAbogadosPorSupervisor(idSupervisor))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo cargar el personal supervisado.')
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    if (idSupervisor !== '') buscar()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [idSupervisor])

  async function toggleActivo(abogado: AbogadoSupervisado) {
    if (idSupervisor === '') return
    try {
      if (abogado.relacionActiva) {
        await quitarAbogadoDeSupervisor(idSupervisor, abogado.idUsuario)
      } else {
        await agregarAbogadoASupervisor(idSupervisor, abogado.idUsuario)
      }
      await buscar()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo actualizar la relación de supervisión.')
    }
  }

  return (
    <PageHeaderSticky
      titulo="Personal por supervisor"
      subtitulo="Relación de supervisión entre abogados y sus supervisores."
      tabs={ADMIN_EQUIPO_JURIDICO_TABS}
      acciones={
        <Button variant="outlined" startIcon={<UploadFileOutlinedIcon />} onClick={() => setExcelAbierto(true)}>
          Plantilla Excel
        </Button>
      }
      filtros={
        <Stack direction="row" spacing={2} alignItems="center" flexWrap="wrap">
          <TextField
            select
            label="Supervisor"
            size="small"
            sx={{ minWidth: 280 }}
            value={idSupervisor === '' ? '' : String(idSupervisor)}
            onChange={(e) => setIdSupervisor(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <MenuItem value="">Seleccione un supervisor</MenuItem>
            {supervisores.map((s) => (
              <MenuItem key={s.idUsuario} value={String(s.idUsuario)}>
                {s.nombreCompleto || s.username}
              </MenuItem>
            ))}
          </TextField>
          <Button variant="contained" onClick={buscar} disabled={idSupervisor === ''}>
            Buscar
          </Button>
        </Stack>
      }
    >
      <Stack spacing={2}>
        {error && <Alert severity="error">{error}</Alert>}
        {idSupervisor === '' ? (
          <Typography color="text.secondary">Seleccione un supervisor para ver su personal.</Typography>
        ) : (
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Activo</TableCell>
                <TableCell>Usuario</TableCell>
                <TableCell>Nombre</TableCell>
                <TableCell>Roles</TableCell>
                <TableCell>Equipos</TableCell>
                <TableCell>Estado</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {abogados.map((a) => (
                <TableRow key={a.idUsuario} sx={{ opacity: a.relacionActiva ? 1 : 0.55 }}>
                  <TableCell>
                    <Checkbox checked={a.relacionActiva} onChange={() => toggleActivo(a)} size="small" />
                  </TableCell>
                  <TableCell>{a.username}</TableCell>
                  <TableCell>{a.nombreCompleto}</TableCell>
                  <TableCell>{a.rolesResumen}</TableCell>
                  <TableCell>{a.equiposResumen}</TableCell>
                  <TableCell>
                    <StatusBadge label={a.relacionActiva ? 'Activo' : 'Inactivo'} tone={a.relacionActiva ? 'success' : 'neutral'} />
                  </TableCell>
                </TableRow>
              ))}
              {abogados.length === 0 && !cargando && (
                <TableRow>
                  <TableCell colSpan={6}>
                    <Typography color="text.secondary" sx={{ py: 2 }}>
                      Este supervisor no tiene abogados asociados.
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        )}
      </Stack>

      {excelAbierto && <DlgExcelSupervision onClose={() => setExcelAbierto(false)} onImportado={buscar} />}
    </PageHeaderSticky>
  )
}

function DlgExcelSupervision({ onClose, onImportado }: { onClose: () => void; onImportado: () => void }) {
  const { confirm: confirmDialog } = useAppDialog()
  const inputRef = useRef<HTMLInputElement>(null)
  const [previsualizacion, setPrevisualizacion] = useState<EquipoSupervisionImportItem[] | null>(null)
  const [resultado, setResultado] = useState<EquipoSupervisionImportResultado[] | null>(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function descargarPlantilla() {
    try {
      const blob = await descargarPlantillaSupervision()
      descargarBlob(blob, 'plantilla_personal_por_supervisor_sdrerc.xlsx')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo descargar la plantilla.')
    }
  }

  async function seleccionarArchivo(e: React.ChangeEvent<HTMLInputElement>) {
    const archivo = e.target.files?.[0]
    if (!archivo) return
    setError(null)
    setResultado(null)
    setCargando(true)
    try {
      setPrevisualizacion(await previsualizarExcelSupervision(archivo));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo previsualizar el archivo.')
    } finally {
      setCargando(false)
      if (inputRef.current) inputRef.current.value = ''
    }
  }

  const resumen = useMemo(() => {
    if (!previsualizacion) return null
    const validos = previsualizacion.filter((i) => i.estadoValidacion === 'VALIDO').length
    const advertencias = previsualizacion.filter((i) => i.estadoValidacion === 'ADVERTENCIA').length
    const errores = previsualizacion.filter((i) => i.estadoValidacion === 'ERROR').length
    return { validos, advertencias, errores, total: previsualizacion.length }
  }, [previsualizacion])

  async function confirmar() {
    if (!previsualizacion) return
    const incluirAdvertencias =
      resumen && resumen.advertencias > 0
        ? await confirmDialog(`Hay ${resumen.advertencias} fila(s) con advertencia. ¿Desea incluirlas en la importación?`, {
            tone: 'warning',
            confirmLabel: 'Incluir',
          })
        : true
    setCargando(true)
    setError(null)
    try {
      const res = await confirmarImportacionSupervision(previsualizacion, incluirAdvertencias)
      setResultado(res)
      onImportado()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo confirmar la importación.')
    } finally {
      setCargando(false)
    }
  }

  return (
    <Dialog open onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Plantilla Excel — Personal por supervisor</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Stack direction="row" spacing={2}>
            <Button variant="outlined" startIcon={<DownloadOutlinedIcon />} onClick={descargarPlantilla}>
              Descargar plantilla
            </Button>
            <Button variant="outlined" component="label" startIcon={<UploadFileOutlinedIcon />}>
              Seleccionar archivo
              <input ref={inputRef} type="file" accept=".xlsx" hidden onChange={seleccionarArchivo} />
            </Button>
          </Stack>
          {error && <Alert severity="error">{error}</Alert>}
          {resumen && !resultado && (
            <Alert severity={resumen.errores > 0 ? 'warning' : 'success'}>
              {resumen.total} fila(s): {resumen.validos} válida(s), {resumen.advertencias} con advertencia, {resumen.errores} con error.
            </Alert>
          )}
          {previsualizacion && !resultado && (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Fila</TableCell>
                  <TableCell>Abogado</TableCell>
                  <TableCell>Supervisor</TableCell>
                  <TableCell>Validación</TableCell>
                  <TableCell>Observación</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {previsualizacion.map((item) => (
                  <TableRow key={item.fila}>
                    <TableCell>{item.fila}</TableCell>
                    <TableCell>{item.usuarioAbogado}</TableCell>
                    <TableCell>{item.usuarioSupervisor}</TableCell>
                    <TableCell>
                      <StatusBadge
                        label={item.estadoValidacion === 'VALIDO' ? 'Válido' : item.estadoValidacion === 'ADVERTENCIA' ? 'Advertencia' : 'Error'}
                        tone={item.estadoValidacion === 'VALIDO' ? 'success' : item.estadoValidacion === 'ADVERTENCIA' ? 'warning' : 'error'}
                      />
                    </TableCell>
                    <TableCell>{item.mensaje ?? '-'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          {resultado && (
            <>
              <Alert severity="success">Importación procesada.</Alert>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Fila</TableCell>
                    <TableCell>Abogado</TableCell>
                    <TableCell>Supervisor</TableCell>
                    <TableCell>Resultado</TableCell>
                    <TableCell>Mensaje</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {resultado.map((item) => (
                    <TableRow key={item.fila}>
                      <TableCell>{item.fila}</TableCell>
                      <TableCell>{item.usuarioAbogado}</TableCell>
                      <TableCell>{item.usuarioSupervisor}</TableCell>
                      <TableCell>
                        <StatusBadge
                          label={item.resultado === 'IMPORTADO' ? 'Importado' : item.resultado === 'OMITIDO' ? 'Omitido' : 'Error'}
                          tone={item.resultado === 'IMPORTADO' ? 'success' : item.resultado === 'OMITIDO' ? 'neutral' : 'error'}
                        />
                      </TableCell>
                      <TableCell>{item.mensaje ?? '-'}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </>
          )}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cerrar</Button>
        {previsualizacion && !resultado && (
          <Button variant="contained" onClick={confirmar} disabled={cargando || resumen?.validos === 0 && resumen?.advertencias === 0}>
            Confirmar importación
          </Button>
        )}
      </DialogActions>
    </Dialog>
  )
}
