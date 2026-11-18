package com.cinema.payment.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BookingClientTest {

    private MockRestServiceServer server;
    private BookingClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8082");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new BookingClient(builder.build());
    }

    @Test
    void confirmBooking_ConfirmedResponse_Succeeds() {
        server.expect(requestTo("http://localhost:8082/api/v2/bookings/BK-001"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(jsonPath("$.action").value("CONFIRM"))
                .andRespond(withSuccess("{\"id\":\"BK-001\",\"status\":\"CONFIRMED\"}", MediaType.APPLICATION_JSON));

        client.confirmBooking("BK-001");

        server.verify();
    }

    @Test
    void confirmBooking_NotFound_ThrowsNotFound() {
        server.expect(requestTo("http://localhost:8082/api/v2/bookings/BK-999"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withResourceNotFound());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> client.confirmBooking("BK-999")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        server.verify();
    }

    @Test
    void confirmBooking_Conflict_ThrowsConflict() {
        server.expect(requestTo("http://localhost:8082/api/v2/bookings/BK-001"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> client.confirmBooking("BK-001")
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        server.verify();
    }

    @Test
    void confirmBooking_ServiceUnavailable_ThrowsServiceUnavailable() {
        server.expect(requestTo("http://localhost:8082/api/v2/bookings/BK-001"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withServiceUnavailable());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> client.confirmBooking("BK-001")
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        server.verify();
    }

    @Test
    void confirmBooking_ConnectionFailure_ThrowsServiceUnavailable() {
        server.expect(requestTo("http://localhost:8082/api/v2/bookings/BK-001"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withException(new IOException("Connection refused")));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> client.confirmBooking("BK-001")
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        server.verify();
    }
}
