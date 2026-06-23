package BLL;

import DLL.ControllerSector;
import DLL.ControllerTicket;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketServiceTest {

    @Test
    void validarAccesoPermiteTicketVendidoYLoMarcaUsado() throws SQLException {
        FakeControllerTicket controllerTicket = new FakeControllerTicket();
        controllerTicket.ticket = new Ticket(1, 1, 1, "ABC", new BigDecimal("50.00"), "Vendido", 10);
        TicketService service = new TicketService(controllerTicket, new FakeControllerSector());

        ValidacionTicketResult result = service.validarAcceso(" ABC ");

        assertTrue(result.isValido());
        assertEquals("Usado", result.getTicket().getEstado());
        assertEquals("ABC", controllerTicket.markedCode);
    }

    @Test
    void validarAccesoRechazaCodigoVacioSinConsultarController() throws SQLException {
        FakeControllerTicket controllerTicket = new FakeControllerTicket();
        TicketService service = new TicketService(controllerTicket, new FakeControllerSector());

        ValidacionTicketResult result = service.validarAcceso("   ");

        assertFalse(result.isValido());
        assertEquals("Codigo obligatorio.", result.getMensaje());
        assertEquals(0, controllerTicket.buscarPorCodigoCalls);
    }

    @Test
    void validarAccesoRechazaTicketBloqueadoSinMarcarlo() throws SQLException {
        FakeControllerTicket controllerTicket = new FakeControllerTicket();
        controllerTicket.ticket = new Ticket(1, 1, 1, "ABC", new BigDecimal("50.00"), "Bloqueado", 0);
        TicketService service = new TicketService(controllerTicket, new FakeControllerSector());

        ValidacionTicketResult result = service.validarAcceso("ABC");

        assertFalse(result.isValido());
        assertEquals("Ticket bloqueado.", result.getMensaje());
        assertEquals(0, controllerTicket.marcarUsadoCalls);
    }

    @Test
    void generarTicketsParaSectorCreaSoloFaltantes() throws SQLException {
        FakeControllerTicket controllerTicket = new FakeControllerTicket();
        controllerTicket.existing.add(new Ticket(1, 2, 5, "T-2-5-01", new BigDecimal("30.00"), "Disponible", 0));
        FakeControllerSector controllerSector = new FakeControllerSector();
        controllerSector.sector = new Sector(5, 2, "Campo", "Campo", 3, new BigDecimal("30.00"));
        TicketService service = new TicketService(controllerTicket, controllerSector);

        int creados = service.generarTicketsParaSector(5);

        assertEquals(2, creados);
        assertEquals("T-2-5-02", controllerTicket.created.get(0).getCodigo());
        assertEquals("T-2-5-03", controllerTicket.created.get(1).getCodigo());
    }

    private static class FakeControllerTicket extends ControllerTicket {
        private Ticket ticket;
        private String markedCode;
        private int buscarPorCodigoCalls;
        private int marcarUsadoCalls;
        private final LinkedList<Ticket> existing = new LinkedList<>();
        private final LinkedList<Ticket> created = new LinkedList<>();

        @Override
        public Ticket buscarPorCodigo(String codigo) {
            buscarPorCodigoCalls++;
            if (ticket != null && ticket.getCodigo().equals(codigo)) {
                return ticket;
            }
            return null;
        }

        @Override
        public boolean marcarUsadoPorCodigo(String codigo) {
            marcarUsadoCalls++;
            markedCode = codigo;
            return true;
        }

        @Override
        public LinkedList<Ticket> listarPorSector(int sectorId) {
            return existing;
        }

        @Override
        public int crear(Ticket ticket) {
            created.add(ticket);
            ticket.setId(created.size() + 1);
            return ticket.getId();
        }
    }

    private static class FakeControllerSector extends ControllerSector {
        private Sector sector;

        @Override
        public Sector buscarPorId(int id) {
            return sector;
        }
    }
}
