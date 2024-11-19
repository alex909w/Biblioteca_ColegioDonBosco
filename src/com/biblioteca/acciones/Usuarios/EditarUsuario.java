package com.biblioteca.acciones.Usuarios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditarUsuario extends JPanel {
    private JTextField idUsuarioField, nombreField, emailField, telefonoField, direccionField, fechaNacimientoField, departamentoField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;
    private JButton buscarButton, actualizarButton, limpiarButton;

    public EditarUsuario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Editar Usuario"));

        // Panel superior para ID del usuario
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        idPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        idPanel.add(new JLabel("ID Usuario:"));
        idUsuarioField = new JTextField(10);
        idPanel.add(idUsuarioField);

        buscarButton = new JButton("Buscar");
        buscarButton.setFont(new Font("Arial", Font.BOLD, 14));
        buscarButton.setBackground(new Color(34, 139, 34)); // Verde
        buscarButton.setForeground(Color.WHITE);
        buscarButton.setFocusPainted(false);
        buscarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buscarButton.setBackground(new Color(0, 100, 0)); // Verde oscuro
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                buscarButton.setBackground(new Color(34, 139, 34)); // Color original
            }
        });
        buscarButton.addActionListener(e -> buscarUsuario());
        idPanel.add(buscarButton);

        add(idPanel, BorderLayout.NORTH);

        // Panel central con columnas
        JPanel columnasPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        columnasPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Columna 1
        JPanel columna1 = new JPanel(new GridLayout(4, 1, 10, 10));
        columna1.add(new JLabel("Nombre:"));
        nombreField = new JTextField();
        nombreField.setEnabled(false);
        columna1.add(nombreField);

        columna1.add(new JLabel("Email:"));
        emailField = new JTextField();
        emailField.setEnabled(false);
        columna1.add(emailField);

        columna1.add(new JLabel("Teléfono:"));
        telefonoField = new JTextField();
        telefonoField.setEnabled(false);
        columna1.add(telefonoField);

        columna1.add(new JLabel("Fecha de Nacimiento (YYYY-MM-DD):"));
        fechaNacimientoField = new JTextField();
        fechaNacimientoField.setEnabled(false);
        columna1.add(fechaNacimientoField);

        // Columna 2
        JPanel columna2 = new JPanel(new GridLayout(4, 1, 10, 10));
        columna2.add(new JLabel("Rol:"));
        rolComboBox = new JComboBox<>(new String[]{"Administrador", "Profesor", "Alumno"});
        rolComboBox.setEnabled(false);
        columna2.add(rolComboBox);

        columna2.add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField();
        passwordField.setEnabled(false);
        columna2.add(passwordField);

        columna2.add(new JLabel("Dirección:"));
        direccionField = new JTextField();
        direccionField.setEnabled(false);
        columna2.add(direccionField);

        columna2.add(new JLabel("Departamento:"));
        departamentoField = new JTextField();
        departamentoField.setEnabled(false);
        columna2.add(departamentoField);

        columnasPanel.add(columna1);
        columnasPanel.add(columna2);

        add(columnasPanel, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Botón Actualizar
        actualizarButton = new JButton("Actualizar");
        actualizarButton.setFont(new Font("Arial", Font.BOLD, 14));
        actualizarButton.setBackground(new Color(255, 140, 0)); // Naranja
        actualizarButton.setForeground(Color.WHITE);
        actualizarButton.setFocusPainted(false);
        actualizarButton.setEnabled(false);
        actualizarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                actualizarButton.setBackground(new Color(255, 120, 0)); // Naranja oscuro
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                actualizarButton.setBackground(new Color(255, 140, 0)); // Color original
            }
        });
        actualizarButton.addActionListener(e -> actualizarUsuario());
        buttonPanel.add(actualizarButton);

        // Botón Limpiar
        limpiarButton = new JButton("Limpiar");
        limpiarButton.setFont(new Font("Arial", Font.BOLD, 14));
        limpiarButton.setBackground(new Color(220, 53, 69)); // Rojo
        limpiarButton.setForeground(Color.WHITE);
        limpiarButton.setFocusPainted(false);
        limpiarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                limpiarButton.setBackground(new Color(176, 0, 32)); // Rojo oscuro
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                limpiarButton.setBackground(new Color(220, 53, 69)); // Color original
            }
        });
        limpiarButton.addActionListener(e -> limpiarFormulario());
        buttonPanel.add(limpiarButton);

        add(buttonPanel, BorderLayout.SOUTH);
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
                telefonoField.setText(rs.getString("telefono"));
                direccionField.setText(rs.getString("direccion"));
                fechaNacimientoField.setText(rs.getString("fecha_nacimiento"));
                departamentoField.setText(rs.getString("departamento"));

                nombreField.setEnabled(true);
                emailField.setEnabled(true);
                rolComboBox.setEnabled(true);
                passwordField.setEnabled(true);
                telefonoField.setEnabled(true);
                direccionField.setEnabled(true);
                fechaNacimientoField.setEnabled(true);
                departamentoField.setEnabled(true);
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
        String telefono = telefonoField.getText();
        String direccion = direccionField.getText();
        String fechaNacimiento = fechaNacimientoField.getText();
        String departamento = departamentoField.getText();

        if (nombre.isEmpty() || email.isEmpty() || contraseña.isEmpty() || rol == null) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos obligatorios.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "UPDATE usuarios SET nombre = ?, email = ?, rol = ?, contraseña = ?, telefono = ?, direccion = ?, fecha_nacimiento = ?, departamento = ? WHERE id_usuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, email);
            stmt.setString(3, rol);
            stmt.setString(4, contraseña);
            stmt.setString(5, telefono);
            stmt.setString(6, direccion);
            stmt.setString(7, fechaNacimiento);
            stmt.setString(8, departamento);
            stmt.setString(9, idUsuario);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Usuario actualizado exitosamente.");
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el usuario.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar usuario: " + ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        idUsuarioField.setText("");
        nombreField.setText("");
        emailField.setText("");
        passwordField.setText("");
        telefonoField.setText("");
        direccionField.setText("");
        fechaNacimientoField.setText("");
        departamentoField.setText("");
        rolComboBox.setSelectedIndex(0);
        nombreField.setEnabled(false);
        emailField.setEnabled(false);
        rolComboBox.setEnabled(false);
        passwordField.setEnabled(false);
        telefonoField.setEnabled(false);
        direccionField.setEnabled(false);
        fechaNacimientoField.setEnabled(false);
        departamentoField.setEnabled(false);
        actualizarButton.setEnabled(false);
    }
}
