package com.biblioteca.acciones.Usuarios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.border.TitledBorder;

public class EliminarUsuario extends JPanel {
    private JTextField idUsuarioField;
    private JButton eliminarButton;

    public EliminarUsuario() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)), 
                "Eliminar Usuario", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Arial", Font.BOLD, 16), new Color(50, 50, 50)));

        // Panel central con ID del usuario
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        idPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel idLabel = new JLabel("ID Usuario:");
        idLabel.setFont(new Font("Arial", Font.BOLD, 14));
        idPanel.add(idLabel);

        idUsuarioField = new JTextField(15);
        idUsuarioField.setFont(new Font("Arial", Font.PLAIN, 14));
        idUsuarioField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        idPanel.add(idUsuarioField);

        add(idPanel, BorderLayout.CENTER);

        // Botón para eliminar
        eliminarButton = new JButton("Eliminar");
        eliminarButton.setFont(new Font("Arial", Font.BOLD, 14));
        eliminarButton.setBackground(new Color(220, 53, 69)); // Rojo
        eliminarButton.setForeground(Color.WHITE);
        eliminarButton.setFocusPainted(false);
        eliminarButton.setPreferredSize(new Dimension(150, 40));
        eliminarButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        eliminarButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                eliminarButton.setBackground(new Color(176, 0, 32)); // Rojo oscuro al pasar
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                eliminarButton.setBackground(new Color(220, 53, 69)); // Color original
            }
        });
        eliminarButton.addActionListener(e -> eliminarUsuario());

        // Panel inferior con botón
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(eliminarButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void eliminarUsuario() {
        String idUsuario = idUsuarioField.getText();

        if (idUsuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese el ID del usuario.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar este usuario?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = ConexionBaseDatos.getConexion()) {
                // Verificar si el usuario existe
                String checkQuery = "SELECT * FROM usuarios WHERE id_usuario = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, idUsuario);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Eliminar usuario
                    String deleteQuery = "DELETE FROM usuarios WHERE id_usuario = ?";
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                    deleteStmt.setString(1, idUsuario);
                    deleteStmt.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Usuario eliminado exitosamente.");
                } else {
                    JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar usuario: " + ex.getMessage());
            }
        }
    }
}
