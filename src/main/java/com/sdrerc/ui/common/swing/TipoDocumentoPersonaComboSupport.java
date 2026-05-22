package com.sdrerc.ui.common.swing;

import com.sdrerc.application.CatalogoItemService;
import com.sdrerc.domain.model.CatalogoItem;
import java.awt.Dimension;
import java.util.List;
import javax.swing.JComboBox;

public final class TipoDocumentoPersonaComboSupport {
    public static final int ID_CATALOGO_TIPO_DOCUMENTO_PERSONA = 17;

    private TipoDocumentoPersonaComboSupport() {
    }

    public static void cargar(CatalogoItemService service, JComboBox<CatalogoItem> combo) {
        combo.removeAllItems();
        List<CatalogoItem> lista = service.listarCatalogoItem(ID_CATALOGO_TIPO_DOCUMENTO_PERSONA);
        boolean tieneNa = false;
        for (CatalogoItem item : lista) {
            combo.addItem(item);
            if ("NA".equals(normalizar(item))) {
                tieneNa = true;
            }
        }
        if (!tieneNa) {
            combo.addItem(new CatalogoItem(0, ID_CATALOGO_TIPO_DOCUMENTO_PERSONA, "NA", 1));
        }
        combo.setPreferredSize(new Dimension(150, 38));
        combo.setEnabled(false);
    }

    public static void seleccionarInferido(JComboBox<CatalogoItem> combo, String numeroDocumento) {
        seleccionar(combo, inferir(numeroDocumento));
    }

    public static void seleccionar(JComboBox<CatalogoItem> combo, String descripcion) {
        String buscado = descripcion == null ? "" : descripcion.trim().toUpperCase();
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (buscado.equals(normalizar(combo.getItemAt(i)))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(combo.getItemCount() > 0 ? 0 : -1);
    }

    private static String inferir(String numeroDocumento) {
        String valor = numeroDocumento == null ? "" : numeroDocumento.trim();
        if (valor.isEmpty()) {
            return "NA";
        }
        if (valor.matches("\\d{8}")) {
            return "DNI";
        }
        if (valor.matches("\\d{11}")) {
            return "RUC";
        }
        return "PASAPORTE";
    }

    private static String normalizar(CatalogoItem item) {
        return item == null || item.getDescripcion() == null
                ? ""
                : item.getDescripcion().trim().toUpperCase();
    }
}
