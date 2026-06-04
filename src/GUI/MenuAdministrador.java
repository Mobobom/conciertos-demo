package GUI;

import BLL.Concierto;
import BLL.ConciertoService;
import BLL.Sector;
import BLL.SectorService;
import BLL.Ticket;
import BLL.TicketService;
import BLL.Usuario;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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

public class MenuAdministrador extends JFrame {

    private final Usuario usuario;
    private final ConciertoService conciertoService;
    private final SectorService sectorService;
    private final TicketService ticketService;

    public MenuAdministrador(Usuario usuario) {
        this.usuario = usuario;
        this.conciertoService = new ConciertoService();
        this.sectorService = new SectorService();
        this.ticketService = new TicketService();
        initialize();
    }

    private void initialize() {
        setTitle("Menu Administrador");
        setSize(820, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildButtons(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JLabel title = new JLabel("Panel de Administrador", SwingConstants.CENTER);
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
        addButton(panel, "Listar todos los conciertos", e -> mostrarTodosLosConciertos());
        addButton(panel, "Crear concierto", e -> crearConcierto());
        addButton(panel, "Modificar concierto", e -> modificarConcierto());
        addButton(panel, "Cancelar concierto", e -> cancelarConcierto());
        addButton(panel, "Ver disponibilidad", e -> verDisponibilidadConcierto());
        addButton(panel, "Ver sectores", e -> verSectoresDeConcierto());
        addButton(panel, "Crear sector", e -> crearSector());
        addButton(panel, "Crear tickets de sector", e -> crearTicketsDeSector());
        addButton(panel, "Ver tickets", e -> verTicketsDeConcierto());
        addButton(panel, "Bloquear ticket", e -> bloquearTicket());
        addButton(panel, "Liberar ticket", e -> liberarTicket());
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

    private void mostrarTodosLosConciertos() {
        try {
            mostrarTablaConciertos("Todos los conciertos", conciertoService.listarTodos());
        } catch (SQLException e) {
            mostrarError("No se pudieron listar los conciertos", e);
        }
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

    private void cancelarConcierto() {
        try {
            int id = pedirEntero("ID del concierto a cancelar", null);
            boolean cancelado = conciertoService.cancelarConcierto(id);
            mostrarInfo("Cancelar concierto", cancelado
                    ? "El concierto fue cancelado."
                    : "No se encontro el concierto indicado.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo cancelar el concierto", e);
        }
    }

    private void verDisponibilidadConcierto() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }

            StringBuilder message = new StringBuilder();
            message.append("Concierto: ").append(concierto.getArtista()).append('\n');
            message.append("Disponibles: ").append(concierto.getDisponibles()).append('\n');
            message.append("Capacidad total: ").append(concierto.getCapacidadTotal()).append('\n');
            message.append("Estado: ").append(concierto.getEstado());
            mostrarInfo("Disponibilidad", message.toString());
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo consultar la disponibilidad", e);
        }
    }

    private void verSectoresDeConcierto() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }

            LinkedList<Sector> sectores = sectorService.listarPorConcierto(concierto.getId());
            String[] columns = {"ID", "Tipo", "Nombre", "Capacidad", "Precio", "Disponibles"};
            Object[][] rows = new Object[sectores.size()][columns.length];
            for (int i = 0; i < sectores.size(); i++) {
                Sector sector = sectores.get(i);
                rows[i][0] = sector.getId();
                rows[i][1] = sector.getTipo();
                rows[i][2] = sector.getNombre();
                rows[i][3] = sector.getCapacidad();
                rows[i][4] = sector.getPrecio();
                rows[i][5] = sector.getDisponibles();
            }
            mostrarTabla("Sectores del concierto " + concierto.getArtista(), columns, rows);
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudieron listar los sectores", e);
        }
    }

    private void crearSector() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }
            String tipo = pedirTexto("Tipo de sector (ej. PLATEA, VIP)", "PLATEA");
            String nombre = pedirTexto("Nombre del sector", "Principal");
            int capacidad = pedirEntero("Capacidad del sector", "10");
            String precioStr = pedirTexto("Precio (ej. 100.00)", "100.00");
            BigDecimal precio;
            try {
                precio = new BigDecimal(precioStr.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Precio invalido.");
            }

            int id = sectorService.crearSector(concierto.getId(), tipo, nombre, capacidad, precio);
            mostrarInfo("Sector creado", "Se creo el sector con ID " + id + ".");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo crear el sector", e);
        }
    }

    private void crearTicketsDeSector() {
        try {
            Sector sector = seleccionarSector();
            if (sector == null) {
                return;
            }
            int creados = ticketService.generarTicketsParaSector(sector.getId());
            mostrarInfo("Tickets creados", "Se crearon " + creados + " tickets para el sector.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudieron crear los tickets", e);
        }
    }

    private Sector seleccionarSector() throws SQLException {
        LinkedList<Sector> sectores = sectorService.listarTodos();
        if (sectores.isEmpty()) {
            mostrarInfo("Sin sectores", "No hay sectores registrados. Cree un sector primero.");
            return null;
        }

        Map<Integer, Concierto> conciertosPorId = cargarConciertosPorId();
        Map<String, Sector> sectoresPorEtiqueta = new HashMap<>();
        String[] opciones = new String[sectores.size()];
        for (int i = 0; i < sectores.size(); i++) {
            Sector sector = sectores.get(i);
            Concierto concierto = conciertosPorId.get(sector.getConciertoId());
            String artista = concierto == null ? "Concierto " + sector.getConciertoId() : concierto.getArtista();
            String fechaHora = concierto == null ? "" : " - " + concierto.getFecha() + " " + concierto.getHora();
            String etiqueta = String.format("%s%s - %s - %s - capacidad: %d - disponibles: %d",
                    artista, fechaHora, sector.getTipo(), sector.getNombre(),
                    sector.getCapacidad(), sector.getDisponibles());
            opciones[i] = etiqueta;
            sectoresPorEtiqueta.put(etiqueta, sector);
        }

        String seleccionado = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione un sector:",
                "Sectores disponibles",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]);

        return seleccionado == null ? null : sectoresPorEtiqueta.get(seleccionado);
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
            String etiqueta = String.format("%s - %s %s - %s - disponibles: %d",
                    concierto.getArtista(), concierto.getFecha(), concierto.getHora(),
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

    private Ticket seleccionarTicket(Concierto concierto) throws SQLException {
        LinkedList<Ticket> tickets = ticketService.listarPorConcierto(concierto.getId());
        if (tickets.isEmpty()) {
            mostrarInfo("Sin tickets", "No hay tickets creados para el concierto seleccionado.");
            return null;
        }

        LinkedList<Sector> sectores = sectorService.listarPorConcierto(concierto.getId());
        Map<Integer, Sector> sectoresPorId = new HashMap<>();
        for (Sector sector : sectores) {
            sectoresPorId.put(sector.getId(), sector);
        }

        Map<String, Ticket> ticketsPorEtiqueta = new HashMap<>();
        String[] opciones = new String[tickets.size()];
        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);
            Sector sector = sectoresPorId.get(ticket.getSectorId());
            String sectorTexto = sector == null
                    ? "Sector " + ticket.getSectorId()
                    : sector.getTipo() + " - " + sector.getNombre();
            String etiqueta = String.format("%s - %s - %s - %s",
                    concierto.getArtista(), sectorTexto, ticket.getCodigo(), ticket.getEstado());
            opciones[i] = etiqueta;
            ticketsPorEtiqueta.put(etiqueta, ticket);
        }

        String seleccionado = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione un ticket:",
                "Tickets disponibles",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]);

        return seleccionado == null ? null : ticketsPorEtiqueta.get(seleccionado);
    }

    private Map<Integer, Concierto> cargarConciertosPorId() throws SQLException {
        LinkedList<Concierto> conciertos = conciertoService.listarTodos();
        Map<Integer, Concierto> conciertosPorId = new HashMap<>();
        for (Concierto concierto : conciertos) {
            conciertosPorId.put(concierto.getId(), concierto);
        }
        return conciertosPorId;
    }

    private void cerrarSesion() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    private void verTicketsDeConcierto() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }

            LinkedList<Ticket> tickets = ticketService.listarPorConcierto(concierto.getId());
            String[] columns = {"ID", "Concierto", "Sector", "Codigo", "Precio", "Estado", "Compra"};
            Object[][] rows = new Object[tickets.size()][columns.length];
            for (int i = 0; i < tickets.size(); i++) {
                BLL.Ticket ticket = tickets.get(i);
                rows[i][0] = ticket.getId();
                rows[i][1] = ticket.getConciertoId();
                rows[i][2] = ticket.getSectorId();
                rows[i][3] = ticket.getCodigo();
                rows[i][4] = ticket.getPrecio();
                rows[i][5] = ticket.getEstado();
                rows[i][6] = ticket.getCompraId();
            }
            mostrarTabla("Tickets del concierto " + concierto.getArtista(), columns, rows);
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudieron listar los tickets", e);
        }
    }

    private void bloquearTicket() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }

            Ticket ticket = seleccionarTicket(concierto);
            if (ticket == null) {
                return;
            }

            boolean bloqueado = ticketService.bloquearTicket(ticket.getId());
            mostrarInfo("Bloquear ticket", bloqueado
                    ? "El ticket fue bloqueado."
                    : "No se pudo bloquear el ticket.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo bloquear el ticket", e);
        }
    }

    private void liberarTicket() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }

            Ticket ticket = seleccionarTicket(concierto);
            if (ticket == null) {
                return;
            }

            boolean liberado = ticketService.liberarTicket(ticket.getId());
            mostrarInfo("Liberar ticket", liberado
                    ? "El ticket fue liberado."
                    : "No se pudo liberar el ticket.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo liberar el ticket", e);
        }
    }

    private void mostrarTablaConciertos(String titulo, LinkedList<Concierto> conciertos) {
        String[] columns = {"ID", "Artista", "Fecha", "Hora", "Lugar", "Capacidad", "Disponibles", "Estado", "Organizador"};
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
            rows[i][8] = concierto.getOrganizadorId();
        }
        mostrarTabla(titulo, columns, rows);
    }

    private void mostrarTabla(String titulo, String[] columns, Object[][] rows) {
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
        frame.setSize(900, 400);
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