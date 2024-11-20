package com.biblioteca.Inventario;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;

public class EditarArticulo extends JPanel {
    private JComboBox<String> tablasComboBox;
    private JPanel formularioPanel;
    private JButton cargarTablaButton, actualizarButton;
    private HashMap<String, String> idArticuloMap;
    private String tablaSeleccionada;
    private JTable tablaDatos;
    private DefaultTableModel tableModel;

    private final Color botonCargarTabla = new Color(34, 139, 34);
    private final Color botonCargarTablaHover = new Color(0, 100, 0);
    private final Color botonActualizar = new Color(255, 69, 0);
    private final Color botonActualizarHover = new Color(178, 34, 34);

    public EditarArticulo() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Editar Artículo del Inventario",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel de selección de tabla
        JPanel seleccionPanel = new JPanel(new GridBagLayout());
        seleccionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta para seleccionar tabla
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        seleccionPanel.add(createStyledLabel("Seleccionar Tabla:"), gbc);

        // ComboBox para tablas
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        tablasComboBox = createStyledComboBox();
        tablasComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    String displayName = formatString((String) value);
                    label.setText(displayName);
                }
                return label;
            }
        });
        seleccionPanel.add(tablasComboBox, gbc);

        // Botón para cargar tabla
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        seleccionPanel.add(cargarTablaButton = createStyledButton("Cargar Tabla", botonCargarTabla, botonCargarTablaHover), gbc);

        cargarTablaButton.addActionListener(e -> cargarTabla());
        add(seleccionPanel, BorderLayout.NORTH);

        // Panel para mostrar datos de la tabla
        formularioPanel = new JPanel(new BorderLayout());
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

        // Panel inferior con botón de actualizar
        JPanel inferiorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        inferiorPanel.add(actualizarButton = createStyledButton("Actualizar", botonActualizar, botonActualizarHover));
        actualizarButton.setEnabled(false);
        actualizarButton.addActionListener(e -> abrirFormularioEdicion());
        add(inferiorPanel, BorderLayout.SOUTH);

        idArticuloMap = new HashMap<>();
        cargarTablasExistentes();
    }

    // Carga las tablas existentes en el ComboBox
    private void cargarTablasExistentes() {
        tablasComboBox.removeAllItems();
        addDefaultItem(tablasComboBox, "Opciones");

        String query = "SELECT nombre FROM tipos_documentos";
        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                tablasComboBox.addItem(rs.getString("nombre"));
            }

            if (tablasComboBox.getItemCount() == 1) {
                JOptionPane.showMessageDialog(this, "No se encontraron tablas registradas.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar tablas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carga los datos de la tabla seleccionada
    private void cargarTabla() {
        tablaSeleccionada = (String) tablasComboBox.getSelectedItem();
        if (tablaSeleccionada == null || tablaSeleccionada.equals("Opciones")) {
            JOptionPane.showMessageDialog(this, "Seleccione una opción válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        formularioPanel.removeAll();
        actualizarButton.setEnabled(false);
        idArticuloMap.clear();

        String describeQuery = "DESCRIBE " + tablaSeleccionada;
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(describeQuery);
             ResultSet rs = stmt.executeQuery()) {

            tableModel = new DefaultTableModel();
            HashMap<String, String> columnNameMap = new HashMap<>();

            while (rs.next()) {
                String nombreColumna = rs.getString("Field");
                String displayName = nombreColumna;

                if (nombreColumna.equalsIgnoreCase("ubicacion_fisica")) {
                    displayName = "Ubicación Física";
                } else if (nombreColumna.toLowerCase().startsWith("id_")) {
                    displayName = "ID";
                    idArticuloMap.put(displayName, nombreColumna);
                }

                columnNameMap.put(displayName, nombreColumna);
                tableModel.addColumn(displayName);
            }

            if (!idArticuloMap.containsKey("ID")) {
                JOptionPane.showMessageDialog(this, "No se encontró una columna ID en la tabla seleccionada.",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String dataQuery = "SELECT * FROM " + tablaSeleccionada;
            try (PreparedStatement dataStmt = conn.prepareStatement(dataQuery);
                 ResultSet dataRs = dataStmt.executeQuery()) {

                while (dataRs.next()) {
                    Object[] rowData = new Object[tableModel.getColumnCount()];
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        String columnName = tableModel.getColumnName(i);
                        String realColumnName = columnNameMap.get(columnName);

                        rowData[i] = dataRs.getString(realColumnName);
                    }
                    tableModel.addRow(rowData);
                }
            }

            tablaDatos = new JTable(tableModel) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return !getColumnName(column).equalsIgnoreCase("ID");
                }
            };
            tablaDatos.setFont(new Font("Arial", Font.PLAIN, 14));
            tablaDatos.setRowHeight(25);
            tablaDatos.setFillsViewportHeight(true);
            tablaDatos.getTableHeader().setReorderingAllowed(false);
            tablaDatos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            tablaDatos.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting() && tablaDatos.getSelectedRow() != -1) {
                    actualizarButton.setEnabled(true);
                } else {
                    actualizarButton.setEnabled(false);
                }
            });

            JScrollPane tableScrollPane = new JScrollPane(tablaDatos);
            formularioPanel.add(tableScrollPane, BorderLayout.CENTER);
            formularioPanel.revalidate();
            formularioPanel.repaint();

            JOptionPane.showMessageDialog(this, "Datos cargados exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos de la tabla: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Abre el formulario para actualizar un artículo
private void abrirFormularioEdicion() {
    int filaSeleccionada = tablaDatos.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Seleccione un registro para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    JDialog dialogoActualizacion = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Actualizar Artículo", true);
    dialogoActualizacion.setLayout(new BorderLayout());
    dialogoActualizacion.setSize(500, 400);
    dialogoActualizacion.setLocationRelativeTo(this);

    // Estilo del panel de edición
    JPanel panelEdicion = new JPanel(new GridBagLayout());
    panelEdicion.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "Editar Información",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16),
            new Color(70, 130, 180)
    ));
    panelEdicion.setBackground(Color.WHITE);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    HashMap<String, JTextField> camposEdicion = new HashMap<>();
    for (int i = 0; i < tablaDatos.getColumnCount(); i++) {
        String columnName = tablaDatos.getColumnName(i);

        JLabel etiqueta = new JLabel(columnName + ":");
        etiqueta.setFont(new Font("Arial", Font.BOLD, 14));
        etiqueta.setForeground(new Color(25, 25, 112)); // Navy color
        gbc.gridx = 0;
        gbc.gridy = i;
        gbc.weightx = 0.3;
        panelEdicion.add(etiqueta, gbc);

        JTextField campoTexto = new JTextField((String) tablaDatos.getValueAt(filaSeleccionada, i), 20);
        campoTexto.setFont(new Font("Arial", Font.PLAIN, 14));
        campoTexto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        campoTexto.setEnabled(!columnName.equalsIgnoreCase("ID"));
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panelEdicion.add(campoTexto, gbc);
        camposEdicion.put(columnName, campoTexto);
    }

    // Panel de botones con estilo
    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    panelBotones.setBackground(Color.WHITE);

    JButton guardarButton = new JButton("Guardar");
    JButton cancelarButton = new JButton("Cancelar");

    // Estilo para el botón "Guardar"
    guardarButton.setFont(new Font("Arial", Font.BOLD, 14));
    guardarButton.setBackground(new Color(34, 139, 34)); // Forest Green
    guardarButton.setForeground(Color.WHITE);
    guardarButton.setFocusPainted(false);
    guardarButton.setPreferredSize(new Dimension(100, 40));

    // Efecto hover para "Guardar"
    guardarButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            guardarButton.setBackground(new Color(0, 100, 0)); // Dark Green
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            guardarButton.setBackground(new Color(34, 139, 34)); // Forest Green
        }
    });

    // Estilo para el botón "Cancelar"
    cancelarButton.setFont(new Font("Arial", Font.BOLD, 14));
    cancelarButton.setBackground(new Color(178, 34, 34)); // Firebrick
    cancelarButton.setForeground(Color.WHITE);
    cancelarButton.setFocusPainted(false);
    cancelarButton.setPreferredSize(new Dimension(100, 40));

    // Efecto hover para "Cancelar"
    cancelarButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            cancelarButton.setBackground(new Color(139, 0, 0)); // Dark Red
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            cancelarButton.setBackground(new Color(178, 34, 34)); // Firebrick
        }
    });

    guardarButton.addActionListener(e -> {
        try {
            actualizarEnBaseDeDatos(filaSeleccionada, camposEdicion);
            for (int i = 0; i < tablaDatos.getColumnCount(); i++) {
                String columnName = tablaDatos.getColumnName(i);
                tablaDatos.setValueAt(camposEdicion.get(columnName).getText(), filaSeleccionada, i);
            }
            dialogoActualizacion.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    cancelarButton.addActionListener(e -> dialogoActualizacion.dispose());

    panelBotones.add(guardarButton);
    panelBotones.add(cancelarButton);

    // Agregar los paneles al diálogo
    dialogoActualizacion.add(panelEdicion, BorderLayout.CENTER);
    dialogoActualizacion.add(panelBotones, BorderLayout.SOUTH);
    dialogoActualizacion.setVisible(true);
}


    // Actualiza los datos en la base de datos
    private void actualizarEnBaseDeDatos(int filaSeleccionada, HashMap<String, JTextField> camposEdicion) throws SQLException {
        String idColumna = idArticuloMap.get("ID");
        String idValor = (String) tablaDatos.getValueAt(filaSeleccionada, 0);

        StringBuilder sql = new StringBuilder("UPDATE ").append(tablaSeleccionada).append(" SET ");
        for (int i = 0; i < tablaDatos.getColumnCount(); i++) {
            String columnName = tablaDatos.getColumnName(i);
            if (columnName.equalsIgnoreCase("ID")) {
                continue;
            }

            String realColumnName = idArticuloMap.getOrDefault(columnName, columnName);
            if (columnName.equalsIgnoreCase("Ubicación Física")) {
                realColumnName = "ubicacion_fisica";
            }

            sql.append("`").append(realColumnName).append("` = ?, ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE `").append(idColumna).append("` = ?");

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement updateStmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            for (int i = 0; i < tablaDatos.getColumnCount(); i++) {
                String columnName = tablaDatos.getColumnName(i);
                if (columnName.equalsIgnoreCase("ID")) {
                    continue;
                }

                String realColumnName = idArticuloMap.getOrDefault(columnName, columnName);
                if (columnName.equalsIgnoreCase("Ubicación Física")) {
                    realColumnName = "ubicacion_fisica";
                }

                String value = camposEdicion.get(columnName).getText();
                updateStmt.setString(paramIndex++, value);
            }

            updateStmt.setString(paramIndex, idValor);

            int filasAfectadas = updateStmt.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Artículo actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se actualizó ningún artículo.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // Crea un botón estilizado
    private JButton createStyledButton(String text, Color defaultColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(defaultColor);
        button.setForeground(Color.WHITE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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

    // Crea una etiqueta estilizada
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180));
        return label;
    }

    // Crea un ComboBox estilizado
    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox;
    }

    // Añade un elemento por defecto al ComboBox
    private void addDefaultItem(JComboBox<String> comboBox, String defaultItem) {
        if (comboBox.getItemCount() == 0) {
            comboBox.addItem(defaultItem);
        }
    }

    // Formatea una cadena de texto
    private String formatString(String input) {
        return input.toUpperCase().replace("_", " ");
    }
}
