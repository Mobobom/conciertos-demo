package GUI;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

public class AdminOtrosPanel extends JPanel {

    public AdminOtrosPanel(
            ActionListener gestionarMerchandising,
            ActionListener verVentasMerchandising,
            ActionListener gestionarUsuarios,
            ActionListener cambiarPassword,
            JButton volverLogin,
            JButton cerrarSistema) {
        setLayout(new GridLayout(0, 2, EstiloGUI.ESPACIADO, EstiloGUI.ESPACIADO));
        EstiloGUI.aplicarPanel(this);
        setBorder(BorderFactory.createTitledBorder("Administracion"));

        add(BotonHelper.crearBoton("Gestionar merchandising", "merchandising", gestionarMerchandising));
        add(BotonHelper.crearBoton("Ventas de merchandising", "report", verVentasMerchandising));
        add(BotonHelper.crearBoton("Gestionar usuarios", "user", gestionarUsuarios));
        add(BotonHelper.crearBoton("Cambiar password", "edit", cambiarPassword));
        add(volverLogin);
        add(cerrarSistema);
    }
}
