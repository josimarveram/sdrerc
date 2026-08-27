import { useEffect, useMemo, useState } from 'react'
import { Alert, Chip, Dialog, DialogContent, DialogTitle, IconButton, InputAdornment, Stack, TextField, Typography } from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import SearchIcon from '@mui/icons-material/SearchOutlined'
import ContentCopyOutlinedIcon from '@mui/icons-material/ContentCopyOutlined'
import { listarVariablesDisponibles, type PlantillaVariable } from '../api/adminPlantillasApi'
import { sdrercColors } from '../theme'

/**
 * Catálogo de variables `#variable#` disponibles al redactar una plantilla Word — mejora
 * explícita sobre V2 (`JPanelPlantillasDocumentoV2`, botón "Ver variables disponibles" que abría
 * un `JOptionPane` con un bloque de texto fijo): acá es una lista filtrable agrupada por categoría,
 * con clic para copiar el nombre `#variable#` directo al portapapeles.
 */
export function DlgVariablesDisponibles({ onClose }: { onClose: () => void }) {
  const [variables, setVariables] = useState<PlantillaVariable[]>([])
  const [texto, setTexto] = useState('')
  const [copiado, setCopiado] = useState<string | null>(null)

  useEffect(() => {
    listarVariablesDisponibles().then(setVariables).catch(() => setVariables([]))
  }, [])

  const porCategoria = useMemo(() => {
    const filtro = texto.trim().toLowerCase()
    const filtradas = variables.filter(
      (v) => !filtro || v.nombre.toLowerCase().includes(filtro) || v.descripcion.toLowerCase().includes(filtro),
    )
    const mapa = new Map<string, PlantillaVariable[]>()
    for (const v of filtradas) {
      const lista = mapa.get(v.categoria) ?? []
      lista.push(v)
      mapa.set(v.categoria, lista)
    }
    return mapa
  }, [variables, texto])

  function copiar(nombre: string) {
    navigator.clipboard?.writeText(`#${nombre}#`)
    setCopiado(nombre)
    setTimeout(() => setCopiado(null), 1500)
  }

  return (
    <Dialog open onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        Variables disponibles
        <IconButton size="small" onClick={onClose}>
          <CloseIcon fontSize="small" />
        </IconButton>
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2}>
          <Typography variant="body2" color="text.secondary">
            Use estas variables con el formato <code>#variable#</code> dentro del Word. Clic en una para copiarla.
          </Typography>
          <TextField
            size="small"
            placeholder="Buscar variable..."
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> } }}
          />
          {[...porCategoria.entries()].map(([categoria, items]) => (
            <Stack key={categoria} spacing={1}>
              <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                {categoria}
              </Typography>
              <Stack direction="row" flexWrap="wrap" gap={1}>
                {items.map((v) => (
                  <Chip
                    key={v.nombre}
                    label={`#${v.nombre}#`}
                    title={v.descripcion}
                    onClick={() => copiar(v.nombre)}
                    icon={<ContentCopyOutlinedIcon fontSize="small" />}
                    sx={{ bgcolor: sdrercColors.softBlue, fontFamily: 'monospace' }}
                  />
                ))}
              </Stack>
            </Stack>
          ))}
          {porCategoria.size === 0 && (
            <Typography color="text.secondary" sx={{ py: 2 }}>
              Sin resultados para "{texto}".
            </Typography>
          )}
          {copiado && <Alert severity="success">Copiado: #{copiado}#</Alert>}
          <Typography variant="caption" color="text.secondary">
            Condicionales dentro del Word: <code>[[SI_variable:valor1|valor2]] ... [[FIN_SI]]</code> — el texto entre
            marcadores solo aparece si la variable coincide con alguno de los valores. Bloques de contenido: use la
            pestaña "Bloques de contenido" del tipo de documento en vez de escribir marcadores a mano.
          </Typography>
        </Stack>
      </DialogContent>
    </Dialog>
  )
}
