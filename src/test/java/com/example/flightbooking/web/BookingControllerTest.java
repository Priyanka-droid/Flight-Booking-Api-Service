package com.example.flightbooking.web;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void missingFlightIdReturns400() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seats\":3,\"passenger\":\"Jane Doe\"}"))
                .andExpect(status().isBadRequest());
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
