/**
 * Rango de fechas por defecto de los buscadores de bandeja: día 01 del mes actual -> hoy (mismo
 * criterio en Registro, Asignación y sus sub-pestañas, y en Dashboard). Antes cada página tenía su
 * propia copia de estas 2 funciones (o, en algunas bandejas de Asignación, ningún default) —
 * consolidado acá el 13/08/2026, pedido explícito del usuario: "en ambos filtros de busqueda de
 * ambos modulos en todas las bandejas debe cargar por defecto la fecha desde... 01 del mes actual y
 * la fecha hasta... la fecha actual".
 *
 * `hoyISO` calcula con componentes de fecha LOCALES (no `toISOString()`, que es UTC): con
 * `toISOString()`, en Perú (UTC-5) el resultado se adelanta un día después de las ~19:00 hora
 * local, mostrando "mañana" como fecha de hoy — bug latente que existía en las 2 copias previas de
 * `hoyISO`, corregido de paso al consolidar.
 */
export function primerDiaMesActualISO(): string {
  const hoy = new Date()
  return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-01`
}

export function hoyISO(): string {
  const hoy = new Date()
  return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`
}
