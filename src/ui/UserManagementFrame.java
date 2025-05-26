package ui;

import model.Account;
import dao.AccountDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class
UserManagementFrame extends JFrame {
    private JTable userTable;
    private final AccountDAO accountDAO;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    
    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(51, 51, 51);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);

    public UserManagementFrame() {
        this.accountDAO = new AccountDAO();
        
        setTitle("User Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header with title and search/filter
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Table
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);

        // Bottom panel with action buttons
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        loadUsers();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Search and filter panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(BACKGROUND_COLOR);

        // Search field
        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "Search users...");
        searchField.addActionListener(e -> loadUsers());
        
        // Filter dropdown using standardized status values
        filterCombo = new JComboBox<>(new String[]{"All", AccountDAO.STATUS_ACTIVE, AccountDAO.STATUS_INACTIVE});
        filterCombo.addActionListener(e -> loadUsers());

        controlsPanel.add(new JLabel("Search:"));
        controlsPanel.add(searchField);
        controlsPanel.add(Box.createHorizontalStrut(10));
        controlsPanel.add(new JLabel("Status:"));
        controlsPanel.add(filterCombo);

        headerPanel.add(controlsPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);

        // Create table
        String[] columns = {"ID", "Username", "First Name", "Last Name", "Email", "Role", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(model);
        userTable.setFillsViewportHeight(true);
        userTable.setRowHeight(35);
        userTable.setShowGrid(true);
        userTable.setGridColor(new Color(230, 230, 230));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Style the table header
        userTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        userTable.getTableHeader().setBackground(Color.WHITE);
        userTable.getTableHeader().setForeground(PRIMARY_COLOR);
        
        // Center align all columns except Email
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < columns.length; i++) {
            if (i != 4) { // Skip Email column
                userTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // Custom renderer for Status column
        userTable.getColumnModel().getColumn(6).setCellRenderer(new StatusColumnRenderer());
        
        // Set column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        userTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // Username
        userTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // First Name
        userTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Last Name
        userTable.getColumnModel().getColumn(4).setPreferredWidth(200);  // Email
        userTable.getColumnModel().getColumn(5).setPreferredWidth(80);   // Role
        userTable.getColumnModel().getColumn(6).setPreferredWidth(80);   // Status

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        tablePanel.add(scrollPane);

        return tablePanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND_COLOR);

        // Info panel on the left
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(BACKGROUND_COLOR);
        JLabel selectedLabel = new JLabel("Select a user to activate or deactivate");
        selectedLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        selectedLabel.setForeground(Color.GRAY);
        infoPanel.add(selectedLabel);
        bottomPanel.add(infoPanel, BorderLayout.WEST);

        // Action buttons on the right
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        JButton activateButton = new JButton("Activate");
        styleButton(activateButton, SUCCESS_COLOR);
        activateButton.addActionListener(e -> updateUserStatus(AccountDAO.STATUS_ACTIVE));

        JButton deactivateButton = new JButton("Deactivate");
        styleButton(deactivateButton, DANGER_COLOR);
        deactivateButton.addActionListener(e -> updateUserStatus(AccountDAO.STATUS_INACTIVE));

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, Color.GRAY);
        refreshButton.addActionListener(e -> loadUsers());

        JButton closeButton = new JButton("Close");
        styleButton(closeButton, new Color(108, 117, 125));
        closeButton.addActionListener(e -> dispose());

        buttonsPanel.add(activateButton);
        buttonsPanel.add(deactivateButton);
        buttonsPanel.add(Box.createHorizontalStrut(20));
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createHorizontalStrut(20));
        buttonsPanel.add(closeButton);

        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        return bottomPanel;
    }

    private void loadUsers() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0);
        
        try {
            String searchText = searchField.getText().toLowerCase().trim();
            String filterStatus = (String) filterCombo.getSelectedItem();
            
            List<Account> users = accountDAO.getAllAccounts();
            
            for (Account user : users) {
                // Apply filters - make status comparison case-insensitive
                if (!"All".equals(filterStatus) && !filterStatus.equalsIgnoreCase(user.getStatus())) {
                    continue;
                }
                
                // Apply search
                if (!searchText.isEmpty() && 
                    !user.getUsername().toLowerCase().contains(searchText) && 
                    !user.getEmail().toLowerCase().contains(searchText) &&
                    !user.getFirstName().toLowerCase().contains(searchText) &&
                    !user.getLastName().toLowerCase().contains(searchText)) {
                    continue;
                }
                
                model.addRow(new Object[]{
                    user.getAccountId(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatus()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading users: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateUserStatus(String newStatus) {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a user first",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int accountId = (int) userTable.getValueAt(selectedRow, 0);
        String currentStatus = (String) userTable.getValueAt(selectedRow, 6);
        String username = (String) userTable.getValueAt(selectedRow, 1);
        
        if (currentStatus.equalsIgnoreCase(newStatus)) {
            JOptionPane.showMessageDialog(this,
                "User is already " + newStatus.toLowerCase(),
                "No Change",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            boolean success = accountDAO.updateAccountStatus(accountId, newStatus);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "User " + username + " has been " + newStatus.toLowerCase(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to update user status",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    // Custom renderer for Status column
    private class StatusColumnRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.CENTER);
            
            if (value != null) {
                String status = value.toString();
                switch (status) {
                    case "Active":
                        setForeground(SUCCESS_COLOR);
                        break;
                    case "Inactive":
                        setForeground(DANGER_COLOR);
                        break;
                    default:
                        setForeground(table.getForeground());
                }
            }
            return c;
        }
    }
}
