package BLL;

import DLL.Conexion;
import DLL.ControllerCompra;
import DLL.ControllerConcierto;
import DLL.ControllerPago;
import DLL.ControllerSector;
import DLL.ControllerTicket;
import DLL.ControllerUsuario;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;

public class CompraService {

    private static final int MAX_TICKETS_POR_COMPRADOR_CONCIERTO = 6;

    private final ControllerUsuario controllerUsuario;
    private final ControllerConcierto controllerConcierto;
    private final ControllerSector controllerSector;
    private final ControllerTicket controllerTicket;
    private final ControllerCompra controllerCompra;
    private final ControllerPago controllerPago;

    public CompraService() {
        this(new ControllerUsuario(), new ControllerConcierto(), new ControllerSector(),
                new ControllerTicket(), new ControllerCompra(), new ControllerPago());
    }

    public CompraService(ControllerUsuario controllerUsuario,
                         ControllerConcierto controllerConcierto,
                         ControllerSector controllerSector,
                         ControllerTicket controllerTicket,
                         ControllerCompra controllerCompra,
                         ControllerPago controllerPago) {
        this.controllerUsuario = controllerUsuario;
        this.controllerConcierto = controllerConcierto;
        this.controllerSector = controllerSector;
        this.controllerTicket = controllerTicket;
        this.controllerCompra = controllerCompra;
        this.controllerPago = controllerPago;
    }

    public CompraResultado comprarTickets(int compradorId, int conciertoId, int sectorId,
                                          int cantidad, String metodoPago) throws SQLException {
        validarSolicitud(compradorId, conciertoId, sectorId, cantidad, metodoPago);

        int compradosPrevios = controllerTicket.contarCompradosPorCompradorConcierto(compradorId, conciertoId);
        if (compradosPrevios + cantidad > MAX_TICKETS_POR_COMPRADOR_CONCIERTO) {
            throw new IllegalArgumentException("El comprador no puede superar "
                    + MAX_TICKETS_POR_COMPRADOR_CONCIERTO + " tickets para el mismo concierto.");
        }

        Connection connection = Conexion.getInstance().getConnectionOrThrow();
        boolean originalAutoCommit = connection.getAutoCommit();
        int originalIsolation = connection.getTransactionIsolation();

        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            LinkedList<Ticket> tickets = controllerTicket.buscarDisponiblesParaCompra(
                    connection, conciertoId, sectorId, cantidad);
            if (tickets.size() < cantidad) {
                throw new IllegalArgumentException("No hay suficientes tickets disponibles en el sector seleccionado.");
            }

            BigDecimal total = calcularTotal(tickets);
            int compraId = controllerCompra.crear(connection, compradorId, conciertoId, LocalDateTime.now(), total);
            int pagoId = controllerPago.crear(connection, compraId, metodoPago, total);

            int actualizados = controllerTicket.marcarVendidos(connection, tickets, compraId);
            if (actualizados != tickets.size()) {
                throw new IllegalStateException("No se pudieron vender todos los tickets seleccionados.");
            }

            connection.commit();
            return new CompraResultado(compraId, pagoId, total, tickets);
        } catch (RuntimeException | SQLException e) {
            rollbackQuietly(connection);
            throw e;
        } finally {
            connection.setTransactionIsolation(originalIsolation);
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private void validarSolicitud(int compradorId, int conciertoId, int sectorId,
                                  int cantidad, String metodoPago) throws SQLException {
        if (compradorId <= 0) {
            throw new IllegalArgumentException("El comprador es obligatorio.");
        }
        if (conciertoId <= 0) {
            throw new IllegalArgumentException("El concierto es obligatorio.");
        }
        if (sectorId <= 0) {
            throw new IllegalArgumentException("El sector es obligatorio.");
        }
        if (cantidad <= 0 || cantidad > MAX_TICKETS_POR_COMPRADOR_CONCIERTO) {
            throw new IllegalArgumentException("La cantidad debe estar entre 1 y "
                    + MAX_TICKETS_POR_COMPRADOR_CONCIERTO + ".");
        }
        if (!esMetodoPagoValido(metodoPago)) {
            throw new IllegalArgumentException("Metodo de pago invalido: " + metodoPago);
        }

        Usuario comprador = controllerUsuario.buscarPorId(compradorId);
        if (comprador == null || !"Comprador".equals(comprador.getRol())) {
            throw new IllegalArgumentException("El usuario comprador no existe o no tiene rol Comprador.");
        }

        Concierto concierto = controllerConcierto.buscarPorId(conciertoId);
        if (concierto == null || !"Activo".equals(concierto.getEstado())) {
            throw new IllegalArgumentException("El concierto no existe o no esta activo.");
        }

        Sector sector = controllerSector.buscarPorId(sectorId);
        if (sector == null || sector.getConciertoId() != conciertoId) {
            throw new IllegalArgumentException("El sector no existe o no pertenece al concierto seleccionado.");
        }
    }

    private boolean esMetodoPagoValido(String metodoPago) {
        return "Efectivo".equals(metodoPago)
                || "TarjetaCredito".equals(metodoPago)
                || "TarjetaDebito".equals(metodoPago)
                || "Transferencia".equals(metodoPago);
    }

    private BigDecimal calcularTotal(LinkedList<Ticket> tickets) {
        BigDecimal total = BigDecimal.ZERO;
        for (Ticket ticket : tickets) {
            total = total.add(ticket.getPrecio());
        }
        return total;
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }
}

