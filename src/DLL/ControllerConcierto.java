package DLL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import BLL.Concierto;

public class ControllerConcierto {

    private static final Connection con = Conexion.getInstance().getConnection();

    public LinkedList<Concierto> mostrarActivos() {
        LinkedList<Concierto> result = new LinkedList<>();
        String sql =
            "SELECT c.id, c.artista, c.fecha, c.hora, c.lugar, " +
            "       c.capacidad_total, c.organizador_id, c.estado, " +
            "       COALESCE(SUM(CASE WHEN t.estado='Disponible' THEN 1 ELSE 0 END), 0) AS disponibles " +
            "FROM concierto c " +
            "LEFT JOIN ticket t ON t.concierto_id = c.id " +
            "WHERE c.estado = 'Activo' " +
            "GROUP BY c.id " +
            "ORDER BY c.fecha, c.hora";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
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
                result.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
