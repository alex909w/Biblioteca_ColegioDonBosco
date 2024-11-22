package com.biblioteca.Panel.Usuarios;

import com.biblioteca.controller.UsuarioController;
import com.biblioteca.modelos.Usuario;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel para ver todos los usuarios registrados.
 */
public class VerUsuarios extends JPanel {
    private JTable usuariosTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchTypeComboBox;

    private UsuarioController usuarioController;

    public VerUsuarios() {
        usuarioController = new UsuarioController();

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
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
        searchTypeComboBox = new JComboBox<>(new String[]{"Nombre", "ID", "Email"});
        searchTypeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Campo de texto para la búsqueda
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Botón de búsqueda
        JButton searchButton = new JButton("Buscar");
        searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchButton.setBackground(new Color(51, 102, 153));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchButton.setBackground(new Color(51, 102, 153).darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchButton.setBackground(new Color(51, 102, 153));
            }
        });

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
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        try {
            List<Usuario> usuarios;
            if (parametro != null && valor != null && !valor.isEmpty()) {
                usuarios = usuarioController.buscarUsuarios(parametro, valor);
            } else {
                usuarios = usuarioController.obtenerTodosLosUsuarios();
            }

            // Añadir columnas al modelo de la tabla
            String[] columnNames = {"ID", "Nombre", "Email", "Rol", "Teléfono", "Dirección", "Fecha Nacimiento", "Fecha Registro"};
            for (String columnName : columnNames) {
                tableModel.addColumn(columnName);
            }

            // Añadir filas al modelo de la tabla
            for (Usuario usuario : usuarios) {
                Object[] rowData = {
                        usuario.getId(),
                        usuario.getNombre(),
                        usuario.getEmail(),
                        usuario.getRol(),
                        usuario.getTelefono(),
                        usuario.getDireccion(),
                        usuario.getFechaNacimiento(),
                        usuario.getFechaRegistro()
                };
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
        } else if ("Email".equals(tipoBusqueda)) {
            parametro = "email";
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
                columnModel.getColumn(i).setPreferredWidth(100);
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
}
