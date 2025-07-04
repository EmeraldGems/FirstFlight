package com.csols.FirstFlight;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;

public class ReceiptDialog extends JDialog {
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color SECONDARY_COLOR = new Color(240, 240, 245);
    private static final Color ACCENT_COLOR = new Color(70, 160, 70);
    private static final Color CANCEL_COLOR = new Color(220, 80, 80);

    public ReceiptDialog(JFrame parent, Receipt receipt) {
        super(parent, "Booking Receipt", true);
        setupDialog();
        initUI(receipt);
    }

    private void setupDialog() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
    }

    private void initUI(Receipt receipt) {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(receipt), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel titleLabel = new JLabel("FIRSTFLIGHT RECEIPT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel);

        return panel;
    }

    private JPanel createContentPanel(Receipt receipt) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(Color.WHITE);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        addReceiptLine(panel, "Customer:", receipt.getUsername());
        addReceiptLine(panel, "Service Type:", receipt.getServiceType());
        addReceiptLine(panel, "Details:", receipt.getServiceDetails());
        addReceiptLine(panel, "Seats Booked:", String.valueOf(receipt.getSeatsBooked()));

        if (receipt.getSeatNumbers() != null && !receipt.getSeatNumbers().isEmpty()) {
            addReceiptLine(panel, "Seat Numbers:", receipt.getSeatNumbers());
        }

        addReceiptLine(panel, "Booking Date:", dateFormat.format(receipt.getBookingDate()));

        // Add separator before total
        panel.add(Box.createVerticalStrut(15));
        JSeparator separator = new JSeparator();
        separator.setForeground(PRIMARY_COLOR);
        panel.add(separator);
        panel.add(Box.createVerticalStrut(15));

        // Add total cost with emphasis
        addTotalCost(panel, receipt.getTotalCost());

        return panel;
    }

    private void addReceiptLine(JPanel panel, String label, String value) {
        JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        linePanel.setBackground(Color.WHITE);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelComponent.setPreferredSize(new Dimension(120, 20));

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        linePanel.add(labelComponent);
        linePanel.add(valueComponent);
        panel.add(linePanel);
        panel.add(Box.createVerticalStrut(8));
    }

    private void addTotalCost(JPanel panel, double totalCost) {
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(Color.WHITE);

        JLabel totalLabel = new JLabel("TOTAL:");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(PRIMARY_COLOR);

        JLabel totalValue = new JLabel(String.format("₱%.2f", totalCost));
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalValue.setForeground(ACCENT_COLOR);

        totalPanel.add(totalLabel);
        totalPanel.add(Box.createHorizontalStrut(10));
        totalPanel.add(totalValue);
        panel.add(totalPanel);
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panel.setBackground(SECONDARY_COLOR);

        // Thank you message
        JLabel thankYouLabel = new JLabel("Thank you for choosing FirstFlight!");
        thankYouLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        thankYouLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(thankYouLabel, BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);

        JButton printButton = createStyledButton("Print", PRIMARY_COLOR, e -> handlePrintAction());
        JButton closeButton = createStyledButton("Close", CANCEL_COLOR, e -> dispose());

        buttonPanel.add(printButton);
        printButton.setForeground(Color.BLACK);
        closeButton.setForeground(Color.BLACK);
        buttonPanel.add(Box.createHorizontalStrut(15));
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createStyledButton(String text, Color color, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        button.addActionListener(action);

        // Hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void handlePrintAction() {
        // Implement actual printing functionality here
        JOptionPane.showMessageDialog(this,
            "Print functionality would be implemented here",
            "Print Receipt",
            JOptionPane.INFORMATION_MESSAGE);
    }
}