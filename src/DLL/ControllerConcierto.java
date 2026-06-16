package DLL;

import BLL.Concierto;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;

public class ControllerConcierto {

    private Connection getConnection() throws SQLException {
        return Conexion.getInstance().getConnectionOrThrow();
    }

    public LinkedList<Concierto> mostrarActivos() throws SQLException {
        LinkedList<Concierto> result = new LinkedList<>();
        String sql =
            "SELECT c.id, c.artista, c.fecha, c.hora, c.lugar, " +
            "       c.capacidad_total, c.organizador_id, c.estado, " +
            "       COALESCE(SUM(CASE WHEN t.estado='Disponible' THEN 1 ELSE 0 END), 0) AS disponibles " +
            "FROM concierto c " +
            "LEFT JOIN ticket t ON t.concierto_id = c.id " +
            "WHERE c.estado = 'Activo' " +
            "GROUP BY c.id, c.artista, c.fecha, c.hora, c.lugar, c.capacidad_total, c.organizador_id, c.estado " +
            "ORDER BY c.fecha, c.hora";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(mapConciertoConDisponibles(rs));
            }
        }
        return result;
    }

    public LinkedList<Concierto> listarTodos() throws SQLException {
        LinkedList<Concierto> result = new LinkedList<>();
        String sql =
            "SELECT c.id, c.artista, c.fecha, c.hora, c.lugar, " +
            "       c.capacidad_total, c.organizador_id, c.estado, " +
            "       COALESCE(SUM(CASE WHEN t.estado='Disponible' THEN 1 ELSE 0 END), 0) AS disponibles " +
            "FROM concierto c " +
            "LEFT JOIN ticket t ON t.concierto_id = c.id " +
            "GROUP BY c.id, c.artista, c.fecha, c.hora, c.lugar, c.capacidad_total, c.organizador_id, c.estado " +
            "ORDER BY c.fecha, c.hora";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapConciertoConDisponibles(rs));
            }
        }
        return result;
    }

    public Concierto buscarPorId(int id) throws SQLException {
        String sql =
            "SELECT c.id, c.artista, c.fecha, c.hora, c.lugar, " +
            "       c.capacidad_total, c.organizador_id, c.estado, " +
            "       COALESCE(SUM(CASE WHEN t.estado='Disponible' THEN 1 ELSE 0 END), 0) AS disponibles " +
            "FROM concierto c " +
            "LEFT JOIN ticket t ON t.concierto_id = c.id " +
            "WHERE c.id = ? " +
            "GROUP BY c.id, c.artista, c.fecha, c.hora, c.lugar, c.capacidad_total, c.organizador_id, c.estado";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapConciertoConDisponibles(rs);
                }
            }
        }
        return null;
    }

    public int crear(Concierto concierto) throws SQLException {
        String sql = "INSERT INTO concierto (artista, fecha, hora, lugar, capacidad_total, organizador_id, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(stmt, concierto);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    concierto.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para concierto.");
    }

    public boolean modificar(Concierto concierto) throws SQLException {
        String sql = "UPDATE concierto SET artista = ?, fecha = ?, hora = ?, lugar = ?, "
                + "capacidad_total = ?, organizador_id = ?, estado = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            fillStatement(stmt, concierto);
            stmt.setInt(8, concierto.getId());
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean cancelar(int id) throws SQLException {
        String sql = "UPDATE concierto SET estado = 'Cancelado' WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        return cancelar(id);
    }

    public boolean existeMismoDiaYLugar(LocalDate fecha, LocalTime hora, String lugar, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM concierto "
                + "WHERE fecha = ? AND hora = ? AND lugar = ? AND id <> ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(fecha));
            stmt.setTime(2, Time.valueOf(hora));
            stmt.setString(3, lugar);
            stmt.setInt(4, excludeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        }
    }

    public int contarDisponibles(int conciertoId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM ticket WHERE concierto_id = ? AND estado = 'Disponible'";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, conciertoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private void fillStatement(PreparedStatement stmt, Concierto concierto) throws SQLException {
        stmt.setString(1, concierto.getArtista());
        stmt.setDate(2, Date.valueOf(concierto.getFecha()));
        stmt.setTime(3, Time.valueOf(concierto.getHora()));
        stmt.setString(4, concierto.getLugar());
        stmt.setInt(5, concierto.getCapacidadTotal());
        if (concierto.getOrganizadorId() > 0) {
            stmt.setInt(6, concierto.getOrganizadorId());
        } else {
            stmt.setNull(6, java.sql.Types.INTEGER);
        }
        stmt.setString(7, concierto.getEstado());
    }

    private Concierto mapConciertoConDisponibles(ResultSet rs) throws SQLException {
        Concierto c = new Concierto(
            rs.getInt("id"),
            rs.getString("artista"),
            rs.getDate("fecha").toLocalDate(),
            rs.getTime("hora").toLocalTime(),
            rs.getString("lugar"),
            rs.getInt("capacidad_total"),
            rs.getInt("organizador_id"),
            rs.getString("estado")
        );
        c.setDisponibles(rs.getInt("disponibles"));
        return c;
    }
}
