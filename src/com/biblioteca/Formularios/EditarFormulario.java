
package com.biblioteca.Formularios;



import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EditarFormulario extends JPanel {
    private JComboBox<String> tablasComboBox;
    private JPanel columnasPanel;
    private JButton cargarTablaButton, actualizarTablaButton;

    public EditarFormulario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Editar Formulario Existente"));

        JPanel configuracionPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        configuracionPanel.add(new JLabel("Seleccione una Tabla:"));

        tablasComboBox = new JComboBox<>();
        cargarTablasExistentes();
        configuracionPanel.add(tablasComboBox);

        cargarTablaButton = new JButton("Cargar Tabla");
        cargarTablaButton.addActionListener(e -> cargarColumnas());
        configuracionPanel.add(cargarTablaButton);

        add(configuracionPanel, BorderLayout.NORTH);

        columnasPanel = new JPanel();
        columnasPanel.setLayout(new BoxLayout(columnasPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPanel = new JScrollPane(columnasPanel);
        add(scrollPanel, BorderLayout.CENTER);

        actualizarTablaButton = new JButton("Actualizar Tabla");
        actualizarTablaButton.addActionListener(e -> actualizarTabla());
        add(actualizarTablaButton, BorderLayout.SOUTH);
    }

    private void cargarTablasExistentes() {
        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {

            while (rs.next()) {
                tablasComboBox.addItem(rs.getString(1));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar tablas: " + ex.getMessage());
        }
    }

    private void cargarColumnas() {
        columnasPanel.removeAll();

        String nombreTabla = (String) tablasComboBox.getSelectedItem();
        if (nombreTabla == null || nombreTabla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("DESCRIBE " + nombreTabla)) {

            while (rs.next()) {
                JPanel columnaPanel = new JPanel(new GridLayout(1, 2, 10, 10));
                columnaPanel.add(new JLabel("Columna: " + rs.getString("Field")));
                JTextField nuevoNombreField = new JTextField();
                nuevoNombreField.setToolTipText(rs.getString("Field"));
                columnaPanel.add(nuevoNombreField);
                columnasPanel.add(columnaPanel);
            }

            // Opción para agregar nuevas columnas
            JButton agregarColumnaButton = new JButton("Agregar Nueva Columna");
            agregarColumnaButton.addActionListener(e -> agregarNuevaColumna());
            columnasPanel.add(agregarColumnaButton);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar columnas: " + ex.getMessage());
        }

        columnasPanel.revalidate();
        columnasPanel.repaint();
    }

    private void agregarNuevaColumna() {
        JPanel nuevaColumnaPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        nuevaColumnaPanel.add(new JLabel("Nueva Columna:"));
        JTextField nuevaColumnaField = new JTextField();
        nuevaColumnaPanel.add(nuevaColumnaField);
        columnasPanel.add(nuevaColumnaPanel);
        columnasPanel.revalidate();
        columnasPanel.repaint();
    }

    private void actualizarTabla() {
        String nombreTabla = (String) tablasComboBox.getSelectedItem();
        if (nombreTabla == null || nombreTabla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement()) {

            // Modificar nombres de columnas existentes
            for (Component componente : columnasPanel.getComponents()) {
                if (componente instanceof JPanel) {
                    JPanel columnaPanel = (JPanel) componente;
                    Component[] campos = columnaPanel.getComponents();
                    if (campos[1] instanceof JTextField) {
                        JTextField nombreField = (JTextField) campos[1];
                        String nuevoNombre = nombreField.getText();
                        String nombreActual = nombreField.getToolTipText();

                        if (!nuevoNombre.isEmpty() && !nuevoNombre.equals(nombreActual)) {
                            String sql = "ALTER TABLE " + nombreTabla + " CHANGE " + nombreActual + " " + nuevoNombre + " VARCHAR(255)";
                            stmt.executeUpdate(sql);
                        }
                    }
                }
            }

            // Agregar nuevas columnas
            for (Component componente : columnasPanel.getComponents()) {
                if (componente instanceof JPanel) {
                    JPanel columnaPanel = (JPanel) componente;
                    Component[] campos = columnaPanel.getComponents();
                    if (campos[1] instanceof JTextField) {
                        JTextField nuevaColumnaField = (JTextField) campos[1];
                        String nuevoNombre = nuevaColumnaField.getText();

                        if (!nuevoNombre.isEmpty()) {
                            String sql = "ALTER TABLE " + nombreTabla + " ADD " + nuevoNombre + " VARCHAR(255)";
                            stmt.executeUpdate(sql);
                        }
                    }
                }
            }

            JOptionPane.showMessageDialog(this, "Tabla actualizada exitosamente.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar la tabla: " + ex.getMessage());
        }
    }
}
