/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

/**
 *
 * @author usuario
 */
public class Enumerado 
{
    private Enumerado() 
    {
        // Evita instanciación
    }  

    // ======================
    // ESTADO EXPEDIENTE
    // ======================
    public enum EstadoExpediente 
    {

        RegistroExpediente(56),
        ExpedienteAsignado(57),
        ExpedienteRecibido(58),
        ExpedienteAtendido(59),
        ExpedienteVerificado(60),
        ExpedienteEjecucionAsignada(88);

        private final int id;

        EstadoExpediente(int id) 
        {
            this.id = id;
        }

        public int getId() 
        {
            return id;
        }

        public static EstadoExpediente fromId(int id) {
            for (EstadoExpediente e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            throw new IllegalArgumentException("EstadoExpediente no válido: " + id);
        }
    }

    // ======================
    // TIPO SOLICITUD
    // ======================
    public enum TipoSolicitud {

        CARTA(1),
        OFICIO(2),
        MEMO(3);

        private final int id;

        TipoSolicitud(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static TipoSolicitud fromId(int id) {
            for (TipoSolicitud t : values()) {
                if (t.id == id) {
                    return t;
                }
            }
            throw new IllegalArgumentException("TipoSolicitud no válido: " + id);
        }
    }

}
