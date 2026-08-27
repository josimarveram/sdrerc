import { useEffect, useState } from 'react'
import { Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material'
import { listarIntegrantesGrupoFamiliar, type GrupoFamiliarIntegrante } from '../api/grupoFamiliarApi'

interface GrupoFamiliarConfirmadoContentProps {
  idExpediente: number
}

/**
 * Subpestaña "Grupo Familiar" de solo lectura (21/08/2026, pedido explícito del usuario) para los
 * workspaces multipestañas de Análisis/Verificación/Ejecución. A diferencia de
 * `GrupoFamiliarContent.tsx` (Registro/Asignación — candidatos por apellidos + "Asociar al grupo
 * familiar" + retirar integrante), acá NO hay ninguna acción: solo se listan los integrantes ya
 * confirmados (`GET /api/grupo-familiar/{idExpediente}/integrantes`, mismo endpoint genérico ya
 * compartido, sin cambios de backend). Estos 3 módulos no gestionan la composición del grupo
 * familiar (eso es responsabilidad de Registro/Asignación), solo necesitan poder consultarla.
 *
 * La página que embebe este componente decide SI mostrar la subpestaña en absoluto — condicionado a
 * `expediente.grupoFamiliar` (flag `EXPEDIENTE_SOLICITUD.grupo_familiar`, el mismo que ya usan
 * Análisis/Verificación para su KPI "Grupo Familiar Confirmado"; Ejecución no lo exponía hasta este
 * incremento, ver `EjecucionExpedienteDAO`/`EjecucionExpedienteDTO`). Mismas 6 columnas de solo
 * lectura que la tabla "Grupo familiar actual" de `GrupoFamiliarContent.tsx`, sin la columna de
 * acción "Retirar".
 */
export function GrupoFamiliarConfirmadoContent({ idExpediente }: GrupoFamiliarConfirmadoContentProps) {
  const [integrantes, setIntegrantes] = useState<GrupoFamiliarIntegrante[]>([])
  const [cargando, setCargando] = useState(false)

  useEffect(() => {
    setCargando(true)
    listarIntegrantesGrupoFamiliar(idExpediente)
      .then(setIntegrantes)
      .catch(() => setIntegrantes([]))
      .finally(() => setCargando(false))
  }, [idExpediente])

  return (
    <Stack spacing={2}>
      <Typography variant="subtitle2">Grupo familiar actual</Typography>
      {cargando ? (
        <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
          Cargando...
        </Typography>
      ) : integrantes.length === 0 ? (
        <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
          Este expediente todavía no pertenece a un grupo familiar confirmado.
        </Typography>
      ) : (
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Nombre</TableCell>
                <TableCell>N° Expediente</TableCell>
                <TableCell>Etapa</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell>Abogado</TableCell>
                <TableCell>Tipo Acta</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {integrantes.map((i) => (
                <TableRow key={i.idPersona}>
                  <TableCell>{i.nombreCompleto || '-'}</TableCell>
                  <TableCell>{i.numeroExpediente || '-'}</TableCell>
                  <TableCell>{i.etapaCodigo || '-'}</TableCell>
                  <TableCell>{i.estadoCodigo || '-'}</TableCell>
                  <TableCell>{i.abogadoAsignado || '-'}</TableCell>
                  <TableCell>{i.tipoActa || '-'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Stack>
  )
}
