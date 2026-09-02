import { SvgIcon, type SvgIconProps } from '@mui/material'

/**
 * Íconos propios de "documento de Word/Excel" (28/08/2026, sistema de diseño premium, pasadas 4/5,
 * pedido explícito del usuario: primero para Excel ("se ve tipo rejilla, no crees que sea mejor tipo
 * esos iconos premium de excel"), después replicado para Word ("puedes mejorar el icono word... que
 * se vea tipo icono svg propio como hiciste con el excel"). Antes vivían como Material Icons
 * genéricos (`GridOnOutlinedIcon`/`DescriptionOutlinedIcon`) recoloreados por CSS — reconocibles por
 * color pero sin ninguna forma que sugiriera "documento de Word/Excel" real.
 *
 * **Sin librería nueva** (mismo criterio para ambos): un SVG propio evita sumar una dependencia
 * entera (`react-icons`/Simple Icons) solo para 2 íconos, y evita reproducir literalmente los
 * logotipos registrados de Microsoft dentro de una app institucional. Ambos comparten el mismo
 * lenguaje visual — documento con la esquina superior derecha doblada (silueta clásica de "archivo"),
 * `fill="currentColor"` (hereda el color/hover/disabled ya definidos en `index.css` vía
 * `.sdrerc-icon-word`/`.sdrerc-icon-export`, sin tocar ese archivo) + un glifo blanco distintivo
 * adentro: rejilla de hoja de cálculo para Excel, una "W" en trazo grueso para Word — mismo patrón
 * que usan íconos de terceros para "tipo de archivo" (documento + glifo + color de marca).
 *
 * Extraídos a este archivo compartido (antes `ExcelFileIcon` vivía inline en
 * `DataGridStandardToolbar.tsx`, su único consumidor en ese momento) porque `WordFileIcon` lo
 * necesitan 3 paneles distintos (`AnalisisContent.tsx`/`EjecucionContent.tsx`/
 * `DocumentosExpedienteContent.tsx`) — con 2+ consumidores reales, vale la pena el archivo propio en
 * vez de duplicar el SVG.
 */

const DOCUMENTO_ESQUINA_DOBLADA = 'M6 2h8l4 4v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z'
const PLIEGUE_ESQUINA = 'M14 2v3a1 1 0 0 0 1 1h3'

export function ExcelFileIcon(props: SvgIconProps) {
  return (
    <SvgIcon {...props} viewBox="0 0 24 24">
      <path d={DOCUMENTO_ESQUINA_DOBLADA} fill="currentColor" />
      <path
        d={PLIEGUE_ESQUINA}
        fill="none"
        stroke="#fff"
        strokeWidth="1"
        strokeOpacity="0.6"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <g stroke="#fff" strokeWidth="1" strokeOpacity="0.9" strokeLinecap="round">
        <line x1="7" y1="10" x2="15" y2="10" />
        <line x1="7" y1="13.5" x2="15" y2="13.5" />
        <line x1="7" y1="17" x2="15" y2="17" />
        <line x1="9.5" y1="8.5" x2="9.5" y2="19" />
        <line x1="12.5" y1="8.5" x2="12.5" y2="19" />
      </g>
    </SvgIcon>
  )
}

export function WordFileIcon(props: SvgIconProps) {
  return (
    <SvgIcon {...props} viewBox="0 0 24 24">
      <path d={DOCUMENTO_ESQUINA_DOBLADA} fill="currentColor" />
      <path
        d={PLIEGUE_ESQUINA}
        fill="none"
        stroke="#fff"
        strokeWidth="1"
        strokeOpacity="0.6"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <polyline
        points="7.3,9.5 9,17 11,11.5 13,17 14.7,9.5"
        fill="none"
        stroke="#fff"
        strokeWidth="1.5"
        strokeOpacity="0.95"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </SvgIcon>
  )
}
