import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { AppLayout } from './layout/AppLayout'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { BandejaExpedientesPage } from './pages/BandejaExpedientesPage'
import { RegistroManualPage } from './pages/RegistroManualPage'
import { RegistroBandejaPage } from './pages/RegistroBandejaPage'
import { RegistroCargaDiariaPage } from './pages/RegistroCargaDiariaPage'
import { RegistroEdicionManualPage } from './pages/RegistroEdicionManualPage'
import { AsignacionBandejaPage } from './pages/AsignacionBandejaPage'
import { AsignacionCartasRespuestaPage } from './pages/AsignacionCartasRespuestaPage'
import { AsignacionCargaAbogadosPage } from './pages/AsignacionCargaAbogadosPage'
import { AnalisisBandejaPage } from './pages/AnalisisBandejaPage'
import { VerificacionBandejaPage } from './pages/VerificacionBandejaPage'
import { EjecucionBandejaPage } from './pages/EjecucionBandejaPage'
import { NotificacionIndexPage } from './pages/NotificacionIndexPage'
import { NotificacionAsignacionBandejaPage } from './pages/NotificacionAsignacionBandejaPage'
import { NotificacionValidacionBandejaPage } from './pages/NotificacionValidacionBandejaPage'
import { NotificacionNotificacionBandejaPage } from './pages/NotificacionNotificacionBandejaPage'
import { NotificacionPublicacionBandejaPage } from './pages/NotificacionPublicacionBandejaPage'
import { AdminUsuariosPage } from './pages/AdminUsuariosPage'
import { AdminRolesPage } from './pages/AdminRolesPage'
import { AdminEquipoJuridicoPage } from './pages/AdminEquipoJuridicoPage'
import { AdminPersonalPorSupervisorPage } from './pages/AdminPersonalPorSupervisorPage'
import { AdminPlantillasPage } from './pages/AdminPlantillasPage'
import { AdminFeriadosPage } from './pages/AdminFeriadosPage'
import { AdminPlazosPage } from './pages/AdminPlazosPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<HomePage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/bandeja" element={<BandejaExpedientesPage />} />
        <Route path="/registro/manual" element={<RegistroManualPage />} />
        <Route path="/registro" element={<RegistroBandejaPage />} />
        <Route path="/registro/carga-diaria" element={<RegistroCargaDiariaPage />} />
        <Route path="/registro/editar/:idExpediente" element={<RegistroEdicionManualPage />} />
        <Route path="/asignacion" element={<AsignacionBandejaPage />} />
        <Route path="/asignacion/cartas-respuesta" element={<AsignacionCartasRespuestaPage />} />
        <Route path="/asignacion/carga-abogados" element={<AsignacionCargaAbogadosPage />} />
        <Route path="/analisis" element={<AnalisisBandejaPage />} />
        <Route path="/verificacion" element={<VerificacionBandejaPage />} />
        <Route path="/ejecucion" element={<EjecucionBandejaPage />} />
        <Route path="/notificacion" element={<NotificacionIndexPage />} />
        <Route path="/notificacion/asignacion" element={<NotificacionAsignacionBandejaPage />} />
        <Route path="/notificacion/validacion" element={<NotificacionValidacionBandejaPage />} />
        <Route path="/notificacion/notificacion" element={<NotificacionNotificacionBandejaPage />} />
        <Route path="/notificacion/publicacion" element={<NotificacionPublicacionBandejaPage />} />
        <Route path="/admin/usuarios" element={<AdminUsuariosPage />} />
        <Route path="/admin/roles" element={<AdminRolesPage />} />
        <Route path="/admin/equipo-juridico" element={<AdminEquipoJuridicoPage />} />
        <Route path="/admin/equipo-juridico/supervisor" element={<AdminPersonalPorSupervisorPage />} />
        <Route path="/admin/plantillas" element={<AdminPlantillasPage />} />
        <Route path="/admin/feriados" element={<AdminFeriadosPage />} />
        <Route path="/admin/plazos" element={<AdminPlazosPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
