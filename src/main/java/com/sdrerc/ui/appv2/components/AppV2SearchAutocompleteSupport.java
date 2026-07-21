package com.sdrerc.ui.appv2.components;

import com.sdrerc.ui.appv2.theme.AppV2Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Autocompletado tipo "buscar mientras escribe" para un {@link JTextField}: dispara una
 * busqueda asincrona con debounce, muestra los resultados en un popup flotante y notifica
 * la seleccion de una unica opcion.
 */
public final class AppV2SearchAutocompleteSupport {

    private AppV2SearchAutocompleteSupport() {
    }

    public interface Buscador<T> {
        List<T> buscar(String texto) throws Exception;
    }

    public static <T> void attach(
            JTextField campo,
            int minCaracteres,
            Buscador<T> buscador,
            Function<T, String> textoOpcion,
            Consumer<T> alSeleccionar) {
        new Controlador<T>(campo, minCaracteres, buscador, textoOpcion, alSeleccionar);
    }

    private static final class Controlador<T> {

        private final JTextField campo;
        private final int minCaracteres;
        private final Buscador<T> buscador;
        private final Function<T, String> textoOpcion;
        private final Consumer<T> alSeleccionar;
        private final DefaultListModel<T> modelo = new DefaultListModel<>();
        private final JList<T> lista = new JList<>(modelo);
        private final JWindow popup;
        private final Timer debounce;
        private long secuencia;
        private boolean seleccionando;

        private Controlador(
                JTextField campo,
                int minCaracteres,
                Buscador<T> buscador,
                Function<T, String> textoOpcion,
                Consumer<T> alSeleccionar) {
            this.campo = campo;
            this.minCaracteres = minCaracteres;
            this.buscador = buscador;
            this.textoOpcion = textoOpcion;
            this.alSeleccionar = alSeleccionar;

            Window ventana = SwingUtilities.getWindowAncestor(campo);
            popup = new JWindow(ventana);
            popup.setFocusableWindowState(false);
            lista.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
                javax.swing.JLabel label = new javax.swing.JLabel(textoOpcion.apply(value));
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                label.setFont(AppV2Theme.fontPlain(AppV2Theme.FONT_SIZE_BASE));
                label.setBackground(isSelected ? new java.awt.Color(207, 229, 244) : AppV2Theme.SURFACE);
                label.setForeground(AppV2Theme.TEXT_PRIMARY);
                return label;
            });
            JScrollPane scroll = new JScrollPane(lista);
            scroll.setBorder(BorderFactory.createLineBorder(AppV2Theme.BORDER));
            popup.getContentPane().setLayout(new BorderLayout());
            popup.getContentPane().add(scroll, BorderLayout.CENTER);

            debounce = new Timer(300, e -> ejecutarBusqueda());
            debounce.setRepeats(false);

            campo.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    programarBusqueda();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    programarBusqueda();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    programarBusqueda();
                }
            });
            campo.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        ocultarPopup();
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN && popup.isVisible()) {
                        lista.requestFocusInWindow();
                        if (lista.getModel().getSize() > 0) {
                            lista.setSelectedIndex(0);
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER && modelo.size() == 1) {
                        seleccionar(modelo.get(0));
                    }
                }
            });
            campo.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (seleccionando) {
                        return;
                    }
                    java.awt.Component opuesto = e.getOppositeComponent();
                    if (opuesto == lista || (opuesto != null && SwingUtilities.isDescendingFrom(opuesto, popup))) {
                        return;
                    }
                    ocultarPopup();
                }
            });
            lista.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        T seleccionado = lista.getSelectedValue();
                        if (seleccionado != null) {
                            seleccionar(seleccionado);
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        ocultarPopup();
                        campo.requestFocusInWindow();
                    }
                }
            });
            lista.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int index = lista.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        seleccionar(modelo.get(index));
                    }
                }
            });
        }

        private void programarBusqueda() {
            debounce.restart();
        }

        private void ejecutarBusqueda() {
            String texto = campo.getText() == null ? "" : campo.getText().trim();
            if (texto.length() < minCaracteres) {
                ocultarPopup();
                return;
            }
            final long solicitud = ++secuencia;
            SwingWorker<List<T>, Void> worker = new SwingWorker<List<T>, Void>() {
                @Override
                protected List<T> doInBackground() throws Exception {
                    return buscador.buscar(texto);
                }

                @Override
                protected void done() {
                    if (solicitud != secuencia) {
                        return;
                    }
                    List<T> resultados;
                    try {
                        resultados = get();
                    } catch (Exception ex) {
                        resultados = java.util.Collections.emptyList();
                    }
                    modelo.clear();
                    for (T item : resultados) {
                        modelo.addElement(item);
                    }
                    if (modelo.isEmpty()) {
                        ocultarPopup();
                    } else {
                        mostrarPopup();
                    }
                }
            };
            worker.execute();
        }

        private void mostrarPopup() {
            if (!campo.isShowing()) {
                return;
            }
            int filas = Math.min(modelo.size(), 6);
            int alturaFila = 26;
            lista.setVisibleRowCount(filas);
            Point ubicacion = campo.getLocationOnScreen();
            popup.setLocation(ubicacion.x, ubicacion.y + campo.getHeight());
            popup.setSize(new Dimension(Math.max(campo.getWidth(), 260), filas * alturaFila + 8));
            popup.setVisible(true);
        }

        private void ocultarPopup() {
            popup.setVisible(false);
        }

        private void seleccionar(T item) {
            seleccionando = true;
            try {
                alSeleccionar.accept(item);
            } finally {
                modelo.clear();
                campo.setText("");
                ocultarPopup();
                seleccionando = false;
            }
        }
    }
}
