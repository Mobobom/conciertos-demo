package GUI;

import BLL.Concierto;
import DLL.ControllerConcierto;
import java.awt.Component;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ShowConciertosTable {

    public static JFrame showTable(Component parent, Consumer<Concierto> onComprar) {
        ControllerConcierto controller = new ControllerConcierto();
        List<Concierto> data = new ArrayList<>();
        String[] columns = {"ID", "Artista", "Fecha", "Hora", "Lugar", "Capacidad", "Disponibles"};

        return TablaConBotones.mostrar(
                parent,
                "Conciertos disponibles",
                columns,
                () -> cargarFilas(parent, controller, data),
                (table, refrescar) -> {
                    if (onComprar == null) {
                        return Arrays.asList();
                    }
                    JButton comprar = new JButton("Comprar seleccionado");
                    comprar.addActionListener(e -> {
                        Integer id = TablaConBotones.idSeleccionado(parent, table);
                        if (id == null) {
                            return;
                        }

                        Concierto seleccionado = buscarConcierto(data, id);
                        if (seleccionado == null) {
                            JOptionPane.showMessageDialog(parent, "No se encontro el concierto.",
                                    "Comprar", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        onComprar.accept(seleccionado);
                        refrescar.run();
                    });
                    return Arrays.asList(comprar);
                },
                new TablaConBotones.Busqueda("Buscar por artista o lugar:", 1, 4),
                new TablaConBotones.FiltrosConcierto(2, 4, -1));
    }

    private static Object[][] cargarFilas(Component parent, ControllerConcierto controller, List<Concierto> data) {
        try {
            data.clear();
            data.addAll(controller.mostrarActivos());
            Object[][] rows = new Object[data.size()][7];
            for (int i = 0; i < data.size(); i++) {
                Concierto c = data.get(i);
                rows[i][0] = c.getId();
                rows[i][1] = c.getArtista();
                rows[i][2] = c.getFecha();
                rows[i][3] = c.getHora();
                rows[i][4] = c.getLugar();
                rows[i][5] = c.getCapacidadTotal();
                rows[i][6] = c.getDisponibles();
            }
            return rows;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent,
                    "Error de base de datos: " + e.getMessage(),
                    "Conciertos disponibles", JOptionPane.ERROR_MESSAGE);
            return new Object[0][0];
        }
    }

    private static Concierto buscarConcierto(List<Concierto> data, int id) {
        for (Concierto concierto : data) {
            if (concierto.getId() == id) {
                return concierto;
            }
        }
        return null;
    }
}
