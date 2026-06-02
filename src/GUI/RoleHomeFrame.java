package GUI;

import BLL.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JOptionPane;
import java.awt.FlowLayout;
import java.awt.BorderLayout;

public class RoleHomeFrame extends JFrame {

    private final Usuario usuario;

    public RoleHomeFrame(Usuario usuario) {
        this.usuario = usuario;
        initialize();
    }

    private void initialize() {
        setTitle("Menu principal - " + usuario.getRol());
        setSize(560, 360);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JLabel title = new JLabel("Sesion iniciada", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(17f));
        panel.add(title, BorderLayout.NORTH);

        JLabel userLabel = new JLabel(
                usuario.getNombre() + " " + usuario.getApellido()
                        + " | " + usuario.getEmail()
                        + " | Rol: " + usuario.getRol(),
                SwingConstants.CENTER);
        panel.add(userLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        JButton concertsButton = new JButton("Ver conciertos disponibles");
        concertsButton.addActionListener(e -> ShowConciertosTable.showTable());

        JButton roleButton = new JButton("Ir a menu de rol");
        roleButton.addActionListener(e -> openRoleMenu());

        JButton logoutButton = new JButton("Cerrar sesion");
        logoutButton.addActionListener(e -> logout());

        JButton exitButton = new JButton("Salir");
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(concertsButton);
        panel.add(roleButton);
        panel.add(logoutButton);
        panel.add(exitButton);
        return panel;
    }

    private void openRoleMenu() {
        if ("Comprador".equals(usuario.getRol())) {
            MenuComprador menu = new MenuComprador(usuario);
            menu.setVisible(true);
            return;
        }
        if ("PersonalAcceso".equals(usuario.getRol())) {
            MenuPersonalAcceso menu = new MenuPersonalAcceso(usuario);
            menu.setVisible(true);
            return;
        }
        JOptionPane.showMessageDialog(this,
                "No hay un menu configurado para el rol: " + usuario.getRol(),
                "Rol sin menu", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        dispose();
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }
}


