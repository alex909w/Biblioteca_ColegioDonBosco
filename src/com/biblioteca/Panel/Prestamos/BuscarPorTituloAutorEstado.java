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
    private JComboBox<String> tablaComboBox; // Nuevo ComboBox para seleccionar la tabla
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

        // Etiqueta para el ComboBox de selección de tabla
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        searchPanel.add(createStyledLabel("Seleccionar Tabla:"), gbc);

        // ComboBox para seleccionar la tabla
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        tablaComboBox = createStyledComboBox();
        cargarTablasEnComboBox(); // Método para cargar las tablas en el ComboBox
        searchPanel.add(tablaComboBox, gbc);

        // Botón de búsqueda
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 2; // Hacer que el botón abarque dos filas
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
     * Método para cargar los nombres de las tablas dinámicas en el ComboBox.
     */
    private void cargarTablasEnComboBox() {
        try {
            List<String> tablas = documentoController.obtenerNombresTablas();
            for (String tabla : tablas) {
                tablaComboBox.addItem(tabla);
            }
            if (tablas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay tablas disponibles para buscar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar tablas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void buscarCriterio() {
    String criterio = criterioField.getText().trim();
    String tablaSeleccionada = (String) tablaComboBox.getSelectedItem();

    if (criterio.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Por favor, ingrese un criterio de búsqueda.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (tablaSeleccionada == null || tablaSeleccionada.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla para buscar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        // Llama al método del controlador para buscar en la tabla seleccionada
        List<Map<String, Object>> resultados = documentoController.buscarPorCriterioOrdenado(tablaSeleccionada, criterio);

        // Limpia el modelo de la tabla
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        if (resultados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron resultados para el criterio especificado en la tabla seleccionada.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Configura las columnas dinámicamente según los resultados en orden
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

    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox;
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
