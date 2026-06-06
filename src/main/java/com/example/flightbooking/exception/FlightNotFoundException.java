package com.example.flightbooking.exception;

public class FlightNotFoundException extends RuntimeException {

    public FlightNotFoundException(String flightId) {
        super("Unknown flight: " + flightId);
    }
}
