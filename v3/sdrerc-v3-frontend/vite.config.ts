import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // El backend (v3/sdrerc-v3-backend) no tiene CORS configurado a propósito en
    // la Fase 0: en dev, el navegador ve las llamadas a /api/* como same-origin
    // porque Vite las reenvia a localhost:8081. En produccion, el plan (seccion 2)
    // ya propone servir el build estatico del frontend junto al backend, mismo
    // origen real, sin necesitar CORS tampoco.
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
})
