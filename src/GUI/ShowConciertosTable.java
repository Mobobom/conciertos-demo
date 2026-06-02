package GUI;

import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import BLL.Concierto;
import DLL.ControllerConcierto;

public class ShowConciertosTable {

    public static void showTable() {
        ControllerConcierto controller = new ControllerConcierto();
        LinkedList<Concierto> data = controller.mostrarActivos();

        JFrame frame = new JFrame("Conciertos disponibles");
        String[] columns = { "Artista", "Fecha", "Hora", "Lugar", "Capacidad", "Disponibles" };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Concierto c : data) {
            model.addRow(new Object[] {
                c.getArtista(),
                c.getFecha(),
                c.getHora(),
                c.getLugar(),
                c.getCapacidadTotal(),
                c.getDisponibles()
            });
        }

        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        frame.add(sp);

        frame.setSize(700, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
