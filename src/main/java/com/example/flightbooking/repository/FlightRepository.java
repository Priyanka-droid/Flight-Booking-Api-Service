package com.example.flightbooking.repository;

import com.example.flightbooking.exception.FlightNotFoundException;
import com.example.flightbooking.exception.InsufficientSeatsException;
import com.example.flightbooking.model.Flight;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class FlightRepository {

    private final ConcurrentMap<String, Flight> flights = new ConcurrentHashMap<>();

    public void save(Flight flight) {
        flights.put(flight.getFlightNumber(), flight);
    }

    public Optional<Flight> findById(String id) {
        return Optional.ofNullable(flights.get(id));
    }

    // Atomically decrements the flight's remaining seats by `seats` and returns the
    // updated flight. The check-and-decrement runs as one operation per flight so
    // concurrent bookings on the same flight cannot oversell.
    public Flight reserveSeats(String flightId, int seats) {
        return flights.compute(flightId, (id, flight) -> {
            if (flight == null) {
                throw new FlightNotFoundException(flightId);
            }
            if (!flight.hasSeats(seats)) {
                throw new InsufficientSeatsException(flightId, seats, flight.getRemainingSeats());
            }
            return flight.book(seats);
        });
    }
}
