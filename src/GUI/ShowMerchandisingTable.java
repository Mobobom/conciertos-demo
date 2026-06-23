package GUI;

import BLL.Concierto;
import BLL.Merchandising;
import BLL.MerchandisingService;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

public class ShowMerchandisingTable {

    private static JFrame activeFrame;

    public static void showTable(Concierto concierto, Consumer<Merchandising> onComprar) {
        if (concierto == null) {
            JOptionPane.showMessageDialog(null, "Seleccione un concierto.",
                    "Catalogo de merchandising", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (activeFrame != null) {
            if (activeFrame.isDisplayable()) {
                activeFrame.setState(Frame.NORMAL);
                activeFrame.toFront();
                activeFrame.requestFocus();
                return;
            }
            activeFrame = null;
        }

        MerchandisingService merchandisingService = new MerchandisingService();
        List<Merchandising> data = new ArrayList<>();
        String[] columns = {"ID", "Producto", "Precio", "Stock", "Estado"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        JFrame frame = new JFrame("Merchandising - " + concierto.getArtista());
        activeFrame = frame;
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (activeFrame == frame) {
                    activeFrame = null;
                }
            }
        });
        frame.setLayout(new BorderLayout(5, 5));
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable recargar = () -> {
            try {
                data.clear();
                data.addAll(merchandisingService.listarPorConcierto(concierto.getId()));
                model.setRowCount(0);
                for (Merchandising m : data) {
                    model.addRow(new Object[] {
                            m.getId(), m.getNombre(), m.getPrecio(), m.getStock(),
                            m.getStock() > 0 ? "Disponible" : "Agotado"
                    });
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame,
                        "Error de base de datos: " + e.getMessage(),
                        "Catalogo de merchandising", JOptionPane.ERROR_MESSAGE);
            }
        };
        recargar.run();

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        if (onComprar != null) {
            JButton comprar = new JButton("Comprar seleccionado");
            comprar.addActionListener(e -> {
                Merchandising seleccionado = obtenerSeleccionado(frame, table, data);
                if (seleccionado == null) {
                    return;
                }
                if (seleccionado.getStock() <= 0) {
                    JOptionPane.showMessageDialog(frame,
                            "El producto seleccionado no tiene stock disponible.",
                            "Comprar merchandising", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                onComprar.accept(seleccionado);
                recargar.run();
            });
            bar.add(comprar);
        }

        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> recargar.run());
        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(e -> frame.dispose());
        bar.add(actualizar);
        bar.add(cerrar);
        frame.add(bar, BorderLayout.SOUTH);

        frame.setSize(720, 420);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static Merchandising obtenerSeleccionado(JFrame frame, JTable table, List<Merchandising> data) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(frame, "Seleccione un producto.",
                    "Merchandising", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());
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

