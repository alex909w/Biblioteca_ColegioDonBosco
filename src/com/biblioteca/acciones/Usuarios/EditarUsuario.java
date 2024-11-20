package com.biblioteca.acciones.Usuarios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class EditarUsuario extends JPanel {
    private JComboBox<String> idUsuarioComboBox;
    private JTextField nombreField, emailField, telefonoField, direccionField, fechaNacimientoField, departamentoField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;
    private JButton actualizarButton, limpiarButton;

    public EditarUsuario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Editar Usuario"));

        // Panel superior para ID del usuario
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        idPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        idPanel.add(new JLabel("ID Usuario:"));
        idUsuarioComboBox = new JComboBox<>();
        idUsuarioComboBox.setEditable(true); // Permite la búsqueda
        cargarUsuariosEnComboBox(); // Cargar los IDs de usuarios
        idUsuarioComboBox.addActionListener(e -> cargarUsuarioSeleccionado());
        idPanel.add(idUsuarioComboBox);

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
        actualizarButton.addActionListener(e -> actualizarUsuario());
        buttonPanel.add(actualizarButton);

        // Botón Limpiar
        limpiarButton = new JButton("Limpiar");
        limpiarButton.setFont(new Font("Arial", Font.BOLD, 14));
        limpiarButton.setBackground(new Color(220, 53, 69)); // Rojo
        limpiarButton.setForeground(Color.WHITE);
        limpiarButton.setFocusPainted(false);
        limpiarButton.addActionListener(e -> limpiarFormulario());
        buttonPanel.add(limpiarButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void cargarUsuariosEnComboBox() {
        try (Connection conn = ConexionBaseDatos.getConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM usuarios")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                idUsuarioComboBox.addItem(rs.getString("id"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage());
        }
    }

    private void cargarUsuarioSeleccionado() {
        String idUsuario = (String) idUsuarioComboBox.getSelectedItem();
        if (idUsuario == null || idUsuario.isEmpty()) return;

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT * FROM usuarios WHERE id = ?";
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
                limpiarFormulario();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuario: " + ex.getMessage());
        }
    }

    private void actualizarUsuario() {
        String idUsuario = (String) idUsuarioComboBox.getSelectedItem();
        String nombre = nombreField.getText();
        String email = emailField.getText();
        String rol = (String) rolComboBox.getSelectedItem();
        String contraseña = new String(passwordField.getPassword());
        String telefono = telefonoField.getText();
        String direccion = direccionField.getText();
        String fechaNacimiento = fechaNacimientoField.getText();
        String departamento = departamentoField.getText();

        if (!validarFormulario()) return;
    
        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "UPDATE usuarios SET nombre = ?, email = ?, rol = ?, contraseña = ?, telefono = ?, direccion = ?, fecha_nacimiento = ?, departamento = ? WHERE id = ?";
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
    private boolean validarFormulario() {
        String nombre = nombreField.getText().trim();
        String email = emailField.getText().trim();
        String contraseña = new String(passwordField.getPassword()).trim();
        String telefono = telefonoField.getText().trim();
        String fechaNacimiento = fechaNacimientoField.getText().trim();

        if (nombre.isEmpty() || email.isEmpty() || contraseña.isEmpty() || fechaNacimiento.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos obligatorios.");
            return false;
        }

        if (!Pattern.matches("^[a-zA-Z\\s]+$", nombre)) {
            JOptionPane.showMessageDialog(this, "El nombre solo debe contener letras y espacios.");
            return false;
        }

        if (!Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", email)) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un correo electrónico válido.");
            return false;
        }

        if (contraseña.length() < 8) {
            JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 8 caracteres.");
            return false;
        }

        if (!telefono.isEmpty() && !Pattern.matches("^\\d{8}$", telefono)) {
            JOptionPane.showMessageDialog(this, "El número de teléfono debe contener 18 dígitos.");
            return false;
        }

        try {
            LocalDate.parse(fechaNacimiento);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "La fecha de nacimiento debe estar en formato YYYY-MM-DD.");
            return false;
        }

        return true;
    }

    private void limpiarFormulario() {
        idUsuarioComboBox.setSelectedItem(null);
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
