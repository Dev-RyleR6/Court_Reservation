package dao;

import model.Court;
import model.CourtType;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CourtDAO {
    
    public List<Court> getAllCourts() throws SQLException {
        List<Court> courts = new ArrayList<>();
        String query = "SELECT * FROM court";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Court court = new Court(
                    rs.getInt("court_id"),
                    rs.getInt("court_type_id"),
                    rs.getString("description")
                );
                courts.add(court);
            }
        }
        return courts;
    }

    public Court getCourtById(int courtId) throws SQLException {
        String query = "SELECT * FROM court WHERE court_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, courtId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Court(
                        rs.getInt("court_id"),
                        rs.getInt("court_type_id"),
                        rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    public List<Court> getCourtsByType(int courtTypeId) throws SQLException {
        List<Court> courts = new ArrayList<>();
        String query = "SELECT * FROM court WHERE court_type_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, courtTypeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Court court = new Court(
                        rs.getInt("court_id"),
                        rs.getInt("court_type_id"),
                        rs.getString("description")
                    );
                    courts.add(court);
                }
            }
        }
        return courts;
    }

    public List<CourtType> getAllCourtTypes() throws SQLException {
        List<CourtType> courtTypes = new ArrayList<>();
        String query = "SELECT * FROM court_type";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                CourtType courtType = new CourtType(
                    rs.getInt("court_type_id"),
                    rs.getString("description")
                );
                courtTypes.add(courtType);
            }
        }
        return courtTypes;
    }

    public CourtType getCourtTypeById(int courtTypeId) throws SQLException {
        String query = "SELECT * FROM court_type WHERE court_type_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, courtTypeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new CourtType(
                        rs.getInt("court_type_id"),
                        rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

    public boolean isCourtAvailable(int courtId, LocalDate date, LocalDateTime startTime, LocalDateTime endTime) throws SQLException {
        String query = "SELECT COUNT(*) FROM reservation " +
                      "WHERE court_id = ? AND reservation_date = ? " +
                      "AND status != 'cancelled' " +
                      "AND ((start_datetime <= ? AND end_datetime > ?) " +
                      "OR (start_datetime < ? AND end_datetime >= ?))";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, courtId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTimestamp(3, Timestamp.valueOf(endTime));
            stmt.setTimestamp(4, Timestamp.valueOf(startTime));
            stmt.setTimestamp(5, Timestamp.valueOf(endTime));
            stmt.setTimestamp(6, Timestamp.valueOf(startTime));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // Return true if no overlapping reservations found
                }
            }
        }
        return false;
    }

    public boolean createCourt(Court court) throws SQLException {
        String query = "INSERT INTO court (court_type_id, description) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, court.getCourtTypeId());
            stmt.setString(2, court.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        court.setCourtId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean updateCourt(Court court) throws SQLException {
        String query = "UPDATE court SET court_type_id = ?, description = ? WHERE court_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, court.getCourtTypeId());
            stmt.setString(2, court.getDescription());
            stmt.setInt(3, court.getCourtId());
            
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCourt(int courtId) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM reservation WHERE court_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            
            checkStmt.setInt(1, courtId);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }
            
            String deleteQuery = "DELETE FROM court WHERE court_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, courtId);
                return deleteStmt.executeUpdate() > 0;
            }
        }
    }
} 