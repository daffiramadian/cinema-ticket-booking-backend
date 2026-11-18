package com.cinema.payment.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Component
public class BookingClient {
    private static final int TIMEOUT_MS = 3000;

    private final RestClient restClient;

    @Autowired
    public BookingClient(@Value("${booking.base-url:http://localhost:8082}") String bookingBaseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(TIMEOUT_MS))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(TIMEOUT_MS));
        this.restClient = RestClient.builder()
                .baseUrl(bookingBaseUrl)
                .requestFactory(factory)
                .build();
    }

    BookingClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Confirms a pending booking in Booking Service before persisting a SUCCESS v2 payment.
     */
    public void confirmBooking(String bookingId) {
        try {
            BookingConfirmationResponse response = restClient.patch()
                    .uri("/api/v2/bookings/{id}", bookingId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("action", "CONFIRM"))
                    .retrieve()
                    .body(BookingConfirmationResponse.class);

            if (response == null || !"CONFIRMED".equals(response.status())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking " + bookingId + " could not be confirmed");
            }
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking " + bookingId + " not found");
        } catch (HttpClientErrorException.Conflict ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking " + bookingId + " cannot be confirmed");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Booking Service is unavailable, payment could not be confirmed"
            );
        }
    }

    private record BookingConfirmationResponse(String id, String status) {}
}
