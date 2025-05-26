package ui;

import model.Account;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminDashboardFrame extends JFrame {
    private final Account account;
    
    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(51, 51, 51);    // Dark gray
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);   // Steel blue
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light gray
    private static final Color CARD_COLOR = new Color(255, 255, 255);    // White

    public AdminDashboardFrame(Account account) {
        this.account = account;
        setTitle("Admin Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBackground(BACKGROUND_COLOR);
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top bar with admin info
        container.add(createTopBar(), BorderLayout.NORTH);

        // Center panel with admin cards
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add admin action cards
        centerPanel.add(createCard("Manage Reservations", "📅", () -> new ReservationManagementFrame().setVisible(true)));
        centerPanel.add(createCard("Manage Users", "👥", () -> new UserManagementFrame().setVisible(true)));
        centerPanel.add(createCard("Manage Courts", "🏟️", () -> manageCourts()));
        centerPanel.add(createCard("Logout", "🚪", () -> logout()));

        container.add(centerPanel, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(CARD_COLOR);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel statusLabel = new JLabel("System Status: Online");
        statusLabel.setForeground(PRIMARY_COLOR);
        statusBar.add(statusLabel);
        container.add(statusBar, BorderLayout.SOUTH);

        add(container);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(CARD_COLOR);
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Admin info on the left
        JPanel adminInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        adminInfo.setBackground(CARD_COLOR);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + account.getFirstName());
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        welcomeLabel.setForeground(PRIMARY_COLOR);
        adminInfo.add(welcomeLabel);

        // Admin badge
        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        roleLabel.setForeground(ACCENT_COLOR);
        roleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        adminInfo.add(Box.createHorizontalStrut(10));
        adminInfo.add(roleLabel);

        topBar.add(adminInfo, BorderLayout.WEST);

        return topBar;
    }

    private JPanel createCard(String title, String icon, Runnable onClick) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        // Icon and title panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(iconLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(titleLabel);

        card.add(contentPanel, BorderLayout.CENTER);
        
        // Hover effect and click handling
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(BACKGROUND_COLOR);
                contentPanel.setBackground(BACKGROUND_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_COLOR);
                contentPanel.setBackground(CARD_COLOR);
            }
        });
        
        return card;
    }

    private void manageCourts() {
        new CourtManagementFrame().setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
