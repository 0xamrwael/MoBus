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
import java.util.List;
/**
 *
 * @author Khaled
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
}