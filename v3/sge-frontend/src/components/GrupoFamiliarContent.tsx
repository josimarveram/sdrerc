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
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import {
  asociarGrupoFamiliar,
  buscarPosiblesIntegrantesManual,
  eliminarAlertaPosibleGrupoFamiliar,
  eliminarDeGrupoFamiliar,
  listarIntegrantesGrupoFamiliar,
  listarPosiblesIntegrantes,
  obtenerEstadoAlertaGrupoFamiliar,
  type GrupoFamiliarCandidato,
  type GrupoFamiliarEstadoAlerta,
  type GrupoFamiliarIntegrante,
} from '../api/grupoFamiliarApi'
import { ApiError } from '../api/http'
import { useAppDialog } from '../dialogs/AppDialogProvider'
import { DATA_GRID_ROW_ICON_DELETE_CLASS } from '../dataGridTheme'

interface GrupoFamiliarContentProps {
  idExpedientePrincipal: number
  // Expediente seleccionado desde la grilla que dio origen a este workspace (19/08/2026, pedido
  // explícito del usuario): mismos datos que ya se muestran en el encabezado del tab superior y en
  // el "Panel de datos" de la subpestaña "Datos" — se reciben por prop en vez de volver a
  // consultarlos, el llamador (`RegistroBandejaPage.tsx`/`AsignacionBandejaPage.tsx`) ya los tiene
  // en la fila de la bandeja (`w.row`).
  numeroExpediente?: string
  titular?: string
  onAsociado?: () => void
}

/**
 * Contenido de "Grupo Familiar" sin envoltorio de `Dialog` — extraído de `GrupoFamiliarPanel.tsx`
 * para poder embeberse como subpestaña dentro de `WorkspaceTabsDrawer` (Bandeja Registro). Misma
 * lógica exacta, solo cambia el ciclo de vida: se carga al montar/cambiar `idExpedientePrincipal`
 * en vez de estar condicionado a un `open` de Dialog. `GrupoFamiliarPanel.tsx` sigue existiendo
 * como envoltorio `Dialog` delgado sobre este mismo componente, usado sin cambios por Asignación.
 *
 * **Ampliado el 13/08/2026, pedido explícito del usuario** ("revisa bien el panel de grupo familiar
 * de la v2 y replícalo... respetando el diseño de la v3"), tras revisar a fondo la clase real de V2
 * (`JPanelRegistrarGrupoFamiliarV2` en Registro / `crearPanelGrupoFamiliar` en Asignación): faltaba
 * la sección "Alerta" con el botón de descartarla sin asociar. Sí ya estaba completo el resto
 * (candidatos por apellidos, búsqueda manual, "Asociar al grupo familiar", "Grupo familiar actual"
 * con retirar integrante) — no se tocó esa parte.
 * - **Sección "Alerta"** (`obtenerEstadoAlertaGrupoFamiliar`, cargada junto con los candidatos):
 *   muestra el mensaje de la alerta "Posible Grupo Familiar" activa (si hay), en un `Alert
 *   severity="warning"`, con un ícono de papelera al lado para descartarla SIN asociar — "esto no es
 *   en realidad un posible integrante" (mismo botón papelera de V2, visible solo si hay alerta activa
 *   y el grupo todavía no está confirmado). Confirma con `window.confirm` antes de ejecutar (mismo
 *   patrón ya usado en `AsociarDuplicadosContent.tsx`/`AsignacionBandejaPage.tsx`, no un `Dialog` de
 *   MUI nuevo) — replica el `JOptionPane.showConfirmDialog` de V2.
 * - **Deliberadamente NO replicado** (cosmético, no funcional): en V2 la búsqueda manual está detrás
 *   de un ícono que la muestra/oculta (colapsada por defecto). Se mantiene el `TextField` siempre
 *   visible que ya tenía este componente — mejor UX, sin perder ninguna funcionalidad real.
 * - **Deliberadamente NO replicado** (fuera de alcance de este panel, no de esta subpestaña): la
 *   sección "Selección múltiple marcada" de V2 (marcar 2+ filas con checkbox directo en la bandeja
 *   principal y asociarlas de una vez, sin pasar por "posibles integrantes") se implementó en
 *   `RegistroBandejaPage.tsx` reutilizando la selección por checkbox ya existente de esa grilla
 *   (acción contextual nueva en la barra de "N seleccionados"), no acá — este componente solo cubre
 *   el flujo por expediente enfocado (workspace abierto).
 *
 * **Regla de "tamaño mínimo de grupo" (20/08/2026, pedido explícito del usuario)**: un grupo/alerta
 * no tiene sentido con 1 solo integrante — con 2, "descartar alerta" (`handleDescartarAlerta`) y
 * "retirar del grupo confirmado" (`handleRetirar`, botón "x") eliminan el vínculo de AMBAS
 * solicitudes (el backend ya lo hace así en cascada, ver `GrupoFamiliarDAO`); con 3 o más, solo
 * afectan a la solicitud sobre la que se actúa. El texto de confirmación (`window.confirm`, ambas
 * acciones) refleja el alcance real ANTES de ejecutar, usando el tamaño ya calculado dinámicamente
 * por el backend en la última carga (`estadoAlerta.tamanoGrupo` para la alerta, `integrantes.length`
 * para el grupo confirmado — ambos recién obtenidos por `cargar()`, nunca un valor viejo). `handleRetirar`
 * antes NO pedía confirmación en absoluto — se agregó en este mismo cambio.
 */
export function GrupoFamiliarContent({ idExpedientePrincipal, numeroExpediente, titular, onAsociado }: GrupoFamiliarContentProps) {
  const { confirm: confirmDialog } = useAppDialog()
  const [candidatos, setCandidatos] = useState<GrupoFamiliarCandidato[]>([])
  const [integrantes, setIntegrantes] = useState<GrupoFamiliarIntegrante[]>([])
  const [estadoAlerta, setEstadoAlerta] = useState<GrupoFamiliarEstadoAlerta | null>(null)
  const [seleccionados, setSeleccionados] = useState<number[]>([])
  const [textoBusqueda, setTextoBusqueda] = useState('')
  const [loading, setLoading] = useState(false)
  const [procesando, setProcesando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [mensaje, setMensaje] = useState<string | null>(null)

  function cargar() {
    setLoading(true)
    setError(null)
    Promise.all([
      listarPosiblesIntegrantes(idExpedientePrincipal),
      listarIntegrantesGrupoFamiliar(idExpedientePrincipal),
      obtenerEstadoAlertaGrupoFamiliar(idExpedientePrincipal),
    ])
      .then(([cand, integ, alerta]) => {
        setCandidatos(cand)
        setIntegrantes(integ)
        setEstadoAlerta(alerta)
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

  // 20/08/2026, pedido explícito del usuario (regla de "tamaño mínimo de grupo"): con 2 solicitudes
  // relacionadas (contando esta), descartar la alerta la elimina de AMBAS — no tiene sentido que
  // quede una sola marcada como "posible" sin nadie con quién agruparse. Con 3 o más, solo se
  // descarta la de esta solicitud, las demás mantienen la suya. `estadoAlerta.tamanoGrupo` ya viene
  // calculado dinámicamente por el backend (no cacheado) con el mismo criterio que aplicará la
  // eliminación real.
  function mensajeConfirmarDescartarAlerta(): string {
    const tamano = estadoAlerta?.tamanoGrupo ?? 0
    if (tamano > 0 && tamano <= 2) {
      return (
        'Esta alerta de Posible Grupo Familiar solo tiene 2 solicitudes relacionadas. ' +
        'Al eliminarla, se quitará la alerta de AMBAS solicitudes (ya no tendría sentido que quede una sola marcada).\n\n¿Desea continuar?'
      )
    }
    if (tamano > 2) {
      return (
        `Esta acción eliminará la alerta de Posible Grupo Familiar únicamente de esta solicitud. ` +
        `Las demás solicitudes relacionadas (${tamano - 1}) conservarán su alerta sin cambios.\n\n¿Desea continuar?`
      )
    }
    return 'Se eliminará la alerta de Posible Grupo Familiar de este expediente.\n\n¿Desea continuar?'
  }

  async function handleDescartarAlerta() {
    if (!estadoAlerta?.tieneAlerta) return
    const confirmado = await confirmDialog(mensajeConfirmarDescartarAlerta(), { tone: 'danger', confirmLabel: 'Descartar' })
    if (!confirmado) return
    setProcesando(true)
    setError(null)
    setMensaje(null)
    try {
      await eliminarAlertaPosibleGrupoFamiliar(idExpedientePrincipal)
      cargar()
      onAsociado?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo eliminar la alerta de Posible Grupo Familiar.')
    } finally {
      setProcesando(false)
    }
  }

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

  // 20/08/2026, pedido explícito del usuario (regla de "tamaño mínimo de grupo", faltaba
  // confirmación previa en esta acción): `integrantes.length` es el tamaño ACTUAL del grupo
  // confirmado del expediente enfocado (recién cargado por `cargar()`, no un valor viejo) — con 2
  // integrantes, retirar a uno disuelve el grupo por completo (el otro también pierde el vínculo);
  // con 3 o más, solo se retira al elegido y el resto conserva su pertenencia sin cambios.
  function mensajeConfirmarRetirar(integrante: GrupoFamiliarIntegrante): string {
    const nombre = integrante.nombreCompleto || 'esta persona'
    if (integrantes.length <= 2) {
      return (
        `Este grupo familiar confirmado solo tiene 2 integrantes. Al retirar a ${nombre}, el grupo se disolverá por completo ` +
        '(el otro integrante también perderá el vínculo; no tiene sentido un grupo de 1 sola persona).\n\n¿Desea continuar?'
      )
    }
    return (
      `Se retirará a ${nombre} del grupo familiar. Los demás integrantes (${integrantes.length - 1}) conservarán su pertenencia al grupo sin cambios.\n\n¿Desea continuar?`
    )
  }

  async function handleRetirar(integrante: GrupoFamiliarIntegrante) {
    if (!integrante.idExpediente) return
    const confirmado = await confirmDialog(mensajeConfirmarRetirar(integrante), { tone: 'danger', confirmLabel: 'Retirar' })
    if (!confirmado) return
    setProcesando(true)
    setError(null)
    setMensaje(null)
    try {
      const resultado = await eliminarDeGrupoFamiliar(integrante.idExpediente)
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

      {/* Recuadro informativo azul (19/08/2026, pedido explícito del usuario): contexto del
          expediente que dio origen a este workspace — mismo dato ya visible en la pestaña superior
          y en el "Panel de datos" de la subpestaña "Datos", recibido por prop en vez de volver a
          consultarlo. Elemento adicional, separado del `Alert severity="warning"` de "Posible Grupo
          Familiar" de abajo (no se fusionan ni se reemplazan entre sí). `severity="info"` es el
          mismo patrón azul ya usado en el sistema (ver `RegistroCargaDiariaPage.tsx`), con el ícono
          "i" en círculo por defecto de MUI — sin paleta nueva. */}
      {(numeroExpediente || titular) && (
        <Alert severity="info">
          Expediente seleccionado: {numeroExpediente || 'Sin número'}
          {titular ? ` — ${titular}` : ''}
        </Alert>
      )}

      {estadoAlerta?.tieneAlerta && (
        <Alert
          severity="warning"
          action={
            !estadoAlerta.grupoFamiliarConfirmado && (
              <Tooltip title="Eliminar alerta de Posible Grupo Familiar">
                <span>
                  <IconButton size="small" className={DATA_GRID_ROW_ICON_DELETE_CLASS} disabled={procesando} onClick={handleDescartarAlerta} aria-label="Eliminar alerta">
                    <DeleteOutlinedIcon fontSize="small" />
                  </IconButton>
                </span>
              </Tooltip>
            )
          }
        >
          {estadoAlerta.mensajeAlertaPosibleGrupoFamiliar}
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
                        <IconButton size="small" className={DATA_GRID_ROW_ICON_DELETE_CLASS} disabled={procesando || !i.idExpediente} onClick={() => handleRetirar(i)}>
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
