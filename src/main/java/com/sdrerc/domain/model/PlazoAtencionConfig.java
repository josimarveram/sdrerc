package com.sdrerc.domain.model;

public class PlazoAtencionConfig {
    private final int idTipoDocumento;
    private final int diasPlazo;
    private final int porcentajeVerdeDesde;
    private final int porcentajeAmarilloDesde;
    private final int porcentajeRojoDesde;

    public PlazoAtencionConfig(int idTipoDocumento, int diasPlazo, int porcentajeVerdeDesde, int porcentajeAmarilloDesde, int porcentajeRojoDesde) {
        this.idTipoDocumento = idTipoDocumento;
        this.diasPlazo = diasPlazo;
        this.porcentajeVerdeDesde = porcentajeVerdeDesde;
        this.porcentajeAmarilloDesde = porcentajeAmarilloDesde;
        this.porcentajeRojoDesde = porcentajeRojoDesde;
    }

    public int getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public int getDiasPlazo() {
        return diasPlazo;
    }

    public int getPorcentajeVerdeDesde() {
        return porcentajeVerdeDesde;
    }

    public int getPorcentajeAmarilloDesde() {
        return porcentajeAmarilloDesde;
    }

    public int getPorcentajeRojoDesde() {
        return porcentajeRojoDesde;
    }
}
