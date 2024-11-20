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
    
    private final Color FONDO_LATERAL = new Color(248, 249, 250);
    private final Font FUENTE_PRINCIPAL = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);

    public EditarUsuario() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(FONDO_LATERAL);

        // Panel superior para ID del usuario
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        idPanel.setBackground(getBackground());
        idPanel.add(createLabel("ID Usuario:"));
        idUsuarioComboBox = crearCombobox();
        cargarUsuariosEnComboBox();
        idUsuarioComboBox.addActionListener(e -> cargarUsuarioSeleccionado());
        idPanel.add(idUsuarioComboBox);
        add(idPanel, BorderLayout.NORTH);

        // Panel central
        JPanel formularioPanel = new JPanel();
        formularioPanel.setLayout(new BoxLayout(formularioPanel, BoxLayout.Y_AXIS));
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        formularioPanel.setBackground(FONDO_LATERAL);

        formularioPanel.add(crearCampo("Nombre:", nombreField = new JTextField(), false));
        formularioPanel.add(crearCampo("Email:", emailField = new JTextField(), false));
        formularioPanel.add(crearCombo("Rol:", rolComboBox = new JComboBox<>(new String[]{"Administrador", "Profesor", "Alumno"})));
        formularioPanel.add(crearCampo("Contraseña:", passwordField = new JPasswordField(), false));
        formularioPanel.add(crearCampo("Teléfono:", telefonoField = new JTextField(), false));
        formularioPanel.add(crearCampo("Dirección:", direccionField = new JTextField(), false));
        formularioPanel.add(crearCampo("Fecha de Nacimiento (YYYY-MM-DD):", fechaNacimientoField = new JTextField(), false));
        formularioPanel.add(crearCampo("Departamento:", departamentoField = new JTextField(), false));

        add(formularioPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));

        JButton agregarButton = crearBoton("Actualizar", new Color(21, 156, 6));
        agregarButton.addActionListener(e -> actualizarUsuario());
        buttonPanel.add(agregarButton);

        JButton limpiarButton = crearBoton("Limpiar", new Color(247, 185, 0));
        limpiarButton.addActionListener(e -> limpiarFormulario());
        buttonPanel.add(limpiarButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
  
    private JComboBox<String> crearCombobox(String... items){
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(FUENTE_PRINCIPAL);
        return comboBox;
        
    }
    
        private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FUENTE_TITULO);
        return label;
    }
    
    private JPanel crearCampo(String etiqueta, JComponent campo, boolean soloLectura) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(250, 250, 250));

        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setPreferredSize(new Dimension(200, 30));
        label.setForeground(new Color(50, 50, 50));

        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setPreferredSize(new Dimension(200, 30));
        campo.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        if (campo instanceof JTextField) {
            ((JTextField) campo).setEnabled(!soloLectura);
        }

        panel.add(label, BorderLayout.WEST);
        panel.add(campo, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        return panel;
    }

    private JPanel crearCombo(String etiqueta, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(250, 250, 250));

        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setPreferredSize(new Dimension(200, 30));
        label.setForeground(new Color(50, 50, 50));

        panel.add(label, BorderLayout.WEST);
        panel.add(comboBox, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        return panel;
    }

    private JButton crearBoton(String texto, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(colorFondo);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setOpaque(true);

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorFondo);
            }
        });

        return boton;
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
