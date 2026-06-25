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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MenuComprador extends MenuBase {

    private final ConciertoService conciertoService;
    private final SectorService sectorService;
    private final CompraService compraService;
    private final MerchandisingService merchandisingService;
    private final LinkedList<JFrame> openTableFrames;

    public MenuComprador(Usuario usuario) {
        super(usuario);
        this.conciertoService = new ConciertoService();
        this.sectorService = new SectorService();
        this.compraService = new CompraService();
        this.merchandisingService = new MerchandisingService();
        this.openTableFrames = new LinkedList<>();
        inicializarMenu("Menu Comprador");
    }

    @Override
    protected String getTituloPanel() {
        return "Panel de Comprador";
    }

    @Override
    protected JComponent crearContenido() {
        JPanel panel = new JPanel(new BorderLayout());
        EstiloGUI.aplicarPanelContenido(panel);

        JPanel grid = new JPanel(new GridLayout(0, 2, EstiloGUI.ESPACIADO, EstiloGUI.ESPACIADO));
        EstiloGUI.aplicarPanel(grid);

        addButton(grid, "Ver conciertos disponibles", "search",
                e -> registrarVentana(ShowConciertosTable.showTable(this, this::iniciarCompra)));
        addButton(grid, "Comprar tickets", "buy", e -> comprarTickets());
        addButton(grid, "Tickets comprados", "ticket",
                e -> registrarVentana(TicketsCompradosTable.showTable(this, usuario)));
        addButton(grid, "Ver catalogo merchandising", "merchandising", e -> verCatalogoMerchandising());
        addButton(grid, "Comprar merchandising", "buy", e -> comprarMerchandising());
        addButton(grid, "Merchandising comprado", "merchandising",
                e -> registrarVentana(MerchandisingCompradoTable.showTable(this, usuario)));
        addButton(grid, "Cambiar password", "edit", e -> PasswordDialogs.cambiarPassword(this, usuario));
        grid.add(crearBotonVolverLogin());
        panel.add(grid, BorderLayout.NORTH);
        return panel;
    }

    @Override
    public void dispose() {
        cerrarTablasAbiertas();
        super.dispose();
    }

    private void registrarVentana(JFrame frame) {
        if (frame == null || openTableFrames.contains(frame)) {
            return;
        }
        openTableFrames.add(frame);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                openTableFrames.remove(frame);
            }
        });
    }

    private void cerrarTablasAbiertas() {
        for (JFrame frame : openTableFrames.toArray(new JFrame[0])) {
            if (frame != null && frame.isDisplayable()) {
                frame.dispose();
            }
        }
        openTableFrames.clear();
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

            iniciarCompraMerchandising(merchandising);
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        } catch (SQLException e) {
            mostrarError("Error de base de datos: " + e.getMessage());
        }
    }

    private void verCatalogoMerchandising() {
        try {
            Concierto concierto = seleccionarConcierto();
            if (concierto == null) {
                return;
            }
            registrarVentana(ShowMerchandisingTable.showTable(this, concierto, this::iniciarCompraMerchandising));
        } catch (SQLException e) {
            mostrarError("Error de base de datos: " + e.getMessage());
        }
    }

    private void iniciarCompraMerchandising(Merchandising merchandising) {
        if (merchandising == null) {
            return;
        }
        try {
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
            if (!confirmarCompraTickets(concierto, sector, cantidad, metodoPago)) {
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

    private boolean confirmarCompraTickets(Concierto concierto, Sector sector, int cantidad, String metodoPago) {
        String mensaje = "Concierto: " + concierto.getArtista() + "\n"
                + "Sector: " + sector.getTipo() + " - " + sector.getNombre() + "\n"
                + "Cantidad: " + cantidad + "\n"
                + "Precio unitario: " + sector.getPrecio() + "\n"
                + "Total: " + sector.getPrecio().multiply(java.math.BigDecimal.valueOf(cantidad)) + "\n"
                + "Metodo de pago: " + metodoPago + "\n\n"
                + "Desea confirmar la compra?";
        int opcion = JOptionPane.showConfirmDialog(this, mensaje,
                "Confirmar compra de tickets", JOptionPane.YES_NO_OPTION);
        return opcion == JOptionPane.YES_OPTION;
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
        String mensaje = "Compra de merchandising exitosa:\n"
                + "Compra ID: " + resultado.getCompraId() + "\n"
                + "Pago ID: " + resultado.getPagoId() + "\n"
                + "Detalle ID: " + resultado.getCompraMerchandisingId() + "\n"
                + "Producto: " + resultado.getMerchandising().getNombre() + "\n"
                + "Cantidad: " + resultado.getCantidad() + "\n"
                + "Total: " + resultado.getTotal() + "\n";
        JOptionPane.showMessageDialog(this, mensaje, "Compra completada",
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
