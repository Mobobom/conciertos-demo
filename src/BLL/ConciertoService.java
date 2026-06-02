package BLL;

import DLL.ControllerConcierto;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;

public class ConciertoService {

    private final ControllerConcierto controllerConcierto;

    public ConciertoService() {
        this(new ControllerConcierto());
    }

    public ConciertoService(ControllerConcierto controllerConcierto) {
        this.controllerConcierto = controllerConcierto;
    }

    public int crearConcierto(String artista, LocalDate fecha, LocalTime hora,
                              String lugar, int capacidadTotal, int organizadorId) throws SQLException {
        Concierto concierto = new Concierto(0, artista, fecha, hora, lugar,
                capacidadTotal, organizadorId, "Activo");
        validarConcierto(concierto);
        if (controllerConcierto.existeSuperposicion(fecha, hora, lugar.trim(), 0)) {
            throw new IllegalArgumentException("Ya existe un concierto en la misma fecha, hora y lugar.");
        }
        concierto.setArtista(artista.trim());
        concierto.setLugar(lugar.trim());
        return controllerConcierto.crear(concierto);
    }

    public boolean modificarConcierto(Concierto concierto) throws SQLException {
        if (concierto.getId() <= 0) {
            throw new IllegalArgumentException("El id de concierto debe ser mayor a cero.");
        }
        validarConcierto(concierto);
        if (controllerConcierto.buscarPorId(concierto.getId()) == null) {
            throw new IllegalArgumentException("No existe el concierto indicado.");
        }
        if (controllerConcierto.existeSuperposicion(concierto.getFecha(), concierto.getHora(),
                concierto.getLugar().trim(), concierto.getId())) {
            throw new IllegalArgumentException("Ya existe otro concierto en la misma fecha, hora y lugar.");
        }
        concierto.setArtista(concierto.getArtista().trim());
        concierto.setLugar(concierto.getLugar().trim());
        return controllerConcierto.modificar(concierto);
    }

    public boolean cancelarConcierto(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("El id de concierto debe ser mayor a cero.");
        }
        return controllerConcierto.cancelar(id);
    }

    public Concierto buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("El id de concierto debe ser mayor a cero.");
        }
        return controllerConcierto.buscarPorId(id);
    }

    public LinkedList<Concierto> listarActivos() {
        return controllerConcierto.mostrarActivos();
    }

    public LinkedList<Concierto> listarTodos() throws SQLException {
        return controllerConcierto.listarTodos();
    }

    private void validarConcierto(Concierto concierto) {
        validarTexto(concierto.getArtista(), "artista");
        validarTexto(concierto.getLugar(), "lugar");
        if (concierto.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (concierto.getHora() == null) {
            throw new IllegalArgumentException("La hora es obligatoria.");
        }
        if (concierto.getCapacidadTotal() <= 0) {
            throw new IllegalArgumentException("La capacidad total debe ser mayor a cero.");
        }
        if (!"Activo".equals(concierto.getEstado()) && !"Cancelado".equals(concierto.getEstado())) {
            throw new IllegalArgumentException("Estado de concierto invalido: " + concierto.getEstado());
        }
    }

    private void validarTexto(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + fieldName + " es obligatorio.");
        }
    }
}
