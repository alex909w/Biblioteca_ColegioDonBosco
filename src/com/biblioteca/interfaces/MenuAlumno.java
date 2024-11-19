package com.biblioteca.interfaces;

import com.biblioteca.acciones.Mora.VerMoraPendiente;
import com.biblioteca.acciones.Prestamos.BuscarPorAutor;
import com.biblioteca.acciones.Prestamos.BuscarPorTitulo;
import com.biblioteca.acciones.Prestamos.HistorialPrestamos;
import com.biblioteca.acciones.Prestamos.RegistrarPrestamo;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MenuAlumno extends JFrame {
    private JPanel panelCentral;
    private JPanel panelIzquierdo;
    private Map<String, JPanel> submenusVisibles;

    public MenuAlumno() {
        setTitle("Menú Principal - Rol: Alumno");
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

        setLocationRelativeTo(null); // Centrar la ventana
        setVisible(true);
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
            botonSubmenu.addActionListener(e -> ejecutarFuncionAlumno(submenu));
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
