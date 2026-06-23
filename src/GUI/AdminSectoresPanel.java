package GUI;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class AdminSectoresPanel extends JPanel {

    public AdminSectoresPanel(
            ActionListener verSectores,
            ActionListener crearSector,
            ActionListener crearTicketsDeSector) {
        setLayout(new GridLayout(0, 2, EstiloGUI.ESPACIADO, EstiloGUI.ESPACIADO));
        EstiloGUI.aplicarPanel(this);
        setBorder(BorderFactory.createTitledBorder("Sectores"));

        add(BotonHelper.crearBoton("Ver sectores", "concert", verSectores));
        add(BotonHelper.crearBoton("Crear sector", "add", crearSector));
        add(BotonHelper.crearBoton("Crear tickets de sector", "ticket", crearTicketsDeSector));
    }
}
