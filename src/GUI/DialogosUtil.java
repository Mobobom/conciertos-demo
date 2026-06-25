package GUI;

import java.awt.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import javax.swing.JOptionPane;

public final class DialogosUtil {

    private DialogosUtil() {
    }

    public static String pedirTexto(Component parent, String campo, String valorInicial) {
        String valor = pedirTextoConDefault(parent, campo, valorInicial);
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    public static String pedirTextoOpcional(Component parent, String campo, String valorInicial) {
        String valor = (String) JOptionPane.showInputDialog(parent,
                "Ingrese " + campo,
                campo,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                valorInicial == null ? "" : valorInicial);
        if (valor == null) {
            throw new IllegalArgumentException("Operacion cancelada.");
        }
        if (valor.trim().isEmpty()) {
            return null;
        }
        return valor.trim();
    }

    public static int pedirEntero(Component parent, String campo, String valorInicial) {
        String valor = (String) JOptionPane.showInputDialog(parent,
                "Ingrese " + campo,
                campo,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                valorInicial);
        if (valor == null) {
            throw new IllegalArgumentException("Operacion cancelada.");
        }
        String texto = valor.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un numero entero valido.");
        }
    }

    public static BigDecimal pedirPrecio(Component parent, String valorInicial) {
        String valor = pedirTexto(parent, "Precio (ej. 100.00)", valorInicial);
        try {
            return new BigDecimal(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Precio invalido.");
        }
    }

    public static LocalDate pedirFecha(Component parent, String campo, String valorInicial) {
        String valor = pedirTextoConDefault(parent, campo, valorInicial);
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de fecha invalido. Use yyyy-MM-dd.");
        }
    }

    public static LocalTime pedirHora(Component parent, String campo, String valorInicial) {
        String valor = pedirTextoConDefault(parent, campo, valorInicial);
        try {
            return LocalTime.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de hora invalido. Use HH:mm.");
        }
    }

    public static String pedirTextoConDefault(Component parent, String campo, String valorInicial) {
        String valor = (String) JOptionPane.showInputDialog(parent,
                "Ingrese " + campo,
                campo,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                valorInicial);
        if (valor == null) {
            throw new IllegalArgumentException("Operacion cancelada.");
        }
        if (valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        return valor.trim();
    }
}
