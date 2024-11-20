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
    private JTextField searchField;
    private JComboBox<String> searchTypeComboBox;

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

        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180)),
                "Buscar Usuario",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.PLAIN, 14),
                new Color(70, 130, 180)
        ));

        // ComboBox para tipo de búsqueda
        searchTypeComboBox = new JComboBox<>(new String[]{"Nombre", "ID", "Correo"});
        searchTypeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Campo de texto para la búsqueda
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Botón de búsqueda
        JButton searchButton = new JButton("Buscar");
        searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchButton.setBackground(new Color(51, 102, 153));
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> buscarUsuarios());

        // Añadir componentes al panel de búsqueda
        JPanel inputsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        inputsPanel.setBackground(Color.WHITE);
        inputsPanel.add(searchTypeComboBox);
        inputsPanel.add(searchField);
        searchPanel.add(inputsPanel, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        add(searchPanel, BorderLayout.SOUTH);

        // Configuración de la tabla
        tableModel = new DefaultTableModel();
        usuariosTable = createStyledTable(tableModel);

        JScrollPane scrollPane = createStyledScrollPane(usuariosTable);
        add(scrollPane, BorderLayout.CENTER);

        // Cargar los usuarios
        cargarUsuarios(null, null);
    }

    private void cargarUsuarios(String parametro, String valor) {
        tableModel.setRowCount(0); // Limpiar los datos actuales de la tabla
        tableModel.setColumnCount(0); // Limpiar las columnas (si es necesario)

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String query = "SELECT * FROM usuarios";
            if (parametro != null && valor != null && !valor.isEmpty()) {
                query += " WHERE " + parametro + " LIKE ?";
            }

            PreparedStatement stmt = conn.prepareStatement(query);

            if (parametro != null && valor != null && !valor.isEmpty()) {
                stmt.setString(1, "%" + valor + "%");
            }

            ResultSet rs = stmt.executeQuery();

            // Obtener metadatos de las columnas
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Añadir columnas al modelo de la tabla, excepto la columna de contraseña
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                if (!columnName.equalsIgnoreCase("contraseña")) { // Filtrar columna 'clave'
                    tableModel.addColumn(formatColumnName(columnName));
                }
            }

            // Añadir filas al modelo de la tabla
            while (rs.next()) {
                Object[] rowData = new Object[tableModel.getColumnCount()];
                int colIndex = 0;
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    if (!columnName.equalsIgnoreCase("contraseña")) { // Ignorar valores de la columna 'clave'
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

    private void buscarUsuarios() {
        String tipoBusqueda = (String) searchTypeComboBox.getSelectedItem();
        String parametro = null;

        // Determinar el parámetro de búsqueda basado en la selección
        if ("Nombre".equals(tipoBusqueda)) {
            parametro = "nombre";
        } else if ("ID".equals(tipoBusqueda)) {
            parametro = "id";
        } else if ("Correo".equals(tipoBusqueda)) {
            parametro = "correo";
        }

        String valorBusqueda = searchField.getText().trim();
        cargarUsuarios(parametro, valorBusqueda);
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

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

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
        String output = input.replace("_", " ");
        return Character.toUpperCase(output.charAt(0)) + output.substring(1);
    }
}
