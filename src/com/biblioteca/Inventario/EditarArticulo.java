package com.biblioteca.Inventario;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.border.TitledBorder;
import java.util.List;


public class EditarArticulo extends JPanel {
    private JComboBox<String> tablasComboBox; // Para seleccionar tabla
    private JComboBox<String> columnasComboBox; // Para seleccionar columna adicional
    private JComboBox<String> articulosComboBox; // Para seleccionar artículo
    private JPanel formularioPanel; // Panel dinámico para las columnas
    private JButton cargarTablaButton, cargarArticuloButton, actualizarButton;
    private HashMap<String, String> idArticuloMap; // Mapa para almacenar ID y el valor adicional
    private String tablaSeleccionada; // Nombre de la tabla seleccionada

    public EditarArticulo() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Editar Artículo del Inventario"));

        // Panel superior para seleccionar tabla y columna
        JPanel seleccionPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        seleccionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        seleccionPanel.add(new JLabel("Seleccionar Tabla:"));
        tablasComboBox = new JComboBox<>();
        cargarTablas();
        tablasComboBox.addActionListener(e -> cargarColumnas());
        seleccionPanel.add(tablasComboBox);

        seleccionPanel.add(new JLabel("Seleccionar Columna:"));
        columnasComboBox = new JComboBox<>();
        columnasComboBox.setEnabled(false);
        seleccionPanel.add(columnasComboBox);

        cargarTablaButton = new JButton("Cargar Artículos");
        cargarTablaButton.addActionListener(e -> cargarArticulos());
        seleccionPanel.add(cargarTablaButton);

        seleccionPanel.add(new JLabel("Seleccionar Artículo (ID y columna):"));
        articulosComboBox = new JComboBox<>();
        articulosComboBox.setEnabled(false);
        seleccionPanel.add(articulosComboBox);

        cargarArticuloButton = new JButton("Cargar Artículo");
        cargarArticuloButton.setEnabled(false);
        cargarArticuloButton.addActionListener(e -> cargarArticuloSeleccionado());
        seleccionPanel.add(cargarArticuloButton);

        add(seleccionPanel, BorderLayout.NORTH);

        // Panel central para mostrar y editar columnas dinámicamente
        formularioPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JScrollPane scrollPanel = new JScrollPane(formularioPanel);
        scrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Detalles del Artículo",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        add(scrollPanel, BorderLayout.CENTER);

        // Botón para actualizar datos
        actualizarButton = new JButton("Actualizar");
        actualizarButton.setEnabled(false);
        actualizarButton.addActionListener(e -> actualizarArticulo());
        add(actualizarButton, BorderLayout.SOUTH);

        idArticuloMap = new HashMap<>();
    }

    private void cargarTablas() {
        tablasComboBox.removeAllItems();
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT nombre FROM tipos_documentos");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tablasComboBox.addItem(rs.getString("nombre"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar tablas: " + ex.getMessage());
        }
    }

    private void cargarColumnas() {
        tablaSeleccionada = (String) tablasComboBox.getSelectedItem();
        if (tablaSeleccionada == null || tablaSeleccionada.isEmpty()) {
            return;
        }

        columnasComboBox.removeAllItems();
        columnasComboBox.setEnabled(true);

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement("DESCRIBE " + tablaSeleccionada);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String columna = rs.getString("Field");
                if (!columna.equalsIgnoreCase("id")) { // Evitar mostrar la columna ID aquí
                    columnasComboBox.addItem(columna);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar columnas: " + ex.getMessage());
        }
    }

    private void cargarArticulos() {
        String columnaSeleccionada = (String) columnasComboBox.getSelectedItem();
        if (tablaSeleccionada == null || tablaSeleccionada.isEmpty() || columnaSeleccionada == null || columnaSeleccionada.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una tabla y una columna válida.");
            return;
        }

        articulosComboBox.removeAllItems();
        articulosComboBox.setEnabled(true);
        cargarArticuloButton.setEnabled(true);
        idArticuloMap.clear();

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, " + columnaSeleccionada + " FROM " + tablaSeleccionada);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String columnaValor = rs.getString(columnaSeleccionada);
                String displayText = id + " - " + (columnaValor != null ? columnaValor : "Sin Valor");
                idArticuloMap.put(displayText, id);
                articulosComboBox.addItem(displayText);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar artículos: " + ex.getMessage());
        }
    }

    private void cargarArticuloSeleccionado() {
        String seleccionado = (String) articulosComboBox.getSelectedItem();
        if (seleccionado == null || seleccionado.isEmpty() || !idArticuloMap.containsKey(seleccionado)) {
            JOptionPane.showMessageDialog(this, "Seleccione un artículo válido.");
            return;
        }

        String idArticulo = idArticuloMap.get(seleccionado);

        formularioPanel.removeAll();

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + tablaSeleccionada + " WHERE id = ?")) {

            stmt.setString(1, idArticulo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnValue = rs.getString(columnName);

                    // Evitar mostrar columnas automáticas
                    if (columnName.equalsIgnoreCase("fecha_registro")) {
                        continue;
                    }

                    formularioPanel.add(new JLabel(formatString(columnName) + ":"));
                    JTextField textField = new JTextField(columnValue);
                    if (columnName.equalsIgnoreCase("id")) {
                        textField.setEditable(false); // No editable para ID
                    }
                    formularioPanel.add(textField);
                }

                formularioPanel.revalidate();
                formularioPanel.repaint();
                actualizarButton.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Artículo no encontrado.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos del artículo: " + ex.getMessage());
        }
    }

   private void actualizarArticulo() {
    String seleccionado = (String) articulosComboBox.getSelectedItem();
    if (seleccionado == null || seleccionado.isEmpty() || !idArticuloMap.containsKey(seleccionado)) {
        JOptionPane.showMessageDialog(this, "Seleccione un artículo válido.");
        return;
    }

    String idArticulo = idArticuloMap.get(seleccionado);
    Component[] components = formularioPanel.getComponents();
    StringBuilder sql = new StringBuilder("UPDATE ").append(tablaSeleccionada).append(" SET ");
    boolean hasUpdates = false;

    try (Connection conn = ConexionBaseDatos.getConexion()) {
        PreparedStatement stmt = null;
        int index = 1;

        for (int i = 0; i < components.length; i += 2) {
            JLabel label = (JLabel) components[i];
            JTextField textField = (JTextField) components[i + 1];

            String columnName = label.getText().trim().replace(" ", "_").toLowerCase();
            String value = textField.getText().trim();

            if (!columnName.equalsIgnoreCase("id") && !value.isEmpty()) {
                if (hasUpdates) {
                    sql.append(", ");
                }
                sql.append("`").append(columnName).append("` = ?"); // Escapar nombres de columnas
                hasUpdates = true;
            }
        }

        sql.append(" WHERE `id` = ?"); // Escapar columna `id`

        if (hasUpdates) {
            stmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < components.length; i += 2) {
                JTextField textField = (JTextField) components[i + 1];
                String value = textField.getText().trim();

                if (!value.isEmpty()) {
                    stmt.setString(index++, value);
                }
            }
            stmt.setString(index, idArticulo); // Asignar ID al final para la cláusula WHERE
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Artículo actualizado exitosamente.");
        } else {
            JOptionPane.showMessageDialog(this, "No hay cambios para actualizar.");
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error al actualizar artículo: " + ex.getMessage());
    }
}

   // Método para formatear nombres de columnas
    private String formatString(String input) {
        return input.toUpperCase().replace("_", " ");
    }
}
