package GUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public final class TablaConBotones {

    private TablaConBotones() {
    }

    public static JFrame mostrar(
            Component parent,
            String titulo,
            String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons) {
        return mostrar(parent, titulo, columns, rowsSupplier, extraButtons, null);
    }

    public static JFrame mostrar(
            Component parent,
            String titulo,
            String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons,
            Busqueda busqueda) {
        return mostrar(parent, titulo, columns, rowsSupplier, extraButtons, busqueda, null);
    }

    public static JFrame mostrar(
            Component parent,
            String titulo,
            String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons,
            Busqueda busqueda,
            DetalleImagen detalleImagen) {
        DefaultTableModel model = new DefaultTableModel(rowsSupplier.get(), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        if (detalleImagen != null) {
            ocultarColumna(table, detalleImagen.columnaImagen);
        }

        TableRowSorter<DefaultTableModel> sorter = null;
        if (busqueda != null && busqueda.tieneColumnas()) {
            sorter = new TableRowSorter<>(model);
            deshabilitarOrdenamiento(sorter, columns.length);
            table.setRowSorter(sorter);
        }

        JFrame frame = new JFrame(titulo);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));

        if (sorter != null) {
            JTextField buscar = new JTextField();
            configurarBusqueda(buscar, sorter, busqueda.columnas);

            JPanel filtros = new JPanel(new BorderLayout(6, 6));
            filtros.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));
            filtros.add(new JLabel(busqueda.etiqueta), BorderLayout.WEST);
            filtros.add(buscar, BorderLayout.CENTER);
            frame.add(filtros, BorderLayout.NORTH);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        if (detalleImagen == null) {
            frame.add(scrollPane, BorderLayout.CENTER);
        } else {
            frame.add(crearPanelConDetalle(table, scrollPane, detalleImagen), BorderLayout.CENTER);
        }

        TableRowSorter<DefaultTableModel> finalSorter = sorter;
        Runnable refrescar = () -> {
            model.setDataVector(rowsSupplier.get(), columns);
            if (detalleImagen != null) {
                ocultarColumna(table, detalleImagen.columnaImagen);
                actualizarDetalleImagen(table, detalleImagen);
            }
            if (finalSorter != null) {
                deshabilitarOrdenamiento(finalSorter, columns.length);
            }
        };

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        if (extraButtons != null) {
            for (JButton button : extraButtons.apply(table, refrescar)) {
                bar.add(button);
            }
        }

        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> refrescar.run());
        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(e -> frame.dispose());
        bar.add(actualizar);
        bar.add(cerrar);

        frame.add(bar, BorderLayout.SOUTH);
        frame.setSize(detalleImagen == null ? 960 : 1120, detalleImagen == null ? 460 : 540);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        if (detalleImagen != null) {
            actualizarDetalleImagen(table, detalleImagen);
        }
        return frame;
    }

    public static Integer idSeleccionado(Component parent, JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(parent, "Seleccione una fila.",
                    "Accion", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Object value = table.getModel().getValueAt(modelRow, 0);
        return Integer.valueOf(value.toString());
    }

    private static JPanel crearPanelConDetalle(JTable table, JScrollPane scrollPane, DetalleImagen detalleImagen) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        EstiloGUI.aplicarPanel(panel);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel detalle = new JPanel(new BorderLayout(6, 6));
        EstiloGUI.aplicarPanel(detalle);
        detalle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloGUI.BORDE),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        detalle.setPreferredSize(new Dimension(detalleImagen.ancho + 32, detalleImagen.alto + 110));

        JLabel imagen = new JLabel();
        imagen.setHorizontalAlignment(SwingConstants.CENTER);
        detalle.add(imagen, BorderLayout.CENTER);

        JTextArea info = new JTextArea();
        EstiloGUI.aplicarAreaTexto(info);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        detalle.add(info, BorderLayout.SOUTH);

        detalleImagen.imagen = imagen;
        detalleImagen.info = info;
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarDetalleImagen(table, detalleImagen);
            }
        });

        panel.add(detalle, BorderLayout.EAST);
        return panel;
    }

    private static void actualizarDetalleImagen(JTable table, DetalleImagen detalleImagen) {
        if (detalleImagen.imagen == null || detalleImagen.info == null) {
            return;
        }
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            detalleImagen.imagen.setIcon(ImagenHelper.cargarImagen(null, detalleImagen.ancho, detalleImagen.alto));
            detalleImagen.info.setText("Seleccione un concierto para ver su poster.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        String ruta = valorTabla(table, modelRow, detalleImagen.columnaImagen);
        detalleImagen.imagen.setIcon(ImagenHelper.cargarImagen(ruta, detalleImagen.ancho, detalleImagen.alto));
        detalleImagen.info.setText(textoDetalle(table, modelRow, detalleImagen));
    }

    private static String textoDetalle(JTable table, int modelRow, DetalleImagen detalleImagen) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < detalleImagen.columnasTexto.length; i++) {
            int columna = detalleImagen.columnasTexto[i];
            String etiqueta = i < detalleImagen.etiquetasTexto.length ? detalleImagen.etiquetasTexto[i] : "";
            if (!etiqueta.isEmpty()) {
                texto.append(etiqueta).append(": ");
            }
            texto.append(valorTabla(table, modelRow, columna)).append('\n');
        }
        return texto.toString().trim();
    }

    private static String valorTabla(JTable table, int modelRow, int column) {
        Object value = table.getModel().getValueAt(modelRow, column);
        return value == null ? "" : value.toString();
    }

    private static void ocultarColumna(JTable table, int columnaModelo) {
        int columnaVista = table.convertColumnIndexToView(columnaModelo);
        if (columnaVista < 0) {
            return;
        }
        TableColumn column = table.getColumnModel().getColumn(columnaVista);
        table.getColumnModel().removeColumn(column);
    }

    private static void configurarBusqueda(JTextField buscar, TableRowSorter<DefaultTableModel> sorter, int... columnas) {
        buscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            private void aplicarFiltro() {
                String texto = buscar.getText().trim();
                if (texto.isEmpty()) {
                    sorter.setRowFilter(null);
                    return;
                }
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto), columnas));
            }
        });
    }

    private static void deshabilitarOrdenamiento(TableRowSorter<DefaultTableModel> sorter, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sorter.setSortable(i, false);
        }
    }

    public static final class Busqueda {
        private final String etiqueta;
        private final int[] columnas;

        public Busqueda(String etiqueta, int... columnas) {
            this.etiqueta = etiqueta;
            this.columnas = columnas;
        }

        private boolean tieneColumnas() {
            return etiqueta != null && columnas != null && columnas.length > 0;
        }
    }

    public static final class DetalleImagen {
        private final int columnaImagen;
        private final int ancho;
        private final int alto;
        private final int[] columnasTexto;
        private final String[] etiquetasTexto;
        private JLabel imagen;
        private JTextArea info;

        public DetalleImagen(int columnaImagen, int ancho, int alto,
                             int[] columnasTexto, String[] etiquetasTexto) {
            this.columnaImagen = columnaImagen;
            this.ancho = ancho;
            this.alto = alto;
            this.columnasTexto = columnasTexto == null ? new int[0] : columnasTexto;
            this.etiquetasTexto = etiquetasTexto == null ? new String[0] : etiquetasTexto;
        }
    }
}
