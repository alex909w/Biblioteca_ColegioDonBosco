package com.biblioteca.Formularios;


import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CrearFormulario extends JPanel {
    private JTextField nombreTablaField;
    private JTextField numeroColumnasField;
    private JPanel columnasPanel;
    private JButton generarColumnasButton, crearTablaButton;

    public CrearFormulario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Crear Nuevo Formulario"));

        JPanel configuracionPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        configuracionPanel.add(new JLabel("Nombre del Formulario:"));
        nombreTablaField = new JTextField();
        configuracionPanel.add(nombreTablaField);

        configuracionPanel.add(new JLabel("Número de Columnas Personalizadas:"));
        numeroColumnasField = new JTextField();
        configuracionPanel.add(numeroColumnasField);

        generarColumnasButton = new JButton("Generar Campos");
        generarColumnasButton.addActionListener(e -> generarCampos());
        configuracionPanel.add(generarColumnasButton);

        add(configuracionPanel, BorderLayout.NORTH);

        columnasPanel = new JPanel();
        columnasPanel.setLayout(new BoxLayout(columnasPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPanel = new JScrollPane(columnasPanel);
        add(scrollPanel, BorderLayout.CENTER);

        crearTablaButton = new JButton("Crear Tabla");
        crearTablaButton.addActionListener(e -> crearTabla());
        add(crearTablaButton, BorderLayout.SOUTH);
    }

    private void generarCampos() {
        columnasPanel.removeAll(); // Limpiar panel de columnas previas
        try {
            int numeroColumnas = Integer.parseInt(numeroColumnasField.getText());
            for (int i = 0; i < numeroColumnas; i++) {
                JPanel columnaPanel = new JPanel(new GridLayout(1, 2, 10, 10));
                columnaPanel.add(new JLabel("Nombre de la Columna " + (i + 1) + ":"));
                JTextField nombreColumnaField = new JTextField();
                columnaPanel.add(nombreColumnaField);
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
}
