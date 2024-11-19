package com.biblioteca.acciones.Usuarios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AgregarUsuario extends JPanel {
    private JTextField nombreField, emailField, telefonoField, direccionField, fechaNacimientoField, departamentoField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;

    public AgregarUsuario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Agregar Usuario"));

        // Panel central con los campos del formulario
        JPanel formularioPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formularioPanel.add(new JLabel("Nombre:"));
        nombreField = new JTextField();
        formularioPanel.add(nombreField);

        formularioPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formularioPanel.add(emailField);

        formularioPanel.add(new JLabel("Rol:"));
        rolComboBox = new JComboBox<>(new String[]{"Administrador", "Profesor", "Alumno"});
        formularioPanel.add(rolComboBox);

        formularioPanel.add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField();
        formularioPanel.add(passwordField);

        formularioPanel.add(new JLabel("Teléfono:"));
        telefonoField = new JTextField();
        formularioPanel.add(telefonoField);

        formularioPanel.add(new JLabel("Dirección:"));
        direccionField = new JTextField();
        formularioPanel.add(direccionField);

        formularioPanel.add(new JLabel("Fecha de Nacimiento (YYYY-MM-DD):"));
        fechaNacimientoField = new JTextField();
        formularioPanel.add(fechaNacimientoField);

        formularioPanel.add(new JLabel("Departamento:"));
        departamentoField = new JTextField();
        formularioPanel.add(departamentoField);

        add(formularioPanel, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Botón Agregar
        JButton agregarButton = new JButton("Agregar");
        agregarButton.setFont(new Font("Arial", Font.BOLD, 14));
        agregarButton.setBackground(new Color(34, 139, 34)); // Verde
        agregarButton.setForeground(Color.WHITE);
        agregarButton.setFocusPainted(false);
        agregarButton.setPreferredSize(new Dimension(120, 40));
        agregarButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        agregarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                agregarButton.setBackground(new Color(0, 100, 0)); // Verde oscuro al pasar
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                agregarButton.setBackground(new Color(34, 139, 34)); // Color original
            }
        });
        agregarButton.addActionListener(e -> agregarUsuario());
        buttonPanel.add(agregarButton);

        // Botón Limpiar
        JButton limpiarButton = new JButton("Limpiar");
        limpiarButton.setFont(new Font("Arial", Font.BOLD, 14));
        limpiarButton.setBackground(new Color(220, 53, 69)); // Rojo
        limpiarButton.setForeground(Color.WHITE);
        limpiarButton.setFocusPainted(false);
        limpiarButton.setPreferredSize(new Dimension(120, 40));
        limpiarButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        limpiarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                limpiarButton.setBackground(new Color(176, 0, 32)); // Rojo oscuro al pasar
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                limpiarButton.setBackground(new Color(220, 53, 69)); // Color original
            }
        });
        limpiarButton.addActionListener(e -> limpiarFormulario());
        buttonPanel.add(limpiarButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void agregarUsuario() {
        String nombre = nombreField.getText();
        String email = emailField.getText();
        String rol = (String) rolComboBox.getSelectedItem();
        String contraseña = new String(passwordField.getPassword());
        String telefono = telefonoField.getText();
        String direccion = direccionField.getText();
        String fechaNacimiento = fechaNacimientoField.getText();
        String departamento = departamentoField.getText();

        if (nombre.isEmpty() || email.isEmpty() || contraseña.isEmpty() || fechaNacimiento.isEmpty() || departamento.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos obligatorios.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "INSERT INTO usuarios (nombre, email, rol, contraseña, telefono, direccion, fecha_nacimiento, departamento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, email);
            stmt.setString(3, rol);
            stmt.setString(4, contraseña);
            stmt.setString(5, telefono);
            stmt.setString(6, direccion);
            stmt.setString(7, fechaNacimiento);
            stmt.setString(8, departamento);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Usuario agregado exitosamente.");
            limpiarFormulario(); // Limpia el formulario tras agregar
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al agregar usuario: " + ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        nombreField.setText("");
        emailField.setText("");
        passwordField.setText("");
        telefonoField.setText("");
        direccionField.setText("");
        fechaNacimientoField.setText("");
        departamentoField.setText("");
        rolComboBox.setSelectedIndex(0);
    }
}
