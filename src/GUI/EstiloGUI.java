package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public final class EstiloGUI {

    public static final Color FONDO = new Color(245, 247, 250);
    public static final Color PANEL = new Color(255, 255, 255);
    public static final Color PRIMARIO = new Color(43, 108, 176);
    public static final Color TEXTO = new Color(31, 41, 55);
    public static final Color TEXTO_SECUNDARIO = new Color(75, 85, 99);
    public static final Color BORDE = new Color(209, 213, 219);

    public static final Font FUENTE_BASE = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 18);
    public static final Font FUENTE_BOTON = new Font("SansSerif", Font.BOLD, 14);

    public static final int MARGEN = 16;
    public static final int ESPACIADO = 12;
    public static final Dimension TAMANIO_BOTON = new Dimension(220, 44);
    public static final Dimension TAMANIO_LOGIN = new Dimension(760, 500);
    public static final Dimension TAMANIO_MENU_ROL = new Dimension(960, 720);

    private EstiloGUI() {
    }

    public static void aplicarLookAndFeel() {
        UIManager.put("Panel.background", FONDO);
        UIManager.put("Label.font", FUENTE_BASE);
        UIManager.put("Button.font", FUENTE_BOTON);
        UIManager.put("TextField.font", FUENTE_BASE);
        UIManager.put("PasswordField.font", FUENTE_BASE);
        UIManager.put("TextArea.font", FUENTE_BASE);
        UIManager.put("OptionPane.messageFont", FUENTE_BASE);
        UIManager.put("OptionPane.buttonFont", FUENTE_BOTON);
    }

    public static void aplicarVentana(JFrame ventana) {
        ventana.getContentPane().setBackground(FONDO);
    }

    public static void aplicarTamanioLogin(JFrame ventana) {
        ventana.setSize(TAMANIO_LOGIN);
        ventana.setMinimumSize(TAMANIO_LOGIN);
    }

    public static void aplicarTamanioMenuRol(JFrame ventana) {
        ventana.setSize(TAMANIO_MENU_ROL);
        ventana.setMinimumSize(TAMANIO_MENU_ROL);
    }

    public static void aplicarPanel(JPanel panel) {
        panel.setBackground(FONDO);
    }

    public static void aplicarPanelContenido(JPanel panel) {
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(MARGEN, MARGEN + 4, MARGEN + 4, MARGEN + 4));
    }

    public static void aplicarPanelCabecera(JPanel panel) {
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(MARGEN, MARGEN, 12, MARGEN)));
    }

    public static void aplicarTitulo(JLabel label) {
        label.setFont(FUENTE_TITULO);
        label.setForeground(TEXTO);
    }

    public static void aplicarTextoSecundario(JComponent component) {
        component.setFont(FUENTE_BASE);
        component.setForeground(TEXTO_SECUNDARIO);
    }

    public static void aplicarAreaTexto(JTextArea textArea) {
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFocusable(false);
        textArea.setFont(FUENTE_BASE);
        textArea.setForeground(TEXTO_SECUNDARIO);
    }

    public static void aplicarBoton(JButton boton) {
        boton.setFont(FUENTE_BOTON);
        boton.setForeground(TEXTO);
        boton.setPreferredSize(TAMANIO_BOTON);
        boton.setMinimumSize(TAMANIO_BOTON);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
    }
}
