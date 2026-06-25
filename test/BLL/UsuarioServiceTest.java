package BLL;

import DLL.ControllerUsuario;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioServiceTest {

    @Test
    void crearUsuarioNormalizaDatosYGeneraHash() throws SQLException {
        FakeControllerUsuario controller = new FakeControllerUsuario();
        UsuarioService service = new UsuarioService(controller);

        int id = service.crearUsuario(" Juan ", " Perez ", " juan@test.com ", " 123 ", "clave", "Comprador");

        assertEquals(10, id);
        assertEquals(1, controller.crearCalls);
        assertEquals("Juan", controller.created.getNombre());
        assertEquals("Perez", controller.created.getApellido());
        assertEquals("juan@test.com", controller.created.getEmail());
        assertEquals("123", controller.created.getDocumento());
        assertEquals("Comprador", controller.created.getRol());
        assertNotEquals("clave", controller.created.getPassword());
        assertTrue(controller.created.getPassword().startsWith("$2"));
    }

    @Test
    void autenticarDevuelveUsuarioConPasswordCorrecto() throws SQLException {
        FakeControllerUsuario controller = new FakeControllerUsuario();
        UsuarioService service = new UsuarioService(controller);
        service.crearUsuario("Ana", "Lopez", "ana@test.com", null, "secreto", "Comprador");

        Usuario autenticado = service.autenticar(" ana@test.com ", "secreto");

        assertNotNull(autenticado);
        assertEquals("ana@test.com", autenticado.getEmail());
        assertNull(service.autenticar("ana@test.com", "incorrecta"));
    }

    @Test
    void crearUsuarioRechazaEmailInvalidoSinCrear() {
        FakeControllerUsuario controller = new FakeControllerUsuario();
        UsuarioService service = new UsuarioService(controller);

        assertThrows(IllegalArgumentException.class,
                () -> service.crearUsuario("Juan", "Perez", "juan.test.com", null, "clave", "Comprador"));
        assertEquals(0, controller.crearCalls);
    }

    @Test
    void crearUsuarioRechazaRolInvalidoSinCrear() {
        FakeControllerUsuario controller = new FakeControllerUsuario();
        UsuarioService service = new UsuarioService(controller);

        assertThrows(IllegalArgumentException.class,
                () -> service.crearUsuario("Juan", "Perez", "juan@test.com", null, "clave", "Supervisor"));
        assertEquals(0, controller.crearCalls);
    }

    @Test
    void crearUsuarioRechazaEmailDuplicado() {
        FakeControllerUsuario controller = new FakeControllerUsuario();
        controller.usuario = new Usuario(1, "Juan", "Perez", "juan@test.com", null, "hash", "Comprador");
        UsuarioService service = new UsuarioService(controller);

        assertThrows(IllegalArgumentException.class,
                () -> service.crearUsuario("Juan", "Perez", " juan@test.com ", null, "clave", "Comprador"));
        assertEquals(0, controller.crearCalls);
    }

    @Test
    void cambiarPasswordActualizaHashConPasswordActualCorrecto() throws SQLException {
        FakeControllerUsuario controller = new FakeControllerUsuario();
        UsuarioService service = new UsuarioService(controller);
        service.crearUsuario("Ana", "Lopez", "ana@test.com", null, "secreto", "Comprador");

        boolean actualizado = service.cambiarPassword(10, "secreto", "nuevo123", "nuevo123");

        assertTrue(actualizado);
        assertEquals(1, controller.actualizarPasswordCalls);
        assertNotEquals("nuevo123", controller.usuario.getPassword());
        assertNotNull(service.autenticar("ana@test.com", "nuevo123"));
        assertNull(service.autenticar("ana@test.com", "secreto"));
    }

    @Test
    void cambiarPasswordRechazaPasswordActualIncorrectoSinActualizar() throws SQLException {
        FakeControllerUsuario controller = new FakeControllerUsuario();
        UsuarioService service = new UsuarioService(controller);
        service.crearUsuario("Ana", "Lopez", "ana@test.com", null, "secreto", "Comprador");

        assertThrows(IllegalArgumentException.class,
                () -> service.cambiarPassword(10, "incorrecto", "nuevo123", "nuevo123"));
        assertEquals(0, controller.actualizarPasswordCalls);
    }

    @Test
    void actualizarPasswordActualizaHashSinPasswordActual() throws SQLException {
        FakeControllerUsuario controller = new FakeControllerUsuario();
        controller.usuario = new Usuario(10, "Ana", "Lopez", "ana@test.com", null, "hash-antiguo", "Comprador");
        UsuarioService service = new UsuarioService(controller);

        boolean actualizado = service.actualizarPassword(10, "reset123", "reset123");

        assertTrue(actualizado);
        assertEquals(1, controller.actualizarPasswordCalls);
        assertNotEquals("reset123", controller.usuario.getPassword());
        assertNotNull(service.autenticar("ana@test.com", "reset123"));
    }

    @Test
    void actualizarPasswordRechazaConfirmacionDistintaSinActualizar() {
        FakeControllerUsuario controller = new FakeControllerUsuario();
        UsuarioService service = new UsuarioService(controller);

        assertThrows(IllegalArgumentException.class,
                () -> service.actualizarPassword(10, "nuevo123", "otro123"));
        assertEquals(0, controller.actualizarPasswordCalls);
    }

    private static class FakeControllerUsuario extends ControllerUsuario {
        private Usuario usuario;
        private Usuario created;
        private int crearCalls;
        private int actualizarPasswordCalls;

        @Override
        public Usuario buscarPorId(int id) {
            if (usuario != null && usuario.getId() == id) {
                return usuario;
            }
            return null;
        }

        @Override
        public Usuario buscarPorEmail(String email) {
            if (usuario != null && usuario.getEmail().equals(email)) {
                return usuario;
            }
            return null;
        }

        @Override
        public int crear(Usuario usuario) {
            crearCalls++;
            created = usuario;
            usuario.setId(10);
            this.usuario = usuario;
            return 10;
        }

        @Override
        public boolean actualizarPassword(int id, String passwordHash) {
            actualizarPasswordCalls++;
            if (usuario != null && usuario.getId() == id) {
                usuario.setPassword(passwordHash);
                return true;
            }
            return false;
        }
    }
}
