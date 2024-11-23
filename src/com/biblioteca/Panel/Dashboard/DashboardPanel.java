package com.biblioteca.Panel.Dashboard;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.biblioteca.basedatos.ConexionBaseDatos;

public class DashboardPanel extends JPanel {
    private final String emailUsuario;
    private JLabel welcomeLabel;
    private JPanel cardPanel;

    // Constantes de diseño
    private static final Color COLOR_PRIMARIO = new Color(51, 102, 153);
    private static final Color COLOR_PRESTAMOS = new Color(52, 152, 219);
    private static final Color COLOR_MORA = new Color(231, 76, 60);
    private static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FUENTE_CARD = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FUENTE_NUMERO = new Font("Segoe UI", Font.BOLD, 48);
    private static final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);

    public DashboardPanel(String email) {
        this.emailUsuario = email;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initializeUI();
        loadDashboardData();
    }

    private void initializeUI() {
        // Panel de bienvenida
        JPanel welcomePanel = createWelcomePanel();
        add(welcomePanel, BorderLayout.NORTH);

        // Panel de tarjetas
        cardPanel = createCardPanel();
        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        welcomeLabel = new JLabel("¡Bienvenido al Sistema de Biblioteca!");
        welcomeLabel.setFont(FUENTE_TITULO);
        welcomeLabel.setForeground(COLOR_PRIMARIO);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Panel de Control de Préstamos");
        subtitleLabel.setFont(FUENTE_NORMAL);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(welcomeLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitleLabel);

        return panel;
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));

        // Card de Préstamos Actuales
        JPanel currentLoansCard = createCard(
            "Préstamos Actuales",
            "0",
            "libros prestados",
            COLOR_PRESTAMOS
        );

        // Card de Préstamos en Mora
        JPanel overdueLoansCard = createCard(
            "Préstamos en Mora",
            "0",
            "libros vencidos",
            COLOR_MORA
        );

        panel.add(currentLoansCard);
        panel.add(overdueLoansCard);

        return panel;
    }

    private JPanel createCard(String title, String number, String description, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FUENTE_CARD);
        titleLabel.setForeground(color);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(FUENTE_NUMERO);
        numberLabel.setForeground(color);
        numberLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        numberLabel.setName("numberLabel"); // Añadido para facilitar la actualización

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(FUENTE_NORMAL);
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(numberLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descLabel);

        return card;
    }

    private void loadDashboardData() {
        try (Connection conn = ConexionBaseDatos.getConexion()) {
            // Cargar nombre del usuario
            loadUserName(conn);
            
            // Obtener ID del usuario
            String userId = getUserIdByEmail(conn, emailUsuario);
            if (userId == null) {
                showError("No se encontró el usuario con el email proporcionado.");
                return;
            }

            // Cargar datos de préstamos
            loadLoansData(conn, userId);
        } catch (SQLException e) {
            showError("Error al cargar los datos del dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLoansData(Connection conn, String userId) throws SQLException {
        // Contar préstamos actuales
        String currentLoansQuery = "SELECT COUNT(*) as total FROM prestamos WHERE id_usuario = ? AND estado = 'Pendiente'";
        loadCardData(conn, currentLoansQuery, userId, 0);

        // Contar préstamos en mora
        String overdueLoansQuery = "SELECT COUNT(*) as total FROM prestamos WHERE id_usuario = ? AND estado = 'Mora'";
        loadCardData(conn, overdueLoansQuery, userId, 1);
    }


    private String getUserIdByEmail(Connection conn, String email) throws SQLException {
        String query = "SELECT id FROM usuarios WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return String.valueOf(rs.getString("id")); // Convertir a String
            }
        }
        return null; // Retorna null si no se encuentra el usuario
    }

    private void loadUserName(Connection conn) throws SQLException {
        String query = "SELECT nombre FROM usuarios WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, emailUsuario);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String nombre = rs.getString("nombre");
                if (nombre != null && !nombre.isEmpty()) {
                    welcomeLabel.setText("¡Bienvenido, " + nombre + "!");
                }
            }
        }
    }

    private void loadCardData(Connection conn, String query, String userId, int cardIndex) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId); // Usa setString
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                updateCard(cardIndex, String.valueOf(rs.getInt("total")));
            }
        }
    }

    private void updateCard(int cardIndex, String newValue) {
        if (cardPanel.getComponent(cardIndex) instanceof JPanel) {
            JPanel card = (JPanel) cardPanel.getComponent(cardIndex);
            for (Component comp : card.getComponents()) {
                if (comp instanceof JLabel && ((JLabel) comp).getName() != null 
                    && ((JLabel) comp).getName().equals("numberLabel")) {
                    ((JLabel) comp).setText(newValue);
                    break;
                }
            }
        }
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    public void refreshDashboard() {
        loadDashboardData();
    }
}