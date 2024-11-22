package com.biblioteca.Panel.Prestamos;

import com.biblioteca.base_datos.ConexionBaseDatos;
import com.biblioteca.controller.PrestamoController;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.dao.DocumentoDAO;
import com.biblioteca.modelos.Prestamo;
import com.biblioteca.modelos.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Clase para gestionar los préstamos.
 */
public class GestionPrestamos extends JPanel {

    private String emailUsuario; // Almacenar el correo del usuario autenticado
    private JTable tablaLibros;
    private JComboBox<String> tiposDocumentosComboBox;
    private JComboBox<Integer> diasPrestamoComboBox;
    private JButton buscarButton;
    private JButton registrarButton;
    private JLabel infoLabel; // Etiqueta para mostrar el correo del usuario
    private JLabel rolLabel;  // Etiqueta para mostrar el rol del usuario

    private PrestamoController prestamoController;
    private UsuarioDAO usuarioDAO;
    private DocumentoDAO documentoDAO;

    public GestionPrestamos(String emailUsuario) {
        this.emailUsuario = emailUsuario;
        prestamoController = new PrestamoController();
        usuarioDAO = new UsuarioDAO();
        documentoDAO = new DocumentoDAO();

        setLayout(new BorderLayout(15, 15));

        // Estilo general
        setBackground(new Color(240, 240, 240));

        // Contenedor para las etiquetas de información y el panel superior
        JPanel panelNorth = new JPanel();
        panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
        panelNorth.setBackground(new Color(240, 240, 240));

        // Obtener información del usuario autenticado
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        String rolUsuario = usuarioAutenticado.getRol();

        // Etiqueta superior: Información del Usuario
        infoLabel = createStyledLabel("Préstamos gestionados por: " + emailUsuario);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorth.add(infoLabel);

        // Etiqueta inferior: Rol del Usuario
        rolLabel = createStyledLabel("Rol del usuario: " + rolUsuario);
        rolLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorth.add(rolLabel);

        // Panel Superior: Búsqueda de Tipo de Documento
        JPanel panelSuperior = new JPanel(new GridLayout(1, 1, 10, 10)); // Grid para alineación
        panelSuperior.setBorder(new EmptyBorder(10, 10, 10, 10)); // Espacio alrededor
        panelSuperior.setBackground(new Color(255, 255, 255)); // Fondo blanco

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila1.setBackground(new Color(255, 255, 255));
        buscarButton = new JButton("Buscar Documentos");
        tiposDocumentosComboBox = new JComboBox<>();

        fila1.add(new JLabel("Tipo de Documento:"));
        fila1.add(tiposDocumentosComboBox);
        fila1.add(buscarButton);

        panelSuperior.add(fila1);
        panelNorth.add(panelSuperior);

        add(panelNorth, BorderLayout.NORTH);

        // Panel Central: Tabla de Documentos Disponibles
        tablaLibros = new JTable();
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

        diasPrestamoComboBox = new JComboBox<>();
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
        cargarDiasPrestamoPorRol(usuarioAutenticado.getRol().toLowerCase());

        // Eventos
        buscarButton.addActionListener(e -> buscarLibros());
        registrarButton.addActionListener(e -> registrarPrestamo(usuarioAutenticado));
    }

    private Usuario obtenerUsuarioAutenticado() {
        try {
            return usuarioDAO.obtenerUsuarioPorEmail(emailUsuario);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al obtener usuario autenticado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void cargarDiasPrestamoPorRol(String rolUsuario) {
        try {
            int limiteDias = prestamoController.obtenerLimitePrestamosPorRol(rolUsuario);
            diasPrestamoComboBox.removeAllItems();
            for (int i = 1; i <= limiteDias; i++) {
                diasPrestamoComboBox.addItem(i);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar días de préstamo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTablasDesdeTiposDocumentos() {
        try {
            List<String> tiposDocumentos = documentoDAO.obtenerNombresTablas();
            for (String tipo : tiposDocumentos) {
                tiposDocumentosComboBox.addItem(tipo);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar tipos de documentos: " + e.getMessage());
        }
    }

    private void buscarLibros() {
    String tipoDocumento = (String) tiposDocumentosComboBox.getSelectedItem();

    if (tipoDocumento == null) {
        JOptionPane.showMessageDialog(this, "Seleccione una tabla de tipo de documento.");
        return;
    }

    try {
        // Obtén el nombre dinámico del ID
        String nombreColumnaID = obtenerNombreColumnaID(tipoDocumento);

        List<Map<String, Object>> documentos = documentoDAO.obtenerTodosLosDocumentos(tipoDocumento);

        // Obtener el modelo de la tabla y limpiarlo antes de insertar nuevos datos
        DefaultTableModel modeloTabla = new DefaultTableModel();
        tablaLibros.setModel(modeloTabla);

        if (!documentos.isEmpty()) {
            Map<String, Object> firstRow = documentos.get(0);
            for (String columnName : firstRow.keySet()) {
                modeloTabla.addColumn(columnName);
            }

            for (Map<String, Object> doc : documentos) {
                Object[] rowData = doc.values().toArray();
                modeloTabla.addRow(rowData);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No hay documentos disponibles.");
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar datos de la tabla seleccionada: " + e.getMessage());
    }
}


   private void registrarPrestamo(Usuario usuarioAutenticado) {
    int filaSeleccionada = tablaLibros.getSelectedRow();
    if (filaSeleccionada == -1) {
        JOptionPane.showMessageDialog(this, "Seleccione un documento para registrar el préstamo.");
        return;
    }

    String tipoDocumento = (String) tiposDocumentosComboBox.getSelectedItem();
    if (tipoDocumento == null || tipoDocumento.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Seleccione un tipo de documento válido.");
        return;
    }

    try {
        // Obtén el nombre dinámico del ID
        String nombreColumnaID = obtenerNombreColumnaID(tipoDocumento);

        String idDocumento = tablaLibros.getValueAt(filaSeleccionada, getColumnIndex(nombreColumnaID)).toString();
        String cantidadDisponibleStr = tablaLibros.getValueAt(filaSeleccionada, getColumnIndex("cantidad_disponible")).toString();
        int cantidadDisponible = Integer.parseInt(cantidadDisponibleStr);

        if (cantidadDisponible <= 0) {
            JOptionPane.showMessageDialog(this, "No hay suficientes copias disponibles para realizar el préstamo.");
            return;
        }

        int diasPrestamo = (int) diasPrestamoComboBox.getSelectedItem();

        // Validar límite de préstamos por rol
        int limitePrestamos = prestamoController.obtenerLimitePrestamosPorRol(usuarioAutenticado.getRol().toLowerCase());
        boolean puedePrestar = prestamoController.validarLimitePrestamos(usuarioAutenticado.getId(), limitePrestamos);
        if (!puedePrestar) {
            JOptionPane.showMessageDialog(this, "El usuario ha alcanzado el límite de préstamos permitidos para su rol.");
            return;
        }

        // Obtener mora diaria
        double moraDiaria = prestamoController.obtenerMoraDiariaPorRol(usuarioAutenticado.getRol().toLowerCase());

        // Registrar el préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setIdUsuario(usuarioAutenticado.getId());
        prestamo.setIdDocumento(idDocumento);
        prestamo.setMoraDiaria(moraDiaria);
        prestamo.setDiasMora(diasPrestamo); // Usando diasMora para almacenar los días de préstamo

        prestamoController.registrarPrestamo(prestamo);

        // Actualizar cantidad disponible
        prestamoController.actualizarDisponibilidadDocumento(tipoDocumento, idDocumento, -1);

        JOptionPane.showMessageDialog(this, "Préstamo registrado exitosamente.");

        // Recargar la tabla para reflejar los cambios
        buscarLibros();

    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "La cantidad disponible no es válida. Verifique los datos del documento.");
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al registrar préstamo: " + e.getMessage());
    }
}


    private int getColumnIndex(String columnName) {
        for (int i = 0; i < tablaLibros.getColumnCount(); i++) {
            if (tablaLibros.getColumnName(i).equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    // Método auxiliar para crear una etiqueta estilizada.
  
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(70, 130, 180)); // Steel Blue
        return label;
    }

    private String obtenerNombreColumnaID(String tabla) throws SQLException {
    String sql = "DESCRIBE " + tabla;
    try (Connection conexion = ConexionBaseDatos.getConexion();
         PreparedStatement stmt = conexion.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            String columna = rs.getString("Field");
            if (columna.toLowerCase().startsWith("id_")) {
                return columna; // Retorna la columna que empieza con "id_"
            }
        }
    }
    throw new SQLException("No se encontró ninguna columna que comience con 'id_' en la tabla: " + tabla);
}
    
    
}
