package com.biblioteca.acciones.Configuraciones;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class ConfiguracionRoles extends JPanel {
    private JComboBox<String> rolComboBox;
    private JTextField moraField;
    private JTextField limitePrestamosField;
    private JTextField limiteDiasField; // Nuevo campo para límite de días
    private JButton guardarButton;

    public ConfiguracionRoles() {
        setLayout(new GridLayout(5, 2, 10, 10)); // Incrementado el número de filas
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
        add(new JLabel("Límite de días de préstamo:")); // Nueva etiqueta
        limiteDiasField = new JTextField(); // Nuevo campo
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

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Consulta para obtener la mora diaria, límite de préstamos y límite de días para el rol
            String query = "SELECT clave, valor FROM configuraciones WHERE clave IN (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setString(1, "mora_" + rol);
            stmt.setString(2, "limite_prestamos_" + rol);
            stmt.setString(3, "limite_dias_" + rol);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
    String clave = rs.getString("clave");
    double valor = rs.getDouble("valor");

    if (clave.equals("mora_" + rol)) {
        moraField.setText(String.valueOf(valor));
    } else if (clave.equals("limite_prestamos_" + rol)) {
        limitePrestamosField.setText(String.valueOf((int) valor));
    } else if (clave.equals("limite_dias_" + rol)) {
        limiteDiasField.setText(String.valueOf((int) valor));
    }
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

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Actualizar mora diaria para el rol
            String updateQuery = "UPDATE configuraciones SET valor = ? WHERE clave = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);

            updateStmt.setDouble(1, mora);
            updateStmt.setString(2, "mora_" + rol);
            updateStmt.executeUpdate();

            // Actualizar límite de préstamos para el rol
            updateStmt.setDouble(1, limitePrestamos);
            updateStmt.setString(2, "limite_prestamos_" + rol);
            updateStmt.executeUpdate();

            // Actualizar límite de días de préstamo para el rol
            updateStmt.setDouble(1, limiteDias);
            updateStmt.setString(2, "limite_dias_" + rol);
            updateStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Configuración actualizada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar configuración: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
