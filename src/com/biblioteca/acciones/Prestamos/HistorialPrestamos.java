package com.biblioteca.acciones.Prestamos;


import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HistorialPrestamos extends JPanel {
    private JTextField idUsuarioField;
    private JTable historialTable;
    private DefaultTableModel tableModel;

    public HistorialPrestamos() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Historial de Préstamos"));

        JPanel searchPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        searchPanel.add(new JLabel("ID Usuario:"));
        idUsuarioField = new JTextField();
        searchPanel.add(idUsuarioField);

        JButton buscarButton = new JButton("Buscar");
        buscarButton.addActionListener(e -> buscarHistorial());
        searchPanel.add(buscarButton);

        add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID Préstamo", "ID Documento", "Fecha Préstamo", "Estado"}, 0);
        historialTable = new JTable(tableModel);
        add(new JScrollPane(historialTable), BorderLayout.CENTER);
    }

    private void buscarHistorial() {
        String idUsuario = idUsuarioField.getText();

        if (idUsuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese el ID del usuario.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT id_prestamo, id_documento, fecha_prestamo, estado FROM prestamos WHERE id_usuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idUsuario);

            ResultSet rs = stmt.executeQuery();
            tableModel.setRowCount(0); // Limpiar resultados previos
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id_prestamo"),
                        rs.getString("id_documento"),
                        rs.getDate("fecha_prestamo"),
                        rs.getString("estado")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar historial de préstamos: " + ex.getMessage());
        }
    }
}
