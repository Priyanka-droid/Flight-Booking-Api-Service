package com.example.flightbooking.dto;

public record BookingResponse(
                String bookingId,
                String flightId,
                int seatsBooked,
                int seatsRemaining) {
}
