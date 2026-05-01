package com.sdrerc.shared.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * IDs de catalogo usados por el flujo de expedientes SDRERC.
 *
 * Estos valores reflejan los ID_CATALOGO_ITEM existentes en Oracle y no deben
 * cambiarse sin validar previamente CATALOGO_ITEM y las reglas de bandeja.
 */
public final class FlujoExpedienteConstants {

    private FlujoExpedienteConstants() {
    }

    public static final class EstadoExpediente {
        public static final int REGISTRO_EXPEDIENTE = 56;
        public static final int EXPEDIENTE_ASIGNADO = 57;
        public static final int EXPEDIENTE_RECIBIDO = 58;
        public static final int EXPEDIENTE_ATENDIDO = 59;
        public static final int EXPEDIENTE_VERIFICADO = 87;
        public static final int EJECUCION_ASIGNADA = 88;
        public static final int EJECUCION_TRABAJADA = 89;
        public static final int NOTIFICACION_ASIGNADA = 90;
        public static final int NOTIFICACION_TRABAJADA = 91;

        public static final List<Integer> ESTADOS_FUNCIONALES_EXPEDIENTE = Collections.unmodifiableList(Arrays.asList(
                REGISTRO_EXPEDIENTE,
                EXPEDIENTE_ASIGNADO,
                EXPEDIENTE_RECIBIDO,
                EXPEDIENTE_ATENDIDO,
                EXPEDIENTE_VERIFICADO,
                EJECUCION_ASIGNADA,
                EJECUCION_TRABAJADA,
                NOTIFICACION_ASIGNADA,
                NOTIFICACION_TRABAJADA
        ));

        private EstadoExpediente() {
        }
    }

    public static final class AnalisisAbogado {
        public static final int PROCEDENTE = 73;
        public static final int IMPROCEDENTE = 74;

        private AnalisisAbogado() {
        }
    }

    public static final class DocumentoAnalizado {
        public static final int CARTA_EDICTO = 60;
        public static final int CARTA_FALTA_SUSTENTO = 61;
        // TODO: Confirmar con negocio/BD. El ID 62 no existe actualmente en CATALOGO_ITEM.
        public static final int CARTA_NOTIFICACION_PENDIENTE_CONFIRMAR = 62;
        public static final int CARTA_INDAGATORIO = 63;
        public static final int CARTA_PRETENSION = 64;
        public static final int RESOLUCIONES = 71;
        public static final int INFORMES = 72;

        private DocumentoAnalizado() {
        }
    }

    public static final class EtapaFlujo {
        public static final int EJECUCION_ASIGNADA = EstadoExpediente.EJECUCION_ASIGNADA;
        public static final int NOTIFICACION_ASIGNADA = EstadoExpediente.NOTIFICACION_ASIGNADA;

        private EtapaFlujo() {
        }
    }
}
