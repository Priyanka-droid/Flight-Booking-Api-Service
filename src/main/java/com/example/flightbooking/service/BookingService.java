package com.example.flightbooking.service;

import com.example.flightbooking.model.Booking;
import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final FlightRepository flightRepository;

    public BookingService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    // Reserves the seats atomically, then returns a booking plus the seats left.
    public BookingResult book(String flightId, int seats, String passenger) {
        Flight updated = flightRepository.reserveSeats(flightId, seats);
        Booking booking = new Booking(UUID.randomUUID().toString(), flightId, seats, passenger);
        return new BookingResult(booking, updated.remainingSeats());
    }
}
