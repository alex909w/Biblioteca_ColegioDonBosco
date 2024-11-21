package com.biblioteca.Inventario;

import com.biblioteca.base_datos.ConexionBaseDatos;
import com.biblioteca.utilidades.DateLabelFormatter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.UtilDateModel;

public class RegistrarArticulo extends JPanel {
    private JComboBox<String> formulariosComboBox; // ComboBox para seleccionar tablas
    private JPanel formularioPanel; // Panel dinámico para los formularios
    private JButton cargarFormularioButton, registrarButton;
    private String tablaSeleccionada; // Nombre de la tabla seleccionada
    private List<Component> camposDinamicos; // Cambiar a Component

    // Definición de colores para los botones
    private final Color botonCargarFormulario = new Color(34, 139, 34); // Forest Green
    private final Color botonCargarFormularioHover = new Color(0, 100, 0); // Dark Green
    private final Color botonRegistrar = new Color(255, 69, 0); // Orange Red
    private final Color botonRegistrarHover = new Color(178, 34, 34); // Firebrick

    public RegistrarArticulo() {

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Registrar Información en Tablas",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel superior con el ComboBox para seleccionar tablas y botón cargar formulario
        JPanel superiorPanel = new JPanel(new GridBagLayout());
        superiorPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta para seleccionar tabla
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        superiorPanel.add(createStyledLabel("Seleccionar Tabla:"), gbc);

        // ComboBox para tablas
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        formulariosComboBox = createStyledComboBox();
        // Configurar el renderer personalizado para formatear la visualización
        formulariosComboBox.setRenderer(new DefaultListCellRenderer() {
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
        superiorPanel.add(formulariosComboBox, gbc);

        // Botón Cargar Formulario
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        superiorPanel.add(cargarFormularioButton = createStyledButton("Cargar Formulario", botonCargarFormulario, botonCargarFormularioHover), gbc);

        cargarFormularioButton.addActionListener(e -> cargarFormulario());

        add(superiorPanel, BorderLayout.NORTH);

        // Panel central para formularios dinámicos
        formularioPanel = new JPanel(new GridBagLayout());
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JScrollPane scrollPanel = new JScrollPane(formularioPanel);
        scrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Formulario Dinámico",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        add(scrollPanel, BorderLayout.CENTER);

        // Panel inferior para el botón de registrar
        JPanel inferiorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        inferiorPanel.add(registrarButton = createStyledButton("Registrar Datos", botonRegistrar, botonRegistrarHover));
        registrarButton.setEnabled(false); // Deshabilitado hasta que se cargue un formulario
        registrarButton.addActionListener(e -> registrarDatos());
        add(inferiorPanel, BorderLayout.SOUTH);

        // Inicializar camposDinamicos
        camposDinamicos = new ArrayList<>();

        // Cargar formularios existentes al inicializar
        cargarFormularios();
    }

    // Carga los formularios existentes desde la base de datos y los añade al ComboBox.
    private void cargarFormularios() {
        formulariosComboBox.removeAllItems();
        // Añadir el elemento predeterminado "Opciones"
        addDefaultItem(formulariosComboBox, "Opciones");

        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT nombre FROM tipos_documentos");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                formulariosComboBox.addItem(rs.getString("nombre"));
            }

            if (formulariosComboBox.getItemCount() == 1) { // Solo el elemento predeterminado
                JOptionPane.showMessageDialog(this, "No se encontraron formularios registrados en 'tipos_documentos'.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar formularios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Carga el formulario dinámico basado en la tabla seleccionada.
    // Carga el formulario dinámico basado en la tabla seleccionada.
private void cargarFormulario() {
    tablaSeleccionada = (String) formulariosComboBox.getSelectedItem();
    if (tablaSeleccionada == null || tablaSeleccionada.equals("Opciones")) {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione una opción válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    formularioPanel.removeAll();
    camposDinamicos = new ArrayList<>();

    try (Connection conn = ConexionBaseDatos.getConexion()) {
        // Obtener las columnas de la tabla
        String describeQuery = "DESCRIBE `" + tablaSeleccionada + "`";
        try (PreparedStatement stmt = conn.prepareStatement(describeQuery);
             ResultSet rs = stmt.executeQuery()) {

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.3;

            while (rs.next()) {
                String nombreColumna = rs.getString("Field");
                String tipo = rs.getString("Type").toLowerCase();
                String key = rs.getString("Key");

                // Identificar si la columna es el ID dinámico (id_<nombre_tabla>)
                boolean esId = nombreColumna.equalsIgnoreCase("id_" + tablaSeleccionada.toLowerCase());

                // Etiqueta para la columna
                String etiquetaTexto = esId ? "ID:" : formatString(nombreColumna) + ":";
                JLabel etiqueta = createStyledLabel(etiquetaTexto);
                formularioPanel.add(etiqueta, gbc);

                gbc.gridx = 1;
                gbc.weightx = 0.7;

                if (nombreColumna.equalsIgnoreCase("estado")) {
                    // Campo de selección para el estado
                    JComboBox<String> estadoComboBox = new JComboBox<>(new String[]{"Bueno", "Dañado", "En Reparación"});
                    estadoComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
                    estadoComboBox.setBackground(Color.WHITE);
                    formularioPanel.add(estadoComboBox, gbc);
                    camposDinamicos.add(estadoComboBox);
                } else if (esId) {
                    // Campo para ID: generar automáticamente
                    String nuevoId = generarNuevoId(conn, tablaSeleccionada);
                    JTextField campoTexto = createStyledTextField();
                    campoTexto.setText(nuevoId);
                    campoTexto.setEditable(false); // Solo lectura
                    formularioPanel.add(campoTexto, gbc);
                    camposDinamicos.add(campoTexto);
                } else if (tipo.contains("date") || tipo.contains("timestamp")) {
                    // Campo de fecha con calendario
                    JPanel campoFecha = crearCampoFecha();
                    formularioPanel.add(campoFecha, gbc);
                } else {
                    // Campo de texto para otras columnas
                    JTextField campoTexto = createStyledTextField();
                    formularioPanel.add(campoTexto, gbc);
                    camposDinamicos.add(campoTexto);
                }

                // Resetear GridBagConstraints para la siguiente fila
                gbc.gridx = 0;
                gbc.weightx = 0.3;
                gbc.gridy++;
            }

            formularioPanel.revalidate();
            formularioPanel.repaint();
            registrarButton.setEnabled(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar formulario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error al conectar a la base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Crear un campo de fecha con calendario
private JPanel crearCampoFecha() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(new Color(255, 255, 255));

    // Crear el modelo de fecha
    UtilDateModel model = new UtilDateModel();
    model.setSelected(false); // Por defecto, sin selección

    // Propiedades del calendario
    Properties properties = new Properties();
    properties.put("text.today", "Hoy");
    properties.put("text.month", "Mes");
    properties.put("text.year", "Año");

    JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
    JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

    panel.add(datePicker, BorderLayout.CENTER);
    camposDinamicos.add(datePicker); // Agregar a la lista de campos dinámicos

    return panel;
}

// Obtener la fecha seleccionada del JDatePicker
private java.sql.Date obtenerFechaSeleccionada(JDatePickerImpl datePicker) {
    if (datePicker.getModel().getValue() != null) {
        java.util.Calendar selectedDate = (java.util.Calendar) datePicker.getModel().getValue();
        return new java.sql.Date(selectedDate.getTimeInMillis());
    }
    return null; // Si no se seleccionó ninguna fecha
}


    // Genera el próximo ID automáticamente basado en el nombre de la tabla.
    private String generarNuevoId(Connection conn, String tabla) throws SQLException {
        // Obtener las primeras 3 letras del nombre de la tabla
        String prefijo = tabla.length() >= 3 ? tabla.substring(0, 3).toUpperCase() : tabla.toUpperCase();
        String nuevoId = prefijo + "0001"; // Valor predeterminado si no hay registros

        // Nombre de la columna ID dinámico basado en la tabla
        String columnaId = "id_" + tabla.toLowerCase();

        // Consulta para obtener el mayor ID existente
        String sql = "SELECT MAX(CAST(SUBSTRING(" + columnaId + ", LENGTH(?) + 1) AS UNSIGNED)) AS max_id FROM " + tabla;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, prefijo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt("max_id") > 0) {
                    int maxId = rs.getInt("max_id");
                    nuevoId = prefijo + String.format("%04d", maxId + 1); // Incrementar el valor numérico y formatear
                }
            }
        }

        return nuevoId;
    }

    // Registra los datos ingresados en el formulario en la tabla seleccionada.
    private void registrarDatos() {
    if (tablaSeleccionada == null || tablaSeleccionada.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Seleccione una tabla válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Validar entradas antes de proceder
    if (!validarEntradas()) {
        return;
    }

    List<Object> valores = new ArrayList<>();
    List<String> columnas = new ArrayList<>();

    for (Component campo : camposDinamicos) {
        if (campo instanceof JTextField) {
            valores.add(((JTextField) campo).getText().trim());
        } else if (campo instanceof JComboBox) {
            valores.add(((JComboBox<?>) campo).getSelectedItem().toString());
        } else if (campo instanceof JDatePickerImpl) {
            java.sql.Date fecha = obtenerFechaSeleccionada((JDatePickerImpl) campo);
            valores.add(fecha);
        }
    }

    StringBuilder sql = new StringBuilder("INSERT INTO ").append("`").append(tablaSeleccionada).append("` (");
    StringBuilder placeholders = new StringBuilder(" VALUES (");

    try (Connection conn = ConexionBaseDatos.getConexion()) {
        // Obtener las columnas para generar el SQL dinámico
        String describeQuery = "DESCRIBE `" + tablaSeleccionada + "`";
        try (PreparedStatement stmt = conn.prepareStatement(describeQuery);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String nombreColumna = rs.getString("Field");
                columnas.add(nombreColumna);
                sql.append("`").append(nombreColumna).append("`, ");
                placeholders.append("?, ");
            }
        }

        // Quitar las comas finales y cerrar paréntesis
        if (sql.length() > 0 && placeholders.length() > 0) {
            sql.setLength(sql.length() - 2);
            sql.append(")");
            placeholders.setLength(placeholders.length() - 2);
            placeholders.append(")");
            sql.append(placeholders);
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < valores.size(); i++) {
                Object valor = valores.get(i);
                if (valor instanceof java.sql.Date) {
                    stmt.setDate(i + 1, (java.sql.Date) valor);
                } else {
                    stmt.setString(i + 1, valor.toString());
                }
            }
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Datos registrados exitosamente en " + tablaSeleccionada, "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // Limpiar campos después del registro
            limpiarCampos();

            // Actualizar el ID para el siguiente registro
            actualizarProximoId();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error al conectar a la base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    // Valida las entradas del formulario antes de registrar los datos.
    private boolean validarEntradas() {
        for (Component campo : camposDinamicos) {
            if (campo instanceof JTextField) {
                JTextField textField = (JTextField) campo;
                if (textField.isEditable() && textField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } else if (campo instanceof JComboBox) {
                JComboBox<?> comboBox = (JComboBox<?>) campo;
                if (comboBox.getSelectedItem() == null || comboBox.getSelectedItem().toString().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } else if (campo instanceof JDatePickerImpl) {
                JDatePickerImpl datePicker = (JDatePickerImpl) campo;
                if (datePicker.getModel().getValue() == null) {
                    JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
            // Puedes añadir más validaciones para otros tipos de componentes si es necesario
        }
        return true;
    }

    // Limpia los campos de texto editables después de registrar los datos.
    private void limpiarCampos() {
        for (Component campo : camposDinamicos) {
            if (campo instanceof JTextField) {
                JTextField textField = (JTextField) campo;
                if (textField.isEditable()) { // Evitar limpiar el campo ID que está en modo solo lectura
                    textField.setText("");
                }
            } else if (campo instanceof JComboBox) {
                JComboBox<?> comboBox = (JComboBox<?>) campo;
                comboBox.setSelectedIndex(-1); // Reiniciar el comboBox (dejarlo sin selección)
            } else if (campo instanceof JDatePickerImpl) {
                JDatePickerImpl datePicker = (JDatePickerImpl) campo;
                datePicker.getModel().setValue(null); // Reiniciar el selector de fecha
            }
            // Puedes añadir más tipos de componentes para limpiar si es necesario
        }
    }

    private void actualizarProximoId() {
        for (Component campo : camposDinamicos) {
            if (campo instanceof JTextField) {
                JTextField textField = (JTextField) campo;
                if (!textField.isEditable()) { // Es el campo ID
                    try (Connection conn = ConexionBaseDatos.getConexion()) {
                        String nuevoId = generarNuevoId(conn, tablaSeleccionada);
                        textField.setText(nuevoId);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error al actualizar el ID: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                }
            }
        }
    }

    // Métodos auxiliares para crear componentes estilizados
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

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); // Steel Blue
        return label;
    }

    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        return comboBox;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        textField.setBackground(Color.WHITE); // Fondo blanco para mejor contraste
        return textField;
    }

    private String formatString(String input) {
        return input.toUpperCase().replace("_", " ");
    }

    private void addDefaultItem(JComboBox<String> comboBox, String defaultItem) {
        if (comboBox.getItemCount() == 0 || !comboBox.getItemAt(0).equals(defaultItem)) {
            comboBox.insertItemAt(defaultItem, 0);
            comboBox.setSelectedIndex(0);
        }
    }

    // Crear un campo de fecha con calendario
    private JPanel crearCampoFecha(GridBagConstraints gbc, List<Component> camposDinamicos) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 255, 255));

        UtilDateModel model = new UtilDateModel();
        model.setSelected(false);

        Properties properties = new Properties();
        properties.put("text.today", "Hoy");
        properties.put("text.month", "Mes");
        properties.put("text.year", "Año");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        panel.add(datePicker, BorderLayout.CENTER);
        camposDinamicos.add(datePicker);

        return panel;
    }
}
