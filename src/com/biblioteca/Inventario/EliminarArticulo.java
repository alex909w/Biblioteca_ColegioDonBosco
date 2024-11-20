package com.biblioteca.Inventario;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;

public class EliminarArticulo extends JPanel {
    private JComboBox<String> tablasComboBox; // ComboBox para seleccionar tabla
    private JButton cargarTablaButton, eliminarButton;
    private JTable tablaDatos;
    private DefaultTableModel tableModel;
    private String tablaSeleccionada; // Nombre de la tabla seleccionada
    private String columnaID; // Nombre de la columna ID de la tabla seleccionada

    // Definición de colores para los botones
    private final Color botonCargarTablaColor = new Color(34, 139, 34); // Forest Green
    private final Color botonCargarTablaHover = new Color(0, 100, 0); // Dark Green
    private final Color botonEliminarColor = new Color(220, 20, 60); // Crimson
    private final Color botonEliminarHover = new Color(178, 34, 34); // Firebrick

    public EliminarArticulo() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Eliminar Artículo del Inventario",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel superior para selección de tabla y botón de cargar
        JPanel superiorPanel = new JPanel(new GridBagLayout());
        superiorPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta para seleccionar tabla
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        superiorPanel.add(createStyledLabel("Seleccionar Tabla:"), gbc);

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
        superiorPanel.add(tablasComboBox, gbc);

        // Botón Cargar Tabla
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        superiorPanel.add(cargarTablaButton = createStyledButton("Cargar Tabla", botonCargarTablaColor, botonCargarTablaHover), gbc);

        cargarTablaButton.addActionListener(e -> cargarTabla());

        add(superiorPanel, BorderLayout.NORTH);

        // Panel central para mostrar la tabla de datos
        JPanel centralPanel = new JPanel(new BorderLayout());
        centralPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Configuración del JTable
        tableModel = new DefaultTableModel();
        tablaDatos = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla de solo lectura
            }
        };
        tablaDatos.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaDatos.setRowHeight(25);
        tablaDatos.getTableHeader().setReorderingAllowed(false);
        tablaDatos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tablaScrollPane = new JScrollPane(tablaDatos);
        tablaScrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Detalles del Artículo",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));

        centralPanel.add(tablaScrollPane, BorderLayout.CENTER);
        add(centralPanel, BorderLayout.CENTER);

        // Panel inferior para el botón de eliminar
        JPanel inferiorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        inferiorPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        inferiorPanel.add(eliminarButton = createStyledButton("Eliminar", botonEliminarColor, botonEliminarHover));
        eliminarButton.setEnabled(false); // Deshabilitado hasta que se cargue una tabla y se seleccione una fila
        eliminarButton.addActionListener(e -> eliminarArticulo());

        add(inferiorPanel, BorderLayout.SOUTH);

        // Cargar tablas al inicializar
        cargarTablasExistentes();
    }

    /**
     * Carga los nombres de las tablas desde 'tipos_documentos' y los añade al ComboBox.
     */
    private void cargarTablasExistentes() {
        tablasComboBox.removeAllItems();
        // Añadir el elemento predeterminado "Opciones"
        addDefaultItem(tablasComboBox, "Opciones");

        String query = "SELECT nombre FROM tipos_documentos"; // Consulta para obtener los nombres de las tablas

        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                tablasComboBox.addItem(rs.getString("nombre"));
            }

            if (tablasComboBox.getItemCount() == 1) { // Solo el elemento predeterminado
                JOptionPane.showMessageDialog(this, "No se encontraron tablas registradas en 'tipos_documentos'.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar tablas desde 'tipos_documentos': " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

   private void cargarTabla() {
    tablaSeleccionada = (String) tablasComboBox.getSelectedItem();
    if (tablaSeleccionada == null || tablaSeleccionada.equals("Opciones")) {
        JOptionPane.showMessageDialog(this, "Seleccione una opción válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Limpiar la tabla antes de cargar nuevos datos
    tableModel.setRowCount(0);
    tableModel.setColumnCount(0);
    eliminarButton.setEnabled(false);
    columnaID = "";

    try (Connection conn = ConexionBaseDatos.getConexion()) {
        // Obtener las columnas de la tabla
        String describeQuery = "DESCRIBE " + tablaSeleccionada;
        try (PreparedStatement stmt = conn.prepareStatement(describeQuery);
             ResultSet rs = stmt.executeQuery()) {

            // Crear el modelo para el JTable
            tableModel = new DefaultTableModel();

            while (rs.next()) {
                String nombreColumna = rs.getString("Field");
                String key = rs.getString("Key");

                if (key.equalsIgnoreCase("PRI")) {
                    columnaID = nombreColumna; // Guardar la columna ID primaria
                    tableModel.addColumn("ID"); // Mostrar como "ID" en el JTable
                } else {
                    tableModel.addColumn(formatString(nombreColumna)); // Formatear nombre amigable
                }
            }

            // Verificar si se detectó una columna primaria
            if (columnaID.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontró una columna primaria (ID) en la tabla seleccionada.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Obtener los datos de la tabla
            String dataQuery = "SELECT * FROM " + tablaSeleccionada;
            try (PreparedStatement dataStmt = conn.prepareStatement(dataQuery);
                 ResultSet dataRs = dataStmt.executeQuery()) {

                while (dataRs.next()) {
                    Object[] rowData = new Object[tableModel.getColumnCount()];
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        String columnName = tableModel.getColumnName(i);
                        if (columnName.equalsIgnoreCase("ID")) {
                            rowData[i] = dataRs.getString(columnaID); // Obtener el valor de la columna primaria
                        } else {
                            String realColumnName = columnName.replace(" ", "_").toLowerCase(); // Mapear nombre amigable al real
                            rowData[i] = dataRs.getString(realColumnName);
                        }
                    }
                    tableModel.addRow(rowData);
                }
            }

            // Asignar el modelo al JTable
            tablaDatos.setModel(tableModel);

            // Configurar propiedades del JTable
            tablaDatos.getColumnModel().getColumn(0).setPreferredWidth(100); // Ajustar ancho de la columna ID
            for (int i = 1; i < tablaDatos.getColumnCount(); i++) {
                tablaDatos.getColumnModel().getColumn(i).setPreferredWidth(150); // Ajustar ancho de otras columnas
            }

            // Listener para habilitar el botón Eliminar al seleccionar una fila
            tablaDatos.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting() && tablaDatos.getSelectedRow() != -1) {
                    eliminarButton.setEnabled(true);
                } else {
                    eliminarButton.setEnabled(false);
                }
            });

            JOptionPane.showMessageDialog(this, "Datos cargados exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener columnas de la tabla: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error al conectar a la base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    /**
     * Obtiene el nombre real de la columna en la base de datos basado en el nombre mostrado en el JTable.
     *
     * @param tabla        Nombre de la tabla.
     * @param displayName  Nombre mostrado en el JTable.
     * @return Nombre real de la columna en la base de datos.
     * @throws SQLException Si ocurre un error al acceder a los metadatos.
     */
    private String obtenerNombreColumnaReal(String tabla, String displayName) throws SQLException {
        String query = "DESCRIBE " + tabla;
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String nombreColumna = rs.getString("Field");
                String tipo = rs.getString("Type");
                String key = rs.getString("Key");
                String formattedName = formatString(nombreColumna);

                if (formattedName.equalsIgnoreCase(displayName)) {
                    return nombreColumna;
                }
            }
        }
        return displayName; // Retorna el nombre mostrado si no se encuentra una coincidencia
    }

    /**
     * Elimina el artículo seleccionado en el JTable después de confirmar la acción.
     */
    private void eliminarArticulo() {
        int filaSeleccionada = tablaDatos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idArticulo = (String) tablaDatos.getValueAt(filaSeleccionada, 0); // Suponiendo que la columna ID es la primera

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar el artículo con ID: " + idArticulo + "?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = ConexionBaseDatos.getConexion()) {
                String sqlEliminar = "DELETE FROM " + tablaSeleccionada + " WHERE " + columnaID + " = ?";
                PreparedStatement stmtEliminar = conn.prepareStatement(sqlEliminar);
                stmtEliminar.setString(1, idArticulo);

                int filasAfectadas = stmtEliminar.executeUpdate();
                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(this, "Artículo eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarTabla(); // Recargar la tabla para reflejar los cambios
                } else {
                    JOptionPane.showMessageDialog(this, "Artículo no encontrado o ya ha sido eliminado.", "Información", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar el artículo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Método auxiliar para añadir un elemento predeterminado a un JComboBox si no está presente.
     *
     * @param comboBox    El ComboBox al que se añadirá el elemento.
     * @param defaultItem El elemento predeterminado a añadir.
     */
    private void addDefaultItem(JComboBox<String> comboBox, String defaultItem) {
        if (comboBox.getItemCount() == 0 || !comboBox.getItemAt(0).equals(defaultItem)) {
            comboBox.insertItemAt(defaultItem, 0);
            comboBox.setSelectedIndex(0);
        }
    }

    // Métodos auxiliares para crear componentes estilizados

    /**
     * Crea un botón estilizado con colores personalizados y efectos hover.
     *
     * @param text         Texto del botón.
     * @param defaultColor Color de fondo predeterminado.
     * @param hoverColor   Color de fondo al pasar el cursor.
     * @return El botón estilizado.
     */
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

    /**
     * Crea una etiqueta estilizada con colores y fuentes personalizadas.
     *
     * @param text Texto de la etiqueta.
     * @return La etiqueta estilizada.
     */
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); // Steel Blue
        return label;
    }

    /**
     * Crea un ComboBox estilizado con bordes y fuentes personalizadas.
     *
     * @return El ComboBox estilizado.
     */
    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox;
    }

    /**
     * Crea un campo de texto estilizado con bordes y fuentes personalizadas.
     *
     * @return El campo de texto estilizado.
     */
    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        textField.setBackground(Color.WHITE); // Fondo blanco para mejor contraste
        return textField;
    }

    /**
     * Formatea una cadena de texto convirtiéndola a mayúsculas y reemplazando guiones bajos con espacios.
     *
     * @param input Cadena de entrada.
     * @return Cadena formateada.
     */
    private String formatString(String input) {
        return input.toUpperCase().replace("_", " ");
    }
}
