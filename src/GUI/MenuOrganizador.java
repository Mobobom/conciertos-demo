package GUI;

import BLL.Concierto;
import BLL.ConciertoService;
import BLL.Sector;
import BLL.SectorService;
import BLL.Usuario;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class MenuOrganizador extends JFrame {

    private final Usuario usuario;
    private final ConciertoService conciertoService;
    private final SectorService sectorService;

    public MenuOrganizador(Usuario usuario) {
        this.usuario = usuario;
        this.conciertoService = new ConciertoService();
        this.sectorService = new SectorService();
        initialize();
    }

    private void initialize() {
        setTitle("Menu Organizador");
        setSize(780, 480);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildButtons(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JLabel title = new JLabel("Panel de Organizador", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(18f));
        panel.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel(
                usuario.getNombre() + " " + usuario.getApellido() + " | " + usuario.getEmail(),
                SwingConstants.CENTER);
        panel.add(subtitle, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        addButton(panel, "Listar conciertos activos", e -> mostrarConciertosActivos());
        addButton(panel, "Crear concierto", e -> crearConcierto());
        addButton(panel, "Modificar concierto", e -> modificarConcierto());
        addButton(panel, "Ver informacion del evento", e -> verInformacionEvento());
        addButton(panel, "Cerrar sesion", e -> cerrarSesion());
        addButton(panel, "Cerrar menu", e -> dispose());

        return panel;
    }

    private void addButton(JPanel panel, String label, java.awt.event.ActionListener action) {
        JButton button = new JButton(label);
        button.addActionListener(action);
        panel.add(button);
    }

    private void mostrarConciertosActivos() {
        mostrarTablaConciertos("Conciertos activos", conciertoService.listarActivos());
    }

    private void crearConcierto() {
        try {
            String artista = pedirTexto("Artista", "Ingrese el nombre del artista");
            LocalDate fecha = pedirFecha("Fecha (yyyy-MM-dd)", "2026-06-15");
            LocalTime hora = pedirHora("Hora (HH:mm)", "21:00");
            String lugar = pedirTexto("Lugar", "Ingrese el lugar del concierto");
            int capacidadTotal = pedirEntero("Capacidad total", "60");
            int organizadorId = pedirEnteroOpcional("ID del organizador", String.valueOf(usuario.getId()));

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
            int id = pedirEntero("ID del concierto", null);
            Concierto concierto = conciertoService.buscarPorId(id);
            if (concierto == null) {
                mostrarInfo("Concierto no encontrado", "No existe un concierto con ID " + id + ".");
                return;
            }

            String artista = pedirTexto("Artista", concierto.getArtista());
            LocalDate fecha = pedirFecha("Fecha (yyyy-MM-dd)", concierto.getFecha().toString());
            LocalTime hora = pedirHora("Hora (HH:mm)", concierto.getHora().toString());
            String lugar = pedirTexto("Lugar", concierto.getLugar());
            int capacidadTotal = pedirEntero("Capacidad total", String.valueOf(concierto.getCapacidadTotal()));
            int organizadorId = pedirEnteroOpcional("ID del organizador", String.valueOf(concierto.getOrganizadorId()));

            concierto.setArtista(artista);
            concierto.setFecha(fecha);
            concierto.setHora(hora);
            concierto.setLugar(lugar);
            concierto.setCapacidadTotal(capacidadTotal);
            concierto.setOrganizadorId(organizadorId);

            if (conciertoService.modificarConcierto(concierto)) {
                mostrarInfo("Concierto modificado", "Se actualizo el concierto ID " + id + ".");
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
            int id = pedirEntero("ID del concierto", null);
            Concierto concierto = conciertoService.buscarPorId(id);
            if (concierto == null) {
                mostrarInfo("Concierto no encontrado", "No existe un concierto con ID " + id + ".");
                return;
            }

            LinkedList<Sector> sectores = sectorService.listarPorConcierto(id);
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

    private void cerrarSesion() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    private void mostrarTablaConciertos(String titulo, LinkedList<Concierto> conciertos) {
        String[] columns = {"ID", "Artista", "Fecha", "Hora", "Lugar", "Capacidad", "Disponibles", "Estado"};
        Object[][] rows = new Object[conciertos.size()][columns.length];
        for (int i = 0; i < conciertos.size(); i++) {
            Concierto concierto = conciertos.get(i);
            rows[i][0] = concierto.getId();
            rows[i][1] = concierto.getArtista();
            rows[i][2] = concierto.getFecha();
            rows[i][3] = concierto.getHora();
            rows[i][4] = concierto.getLugar();
            rows[i][5] = concierto.getCapacidadTotal();
            rows[i][6] = concierto.getDisponibles();
            rows[i][7] = concierto.getEstado();
        }

        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JFrame frame = new JFrame(titulo);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(scrollPane);
        frame.setSize(820, 380);
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    private String pedirTexto(String campo, String valorInicial) {
        String valor = (String) JOptionPane.showInputDialog(this,
                "Ingrese " + campo,
                campo,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                valorInicial);
        if (valor == null) {
            throw new IllegalArgumentException("Operacion cancelada.");
        }
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    private int pedirEntero(String campo, String valorInicial) {
        String valor = (String) JOptionPane.showInputDialog(this,
                "Ingrese " + campo,
                campo,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                valorInicial);
        if (valor == null) {
            throw new IllegalArgumentException("Operacion cancelada.");
        }
        return Integer.parseInt(valor.trim());
    }

    private int pedirEnteroOpcional(String campo, String valorInicial) {
        String valor = (String) JOptionPane.showInputDialog(this,
                "Ingrese " + campo + " (dejar en 0 si no aplica)",
                campo,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                valorInicial);
        if (valor == null) {
            throw new IllegalArgumentException("Operacion cancelada.");
        }
        String trimmed = valor.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(trimmed);
    }

    private LocalDate pedirFecha(String campo, String valorInicial) {
        String valor = pedirTextoConDefault(campo, valorInicial);
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha invalido. Use yyyy-MM-dd.");
        }
    }

    private LocalTime pedirHora(String campo, String valorInicial) {
        String valor = pedirTextoConDefault(campo, valorInicial);
        try {
            return LocalTime.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de hora invalido. Use HH:mm.");
        }
    }

    private String pedirTextoConDefault(String campo, String valorInicial) {
        String valor = (String) JOptionPane.showInputDialog(this,
                "Ingrese " + campo,
                campo,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                valorInicial);
        if (valor == null) {
            throw new IllegalArgumentException("Operacion cancelada.");
        }
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String titulo, Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), titulo, JOptionPane.ERROR_MESSAGE);
    }
}