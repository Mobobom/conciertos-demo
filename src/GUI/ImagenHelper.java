package GUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class ImagenHelper {

    private static final String IMG_PATH = "/resources/img/";
    private static final String PLACEHOLDER = "placeholder.png";

    private ImagenHelper() {
    }

    public static ImageIcon cargarImagen(String rutaRelativa, int ancho, int alto) {
        ImageIcon icon = cargarDesdeRuta(rutaRelativa);
        if (icon == null) {
            icon = cargarDesdeRuta(PLACEHOLDER);
        }
        if (icon == null) {
            icon = crearPlaceholder(ancho, alto);
        }
        return escalar(icon, ancho, alto);
    }

    public static ImageIcon cargarPoster(String rutaRelativa) {
        return cargarImagen(rutaRelativa, 220, 300);
    }

    public static ImageIcon cargarMiniatura(String rutaRelativa) {
        return cargarImagen(rutaRelativa, 48, 48);
    }

    private static ImageIcon cargarDesdeRuta(String rutaRelativa) {
        String ruta = normalizarRuta(rutaRelativa);
        if (ruta.isEmpty()) {
            return null;
        }

        URL resource = ImagenHelper.class.getResource(IMG_PATH + ruta);
        if (resource != null) {
            return crearIcono(resource);
        }

        String[] rutasAlternativas = {
                "src/resources/img/" + ruta,
                "bin/resources/img/" + ruta,
                "target/classes/resources/img/" + ruta
        };
        for (String rutaAlternativa : rutasAlternativas) {
            File archivo = new File(rutaAlternativa);
            if (archivo.isFile()) {
                return crearIcono(archivo);
            }
        }
        return null;
    }

    private static ImageIcon crearIcono(URL resource) {
        try {
            BufferedImage imagen = ImageIO.read(resource);
            return imagen == null ? null : new ImageIcon(imagen);
        } catch (IOException e) {
            return null;
        }
    }

    private static ImageIcon crearIcono(File archivo) {
        try {
            BufferedImage imagen = ImageIO.read(archivo);
            return imagen == null ? null : new ImageIcon(imagen);
        } catch (IOException e) {
            return null;
        }
    }

    private static String normalizarRuta(String rutaRelativa) {
        if (rutaRelativa == null) {
            return "";
        }
        String ruta = rutaRelativa.trim().replace('\\', '/');
        while (ruta.startsWith("/")) {
            ruta = ruta.substring(1);
        }
        if (ruta.startsWith("resources/img/")) {
            ruta = ruta.substring("resources/img/".length());
        }
        if (ruta.startsWith("img/")) {
            ruta = ruta.substring("img/".length());
        }
        return ruta;
    }

    private static ImageIcon escalar(ImageIcon icon, int ancho, int alto) {
        if (ancho <= 0 || alto <= 0) {
            return icon;
        }

        int originalAncho = icon.getIconWidth();
        int originalAlto = icon.getIconHeight();
        if (originalAncho <= 0 || originalAlto <= 0) {
            return crearPlaceholder(ancho, alto);
        }

        double escala = Math.min((double) ancho / originalAncho, (double) alto / originalAlto);
        int nuevoAncho = Math.max(1, (int) Math.round(originalAncho * escala));
        int nuevoAlto = Math.max(1, (int) Math.round(originalAlto * escala));

        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int x = (ancho - nuevoAncho) / 2;
        int y = (alto - nuevoAlto) / 2;
        g.drawImage(icon.getImage(), x, y, nuevoAncho, nuevoAlto, null);
        g.dispose();
        return new ImageIcon(imagen);
    }

    private static ImageIcon crearPlaceholder(int ancho, int alto) {
        int width = Math.max(1, ancho);
        int height = Math.max(1, alto);
        BufferedImage imagen = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(EstiloGUI.FONDO);
        g.fillRect(0, 0, width, height);
        g.setColor(EstiloGUI.BORDE);
        g.drawRect(0, 0, width - 1, height - 1);
        g.setColor(new Color(156, 163, 175));
        int margen = Math.max(6, Math.min(width, height) / 5);
        g.drawLine(margen, height - margen, width / 2, height / 2);
        g.drawLine(width / 2, height / 2, width - margen, height - margen);
        g.drawOval(width - margen * 2, margen, margen, margen);
        g.dispose();
        return new ImageIcon(imagen);
    }
}
