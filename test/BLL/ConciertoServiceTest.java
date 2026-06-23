package BLL;

import DLL.ControllerConcierto;
import DLL.ControllerUsuario;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConciertoServiceTest {

    @Test
    void crearConciertoNormalizaDatosYUsaEstadoActivo() throws SQLException {
        FakeControllerConcierto controllerConcierto = new FakeControllerConcierto();
        ConciertoService service = new ConciertoService(controllerConcierto, new FakeControllerUsuario());

        int id = service.crearConcierto(" Coldplay ", LocalDate.of(2026, 6, 15),
                LocalTime.of(21, 0), " Monumental ", 60000, 0);

        assertEquals(20, id);
        assertEquals("Coldplay", controllerConcierto.created.getArtista());
        assertEquals("Monumental", controllerConcierto.created.getLugar());
        assertEquals("Activo", controllerConcierto.created.getEstado());
    }

    @Test
    void crearConciertoRechazaDuplicadoDeFechaHoraYLugar() {
        FakeControllerConcierto controllerConcierto = new FakeControllerConcierto();
        controllerConcierto.duplicated = true;
        ConciertoService service = new ConciertoService(controllerConcierto, new FakeControllerUsuario());

        assertThrows(IllegalArgumentException.class,
                () -> service.crearConcierto("Coldplay", LocalDate.of(2026, 6, 15),
                        LocalTime.of(21, 0), "Monumental", 60000, 0));
        assertEquals(0, controllerConcierto.crearCalls);
    }

    @Test
    void crearConciertoRechazaCapacidadInvalida() {
        ConciertoService service = new ConciertoService(new FakeControllerConcierto(), new FakeControllerUsuario());

        assertThrows(IllegalArgumentException.class,
                () -> service.crearConcierto("Coldplay", LocalDate.of(2026, 6, 15),
                        LocalTime.of(21, 0), "Monumental", 0, 0));
    }

    @Test
    void crearConciertoRechazaOrganizadorConRolIncorrecto() {
        FakeControllerUsuario controllerUsuario = new FakeControllerUsuario();
        controllerUsuario.usuario = new Usuario(5, "Juan", "Perez", "juan@test.com", null, "hash", "Comprador");
        ConciertoService service = new ConciertoService(new FakeControllerConcierto(), controllerUsuario);

        assertThrows(IllegalArgumentException.class,
                () -> service.crearConcierto("Coldplay", LocalDate.of(2026, 6, 15),
                        LocalTime.of(21, 0), "Monumental", 60000, 5));
    }

    @Test
    void modificarConciertoRechazaConciertoCancelado() {
        FakeControllerConcierto controllerConcierto = new FakeControllerConcierto();
        controllerConcierto.existing = new Concierto(3, "Coldplay", LocalDate.of(2026, 6, 15),
                LocalTime.of(21, 0), "Monumental", 60000, 0, "Cancelado");
        ConciertoService service = new ConciertoService(controllerConcierto, new FakeControllerUsuario());
        Concierto cambio = new Concierto(3, "Coldplay", LocalDate.of(2026, 6, 16),
                LocalTime.of(21, 0), "Monumental", 60000, 0, "Activo");

        assertThrows(IllegalArgumentException.class, () -> service.modificarConcierto(cambio));
    }

    private static class FakeControllerConcierto extends ControllerConcierto {
        private boolean duplicated;
        private Concierto created;
        private Concierto existing;
        private int crearCalls;

        @Override
        public boolean existeMismoDiaYLugar(LocalDate fecha, LocalTime hora, String lugar, int excludeId) {
            return duplicated;
        }

        @Override
        public int crear(Concierto concierto) {
            crearCalls++;
            created = concierto;
            concierto.setId(20);
            return 20;
        }

        @Override
        public Concierto buscarPorId(int id) {
            return existing;
        }
    }

    private static class FakeControllerUsuario extends ControllerUsuario {
        private Usuario usuario;

        @Override
        public Usuario buscarPorId(int id) {
            return usuario;
        }
    }
}
