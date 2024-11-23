package com.biblioteca.Panel.Prestamos;

import com.biblioteca.basedatos.ConexionBaseDatos;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ConsultarMisPrestamos extends JPanel {

    private JTable tablaPrestamos;

    public ConsultarMisPrestamos(String emailUsuario) { // Recibe el correo del usuario autenticado
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(240, 240, 240));

        inicializarPanelCentral();

        // Cargar los préstamos del usuario
        cargarPrestamosVigentes(emailUsuario);
    }

    private void inicializarPanelCentral() {
        String[] columnas = {"ID", "ID Documento", "Fecha Préstamo", "Fecha Devolución", "Estado", "Días Mora", "Monto Mora"};
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0);
        tablaPrestamos = new JTable(modeloTabla);

        tablaPrestamos.setRowHeight(30);
        tablaPrestamos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaPrestamos.getTableHeader().setBackground(new Color(200, 200, 200));
        tablaPrestamos.setFillsViewportHeight(true);
        tablaPrestamos.getTableHeader().setReorderingAllowed(false);
        tablaPrestamos.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(tablaPrestamos);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void cargarPrestamosVigentes(String emailUsuario) {
        try (Connection conexion = ConexionBaseDatos.getConexion()) {
            // Obtener el ID del usuario a partir del email
            String idUsuario = getUserIdByEmail(conexion, emailUsuario);

            if (idUsuario == null) {
                JOptionPane.showMessageDialog(this, "No se encontró el usuario con el email proporcionado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Consultar los préstamos vigentes
            String sql = "SELECT id, id_documento, fecha_prestamo, fecha_devolucion, estado, dias_mora, monto_mora " +
                         "FROM prestamos WHERE id_usuario = ? AND estado IN ('Pendiente', 'Mora')";
            try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
                stmt.setString(1, idUsuario);

                ResultSet rs = stmt.executeQuery();
                cargarResultadosEnTabla(rs);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar los préstamos vigentes.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String getUserIdByEmail(Connection conn, String email) throws SQLException {
        String query = "SELECT id FROM usuarios WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id"); // Retorna el ID del usuario como String
            }
        }
        return null; // Retorna null si no se encuentra el usuario
    }

    private void cargarResultadosEnTabla(ResultSet rs) throws SQLException {
        DefaultTableModel modeloTabla = (DefaultTableModel) tablaPrestamos.getModel();
        modeloTabla.setRowCount(0); // Limpia la tabla antes de cargar nuevos datos

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
