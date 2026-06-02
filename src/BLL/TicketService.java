package BLL;

import DLL.ControllerSector;
import DLL.ControllerTicket;

import java.sql.SQLException;
import java.util.LinkedList;

public class TicketService {

    private final ControllerTicket controllerTicket;
    private final ControllerSector controllerSector;

    public TicketService() {
        this(new ControllerTicket(), new ControllerSector());
    }

    public TicketService(ControllerTicket controllerTicket, ControllerSector controllerSector) {
        this.controllerTicket = controllerTicket;
        this.controllerSector = controllerSector;
    }

    public boolean bloquearTicket(int ticketId) throws SQLException {
        if (ticketId <= 0) {
            throw new IllegalArgumentException("El id de ticket debe ser mayor a cero.");
        }
        return controllerTicket.bloquear(ticketId);
    }

    public boolean liberarTicket(int ticketId) throws SQLException {
        if (ticketId <= 0) {
            throw new IllegalArgumentException("El id de ticket debe ser mayor a cero.");
        }
        return controllerTicket.liberar(ticketId);
    }

    public ValidacionTicketResult validarAcceso(String codigo) throws SQLException {
        if (codigo == null || codigo.trim().isEmpty()) {
            return new ValidacionTicketResult(false, "Codigo obligatorio.", null);
        }

        Ticket ticket = controllerTicket.buscarPorCodigo(codigo.trim());
        if (ticket == null) {
            return new ValidacionTicketResult(false, "Ticket inexistente.", null);
        }

        if ("Vendido".equals(ticket.getEstado())) {
            boolean marcado = controllerTicket.marcarUsadoPorCodigo(ticket.getCodigo());
            if (marcado) {
                ticket.setEstado("Usado");
                return new ValidacionTicketResult(true, "Acceso permitido. Ticket marcado como usado.", ticket);
            }
            return new ValidacionTicketResult(false, "No se pudo actualizar el ticket.", ticket);
        }

        if ("Usado".equals(ticket.getEstado())) {
            return new ValidacionTicketResult(false, "Ticket ya utilizado.", ticket);
        }
        if ("Bloqueado".equals(ticket.getEstado())) {
            return new ValidacionTicketResult(false, "Ticket bloqueado.", ticket);
        }
        return new ValidacionTicketResult(false, "Ticket no vendido. No habilita acceso.", ticket);
    }

    public int generarTicketsParaSector(int sectorId) throws SQLException {
        if (sectorId <= 0) {
            throw new IllegalArgumentException("El id de sector debe ser mayor a cero.");
        }

        Sector sector = controllerSector.buscarPorId(sectorId);
        if (sector == null) {
            throw new IllegalArgumentException("No existe el sector indicado.");
        }

        LinkedList<Ticket> existentes = controllerTicket.listarPorSector(sectorId);
        int creados = 0;
        for (int i = existentes.size() + 1; i <= sector.getCapacidad(); i++) {
            String codigo = "T-" + sector.getConciertoId() + "-" + sector.getId() + "-" + String.format("%02d", i);
            Ticket ticket = new Ticket(0, sector.getConciertoId(), sector.getId(), codigo,
                    sector.getPrecio(), "Disponible", 0);
            controllerTicket.crear(ticket);
            creados++;
        }
        return creados;
    }

    public Ticket buscarPorCodigo(String codigo) throws SQLException {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El codigo de ticket es obligatorio.");
        }
        return controllerTicket.buscarPorCodigo(codigo.trim());
    }

    public LinkedList<Ticket> listarPorConcierto(int conciertoId) throws SQLException {
        if (conciertoId <= 0) {
            throw new IllegalArgumentException("El id de concierto debe ser mayor a cero.");
        }
        return controllerTicket.listarPorConcierto(conciertoId);
    }

    public int contarDisponiblesPorSector(int sectorId) throws SQLException {
        if (sectorId <= 0) {
            throw new IllegalArgumentException("El id de sector debe ser mayor a cero.");
        }
        return controllerTicket.contarDisponiblesPorSector(sectorId);
    }
}
