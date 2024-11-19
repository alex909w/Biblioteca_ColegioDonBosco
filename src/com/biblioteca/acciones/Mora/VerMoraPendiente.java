package com.biblioteca.acciones.Mora;

import com.biblioteca.base_datos.ConexionBaseDatos;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VerMoraPendiente extends JPanel {
    private JTextField idUsuarioField;
    private JLabel moraLabel;

    public VerMoraPendiente() {
        setLayout(new GridLayout(3, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Ver Mora Pendiente"));

        add(new JLabel("ID Usuario:"));
        idUsuarioField = new JTextField();
        add(idUsuarioField);

        JButton buscarButton = new JButton("Buscar");
        buscarButton.addActionListener(e -> buscarMora());
        add(buscarButton);

        moraLabel = new JLabel("Mora Pendiente: ");
        moraLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(moraLabel);
    }

    private void buscarMora() {
        String idUsuario = idUsuarioField.getText();

        if (idUsuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese el ID del usuario.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT SUM(mora) AS mora_total FROM devoluciones WHERE id_usuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idUsuario);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double moraTotal = rs.getDouble("mora_total");
                moraLabel.setText("Mora Pendiente: " + moraTotal + " USD");
            } else {
                moraLabel.setText("Mora Pendiente: 0.0 USD");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar mora: " + ex.getMessage());
        }
    }
}
