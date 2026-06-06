package com.example.flightbooking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingServiceTest {

    private FlightRepository repository;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        repository = new FlightRepository();
        repository.save(new Flight("AI101", 180));
        bookingService = new BookingService(repository, new IdempotencyStore());
    }

    @Test
    void bookingReducesRemainingSeatsAndReturnsAnIdentifiableBooking() {
        BookingResult result = bookingService.book("AI101", 3, "Jane Doe");

        assertThat(result.booking().id()).isNotBlank();
        assertThat(result.booking().flightId()).isEqualTo("AI101");
        assertThat(result.booking().seats()).isEqualTo(3);
        assertThat(result.booking().passenger()).isEqualTo("Jane Doe");
        assertThat(result.remainingSeats()).isEqualTo(177);
    }

    @Test
    void bookingPersistsTheDecrementedSeatCount() {
        bookingService.book("AI101", 5, "Jane Doe");

        assertThat(repository.findById("AI101")).get()
                .extracting(flight -> flight.getRemainingSeats())
                .isEqualTo(175);
    }
}
