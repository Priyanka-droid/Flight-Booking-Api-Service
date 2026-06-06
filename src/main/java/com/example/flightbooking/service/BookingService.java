package com.example.flightbooking.service;

import com.example.flightbooking.model.Booking;
import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final FlightRepository flightRepository;
    private final IdempotencyStore idempotencyStore;

    public BookingService(FlightRepository flightRepository, IdempotencyStore idempotencyStore) {
        this.flightRepository = flightRepository;
        this.idempotencyStore = idempotencyStore;
    }

    public BookingResult book(String flightId, int seats, String passenger) {
        return book(flightId, seats, passenger, null);
    }

    // With a token, the booking work runs once per token; a retry replays the
    // original
    // booking. Without a token, every call is an independent booking.
    public BookingResult book(String flightId, int seats, String passenger, String idempotencyToken) {
        if (idempotencyToken == null || idempotencyToken.isBlank()) {
            return reserveAndBuild(flightId, seats, passenger);
        }
        String fingerprint = fingerprint(flightId, seats, passenger);
        return idempotencyStore.execute(idempotencyToken, fingerprint,
                () -> reserveAndBuild(flightId, seats, passenger));
    }

    private BookingResult reserveAndBuild(String flightId, int seats, String passenger) {
        Flight updated = flightRepository.reserveSeats(flightId, seats);
        Booking booking = new Booking(UUID.randomUUID().toString(), flightId, seats, passenger);
        return new BookingResult(booking, updated.getRemainingSeats());
    }

    private String fingerprint(String flightId, int seats, String passenger) {
        return flightId + "|" + seats + "|" + passenger;
    }
}
