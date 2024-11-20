package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BuscarPorTituloAutorEstado extends JPanel {
    private JTextField tituloField;
    private JTable resultadosTable;
    private DefaultTableModel tableModel;

    public BuscarPorTituloAutorEstado() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Buscar por Título",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel superior para búsqueda
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta para el campo de título
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(createStyledLabel("Título:"), gbc);

        // Campo de texto para el título
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        tituloField = createStyledTextField();
        searchPanel.add(tituloField, gbc);

        // Botón de búsqueda
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JButton buscarButton = createStyledButton("Buscar", new Color(34, 139, 34), new Color(0, 100, 0));
        buscarButton.addActionListener(e -> buscarTitulo());
        searchPanel.add(buscarButton, gbc);

        add(searchPanel, BorderLayout.NORTH);

        // Tabla para mostrar los resultados
        tableModel = new DefaultTableModel();
        resultadosTable = new JTable(tableModel);
        resultadosTable.setFont(new Font("Arial", Font.PLAIN, 14));
        resultadosTable.setRowHeight(25);
        resultadosTable.setFillsViewportHeight(true);
        resultadosTable.getTableHeader().setReorderingAllowed(false);
        resultadosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(resultadosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Resultados",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void buscarTitulo() {
        String titulo = tituloField.getText();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un título.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Obtener nombres de las tablas desde `tipos_documentos`
            List<String> tablas = obtenerTablas(conn);
            if (tablas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron tablas en 'tipos_documentos'.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            tableModel.setRowCount(0); // Limpiar resultados previos

            for (String tabla : tablas) {
                buscarEnTabla(conn, tabla, titulo);
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No se encontraron resultados para el título especificado.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar ejemplares: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> obtenerTablas(Connection conn) throws SQLException {
        List<String> tablas = new ArrayList<>();
        String sql = "SELECT nombre FROM tipos_documentos";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                tablas.add(rs.getString("nombre"));
            }
        }
        return tablas;
    }

    private void buscarEnTabla(Connection conn, String tabla, String titulo) throws SQLException {
        // Consulta dinámica para buscar en todas las columnas de la tabla
        String sql = "SELECT * FROM " + tabla + " WHERE CONCAT_WS(' ', " + obtenerColumnasParaBusqueda(conn, tabla) + ") LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + titulo + "%");

            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Configurar columnas dinámicas en la tabla de resultados
            if (tableModel.getColumnCount() == 0) {
                for (int i = 1; i <= columnCount; i++) {
                    tableModel.addColumn(metaData.getColumnLabel(i));
                }
            }

            // Agregar los resultados encontrados
            while (rs.next()) {
                Object[] rowData = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    rowData[i] = rs.getObject(i + 1);
                }
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            System.err.println("Error al buscar en la tabla " + tabla + ": " + ex.getMessage());
        }
    }

    private String obtenerColumnasParaBusqueda(Connection conn, String tabla) throws SQLException {
        List<String> columnas = new ArrayList<>();
        String sql = "DESCRIBE " + tabla;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                columnas.add("`" + rs.getString("Field") + "`");
            }
        }
        return String.join(", ", columnas);
    }

    // Método para crear etiquetas estilizadas
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); // Steel Blue
        return label;
    }

    // Método para crear campos de texto estilizados
    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return textField;
    }

    // Método para crear botones estilizados
    private JButton createStyledButton(String text, Color defaultColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(defaultColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 40));
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
}
