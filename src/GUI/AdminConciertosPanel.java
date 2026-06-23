package GUI;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class AdminConciertosPanel extends JPanel {

    public AdminConciertosPanel(
            ActionListener listarActivos,
            ActionListener listarTodos,
            ActionListener crear,
            ActionListener modificar,
            ActionListener cancelar,
            ActionListener verDisponibilidad) {
        setLayout(new GridLayout(0, 2, EstiloGUI.ESPACIADO, EstiloGUI.ESPACIADO));
        EstiloGUI.aplicarPanel(this);
        setBorder(BorderFactory.createTitledBorder("Conciertos"));

        add(BotonHelper.crearBoton("Listar conciertos activos", "search", listarActivos));
        add(BotonHelper.crearBoton("Listar todos los conciertos", "report", listarTodos));
        add(BotonHelper.crearBoton("Crear concierto", "add", crear));
        add(BotonHelper.crearBoton("Modificar concierto", "edit", modificar));
        add(BotonHelper.crearBoton("Cancelar concierto", "exit", cancelar));
        add(BotonHelper.crearBoton("Ver disponibilidad", "search", verDisponibilidad));
    }
}
