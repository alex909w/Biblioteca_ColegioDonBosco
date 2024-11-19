package com.biblioteca.acciones.Devoluciones;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistrarDevolucion extends JPanel {
    private JTextField idPrestamoField;
    private JComboBox<String> estadoComboBox;

    public RegistrarDevolucion() {
        setLayout(new GridLayout(3, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Registrar Devolución"));

        add(new JLabel("ID Préstamo:"));
        idPrestamoField = new JTextField();
        add(idPrestamoField);

        add(new JLabel("Estado del Documento:"));
        estadoComboBox = new JComboBox<>(new String[]{"Bueno", "Dañado", "En Reparación"});
        add(estadoComboBox);

        JButton registrarButton = new JButton("Registrar");
        registrarButton.addActionListener(e -> registrarDevolucion());
        add(registrarButton);
    }

    private void registrarDevolucion() {
        String idPrestamo = idPrestamoField.getText();
        String estadoDocumento = (String) estadoComboBox.getSelectedItem();

        if (idPrestamo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese el ID del préstamo.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Verificar si el préstamo existe y no está devuelto
            String checkQuery = "SELECT id_documento FROM prestamos WHERE id_prestamo = ? AND estado != 'Devuelto'";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, idPrestamo);

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                String idDocumento = rs.getString("id_documento");

                // Registrar devolución
                String updatePrestamoQuery = "UPDATE prestamos SET estado = 'Devuelto' WHERE id_prestamo = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updatePrestamoQuery);
                updateStmt.setString(1, idPrestamo);
                updateStmt.executeUpdate();

                // Actualizar estado del documento si es necesario
                String updateDocumentoQuery = "UPDATE documentos SET estado = ? WHERE id_documento = ?";
                PreparedStatement updateDocStmt = conn.prepareStatement(updateDocumentoQuery);
                updateDocStmt.setString(1, estadoDocumento);
                updateDocStmt.setString(2, idDocumento);
                updateDocStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Devolución registrada exitosamente.");
            } else {
                JOptionPane.showMessageDialog(this, "Préstamo no encontrado o ya devuelto.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar devolución: " + ex.getMessage());
        }
    }
}
