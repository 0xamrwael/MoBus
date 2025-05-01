/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;
import database.BusManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
/**
 *
 * @author Amr
 */
public class AdminDashboard extends JFrame {
    private BusManager busManager = new BusManager();
    private JTextField busNameField, rowsField, columnsField;
    private JTextArea unavailableSeatsArea;
    private JComboBox<String> busComboBox;
    private JPanel seatPanel;
    private JPanel travelerInfoPanel;
    private Map<String, Integer> busNameToIdMap = new HashMap<>();

    public AdminDashboard() {
        setTitle("Admin Dashboard - MoBus");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    
        //main panel
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        
        //back button
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("← Back");
        backButton.addActionListener(e -> NavigationManager.getInstance().goBack());
        topPanel.add(backButton, BorderLayout.WEST);
        
        //add to top
        add(topPanel, BorderLayout.NORTH);
        
        //add bus panel
        JPanel addBusPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        addBusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        addBusPanel.add(new JLabel("Bus Name:"));
        busNameField = new JTextField();
        addBusPanel.add(busNameField);
        addBusPanel.add(new JLabel("Rows:"));
        rowsField = new JTextField();
        addBusPanel.add(rowsField);
        addBusPanel.add(new JLabel("Columns:"));
        columnsField = new JTextField();
        addBusPanel.add(columnsField);
        addBusPanel.add(new JLabel("Unavailable Seats (row,col;row,col):"));
        unavailableSeatsArea = new JTextArea(2, 20);
        addBusPanel.add(unavailableSeatsArea);
        JButton addBusButton = new JButton("Add Bus");
        addBusButton.addActionListener(this::addBus);
        addBusPanel.add(addBusButton);

        //view seats drop down
        JPanel viewPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        viewPanel.setBorder(BorderFactory.createTitledBorder("Bus Selection"));
        viewPanel.add(new JLabel("Select Bus:"));
        
        //bus dropdown
        busComboBox = new JComboBox<>();
        busComboBox.setPreferredSize(new Dimension(200, 25));
        viewPanel.add(busComboBox);
        
        JButton viewSeatsButton = new JButton("View Bus");
        viewSeatsButton.addActionListener(this::viewSeats);
        viewPanel.add(viewSeatsButton);
        
        //refresh
        JButton refreshButton = new JButton("Refresh Bus List");
        refreshButton.addActionListener(e -> loadBusList());
        viewPanel.add(refreshButton);
        
        //color legend
        JPanel legendPanel = createColorLegendPanel();
        viewPanel.add(legendPanel);

        //split for seat display and traveler info
        //70% seat panel,30% traveler info
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7);
        
        //seat scroll pane
        seatPanel = new JPanel();
        JScrollPane seatScrollPane = new JScrollPane(seatPanel);
        seatScrollPane.setPreferredSize(new Dimension(600, 300));
        seatScrollPane.setBorder(BorderFactory.createTitledBorder("Seat Arrangement"));
        splitPane.setLeftComponent(seatScrollPane);
        
        //traveler info
        travelerInfoPanel = new JPanel(new BorderLayout());
        travelerInfoPanel.setBorder(BorderFactory.createTitledBorder("Traveler Information"));
        JLabel noSelectionLabel = new JLabel("Select a seat to view traveler information", SwingConstants.CENTER);
        travelerInfoPanel.add(noSelectionLabel, BorderLayout.CENTER);
        splitPane.setRightComponent(travelerInfoPanel);

        //add to main
        mainContentPanel.add(addBusPanel, BorderLayout.NORTH);
        mainContentPanel.add(viewPanel, BorderLayout.CENTER);
        mainContentPanel.add(splitPane, BorderLayout.SOUTH);
        
        //add main to the frame
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

    private void addBus(ActionEvent e) {
        try {
            String busName = busNameField.getText();
            int rows = Integer.parseInt(rowsField.getText());
            int columns = Integer.parseInt(columnsField.getText());
            List<int[]> unavailableSeats = new ArrayList<>();
            String[] seatPairs = unavailableSeatsArea.getText().split(";");
            for (String pair : seatPairs) {
                String[] coords = pair.split(",");
                if (coords.length == 2) {
                    unavailableSeats.add(new int[]{Integer.parseInt(coords[0]), Integer.parseInt(coords[1])});
                }
            }
            int busId = busManager.addBus(busName, rows, columns, unavailableSeats);
            JOptionPane.showMessageDialog(this, "Bus added with ID: " + busId);
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
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

    private void viewSeats(ActionEvent e) {
        try {
            String selectedBus = (String) busComboBox.getSelectedItem();
            
            //if selected bus
            if (selectedBus == null || selectedBus.equals("-- Select a Bus --")) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a bus from the dropdown!", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            //bus id from map
            Integer busId = busNameToIdMap.get(selectedBus);
            
            if (busId == null) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid bus selection!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            List<String[]> seats = busManager.getBusSeatsWithTravelerInfo(busId);
            seatPanel.removeAll();
            
            //max row and col
            int maxRow = 0, maxCol = 0;
            for (String[] seat : seats) {
                maxRow = Math.max(maxRow, Integer.parseInt(seat[0]));
                maxCol = Math.max(maxCol, Integer.parseInt(seat[1]));
            }
            
            seatPanel.setLayout(new GridLayout(maxRow + 1, maxCol + 1));
            
            //empty panels for array
            for (int r = 0; r <= maxRow; r++) {
                for (int c = 0; c <= maxCol; c++) {
                    JPanel emptyPanel = new JPanel();
                    seatPanel.add(emptyPanel);
                }
            }
            
            //refresh
            seatPanel.removeAll();
            
            //fix grid size
            seatPanel.setLayout(new GridLayout(maxRow + 1, maxCol + 1));
            
            //add seat buttons
            for (int r = 0; r <= maxRow; r++) {
                for (int c = 0; c <= maxCol; c++) {
                    //add in 0 row or 0 col 
                    if (r == 0 || c == 0) {
                        seatPanel.add(new JPanel());
                        continue;
                    }
                    
                    //get seat info
                    String[] seatInfo = null;
                    for (String[] seat : seats) {
                        int seatRow = Integer.parseInt(seat[0]);
                        int seatCol = Integer.parseInt(seat[1]);
                        if (seatRow == r && seatCol == c) {
                            seatInfo = seat;
                            break;
                        }
                    }
                    
                    //empty if no seat
                    if (seatInfo == null) {
                        seatPanel.add(new JPanel());
                        continue;
                    }
                    
                    String status = seatInfo[2];
                    String seatId = seatInfo[3];
                    String travelerId = seatInfo[4];
                    String travelerName = seatInfo[5];
                    String travelerPhone = seatInfo[6];
                    
                    JButton seatButton = new JButton();
                    seatButton.setPreferredSize(new Dimension(40, 40)); // Increased button size
                    seatButton.setText(r + "," + c);
                    seatButton.setFont(new Font("Arial", Font.PLAIN, 11)); // Increased font size
                    
                    Map<String, Color> colorScheme = busManager.getSeatColorScheme();
                    seatButton.setBackground(colorScheme.get(status));
                    
                    //add action to buttons
                    if (travelerId != null) {
                        final String finalSeatId = seatId;
                        final String finalTravelerId = travelerId;
                        final String finalTravelerName = travelerName;
                        final String finalTravelerPhone = travelerPhone;
                        final String finalStatus = status;
                        
                        seatButton.addActionListener(evt -> {
                            showTravelerInfo(finalSeatId, finalTravelerId, finalTravelerName, 
                                            finalTravelerPhone, finalStatus, busId);
                        });
                    }
                    
                    seatPanel.add(seatButton);
                }
            }
            
            seatPanel.revalidate();
            seatPanel.repaint();
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    private void showTravelerInfo(String seatId, String travelerId, String travelerName, 
                                 String travelerPhone, String status, int busId) {
        travelerInfoPanel.removeAll();
        
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        infoPanel.add(new JLabel("Seat ID: " + seatId));
        infoPanel.add(new JLabel("Traveler ID: " + travelerId));
        infoPanel.add(new JLabel("Name: " + travelerName));
        infoPanel.add(new JLabel("Phone: " + travelerPhone));
        infoPanel.add(new JLabel("Status: " + status));
        
        //payment
        if ("RESERVED_NOT_PAID".equals(status)) {
            JButton paymentButton = new JButton("Mark as Paid");
            paymentButton.addActionListener(e -> {
                try {
                    boolean success = busManager.markSeatAsPaid(Integer.parseInt(seatId));
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Payment processed successfully!");
                        // Refresh the view
                        viewSeats(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "refresh"));
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to process payment.");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            });
            infoPanel.add(paymentButton);
        } else if ("RESERVED".equals(status)) {
            JButton unpaidButton = new JButton("Mark as Unpaid");
            unpaidButton.addActionListener(e -> {
                try {
                    boolean success = busManager.markSeatAsUnpaid(Integer.parseInt(seatId));
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Seat marked as unpaid successfully!");
                        // Refresh the view
                        viewSeats(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "refresh"));
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to mark seat as unpaid.");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            });
            infoPanel.add(unpaidButton);
        }
        
        travelerInfoPanel.add(infoPanel, BorderLayout.NORTH);
        travelerInfoPanel.revalidate();
        travelerInfoPanel.repaint();
    }
}