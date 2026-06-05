package GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    public static void showTable(Consumer<Concierto> onComprar) {
        ControllerConcierto controller = new ControllerConcierto();
        List<Concierto> data = new ArrayList<>();

        String[] columns = { "ID", "Artista", "Fecha", "Hora", "Lugar", "Capacidad", "Disponibles" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        JFrame frame = new JFrame("Conciertos disponibles");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));
        frame.add(sp, BorderLayout.CENTER);

        Runnable recargar = () -> {
            try {
                data.clear();
                data.addAll(controller.mostrarActivos());
                model.setRowCount(0);
                for (Concierto c : data) {
                    model.addRow(new Object[] {
                            c.getId(), c.getArtista(), c.getFecha(), c.getHora(),
                            c.getLugar(), c.getCapacidadTotal(), c.getDisponibles()
                    });
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame,
                        "Error de base de datos: " + e.getMessage(),
                        "Conciertos disponibles", JOptionPane.ERROR_MESSAGE);
            }
        };
        recargar.run();

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
                Concierto seleccionado = null;
                for (Concierto c : data) {
                    if (c.getId() == id) {
                        seleccionado = c;
                        break;
                    }
                }
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
