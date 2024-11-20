package com.biblioteca.Formularios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditarFormulario extends JPanel {
    private JComboBox<String> tablasComboBox;
    private JPanel columnasPanel;
    private JButton cargarTablaButton, actualizarTablaButton, agregarColumnaButton, cancelarButton;
    private boolean usarExistente = false; // Flag para indicar si se usa tabla existente

    // Definición de colores para los botones
    private final Color botonCargarTabla = new Color(34, 139, 34); // Forest Green
    private final Color botonCargarTablaHover = new Color(0, 100, 0); // Dark Green
    private final Color botonActualizarTabla = new Color(255, 69, 0); // Orange Red
    private final Color botonActualizarTablaHover = new Color(178, 34, 34); // Firebrick
    private final Color botonAgregarColumna = new Color(70, 130, 180); // Steel Blue
    private final Color botonAgregarColumnaHover = new Color(30, 144, 255); // Dodger Blue
    private final Color botonCancelar = new Color(220, 20, 60); // Crimson
    private final Color botonCancelarHover = new Color(178, 34, 34); // Firebrick

    // Conjunto de columnas excluidas para una búsqueda eficiente
    private final Set<String> columnasExcluidasGenerales = new HashSet<>(Arrays.asList(
            "fecha_registro",
            "ubicacion_fisica",
            "cantidad_total",
            "cantidad_disponible",
            "estado",
            "palabras_clave"
    ));

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

        // Panel superior con configuraciones (usando GridBagLayout)
        JPanel configuracionPanel = new JPanel(new GridBagLayout());
        configuracionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 1: Selección de Tabla
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
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        configuracionPanel.add(cargarTablaButton = createStyledButton("Cargar Tabla", botonCargarTabla, botonCargarTablaHover), gbc);

        cargarTablaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarColumnas();
            }
        });

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

        // Panel inferior para los botones "Actualizar", "Agregar Nueva Columna" y "Cancelar"
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

    private List<JTextField> camposDinamicos = new ArrayList<>(); // Lista para almacenar los campos dinámicos
    private List<JTextField> nuevasColumnas = new ArrayList<>(); // Lista para nuevas columnas

    private void cargarTablasExistentes() {
        tablasComboBox.removeAllItems(); // Limpiar el ComboBox antes de cargar

        // Añadir el elemento predeterminado
        tablasComboBox.addItem("Opciones");

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

    private void cargarColumnas() {
        columnasPanel.removeAll();
        camposDinamicos.clear();
        nuevasColumnas.clear();
        usarExistente = false; // Resetear el flag

        String nombreTabla = (String) tablasComboBox.getSelectedItem();
        if (nombreTabla == null || nombreTabla.equals("Seleccione una Tabla")) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación del nombre de la tabla
        if (!nombreTabla.matches("[a-zA-Z0-9_]+")) {
            JOptionPane.showMessageDialog(this, "El nombre de la tabla contiene caracteres inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Definir el nombre del id_columna dinámico
        String idColumna = "id_" + nombreTabla.toLowerCase();

        try (Connection conn = obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("DESCRIBE " + nombreTabla)) {

            while (rs.next()) {
                String campo = rs.getString("Field").trim();

                // Excluir las columnas predeterminadas y el id_columna dinámico (insensible a mayúsculas)
                if (columnasExcluidasGenerales.contains(campo.toLowerCase()) || campo.equalsIgnoreCase(idColumna)) {
                    continue;
                }

                String tipo = rs.getString("Type");
                String extra = rs.getString("Extra");

                JPanel columnaPanel = new JPanel(new GridBagLayout());
                columnaPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                columnaPanel.setBackground(Color.WHITE);

                GridBagConstraints gbcCol = new GridBagConstraints();
                gbcCol.insets = new Insets(5, 5, 5, 5);
                gbcCol.fill = GridBagConstraints.HORIZONTAL;

                // Etiqueta del campo (formateada)
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

            // No es necesario agregar el botón aquí ya que ahora está en el panel inferior

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar columnas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Útil para depuración
        }

        columnasPanel.revalidate();
        columnasPanel.repaint();
    }

    // Método para obtener una conexión válida
    private Connection obtenerConexion() throws SQLException {
        Connection conn = ConexionBaseDatos.getConexion();
        if (conn == null || conn.isClosed()) {
            throw new SQLException("La conexión a la base de datos no está disponible.");
        }
        return conn;
    }

    // Método para crear el botón de "Agregar Nueva Columna"
    private JButton crearBotonAgregarColumna() {
        JButton agregarColumnaButton = new JButton("Agregar Nueva Columna");
        agregarColumnaButton.setFont(new Font("Arial", Font.BOLD, 14));
        agregarColumnaButton.setBackground(botonAgregarColumna);
        agregarColumnaButton.setForeground(Color.WHITE);
        agregarColumnaButton.setFocusPainted(false);
        agregarColumnaButton.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        agregarColumnaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        agregarColumnaButton.setPreferredSize(new Dimension(180, 40));

        agregarColumnaButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                agregarColumnaButton.setBackground(botonAgregarColumnaHover);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                agregarColumnaButton.setBackground(botonAgregarColumna);
            }
        });

        agregarColumnaButton.addActionListener(e -> agregarNuevaColumna());
        return agregarColumnaButton;
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

    private void actualizarTabla() {
        String nombreTabla = (String) tablasComboBox.getSelectedItem();
        if (nombreTabla == null || nombreTabla.isEmpty() || nombreTabla.equals("Seleccione una Tabla")) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Definir el nombre del id_columna dinámico
        String idColumna = "id_" + nombreTabla.toLowerCase();

        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement()) {

            // Modificar nombres de columnas existentes
            for (JTextField nombreField : camposDinamicos) {
                String nuevoNombre = nombreField.getText().trim();
                String nombreActual = nombreField.getToolTipText();

                if (!nuevoNombre.isEmpty() && !nuevoNombre.equalsIgnoreCase(nombreActual)) {
                    // Validar el nuevo nombre (permitir espacios)
                    if (!nuevoNombre.matches("[a-zA-Z0-9_ ]+")) { // Permitir espacios
                        JOptionPane.showMessageDialog(this, "El nombre de la columna '" + nuevoNombre + "' contiene caracteres inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    // Sanitizar el nuevo nombre
                    String nuevoNombreDB = sanitizeName(nuevoNombre);

                    // Verificar que el nuevo nombre no sea la columna ID
                    if (nuevoNombreDB.equalsIgnoreCase(idColumna)) {
                        JOptionPane.showMessageDialog(this, "No puedes renombrar otra columna al nombre de la columna ID ('" + idColumna + "').", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    String sql = "ALTER TABLE " + nombreTabla + " CHANGE " + nombreActual + " " + nuevoNombreDB + " VARCHAR(255)";
                    stmt.executeUpdate(sql);
                }
            }

            // Agregar nuevas columnas
            for (JTextField nuevaColumnaField : nuevasColumnas) {
                String nuevoNombre = nuevaColumnaField.getText().trim();

                if (!nuevoNombre.isEmpty()) {
                    // Validar el nuevo nombre (permitir espacios)
                    if (!nuevoNombre.matches("[a-zA-Z0-9_ ]+")) { // Permitir espacios
                        JOptionPane.showMessageDialog(this, "El nombre de la nueva columna '" + nuevoNombre + "' contiene caracteres inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    // Sanitizar el nuevo nombre
                    String nuevoNombreDB = sanitizeName(nuevoNombre);

                    // Verificar que el nuevo nombre no sea la columna ID
                    if (nuevoNombreDB.equalsIgnoreCase(idColumna)) {
                        JOptionPane.showMessageDialog(this, "No puedes agregar una columna con el nombre de la columna ID ('" + idColumna + "').", "Error", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    String sql = "ALTER TABLE " + nombreTabla + " ADD " + nuevoNombreDB + " VARCHAR(255)";
                    stmt.executeUpdate(sql);
                }
            }

            JOptionPane.showMessageDialog(this, "Tabla actualizada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // Recargar columnas para reflejar cambios
            cargarColumnas();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar la tabla: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para cancelar acciones
    private void cancelarAccion() {
        // Limpiar el panel central
        columnasPanel.removeAll();
        columnasPanel.revalidate();
        columnasPanel.repaint();

        // Restablecer el JComboBox al estado predeterminado
        tablasComboBox.setSelectedIndex(0);

        // Mostrar mensaje al usuario
        JOptionPane.showMessageDialog(this, "Actualización Cacelada.", "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    // Método para sanitizar nombres: reemplazar espacios con guiones bajos
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
        textField.setBackground(Color.WHITE); // Fondo blanco para mejor contraste
        return textField;
    }

    // Método para formatear strings: convertir a mayúsculas y reemplazar guiones bajos con espacios
    private String formatString(String input) {
        return input.toUpperCase().replace("_", " ");
    }

    // Inicialización de componentes después de construir la interfaz
    @Override
    public void addNotify() {
        super.addNotify();
        cargarTablasExistentes();
    }
}
