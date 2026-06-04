package GUI;

import BLL.Concierto;
import BLL.ConciertoService;
import BLL.Sector;
import BLL.SectorService;
import BLL.Ticket;
import BLL.TicketService;
import BLL.Usuario;
import BLL.UsuarioService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
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
    private final UsuarioService usuarioService;
    private final TicketService ticketService;

    public MenuAdministrador(Usuario usuario) {
        this.usuario = usuario;
        this.conciertoService = new ConciertoService();
        this.sectorService = new SectorService();
        this.usuarioService = new UsuarioService();
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
        mostrarTablaConciertos("Conciertos activos", () -> conciertoService.listarActivos());
    }

    private void mostrarTodosLosConciertos() {
        mostrarTablaConciertos("Todos los conciertos", () -> {
            try {
                return conciertoService.listarTodos();
            } catch (SQLException e) {
                mostrarError("No se pudieron listar los conciertos", e);
                return new LinkedList<Concierto>();
            }
        });
    }

    private void crearConcierto() {
        try {
            String artista = pedirTexto("Artista", "Ingrese el nombre del artista");
            LocalDate fecha = pedirFecha("Fecha (yyyy-MM-dd)", "2026-06-15");
            LocalTime hora = pedirHora("Hora (HH:mm)", "21:00");
            String lugar = pedirTexto("Lugar", "Ingrese el lugar del concierto");
            int capacidadTotal = pedirEntero("Capacidad total", "60");
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

            String artista = pedirTexto("Artista", concierto.getArtista());
            LocalDate fecha = pedirFecha("Fecha (yyyy-MM-dd)", concierto.getFecha().toString());
            LocalTime hora = pedirHora("Hora (HH:mm)", concierto.getHora().toString());
            String lugar = pedirTexto("Lugar", concierto.getLugar());
            int capacidadTotal = pedirEntero("Capacidad total", String.valueOf(concierto.getCapacidadTotal()));
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

            String artista = pedirTexto("Artista", concierto.getArtista());
            LocalDate fecha = pedirFecha("Fecha (yyyy-MM-dd)", concierto.getFecha().toString());
            LocalTime hora = pedirHora("Hora (HH:mm)", concierto.getHora().toString());
            String lugar = pedirTexto("Lugar", concierto.getLugar());
            int capacidadTotal = pedirEntero("Capacidad total", String.valueOf(concierto.getCapacidadTotal()));
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

    private void cancelarConcierto() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }

            boolean cancelado = conciertoService.cancelarConcierto(concierto.getId());
            mostrarInfo("Cancelar concierto", cancelado
                    ? "El concierto fue cancelado."
                    : "No se encontro el concierto indicado.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo cancelar el concierto", e);
        }
    }

    private void cancelarConcierto(int conciertoId) {
        try {
            boolean cancelado = conciertoService.cancelarConcierto(conciertoId);
            mostrarInfo("Cancelar concierto", cancelado
                    ? "El concierto fue cancelado."
                    : "No se encontro el concierto indicado.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo cancelar el concierto", e);
        }
    }

    private boolean puedeModificarConcierto(Concierto concierto) {
        if ("Cancelado".equals(concierto.getEstado())) {
            mostrarInfo("Concierto cancelado", "No se puede modificar un concierto cancelado.");
            return false;
        }
        return true;
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
            String tipo = seleccionarTipoSector();
            if (tipo == null) {
                return;
            }
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

    private void crearSector(int conciertoId) {
        try {
            String tipo = seleccionarTipoSector();
            if (tipo == null) {
                return;
            }
            String nombre = pedirTexto("Nombre del sector", "Principal");
            int capacidad = pedirEntero("Capacidad del sector", "10");
            String precioStr = pedirTexto("Precio (ej. 100.00)", "100.00");
            BigDecimal precio;
            try {
                precio = new BigDecimal(precioStr.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Precio invalido.");
            }

            int id = sectorService.crearSector(conciertoId, tipo, nombre, capacidad, precio);
            mostrarInfo("Sector creado", "Se creo el sector con ID " + id + ".");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo crear el sector", e);
        }
    }

    private String seleccionarTipoSector() {
        return seleccionarTipoSector("VIP");
    }

    private String seleccionarTipoSector(String seleccionInicial) {
        String[] tipos = {"VIP", "Platea", "Campo", "Preferencial"};
        return (String) JOptionPane.showInputDialog(
                this,
                "Seleccione el tipo de sector:",
                "Tipo de sector",
                JOptionPane.PLAIN_MESSAGE,
                null,
                tipos,
                seleccionInicial);
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

    private void modificarSector(int sectorId) {
        try {
            Sector sector = sectorService.buscarPorId(sectorId);
            if (sector == null) {
                mostrarInfo("Sin sector", "No se encontro el sector indicado.");
                return;
            }

            String tipo = seleccionarTipoSector(sector.getTipo());
            if (tipo == null) {
                return;
            }
            String nombre = pedirTexto("Nombre del sector", sector.getNombre());
            int capacidad = pedirEntero("Capacidad del sector", String.valueOf(sector.getCapacidad()));
            String precioStr = pedirTexto("Precio (ej. 100.00)", sector.getPrecio().toString());
            BigDecimal precio;
            try {
                precio = new BigDecimal(precioStr.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Precio invalido.");
            }

            sector.setTipo(tipo);
            sector.setNombre(nombre);
            sector.setCapacidad(capacidad);
            sector.setPrecio(precio);

            boolean modificado = sectorService.modificarSector(sector);
            mostrarInfo("Modificar sector", modificado
                    ? "El sector fue modificado."
                    : "No se pudo modificar el sector.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo modificar el sector", e);
        }
    }

    private void eliminarSector(int sectorId) {
        try {
            int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "Desea eliminar el sector seleccionado?",
                    "Eliminar sector",
                    JOptionPane.YES_NO_OPTION);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }

            boolean eliminado = sectorService.eliminarSector(sectorId);
            mostrarInfo("Eliminar sector", eliminado
                    ? "El sector fue eliminado."
                    : "No se encontro el sector indicado.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo eliminar el sector", e);
        }
    }

    private void generarTicketsDeSector(int sectorId) {
        try {
            int creados = ticketService.generarTicketsParaSector(sectorId);
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
            String etiqueta = String.format("%s - %s - %s - capacidad: %d - disponibles: %d",
                artista, sector.getTipo(), sector.getNombre(),
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

    private void bloquearTicket(int ticketId) {
        try {
            boolean bloqueado = ticketService.bloquearTicket(ticketId);
            mostrarInfo("Bloquear ticket", bloqueado
                    ? "El ticket fue bloqueado."
                    : "No se pudo bloquear el ticket.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo bloquear el ticket", e);
        }
    }

    private void liberarTicket(int ticketId) {
        try {
            boolean liberado = ticketService.liberarTicket(ticketId);
            mostrarInfo("Liberar ticket", liberado
                    ? "El ticket fue liberado."
                    : "No se pudo liberar el ticket.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo liberar el ticket", e);
        }
    }

    private void mostrarTabla(String titulo, String[] columns, Object[][] rows) {
        mostrarTablaConBotones(titulo, columns, () -> rows, null);
    }

    private void mostrarTablaConciertos(String titulo, Supplier<LinkedList<Concierto>> fetcher) {
        String[] columns = {"ID", "Artista", "Fecha", "Hora", "Lugar", "Capacidad", "Disponibles", "Estado", "Organizador"};
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
                data[i][8] = concierto.getOrganizadorId();
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
            JButton cancelar = new JButton("Cancelar");
            cancelar.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { cancelarConcierto(id); refrescar.run(); }
            });
            JButton verSectores = new JButton("Ver sectores");
            verSectores.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { verSectoresDeConcierto(id); }
            });
            return Arrays.asList(crear, editar, cancelar, verSectores);
        });
    }

    private void verSectoresDeConcierto(int conciertoId) {
        String[] columns = {"ID", "Tipo", "Nombre", "Capacidad", "Precio", "Disponibles"};
        Supplier<Object[][]> rows = () -> {
            try {
                LinkedList<Sector> sectores = sectorService.listarPorConcierto(conciertoId);
                Object[][] data = new Object[sectores.size()][columns.length];
                for (int i = 0; i < sectores.size(); i++) {
                    Sector sector = sectores.get(i);
                    data[i][0] = sector.getId();
                    data[i][1] = sector.getTipo();
                    data[i][2] = sector.getNombre();
                    data[i][3] = sector.getCapacidad();
                    data[i][4] = sector.getPrecio();
                    data[i][5] = sector.getDisponibles();
                }
                return data;
            } catch (SQLException e) {
                mostrarError("No se pudieron listar los sectores", e);
                return new Object[0][columns.length];
            }
        };
        mostrarTablaConBotones("Sectores del concierto " + conciertoId, columns, rows, (table, refrescar) -> {
            JButton crear = new JButton("Crear sector");
            crear.addActionListener(e -> { crearSector(conciertoId); refrescar.run(); });
            JButton editar = new JButton("Editar");
            editar.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { modificarSector(id); refrescar.run(); }
            });
            JButton eliminar = new JButton("Eliminar");
            eliminar.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { eliminarSector(id); refrescar.run(); }
            });
            JButton generar = new JButton("Generar tickets");
            generar.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { generarTicketsDeSector(id); refrescar.run(); }
            });
            JButton verTickets = new JButton("Ver tickets");
            verTickets.addActionListener(e -> verTicketsDeConcierto(conciertoId));
            return Arrays.asList(crear, editar, eliminar, generar, verTickets);
        });
    }

    private void verTicketsDeConcierto(int conciertoId) {
        String[] columns = {"ID", "Concierto", "Sector", "Codigo", "Precio", "Estado", "Compra"};
        Supplier<Object[][]> rows = () -> {
            try {
                LinkedList<Ticket> tickets = ticketService.listarPorConcierto(conciertoId);
                Object[][] data = new Object[tickets.size()][columns.length];
                for (int i = 0; i < tickets.size(); i++) {
                    Ticket ticket = tickets.get(i);
                    data[i][0] = ticket.getId();
                    data[i][1] = ticket.getConciertoId();
                    data[i][2] = ticket.getSectorId();
                    data[i][3] = ticket.getCodigo();
                    data[i][4] = ticket.getPrecio();
                    data[i][5] = ticket.getEstado();
                    data[i][6] = ticket.getCompraId();
                }
                return data;
            } catch (SQLException e) {
                mostrarError("No se pudieron listar los tickets", e);
                return new Object[0][columns.length];
            }
        };
        mostrarTablaConBotones("Tickets del concierto " + conciertoId, columns, rows, (table, refrescar) -> {
            JButton bloquear = new JButton("Bloquear");
            bloquear.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { bloquearTicket(id); refrescar.run(); }
            });
            JButton liberar = new JButton("Liberar");
            liberar.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { liberarTicket(id); refrescar.run(); }
            });
            return Arrays.asList(bloquear, liberar);
        });
    }

    private void mostrarTablaConBotones(String titulo, String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons) {
        DefaultTableModel model = new DefaultTableModel(rowsSupplier.get(), columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JFrame frame = new JFrame(titulo);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));
        frame.add(scrollPane, BorderLayout.CENTER);

        Runnable refrescar = () -> model.setDataVector(rowsSupplier.get(), columns);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        if (extraButtons != null) {
            for (JButton button : extraButtons.apply(table, refrescar)) {
                bar.add(button);
            }
        }

        JButton subir = new JButton("Subir");
        subir.addActionListener(e -> moverFila(table, -1));
        JButton bajar = new JButton("Bajar");
        bajar.addActionListener(e -> moverFila(table, 1));
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> refrescar.run());
        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(e -> frame.dispose());
        bar.add(subir);
        bar.add(bajar);
        bar.add(actualizar);
        bar.add(cerrar);

        frame.add(bar, BorderLayout.SOUTH);
        frame.setSize(960, 460);
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    private void moverFila(JTable table, int delta) {
        int row = table.getSelectedRow();
        if (row < 0) {
            mostrarInfo("Mover fila", "Seleccione una fila.");
            return;
        }
        int target = row + delta;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        if (target < 0 || target >= model.getRowCount()) {
            return;
        }
        model.moveRow(row, row, target);
        table.setRowSelectionInterval(target, target);
    }

    private Integer idSeleccionado(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            mostrarInfo("Accion", "Seleccione una fila.");
            return null;
        }
        Object value = table.getModel().getValueAt(row, 0);
        return Integer.valueOf(value.toString());
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
