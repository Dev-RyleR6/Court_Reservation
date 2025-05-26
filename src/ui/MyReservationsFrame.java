package ui;

import model.Account;
import model.Reservation;
import model.Court;
import dao.ReservationDAO;
import dao.CourtDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyReservationsFrame extends JFrame {
    private final Account account;
    private JTable reservationsTable;
    private JComboBox<String> filterCombo;
    
    // Minimal color scheme
    private static final Color PRIMARY_COLOR = new Color(51, 51, 51);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);

    public MyReservationsFrame(Account account) {
        this.account = account;
        setTitle("My Reservations");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Table Panel
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);

        // Bottom Panel with buttons
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        loadReservations();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("My Court Reservations");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(BACKGROUND_COLOR);
        
        filterPanel.add(new JLabel("Filter:"));
        filterCombo = new JComboBox<>(new String[]{"All", "Upcoming", "Past", "Pending", "Approved"});
        filterCombo.addActionListener(e -> loadReservations());
        filterPanel.add(filterCombo);

        headerPanel.add(filterPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);

        // Create table
        String[] columns = {"ID", "Date", "Time", "Duration", "Court", "Purpose", "Status", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only allow editing of Actions column
            }
        };
        
        reservationsTable = new JTable(model);
        reservationsTable.setFillsViewportHeight(true);
        reservationsTable.setRowHeight(35);
        reservationsTable.setShowGrid(false);
        reservationsTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style the table header
        reservationsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        reservationsTable.getTableHeader().setBackground(Color.WHITE);
        reservationsTable.getTableHeader().setForeground(PRIMARY_COLOR);
        
        // Center align all columns except Purpose
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < columns.length; i++) {
            if (i != 5) { // Skip Purpose column
                reservationsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // Custom renderer for Status column
        reservationsTable.getColumnModel().getColumn(6).setCellRenderer(new StatusColumnRenderer());
        
        // Custom renderer and editor for Actions column
        TableColumn actionColumn = reservationsTable.getColumnModel().getColumn(7);
        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Set column widths
        reservationsTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        reservationsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        reservationsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Time
        reservationsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Duration
        reservationsTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Court
        reservationsTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Purpose
        reservationsTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Status
        reservationsTable.getColumnModel().getColumn(7).setPreferredWidth(100); // Actions

        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        tablePanel.add(scrollPane);

        return tablePanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND_COLOR);

        // Summary panel on the left
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setBackground(BACKGROUND_COLOR);
        JLabel totalLabel = new JLabel("Total Reservations: 0");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        summaryPanel.add(totalLabel);
        bottomPanel.add(summaryPanel, BorderLayout.WEST);

        // Buttons panel on the right
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(Color.WHITE);
        refreshButton.addActionListener(e -> loadReservations());

        JButton closeButton = new JButton("Close");
        closeButton.setBackground(Color.WHITE);
        closeButton.addActionListener(e -> dispose());

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(closeButton);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        return bottomPanel;
    }

    private void loadReservations() {
        DefaultTableModel model = (DefaultTableModel) reservationsTable.getModel();
        model.setRowCount(0); // Clear existing data
        
        try {
            ReservationDAO reservationDAO = new ReservationDAO();
            CourtDAO courtDAO = new CourtDAO();
            List<Reservation> reservations = reservationDAO.getReservationsByAccount(account.getAccountId());
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            for (Reservation reservation : reservations) {
                // Get court information
                Court court = courtDAO.getCourtById(reservation.getCourtId());
                String courtName = court != null ? court.getDescription() : "Unknown Court";
                
                // Calculate duration in hours
                long durationHours = java.time.Duration.between(
                    reservation.getStartDateTime(),
                    reservation.getEndDateTime()
                ).toHours();

                model.addRow(new Object[]{  //mao ni table na gi butngan sa reservation
                    reservation.getReservationId(),
                    reservation.getReservationDate().format(dateFormatter),
                    reservation.getStartDateTime().format(timeFormatter),
                    durationHours + " hr" + (durationHours > 1 ? "s" : ""),
                    courtName,
                    reservation.getRemark(),
                    reservation.getStatus(),
                    "Cancel"
                });
            }
            
            // Update total reservations count in a type-safe way
            Container contentPane = getContentPane();
            if (contentPane.getComponent(0) instanceof JPanel) {
                JPanel mainPanel = (JPanel) contentPane.getComponent(0);
                if (mainPanel.getComponent(2) instanceof JPanel) {
                    JPanel bottomPanel = (JPanel) mainPanel.getComponent(2);
                    for (Component comp : bottomPanel.getComponents()) {
                        if (comp instanceof JPanel) {
                            JPanel subPanel = (JPanel) comp;
                            for (Component subComp : subPanel.getComponents()) {
                                if (subComp instanceof JLabel) {
                                    JLabel label = (JLabel) subComp;
                                    if (label.getText().startsWith("Total")) {
                                        label.setText("Total Reservations: " + reservations.size());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading reservations: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
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
                        setForeground(new Color(40, 167, 69));
                        break;
                    case "Pending":
                        setForeground(new Color(255, 193, 7));
                        break;
                    case "Cancelled":
                        setForeground(new Color(220, 53, 69));
                        break;
                    default:
                        setForeground(table.getForeground());
                }
            }
            return c;
        }
    }

    // Custom button renderer for the Actions column
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(220, 53, 69)); // Red color for cancel button
            setForeground(Color.WHITE);
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (String) table.getModel().getValueAt(row, 6);
            setEnabled(!"Cancelled".equals(status));
            setText("Cancel");
            return this;
        }
    }

    // Custom button editor for the Actions column
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(new Color(220, 53, 69)); // Red color for cancel button
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            
            // Handle button click
            button.addActionListener(e -> {
                fireEditingStopped();
                // Get reservation ID and handle cancellation
                int reservationId = (int) reservationsTable.getValueAt(currentRow, 0);
                cancelReservation(reservationId);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            
            // Check if the reservation is already cancelled
            String status = (String) table.getValueAt(row, 6);
            button.setEnabled(!"Cancelled".equals(status));
            
            label = "Cancel";
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    private void cancelReservation(int reservationId) {
        try {
            ReservationDAO reservationDAO = new ReservationDAO();
            
            // Get the reservation to check its status
            Reservation reservation = reservationDAO.getReservationById(reservationId);
            if (reservation == null) {
                JOptionPane.showMessageDialog(this,
                    "Reservation not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if reservation is already cancelled
            if ("Cancelled".equals(reservation.getStatus())) {
                JOptionPane.showMessageDialog(this,
                    "This reservation is already cancelled.",
                    "Cannot Cancel",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Confirm cancellation
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this reservation?\nThis action cannot be undone.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = reservationDAO.updateReservationStatus(reservationId, "Cancelled");
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Reservation cancelled successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadReservations(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to cancel reservation. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
} 