package com.biblioteca.acciones.Usuarios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditarUsuario extends JPanel {
    private JTextField idUsuarioField, nombreField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;
    private JButton buscarButton, actualizarButton;

    public EditarUsuario() {
        setLayout(new GridLayout(6, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Editar Usuario"));

        add(new JLabel("ID Usuario:"));
        idUsuarioField = new JTextField();
        add(idUsuarioField);

        buscarButton = new JButton("Buscar");
        buscarButton.addActionListener(e -> buscarUsuario());
        add(buscarButton);

        add(new JLabel("Nombre:"));
        nombreField = new JTextField();
        nombreField.setEnabled(false);
        add(nombreField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        emailField.setEnabled(false);
        add(emailField);

        add(new JLabel("Rol:"));
        rolComboBox = new JComboBox<>(new String[]{"Administrador", "Profesor", "Alumno"});
        rolComboBox.setEnabled(false);
        add(rolComboBox);

        add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField();
        passwordField.setEnabled(false);
        add(passwordField);

        actualizarButton = new JButton("Actualizar");
        actualizarButton.setEnabled(false);
        actualizarButton.addActionListener(e -> actualizarUsuario());
        add(actualizarButton);
    }

    private void buscarUsuario() {
        String idUsuario = idUsuarioField.getText();
        if (idUsuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese el ID del usuario.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idUsuario);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nombreField.setText(rs.getString("nombre"));
                emailField.setText(rs.getString("email"));
                rolComboBox.setSelectedItem(rs.getString("rol"));
                passwordField.setText(rs.getString("contraseña"));

                nombreField.setEnabled(true);
                emailField.setEnabled(true);
                rolComboBox.setEnabled(true);
                passwordField.setEnabled(true);
                actualizarButton.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar usuario: " + ex.getMessage());
        }
    }

    private void actualizarUsuario() {
        String idUsuario = idUsuarioField.getText();
        String nombre = nombreField.getText();
        String email = emailField.getText();
        String rol = (String) rolComboBox.getSelectedItem();
        String contraseña = new String(passwordField.getPassword());

        if (nombre.isEmpty() || email.isEmpty() || contraseña.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "UPDATE usuarios SET nombre = ?, email = ?, rol = ?, contraseña = ? WHERE id_usuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, email);
            stmt.setString(3, rol);
            stmt.setString(4, contraseña);
            stmt.setString(5, idUsuario);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Usuario actualizado exitosamente.");
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el usuario.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar usuario: " + ex.getMessage());
        }
    }
}
