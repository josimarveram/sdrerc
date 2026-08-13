import { Box, Button, Chip, Paper, Stack, Typography } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { sdrercColors } from '../theme'

/**
 * Placeholder minimo post-login. Los modulos de negocio reales empiezan en la Fase 1
 * (Dashboard) del plan de migracion; esta pantalla solo confirma que la Fase 0
 * (auth end-to-end) funciona.
 */
export function HomePage() {
  const { session, logout } = useAuth()
  const navigate = useNavigate()

  if (!session) return null

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: sdrercColors.backgroundDefault, p: 4 }}>
      <Paper elevation={1} sx={{ maxWidth: 480, mx: 'auto', p: 4, borderRadius: 2 }}>
        <Typography variant="h6" sx={{ color: sdrercColors.primary, mb: 2 }}>
          Sesión iniciada
        </Typography>
        <Stack spacing={1} sx={{ mb: 3 }}>
          <Typography variant="body1">
            <strong>Usuario:</strong> {session.username}
          </Typography>
          <Typography variant="body1">
            <strong>Nombre:</strong> {session.nombreCompleto}
          </Typography>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <Typography variant="body1">
              <strong>Roles:</strong>
            </Typography>
            {session.roles.map((r) => (
              <Chip key={r} label={r} size="small" color="primary" variant="outlined" />
            ))}
          </Stack>
        </Stack>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Fase 0 del plan de migración completa: login + 2FA reales contra la base de datos
          compartida con V2. Los módulos de negocio se agregan en las fases siguientes.
        </Typography>
        <Stack direction="row" spacing={2}>
          {session.roles.includes('ADMIN_SISTEMA') && (
            <Button variant="contained" onClick={() => navigate('/dashboard')}>
              Ir al Dashboard
            </Button>
          )}
          <Button variant="contained" onClick={() => navigate('/registro')}>
            Bandeja Registro
          </Button>
          <Button variant="outlined" onClick={() => navigate('/registro/manual')}>
            Registro manual
          </Button>
          <Button variant="contained" onClick={() => navigate('/asignacion')}>
            Bandeja Asignación
          </Button>
          <Button
            variant="outlined"
            onClick={() => {
              logout()
              navigate('/login')
            }}
          >
            Cerrar sesión
          </Button>
        </Stack>
      </Paper>
    </Box>
  )
}
