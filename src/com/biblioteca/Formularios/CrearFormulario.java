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

public class CrearFormulario extends JPanel {
    private JTextField nombreTablaField;
    private JTextField numeroColumnasField;
    private JPanel columnasPanel;
    private JButton generarColumnasButton, crearTablaButton;
    private boolean usarExistente = false; // Flag para indicar si se usa tabla existente

    // Definición de colores para los botones
    private final Color botonCrearTabla = new Color(255, 69, 0); // Orange Red
    private final Color botonCrearTablaHover = new Color(178, 34, 34); // Firebrick
    private final Color botonGenerarCampos = new Color(34, 139, 34); // Forest Green
    private final Color botonGenerarCamposHover = new Color(0, 100, 0); // Dark Green

    public CrearFormulario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Crear Nuevo Formulario", TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18), new Color(70, 130, 180)));

        // Panel superior con configuraciones (usando GridBagLayout)
        JPanel configuracionPanel = new JPanel(new GridBagLayout());
        configuracionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 1: Nombre del Formulario
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        configuracionPanel.add(createStyledLabel("Nombre del Formulario:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 0.8;
        configuracionPanel.add(nombreTablaField = createStyledTextField(), gbc);

        // Fila 2: Número de Columnas
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        configuracionPanel.add(createStyledLabel("Número de Columnas:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.8;
        configuracionPanel.add(numeroColumnasField = createStyledTextField(), gbc);

        // Fila 3: Botón Generar Campos
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4; // Ocupa todas las columnas
        gbc.weightx = 1.0; // Centrado
        gbc.anchor = GridBagConstraints.CENTER;
        configuracionPanel.add(generarColumnasButton = createStyledButton("Generar Campos", botonGenerarCampos, botonGenerarCamposHover), gbc);

        generarColumnasButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generarCampos();
            }
        });

        add(configuracionPanel, BorderLayout.NORTH);

        // Panel central para columnas dinámicas
        columnasPanel = new JPanel();
        columnasPanel.setLayout(new BoxLayout(columnasPanel, BoxLayout.Y_AXIS));
        columnasPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JScrollPane scrollPanel = new JScrollPane(columnasPanel);
        scrollPanel.setPreferredSize(new Dimension(500, 300));
        scrollPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),
                "Columnas Personalizadas", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), Color.DARK_GRAY));
        add(scrollPanel, BorderLayout.CENTER);

        // Botón inferior para crear tabla
        crearTablaButton = createStyledButton("Crear Tabla", botonCrearTabla, botonCrearTablaHover);
        crearTablaButton.addActionListener(e -> crearTabla());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.add(crearTablaButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private List<JTextField> camposDinamicos = new ArrayList<>(); // Lista para almacenar los campos dinámicos

    private void generarCampos() {
        // Limpiar cualquier contenido previo
        columnasPanel.removeAll();
        camposDinamicos.clear();
        usarExistente = false; // Resetear el flag

        String nombreTabla = nombreTablaField.getText().trim();
        if (nombreTabla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del formulario no puede estar vacío.");
            return;
        }

        if (tableExists(nombreTabla)) {
            int opcion = JOptionPane.showOptionDialog(this,
                    "La tabla '" + nombreTabla + "' ya existe. ¿Desea usar la existente o eliminarla y crear una nueva?",
                    "Tabla existente",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Usar existente", "Eliminar y crear nueva"},
                    "Usar existente");

            if (opcion == JOptionPane.YES_OPTION) {
                usarExistente = true;
                JOptionPane.showMessageDialog(this, "Usando la tabla existente.");
                // Opcional: Deshabilitar la generación de nuevas columnas
                crearTablaButton.setEnabled(false);
                return;
            } else if (opcion == JOptionPane.NO_OPTION) {
                if (dropTable(nombreTabla)) {
                    JOptionPane.showMessageDialog(this, "Tabla eliminada exitosamente. Puede crear una nueva.");
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar la tabla. Operación cancelada.");
                    return;
                }
            } else {
                // Usuario cerró el diálogo o canceló
                return;
            }
        }

        try {
            int numeroColumnas = Integer.parseInt(numeroColumnasField.getText());
            if (numeroColumnas <= 0) {
                JOptionPane.showMessageDialog(this, "El número de columnas debe ser mayor a 0.");
                return;
            }

            // Generar dinámicamente los campos para el número de columnas especificado
            for (int i = 0; i < numeroColumnas; i++) {
                // Crear un panel para cada columna con fondo neutro
                JPanel columnaPanel = new JPanel(new BorderLayout(10, 10));
                columnaPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(botonCrearTabla, 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                columnaPanel.setBackground(Color.WHITE); // Fondo neutro

                // Etiqueta para el nombre de la columna
                JLabel columnaLabel = createStyledLabel("Nombre de la Columna " + (i + 1) + ":");
                columnaPanel.add(columnaLabel, BorderLayout.WEST);

                // Campo de texto para ingresar el nombre de la columna
                JTextField nombreColumnaField = createStyledTextField();
                columnaPanel.add(nombreColumnaField, BorderLayout.CENTER);

                // Añadir el campo a la lista de campos dinámicos
                camposDinamicos.add(nombreColumnaField);

                // Añadir el panel al panel principal
                columnasPanel.add(columnaPanel);
                columnasPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Espacio entre filas
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un número válido para las columnas.");
        }

        // Revalidar y repintar para que los cambios sean visibles
        columnasPanel.revalidate(); // Informar al LayoutManager
        columnasPanel.repaint();    // Redibujar visualmente
    }

    private boolean tableExists(String tableName) {
        try (Connection conn = ConexionBaseDatos.getConexion()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al verificar la existencia de la tabla: " + e.getMessage());
            return false;
        }
    }

    private boolean dropTable(String tableName) {
        String sql = "DROP TABLE " + tableName;
        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al eliminar la tabla: " + e.getMessage());
            return false;
        }
    }

    private void crearTabla() {
        if (usarExistente) {
            JOptionPane.showMessageDialog(this, "Se está utilizando la tabla existente. No es necesario crear una nueva.");
            return;
        }

        String nombreTabla = nombreTablaField.getText().trim();
        if (nombreTabla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del formulario no puede estar vacío.");
            return;
        }

        StringBuilder sql = new StringBuilder("CREATE TABLE ").append(nombreTabla).append(" (");

        // Procesar los nombres de las columnas dinámicas
        for (int i = 0; i < camposDinamicos.size(); i++) {
            String nombreColumna = camposDinamicos.get(i).getText().trim();
            if (nombreColumna.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre de la columna " + (i + 1) + " está vacío.");
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

        // Ejecutar SQL para crear la tabla
        try (Connection conn = ConexionBaseDatos.getConexion();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql.toString());
            JOptionPane.showMessageDialog(this, "Tabla creada exitosamente.");
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
        label.setForeground(new Color(25, 25, 112)); // Navy Blue for consistency
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        textField.setBackground(Color.WHITE); // Fondo blanco para mejor contraste
        return textField;
    }
}
