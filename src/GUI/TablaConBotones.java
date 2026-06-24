package GUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
        return mostrar(parent, titulo, columns, rowsSupplier, extraButtons, busqueda, null);
    }

    public static JFrame mostrar(
            Component parent,
            String titulo,
            String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons,
            Busqueda busqueda,
            FiltrosConcierto filtrosConcierto) {
        DefaultTableModel model = new DefaultTableModel(rowsSupplier.get(), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        TableRowSorter<DefaultTableModel> sorter = null;
        if ((busqueda != null && busqueda.tieneColumnas()) || filtrosConcierto != null) {
            sorter = new TableRowSorter<>(model);
            deshabilitarOrdenamiento(sorter, columns.length);
            table.setRowSorter(sorter);
        }

        JFrame frame = new JFrame(titulo);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));

        FiltrosTabla filtrosTabla = null;
        if (sorter != null) {
            JTextField buscar = new JTextField();
            filtrosTabla = new FiltrosTabla(sorter, buscar, busqueda, filtrosConcierto);
            configurarFiltros(filtrosTabla);
            FiltrosTabla filtrosTablaActual = filtrosTabla;

            JPanel filtros = new JPanel(new GridBagLayout());
            filtros.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));
            agregarCampoFiltro(filtros, 0, new JLabel(busqueda == null ? "Buscar:" : busqueda.etiqueta), buscar);
            if (filtrosConcierto != null) {
                agregarCampoFiltro(filtros, 1, new JLabel("Fecha desde:"), filtrosTabla.fechaDesde);
                agregarCampoFiltro(filtros, 2, new JLabel("Fecha hasta:"), filtrosTabla.fechaHasta);
                agregarCampoFiltro(filtros, 3, new JLabel("Lugar:"), filtrosTabla.lugar);
                if (filtrosConcierto.tieneColumnaEstado()) {
                    agregarCampoFiltro(filtros, 4, new JLabel("Estado:"), filtrosTabla.estado);
                }
                JButton limpiarFiltros = new JButton("Resetear filtros");
                limpiarFiltros.addActionListener(e -> filtrosTablaActual.limpiar());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = filtrosConcierto.tieneColumnaEstado() ? 5 : 4;
                gbc.gridwidth = 2;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(2, 2, 2, 2);
                filtros.add(limpiarFiltros, gbc);
            }
            frame.add(filtros, BorderLayout.NORTH);
        }

        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        TableRowSorter<DefaultTableModel> finalSorter = sorter;
        FiltrosTabla finalFiltrosTabla = filtrosTabla;
        Runnable refrescar = () -> {
            model.setDataVector(rowsSupplier.get(), columns);
            if (finalSorter != null) {
                deshabilitarOrdenamiento(finalSorter, columns.length);
                if (finalFiltrosTabla != null) {
                    finalFiltrosTabla.aplicar();
                }
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

    private static void configurarFiltros(FiltrosTabla filtrosTabla) {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrosTabla.aplicar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrosTabla.aplicar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrosTabla.aplicar();
            }
        };

        filtrosTabla.buscar.getDocument().addDocumentListener(listener);
        if (filtrosTabla.fechaDesde != null) {
            filtrosTabla.fechaDesde.getDocument().addDocumentListener(listener);
            filtrosTabla.fechaHasta.getDocument().addDocumentListener(listener);
            filtrosTabla.lugar.getDocument().addDocumentListener(listener);
        }
        if (filtrosTabla.estado != null) {
            filtrosTabla.estado.addActionListener(e -> filtrosTabla.aplicar());
        }
    }

    private static void agregarCampoFiltro(JPanel panel, int fila, JLabel etiqueta, Component campo) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = fila;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(2, 2, 2, 6);
        panel.add(etiqueta, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = fila;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(2, 2, 2, 2);
        panel.add(campo, fieldConstraints);
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

    public static final class FiltrosConcierto {
        private final int columnaFecha;
        private final int columnaLugar;
        private final int columnaEstado;

        public FiltrosConcierto(int columnaFecha, int columnaLugar, int columnaEstado) {
            this.columnaFecha = columnaFecha;
            this.columnaLugar = columnaLugar;
            this.columnaEstado = columnaEstado;
        }

        private boolean tieneColumnaEstado() {
            return columnaEstado >= 0;
        }
    }

    private static final class FiltrosTabla {
        private final TableRowSorter<DefaultTableModel> sorter;
        private final JTextField buscar;
        private final Busqueda busqueda;
        private final FiltrosConcierto filtrosConcierto;
        private final JTextField fechaDesde;
        private final JTextField fechaHasta;
        private final JTextField lugar;
        private final JComboBox<String> estado;

        private FiltrosTabla(TableRowSorter<DefaultTableModel> sorter, JTextField buscar,
                Busqueda busqueda, FiltrosConcierto filtrosConcierto) {
            this.sorter = sorter;
            this.buscar = buscar;
            this.busqueda = busqueda;
            this.filtrosConcierto = filtrosConcierto;
            this.fechaDesde = filtrosConcierto == null ? null : new JTextField();
            this.fechaHasta = filtrosConcierto == null ? null : new JTextField();
            this.lugar = filtrosConcierto == null ? null : new JTextField();
            this.estado = filtrosConcierto != null && filtrosConcierto.tieneColumnaEstado()
                    ? new JComboBox<>(new String[] {"Todos", "Activo", "Cancelado"})
                    : null;
        }

        private void aplicar() {
            List<RowFilter<DefaultTableModel, Integer>> filtros = new ArrayList<>();
            agregarBusqueda(filtros);
            agregarFiltrosConcierto(filtros);

            if (filtros.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.andFilter(filtros));
            }
        }

        private void limpiar() {
            buscar.setText("");
            if (fechaDesde != null) {
                fechaDesde.setText("");
                fechaHasta.setText("");
                lugar.setText("");
            }
            if (estado != null) {
                estado.setSelectedIndex(0);
            }
            aplicar();
        }

        private void agregarBusqueda(List<RowFilter<DefaultTableModel, Integer>> filtros) {
            if (busqueda == null || !busqueda.tieneColumnas()) {
                return;
            }
            String texto = buscar.getText().trim();
            if (!texto.isEmpty()) {
                filtros.add(RowFilter.regexFilter("(?i)" + Pattern.quote(texto), busqueda.columnas));
            }
        }

        private void agregarFiltrosConcierto(List<RowFilter<DefaultTableModel, Integer>> filtros) {
            if (filtrosConcierto == null) {
                return;
            }
            LocalDate desde = parseFecha(fechaDesde.getText());
            LocalDate hasta = parseFecha(fechaHasta.getText());
            String textoLugar = lugar.getText().trim().toLowerCase();
            String estadoSeleccionado = estado == null ? "Todos" : estado.getSelectedItem().toString();

            filtros.add(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    LocalDate fecha = parseFecha(valor(entry, filtrosConcierto.columnaFecha));
                    if (fecha == null && (desde != null || hasta != null)) {
                        return false;
                    }
                    if (desde != null && fecha.isBefore(desde)) {
                        return false;
                    }
                    if (hasta != null && fecha.isAfter(hasta)) {
                        return false;
                    }
                    if (!textoLugar.isEmpty()
                            && !valor(entry, filtrosConcierto.columnaLugar).toLowerCase().contains(textoLugar)) {
                        return false;
                    }
                    if (filtrosConcierto.tieneColumnaEstado() && !"Todos".equals(estadoSeleccionado)
                            && !estadoSeleccionado.equalsIgnoreCase(valor(entry, filtrosConcierto.columnaEstado))) {
                        return false;
                    }
                    return true;
                }
            });
        }

        private String valor(RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry, int columna) {
            Object valor = entry.getValue(columna);
            return valor == null ? "" : valor.toString();
        }

        private LocalDate parseFecha(String texto) {
            String valor = texto == null ? "" : texto.trim();
            if (valor.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(valor);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }
}
