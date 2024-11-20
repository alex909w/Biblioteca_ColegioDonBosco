package com.biblioteca.acciones.Usuarios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class VerUsuarios extends JPanel {
    private JTable usuariosTable;
    private DefaultTableModel tableModel;

    public VerUsuarios() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE); // Fondo blanco para un aspecto limpio
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título del panel
        JLabel titleLabel = new JLabel("Lista de Usuarios");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Configuración de la tabla
        tableModel = new DefaultTableModel();
        usuariosTable = createStyledTable(tableModel);

        JScrollPane scrollPane = createStyledScrollPane(usuariosTable);
        add(scrollPane, BorderLayout.CENTER);

        // Cargar los usuarios
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String query = "SELECT * FROM usuarios";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Obtener metadatos de las columnas
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Añadir columnas al modelo de la tabla, excepto la columna de contraseña
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                if (!columnName.equalsIgnoreCase("clave")) { // Filtrar columna 'clave'
                    tableModel.addColumn(formatColumnName(columnName));
                }
            }

            // Añadir filas al modelo de la tabla
            while (rs.next()) {
                Object[] rowData = new Object[tableModel.getColumnCount()];
                int colIndex = 0;
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    if (!columnName.equalsIgnoreCase("clave")) { // Ignorar valores de la columna 'clave'
                        rowData[colIndex++] = rs.getObject(i);
                    }
                }
                tableModel.addRow(rowData);
            }

            ajustarColumnas();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajustarColumnas() {
        TableColumnModel columnModel = usuariosTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            // Centrar el texto en todas las columnas
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            columnModel.getColumn(i).setCellRenderer(centerRenderer);

            // Ajustar el ancho de las columnas
            String columnName = columnModel.getColumn(i).getHeaderValue().toString().toLowerCase();
            if (columnName.contains("id")) {
                columnModel.getColumn(i).setPreferredWidth(100); // Aumentamos el ancho a 100
            } else {
                columnModel.getColumn(i).setPreferredWidth(150);
            }
        }
    }

    // Métodos auxiliares para crear componentes estilizados

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        // Estilo para el encabezado de la tabla
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        // Centrar el texto en el encabezado de la tabla
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        return table;
    }

    private JScrollPane createStyledScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private String formatColumnName(String input) {
        // Formatear el nombre de la columna para mostrarlo de manera más legible
        String output = input.replace("_", " ");
        return Character.toUpperCase(output.charAt(0)) + output.substring(1);
    }
}
