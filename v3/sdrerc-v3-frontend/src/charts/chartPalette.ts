import { sdrercColors } from '../theme'

/**
 * Colores de gráficos, validados con la skill dataviz (node scripts/validate_palette.js),
 * nunca a ojo. Cada valor tiene un motivo documentado — no cambiar sin re-validar.
 */

/**
 * Un solo tono para gráficos de barras de una sola serie (Expedientes por etapa, Carga por
 * abogado): "un color por barra" burlaría el único canal libre sin aportar información — la
 * guía es un color uniforme, la longitud de la barra ya es el dato. `PRIMARY` (#154775) falló
 * el checo de luminosidad/chroma del validador (se lee casi negro/gris para relleno de gráfico,
 * aunque funciona bien como color de botón); `PRIMARY_HOVER` (#1F5B91) pasó todos los checks.
 */
export const singleSeriesBarColor = sdrercColors.primaryHover

/**
 * Línea de 2 series (Ingresados vs. cerrados por mes). Validado: ΔE CVD 55-67 (muy por encima
 * del objetivo ≥12), contraste ≥3:1 sobre blanco. `PRIMARY` tampoco sirve aquí por el mismo
 * motivo que arriba.
 */
export const trendSeriesColors = {
  ingresados: sdrercColors.primaryHover,
  cerrados: sdrercColors.success,
}

/**
 * Estado final de notificación: 4 categorías con significado real de bueno/malo (Atendido =
 * resultado positivo, Por publicar = requiere escalar, Pendiente = alerta, Por notificar =
 * neutral/inicial) — por eso usa la paleta de estado ya existente en la app (misma que
 * StatusBadgeV2 en V2), no colores categóricos genéricos. Validado: las 4 pasan banda de
 * luminosidad, piso de chroma, separación CVD (ΔE≥24) y contraste ≥3:1 sobre blanco.
 */
export const estadoNotificacionColors: Record<string, string> = {
  'Por notificar': sdrercColors.info,
  Pendiente: sdrercColors.warning,
  Atendido: sdrercColors.success,
  'Por publicar': sdrercColors.error,
}

/**
 * Paleta categórica para gráficos de identidad abierta/dinámica (Resultado de análisis, cuyas
 * categorías vienen de un catálogo de BD, no de un set fijo conocido de antemano): 8 tonos en
 * orden fijo, tal como vienen en references/palette.md de la skill dataviz (ya validados,
 * ΔE adyacente mínimo 24.2). Se asignan por orden de primera aparición, nunca por posición tras
 * ordenar — así una categoría conserva su color aunque cambie su ranking entre refrescos.
 */
const CATEGORICAL_SLOTS = [
  '#2a78d6', // blue
  '#1baf7a', // aqua
  '#eda100', // yellow
  '#008300', // green
  '#4a3aa7', // violet
  '#e34948', // red
  '#e87ba4', // magenta
  '#eb6834', // orange
]

/** Asigna un color categórico estable por etiqueta, en orden de primera aparición. */
export function createCategoricalColorAssigner() {
  const assigned = new Map<string, string>()
  return (label: string): string => {
    if (!assigned.has(label)) {
      const slot = CATEGORICAL_SLOTS[assigned.size % CATEGORICAL_SLOTS.length]
      assigned.set(label, slot)
    }
    return assigned.get(label)!
  }
}
