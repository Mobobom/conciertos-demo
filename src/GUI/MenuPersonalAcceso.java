package GUI;

import BLL.TicketService;
import BLL.Usuario;
import BLL.ValidacionTicketResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;

public class MenuPersonalAcceso extends JFrame {

    private final Usuario usuario;
    private final TicketService ticketService;

    public MenuPersonalAcceso(Usuario usuario) {
        this.usuario = usuario;
        this.ticketService = new TicketService();
        initialize();
    }

    private void initialize() {
        setTitle("Menu Personal de Acceso - " + usuario.getNombre());
        setSize(520, 240);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JTextArea header = new JTextArea(
                "Bienvenido " + usuario.getNombre() + " " + usuario.getApellido() + "\n"
                        + "Rol: " + usuario.getRol() + "\n"
                        + "Valide codes de ticket con el boton de abajo.");
        header.setEditable(false);
        header.setOpaque(false);
        header.setFocusable(false);
        header.setFont(header.getFont().deriveFont(14f));
        panel.add(header, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        JButton validateButton = new JButton("Validar codigo de ticket");
        validateButton.addActionListener(e -> validarTicket());

        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());

        JButton exitButton = new JButton("Salir");
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(validateButton);
        panel.add(closeButton);
        panel.add(exitButton);
        return panel;
    }

    private void validarTicket() {
        String codigo = JOptionPane.showInputDialog(this,
                "Ingrese el codigo del ticket:", "Validar ticket", JOptionPane.QUESTION_MESSAGE);
        if (codigo == null || codigo.trim().isEmpty()) {
            return;
        }

        try {
            ValidacionTicketResult resultado = ticketService.validarAcceso(codigo.trim());
            int tipo = resultado.isValido() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
            StringBuilder mensaje = new StringBuilder();
            mensaje.append(resultado.getMensaje());
            if (resultado.getTicket() != null) {
                mensaje.append("\nCodigo: ").append(resultado.getTicket().getCodigo());
                mensaje.append("\nEstado: ").append(resultado.getTicket().getEstado());
            }
            JOptionPane.showMessageDialog(this, mensaje.toString(), "Resultado de validacion", tipo);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error de base de datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
