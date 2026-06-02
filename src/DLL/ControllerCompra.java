package DLL;

import BLL.Compra;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedList;

public class ControllerCompra {

    private Connection getConnection() throws SQLException {
        return Conexion.getInstance().getConnectionOrThrow();
    }

    public int crear(Compra compra) throws SQLException {
        return crear(getConnection(), compra.getCompradorId(), compra.getConciertoId(), compra.getFecha(), compra.getTotal());
    }

    public int crear(Connection connection, int compradorId, int conciertoId,
                     LocalDateTime fecha, BigDecimal total) throws SQLException {
        String sql = "INSERT INTO compra (comprador_id, concierto_id, fecha, total) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, compradorId);
            stmt.setInt(2, conciertoId);
            stmt.setTimestamp(3, Timestamp.valueOf(fecha));
            stmt.setBigDecimal(4, total);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para compra.");
    }

    public Compra buscarPorId(int id) throws SQLException {
        String sql = baseSelect() + " WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapCompra(rs);
                }
            }
        }
        return null;
    }

    public LinkedList<Compra> listar() throws SQLException {
        LinkedList<Compra> compras = new LinkedList<>();
        String sql = baseSelect() + " ORDER BY id";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                compras.add(mapCompra(rs));
            }
        }
        return compras;
    }

    public LinkedList<Compra> listarPorComprador(int compradorId) throws SQLException {
        LinkedList<Compra> compras = new LinkedList<>();
        String sql = baseSelect() + " WHERE comprador_id = ? ORDER BY fecha DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, compradorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    compras.add(mapCompra(rs));
                }
            }
        }
        return compras;
    }

    public boolean modificar(Compra compra) throws SQLException {
        String sql = "UPDATE compra SET comprador_id = ?, concierto_id = ?, fecha = ?, total = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, compra.getCompradorId());
            stmt.setInt(2, compra.getConciertoId());
            stmt.setTimestamp(3, Timestamp.valueOf(compra.getFecha()));
            stmt.setBigDecimal(4, compra.getTotal());
            stmt.setInt(5, compra.getId());
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM compra WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        }
    }

    private String baseSelect() {
        return "SELECT id, comprador_id, concierto_id, fecha, total FROM compra";
    }

    private Compra mapCompra(ResultSet rs) throws SQLException {
        return new Compra(
                rs.getInt("id"),
                rs.getInt("comprador_id"),
                rs.getInt("concierto_id"),
                rs.getTimestamp("fecha").toLocalDateTime(),
                rs.getBigDecimal("total")
        );
    }
}
