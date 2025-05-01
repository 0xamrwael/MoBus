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
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author Amr
 */
public class TravelerDashboard extends JFrame {
    private BusManager busManager = new BusManager();
    private JTextField phoneField, nameField;
    private JComboBox<String> busComboBox;
    private JPanel seatPanel;
    private int travelerId = -1;
    private Map<String, Integer> busNameToIdMap = new HashMap<>();

    public TravelerDashboard() {
        setTitle("Traveler Dashboard - MoBus");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        //back button
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("← Back");
        backButton.addActionListener(e -> NavigationManager.getInstance().goBack());
        topPanel.add(backButton, BorderLayout.WEST);
        
        add(topPanel, BorderLayout.NORTH);
        
        //main content panel for fixing the layout
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        
        //login panel
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        loginPanel.add(new JLabel("Phone Number:"));
        phoneField = new JTextField();
        loginPanel.add(phoneField);
        loginPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        loginPanel.add(nameField);
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(this::login);
        loginPanel.add(loginButton);

        //reservation
        JPanel reservePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        reservePanel.setBorder(BorderFactory.createTitledBorder("Bus Selection"));
        reservePanel.add(new JLabel("Select Bus:"));
        
        //bus dropdown
        busComboBox = new JComboBox<>();
        busComboBox.setPreferredSize(new Dimension(200, 25));
        reservePanel.add(busComboBox);
        
        JButton viewBusButton = new JButton("View Bus");
        viewBusButton.addActionListener(this::viewBus);
        reservePanel.add(viewBusButton);
        
        //reload
        JButton refreshButton = new JButton("Refresh Bus List");
        refreshButton.addActionListener(e -> loadBusList());
        reservePanel.add(refreshButton);

        //seats and title
        JPanel seatDisplayPanel = new JPanel(new BorderLayout());
        seatDisplayPanel.setBorder(BorderFactory.createTitledBorder("Seat Arrangement"));
        
        //ribbon
        JPanel legendPanel = createColorLegendPanel();
        seatDisplayPanel.add(legendPanel, BorderLayout.NORTH);
        
        //seat panel with scroll
        seatPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(seatPanel);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        seatDisplayPanel.add(scrollPane, BorderLayout.CENTER);

        //add to main
        mainContentPanel.add(loginPanel, BorderLayout.NORTH);
        
        //center panel for reserving and seats info
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(reservePanel, BorderLayout.NORTH);
        centerPanel.add(seatDisplayPanel, BorderLayout.CENTER);
        
        mainContentPanel.add(centerPanel, BorderLayout.CENTER);
        
        //add to main
        add(mainContentPanel, BorderLayout.CENTER);
        
        //load
        loadBusList();
    }
    
    private JPanel createColorLegendPanel() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        legendPanel.setBorder(BorderFactory.createTitledBorder("Color Legend"));
        
        Map<String, Color> colorScheme = busManager.getSeatColorScheme();
        String[] descriptions = busManager.getSeatStatusDescriptions();
        
        String[] statuses = {"AVAILABLE", "RESERVED", "RESERVED_NOT_PAID", "UNAVAILABLE"};
        
        for (int i = 0; i < statuses.length; i++) {
            JPanel colorBox = new JPanel();
            colorBox.setPreferredSize(new Dimension(20, 20));
            colorBox.setBackground(colorScheme.get(statuses[i]));
            colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            
            JLabel descLabel = new JLabel(descriptions[i]);
            
            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            itemPanel.add(colorBox);
            itemPanel.add(descLabel);
            
            legendPanel.add(itemPanel);
        }
        
        return legendPanel;
    }
    
    private void loadBusList() {
        busComboBox.removeAllItems();
        busNameToIdMap.clear();
        
        try {
            busNameToIdMap = busManager.loadBusList();
            
            busComboBox.addItem("-- Select a Bus --");
            
            for (String displayName : busNameToIdMap.keySet()) {
                busComboBox.addItem(displayName);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading buses: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void login(ActionEvent e) {
        String phone = phoneField.getText().trim();
        String name = nameField.getText().trim();
        
        //no empty inputs
        if (phone.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Phone number and name cannot be empty!", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
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
        
        String selectedBus = (String) busComboBox.getSelectedItem();
        
        //no empty selection
        if (selectedBus == null || selectedBus.equals("-- Select a Bus --")) {
            JOptionPane.showMessageDialog(this, 
                "Please select a bus from the dropdown!", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        //map to id
        Integer busId = busNameToIdMap.get(selectedBus);
        
        if (busId == null) {
            JOptionPane.showMessageDialog(this, 
                "Invalid bus selection!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            List<String[]> seats = busManager.getBusSeats(busId);
            seatPanel.removeAll();
            
            //max row and col
            int maxRow = 0, maxCol = 0;
            for (String[] seat : seats) {
                maxRow = Math.max(maxRow, Integer.parseInt(seat[0]));
                maxCol = Math.max(maxCol, Integer.parseInt(seat[1]));
            }
            
            //-1 because we count from 0
            seatPanel.setLayout(new GridLayout(maxRow + 1, maxCol + 1));
            
            //2d array 
            JButton[][] seatButtons = new JButton[maxRow + 1][maxCol + 1];
            
            //all empty at first
            for (int r = 0; r <= maxRow; r++) {
                for (int c = 0; c <= maxCol; c++) {
                    JPanel emptyPanel = new JPanel();
                    seatPanel.add(emptyPanel);
                }
            }
            
            //reload
            seatPanel.removeAll();
            
            //set
            seatPanel.setLayout(new GridLayout(maxRow + 1, maxCol + 1));
            
            //add buttons
            for (int r = 0; r <= maxRow; r++) {
                for (int c = 0; c <= maxCol; c++) {
                    //start at first
                    if (r == 0 || c == 0) {
                        seatPanel.add(new JPanel());
                        continue;
                    }
                    
                    //get seat data
                    //unavailable by default
                    String status = "UNAVAILABLE";
                    for (String[] seat : seats) {
                        int seatRow = Integer.parseInt(seat[0]);
                        int seatCol = Integer.parseInt(seat[1]);
                        if (seatRow == r && seatCol == c) {
                            status = seat[2];
                            break;
                        }
                    }
                    
                    JButton seatButton = new JButton();
                    seatButton.setPreferredSize(new Dimension(30, 30));
                    seatButton.setText(r + "," + c);
                    seatButton.setFont(new Font("Arial", Font.PLAIN, 9));
                    
                    final int row = r;
                    final int col = c;
                    
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
                    
                    seatButtons[r][c] = seatButton;
                    seatPanel.add(seatButton);
                }
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