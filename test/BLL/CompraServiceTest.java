package BLL;

import DLL.ControllerCompra;
import DLL.ControllerConcierto;
import DLL.ControllerPago;
import DLL.ControllerSector;
import DLL.ControllerTicket;
import DLL.ControllerUsuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompraServiceTest {

    @Test
    void comprarTicketsRechazaCantidadMayorAlLimiteAntesDeConsultarUsuario() {
        FakeControllerUsuario controllerUsuario = new FakeControllerUsuario();
        CompraService service = service(controllerUsuario, new FakeControllerConcierto(), new FakeControllerSector(), new FakeControllerTicket());

        assertThrows(IllegalArgumentException.class,
                () -> service.comprarTickets(1, 1, 1, 7, "Efectivo"));
        assertEquals(0, controllerUsuario.buscarPorIdCalls);
    }

    @Test
    void comprarTicketsRechazaMetodoPagoInvalidoAntesDeConsultarUsuario() {
        FakeControllerUsuario controllerUsuario = new FakeControllerUsuario();
        CompraService service = service(controllerUsuario, new FakeControllerConcierto(), new FakeControllerSector(), new FakeControllerTicket());

        assertThrows(IllegalArgumentException.class,
                () -> service.comprarTickets(1, 1, 1, 1, "Bitcoin"));
        assertEquals(0, controllerUsuario.buscarPorIdCalls);
    }

    @Test
    void comprarTicketsRechazaUsuarioQueNoEsComprador() {
        FakeControllerUsuario controllerUsuario = new FakeControllerUsuario();
        controllerUsuario.usuario = new Usuario(1, "Admin", "Uno", "admin@test.com", null, "hash", "Administrador");
        CompraService service = service(controllerUsuario, new FakeControllerConcierto(), new FakeControllerSector(), new FakeControllerTicket());

        assertThrows(IllegalArgumentException.class,
                () -> service.comprarTickets(1, 1, 1, 1, "Efectivo"));
        assertEquals(1, controllerUsuario.buscarPorIdCalls);
    }

    @Test
    void comprarTicketsRechazaSuperarLimitePorCompradorYConcierto() {
        FakeControllerUsuario controllerUsuario = new FakeControllerUsuario();
        controllerUsuario.usuario = new Usuario(1, "Juan", "Perez", "juan@test.com", null, "hash", "Comprador");
        FakeControllerConcierto controllerConcierto = new FakeControllerConcierto();
        controllerConcierto.concierto = conciertoActivo();
        FakeControllerSector controllerSector = new FakeControllerSector();
        controllerSector.sector = new Sector(3, 2, "Campo", "Campo", 100, new BigDecimal("50.00"));
        FakeControllerTicket controllerTicket = new FakeControllerTicket();
        controllerTicket.compradosPrevios = 5;
        CompraService service = service(controllerUsuario, controllerConcierto, controllerSector, controllerTicket);

        assertThrows(IllegalArgumentException.class,
                () -> service.comprarTickets(1, 2, 3, 2, "TarjetaDebito"));
        assertEquals(1, controllerTicket.contarCompradosCalls);
    }

    private static CompraService service(FakeControllerUsuario usuario,
                                         FakeControllerConcierto concierto,
                                         FakeControllerSector sector,
                                         FakeControllerTicket ticket) {
        return new CompraService(usuario, concierto, sector, ticket, new ControllerCompra(), new ControllerPago());
    }

    private static Concierto conciertoActivo() {
        return new Concierto(2, "Coldplay", LocalDate.of(2026, 6, 15),
                LocalTime.of(21, 0), "Monumental", 60000, 0, "Activo");
    }

    private static class FakeControllerUsuario extends ControllerUsuario {
        private Usuario usuario;
        private int buscarPorIdCalls;

        @Override
        public Usuario buscarPorId(int id) {
            buscarPorIdCalls++;
            return usuario;
        }
    }

    private static class FakeControllerConcierto extends ControllerConcierto {
        private Concierto concierto;

        @Override
        public Concierto buscarPorId(int id) {
            return concierto;
        }
    }

    private static class FakeControllerSector extends ControllerSector {
        private Sector sector;

        @Override
        public Sector buscarPorId(int id) {
            return sector;
        }
    }

    private static class FakeControllerTicket extends ControllerTicket {
        private int compradosPrevios;
        private int contarCompradosCalls;

        @Override
        public int contarCompradosPorCompradorConcierto(int compradorId, int conciertoId) {
            contarCompradosCalls++;
            return compradosPrevios;
        }
    }
}


