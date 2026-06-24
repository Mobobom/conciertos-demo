package GUI;

import BLL.Concierto;
import BLL.Merchandising;
import BLL.MerchandisingService;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ShowMerchandisingTable {

    private static JFrame activeFrame;

    public static JFrame showTable(Concierto concierto, Consumer<Merchandising> onComprar) {
        return showTable(null, concierto, onComprar);
    }

    public static JFrame showTable(Component parent, Concierto concierto, Consumer<Merchandising> onComprar) {
        if (concierto == null) {
            JOptionPane.showMessageDialog(parent, "Seleccione un concierto.",
                    "Catalogo de merchandising", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        if (activeFrame != null) {
            if (activeFrame.isDisplayable()) {
                activeFrame.setState(Frame.NORMAL);
                activeFrame.toFront();
                activeFrame.requestFocus();
                return activeFrame;
            }
            activeFrame = null;
        }

        MerchandisingService merchandisingService = new MerchandisingService();
        List<Merchandising> data = new ArrayList<>();
        String[] columns = {"ID", "Producto", "Precio", "Stock", "Estado"};

        JFrame frame = TablaConBotones.mostrar(
                parent,
                "Merchandising - " + concierto.getArtista(),
                columns,
                () -> cargarFilas(parent, concierto, merchandisingService, data),
                (table, recargar) -> {
                    if (onComprar == null) {
                        return Arrays.asList();
                    }
                    JButton comprar = new JButton("Comprar seleccionado");
                    comprar.addActionListener(e -> {
                        Merchandising seleccionado = obtenerSeleccionado(frameActual(), table, data);
                        if (seleccionado == null) {
                            return;
                        }
                        if (seleccionado.getStock() <= 0) {
                            JOptionPane.showMessageDialog(frameActual(),
                                    "El producto seleccionado no tiene stock disponible.",
                                    "Comprar merchandising", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        onComprar.accept(seleccionado);
                        recargar.run();
                    });
                    return Arrays.asList(comprar);
                },
                new TablaConBotones.Busqueda("Buscar por producto o estado:", 1, 4));
        activeFrame = frame;
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (activeFrame == frame) {
                    activeFrame = null;
                }
            }
        });
        return frame;
    }

    private static Object[][] cargarFilas(Component parent, Concierto concierto,
            MerchandisingService merchandisingService, List<Merchandising> data) {
        try {
            data.clear();
            data.addAll(merchandisingService.listarPorConcierto(concierto.getId()));
            Object[][] rows = new Object[data.size()][5];
            for (int i = 0; i < data.size(); i++) {
                Merchandising m = data.get(i);
                rows[i][0] = m.getId();
                rows[i][1] = m.getNombre();
                rows[i][2] = m.getPrecio();
                rows[i][3] = m.getStock();
                rows[i][4] = m.getStock() > 0 ? "Disponible" : "Agotado";
            }
            return rows;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent,
                    "Error de base de datos: " + e.getMessage(),
                    "Catalogo de merchandising", JOptionPane.ERROR_MESSAGE);
            return new Object[0][0];
        }
    }

    private static JFrame frameActual() {
        return activeFrame;
    }

    private static Merchandising obtenerSeleccionado(JFrame frame, javax.swing.JTable table, List<Merchandising> data) {
        Integer id = TablaConBotones.idSeleccionado(frame, table);
        if (id == null) {
            return null;
        }
        for (Merchandising m : data) {
            if (m.getId() == id) {
                return m;
            }
        }
        JOptionPane.showMessageDialog(frame, "No se encontro el producto seleccionado.",
                "Merchandising", JOptionPane.WARNING_MESSAGE);
        return null;
    }
}
