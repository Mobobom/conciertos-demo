package DLL;

import BLL.CompraMerchandising;
import BLL.Merchandising;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class ControllerMerchandising {

    private Connection getConnection() throws SQLException {
        return Conexion.getInstance().getConnectionOrThrow();
    }

    public int crear(Merchandising merchandising) throws SQLException {
        String sql = "INSERT INTO merchandising (concierto_id, nombre, precio, stock, imagen_url) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(stmt, merchandising);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    merchandising.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para merchandising.");
    }

    public Merchandising buscarPorId(int id) throws SQLException {
        String sql = baseSelect() + " WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapMerchandising(rs);
                }
            }
        }
        return null;
    }

    public LinkedList<Merchandising> listar() throws SQLException {
        LinkedList<Merchandising> productos = new LinkedList<>();
        String sql = baseSelect() + " ORDER BY concierto_id, nombre";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                productos.add(mapMerchandising(rs));
            }
        }
        return productos;
    }

    public LinkedList<Merchandising> listarPorConcierto(int conciertoId) throws SQLException {
        LinkedList<Merchandising> productos = new LinkedList<>();
        String sql = baseSelect() + " WHERE concierto_id = ? ORDER BY nombre";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, conciertoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapMerchandising(rs));
                }
            }
        }
        return productos;
    }

    public LinkedList<Merchandising> listarDisponiblesPorConcierto(int conciertoId) throws SQLException {
        LinkedList<Merchandising> productos = new LinkedList<>();
        String sql = baseSelect() + " WHERE concierto_id = ? AND stock > 0 ORDER BY nombre";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, conciertoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapMerchandising(rs));
                }
            }
        }
        return productos;
    }

    public boolean modificar(Merchandising merchandising) throws SQLException {
        String sql = "UPDATE merchandising SET concierto_id = ?, nombre = ?, precio = ?, stock = ?, imagen_url = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            fillStatement(stmt, merchandising);
            stmt.setInt(6, merchandising.getId());
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM merchandising WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean actualizarStock(int id, int stock) throws SQLException {
        String sql = "UPDATE merchandising SET stock = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, stock);
            stmt.setInt(2, id);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean descontarStock(Connection connection, int merchandisingId, int cantidad) throws SQLException {
        String sql = "UPDATE merchandising SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setInt(2, merchandisingId);
            stmt.setInt(3, cantidad);
            return stmt.executeUpdate() == 1;
        }
    }

    public int registrarCompra(Connection connection, CompraMerchandising compraMerchandising) throws SQLException {
        String sql = "INSERT INTO compra_merchandising (compra_id, merchandising_id, cantidad, precio_unitario) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, compraMerchandising.getCompraId());
            stmt.setInt(2, compraMerchandising.getMerchandisingId());
            stmt.setInt(3, compraMerchandising.getCantidad());
            stmt.setBigDecimal(4, compraMerchandising.getPrecioUnitario());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    compraMerchandising.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para compra_merchandising.");
    }

    public LinkedList<CompraMerchandising> listarComprasPorCompra(int compraId) throws SQLException {
        LinkedList<CompraMerchandising> compras = new LinkedList<>();
        String sql = baseCompraMerchandisingSelect() + " WHERE compra_id = ? ORDER BY id";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, compraId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    compras.add(mapCompraMerchandising(rs));
                }
            }
        }
        return compras;
    }

    private String baseSelect() {
        return "SELECT id, concierto_id, nombre, precio, stock, imagen_url FROM merchandising";
    }

    private String baseCompraMerchandisingSelect() {
        return "SELECT id, compra_id, merchandising_id, cantidad, precio_unitario FROM compra_merchandising";
    }

    private void fillStatement(PreparedStatement stmt, Merchandising merchandising) throws SQLException {
        stmt.setInt(1, merchandising.getConciertoId());
        stmt.setString(2, merchandising.getNombre());
        stmt.setBigDecimal(3, merchandising.getPrecio());
        stmt.setInt(4, merchandising.getStock());
        stmt.setString(5, normalizarRutaImagen(merchandising.getImagenUrl()));
    }

    private Merchandising mapMerchandising(ResultSet rs) throws SQLException {
        return new Merchandising(
                rs.getInt("id"),
                rs.getInt("concierto_id"),
                rs.getString("nombre"),
                rs.getBigDecimal("precio"),
                rs.getInt("stock"),
                rs.getString("imagen_url")
        );
    }

    private CompraMerchandising mapCompraMerchandising(ResultSet rs) throws SQLException {
        return new CompraMerchandising(
                rs.getInt("id"),
                rs.getInt("compra_id"),
                rs.getInt("merchandising_id"),
                rs.getInt("cantidad"),
                rs.getBigDecimal("precio_unitario")
        );
    }

    private String normalizarRutaImagen(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            return null;
        }
        return ruta.trim();
    }
}
