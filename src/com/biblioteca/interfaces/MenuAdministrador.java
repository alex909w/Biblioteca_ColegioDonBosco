package com.biblioteca.interfaces;

import biblioteca.interfaces.Inventario.EliminarArticulo;
import biblioteca.interfaces.Inventario.EditarArticulo;
import biblioteca.interfaces.Inventario.RegistrarArticulo;
import com.biblioteca.Formularios.CrearFormulario;
import com.biblioteca.Formularios.EditarFormulario;
import com.biblioteca.Formularios.EliminarFormulario;
import com.biblioteca.acciones.Devoluciones.RegistrarDevolucion;
import com.biblioteca.acciones.Mora.VerMoraPendiente;
import com.biblioteca.acciones.Prestamos.BuscarPorAutor;
import com.biblioteca.acciones.Prestamos.BuscarPorTitulo;
import com.biblioteca.acciones.Prestamos.FiltrarPorEstado;
import com.biblioteca.acciones.Prestamos.HistorialPrestamos;
import com.biblioteca.acciones.Prestamos.RegistrarPrestamo;
import com.biblioteca.acciones.Usuarios.EditarUsuario;
import com.biblioteca.acciones.Usuarios.EliminarUsuario;
import com.biblioteca.acciones.Usuarios.AgregarUsuario;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MenuAdministrador extends JFrame {
    private JPanel panelCentral;
    private JPanel panelIzquierdo;
    private Map<String, JPanel> submenusVisibles;

    public MenuAdministrador() {
        setTitle("Menú Principal: Administrador");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel izquierdo (Menú Vertical)
        panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPanelIzquierdo = new JScrollPane(panelIzquierdo);
        scrollPanelIzquierdo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPanelIzquierdo, BorderLayout.WEST);

        // Panel central
        panelCentral = new JPanel();
        panelCentral.setLayout(new BorderLayout());
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

    private void configurarMenu() {
        agregarBotonMenu("Gestión de Usuarios", new String[]{
            "Agregar Usuario", "Editar Usuario", "Eliminar Usuario"
        });

        agregarBotonMenu("Gestión de Formularios", new String[]{
            "Crear Formulario", "Editar Formulario", "Eliminar Formulario"
        });

        agregarBotonMenu("Gestión de Inventario", new String[]{
            "Registrar Artículos", "Editar Artículos", "Eliminar Artículos"
        });

        agregarBotonMenu("Consultar Ejemplares", new String[]{
            "Buscar por Título", "Buscar por Autor", "Filtrar por Estado"
        });

        agregarBotonMenu("Gestión de Ejemplares", new String[]{
            "Registrar Préstamos", "Historial de Préstamos"
        });

        agregarBotonMenu("Gestión de Devoluciones", new String[]{
            "Registrar Devolución"
        });

        agregarBotonMenu("Configuraciones", new String[]{
            "Ver Mora Pendiente"
        });
    }

    private void agregarBotonMenu(String titulo, String[] submenus) {
        JButton botonMenu = new JButton(titulo);
        botonMenu.setFont(new Font("Arial", Font.BOLD, 16));
        botonMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonMenu.setBackground(new Color(200, 200, 200));
        botonMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel subMenuPanel = new JPanel();
        subMenuPanel.setLayout(new BoxLayout(subMenuPanel, BoxLayout.Y_AXIS));
        subMenuPanel.setVisible(false);

        for (String submenu : submenus) {
            JButton botonSubmenu = new JButton(submenu);
            botonSubmenu.setFont(new Font("Arial", Font.PLAIN, 14));
            botonSubmenu.setAlignmentX(Component.LEFT_ALIGNMENT);
            botonSubmenu.setBackground(new Color(240, 240, 240));
            botonSubmenu.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 10));
            botonSubmenu.addActionListener(e -> ejecutarFuncionAdministrador(submenu));
            subMenuPanel.add(botonSubmenu);
        }

        botonMenu.addActionListener(e -> {
            boolean esVisible = subMenuPanel.isVisible();
            ocultarTodosLosSubmenus();
            subMenuPanel.setVisible(!esVisible);
            panelIzquierdo.revalidate();
            panelIzquierdo.repaint();
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
        // Crear el botón "Salir"
        JButton botonSalir = new JButton("Salir");
        botonSalir.setFont(new Font("Arial", Font.BOLD, 16));
        botonSalir.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonSalir.setBackground(new Color(255, 100, 100)); // Color rojo claro
        botonSalir.setForeground(Color.WHITE);
        botonSalir.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        botonSalir.addActionListener(e -> {
            // Cerrar la ventana actual y abrir la ventana de login
            dispose(); // Cierra la ventana de Menú Administrador
            new LoginBiblioteca(); // Abre la ventana de Login
        });

        // Crear un Filler para empujar el botón "Salir" hasta el fondo
        Box.Filler filler = new Box.Filler(new Dimension(0, 0), new Dimension(0, Integer.MAX_VALUE), new Dimension(0, 0));

        // Añadir el Filler al panel izquierdo para empujar el botón al final
        panelIzquierdo.add(Box.createVerticalGlue()); // Esto empuja el botón "Salir" hacia abajo

        // Añadir el botón Salir al panel izquierdo
        panelIzquierdo.add(botonSalir);
    }

    private void ejecutarFuncionAdministrador(String submenu) {
        panelCentral.removeAll(); // Limpiar contenido previo

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

            // Consultar Ejemplares
            case "Buscar por Título":
                nuevoPanel = new BuscarPorTitulo();
                break;
            case "Buscar por Autor":
                nuevoPanel = new BuscarPorAutor();
                break;
            case "Filtrar por Estado":
                nuevoPanel = new FiltrarPorEstado();
                break;

            // Gestión de Ejemplares
            case "Registrar Préstamos":
                nuevoPanel = new RegistrarPrestamo();
                break;
            case "Historial de Préstamos":
                nuevoPanel = new HistorialPrestamos();
                break;

            // Gestión de Devoluciones
            case "Registrar Devolución":
                nuevoPanel = new RegistrarDevolucion();
                break;

            // Configuraciones
            case "Ver Mora Pendiente":
                nuevoPanel = new VerMoraPendiente();
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

    public static void main(String[] args) {
        new MenuAdministrador(); // Prueba como Administrador
    }
}
