package com.biblioteca.Panel.Config;

import com.biblioteca.controller.ConfiguracionController;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ConfiguracionRoles extends JPanel {
    private final JComboBox<String> rolComboBox;
    private final JTextField moraField;
    private final JTextField limitePrestamosField;
    private final JTextField limiteDiasField;
    private final JButton guardarButton;

    private final ConfiguracionController configuracionController;

    public ConfiguracionRoles() {
        configuracionController = new ConfiguracionController();

        setLayout(new GridLayout(5, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Configuración por Rol"));

        // Etiqueta y ComboBox para el rol
        add(new JLabel("Rol:"));
        rolComboBox = new JComboBox<>(new String[]{"Administrador", "Profesor", "Alumno"});
        rolComboBox.addActionListener(e -> cargarConfiguracion());
        add(rolComboBox);

        // Etiqueta y campo para la mora diaria
        add(new JLabel("Mora diaria (USD):"));
        moraField = new JTextField();
        add(moraField);

        // Etiqueta y campo para el límite de préstamos
        add(new JLabel("Límite de préstamos:"));
        limitePrestamosField = new JTextField();
        add(limitePrestamosField);

        // Etiqueta y campo para el límite de días de préstamo
        add(new JLabel("Límite de días de préstamo:"));
        limiteDiasField = new JTextField();
        add(limiteDiasField);

        // Botón para guardar los cambios
        guardarButton = new JButton("Guardar");
        guardarButton.addActionListener(e -> guardarConfiguracion());
        add(new JLabel()); // Espaciador
        add(guardarButton);

        // Cargar configuración inicial para el rol seleccionado
        cargarConfiguracion();
    }

    private void cargarConfiguracion() {
        String rol = rolComboBox.getSelectedItem().toString().toLowerCase();

        try {
            Map<String, Double> configuraciones = configuracionController.obtenerConfiguracionPorRol(rol);

            String claveMora = "mora_" + rol;
            String claveLimitePrestamos = "limite_prestamos_" + rol;
            String claveLimiteDias = "limite_dias_" + rol;

            if (configuraciones.containsKey(claveMora)) {
                moraField.setText(String.valueOf(configuraciones.get(claveMora)));
            } else {
                moraField.setText("");
            }

            if (configuraciones.containsKey(claveLimitePrestamos)) {
                limitePrestamosField.setText(String.valueOf(configuraciones.get(claveLimitePrestamos).intValue()));
            } else {
                limitePrestamosField.setText("");
            }

            if (configuraciones.containsKey(claveLimiteDias)) {
                limiteDiasField.setText(String.valueOf(configuraciones.get(claveLimiteDias).intValue()));
            } else {
                limiteDiasField.setText("");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar configuración: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarConfiguracion() {
        String rol = rolComboBox.getSelectedItem().toString().toLowerCase();
        double mora;
        int limitePrestamos;
        int limiteDias;

        try {
            mora = Double.parseDouble(moraField.getText());
            limitePrestamos = Integer.parseInt(limitePrestamosField.getText());
            limiteDias = Integer.parseInt(limiteDiasField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese valores válidos para la mora, límite de préstamos y límite de días.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Map<String, Double> valores = new HashMap<>();
            valores.put("mora_" + rol, mora);
            valores.put("limite_prestamos_" + rol, (double) limitePrestamos);
            valores.put("limite_dias_" + rol, (double) limiteDias);

            configuracionController.actualizarConfiguracionPorRol(rol, valores);

            JOptionPane.showMessageDialog(this, "Configuración actualizada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar configuración: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
