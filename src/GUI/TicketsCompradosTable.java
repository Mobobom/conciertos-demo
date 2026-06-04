package GUI;

import BLL.Concierto;
import BLL.ConciertoService;
import BLL.Sector;
import BLL.SectorService;
import BLL.Ticket;
import BLL.TicketService;
import BLL.Usuario;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class TicketsCompradosTable {

    public static void showTable(Usuario comprador) {
        TicketService ticketService = new TicketService();
        ConciertoService conciertoService = new ConciertoService();
        SectorService sectorService = new SectorService();

        try {
            LinkedList<Ticket> tickets = ticketService.listarCompradosPorComprador(comprador.getId());
            if (tickets.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "No hay tickets comprados para este usuario.",
                        "Tickets comprados",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Map<Integer, Concierto> conciertosPorId = new HashMap<>();
            Map<Integer, Sector> sectoresPorId = new HashMap<>();
            String[] columns = {"Concierto", "Fecha", "Hora", "Lugar", "Sector", "Codigo", "Precio", "Estado"};
            Object[][] rows = new Object[tickets.size()][columns.length];

            for (int i = 0; i < tickets.size(); i++) {
                Ticket ticket = tickets.get(i);
                Concierto concierto = conciertosPorId.get(ticket.getConciertoId());
                if (concierto == null) {
                    concierto = conciertoService.buscarPorId(ticket.getConciertoId());
                    conciertosPorId.put(ticket.getConciertoId(), concierto);
                }

                Sector sector = sectoresPorId.get(ticket.getSectorId());
                if (sector == null) {
                    sector = sectorService.buscarPorId(ticket.getSectorId());
                    sectoresPorId.put(ticket.getSectorId(), sector);
                }

                rows[i][0] = concierto == null ? ticket.getConciertoId() : concierto.getArtista();
                rows[i][1] = concierto == null ? "" : concierto.getFecha();
                rows[i][2] = concierto == null ? "" : concierto.getHora();
                rows[i][3] = concierto == null ? "" : concierto.getLugar();
                rows[i][4] = sector == null ? ticket.getSectorId() : sector.getTipo() + " - " + sector.getNombre();
                rows[i][5] = ticket.getCodigo();
                rows[i][6] = ticket.getPrecio();
                rows[i][7] = ticket.getEstado();
            }

            mostrarTabla("Tickets comprados - " + comprador.getNombre(), columns, rows);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error de base de datos: " + e.getMessage(),
                    "Tickets comprados",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void mostrarTabla(String titulo, String[] columns, Object[][] rows) {
        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JFrame frame = new JFrame(titulo);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.setSize(900, 420);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
