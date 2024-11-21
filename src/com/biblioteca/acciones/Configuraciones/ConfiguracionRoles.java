package com.biblioteca.acciones.Configuraciones;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfiguracionRoles extends JPanel {
    private JComboBox<String> rolComboBox;
    private JTextField moraField;
    private JTextField limitePrestamosField;
    private JButton guardarButton;

    public ConfiguracionRoles() {
        setLayout(new GridLayout(4, 2, 10, 10));
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

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Consulta para obtener la mora diaria y el límite de préstamos para el rol
            String moraQuery = "SELECT valor FROM configuraciones WHERE clave = ?";
            PreparedStatement moraStmt = conn.prepareStatement(moraQuery);

            moraStmt.setString(1, "mora_" + rol);
            ResultSet moraRs = moraStmt.executeQuery();
            if (moraRs.next()) {
                moraField.setText(String.valueOf(moraRs.getDouble("valor")));
            }

            moraStmt.setString(1, "limite_prestamos_" + rol);
            ResultSet limiteRs = moraStmt.executeQuery();
            if (limiteRs.next()) {
                limitePrestamosField.setText(String.valueOf(limiteRs.getInt("valor")));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar configuración: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarConfiguracion() {
        String rol = rolComboBox.getSelectedItem().toString().toLowerCase();
        double mora;
        int limitePrestamos;

        try {
            mora = Double.parseDouble(moraField.getText());
            limitePrestamos = Integer.parseInt(limitePrestamosField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese valores válidos para la mora y el límite de préstamos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Actualizar mora diaria para el rol
            String updateMoraQuery = "UPDATE configuraciones SET valor = ? WHERE clave = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateMoraQuery);

            updateStmt.setDouble(1, mora);
            updateStmt.setString(2, "mora_" + rol);
            updateStmt.executeUpdate();

            // Actualizar límite de préstamos para el rol
            updateStmt.setDouble(1, limitePrestamos);
            updateStmt.setString(2, "limite_prestamos_" + rol);
            updateStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Configuración actualizada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar configuración: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
