package com.csols.FirstFlight;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class TransportationSystem {
    private List<TransportationOption> transportOptions;
    private Map<String, List<Transportation>> userTransportBookings;
    private ExecutorService bookingExecutor;

    public TransportationSystem() {
        this.transportOptions = new ArrayList<>();
        this.userTransportBookings = new HashMap<>();
        this.bookingExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        initializeTransportOptions();
    }

    private void initializeTransportOptions() {
        transportOptions.add(new TransportationOption(
            "Airport Transfer", 
            "Private car from airport to hotel", 
            1200.00, 
            "car.png"));
        
        transportOptions.add(new TransportationOption(
            "City Tour", 
            "4-hour guided city tour", 
            800.00, 
            "bus.png"));
            
        transportOptions.add(new TransportationOption(
            "Car Rental", 
            "Self-drive car rental", 
            2500.00, 
            "keys.png"));
            
        transportOptions.add(new TransportationOption(
            "Private Driver", 
            "Personal driver service", 
            1500.00, 
            "driver.png"));
    }

    public List<TransportationOption> getTransportOptions() {
        return Collections.unmodifiableList(transportOptions);
    }

    public boolean bookTransport(String username, String serviceName) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        TransportationOption option = transportOptions.stream()
            .filter(t -> t.getName().equals(serviceName))
            .findFirst()
            .orElse(null);

        if (option == null) {
            return false;
        }
        
        Transportation booking = new Transportation(
            "TX-" + System.currentTimeMillis(),
            serviceName,
            option.getDescription(),
            option.getPrice()
        );
        
        userTransportBookings.putIfAbsent(username, new ArrayList<>());
        userTransportBookings.get(username).add(booking);
        return true;
    }

    public void bookTransportAsync(String username, String serviceName, Consumer<Boolean> callback) {
        bookingExecutor.submit(() -> {
            try {
                boolean result = bookTransport(username, serviceName);
                SwingUtilities.invokeLater(() -> callback.accept(result));
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> callback.accept(false));
            }
        });
    }

    public boolean cancelTransport(String username, String transportId) {
        if (userTransportBookings.containsKey(username)) {
            return userTransportBookings.get(username).removeIf(t -> t.getId().equals(transportId));
        }
        return false;
    }

    public List<Transportation> getUserBookings(String username) {
        return userTransportBookings.getOrDefault(username, new ArrayList<>());
    }

    public void addTransportOption(TransportationOption option) {
        transportOptions.add(option);
    }

    public void removeTransportOption(String optionName) {
        transportOptions.removeIf(t -> t.getName().equals(optionName));
    }

    public void shutdown() {
        bookingExecutor.shutdown();
    }
}

class TransportationOption {
    private String name;
    private String description;
    private double price;
    private String iconName;

    public TransportationOption(String name, String description, double price, String iconName) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.iconName = iconName;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getIconName() { return iconName; }

    public String getFormattedPrice() {
        return String.format("₱%.2f", price);
    }
}

class Transportation {
    private String id;
    private String type;
    private String description;
    private double price;
    private Date bookingDate;

    public Transportation(String id, String type, String description, double price) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.price = price;
        this.bookingDate = new Date();
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public Date getBookingDate() { return bookingDate; }

    public String getBookingReference() {
        return "TR-" + this.id.substring(3);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | ₱%.2f | %s", 
            id, type, description, price, bookingDate);
    }
}

class TransportationPanel extends JPanel {
    private final Color PRIMARY_COLOR;
    private final Color SECONDARY_COLOR;
    private final String username;
    private final TransportationSystem transportSystem;
    private final ReceiptManager receiptManager;
    
    private JPanel transportCardsPanel;

    public TransportationPanel(String username, Color primaryColor, Color secondaryColor, 
                             TransportationSystem transportSystem, ReceiptManager receiptManager) {
        this.username = username;
        this.PRIMARY_COLOR = primaryColor;
        this.SECONDARY_COLOR = secondaryColor;
        this.transportSystem = transportSystem;
        this.receiptManager = receiptManager;
        
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Transportation Services");
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Transport cards panel
        transportCardsPanel = new JPanel();
        transportCardsPanel.setLayout(new BoxLayout(transportCardsPanel, BoxLayout.Y_AXIS));
        transportCardsPanel.setBackground(Color.WHITE);
        
        renderTransportCards();
        
        JScrollPane scrollPane = new JScrollPane(transportCardsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    private void renderTransportCards() {
        transportCardsPanel.removeAll();
        
        List<TransportationOption> options = transportSystem.getTransportOptions();
        for (TransportationOption option : options) {
            JPanel card = createTransportCard(option);
            transportCardsPanel.add(card);
            transportCardsPanel.add(Box.createVerticalStrut(10));
        }
        
        transportCardsPanel.revalidate();
        transportCardsPanel.repaint();
    }

    private JPanel createTransportCard(TransportationOption option) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(300, 150));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel(option.getName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JLabel priceLabel = new JLabel(option.getFormattedPrice());
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JTextArea descArea = new JTextArea(option.getDescription());
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        
        // Icon placeholder
        JLabel iconLabel = new JLabel(new ImageIcon(createTransportIcon(PRIMARY_COLOR, 40)));
        iconLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(descArea, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(priceLabel, BorderLayout.WEST);
        southPanel.add(iconLabel, BorderLayout.EAST);
        
        contentPanel.add(southPanel, BorderLayout.SOUTH);
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Add click handler
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                bookTransportation(option);
            }
        });
        
        return card;
    }

    private Image createTransportIcon(Color color, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.fillOval(0, 0, size, size);
        g2d.dispose();
        return image;
    }

    private void bookTransportation(TransportationOption option) {
    int choice = JOptionPane.showConfirmDialog(
        this,
        "Book " + option.getName() + " for " + option.getFormattedPrice() + "?\n" + option.getDescription(),
        "Confirm Booking",
        JOptionPane.YES_NO_OPTION
    );
    
    if (choice == JOptionPane.YES_OPTION) {
        // Show loading indicator
        JOptionPane.showMessageDialog(this, 
            "Processing your booking...",
            "Please wait",
            JOptionPane.INFORMATION_MESSAGE);
            
        transportSystem.bookTransportAsync(username, option.getName(), success -> {
            if (success) {
                String details = String.format("%s | %s | Price: %s", 
                    option.getName(), 
                    option.getDescription(),
                    option.getFormattedPrice());
                
                receiptManager.createReceipt(username, 
                    "Transport: " + option.getName(), 
                    details,
                    0, // seatsBooked
                    "N/A", // seatNumbers
                    option.getPrice() // Add the total cost parameter
                );
                
                JOptionPane.showMessageDialog(this, 
                    "Transportation booked successfully!\n\n" +
                    "Service: " + option.getName() + "\n" +
                    "Price: " + option.getFormattedPrice() + "\n" +
                    "Reference: " + transportSystem.getUserBookings(username)
                        .get(0).getBookingReference(),
                    "Booking Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to book transportation service",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
}