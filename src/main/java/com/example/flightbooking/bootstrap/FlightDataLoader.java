package com.example.flightbooking.bootstrap;

import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Seeds a few flights into the in-memory store at startup so the service is
// usable without a search feature. Remaining seats start equal to capacity.
@Component
public class FlightDataLoader implements CommandLineRunner {

    private final FlightRepository flightRepository;

    public FlightDataLoader(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    public void run(String... args) {
        flightRepository.save(new Flight("AI101", 180, 180));
        flightRepository.save(new Flight("BA202", 200, 200));
        flightRepository.save(new Flight("UA303", 150, 150));
    }
}
