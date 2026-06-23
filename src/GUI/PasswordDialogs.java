package GUI;

import BLL.Usuario;
import BLL.UsuarioService;

import java.awt.Component;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public final class PasswordDialogs {

    private PasswordDialogs() {
    }

    public static void cambiarPassword(Component parent, Usuario usuario) {
        UsuarioService usuarioService = new UsuarioService();
        while (true) {
            JPasswordField actual = new JPasswordField();
            JPasswordField nuevo = new JPasswordField();
            JPasswordField confirmacion = new JPasswordField();
            JPanel panel = crearPanel(actual, nuevo, confirmacion);

            int opcion = JOptionPane.showConfirmDialog(parent, panel,
                    "Cambiar password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }

            String passwordActual = leerPassword(actual);
            String passwordNuevo = leerPassword(nuevo);
            String confirmacionPassword = leerPassword(confirmacion);
            try {
                boolean actualizado = usuarioService.cambiarPassword(
                        usuario.getId(), passwordActual, passwordNuevo, confirmacionPassword);
                JOptionPane.showMessageDialog(parent,
                        actualizado ? "El password fue actualizado." : "No se pudo actualizar el password.",
                        "Cambiar password", JOptionPane.INFORMATION_MESSAGE);
                return;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(parent, e.getMessage(),
                        "Datos invalidos", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(parent, e.getMessage(),
                        "Error de base de datos", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    public static String pedirNuevoPassword(Component parent, String titulo) {
        while (true) {
            JPasswordField nuevo = new JPasswordField();
            JPasswordField confirmacion = new JPasswordField();
            JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
            panel.add(new JLabel("Nuevo password:"));
            panel.add(nuevo);
            panel.add(new JLabel("Confirmar password:"));
            panel.add(confirmacion);

            int opcion = JOptionPane.showConfirmDialog(parent, panel,
                    titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) {
                return null;
            }

            String passwordNuevo = leerPassword(nuevo);
            String confirmacionPassword = leerPassword(confirmacion);
            try {
                validarPassword(passwordNuevo, confirmacionPassword);
                return passwordNuevo;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(parent, e.getMessage(),
                        "Datos invalidos", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static JPanel crearPanel(JPasswordField actual,
                                     JPasswordField nuevo,
                                     JPasswordField confirmacion) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Password actual:"));
        panel.add(actual);
        panel.add(new JLabel("Nuevo password:"));
        panel.add(nuevo);
        panel.add(new JLabel("Confirmar password:"));
        panel.add(confirmacion);
        return panel;
    }

    private static String leerPassword(JPasswordField field) {
        char[] chars = field.getPassword();
        try {
            return new String(chars);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    private static void validarPassword(String passwordNuevo, String confirmacionPassword) {
        if (passwordNuevo == null || passwordNuevo.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo password nuevo es obligatorio.");
        }
        if (confirmacionPassword == null || confirmacionPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo confirmacion de password es obligatorio.");
        }
        if (!passwordNuevo.equals(confirmacionPassword)) {
            throw new IllegalArgumentException("Los passwords no coinciden.");
        }
        if (passwordNuevo.trim().length() < 4) {
            throw new IllegalArgumentException("El password debe tener al menos 4 caracteres.");
        }
    }
}

