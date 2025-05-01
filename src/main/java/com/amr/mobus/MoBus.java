/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.amr.mobus;
import gui.NavigationManager;
import gui.AdminDashboard;
import gui.TravelerDashboard;
import javax.swing.*;
import java.awt.*;
/**
 *
 * @author Amr
 */
public class MoBus {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NavigationManager navManager = NavigationManager.getInstance();
            
            JFrame frame = new JFrame("MoBus Reservation System");
            frame.setSize(300, 150);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setLayout(new BorderLayout());

            JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JButton adminButton = new JButton("Admin Dashboard");
            adminButton.addActionListener(e -> {
                AdminDashboard adminDash = new AdminDashboard();
                navManager.navigateTo(adminDash);
            });

            JButton travelerButton = new JButton("Traveler Dashboard");
            travelerButton.addActionListener(e -> {
                TravelerDashboard travelerDash = new TravelerDashboard();
                navManager.navigateTo(travelerDash);
            });

            buttonPanel.add(adminButton);
            buttonPanel.add(travelerButton);
            frame.add(buttonPanel, BorderLayout.CENTER);
            
            JLabel titleLabel = new JLabel("MoBus Reservation System", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            frame.add(titleLabel, BorderLayout.NORTH);

            navManager.navigateTo(frame);
        });
    }
}
