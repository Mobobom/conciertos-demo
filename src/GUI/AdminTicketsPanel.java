package GUI;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class AdminTicketsPanel extends JPanel {

    public AdminTicketsPanel(
            ActionListener verTickets,
            ActionListener bloquearTicket,
            ActionListener liberarTicket) {
        setLayout(new GridLayout(0, 2, EstiloGUI.ESPACIADO, EstiloGUI.ESPACIADO));
        EstiloGUI.aplicarPanel(this);
        setBorder(BorderFactory.createTitledBorder("Tickets"));

        add(BotonHelper.crearBoton("Ver tickets", "ticket", verTickets));
        add(BotonHelper.crearBoton("Bloquear ticket", "exit", bloquearTicket));
        add(BotonHelper.crearBoton("Liberar ticket", "validate", liberarTicket));
    }
}
