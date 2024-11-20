package com.biblioteca.Formularios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.TitledBorder;

public class CrearFormulario extends JPanel {
    private JTextField nombreTablaField;
    private JTextField numeroColumnasField;
    private JPanel columnasPanel;
    private JButton generarColumnasButton, crearTablaButton;
    private boolean usarExistente = false;

    // Definición de colores para los botones
    private final Color botonCrearTabla = new Color(255, 69, 0); // Orange Red
    private final Color botonCrearTablaHover = new Color(178, 34, 34); // Firebrick
    private final Color botonGenerarCampos = new Color(34, 139, 34); // Forest Green
    private final Color botonGenerarCamposHover = new Color(0, 100, 0); // Dark Green

    private List<JTextField> camposDinamicos = new ArrayList<>();

    public CrearFormulario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Crear Nuevo Formulario",
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

        // Nombre del formulario
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        configuracionPanel.add(createStyledLabel("Nombre del Formulario:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        configuracionPanel.add(nombreTablaField = createStyledTextField(), gbc);

        // Número de columnas
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        configuracionPanel.add(createStyledLabel("Número de Columnas:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.8;
        configuracionPanel.add(numeroColumnasField = createStyledTextField(), gbc);

        // Botón generar columnas
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        configuracionPanel.add(generarColumnasButton = createStyledButton("Generar Campos", botonGenerarCampos, botonGenerarCamposHover), gbc);

        generarColumnasButton.addActionListener(e -> generarCampos());

        add(configuracionPanel, BorderLayout.NORTH);

        // Panel central para columnas dinámicas
        columnasPanel = new JPanel();
        columnasPanel.setLayout(new BoxLayout(columnasPanel, BoxLayout.Y_AXIS));
        columnasPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JScrollPane scrollPanel = new JScrollPane(columnasPanel);
        scrollPanel.setPreferredSize(new Dimension(500, 300));
        scrollPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Columnas Personalizadas",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        add(scrollPanel, BorderLayout.CENTER);

        // Botón crear tabla
        crearTablaButton = createStyledButton("Crear Tabla", botonCrearTabla, botonCrearTablaHover);
        crearTablaButton.addActionListener(e -> crearTabla());
        crearTablaButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.add(crearTablaButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void generarCampos() {
        columnasPanel.removeAll();
        camposDinamicos.clear();
        crearTablaButton.setEnabled(false);

        String nombreTabla = nombreTablaField.getText().trim();
        if (nombreTabla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del formulario no puede estar vacío.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Verificar si el formulario ya existe en tipos_documentos
            String verificarRegistroSQL = "SELECT COUNT(*) AS total FROM tipos_documentos WHERE nombre = ?";
            try (PreparedStatement psVerificar = conn.prepareStatement(verificarRegistroSQL)) {
                psVerificar.setString(1, nombreTabla);
                ResultSet rs = psVerificar.executeQuery();
                if (rs.next() && rs.getInt("total") > 0) {
                    JOptionPane.showMessageDialog(this, "El nombre del formulario ya existe en 'tipos_documentos'.");
                    return;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al verificar el nombre en 'tipos_documentos': " + e.getMessage());
            return;
        }

        try {
            int numeroColumnas = Integer.parseInt(numeroColumnasField.getText());
            if (numeroColumnas <= 0) {
                JOptionPane.showMessageDialog(this, "El número de columnas debe ser mayor a 0.");
                return;
            }

            for (int i = 0; i < numeroColumnas; i++) {
                JPanel columnaPanel = new JPanel(new BorderLayout(10, 10));
                columnaPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                columnaPanel.setBackground(Color.WHITE);

                JLabel columnaLabel = createStyledLabel("Nombre de la Columna " + (i + 1) + ":");
                columnaPanel.add(columnaLabel, BorderLayout.WEST);

                JTextField nombreColumnaField = createStyledTextField();
                columnaPanel.add(nombreColumnaField, BorderLayout.CENTER);

                camposDinamicos.add(nombreColumnaField);
                columnasPanel.add(columnaPanel);
                columnasPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            crearTablaButton.setEnabled(true);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un número válido para las columnas.");
        }

        columnasPanel.revalidate();
        columnasPanel.repaint();
    }

   private void crearTabla() {
    if (usarExistente) {
        JOptionPane.showMessageDialog(this, "Se está utilizando la tabla existente. No es necesario crear una nueva.");
        return;
    }

    String nombreTabla = nombreTablaField.getText().trim().toUpperCase(); // Convertir a mayúsculas
    if (nombreTabla.isEmpty()) {
        JOptionPane.showMessageDialog(this, "El nombre del formulario no puede estar vacío.");
        return;
    }

    // Validar formato del nombre de la tabla (solo letras, números y guiones bajos permitidos)
    if (!nombreTabla.matches("[A-Z0-9_]+")) {
        JOptionPane.showMessageDialog(this, "El nombre del formulario debe contener solo letras, números o guiones bajos.", 
                                      "Error en el nombre", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try (Connection conn = ConexionBaseDatos.getConexion()) {
        conn.setAutoCommit(false);

        // Verificar si el nombre ya está registrado en `tipos_documentos`
        String verificarRegistroSQL = "SELECT COUNT(*) FROM tipos_documentos WHERE nombre = ?";
        try (PreparedStatement psVerificar = conn.prepareStatement(verificarRegistroSQL)) {
            psVerificar.setString(1, nombreTabla);
            ResultSet rs = psVerificar.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "El formulario ya está registrado en 'tipos_documentos'.");
                return;
            }
        }

        // Crear la definición de la tabla
        StringBuilder sql = new StringBuilder("CREATE TABLE ").append(nombreTabla).append(" (");

        // Agregar la columna ID con formato `id_nombreDeLaTabla`
        String idColumna = "id_" + nombreTabla.toLowerCase();
        sql.append(idColumna).append(" VARCHAR(15) PRIMARY KEY, "); // Máximo 15 caracteres: 3 letras + 5 números, ajustado si es necesario

        // Procesar los nombres de las columnas dinámicas
        for (JTextField campo : camposDinamicos) {
            String nombreColumna = campo.getText().trim();
            if (nombreColumna.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Los nombres de las columnas no pueden estar vacíos.");
                return;
            }
            sql.append(nombreColumna).append(" VARCHAR(255), ");
        }

        // Agregar columnas predeterminadas
        sql.append("ubicacion_fisica VARCHAR(255), ");
        sql.append("cantidad_total INT DEFAULT 0, ");
        sql.append("cantidad_disponible INT DEFAULT 0, ");
        sql.append("estado ENUM('Bueno', 'Dañado', 'En Reparación') DEFAULT 'Bueno', ");
        sql.append("palabras_clave TEXT, ");
        sql.append("fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
        sql.append(");");

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql.toString());
        }

        // Registrar el nuevo tipo de documento en la tabla `tipos_documentos`
        String registrarTipoSQL = "INSERT INTO tipos_documentos (nombre, fecha_creacion) VALUES (?, NOW())";
        try (PreparedStatement psRegistrar = conn.prepareStatement(registrarTipoSQL)) {
            psRegistrar.setString(1, nombreTabla);
            psRegistrar.executeUpdate();
        }

        conn.commit();
        JOptionPane.showMessageDialog(this, "Tabla registrada de forma exitosa.");

        // Limpiar campos
        nombreTablaField.setText("");
        numeroColumnasField.setText("");
        columnasPanel.removeAll();
        camposDinamicos.clear();
        columnasPanel.revalidate();
        columnasPanel.repaint();
        crearTablaButton.setEnabled(false);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al crear la tabla: " + e.getMessage());
    }
}

    private JButton createStyledButton(String text, Color defaultColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(defaultColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 45));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
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
        label.setForeground(new Color(25, 25, 112));
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        textField.setBackground(Color.WHITE);
        return textField;
    }
}
