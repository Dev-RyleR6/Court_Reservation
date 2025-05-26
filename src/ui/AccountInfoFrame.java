package ui;

import model.Account;
import javax.swing.*;
import java.awt.*;

public class AccountInfoFrame extends JFrame {
    private final Account account;
    
    // Minimal color scheme matching other frames
    private static final Color PRIMARY_COLOR = new Color(51, 51, 51);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);
    private static final Color CARD_COLOR = new Color(255, 255, 255);

    public AccountInfoFrame(Account account) {
        this.account = account;
        setTitle("Account Information");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Account Information");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Info Card
        JPanel infoCard = createInfoCard();
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(infoCard);
        mainPanel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonPanel = createButtonPanel();
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(buttonPanel);

        add(mainPanel);
    }

    private JPanel createInfoCard() {
        JPanel card = new JPanel();
        card.setLayout(new GridBagLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 15);

        // Add info fields
        addInfoField(card, gbc, "Name:", account.getFirstName() + " " + account.getLastName());
        addInfoField(card, gbc, "Username:", account.getUsername());
        addInfoField(card, gbc, "Role:", account.getRole());
        addInfoField(card, gbc, "Email:", account.getEmail());
        addInfoField(card, gbc, "Phone:", account.getPhone());

        return card;
    }

    private void addInfoField(JPanel panel, GridBagConstraints gbc, String label, String value) {
        // Label
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        fieldLabel.setForeground(PRIMARY_COLOR);
        panel.add(fieldLabel, gbc);

        // Value
        gbc.gridx = 1;
        JLabel fieldValue = new JLabel(value != null ? value : "Not provided");
        fieldValue.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panel.add(fieldValue, gbc);

        // Reset for next row
        gbc.gridx = 0;
        gbc.gridy++;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(BACKGROUND_COLOR);

        JButton editButton = new JButton("Edit Profile");
        editButton.setBackground(ACCENT_COLOR);
        editButton.setForeground(Color.WHITE);
        editButton.addActionListener(e -> editProfile());

        JButton closeButton = new JButton("Close");
        closeButton.setBackground(Color.WHITE);
        closeButton.addActionListener(e -> dispose());

        panel.add(editButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(closeButton);

        return panel;
    }

    private void editProfile() {
        JOptionPane.showMessageDialog(this,
            "Edit profile functionality will be implemented soon.",
            "Coming Soon",
            JOptionPane.INFORMATION_MESSAGE);
    }
} 