package dao;

import model.Account;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private static final String SQL_LOGIN = 
        "SELECT * FROM account WHERE username = ? AND password = ?";
        
    private static final String SQL_GET_ALL_ACCOUNTS =
        "SELECT * FROM account ORDER BY account_id";
        
    private static final String SQL_UPDATE_STATUS =
        "UPDATE account SET status = ? WHERE account_id = ?";
        
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_INACTIVE = "Inactive";

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashedBytes = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public Account login(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_LOGIN)) {
            
            String hashedPassword = hashPassword(password);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Account account = mapResultSetToAccount(rs);
                    String status = account.getStatus();
                    if (status != null) {
                        if (status.equalsIgnoreCase("active")) {
                            account.setStatus(STATUS_ACTIVE);
                            return account;
                        } else if (status.equalsIgnoreCase("inactive")) {
                            account.setStatus(STATUS_INACTIVE);
                            throw new SQLException("Account is not activated. Please contact administrator.");
                        }
                    }
                }
            }
            return null;
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }
    
    public List<Account> getAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_ALL_ACCOUNTS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Account account = mapResultSetToAccount(rs);
                String status = account.getStatus();
                if (status != null) {
                    if (status.equalsIgnoreCase("active")) {
                        account.setStatus(STATUS_ACTIVE);
                    } else if (status.equalsIgnoreCase("inactive")) {
                        account.setStatus(STATUS_INACTIVE);
                    }
                }
                accounts.add(account);
            }
        }
        
        return accounts;
    }
    
    public boolean updateAccountStatus(int accountId, String status) throws SQLException {
        if (!status.equalsIgnoreCase(STATUS_ACTIVE) && !status.equalsIgnoreCase(STATUS_INACTIVE)) {
            throw new IllegalArgumentException("Invalid status value. Must be either 'Active' or 'Inactive'");
        }
        
        status = status.equalsIgnoreCase(STATUS_ACTIVE) ? STATUS_ACTIVE : STATUS_INACTIVE;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_STATUS)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, accountId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setTypeId(rs.getInt("type_id"));
        account.setDepartmentId(rs.getInt("department_id"));
        account.setFirstName(rs.getString("fn"));
        account.setLastName(rs.getString("ln"));
        account.setEmail(rs.getString("email"));
        account.setUsername(rs.getString("username"));
        account.setPassword(rs.getString("password"));
        account.setStatus(rs.getString("status"));
        account.setPhone(rs.getString("phone"));
        return account;
    }
/* soon to be implemented if mo accept nag signup
    public boolean registerUser(Account account) {
        String sql = "INSERT INTO account (type_id, department_id, college_id, fn, ln, email, phone, username, password, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, account.getTypeId());
            stmt.setInt(2, account.getDepartmentId());
            stmt.setInt(3, account.getCollegeId());
            stmt.setString(4, account.getFirstName());
            stmt.setString(5, account.getLastName());
            stmt.setString(6, account.getEmail());
            stmt.setString(7, account.getPhone());
            stmt.setString(8, account.getUsername());
            stmt.setString(9, hashPassword(account.getPassword()));
            stmt.setString(10, STATUS_INACTIVE);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }

 */

    public Account getAccountById(int accountId) {
        String sql = "SELECT * FROM account WHERE account_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Account account = new Account();
                    account.setAccountId(rs.getInt("account_id"));
                    account.setTypeId(rs.getInt("type_id"));
                    account.setDepartmentId(rs.getInt("department_id"));
                    account.setFirstName(rs.getString("fn"));
                    account.setLastName(rs.getString("ln"));
                    account.setEmail(rs.getString("email"));
                    account.setUsername(rs.getString("username"));
                    account.setPassword(rs.getString("password"));
                    account.setStatus(rs.getString("status"));
                    account.setPhone(rs.getString("phone"));
                    return account;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return null;
    }
}
