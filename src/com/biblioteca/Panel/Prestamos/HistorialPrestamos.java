package com.biblioteca.Panel.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HistorialPrestamos extends JPanel {
    private JTable historialTable;
    private DefaultTableModel tableModel;

    public HistorialPrestamos(String correoUsuario) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                "Historial de Préstamos",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        // Etiqueta superior
        JLabel infoLabel = createStyledLabel("Historial de préstamos para el usuario: " + correoUsuario);
        add(infoLabel, BorderLayout.NORTH);

        // Configuración de la tabla
        tableModel = new DefaultTableModel(new String[]{"ID", "ID Documento", "Fecha Préstamo", "Estado"}, 0);
        historialTable = createStyledTable(tableModel);

        JScrollPane scrollPane = createStyledScrollPane(historialTable, "Resultados");
        add(scrollPane, BorderLayout.CENTER);

        // Cargar el historial automáticamente
        cargarHistorial(correoUsuario);
    }

    private void cargarHistorial(String correoUsuario) {
        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Obtener el ID del usuario por su correo
            String usuarioQuery = "SELECT id FROM usuarios WHERE email = ?";
            PreparedStatement usuarioStmt = conn.prepareStatement(usuarioQuery);
            usuarioStmt.setString(1, correoUsuario);

            ResultSet usuarioRs = usuarioStmt.executeQuery();
            if (!usuarioRs.next()) {
                JOptionPane.showMessageDialog(this, "No se encontró el usuario con el correo: " + correoUsuario,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String idUsuario = usuarioRs.getString("id");

            // Consultar el historial de préstamos
            String historialQuery = "SELECT id, id_documento, fecha_prestamo, estado FROM prestamos WHERE id_usuario = ?";
            PreparedStatement historialStmt = conn.prepareStatement(historialQuery);
            historialStmt.setString(1, idUsuario);

            ResultSet historialRs = historialStmt.executeQuery();
            tableModel.setRowCount(0); // Limpiar resultados previos
            while (historialRs.next()) {
                tableModel.addRow(new Object[]{
                        historialRs.getInt("id"),
                        historialRs.getString("id_documento"),
                        historialRs.getDate("fecha_prestamo"),
                        historialRs.getString("estado")
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No se encontraron préstamos para este usuario.",
                        "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar historial de préstamos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Métodos auxiliares para crear componentes estilizados

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); // Steel Blue
        return label;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        // Centrar el texto en columnas pequeñas
        centrarTextoColumnas(table);

        return table;
    }

    private void centrarTextoColumnas(JTable table) {
        // Crear un renderer para centrar el texto
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Aplicar el renderer a las columnas pequeñas
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            String columnName = columnModel.getColumn(i).getHeaderValue().toString().toLowerCase();
            if (columnName.contains("id") || columnName.contains("estado")) {
                columnModel.getColumn(i).setCellRenderer(centerRenderer);
                columnModel.getColumn(i).setPreferredWidth(80);
            } else {
                columnModel.getColumn(i).setPreferredWidth(150);
            }
        }

        // Centrar el texto en el encabezado de la tabla
        JTableHeader header = table.getTableHeader();
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private JScrollPane createStyledScrollPane(Component component, String title) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                Color.DARK_GRAY
        ));
        return scrollPane;
    }
}
