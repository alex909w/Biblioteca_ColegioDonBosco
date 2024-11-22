package com.biblioteca.Panel.Config;

import com.biblioteca.controller.ConfiguracionController;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE); // Fondo blanco para un diseño limpio
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título del panel
        JLabel titleLabel = new JLabel("Configuración de Roles");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Panel de contenido
        JPanel contentPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180)),
                "Configuración por Rol",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.PLAIN, 14),
                new Color(70, 130, 180)
        ));

        // Configuración de etiquetas y campos
        contentPanel.add(createLabel("Rol:"));
        rolComboBox = createComboBox(new String[]{"Administrador", "Profesor", "Alumno"});
        rolComboBox.addActionListener(e -> cargarConfiguracion());
        contentPanel.add(rolComboBox);

        contentPanel.add(createLabel("Mora diaria (USD):"));
        moraField = createTextField();
        contentPanel.add(moraField);

        contentPanel.add(createLabel("Límite de préstamos:"));
        limitePrestamosField = createTextField();
        contentPanel.add(limitePrestamosField);

        contentPanel.add(createLabel("Límite de días de préstamo:"));
        limiteDiasField = createTextField();
        contentPanel.add(limiteDiasField);

        add(contentPanel, BorderLayout.CENTER);

        // Botón de guardar
        guardarButton = new JButton("Guardar");
        guardarButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        guardarButton.setBackground(new Color(70, 130, 180));
        guardarButton.setForeground(Color.WHITE);
        guardarButton.addActionListener(e -> guardarConfiguracion());
        add(createButtonPanel(guardarButton), BorderLayout.SOUTH);

        // Cargar configuración inicial
        cargarConfiguracion();
    }

    private void cargarConfiguracion() {
        String rol = rolComboBox.getSelectedItem().toString().toLowerCase();

        try {
            Map<String, Double> configuraciones = configuracionController.obtenerConfiguracionPorRol(rol);

            String claveMora = "mora_" + rol;
            String claveLimitePrestamos = "limite_prestamos_" + rol;
            String claveLimiteDias = "limite_dias_" + rol;

            moraField.setText(configuraciones.containsKey(claveMora) ? String.valueOf(configuraciones.get(claveMora)) : "");
            limitePrestamosField.setText(configuraciones.containsKey(claveLimitePrestamos) ? String.valueOf(configuraciones.get(claveLimitePrestamos).intValue()) : "");
            limiteDiasField.setText(configuraciones.containsKey(claveLimiteDias) ? String.valueOf(configuraciones.get(claveLimiteDias).intValue()) : "");

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

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(new Color(70, 130, 180));
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        return textField;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.BLACK);
        return comboBox;
    }

    private JPanel createButtonPanel(JButton button) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Color.WHITE);
        panel.add(button);
        return panel;
    }
}
