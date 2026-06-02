package DLL;

import BLL.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class ControllerTicket {

    private Connection getConnection() throws SQLException {
        return Conexion.getInstance().getConnectionOrThrow();
    }

    public int crear(Ticket ticket) throws SQLException {
        String sql = "INSERT INTO ticket (concierto_id, sector_id, codigo, precio, estado, compra_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(stmt, ticket);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    ticket.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para ticket.");
    }

    public Ticket buscarPorId(int id) throws SQLException {
        String sql = baseSelect() + " WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapTicket(rs);
                }
            }
        }
        return null;
    }

    public Ticket buscarPorCodigo(String codigo) throws SQLException {
        String sql = baseSelect() + " WHERE codigo = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapTicket(rs);
                }
            }
        }
        return null;
    }

    public LinkedList<Ticket> listar() throws SQLException {
        LinkedList<Ticket> tickets = new LinkedList<>();
        String sql = baseSelect() + " ORDER BY id";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tickets.add(mapTicket(rs));
            }
        }
        return tickets;
    }

    public LinkedList<Ticket> listarPorConcierto(int conciertoId) throws SQLException {
        LinkedList<Ticket> tickets = new LinkedList<>();
        String sql = baseSelect() + " WHERE concierto_id = ? ORDER BY sector_id, id";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, conciertoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        }
        return tickets;
    }

    public LinkedList<Ticket> listarPorSector(int sectorId) throws SQLException {
        LinkedList<Ticket> tickets = new LinkedList<>();
        String sql = baseSelect() + " WHERE sector_id = ? ORDER BY id";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sectorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        }
        return tickets;
    }

    public LinkedList<Ticket> buscarDisponiblesParaCompra(Connection connection, int conciertoId,
                                                          int sectorId, int cantidad) throws SQLException {
        LinkedList<Ticket> tickets = new LinkedList<>();
        String sql = baseSelect()
                + " WHERE concierto_id = ? AND sector_id = ? AND estado = 'Disponible'"
                + " ORDER BY id LIMIT ? FOR UPDATE";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, conciertoId);
            stmt.setInt(2, sectorId);
            stmt.setInt(3, cantidad);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        }
        return tickets;
    }

    public boolean modificar(Ticket ticket) throws SQLException {
        String sql = "UPDATE ticket SET concierto_id = ?, sector_id = ?, codigo = ?, precio = ?, "
                + "estado = ?, compra_id = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            fillStatement(stmt, ticket);
            stmt.setInt(7, ticket.getId());
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM ticket WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean actualizarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE ticket SET estado = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setInt(2, id);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean bloquear(int id) throws SQLException {
        String sql = "UPDATE ticket SET estado = 'Bloqueado' WHERE id = ? AND estado = 'Disponible'";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean liberar(int id) throws SQLException {
        String sql = "UPDATE ticket SET estado = 'Disponible' WHERE id = ? AND estado = 'Bloqueado'";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        }
    }

    public int marcarVendidos(Connection connection, LinkedList<Ticket> tickets, int compraId) throws SQLException {
        String sql = "UPDATE ticket SET estado = 'Vendido', compra_id = ? WHERE id = ? AND estado = 'Disponible'";
        int updated = 0;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Ticket ticket : tickets) {
                stmt.setInt(1, compraId);
                stmt.setInt(2, ticket.getId());
                updated += stmt.executeUpdate();
                ticket.setEstado("Vendido");
                ticket.setCompraId(compraId);
            }
        }
        return updated;
    }

    public boolean marcarUsadoPorCodigo(String codigo) throws SQLException {
        String sql = "UPDATE ticket SET estado = 'Usado' WHERE codigo = ? AND estado = 'Vendido'";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, codigo);
            return stmt.executeUpdate() == 1;
        }
    }

    public int contarPorEstado(int conciertoId, String estado) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM ticket WHERE concierto_id = ? AND estado = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, conciertoId);
            stmt.setString(2, estado);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    public int contarDisponiblesPorSector(int sectorId) throws SQLException {
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

    public int contarCompradosPorCompradorConcierto(int compradorId, int conciertoId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total "
                + "FROM ticket t INNER JOIN compra c ON c.id = t.compra_id "
                + "WHERE c.comprador_id = ? AND t.concierto_id = ? AND t.estado IN ('Vendido', 'Usado')";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, compradorId);
            stmt.setInt(2, conciertoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    private String baseSelect() {
        return "SELECT id, concierto_id, sector_id, codigo, precio, estado, compra_id FROM ticket";
    }

    private void fillStatement(PreparedStatement stmt, Ticket ticket) throws SQLException {
        stmt.setInt(1, ticket.getConciertoId());
        stmt.setInt(2, ticket.getSectorId());
        stmt.setString(3, ticket.getCodigo());
        stmt.setBigDecimal(4, ticket.getPrecio());
        stmt.setString(5, ticket.getEstado());
        if (ticket.getCompraId() > 0) {
            stmt.setInt(6, ticket.getCompraId());
        } else {
            stmt.setNull(6, java.sql.Types.INTEGER);
        }
    }

    private Ticket mapTicket(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getInt("id"),
                rs.getInt("concierto_id"),
                rs.getInt("sector_id"),
                rs.getString("codigo"),
                rs.getBigDecimal("precio"),
                rs.getString("estado"),
                rs.getInt("compra_id")
        );
    }
}

