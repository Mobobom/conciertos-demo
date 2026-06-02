package BLL;

import DLL.ControllerUsuario;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.LinkedList;

public class UsuarioService {

    private final ControllerUsuario controllerUsuario;

    public UsuarioService() {
        this(new ControllerUsuario());
    }

    public UsuarioService(ControllerUsuario controllerUsuario) {
        this.controllerUsuario = controllerUsuario;
    }

    public Usuario autenticar(String email, String passwordPlano) throws SQLException {
        validarTexto(email, "email");
        validarTexto(passwordPlano, "password");

        Usuario usuario = controllerUsuario.buscarPorEmail(email.trim());
        if (usuario == null) {
            return null;
        }

        if (passwordMatches(passwordPlano, usuario.getPassword())) {
            return usuario;
        }
        return null;
    }

    public int crearUsuario(String nombre, String apellido, String email, String documento,
                            String passwordPlano, String rol) throws SQLException {
        validarUsuario(nombre, apellido, email, passwordPlano, rol);
        if (controllerUsuario.buscarPorEmail(email.trim()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");
        }

        Usuario usuario = new Usuario(0, nombre.trim(), apellido.trim(), email.trim(),
                normalizarDocumento(documento), hashPassword(passwordPlano), rol);
        return controllerUsuario.crear(usuario);
    }

    public boolean modificarUsuario(Usuario usuario) throws SQLException {
        validarUsuario(usuario.getNombre(), usuario.getApellido(), usuario.getEmail(),
                usuario.getPassword(), usuario.getRol());
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setApellido(usuario.getApellido().trim());
        usuario.setEmail(usuario.getEmail().trim());
        usuario.setDocumento(normalizarDocumento(usuario.getDocumento()));
        usuario.setPassword(ensureHash(usuario.getPassword()));
        return controllerUsuario.modificar(usuario);
    }

    public LinkedList<Usuario> listarUsuarios() throws SQLException {
        return controllerUsuario.listar();
    }

    public Usuario buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("El id de usuario debe ser mayor a cero.");
        }
        return controllerUsuario.buscarPorId(id);
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        validarTexto(email, "email");
        return controllerUsuario.buscarPorEmail(email.trim());
    }

    public boolean eliminarUsuario(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("El id de usuario debe ser mayor a cero.");
        }
        return controllerUsuario.eliminar(id);
    }

    private void validarUsuario(String nombre, String apellido, String email,
                                String password, String rol) {
        validarTexto(nombre, "nombre");
        validarTexto(apellido, "apellido");
        validarTexto(email, "email");
        validarTexto(password, "password");
        validarTexto(rol, "rol");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("El email debe contener @.");
        }
        if (!esRolValido(rol)) {
            throw new IllegalArgumentException("Rol invalido: " + rol);
        }
    }

    private void validarTexto(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + fieldName + " es obligatorio.");
        }
    }

    private boolean esRolValido(String rol) {
        return "Administrador".equals(rol)
                || "Organizador".equals(rol)
                || "Comprador".equals(rol)
                || "PersonalAcceso".equals(rol);
    }

    private String normalizarDocumento(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return null;
        }
        return documento.trim();
    }

    private String ensureHash(String password) {
        if (password != null && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"))) {
            return password;
        }
        return hashPassword(password);
    }

    private boolean passwordMatches(String passwordPlano, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        try {
            Class<?> bcryptClass = Class.forName("org.mindrot.jbcrypt.BCrypt");
            Method checkpw = bcryptClass.getMethod("checkpw", String.class, String.class);
            return ((Boolean) checkpw.invoke(null, passwordPlano, storedHash)).booleanValue();
        } catch (Exception e) {
            return passwordPlano.equals(storedHash);
        }
    }

    private String hashPassword(String passwordPlano) {
        try {
            Class<?> bcryptClass = Class.forName("org.mindrot.jbcrypt.BCrypt");
            Method gensalt = bcryptClass.getMethod("gensalt");
            Method hashpw = bcryptClass.getMethod("hashpw", String.class, String.class);
            String salt = (String) gensalt.invoke(null);
            return (String) hashpw.invoke(null, passwordPlano, salt);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar hash bcrypt. Verificar lib/jbcrypt-0.4.jar.", e);
        }
    }
}

