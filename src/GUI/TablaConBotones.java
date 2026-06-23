package GUI;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
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
        DefaultTableModel model = new DefaultTableModel(rowsSupplier.get(), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
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

        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        TableRowSorter<DefaultTableModel> finalSorter = sorter;
        Runnable refrescar = () -> {
            model.setDataVector(rowsSupplier.get(), columns);
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
        frame.setSize(960, 460);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
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
}
