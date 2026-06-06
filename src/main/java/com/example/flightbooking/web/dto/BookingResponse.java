package com.example.flightbooking.web.dto;

public record BookingResponse(
        String bookingId,
        String flightId,
        int seatsBooked,
        int seatsRemaining) {
}
