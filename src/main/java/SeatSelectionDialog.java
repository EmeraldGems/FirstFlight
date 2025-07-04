package com.csols.FirstFlight;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class SeatSelectionDialog extends JDialog {
    private JSpinner seatQuantitySpinner;
    private JButton confirmButton;
    private JButton cancelButton;
    private List<String> selectedSeats = new ArrayList<>();
    private JPanel seatsPanel;
    private int seatsBooked;
    private boolean confirmed = false;

    public SeatSelectionDialog(JFrame parent, String flightId, int totalSeats, int availableSeats) {
        super(parent, "Select Seats for Flight " + flightId, true);
        setSize(800, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Top panel - flight info and quantity selection
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel flightInfoLabel = new JLabel("Flight: " + flightId + " | Available Seats: " + availableSeats + "/" + totalSeats);
        flightInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        topPanel.add(flightInfoLabel);
        
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantityPanel.add(new JLabel("Number of seats:"));
        seatQuantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, availableSeats, 1));
        quantityPanel.add(seatQuantitySpinner);
        topPanel.add(quantityPanel);
        
        add(topPanel, BorderLayout.NORTH);

        // Center panel - seat visualization
        seatsPanel = new JPanel(new GridLayout(0, 6, 5, 5));
        seatsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(seatsPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel - buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        confirmButton = new JButton("Confirm Selection");
        cancelButton = new JButton("Cancel");
        
        confirmButton.addActionListener(e -> {
            seatsBooked = (Integer) seatQuantitySpinner.getValue();
            confirmed = true;
            dispose();
        });
        
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Generate seat buttons
        generateSeatButtons(totalSeats, availableSeats);
        
        // Update seat selection when quantity changes
        seatQuantitySpinner.addChangeListener(e -> updateSeatSelectionUI());
    }

    private void generateSeatButtons(int totalSeats, int availableSeats) {
        seatsPanel.removeAll();
        int bookedSeats = totalSeats - availableSeats;
        
        for (int i = 1; i <= totalSeats; i++) {
            JButton seatButton = new JButton(String.valueOf(i));
            seatButton.setPreferredSize(new Dimension(60, 60));
            seatButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            if (i <= bookedSeats) {
                seatButton.setBackground(Color.RED);
                seatButton.setForeground(Color.WHITE);
                seatButton.setEnabled(false);
                seatButton.setToolTipText("This seat is already booked");
            } else {
                seatButton.setBackground(Color.GREEN);
                seatButton.setForeground(Color.BLACK);
                seatButton.addActionListener(this::toggleSeatSelection);
                seatButton.setToolTipText("Click to select this seat");
            }
            
            seatsPanel.add(seatButton);
        }
        seatsPanel.revalidate();
        seatsPanel.repaint();
    }

    private void toggleSeatSelection(ActionEvent e) {
        JButton seatButton = (JButton) e.getSource();
        String seatNumber = seatButton.getText();
        
        if (selectedSeats.contains(seatNumber)) {
            selectedSeats.remove(seatNumber);
            seatButton.setBackground(Color.GREEN);
        } else {
            if (selectedSeats.size() < (Integer) seatQuantitySpinner.getValue()) {
                selectedSeats.add(seatNumber);
                seatButton.setBackground(Color.YELLOW);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "You can only select " + seatQuantitySpinner.getValue() + " seats", 
                    "Selection Limit", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void updateSeatSelectionUI() {
        int maxSelection = (Integer) seatQuantitySpinner.getValue();
        if (selectedSeats.size() > maxSelection) {
            // Deselect excess seats
            while (selectedSeats.size() > maxSelection) {
                String seatNumber = selectedSeats.remove(selectedSeats.size() - 1);
                for (Component comp : seatsPanel.getComponents()) {
                    if (comp instanceof JButton) {
                        JButton button = (JButton) comp;
                        if (button.getText().equals(seatNumber)) {
                            button.setBackground(Color.GREEN);
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getSeatsBooked() {
        return seatsBooked;
    }

    public String getSelectedSeats() {
        return String.join(", ", selectedSeats);
    }
}