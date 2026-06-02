package BLL;

import DLL.ControllerConcierto;
import DLL.ControllerSector;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedList;

public class SectorService {

    private final ControllerSector controllerSector;
    private final ControllerConcierto controllerConcierto;

    public SectorService() {
        this(new ControllerSector(), new ControllerConcierto());
    }

    public SectorService(ControllerSector controllerSector, ControllerConcierto controllerConcierto) {
        this.controllerSector = controllerSector;
        this.controllerConcierto = controllerConcierto;
    }

    public int crearSector(int conciertoId, String tipo, String nombre,
                           int capacidad, BigDecimal precio) throws SQLException {
        Sector sector = new Sector(0, conciertoId, tipo, nombre, capacidad, precio);
        validarSector(sector);
        validarCapacidad(sector);
        sector.setTipo(tipo.trim());
        sector.setNombre(nombre.trim());
        return controllerSector.crear(sector);
    }

    public boolean modificarSector(Sector sector) throws SQLException {
        if (sector.getId() <= 0) {
            throw new IllegalArgumentException("El id de sector debe ser mayor a cero.");
        }
        validarSector(sector);
        validarCapacidad(sector);
        sector.setTipo(sector.getTipo().trim());
        sector.setNombre(sector.getNombre().trim());
        return controllerSector.modificar(sector);
    }

    public boolean eliminarSector(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("El id de sector debe ser mayor a cero.");
        }
        return controllerSector.eliminar(id);
    }

    public Sector buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("El id de sector debe ser mayor a cero.");
        }
        return controllerSector.buscarPorId(id);
    }

    public LinkedList<Sector> listarPorConcierto(int conciertoId) throws SQLException {
        if (conciertoId <= 0) {
            throw new IllegalArgumentException("El id de concierto debe ser mayor a cero.");
        }
        return controllerSector.listarPorConcierto(conciertoId);
    }

    private void validarSector(Sector sector) throws SQLException {
        if (sector.getConciertoId() <= 0) {
            throw new IllegalArgumentException("El concierto del sector es obligatorio.");
        }
        validarTexto(sector.getTipo(), "tipo");
        validarTexto(sector.getNombre(), "nombre");
        if (!esTipoValido(sector.getTipo())) {
            throw new IllegalArgumentException("Tipo de sector invalido: " + sector.getTipo());
        }
        if (sector.getCapacidad() <= 0) {
            throw new IllegalArgumentException("La capacidad del sector debe ser mayor a cero.");
        }
        if (sector.getPrecio() == null || sector.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio del sector no puede ser negativo.");
        }
        if (controllerConcierto.buscarPorId(sector.getConciertoId()) == null) {
            throw new IllegalArgumentException("No existe el concierto indicado para el sector.");
        }
    }

    private void validarCapacidad(Sector sector) throws SQLException {
        Concierto concierto = controllerConcierto.buscarPorId(sector.getConciertoId());
        int capacidadOtrosSectores = controllerSector.sumarCapacidadPorConcierto(
                sector.getConciertoId(), sector.getId());
        if (capacidadOtrosSectores + sector.getCapacidad() > concierto.getCapacidadTotal()) {
            throw new IllegalArgumentException("La suma de capacidades de sectores supera la capacidad total del concierto.");
        }
    }

    private boolean esTipoValido(String tipo) {
        return "VIP".equals(tipo)
                || "Platea".equals(tipo)
                || "Campo".equals(tipo)
                || "Preferencial".equals(tipo);
    }

    private void validarTexto(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + fieldName + " es obligatorio.");
        }
    }
}
