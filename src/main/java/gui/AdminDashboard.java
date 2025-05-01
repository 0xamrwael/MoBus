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
/**
 *
 * @author Amr
 */
public class AdminDashboard extends JFrame {
    private BusManager busManager = new BusManager();
    private JTextField busNameField, rowsField, columnsField;
    private JTextArea unavailableSeatsArea;
    private JTextField busIdField;
    private JPanel seatPanel;

    public AdminDashboard() {
        setTitle("Admin Dashboard - MoBus");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Add Bus Panel
        JPanel addBusPanel = new JPanel(new GridLayout(5, 2));
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

        // View Seats Panel
        JPanel viewPanel = new JPanel();
        viewPanel.add(new JLabel("Bus ID:"));
        busIdField = new JTextField(10);
        viewPanel.add(busIdField);
        JButton viewSeatsButton = new JButton("View Seats");
        viewSeatsButton.addActionListener(this::viewSeats);
        viewPanel.add(viewSeatsButton);

        seatPanel = new JPanel();
        seatPanel.setPreferredSize(new Dimension(400, 400));

        add(addBusPanel, BorderLayout.NORTH);
        add(viewPanel, BorderLayout.CENTER);
        add(seatPanel, BorderLayout.SOUTH);
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

    private void viewSeats(ActionEvent e) {
        try {
            int busId = Integer.parseInt(busIdField.getText());
            List<String[]> seats = busManager.getBusSeats(busId);
            seatPanel.removeAll();
            seatPanel.setLayout(new GridLayout(0, 0)); // Will be set dynamically
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
                        break;
                    case "RESERVED":
                        seatButton.setBackground(Color.GREEN);
                        break;
                    case "RESERVED_NOT_PAID":
                        seatButton.setBackground(Color.GRAY);
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
}