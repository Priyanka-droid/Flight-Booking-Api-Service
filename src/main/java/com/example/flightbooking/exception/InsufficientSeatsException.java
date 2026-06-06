package com.example.flightbooking.exception;

public class InsufficientSeatsException extends RuntimeException {

    public InsufficientSeatsException(String flightId, int requested, int remaining) {
        super("Flight " + flightId + " has " + remaining + " seat(s) left, requested " + requested);
    }
}
