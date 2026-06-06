package com.example.flightbooking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.flightbooking.exception.IdempotencyConflictException;
import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookingIdempotencyTest {

    private FlightRepository repository;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        repository = new FlightRepository();
        repository.save(new Flight("AI101", 180));
        bookingService = new BookingService(repository, new IdempotencyStore());
    }

    @Test
    void retryWithSameTokenReturnsOriginalBookingAndBooksOnce() {
        BookingResult first = bookingService.book("AI101", 2, "Jane Doe", "tok-1");
        BookingResult retry = bookingService.book("AI101", 2, "Jane Doe", "tok-1");

        assertThat(retry.booking().id()).isEqualTo(first.booking().id());
        assertThat(retry.remainingSeats()).isEqualTo(first.remainingSeats());
        assertThat(repository.findById("AI101")).get()
                .extracting(Flight::getRemainingSeats)
                .isEqualTo(178);
    }

    @Test
    void sameTokenWithDifferentRequestIsRejected() {
        bookingService.book("AI101", 2, "Jane Doe", "tok-1");

        assertThatThrownBy(() -> bookingService.book("AI101", 3, "Jane Doe", "tok-1"))
                .isInstanceOf(IdempotencyConflictException.class);
    }

    @Test
    void requestsWithoutTokenCreateDistinctBookings() {
        BookingResult a = bookingService.book("AI101", 1, "Jane Doe", null);
        BookingResult b = bookingService.book("AI101", 1, "Jane Doe", null);

        assertThat(a.booking().id()).isNotEqualTo(b.booking().id());
        assertThat(repository.findById("AI101")).get()
                .extracting(Flight::getRemainingSeats)
                .isEqualTo(178);
    }

    // Two retries with the same token arriving together must collapse to one
    // booking.
    @Test
    void simultaneousRetriesWithSameTokenCreateOnlyOneBooking() throws InterruptedException {
        int threads = 50;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(threads);
        Set<String> bookingIds = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    BookingResult result = bookingService.book("AI101", 2, "Jane Doe", "tok-1");
                    bookingIds.add(result.booking().id());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            });
        }

        startGun.countDown();
        finished.await();
        pool.shutdown();

        assertThat(bookingIds).hasSize(1);
        assertThat(repository.findById("AI101")).get()
                .extracting(Flight::getRemainingSeats)
                .isEqualTo(178);
    }
}
