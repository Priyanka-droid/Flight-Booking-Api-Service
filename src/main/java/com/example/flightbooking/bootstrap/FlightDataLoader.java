package com.example.flightbooking.bootstrap;

import com.example.flightbooking.config.FlightCatalogProperties;
import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Seeds a few flights into the in-memory store at startup so the service is
// usable without a search feature. Remaining seats start equal to capacity.
@Component
public class FlightDataLoader implements CommandLineRunner {

    private final FlightRepository flightRepository;
    private final FlightCatalogProperties properties;

    public FlightDataLoader(FlightRepository flightRepository, FlightCatalogProperties properties) {
        this.flightRepository = flightRepository;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        properties.getCatalog().stream()
                .map(e -> new Flight(e.getNumber(), e.getSeats()))
                .forEach(flightRepository::save);
    }
}
