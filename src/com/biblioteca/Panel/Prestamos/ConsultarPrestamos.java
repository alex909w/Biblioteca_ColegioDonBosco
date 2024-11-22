package com.biblioteca.Panel.Prestamos;

import com.biblioteca.controller.PrestamoController;
import com.biblioteca.modelos.Prestamo;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ConsultarPrestamos extends JPanel {

    private JTable tablaPrestamos;
    private PrestamoController prestamoController;

    public ConsultarPrestamos(String idUsuario) {
        prestamoController = new PrestamoController();
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(240, 240, 240));

        inicializarPanelCentral();
        cargarPrestamosVigentes(idUsuario);
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
        try {
            List<Prestamo> prestamos = prestamoController.obtenerPrestamosVigentes(idUsuario);
            DefaultTableModel modeloTabla = (DefaultTableModel) tablaPrestamos.getModel();
            modeloTabla.setRowCount(0);

            for (Prestamo prestamo : prestamos) {
                Object[] fila = {
                        prestamo.getId(),
                        prestamo.getIdDocumento(),
                        prestamo.getFechaPrestamo(),
                        prestamo.getFechaDevolucion(),
                        prestamo.getEstado(),
                        prestamo.getDiasMora(),
                        prestamo.getMontoMora()
                };
                modeloTabla.addRow(fila);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar los préstamos vigentes.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
