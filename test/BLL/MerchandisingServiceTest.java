package BLL;

import DLL.ControllerCompra;
import DLL.ControllerConcierto;
import DLL.ControllerMerchandising;
import DLL.ControllerPago;
import DLL.ControllerUsuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MerchandisingServiceTest {

    @Test
    void crearProductoNormalizaNombreYRegistraProducto() throws SQLException {
        FakeControllerMerchandising controllerMerchandising = new FakeControllerMerchandising();
        FakeControllerConcierto controllerConcierto = new FakeControllerConcierto();
        controllerConcierto.concierto = concierto("Activo");
        MerchandisingService service = service(controllerMerchandising, controllerConcierto, new FakeControllerUsuario());

        int id = service.crearProducto(1, " Remera Coldplay ", new BigDecimal("35.00"), 100);

        assertEquals(40, id);
        assertEquals("Remera Coldplay", controllerMerchandising.created.getNombre());
        assertEquals(new BigDecimal("35.00"), controllerMerchandising.created.getPrecio());
        assertEquals(100, controllerMerchandising.created.getStock());
    }

    @Test
    void crearProductoRechazaPrecioNoPositivo() {
        FakeControllerConcierto controllerConcierto = new FakeControllerConcierto();
        controllerConcierto.concierto = concierto("Activo");
        MerchandisingService service = service(new FakeControllerMerchandising(), controllerConcierto, new FakeControllerUsuario());

        assertThrows(IllegalArgumentException.class,
                () -> service.crearProducto(1, "Poster", BigDecimal.ZERO, 10));
    }

    @Test
    void crearProductoRechazaConciertoCancelado() {
        FakeControllerMerchandising controllerMerchandising = new FakeControllerMerchandising();
        FakeControllerConcierto controllerConcierto = new FakeControllerConcierto();
        controllerConcierto.concierto = concierto("Cancelado");
        MerchandisingService service = service(controllerMerchandising, controllerConcierto, new FakeControllerUsuario());

        assertThrows(IllegalArgumentException.class,
                () -> service.crearProducto(1, "Poster", new BigDecimal("15.00"), 10));
        assertEquals(0, controllerMerchandising.crearCalls);
    }

    @Test
    void actualizarStockRechazaStockNegativoAntesDeConsultarProducto() {
        FakeControllerMerchandising controllerMerchandising = new FakeControllerMerchandising();
        MerchandisingService service = service(controllerMerchandising, new FakeControllerConcierto(), new FakeControllerUsuario());

        assertThrows(IllegalArgumentException.class, () -> service.actualizarStock(1, -1));
        assertEquals(0, controllerMerchandising.buscarPorIdCalls);
    }

    @Test
    void comprarMerchandisingRechazaMetodoPagoInvalidoAntesDeConsultarUsuario() {
        FakeControllerUsuario controllerUsuario = new FakeControllerUsuario();
        MerchandisingService service = service(new FakeControllerMerchandising(), new FakeControllerConcierto(), controllerUsuario);

        assertThrows(IllegalArgumentException.class,
                () -> service.comprarMerchandising(1, 1, 1, "Bitcoin"));
        assertEquals(0, controllerUsuario.buscarPorIdCalls);
    }

    private static MerchandisingService service(FakeControllerMerchandising merchandising,
                                                FakeControllerConcierto concierto,
                                                FakeControllerUsuario usuario) {
        return new MerchandisingService(merchandising, concierto, usuario, new ControllerCompra(), new ControllerPago());
    }

    private static Concierto concierto(String estado) {
        return new Concierto(1, "Coldplay", LocalDate.of(2026, 6, 15),
                LocalTime.of(21, 0), "Monumental", 60000, 0, estado);
    }

    private static class FakeControllerMerchandising extends ControllerMerchandising {
        private Merchandising created;
        private int crearCalls;
        private int buscarPorIdCalls;

        @Override
        public int crear(Merchandising merchandising) {
            crearCalls++;
            created = merchandising;
            merchandising.setId(40);
            return 40;
        }

        @Override
        public Merchandising buscarPorId(int id) {
            buscarPorIdCalls++;
            return null;
        }
    }

    private static class FakeControllerConcierto extends ControllerConcierto {
        private Concierto concierto;

        @Override
        public Concierto buscarPorId(int id) {
            return concierto;
        }
    }

    private static class FakeControllerUsuario extends ControllerUsuario {
        private int buscarPorIdCalls;

        @Override
        public Usuario buscarPorId(int id) {
            buscarPorIdCalls++;
            return null;
        }
    }
}

