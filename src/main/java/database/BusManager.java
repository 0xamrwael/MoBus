/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.*;
/**
 *
 * @author Amr
 */
public class BusManager {
    public int addBus(String busName, int rows, int columns, List<int[]> unavailableSeats) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String insertBus = "INSERT INTO buses (bus_name, _rows, _columns) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertBus, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, busName);
            stmt.setInt(2, rows);
            stmt.setInt(3, columns);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            int busId = rs.next() ? rs.getInt(1) : -1;

            if (busId != -1) {
                String insertSeat = "INSERT INTO seats (bus_id, seat_row, seat_column, status) VALUES (?, ?, ?, ?)";
                PreparedStatement seatStmt = conn.prepareStatement(insertSeat);
                for (int r = 1; r <= rows; r++) {
                    for (int c = 1; c <= columns; c++) {
                        boolean isUnavailable = false;
                        for (int[] seat : unavailableSeats) {
                            if (seat[0] == r && seat[1] == c) {
                                isUnavailable = true;
                                break;
                            }
                        }
                        seatStmt.setInt(1, busId);
                        seatStmt.setInt(2, r);
                        seatStmt.setInt(3, c);
                        seatStmt.setString(4, isUnavailable ? "UNAVAILABLE" : "AVAILABLE");
                        seatStmt.addBatch();
                    }
                }
                seatStmt.executeBatch();
            }
            return busId;
        }
    }

    public List<String[]> getBusSeats(int busId) throws SQLException {
        List<String[]> seats = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT seat_row, seat_column, status FROM seats WHERE bus_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, busId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                seats.add(new String[]{String.valueOf(rs.getInt("seat_row")), String.valueOf(rs.getInt("seat_column")), rs.getString("status")});
            }
        }
        return seats;
    }
    
    public Map<String, Integer> loadBusList() throws SQLException {
        Map<String, Integer> busNameToIdMap = new HashMap<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT bus_id, bus_name FROM buses";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int busId = rs.getInt("bus_id");
                String busName = rs.getString("bus_name");
                String displayName = busName + " (ID: " + busId + ")";
                
                busNameToIdMap.put(displayName, busId);
            }
        }
        return busNameToIdMap;
    }
    
    public List<String[]> getBusSeatsWithTravelerInfo(int busId) throws SQLException {
        List<String[]> seats = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT s.seat_id, s.seat_row, s.seat_column, s.status, " +
                          "t.traveler_id, t.name, t.phone_number " +
                          "FROM seats s " +
                          "LEFT JOIN reservations r ON s.seat_id = r.seat_id " +
                          "LEFT JOIN travelers t ON r.traveler_id = t.traveler_id " +
                          "WHERE s.bus_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, busId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String[] seatInfo = new String[7];
                seatInfo[0] = String.valueOf(rs.getInt("seat_row"));
                seatInfo[1] = String.valueOf(rs.getInt("seat_column"));
                seatInfo[2] = rs.getString("status");
                seatInfo[3] = rs.getString("seat_id");
                
                // Traveler info might be null if seat is not reserved
                seatInfo[4] = rs.getString("traveler_id"); // might be null
                seatInfo[5] = rs.getString("name"); // might be null
                seatInfo[6] = rs.getString("phone_number"); // might be null
                
                seats.add(seatInfo);
            }
        }
        return seats;
    }
    
    public boolean markSeatAsPaid(int seatId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String updateSeat = "UPDATE seats SET status = 'RESERVED' WHERE seat_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSeat);
            updateStmt.setInt(1, seatId);
            int rowsAffected = updateStmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    public boolean markSeatAsUnpaid(int seatId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String updateSeat = "UPDATE seats SET status = 'RESERVED_NOT_PAID' WHERE seat_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSeat);
            updateStmt.setInt(1, seatId);
            int rowsAffected = updateStmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    public Map<String, Color> getSeatColorScheme() {
        Map<String, Color> colorScheme = new HashMap<>();
        colorScheme.put("AVAILABLE", Color.WHITE);
        colorScheme.put("RESERVED", Color.GREEN);
        colorScheme.put("RESERVED_NOT_PAID", Color.GRAY);
        colorScheme.put("UNAVAILABLE", Color.RED);
        return colorScheme;
    }
    
    public String[] getSeatStatusDescriptions() {
        return new String[] {
            "Available", 
            "Reserved & Paid", 
            "Reserved (Not Paid)", 
            "Unavailable"
        };
    }
}