package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CargaDiariaPreviewDTO;
import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
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
    private final GrupoFamiliarHeuristicaService grupoFamiliarHeuristicaService;

    public CargaDiariaValidacionService() {
        this(new ExpedienteRegistroDAO(), new CorrelativoExpedienteService(), new GrupoFamiliarHeuristicaService());
    }

    public CargaDiariaValidacionService(
            ExpedienteRegistroDAO expedienteRegistroDAO,
            CorrelativoExpedienteService correlativoExpedienteService,
            GrupoFamiliarHeuristicaService grupoFamiliarHeuristicaService) {
        this.expedienteRegistroDAO = expedienteRegistroDAO;
        this.correlativoExpedienteService = correlativoExpedienteService;
        this.grupoFamiliarHeuristicaService = grupoFamiliarHeuristicaService;
    }

    public List<CargaDiariaPreviewDTO> validar(List<CargaDiariaPreviewDTO> registros) throws SQLException {
        if (registros == null) {
            return new ArrayList<>();
        }

        Map<String, Integer> actaTitular = new HashMap<>();
        Map<String, Integer> primeraFilaActaTitular = new HashMap<>();
        Map<String, Integer> apellidosTitular = new HashMap<>();
        for (CargaDiariaPreviewDTO item : registros) {
            item.reiniciarValidacion();
            item.limpiarDeteccionGrupoFamiliar();
            String claveActaTitular = claveActaTitular(item);
            sumarClave(actaTitular, claveActaTitular);
            if (hasText(claveActaTitular) && !primeraFilaActaTitular.containsKey(claveActaTitular)) {
                primeraFilaActaTitular.put(claveActaTitular, item.getFila());
            }
            sumarClave(apellidosTitular, claveApellidosTitular(item));
        }

        Map<Integer, String> duplicadosBase = expedienteRegistroDAO.detectarDuplicadosContraBase(registros);
        Map<Integer, String> posiblesGruposFamiliaresBase =
                expedienteRegistroDAO.detectarPosiblesGruposFamiliaresContraBase(registros);

        int siguienteCorrelativo = expedienteRegistroDAO.obtenerSiguienteCorrelativoExpediente(correlativoExpedienteService.anioActual());
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
            if (!hasText(item.getTipoActa())) {
                item.agregarMensaje("Tipo de acta obligatorio.");
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
            if (!hasText(item.getCanalRecepcion())) {
                item.agregarMensaje("No se pudo determinar canal de recepción con las reglas de la plantilla.");
                error = true;
            }

            List<String> erroresIdentidad = validarIdentidad(item);
            if (!erroresIdentidad.isEmpty()) {
                for (String mensaje : erroresIdentidad) {
                    item.agregarMensaje(mensaje);
                }
                error = true;
            }

            List<String> motivosDuplicado = new ArrayList<>();
            String claveActaTitular = claveActaTitular(item);
            Integer cantidadActaTitular = hasText(claveActaTitular) ? actaTitular.get(claveActaTitular) : null;
            if (cantidadActaTitular != null && cantidadActaTitular > 1) {
                Integer primeraFila = primeraFilaActaTitular.get(claveActaTitular);
                if (primeraFila == null || item.getFila() != primeraFila) {
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

            String claveApellidos = claveApellidosTitular(item);
            Integer cantidadApellidos = hasText(claveApellidos) ? apellidosTitular.get(claveApellidos) : null;
            if (cantidadApellidos != null && cantidadApellidos > 1) {
                item.agregarObservacionGrupoFamiliar(
                        "COINCIDENCIA_APELLIDOS_EXCEL",
                        "Posible grupo familiar por coincidencia de apellidos.");
                item.agregarMensaje("Posible grupo familiar por coincidencia de apellidos.");
            }
            String grupoFamiliarBd = posiblesGruposFamiliaresBase.get(item.getFila());
            if (hasText(grupoFamiliarBd)) {
                item.agregarObservacionGrupoFamiliar("COINCIDENCIA_APELLIDOS_BD", grupoFamiliarBd);
                item.agregarMensaje(grupoFamiliarBd);
            }

            boolean requiereDecisionAsignacionNumero =
                    ProcedimientoRegistralRules.requiereDecisionAsignacionParaNumero(item.getTipoProcedimiento());
            if (requiereDecisionAsignacionNumero) {
                item.agregarMensaje(ProcedimientoRegistralRules.mensajeSinNumeroRecepcion());
            }

            if (item.isPosibleDuplicado() || requiereDecisionAsignacionNumero) {
                item.setNumeroExpedienteGenerado(null);
            } else {
                item.setNumeroExpedienteGenerado(correlativoExpedienteService.generarPreliminar(siguienteCorrelativo++));
            }
            item.setListoParaRegistrar(true);
            if (item.isPosibleDuplicado()) {
                item.setEstadoValidacion("Duplicado");
            } else if (requiereDecisionAsignacionNumero) {
                item.setEstadoValidacion("Con observaciones");
            } else if (error) {
                item.setEstadoValidacion("Con observaciones");
            } else {
                item.setEstadoValidacion("Válido");
                item.setMensajeValidacion("Listo para registrar.");
            }
        }

        return registros;
    }

    private List<String> validarIdentidad(CargaDiariaPreviewDTO item) {
        List<String> errores = new ArrayList<>();
        validarDocumento(
                errores,
                "solicitante",
                item.getTipoDocumentoIdentidadSolicitante(),
                item.getNumeroDocumentoIdentidadSolicitante(),
                true);
        validarDocumento(
                errores,
                "titular",
                item.getTipoDocumentoIdentidadTitular(),
                item.getNumeroDocumentoIdentidadTitular(),
                false);
        return errores;
    }

    private void validarDocumento(List<String> errores, String etiqueta, String tipo, String numero, boolean permiteRuc) {
        if (!hasText(tipo) && !hasText(numero)) {
            return;
        }
        if ("SIN DNI".equalsIgnoreCase(numero == null ? "" : numero.trim())) {
            return;
        }
        if (!hasText(tipo)) {
            errores.add("Tipo de documento de identidad del " + etiqueta + " obligatorio si informa número.");
            return;
        }
        String tipoNormalizado = tipo.trim().toUpperCase(Locale.ROOT);
        if ("SIN DNI".equals(tipoNormalizado)) {
            if (hasText(numero) && !"SIN DNI".equalsIgnoreCase(numero.trim())) {
                errores.add("Si el tipo de documento del " + etiqueta + " es SIN DNI, el número debe quedar vacío.");
            }
            return;
        }
        if ("RUC".equals(tipoNormalizado) && !permiteRuc) {
            errores.add("El titular no permite RUC como tipo de documento de identidad.");
            return;
        }
        if (!hasText(numero)) {
            errores.add("Número de documento de identidad del " + etiqueta + " obligatorio para tipo " + tipoNormalizado + ".");
            return;
        }
        String numeroNormalizado = numero.trim().toUpperCase(Locale.ROOT);
        if ("DNI".equals(tipoNormalizado)) {
            if (!numeroNormalizado.matches("\\d{8}")) {
                errores.add("DNI del " + etiqueta + " debe tener 8 caracteres numéricos.");
            }
            return;
        }
        if ("RUC".equals(tipoNormalizado)) {
            if (!numeroNormalizado.matches("\\d{11}")) {
                errores.add("RUC del " + etiqueta + " debe tener 11 caracteres numéricos.");
            }
            return;
        }
        if ("CE".equals(tipoNormalizado) || "PASAPORTE".equals(tipoNormalizado)) {
            if (!numeroNormalizado.matches("[A-Z0-9]{1,12}")) {
                errores.add(tipoNormalizado + " del " + etiqueta + " debe ser alfanumérico de hasta 12 caracteres.");
            }
            return;
        }
        errores.add("Tipo de documento de identidad del " + etiqueta + " no reconocido: " + tipo + ".");
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

    private String claveApellidosTitular(CargaDiariaPreviewDTO item) {
        if (item == null) {
            return null;
        }
        return grupoFamiliarHeuristicaService.claveApellidosTitular(item.getTitular());
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
