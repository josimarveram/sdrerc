import { alpha, Box, Paper, Typography } from '@mui/material'
import { sdrercColors } from '../theme'

interface MetricCardProps {
  label: string
  value: number
  caption: string
  color: string
  /**
   * Verdadero cuando esta tarjeta representa el filtro de KPI actualmente activo en la bandeja
   * (`filtroKpi === 'X'`, ya calculado por cada página — este componente no sabe nada de esa
   * lógica, solo la refleja visualmente). `undefined`/`false` = tarjeta no clicable o no
   * seleccionada, mismo aspecto que antes de este incremento.
   */
  active?: boolean
}

/**
 * Stat tile reutilizable (Anexo A/B del plan de migracion): equivalente web de MetricCardV2 (V2).
 * Sentence case sin dos puntos finales, valor en cifras proporcionales (nunca tabular-nums en un
 * numero grande y aislado, ver skill dataviz), color solo en el valor — la etiqueta y el
 * subtitulo siempre en tinta de texto, nunca en el color de la serie.
 *
 * Padding vertical reducido (13/08/2026, pedido explícito del usuario: "achica el padding vertical
 * interno... para que ocupen menos espacio vertical, manteniendo la legibilidad") — `py` bajó de
 * 2.5 a 1.5, `px` sin cambios. Cambio en el componente compartido: se refleja automáticamente en
 * TODAS las bandejas que ya usan `MetricCard` (Registro, Asignación, Carga Abogados, Cartas de
 * Respuesta, Dashboard), no solo en la que lo pidió.
 *
 * Barra de acento superior (28/08/2026, sistema de diseño premium — Incremento 1, ver plan
 * "necesito-un-plan-de-cuddly-wombat.md"): reutiliza el mismo `color` que ya tenía el valor
 * numérico, ahora también como franja de 3px arriba de la tarjeta — refuerza visualmente el
 * significado del KPI (verde/ámbar/rojo/azul) sin agregar una prop nueva. `overflow:'hidden'` +
 * `position:'relative'` en el `Paper` para que la franja respete el `borderRadius` existente en
 * vez de sobresalir en las esquinas.
 *
 * Borde completo + estados hover/activo (28/08/2026, mismo día, pedido explícito del usuario tras
 * ver la barra superior en producción: "es posible mejorar que se pinte todo el borde... y que el
 * background sea más oscuro cuando se presiona, que se note un brillo interesante"). Verificado
 * antes de implementar que el sistema real solo usa 5 colores semánticos para KPIs (`primary`/
 * `info`/`warning`/`error`/`success`, nunca colores arbitrarios) — la escala de `alpha(color, N)`
 * funciona igual de bien para los 5 sin necesitar tokens nuevos:
 * - Reposo: borde completo `alpha(color, 0.25)` (antes solo la barra superior, sin borde).
 * - Hover (solo si `active` no está definido, ver `onClick` del padre — este componente no
 *   escucha su propio click, la tarjeta clicable sigue siendo el `Box onClick=...` que la envuelve
 *   en cada página): borde a `alpha(color, 0.5)` + `elevation` 1→2 + `translateY(-1px)`.
 * - Activa (`active=true`): fondo pasa de blanco a `alpha(color, 0.10)`, borde sólido al color
 *   completo, más un brillo diagonal superpuesto (`::after`, gradiente blanco→transparente) y un
 *   anillo exterior suave (`box-shadow` extra) — mismo lenguaje que un chip de filtro activo en
 *   dashboards tipo Stripe/Linear, para que sea evidente que el KPI está filtrando sin depender
 *   solo del color de fondo.
 *
 * Más compacta + responsive real (28/08/2026, mismo día, pedido explícito del usuario tras ver que
 * al angostar la ventana las filas de KPI se desbordaban horizontalmente — "se ve fatal" — en vez
 * de envolver a una 2da fila; y pidiendo además reducir la altura sin que el texto se superponga):
 * - `minWidth` bajó de 160 a 120px — con `minWidth` más chico, el `repeat(auto-fit, minmax(...))`
 *   de cada página (ver `dataGridTheme.ts`/páginas de bandeja) puede empaquetar más columnas por
 *   fila en pantallas normales Y wrappear de verdad (no desbordar) en pantallas angostas, porque
 *   ya no compite con un piso de 160px más grande que el `minmax` de la grilla.
 * - `py` bajó de 1.5 a 1, tipografía del valor de `h4` a `h5` (más corta verticalmente, sigue
 *   siendo claramente el elemento dominante de la tarjeta) y label/leyenda pasan a `variant="caption"`
 *   (antes `body2`) — mismo criterio ya aplicado en el ajuste de padding del 13/08/2026, esta vez
 *   más agresivo porque el usuario pidió explícitamente "disminuir la altura".
 * - `display:flex; flexDirection:column; justifyContent:center` en el `Paper`: si una leyenda larga
 *   ("0 a 5 días hábiles", "Requieren corrección") envuelve a 2 líneas en una columna angosta, esa
 *   tarjeta queda más alta que sus vecinas en la misma fila del grid — como el grid ya estira por
 *   defecto (`align-items` implícito `stretch`) todas las tarjetas de una fila a la altura de la más
 *   alta, centrar el contenido verticalmente evita que las tarjetas más cortas se vean con texto
 *   pegado arriba y un hueco vacío abajo (la distorsión que pidió evitar explícitamente el usuario).
 * - `whiteSpace:'nowrap'` en label/valor (nunca deberían envolver, son cortos por diseño) — solo la
 *   leyenda puede envolver a 2 líneas como máximo, nunca más, vía `WebkitLineClamp`.
 */
export function MetricCard({ label, value, caption, color, active }: MetricCardProps) {
  return (
    <Paper
      elevation={active ? 2 : 1}
      sx={{
        position: 'relative',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        px: 2,
        py: 1,
        minHeight: 76,
        borderRadius: 2,
        flex: 1,
        minWidth: 120,
        border: `1px solid ${alpha(color, active ? 1 : 0.25)}`,
        bgcolor: active ? alpha(color, 0.1) : sdrercColors.backgroundPaper,
        transition: 'border-color 150ms ease, background-color 150ms ease, box-shadow 150ms ease, transform 150ms ease',
        boxShadow: active ? `0 0 0 3px ${alpha(color, 0.15)}` : undefined,
        '&:hover': {
          borderColor: alpha(color, active ? 1 : 0.5),
          transform: 'translateY(-1px)',
        },
        '&::after': active
          ? {
              content: '""',
              position: 'absolute',
              inset: 0,
              background: 'linear-gradient(135deg, rgba(255,255,255,0.35), rgba(255,255,255,0) 45%)',
              pointerEvents: 'none',
            }
          : undefined,
      }}
    >
      <Box sx={{ position: 'absolute', top: 0, left: 0, right: 0, height: 3, bgcolor: color }} />
      <Typography variant="caption" color="text.secondary" sx={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
        {label}
      </Typography>
      <Typography variant="h5" sx={{ color, my: 0.25, lineHeight: 1.15, fontVariantNumeric: 'proportional-nums' }}>
        {value.toLocaleString('es-PE')}
      </Typography>
      <Typography
        variant="caption"
        sx={{
          color: sdrercColors.textSecondary,
          display: '-webkit-box',
          WebkitLineClamp: 2,
          WebkitBoxOrient: 'vertical',
          overflow: 'hidden',
          lineHeight: 1.25,
        }}
      >
        {caption}
      </Typography>
    </Paper>
  )
}
