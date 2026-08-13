import { Alert, Box, CircularProgress, Grid, Stack, Typography } from '@mui/material'
import type { ExpedienteBandeja } from '../api/registroBandejaApi'
import type { ExpedienteEdicionManual } from '../api/registroApi'
import { sdrercColors } from '../theme'
import { StatusBadge, diasBadgeTone } from './StatusBadge'
import { SectionCard } from './RegistroExpedienteForm'

function formatearFecha(iso: string | null): string {
  if (!iso) return '-'
  const [anio, mes, dia] = iso.slice(0, 10).split('-')
  return `${dia}/${mes}/${anio}`
}

function Dato({ label, value }: { label: string; value: string | null | undefined }) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary" component="div">
        {label}
      </Typography>
      <Typography variant="body2">{value && value.trim() !== '' ? value : '-'}</Typography>
    </Box>
  )
}

interface PanelDatosExpedienteProps {
  bandeja: ExpedienteBandeja
  detalle: ExpedienteEdicionManual | null
  cargando: boolean
  error: string | null
}

/**
 * Equivalente web del "Panel de datos" de V2 (doble clic en Bandeja Registro): título genérico
 * "Panel de datos" + titular en azul debajo (ver CLAUDE.md raíz, "Panel derecho Registro"), mismos
 * 7 bloques: Datos del plazo, Datos del expediente, Datos del acta, Datos de solicitud, Datos del
 * titular, Datos del solicitante, Datos de Notificación y Ubicación. Puramente informativo, sin
 * botones — solo lectura. Combina datos ya disponibles en la fila de bandeja (plazo, canal,
 * procedimiento — sin round-trip adicional) con una consulta de detalle (documento del titular,
 * contacto/ubigeo del solicitante) que no expone la Bandeja.
 */
export function PanelDatosExpediente({ bandeja, detalle, cargando, error }: PanelDatosExpedienteProps) {
  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h6" sx={{ color: sdrercColors.primary }}>
          Panel de datos
        </Typography>
        <Typography variant="subtitle1" sx={{ color: sdrercColors.info, fontWeight: 600 }}>
          {bandeja.titular || '-'}
        </Typography>
      </Box>

      {error && <Alert severity="error">{error}</Alert>}

      <SectionCard title="Datos del plazo">
        <Grid container spacing={2}>
          <Grid size={6}>
            <Typography variant="caption" color="text.secondary" component="div">
              Días
            </Typography>
            <StatusBadge
              label={bandeja.diasRestantes === null || bandeja.diasRestantes === undefined ? '-' : String(bandeja.diasRestantes)}
              tone={diasBadgeTone(bandeja.diasRestantes)}
            />
          </Grid>
          <Grid size={6}>
            <Dato label="Fecha Vencimiento" value={formatearFecha(bandeja.fechaVencimiento)} />
          </Grid>
        </Grid>
      </SectionCard>

      <SectionCard title="Datos del expediente">
        <Grid container spacing={2}>
          <Grid size={6}>
            <Dato label="N° expediente" value={bandeja.numeroExpediente} />
          </Grid>
          <Grid size={6}>
            <Dato label="N° expediente SGD" value={bandeja.numeroExpedienteSgd} />
          </Grid>
          <Grid size={6}>
            <Dato label="N° trámite" value={bandeja.numeroTramiteDocumentario} />
          </Grid>
          <Grid size={6}>
            <Dato label="Canal" value={bandeja.canal} />
          </Grid>
          <Grid size={6}>
            <Dato label="Etapa" value={bandeja.etapaCodigo} />
          </Grid>
          <Grid size={6}>
            <Dato label="Estado" value={bandeja.estadoCodigo} />
          </Grid>
        </Grid>
      </SectionCard>

      <SectionCard title="Datos del acta">
        <Grid container spacing={2}>
          <Grid size={6}>
            <Dato label="Tipo de acta" value={bandeja.tipoActa} />
          </Grid>
          <Grid size={6}>
            <Dato label="N° acta" value={bandeja.numeroActa} />
          </Grid>
        </Grid>
      </SectionCard>

      <SectionCard title="Datos de solicitud">
        <Grid container spacing={2}>
          <Grid size={6}>
            <Dato label="Procedimiento registral" value={bandeja.procedimiento} />
          </Grid>
          <Grid size={6}>
            <Dato label="Fecha de recepción" value={formatearFecha(bandeja.fechaRecepcion)} />
          </Grid>
          <Grid size={6}>
            <Dato label="Tipo de solicitud" value={detalle?.solicitud.tipoSolicitudNombre} />
          </Grid>
          <Grid size={6}>
            <Dato label="Tipo de documento" value={detalle?.solicitud.tipoDocumentoNombre} />
          </Grid>
          <Grid size={6}>
            <Dato label="N° documento" value={detalle?.solicitud.numeroDocumento} />
          </Grid>
          <Grid size={6}>
            <Dato label="Validación inicial" value={detalle?.solicitud.validacionInicial} />
          </Grid>
          <Grid size={6}>
            <Dato label="Hoja de envío" value={detalle?.solicitud.hojaEnvio} />
          </Grid>
        </Grid>
      </SectionCard>

      <SectionCard title="Datos del titular">
        <Grid container spacing={2}>
          <Grid size={12}>
            <Dato label="Nombres" value={detalle?.titular.nombreCompleto ?? bandeja.titular} />
          </Grid>
          <Grid size={6}>
            <Dato label="Tipo documento" value={detalle?.titular.tipoDocumento} />
          </Grid>
          <Grid size={6}>
            <Dato label="N° documento" value={detalle?.titular.numeroDocumento} />
          </Grid>
        </Grid>
      </SectionCard>

      <SectionCard title="Datos del solicitante">
        <Grid container spacing={2}>
          <Grid size={12}>
            <Dato label="Nombres / Razón Social" value={detalle?.remitente.nombreCompleto} />
          </Grid>
          <Grid size={6}>
            <Dato label="Tipo documento" value={detalle?.remitente.tipoDocumento} />
          </Grid>
          <Grid size={6}>
            <Dato label="N° documento" value={detalle?.remitente.numeroDocumento} />
          </Grid>
        </Grid>
      </SectionCard>

      <SectionCard title="Datos de Notificación y Ubicación">
        <Grid container spacing={2}>
          <Grid size={6}>
            <Dato label="Correo" value={detalle?.remitente.correo} />
          </Grid>
          <Grid size={6}>
            <Dato label="Teléfono" value={detalle?.remitente.telefono} />
          </Grid>
          <Grid size={4}>
            <Dato label="Departamento" value={detalle?.remitente.departamento} />
          </Grid>
          <Grid size={4}>
            <Dato label="Provincia" value={detalle?.remitente.provincia} />
          </Grid>
          <Grid size={4}>
            <Dato label="Distrito" value={detalle?.remitente.distrito} />
          </Grid>
          <Grid size={12}>
            <Dato label="Dirección" value={detalle?.remitente.direccion} />
          </Grid>
        </Grid>
      </SectionCard>

      {cargando && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
          <CircularProgress size={22} />
        </Box>
      )}
    </Stack>
  )
}
