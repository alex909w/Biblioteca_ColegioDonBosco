package com.biblioteca.Panel.Inventario;

import com.biblioteca.controller.InventarioController;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditarArticulo extends JPanel {
    private JComboBox<String> tablasComboBox;
    private JPanel formularioPanel;
    private JButton cargarTablaButton, actualizarButton;
    private HashMap<String, String> idArticuloMap;
    private String tablaSeleccionada;
    private JTable tablaDatos;
    private DefaultTableModel tableModel;
    private InventarioController inventarioController = new InventarioController();

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

    // Carga las tablas existentes en el ComboBox.
    
    private void cargarTablasExistentes() {
        tablasComboBox.removeAllItems();
        addDefaultItem(tablasComboBox, "Opciones");

        try {
            List<String> tablas = inventarioController.obtenerFormularios();
            for (String tabla : tablas) {
                tablasComboBox.addItem(tabla);
            }

            if (tablasComboBox.getItemCount() == 1) {
                JOptionPane.showMessageDialog(this, "No se encontraron tablas registradas.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar tablas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carga los datos de la tabla seleccionada en el JTable.
   private void cargarTabla() {
    tablaSeleccionada = (String) tablasComboBox.getSelectedItem();
    if (tablaSeleccionada == null || tablaSeleccionada.equals("Opciones")) {
        JOptionPane.showMessageDialog(this, "Seleccione una opción válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    formularioPanel.removeAll();
    actualizarButton.setEnabled(false);
    idArticuloMap.clear();

    try {
        List<Map<String, String>> columnasInfo = inventarioController.obtenerColumnasTabla(tablaSeleccionada);
        tableModel = new DefaultTableModel();
        HashMap<String, String> columnNameMap = new HashMap<>();

        for (Map<String, String> columnaInfo : columnasInfo) {
            String nombreColumna = columnaInfo.get("Field");
            String displayName = nombreColumna.equalsIgnoreCase("ubicacion_fisica") ? "Ubicación Física" :
                    (nombreColumna.toLowerCase().startsWith("id_") ? "ID" : nombreColumna);

            idArticuloMap.put(displayName, nombreColumna);
            tableModel.addColumn(displayName);
            columnNameMap.put(displayName, nombreColumna);
        }

        if (!idArticuloMap.containsKey("ID")) {
            JOptionPane.showMessageDialog(this, "No se encontró una columna ID en la tabla seleccionada.",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Map<String, Object>> datos = inventarioController.obtenerDatosTabla(tablaSeleccionada);
        for (Map<String, Object> fila : datos) {
            Object[] rowData = new Object[tableModel.getColumnCount()];
            int i = 0;

            // Ajuste para Java 8
            for (int colIndex = 0; colIndex < tableModel.getColumnCount(); colIndex++) {
                String columnName = tableModel.getColumnName(colIndex);
                String realColumnName = columnNameMap.get(columnName);
                rowData[i++] = fila.get(realColumnName);
            }
            tableModel.addRow(rowData);
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

    private void abrirFormularioEdicion() {
    int filaSeleccionada = tablaDatos.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Seleccione un registro para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    JDialog dialogoActualizacion = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Actualizar Artículo", true);
    dialogoActualizacion.setLayout(new BorderLayout());
    dialogoActualizacion.setSize(1000, 600); // Tamaño ampliado
    dialogoActualizacion.setLocationRelativeTo(this);

    // Panel de edición
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
    int columnas = 2; // Número de columnas
    int totalCampos = tablaDatos.getColumnCount();
    int filas = (int) Math.ceil((double) totalCampos / columnas); // Calcular filas necesarias

    for (int i = 0; i < totalCampos; i++) {
        String columnName = tablaDatos.getColumnName(i);

        JLabel etiqueta = createStyledLabel(columnName + ":");
        etiqueta.setFont(new Font("Arial", Font.BOLD, 14));
        etiqueta.setForeground(new Color(25, 25, 112));
        gbc.gridx = i % columnas * 2; // Usar columnas dinámicas
        gbc.gridy = i / columnas;    // Mover a la siguiente fila cuando sea necesario
        gbc.weightx = 0.3;
        panelEdicion.add(etiqueta, gbc);

        JTextField campoTexto = new JTextField(tablaDatos.getValueAt(filaSeleccionada, i).toString(), 20);
        campoTexto.setFont(new Font("Arial", Font.PLAIN, 14));
        campoTexto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        campoTexto.setEnabled(!columnName.equalsIgnoreCase("ID"));
        gbc.gridx = i % columnas * 2 + 1; // Colocar el campo en la columna siguiente
        gbc.weightx = 0.7;
        panelEdicion.add(campoTexto, gbc);
        camposEdicion.put(columnName, campoTexto);
    }

    // Panel de botones
    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    panelBotones.setBackground(Color.WHITE);

    JButton guardarButton = new JButton("Guardar");
    JButton cancelarButton = new JButton("Cancelar");

    // Estilo para el botón "Guardar"
    guardarButton.setFont(new Font("Arial", Font.BOLD, 14));
    guardarButton.setBackground(new Color(34, 139, 34));
    guardarButton.setForeground(Color.WHITE);
    guardarButton.setFocusPainted(false);
    guardarButton.setPreferredSize(new Dimension(120, 40));

    // Efecto hover para "Guardar"
    guardarButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            guardarButton.setBackground(new Color(0, 100, 0));
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            guardarButton.setBackground(new Color(34, 139, 34));
        }
    });

    // Estilo para el botón "Cancelar"
    cancelarButton.setFont(new Font("Arial", Font.BOLD, 14));
    cancelarButton.setBackground(new Color(178, 34, 34));
    cancelarButton.setForeground(Color.WHITE);
    cancelarButton.setFocusPainted(false);
    cancelarButton.setPreferredSize(new Dimension(120, 40));

    // Efecto hover para "Cancelar"
    cancelarButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            cancelarButton.setBackground(new Color(139, 0, 0));
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            cancelarButton.setBackground(new Color(178, 34, 34));
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

    // Agregar paneles al diálogo
    dialogoActualizacion.add(new JScrollPane(panelEdicion), BorderLayout.CENTER); // Usar JScrollPane para manejar overflow
    dialogoActualizacion.add(panelBotones, BorderLayout.SOUTH);
    dialogoActualizacion.setVisible(true);
}


    // Actualiza los datos en la base de datos.
    private void actualizarEnBaseDeDatos(int filaSeleccionada, HashMap<String, JTextField> camposEdicion) throws SQLException {
        if (!validarDatos(camposEdicion)) {
            return; // Si los datos no son válidos, detiene el proceso
        }

        String idColumna = idArticuloMap.get("ID");
        String idValor = tablaDatos.getValueAt(filaSeleccionada, 0).toString();

        Map<String, String> datos = new HashMap<>();
        for (String columnName : camposEdicion.keySet()) {
            if (!columnName.equalsIgnoreCase("ID")) {
                String realColumnName = idArticuloMap.getOrDefault(columnName, columnName);
                String value = camposEdicion.get(columnName).getText().trim();
                datos.put(realColumnName, value);
            }
        }

        inventarioController.actualizarDatos(tablaSeleccionada, idColumna, idValor, datos);
        JOptionPane.showMessageDialog(this, "Artículo actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        cargarTabla();
    }

    // Valida los datos antes de actualizar.
    private boolean validarDatos(HashMap<String, JTextField> camposEdicion) {
        for (String displayName : camposEdicion.keySet()) {
            String realColumnName = idArticuloMap.get(displayName);
            String value = camposEdicion.get(displayName).getText().trim();

            // Validación específica para cantidad
            if (realColumnName.equalsIgnoreCase("cantidad_disponible") || realColumnName.equalsIgnoreCase("cantidad_total")) {
                if (!value.matches("\\d+")) {
                    JOptionPane.showMessageDialog(this, "El campo " + displayName + " debe ser un número entero.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }

            // Validación para fechas
            if (realColumnName.equalsIgnoreCase("fecha_publicacion")) {
                try {
                    java.sql.Date.valueOf(value);
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(this, "El campo " + displayName + " debe tener el formato YYYY-MM-DD.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    // Crea un botón estilizado con colores personalizados y efectos hover.
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

    // Crea una etiqueta estilizada con colores y fuentes personalizadas.
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180));
        return label;
    }

    // Crea un ComboBox estilizado con bordes y fuentes personalizadas.
    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox;
    }

    // Añade un elemento predeterminado al ComboBox.
    private void addDefaultItem(JComboBox<String> comboBox, String defaultItem) {
        if (comboBox.getItemCount() == 0 || !comboBox.getItemAt(0).equals(defaultItem)) {
            comboBox.insertItemAt(defaultItem, 0);
            comboBox.setSelectedIndex(0);
        }
    }

    // Formatea una cadena de texto convirtiéndola a mayúsculas y reemplazando guiones bajos con espacios.
    private String formatString(String input) {
        return input.toUpperCase().replace("_", " ");
    }
}
