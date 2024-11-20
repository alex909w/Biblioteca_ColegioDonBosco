package com.biblioteca.Formularios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EliminarFormulario extends JPanel {
    private JComboBox<String> tablasComboBox;
    private JTable datosTabla;
    private DefaultTableModel tableModel;
    private JButton eliminarButton, mostrarDatosButton;

    // Definición de colores para los botones
    private final Color botonEliminar = new Color(255, 69, 0); // Orange Red
    private final Color botonEliminarHover = new Color(178, 34, 34); // Firebrick
    private final Color botonMostrarDatos = new Color(34, 139, 34); // Forest Green
    private final Color botonMostrarDatosHover = new Color(0, 100, 0); // Dark Green

    public EliminarFormulario() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Eliminar Formulario Existente",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel superior con selección de tabla y botones
        JPanel configuracionPanel = new JPanel(new GridBagLayout());
        configuracionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta para selección de tabla
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        configuracionPanel.add(createStyledLabel("Seleccione una Tabla para Eliminar:"), gbc);

        // ComboBox para tablas
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        configuracionPanel.add(tablasComboBox = createStyledComboBox(), gbc);

        // Botón Mostrar Datos
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        configuracionPanel.add(mostrarDatosButton = createStyledButton("Mostrar Datos", botonMostrarDatos, botonMostrarDatosHover), gbc);

        mostrarDatosButton.addActionListener(e -> mostrarDatos());

        add(configuracionPanel, BorderLayout.NORTH);

        // Panel central para mostrar datos de la tabla
        tableModel = new DefaultTableModel();
        datosTabla = new JTable(tableModel);
        datosTabla.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(datosTabla);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Datos de la Tabla",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior para el botón de eliminar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.add(eliminarButton = createStyledButton("Eliminar Tabla", botonEliminar, botonEliminarHover));
        eliminarButton.addActionListener(e -> eliminarTabla());
        add(buttonPanel, BorderLayout.SOUTH);

        // Cargar tablas existentes al inicializar
        cargarTablasExistentes();
    }

    private void cargarTablasExistentes() {
    tablasComboBox.removeAllItems(); // Limpiar el ComboBox antes de cargar
    String query = "SELECT nombre FROM tipos_documentos"; // Consulta para obtener los nombres de las tablas

    try (Connection conn = ConexionBaseDatos.getConexion();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        while (rs.next()) {
            tablasComboBox.addItem(rs.getString("nombre"));
        }

        if (tablasComboBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No se encontraron tablas registradas en 'tipos_documentos'.", "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error al cargar tablas desde 'tipos_documentos': " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void mostrarDatos() {
        String nombreTabla = (String) tablasComboBox.getSelectedItem();
        if (nombreTabla == null || nombreTabla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Limpiar modelo de tabla existente
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        String query = "SELECT * FROM " + nombreTabla + " LIMIT 100"; // Limitar a 100 filas para rendimiento

        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Añadir columnas al modelo
            for (int i = 1; i <= columnCount; i++) {
                tableModel.addColumn(metaData.getColumnName(i));
            }

            // Añadir filas al modelo
            while (rs.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowData[i - 1] = rs.getObject(i);
                }
                tableModel.addRow(rowData);
            }

            if (columnCount == 0) {
                JOptionPane.showMessageDialog(this, "La tabla seleccionada no tiene datos.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al mostrar datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarTabla() {
    String nombreTabla = (String) tablasComboBox.getSelectedItem();
    if (nombreTabla == null || nombreTabla.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar la tabla '" + nombreTabla + "'?\nEsta acción no se puede deshacer.",
            "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

    if (confirm == JOptionPane.YES_OPTION) {
        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement()) {

            // Eliminar la tabla de la base de datos
            String sqlDropTable = "DROP TABLE " + nombreTabla;
            stmt.executeUpdate(sqlDropTable);

            // Eliminar el registro de tipos_documentos
            String sqlDeleteTipo = "DELETE FROM tipos_documentos WHERE nombre = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDeleteTipo)) {
                ps.setString(1, nombreTabla);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Tabla eliminada y registro actualizado en 'tipos_documentos'.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            tablasComboBox.removeItem(nombreTabla);
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar la tabla o actualizar 'tipos_documentos': " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


    private JButton createStyledButton(String text, Color defaultColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(defaultColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 40));
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(defaultColor);
            }
        });
        return button;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); // Steel Blue
        return label;
    }

    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox;
    }
}
