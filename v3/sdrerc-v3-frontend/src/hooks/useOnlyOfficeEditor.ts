import { useEffect, useRef, useState } from 'react'
import { obtenerConfigOnlyOffice } from '../api/adminPlantillasApi'
import { ApiError } from '../api/http'

const SCRIPT_ID = 'sdrerc-onlyoffice-api-script'
const ONLYOFFICE_PORT = 8082

/**
 * Bug real corregido (27/08/2026): con el proxy de Vite (`/onlyoffice` → reescribiendo la ruta),
 * el editor cargaba `api.js` bien pero fallaba al abrir el documento con "Load timeout for
 * modules: documenteditor/main/...". Causa: una vez dentro del iframe del editor, OnlyOffice pide
 * sus propios archivos internos con rutas ABSOLUTAS ancladas a la raíz de su propio servidor
 * (`/web-apps/...`), no relativas al prefijo `/onlyoffice/` — la reescritura del proxy las rompía.
 * Fix: el navegador habla DIRECTO con el contenedor (puerto 8082, sin proxy ni reescritura),
 * calculando el host en tiempo real desde `window.location.hostname` — funciona igual si se
 * accedió a la app por `localhost` o por la IP de Tailscale de esta máquina, sin hardcodear nada.
 * Requiere el puerto 8082 abierto en el firewall (mismo criterio que el 5173 de Vite).
 */
function scriptSrcOnlyOffice(): string {
  return `${window.location.protocol}//${window.location.hostname}:${ONLYOFFICE_PORT}/web-apps/apps/api/documents/api.js`
}

interface DocEditorInstance {
  destroyEditor: () => void
}

declare global {
  interface Window {
    DocsAPI?: {
      DocEditor: new (containerId: string, config: Record<string, unknown>) => DocEditorInstance
    }
  }
}

function cargarScriptOnlyOffice(): Promise<void> {
  if (window.DocsAPI) return Promise.resolve()
  const existente = document.getElementById(SCRIPT_ID) as HTMLScriptElement | null
  if (existente) {
    return new Promise((resolve, reject) => {
      existente.addEventListener('load', () => resolve())
      existente.addEventListener('error', () => reject(new Error('No se pudo cargar el editor de OnlyOffice.')))
    })
  }
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.id = SCRIPT_ID
    script.src = scriptSrcOnlyOffice()
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('No se pudo cargar el editor de OnlyOffice. Verifique que el contenedor esté corriendo.'))
    document.body.appendChild(script)
  })
}

/**
 * Monta/desmonta un editor real de OnlyOffice Document Server dentro de un contenedor DOM propio
 * (`containerId`). Extraído como hook compartido para que el mismo mecanismo sirva tanto al
 * diálogo de pantalla completa como al panel embebido dentro de una subpestaña — sin duplicar la
 * carga de `api.js`/el manejo de config firmada/el ciclo de vida del editor en 2 lugares.
 */
export function useOnlyOfficeEditor(containerId: string, idPlantillaDocumento: number | null, activo: boolean) {
  const editorRef = useRef<DocEditorInstance | null>(null)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!activo || idPlantillaDocumento == null) return
    let cancelado = false

    async function iniciar() {
      setCargando(true)
      setError(null)
      try {
        await cargarScriptOnlyOffice()
        const config = await obtenerConfigOnlyOffice(idPlantillaDocumento as number)
        if (cancelado) return
        if (!window.DocsAPI) {
          throw new Error('El editor de OnlyOffice no se pudo inicializar.')
        }
        editorRef.current = new window.DocsAPI.DocEditor(containerId, config)
      } catch (err) {
        if (!cancelado) {
          setError(err instanceof ApiError ? err.message : err instanceof Error ? err.message : 'No se pudo abrir el editor.')
        }
      } finally {
        if (!cancelado) setCargando(false)
      }
    }

    iniciar()
    return () => {
      cancelado = true
      editorRef.current?.destroyEditor()
      editorRef.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activo, idPlantillaDocumento, containerId])

  return { cargando, error }
}
