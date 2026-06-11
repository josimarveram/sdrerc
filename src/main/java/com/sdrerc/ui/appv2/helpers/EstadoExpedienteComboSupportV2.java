package com.sdrerc.ui.appv2.helpers;

import com.sdrerc.application.sdrercapp.CatalogoLookupService;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;

public final class EstadoExpedienteComboSupportV2 {

    public interface ItemFactory<T> {
        T create(String codigo, String nombre);
    }

    private EstadoExpedienteComboSupportV2() {
    }

    public static <T> void cargar(
            JComboBox<T> combo,
            String etapaCodigo,
            T opcionTodos,
            ItemFactory<T> itemFactory,
            Consumer<Exception> errorHandler) {
        combo.removeAllItems();
        combo.addItem(opcionTodos);

        SwingWorker<List<CatalogoItemDTO>, Void> worker = new SwingWorker<List<CatalogoItemDTO>, Void>() {
            @Override
            protected List<CatalogoItemDTO> doInBackground() throws Exception {
                return new CatalogoLookupService().listarEstadosExpedientePorEtapa(etapaCodigo);
            }

            @Override
            protected void done() {
                try {
                    List<CatalogoItemDTO> estados = get();
                    combo.removeAllItems();
                    combo.addItem(opcionTodos);
                    for (CatalogoItemDTO estado : estados) {
                        combo.addItem(itemFactory.create(estado.getCodigo(), estado.getNombre()));
                    }
                } catch (Exception ex) {
                    if (errorHandler != null) {
                        errorHandler.accept(ex);
                    }
                }
            }
        };
        worker.execute();
    }

    public static <T> void cargarPorCodigos(
            JComboBox<T> combo,
            T opcionTodos,
            ItemFactory<T> itemFactory,
            Consumer<Exception> errorHandler,
            String... codigosEstado) {
        combo.removeAllItems();
        combo.addItem(opcionTodos);

        SwingWorker<List<CatalogoItemDTO>, Void> worker = new SwingWorker<List<CatalogoItemDTO>, Void>() {
            @Override
            protected List<CatalogoItemDTO> doInBackground() throws Exception {
                return new CatalogoLookupService().listarEstadosExpedientePorCodigos(Arrays.asList(codigosEstado));
            }

            @Override
            protected void done() {
                try {
                    List<CatalogoItemDTO> estados = get();
                    combo.removeAllItems();
                    combo.addItem(opcionTodos);
                    for (CatalogoItemDTO estado : estados) {
                        combo.addItem(itemFactory.create(estado.getCodigo(), estado.getNombre()));
                    }
                } catch (Exception ex) {
                    if (errorHandler != null) {
                        errorHandler.accept(ex);
                    }
                }
            }
        };
        worker.execute();
    }
}
