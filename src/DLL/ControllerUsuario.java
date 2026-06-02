package DLL;

import BLL.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class ControllerUsuario {

    private Connection getConnection() throws SQLException {
        return Conexion.getInstance().getConnectionOrThrow();
    }

    public int crear(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, apellido, email, documento, password, rol) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(stmt, usuario, true);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    usuario.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("No se pudo obtener el id generado para usuario.");
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, apellido, email, documento, password, rol FROM usuario WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUsuario(rs);
                }
            }
        }
        return null;
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT id, nombre, apellido, email, documento, password, rol FROM usuario WHERE email = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUsuario(rs);
                }
            }
        }
        return null;
    }

    public LinkedList<Usuario> listar() throws SQLException {
        LinkedList<Usuario> usuarios = new LinkedList<>();
        String sql = "SELECT id, nombre, apellido, email, documento, password, rol FROM usuario ORDER BY id";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapUsuario(rs));
            }
        }
        return usuarios;
    }

    public boolean modificar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nombre = ?, apellido = ?, email = ?, documento = ?, "
                + "password = ?, rol = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            fillStatement(stmt, usuario, true);
            stmt.setInt(7, usuario.getId());
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() == 1;
        }
    }

    public boolean actualizarPassword(int id, String passwordHash) throws SQLException {
        String sql = "UPDATE usuario SET password = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, passwordHash);
            stmt.setInt(2, id);
            return stmt.executeUpdate() == 1;
        }
    }

    private void fillStatement(PreparedStatement stmt, Usuario usuario, boolean includePassword) throws SQLException {
        stmt.setString(1, usuario.getNombre());
        stmt.setString(2, usuario.getApellido());
        stmt.setString(3, usuario.getEmail());
        stmt.setString(4, usuario.getDocumento());
        if (includePassword) {
            stmt.setString(5, usuario.getPassword());
        }
        stmt.setString(6, usuario.getRol());
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("email"),
                rs.getString("documento"),
                rs.getString("password"),
                rs.getString("rol")
        );
    }
}
