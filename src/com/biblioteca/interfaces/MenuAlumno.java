package com.biblioteca.interfaces;

import com.biblioteca.acciones.Mora.VerMoraPendiente;
import com.biblioteca.acciones.Prestamos.BuscarPorAutor;
import com.biblioteca.acciones.Prestamos.BuscarPorTitulo;
import com.biblioteca.acciones.Prestamos.HistorialPrestamos;
import com.biblioteca.acciones.Prestamos.RegistrarPrestamo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class MenuAlumno extends JFrame {
    private JPanel panelCentral;
    private JPanel panelIzquierdo;
    private Map<String, JPanel> submenusVisibles;
    
        private final Color COLOR_PRIMARIO = new Color(51, 102, 153);
    private final Color FONDO_LATERAL = new Color(248, 249, 250);
    private final Color COLOR_HOVER = new Color(233, 236, 239);
    private final Font FUENTE_PRINCIPAL = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    
    public MenuAlumno() {
        setTitle("Menú Principal - Rol: Alumno");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
            add(crearPanelSuperior(), BorderLayout.NORTH);
            
        // Panel izquierdo (Menú Vertical)
        panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelIzquierdo.setBackground(FONDO_LATERAL);
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
        agregarBotonMenu("Consultar Ejemplares", new String[]{
            "Buscar por Título", "Buscar por Autor"
        });
        agregarBotonMenu("Gestión de Préstamos", new String[]{
            "Solicitar Préstamo", "Ver Historial de Préstamos"
        });
        agregarBotonMenu("Gestión de Devoluciones", new String[]{
            "Ver Mora Pendiente", "Ver Estado de Devoluciones"
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

        // Agregar el efecto de hover (cambio de color al pasar el ratón sobre el botón)
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
            botonSubmenu.setPreferredSize(new Dimension(250, 40));  // Hacer que los submenús también tengan el mismo tamaño
            botonSubmenu.setMaximumSize(new Dimension(250, 40));  // Asegurarse de que todos los submenús tengan el mismo tamaño
            botonSubmenu.addActionListener(e -> ejecutarFuncionAlumno(submenu));
            subMenuPanel.add(botonSubmenu);
            
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
        botonSalir.setFont(FUENTE_PRINCIPAL);
        botonSalir.setAlignmentX(Component.LEFT_ALIGNMENT);
        botonSalir.setBackground(new Color(193, 42, 46)); // Color rojo claro
        botonSalir.setForeground(Color.WHITE);
        botonSalir.setPreferredSize(new Dimension(100, 40));  // El tamaño del botón "Salir" es el mismo que los otros
        botonSalir.setMaximumSize(new Dimension(100, 40));  // Asegurarse de que tenga el mismo tamaño que los demás botones
        botonSalir.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
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
            // Cerrar la ventana actual y abrir la ventana de login
            dispose(); // Cierra la ventana de Menú Alumno
            new LoginBiblioteca(); // Abre la ventana de Login
        });

        // Crear un Filler para empujar el botón "Salir" hasta el fondo
        Box.Filler filler = new Box.Filler(new Dimension(0, 0), new Dimension(0, Integer.MAX_VALUE), new Dimension(0, 0));

        // Añadir el Filler al panel izquierdo para empujar el botón al final
        panelIzquierdo.add(Box.createVerticalGlue()); // Esto empuja el botón "Salir" hacia abajo

        // Añadir el botón Salir al panel izquierdo
        panelIzquierdo.add(botonSalir);
    }

    private void ejecutarFuncionAlumno(String submenu) {
        panelCentral.removeAll(); // Limpiar contenido previo

        JPanel nuevoPanel = null;

        switch (submenu) {
            case "Buscar por Título":
                nuevoPanel = new BuscarPorTitulo();
                break;
            case "Buscar por Autor":
                nuevoPanel = new BuscarPorAutor();
                break;
            case "Solicitar Préstamo":
                nuevoPanel = new RegistrarPrestamo();
                break;
            case "Ver Historial de Préstamos":
                nuevoPanel = new HistorialPrestamos();
                break;
            case "Ver Mora Pendiente":
                nuevoPanel = new VerMoraPendiente();
                break;
            case "Ver Estado de Devoluciones":
                nuevoPanel = new JPanel(); // Por implementar
                nuevoPanel.add(new JLabel("Función pendiente: Ver Estado de Devoluciones"));
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
        new MenuAlumno(); // Prueba como Alumno
    }
}
