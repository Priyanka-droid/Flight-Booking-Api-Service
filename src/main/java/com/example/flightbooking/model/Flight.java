package com.example.flightbooking.model;

public class Flight {

    private final String flightNumber;
    private final int totalSeats;
    private final int remainingSeats;

    public Flight(String flightNumber, int totalSeats) {
        this(flightNumber, totalSeats, totalSeats);
    }

    private Flight(String flightNumber, int totalSeats, int remainingSeats) {
        this.flightNumber = flightNumber;
        this.totalSeats = totalSeats;
        this.remainingSeats = remainingSeats;
    }

    public boolean hasSeats(int seats) {
        return remainingSeats >= seats;
    }

    // Returns a copy of this flight with the given number of seats taken.
    public Flight book(int seats) {
        return new Flight(flightNumber, totalSeats, remainingSeats - seats);
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getRemainingSeats() {
        return remainingSeats;
    }
}
