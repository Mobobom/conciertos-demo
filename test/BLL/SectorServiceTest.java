package BLL;

import DLL.ControllerConcierto;
import DLL.ControllerSector;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SectorServiceTest {

    @Test
    void crearSectorNormalizaDatos() throws SQLException {
        FakeControllerSector controllerSector = new FakeControllerSector();
        FakeControllerConcierto controllerConcierto = new FakeControllerConcierto();
        controllerConcierto.concierto = conciertoActivo();
        SectorService service = new SectorService(controllerSector, controllerConcierto);

        int id = service.crearSector(1, "VIP", " Campo delantero ", 100, new BigDecimal("55.00"));

        assertEquals(30, id);
        assertEquals("VIP", controllerSector.created.getTipo());
        assertEquals("Campo delantero", controllerSector.created.getNombre());
        assertEquals(new BigDecimal("55.00"), controllerSector.created.getPrecio());
    }

    @Test
    void crearSectorRechazaTipoInvalido() {
        FakeControllerConcierto controllerConcierto = new FakeControllerConcierto();
        controllerConcierto.concierto = conciertoActivo();
        SectorService service = new SectorService(new FakeControllerSector(), controllerConcierto);

        assertThrows(IllegalArgumentException.class,
                () -> service.crearSector(1, "General", "General", 100, new BigDecimal("20.00")));
    }

    @Test
    void crearSectorRechazaPrecioNegativo() {
        FakeControllerConcierto controllerConcierto = new FakeControllerConcierto();
        controllerConcierto.concierto = conciertoActivo();
        SectorService service = new SectorService(new FakeControllerSector(), controllerConcierto);

        assertThrows(IllegalArgumentException.class,
                () -> service.crearSector(1, "Campo", "Campo", 100, new BigDecimal("-1.00")));
    }

    @Test
    void crearSectorRechazaCapacidadTotalSuperada() {
        FakeControllerSector controllerSector = new FakeControllerSector();
        controllerSector.otherCapacity = 80;
        FakeControllerConcierto controllerConcierto = new FakeControllerConcierto();
        controllerConcierto.concierto = conciertoActivo();
        SectorService service = new SectorService(controllerSector, controllerConcierto);

        assertThrows(IllegalArgumentException.class,
                () -> service.crearSector(1, "Campo", "Campo", 30, new BigDecimal("20.00")));
        assertEquals(0, controllerSector.crearCalls);
    }

    private static Concierto conciertoActivo() {
        return new Concierto(1, "Coldplay", LocalDate.of(2026, 6, 15),
                LocalTime.of(21, 0), "Monumental", 100, 0, "Activo");
    }

    private static class FakeControllerSector extends ControllerSector {
        private int otherCapacity;
        private Sector created;
        private int crearCalls;

        @Override
        public int crear(Sector sector) {
            crearCalls++;
            created = sector;
            sector.setId(30);
            return 30;
        }

        @Override
        public int sumarCapacidadPorConcierto(int conciertoId, int excludeSectorId) {
            return otherCapacity;
        }
    }

    private static class FakeControllerConcierto extends ControllerConcierto {
        private Concierto concierto;

        @Override
        public Concierto buscarPorId(int id) {
            return concierto;
        }
    }
}

