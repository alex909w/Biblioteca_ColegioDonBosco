package com.biblioteca.interfaces.menus;

<<<<<<< Updated upstream:src/com/biblioteca/interfaces/MenuAlumno.java
import com.biblioteca.acciones.Prestamos.BuscarPorTituloAutorEstado;
import com.biblioteca.acciones.Prestamos.HistorialPrestamos;
import com.biblioteca.acciones.Prestamos.ConsultarPrestamos;
import com.biblioteca.acciones.Prestamos.GestionPrestamos;
import com.biblioteca.acciones.Prestamos.RegistrarDevolucion;
=======
>>>>>>> Stashed changes:src/com/biblioteca/interfaces/menus/MenuAlumno.java


import com.biblioteca.Panel.Prestamos.BuscarPorTituloAutorEstado;
import com.biblioteca.Panel.Prestamos.ConsultarPrestamos;
import com.biblioteca.Panel.Prestamos.GestionPrestamos;
import com.biblioteca.Panel.Prestamos.HistorialPrestamos;
import com.biblioteca.Panel.Prestamos.RegistrarDevolucion;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class MenuAlumno extends JFrame {
    private String emailUsuario;
    private JPanel panelCentral;
    private JPanel panelIzquierdo;
    private Map<String, JPanel> submenusVisibles;

    private final Color COLOR_PRIMARIO = new Color(51, 102, 153);
    private final Color FONDO_LATERAL = new Color(248, 249, 250);
    private final Color COLOR_HOVER = new Color(233, 236, 239);
    private final Font FUENTE_PRINCIPAL = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);

    public MenuAlumno(String email) {
        this.emailUsuario = email;
        setTitle("Menú Principal - Rol: Alumno");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(crearPanelSuperior(), BorderLayout.NORTH);

        panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panelIzquierdo.setBackground(FONDO_LATERAL);
        JScrollPane scrollPanelIzquierdo = new JScrollPane(panelIzquierdo);
        scrollPanelIzquierdo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPanelIzquierdo, BorderLayout.WEST);

        panelCentral = new JPanel();
        panelCentral.setLayout(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createTitledBorder("Contenido"));
        add(panelCentral, BorderLayout.CENTER);

        submenusVisibles = new HashMap<>();

        configurarMenu();
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
        agregarBotonMenu("Gestión de Préstamos", new String[]{
            "Buscar por Título, Autor o Estado","Solicitar Préstamo", "Ver Historial de Préstamos","Ver Estado de Devoluciones"
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
        botonSalir.setPreferredSize(new Dimension(250, 40));
        botonSalir.setMaximumSize(new Dimension(250, 40));
        botonSalir.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        botonSalir.setFocusPainted(false);

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
            new LoginBiblioteca(); // Asegúrate de que LoginBiblioteca tenga un constructor sin parámetros
        });

        panelIzquierdo.add(Box.createVerticalGlue());
        panelIzquierdo.add(botonSalir);
    }

    private void ejecutarFuncionAlumno(String submenu) {
        panelCentral.removeAll();
        String correoUsuario = obtenerCorreoUsuarioAutenticado();

        JPanel nuevoPanel = null;

        switch (submenu) {
            case "Buscar por Título, Autor o Estado":
                nuevoPanel = new BuscarPorTituloAutorEstado(); // Pasar el email
                break;
            case "Solicitar Préstamo":
                nuevoPanel = new GestionPrestamos(correoUsuario); // Implementa esta clase y pasa el email
                break;
            case "Ver Historial de Préstamos":
                nuevoPanel = new HistorialPrestamos(correoUsuario); // Pasar el email
                break;
            case "Ver Estado de Devoluciones":
                nuevoPanel = new RegistrarDevolucion(correoUsuario); // Implementa esta clase y pasa el email
                break;
            case "Consultas":
                nuevoPanel = new ConsultarPrestamos(correoUsuario); // Implementa esta clase y pasa el email
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

    private String obtenerCorreoUsuarioAutenticado() {
        return this.emailUsuario;
    }

   
}