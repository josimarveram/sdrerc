import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { CssBaseline, ThemeProvider } from '@mui/material'
import App from './App.tsx'
import { AuthProvider } from './auth/AuthContext'
import { AppDialogProvider } from './dialogs/AppDialogProvider'
import { sdrercTheme } from './theme'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider theme={sdrercTheme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <AppDialogProvider>
            <App />
          </AppDialogProvider>
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  </StrictMode>,
)
