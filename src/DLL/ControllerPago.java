package DLL;

import BLL.Pago;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class ControllerPago {

    private Connection getConnection() throws SQLException {
        return Conexion.getInstance().getConnectionOrThrow();
    }

    public int crear(Pago pago) throws SQLException {
        return crear(getConnection(), pago.getCompraId(), pago.getMetodo(), pago.getMonto());
    }

    public int crear(Connection connection, int compraId, String metodo, BigDecimal monto) throws SQLException {
        String sql = "INSERT INTO pago (compra_id, metodo, monto) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, compraId);
            stmt.setString(2, metodo);
            stmt.setBigDecimal(3, monto);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para pago.");
    }

    public Pago buscarPorId(int id) throws SQLException {
        String sql = baseSelect() + " WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPago(rs);
                }
            }
        }
        return null;
    }

    public Pago buscarPorCompraId(int compraId) throws SQLException {
        String sql = baseSelect() + " WHERE compra_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, compraId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPago(rs);
                }
            }
        }
        return null;
    }

    public LinkedList<Pago> listar() throws SQLException {
        LinkedList<Pago> pagos = new LinkedList<>();
        String sql = baseSelect() + " ORDER BY id";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                pagos.add(mapPago(rs));
            }
        }
        return pagos;
    }

    public boolean modificar(Pago pago) throws SQLException {
        String sql = "UPDATE pago SET compra_id = ?, metodo = ?, monto = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, pago.getCompraId());
            stmt.setString(2, pago.getMetodo());
            stmt.setBigDecimal(3, pago.getMonto());
            stmt.setInt(4, pago.getId());
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM pago WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        }
    }

    private String baseSelect() {
        return "SELECT id, compra_id, metodo, monto FROM pago";
    }

    private Pago mapPago(ResultSet rs) throws SQLException {
        return new Pago(
                rs.getInt("id"),
                rs.getInt("compra_id"),
                rs.getString("metodo"),
                rs.getBigDecimal("monto")
        );
    }
}
