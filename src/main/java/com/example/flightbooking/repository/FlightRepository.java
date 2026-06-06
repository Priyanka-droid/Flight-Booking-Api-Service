package com.example.flightbooking.repository;

import com.example.flightbooking.model.Flight;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class FlightRepository {

    private final ConcurrentMap<String, Flight> flights = new ConcurrentHashMap<>();

    public void save(Flight flight) {
        flights.put(flight.id(), flight);
    }

    public Optional<Flight> findById(String id) {
        return Optional.ofNullable(flights.get(id));
    }
}
