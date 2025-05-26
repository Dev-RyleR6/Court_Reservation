package ui;

import model.Court;
import model.CourtType;
import dao.CourtDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CourtManagementFrame extends JFrame {
    private JTable courtTable;
    private final CourtDAO courtDAO;
    private JComboBox<String> filterCombo;
    private JTextField searchField;
    private Map<Integer, String> courtTypeMap;
    private List<CourtType> courtTypes;
    
    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(51, 51, 51);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);

    public CourtManagementFrame() {
        this.courtDAO = new CourtDAO();
        this.courtTypeMap = new HashMap<>();
        
        try {
            // Load court types
            this.courtTypes = courtDAO.getAllCourtTypes();
            for (CourtType type : courtTypes) {
                courtTypeMap.put(type.getCourtTypeId(), type.getDescription());
            }
        } catch (SQLException e) {
            showError("Error loading court types: " + e.getMessage());
        }
        
        setTitle("Court Management");
        setSize(800, 600);
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
        loadCourts();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("Court Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Search and filter panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlsPanel.setBackground(BACKGROUND_COLOR);

        // Search field
        searchField = new JTextField(15);
        searchField.putClientProperty("JTextField.placeholderText", "Search courts...");
        searchField.addActionListener(e -> loadCourts());
        
        // Filter dropdown with court types
        String[] filterOptions = new String[courtTypes.size() + 1];
        filterOptions[0] = "All";
        for (int i = 0; i < courtTypes.size(); i++) {
            filterOptions[i + 1] = courtTypes.get(i).getDescription();
        }
        filterCombo = new JComboBox<>(filterOptions);
        filterCombo.addActionListener(e -> loadCourts());

        controlsPanel.add(new JLabel("Search:"));
        controlsPanel.add(searchField);
        controlsPanel.add(Box.createHorizontalStrut(10));
        controlsPanel.add(new JLabel("Type:"));
        controlsPanel.add(filterCombo);

        headerPanel.add(controlsPanel, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);

        // Create table
        String[] columns = {"ID", "Name", "Type", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        courtTable = new JTable(model);
        courtTable.setFillsViewportHeight(true);
        courtTable.setRowHeight(35);
        courtTable.setShowGrid(true);
        courtTable.setGridColor(new Color(230, 230, 230));
        courtTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Style the table header
        courtTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        courtTable.getTableHeader().setBackground(Color.WHITE);
        courtTable.getTableHeader().setForeground(PRIMARY_COLOR);
        
        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < columns.length; i++) {
            courtTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Set column widths
        courtTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        courtTable.getColumnModel().getColumn(1).setPreferredWidth(200);  // Name
        courtTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Type
        courtTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Status

        JScrollPane scrollPane = new JScrollPane(courtTable);
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
        JLabel selectedLabel = new JLabel("Select a court to edit or delete");
        selectedLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        selectedLabel.setForeground(Color.GRAY);
        infoPanel.add(selectedLabel);
        bottomPanel.add(infoPanel, BorderLayout.WEST);

        // Action buttons on the right
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);

        JButton addButton = new JButton("Add Court");
        styleButton(addButton, SUCCESS_COLOR);
        addButton.addActionListener(e -> showCourtDialog(null));

        JButton editButton = new JButton("Edit");
        styleButton(editButton, ACCENT_COLOR);
        editButton.addActionListener(e -> {
            int selectedRow = courtTable.getSelectedRow();
            if (selectedRow != -1) {
                int courtId = (int) courtTable.getValueAt(selectedRow, 0);
                try {
                    Court court = courtDAO.getCourtById(courtId);
                    if (court != null) {
                        showCourtDialog(court);
                    }
                } catch (SQLException ex) {
                    showError("Error loading court details: " + ex.getMessage());
                }
            } else {
                showWarning("Please select a court to edit.");
            }
        });

        JButton deleteButton = new JButton("Delete");
        styleButton(deleteButton, DANGER_COLOR);
        deleteButton.addActionListener(e -> deleteCourt());

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, Color.GRAY);
        refreshButton.addActionListener(e -> loadCourts());

        JButton closeButton = new JButton("Close");
        styleButton(closeButton, new Color(108, 117, 125));
        closeButton.addActionListener(e -> dispose());

        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(Box.createHorizontalStrut(20));
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(Box.createHorizontalStrut(20));
        buttonsPanel.add(closeButton);

        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        return bottomPanel;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    private void loadCourts() {
        DefaultTableModel model = (DefaultTableModel) courtTable.getModel();
        model.setRowCount(0);
        
        try {
            String searchText = searchField.getText().toLowerCase().trim();
            String filterType = (String) filterCombo.getSelectedItem();
            
            List<Court> courts = courtDAO.getAllCourts();
            
            for (Court court : courts) {
                String courtType = courtTypeMap.get(court.getCourtTypeId());
                
                // Apply type filter
                if (!"All".equals(filterType) && !filterType.equals(courtType)) {
                    continue;
                }
                
                // Apply search
                if (!searchText.isEmpty() && 
                    !court.getDescription().toLowerCase().contains(searchText)) {
                    continue;
                }
                
                model.addRow(new Object[]{
                    court.getCourtId(),
                    court.getDescription(),
                    courtType,
                    "Available" // You could add real-time availability check here
                });
            }
        } catch (SQLException e) {
            showError("Error loading courts: " + e.getMessage());
        }
    }

    private void showCourtDialog(Court court) {
        JDialog dialog = new JDialog(this, court == null ? "Add New Court" : "Edit Court", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Court Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Court Name:"), gbc);

        JTextField nameField = new JTextField(20);
        if (court != null) {
            nameField.setText(court.getDescription());
        }
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(nameField, gbc);

        // Court Type
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Court Type:"), gbc);

        JComboBox<CourtType> typeCombo = new JComboBox<>(courtTypes.toArray(new CourtType[0]));
        if (court != null) {
            for (int i = 0; i < courtTypes.size(); i++) {
                if (courtTypes.get(i).getCourtTypeId() == court.getCourtTypeId()) {
                    typeCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(typeCombo, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(108, 117, 125));
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton saveButton = new JButton(court == null ? "Add" : "Save");
        styleButton(saveButton, SUCCESS_COLOR);
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showWarning("Please enter a court name.");
                    return;
                }

                CourtType selectedType = (CourtType) typeCombo.getSelectedItem();
                
                if (court == null) {
                    // Create new court
                    Court newCourt = new Court(0, selectedType.getCourtTypeId(), name);
                    if (courtDAO.createCourt(newCourt)) {
                        dialog.dispose();
                        loadCourts();
                        showInfo("Court added successfully.");
                    }
                } else {
                    // Update existing court
                    court.setCourtTypeId(selectedType.getCourtTypeId());
                    court.setDescription(name);
                    if (courtDAO.updateCourt(court)) {
                        dialog.dispose();
                        loadCourts();
                        showInfo("Court updated successfully.");
                    }
                }
            } catch (SQLException ex) {
                showError("Error saving court: " + ex.getMessage());
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(saveButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteCourt() {
        int selectedRow = courtTable.getSelectedRow();
        if (selectedRow != -1) {
            int courtId = (int) courtTable.getValueAt(selectedRow, 0);
            String courtName = (String) courtTable.getValueAt(selectedRow, 1);
            
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete " + courtName + "?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (courtDAO.deleteCourt(courtId)) {
                        loadCourts();
                        showInfo("Court deleted successfully.");
                    } else {
                        showWarning("Cannot delete court: It has existing reservations.");
                    }
                } catch (SQLException e) {
                    showError("Error deleting court: " + e.getMessage());
                }
            }
        } else {
            showWarning("Please select a court to delete.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
} 