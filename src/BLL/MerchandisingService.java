package BLL;

import DLL.Conexion;
import DLL.ControllerCompra;
import DLL.ControllerConcierto;
import DLL.ControllerMerchandising;
import DLL.ControllerPago;
import DLL.ControllerUsuario;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;

public class MerchandisingService {

    private final ControllerMerchandising controllerMerchandising;
    private final ControllerConcierto controllerConcierto;
    private final ControllerUsuario controllerUsuario;
    private final ControllerCompra controllerCompra;
    private final ControllerPago controllerPago;

    public MerchandisingService() {
        this(new ControllerMerchandising(), new ControllerConcierto(), new ControllerUsuario(),
                new ControllerCompra(), new ControllerPago());
    }

    public MerchandisingService(ControllerMerchandising controllerMerchandising,
                                ControllerConcierto controllerConcierto,
                                ControllerUsuario controllerUsuario,
                                ControllerCompra controllerCompra,
                                ControllerPago controllerPago) {
        this.controllerMerchandising = controllerMerchandising;
        this.controllerConcierto = controllerConcierto;
        this.controllerUsuario = controllerUsuario;
        this.controllerCompra = controllerCompra;
        this.controllerPago = controllerPago;
    }

    public int crearProducto(int conciertoId, String nombre, BigDecimal precio, int stock) throws SQLException {
        Merchandising merchandising = new Merchandising(0, conciertoId, nombre, precio, stock);
        validarProducto(merchandising);
        merchandising.setNombre(nombre.trim());
        return controllerMerchandising.crear(merchandising);
    }

    public boolean modificarProducto(Merchandising merchandising) throws SQLException {
        if (merchandising.getId() <= 0) {
            throw new IllegalArgumentException("El id de merchandising debe ser mayor a cero.");
        }
        if (controllerMerchandising.buscarPorId(merchandising.getId()) == null) {
            throw new IllegalArgumentException("No existe el producto de merchandising indicado.");
        }
        validarProducto(merchandising);
        merchandising.setNombre(merchandising.getNombre().trim());
        return controllerMerchandising.modificar(merchandising);
    }

    public boolean eliminarProducto(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("El id de merchandising debe ser mayor a cero.");
        }
        return controllerMerchandising.eliminar(id);
    }

    public boolean actualizarStock(int id, int stock) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("El id de merchandising debe ser mayor a cero.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        if (controllerMerchandising.buscarPorId(id) == null) {
            throw new IllegalArgumentException("No existe el producto de merchandising indicado.");
        }
        return controllerMerchandising.actualizarStock(id, stock);
    }

    public Merchandising buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("El id de merchandising debe ser mayor a cero.");
        }
        return controllerMerchandising.buscarPorId(id);
    }

    public LinkedList<Merchandising> listarTodos() throws SQLException {
        return controllerMerchandising.listar();
    }

    public LinkedList<Merchandising> listarPorConcierto(int conciertoId) throws SQLException {
        if (conciertoId <= 0) {
            throw new IllegalArgumentException("El id de concierto debe ser mayor a cero.");
        }
        return controllerMerchandising.listarPorConcierto(conciertoId);
    }

    public LinkedList<Merchandising> listarDisponiblesPorConcierto(int conciertoId) throws SQLException {
        if (conciertoId <= 0) {
            throw new IllegalArgumentException("El id de concierto debe ser mayor a cero.");
        }
        return controllerMerchandising.listarDisponiblesPorConcierto(conciertoId);
    }

    public LinkedList<VentaMerchandising> listarVentas() throws SQLException {
        return controllerMerchandising.listarVentas();
    }

    public LinkedList<VentaMerchandising> listarVentasPorOrganizador(int organizadorId) throws SQLException {
        if (organizadorId <= 0) {
            throw new IllegalArgumentException("El organizador es obligatorio.");
        }
        return controllerMerchandising.listarVentasPorOrganizador(organizadorId);
    }

    public CompraMerchandisingResultado comprarMerchandising(int compradorId, int merchandisingId,
                                                            int cantidad, String metodoPago) throws SQLException {
        Merchandising merchandising = validarCompra(compradorId, merchandisingId, cantidad, metodoPago);
        BigDecimal total = merchandising.getPrecio().multiply(BigDecimal.valueOf(cantidad));

        Connection connection = Conexion.getInstance().getConnectionOrThrow();
        boolean originalAutoCommit = connection.getAutoCommit();
        int originalIsolation = connection.getTransactionIsolation();

        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            boolean stockActualizado = controllerMerchandising.descontarStock(connection, merchandisingId, cantidad);
            if (!stockActualizado) {
                throw new IllegalArgumentException("No hay stock suficiente para el producto seleccionado.");
            }

            int compraId = controllerCompra.crear(connection, compradorId, merchandising.getConciertoId(),
                    LocalDateTime.now(), total);
            int pagoId = controllerPago.crear(connection, compraId, metodoPago, total);

            CompraMerchandising detalle = new CompraMerchandising(0, compraId, merchandisingId,
                    cantidad, merchandising.getPrecio());
            int detalleId = controllerMerchandising.registrarCompra(connection, detalle);

            connection.commit();
            merchandising.setStock(merchandising.getStock() - cantidad);
            return new CompraMerchandisingResultado(compraId, pagoId, detalleId, total, merchandising, cantidad);
        } catch (RuntimeException | SQLException e) {
            rollbackQuietly(connection);
            throw e;
        } finally {
            connection.setTransactionIsolation(originalIsolation);
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private void validarProducto(Merchandising merchandising) throws SQLException {
        if (merchandising.getConciertoId() <= 0) {
            throw new IllegalArgumentException("El concierto del producto es obligatorio.");
        }
        validarTexto(merchandising.getNombre(), "nombre");
        if (merchandising.getPrecio() == null || merchandising.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio del producto debe ser mayor a cero.");
        }
        if (merchandising.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        Concierto concierto = controllerConcierto.buscarPorId(merchandising.getConciertoId());
        if (concierto == null || "Cancelado".equals(concierto.getEstado())) {
            throw new IllegalArgumentException("El concierto no existe o esta cancelado.");
        }
    }

    private Merchandising validarCompra(int compradorId, int merchandisingId,
                                        int cantidad, String metodoPago) throws SQLException {
        if (compradorId <= 0) {
            throw new IllegalArgumentException("El comprador es obligatorio.");
        }
        if (merchandisingId <= 0) {
            throw new IllegalArgumentException("El producto de merchandising es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }
        if (!esMetodoPagoValido(metodoPago)) {
            throw new IllegalArgumentException("Metodo de pago invalido: " + metodoPago);
        }

        Usuario comprador = controllerUsuario.buscarPorId(compradorId);
        if (comprador == null || !"Comprador".equals(comprador.getRol())) {
            throw new IllegalArgumentException("El usuario comprador no existe o no tiene rol Comprador.");
        }

        Merchandising merchandising = controllerMerchandising.buscarPorId(merchandisingId);
        if (merchandising == null) {
            throw new IllegalArgumentException("No existe el producto de merchandising indicado.");
        }
        if (merchandising.getStock() < cantidad) {
            throw new IllegalArgumentException("No hay stock suficiente para el producto seleccionado.");
        }

        Concierto concierto = controllerConcierto.buscarPorId(merchandising.getConciertoId());
        if (concierto == null || !"Activo".equals(concierto.getEstado())) {
            throw new IllegalArgumentException("El concierto del producto no existe o no esta activo.");
        }
        return merchandising;
    }

    private boolean esMetodoPagoValido(String metodoPago) {
        return "Efectivo".equals(metodoPago)
                || "TarjetaCredito".equals(metodoPago)
                || "TarjetaDebito".equals(metodoPago)
                || "Transferencia".equals(metodoPago);
    }

    private void validarTexto(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + fieldName + " es obligatorio.");
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }
}
