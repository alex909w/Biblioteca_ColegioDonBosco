package com.biblioteca.acciones.Usuarios;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EliminarUsuario extends JPanel {
    private JTextField idUsuarioField;

    public EliminarUsuario() {
        setLayout(new GridLayout(3, 1, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Eliminar Usuario"));

        add(new JLabel("ID Usuario:"));
        idUsuarioField = new JTextField();
        add(idUsuarioField);

        JButton eliminarButton = new JButton("Eliminar");
        eliminarButton.addActionListener(e -> eliminarUsuario());
        add(eliminarButton);
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
