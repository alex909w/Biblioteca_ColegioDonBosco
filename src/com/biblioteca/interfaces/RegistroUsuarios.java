package com.biblioteca.interfaces;

import com.biblioteca.acciones.GestionUsuarios;
import com.biblioteca.utilidades.Validaciones;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class RegistroUsuarios extends JFrame {
    private JTextField txtNombre, txtEmail, txtTelefono, txtPreguntaSeguridad, txtRespuestaSeguridad, txtBuscar, txtID;
    private JPasswordField txtContraseña;
    private JTextArea txtDireccion;
    private JComboBox<String> cbRol;
    private JButton btnRegistrar, btnRestablecer, btnEliminar, btnBuscar, btnEditar;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    public RegistroUsuarios() {
        setTitle("Gestión de Usuarios");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel Superior: Formulario con 4 columnas
        JPanel formularioPanel = new JPanel(new GridBagLayout());
        formularioPanel.setBorder(BorderFactory.createTitledBorder("Registro de Usuarios"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espaciado entre campos

        // Columna 1 (Etiquetas)
        JLabel lblID = new JLabel("ID:");
        JLabel lblNombre = new JLabel("Nombre:");
        JLabel lblEmail = new JLabel("Correo:");
        JLabel lblTelefono = new JLabel("Teléfono:");
        
        // Columna 2 (Campos de Entrada)
        txtID = new JTextField(15);
        txtID.setEditable(false);
        txtID.setBackground(Color.LIGHT_GRAY);
        txtNombre = new JTextField(20);
        txtEmail = new JTextField(20);
        txtTelefono = new JTextField(20);

        // Columna 3 (Más Etiquetas)
        JLabel lblContraseña = new JLabel("Contraseña:");
        JLabel lblDireccion = new JLabel("Dirección:");
        JLabel lblPreguntaSeguridad = new JLabel("Pregunta de Seguridad:");
        JLabel lblRespuestaSeguridad = new JLabel("Respuesta de Seguridad:");
        
        // Columna 4 (Campos de Entrada)
        txtContraseña = new JPasswordField(20);
        txtDireccion = new JTextArea(3, 20);
        JScrollPane scrollDireccion = new JScrollPane(txtDireccion);
        txtPreguntaSeguridad = new JTextField(20);
        txtRespuestaSeguridad = new JTextField(20);

        // Columna 1 y 2
        gbc.gridx = 0;
        gbc.gridy = 0;
        formularioPanel.add(lblID, gbc);
        gbc.gridx = 1;
        formularioPanel.add(txtID, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formularioPanel.add(lblNombre, gbc);
        gbc.gridx = 1;
        formularioPanel.add(txtNombre, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formularioPanel.add(lblEmail, gbc);
        gbc.gridx = 1;
        formularioPanel.add(txtEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formularioPanel.add(lblTelefono, gbc);
        gbc.gridx = 1;
        formularioPanel.add(txtTelefono, gbc);

        // Columna 3 y 4
        gbc.gridx = 2;
        gbc.gridy = 0;
        formularioPanel.add(lblContraseña, gbc);
        gbc.gridx = 3;
        formularioPanel.add(txtContraseña, gbc);

        gbc.gridx = 2;
        gbc.gridy++;
        formularioPanel.add(lblDireccion, gbc);
        gbc.gridx = 3;
        formularioPanel.add(scrollDireccion, gbc);

        gbc.gridx = 2;
        gbc.gridy++;
        formularioPanel.add(lblPreguntaSeguridad, gbc);
        gbc.gridx = 3;
        formularioPanel.add(txtPreguntaSeguridad, gbc);

        gbc.gridx = 2;
        gbc.gridy++;
        formularioPanel.add(lblRespuestaSeguridad, gbc);
        gbc.gridx = 3;
        formularioPanel.add(txtRespuestaSeguridad, gbc);

        // Columna de Rol
        JLabel lblRol = new JLabel("Rol:");
        cbRol = new JComboBox<>(new String[]{"Administrador", "Profesor", "Alumno"});
        gbc.gridx = 2;
        gbc.gridy++;
        formularioPanel.add(lblRol, gbc);
        gbc.gridx = 3;
        formularioPanel.add(cbRol, gbc);

        // Panel Inferior: Barra de Búsqueda y Botones de Acciones
        JPanel accionesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JPanel barraBusquedaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        txtBuscar = new JTextField(20);
        btnBuscar = new JButton("Buscar");
        btnEditar = new JButton("Editar Usuario");
        btnEliminar = new JButton("Eliminar Usuario");

        barraBusquedaPanel.add(new JLabel("Buscar Usuario:"));
        barraBusquedaPanel.add(txtBuscar);
        barraBusquedaPanel.add(btnBuscar);
        barraBusquedaPanel.add(btnEditar);
        barraBusquedaPanel.add(btnEliminar);

        accionesPanel.add(barraBusquedaPanel);

        // Botones: Registrar y Restablecer Contraseña
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnRegistrar = new JButton("Registrar Usuario");
        btnRestablecer = new JButton("Restablecer Contraseña");
        botonesPanel.add(btnRegistrar);
        botonesPanel.add(btnRestablecer);

        // Panel de la tabla de usuarios
        JPanel tablaPanel = new JPanel(new BorderLayout());
        tablaPanel.setBorder(BorderFactory.createTitledBorder("Usuarios Registrados"));

        String[] columnas = {"ID", "Nombre", "Email", "Teléfono", "Dirección", "Rol", "Fecha de Registro"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        tablaPanel.add(scrollPane, BorderLayout.CENTER);

        tablaPanel.add(accionesPanel, BorderLayout.NORTH);  // Barra de búsqueda y acciones arriba de la tabla

        // Agregar los paneles al frame
        add(formularioPanel, BorderLayout.NORTH);  // Formulario en la parte superior
        add(tablaPanel, BorderLayout.CENTER);     // Tabla en el centro
        add(botonesPanel, BorderLayout.SOUTH);    // Botones abajo

        // Acciones de los Botones
        btnRegistrar.addActionListener(e -> registrarUsuario());
        btnEditar.addActionListener(e -> editarUsuario());
        btnRestablecer.addActionListener(e -> restablecerContraseña());
        btnBuscar.addActionListener(e -> buscarUsuario(txtBuscar.getText().trim()));
        btnEliminar.addActionListener(e -> eliminarUsuario());

        // Cargar Usuarios al Iniciar
        cargarUsuarios();
    }

    private void actualizarID() {
        String rol = (String) cbRol.getSelectedItem();
        String idGenerado = "ID" + rol.substring(0, 2).toUpperCase() + "001"; // Generación del ID basado en el rol
        txtID.setText(idGenerado);
    }

    private void registrarUsuario() {
        String nombre = txtNombre.getText().trim();
        String email = txtEmail.getText().trim();
        String contraseña = new String(txtContraseña.getPassword()).trim();
        String telefono = txtTelefono.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String preguntaSeguridad = txtPreguntaSeguridad.getText().trim();
        String respuestaSeguridad = txtRespuestaSeguridad.getText().trim();
        String rol = (String) cbRol.getSelectedItem();
        String idUsuario = txtID.getText().trim();

        // Validaciones
        if (Validaciones.estaVacio(nombre) || Validaciones.estaVacio(email) || Validaciones.estaVacio(contraseña) ||
            Validaciones.estaVacio(telefono) || Validaciones.estaVacio(direccion) ||
            Validaciones.estaVacio(preguntaSeguridad) || Validaciones.estaVacio(respuestaSeguridad)) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Validaciones.esEmailValido(email)) {
            JOptionPane.showMessageDialog(this, "Email no válido.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Validaciones.esContraseñaSegura(contraseña)) {
            JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 8 caracteres, incluir letras y números.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        GestionUsuarios gestion = new GestionUsuarios();

        if (gestion.registrarUsuario(nombre, email, contraseña, rol, telefono, direccion, preguntaSeguridad, respuestaSeguridad)) {
            JOptionPane.showMessageDialog(this, "Usuario registrado con éxito.\nID generado: " + idUsuario, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarUsuarios();
        } else {
            JOptionPane.showMessageDialog(this, "Error al registrar el usuario. Verifique los datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarUsuario() {
        // Método de edición de usuario (sin cambios)
    }

    private void restablecerContraseña() {
        // Método de restablecer contraseña (sin cambios)
    }

    private void eliminarUsuario() {
        // Método de eliminar usuario (sin cambios)
    }

    private void buscarUsuario(String criterio) {
        modeloTabla.setRowCount(0);
        GestionUsuarios gestion = new GestionUsuarios();
        try {
            List<String[]> usuarios = gestion.buscarUsuarios(criterio);
            for (String[] usuario : usuarios) {
                modeloTabla.addRow(usuario);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al buscar usuarios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarUsuarios() {
        modeloTabla.setRowCount(0); 
        GestionUsuarios gestion = new GestionUsuarios();

        try {
            List<String[]> usuarios = gestion.obtenerUsuarios();
            for (String[] usuario : usuarios) {
                modeloTabla.addRow(usuario);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtEmail.setText("");
        txtContraseña.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        txtPreguntaSeguridad.setText("");
        txtRespuestaSeguridad.setText("");
        cbRol.setSelectedIndex(0);
        txtID.setText(""); // Limpiar el ID
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistroUsuarios().setVisible(true));
    }
}
