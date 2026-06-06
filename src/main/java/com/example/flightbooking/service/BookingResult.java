package com.example.flightbooking.service;

import com.example.flightbooking.model.Booking;

public record BookingResult(Booking booking, int remainingSeats) {
}
