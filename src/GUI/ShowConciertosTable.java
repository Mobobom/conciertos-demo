package GUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import BLL.Concierto;
import DLL.ControllerConcierto;

public class ShowConciertosTable {

    public static JFrame showTable(Component parent, Consumer<Concierto> onComprar) {
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
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        for (int i = 0; i < columns.length; i++) {
            sorter.setSortable(i, false);
        }
        table.setRowSorter(sorter);

        JTextField buscar = new JTextField();
        configurarBusqueda(buscar, sorter);

        JPanel filtros = new JPanel(new BorderLayout(6, 6));
        filtros.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));
        filtros.add(new JLabel("Buscar por artista o lugar:"), BorderLayout.WEST);
        filtros.add(buscar, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(table);

        JFrame frame = new JFrame("Conciertos disponibles");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));
        frame.add(filtros, BorderLayout.NORTH);
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
                int modelRow = table.convertRowIndexToModel(row);
                int id = Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());
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

        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> recargar.run());
        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(e -> frame.dispose());
        bar.add(actualizar);
        bar.add(cerrar);
        frame.add(bar, BorderLayout.SOUTH);

        frame.setSize(720, 440);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        return frame;
    }

    private static void configurarBusqueda(JTextField buscar, TableRowSorter<DefaultTableModel> sorter) {
        buscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aplicarFiltro();
            }

            private void aplicarFiltro() {
                String texto = buscar.getText().trim();
                if (texto.isEmpty()) {
                    sorter.setRowFilter(null);
                    return;
                }
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 1, 4));
            }
        });
    }

}
