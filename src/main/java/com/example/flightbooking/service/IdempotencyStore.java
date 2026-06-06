package com.example.flightbooking.service;

import com.example.flightbooking.exception.IdempotencyConflictException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyStore {

    private final ConcurrentMap<String, Entry> entries = new ConcurrentHashMap<>();

    private record Entry(String fingerprint, BookingResult result) {
    }

    // Claims the token and runs `work` exactly once under it. Concurrent calls with
    // the
    // same token block until the first finishes, then replay its result instead of
    // running `work` again. A token replayed with a different fingerprint is a
    // conflict.
    public BookingResult execute(String token, String fingerprint, Supplier<BookingResult> work) {
        Entry entry = entries.computeIfAbsent(token, t -> new Entry(fingerprint, work.get()));
        if (!entry.fingerprint().equals(fingerprint)) {
            throw new IdempotencyConflictException(token);
        }
        return entry.result();
    }
}
