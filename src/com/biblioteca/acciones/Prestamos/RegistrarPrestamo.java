package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistrarPrestamo extends JPanel {
    private JTextField usuarioField, documentoField;

    public RegistrarPrestamo() {
        setLayout(new GridLayout(3, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Registrar Préstamo"));

        add(new JLabel("ID Usuario:"));
        usuarioField = new JTextField();
        add(usuarioField);

        add(new JLabel("ID Documento:"));
        documentoField = new JTextField();
        add(documentoField);

        JButton registrarButton = new JButton("Registrar");
        registrarButton.addActionListener(e -> registrarPrestamo());
        add(registrarButton);
    }

    private void registrarPrestamo() {
        String idUsuario = usuarioField.getText();
        String idDocumento = documentoField.getText();

        if (idUsuario.isEmpty() || idDocumento.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Validar mora pendiente
            String moraQuery = "SELECT mora FROM devoluciones WHERE id_usuario = ? AND mora > 0";
            PreparedStatement moraStmt = conn.prepareStatement(moraQuery);
            moraStmt.setString(1, idUsuario);
            ResultSet moraRs = moraStmt.executeQuery();
            if (moraRs.next()) {
                JOptionPane.showMessageDialog(this, "El usuario tiene mora pendiente. No puede registrar préstamos.");
                return;
            }

            // Validar disponibilidad
            String disponibilidadQuery = "SELECT cantidad_disponible FROM documentos WHERE id_documento = ?";
            PreparedStatement dispStmt = conn.prepareStatement(disponibilidadQuery);
            dispStmt.setString(1, idDocumento);
            ResultSet dispRs = dispStmt.executeQuery();
            if (dispRs.next() && dispRs.getInt("cantidad_disponible") > 0) {
                // Registrar préstamo
                String insertQuery = "INSERT INTO prestamos (id_usuario, id_documento) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, idUsuario);
                insertStmt.setString(2, idDocumento);
                insertStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Préstamo registrado exitosamente.");
            } else {
                JOptionPane.showMessageDialog(this, "El documento no tiene ejemplares disponibles.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar préstamo: " + ex.getMessage());
        }
    }
}
