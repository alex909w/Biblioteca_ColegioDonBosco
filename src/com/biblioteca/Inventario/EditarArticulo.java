package com.biblioteca.Inventario;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;

public class EditarArticulo extends JPanel {
    private JComboBox<String> tablasComboBox; // Para seleccionar tabla
    private JPanel formularioPanel; // Panel dinámico para la tabla de datos
    private JButton cargarTablaButton, actualizarButton;
    private HashMap<String, String> idArticuloMap; // Mapa para almacenar ID y el nombre real de la columna
    private String tablaSeleccionada; // Nombre de la tabla seleccionada
    private JTable tablaDatos; // JTable para mostrar los datos
    private DefaultTableModel tableModel; // Modelo para el JTable

    // Definición de colores para los botones
    private final Color botonCargarTabla = new Color(34, 139, 34); // Forest Green
    private final Color botonCargarTablaHover = new Color(0, 100, 0); // Dark Green
    private final Color botonActualizar = new Color(255, 69, 0); // Orange Red
    private final Color botonActualizarHover = new Color(178, 34, 34); // Firebrick

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

        // Panel superior para selección de tabla
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
        // Configurar el renderer personalizado para formatear la visualización
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

        // Botón Cargar Tabla
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        seleccionPanel.add(cargarTablaButton = createStyledButton("Cargar Tabla", botonCargarTabla, botonCargarTablaHover), gbc);

        cargarTablaButton.addActionListener(e -> cargarTabla());

        add(seleccionPanel, BorderLayout.NORTH);

        // Panel central para mostrar la tabla de datos
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

        // Panel inferior para el botón de actualizar
        JPanel inferiorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        inferiorPanel.add(actualizarButton = createStyledButton("Actualizar", botonActualizar, botonActualizarHover));
        actualizarButton.setEnabled(false); // Deshabilitado hasta que se cargue una tabla
        actualizarButton.addActionListener(e -> actualizarArticulo());
        add(inferiorPanel, BorderLayout.SOUTH);

        idArticuloMap = new HashMap<>();

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

    formularioPanel.removeAll();
    actualizarButton.setEnabled(false);
    idArticuloMap.clear();

    try (Connection conn = ConexionBaseDatos.getConexion()) {
        // Obtener las columnas de la tabla
        String describeQuery = "DESCRIBE " + tablaSeleccionada;
        try (PreparedStatement stmt = conn.prepareStatement(describeQuery);
             ResultSet rs = stmt.executeQuery()) {

            // Crear el modelo para el JTable
            tableModel = new DefaultTableModel();
            // Añadir columnas al modelo
            while (rs.next()) {
                String nombreColumna = rs.getString("Field");
                String displayName = nombreColumna;

                if (nombreColumna.equalsIgnoreCase("ubicacion_fisica")) {
                    displayName = "Ubicación Física"; // Nombre más amigable para el usuario
                } else if (nombreColumna.toLowerCase().startsWith("id_")) {
                    displayName = "ID";
                    idArticuloMap.put(displayName, nombreColumna); // Mapear "ID" a la columna real
                } else {
                    displayName = formatString(nombreColumna);
                }
                tableModel.addColumn(displayName);
            }

            // Obtener los datos de la tabla
            String dataQuery = "SELECT * FROM " + tablaSeleccionada;
            try (PreparedStatement dataStmt = conn.prepareStatement(dataQuery);
                 ResultSet dataRs = dataStmt.executeQuery()) {

                while (dataRs.next()) {
                    Object[] rowData = new Object[tableModel.getColumnCount()];
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        String columnName = idArticuloMap.getOrDefault(tableModel.getColumnName(i), formatColumnName(tablaSeleccionada, tableModel.getColumnName(i)));
                        rowData[i] = dataRs.getString(columnName);
                    }
                    tableModel.addRow(rowData);
                }
            }

            // Crear el JTable y configurarlo
            tablaDatos = new JTable(tableModel) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    // Hacer no editable la columna "ID"
                    return !getColumnName(column).equalsIgnoreCase("ID");
                }
            };
            tablaDatos.setFont(new Font("Arial", Font.PLAIN, 14));
            tablaDatos.setRowHeight(25);
            tablaDatos.setFillsViewportHeight(true);
            tablaDatos.getTableHeader().setReorderingAllowed(false);
            tablaDatos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Añadir el JTable al panel
            JScrollPane tableScrollPane = new JScrollPane(tablaDatos);
            formularioPanel.add(tableScrollPane, BorderLayout.CENTER);
            formularioPanel.revalidate();
            formularioPanel.repaint();

            actualizarButton.setEnabled(true); // Habilitar botón de actualizar

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar formulario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error al conectar a la base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    private String formatColumnName(String tabla, String columnName) {
    // Devuelve el nombre de la columna como se espera en la base de datos
    if (columnName.equalsIgnoreCase("UBICACION FISICA") || columnName.equalsIgnoreCase("ubicacion fisica")) {
        return "ubicacion_fisica"; // Devuelve el formato correcto de la columna en la base de datos
    }
    return columnName; // Para otros casos, devolver sin cambios
}


    /**
     * Método para actualizar datos en la base de datos basado en los cambios en el JTable.
     */
    private void actualizarArticulo() {
        if (tablaDatos == null || tablaDatos.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para actualizar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int filaSeleccionada = tablaDatos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idColumna = "ID";
        String idValor = (String) tablaDatos.getValueAt(filaSeleccionada, tablaDatos.getColumnCount() - 1); // Suponiendo que "ID" es la última columna

        // Construir la consulta de actualización
        StringBuilder sql = new StringBuilder("UPDATE ").append(tablaSeleccionada).append(" SET ");

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Obtener las columnas de la tabla
            String describeQuery = "DESCRIBE " + tablaSeleccionada;
            try (PreparedStatement stmt = conn.prepareStatement(describeQuery);
                 ResultSet rs = stmt.executeQuery()) {

                HashMap<String, String> columnTypes = new HashMap<>();
                while (rs.next()) {
                    String nombreColumna = rs.getString("Field");
                    String tipo = rs.getString("Type");
                    columnTypes.put(nombreColumna, tipo);
                }

                // Iterar sobre las columnas y preparar la consulta
                for (int i = 0; i < tablaDatos.getColumnCount(); i++) {
                    String columnName = tablaDatos.getColumnName(i);
                    if (columnName.equalsIgnoreCase("ID")) {
                        continue; // No actualizar el ID
                    }

                    sql.append("`").append(formatColumnName(tablaSeleccionada, idArticuloMap.getOrDefault(columnName, columnName))).append("` = ?, ");
                }

                // Eliminar la última coma y espacio
                if (sql.length() > 0 && sql.charAt(sql.length() - 2) == ',') {
                    sql.setLength(sql.length() - 2);
                }

                // Añadir la cláusula WHERE
                String idColumnReal = idArticuloMap.get("ID");
                sql.append(" WHERE `").append(idColumnReal).append("` = ?");

                try (PreparedStatement updateStmt = conn.prepareStatement(sql.toString())) {
                    int paramIndex = 1;
                    for (int i = 0; i < tablaDatos.getColumnCount(); i++) {
                        String columnName = tablaDatos.getColumnName(i);
                        if (columnName.equalsIgnoreCase("ID")) {
                            continue;
                        }
                        String value = (String) tablaDatos.getValueAt(filaSeleccionada, i);
                        updateStmt.setString(paramIndex++, value);
                    }
                    updateStmt.setString(paramIndex, idValor); // Añadir el valor del ID para la cláusula WHERE

                    int filasAfectadas = updateStmt.executeUpdate();
                    if (filasAfectadas > 0) {
                        JOptionPane.showMessageDialog(this, "Artículo actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        cargarTabla(); // Recargar la tabla para reflejar los cambios
                    } else {
                        JOptionPane.showMessageDialog(this, "No se actualizó ningún artículo.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al obtener detalles de la tabla: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al conectar a la base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
