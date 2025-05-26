package ui;

import model.Account;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ReserverDashboardFrame extends JFrame {
    private final Account account;
    // Minimal color scheme
    private static final Color PRIMARY_COLOR = new Color(51, 51, 51);    // Dark gray
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);   // Steel blue
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light gray
    private static final Color CARD_COLOR = new Color(255, 255, 255);    // White

    public ReserverDashboardFrame(Account account) {
        this.account = account;
        setTitle("Court Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBackground(BACKGROUND_COLOR);
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top bar with user info and time
        JPanel topBar = createTopBar();
        container.add(topBar, BorderLayout.NORTH);

        // Center panel with action cards
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add cards with icons

        centerPanel.add(createCard("Reserve Court", "🏀", () -> openReservationForm()));
        centerPanel.add(createCard("My Reservations", "📋", () -> viewMyReservations()));
        centerPanel.add(createCard("Account Info", "👤", () -> viewAccountInfo()));
        centerPanel.add(createCard("Logout", "🚪", () -> logout()));



        container.add(centerPanel, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(CARD_COLOR);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel statusLabel = new JLabel("Connected to system");
        statusLabel.setForeground(PRIMARY_COLOR);
        statusBar.add(statusLabel);
        container.add(statusBar, BorderLayout.SOUTH);

        add(container);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(CARD_COLOR);
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // User info on the left
        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfo.setBackground(CARD_COLOR);
        JLabel welcomeLabel = new JLabel("Welcome, " + account.getFirstName());
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        welcomeLabel.setForeground(PRIMARY_COLOR);
        userInfo.add(welcomeLabel);

        // Role badge
        JLabel roleLabel = new JLabel(account.getRole());
        roleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        roleLabel.setForeground(ACCENT_COLOR);
        roleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        userInfo.add(Box.createHorizontalStrut(10));
        userInfo.add(roleLabel);

        topBar.add(userInfo, BorderLayout.WEST);

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
        
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Click handler
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
        
        // Hover effect
        card.addMouseListener(new MouseAdapter() {
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

    private void openReservationForm() {
        new ReservationForm(this.account).setVisible(true);
    }

    private void viewMyReservations() {
        new MyReservationsFrame(this.account).setVisible(true);
    }

    private void viewAccountInfo() {
        new AccountInfoFrame(this.account).setVisible(true);
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
