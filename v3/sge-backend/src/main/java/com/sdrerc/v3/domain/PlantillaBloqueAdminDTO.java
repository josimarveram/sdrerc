package com.sdrerc.v3.domain;

/** Port literal de {@code com.sdrerc.domain.dto.sdrercapp.PlantillaBloqueDTO} (V2). */
public class PlantillaBloqueAdminDTO {

    public static final String OPERADOR_COINCIDE = "COINCIDE";
    public static final String OPERADOR_NO_COINCIDE = "NO_COINCIDE";

    private Long idPlantillaBloque;
    private Long idTipoDocumentoAdjunto;
    private int orden;
    private String seccion;
    private String titulo;
    private String contenido;
    private String variableCondicion;
    private String operadorCondicion;
    private String valoresCondicion;
    private boolean activo = true;

    public Long getIdPlantillaBloque() {
        return idPlantillaBloque;
    }

    public void setIdPlantillaBloque(Long idPlantillaBloque) {
        this.idPlantillaBloque = idPlantillaBloque;
    }

    public Long getIdTipoDocumentoAdjunto() {
        return idTipoDocumentoAdjunto;
    }

    public void setIdTipoDocumentoAdjunto(Long idTipoDocumentoAdjunto) {
        this.idTipoDocumentoAdjunto = idTipoDocumentoAdjunto;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    /** Nombre de sección (marcador {@code [[CONTENIDO:seccion]]}); null/vacío = marcador sin nombre {@code [[CONTENIDO]]}. */
    public String getSeccion() {
        return seccion;
    }

    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }

    public boolean tieneSeccion() {
        return seccion != null && !seccion.trim().isEmpty();
    }

    public boolean tieneCondicion() {
        return variableCondicion != null && !variableCondicion.trim().isEmpty();
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getVariableCondicion() {
        return variableCondicion;
    }

    public void setVariableCondicion(String variableCondicion) {
        this.variableCondicion = variableCondicion;
    }

    public String getOperadorCondicion() {
        return operadorCondicion;
    }

    public void setOperadorCondicion(String operadorCondicion) {
        this.operadorCondicion = operadorCondicion;
    }

    public String getValoresCondicion() {
        return valoresCondicion;
    }

    public void setValoresCondicion(String valoresCondicion) {
        this.valoresCondicion = valoresCondicion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
