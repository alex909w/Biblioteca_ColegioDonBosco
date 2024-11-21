package com.biblioteca.acciones.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class GestionPrestamos extends JPanel {

    private JTextField correoUsuarioField;
    private JTable tablaLibros;
    private JComboBox<String> tiposDocumentosComboBox;
    private JComboBox<Integer> diasPrestamoComboBox;
    private JButton buscarButton;
    private JButton registrarButton;

    public GestionPrestamos() {
        setLayout(new BorderLayout(15, 15)); // Margen entre componentes

        // Estilo general
        setBackground(new Color(240, 240, 240)); // Fondo claro

        // Panel Superior: Búsqueda de Usuario y Tipo de Documento
        JPanel panelSuperior = new JPanel(new GridLayout(2, 1, 10, 10)); // Grid para alineación
        panelSuperior.setBorder(new EmptyBorder(10, 10, 10, 10)); // Espacio alrededor
        panelSuperior.setBackground(new Color(255, 255, 255)); // Fondo blanco

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila1.setBackground(new Color(255, 255, 255));
        correoUsuarioField = new JTextField(20);
        buscarButton = new JButton("Buscar Documentos");
        tiposDocumentosComboBox = new JComboBox<>();

        fila1.add(new JLabel("Correo del Usuario:"));
        fila1.add(correoUsuarioField);
        fila1.add(new JLabel("Tipo de Documento:"));
        fila1.add(tiposDocumentosComboBox);
        fila1.add(buscarButton);

        panelSuperior.add(fila1);
        add(panelSuperior, BorderLayout.NORTH);

        // Panel Central: Tabla de Documentos Disponibles
        String[] columnas = {"ID", "Título", "Autor", "Cantidad Disponible"};
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0);
        tablaLibros = new JTable(modeloTabla);

        tablaLibros.setFillsViewportHeight(true); // Rellenar todo el área visible
        tablaLibros.setRowHeight(30); // Altura de filas
        tablaLibros.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaLibros.getTableHeader().setBackground(new Color(200, 200, 200));
        tablaLibros.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(tablaLibros);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        add(scrollPane, BorderLayout.CENTER);

        // Panel Inferior: Registro de Préstamos
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelInferior.setBackground(new Color(255, 255, 255));

        diasPrestamoComboBox = new JComboBox<>(new Integer[]{7, 14, 21});
        registrarButton = new JButton("Registrar Préstamo");

        registrarButton.setBackground(new Color(0, 123, 255));
        registrarButton.setForeground(Color.WHITE);
        registrarButton.setFocusPainted(false);
        registrarButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        panelInferior.add(new JLabel("Días de Préstamo:"));
        panelInferior.add(diasPrestamoComboBox);
        panelInferior.add(registrarButton);

        add(panelInferior, BorderLayout.SOUTH);

        // Cargar nombres de tablas desde tipos_documentos
        cargarTablasDesdeTiposDocumentos();

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

    private void cargarTablasDesdeTiposDocumentos() {
    try (Connection conexion = ConexionBaseDatos.getConexion()) {
        // Obtener los nombres de las tablas registradas en la tabla `tipos_documentos`
        String sql = "SELECT nombre FROM tipos_documentos";
        PreparedStatement stmt = conexion.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            tiposDocumentosComboBox.addItem(rs.getString("nombre"));
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar tipos de documentos: " + e.getMessage());
    }
}


   private void buscarLibros() {
    String correo = correoUsuarioField.getText().trim();

    // Validar que el correo no esté vacío
    if (correo.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Ingrese el correo del usuario.");
        return;
    }

    String tablaSeleccionada = (String) tiposDocumentosComboBox.getSelectedItem();

    // Validar que se haya seleccionado una tabla
    if (tablaSeleccionada == null) {
        JOptionPane.showMessageDialog(this, "Seleccione una tabla de tipo de documento.");
        return;
    }

    try (Connection conexion = ConexionBaseDatos.getConexion()) {
        // Verificar que el correo exista en la tabla de usuarios
        String usuarioQuery = "SELECT id FROM usuarios WHERE email = ?";
        PreparedStatement usuarioStmt = conexion.prepareStatement(usuarioQuery);
        usuarioStmt.setString(1, correo);
        ResultSet usuarioRs = usuarioStmt.executeQuery();

        if (!usuarioRs.next()) {
            JOptionPane.showMessageDialog(this, "El correo no está registrado.");
            return;
        }

        // Cargar todos los datos de la tabla seleccionada
        String sql = "SELECT * FROM " + tablaSeleccionada;
        PreparedStatement stmt = conexion.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        // Obtener el modelo de la tabla y limpiarlo antes de insertar nuevos datos
        DefaultTableModel modeloTabla = (DefaultTableModel) tablaLibros.getModel();
        modeloTabla.setRowCount(0);

        // Leer los metadatos para manejar dinámicamente las columnas de la tabla
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Establecer los nombres de las columnas en la tabla
        String[] columnas = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnas[i - 1] = metaData.getColumnName(i);
        }
        modeloTabla.setColumnIdentifiers(columnas);

        // Agregar las filas desde el resultado
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            modeloTabla.addRow(row);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar datos de la tabla seleccionada: " + e.getMessage());
    }
}



   private void registrarPrestamo() {
    int filaSeleccionada = tablaLibros.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Seleccione un documento para registrar el préstamo.");
        return;
    }

    String correo = correoUsuarioField.getText().trim();
    if (correo.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Ingrese el correo del usuario.");
        return;
    }

    String tipoDocumento = (String) tiposDocumentosComboBox.getSelectedItem();
    if (tipoDocumento == null) {
        JOptionPane.showMessageDialog(this, "Seleccione un tipo de documento.");
        return;
    }

    // Validar el valor de la cantidad disponible
    Object cantidadObj = tablaLibros.getValueAt(filaSeleccionada, 3); // Índice de "Cantidad Disponible"
    if (cantidadObj == null || cantidadObj.toString().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "La cantidad disponible no puede estar vacía o ser nula.");
        return;
    }

    int cantidadDisponible;
    try {
        cantidadDisponible = Integer.parseInt(cantidadObj.toString().trim());
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "El valor de la cantidad disponible no es un número válido.");
        return;
    }

    if (cantidadDisponible <= 0) {
        JOptionPane.showMessageDialog(this, "No hay suficientes copias disponibles para realizar el préstamo.");
        return;
    }

    int diasPrestamo = (int) diasPrestamoComboBox.getSelectedItem();
    String idDocumento = (String) tablaLibros.getValueAt(filaSeleccionada, 0); // ID del documento

    try (Connection conexion = ConexionBaseDatos.getConexion()) {
        // Validar el correo del usuario
        String usuarioQuery = "SELECT id FROM usuarios WHERE email = ?";
        PreparedStatement usuarioStmt = conexion.prepareStatement(usuarioQuery);
        usuarioStmt.setString(1, correo);
        ResultSet usuarioRs = usuarioStmt.executeQuery();

        if (!usuarioRs.next()) {
            JOptionPane.showMessageDialog(this, "El correo no está registrado.");
            return;
        }

        String idUsuario = usuarioRs.getString("id");

        // Insertar el préstamo en la tabla `prestamos`
        String prestamoQuery = "INSERT INTO prestamos (id_usuario, id_documento, dias_prestamo, fecha_prestamo, fecha_devolucion, estado) " +
                               "VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY), 'Pendiente')";
        PreparedStatement prestamoStmt = conexion.prepareStatement(prestamoQuery);
        prestamoStmt.setString(1, idUsuario);
        prestamoStmt.setString(2, idDocumento);
        prestamoStmt.setInt(3, diasPrestamo);
        prestamoStmt.setInt(4, diasPrestamo);
        prestamoStmt.executeUpdate();

        // Actualizar la cantidad disponible
        String actualizarDisponibilidadQuery = "UPDATE " + tipoDocumento + " SET cantidad_disponible = cantidad_disponible - 1 WHERE id_libros = ?";
        PreparedStatement actualizarStmt = conexion.prepareStatement(actualizarDisponibilidadQuery);
        actualizarStmt.setString(1, idDocumento);
        actualizarStmt.executeUpdate();

        JOptionPane.showMessageDialog(this, "Préstamo registrado exitosamente.");

        // Recargar la tabla para reflejar los cambios
        buscarLibros();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al registrar préstamo: " + e.getMessage());
    }
}



}
