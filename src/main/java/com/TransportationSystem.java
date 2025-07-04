package com.csols.FirstFlight;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class TransportationSystem {
    private List<Transportation> availableTransport;
    private Map<String, List<Transportation>> userTransportBookings;

    public TransportationSystem() {
        this.availableTransport = new ArrayList<>();
        this.userTransportBookings = new HashMap<>();
        initializeSampleTransport();
    }

    private void initializeSampleTransport() {
        availableTransport.add(new Transportation("TX101", "Taxi", "Airport Taxi", 50.00));
        availableTransport.add(new Transportation("UB202", "Uber", "Premium SUV", 35.00));
        availableTransport.add(new Transportation("BS303", "Bus", "Shuttle Service", 10.00));
    }

    public List<Transportation> getAvailableTransport() {
        return new ArrayList<>(availableTransport);
    }

    public boolean bookTransport(String username, String transportId) {
        Transportation transport = availableTransport.stream()
            .filter(t -> t.getId().equals(transportId))
            .findFirst()
            .orElse(null);

        if (transport != null) {
            userTransportBookings.putIfAbsent(username, new ArrayList<>());
            userTransportBookings.get(username).add(transport);
            return true;
        }
        return false;
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

    public void addTransport(Transportation transport) {
        availableTransport.add(transport);
    }

    public void removeTransport(String transportId) {
        availableTransport.removeIf(t -> t.getId().equals(transportId));
        // Also remove from all user bookings
        userTransportBookings.values().forEach(list -> list.removeIf(t -> t.getId().equals(transportId)));
    }
}

class Transportation {
    private String id;
    private String type;
    private String description;
    private double price;

    public Transportation(String id, String type, String description, double price) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.price = price;
    }

    // Getters and toString()
    public String getId() { return id; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | $%.2f", id, type, description, price);
    }
}