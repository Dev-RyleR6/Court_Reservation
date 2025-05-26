package ui;

import model.Reservation;
import model.Account;
import model.Court;
import dao.ReservationDAO;
import dao.CourtDAO;
import dao.AccountDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationManagementFrame extends JFrame {
    private JTable reservationTable;
    private final ReservationDAO reservationDAO;
    private final CourtDAO courtDAO;
    private final AccountDAO accountDAO;
    private JComboBox<String> filterCombo;
    private JTextField searchField;
    private JLabel totalReservationsLabel;
    private JLabel pendingReservationsLabel;
    private JLabel todayReservationsLabel;
    
    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(51, 51, 51);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);

    public ReservationManagementFrame() {
        this.reservationDAO = new ReservationDAO();
        this.courtDAO = new CourtDAO();
        this.accountDAO = new AccountDAO();
        
        setTitle("Reservation Management");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header with title and search/filter
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Stats Panel
        mainPanel.add(createStatsPanel(), BorderLayout.WEST);

        // Table
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);

        // Bottom panel with action buttons
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        loadReservations();
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        statsPanel.setPreferredSize(new Dimension(200, 0));

        // Total Reservations
        totalReservationsLabel = createStatsLabel("Total Reservations", "0");
        statsPanel.add(createStatsCard("📊 Overview", totalReservationsLabel));

        // Pending Reservations
        pendingReservationsLabel = createStatsLabel("Pending", "0");
        statsPanel.add(createStatsCard("⏳ Pending", pendingReservationsLabel));

        // Today's Reservations
        todayReservationsLabel = createStatsLabel("Today", "0");
        statsPanel.add(createStatsCard("📅 Today", todayReservationsLabel));

        return statsPanel;
    }

    private JPanel createStatsCard(String title, JLabel statsLabel) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(PRIMARY_COLOR);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(statsLabel, BorderLayout.CENTER);

        return card;
    }

    private JLabel createStatsLabel(String prefix, String value) {
        JLabel label = new JLabel(prefix + ": " + value);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return label;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("Reservation Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Search and filter panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(BACKGROUND_COLOR);

        // Date filter
        JComboBox<String> dateFilterCombo = new JComboBox<>(new String[]{
            "All Dates", "Today", "This Week", "This Month"
        });
        dateFilterCombo.addActionListener(e -> loadReservations());

        // Search field
        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "Search by user, court, or purpose...");
        searchField.addActionListener(e -> loadReservations());
        
        // Status filter
        filterCombo = new JComboBox<>(new String[]{
            "All Status", "Pending", "Approved", "Rejected", "Cancelled", "Completed"
        });
        filterCombo.addActionListener(e -> loadReservations());

        controlsPanel.add(new JLabel("Date:"));
        controlsPanel.add(dateFilterCombo);
        controlsPanel.add(Box.createHorizontalStrut(10));
        controlsPanel.add(new JLabel("Status:"));
        controlsPanel.add(filterCombo);
        controlsPanel.add(Box.createHorizontalStrut(10));
        controlsPanel.add(new JLabel("Search:"));
        controlsPanel.add(searchField);

        headerPanel.add(controlsPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);

        // Create table with enhanced columns
        String[] columns = {
            "ID", "User", "Department", "Court Type", "Court", "Date", "Time", 
            "Status", "Created", "Last Updated"
        };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reservationTable = new JTable(model);
        reservationTable.setFillsViewportHeight(true);
        reservationTable.setRowHeight(35);
        reservationTable.setShowGrid(true);
        reservationTable.setGridColor(new Color(230, 230, 230));
        reservationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Style the table header
        reservationTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        reservationTable.getTableHeader().setBackground(Color.WHITE);
        reservationTable.getTableHeader().setForeground(PRIMARY_COLOR);
        
        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < columns.length; i++) {
            reservationTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Custom renderer for Status column
        reservationTable.getColumnModel().getColumn(7).setCellRenderer(new StatusColumnRenderer());
        
        // Set column widths
        reservationTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        reservationTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // User
        reservationTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Department
        reservationTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Court Type
        reservationTable.getColumnModel().getColumn(4).setPreferredWidth(120);  // Court
        reservationTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Date
        reservationTable.getColumnModel().getColumn(6).setPreferredWidth(120);  // Time
        reservationTable.getColumnModel().getColumn(7).setPreferredWidth(100);  // Status
        reservationTable.getColumnModel().getColumn(8).setPreferredWidth(150);  // Created
        reservationTable.getColumnModel().getColumn(9).setPreferredWidth(150);  // Last Updated

        // Add double-click listener for detailed view
        reservationTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetailedView();
                }
            }
        });

        // Add tooltip to show full text on hover
        reservationTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = reservationTable.rowAtPoint(e.getPoint());
                int col = reservationTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0) {
                    Object value = reservationTable.getValueAt(row, col);
                    reservationTable.setToolTipText(value != null ? value.toString() : null);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        tablePanel.add(scrollPane);

        return tablePanel;
    }

    private void showDetailedView() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow == -1) return;

        int reservationId = (int) reservationTable.getValueAt(selectedRow, 0);
        try {
            Reservation reservation = reservationDAO.getReservationById(reservationId);
            if (reservation == null) return;

            Account user = accountDAO.getAccountById(reservation.getAccountId());
            Court court = courtDAO.getCourtById(reservation.getCourtId());

            JDialog detailDialog = new JDialog(this, "Reservation Details", true);
            detailDialog.setSize(500, 400);
            detailDialog.setLocationRelativeTo(this);

            JPanel detailPanel = new JPanel(new BorderLayout(10, 10));
            detailPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            detailPanel.setBackground(Color.WHITE);

            // Create details content
            JPanel contentPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            contentPanel.setBackground(Color.WHITE);

            addDetailRow(contentPanel, "Reservation ID:", String.valueOf(reservation.getReservationId()));
            addDetailRow(contentPanel, "User:", user != null ? user.getUsername() : "Unknown");
            addDetailRow(contentPanel, "Court:", court != null ? court.getDescription() : "Unknown");
            addDetailRow(contentPanel, "Date:", reservation.getReservationDate().toString());
            addDetailRow(contentPanel, "Time:", 
                reservation.getStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                reservation.getEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            addDetailRow(contentPanel, "Status:", reservation.getStatus());
            addDetailRow(contentPanel, "Created:", reservation.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            addDetailRow(contentPanel, "Last Updated:", reservation.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            // Add purpose in a scrollable text area
            JPanel purposePanel = new JPanel(new BorderLayout(5, 5));
            purposePanel.setBackground(Color.WHITE);
            purposePanel.add(new JLabel("Purpose:"), BorderLayout.NORTH);
            
            JTextArea purposeArea = new JTextArea(reservation.getRemark());
            purposeArea.setLineWrap(true);
            purposeArea.setWrapStyleWord(true);
            purposeArea.setEditable(false);
            purposeArea.setRows(3);
            
            purposePanel.add(new JScrollPane(purposeArea), BorderLayout.CENTER);

            detailPanel.add(contentPanel, BorderLayout.CENTER);
            detailPanel.add(purposePanel, BorderLayout.SOUTH);

            // Add close button
            JButton closeButton = new JButton("Close");
            closeButton.setFocusPainted(false);
            closeButton.addActionListener(e -> detailDialog.dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(closeButton);

            detailPanel.add(buttonPanel, BorderLayout.SOUTH);

            detailDialog.add(detailPanel);
            detailDialog.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading reservation details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        panel.add(new JLabel(label, SwingConstants.RIGHT));
        panel.add(new JLabel(value));
    }

    private void loadReservations() {
        DefaultTableModel model = (DefaultTableModel) reservationTable.getModel();
        model.setRowCount(0);
        
        try {
            String searchText = searchField.getText().toLowerCase().trim();
            String filterStatus = (String) filterCombo.getSelectedItem();
            if (filterStatus.startsWith("All")) filterStatus = null;
            
            List<Reservation> reservations = reservationDAO.getAllReservationsWithDetails();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            int totalCount = 0;
            int pendingCount = 0;
            int todayCount = 0;
            LocalDateTime now = LocalDateTime.now();
            
            for (Reservation reservation : reservations) {
                // Apply filters - make status comparison case-insensitive
                if (filterStatus != null && !filterStatus.equalsIgnoreCase(reservation.getStatus())) {
                    continue;
                }
                
                // Apply search
                if (!searchText.isEmpty() && 
                    !reservation.getUserFullName().toLowerCase().contains(searchText) && 
                    !reservation.getCourtName().toLowerCase().contains(searchText) &&
                    !reservation.getDepartment().toLowerCase().contains(searchText) &&
                    !reservation.getRemark().toLowerCase().contains(searchText)) {
                    continue;
                }
                
                // Calculate duration
                long durationHours = java.time.Duration.between(
                    reservation.getStartDateTime(),
                    reservation.getEndDateTime()
                ).toHours();
                
                model.addRow(new Object[]{ // load reservation gi add sa table
                    reservation.getReservationId(),
                    reservation.getUserFullName(),
                    reservation.getDepartment(),
                    reservation.getCourtType(),
                    reservation.getCourtName(),
                    reservation.getReservationDate().format(dateFormatter),
                    reservation.getStartDateTime().format(timeFormatter) + " - " + 
                        reservation.getEndDateTime().format(timeFormatter) + 
                        " (" + durationHours + "h)",
                    reservation.getStatus(),
                    "Created: " + reservation.getCreatedAt().format(timestampFormatter) + 
                        " by " + reservation.getUserName(),
                    "Updated: " + reservation.getUpdatedAt().format(timestampFormatter)
                });

                // Update counters
                totalCount++;
                if ("Pending".equals(reservation.getStatus())) {
                    pendingCount++;
                }
                if (reservation.getReservationDate().equals(now.toLocalDate())) {
                    todayCount++;
                }
            }

            // Update statistics
            totalReservationsLabel.setText("Total: " + totalCount);
            pendingReservationsLabel.setText("Pending: " + pendingCount);
            todayReservationsLabel.setText("Today: " + todayCount);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading reservations: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND_COLOR);

        // Info panel on the left
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(BACKGROUND_COLOR);
        JLabel selectedLabel = new JLabel("Select a reservation to approve or reject");
        selectedLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        selectedLabel.setForeground(Color.GRAY);
        infoPanel.add(selectedLabel);
        bottomPanel.add(infoPanel, BorderLayout.WEST);

        // Action buttons on the right
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        JButton approveButton = new JButton("Approve");
        styleButton(approveButton, SUCCESS_COLOR);
        approveButton.addActionListener(e -> handleStatusChange("Approved"));

        JButton rejectButton = new JButton("Reject");
        styleButton(rejectButton, DANGER_COLOR);
        rejectButton.addActionListener(e -> handleStatusChange("Rejected"));

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, Color.GRAY);
        refreshButton.addActionListener(e -> loadReservations());

        JButton closeButton = new JButton("Close");
        styleButton(closeButton, new Color(108, 117, 125));
        closeButton.addActionListener(e -> dispose());

        buttonsPanel.add(approveButton);
        buttonsPanel.add(rejectButton);
        buttonsPanel.add(Box.createHorizontalStrut(20));
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createHorizontalStrut(20));
        buttonsPanel.add(closeButton);

        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        return bottomPanel;
    }

    private void handleStatusChange(String newStatus) {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a reservation first",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reservationId = (int) reservationTable.getValueAt(selectedRow, 0);
        String currentStatus = (String) reservationTable.getValueAt(selectedRow, 7);
        
        // Validate status change
        if ("Cancelled".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                "Cannot modify a cancelled reservation",
                "Invalid Action",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (currentStatus.equals(newStatus)) {
            JOptionPane.showMessageDialog(this,
                "Reservation is already " + newStatus.toLowerCase(),
                "No Change",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            boolean success = reservationDAO.updateReservationStatus(reservationId, newStatus);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Reservation status updated to " + newStatus,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                loadReservations();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to update reservation status",
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
                    case "Approved":
                        setForeground(SUCCESS_COLOR);
                        break;
                    case "Pending":
                        setForeground(WARNING_COLOR);
                        break;
                    case "Rejected":
                    case "Cancelled":
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
