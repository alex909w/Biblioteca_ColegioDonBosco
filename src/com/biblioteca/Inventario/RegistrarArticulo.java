package com.biblioteca.Inventario;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrarArticulo extends JPanel {
    private JComboBox<String> formulariosComboBox; // ComboBox para seleccionar tablas
    private JPanel formularioPanel; // Panel dinámico para los formularios
    private JButton cargarFormularioButton, registrarButton;
    private List<JTextField> camposDinamicos; // Lista para almacenar los campos dinámicos
    private String tablaSeleccionada; // Nombre de la tabla seleccionada

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
        gbc.weightx = 0.3;
        superiorPanel.add(createStyledLabel("Seleccionar Tabla:"), gbc);

        // ComboBox para tablas
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        formulariosComboBox = createStyledComboBox();
        // Set custom renderer to format display
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
        gbc.weightx = 0.2;
        superiorPanel.add(cargarFormularioButton = createStyledButton("Cargar Formulario", botonCargarFormulario, botonCargarFormularioHover), gbc);

        cargarFormularioButton.addActionListener(e -> cargarFormulario());

        add(superiorPanel, BorderLayout.NORTH);

        // Panel central para formularios dinámicos
        formularioPanel = new JPanel();
        formularioPanel.setLayout(new GridBagLayout());
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

    private void cargarFormularios() {
        formulariosComboBox.removeAllItems();
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT nombre FROM tipos_documentos");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                formulariosComboBox.addItem(rs.getString("nombre"));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar formularios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

   // Método para cargar el formulario
private void cargarFormulario() {
    tablaSeleccionada = (String) formulariosComboBox.getSelectedItem();
    if (tablaSeleccionada == null || tablaSeleccionada.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Seleccione una tabla válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    formularioPanel.removeAll();
    camposDinamicos = new ArrayList<>();

    try (Connection conn = ConexionBaseDatos.getConexion()) {
        // Obtener las columnas de la tabla
        try (PreparedStatement stmt = conn.prepareStatement("DESCRIBE " + tablaSeleccionada);
             ResultSet rs = stmt.executeQuery()) {

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.weightx = 0.3;

            while (rs.next()) {
                String nombreColumna = rs.getString("Field");
                String tipo = rs.getString("Type");

                // Etiqueta para la columna
                JLabel etiqueta = createStyledLabel(formatString(nombreColumna) + ":");
                formularioPanel.add(etiqueta, gbc);

                gbc.gridx = 1;
                gbc.weightx = 0.7;

                if (nombreColumna.equalsIgnoreCase("id")) {
                    // Campo para ID: calcular el próximo ID automáticamente
                    String nuevoId = generarNuevoId(conn, tablaSeleccionada);
                    JTextField campoTexto = createStyledTextField();
                    campoTexto.setText(nuevoId);
                    campoTexto.setEditable(false); // Solo lectura
                    formularioPanel.add(campoTexto, gbc);

                    // Agregar a la lista dinámica para incluirlo en el registro
                    camposDinamicos.add(campoTexto);
                } else {
                    // Campo de texto para otras columnas
                    JTextField campoTexto = createStyledTextField();
                    formularioPanel.add(campoTexto, gbc);
                    camposDinamicos.add(campoTexto);
                }

                gbc.gridx = 0;
                gbc.weightx = 0.3;
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

// Método para generar el próximo ID automáticamente
private String generarNuevoId(Connection conn, String tabla) throws SQLException {
    String prefijo = tabla.substring(0, 3).toUpperCase(); // Obtener las primeras 3 letras del nombre de la tabla
    String nuevoId = prefijo + "0001"; // Valor predeterminado si no hay registros

    String sql = "SELECT MAX(CAST(SUBSTRING(id, LENGTH(?) + 1) AS UNSIGNED)) AS max_id FROM " + tabla;
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

// Método para registrar datos en la tabla
private void registrarDatos() {
    if (tablaSeleccionada == null || tablaSeleccionada.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Seleccione una tabla válida.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    List<String> valores = new ArrayList<>();
    for (JTextField campo : camposDinamicos) {
        valores.add(campo.getText().trim());
    }

    StringBuilder sql = new StringBuilder("INSERT INTO ").append(tablaSeleccionada).append(" (");
    StringBuilder placeholders = new StringBuilder(" VALUES (");

    try (Connection conn = ConexionBaseDatos.getConexion()) {
        // Obtener las columnas para generar el SQL dinámico
        try (PreparedStatement stmt = conn.prepareStatement("DESCRIBE " + tablaSeleccionada);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String nombreColumna = rs.getString("Field");
                sql.append(nombreColumna).append(", ");
                placeholders.append("?, ");
            }
        }

        // Quitar las comas finales y cerrar paréntesis
        sql.setLength(sql.length() - 2);
        sql.append(")");
        placeholders.setLength(placeholders.length() - 2);
        placeholders.append(")");
        sql.append(placeholders);

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < valores.size(); i++) {
                stmt.setString(i + 1, valores.get(i));
            }
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Datos registrados exitosamente en " + tablaSeleccionada, "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // Limpiar campos después del registro
            limpiarCampos();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Error al conectar a la base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}




   

    private void limpiarCampos() {
        for (JTextField campo : camposDinamicos) {
            campo.setText("");
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

    // Método para formatear strings: convertir a mayúsculas y reemplazar guiones bajos con espacios
    private String formatString(String input) {
        return input.toUpperCase().replace("_", " ");
    }
}
