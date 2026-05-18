package com.sdrerc.application;

import com.sdrerc.domain.model.PlazoAtencionConfig;
import com.sdrerc.domain.model.PlazoAtencionResultado;
import com.sdrerc.infrastructure.repository.PlazoAtencionRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class PlazoAtencionService {
    private final PlazoAtencionRepository repository;
    private Map<Integer, PlazoAtencionConfig> configs;

    public PlazoAtencionService()
    {
        this.repository = new PlazoAtencionRepository();
    }

    public PlazoAtencionResultado calcular(int idTipoDocumento, Date fechaSolicitud)
    {
        if (idTipoDocumento <= 0) {
            return PlazoAtencionResultado.sinConfig();
        }
        if (fechaSolicitud == null) {
            return PlazoAtencionResultado.sinFecha();
        }

        PlazoAtencionConfig config = obtenerConfigs().get(idTipoDocumento);
        if (config == null || config.getDiasPlazo() <= 0) {
            return PlazoAtencionResultado.sinConfig();
        }

        LocalDate fechaInicio = toLocalDate(fechaSolicitud);
        long diasTranscurridos = ChronoUnit.DAYS.between(fechaInicio, LocalDate.now());
        int diasRestantes = config.getDiasPlazo() - Math.toIntExact(diasTranscurridos);
        return PlazoAtencionResultado.of(diasRestantes, config.getDiasPlazo(), resolverNivel(diasRestantes, config));
    }

    private LocalDate toLocalDate(Date fecha)
    {
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private PlazoAtencionResultado.Nivel resolverNivel(int diasRestantes, PlazoAtencionConfig config)
    {
        if (diasRestantes < 0) {
            return PlazoAtencionResultado.Nivel.VENCIDO;
        }
        int porcentajeRestante = Math.round((diasRestantes * 100f) / config.getDiasPlazo());
        if (porcentajeRestante >= config.getPorcentajeVerdeDesde()) {
            return PlazoAtencionResultado.Nivel.VERDE;
        }
        if (porcentajeRestante >= config.getPorcentajeAmarilloDesde()) {
            return PlazoAtencionResultado.Nivel.AMARILLO;
        }
        if (porcentajeRestante >= config.getPorcentajeRojoDesde()) {
            return PlazoAtencionResultado.Nivel.ROJO;
        }
        return PlazoAtencionResultado.Nivel.ROJO;
    }

    private Map<Integer, PlazoAtencionConfig> obtenerConfigs()
    {
        if (configs != null) {
            return configs;
        }
        try {
            configs = repository.listarConfiguracionesActivas();
        } catch (SQLException ex) {
            configs = Collections.emptyMap();
        }
        return configs;
    }
}
