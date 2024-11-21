package com.biblioteca.acciones.Usuarios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class EditarUsuario extends JPanel {
    private JComboBox<String> idUsuarioComboBox;
    private JTextField nombreField, emailField, telefonoField, direccionField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;
    private JButton actualizarButton, limpiarButton;

    public EditarUsuario() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(248, 249, 250));

        // Panel superior para ID del usuario
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        idPanel.setBackground(getBackground());
        idPanel.add(createLabel("ID Usuario:"));
        idUsuarioComboBox = new JComboBox<>();
        cargarUsuariosEnComboBox();
        idUsuarioComboBox.addActionListener(e -> cargarUsuarioSeleccionado());
        idPanel.add(idUsuarioComboBox);
        add(idPanel, BorderLayout.NORTH);

        // Panel central
        JPanel formularioPanel = new JPanel();
        formularioPanel.setLayout(new BoxLayout(formularioPanel, BoxLayout.Y_AXIS));
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        formularioPanel.setBackground(new Color(250, 250, 250));

        formularioPanel.add(crearCampo("Nombre:", nombreField = new JTextField(), false));
        formularioPanel.add(crearCampo("Email:", emailField = new JTextField(), false));
        formularioPanel.add(crearCampo("Rol:", rolComboBox = new JComboBox<>(new String[]{"Administrador", "Profesor", "Alumno"}), false));
        formularioPanel.add(crearCampo("Contraseña:", passwordField = new JPasswordField(), false));
        formularioPanel.add(crearCampo("Teléfono:", telefonoField = new JTextField(), false));
        agregarFormatoTelefonoSimple(telefonoField);
        formularioPanel.add(crearCampo("Dirección:", direccionField = new JTextField(), false));

        add(formularioPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));

        actualizarButton = crearBoton("Actualizar", new Color(21, 156, 6));
        actualizarButton.addActionListener(e -> actualizarUsuario());
        actualizarButton.setEnabled(false);
        buttonPanel.add(actualizarButton);

        limpiarButton = crearBoton("Limpiar", new Color(247, 185, 0));
        limpiarButton.addActionListener(e -> limpiarFormulario());
        limpiarButton.setForeground(Color.BLACK);
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

                habilitarCampos(true);
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
        if (!validarFormulario()) return;

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "UPDATE usuarios SET nombre = ?, email = ?, rol = ?, contraseña = ?, telefono = ?, direccion = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombreField.getText().trim());
            stmt.setString(2, emailField.getText().trim());
            stmt.setString(3, (String) rolComboBox.getSelectedItem());
            stmt.setString(4, new String(passwordField.getPassword()).trim());
            stmt.setString(5, telefonoField.getText().trim());
            stmt.setString(6, direccionField.getText().trim());
            stmt.setString(7, idUsuario);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Usuario actualizado exitosamente.");
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el usuario.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar usuario: " + ex.getMessage());
        }
    }
    
        private void agregarFormatoTelefonoSimple(JTextField campoTelefono) {
        campoTelefono.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String texto = campoTelefono.getText();
                texto = texto.replaceAll("[^\\d]", ""); // Elimina caracteres no numéricos

                if (texto.length() > 4) {
                    texto = texto.substring(0, 4) + "-" + texto.substring(4); // Añade el guion después del 4º dígito
                }

                if (texto.length() > 9) {
                    texto = texto.substring(0, 9); // Limita a 8 dígitos más el guion
                }

                campoTelefono.setText(texto);
            }
        });
    }

    private boolean validarFormulario() {
        String nombre = nombreField.getText().trim();
        String email = emailField.getText().trim();
        String contraseña = new String(passwordField.getPassword()).trim();
        String telefono = telefonoField.getText().trim();

        if (nombre.isEmpty() || email.isEmpty() || contraseña.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos obligatorios.");
            return false;
        }

        if (!Pattern.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$", nombre)) {
            JOptionPane.showMessageDialog(this, "El nombre solo debe contener letras, espacios y caracteres especiales como tildes.");
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

        if (!telefono.isEmpty() && !Pattern.matches("^\\d{4}-\\d{4}$", telefono)) {
            JOptionPane.showMessageDialog(this, "El número de teléfono debe estar en formato 1234-5678.");
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
        rolComboBox.setSelectedIndex(0);
        actualizarButton.setEnabled(false);
    }

    private void habilitarCampos(boolean habilitar) {
        nombreField.setEnabled(habilitar);
        emailField.setEnabled(habilitar);
        rolComboBox.setEnabled(habilitar);
        passwordField.setEnabled(habilitar);
        telefonoField.setEnabled(habilitar);
        direccionField.setEnabled(habilitar);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
}
