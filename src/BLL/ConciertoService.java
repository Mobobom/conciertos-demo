package BLL;

import DLL.ControllerConcierto;
import DLL.ControllerUsuario;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;

public class ConciertoService {

    private final ControllerConcierto controllerConcierto;
    private final ControllerUsuario controllerUsuario;

    public ConciertoService() {
        this(new ControllerConcierto(), new ControllerUsuario());
    }

    public ConciertoService(ControllerConcierto controllerConcierto) {
        this(controllerConcierto, new ControllerUsuario());
    }

    public ConciertoService(ControllerConcierto controllerConcierto, ControllerUsuario controllerUsuario) {
        this.controllerConcierto = controllerConcierto;
        this.controllerUsuario = controllerUsuario;
    }

    public int crearConcierto(String artista, LocalDate fecha, LocalTime hora,
                              String lugar, int capacidadTotal, int organizadorId) throws SQLException {
        Concierto concierto = new Concierto(0, artista, fecha, hora, lugar,
                capacidadTotal, organizadorId, "Activo");
        validarConcierto(concierto);
        validarOrganizador(concierto.getOrganizadorId());
        if (controllerConcierto.existeMismoDiaYLugar(fecha, hora, lugar.trim(), 0)) {
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
        validarOrganizador(concierto.getOrganizadorId());
        Concierto existente = controllerConcierto.buscarPorId(concierto.getId());
        if (existente == null) {
            throw new IllegalArgumentException("No existe el concierto indicado.");
        }
        if ("Cancelado".equals(existente.getEstado())) {
            throw new IllegalArgumentException("No se puede modificar un concierto cancelado.");
        }
        if (controllerConcierto.existeMismoDiaYLugar(concierto.getFecha(), concierto.getHora(),
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

    public LinkedList<Concierto> listarActivos() throws SQLException {
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

    private void validarOrganizador(int organizadorId) throws SQLException {
        if (organizadorId == 0) {
            return;
        }
        if (organizadorId < 0) {
            throw new IllegalArgumentException("El organizador debe ser 0 o un usuario Organizador.");
        }
        Usuario organizador = controllerUsuario.buscarPorId(organizadorId);
        if (organizador == null || !"Organizador".equals(organizador.getRol())) {
            throw new IllegalArgumentException("El organizador debe ser 0 o un usuario con rol Organizador.");
        }
    }

    private void validarTexto(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + fieldName + " es obligatorio.");
        }
    }
}
