package com.biblioteca.interfaces.menus;

import com.biblioteca.Panel.Inventario.EliminarArticulo;
import com.biblioteca.Panel.Inventario.EditarArticulo;
import com.biblioteca.Panel.Inventario.RegistrarArticulo;
import com.biblioteca.Formularios.CrearFormulario;
import com.biblioteca.Formularios.EditarFormulario;
import com.biblioteca.Formularios.EliminarFormulario;
import com.biblioteca.Panel.Config.ConfiguracionRoles;
import com.biblioteca.Panel.Prestamos.BuscarPorTituloAutorEstado;
import com.biblioteca.Panel.Prestamos.GestionPrestamos;
import com.biblioteca.Panel.Prestamos.HistorialAdministrador;
import com.biblioteca.Panel.Prestamos.HistorialPrestamos;
import com.biblioteca.Panel.Prestamos.RegistrarDevolucion;
import com.biblioteca.Panel.Usuarios.AgregarUsuario;
import com.biblioteca.Panel.Usuarios.EditarUsuario;
import com.biblioteca.Panel.Usuarios.EliminarUsuario;
import com.biblioteca.Panel.Usuarios.VerUsuarios; // Importamos la clase VerUsuarios

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import org.jdatepicker.impl.JDatePickerImpl;

public class MenuAdministrador extends JFrame {
    private String emailUsuario;
    private boolean esAdministrador;
    private JPanel panelCentral;
    private JPanel panelIzquierdo;
    private Map<String, JPanel> submenusVisibles;

    private final Color COLOR_PRIMARIO = new Color(51, 102, 153);
    private final Color FONDO_LATERAL = new Color(248, 249, 250);
    private final Color COLOR_HOVER = new Color(233, 236, 239);
    private final Font FUENTE_PRINCIPAL = new Font("Segoe UI", Font.PLAIN, 14);

    public MenuAdministrador(String email) {
         this.emailUsuario = email;
        setTitle("Menú Principal: Administrador");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(crearPanelSuperior(), BorderLayout.NORTH);
        
        // Cargar y establecer el ícono de la ventana
        ImageIcon icon = new ImageIcon(getClass().getResource("/com/biblioteca/img/logoinicio.png")); // Cambia la ruta si es necesario
        setIconImage(icon.getImage()); // Establecer el ícono de la ventana

        // Panel izquierdo (Menú Vertical)
        panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setBackground(FONDO_LATERAL);
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPanelIzquierdo = new JScrollPane(panelIzquierdo);
        scrollPanelIzquierdo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPanelIzquierdo, BorderLayout.WEST);

        // Panel central
        panelCentral = new JPanel();
        panelCentral.setLayout(new BorderLayout());
        panelCentral.setBackground(FONDO_LATERAL);
        panelCentral.setBorder(BorderFactory.createTitledBorder("Contenido"));
        add(panelCentral, BorderLayout.CENTER);

        // Mapa para controlar submenús visibles
        submenusVisibles = new HashMap<>();

        // Configurar el menú
        configurarMenu();

        // Crear botón "Salir" fijo en la parte inferior
        agregarBotonSalir();

        setLocationRelativeTo(null); // Centrar la ventana
        setVisible(true);
    }

    private JPanel crearPanelSuperior() {
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(COLOR_PRIMARIO);
        panelSuperior.setPreferredSize(new Dimension(0, 60));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitulo = new JLabel("Biblioteca Colegio Amigos De Don Bosco");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        panelSuperior.add(lblTitulo, BorderLayout.WEST);

        return panelSuperior;
    }

    private void configurarMenu() {
        agregarBotonMenu("Gestión de Usuarios", new String[]{
                "Agregar Usuario", "Editar Usuario", "Eliminar Usuario"
        });
        
        agregarBotonMenu("Consultar Ejemplares", new String[]{
                "Registrar Préstamos", "Historial de Préstamos", "Registrar Devolución"
        });

        agregarBotonMenu("Gestión de Inventario", new String[]{
                "Registrar Artículos", "Editar Artículos", "Eliminar Artículos"
        });
        
        agregarBotonMenu("Gestión de Formularios", new String[]{
                "Crear Formulario", "Editar Formulario", "Eliminar Formulario"
        });

        agregarBotonMenu("Más", new String[]{
                "Configuraciones"
        });
    }

    private void agregarBotonMenu(String titulo, String[] submenus) {
        JButton botonMenu = new JButton(titulo);
        botonMenu.setHorizontalAlignment(SwingConstants.LEFT);
        botonMenu.setPreferredSize(new Dimension(250, 40));
        botonMenu.setMaximumSize(new Dimension(250, 40));
        botonMenu.setBackground(FONDO_LATERAL);
        botonMenu.setFont(FUENTE_PRINCIPAL);
        botonMenu.setForeground(new Color(33, 37, 41));
        botonMenu.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        botonMenu.setFocusPainted(false);

        // Efecto de hover
        botonMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botonMenu.setBackground(COLOR_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botonMenu.setBackground(FONDO_LATERAL);
            }
        });

        JPanel subMenuPanel = new JPanel();
        subMenuPanel.setLayout(new BoxLayout(subMenuPanel, BoxLayout.Y_AXIS));
        subMenuPanel.setVisible(false);

        for (String submenu : submenus) {
            JButton botonSubmenu = new JButton(submenu);
            botonSubmenu.setFont(FUENTE_PRINCIPAL);
            botonSubmenu.setAlignmentX(Component.LEFT_ALIGNMENT);
            botonSubmenu.setBackground(FONDO_LATERAL);
            botonSubmenu.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 10));
            botonSubmenu.setPreferredSize(new Dimension(250, 40));
            botonSubmenu.setMaximumSize(new Dimension(250, 40));
            botonSubmenu.addActionListener(e -> ejecutarFuncionAdministrador(submenu));
            subMenuPanel.add(botonSubmenu);

            // Efecto de hover
            botonSubmenu.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    botonSubmenu.setBackground(COLOR_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    botonSubmenu.setBackground(FONDO_LATERAL);
                }
            });
        }

        botonMenu.addActionListener(e -> {
            boolean esVisible = subMenuPanel.isVisible();
            ocultarTodosLosSubmenus();
            subMenuPanel.setVisible(!esVisible);
            panelIzquierdo.revalidate();
            panelIzquierdo.repaint();

            // Si el menú es "Gestión de Usuarios", cargar la tabla de usuarios
            if (titulo.equals("Gestión de Usuarios")) {
                cargarTablaUsuarios();
            }
            if (titulo.equals("Consultar Ejemplares")) {
                BuscarPorTituloAutorEstado();
            }
        });

        panelIzquierdo.add(botonMenu);
        panelIzquierdo.add(subMenuPanel);

        submenusVisibles.put(titulo, subMenuPanel);

        // Espaciado entre menús
        panelIzquierdo.add(Box.createVerticalStrut(10));
    }

    
    private void ocultarTodosLosSubmenus() {
        for (JPanel submenu : submenusVisibles.values()) {
            submenu.setVisible(false);
        }
    }

    private void agregarBotonSalir() {
        JButton botonSalir = new JButton("Salir");
        botonSalir.setFont(FUENTE_PRINCIPAL);
        botonSalir.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonSalir.setBackground(new Color(193, 42, 46));
        botonSalir.setForeground(Color.WHITE);
        botonSalir.setPreferredSize(new Dimension(100, 40));
        botonSalir.setMaximumSize(new Dimension(100, 40));
        botonSalir.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Efecto de hover
        botonSalir.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                botonSalir.setBackground(new Color(191, 1, 3));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botonSalir.setBackground(new Color(193, 42, 46));
            }
        });

        botonSalir.addActionListener(e -> {
            dispose();
            new LoginBiblioteca();
        });

        panelIzquierdo.add(Box.createVerticalGlue());
        panelIzquierdo.add(botonSalir);
    }

    private void ejecutarFuncionAdministrador(String submenu) {
        panelCentral.removeAll();

        JPanel nuevoPanel = null;

        switch (submenu) {
            // Gestión de Usuarios
            case "Agregar Usuario":
                nuevoPanel = new AgregarUsuario();
                break;
            case "Editar Usuario":
                nuevoPanel = new EditarUsuario();
                break;
            case "Eliminar Usuario":
                nuevoPanel = new EliminarUsuario();
                break;

            // Gestión de Formularios
            case "Crear Formulario":
                nuevoPanel = new CrearFormulario();
                break;
            case "Editar Formulario":
                nuevoPanel = new EditarFormulario();
                break;
            case "Eliminar Formulario":
                nuevoPanel = new EliminarFormulario();
                break;

            // Gestión de Inventario
            case "Registrar Artículos":
                nuevoPanel = new RegistrarArticulo();
                break;
            case "Editar Artículos":
                nuevoPanel = new EditarArticulo();
                break;
            case "Eliminar Artículos":
                nuevoPanel = new EliminarArticulo();
                break;

            // Gestión de Ejemplares
            case "Registrar Préstamos":
                nuevoPanel = new GestionPrestamos(emailUsuario);
                break;
            case "Historial de Préstamos":
                String correoUsuario = obtenerCorreoUsuarioAutenticado();
                nuevoPanel = new HistorialAdministrador();
                break;

            // Gestión de Devoluciones
            case "Registrar Devolución":
                nuevoPanel = new RegistrarDevolucion(emailUsuario); // Pasar el email
                break;

            // Configuraciones
            case "Configuraciones":
                nuevoPanel = new ConfiguracionRoles();
                break;

            default:
                nuevoPanel = new JPanel();
                nuevoPanel.add(new JLabel("Función no implementada: " + submenu));
        }

        if (nuevoPanel != null) {
            panelCentral.add(nuevoPanel, BorderLayout.CENTER);
        }

        panelCentral.revalidate();
        panelCentral.repaint();
    }

    private void cargarTablaUsuarios() {
        panelCentral.removeAll();

        VerUsuarios panelUsuarios = new VerUsuarios(); // Creamos una instancia de VerUsuarios
        panelCentral.add(panelUsuarios, BorderLayout.CENTER);

        panelCentral.revalidate();
        panelCentral.repaint();
    }
    
        private void BuscarPorTituloAutorEstado() {
        panelCentral.removeAll();

        BuscarPorTituloAutorEstado panelUsuarios = new BuscarPorTituloAutorEstado(); // Creamos una instancia de VerUsuarios
        panelCentral.add(panelUsuarios, BorderLayout.CENTER);

        panelCentral.revalidate();
        panelCentral.repaint();
    }
    
    private java.sql.Date obtenerFechaNacimiento(JDatePickerImpl datePicker) {
    Object selectedDate = datePicker.getModel().getValue();
    if (selectedDate != null) {
        return new java.sql.Date(((java.util.Date) selectedDate).getTime());
    }
    return null; // Si no se seleccionó ninguna fecha
}


    private String obtenerCorreoUsuarioAutenticado() {
        return this.emailUsuario;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuAdministrador("admin@colegio.com")); // Correo de prueba
    }

  }