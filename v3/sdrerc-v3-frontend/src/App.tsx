import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { RegistroManualPage } from './pages/RegistroManualPage'
import { RegistroBandejaPage } from './pages/RegistroBandejaPage'
import { RegistroCargaDiariaPage } from './pages/RegistroCargaDiariaPage'
import { RegistroEdicionManualPage } from './pages/RegistroEdicionManualPage'
import { AsignacionBandejaPage } from './pages/AsignacionBandejaPage'
import { AsignacionCartasRespuestaPage } from './pages/AsignacionCartasRespuestaPage'
import { AsignacionCargaAbogadosPage } from './pages/AsignacionCargaAbogadosPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <HomePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/registro/manual"
        element={
          <ProtectedRoute>
            <RegistroManualPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/registro"
        element={
          <ProtectedRoute>
            <RegistroBandejaPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/registro/carga-diaria"
        element={
          <ProtectedRoute>
            <RegistroCargaDiariaPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/registro/editar/:idExpediente"
        element={
          <ProtectedRoute>
            <RegistroEdicionManualPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/asignacion"
        element={
          <ProtectedRoute>
            <AsignacionBandejaPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/asignacion/cartas-respuesta"
        element={
          <ProtectedRoute>
            <AsignacionCartasRespuestaPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/asignacion/carga-abogados"
        element={
          <ProtectedRoute>
            <AsignacionCargaAbogadosPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
