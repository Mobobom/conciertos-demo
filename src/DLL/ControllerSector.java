package DLL;

import BLL.Sector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class ControllerSector {

    private Connection getConnection() throws SQLException {
        return Conexion.getInstance().getConnectionOrThrow();
    }

    public int crear(Sector sector) throws SQLException {
        String sql = "INSERT INTO sector (concierto_id, tipo, nombre, capacidad, precio) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(stmt, sector);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    sector.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para sector.");
    }

    public Sector buscarPorId(int id) throws SQLException {
        String sql = baseSelect() + " WHERE s.id = ? GROUP BY s.id, s.concierto_id, s.tipo, s.nombre, s.capacidad, s.precio";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapSector(rs);
                }
            }
        }
        return null;
    }

    public LinkedList<Sector> listar() throws SQLException {
        LinkedList<Sector> sectores = new LinkedList<>();
        String sql = baseSelect()
                + " GROUP BY s.id, s.concierto_id, s.tipo, s.nombre, s.capacidad, s.precio"
                + " ORDER BY s.concierto_id, s.id";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sectores.add(mapSector(rs));
            }
        }
        return sectores;
    }

    public LinkedList<Sector> listarPorConcierto(int conciertoId) throws SQLException {
        LinkedList<Sector> sectores = new LinkedList<>();
        String sql = baseSelect()
                + " WHERE s.concierto_id = ?"
                + " GROUP BY s.id, s.concierto_id, s.tipo, s.nombre, s.capacidad, s.precio"
                + " ORDER BY s.id";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, conciertoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sectores.add(mapSector(rs));
                }
            }
        }
        return sectores;
    }

    public boolean modificar(Sector sector) throws SQLException {
        String sql = "UPDATE sector SET concierto_id = ?, tipo = ?, nombre = ?, capacidad = ?, precio = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            fillStatement(stmt, sector);
            stmt.setInt(6, sector.getId());
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM sector WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        }
    }

    public int sumarCapacidadPorConcierto(int conciertoId, int excludeSectorId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(capacidad), 0) AS total FROM sector WHERE concierto_id = ? AND id <> ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, conciertoId);
            stmt.setInt(2, excludeSectorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    public int contarDisponibles(int sectorId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM ticket WHERE sector_id = ? AND estado = 'Disponible'";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sectorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private String baseSelect() {
        return "SELECT s.id, s.concierto_id, s.tipo, s.nombre, s.capacidad, s.precio, "
                + "COALESCE(SUM(CASE WHEN t.estado = 'Disponible' THEN 1 ELSE 0 END), 0) AS disponibles "
                + "FROM sector s LEFT JOIN ticket t ON t.sector_id = s.id";
    }

    private void fillStatement(PreparedStatement stmt, Sector sector) throws SQLException {
        stmt.setInt(1, sector.getConciertoId());
        stmt.setString(2, sector.getTipo());
        stmt.setString(3, sector.getNombre());
        stmt.setInt(4, sector.getCapacidad());
        stmt.setBigDecimal(5, sector.getPrecio());
    }

    private Sector mapSector(ResultSet rs) throws SQLException {
        Sector sector = new Sector(
                rs.getInt("id"),
                rs.getInt("concierto_id"),
                rs.getString("tipo"),
                rs.getString("nombre"),
                rs.getInt("capacidad"),
                rs.getBigDecimal("precio")
        );
        sector.setDisponibles(rs.getInt("disponibles"));
        return sector;
    }
}
