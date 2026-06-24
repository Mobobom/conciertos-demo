package GUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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
        return mostrarCompleta(parent, titulo, columns, rowsSupplier, extraButtons, null, null, null);
    }

    public static JFrame mostrar(
            Component parent,
            String titulo,
            String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons,
            Busqueda busqueda) {
        return mostrarCompleta(parent, titulo, columns, rowsSupplier, extraButtons, busqueda, null, null);
    }

    public static JFrame mostrar(
            Component parent,
            String titulo,
            String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons,
            Busqueda busqueda,
            DetalleImagen detalleImagen) {
        return mostrarCompleta(parent, titulo, columns, rowsSupplier, extraButtons, busqueda, detalleImagen, null);
    }

    public static JFrame mostrar(
            Component parent,
            String titulo,
            String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons,
            Busqueda busqueda,
            FiltrosConcierto filtrosConcierto) {
        return mostrarCompleta(parent, titulo, columns, rowsSupplier, extraButtons, busqueda, null, filtrosConcierto);
    }

    public static JFrame mostrar(
            Component parent,
            String titulo,
            String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons,
            Busqueda busqueda,
            DetalleImagen detalleImagen,
            FiltrosConcierto filtrosConcierto) {
        return mostrarCompleta(parent, titulo, columns, rowsSupplier, extraButtons,
                busqueda, detalleImagen, filtrosConcierto);
    }

    private static JFrame mostrarCompleta(
            Component parent,
            String titulo,
            String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons,
            Busqueda busqueda,
            DetalleImagen detalleImagen,
            FiltrosConcierto filtrosConcierto) {
        DefaultTableModel model = new DefaultTableModel(rowsSupplier.get(), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                for (int row = 0; row < getRowCount(); row++) {
                    Object value = getValueAt(row, columnIndex);
                    if (value != null) {
                        return value.getClass();
                    }
                }
                return Object.class;
            }
        };

        JTable table = new JTable(model);
        ajustarAltoSiTieneImagenes(table);
        if (detalleImagen != null) {
            ocultarColumna(table, detalleImagen.columnaImagen);
        }

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JFrame frame = new JFrame(titulo);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));

        FiltrosTabla filtrosTabla = null;
        if ((busqueda != null && busqueda.tieneColumnas()) || filtrosConcierto != null) {
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

        JScrollPane scrollPane = new JScrollPane(table);
        if (detalleImagen == null) {
            frame.add(scrollPane, BorderLayout.CENTER);
        } else {
            frame.add(crearPanelConDetalle(table, scrollPane, detalleImagen), BorderLayout.CENTER);
        }

        FiltrosTabla finalFiltrosTabla = filtrosTabla;
        Runnable refrescar = () -> {
            model.setDataVector(rowsSupplier.get(), columns);
            ajustarAltoSiTieneImagenes(table);
            if (detalleImagen != null) {
                ocultarColumna(table, detalleImagen.columnaImagen);
                actualizarDetalleImagen(table, detalleImagen);
            }
            if (finalFiltrosTabla != null) {
                finalFiltrosTabla.aplicar();
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

    private static void ajustarAltoSiTieneImagenes(JTable table) {
        for (int column = 0; column < table.getModel().getColumnCount(); column++) {
            if (javax.swing.ImageIcon.class.isAssignableFrom(table.getModel().getColumnClass(column))) {
                table.setRowHeight(56);
                return;
            }
        }
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
