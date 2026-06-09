package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CargaDiariaPreviewDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteRegistroDAO;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CargaDiariaValidacionService {

    private final ExpedienteRegistroDAO expedienteRegistroDAO;
    private final CorrelativoExpedienteService correlativoExpedienteService;

    public CargaDiariaValidacionService() {
        this(new ExpedienteRegistroDAO(), new CorrelativoExpedienteService());
    }

    public CargaDiariaValidacionService(
            ExpedienteRegistroDAO expedienteRegistroDAO,
            CorrelativoExpedienteService correlativoExpedienteService) {
        this.expedienteRegistroDAO = expedienteRegistroDAO;
        this.correlativoExpedienteService = correlativoExpedienteService;
    }

    public List<CargaDiariaPreviewDTO> validar(List<CargaDiariaPreviewDTO> registros) throws SQLException {
        if (registros == null) {
            return new ArrayList<>();
        }

        Map<String, Integer> actaTitular = new HashMap<>();
        Map<String, Integer> primeraFilaActaTitular = new HashMap<>();
        for (CargaDiariaPreviewDTO item : registros) {
            item.reiniciarValidacion();
            String claveActaTitular = claveActaTitular(item);
            sumarClave(actaTitular, claveActaTitular);
            if (hasText(claveActaTitular) && !primeraFilaActaTitular.containsKey(claveActaTitular)) {
                primeraFilaActaTitular.put(claveActaTitular, item.getFila());
            }
        }

        Map<Integer, String> duplicadosBase = expedienteRegistroDAO.detectarDuplicadosContraBase(registros);

        int indiceValido = 1;
        for (CargaDiariaPreviewDTO item : registros) {
            boolean error = false;
            if (!hasText(item.getNumeroTramite())) {
                item.agregarMensaje("Número de trámite obligatorio.");
                error = true;
            }
            if (!hasText(item.getNumeroDocumento())) {
                item.agregarMensaje("Número de documento obligatorio.");
                error = true;
            }
            if (!hasText(item.getTipoProcedimiento())) {
                item.agregarMensaje("Tipo de procedimiento obligatorio.");
                error = true;
            }
            if (!hasText(item.getTipoSolicitud())) {
                item.agregarMensaje("Tipo de solicitud obligatorio.");
                error = true;
            }
            if (!hasText(item.getTipoDocumento())) {
                item.agregarMensaje("Tipo de documento obligatorio.");
                error = true;
            }
            if (!hasText(item.getNumeroActa())) {
                item.agregarMensaje("Número de acta obligatorio.");
                error = true;
            }
            if (!hasText(item.getTitular())) {
                item.agregarMensaje("Titular obligatorio.");
                error = true;
            }
            if (item.getFechaRecepcion() == null) {
                item.agregarMensaje("Fecha recepción inválida u obligatoria.");
                error = true;
            }

            List<String> motivosDuplicado = new ArrayList<>();
            String claveActaTitular = claveActaTitular(item);
            Integer cantidadActaTitular = hasText(claveActaTitular) ? actaTitular.get(claveActaTitular) : null;
            if (cantidadActaTitular != null && cantidadActaTitular > 1) {
                Integer primeraFila = primeraFilaActaTitular.get(claveActaTitular);
                if (primeraFila != null && item.getFila() == primeraFila) {
                    motivosDuplicado.add("Existen filas repetidas con la misma acta y titular.");
                } else {
                    motivosDuplicado.add("Acta y titular repetidos en el archivo. Ya existe una primera ocurrencia en la fila " + primeraFila + ".");
                }
            }
            String duplicadoBd = duplicadosBase.get(item.getFila());
            if (duplicadoBd != null) {
                motivosDuplicado.add(duplicadoBd);
            }

            if (!motivosDuplicado.isEmpty()) {
                item.setPosibleDuplicado(true);
                item.setMotivoDuplicado(String.join(" | ", motivosDuplicado));
                item.agregarMensaje(item.getMotivoDuplicado());
            }

            item.setNumeroExpedienteGenerado(correlativoExpedienteService.generarPreliminar(indiceValido++));
            item.setListoParaRegistrar(true);
            if (item.isPosibleDuplicado()) {
                item.setEstadoValidacion("Duplicado");
            } else if (error) {
                item.setEstadoValidacion("Con observaciones");
            } else {
                item.setEstadoValidacion("Válido");
                item.setMensajeValidacion("Listo para registrar.");
            }
        }

        return registros;
    }

    private void sumarClave(Map<String, Integer> contador, String key) {
        if (!hasText(key)) {
            return;
        }
        Integer actual = contador.get(key);
        contador.put(key, actual == null ? 1 : actual + 1);
    }

    private String claveActaTitular(CargaDiariaPreviewDTO item) {
        if (item == null || !hasText(item.getNumeroActa()) || !hasText(item.getTitular())) {
            return null;
        }
        return clave(item.getNumeroActa()) + "|" + clave(item.getTitular());
    }

    private String clave(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        return normalized.replaceAll("[^A-Z0-9]", "");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
