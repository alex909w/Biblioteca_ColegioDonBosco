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

public class AgregarUsuario extends JPanel {
    private JTextField idField, nombreField, emailField, telefonoField, direccionField, fechaNacimientoField, departamentoField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;

    public AgregarUsuario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Agregar Usuario"));

        // Configuración del formulario
        JPanel formularioPanel = crearFormulario();
        add(formularioPanel, BorderLayout.CENTER);

        // Configuración de botones
        JPanel buttonPanel = crearBotones();
        add(buttonPanel, BorderLayout.SOUTH);

        // Generar ID inicial basado en el rol seleccionado
        actualizarID();
    }

    private JPanel crearFormulario() {
        JPanel formularioPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formularioPanel.add(new JLabel("ID del Usuario:"));
        idField = new JTextField();
        idField.setEnabled(false); // Campo solo lectura
        formularioPanel.add(idField);

        formularioPanel.add(new JLabel("Nombre:"));
        nombreField = new JTextField();
        formularioPanel.add(nombreField);

        formularioPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formularioPanel.add(emailField);

        formularioPanel.add(new JLabel("Rol:"));
        rolComboBox = new JComboBox<>(new String[]{"Administrador", "Profesor", "Alumno"});
        rolComboBox.addActionListener(e -> actualizarID());
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

        return formularioPanel;
    }

    private JPanel crearBotones() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Botón Agregar
        JButton agregarButton = new JButton("Agregar");
        configurarBoton(agregarButton, new Color(34, 139, 34), e -> agregarUsuario());
        buttonPanel.add(agregarButton);

        // Botón Limpiar
        JButton limpiarButton = new JButton("Limpiar");
        configurarBoton(limpiarButton, new Color(220, 53, 69), e -> limpiarFormulario());
        buttonPanel.add(limpiarButton);

        return buttonPanel;
    }

    private void configurarBoton(JButton boton, Color colorBase, java.awt.event.ActionListener accion) {
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setBackground(colorBase);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setPreferredSize(new Dimension(120, 40));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        boton.addActionListener(accion);
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorBase.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorBase);
            }
        });
    }

    private void actualizarID() {
        String rol = (String) rolComboBox.getSelectedItem();
        String prefijo;

        switch (rol) {
            case "Administrador":
                prefijo = "AD";
                break;
            case "Profesor":
                prefijo = "PR";
                break;
            case "Alumno":
                prefijo = "AL";
                break;
            default:
                prefijo = "XX";
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT COUNT(*) + 1 AS siguiente_id FROM usuarios WHERE rol = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, rol);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int siguienteID = rs.getInt("siguiente_id");
                idField.setText(String.format("%s%05d", prefijo, siguienteID));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al generar el ID: " + ex.getMessage());
        }
    }

    private void agregarUsuario() {
        if (!validarFormulario()) return;

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "INSERT INTO usuarios (id, nombre, email, rol, contraseña, telefono, direccion, fecha_nacimiento, departamento) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idField.getText());
            stmt.setString(2, nombreField.getText());
            stmt.setString(3, emailField.getText());
            stmt.setString(4, (String) rolComboBox.getSelectedItem());
            stmt.setString(5, new String(passwordField.getPassword()));
            stmt.setString(6, telefonoField.getText());
            stmt.setString(7, direccionField.getText());
            stmt.setString(8, fechaNacimientoField.getText());
            stmt.setString(9, departamentoField.getText());

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Usuario agregado exitosamente.");
            limpiarFormulario();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al agregar usuario: " + ex.getMessage());
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
        nombreField.setText("");
        emailField.setText("");
        passwordField.setText("");
        telefonoField.setText("");
        direccionField.setText("");
        fechaNacimientoField.setText("");
        departamentoField.setText("");
        rolComboBox.setSelectedIndex(0);
        actualizarID();
    }
}
