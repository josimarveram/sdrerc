import { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
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
import { useAppDialog } from '../dialogs/AppDialogProvider'

interface AsociarDuplicadosContentProps {
  idExpedientePrincipal: number
  onAsociado?: () => void
}

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

function tieneNumero(c: ExpedienteRelacionado | undefined): boolean {
  return !!(c && c.numeroExpediente && c.numeroExpediente.trim())
}

function requiereDecisionNumero(c: ExpedienteRelacionado | undefined): boolean {
  return !!c && c.etapaCodigo === 'REGISTRO' && c.estadoCodigo === 'REGISTRADO' && !tieneNumero(c)
}

function textoExpediente(c: ExpedienteRelacionado | undefined): string {
  if (!c) return '-'
  const numero = c.numeroExpediente || '(sin número)'
  return c.numeroExpedienteSgd ? `${numero} / SGD ${c.numeroExpedienteSgd}` : numero
}

// Colores replicados de V2 (JPanelAsociarDuplicadosRecepcionV2 en Registro / crearPanelAsociar en
// Asignación): fila "candidato con número" en ámbar, fila normal seleccionada en celeste.
const BG_CANDIDATO_NUMERO = '#FFF3DB'
const BG_CANDIDATO_NUMERO_SELECCIONADO = '#FFCE7A'
const BG_SELECCIONADO = '#DBF4F9'

/**
 * Contenido de "Asociar" sin envoltorio de `Dialog` — extraído de `AsociarDuplicadosPanel.tsx` para
 * poder embeberse como subpestaña dentro de `WorkspaceTabsDrawer` (Bandeja Registro).
 * `AsociarDuplicadosPanel.tsx` sigue existiendo como envoltorio `Dialog` delgado sobre este mismo
 * componente, usado sin cambios por Asignación.
 *
 * Reescrito el 13/08/2026, pedido explícito del usuario ("revisa bien el panel de asociar de la v2
 * y replícalo... respetando el diseño de la v3"), tras revisar a fondo la clase real de V2
 * (`JPanelBandejaExpedientesNueva.JPanelAsociarDuplicadosRecepcionV2`, Registro — misma lógica que
 * `JPanelAsignacionV2.crearPanelAsociar` en Asignación): la versión anterior (12/08/2026) trataba al
 * expediente enfocado del workspace como "el principal" siempre, sin checkbox propio, y documentaba
 * eso como "simplificación deliberada" — pero NO lo es: V2 resuelve el principal de forma dinámica
 * según cuántas solicitudes del grupo ya tienen número, y eso es justamente lo que hacía fallar la
 * asociación cuando el expediente abierto no era el que debía quedar como principal.
 *
 * **Resolución del expediente principal (idéntica a V2)**:
 * - TODAS las filas de "Solicitudes asociadas" tienen checkbox, incluida la del propio expediente
 *   enfocado del workspace (ya no se muestra como texto fijo "Principal" sin casilla).
 * - Si NINGUNA solicitud del grupo tiene número de expediente → el expediente del workspace actúa
 *   como principal automáticamente, sin elección manual.
 * - Si AL MENOS UNA (1 o más) ya tiene número → "principal ambiguo": el usuario debe marcar
 *   manualmente cuál de esas filas con número es el principal. Esas filas se pintan en ámbar y su
 *   checkbox se comporta como radio-button (marcar una desmarca cualquier otra con número ya
 *   marcada); las filas sin número siguen siendo casillas independientes normales.
 * - El principal elegido nunca se cuenta a sí mismo como "marcado para asociar", aunque su propia
 *   casilla esté tildada.
 * - Botón "Asociar al principal": deshabilitado con texto "Seleccione el expediente principal"
 *   mientras haya ambigüedad sin resolver; "Sin relacionados marcados" sin nada marcado (aparte del
 *   principal); si no, "Asociar al principal (N)".
 * - **Regla añadida sobre V2** (pedido explícito del usuario, turno previo: "no debería permitir
 *   asociar 2 expedientes sin número de expediente... no debería activarse el botón... hasta que se
 *   seleccione una casilla que sí tenga número"): si el principal elegido NO tiene número —caso
 *   posible solo cuando ningún miembro del grupo tiene número, ya que el flujo ambiguo obliga a
 *   elegir entre filas que sí lo tienen—, el botón queda deshabilitado ("El expediente principal
 *   necesita número de expediente") aunque haya filas marcadas, con aviso explicando que debe
 *   generarse un número primero en "Decisión de número". V2 no tiene este bloqueo: permite asociar
 *   duplicados sin número entre sí (ambos quedan sin número hasta que alguien decida generarlo
 *   después). Aquí se agrega porque fue pedido explícitamente como requisito nuevo de V3.
 *
 * **"Decisión de número"** (sección aparte, igual que V2): visible si el expediente del workspace en
 * sí necesita número (REGISTRO/REGISTRADO sin número), o si hay 1+ solicitudes del grupo sin número,
 * o si hay ambigüedad de principal. El botón genera un número NUEVO e independiente (no asocia) para
 * cada fila TILDADA que lo requiera, en lote (`generarNumerosParaMarcados` en V2) — deshabilitado
 * con "Marque solicitudes sin número para generar" mientras no haya ninguna marcada (13/08/2026,
 * pedido explícito del usuario: "cuando no selecciono ninguna de las casillas... no se debería
 * activar el botón generar número de expediente").
 * - **Detalle importante para no reintroducir el bug de V2**: el conteo de "marcadas" de esta acción
 *   se calcula sobre `seleccionados` en crudo (lo tildado en la grilla), NO sobre `idsMarcados` (que
 *   excluye al principal elegido, exclusión propia de la acción "Asociar al principal" — no tiene
 *   sentido acá). En V2 la sección SÍ reutiliza `obtenerIdsMarcados()` para este cálculo, lo que la
 *   deja con un callejón sin salida real: si el grupo no tiene hermanos (el expediente del workspace
 *   está solo y sin número), su propia casilla, aunque se tilde, siempre queda excluida por ser el
 *   principal automático — el botón de V2 nunca llega a habilitarse en ese caso, sin forma de
 *   generarle un número desde este panel. Al usar `seleccionados` en crudo se evita ese callejón sin
 *   perder el requisito nuevo del usuario: sigue deshabilitado sin nada tildado, pero SÍ se habilita
 *   si el usuario tilda a propósito la fila que necesita número, sea o no el principal actual.
 * - **Fix 19/08/2026 (pedido explícito del usuario, con capturas de pantalla)**: de las filas
 *   marcadas sin número, solo cuentan para "Generar número" las que NO van a asociarse al principal
 *   elegido de todas formas (es decir, el propio principal elegido si le falta número, o cualquier
 *   marcada mientras el principal siga sin resolver) — el resto ya queda cubierto por "Asociar al
 *   principal" (`idsMarcados`), así que generarles un número aparte no tiene sentido. Antes la
 *   condición comparaba contra `idExpedientePrincipal` (la prop de ESTE workspace, un accidente de
 *   por dónde se abrió el panel), lo que hacía que el mismo estado de selección mostrara el botón
 *   habilitado al abrir el panel desde el expediente principal pero deshabilitado al abrirlo desde
 *   el potencial duplicado — misma selección, resultado inconsistente según la pestaña activa.
 *
 * Confirmación antes de asociar vía `window.confirm` (mismo patrón ya usado en
 * `AsignacionBandejaPage.tsx`/`RegistroCargaDiariaPage.tsx`, no un `Dialog` de MUI nuevo) — V2 pide
 * confirmación con `JOptionPane` antes de ejecutar la asociación real.
 */
export function AsociarDuplicadosContent({ idExpedientePrincipal, onAsociado }: AsociarDuplicadosContentProps) {
  const { confirm: confirmDialog } = useAppDialog()
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

  // Ids del grupo que ya tienen número de expediente propio — determinan si hay ambigüedad de
  // principal (ver Javadoc del componente).
  const idsConNumero = useMemo(() => new Set(candidatos.filter(tieneNumero).map((c) => c.idExpediente)), [candidatos])
  const principalAmbiguo = idsConNumero.size > 0

  function alternarSeleccion(id: number) {
    const esConNumero = idsConNumero.has(id)
    setSeleccionados((prev) => {
      if (prev.includes(id)) {
        return prev.filter((x) => x !== id)
      }
      if (esConNumero) {
        // Radio-button entre las filas "con número": marcar una desmarca cualquier otra con número
        // ya marcada, sin tocar las selecciones de filas "sin número".
        return [...prev.filter((x) => !idsConNumero.has(x)), id]
      }
      return [...prev, id]
    })
  }

  const idConNumeroSeleccionado = seleccionados.find((id) => idsConNumero.has(id)) ?? null
  const idPrincipalElegido = principalAmbiguo ? idConNumeroSeleccionado : idExpedientePrincipal
  const idsMarcados = idPrincipalElegido === null ? [] : seleccionados.filter((id) => id !== idPrincipalElegido)

  // El principal elegido siempre tiene número cuando hay ambigüedad resuelta (por definición: solo
  // las filas "con número" participan de esa elección). Cuando NO hay ambigüedad (ninguna solicitud
  // del grupo tiene número, ni siquiera el propio expediente del workspace), el principal elegido
  // automáticamente tampoco tiene número — ese es exactamente el caso que no debe poder asociarse:
  // dos expedientes sin número de expediente no deben asociarse entre sí (pedido explícito del
  // usuario). Debe generarse primero un número (sección "Decisión de número" más abajo).
  const principalTieneNumero = idPrincipalElegido !== null && tieneNumero(candidatos.find((c) => c.idExpediente === idPrincipalElegido))

  let textoBotonAsociar: string
  let puedeAsociar: boolean
  if (principalAmbiguo && idPrincipalElegido === null) {
    textoBotonAsociar = 'Seleccione el expediente principal'
    puedeAsociar = false
  } else if (!principalTieneNumero) {
    textoBotonAsociar = 'El expediente principal necesita número de expediente'
    puedeAsociar = false
  } else if (idsMarcados.length === 0) {
    textoBotonAsociar = 'Sin relacionados marcados'
    puedeAsociar = false
  } else {
    textoBotonAsociar = `Asociar al principal (${idsMarcados.length})`
    puedeAsociar = true
  }

  async function ejecutarAsociacion() {
    if (!puedeAsociar || idPrincipalElegido === null) return
    const filaPrincipal = candidatos.find((c) => c.idExpediente === idPrincipalElegido)
    const confirmado = await confirmDialog(
      `Se asociarán ${idsMarcados.length} expediente(s) sin número como duplicados del expediente principal ${textoExpediente(filaPrincipal)}.`,
      { tone: 'info', confirmLabel: 'Asociar' },
    )
    if (!confirmado) return
    setProcesando(true)
    setError(null)
    setMensaje(null)
    try {
      const resultado = await asociarRelacionados({
        idExpedientePrincipal: idPrincipalElegido,
        idsRelacionados: idsMarcados,
        descripcion: 'Documento duplicado asociado desde Recepción por misma acta y titular.',
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

  // "Decisión de número": visible si el propio expediente del workspace lo requiere, o si hay 1+
  // solicitudes del grupo sin número, o si hay ambigüedad de principal — igual que V2.
  const filaPropia = candidatos.find((c) => c.idExpediente === idExpedientePrincipal)
  const propioRequiereNumero = requiereDecisionNumero(filaPropia)
  const candidatosSinNumero = candidatos.filter(requiereDecisionNumero)
  const mostrarDecisionNumero = propioRequiereNumero || candidatosSinNumero.length > 0 || principalAmbiguo

  // Ojo: se calcula sobre `seleccionados` (lo tildado en la grilla), NO sobre `idsMarcados` (que
  // excluye al principal elegido para la acción "Asociar"). Esa exclusión es propia de "Asociar" —
  // no tiene sentido acá: el usuario puede querer generar número justo para la fila que marcó,
  // sea o no el principal actual. Con esto, el botón queda deshabilitado sin nada tildado (pedido
  // explícito del usuario: "cuando no selecciono ninguna de las casillas... no se debería activar
  // el botón generar número de expediente") pero SÍ se habilita si el usuario tilda a propósito la
  // fila que necesita número (incluida la del propio workspace cuando no tiene hermanos en el grupo).
  const marcadosSinNumero = seleccionados.filter((id) => requiereDecisionNumero(candidatos.find((c) => c.idExpediente === id)))
  // 19/08/2026, pedido explícito del usuario (bug real reportado con capturas: el mismo estado de
  // selección mostraba el botón habilitado al abrir el panel desde el expediente PRINCIPAL, pero
  // deshabilitado al abrirlo desde el POTENCIAL DUPLICADO). Causa: la condición anterior comparaba
  // `idPrincipalElegido` (propio de la SELECCIÓN, compartido conceptualmente por todo el grupo)
  // contra `idExpedientePrincipal` (prop propia de CADA workspace — cuál expediente se enfocó al
  // abrir ESTE panel, un accidente de navegación, no una decisión del usuario) — dos conceptos
  // distintos que solo coinciden quen el panel se abre justo desde la fila elegida como principal.
  // El criterio correcto no depende de qué workspace está abierto: una fila marcada y sin número
  // NO necesita número propio si de todas formas va a asociarse al principal elegido (ya cubierta
  // por `idsMarcados`, ver botón "Asociar" arriba) — solo el propio principal elegido (o cualquier
  // marcada, mientras el principal siga sin resolver/ambiguo) puede necesitar "Generar número".
  const marcadosSinNumeroIndependientes = marcadosSinNumero.filter(
    (id) => idPrincipalElegido === null || id === idPrincipalElegido,
  )
  const todosCubiertosPorAsociacion = marcadosSinNumero.length > 0 && marcadosSinNumeroIndependientes.length === 0

  let textoBotonNumero: string
  let puedeGenerarNumero: boolean
  if (todosCubiertosPorAsociacion) {
    textoBotonNumero = 'Se asociará al expediente principal elegido'
    puedeGenerarNumero = false
  } else if (marcadosSinNumeroIndependientes.length > 0) {
    textoBotonNumero = `Generar número de expediente (${marcadosSinNumeroIndependientes.length})`
    puedeGenerarNumero = true
  } else {
    textoBotonNumero = 'Marque solicitudes sin número para generar'
    puedeGenerarNumero = false
  }

  async function handleGenerarNumero() {
    if (marcadosSinNumeroIndependientes.length === 0) return
    setProcesando(true)
    setError(null)
    setMensaje(null)
    try {
      const idsAGenerar = marcadosSinNumeroIndependientes
      const numeros: string[] = []
      for (const id of idsAGenerar) {
        const resultado = await generarNumeroExpediente(id)
        numeros.push(resultado.numeroExpediente)
      }
      setMensaje(
        numeros.length === 1
          ? `Número de expediente generado: ${numeros[0]}`
          : `Números de expediente generados: ${numeros.join(', ')}`,
      )
      cargar()
      onAsociado?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo generar el número de expediente.')
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

      {/* "Selección y alertas" (V2) */}
      <Box>
        <Typography variant="subtitle2">Solicitudes asociadas</Typography>
        <Typography variant="caption" color="text.secondary">
          Se muestran todas las solicitudes con la misma acta y titular, incluida la principal.
          {principalAmbiguo
            ? ' Al menos una ya tiene número: marque (en ámbar) cuál debe ser el expediente principal.'
            : ' Marque las que aún no tienen número para asociarlas al expediente principal.'}
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
                const esWorkspace = c.idExpediente === idExpedientePrincipal
                const esConNumero = idsConNumero.has(c.idExpediente)
                const marcada = seleccionados.includes(c.idExpediente)
                const bgcolor = esConNumero
                  ? marcada
                    ? BG_CANDIDATO_NUMERO_SELECCIONADO
                    : BG_CANDIDATO_NUMERO
                  : marcada
                    ? BG_SELECCIONADO
                    : undefined
                return (
                  <TableRow key={c.idExpediente} sx={{ bgcolor }}>
                    <TableCell padding="checkbox">
                      <Tooltip
                        title={
                          esConNumero
                            ? 'Este expediente ya tiene número: márquelo para elegirlo como expediente principal (solo se puede elegir uno).'
                            : 'Marque para asociar esta solicitud al expediente principal, o para generar un número de expediente independiente en "Decisión de número".'
                        }
                      >
                        <Checkbox size="small" checked={marcada} onChange={() => alternarSeleccion(c.idExpediente)} />
                      </Tooltip>
                    </TableCell>
                    <TableCell>
                      <Stack direction="row" spacing={0.5} alignItems="center">
                        <span>{c.numeroExpediente || '-'}</span>
                        {esWorkspace && <Chip label="Este workspace" size="small" variant="outlined" />}
                      </Stack>
                    </TableCell>
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

      {/* "Asociación rápida" (V2) */}
      <Box>
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
          Expediente principal: {idPrincipalElegido !== null ? textoExpediente(candidatos.find((c) => c.idExpediente === idPrincipalElegido)) : 'Seleccione el expediente principal'}
        </Typography>
      </Box>
      <Stack spacing={0.5} sx={{ alignItems: 'flex-start' }}>
        <Tooltip title={idsMarcados.length > 0 && !principalTieneNumero ? 'No se pueden asociar 2 expedientes sin número entre sí. Genere primero un número en "Decisión de número".' : ''}>
          <span>
            <Button variant="contained" disabled={procesando || !puedeAsociar} onClick={ejecutarAsociacion}>
              {textoBotonAsociar}
            </Button>
          </span>
        </Tooltip>
        {idsMarcados.length > 0 && !principalTieneNumero && (
          <Typography variant="caption" color="error">
            No se pueden asociar 2 expedientes sin número entre sí. Genere primero un número en "Decisión de número".
          </Typography>
        )}
      </Stack>

      <Typography variant="caption" color="text.secondary">
        Los expedientes ya asociados se ven expandiendo la fila principal en la Bandeja Registro (icono "+").
      </Typography>

      {/* "Decisión de número" (V2) */}
      {mostrarDecisionNumero && (
        <Box sx={{ pt: 1, borderTop: 1, borderColor: 'divider' }}>
          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            Decisión de número
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>
            Genera un número de expediente nuevo e independiente para las solicitudes marcadas sin número, en vez de asociarlas al
            principal. Use esta opción solo si no corresponde asociarlas como duplicado.
          </Typography>
          <Button variant="outlined" disabled={procesando || !puedeGenerarNumero} onClick={handleGenerarNumero}>
            {textoBotonNumero}
          </Button>
        </Box>
      )}
    </Stack>
  )
}
