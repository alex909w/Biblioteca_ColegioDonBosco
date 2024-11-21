package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class GestionPrestamos extends JPanel {

    private JTextField correoUsuarioField;
    private JTable tablaLibros;
    private JComboBox<Integer> diasPrestamoComboBox;
    private JButton buscarButton;
    private JButton registrarButton;

    public GestionPrestamos() {
        setLayout(new BorderLayout());

        // Panel Superior: Búsqueda de Usuario
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new FlowLayout());

        correoUsuarioField = new JTextField(20);
        buscarButton = new JButton("Buscar Libros");
        panelSuperior.add(new JLabel("Correo del Usuario:"));
        panelSuperior.add(correoUsuarioField);
        panelSuperior.add(buscarButton);

        add(panelSuperior, BorderLayout.NORTH);

        // Panel Central: Tabla de Libros Disponibles
        String[] columnas = {"ID", "Título", "Autor", "Cantidad Disponible"};
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0);
        tablaLibros = new JTable(modeloTabla);
        add(new JScrollPane(tablaLibros), BorderLayout.CENTER);

        // Panel Inferior: Registro de Préstamos
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new FlowLayout());

        diasPrestamoComboBox = new JComboBox<>(new Integer[]{7, 14, 21});
        registrarButton = new JButton("Registrar Préstamo");
        panelInferior.add(new JLabel("Días de Préstamo:"));
        panelInferior.add(diasPrestamoComboBox);
        panelInferior.add(registrarButton);

        add(panelInferior, BorderLayout.SOUTH);

        // Eventos
        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarLibros();
            }
        });

        registrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrarPrestamo();
            }
        });
    }

    private void buscarLibros() {
        String correo = correoUsuarioField.getText().trim();

        if (correo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el correo del usuario.");
            return;
        }

        try (Connection conexion = ConexionBaseDatos.getConexion()) {
            String sql = "SELECT id_libros, `Título del Libro`, `Autor(es)`, cantidad_disponible " +
                         "FROM libros WHERE cantidad_disponible > 0";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel modeloTabla = (DefaultTableModel) tablaLibros.getModel();
            modeloTabla.setRowCount(0);

            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                    rs.getString("id_libros"),
                    rs.getString("Título del Libro"),
                    rs.getString("Autor(es)"),
                    rs.getInt("cantidad_disponible")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al buscar libros: " + e.getMessage());
        }
    }

    private void registrarPrestamo() {
        int filaSeleccionada = tablaLibros.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un libro para registrar el préstamo.");
            return;
        }

        String correo = correoUsuarioField.getText().trim();
        String idLibro = (String) tablaLibros.getValueAt(filaSeleccionada, 0);
        int diasPrestamo = (int) diasPrestamoComboBox.getSelectedItem();

        if (correo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el correo del usuario.");
            return;
        }

        try (Connection conexion = ConexionBaseDatos.getConexion()) {
            // Validar usuario
            String usuarioQuery = "SELECT id FROM usuarios WHERE email = ?";
            PreparedStatement usuarioStmt = conexion.prepareStatement(usuarioQuery);
            usuarioStmt.setString(1, correo);
            ResultSet usuarioRs = usuarioStmt.executeQuery();

            if (!usuarioRs.next()) {
                JOptionPane.showMessageDialog(this, "Usuario no encontrado.");
                return;
            }
            String idUsuario = usuarioRs.getString("id");

            // Registrar el préstamo
            String prestamoQuery = "INSERT INTO prestamos (id_usuario, id_documento, dias_prestamo, fecha_prestamo, fecha_devolucion) " +
                                   "VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY))";
            PreparedStatement prestamoStmt = conexion.prepareStatement(prestamoQuery);
            prestamoStmt.setString(1, idUsuario);
            prestamoStmt.setString(2, idLibro);
            prestamoStmt.setInt(3, diasPrestamo);
            prestamoStmt.setInt(4, diasPrestamo);
            prestamoStmt.executeUpdate();

            // Actualizar disponibilidad del libro
            String actualizarLibroQuery = "UPDATE libros SET cantidad_disponible = cantidad_disponible - 1 WHERE id_libros = ?";
            PreparedStatement libroStmt = conexion.prepareStatement(actualizarLibroQuery);
            libroStmt.setString(1, idLibro);
            libroStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Préstamo registrado exitosamente.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al registrar préstamo: " + e.getMessage());
        }
    }
}
