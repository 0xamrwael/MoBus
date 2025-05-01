/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;
import database.BusManager;
import database.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.List;
/**
 *
 * @author Khaled
 */
public class TravelerDashboard extends JFrame {
    private BusManager busManager = new BusManager();
    private JTextField phoneField, nameField, busIdField;
    private JPanel seatPanel;
    private int travelerId = -1;

    public TravelerDashboard() {
        setTitle("Traveler Dashboard - MoBus");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Login Panel
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        loginPanel.add(new JLabel("Phone Number:"));
        phoneField = new JTextField();
        loginPanel.add(phoneField);
        loginPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        loginPanel.add(nameField);
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this::login);
        loginPanel.add(loginButton);

        // Reservation Panel
        JPanel reservePanel = new JPanel();
        reservePanel.add(new JLabel("Bus ID:"));
        busIdField = new JTextField(10);
        reservePanel.add(busIdField);
        JButton viewBusButton = new JButton("View Bus");
        viewBusButton.addActionListener(this::viewBus);
        reservePanel.add(viewBusButton);

        seatPanel = new JPanel();
        seatPanel.setPreferredSize(new Dimension(400, 400));

        add(loginPanel, BorderLayout.NORTH);
        add(reservePanel, BorderLayout.CENTER);
        add(seatPanel, BorderLayout.SOUTH);
    }

    private void login(ActionEvent e) {
        String phone = phoneField.getText();
        String name = nameField.getText();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT traveler_id FROM travelers WHERE phone_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                travelerId = rs.getInt("traveler_id");
                JOptionPane.showMessageDialog(this, "Logged in successfully!");
            } else {
                String insert = "INSERT INTO travelers (phone_number, name) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS);
                insertStmt.setString(1, phone);
                insertStmt.setString(2, name);
                insertStmt.executeUpdate();
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    travelerId = generatedKeys.getInt(1);
                    JOptionPane.showMessageDialog(this, "Registered and logged in successfully!");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void viewBus(ActionEvent e) {
        if (travelerId == -1) {
            JOptionPane.showMessageDialog(this, "Please login first!");
            return;
        }
        try {
            int busId = Integer.parseInt(busIdField.getText());
            List<String[]> seats = busManager.getBusSeats(busId);
            seatPanel.removeAll();
            seatPanel.setLayout(new GridLayout(0, 0));
            int maxRow = 0, maxCol = 0;
            for (String[] seat : seats) {
                maxRow = Math.max(maxRow, Integer.parseInt(seat[0]));
                maxCol = Math.max(maxCol, Integer.parseInt(seat[1]));
            }
            seatPanel.setLayout(new GridLayout(maxRow, maxCol));
            JButton[][] seatButtons = new JButton[maxRow + 1][maxCol + 1];
            for (String[] seat : seats) {
                int row = Integer.parseInt(seat[0]);
                int col = Integer.parseInt(seat[1]);
                String status = seat[2];
                JButton seatButton = new JButton();
                seatButton.setPreferredSize(new Dimension(30, 30));
                switch (status) {
                    case "AVAILABLE":
                        seatButton.setBackground(Color.WHITE);
                        seatButton.addActionListener(evt -> reserveSeat(busId, row, col));
                        break;
                    case "RESERVED":
                        seatButton.setBackground(Color.GREEN);
                        break;
                    case "RESERVED_NOT_PAID":
                        seatButton.setBackground(Color.GRAY);
                        seatButton.addActionListener(evt -> cancelReservation(busId, row, col));
                        break;
                    case "UNAVAILABLE":
                        seatButton.setBackground(Color.RED);
                        break;
                }
                seatButtons[row][col] = seatButton;
                seatPanel.add(seatButton);
            }
            seatPanel.revalidate();
            seatPanel.repaint();
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void reserveSeat(int busId, int row, int col) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String selectSeat = "SELECT seat_id FROM seats WHERE bus_id = ? AND seat_row = ? AND seat_column = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSeat);
            selectStmt.setInt(1, busId);
            selectStmt.setInt(2, row);
            selectStmt.setInt(3, col);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                int seatId = rs.getInt("seat_id");
                String updateSeat = "UPDATE seats SET status = 'RESERVED_NOT_PAID' WHERE seat_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSeat);
                updateStmt.setInt(1, seatId);
                updateStmt.executeUpdate();

                String insertReservation = "INSERT INTO reservations (bus_id, seat_id, traveler_id) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertReservation);
                insertStmt.setInt(1, busId);
                insertStmt.setInt(2, seatId);
                insertStmt.setInt(3, travelerId);
                insertStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Seat reserved!");
                viewBus(null); // Refresh the view
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void cancelReservation(int busId, int row, int col) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String selectSeat = "SELECT seat_id FROM seats WHERE bus_id = ? AND seat_row = ? AND seat_column = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSeat);
            selectStmt.setInt(1, busId);
            selectStmt.setInt(2, row);
            selectStmt.setInt(3, col);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                int seatId = rs.getInt("seat_id");
                String deleteReservation = "DELETE FROM reservations WHERE seat_id = ? AND traveler_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteReservation);
                deleteStmt.setInt(1, seatId);
                deleteStmt.setInt(2, travelerId);
                deleteStmt.executeUpdate();

                String updateSeat = "UPDATE seats SET status = 'AVAILABLE' WHERE seat_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSeat);
                updateStmt.setInt(1, seatId);
                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Reservation canceled!");
                viewBus(null); // Refresh the view
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}