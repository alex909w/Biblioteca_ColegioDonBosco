package com.biblioteca.Formularios;

import com.biblioteca.controller.FormularioController;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.border.TitledBorder;

/**
 * Panel para editar un formulario existente.
 */
public class EditarFormulario extends JPanel {
    private JComboBox<String> tablasComboBox;
    private JPanel columnasPanel;
    private JButton cargarTablaButton, actualizarTablaButton, agregarColumnaButton, cancelarButton;
    private boolean usarExistente = false;

    private final Color botonCargarTabla = new Color(34, 139, 34); // Forest Green
    private final Color botonCargarTablaHover = new Color(0, 100, 0); // Dark Green
    private final Color botonActualizarTabla = new Color(255, 69, 0); // Orange Red
    private final Color botonActualizarTablaHover = new Color(178, 34, 34); // Firebrick
    private final Color botonAgregarColumna = new Color(70, 130, 180); // Steel Blue
    private final Color botonAgregarColumnaHover = new Color(30, 144, 255); // Dodger Blue
    private final Color botonCancelar = new Color(220, 20, 60); // Crimson
    private final Color botonCancelarHover = new Color(178, 34, 34); // Firebrick

    private final Set<String> columnasExcluidasGenerales = new HashSet<>(Arrays.asList(
            "fecha_registro",
            "ubicacion_fisica",
            "cantidad_total",
            "cantidad_disponible",
            "estado",
            "palabras_clave"
    ));

    private FormularioController formularioController = new FormularioController();

    private List<JTextField> camposDinamicos = new ArrayList<>();
    private List<JTextField> nuevasColumnas = new ArrayList<>();

    public EditarFormulario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Editar Formulario Existente",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Panel superior con configuraciones
        JPanel configuracionPanel = new JPanel(new GridBagLayout());
        configuracionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Selección de Tabla
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        configuracionPanel.add(createStyledLabel("Seleccione una Tabla:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        configuracionPanel.add(tablasComboBox = createStyledComboBox(), gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        configuracionPanel.add(cargarTablaButton = createStyledButton("Cargar Tabla", botonCargarTabla, botonCargarTablaHover), gbc);

        cargarTablaButton.addActionListener(e -> cargarColumnas());

        add(configuracionPanel, BorderLayout.NORTH);

        // Panel central para columnas dinámicas
        columnasPanel = new JPanel();
        columnasPanel.setLayout(new BoxLayout(columnasPanel, BoxLayout.Y_AXIS));
        columnasPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JScrollPane scrollPanel = new JScrollPane(columnasPanel);
        scrollPanel.setPreferredSize(new Dimension(600, 400));
        scrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Columnas Personalizadas",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        add(scrollPanel, BorderLayout.CENTER);

        // Panel inferior para los botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        // Botón "Actualizar Tabla"
        actualizarTablaButton = createStyledButton("Actualizar Tabla", botonActualizarTabla, botonActualizarTablaHover);
        actualizarTablaButton.addActionListener(e -> actualizarTabla());
        buttonPanel.add(actualizarTablaButton);

        // Botón "Agregar Nueva Columna"
        agregarColumnaButton = createStyledButton("Agregar Nueva Columna", botonAgregarColumna, botonAgregarColumnaHover);
        agregarColumnaButton.addActionListener(e -> agregarNuevaColumna());
        buttonPanel.add(agregarColumnaButton);

        // Botón "Cancelar"
        cancelarButton = createStyledButton("Cancelar", botonCancelar, botonCancelarHover);
        cancelarButton.addActionListener(e -> cancelarAccion());
        buttonPanel.add(cancelarButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void cargarColumnas() {
        columnasPanel.removeAll();
        camposDinamicos.clear();
        nuevasColumnas.clear();
        usarExistente = false;

        String nombreTabla = (String) tablasComboBox.getSelectedItem();
        if (nombreTabla == null || nombreTabla.equals("Opciones")) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!nombreTabla.matches("[a-zA-Z0-9_]+")) {
            JOptionPane.showMessageDialog(this, "El nombre de la tabla contiene caracteres inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String idColumna = "id_" + nombreTabla.toLowerCase();

        try {
            List<String> columnas = formularioController.obtenerColumnas(nombreTabla);
            for (String campo : columnas) {
                String tipo = obtenerTipoColumna(nombreTabla, campo);
                JPanel columnaPanel = new JPanel(new GridBagLayout());
                columnaPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                columnaPanel.setBackground(Color.WHITE);

                GridBagConstraints gbcCol = new GridBagConstraints();
                gbcCol.insets = new Insets(5, 5, 5, 5);
                gbcCol.fill = GridBagConstraints.HORIZONTAL;

                // Etiqueta del campo
                gbcCol.gridx = 0;
                gbcCol.gridy = 0;
                gbcCol.weightx = 0.4;
                gbcCol.anchor = GridBagConstraints.WEST;
                String etiquetaTexto = formatString(campo);
                columnaPanel.add(createStyledLabel("Columna: " + etiquetaTexto), gbcCol);

                // Campo para nuevo nombre
                gbcCol.gridx = 1;
                gbcCol.gridy = 0;
                gbcCol.weightx = 0.6;
                JTextField nuevoNombreField = createStyledTextField();
                nuevoNombreField.setToolTipText(campo); // Guardar el nombre original
                columnaPanel.add(nuevoNombreField, gbcCol);

                camposDinamicos.add(nuevoNombreField);
                columnasPanel.add(columnaPanel);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar columnas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        columnasPanel.revalidate();
        columnasPanel.repaint();
    }

    private String obtenerTipoColumna(String nombreTabla, String columna) throws SQLException {
    String tipo = "VARCHAR(255)";
    String sql = "DESCRIBE `" + nombreTabla + "`";

    // Verifica conexión
    try (Connection conn = formularioController.formularioDAO.getConexion()) {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("La conexión a la base de datos no está disponible.");
        }

        // Verifica si la tabla existe
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rsMeta = metaData.getTables(null, null, nombreTabla, null)) {
            if (!rsMeta.next()) {
                throw new SQLException("La tabla especificada no existe: " + nombreTabla);
            }
        }

        // Ejecuta la consulta DESCRIBE
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Filtra la columna deseada
            while (rs.next()) {
                if (rs.getString("Field").equalsIgnoreCase(columna)) {
                    tipo = rs.getString("Type");
                    break;
                }
            }
        }
    }

    return tipo; // Retorna el tipo de la columna o el valor predeterminado
}

    private void actualizarTabla() {
        String nombreTabla = (String) tablasComboBox.getSelectedItem();
        if (nombreTabla == null || nombreTabla.isEmpty() || nombreTabla.equals("Opciones")) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            for (JTextField nombreField : camposDinamicos) {
                String nuevoNombre = nombreField.getText().trim();
                String nombreActual = nombreField.getToolTipText();

                if (!nuevoNombre.isEmpty() && !nuevoNombre.equalsIgnoreCase(nombreActual)) {
                    if (!nuevoNombre.matches("[\\p{L}\\p{N}_ áéíóúÁÉÍÓÚñÑüÜ!@#$%^&*()+-=/]+")) {
                        JOptionPane.showMessageDialog(this, "El nombre de la columna '" + nuevoNombre + "' contiene caracteres inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    String nuevoNombreDB = sanitizeName(nuevoNombre);

                    String idColumna = "id_" + nombreTabla.toLowerCase();
                    if (nuevoNombreDB.equalsIgnoreCase(idColumna)) {
                        JOptionPane.showMessageDialog(this, "No puedes renombrar otra columna al nombre de la columna ID ('" + idColumna + "').", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    String tipoDato = nuevoNombre.toLowerCase().contains("fecha") ? "DATE" : "VARCHAR(255)";
                    formularioController.actualizarNombreColumna(nombreTabla, nombreActual, nuevoNombreDB, tipoDato);
                }
            }

            for (JTextField nuevaColumnaField : nuevasColumnas) {
                String nuevoNombre = nuevaColumnaField.getText().trim();

                if (!nuevoNombre.isEmpty()) {
                    if (!nuevoNombre.matches("[\\p{L}\\p{N}_ áéíóúÁÉÍÓÚñÑüÜ!@#$%^&*()+-=/]+")) {
                        JOptionPane.showMessageDialog(this, "El nombre de la nueva columna '" + nuevoNombre + "' contiene caracteres inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    String nuevoNombreDB = sanitizeName(nuevoNombre);
                    String idColumna = "id_" + nombreTabla.toLowerCase();
                    if (nuevoNombreDB.equalsIgnoreCase(idColumna)) {
                        JOptionPane.showMessageDialog(this, "No puedes agregar una columna con el nombre de la columna ID ('" + idColumna + "').", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    String tipoDato = nuevoNombre.toLowerCase().contains("fecha") ? "DATE" : "VARCHAR(255)";
                    formularioController.agregarNuevaColumna(nombreTabla, nuevoNombreDB, tipoDato);
                }
            }

            JOptionPane.showMessageDialog(this, "Tabla actualizada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarColumnas();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar la tabla: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarNuevaColumna() {
        JPanel nuevaColumnaPanel = new JPanel(new GridBagLayout());
        nuevaColumnaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        nuevaColumnaPanel.setBackground(Color.WHITE);

        GridBagConstraints gbcNueva = new GridBagConstraints();
        gbcNueva.insets = new Insets(5, 5, 5, 5);
        gbcNueva.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta para nueva columna
        gbcNueva.gridx = 0;
        gbcNueva.gridy = 0;
        gbcNueva.weightx = 0.4;
        gbcNueva.anchor = GridBagConstraints.WEST;
        nuevaColumnaPanel.add(createStyledLabel("Nueva Columna:"), gbcNueva);

        // Campo para nombre de nueva columna
        gbcNueva.gridx = 1;
        gbcNueva.gridy = 0;
        gbcNueva.weightx = 0.6;
        JTextField nuevaColumnaField = createStyledTextField();
        nuevaColumnaPanel.add(nuevaColumnaField, gbcNueva);

        nuevasColumnas.add(nuevaColumnaField);
        columnasPanel.add(nuevaColumnaPanel);
        columnasPanel.revalidate();
        columnasPanel.repaint();
    }

    private void cancelarAccion() {
        columnasPanel.removeAll();
        columnasPanel.revalidate();
        columnasPanel.repaint();

        tablasComboBox.setSelectedIndex(0);

        JOptionPane.showMessageDialog(this, "Actualización Cancelada.", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private String sanitizeName(String name) {
        return name.trim().replaceAll(" +", "_");
    }

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
        textField.setBackground(Color.WHITE);
        return textField;
    }

    private String formatString(String input) {
        return input.toUpperCase().replace("_", " ");
    }

    @Override
    public void addNotify() {
        super.addNotify();
        cargarTablasExistentes();
    }

    private void cargarTablasExistentes() {
        tablasComboBox.removeAllItems();
        tablasComboBox.addItem("Opciones");
        try {
            List<String> tablas = formularioController.obtenerTablas();
            for (String tabla : tablas) {
                tablasComboBox.addItem(tabla);
            }
            if (tablas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron tablas registradas en 'tipos_documentos'.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar tablas desde 'tipos_documentos': " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
