package com.csols.FirstFlight;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Dashboard extends JFrame {
    // Constants
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String DATA_FILE = "user_data.ser";
    private static final String FLIGHT_BOOKING_SERVICE = "Flight Booking";
    private static final String RECEIPTS_KEY = "receipts";
    private static final String BOOKINGS_KEY = "bookings";
    private static final String SUPPORT_EMAIL = "support@firstflight.com";
    private static final String SUPPORT_PHONE = "1-800-FLY-NOW";
    private static final String BG_MUSIC_FILE = "/fazbear.wav"; 
    private final MusicPlayer musicPlayer = new MusicPlayer();
    
    // UI Colors
    private final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private final Color SECONDARY_COLOR = new Color(240, 240, 245);
    private final Color ACCENT_COLOR = new Color(70, 160, 70);
    private final Color CANCEL_COLOR = new Color(220, 80, 80);
    
    // Components
    private JTabbedPane tabbedPane;
    private JPanel flightCardsPanel;
    private DefaultTableModel transactionModel;
    private DefaultListModel<String> receiptListModel;
    
    // Data
    private final String username;
    private final ReceiptManager receiptManager;
    private final Map<String, List<String>> bookings;
    private final List<String> flights;

    public Dashboard(String username) {
        this.username = username;
        this.receiptManager = new ReceiptManager();
        this.bookings = new HashMap<>();
        this.flights = new ArrayList<>();
        
        initializeData();
        setupUI();
        setupWindowListeners();
        setVisible(true);
        musicPlayer.playMusic(BG_MUSIC_FILE);
    }

    private void initializeData() {
    initializeFlights();      // First create the flight list
    initializeFlightSeats();  // Then initialize seat counts
    loadUserData();          // Finally load any saved data
}

    private void initializeFlights() {
        flights.clear();
        flights.add("AA101 | New York | 10:00 AM - 11:45 AM | 180 seats");
        flights.add("BA202 | London | 2:00 PM - 6:30 PM | 220 seats");
        flights.add("QF303 | Sydney | 9:00 PM - 5:00 AM | 250 seats");
        System.out.println("Initialized " + flights.size() + " sample flights");
    }

    private void initializeFlightSeats() {
        System.out.println("Initializing flight seats...");
        for (String flight : flights) {
            try {
                String[] parts = flight.split("\\|");
                String flightId = parts[0].trim();
                String seatsStr = parts[3].trim();
                int totalSeats = Integer.parseInt(seatsStr.replaceAll("[^0-9]", ""));

                // Only initialize if not already initialized
                if (receiptManager.getTotalSeats(flightId) == 0) {
                    System.out.println("DEBUG - Initializing flight " + flightId + 
                                     " with " + totalSeats + " seats");
                    receiptManager.initializeFlight(flightId, totalSeats);
                } else {
                    System.out.println("DEBUG - Flight " + flightId + 
                                     " already initialized with " + 
                                     receiptManager.getTotalSeats(flightId) + " seats");
                }
            } catch (Exception e) {
                System.err.println("ERROR initializing flight: " + flight + " - " + e.getMessage());
            }
        }
    }

    private void setupUI() {
        configureWindow();
        setLookAndFeel();
        createMainPanel();
    }

    private void configureWindow() {
        setTitle("FirstFlight Dashboard - " + username);
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("TabbedPane.selected", Color.WHITE);
            UIManager.put("TabbedPane.tabAreaBackground", SECONDARY_COLOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createMainPanel() {
        JPanel mainPanel = new GradientPanel(SECONDARY_COLOR, Color.WHITE);
        mainPanel.setLayout(new BorderLayout());
        
        tabbedPane = createTabbedPane();
        mainPanel.add(createSidebar(), BorderLayout.WEST);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane pane = new JTabbedPane();
        pane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pane.setBackground(SECONDARY_COLOR);
        pane.setForeground(PRIMARY_COLOR);
        
        pane.addTab("Flights", createFlightPanel());
        pane.addTab("Transportation", createTransportPanel());
        pane.addTab("History", createTransactionHistoryPanel());
        pane.addTab("Receipts", createReceiptPanel());
        
        return pane;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        
        sidebar.add(createLogo());
        sidebar.add(createProfileSection());
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(createSidebarButtons());
        sidebar.add(Box.createVerticalGlue());
        
        return sidebar;
    }

    private JComponent createLogo() {
        JLabel logoLabel = new JLabel(loadLogo());
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        return logoLabel;
    }

    private ImageIcon loadLogo() {
        try {
            Image logoImage = new ImageIcon(getClass().getResource("/FirstFlight_logo.png")).getImage();
            Image scaledLogo = logoImage.getScaledInstance(160, -1, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledLogo);
        } catch (Exception e) {
            BufferedImage placeholder = new BufferedImage(160, 50, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = placeholder.createGraphics();
            g2d.setColor(PRIMARY_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString("FirstFlight", 10, 25);
            g2d.dispose();
            return new ImageIcon(placeholder);
        }
    }

    private JComponent createProfileSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        
        panel.add(createProfileIcon());
        panel.add(createUsernameLabel());
        panel.add(createMemberLabel());
        
        return panel;
    }

    private JComponent createProfileIcon() {
        JLabel icon = new JLabel(new ImageIcon(createRoundIcon(PRIMARY_COLOR, 70)));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return icon;
    }

    private Image createRoundIcon(Color color, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillOval(0, 0, size, size);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        
        FontMetrics fm = g2d.getFontMetrics();
        String initial = username.substring(0, 1).toUpperCase();
        int x = (size - fm.stringWidth(initial)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        
        g2d.drawString(initial, x, y);
        g2d.dispose();
        
        return image;
    }

    private JComponent createUsernameLabel() {
        JLabel label = new JLabel(username);
        label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        label.setForeground(PRIMARY_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JComponent createMemberLabel() {
        JLabel label = new JLabel("Gold Member");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(100, 100, 100));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        return label;
    }

    private JComponent createSidebarButtons() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        panel.add(createSidebarButton("Contact Support", e -> showSupportDialog()));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createSidebarButton("Settings", e -> showSettingsDialog()));
        
        return panel;
    }

    private JButton createSidebarButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.BLACK);
        button.setBackground(new Color(230, 240, 250));
        button.setBorder(createSidebarButtonBorder());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        addSidebarButtonHoverEffect(button);
        return button;
    }

    private Border createSidebarButtonBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 0, new Color(200, 220, 240)),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        );
    }

    private void addSidebarButtonHoverEffect(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(240, 245, 250));
                button.setForeground(Color.BLACK);
            }
            
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(230, 240, 250));
                button.setForeground(Color.BLACK);
            }
        });
    }

    private JPanel createFlightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panel.add(createFlightHeader(), BorderLayout.NORTH);
        panel.add(createFlightCardsScrollPane(), BorderLayout.CENTER);
        panel.add(createFlightActionButtons(), BorderLayout.SOUTH);
        
        return panel;
    }

    private JComponent createFlightHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel title = new JLabel("Flight Management");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        title.setForeground(PRIMARY_COLOR);
        header.add(title, BorderLayout.WEST);
        header.add(createHelpButton(), BorderLayout.EAST);
        
        return header;
    }

    private JButton createHelpButton() {
        JButton button = new JButton("Help");
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setForeground(PRIMARY_COLOR);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.addActionListener(e -> showHelpDialog());
        return button;
    }

    private JComponent createFlightCardsScrollPane() {
        flightCardsPanel = new JPanel();
        flightCardsPanel.setLayout(new BoxLayout(flightCardsPanel, BoxLayout.Y_AXIS));
        flightCardsPanel.setBackground(Color.WHITE);
        renderFlightCards();
        
        JScrollPane scrollPane = new JScrollPane(flightCardsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private void renderFlightCards() {
        flightCardsPanel.removeAll();
        
        for (String flight : flights) {
            FlightDetails details = parseFlightDetails(flight);
            if (details != null) {
                JPanel card = createFlightCard(details);
                flightCardsPanel.add(card);
                flightCardsPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        flightCardsPanel.revalidate();
        flightCardsPanel.repaint();
    }

    private FlightDetails parseFlightDetails(String flight) {
        try {
            String[] parts = flight.split("\\|");
            String flightId = parts[0].trim();

            // Make sure this matches your flight string format exactly
            String seatsStr = parts[3].trim(); // "180 seats"
            int totalSeats = Integer.parseInt(seatsStr.replaceAll("[^0-9]", "")); // Extract just the number

            int availableSeats = receiptManager.getAvailableSeats(flightId);

            System.out.println("DEBUG - Flight: " + flightId + 
                             " | Total: " + totalSeats + 
                             " | Available: " + availableSeats);

            return new FlightDetails(
                flightId,
                parts[1].trim(),
                parts[2].trim(),
                seatsStr,
                calculateDuration(parts[2].trim()),
                calculatePrice(flightId, parts[1].trim()),
                bookings.getOrDefault(username, new ArrayList<>()).contains(flightId),
                totalSeats,
                availableSeats
            );
        } catch (Exception e) {
            System.err.println("ERROR parsing flight: " + flight + " - " + e.getMessage());
            return null;
        }
    }

    private JPanel createFlightCard(FlightDetails details) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setPreferredSize(new Dimension(920, 150));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(new Color(245, 245, 250));

        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(createFlightInfoPanel(details), BorderLayout.CENTER);
        contentPanel.add(createBookingButtonPanel(details), BorderLayout.EAST);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFlightInfoPanel(FlightDetails details) {
        JPanel panel = new JPanel(new GridLayout(4, 1, 8, 8));
        panel.setOpaque(false);

        // Flight ID and Destination
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(createInfoLabel("✈ Flight " + details.id + ":", "", true));
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(createInfoLabel("📍 " + details.dest, "", false));
        
        // Departure Time
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.setOpaque(false);
        timePanel.add(createInfoLabel("⭕ Departure: " + details.time, "", false));
        
        // Price (in pesos)
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pricePanel.setOpaque(false);
        pricePanel.add(createInfoLabel("💰 Price: ₱" + details.price + "/pax", "", false));
        
        // Duration and Seats
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(createInfoLabel("⏱ Duration: " + details.duration, "", false));
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(createSeatAvailabilityLabel(details.availableSeats, details.totalSeats));

        panel.add(topPanel);
        panel.add(timePanel);
        panel.add(pricePanel);
        panel.add(bottomPanel);

        return panel;
    }

    private JLabel createInfoLabel(String prefix, String text, boolean bold) {
        JLabel label = new JLabel(prefix + text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        label.setForeground(new Color(50, 50, 50));
        label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return label;
    }

    private JLabel createSeatAvailabilityLabel(int available, int total) {
        JLabel label = new JLabel("↓ Seats: " + available + "/" + total + " available");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(available > 0 ? new Color(0, 120, 0) : Color.RED);
        return label;
    }

    private JPanel createBookingButtonPanel(FlightDetails details) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        
        panel.add(createBookingButton(details.isBooked, details.availableSeats, details));
        
        return panel;
    }

    private JButton createBookingButton(boolean isBooked, int availableSeats, FlightDetails details) {
        String text = isBooked ? "CANCEL BOOKING" : (availableSeats > 0 ? "BOOK NOW" : "FULL");
        Color bgColor = isBooked ? CANCEL_COLOR : (availableSeats > 0 ? ACCENT_COLOR : new Color(150, 150, 150));
        
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 35));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setEnabled(availableSeats > 0 || isBooked);
        
        button.addActionListener(e -> handleBookingAction(details));
        addButtonHoverEffect(button, isBooked, availableSeats);
        
        return button;
    }

    private void addButtonHoverEffect(JButton button, boolean isBooked, int availableSeats) {
        Color originalColor = button.getBackground();
        Color hoverColor = isBooked ? CANCEL_COLOR.darker() : ACCENT_COLOR.darker();
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isBooked || availableSeats > 0) {
                    button.setBackground(hoverColor);
                    button.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
                button.setForeground(Color.BLACK);
            }
        });
    }

    private void handleBookingAction(FlightDetails details) {
        if (details.isBooked) {
            cancelBooking(details);
        } else {
            bookFlight(details);
        }
        renderFlightCards();
    }

    private void cancelBooking(FlightDetails details) {
        int option = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to cancel your booking for flight " + details.id + "?", 
            "Confirm Cancellation", 
            JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            try {
                String serviceDetails = String.format("%s | %s | %s | %s", 
                    details.id, details.dest, details.time, details.seats);
                
                boolean removed = receiptManager.removeReceipt(username, FLIGHT_BOOKING_SERVICE, serviceDetails);
                
                if (removed) {
                    bookings.get(username).remove(details.id);
                    showInfoDialog("Cancelled booking: " + details.id);
                } else {
                    showErrorDialog("Failed to find booking to cancel");
                }
            } catch (Exception e) {
                showErrorDialog("Error cancelling booking: " + e.getMessage());
            }
        }
    }

    private void bookFlight(FlightDetails details) {
        try {
            SeatSelectionDialog dialog = new SeatSelectionDialog(this, details.id, 
                details.totalSeats, details.availableSeats);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                int seatsToBook = dialog.getSeatsBooked();
                String seatNumbers = dialog.getSelectedSeats();

                // Calculate total cost (price per seat * seats booked)
                double pricePerSeat = Double.parseDouble(details.price.replace("₱", "").trim());
                double totalCost = pricePerSeat * seatsToBook;

                // Pass totalCost to createReceipt
                receiptManager.createReceipt(
                    username, 
                    FLIGHT_BOOKING_SERVICE, 
                    String.format("%s | %s | %s | %s seats", details.id, details.dest, details.time, details.seats),
                    seatsToBook,
                    seatNumbers,
                    totalCost  // New parameter
                );
            }
        } catch (Exception e) {
            showErrorDialog("Error booking flight: " + e.getMessage());
        }
    }

    private JComponent createFlightActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        panel.add(createActionButton("Refresh Flights", e -> refreshFlights()));
        panel.add(createActionButton("Add Flight (Admin)", e -> addFlight()));
        panel.add(createActionButton("Reset All Seats (Admin)", e -> resetAllSeats()));
        
        return panel;
    }

    private JButton createActionButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        button.setBorder(createButtonBorder());
        button.setFocusPainted(false);
        button.addActionListener(listener);
        addActionButtonHoverEffect(button);
        return button;
    }

    private Border createButtonBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR.darker(), 1),
            BorderFactory.createEmptyBorder(6, 18, 6, 18)
        );
    }

    private void addActionButtonHoverEffect(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR.brighter());
                button.setForeground(Color.BLACK);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.BLACK);
            }
        });
    }

    private void refreshFlights() {
        showInfoDialog("Flights refreshed!");
        renderFlightCards();
    }

    private void resetAllSeats() {
        String input = JOptionPane.showInputDialog(this, "Enter admin password to reset all seats:");
        if (input == null || !input.equals(ADMIN_PASSWORD)) {
            showErrorDialog("Admin access denied");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "This will reset ALL seat availability to full capacity.\nContinue?",
            "Confirm Seat Reset",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Clear all bookings
            bookings.clear();
            bookings.put(username, new ArrayList<>());

            // Clear all receipts
            receiptManager.setReceipts(new ArrayList<>());

            // Reinitialize all flights with full capacity
            initializeFlightSeats();

            saveUserData();
            renderFlightCards();
            showInfoDialog("All seats have been reset to full availability");
        }
    }

    private void addFlight() {
        String input = JOptionPane.showInputDialog(this, "Enter admin password:");
        if (input == null || !input.equals(ADMIN_PASSWORD)) {
            showErrorDialog("Admin access denied");
            return;
        }

        String flightInfo = JOptionPane.showInputDialog(this, 
            "Enter flight details in format:\nFlightID | Destination | Time | Seats\nExample: AA101 | New York | 10:00 AM - 11:45 AM | 180 seats");
        
        if (flightInfo != null && !flightInfo.trim().isEmpty()) {
            try {
                flights.add(flightInfo);
                String[] parts = flightInfo.split("\\|");
                String flightId = parts[0].trim();
                int totalSeats = Integer.parseInt(parts[3].trim().replaceAll("[^0-9]", ""));
                receiptManager.initializeFlight(flightId, totalSeats);
                renderFlightCards();
            } catch (Exception e) {
                showErrorDialog("Invalid flight format: " + e.getMessage());
            }
        }
    }

    private JPanel createTransportPanel() {
        TransportationSystem transportSystem = new TransportationSystem();
        return new TransportationPanel(username, PRIMARY_COLOR, SECONDARY_COLOR, transportSystem, receiptManager);
    }

    private JPanel createTransactionHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panel.add(createHistoryHeader(), BorderLayout.NORTH);
        panel.add(createHistoryTable(), BorderLayout.CENTER);
        panel.add(createHistoryRefreshButton(), BorderLayout.SOUTH);
        
        return panel;
    }

    private JComponent createHistoryHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel title = new JLabel("Transaction History");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        title.setForeground(PRIMARY_COLOR);
        header.add(title, BorderLayout.WEST);
        
        return header;
    }

    private JComponent createHistoryTable() {
        String[] columns = {"Date", "Type", "Details", "Status"};
        transactionModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        populateTransactionTable(transactionModel);
        
        JTable table = new JTable(transactionModel);
        styleHistoryTable(table);
        
        return new JScrollPane(table);
    }

    private void styleHistoryTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
    }

    private void populateTransactionTable(DefaultTableModel model) {
        for (Receipt receipt : receiptManager.getUserReceipts(username)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            model.addRow(new Object[]{
                dateFormat.format(receipt.getBookingDate()),
                receipt.getServiceType(),
                receipt.getServiceDetails() + " (Seats: " + receipt.getSeatsBooked() + ")",
                "Completed"
            });
        }
    }

    private JComponent createHistoryRefreshButton() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        
        panel.add(createActionButton("Refresh History", e -> refreshHistory()));
        
        return panel;
    }

    private void refreshHistory() {
        transactionModel.setRowCount(0);
        populateTransactionTable(transactionModel);
    }

    private JPanel createReceiptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panel.add(createReceiptHeader(), BorderLayout.NORTH);
        panel.add(createReceiptList(), BorderLayout.CENTER);
        panel.add(createReceiptRefreshButton(), BorderLayout.SOUTH);
        
        return panel;
    }

    private JComponent createReceiptHeader() {
        JLabel title = new JLabel("Your Receipts");
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        title.setForeground(PRIMARY_COLOR);
        return title;
    }

    private JComponent createReceiptList() {
        receiptListModel = new DefaultListModel<>();
        for (Receipt receipt : receiptManager.getUserReceipts(username)) {
            receiptListModel.addElement(receipt.toString());
        }

        JList<String> list = new JList<>(receiptListModel);
        list.setFont(new Font("Monospaced", Font.PLAIN, 12));
        list.setBackground(Color.WHITE);

        // Add double-click listener
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click
                    int index = list.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Receipt receipt = receiptManager.getUserReceipts(username).get(index);
                        new ReceiptDialog(Dashboard.this, receipt).setVisible(true);
                    }
                }
            }
        });

        return new JScrollPane(list);
    }

    private JComponent createReceiptRefreshButton() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        panel.add(createActionButton("Refresh Receipts", e -> refreshReceipts()));
        
        return panel;
    }

    private void refreshReceipts() {
        receiptListModel.clear();
        for (Receipt receipt : receiptManager.getUserReceipts(username)) {
            receiptListModel.addElement(receipt.toString());
        }
    }

    private String calculateDuration(String time) {
        try {
            String[] times = time.split("-");
            if (time.contains("AM") && time.contains("PM")) {
                return "10h+";
            }
            return "2h 30m";
        } catch (Exception e) {
            return "1h 45m";
        }
    }

    private String calculatePrice(String id, String destination) {
        if (id.startsWith("BA") || destination.contains("London")) return "₱6000";
        if (id.startsWith("QF") || destination.contains("Sydney")) return "₱5500"; 
        if (id.startsWith("AA")) return "₱2500";
        return "₱2000";
    }

    private void loadUserData() {
        if (!Files.exists(Paths.get(DATA_FILE))) {
            bookings.putIfAbsent(username, new ArrayList<>());
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) ois.readObject();

            @SuppressWarnings("unchecked")
            Map<String, List<String>> savedBookings = (Map<String, List<String>>) data.get(BOOKINGS_KEY);
            if (savedBookings != null) bookings.putAll(savedBookings);

            @SuppressWarnings("unchecked")
            List<Receipt> savedReceipts = (List<Receipt>) data.get(RECEIPTS_KEY);
            if (savedReceipts != null) {
                receiptManager.setReceipts(savedReceipts);
            }
        } catch (Exception e) {
            showErrorDialog("Failed to load user data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveUserData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            Map<String, Object> data = new HashMap<>();
            data.put(BOOKINGS_KEY, bookings);
            data.put(RECEIPTS_KEY, receiptManager.getAllReceipts());
            oos.writeObject(data);
        } catch (IOException e) {
            showErrorDialog("Failed to save user data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupWindowListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveUserData();
                dispose();
            }
        });
    }

    private void showHelpDialog() {
        JOptionPane.showMessageDialog(this, 
            "Contact our support team at:\n\n" + SUPPORT_EMAIL + "\n" + SUPPORT_PHONE, 
            "Help & Support", 
            JOptionPane.INFORMATION_MESSAGE,
            new ImageIcon(createRoundIcon(new Color(0, 150, 200), 48)));
    }

    private void showSupportDialog() {
        JOptionPane.showMessageDialog(this, 
            "Support feature coming soon!\n\nFor now, please contact:\n" + SUPPORT_EMAIL, 
            "Support", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSettingsDialog() {
    JPanel settingsPanel = new JPanel(new BorderLayout(10, 10));
    settingsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    
    // User info section
    JPanel userPanel = new JPanel();
    userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
    userPanel.setBorder(BorderFactory.createTitledBorder("User Information"));
    
    JLabel userLabel = new JLabel("Current User: " + username);
    userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
    userPanel.add(userLabel);
    
    // Music controls section
    JPanel musicPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
    musicPanel.setBorder(BorderFactory.createTitledBorder("Background Music"));
    
    JButton playButton = new JButton("Play");
    playButton.addActionListener(e -> musicPlayer.playMusic(BG_MUSIC_FILE));
    
    JButton stopButton = new JButton("Stop");
    stopButton.addActionListener(e -> musicPlayer.stopMusic());
    
    musicPanel.add(playButton);
    musicPanel.add(stopButton);
    
    // Combine panels
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.add(userPanel);
    contentPanel.add(Box.createVerticalStrut(15));
    contentPanel.add(musicPanel);
    
    settingsPanel.add(contentPanel, BorderLayout.CENTER);
    
    JOptionPane.showMessageDialog(this, 
        settingsPanel, 
        "Settings", 
        JOptionPane.PLAIN_MESSAGE);
    }
    

    private void showInfoDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Dashboard dashboard = new Dashboard("TestUser");
            dashboard.setVisible(true);
        });
    }

    private static class GradientPanel extends JPanel {
        private final Color startColor;
        private final Color endColor;

        public GradientPanel(Color startColor, Color endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class FlightDetails {
        final String id;
        final String dest;
        final String time;
        final String seats;
        final String duration;
        final String price;
        final boolean isBooked;
        final int totalSeats;
        final int availableSeats;

        public FlightDetails(String id, String dest, String time, String seats, 
                           String duration, String price, boolean isBooked,
                           int totalSeats, int availableSeats) {
            this.id = id;
            this.dest = dest;
            this.time = time;
            this.seats = seats;
            this.duration = duration;
            this.price = price;
            this.isBooked = isBooked;
            this.totalSeats = totalSeats;
            this.availableSeats = availableSeats;
        }
    }
}