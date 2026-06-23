package GUI;

import BLL.CompraMerchandisingResultado;
import BLL.CompraResultado;
import BLL.CompraService;
import BLL.Concierto;
import BLL.ConciertoService;
import BLL.Merchandising;
import BLL.MerchandisingService;
import BLL.Sector;
import BLL.SectorService;
import BLL.Usuario;

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

public class MenuComprador extends JFrame {

    private final Usuario usuario;
    private final ConciertoService conciertoService;
    private final SectorService sectorService;
    private final CompraService compraService;
    private final MerchandisingService merchandisingService;

    public MenuComprador(Usuario usuario) {
        this.usuario = usuario;
        this.conciertoService = new ConciertoService();
        this.sectorService = new SectorService();
        this.compraService = new CompraService();
        this.merchandisingService = new MerchandisingService();
        initialize();
    }

    private void initialize() {
        setTitle("Menu Comprador - " + usuario.getNombre());
        setSize(520, 300);
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
                        + "Use las opciones para comprar tickets y merchandising.");
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

        JButton showButton = new JButton("Ver conciertos disponibles");
        showButton.addActionListener(e -> ShowConciertosTable.showTable(this::iniciarCompra));

        JButton buyButton = new JButton("Comprar tickets");
        buyButton.addActionListener(e -> comprarTickets());

        JButton purchasedTicketsButton = new JButton("Tickets comprados");
        purchasedTicketsButton.addActionListener(e -> TicketsCompradosTable.showTable(usuario));

        JButton merchButton = new JButton("Comprar merchandising");
        merchButton.addActionListener(e -> comprarMerchandising());

        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());

        JButton exitButton = new JButton("Salir");
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(showButton);
        panel.add(buyButton);
        panel.add(purchasedTicketsButton);
        panel.add(merchButton);
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

    private void comprarMerchandising() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }

            Merchandising merchandising = seleccionarMerchandising(concierto.getId());
            if (merchandising == null) {
                return;
            }

            int cantidad = solicitarCantidadMerchandising(merchandising.getStock());
            if (cantidad <= 0) {
                return;
            }

            String metodoPago = seleccionarMetodoPago();
            if (metodoPago == null) {
                return;
            }

            if (!confirmarCompraMerchandising(merchandising, cantidad, metodoPago)) {
                return;
            }

            CompraMerchandisingResultado resultado = merchandisingService.comprarMerchandising(
                    usuario.getId(), merchandising.getId(), cantidad, metodoPago);

            mostrarResultadoMerchandising(resultado);
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
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

    private Merchandising seleccionarMerchandising(int conciertoId) throws SQLException {
        LinkedList<Merchandising> productos = merchandisingService.listarDisponiblesPorConcierto(conciertoId);
        if (productos.isEmpty()) {
            mostrarError("No hay merchandising disponible para el concierto seleccionado.");
            return null;
        }

        Map<String, Merchandising> map = new HashMap<>();
        String[] opciones = new String[productos.size()];
        for (int i = 0; i < productos.size(); i++) {
            Merchandising m = productos.get(i);
            String label = String.format("%d - %s - precio: %s - stock: %d",
                    m.getId(), m.getNombre(), m.getPrecio(), m.getStock());
            opciones[i] = label;
            map.put(label, m);
        }

        String seleccionado = (String) JOptionPane.showInputDialog(this,
                "Seleccione merchandising:", "Merchandising disponible",
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

    private int solicitarCantidadMerchandising(int stockDisponible) {
        while (true) {
            String valor = JOptionPane.showInputDialog(this,
                    "Ingrese cantidad de productos (stock " + stockDisponible + "):",
                    "Cantidad de merchandising", JOptionPane.QUESTION_MESSAGE);
            if (valor == null) {
                return 0;
            }
            try {
                int cantidad = Integer.parseInt(valor.trim());
                if (cantidad <= 0) {
                    mostrarError("La cantidad debe ser mayor a cero.");
                    continue;
                }
                if (cantidad > stockDisponible) {
                    mostrarError("No hay stock suficiente para el producto seleccionado.");
                    continue;
                }
                return cantidad;
            } catch (NumberFormatException e) {
                mostrarError("Cantidad invalida. Ingrese un numero entero.");
            }
        }
    }

    private boolean confirmarCompraMerchandising(Merchandising merchandising, int cantidad, String metodoPago) {
        String mensaje = "Producto: " + merchandising.getNombre() + "\n"
                + "Cantidad: " + cantidad + "\n"
                + "Precio unitario: " + merchandising.getPrecio() + "\n"
                + "Total: " + merchandising.getPrecio().multiply(java.math.BigDecimal.valueOf(cantidad)) + "\n"
                + "Metodo de pago: " + metodoPago + "\n\n"
                + "Desea confirmar la compra?";
        int opcion = JOptionPane.showConfirmDialog(this, mensaje,
                "Confirmar compra de merchandising", JOptionPane.YES_NO_OPTION);
        return opcion == JOptionPane.YES_OPTION;
    }

    private void mostrarResultadoMerchandising(CompraMerchandisingResultado resultado) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Compra de merchandising exitosa:\n");
        mensaje.append("Compra ID: ").append(resultado.getCompraId()).append("\n");
        mensaje.append("Pago ID: ").append(resultado.getPagoId()).append("\n");
        mensaje.append("Detalle ID: ").append(resultado.getCompraMerchandisingId()).append("\n");
        mensaje.append("Producto: ").append(resultado.getMerchandising().getNombre()).append("\n");
        mensaje.append("Cantidad: ").append(resultado.getCantidad()).append("\n");
        mensaje.append("Total: ").append(resultado.getTotal()).append("\n");
        JOptionPane.showMessageDialog(this, mensaje.toString(), "Compra completada",
                JOptionPane.INFORMATION_MESSAGE);
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
