package com.csols.FirstFlight;

import javax.swing.*;
import java.io.Serializable;
import java.util.*;

public class ReceiptManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Receipt> receipts;
    private Map<String, Integer> availableSeats;
    private Map<String, Integer> totalSeats;

    public ReceiptManager() {
        receipts = new ArrayList<>();
        availableSeats = new HashMap<>();
        totalSeats = new HashMap<>();
    }

    public void initializeFlight(String flightId, int totalSeats) {
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("Total seats must be positive");
        }
        this.totalSeats.put(flightId, totalSeats);
        this.availableSeats.put(flightId, totalSeats);
    }

    public void validateFlightExists(String flightId) {
        if (!totalSeats.containsKey(flightId)) {
            throw new IllegalArgumentException("Flight " + flightId + " does not exist");
        }
    }
    
    public void createReceipt(String username, String serviceType, 
                            String serviceDetails, int seatsBooked, 
                            String seatNumbers, double totalCost) {
        if (serviceType.equals("Flight Booking")) {
            String flightId = serviceDetails.split("\\|")[0].trim();
            validateFlightExists(flightId);
            validateSeatAvailability(flightId, seatsBooked);
            updateAvailableSeats(flightId, -seatsBooked);
        }

        Receipt receipt = new Receipt(username, serviceType, serviceDetails, 
                                    new Date(), seatsBooked, seatNumbers, totalCost);
        receipts.add(receipt);
        showReceiptConfirmation(receipt);
    }

    public boolean removeReceipt(String username, String serviceType, String serviceDetails) {
        Iterator<Receipt> iterator = receipts.iterator();
        while (iterator.hasNext()) {
            Receipt receipt = iterator.next();
            if (receipt.getUsername().equals(username) &&
                receipt.getServiceType().equals(serviceType) &&
                receipt.getServiceDetails().equals(serviceDetails)) {
                
                if (serviceType.equals("Flight Booking")) {
                    String flightId = serviceDetails.split("\\|")[0].trim();
                    updateAvailableSeats(flightId, receipt.getSeatsBooked());
                }
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public int getAvailableSeats(String flightId) {
        return availableSeats.getOrDefault(flightId, 0);
    }

    public int getTotalSeats(String flightId) {
        return totalSeats.getOrDefault(flightId, 0);
    }

    public List<Receipt> getUserReceipts(String username) {
        List<Receipt> userReceipts = new ArrayList<>();
        for (Receipt receipt : receipts) {
            if (receipt.getUsername().equals(username)) {
                userReceipts.add(receipt);
            }
        }
        return userReceipts;
    }

    public List<Receipt> getAllReceipts() {
        return new ArrayList<>(receipts);
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = new ArrayList<>(receipts);
        recalculateSeatAvailability();
    }

    public void updateAvailableSeats(String flightId, int change) {
        if (!totalSeats.containsKey(flightId)) {
            throw new IllegalArgumentException("Flight " + flightId + " not initialized");
        }

        int current = availableSeats.get(flightId);
        int newValue = current + change;
        int capacity = totalSeats.get(flightId);

        if (newValue < 0 || newValue > capacity) {
            throw new IllegalStateException(
                String.format("Invalid seat count %d for flight %s (capacity %d)", 
                newValue, flightId, capacity));
        }

        availableSeats.put(flightId, newValue);
    }

    private void validateSeatAvailability(String flightId, int seatsToBook) {
        int available = getAvailableSeats(flightId);
        if (seatsToBook > available) {
            throw new IllegalArgumentException(
                String.format("Only %d seats available for flight %s, but tried to book %d", 
                available, flightId, seatsToBook));
        }
    }

    private void recalculateSeatAvailability() {
        Map<String, Integer> originalTotals = new HashMap<>(totalSeats);
        availableSeats.clear();
        availableSeats.putAll(originalTotals);

        for (Receipt receipt : receipts) {
            if (receipt.getServiceType().equals("Flight Booking")) {
                String flightId = receipt.getServiceDetails().split("\\|")[0].trim();
                if (availableSeats.containsKey(flightId)) {
                    int current = availableSeats.get(flightId);
                    availableSeats.put(flightId, current - receipt.getSeatsBooked());
                }
            }
        }
    }

    private void showReceiptConfirmation(Receipt receipt) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null, 
                receipt.toString(), 
                "Booking Confirmed", 
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
}