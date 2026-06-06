package com.example.flightbooking.web;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.flightbooking.exception.FlightNotFoundException;
import com.example.flightbooking.exception.InsufficientSeatsException;
import com.example.flightbooking.model.Booking;
import com.example.flightbooking.service.BookingResult;
import com.example.flightbooking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void validBookingReturns201WithCompleteBody() throws Exception {
        Booking booking = new Booking("bk-1", "AI101", 3, "Jane Doe");
        when(bookingService.book(anyString(), anyInt(), anyString()))
                .thenReturn(new BookingResult(booking, 177));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flightId\":\"AI101\",\"seats\":3,\"passenger\":\"Jane Doe\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value("bk-1"))
                .andExpect(jsonPath("$.flightId").value("AI101"))
                .andExpect(jsonPath("$.seatsBooked").value(3))
                .andExpect(jsonPath("$.seatsRemaining").value(177));
    }

    @Test
    void unknownFlightReturns404WithErrorBody() throws Exception {
        when(bookingService.book(anyString(), anyInt(), anyString()))
                .thenThrow(new FlightNotFoundException("ZZ999"));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flightId\":\"ZZ999\",\"seats\":3,\"passenger\":\"Jane Doe\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FLIGHT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void notEnoughSeatsReturns409WithErrorBody() throws Exception {
        when(bookingService.book(anyString(), anyInt(), anyString()))
                .thenThrow(new InsufficientSeatsException("AI101", 200, 180));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flightId\":\"AI101\",\"seats\":200,\"passenger\":\"Jane Doe\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_SEATS"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void missingFlightIdReturns400WithErrorBody() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seats\":3,\"passenger\":\"Jane Doe\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void nonPositiveSeatsReturns400() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flightId\":\"AI101\",\"seats\":0,\"passenger\":\"Jane Doe\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingPassengerReturns400() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flightId\":\"AI101\",\"seats\":3}"))
                .andExpect(status().isBadRequest());
    }
}
