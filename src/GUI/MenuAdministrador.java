package GUI;

import BLL.Concierto;
import BLL.ConciertoService;
import BLL.Merchandising;
import BLL.MerchandisingService;
import BLL.Sector;
import BLL.SectorService;
import BLL.Ticket;
import BLL.TicketService;
import BLL.Usuario;
import BLL.UsuarioService;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
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
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class MenuAdministrador extends MenuBase {

    private final ConciertoService conciertoService;
    private final SectorService sectorService;
    private final UsuarioService usuarioService;
    private final TicketService ticketService;
    private final MerchandisingService merchandisingService;
    private final Map<String, JFrame> openTableFrames;

    public MenuAdministrador(Usuario usuario) {
        super(usuario);
        this.conciertoService = new ConciertoService();
        this.sectorService = new SectorService();
        this.usuarioService = new UsuarioService();
        this.ticketService = new TicketService();
        this.merchandisingService = new MerchandisingService();
        this.openTableFrames = new HashMap<>();
        inicializarMenu("Menu Administrador");
    }

    @Override
    protected String getTituloPanel() {
        return "Panel de Administrador";
    }

    @Override
    protected JComponent crearContenido() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        EstiloGUI.aplicarPanelContenido(panel);

        panel.add(new AdminConciertosPanel(
                e -> mostrarConciertosActivos(),
                e -> mostrarTodosLosConciertos(),
                e -> crearConcierto(),
                e -> modificarConcierto(),
                e -> cancelarConcierto(),
                e -> verDisponibilidadConcierto()));
        panel.add(new AdminSectoresPanel(
                e -> verSectoresDeConcierto(),
                e -> crearSector(),
                e -> crearTicketsDeSector()));
        panel.add(new AdminTicketsPanel(
                e -> verTicketsDeConcierto(),
                e -> bloquearTicket(),
                e -> liberarTicket()));
        panel.add(new AdminOtrosPanel(
                e -> gestionarMerchandising(),
                e -> gestionarUsuarios(),
                e -> PasswordDialogs.cambiarPassword(this, usuario),
                crearBotonVolverLogin(),
                crearBotonCerrarSistema()));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
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
            modificarConcierto(concierto);
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
            modificarConcierto(concierto);
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo modificar el concierto", e);
        }
    }

    private void modificarConcierto(Concierto concierto) throws SQLException {
        if (concierto == null || !puedeModificarConcierto(concierto)) {
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
    }

    private void cancelarConcierto() {
        try {
            Concierto concierto = seleccionarConcierto();
            cancelarConcierto(concierto);
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo cancelar el concierto", e);
        }
    }

    private void cancelarConcierto(int conciertoId) {
        try {
            Concierto concierto = conciertoService.buscarPorId(conciertoId);
            if (concierto == null) {
                mostrarInfo("Cancelar concierto", "No se encontro el concierto indicado.");
                return;
            }
            cancelarConcierto(concierto);
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo cancelar el concierto", e);
        }
    }

    private void cancelarConcierto(Concierto concierto) throws SQLException {
        if (concierto == null) {
            return;
        }
        if (!confirmarAccion("Cancelar concierto",
                "Desea cancelar el concierto \"" + concierto.getArtista() + "\"?")) {
            return;
        }

        boolean cancelado = conciertoService.cancelarConcierto(concierto.getId());
        mostrarInfo("Cancelar concierto", cancelado
                ? "El concierto fue cancelado."
                : "No se encontro el concierto indicado.");
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
            crearSector(concierto.getId());
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
            String nombre = DialogosUtil.pedirTexto(this, "Nombre del sector", "Principal");
            int capacidad = DialogosUtil.pedirEntero(this, "Capacidad del sector", "10");
            String precioStr = DialogosUtil.pedirTexto(this, "Precio (ej. 100.00)", "100.00");
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
            String nombre = DialogosUtil.pedirTexto(this, "Nombre del sector", sector.getNombre());
            int capacidad = DialogosUtil.pedirEntero(this, "Capacidad del sector", String.valueOf(sector.getCapacidad()));
            String precioStr = DialogosUtil.pedirTexto(this, "Precio (ej. 100.00)", sector.getPrecio().toString());
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
            if (!confirmarAccion("Eliminar sector", "Desea eliminar el sector seleccionado?")) {
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
            String etiqueta = String.format("#%d - %s - %s - %s - capacidad: %d - disponibles: %d",
                sector.getId(), artista, sector.getTipo(), sector.getNombre(),
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

    private void cerrarTablasAbiertas() {
        for (JFrame frame : openTableFrames.values().toArray(new JFrame[0])) {
            if (frame != null && frame.isDisplayable()) {
                frame.dispose();
            }
        }
        openTableFrames.clear();
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
            bloquearTicket(ticket.getId(), "Desea bloquear el ticket " + ticket.getCodigo() + "?");
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
            liberarTicket(ticket.getId(), "Desea liberar el ticket " + ticket.getCodigo() + "?");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo liberar el ticket", e);
        }
    }

    private void bloquearTicket(int ticketId) {
        try {
            bloquearTicket(ticketId, "Desea bloquear el ticket seleccionado?");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo bloquear el ticket", e);
        }
    }

    private void liberarTicket(int ticketId) {
        try {
            liberarTicket(ticketId, "Desea liberar el ticket seleccionado?");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo liberar el ticket", e);
        }
    }

    private void bloquearTicket(int ticketId, String mensajeConfirmacion) throws SQLException {
        if (!confirmarAccion("Bloquear ticket", mensajeConfirmacion)) {
            return;
        }

        boolean bloqueado = ticketService.bloquearTicket(ticketId);
        mostrarInfo("Bloquear ticket", bloqueado
                ? "El ticket fue bloqueado."
                : "No se pudo bloquear el ticket.");
    }

    private void liberarTicket(int ticketId, String mensajeConfirmacion) throws SQLException {
        if (!confirmarAccion("Liberar ticket", mensajeConfirmacion)) {
            return;
        }

        boolean liberado = ticketService.liberarTicket(ticketId);
        mostrarInfo("Liberar ticket", liberado
                ? "El ticket fue liberado."
                : "No se pudo liberar el ticket.");
    }

    private void mostrarTabla(String titulo, String[] columns, Object[][] rows) {
        mostrarTablaConBotones(titulo, columns, () -> rows, null);
    }

    private void mostrarTablaConciertos(String titulo, Supplier<LinkedList<Concierto>> fetcher) {
        String[] columns = {"ID", "Artista", "Fecha", "Hora", "Lugar", "Capacidad", "Disponibles", "Estado", "Organizador", "Poster"};
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
                data[i][9] = concierto.getPosterUrl();
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
        }, "Buscar por artista o lugar:", detallePosterConcierto(9), 1, 4);
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

    private void gestionarMerchandising() {
        String[] columns = {"ID", "Concierto", "Producto", "Precio", "Stock"};
        Supplier<Object[][]> rows = () -> {
            try {
                LinkedList<Merchandising> productos = merchandisingService.listarTodos();
                Object[][] data = new Object[productos.size()][columns.length];
                for (int i = 0; i < productos.size(); i++) {
                    Merchandising producto = productos.get(i);
                    Concierto concierto = conciertoService.buscarPorId(producto.getConciertoId());
                    data[i][0] = producto.getId();
                    data[i][1] = concierto == null ? producto.getConciertoId() : concierto.getArtista();
                    data[i][2] = producto.getNombre();
                    data[i][3] = producto.getPrecio();
                    data[i][4] = producto.getStock();
                }
                return data;
            } catch (SQLException e) {
                mostrarError("No se pudo listar el merchandising", e);
                return new Object[0][columns.length];
            }
        };
        mostrarTablaConBotones("Gestion de merchandising", columns, rows, (table, refrescar) -> {
            JButton crear = new JButton("Crear producto");
            crear.addActionListener(e -> { crearMerchandising(); refrescar.run(); });
            JButton editar = new JButton("Editar");
            editar.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { modificarMerchandising(id); refrescar.run(); }
            });
            JButton stock = new JButton("Ajustar stock");
            stock.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { ajustarStockMerchandising(id); refrescar.run(); }
            });
            JButton eliminar = new JButton("Eliminar");
            eliminar.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { eliminarMerchandising(id); refrescar.run(); }
            });
            return Arrays.asList(crear, editar, stock, eliminar);
        });
    }

    private void gestionarUsuarios() {
        String[] columns = {"ID", "Nombre", "Apellido", "Email", "Documento/DNI", "Rol"};
        Supplier<Object[][]> rows = () -> {
            try {
                LinkedList<Usuario> usuarios = usuarioService.listarUsuarios();
                Object[][] data = new Object[usuarios.size()][columns.length];
                for (int i = 0; i < usuarios.size(); i++) {
                    Usuario usuarioListado = usuarios.get(i);
                    data[i][0] = usuarioListado.getId();
                    data[i][1] = usuarioListado.getNombre();
                    data[i][2] = usuarioListado.getApellido();
                    data[i][3] = usuarioListado.getEmail();
                    data[i][4] = usuarioListado.getDocumento();
                    data[i][5] = usuarioListado.getRol();
                }
                return data;
            } catch (SQLException e) {
                mostrarError("No se pudieron listar los usuarios", e);
                return new Object[0][columns.length];
            }
        };
        mostrarTablaConBotones("Gestion de usuarios", columns, rows, (table, refrescar) -> {
            JButton crear = new JButton("Crear usuario");
            crear.addActionListener(e -> { crearUsuario(); refrescar.run(); });
            JButton editar = new JButton("Editar");
            editar.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { modificarUsuario(id); refrescar.run(); }
            });
            JButton password = new JButton("Cambiar password");
            password.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { cambiarPasswordUsuario(id); refrescar.run(); }
            });
            JButton eliminar = new JButton("Eliminar");
            eliminar.addActionListener(e -> {
                Integer id = idSeleccionado(table);
                if (id != null) { eliminarUsuario(id); refrescar.run(); }
            });
            return Arrays.asList(crear, editar, password, eliminar);
        }, "Buscar por nombre, email o rol:", 1, 2, 3, 5);
    }

    private void crearUsuario() {
        try {
            String nombre = DialogosUtil.pedirTexto(this, "Nombre", "Nombre");
            String apellido = DialogosUtil.pedirTexto(this, "Apellido", "Apellido");
            String email = DialogosUtil.pedirTexto(this, "Email", "usuario@mail.com");
            String documento = DialogosUtil.pedirTextoOpcional(this, "Documento/DNI (opcional)", "");
            String rol = seleccionarRol("Comprador");
            if (rol == null) {
                return;
            }
            String password = PasswordDialogs.pedirNuevoPassword(this, "Password inicial");
            if (password == null) {
                return;
            }

            int id = usuarioService.crearUsuario(nombre, apellido, email, documento, password, rol);
            mostrarInfo("Usuario creado", "Se creo el usuario con ID " + id + ".");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo crear el usuario", e);
        }
    }

    private void modificarUsuario(int id) {
        try {
            Usuario usuarioEditado = usuarioService.buscarPorId(id);
            if (usuarioEditado == null) {
                mostrarInfo("Sin usuario", "No se encontro el usuario indicado.");
                return;
            }

            String nombre = DialogosUtil.pedirTexto(this, "Nombre", usuarioEditado.getNombre());
            String apellido = DialogosUtil.pedirTexto(this, "Apellido", usuarioEditado.getApellido());
            String email = DialogosUtil.pedirTexto(this, "Email", usuarioEditado.getEmail());
            String documento = DialogosUtil.pedirTextoOpcional(this, "Documento/DNI (opcional)", usuarioEditado.getDocumento());
            String rol = seleccionarRol(usuarioEditado.getRol());
            if (rol == null) {
                return;
            }

            usuarioEditado.setNombre(nombre);
            usuarioEditado.setApellido(apellido);
            usuarioEditado.setEmail(email);
            usuarioEditado.setDocumento(documento);
            usuarioEditado.setRol(rol);

            boolean modificado = usuarioService.modificarUsuario(usuarioEditado);
            mostrarInfo("Modificar usuario", modificado
                    ? "El usuario fue modificado."
                    : "No se pudo modificar el usuario.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo modificar el usuario", e);
        }
    }

    private void cambiarPasswordUsuario(int id) {
        try {
            Usuario usuarioCambio = usuarioService.buscarPorId(id);
            if (usuarioCambio == null) {
                mostrarInfo("Sin usuario", "No se encontro el usuario indicado.");
                return;
            }
            String password = PasswordDialogs.pedirNuevoPassword(this, "Cambiar password de " + usuarioCambio.getEmail());
            if (password == null) {
                return;
            }
            if (!confirmarAccion("Cambiar password",
                    "Desea cambiar el password de " + usuarioCambio.getEmail() + "?")) {
                return;
            }

            boolean actualizado = usuarioService.actualizarPassword(id, password, password);
            mostrarInfo("Cambiar password", actualizado
                    ? "El password fue actualizado."
                    : "No se pudo actualizar el password.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo actualizar el password", e);
        }
    }

    private void eliminarUsuario(int id) {
        try {
            if (id == usuario.getId()) {
                mostrarInfo("Eliminar usuario", "No se puede eliminar el usuario actual.");
                return;
            }
            Usuario usuarioEliminar = usuarioService.buscarPorId(id);
            if (usuarioEliminar == null) {
                mostrarInfo("Sin usuario", "No se encontro el usuario indicado.");
                return;
            }
            if (!confirmarAccion("Eliminar usuario",
                    "Desea eliminar el usuario " + usuarioEliminar.getEmail() + "?")) {
                return;
            }

            boolean eliminado = usuarioService.eliminarUsuario(id);
            mostrarInfo("Eliminar usuario", eliminado
                    ? "El usuario fue eliminado."
                    : "No se encontro el usuario indicado.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo eliminar el usuario", e);
        }
    }

    private void crearMerchandising() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }
            String nombre = DialogosUtil.pedirTexto(this, "Nombre del producto", "Remera");
            BigDecimal precio = DialogosUtil.pedirPrecio(this, "35.00");
            int stock = DialogosUtil.pedirEntero(this, "Stock", "100");
            int id = merchandisingService.crearProducto(concierto.getId(), nombre, precio, stock);
            mostrarInfo("Producto creado", "Se creo el producto con ID " + id + ".");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo crear el producto", e);
        }
    }

    private void modificarMerchandising(int id) {
        try {
            Merchandising producto = merchandisingService.buscarPorId(id);
            if (producto == null) {
                mostrarInfo("Sin producto", "No se encontro el producto indicado.");
                return;
            }
            String nombre = DialogosUtil.pedirTexto(this, "Nombre del producto", producto.getNombre());
            BigDecimal precio = DialogosUtil.pedirPrecio(this, producto.getPrecio().toString());
            int stock = DialogosUtil.pedirEntero(this, "Stock", String.valueOf(producto.getStock()));
            producto.setNombre(nombre);
            producto.setPrecio(precio);
            producto.setStock(stock);
            boolean modificado = merchandisingService.modificarProducto(producto);
            mostrarInfo("Modificar producto", modificado
                    ? "El producto fue modificado."
                    : "No se pudo modificar el producto.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo modificar el producto", e);
        }
    }

    private void ajustarStockMerchandising(int id) {
        try {
            Merchandising producto = merchandisingService.buscarPorId(id);
            if (producto == null) {
                mostrarInfo("Sin producto", "No se encontro el producto indicado.");
                return;
            }
            int stock = DialogosUtil.pedirEntero(this, "Stock", String.valueOf(producto.getStock()));
            if (!confirmarAccion("Actualizar stock",
                    "Desea cambiar el stock de \"" + producto.getNombre() + "\" de "
                            + producto.getStock() + " a " + stock + "?")) {
                return;
            }
            boolean actualizado = merchandisingService.actualizarStock(id, stock);
            mostrarInfo("Actualizar stock", actualizado
                    ? "El stock fue actualizado."
                    : "No se pudo actualizar el stock.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo actualizar el stock", e);
        }
    }

    private void eliminarMerchandising(int id) {
        try {
            if (!confirmarAccion("Eliminar producto", "Desea eliminar el producto seleccionado?")) {
                return;
            }
            boolean eliminado = merchandisingService.eliminarProducto(id);
            mostrarInfo("Eliminar producto", eliminado
                    ? "El producto fue eliminado."
                    : "No se encontro el producto indicado.");
        } catch (IllegalArgumentException e) {
            mostrarInfo("Datos invalidos", e.getMessage());
        } catch (SQLException e) {
            mostrarError("No se pudo eliminar el producto", e);
        }
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
        mostrarTablaConBotones(titulo, columns, rowsSupplier, extraButtons, null);
    }

    private void mostrarTablaConBotones(String titulo, String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons,
            String etiquetaBusqueda,
            int... columnasBusqueda) {
        mostrarTablaConBotones(titulo, columns, rowsSupplier, extraButtons, etiquetaBusqueda, null, columnasBusqueda);
    }

    private void mostrarTablaConBotones(String titulo, String[] columns,
            Supplier<Object[][]> rowsSupplier,
            BiFunction<JTable, Runnable, List<JButton>> extraButtons,
            String etiquetaBusqueda,
            TablaConBotones.DetalleImagen detalleImagen,
            int... columnasBusqueda) {
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

        TablaConBotones.Busqueda busqueda = etiquetaBusqueda == null
                ? null
                : new TablaConBotones.Busqueda(etiquetaBusqueda, columnasBusqueda);
        JFrame frame = TablaConBotones.mostrar(this, titulo, columns, rowsSupplier, extraButtons, busqueda, detalleImagen);
        openTableFrames.put(titulo, frame);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (openTableFrames.get(titulo) == frame) {
                    openTableFrames.remove(titulo);
                }
            }
        });
    }

    private TablaConBotones.DetalleImagen detallePosterConcierto(int columnaPoster) {
        return new TablaConBotones.DetalleImagen(
                columnaPoster,
                220,
                300,
                new int[] {1, 2, 4, 7},
                new String[] {"Artista", "Fecha", "Lugar", "Estado"});
    }

    private Integer idSeleccionado(JTable table) {
        return TablaConBotones.idSeleccionado(this, table);
    }

    private boolean confirmarAccion(String titulo, String mensaje) {
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                mensaje,
                titulo,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return confirmacion == JOptionPane.YES_OPTION;
    }

    private String seleccionarRol(String seleccionInicial) {
        String[] roles = {"Administrador", "Organizador", "Comprador", "PersonalAcceso"};
        return (String) JOptionPane.showInputDialog(
                this,
                "Seleccione el rol:",
                "Rol de usuario",
                JOptionPane.PLAIN_MESSAGE,
                null,
                roles,
                seleccionInicial == null ? "Comprador" : seleccionInicial);
    }

    private void mostrarInfo(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String titulo, Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), titulo, JOptionPane.ERROR_MESSAGE);
    }
}
