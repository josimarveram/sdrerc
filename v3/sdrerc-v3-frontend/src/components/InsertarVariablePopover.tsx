import { useMemo, useState } from 'react'
import { Box, IconButton, InputAdornment, List, ListItemButton, ListItemText, Popover, TextField, Tooltip, Typography } from '@mui/material'
import SearchIcon from '@mui/icons-material/SearchOutlined'
import type { PlantillaVariable } from '../api/adminPlantillasApi'

/**
 * Botón "lupa" embebido en un campo de texto libre (Título/Contenido de un bloque de contenido)
 * que abre un buscador de variables sin salir del diálogo ni saltar a la subpestaña "Variables" —
 * pedido explícito del usuario ("para no tener que estar saliendo a la pestaña Variables a cada
 * momento"). Inserta `#nombre#` en la posición EXACTA del cursor del campo (no al final), vía
 * `campoRef` (el `inputRef` del `TextField`, que expone el `<input>`/`<textarea>` real del DOM) —
 * mismo patrón ya usado por editores de texto con autocompletado de variables (Slack/Notion).
 */
export function InsertarVariablePopover({
  variables,
  campoRef,
  valorActual,
  onInsertar,
}: {
  variables: PlantillaVariable[]
  campoRef: React.RefObject<HTMLInputElement | HTMLTextAreaElement | null>
  valorActual: string
  onInsertar: (nuevoValor: string) => void
}) {
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null)
  const [texto, setTexto] = useState('')

  const filtradas = useMemo(() => {
    const filtro = texto.trim().toLowerCase()
    return variables.filter(
      (v) => !filtro || v.nombre.toLowerCase().includes(filtro) || v.descripcion.toLowerCase().includes(filtro),
    )
  }, [variables, texto])

  function cerrar() {
    setAnchorEl(null)
    setTexto('')
  }

  function elegir(nombre: string) {
    const marcador = `#${nombre}#`
    const el = campoRef.current
    if (!el) {
      onInsertar(valorActual + marcador)
      cerrar()
      return
    }
    const inicio = el.selectionStart ?? valorActual.length
    const fin = el.selectionEnd ?? valorActual.length
    const nuevoValor = valorActual.slice(0, inicio) + marcador + valorActual.slice(fin)
    onInsertar(nuevoValor)
    cerrar()
    // El valor recién se aplica en el próximo render (estado del padre) — reposicionar el cursor
    // después del marcador insertado recién ahí, o `setSelectionRange` actuaría sobre el valor viejo.
    requestAnimationFrame(() => {
      el.focus()
      const nuevaPos = inicio + marcador.length
      el.setSelectionRange(nuevaPos, nuevaPos)
    })
  }

  return (
    <>
      <Tooltip title="Buscar e insertar una variable aquí">
        <IconButton size="small" onClick={(e) => setAnchorEl(e.currentTarget)}>
          <SearchIcon fontSize="small" />
        </IconButton>
      </Tooltip>
      <Popover
        open={Boolean(anchorEl)}
        anchorEl={anchorEl}
        onClose={cerrar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Box sx={{ width: 320, p: 1.5 }}>
          <TextField
            autoFocus
            size="small"
            fullWidth
            placeholder="Buscar variable..."
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> } }}
          />
          <List dense sx={{ maxHeight: 280, overflowY: 'auto', mt: 1 }}>
            {filtradas.map((v) => (
              <ListItemButton key={v.nombre} onClick={() => elegir(v.nombre)} sx={{ borderRadius: 1 }}>
                <ListItemText
                  primary={`#${v.nombre}#`}
                  secondary={v.descripcion}
                  slotProps={{ primary: { sx: { fontFamily: 'monospace', fontSize: 13 } }, secondary: { sx: { fontSize: 11 } } }}
                />
              </ListItemButton>
            ))}
            {filtradas.length === 0 && (
              <Typography variant="body2" color="text.secondary" sx={{ p: 1 }}>
                Sin resultados para "{texto}".
              </Typography>
            )}
          </List>
        </Box>
      </Popover>
    </>
  )
}
