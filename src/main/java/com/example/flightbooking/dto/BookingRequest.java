package com.example.flightbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookingRequest(
                @NotBlank String flightId,
                @NotNull @Positive Integer seats,
                @NotBlank String passenger) {
}
