package com.biblioteca.acciones.Usuarios;

import com.biblioteca.base_datos.ConexionBaseDatos;
import com.biblioteca.utilidades.DateLabelFormatter;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.UtilDateModel;

public class AgregarUsuario extends JPanel {
    private JTextField idField, nombreField, emailField, telefonoField, direccionField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;
    private JDatePickerImpl fechaNacimientoPicker; // Añadido

    public AgregarUsuario() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(248, 249, 250));

        // Título
        JLabel titulo = new JLabel("Agregar Usuario", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(new Color(50, 50, 50));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        add(titulo, BorderLayout.NORTH);

        // Panel central
        JPanel formularioPanel = new JPanel();
        formularioPanel.setLayout(new BoxLayout(formularioPanel, BoxLayout.Y_AXIS));
        formularioPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        formularioPanel.setBackground(new Color(250, 250, 250));

        formularioPanel.add(crearCampo("ID del Usuario:", idField = new JTextField(), true));
        formularioPanel.add(crearCampo("Nombre:", nombreField = new JTextField(), false));
        formularioPanel.add(crearCampo("Email:", emailField = new JTextField(), false));
        formularioPanel.add(crearCombo("Rol:", rolComboBox = new JComboBox<>(new String[]{"Administrador", "Profesor", "Alumno"})));
        formularioPanel.add(crearCampo("Contraseña:", passwordField = new JPasswordField(), false));
        formularioPanel.add(crearCampo("Teléfono:", telefonoField = new JTextField(), false));
        agregarFormatoTelefonoSimple(telefonoField); // Formato para el teléfono
        formularioPanel.add(crearCampo("Dirección:", direccionField = new JTextField(), false));
        formularioPanel.add(crearCampoFecha("Fecha de Nacimiento:")); // Modificado

        add(formularioPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));

        JButton agregarButton = crearBoton("Agregar", new Color(21, 156, 6));
        agregarButton.addActionListener(e -> agregarUsuario());
        buttonPanel.add(agregarButton);

        JButton limpiarButton = crearBoton("Limpiar", new Color(247, 185, 0));
        limpiarButton.setForeground(Color.BLACK);
        limpiarButton.addActionListener(e -> limpiarFormulario());
        buttonPanel.add(limpiarButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Generar ID inicial basado en el rol seleccionado
        actualizarID();
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

        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        comboBox.addActionListener(e -> actualizarID());

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
            } else {
                idField.setText(String.format("%s%05d", prefijo, 1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al generar el ID: " + ex.getMessage());
        }
    }

    private void agregarUsuario() {
        if (!validarFormulario()) return;

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Validar si el correo ya existe
            String validarEmailSQL = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
            PreparedStatement validarStmt = conn.prepareStatement(validarEmailSQL);
            validarStmt.setString(1, emailField.getText().trim());
            ResultSet rs = validarStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "El correo electrónico ya está registrado.");
                return;
            }

            // Insertar usuario
            String sql = "INSERT INTO usuarios (id, nombre, email, rol, contraseña, telefono, direccion, fecha_nacimiento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idField.getText());
            stmt.setString(2, nombreField.getText());
            stmt.setString(3, emailField.getText());
            stmt.setString(4, (String) rolComboBox.getSelectedItem());
            stmt.setString(5, new String(passwordField.getPassword()));
            stmt.setString(6, telefonoField.getText());
            stmt.setString(7, direccionField.getText());

            // Obtener la fecha seleccionada
            java.sql.Date fechaNacimiento = obtenerFechaSeleccionada(fechaNacimientoPicker);
            stmt.setDate(8, fechaNacimiento);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Usuario agregado exitosamente.");
            limpiarFormulario();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al agregar usuario: " + ex.getMessage());
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

        if (fechaNacimientoPicker.getModel().getValue() == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una fecha de nacimiento.");
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
        fechaNacimientoPicker.getModel().setValue(null); // Resetea el selector de fecha
        actualizarID();
    }

    private JPanel crearCampoFecha(String etiqueta) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(250, 250, 250));

        JLabel label = new JLabel(etiqueta);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setPreferredSize(new Dimension(200, 30));
        label.setForeground(new Color(50, 50, 50));

        UtilDateModel model = new UtilDateModel();
        // model.setSelected(true); // Puedes descomentar esta línea si deseas que se seleccione la fecha actual por defecto

        Properties properties = new Properties();
        properties.put("text.today", "Hoy");
        properties.put("text.month", "Mes");
        properties.put("text.year", "Año");

        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        fechaNacimientoPicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        panel.add(label, BorderLayout.WEST);
        panel.add(fechaNacimientoPicker, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        return panel;
    }

    private java.sql.Date obtenerFechaSeleccionada(JDatePickerImpl datePicker) {
        if (datePicker.getModel().getValue() != null) {
            java.util.Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
            return new java.sql.Date(selectedDate.getTime());
        }
        return null; // Si no se seleccionó ninguna fecha
    }
}
