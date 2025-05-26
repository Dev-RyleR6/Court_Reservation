package ui;

import dao.AccountDAO;
import model.Account;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Court Reservation Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(250, 250, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginButton.setFocusPainted(false);
        loginButton.setBackground(new Color(33, 150, 243));
        loginButton.setForeground(Color.WHITE);
        panel.add(loginButton, gbc);

        getContentPane().add(panel);

        // Login button logic
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = String.valueOf(passwordField.getPassword()).trim();

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "Please enter both username and password.",
                        "Login Error",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    AccountDAO dao = new AccountDAO();
                    Account account = dao.login(username, password);

                    if (account != null) {
                        String role = account.getRole();
                        if (role.equals("admin")) {
                            new AdminDashboardFrame(account).setVisible(true);
                        } else if (role.equals("user")) {
                            new ReserverDashboardFrame(account).setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(LoginFrame.this, 
                                "Unknown role type.",
                                "Login Error",
                                JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Invalid username or password.",
                            "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (RuntimeException ex) {
                    if (ex.getMessage().contains("not activated")) {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Your account is not yet activated.\nPlease contact the administrator.",
                            "Account Inactive",
                            JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "Login error: " + ex.getMessage(),
                            "System Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }
}
