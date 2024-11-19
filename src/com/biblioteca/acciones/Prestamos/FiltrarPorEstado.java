package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FiltrarPorEstado extends JPanel {
    private JComboBox<String> estadoComboBox;
    private JTable resultadosTable;
    private DefaultTableModel tableModel;

    public FiltrarPorEstado() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Filtrar por Estado"));

        JPanel searchPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        searchPanel.add(new JLabel("Estado:"));
        estadoComboBox = new JComboBox<>(new String[]{"Bueno", "Dañado", "En Reparación"});
        searchPanel.add(estadoComboBox);

        JButton filtrarButton = new JButton("Filtrar");
        filtrarButton.addActionListener(e -> filtrarPorEstado());
        searchPanel.add(filtrarButton);

        add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Título", "Autor", "Estado", "Disponibles"}, 0);
        resultadosTable = new JTable(tableModel);
        add(new JScrollPane(resultadosTable), BorderLayout.CENTER);
    }

    private void filtrarPorEstado() {
        String estado = (String) estadoComboBox.getSelectedItem();

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT id_documento, titulo, autor, estado, cantidad_disponible FROM documentos WHERE estado = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, estado);

            ResultSet rs = stmt.executeQuery();
            tableModel.setRowCount(0); // Limpiar resultados previos
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id_documento"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("estado"),
                        rs.getInt("cantidad_disponible")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al filtrar ejemplares: " + ex.getMessage());
        }
    }
}
