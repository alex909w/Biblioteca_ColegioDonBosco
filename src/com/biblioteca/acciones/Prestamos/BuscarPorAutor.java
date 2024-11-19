package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BuscarPorAutor extends JPanel {
    private JTextField autorField;
    private JTable resultadosTable;
    private DefaultTableModel tableModel;

    public BuscarPorAutor() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Buscar por Autor"));

        JPanel searchPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        searchPanel.add(new JLabel("Autor:"));
        autorField = new JTextField();
        searchPanel.add(autorField);

        JButton buscarButton = new JButton("Buscar");
        buscarButton.addActionListener(e -> buscarPorAutor());
        searchPanel.add(buscarButton);

        add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Título", "Autor", "Estado", "Disponibles"}, 0);
        resultadosTable = new JTable(tableModel);
        add(new JScrollPane(resultadosTable), BorderLayout.CENTER);
    }

    private void buscarPorAutor() {
        String autor = autorField.getText();
        if (autor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un autor.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT id_documento, titulo, autor, estado, cantidad_disponible FROM documentos WHERE autor LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + autor + "%");

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
            JOptionPane.showMessageDialog(this, "Error al buscar ejemplares: " + ex.getMessage());
        }
    }
}
