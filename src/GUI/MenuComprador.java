package GUI;

import BLL.CompraResultado;
import BLL.CompraService;
import BLL.Concierto;
import BLL.ConciertoService;
import BLL.Sector;
import BLL.SectorService;
import BLL.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MenuComprador extends JFrame {

    private final Usuario usuario;
    private final ConciertoService conciertoService;
    private final SectorService sectorService;
    private final CompraService compraService;

    public MenuComprador(Usuario usuario) {
        this.usuario = usuario;
        this.conciertoService = new ConciertoService();
        this.sectorService = new SectorService();
        this.compraService = new CompraService();
        initialize();
    }

    private void initialize() {
        setTitle("Menu Comprador - " + usuario.getNombre());
        setSize(520, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JTextArea header = new JTextArea(
                "Bienvenido " + usuario.getNombre() + " " + usuario.getApellido() + "\n"
                        + "Rol: " + usuario.getRol() + "\n"
                        + "Use las opciones para comprar tickets.");
        header.setEditable(false);
        header.setOpaque(false);
        header.setFocusable(false);
        header.setFont(header.getFont().deriveFont(14f));
        panel.add(header, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        JButton showButton = new JButton("Ver conciertos disponibles");
        showButton.addActionListener(e -> ShowConciertosTable.showTable(this::iniciarCompra));

        JButton buyButton = new JButton("Comprar tickets");
        buyButton.addActionListener(e -> comprarTickets());

        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());

        JButton exitButton = new JButton("Salir");
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(showButton);
        panel.add(buyButton);
        panel.add(closeButton);
        panel.add(exitButton);
        return panel;
    }

    private void comprarTickets() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }
            iniciarCompra(concierto);
        } catch (SQLException e) {
            mostrarError("Error de base de datos: " + e.getMessage());
        }
    }

    public void iniciarCompra(Concierto concierto) {
        if (concierto == null) {
            return;
        }
        try {
            Sector sector = seleccionarSector(concierto.getId());
            if (sector == null) {
                return;
            }

            if (sector.getDisponibles() <= 0) {
                mostrarError("No hay tickets disponibles en el sector seleccionado.");
                return;
            }

            int cantidad = solicitarCantidad(sector.getDisponibles());
            if (cantidad <= 0) {
                return;
            }

            String metodoPago = seleccionarMetodoPago();
            if (metodoPago == null) {
                return;
            }

            CompraResultado resultado = compraService.comprarTickets(
                    usuario.getId(), concierto.getId(), sector.getId(), cantidad, metodoPago);

            StringBuilder mensaje = new StringBuilder();
            mensaje.append("Compra exitosa:\n");
            mensaje.append("Compra ID: ").append(resultado.getCompraId()).append("\n");
            mensaje.append("Pago ID: ").append(resultado.getPagoId()).append("\n");
            mensaje.append("Total: ").append(resultado.getTotal()).append("\n");
            mensaje.append("Tickets:\n");
            for (int i = 0; i < resultado.getTickets().size(); i++) {
                mensaje.append(" - ").append(resultado.getTickets().get(i).getCodigo()).append("\n");
            }

            JOptionPane.showMessageDialog(this, mensaje.toString(), "Compra completada",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        } catch (SQLException e) {
            mostrarError("Error de base de datos: " + e.getMessage());
        }
    }

    private Concierto seleccionarConcierto() throws SQLException {
        LinkedList<Concierto> conciertos = conciertoService.listarActivos();
        if (conciertos.isEmpty()) {
            mostrarError("No hay conciertos activos disponibles.");
            return null;
        }

        Map<String, Concierto> map = new HashMap<>();
        String[] opciones = new String[conciertos.size()];
        for (int i = 0; i < conciertos.size(); i++) {
            Concierto c = conciertos.get(i);
            String label = String.format("%d - %s (%s %s) - %s - dispon.: %d",
                    c.getId(), c.getArtista(), c.getFecha(), c.getHora(), c.getLugar(), c.getDisponibles());
            opciones[i] = label;
            map.put(label, c);
        }

        String seleccionado = (String) JOptionPane.showInputDialog(this,
                "Seleccione un concierto:", "Conciertos activos",
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        return seleccionado == null ? null : map.get(seleccionado);
    }

    private Sector seleccionarSector(int conciertoId) throws SQLException {
        LinkedList<Sector> sectores = sectorService.listarPorConcierto(conciertoId);
        if (sectores.isEmpty()) {
            mostrarError("No hay sectores registrados para el concierto seleccionado.");
            return null;
        }

        Map<String, Sector> map = new HashMap<>();
        String[] opciones = new String[sectores.size()];
        for (int i = 0; i < sectores.size(); i++) {
            Sector s = sectores.get(i);
            String label = String.format("%d - %s - %s - precio: %s - dispon.: %d",
                    s.getId(), s.getTipo(), s.getNombre(), s.getPrecio(), s.getDisponibles());
            opciones[i] = label;
            map.put(label, s);
        }

        String seleccionado = (String) JOptionPane.showInputDialog(this,
                "Seleccione un sector:", "Sectores disponibles",
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        return seleccionado == null ? null : map.get(seleccionado);
    }

    private int solicitarCantidad(int maxDisponibles) {
        while (true) {
            String valor = JOptionPane.showInputDialog(this,
                    "Ingrese cantidad de tickets (max " + Math.min(6, maxDisponibles) + "):",
                    "Cantidad de tickets", JOptionPane.QUESTION_MESSAGE);
            if (valor == null) {
                return 0;
            }
            try {
                int cantidad = Integer.parseInt(valor.trim());
                if (cantidad <= 0) {
                    mostrarError("La cantidad debe ser mayor a cero.");
                    continue;
                }
                if (cantidad > 6) {
                    mostrarError("No puede comprar mas de 6 tickets en la misma operacion.");
                    continue;
                }
                if (cantidad > maxDisponibles) {
                    mostrarError("No hay suficientes tickets disponibles en el sector elegido.");
                    continue;
                }
                return cantidad;
            } catch (NumberFormatException e) {
                mostrarError("Cantidad invalida. Ingrese un numero entero.");
            }
        }
    }

    private String seleccionarMetodoPago() {
        String[] opciones = {"Efectivo", "TarjetaCredito", "TarjetaDebito", "Transferencia"};
        return (String) JOptionPane.showInputDialog(this,
                "Seleccione metodo de pago:", "Metodo de pago",
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.WARNING_MESSAGE);
    }
}
