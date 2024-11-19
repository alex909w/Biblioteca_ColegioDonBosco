package com.biblioteca.Formularios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.border.TitledBorder;

public class CrearFormulario extends JPanel {
    private JTextField nombreTablaField;
    private JTextField numeroColumnasField;
    private JPanel columnasPanel;
    private JButton generarColumnasButton, crearTablaButton;

    public CrearFormulario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)),
                "Crear Nuevo Formulario", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16), new Color(50, 50, 50)));

        // Panel superior con configuraciones
        JPanel configuracionPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        configuracionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel nombreTablaLabel = new JLabel("Nombre del Formulario:");
        nombreTablaLabel.setFont(new Font("Arial", Font.BOLD, 14));
        configuracionPanel.add(nombreTablaLabel);

        nombreTablaField = new JTextField();
        nombreTablaField.setFont(new Font("Arial", Font.PLAIN, 14));
        nombreTablaField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        configuracionPanel.add(nombreTablaField);

        JLabel numeroColumnasLabel = new JLabel("Número de Columnas Personalizadas:");
        numeroColumnasLabel.setFont(new Font("Arial", Font.BOLD, 14));
        configuracionPanel.add(numeroColumnasLabel);

        numeroColumnasField = new JTextField();
        numeroColumnasField.setFont(new Font("Arial", Font.PLAIN, 14));
        numeroColumnasField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        configuracionPanel.add(numeroColumnasField);

        generarColumnasButton = createStyledButton("Generar Campos", new Color(34, 139, 34), new Color(0, 100, 0));
        generarColumnasButton.addActionListener(e -> generarCampos());
        configuracionPanel.add(generarColumnasButton);

        add(configuracionPanel, BorderLayout.NORTH);

        // Panel central para columnas dinámicas
        columnasPanel = new JPanel();
        columnasPanel.setLayout(new BoxLayout(columnasPanel, BoxLayout.Y_AXIS));
        columnasPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JScrollPane scrollPanel = new JScrollPane(columnasPanel);
        scrollPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(scrollPanel, BorderLayout.CENTER);

        // Botón inferior para crear tabla
        crearTablaButton = createStyledButton("Crear Tabla", new Color(220, 53, 69), new Color(176, 0, 32));
        crearTablaButton.addActionListener(e -> crearTabla());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(crearTablaButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void generarCampos() {
        columnasPanel.removeAll(); // Limpiar panel de columnas previas
        try {
            int numeroColumnas = Integer.parseInt(numeroColumnasField.getText());
            for (int i = 0; i < numeroColumnas; i++) {
                JPanel columnaPanel = new JPanel(new BorderLayout(10, 10));
                columnaPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                JLabel columnaLabel = new JLabel("Nombre de la Columna " + (i + 1) + ":");
                columnaLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                columnaPanel.add(columnaLabel, BorderLayout.WEST);

                JTextField nombreColumnaField = new JTextField();
                nombreColumnaField.setFont(new Font("Arial", Font.PLAIN, 14));
                nombreColumnaField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                columnaPanel.add(nombreColumnaField, BorderLayout.CENTER);

                columnasPanel.add(columnaPanel);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un número válido para las columnas.");
        }
        columnasPanel.revalidate();
        columnasPanel.repaint();
    }

    private void crearTabla() {
        String nombreTabla = nombreTablaField.getText();
        if (nombreTabla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del formulario no puede estar vacío.");
            return;
        }

        StringBuilder sql = new StringBuilder("CREATE TABLE ").append(nombreTabla).append(" (");

        // Agregar columnas personalizadas
        Component[] componentes = columnasPanel.getComponents();
        for (Component componente : componentes) {
            if (componente instanceof JPanel) {
                JPanel columnaPanel = (JPanel) componente;
                Component[] campos = columnaPanel.getComponents();
                if (campos[1] instanceof JTextField) {
                    JTextField nombreColumnaField = (JTextField) campos[1];
                    String nombreColumna = nombreColumnaField.getText();
                    if (!nombreColumna.isEmpty()) {
                        sql.append(nombreColumna).append(" VARCHAR(255), ");
                    }
                }
            }
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
        button.setPreferredSize(new Dimension(150, 40));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
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
}
