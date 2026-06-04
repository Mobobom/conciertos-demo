package GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import BLL.Concierto;
import DLL.ControllerConcierto;

public class ShowConciertosTable {

    
    public static void showTable() {
        showTable(null);
    }

    /**
     * Buyer mode when onComprar != null: adds "Comprar seleccionado" which
     * invokes the callback with the selected concierto. View mode otherwise.
     */
    public static void showTable(Consumer<Concierto> onComprar) {
        ControllerConcierto controller = new ControllerConcierto();

        String[] columns = { "ID", "Artista", "Fecha", "Hora", "Lugar", "Capacidad", "Disponibles" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Runnable recargar = () -> {
            model.setRowCount(0);
            for (Concierto c : controller.mostrarActivos()) {
                model.addRow(new Object[] {
                        c.getId(), c.getArtista(), c.getFecha(), c.getHora(),
                        c.getLugar(), c.getCapacidadTotal(), c.getDisponibles()
                });
            }
        };
        recargar.run();

        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        JFrame frame = new JFrame("Conciertos disponibles");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));
        frame.add(sp, BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        if (onComprar != null) {
            JButton comprar = new JButton("Comprar seleccionado");
            comprar.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(frame, "Seleccione un concierto.",
                            "Comprar", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int id = Integer.parseInt(table.getModel().getValueAt(row, 0).toString());
                Concierto seleccionado = buscarPorId(controller, id);
                if (seleccionado == null) {
                    JOptionPane.showMessageDialog(frame, "No se encontro el concierto.",
                            "Comprar", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                onComprar.accept(seleccionado);
                recargar.run();
            });
            bar.add(comprar);
        }

        JButton subir = new JButton("Subir");
        subir.addActionListener(e -> moverFila(frame, table, -1));
        JButton bajar = new JButton("Bajar");
        bajar.addActionListener(e -> moverFila(frame, table, 1));
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> recargar.run());
        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(e -> frame.dispose());
        bar.add(subir);
        bar.add(bajar);
        bar.add(actualizar);
        bar.add(cerrar);
        frame.add(bar, BorderLayout.SOUTH);

        frame.setSize(720, 440);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static Concierto buscarPorId(ControllerConcierto controller, int id) {
        for (Concierto c : controller.mostrarActivos()) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    private static void moverFila(JFrame frame, JTable table, int delta) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(frame, "Seleccione una fila.",
                    "Mover", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int target = row + delta;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        if (target < 0 || target >= model.getRowCount()) {
            return;
        }
        model.moveRow(row, row, target);
        table.setRowSelectionInterval(target, target);
    }
}
