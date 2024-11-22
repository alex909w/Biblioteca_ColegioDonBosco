package com.biblioteca.interfaces.menus;

import com.biblioteca.basedatos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginBiblioteca extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginBiblioteca() {
        // Título de la ventana
        setTitle("Inicio de Sesión - Biblioteca");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Cargar y establecer el ícono de la ventana
        ImageIcon icon = new ImageIcon(getClass().getResource("/com/biblioteca/img/logoinicio.png")); // Cambia la ruta si es necesario
        setIconImage(icon.getImage()); // Establecer el ícono de la ventana

        // Layout más flexible para dar espacio y centrar
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Fondo de la ventana
        getContentPane().setBackground(new Color(248, 248, 248));

        // Establecer fuentes
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Establecer colores
        Color buttonColor = new Color(255, 140, 0); // Naranja brillante
        Color buttonHoverColor = new Color(255, 120, 0); // Naranja más oscuro
        Color textColor = new Color(44, 62, 80); // Color de texto oscuro

        // Añadir imagen en la parte superior
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/com/biblioteca/img/logoinicio.png"));
        JLabel logoLabel = new JLabel(logoIcon); // Colocar el ícono en un JLabel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;  // Hacer que ocupe toda la anchura
        gbc.insets = new Insets(10, 10, 10, 10);
        add(logoLabel, gbc);  // Agregar la imagen a la ventana

        // Etiqueta para Correo Electrónico
        JLabel emailLabel = new JLabel("Correo Electrónico:");
        emailLabel.setFont(labelFont);
        emailLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 10);
        add(emailLabel, gbc);

        // Campo de texto para el correo electrónico
        emailField = new JTextField(20);
        emailField.setFont(inputFont);
        emailField.setBackground(Color.WHITE);
        emailField.setForeground(textColor);
        emailField.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 10, 5, 10);
        add(emailField, gbc);

        // Etiqueta para Contraseña
        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 5, 10);
        add(passwordLabel, gbc);

        // Campo de texto para la contraseña
        passwordField = new JPasswordField(20);
        passwordField.setFont(inputFont);
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(textColor);
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 10, 5, 10);
        add(passwordField, gbc);

        // Botón de Iniciar Sesión
        loginButton = new JButton("Iniciar Sesión");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setBackground(buttonColor);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createLineBorder(buttonColor));
        loginButton.setPreferredSize(new Dimension(150, 40));

        // Efecto hover en el botón
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(buttonHoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(buttonColor);
            }
        });
        
        // Acción del botón
        loginButton.addActionListener(e -> iniciarSesion());
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(15, 10, 10, 10);
        add(loginButton, gbc);

        // Centrar la ventana en la pantalla
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void iniciarSesion() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.");
            return;
        }

        // Validar credenciales y redirigir según el rol
        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT rol FROM usuarios WHERE email = ? AND contraseña = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String rol = rs.getString("rol");
                JOptionPane.showMessageDialog(this, "Bienvenido, " + rol);

                // Redirigir según el rol
                dispose(); // Cerrar la ventana de login
                abrirMenuPorRol(rol, email);
            } else {
                JOptionPane.showMessageDialog(this, "Credenciales incorrectas. Inténtelo de nuevo.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos: " + ex.getMessage());
        }
    }

    // En LoginBiblioteca.java

private void abrirMenuPorRol(String rol, String email) {
    switch (rol.toLowerCase()) {
        case "administrador":
            new MenuAdministrador(email);
            break;
        case "profesor":
            new MenuProfesor(email);
            break;
        case "alumno":
            new MenuAlumno(email);
            break;
        default:
            JOptionPane.showMessageDialog(this, "Rol desconocido. No se puede acceder al sistema.");
    }
}




    public static void main(String[] args) {
        new LoginBiblioteca();
    }
}
