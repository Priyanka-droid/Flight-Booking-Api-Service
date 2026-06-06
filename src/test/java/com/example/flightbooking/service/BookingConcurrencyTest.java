package com.example.flightbooking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.flightbooking.exception.InsufficientSeatsException;
import com.example.flightbooking.model.Flight;
import com.example.flightbooking.repository.FlightRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class BookingConcurrencyTest {

    // Many threads race for the last few seats at once; total booked must never
    // exceed capacity, so successes equal capacity exactly and none oversell.
    @Test
    void concurrentBookingsNeverOverbookTheLastSeats() throws InterruptedException {
        FlightRepository repository = new FlightRepository();
        int capacity = 5;
        repository.save(new Flight("XX1", capacity, capacity));
        BookingService bookingService = new BookingService(repository);

        int attempts = 100;
        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(attempts);
        AtomicInteger booked = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < attempts; i++) {
            pool.submit(() -> {
                try {
                    startGun.await();
                    bookingService.book("XX1", 1, "passenger");
                    booked.incrementAndGet();
                } catch (InsufficientSeatsException e) {
                    rejected.incrementAndGet();
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

        assertThat(booked.get()).isEqualTo(capacity);
        assertThat(rejected.get()).isEqualTo(attempts - capacity);
        assertThat(repository.findById("XX1")).get()
                .extracting(Flight::remainingSeats)
                .isEqualTo(0);
    }

    // Bookings on different flights run concurrently and each flight's count stays
    // correct, since the per-flight atomic step only serializes the same flight.
    @Test
    void concurrentBookingsOnDifferentFlightsEachStayCorrect() throws InterruptedException {
        FlightRepository repository = new FlightRepository();
        int flightCount = 10;
        int capacity = 20;
        for (int f = 0; f < flightCount; f++) {
            repository.save(new Flight("F" + f, capacity, capacity));
        }
        BookingService bookingService = new BookingService(repository);

        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(flightCount * capacity);

        for (int f = 0; f < flightCount; f++) {
            String flightId = "F" + f;
            for (int s = 0; s < capacity; s++) {
                pool.submit(() -> {
                    try {
                        startGun.await();
                        bookingService.book(flightId, 1, "passenger");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finished.countDown();
                    }
                });
            }
        }

        startGun.countDown();
        finished.await();
        pool.shutdown();

        for (int f = 0; f < flightCount; f++) {
            assertThat(repository.findById("F" + f)).get()
                    .extracting(Flight::remainingSeats)
                    .isEqualTo(0);
        }
    }
}
