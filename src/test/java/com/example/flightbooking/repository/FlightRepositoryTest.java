package com.example.flightbooking.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.flightbooking.bootstrap.FlightDataLoader;
import com.example.flightbooking.model.Flight;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlightRepositoryTest {

    private FlightRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        repository = new FlightRepository();
        new FlightDataLoader(repository).run();
    }

    @Test
    void loadedFlightIsFoundWithItsSeatCount() {
        Optional<Flight> flight = repository.findById("AI101");

        assertThat(flight).isPresent();
        assertThat(flight.get().capacity()).isEqualTo(180);
        assertThat(flight.get().remainingSeats()).isEqualTo(180);
    }

    @Test
    void unknownFlightIsNotFound() {
        assertThat(repository.findById("ZZ999")).isEmpty();
    }
}
