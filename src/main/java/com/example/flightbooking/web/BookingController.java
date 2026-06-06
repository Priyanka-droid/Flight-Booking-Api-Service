package com.example.flightbooking.web;

import com.example.flightbooking.service.BookingResult;
import com.example.flightbooking.service.BookingService;
import com.example.flightbooking.web.dto.BookingRequest;
import com.example.flightbooking.web.dto.BookingResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> book(
            @Valid @RequestBody BookingRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        BookingResult result = bookingService.book(
                request.flightId(), request.seats(), request.passenger(), idempotencyKey);
        BookingResponse response = new BookingResponse(
                result.booking().id(),
                result.booking().flightId(),
                result.booking().seats(),
                result.remainingSeats());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
