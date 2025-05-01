/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.amr.mobus;
import gui.AdminDashboard;
import gui.TravelerDashboard;
import javax.swing.*;
/**
 *
 * @author Amr
 */
public class MoBus {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MoBus Reservation System");
            frame.setSize(300, 150);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(3);
            frame.setResizable(false);
            frame.setLayout(new java.awt.FlowLayout());

            JButton adminButton = new JButton("Admin Dashboard");
            adminButton.addActionListener(e -> {
                new AdminDashboard().setVisible(true);
                frame.dispose();
            });

            JButton travelerButton = new JButton("Traveler Dashboard");
            travelerButton.addActionListener(e -> {
                new TravelerDashboard().setVisible(true);
                frame.dispose();
            });

            frame.add(adminButton);
            frame.add(travelerButton);
            frame.setVisible(true);
        });
    }
}
