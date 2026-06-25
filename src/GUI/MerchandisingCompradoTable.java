package GUI;

import BLL.MerchandisingService;
import BLL.Usuario;
import BLL.VentaMerchandising;

import java.awt.Component;
import java.sql.SQLException;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MerchandisingCompradoTable {

    public static JFrame showTable(Component parent, Usuario comprador) {
        MerchandisingService merchandisingService = new MerchandisingService();

        try {
            LinkedList<VentaMerchandising> compras =
                    merchandisingService.listarComprasPorComprador(comprador.getId());
            if (compras.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "No hay compras de merchandising para este usuario.",
                        "Compras de merchandising",
                        JOptionPane.INFORMATION_MESSAGE);
                return null;
            }

            String[] columns = {"Imagen", "Concierto", "Fecha", "Producto",
                    "Cantidad", "Precio unitario", "Total", "Metodo pago"};
            return TablaConBotones.mostrar(
                    parent,
                    "Compras de merchandising - " + comprador.getNombre(),
                    columns,
                    () -> filas(compras, columns.length),
                    null,
                    new TablaConBotones.Busqueda("Buscar por concierto, producto o metodo:", 1, 3, 7));
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(parent,
                    e.getMessage(),
                    "Compras de merchandising",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent,
                    "Error de base de datos: " + e.getMessage(),
                    "Compras de merchandising",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private static Object[][] filas(LinkedList<VentaMerchandising> compras, int columnas) {
        Object[][] rows = new Object[compras.size()][columnas];
        for (int i = 0; i < compras.size(); i++) {
            VentaMerchandising compra = compras.get(i);
            rows[i][0] = ImagenHelper.cargarMiniatura(compra.getImagenUrl());
            rows[i][1] = compra.getConcierto();
            rows[i][2] = compra.getFecha() == null ? "" : compra.getFecha().toLocalDate();
            rows[i][3] = compra.getProducto();
            rows[i][4] = compra.getCantidad();
            rows[i][5] = compra.getPrecioUnitario();
            rows[i][6] = compra.getTotal();
            rows[i][7] = compra.getMetodoPago();
        }
        return rows;
    }
}
