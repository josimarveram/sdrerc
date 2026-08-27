import { useEffect, useMemo, useRef, useState } from 'react'
import { Alert, Box, Button, Stack, Tooltip, Typography } from '@mui/material'
import { DataGrid, useGridApiContext, type GridColDef, type GridRenderEditCellParams } from '@mui/x-data-grid'
import {
  confirmarCargaDiaria,
  descargarPlantillaCargaDiaria,
  exportarCargaDiaria,
  previsualizarCargaDiaria,
  validarCargaDiaria,
  type CargaDiariaPreview,
  type CargaDiariaResultado,
} from '../api/cargaDiariaApi'
import {
  listarCanalesRecepcion,
  listarProcedimientosRegistrales,
  listarTiposActa,
  listarTiposDocumento,
  type CatalogoItem,
} from '../api/registroApi'
import { ApiError, descargarBlob } from '../api/http'
import { sdrercColors } from '../theme'
import { MetricCard } from '../components/MetricCard'
import { StatusBadge } from '../components/StatusBadge'
import { CargaDiariaConfirmarModal } from '../components/CargaDiariaConfirmarModal'
import { PageHeaderSticky } from '../layout/PageHeaderSticky'
import { REGISTRO_TABS } from '../layout/moduleTabs'

// Etapas del modal de progreso de "Confirmar carga" (18/08/2026, pedido explícito del usuario) —
// nombradas según el trabajo REAL que hace `ExpedienteRegistroDAO.registrarCarga` por fila
// candidata dentro de su única transacción JDBC (persona titular/solicitante -> expediente+número
// -> solicitud/acta/documento/historial -> alertas de duplicado/grupo familiar), no una narrativa
// inventada. Ver Javadoc de `CargaDiariaConfirmarModal.tsx` para el detalle de por qué se simula el
// avance en el cliente en vez de reportarlo desde el backend.
const PASOS_CONFIRMACION_CARGA_DIARIA = [
  'Preparando registros para confirmar...',
  'Registrando titulares y solicitantes...',
  'Creando expedientes y asignando números...',
  'Guardando actas, documentos e historial...',
  'Actualizando alertas y grupos familiares...',
]

// Duración estimada (ms) de las primeras 4 etapas antes de avanzar a la siguiente — el backend no
// reporta avance incremental real (POST /confirmar es una única llamada síncrona), así que se
// simula un avance progresivo mientras se espera la respuesta real, escalado levemente según la
// cantidad de filas a confirmar (un archivo grande "se siente" más lento que uno de 5 filas, sin
// llegar a ser exageradamente largo). La 5ta etapa (última) NUNCA se completa por temporizador: se
// queda "en curso" indefinidamente hasta que la respuesta real llega — evita mostrar 100% antes de
// que el registro haya terminado de verdad.
function duracionesSimuladasConfirmacion(filas: number): number[] {
  const escala = Math.min(Math.max(filas, 1), 150)
  return [350, 500 + escala * 6, 500 + escala * 6, 400 + escala * 4]
}

function formatearFecha(iso: string | null): string {
  if (!iso) return ''
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

// Conversión ISO (yyyy-MM-dd, formato del DTO) <-> Date local (formato que exige la columna nativa
// `type: 'date'` de MUI DataGrid para habilitar el editor con calendario). Construida con
// componentes locales (no `new Date(iso)`) para evitar el mismo desfase de un día por UTC que ya
// se documentó en `hoyISO()` (src/utils/fechas.ts).
function isoADate(iso: string | null): Date | null {
  if (!iso) return null
  const [anio, mes, dia] = iso.slice(0, 10).split('-').map(Number)
  if (!anio || !mes || !dia) return null
  return new Date(anio, mes - 1, dia)
}

function dateAIso(fecha: Date): string {
  const anio = fecha.getFullYear()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${anio}-${mes}-${dia}`
}

// Catálogo de Tipo doc. TITULAR: mismos 5 valores de siempre (CargaDiariaPlantillaService.
// CATALOGO_IDENTIDAD_TITULAR, backend) — incluye "SIN DNI" a diferencia de TIPOS_DOC_TITULAR/
// TIPOS_DOC_REMITENTE de Registro manual (`RegistroExpedienteForm.tsx`), que no lo tienen porque
// ese formulario no soporta el atajo "SIN DNI" en el campo de número. "No Definido" (18/08/2026,
// pedido explícito del usuario): valor con el que el backend autocompleta tipo/número de identidad
// cuando faltan en el Excel (dejaron de ser campos obligatorios de Carga diaria, ver
// CargaDiariaReglasService) — se agrega a la lista de opciones del combo para que el valor que ya
// trae la fila se pueda mostrar y, si se desea, reelegir manualmente.
const TIPOS_DOC_IDENTIDAD_TITULAR = ['No Definido', 'SIN DNI', 'DNI', 'CE', 'PASAPORTE']
// Catálogo de Tipo doc. SOLICITANTE: SIN "SIN DNI" (18/08/2026, pedido explícito del usuario) — a
// diferencia del titular, "SIN DNI" nunca debe quedar como valor final del solicitante; el backend
// (`CargaDiariaReglasService.validarIdentidad`) ya normaliza cualquier "SIN DNI" del solicitante a
// "No Definido" en el siguiente "Validar", así que se quita del combo para no ofrecer una opción
// que se revertiría sola. "NO APLICA" (exclusivo del solicitante): valor fijo que el backend asigna
// cuando Tipo Solicitante es Centro de Atención/Unidad Orgánica/Municipalidad — una entidad no
// tiene documento de identidad, a diferencia de un Ciudadano.
const TIPOS_DOC_IDENTIDAD_SOLICITANTE = ['No Definido', 'NO APLICA', 'DNI', 'RUC', 'CE', 'PASAPORTE']
// Mismos 2 valores que CargaDiariaPreviewDTO.normalizarTipoSolicitud (backend): "Parte"/"Oficio"
// con esa capitalización exacta es lo que queda persistido en el campo.
const TIPOS_SOLICITUD_OPCIONES = ['Parte', 'Oficio']
// Mismos 4 códigos que CargaDiariaReglasService.clasificarTipoSolicitante persiste en
// `tipoSolicitante` (backend) — `value` es el código real que se guarda, `label` reutiliza
// `nombreVisualTipoSolicitante` (misma traducción ya usada para mostrar la columna en modo lectura)
// para que la etiqueta del combo coincida exactamente con lo que el usuario ya venía viendo.
const TIPOS_SOLICITANTE_OPCIONES = ['CIUDADANO', 'CENTRO_ATENCION', 'UNIDAD_ORGANICA', 'MUNICIPALIDAD'].map(
  (codigo) => ({ value: codigo, label: nombreVisualTipoSolicitante(codigo) }),
)

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
  // N° expediente SGD duplicado (13/08/2026, pedido explícito del usuario): mismo fix que el
  // backend (`CargaDiariaExportService.convertirADatoIncompleto`) — ni "ya está registrado en..."
  // ni "repetido en el archivo..." contienen ninguna de las palabras clave del guard de abajo, así
  // que la fila mostraba "Sin observación" pese a bloquear el registro por SGD duplicado.
  if (lower.includes('expediente sgd') && (lower.includes('ya está registrado') || lower.includes('repetido en el archivo'))) {
    return 'N° expediente SGD ya registrado'
  }
  if (!lower.includes('obligatorio') && !lower.includes('inválida') && !lower.includes('invalida') && !lower.includes('determinar')) {
    return null
  }
  if (lower.includes('expediente sgd')) return 'Dato incompleto: N° expediente SGD'
  // "Solicitado por" (18/08/2026, pedido explícito del usuario, nuevo campo obligatorio de Carga
  // diaria) — antes de "número de trámite" (ya no es obligatorio, se deja el mapeo existente).
  if (lower.includes('solicitado por')) return 'Dato incompleto: Solicitado por'
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

// Límites de formato del N° de documento de identidad por tipo (18/08/2026, pedido explícito del
// usuario): DNI exactamente 8 dígitos numéricos (con relleno de ceros a la izquierda si el usuario
// escribe menos, ver backend `CargaDiariaReglasService.normalizarNumeroDni`), RUC 11 dígitos
// numéricos, CE 15 caracteres alfanuméricos, Pasaporte 12 caracteres alfanuméricos. "SIN DNI"/"No
// Definido"/vacío no restringen el input — son valores especiales, no números reales.
function limitesNumeroDocumento(tipo: string | null): { maxLength: number; soloDigitos: boolean } | null {
  const t = (tipo ?? '').trim().toUpperCase()
  if (t === 'DNI') return { maxLength: 8, soloDigitos: true }
  if (t === 'RUC') return { maxLength: 11, soloDigitos: true }
  if (t === 'CE') return { maxLength: 15, soloDigitos: false }
  if (t === 'PASAPORTE') return { maxLength: 12, soloDigitos: false }
  return null
}

// Editor de celda con restricción de caracteres en vivo (18/08/2026, pedido explícito del usuario:
// "el input... solo debe permitir N caracteres/dígitos") — el límite se calcula a partir del valor
// actual de la columna "Tipo doc." de la MISMA fila (`campoTipo`), así que cambia dinámicamente si
// el usuario edita primero el tipo y luego el número.
function NumeroDocumentoEditCell(
  props: GridRenderEditCellParams<CargaDiariaPreview, string | null> & {
    campoTipo: 'tipoDocumentoIdentidadSolicitante' | 'tipoDocumentoIdentidadTitular'
  },
) {
  const apiRef = useGridApiContext()
  const { id, field, value, row, campoTipo } = props
  const limites = limitesNumeroDocumento(row[campoTipo])
  return (
    <input
      autoFocus
      value={value ?? ''}
      maxLength={limites?.maxLength}
      style={{ width: '100%', height: '100%', border: 'none', outline: 'none', padding: '0 10px', font: 'inherit', boxSizing: 'border-box' }}
      onChange={(e) => {
        let nuevo = e.target.value
        if (limites?.soloDigitos) {
          nuevo = nuevo.replace(/\D/g, '')
        } else if (limites) {
          nuevo = nuevo.toUpperCase().replace(/[^A-Z0-9]/g, '')
        }
        if (limites) nuevo = nuevo.slice(0, limites.maxLength)
        apiRef.current.setEditCellValue({ id, field, value: nuevo })
      }}
    />
  )
}

/** CIUDADANO/CENTRO_ATENCION/UNIDAD_ORGANICA/MUNICIPALIDAD -> texto legible (Solicitante con 4 tipos, 14/08/2026, ver v3/CLAUDE.md). */
function nombreVisualTipoSolicitante(codigo: string | null): string {
  if (codigo === 'CENTRO_ATENCION') return 'Centro de Atención'
  if (codigo === 'UNIDAD_ORGANICA') return 'Unidad Orgánica'
  if (codigo === 'MUNICIPALIDAD') return 'Municipalidad'
  return 'Ciudadano'
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
 *
 * **2 bugs corregidos el 13/08/2026, pedido explícito del usuario** ("no carga bien los kpis tal
 * como estaba en la carga diaria de la v2... no muestra los mensajes correcto y cuando sale error
 * a veces no especifica el tipo de error"; diseño/colores de `MetricCard`/`StatusBadge`/`Alert`
 * explícitamente NO tocados, solo datos/lógica):
 * 1. **KPIs incompletos**: el `resumen` de esta página solo calculaba `validos`/`conAlertas`, sin
 *    el desglose que sí muestra V2 (`JPanelCargaDiariaRecepcionV2.actualizarResumen`, caption
 *    dinámico de la tarjeta "Con alertas": "Duplicados: X | Grupo familiar: Y | Pendientes de
 *    número: Z", con el mismo cálculo exacto de `CargaDiariaResumenDTO.desde` del backend). Ahora
 *    `resumen` replica esa función Java línea por línea (mismo conteo, mismos campos) y el caption
 *    de "Con alertas" muestra el desglose real en vez del texto fijo "No bloqueantes" anterior. Las
 *    otras 4 tarjetas de V2 (`cardDuplicados`/`cardGrupoFamiliar`/`cardPendientesNumero`/
 *    `cardRegistrados`) se descartaron a propósito: existen en el código de V2 pero NUNCA se
 *    agregan al layout visible (`metricas.add(...)` solo incluye `cardValidos`/`cardErrores`) — son
 *    código muerto en la propia V2, así que no hay nada real que replicar ahí.
 * 2. **Mensajes de error reales no visibles**: la columna "Observación" (`observacionValidacionTabla`,
 *    réplica exacta del método homónimo de V2) solo traduce mensajes que contienen las palabras
 *    "obligatorio"/"inválida"/"determinar" a frases cortas "Dato incompleto: X" — cualquier otro
 *    mensaje real (ej. "N° expediente SGD ya está registrado en...", reglas de formato de DNI/RUC/CE,
 *    "Acta y titular repetidos en el archivo...") se descarta silenciosamente de esa columna, TANTO
 *    en V2 como en la V3 anterior a este fix — no es una regresión de V3, es una limitación real de
 *    ese método que también tiene V2. La diferencia es que V2 SÍ compensa esto con un tooltip en la
 *    columna "Resultado del sistema" (`tooltipPrevisualizacion`, `COL_ESTADO_VALIDACION`) que
 *    muestra `item.getMensajeValidacion()` completo, SIN filtrar — ese tooltip faltaba en V3. Se
 *    agregó (`renderCell` con `Tooltip` sobre el `StatusBadge`), replicando exactamente ese
 *    comportamiento; también se agregaron los 2 tooltips menores que tenía V2 en `numeroExpedienteGenerado`
 *    ("Expediente registrado correctamente.", solo si `registrado`) y en la propia columna
 *    "Observación" (mismo texto ya visible, solo evita que se corte si la celda es angosta).
 *
 * **3 correcciones más, mismo día (13/08/2026), pedido explícito del usuario, priorizando la
 * validación de integridad de N° expediente SGD**:
 * 1. **N° expediente SGD único, server-side, en los 3 puntos de entrada**: ya estaba implementado
 *    en el backend para los 3 flujos (Carga diaria: `CargaDiariaReglasService`, bloquea con estado
 *    "Error"/`listoParaRegistrar=false`, excluido de `registrarCarga`; Registro manual:
 *    `RegistroManualExpedienteService.validarConDuplicados`, bloquea `registrar()`; Edición
 *    manual/panel de datos: `ExpedienteEdicionManualService.validar`, bloquea `guardar()`) — se
 *    verificó código por código antes de tocar nada. El bug real de Edición manual: `validar()`
 *    detectaba el SGD duplicado y lo agregaba a la lista de errores (bloqueaba igual el guardado),
 *    pero NUNCA marcaba `dto.setNumeroExpedienteSgdDuplicado(true)`/`setMotivoNumeroExpedienteSgdDuplicado(...)`
 *    en el DTO — a diferencia de `RegistroManualExpedienteService`, que sí marca esos 2 campos. Sin
 *    eso, `RegistroExpedienteForm` (compartido con Registro manual) nunca resaltaba el campo "N°
 *    expediente SGD" en rojo con su `helperText`, ni en la página de edición dedicada ni en el panel
 *    de datos inline. Corregido con el mismo patrón de reset+set que ya usa
 *    `RegistroManualExpedienteService`. Además, `PanelDatosExpediente.tsx` (edición inline, sin
 *    botón "Validar" separado a propósito) llamaba `guardarEdicionManual` directo — ante un rechazo
 *    400 solo mostraba el mensaje genérico en el `Alert` superior, sin resaltar el campo (el estado
 *    local nunca se actualizaba con el DTO). Ahora `guardarEdicion()` llama primero a
 *    `validarEdicionManual`, y si hay mensajes, actualiza el formulario con el DTO devuelto (con
 *    los flags ya marcados) y corta antes de guardar — mismo resultado final, sin agregar un botón
 *    nuevo (sigue siendo solo Guardar/Cancelar).
 * 2. **Observación "Sin observación" en SGD duplicado**: ver bug #2 documentado arriba
 *    (`convertirADatoIncompleto`) — se agregó el caso específico ("N° expediente SGD ya registrado")
 *    tanto en el frontend (esta página) como en el backend (`CargaDiariaExportService`, para que la
 *    grilla en vivo y el Excel exportado coincidan), antes de la validación genérica
 *    "obligatorio"/"inválida"/"determinar" (los mensajes de SGD duplicado no contienen ninguna de
 *    esas palabras). De paso se corrigió el mismo vacío para "N° expediente SGD obligatorio."
 *    (campo vacío), que tampoco tenía ninguna rama que lo capturara pese a contener "obligatorio".
 * 3. **Altura fija + "Rows per page" sin 10**: el `Box` que envolvía el `DataGrid` tenía
 *    `height: 560` fijo (dejaba un hueco vacío con pocas filas) y no declaraba `pageSizeOptions`
 *    (MUI X usa `[25, 50, 100]` por defecto, sin "10"). El resto de grillas del sistema
 *    (`RegistroBandejaPage.tsx`/`AsignacionBandejaPage.tsx`/`AsignacionCargaAbogadosPage.tsx`/
 *    `AsignacionCartasRespuestaPage.tsx`, verificadas una por una) ya usan `autoHeight` +
 *    `pageSizeOptions={[10, 25, 50, 100]}` + `initialState` con `pageSize: 10` por defecto — Carga
 *    diaria era la única grilla del sistema que no seguía ese patrón compartido. Se igualó
 *    exactamente, sin inventar un mecanismo nuevo de alto máximo/scroll interno propio: la altura
 *    queda acotada por la paginación (máximo 100 filas visibles a la vez, igual que en el resto de
 *    bandejas), consistente con "el resto de tablas del sistema que compartan el mismo componente
 *    base".
 *
 * Verificado con `npx tsc --noEmit` (limpio). **Pendiente**: sin verificar en navegador — falta
 * confirmar visualmente que el campo SGD se resalta en rojo en los 3 flujos con un valor
 * duplicado real, que la columna "Observación" ya no muestra "Sin observación" en ese caso, y que
 * la grilla ya no deja hueco vacío con pocas filas.
 */
export function RegistroCargaDiariaPage() {
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
  // Estado del modal de progreso de "Confirmar carga" (18/08/2026, pedido explícito del usuario) —
  // ver `handleConfirmar` para el detalle del temporizado simulado y `CargaDiariaConfirmarModal.tsx`
  // para el porqué de no usar WebSocket/SSE.
  const [modalConfirmarAbierto, setModalConfirmarAbierto] = useState(false)
  const [pasoActual, setPasoActual] = useState(0)
  const [pasoConError, setPasoConError] = useState<number | null>(null)
  const [resultadoConfirmacion, setResultadoConfirmacion] = useState<CargaDiariaResultado | null>(null)
  const [errorConfirmacion, setErrorConfirmacion] = useState<string | null>(null)
  const pasoActualRef = useRef(0)
  const temporizadoresConfirmarRef = useRef<number[]>([])
  // Catálogos para los combos editables de la grilla (18/08/2026, pedido explícito del usuario:
  // "guiate con la informacion que esta en registro manual tal cual como se carga") — mismo patrón
  // de carga (Promise.all al montar) que `RegistroManualPage.tsx`, mismos 4 endpoints. Los otros 3
  // catálogos que sí carga Registro manual (Centro de Atención/Unidad Orgánica/Municipalidad) no
  // aplican aquí: en Carga diaria son de solo lectura, calculados automáticamente por el backend.
  const [canales, setCanales] = useState<CatalogoItem[]>([])
  const [procedimientos, setProcedimientos] = useState<CatalogoItem[]>([])
  const [tiposDocumento, setTiposDocumento] = useState<CatalogoItem[]>([])
  const [tiposActa, setTiposActa] = useState<CatalogoItem[]>([])

  useEffect(() => {
    Promise.all([
      listarCanalesRecepcion(),
      listarProcedimientosRegistrales(),
      listarTiposDocumento(),
      listarTiposActa(),
    ])
      .then(([c, p, d, a]) => {
        setCanales(c)
        setProcedimientos(p)
        setTiposDocumento(d)
        setTiposActa(a)
      })
      .catch((e) => setError(e instanceof ApiError ? e.message : 'No se pudieron cargar los catálogos.'))
  }, [])

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

  function limpiarTemporizadoresConfirmar() {
    temporizadoresConfirmarRef.current.forEach((id) => window.clearTimeout(id))
    temporizadoresConfirmarRef.current = []
  }

  function avanzarPasoConfirmar(indice: number) {
    pasoActualRef.current = indice
    setPasoActual(indice)
  }

  /** Completa las etapas restantes de un tirón (180ms entre cada una) tras recibir la respuesta real. */
  function completarPasosRestantes(onListo: () => void) {
    if (pasoActualRef.current >= PASOS_CONFIRMACION_CARGA_DIARIA.length) {
      onListo()
      return
    }
    window.setTimeout(() => {
      avanzarPasoConfirmar(pasoActualRef.current + 1)
      completarPasosRestantes(onListo)
    }, 180)
  }

  function cerrarModalConfirmar() {
    setModalConfirmarAbierto(false)
  }

  /**
   * "Confirmar carga" simula el avance del stepper mientras espera la única respuesta HTTP real
   * (18/08/2026, pedido explícito del usuario — ver Javadoc de `CargaDiariaConfirmarModal.tsx` para
   * el porqué de no implementar progreso incremental real desde el backend). Se lanzan 4
   * temporizadores (uno por transición entre las 5 etapas) con duración estimada según la cantidad
   * de filas a confirmar; si la respuesta real llega ANTES de que la simulación termine, se cancelan
   * los temporizadores pendientes y se completan las etapas restantes de un tirón
   * (`completarPasosRestantes`); si la respuesta tarda MÁS que la simulación, la 5ta etapa
   * simplemente quesa "en curso" (spinner) hasta que la respuesta llegue — nunca se muestra el 100%
   * antes de que el registro haya terminado de verdad en la base de datos.
   */
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
    setResultadoConfirmacion(null)
    setErrorConfirmacion(null)
    setPasoConError(null)
    avanzarPasoConfirmar(0)
    setModalConfirmarAbierto(true)

    limpiarTemporizadoresConfirmar()
    let respuestaLlegada = false
    let acumulado = 0
    duracionesSimuladasConfirmacion(listos).forEach((ms, i) => {
      acumulado += ms
      const id = window.setTimeout(() => {
        if (!respuestaLlegada) avanzarPasoConfirmar(i + 1)
      }, acumulado)
      temporizadoresConfirmarRef.current.push(id)
    })

    confirmarCargaDiaria(registros)
      .then((resultado) => {
        respuestaLlegada = true
        limpiarTemporizadoresConfirmar()
        setRegistros(resultado.registros)
        setMensaje(resultado.mensaje)
        completarPasosRestantes(() => {
          setResultadoConfirmacion(resultado)
          window.setTimeout(() => setModalConfirmarAbierto(false), 1800)
        })
      })
      .catch((e) => {
        respuestaLlegada = true
        limpiarTemporizadoresConfirmar()
        setPasoConError(pasoActualRef.current)
        setErrorConfirmacion(e instanceof ApiError ? e.message : 'No se pudo confirmar la carga diaria.')
      })
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

  // Réplica literal de CargaDiariaResumenDTO.desde (V2/V3 backend) — antes esta bandeja solo
  // contaba "válidos"/"con alertas" sin la desagregación real que sí calcula el backend
  // (13/08/2026, pedido explícito del usuario: "no carga bien los kpis tal como estaba en la carga
  // diaria de la v2"). El desglose (duplicados/grupo familiar/pendientes de número) se muestra en
  // el caption de la tarjeta "Con alertas", igual texto que V2 (`actualizarResumen`).
  const resumen = useMemo(() => {
    let validos = 0
    let conAlertas = 0
    let duplicados = 0
    let grupoFamiliar = 0
    let pendientesNumero = 0
    for (const r of registros) {
      if (r.posibleDuplicado) duplicados++
      if (r.grupoFamiliar || r.posibleGrupoFamiliar) grupoFamiliar++
      if (!r.registrado && r.listoParaRegistrar && !r.numeroExpedienteGenerado) pendientesNumero++
      const estado = r.estadoValidacion
      if (estado === 'Error' || estado === 'Duplicado' || estado === 'Con observaciones') {
        conAlertas++
      } else if (estado === 'Válido' || estado === 'Advertencia' || r.listoParaRegistrar) {
        validos++
      }
    }
    return { validos, conAlertas, duplicados, grupoFamiliar, pendientesNumero }
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

  // Combo editable nativo del DataGrid (`type: 'singleSelect'`, mismo mecanismo que ya usa MUI X
  // para renderizar un `<select>` en modo edición sin necesitar un `renderEditCell` propio) — la
  // contraparte de `CatalogoSelect`/`ValidatedTextField select` que usa Registro manual
  // (`RegistroExpedienteForm.tsx`). Las opciones son siempre `nombre` (nunca `codigo`): estas
  // columnas del preview guardan el nombre canónico, no el código de catálogo (ver
  // `CargaDiariaPreviewDTO.setTipoProcedimiento`/`normalizarTipoSolicitud`, backend).
  function columnaCombo(
    field: keyof CargaDiariaPreview,
    headerName: string,
    width: number,
    opciones: string[],
  ): GridColDef<CargaDiariaPreview> {
    return {
      field,
      headerName,
      width,
      type: 'singleSelect',
      valueOptions: opciones,
      editable: CAMPOS_EDITABLES.has(field),
      isCellEditable: (params) => !params.row.registrado,
    }
  }

  const nombresCatalogo = (items: CatalogoItem[]) =>
    items.map((item) => item.nombre).filter((nombre): nombre is string => !!nombre)

  const columnas: GridColDef<CargaDiariaPreview>[] = [
    columnaCombo('tipoSolicitud', 'Tipo de solicitud', 130, TIPOS_SOLICITUD_OPCIONES),
    {
      field: 'fechaRecepcion',
      headerName: 'Fecha de solicitud',
      width: 160,
      type: 'date',
      editable: true,
      isCellEditable: (params) => !params.row.registrado,
      // `type: 'date'` exige que el valor durante la edición sea un `Date` real (habilita el editor
      // nativo con ícono de calendario del propio DataGrid, sin depender de `@mui/x-date-pickers`,
      // que no está instalado en el proyecto) — de ahí la conversión ISO<->Date en getter/setter.
      valueGetter: (_value, row) => isoADate(row.fechaRecepcion),
      valueFormatter: (value) => (value instanceof Date ? formatearFecha(dateAIso(value)) : ''),
      valueSetter: (value, row) => {
        const iso = value instanceof Date && !isNaN(value.getTime()) ? dateAIso(value) : null
        return { ...row, fechaRecepcion: iso, fechaRecepcionTexto: iso ? formatearFecha(iso) : '' }
      },
    },
    columnaTexto('remitente', 'Solicitado por', 220),
    columnaCombo('tipoDocumentoIdentidadSolicitante', 'Tipo doc. solicitante', 150, TIPOS_DOC_IDENTIDAD_SOLICITANTE),
    {
      field: 'numeroDocumentoIdentidadSolicitante',
      headerName: 'N° doc. solicitante',
      width: 140,
      editable: CAMPOS_EDITABLES.has('numeroDocumentoIdentidadSolicitante'),
      isCellEditable: (params) => !params.row.registrado,
      renderEditCell: (params) => <NumeroDocumentoEditCell {...params} campoTipo="tipoDocumentoIdentidadSolicitante" />,
    },
    // Solicitante con 4 tipos (14/08/2026, ver v3/CLAUDE.md): columna calculada por el sistema al
    // validar (identificación automática desde "Solicitado por" contra los catálogos). Editable como
    // combo (18/08/2026, pedido explícito del usuario: "en caso uno quiera modificar manualmente esa
    // informacion luego de seleccionado el excel de la carga") — al editarla, `valueSetter` marca
    // `tipoSolicitanteManual=true` (ver CargaDiariaPreview) para que el próximo "Validar" respete la
    // elección manual en vez de recalcularla, y limpia idCentroAtencion/idUnidadOrganica/
    // idMunicipalidad (y sus denominaciones): la entidad específica que había resuelto la
    // clasificación automática ya no es necesariamente coherente con la categoría elegida a mano, y
    // esta grilla no ofrece un selector para volver a elegir la entidad exacta, solo la categoría.
    {
      field: 'tipoSolicitante',
      headerName: 'Tipo solicitante',
      width: 170,
      type: 'singleSelect',
      valueOptions: TIPOS_SOLICITANTE_OPCIONES,
      editable: true,
      isCellEditable: (params) => !params.row.registrado,
      valueGetter: (_value, row) => row.tipoSolicitante ?? 'CIUDADANO',
      valueSetter: (value, row) => ({
        ...row,
        tipoSolicitante: String(value),
        tipoSolicitanteManual: true,
        idCentroAtencion: null,
        centroAtencionDenominacion: null,
        idUnidadOrganica: null,
        unidadOrganicaNombre: null,
        idMunicipalidad: null,
        municipalidadDenominacion: null,
      }),
    },
    columnaTexto('numeroTramite', 'N° trámite web', 140),
    columnaCombo('canalRecepcion', 'Canal recepción', 160, nombresCatalogo(canales)),
    columnaTexto('numeroExpedienteSgd', 'N° expediente SGD', 160),
    columnaCombo('tipoDocumento', 'Tipo documento', 140, nombresCatalogo(tiposDocumento)),
    columnaTexto('numeroDocumento', 'N° documento', 130),
    columnaCombo('tipoProcedimiento', 'Procedimiento registral', 190, nombresCatalogo(procedimientos)),
    columnaCombo('tipoActa', 'Tipo de acta', 130, nombresCatalogo(tiposActa)),
    columnaTexto('numeroActa', 'N° acta', 110),
    columnaTexto('titular', 'Titular', 220),
    columnaCombo('tipoDocumentoIdentidadTitular', 'Tipo doc. titular', 130, TIPOS_DOC_IDENTIDAD_TITULAR),
    {
      field: 'numeroDocumentoIdentidadTitular',
      headerName: 'N° doc. titular',
      width: 130,
      editable: CAMPOS_EDITABLES.has('numeroDocumentoIdentidadTitular'),
      isCellEditable: (params) => !params.row.registrado,
      renderEditCell: (params) => <NumeroDocumentoEditCell {...params} campoTipo="tipoDocumentoIdentidadTitular" />,
    },
    {
      field: 'estadoValidacion',
      headerName: 'Resultado del sistema',
      width: 170,
      renderCell: (params) => {
        const item = params.row as CargaDiariaPreview
        const badge = <StatusBadge label={(params.value as string) ?? '-'} tone={toneEstadoValidacion(params.value as string | null)} />
        // Tooltip con el mensaje de validación COMPLETO sin filtrar (mismo comportamiento que
        // `tooltipPrevisualizacion`/COL_ESTADO_VALIDACION en V2) — la columna "Observación" de al
        // lado solo muestra un subconjunto curado ("Dato incompleto: X"), así que motivos reales
        // como "N° expediente SGD ya está registrado en..." o reglas de formato de documento de
        // identidad quedaban sin mostrarse en ningún lado (13/08/2026, pedido explícito del
        // usuario: "cuando sale error a veces no especifica el tipo de error").
        return item.mensajeValidacion ? (
          <Tooltip title={item.mensajeValidacion}>
            <span>{badge}</span>
          </Tooltip>
        ) : (
          badge
        )
      },
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
      renderCell: (params) => {
        const item = params.row as CargaDiariaPreview
        const texto = params.value as string
        return item.registrado ? (
          <Tooltip title="Expediente registrado correctamente.">
            <span>{texto}</span>
          </Tooltip>
        ) : (
          <span>{texto}</span>
        )
      },
    },
    {
      field: 'mensajeValidacion',
      headerName: 'Observación',
      width: 320,
      valueGetter: (_value, row) => observacionValidacionTabla(row),
      renderCell: (params) => (
        <Tooltip title={params.value as string}>
          <span>{params.value as string}</span>
        </Tooltip>
      ),
    },
  ]

  return (
    <PageHeaderSticky
      titulo="Carga diaria"
      subtitulo="Suba la plantilla oficial de carga diaria (.xlsx o .csv) para registrar expedientes en lote."
      tabs={REGISTRO_TABS}
    >
      {/* Sin `filtros` (Fila 3): esta página es un flujo de carga de archivo, no una bandeja de
          búsqueda — su propia barra de acciones (Descargar plantilla/Seleccionar archivo/Validar/
          Confirmar carga/Exportar/Limpiar) es demasiado extensa para la Fila 1 de acciones
          (pensada para 1-2 botones tipo CTA) y queda en el cuerpo, debajo del header sticky, igual
          que antes (13/08/2026, pedido explícito del usuario, ver Javadoc de `PageHeaderSticky`). */}
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
        <MetricCard
          label="Con alertas"
          value={resumen.conAlertas}
          caption={`Duplicados: ${resumen.duplicados} | Grupo familiar: ${resumen.grupoFamiliar} | Pendientes de número: ${resumen.pendientesNumero}`}
          color={sdrercColors.warning}
        />
      </Stack>

      {/* Alto dinámico (13/08/2026, pedido explícito del usuario: "elimina el espacio vacío cuando
          hay pocas filas") — antes tenía `height: 560` fijo, dejando un hueco vacío con pocas
          filas cargadas. `autoHeight` + `pageSizeOptions`/`initialState.pagination` replican
          exactamente el mismo patrón que ya usan el resto de grillas del sistema que comparten
          este componente base (`RegistroBandejaPage.tsx`/`AsignacionBandejaPage.tsx`), así que la
          altura queda acotada por la paginación (máximo 100 filas por página) igual que en esas
          bandejas — no una tabla nueva/distinta, la misma consistente con "el resto de tablas del
          sistema que compartan el mismo componente base". */}
      <Box sx={{ bgcolor: sdrercColors.backgroundPaper, borderRadius: 2 }}>
        <DataGrid<CargaDiariaPreview>
          rows={registros}
          columns={columnas}
          getRowId={(row) => row.fila}
          loading={loading || confirmando}
          density="compact"
          autoHeight
          disableRowSelectionOnClick
          processRowUpdate={processRowUpdate}
          onProcessRowUpdateError={(e) => setError(e instanceof ApiError ? e.message : 'No se pudo editar la celda.')}
          pageSizeOptions={[10, 25, 50, 100]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
        />
      </Box>

      <CargaDiariaConfirmarModal
        open={modalConfirmarAbierto}
        pasos={PASOS_CONFIRMACION_CARGA_DIARIA}
        pasoActual={pasoActual}
        pasoConError={pasoConError}
        errorMensaje={errorConfirmacion}
        resultado={resultadoConfirmacion}
        onCerrar={cerrarModalConfirmar}
      />
    </PageHeaderSticky>
  )
}
