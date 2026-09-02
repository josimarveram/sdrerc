import { Avatar, Box, Card, CardActionArea, CardContent, Chip, Paper, Stack, Typography, alpha } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { sdrercColors } from '../theme'
import { NAV_GROUPS, type NavItem } from '../layout/navConfig'

function inicialesDe(nombre: string): string {
  const partes = nombre.trim().split(/\s+/).filter(Boolean)
  if (partes.length === 0) return '?'
  if (partes.length === 1) return partes[0].slice(0, 2).toUpperCase()
  return (partes[0][0] + partes[1][0]).toUpperCase()
}

// Color de acento por grupo (01/09/2026, pedido explícito del usuario: "mejora el diseño de Inicio
// acorde al login" — autorizado explícitamente tras un análisis previo con 3 puntos de decisión).
// Reutiliza tokens semánticos YA existentes en theme.ts (info=identidad de "Datos" en todo el
// sistema, primary=azul institucional, textSecondary=neutro para Administración) — deliberadamente
// NO una paleta de 14 colores por módulo individual, solo 3 acentos con significado por grupo.
const COLOR_POR_GRUPO: Record<string, string> = {
  'Dashboard y Reportes': sdrercColors.info,
  Operación: sdrercColors.primary,
  Administración: sdrercColors.textSecondary,
}

/**
 * Home con accesos rápidos por módulo (mismo `NAV_GROUPS` que el Sidebar, sin duplicar la lista),
 * reemplaza el placeholder "Sesión iniciada" de la Fase 0. Los módulos aún no construidos se
 * muestran igual, deshabilitados con chip "Próximamente" (misma decisión que en el Sidebar).
 *
 * Rediseño premium (01/09/2026, mismo día del rediseño de `LoginPage.tsx`): la pantalla original
 * (tarjetas `Card variant="outlined"` planas, ícono siempre en `primary` sin importar el módulo,
 * encabezados de sección como texto suelto) era la única pantalla principal del sistema sin ningún
 * tratamiento del "sistema de diseño premium" ya aplicado a las bandejas operativas (`MetricCard`,
 * ver su Javadoc) — de ahí la sensación de "no combina con el login". Alcance decidido
 * explícitamente con el usuario (3 puntos, ver conversación): acento sutil (no un banner de
 * pantalla completa como el login, que solo tiene 1 tarjeta — acá conviven 14 módulos y necesitan
 * densidad), colores por GRUPO (no por módulo individual) reutilizando tokens ya existentes, y sin
 * tocar `PageHeaderSticky`/el resto de páginas (fuera de alcance de este pedido puntual).
 * - Tarjeta de saludo: avatar circular con el mismo gradiente (`primaryDark → primary`) ya usado en
 *   la cabecera de `LoginPage.tsx` — misma firma visual de marca, sin banner de ancho completo.
 * - Encabezados de sección: gana una barra corta de 4px del color del grupo junto al texto
 *   uppercase ya existente, para que las 3 secciones se lean como bloques reales.
 * - Tarjetas de módulo: mismo lenguaje que `MetricCard.tsx` (franja superior de 3px, ícono dentro
 *   de un círculo con fondo pastel `alpha(color,0.12)` en vez de un ícono plano, borde que se
 *   intensifica en hover con elevación) — mecanismo copiado, no reinventado. Los módulos "Próximamente"
 *   quedan con acento neutro (`textSecondary`) y sin hover, para que el contraste
 *   disponible/no-disponible sea más nítido que el `opacity` genérico anterior.
 */
export function HomePage() {
  const { session } = useAuth()
  const navigate = useNavigate()

  if (!session) return null

  const grupos = NAV_GROUPS.filter((g) => g.title !== null).map((g) => ({
    ...g,
    items: g.items.filter((item) => !item.requiredPermiso || session.permisos.length === 0 || session.permisos.includes(item.requiredPermiso)),
  }))

  return (
    <Box>
      <Paper
        elevation={1}
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 2,
          p: 3,
          mb: 4,
          borderRadius: 3,
        }}
      >
        <Avatar
          sx={{
            width: 56,
            height: 56,
            fontWeight: 700,
            fontSize: '1.1rem',
            background: `linear-gradient(135deg, ${sdrercColors.primaryDark} 0%, ${sdrercColors.primary} 100%)`,
          }}
        >
          {inicialesDe(session.nombreCompleto || session.username)}
        </Avatar>
        <Box>
          <Typography variant="h6" sx={{ color: sdrercColors.primary, fontWeight: 700, mb: 0.25 }}>
            Hola, {session.nombreCompleto || session.username}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Accesos rápidos a los módulos de SGE.
          </Typography>
        </Box>
      </Paper>

      <Stack spacing={4}>
        {grupos.map((grupo) => {
          if (!grupo.title || grupo.items.length === 0) return null
          const color = COLOR_POR_GRUPO[grupo.title] ?? sdrercColors.primary
          return (
            <Box key={grupo.title}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1.5 }}>
                <Box sx={{ width: 4, height: 16, borderRadius: 1, bgcolor: color }} />
                <Typography
                  variant="subtitle2"
                  color="text.secondary"
                  sx={{ textTransform: 'uppercase', letterSpacing: 0.6, fontWeight: 700 }}
                >
                  {grupo.title}
                </Typography>
              </Stack>
              <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 2 }}>
                {grupo.items.map((item) => (
                  <ModuloCard key={item.label} item={item} color={color} onClick={() => item.path && navigate(item.path)} />
                ))}
              </Box>
            </Box>
          )
        })}
      </Stack>
    </Box>
  )
}

function ModuloCard({ item, color, onClick }: { item: NavItem; color: string; onClick: () => void }) {
  const habilitado = item.built
  const colorEfectivo = habilitado ? color : sdrercColors.textSecondary

  return (
    <Card
      variant="outlined"
      sx={{
        position: 'relative',
        overflow: 'hidden',
        borderRadius: 2,
        border: `1px solid ${alpha(colorEfectivo, habilitado ? 0.25 : 0.15)}`,
        opacity: habilitado ? 1 : 0.75,
        transition: 'border-color 150ms ease, box-shadow 150ms ease, transform 150ms ease',
        ...(habilitado && {
          '&:hover': {
            borderColor: alpha(color, 0.5),
            boxShadow: '0 4px 12px rgba(16,24,40,0.08)',
            transform: 'translateY(-1px)',
          },
        }),
      }}
    >
      <Box sx={{ position: 'absolute', top: 0, left: 0, right: 0, height: 3, bgcolor: colorEfectivo, zIndex: 1 }} />
      <CardActionArea disabled={!habilitado} onClick={onClick} sx={{ height: '100%' }}>
        <CardContent>
          <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
            <Box
              sx={{
                width: 36,
                height: 36,
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
                bgcolor: alpha(colorEfectivo, 0.12),
                color: colorEfectivo,
              }}
            >
              {item.icon}
            </Box>
            <Typography variant="body1" sx={{ fontWeight: 600 }}>
              {item.label}
            </Typography>
          </Stack>
          {!habilitado && <Chip label="Próximamente" size="small" sx={{ mt: 1.5 }} />}
        </CardContent>
      </CardActionArea>
    </Card>
  )
}
