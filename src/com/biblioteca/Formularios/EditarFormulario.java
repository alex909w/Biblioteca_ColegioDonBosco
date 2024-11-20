package com.biblioteca.Formularios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.TitledBorder;

public class EditarFormulario extends JPanel {
    private JComboBox<String> tablasComboBox;
    private JPanel columnasPanel;
    private JButton cargarTablaButton, actualizarTablaButton;
    private boolean usarExistente = false; // Flag para indicar si se usa tabla existente

    // Definición de colores para los botones
    private final Color botonCargarTabla = new Color(34, 139, 34); // Forest Green
    private final Color botonCargarTablaHover = new Color(0, 100, 0); // Dark Green
    private final Color botonActualizarTabla = new Color(255, 69, 0); // Orange Red
    private final Color botonActualizarTablaHover = new Color(178, 34, 34); // Firebrick

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
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        configuracionPanel.add(tablasComboBox = createStyledComboBox(), gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
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

        // Botón inferior para actualizar tabla
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.add(actualizarTablaButton = createStyledButton("Actualizar Tabla", botonActualizarTabla, botonActualizarTablaHover));
        actualizarTablaButton.addActionListener(e -> actualizarTabla());
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private List<JTextField> camposDinamicos = new ArrayList<>(); // Lista para almacenar los campos dinámicos
    private List<JTextField> nuevasColumnas = new ArrayList<>(); // Lista para nuevas columnas

    private void cargarTablasExistentes() {
    tablasComboBox.removeAllItems(); // Limpiar el ComboBox antes de cargar
    String query = "SELECT nombre FROM tipos_documentos"; // Consulta para obtener los nombres de las tablas

    try (Connection conn = ConexionBaseDatos.getConexion();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        while (rs.next()) {
            tablasComboBox.addItem(rs.getString("nombre"));
        }

        if (tablasComboBox.getItemCount() == 0) {
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
    if (nombreTabla == null || nombreTabla.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Validación del nombre de la tabla
    if (!nombreTabla.matches("[a-zA-Z0-9_]+")) {
        JOptionPane.showMessageDialog(this, "El nombre de la tabla contiene caracteres inválidos.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try (Connection conn = obtenerConexion(); // Método para validar la conexión
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("DESCRIBE " + nombreTabla)) {

        while (rs.next()) {
            String campo = rs.getString("Field");
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

            // Etiqueta del campo
            gbcCol.gridx = 0;
            gbcCol.gridy = 0;
            gbcCol.weightx = 0.4;
            gbcCol.anchor = GridBagConstraints.WEST;
            columnaPanel.add(createStyledLabel("Columna: " + campo), gbcCol);

            // Campo para nuevo nombre
            gbcCol.gridx = 1;
            gbcCol.gridy = 0;
            gbcCol.weightx = 0.6;
            JTextField nuevoNombreField = new JTextField(campo);
            nuevoNombreField.setToolTipText(campo);
            columnaPanel.add(nuevoNombreField, gbcCol);

            camposDinamicos.add(nuevoNombreField);
            columnasPanel.add(columnaPanel);
        }

        // Botón para agregar nueva columna
        JButton agregarColumnaButton = crearBotonAgregarColumna();
        columnasPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        columnasPanel.add(agregarColumnaButton);

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
    agregarColumnaButton.setBackground(new Color(135, 206, 250)); // Light Sky Blue
    agregarColumnaButton.setForeground(Color.WHITE);
    agregarColumnaButton.setFocusPainted(false);
    agregarColumnaButton.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    agregarColumnaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

    agregarColumnaButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            agregarColumnaButton.setBackground(new Color(70, 130, 180)); // Steel Blue
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            agregarColumnaButton.setBackground(new Color(135, 206, 250)); // Light Sky Blue
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
        JTextField nuevaColumnaField = new JTextField();
        nuevaColumnaPanel.add(nuevaColumnaField, gbcNueva);

        nuevasColumnas.add(nuevaColumnaField);
        columnasPanel.add(nuevaColumnaPanel);
        columnasPanel.revalidate();
        columnasPanel.repaint();
    }

    private void actualizarTabla() {
        String nombreTabla = (String) tablasComboBox.getSelectedItem();
        if (nombreTabla == null || nombreTabla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement()) {

            // Modificar nombres de columnas existentes
            for (JTextField nombreField : camposDinamicos) {
                String nuevoNombre = nombreField.getText().trim();
                String nombreActual = nombreField.getToolTipText();

                if (!nuevoNombre.isEmpty() && !nuevoNombre.equals(nombreActual)) {
                    String sql = "ALTER TABLE " + nombreTabla + " CHANGE " + nombreActual + " " + nuevoNombre + " VARCHAR(255)";
                    stmt.executeUpdate(sql);
                }
            }

            // Agregar nuevas columnas
            for (JTextField nuevaColumnaField : nuevasColumnas) {
                String nuevoNombre = nuevaColumnaField.getText().trim();

                if (!nuevoNombre.isEmpty()) {
                    String sql = "ALTER TABLE " + nombreTabla + " ADD " + nuevoNombre + " VARCHAR(255)";
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

    // Inicialización de componentes después de construir la interfaz
    @Override
    public void addNotify() {
        super.addNotify();
        cargarTablasExistentes();
    }
}
