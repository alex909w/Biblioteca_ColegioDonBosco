
package com.biblioteca.interfaces.menus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import com.biblioteca.controller.RecuperacionContraController;


public class RecuperarContra extends JFrame{
    
    private static RecuperarContra instancia;
    
    public RecuperarContra() {
        // Configuramos la ventana
        setTitle("Recuperar Contraseña");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Fondo y diseño del panel
        getContentPane().setBackground(new Color(248, 248, 248));
        
        JLabel emailLabel = new JLabel("Ingrese su correo electrónico:");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 5, 10);
        add(emailLabel, gbc);

        // Campo de texto para el correo
        JTextField emailRecoveryField = new JTextField(25);
        emailRecoveryField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 10, 10, 10);
        add(emailRecoveryField, gbc);

        // Botón para enviar solicitud de recuperación
        JButton sendButton = new JButton("Enviar");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(30, 144, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setPreferredSize(new Dimension(100, 30));
        sendButton.addActionListener(e -> {
            String email = emailRecoveryField.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un correo electrónico.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                RecuperacionContraController controller = new RecuperacionContraController();
                controller.enviarCredencialesController(email);
                
                dispose();
                instancia = null;
            }
        });
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        add(sendButton, gbc);

        // Centrar la ventana
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                instancia = null;
            }
            
        });
    }
    
    // Metodo para obtener una unica istancia de la clase
    public static void showFrame() {
        if(instancia == null) {
            instancia = new RecuperarContra();
            instancia.setVisible(true);
        } else {
            instancia.toFront();
        }
    }
}
