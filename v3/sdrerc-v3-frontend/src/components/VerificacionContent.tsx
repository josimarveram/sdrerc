import { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  IconButton,
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
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined'
import {
  aprobarVerificacionConDestino,
  registrarAprobado,
  guardarDocumentoVerificado,
  listarDocumentosVerificados,
  listarEquiposDestinoVerificacion,
  listarUsuariosDestinoVerificacion,
  registrarObservado,
  type VerificacionExpediente,
} from '../api/verificacionApi'
import type { EquipoAsignacion, UsuarioAsignable } from '../api/asignacionApi'
import { listarEstadosDocumentoAnalisis, type DocumentoAnalizado } from '../api/analisisApi'
import type { CatalogoItem } from '../api/registroApi'
import { ApiError } from '../api/http'
import { useAppDialog } from '../dialogs/AppDialogProvider'
import { hoyISO } from '../utils/fechas'
import { DATA_GRID_ROW_ICON_SAVE_CLASS } from '../dataGridTheme'
import { SectionCard } from './RegistroExpedienteForm'
import { StatusBadge } from './StatusBadge'

interface VerificacionContentProps {
  expediente: VerificacionExpediente
  onActualizado?: () => void
}

const EQUIPOS_DESTINO_CODIGOS = new Set(['EQ_ANALISIS', 'EQ_EJECUCION', 'EQ_SUPERVISION'])

const ETIQUETA_DESTINO: Record<string, string> = {
  EQ_ANALISIS: 'Devolver a Análisis',
  EQ_EJECUCION: 'Enviar a Ejecución',
  EQ_SUPERVISION: 'Enviar a Supervisión',
}

// 01/09/2026, bug real corregido (pedido explícito del usuario): `WorkspaceTabsDrawer` solo
// renderiza el `content` de la subpestaña ACTIVA — al cambiar a "Datos" y volver a "Verificar",
// este componente se DESMONTA y se vuelve a MONTAR desde cero, así que un `useState` local nunca
// puede sobrevivir ese viaje. La fotografía "documentos ya emitidos al abrir" (`idsYaEmitidosAlAbrir`,
// ver su Javadoc más abajo) necesita sobrevivir exactamente ese remount para no volver a capturar,
// como si fueran "ya emitidos desde antes", documentos que el propio usuario acaba de guardar como
// Emitido en la sesión actual (ej. una Resolución) — antes de este fix, cualquier ida y vuelta entre
// subpestañas bloqueaba también esos documentos recién tocados, no solo los que de verdad ya venían
// cerrados de un módulo anterior (ej. el Informe de Análisis). Cache a nivel de MÓDULO (no de
// componente): sobrevive al desmontaje de React porque vive en memoria del proceso del navegador,
// no en el árbol de componentes — se descarta solo si el usuario recarga la página completa (mismo
// costo aceptado que otros cachés en memoria de este proyecto, acotado por expediente).
const idsYaEmitidosAlAbrirPorExpediente = new Map<number, Set<number>>()

/**
 * Contenido de la subpestaña "Verificar" del workspace multipestañas de la Bandeja Verificación.
 * "Datos" (la otra subpestaña) es la misma de Registro/Análisis pero con `permiteEdicion={false}`
 * (ver `PanelDatosExpediente.tsx`).
 *
 * **Rediseño 21/08/2026, pedido explícito del usuario**: "Registrar verificación" pasó de ser una
 * acción que YA MOVÍA el expediente fuera de Verificación (a Análisis si Observado, o directo a
 * Ejecución si Aprobado + resultado resolutivo) a ser una acción que SOLO GUARDA — mismo patrón que
 * "Registrar resultado final" en Análisis (`AnalisisContent.tsx`), que guarda sin salir de la etapa.
 * El envío real ahora ocurre siempre en un segundo paso independiente, gatillado por el botón
 * dinámico de "Destino operativo" ("Devolver a Análisis"/"Enviar a Ejecución"/"Enviar a
 * Supervisión"), igual que "Enviar a Verificación" es un botón aparte en Análisis. Ver Javadoc de
 * `VerificacionExpedienteDAO` (backend) para el detalle completo de las 2 transiciones nuevas que
 * hizo falta agregar al catálogo (`db/sdrerc_app/scripts/109_transiciones_verificacion_registrar_enviar.sql`,
 * creado, NO ejecutado) y las que ya existían.
 *
 * Estructura de 5 secciones, mismo orden que antes del rediseño (solo cambió CUÁNDO se muestra cada
 * una, no el orden): "Estado de la verificación" → "Documentos verificados" → "Documentos de
 * respuesta" → "Resultado de la verificación" → "Destino operativo" (visible SOLO cuando
 * `expediente.observado || expediente.aprobado` — es decir, DESPUÉS de haber registrado un
 * resultado; puede convivir con "Resultado" en pantalla, ver bullet siguiente).
 *
 * **"Resultado de la verificación" ya NO se oculta tras registrar** (21/08/2026, 3ª pasada, pedido
 * explícito del usuario: "no debería ocultarse ese bloque"). Antes, la sección entera desaparecía en
 * cuanto `expediente.registrableVerificacion` pasaba a `false` (justo después de un "Registrar
 * verificación" exitoso), dejando al usuario sin ver qué acababa de registrar. Ahora la sección
 * siempre se muestra mientras `enVerificacion` (igual que "Documentos verificados"); cuando ya NO se
 * puede registrar (`!puedeRegistrar`, resultado ya fijado), se agrega un `Alert severity="info"`
 * arriba indicando qué se registró ("Aprobado"/"Observado") y remitiendo a "Destino operativo", y
 * los 3 campos + el botón quedan `disabled` (el combo/campos siguen visibles, solo no editables —
 * coherente con que el backend de todas formas rechazaría un 2º registro desde un estado que ya no
 * es EN_VERIFICACION).
 *
 * **Filtro del combo "Equipo destino" según el resultado ya registrado** (2ª pasada del rediseño,
 * mismo día, corrige un error real de la 1ª pasada): el criterio NO es "resultado de Análisis
 * resolutivo" (esa 1ª versión bloqueaba el combo incorrectamente, excluyendo Ejecución cuando en
 * realidad el destino real depende de la CLASIFICACIÓN DEL DOCUMENTO — FINAL=resolución,
 * INTERMEDIO=oficio/carta intermedia, ver `CLAUDE.md` raíz —, no del resultado de Análisis). El
 * combo `equiposFiltrados` **nunca se deshabilita**; solo se filtran las opciones válidas:
 * - Observado (estado REQUIERE_CORRECCION): solo `EQ_ANALISIS` (único destino posible).
 * - Aprobado (estado VERIFICADO): `EQ_EJECUCION` (documento FINAL/resolución) o `EQ_SUPERVISION`
 *   (documento INTERMEDIO/oficio o carta intermedia) — el usuario elige libremente según el
 *   documento real que está verificando, sin restricción automática ni bloqueo; `EQ_ANALISIS` deja
 *   de ser una opción para Aprobado (un expediente ya Aprobado nunca vuelve a Análisis).
 * El backend valida el mismo criterio (Aprobado + `EQ_ANALISIS` se rechaza server-side), pero no
 * intenta determinar automáticamente FINAL/INTERMEDIO — es el usuario quien decide.
 *
 * El campo "Comentario" de "Resultado de la verificación" es independiente del de "Destino
 * operativo" (`comentarioDestino`, estado separado) — cada acción es su propio paso ahora, con su
 * propio comentario para el historial, en vez de compartir un único campo como antes del rediseño.
 *
 * **Mensajes de resultado a la derecha del botón, no en un Alert compartido al tope del panel**
 * (21/08/2026, pedido explícito del usuario): "Registrar verificación" y "Enviar a ..." tienen cada
 * uno su propio par de estados (`mensajeResultado`/`errorResultado`, `mensajeDestino`/`errorDestino`),
 * renderizados en un `Stack direction="row"` junto a su propio botón. El `error` genérico que queda
 * al tope del panel es exclusivamente para el ícono "Guardar" de la grilla "Documentos verificados"
 * (esa acción no fue parte de este pedido).
 *
 * "Destino operativo" sigue el mismo patrón visual de `AsignacionSeleccionContent.tsx` (campo de
 * solo lectura arriba + combo Equipo destino + combo Usuario destino en cascada), sin el campo
 * "Supervisor" (Verificación no tiene ese concepto). El campo de solo lectura ("Abogado {equipo}")
 * replica `actualizarAbogadoAnalisisDestino()` de V2 con una diferencia documentada: V2 tiene
 * fallback a `item.getEquipo()` (nombre de equipo GENÉRICO del expediente) que
 * `VerificacionExpedienteDTO` (V3) no expone — el label cae a "Abogado" sin sufijo de equipo en ese
 * caso, mientras el valor sí conserva el fallback completo (`responsableAnalisis || responsable`).
 *
 * La grilla "Documentos verificados" reutiliza el mismo dato que Análisis
 * (`EXPEDIENTE_DOCUMENTO_ANALIZADO`, vía `DocumentoAnalisisDAO`) expuesto bajo `/api/verificacion`
 * por higiene de API. Columnas iguales a `DocumentoVerificacionTreeGridPanelV2` (V2): Tipo
 * documento es de solo lectura acá (a diferencia de Análisis, donde es editable); "Comentario"
 * escribe en la misma columna `descripcion` que en Análisis. La tabla hija "Documentos de respuesta"
 * es enteramente de solo lectura, sin ícono Guardar, igual que V2.
 *
 * **2 fixes reales, 01/09/2026, pedido explícito del usuario**: (1) el combo "Estado documento" ya
 * autocompleta "Fecha Emisión" con la fecha de hoy al elegir "Emitido" (y la limpia con cualquier
 * otro valor) — mismo criterio ya vigente en Análisis/Ejecución, que faltaba acá. (2) el bloqueo de
 * fila (`docEmitido`) ya NO se recalcula en cada remount del componente al cambiar de subpestaña —
 * ver `idsYaEmitidosAlAbrirPorExpediente` (cache a nivel de módulo, arriba) — y además se combina
 * con `!puedeRegistrar` (todo se bloquea sin excepción recién cuando la verificación YA fue
 * registrada, nunca antes).
 */
export function VerificacionContent({ expediente, onActualizado }: VerificacionContentProps) {
  const { confirm: confirmDialog } = useAppDialog()
  const [resultado, setResultado] = useState<'APROBADO' | 'OBSERVADO'>('APROBADO')
  const [comentario, setComentario] = useState('')
  const [descripcionObservacion, setDescripcionObservacion] = useState('')
  const [comentarioDestino, setComentarioDestino] = useState('')
  const [equipos, setEquipos] = useState<EquipoAsignacion[]>([])
  const [usuarios, setUsuarios] = useState<UsuarioAsignable[]>([])
  const [idEquipoDestino, setIdEquipoDestino] = useState('')
  const [idUsuarioDestino, setIdUsuarioDestino] = useState('')
  const [procesando, setProcesando] = useState(false)
  const [procesandoDestino, setProcesandoDestino] = useState(false)
  // Error genérico, solo para el ícono "Guardar" de la grilla "Documentos verificados" (sin mensaje
  // de éxito propio). Los mensajes de "Registrar verificación"/"Enviar a ..." tienen su propio
  // estado (mensajeResultado/errorResultado, mensajeDestino/errorDestino) — 21/08/2026, pedido
  // explícito del usuario: deben aparecer a la derecha de su propio botón, no en un Alert compartido
  // al tope del panel.
  const [error, setError] = useState<string | null>(null)
  const [mensajeResultado, setMensajeResultado] = useState<string | null>(null)
  const [errorResultado, setErrorResultado] = useState<string | null>(null)
  const [mensajeDestino, setMensajeDestino] = useState<string | null>(null)
  const [errorDestino, setErrorDestino] = useState<string | null>(null)

  const [estadosDocumento, setEstadosDocumento] = useState<CatalogoItem[]>([])
  const [documentos, setDocumentos] = useState<DocumentoAnalizado[]>([])
  const [documentosLoading, setDocumentosLoading] = useState(false)
  const [padreFocoId, setPadreFocoId] = useState<number | null>(null)
  const [guardandoId, setGuardandoId] = useState<number | null>(null)

  const enVerificacion = expediente.etapaCodigo === 'VERIFICACION'
  const puedeRegistrar = expediente.registrableVerificacion
  const puedeEnviar = expediente.observado || expediente.aprobado
  const labelAbogadoDestino = expediente.equipoAnalisis ? `Abogado ${expediente.equipoAnalisis}` : 'Abogado'
  const valorAbogadoDestino = expediente.responsableAnalisis || expediente.responsable || '-'

  // Filtro de opciones del combo "Equipo destino" — 21/08/2026, 2ª pasada, pedido explícito del
  // usuario: el criterio real NO es "resultado de Análisis resolutivo" (eso bloqueaba
  // incorrectamente el combo, excluyendo Ejecución/Supervisión por error). El combo NUNCA se
  // deshabilita; solo se filtran las opciones válidas según el resultado YA registrado:
  // - Observado -> solo Análisis (único destino posible para una observación).
  // - Aprobado -> Ejecución (documento FINAL/resolución) o Supervisión (documento
  //   INTERMEDIO/oficio o carta intermedia) — el usuario elige según el documento real, sin
  //   restricción automática ni bloqueo.
  const equiposFiltrados = useMemo(() => {
    if (expediente.observado) return equipos.filter((e) => e.codigo === 'EQ_ANALISIS')
    if (expediente.aprobado) return equipos.filter((e) => e.codigo === 'EQ_EJECUCION' || e.codigo === 'EQ_SUPERVISION')
    return equipos
  }, [equipos, expediente.observado, expediente.aprobado])
  const ayudaEquipoDestino = expediente.observado
    ? 'Un expediente Observado solo puede enviarse al equipo de Análisis.'
    : expediente.aprobado
      ? 'Elija Ejecución si el documento es una resolución (FINAL) o Supervisión si es un oficio/carta intermedia (INTERMEDIO).'
      : undefined

  const equipoDestinoSeleccionado = equipos.find((eq) => String(eq.idEquipo) === idEquipoDestino)
  const labelBotonDestino = equipoDestinoSeleccionado ? ETIQUETA_DESTINO[equipoDestinoSeleccionado.codigo] : null

  useEffect(() => {
    listarEstadosDocumentoAnalisis()
      .then(setEstadosDocumento)
      .catch(() => undefined)
    listarEquiposDestinoVerificacion()
      .then((lista) => setEquipos(lista.filter((e) => EQUIPOS_DESTINO_CODIGOS.has(e.codigo))))
      .catch(() => setEquipos([]))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    setResultado('APROBADO')
    setComentario('')
    setDescripcionObservacion('')
    setComentarioDestino('')
    setIdEquipoDestino('')
    setIdUsuarioDestino('')
    setError(null)
    setMensajeResultado(null)
    setErrorResultado(null)
    setMensajeDestino(null)
    setErrorDestino(null)
    recargarDocumentos(true)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [expediente.idExpediente])

  // Autoselecciona el equipo destino solo cuando queda una única opción posible (Observado -> solo
  // Análisis) — mera conveniencia, el combo sigue habilitado y el usuario igual debe hacer clic a
  // propósito en "Enviar a ...". Con 2 opciones (Aprobado -> Ejecución/Supervisión) no autoselecciona nada.
  useEffect(() => {
    if (equiposFiltrados.length === 1 && idEquipoDestino !== String(equiposFiltrados[0].idEquipo)) {
      setIdEquipoDestino(String(equiposFiltrados[0].idEquipo))
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [equiposFiltrados])

  useEffect(() => {
    if (!idEquipoDestino) {
      setUsuarios([])
      return
    }
    listarUsuariosDestinoVerificacion(Number(idEquipoDestino))
      .then(setUsuarios)
      .catch(() => setUsuarios([]))
  }, [idEquipoDestino])

  // 28/08/2026, pedido explícito del usuario, corrige el fix anterior (mismo día): bloquear por
  // `estadoDocumentoCodigo === 'EMITIDO'` en vivo bloqueaba la fila EN EL MOMENTO de elegir "Emitido"
  // en el combo, antes incluso de completar los datos y sin haber presionado Guardar — dentro de ESTE
  // módulo, mientras el usuario sigue trabajando el documento, nunca debe bloquearse, sin importar el
  // valor que tenga el combo en ese instante. Solo debe bloquearse un documento que YA estaba
  // `EMITIDO` cuando el panel se abrió (ej. el Informe que Análisis, un módulo anterior, ya cerró
  // antes de que el expediente llegara a Verificación) — nunca uno que el propio usuario recién marcó
  // así en la sesión actual. `idsYaEmitidosAlAbrir` es una fotografía que se toma UNA SOLA VEZ por
  // expediente (cacheada en `idsYaEmitidosAlAbrirPorExpediente`, ver Javadoc del módulo arriba — NO
  // en un `useState` local, que no sobrevive el remount al cambiar de subpestaña); los recargos
  // posteriores tras Guardar (`recargarDocumentos()`, sin el flag) refrescan `documentos` pero NUNCA
  // vuelven a calcular esa fotografía, así que un documento recién emitido en esta misma sesión de
  // verificación (ej. una Resolución) sigue editable hasta que se registre la verificación — ver
  // `docEmitido` más abajo, que además bloquea TODO sin excepción una vez que `!puedeRegistrar`
  // (verificación ya registrada), momento en el que la fotografía deja de importar.
  const [idsYaEmitidosAlAbrir, setIdsYaEmitidosAlAbrir] = useState<Set<number>>(new Set())

  function recargarDocumentos(actualizarFotografiaInicial = false) {
    setDocumentosLoading(true)
    listarDocumentosVerificados(expediente.idExpediente)
      .then((data) => {
        setDocumentos(data)
        if (actualizarFotografiaInicial) {
          if (!idsYaEmitidosAlAbrirPorExpediente.has(expediente.idExpediente)) {
            idsYaEmitidosAlAbrirPorExpediente.set(
              expediente.idExpediente,
              new Set(
                data
                  .filter((d) => d.nivel === 0 && d.estadoDocumentoCodigo === 'EMITIDO')
                  .map((d) => d.idDocumentoAnalizado as number),
              ),
            )
          }
          setIdsYaEmitidosAlAbrir(idsYaEmitidosAlAbrirPorExpediente.get(expediente.idExpediente) ?? new Set())
        }
      })
      .catch(() => setDocumentos([]))
      .finally(() => setDocumentosLoading(false))
  }

  const padres = useMemo(() => documentos.filter((d) => d.nivel === 0 && d.activo), [documentos])
  const hijos = useMemo(
    () => documentos.filter((d) => d.nivel === 1 && d.activo && d.idDocumentoPadre === padreFocoId),
    [documentos, padreFocoId],
  )
  const padreFoco = padres.find((p) => p.idDocumentoAnalizado === padreFocoId) ?? null

  function actualizarDocumento(id: number, cambios: Partial<DocumentoAnalizado>) {
    setDocumentos((prev) => prev.map((d) => (d.idDocumentoAnalizado === id ? { ...d, ...cambios } : d)))
  }

  async function handleGuardarFila(doc: DocumentoAnalizado) {
    if ((doc.idDocumentoAnalizado ?? 0) < 0) return
    setGuardandoId(doc.idDocumentoAnalizado)
    setError(null)
    try {
      await guardarDocumentoVerificado({ ...doc, idExpediente: expediente.idExpediente })
      recargarDocumentos()
      onActualizado?.()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo guardar el documento.')
    } finally {
      setGuardandoId(null)
    }
  }

  async function handleRegistrarVerificacion() {
    setErrorResultado(null)
    setMensajeResultado(null)

    if (resultado === 'OBSERVADO' && !descripcionObservacion.trim()) {
      setErrorResultado('Ingrese la descripción de la observación.')
      return
    }

    const confirmado = await confirmDialog(
      'Se registrará el resultado de la verificación. El expediente no se moverá todavía: use "Destino operativo" después para enviarlo.',
      { tone: 'info', confirmLabel: 'Registrar' },
    )
    if (!confirmado) return

    setProcesando(true)
    try {
      const resp =
        resultado === 'OBSERVADO'
          ? await registrarObservado(expediente.idExpediente, comentario, descripcionObservacion)
          : await registrarAprobado(expediente.idExpediente, comentario)
      setMensajeResultado(resp.mensaje)
      // La verificación ya quedó registrada (`puedeRegistrar` pasará a `false`, bloqueando todo el
      // bloque sin excepción vía `docEmitido`) — se descarta la fotografía cacheada para que, si este
      // expediente vuelve a pasar por Verificación más adelante (ej. devuelto de nuevo desde otro
      // módulo), la próxima visita tome una fotografía fresca en vez de reutilizar esta ya vencida.
      idsYaEmitidosAlAbrirPorExpediente.delete(expediente.idExpediente)
      onActualizado?.()
    } catch (e) {
      setErrorResultado(e instanceof ApiError ? e.message : 'No se pudo registrar la verificación.')
    } finally {
      setProcesando(false)
    }
  }

  async function handleEnviarDestino() {
    setErrorDestino(null)
    setMensajeDestino(null)

    if (!equipoDestinoSeleccionado || !idUsuarioDestino) {
      setErrorDestino('Seleccione el equipo y el usuario destino.')
      return
    }

    const confirmado = await confirmDialog(
      `Se ${ETIQUETA_DESTINO[equipoDestinoSeleccionado.codigo].toLowerCase()} el expediente ${expediente.numeroExpediente}.`,
      { tone: 'info', confirmLabel: 'Enviar' },
    )
    if (!confirmado) return

    setProcesandoDestino(true)
    try {
      const resp = await aprobarVerificacionConDestino(expediente.idExpediente, comentarioDestino, Number(idEquipoDestino), Number(idUsuarioDestino))
      setMensajeDestino(resp.mensaje)
      onActualizado?.()
    } catch (e) {
      setErrorDestino(e instanceof ApiError ? e.message : 'No se pudo enviar el expediente al destino seleccionado.')
    } finally {
      setProcesandoDestino(false)
    }
  }

  return (
    <Stack spacing={2}>
      {error && (
        <Alert severity="error" sx={{ whiteSpace: 'pre-line' }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <SectionCard title="Estado de la verificación">
        <Stack spacing={1.5}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Etapa / Estado:
            </Typography>
            <StatusBadge label={`${expediente.etapaCodigo} / ${expediente.estadoCodigo}`} tone="info" />
            {expediente.observado && <StatusBadge label="Observado" tone="warning" />}
            {expediente.inconsistente && <StatusBadge label="Documento inconsistente" tone="error" />}
            {expediente.aprobado && <StatusBadge label="Verificado" tone="success" />}
          </Stack>
          <Typography variant="body2" color="text.secondary">
            Abogado de Análisis: {expediente.responsableAnalisis || '-'} — Resultado de Análisis: {expediente.ultimoResultadoAnalisis || '-'}
          </Typography>
        </Stack>
      </SectionCard>

      {enVerificacion && (
        <SectionCard title="Documentos verificados">
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell />
                  <TableCell>Tipo documento</TableCell>
                  <TableCell>N° Documento</TableCell>
                  <TableCell>Estado documento</TableCell>
                  <TableCell>Fecha Emisión</TableCell>
                  <TableCell>Comentario</TableCell>
                  <TableCell align="center">¿Requiere respuesta?</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {documentosLoading ? (
                  <TableRow>
                    <TableCell colSpan={7}>
                      <Typography variant="body2" color="text.secondary">
                        Cargando...
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : padres.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7}>
                      <Typography variant="body2" color="text.secondary">
                        Sin documentos analizados.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  padres.map((doc) => {
                    // Ver Javadoc de `idsYaEmitidosAlAbrir` arriba: bloqueado si YA estaba `EMITIDO`
                    // al abrir este workspace (documento cerrado por un módulo anterior, nunca por un
                    // cambio hecho en la sesión actual de este mismo panel) — O si la verificación de
                    // este expediente YA fue registrada (`!puedeRegistrar`), caso en el que todo el
                    // bloque queda de solo lectura sin excepción (01/09/2026, pedido explícito del
                    // usuario: "una vez que registre la verificación recién se debería bloquear").
                    const docEmitido = !puedeRegistrar || idsYaEmitidosAlAbrir.has(doc.idDocumentoAnalizado as number)
                    return (
                      <TableRow
                        key={doc.idDocumentoAnalizado}
                        hover
                        selected={padreFocoId === doc.idDocumentoAnalizado}
                        onClick={() => setPadreFocoId(doc.idDocumentoAnalizado)}
                        sx={{ cursor: 'pointer' }}
                      >
                        <TableCell onClick={(e) => e.stopPropagation()}>
                          <Tooltip title={docEmitido ? 'Documento ya emitido: solo lectura' : 'Guardar'}>
                            <span>
                              <IconButton
                                size="small"
                                className={DATA_GRID_ROW_ICON_SAVE_CLASS}
                                disabled={docEmitido || guardandoId === doc.idDocumentoAnalizado}
                                onClick={() => handleGuardarFila(doc)}
                              >
                                <SaveOutlinedIcon fontSize="small" />
                              </IconButton>
                            </span>
                          </Tooltip>
                        </TableCell>
                        <TableCell>{doc.tipoDocumentoNombre || '-'}</TableCell>
                        <TableCell onClick={(e) => e.stopPropagation()}>
                          <TextField
                            size="small"
                            disabled={docEmitido}
                            value={doc.numeroDocumento}
                            onChange={(e) => actualizarDocumento(doc.idDocumentoAnalizado as number, { numeroDocumento: e.target.value })}
                          />
                        </TableCell>
                        <TableCell onClick={(e) => e.stopPropagation()} sx={{ minWidth: 150 }}>
                          <TextField
                            select
                            size="small"
                            fullWidth
                            disabled={docEmitido}
                            value={doc.estadoDocumentoCodigo}
                            onChange={(e) => {
                              const est = estadosDocumento.find((x) => x.codigo === e.target.value)
                              // 01/09/2026, pedido explícito del usuario: mismo criterio ya usado en
                              // Análisis/Ejecución — "Fecha Emisión" se autocompleta con la fecha de
                              // hoy solo al pasar el combo Estado a "Emitido"; con cualquier otro
                              // valor, la fecha se limpia (sigue editable a mano en cualquier momento).
                              const emitido = e.target.value === 'EMITIDO'
                              actualizarDocumento(doc.idDocumentoAnalizado as number, {
                                estadoDocumentoCodigo: e.target.value,
                                estadoDocumentoNombre: est?.nombre || '',
                                fechaDocumento: emitido ? hoyISO() : null,
                              })
                            }}
                          >
                            <MenuItem value="">Seleccione</MenuItem>
                            {estadosDocumento.map((e2) => (
                              <MenuItem key={e2.codigo} value={e2.codigo ?? ''}>
                                {e2.nombre}
                              </MenuItem>
                            ))}
                          </TextField>
                        </TableCell>
                        <TableCell onClick={(e) => e.stopPropagation()}>
                          <TextField
                            size="small"
                            type="date"
                            disabled={docEmitido}
                            value={doc.fechaDocumento ?? ''}
                            onChange={(e) => actualizarDocumento(doc.idDocumentoAnalizado as number, { fechaDocumento: e.target.value || null })}
                          />
                        </TableCell>
                        <TableCell onClick={(e) => e.stopPropagation()}>
                          <TextField
                            size="small"
                            disabled={docEmitido}
                            value={doc.descripcion}
                            onChange={(e) => actualizarDocumento(doc.idDocumentoAnalizado as number, { descripcion: e.target.value })}
                          />
                        </TableCell>
                        <TableCell align="center">{doc.requiereRespuesta ? 'Sí' : 'No'}</TableCell>
                      </TableRow>
                    )
                  })
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </SectionCard>
      )}

      {enVerificacion && (
        <SectionCard title="Documentos de respuesta">
          <Box>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>
              {padreFoco ? `Documento seleccionado: ${padreFoco.tipoDocumentoNombre || '-'}` : 'Ningún documento seleccionado'}
            </Typography>
            {!padreFoco ? (
              <Typography variant="body2" color="text.secondary">
                Seleccione un documento principal en "Documentos verificados" para ver sus respuestas.
              </Typography>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Tipo documento</TableCell>
                      <TableCell>Confirmación de respuesta</TableCell>
                      <TableCell>Fecha Respuesta</TableCell>
                      <TableCell>Fecha Publicación</TableCell>
                      <TableCell>Existe Oposición</TableCell>
                      <TableCell>Hoja de Envío</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {hijos.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={6}>
                          <Typography variant="body2" color="text.secondary">
                            Sin documentos de respuesta.
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ) : (
                      hijos.map((doc) => (
                        <TableRow key={doc.idDocumentoAnalizado}>
                          <TableCell>{doc.tipoDocumentoNombre || '-'}</TableCell>
                          <TableCell>{doc.confirmacionRespuesta || '-'}</TableCell>
                          <TableCell>{doc.fechaRespuesta || '-'}</TableCell>
                          <TableCell>{doc.fechaPublicacion || '-'}</TableCell>
                          <TableCell>{doc.existeOposicion === null ? '-' : doc.existeOposicion ? 'Sí' : 'No'}</TableCell>
                          <TableCell>{doc.numeroHojaEnvioRespuesta || '-'}</TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Box>
        </SectionCard>
      )}

      {enVerificacion && (
        <SectionCard title="Resultado de la verificación">
          <Stack spacing={2}>
            {!puedeRegistrar && (
              // 01/09/2026, pedido explícito del usuario: estandarizar en verde (success) el aviso
              // de "resultado ya registrado" en los 4 paneles (Análisis/Verificación/Ejecución/
              // Notificación) — antes era severity="info" (azul), aunque el dato ya era persistido
              // (deriva de `expediente.aprobado`/`observado`, no de un mensaje efímero).
              <Alert severity="success">
                Ya se registró el resultado de la verificación ({expediente.aprobado ? 'Aprobado' : expediente.observado ? 'Observado' : expediente.estadoCodigo}). Use "Destino operativo" para enviarlo.
              </Alert>
            )}

            <TextField
              select
              label="Resultado"
              size="small"
              fullWidth
              disabled={!puedeRegistrar}
              value={resultado}
              onChange={(e) => setResultado(e.target.value as 'APROBADO' | 'OBSERVADO')}
            >
              <MenuItem value="APROBADO">Aprobado</MenuItem>
              <MenuItem value="OBSERVADO">Observado</MenuItem>
            </TextField>

            <TextField
              label="Comentario (opcional)"
              size="small"
              fullWidth
              multiline
              minRows={2}
              disabled={!puedeRegistrar}
              value={comentario}
              onChange={(e) => setComentario(e.target.value)}
            />

            {resultado === 'OBSERVADO' && (
              <TextField
                label="Descripción de la observación"
                size="small"
                fullWidth
                multiline
                minRows={2}
                required
                disabled={!puedeRegistrar}
                value={descripcionObservacion}
                onChange={(e) => setDescripcionObservacion(e.target.value)}
              />
            )}

            <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
              <Button variant="contained" onClick={handleRegistrarVerificacion} disabled={procesando || !puedeRegistrar} sx={{ alignSelf: 'flex-start' }}>
                Registrar verificación
              </Button>
              {errorResultado && (
                <Alert severity="error" sx={{ py: 0, whiteSpace: 'pre-line' }} onClose={() => setErrorResultado(null)}>
                  {errorResultado}
                </Alert>
              )}
              {mensajeResultado && (
                <Alert severity="success" sx={{ py: 0 }} onClose={() => setMensajeResultado(null)}>
                  {mensajeResultado}
                </Alert>
              )}
            </Stack>
          </Stack>
        </SectionCard>
      )}

      {enVerificacion && puedeEnviar && (
        <SectionCard title="Destino operativo">
          <Stack spacing={2}>
            <TextField label={labelAbogadoDestino} size="small" fullWidth value={valorAbogadoDestino} slotProps={{ input: { readOnly: true } }} />
            <TextField
              select
              label="Equipo destino"
              size="small"
              fullWidth
              helperText={ayudaEquipoDestino}
              value={idEquipoDestino}
              onChange={(e) => {
                setIdEquipoDestino(e.target.value)
                setIdUsuarioDestino('')
              }}
            >
              <MenuItem value="">Seleccione equipo</MenuItem>
              {equiposFiltrados.map((eq) => (
                <MenuItem key={eq.idEquipo} value={String(eq.idEquipo)}>
                  {eq.displayName || eq.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Usuario destino"
              size="small"
              fullWidth
              disabled={!idEquipoDestino}
              value={idUsuarioDestino}
              onChange={(e) => setIdUsuarioDestino(e.target.value)}
            >
              <MenuItem value="">Seleccione usuario</MenuItem>
              {usuarios.map((u) => (
                <MenuItem key={u.idUsuario} value={String(u.idUsuario)}>
                  {u.nombreCompleto}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Comentario (opcional)"
              size="small"
              fullWidth
              multiline
              minRows={2}
              value={comentarioDestino}
              onChange={(e) => setComentarioDestino(e.target.value)}
            />
            <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
              <Button
                variant="contained"
                onClick={handleEnviarDestino}
                disabled={procesandoDestino || !idEquipoDestino || !idUsuarioDestino}
                sx={{ alignSelf: 'flex-start' }}
              >
                {labelBotonDestino || 'Seleccione un equipo destino'}
              </Button>
              {errorDestino && (
                <Alert severity="error" sx={{ py: 0, whiteSpace: 'pre-line' }} onClose={() => setErrorDestino(null)}>
                  {errorDestino}
                </Alert>
              )}
              {mensajeDestino && (
                <Alert severity="success" sx={{ py: 0 }} onClose={() => setMensajeDestino(null)}>
                  {mensajeDestino}
                </Alert>
              )}
            </Stack>
          </Stack>
        </SectionCard>
      )}

      {enVerificacion && !puedeRegistrar && !puedeEnviar && (
        <Alert severity="info">Este expediente no tiene ninguna acción disponible desde Verificación en su estado actual.</Alert>
      )}

      {!enVerificacion && <Alert severity="info">Este expediente no está en un estado operable desde Verificación.</Alert>}
    </Stack>
  )
}
