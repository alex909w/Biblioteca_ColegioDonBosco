package com.biblioteca.Panel.Usuarios;

import com.biblioteca.controller.UsuarioController;
import com.biblioteca.modelos.Usuario;
import com.biblioteca.utilidades.DateLabelFormatter;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.UtilDateModel;

public class AgregarUsuario extends JPanel {
    private JTextField idField, nombreField, emailField, telefonoField, direccionField;
    private JPasswordField passwordField;
    private JComboBox<String> rolComboBox;
    private JDatePickerImpl fechaNacimientoPicker;

    private UsuarioController usuarioController;

    public AgregarUsuario() {
        usuarioController = new UsuarioController();

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(248, 249, 250));

        JLabel titulo = new JLabel("Agregar Usuario", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(new Color(50, 50, 50));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        add(titulo, BorderLayout.NORTH);

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
        agregarFormatoTelefonoSimple(telefonoField);
        formularioPanel.add(crearCampo("Dirección:", direccionField = new JTextField(), false));
        formularioPanel.add(crearCampoFecha("Fecha de Nacimiento:"));

        add(formularioPanel, BorderLayout.CENTER);

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

        try {
            int siguienteID = usuarioController.contarUsuariosPorRol(rol) + 1;
            idField.setText(String.format("%s%05d", prefijo, siguienteID));
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al generar el ID: " + ex.getMessage());
        }
    }

    private void agregarUsuario() {
        if (!validarFormulario()) return;

        try {
            if (usuarioController.validarCorreoExiste(emailField.getText().trim())) {
                JOptionPane.showMessageDialog(this, "El correo electrónico ya está registrado.");
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setId(idField.getText());
            usuario.setNombre(nombreField.getText());
            usuario.setEmail(emailField.getText());
            usuario.setRol((String) rolComboBox.getSelectedItem());
            usuario.setContraseña(new String(passwordField.getPassword()));
            usuario.setTelefono(telefonoField.getText());
            usuario.setDireccion(direccionField.getText());
            usuario.setFechaNacimiento(obtenerFechaSeleccionada(fechaNacimientoPicker));

            usuarioController.agregarUsuario(usuario);
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
                texto = texto.replaceAll("[^\\d]", "");

                if (texto.length() > 4) {
                    texto = texto.substring(0, 4) + "-" + texto.substring(4);
                }

                if (texto.length() > 9) {
                    texto = texto.substring(0, 9);
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
        fechaNacimientoPicker.getModel().setValue(null);
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
        return null;
    }
}
