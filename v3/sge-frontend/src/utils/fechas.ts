/**
 * Rango de fechas por defecto de los buscadores de bandeja: día 01 del mes anterior -> hoy (mismo
 * criterio en todas las bandejas de todos los módulos, y en Dashboard). Antes cada página tenía su
 * propia copia de estas 2 funciones (o, en algunas bandejas de Asignación, ningún default) —
 * consolidado acá el 13/08/2026, pedido explícito del usuario: "en ambos filtros de busqueda de
 * ambos modulos en todas las bandejas debe cargar por defecto la fecha desde... 01 del mes actual y
 * la fecha hasta... la fecha actual".
 *
 * **01/09/2026, pedido explícito del usuario**: el default de "Fecha desde" cambia de "01 del mes
 * actual" a "01 del mes anterior" (ej. si hoy es 01/09/2026, el rango por defecto arranca en
 * 01/08/2026) — más margen para encontrar registros recientes sin tener que ajustar el filtro a
 * mano apenas empieza un mes nuevo. Función renombrada de `primerDiaMesActualISO` a
 * `primerDiaMesAnteriorISO` (antes el nombre ya no describiría lo que calcula) — actualizado en
 * los 13 archivos de bandeja que la consumen.
 *
 * `hoyISO` calcula con componentes de fecha LOCALES (no `toISOString()`, que es UTC): con
 * `toISOString()`, en Perú (UTC-5) el resultado se adelanta un día después de las ~19:00 hora
 * local, mostrando "mañana" como fecha de hoy — bug latente que existía en las 2 copias previas de
 * `hoyISO`, corregido de paso al consolidar.
 */
export function primerDiaMesAnteriorISO(): string {
  const hoy = new Date()
  const primerDiaMesAnterior = new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1)
  return `${primerDiaMesAnterior.getFullYear()}-${String(primerDiaMesAnterior.getMonth() + 1).padStart(2, '0')}-01`
}

export function hoyISO(): string {
  const hoy = new Date()
  return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`
}
