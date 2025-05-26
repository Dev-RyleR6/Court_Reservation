package dao;

import model.Reservation;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    
    public boolean createReservation(Reservation reservation) throws SQLException {
        String query = "INSERT INTO reservation (account_id, court_id, reservation_date, " +
                      "start_datetime, end_datetime, status, remark) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, reservation.getAccountId());
            stmt.setInt(2, reservation.getCourtId());
            stmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
            stmt.setTimestamp(4, Timestamp.valueOf(reservation.getStartDateTime()));
            stmt.setTimestamp(5, Timestamp.valueOf(reservation.getEndDateTime()));
            stmt.setString(6, reservation.getStatus());
            stmt.setString(7, reservation.getRemark());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reservation.setReservationId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<Reservation> getReservationsByAccount(int accountId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation WHERE account_id = ? ORDER BY reservation_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, accountId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = new Reservation();
                    reservation.setReservationId(rs.getInt("reservation_id"));
                    reservation.setAccountId(rs.getInt("account_id"));
                    reservation.setCourtId(rs.getInt("court_id"));
                    reservation.setReservationDate(rs.getDate("reservation_date").toLocalDate());
                    reservation.setStartDateTime(rs.getTimestamp("start_datetime").toLocalDateTime());
                    reservation.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
                    reservation.setStatus(rs.getString("status"));
                    reservation.setRemark(rs.getString("remark"));
                    reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    reservation.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    reservations.add(reservation);
                }
            }
        }
        return reservations;
    }

    public boolean updateReservationStatus(int reservationId, String status) throws SQLException {
        // Ensure first letter is uppercase and rest is lowercase
        status = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        
        String query = "UPDATE reservation SET status = ? WHERE reservation_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, reservationId);
            
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Reservation> getReservationsByDate(LocalDate date) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation WHERE reservation_date = ? AND status != 'cancelled'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDate(1, Date.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = new Reservation();
                    reservation.setReservationId(rs.getInt("reservation_id"));
                    reservation.setAccountId(rs.getInt("account_id"));
                    reservation.setCourtId(rs.getInt("court_id"));
                    reservation.setReservationDate(rs.getDate("reservation_date").toLocalDate());
                    reservation.setStartDateTime(rs.getTimestamp("start_datetime").toLocalDateTime());
                    reservation.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
                    reservation.setStatus(rs.getString("status"));
                    reservation.setRemark(rs.getString("remark"));
                    reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    reservation.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    reservations.add(reservation);
                }
            }
        }
        return reservations;
    }

    public Reservation getReservationById(int reservationId) throws SQLException {
        String query = "SELECT * FROM reservation WHERE reservation_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, reservationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Reservation reservation = new Reservation();
                    reservation.setReservationId(rs.getInt("reservation_id"));
                    reservation.setAccountId(rs.getInt("account_id"));
                    reservation.setCourtId(rs.getInt("court_id"));
                    reservation.setReservationDate(rs.getDate("reservation_date").toLocalDate());
                    reservation.setStartDateTime(rs.getTimestamp("start_datetime").toLocalDateTime());
                    reservation.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
                    reservation.setStatus(rs.getString("status"));
                    reservation.setRemark(rs.getString("remark"));
                    reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    reservation.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return reservation;
                }
            }
        }
        return null;
    }

    public List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservation ORDER BY reservation_date DESC, start_datetime ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setReservationId(rs.getInt("reservation_id"));
                reservation.setAccountId(rs.getInt("account_id"));
                reservation.setCourtId(rs.getInt("court_id"));
                reservation.setReservationDate(rs.getDate("reservation_date").toLocalDate());
                reservation.setStartDateTime(rs.getTimestamp("start_datetime").toLocalDateTime());
                reservation.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
                reservation.setStatus(rs.getString("status"));
                reservation.setRemark(rs.getString("remark"));
                reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                reservation.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                reservations.add(reservation);
            }
        }
        return reservations;
    }

    public List<Reservation> getAllReservationsWithDetails() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT r.*, " +
                      "a.username as user_name, " +
                      "a.fn as user_firstname, " +
                      "a.ln as user_lastname, " +
                      "c.description as court_name, " +
                      "ct.description as court_type, " +
                      "d.name as department_name " +
                      "FROM reservation r " +
                      "JOIN account a ON r.account_id = a.account_id " +
                      "JOIN court c ON r.court_id = c.court_id " +
                      "JOIN court_type ct ON c.court_type_id = ct.court_type_id " +
                      "JOIN department d ON a.department_id = d.department_id " +
                      "ORDER BY r.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setReservationId(rs.getInt("reservation_id"));
                reservation.setAccountId(rs.getInt("account_id"));
                reservation.setCourtId(rs.getInt("court_id"));
                reservation.setReservationDate(rs.getDate("reservation_date").toLocalDate());
                reservation.setStartDateTime(rs.getTimestamp("start_datetime").toLocalDateTime());
                reservation.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
                reservation.setStatus(rs.getString("status"));
                reservation.setRemark(rs.getString("remark"));
                reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                reservation.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                
                reservation.setUserName(rs.getString("user_name"));
                reservation.setUserFullName(rs.getString("user_firstname") + " " + rs.getString("user_lastname"));
                reservation.setCourtName(rs.getString("court_name"));
                reservation.setCourtType(rs.getString("court_type"));
                reservation.setDepartment(rs.getString("department_name"));
                
                reservations.add(reservation);
            }
        }
        return reservations;
    }
} 