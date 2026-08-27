import { Box, Card, CardActionArea, CardContent, Chip, Stack, Typography } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { sdrercColors } from '../theme'
import { NAV_GROUPS } from '../layout/navConfig'

/**
 * Home con accesos rápidos por módulo (mismo `NAV_GROUPS` que el Sidebar, sin duplicar la lista),
 * reemplaza el placeholder "Sesión iniciada" de la Fase 0. Los módulos aún no construidos se
 * muestran igual, deshabilitados con chip "Próximamente" (misma decisión que en el Sidebar).
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
      <Typography variant="h6" sx={{ color: sdrercColors.primary, mb: 0.5 }}>
        Hola, {session.nombreCompleto || session.username}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
        Accesos rápidos a los módulos de SDRERC.
      </Typography>

      <Stack spacing={4}>
        {grupos.map((grupo) =>
          grupo.items.length === 0 ? null : (
            <Box key={grupo.title}>
              <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1.5, textTransform: 'uppercase', letterSpacing: 0.6 }}>
                {grupo.title}
              </Typography>
              <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 2 }}>
                {grupo.items.map((item) => (
                  <Card key={item.label} variant="outlined" sx={{ opacity: item.built ? 1 : 0.6 }}>
                    <CardActionArea disabled={!item.built} onClick={() => item.path && navigate(item.path)} sx={{ height: '100%' }}>
                      <CardContent>
                        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center', mb: item.built ? 0 : 1 }}>
                          <Box sx={{ color: sdrercColors.primary, display: 'flex' }}>{item.icon}</Box>
                          <Typography variant="body1" sx={{ fontWeight: 600 }}>
                            {item.label}
                          </Typography>
                        </Stack>
                        {!item.built && <Chip label="Próximamente" size="small" sx={{ mt: 1 }} />}
                      </CardContent>
                    </CardActionArea>
                  </Card>
                ))}
              </Box>
            </Box>
          ),
        )}
      </Stack>
    </Box>
  )
}
