package GUI;

import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public final class BotonHelper {

    private static final String ICONS_PATH = "/resources/icons/";
    private static final String PNG_EXTENSION = ".png";

    private BotonHelper() {
    }

    public static JButton crearBoton(String texto, String icono, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.setHorizontalTextPosition(SwingConstants.RIGHT);
        boton.setIconTextGap(8);

        ImageIcon imageIcon = cargarIcono(icono);
        if (imageIcon != null) {
            boton.setIcon(imageIcon);
        }

        if (accion != null) {
            boton.addActionListener(accion);
        }

        return boton;
    }

    private static ImageIcon cargarIcono(String icono) {
        if (icono == null || icono.trim().isEmpty()) {
            return null;
        }

        String nombreArchivo = icono.endsWith(PNG_EXTENSION) ? icono : icono + PNG_EXTENSION;
        URL iconUrl = BotonHelper.class.getResource(ICONS_PATH + nombreArchivo);
        return iconUrl == null ? null : new ImageIcon(iconUrl);
    }
}
