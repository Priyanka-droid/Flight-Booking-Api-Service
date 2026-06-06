package com.example.flightbooking.exception;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String token) {
        super("Idempotency-Key " + token + " was reused with a different request");
    }
}
