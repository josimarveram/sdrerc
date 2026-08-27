import { useState } from 'react'
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField, Typography } from '@mui/material'
import { importarFeriadosXml } from '../api/adminFeriadosApi'
import { ApiError } from '../api/http'

interface DlgImportarFeriadosXmlProps {
  onClose: () => void
  onImportado: (importados: number) => void
}

/** "Importar XML" de Administración &gt; Feriados (V2: `FeriadoNacionalService.importarFeriadosDesdeXml`). */
export function DlgImportarFeriadosXml({ onClose, onImportado }: DlgImportarFeriadosXmlProps) {
  const [anio, setAnio] = useState(new Date().getFullYear());
  const [archivo, setArchivo] = useState<File | null>(null)
  const [importando, setImportando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function importar() {
    setError(null)
    if (!archivo) {
      setError('Seleccione un archivo .xml para importar.')
      return
    }
    setImportando(true)
    try {
      const resp = await importarFeriadosXml(archivo, anio)
      onImportado(resp.importados)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo importar el archivo.')
    } finally {
      setImportando(false)
    }
  }

  return (
    <Dialog open onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>Importar feriados desde XML</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Typography variant="body2" color="text.secondary">
            El archivo debe declarar feriados con elementos <code>&lt;feriado&gt;</code> u <code>&lt;holiday&gt;</code>, con atributos o
            subelementos <code>fecha</code>/<code>nombre</code>/<code>tipo</code>/<code>activo</code>/<code>observacion</code>.
          </Typography>
          <TextField
            label="Año a importar"
            type="number"
            fullWidth
            value={anio}
            onChange={(e) => setAnio(Number(e.target.value))}
          />
          <Button variant="outlined" component="label">
            {archivo ? archivo.name : 'Seleccionar archivo .xml'}
            <input type="file" accept=".xml" hidden onChange={(e) => setArchivo(e.target.files?.[0] ?? null)} />
          </Button>
          {error && <Alert severity="error">{error}</Alert>}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={importando}>
          Cancelar
        </Button>
        <Button onClick={importar} variant="contained" disabled={importando}>
          Importar
        </Button>
      </DialogActions>
    </Dialog>
  )
}
