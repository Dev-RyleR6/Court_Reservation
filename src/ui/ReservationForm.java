package ui;

import dao.CourtDAO;
import dao.ReservationDAO;
import model.Account;
import model.Reservation;
import model.Court;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import java.util.List;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class ReservationForm extends JFrame {
    private final Account account;
    private JComboBox<Court> courtCombo;
    private JSpinner dateSpinner;
    private JComboBox<String> timeCombo;
    private JTextArea purposeArea;
    private JSpinner durationSpinner;
    private JTextArea descriptionArea;
    private JLabel availabilityLabel;
    
    // Minimal color scheme
    private static final Color PRIMARY_COLOR = new Color(51, 51, 51);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(70, 130, 180);

    public ReservationForm(Account account) {
        this.account = account;
        setTitle("Reserve Court");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("New Court Reservation");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Court Selection
        mainPanel.add(createFormField("Select Court:", createCourtPanel()));
        mainPanel.add(Box.createVerticalStrut(15));

        // Date Selection
        mainPanel.add(createFormField("Select Date:", createDatePanel()));
        mainPanel.add(Box.createVerticalStrut(15));

        // Time Selection
        mainPanel.add(createFormField("Select Time:", createTimePanel()));
        mainPanel.add(Box.createVerticalStrut(15));

        // Duration Selection
        mainPanel.add(createFormField("Duration (hours):", createDurationPanel()));
        mainPanel.add(Box.createVerticalStrut(15));

        // Purpose
        mainPanel.add(createFormField("Purpose:", createPurposePanel()));
        mainPanel.add(Box.createVerticalStrut(20));

        // Buttons
        mainPanel.add(createButtonPanel());

        add(mainPanel);

        addAvailabilityListeners();
    }

    private JPanel createFormField(String label, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(BACKGROUND_COLOR);
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(fieldLabel, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCourtPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(BACKGROUND_COLOR);
        
        try {
            CourtDAO courtDAO = new CourtDAO();
            List<Court> courts = courtDAO.getAllCourts();
            
            // Create selection panel with just the combo box
            JPanel selectionPanel = new JPanel(new BorderLayout(5, 0));
            selectionPanel.setBackground(BACKGROUND_COLOR);
            
            courtCombo = new JComboBox<>(courts.toArray(new Court[0]));
            courtCombo.addActionListener(e -> updateCourtDescription());
            
            selectionPanel.add(courtCombo, BorderLayout.CENTER);
            
            // Create description panel
            JPanel descriptionPanel = new JPanel(new BorderLayout());
            descriptionPanel.setBackground(BACKGROUND_COLOR);
            descriptionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            
            descriptionArea = new JTextArea(2, 20);
            descriptionArea.setEditable(false);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setLineWrap(true);
            descriptionArea.setFont(new Font("SansSerif", Font.ITALIC, 11));
            descriptionArea.setBackground(new Color(245, 245, 245));
            descriptionArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            descriptionPanel.add(descriptionArea);
            
            panel.add(selectionPanel, BorderLayout.NORTH);
            panel.add(descriptionPanel, BorderLayout.CENTER);
            
            // Initial description update
            updateCourtDescription();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading courts: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            
            // Create an empty combo box if database access fails
            courtCombo = new JComboBox<>(new Court[0]);
            panel.add(courtCombo);
        }
        
        return panel;
    }
    
    private void updateCourtDescription() {
        Court selectedCourt = (Court) courtCombo.getSelectedItem();
        if (selectedCourt != null && descriptionArea != null) {
            String description = String.format(
                "Type: %s\nDescription: %s",
                selectedCourt.getCourtTypeId() == 1 ? "Indoor" : "Outdoor",
                selectedCourt.getDescription()
            );
            descriptionArea.setText(description);
        }
    }

    private JPanel createDatePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(BACKGROUND_COLOR);

        // Create the date input panel (spinner + calendar button)
        JPanel dateInputPanel = new JPanel(new BorderLayout(5, 0));
        dateInputPanel.setBackground(BACKGROUND_COLOR);

        // Set up date range
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar maxDate = (Calendar) today.clone();
        maxDate.add(Calendar.MONTH, 3);

        // Create date spinner
        SpinnerDateModel model = new SpinnerDateModel(today.getTime(), today.getTime(), maxDate.getTime(), Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd MMM yyyy (EEE)");
        dateSpinner.setEditor(editor);

        // Create calendar button
        JButton calendarButton = new JButton("📅");
        calendarButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        calendarButton.setFocusPainted(false);
        calendarButton.setBackground(Color.WHITE);
        calendarButton.setToolTipText("Click to open calendar");
        calendarButton.addActionListener(e -> showCalendarPopup(dateSpinner));

        dateInputPanel.add(dateSpinner, BorderLayout.CENTER);
        dateInputPanel.add(calendarButton, BorderLayout.EAST);

        // Add helper text
        JLabel helperLabel = new JLabel("You can book up to " + new SimpleDateFormat("dd MMM yyyy").format(maxDate.getTime()));
        helperLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
        helperLabel.setForeground(Color.GRAY);

        panel.add(dateInputPanel, BorderLayout.CENTER);
        panel.add(helperLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void showCalendarPopup(JSpinner dateSpinner) {
        // Create popup frame
        JDialog popup = new JDialog(this, "Select Date", true);
        popup.setLayout(new BorderLayout());

        // Get current date from spinner
        Date currentDate = (Date) dateSpinner.getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Create calendar panel
        JPanel calendarPanel = new JPanel(new BorderLayout(5, 5));
        calendarPanel.setBackground(Color.WHITE);
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Month/Year navigation panel
        JPanel navigationPanel = new JPanel(new BorderLayout(5, 0));
        navigationPanel.setBackground(Color.WHITE);
        
        JButton prevMonth = new JButton("◀");
        JButton nextMonth = new JButton("▶");
        JLabel monthYearLabel = new JLabel("", SwingConstants.CENTER);
        
        navigationPanel.add(prevMonth, BorderLayout.WEST);
        navigationPanel.add(monthYearLabel, BorderLayout.CENTER);
        navigationPanel.add(nextMonth, BorderLayout.EAST);

        // Days panel
        JPanel daysPanel = new JPanel(new GridLayout(7, 7, 5, 5));
        daysPanel.setBackground(Color.WHITE);

        // Day names
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String dayName : dayNames) {
            JLabel label = new JLabel(dayName, SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            daysPanel.add(label);
        }

        // Update calendar display
        updateCalendarPanel(calendar, daysPanel, monthYearLabel, dateSpinner, popup);

        // Add month navigation listeners
        prevMonth.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendarPanel(calendar, daysPanel, monthYearLabel, dateSpinner, popup);
        });

        nextMonth.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendarPanel(calendar, daysPanel, monthYearLabel, dateSpinner, popup);
        });

        calendarPanel.add(navigationPanel, BorderLayout.NORTH);
        calendarPanel.add(daysPanel, BorderLayout.CENTER);

        popup.add(calendarPanel);
        popup.pack();
        popup.setLocationRelativeTo(dateSpinner);
        popup.setVisible(true);
    }

    private void updateCalendarPanel(Calendar calendar, JPanel daysPanel, JLabel monthYearLabel,
                                   JSpinner dateSpinner, JDialog popup) {
        // Clear existing day buttons
        Component[] components = daysPanel.getComponents();
        for (int i = 7; i < components.length; i++) {
            daysPanel.remove(components[i]);
        }

        // Update month/year label
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy");
        monthYearLabel.setText(monthYearFormat.format(calendar.getTime()));

        // Get current selection
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTime((Date) dateSpinner.getValue());

        // Get min/max dates
        SpinnerDateModel model = (SpinnerDateModel) dateSpinner.getModel();
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        minDate.setTime((Date) model.getStart());
        maxDate.setTime((Date) model.getEnd());

        // Calculate first day of month
        Calendar temp = (Calendar) calendar.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add empty cells before first day
        for (int i = 1; i < firstDayOfWeek; i++) {
            daysPanel.add(new JLabel());
        }

        // Add day buttons
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setMargin(new Insets(2, 2, 2, 2));
            
            // Set the calendar to this day
            temp.set(Calendar.DAY_OF_MONTH, day);
            
            // Check if date is within valid range
            boolean isValidDate = temp.compareTo(minDate) >= 0 && temp.compareTo(maxDate) <= 0;
            
            // Style button based on selection and validity
            if (temp.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                temp.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                temp.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)) {
                dayButton.setBackground(ACCENT_COLOR);
                dayButton.setForeground(Color.WHITE);
            } else {
                dayButton.setBackground(Color.WHITE);
                dayButton.setForeground(isValidDate ? Color.BLACK : Color.LIGHT_GRAY);
            }
            
            if (isValidDate) {
                final Date dateToSelect = temp.getTime();
                dayButton.addActionListener(e -> {
                    dateSpinner.setValue(dateToSelect);
                    popup.dispose();
                });
            } else {
                dayButton.setEnabled(false);
            }
            
            daysPanel.add(dayButton);
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private JPanel createTimePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Time selection combo box
        timeCombo = new JComboBox<>(generateTimeSlots());
        timeCombo.addActionListener(e -> checkAvailability());
        
        // Availability indicator
        availabilityLabel = new JLabel("");
        availabilityLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        
        panel.add(timeCombo, BorderLayout.CENTER);
        panel.add(availabilityLabel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createDurationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        SpinnerNumberModel durationModel = new SpinnerNumberModel(1, 1, 4, 1);
        durationSpinner = new JSpinner(durationModel);
        panel.add(durationSpinner);
        return panel;
    }

    private JPanel createPurposePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        purposeArea = new JTextArea(4, 20);
        purposeArea.setLineWrap(true);
        purposeArea.setWrapStyleWord(true);
        purposeArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane scrollPane = new JScrollPane(purposeArea);
        panel.add(scrollPane);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(BACKGROUND_COLOR);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.WHITE);
        cancelButton.addActionListener(e -> dispose());

        JButton submitButton = new JButton("Submit Reservation");
        submitButton.setBackground(ACCENT_COLOR);
        submitButton.setForeground(Color.WHITE);
        submitButton.addActionListener(e -> submitReservation());

        panel.add(cancelButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(submitButton);
        return panel;
    }

    private String[] generateTimeSlots() {
        String[] slots = new String[14]; // 7AM to 8PM
        for (int i = 7, j = 0; i <= 20; i++, j++) {
            slots[j] = String.format("%02d:00", i);
        }
        return slots;
    }

    private void submitReservation() {
        StringBuilder errors = new StringBuilder();
        
        // Validate court selection
        Court selectedCourt = (Court) courtCombo.getSelectedItem();
        if (selectedCourt == null) {
            errors.append("- Please select a court\n");
        }
        
        // Validate date
        Date selectedDate = (Date) dateSpinner.getValue();
        LocalDate reservationDate = selectedDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
        
        if (reservationDate.isBefore(LocalDate.now())) {
            errors.append("- Cannot make reservations for past dates\n");
        }
        
        // Validate time
        String selectedTime = (String) timeCombo.getSelectedItem();
        int hour = Integer.parseInt(selectedTime.split(":")[0]);
        LocalDateTime startDateTime = reservationDate.atTime(hour, 0);
        
        if (startDateTime.isBefore(LocalDateTime.now())) {
            errors.append("- Cannot make reservations for past time slots\n");
        }
        
        // Validate duration
        int duration = (Integer) durationSpinner.getValue();
        LocalDateTime endDateTime = startDateTime.plusHours(duration);
        
        // Check if end time is after closing time (20:00)
        if (endDateTime.getHour() > 20) {
            errors.append("- Reservation cannot extend beyond 20:00\n");
        }
        
        // Validate purpose
        String purpose = purposeArea.getText().trim();
        if (purpose.isEmpty()) {
            errors.append("- Please enter the purpose of reservation\n");
        } else if (purpose.length() < 10) {
            errors.append("- Purpose should be at least 10 characters long\n");
        }
        
        // If there are validation errors, show them and return
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this,
                "Please correct the following errors:\n" + errors.toString(),
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Check court availability
            CourtDAO courtDAO = new CourtDAO();
            if (!courtDAO.isCourtAvailable(selectedCourt.getCourtId(), reservationDate, startDateTime, endDateTime)) {
                JOptionPane.showMessageDialog(this,
                    "Sorry, the court is not available for the selected time slot.\n" +
                    "Please choose a different time or date.",
                    "Court Not Available",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // dri insert sa reservation data
            Reservation reservation = new Reservation(
                account.getAccountId(),
                selectedCourt.getCourtId(),
                reservationDate,
                startDateTime,
                endDateTime,
                purpose
            );
            
            // Save to database
            ReservationDAO reservationDAO = new ReservationDAO();
            boolean success = reservationDAO.createReservation(reservation);
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Reservation submitted successfully!\n\n" +
                    "Details:\n" +
                    "Court: " + selectedCourt.getDescription() + "\n" +
                    "Date: " + new SimpleDateFormat("dd MMM yyyy (EEE)").format(selectedDate) + "\n" +
                    "Time: " + selectedTime + " - " + endDateTime.format(DateTimeFormatter.ofPattern("HH:mm")) + "\n" +
                    "Duration: " + duration + " hour(s)",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close the form after successful submission
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to submit reservation. Please try again.",
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

    private void checkAvailability() {
        Court selectedCourt = (Court) courtCombo.getSelectedItem();
        Date selectedDate = (Date) dateSpinner.getValue();
        String selectedTime = (String) timeCombo.getSelectedItem();
        int duration = (Integer) durationSpinner.getValue();
        
        if (selectedCourt != null && selectedDate != null && selectedTime != null) {
            try {
                LocalDate reservationDate = selectedDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                
                int hour = Integer.parseInt(selectedTime.split(":")[0]);
                LocalDateTime startDateTime = reservationDate.atTime(hour, 0);
                LocalDateTime endDateTime = startDateTime.plusHours(duration);
                
                CourtDAO courtDAO = new CourtDAO();
                boolean isAvailable = courtDAO.isCourtAvailable(
                    selectedCourt.getCourtId(),
                    reservationDate,
                    startDateTime,
                    endDateTime
                );
                
                if (isAvailable) {
                    availabilityLabel.setText("✓ Court is available for this time slot");
                    availabilityLabel.setForeground(new Color(40, 167, 69)); // Green
                } else {
                    availabilityLabel.setText("✗ Court is not available for this time slot");
                    availabilityLabel.setForeground(new Color(220, 53, 69)); // Red
                }
            } catch (SQLException e) {
                availabilityLabel.setText("! Could not check availability");
                availabilityLabel.setForeground(Color.ORANGE);
                e.printStackTrace();
            }
        }
    }

    private void addAvailabilityListeners() {
        courtCombo.addActionListener(e -> checkAvailability());
        dateSpinner.addChangeListener(e -> checkAvailability());
        durationSpinner.addChangeListener(e -> checkAvailability());
    }
} 