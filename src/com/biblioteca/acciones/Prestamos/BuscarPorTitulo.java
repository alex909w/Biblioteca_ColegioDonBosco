package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BuscarPorTitulo extends JPanel {
    private JTextField tituloField;
    private JTable resultadosTable;
    private DefaultTableModel tableModel;

    public BuscarPorTitulo() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Buscar por Título"));

        JPanel searchPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        searchPanel.add(new JLabel("Título:"));
        tituloField = new JTextField();
        searchPanel.add(tituloField);

        JButton buscarButton = new JButton("Buscar");
        buscarButton.addActionListener(e -> buscarTitulo());
        searchPanel.add(buscarButton);

        add(searchPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Título", "Autor", "Estado", "Disponibles"}, 0);
        resultadosTable = new JTable(tableModel);
        add(new JScrollPane(resultadosTable), BorderLayout.CENTER);
    }

    private void buscarTitulo() {
        String titulo = tituloField.getText();
        if (titulo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un título.");
            return;
        }

        try (Connection conn = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT id_documento, titulo, autor, estado, cantidad_disponible FROM documentos WHERE titulo LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + titulo + "%");

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
