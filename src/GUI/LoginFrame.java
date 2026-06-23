package GUI;

import BLL.Usuario;
import BLL.UsuarioService;
import GUI.RoleHomeFrame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Arrays;

public class LoginFrame extends JFrame {

    private final UsuarioService usuarioService;
    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame() {
        this.usuarioService = new UsuarioService();
        initialize();
    }

    private void initialize() {
        setTitle("Sistema de Tickets - Login");
        EstiloGUI.aplicarTamanioLogin(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        EstiloGUI.aplicarVentana(this);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        EstiloGUI.aplicarPanelCabecera(panel);

        JLabel title = new JLabel("Sistema de Gestion y Venta de Tickets", SwingConstants.CENTER);
        EstiloGUI.aplicarTitulo(title);
        panel.add(title, BorderLayout.NORTH);

        JTextArea help = new JTextArea(
                "Usuarios de prueba:\n"
                        + "admin@ticket.com / admin123\n"
                        + "org@ticket.com / org123\n"
                        + "acceso@ticket.com / acceso123\n"
                        + "juan@mail.com / 1234");
        EstiloGUI.aplicarAreaTexto(help);
        panel.add(help, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        EstiloGUI.aplicarPanelContenido(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Email:"), gbc);

        emailField = new JTextField("admin@ticket.com", 24);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Password:"), gbc);

        passwordField = new JPasswordField("admin123", 24);
        passwordField.addActionListener(e -> login());
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(passwordField, gbc);

        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel();
        EstiloGUI.aplicarPanelContenido(panel);

        JButton loginButton = BotonHelper.crearBoton("Ingresar", "login", e -> login());
        JButton clearButton = BotonHelper.crearBoton("Limpiar", "delete", e -> clearForm());
        JButton exitButton = BotonHelper.crearBoton("Salir", "exit", e -> System.exit(0));

        panel.add(loginButton);
        panel.add(clearButton);
        panel.add(exitButton);
        return panel;
    }

    private void login() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);

        try {
            Usuario usuario = usuarioService.autenticar(email, password);
            if (usuario == null) {
                JOptionPane.showMessageDialog(this,
                        "Email o password incorrectos.",
                        "Login invalido",
                        JOptionPane.WARNING_MESSAGE);
                passwordField.selectAll();
                passwordField.requestFocusInWindow();
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Bienvenido, " + usuario.getNombre() + " (" + usuario.getRol() + ").",
                    "Login correcto",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();
            RoleHomeFrame roleHomeFrame = new RoleHomeFrame(usuario);
            roleHomeFrame.setVisible(true);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo autenticar contra la base de datos:\n" + e.getMessage(),
                    "Error JDBC",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    private void clearForm() {
        emailField.setText("");
        passwordField.setText("");
        emailField.requestFocusInWindow();
    }
}
