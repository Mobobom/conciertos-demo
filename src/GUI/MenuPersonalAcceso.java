package GUI;

import BLL.Concierto;
import BLL.ConciertoService;
import BLL.Sector;
import BLL.SectorService;
import BLL.Ticket;
import BLL.TicketService;
import BLL.Usuario;
import BLL.ValidacionTicketResult;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MenuPersonalAcceso extends MenuBase {

    private final ConciertoService conciertoService;
    private final SectorService sectorService;
    private final TicketService ticketService;

    public MenuPersonalAcceso(Usuario usuario) {
        super(usuario);
        this.conciertoService = new ConciertoService();
        this.sectorService = new SectorService();
        this.ticketService = new TicketService();
        inicializarMenu("Menu Personal de Acceso - " + usuario.getNombre());
    }

    @Override
    protected String getTituloPanel() {
        return "Panel de Personal de Acceso";
    }

    @Override
    protected JComponent crearCabecera() {
        JPanel panel = new JPanel(new BorderLayout());
        EstiloGUI.aplicarPanelCabecera(panel);

        JTextArea header = new JTextArea(
                "Bienvenido " + usuario.getNombre() + " " + usuario.getApellido() + "\n"
                        + "Rol: " + usuario.getRol() + "\n"
                        + "Valide tickets seleccionando concierto y ticket.");
        EstiloGUI.aplicarAreaTexto(header);
        panel.add(header, BorderLayout.CENTER);
        return panel;
    }

    @Override
    protected JComponent crearContenido() {
        JPanel panel = new JPanel(new BorderLayout());
        EstiloGUI.aplicarPanelContenido(panel);

        JPanel grid = new JPanel(new GridLayout(0, 2, EstiloGUI.ESPACIADO, EstiloGUI.ESPACIADO));
        EstiloGUI.aplicarPanel(grid);

        JButton validateButton = BotonHelper.crearBoton("Validar codigo de ticket", "validate", e -> validarTicket());
        JButton passwordButton = BotonHelper.crearBoton("Cambiar password", "edit",
                e -> PasswordDialogs.cambiarPassword(this, usuario));

        grid.add(validateButton);
        grid.add(passwordButton);
        grid.add(crearBotonVolverLogin());
        grid.add(crearBotonCerrarSistema());
        panel.add(grid, BorderLayout.NORTH);
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
            if (!confirmarValidacionTicket(ticket)) {
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

    private boolean confirmarValidacionTicket(Ticket ticket) {
        String mensaje = "Codigo: " + ticket.getCodigo() + "\n"
                + "Estado actual: " + ticket.getEstado() + "\n\n"
                + "Desea validar este ticket?";
        int opcion = JOptionPane.showConfirmDialog(this, mensaje,
                "Confirmar validacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return opcion == JOptionPane.YES_OPTION;
    }
}
