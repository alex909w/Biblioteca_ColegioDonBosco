package com.biblioteca.Panel.Prestamos;

import com.biblioteca.controller.DocumentoController;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Panel para buscar documentos por cualquier dato en tablas dinámicas.
 */
public class BuscarPorTituloAutorEstado extends JPanel {
    private JTextField criterioField;
    private JTable resultadosTable;
    private DefaultTableModel tableModel;
    private DocumentoController documentoController;

    public BuscarPorTituloAutorEstado() {
        documentoController = new DocumentoController();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Buscar Documentos",
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

        // Etiqueta para el campo de búsqueda
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(createStyledLabel("Criterio de Búsqueda:"), gbc);

        // Campo de texto para el criterio
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        criterioField = createStyledTextField();
        searchPanel.add(criterioField, gbc);

        // Botón de búsqueda
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JButton buscarButton = createStyledButton("Buscar", new Color(34, 139, 34), new Color(0, 100, 0));
        buscarButton.addActionListener(e -> buscarCriterio());
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

    /**
     * Método que se ejecuta al presionar el botón de búsqueda.
     */
    private void buscarCriterio() {
        String criterio = criterioField.getText().trim();
        if (criterio.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un criterio de búsqueda.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Llama al método del controlador para buscar en todas las tablas dinámicas
            List<Map<String, Object>> resultados = documentoController.buscarPorCriterioEnTodasLasTablas(criterio);

            // Limpia el modelo de la tabla
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron resultados para el criterio especificado.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Configura las columnas dinámicamente según los resultados
            // Obtiene todas las claves de los mapas para definir las columnas
            // Asume que todas las filas tienen las mismas columnas
            Map<String, Object> firstRow = resultados.get(0);
            for (String columnName : firstRow.keySet()) {
                tableModel.addColumn(columnName);
            }

            // Agrega los datos de los resultados a la tabla
            for (Map<String, Object> row : resultados) {
                Object[] rowData = row.values().toArray();
                tableModel.addRow(rowData);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar documentos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Métodos para crear componentes estilizados
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); // Steel Blue
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return textField;
    }

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
