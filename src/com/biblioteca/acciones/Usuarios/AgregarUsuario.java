package com.biblioteca.acciones.Usuarios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AgregarUsuario extends JPanel {
    private JTextField nombreField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;

    public AgregarUsuario() {
        setLayout(new GridLayout(5, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Agregar Usuario"));

        // Campos del formulario
        add(new JLabel("Nombre:"));
        nombreField = new JTextField();
        add(nombreField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Rol:"));
        rolComboBox = new JComboBox<>(new String[]{"Administrador", "Profesor", "Alumno"});
        add(rolComboBox);

        add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton agregarButton = new JButton("Agregar");
        agregarButton.addActionListener(e -> agregarUsuario());
        add(agregarButton);
    }

    private void agregarUsuario() {
        String nombre = nombreField.getText();
        String email = emailField.getText();
        String rol = (String) rolComboBox.getSelectedItem();
        String contraseña = new String(passwordField.getPassword());

        if (nombre.isEmpty() || email.isEmpty() || contraseña.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "INSERT INTO usuarios (nombre, email, rol, contraseña) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, email);
            stmt.setString(3, rol);
            stmt.setString(4, contraseña);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Usuario agregado exitosamente.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al agregar usuario: " + ex.getMessage());
        }
    }
}
