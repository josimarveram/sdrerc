import { useEffect, useState } from 'react'
import {
  Alert,
  Box,
  Button,
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
  type PlantillaBloqueAdmin,
  type PlantillaDocumentoAdmin,
} from '../api/adminPlantillasApi'
import { ApiError } from '../api/http'
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
  const [bloques, setBloques] = useState<PlantillaBloqueAdmin[]>([])
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
    const confirmado = window.confirm(`¿Eliminar el bloque "${bloque.titulo || bloque.contenido.slice(0, 40)}"?`)
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
                  <IconButton size="small" onClick={() => eliminar(b)}>
                    <DeleteOutlineOutlinedIcon fontSize="small" sx={{ color: sdrercColors.error }} />
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

      {editando && <DlgEditarBloque bloque={editando} onClose={() => setEditando(null)} onGuardar={guardar} />}
    </Stack>
  )
}

function DlgEditarBloque({
  bloque,
  onClose,
  onGuardar,
}: {
  bloque: PlantillaBloqueAdmin
  onClose: () => void
  onGuardar: (bloque: PlantillaBloqueAdmin) => void
}) {
  const [form, setForm] = useState<PlantillaBloqueAdmin>(bloque)
  const [error, setError] = useState<string | null>(null)

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
          <TextField label="Título (opcional)" size="small" value={form.titulo ?? ''} onChange={(e) => setForm({ ...form, titulo: e.target.value })} />
          <TextField
            label="Contenido"
            multiline
            minRows={4}
            value={form.contenido}
            onChange={(e) => setForm({ ...form, contenido: e.target.value })}
          />
          <Typography variant="subtitle2">Condición (opcional)</Typography>
          <Stack direction="row" spacing={2}>
            <TextField
              label="Variable"
              size="small"
              fullWidth
              value={form.variableCondicion ?? ''}
              onChange={(e) => setForm({ ...form, variableCondicion: e.target.value })}
              placeholder="Ej. canalRecepcion"
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
          <TextField
            label="Valores esperados (separados por |)"
            size="small"
            value={form.valoresCondicion ?? ''}
            onChange={(e) => setForm({ ...form, valoresCondicion: e.target.value })}
            placeholder="Ej. PROCEDENTE|PROCEDENTE_EN_PARTE"
            disabled={!form.variableCondicion}
          />
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
