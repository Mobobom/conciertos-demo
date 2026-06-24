package GUI;

import BLL.Concierto;
import BLL.ConciertoService;
import BLL.Sector;
import BLL.SectorService;
import BLL.Usuario;
import BLL.UsuarioService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class MenuOrganizador extends MenuBase {

    private final ConciertoService conciertoService;
    private final SectorService sectorService;
    private final UsuarioService usuarioService;
    private final Map<String, JFrame> openTableFrames;

    public MenuOrganizador(Usuario usuario) {
        super(usuario);
        this.conciertoService = new ConciertoService();
        this.sectorService = new SectorService();
        this.usuarioService = new UsuarioService();
        this.openTableFrames = new HashMap<>();
        inicializarMenu("Menu Organizador");
    }

    @Override
    protected String getTituloPanel() {
        return "Panel de Organizador";
    }

    @Override
    protected JComponent crearContenido() {
        JPanel panel = new JPanel(new BorderLayout());
        EstiloGUI.aplicarPanelContenido(panel);

        JPanel grid = new JPanel(new GridLayout(0, 2, EstiloGUI.ESPACIADO, EstiloGUI.ESPACIADO));
        EstiloGUI.aplicarPanel(grid);

        addButton(grid, "Listar conciertos activos", "search", e -> mostrarConciertosActivos());
        addButton(grid, "Crear concierto", "add", e -> crearConcierto());
        addButton(grid, "Modificar concierto", "edit", e -> modificarConcierto());
        addButton(grid, "Ver informacion del evento", "report", e -> verInformacionEvento());
        addButton(grid, "Cambiar password", "edit", e -> PasswordDialogs.cambiarPassword(this, usuario));
        grid.add(crearBotonVolverLogin());
        grid.add(crearBotonCerrarSistema());
        panel.add(grid, BorderLayout.NORTH);

        return panel;
    }

    @Override
    public void dispose() {
        cerrarTablasAbiertas();
        super.dispose();
    }

    private void mostrarConciertosActivos() {
        mostrarTablaConciertos("Conciertos activos", () -> {
            try {
                return conciertoService.listarActivos();
            } catch (SQLException e) {
                mostrarError("No se pudieron listar los conciertos", e);
                return new LinkedList<Concierto>();
            }
        });
    }

    private void crearConcierto() {
        try {
            String artista = DialogosUtil.pedirTexto(this, "Artista", "Ingrese el nombre del artista");
            LocalDate fecha = DialogosUtil.pedirFecha(this, "Fecha (yyyy-MM-dd)", "2026-06-15");
            LocalTime hora = DialogosUtil.pedirHora(this, "Hora (HH:mm)", "21:00");
            String lugar = DialogosUtil.pedirTexto(this, "Lugar", "Ingrese el lugar del concierto");
            int capacidadTotal = DialogosUtil.pedirEntero(this, "Capacidad total", "60");
            Integer organizadorId = seleccionarOrganizador();
            if (organizadorId == null) {
                return;
            }

            int id = conciertoService.crearConcierto(artista, fecha, hora, lugar, capacidadTotal, organizadorId);
            mostrarInfo("Concierto creado", "Se creo el concierto con ID " + id + ".");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo crear el concierto", e);
        }
    }

    private void modificarConcierto() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }
            if (!puedeModificarConcierto(concierto)) {
                return;
            }

            String artista = DialogosUtil.pedirTexto(this, "Artista", concierto.getArtista());
            LocalDate fecha = DialogosUtil.pedirFecha(this, "Fecha (yyyy-MM-dd)", concierto.getFecha().toString());
            LocalTime hora = DialogosUtil.pedirHora(this, "Hora (HH:mm)", concierto.getHora().toString());
            String lugar = DialogosUtil.pedirTexto(this, "Lugar", concierto.getLugar());
            int capacidadTotal = DialogosUtil.pedirEntero(this, "Capacidad total", String.valueOf(concierto.getCapacidadTotal()));
            Integer organizadorId = seleccionarOrganizador(concierto.getOrganizadorId());
            if (organizadorId == null) {
                return;
            }

            concierto.setArtista(artista);
            concierto.setFecha(fecha);
            concierto.setHora(hora);
            concierto.setLugar(lugar);
            concierto.setCapacidadTotal(capacidadTotal);
            concierto.setOrganizadorId(organizadorId);

            if (conciertoService.modificarConcierto(concierto)) {
                mostrarInfo("Concierto modificado", "Se actualizo el concierto " + concierto.getArtista() + ".");
            } else {
                mostrarInfo("Sin cambios", "No se pudo modificar el concierto.");
            }
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo modificar el concierto", e);
        }
    }

    private void modificarConcierto(int conciertoId) {
        try {
            Concierto concierto = conciertoService.buscarPorId(conciertoId);
            if (concierto == null) {
                mostrarInfo("Sin concierto", "No se encontro el concierto indicado.");
                return;
            }
            if (!puedeModificarConcierto(concierto)) {
                return;
            }

            String artista = DialogosUtil.pedirTexto(this, "Artista", concierto.getArtista());
            LocalDate fecha = DialogosUtil.pedirFecha(this, "Fecha (yyyy-MM-dd)", concierto.getFecha().toString());
            LocalTime hora = DialogosUtil.pedirHora(this, "Hora (HH:mm)", concierto.getHora().toString());
            String lugar = DialogosUtil.pedirTexto(this, "Lugar", concierto.getLugar());
            int capacidadTotal = DialogosUtil.pedirEntero(this, "Capacidad total", String.valueOf(concierto.getCapacidadTotal()));
            Integer organizadorId = seleccionarOrganizador(concierto.getOrganizadorId());
            if (organizadorId == null) {
                return;
            }

            concierto.setArtista(artista);
            concierto.setFecha(fecha);
            concierto.setHora(hora);
            concierto.setLugar(lugar);
            concierto.setCapacidadTotal(capacidadTotal);
            concierto.setOrganizadorId(organizadorId);

            if (conciertoService.modificarConcierto(concierto)) {
                mostrarInfo("Concierto modificado", "Se actualizo el concierto " + concierto.getArtista() + ".");
            } else {
                mostrarInfo("Sin cambios", "No se pudo modificar el concierto.");
            }
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo modificar el concierto", e);
        }
    }

    private void verInformacionEvento() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }

            LinkedList<Sector> sectores = sectorService.listarPorConcierto(concierto.getId());
            StringBuilder message = new StringBuilder();
            message.append("Concierto: ").append(concierto.getArtista()).append('\n');
            message.append("Fecha: ").append(concierto.getFecha()).append('\n');
            message.append("Hora: ").append(concierto.getHora()).append('\n');
            message.append("Lugar: ").append(concierto.getLugar()).append('\n');
            message.append("Estado: ").append(concierto.getEstado()).append('\n');
            message.append("Disponibles: ").append(concierto.getDisponibles()).append('\n');
            message.append('\n').append("Sectores:").append('\n');
            for (Sector sector : sectores) {
                message.append("- ")
                        .append(sector.getTipo())
                        .append(" / ")
                        .append(sector.getNombre())
                        .append(" / Capacidad: ")
                        .append(sector.getCapacidad())
                        .append(" / Disponibles: ")
                        .append(sector.getDisponibles())
                        .append('\n');
            }

            mostrarInfo("Informacion del evento", message.toString());
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo consultar el evento", e);
        }
    }

    private boolean puedeModificarConcierto(Concierto concierto) {
        if ("Cancelado".equals(concierto.getEstado())) {
            mostrarInfo("Concierto cancelado", "No se puede modificar un concierto cancelado.");
            return false;
        }
        return true;
    }

    private void verInformacionEvento(int conciertoId) {
        try {
            Concierto concierto = conciertoService.buscarPorId(conciertoId);
            if (concierto == null) {
                mostrarInfo("Sin concierto", "No se encontro el concierto indicado.");
                return;
            }

            LinkedList<Sector> sectores = sectorService.listarPorConcierto(concierto.getId());
            StringBuilder message = new StringBuilder();
            message.append("Concierto: ").append(concierto.getArtista()).append('\n');
            message.append("Fecha: ").append(concierto.getFecha()).append('\n');
            message.append("Hora: ").append(concierto.getHora()).append('\n');
            message.append("Lugar: ").append(concierto.getLugar()).append('\n');
            message.append("Estado: ").append(concierto.getEstado()).append('\n');
            message.append("Disponibles: ").append(concierto.getDisponibles()).append('\n');
            message.append('\n').append("Sectores:").append('\n');
            for (Sector sector : sectores) {
                message.append("- ")
                        .append(sector.getTipo())
                        .append(" / ")
                        .append(sector.getNombre())
                        .append(" / Capacidad: ")
                        .append(sector.getCapacidad())
                        .append(" / Disponibles: ")
                        .append(sector.getDisponibles())
                        .append('\n');
            }

            mostrarInfo("Informacion del evento", message.toString());
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo consultar el evento", e);
        }
    }

    private Concierto seleccionarConcierto() throws SQLException {
        LinkedList<Concierto> conciertos = conciertoService.listarTodos();
        if (conciertos.isEmpty()) {
            mostrarInfo("Sin conciertos", "No hay conciertos registrados.");
            return null;
        }

        Map<String, Concierto> conciertosPorEtiqueta = new HashMap<>();
        String[] opciones = new String[conciertos.size()];
        for (int i = 0; i < conciertos.size(); i++) {
            Concierto concierto = conciertos.get(i);
            String etiqueta = String.format("#%d - %s - %s %s - %s - disponibles: %d",
                    concierto.getId(), concierto.getArtista(), concierto.getFecha(), concierto.getHora(),
                    concierto.getLugar(), concierto.getDisponibles());
            opciones[i] = etiqueta;
            conciertosPorEtiqueta.put(etiqueta, concierto);
        }

        String seleccionado = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione un concierto:",
                "Conciertos disponibles",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]);

        return seleccionado == null ? null : conciertosPorEtiqueta.get(seleccionado);
    }

    private Integer seleccionarOrganizador() throws SQLException {
        return seleccionarOrganizador(0);
    }

    private Integer seleccionarOrganizador(int organizadorActualId) throws SQLException {
        LinkedList<Usuario> usuarios = usuarioService.listarUsuarios();
        Map<String, Integer> opcionesPorEtiqueta = new HashMap<>();
        LinkedList<String> opciones = new LinkedList<>();

        String sinOrganizador = "0 - Organizador indefinido";
        opciones.add(sinOrganizador);
        opcionesPorEtiqueta.put(sinOrganizador, 0);

        String seleccionInicial = sinOrganizador;
        for (Usuario usuarioListado : usuarios) {
            if (!"Organizador".equals(usuarioListado.getRol())) {
                continue;
            }
            String etiqueta = String.format("%d - %s %s",
                    usuarioListado.getId(),
                    usuarioListado.getNombre(),
                    usuarioListado.getApellido());
            opciones.add(etiqueta);
            opcionesPorEtiqueta.put(etiqueta, usuarioListado.getId());
            if (usuarioListado.getId() == organizadorActualId) {
                seleccionInicial = etiqueta;
            }
        }

        String seleccionado = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione un organizador:",
                "Organizadores disponibles",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones.toArray(new String[0]),
                seleccionInicial);

        return seleccionado == null ? null : opcionesPorEtiqueta.get(seleccionado);
    }

    private void cerrarTablasAbiertas() {
        for (JFrame frame : openTableFrames.values().toArray(new JFrame[0])) {
            if (frame != null && frame.isDisplayable()) {
                frame.dispose();
            }
        }
        openTableFrames.clear();
    }

    private void mostrarTablaConBotones(String titulo, String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons) {
        JFrame openFrame = openTableFrames.get(titulo);
        if (openFrame != null) {
            if (openFrame.isDisplayable()) {
                openFrame.setState(Frame.NORMAL);
                openFrame.toFront();
                openFrame.requestFocus();
                return;
            }
            openTableFrames.remove(titulo);
        }

        DefaultTableModel model = new DefaultTableModel(rowsSupplier.get(), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        ocultarColumna(table, 8);
        final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        deshabilitarOrdenamiento(sorter, columns.length);
        table.setRowSorter(sorter);
        JScrollPane scrollPane = new JScrollPane(table);

        JFrame frame = new JFrame(titulo);
        openTableFrames.put(titulo, frame);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (openTableFrames.get(titulo) == frame) {
                    openTableFrames.remove(titulo);
                }
            }
        });
        frame.setLayout(new BorderLayout(5, 5));
        JTextField buscar = new JTextField();
        configurarBusqueda(buscar, sorter);

        JPanel filtros = new JPanel(new BorderLayout(6, 6));
        filtros.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));
        filtros.add(new JLabel("Buscar por artista o lugar:"), BorderLayout.WEST);
        filtros.add(buscar, BorderLayout.CENTER);
        frame.add(filtros, BorderLayout.NORTH);
        frame.add(crearPanelDetallePoster(table, scrollPane), BorderLayout.CENTER);

        Runnable refrescar = () -> {
            model.setDataVector(rowsSupplier.get(), columns);
            ocultarColumna(table, 8);
            deshabilitarOrdenamiento(sorter, columns.length);
            actualizarDetallePoster(table);
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
        frame.setSize(1040, 520);
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
        actualizarDetallePoster(table);
    }

    private void configurarBusqueda(JTextField buscar, TableRowSorter<DefaultTableModel> sorter) {
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
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 1, 4));
            }
        });
    }

    private void deshabilitarOrdenamiento(TableRowSorter<DefaultTableModel> sorter, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sorter.setSortable(i, false);
        }
    }


    private Integer idSeleccionado(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            mostrarInfo("Accion", "Seleccione una fila.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Object value = table.getModel().getValueAt(modelRow, 0);
        return Integer.valueOf(value.toString());
    }

    private JPanel crearPanelDetallePoster(JTable table, JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        EstiloGUI.aplicarPanel(panel);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel detalle = new JPanel(new BorderLayout(6, 6));
        EstiloGUI.aplicarPanel(detalle);
        detalle.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstiloGUI.BORDE),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        detalle.setPreferredSize(new Dimension(252, 410));

        JLabel poster = new JLabel();
        poster.setName("poster");
        poster.setHorizontalAlignment(SwingConstants.CENTER);
        detalle.add(poster, BorderLayout.CENTER);

        JTextArea info = new JTextArea();
        info.setName("posterInfo");
        EstiloGUI.aplicarAreaTexto(info);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        detalle.add(info, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarDetallePoster(table);
            }
        });

        panel.add(detalle, BorderLayout.EAST);
        return panel;
    }

    private void actualizarDetallePoster(JTable table) {
        JLabel poster = buscarComponente(table.getTopLevelAncestor(), "poster", JLabel.class);
        JTextArea info = buscarComponente(table.getTopLevelAncestor(), "posterInfo", JTextArea.class);
        if (poster == null || info == null) {
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            poster.setIcon(ImagenHelper.cargarImagen(null, 220, 300));
            info.setText("Seleccione un concierto para ver su poster.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        poster.setIcon(ImagenHelper.cargarImagen(valorTabla(table, modelRow, 8), 220, 300));
        info.setText("Artista: " + valorTabla(table, modelRow, 1)
                + "\nFecha: " + valorTabla(table, modelRow, 2)
                + "\nLugar: " + valorTabla(table, modelRow, 4)
                + "\nEstado: " + valorTabla(table, modelRow, 7));
    }

    private String valorTabla(JTable table, int modelRow, int column) {
        Object value = table.getModel().getValueAt(modelRow, column);
        return value == null ? "" : value.toString();
    }

    private void ocultarColumna(JTable table, int columnaModelo) {
        int columnaVista = table.convertColumnIndexToView(columnaModelo);
        if (columnaVista < 0) {
            return;
        }
        TableColumn column = table.getColumnModel().getColumn(columnaVista);
        table.getColumnModel().removeColumn(column);
    }

    private <T extends Component> T buscarComponente(Component raiz, String nombre, Class<T> tipo) {
        if (raiz == null) {
            return null;
        }
        if (tipo.isInstance(raiz) && nombre.equals(raiz.getName())) {
            return tipo.cast(raiz);
        }
        if (raiz instanceof java.awt.Container) {
            Component[] hijos = ((java.awt.Container) raiz).getComponents();
            for (Component hijo : hijos) {
                T encontrado = buscarComponente(hijo, nombre, tipo);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private void mostrarTablaConciertos(String titulo, Supplier<LinkedList<Concierto>> fetcher) {
        String[] columns = {"ID", "Artista", "Fecha", "Hora", "Lugar", "Capacidad", "Disponibles", "Estado", "Poster"};
        Supplier<Object[][]> rows = () -> {
            LinkedList<Concierto> conciertos = fetcher.get();
            Object[][] data = new Object[conciertos.size()][columns.length];
            for (int i = 0; i < conciertos.size(); i++) {
                Concierto concierto = conciertos.get(i);
                data[i][0] = concierto.getId();
                data[i][1] = concierto.getArtista();
                data[i][2] = concierto.getFecha();
                data[i][3] = concierto.getHora();
                data[i][4] = concierto.getLugar();
                data[i][5] = concierto.getCapacidadTotal();
                data[i][6] = concierto.getDisponibles();
                data[i][7] = concierto.getEstado();
                data[i][8] = concierto.getPosterUrl();
            }
            return data;
        };
        mostrarTablaConBotones(titulo, columns, rows, (table, refrescar) -> {
            JButton crear = new JButton("Crear");
            crear.addActionListener(e -> { crearConcierto(); refrescar.run(); });
            JButton editar = new JButton("Editar");
            editar.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { modificarConcierto(id); refrescar.run(); }
            });
            JButton verInfo = new JButton("Ver informacion");
            verInfo.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { verInformacionEvento(id); }
            });
            return Arrays.asList(crear, editar, verInfo);
        });
    }

    private void mostrarInfo(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String titulo, Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), titulo, JOptionPane.ERROR_MESSAGE);
    }
}
