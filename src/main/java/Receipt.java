package com.csols.FirstFlight;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Receipt implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String serviceType;
    private String serviceDetails;
    private Date bookingDate;
    private int seatsBooked;
    private String seatNumbers;
    private double totalCost;
    
    public Receipt(String username, String serviceType, String serviceDetails, 
                  Date bookingDate, int seatsBooked, String seatNumbers, double totalCost) {
        this.username = username;
        this.serviceType = serviceType;
        this.serviceDetails = serviceDetails;
        this.bookingDate = bookingDate;
        this.seatsBooked = seatsBooked;
        this.seatNumbers = seatNumbers;
        this.totalCost = totalCost;
    }
    
    // Getters
    public String getUsername() { return username; }
    public String getServiceType() { return serviceType; }
    public String getServiceDetails() { return serviceDetails; }
    public Date getBookingDate() { return bookingDate; }
    public int getSeatsBooked() { return seatsBooked; }
    public String getSeatNumbers() { return seatNumbers; }
    public double getTotalCost() { return totalCost; }
    
    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("=== FirstFlight Receipt ===\n");
        sb.append("Customer: ").append(username).append("\n");
        sb.append("Service: ").append(serviceType).append("\n");
        sb.append("Details: ").append(serviceDetails).append("\n");
        sb.append("Seats Booked: ").append(seatsBooked).append("\n");
        sb.append("Total Cost: ₱").append(String.format("%.2f", totalCost)).append("\n");
        if (seatNumbers != null && !seatNumbers.isEmpty()) {
            sb.append("Seat Numbers: ").append(seatNumbers).append("\n");
        }
        sb.append("Date: ").append(dateFormat.format(bookingDate)).append("\n");
        sb.append("==========================");
        return sb.toString();
    }
}