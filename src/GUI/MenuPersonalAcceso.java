package GUI;

import BLL.Concierto;
import BLL.ConciertoService;
import BLL.Sector;
import BLL.SectorService;
import BLL.Ticket;
import BLL.TicketService;
import BLL.Usuario;
import BLL.ValidacionTicketResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MenuPersonalAcceso extends JFrame {

    private final Usuario usuario;
    private final ConciertoService conciertoService;
    private final SectorService sectorService;
    private final TicketService ticketService;

    public MenuPersonalAcceso(Usuario usuario) {
        this.usuario = usuario;
        this.conciertoService = new ConciertoService();
        this.sectorService = new SectorService();
        this.ticketService = new TicketService();
        initialize();
    }

    private void initialize() {
        setTitle("Menu Personal de Acceso - " + usuario.getNombre());
        setSize(520, 280);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildButtons(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JTextArea header = new JTextArea(
                "Bienvenido " + usuario.getNombre() + " " + usuario.getApellido() + "\n"
                        + "Rol: " + usuario.getRol() + "\n"
                        + "Valide tickets seleccionando concierto y ticket.");
        header.setEditable(false);
        header.setOpaque(false);
        header.setFocusable(false);
        header.setFont(header.getFont().deriveFont(14f));
        panel.add(header, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 80, 25, 80));

        JButton validateButton = new JButton("Validar codigo de ticket");
        validateButton.addActionListener(e -> validarTicket());

        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());

        JButton exitButton = new JButton("Salir");
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(validateButton);
        panel.add(closeButton);
        panel.add(exitButton);
        return panel;
    }

    private void validarTicket() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }

            Ticket ticket = seleccionarTicket(concierto);
            if (ticket == null) {
                return;
            }

            ValidacionTicketResult resultado = ticketService.validarAcceso(ticket.getCodigo());
            int tipo = resultado.isValido() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
            StringBuilder mensaje = new StringBuilder();
            mensaje.append(resultado.getMensaje());
            if (resultado.getTicket() != null) {
                mensaje.append("\nCodigo: ").append(resultado.getTicket().getCodigo());
                mensaje.append("\nEstado: ").append(resultado.getTicket().getEstado());
            }
            JOptionPane.showMessageDialog(this, mensaje.toString(), "Resultado de validacion", tipo);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error de base de datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Concierto seleccionarConcierto() throws SQLException {
        LinkedList<Concierto> conciertos = conciertoService.listarActivos();
        if (conciertos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay conciertos activos disponibles.",
                    "Sin conciertos", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Map<String, Concierto> conciertosPorEtiqueta = new HashMap<>();
        String[] opciones = new String[conciertos.size()];
        for (int i = 0; i < conciertos.size(); i++) {
            Concierto concierto = conciertos.get(i);
            String etiqueta = String.format("%d - %s (%s %s) - %s",
                    concierto.getId(), concierto.getArtista(),
                    concierto.getFecha(), concierto.getHora(), concierto.getLugar());
            opciones[i] = etiqueta;
            conciertosPorEtiqueta.put(etiqueta, concierto);
        }

        String seleccionado = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione un concierto:",
                "Conciertos activos",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]);

        return seleccionado == null ? null : conciertosPorEtiqueta.get(seleccionado);
    }

    private Ticket seleccionarTicket(Concierto concierto) throws SQLException {
        LinkedList<Ticket> tickets = ticketService.listarPorConcierto(concierto.getId());
        if (tickets.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay tickets creados para el concierto seleccionado.",
                    "Sin tickets", JOptionPane.WARNING_MESSAGE);
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
                "Tickets del concierto",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]);

        return seleccionado == null ? null : ticketsPorEtiqueta.get(seleccionado);
    }
}
