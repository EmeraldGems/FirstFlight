package com.csols.FirstFlight;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dashboard extends JFrame {
    private static final String ADMIN_PASSWORD = "admin123";
    private List<String> flights = new ArrayList<>();
    private Map<String, List<String>> bookings = new HashMap<>(); // username -> list of booked flights
    private String username;
    private JList<String> flightList;
    private DefaultListModel<String> flightListModel;
    private TransportationSystem transportSystem;
    private JTabbedPane tabbedPane;

    public Dashboard(String username) {
        this.username = username;
        transportSystem = new TransportationSystem();
        initializeFlights();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Dashboard - Welcome " + username);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Flight tab
        tabbedPane.addTab("Flights", createFlightPanel());
        
        // Transportation tab
        tabbedPane.addTab("Transportation", createTransportPanel());
        
        // Transaction history tab
        tabbedPane.addTab("Transaction History", createTransactionHistoryPanel());

        add(tabbedPane);
        setVisible(true);
    }

    private JPanel createFlightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Welcome label at top
        JLabel welcomeLabel = new JLabel("Flight Management - Welcome " + username + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(welcomeLabel, BorderLayout.NORTH);

        // Flight list
        flightListModel = new DefaultListModel<>();
        updateFlightListModel();
        flightList = new JList<>(flightListModel);
        flightList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        // Add right-click context menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem bookMenuItem = new JMenuItem("Book Flight");
        JMenuItem unbookMenuItem = new JMenuItem("Cancel Booking");
        
        bookMenuItem.addActionListener(e -> bookSelectedFlight());
        unbookMenuItem.addActionListener(e -> unbookSelectedFlight());
        
        popupMenu.add(bookMenuItem);
        popupMenu.add(unbookMenuItem);
        
        flightList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = flightList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        flightList.setSelectedIndex(index);
                        String flight = flightListModel.getElementAt(index);
                        
                        // Show appropriate menu item based on booking status
                        bookMenuItem.setVisible(!isFlightBookedByUser(flight));
                        unbookMenuItem.setVisible(isFlightBookedByUser(flight));
                        
                        popupMenu.show(flightList, e.getX(), e.getY());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(flightList);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton viewButton = new JButton("Refresh Flights");
        viewButton.addActionListener(e -> updateFlightListModel());
        
        JButton editButton = new JButton("Edit Flight");
        editButton.addActionListener(e -> editFlight());
        
        JButton deleteButton = new JButton("Delete Flight");
        deleteButton.addActionListener(e -> deleteFlight());
        
        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTransportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Title label
        JLabel titleLabel = new JLabel("Transportation Options - Welcome " + username + "!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Transport list
        DefaultListModel<String> transportListModel = new DefaultListModel<>();
        JList<String> transportList = new JList<>(transportListModel);
        updateTransportListModel(transportListModel);
        
        // Right-click menu for transport
        JPopupMenu transportMenu = new JPopupMenu();
        JMenuItem bookTransportItem = new JMenuItem("Book Transport");
        JMenuItem cancelTransportItem = new JMenuItem("Cancel Booking");
        
        bookTransportItem.addActionListener(e -> bookSelectedTransport(transportList, transportListModel));
        cancelTransportItem.addActionListener(e -> cancelSelectedTransport(transportList, transportListModel));
        
        transportMenu.add(bookTransportItem);
        transportMenu.add(cancelTransportItem);
        
        transportList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = transportList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        transportList.setSelectedIndex(index);
                        String transport = transportListModel.getElementAt(index);
                        
                        boolean isBooked = isTransportBookedByUser(transport);
                        bookTransportItem.setVisible(!isBooked);
                        cancelTransportItem.setVisible(isBooked);
                        
                        transportMenu.show(transportList, e.getX(), e.getY());
                    }
                }
            }
        });
        
        panel.add(new JScrollPane(transportList), BorderLayout.CENTER);
        
        // Refresh button
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> updateTransportListModel(transportListModel));
        panel.add(refreshBtn, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createTransactionHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Title label
        JLabel titleLabel = new JLabel("Your Transaction History - Welcome " + username + "!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        DefaultListModel<String> historyModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyModel);
        
        // Load transaction history
        List<String> transactions = UserStorage.getUserTransactions(username);
        transactions.forEach(historyModel::addElement);
        
        panel.add(new JScrollPane(historyList), BorderLayout.CENTER);
        
        // Refresh button
        JButton refreshBtn = new JButton("Refresh History");
        refreshBtn.addActionListener(e -> {
            historyModel.clear();
            UserStorage.getUserTransactions(username).forEach(historyModel::addElement);
        });
        panel.add(refreshBtn, BorderLayout.SOUTH);
        
        return panel;
    }

    private void initializeFlights() {
        // Initialize some sample flights
        flights.add("AA101 | New York | 10:00 AM | 180 seats");
        flights.add("BA202 | London | 2:00 PM | 120 seats");
        flights.add("QF303 | Sydney | 9:30 PM | 200 seats");
        
        // Initialize bookings map
        bookings.putIfAbsent(username, new ArrayList<>());
    }

    private void updateFlightListModel() {
        flightListModel.clear();
        for (String flight : flights) {
            String displayText = flight;
            if (isFlightBookedByUser(flight)) {
                displayText += " (Booked by you)";
            }
            flightListModel.addElement(displayText);
        }
    }

    private boolean isFlightBookedByUser(String flight) {
        String flightId = flight.split("\\|")[0].trim();
        return bookings.getOrDefault(username, new ArrayList<>()).contains(flightId);
    }

    private void bookSelectedFlight() {
        int selectedIndex = flightList.getSelectedIndex();
        if (selectedIndex != -1) {
            String flight = flights.get(selectedIndex);
            String flightId = flight.split("\\|")[0].trim();
            
            if (!bookings.get(username).contains(flightId)) {
                bookings.get(username).add(flightId);
                UserStorage.addUserTransaction(username, "Booked flight: " + flight);
                JOptionPane.showMessageDialog(this, "Flight " + flightId + " booked successfully!", 
                    "Booking Confirmation", JOptionPane.INFORMATION_MESSAGE);
                updateFlightListModel();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a flight first", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void unbookSelectedFlight() {
        int selectedIndex = flightList.getSelectedIndex();
        if (selectedIndex != -1) {
            String flight = flights.get(selectedIndex);
            String flightId = flight.split("\\|")[0].trim();
            
            if (bookings.get(username).contains(flightId)) {
                bookings.get(username).remove(flightId);
                UserStorage.addUserTransaction(username, "Cancelled flight: " + flight);
                JOptionPane.showMessageDialog(this, "Booking for flight " + flightId + " cancelled", 
                    "Cancellation Confirmation", JOptionPane.INFORMATION_MESSAGE);
                updateFlightListModel();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a flight first", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateTransportListModel(DefaultListModel<String> model) {
        model.clear();
        transportSystem.getAvailableTransport().forEach(t -> {
            String display = t.toString();
            if (isTransportBookedByUser(t.getId())) {
                display += " (Booked)";
            }
            model.addElement(display);
        });
    }

    private boolean isTransportBookedByUser(String transportInfo) {
        String transportId = transportInfo.split("\\|")[0].trim();
        return transportSystem.getUserBookings(username).stream()
            .anyMatch(t -> t.getId().equals(transportId));
    }



    private void bookSelectedTransport(JList<String> list, DefaultListModel<String> model) {
        int index = list.getSelectedIndex();
        if (index != -1) {
            String transportInfo = model.getElementAt(index);
            String transportId = transportInfo.split("\\|")[0].trim();
            
            if (!isTransportBookedByUser(transportId)) {
                if (transportSystem.bookTransport(username, transportId)) {
                    UserStorage.addUserTransaction(username, "Booked transport: " + transportInfo);
                    JOptionPane.showMessageDialog(this, "Transport booked successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    updateTransportListModel(model);
                }
            }
        }
    }

    private void cancelSelectedTransport(JList<String> list, DefaultListModel<String> model) {
        int index = list.getSelectedIndex();
        if (index != -1) {
            String transportInfo = model.getElementAt(index);
            String transportId = transportInfo.split("\\|")[0].trim();
            
            if (transportSystem.cancelTransport(username, transportId)) {
                UserStorage.addUserTransaction(username, "Cancelled transport: " + transportInfo);
                JOptionPane.showMessageDialog(this, "Transport booking cancelled", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                updateTransportListModel(model);
            }
        }
    }

    private void editFlight() {
        String input = JOptionPane.showInputDialog(this, "Enter admin password:");
        if (input == null || !input.equals(ADMIN_PASSWORD)) {
            JOptionPane.showMessageDialog(this, "Admin access denied", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String flightIndex = JOptionPane.showInputDialog(this, "Enter flight number to edit (0-" + (flights.size()-1) + "):");
        try {
            int index = Integer.parseInt(flightIndex);
            if (index >= 0 && index < flights.size()) {
                String newInfo = JOptionPane.showInputDialog(this, "Enter new flight info:", flights.get(index));
                if (newInfo != null && !newInfo.trim().isEmpty()) {
                    flights.set(index, newInfo);
                    updateFlightListModel();
                    UserStorage.addUserTransaction(username, "Edited flight: " + newInfo);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid flight number", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteFlight() {
        String input = JOptionPane.showInputDialog(this, "Enter admin password:");
        if (input == null || !input.equals(ADMIN_PASSWORD)) {
            JOptionPane.showMessageDialog(this, "Admin access denied", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String flightIndex = JOptionPane.showInputDialog(this, "Enter flight number to delete (0-" + (flights.size()-1) + "):");
        try {
            int index = Integer.parseInt(flightIndex);
            if (index >= 0 && index < flights.size()) {
                String flightId = flights.get(index).split("\\|")[0].trim();
                String flightInfo = flights.get(index);
                
                // Remove this flight from all users' bookings
                for (List<String> userBookings : bookings.values()) {
                    userBookings.remove(flightId);
                }
                
                flights.remove(index);
                updateFlightListModel();
                UserStorage.addUserTransaction(username, "Deleted flight: " + flightInfo);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid flight number", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}