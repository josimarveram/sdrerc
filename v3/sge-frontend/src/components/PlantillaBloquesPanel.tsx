import { useEffect, useMemo, useRef, useState } from 'react'
import {
  Alert,
  Autocomplete,
  type AutocompleteRenderValueGetItemProps,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  MenuItem,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined'
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward'
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward'
import {
  crearBloque,
  editarBloque,
  eliminarBloque,
  guardarOrdenBloques,
  listarBloques,
  listarVariablesDisponibles,
  type PlantillaBloqueAdmin,
  type PlantillaDocumentoAdmin,
  type PlantillaVariable,
} from '../api/adminPlantillasApi'
import { ApiError } from '../api/http'
import { useAppDialog } from '../dialogs/AppDialogProvider'
import { DATA_GRID_ROW_ICON_DELETE_CLASS } from '../dataGridTheme'
import { InsertarVariablePopover } from './InsertarVariablePopover'
import { SectionCard } from './RegistroExpedienteForm'
import { sdrercColors } from '../theme'

function bloqueVacio(idTipo: number): PlantillaBloqueAdmin {
  return {
    idPlantillaBloque: 0,
    idTipoDocumentoAdjunto: idTipo,
    orden: 0,
    seccion: '',
    titulo: '',
    contenido: '',
    variableCondicion: '',
    operadorCondicion: 'COINCIDE',
    valoresCondicion: '',
    activo: true,
  }
}

/**
 * Subpestaña "Bloques de contenido" (Administración &gt; Plantillas de documento) — equivalente
 * web de `DlgBloquesPlantillaV2` (V2), pero embebida en la misma página en vez de un diálogo
 * aparte (menos saltos de contexto). Reordenar con flechas arriba/abajo (no arrastrar-soltar, sin
 * agregar una librería de drag&amp;drop nueva solo para esto — la lista rara vez supera unos pocos
 * bloques por tipo de documento).
 */
export function PlantillaBloquesPanel({ tipo }: { tipo: PlantillaDocumentoAdmin }) {
  const { confirm: confirmDialog } = useAppDialog()
  const [bloques, setBloques] = useState<PlantillaBloqueAdmin[]>([])
  const [variables, setVariables] = useState<PlantillaVariable[]>([])
  const [editando, setEditando] = useState<PlantillaBloqueAdmin | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function cargar() {
    try {
      setBloques(await listarBloques(tipo.idTipoDocumentoAdjunto))
    } catch {
      setBloques([])
    }
  }

  useEffect(() => {
    cargar()
    setError(null)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tipo.idTipoDocumentoAdjunto])

  useEffect(() => {
    listarVariablesDisponibles().then(setVariables).catch(() => setVariables([]))
  }, [])

  async function guardar(bloque: PlantillaBloqueAdmin) {
    setError(null)
    try {
      const request = {
        idPlantillaBloque: bloque.idPlantillaBloque || null,
        idTipoDocumentoAdjunto: tipo.idTipoDocumentoAdjunto,
        seccion: bloque.seccion || null,
        titulo: bloque.titulo || null,
        contenido: bloque.contenido,
        variableCondicion: bloque.variableCondicion || null,
        operadorCondicion: bloque.variableCondicion ? bloque.operadorCondicion : null,
        valoresCondicion: bloque.variableCondicion ? bloque.valoresCondicion || null : null,
      }
      if (bloque.idPlantillaBloque) {
        await editarBloque(bloque.idPlantillaBloque, request)
      } else {
        await crearBloque(request)
      }
      setEditando(null)
      await cargar()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo guardar el bloque.')
    }
  }

  async function eliminar(bloque: PlantillaBloqueAdmin) {
    const confirmado = await confirmDialog(`¿Eliminar el bloque "${bloque.titulo || bloque.contenido.slice(0, 40)}"?`, {
      tone: 'danger',
      confirmLabel: 'Eliminar',
    })
    if (!confirmado) return
    try {
      await eliminarBloque(bloque.idPlantillaBloque)
      await cargar()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo eliminar el bloque.')
    }
  }

  async function mover(index: number, direccion: -1 | 1) {
    const destino = index + direccion
    if (destino < 0 || destino >= bloques.length) return
    const copia = [...bloques]
    const tmp = copia[index];
    copia[index] = copia[destino]
    copia[destino] = tmp
    setBloques(copia)
    try {
      await guardarOrdenBloques(tipo.idTipoDocumentoAdjunto, copia.map((b) => b.idPlantillaBloque))
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo guardar el nuevo orden.')
      await cargar()
    }
  }

  return (
    <Stack spacing={2}>
      <SectionCard title="Bloques de contenido">
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Texto dinámico insertado en el marcador <code>[[CONTENIDO]]</code> (o <code>[[CONTENIDO:sección]]</code> si
          tiene sección asignada) de la plantilla base. Opcionalmente condicionado a que una variable coincida con
          un valor esperado.
        </Typography>
        <Stack spacing={1}>
          {bloques.map((b, index) => (
            <Stack
              key={b.idPlantillaBloque}
              direction="row"
              spacing={2}
              alignItems="flex-start"
              sx={{ p: 1.5, borderRadius: 1.5, border: `1px solid ${sdrercColors.divider}` }}
            >
              <Stack>
                <IconButton size="small" disabled={index === 0} onClick={() => mover(index, -1)}>
                  <ArrowUpwardIcon fontSize="small" />
                </IconButton>
                <IconButton size="small" disabled={index === bloques.length - 1} onClick={() => mover(index, 1)}>
                  <ArrowDownwardIcon fontSize="small" />
                </IconButton>
              </Stack>
              <Box sx={{ flex: 1 }}>
                {b.seccion && (
                  <Typography variant="caption" sx={{ color: sdrercColors.info, fontWeight: 600 }}>
                    Sección: {b.seccion}
                  </Typography>
                )}
                {b.titulo && <Typography sx={{ fontWeight: 600 }}>{b.titulo}</Typography>}
                <Typography variant="body2" color="text.secondary" sx={{ whiteSpace: 'pre-wrap' }}>
                  {b.contenido}
                </Typography>
                {b.variableCondicion && (
                  <Typography variant="caption" sx={{ display: 'block', mt: 0.5, fontFamily: 'monospace' }}>
                    Se muestra si <strong>{b.variableCondicion}</strong> {b.operadorCondicion === 'NO_COINCIDE' ? 'no coincide con' : 'coincide con'} "{b.valoresCondicion}"
                  </Typography>
                )}
              </Box>
              <Stack direction="row" spacing={0.5}>
                <Tooltip title="Editar">
                  <IconButton size="small" onClick={() => setEditando(b)}>
                    <EditOutlinedIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Eliminar">
                  <IconButton size="small" className={DATA_GRID_ROW_ICON_DELETE_CLASS} onClick={() => eliminar(b)}>
                    <DeleteOutlineOutlinedIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </Stack>
            </Stack>
          ))}
          {bloques.length === 0 && <Typography color="text.secondary">Sin bloques configurados.</Typography>}
        </Stack>
        <Button
          variant="outlined"
          startIcon={<AddCircleOutlineIcon />}
          sx={{ mt: 2 }}
          onClick={() => setEditando(bloqueVacio(tipo.idTipoDocumentoAdjunto))}
        >
          Nuevo bloque
        </Button>
        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}
      </SectionCard>

      {editando && (
        <DlgEditarBloque bloque={editando} variables={variables} onClose={() => setEditando(null)} onGuardar={guardar} />
      )}
    </Stack>
  )
}

function coincideCondicion(valorPrueba: string, operador: 'COINCIDE' | 'NO_COINCIDE', valores: string[]): boolean {
  const normalizado = valorPrueba.trim().toLowerCase()
  const coincide = valores.some((v) => v.trim().toLowerCase() === normalizado)
  return operador === 'NO_COINCIDE' ? !coincide : coincide
}

/**
 * Réplica exacta de `AnalisisPlantillaDocumentoService#normalizarNombreVariable` (backend): minúsculas
 * y se descarta todo lo que no sea letra/dígito ASCII (espacios, puntos, guiones, tildes —esta
 * función NO hace NFD, una tilde simplemente se elimina, no se convierte a su letra base—, etc.) —
 * para que el "Sección" del bloque solo empareje con el marcador `[[CONTENIDO:seccion]]` de la
 * plantilla si ambos normalizan al MISMO texto, sin importar mayúsculas.
 * Bug real diagnosticado el 27/08/2026 (SDRERC-EXP-2026-000011, Informe Rectificación): un bloque con
 * Sección "I. ANTECEDENTES" normaliza a "iantecedentes" — NO coincide con el marcador real de la
 * plantilla `[[CONTENIDO:antecedentes]]` (normaliza a "antecedentes"), así que el bloque nunca se
 * insertaba. Esta vista previa expone el valor normalizado para que el usuario pueda compararlo
 * visualmente contra el marcador real del Word antes de guardar.
 */
function normalizarSeccionPreview(valor: string): string {
  return valor.toLowerCase().replace(/[^a-z0-9]/g, '')
}

function DlgEditarBloque({
  bloque,
  variables,
  onClose,
  onGuardar,
}: {
  bloque: PlantillaBloqueAdmin
  variables: PlantillaVariable[]
  onClose: () => void
  onGuardar: (bloque: PlantillaBloqueAdmin) => void
}) {
  const [form, setForm] = useState<PlantillaBloqueAdmin>(bloque)
  const [valorPrueba, setValorPrueba] = useState('')
  const [error, setError] = useState<string | null>(null)
  const tituloRef = useRef<HTMLInputElement>(null)
  const contenidoRef = useRef<HTMLTextAreaElement>(null)

  const valoresCondicion = useMemo(
    () => (form.valoresCondicion ?? '').split('|').map((v) => v.trim()).filter(Boolean),
    [form.valoresCondicion],
  )

  function confirmar() {
    if (!form.contenido.trim()) {
      setError('El contenido del bloque no puede estar vacío.')
      return;
    }
    onGuardar(form)
  }

  return (
    <Dialog open onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{bloque.idPlantillaBloque ? 'Editar bloque' : 'Nuevo bloque'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            label="Sección (opcional)"
            size="small"
            value={form.seccion ?? ''}
            onChange={(e) => setForm({ ...form, seccion: e.target.value })}
            helperText='Deje vacío para el marcador [[CONTENIDO]] sin nombre, o indique un nombre para usar [[CONTENIDO:nombre]].'
          />
          <Typography variant="caption" color="text.secondary" sx={{ mt: -1.5, fontFamily: 'monospace' }}>
            Este bloque busca exactamente{' '}
            <strong>
              {form.seccion?.trim() ? `[[CONTENIDO:${normalizarSeccionPreview(form.seccion)}]]` : '[[CONTENIDO]]'}
            </strong>{' '}
            en el Word (sin distinguir mayúsculas, espacios ni puntuación) — copie el texto del marcador tal cual
            aparece en la plantilla si no coincide.
          </Typography>
          <TextField
            label="Título (opcional)"
            size="small"
            value={form.titulo ?? ''}
            onChange={(e) => setForm({ ...form, titulo: e.target.value })}
            slotProps={{
              input: {
                inputRef: tituloRef,
                endAdornment: (
                  <InsertarVariablePopover
                    variables={variables}
                    campoRef={tituloRef}
                    valorActual={form.titulo ?? ''}
                    onInsertar={(nuevoValor) => setForm({ ...form, titulo: nuevoValor })}
                  />
                ),
              },
            }}
          />
          <TextField
            label="Contenido"
            multiline
            minRows={4}
            value={form.contenido}
            onChange={(e) => setForm({ ...form, contenido: e.target.value })}
            helperText="Use el ícono de lupa para buscar e insertar una variable en la posición del cursor."
            slotProps={{
              input: {
                inputRef: contenidoRef,
                endAdornment: (
                  <InsertarVariablePopover
                    variables={variables}
                    campoRef={contenidoRef}
                    valorActual={form.contenido}
                    onInsertar={(nuevoValor) => setForm({ ...form, contenido: nuevoValor })}
                  />
                ),
                sx: { alignItems: 'flex-start' },
              },
            }}
          />
          <Typography variant="subtitle2">Condición (opcional)</Typography>
          <Stack direction="row" spacing={2}>
            <Autocomplete
              freeSolo
              fullWidth
              options={variables.map((v) => v.nombre)}
              value={form.variableCondicion ?? ''}
              onInputChange={(_e, valor) => setForm({ ...form, variableCondicion: valor })}
              renderInput={(params) => (
                <TextField {...params} label="Variable" size="small" placeholder="Ej. canalRecepcion" />
              )}
            />
            <TextField
              select
              label="Operador"
              size="small"
              sx={{ minWidth: 160 }}
              value={form.operadorCondicion ?? 'COINCIDE'}
              onChange={(e) => setForm({ ...form, operadorCondicion: e.target.value as 'COINCIDE' | 'NO_COINCIDE' })}
              disabled={!form.variableCondicion}
            >
              <MenuItem value="COINCIDE">Coincide con</MenuItem>
              <MenuItem value="NO_COINCIDE">No coincide con</MenuItem>
            </TextField>
          </Stack>
          <Autocomplete<string, true, false, true>
            multiple
            freeSolo
            size="small"
            options={[] as string[]}
            value={valoresCondicion}
            onChange={(_e, valores) => setForm({ ...form, valoresCondicion: valores.join('|') })}
            disabled={!form.variableCondicion}
            renderValue={(value: string[], getItemProps: AutocompleteRenderValueGetItemProps<true>) =>
              value.map((valor, index) => {
                const { key, ...rest } = getItemProps({ index })
                return <Chip key={key} label={valor} size="small" {...rest} />
              })
            }
            renderInput={(params) => (
              <TextField
                {...params}
                label="Valores esperados"
                placeholder="Escriba un valor y presione Enter"
                helperText="Uno o más valores que activan la condición — cada Enter agrega un valor nuevo."
              />
            )}
          />
          {form.variableCondicion && valoresCondicion.length > 0 && (
            <Stack direction="row" spacing={1.5} alignItems="center" sx={{ p: 1.5, borderRadius: 1.5, bgcolor: sdrercColors.backgroundPaperAlt }}>
              <TextField
                label="Probar con este valor"
                size="small"
                value={valorPrueba}
                onChange={(e) => setValorPrueba(e.target.value)}
                sx={{ minWidth: 220 }}
              />
              {valorPrueba && (
                <Chip
                  label={
                    coincideCondicion(valorPrueba, form.operadorCondicion ?? 'COINCIDE', valoresCondicion)
                      ? 'Se activaría'
                      : 'No se activaría'
                  }
                  color={coincideCondicion(valorPrueba, form.operadorCondicion ?? 'COINCIDE', valoresCondicion) ? 'success' : 'default'}
                  size="small"
                />
              )}
            </Stack>
          )}
          {error && <Alert severity="error">{error}</Alert>}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancelar</Button>
        <Button variant="contained" onClick={confirmar}>
          Guardar
        </Button>
      </DialogActions>
    </Dialog>
  )
}
