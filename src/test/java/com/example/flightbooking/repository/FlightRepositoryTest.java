package com.example.flightbooking.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.flightbooking.exception.FlightNotFoundException;
import com.example.flightbooking.exception.InsufficientSeatsException;
import com.example.flightbooking.model.Flight;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlightRepositoryTest {

    private FlightRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FlightRepository();
        repository.save(new Flight("AI101", 180));
    }

    @Test
    void loadedFlightIsFoundWithItsSeatCount() {
        Optional<Flight> flight = repository.findById("AI101");

        assertThat(flight).isPresent();
        assertThat(flight.get().getTotalSeats()).isEqualTo(180);
        assertThat(flight.get().getRemainingSeats()).isEqualTo(180);
    }

    @Test
    void unknownFlightIsNotFound() {
        assertThat(repository.findById("ZZ999")).isEmpty();
    }

    @Test
    void reservingSeatsOnUnknownFlightThrows() {
        assertThatThrownBy(() -> repository.reserveSeats("ZZ999", 1))
                .isInstanceOf(FlightNotFoundException.class);
    }

    @Test
    void reservingMoreSeatsThanRemainThrowsAndLeavesCountUnchanged() {
        assertThatThrownBy(() -> repository.reserveSeats("AI101", 181))
                .isInstanceOf(InsufficientSeatsException.class);

        assertThat(repository.findById("AI101")).get()
                .extracting(Flight::getRemainingSeats)
                .isEqualTo(180);
    }
}
