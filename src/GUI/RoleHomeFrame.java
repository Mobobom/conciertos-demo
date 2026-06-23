package GUI;

import BLL.Usuario;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class RoleHomeFrame extends JFrame {

    private final Usuario usuario;

    public RoleHomeFrame(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public void setVisible(boolean visible) {
        if (!visible) {
            super.setVisible(false);
            return;
        }
        openRoleMenu();
    }

    private void openRoleMenu() {
        String rol = usuario.getRol();
        if ("Administrador".equals(rol)) {
            new MenuAdministrador(usuario).setVisible(true);
            return;
        }
        if ("Organizador".equals(rol)) {
            new MenuOrganizador(usuario).setVisible(true);
            return;
        }
        if ("Comprador".equals(rol)) {
            new MenuComprador(usuario).setVisible(true);
            return;
        }
        if ("PersonalAcceso".equals(rol)) {
            new MenuPersonalAcceso(usuario).setVisible(true);
            return;
        }
        JOptionPane.showMessageDialog(null,
                "No hay menu definido para el rol: " + rol,
                "Rol no soportado",
                JOptionPane.INFORMATION_MESSAGE);
        new LoginFrame().setVisible(true);
    }
}

