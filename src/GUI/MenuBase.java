package GUI;

import BLL.Usuario;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public abstract class MenuBase extends JFrame {

    protected final Usuario usuario;

    protected MenuBase(Usuario usuario) {
        this.usuario = usuario;
    }

    protected final void inicializarMenu(String tituloVentana) {
        setTitle(tituloVentana);
        EstiloGUI.aplicarTamanioMenuRol(this);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        EstiloGUI.aplicarVentana(this);

        add(crearCabecera(), BorderLayout.NORTH);
        add(crearContenido(), BorderLayout.CENTER);
    }

    protected JComponent crearCabecera() {
        JPanel panel = new JPanel(new BorderLayout());
        EstiloGUI.aplicarPanelCabecera(panel);

        JLabel title = new JLabel(getTituloPanel(), SwingConstants.CENTER);
        EstiloGUI.aplicarTitulo(title);
        panel.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel(
                usuario.getNombre() + " " + usuario.getApellido() + " | " + usuario.getEmail(),
                SwingConstants.CENTER);
        EstiloGUI.aplicarTextoSecundario(subtitle);
        panel.add(subtitle, BorderLayout.CENTER);

        return panel;
    }

    protected abstract String getTituloPanel();

    protected abstract JComponent crearContenido();

    protected void addButton(JPanel panel, String label, String icon, java.awt.event.ActionListener action) {
        panel.add(BotonHelper.crearBoton(label, icon, action));
    }

    protected JButton crearBotonVolverLogin() {
        return BotonHelper.crearBoton("Volver al login", "logout", e -> cerrarSesion());
    }

    protected JButton crearBotonCerrarSistema() {
        return BotonHelper.crearBoton("Cerrar sistema", "exit", e -> cerrarSistema());
    }

    protected void cerrarSesion() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    protected void cerrarSistema() {
        System.exit(0);
    }
}
