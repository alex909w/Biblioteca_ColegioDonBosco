package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ConsultarPrestamos extends JPanel {

    private JTable tablaPrestamos;

    public ConsultarPrestamos(String idUsuario) { // Recibe el ID del usuario autenticado
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(240, 240, 240));

        inicializarPanelCentral();
        cargarPrestamosVigentes(idUsuario); // Consulta los préstamos en curso
    }

    private void inicializarPanelCentral() {
        String[] columnas = {"ID", "ID Documento", "Fecha Préstamo", "Fecha Devolución", "Estado", "Días Mora", "Monto Mora"};
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0);
        tablaPrestamos = new JTable(modeloTabla);

        tablaPrestamos.setRowHeight(30);
        tablaPrestamos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaPrestamos.getTableHeader().setBackground(new Color(200, 200, 200));
        tablaPrestamos.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(tablaPrestamos);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void cargarPrestamosVigentes(String idUsuario) {
        try (Connection conexion = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT id, id_documento, fecha_prestamo, fecha_devolucion, estado, dias_mora, monto_mora " +
                         "FROM prestamos WHERE id_usuario = ? AND estado IN ('Pendiente', 'Mora')";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, idUsuario);

            ResultSet rs = stmt.executeQuery();
            cargarResultadosEnTabla(rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar los préstamos vigentes.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cargarResultadosEnTabla(ResultSet rs) throws SQLException {
        DefaultTableModel modeloTabla = (DefaultTableModel) tablaPrestamos.getModel();
        modeloTabla.setRowCount(0);

        while (rs.next()) {
            Object[] fila = {
                rs.getInt("id"),
                rs.getString("id_documento"),
                rs.getDate("fecha_prestamo"),
                rs.getDate("fecha_devolucion"),
                rs.getString("estado"),
                rs.getInt("dias_mora"),
                rs.getBigDecimal("monto_mora")
            };
            modeloTabla.addRow(fila);
        }
    }
}
