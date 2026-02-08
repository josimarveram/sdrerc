/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model.ExpedienteAnalisAbogadoDetDoc;

import java.util.Date;

/**
 *
 * @author betom
 */
public class ExpedienteAnalisisAbogadoDetResponse 
{
    private Integer idTipoDocumento;
    private String tipoDocumento;
    private String descripcionDocumento;

    public ExpedienteAnalisisAbogadoDetResponse(Integer idTipoDocumento,
                                String tipoDocumento,
                                String descripcionDocumento
                                ) {
        this.idTipoDocumento = idTipoDocumento;
        this.tipoDocumento = tipoDocumento;
        this.descripcionDocumento = descripcionDocumento;
    }
    public Integer getIdTipoDocumento() {
        return idTipoDocumento;
    }
    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getDescripcionDocumento() {
        return descripcionDocumento;
    }  
}
